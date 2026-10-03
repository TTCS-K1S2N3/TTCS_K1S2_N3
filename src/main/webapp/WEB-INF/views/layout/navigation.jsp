<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="vn.nhom10.crm.dto.ThongTinDieuHuongDTO" %>
<%@ page import="vn.nhom10.crm.dto.MucMenuDTO" %>
<%
    ThongTinDieuHuongDTO navData = (ThongTinDieuHuongDTO) request.getAttribute("thongTinDieuHuong");
    if (navData == null) {
        navData = new ThongTinDieuHuongDTO();
    }
%>

<!-- Mobile Top Header Bar (Tối ưu cho màn hình 360px và điện thoại) -->
<header class="crm-top-navbar" id="crm-top-navbar" role="banner">
    <div class="crm-nav-left">
        <button type="button" class="crm-btn-hamburger" id="btn-menu-toggle" aria-label="Mở menu điều hướng" aria-expanded="false" aria-controls="crm-sidebar">
            <svg class="crm-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <line x1="3" y1="12" x2="21" y2="12"></line>
                <line x1="3" y1="6" x2="21" y2="6"></line>
                <line x1="3" y1="18" x2="21" y2="18"></line>
            </svg>
        </button>
        <a href="<%= request.getContextPath() %>/dieu-huong" class="crm-brand-title" title="CRM Bán Hàng">
            <span class="crm-brand-logo-badge">CRM</span>
            <span class="crm-brand-name">BÁN HÀNG</span>
        </a>
    </div>
    <div class="crm-nav-right">
        <button type="button" class="crm-mobile-user-avatar-btn" id="btn-mobile-user-profile" aria-label="Xem hồ sơ người dùng: <%= navData.getHoTen() %>" title="<%= navData.getHoTen() %> (<%= navData.getVaiTroHienThi() %>)">
            <span class="crm-mobile-user-avatar">
                <% if (navData.getAnhDaiDienThumbUrl() != null && !navData.getAnhDaiDienThumbUrl().isBlank()) { %>
                    <img src="<%= navData.getAnhDaiDienThumbUrl() %>" alt="<%= navData.getHoTen() %>" class="crm-user-avatar-img" width="36" height="36" onerror="this.style.display='none'; this.nextElementSibling.style.display='inline';">
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
            <svg class="crm-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
        </button>
    </div>

    <!-- User Profile Box (AC: Hiển thị Tên, Vai trò và Nhóm kinh doanh) -->
    <a href="<%= request.getContextPath() %>/ho-so" class="crm-user-profile-box" id="crm-user-profile-card" style="text-decoration: none; color: inherit;">
        <div class="crm-user-avatar-wrap">
            <div class="crm-user-avatar" title="<%= navData.getHoTen() %>">
                <% if (navData.getAnhDaiDienThumbUrl() != null && !navData.getAnhDaiDienThumbUrl().isBlank()) { %>
                    <img src="<%= navData.getAnhDaiDienThumbUrl() %>" alt="<%= navData.getHoTen() %>" class="crm-user-avatar-img" width="44" height="44" onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
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
                    <svg class="crm-badge-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                        <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"></path>
                    </svg>
                    <%= navData.getVaiTroHienThi() %>
                </span>
            </div>

            <!-- Nhóm kinh doanh đang thuộc về -->
            <div class="crm-user-team" id="user-display-team" title="Nhóm: <%= navData.getTenNhomKinhDoanh() %>">
                <span class="crm-badge crm-badge-team">
                    <svg class="crm-badge-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                        <circle cx="9" cy="7" r="4"></circle>
                        <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                        <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                    </svg>
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
                <svg class="crm-menu-empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <circle cx="12" cy="12" r="10"></circle>
                    <line x1="12" y1="8" x2="12" y2="12"></line>
                    <line x1="12" y1="16" x2="12.01" y2="16"></line>
                </svg>
                <span>Chưa có chức năng khả dụng cho tài khoản này</span>
            </li>
            <%
                } else {
                    for (MucMenuDTO item : navData.getDanhSachMucMenu()) {
                        String activeClass = item.isActive() ? " active" : "";
                        String ariaCurrent = item.isActive() ? " aria-current=\"page\"" : "";
            %>
            <li class="crm-menu-item">
                <a href="<%= request.getContextPath() %><%= item.getUrl() %>" class="crm-menu-link<%= activeClass %>" data-module="<%= item.getMaModule() %>"<%= ariaCurrent %>>
                    <span class="crm-menu-icon" aria-hidden="true">
                        <% if ("overview".equals(item.getBieuTuong())) { %>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.21 15.89A10 10 0 1 1 8 2.83"></path><path d="M22 12A10 10 0 0 0 12 2v10z"></path></svg>
                        <% } else if ("users".equals(item.getBieuTuong())) { %>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
                        <% } else if ("target".equals(item.getBieuTuong())) { %>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><circle cx="12" cy="12" r="6"></circle><circle cx="12" cy="12" r="2"></circle></svg>
                        <% } else if ("briefcase".equals(item.getBieuTuong())) { %>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path></svg>
                        <% } else if ("calendar".equals(item.getBieuTuong())) { %>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
                        <% } else if ("file-text".equals(item.getBieuTuong())) { %>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>
                        <% } else if ("check-circle".equals(item.getBieuTuong())) { %>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>
                        <% } else if ("trending-up".equals(item.getBieuTuong())) { %>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"></polyline><polyline points="17 6 23 6 23 12"></polyline></svg>
                        <% } else if ("bar-chart".equals(item.getBieuTuong())) { %>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="20" x2="12" y2="10"></line><line x1="18" y1="20" x2="18" y2="4"></line><line x1="6" y1="20" x2="6" y2="16"></line></svg>
                        <% } else if ("bell".equals(item.getBieuTuong())) { %>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path><path d="M13.73 21a2 2 0 0 1-3.46 0"></path></svg>
                        <% } else if ("settings".equals(item.getBieuTuong())) { %>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"></circle><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path></svg>
                        <% } else if ("shield".equals(item.getBieuTuong())) { %>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg>
                        <% } else { %>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
                        <% } %>
                    </span>
                    <span class="crm-menu-title"><%= item.getTenHienThi() %></span>
                    <% if (item.isActive()) { %>
                        <span class="crm-active-indicator" aria-hidden="true"></span>
                    <% } %>
                </a>
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
            <svg class="crm-logout-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                <polyline points="16 17 21 12 16 7"></polyline>
                <line x1="21" y1="12" x2="9" y2="12"></line>
            </svg>
            <span class="crm-logout-text">Đăng xuất</span>
        </a>
    </div>
</aside>

