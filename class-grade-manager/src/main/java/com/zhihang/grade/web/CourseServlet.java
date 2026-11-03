package com.zhihang.grade.web;

import com.zhihang.grade.model.ClassInfo;
import com.zhihang.grade.model.Course;
import com.zhihang.grade.service.ClassService;
import com.zhihang.grade.service.CourseService;
import com.zhihang.grade.service.ServiceResult;
import com.zhihang.grade.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class CourseServlet extends BaseServlet {

    private final CourseService courseService = new CourseService();
    private final ClassService classService = new ClassService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long classId = Web.longParam(request, "classId", 0);
        List<Course> courses = courseService.listWithStats(classId > 0 ? classId : null);
        List<ClassInfo> classes = classService.findAll();
        request.setAttribute("courses", courses);
        request.setAttribute("classes", classes);
        request.setAttribute("classId", classId);
        request.setAttribute("pageTitle", "课程管理");
        request.setAttribute("pageEyebrow", "CURRICULUM");
        request.setAttribute("activeNav", "courses");
        forward(request, response, "courses.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = Web.param(request, "action");
        String returnTo = safeReturnTo(request, "/courses");
        if ("save".equals(action)) {
            Course course = new Course();
            course.setId(Web.longParam(request, "id", 0));
            course.setCode(Web.param(request, "code"));
            course.setName(Web.param(request, "name"));
            try {
                course.setCredit(Double.parseDouble(Web.param(request, "credit", "3")));
            } catch (NumberFormatException ex) {
                Web.flashError(request, "学分必须是数字");
                redirect(request, response, returnTo);
                return;
            }
            course.setTeacher(Web.param(request, "teacher"));
            course.setSemester(Web.param(request, "semester", "2026-2027 秋"));
            course.setColor(Web.param(request, "color", "indigo"));
            ServiceResult<Long> result = courseService.save(course);
            if (result.isSuccess()) {
                Web.flashSuccess(request, result.getMessage());
            } else {
                Web.flashError(request, result.getMessage());
            }
        } else if ("delete".equals(action)) {
            ServiceResult<Void> result = courseService.delete(Web.longParam(request, "id", 0));
            if (result.isSuccess()) {
                Web.flashSuccess(request, result.getMessage());
            } else {
                Web.flashError(request, result.getMessage());
            }
        } else {
            Web.flashError(request, "无法识别的操作");
        }
        redirect(request, response, returnTo);
    }
}
