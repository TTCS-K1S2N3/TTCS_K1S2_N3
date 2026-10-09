<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="vn.nhom10.crm.dto.ThongTinDieuHuongDTO" %>
<%@ page import="vn.nhom10.crm.dto.MucMenuDTO" %>
<%
    ThongTinDieuHuongDTO navData = (ThongTinDieuHuongDTO) request.getAttribute("thongTinDieuHuong");
    if (navData == null) {
        navData = new ThongTinDieuHuongDTO();
    }
    String navThumbUrl = navData.getAnhDaiDienThumbUrl();
    if (navThumbUrl != null && !navThumbUrl.isBlank()) {
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isBlank() && !"/".equals(ctx) && !navThumbUrl.startsWith(ctx) && !navThumbUrl.startsWith("http")) {
            navThumbUrl = ctx + (navThumbUrl.startsWith("/") ? navThumbUrl : "/" + navThumbUrl);
        }
    }
%>

<!-- Import Google Material Symbols Outlined một lần duy nhất tại layout chung -->
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200">

<!-- Mobile Top Header Bar (Tối ưu cho màn hình 360px và điện thoại) -->
<header class="crm-top-navbar" id="crm-top-navbar" role="banner">
    <div class="crm-nav-left">
        <button type="button" class="crm-btn-hamburger" id="btn-menu-toggle" aria-label="Mở menu điều hướng" aria-expanded="false" aria-controls="crm-sidebar">
            <span class="material-symbols-outlined" aria-hidden="true">menu</span>
        </button>
        <a href="<%= request.getContextPath() %>/dieu-huong" class="crm-brand-title" title="CRM Bán Hàng">
            <span class="crm-brand-logo-badge">CRM</span>
            <span class="crm-brand-name">BÁN HÀNG</span>
        </a>
    </div>
    <div class="crm-nav-right">
        <button type="button" class="crm-mobile-user-avatar-btn" id="btn-mobile-user-profile" aria-label="Xem hồ sơ người dùng: <%= navData.getHoTen() %>" title="<%= navData.getHoTen() %> (<%= navData.getVaiTroHienThi() %>)">
            <span class="crm-mobile-user-avatar">
                <% if (navThumbUrl != null && !navThumbUrl.isBlank()) { %>
                    <img src="<%= navThumbUrl %>" alt="<%= navData.getHoTen() %>" class="crm-user-avatar-img" width="36" height="36" onerror="this.style.display='none'; this.nextElementSibling.style.display='inline';">
                    <span style="display:none;"><%= navData.getTenVietTat() %></span>
                <% } else { %>
                    <%= navData.getTenVietTat() %>
                <% } %>
            </span>
        </button>
    </div>
</header>

<!-- Backdrop Overlay for Mobile 360px Drawer -->
<div class="crm-sidebar-overlay" id="crm-sidebar-overlay" aria-hidden="true"></div>

