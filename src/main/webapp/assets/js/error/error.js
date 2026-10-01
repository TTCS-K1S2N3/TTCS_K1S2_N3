/**
 * CRM BÁN HÀNG - ERROR PAGES INTERACTION SCRIPT (Story S1-07)
 * Xử lý tương tác giao diện người dùng trên các trang báo lỗi
 */

(function () {
    "use strict";

    document.addEventListener("DOMContentLoaded", function () {
        initMobileMenu();
        initTechnicalDetailsToggle();
        initCopyButtons();
        initSmartBackButtons();
    });

    /**
     * Khởi tạo toggle menu cho giao diện thiết bị di động
     */
    function initMobileMenu() {
        const toggleBtn = document.getElementById("btn-mobile-menu-toggle");
        const drawer = document.getElementById("mobile-menu-drawer");

        if (toggleBtn && drawer) {
            toggleBtn.addEventListener("click", function () {
                const isExpanded = drawer.classList.toggle("open");
                toggleBtn.setAttribute("aria-expanded", isExpanded ? "true" : "false");
            });
        }
    }

    /**
     * Khởi tạo tính năng ẩn/hiện thông tin kỹ thuật phục vụ tra cứu
     */
    function initTechnicalDetailsToggle() {
        const toggleBtn = document.getElementById("btn-toggle-tech-details");
        const panel = document.getElementById("tech-details-panel");

        if (toggleBtn && panel) {
            toggleBtn.addEventListener("click", function () {
                const isExpanded = panel.classList.toggle("expanded");
                toggleBtn.setAttribute("aria-expanded", isExpanded ? "true" : "false");
                const arrow = toggleBtn.querySelector(".toggle-arrow");
                if (arrow) {
                    arrow.textContent = isExpanded ? "▲" : "▼";
                }
            });
        }
    }

    /**
     * Khởi tạo tính năng sao chép mã sự cố vào Clipboard
     */
    function initCopyButtons() {
        const copyBtn = document.getElementById("btn-copy-error-code");
        if (!copyBtn) return;

        copyBtn.addEventListener("click", function () {
            const errorCode = copyBtn.getAttribute("data-error-code");
            if (!errorCode) return;

            if (navigator.clipboard && navigator.clipboard.writeText) {
                navigator.clipboard.writeText(errorCode)
                    .then(function () {
                        showToast("Đã sao chép mã sự cố: " + errorCode);
                    })
                    .catch(function () {
                        fallbackCopy(errorCode);
                    });
            } else {
                fallbackCopy(errorCode);
            }
        });
    }

    /**
     * Cơ chế sao chép dự phòng cho trình duyệt cũ
     */
    function fallbackCopy(text) {
        const textArea = document.createElement("textarea");
        textArea.value = text;
        textArea.style.position = "fixed";
        textArea.style.left = "-999999px";
        document.body.appendChild(textArea);
        textArea.focus();
        textArea.select();
        try {
            document.execCommand("copy");
            showToast("Đã sao chép mã sự cố: " + text);
        } catch (err) {
            alert("Mã sự cố: " + text);
        }
        document.body.removeChild(textArea);
    }

    /**
     * Hiển thị thông báo Toast nổi
     */
    function showToast(message) {
        let toast = document.getElementById("crm-toast-notification");
        if (!toast) {
            toast = document.createElement("div");
            toast.id = "crm-toast-notification";
            toast.className = "crm-toast";
            document.body.appendChild(toast);
        }

        toast.innerHTML = "<span>📋</span><span>" + message + "</span>";
        toast.classList.add("show");

        setTimeout(function () {
            toast.classList.remove("show");
        }, 3200);
    }

    /**
     * Xử lý quay lại trang trước thông minh (nếu không có lịch sử thì về trang chủ)
     */
    function initSmartBackButtons() {
        const backButtons = document.querySelectorAll(".btn-smart-back");
        backButtons.forEach(function (btn) {
            btn.addEventListener("click", function (event) {
                event.preventDefault();
                const fallbackUrl = btn.getAttribute("data-fallback-url") || "/";
                if (window.history && window.history.length > 1) {
                    window.history.back();
                } else {
                    window.location.href = fallbackUrl;
                }
            });
        });
    }
})();
