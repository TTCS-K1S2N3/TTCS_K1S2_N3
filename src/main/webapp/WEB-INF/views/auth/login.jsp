<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập — CRM Bán Hàng B2B</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body class="auth-page">

    <div class="auth-container">
        <header class="auth-header">
            <div class="brand-title">CRM Bán Hàng B2B</div>
            <div class="brand-subtitle">Hệ thống quản lý khách hàng và quy trình kinh doanh</div>
        </header>

        <main class="auth-card">
            <h1>Đăng nhập hệ thống</h1>
            <p class="form-description">Nhập email công ty và mật khẩu được cấp để truy cập vào hệ thống.</p>

            <%-- Thông báo lỗi xác thực hoặc tài khoản bị khóa --%>
            <%
                String errorMessage = (String) request.getAttribute("errorMessage");
                if (errorMessage != null && !errorMessage.isBlank()) {
            %>
                <div class="alert alert-danger" role="alert" id="loginAlertError">
                    <%= errorMessage %>
                </div>
            <%
                }
            %>

            <%-- Thông báo phiên hết hạn do không có hoạt động (S1-02-AC3) --%>
            <%
                String timeoutParam = request.getParameter("timeout");
                if ("1".equals(timeoutParam)) {
            %>
                <div class="alert alert-warning" role="alert" id="loginAlertTimeout">
                    Phiên đăng nhập đã hết hạn do không có hoạt động. Vui lòng đăng nhập lại để tiếp tục làm việc.
                </div>
            <%
                }
            %>

            <%-- Thông báo đăng xuất thành công (S1-02-AC2) --%>
            <%
                String logoutParam = request.getParameter("logout");
                if ("1".equals(logoutParam)) {
            %>
                <div class="alert alert-info" role="alert" id="loginAlertLogout">
                    Bạn đã đăng xuất an toàn khỏi hệ thống.
                </div>
            <%
                }
            %>

            <form action="${pageContext.request.contextPath}/login" method="post" id="loginForm" novalidate>
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

                <div class="form-group">
                    <label class="form-label" for="matKhauInput">Mật khẩu</label>
                    <input type="password"
                           id="matKhauInput"
                           name="matKhau"
                           class="form-input"
                           placeholder="••••••••"
                           required
                           autocomplete="current-password">
                </div>

                <div class="form-group" style="margin-top: 24px;">
                    <button type="submit" class="btn btn-primary btn-block" id="btnLoginSubmit">
                        Đăng nhập
                    </button>
                </div>
            </form>
        </main>
    </div>

    <script>
        // Client-side validation cơ bản bằng tiếng Việt
        document.getElementById('loginForm').addEventListener('submit', function (e) {
            var email = document.getElementById('emailInput').value.trim();
            var matKhau = document.getElementById('matKhauInput').value;

            if (!email || !matKhau) {
                e.preventDefault();
                var alertBox = document.getElementById('loginAlertError');
                if (!alertBox) {
                    alertBox = document.createElement('div');
                    alertBox.className = 'alert alert-danger';
                    alertBox.id = 'loginAlertError';
                    var form = document.getElementById('loginForm');
                    form.parentNode.insertBefore(alertBox, form);
                }
                alertBox.textContent = 'Vui lòng nhập đầy đủ email công ty và mật khẩu.';
            }
        });
    </script>
</body>
</html>
