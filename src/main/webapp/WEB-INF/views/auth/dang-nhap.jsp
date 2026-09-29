<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng Nhập | CRM Bán Hàng</title>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth/doi-mat-khau.css">
    <style>
        .login-card {
            max-width: 440px;
            margin: 60px auto;
            background: #ffffff;
            border-radius: 16px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
            padding: 36px 32px;
            border: 1px solid #e2e8f0;
        }
        .login-header {
            text-align: center;
            margin-bottom: 24px;
        }
        .login-header h1 {
            font-size: 22px;
            font-weight: 700;
            color: #0f172a;
            margin-bottom: 8px;
        }
        .login-header p {
            font-size: 14px;
            color: #64748b;
        }
        .demo-credentials {
            margin-top: 20px;
            padding: 12px;
            background: #f8fafc;
            border-radius: 8px;
            font-size: 13px;
            color: #475569;
            border-left: 3px solid #3b82f6;
        }
    </style>
</head>
<body class="crm-body">

    <div class="login-card">
        <div class="login-header">
            <h1>Đăng Nhập Hệ Thống CRM</h1>
            <p>Nhập email và mật khẩu của bạn để tiếp tục</p>
        </div>

        <% if (request.getAttribute("thongBaoLoi") != null) { %>
            <div class="alert alert-error" style="margin-bottom: 20px;">
                <div class="alert-content">
                    <p class="alert-message"><%= request.getAttribute("thongBaoLoi") %></p>
                </div>
            </div>
        <% } %>

        <form action="${pageContext.request.contextPath}/dang-nhap" method="POST">
            <div class="form-group" style="margin-bottom: 16px;">
                <label for="email" class="form-label">Email tài khoản</label>
                <input type="email" id="email" name="email" class="form-control" value="sales@crm.vn" required autofocus />
            </div>

            <div class="form-group" style="margin-bottom: 24px;">
                <label for="matKhau" class="form-label">Mật khẩu</label>
                <input type="password" id="matKhau" name="matKhau" class="form-control" placeholder="Nhập mật khẩu" required />
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%;">
                Đăng Nhập
            </button>
        </form>

        <div class="demo-credentials">
            <strong>Tài khoản thử nghiệm:</strong><br>
            Email: <code>sales@crm.vn</code><br>
            Mật khẩu mặc định: <code>MatKhau@123</code>
        </div>
    </div>

</body>
</html>
