package com.zhihang.grade.dao;

import com.zhihang.grade.config.Database;
import com.zhihang.grade.model.PageResult;
import com.zhihang.grade.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StudentDao {

    private static final String SELECT_WITH_STATS = """
            SELECT s.id, s.student_no, s.name, s.gender, s.class_id, s.phone, s.email, s.status,
                   s.created_at, c.name AS class_name,
                   COALESCE(AVG(g.score), 0) AS average_score,
                   COALESCE(SUM(CASE WHEN g.score >= 60 THEN 1 ELSE 0 END) * 100.0
                            / NULLIF(COUNT(g.id), 0), 0) AS pass_rate,
                   COUNT(g.id) AS grade_count
            FROM students s
            JOIN classes c ON c.id = s.class_id
            LEFT JOIN grades g ON g.student_id = s.id
            """;

    public PageResult<Student> findPage(Long classId, String keyword, int page, int size) {
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, Math.min(50, size));
        Where where = buildWhere(classId, keyword);
        long total = count(where);
        int totalPages = Math.max(1, (int) Math.ceil((double) total / safeSize));
        if (safePage > totalPages) {
            safePage = totalPages;
        }
        String sql = SELECT_WITH_STATS + " WHERE " + where.sql()
                + " GROUP BY s.id, s.student_no, s.name, s.gender, s.class_id, s.phone, s.email,"
                + " s.status, s.created_at, c.name"
                + " ORDER BY s.student_no LIMIT ? OFFSET ?";
        List<Object> params = new ArrayList<>(where.params());
        params.add(safeSize);
        params.add((safePage - 1) * safeSize);
        List<Student> list = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapWithStats(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DaoException("查询学生列表失败", ex);
        }
        return new PageResult<>(list, safePage, safeSize, total);
    }

    public List<Student> findByClass(long classId) {
        String sql = """
                SELECT s.id, s.student_no, s.name, s.gender, s.class_id, s.phone, s.email, s.status,
                       s.created_at, c.name AS class_name
                FROM students s JOIN classes c ON c.id = s.class_id
                WHERE s.class_id = ?
                ORDER BY s.student_no
                """;
        List<Student> list = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBase(rs));
                }
            }
            return list;
        } catch (SQLException ex) {
            throw new DaoException("查询班级学生失败", ex);
        }
    }

    public Student findById(long id) {
        String sql = """
                SELECT s.id, s.student_no, s.name, s.gender, s.class_id, s.phone, s.email, s.status,
                       s.created_at, c.name AS class_name
                FROM students s JOIN classes c ON c.id = s.class_id
                WHERE s.id = ?
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapBase(rs) : null;
            }
        } catch (SQLException ex) {
            throw new DaoException("查询学生失败", ex);
        }
    }

    public boolean studentNoExists(Connection connection, String studentNo, Long excludeId)
            throws SQLException {
        String sql = excludeId == null
                ? "SELECT COUNT(*) FROM students WHERE LOWER(student_no) = LOWER(?)"
                : "SELECT COUNT(*) FROM students WHERE LOWER(student_no) = LOWER(?) AND id <> ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, studentNo);
            if (excludeId != null) {
                ps.setLong(2, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public long insert(Connection connection, Student student) throws SQLException {
        String sql = """
                INSERT INTO students (student_no, name, gender, class_id, phone, email, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, student.getStudentNo());
            ps.setString(2, student.getName());
            ps.setString(3, student.getGender());
            ps.setLong(4, student.getClassId());
            ps.setString(5, student.getPhone());
            ps.setString(6, student.getEmail());
            ps.setString(7, student.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("新增学生后未取得主键");
    }

    public void update(Connection connection, Student student) throws SQLException {
        String sql = """
                UPDATE students SET student_no = ?, name = ?, gender = ?, class_id = ?,
                                   phone = ?, email = ?, status = ?
                WHERE id = ?
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, student.getStudentNo());
            ps.setString(2, student.getName());
            ps.setString(3, student.getGender());
            ps.setLong(4, student.getClassId());
            ps.setString(5, student.getPhone());
            ps.setString(6, student.getEmail());
            ps.setString(7, student.getStatus());
            ps.setLong(8, student.getId());
            ps.executeUpdate();
        }
    }

    public void delete(Connection connection, long id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM students WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public long countAll() {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM students");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException ex) {
            throw new DaoException("统计学生人数失败", ex);
        }
    }

    public long countByClass(long classId) {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT COUNT(*) FROM students WHERE class_id = ?")) {
            ps.setLong(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException ex) {
            throw new DaoException("统计班级人数失败", ex);
        }
    }

    public List<Student> findAllBasic() {
        String sql = """
                SELECT s.id, s.student_no, s.name, s.gender, s.class_id, s.phone, s.email, s.status,
                       s.created_at, c.name AS class_name
                FROM students s JOIN classes c ON c.id = s.class_id
                ORDER BY s.class_id, s.student_no
                """;
        List<Student> list = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapBase(rs));
            }
            return list;
        } catch (SQLException ex) {
            throw new DaoException("查询学生失败", ex);
        }
    }

    private long count(Where where) {
        String sql = "SELECT COUNT(*) FROM students s WHERE " + where.sql();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, where.params());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException ex) {
            throw new DaoException("统计学生数量失败", ex);
        }
    }

    private Where buildWhere(Long classId, String keyword) {
        StringBuilder sql = new StringBuilder("1 = 1");
        List<Object> params = new ArrayList<>();
        if (classId != null && classId > 0) {
            sql.append(" AND s.class_id = ?");
            params.add(classId);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(s.name) LIKE ? OR LOWER(s.student_no) LIKE ? OR s.phone LIKE ?)");
            String like = "%" + keyword.trim().toLowerCase() + "%";
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

    private Student mapBase(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setId(rs.getLong("id"));
        student.setStudentNo(rs.getString("student_no"));
        student.setName(rs.getString("name"));
        student.setGender(rs.getString("gender"));
        student.setClassId(rs.getLong("class_id"));
        student.setClassName(rs.getString("class_name"));
        student.setPhone(rs.getString("phone"));
        student.setEmail(rs.getString("email"));
        student.setStatus(rs.getString("status"));
        student.setCreatedAt(rs.getTimestamp("created_at"));
        return student;
    }

    private Student mapWithStats(ResultSet rs) throws SQLException {
        Student student = mapBase(rs);
        student.setAverageScore(rs.getDouble("average_score"));
        student.setPassRate(rs.getDouble("pass_rate"));
        student.setGradeCount(rs.getInt("grade_count"));
        return student;
    }

    private record Where(String sql, List<Object> params) {
    }
}
