package com.zhihang.grade.web;

import com.zhihang.grade.model.ClassInfo;
import com.zhihang.grade.model.PageResult;
import com.zhihang.grade.model.Student;
import com.zhihang.grade.service.ClassService;
import com.zhihang.grade.service.ServiceResult;
import com.zhihang.grade.service.StudentService;
import com.zhihang.grade.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class StudentServlet extends BaseServlet {

    private final StudentService studentService = new StudentService();
    private final ClassService classService = new ClassService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long classId = Web.longParam(request, "classId", 0);
        String keyword = Web.param(request, "q");
        int page = Web.intParam(request, "page", 1);
        PageResult<Student> result = studentService.page(classId > 0 ? classId : null, keyword, page, 8);
        List<ClassInfo> classes = classService.findAll();
        request.setAttribute("data", result);
        request.setAttribute("classes", classes);
        request.setAttribute("classId", classId);
        request.setAttribute("keyword", keyword);
        request.setAttribute("pageTitle", "学生管理");
        request.setAttribute("pageEyebrow", "STUDENTS");
        request.setAttribute("activeNav", "students");
        forward(request, response, "students.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = Web.param(request, "action");
        String returnTo = safeReturnTo(request, "/students");
        if ("save".equals(action)) {
            Student student = new Student();
            student.setId(Web.longParam(request, "id", 0));
            student.setStudentNo(Web.param(request, "studentNo"));
            student.setName(Web.param(request, "name"));
            student.setGender(Web.param(request, "gender"));
            student.setClassId(Web.longParam(request, "classId", 0));
            student.setPhone(Web.param(request, "phone"));
            student.setEmail(Web.param(request, "email"));
            String status = Web.param(request, "status", "在读");
            student.setStatus(status);
            ServiceResult<Long> result = studentService.save(student);
            if (result.isSuccess()) {
                Web.flashSuccess(request, result.getMessage());
            } else {
                Web.flashError(request, result.getMessage());
            }
        } else if ("delete".equals(action)) {
            ServiceResult<Void> result = studentService.delete(Web.longParam(request, "id", 0));
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
