package com.zhihang.grade.web;

import com.zhihang.grade.model.ClassInfo;
import com.zhihang.grade.model.Course;
import com.zhihang.grade.model.GradeLog;
import com.zhihang.grade.model.PageResult;
import com.zhihang.grade.service.ClassService;
import com.zhihang.grade.service.CourseService;
import com.zhihang.grade.service.GradeHistoryService;
import com.zhihang.grade.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class GradeHistoryServlet extends BaseServlet {

    private final GradeHistoryService historyService = new GradeHistoryService();
    private final ClassService classService = new ClassService();
    private final CourseService courseService = new CourseService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long classId = Web.longParam(request, "classId", 0);
        long courseId = Web.longParam(request, "courseId", 0);
        long studentId = Web.longParam(request, "studentId", 0);
        String action = Web.param(request, "action");
        String keyword = Web.param(request, "q");
        int page = Web.intParam(request, "page", 1);

        PageResult<GradeLog> data = historyService.page(
                classId > 0 ? classId : null,
                courseId > 0 ? courseId : null,
                studentId > 0 ? studentId : null,
                action, keyword, page, 12);
        List<ClassInfo> classes = classService.findAll();
        List<Course> courses = courseService.all();

        request.setAttribute("data", data);
        request.setAttribute("classes", classes);
        request.setAttribute("courses", courses);
        request.setAttribute("classId", classId);
        request.setAttribute("courseId", courseId);
        request.setAttribute("studentId", studentId);
        request.setAttribute("actionType", action);
        request.setAttribute("keyword", keyword);
        request.setAttribute("totalLogs", historyService.countAll());
        request.setAttribute("pageTitle", "修改记录");
        request.setAttribute("pageEyebrow", "AUDIT TRAIL");
        request.setAttribute("activeNav", "history");
        forward(request, response, "grade-history.jsp");
    }
}
