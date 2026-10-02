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
    String avatarUrl = (dieuHuong.getAnhDaiDienUrl() != null && !dieuHuong.getAnhDaiDienUrl().isBlank())
            ? dieuHuong.getAnhDaiDienUrl()
            : contextPath + "/avatar?id=" + nguoiDung.getId();
    String thumbUrl = (dieuHuong.getAnhDaiDienThumbUrl() != null && !dieuHuong.getAnhDaiDienThumbUrl().isBlank())
            ? dieuHuong.getAnhDaiDienThumbUrl()
            : contextPath + "/avatar?id=" + nguoiDung.getId() + "&thumb=true";
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0">
    <title>Hồ sơ cá nhân & Ảnh đại diện | CRM Bán Hàng</title>
    <!-- Google Fonts: Inter -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <!-- Stylesheet Module Hồ sơ & Avatar (Story S2-03) -->
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/ho-so/avatar.css">
</head>
<body>

<div class="crm-layout">

    <!-- Mobile Backdrop for Sidebar -->
    <div class="sidebar-backdrop" id="sidebarBackdrop"></div>

    <!-- Sidebar Navigation: Chỉ hiển thị các mục menu theo vai trò người dùng -->
    <aside class="crm-sidebar" id="crmSidebar" aria-label="Menu điều hướng hệ thống">
        <div class="sidebar-header">
            <a href="<%= contextPath %>/ho-so" class="crm-brand">
                <span class="brand-badge">CRM</span>
                <span>Bán Hàng</span>
            </a>
            <button type="button" class="btn-sidebar-close" id="sidebarCloseBtn" aria-label="Đóng menu">
                <i class="bi bi-x-lg"></i>
            </button>
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
                <button type="button" class="btn-sidebar-toggle" id="sidebarToggleBtn" aria-label="Mở menu điều hướng">
                    <i class="bi bi-list"></i>
                </button>
                <div class="header-title-box">
                    <h1 class="header-title">Hồ sơ cá nhân & Ảnh đại diện</h1>
                    <span class="header-subtitle">Quản lý nhận diện người phụ trách trong quy trình bán hàng</span>
                </div>
            </div>

            <div class="header-right">
                <!-- User Header Pill: Hiển thị tên, vai trò và bản thu nhỏ thumbnail -->
                <a href="<%= contextPath %>/ho-so" class="user-header-pill" title="Xem hồ sơ cá nhân">
                    <div class="user-header-thumb-wrapper">
                        <img src="<%= thumbUrl %>"
                             alt="Avatar <%= dieuHuong.getHoTen() %>"
                             class="user-header-thumb"
                             id="avatarPreviewHeader">
                        <span class="online-indicator-dot" title="Trực tuyến"></span>
                    </div>
                    <div class="user-header-info">
                        <span class="user-header-name"><%= dieuHuong.getHoTen() %></span>
                        <span class="user-header-role"><%= dieuHuong.getVaiTroHienThi() %></span>
                    </div>
                </a>
            </div>
        </header>

        <!-- Main Content -->
        <main class="crm-content">

            <!-- Server Flash Alerts -->
            <% if (thongBaoThanhCong != null && !thongBaoThanhCong.isBlank()) { %>
            <div class="alert alert-success" id="serverSuccessAlert">
                <i class="bi bi-check-circle-fill"></i>
                <div class="alert-content">
                    <strong>Thành công:</strong>
                    <span><%= thongBaoThanhCong %></span>
                </div>
            </div>
            <% } %>

            <% if (thongBaoLoi != null && !thongBaoLoi.isBlank()) { %>
            <div class="alert alert-danger" id="serverErrorAlert">
                <i class="bi bi-exclamation-triangle-fill"></i>
                <div class="alert-content">
                    <strong>Thông báo lỗi:</strong>
                    <span><%= thongBaoLoi %></span>
                </div>
            </div>
            <% } %>

            <!-- Thanh chuyển đổi vai trò phục vụ kiểm thử AC (Role Switcher Toolbar) -->
            <div class="role-test-bar">
                <div class="role-test-bar-title">
                    <i class="bi bi-shield-check"></i>
                    <span>Kiểm thử phân quyền menu & hồ sơ theo vai trò:</span>
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

                <!-- ===============================================================
                     CỘT TRÁI: QUẢN LÝ ẢNH ĐẠI DIỆN & MÔ PHỎNG NHẬN DIỆN ĐỒNG NGHIỆP
                     (Fulfills Acceptance Criteria S2-03)
                     =============================================================== -->
                <div class="profile-col-left">

                    <!-- Thẻ 1: Tải lên & Cắt vuông ảnh đại diện -->
                    <div class="crm-card">
                        <div class="card-header">
                            <h2 class="card-title">
                                <i class="bi bi-camera-fill"></i>
                                <span>Ảnh đại diện</span>
                            </h2>
                            <span class="badge-ac-rule" title="Yêu cầu Story S2-03">
                                <i class="bi bi-check2-circle"></i> JPG/PNG &le; 2MB
                            </span>
                        </div>
                        <div class="card-body">
                            <div class="avatar-card-content">

                                <!-- Khung hiển thị ảnh đại diện chính (cắt vuông 1:1) -->
                                <div class="avatar-large-container">
                                    <div class="avatar-large-wrapper" id="avatarLargeWrapper">
                                        <img src="<%= avatarUrl %>"
                                             alt="Ảnh đại diện <%= dieuHuong.getHoTen() %>"
                                             class="avatar-large-img"
                                             id="avatarPreviewLarge">
                                        <span class="avatar-ratio-badge" title="Tỷ lệ chuẩn vuông 1:1">1:1</span>
                                    </div>
                                    <div class="avatar-quick-actions">
                                        <button type="button" class="btn-avatar-action" id="btnTriggerFileSelect" title="Chọn ảnh từ thiết bị">
                                            <i class="bi bi-cloud-arrow-up"></i> Chọn ảnh
                                        </button>
                                    </div>
                                </div>

                                <!-- Thông tin người phụ trách -->
                                <div class="avatar-badges">
                                    <h3 class="user-full-name" id="userDisplayName"><%= dieuHuong.getHoTen() %></h3>
                                    <div class="badge-row">
                                        <span class="badge-role" title="Vai trò trong hệ thống">
                                            <i class="bi bi-person-badge"></i> <%= dieuHuong.getVaiTroHienThi() %>
                                        </span>
                                        <span class="badge-team" title="Nhóm kinh doanh trực thuộc">
                                            <i class="bi bi-diagram-3"></i> <%= dieuHuong.getTenNhomKinhDoanh() %>
                                        </span>
                                    </div>
                                </div>

                                <!-- Form tải lên ảnh (Multipart form data) -->
                                <form id="avatarUploadForm"
                                      action="<%= contextPath %>/avatar"
                                      method="post"
                                      enctype="multipart/form-data"
                                      class="avatar-form">

                                    <!-- Vùng kéo thả file (Drag and Drop Zone) -->
                                    <div class="upload-dropzone" id="uploadDropzone" tabindex="0" role="button" aria-label="Kéo thả file ảnh hoặc nhấn để chọn">
                                        <input type="file"
                                               name="avatar"
                                               id="avatarInput"
                                               accept=".jpg,.jpeg,.png,image/jpeg,image/png"
                                               aria-label="Chọn file ảnh đại diện">
                                        <div class="dropzone-inner">
                                            <div class="upload-icon-circle">
                                                <i class="bi bi-cloud-arrow-up-fill upload-icon"></i>
                                            </div>
                                            <div class="upload-text">Kéo thả ảnh vào đây</div>
                                            <div class="upload-subtext">hoặc <span class="browse-link">nhấn để duyệt file</span></div>
                                            <div class="upload-hint">
                                                <i class="bi bi-info-circle"></i> Định dạng JPG, PNG • Tối đa 2MB
                                            </div>
                                        </div>
                                    </div>

                                    <!-- Box thông tin file được chọn kèm kiểm tra hợp lệ -->
                                    <div class="selected-file-info" id="selectedFileInfo" style="display: none;">
                                        <div class="file-info-header">
                                            <i class="bi bi-file-earmark-image file-info-icon"></i>
                                            <div class="file-info-text">
                                                <div class="file-name" id="fileName">chua-chon-file.png</div>
                                                <div class="file-meta">
                                                    <span id="fileSize">0 KB</span>
                                                    <span class="badge-valid-status" id="badgeValidStatus">
                                                        <i class="bi bi-check-circle-fill"></i> Hợp lệ (&le; 2MB)
                                                    </span>
                                                </div>
                                            </div>
                                            <button type="button" class="btn-clear-file" id="btnClearFile" title="Hủy chọn file">
                                                <i class="bi bi-x"></i>
                                            </button>
                                        </div>
                                    </div>

                                    <!-- Hộp thông báo lỗi phía Client (Validate AC: JPG/PNG <= 2MB) -->
                                    <div class="client-error-box" id="clientErrorBox" style="display: none;">
                                        <i class="bi bi-exclamation-octagon-fill"></i>
                                        <span id="clientErrorText"></span>
                                    </div>

                                    <!-- Hàng nút hành động: Cắt vuông trực quan & Tải lên -->
                                    <div class="upload-action-buttons">
                                        <button type="button" class="btn btn-primary btn-block" id="btnOpenCropModal" disabled>
                                            <i class="bi bi-crop"></i>
                                            <span>Cắt vuông & Tải lên</span>
                                        </button>
                                        <button type="submit" class="btn btn-secondary btn-block" id="btnSubmitDirect" disabled>
                                            <i class="bi bi-arrow-up-circle"></i>
                                            <span>Tải lên nhanh</span>
                                        </button>
                                    </div>
                                </form>

                            </div>
                        </div>
                    </div>

                    <!-- Thẻ 2: Mô phỏng nhận diện đồng nghiệp (Colleague Recognition Demo) -->
                    <div class="crm-card colleague-recognition-card">
                        <div class="card-header">
                            <h2 class="card-title">
                                <i class="bi bi-people-fill"></i>
                                <span>Nhận diện đồng nghiệp</span>
                            </h2>
                            <span class="badge-feature">Acceptance Criteria</span>
                        </div>
                        <div class="card-body">
                            <p class="colleague-desc">
                                Khi đồng nghiệp xem hồ sơ khách hàng, cơ hội bán hàng hoặc danh sách phân công, bản thu nhỏ (thumbnail) sẽ giúp nhận ra bạn ngay lập tức:
                            </p>

                            <!-- Mô phỏng 1: Khách hàng chi tiết có người phụ trách -->
                            <div class="mockup-customer-card">
                                <div class="mockup-customer-header">
                                    <span class="mockup-label">HỒ SƠ KHÁCH HÀNG</span>
                                    <span class="mockup-status-tag">VIP</span>
                                </div>
                                <div class="mockup-customer-name">Công ty Cổ phần Công nghệ Toàn Cầu</div>
                                <div class="mockup-staff-box">
                                    <div class="mockup-staff-label">Người phụ trách khách hàng:</div>
                                    <div class="mockup-staff-content">
                                        <div class="mockup-thumb-wrap">
                                            <img src="<%= thumbUrl %>"
                                                 alt="Thumbnail"
                                                 class="avatar-thumb-img"
                                                 id="avatarPreviewThumb">
                                            <span class="mockup-thumb-dot"></span>
                                        </div>
                                        <div class="mockup-staff-details">
                                            <div class="mockup-staff-name" id="mockupStaffName"><%= dieuHuong.getHoTen() %></div>
                                            <div class="mockup-staff-role"><%= dieuHuong.getVaiTroHienThi() %> • <%= dieuHuong.getTenNhomKinhDoanh() %></div>
                                        </div>
                                        <span class="badge-assigned">Đang phụ trách</span>
                                    </div>
                                </div>
                            </div>

                            <!-- Mô phỏng 2: Bảng danh sách thành viên nhóm -->
                            <div class="mockup-team-row">
                                <div class="mockup-team-label">Hiển thị trong danh bạ nội bộ:</div>
                                <div class="mockup-team-item">
                                    <img src="<%= thumbUrl %>"
                                         alt="Thumbnail"
                                         class="avatar-thumb-mini"
                                         id="avatarPreviewThumbMini">
                                    <div class="mockup-team-text">
                                        <span class="mockup-team-user"><%= dieuHuong.getHoTen() %></span>
                                        <span class="mockup-team-email"><%= nguoiDung.getEmail() != null ? nguoiDung.getEmail() : "" %></span>
                                    </div>
                                    <span class="mockup-team-badge">Sẵn sàng</span>
                                </div>
                            </div>

                        </div>
                    </div>

                </div>

                <!-- ===============================================================
                     CỘT PHẢI: CHI TIẾT THÔNG TIN HỒ SƠ & CHỮ KÝ BÁN HÀNG
                     =============================================================== -->
                <div class="profile-col-right">

                    <div class="crm-card">
                        <div class="card-header">
                            <h2 class="card-title">
                                <i class="bi bi-person-lines-fill"></i>
                                <span>Thông tin cá nhân & Phụ trách</span>
                            </h2>
                        </div>
                        <div class="card-body">

                            <form action="<%= contextPath %>/ho-so" method="post" id="profileInfoForm">
                                <input type="hidden" name="action" value="capNhatThongTin">

                                <div class="form-group">
                                    <label class="form-label" for="hoTen">
                                        Họ và tên <span class="required-star">*</span>
                                    </label>
                                    <div class="input-icon-wrap">
                                        <i class="bi bi-person input-icon"></i>
                                        <input type="text"
                                               class="form-control"
                                               id="hoTen"
                                               name="hoTen"
                                               value="<%= nguoiDung.getHoTen() != null ? nguoiDung.getHoTen() : "" %>"
                                               placeholder="Nhập họ và tên đầy đủ"
                                               required>
                                    </div>
                                    <span class="form-help">Tên sẽ hiển thị cùng ảnh đại diện khi đồng nghiệp xem hồ sơ khách hàng.</span>
                                </div>

                                <div class="form-row-2col">
                                    <div class="form-group">
                                        <label class="form-label" for="email">Địa chỉ Email</label>
                                        <div class="input-icon-wrap">
                                            <i class="bi bi-envelope input-icon"></i>
                                            <input type="email"
                                                   class="form-control"
                                                   id="email"
                                                   value="<%= nguoiDung.getEmail() != null ? nguoiDung.getEmail() : "" %>"
                                                   readonly
                                                   disabled>
                                        </div>
                                        <span class="form-help">Email là định danh tài khoản, không tự ý thay đổi.</span>
                                    </div>

                                    <div class="form-group">
                                        <label class="form-label" for="soDienThoai">Số điện thoại liên hệ</label>
                                        <div class="input-icon-wrap">
                                            <i class="bi bi-telephone input-icon"></i>
                                            <input type="tel"
                                                   class="form-control"
                                                   id="soDienThoai"
                                                   name="soDienThoai"
                                                   value="<%= nguoiDung.getSoDienThoai() != null ? nguoiDung.getSoDienThoai() : "" %>"
                                                   placeholder="09xx xxx xxx">
                                        </div>
                                        <span class="form-help">Đồng nghiệp có thể liên hệ nhanh khi bàn giao khách hàng.</span>
                                    </div>
                                </div>

                                <div class="form-row-2col">
                                    <div class="form-group">
                                        <label class="form-label">Vai trò được cấp</label>
                                        <div class="input-icon-wrap">
                                            <i class="bi bi-shield-lock input-icon"></i>
                                            <input type="text"
                                                   class="form-control"
                                                   value="<%= dieuHuong.getVaiTroHienThi() %>"
                                                   readonly
                                                   disabled>
                                        </div>
                                        <span class="form-help">Quyết định các mục menu và quyền hạn trên hệ thống CRM.</span>
                                    </div>

                                    <div class="form-group">
                                        <label class="form-label">Nhóm kinh doanh trực thuộc</label>
                                        <div class="input-icon-wrap">
                                            <i class="bi bi-diagram-3 input-icon"></i>
                                            <input type="text"
                                                   class="form-control"
                                                   value="<%= dieuHuong.getTenNhomKinhDoanh() %>"
                                                   readonly
                                                   disabled>
                                        </div>
                                        <span class="form-help">Xác định phạm vi dữ liệu khách hàng được phụ trách.</span>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="form-label" for="chuKyEmail">
                                        Chữ ký Email khi gửi báo giá & hợp đồng
                                    </label>
                                    <textarea class="form-control"
                                              id="chuKyEmail"
                                              name="chuKyEmail"
                                              rows="4"
                                              placeholder="Trân trọng,&#10;Bàn Thị Linh - Chuyên viên Kinh doanh CRM&#10;SĐT: 0988 123 456"><%= nguoiDung.getChuKyEmail() != null ? nguoiDung.getChuKyEmail() : "" %></textarea>
                                    <span class="form-help">Chữ ký sẽ tự động chèn vào email gửi báo giá tới khách hàng.</span>
                                </div>

                                <div class="form-actions-right">
                                    <button type="submit" class="btn btn-primary" id="btnUpdateProfile">
                                        <i class="bi bi-floppy"></i>
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

