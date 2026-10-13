<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="vn.nhom10.crm.dto.ThongTinDieuHuongDTO" %>
<%@ page import="vn.nhom10.crm.model.NguoiDung" %>
<%@ page import="vn.nhom10.crm.model.ModuleHeThong" %>
<%@ page import="vn.nhom10.crm.dto.MucMenuDTO" %>
<%@ page import="vn.nhom10.crm.service.MenuService" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Comparator" %>
<%
    ThongTinDieuHuongDTO navData = (ThongTinDieuHuongDTO) request.getAttribute("thongTinDieuHuong");
    if (navData == null) {
        navData = new ThongTinDieuHuongDTO();
    }
    NguoiDung currentUser = (NguoiDung) request.getAttribute("nguoiDungHienTai");
    if (currentUser == null) {
        currentUser = new NguoiDung();
    }

    ModuleHeThong[] allModules = ModuleHeThong.values();
    Arrays.sort(allModules, Comparator.comparingInt(ModuleHeThong::getThuTu));
    MenuService menuService = MenuService.getInstance();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Menu Điều Hướng Phân Quyền | CRM Bán Hàng (S1-06)</title>
    <!-- Google Fonts: Plus Jakarta Sans -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <!-- CSS Navigation & Responsive cho 360px -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/navigation.css">
