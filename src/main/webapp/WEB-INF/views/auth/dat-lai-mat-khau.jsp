<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đặt Lại Mật Khẩu - CRM Bán Hàng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth/auth.css">
</head>
<body>
    <div class="auth-container">
        <div class="auth-card">
            <div class="auth-header">
                <div class="auth-logo">🔑</div>
                <h1 class="auth-title">Đặt Lại Mật Khẩu</h1>
                <p class="auth-subtitle">
                    Thiết lập mật khẩu mới cho tài khoản CRM của bạn.
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

            <% Boolean tokenHopLe = (Boolean) request.getAttribute("tokenHopLe");
               Boolean datLaiThanhCong = (Boolean) request.getAttribute("datLaiThanhCong");
            %>

            <% if (Boolean.TRUE.equals(datLaiThanhCong)) { %>
                <div style="margin-top: 20px;">
                    <a href="${pageContext.request.contextPath}/dang-nhap" class="btn-primary" style="text-align: center; display: block;">
                        Đăng Nhập Ngay
                    </a>
                </div>
            <% } else if (Boolean.TRUE.equals(tokenHopLe)) { %>
                <div class="token-notice">
                    ⏱ Liên kết có hiệu lực 30 phút và chỉ sử dụng được một lần duy nhất.
                </div>

                <form action="${pageContext.request.contextPath}/dat-lai-mat-khau" method="post" id="form-dat-lai-mat-khau">
                    <input type="hidden" name="token" value="<%= request.getAttribute("token") != null ? request.getAttribute("token") : "" %>">

                    <div class="form-group">
                        <label for="matKhauMoi" class="form-label">Mật khẩu mới</label>
                        <input type="password" 
                               id="matKhauMoi" 
                               name="matKhauMoi" 
                               class="form-control" 
                               placeholder="Tối thiểu 8 ký tự, có chữ và số" 
                               required 
                               minlength="8"
                               autofocus>
                        <div class="form-hint">Mật khẩu phải có tối thiểu 8 ký tự, bao gồm cả chữ cái và chữ số.</div>
                    </div>

                    <div class="form-group">
                        <label for="xacNhanMatKhau" class="form-label">Xác nhận mật khẩu mới</label>
                        <input type="password" 
                               id="xacNhanMatKhau" 
                               name="xacNhanMatKhau" 
                               class="form-control" 
                               placeholder="Nhập lại mật khẩu mới" 
                               required 
                               minlength="8">
                    </div>

                    <button type="submit" class="btn-primary" id="btn-xac-nhan-doi-mat-khau">
                        Cập Nhật Mật Khẩu Mới
                    </button>
                </form>
            <% } else { %>
                <!-- Token không hợp lệ, đã dùng hoặc đã hết hạn -->
                <div style="margin-top: 20px;">
                    <a href="${pageContext.request.contextPath}/quen-mat-khau" class="btn-primary" style="text-align: center; display: block;">
                        Yêu Cầu Liên Kết Mới
                    </a>
                    <a href="${pageContext.request.contextPath}/dang-nhap" class="btn-secondary" style="text-align: center; display: block;">
                        Quay lại trang đăng nhập
                    </a>
                </div>
            <% } %>

            <% if (!Boolean.TRUE.equals(datLaiThanhCong) && Boolean.TRUE.equals(tokenHopLe)) { %>
                <div class="auth-footer">
                    <a href="${pageContext.request.contextPath}/dang-nhap">← Quay lại trang đăng nhập</a>
                </div>
            <% } %>
        </div>
    </div>
</body>
</html>
