package com.zhihang.grade.service;

import com.zhihang.grade.config.Database;
import com.zhihang.grade.dao.ClassDao;
import com.zhihang.grade.dao.DaoException;
import com.zhihang.grade.dao.StudentDao;
import com.zhihang.grade.dao.UserDao;
import com.zhihang.grade.model.PageResult;
import com.zhihang.grade.model.Student;
import com.zhihang.grade.util.PasswordUtil;
import com.zhihang.grade.util.Validation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class StudentService {

    public static final String DEFAULT_PASSWORD = "123456";

    private final StudentDao studentDao = new StudentDao();
    private final ClassDao classDao = new ClassDao();
    private final UserDao userDao = new UserDao();

    public PageResult<Student> page(Long classId, String keyword, int page, int size) {
        return studentDao.findPage(classId, keyword, page, size);
    }

    public List<Student> listByClass(long classId) {
        return studentDao.findByClass(classId);
    }

    public List<Student> all() {
        return studentDao.findAllBasic();
    }

    public Student findById(long id) {
        return studentDao.findById(id);
    }

    public long countAll() {
        return studentDao.countAll();
    }

    public long countByClass(long classId) {
        return studentDao.countByClass(classId);
    }

    public ServiceResult<Long> save(Student form) {
        String error = validate(form);
        if (error != null) {
            return ServiceResult.fail(error);
        }
        if (classDao.findById(form.getClassId()) == null) {
            return ServiceResult.fail("所选班级不存在");
        }
        boolean creating = form.getId() <= 0;
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (studentDao.studentNoExists(connection, form.getStudentNo(), creating ? null : form.getId())) {
                    connection.rollback();
                    return ServiceResult.fail("学号 " + form.getStudentNo() + " 已存在，请检查后重试");
                }
                if (userDao.usernameExists(connection, form.getStudentNo(), creating ? null : form.getId())) {
                    connection.rollback();
                    return ServiceResult.fail("学号与现有登录账号冲突，请更换学号");
                }
                if (creating) {
                    long id = studentDao.insert(connection, form);
                    userDao.createStudentUser(connection, form.getStudentNo(),
                            PasswordUtil.hash(DEFAULT_PASSWORD), form.getName(), id, colorFor(form.getStudentNo()));
                    connection.commit();
                    return ServiceResult.ok("学生已添加，登录账号为学号，初始密码 " + DEFAULT_PASSWORD, id);
                }
                studentDao.update(connection, form);
                if (userDao.existsStudentAccount(connection, form.getId())) {
                    userDao.updateStudentAccount(connection, form.getId(), form.getStudentNo(),
                            form.getName(), colorFor(form.getStudentNo()));
                } else {
                    userDao.createStudentUser(connection, form.getStudentNo(),
                            PasswordUtil.hash(DEFAULT_PASSWORD), form.getName(), form.getId(),
                            colorFor(form.getStudentNo()));
                }
                connection.commit();
                return ServiceResult.ok("学生信息已更新", form.getId());
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new DaoException("保存学生失败", ex);
        }
    }

    public ServiceResult<Void> delete(long id) {
        Student student = studentDao.findById(id);
        if (student == null) {
            return ServiceResult.fail("学生不存在或已被删除");
        }
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                studentDao.delete(connection, id);
                connection.commit();
                return ServiceResult.ok("已删除学生「" + student.getName() + "」及其成绩记录");
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new DaoException("删除学生失败", ex);
        }
    }

    private String validate(Student form) {
        if (Validation.blank(form.getName())) {
            return "请填写学生姓名";
        }
        if (form.getName().length() > 50) {
            return "学生姓名不能超过 50 个字符";
        }
        if (!Validation.validStudentNo(form.getStudentNo())) {
            return "学号需为 4-20 位数字或字母";
        }
        if (!"男".equals(form.getGender()) && !"女".equals(form.getGender())) {
            return "请选择正确的性别";
        }
        if (!Validation.validPhone(form.getPhone())) {
            return "手机号格式不正确";
        }
        if (!Validation.validEmail(form.getEmail())) {
            return "邮箱格式不正确";
        }
        if (!List.of("在读", "休学", "毕业").contains(form.getStatus())) {
            return "请选择正确的学籍状态";
        }
        return null;
    }

    private String colorFor(String studentNo) {
        String[] colors = {"indigo", "teal", "amber", "rose", "sky"};
        int hash = Math.abs(studentNo == null ? 0 : studentNo.hashCode());
        return colors[hash % colors.length];
    }
}
