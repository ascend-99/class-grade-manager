package com.zhihang.grade.service;

import com.zhihang.grade.dao.UserDao;
import com.zhihang.grade.model.SessionUser;
import com.zhihang.grade.model.UserAccount;
import com.zhihang.grade.util.PasswordUtil;

public class AuthService {

    private final UserDao userDao = new UserDao();

    public SessionUser login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            return null;
        }
        UserAccount user = userDao.findByUsername(username.trim());
        if (user == null || !PasswordUtil.matches(password, user.getPasswordHash())) {
            return null;
        }
        userDao.touchLastLogin(user.getId());
        return user.toSessionUser();
    }

    public UserAccount findUser(long id) {
        return userDao.findById(id);
    }

    public ServiceResult<Void> changePassword(long userId, String oldPassword,
                                               String newPassword, String confirmPassword) {
        if (oldPassword == null || oldPassword.isEmpty()) {
            return ServiceResult.fail("请输入当前密码");
        }
        if (newPassword == null || newPassword.length() < 6) {
            return ServiceResult.fail("新密码至少需要 6 位字符");
        }
        if (newPassword.length() > 50) {
            return ServiceResult.fail("新密码不能超过 50 位字符");
        }
        if (!newPassword.equals(confirmPassword)) {
            return ServiceResult.fail("两次输入的新密码不一致");
        }
        if (newPassword.equals(oldPassword)) {
            return ServiceResult.fail("新密码不能与当前密码相同");
        }
        UserAccount user = userDao.findById(userId);
        if (user == null) {
            return ServiceResult.fail("账号不存在，请重新登录");
        }
        if (!PasswordUtil.matches(oldPassword, user.getPasswordHash())) {
            return ServiceResult.fail("当前密码不正确");
        }
        userDao.updatePassword(userId, PasswordUtil.hash(newPassword));
        return ServiceResult.ok("密码修改成功，请使用新密码登录");
    }
}
