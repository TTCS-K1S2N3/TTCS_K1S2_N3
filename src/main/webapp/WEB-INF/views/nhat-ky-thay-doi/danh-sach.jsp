<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="vn.nhom10.crm.model.LoaiDoiTuongNhayCam" %>
<%@ page import="vn.nhom10.crm.model.HanhDongThayDoi" %>
<%@ page import="vn.nhom10.crm.dto.NhatKyThayDoiDTO" %>
<%@ page import="vn.nhom10.crm.dto.BoLocNhatKyDTO" %>
<%@ page import="vn.nhom10.crm.dto.KetQuaPhanTrangDTO" %>
<%@ page import="vn.nhom10.crm.dto.ThongKeNhatKyDTO" %>
<%@ page import="vn.nhom10.crm.dto.NguoiDungOptionDTO" %>
<%@ page import="java.util.List" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nhật Ký Thay Đổi Dữ Liệu Nhạy Cảm - CRM Quản Trị Hệ Thống (S2-04)</title>
    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/nhat-ky-thay-doi/nhat-ky-thay-doi.css">
</head>
<body class="crm-body">

    <!-- Thanh điều hướng và Sidebar chuẩn hệ thống S1-06 -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content" id="crm-main-content">

<%
    BoLocNhatKyDTO boLoc = (BoLocNhatKyDTO) request.getAttribute("boLoc");
    if (boLoc == null) boLoc = new BoLocNhatKyDTO();

    KetQuaPhanTrangDTO<NhatKyThayDoiDTO> phanTrang = (KetQuaPhanTrangDTO<NhatKyThayDoiDTO>) request.getAttribute("phanTrang");
    List<NhatKyThayDoiDTO> danhSachNhatKy = (List<NhatKyThayDoiDTO>) request.getAttribute("danhSachNhatKy");
    ThongKeNhatKyDTO thongKe = (ThongKeNhatKyDTO) request.getAttribute("thongKe");
    if (thongKe == null) thongKe = new ThongKeNhatKyDTO();

    List<NguoiDungOptionDTO> danhSachNguoiDung = (List<NguoiDungOptionDTO>) request.getAttribute("danhSachNguoiDung");
    LoaiDoiTuongNhayCam[] danhSachLoai = (LoaiDoiTuongNhayCam[]) request.getAttribute("danhSachLoaiDoiTuong");

    String thongBaoLoi = (String) request.getAttribute("thongBaoLoi");
    String thongBaoThanhCong = (String) request.getAttribute("thongBaoThanhCong");

    String contextPath = request.getContextPath();
%>

