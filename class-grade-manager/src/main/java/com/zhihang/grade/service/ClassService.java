package com.zhihang.grade.service;

import com.zhihang.grade.config.Database;
import com.zhihang.grade.dao.ClassDao;
import com.zhihang.grade.dao.DaoException;
import com.zhihang.grade.model.ClassInfo;
import com.zhihang.grade.util.Validation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ClassService {

    private final ClassDao classDao = new ClassDao();

    public List<ClassInfo> findAll() {
        return classDao.findAll();
    }

    public List<ClassInfo> findAllWithStats() {
        return classDao.findAllWithStats();
    }

    public ClassInfo findById(long id) {
        return classDao.findById(id);
    }

    public long count() {
        return classDao.count();
    }

    public ServiceResult<Long> save(ClassInfo form) {
        String error = validate(form);
        if (error != null) {
            return ServiceResult.fail(error);
        }
        boolean creating = form.getId() <= 0;
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (classDao.nameExists(connection, form.getName(), creating ? null : form.getId())) {
                    connection.rollback();
                    return ServiceResult.fail("班级「" + form.getName() + "」已存在");
                }
                if (creating) {
                    long id = classDao.insert(connection, form);
                    connection.commit();
                    return ServiceResult.ok("班级已添加", id);
                }
                classDao.update(connection, form);
                connection.commit();
                return ServiceResult.ok("班级信息已更新", form.getId());
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new DaoException("保存班级失败", ex);
        }
    }

    public ServiceResult<Void> delete(long id) {
        ClassInfo existing = classDao.findById(id);
        if (existing == null) {
            return ServiceResult.fail("班级不存在或已被删除");
        }
        try (Connection connection = Database.getConnection()) {
            int students = classDao.countStudents(connection, id);
            if (students > 0) {
                return ServiceResult.fail("班级「" + existing.getName() + "」还有 " + students
                        + " 名学生，请先转出或删除学生");
            }
            classDao.delete(connection, id);
            return ServiceResult.ok("班级「" + existing.getName() + "」已删除");
        } catch (SQLException ex) {
            throw new DaoException("删除班级失败", ex);
        }
    }

    private String validate(ClassInfo form) {
        if (Validation.blank(form.getName())) {
            return "请填写班级名称";
        }
        if (form.getName().length() > 100) {
            return "班级名称不能超过 100 个字符";
        }
        if (Validation.blank(form.getGradeYear())) {
            return "请填写年级";
        }
        if (form.getGradeYear().length() > 20) {
            return "年级不能超过 20 个字符";
        }
        if (form.getMajor() != null && form.getMajor().length() > 100) {
            return "专业名称不能超过 100 个字符";
        }
        if (form.getAdvisor() != null && form.getAdvisor().length() > 50) {
            return "班主任姓名不能超过 50 个字符";
        }
        form.setName(form.getName().trim());
        form.setGradeYear(form.getGradeYear().trim());
        form.setMajor(form.getMajor() == null ? "" : form.getMajor().trim());
        form.setAdvisor(form.getAdvisor() == null ? "" : form.getAdvisor().trim());
        return null;
    }
}
