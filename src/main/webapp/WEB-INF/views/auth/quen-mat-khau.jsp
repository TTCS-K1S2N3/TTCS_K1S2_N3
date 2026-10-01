<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quên Mật Khẩu | CRM Bán Hàng</title>
    <meta name="description" content="Khôi phục quyền truy cập tài khoản CRM bán hàng qua email an toàn, nhận liên kết đặt lại mật khẩu có hiệu lực 30 phút.">

    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">

    <!-- CSS Module -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth/auth.css">
</head>
<body class="crm-auth-body">

    <!-- Topbar Brand -->
    <header class="auth-topbar">
        <a href="${pageContext.request.contextPath}/" class="auth-brand" id="brand-link">
            <span class="brand-icon">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
                    <circle cx="9" cy="7" r="4"></circle>
                    <path d="M22 21v-2a4 4 0 0 0-3-3.87"></path>
                    <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                </svg>
            </span>
            <span>CRM Bán Hàng</span>
        </a>
        <span class="brand-badge">Bảo mật</span>
    </header>

    <!-- Main Container -->
    <main class="auth-main">
        <div class="auth-container">
            <div class="auth-card">

                <!-- Header / Logo -->
                <div class="auth-header">
                    <div class="auth-icon-circle" aria-hidden="true">
                        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                            <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                        </svg>
                    </div>
                    <h1 class="auth-title" id="page-title">Quên Mật Khẩu</h1>
                    <p class="auth-subtitle">
                        Nhập email công ty đã đăng ký để nhận liên kết khôi phục tài khoản khi đang đi gặp khách hàng hoặc công tác.
                    </p>
                </div>

                <!-- Security Feature Notice -->
                <div class="security-notice-pill" aria-label="Thông tin hiệu lực">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="12" cy="12" r="10"></circle>
                        <polyline points="12 6 12 12 16 14"></polyline>
                    </svg>
                    <span>Liên kết có hiệu lực <strong>30 phút</strong> &bull; Dùng <strong>1 lần duy nhất</strong></span>
                </div>

                <!-- AC3: Thông báo thành công (Hiển thị cùng một thông báo dù email có tồn tại hay không) -->
                <% if (request.getAttribute("thongBaoThanhCong") != null) { %>
                    <div class="email-sent-card" id="email-sent-success-box" role="status">
                        <div class="email-sent-header">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                                <polyline points="22 4 12 14.01 9 11.01"></polyline>
                            </svg>
                            <span>Yêu cầu đã được tiếp nhận!</span>
                        </div>
                        <p class="email-sent-desc">
                            <%= request.getAttribute("thongBaoThanhCong") %>
                        </p>
                        <ul class="email-sent-instructions">
                            <li>Kiểm tra cả thư mục <strong>Hộp thư đến</strong> và <strong>Spam / Thư rác</strong>.</li>
                            <li>Nhấp vào liên kết trong thư để đặt mật khẩu mới trong vòng <strong>30 phút</strong>.</li>
                            <li>Liên kết bảo mật chỉ sử dụng được <strong>một lần duy nhất</strong>.</li>
                        </ul>
                        <div class="resend-box">
                            <span>Chưa nhận được email?</span>
                            <button type="button" class="resend-link" id="btn-resend-countdown" style="background:none;border:none;padding:0;">
                                Gửi lại sau (60s)
                            </button>
                        </div>
                    </div>
                <% } %>

                <!-- Thông báo lỗi nghiệp vụ / định dạng -->
                <% if (request.getAttribute("thongBaoLoi") != null) { %>
                    <div class="alert-message alert-danger" id="alert-error" role="alert">
                        <span class="alert-icon-col" aria-hidden="true">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <circle cx="12" cy="12" r="10"></circle>
                                <line x1="12" y1="8" x2="12" y2="12"></line>
                                <line x1="12" y1="16" x2="12.01" y2="16"></line>
                            </svg>
                        </span>
                        <div class="alert-body">
                            <strong>Đã có lỗi xảy ra</strong>
                            <span><%= request.getAttribute("thongBaoLoi") %></span>
                        </div>
                    </div>
                <% } %>

                <!-- Form Gửi Yêu Cầu Đặt Lại Mật Khẩu (AC 1) -->
                <form action="${pageContext.request.contextPath}/quen-mat-khau" method="post" id="form-quen-mat-khau" novalidate autocomplete="off">
                    <div class="form-group">
                        <label for="email" class="form-label">
                            Email công ty của bạn <span style="color:var(--danger)">*</span>
                        </label>
                        <div class="input-wrapper">
                            <span class="input-icon-left" aria-hidden="true">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                                    <polyline points="22,6 12,13 2,6"></polyline>
                                </svg>
                            </span>
                            <input 
                                type="email" 
                                id="email" 
                                name="email" 
                                class="form-control no-icon-right" 
                                placeholder="name@crmbanhang.vn" 
                                value="${emailNhapLai != null ? emailNhapLai : ''}"
                                required 
                                autofocus
                                autocomplete="email"
                                aria-required="true"
                                aria-describedby="hint-email err-email"
                            >
                        </div>
                        <span id="hint-email" class="field-hint">Hệ thống sẽ gửi mã liên kết bảo mật tới hộp thư này.</span>
                        <div id="err-email" class="field-error" aria-live="polite"></div>
                    </div>

                    <button type="submit" class="btn-primary" id="btn-gui-yeu-cau">
                        <span class="btn-spinner d-none" id="btn-spinner" aria-hidden="true"></span>
                        <span id="btn-icon" aria-hidden="true">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                                <line x1="22" y1="2" x2="11" y2="13"></line>
                                <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
                            </svg>
                        </span>
                        <span id="btn-text">Gửi Liên Kết Đặt Lại Mật Khẩu</span>
                    </button>
                </form>

                <!-- Footer Links -->
                <div class="auth-card-footer">
                    <a href="${pageContext.request.contextPath}/dang-nhap" id="link-quay-lai-dang-nhap">
                        &larr; Quay lại trang đăng nhập
                    </a>
                </div>

            </div>
        </div>
    </main>

    <!-- Page Footer -->
    <footer class="crm-auth-page-footer">
        <p>&copy; 2026 CRM Bán Hàng - Nhóm 10. Bảo vệ dữ liệu khách hàng tuyệt đối.</p>
    </footer>

    <!-- Script Module -->
    <script src="${pageContext.request.contextPath}/assets/js/auth/quen-mat-khau.js"></script>
</body>
</html>
