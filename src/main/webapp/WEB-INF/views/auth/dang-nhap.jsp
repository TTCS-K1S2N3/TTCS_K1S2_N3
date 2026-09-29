<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng Nhập - CRM Bán Hàng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth/auth.css">
</head>
<body>
    <div class="auth-container">
        <div class="auth-card">
            <div class="auth-header">
                <div class="auth-logo">👥</div>
                <h1 class="auth-title">Đăng Nhập CRM</h1>
                <p class="auth-subtitle">Hệ thống quản lý khách hàng và quy trình bán hàng</p>
            </div>

            <form action="${pageContext.request.contextPath}/dang-nhap" method="post">
                <div class="form-group">
                    <label for="email" class="form-label">Email công ty</label>
                    <input type="email" id="email" name="email" class="form-control" placeholder="sales@crmbanhang.vn" required>
                </div>

                <div class="form-group">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
                        <label for="matKhau" class="form-label" style="margin-bottom: 0;">Mật khẩu</label>
                        <a href="${pageContext.request.contextPath}/quen-mat-khau" style="font-size: 13px; color: var(--primary-color); text-decoration: none;">
                            Quên mật khẩu?
                        </a>
                    </div>
                    <input type="password" id="matKhau" name="matKhau" class="form-control" placeholder="Nhập mật khẩu" required>
                </div>

                <button type="submit" class="btn-primary" style="margin-top: 8px;">
                    Đăng Nhập
                </button>
            </form>
        </div>
    </div>
</body>
</html>
