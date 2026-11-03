<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="工作台"/>
<c:set var="pageEyebrow" value="TEACHER WORKSPACE"/>
<c:set var="activeNav" value="dashboard"/>
<c:set var="useCharts" value="true"/>
<%@ include file="common/head.jspf" %>

<div class="app-shell">
    <%@ include file="common/sidebar.jspf" %>
    <div class="main-shell">
        <%@ include file="common/topbar.jspf" %>
        <main class="page-content">
            <%@ include file="common/flash.jspf" %>

            <section class="hero">
                <div class="hero-copy">
                    <span class="hero-kicker"><span class="live-dot"></span> SEMESTER OVERVIEW</span>
                    <h2>${greeting}，<c:out value="${currentUser.displayName}"/>老师。<br>今天也把每一份<em>成长</em>记录清楚。</h2>
                    <p>
                        <c:if test="${not empty data.selectedClass}">
                            <c:out value="${data.selectedClass.name}"/>的学期学习画像已更新，目前共有
                            <strong style="color:#fff;">${data.studentCount}</strong> 名学生、
                            <strong style="color:#fff;">${data.gradeCount}</strong> 条成绩记录。
                        </c:if>
                        从一次录入开始，看见班级真实的进步轨迹。
                    </p>
                    <div class="hero-actions">
                        <a class="btn btn-light" href="${ctx}/entry<c:if test="${not empty data.selectedClass}">?classId=${data.selectedClass.id}</c:if>">
                            <svg class="icon"><use href="#i-edit"></use></svg>
                            录入成绩
                        </a>
                        <a class="btn btn-on-dark" href="${ctx}/analytics<c:if test="${not empty data.selectedClass}">?classId=${data.selectedClass.id}</c:if>">
                            <svg class="icon"><use href="#i-chart"></use></svg>
                            查看学情分析
                        </a>
                    </div>
                </div>
                <div class="hero-visual">
                    <div class="score-orbit" style="--score:${data.averageScore};">
                        <div class="score-orbit-inner">
                            <strong><fmt:formatNumber value="${data.averageScore}" pattern="0.0"/></strong>
                            <span>班级平均分</span>
                        </div>
                    </div>
                    <div class="hero-float a">
                        <svg class="icon"><use href="#i-trend"></use></svg>
                        <span>及格率 <strong><fmt:formatNumber value="${data.passRate}" pattern="0.0"/>%</strong></span>
                    </div>
                    <div class="hero-float b">
                        <svg class="icon"><use href="#i-award"></use></svg>
                        <span>优秀率 <strong><fmt:formatNumber value="${data.excellentRate}" pattern="0.0"/>%</strong></span>
                    </div>
                </div>
            </section>

            <div class="dashboard-toolbar">
                <div class="section-head" style="margin-bottom:0;">
                    <div>
                        <h2>班级概览</h2>
                        <p>一屏掌握人数、均分与学习质量</p>
                    </div>
                </div>
                <form class="class-select" method="get" action="${ctx}/dashboard">
                    <svg class="icon" style="width:15px;height:15px;color:#94a3b8;"><use href="#i-users"></use></svg>
                    <select class="select" name="classId" data-auto-submit aria-label="切换班级">
                        <c:forEach var="item" items="${data.classes}">
                            <option value="${item.id}" ${item.id == data.selectedClass.id ? 'selected' : ''}>
                                <c:out value="${item.name}"/> · ${item.studentCount} 人
                            </option>
                        </c:forEach>
                    </select>
                </form>
            </div>

            <div class="kpi-grid">
                <div class="kpi-card" style="--kpi-color:#4f46e5;--kpi-bg:#eef0ff;--kpi-bar:linear-gradient(90deg,#a5b4fc,#4f46e5);--kpi-glow:rgba(99,102,241,.16);">
                    <div class="kpi-top">
                        <span class="kpi-label">班级人数</span>
                        <span class="kpi-icon"><svg class="icon"><use href="#i-users"></use></svg></span>
                    </div>
                    <div class="kpi-value">${data.studentCount}<small>人</small></div>
                    <div class="kpi-foot">覆盖 <strong>${data.courseCount}</strong> 门课程 · ${data.gradeCount} 条记录</div>
                    <div class="kpi-line"><span style="width:100%;"></span></div>
                </div>

                <div class="kpi-card" style="--kpi-color:#0e9384;--kpi-bg:#e6f7f4;--kpi-bar:linear-gradient(90deg,#5eead4,#0e9384);--kpi-glow:rgba(14,147,132,.14);">
                    <div class="kpi-top">
                        <span class="kpi-label">班级平均分</span>
                        <span class="kpi-icon"><svg class="icon"><use href="#i-target"></use></svg></span>
                    </div>
                    <div class="kpi-value"><fmt:formatNumber value="${data.averageScore}" pattern="0.0"/><small>分</small></div>
                    <div class="kpi-foot"><span class="trend-up">稳步积累</span> · 全部考试均值</div>
                    <div class="kpi-line"><span style="width:${data.averageScore}%;"></span></div>
                </div>

                <div class="kpi-card" style="--kpi-color:#0284c7;--kpi-bg:#e8f4fd;--kpi-bar:linear-gradient(90deg,#7dd3fc,#0284c7);--kpi-glow:rgba(2,132,199,.13);">
                    <div class="kpi-top">
                        <span class="kpi-label">及格率</span>
                        <span class="kpi-icon"><svg class="icon"><use href="#i-check-circle"></use></svg></span>
                    </div>
                    <div class="kpi-value"><fmt:formatNumber value="${data.passRate}" pattern="0.0"/><small>%</small></div>
                    <div class="kpi-foot">
                        <c:choose>
                            <c:when test="${data.failCount == 0}"><span class="trend-up">全员达标</span> · 继续保持</c:when>
                            <c:otherwise><strong>${data.failCount}</strong> 条成绩低于 60 分</c:otherwise>
                        </c:choose>
                    </div>
                    <div class="kpi-line"><span style="width:${data.passRate}%;"></span></div>
                </div>

                <div class="kpi-card" style="--kpi-color:#d97706;--kpi-bg:#fff8e6;--kpi-bar:linear-gradient(90deg,#fcd34d,#d97706);--kpi-glow:rgba(217,119,6,.13);">
                    <div class="kpi-top">
                        <span class="kpi-label">优秀率</span>
                        <span class="kpi-icon"><svg class="icon"><use href="#i-award"></use></svg></span>
                    </div>
                    <div class="kpi-value"><fmt:formatNumber value="${data.excellentRate}" pattern="0.0"/><small>%</small></div>
                    <div class="kpi-foot">90 分及以上 · 共 <strong>${data.gradeCount}</strong> 条样本</div>
                    <div class="kpi-line"><span style="width:${data.excellentRate}%;"></span></div>
                </div>
            </div>

            <div class="grid grid-main" style="margin-bottom:18px;">
                <section class="card card-pad">
                    <div class="card-head">
                        <div>
                            <h3 class="card-title">成绩分布</h3>
                            <p class="card-sub">全部课程与考试的成绩区间构成</p>
                        </div>
                        <a class="card-link" href="${ctx}/analytics<c:if test="${not empty data.selectedClass}">?classId=${data.selectedClass.id}</c:if>">
                            深入分析 <svg class="icon"><use href="#i-arrow-right"></use></svg>
                        </a>
                    </div>
                    <div class="donut-layout">
                        <div class="donut-wrap">
                            <canvas id="distributionChart" aria-label="成绩分布图"></canvas>
                            <div class="donut-center">
                                <strong>${data.gradeCount}</strong>
                                <span>成绩样本</span>
                            </div>
                        </div>
                        <div class="chart-legend">
                            <c:forEach var="point" items="${data.distribution}">
                                <div class="legend-item">
                                    <span class="legend-dot ${point.tone}"></span>
                                    <span>${point.label}</span>
                                    <strong>${point.count} 条 · <fmt:formatNumber value="${point.percent}" pattern="0.0"/>%</strong>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </section>

                <section class="card card-pad">
                    <div class="card-head">
                        <div>
                            <h3 class="card-title">课程表现</h3>
                            <p class="card-sub">各课程平均分与及格率</p>
                        </div>
                        <a class="card-link" href="${ctx}/courses?classId=${data.selectedClass.id}">
                            课程管理 <svg class="icon"><use href="#i-arrow-right"></use></svg>
                        </a>
                    </div>
                    <div class="course-list">
                        <c:forEach var="course" items="${data.courseStats}">
                            <div class="course-row">
                                <span class="course-symbol ${course.color}">
                                    <c:out value="${fn:substring(course.courseCode, 0, 2)}"/>
                                </span>
                                <div class="course-row-main">
                                    <div class="course-row-top">
                                        <strong><c:out value="${course.courseName}"/></strong>
                                        <span><fmt:formatNumber value="${course.passRate}" pattern="0.0"/>% 及格</span>
                                    </div>
                                    <div class="meter">
                                        <span class="${course.color}" style="width:${course.passRate}%;"></span>
                                    </div>
                                </div>
                                <div class="course-row-score"><fmt:formatNumber value="${course.average}" pattern="0.0"/></div>
                            </div>
                        </c:forEach>
                        <c:if test="${empty data.courseStats}">
                            <div class="empty-state" style="padding:24px 12px;">
                                <div class="empty-icon"><svg class="icon"><use href="#i-book"></use></svg></div>
                                <h3>还没有课程数据</h3>
                                <p>添加课程并录入成绩后，这里会自动生成课程表现。</p>
                            </div>
                        </c:if>
                    </div>
                </section>
            </div>

            <div class="grid grid-main-reverse">
                <section class="card card-pad">
                    <div class="card-head">
                        <div>
                            <h3 class="card-title">班级之星</h3>
                            <p class="card-sub">当前班级平均分排名</p>
                        </div>
                        <span class="badge badge-amber no-dot">TOP 5</span>
                    </div>
                    <div class="rank-list">
                        <c:forEach var="student" items="${data.topStudents}">
                            <div class="rank-item">
                                <span class="medal ${student.rankTone}">${student.rank}</span>
                                <span class="avatar sm avatar-${student.rank % 5}">${fn:escapeXml(student.initial)}</span>
                                <div class="rank-copy">
                                    <strong><c:out value="${student.name}"/></strong>
                                    <span>${student.studentNo}</span>
                                </div>
                                <span class="rank-score"><fmt:formatNumber value="${student.average}" pattern="0.0"/></span>
                            </div>
                        </c:forEach>
                        <c:if test="${empty data.topStudents}">
                            <div class="empty-state" style="padding:24px 12px;">
                                <h3>暂无排名</h3>
                                <p>录入成绩后即可生成班级排名。</p>
                            </div>
                        </c:if>
                    </div>
                </section>

                <section class="card card-pad">
                    <div class="card-head">
                        <div>
                            <h3 class="card-title">最近录入</h3>
                            <p class="card-sub">最新更新的成绩记录</p>
                        </div>
                        <a class="card-link" href="${ctx}/grades?classId=${data.selectedClass.id}">
                            全部成绩 <svg class="icon"><use href="#i-arrow-right"></use></svg>
                        </a>
                    </div>
                    <div class="recent-list">
                        <c:forEach var="row" items="${data.recentGrades}">
                            <div class="recent-item">
                                <span class="avatar sm avatar-${row.studentId % 5}">${fn:escapeXml(row.initial)}</span>
                                <div class="recent-copy">
                                    <strong><c:out value="${row.studentName}"/> · <c:out value="${row.courseName}"/></strong>
                                    <span>${row.examName} · ${row.studentNo}</span>
                                </div>
                                <div class="recent-meta">
                                    <span class="score-chip score-${row.levelTone}"><fmt:formatNumber value="${row.score}" pattern="0.0"/></span>
                                    <time><fmt:formatDate value="${row.updatedAt}" pattern="MM-dd HH:mm"/></time>
                                </div>
                            </div>
                        </c:forEach>
                        <c:if test="${empty data.recentGrades}">
                            <div class="empty-state" style="padding:24px 12px;">
                                <div class="empty-icon"><svg class="icon"><use href="#i-edit"></use></svg></div>
                                <h3>还没有成绩记录</h3>
                                <p>从成绩录入开始，建立班级的第一份学习档案。</p>
                                <a class="btn btn-primary btn-sm" href="${ctx}/entry">去录入</a>
                            </div>
                        </c:if>
                    </div>
                </section>
            </div>
        </main>
        <%@ include file="common/footer.jspf" %>

