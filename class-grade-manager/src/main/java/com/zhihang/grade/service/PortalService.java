package com.zhihang.grade.service;

import com.zhihang.grade.dao.ClassDao;
import com.zhihang.grade.dao.CourseDao;
import com.zhihang.grade.dao.GradeDao;
import com.zhihang.grade.dao.StudentDao;
import com.zhihang.grade.model.ClassInfo;
import com.zhihang.grade.model.Course;
import com.zhihang.grade.model.GradeRow;
import com.zhihang.grade.model.PortalCourse;
import com.zhihang.grade.model.PortalSummary;
import com.zhihang.grade.model.Student;
import com.zhihang.grade.model.TrendPoint;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PortalService {

    private static final DateTimeFormatter DATE_LABEL = DateTimeFormatter.ofPattern("MM月dd日");

    private final StudentDao studentDao = new StudentDao();
    private final ClassDao classDao = new ClassDao();
    private final GradeDao gradeDao = new GradeDao();
    private final CourseDao courseDao = new CourseDao();
    private final GradeService gradeService = new GradeService();

    public PortalSummary load(long studentId) {
        Student student = studentDao.findById(studentId);
        if (student == null) {
            return null;
        }
        ClassInfo classInfo = classDao.findById(student.getClassId());
        List<GradeRow> classGrades = gradeDao.findByClass(student.getClassId());
        List<GradeRow> ownGrades = new ArrayList<>();
        for (GradeRow row : classGrades) {
            if (row.getStudentId() == studentId) {
                ownGrades.add(row);
            }
        }
        List<Course> courses = courseDao.findAllBasic();
        List<String> examOrder = gradeService.examNames();
        Map<Long, Course> courseMap = new HashMap<>();
        for (Course course : courses) {
            courseMap.put(course.getId(), course);
        }

        Map<Long, Map<Long, GradeRow>> latestByStudent = new LinkedHashMap<>();
        Set<Long> classCourseIds = new HashSet<>();
        for (GradeRow row : classGrades) {
            classCourseIds.add(row.getCourseId());
            latestByStudent.computeIfAbsent(row.getStudentId(), key -> new HashMap<>());
            Map<Long, GradeRow> perCourse = latestByStudent.get(row.getStudentId());
            GradeRow current = perCourse.get(row.getCourseId());
            if (current == null || isAfter(row, current, examOrder)) {
                perCourse.put(row.getCourseId(), row);
            }
        }
        Map<Long, GradeRow> ownLatest = latestByStudent.getOrDefault(studentId, Map.of());

        Map<Long, Double> studentAverages = new LinkedHashMap<>();
        for (Map.Entry<Long, Map<Long, GradeRow>> entry : latestByStudent.entrySet()) {
            double sum = 0;
            int count = 0;
            for (GradeRow row : entry.getValue().values()) {
                sum += row.getScore();
                count++;
            }
            if (count > 0) {
                studentAverages.put(entry.getKey(), round(sum / count));
            }
        }
        List<Map.Entry<Long, Double>> ranked = new ArrayList<>(studentAverages.entrySet());
        ranked.sort((a, b) -> {
            int cmp = Double.compare(b.getValue(), a.getValue());
            return cmp != 0 ? cmp : Long.compare(a.getKey(), b.getKey());
        });
        int rank = 0;
        for (int i = 0; i < ranked.size(); i++) {
            if (ranked.get(i).getKey() == studentId) {
                rank = i + 1;
                break;
            }
        }

        Map<Long, Double> courseClassAverages = new HashMap<>();
        Map<Long, List<Double>> courseScores = new HashMap<>();
        for (Map.Entry<Long, Map<Long, GradeRow>> entry : latestByStudent.entrySet()) {
            for (Map.Entry<Long, GradeRow> courseEntry : entry.getValue().entrySet()) {
                courseScores.computeIfAbsent(courseEntry.getKey(), key -> new ArrayList<>())
                        .add(courseEntry.getValue().getScore());
            }
        }
        for (Map.Entry<Long, List<Double>> entry : courseScores.entrySet()) {
            double sum = 0;
            for (double value : entry.getValue()) {
                sum += value;
            }
            courseClassAverages.put(entry.getKey(), round(sum / entry.getValue().size()));
        }

        List<PortalCourse> portalCourses = new ArrayList<>();
        double sum = 0;
        int recorded = 0;
        int passed = 0;
        int excellent = 0;
        double totalCredits = 0;
        double earnedCredits = 0;
        double gpaPoints = 0;
        double attemptedCredits = 0;
        for (Course course : courses) {
            if (!classCourseIds.contains(course.getId())) {
                continue;
            }
            PortalCourse item = new PortalCourse();
            item.setCourseId(course.getId());
            item.setCourseName(course.getName());
            item.setCourseCode(course.getCode());
            item.setTeacher(course.getTeacher());
            item.setColor(course.getColor());
            item.setCredit(course.getCredit());
            item.setClassAverage(courseClassAverages.getOrDefault(course.getId(), 0.0));
            totalCredits += course.getCredit();
            GradeRow latest = ownLatest.get(course.getId());
            if (latest != null) {
                item.setScore(latest.getScore());
                item.setExamName(latest.getExamName());
                item.setDifference(round(latest.getScore() - item.getClassAverage()));
                recorded++;
                sum += latest.getScore();
                attemptedCredits += course.getCredit();
                gpaPoints += gradePoint(latest.getScore()) * course.getCredit();
                if (latest.getScore() >= 60) {
                    passed++;
                    earnedCredits += course.getCredit();
                }
                if (latest.getScore() >= 90) {
                    excellent++;
                }
            } else {
                item.setExamName("待录入");
            }
            portalCourses.add(item);
        }
        portalCourses.sort(Comparator.comparingLong(PortalCourse::getCourseId));

        PortalSummary summary = new PortalSummary();
        summary.setStudent(student);
        summary.setClassInfo(classInfo);
        summary.setCourses(portalCourses);
        summary.setAllGrades(sortOwnGrades(ownGrades));
        summary.setTrend(buildTrend(ownGrades));
        summary.setAverage(recorded == 0 ? 0 : round(sum / recorded));
        summary.setPassRate(recorded == 0 ? 0 : round(passed * 100.0 / recorded));
        summary.setGpa(attemptedCredits == 0 ? 0 : round(gpaPoints / attemptedCredits));
        summary.setTotalCredits(round(totalCredits));
        summary.setEarnedCredits(round(earnedCredits));
        summary.setRank(rank);
        summary.setClassSize(classInfo == null ? 0 : classInfo.getStudentCount());
        summary.setClassAverage(studentAverages.isEmpty() ? 0 : round(average(studentAverages.values())));
        summary.setGradedCourses(recorded);
        summary.setExcellentCount(excellent);
        summary.setFailCount(recorded - passed);
        summary.setTotalCourses(classCourseIds.size());
        return summary;
    }

    private List<GradeRow> sortOwnGrades(List<GradeRow> rows) {
        List<GradeRow> sorted = new ArrayList<>(rows);
        sorted.sort((a, b) -> {
            LocalDate da = toLocal(a.getExamDate());
            LocalDate db = toLocal(b.getExamDate());
            int cmp = db.compareTo(da);
            if (cmp != 0) return cmp;
            return Long.compare(b.getId(), a.getId());
        });
        return sorted;
    }

    private List<TrendPoint> buildTrend(List<GradeRow> rows) {
        Map<String, List<GradeRow>> byExam = new LinkedHashMap<>();
        for (GradeRow row : rows) {
            byExam.computeIfAbsent(row.getExamName(), key -> new ArrayList<>()).add(row);
        }
        List<TrendPoint> points = new ArrayList<>();
        for (Map.Entry<String, List<GradeRow>> entry : byExam.entrySet()) {
            double sum = 0;
            LocalDate examDate = null;
            for (GradeRow row : entry.getValue()) {
                sum += row.getScore();
                if (row.getExamDate() != null) {
                    examDate = row.getExamDate().toLocalDate();
                }
            }
            String dateLabel = examDate == null ? "" : examDate.format(DATE_LABEL);
            points.add(new TrendPoint(entry.getKey(), round(sum / entry.getValue().size()), dateLabel));
        }
        points.sort(Comparator.comparing(point -> {
            for (GradeRow row : rows) {
                if (row.getExamName().equals(point.getLabel())) {
                    return row.getExamDate() == null ? LocalDate.MIN : row.getExamDate().toLocalDate();
                }
            }
            return LocalDate.MIN;
        }));
        return points;
    }

    private boolean isAfter(GradeRow candidate, GradeRow current, List<String> examOrder) {
        LocalDate candidateDate = toLocal(candidate.getExamDate());
        LocalDate currentDate = toLocal(current.getExamDate());
        int cmp = candidateDate.compareTo(currentDate);
        if (cmp != 0) {
            return cmp > 0;
        }
        List<String> exams = examOrder;
        int candidateOrder = exams.indexOf(candidate.getExamName());
        int currentOrder = exams.indexOf(current.getExamName());
        if (candidateOrder != currentOrder) {
            return candidateOrder > currentOrder;
        }
        if (candidate.getUpdatedAt() != null && current.getUpdatedAt() != null) {
            return candidate.getUpdatedAt().after(current.getUpdatedAt());
        }
        return candidate.getId() > current.getId();
    }

    private LocalDate toLocal(Date date) {
        return date == null ? LocalDate.MIN : date.toLocalDate();
    }

    private double average(Iterable<Double> values) {
        double sum = 0;
        int count = 0;
        for (double value : values) {
            sum += value;
            count++;
        }
        return count == 0 ? 0 : sum / count;
    }

    private double gradePoint(double score) {
        if (score >= 90) return 4.0;
        if (score >= 85) return 3.7;
        if (score >= 82) return 3.3;
        if (score >= 78) return 3.0;
        if (score >= 75) return 2.7;
        if (score >= 72) return 2.3;
        if (score >= 68) return 2.0;
        if (score >= 64) return 1.5;
        if (score >= 60) return 1.0;
        return 0.0;
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
