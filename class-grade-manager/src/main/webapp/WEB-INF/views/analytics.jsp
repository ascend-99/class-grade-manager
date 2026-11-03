<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="统计分析"/>
<c:set var="pageEyebrow" value="INSIGHTS"/>
<c:set var="activeNav" value="analytics"/>
<c:set var="useCharts" value="true"/>
<%@ include file="common/head.jspf" %>
<c:url var="exportUrl" value="/export">
    <c:param name="type" value="analytics"/>
    <c:param name="classId" value="${data.classId}"/>
    <c:param name="courseId" value="${data.courseId}"/>
    <c:param name="examName" value="${data.examName}"/>
</c:url>

<c:url var="exportExcelUrl" value="/export">
    <c:param name="type" value="analytics"/>
    <c:param name="format" value="xlsx"/>
    <c:param name="classId" value="${data.classId}"/>
    <c:param name="courseId" value="${data.courseId}"/>
    <c:param name="examName" value="${data.examName}"/>
</c:url>

<div class="app-shell">
    <%@ include file="common/sidebar.jspf" %>
    <div class="main-shell">
        <%@ include file="common/topbar.jspf" %>
        <main class="page-content">
            <%@ include file="common/flash.jspf" %>

            <div class="page-toolbar">
                <div class="page-toolbar-copy">
                    <h2>学情分析</h2>
                    <p>从均分、及格率到分数段与排名，读懂班级的真实学习状态。</p>
                </div>
                <div class="page-toolbar-actions">
                    <a class="btn btn-ghost" href="${exportExcelUrl}">
                        <svg class="icon"><use href="#i-download"></use></svg>
                        导出 Excel
                    </a>
                    <a class="btn btn-ghost" href="${exportUrl}">CSV</a>
                </div>
            </div>

            <form class="filter-card" method="get" action="${ctx}/analytics">
                <div class="field select-wide">
                    <label class="field-label" for="analysisClass">班级</label>
                    <select class="select" id="analysisClass" name="classId" data-auto-submit>
                        <c:forEach var="item" items="${data.classes}">
                            <option value="${item.id}" ${item.id == data.classId ? 'selected' : ''}><c:out value="${item.name}"/></option>
                        </c:forEach>
                    </select>
                </div>
                <div class="field">
                    <label class="field-label" for="analysisCourse">课程</label>
                    <select class="select" id="analysisCourse" name="courseId" data-auto-submit>
                        <option value="0">全部课程</option>
                        <c:forEach var="item" items="${data.courses}">
                            <option value="${item.id}" ${item.id == data.courseId ? 'selected' : ''}><c:out value="${item.name}"/></option>
                        </c:forEach>
                    </select>
                </div>
                <div class="field">
                    <label class="field-label" for="analysisExam">考试</label>
                    <select class="select" id="analysisExam" name="examName" data-auto-submit>
                        <option value="">全部考试</option>
                        <c:forEach var="exam" items="${data.exams}">
                            <option value="${fn:escapeXml(exam)}" ${exam == data.examName ? 'selected' : ''}>${fn:escapeXml(exam)}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="filter-actions">
                    <button type="submit" class="btn btn-primary">
                        <svg class="icon"><use href="#i-chart"></use></svg>
                        生成分析
                    </button>
                    <a class="btn btn-ghost" href="${ctx}/analytics">重置</a>
                </div>
            </form>

            <section class="insight-banner">
                <div class="insight-copy">
                    <h2>
                        <c:out value="${data.className}"/> · <em><c:out value="${data.courseName}"/></em>
                        <c:if test="${not data.allExams}"> · ${fn:escapeXml(data.examName)}</c:if>
                    </h2>
                    <p>
                        本次统计覆盖 <strong>${data.stats.studentCount}</strong> 名学生、<strong>${data.stats.recordCount}</strong> 条成绩。
                        班级平均分为 <strong><fmt:formatNumber value="${data.stats.average}" pattern="0.0"/></strong> 分，
                        及格率 <strong><fmt:formatNumber value="${data.stats.passRate}" pattern="0.0"/>%</strong>。
                        <c:choose>
                            <c:when test="${data.stats.failCount > 0}">有 <strong>${data.stats.failCount}</strong> 条成绩低于 60 分，建议安排一次针对性辅导。</c:when>
                            <c:otherwise>目前所有成绩均已达到及格线，继续保持。</c:otherwise>
                        </c:choose>
                    </p>
                    <div class="insight-chips">
                        <span class="insight-chip">最高分 <strong><fmt:formatNumber value="${data.stats.highest}" pattern="0.0"/></strong></span>
                        <span class="insight-chip">最低分 <strong><fmt:formatNumber value="${data.stats.lowest}" pattern="0.0"/></strong></span>
                        <span class="insight-chip">中位数 <strong><fmt:formatNumber value="${data.stats.median}" pattern="0.0"/></strong></span>
                        <span class="insight-chip">优秀率 <strong><fmt:formatNumber value="${data.stats.excellentRate}" pattern="0.0"/>%</strong></span>
                    </div>
                </div>
                <div class="insight-score">
                    <strong><fmt:formatNumber value="${data.stats.average}" pattern="0.0"/></strong>
                    <span>AVERAGE SCORE</span>
                </div>
            </section>

            <div class="kpi-grid">
                <div class="kpi-card" style="--kpi-color:#4f46e5;--kpi-bg:#eef0ff;--kpi-bar:linear-gradient(90deg,#a5b4fc,#4f46e5);">
                    <div class="kpi-top"><span class="kpi-label">平均分</span><span class="kpi-icon"><svg class="icon"><use href="#i-target"></use></svg></span></div>
                    <div class="kpi-value"><fmt:formatNumber value="${data.stats.average}" pattern="0.0"/><small>分</small></div>
                    <div class="kpi-foot">基于 ${data.stats.recordCount} 条成绩记录</div>
                </div>
                <div class="kpi-card" style="--kpi-color:#0e9384;--kpi-bg:#e6f7f4;--kpi-bar:linear-gradient(90deg,#5eead4,#0e9384);">
                    <div class="kpi-top"><span class="kpi-label">及格率</span><span class="kpi-icon"><svg class="icon"><use href="#i-check-circle"></use></svg></span></div>
                    <div class="kpi-value"><fmt:formatNumber value="${data.stats.passRate}" pattern="0.0"/><small>%</small></div>
                    <div class="kpi-foot">${data.stats.failCount} 条成绩未及格</div>
                </div>
                <div class="kpi-card" style="--kpi-color:#d97706;--kpi-bg:#fff8e6;--kpi-bar:linear-gradient(90deg,#fcd34d,#d97706);">
                    <div class="kpi-top"><span class="kpi-label">优秀率</span><span class="kpi-icon"><svg class="icon"><use href="#i-award"></use></svg></span></div>
                    <div class="kpi-value"><fmt:formatNumber value="${data.stats.excellentRate}" pattern="0.0"/><small>%</small></div>
                    <div class="kpi-foot">90 分及以上成绩占比</div>
                </div>
                <div class="kpi-card" style="--kpi-color:#0284c7;--kpi-bg:#e8f4fd;--kpi-bar:linear-gradient(90deg,#7dd3fc,#0284c7);">
                    <div class="kpi-top"><span class="kpi-label">成绩中位数</span><span class="kpi-icon"><svg class="icon"><use href="#i-chart"></use></svg></span></div>
                    <div class="kpi-value"><fmt:formatNumber value="${data.stats.median}" pattern="0.0"/><small>分</small></div>
                    <div class="kpi-foot">不受极端分数影响的参考值</div>
                </div>
            </div>

            <c:choose>
                <c:when test="${data.stats.recordCount == 0}">
                    <div class="card">
                        <div class="empty-state">
                            <div class="empty-icon"><svg class="icon"><use href="#i-chart"></use></svg></div>
                            <h3>这个筛选条件下没有数据</h3>
                            <p>换一个班级、课程或考试，或者先去录入成绩。</p>
                            <a class="btn btn-primary btn-sm" href="${ctx}/entry?classId=${data.classId}">去录入成绩</a>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="analytics-grid">
                        <section class="card card-pad">
                            <div class="card-head">
                                <div>
                                    <h3 class="card-title">分数段分布</h3>
                                    <p class="card-sub">观察成绩结构，而不只是一个平均分</p>
                                </div>
                                <span class="badge badge-indigo no-dot">${data.stats.recordCount} 条</span>
                            </div>
                            <div class="chart-box"><canvas id="distBarChart"></canvas></div>
                        </section>
                        <section class="card card-pad">
                            <div class="card-head">
                                <div>
                                    <h3 class="card-title">课程均分对比</h3>
                                    <p class="card-sub">同一考试口径下的课程表现</p>
                                </div>
                            </div>
                            <div class="chart-box"><canvas id="courseBarChart"></canvas></div>
                        </section>
                    </div>

                    <div class="grid grid-main analytics-bottom">
                        <section class="table-card">
                            <div class="table-head">
                                <div>
                                    <h3>课程明细</h3>
                                    <p>每门课程的均分、及格率与最高 / 最低分</p>
                                </div>
                            </div>
                            <div class="table-wrap">
                                <table class="data-table course-stat-table">
                                    <thead>
                                    <tr>
                                        <th>课程</th>
                                        <th>均分</th>
                                        <th>及格率</th>
                                        <th class="hide-mobile">优秀率</th>
                                        <th class="hide-mobile">最高 / 最低</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach var="course" items="${data.courseStats}">
                                        <tr>
                                            <td>
                                                <span class="course-tag">
                                                    <span class="course-dot ${course.color}"></span>
                                                    <span>
                                                        <span class="cell-strong" style="display:block;"><c:out value="${course.courseName}"/></span>
                                                        <span class="cell-muted">${fn:escapeXml(course.teacher)}</span>
                                                    </span>
                                                </span>
                                            </td>
                                            <td><span class="cell-strong"><fmt:formatNumber value="${course.average}" pattern="0.0"/></span></td>
                                            <td>
                                                <div class="score-bar">
                                                    <div class="score-bar-track"><span class="${course.color}" style="width:${course.passRate}%;"></span></div>
                                                    <span class="score-bar-value"><fmt:formatNumber value="${course.passRate}" pattern="0.0"/>%</span>
                                                </div>
                                            </td>
                                            <td class="hide-mobile"><fmt:formatNumber value="${course.excellentRate}" pattern="0.0"/>%</td>
                                            <td class="hide-mobile cell-muted">
                                                <fmt:formatNumber value="${course.highest}" pattern="0.0"/> / <fmt:formatNumber value="${course.lowest}" pattern="0.0"/>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </section>

                        <div class="stack">
                            <section class="card card-pad ranking-panel">
                                <div class="card-head">
                                    <div>
                                        <h3 class="card-title">班级排名</h3>
                                        <p class="card-sub">按当前筛选口径计算</p>
                                    </div>
                                    <span class="badge badge-amber no-dot">TOP 10</span>
                                </div>
                                <div class="rank-list">
                                    <c:forEach var="student" items="${data.ranking}">
                                        <div class="rank-item">
                                            <span class="medal ${student.rankTone}">${student.rank}</span>
                                            <span class="avatar sm avatar-${student.studentId % 5}">${fn:escapeXml(student.initial)}</span>
                                            <div class="rank-copy">
                                                <strong><c:out value="${student.name}"/></strong>
                                                <span>${student.studentNo} · ${student.recordCount} 条成绩</span>
                                            </div>
                                            <span class="rank-score"><fmt:formatNumber value="${student.average}" pattern="0.0"/></span>
                                        </div>
                                    </c:forEach>
                                    <c:if test="${empty data.ranking}">
                                        <div class="empty-state" style="padding:20px;">
                                            <h3>暂无排名</h3>
                                        </div>
                                    </c:if>
                                </div>
                            </section>

                            <section class="card card-pad attention-card">
                                <div class="card-head" style="margin-bottom:0;">
                                    <div>
                                        <h3 class="card-title">重点关注</h3>
                                        <p class="card-sub">低于 60 分的成绩记录</p>
                                    </div>
                                    <svg class="icon" style="color:#f5c66e;"><use href="#i-alert"></use></svg>
                                </div>
                                <div class="attention-number">${data.stats.failCount}</div>
                                <p>条成绩需要关注，建议结合试卷与课堂表现做一次个别沟通。</p>
                                <div class="attention-list">
                                    <c:forEach var="item" items="${data.attention}">
                                        <c:url var="itemHistoryUrl" value="/grade-history">
                                            <c:param name="studentId" value="${item.studentId}"/>
                                            <c:param name="courseId" value="${item.courseId}"/>
                                        </c:url>
                                        <a class="attention-student" href="${itemHistoryUrl}">
                                            <span class="avatar sm avatar-${item.studentId % 5}">${fn:escapeXml(item.initial)}</span>
                                            <span class="attention-student-copy">
                                                <strong><c:out value="${item.studentName}"/></strong>
                                                <small><c:out value="${item.courseName}"/> · ${fn:escapeXml(item.examName)}</small>
                                            </span>
                                            <span class="attention-score"><fmt:formatNumber value="${item.score}" pattern="0.0"/></span>
                                        </a>
                                    </c:forEach>
                                    <c:if test="${empty data.attention}">
                                        <div class="attention-row"><span style="color:#cbd5e1;">全部课程</span><span>本次无不及格记录</span></div>
                                    </c:if>
                                </div>
                                <div class="attention-list" style="margin-top:10px;">
                                    <div class="attention-row"><span style="color:#cbd5e1;">最低分</span><span><fmt:formatNumber value="${data.stats.lowest}" pattern="0.0"/> 分</span></div>
                                    <div class="attention-row"><span style="color:#cbd5e1;">中位数</span><span><fmt:formatNumber value="${data.stats.median}" pattern="0.0"/> 分</span></div>
                                    <div class="attention-row"><span style="color:#cbd5e1;">优秀率</span><span><fmt:formatNumber value="${data.stats.excellentRate}" pattern="0.0"/>%</span></div>
                                </div>
                            </section>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </main>
        <%@ include file="common/footer.jspf" %>

