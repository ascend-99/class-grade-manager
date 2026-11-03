package com.zhihang.grade.web;

import com.zhihang.grade.model.SessionUser;
import com.zhihang.grade.model.Student;
import com.zhihang.grade.model.UserAccount;
import com.zhihang.grade.service.AuthService;
import com.zhihang.grade.service.ServiceResult;
import com.zhihang.grade.service.StudentService;
import com.zhihang.grade.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ProfileServlet extends BaseServlet {

    private final AuthService authService = new AuthService();
    private final StudentService studentService = new StudentService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SessionUser sessionUser = currentUser(request);
        UserAccount account = authService.findUser(sessionUser.getId());
        Student student = sessionUser.getStudentId() == null
                ? null : studentService.findById(sessionUser.getStudentId());
        request.setAttribute("account", account);
        request.setAttribute("student", student);
        request.setAttribute("pageTitle", "个人中心");
        request.setAttribute("pageEyebrow", "ACCOUNT");
        request.setAttribute("activeNav", "profile");
        forward(request, response, "profile.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SessionUser user = currentUser(request);
        String action = Web.param(request, "action");
        if (!"password".equals(action)) {
            Web.flashError(request, "无法识别的操作");
            redirect(request, response, "/profile");
            return;
        }
        ServiceResult<Void> result = authService.changePassword(user.getId(),
                request.getParameter("oldPassword"),
                request.getParameter("newPassword"),
                request.getParameter("confirmPassword"));
        if (result.isSuccess()) {
            Web.flashSuccess(request, result.getMessage());
        } else {
            Web.flashError(request, result.getMessage());
        }
        redirect(request, response, "/profile");
    }
}
