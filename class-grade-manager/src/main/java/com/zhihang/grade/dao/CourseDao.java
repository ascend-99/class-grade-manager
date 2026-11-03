package com.zhihang.grade.dao;

import com.zhihang.grade.config.Database;
import com.zhihang.grade.model.Course;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CourseDao {

    public List<Course> findAllWithStats(Long classId) {
        boolean byClass = classId != null && classId > 0;
        String sql = """
                SELECT c.id, c.code, c.name, c.credit, c.teacher, c.semester, c.color,
                       COUNT(g.id) AS grade_count,
                       COUNT(DISTINCT g.student_id) AS student_count,
                       COALESCE(AVG(g.score), 0) AS average_score,
                       COALESCE(SUM(CASE WHEN g.score >= 60 THEN 1 ELSE 0 END) * 100.0
                                / NULLIF(COUNT(g.id), 0), 0) AS pass_rate
                FROM courses c
                LEFT JOIN grades g ON g.course_id = c.id
                    AND (? = 0 OR g.student_id IN (SELECT s.id FROM students s WHERE s.class_id = ?))
                GROUP BY c.id, c.code, c.name, c.credit, c.teacher, c.semester, c.color
                ORDER BY c.id
                """;
        List<Course> list = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, byClass ? classId : 0);
            ps.setLong(2, byClass ? classId : 0);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapWithStats(rs));
                }
            }
            return list;
        } catch (SQLException ex) {
            throw new DaoException("查询课程失败", ex);
        }
    }

    public List<Course> findAllBasic() {
        String sql = """
                SELECT id, code, name, credit, teacher, semester, color
                FROM courses ORDER BY id
                """;
        List<Course> list = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapBase(rs));
            }
            return list;
        } catch (SQLException ex) {
            throw new DaoException("查询课程失败", ex);
        }
    }

    public Course findById(long id) {
        String sql = """
                SELECT id, code, name, credit, teacher, semester, color
                FROM courses WHERE id = ?
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapBase(rs) : null;
            }
        } catch (SQLException ex) {
            throw new DaoException("查询课程失败", ex);
        }
    }

    public boolean codeExists(Connection connection, String code, Long excludeId) throws SQLException {
        String sql = excludeId == null
                ? "SELECT COUNT(*) FROM courses WHERE LOWER(code) = LOWER(?)"
                : "SELECT COUNT(*) FROM courses WHERE LOWER(code) = LOWER(?) AND id <> ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, code);
            if (excludeId != null) {
                ps.setLong(2, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public long insert(Connection connection, Course course) throws SQLException {
        String sql = """
                INSERT INTO courses (code, name, credit, teacher, semester, color)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, course.getCode());
            ps.setString(2, course.getName());
            ps.setDouble(3, course.getCredit());
            ps.setString(4, course.getTeacher());
            ps.setString(5, course.getSemester());
            ps.setString(6, course.getColor());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("新增课程后未取得主键");
    }

    public void update(Connection connection, Course course) throws SQLException {
        String sql = """
                UPDATE courses SET code = ?, name = ?, credit = ?, teacher = ?, semester = ?, color = ?
                WHERE id = ?
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, course.getCode());
            ps.setString(2, course.getName());
            ps.setDouble(3, course.getCredit());
            ps.setString(4, course.getTeacher());
            ps.setString(5, course.getSemester());
            ps.setString(6, course.getColor());
            ps.setLong(7, course.getId());
            ps.executeUpdate();
        }
    }

    public boolean hasGrades(Connection connection, long courseId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM grades WHERE course_id = ?")) {
            ps.setLong(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public void delete(Connection connection, long id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM courses WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public long count() {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM courses");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException ex) {
            throw new DaoException("统计课程失败", ex);
        }
    }

    private Course mapBase(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setId(rs.getLong("id"));
        course.setCode(rs.getString("code"));
        course.setName(rs.getString("name"));
        course.setCredit(rs.getDouble("credit"));
        course.setTeacher(rs.getString("teacher"));
        course.setSemester(rs.getString("semester"));
        course.setColor(rs.getString("color"));
        return course;
    }

    private Course mapWithStats(ResultSet rs) throws SQLException {
        Course course = mapBase(rs);
        course.setGradeCount(rs.getInt("grade_count"));
        course.setStudentCount(rs.getInt("student_count"));
        course.setAverageScore(rs.getDouble("average_score"));
        course.setPassRate(rs.getDouble("pass_rate"));
        return course;
    }
}
