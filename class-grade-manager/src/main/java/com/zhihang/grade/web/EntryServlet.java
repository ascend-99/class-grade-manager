package com.zhihang.grade.web;

import com.zhihang.grade.model.ClassInfo;
import com.zhihang.grade.model.Course;
import com.zhihang.grade.model.GradeEntryRow;
import com.zhihang.grade.service.ClassService;
import com.zhihang.grade.service.CourseService;
import com.zhihang.grade.service.GradeService;
import com.zhihang.grade.service.ServiceResult;
import com.zhihang.grade.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntryServlet extends BaseServlet {

    private final GradeService gradeService = new GradeService();
    private final ClassService classService = new ClassService();
    private final CourseService courseService = new CourseService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<ClassInfo> classes = classService.findAll();
        List<Course> courses = courseService.all();
        List<String> exams = gradeService.examNames();

        long classId = Web.longParam(request, "classId", 0);
        long courseId = Web.longParam(request, "courseId", 0);
        String examName = Web.param(request, "examName");

        ClassInfo selectedClass = classId > 0 ? classService.findById(classId) : null;
        if (selectedClass == null && !classes.isEmpty()) {
            selectedClass = classes.get(0);
        }
        Course selectedCourse = courseId > 0 ? courseService.findById(courseId) : null;
        if (selectedCourse == null && !courses.isEmpty()) {
            selectedCourse = courses.get(0);
        }
        if (examName.isEmpty() || !exams.contains(examName)) {
            examName = exams.contains("期中考试") ? "期中考试" : (exams.isEmpty() ? "期中考试" : exams.get(0));
        }

        List<GradeEntryRow> rows = List.of();
        int recorded = 0;
        double average = 0;
        if (selectedClass != null && selectedCourse != null) {
            rows = gradeService.entryRows(selectedClass.getId(), selectedCourse.getId(), examName);
            double sum = 0;
            for (GradeEntryRow row : rows) {
                if (row.getScore() != null) {
                    recorded++;
                    sum += row.getScore();
                }
            }
            average = recorded == 0 ? 0 : Math.round(sum / recorded * 10.0) / 10.0;
        }

        request.setAttribute("classes", classes);
        request.setAttribute("courses", courses);
        request.setAttribute("exams", exams);
        request.setAttribute("selectedClass", selectedClass);
        request.setAttribute("selectedCourse", selectedCourse);
        request.setAttribute("examName", examName);
        request.setAttribute("rows", rows);
        request.setAttribute("recorded", recorded);
        request.setAttribute("recordedAverage", average);
        request.setAttribute("pageTitle", "成绩录入");
        request.setAttribute("pageEyebrow", "SCORE ENTRY");
        request.setAttribute("activeNav", "entry");
        forward(request, response, "entry.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long classId = Web.longParam(request, "classId", 0);
        long courseId = Web.longParam(request, "courseId", 0);
        String examName = Web.param(request, "examName");
        Map<Long, String> scores = new HashMap<>();
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (name.startsWith("score_")) {
                try {
                    long studentId = Long.parseLong(name.substring("score_".length()));
                    scores.put(studentId, request.getParameter(name));
                } catch (NumberFormatException ignored) {
                    // 非法字段直接忽略
                }
            }
        }
        ServiceResult<GradeService.BatchSummary> result =
                gradeService.saveBatch(classId, courseId, examName, scores,
                        currentUser(request).getDisplayName());
        if (result.isSuccess()) {
            Web.flashSuccess(request, result.getMessage());
        } else {
            Web.flashError(request, result.getMessage());
        }
        String target = "/entry?classId=" + classId + "&courseId=" + courseId + "&examName="
                + java.net.URLEncoder.encode(examName, java.nio.charset.StandardCharsets.UTF_8);
        redirect(request, response, safeReturnTo(request, target));
    }
}
