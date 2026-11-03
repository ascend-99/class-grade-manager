package com.zhihang.grade.config;

import com.zhihang.grade.util.PasswordUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 首次启动时写入一套可直接演示的数据。
 * 数据库中已有班级时不会重复写入。
 */
public final class DemoData {

    private static final String[][] CLASS_NAMES = {
            {"计算机科学与技术 2301班", "2023", "计算机科学与技术", "周明远"},
            {"软件工程 2302班", "2023", "软件工程", "李静"},
            {"人工智能 2301班", "2023", "人工智能", "王启"}
    };

    private static final String[][] STUDENTS = {
            {"林知夏", "女"}, {"陈屿", "男"}, {"沈星野", "男"}, {"许清和", "女"},
            {"顾言", "男"}, {"苏沐", "女"}, {"江晚", "女"}, {"陆沉舟", "男"},
            {"温以宁", "女"}, {"宋嘉树", "男"}, {"叶青", "女"}, {"唐棠", "女"},
            {"何清越", "男"}, {"周叙", "男"}, {"白露", "女"}, {"方予安", "男"},
            {"程砚", "男"}, {"夏栀", "女"}, {"秦朗", "男"}, {"喻可", "女"},
            {"韩叙", "男"}, {"林墨", "男"}, {"段清", "女"}, {"罗一舟", "男"},
            {"苏晚晴", "女"}, {"魏来", "男"}, {"夏知遇", "女"}, {"乔安", "女"},
            {"谢云舟", "男"}, {"温言", "女"}, {"江照", "男"}, {"沈知意", "女"},
            {"傅斯年", "男"}, {"夏木", "男"}, {"池鱼", "女"}, {"顾南", "男"},
            {"闻溪", "女"}, {"许星河", "男"}
    };

    private static final int[] CLASS_SIZES = {16, 12, 10};

    private static final String[][] COURSES = {
            {"JAVAEE", "JavaEE 企业级开发", "4.0", "周明远", "2026-2027 秋", "indigo"},
            {"DB", "数据库系统原理", "3.5", "李静", "2026-2027 秋", "teal"},
            {"DS", "数据结构与算法", "4.0", "王启", "2026-2027 秋", "amber"},
            {"NET", "计算机网络", "3.0", "赵行", "2026-2027 秋", "rose"},
            {"WEB", "Web 前端开发", "3.0", "陈思", "2026-2027 秋", "sky"}
    };

    private static final boolean[][] CLASS_COURSES = {
            {true, true, true, true, true},
            {true, true, true, false, true},
            {true, true, true, true, false}
    };

    private static final String[] EXAMS = {"单元测验", "课堂测验", "期中考试"};
    private static final LocalDate[] EXAM_DATES = {
            LocalDate.of(2026, 10, 10),
            LocalDate.of(2026, 10, 20),
            LocalDate.of(2026, 10, 30)
    };

    private DemoData() {
    }

