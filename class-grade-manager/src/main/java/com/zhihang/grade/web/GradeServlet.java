package com.zhihang.grade.web;

import com.zhihang.grade.model.ClassInfo;
import com.zhihang.grade.model.Course;
import com.zhihang.grade.model.GradeRow;
import com.zhihang.grade.model.PageResult;
import com.zhihang.grade.service.ClassService;
import com.zhihang.grade.service.CourseService;
import com.zhihang.grade.service.GradeService;
import com.zhihang.grade.service.ServiceResult;
import com.zhihang.grade.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class GradeServlet extends BaseServlet {

    private final GradeService gradeService = new GradeService();
    private final ClassService classService = new ClassService();
    private final CourseService courseService = new CourseService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long classId = Web.longParam(request, "classId", 0);
        long courseId = Web.longParam(request, "courseId", 0);
        String examName = Web.param(request, "examName");
        String keyword = Web.param(request, "q");
        int page = Web.intParam(request, "page", 1);

        PageResult<GradeRow> result = gradeService.page(
                classId > 0 ? classId : null,
                courseId > 0 ? courseId : null,
                examName, keyword, page, 10);
        List<ClassInfo> classes = classService.findAll();
        List<Course> courses = courseService.all();
        List<String> exams = gradeService.examNames();

        request.setAttribute("data", result);
        request.setAttribute("classes", classes);
        request.setAttribute("courses", courses);
        request.setAttribute("exams", exams);
        request.setAttribute("classId", classId);
        request.setAttribute("courseId", courseId);
        request.setAttribute("examName", examName);
        request.setAttribute("keyword", keyword);
        request.setAttribute("pageTitle", "成绩管理");
        request.setAttribute("pageEyebrow", "GRADE BOOK");
        request.setAttribute("activeNav", "grades");
        forward(request, response, "grades.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = Web.param(request, "action");
        String returnTo = safeReturnTo(request, "/grades");
        if ("update".equals(action)) {
            long id = Web.longParam(request, "id", 0);
            String score = Web.param(request, "score");
            String reason = Web.param(request, "reason", "手动修改");
            ServiceResult<Void> result = gradeService.updateScore(id, score,
                    currentUser(request).getDisplayName(), reason);
            if (result.isSuccess()) {
                Web.flashSuccess(request, result.getMessage());
            } else {
                Web.flashError(request, result.getMessage());
            }
        } else if ("delete".equals(action)) {
            ServiceResult<Void> result = gradeService.delete(Web.longParam(request, "id", 0),
                    currentUser(request).getDisplayName());
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
