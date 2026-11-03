<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="我的概览"/>
<c:set var="pageEyebrow" value="MY SEMESTER"/>
<c:set var="activeNav" value="portal"/>
<c:set var="useCharts" value="true"/>
<%@ include file="common/head.jspf" %>

<div class="app-shell">
    <%@ include file="common/sidebar.jspf" %>
    <div class="main-shell">
        <%@ include file="common/topbar.jspf" %>
        <main class="page-content">
            <%@ include file="common/flash.jspf" %>

            <section class="portal-hero">
                <div class="portal-hero-copy">
                    <span class="hero-kicker"><span class="live-dot"></span> STUDENT OVERVIEW</span>
                    <h2>${greeting}，<c:out value="${data.student.name}"/>。<br>这是你本学期的<em>学习答卷</em>。</h2>
                    <p>每一次认真，都被记录在这里。继续保持自己的节奏，向下一个目标靠近。</p>
                    <div class="portal-tags">
                        <span class="portal-tag"><svg class="icon"><use href="#i-id"></use></svg> ${data.student.studentNo}</span>
                        <span class="portal-tag"><svg class="icon"><use href="#i-users"></use></svg> <c:out value="${data.classInfo.name}"/></span>
                        <span class="portal-tag"><svg class="icon"><use href="#i-book"></use></svg> ${data.gradedCourses} / ${data.totalCourses} 门已出分</span>
                    </div>
                </div>
                <div class="portal-rank-badge">
                    <svg class="icon"><use href="#i-award"></use></svg>
                    <strong><c:choose><c:when test="${data.rank > 0}">${data.rank}</c:when><c:otherwise>—</c:otherwise></c:choose></strong>
                    <span>${data.rank > 0 ? 'CLASS RANK' : 'NO RANK'}</span>
                </div>
            </section>

            <div class="kpi-grid">
                <div class="kpi-card" style="--kpi-color:#4f46e5;--kpi-bg:#eef0ff;--kpi-bar:linear-gradient(90deg,#a5b4fc,#4f46e5);">
                    <div class="kpi-top"><span class="kpi-label">本学期平均分</span><span class="kpi-icon"><svg class="icon"><use href="#i-target"></use></svg></span></div>
                    <div class="kpi-value"><fmt:formatNumber value="${data.average}" pattern="0.0"/><small>分</small></div>
                    <div class="kpi-foot">班级均分 <strong><fmt:formatNumber value="${data.classAverage}" pattern="0.0"/></strong> 分</div>
                </div>
                <div class="kpi-card" style="--kpi-color:#0e9384;--kpi-bg:#e6f7f4;--kpi-bar:linear-gradient(90deg,#5eead4,#0e9384);">
                    <div class="kpi-top"><span class="kpi-label">班级排名</span><span class="kpi-icon"><svg class="icon"><use href="#i-award"></use></svg></span></div>
                    <div class="kpi-value"><c:choose><c:when test="${data.rank > 0}">${data.rank}<small>/ ${data.classSize}</small></c:when><c:otherwise>—</c:otherwise></c:choose></div>
                    <div class="kpi-foot">${data.rankLabel}</div>
                </div>
                <div class="kpi-card" style="--kpi-color:#d97706;--kpi-bg:#fff8e6;--kpi-bar:linear-gradient(90deg,#fcd34d,#d97706);">
                    <div class="kpi-top"><span class="kpi-label">平均绩点</span><span class="kpi-icon"><svg class="icon"><use href="#i-sparkles"></use></svg></span></div>
                    <div class="kpi-value"><fmt:formatNumber value="${data.gpa}" pattern="0.00"/><small>/ 4.0</small></div>
                    <div class="kpi-foot">按课程学分加权计算</div>
                </div>
                <div class="kpi-card" style="--kpi-color:#0284c7;--kpi-bg:#e8f4fd;--kpi-bar:linear-gradient(90deg,#7dd3fc,#0284c7);">
                    <div class="kpi-top"><span class="kpi-label">已获学分</span><span class="kpi-icon"><svg class="icon"><use href="#i-graduation"></use></svg></span></div>
                    <div class="kpi-value"><fmt:formatNumber value="${data.earnedCredits}" pattern="0.#"/><small>/ ${data.totalCredits} 学分</small></div>
                    <div class="kpi-foot">及格课程自动计入已获学分</div>
                </div>
            </div>

            <div class="portal-grid">
                <section class="card card-pad">
                    <div class="card-head">
                        <div>
                            <h3 class="card-title">成绩趋势</h3>
                            <p class="card-sub">按考试场次查看各科平均表现</p>
                        </div>
                        <span class="badge badge-indigo no-dot">${fn:length(data.trend)} 个节点</span>
                    </div>
                    <c:choose>
                        <c:when test="${empty data.trend}">
                            <div class="empty-state">
                                <div class="empty-icon"><svg class="icon"><use href="#i-chart"></use></svg></div>
                                <h3>还没有成绩趋势</h3>
                                <p>老师录入成绩后，这里会生成你的学习曲线。</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="chart-box"><canvas id="trendChart"></canvas></div>
                        </c:otherwise>
                    </c:choose>
                </section>

                <div class="stack">
                    <section class="card card-pad">
                        <div class="card-head">
                            <div>
                                <h3 class="card-title">出分进度</h3>
                                <p class="card-sub">本学期课程完成情况</p>
                            </div>
                        </div>
                        <div class="portal-progress">
                            <div class="progress-ring" style="--value:${data.progressPercent};">
                                <div class="progress-ring-inner">
                                    <strong>${data.progressPercent}%</strong>
                                    <span>${data.gradedCourses} / ${data.totalCourses} 门</span>
                                </div>
                            </div>
                        </div>
                        <div class="stat-lines">
                            <div class="stat-line"><span>及格率</span><strong><fmt:formatNumber value="${data.passRate}" pattern="0.0"/>%</strong></div>
                            <div class="stat-line"><span>优秀课程</span><strong>${data.excellentCount} 门</strong></div>
                            <div class="stat-line"><span>待提升课程</span><strong>${data.failCount} 门</strong></div>
                        </div>
                    </section>

                    <section class="quote-card">
                        <svg class="icon"><use href="#i-sparkles"></use></svg>
                        <p>“不积跬步，无以至千里。<br>把每一次小进步，留在自己的时间线上。”</p>
                        <span>知衡 · 学习寄语</span>
                    </section>
                </div>
            </div>

            <section class="card card-pad" style="margin-bottom:18px;">
                <div class="card-head">
                    <div>
                        <h3 class="card-title">本学期课程</h3>
                        <p class="card-sub">以最近一次考试作为课程当前成绩</p>
                    </div>
                    <a class="card-link" href="${ctx}/portal/grades">
                        查看完整成绩单 <svg class="icon"><use href="#i-arrow-right"></use></svg>
                    </a>
                </div>
                <div class="portal-course-grid">
                    <c:forEach var="course" items="${data.courses}">
                        <article class="portal-course">
                            <div class="portal-course-top">
                                <div>
                                    <span class="portal-course-code">${fn:escapeXml(course.courseCode)}</span>
                                    <h4><c:out value="${course.courseName}"/></h4>
                                    <div class="portal-course-teacher">${fn:escapeXml(course.teacher)} · ${course.credit} 学分</div>
                                </div>
                                <c:choose>
                                    <c:when test="${course.recorded}">
                                        <div class="portal-score-ring" style="--score:${course.score};--ring-color:${course.color == 'indigo' ? '#6366f1' : course.color == 'teal' ? '#0e9384' : course.color == 'amber' ? '#d97706' : course.color == 'rose' ? '#e11d48' : '#0284c7'};">
                                            <span><fmt:formatNumber value="${course.score}" pattern="0.#"/></span>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="portal-score-ring empty"><span>待录</span></div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="portal-course-foot">
                                <span>${course.recorded ? fn:escapeXml(course.examName) : '成绩待录入'}</span>
                                <c:choose>
                                    <c:when test="${course.recorded}">
                                        <strong class="${course.difference >= 0 ? 'diff-up' : 'diff-down'}">
                                            班级均分 <fmt:formatNumber value="${course.classAverage}" pattern="0.0"/> · ${course.differenceLabel}
                                        </strong>
                                    </c:when>
                                    <c:otherwise><strong>—</strong></c:otherwise>
                                </c:choose>
                            </div>
                        </article>
                    </c:forEach>
                    <c:if test="${empty data.courses}">
                        <div class="empty-state" style="grid-column:1/-1;">
                            <div class="empty-icon"><svg class="icon"><use href="#i-book"></use></svg></div>
                            <h3>还没有课程成绩</h3>
                            <p>老师录入成绩后，课程卡片会出现在这里。</p>
                        </div>
                    </c:if>
                </div>
            </section>

            <section class="card card-pad">
                <div class="card-head">
                    <div>
                        <h3 class="card-title">最近成绩</h3>
                        <p class="card-sub">按考试日期倒序排列</p>
                    </div>
                    <a class="card-link" href="${ctx}/portal/grades">完整成绩单 <svg class="icon"><use href="#i-arrow-right"></use></svg></a>
                </div>
                <div class="table-wrap">
                    <table class="data-table">
                        <thead>
                        <tr>
                            <th>课程</th>
                            <th>考试</th>
                            <th class="hide-mobile">日期</th>
                            <th>成绩</th>
                            <th>等级</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="row" items="${data.allGrades}" end="4">
                            <tr>
                                <td>
                                    <span class="course-tag"><span class="course-dot ${row.courseColor}"></span><c:out value="${row.courseName}"/></span>
                                </td>
                                <td><span class="badge badge-muted no-dot">${fn:escapeXml(row.examName)}</span></td>
                                <td class="hide-mobile cell-muted"><fmt:formatDate value="${row.examDate}" pattern="yyyy.MM.dd"/></td>
                                <td><span class="score-chip score-${row.levelTone}"><fmt:formatNumber value="${row.score}" pattern="0.0"/></span></td>
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
                        <p>成绩发布后会在这里展示。</p>
                    </div>
                </c:if>
            </section>
        </main>
        <%@ include file="common/footer.jspf" %>

