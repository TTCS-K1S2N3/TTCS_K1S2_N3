<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="vn.nhom10.crm.model.NguoiDung" %>
<%@ page import="vn.nhom10.crm.model.VaiTro" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    NguoiDung user = (NguoiDung) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    String tieuDeTrangChu = (String) request.getAttribute("tieuDeTrangChu");
    String moTaVaiTro = (String) request.getAttribute("moTaVaiTro");
    VaiTro vaiTroChinh = (VaiTro) request.getAttribute("vaiTroChinh");

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    String lanDangNhapCuoiStr = user.getLanDangNhapCuoi() != null ? sdf.format(user.getLanDangNhapCuoi()) : "Lần đầu đăng nhập";
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= tieuDeTrangChu != null ? tieuDeTrangChu : "Trang chủ" %> — CRM Bán Hàng B2B</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>

    <header class="app-navbar">
        <a href="${pageContext.request.contextPath}/home" class="navbar-brand">
            <span>CRM BÁN HÀNG B2B</span>
        </a>

        <div class="navbar-user">
            <div class="user-badge">
                <div class="user-name"><%= user.getHoTen() %></div>
                <div class="user-role-tag">
                    <%= vaiTroChinh != null ? vaiTroChinh.getTenVaiTro() : "Người dùng" %>
                </div>
            </div>
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline btn-sm" id="btnLogout">
                Đăng xuất
            </a>
        </div>
    </header>

    <main class="workspace-container">
        <section class="workspace-header">
            <h1 id="homeRoleTitle"><%= tieuDeTrangChu %></h1>
            <p><%= moTaVaiTro %></p>
        </section>

        <section class="info-grid">
            <article class="info-card">
                <h2>Hồ sơ tài khoản</h2>
                <div class="info-row">
                    <span class="info-label">Họ và tên:</span>
                    <span class="info-value"><%= user.getHoTen() %></span>
                </div>
                <div class="info-row">
                    <span class="info-label">Email công ty:</span>
                    <span class="info-value"><%= user.getEmail() %></span>
                </div>
                <div class="info-row">
                    <span class="info-label">Trạng thái:</span>
                    <span class="info-value"><%= user.getTrangThai() %></span>
                </div>
                <div class="info-row">
                    <span class="info-label">Lần đăng nhập cuối:</span>
                    <span class="info-value"><%= lanDangNhapCuoiStr %></span>
                </div>
            </article>

            <article class="info-card">
                <h2>Vai trò & Phân quyền</h2>
                <div class="info-row">
                    <span class="info-label">Vai trò chính:</span>
                    <span class="info-value"><%= vaiTroChinh != null ? vaiTroChinh.getTenVaiTro() : "Chưa gán vai trò" %></span>
                </div>
                <div class="info-row">
                    <span class="info-label">Mã vai trò:</span>
                    <span class="info-value"><%= vaiTroChinh != null ? vaiTroChinh.getMaVaiTro() : "N/A" %></span>
                </div>
                <div class="info-row">
                    <span class="info-label">Phạm vi mặc định:</span>
                    <span class="info-value"><%= (vaiTroChinh != null && vaiTroChinh.getPhamViMacDinh() != null) ? vaiTroChinh.getPhamViMacDinh() : "Mặc định hệ thống" %></span>
                </div>
                <div class="info-row">
                    <span class="info-label">Tổng vai trò gán:</span>
                    <span class="info-value"><%= user.getDanhSachVaiTro() != null ? user.getDanhSachVaiTro().size() : 0 %></span>
                </div>
            </article>

            <article class="info-card">
                <h2>Bảo mật & Phiên làm việc</h2>
                <div class="info-row">
                    <span class="info-label">Phiên bản phiên:</span>
                    <span class="info-value">v<%= user.getSessionVersion() %></span>
                </div>
                <div class="info-row">
                    <span class="info-label">Cơ chế lưu phiên:</span>
                    <span class="info-value">HttpSession (Server-side)</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Bảo vệ phiên:</span>
                    <span class="info-value">HttpOnly Cookie</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Thời hạn phiên:</span>
                    <span class="info-value">30 phút không hoạt động</span>
                </div>
            </article>
        </section>
    </main>

</body>
</html>
