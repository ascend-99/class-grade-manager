<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="课程管理"/>
<c:set var="pageEyebrow" value="CURRICULUM"/>
<c:set var="activeNav" value="courses"/>
<%@ include file="common/head.jspf" %>

<div class="app-shell">
    <%@ include file="common/sidebar.jspf" %>
    <div class="main-shell">
        <%@ include file="common/topbar.jspf" %>
        <main class="page-content">
            <%@ include file="common/flash.jspf" %>

            <div class="page-toolbar">
                <div class="page-toolbar-copy">
                    <h2>课程设置</h2>
                    <p>维护本学期课程信息，课程成绩、均分与及格率会实时汇总。</p>
                </div>
                <div class="page-toolbar-actions">
                    <button type="button" class="btn btn-primary" data-course-add>
                        <svg class="icon"><use href="#i-plus"></use></svg>
                        新增课程
                    </button>
                </div>
            </div>

            <form class="filter-card" method="get" action="${ctx}/courses">
                <div class="field select-wide">
                    <label class="field-label" for="courseClass">统计班级</label>
                    <select class="select" id="courseClass" name="classId" data-auto-submit>
                        <option value="0">全部班级</option>
                        <c:forEach var="item" items="${classes}">
                            <option value="${item.id}" ${item.id == classId ? 'selected' : ''}>
                                <c:out value="${item.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="filter-actions">
                    <button type="submit" class="btn btn-ghost">
                        <svg class="icon"><use href="#i-filter"></use></svg>
                        应用筛选
                    </button>
                </div>
                <div class="filter-divider"></div>
                <span class="result-count">共 <strong>${fn:length(courses)}</strong> 门课程</span>
            </form>

            <div class="course-grid">
                <c:forEach var="course" items="${courses}">
                    <article class="course-card ${course.color}">
                        <div class="course-card-top">
                            <span class="course-code ${course.color}">${fn:escapeXml(course.code)}</span>
                            <div class="course-card-actions">
                                <button type="button" class="row-action" title="编辑课程"
                                        data-course-edit
                                        data-id="${course.id}"
                                        data-code="${fn:escapeXml(course.code)}"
                                        data-name="${fn:escapeXml(course.name)}"
                                        data-credit="${course.credit}"
                                        data-teacher="${fn:escapeXml(course.teacher)}"
                                        data-semester="${fn:escapeXml(course.semester)}"
                                        data-color="${course.color}">
                                    <svg class="icon"><use href="#i-pencil"></use></svg>
                                </button>
                                <c:if test="${course.gradeCount == 0}">
                                    <form method="post" action="${ctx}/courses" style="display:inline;"
                                          data-confirm="删除课程后无法恢复，确定要删除「${fn:escapeXml(course.name)}」吗？"
                                          data-confirm-title="删除这门课程？">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${course.id}">
                                        <input type="hidden" name="returnTo" value="/courses?classId=${classId}">
                                        <button type="submit" class="row-action danger" title="删除课程">
                                            <svg class="icon"><use href="#i-trash"></use></svg>
                                        </button>
                                    </form>
                                </c:if>
                            </div>
                        </div>
                        <h3><c:out value="${course.name}"/></h3>
                        <div class="course-meta">
                            <span><svg class="icon"><use href="#i-user"></use></svg> ${fn:escapeXml(course.teacher)}</span>
                            <span><svg class="icon"><use href="#i-award"></use></svg> ${course.credit} 学分</span>
                            <span><svg class="icon"><use href="#i-calendar"></use></svg> ${fn:escapeXml(course.semester)}</span>
                        </div>
                        <div class="course-stats">
                            <div class="course-stat">
                                <span>平均分</span>
                                <strong><fmt:formatNumber value="${course.averageScore}" pattern="0.0"/><small>分</small></strong>
                            </div>
                            <div class="course-stat">
                                <span>及格率</span>
                                <strong><fmt:formatNumber value="${course.passRate}" pattern="0.0"/><small>%</small></strong>
                            </div>
                        </div>
                        <div class="course-card-foot">
                            <div class="course-progress">
                                <div class="meter">
                                    <span class="${course.color}" style="width:${course.passRate}%;"></span>
                                </div>
                                <div class="tiny">${course.gradeCount} 条成绩 · ${course.studentCount} 名学生</div>
                            </div>
                            <a class="btn btn-ghost btn-sm" href="${ctx}/grades?courseId=${course.id}<c:if test="${classId > 0}">&amp;classId=${classId}</c:if>">
                                查看成绩
                                <svg class="icon"><use href="#i-arrow-right"></use></svg>
                            </a>
                        </div>
                    </article>
                </c:forEach>
            </div>

            <c:if test="${empty courses}">
                <div class="card">
                    <div class="empty-state">
                        <div class="empty-icon"><svg class="icon"><use href="#i-book"></use></svg></div>
                        <h3>还没有课程</h3>
                        <p>创建第一门课程，然后到成绩录入中填写班级成绩。</p>
                        <button type="button" class="btn btn-primary btn-sm" data-course-add>
                            <svg class="icon"><use href="#i-plus"></use></svg> 新增课程
                        </button>
                    </div>
                </div>
            </c:if>
        </main>
        <%@ include file="common/footer.jspf" %>