<div class="crm-app-container">

    <!-- 1. HEADER CHÍNH -->
    <header class="crm-page-header">
        <div class="crm-header-content">
            <div class="crm-breadcrumb">
                <a href="<%= contextPath %>/dieu-huong" class="breadcrumb-item">Trang chủ</a>
                <span class="breadcrumb-separator">/</span>
                <a href="<%= contextPath %>/nguoi-dung" class="breadcrumb-item">Người dùng & Hệ thống</a>
                <span class="breadcrumb-separator">/</span>
                <span class="breadcrumb-item active">Nhật ký dữ liệu nhạy cảm</span>
            </div>

            <div class="crm-header-main">
                <div class="crm-title-area">
                    <div class="crm-icon-badge">
                        <span class="material-symbols-outlined" style="font-size: 28px;" aria-hidden="true">verified_user</span>
                    </div>
                    <div>
                        <h1 class="crm-page-title">Nhật Ký Thay Đổi Trên Dữ Liệu Nhạy Cảm</h1>
                        <p class="crm-page-subtitle">
                            Story S2-04: Giám sát toàn diện thay đổi chiết khấu, chỉ tiêu kinh doanh, quyền sở hữu dữ liệu và vai trò người dùng phục vụ đối soát số liệu cuối quý.
                        </p>
                    </div>
                </div>

                <div style="display: flex; align-items: center; gap: 12px; flex-wrap: wrap;">
                    <a href="<%= contextPath %>/nguoi-dung" class="crm-btn-back" id="btn-back-to-users" title="Quay lại Quản lý tài khoản" style="display: inline-flex; align-items: center; gap: 6px; padding: 8px 14px; border-radius: 6px; border: 1px solid #cbd5e1; background: #fff; color: #334155; font-size: 13.5px; font-weight: 600; text-decoration: none;">
                        <span class="material-symbols-outlined icon-sm" aria-hidden="true">arrow_back</span> Quản lý người dùng
                    </a>
                <%
                    vn.nhom10.crm.model.NguoiDung ndHienTai = (session != null) ? (vn.nhom10.crm.model.NguoiDung) session.getAttribute("nguoiDung") : null;
                    String tenAdmin = (ndHienTai != null && ndHienTai.getHoTen() != null) ? ndHienTai.getHoTen() : "Quản trị hệ thống";
                    String vaiTroAdmin = (ndHienTai != null && ndHienTai.getChuoiVaiTroHienThi() != null) ? ndHienTai.getChuoiVaiTroHienThi() : "Quản trị hệ thống (System Admin)";
                    String avatarAdmin = (ndHienTai != null && ndHienTai.getHoTen() != null && ndHienTai.getHoTen().trim().length() > 0)
                        ? ndHienTai.getHoTen().trim().substring(0, Math.min(2, ndHienTai.getHoTen().trim().length())).toUpperCase()
                        : "AD";
                %>
                <div class="crm-user-badge-group">
                    <div class="crm-user-profile-badge">
                        <div class="user-avatar-circle"><%= avatarAdmin %></div>
                        <div class="user-meta">
                            <span class="user-name"><%= tenAdmin %></span>
                            <span class="user-role"><%= vaiTroAdmin %></span>
                        </div>
                    </div>
                    <span class="crm-live-badge" title="Hệ thống tự động ghi nhật ký bất biến theo chuẩn kiểm toán">
                        <span class="pulse-dot"></span> LIVE AUDIT
                    </span>
                </div>
                </div>
            </div>
        </div>
    </header>

    <!-- THÔNG BÁO HỆ THỐNG -->
    <% if (thongBaoLoi != null && !thongBaoLoi.trim().isEmpty()) { %>
        <div class="crm-alert crm-alert-danger" id="alert-message-error" role="alert">
            <div class="alert-icon"><span class="material-symbols-outlined" aria-hidden="true">warning</span></div>
            <div class="alert-text"><%= thongBaoLoi %></div>
            <button type="button" class="alert-close-btn" onclick="this.parentElement.remove()" aria-label="Đóng"><span class="material-symbols-outlined icon-sm" aria-hidden="true">close</span></button>
        </div>
    <% } %>

    <% if (thongBaoThanhCong != null && !thongBaoThanhCong.trim().isEmpty()) { %>
        <div class="crm-alert crm-alert-success" id="alert-message-success" role="alert">
            <div class="alert-icon"><span class="material-symbols-outlined" aria-hidden="true">check_circle</span></div>
            <div class="alert-text"><%= thongBaoThanhCong %></div>
            <button type="button" class="alert-close-btn" onclick="this.parentElement.remove()" aria-label="Đóng"><span class="material-symbols-outlined icon-sm" aria-hidden="true">close</span></button>
        </div>
    <% } %>

    <!-- 2. THỐNG KÊ NHANH (METRICS ROW) -->
    <section class="crm-metrics-grid" aria-label="Thống kê tổng hợp dữ liệu nhạy cảm">
        <a href="<%= contextPath %>/nhat-ky-thay-doi" class="metric-card metric-card-total <%= (boLoc.getLoaiDoiTuong() == null || boLoc.getLoaiDoiTuong().isEmpty()) ? "metric-active" : "" %>" id="metric-card-total">
            <div class="metric-info">
                <span class="metric-label">Tổng thay đổi nhạy cảm</span>
                <span class="metric-number"><%= thongKe.getTongSoBanGhi() %></span>
                <span class="metric-subtext">Toàn bộ bản ghi lưu trữ</span>
            </div>
            <div class="metric-icon-wrap icon-slate">
                <span class="material-symbols-outlined" style="font-size: 24px;" aria-hidden="true">history</span>
            </div>
        </a>

        <a href="<%= contextPath %>/nhat-ky-thay-doi?loaiDoiTuong=CHIET_KHAU" class="metric-card metric-card-discount <%= "CHIET_KHAU".equals(boLoc.getLoaiDoiTuong()) ? "metric-active" : "" %>" id="metric-card-discount">
            <div class="metric-info">
                <span class="metric-label">Sửa chiết khấu</span>
                <span class="metric-number"><%= thongKe.getSoThayDoiChietKhau() %></span>
                <span class="metric-subtext">Báo giá & hợp đồng bán hàng</span>
            </div>
            <div class="metric-icon-wrap icon-amber">
                <span class="material-symbols-outlined" style="font-size: 24px;" aria-hidden="true">percent</span>
            </div>
        </a>

        <a href="<%= contextPath %>/nhat-ky-thay-doi?loaiDoiTuong=CHI_TIEU" class="metric-card metric-card-target <%= "CHI_TIEU".equals(boLoc.getLoaiDoiTuong()) ? "metric-active" : "" %>" id="metric-card-target">
            <div class="metric-info">
                <span class="metric-label">Sửa chỉ tiêu doanh số</span>
                <span class="metric-number"><%= thongKe.getSoThayDoiChiTieu() %></span>
                <span class="metric-subtext">KPI nhóm, hạn ngạch cuối quý</span>
            </div>
            <div class="metric-icon-wrap icon-purple">
                <span class="material-symbols-outlined" style="font-size: 24px;" aria-hidden="true">track_changes</span>
            </div>
        </a>

        <a href="<%= contextPath %>/nhat-ky-thay-doi?loaiDoiTuong=QUYEN_SO_HUU" class="metric-card metric-card-ownership <%= "QUYEN_SO_HUU".equals(boLoc.getLoaiDoiTuong()) ? "metric-active" : "" %>" id="metric-card-ownership">
            <div class="metric-info">
                <span class="metric-label">Đổi quyền sở hữu</span>
                <span class="metric-number"><%= thongKe.getSoThayDoiQuyenSoHuu() %></span>
                <span class="metric-subtext">Khách hàng, cơ hội & lead</span>
            </div>
            <div class="metric-icon-wrap icon-emerald">
                <span class="material-symbols-outlined" style="font-size: 24px;" aria-hidden="true">person_check</span>
            </div>
        </a>

        <a href="<%= contextPath %>/nhat-ky-thay-doi?loaiDoiTuong=VAI_TRO_NGUOI_DUNG" class="metric-card metric-card-role <%= "VAI_TRO_NGUOI_DUNG".equals(boLoc.getLoaiDoiTuong()) ? "metric-active" : "" %>" id="metric-card-role">
            <div class="metric-info">
                <span class="metric-label">Đổi vai trò người dùng</span>
                <span class="metric-number"><%= thongKe.getSoThayDoiVaiTro() %></span>
                <span class="metric-subtext">Phân quyền & chức năng tài khoản</span>
            </div>
            <div class="metric-icon-wrap icon-blue">
                <span class="material-symbols-outlined" style="font-size: 24px;" aria-hidden="true">admin_panel_settings</span>
            </div>
        </a>
    </section>

    <!-- 3. KHU VỰC BỘ LỌC ĐA TIÊU CHÍ (AC 3) -->
    <section class="crm-filter-panel" aria-label="Bộ lọc tìm kiếm nhật ký">
        <form action="<%= contextPath %>/nhat-ky-thay-doi" method="GET" class="crm-filter-form" id="crm-filter-form">

            <div class="filter-top-bar">
                <div class="filter-title-group">
                    <span class="material-symbols-outlined filter-icon" aria-hidden="true">filter_alt</span>
                    <span class="filter-heading">Bộ Lọc Truy Vết Thay Đổi Dữ Liệu</span>
                    <% if (boLoc.coBoLoc()) { %>
                        <span class="filter-applied-badge">Đang kích hoạt bộ lọc</span>
                    <% } %>
                </div>

                <!-- Các nút mốc thời gian nhanh (Quick Presets) -->
                <div class="quick-period-group" role="group" aria-label="Mốc thời gian nhanh">
                    <span class="quick-label">Mốc nhanh:</span>
                    <button type="button" class="quick-btn <%= (boLoc.getQuickPeriod() == null || "all".equals(boLoc.getQuickPeriod())) ? "active" : "" %>" data-period="all">Tất cả</button>
                    <button type="button" class="quick-btn <%= "today".equals(boLoc.getQuickPeriod()) ? "active" : "" %>" data-period="today">Hôm nay</button>
                    <button type="button" class="quick-btn <%= "week".equals(boLoc.getQuickPeriod()) ? "active" : "" %>" data-period="week">7 ngày</button>
                    <button type="button" class="quick-btn <%= "month".equals(boLoc.getQuickPeriod()) ? "active" : "" %>" data-period="month">Tháng 9</button>
                    <button type="button" class="quick-btn <%= "quarter".equals(boLoc.getQuickPeriod()) ? "active" : "" %>" data-period="quarter" title="Đối soát Quý 3 (Thời điểm số liệu cuối quý lệch)"><span class="material-symbols-outlined icon-xs" style="vertical-align: -2px;" aria-hidden="true">track_changes</span> Quý 3 chốt số</button>
                    <input type="hidden" name="quickPeriod" id="quickPeriod" value="<%= boLoc.getQuickPeriod() != null ? boLoc.getQuickPeriod() : "" %>">
                </div>
            </div>

            <div class="filter-fields-grid">

                <!-- 1. Lọc theo Người dùng thực hiện (AC 3) -->
                <div class="form-group">
                    <label for="filter-nguoi-dung" class="form-label">
                        <span class="material-symbols-outlined label-icon" aria-hidden="true">person</span> Người thực hiện
                    </label>
                    <select name="nguoiDungId" id="filter-nguoi-dung" class="form-select">
                        <option value="">-- Tất cả người dùng --</option>
                        <%
                            if (danhSachNguoiDung != null) {
                                for (NguoiDungOptionDTO nd : danhSachNguoiDung) {
                                    boolean selected = boLoc.getNguoiDungId() != null && boLoc.getNguoiDungId().equals(nd.getId());
                        %>
                            <option value="<%= nd.getId() %>" <%= selected ? "selected" : "" %>>
                                <%= nd.getTenHienThiDropdown() %>
                            </option>
                        <%
                                }
                            }
                        %>
                    </select>
                </div>

                <!-- 2. Lọc theo Loại đối tượng nhạy cảm (AC 3) -->
                <div class="form-group">
                    <label for="filter-loai-doi-tuong" class="form-label">
                        <span class="material-symbols-outlined label-icon" aria-hidden="true">label</span> Loại đối tượng nhạy cảm
                    </label>
                    <select name="loaiDoiTuong" id="filter-loai-doi-tuong" class="form-select">
                        <option value="">-- Tất cả loại đối tượng --</option>
                        <%
                            if (danhSachLoai != null) {
                                for (LoaiDoiTuongNhayCam loai : danhSachLoai) {
                                    boolean selected = loai.getMa().equalsIgnoreCase(boLoc.getLoaiDoiTuong());
                        %>
                            <option value="<%= loai.getMa() %>" <%= selected ? "selected" : "" %>>
                                <%= loai.getTenHienThi() %>
                            </option>
                        <%
                                }
                            }
                        %>
                    </select>
                </div>

                <!-- 3. Lọc theo Khoảng thời gian: Từ ngày (AC 3) -->
                <div class="form-group">
                    <label for="filter-tu-ngay" class="form-label">
                        <span class="material-symbols-outlined label-icon" aria-hidden="true">calendar_today</span> Từ ngày
                    </label>
                    <input type="date" name="tuNgay" id="filter-tu-ngay" class="form-input"
                           value="<%= boLoc.getTuNgayChuoi() %>" placeholder="YYYY-MM-DD">
                </div>

                <!-- 4. Lọc theo Khoảng thời gian: Đến ngày (AC 3) -->
                <div class="form-group">
                    <label for="filter-den-ngay" class="form-label">
                        <span class="material-symbols-outlined label-icon" aria-hidden="true">event</span> Đến ngày
                    </label>
                    <input type="date" name="denNgay" id="filter-den-ngay" class="form-input"
                           value="<%= boLoc.getDenNgayChuoi() %>" placeholder="YYYY-MM-DD">
                </div>

                <!-- 5. Tìm kiếm từ khóa tự do -->
                <div class="form-group form-group-search">
                    <label for="filter-tu-khoa" class="form-label">
                        <span class="material-symbols-outlined label-icon" aria-hidden="true">search</span> Từ khóa tìm nhanh
                    </label>
                    <div class="search-input-wrap">
                        <input type="text" name="tuKhoa" id="filter-tu-khoa" class="form-input"
                                placeholder="Tìm theo mã log, tên đối tượng, mã đối tượng, lý do..."
                                value="<%= boLoc.getTuKhoa() != null ? boLoc.getTuKhoa() : "" %>">
                        <% if (boLoc.getTuKhoa() != null && !boLoc.getTuKhoa().isEmpty()) { %>
                            <button type="button" class="search-clear-btn" id="btn-clear-keyword" title="Xóa từ khóa" aria-label="Xóa từ khóa"><span class="material-symbols-outlined" aria-hidden="true">close</span></button>
                        <% } %>
                    </div>
                </div>

            </div>

            <!-- Giữ nguyên trang và số lượng hiển thị -->
            <input type="hidden" name="trang" value="1">
            <input type="hidden" name="soBanGhi" value="<%= boLoc.getSoBanGhiTrenTrang() %>">

            <div class="filter-actions-bar">
                <div class="filter-actions-left">
                    <button type="submit" class="btn btn-primary" id="btn-submit-filter">
                        <span class="material-symbols-outlined icon-sm" aria-hidden="true">filter_alt</span>
                        Áp Dụng Bộ Lọc
                    </button>
                    <a href="<%= contextPath %>/nhat-ky-thay-doi" class="btn btn-outline" id="btn-reset-filter" title="Khôi phục mặc định">
                        <span class="material-symbols-outlined icon-sm" aria-hidden="true">restart_alt</span>
                        Đặt Lại
                    </a>
                </div>

                <div class="filter-actions-right">
                    <button type="button" class="btn btn-ghost" id="btn-print-report" onclick="window.print()" title="In trang báo cáo kiểm toán">
                        <span class="material-symbols-outlined icon-sm" aria-hidden="true">print</span> In Báo Cáo
                    </button>
                    <button type="button" class="btn btn-ghost" id="btn-export-excel" title="Xuất dữ liệu đối soát phục vụ kiểm toán cuối quý">
                        <span class="material-symbols-outlined icon-sm" aria-hidden="true">download</span> Xuất Dữ Liệu
                    </button>
                </div>
            </div>

        </form>
    </section>

    <!-- 4. BẢNG DỮ LIỆU NHẬT KÝ THAY ĐỔI (AC 1 & AC 2) -->
    <section class="crm-table-card" aria-label="Bảng nhật ký thay đổi">

        <div class="table-card-header">
            <div class="table-title-group">
                <h2 class="table-title">Danh Sách Nhật Ký Truy Vết</h2>
                <span class="table-count-badge">
                    <%= (phanTrang != null) ? phanTrang.getTongSoBanGhi() : 0 %> bản ghi phù hợp
                </span>
            </div>

            <!-- Điều chỉnh số lượng bản ghi hiển thị trên trang -->
            <div class="page-size-selector">
                <label for="page-size-select">Hiển thị:</label>
                <select id="page-size-select" class="form-select form-select-sm" onchange="doiSoBanGhiTrenTrang(this.value)">
                    <option value="10" <%= boLoc.getSoBanGhiTrenTrang() == 10 ? "selected" : "" %>>10 dòng</option>
                    <option value="20" <%= boLoc.getSoBanGhiTrenTrang() == 20 ? "selected" : "" %>>20 dòng</option>
                    <option value="50" <%= boLoc.getSoBanGhiTrenTrang() == 50 ? "selected" : "" %>>50 dòng</option>
                </select>
            </div>
        </div>

        <% if (danhSachNhatKy == null || danhSachNhatKy.isEmpty()) { %>
            <!-- TRẠNG THÁI RỖNG (EMPTY STATE) -->
            <div class="crm-empty-state" id="crm-empty-state">
                <div class="empty-icon-box"><span class="material-symbols-outlined" style="font-size: 40px; color: var(--crm-text-muted);" aria-hidden="true">assignment</span></div>
                <h3 class="empty-title">Không Tìm Thấy Bản Ghi Nhật Ký Nào</h3>
                <p class="empty-description">
                    Không có thay đổi dữ liệu nhạy cảm nào khớp với các tiêu chí lọc hiện tại của bạn.
                    Hãy thử nới rộng khoảng thời gian hoặc chọn người dùng khác để đối soát.
                </p>
                <div class="empty-action">
                    <a href="<%= contextPath %>/nhat-ky-thay-doi" class="btn btn-primary">Xóa Toàn Bộ Bộ Lọc</a>
                </div>
            </div>
        <% } else { %>
            <!-- BẢNG HIỂN THỊ DỮ LIỆU ĐẦY ĐỦ -->
            <div class="crm-table-responsive">
                <table class="crm-data-table" id="crm-audit-table">
                    <thead>
                        <tr>
                            <th scope="col" class="col-log-id">Mã Truy Vết & Thời Điểm</th>
                            <th scope="col" class="col-actor">Người Thực Hiện</th>
                            <th scope="col" class="col-target">Đối Tượng & Loại Dữ Liệu</th>
                            <th scope="col" class="col-field">Trường Thay Đổi & Hành Động</th>
                            <th scope="col" class="col-before">Giá Trị Trước (Old)</th>
                            <th scope="col" class="col-after">Giá Trị Sau (New)</th>
                            <th scope="col" class="col-actions text-center">Thao Tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            for (NhatKyThayDoiDTO item : danhSachNhatKy) {
                                LoaiDoiTuongNhayCam loai = item.getLoaiDoiTuong();
                                String loaiClass = loai != null ? loai.getClassMauSac() : "badge-slate";
                                String loaiTen = loai != null ? loai.getTenHienThi() : "Chưa rõ";
                                HanhDongThayDoi hd = item.getHanhDong();
                                String hdClass = hd != null ? hd.getClassMauSac() : "badge-slate";
                                String hdTen = hd != null ? hd.getTenHienThi() : "Cập nhật";
                        %>
                            <tr class="table-row-item" data-log-id="<%= item.getId() %>">

                                <!-- Cột 1: Mã truy vết & Thời điểm -->
                                <td class="col-log-id">
                                    <div class="log-code-wrap">
                                        <span class="log-code-badge" title="Mã định danh truy vết bất biến"><%= item.getMaTruyVet() %></span>
                                        <button type="button" class="btn-copy-code" onclick="saoChepMa('<%= item.getMaTruyVet() %>', this)" title="Sao chép mã" aria-label="Sao chép mã">
                                            <span class="material-symbols-outlined icon-sm" aria-hidden="true">content_copy</span>
                                        </button>
                                    </div>
                                    <div class="log-time-meta">
                                        <span class="log-date"><span class="material-symbols-outlined icon-xs" style="vertical-align: -2px;" aria-hidden="true">calendar_today</span> <%= item.getNgayDinhDang() %></span>
                                        <span class="log-time"><span class="material-symbols-outlined icon-xs" style="vertical-align: -2px;" aria-hidden="true">schedule</span> <%= item.getGioDinhDang() %></span>
                                    </div>
                                </td>

                                <!-- Cột 2: Người thực hiện (AC 2) -->
                                <td class="col-actor">
                                    <div class="actor-card">
                                        <div class="actor-avatar" title="<%= item.getTenNguoiThucHien() %>">
                                            <%= item.getChuCaiDau() %>
                                        </div>
                                        <div class="actor-details">
                                            <span class="actor-name"><%= item.getTenNguoiThucHien() %></span>
                                            <span class="actor-email"><%= item.getEmailNguoiThucHien() %></span>
                                            <%
                                                String vtNguoiThucHien = item.getVaiTroNguoiThucHienHienThi();
                                                if (vtNguoiThucHien == null || vtNguoiThucHien.isBlank() || "null".equalsIgnoreCase(vtNguoiThucHien.trim())) {
                                                    vtNguoiThucHien = "Không xác định";
                                                }
                                            %>
                                            <span class="actor-role-badge"><%= vtNguoiThucHien %></span>
                                        </div>
                                    </div>
                                </td>

                                <!-- Cột 3: Loại đối tượng & Tên đối tượng (AC 1 & AC 2) -->
                                <td class="col-target">
                                    <div class="target-type-badge <%= loaiClass %>">
                                        <% if (loai == LoaiDoiTuongNhayCam.CHIET_KHAU) { %> % <% } %>
                                        <% if (loai == LoaiDoiTuongNhayCam.CHI_TIEU) { %> <span class="material-symbols-outlined icon-xs" style="vertical-align: -2px;" aria-hidden="true">track_changes</span> <% } %>
                                        <% if (loai == LoaiDoiTuongNhayCam.QUYEN_SO_HUU) { %> <span class="material-symbols-outlined icon-xs" style="vertical-align: -2px;" aria-hidden="true">person</span> <% } %>
                                        <% if (loai == LoaiDoiTuongNhayCam.VAI_TRO_NGUOI_DUNG) { %> <span class="material-symbols-outlined icon-xs" style="vertical-align: -2px;" aria-hidden="true">shield</span> <% } %>
                                        <%= loaiTen %>
                                    </div>
                                    <div class="target-name" title="<%= item.getTenDoiTuong() != null ? item.getTenDoiTuong() : "" %>">
                                        <%= item.getTenDoiTuong() != null ? item.getTenDoiTuong() : "-" %>
                                    </div>
                                    <div class="target-code">
                                        <% if (loai == LoaiDoiTuongNhayCam.VAI_TRO_NGUOI_DUNG) { %>
                                            Email / Mã: <code><%= item.getMaDoiTuong() != null ? item.getMaDoiTuong() : "-" %></code>
                                        <% } else { %>
                                            Mã đối tượng: <code><%= item.getMaDoiTuong() != null ? item.getMaDoiTuong() : "-" %></code>
                                        <% } %>
                                    </div>
                                </td>

                                <!-- Cột 4: Trường thay đổi & Hành động -->
                                <td class="col-field">
                                    <div class="field-name-highlight">
                                        <%= item.getTruongThayDoiHienThi() %>
                                    </div>
                                    <span class="action-tag <%= hdClass %>"><%= hdTen %></span>
                                </td>

                                <!-- Cột 5: Giá trị trước khi sửa (AC 2) -->
                                <td class="col-before">
                                    <div class="diff-value-card diff-before">
                                        <div class="diff-badge-sub">Giá trị cũ</div>
                                        <div class="diff-text"><del><%= item.getGiaTriTruocHienThi() %></del></div>
                                    </div>
                                </td>

                                <!-- Cột 6: Giá trị sau khi sửa (AC 2) -->
                                <td class="col-after">
                                    <div class="diff-value-card diff-after">
                                        <div class="diff-badge-sub">Giá trị mới</div>
                                        <div class="diff-text"><strong><%= item.getGiaTriSauHienThi() %></strong></div>
                                    </div>
                                </td>

                                <!-- Cột 7: Nút Xem chi tiết -->
                                <td class="col-actions text-center">
                                    <button type="button" class="btn btn-detail"
                                            onclick="moModalChiTiet(<%= item.getId() %>)"
                                            id="btn-detail-<%= item.getId() %>"
                                            title="Xem chi tiết so sánh trước và sau">
                                        <span class="material-symbols-outlined icon-sm" aria-hidden="true">visibility</span>
                                        Chi Tiết
                                    </button>
                                </td>

                            </tr>
                        <%
                            }
                        %>
                    </tbody>
                </table>
            </div>

            <!-- 5. PHÂN TRANG (PAGINATION) -->
            <% if (phanTrang != null && phanTrang.getTongSoTrang() > 0) { %>
                <div class="crm-pagination-bar">
                    <div class="pagination-info">
                        Hiển thị từ <strong><%= phanTrang.getBanGhiBatDau() %></strong> đến
                        <strong><%= phanTrang.getBanGhiKetThuc() %></strong> trong tổng số
                        <strong><%= phanTrang.getTongSoBanGhi() %></strong> thay đổi nhạy cảm
                    </div>

                    <nav class="pagination-controls" aria-label="Điều hướng phân trang">
                        <!-- Trang trước -->
                        <% if (phanTrang.isCoTrangTruoc()) { %>
                            <a href="<%= taoUrlTrang(contextPath, boLoc, phanTrang.getTrangHienTai() - 1) %>"
                               class="page-btn page-nav" id="page-prev" title="Trang trước"><span class="material-symbols-outlined icon-xs" style="vertical-align: -2px;" aria-hidden="true">arrow_back</span> Trước</a>
                        <% } else { %>
                            <span class="page-btn page-nav disabled" aria-disabled="true"><span class="material-symbols-outlined icon-xs" style="vertical-align: -2px;" aria-hidden="true">arrow_back</span> Trước</span>
                        <% } %>

                        <!-- Các số trang -->
                        <%
                            int totalPages = phanTrang.getTongSoTrang();
                            int current = phanTrang.getTrangHienTai();
                            for (int p = 1; p <= totalPages; p++) {
                                if (p == 1 || p == totalPages || (p >= current - 2 && p <= current + 2)) {
                                    boolean isCurrent = (p == current);
                        %>
                            <a href="<%= taoUrlTrang(contextPath, boLoc, p) %>"
                               class="page-btn <%= isCurrent ? "active" : "" %>"
                               <%= isCurrent ? "aria-current=\"page\"" : "" %>><%= p %></a>
                        <%
                                } else if (p == current - 3 || p == current + 3) {
                        %>
                            <span class="page-ellipsis">&hellip;</span>
                        <%
                                }
                            }
                        %>

                        <!-- Trang sau -->
                        <% if (phanTrang.isCoTrangSau()) { %>
                            <a href="<%= taoUrlTrang(contextPath, boLoc, phanTrang.getTrangHienTai() + 1) %>"
                               class="page-btn page-nav" id="page-next" title="Trang sau">Sau <span class="material-symbols-outlined icon-xs" style="vertical-align: -2px;" aria-hidden="true">arrow_forward</span></a>
                        <% } else { %>
                            <span class="page-btn page-nav disabled" aria-disabled="true">Sau <span class="material-symbols-outlined icon-xs" style="vertical-align: -2px;" aria-hidden="true">arrow_forward</span></span>
                        <% } %>
                    </nav>
                </div>
            <% } %>

        <% } %>

    </section>

    <!-- FOOTER HỆ THỐNG -->
    <footer class="crm-footer">
        <p>Hệ Thống CRM Doanh Nghiệp &copy; 2026 - Nhóm 10 (Thực Tập Cơ Sở K1S2 N3) - Phân hệ Quản Trị Hệ Thống (Epic EP-01 / Story S2-04)</p>
    </footer>

