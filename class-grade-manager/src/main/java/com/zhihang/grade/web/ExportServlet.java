package com.zhihang.grade.web;

import com.zhihang.grade.model.AnalyticsData;
import com.zhihang.grade.model.CourseStat;
import com.zhihang.grade.model.GradeRow;
import com.zhihang.grade.service.AnalyticsService;
import com.zhihang.grade.service.GradeService;
import com.zhihang.grade.util.CsvUtil;
import com.zhihang.grade.util.ExcelUtil;
import com.zhihang.grade.util.Web;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ExportServlet extends BaseServlet {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final GradeService gradeService = new GradeService();
    private final AnalyticsService analyticsService = new AnalyticsService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String type = Web.param(request, "type", "grades");
        boolean excel = "xlsx".equalsIgnoreCase(Web.param(request, "format"));
        if ("analytics".equals(type)) {
            exportAnalytics(request, response, excel);
        } else {
            exportGrades(request, response, excel);
        }
    }

    private void exportGrades(HttpServletRequest request, HttpServletResponse response, boolean excel)
            throws IOException {
        long classId = Web.longParam(request, "classId", 0);
        long courseId = Web.longParam(request, "courseId", 0);
        String examName = Web.param(request, "examName");
        String keyword = Web.param(request, "q");
        List<GradeRow> rows = gradeService.forExport(
                classId > 0 ? classId : null,
                courseId > 0 ? courseId : null,
                examName, keyword);
        List<String> headers = List.of("学号", "姓名", "班级", "课程代码", "课程名称", "考试",
                "考试日期", "成绩", "等级", "录入教师", "更新时间");
        List<List<String>> data = new ArrayList<>();
        for (GradeRow row : rows) {
            data.add(List.of(
                    row.getStudentNo(),
                    row.getStudentName(),
                    row.getClassName(),
                    row.getCourseCode(),
                    row.getCourseName(),
                    row.getExamName(),
                    row.getExamDate() == null ? "" : row.getExamDate().toLocalDate().toString(),
                    String.format("%.1f", row.getScore()),
                    row.getLevel(),
                    row.getRecordedBy() == null ? "" : row.getRecordedBy(),
                    row.getUpdatedAt() == null ? "" : row.getUpdatedAt().toLocalDateTime().format(TIME_FORMAT)
            ));
        }
        if (excel) {
            ExcelUtil.writeTable(response, "知衡-成绩明细", "知衡 · 班级成绩明细", headers, data);
        } else {
            CsvUtil.writeCsv(response, "知衡-成绩明细", headers, data);
        }
    }

    private void exportAnalytics(HttpServletRequest request, HttpServletResponse response, boolean excel)
            throws IOException {
        long classId = Web.longParam(request, "classId", 0);
        long courseId = Web.longParam(request, "courseId", 0);
        String examName = Web.param(request, "examName");
        AnalyticsData data = analyticsService.load(
                classId > 0 ? classId : null,
                courseId > 0 ? courseId : null,
                examName);
        List<String> headers = List.of("数据维度", "班级", "课程范围", "考试范围", "记录数",
                "平均分", "及格率(%)", "优秀率(%)", "最高分", "最低分", "中位数");
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of(
                "班级总览",
                data.getClassName(),
                data.getCourseName(),
                data.isAllExams() ? "全部考试" : data.getExamName(),
                String.valueOf(data.getStats().getRecordCount()),
                String.format("%.1f", data.getStats().getAverage()),
                String.format("%.1f", data.getStats().getPassRate()),
                String.format("%.1f", data.getStats().getExcellentRate()),
                String.format("%.1f", data.getStats().getHighest()),
                String.format("%.1f", data.getStats().getLowest()),
                String.format("%.1f", data.getStats().getMedian())
        ));
        for (CourseStat stat : data.getCourseStats()) {
            rows.add(List.of(
                    "课程明细",
                    data.getClassName(),
                    stat.getCourseName(),
                    data.isAllExams() ? "全部考试" : data.getExamName(),
                    String.valueOf(stat.getRecordCount()),
                    String.format("%.1f", stat.getAverage()),
                    String.format("%.1f", stat.getPassRate()),
                    String.format("%.1f", stat.getExcellentRate()),
                    String.format("%.1f", stat.getHighest()),
                    String.format("%.1f", stat.getLowest()),
                    ""
            ));
        }
        if (excel) {
            ExcelUtil.writeTable(response, "知衡-成绩分析", "知衡 · 班级成绩分析", headers, rows);
        } else {
            CsvUtil.writeCsv(response, "知衡-成绩分析", headers, rows);
        }
    }
}
