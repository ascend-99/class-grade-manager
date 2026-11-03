<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="个人中心"/>
<c:set var="pageEyebrow" value="ACCOUNT"/>
<c:set var="activeNav" value="profile"/>
<%@ include file="common/head.jspf" %>

<div class="app-shell">
    <%@ include file="common/sidebar.jspf" %>
    <div class="main-shell">
        <%@ include file="common/topbar.jspf" %>
        <main class="page-content">
            <%@ include file="common/flash.jspf" %>

            <div class="profile-layout">
                <section class="profile-card">
                    <div class="profile-card-top">
                        <div class="profile-avatar">${fn:escapeXml(currentUser.initial)}</div>
                        <h2><c:out value="${currentUser.displayName}"/></h2>
                        <p>@${fn:escapeXml(currentUser.username)}</p>
                        <span class="profile-role">
                            <svg class="icon" style="width:11px;height:11px;"><use href="#i-shield"></use></svg>
                            ${currentUser.roleLabel}账号
                        </span>
                    </div>
                    <div class="profile-info">
                        <div class="profile-info-row">
                            <span>账号角色</span>
                            <strong>${currentUser.teacher ? '教师 · 班级管理' : '学生 · 成绩查询'}</strong>
                        </div>
                        <c:choose>
                            <c:when test="${currentUser.teacher}">
                                <div class="profile-info-row">
                                    <span>工号</span>
                                    <strong>${fn:escapeXml(currentUser.username)}</strong>
                                </div>
                                <div class="profile-info-row">
                                    <span>管理范围</span>
                                    <strong>计算机科学与技术 2301班 等 3 个班级</strong>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="profile-info-row">
                                    <span>学号</span>
                                    <strong>${fn:escapeXml(account.studentNo)}</strong>
                                </div>
                                <div class="profile-info-row">
                                    <span>班级</span>
                                    <strong><c:out value="${account.className}"/></strong>
                                </div>
                                <div class="profile-info-row">
                                    <span>学籍状态</span>
                                    <strong>${student.status}</strong>
                                </div>
                            </c:otherwise>
                        </c:choose>
                        <div class="profile-info-row">
                            <span>最近登录</span>
                            <strong>
                                <c:choose>
                                    <c:when test="${not empty account.lastLoginAt}">
                                        <fmt:formatDate value="${account.lastLoginAt}" pattern="yyyy.MM.dd HH:mm"/>
                                    </c:when>
                                    <c:otherwise>首次登录</c:otherwise>
                                </c:choose>
                            </strong>
                        </div>
                        <div class="profile-info-row">
                            <span>账号状态</span>
                            <strong><span class="badge badge-success">正常使用</span></strong>
                        </div>
                    </div>
                </section>

                <section class="card password-card">
                    <h2>账号安全</h2>
                    <p>建议定期更换密码。为了演示方便，初始密码统一为 123456。</p>

                    <form class="password-form" id="passwordForm" method="post" action="${ctx}/profile">
                        <input type="hidden" name="action" value="password">
                        <div class="field" style="margin-bottom:16px;">
                            <label class="field-label" for="oldPassword">当前密码 <span class="req">*</span></label>
                            <div class="input-suffix">
                                <input class="input" id="oldPassword" name="oldPassword" type="password"
                                       autocomplete="current-password" required placeholder="输入当前登录密码">
                                <button type="button" class="suffix-btn toggle-password" data-target="oldPassword" aria-label="显示密码">
                                    <svg class="icon"><use href="#i-eye"></use></svg>
                                </button>
                            </div>
                        </div>
                        <div class="field" style="margin-bottom:8px;">
                            <label class="field-label" for="newPassword">新密码 <span class="req">*</span></label>
                            <div class="input-suffix">
                                <input class="input" id="newPassword" name="newPassword" type="password"
                                       minlength="6" maxlength="50" autocomplete="new-password" required
                                       placeholder="至少 6 位字符">
                                <button type="button" class="suffix-btn toggle-password" data-target="newPassword" aria-label="显示密码">
                                    <svg class="icon"><use href="#i-eye"></use></svg>
                                </button>
                            </div>
                        </div>
                        <div class="password-strength" id="passwordStrength" aria-hidden="true">
                            <span></span><span></span><span></span><span></span>
                        </div>
                        <div class="field" style="margin-top:16px;">
                            <label class="field-label" for="confirmPassword">确认新密码 <span class="req">*</span></label>
                            <input class="input" id="confirmPassword" name="confirmPassword" type="password"
                                   minlength="6" maxlength="50" autocomplete="new-password" required
                                   placeholder="再次输入新密码">
                        </div>
                        <button type="submit" class="btn btn-primary" style="margin-top:20px;">
                            <svg class="icon"><use href="#i-key"></use></svg>
                            更新密码
                        </button>
                    </form>

                    <div class="security-list">
                        <div class="security-item">
                            <svg class="icon"><use href="#i-shield"></use></svg>
                            <span>密码使用 BCrypt 加密存储，系统不会保存明文密码。修改后下一次登录生效。</span>
                        </div>
                        <div class="security-item">
                            <svg class="icon"><use href="#i-clock"></use></svg>
                            <span>登录状态保持 60 分钟，离开公共电脑时请主动退出登录。</span>
                        </div>
                        <div class="security-item">
                            <svg class="icon"><use href="#i-info"></use></svg>
                            <span>本系统为课程设计演示项目，数据库保存在本机 ./data 目录中，不会上传任何个人信息。</span>
                        </div>
                    </div>
                </section>
            </div>
        </main>
        <%@ include file="common/footer.jspf" %>

<script>
(function () {
    document.querySelectorAll('.toggle-password').forEach(function (button) {
        button.addEventListener('click', function () {
            const input = document.getElementById(button.dataset.target);
            if (!input) return;
            const visible = input.type === 'text';
            input.type = visible ? 'password' : 'text';
            button.innerHTML = visible
                ? '<svg class="icon"><use href="#i-eye"></use></svg>'
                : '<svg class="icon"><use href="#i-eye-off"></use></svg>';
        });
    });
})();
</script>
</body>
</html>
