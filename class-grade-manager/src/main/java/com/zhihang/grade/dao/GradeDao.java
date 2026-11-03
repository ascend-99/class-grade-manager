package com.zhihang.grade.dao;

import com.zhihang.grade.config.Database;
import com.zhihang.grade.model.GradeEntryRow;
import com.zhihang.grade.model.GradeRow;
import com.zhihang.grade.model.PageResult;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class GradeDao {

    private static final String SELECT_BASE = """
            SELECT g.id, g.student_id, s.student_no, s.name AS student_name, s.class_id,
                   c.name AS class_name, g.course_id, co.name AS course_name, co.code AS course_code,
                   co.color AS course_color, g.exam_name, g.exam_date, g.score,
                   g.recorded_by, g.updated_at
            FROM grades g
            JOIN students s ON s.id = g.student_id
            JOIN classes c ON c.id = s.class_id
            JOIN courses co ON co.id = g.course_id
            """;

    public PageResult<GradeRow> findPage(Long classId, Long courseId, String examName,
                                         String keyword, int page, int size) {
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, Math.min(100, size));
        Where where = buildWhere(classId, courseId, examName, keyword);
        long total = count(where);
        int totalPages = Math.max(1, (int) Math.ceil((double) total / safeSize));
        if (safePage > totalPages) {
            safePage = totalPages;
        }
        String sql = SELECT_BASE + " WHERE " + where.sql()
                + " ORDER BY g.updated_at DESC, g.id DESC LIMIT ? OFFSET ?";
        List<Object> params = new ArrayList<>(where.params());
        params.add(safeSize);
        params.add((safePage - 1) * safeSize);
        List<GradeRow> rows = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DaoException("查询成绩失败", ex);
        }
        return new PageResult<>(rows, safePage, safeSize, total);
    }

    public List<GradeRow> findForExport(Long classId, Long courseId, String examName, String keyword) {
        Where where = buildWhere(classId, courseId, examName, keyword);
        String sql = SELECT_BASE + " WHERE " + where.sql() + " ORDER BY s.student_no, co.id, g.exam_name";
        List<GradeRow> rows = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, where.params());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(map(rs));
                }
            }
            return rows;
        } catch (SQLException ex) {
            throw new DaoException("导出成绩失败", ex);
        }
    }

    public List<GradeRow> findRecent(Long classId, int limit) {
        String sql = SELECT_BASE
                + (classId != null && classId > 0 ? " WHERE s.class_id = ?" : "")
                + " ORDER BY g.updated_at DESC, g.id DESC LIMIT ?";
        List<GradeRow> rows = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            int index = 1;
            if (classId != null && classId > 0) {
                ps.setLong(index++, classId);
            }
            ps.setInt(index, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(map(rs));
                }
            }
            return rows;
        } catch (SQLException ex) {
            throw new DaoException("查询最近成绩失败", ex);
        }
    }

    public List<GradeEntryRow> findEntryRows(long classId, long courseId, String examName) {
        String sql = """
                SELECT s.id AS student_id, s.student_no, s.name AS student_name, s.gender,
                       g.id AS grade_id, g.score, g.updated_at
                FROM students s
                LEFT JOIN grades g ON g.student_id = s.id AND g.course_id = ? AND g.exam_name = ?
                WHERE s.class_id = ?
                ORDER BY s.student_no
                """;
        List<GradeEntryRow> rows = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, courseId);
            ps.setString(2, examName);
            ps.setLong(3, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GradeEntryRow row = new GradeEntryRow();
                    row.setStudentId(rs.getLong("student_id"));
                    row.setStudentNo(rs.getString("student_no"));
                    row.setStudentName(rs.getString("student_name"));
                    row.setGender(rs.getString("gender"));
                    Object gradeId = rs.getObject("grade_id");
                    row.setGradeId(gradeId == null ? null : ((Number) gradeId).longValue());
                    Object score = rs.getObject("score");
                    row.setScore(score == null ? null : ((Number) score).doubleValue());
                    row.setUpdatedAt(rs.getTimestamp("updated_at"));
                    rows.add(row);
                }
            }
            return rows;
        } catch (SQLException ex) {
            throw new DaoException("查询录入名单失败", ex);
        }
    }

    public List<GradeRow> findByStudent(long studentId) {
        String sql = SELECT_BASE + " WHERE g.student_id = ? ORDER BY co.id, g.exam_date, g.id";
        List<GradeRow> rows = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(map(rs));
                }
            }
            return rows;
        } catch (SQLException ex) {
            throw new DaoException("查询个人成绩失败", ex);
        }
    }

    public List<GradeRow> findByClass(long classId) {
        String sql = SELECT_BASE + " WHERE s.class_id = ? ORDER BY s.student_no, co.id, g.exam_date";
        List<GradeRow> rows = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(map(rs));
                }
            }
            return rows;
        } catch (SQLException ex) {
            throw new DaoException("查询班级成绩失败", ex);
        }
    }

    public GradeRow findById(long id) {
        String sql = SELECT_BASE + " WHERE g.id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException ex) {
            throw new DaoException("查询成绩失败", ex);
        }
    }

    public void upsert(Connection connection, long studentId, long courseId, String examName,
                       LocalDate examDate, double score, String recordedBy) throws SQLException {
        String update = """
                UPDATE grades SET score = ?, exam_date = ?, recorded_by = ?, updated_at = CURRENT_TIMESTAMP
                WHERE student_id = ? AND course_id = ? AND exam_name = ?
                """;
        try (PreparedStatement ps = connection.prepareStatement(update)) {
            ps.setDouble(1, score);
            setDate(ps, 2, examDate);
            ps.setString(3, recordedBy);
            ps.setLong(4, studentId);
            ps.setLong(5, courseId);
            ps.setString(6, examName);
            if (ps.executeUpdate() > 0) {
                return;
            }
        }
        String insert = """
                INSERT INTO grades (student_id, course_id, exam_name, exam_date, score, recorded_by,
                                    recorded_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """;
        try (PreparedStatement ps = connection.prepareStatement(insert)) {
            ps.setLong(1, studentId);
            ps.setLong(2, courseId);
            ps.setString(3, examName);
            setDate(ps, 4, examDate);
            ps.setDouble(5, score);
            ps.setString(6, recordedBy);
            ps.executeUpdate();
        }
    }

    public void updateScore(long id, double score, String recordedBy) {
        try (Connection connection = Database.getConnection()) {
            updateScore(connection, id, score, recordedBy);
        } catch (SQLException ex) {
            throw new DaoException("修改成绩失败", ex);
        }
    }

    public void updateScore(Connection connection, long id, double score, String recordedBy)
            throws SQLException {
        String sql = """
                UPDATE grades SET score = ?, recorded_by = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, score);
            ps.setString(2, recordedBy);
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }

    public void delete(long id) {
        try (Connection connection = Database.getConnection()) {
            delete(connection, id);
        } catch (SQLException ex) {
            throw new DaoException("删除成绩失败", ex);
        }
    }

    public void delete(Connection connection, long id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM grades WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public List<String> distinctExams() {
        Set<String> exams = new LinkedHashSet<>();
        String sql = "SELECT DISTINCT exam_name FROM grades ORDER BY exam_name";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                exams.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            throw new DaoException("查询考试名称失败", ex);
        }
        return new ArrayList<>(exams);
    }

    public long countAll() {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM grades");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException ex) {
            throw new DaoException("统计成绩数量失败", ex);
        }
    }

    public long countByClass(long classId) {
        String sql = "SELECT COUNT(*) FROM grades g JOIN students s ON s.id = g.student_id WHERE s.class_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException ex) {
            throw new DaoException("统计班级成绩数量失败", ex);
        }
    }

    private long count(Where where) {
        String sql = """
                SELECT COUNT(*)
                FROM grades g
                JOIN students s ON s.id = g.student_id
                JOIN courses co ON co.id = g.course_id
                WHERE %s
                """.formatted(where.sql());
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, where.params());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException ex) {
            throw new DaoException("统计成绩数量失败", ex);
        }
    }

    private Where buildWhere(Long classId, Long courseId, String examName, String keyword) {
        StringBuilder sql = new StringBuilder("1 = 1");
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
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(s.name) LIKE ? OR LOWER(s.student_no) LIKE ?"
                    + " OR LOWER(co.name) LIKE ? OR LOWER(g.exam_name) LIKE ?)");
            String like = "%" + keyword.trim().toLowerCase() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
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

    private void setDate(PreparedStatement ps, int index, LocalDate date) throws SQLException {
        if (date == null) {
            ps.setDate(index, Date.valueOf(LocalDate.now()));
        } else {
            ps.setDate(index, Date.valueOf(date));
        }
    }

    private GradeRow map(ResultSet rs) throws SQLException {
        GradeRow row = new GradeRow();
        row.setId(rs.getLong("id"));
        row.setStudentId(rs.getLong("student_id"));
        row.setStudentNo(rs.getString("student_no"));
        row.setStudentName(rs.getString("student_name"));
        row.setClassId(rs.getLong("class_id"));
        row.setClassName(rs.getString("class_name"));
        row.setCourseId(rs.getLong("course_id"));
        row.setCourseName(rs.getString("course_name"));
        row.setCourseCode(rs.getString("course_code"));
        row.setCourseColor(rs.getString("course_color"));
        row.setExamName(rs.getString("exam_name"));
        row.setExamDate(rs.getDate("exam_date"));
        row.setScore(rs.getDouble("score"));
        row.setRecordedBy(rs.getString("recorded_by"));
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        row.setUpdatedAt(updatedAt);
        return row;
    }

    private record Where(String sql, List<Object> params) {
    }
}
