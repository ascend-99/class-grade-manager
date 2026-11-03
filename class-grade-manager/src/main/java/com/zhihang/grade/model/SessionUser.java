package com.zhihang.grade.model;

import java.io.Serializable;

/**
 * 登录后放入 Session 的轻量用户对象。
 * 只保存展示所需字段，不保存密码等敏感信息。
 */
public class SessionUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String username;
    private String displayName;
    private String role;
    private Long studentId;
    private String avatarColor;

    public SessionUser() {
    }

    public SessionUser(long id, String username, String displayName, String role,
                       Long studentId, String avatarColor) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.role = role;
        this.studentId = studentId;
        this.avatarColor = avatarColor;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getAvatarColor() {
        return avatarColor == null ? "indigo" : avatarColor;
    }

    public void setAvatarColor(String avatarColor) {
        this.avatarColor = avatarColor;
    }

    public boolean isTeacher() {
        return "TEACHER".equals(role);
    }

    public boolean isStudent() {
        return "STUDENT".equals(role);
    }

    public String getRoleLabel() {
        return isTeacher() ? "教师" : "学生";
    }

    public String getInitial() {
        if (displayName == null || displayName.isBlank()) {
            return "知";
        }
        return displayName.substring(0, 1);
    }

    public String getHomePath() {
        return isTeacher() ? "/dashboard" : "/portal";
    }
}