<script>
(function () {
    if (typeof Chart === 'undefined') return;
    Chart.defaults.font.family = getComputedStyle(document.body).fontFamily;
    Chart.defaults.color = '#94a3b8';
    const tooltipStyle = {
        backgroundColor: '#0f172a',
        padding: 10,
        cornerRadius: 8,
        titleFont: { size: 11, weight: '600' },
        bodyFont: { size: 11 }
    };

    const distCanvas = document.getElementById('distBarChart');
    if (distCanvas) {
        new Chart(distCanvas, {
            type: 'bar',
            data: {
                labels: ${distLabelsJson},
                datasets: [{
                    data: ${distCountsJson},
                    backgroundColor: ['#fb7185', '#fbbf24', '#60a5fa', '#34d399', '#4f46e5'],
                    borderRadius: 7,
                    borderSkipped: false,
                    barThickness: 34
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                animation: { duration: 800, easing: 'easeOutQuart' },
                plugins: { legend: { display: false }, tooltip: tooltipStyle },
                scales: {
                    x: { grid: { display: false }, border: { display: false }, ticks: { font: { size: 10 } } },
                    y: { beginAtZero: true, grid: { color: '#f1f5f9' }, border: { display: false }, ticks: { precision: 0, font: { size: 10 } } }
                }
            }
        });
    }

    const courseCanvas = document.getElementById('courseBarChart');
    if (courseCanvas) {
        new Chart(courseCanvas, {
            type: 'bar',
            data: {
                labels: ${courseLabelsJson},
                datasets: [{
                    data: ${courseAveragesJson},
                    backgroundColor: ['#6366f1', '#0e9384', '#f59e0b', '#e11d48', '#0284c7'],
                    borderRadius: 6,
                    borderSkipped: false,
                    barThickness: 18
                }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                maintainAspectRatio: false,
                animation: { duration: 800, easing: 'easeOutQuart' },
                plugins: { legend: { display: false }, tooltip: tooltipStyle },
                scales: {
                    x: { beginAtZero: true, max: 100, grid: { color: '#f1f5f9' }, border: { display: false }, ticks: { font: { size: 10 } } },
                    y: { grid: { display: false }, border: { display: false }, ticks: { font: { size: 9.5 } } }
                }
            }
        });
    }
})();
</script>
</body>
</html>