    public static synchronized void seedIfEmpty() {
        try (Connection connection = Database.getConnection()) {
            if (countClasses(connection) > 0) {
                return;
            }
            connection.setAutoCommit(false);
            try {
                String demoHash = PasswordUtil.hash("123456");
                List<Long> classIds = insertClasses(connection);
                List<Long> studentIds = insertStudents(connection, classIds, demoHash);
                List<Long> courseIds = insertCourses(connection);
                insertGrades(connection, classIds, studentIds, courseIds);
                insertTeacher(connection, demoHash);
                connection.commit();
                System.out.println("[知衡] 演示数据初始化完成：3 个班级 / 38 名学生 / 5 门课程");
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("演示数据初始化失败：" + ex.getMessage(), ex);
        }
    }

    private static int countClasses(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM classes")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static List<Long> insertClasses(Connection connection) throws SQLException {
        List<Long> ids = new ArrayList<>();
        String sql = "INSERT INTO classes (name, grade_year, major, advisor) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (String[] item : CLASS_NAMES) {
                ps.setString(1, item[0]);
                ps.setString(2, item[1]);
                ps.setString(3, item[2]);
                ps.setString(4, item[3]);
                ps.addBatch();
            }
            ps.executeBatch();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                while (keys.next()) {
                    ids.add(keys.getLong(1));
                }
            }
        }
        if (ids.size() != CLASS_NAMES.length) {
            throw new SQLException("班级初始化数量异常");
        }
        return ids;
    }

    private static List<Long> insertStudents(Connection connection, List<Long> classIds,
                                              String demoHash) throws SQLException {
        List<Long> ids = new ArrayList<>();
        String studentSql = """
                INSERT INTO students (student_no, name, gender, class_id, phone, email, status)
                VALUES (?, ?, ?, ?, ?, ?, '在读')
                """;
        String userSql = """
                INSERT INTO users (username, password_hash, role_code, display_name, student_id, avatar_color)
                VALUES (?, ?, 'STUDENT', ?, ?, ?)
                """;
        int cursor = 0;
        String[] colors = {"indigo", "teal", "amber", "rose", "sky"};
        try (PreparedStatement studentPs = connection.prepareStatement(studentSql, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement userPs = connection.prepareStatement(userSql)) {
            for (int classIndex = 0; classIndex < CLASS_SIZES.length; classIndex++) {
                for (int i = 0; i < CLASS_SIZES[classIndex]; i++, cursor++) {
                    String studentNo = String.format("2023%02d%02d", classIndex + 1, i + 1);
                    String name = STUDENTS[cursor][0];
                    String gender = STUDENTS[cursor][1];
                    studentPs.setString(1, studentNo);
                    studentPs.setString(2, name);
                    studentPs.setString(3, gender);
                    studentPs.setLong(4, classIds.get(classIndex));
                    studentPs.setString(5, String.format("138%08d", classIndex * 100 + i + 1));
                    studentPs.setString(6, studentNo + "@campus.edu.cn");
                    studentPs.addBatch();
                }
            }
            studentPs.executeBatch();
            try (ResultSet keys = studentPs.getGeneratedKeys()) {
                while (keys.next()) {
                    ids.add(keys.getLong(1));
                }
            }
            if (ids.size() != STUDENTS.length) {
                throw new SQLException("学生初始化数量异常");
            }
            cursor = 0;
            for (int classIndex = 0; classIndex < CLASS_SIZES.length; classIndex++) {
                for (int i = 0; i < CLASS_SIZES[classIndex]; i++, cursor++) {
                    String studentNo = String.format("2023%02d%02d", classIndex + 1, i + 1);
                    userPs.setString(1, studentNo);
                    userPs.setString(2, demoHash);
                    userPs.setString(3, STUDENTS[cursor][0]);
                    userPs.setLong(4, ids.get(cursor));
                    userPs.setString(5, colors[classIndex % colors.length]);
                    userPs.addBatch();
                }
            }
            userPs.executeBatch();
        }
        return ids;
    }

    private static List<Long> insertCourses(Connection connection) throws SQLException {
        List<Long> ids = new ArrayList<>();
        String sql = """
                INSERT INTO courses (code, name, credit, teacher, semester, color)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (String[] item : COURSES) {
                ps.setString(1, item[0]);
                ps.setString(2, item[1]);
                ps.setDouble(3, Double.parseDouble(item[2]));
                ps.setString(4, item[3]);
                ps.setString(5, item[4]);
                ps.setString(6, item[5]);
                ps.addBatch();
            }
            ps.executeBatch();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                while (keys.next()) {
                    ids.add(keys.getLong(1));
                }
            }
        }
        if (ids.size() != COURSES.length) {
            throw new SQLException("课程初始化数量异常");
        }
        return ids;
    }

    private static void insertGrades(Connection connection, List<Long> classIds,
                                     List<Long> studentIds, List<Long> courseIds) throws SQLException {
        String sql = """
                INSERT INTO grades (student_id, course_id, exam_name, exam_date, score, recorded_by,
                                    recorded_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        int studentCursor = 0;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int classIndex = 0; classIndex < CLASS_SIZES.length; classIndex++) {
                for (int i = 0; i < CLASS_SIZES[classIndex]; i++, studentCursor++) {
                    long studentId = studentIds.get(studentCursor);
                    for (int courseIndex = 0; courseIndex < COURSES.length; courseIndex++) {
                        if (!CLASS_COURSES[classIndex][courseIndex]) {
                            continue;
                        }
                        for (int examIndex = 0; examIndex < EXAMS.length; examIndex++) {
                            double score = calculateScore(classIndex, i, courseIndex, examIndex);
                            int hour = 9 + ((i * 3 + courseIndex * 2 + examIndex) % 8);
                            int minute = (i * 13 + courseIndex * 7 + examIndex * 5) % 60;
                            Timestamp recordedAt = Timestamp.valueOf(
                                    EXAM_DATES[examIndex].atTime(hour, minute));
                            ps.setLong(1, studentId);
                            ps.setLong(2, courseIds.get(courseIndex));
                            ps.setString(3, EXAMS[examIndex]);
                            ps.setDate(4, Date.valueOf(EXAM_DATES[examIndex]));
                            ps.setDouble(5, score);
                            ps.setString(6, COURSES[courseIndex][3]);
                            ps.setTimestamp(7, recordedAt);
                            ps.setTimestamp(8, recordedAt);
                            ps.addBatch();
                        }
                    }
                }
            }
            ps.executeBatch();
        }
    }

    private static double calculateScore(int classIndex, int studentIndex, int courseIndex, int examIndex) {
        double[] courseOffset = {1.5, -2.0, 0.5, -4.0, 2.5};
        double ability = 61 + ((studentIndex * 19 + classIndex * 13 + courseIndex * 3) % 32);
        double noise = ((studentIndex * 7 + courseIndex * 5 + examIndex * 3) % 9) - 4;
        double score = ability + courseOffset[courseIndex] + examIndex * 1.5 + noise;
        if (studentIndex % 11 == 5) {
            score -= 17;
        }
        if (studentIndex % 17 == 9) {
            score -= 10;
        }
        if (classIndex == 1 && studentIndex == 6) {
            score -= 8;
        }
        if (classIndex == 0 && studentIndex == 0) {
            // 学生演示账号：让「林知夏」呈现一份更有参考价值的成绩
            score += 18;
        }
        score = Math.max(42, Math.min(99, score));
        return Math.round(score * 10.0) / 10.0;
    }

    private static void insertTeacher(Connection connection, String demoHash) throws SQLException {
        String sql = """
                INSERT INTO users (username, password_hash, role_code, display_name, student_id, avatar_color)
                VALUES ('teacher', ?, 'TEACHER', '周明远', NULL, 'indigo')
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, demoHash);
            ps.executeUpdate();
        }
    }
}
