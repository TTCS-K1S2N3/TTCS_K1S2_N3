<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ page import="vn.nhom10.crm.dto.ThongTinLoi" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%
    ThongTinLoi thongTinLoi = (ThongTinLoi) request.getAttribute("thongTinLoi");
    if (thongTinLoi == null) {
        // Fallback an toàn nếu JSP được gọi trực tiếp mà chưa qua ErrorHandlerServlet
        int code = response.getStatus() > 0 ? response.getStatus() : 404;
        vn.nhom10.crm.service.BaoLoiService service = new vn.nhom10.crm.service.BaoLoiService();
        thongTinLoi = service.taoThongTinLoi(code, request.getRequestURI(), null, request.getContextPath(), null);
    }

    String contextPath = request.getContextPath();
    if (contextPath == null) {
        contextPath = "";
    }
    String cleanContextPath = contextPath.replaceAll("/+$", "");
    String urlTrangChu = cleanContextPath.isEmpty() ? "/" : cleanContextPath;

    // Xác định kiểu giao diện dựa trên loại lỗi
    String themeClass = "theme-info";
    String badgeClass = "badge-info";
    if ("warning".equals(thongTinLoi.getLoaiGiaoDien())) {
        themeClass = "theme-warning";
        badgeClass = "badge-warning";
    } else if ("danger".equals(thongTinLoi.getLoaiGiaoDien())) {
        themeClass = "theme-danger";
        badgeClass = "badge-danger";
    }

    // Kiểm tra thông tin người dùng trong phiên làm việc (nếu đã đăng nhập)
    HttpSession currentSession = request.getSession(false);
    String tenNguoiDung = null;
    String vaiTroNguoiDung = null;
    boolean daDangNhap = false;

    if (currentSession != null) {
        Object sessionUser = currentSession.getAttribute("nguoiDung");
        if (sessionUser != null) {
            daDangNhap = true;
            try {
                java.lang.reflect.Method m = sessionUser.getClass().getMethod("getHoTen");
                tenNguoiDung = (String) m.invoke(sessionUser);
            } catch (Exception ignored) {}
            if (tenNguoiDung == null) {
                try {
                    java.lang.reflect.Method m = sessionUser.getClass().getMethod("getTenDangNhap");
                    tenNguoiDung = (String) m.invoke(sessionUser);
                } catch (Exception ignored) {}
            }
        }
        if (tenNguoiDung == null) {
            if (currentSession.getAttribute("hoTen") != null) {
                tenNguoiDung = (String) currentSession.getAttribute("hoTen");
                daDangNhap = true;
            } else if (currentSession.getAttribute("tenDangNhap") != null) {
                tenNguoiDung = (String) currentSession.getAttribute("tenDangNhap");
                daDangNhap = true;
            } else if (currentSession.getAttribute("username") != null) {
                tenNguoiDung = (String) currentSession.getAttribute("username");
                daDangNhap = true;
            }
        }
    }

    String thoiGianLoi = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    String requestUriDisplay = request.getRequestURI();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Trang thông báo sự cố và điều hướng quay lại luồng làm việc hệ thống CRM">
    <title><%= thongTinLoi.getTieuDe() %> - CRM Bán Hàng</title>
    <link rel="stylesheet" href="<%= cleanContextPath %>/assets/css/error/error.css">