</div>
</main>

<!-- 6. MODAL CHI TIẾT TRUY VẾT SO SÁNH TRỰC QUAN (SIDE-BY-SIDE DIFF INSPECTOR) -->
<div class="crm-modal-backdrop" id="audit-detail-modal" aria-hidden="true" role="dialog" aria-labelledby="modal-log-title">
    <div class="crm-modal-container">

        <div class="crm-modal-header">
            <div class="modal-title-wrap">
                <span class="material-symbols-outlined modal-badge-icon" aria-hidden="true">search</span>
                <div>
                    <h3 class="modal-title" id="modal-log-title">Chi Tiết Thay Đổi Dữ Liệu Nhạy Cảm</h3>
                    <p class="modal-subtitle">Đối soát truy vết chi tiết theo tiêu chuẩn an toàn bảo mật CRM</p>
                </div>
            </div>
            <button type="button" class="modal-close-btn" onclick="dongModalChiTiet()" aria-label="Đóng modal"><span class="material-symbols-outlined icon-sm" aria-hidden="true">close</span></button>
        </div>

        <div class="crm-modal-body" id="modal-content-body">
            <!-- Nội dung chi tiết được nạp động qua JavaScript -->
            <div class="modal-loading-spinner" id="modal-loading-spinner">
                <div class="spinner"></div>
                <span>Đang tải thông tin truy vết...</span>
            </div>

            <div class="modal-real-content" id="modal-real-content" style="display: none;">

                <!-- Meta bar của bản ghi -->
                <div class="modal-meta-grid">
                    <div class="meta-item">
                        <span class="meta-label">Mã truy vết:</span>
                        <span class="meta-value font-mono" id="modal-meta-log-code">-</span>
                    </div>
                    <div class="meta-item">
                        <span class="meta-label">Thời điểm ghi nhận:</span>
                        <span class="meta-value" id="modal-meta-time">-</span>
                    </div>
                    <div class="meta-item">
                        <span class="meta-label">Người thực hiện:</span>
                        <span class="meta-value font-bold" id="modal-meta-actor">-</span>
                    </div>
                    <div class="meta-item">
                        <span class="meta-label">Vai trò tài khoản:</span>
                        <span class="meta-value" id="modal-meta-role">-</span>
                    </div>
                    <div class="meta-item">
                        <span class="meta-label">Địa chỉ IP:</span>
                        <span class="meta-value font-mono" id="modal-meta-ip">-</span>
                    </div>
                    <div class="meta-item">
                        <span class="meta-label">Thiết bị / Trình duyệt:</span>
                        <span class="meta-value" id="modal-meta-device">-</span>
                    </div>
                </div>

                <!-- Thông tin đối tượng chịu tác động -->
                <div class="modal-target-box">
                    <div class="target-box-title">ĐỐI TƯỢNG BỊ THAY ĐỔI</div>
                    <div class="target-box-content">
                        <div>
                            <span class="text-muted">Loại dữ liệu nhạy cảm:</span>
                            <span class="badge" id="modal-target-type">-</span>
                        </div>
                        <div>
                            <span class="text-muted">Mã đối tượng:</span>
                            <code id="modal-target-code">-</code>
                        </div>
                        <div>
                            <span class="text-muted">Tên đối tượng:</span>
                            <strong id="modal-target-name">-</strong>
                        </div>
                        <div>
                            <span class="text-muted">Hành động:</span>
                            <span class="badge" id="modal-target-action">-</span>
                        </div>
                    </div>
                </div>

                <!-- SO SÁNH SIDE-BY-SIDE DIFF (TRƯỚC & SAU) -->
                <div class="modal-diff-section">
                    <div class="diff-section-heading">
                        <span>SO SÁNH THAY ĐỔI TRÊN TRƯỜNG:</span>
                        <span class="diff-field-name" id="modal-diff-field-name">-</span>
                    </div>

                    <div class="modal-diff-grid">
                        <!-- Giá trị trước -->
                        <div class="diff-panel diff-panel-before">
                            <div class="diff-panel-header">
                                <span class="material-symbols-outlined diff-status-icon" style="color: #dc2626; font-size: 18px;" aria-hidden="true">remove_circle</span>
                                <span class="diff-status-title">GIÁ TRỊ TRƯỚC KHI THAY ĐỔI</span>
                            </div>
                            <div class="diff-panel-body" id="modal-diff-before-value">
                                -
                            </div>
                        </div>

                        <div class="diff-arrow-indicator">
                            <span class="material-symbols-outlined" style="font-size: 32px;" aria-hidden="true">arrow_forward</span>
                        </div>

                        <!-- Giá trị sau -->
                        <div class="diff-panel diff-panel-after">
                            <div class="diff-panel-header">
                                <span class="material-symbols-outlined diff-status-icon" style="color: #16a34a; font-size: 18px;" aria-hidden="true">add_circle</span>
                                <span class="diff-status-title">GIÁ TRỊ SAU KHI THAY ĐỔI</span>
                            </div>
                            <div class="diff-panel-body" id="modal-diff-after-value">
                                -
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Lý do / Giải trình thay đổi -->
                <div class="modal-reason-box">
                    <div class="reason-title"><span class="material-symbols-outlined icon-sm" style="vertical-align: -2px;" aria-hidden="true">edit_note</span> LÝ DO / GIẢI TRÌNH ĐƯỢC GHI NHẬN:</div>
                    <div class="reason-content" id="modal-reason-content">-</div>
                </div>

                <!-- Cam kết bất biến -->
                <div class="audit-integrity-note">
                    <span class="material-symbols-outlined integrity-icon" aria-hidden="true">lock</span>
                    <span>Bản ghi kiểm toán được khóa toàn vẹn (Immutable Audit Log). Dữ liệu không thể bị chỉnh sửa hoặc xóa bởi bất kỳ người dùng nào theo quy chuẩn kiểm toán doanh nghiệp.</span>
                </div>

            </div>
        </div>

        <div class="crm-modal-footer">
            <button type="button" class="btn btn-outline" id="btn-copy-modal-id" onclick="saoChepMaModal()">
                <span class="material-symbols-outlined icon-sm" aria-hidden="true">content_copy</span> Sao Chép Mã Truy Vết
            </button>
            <button type="button" class="btn btn-primary" onclick="dongModalChiTiet()">
                <span class="material-symbols-outlined icon-sm" aria-hidden="true">close</span> Đóng Cửa Sổ
            </button>
        </div>

    </div>
