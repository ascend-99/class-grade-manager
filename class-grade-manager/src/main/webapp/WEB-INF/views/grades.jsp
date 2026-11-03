<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="成绩管理"/>
<c:set var="pageEyebrow" value="GRADE BOOK"/>
<c:set var="activeNav" value="grades"/>
<%@ include file="common/head.jspf" %>
<c:url var="returnTo" value="/grades">
    <c:param name="classId" value="${classId}"/>
    <c:param name="courseId" value="${courseId}"/>
    <c:param name="examName" value="${examName}"/>
    <c:param name="q" value="${keyword}"/>
    <c:param name="page" value="${data.page}"/>
</c:url>
<c:url var="exportUrl" value="/export">
    <c:param name="type" value="grades"/>
    <c:param name="classId" value="${classId}"/>
    <c:param name="courseId" value="${courseId}"/>
    <c:param name="examName" value="${examName}"/>
    <c:param name="q" value="${keyword}"/>
</c:url>

<c:url var="exportExcelUrl" value="/export">
    <c:param name="type" value="grades"/>
    <c:param name="format" value="xlsx"/>
    <c:param name="classId" value="${classId}"/>
    <c:param name="courseId" value="${courseId}"/>
    <c:param name="examName" value="${examName}"/>
    <c:param name="q" value="${keyword}"/>
</c:url>