<!-- Sidebar Navigation Drawer -->
<aside class="crm-sidebar" id="crm-sidebar" role="navigation" aria-label="Menu điều hướng chính">
    <!-- Sidebar Header -->
    <div class="crm-sidebar-header">
        <a href="<%= request.getContextPath() %>/dieu-huong" class="crm-brand" title="CRM Bán Hàng">
            <div class="crm-brand-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                    <circle cx="8.5" cy="7.5" r="4"></circle>
                    <polyline points="17 11 19 13 23 9"></polyline>
                </svg>
            </div>
            <div class="crm-brand-text">
                <div class="crm-app-title">CRM BÁN HÀNG</div>
                <div class="crm-app-subtitle">Hệ thống quản lý khách hàng</div>
            </div>
        </a>
        <button type="button" class="crm-btn-close-sidebar" id="btn-close-sidebar" aria-label="Đóng menu điều hướng">
            <span class="material-symbols-outlined" aria-hidden="true">close</span>
        </button>
    </div>

    <!-- User Profile Box (AC: Hiển thị Tên, Vai trò và Nhóm kinh doanh) -->
    <a href="<%= request.getContextPath() %>/ho-so" class="crm-user-profile-box" id="crm-user-profile-card" style="text-decoration: none; color: inherit;">
        <div class="crm-user-avatar-wrap">
            <div class="crm-user-avatar" title="<%= navData.getHoTen() %>">
                <% if (navThumbUrl != null && !navThumbUrl.isBlank()) { %>
                    <img src="<%= navThumbUrl %>" alt="<%= navData.getHoTen() %>" class="crm-user-avatar-img" width="44" height="44" onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
                    <span style="display:none;"><%= navData.getTenVietTat() %></span>
                <% } else { %>
                    <%= navData.getTenVietTat() %>
                <% } %>
            </div>
            <span class="crm-status-dot" title="Đang hoạt động" aria-label="Trạng thái trực tuyến"></span>
        </div>
        <div class="crm-user-meta">
            <!-- Tên người dùng -->
            <div class="crm-user-name" id="user-display-name" title="<%= navData.getHoTen() %>">
                <%= navData.getHoTen() %>
            </div>

            <!-- Vai trò người dùng -->
            <div class="crm-user-roles" id="user-display-role" title="Vai trò: <%= navData.getVaiTroHienThi() %>">
                <span class="crm-badge crm-badge-role">
                    <span class="material-symbols-outlined crm-badge-icon" aria-hidden="true">badge</span>
                    <%= navData.getVaiTroHienThi() %>
                </span>
            </div>

            <!-- Nhóm kinh doanh đang thuộc về -->
            <div class="crm-user-team" id="user-display-team" title="Nhóm: <%= navData.getTenNhomKinhDoanh() %>">
                <span class="crm-badge crm-badge-team">
                    <span class="material-symbols-outlined crm-badge-icon" aria-hidden="true">groups</span>
                    <%= navData.getTenNhomKinhDoanh() %>
                </span>
            </div>
        </div>
    </a>

    <!-- Navigation Menu Items (AC: Mục menu không thuộc quyền thì không hiển thị) -->
    <nav class="crm-nav-menu" id="crm-nav-menu" aria-label="Danh mục chức năng">
        <div class="crm-menu-section-label">
            <span>CHỨC NĂNG HỆ THỐNG</span>
            <span class="crm-menu-count-badge"><%= navData.getSoLuongMenu() %></span>
        </div>
        <ul class="crm-menu-list">
            <%
                if (navData.getDanhSachMucMenu() == null || navData.getDanhSachMucMenu().isEmpty()) {
            %>
            <li class="crm-menu-empty">
                <span class="material-symbols-outlined crm-menu-empty-icon" aria-hidden="true">info</span>
                <span>Chưa có chức năng khả dụng cho tài khoản này</span>
            </li>
            <%
                } else {
                    for (MucMenuDTO item : navData.getDanhSachMucMenu()) {
                        String activeClass = item.isActive() ? " active" : "";
                        String ariaCurrent = item.isActive() ? " aria-current=\"page\"" : "";
            %>
            <li class="crm-menu-item">
                <%
                    String iconName = "widgets";
                    String bt = item.getBieuTuong();
                    if ("overview".equals(bt)) { iconName = "dashboard"; }
                    else if ("users".equals(bt)) { iconName = "group"; }
                    else if ("target".equals(bt)) { iconName = "track_changes"; }
                    else if ("briefcase".equals(bt)) { iconName = "conversion_path"; }
                    else if ("calendar".equals(bt)) { iconName = "calendar_today"; }
                    else if ("file-text".equals(bt)) { iconName = "description"; }
                    else if ("check-circle".equals(bt)) { iconName = "verified"; }
                    else if ("trending-up".equals(bt)) { iconName = "trending_up"; }
                    else if ("bar-chart".equals(bt)) { iconName = "bar_chart"; }
                    else if ("bell".equals(bt)) { iconName = "notifications"; }
                    else if ("settings".equals(bt)) { iconName = "category"; }
                    else if ("shield".equals(bt)) { iconName = "admin_panel_settings"; }
                %>
                <% if (item.isDaTrienKhai()) { %>
                <a href="<%= request.getContextPath() %><%= item.getUrl() %>" class="crm-menu-link<%= activeClass %>" data-module="<%= item.getMaModule() %>"<%= ariaCurrent %>>
                    <span class="crm-menu-icon" aria-hidden="true">
                        <span class="material-symbols-outlined" aria-hidden="true"><%= iconName %></span>
                    </span>
                    <span class="crm-menu-title"><%= item.getTenHienThi() %></span>
                    <% if (item.isActive()) { %>
                        <span class="crm-active-indicator" aria-hidden="true"></span>
                    <% } %>
                </a>
                <% } else { %>
                <a href="javascript:void(0)" class="crm-menu-link crm-menu-link-disabled" data-module="<%= item.getMaModule() %>" aria-disabled="true" title="Chức năng đang được phát triển trong các Sprint tiếp theo (Sprint 3-8)">
                    <span class="crm-menu-icon" aria-hidden="true">
                        <span class="material-symbols-outlined" aria-hidden="true"><%= iconName %></span>
                    </span>
                    <span class="crm-menu-title"><%= item.getTenHienThi() %></span>
                    <span class="crm-menu-badge-dev" aria-hidden="true">Sắp ra mắt</span>
                </a>
                <% } %>
            </li>
            <%
                    }
                }
            %>
        </ul>
    </nav>

    <!-- Sidebar Footer -->
    <div class="crm-sidebar-footer">
        <a href="<%= request.getContextPath() %>/dang-xuat" class="crm-logout-link" title="Đăng xuất khỏi hệ thống">
            <span class="material-symbols-outlined crm-logout-icon" aria-hidden="true">logout</span>
            <span class="crm-logout-text">Đăng xuất</span>
        </a>
    </div>
</aside>
