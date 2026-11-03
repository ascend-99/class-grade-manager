package com.zhihang.grade.model;

import java.util.List;

public class PortalSummary {

    private Student student;
    private ClassInfo classInfo;
    private List<PortalCourse> courses;
    private List<TrendPoint> trend;
    private List<GradeRow> allGrades;
    private double average;
    private double passRate;
    private double gpa;
    private double totalCredits;
    private double earnedCredits;
    private int rank;
    private int classSize;
    private double classAverage;
    private int gradedCourses;
    private int excellentCount;
    private int failCount;
    private int totalCourses;

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public ClassInfo getClassInfo() {
        return classInfo;
    }

    public void setClassInfo(ClassInfo classInfo) {
        this.classInfo = classInfo;
    }

    public List<PortalCourse> getCourses() {
        return courses;
    }

    public void setCourses(List<PortalCourse> courses) {
        this.courses = courses;
    }

    public List<TrendPoint> getTrend() {
        return trend;
    }

    public void setTrend(List<TrendPoint> trend) {
        this.trend = trend;
    }

    public List<GradeRow> getAllGrades() {
        return allGrades;
    }

    public void setAllGrades(List<GradeRow> allGrades) {
        this.allGrades = allGrades;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public double getPassRate() {
        return passRate;
    }

    public void setPassRate(double passRate) {
        this.passRate = passRate;
    }

    public double getGpa() {
        return gpa;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    public double getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(double totalCredits) {
        this.totalCredits = totalCredits;
    }

    public double getEarnedCredits() {
        return earnedCredits;
    }

    public void setEarnedCredits(double earnedCredits) {
        this.earnedCredits = earnedCredits;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getClassSize() {
        return classSize;
    }

    public void setClassSize(int classSize) {
        this.classSize = classSize;
    }

    public double getClassAverage() {
        return classAverage;
    }

    public void setClassAverage(double classAverage) {
        this.classAverage = classAverage;
    }

    public int getGradedCourses() {
        return gradedCourses;
    }

    public void setGradedCourses(int gradedCourses) {
        this.gradedCourses = gradedCourses;
    }

    public int getExcellentCount() {
        return excellentCount;
    }

    public void setExcellentCount(int excellentCount) {
        this.excellentCount = excellentCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public int getTotalCourses() {
        return totalCourses;
    }

    public void setTotalCourses(int totalCourses) {
        this.totalCourses = totalCourses;
    }

    public int getProgressPercent() {
        if (totalCourses <= 0) {
            return 0;
        }
        return (int) Math.round(gradedCourses * 100.0 / totalCourses);
    }

    public String getRankLabel() {
        if (rank <= 0) {
            return "暂无排名";
        }
        if (rank <= 3) {
            return "班级 TOP " + rank;
        }
        return "班级第 " + rank + " 名";
    }
}
