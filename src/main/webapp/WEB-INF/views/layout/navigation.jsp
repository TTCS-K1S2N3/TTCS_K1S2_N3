<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="vn.nhom10.crm.dto.ThongTinDieuHuongDTO" %>
<%@ page import="vn.nhom10.crm.dto.MucMenuDTO" %>
<%
    ThongTinDieuHuongDTO navData = (ThongTinDieuHuongDTO) request.getAttribute("thongTinDieuHuong");
    if (navData == null) {
        navData = new ThongTinDieuHuongDTO();
    }
%>

<!-- Import Google Material Symbols Outlined má»™t láº§n duy nháº¥t táº¡i layout chung -->
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200">

<!-- Mobile Top Header Bar (Tá»‘i Æ°u cho mÃ n hÃ¬nh 360px vÃ  Ä‘iá»‡n thoáº¡i) -->
<header class="crm-top-navbar" id="crm-top-navbar" role="banner">
    <div class="crm-nav-left">
        <button type="button" class="crm-btn-hamburger" id="btn-menu-toggle" aria-label="Má»Ÿ menu Ä‘iá»u hÆ°á»›ng" aria-expanded="false" aria-controls="crm-sidebar">
            <span class="material-symbols-outlined" aria-hidden="true">menu</span>
        </button>
        <a href="<%= request.getContextPath() %>/dieu-huong" class="crm-brand-title" title="CRM BÃ¡n HÃ ng">
            <span class="crm-brand-logo-badge">CRM</span>
            <span class="crm-brand-name">BÃN HÃ€NG</span>
        </a>
    </div>
    <div class="crm-nav-right">
        <button type="button" class="crm-mobile-user-avatar-btn" id="btn-mobile-user-profile" aria-label="Xem há»“ sÆ¡ ngÆ°á»i dÃ¹ng: <%= navData.getHoTen() %>" title="<%= navData.getHoTen() %> (<%= navData.getVaiTroHienThi() %>)">
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
<aside class="crm-sidebar" id="crm-sidebar" role="navigation" aria-label="Menu Ä‘iá»u hÆ°á»›ng chÃ­nh">
    <!-- Sidebar Header -->
    <div class="crm-sidebar-header">
        <a href="<%= request.getContextPath() %>/dieu-huong" class="crm-brand" title="CRM BÃ¡n HÃ ng">
            <div class="crm-brand-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                    <circle cx="8.5" cy="7.5" r="4"></circle>
                    <polyline points="17 11 19 13 23 9"></polyline>
                </svg>
            </div>
            <div class="crm-brand-text">
                <div class="crm-app-title">CRM BÃN HÃ€NG</div>
                <div class="crm-app-subtitle">Há»‡ thá»‘ng quáº£n lÃ½ khÃ¡ch hÃ ng</div>
            </div>
        </a>
        <button type="button" class="crm-btn-close-sidebar" id="btn-close-sidebar" aria-label="ÄÃ³ng menu Ä‘iá»u hÆ°á»›ng">
            <span class="material-symbols-outlined" aria-hidden="true">close</span>
        </button>
    </div>

    <!-- User Profile Box (AC: Hiá»ƒn thá»‹ TÃªn, Vai trÃ² vÃ  NhÃ³m kinh doanh) -->
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
            <span class="crm-status-dot" title="Äang hoáº¡t Ä‘á»™ng" aria-label="Tráº¡ng thÃ¡i trá»±c tuyáº¿n"></span>
        </div>
        <div class="crm-user-meta">
            <!-- TÃªn ngÆ°á»i dÃ¹ng -->
            <div class="crm-user-name" id="user-display-name" title="<%= navData.getHoTen() %>">
                <%= navData.getHoTen() %>
            </div>

            <!-- Vai trÃ² ngÆ°á»i dÃ¹ng -->
            <div class="crm-user-roles" id="user-display-role" title="Vai trÃ²: <%= navData.getVaiTroHienThi() %>">
                <span class="crm-badge crm-badge-role">
                    <span class="material-symbols-outlined crm-badge-icon" aria-hidden="true">badge</span>
                    <%= navData.getVaiTroHienThi() %>
                </span>
            </div>

            <!-- NhÃ³m kinh doanh Ä‘ang thuá»™c vá» -->
            <div class="crm-user-team" id="user-display-team" title="NhÃ³m: <%= navData.getTenNhomKinhDoanh() %>">
                <span class="crm-badge crm-badge-team">
                    <span class="material-symbols-outlined crm-badge-icon" aria-hidden="true">groups</span>
                    <%= navData.getTenNhomKinhDoanh() %>
                </span>
            </div>
        </div>
    </a>

    <!-- Navigation Menu Items (AC: Má»¥c menu khÃ´ng thuá»™c quyá»n thÃ¬ khÃ´ng hiá»ƒn thá»‹) -->
    <nav class="crm-nav-menu" id="crm-nav-menu" aria-label="Danh má»¥c chá»©c nÄƒng">
        <div class="crm-menu-section-label">
            <span>CHá»¨C NÄ‚NG Há»† THá»NG</span>
            <span class="crm-menu-count-badge"><%= navData.getSoLuongMenu() %></span>
        </div>
        <ul class="crm-menu-list">
            <%
                if (navData.getDanhSachMucMenu() == null || navData.getDanhSachMucMenu().isEmpty()) {
            %>
            <li class="crm-menu-empty">
                <span class="material-symbols-outlined crm-menu-empty-icon" aria-hidden="true">info</span>
                <span>ChÆ°a cÃ³ chá»©c nÄƒng kháº£ dá»¥ng cho tÃ i khoáº£n nÃ y</span>
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
                <a href="javascript:void(0)" class="crm-menu-link crm-menu-link-disabled" data-module="<%= item.getMaModule() %>" aria-disabled="true" title="Chá»©c nÄƒng Ä‘ang Ä‘Æ°á»£c phÃ¡t triá»ƒn trong cÃ¡c Sprint tiáº¿p theo (Sprint 3-8)">
                    <span class="crm-menu-icon" aria-hidden="true">
                        <span class="material-symbols-outlined" aria-hidden="true"><%= iconName %></span>
                    </span>
                    <span class="crm-menu-title"><%= item.getTenHienThi() %></span>
                    <span class="crm-menu-badge-dev" aria-hidden="true">Sáº¯p ra máº¯t</span>
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
        <a href="<%= request.getContextPath() %>/dang-xuat" class="crm-logout-link" title="ÄÄƒng xuáº¥t khá»i há»‡ thá»‘ng">
            <span class="material-symbols-outlined crm-logout-icon" aria-hidden="true">logout</span>
            <span class="crm-logout-text">ÄÄƒng xuáº¥t</span>
        </a>
    </div>
</aside>
