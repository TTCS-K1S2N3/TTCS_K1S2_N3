<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quên Mật Khẩu - CRM Bán Hàng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth/auth.css">
</head>
<body>
    <div class="auth-container">
        <div class="auth-card">
            <div class="auth-header">
                <div class="auth-logo">🔒</div>
                <h1 class="auth-title">Quên Mật Khẩu</h1>
                <p class="auth-subtitle">
                    Nhập địa chỉ email công ty của bạn để nhận liên kết khôi phục mật khẩu.
                </p>
            </div>

            <% if (request.getAttribute("thongBaoThanhCong") != null) { %>
                <div class="alert-message alert-success">
                    <span>✓</span>
                    <div><%= request.getAttribute("thongBaoThanhCong") %></div>
                </div>
            <% } %>

            <% if (request.getAttribute("thongBaoLoi") != null) { %>
                <div class="alert-message alert-danger">
                    <span>⚠</span>
                    <div><%= request.getAttribute("thongBaoLoi") %></div>
                </div>
            <% } %>

            <form action="${pageContext.request.contextPath}/quen-mat-khau" method="post">
                <div class="form-group">
                    <label for="email" class="form-label">Email công ty</label>
                    <input type="email" 
                           id="email" 
                           name="email" 
                           class="form-control" 
                           placeholder="example@crmbanhang.vn" 
                           value="${emailNhapLai != null ? emailNhapLai : ''}"
                           required 
                           autofocus>
                    <div class="form-hint">Liên kết đặt lại sẽ có hiệu lực trong vòng 30 phút.</div>
                </div>

                <button type="submit" class="btn-primary" id="btn-gui-yeu-cau">
                    Gửi Liên Kết Đặt Lại
                </button>
            </form>

            <div class="auth-footer">
                <a href="${pageContext.request.contextPath}/dang-nhap">← Quay lại trang đăng nhập</a>
            </div>
        </div>
    </div>
</body>
</html>
