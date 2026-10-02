<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="vn.nhom10.crm.model.NguoiDung" %>
<%@ page import="vn.nhom10.crm.dto.ThongTinDieuHuongDTO" %>
<%@ page import="vn.nhom10.crm.dto.MucMenuDTO" %>
<%
    NguoiDung nguoiDung = (NguoiDung) request.getAttribute("nguoiDung");
    ThongTinDieuHuongDTO dieuHuong = (ThongTinDieuHuongDTO) request.getAttribute("dieuHuong");
    String thongBaoThanhCong = (String) request.getAttribute("thongBaoThanhCong");
    String thongBaoLoi = (String) request.getAttribute("thongBaoLoi");

    if (nguoiDung == null) {
        nguoiDung = new NguoiDung();
    }
    if (dieuHuong == null) {
        dieuHuong = new ThongTinDieuHuongDTO();
    }

    String contextPath = request.getContextPath();
    String avatarUrl = (dieuHuong.getAnhDaiDienUrl() != null)
            ? dieuHuong.getAnhDaiDienUrl()
            : contextPath + "/avatar?id=" + nguoiDung.getId();
    String thumbUrl = (dieuHuong.getAnhDaiDienThumbUrl() != null)
            ? dieuHuong.getAnhDaiDienThumbUrl()
            : contextPath + "/avatar?id=" + nguoiDung.getId() + "&thumb=true";
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0">
    <title>Hồ sơ cá nhân & Ảnh đại diện | CRM Bán Hàng</title>
    <!-- Bootstrap Icons (CDN) -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <!-- Style chính của module Hồ sơ & Avatar -->
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/ho-so/avatar.css">
</head>
<body>