<!-- =======================================================================
     MODAL CẮT ẢNH VUÔNG (1:1) VÀ TẠO BẢN THU NHỎ THUMBNAIL
     (Acceptance Criteria: Ảnh được cắt vuông và tạo bản thu nhỏ)
     ======================================================================= -->
<div class="crop-modal-backdrop" id="cropModalBackdrop" style="display: none;"></div>
<div class="crop-modal" id="cropModal" role="dialog" aria-modal="true" aria-labelledby="cropModalTitle" style="display: none;">
    <div class="crop-modal-content">

        <!-- Modal Header -->
        <div class="crop-modal-header">
            <div class="crop-modal-title-wrap">
                <i class="bi bi-crop crop-modal-icon"></i>
                <div>
                    <h3 class="crop-modal-title" id="cropModalTitle">Cắt ảnh vuông & Tạo thumbnail</h3>
                    <div class="crop-modal-subtitle">Điều chỉnh khung hình vuông (1:1) để nhận diện rõ nét nhất</div>
                </div>
            </div>
            <button type="button" class="crop-modal-close" id="btnCloseCropModal" aria-label="Đóng cửa sổ">
                <i class="bi bi-x-lg"></i>
            </button>
        </div>

        <!-- Modal Body: Workspace + Dual Previews -->
        <div class="crop-modal-body">

            <!-- Workspace: Canvas tương tác di chuyển, phóng to, xoay -->
            <div class="crop-workspace-container">
                <div class="crop-canvas-wrapper" id="cropCanvasWrapper">
                    <canvas id="cropCanvas" width="360" height="360"></canvas>
                    <div class="crop-overlay-guide">
                        <div class="crop-rule-of-thirds">
                            <span class="grid-line grid-v1"></span>
                            <span class="grid-line grid-v2"></span>
                            <span class="grid-line grid-h1"></span>
                            <span class="grid-line grid-h2"></span>
                        </div>
                    </div>
                </div>

                <!-- Thanh công cụ điều khiển tương tác (Zoom, Rotate, Reset) -->
                <div class="crop-toolbar">
                    <button type="button" class="btn-tool" id="btnZoomOut" title="Thu nhỏ (-)">
                        <i class="bi bi-dash-lg"></i>
                    </button>
                    <div class="zoom-slider-wrap">
                        <i class="bi bi-zoom-in slider-icon"></i>
                        <input type="range"
                               id="zoomSlider"
                               min="0.5"
                               max="3.0"
                               step="0.05"
                               value="1.0"
                               aria-label="Thanh trượt thu phóng ảnh">
                    </div>
                    <button type="button" class="btn-tool" id="btnZoomIn" title="Phóng to (+)">
                        <i class="bi bi-plus-lg"></i>
                    </button>
                    <div class="tool-divider"></div>
                    <button type="button" class="btn-tool" id="btnRotateLeft" title="Xoay trái 90°">
                        <i class="bi bi-arrow-counterclockwise"></i>
                    </button>
                    <button type="button" class="btn-tool" id="btnRotateRight" title="Xoay phải 90°">
                        <i class="bi bi-arrow-clockwise"></i>
                    </button>
                    <button type="button" class="btn-tool" id="btnResetCrop" title="Đặt lại vị trí ban đầu">
                        <i class="bi bi-aspect-ratio"></i> Đặt lại
                    </button>
                </div>
                <div class="crop-drag-hint">
                    <i class="bi bi-arrows-move"></i> Kéo giữ chuột hoặc chạm vuốt trên điện thoại để điều chỉnh vị trí ảnh
                </div>
            </div>

            <!-- Preview Sidebar: Xem trước đồng thời Ảnh vuông chuẩn & Thumbnail -->
            <div class="crop-preview-sidebar">
                <div class="preview-group">
                    <div class="preview-group-title">
                        <i class="bi bi-aspect-ratio"></i> Ảnh vuông chuẩn (1:1 - 400x400)
                    </div>
                    <div class="preview-circle-large-wrap">
                        <canvas id="previewSquareCanvas" width="140" height="140" class="preview-canvas-round"></canvas>
                        <canvas id="previewSquareRectCanvas" width="80" height="80" class="preview-canvas-rect"></canvas>
                    </div>
                    <div class="preview-note">Hiển thị trong trang Hồ sơ cá nhân</div>
                </div>

                <div class="preview-group">
                    <div class="preview-group-title">
                        <i class="bi bi-eye"></i> Bản thu nhỏ (Thumbnail - 120x120)
                    </div>
                    <div class="preview-thumb-demo-wrap">
                        <canvas id="previewThumbCanvas" width="48" height="48" class="preview-canvas-thumb"></canvas>
                        <div class="thumb-demo-meta">
                            <span class="thumb-demo-name"><%= dieuHuong.getHoTen() %></span>
                            <span class="thumb-demo-tag">Nhận diện người phụ trách</span>
                        </div>
                    </div>
                    <div class="preview-note">Hiển thị ở Header, Danh bạ & Hồ sơ khách hàng</div>
                </div>

                <div class="crop-meta-box">
                    <div class="meta-row">
                        <span>Tỷ lệ cắt:</span>
                        <strong class="text-success">1 : 1 (Vuông chuẩn)</strong>
                    </div>
                    <div class="meta-row">
                        <span>Định dạng xuất:</span>
                        <strong id="cropFormatLabel">JPG</strong>
                    </div>
                    <div class="meta-row">
                        <span>Dung lượng sau cắt:</span>
                        <strong id="cropEstimatedSize">&le; 200 KB (&le; 2MB)</strong>
                    </div>
                </div>
            </div>

        </div>

        <!-- Modal Footer -->
        <div class="crop-modal-footer">
            <button type="button" class="btn btn-light" id="btnCancelCrop">
                Hủy bỏ
            </button>
            <button type="button" class="btn btn-primary" id="btnConfirmCropAndUpload">
                <i class="bi bi-check2-circle"></i>
                <span id="btnConfirmText">Cắt & Lưu ảnh đại diện</span>
            </button>
        </div>

    </div>
</div>

<!-- Toast Container cho các thông báo tương tác nhanh -->
<div class="crm-toast-container" id="crmToastContainer" aria-live="polite"></div>

<!-- JavaScript xử lý client (Story S2-03) -->
<script src="<%= contextPath %>/assets/js/ho-so/avatar.js"></script>
</body>
</html>
