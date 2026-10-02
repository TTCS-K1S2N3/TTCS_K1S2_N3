<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quên mật khẩu — CRM Bán Hàng B2B</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body class="auth-page">

    <div class="auth-container">
        <header class="auth-header">
            <div class="brand-title">CRM Bán Hàng B2B</div>
            <div class="brand-subtitle">Hệ thống quản lý khách hàng và quy trình kinh doanh</div>
        </header>

        <main class="auth-card">
            <h1>Quên mật khẩu</h1>
            <p class="form-description">
                Nhập địa chỉ email công ty của bạn để nhận liên kết thiết lập lại mật khẩu an toàn.
            </p>

            <%-- Thông báo lỗi validation hoặc lỗi xử lý --%>
            <%
                String errorMessage = (String) request.getAttribute("errorMessage");
                if (errorMessage != null && !errorMessage.isBlank()) {
            %>
                <div class="alert alert-danger" role="alert" id="forgotAlertError">
                    <%= errorMessage %>
                </div>
            <%
                }
            %>

            <%-- S1-03-AC3: Thông báo chung thống nhất chống user enumeration --%>
            <%
                String successMessage = (String) request.getAttribute("successMessage");
                if (successMessage != null && !successMessage.isBlank()) {
            %>
                <div class="alert alert-info" role="alert" id="forgotAlertSuccess">
                    <%= successMessage %>
                </div>
            <%
                }
            %>

            <form action="${pageContext.request.contextPath}/forgot-password" method="post" id="forgotPasswordForm" novalidate>
                <div class="form-group">
                    <label class="form-label" for="emailInput">Email công ty</label>
                    <input type="email"
                           id="emailInput"
                           name="email"
                           class="form-input"
                           placeholder="ten.nhanvien@congty.vn"
                           value="<%= request.getAttribute("email") != null ? request.getAttribute("email") : "" %>"
                           required
                           autocomplete="email"
                           autofocus>
                </div>

                <div class="form-group" style="margin-top: 24px;">
                    <button type="submit" class="btn btn-primary btn-block" id="btnForgotSubmit">
                        Gửi liên kết đặt lại mật khẩu
                    </button>
                </div>

                <div class="form-group" style="text-align: center; margin-top: 16px;">
                    <a href="${pageContext.request.contextPath}/login" class="auth-link" id="linkBackToLogin">
                        ← Quay lại trang đăng nhập
                    </a>
                </div>
            </form>
        </main>
    </div>

    <script>
        document.getElementById('forgotPasswordForm').addEventListener('submit', function (e) {
            var email = document.getElementById('emailInput').value.trim();

            if (!email || email.indexOf('@') === -1) {
                e.preventDefault();
                var alertBox = document.getElementById('forgotAlertError');
                if (!alertBox) {
                    alertBox = document.createElement('div');
                    alertBox.className = 'alert alert-danger';
                    alertBox.id = 'forgotAlertError';
                    var form = document.getElementById('forgotPasswordForm');
                    form.parentNode.insertBefore(alertBox, form);
                }
                alertBox.textContent = 'Vui lòng nhập địa chỉ email công ty hợp lệ.';
            }
        });
    </script>
</body>
</html>
