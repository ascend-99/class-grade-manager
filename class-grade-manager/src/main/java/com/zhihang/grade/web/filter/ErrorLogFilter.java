package com.zhihang.grade.web.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

/**
 * 兜底记录未处理异常，便于在 IDEA 控制台之外定位问题。
 * 不向浏览器暴露任何堆栈信息。
 */
public class ErrorLogFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // no-op
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Throwable ex) {
            log(ex);
            if (ex instanceof IOException io) {
                throw io;
            }
            if (ex instanceof ServletException se) {
                throw se;
            }
            throw new ServletException(ex);
        }
    }

    private void log(Throwable ex) {
        try {
            Path path = Path.of("logs", "error.log");
            Files.createDirectories(path.getParent());
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path,
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
                writer.println("[" + LocalDateTime.now() + "]");
                ex.printStackTrace(writer);
                writer.println();
            }
        } catch (IOException ignored) {
            // 日志失败不能影响主流程
        }
    }

    @Override
    public void destroy() {
        // no-op
    }
}
