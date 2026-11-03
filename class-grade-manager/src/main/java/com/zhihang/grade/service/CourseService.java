package com.zhihang.grade.service;

import com.zhihang.grade.config.Database;
import com.zhihang.grade.dao.CourseDao;
import com.zhihang.grade.dao.DaoException;
import com.zhihang.grade.model.Course;
import com.zhihang.grade.util.Validation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class CourseService {

    private static final Set<String> COLORS = Set.of("indigo", "teal", "amber", "rose", "sky");

    private final CourseDao courseDao = new CourseDao();

    public List<Course> listWithStats(Long classId) {
        return courseDao.findAllWithStats(classId);
    }

    public List<Course> all() {
        return courseDao.findAllBasic();
    }

    public Course findById(long id) {
        return courseDao.findById(id);
    }

    public long count() {
        return courseDao.count();
    }

    public ServiceResult<Long> save(Course form) {
        String error = validate(form);
        if (error != null) {
            return ServiceResult.fail(error);
        }
        boolean creating = form.getId() <= 0;
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (courseDao.codeExists(connection, form.getCode(), creating ? null : form.getId())) {
                    connection.rollback();
                    return ServiceResult.fail("课程代码 " + form.getCode() + " 已存在");
                }
                if (creating) {
                    long id = courseDao.insert(connection, form);
                    connection.commit();
                    return ServiceResult.ok("课程已添加", id);
                }
                courseDao.update(connection, form);
                connection.commit();
                return ServiceResult.ok("课程信息已更新", form.getId());
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new DaoException("保存课程失败", ex);
        }
    }

    public ServiceResult<Void> delete(long id) {
        Course course = courseDao.findById(id);
        if (course == null) {
            return ServiceResult.fail("课程不存在或已被删除");
        }
        try (Connection connection = Database.getConnection()) {
            if (courseDao.hasGrades(connection, id)) {
                return ServiceResult.fail("课程「" + course.getName() + "」已有成绩记录，不能直接删除");
            }
            courseDao.delete(connection, id);
            return ServiceResult.ok("课程「" + course.getName() + "」已删除");
        } catch (SQLException ex) {
            throw new DaoException("删除课程失败", ex);
        }
    }

    private String validate(Course form) {
        if (Validation.blank(form.getName())) {
            return "请填写课程名称";
        }
        if (form.getName().length() > 100) {
            return "课程名称不能超过 100 个字符";
        }
        if (!Validation.validCourseCode(form.getCode())) {
            return "课程代码需为 2-20 位字母、数字、下划线或短横线";
        }
        if (!Validation.validCredit(form.getCredit())) {
            return "学分需在 0.1 到 20 之间";
        }
        if (Validation.blank(form.getTeacher())) {
            return "请填写授课教师";
        }
        if (Validation.blank(form.getSemester())) {
            return "请填写开课学期";
        }
        if (!COLORS.contains(form.getColor())) {
            form.setColor("indigo");
        }
        return null;
    }
}
