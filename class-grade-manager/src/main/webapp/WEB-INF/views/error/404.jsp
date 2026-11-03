<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="页面未找到"/>
<c:set var="pageClass" value="error-body"/>
<%@ include file="../common/head.jspf" %>

<div class="error-card">
    <div class="error-mark">
        <svg class="icon"><use href="#i-search"></use></svg>
    </div>
    <div class="error-code">404</div>
    <h1>这一页走丢了</h1>
    <p>你访问的页面不存在，或者已经被移动。可以返回工作台，继续查看班级数据。</p>
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
