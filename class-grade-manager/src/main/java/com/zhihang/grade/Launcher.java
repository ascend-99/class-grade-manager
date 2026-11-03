package com.zhihang.grade;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.net.URI;

/**
 * 内嵌 Tomcat 启动器：不需要单独安装 Tomcat。
 * 在 IDEA 中直接运行本类的 main 方法即可。
 */
public class Launcher {

    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws Exception {
        int port = resolvePort();
        File projectDir = resolveProjectDir();
        System.setProperty("user.dir", projectDir.getAbsolutePath());
        File webappDir = new File(projectDir, "src/main/webapp");
        File classesDir = new File(projectDir, "target/classes");
        System.out.println("项目目录   " + projectDir.getAbsolutePath());
        if (!webappDir.isDirectory()) {
            System.err.println("找不到 Web 目录：" + webappDir.getAbsolutePath());
            System.err.println("请在项目根目录运行，或使用 Maven 命令：mvn exec:java");
            System.exit(1);
        }
        if (!classesDir.isDirectory()) {
            System.err.println("找不到编译产物：" + classesDir.getAbsolutePath());
            System.err.println("请先执行：mvn compile");
            System.exit(1);
        }

        File baseDir = new File(projectDir, "target/tomcat");
        if (!baseDir.exists() && !baseDir.mkdirs()) {
            throw new IllegalStateException("无法创建 Tomcat 工作目录：" + baseDir);
        }

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(baseDir.getAbsolutePath());
        tomcat.setPort(port);
        tomcat.getConnector().setURIEncoding("UTF-8");
        Context context = tomcat.addWebapp("", webappDir.getAbsolutePath());
        context.setReloadable(false);
        // 让 webapp 能看到启动器所在的类加载器（IDEA 直接运行、Maven exec 都能工作）
        context.setParentClassLoader(Launcher.class.getClassLoader());
        WebResourceRoot resources = new StandardRoot(context);
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                classesDir.getAbsolutePath(), "/"));
        context.setResources(resources);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                tomcat.stop();
                tomcat.destroy();
            } catch (Exception ignored) {
                // 退出阶段无需处理
            }
        }, "zhihang-shutdown"));

        tomcat.start();
        String url = "http://localhost:" + port;
        printBanner(url);
        openBrowser(url);
        tomcat.getServer().await();
    }

    private static int resolvePort() {
        String property = System.getProperty("server.port");
        if (property == null || property.isBlank()) {
            property = System.getenv("PORT");
        }
        if (property != null && !property.isBlank()) {
            try {
                return Integer.parseInt(property.trim());
            } catch (NumberFormatException ignored) {
                System.err.println("端口配置无效，使用默认端口 " + DEFAULT_PORT);
            }
        }
        return DEFAULT_PORT;
    }

    /**
     * 兼容两种情况：
     * 1. IDEA 直接打开 class-grade-manager 目录（user.dir 就是项目根目录）
     * 2. IDEA 打开父目录 default-workspace（user.dir 是父目录，项目在其下）
     */
    private static File resolveProjectDir() {
        File cwd = new File(System.getProperty("user.dir")).getAbsoluteFile();
        if (new File(cwd, "src/main/webapp").isDirectory()) {
            return cwd;
        }
        File nested = new File(cwd, "class-grade-manager");
        if (new File(nested, "src/main/webapp").isDirectory()) {
            return nested;
        }
        try {
            File codeLocation = new File(Launcher.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).getAbsoluteFile();
            File candidate = codeLocation;
            if (candidate.isFile()) {
                candidate = candidate.getParentFile();
            }
            // 从 target/classes 回到项目根目录
            if (candidate != null && "classes".equals(candidate.getName())) {
                candidate = candidate.getParentFile();
            }
            if (candidate != null && "target".equals(candidate.getName())) {
                candidate = candidate.getParentFile();
            }
            if (candidate != null && new File(candidate, "src/main/webapp").isDirectory()) {
                return candidate;
            }
        } catch (Exception ignored) {
            // 无法从 class 位置推断时，继续使用当前工作目录
        }
        return cwd;
    }

    private static void printBanner(String url) {
        System.out.println();
        System.out.println("  ========================================================");
        System.out.println("    知衡 · 班级学生成绩管理系统  /  ZHIHENG GRADE STUDIO");
        System.out.println("  ========================================================");
        System.out.println("    访问地址   " + url);
        System.out.println("    教师账号   teacher / 123456");
        System.out.println("    学生账号   20230101 / 123456");
        System.out.println("    数据文件   ./data/zhihang.mv.db");
        System.out.println("  --------------------------------------------------------");
        System.out.println("    按 Ctrl + C 停止服务");
        System.out.println();
    }

    private static void openBrowser(String url) {
        String enabled = System.getProperty("open.browser", "true");
        if (!Boolean.parseBoolean(enabled) || GraphicsEnvironment.isHeadless()) {
            return;
        }
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(900);
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(URI.create(url));
                }
            } catch (Exception ignored) {
                // 没有桌面环境时忽略，用户可手动打开
            }
        }, "open-browser");
        thread.setDaemon(true);
        thread.start();
    }
}
