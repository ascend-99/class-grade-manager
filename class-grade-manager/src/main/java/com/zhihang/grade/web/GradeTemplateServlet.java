package com.zhihang.grade.web;

import com.zhihang.grade.model.ClassInfo;
import com.zhihang.grade.model.Course;
import com.zhihang.grade.model.GradeEntryRow;
import com.zhihang.grade.service.ClassService;
import com.zhihang.grade.service.CourseService;
import com.zhihang.grade.service.GradeService;
import com.zhihang.grade.util.ExcelUtil;
import com.zhihang.grade.util.Web;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.util.List;

public class GradeTemplateServlet extends BaseServlet {

    private final GradeService gradeService = new GradeService();
    private final ClassService classService = new ClassService();
    private final CourseService courseService = new CourseService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long classId = Web.longParam(request, "classId", 0);
        long courseId = Web.longParam(request, "courseId", 0);
        String examName = Web.param(request, "examName");
        ClassInfo classInfo = classService.findById(classId);
        Course course = courseService.findById(courseId);
        if (classInfo == null || course == null || examName.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/entry");
            return;
        }
        List<GradeEntryRow> rows = gradeService.entryRows(classId, courseId, examName);

        XSSFWorkbook workbook = ExcelUtil.newWorkbook();
        Sheet sheet = workbook.createSheet("成绩导入");
        CellStyle titleStyle = ExcelUtil.titleStyle(workbook);
        CellStyle metaStyle = ExcelUtil.metaStyle(workbook);
        CellStyle headerStyle = ExcelUtil.headerStyle(workbook);
        CellStyle bodyStyle = ExcelUtil.bodyStyle(workbook);
        CellStyle centerStyle = ExcelUtil.centerStyle(workbook);

        Row title = sheet.createRow(0);
        title.setHeightInPoints(30);
        Cell titleCell = title.createCell(0);
        titleCell.setCellValue("知衡 · 成绩导入模板");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        Row meta = sheet.createRow(1);
        Cell metaCell = meta.createCell(0);
        metaCell.setCellValue("班级：" + classInfo.getName() + "    课程：" + course.getName()
                + "    考试：" + examName + "    授课教师：" + course.getTeacher());
        metaCell.setCellStyle(metaStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 3));

        Row note = sheet.createRow(2);
        Cell noteCell = note.createCell(0);
        noteCell.setCellValue("填写说明：只需填写「成绩」列，0–100 分，可保留一位小数；留空表示不录入。请不要修改学号和姓名。");
        noteCell.setCellStyle(metaStyle);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 3));

        Row header = sheet.createRow(4);
        header.setHeightInPoints(23);
        String[] headers = {"学号", "姓名", "成绩", "当前状态"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIndex = 5;
        for (GradeEntryRow item : rows) {
            Row row = sheet.createRow(rowIndex++);
            Cell no = row.createCell(0);
            no.setCellValue(item.getStudentNo());
            no.setCellStyle(bodyStyle);
            Cell name = row.createCell(1);
            name.setCellValue(item.getStudentName());
            name.setCellStyle(bodyStyle);
            Cell score = row.createCell(2);
            if (item.getScore() != null) {
                score.setCellValue(item.getScore());
            }
            score.setCellStyle(centerStyle);
            Cell status = row.createCell(3);
            status.setCellValue(item.isRecorded() ? "已录入" : "待录入");
            status.setCellStyle(centerStyle);
        }

        if (!rows.isEmpty()) {
            DataValidationHelper helper = sheet.getDataValidationHelper();
            DataValidationConstraint constraint = helper.createNumericConstraint(
                    DataValidationConstraint.ValidationType.DECIMAL,
                    DataValidationConstraint.OperatorType.BETWEEN, "0", "100");
            CellRangeAddressList range = new CellRangeAddressList(5, 4 + rows.size(), 2, 2);
            DataValidation validation = helper.createValidation(constraint, range);
            validation.setShowErrorBox(true);
            validation.createErrorBox("成绩格式不正确", "请输入 0 到 100 之间的数字。");
            sheet.addValidationData(validation);
        }

        sheet.setColumnWidth(0, 16 * 256);
        sheet.setColumnWidth(1, 14 * 256);
        sheet.setColumnWidth(2, 12 * 256);
        sheet.setColumnWidth(3, 14 * 256);
        sheet.createFreezePane(0, 5);

        ExcelUtil.write(response, workbook,
                "成绩导入模板-" + course.getCode() + "-" + examName);
    }
}