<div class="app-shell">
    <%@ include file="common/sidebar.jspf" %>
    <div class="main-shell">
        <%@ include file="common/topbar.jspf" %>
        <main class="page-content">
            <%@ include file="common/flash.jspf" %>

            <div class="page-toolbar">
                <div class="page-toolbar-copy">
                    <h2>成绩台账</h2>
                    <p>按班级、课程、考试自由筛选，支持单条修改、删除与导出。</p>
                </div>
                <div class="page-toolbar-actions">
                    <a class="btn btn-ghost" href="${exportExcelUrl}">
                        <svg class="icon"><use href="#i-download"></use></svg>
                        导出 Excel
                    </a>
                    <a class="btn btn-ghost" href="${exportUrl}">CSV</a>
                    <a class="btn btn-ghost" href="${ctx}/grade-history">
                        <svg class="icon"><use href="#i-clock"></use></svg>
                        修改记录
                    </a>
                    <a class="btn btn-primary" href="${ctx}/entry<c:if test="${classId > 0}">?classId=${classId}</c:if>">
                        <svg class="icon"><use href="#i-edit"></use></svg>
                        录入成绩
                    </a>
                </div>
            </div>

            <form class="filter-card" method="get" action="${ctx}/grades">
                <div class="field search-field">
                    <label class="field-label" for="gradeSearch">搜索</label>
                    <div class="input-icon">
                        <svg class="icon"><use href="#i-search"></use></svg>
                        <input class="input" id="gradeSearch" name="q" type="search"
                               value="${fn:escapeXml(keyword)}" placeholder="学生、课程或考试">
                    </div>
                </div>
                <div class="field select-wide">
                    <label class="field-label" for="gradeClass">班级</label>
                    <select class="select" id="gradeClass" name="classId">
                        <option value="0">全部班级</option>
                        <c:forEach var="item" items="${classes}">
                            <option value="${item.id}" ${item.id == classId ? 'selected' : ''}><c:out value="${item.name}"/></option>
                        </c:forEach>
                    </select>
                </div>
                <div class="field">
                    <label class="field-label" for="gradeCourse">课程</label>
                    <select class="select" id="gradeCourse" name="courseId">
                        <option value="0">全部课程</option>
                        <c:forEach var="item" items="${courses}">
                            <option value="${item.id}" ${item.id == courseId ? 'selected' : ''}><c:out value="${item.name}"/></option>
                        </c:forEach>
                    </select>
                </div>
                <div class="field">
                    <label class="field-label" for="gradeExam">考试</label>
                    <select class="select" id="gradeExam" name="examName">
                        <option value="">全部考试</option>
                        <c:forEach var="exam" items="${exams}">
                            <option value="${fn:escapeXml(exam)}" ${exam == examName ? 'selected' : ''}>${fn:escapeXml(exam)}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="filter-actions">
                    <button type="submit" class="btn btn-primary">
                        <svg class="icon"><use href="#i-filter"></use></svg>
                        筛选
                    </button>
                    <a class="btn btn-ghost" href="${ctx}/grades">重置</a>
                </div>
            </form>

            <div class="table-card">
                <div class="table-head">
                    <div>
                        <h3>成绩明细</h3>
                        <p>共 ${data.total} 条记录 · 按最近更新时间排序</p>
                    </div>
                    <span class="result-count">第 <strong>${data.page}</strong> / ${data.totalPages} 页</span>
                </div>
                <div class="table-wrap">
                    <table class="data-table">
                        <thead>
                        <tr>
                            <th>学生</th>
                            <th class="hide-tablet">班级</th>
                            <th>课程</th>
                            <th class="hide-mobile">考试</th>
                            <th>成绩</th>
                            <th>等级</th>
                            <th class="hide-mobile">更新时间</th>
                            <th style="text-align:right;">操作</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="row" items="${data.items}">
                            <tr>
                                <td>
                                    <div class="user-cell">
                                        <span class="avatar sm avatar-${row.studentId % 5}">${fn:escapeXml(row.initial)}</span>
                                        <div class="user-cell-copy">
                                            <strong><c:out value="${row.studentName}"/></strong>
                                            <span>${row.studentNo}</span>
                                        </div>
                                    </div>
                                </td>
                                <td class="hide-tablet"><c:out value="${row.className}"/></td>
                                <td>
                                    <span class="course-tag">
                                        <span class="course-dot ${row.courseColor}"></span>
                                        <c:out value="${row.courseName}"/>
                                    </span>
                                </td>
                                <td class="hide-mobile"><span class="badge badge-muted no-dot">${fn:escapeXml(row.examName)}</span></td>
                                <td><span class="score-chip score-${row.levelTone}"><fmt:formatNumber value="${row.score}" pattern="0.0"/></span></td>
                                <td><span class="level-text level-${row.levelTone}">${row.level}</span></td>
                                <td class="hide-mobile cell-muted">
                                    <fmt:formatDate value="${row.updatedAt}" pattern="MM-dd HH:mm"/>
                                    <br>${fn:escapeXml(row.recordedBy)}
                                </td>
                                <td>
                                    <div class="row-actions">
                                        <button type="button" class="row-action" title="修改成绩"
                                                data-grade-edit
                                                data-id="${row.id}"
                                                data-score="${row.score}"
                                                data-student="${fn:escapeXml(row.studentName)}"
                                                data-course="${fn:escapeXml(row.courseName)}"
                                                data-exam="${fn:escapeXml(row.examName)}">
                                            <svg class="icon"><use href="#i-pencil"></use></svg>
                                        </button>
                                        <c:url var="historyUrl" value="/grade-history">
                                            <c:param name="studentId" value="${row.studentId}"/>
                                            <c:param name="courseId" value="${row.courseId}"/>
                                        </c:url>
                                        <a class="row-action" href="${historyUrl}" title="查看该学生这门课的修改记录">
                                            <svg class="icon"><use href="#i-clock"></use></svg>
                                        </a>
                                        <form method="post" action="${ctx}/grades" style="display:inline;"
                                              data-confirm="确定删除「${fn:escapeXml(row.studentName)} · ${fn:escapeXml(row.courseName)} · ${fn:escapeXml(row.examName)}」的成绩吗？"
                                              data-confirm-title="删除这条成绩？">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="${row.id}">
                                            <input type="hidden" name="returnTo" value="${fn:escapeXml(returnTo)}">
                                            <button type="submit" class="row-action danger" title="删除成绩">
                                                <svg class="icon"><use href="#i-trash"></use></svg>
                                            </button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>

                <c:if test="${empty data.items}">
                    <div class="empty-state">
                        <div class="empty-icon"><svg class="icon"><use href="#i-list-check"></use></svg></div>
                        <h3>没有匹配的成绩</h3>
                        <p>调整筛选条件，或者去录入一批新的成绩。</p>
                        <a class="btn btn-primary btn-sm" href="${ctx}/entry">去录入</a>
                    </div>
                </c:if>

                <c:if test="${data.total > 0}">
                    <c:set var="startPage" value="${data.page - 2}"/>
                    <c:if test="${startPage < 1}"><c:set var="startPage" value="1"/></c:if>
                    <c:set var="endPage" value="${startPage + 4}"/>
                    <c:if test="${endPage > data.totalPages}">
                        <c:set var="endPage" value="${data.totalPages}"/>
                        <c:set var="startPage" value="${endPage - 4}"/>
                    </c:if>
                    <c:if test="${startPage < 1}"><c:set var="startPage" value="1"/></c:if>
                    <div class="pagination">
                        <span class="pagination-info">
                            显示 <strong>${data.from}</strong>–<strong>${data.to}</strong>，共 <strong>${data.total}</strong> 条
                        </span>
                        <div class="pagination-btns">
                            <c:url var="prevUrl" value="/grades">
                                <c:param name="classId" value="${classId}"/>
                                <c:param name="courseId" value="${courseId}"/>
                                <c:param name="examName" value="${examName}"/>
                                <c:param name="q" value="${keyword}"/>
                                <c:param name="page" value="${data.prevPage}"/>
                            </c:url>
                            <a class="page-btn ${data.hasPrev ? '' : 'disabled'}" href="${prevUrl}" aria-label="上一页">
                                <svg class="icon"><use href="#i-chevron-left"></use></svg>
                            </a>
                            <c:if test="${startPage > 1}">
                                <c:url var="firstUrl" value="/grades">
                                    <c:param name="classId" value="${classId}"/>
                                    <c:param name="courseId" value="${courseId}"/>
                                    <c:param name="examName" value="${examName}"/>
                                    <c:param name="q" value="${keyword}"/>
                                    <c:param name="page" value="1"/>
                                </c:url>
                                <a class="page-btn" href="${firstUrl}">1</a>
                                <c:if test="${startPage > 2}"><span class="page-ellipsis">…</span></c:if>
                            </c:if>
                            <c:forEach var="p" begin="${startPage}" end="${endPage}">
                                <c:url var="pageUrl" value="/grades">
                                    <c:param name="classId" value="${classId}"/>
                                    <c:param name="courseId" value="${courseId}"/>
                                    <c:param name="examName" value="${examName}"/>
                                    <c:param name="q" value="${keyword}"/>
                                    <c:param name="page" value="${p}"/>
                                </c:url>
                                <a class="page-btn ${p == data.page ? 'active' : ''}" href="${pageUrl}">${p}</a>
                            </c:forEach>
                            <c:if test="${endPage < data.totalPages}">
                                <c:if test="${endPage < data.totalPages - 1}"><span class="page-ellipsis">…</span></c:if>
                                <c:url var="lastUrl" value="/grades">
                                    <c:param name="classId" value="${classId}"/>
                                    <c:param name="courseId" value="${courseId}"/>
                                    <c:param name="examName" value="${examName}"/>
                                    <c:param name="q" value="${keyword}"/>
                                    <c:param name="page" value="${data.totalPages}"/>
                                </c:url>
                                <a class="page-btn" href="${lastUrl}">${data.totalPages}</a>
                            </c:if>
                            <c:url var="nextUrl" value="/grades">
                                <c:param name="classId" value="${classId}"/>
                                <c:param name="courseId" value="${courseId}"/>
                                <c:param name="examName" value="${examName}"/>
                                <c:param name="q" value="${keyword}"/>
                                <c:param name="page" value="${data.nextPage}"/>
                            </c:url>
                            <a class="page-btn ${data.hasNext ? '' : 'disabled'}" href="${nextUrl}" aria-label="下一页">
                                <svg class="icon"><use href="#i-chevron-right"></use></svg>
                            </a>
                        </div>
                    </div>
                </c:if>
            </div>
        </main>
        <%@ include file="common/footer.jspf" %>

