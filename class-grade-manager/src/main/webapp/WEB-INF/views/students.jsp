<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="学生管理"/>
<c:set var="pageEyebrow" value="STUDENTS"/>
<c:set var="activeNav" value="students"/>
<%@ include file="common/head.jspf" %>
<c:url var="returnTo" value="/students">
    <c:param name="classId" value="${classId}"/>
    <c:param name="q" value="${keyword}"/>
    <c:param name="page" value="${data.page}"/>
</c:url>

<div class="app-shell">
    <%@ include file="common/sidebar.jspf" %>
    <div class="main-shell">
        <%@ include file="common/topbar.jspf" %>
        <main class="page-content">
            <%@ include file="common/flash.jspf" %>

            <div class="page-toolbar">
                <div class="page-toolbar-copy">
                    <h2>学生档案</h2>
                    <p>维护班级成员信息，新增学生会自动开通学号登录账号。</p>
                </div>
                <div class="page-toolbar-actions">
                    <button type="button" class="btn btn-primary" data-student-add>
                        <svg class="icon"><use href="#i-plus"></use></svg>
                        新增学生
                    </button>
                </div>
            </div>

            <form class="filter-card" method="get" action="${ctx}/students">
                <div class="field search-field">
                    <label class="field-label" for="studentSearch">搜索学生</label>
                    <div class="input-icon">
                        <svg class="icon"><use href="#i-search"></use></svg>
                        <input class="input" id="studentSearch" name="q" type="search"
                               value="${fn:escapeXml(keyword)}" placeholder="姓名、学号或手机号">
                    </div>
                </div>
                <div class="field select-wide">
                    <label class="field-label" for="studentClass">所在班级</label>
                    <select class="select" id="studentClass" name="classId">
                        <option value="0">全部班级</option>
                        <c:forEach var="item" items="${classes}">
                            <option value="${item.id}" ${item.id == classId ? 'selected' : ''}>
                                <c:out value="${item.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="filter-actions">
                    <button type="submit" class="btn btn-primary">
                        <svg class="icon"><use href="#i-search"></use></svg>
                        筛选
                    </button>
                    <a class="btn btn-ghost" href="${ctx}/students">重置</a>
                </div>
                <div class="filter-divider"></div>
                <span class="result-count">共 <strong>${data.total}</strong> 名学生</span>
            </form>

            <div class="table-card">
                <div class="table-head">
                    <div>
                        <h3>班级成员</h3>
                        <p>统计口径为该学生已录入的全部成绩</p>
                    </div>
                    <span class="hint-strip">
                        <svg class="icon"><use href="#i-info"></use></svg>
                        新学生默认密码 123456
                    </span>
                </div>
                <div class="table-wrap">
                    <table class="data-table">
                        <thead>
                        <tr>
                            <th>学生</th>
                            <th>学号</th>
                            <th class="hide-tablet">班级</th>
                            <th class="hide-mobile">联系方式</th>
                            <th>平均分</th>
                            <th class="hide-mobile">及格率</th>
                            <th>状态</th>
                            <th style="text-align:right;">操作</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="student" items="${data.items}">
                            <tr>
                                <td>
                                    <div class="user-cell">
                                        <span class="avatar avatar-${student.id % 5}">${fn:escapeXml(student.initial)}</span>
                                        <div class="user-cell-copy">
                                            <strong><c:out value="${student.name}"/></strong>
                                            <span>${student.gender} · 加入于 <fmt:formatDate value="${student.createdAt}" pattern="yyyy.MM"/></span>
                                        </div>
                                    </div>
                                </td>
                                <td class="mono cell-strong">${fn:escapeXml(student.studentNo)}</td>
                                <td class="hide-tablet"><c:out value="${student.className}"/></td>
                                <td class="hide-mobile">
                                    <div class="cell-strong" style="font-size:10.5px;">${fn:escapeXml(student.phone)}</div>
                                    <div class="cell-muted">${fn:escapeXml(student.email)}</div>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${student.gradeCount > 0}">
                                            <div class="score-bar">
                                                <div class="score-bar-track">
                                                    <span class="${student.averageScore < 60 ? 'bad' : student.averageScore < 70 ? 'warn' : student.averageScore >= 85 ? 'good' : ''}"
                                                          style="width:${student.averageScore}%;"></span>
                                                </div>
                                                <span class="score-bar-value"><fmt:formatNumber value="${student.averageScore}" pattern="0.0"/></span>
                                            </div>
                                        </c:when>
                                        <c:otherwise><span class="cell-muted">暂无成绩</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="hide-mobile">
                                    <c:choose>
                                        <c:when test="${student.gradeCount > 0}">
                                            <span class="badge ${student.passRate >= 90 ? 'badge-success' : student.passRate >= 60 ? 'badge-warning' : 'badge-danger'} no-dot">
                                                <fmt:formatNumber value="${student.passRate}" pattern="0.0"/>%
                                            </span>
                                        </c:when>
                                        <c:otherwise><span class="cell-muted">—</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td><span class="badge ${student.statusTone}">${student.status}</span></td>
                                <td>
                                    <div class="row-actions">
                                        <button type="button" class="row-action" title="编辑学生"
                                                data-student-edit
                                                data-id="${student.id}"
                                                data-student-no="${fn:escapeXml(student.studentNo)}"
                                                data-name="${fn:escapeXml(student.name)}"
                                                data-gender="${student.gender}"
                                                data-class-id="${student.classId}"
                                                data-phone="${fn:escapeXml(student.phone)}"
                                                data-email="${fn:escapeXml(student.email)}"
                                                data-status="${student.status}">
                                            <svg class="icon"><use href="#i-pencil"></use></svg>
                                        </button>
                                        <form method="post" action="${ctx}/students" style="display:inline;"
                                              data-confirm="删除后，该学生的登录账号和全部成绩记录也会一并删除，且无法恢复。"
                                              data-confirm-title="删除这位学生？"
                                              data-confirm-button="确认删除">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="${student.id}">
                                            <input type="hidden" name="returnTo" value="${fn:escapeXml(returnTo)}">
                                            <button type="submit" class="row-action danger" title="删除学生">
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
                        <div class="empty-icon"><svg class="icon"><use href="#i-users"></use></svg></div>
                        <h3>没有找到学生</h3>
                        <p>试试更换筛选条件，或者添加一位新学生。</p>
                        <button type="button" class="btn btn-primary btn-sm" data-student-add>
                            <svg class="icon"><use href="#i-plus"></use></svg> 新增学生
                        </button>
                    </div>
                </c:if>

                <c:if test="${data.total > 0}">
                    <div class="pagination">
                        <span class="pagination-info">
                            显示 <strong>${data.from}</strong>–<strong>${data.to}</strong>，共 <strong>${data.total}</strong> 条
                        </span>
                        <div class="pagination-btns">
                            <c:url var="prevUrl" value="/students">
                                <c:param name="classId" value="${classId}"/>
                                <c:param name="q" value="${keyword}"/>
                                <c:param name="page" value="${data.prevPage}"/>
                            </c:url>
                            <a class="page-btn ${data.hasPrev ? '' : 'disabled'}" href="${prevUrl}" aria-label="上一页">
                                <svg class="icon"><use href="#i-chevron-left"></use></svg>
                            </a>
                            <c:forEach var="p" begin="1" end="${data.totalPages}">
                                <c:url var="pageUrl" value="/students">
                                    <c:param name="classId" value="${classId}"/>
                                    <c:param name="q" value="${keyword}"/>
                                    <c:param name="page" value="${p}"/>
                                </c:url>
                                <a class="page-btn ${p == data.page ? 'active' : ''}" href="${pageUrl}">${p}</a>
                            </c:forEach>
                            <c:url var="nextUrl" value="/students">
                                <c:param name="classId" value="${classId}"/>
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

