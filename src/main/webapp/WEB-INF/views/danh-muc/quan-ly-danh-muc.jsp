<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="vn.nhom10.crm.model.LoaiDanhMuc" %>
<%@ page import="vn.nhom10.crm.model.NguoiDung" %>
<%@ page import="vn.nhom10.crm.dto.MucDanhMucDTO" %>
<%@ page import="java.util.List" %>
<%
    LoaiDanhMuc loaiHienTai = (LoaiDanhMuc) request.getAttribute("loaiHienTai");
    LoaiDanhMuc[] danhSachLoai = (LoaiDanhMuc[]) request.getAttribute("danhSachLoaiDanhMuc");
    List<MucDanhMucDTO> danhSachMuc = (List<MucDanhMucDTO>) request.getAttribute("danhSachMuc");
    Long tongSoMuc = (Long) request.getAttribute("tongSoMuc");
    Long soMucKichHoat = (Long) request.getAttribute("soMucKichHoat");
    Long tongSoThamChieu = (Long) request.getAttribute("tongSoThamChieu");
    String tuKhoaHienTai = (String) request.getAttribute("tuKhoaHienTai");
    String thongBaoThanhCong = (String) request.getAttribute("thongBaoThanhCong");
    String thongBaoLoi = (String) request.getAttribute("thongBaoLoi");

    NguoiDung currentUser = (NguoiDung) request.getAttribute("nguoiDungHienTai");
    if (currentUser == null) {
        currentUser = (NguoiDung) session.getAttribute("nguoiDung");
    }
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Khai báo và quản lý danh mục dùng chung của bán hàng - Story S2-07 MSys CRM">
    <title>Danh Mục Bán Hàng Dùng Chung - CRM Bán Hàng</title>

    <!-- Google Fonts & Material Symbols (Đồng bộ chuẩn /khach-hang và /lead) -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&family=JetBrains+Mono:wght@500;700&display=swap" rel="stylesheet">

    <!-- CSS chung toàn hệ thống -->
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/material-symbols.css">
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/navigation.css">
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/app.css">

    <!-- Stylesheet module Danh mục dùng chung (Story S2-07) -->
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/danh-muc/sales-category.css?v=20261017_ui">
</head>
<body class="crm-body">

    <!-- Thanh điều hướng và Sidebar chuẩn hệ thống S1-06 -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <div class="category-page-container">

            <!-- 1. Breadcrumb điều hướng chuẩn -->
            <nav class="category-breadcrumb" aria-label="Breadcrumb">
                <ul class="category-breadcrumb-list">
                    <li>
                        <a href="<%= contextPath %>/dieu-huong" class="category-breadcrumb-link">Trang chủ</a>
                    </li>
                    <li class="category-breadcrumb-separator" aria-hidden="true">/</li>
                    <li>
                        <a href="<%= contextPath %>/danh-muc" class="category-breadcrumb-link">Danh mục & Cấu hình</a>
                    </li>
                    <li class="category-breadcrumb-separator" aria-hidden="true">/</li>
                    <li class="category-breadcrumb-active" aria-current="page">Danh Mục Bán Hàng Dùng Chung</li>
                </ul>
                <div>
                    <a href="<%= contextPath %>/dieu-huong" class="btn btn-outline btn-sm" id="btnQuayLaiTrangChu">
                        <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">arrow_back</span>
                        <span>Trang chủ</span>
                    </a>
                </div>
            </nav>

            <!-- 2. Header Banner & Quick Actions -->
            <header class="category-header-banner">
                <div class="category-header-title-wrap">
                    <div class="category-header-icon-box" aria-hidden="true">
                        <span class="material-symbols-outlined">category</span>
                    </div>
                    <div class="category-header-text">
                        <h1>Danh Mục Bán Hàng Dùng Chung</h1>
                        <p>Khai báo và chuẩn hóa ngành nghề, quy mô, nguồn lead và loại hoạt động cho toàn khối kinh doanh (Story S2-07)</p>
                    </div>
                </div>
                <div class="category-header-actions">
                    <button type="button" id="btnOpenAddModal" class="btn btn-primary" title="Thêm mục mới vào danh mục">
                        <span class="material-symbols-outlined" aria-hidden="true">add</span>
                        <span>Thêm mục <%= loaiHienTai.getTenHienThi() %></span>
                    </button>
                </div>
            </header>

            <!-- 3. Liên kết phân hệ Cấu hình & Danh mục Sprint 2 -->
            <nav class="submodule-nav-bar" aria-label="Các phân hệ danh mục và cấu hình">
                <a href="<%= contextPath %>/danh-muc-ban-hang" class="submodule-nav-link active">
                    <span class="material-symbols-outlined" aria-hidden="true">category</span>
                    <span>Danh mục dùng chung</span>
                </a>
                <a href="<%= contextPath %>/san-pham" class="submodule-nav-link">
                    <span class="material-symbols-outlined" aria-hidden="true">inventory_2</span>
                    <span>Sản phẩm & Bảng giá</span>
                </a>
                <a href="<%= contextPath %>/truong-tuy-chinh" class="submodule-nav-link">
                    <span class="material-symbols-outlined" aria-hidden="true">tune</span>
                    <span>Trường tùy chỉnh</span>
                </a>
                <a href="<%= contextPath %>/pipeline/giai-doan" class="submodule-nav-link">
                    <span class="material-symbols-outlined" aria-hidden="true">linear_scale</span>
                    <span>Giai đoạn Pipeline</span>
                </a>
                <a href="<%= contextPath %>/danh-muc/ly-do-thang-thua" class="submodule-nav-link">
                    <span class="material-symbols-outlined" aria-hidden="true">flag</span>
                    <span>Lý do thắng/thua & Đối thủ</span>
                </a>
            </nav>

            <!-- 4. Thông báo hệ thống (Thành công / Lỗi) -->
            <% if (thongBaoThanhCong != null && !thongBaoThanhCong.trim().isEmpty()) { %>
                <div class="alert alert-success" role="alert">
                    <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                    <span><%= thongBaoThanhCong %></span>
                </div>
            <% } %>
            <% if (thongBaoLoi != null && !thongBaoLoi.trim().isEmpty()) { %>
                <div class="alert alert-danger" role="alert">
                    <span class="material-symbols-outlined" aria-hidden="true">error</span>
                    <span><%= thongBaoLoi %></span>
                </div>
            <% } %>

            <!-- 5. Thẻ thống kê KPI (Tương thích Lead Morning KPI) -->
            <section class="category-kpi-grid" aria-label="Thống kê danh mục">
                <div class="category-kpi-card kpi-total">
                    <div class="category-kpi-icon-wrap" aria-hidden="true">
                        <span class="material-symbols-outlined">folder_open</span>
                    </div>
                    <div class="category-kpi-info">
                        <div class="category-kpi-value"><%= tongSoMuc != null ? tongSoMuc : 0 %></div>
                        <div class="category-kpi-label">Tổng số mục trong nhóm</div>
                    </div>
                </div>

                <div class="category-kpi-card kpi-active">
                    <div class="category-kpi-icon-wrap" aria-hidden="true">
                        <span class="material-symbols-outlined">check_circle</span>
                    </div>
                    <div class="category-kpi-info">
                        <div class="category-kpi-value text-success"><%= soMucKichHoat != null ? soMucKichHoat : 0 %></div>
                        <div class="category-kpi-label">Đang áp dụng (Kích hoạt)</div>
                    </div>
                </div>

                <div class="category-kpi-card kpi-usage">
                    <div class="category-kpi-icon-wrap" aria-hidden="true">
                        <span class="material-symbols-outlined">link</span>
                    </div>
                    <div class="category-kpi-info">
                        <div class="category-kpi-value text-purple"><%= tongSoThamChieu != null ? tongSoThamChieu : 0 %></div>
                        <div class="category-kpi-label">Bản ghi đang tham chiếu (AC2)</div>
                    </div>
                </div>

                <div class="category-kpi-card kpi-ac">
                    <div class="category-kpi-icon-wrap" aria-hidden="true">
                        <span class="material-symbols-outlined">verified_user</span>
                    </div>
                    <div class="category-kpi-info">
                        <div class="category-kpi-value text-indigo">4 Nhóm</div>
                        <div class="category-kpi-label">Khóa tham chiếu & Thứ tự (AC3)</div>
                    </div>
                </div>
            </section>

            <!-- 6. Tabs chọn 4 loại danh mục bán hàng (AC1) -->
            <div class="crm-tabs-nav" role="tablist" aria-label="Chọn loại danh mục bán hàng">
                <% for (LoaiDanhMuc loai : danhSachLoai) {
                    boolean isActive = (loai == loaiHienTai);
                %>
                    <a href="<%= contextPath %>/danh-muc-ban-hang?loai=<%= loai.getMa() %>"
                       class="crm-tab-btn <%= isActive ? "active" : "" %>"
                       role="tab"
                       aria-selected="<%= isActive ? "true" : "false" %>">
                        <span class="material-symbols-outlined" aria-hidden="true">
                            <% if ("NGANH_NGHE".equals(loai.getMa())) { %>
                                apartment
                            <% } else if ("QUY_MO".equals(loai.getMa())) { %>
                                groups
                            <% } else if ("NGUON_LEAD".equals(loai.getMa())) { %>
                                track_changes
                            <% } else if ("LOAI_HOAT_DONG".equals(loai.getMa())) { %>
                                call
                            <% } else { %>
                                category
                            <% } %>
                        </span>
                        <span><%= loai.getTenHienThi() %></span>
                        <span class="crm-tab-badge"><%= loai.getMa() %></span>
                    </a>
                <% } %>
            </div>

            <!-- 7. Khu vực thao tác và Bảng dữ liệu chính -->
            <div class="category-main-card">

                <!-- Toolbar: Tìm kiếm nhanh, Bộ lọc & Nút thêm mới -->
                <div class="table-toolbar">
                    <div class="toolbar-left">
                        <form action="<%= contextPath %>/danh-muc-ban-hang" method="get" class="search-form" id="searchCategoryForm">
                            <input type="hidden" name="loai" value="<%= loaiHienTai.getMa() %>">
                            <div class="quick-search-box">
                                <span class="material-symbols-outlined quick-search-icon" aria-hidden="true">search</span>
                                <input type="text"
                                       id="categorySearchInput"
                                       name="tuKhoa"
                                       value="<%= tuKhoaHienTai != null ? tuKhoaHienTai : "" %>"
                                       class="quick-search-input"
                                       placeholder="Tìm nhanh theo mã hoặc tên trong nhóm <%= loaiHienTai.getTenHienThi() %>..."
                                       aria-label="Tìm kiếm mục danh mục"
                                       autocomplete="off">
                                <button type="button" class="quick-search-clear" id="btnClearSearch" title="Xóa từ khóa" style="display: none;">
                                    <span class="material-symbols-outlined icon-sm" aria-hidden="true">close</span>
                                </button>
                            </div>
                        </form>

                        <!-- Bộ lọc nhanh trạng thái -->
                        <div class="filter-pills-group">
                            <button type="button" class="filter-chip active" data-filter="all">Tất cả (<span id="countFilterAll"><%= danhSachMuc != null ? danhSachMuc.size() : 0 %></span>)</button>
                            <button type="button" class="filter-chip" data-filter="active">Đang áp dụng</button>
                            <button type="button" class="filter-chip" data-filter="inactive">Tạm ngưng</button>
                            <button type="button" class="filter-chip" data-filter="referenced">Có tham chiếu (AC2)</button>
                        </div>
                    </div>
                </div>

                <!-- Meta bar: Tóm tắt trạng thái nhóm danh mục -->
                <div class="table-meta-bar">
                    <div class="meta-left">
                        <span class="material-symbols-outlined meta-info-icon" aria-hidden="true">info</span>
                        <span>Đang quản trị: <strong class="text-primary"><%= loaiHienTai.getTenHienThi() %></strong> — Hiển thị: <strong id="visibleItemsCount"><%= danhSachMuc != null ? danhSachMuc.size() : 0 %></strong> mục</span>
                    </div>
                    <div class="meta-right">
                        <span class="meta-tip"><span class="material-symbols-outlined icon-xs" aria-hidden="true">swap_vert</span> Bấm mũi tên hoặc kéo thả hàng để sắp xếp thứ tự hiển thị (AC3)</span>
                    </div>
                </div>

                <!-- Bảng danh mục chính (AC1, AC2, AC3) -->
                <div class="table-responsive">
                    <table class="category-table" id="categoryTable">
                        <thead>
                            <tr>
                                <th style="width: 110px;" title="Thứ tự hiển thị trong danh sách chọn (AC3)">Thứ tự</th>
                                <th style="width: 140px;">Mã định danh</th>
                                <th>Tên mục hiển thị</th>
                                <th>Mô tả chi tiết</th>
                                <th style="width: 140px; text-align: center;">Trạng thái</th>
                                <th style="width: 180px;" title="Kiểm tra ràng buộc tham chiếu trước khi xóa (AC2)">Bản ghi tham chiếu</th>
                                <th style="width: 120px;">Ngày tạo</th>
                                <th style="width: 130px; text-align: center;">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody id="categoryTableBody">
                            <% if (danhSachMuc == null || danhSachMuc.isEmpty()) { %>
                                <tr class="empty-row" id="emptyStateRow">
                                    <td colspan="8">
                                        <div class="empty-state-box">
                                            <div class="empty-icon"><span class="material-symbols-outlined" aria-hidden="true">inbox</span></div>
                                            <h3 class="empty-title">Chưa có mục danh mục nào trong nhóm này</h3>
                                            <p class="empty-desc">Khai báo các giá trị chuẩn để toàn bộ khối kinh doanh gọi tên giống nhau khi báo cáo gộp.</p>
                                            <button type="button" class="btn btn-primary" onclick="document.getElementById('btnOpenAddModal').click();">
                                                <span class="material-symbols-outlined" aria-hidden="true">add</span> Thêm mục đầu tiên
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            <% } else {
                                int total = danhSachMuc.size();
                                for (int i = 0; i < total; i++) {
                                    MucDanhMucDTO item = danhSachMuc.get(i);
                                    boolean isFirst = (i == 0);
                                    boolean isLast = (i == total - 1);
                                    boolean hasUsage = item.isDangDuocSuDung();
                            %>
                                <tr class="category-data-row"
                                    id="row-item-<%= item.getId() %>"
                                    data-id="<%= item.getId() %>"
                                    data-status="<%= item.isKichHoat() ? "active" : "inactive" %>"
                                    data-usage="<%= item.getSoBanGhiDangSuDung() %>"
                                    data-code="<%= item.getMaMuc() != null ? item.getMaMuc().toLowerCase() : "" %>"
                                    data-name="<%= item.getTenMuc() != null ? item.getTenMuc().toLowerCase() : "" %>">

                                    <!-- CỘT 1: THỨ TỰ HIỂN THỊ & NÚT SẮP XẾP (AC3) -->
                                    <td>
                                        <div class="order-cell">
                                            <span class="order-drag-handle" title="Kéo để sắp xếp vị trí"><span class="material-symbols-outlined" aria-hidden="true">drag_indicator</span></span>
                                            <span class="order-badge" title="Thứ tự hiển thị: <%= item.getThuTuHienThi() %>"><%= item.getThuTuHienThi() %></span>
                                            <div class="order-btn-group">
                                                <form action="<%= contextPath %>/danh-muc-ban-hang" method="post" style="display:inline;">
                                                    <input type="hidden" name="action" value="doi-thu-tu">
                                                    <input type="hidden" name="loaiDanhMuc" value="<%= loaiHienTai.getMa() %>">
                                                    <input type="hidden" name="id" value="<%= item.getId() %>">
                                                    <input type="hidden" name="huong" value="len">
                                                    <button type="submit" class="btn-order-move btn-move-up" title="Di chuyển lên trên (AC3)" <%= isFirst ? "disabled" : "" %>>
                                                        <span class="material-symbols-outlined" aria-hidden="true">keyboard_arrow_up</span>
                                                    </button>
                                                </form>
                                                <form action="<%= contextPath %>/danh-muc-ban-hang" method="post" style="display:inline;">
                                                    <input type="hidden" name="action" value="doi-thu-tu">
                                                    <input type="hidden" name="loaiDanhMuc" value="<%= loaiHienTai.getMa() %>">
                                                    <input type="hidden" name="id" value="<%= item.getId() %>">
                                                    <input type="hidden" name="huong" value="xuong">
                                                    <button type="submit" class="btn-order-move btn-move-down" title="Di chuyển xuống dưới (AC3)" <%= isLast ? "disabled" : "" %>>
                                                        <span class="material-symbols-outlined" aria-hidden="true">keyboard_arrow_down</span>
                                                    </button>
                                                </form>
                                            </div>
                                        </div>
                                    </td>

                                    <!-- CỘT 2: MÃ ĐỊNH DANH -->
                                    <td>
                                        <span class="code-badge" title="Mã khóa: <%= item.getMaMuc() %>"><%= item.getMaMuc() %></span>
                                    </td>

                                    <!-- CỘT 3: TÊN MỤC -->
                                    <td>
                                        <div class="item-name-cell">
                                            <strong class="item-name-text"><%= item.getTenMuc() %></strong>
                                            <% if (hasUsage) { %>
                                                <span class="tag-in-use-mini" title="Đang được tham chiếu trong báo cáo gộp">Đang dùng</span>
                                            <% } %>
                                        </div>
                                    </td>

                                    <!-- CỘT 4: MÔ TẢ CHI TIẾT -->
                                    <td>
                                        <div class="item-desc-text">
                                            <%= (item.getMoTa() != null && !item.getMoTa().isBlank())
                                                    ? item.getMoTa()
                                                    : "<span class='text-muted'>Chưa có mô tả</span>" %>
                                        </div>
                                    </td>

                                    <!-- CỘT 5: TRẠNG THÁI ÁP DỤNG -->
                                    <td style="text-align: center;">
                                        <form action="<%= contextPath %>/danh-muc-ban-hang" method="post" style="display:inline;">
                                            <input type="hidden" name="action" value="chuyen-trang-thai">
                                            <input type="hidden" name="loaiDanhMuc" value="<%= loaiHienTai.getMa() %>">
                                            <input type="hidden" name="id" value="<%= item.getId() %>">
                                            <button type="submit" class="status-toggle-btn" title="Bấm để chuyển đổi trạng thái Áp dụng / Tạm ngưng">
                                                <% if (item.isKichHoat()) { %>
                                                    <span class="status-pill status-active">
                                                        <span class="material-symbols-outlined" aria-hidden="true">check_circle</span> Áp dụng
                                                    </span>
                                                <% } else { %>
                                                    <span class="status-pill status-inactive">
                                                        <span class="material-symbols-outlined" aria-hidden="true">pause_circle</span> Tạm ngưng
                                                    </span>
                                                <% } %>
                                            </button>
                                        </form>
                                    </td>

                                    <!-- CỘT 6: SỐ BẢN GHI ĐANG THAM CHIẾU (AC2) -->
                                    <td>
                                        <% if (hasUsage) { %>
                                            <div class="usage-box usage-positive"
                                                 title="Đang có <%= item.getSoBanGhiDangSuDung() %> bản ghi nghiệp vụ tham chiếu. Theo AC2: Không thể xóa để bảo toàn tính toàn vẹn dữ liệu báo cáo gộp.">
                                                <span class="material-symbols-outlined" aria-hidden="true">link</span>
                                                <span class="usage-count"><%= item.getSoBanGhiDangSuDung() %> bản ghi</span>
                                                <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 15px; margin-left: 2px;">lock</span>
                                            </div>
                                        <% } else { %>
                                            <div class="usage-box usage-zero" title="Chưa có dữ liệu nào liên kết. Có thể xóa an toàn nếu cần.">
                                                <span class="material-symbols-outlined" aria-hidden="true">remove_circle_outline</span>
                                                <span class="usage-count">0 (Chưa dùng)</span>
                                            </div>
                                        <% } %>
                                    </td>

                                    <!-- CỘT 7: NGÀY TẠO -->
                                    <td>
                                        <span class="date-text"><%= item.getNgayTao() != null ? item.getNgayTao() : "—" %></span>
                                    </td>

                                    <!-- CỘT 8: THAO TÁC SỬA / XÓA (AC2) -->
                                    <td>
                                        <div class="action-cell">
                                            <!-- Nút Sửa -->
                                            <button type="button"
                                                    class="btn-icon-action btn-edit-item"
                                                    data-id="<%= item.getId() %>"
                                                    data-ma="<%= item.getMaMuc() %>"
                                                    data-ten="<%= item.getTenMuc() %>"
                                                    data-mota="<%= item.getMoTa() != null ? item.getMoTa() : "" %>"
                                                    data-kichhoat="<%= item.isKichHoat() %>"
                                                    data-usage="<%= item.getSoBanGhiDangSuDung() %>"
                                                    title="Chỉnh sửa thông tin mục này"
                                                    aria-label="Chỉnh sửa thông tin mục này">
                                                <span class="material-symbols-outlined" aria-hidden="true">edit</span>
                                            </button>

                                            <!-- Nút Xóa (AC2: Giá trị đang được tham chiếu thì KHÔNG xóa được) -->
                                            <% if (hasUsage) { %>
                                                <button type="button"
                                                        class="btn-icon-action btn-delete-disabled"
                                                        data-id="<%= item.getId() %>"
                                                        data-ten="<%= item.getTenMuc() %>"
                                                        data-usage="<%= item.getSoBanGhiDangSuDung() %>"
                                                        title="⛔ KHÔNG THỂ XÓA (AC2): Mục đang có <%= item.getSoBanGhiDangSuDung() %> bản ghi tham chiếu."
                                                        aria-label="Không thể xóa do đang có bản ghi tham chiếu">
                                                    <span class="material-symbols-outlined" aria-hidden="true">lock</span>
                                                </button>
                                            <% } else { %>
                                                <button type="button"
                                                        class="btn-icon-action btn-delete-item"
                                                        data-id="<%= item.getId() %>"
                                                        data-ten="<%= item.getTenMuc() %>"
                                                        data-ma="<%= item.getMaMuc() %>"
                                                        data-usage="0"
                                                        title="Xóa mục danh mục này"
                                                        aria-label="Xóa mục danh mục này">
                                                    <span class="material-symbols-outlined" aria-hidden="true">delete</span>
                                                </button>
                                            <% } %>
                                        </div>
                                    </td>
                                </tr>
                            <% } } %>
                        </tbody>
                    </table>
                </div>

            </div>

        </div>
    </main>

    <!-- =======================================================================
         MODAL 1: THÊM MỚI MỤC DANH MỤC (AC1)
         ======================================================================= -->
    <div id="modalAddCategory" class="modal-overlay" role="dialog" aria-modal="true" aria-labelledby="addModalTitle" style="display: none;">
        <div class="modal-dialog">
            <form action="<%= contextPath %>/danh-muc-ban-hang" method="post" id="formAddCategory" class="modal-form">
                <input type="hidden" name="action" value="them">
                <input type="hidden" name="loaiDanhMuc" value="<%= loaiHienTai.getMa() %>">

                <div class="modal-header">
                    <div class="modal-title-wrap">
                        <span class="modal-icon-badge icon-add"><span class="material-symbols-outlined" aria-hidden="true">add_circle</span></span>
                        <div>
                            <h3 class="modal-title" id="addModalTitle">Thêm mục mới vào: <%= loaiHienTai.getTenHienThi() %></h3>
                            <span class="modal-subtitle">Định danh chuẩn cho toàn khối kinh doanh (Story S2-07)</span>
                        </div>
                    </div>
                    <button type="button" id="btnCloseAddModal" class="modal-close-btn" aria-label="Đóng cửa sổ" title="Đóng cửa sổ">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>

                <div class="modal-body">
                    <div class="form-group">
                        <label class="form-label" for="addMaMuc">
                            Mã định danh <span class="required-star">*</span>
                        </label>
                        <div class="input-wrap">
                            <input type="text"
                                   id="addMaMuc"
                                   name="maMuc"
                                   required
                                   maxlength="50"
                                   class="form-control uppercase-input"
                                   placeholder="Ví dụ: CNTT, BAN_LE, WEB_FORM..."
                                   autocomplete="off">
                        </div>
                        <span class="form-hint">Mã viết hoa, không dấu, dùng dấu gạch dưới (_). Không được trùng lặp trong nhóm.</span>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="addTenMuc">
                            Tên mục hiển thị <span class="required-star">*</span>
                        </label>
                        <div class="input-wrap">
                            <input type="text"
                                   id="addTenMuc"
                                   name="tenMuc"
                                   required
                                   maxlength="150"
                                   class="form-control"
                                   placeholder="Ví dụ: Công nghệ thông tin & Phần mềm">
                        </div>
                        <span class="form-hint">Tên sẽ hiển thị trong các danh sách chọn khi tạo khách hàng, lead hoặc cơ hội.</span>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="addMoTa">Mô tả / Diễn giải phạm vi</label>
                        <textarea id="addMoTa"
                                  name="moTa"
                                  rows="3"
                                  class="form-control"
                                  placeholder="Diễn giải phạm vi áp dụng giúp nhân viên bán hàng chọn đúng tiêu chí..."></textarea>
                    </div>

                    <div class="form-group checkbox-group">
                        <label class="custom-checkbox-label">
                            <input type="checkbox" name="kichHoat" value="true" checked>
                            <span class="checkbox-box"></span>
                            <span class="checkbox-text">
                                <strong>Áp dụng ngay sau khi tạo</strong>
                                <small>Cho phép nhân viên kinh doanh chọn mục này ngay trong quy trình bán hàng</small>
                            </span>
                        </label>
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" id="btnCancelAddModal" class="btn btn-light">Hủy bỏ</button>
                    <button type="submit" class="btn btn-primary" id="btnSubmitAdd">
                        <span class="material-symbols-outlined icon-sm" aria-hidden="true">check_circle</span> Lưu mục danh mục
                    </button>
                </div>
            </form>
        </div>
    </div>

    <!-- =======================================================================
         MODAL 2: CHỈNH SỬA MỤC DANH MỤC (AC1)
         ======================================================================= -->
    <div id="modalEditCategory" class="modal-overlay" role="dialog" aria-modal="true" aria-labelledby="editModalTitle" style="display: none;">
        <div class="modal-dialog">
            <form action="<%= contextPath %>/danh-muc-ban-hang" method="post" id="formEditCategory" class="modal-form">
                <input type="hidden" name="action" value="sua">
                <input type="hidden" name="loaiDanhMuc" value="<%= loaiHienTai.getMa() %>">
                <input type="hidden" id="editId" name="id" value="">

                <div class="modal-header">
                    <div class="modal-title-wrap">
                        <span class="modal-icon-badge icon-edit"><span class="material-symbols-outlined" aria-hidden="true">edit</span></span>
                        <div>
                            <h3 class="modal-title" id="editModalTitle">Chỉnh sửa mục danh mục</h3>
                            <span class="modal-subtitle">Cập nhật tên hiển thị và trạng thái áp dụng</span>
                        </div>
                    </div>
                    <button type="button" id="btnCloseEditModal" class="modal-close-btn" aria-label="Đóng cửa sổ" title="Đóng cửa sổ">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>

                <div class="modal-body">
                    <!-- Cảnh báo nếu mục đang được tham chiếu -->
                    <div id="editUsageAlert" class="alert-warning-soft" style="display: none;">
                        <span class="material-symbols-outlined" aria-hidden="true">info</span>
                        <span>Mục này hiện đang có <strong id="editUsageCount">0</strong> bản ghi nghiệp vụ tham chiếu. Để bảo toàn tính toàn vẹn báo cáo gộp, mã định danh được khóa cố định.</span>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="editMaMuc">Mã định danh (Khóa cố định)</label>
                        <div class="input-wrap">
                            <input type="text"
                                   id="editMaMuc"
                                   name="maMuc"
                                   readonly
                                   disabled
                                   class="form-control readonly-input">
                        </div>
                        <span class="form-hint">Mã định danh không thể thay đổi sau khi tạo để duy trì liên kết dữ liệu.</span>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="editTenMuc">
                            Tên mục hiển thị <span class="required-star">*</span>
                        </label>
                        <div class="input-wrap">
                            <input type="text"
                                   id="editTenMuc"
                                   name="tenMuc"
                                   required
                                   maxlength="150"
                                   class="form-control">
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="editMoTa">Mô tả / Ghi chú</label>
                        <textarea id="editMoTa" name="moTa" rows="3" class="form-control"></textarea>
                    </div>

                    <div class="form-group checkbox-group">
                        <label class="custom-checkbox-label">
                            <input type="checkbox" id="editKichHoat" name="kichHoat" value="true">
                            <span class="checkbox-box"></span>
                            <span class="checkbox-text">
                                <strong>Đang áp dụng (Kích hoạt)</strong>
                                <small>Bỏ chọn để tạm ngưng mục này mà không làm ảnh hưởng đến dữ liệu lịch sử đã có</small>
                            </span>
                        </label>
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" id="btnCancelEditModal" class="btn btn-light">Hủy bỏ</button>
                    <button type="submit" class="btn btn-primary" id="btnSubmitEdit">
                        <span class="material-symbols-outlined icon-sm" aria-hidden="true">save</span> Cập nhật thay đổi
                    </button>
                </div>
            </form>
        </div>
    </div>

    <!-- =======================================================================
         MODAL 3: XÁC NHẬN XÓA MỤC DANH MỤC (AC2)
         (Giá trị đang được tham chiếu thì KHÔNG xoá được)
         ======================================================================= -->
    <div id="modalDeleteCategory" class="modal-overlay" role="dialog" aria-modal="true" aria-labelledby="deleteModalTitle" style="display: none;">
        <div class="modal-dialog modal-dialog-sm">
            <form action="<%= contextPath %>/danh-muc-ban-hang" method="post" id="formDeleteCategory" class="modal-form">
                <input type="hidden" name="action" value="xoa">
                <input type="hidden" name="loaiDanhMuc" value="<%= loaiHienTai.getMa() %>">
                <input type="hidden" id="deleteId" name="id" value="">

                <div class="modal-header">
                    <div class="modal-title-wrap">
                        <span class="modal-icon-badge icon-delete"><span class="material-symbols-outlined" aria-hidden="true">delete</span></span>
                        <div>
                            <h3 class="modal-title" style="color: var(--cat-danger);" id="deleteModalTitle">Xác nhận xóa mục danh mục</h3>
                            <span class="modal-subtitle">Thao tác này sẽ xóa vĩnh viễn mục khỏi danh mục</span>
                        </div>
                    </div>
                    <button type="button" id="btnCloseDeleteModal" class="modal-close-btn" aria-label="Đóng cửa sổ" title="Đóng cửa sổ">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>

                <div class="modal-body">
                    <p class="delete-confirm-text">
                        Bạn có chắc chắn muốn xóa mục danh mục <strong id="deleteTenMucDisplay" class="text-highlight"></strong> (<code id="deleteMaMucDisplay"></code>) không?
                    </p>

                    <div class="ac2-rule-card">
                        <div class="ac2-rule-header">
                            <span class="material-symbols-outlined" aria-hidden="true">shield</span>
                            <span>Quy tắc toàn vẹn nghiệp vụ (AC2):</span>
                        </div>
                        <p>Hệ thống chỉ cho phép xóa mục danh mục khi <strong>chưa có bất kỳ bản ghi nào tham chiếu</strong>. Nếu mục đã được sử dụng, vui lòng chuyển trạng thái sang <em>"Tạm ngưng"</em>.</p>
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" id="btnCancelDeleteModal" class="btn btn-light">Hủy bỏ</button>
                    <button type="submit" class="btn btn-danger" id="btnConfirmDelete">
                        <span class="material-symbols-outlined icon-sm" aria-hidden="true">delete</span> Xác nhận xóa
                    </button>
                </div>
            </form>
        </div>
    </div>

    <!-- =======================================================================
         MODAL 4: CẢNH BÁO KHÔNG THỂ XÓA VÌ ĐANG THAM CHIẾU (AC2 ALERT)
         ======================================================================= -->
    <div id="modalBlockedDelete" class="modal-overlay" role="dialog" aria-modal="true" style="display: none;">
        <div class="modal-dialog modal-dialog-sm">
            <div class="modal-header">
                <div class="modal-title-wrap">
                    <span class="modal-icon-badge icon-warning"><span class="material-symbols-outlined" aria-hidden="true">lock</span></span>
                    <div>
                        <h3 class="modal-title" style="color: var(--cat-warning-text);">Không thể xóa mục danh mục</h3>
                        <span class="modal-subtitle">Ràng buộc toàn vẹn dữ liệu (Acceptance Criteria 2)</span>
                    </div>
                </div>
                <button type="button" id="btnCloseBlockedModal" class="modal-close-btn" aria-label="Đóng cửa sổ" title="Đóng cửa sổ">
                    <span class="material-symbols-outlined" aria-hidden="true">close</span>
                </button>
            </div>
            <div class="modal-body">
                <p class="blocked-msg-text">
                    Mục <strong id="blockedItemName" class="text-highlight"></strong> hiện đang được tham chiếu bởi <strong id="blockedItemUsageCount" style="color: var(--cat-danger);"></strong> bản ghi dữ liệu nghiệp vụ (khách hàng, lead, hoặc hoạt động).
                </p>
                <div class="ac2-recommend-box">
                    <div class="recommend-title"><span class="material-symbols-outlined" aria-hidden="true">lightbulb</span> Giải pháp khuyến nghị:</div>
                    <p>Để không làm sai lệch các báo cáo gộp và dữ liệu lịch sử, bạn nên chuyển mục này sang trạng thái <strong>"Tạm ngưng"</strong>. Khi đó, nhân viên sẽ không thể chọn mục này cho các bản ghi mới, trong khi các bản ghi cũ vẫn giữ nguyên tính đúng đắn.</p>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" id="btnDismissBlockedModal" class="btn btn-primary">Đã hiểu</button>
            </div>
        </div>
    </div>

    <!-- Toast Container cho các tương tác tức thời -->
    <div class="crm-toast-container" id="crmToastContainer" aria-live="polite"></div>

    <!-- JavaScript xử lý client (Story S2-07 FE) -->
    <script src="<%= contextPath %>/assets/js/danh-muc/sales-category.js"></script>
</body>
</html>
