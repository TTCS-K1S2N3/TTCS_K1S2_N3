<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Đăng nhập hệ thống CRM Bán Hàng bảo mật cấp doanh nghiệp">
    <title>Đăng Nhập - Hệ Thống CRM Bán Hàng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth/auth.css">
</head>
<body>
    <div class="auth-container">
        <div class="auth-card">
            <!-- Header thương hiệu -->
            <div class="auth-header">
                <div class="auth-logo-badge" title="CRM Enterprise Platform">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                    </svg>
                </div>
                <h1 class="auth-title">Đăng Nhập CRM</h1>
                <p class="auth-subtitle">Hệ thống quản lý khách hàng & quy trình bán hàng</p>
                <div class="security-badge">
                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">lock</span>
                    <span>Bảo mật cấp doanh nghiệp • Mã hóa an toàn</span>
                </div>
            </div>

            <!-- AC 2 & AC 3: Thông báo lỗi xác thực, Khóa tạm 15 phút sau 5 lần sai hoặc Khóa quản trị -->
            <c:if test="${not empty thongBaoLoi}">
                <c:set var="isAdminLock" value="${biKhoaAdmin eq true or fn:containsIgnoreCase(thongBaoLoi, 'quản trị viên') or fn:containsIgnoreCase(thongBaoLoi, 'ngừng hoạt động')}" />
                <c:set var="isTempLock" value="${not isAdminLock and (biKhoaTam eq true or fn:containsIgnoreCase(thongBaoLoi, 'tạm thời') or (fn:containsIgnoreCase(thongBaoLoi, 'khóa') and fn:containsIgnoreCase(thongBaoLoi, 'phút')))}" />
                <c:set var="isSessionExpired" value="${fn:containsIgnoreCase(thongBaoLoi, 'hết hạn') or param.error eq 'session_expired'}" />
                <div id="alertMessage" class="alert-box ${isTempLock ? 'alert-lockout' : (isSessionExpired ? 'alert-warning' : 'alert-danger')}" role="alert">
                    <div class="alert-icon-wrapper">
                        <c:choose>
                            <c:when test="${isAdminLock}">
                                <span class="material-symbols-outlined" aria-hidden="true">lock</span>
                            </c:when>
                            <c:when test="${isTempLock}">
                                <span class="material-symbols-outlined" aria-hidden="true">schedule</span>
                            </c:when>
                            <c:when test="${isSessionExpired}">
                                <span class="material-symbols-outlined" aria-hidden="true">schedule</span>
                            </c:when>
                            <c:otherwise>
                                <span class="material-symbols-outlined" aria-hidden="true">error</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="alert-body">
                        <div class="alert-title">
                            <c:choose>
                                <c:when test="${isAdminLock}">Tài khoản đã bị khóa</c:when>
                                <c:when test="${isTempLock}">Tài Khoản Tạm Thời Bị Khóa</c:when>
                                <c:when test="${isSessionExpired}">Phiên Làm Việc Hết Hạn</c:when>
                                <c:otherwise>Đăng Nhập Thất Bại</c:otherwise>
                            </c:choose>
                        </div>
                        <div class="alert-desc">
                            <c:out value="${thongBaoLoi}"/>
                        </div>

                        <!-- Countdown Timer khi bị khóa tạm 15 phút (CHỈ áp dụng cho khóa tạm do sai mật khẩu) -->
                        <c:if test="${isTempLock}">
                            <div class="countdown-timer-container" id="countdownTimerContainer">
                                <span class="countdown-label">Thời gian mở khóa dự kiến:</span>
                                <span class="countdown-badge" id="countdownTimeDisplay">
                                    <c:choose>
                                        <c:when test="${not empty soPhutKhoaConLai and soPhutKhoaConLai gt 0}">
                                            <c:out value="${soPhutKhoaConLai < 10 ? '0' : ''}${soPhutKhoaConLai}:00"/>
                                        </c:when>
                                        <c:otherwise>15:00</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                        </c:if>
                    </div>
                </div>
            </c:if>

            <!-- Thông báo thành công (Ví dụ vừa đăng xuất an toàn) -->
            <c:if test="${not empty thongBaoThanhCong}">
                <div class="alert-box alert-success" role="alert">
                    <div class="alert-icon-wrapper">
                        <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                    </div>
                    <div class="alert-body">
                        <div class="alert-title">Thành Công</div>
                        <div class="alert-desc"><c:out value="${thongBaoThanhCong}"/></div>
                    </div>
                </div>
            </c:if>

            <!-- AC 1: Form đăng nhập gửi dữ liệu qua POST /dang-nhap -->
            <form id="loginForm" action="${pageContext.request.contextPath}/dang-nhap" method="post" novalidate autocomplete="on">
                <!-- Trường Email -->
                <div class="form-group">
                    <label for="email" class="form-label">Email công ty</label>
                    <div class="input-wrapper">
                        <input type="email" id="email" name="email" class="form-control" 
                               placeholder="ví dụ: sales@crm.vn" required autofocus autocomplete="username"
                               value="<c:out value="${email}"/>">
                        <span class="input-icon-prefix">
                            <span class="material-symbols-outlined icon-sm" aria-hidden="true">mail</span>
                        </span>
                    </div>
                    <div class="field-error-text" id="emailError"></div>
                </div>

                <!-- Trường Mật khẩu -->
                <div class="form-group">
                    <div class="form-label-row">
                        <label for="matKhau" class="form-label">Mật khẩu</label>
                        <a href="${pageContext.request.contextPath}/quen-mat-khau" class="forgot-link">
                            Quên mật khẩu?
                        </a>
                    </div>
                    <div class="input-wrapper input-password-wrapper">
                        <input type="password" id="matKhau" name="matKhau" class="form-control" 
                               placeholder="Nhập mật khẩu của bạn" required autocomplete="current-password">
                        <span class="input-icon-prefix">
                            <span class="material-symbols-outlined icon-sm" aria-hidden="true">lock</span>
                        </span>
                        <button type="button" class="btn-toggle-password" id="btnTogglePassword" 
                                aria-label="Ẩn/hiện mật khẩu" title="Ẩn/hiện mật khẩu">
                            <span class="material-symbols-outlined" aria-hidden="true">visibility</span>
                        </button>
                    </div>
                    <div class="field-error-text" id="passwordError"></div>
                </div>

                <!-- Nút Submit -->
                <button type="submit" class="btn-submit" id="btnSubmit">
                    <span>Đăng Nhập Hệ Thống</span>
                    <span class="material-symbols-outlined icon-sm" aria-hidden="true">login</span>
                </button>
            </form>

            <!-- Bộ kiểm thử nhanh (Quick Demo Credentials) -->
            <div class="demo-section">
                <div class="demo-title-row">
                    <span class="demo-title">
                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">bolt</span>
                        Tài khoản mẫu kiểm thử nhanh
                    </span>
                    <span style="font-size: 11px; color: var(--slate-400);">Nhấp để điền</span>
                </div>
                <div class="demo-chips">
                    <div class="quick-fill-chip" data-email="sales@crm.vn" data-password="123456@Aa" title="Đăng nhập với vai trò Nhân viên Kinh doanh">
                        <span class="chip-email">sales@crm.vn</span>
                        <span class="chip-role-tag">NVKD (Sales)</span>
                    </div>
                    <div class="quick-fill-chip" data-email="admin@crm.vn" data-password="123456@Aa" title="Đăng nhập với vai trò Quản trị viên">
                        <span class="chip-email">admin@crm.vn</span>
                        <span class="chip-role-tag">Quản Trị (Admin)</span>
                    </div>
                    <div class="quick-fill-chip" data-email="teamlead@crm.vn" data-password="123456@Aa" title="Đăng nhập với vai trò Trưởng nhóm Kinh doanh">
                        <span class="chip-email">teamlead@crm.vn</span>
                        <span class="chip-role-tag">Trưởng Nhóm</span>
                    </div>
                </div>
            </div>
        </div>

        <div class="auth-footer">
            &copy; 2026 CRM Bán Hàng - Nhom10 TTCS. Bảo lưu mọi quyền.
        </div>
    </div>

    <!-- Script tương tác Frontend -->
    <script src="${pageContext.request.contextPath}/assets/js/auth/dang-nhap.js"></script>
</body>
</html>
