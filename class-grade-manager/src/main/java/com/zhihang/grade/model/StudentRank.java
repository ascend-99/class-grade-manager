package com.zhihang.grade.model;

public class StudentRank {

    private int rank;
    private long studentId;
    private String studentNo;
    private String name;
    private String className;
    private double average;
    private int recordCount;
    private int passCount;
    private double passRate;

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    public int getPassCount() {
        return passCount;
    }

    public void setPassCount(int passCount) {
        this.passCount = passCount;
    }

    public double getPassRate() {
        return passRate;
    }

    public void setPassRate(double passRate) {
        this.passRate = passRate;
    }

    public String getInitial() {
        return name == null || name.isBlank() ? "?" : name.substring(0, 1);
    }

    public String getRankTone() {
        if (rank == 1) return "gold";
        if (rank == 2) return "silver";
        if (rank == 3) return "bronze";
        return "plain";
    }
}