<div class="modal-backdrop" id="studentModal" aria-hidden="true">
    <div class="modal" role="dialog" aria-modal="true" aria-labelledby="studentModalTitle">
        <div class="modal-head">
            <div class="modal-head-icon"><svg class="icon"><use href="#i-users"></use></svg></div>
            <div>
                <h2 id="studentModalTitle">新增学生</h2>
                <p id="studentModalSubtitle">新学生默认使用学号登录，初始密码为 123456</p>
            </div>
            <button type="button" class="modal-close" data-close-modal aria-label="关闭">
                <svg class="icon"><use href="#i-x"></use></svg>
            </button>
        </div>
        <form id="studentForm" method="post" action="${ctx}/students">
            <div class="modal-body">
                <input type="hidden" name="action" value="save">
                <input type="hidden" name="id" value="">
                <input type="hidden" name="returnTo" value="${fn:escapeXml(returnTo)}">
                <div class="form-grid">
                    <div class="field">
                        <label class="field-label" for="studentName">姓名 <span class="req">*</span></label>
                        <input class="input" id="studentName" name="name" type="text" maxlength="50" required placeholder="例如：林知夏">
                    </div>
                    <div class="field">
                        <label class="field-label" for="studentNo">学号 <span class="req">*</span></label>
                        <input class="input" id="studentNo" name="studentNo" type="text" maxlength="20" required placeholder="4-20 位数字或字母">
                    </div>
                    <div class="field">
                        <label class="field-label" for="studentGender">性别</label>
                        <select class="select" id="studentGender" name="gender">
                            <option value="男">男</option>
                            <option value="女">女</option>
                        </select>
                    </div>
                    <div class="field">
                        <label class="field-label" for="studentClassId">所在班级 <span class="req">*</span></label>
                        <select class="select" id="studentClassId" name="classId" required>
                            <c:forEach var="item" items="${classes}">
                                <option value="${item.id}"><c:out value="${item.name}"/></option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="field">
                        <label class="field-label" for="studentPhone">手机号</label>
                        <input class="input" id="studentPhone" name="phone" type="tel" maxlength="11" placeholder="选填，11 位手机号">
                    </div>
                    <div class="field">
                        <label class="field-label" for="studentEmail">邮箱</label>
                        <input class="input" id="studentEmail" name="email" type="email" maxlength="120" placeholder="选填，用于联系">
                    </div>
                    <div class="field">
                        <label class="field-label" for="studentStatus">学籍状态</label>
                        <select class="select" id="studentStatus" name="status">
                            <option value="在读">在读</option>
                            <option value="休学">休学</option>
                            <option value="毕业">毕业</option>
                        </select>
                    </div>
                    <div class="field span-2">
                        <div class="hint-strip">
                            <svg class="icon"><use href="#i-info"></use></svg>
                            新增学生会同步生成登录账号：账号为学号，初始密码为 123456。修改学号会同步更新登录账号。
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-foot">
                <button type="button" class="btn btn-ghost" data-close-modal>取消</button>
                <button type="submit" class="btn btn-primary">
                    <svg class="icon"><use href="#i-save"></use></svg>
                    保存学生
                </button>
            </div>
        </form>
    </div>
</div>
</body>
</html>
