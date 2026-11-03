/* 知衡 · 班级学生成绩管理系统 — 公共交互 */
(function () {
    "use strict";

    const $ = (selector, root) => (root || document).querySelector(selector);
    const $$ = (selector, root) => Array.from((root || document).querySelectorAll(selector));

    /* ---------- Sidebar ---------- */
    function initSidebar() {
        const toggle = $("#menuToggle");
        const overlay = $("#sidebarOverlay");
        if (!toggle) return;
        const close = () => document.body.classList.remove("sidebar-open");
        toggle.addEventListener("click", () => {
            document.body.classList.toggle("sidebar-open");
        });
        if (overlay) overlay.addEventListener("click", close);
        $$(".sidebar .nav-item").forEach(link => link.addEventListener("click", close));
        window.addEventListener("resize", () => {
            if (window.innerWidth > 1080) close();
        });
        document.addEventListener("keydown", event => {
            if (event.key === "Escape") close();
        });
    }

    /* ---------- User dropdown ---------- */
    function initUserMenu() {
        const menu = $(".user-menu");
        if (!menu) return;
        const trigger = $(".user-trigger", menu);
        trigger.addEventListener("click", event => {
            event.stopPropagation();
            menu.classList.toggle("open");
        });
        document.addEventListener("click", () => menu.classList.remove("open"));
        document.addEventListener("keydown", event => {
            if (event.key === "Escape") menu.classList.remove("open");
        });
    }

    /* ---------- Alerts ---------- */
    function initAlerts() {
        $$("[data-alert]").forEach(alert => {
            const close = $(".alert-close", alert);
            if (close) {
                close.addEventListener("click", () => {
                    alert.style.opacity = "0";
                    alert.style.transform = "translateY(-4px)";
                    setTimeout(() => alert.remove(), 180);
                });
            }
            if (alert.dataset.autohide !== "false") {
                setTimeout(() => {
                    if (!alert.isConnected) return;
                    alert.style.transition = "opacity .35s, transform .35s";
                    alert.style.opacity = "0";
                    alert.style.transform = "translateY(-4px)";
                    setTimeout(() => alert.remove(), 380);
                }, 6000);
            }
        });
    }

    /* ---------- Modals ---------- */
    let lastFocused = null;

    function openModal(modal) {
        if (!modal) return;
        lastFocused = document.activeElement;
        modal.classList.add("is-open");
        modal.setAttribute("aria-hidden", "false");
        document.body.classList.add("modal-open");
        const focusable = modal.querySelector("input:not([type=hidden]), select, textarea, button");
        if (focusable) setTimeout(() => focusable.focus(), 120);
    }

    function closeModal(modal) {
        if (!modal) return;
        modal.classList.remove("is-open");
        modal.setAttribute("aria-hidden", "true");
        document.body.classList.remove("modal-open");
        if (lastFocused) lastFocused.focus();
    }

    function initModals() {
        $$("[data-open-modal]").forEach(trigger => {
            trigger.addEventListener("click", event => {
                event.preventDefault();
                openModal(document.getElementById(trigger.dataset.openModal));
            });
        });
        $$("[data-close-modal]").forEach(trigger => {
            trigger.addEventListener("click", event => {
                event.preventDefault();
                closeModal(trigger.closest(".modal-backdrop"));
            });
        });
        $$(".modal-backdrop").forEach(backdrop => {
            backdrop.addEventListener("mousedown", event => {
                if (event.target === backdrop) closeModal(backdrop);
            });
        });
        document.addEventListener("keydown", event => {
            if (event.key === "Escape") {
                const open = $(".modal-backdrop.is-open");
                if (open) closeModal(open);
            }
        });
    }

    /* ---------- Confirm dialog ---------- */
    function initConfirm() {
        const dialog = $("#confirmDialog");
        if (!dialog) return;
        const title = $("#confirmTitle", dialog);
        const message = $("#confirmMessage", dialog);
        const confirmButton = $("#confirmSubmit", dialog);
        const cancelButton = $("#confirmCancel", dialog);
        let pendingForm = null;

        function close() {
            dialog.classList.remove("is-open");
            dialog.setAttribute("aria-hidden", "true");
            document.body.classList.remove("modal-open");
            pendingForm = null;
        }

        $$("form[data-confirm]").forEach(form => {
            form.addEventListener("submit", event => {
                if (form.dataset.confirmed === "true") return;
                event.preventDefault();
                pendingForm = form;
                title.textContent = form.dataset.confirmTitle || "确认执行此操作？";
                message.textContent = form.dataset.confirm || "此操作完成后可能无法撤销，请确认。";
                confirmButton.textContent = form.dataset.confirmButton || "确认删除";
                dialog.classList.add("is-open");
                dialog.setAttribute("aria-hidden", "false");
                document.body.classList.add("modal-open");
                setTimeout(() => confirmButton.focus(), 80);
            });
        });

        confirmButton.addEventListener("click", () => {
            if (!pendingForm) return;
            pendingForm.dataset.confirmed = "true";
            const form = pendingForm;
            close();
            form.submit();
        });
        cancelButton.addEventListener("click", close);
        dialog.addEventListener("mousedown", event => {
            if (event.target === dialog) close();
        });
        document.addEventListener("keydown", event => {
            if (event.key === "Escape" && dialog.classList.contains("is-open")) close();
        });
    }

    /* ---------- Student modal ---------- */
    function initStudentModal() {
        const modal = $("#studentModal");
        if (!modal) return;
        const form = $("#studentForm", modal);
        const title = $("#studentModalTitle", modal);
        const subtitle = $("#studentModalSubtitle", modal);
        const idField = form.querySelector("[name=id]");

        $$("[data-student-edit]").forEach(button => {
            button.addEventListener("click", () => {
                const d = button.dataset;
                form.reset();
                idField.value = d.id || "";
                form.querySelector("[name=studentNo]").value = d.studentNo || "";
                form.querySelector("[name=name]").value = d.name || "";
                form.querySelector("[name=gender]").value = d.gender || "男";
                form.querySelector("[name=classId]").value = d.classId || "";
                form.querySelector("[name=phone]").value = d.phone || "";
                form.querySelector("[name=email]").value = d.email || "";
                form.querySelector("[name=status]").value = d.status || "在读";
                title.textContent = "编辑学生档案";
                subtitle.textContent = "修改后将同步更新该学生的登录姓名与学号";
                openModal(modal);
            });
        });

        const addButton = $("[data-student-add]");
        if (addButton) {
            addButton.addEventListener("click", () => {
                form.reset();
                idField.value = "";
                form.querySelector("[name=status]").value = "在读";
                title.textContent = "新增学生";
                subtitle.textContent = "新学生默认使用学号登录，初始密码为 123456";
                openModal(modal);
            });
        }
    }

    /* ---------- Class modal ---------- */
    function initClassModal() {
        const modal = $("#classModal");
        if (!modal) return;
        const form = $("#classForm", modal);
        const title = $("#classModalTitle", modal);
        const subtitle = $("#classModalSubtitle", modal);
        const idField = form.querySelector("[name=id]");

        $$("[data-class-edit]").forEach(button => {
            button.addEventListener("click", () => {
                const d = button.dataset;
                form.reset();
                idField.value = d.id || "";
                form.querySelector("[name=name]").value = d.name || "";
                form.querySelector("[name=gradeYear]").value = d.gradeYear || "";
                form.querySelector("[name=major]").value = d.major || "";
                form.querySelector("[name=advisor]").value = d.advisor || "";
                title.textContent = "编辑班级";
                subtitle.textContent = "修改班级信息不会影响已录入的成绩";
                openModal(modal);
            });
        });

        const addButton = $("[data-class-add]");
        if (addButton) {
            addButton.addEventListener("click", () => {
                form.reset();
                idField.value = "";
                title.textContent = "新增班级";
                subtitle.textContent = "建立班级后，就可以把学生归入这个班级";
                openModal(modal);
            });
        }
    }

    /* ---------- Course modal ---------- */
    function initCourseModal() {
        const modal = $("#courseModal");
        if (!modal) return;
        const form = $("#courseForm", modal);
        const title = $("#courseModalTitle", modal);
        const subtitle = $("#courseModalSubtitle", modal);
        const idField = form.querySelector("[name=id]");

        $$("[data-course-edit]").forEach(button => {
            button.addEventListener("click", () => {
                const d = button.dataset;
                form.reset();
                idField.value = d.id || "";
                form.querySelector("[name=code]").value = d.code || "";
                form.querySelector("[name=name]").value = d.name || "";
                form.querySelector("[name=credit]").value = d.credit || "3";
                form.querySelector("[name=teacher]").value = d.teacher || "";
                form.querySelector("[name=semester]").value = d.semester || "2026-2027 秋";
                const color = form.querySelector("[name=color][value='" + (d.color || "indigo") + "']");
                if (color) color.checked = true;
                title.textContent = "编辑课程";
                subtitle.textContent = "课程代码需保持唯一，已有成绩的课程不能删除";
                openModal(modal);
            });
        });

        const addButton = $("[data-course-add]");
        if (addButton) {
            addButton.addEventListener("click", () => {
                form.reset();
                idField.value = "";
                form.querySelector("[name=credit]").value = "3";
                form.querySelector("[name=semester]").value = "2026-2027 秋";
                const color = form.querySelector("[name=color][value='indigo']");
                if (color) color.checked = true;
                title.textContent = "新增课程";
                subtitle.textContent = "课程建立后，即可在成绩录入中选择";
                openModal(modal);
            });
        }
    }

    /* ---------- Grade edit modal ---------- */
    function initGradeModal() {
        const modal = $("#gradeModal");
        if (!modal) return;
        const form = $("#gradeForm", modal);
        $$("[data-grade-edit]").forEach(button => {
            button.addEventListener("click", () => {
                const d = button.dataset;
                form.querySelector("[name=id]").value = d.id || "";
                form.querySelector("[name=score]").value = d.score || "";
                const reason = form.querySelector("[name=reason]");
                if (reason) reason.value = "";
                $("#gradeStudent", modal).textContent = d.student || "";
                $("#gradeMeta", modal).textContent = (d.course || "") + " · " + (d.exam || "");
                $("#gradeCurrent", modal).textContent = d.score || "—";
                openModal(modal);
            });
        });
    }

    /* ---------- Login ---------- */
    function initLogin() {
        const password = $("#password");
        const toggle = $("#togglePassword");
        if (toggle && password) {
            toggle.addEventListener("click", () => {
                const visible = password.type === "text";
                password.type = visible ? "password" : "text";
                toggle.innerHTML = visible
                    ? '<svg class="icon"><use href="#i-eye"></use></svg>'
                    : '<svg class="icon"><use href="#i-eye-off"></use></svg>';
                toggle.setAttribute("aria-label", visible ? "显示密码" : "隐藏密码");
            });
        }
        $$("[data-demo-user]").forEach(button => {
            button.addEventListener("click", () => {
                const username = $("#username");
                if (!username || !password) return;
                username.value = button.dataset.demoUser;
                password.value = button.dataset.demoPassword;
                password.type = "password";
                username.focus();
                $$(".demo-account").forEach(item => item.classList.remove("selected"));
                button.classList.add("selected");
            });
        });
        const caps = $("#capsLockHint");
        if (caps && password) {
            password.addEventListener("keyup", event => {
                if (typeof event.getModifierState !== "function") return;
                caps.hidden = !event.getModifierState("CapsLock");
            });
        }
    }

    /* ---------- Entry form ---------- */
    function initEntryForm() {
        const form = $("#entryForm");
        if (!form) return;
        const inputs = $$(".entry-score-input", form).filter(input => !input.disabled);
        const filledEl = $("#entryFilled");
        const avgEl = $("#entryAverage");
        const passEl = $("#entryPassRate");
        const saveCountEl = $("#saveBarCount");
        const bar = $(".save-bar");
        let dirty = false;
        const initial = new Map(inputs.map(input => [input, input.value]));

        function values() {
            return inputs.map(input => {
                const raw = input.value.trim();
                if (raw === "") return null;
                const value = Number(raw);
                return Number.isFinite(value) ? value : null;
            });
        }

        function refresh() {
            let filled = 0;
            let sum = 0;
            let passed = 0;
            inputs.forEach(input => {
                const raw = input.value.trim();
                const value = Number(raw);
                const valid = raw === "" || (Number.isFinite(value) && value >= 0 && value <= 100);
                input.classList.toggle("invalid", !valid);
                input.classList.toggle("filled", raw !== "" && valid);
                if (raw !== "") {
                    filled++;
                    if (Number.isFinite(value)) {
                        sum += value;
                        if (value >= 60) passed++;
                    }
                }
            });
            if (filledEl) filledEl.textContent = String(filled);
            if (saveCountEl) saveCountEl.textContent = String(filled);
            if (avgEl) avgEl.textContent = filled ? (sum / filled).toFixed(1) : "—";
            if (passEl) passEl.textContent = filled ? ((passed / filled) * 100).toFixed(1) + "%" : "—";
            if (bar) bar.classList.toggle("has-changes", dirty);
        }

        inputs.forEach((input, index) => {
            input.addEventListener("input", () => {
                dirty = true;
                refresh();
            });
            input.addEventListener("keydown", event => {
                if (event.key === "Enter") {
                    event.preventDefault();
                    const next = inputs[index + 1];
                    if (next) {
                        next.focus();
                        next.select();
                    } else {
                        input.blur();
                    }
                }
                if (event.key === "ArrowDown") {
                    event.preventDefault();
                    const next = inputs[index + 1];
                    if (next) next.focus();
                }
                if (event.key === "ArrowUp") {
                    event.preventDefault();
                    const prev = inputs[index - 1];
                    if (prev) prev.focus();
                }
            });
            input.addEventListener("blur", () => {
                if (input.value.trim() !== "") {
                    const value = Number(input.value);
                    if (Number.isFinite(value)) {
                        input.value = value.toFixed(1).replace(/\.0$/, "");
                    }
                }
            });
        });

        form.addEventListener("submit", () => {
            dirty = false;
            window.removeEventListener("beforeunload", beforeUnload);
        });

        function beforeUnload(event) {
            if (!dirty) return;
            event.preventDefault();
            event.returnValue = "";
        }
        window.addEventListener("beforeunload", beforeUnload);

        const reset = $("#resetScores");
        if (reset) {
            reset.addEventListener("click", () => {
                inputs.forEach(input => {
                    input.value = initial.get(input) || "";
                });
                dirty = false;
                refresh();
            });
        }
        refresh();
    }

    /* ---------- Password strength ---------- */
    function initPasswordStrength() {
        const input = $("#newPassword");
        const meter = $("#passwordStrength");
        if (!input || !meter) return;
        const bars = $$("span", meter);
        input.addEventListener("input", () => {
            const value = input.value;
            let score = 0;
            if (value.length >= 6) score++;
            if (value.length >= 10) score++;
            if (/[A-Za-z]/.test(value) && /\d/.test(value)) score++;
            if (/[^A-Za-z0-9]/.test(value)) score++;
            bars.forEach((bar, index) => {
                bar.className = "";
                if (index < score) {
                    bar.classList.add(score <= 1 ? "active-weak" : score <= 2 ? "active-medium" : "active-strong");
                }
            });
        });
    }

    /* ---------- Misc ---------- */
    function initMisc() {
        $$("[data-auto-submit]").forEach(select => {
            select.addEventListener("change", () => select.form.submit());
        });
        $$("[data-print]").forEach(button => {
            button.addEventListener("click", () => window.print());
        });
        $$(".file-drop input[type=file]").forEach(input => {
            input.addEventListener("change", () => {
                const drop = input.closest(".file-drop");
                const label = drop ? drop.querySelector("strong") : null;
                if (!drop) return;
                if (input.files && input.files.length) {
                    drop.classList.add("has-file");
                    if (label) label.textContent = input.files[0].name;
                } else {
                    drop.classList.remove("has-file");
                    if (label) label.textContent = "选择 Excel 文件";
                }
            });
        });
        document.addEventListener("keydown", event => {
            if (event.key !== "/" || event.ctrlKey || event.metaKey || event.altKey) return;
            const tag = (document.activeElement.tagName || "").toLowerCase();
            if (tag === "input" || tag === "textarea" || tag === "select") return;
            const search = $(".filter-card .search-field input");
            if (search) {
                event.preventDefault();
                search.focus();
            }
        });
        // Prevent double submits on normal forms.
        $$("form").forEach(form => {
            form.addEventListener("submit", event => {
                if (event.defaultPrevented) return;
                const buttons = $$("button[type=submit]", form);
                buttons.forEach(button => {
                    setTimeout(() => {
                        button.disabled = true;
                        button.classList.add("is-loading");
                    }, 10);
                });
            });
        });
    }

    document.addEventListener("DOMContentLoaded", () => {
        initSidebar();
        initUserMenu();
        initAlerts();
        initModals();
        initConfirm();
        initStudentModal();
        initClassModal();
        initCourseModal();
        initGradeModal();
        initLogin();
        initEntryForm();
        initPasswordStrength();
        initMisc();
    });
})();
