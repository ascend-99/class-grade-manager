<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="系统异常"/>
<c:set var="pageClass" value="error-body"/>
<%@ include file="../common/head.jspf" %>

<div class="error-card">
    <div class="error-mark" style="color:#e11d48;background:#fff0f3;">
        <svg class="icon"><use href="#i-alert"></use></svg>
    </div>
    <div class="error-code" style="color:#fecdd3;">500</div>
    <h1>系统暂时开小差了</h1>
    <p>处理请求时出现了一点问题。请稍后重试；如果持续出现，可以重启服务并检查控制台日志。</p>
    <div class="error-actions">
        <c:choose>
            <c:when test="${not empty currentUser}">
                <a class="btn btn-primary" href="${ctx}${currentUser.homePath}">返回首页</a>
            </c:when>
            <c:otherwise>
                <a class="btn btn-primary" href="${ctx}/login">去登录</a>
            </c:otherwise>
        </c:choose>
        <a class="btn btn-ghost" href="javascript:history.back()">返回上一页</a>
    </div>
</div>
</body>
</html>
