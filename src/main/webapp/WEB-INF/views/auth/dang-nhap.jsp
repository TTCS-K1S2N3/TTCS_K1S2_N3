<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng Nhập - Hệ Thống CRM Bán Hàng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth/auth.css">
</head>
<body>
    <div class="auth-container">
        <div class="auth-card">
            <div class="auth-header">
                <div class="auth-logo">💼</div>
                <h1 class="auth-title">Đăng Nhập CRM</h1>
                <p class="auth-subtitle">Hệ thống quản lý khách hàng và quy trình bán hàng</p>
            </div>

            <%-- Thông báo lỗi xác thực hoặc tài khoản bị khóa --%>
            <c:if test="${not empty thongBaoLoi}">
                <div class="alert-message alert-danger">
                    <span class="alert-icon">⚠️</span>
                    <span><c:out value="${thongBaoLoi}"/></span>
                </div>
            </c:if>

            <%-- Thông báo thành công (ví dụ đăng xuất) --%>
            <c:if test="${not empty thongBaoThanhCong}">
                <div class="alert-message alert-success">
                    <span class="alert-icon">✓</span>
                    <span><c:out value="${thongBaoThanhCong}"/></span>
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/dang-nhap" method="post" autocomplete="on">
                <div class="form-group">
                    <label for="email" class="form-label">Email công ty</label>
                    <input type="email" id="email" name="email" class="form-control" 
                           placeholder="sales@crm.vn" required autofocus
                           value="<c:out value="${email}"/>">
                </div>

                <div class="form-group">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
                        <label for="matKhau" class="form-label" style="margin-bottom: 0;">Mật khẩu</label>
                        <a href="${pageContext.request.contextPath}/quen-mat-khau" 
                           style="font-size: 13px; color: var(--primary-color); text-decoration: none;">
                            Quên mật khẩu?
                        </a>
                    </div>
                    <div class="input-password-wrapper">
                        <input type="password" id="matKhau" name="matKhau" class="form-control" 
                               placeholder="Nhập mật khẩu" required>
                        <button type="button" class="btn-toggle-password" id="btnTogglePassword" title="Ẩn/hiện mật khẩu">
                            👁️
                        </button>
                    </div>
                </div>

                <button type="submit" class="btn-primary" id="btnSubmit">
                    Đăng Nhập
                </button>
            </form>

            <div class="demo-credentials">
                <strong>💡 Tài khoản mẫu kiểm thử:</strong><br>
                • NVKD: <code>sales@crm.vn</code> / <code>123456@Aa</code><br>
                • Quản trị: <code>admin@crm.vn</code> / <code>123456@Aa</code><br>
                • Trưởng nhóm: <code>teamlead@crm.vn</code> / <code>123456@Aa</code>
            </div>
        </div>
    </div>

    <script>
        document.addEventListener('DOMContentLoaded', function () {
            var toggleBtn = document.getElementById('btnTogglePassword');
            var passwordInput = document.getElementById('matKhau');

            if (toggleBtn && passwordInput) {
                toggleBtn.addEventListener('click', function () {
                    var currentType = passwordInput.getAttribute('type');
                    if (currentType === 'password') {
                        passwordInput.setAttribute('type', 'text');
                        toggleBtn.textContent = '🔒';
                    } else {
                        passwordInput.setAttribute('type', 'password');
                        toggleBtn.textContent = '👁️';
                    }
                });
            }
        });
    </script>
</body>
</html>
