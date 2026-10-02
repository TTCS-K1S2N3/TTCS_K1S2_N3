<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    Boolean tokenHopLeObj = (Boolean) request.getAttribute("tokenHopLe");
    boolean tokenHopLe = tokenHopLeObj != null && tokenHopLeObj;
    String token = (String) request.getAttribute("token");
    String errorMessage = (String) request.getAttribute("errorMessage");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đặt lại mật khẩu — CRM Bán Hàng B2B</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body class="auth-page">

    <div class="auth-container">
        <header class="auth-header">
            <div class="brand-title">CRM Bán Hàng B2B</div>
            <div class="brand-subtitle">Hệ thống quản lý khách hàng và quy trình kinh doanh</div>
        </header>

        <main class="auth-card">
            <h1>Đặt lại mật khẩu</h1>
            <p class="form-description">
                Thiết lập mật khẩu mới an toàn cho tài khoản của bạn để tiếp tục truy cập hệ thống.
            </p>

            <%-- Thông báo lỗi token không hợp lệ hoặc lỗi mật khẩu --%>
            <%
                if (errorMessage != null && !errorMessage.isBlank()) {
            %>
                <div class="alert alert-danger" role="alert" id="resetAlertError">
                    <%= errorMessage %>
                </div>
            <%
                }
            %>

            <%
                if (tokenHopLe) {
            %>
                <form action="${pageContext.request.contextPath}/reset-password" method="post" id="resetPasswordForm" novalidate>
                    <input type="hidden" name="token" id="tokenInput" value="<%= token != null ? token : "" %>">

                    <div class="form-group">
                        <label class="form-label" for="matKhauMoiInput">Mật khẩu mới</label>
                        <input type="password"
                               id="matKhauMoiInput"
                               name="matKhauMoi"
                               class="form-input"
                               placeholder="••••••••"
                               required
                               minlength="8"
                               autocomplete="new-password"
                               autofocus>
                        <div style="font-size: 12px; color: #666; margin-top: 4px;">
                            Tối thiểu 8 ký tự, bao gồm cả chữ và số.
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="xacNhanMatKhauInput">Xác nhận mật khẩu mới</label>
                        <input type="password"
                               id="xacNhanMatKhauInput"
                               name="xacNhanMatKhau"
                               class="form-input"
                               placeholder="••••••••"
                               required
                               minlength="8"
                               autocomplete="new-password">
                    </div>

                    <div class="form-group" style="margin-top: 24px;">
                        <button type="submit" class="btn btn-primary btn-block" id="btnResetSubmit">
                            Cập nhật mật khẩu mới
                        </button>
                    </div>

                    <div class="form-group" style="text-align: center; margin-top: 16px;">
                        <a href="${pageContext.request.contextPath}/login" class="auth-link" id="linkBackToLogin">
                            ← Quay lại trang đăng nhập
                        </a>
                    </div>
                </form>
            <%
                } else {
            %>
                <%-- S1-03-AC1 & AC2: Token hết hạn hoặc đã sử dụng --%>
                <div class="form-group" style="margin-top: 24px;">
                    <a href="${pageContext.request.contextPath}/forgot-password" class="btn btn-primary btn-block" id="btnRequestNewLink">
                        Yêu cầu liên kết mới
                    </a>
                </div>

                <div class="form-group" style="text-align: center; margin-top: 16px;">
                    <a href="${pageContext.request.contextPath}/login" class="auth-link" id="linkBackToLogin">
                        ← Quay lại trang đăng nhập
                    </a>
                </div>
            <%
                }
            %>
        </main>
    </div>

    <%
        if (tokenHopLe) {
    %>
    <script>
        document.getElementById('resetPasswordForm').addEventListener('submit', function (e) {
            var matKhauMoi = document.getElementById('matKhauMoiInput').value;
            var xacNhan = document.getElementById('xacNhanMatKhauInput').value;

            var alertBox = document.getElementById('resetAlertError');

            function showError(msg) {
                if (!alertBox) {
                    alertBox = document.createElement('div');
                    alertBox.className = 'alert alert-danger';
                    alertBox.id = 'resetAlertError';
                    var form = document.getElementById('resetPasswordForm');
                    form.parentNode.insertBefore(alertBox, form);
                }
                alertBox.textContent = msg;
            }

            if (!matKhauMoi || matKhauMoi.length < 8) {
                e.preventDefault();
                showError('Mật khẩu mới phải có tối thiểu 8 ký tự.');
                return;
            }

            var hasLetter = /[a-zA-Z]/.test(matKhauMoi);
            var hasDigit = /\d/.test(matKhauMoi);
            if (!hasLetter || !hasDigit) {
                e.preventDefault();
                showError('Mật khẩu mới phải bao gồm cả chữ và số.');
                return;
            }

            if (matKhauMoi !== xacNhan) {
                e.preventDefault();
                showError('Mật khẩu xác nhận không khớp với mật khẩu mới.');
                return;
            }
        });
    </script>
    <%
        }
    %>
</body>
</html>
