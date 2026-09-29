<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập hệ thống CRM</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
</head>
<body>
<div class="auth-container">
    <div class="auth-card">
        <div class="auth-header">
            <h1>Hệ thống CRM</h1>
            <p>Quản lý khách hàng và quy trình bán hàng</p>
        </div>

        <%-- Thông báo lỗi hoặc phiên hết hạn (AC3) --%>
        <c:if test="${not empty thongBaoLoi}">
            <div class="alert alert-danger" id="alert-error" role="alert">
                <span class="alert-icon">⚠️</span>
                <div>
                    <strong>Thông báo:</strong>
                    <div><c:out value="${thongBaoLoi}" /></div>
                </div>
            </div>
        </c:if>

        <%-- Thông báo đăng xuất thành công (AC2) --%>
        <c:if test="${not empty thongBaoThanhCong}">
            <div class="alert alert-success" id="alert-success" role="alert">
                <span class="alert-icon">✓</span>
                <div>
                    <strong>Thành công:</strong>
                    <div><c:out value="${thongBaoThanhCong}" /></div>
                </div>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/dang-nhap" method="post" id="login-form">
            <div class="form-group">
                <label for="email">Email công ty</label>
                <input type="email" id="email" name="email" class="form-control"
                       value="<c:out value='${email}' />"
                       placeholder="ten.nhanvien@congty.vn" required autofocus>
            </div>

            <div class="form-group">
                <label for="matKhau">Mật khẩu</label>
                <input type="password" id="matKhau" name="matKhau" class="form-control"
                       placeholder="••••••••" required>
            </div>

            <button type="submit" class="btn-primary" id="btn-submit">Đăng nhập</button>
        </form>

        <div class="auth-footer">
            <p>Phiên đăng nhập được bảo mật và gia hạn tự động khi đang hoạt động.</p>
        </div>
    </div>
</div>
</body>
</html>
