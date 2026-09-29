<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Danh Mục Khách Hàng - CRM Bán Hàng</title>
    <style>
        :root {
            --primary: #2563eb;
            --primary-hover: #1d4ed8;
            --bg: #f8fafc;
            --card: #ffffff;
            --text: #0f172a;
            --text-muted: #64748b;
            --border: #e2e8f0;
            --success: #10b981;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            background-color: var(--bg);
            color: var(--text);
            min-height: 100vh;
        }
        .navbar {
            background-color: var(--card);
            border-bottom: 1px solid var(--border);
            padding: 14px 28px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .nav-brand {
            font-weight: 700;
            font-size: 18px;
            color: var(--primary);
            display: flex;
            align-items: center;
            gap: 8px;
            text-decoration: none;
        }
        .user-nav {
            display: flex;
            align-items: center;
            gap: 16px;
        }
        .user-badge {
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .avatar {
            width: 36px;
            height: 36px;
            border-radius: 50%;
            background-color: var(--primary);
            color: #fff;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 600;
            font-size: 13px;
        }
        .user-info {
            text-align: right;
            font-size: 13px;
        }
        .user-name { font-weight: 600; color: var(--text); }
        .user-role { color: var(--text-muted); font-size: 12px; }
        .btn-logout {
            padding: 7px 14px;
            background-color: #f1f5f9;
            color: var(--text);
            border: 1px solid var(--border);
            border-radius: 6px;
            font-size: 13px;
            font-weight: 500;
            text-decoration: none;
            transition: all 0.2s;
        }
        .btn-logout:hover {
            background-color: #fee2e2;
            color: #dc2626;
            border-color: #fca5a5;
        }
        .main-container {
            max-width: 1100px;
            margin: 32px auto;
            padding: 0 20px;
        }
        .welcome-card {
            background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%);
            color: #ffffff;
            border-radius: 12px;
            padding: 28px 32px;
            margin-bottom: 24px;
            box-shadow: 0 4px 14px rgba(37, 99, 235, 0.2);
        }
        .welcome-card h2 { font-size: 24px; margin-bottom: 8px; }
        .welcome-card p { opacity: 0.9; font-size: 14px; line-height: 1.6; }
        .content-card {
            background-color: var(--card);
            border-radius: 12px;
            border: 1px solid var(--border);
            padding: 24px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.05);
        }
        .content-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
            padding-bottom: 14px;
            border-bottom: 1px solid var(--border);
        }
        .content-header h3 { font-size: 18px; font-weight: 600; }
        .role-tags { display: flex; gap: 6px; margin-top: 6px; }
        .tag {
            display: inline-block;
            padding: 3px 8px;
            border-radius: 4px;
            background-color: rgba(255,255,255,0.2);
            font-size: 12px;
            font-weight: 500;
        }
        .customer-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 14px;
        }
        .customer-table th, .customer-table td {
            padding: 12px 14px;
            text-align: left;
            border-bottom: 1px solid var(--border);
        }
        .customer-table th {
            background-color: #f8fafc;
            color: var(--text-muted);
            font-weight: 600;
        }
        .badge-active {
            background-color: #ecfdf5;
            color: #065f46;
            padding: 3px 8px;
            border-radius: 12px;
            font-size: 12px;
            font-weight: 500;
        }
    </style>
</head>
<body>
    <nav class="navbar">
        <a href="${pageContext.request.contextPath}/khach-hang" class="nav-brand">
            💼 CRM Bán Hàng
        </a>
        <div class="user-nav">
            <div class="user-badge">
                <div class="avatar">${nguoiDungHienTai.tenVietTat}</div>
                <div class="user-info">
                    <div class="user-name"><c:out value="${nguoiDungHienTai.hoTen}"/></div>
                    <div class="user-role"><c:out value="${nguoiDungHienTai.email}"/></div>
                </div>
            </div>
            <a href="${pageContext.request.contextPath}/dang-xuat" class="btn-logout">Đăng Xuất</a>
        </div>
    </nav>

    <div class="main-container">
        <div class="welcome-card">
            <h2>Xin chào, <c:out value="${nguoiDungHienTai.hoTen}"/>! 👋</h2>
            <p>Bạn đã đăng nhập thành công vào Hệ thống Quản lý Khách hàng CRM với quyền tương ứng.</p>
            <div class="role-tags">
                <c:forEach items="${nguoiDungHienTai.danhSachVaiTro}" var="vt">
                    <span class="tag"><c:out value="${vt.tenVaiTro}"/></span>
                </c:forEach>
            </div>
        </div>

        <div class="content-card">
            <div class="content-header">
                <h3>📁 Danh Mục Khách Hàng Thuộc Quyền Phụ Trách</h3>
                <span class="badge-active">Đã xác thực bảo mật</span>
            </div>
            <table class="customer-table">
                <thead>
                    <tr>
                        <th>Mã KH</th>
                        <th>Tên Doanh Nghiệp</th>
                        <th>Lĩnh Vực</th>
                        <th>Người Phụ Trách</th>
                        <th>Trạng Thái</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td><strong>KH-001</strong></td>
                        <td>Tập đoàn Công nghệ Alpha Tech</td>
                        <td>Phần mềm & Viễn thông</td>
                        <td><c:out value="${nguoiDungHienTai.hoTen}"/></td>
                        <td><span class="badge-active">Đang chăm sóc</span></td>
                    </tr>
                    <tr>
                        <td><strong>KH-002</strong></td>
                        <td>Công ty Cổ phần Giải pháp Số Beta</td>
                        <td>Thương mại điện tử</td>
                        <td><c:out value="${nguoiDungHienTai.hoTen}"/></td>
                        <td><span class="badge-active">Tiềm năng</span></td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</body>
</html>
