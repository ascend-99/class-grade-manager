<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="修改记录"/>
<c:set var="pageEyebrow" value="AUDIT TRAIL"/>
<c:set var="activeNav" value="history"/>
<%@ include file="common/head.jspf" %>

<div class="app-shell">
    <%@ include file="common/sidebar.jspf" %>
    <div class="main-shell">
        <%@ include file="common/topbar.jspf" %>
        <main class="page-content">
            <%@ include file="common/flash.jspf" %>

            <div class="page-toolbar">
                <div class="page-toolbar-copy">
                    <h2>成绩变更轨迹</h2>
                    <p>每一次录入、修改和删除都会留下记录，方便追溯与复核。</p>
                </div>
                <div class="page-toolbar-actions">
                    <a class="btn btn-ghost" href="${ctx}/grades">
                        <svg class="icon"><use href="#i-list-check"></use></svg>
                        返回成绩管理
                    </a>
                </div>
            </div>

            <form class="filter-card" method="get" action="${ctx}/grade-history">
                <div class="field search-field">
                    <label class="field-label" for="historySearch">搜索</label>
                    <div class="input-icon">
                        <svg class="icon"><use href="#i-search"></use></svg>
                        <input class="input" id="historySearch" name="q" type="search"
                               value="${fn:escapeXml(keyword)}" placeholder="学生、课程、考试或原因">
                    </div>
                </div>
                <div class="field select-wide">
                    <label class="field-label" for="historyClass">班级</label>
                    <select class="select" id="historyClass" name="classId">
                        <option value="0">全部班级</option>
                        <c:forEach var="item" items="${classes}">
                            <option value="${item.id}" ${item.id == classId ? 'selected' : ''}><c:out value="${item.name}"/></option>
                        </c:forEach>
                    </select>
                </div>
                <div class="field">
                    <label class="field-label" for="historyCourse">课程</label>
                    <select class="select" id="historyCourse" name="courseId">
                        <option value="0">全部课程</option>
                        <c:forEach var="item" items="${courses}">
                            <option value="${item.id}" ${item.id == courseId ? 'selected' : ''}><c:out value="${item.name}"/></option>
                        </c:forEach>
                    </select>
                </div>
                <div class="field">
                    <label class="field-label" for="historyAction">类型</label>
                    <select class="select" id="historyAction" name="action">
                        <option value="">全部类型</option>
                        <option value="CREATE" ${actionType == 'CREATE' ? 'selected' : ''}>录入</option>
                        <option value="UPDATE" ${actionType == 'UPDATE' ? 'selected' : ''}>修改</option>
                        <option value="DELETE" ${actionType == 'DELETE' ? 'selected' : ''}>删除</option>
                    </select>
                </div>
                <div class="filter-actions">
                    <button type="submit" class="btn btn-primary">
                        <svg class="icon"><use href="#i-filter"></use></svg>
                        筛选
                    </button>
                    <a class="btn btn-ghost" href="${ctx}/grade-history">重置</a>
                </div>
            </form>

            <div class="table-card">
                <div class="table-head">
                    <div>
                        <h3>变更明细</h3>
                        <p>共 ${data.total} 条记录 · 累计 ${totalLogs} 条审计日志</p>
                    </div>
                    <span class="badge badge-indigo no-dot">AUDIT</span>
                </div>
                <div class="table-wrap">
                    <table class="data-table">
                        <thead>
                        <tr>
                            <th>时间</th>
                            <th>学生</th>
                            <th>课程</th>
                            <th class="hide-mobile">考试</th>
                            <th>变更</th>
                            <th>类型</th>
                            <th class="hide-mobile">操作教师</th>
                            <th class="hide-mobile">原因</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="log" items="${data.items}">
                            <tr>
                                <td class="cell-muted">
                                    <fmt:formatDate value="${log.changedAt}" pattern="yyyy-MM-dd HH:mm"/>
                                </td>
                                <td>
                                    <div class="user-cell">
                                        <span class="avatar sm avatar-${log.studentId % 5}">${fn:escapeXml(log.initial)}</span>
                                        <div class="user-cell-copy">
                                            <strong><c:out value="${log.studentName}"/></strong>
                                            <span>${log.studentNo}</span>
                                        </div>
                                    </div>
                                </td>
                                <td><c:out value="${log.courseName}"/></td>
                                <td class="hide-mobile"><span class="badge badge-muted no-dot">${fn:escapeXml(log.examName)}</span></td>
                                <td>
                                    <div style="display:flex;align-items:center;gap:6px;">
                                        <c:choose>
                                            <c:when test="${not empty log.oldScore}">
                                                <span class="score-chip score-none"><fmt:formatNumber value="${log.oldScore}" pattern="0.0"/></span>
                                            </c:when>
                                            <c:otherwise><span class="cell-muted">—</span></c:otherwise>
                                        </c:choose>
                                        <svg class="icon" style="width:13px;height:13px;color:#cbd5e1;"><use href="#i-arrow-right"></use></svg>
                                        <c:choose>
                                            <c:when test="${not empty log.newScore}">
                                                <span class="score-chip ${log.newScore >= 90 ? 'score-excellent' : log.newScore >= 60 ? 'score-good' : 'score-fail'}">
                                                    <fmt:formatNumber value="${log.newScore}" pattern="0.0"/>
                                                </span>
                                            </c:when>
                                            <c:otherwise><span class="cell-muted">—</span></c:otherwise>
                                        </c:choose>
                                    </div>
                                </td>
                                <td><span class="badge badge-${log.actionTone} no-dot">${log.actionLabel}</span></td>
                                <td class="hide-mobile"><c:out value="${log.changedBy}"/></td>
                                <td class="hide-mobile cell-muted"><c:out value="${log.reason}"/></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>

                <c:if test="${empty data.items}">
                    <div class="empty-state">
                        <div class="empty-icon"><svg class="icon"><use href="#i-clock"></use></svg></div>
                        <h3>还没有变更记录</h3>
                        <p>在成绩管理中修改一条成绩，这里就会出现可追溯的记录。</p>
                        <a class="btn btn-primary btn-sm" href="${ctx}/grades">去成绩管理</a>
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
                        <span class="pagination-info">显示 <strong>${data.from}</strong>–<strong>${data.to}</strong>，共 <strong>${data.total}</strong> 条</span>
                        <div class="pagination-btns">
                            <c:url var="prevUrl" value="/grade-history">
                                <c:param name="classId" value="${classId}"/><c:param name="courseId" value="${courseId}"/>
                                <c:param name="studentId" value="${studentId}"/><c:param name="action" value="${actionType}"/>
                                <c:param name="q" value="${keyword}"/><c:param name="page" value="${data.prevPage}"/>
                            </c:url>
                            <a class="page-btn ${data.hasPrev ? '' : 'disabled'}" href="${prevUrl}" aria-label="上一页">
                                <svg class="icon"><use href="#i-chevron-left"></use></svg>
                            </a>
                            <c:forEach var="p" begin="${startPage}" end="${endPage}">
                                <c:url var="pageUrl" value="/grade-history">
                                    <c:param name="classId" value="${classId}"/><c:param name="courseId" value="${courseId}"/>
                                    <c:param name="studentId" value="${studentId}"/><c:param name="action" value="${actionType}"/>
                                    <c:param name="q" value="${keyword}"/><c:param name="page" value="${p}"/>
                                </c:url>
                                <a class="page-btn ${p == data.page ? 'active' : ''}" href="${pageUrl}">${p}</a>
                            </c:forEach>
                            <c:url var="nextUrl" value="/grade-history">
                                <c:param name="classId" value="${classId}"/><c:param name="courseId" value="${courseId}"/>
                                <c:param name="studentId" value="${studentId}"/><c:param name="action" value="${actionType}"/>
                                <c:param name="q" value="${keyword}"/><c:param name="page" value="${data.nextPage}"/>
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
</body>
</html>
