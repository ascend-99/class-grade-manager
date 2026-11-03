package com.zhihang.grade.dao;

import com.zhihang.grade.config.Database;
import com.zhihang.grade.model.UserAccount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {

    private static final String SELECT_BASE = """
            SELECT u.id, u.username, u.password_hash, u.role_code, u.display_name, u.student_id,
                   u.avatar_color, u.last_login_at,
                   s.student_no, c.name AS class_name
            FROM users u
            LEFT JOIN students s ON s.id = u.student_id
            LEFT JOIN classes c ON c.id = s.class_id
            """;

    public UserAccount findByUsername(String username) {
        String sql = SELECT_BASE + " WHERE LOWER(u.username) = LOWER(?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException ex) {
            throw new DaoException("查询用户失败", ex);
        }
    }

    public UserAccount findById(long id) {
        String sql = SELECT_BASE + " WHERE u.id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException ex) {
            throw new DaoException("查询用户失败", ex);
        }
    }

    public void touchLastLogin(long userId) {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "UPDATE users SET last_login_at = CURRENT_TIMESTAMP WHERE id = ?")) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DaoException("更新登录时间失败", ex);
        }
    }

    public void updatePassword(long userId, String passwordHash) {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "UPDATE users SET password_hash = ? WHERE id = ?")) {
            ps.setString(1, passwordHash);
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DaoException("修改密码失败", ex);
        }
    }

    public void updatePassword(Connection connection, long userId, String passwordHash) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE users SET password_hash = ? WHERE id = ?")) {
            ps.setString(1, passwordHash);
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }

    public boolean usernameExists(Connection connection, String username, Long excludeStudentId)
            throws SQLException {
        String sql = excludeStudentId == null
                ? "SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(?)"
                : "SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(?) AND (student_id IS NULL OR student_id <> ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            if (excludeStudentId != null) {
                ps.setLong(2, excludeStudentId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public void createStudentUser(Connection connection, String username, String passwordHash,
                                  String displayName, long studentId, String color) throws SQLException {
        String sql = """
                INSERT INTO users (username, password_hash, role_code, display_name, student_id, avatar_color)
                VALUES (?, ?, 'STUDENT', ?, ?, ?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, displayName);
            ps.setLong(4, studentId);
            ps.setString(5, color);
            ps.executeUpdate();
        }
    }

    public void updateStudentAccount(Connection connection, long studentId, String username,
                                     String displayName, String color) throws SQLException {
        String sql = """
                UPDATE users SET username = ?, display_name = ?, avatar_color = ?
                WHERE student_id = ?
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, displayName);
            ps.setString(3, color);
            ps.setLong(4, studentId);
            ps.executeUpdate();
        }
    }

    public boolean existsStudentAccount(Connection connection, long studentId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE student_id = ?")) {
            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    private UserAccount map(ResultSet rs) throws SQLException {
        UserAccount user = new UserAccount();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(rs.getString("role_code"));
        user.setDisplayName(rs.getString("display_name"));
        Object studentId = rs.getObject("student_id");
        user.setStudentId(studentId == null ? null : ((Number) studentId).longValue());
        user.setAvatarColor(rs.getString("avatar_color"));
        user.setLastLoginAt(rs.getTimestamp("last_login_at"));
        user.setStudentNo(rs.getString("student_no"));
        user.setClassName(rs.getString("class_name"));
        return user;
    }
}
