<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đổi Mật Khẩu | CRM Bán Hàng</title>
    <meta name="description" content="Đổi mật khẩu người dùng và thu hồi các phiên đăng nhập khác để bảo vệ an toàn danh mục khách hàng trong hệ thống CRM.">

    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">

    <!-- CSS Module -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth/doi-mat-khau.css">
</head>
<body class="crm-body">

    <!-- ===== TOP HEADER NAVIGATION ===== -->
    <header class="crm-header" role="banner">
        <div class="header-inner">
            <div class="header-brand">
                <a href="${pageContext.request.contextPath}/" class="brand-link" id="link-brand-home">
                    <span class="brand-logo-icon">
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
                            <circle cx="9" cy="7" r="4"></circle>
                            <path d="M22 21v-2a4 4 0 0 0-3-3.87"></path>
                            <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                        </svg>
                    </span>
                    <span class="brand-text">CRM Bán Hàng</span>
                </a>
                <span class="brand-badge">Bảo mật</span>
            </div>

            <!-- Breadcrumb -->
            <nav class="crm-breadcrumb" aria-label="Đường dẫn điều hướng">
                <ol class="breadcrumb-list">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/" id="crumb-trang-chu">Trang chủ</a></li>
                    <li class="breadcrumb-separator" aria-hidden="true">/</li>
                    <li class="breadcrumb-item"><span class="breadcrumb-muted">Tài khoản</span></li>
                    <li class="breadcrumb-separator" aria-hidden="true">/</li>
                    <li class="breadcrumb-item active" aria-current="page">Đổi mật khẩu</li>
                </ol>
            </nav>

            <!-- User quick info -->
            <div class="header-user">
                <div class="user-avatar" id="header-user-avatar" aria-hidden="true">
                    <span>${not empty sessionScope.nguoiDung.hoTen ? sessionScope.nguoiDung.hoTen.substring(0,1).toUpperCase() : 'U'}</span>
                </div>
                <div class="user-details">
                    <span class="user-name" id="header-user-name">${not empty sessionScope.nguoiDung.hoTen ? sessionScope.nguoiDung.hoTen : 'Người dùng hệ thống'}</span>
                    <span class="user-role-tag">${not empty sessionScope.nguoiDung.email ? sessionScope.nguoiDung.email : 'user@crm.vn'}</span>
                </div>
            </div>
        </div>
    </header>

    <!-- ===== MAIN CONTENT CONTAINER ===== -->
    <main class="main-wrapper" id="main-content">
        <div class="content-container">

            <!-- Page Title Section -->
            <section class="page-intro">
                <div class="intro-icon-wrapper" aria-hidden="true">
                    <span class="material-symbols-outlined icon-2xl">lock</span>
                </div>
                <div class="intro-text">
                    <h1 class="page-title" id="page-title">Đổi Mật Khẩu Đang Đăng Nhập</h1>
                    <p class="page-subtitle">
                        Chủ động bảo vệ danh mục khách hàng, thông tin liên hệ và cơ hội bán hàng bằng việc cập nhật mật khẩu định kỳ và thu hồi các phiên đăng nhập khác.
                    </p>
                </div>
            </section>

            <!-- Server Notifications (nếu có từ Controller) -->
            <div id="alert-server-error" class="alert alert-error ${empty thongBaoLoi ? 'd-none' : ''}" role="alert">
                <div class="alert-icon" aria-hidden="true">
                    <span class="material-symbols-outlined icon-sm">error</span>
                </div>
                <div class="alert-content">
                    <strong class="alert-heading">Đổi mật khẩu không thành công</strong>
                    <p id="server-error-text" class="alert-message">${thongBaoLoi}</p>
                </div>
                <button type="button" class="btn-alert-close" aria-label="Đóng thông báo lỗi" onclick="this.closest('.alert').classList.add('d-none');"><span class="material-symbols-outlined icon-xs" aria-hidden="true">close</span></button>
            </div>

            <div id="alert-server-success" class="alert alert-success ${empty thongBaoThanhCong ? 'd-none' : ''}" role="alert">
                <div class="alert-icon" aria-hidden="true">
                    <span class="material-symbols-outlined icon-sm">check_circle</span>
                </div>
                <div class="alert-content">
                    <strong class="alert-heading">Đổi mật khẩu thành công!</strong>
                    <p id="server-success-text" class="alert-message">${thongBaoThanhCong}</p>
                </div>
                <button type="button" class="btn-alert-close" aria-label="Đóng thông báo thành công" onclick="this.closest('.alert').classList.add('d-none');"><span class="material-symbols-outlined icon-xs" aria-hidden="true">close</span></button>
            </div>

            <!-- Client-side Dynamic Feedback Alert -->
            <div id="alert-client-feedback" class="alert d-none" role="alert" aria-live="assertive">
                <div class="alert-icon" id="feedback-icon" aria-hidden="true"></div>
                <div class="alert-content">
                    <strong class="alert-heading" id="feedback-heading">Thông báo</strong>
                    <p class="alert-message" id="feedback-message"></p>
                </div>
                <button type="button" class="btn-alert-close" aria-label="Đóng thông báo" id="btn-close-feedback"><span class="material-symbols-outlined icon-xs" aria-hidden="true">close</span></button>
            </div>

            <!-- Main Layout Grid: Form Column & Security Guidelines Column -->
            <div class="grid-layout">

                <!-- Left Column: Change Password Card Form -->
                <div class="card-form-wrapper">
                    <div class="crm-card">
                        <div class="card-header">
                            <h2 class="card-title">Cập nhật thông tin mật khẩu</h2>
                            <p class="card-desc">Vui lòng điền đầy đủ các thông tin bên dưới. Các trường có dấu (<span class="required-star">*</span>) là bắt buộc.</p>
                        </div>

                        <form id="form-doi-mat-khau" action="${pageContext.request.contextPath}/doi-mat-khau" method="POST" class="auth-form" novalidate autocomplete="off">
                            <!-- CSRF Token (nếu server hỗ trợ) -->
                            <input type="hidden" id="csrf-token" name="csrfToken" value="${csrfToken != null ? csrfToken : ''}" />

                            <!-- ===== TRƯỜNG 1: MẬT KHẨU HIỆN TẠI (AC 1) ===== -->
                            <div class="form-group" id="group-mat-khau-hien-tai">
                                <label for="mat-khau-hien-tai" class="form-label">
                                    Mật khẩu hiện tại <span class="required-star">*</span>
                                </label>
                                <div class="input-wrapper">
                                    <span class="input-icon-left" aria-hidden="true">
                                        <span class="material-symbols-outlined icon-sm">lock</span>
                                    </span>
                                    <input
                                        type="password"
                                        id="mat-khau-hien-tai"
                                        name="matKhauHienTai"
                                        class="form-control"
                                        placeholder="Nhập mật khẩu bạn đang dùng"
                                        autocomplete="current-password"
                                        required
                                        aria-required="true"
                                        aria-describedby="err-mat-khau-hien-tai hint-mat-khau-hien-tai"
                                    />
                                    <button
                                        type="button"
                                        id="btn-toggle-mat-khau-hien-tai"
                                        class="btn-toggle-pwd"
                                        aria-label="Hiện mật khẩu hiện tại"
                                        data-target="mat-khau-hien-tai"
                                        tabindex="0"
                                    >
                                        <span class="material-symbols-outlined eye-open icon-sm" aria-hidden="true">visibility</span>
                                        <span class="material-symbols-outlined eye-closed d-none icon-sm" aria-hidden="true">visibility_off</span>
                                    </button>
                                </div>
                                <span id="hint-mat-khau-hien-tai" class="field-hint">Bắt buộc phải nhập mật khẩu hiện tại để xác thực bạn là chủ tài khoản.</span>
                                <div id="err-mat-khau-hien-tai" class="field-error" aria-live="polite"></div>
                            </div>

                            <hr class="form-divider" />

                            <!-- ===== TRƯỜNG 2: MẬT KHẨU MỚI (AC 2) ===== -->
                            <div class="form-group" id="group-mat-khau-moi">
                                <label for="mat-khau-moi" class="form-label">
                                    Mật khẩu mới <span class="required-star">*</span>
                                </label>
                                <div class="input-wrapper">
                                    <span class="input-icon-left" aria-hidden="true">
                                        <span class="material-symbols-outlined icon-sm">key</span>
                                    </span>
                                    <input
                                        type="password"
                                        id="mat-khau-moi"
                                        name="matKhauMoi"
                                        class="form-control"
                                        placeholder="Tối thiểu 8 ký tự, gồm cả chữ và số"
                                        autocomplete="new-password"
                                        required
                                        minlength="8"
                                        aria-required="true"
                                        aria-describedby="err-mat-khau-moi checklist-mat-khau-moi"
                                    />
                                    <button
                                        type="button"
                                        id="btn-toggle-mat-khau-moi"
                                        class="btn-toggle-pwd"
                                        aria-label="Hiện mật khẩu mới"
                                        data-target="mat-khau-moi"
                                        tabindex="0"
                                    >
                                        <span class="material-symbols-outlined eye-open icon-sm" aria-hidden="true">visibility</span>
                                        <span class="material-symbols-outlined eye-closed d-none icon-sm" aria-hidden="true">visibility_off</span>
                                    </button>
                                </div>
                                <div id="err-mat-khau-moi" class="field-error" aria-live="polite"></div>

                                <!-- Thanh đo độ mạnh mật khẩu (Password Strength Meter) -->
                                <div class="strength-meter-container" id="strength-container" aria-live="polite">
                                    <div class="strength-header">
                                        <span class="strength-title">Độ mạnh mật khẩu:</span>
                                        <span class="strength-level" id="strength-text">Chưa nhập</span>
                                    </div>
                                    <div class="strength-bars" aria-hidden="true">
                                        <span class="bar" id="bar-1"></span>
                                        <span class="bar" id="bar-2"></span>
                                        <span class="bar" id="bar-3"></span>
                                        <span class="bar" id="bar-4"></span>
                                    </div>
                                </div>

                                <!-- Tiêu chuẩn mật khẩu (Checklist) -->
                                <div class="password-checklist-card" id="checklist-mat-khau-moi">
                                    <span class="checklist-heading">Quy định mật khẩu mới:</span>
                                    <ul class="checklist-items">
                                        <li class="checklist-item" id="rule-min-length" data-rule="length">
                                            <span class="rule-icon" aria-hidden="true"><span class="material-symbols-outlined icon-xs">close</span></span>
                                            <span class="rule-label">Tối thiểu <strong>8 ký tự</strong></span>
                                        </li>
                                        <li class="checklist-item" id="rule-has-letter" data-rule="letter">
                                            <span class="rule-icon" aria-hidden="true"><span class="material-symbols-outlined icon-xs">close</span></span>
                                            <span class="rule-label">Chứa ít nhất <strong>1 chữ cái</strong> (a-z hoặc A-Z)</span>
                                        </li>
                                        <li class="checklist-item" id="rule-has-number" data-rule="number">
                                            <span class="rule-icon" aria-hidden="true"><span class="material-symbols-outlined icon-xs">close</span></span>
                                            <span class="rule-label">Chứa ít nhất <strong>1 chữ số</strong> (0-9)</span>
                                        </li>
                                        <li class="checklist-item" id="rule-diff-current" data-rule="different">
                                            <span class="rule-icon" aria-hidden="true"><span class="material-symbols-outlined icon-xs">close</span></span>
                                            <span class="rule-label">Không trùng với <strong>mật khẩu hiện tại</strong></span>
                                        </li>
                                    </ul>
                                </div>
                            </div>

                            <!-- ===== TRƯỜNG 3: XÁC NHẬN MẬT KHẨU MỚI ===== -->
                            <div class="form-group" id="group-xac-nhan-mat-khau">
                                <label for="xac-nhan-mat-khau" class="form-label">
                                    Xác nhận mật khẩu mới <span class="required-star">*</span>
                                </label>
                                <div class="input-wrapper">
                                    <span class="input-icon-left" aria-hidden="true">
                                        <span class="material-symbols-outlined icon-sm">lock_clock</span>
                                    </span>
                                    <input
                                        type="password"
                                        id="xac-nhan-mat-khau"
                                        name="xacNhanMatKhau"
                                        class="form-control"
                                        placeholder="Nhập lại mật khẩu mới vừa đặt"
                                        autocomplete="new-password"
                                        required
                                        aria-required="true"
                                        aria-describedby="err-xac-nhan-mat-khau match-status"
                                    />
                                    <button
                                        type="button"
                                        id="btn-toggle-xac-nhan-mat-khau"
                                        class="btn-toggle-pwd"
                                        aria-label="Hiện xác nhận mật khẩu mới"
                                        data-target="xac-nhan-mat-khau"
                                        tabindex="0"
                                    >
                                        <span class="material-symbols-outlined eye-open icon-sm" aria-hidden="true">visibility</span>
                                        <span class="material-symbols-outlined eye-closed d-none icon-sm" aria-hidden="true">visibility_off</span>
                                    </button>
                                </div>
                                <div id="match-status" class="match-status" aria-live="polite"></div>
                                <div id="err-xac-nhan-mat-khau" class="field-error" aria-live="polite"></div>
                            </div>

                            <hr class="form-divider" />

                            <!-- ===== TRƯỜNG 4: THU HỒI CÁC PHIÊN ĐĂNG NHẬP KHÁC (AC 3) ===== -->
                            <div class="session-revocation-card" id="card-thu-hoi-phien">
                                <div class="switch-box">
                                    <label class="custom-switch" for="thu-hoi-phien-khac">
                                        <input
                                            type="checkbox"
                                            id="thu-hoi-phien-khac"
                                            name="thuHoiPhienKhac"
                                            value="true"
                                            checked
                                            class="switch-checkbox"
                                            aria-describedby="desc-thu-hoi-phien"
                                        />
                                        <span class="switch-slider" aria-hidden="true"></span>
                                    </label>
                                    <div class="switch-content">
                                        <div class="switch-title-row">
                                            <label for="thu-hoi-phien-khac" class="switch-title">
                                                Thu hồi các phiên đăng nhập khác
                                            </label>
                                            <span class="badge-recommended">Khuyến nghị bảo mật</span>
                                        </div>
                                        <p id="desc-thu-hoi-phien" class="switch-desc">
                                            Tự động đăng xuất tài khoản khỏi tất cả các trình duyệt, máy tính, điện thoại khác ngay sau khi đổi mật khẩu thành công. Chỉ duy trì phiên làm việc hiện tại trên thiết bị này.
                                        </p>
                                    </div>
                                </div>

                                <!-- Session warning tip -->
                                <div class="session-notice">
                                    <span class="notice-icon" aria-hidden="true">
                                        <span class="material-symbols-outlined icon-xs">info</span>
                                    </span>
                                    <span class="notice-text">
                                        Nếu bạn nghi ngờ mật khẩu đã bị lộ hoặc vừa đăng nhập từ máy tính công cộng, tính năng này đảm bảo không ai khác có thể tiếp tục truy cập dữ liệu khách hàng của bạn.
                                    </span>
                                </div>
                            </div>

                            <!-- ===== FORM ACTIONS ===== -->
                            <div class="form-actions">
                                <button
                                    type="submit"
                                    id="btn-submit-doi-mat-khau"
                                    class="btn btn-primary btn-submit"
                                >
                                    <span class="btn-spinner d-none" id="btn-spinner" aria-hidden="true"></span>
                                    <span class="btn-icon" id="btn-icon" aria-hidden="true">
                                        <span class="material-symbols-outlined icon-sm">save</span>
                                    </span>
                                    <span class="btn-text" id="btn-text">Đổi Mật Khẩu và Cập Nhật Bảo Mật</span>
                                </button>

                                <a
                                    href="${pageContext.request.contextPath}/"
                                    id="btn-huy-doi-mat-khau"
                                    class="btn btn-secondary"
                                >
                                    Hủy bỏ
                                </a>
                            </div>

                        </form>
                    </div>
                </div>

                <!-- Right Column: Security Sidebar & Guidelines -->
                <aside class="sidebar-wrapper" aria-label="Thông tin bảo mật bổ sung">

                    <!-- Security Tips Card -->
                    <div class="sidebar-card card-tips">
                        <div class="sidebar-card-header">
                            <span class="sidebar-icon-wrapper" aria-hidden="true">
                                <span class="material-symbols-outlined icon-sm">shield</span>
                            </span>
                            <h3 class="sidebar-title">Bảo Vệ Danh Mục Khách Hàng</h3>
                        </div>
                        <div class="sidebar-card-body">
                            <p class="tips-intro">
                                Danh mục khách hàng, số điện thoại, báo giá và doanh số là tài sản quan trọng. Việc giữ mật khẩu an toàn giúp bạn:
                            </p>
                            <ul class="tips-list">
                                <li>
                                    <strong>Tránh rò rỉ dữ liệu khách hàng</strong> sang đối thủ hoặc cá nhân không có thẩm quyền.
                                </li>
                                <li>
                                    <strong>Ngăn chặn giả mạo thao tác</strong> như chuyển đổi người phụ trách hoặc sửa đổi báo giá sai lệch.
                                </li>
                                <li>
                                    <strong>Đảm bảo tính chịu trách nhiệm</strong> cho từng hoạt động bán hàng ghi nhận trên hệ thống.
                                </li>
                            </ul>
                        </div>
                    </div>

                    <!-- Current Session Status Card -->
                    <div class="sidebar-card card-session-info">
                        <div class="sidebar-card-header">
                            <span class="sidebar-icon-wrapper icon-session" aria-hidden="true">
                                <span class="material-symbols-outlined icon-sm">devices</span>
                            </span>
                            <h3 class="sidebar-title">Phiên Đăng Nhập Của Bạn</h3>
                        </div>
                        <div class="sidebar-card-body">
                            <div class="session-item current">
                                <div class="session-dot active" aria-hidden="true"></div>
                                <div class="session-info">
                                    <span class="session-device">Thiết bị hiện tại</span>
                                    <span class="session-status">Đang hoạt động &bull; Được giữ lại</span>
                                </div>
                            </div>
                            <div class="session-item other">
                                <div class="session-dot revoked" aria-hidden="true"></div>
                                <div class="session-info">
                                    <span class="session-device">Các phiên / thiết bị khác</span>
                                    <span class="session-status text-warning">Sẽ thu hồi khi đổi mật khẩu</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Password Best Practices Card -->
                    <div class="sidebar-card card-best-practices">
                        <div class="sidebar-card-header">
                            <span class="sidebar-icon-wrapper icon-idea" aria-hidden="true">
                                <span class="material-symbols-outlined icon-sm">lightbulb</span>
                            </span>
                            <h3 class="sidebar-title">Gợi Ý Mật Khẩu Mạnh</h3>
                        </div>
                        <div class="sidebar-card-body">
                            <ul class="best-practice-list">
                                <li>Nên kết hợp cả chữ hoa, chữ thường, số và ký tự đặc biệt (!@#$%^&*).</li>
                                <li>Không sử dụng thông tin dễ đoán như ngày sinh, số điện thoại hoặc biển số xe.</li>
                                <li>Không dùng lại mật khẩu của email cá nhân hoặc tài khoản mạng xã hội.</li>
                            </ul>
                        </div>
                    </div>

                </aside>

            </div><%-- end grid-layout --%>

        </div><%-- end content-container --%>
    </main>

    <!-- ===== FOOTER ===== -->
    <footer class="crm-footer" role="contentinfo">
        <div class="footer-inner">
            <p>&copy; 2026 CRM Bán Hàng - Nhóm 10. Toàn bộ thông tin được bảo mật theo quy định.</p>
        </div>
    </footer>

    <!-- JavaScript Module -->
    <script src="${pageContext.request.contextPath}/assets/js/auth/doi-mat-khau.js"></script>
</body>
</html>
