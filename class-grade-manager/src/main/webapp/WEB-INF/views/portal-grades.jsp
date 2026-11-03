<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="我的成绩"/>
<c:set var="pageEyebrow" value="TRANSCRIPT"/>
<c:set var="activeNav" value="portal-grades"/>
<%@ include file="common/head.jspf" %>

<div class="app-shell">
    <%@ include file="common/sidebar.jspf" %>
    <div class="main-shell">
        <%@ include file="common/topbar.jspf" %>
        <main class="page-content">
            <%@ include file="common/flash.jspf" %>

            <div class="transcript-shell">
                <div class="transcript-toolbar no-print">
                    <div class="page-toolbar-copy">
                        <h2>我的成绩单</h2>
                        <p>查看本学期全部考试记录，也可以直接打印或另存为 PDF。</p>
                    </div>
                    <div class="page-toolbar-actions">
                        <a class="btn btn-ghost" href="${ctx}/portal">
                            <svg class="icon"><use href="#i-arrow-left"></use></svg>
                            返回概览
                        </a>
                        <button type="button" class="btn btn-primary" data-print>
                            <svg class="icon"><use href="#i-printer"></use></svg>
                            打印成绩单
                        </button>
                    </div>
                </div>

                <div class="filter-card no-print">
                    <div class="field">
                        <label class="field-label" for="transcriptCourse">课程</label>
                        <select class="select" id="transcriptCourse">
                            <option value="">全部课程</option>
                            <c:forEach var="course" items="${data.courses}">
                                <option value="${course.courseId}"><c:out value="${course.courseName}"/></option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="field">
                        <label class="field-label" for="transcriptExam">考试</label>
                        <select class="select" id="transcriptExam">
                            <option value="">全部考试</option>
                            <c:forEach var="point" items="${data.trend}">
                                <option value="${fn:escapeXml(point.label)}">${fn:escapeXml(point.label)}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="filter-divider"></div>
                    <span class="result-count" id="transcriptCount">共 ${fn:length(data.allGrades)} 条记录</span>
                </div>

                <article class="transcript-paper">
                    <div class="print-head">
                        <strong>知衡 · 班级学生成绩管理系统</strong>
                        <span>学生成绩单 / 打印件</span>
                    </div>

                    <div class="transcript-head">
                        <div class="transcript-title">
                            <span class="eyebrow">ZHIHENG · ACADEMIC RECORD</span>
                            <h2>学生成绩单</h2>
                            <p>2026–2027 学年 · 第一学期 · 全部考试</p>
                        </div>
                        <div class="transcript-seal">知衡<br>教务</div>
                    </div>

                    <div class="transcript-meta">
                        <div><span>姓名</span><strong><c:out value="${data.student.name}"/></strong></div>
                        <div><span>学号</span><strong>${data.student.studentNo}</strong></div>
                        <div><span>班级</span><strong><c:out value="${data.classInfo.name}"/></strong></div>
                        <div><span>专业</span><strong><c:out value="${data.classInfo.major}"/></strong></div>
                        <div><span>打印日期</span><strong>${todayIso}</strong></div>
                    </div>

                    <div class="table-wrap">
                        <table class="data-table transcript-table" id="transcriptTable">
                            <thead>
                            <tr>
                                <th>课程代码</th>
                                <th>课程名称</th>
                                <th>考试</th>
                                <th>考试日期</th>
                                <th>成绩</th>
                                <th>等级</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="row" items="${data.allGrades}">
                                <tr data-course="${row.courseId}" data-exam="${fn:escapeXml(row.examName)}">
                                    <td class="mono">${fn:escapeXml(row.courseCode)}</td>
                                    <td class="cell-strong"><c:out value="${row.courseName}"/></td>
                                    <td>${fn:escapeXml(row.examName)}</td>
                                    <td class="cell-muted"><fmt:formatDate value="${row.examDate}" pattern="yyyy.MM.dd"/></td>
                                    <td>
                                        <span class="score-chip score-${row.levelTone}"><fmt:formatNumber value="${row.score}" pattern="0.0"/></span>
                                    </td>
                                    <td><span class="level-text level-${row.levelTone}">${row.level}</span></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>

                    <c:if test="${empty data.allGrades}">
                        <div class="empty-state">
                            <div class="empty-icon"><svg class="icon"><use href="#i-list-check"></use></svg></div>
                            <h3>还没有成绩记录</h3>
                            <p>老师录入成绩后，完整成绩单会在这里生成。</p>
                        </div>
                    </c:if>

                    <div class="transcript-summary">
                        <div><span>平均分</span><strong><fmt:formatNumber value="${data.average}" pattern="0.0"/></strong></div>
                        <div><span>平均绩点</span><strong><fmt:formatNumber value="${data.gpa}" pattern="0.00"/></strong></div>
                        <div><span>班级排名</span><strong>${data.rank > 0 ? data.rank : '—'}</strong></div>
                        <div><span>已获学分</span><strong><fmt:formatNumber value="${data.earnedCredits}" pattern="0.#"/></strong></div>
                    </div>

                    <div class="transcript-foot">
                        <span>本成绩单由知衡成绩管理系统生成，仅用于课程设计演示。</span>
                        <span>共 ${fn:length(data.allGrades)} 条成绩记录 · 知衡教务</span>
                    </div>
                </article>
            </div>
        </main>
        <%@ include file="common/footer.jspf" %>

<script>
(function () {
    const courseSelect = document.getElementById('transcriptCourse');
    const examSelect = document.getElementById('transcriptExam');
    const table = document.getElementById('transcriptTable');
    const count = document.getElementById('transcriptCount');
    if (!table) return;
    const rows = Array.from(table.querySelectorAll('tbody tr'));

    function apply() {
        const course = courseSelect ? courseSelect.value : '';
        const exam = examSelect ? examSelect.value : '';
        let visible = 0;
        rows.forEach(row => {
            const show = (!course || row.dataset.course === course) && (!exam || row.dataset.exam === exam);
            row.style.display = show ? '' : 'none';
            if (show) visible++;
        });
        if (count) count.textContent = '共 ' + visible + ' 条记录';
    }
    if (courseSelect) courseSelect.addEventListener('change', apply);
    if (examSelect) examSelect.addEventListener('change', apply);
})();
</script>
</body>
</html>