</div>

<!-- TOAST THÔNG BÁO COPY -->
<div class="crm-toast" id="crm-toast" aria-live="polite">Đã sao chép vào bộ nhớ tạm!</div>

<script>
    window.CRM_CONTEXT_PATH = '<%= contextPath %>';
</script>
<script src="${pageContext.request.contextPath}/assets/js/navigation.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/nhat-ky-thay-doi/nhat-ky-thay-doi.js"></script>

<%!
    // Hàm phụ trợ tạo URL phân trang giữ lại các tiêu chí bộ lọc
    private String taoUrlTrang(String contextPath, BoLocNhatKyDTO boLoc, int pageNumber) {
        StringBuilder sb = new StringBuilder(contextPath).append("/nhat-ky-thay-doi?");
        sb.append("trang=").append(pageNumber);
        sb.append("&soBanGhi=").append(boLoc.getSoBanGhiTrenTrang());

        if (boLoc.getNguoiDungId() != null) {
            sb.append("&nguoiDungId=").append(boLoc.getNguoiDungId());
        }
        if (boLoc.getLoaiDoiTuong() != null && !boLoc.getLoaiDoiTuong().isEmpty()) {
            sb.append("&loaiDoiTuong=").append(urlEncode(boLoc.getLoaiDoiTuong()));
        }
        if (boLoc.getTuNgay() != null) {
            sb.append("&tuNgay=").append(boLoc.getTuNgayChuoi());
        }
        if (boLoc.getDenNgay() != null) {
            sb.append("&denNgay=").append(boLoc.getDenNgayChuoi());
        }
        if (boLoc.getQuickPeriod() != null && !boLoc.getQuickPeriod().isEmpty()) {
            sb.append("&quickPeriod=").append(urlEncode(boLoc.getQuickPeriod()));
        }
        if (boLoc.getTuKhoa() != null && !boLoc.getTuKhoa().isEmpty()) {
            sb.append("&tuKhoa=").append(urlEncode(boLoc.getTuKhoa()));
        }
        return sb.toString();
    }

    private String urlEncode(String value) {
        if (value == null) return "";
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
%>

</body>
</html>