</head>
<body>

    <!-- ====================================================================
         AC1: DÙNG CHUNG GIAO DIỆN ỨNG DỤNG - HEADER HỆ THỐNG CRM
         ==================================================================== -->
    <header class="app-header" id="app-header">
        <div class="app-header-inner">
            <!-- Brand / Logo -->
            <a href="<%= urlTrangChu %>" class="brand-link" id="brand-link">
                <div class="brand-badge">CRM</div>
                <div class="brand-text-group">
                    <span class="brand-title">CRM Bán Hàng</span>
                    <span class="brand-subtitle">Quản lý Khách hàng & Quy trình Bán hàng</span>
                </div>
            </a>

            <!-- Menu điều hướng chính dùng chung của hệ thống -->
            <nav class="main-nav" aria-label="Điều hướng chính">
                <a href="<%= urlTrangChu %>" class="nav-link">
                    <span class="nav-icon">📊</span>
                    <span>Bàn làm việc</span>
                </a>
                <a href="<%= cleanContextPath %>/khach-hang" class="nav-link">
                    <span class="nav-icon">👥</span>
                    <span>Khách hàng</span>
                </a>
                <a href="<%= cleanContextPath %>/co-hoi" class="nav-link">
                    <span class="nav-icon">💼</span>
                    <span>Cơ hội</span>
                </a>
                <a href="<%= cleanContextPath %>/bao-gia" class="nav-link">
                    <span class="nav-icon">📄</span>
                    <span>Báo giá</span>
                </a>
                <a href="<%= cleanContextPath %>/hop-dong" class="nav-link">
                    <span class="nav-icon">📑</span>
                    <span>Hợp đồng</span>
                </a>
                <a href="<%= cleanContextPath %>/bao-cao" class="nav-link">
                    <span class="nav-icon">📈</span>
                    <span>Báo cáo</span>
                </a>
            </nav>

            <!-- Khu vực tài khoản / Đăng nhập -->
            <div class="user-actions">
                <% if (daDangNhap && tenNguoiDung != null) { %>
                    <div class="user-badge-logged" id="user-badge-logged">
                        <div class="user-avatar-circle">
                            <%= tenNguoiDung.substring(0, 1).toUpperCase() %>
                        </div>
                        <div class="user-meta">
                            <span class="user-name"><%= tenNguoiDung %></span>
                            <span class="user-role-label">Thành viên hệ thống</span>
                        </div>
                    </div>
                    <a href="<%= cleanContextPath %>/dang-xuat" class="btn-header-logout" id="btn-header-logout" title="Đăng xuất khỏi hệ thống">
                        Đăng xuất
                    </a>
                <% } else { %>
                    <a href="<%= cleanContextPath %>/dang-nhap" class="btn-header-login" id="btn-header-login">
                        <span>🔑</span>
                        <span>Đăng nhập</span>
                    </a>
                <% } %>

                <!-- Nút bật menu trên thiết bị di động -->
                <button type="button" class="mobile-nav-toggle" id="btn-mobile-menu-toggle" aria-label="Mở danh mục điều hướng">
                    ☰
                </button>
            </div>
        </div>
    </header>

    <!-- Drawer menu cho thiết bị di động -->
    <div class="mobile-menu-drawer" id="mobile-menu-drawer">
        <ul class="mobile-menu-list">
            <li><a href="<%= urlTrangChu %>"><span>📊</span> Bàn làm việc</a></li>
            <li><a href="<%= cleanContextPath %>/khach-hang"><span>👥</span> Quản lý Khách hàng</a></li>
            <li><a href="<%= cleanContextPath %>/co-hoi"><span>💼</span> Quản lý Cơ hội bán hàng</a></li>
            <li><a href="<%= cleanContextPath %>/bao-gia"><span>📄</span> Danh sách Báo giá</a></li>
            <li><a href="<%= cleanContextPath %>/hop-dong"><span>📑</span> Quản lý Hợp đồng</a></li>
            <li><a href="<%= cleanContextPath %>/bao-cao"><span>📈</span> Báo cáo kinh doanh</a></li>
            <% if (!daDangNhap) { %>
                <li><a href="<%= cleanContextPath %>/dang-nhap"><span>🔑</span> Đăng nhập vào hệ thống</a></li>
            <% } else { %>
                <li><a href="<%= cleanContextPath %>/dang-xuat"><span>🚪</span> Đăng xuất tài khoản</a></li>
            <% } %>
        </ul>
    </div>

    <!-- Thanh định vị đường dẫn (Breadcrumb) -->
    <div class="breadcrumb-wrapper">
        <div class="breadcrumb-container">
            <span class="breadcrumb-item"><a href="<%= urlTrangChu %>">Trang chủ</a></span>
            <span class="breadcrumb-separator">›</span>
            <span class="breadcrumb-item"><a href="<%= urlTrangChu %>">Hệ thống</a></span>
            <span class="breadcrumb-separator">›</span>
            <span class="breadcrumb-current">Thông báo lỗi <%= thongTinLoi.getMaLoi() %></span>
        </div>
    </div>

    <!-- ====================================================================
         KHU VỰC THÔNG BÁO LỖI VÀ GỢI Ý ĐIỀU HƯỚNG
         ==================================================================== -->
    <main class="error-main-content">
        <div class="error-card-container <%= themeClass %>" id="error-card-container">

            <!-- Huy hiệu mã lỗi HTTP -->
            <div class="error-status-badge <%= badgeClass %>" id="error-status-badge">
                <span class="badge-dot"></span>
                <span>MÃ PHẢN HỒI: <%= thongTinLoi.getMaLoi() %></span>
            </div>

            <!-- Biểu tượng minh họa -->
            <div class="error-visual-icon" id="error-visual-icon">
                <%= thongTinLoi.getBieuTuong() != null ? thongTinLoi.getBieuTuong() : "⚠️" %>
            </div>

            <!-- Tiêu đề lỗi rõ ràng -->
            <h1 class="error-main-title" id="error-main-title">
                <%= thongTinLoi.getTieuDe() %>
            </h1>

            <!-- Diễn giải thân thiện cho người dùng -->
            <p class="error-main-description" id="error-main-description">
                <%= thongTinLoi.getMoTa() %>
            </p>

            <!-- Hướng dẫn cụ thể theo từng loại lỗi -->
            <% if (thongTinLoi.getChiTiet() != null && !thongTinLoi.getChiTiet().trim().isEmpty()) { %>
                <div class="error-guidance-box" id="error-guidance-box">
                    <span class="guidance-icon">ℹ️</span>
                    <span class="guidance-text"><%= thongTinLoi.getChiTiet() %></span>
                </div>
            <% } %>

            <!-- ================================================================
                 AC2: HÀNH ĐỘNG GỢI Ý ĐỂ QUAY LẠI LUỒNG LÀM VIỆC
                 ================================================================ -->
            <div class="action-recommendation-panel" id="action-recommendation-panel">
                <div class="recommendation-header">
                    <span class="recommendation-header-icon">💡</span>
                    <span class="recommendation-header-title">Hành động gợi ý để tiếp tục công việc:</span>
                </div>

                <div class="action-button-group">
                    <!-- Nút hành động chính (Primary Action) -->
                    <% if (thongTinLoi.getUrlHanhDongChinh() != null) { %>
                        <% if (thongTinLoi.getUrlHanhDongChinh().startsWith("javascript:history.back")) { %>
                            <a href="#" class="btn-action-primary btn-smart-back" id="btn-hanh-dong-chinh" data-fallback-url="<%= urlTrangChu %>">
                                <span>↩️</span>
                                <span><%= thongTinLoi.getTenHanhDongChinh() %></span>
                            </a>
                        <% } else if (thongTinLoi.getUrlHanhDongChinh().startsWith("javascript:window.location.reload")) { %>
                            <button type="button" class="btn-action-primary" id="btn-hanh-dong-chinh" onclick="window.location.reload()">
                                <span>🔄</span>
                                <span><%= thongTinLoi.getTenHanhDongChinh() %></span>
                            </button>
                        <% } else { %>
                            <a href="<%= thongTinLoi.getUrlHanhDongChinh() %>" class="btn-action-primary" id="btn-hanh-dong-chinh">
                                <span>⚡</span>
                                <span><%= thongTinLoi.getTenHanhDongChinh() %></span>
                            </a>
                        <% } %>
                    <% } %>

                    <!-- Nút hành động phụ (Secondary Action) -->
                    <% if (thongTinLoi.getUrlHanhDongPhu() != null) { %>
                        <% if (thongTinLoi.getUrlHanhDongPhu().startsWith("javascript:history.back")) { %>
                            <a href="#" class="btn-action-secondary btn-smart-back" id="btn-hanh-dong-phu" data-fallback-url="<%= urlTrangChu %>">
                                <span>↩️</span>
                                <span><%= thongTinLoi.getTenHanhDongPhu() %></span>
                            </a>
                        <% } else { %>
                            <a href="<%= thongTinLoi.getUrlHanhDongPhu() %>" class="btn-action-secondary" id="btn-hanh-dong-phu">
                                <span><%= thongTinLoi.getTenHanhDongPhu() %></span>
                            </a>
                        <% } %>
                    <% } %>

                    <!-- Nút tiện ích sao chép mã sự cố (Dành riêng cho lỗi 500) -->
                    <% if (thongTinLoi.getMaThamChieu() != null && !thongTinLoi.getMaThamChieu().trim().isEmpty()) { %>
                        <button type="button" class="btn-action-copy" id="btn-copy-error-code" data-error-code="<%= thongTinLoi.getMaThamChieu() %>" title="Sao chép mã sự cố gửi đội ngũ kỹ thuật IT">
                            <span>📋</span>
                            <span>Sao chép mã lỗi (<%= thongTinLoi.getMaThamChieu() %>)</span>
                        </button>
                    <% } %>
                </div>
            </div>

            <!-- Điều hướng nhanh tới các phân hệ nghiệp vụ chính -->
            <div class="quick-navigation-section">
                <div class="quick-nav-title">Chuyển nhanh đến phân hệ làm việc khác:</div>
                <div class="quick-nav-grid">
                    <a href="<%= urlTrangChu %>" class="quick-nav-tile">
                        <span class="quick-nav-tile-icon">📊</span>
                        <span class="quick-nav-tile-name">Bàn làm việc</span>
                    </a>
                    <a href="<%= cleanContextPath %>/khach-hang" class="quick-nav-tile">
                        <span class="quick-nav-tile-icon">👥</span>
                        <span class="quick-nav-tile-name">Khách hàng</span>
                    </a>
                    <a href="<%= cleanContextPath %>/co-hoi" class="quick-nav-tile">
                        <span class="quick-nav-tile-icon">💼</span>
                        <span class="quick-nav-tile-name">Cơ hội bán hàng</span>
                    </a>
                    <a href="<%= cleanContextPath %>/bao-gia" class="quick-nav-tile">
                        <span class="quick-nav-tile-icon">📄</span>
                        <span class="quick-nav-tile-name">Báo giá</span>
                    </a>
                </div>
            </div>

            <!-- Khối thông tin kỹ thuật mở rộng dành cho quản trị viên / hỗ trợ IT -->
            <div class="technical-details-wrapper">
                <button type="button" class="technical-toggle-btn" id="btn-toggle-tech-details" aria-expanded="false">
                    <span>⚙️ Chi tiết kỹ thuật tra cứu</span>
                    <span class="toggle-arrow">▼</span>
                </button>
                <div class="technical-panel-content" id="tech-details-panel">
                    <div class="tech-row">
                        <span class="tech-label">Đường dẫn yêu cầu:</span>
                        <span class="tech-value"><%= requestUriDisplay != null ? requestUriDisplay : "Không xác định" %></span>
                    </div>
                    <div class="tech-row">
                        <span class="tech-label">Phương thức HTTP:</span>
                        <span class="tech-value"><%= request.getMethod() %></span>
                    </div>
                    <div class="tech-row">
                        <span class="tech-label">Mã phản hồi:</span>
                        <span class="tech-value"><%= thongTinLoi.getMaLoi() %></span>
                    </div>
                    <div class="tech-row">
                        <span class="tech-label">Thời gian phát sinh:</span>
                        <span class="tech-value"><%= thoiGianLoi %> (Asia/Ho_Chi_Minh)</span>
                    </div>
                    <% if (thongTinLoi.getMaThamChieu() != null) { %>
                        <div class="tech-row">
                            <span class="tech-label">Mã tra cứu sự cố:</span>
                            <span class="tech-value"><%= thongTinLoi.getMaThamChieu() %></span>
                        </div>
                    <% } %>
                </div>
            </div>

        </div>
    </main>

    <!-- ====================================================================
         AC1: DÙNG CHUNG GIAO DIỆN ỨNG DỤNG - FOOTER HỆ THỐNG CRM
         ==================================================================== -->
    <footer class="app-footer" id="app-footer">
        <div class="app-footer-inner">
            <div>
                © 2026 CRM Bán Hàng - Hệ thống quản lý quan hệ khách hàng và quy trình bán hàng chuyên nghiệp.
            </div>
            <div class="footer-contact-info">
                <span>📞 Hotline: <strong>1900 6868</strong></span>
                <span>✉️ Email: <a href="mailto:support@crm.vn" class="footer-contact-link">support@crm.vn</a></span>
            </div>
        </div>
    </footer>

    <!-- Script tương tác frontend -->
    <script src="<%= cleanContextPath %>/assets/js/error/error.js"></script>
</body>
</html>
