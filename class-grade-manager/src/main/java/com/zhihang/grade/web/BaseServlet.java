package com.zhihang.grade.web;

import com.zhihang.grade.model.SessionUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public abstract class BaseServlet extends HttpServlet {

    private static final String[] WEEKDAYS = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};

    protected SessionUser currentUser(HttpServletRequest request) {
        Object value = request.getSession(false) == null
                ? null : request.getSession(false).getAttribute("currentUser");
        return value instanceof SessionUser ? (SessionUser) value : null;
    }

    protected void prepare(HttpServletRequest request) {
        LocalDate today = LocalDate.now();
        request.setAttribute("todayLabel",
                today.getMonthValue() + "月" + today.getDayOfMonth() + "日 · "
                        + WEEKDAYS[today.getDayOfWeek().getValue() - 1]);
        request.setAttribute("todayIso", today.format(DateTimeFormatter.ISO_DATE));
        request.setAttribute("semesterLabel", "2026–2027 学年 · 第一学期");
        request.setAttribute("greeting", greeting());
        request.setAttribute("ctx", request.getContextPath());
    }

    protected void forward(HttpServletRequest request, HttpServletResponse response, String view)
            throws ServletException, IOException {
        prepare(request);
        request.getRequestDispatcher("/WEB-INF/views/" + view).forward(request, response);
    }

    protected void redirect(HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    protected String safeReturnTo(HttpServletRequest request, String fallback) {
        String value = request.getParameter("returnTo");
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String context = request.getContextPath();
        if (context != null && !context.isEmpty() && value.startsWith(context + "/")) {
            value = value.substring(context.length());
        }
        if (!value.startsWith("/") || value.startsWith("//") || value.contains("\\")
                || value.contains("\r") || value.contains("\n")) {
            return fallback;
        }
        return value;
    }

    private String greeting() {
        int hour = LocalTime.now().getHour();
        if (hour < 6) return "夜深了";
        if (hour < 9) return "早上好";
        if (hour < 12) return "上午好";
        if (hour < 14) return "中午好";
        if (hour < 18) return "下午好";
        return "晚上好";
    }
}
