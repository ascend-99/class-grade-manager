package com.zhihang.grade.model;

public class DistributionPoint {

    private String label;
    private int count;
    private double percent;
    private String tone;

    public DistributionPoint() {
    }

    public DistributionPoint(String label, int count, double percent, String tone) {
        this.label = label;
        this.count = count;
        this.percent = percent;
        this.tone = tone;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }
}
