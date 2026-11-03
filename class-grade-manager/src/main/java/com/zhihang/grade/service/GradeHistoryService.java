package com.zhihang.grade.service;

import com.zhihang.grade.dao.GradeLogDao;
import com.zhihang.grade.model.GradeLog;
import com.zhihang.grade.model.PageResult;

public class GradeHistoryService {

    private final GradeLogDao gradeLogDao = new GradeLogDao();

    public PageResult<GradeLog> page(Long classId, Long courseId, Long studentId,
                                     String action, String keyword, int page, int size) {
        return gradeLogDao.findPage(classId, courseId, studentId, action, keyword, page, size);
    }

    public long countAll() {
        return gradeLogDao.countAll();
    }
}