<script>
(function () {
    const canvas = document.getElementById('trendChart');
    if (!canvas || typeof Chart === 'undefined') return;
    Chart.defaults.font.family = getComputedStyle(document.body).fontFamily;
    Chart.defaults.color = '#94a3b8';
    const labels = [];
    const values = [];
    <c:forEach var="point" items="${data.trend}">
    labels.push('${fn:escapeXml(point.label)}');
    values.push(${point.value});
    </c:forEach>
    const context = canvas.getContext('2d');
    const gradient = context.createLinearGradient(0, 0, 0, 260);
    gradient.addColorStop(0, 'rgba(99,102,241,.26)');
    gradient.addColorStop(1, 'rgba(99,102,241,0)');
    new Chart(canvas, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                data: values,
                borderColor: '#4f46e5',
                backgroundColor: gradient,
                fill: true,
                tension: .4,
                borderWidth: 2.5,
                pointRadius: 4,
                pointHoverRadius: 6,
                pointBackgroundColor: '#fff',
                pointBorderColor: '#4f46e5',
                pointBorderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            animation: { duration: 900, easing: 'easeOutQuart' },
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: '#0f172a',
                    padding: 10,
                    cornerRadius: 8,
                    callbacks: {
                        label: function (context) { return ' 平均 ' + context.raw + ' 分'; }
                    }
                }
            },
            scales: {
                x: { grid: { display: false }, border: { display: false }, ticks: { font: { size: 10 } } },
                y: {
                    suggestedMin: values.length ? Math.max(0, Math.floor(Math.min(...values) / 10) * 10 - 5) : 40,
                    suggestedMax: 100,
                    grid: { color: '#f1f5f9' }, border: { display: false },
                    ticks: { stepSize: 10, font: { size: 10 } }
                }
            }
        }
    });
})();
</script>
</body>
</html>
