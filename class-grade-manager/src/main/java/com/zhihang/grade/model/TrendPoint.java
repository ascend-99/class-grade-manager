package com.zhihang.grade.model;

public class TrendPoint {

    private String label;
    private double value;
    private String dateLabel;

    public TrendPoint() {
    }

    public TrendPoint(String label, double value, String dateLabel) {
        this.label = label;
        this.value = value;
        this.dateLabel = dateLabel;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getDateLabel() {
        return dateLabel;
    }

    public void setDateLabel(String dateLabel) {
        this.dateLabel = dateLabel;
    }
}
