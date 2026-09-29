<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ page import="vn.nhom10.crm.dto.ThongTinLoi" %>
<%
    ThongTinLoi thongTinLoi = (ThongTinLoi) request.getAttribute("thongTinLoi");
    if (thongTinLoi == null) {
        // Fallback an toàn nếu JSP được gọi mà chưa qua Servlet
        int code = response.getStatus() > 0 ? response.getStatus() : 404;
        vn.nhom10.crm.service.BaoLoiService service = new vn.nhom10.crm.service.BaoLoiService();
        thongTinLoi = service.taoThongTinLoi(code, request.getRequestURI(), null, request.getContextPath(), null);
    }

    String badgeClass = "badge-info";
    if ("warning".equals(thongTinLoi.getLoaiGiaoDien())) {
        badgeClass = "badge-warning";
    } else if ("danger".equals(thongTinLoi.getLoaiGiaoDien())) {
        badgeClass = "badge-danger";
    }

    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= thongTinLoi.getTieuDe() %> - CRM Bán Hàng</title>
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/error/error.css">
</head>
<body>
    <!-- AC1: Dùng chung header giao diện ứng dụng -->
    <header class="app-header">
        <a href="<%= contextPath %>/" class="brand-container">
            <div class="brand-icon">CRM</div>
            <div>
                <div class="brand-title">CRM Bán Hàng</div>
                <div class="brand-subtitle">Hệ thống quản lý khách hàng và quy trình bán hàng</div>
            </div>
        </a>
        <nav class="header-nav">
            <a href="<%= contextPath %>/" class="header-link">Bàn làm việc</a>
            <a href="<%= contextPath %>/dang-nhap" class="header-link">Đăng nhập</a>
        </nav>
    </header>

    <main class="error-main">
        <div class="error-card">
            <div class="error-badge <%= badgeClass %>">
                <span><%= thongTinLoi.getBieuTuong() != null ? thongTinLoi.getBieuTuong() : "⚠️" %></span>
                <span>MÃ LỖI: <%= thongTinLoi.getMaLoi() %></span>
            </div>

            <div class="error-icon-display">
                <%= thongTinLoi.getBieuTuong() != null ? thongTinLoi.getBieuTuong() : "⚠️" %>
            </div>

            <h1 class="error-title"><%= thongTinLoi.getTieuDe() %></h1>

            <p class="error-description"><%= thongTinLoi.getMoTa() %></p>

            <% if (thongTinLoi.getChiTiet() != null && !thongTinLoi.getChiTiet().isEmpty()) { %>
                <div class="error-detail-box">
                    <%= thongTinLoi.getChiTiet() %>
                </div>
            <% } %>

            <!-- AC2: Hành động gợi ý để quay lại luồng làm việc -->
            <div class="action-box">
                <div class="action-box-header">
                    <span>💡</span>
                    <span>Gợi ý hành động tiếp theo:</span>
                </div>
                <div class="action-buttons">
                    <% if (thongTinLoi.getUrlHanhDongChinh() != null) { %>
                        <a href="<%= thongTinLoi.getUrlHanhDongChinh() %>" class="btn-action-primary" id="btn-hanh-dong-chinh">
                            <%= thongTinLoi.getTenHanhDongChinh() %>
                        </a>
                    <% } %>

                    <% if (thongTinLoi.getUrlHanhDongPhu() != null) { %>
                        <a href="<%= thongTinLoi.getUrlHanhDongPhu() %>" class="btn-action-secondary" id="btn-hanh-dong-phu">
                            <%= thongTinLoi.getTenHanhDongPhu() %>
                        </a>
                    <% } %>
                </div>
            </div>

            <div class="quick-links">
                <span>Hoặc chuyển nhanh đến:</span>
                <a href="<%= contextPath %>/">Trang chủ</a>
                <span>•</span>
                <a href="<%= contextPath %>/dang-nhap">Đăng nhập lại</a>
            </div>
        </div>
    </main>

    <footer class="app-footer">
        © 2026 CRM Bán Hàng - Hệ thống quản lý quan hệ khách hàng và bán hàng chuyên nghiệp.
    </footer>
</body>
</html>
