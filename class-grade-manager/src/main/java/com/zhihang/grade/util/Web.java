package com.zhihang.grade.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class Web {

    private Web() {
    }

    public static String param(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    public static String param(HttpServletRequest request, String name, String fallback) {
        String value = param(request, name);
        return value.isEmpty() ? fallback : value;
    }

    public static long longParam(HttpServletRequest request, String name, long fallback) {
        try {
            String value = param(request, name);
            return value.isEmpty() ? fallback : Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    public static int intParam(HttpServletRequest request, String name, int fallback) {
        try {
            String value = param(request, name);
            return value.isEmpty() ? fallback : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    public static Double optionalDouble(HttpServletRequest request, String name) {
        String raw = param(request, name);
        if (raw.isEmpty()) {
            return null;
        }
        try {
            return Double.valueOf(raw);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static void flash(HttpServletRequest request, String type, String message) {
        HttpSession session = request.getSession();
        session.setAttribute("flashType", type);
        session.setAttribute("flashMessage", message);
    }

    public static void flashSuccess(HttpServletRequest request, String message) {
        flash(request, "success", message);
    }

    public static void flashError(HttpServletRequest request, String message) {
        flash(request, "error", message);
    }

    public static void flashInfo(HttpServletRequest request, String message) {
        flash(request, "info", message);
    }

    /**
     * 只允许站内相对路径，避免登录后开放重定向漏洞。
     */
    public static String safeRedirect(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String candidate = value.trim();
        if (!candidate.startsWith("/") || candidate.startsWith("//") || candidate.contains("\\")
                || candidate.contains("\r") || candidate.contains("\n")) {
            return fallback;
        }
        return candidate;
    }

    public static String baseUrl(HttpServletRequest request) {
        String context = request.getContextPath();
        return context == null || context.isEmpty() ? "" : context;
    }
}