</head>
<body class="crm-body">

    <!-- Bao gồm Component Điều hướng Navigation (Top bar + Sidebar Drawer) -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <!-- Main Content Area -->
    <main class="crm-main-content" id="crm-main-content">
        <div class="crm-container">

            <!-- Banner Story Info -->
            <div class="crm-story-card">
                <div class="crm-story-header">
                    <span class="crm-pill crm-pill-epic">EP-01 Xác thực & Phân quyền</span>
                    <span class="crm-pill crm-pill-story">Story S1-06</span>
                    <span class="crm-pill crm-pill-fe">FE: Ngô Trung Kiên</span>
                    <span class="crm-pill crm-pill-role">BE: Thào A Khua</span>
                </div>
                <h1 class="crm-story-title">Kiểm thử Menu Điều Hướng Theo Phân Quyền & Responsive 360px</h1>
                <p class="crm-story-desc">
                    Hệ thống tự động lọc các mục menu dựa trên vai trò của người dùng trong phiên làm việc.
                    Mục menu không thuộc quyền được loại bỏ hoàn toàn phía server.
                    Giao diện thanh điều hướng hỗ trợ mở/đóng drawer và cử chỉ vuốt chạm mượt mà trên thiết bị di động 360px.
                </p>
            </div>

            <!-- Acceptance Criteria Checklist Status -->
            <div class="crm-ac-card">
                <h2 class="crm-section-title">Tiêu chuẩn nghiệm thu (Acceptance Criteria)</h2>
                <div class="crm-ac-grid">
                    <div class="crm-ac-item pass">
                        <div class="crm-ac-icon">
                            <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                        </div>
                        <div class="crm-ac-info">
                            <strong>AC 1: Mục menu không thuộc quyền thì không hiển thị</strong>
                            <p>Hiển thị <strong><%= navData.getSoLuongMenu() %> / 12</strong> module khả dụng. Đã ẩn <strong><%= 12 - navData.getSoLuongMenu() %></strong> module không thuộc quyền.</p>
                        </div>
                    </div>
                    <div class="crm-ac-item pass">
                        <div class="crm-ac-icon">
                            <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                        </div>
                        <div class="crm-ac-info">
                            <strong>AC 2: Hiển thị tên, vai trò và nhóm kinh doanh</strong>
                            <p>Họ tên: <strong><%= navData.getHoTen() %></strong> | Vai trò: <strong><%= navData.getVaiTroHienThi() %></strong> | Nhóm: <strong><%= navData.getTenNhomKinhDoanh() %></strong></p>
                        </div>
                    </div>
                    <div class="crm-ac-item pass">
                        <div class="crm-ac-icon">
                            <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                        </div>
                        <div class="crm-ac-info">
                            <strong>AC 3: Dùng được thuận tiện trên màn hình 360px</strong>
                            <p>Touch target tối thiểu 44px, hamburger drawer mượt mà, hỗ trợ touch swipe, không tràn ngang.</p>
                        </div>
                    </div>
                </div>
            </div>



            <!-- Current User State Summary -->
            <div class="crm-summary-card">
                <h2 class="crm-section-title">Hồ sơ người dùng hiện tại (Session State - AC 2)</h2>
                <div class="crm-meta-grid">
                    <div class="crm-meta-col">
                        <div class="crm-meta-label">Họ và tên:</div>
                        <div class="crm-meta-value highlight"><%= navData.getHoTen() %></div>
                    </div>
                    <div class="crm-meta-col">
                        <div class="crm-meta-label">Email tài khoản:</div>
                        <div class="crm-meta-value"><%= navData.getEmail() %></div>
                    </div>
                    <div class="crm-meta-col">
                        <div class="crm-meta-label">Vai trò hiển thị:</div>
                        <div class="crm-meta-value highlight">
                            <span class="crm-badge crm-badge-role"><%= navData.getVaiTroHienThi() %></span>
                        </div>
                    </div>
                    <div class="crm-meta-col">
                        <div class="crm-meta-label">Nhóm kinh doanh:</div>
                        <div class="crm-meta-value highlight">
                            <span class="crm-badge crm-badge-team"><%= navData.getTenNhomKinhDoanh() %></span>
                        </div>
                    </div>
                </div>

                <h3 class="crm-sub-title">Danh sách mục menu được cấp quyền hiển thị (<%= navData.getSoLuongMenu() %> mục):</h3>
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

            <!-- Module Permission Verification Matrix (Chứng minh AC 1) -->
            <div class="crm-matrix-card">
                <div class="crm-card-header-flex">
                    <div>
                        <h2 class="crm-section-title">Ma trận kiểm tra phân quyền 12 Module (Chứng minh AC 1)</h2>
                        <p class="crm-switcher-help">Đối chiếu danh sách hiển thị trên menu và kiểm tra chặn quyền trực tiếp phía server:</p>
                    </div>
                    <span class="crm-badge crm-badge-matrix">12 Module</span>
                </div>
                <div class="crm-table-responsive">
                    <table class="crm-matrix-table">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Tên Module</th>
                                <th>Đường dẫn URL</th>
                                <th>Trạng thái Menu</th>
                                <th>Quyền truy cập Server</th>
                                <th>Kiểm thử thực tế</th>
                            </tr>
                        </thead>
                        <tbody>
                            <%
                                for (ModuleHeThong mod : allModules) {
                                    boolean coQuyen = menuService.kiemTraQuyenTruyCap(currentUser, mod);
                            %>
                            <tr class="<%= coQuyen ? "row-permitted" : "row-denied" %>">
                                <td><%= mod.getThuTu() %></td>
                                <td><strong><%= mod.getTenHienThi() %></strong></td>
                                <td><code><%= mod.getDuongDanUrl() %></code></td>
                                <td>
                                    <% if (coQuyen) { %>
                                        <span class="crm-badge-status badge-visible">
                                            <span class="material-symbols-outlined" aria-hidden="true">visibility</span>
                                            Hiển thị trên menu
                                        </span>
                                    <% } else { %>
                                        <span class="crm-badge-status badge-hidden">
                                            <span class="material-symbols-outlined" aria-hidden="true">visibility_off</span>
                                            Ẩn (Không thuộc quyền)
                                        </span>
                                    <% } %>
                                </td>
                                <td>
                                    <% if (coQuyen) { %>
                                        <span class="crm-text-success">Cho phép truy cập</span>
                                    <% } else { %>
                                        <span class="crm-text-denied">Chặn truy cập (Lỗi 403)</span>
                                    <% } %>
                                </td>
                                <td>
                                    <% if (coQuyen) { %>
                                        <a href="<%= request.getContextPath() %><%= mod.getDuongDanUrl() %>" class="crm-btn-table-action btn-allowed" title="Truy cập module được cấp phép">
                                            Mở chức năng
                                        </a>
                                    <% } else { %>
                                        <a href="<%= request.getContextPath() %><%= mod.getDuongDanUrl() %>" class="crm-btn-table-action btn-test-denied" title="Thử gõ trực tiếp URL để kiểm tra lỗi 403">
                                            Thử truy cập URL (Kiểm tra 403)
                                        </a>
                                    <% } %>
                                </td>
                            </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- 360px Mobile Simulator Section (Chứng minh AC 3) -->
            <div class="crm-simulator-section">
                <div class="crm-simulator-header">
                    <div class="crm-simulator-header-left">
                        <h2 class="crm-section-title">Mô phỏng trải nghiệm trên màn hình 360px (Chứng minh AC 3)</h2>
                        <p class="crm-simulator-hint">
                            Kiểm tra trực quan layout, touch target tối thiểu 44px, hamburger drawer và thông tin hồ sơ trên kích thước 360px:
                        </p>
                    </div>
                    <div class="crm-simulator-toolbar">
                        <span class="crm-dimensions-badge" id="simulator-dimensions-badge">360 × 640 px</span>
                        <button type="button" class="crm-btn-sim-action" id="btn-simulator-toggle-menu">
                            <span class="material-symbols-outlined" aria-hidden="true">menu</span>
                            Mở menu trên mô phỏng
                        </button>
                    </div>
                </div>

                <div class="crm-simulator-presets">
                    <button type="button" class="crm-btn-size-preset active" data-width="360" data-height="640">
                        Chuẩn 360px (360×640)
                    </button>
                    <button type="button" class="crm-btn-size-preset" data-width="375" data-height="667">
                        iPhone SE (375×667)
                    </button>
                    <button type="button" class="crm-btn-size-preset" data-width="390" data-height="844">
                        iPhone 14 (390×844)
                    </button>
                    <button type="button" class="crm-btn-size-preset" data-width="100%" data-height="600">
                        Toàn chiều rộng (Fluid)
                    </button>
                </div>

                <div class="crm-simulator-wrapper">
                    <div class="crm-simulator-phone" id="simulator-phone">
                        <div class="crm-simulator-notch"></div>
                        <iframe src="<%= request.getContextPath() %>/dieu-huong" class="crm-simulator-iframe" title="Mô phỏng màn hình di động 360px"></iframe>
                    </div>
                </div>
            </div>

        </div>
    </main>

    <!-- JavaScript Navigation -->
    <script src="<%= request.getContextPath() %>/assets/js/navigation.js"></script>
</body>
</html>

