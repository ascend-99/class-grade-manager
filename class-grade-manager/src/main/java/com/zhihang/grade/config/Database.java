package com.zhihang.grade.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * H2 数据库连接与建表。
 * 默认数据文件位于项目根目录 ./data/zhihang.mv.db，重启后数据仍在。
 * 可通过 JVM 参数 -Dgrade.db.url=... 指向其它数据库（测试时使用内存库）。
 */
public final class Database {

    private static final String DEFAULT_URL =
            "jdbc:h2:file:./data/zhihang;MODE=MySQL;DATABASE_TO_LOWER=TRUE;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private static volatile boolean initialized;

    private Database() {
    }

    public static String url() {
        String custom = System.getProperty("grade.db.url");
        if (custom != null && !custom.isBlank()) {
            return custom;
        }
        String env = System.getenv("GRADE_DB_URL");
        if (env != null && !env.isBlank()) {
            return env;
        }
        return DEFAULT_URL;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url(), USER, PASSWORD);
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        try {
            Class.forName("org.h2.Driver");
            ensureDataDirectory();
            try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
                for (String ddl : schema()) {
                    statement.execute(ddl);
                }
            }
            initialized = true;
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("未找到 H2 数据库驱动", ex);
        } catch (SQLException ex) {
            throw new IllegalStateException("数据库初始化失败：" + ex.getMessage(), ex);
        }
    }

    private static void ensureDataDirectory() {
        String url = url();
        if (!url.startsWith("jdbc:h2:file:")) {
            return;
        }
        String path = url.substring("jdbc:h2:file:".length());
        int params = path.indexOf(';');
        if (params >= 0) {
            path = path.substring(0, params);
        }
        Path parent = Path.of(path).toAbsolutePath().getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException ex) {
                throw new IllegalStateException("无法创建数据目录：" + parent, ex);
            }
        }
    }

    private static List<String> schema() {
        return List.of(
                """
                CREATE TABLE IF NOT EXISTS classes (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    grade_year VARCHAR(20) NOT NULL,
                    major VARCHAR(100),
                    advisor VARCHAR(50),
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS students (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    student_no VARCHAR(30) NOT NULL UNIQUE,
                    name VARCHAR(50) NOT NULL,
                    gender VARCHAR(10) NOT NULL DEFAULT '男',
                    class_id BIGINT NOT NULL,
                    phone VARCHAR(30),
                    email VARCHAR(120),
                    status VARCHAR(20) NOT NULL DEFAULT '在读',
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT fk_students_class FOREIGN KEY (class_id) REFERENCES classes(id)
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS courses (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    code VARCHAR(30) NOT NULL UNIQUE,
                    name VARCHAR(100) NOT NULL,
                    credit DECIMAL(4,1) NOT NULL DEFAULT 3.0,
                    teacher VARCHAR(50),
                    semester VARCHAR(50),
                    color VARCHAR(20) DEFAULT 'indigo',
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS users (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    password_hash VARCHAR(100) NOT NULL,
                    role_code VARCHAR(20) NOT NULL,
                    display_name VARCHAR(50) NOT NULL,
                    student_id BIGINT,
                    avatar_color VARCHAR(20) DEFAULT 'indigo',
                    last_login_at TIMESTAMP,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT fk_users_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS grades (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    student_id BIGINT NOT NULL,
                    course_id BIGINT NOT NULL,
                    exam_name VARCHAR(50) NOT NULL,
                    exam_date DATE,
                    score DECIMAL(5,2) NOT NULL,
                    recorded_by VARCHAR(50),
                    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT uk_grades_record UNIQUE (student_id, course_id, exam_name),
                    CONSTRAINT fk_grades_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
                    CONSTRAINT fk_grades_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
                    CONSTRAINT ck_grades_score CHECK (score >= 0 AND score <= 100)
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS grade_logs (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    grade_id BIGINT,
                    student_id BIGINT NOT NULL,
                    student_no VARCHAR(30),
                    student_name VARCHAR(50),
                    course_id BIGINT NOT NULL,
                    course_name VARCHAR(100),
                    exam_name VARCHAR(50),
                    old_score DECIMAL(5,2),
                    new_score DECIMAL(5,2),
                    action_type VARCHAR(20) NOT NULL,
                    changed_by VARCHAR(50),
                    reason VARCHAR(300),
                    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """,
                "CREATE INDEX IF NOT EXISTS idx_grade_logs_student ON grade_logs(student_id)",
                "CREATE INDEX IF NOT EXISTS idx_grade_logs_course ON grade_logs(course_id)",
                "CREATE INDEX IF NOT EXISTS idx_grade_logs_time ON grade_logs(changed_at)",
                "CREATE INDEX IF NOT EXISTS idx_students_class ON students(class_id)",
                "CREATE INDEX IF NOT EXISTS idx_grades_student ON grades(student_id)",
                "CREATE INDEX IF NOT EXISTS idx_grades_course ON grades(course_id)",
                "CREATE INDEX IF NOT EXISTS idx_grades_exam ON grades(exam_name)"
        );
    }
}
