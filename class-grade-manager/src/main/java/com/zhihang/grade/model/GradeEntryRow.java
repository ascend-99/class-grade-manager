package com.zhihang.grade.model;

import java.sql.Timestamp;

public class GradeEntryRow {

    private long studentId;
    private String studentNo;
    private String studentName;
    private String gender;
    private Double score;
    private Long gradeId;
    private Timestamp updatedAt;

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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Long getGradeId() {
        return gradeId;
    }

    public void setGradeId(Long gradeId) {
        this.gradeId = gradeId;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getInitial() {
        return studentName == null || studentName.isBlank() ? "?" : studentName.substring(0, 1);
    }

    public boolean isRecorded() {
        return score != null;
    }
}
