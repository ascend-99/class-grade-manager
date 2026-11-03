package com.zhihang.grade.web.filter;

import com.zhihang.grade.model.SessionUser;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

public class AuthFilter implements Filter {

    private static final List<String> PUBLIC_PREFIXES = List.of(
            "/login", "/logout", "/assets/", "/error/", "/favicon.ico", "/health"
    );

    private static final List<String> TEACHER_PREFIXES = List.of(
            "/dashboard", "/students", "/courses", "/grades", "/entry", "/analytics", "/export",
            "/classes", "/grade-history"
    );

    @Override
    public void init(FilterConfig filterConfig) {
        // no-op
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String context = req.getContextPath();
        String path = req.getRequestURI().substring(context.length());
        int semicolon = path.indexOf(';');
        if (semicolon >= 0) {
            path = path.substring(0, semicolon);
        }

        boolean isPublic = path.equals("/") || path.isEmpty();
        if (!isPublic) {
            for (String prefix : PUBLIC_PREFIXES) {
                if (path.startsWith(prefix)) {
                    isPublic = true;
                    break;
                }
            }
        }
        if (isPublic) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        SessionUser user = session == null ? null : (SessionUser) session.getAttribute("currentUser");
        if (user == null) {
            if ("GET".equalsIgnoreCase(req.getMethod())) {
                String target = path;
                if (req.getQueryString() != null && !req.getQueryString().isBlank()) {
                    target += "?" + req.getQueryString();
                }
                req.getSession(true).setAttribute("loginRedirect", target);
            }
            res.sendRedirect(context + "/login?auth=required");
            return;
        }

        boolean teacherOnly = TEACHER_PREFIXES.stream().anyMatch(path::startsWith);
        if (teacherOnly && !user.isTeacher()) {
            req.getSession().setAttribute("flashType", "info");
            req.getSession().setAttribute("flashMessage", "该页面仅教师账号可访问");
            res.sendRedirect(context + "/portal");
            return;
        }
        if (path.startsWith("/portal") && !user.isStudent()) {
            req.getSession().setAttribute("flashType", "info");
            req.getSession().setAttribute("flashMessage", "教师账号请从工作台查看班级数据");
            res.sendRedirect(context + "/dashboard");
            return;
        }

        res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        res.setHeader("Pragma", "no-cache");
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // no-op
    }
}
