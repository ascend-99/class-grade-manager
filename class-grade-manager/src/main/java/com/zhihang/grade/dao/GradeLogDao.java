package com.zhihang.grade.dao;

import com.zhihang.grade.config.Database;
import com.zhihang.grade.model.GradeLog;
import com.zhihang.grade.model.PageResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GradeLogDao {

    private static final String SELECT_BASE = """
            SELECT l.id, l.grade_id, l.student_id, l.student_no,
                   COALESCE(l.student_name, s.name) AS student_name,
                   l.course_id, l.course_name, c.name AS class_name, l.exam_name,
                   l.old_score, l.new_score, l.action_type, l.changed_by, l.reason, l.changed_at
            FROM grade_logs l
            LEFT JOIN students s ON s.id = l.student_id
            LEFT JOIN classes c ON c.id = s.class_id
            """;

    public void record(Connection connection, Long gradeId, long studentId, String studentNo,
                       String studentName, long courseId, String courseName, String examName,
                       Double oldScore, Double newScore, String actionType, String changedBy,
                       String reason) throws SQLException {
        String sql = """
                INSERT INTO grade_logs (grade_id, student_id, student_no, student_name, course_id,
                                        course_name, exam_name, old_score, new_score, action_type,
                                        changed_by, reason)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (gradeId == null) {
                ps.setObject(1, null);
            } else {
                ps.setLong(1, gradeId);
            }
            ps.setLong(2, studentId);
            ps.setString(3, studentNo);
            ps.setString(4, studentName);
            ps.setLong(5, courseId);
            ps.setString(6, courseName);
            ps.setString(7, examName);
            if (oldScore == null) {
                ps.setObject(8, null);
            } else {
                ps.setDouble(8, oldScore);
            }
            if (newScore == null) {
                ps.setObject(9, null);
            } else {
                ps.setDouble(9, newScore);
            }
            ps.setString(10, actionType);
            ps.setString(11, changedBy);
            ps.setString(12, reason);
            ps.executeUpdate();
        }
    }

    public PageResult<GradeLog> findPage(Long classId, Long courseId, Long studentId,
                                         String action, String keyword, int page, int size) {
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, Math.min(50, size));
        Where where = buildWhere(classId, courseId, studentId, action, keyword);
        long total = count(where);
        int totalPages = Math.max(1, (int) Math.ceil((double) total / safeSize));
        if (safePage > totalPages) {
            safePage = totalPages;
        }
        String sql = SELECT_BASE + " WHERE " + where.sql()
                + " ORDER BY l.changed_at DESC, l.id DESC LIMIT ? OFFSET ?";
        List<Object> params = new ArrayList<>(where.params());
        params.add(safeSize);
        params.add((safePage - 1) * safeSize);
        List<GradeLog> logs = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DaoException("查询成绩修改记录失败", ex);
        }
        return new PageResult<>(logs, safePage, safeSize, total);
    }

    public long countAll() {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM grade_logs");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException ex) {
            throw new DaoException("统计修改记录失败", ex);
        }
    }

    private long count(Where where) {
        String sql = "SELECT COUNT(*) FROM grade_logs l "
                + "LEFT JOIN students s ON s.id = l.student_id WHERE " + where.sql();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, where.params());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException ex) {
            throw new DaoException("统计修改记录失败", ex);
        }
    }

    private Where buildWhere(Long classId, Long courseId, Long studentId,
                             String action, String keyword) {
        StringBuilder sql = new StringBuilder("1 = 1");
        List<Object> params = new ArrayList<>();
        if (classId != null && classId > 0) {
            sql.append(" AND s.class_id = ?");
            params.add(classId);
        }
        if (courseId != null && courseId > 0) {
            sql.append(" AND l.course_id = ?");
            params.add(courseId);
        }
        if (studentId != null && studentId > 0) {
            sql.append(" AND l.student_id = ?");
            params.add(studentId);
        }
        if (action != null && !action.isBlank()) {
            sql.append(" AND l.action_type = ?");
            params.add(action);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(COALESCE(l.student_name, s.name)) LIKE ?"
                    + " OR LOWER(l.student_no) LIKE ? OR LOWER(l.course_name) LIKE ?"
                    + " OR LOWER(l.exam_name) LIKE ? OR LOWER(l.reason) LIKE ?)");
            String like = "%" + keyword.trim().toLowerCase() + "%";
            for (int i = 0; i < 5; i++) {
                params.add(like);
            }
        }
        return new Where(sql.toString(), params);
    }

    private void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            if (value instanceof Integer) {
                ps.setInt(i + 1, (Integer) value);
            } else if (value instanceof Long) {
                ps.setLong(i + 1, (Long) value);
            } else {
                ps.setString(i + 1, String.valueOf(value));
            }
        }
    }

    private GradeLog map(ResultSet rs) throws SQLException {
        GradeLog log = new GradeLog();
        log.setId(rs.getLong("id"));
        Object gradeId = rs.getObject("grade_id");
        log.setGradeId(gradeId == null ? null : ((Number) gradeId).longValue());
        log.setStudentId(rs.getLong("student_id"));
        log.setStudentNo(rs.getString("student_no"));
        log.setStudentName(rs.getString("student_name"));
        log.setCourseId(rs.getLong("course_id"));
        log.setCourseName(rs.getString("course_name"));
        log.setClassName(rs.getString("class_name"));
        log.setExamName(rs.getString("exam_name"));
        Object oldScore = rs.getObject("old_score");
        log.setOldScore(oldScore == null ? null : ((Number) oldScore).doubleValue());
        Object newScore = rs.getObject("new_score");
        log.setNewScore(newScore == null ? null : ((Number) newScore).doubleValue());
        log.setActionType(rs.getString("action_type"));
        log.setChangedBy(rs.getString("changed_by"));
        log.setReason(rs.getString("reason"));
        log.setChangedAt(rs.getTimestamp("changed_at"));
        return log;
    }

    private record Where(String sql, List<Object> params) {
    }
}
