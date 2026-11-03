package com.zhihang.grade.web.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * 页面渲染完成后移除一次性提示；302 跳转时保留，供下一次 GET 显示。
 */
public class FlashFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // no-op
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(request, response);
        if (request instanceof HttpServletRequest httpRequest
                && response instanceof HttpServletResponse httpResponse) {
            int status = httpResponse.getStatus();
            if (status < 300 || status >= 400) {
                HttpSession session = httpRequest.getSession(false);
                if (session != null) {
                    session.removeAttribute("flashType");
                    session.removeAttribute("flashMessage");
                }
            }
        }
    }

    @Override
    public void destroy() {
        // no-op
    }
}
