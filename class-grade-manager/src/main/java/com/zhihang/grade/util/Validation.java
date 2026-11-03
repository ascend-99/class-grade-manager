package com.zhihang.grade.util;

import java.util.regex.Pattern;

public final class Validation {

    private static final Pattern STUDENT_NO = Pattern.compile("^[0-9A-Za-z]{4,20}$");
    private static final Pattern PHONE = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern COURSE_CODE = Pattern.compile("^[A-Za-z0-9_-]{2,20}$");

    private Validation() {
    }

    public static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public static boolean validStudentNo(String value) {
        return value != null && STUDENT_NO.matcher(value).matches();
    }

    public static boolean validPhone(String value) {
        return blank(value) || PHONE.matcher(value).matches();
    }

    public static boolean validEmail(String value) {
        return blank(value) || EMAIL.matcher(value).matches();
    }

    public static boolean validCourseCode(String value) {
        return value != null && COURSE_CODE.matcher(value).matches();
    }

    public static boolean validScore(double value) {
        return value >= 0 && value <= 100;
    }

    public static boolean validCredit(double value) {
        return value > 0 && value <= 20;
    }

    public static String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }
}
