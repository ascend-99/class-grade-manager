<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="登录"/>
<c:set var="pageClass" value="login-body"/>
<%@ include file="common/head.jspf" %>

<div class="login-shell">
    <section class="login-showcase">
        <div class="login-brand">
            <span class="brand-mark">知</span>
            <span class="brand-text">
                <strong>知衡</strong>
                <small>ZHIHENG · GRADE STUDIO</small>
            </span>
        </div>

        <div class="login-copy">
            <span class="eyebrow" style="color:#8fa0b9;">CLASS GRADE STUDIO</span>
            <h1>让每一次进步，<br><em>都有迹可循。</em></h1>
            <p>为班级打造的轻盈成绩管理空间。录入、查询、统计，一条清晰的教学线索，让数据真正服务于每一次成长。</p>
        </div>

        <div class="login-preview">
            <div class="login-preview-head">
                <span><span class="live-dot"></span> 班级学习概览</span>
                <span>2026 – 2027 秋</span>
            </div>
            <div class="login-preview-chart" aria-hidden="true">
                <svg viewBox="0 0 400 66" preserveAspectRatio="none">
                    <defs>
                        <linearGradient id="loginArea" x1="0" y1="0" x2="0" y2="1">
                            <stop offset="0" stop-color="#818cf8" stop-opacity=".38"/>
                            <stop offset="1" stop-color="#818cf8" stop-opacity="0"/>
                        </linearGradient>
                        <linearGradient id="loginLine" x1="0" y1="0" x2="1" y2="0">
                            <stop offset="0" stop-color="#a5b4fc"/>
                            <stop offset="1" stop-color="#f5c66e"/>
                        </linearGradient>
                    </defs>
                    <path d="M0,52 C30,48 45,37 72,40 C102,43 116,26 145,30 C174,34 188,18 217,22 C248,27 258,12 290,17 C322,22 342,8 400,10 L400,66 L0,66 Z" fill="url(#loginArea)"/>
                    <path d="M0,52 C30,48 45,37 72,40 C102,43 116,26 145,30 C174,34 188,18 217,22 C248,27 258,12 290,17 C322,22 342,8 400,10" fill="none" stroke="url(#loginLine)" stroke-width="2" stroke-linecap="round"/>
                    <circle cx="290" cy="17" r="4" fill="#0b1220" stroke="#f5c66e" stroke-width="2"/>
                </svg>
            </div>
            <div class="login-preview-stats">
                <div><span>班级平均分</span><strong>78.4<small> 分</small></strong></div>
                <div><span>及格率</span><strong>92.1<small> %</small></strong></div>
                <div><span>优秀人次</span><strong>36<small> 人次</small></strong></div>
            </div>
        </div>

        <div class="login-showcase-foot">
            <span><svg class="icon"><use href="#i-shield"></use></svg> 数据仅用于教学演示</span>
            <span>JAVAEE · SERVLET / JSP</span>
        </div>
    </section>

    <section class="login-panel">
        <div class="login-form-wrap">
            <div class="login-mobile-brand">
                <span class="brand-mark">知</span>
                <strong>知衡</strong>
            </div>

            <span class="eyebrow">WELCOME BACK</span>
            <h2>登录你的教学空间</h2>
            <p>使用教师或学生账号，进入属于你的知衡。</p>

            <c:if test="${not empty notice}">
                <div class="alert alert-info login-alert" data-alert data-autohide="false">
                    <svg class="icon"><use href="#i-info"></use></svg>
                    <div><c:out value="${notice}"/></div>
                </div>
            </c:if>
            <c:if test="${not empty error}">
                <div class="alert alert-error login-alert" data-alert data-autohide="false" role="alert">
                    <svg class="icon"><use href="#i-alert"></use></svg>
                    <div><c:out value="${error}"/></div>
                </div>
            </c:if>
            <%@ include file="common/flash.jspf" %>

            <form class="login-form" method="post" action="${ctx}/login" autocomplete="on">
                <div class="field">
                    <label class="field-label" for="username">账号</label>
                    <div class="input-icon">
                        <svg class="icon"><use href="#i-user"></use></svg>
                        <input class="input" id="username" name="username" type="text"
                               value="${fn:escapeXml(username)}" placeholder="教师账号或学生学号"
                               autocomplete="username" maxlength="50" required autofocus>
                    </div>
                </div>

                <div class="field">
                    <label class="field-label" for="password">密码</label>
                    <div class="input-suffix">
                        <div class="input-icon">
                            <svg class="icon"><use href="#i-lock"></use></svg>
                            <input class="input" id="password" name="password" type="password"
                                   placeholder="请输入登录密码" autocomplete="current-password"
                                   maxlength="50" required>
                        </div>
                        <button type="button" class="suffix-btn" id="togglePassword" aria-label="显示密码">
                            <svg class="icon"><use href="#i-eye"></use></svg>
                        </button>
                    </div>
                    <div class="field-hint" id="capsLockHint" hidden>大写锁定已开启</div>
                </div>

                <button type="submit" class="btn btn-primary btn-block login-submit">
                    进入知衡
                    <svg class="icon"><use href="#i-arrow-right"></use></svg>
                </button>
            </form>

            <div class="demo-block">
                <div class="demo-label">演示账号 · 点击自动填入</div>
                <div class="demo-accounts">
                    <button type="button" class="demo-account" data-demo-user="teacher" data-demo-password="123456">
                        <span class="avatar indigo">周</span>
                        <span class="demo-account-copy">
                            <strong>教师演示</strong>
                            <span>teacher / 123456</span>
                        </span>
                    </button>
                    <button type="button" class="demo-account" data-demo-user="20230101" data-demo-password="123456">
                        <span class="avatar teal">林</span>
                        <span class="demo-account-copy">
                            <strong>学生演示</strong>
                            <span>20230101 / 123456</span>
                        </span>
                    </button>
                </div>
            </div>

            <p class="login-footnote">
                首次运行会自动创建演示数据 · 教师端与学生端数据实时互通<br>
                课程设计 · 基于 Servlet + JSP + JDBC 构建
            </p>
        </div>
    </section>
</div>

<script src="${ctx}/assets/js/app.js"></script>
</body>
</html>
