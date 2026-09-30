<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Đăng nhập hệ thống CRM Bán Hàng bảo mật & duy trì phiên an toàn">
    <title>Đăng Nhập - Hệ Thống CRM Bán Hàng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
</head>
<body>
<div class="auth-container">
    <div class="auth-card">
        <!-- Logo & Header -->
        <div class="auth-header">
            <div class="auth-logo-badge">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                </svg>
            </div>
            <h1>Hệ Thống CRM</h1>
            <p>Quản lý khách hàng & quy trình bán hàng</p>
        </div>

        <!-- AC3: Thông báo lỗi hoặc phiên làm việc hết hạn rõ ràng -->
        <c:if test="${not empty thongBaoLoi}">
            <c:set var="isSessionExpired" value="${fn:containsIgnoreCase(thongBaoLoi, 'hết hạn') or param.error eq 'session_expired'}" />
            <div class="alert ${isSessionExpired ? 'alert-warning' : 'alert-danger'}" id="alert-error" role="alert">
                <div class="alert-icon-wrapper">
                    <c:choose>
                        <c:when test="${isSessionExpired}">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <circle cx="12" cy="12" r="10"></circle>
                                <polyline points="12 6 12 12 16 14"></polyline>
                            </svg>
                        </c:when>
                        <c:otherwise>
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <circle cx="12" cy="12" r="10"></circle>
                                <line x1="12" y1="8" x2="12" y2="12"></line>
                                <line x1="12" y1="16" x2="12.01" y2="16"></line>
                            </svg>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="alert-body">
                    <div class="alert-title">
                        <c:out value="${isSessionExpired ? 'Phiên Làm Việc Đã Hết Hạn' : 'Đăng Nhập Thất Bại'}" />
                    </div>
                    <div><c:out value="${thongBaoLoi}" /></div>

                    <!-- Gợi ý bảo lưu bản nháp nếu phiên hết hạn -->
                    <div id="draft-saved-notice" class="draft-recovery-hint" style="display: none;">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path>
                        </svg>
                        <span>Ghi chú bạn đang soạn dở đã được lưu tạm an toàn và sẵn sàng sau khi đăng nhập.</span>
                    </div>
                </div>
            </div>
        </c:if>

        <!-- AC2: Thông báo đăng xuất an toàn -->
        <c:if test="${not empty thongBaoThanhCong}">
            <div class="alert alert-success" id="alert-success" role="alert">
                <div class="alert-icon-wrapper">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                        <polyline points="22 4 12 14.01 9 11.01"></polyline>
                    </svg>
                </div>
                <div class="alert-body">
                    <div class="alert-title">Đăng Xuất Thành Công</div>
                    <div><c:out value="${thongBaoThanhCong}" /></div>
                </div>
            </div>
        </c:if>

        <!-- Form Đăng nhập -->
        <form action="${pageContext.request.contextPath}/dang-nhap" method="post" id="login-form" autocomplete="on">
            <div class="form-group">
                <label for="email">Email công ty</label>
                <div class="input-wrapper">
                    <input type="email" id="email" name="email" class="form-control"
                           value="<c:out value='${email}' />"
                           placeholder="ten.nhanvien@crm.vn" required autofocus autocomplete="username">
                    <span class="input-icon-prefix">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                            <polyline points="22,6 12,13 2,6"></polyline>
                        </svg>
                    </span>
                </div>
            </div>

            <div class="form-group">
                <label for="matKhau">Mật khẩu</label>
                <div class="input-wrapper input-password-wrapper">
                    <input type="password" id="matKhau" name="matKhau" class="form-control"
                           placeholder="Nhập mật khẩu" required autocomplete="current-password">
                    <span class="input-icon-prefix">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                            <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                        </svg>
                    </span>
                    <button type="button" class="btn-toggle-password" id="btn-toggle-password" 
                            aria-label="Ẩn/hiện mật khẩu" title="Ẩn/hiện mật khẩu">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                            <circle cx="12" cy="12" r="3"></circle>
                        </svg>
                    </button>
                </div>
            </div>

            <button type="submit" class="btn-primary" id="btn-submit">
                <span>Đăng Nhập Hệ Thống</span>
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="5" y1="12" x2="19" y2="12"></line>
                    <polyline points="12 5 19 12 12 19"></polyline>
                </svg>
            </button>
        </form>

        <!-- Quick Demo Credentials -->
        <div class="demo-section">
            <span class="demo-title">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"></path>
                </svg>
                Tài khoản kiểm thử nhanh (Nhấp để điền)
            </span>
            <div class="quick-fill-chip" data-email="sales@crm.vn" data-password="123456@Aa">
                <span style="font-family: monospace;">sales@crm.vn</span>
                <span style="font-size: 11px; font-weight: 600; color: var(--primary);">NVKD (Sales)</span>
            </div>
            <div class="quick-fill-chip" data-email="admin@crm.vn" data-password="123456@Aa">
                <span style="font-family: monospace;">admin@crm.vn</span>
                <span style="font-size: 11px; font-weight: 600; color: var(--primary);">Quản Trị Viên</span>
            </div>
        </div>

        <div class="auth-footer">
            <p>Phiên đăng nhập được bảo vệ và tự động gia hạn khi có hoạt động.</p>
        </div>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function () {
        // Toggle ẩn/hiện mật khẩu
        const toggleBtn = document.getElementById('btn-toggle-password');
        const pwdInput = document.getElementById('matKhau');
        if (toggleBtn && pwdInput) {
            toggleBtn.addEventListener('click', function () {
                const isPwd = pwdInput.getAttribute('type') === 'password';
                pwdInput.setAttribute('type', isPwd ? 'text' : 'password');
                toggleBtn.innerHTML = isPwd ? 
                    '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>' :
                    '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>';
            });
        }

        // Quick fill
        const chips = document.querySelectorAll('.quick-fill-chip');
        const emailInput = document.getElementById('email');
        chips.forEach(function (c) {
            c.addEventListener('click', function () {
                const email = c.getAttribute('data-email');
                const pwd = c.getAttribute('data-password');
                if (emailInput) emailInput.value = email;
                if (pwdInput) pwdInput.value = pwd;
                if (pwdInput) pwdInput.focus();
            });
        });

        // Kiểm tra xem có bản nháp trong localStorage không để thông báo an tâm
        const savedDraft = localStorage.getItem('crm_ban_nhap_ghi_chu');
        const noticeEl = document.getElementById('draft-saved-notice');
        if (savedDraft && savedDraft.trim() && noticeEl) {
            noticeEl.style.display = 'flex';
        }
    });
</script>
</body>
</html>