<script>
(function () {
    const canvas = document.getElementById('distributionChart');
    if (!canvas || typeof Chart === 'undefined') return;
    const labels = [];
    const values = [];
    <c:forEach var="point" items="${data.distribution}">
    labels.push('${fn:escapeXml(point.label)}');
    values.push(${point.count});
    </c:forEach>
    Chart.defaults.font.family = getComputedStyle(document.body).fontFamily;
    Chart.defaults.color = '#94a3b8';
    new Chart(canvas, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: values,
                backgroundColor: ['#fb7185', '#fbbf24', '#60a5fa', '#34d399', '#4f46e5'],
                hoverBackgroundColor: ['#f43f5e', '#f59e0b', '#3b82f6', '#10b981', '#4338ca'],
                borderWidth: 0,
                spacing: 3,
                borderRadius: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '76%',
            animation: { animateRotate: true, duration: 900, easing: 'easeOutQuart' },
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: '#0f172a',
                    padding: 10,
                    cornerRadius: 8,
                    displayColors: true,
                    callbacks: {
                        label: function (context) {
                            const total = context.dataset.data.reduce(function (a, b) { return a + b; }, 0);
                            const percent = total ? (context.raw * 100 / total).toFixed(1) : '0.0';
                            return ' ' + context.label + '：' + context.raw + ' 条（' + percent + '%）';
                        }
                    }
                }
            }
        }
    });
})();
</script>
</body>
</html>
