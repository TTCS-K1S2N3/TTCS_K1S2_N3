<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="vn.nhom10.crm.dto.ThongTinDieuHuongDTO" %>
<%@ page import="vn.nhom10.crm.model.NguoiDung" %>
<%@ page import="vn.nhom10.crm.dto.MucMenuDTO" %>
<%
    ThongTinDieuHuongDTO navData = (ThongTinDieuHuongDTO) request.getAttribute("thongTinDieuHuong");
    NguoiDung currentUser = (NguoiDung) request.getAttribute("nguoiDungHienTai");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Menu Điều Hướng Phân Quyền | CRM Bán Hàng</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/navigation.css">
</head>
<body class="crm-body">

    <!-- Bao gồm Component Điều hướng Navigation (Top bar + Sidebar) -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <!-- Main Content Area -->
    <main class="crm-main-content" id="crm-main-content">
        <div class="crm-container">

            <!-- Banner Story Info -->
            <div class="crm-story-card">
                <div class="crm-story-header">
                    <span class="crm-pill crm-pill-epic">EP-01</span>
                    <span class="crm-pill crm-pill-story">S1-09 & S1-06</span>
                    <span class="crm-pill crm-pill-role">BE: Thào A Khua</span>
                </div>
                <h1 class="crm-story-title">Kiểm thử Menu Điều Hướng & Gán Vai Trò Phân Quyền</h1>
                <p class="crm-story-desc">
                    Hệ thống tự động lọc các mục menu dựa trên vai trò của người dùng.
                    Mục menu không thuộc quyền sẽ bị ẩn hoàn toàn khỏi danh sách menu phía server.
                </p>
                <div style="margin-top: 14px;">
                    <a href="<%= request.getContextPath() %>/nguoi-dung/phan-quyen" class="crm-btn-role active" style="display: inline-flex; width: auto; padding: 8px 16px;">
                        &rarr; Đến Trang Gán Vai Trò & Nhóm Kinh Doanh (S1-09)
                    </a>
                </div>
            </div>

            <!-- Acceptance Criteria Checklist Status -->
            <div class="crm-ac-card">
                <h2 class="crm-section-title">Tiêu chuẩn nghiệm thu (Acceptance Criteria)</h2>
                <div class="crm-ac-grid">
                    <div class="crm-ac-item pass">
                        <div class="crm-ac-icon">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>
                        </div>
                        <div class="crm-ac-info">
                            <strong>Mục menu không thuộc quyền thì không hiển thị</strong>
                            <p>Số mục menu hiện tại: <strong><%= navData.getSoLuongMenu() %></strong> mục.</p>
                        </div>
                    </div>
                    <div class="crm-ac-item pass">
                        <div class="crm-ac-icon">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>
                        </div>
                        <div class="crm-ac-info">
                            <strong>Hiển thị tên, vai trò và nhóm kinh doanh</strong>
                            <p>Họ tên: <strong><%= navData.getHoTen() %></strong> | Nhóm: <strong><%= navData.getTenNhomKinhDoanh() %></strong></p>
                        </div>
                    </div>
                    <div class="crm-ac-item pass">
                        <div class="crm-ac-icon">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>
                        </div>
                        <div class="crm-ac-info">
                            <strong>Dùng được thuận tiện trên màn hình 360px</strong>
                            <p>Hamburger drawer mở/đóng mượt mà, touch target >= 44px, không tràn ngang.</p>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Role Switcher Panel for Testing All 7 Roles -->
            <div class="crm-role-switcher-card">
                <h2 class="crm-section-title">Chuyển đổi vai trò kiểm thử (7 Vai Trò Hệ Thống)</h2>
                <p class="crm-switcher-help">Nhấp chọn một vai trò để kiểm tra sự thay đổi của menu điều hướng và thông tin người dùng:</p>
                <div class="crm-role-buttons">
                    <a href="?vaiTro=ADMIN" class="crm-btn-role <%= currentUser.coVaiTro("ADMIN") ? "active" : "" %>">
                        <span class="role-title">Quản trị hệ thống</span>
                        <span class="role-code">Admin</span>
                    </a>
                    <a href="?vaiTro=DIRECTOR" class="crm-btn-role <%= currentUser.coVaiTro("DIRECTOR") ? "active" : "" %>">
                        <span class="role-title">Giám đốc kinh doanh</span>
                        <span class="role-code">Director</span>
                    </a>
                    <a href="?vaiTro=TEAM_LEAD" class="crm-btn-role <%= currentUser.coVaiTro("TEAM_LEAD") && !currentUser.coVaiTro("SALES_REP") ? "active" : "" %>">
                        <span class="role-title">Trưởng nhóm kinh doanh</span>
                        <span class="role-code">Team Lead</span>
                    </a>
                    <a href="?vaiTro=SALES_REP" class="crm-btn-role <%= currentUser.coVaiTro("SALES_REP") && !currentUser.coVaiTro("TEAM_LEAD") ? "active" : "" %>">
                        <span class="role-title">Nhân viên kinh doanh</span>
                        <span class="role-code">Sales Rep</span>
                    </a>
                    <a href="?vaiTro=MARKETING" class="crm-btn-role <%= currentUser.coVaiTro("MARKETING") ? "active" : "" %>">
                        <span class="role-title">Nhân viên Marketing</span>
                        <span class="role-code">Marketing</span>
                    </a>
                    <a href="?vaiTro=CUST_SUCCESS" class="crm-btn-role <%= currentUser.coVaiTro("CUST_SUCCESS") ? "active" : "" %>">
                        <span class="role-title">Chăm sóc khách hàng</span>
                        <span class="role-code">Cust. Success</span>
                    </a>
                    <a href="?vaiTro=ACCOUNTANT" class="crm-btn-role <%= currentUser.coVaiTro("ACCOUNTANT") ? "active" : "" %>">
                        <span class="role-title">Kế toán</span>
                        <span class="role-code">Accountant</span>
                    </a>
                    <a href="?vaiTro=MULTI" class="crm-btn-role <%= currentUser.coVaiTro("TEAM_LEAD") && currentUser.coVaiTro("SALES_REP") ? "active" : "" %>">
                        <span class="role-title">Người dùng Đa vai trò</span>
                        <span class="role-code">Team Lead + Sales</span>
                    </a>
                </div>
            </div>

            <!-- Current User State Summary -->
            <div class="crm-summary-card">
                <h2 class="crm-section-title">Thông tin người dùng hiện tại trong Session</h2>
                <div class="crm-meta-grid">
                    <div class="crm-meta-col">
                        <div class="crm-meta-label">Họ và tên:</div>
                        <div class="crm-meta-value highlight"><%= navData.getHoTen() %></div>
                    </div>
                    <div class="crm-meta-col">
                        <div class="crm-meta-label">Email:</div>
                        <div class="crm-meta-value"><%= navData.getEmail() %></div>
                    </div>
                    <div class="crm-meta-col">
                        <div class="crm-meta-label">Vai trò:</div>
                        <div class="crm-meta-value highlight"><%= navData.getVaiTroHienThi() %></div>
                    </div>
                    <div class="crm-meta-col">
                        <div class="crm-meta-label">Nhóm kinh doanh:</div>
                        <div class="crm-meta-value highlight"><%= navData.getTenNhomKinhDoanh() %></div>
                    </div>
                </div>

                <h3 class="crm-sub-title">Danh sách mục menu đang được cấp quyền hiển thị (<%= navData.getSoLuongMenu() %> mục):</h3>
                <div class="crm-menu-tags">
                    <% for (MucMenuDTO m : navData.getDanhSachMucMenu()) { %>
                        <span class="crm-menu-tag">
                            <span class="tag-order"><%= m.getThuTu() %></span>
                            <span class="tag-name"><%= m.getTenHienThi() %></span>
                            <code class="tag-url"><%= m.getUrl() %></code>
                        </span>
                    <% } %>
                </div>
            </div>

        </div>
    </main>

    <!-- JavaScript Navigation -->
    <script src="<%= request.getContextPath() %>/assets/js/navigation.js"></script>
</body>
</html>
