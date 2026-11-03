package com.zhihang.grade.web;

import com.zhihang.grade.model.PortalSummary;
import com.zhihang.grade.model.SessionUser;
import com.zhihang.grade.service.PortalService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class PortalGradesServlet extends BaseServlet {

    private final PortalService portalService = new PortalService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SessionUser user = currentUser(request);
        PortalSummary summary = user.getStudentId() == null
                ? null : portalService.load(user.getStudentId());
        if (summary == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "未找到学生档案");
            return;
        }
        request.setAttribute("data", summary);
        request.setAttribute("pageTitle", "我的成绩");
        request.setAttribute("pageEyebrow", "TRANSCRIPT");
        request.setAttribute("activeNav", "portal-grades");
        forward(request, response, "portal-grades.jsp");
    }
}
