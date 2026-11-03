<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="成绩录入"/>
<c:set var="pageEyebrow" value="SCORE ENTRY"/>
<c:set var="activeNav" value="entry"/>
<c:set var="useCharts" value="false"/>
<%@ include file="common/head.jspf" %>

<div class="app-shell">
    <%@ include file="common/sidebar.jspf" %>
    <div class="main-shell">
        <%@ include file="common/topbar.jspf" %>
        <main class="page-content">
            <%@ include file="common/flash.jspf" %>

            <section class="entry-hero">
                <div class="entry-hero-copy">
                    <span class="eyebrow">BATCH ENTRY</span>
                    <h2>一次录入，整班同步</h2>
                    <p>选择班级、课程与考试，支持键盘 Enter 连续录入，保存后统计自动刷新。</p>
                </div>
                <form class="entry-selector" method="get" action="${ctx}/entry" id="entrySelector">
                    <div class="field">
                        <label class="field-label" for="entryClass">班级</label>
                        <select class="select" id="entryClass" name="classId">
                            <c:forEach var="item" items="${classes}">
                                <option value="${item.id}" ${item.id == selectedClass.id ? 'selected' : ''}>
                                    <c:out value="${item.name}"/>
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="field">
                        <label class="field-label" for="entryCourse">课程</label>
                        <select class="select" id="entryCourse" name="courseId">
                            <c:forEach var="item" items="${courses}">
                                <option value="${item.id}" ${item.id == selectedCourse.id ? 'selected' : ''}>
                                    <c:out value="${item.name}"/>
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="field">
                        <label class="field-label" for="entryExam">考试</label>
                        <select class="select" id="entryExam" name="examName">
                            <c:forEach var="item" items="${exams}">
                                <option value="${fn:escapeXml(item)}" ${item == examName ? 'selected' : ''}>${fn:escapeXml(item)}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <button type="submit" class="btn btn-dark" style="height:39px;">
                        <svg class="icon"><use href="#i-refresh"></use></svg>
                        切换名单
                    </button>
                </form>
            </section>

            <div class="entry-stats">
                <div class="entry-stat">
                    <span class="entry-stat-icon" style="color:#4f46e5;background:#eef0ff;"><svg class="icon"><use href="#i-users"></use></svg></span>
                    <div class="entry-stat-copy">
                        <span>班级人数</span>
                        <strong>${fn:length(rows)}<small>人</small></strong>
                    </div>
                </div>
                <div class="entry-stat">
                    <span class="entry-stat-icon" style="color:#0e9384;background:#e6f7f4;"><svg class="icon"><use href="#i-check-circle"></use></svg></span>
                    <div class="entry-stat-copy">
                        <span>已录入</span>
                        <strong>${recorded}<small>人</small></strong>
                    </div>
                </div>
                <div class="entry-stat">
                    <span class="entry-stat-icon" style="color:#d97706;background:#fff8e6;"><svg class="icon"><use href="#i-target"></use></svg></span>
                    <div class="entry-stat-copy">
                        <span>已录平均</span>
                        <strong><c:choose><c:when test="${recorded > 0}"><fmt:formatNumber value="${recordedAverage}" pattern="0.0"/></c:when><c:otherwise>—</c:otherwise></c:choose><small>分</small></strong>
                    </div>
                </div>
                <div class="entry-stat">
                    <span class="entry-stat-icon" style="color:#0284c7;background:#e8f4fd;"><svg class="icon"><use href="#i-edit"></use></svg></span>
                    <div class="entry-stat-copy">
                        <span>本次已填写</span>
                        <strong><span id="entryFilled">${recorded}</span><small>人</small></strong>
                    </div>
                </div>
            </div>

            <c:url var="templateUrl" value="/entry/template">
                <c:param name="classId" value="${selectedClass.id}"/>
                <c:param name="courseId" value="${selectedCourse.id}"/>
                <c:param name="examName" value="${examName}"/>
            </c:url>
            <div class="entry-tools">
                <div class="hint-strip">
                    <svg class="icon"><use href="#i-info"></use></svg>
                    支持 Excel 批量导入：先下载模板，填好成绩后上传，系统会自动校验学号与分数。
                </div>
                <div class="entry-tools-actions">
                    <a class="btn btn-ghost" href="${templateUrl}">
                        <svg class="icon"><use href="#i-download"></use></svg>
                        下载 Excel 模板
                    </a>
                    <button type="button" class="btn btn-soft" data-open-modal="importModal">
                        <svg class="icon"><use href="#i-upload"></use></svg>
                        Excel 导入
                    </button>
                </div>
            </div>

            <form id="entryForm" method="post" action="${ctx}/entry">
                <input type="hidden" name="classId" value="${selectedClass.id}">
                <input type="hidden" name="courseId" value="${selectedCourse.id}">
                <input type="hidden" name="examName" value="${fn:escapeXml(examName)}">

                <div class="entry-card">
                    <div class="entry-card-head">
                        <div>
                            <h3><c:out value="${selectedCourse.name}"/> · ${fn:escapeXml(examName)}</h3>
                            <p><c:out value="${selectedClass.name}"/> · 授课教师 ${fn:escapeXml(selectedCourse.teacher)} · ${selectedCourse.credit} 学分</p>
                        </div>
                        <div class="entry-legend">
                            <span><i class="recorded"></i> 已录入</span>
                            <span><i></i> 待录入</span>
                            <span style="color:#818cf8;">Enter 跳到下一位</span>
                        </div>
                    </div>
                    <div class="table-wrap">
                        <table class="data-table entry-table">
                            <thead>
                            <tr>
                                <th style="width:40px;">#</th>
                                <th>学生</th>
                                <th class="hide-mobile">学号</th>
                                <th class="hide-tablet">状态</th>
                                <th>成绩（0–100）</th>
                                <th class="hide-mobile">最近更新</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="row" items="${rows}" varStatus="loop">
                                <c:set var="scoreText" value=""/>
                                <c:if test="${not empty row.score}">
                                    <fmt:formatNumber value="${row.score}" pattern="0.#" var="scoreText"/>
                                </c:if>
                                <tr>
                                    <td class="entry-row-no">${loop.index + 1}</td>
                                    <td>
                                        <div class="user-cell">
                                            <span class="avatar sm avatar-${row.studentId % 5}">${fn:escapeXml(row.initial)}</span>
                                            <div class="user-cell-copy">
                                                <strong><c:out value="${row.studentName}"/></strong>
                                                <span>${row.gender}</span>
                                            </div>
                                        </div>
                                    </td>
                                    <td class="hide-mobile mono">${row.studentNo}</td>
                                    <td class="hide-tablet">
                                        <c:choose>
                                            <c:when test="${row.recorded}"><span class="badge badge-success">已录入</span></c:when>
                                            <c:otherwise><span class="badge badge-muted">待录入</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <input class="entry-score-input" type="number" inputmode="decimal"
                                               name="score_${row.studentId}" value="${scoreText}"
                                               min="0" max="100" step="0.1" placeholder="—"
                                               aria-label="${fn:escapeXml(row.studentName)}的成绩">
                                    </td>
                                    <td class="hide-mobile cell-muted">
                                        <c:choose>
                                            <c:when test="${not empty row.updatedAt}"><fmt:formatDate value="${row.updatedAt}" pattern="MM-dd HH:mm"/></c:when>
                                            <c:otherwise>—</c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>

                    <c:if test="${empty rows}">
                        <div class="empty-state">
                            <div class="empty-icon"><svg class="icon"><use href="#i-users"></use></svg></div>
                            <h3>这个班级还没有学生</h3>
                            <p>先在学生管理中添加成员，再回来录入成绩。</p>
                            <a class="btn btn-primary btn-sm" href="${ctx}/students">去添加学生</a>
                        </div>
                    </c:if>
                </div>

                <c:if test="${not empty rows}">
                    <div class="save-bar">
                        <div class="save-bar-info">
                            <svg class="icon" style="width:15px;height:15px;color:#94a3b8;"><use href="#i-info"></use></svg>
                            <span>已填写 <strong id="saveBarCount">${recorded}</strong> / ${fn:length(rows)} 人</span>
                            <span class="dot"></span>
                            <span>本次均分 <strong id="entryAverage">—</strong></span>
                            <span class="dot"></span>
                            <span>本次及格率 <strong id="entryPassRate">—</strong></span>
                        </div>
                        <div class="save-bar-actions">
                            <button type="button" class="btn btn-ghost" id="resetScores">
                                <svg class="icon"><use href="#i-refresh"></use></svg>
                                还原
                            </button>
                            <button type="submit" class="btn btn-primary">
                                <svg class="icon"><use href="#i-save"></use></svg>
                                保存本页成绩
                            </button>
                        </div>
                    </div>
                </c:if>
            </form>
        </main>
        <%@ include file="common/footer.jspf" %>