<div class="crm-layout">

    <!-- Mobile Backdrop -->
    <div class="sidebar-backdrop" id="sidebarBackdrop"></div>

    <!-- Sidebar Navigation: Mục menu không thuộc quyền thì KHÔNG hiển thị -->
    <aside class="crm-sidebar" id="crmSidebar">
        <div class="sidebar-header">
            <a href="<%= contextPath %>/ho-so" class="crm-brand">
                <span class="brand-badge">CRM</span>
                <span>Bán Hàng</span>
            </a>
        </div>

        <div class="sidebar-menu-title">Chức năng khả dụng (<%= dieuHuong.getDanhSachMucMenu().size() %>)</div>
        <ul class="sidebar-nav">
            <%
                for (MucMenuDTO menu : dieuHuong.getDanhSachMucMenu()) {
            %>
            <li>
                <a href="<%= contextPath %><%= menu.getDuongDanUrl() %>"
                   class="nav-link <%= menu.isDangChon() ? "active" : "" %>">
                    <i class="bi <%= menu.getBieuTuong() %> nav-icon"></i>
                    <span><%= menu.getTenHienThi() %></span>
                </a>
            </li>
            <%
                }
            %>
        </ul>
    </aside>

    <!-- Main Container -->
    <div class="crm-main">

        <!-- Top Header Navigation -->
        <header class="crm-header">
            <div class="header-left">
                <button type="button" class="btn-sidebar-toggle" id="sidebarToggleBtn" aria-label="Mở menu">
                    <i class="bi bi-list"></i>
                </button>
                <h1 class="header-title">Hồ sơ cá nhân</h1>
            </div>

            <div class="header-right">
                <!-- User Pill: Hiển thị tên, vai trò và ảnh thu nhỏ thumbnail -->
                <a href="<%= contextPath %>/ho-so" class="user-header-pill" title="Xem hồ sơ cá nhân">
                    <img src="<%= thumbUrl %>"
                         alt="Avatar <%= dieuHuong.getHoTen() %>"
                         class="user-header-thumb"
                         id="avatarPreviewHeader">
                    <div class="user-header-info">
                        <span class="user-header-name"><%= dieuHuong.getHoTen() %></span>
                        <span class="user-header-role"><%= dieuHuong.getVaiTroHienThi() %></span>
                    </div>
                </a>
            </div>
        </header>

        <!-- Main Content -->
        <main class="crm-content">

            <!-- Thông báo thành công / lỗi -->
            <% if (thongBaoThanhCong != null && !thongBaoThanhCong.isBlank()) { %>
            <div class="alert alert-success">
                <i class="bi bi-check-circle-fill"></i>
                <span><%= thongBaoThanhCong %></span>
            </div>
            <% } %>

            <% if (thongBaoLoi != null && !thongBaoLoi.isBlank()) { %>
            <div class="alert alert-danger">
                <i class="bi bi-exclamation-triangle-fill"></i>
                <span><%= thongBaoLoi %></span>
            </div>
            <% } %>

            <!-- Thanh công cụ kiểm thử phân quyền hiển thị menu (Acceptance Criteria Test Tool) -->
            <div class="role-test-bar">
                <div class="role-test-bar-title">
                    <i class="bi bi-person-gear"></i>
                    <span>Kiểm thử phân quyền menu theo vai trò:</span>
                </div>
                <div class="role-switcher-buttons">
                    <a href="<%= contextPath %>/ho-so?switchRole=SALES_REP"
                       class="btn-role-chip <%= "Nhân viên kinh doanh".equals(dieuHuong.getVaiTroHienThi()) ? "active" : "" %>">
                        Nhân viên KD
                    </a>
                    <a href="<%= contextPath %>/ho-so?switchRole=TEAM_LEAD"
                       class="btn-role-chip <%= "Trưởng nhóm kinh doanh".equals(dieuHuong.getVaiTroHienThi()) ? "active" : "" %>">
                        Trưởng nhóm KD
                    </a>
                    <a href="<%= contextPath %>/ho-so?switchRole=ADMIN"
                       class="btn-role-chip <%= "Quản trị hệ thống".equals(dieuHuong.getVaiTroHienThi()) ? "active" : "" %>">
                        Quản trị (Admin)
                    </a>
                    <a href="<%= contextPath %>/ho-so?switchRole=DIRECTOR"
                       class="btn-role-chip <%= "Giám đốc kinh doanh".equals(dieuHuong.getVaiTroHienThi()) ? "active" : "" %>">
                        Giám đốc KD
                    </a>
                    <a href="<%= contextPath %>/ho-so?switchRole=MARKETING"
                       class="btn-role-chip <%= "Nhân viên Marketing".equals(dieuHuong.getVaiTroHienThi()) ? "active" : "" %>">
                        Marketing
                    </a>
                </div>
            </div>

            <!-- Profile & Avatar Grid -->
            <div class="profile-grid">

                <!-- Column 1: Thẻ Ảnh đại diện & Nhận diện đồng nghiệp (Story S2-03) -->
                <div class="profile-col-left">

                    <div class="crm-card">
                        <div class="card-header">
                            <h2 class="card-title">
                                <i class="bi bi-camera"></i>
                                <span>Ảnh đại diện</span>
                            </h2>
                        </div>
                        <div class="card-body">
                            <div class="avatar-card-content">

                                <!-- Ảnh đại diện cắt vuông chính (400x400 hiển thị 160x160) -->
                                <div class="avatar-large-wrapper">
                                    <img src="<%= avatarUrl %>"
                                         alt="Ảnh đại diện <%= dieuHuong.getHoTen() %>"
                                         class="avatar-large-img"
                                         id="avatarPreviewLarge">
                                </div>

                                <!-- Tên, vai trò và nhóm kinh doanh đang thuộc về (AC 2) -->
                                <div class="avatar-badges">
                                    <h3 class="user-full-name"><%= dieuHuong.getHoTen() %></h3>
                                    <span class="badge-role">
                                        <i class="bi bi-person-badge"></i> <%= dieuHuong.getVaiTroHienThi() %>
                                    </span>
                                    <span class="badge-team">
                                        <i class="bi bi-diagram-3"></i> <%= dieuHuong.getTenNhomKinhDoanh() %>
                                    </span>
                                </div>

                                <!-- Form Tải lên ảnh đại diện mới -->
                                <form id="avatarUploadForm"
                                      action="<%= contextPath %>/avatar"
                                      method="post"
                                      enctype="multipart/form-data"
                                      style="width: 100%;">

                                    <div class="upload-dropzone" id="uploadDropzone">
                                        <input type="file"
                                               name="avatar"
                                               id="avatarInput"
                                               accept=".jpg,.jpeg,.png,image/jpeg,image/png">
                                        <i class="bi bi-cloud-arrow-up upload-icon"></i>
                                        <div class="upload-text">Kéo thả hoặc chọn ảnh</div>
                                        <div class="upload-hint">Chấp nhận JPG, PNG tối đa 2MB</div>
                                    </div>

                                    <div class="selected-file-info" id="selectedFileInfo">
                                        <i class="bi bi-file-earmark-image"></i>
                                        <span id="fileName">chua-chon-file.png</span>
                                        (<span id="fileSize">0 KB</span>)
                                    </div>

                                    <div class="client-error-box" id="clientErrorBox"></div>

                                    <button type="submit" class="btn btn-primary btn-block" id="btnSubmitAvatar" disabled style="margin-top: 12px;">
                                        <i class="bi bi-check2-circle"></i>
                                        <span>Lưu ảnh đại diện</span>
                                    </button>
                                </form>

                                <!-- AC: Bản thu nhỏ để đồng nghiệp nhận ra ai đang phụ trách khi xem hồ sơ -->
                                <div class="thumbnail-preview-box">
                                    <span class="thumbnail-preview-label">
                                        <i class="bi bi-eye"></i> Bản thu nhỏ hiển thị cho đồng nghiệp:
                                    </span>
                                    <div class="colleague-card-demo">
                                        <img src="<%= thumbUrl %>"
                                             alt="Thumbnail"
                                             class="avatar-thumb-img"
                                             id="avatarPreviewThumb">
                                        <div class="colleague-info">
                                            <div class="colleague-name"><%= dieuHuong.getHoTen() %></div>
                                            <div class="colleague-role">Phụ trách: <%= dieuHuong.getTenNhomKinhDoanh() %></div>
                                        </div>
                                    </div>
                                </div>

                            </div>
                        </div>
                    </div>

                </div>

                <!-- Column 2: Chi tiết hồ sơ cá nhân -->
                <div class="profile-col-right">

                    <div class="crm-card">
                        <div class="card-header">
                            <h2 class="card-title">
                                <i class="bi bi-person-lines-fill"></i>
                                <span>Thông tin cá nhân & Phụ trách</span>
                            </h2>
                        </div>
                        <div class="card-body">

                            <form action="<%= contextPath %>/ho-so" method="post">
                                <input type="hidden" name="action" value="capNhatThongTin">

                                <div class="form-group">
                                    <label class="form-label" for="hoTen">Họ và tên *</label>
                                    <input type="text"
                                           class="form-control"
                                           id="hoTen"
                                           name="hoTen"
                                           value="<%= nguoiDung.getHoTen() != null ? nguoiDung.getHoTen() : "" %>"
                                           required>
                                </div>

                                <div class="form-group">
                                    <label class="form-label" for="email">Địa chỉ Email</label>
                                    <input type="email"
                                           class="form-control"
                                           id="email"
                                           value="<%= nguoiDung.getEmail() != null ? nguoiDung.getEmail() : "" %>"
                                           readonly
                                           disabled>
                                    <span class="form-help">Email là định danh tài khoản, không tự ý thay đổi.</span>
                                </div>

                                <div class="form-group">
                                    <label class="form-label">Vai trò được cấp</label>
                                    <input type="text"
                                           class="form-control"
                                           value="<%= dieuHuong.getVaiTroHienThi() %>"
                                           readonly
                                           disabled>
                                    <span class="form-help">Vai trò do Quản trị viên phân bổ, quyết định quyền xem các mục menu.</span>
                                </div>

                                <div class="form-group">
                                    <label class="form-label">Nhóm kinh doanh trực thuộc</label>
                                    <input type="text"
                                           class="form-control"
                                           value="<%= dieuHuong.getTenNhomKinhDoanh() %>"
                                           readonly
                                           disabled>
                                    <span class="form-help">Xác định phạm vi dữ liệu và đối tượng khách hàng phụ trách.</span>
                                </div>

                                <div class="form-group">
                                    <label class="form-label" for="soDienThoai">Số điện thoại liên hệ</label>
                                    <input type="tel"
                                           class="form-control"
                                           id="soDienThoai"
                                           name="soDienThoai"
                                           value="<%= nguoiDung.getSoDienThoai() != null ? nguoiDung.getSoDienThoai() : "" %>"
                                           placeholder="09xx xxx xxx">
                                </div>

                                <div class="form-group">
                                    <label class="form-label" for="chuKyEmail">Chữ ký Email khi gửi báo giá</label>
                                    <textarea class="form-control"
                                              id="chuKyEmail"
                                              name="chuKyEmail"
                                              rows="4"
                                              placeholder="Nhập chữ ký cá nhân..."><%= nguoiDung.getChuKyEmail() != null ? nguoiDung.getChuKyEmail() : "" %></textarea>
                                </div>

                                <div style="display: flex; justify-content: flex-end;">
                                    <button type="submit" class="btn btn-primary">
                                        <i class="bi bi-save"></i>
                                        <span>Cập nhật hồ sơ</span>
                                    </button>
                                </div>
                            </form>

                        </div>
                    </div>

                </div>

            </div>

        </main>
    </div>

</div>

<!-- JavaScript xử lý client -->
<script src="<%= contextPath %>/assets/js/ho-so/avatar.js"></script>
</body>
</html>
