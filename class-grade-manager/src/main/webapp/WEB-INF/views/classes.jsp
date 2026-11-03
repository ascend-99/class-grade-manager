<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="班级管理"/>
<c:set var="pageEyebrow" value="CLASSES"/>
<c:set var="activeNav" value="classes"/>
<%@ include file="common/head.jspf" %>

<div class="app-shell">
    <%@ include file="common/sidebar.jspf" %>
    <div class="main-shell">
        <%@ include file="common/topbar.jspf" %>
        <main class="page-content">
            <%@ include file="common/flash.jspf" %>

            <div class="page-toolbar">
                <div class="page-toolbar-copy">
                    <h2>班级档案</h2>
                    <p>维护班级名称、年级、专业与班主任；删除班级前需要先转出班级学生。</p>
                </div>
                <div class="page-toolbar-actions">
                    <button type="button" class="btn btn-primary" data-class-add>
                        <svg class="icon"><use href="#i-plus"></use></svg>
                        新增班级
                    </button>
                </div>
            </div>

            <div class="course-grid">
                <c:forEach var="item" items="${classes}">
                    <article class="course-card">
                        <div class="course-card-top">
                            <span class="course-code">${fn:escapeXml(item.gradeYear)} 级</span>
                            <div class="course-card-actions">
                                <button type="button" class="row-action" title="编辑班级"
                                        data-class-edit
                                        data-id="${item.id}"
                                        data-name="${fn:escapeXml(item.name)}"
                                        data-grade-year="${fn:escapeXml(item.gradeYear)}"
                                        data-major="${fn:escapeXml(item.major)}"
                                        data-advisor="${fn:escapeXml(item.advisor)}">
                                    <svg class="icon"><use href="#i-pencil"></use></svg>
                                </button>
                                <c:if test="${item.studentCount == 0}">
                                    <form method="post" action="${ctx}/classes" style="display:inline;"
                                          data-confirm="删除班级后无法恢复，确定要删除「${fn:escapeXml(item.name)}」吗？"
                                          data-confirm-title="删除这个班级？">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${item.id}">
                                        <button type="submit" class="row-action danger" title="删除班级">
                                            <svg class="icon"><use href="#i-trash"></use></svg>
                                        </button>
                                    </form>
                                </c:if>
                            </div>
                        </div>
                        <h3><c:out value="${item.name}"/></h3>
                        <div class="course-meta">
                            <span><svg class="icon"><use href="#i-graduation"></use></svg> ${fn:escapeXml(item.major)}</span>
                            <span><svg class="icon"><use href="#i-user"></use></svg> 班主任 ${fn:escapeXml(item.advisor)}</span>
                        </div>
                        <div class="course-stats" style="grid-template-columns:repeat(3,1fr);">
                            <div class="course-stat">
                                <span>学生</span>
                                <strong>${item.studentCount}<small>人</small></strong>
                            </div>
                            <div class="course-stat">
                                <span>平均分</span>
                                <strong><fmt:formatNumber value="${item.averageScore}" pattern="0.0"/></strong>
                            </div>
                            <div class="course-stat">
                                <span>及格率</span>
                                <strong><fmt:formatNumber value="${item.passRate}" pattern="0.0"/><small>%</small></strong>
                            </div>
                        </div>
                        <div class="course-card-foot">
                            <div class="course-progress">
                                <div class="meter">
                                    <span style="width:${item.passRate}%;"></span>
                                </div>
                                <div class="tiny">${item.gradeCount} 条成绩记录</div>
                            </div>
                            <div class="course-card-actions">
                                <a class="btn btn-ghost btn-sm" href="${ctx}/students?classId=${item.id}">
                                    学生
                                    <svg class="icon"><use href="#i-arrow-right"></use></svg>
                                </a>
                            </div>
                        </div>
                    </article>
                </c:forEach>
            </div>

            <c:if test="${empty classes}">
                <div class="card">
                    <div class="empty-state">
                        <div class="empty-icon"><svg class="icon"><use href="#i-graduation"></use></svg></div>
                        <h3>还没有班级</h3>
                        <p>创建第一个班级后，就可以添加学生并录入成绩。</p>
                        <button type="button" class="btn btn-primary btn-sm" data-class-add>
                            <svg class="icon"><use href="#i-plus"></use></svg> 新增班级
                        </button>
                    </div>
                </div>
            </c:if>

            <div class="hint-strip" style="margin-top:18px;">
                <svg class="icon"><use href="#i-info"></use></svg>
                提示：已删除学生的班级才会显示删除按钮；有学生的班级需要先在学生管理中调整班级。
            </div>
        </main>
        <%@ include file="common/footer.jspf" %>

<div class="modal-backdrop" id="classModal" aria-hidden="true">
    <div class="modal" role="dialog" aria-modal="true" aria-labelledby="classModalTitle">
        <div class="modal-head">
            <div class="modal-head-icon" style="color:#0e9384;background:#e6f7f4;">
                <svg class="icon"><use href="#i-graduation"></use></svg>
            </div>
            <div>
                <h2 id="classModalTitle">新增班级</h2>
                <p id="classModalSubtitle">建立班级后，就可以把学生归入这个班级</p>
            </div>
            <button type="button" class="modal-close" data-close-modal aria-label="关闭">
                <svg class="icon"><use href="#i-x"></use></svg>
            </button>
        </div>
        <form id="classForm" method="post" action="${ctx}/classes">
            <div class="modal-body">
                <input type="hidden" name="action" value="save">
                <input type="hidden" name="id" value="">
                <div class="form-grid">
                    <div class="field span-2">
                        <label class="field-label" for="className">班级名称 <span class="req">*</span></label>
                        <input class="input" id="className" name="name" type="text" maxlength="100" required
                               placeholder="例如：计算机科学与技术 2301班">
                    </div>
                    <div class="field">
                        <label class="field-label" for="classGradeYear">年级 <span class="req">*</span></label>
                        <input class="input" id="classGradeYear" name="gradeYear" type="text" maxlength="20" required
                               placeholder="例如：2023">
                    </div>
                    <div class="field">
                        <label class="field-label" for="classMajor">专业</label>
                        <input class="input" id="classMajor" name="major" type="text" maxlength="100"
                               placeholder="例如：计算机科学与技术">
                    </div>
                    <div class="field span-2">
                        <label class="field-label" for="classAdvisor">班主任</label>
                        <input class="input" id="classAdvisor" name="advisor" type="text" maxlength="50"
                               placeholder="例如：周明远">
                    </div>
                </div>
            </div>
            <div class="modal-foot">
                <button type="button" class="btn btn-ghost" data-close-modal>取消</button>
                <button type="submit" class="btn btn-primary">
                    <svg class="icon"><use href="#i-save"></use></svg>
                    保存班级
                </button>
            </div>
        </form>
    </div>
</div>
</body>
</html>
