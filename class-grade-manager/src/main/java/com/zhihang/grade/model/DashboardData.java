package com.zhihang.grade.model;

import java.util.List;

public class DashboardData {

    private List<ClassInfo> classes;
    private ClassInfo selectedClass;
    private int studentCount;
    private int courseCount;
    private int gradeCount;
    private double averageScore;
    private double passRate;
    private double excellentRate;
    private int failCount;
    private List<DistributionPoint> distribution;
    private List<CourseStat> courseStats;
    private List<GradeRow> recentGrades;
    private List<StudentRank> topStudents;

    public List<ClassInfo> getClasses() {
        return classes;
    }

    public void setClasses(List<ClassInfo> classes) {
        this.classes = classes;
    }

    public ClassInfo getSelectedClass() {
        return selectedClass;
    }

    public void setSelectedClass(ClassInfo selectedClass) {
        this.selectedClass = selectedClass;
    }

    public int getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }

    public int getCourseCount() {
        return courseCount;
    }

    public void setCourseCount(int courseCount) {
        this.courseCount = courseCount;
    }

    public int getGradeCount() {
        return gradeCount;
    }

    public void setGradeCount(int gradeCount) {
        this.gradeCount = gradeCount;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public double getPassRate() {
        return passRate;
    }

    public void setPassRate(double passRate) {
        this.passRate = passRate;
    }

    public double getExcellentRate() {
        return excellentRate;
    }

    public void setExcellentRate(double excellentRate) {
        this.excellentRate = excellentRate;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
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

    public List<GradeRow> getRecentGrades() {
        return recentGrades;
    }

    public void setRecentGrades(List<GradeRow> recentGrades) {
        this.recentGrades = recentGrades;
    }

    public List<StudentRank> getTopStudents() {
        return topStudents;
    }

    public void setTopStudents(List<StudentRank> topStudents) {
        this.topStudents = topStudents;
    }
}