<div class="modal-backdrop" id="courseModal" aria-hidden="true">
    <div class="modal" role="dialog" aria-modal="true" aria-labelledby="courseModalTitle">
        <div class="modal-head">
            <div class="modal-head-icon" style="color:#0e9384;background:#e6f7f4;"><svg class="icon"><use href="#i-book"></use></svg></div>
            <div>
                <h2 id="courseModalTitle">新增课程</h2>
                <p id="courseModalSubtitle">课程建立后，即可在成绩录入中选择</p>
            </div>
            <button type="button" class="modal-close" data-close-modal aria-label="关闭">
                <svg class="icon"><use href="#i-x"></use></svg>
            </button>
        </div>
        <form id="courseForm" method="post" action="${ctx}/courses">
            <div class="modal-body">
                <input type="hidden" name="action" value="save">
                <input type="hidden" name="id" value="">
                <input type="hidden" name="returnTo" value="/courses?classId=${classId}">
                <div class="form-grid">
                    <div class="field">
                        <label class="field-label" for="courseCode">课程代码 <span class="req">*</span></label>
                        <input class="input" id="courseCode" name="code" type="text" maxlength="20" required placeholder="例如：JAVAEE">
                    </div>
                    <div class="field">
                        <label class="field-label" for="courseName">课程名称 <span class="req">*</span></label>
                        <input class="input" id="courseName" name="name" type="text" maxlength="100" required placeholder="例如：JavaEE 企业级开发">
                    </div>
                    <div class="field">
                        <label class="field-label" for="courseCredit">学分 <span class="req">*</span></label>
                        <input class="input" id="courseCredit" name="credit" type="number" min="0.1" max="20" step="0.1" required placeholder="3">
                    </div>
                    <div class="field">
                        <label class="field-label" for="courseTeacher">授课教师 <span class="req">*</span></label>
                        <input class="input" id="courseTeacher" name="teacher" type="text" maxlength="50" required placeholder="教师姓名">
                    </div>
                    <div class="field span-2">
                        <label class="field-label" for="courseSemester">开课学期 <span class="req">*</span></label>
                        <input class="input" id="courseSemester" name="semester" type="text" maxlength="50" required value="2026-2027 秋">
                    </div>
                    <div class="field span-2">
                        <label class="field-label">课程色彩</label>
                        <div class="color-options">
                            <label class="color-option color-indigo">
                                <input type="radio" name="color" value="indigo" checked>
                                <span></span>靛蓝
                            </label>
                            <label class="color-option color-teal">
                                <input type="radio" name="color" value="teal">
                                <span></span>青绿
                            </label>
                            <label class="color-option color-amber">
                                <input type="radio" name="color" value="amber">
                                <span></span>琥珀
                            </label>
                            <label class="color-option color-rose">
                                <input type="radio" name="color" value="rose">
                                <span></span>绯红
                            </label>
                            <label class="color-option color-sky">
                                <input type="radio" name="color" value="sky">
                                <span></span>天蓝
                            </label>
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-foot">
                <button type="button" class="btn btn-ghost" data-close-modal>取消</button>
                <button type="submit" class="btn btn-primary">
                    <svg class="icon"><use href="#i-save"></use></svg>
                    保存课程
                </button>
            </div>
        </form>
    </div>
</div>
</body>
</html>
