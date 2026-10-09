<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đặt Lại Mật Khẩu | CRM Bán Hàng</title>
    <meta name="description" content="Thiết lập mật khẩu mới cho tài khoản CRM của bạn thông qua liên kết xác thực email 30 phút.">

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

                <% 
                    Boolean tokenHopLe = (Boolean) request.getAttribute("tokenHopLe");
                    Boolean datLaiThanhCong = (Boolean) request.getAttribute("datLaiThanhCong");
                    String trangThaiToken = (String) request.getAttribute("trangThaiToken");
                %>

                <!-- =======================================================
                     TRƯỜNG HỢP 1: ĐẶT LẠI MẬT KHẨU THÀNH CÔNG
                     ======================================================= -->
                <% if (Boolean.TRUE.equals(datLaiThanhCong)) { %>
                    <div class="success-card-celebrate">
                        <div class="celebrate-icon" aria-hidden="true">
                            <span class="material-symbols-outlined icon-2xl">check_circle</span>
                        </div>
                        <h2>Đặt Lại Mật Khẩu Thành Công!</h2>
                        <p>
                            <%= request.getAttribute("thongBaoThanhCong") != null 
                                ? request.getAttribute("thongBaoThanhCong") 
                                : "Mật khẩu của bạn đã được cập nhật thành công. Bạn có thể sử dụng mật khẩu mới để đăng nhập ngay bây giờ." %>
                        </p>
                        <a href="${pageContext.request.contextPath}/dang-nhap" class="btn-primary" id="btn-dang-nhap-ngay">
                            Đăng Nhập Ngay
                        </a>
                    </div>

                <!-- =======================================================
                     TRƯỜNG HỢP 2: TOKEN HỢP LỆ -> HIỂN THỊ FORM ĐỔI MẬT KHẨU
                     ======================================================= -->
                <% } else if (Boolean.TRUE.equals(tokenHopLe)) { %>

                    <div class="auth-header">
                        <div class="auth-icon-circle" aria-hidden="true">
                            <span class="material-symbols-outlined icon-2xl">key</span>
                        </div>
                        <h1 class="auth-title">Thiết Lập Mật Khẩu Mới</h1>
                        <p class="auth-subtitle">
                            Nhập mật khẩu mới đáp ứng các tiêu chuẩn bảo mật hệ thống để bảo vệ danh mục khách hàng của bạn.
                        </p>
                    </div>

                    <!-- Security Notice Pill: AC1 (30 mins) & AC2 (1-time use) -->
                    <div class="security-notice-pill" aria-label="Thời hạn hiệu lực">
                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">schedule</span>
                        <span>Hiệu lực <strong>30 phút</strong> &bull; Dùng <strong>1 lần duy nhất</strong></span>
                    </div>

                    <!-- Thông báo lỗi khi cập nhật thất bại -->
                    <% if (request.getAttribute("thongBaoLoi") != null) { %>
                        <div class="alert-message alert-danger" role="alert">
                            <span class="alert-icon-col" aria-hidden="true">
                                <span class="material-symbols-outlined icon-sm">error</span>
                            </span>
                            <div class="alert-body">
                                <strong>Không thể đặt lại mật khẩu</strong>
                                <span><%= request.getAttribute("thongBaoLoi") %></span>
                            </div>
                        </div>
                    <% } %>

                    <form action="${pageContext.request.contextPath}/dat-lai-mat-khau" method="post" id="form-dat-lai-mat-khau" novalidate autocomplete="off">
                        <!-- Hidden Token Input -->
                        <input type="hidden" name="token" id="token" value="<%= request.getAttribute("token") != null ? request.getAttribute("token") : "" %>">

                        <!-- Mật khẩu mới -->
                                           <div class="input-wrapper">
                                <span class="input-icon-left" aria-hidden="true">
                                    <span class="material-symbols-outlined icon-sm">lock</span>
                                </span>
                                <input 
                                    type="password" 
                                    id="matKhauMoi" 
                                    name="matKhauMoi" 
                                    class="form-control" 
                                    placeholder="Tối thiểu 8 ký tự, có chữ và số" 
                                    required 
                                    minlength="8"
                                    autocomplete="new-password"
                                    aria-required="true"
                                    aria-describedby="err-mat-khau-moi checklist-mat-khau-moi"
                                >
                                <button 
                                    type="button" 
                                    id="btn-toggle-mat-khau-moi" 
                                    class="btn-toggle-pwd" 
                                    aria-label="Hiện mật khẩu mới" 
                                    data-target="matKhauMoi"
                                    tabindex="0"
                                >
                                    <span class="material-symbols-outlined eye-open icon-sm" aria-hidden="true">visibility</span>
                                    <span class="material-symbols-outlined eye-closed d-none icon-sm" aria-hidden="true">visibility_off</span>
                                </button>
                            </div>
                            <div id="err-mat-khau-moi" class="field-error" aria-live="polite"></div>

                            <!-- Password Strength Meter -->
                            <div class="strength-meter-container" aria-live="polite">
                                <div class="strength-header">
                                    <span class="strength-title">Độ mạnh:</span>
                                    <span class="strength-level" id="strength-text">Chưa nhập</span>
                                </div>
                                <div class="strength-bars" aria-hidden="true">
                                    <span class="bar" id="bar-1"></span>
                                    <span class="bar" id="bar-2"></span>
                                    <span class="bar" id="bar-3"></span>
                                    <span class="bar" id="bar-4"></span>
                                </div>
                            </div>

                            <!-- Password Requirement Checklist -->
                            <div class="password-checklist-card" id="checklist-mat-khau-moi">
                                <span class="checklist-heading">Tiêu chuẩn mật khẩu:</span>
                                <ul class="checklist-items">
                                    <li class="checklist-item" id="rule-min-length">
                                        <span class="rule-icon" aria-hidden="true"><span class="material-symbols-outlined icon-xs">close</span></span>
                                        <span>Tối thiểu <strong>8 ký tự</strong></span>
                                    </li>
                                    <li class="checklist-item" id="rule-has-letter">
                                        <span class="rule-icon" aria-hidden="true"><span class="material-symbols-outlined icon-xs">close</span></span>
                                        <span>Chứa ít nhất <strong>1 chữ cái</strong> (a-z, A-Z)</span>
                                    </li>
                                    <li class="checklist-item" id="rule-has-number">
                                        <span class="rule-icon" aria-hidden="true"><span class="material-symbols-outlined icon-xs">close</span></span>
                                        <span>Chứa ít nhất <strong>1 chữ số</strong> (0-9)</span>
                                    </li>
                                </ul>
                            </div>
                        </div>

                        <!-- Xác nhận mật khẩu mới -->
                        <div class="form-group">
                            <label for="xacNhanMatKhau" class="form-label">
                                Xác nhận mật khẩu mới <span style="color:var(--danger)">*</span>
                            </label>
                            <div class="input-wrapper">
                                <span class="input-icon-left" aria-hidden="true">
                                    <span class="material-symbols-outlined icon-sm">lock_clock</span>
                                </span>
                                <input 
                                    type="password" 
                                    id="xacNhanMatKhau" 
                                    name="xacNhanMatKhau" 
                                    class="form-control" 
                                    placeholder="Nhập lại mật khẩu mới" 
                                    required 
                                    minlength="8"
                                    autocomplete="new-password"
                                    aria-required="true"
                                    aria-describedby="err-xac-nhan-mat-khau match-status"
                                >
                                <button 
                                    type="button" 
                                    id="btn-toggle-xac-nhan-mat-khau" 
                                    class="btn-toggle-pwd" 
                                    aria-label="Hiện xác nhận mật khẩu mới" 
                                    data-target="xacNhanMatKhau"
                                    tabindex="0"
                                >
                                    <span class="material-symbols-outlined eye-open icon-sm" aria-hidden="true">visibility</span>
                                    <span class="material-symbols-outlined eye-closed d-none icon-sm" aria-hidden="true">visibility_off</span>
                                </button>
                            </div>
                            <div id="match-status" class="match-status" aria-live="polite"></div>
                            <div id="err-xac-nhan-mat-khau" class="field-error" aria-live="polite"></div>
                        </div>

                        <button type="submit" class="btn-primary" id="btn-xac-nhan-doi-mat-khau">
                            <span class="btn-spinner d-none" id="btn-spinner" aria-hidden="true"></span>
                            <span id="btn-text">Cập Nhật Mật Khẩu Mới</span>
                        </button>
                    </form>

                    <div class="auth-card-footer">
                        <a href="${pageContext.request.contextPath}/dang-nhap"><span class="material-symbols-outlined icon-xs" aria-hidden="true">arrow_back</span> Quay lại trang đăng nhập</a>
                    </div>
                            <span id="btn-text">Cập Nhật Mật Khẩu Mới</span>
                        </button>
                    </form>

                    <div class="auth-card-footer">
                        <a href="${pageContext.request.contextPath}/dang-nhap"><span class="material-symbols-outlined icon-xs" aria-hidden="true">arrow_back</span> Quay lại trang đăng nhập</a>
                    </div>

                <!-- =======================================================
                     TRƯỜNG HỢP 3: TOKEN KHÔNG HỢP LỆ / HẾT HẠN / ĐÃ DÙNG
                     ======================================================= -->
                <% } else { %>

                    <div class="token-status-card">
                        <% if ("DA_HET_HAN".equalsIgnoreCase(trangThaiToken)) { %>
                            <!-- AC 1: Token hết hạn (30 phút) -->
                            <div class="token-status-icon expired" aria-hidden="true">
                                <span class="material-symbols-outlined icon-2xl">timer_off</span>
                            </div>
                            <h2 class="token-status-title">Liên Kết Đã Hết Hạn</h2>
                            <p class="token-status-desc">
                                <%= request.getAttribute("thongBaoLoi") != null 
                                    ? request.getAttribute("thongBaoLoi") 
                                    : "Liên kết đặt lại mật khẩu đã hết thời hạn hiệu lực (30 phút). Vì lý do bảo mật, bạn cần yêu cầu một liên kết mới." %>
                            </p>

                        <% } else if ("DA_SU_DUNG".equalsIgnoreCase(trangThaiToken)) { %>
                            <!-- AC 2: Token đã sử dụng (chỉ dùng 1 lần) -->
                            <div class="token-status-icon used" aria-hidden="true">
                                <span class="material-symbols-outlined icon-2xl">lock</span>
                            </div>
                            <h2 class="token-status-title">Liên Kết Đã Được Sử Dụng</h2>
                            <p class="token-status-desc">
                                <%= request.getAttribute("thongBaoLoi") != null 
                                    ? request.getAttribute("thongBaoLoi") 
                                    : "Liên kết này đã được sử dụng trước đó. Mỗi liên kết chỉ có thể dùng duy nhất một lần để ngăn chặn việc chiếm đoạt tài khoản." %>
                            </p>

                        <% } else { %>
                            <!-- Token không tồn tại hoặc lỗi khác -->
                            <div class="token-status-icon invalid" aria-hidden="true">
                                <span class="material-symbols-outlined icon-2xl">link_off</span>
                            </div>
                            <h2 class="token-status-title">Liên Kết Không Hợp Lệ</h2>
                            <p class="token-status-desc">
                                <%= request.getAttribute("thongBaoLoi") != null 
                                    ? request.getAttribute("thongBaoLoi") 
                                    : "Mã liên kết không tồn tại hoặc bị sai lệch. Vui lòng kiểm tra lại đường dẫn trong email của bạn." %>
                            </p>
                        <% } %>

                        <!-- Action Buttons -->
                        <a href="${pageContext.request.contextPath}/quen-mat-khau" class="btn-primary" id="btn-yeu-cau-lai">
                            Yêu Cầu Liên Kết Mới
                        </a>
                        <a href="${pageContext.request.contextPath}/dang-nhap" class="btn-secondary" id="btn-ve-dang-nhap">
                            Quay lại trang đăng nhập
                        </a>
                    </div>

                <% } %>

            </div>
        </div>
    </main>

    <!-- Page Footer -->
    <footer class="crm-auth-page-footer">
        <p>&copy; 2026 CRM Bán Hàng - Nhóm 10. Toàn bộ thông tin được bảo mật theo quy định.</p>
    </footer>

    <!-- Script Module -->
    <script src="${pageContext.request.contextPath}/assets/js/auth/dat-lai-mat-khau.js"></script>
</body>
</html>
