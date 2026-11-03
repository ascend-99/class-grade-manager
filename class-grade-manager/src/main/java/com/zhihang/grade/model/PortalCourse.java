package com.zhihang.grade.model;

public class PortalCourse {

    private long courseId;
    private String courseName;
    private String courseCode;
    private String teacher;
    private String color;
    private double credit;
    private Double score;
    private String examName;
    private double classAverage;
    private double difference;
    private int classRank;

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

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getColor() {
        return color == null || color.isBlank() ? "indigo" : color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public double getCredit() {
        return credit;
    }

    public void setCredit(double credit) {
        this.credit = credit;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public double getClassAverage() {
        return classAverage;
    }

    public void setClassAverage(double classAverage) {
        this.classAverage = classAverage;
    }

    public double getDifference() {
        return difference;
    }

    public void setDifference(double difference) {
        this.difference = difference;
    }

    public int getClassRank() {
        return classRank;
    }

    public void setClassRank(int classRank) {
        this.classRank = classRank;
    }

    public boolean isRecorded() {
        return score != null;
    }

    public boolean isPassed() {
        return score != null && score >= 60;
    }

    public String getLevel() {
        if (score == null) return "未录入";
        if (score >= 90) return "优秀";
        if (score >= 80) return "良好";
        if (score >= 70) return "中等";
        if (score >= 60) return "及格";
        return "不及格";
    }

    public String getLevelTone() {
        if (score == null) return "muted";
        if (score >= 90) return "excellent";
        if (score >= 80) return "good";
        if (score >= 70) return "medium";
        if (score >= 60) return "pass";
        return "fail";
    }

    public String getDifferenceLabel() {
        if (!isRecorded()) return "—";
        if (Math.abs(difference) < 0.05) return "持平";
        return String.format("%s%.1f", difference > 0 ? "+" : "", difference);
    }
}
