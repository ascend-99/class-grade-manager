package com.zhihang.grade.web;

import com.zhihang.grade.model.ClassInfo;
import com.zhihang.grade.service.ClassService;
import com.zhihang.grade.service.ServiceResult;
import com.zhihang.grade.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class ClassServlet extends BaseServlet {

    private final ClassService classService = new ClassService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<ClassInfo> classes = classService.findAllWithStats();
        request.setAttribute("classes", classes);
        request.setAttribute("pageTitle", "班级管理");
        request.setAttribute("pageEyebrow", "CLASSES");
        request.setAttribute("activeNav", "classes");
        forward(request, response, "classes.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = Web.param(request, "action");
        if ("save".equals(action)) {
            ClassInfo form = new ClassInfo();
            form.setId(Web.longParam(request, "id", 0));
            form.setName(Web.param(request, "name"));
            form.setGradeYear(Web.param(request, "gradeYear"));
            form.setMajor(Web.param(request, "major"));
            form.setAdvisor(Web.param(request, "advisor"));
            ServiceResult<Long> result = classService.save(form);
            if (result.isSuccess()) {
                Web.flashSuccess(request, result.getMessage());
            } else {
                Web.flashError(request, result.getMessage());
            }
        } else if ("delete".equals(action)) {
            ServiceResult<Void> result = classService.delete(Web.longParam(request, "id", 0));
            if (result.isSuccess()) {
                Web.flashSuccess(request, result.getMessage());
            } else {
                Web.flashError(request, result.getMessage());
            }
        } else {
            Web.flashError(request, "无法识别的操作");
        }
        redirect(request, response, "/classes");
    }
}