<div class="modal-backdrop" id="importModal" aria-hidden="true">
    <div class="modal" role="dialog" aria-modal="true" aria-labelledby="importModalTitle">
        <div class="modal-head">
            <div class="modal-head-icon" style="color:#0e9384;background:#e6f7f4;">
                <svg class="icon"><use href="#i-upload"></use></svg>
            </div>
            <div>
                <h2 id="importModalTitle">Excel 批量导入</h2>
                <p><c:out value="${selectedClass.name}"/> · <c:out value="${selectedCourse.name}"/> · ${fn:escapeXml(examName)}</p>
            </div>
            <button type="button" class="modal-close" data-close-modal aria-label="关闭">
                <svg class="icon"><use href="#i-x"></use></svg>
            </button>
        </div>
        <form method="post" action="${ctx}/entry/import" enctype="multipart/form-data">
            <div class="modal-body">
                <input type="hidden" name="classId" value="${selectedClass.id}">
                <input type="hidden" name="courseId" value="${selectedCourse.id}">
                <input type="hidden" name="examName" value="${fn:escapeXml(examName)}">
                <div class="import-steps">
                    <div class="import-step"><span>1</span><div><strong>下载模板</strong><p>模板已包含本班学生名单和已录成绩。</p></div></div>
                    <div class="import-step"><span>2</span><div><strong>填写成绩</strong><p>只修改「成绩」列，0–100 分，可留空。</p></div></div>
                    <div class="import-step"><span>3</span><div><strong>上传文件</strong><p>系统会校验学号、分数，并自动新增或更新。</p></div></div>
                </div>
                <label class="file-drop" for="importFile">
                    <svg class="icon"><use href="#i-upload"></use></svg>
                    <strong>选择 Excel 文件</strong>
                    <span>支持 .xlsx / .xls，最大 5MB</span>
                    <input id="importFile" name="file" type="file" accept=".xlsx,.xls" required>
                </label>
                <div class="hint-strip" style="margin-top:14px;">
                    <svg class="icon"><use href="#i-info"></use></svg>
                    如果没有模板，请先关闭弹窗并点击「下载 Excel 模板」。不要修改学号和姓名列。
                </div>
            </div>
            <div class="modal-foot">
                <a class="btn btn-ghost" href="${templateUrl}">
                    <svg class="icon"><use href="#i-download"></use></svg>
                    下载模板
                </a>
                <button type="button" class="btn btn-ghost" data-close-modal>取消</button>
                <button type="submit" class="btn btn-primary">
                    <svg class="icon"><use href="#i-upload"></use></svg>
                    开始导入
                </button>
            </div>
        </form>
    </div>
</div>
</body>
</html>
