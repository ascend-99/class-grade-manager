package com.zhihang.grade.model;

import java.sql.Timestamp;

public class GradeLog {

    private long id;
    private Long gradeId;
    private long studentId;
    private String studentNo;
    private String studentName;
    private long courseId;
    private String courseName;
    private String className;
    private String examName;
    private Double oldScore;
    private Double newScore;
    private String actionType;
    private String changedBy;
    private String reason;
    private Timestamp changedAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getGradeId() {
        return gradeId;
    }

    public void setGradeId(Long gradeId) {
        this.gradeId = gradeId;
    }

    public long getStudentId() {
        return studentId;
    }

    public void setStudentId(long studentId) {
        this.studentId = studentId;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public long getCourseId() {
        return courseId;
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public Double getOldScore() {
        return oldScore;
    }

    public void setOldScore(Double oldScore) {
        this.oldScore = oldScore;
    }

    public Double getNewScore() {
        return newScore;
    }

    public void setNewScore(Double newScore) {
        this.newScore = newScore;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Timestamp getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Timestamp changedAt) {
        this.changedAt = changedAt;
    }

    public String getActionLabel() {
        if ("CREATE".equals(actionType)) return "录入";
        if ("DELETE".equals(actionType)) return "删除";
        return "修改";
    }

    public String getActionTone() {
        if ("CREATE".equals(actionType)) return "success";
        if ("DELETE".equals(actionType)) return "danger";
        return "warning";
    }

    public String getInitial() {
        return studentName == null || studentName.isBlank() ? "?" : studentName.substring(0, 1);
    }
}
