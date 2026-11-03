package com.zhihang.grade.dao;

import com.zhihang.grade.config.Database;
import com.zhihang.grade.model.AttentionRow;
import com.zhihang.grade.model.CourseStat;
import com.zhihang.grade.model.DistributionPoint;
import com.zhihang.grade.model.GradeStats;
import com.zhihang.grade.model.StudentRank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatsDao {

    public GradeStats stats(Long classId, Long courseId, String examName) {
        List<Double> scores = queryScores(classId, courseId, examName);
        GradeStats stats = new GradeStats();
        stats.setRecordCount(scores.size());
        if (scores.isEmpty()) {
            stats.setStudentCount(0);
            stats.setAverage(0);
            stats.setPassRate(0);
            stats.setExcellentRate(0);
            stats.setHighest(0);
            stats.setLowest(0);
            stats.setMedian(0);
            stats.setFailCount(0);
            return stats;
        }
        double sum = 0;
        int pass = 0;
        int excellent = 0;
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        for (double score : scores) {
            sum += score;
            if (score >= 60) pass++;
            if (score >= 90) excellent++;
            max = Math.max(max, score);
            min = Math.min(min, score);
        }
        stats.setAverage(round(sum / scores.size()));
        stats.setPassRate(round(pass * 100.0 / scores.size()));
        stats.setExcellentRate(round(excellent * 100.0 / scores.size()));
        stats.setHighest(round(max));
        stats.setLowest(round(min));
        stats.setMedian(round(median(scores)));
        stats.setFailCount(scores.size() - pass);
        stats.setStudentCount(countStudents(classId, courseId, examName));
        return stats;
    }

    public List<DistributionPoint> distribution(Long classId, Long courseId, String examName) {
        List<Double> scores = queryScores(classId, courseId, examName);
        int[] counts = new int[5];
        for (double score : scores) {
            if (score < 60) counts[0]++;
            else if (score < 70) counts[1]++;
            else if (score < 80) counts[2]++;
            else if (score < 90) counts[3]++;
            else counts[4]++;
        }
        String[] labels = {"不及格", "及格", "中等", "良好", "优秀"};
        String[] tones = {"fail", "pass", "medium", "good", "excellent"};
        List<DistributionPoint> points = new ArrayList<>();
        for (int i = 0; i < counts.length; i++) {
            double percent = scores.isEmpty() ? 0 : round(counts[i] * 100.0 / scores.size());
            points.add(new DistributionPoint(labels[i], counts[i], percent, tones[i]));
        }
        return points;
    }

    public List<CourseStat> courseStats(long classId, String examName) {
        String sql = """
                SELECT co.id, co.name, co.code, co.teacher, co.color, co.credit,
                       COUNT(g.id) AS record_count,
                       COUNT(DISTINCT g.student_id) AS student_count,
                       COALESCE(AVG(g.score), 0) AS average_score,
                       COALESCE(SUM(CASE WHEN g.score >= 60 THEN 1 ELSE 0 END) * 100.0
                                / NULLIF(COUNT(g.id), 0), 0) AS pass_rate,
                       COALESCE(SUM(CASE WHEN g.score >= 90 THEN 1 ELSE 0 END) * 100.0
                                / NULLIF(COUNT(g.id), 0), 0) AS excellent_rate,
                       COALESCE(MAX(g.score), 0) AS highest,
                       COALESCE(MIN(g.score), 0) AS lowest
                FROM courses co
                JOIN grades g ON g.course_id = co.id
                JOIN students s ON s.id = g.student_id
                WHERE s.class_id = ?
                %s
                GROUP BY co.id, co.name, co.code, co.teacher, co.color, co.credit
                ORDER BY co.id
                """.formatted(examName == null || examName.isBlank() ? "" : "AND g.exam_name = ?");
        List<CourseStat> list = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, classId);
            if (examName != null && !examName.isBlank()) {
                ps.setString(2, examName);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CourseStat stat = new CourseStat();
                    stat.setCourseId(rs.getLong("id"));
                    stat.setCourseName(rs.getString("name"));
                    stat.setCourseCode(rs.getString("code"));
                    stat.setTeacher(rs.getString("teacher"));
                    stat.setColor(rs.getString("color"));
                    stat.setCredit(rs.getDouble("credit"));
                    stat.setRecordCount(rs.getInt("record_count"));
                    stat.setStudentCount(rs.getInt("student_count"));
                    stat.setAverage(round(rs.getDouble("average_score")));
                    stat.setPassRate(round(rs.getDouble("pass_rate")));
                    stat.setExcellentRate(round(rs.getDouble("excellent_rate")));
                    stat.setHighest(round(rs.getDouble("highest")));
                    stat.setLowest(round(rs.getDouble("lowest")));
                    list.add(stat);
                }
            }
            return list;
        } catch (SQLException ex) {
            throw new DaoException("查询课程统计失败", ex);
        }
    }

    public List<StudentRank> ranking(long classId, Long courseId, String examName, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT s.id AS student_id, s.student_no, s.name, c.name AS class_name,
                       AVG(g.score) AS average_score,
                       COUNT(g.id) AS record_count,
                       SUM(CASE WHEN g.score >= 60 THEN 1 ELSE 0 END) AS pass_count,
                       SUM(CASE WHEN g.score >= 60 THEN 1 ELSE 0 END) * 100.0 / COUNT(g.id) AS pass_rate
                FROM grades g
                JOIN students s ON s.id = g.student_id
                JOIN classes c ON c.id = s.class_id
                WHERE s.class_id = ?
                """);
        List<Object> params = new ArrayList<>();
        params.add(classId);
        if (courseId != null && courseId > 0) {
            sql.append(" AND g.course_id = ?");
            params.add(courseId);
        }
        if (examName != null && !examName.isBlank()) {
            sql.append(" AND g.exam_name = ?");
            params.add(examName);
        }
        sql.append("""
                GROUP BY s.id, s.student_no, s.name, c.name
                ORDER BY average_score DESC, s.student_no ASC
                LIMIT ?
                """);
        params.add(limit);
        List<StudentRank> list = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object value = params.get(i);
                if (value instanceof Long) {
                    ps.setLong(i + 1, (Long) value);
                } else if (value instanceof Integer) {
                    ps.setInt(i + 1, (Integer) value);
                } else {
                    ps.setString(i + 1, String.valueOf(value));
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    StudentRank row = new StudentRank();
                    row.setRank(rank++);
                    row.setStudentId(rs.getLong("student_id"));
                    row.setStudentNo(rs.getString("student_no"));
                    row.setName(rs.getString("name"));
                    row.setClassName(rs.getString("class_name"));
                    row.setAverage(round(rs.getDouble("average_score")));
                    row.setRecordCount(rs.getInt("record_count"));
                    row.setPassCount(rs.getInt("pass_count"));
                    row.setPassRate(round(rs.getDouble("pass_rate")));
                    list.add(row);
                }
            }
            return list;
        } catch (SQLException ex) {
            throw new DaoException("查询排名失败", ex);
        }
    }

    public List<AttentionRow> attention(long classId, Long courseId, String examName, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT s.id AS student_id, s.student_no, s.name AS student_name,
                       c.name AS class_name, g.course_id, co.name AS course_name, g.exam_name, g.score
                FROM grades g
                JOIN students s ON s.id = g.student_id
                JOIN classes c ON c.id = s.class_id
                JOIN courses co ON co.id = g.course_id
                WHERE s.class_id = ? AND g.score < 60
                """);
        List<Object> params = new ArrayList<>();
        params.add(classId);
        if (courseId != null && courseId > 0) {
            sql.append(" AND g.course_id = ?");
            params.add(courseId);
        }
        if (examName != null && !examName.isBlank()) {
            sql.append(" AND g.exam_name = ?");
            params.add(examName);
        }
        sql.append(" ORDER BY g.score ASC, s.student_no ASC LIMIT ?");
        params.add(Math.max(limit * 10, 50));
        List<AttentionRow> list = new ArrayList<>();
        java.util.Set<Long> seen = new java.util.HashSet<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object value = params.get(i);
                if (value instanceof Long) {
                    ps.setLong(i + 1, (Long) value);
                } else if (value instanceof Integer) {
                    ps.setInt(i + 1, (Integer) value);
                } else {
                    ps.setString(i + 1, String.valueOf(value));
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long studentId = rs.getLong("student_id");
                    if (!seen.add(studentId)) {
                        continue;
                    }
                    AttentionRow row = new AttentionRow();
                    row.setStudentId(rs.getLong("student_id"));
                    row.setStudentNo(rs.getString("student_no"));
                    row.setStudentName(rs.getString("student_name"));
                    row.setClassName(rs.getString("class_name"));
                    row.setCourseId(rs.getLong("course_id"));
                    row.setCourseName(rs.getString("course_name"));
                    row.setExamName(rs.getString("exam_name"));
                    row.setScore(round(rs.getDouble("score")));
                    list.add(row);
                    if (list.size() >= limit) {
                        break;
                    }
                }
            }
            return list;
        } catch (SQLException ex) {
            throw new DaoException("查询不及格名单失败", ex);
        }
    }

    public int countCoursesWithGrades(long classId) {
        String sql = """
                SELECT COUNT(DISTINCT g.course_id)
                FROM grades g JOIN students s ON s.id = g.student_id
                WHERE s.class_id = ?
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            throw new DaoException("统计课程数量失败", ex);
        }
    }

    private List<Double> queryScores(Long classId, Long courseId, String examName) {
        StringBuilder sql = new StringBuilder("""
                SELECT g.score
                FROM grades g
                JOIN students s ON s.id = g.student_id
                WHERE 1 = 1
                """);
        List<Object> params = new ArrayList<>();
        if (classId != null && classId > 0) {
            sql.append(" AND s.class_id = ?");
            params.add(classId);
        }
        if (courseId != null && courseId > 0) {
            sql.append(" AND g.course_id = ?");
            params.add(courseId);
        }
        if (examName != null && !examName.isBlank()) {
            sql.append(" AND g.exam_name = ?");
            params.add(examName);
        }
        sql.append(" ORDER BY g.score");
        List<Double> scores = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object value = params.get(i);
                if (value instanceof Long) {
                    ps.setLong(i + 1, (Long) value);
                } else {
                    ps.setString(i + 1, String.valueOf(value));
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    scores.add(rs.getDouble(1));
                }
            }
            return scores;
        } catch (SQLException ex) {
            throw new DaoException("查询成绩统计失败", ex);
        }
    }

    private int countStudents(Long classId, Long courseId, String examName) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(DISTINCT g.student_id)
                FROM grades g JOIN students s ON s.id = g.student_id
                WHERE 1 = 1
                """);
        List<Object> params = new ArrayList<>();
        if (classId != null && classId > 0) {
            sql.append(" AND s.class_id = ?");
            params.add(classId);
        }
        if (courseId != null && courseId > 0) {
            sql.append(" AND g.course_id = ?");
            params.add(courseId);
        }
        if (examName != null && !examName.isBlank()) {
            sql.append(" AND g.exam_name = ?");
            params.add(examName);
        }
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object value = params.get(i);
                if (value instanceof Long) {
                    ps.setLong(i + 1, (Long) value);
                } else {
                    ps.setString(i + 1, String.valueOf(value));
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            throw new DaoException("统计学生数量失败", ex);
        }
    }

    private double median(List<Double> scores) {
        List<Double> copy = new ArrayList<>(scores);
        Collections.sort(copy);
        int size = copy.size();
        if (size == 0) {
            return 0;
        }
        if (size % 2 == 1) {
            return copy.get(size / 2);
        }
        return (copy.get(size / 2 - 1) + copy.get(size / 2)) / 2.0;
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
