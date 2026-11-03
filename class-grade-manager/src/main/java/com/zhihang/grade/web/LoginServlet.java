package com.zhihang.grade.web;

import com.zhihang.grade.model.SessionUser;
import com.zhihang.grade.service.AuthService;
import com.zhihang.grade.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class LoginServlet extends BaseServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SessionUser current = currentUser(request);
        if (current != null) {
            redirect(request, response, current.getHomePath());
            return;
        }
        if ("required".equals(request.getParameter("auth"))) {
            request.setAttribute("notice", "请先登录后再继续访问");
        }
        request.setAttribute("pageTitle", "登录");
        forward(request, response, "login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String username = Web.param(request, "username");
        String password = request.getParameter("password");

        SessionUser user = authService.login(username, password);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            request.setAttribute("error", "账号或密码不正确，请检查后重试");
            request.setAttribute("username", username);
            request.setAttribute("pageTitle", "登录");
            forward(request, response, "login.jsp");
            return;
        }

        HttpSession oldSession = request.getSession(false);
        String redirectTarget = null;
        if (oldSession != null) {
            Object saved = oldSession.getAttribute("loginRedirect");
            if (saved != null) {
                redirectTarget = String.valueOf(saved);
            }
            oldSession.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(60 * 60);
        session.setAttribute("currentUser", user);
        session.setAttribute("flashType", "success");
        session.setAttribute("flashMessage", "欢迎回来，" + user.getDisplayName()
                + (user.isTeacher() ? "老师" : "同学") + "。今天也一起把进步记录下来。");

        String defaultHome = user.getHomePath();
        String target = Web.safeRedirect(redirectTarget, defaultHome);
        if (user.isStudent() && !target.startsWith("/portal")) {
            target = "/portal";
        }
        if (user.isTeacher() && target.startsWith("/portal")) {
            target = "/dashboard";
        }
        redirect(request, response, target);
    }
}
