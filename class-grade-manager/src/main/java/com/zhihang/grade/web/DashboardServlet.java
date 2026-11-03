package com.zhihang.grade.web;

import com.zhihang.grade.model.DashboardData;
import com.zhihang.grade.service.DashboardService;
import com.zhihang.grade.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class DashboardServlet extends BaseServlet {

    private final DashboardService dashboardService = new DashboardService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long classId = Web.longParam(request, "classId", 0);
        if (classId <= 0) {
            classId = null;
        }
        DashboardData data = dashboardService.load(classId);
        request.setAttribute("data", data);
        request.setAttribute("pageTitle", "工作台");
        request.setAttribute("pageEyebrow", "TEACHER WORKSPACE");
        request.setAttribute("activeNav", "dashboard");
        forward(request, response, "dashboard.jsp");
    }
}
