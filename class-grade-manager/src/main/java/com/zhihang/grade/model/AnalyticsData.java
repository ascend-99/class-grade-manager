package com.zhihang.grade.model;

import java.util.List;

public class AnalyticsData {

    private List<ClassInfo> classes;
    private List<Course> courses;
    private List<String> exams;
    private long classId;
    private long courseId;
    private String examName;
    private String className;
    private String courseName;
    private GradeStats stats;
    private List<DistributionPoint> distribution;
    private List<CourseStat> courseStats;
    private List<StudentRank> ranking;
    private List<AttentionRow> attention;

    public List<ClassInfo> getClasses() {
        return classes;
    }

    public void setClasses(List<ClassInfo> classes) {
        this.classes = classes;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public List<String> getExams() {
        return exams;
    }

    public void setExams(List<String> exams) {
        this.exams = exams;
    }

    public long getClassId() {
        return classId;
    }

    public void setClassId(long classId) {
        this.classId = classId;
    }

    public long getCourseId() {
        return courseId;
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public GradeStats getStats() {
        return stats;
    }

    public void setStats(GradeStats stats) {
        this.stats = stats;
    }

    public List<DistributionPoint> getDistribution() {
        return distribution;
    }

    public void setDistribution(List<DistributionPoint> distribution) {
        this.distribution = distribution;
    }

    public List<CourseStat> getCourseStats() {
        return courseStats;
    }

    public void setCourseStats(List<CourseStat> courseStats) {
        this.courseStats = courseStats;
    }

    public List<StudentRank> getRanking() {
        return ranking;
    }

    public void setRanking(List<StudentRank> ranking) {
        this.ranking = ranking;
    }

    public List<AttentionRow> getAttention() {
        return attention;
    }

    public void setAttention(List<AttentionRow> attention) {
        this.attention = attention;
    }

    public boolean isAllCourses() {
        return courseId <= 0;
    }

    public boolean isAllExams() {
        return examName == null || examName.isBlank();
    }
}
