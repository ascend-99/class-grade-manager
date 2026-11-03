package com.zhihang.grade.service;

import com.zhihang.grade.dao.ClassDao;
import com.zhihang.grade.dao.GradeDao;
import com.zhihang.grade.dao.StatsDao;
import com.zhihang.grade.model.ClassInfo;
import com.zhihang.grade.model.DashboardData;
import com.zhihang.grade.model.GradeStats;
import com.zhihang.grade.model.StudentRank;

import java.util.List;

public class DashboardService {

    private final ClassDao classDao = new ClassDao();
    private final StatsDao statsDao = new StatsDao();
    private final GradeDao gradeDao = new GradeDao();

    public DashboardData load(Long classId) {
        List<ClassInfo> classes = classDao.findAll();
        ClassInfo selected = null;
        if (classId != null && classId > 0) {
            selected = classDao.findById(classId);
        }
        if (selected == null && !classes.isEmpty()) {
            selected = classes.get(0);
        }
        DashboardData data = new DashboardData();
        data.setClasses(classes);
        data.setSelectedClass(selected);
        if (selected == null) {
            return data;
        }
        long id = selected.getId();
        GradeStats stats = statsDao.stats(id, null, null);
        data.setStudentCount(selected.getStudentCount());
        data.setGradeCount(stats.getRecordCount());
        data.setAverageScore(stats.getAverage());
        data.setPassRate(stats.getPassRate());
        data.setExcellentRate(stats.getExcellentRate());
        data.setFailCount(stats.getFailCount());
        data.setDistribution(statsDao.distribution(id, null, null));
        data.setCourseStats(statsDao.courseStats(id, null));
        data.setCourseCount(data.getCourseStats().size());
        data.setRecentGrades(gradeDao.findRecent(id, 6));
        List<StudentRank> top = statsDao.ranking(id, null, null, 5);
        data.setTopStudents(top);
        return data;
    }
}
