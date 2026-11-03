package com.zhihang.grade.service;

import com.zhihang.grade.config.Database;
import com.zhihang.grade.dao.CourseDao;
import com.zhihang.grade.dao.DaoException;
import com.zhihang.grade.dao.GradeDao;
import com.zhihang.grade.dao.GradeLogDao;
import com.zhihang.grade.model.Course;
import com.zhihang.grade.model.GradeEntryRow;
import com.zhihang.grade.model.GradeRow;
import com.zhihang.grade.model.PageResult;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GradeService {

    private static final List<String> DEFAULT_EXAMS =
            List.of("单元测验", "课堂测验", "期中考试", "期末考试", "补考");

    private final GradeDao gradeDao = new GradeDao();
    private final GradeLogDao gradeLogDao = new GradeLogDao();
    private final CourseDao courseDao = new CourseDao();

    public PageResult<GradeRow> page(Long classId, Long courseId, String examName,
                                     String keyword, int page, int size) {
        return gradeDao.findPage(classId, courseId, examName, keyword, page, size);
    }

    public List<GradeRow> forExport(Long classId, Long courseId, String examName, String keyword) {
        return gradeDao.findForExport(classId, courseId, examName, keyword);
    }

    public List<GradeEntryRow> entryRows(long classId, long courseId, String examName) {
        return gradeDao.findEntryRows(classId, courseId, examName);
    }

    public List<GradeRow> byStudent(long studentId) {
        return gradeDao.findByStudent(studentId);
    }

    public List<GradeRow> byClass(long classId) {
        return gradeDao.findByClass(classId);
    }

    public GradeRow findById(long id) {
        return gradeDao.findById(id);
    }

    public List<String> examNames() {
        Set<String> names = new LinkedHashSet<>(DEFAULT_EXAMS);
        names.addAll(gradeDao.distinctExams());
        return new ArrayList<>(names);
    }

    public LocalDate examDate(String examName) {
        if (examName == null) {
            return LocalDate.now();
        }
        return switch (examName) {
            case "单元测验" -> LocalDate.of(2026, 10, 10);
            case "课堂测验" -> LocalDate.of(2026, 10, 20);
            case "期中考试" -> LocalDate.of(2026, 10, 30);
            case "期末考试" -> LocalDate.of(2027, 1, 8);
            case "补考" -> LocalDate.of(2027, 2, 20);
            default -> LocalDate.now();
        };
    }

    public ServiceResult<BatchSummary> saveBatch(long classId, long courseId, String examName,
                                                 Map<Long, String> rawScores, String teacher) {
        return saveBatch(classId, courseId, examName, rawScores, teacher, "批量保存");
    }

    public ServiceResult<BatchSummary> saveBatch(long classId, long courseId, String examName,
                                                 Map<Long, String> rawScores, String teacher,
                                                 String reason) {
        Course course = courseDao.findById(courseId);
        if (course == null) {
            return ServiceResult.fail("课程不存在，请刷新后重试");
        }
        if (examName == null || examName.isBlank()) {
            return ServiceResult.fail("请选择考试名称");
        }
        if (examName.length() > 50) {
            return ServiceResult.fail("考试名称不能超过 50 个字符");
        }
        List<GradeEntryRow> roster = gradeDao.findEntryRows(classId, courseId, examName);
        if (roster.isEmpty()) {
            return ServiceResult.fail("该班级暂无学生，无法录入成绩");
        }
        Set<Long> validStudentIds = new LinkedHashSet<>();
        for (GradeEntryRow row : roster) {
            validStudentIds.add(row.getStudentId());
        }
        List<ScoreInput> inputs = new ArrayList<>();
        for (GradeEntryRow row : roster) {
            String raw = rawScores.get(row.getStudentId());
            if (raw == null || raw.isBlank()) {
                continue;
            }
            double score;
            try {
                score = Double.parseDouble(raw.trim());
            } catch (NumberFormatException ex) {
                return ServiceResult.fail("「" + row.getStudentName() + "」的成绩不是有效数字");
            }
            if (!Double.isFinite(score) || score < 0 || score > 100) {
                return ServiceResult.fail("「" + row.getStudentName() + "」的成绩需在 0-100 之间");
            }
            score = Math.round(score * 10.0) / 10.0;
            inputs.add(new ScoreInput(row, score));
        }
        if (inputs.isEmpty()) {
            return ServiceResult.fail("还没有填写任何成绩");
        }
        for (Long studentId : rawScores.keySet()) {
            if (!validStudentIds.contains(studentId)) {
                return ServiceResult.fail("提交的数据中包含不属于该班级的学生");
            }
        }
        BatchSummary summary = new BatchSummary();
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                LocalDate date = examDate(examName);
                for (ScoreInput input : inputs) {
                    boolean existed = input.row.getGradeId() != null;
                    Double oldScore = input.row.getScore();
                    gradeDao.upsert(connection, input.row.getStudentId(), courseId, examName,
                            date, input.score, teacher);
                    if (existed && oldScore != null
                            && Double.compare(oldScore, input.score) == 0) {
                        summary.updated++;
                        continue;
                    }
                    gradeLogDao.record(connection, input.row.getGradeId(), input.row.getStudentId(),
                            input.row.getStudentNo(), input.row.getStudentName(), courseId,
                            course.getName(), examName, oldScore, input.score,
                            existed ? "UPDATE" : "CREATE", teacher, reason);
                    if (existed) {
                        summary.updated++;
                    } else {
                        summary.created++;
                    }
                }
                connection.commit();
                return ServiceResult.ok("成绩保存成功：" + summary.created + " 条新增，" 
                        + summary.updated + " 条更新", summary);
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new DaoException("批量保存成绩失败", ex);
        }
    }

    public ServiceResult<Void> updateScore(long id, String rawScore, String teacher) {
        return updateScore(id, rawScore, teacher, "手动修改");
    }

    public ServiceResult<Void> updateScore(long id, String rawScore, String teacher, String reason) {
        GradeRow existing = gradeDao.findById(id);
        if (existing == null) {
            return ServiceResult.fail("成绩记录不存在或已被删除");
        }
        if (rawScore == null || rawScore.isBlank()) {
            return ServiceResult.fail("请输入 0-100 之间的成绩");
        }
        double score;
        try {
            score = Double.parseDouble(rawScore);
        } catch (NumberFormatException ex) {
            return ServiceResult.fail("请输入 0-100 之间的成绩");
        }
        if (!Double.isFinite(score) || score < 0 || score > 100) {
            return ServiceResult.fail("成绩需在 0-100 之间");
        }
        score = Math.round(score * 10.0) / 10.0;
        if (Double.compare(existing.getScore(), score) == 0) {
            return ServiceResult.ok("成绩没有发生变化");
        }
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                gradeDao.updateScore(connection, id, score, teacher);
                gradeLogDao.record(connection, id, existing.getStudentId(), existing.getStudentNo(),
                        existing.getStudentName(), existing.getCourseId(), existing.getCourseName(),
                        existing.getExamName(), existing.getScore(), score, "UPDATE", teacher,
                        trimReason(reason));
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new DaoException("修改成绩失败", ex);
        }
        return ServiceResult.ok("成绩已更新为 " + score + " 分");
    }

    public ServiceResult<Void> delete(long id) {
        return delete(id, "手动删除");
    }

    public ServiceResult<Void> delete(long id, String teacher) {
        GradeRow existing = gradeDao.findById(id);
        if (existing == null) {
            return ServiceResult.fail("成绩记录不存在或已被删除");
        }
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                gradeDao.delete(connection, id);
                gradeLogDao.record(connection, id, existing.getStudentId(), existing.getStudentNo(),
                        existing.getStudentName(), existing.getCourseId(), existing.getCourseName(),
                        existing.getExamName(), existing.getScore(), null, "DELETE", teacher,
                        "页面删除");
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new DaoException("删除成绩失败", ex);
        }
        return ServiceResult.ok("已删除「" + existing.getStudentName() + " · "
                + existing.getCourseName() + "」的成绩");
    }

    private String trimReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return "手动修改";
        }
        String value = reason.trim();
        return value.length() <= 300 ? value : value.substring(0, 300);
    }

    public long countAll() {
        return gradeDao.countAll();
    }

    public long countByClass(long classId) {
        return gradeDao.countByClass(classId);
    }

    public static class BatchSummary {
        private int created;
        private int updated;

        public int getCreated() {
            return created;
        }

        public int getUpdated() {
            return updated;
        }

        public int getTotal() {
            return created + updated;
        }
    }

    private record ScoreInput(GradeEntryRow row, double score) {
    }
}