<div class="modal-backdrop" id="gradeModal" aria-hidden="true">
    <div class="modal" style="width:min(460px, 100%);" role="dialog" aria-modal="true" aria-labelledby="gradeModalTitle">
        <div class="modal-head">
            <div class="modal-head-icon" style="color:#d97706;background:#fff8e6;"><svg class="icon"><use href="#i-pencil"></use></svg></div>
            <div>
                <h2 id="gradeModalTitle">修改成绩</h2>
                <p id="gradeMeta">课程 · 考试</p>
            </div>
            <button type="button" class="modal-close" data-close-modal aria-label="关闭">
                <svg class="icon"><use href="#i-x"></use></svg>
            </button>
        </div>
        <form id="gradeForm" method="post" action="${ctx}/grades">
            <div class="modal-body">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="id" value="">
                <input type="hidden" name="returnTo" value="${fn:escapeXml(returnTo)}">
                <div class="field" style="margin-bottom:16px;">
                    <span class="field-label">学生</span>
                    <div style="display:flex;align-items:center;gap:8px;padding:11px 13px;border:1px solid var(--line);border-radius:10px;background:#fafbfd;">
                        <svg class="icon" style="width:15px;height:15px;color:#818cf8;"><use href="#i-user"></use></svg>
                        <strong id="gradeStudent" style="font-size:12px;">—</strong>
                        <span style="margin-left:auto;color:#94a3b8;font-size:10px;">当前 <span id="gradeCurrent">—</span> 分</span>
                    </div>
                </div>
                <div class="field">
                    <label class="field-label" for="gradeScore">新的成绩 <span class="req">*</span></label>
                    <input class="input" id="gradeScore" name="score" type="number" min="0" max="100" step="0.1"
                           required placeholder="0 - 100" style="height:52px;font-size:19px;font-weight:650;text-align:center;">
                    <span class="field-hint">支持一位小数，录入后立即更新平均分与及格率。</span>
                </div>
                <div class="field" style="margin-top:14px;">
                    <label class="field-label" for="gradeReason">修改原因</label>
                    <input class="input" id="gradeReason" name="reason" type="text" maxlength="300"
                           placeholder="例如：期中卷面复核 / 补录平时分">
                    <span class="field-hint">会记录在「修改记录」中，方便后续追溯。</span>
                </div>
            </div>
            <div class="modal-foot">
                <button type="button" class="btn btn-ghost" data-close-modal>取消</button>
                <button type="submit" class="btn btn-primary">
                    <svg class="icon"><use href="#i-save"></use></svg>
                    保存修改
                </button>
            </div>
        </form>
    </div>
</div>
</body>
</html>
