package com.zhihang.grade.dao;

import com.zhihang.grade.config.Database;
import com.zhihang.grade.model.ClassInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClassDao {

    public List<ClassInfo> findAll() {
        String sql = """
                SELECT c.id, c.name, c.grade_year, c.major, c.advisor,
                       COUNT(DISTINCT s.id) AS student_count,
                       COUNT(g.id) AS grade_count,
                       COALESCE(AVG(g.score), 0) AS average_score,
                       COALESCE(SUM(CASE WHEN g.score >= 60 THEN 1 ELSE 0 END) * 100.0
                                / NULLIF(COUNT(g.id), 0), 0) AS pass_rate
                FROM classes c
                LEFT JOIN students s ON s.class_id = c.id
                LEFT JOIN grades g ON g.student_id = s.id
                GROUP BY c.id, c.name, c.grade_year, c.major, c.advisor
                ORDER BY c.id
                """;
        return query(sql);
    }

    public List<ClassInfo> findAllWithStats() {
        return findAll();
    }

    public ClassInfo findById(long id) {
        String sql = """
                SELECT c.id, c.name, c.grade_year, c.major, c.advisor,
                       (SELECT COUNT(*) FROM students s WHERE s.class_id = c.id) AS student_count,
                       (SELECT COUNT(*) FROM grades g JOIN students s ON s.id = g.student_id
                        WHERE s.class_id = c.id) AS grade_count,
                       (SELECT COALESCE(AVG(g.score), 0) FROM grades g JOIN students s ON s.id = g.student_id
                        WHERE s.class_id = c.id) AS average_score,
                       (SELECT COALESCE(SUM(CASE WHEN g.score >= 60 THEN 1 ELSE 0 END) * 100.0
                                         / NULLIF(COUNT(g.id), 0), 0)
                        FROM grades g JOIN students s ON s.id = g.student_id
                        WHERE s.class_id = c.id) AS pass_rate
                FROM classes c WHERE c.id = ?
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException ex) {
            throw new DaoException("查询班级失败", ex);
        }
    }

    public boolean nameExists(Connection connection, String name, Long excludeId) throws SQLException {
        String sql = excludeId == null
                ? "SELECT COUNT(*) FROM classes WHERE LOWER(name) = LOWER(?)"
                : "SELECT COUNT(*) FROM classes WHERE LOWER(name) = LOWER(?) AND id <> ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            if (excludeId != null) {
                ps.setLong(2, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public long insert(Connection connection, ClassInfo item) throws SQLException {
        String sql = "INSERT INTO classes (name, grade_year, major, advisor) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getGradeYear());
            ps.setString(3, item.getMajor());
            ps.setString(4, item.getAdvisor());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("新增班级后未取得主键");
    }

    public void update(Connection connection, ClassInfo item) throws SQLException {
        String sql = "UPDATE classes SET name = ?, grade_year = ?, major = ?, advisor = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getGradeYear());
            ps.setString(3, item.getMajor());
            ps.setString(4, item.getAdvisor());
            ps.setLong(5, item.getId());
            ps.executeUpdate();
        }
    }

    public int countStudents(Connection connection, long classId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM students WHERE class_id = ?")) {
            ps.setLong(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void delete(Connection connection, long id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM classes WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public long count() {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM classes");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException ex) {
            throw new DaoException("统计班级失败", ex);
        }
    }

    private List<ClassInfo> query(String sql) {
        List<ClassInfo> list = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (SQLException ex) {
            throw new DaoException("查询班级失败", ex);
        }
    }

    private ClassInfo map(ResultSet rs) throws SQLException {
        ClassInfo item = new ClassInfo();
        item.setId(rs.getLong("id"));
        item.setName(rs.getString("name"));
        item.setGradeYear(rs.getString("grade_year"));
        item.setMajor(rs.getString("major"));
        item.setAdvisor(rs.getString("advisor"));
        item.setStudentCount(rs.getInt("student_count"));
        item.setGradeCount(rs.getInt("grade_count"));
        item.setAverageScore(Math.round(rs.getDouble("average_score") * 10.0) / 10.0);
        item.setPassRate(Math.round(rs.getDouble("pass_rate") * 10.0) / 10.0);
        return item;
    }
}
