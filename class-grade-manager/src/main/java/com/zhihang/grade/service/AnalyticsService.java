package com.zhihang.grade.service;

import com.zhihang.grade.dao.ClassDao;
import com.zhihang.grade.dao.CourseDao;
import com.zhihang.grade.dao.StatsDao;
import com.zhihang.grade.model.AnalyticsData;
import com.zhihang.grade.model.ClassInfo;
import com.zhihang.grade.model.Course;
import com.zhihang.grade.model.CourseStat;
import com.zhihang.grade.model.GradeStats;
import com.zhihang.grade.model.StudentRank;

import java.util.List;

public class AnalyticsService {

    private final ClassDao classDao = new ClassDao();
    private final CourseDao courseDao = new CourseDao();
    private final GradeService gradeService = new GradeService();
    private final StatsDao statsDao = new StatsDao();

    public AnalyticsData load(Long classId, Long courseId, String examName) {
        List<ClassInfo> classes = classDao.findAll();
        ClassInfo selected = null;
        if (classId != null && classId > 0) {
            selected = classDao.findById(classId);
        }
        if (selected == null && !classes.isEmpty()) {
            selected = classes.get(0);
        }
        List<Course> courses = courseDao.findAllBasic();
        List<String> exams = gradeService.examNames();
        long safeCourseId = 0;
        if (courseId != null && courseId > 0) {
            for (Course course : courses) {
                if (course.getId() == courseId) {
                    safeCourseId = courseId;
                    break;
                }
            }
        }
        String safeExam = examName == null ? "" : examName.trim();
        if (!safeExam.isEmpty() && !exams.contains(safeExam)) {
            safeExam = "";
        }

        AnalyticsData data = new AnalyticsData();
        data.setClasses(classes);
        data.setCourses(courses);
        data.setExams(exams);
        data.setClassId(selected == null ? 0 : selected.getId());
        data.setClassName(selected == null ? "全部班级" : selected.getName());
        data.setCourseId(safeCourseId);
        data.setExamName(safeExam);
        data.setCourseName(courseName(courses, safeCourseId));
        if (selected == null) {
            data.setStats(new GradeStats());
            data.setDistribution(List.of());
            data.setCourseStats(List.of());
            data.setRanking(List.of());
            data.setAttention(List.of());
            return data;
        }
        Long courseFilter = safeCourseId > 0 ? safeCourseId : null;
        String examFilter = safeExam.isEmpty() ? null : safeExam;
        GradeStats stats = statsDao.stats(selected.getId(), courseFilter, examFilter);
        data.setStats(stats);
        data.setDistribution(statsDao.distribution(selected.getId(), courseFilter, examFilter));
        List<CourseStat> courseStats = statsDao.courseStats(selected.getId(), examFilter);
        data.setCourseStats(courseStats);
        List<StudentRank> ranking = statsDao.ranking(selected.getId(), courseFilter, examFilter, 10);
        data.setRanking(ranking);
        data.setAttention(statsDao.attention(selected.getId(), courseFilter, examFilter, 6));
        return data;
    }

    private String courseName(List<Course> courses, long courseId) {
        for (Course course : courses) {
            if (course.getId() == courseId) {
                return course.getName();
            }
        }
        return "全部课程";
    }
}
