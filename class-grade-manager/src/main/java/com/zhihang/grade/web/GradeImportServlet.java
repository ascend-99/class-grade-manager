package com.zhihang.grade.web;

import com.zhihang.grade.model.ClassInfo;
import com.zhihang.grade.model.Course;
import com.zhihang.grade.model.GradeEntryRow;
import com.zhihang.grade.service.ClassService;
import com.zhihang.grade.service.CourseService;
import com.zhihang.grade.service.GradeService;
import com.zhihang.grade.service.ServiceResult;
import com.zhihang.grade.util.ExcelUtil;
import com.zhihang.grade.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GradeImportServlet extends BaseServlet {

    private final GradeService gradeService = new GradeService();
    private final ClassService classService = new ClassService();
    private final CourseService courseService = new CourseService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long classId = Web.longParam(request, "classId", 0);
        long courseId = Web.longParam(request, "courseId", 0);
        String examName = Web.param(request, "examName");
        String target = "/entry?classId=" + classId + "&courseId=" + courseId + "&examName="
                + URLEncoder.encode(examName, StandardCharsets.UTF_8);

        ClassInfo classInfo = classService.findById(classId);
        Course course = courseService.findById(courseId);
        if (classInfo == null || course == null || examName.isBlank()) {
            Web.flashError(request, "导入参数不完整，请重新选择班级、课程和考试");
            redirect(request, response, "/entry");
            return;
        }

        Part part;
        try {
            part = request.getPart("file");
        } catch (IllegalStateException ex) {
            Web.flashError(request, "文件过大，请上传 5MB 以内的 Excel 文件");
            redirect(request, response, target);
            return;
        }
        if (part == null || part.getSize() == 0) {
            Web.flashError(request, "请选择要导入的 Excel 文件");
            redirect(request, response, target);
            return;
        }
        if (part.getSize() > 5 * 1024 * 1024) {
            Web.flashError(request, "文件过大，请上传 5MB 以内的 Excel 文件");
            redirect(request, response, target);
            return;
        }
        String fileName = part.getSubmittedFileName() == null ? "" : part.getSubmittedFileName();
        if (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xls")) {
            Web.flashError(request, "只支持 .xlsx 或 .xls 格式的 Excel 文件");
            redirect(request, response, target);
            return;
        }

        List<GradeEntryRow> roster = gradeService.entryRows(classId, courseId, examName);
        Map<String, GradeEntryRow> rosterByNo = new HashMap<>();
        for (GradeEntryRow row : roster) {
            rosterByNo.put(row.getStudentNo(), row);
        }
        if (rosterByNo.isEmpty()) {
            Web.flashError(request, "该班级暂无学生，无法导入成绩");
            redirect(request, response, target);
            return;
        }

        try (Workbook workbook = WorkbookFactory.create(part.getInputStream())) {
            if (workbook.getNumberOfSheets() == 0) {
                Web.flashError(request, "Excel 中没有工作表");
                redirect(request, response, target);
                return;
            }
            Sheet sheet = workbook.getSheetAt(0);
            int headerRow = findHeaderRow(sheet);
            if (headerRow < 0) {
                Web.flashError(request, "没有找到表头，请使用系统下载的模板");
                redirect(request, response, target);
                return;
            }
            int studentNoColumn = findColumn(sheet.getRow(headerRow), "学号");
            int scoreColumn = findColumn(sheet.getRow(headerRow), "成绩");
            if (scoreColumn < 0) {
                scoreColumn = findColumn(sheet.getRow(headerRow), "分数");
            }
            if (studentNoColumn < 0 || scoreColumn < 0) {
                Web.flashError(request, "模板缺少「学号」或「成绩」列，请使用系统下载的模板");
                redirect(request, response, target);
                return;
            }

            Map<Long, String> scores = new HashMap<>();
            Set<String> seen = new HashSet<>();
            List<String> errors = new ArrayList<>();
            int skipped = 0;
            for (int i = headerRow + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                String studentNo = ExcelUtil.text(row.getCell(studentNoColumn));
                String scoreText = ExcelUtil.text(row.getCell(scoreColumn));
                if (studentNo.isBlank() && scoreText.isBlank()) {
                    continue;
                }
                if (studentNo.isBlank()) {
                    errors.add("第 " + (i + 1) + " 行缺少学号");
                    continue;
                }
                GradeEntryRow student = rosterByNo.get(studentNo);
                if (student == null) {
                    errors.add("第 " + (i + 1) + " 行学号 " + studentNo + " 不属于该班级");
                    continue;
                }
                if (!seen.add(studentNo)) {
                    errors.add("第 " + (i + 1) + " 行学号 " + studentNo + " 重复");
                    continue;
                }
                if (scoreText.isBlank()) {
                    skipped++;
                    continue;
                }
                double score;
                try {
                    score = Double.parseDouble(scoreText);
                } catch (NumberFormatException ex) {
                    errors.add("第 " + (i + 1) + " 行「" + student.getStudentName() + "」成绩不是数字");
                    continue;
                }
                if (!Double.isFinite(score) || score < 0 || score > 100) {
                    errors.add("第 " + (i + 1) + " 行「" + student.getStudentName()
                            + "」成绩需在 0-100 之间");
                    continue;
                }
                scores.put(student.getStudentId(), String.valueOf(score));
            }

            if (!errors.isEmpty()) {
                String message = "导入失败，共 " + errors.size() + " 处问题："
                        + String.join("；", errors.subList(0, Math.min(5, errors.size())));
                if (errors.size() > 5) {
                    message += "；……";
                }
                Web.flashError(request, message);
                redirect(request, response, target);
                return;
            }
            if (scores.isEmpty()) {
                Web.flashError(request, "Excel 中没有可导入的成绩" + (skipped > 0 ? "（成绩列全部为空）" : ""));
                redirect(request, response, target);
                return;
            }

            ServiceResult<GradeService.BatchSummary> result = gradeService.saveBatch(
                    classId, courseId, examName, scores,
                    currentUser(request).getDisplayName(), "Excel 导入");
            if (result.isSuccess()) {
                GradeService.BatchSummary summary = result.getData();
                String message = "Excel 导入成功：";
                if (summary != null) {
                    message += summary.getCreated() + " 条新增，" + summary.getUpdated() + " 条更新";
                } else {
                    message += result.getMessage();
                }
                if (skipped > 0) {
                    message += "，跳过 " + skipped + " 条空成绩";
                }
                Web.flashSuccess(request, message);
            } else {
                Web.flashError(request, result.getMessage());
            }
        } catch (Exception ex) {
            Web.flashError(request, "Excel 解析失败，请确认文件没有损坏，并使用系统模板");
        }
        redirect(request, response, target);
    }

    private int findHeaderRow(Sheet sheet) {
        int last = Math.min(sheet.getLastRowNum(), 12);
        for (int i = 0; i <= last; i++) {
            Row row = sheet.getRow(i);
            if (row != null && findColumn(row, "学号") >= 0
                    && (findColumn(row, "成绩") >= 0 || findColumn(row, "分数") >= 0)) {
                return i;
            }
        }
        return -1;
    }

    private int findColumn(Row row, String name) {
        if (row == null) {
            return -1;
        }
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && name.equals(ExcelUtil.text(cell))) {
                return i;
            }
        }
        return -1;
    }
}
