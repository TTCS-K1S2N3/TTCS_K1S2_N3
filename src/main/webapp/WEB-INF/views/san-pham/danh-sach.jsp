<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Danh mục Sản phẩm & Bảng giá niêm yết | CRM Bán hàng</title>
    <meta name="description" content="Quản lý danh mục hàng hóa, dịch vụ và khung giá chuẩn của doanh nghiệp. Mọi báo giá kinh doanh đều bắt buộc xuất phát từ bảng giá chuẩn.">

    <!-- Google Fonts & Material Symbols (Đồng bộ chuẩn /khach-hang và /lead) -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&family=JetBrains+Mono:wght@500;700&display=swap" rel="stylesheet">

    <!-- CSS chung toàn hệ thống -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/material-symbols.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">

    <!-- CSS Module Sản phẩm -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/san-pham/san-pham.css?v=20261018_ui_v5_final">
</head>
<body class="crm-body">

    <!-- Thanh điều hướng và Sidebar chuẩn hệ thống S1-06 -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <div class="category-page-container sanpham-page-container">

            <!-- 1. Breadcrumb điều hướng chuẩn theo phân quyền -->
            <nav class="category-breadcrumb sanpham-breadcrumb crm-breadcrumb" aria-label="Breadcrumb">
                <ul class="category-breadcrumb-list sanpham-breadcrumb-list crm-breadcrumb-list">
                    <li>
                        <a href="${pageContext.request.contextPath}/dieu-huong" class="category-breadcrumb-link sanpham-breadcrumb-link crm-breadcrumb-link">Trang chủ</a>
                    </li>
                    <li class="category-breadcrumb-separator sanpham-breadcrumb-separator crm-breadcrumb-separator" aria-hidden="true">/</li>
                    <li>
                        <c:choose>
                            <c:when test="${coQuyenQuanLy}">
                                <a href="${pageContext.request.contextPath}/danh-muc" class="category-breadcrumb-link sanpham-breadcrumb-link crm-breadcrumb-link">Danh mục & Cấu hình</a>
                            </c:when>
                            <c:otherwise>
                                <a href="${pageContext.request.contextPath}/khach-hang" class="category-breadcrumb-link sanpham-breadcrumb-link crm-breadcrumb-link">Khách hàng & Liên hệ</a>
                            </c:otherwise>
                        </c:choose>
                    </li>
                    <li class="category-breadcrumb-separator sanpham-breadcrumb-separator crm-breadcrumb-separator" aria-hidden="true">/</li>
                    <li class="category-breadcrumb-active sanpham-breadcrumb-active crm-breadcrumb-active" aria-current="page">Sản phẩm & Bảng giá</li>
                </ul>
                <div>
                    <c:choose>
                        <c:when test="${coQuyenQuanLy}">
                            <a href="${pageContext.request.contextPath}/danh-muc" class="btn btn-outline btn-sm" id="btnQuayLai">
                                <span class="material-symbols-outlined icon-xs" aria-hidden="true">arrow_back</span>
                                <span>Quay lại Danh mục</span>
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/khach-hang" class="btn btn-outline btn-sm" id="btnQuayLai">
                                <span class="material-symbols-outlined icon-xs" aria-hidden="true">arrow_back</span>
                                <span>Quay lại Khách hàng</span>
                            </a>
                        </c:otherwise>
                    </c:choose>
                </div>
            </nav>

            <!-- 2. Header Section (Chuẩn Card Banner đồng bộ /danh-muc, /lead) -->
            <header class="category-header-banner sanpham-header-banner">
                <div class="category-header-title-wrap sanpham-header-title-wrap">
                    <div class="category-header-icon-box sanpham-header-icon-box" aria-hidden="true">
                        <span class="material-symbols-outlined">inventory_2</span>
                    </div>
                    <div class="category-header-text sanpham-header-text">
                        <h1>Danh mục Sản phẩm & Bảng giá niêm yết</h1>
                        <p>
                            Quản lý danh mục hàng hóa, dịch vụ và khung giá chuẩn của doanh nghiệp. Mọi báo giá kinh doanh đều bắt buộc xuất phát từ bảng giá chuẩn này thay vì giá tự tính.
                        </p>
                    </div>
                </div>

                <div class="category-header-actions sanpham-header-actions">
                    <!-- Thông tin vai trò người dùng -->
                    <div class="role-badge-box">
                        <c:choose>
                            <c:when test="${coQuyenGiaVon}">
                                <span class="role-badge role-badge-director" title="Đang ở vai trò Giám đốc: Toàn quyền quản trị và xem/sửa Giá vốn">
                                    <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">verified_user</span>
                                    <span>Giám đốc kinh doanh (${not empty nguoiDungHienTai ? nguoiDungHienTai.hoTen : 'Director'})</span>
                                </span>
                            </c:when>
                            <c:otherwise>
                                <span class="role-badge role-badge-sales" title="Đang ở vai trò người dùng: Không xem Giá vốn">
                                    <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">person</span>
                                    <span>${not empty nguoiDungHienTai ? nguoiDungHienTai.hoTen : 'Nhân viên'} (${not empty nguoiDungHienTai ? nguoiDungHienTai.chuoiVaiTroHienThi : 'Xem'})</span>
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <c:if test="${coQuyenQuanLy}">
                        <a href="${pageContext.request.contextPath}/san-pham/tao"
                           class="btn btn-primary" id="btnThemSanPhamMoi">
                            <span class="material-symbols-outlined icon-sm" aria-hidden="true">add</span>
                            <span>Khai báo sản phẩm mới</span>
                        </a>
                    </c:if>
                </div>
            </header>

            <!-- 3. Liên kết 5 phân hệ Cấu hình & Danh mục Sprint 2 -->
            <nav class="submodule-nav-bar" aria-label="Các phân hệ danh mục và cấu hình">
                <a href="${pageContext.request.contextPath}/danh-muc-ban-hang" class="submodule-nav-link">
                    <span class="material-symbols-outlined" aria-hidden="true">category</span>
                    <span>Danh mục dùng chung</span>
                </a>
                <a href="${pageContext.request.contextPath}/san-pham" class="submodule-nav-link active">
                    <span class="material-symbols-outlined" aria-hidden="true">inventory_2</span>
                    <span>Sản phẩm & Bảng giá</span>
                </a>
                <a href="${pageContext.request.contextPath}/truong-tuy-chinh" class="submodule-nav-link">
                    <span class="material-symbols-outlined" aria-hidden="true">tune</span>
                    <span>Trường tùy chỉnh</span>
                </a>
                <a href="${pageContext.request.contextPath}/pipeline/giai-doan" class="submodule-nav-link">
                    <span class="material-symbols-outlined" aria-hidden="true">linear_scale</span>
                    <span>Giai đoạn Pipeline</span>
                </a>
                <a href="${pageContext.request.contextPath}/danh-muc/ly-do-thang-thua" class="submodule-nav-link">
                    <span class="material-symbols-outlined" aria-hidden="true">flag</span>
                    <span>Lý do thắng/thua & Đối thủ</span>
                </a>
            </nav>

            <!-- 4. Thông báo Flash Messages -->
            <c:if test="${not empty thongBaoThanhCong}">
                <div class="alert alert-success" role="alert">
                    <div style="display: flex; align-items: center; gap: 8px;">
                        <span class="material-symbols-outlined icon-sm" style="color: var(--success);" aria-hidden="true">check_circle</span>
                        <span><strong>Thành công:</strong> ${thongBaoThanhCong}</span>
                    </div>
                    <span class="alert-close" onclick="this.parentElement.style.display='none';" title="Đóng" aria-label="Đóng">
                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">close</span>
                    </span>
                </div>
            </c:if>

            <c:if test="${not empty thongBaoLoi}">
                <div class="alert alert-danger" role="alert">
                    <div style="display: flex; align-items: center; gap: 8px;">
                        <span class="material-symbols-outlined icon-sm" style="color: var(--danger);" aria-hidden="true">error</span>
                        <span><strong>Thông báo:</strong> ${thongBaoLoi}</span>
                    </div>
                    <span class="alert-close" onclick="this.parentElement.style.display='none';" title="Đóng" aria-label="Đóng">
                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">close</span>
                    </span>
                </div>
            </c:if>

            <!-- 5. Thống kê tổng quan KPI -->
            <section class="stats-grid">
                <div class="stat-card">
                    <div class="stat-icon primary">
                        <span class="material-symbols-outlined" aria-hidden="true">inventory_2</span>
                    </div>
                    <div class="stat-content">
                        <span class="stat-value">${phanTrang.tongSoBanGhi}</span>
                        <span class="stat-label">Tổng mặt hàng trong danh mục</span>
                    </div>
                </div>

                <div class="stat-card">
                    <div class="stat-icon purple">
                        <span class="material-symbols-outlined" aria-hidden="true">category</span>
                    </div>
                    <div class="stat-content">
                        <span class="stat-value">Dịch vụ & Sản phẩm</span>
                        <span class="stat-label">Thuê bao định kỳ / Một lần</span>
                    </div>
                </div>

                <div class="stat-card">
                    <div class="stat-icon warning">
                        <span class="material-symbols-outlined" aria-hidden="true">price_check</span>
                    </div>
                    <div class="stat-content">
                        <span class="stat-value">Giá sàn niêm yết</span>
                        <span class="stat-label">Ngưỡng kiểm soát chiết khấu</span>
                    </div>
                </div>

                <div class="stat-card">
                    <div class="stat-icon success">
                        <span class="material-symbols-outlined" aria-hidden="true">lock</span>
                    </div>
                    <div class="stat-content">
                        <span class="stat-value">${coQuyenGiaVon ? 'Được bảo mật' : 'Ẩn hoàn toàn'}</span>
                        <span class="stat-label">Giá vốn & Tỷ suất lợi nhuận</span>
                    </div>
                </div>
            </section>

            <!-- 6. Card danh sách & Bộ lọc -->
            <main class="crm-card">
                <!-- Bộ lọc tìm kiếm -->
                <form method="get" action="${pageContext.request.contextPath}/san-pham" class="filter-bar">
                    <div class="filter-input-group">
                        <input type="text" name="tuKhoa" value="${tuKhoa}"
                               placeholder="Tìm theo mã hàng, tên sản phẩm hoặc dịch vụ..."
                               class="filter-input" autocomplete="off">
                    </div>

                    <select name="loai" class="filter-select" aria-label="Lọc theo loại sản phẩm">
                        <option value="">-- Tất cả loại sản phẩm --</option>
                        <c:forEach var="l" items="${danhSachLoai}">
                            <option value="${l.maLoai}" ${loaiChon == l.maLoai ? 'selected' : ''}>
                                ${l.tenHienThi}
                            </option>
                        </c:forEach>
                    </select>

                    <select name="trangThai" class="filter-select" aria-label="Lọc theo trạng thái kinh doanh">
                        <option value="">-- Tất cả trạng thái --</option>
                        <c:forEach var="tt" items="${danhSachTrangThai}">
                            <option value="${tt.maTrangThai}" ${trangThaiChon == tt.maTrangThai ? 'selected' : ''}>
                                ${tt.tenHienThi}
                            </option>
                        </c:forEach>
                    </select>

                    <button type="submit" class="btn btn-outline" id="btnLocSanPham">
                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">filter_alt</span>
                        <span>Lọc dữ liệu</span>
                    </button>
                    <a href="${pageContext.request.contextPath}/san-pham"
                       class="btn btn-outline" style="color: var(--slate-500);">
                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">restart_alt</span>
                        <span>Đặt lại</span>
                    </a>
                </form>

                <!-- Bảng danh sách sản phẩm & Bảng giá -->
                <div class="table-responsive">
                    <table class="crm-table" id="bangSanPham">
                        <thead>
                        <tr>
                            <th style="width: 95px;">Mã sản phẩm</th>
                            <th style="min-width: 150px;">Tên sản phẩm / Dịch vụ</th>
                            <th style="width: 95px; text-align: center;">Loại</th>
                            <th style="width: 50px; text-align: center;">ĐVT</th>
                            <th style="text-align: right; width: 105px;">Giá niêm yết</th>
                            <th style="text-align: right; width: 110px;" title="Ngưỡng duyệt chiết khấu báo giá (AC2)">
                                Giá sàn
                            </th>
                            <c:if test="${coQuyenGiaVon}">
                                <th style="text-align: right; width: 110px; background-color: #f1f5f9;" title="Chỉ Giám đốc kinh doanh xem được (AC3)">
                                    Giá vốn (Director)
                                </th>
                            </c:if>
                            <th style="text-align: center; width: 95px;">Trạng thái</th>
                            <th style="text-align: center; width: 85px;" title="Ràng buộc xóa khi đã có trong báo giá (AC4)">
                                Báo giá LK
                            </th>
                            <th style="text-align: center; width: 150px;">Thao tác</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${empty phanTrang.danhSach}">
                                <tr>
                                    <td colspan="${coQuyenGiaVon ? 10 : 9}" style="text-align: center; padding: 48px 16px; color: var(--slate-500);">
                                        <div style="font-weight: 600; font-size: 15px; color: var(--slate-700);">Không tìm thấy sản phẩm nào</div>
                                        <div style="font-size: 13px; margin-top: 4px;">Thử thay đổi từ khóa hoặc điều kiện lọc để hiển thị kết quả.</div>
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="sp" items="${phanTrang.danhSach}">
                                    <tr id="row-sp-${sp.id}">
                                        <td>
                                            <span class="product-code-pill">${sp.maSanPham}</span>
                                        </td>
                                        <td>
                                            <div class="product-name">${sp.tenSanPham}</div>
                                            <c:if test="${not empty sp.moTa}">
                                                <div class="product-desc" title="${sp.moTa}">${sp.moTa}</div>
                                            </c:if>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${sp.dichVuThueBao}">
                                                     <span class="badge badge-subscription">
                                                        ${sp.tenLoai}
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge-onetime">
                                                        ${sp.tenLoai}
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center;"><strong>${sp.donViTinh}</strong></td>

                                        <!-- Giá niêm yết -->
                                        <td style="text-align: right;">
                                            <span style="font-weight: 800; color: var(--primary); font-size: 14.5px;">
                                                ${sp.giaNiemYetDinhDang}
                                            </span>
                                        </td>

                                        <!-- Giá sàn (Ngưỡng duyệt chiết khấu) -->
                                        <td style="text-align: right;">
                                            <div class="floor-threshold-pill">
                                                <span class="floor-price-val">${sp.giaSanDinhDang}</span>
                                                <span class="floor-price-sub">Ngưỡng duyệt</span>
                                            </div>
                                        </td>

                                        <!-- AC3: Giá vốn chỉ Giám đốc kinh doanh xem được -->
                                        <c:if test="${coQuyenGiaVon}">
                                            <td style="text-align: right; background-color: #fafafa;">
                                                <div style="font-weight: 700; color: #166534;">
                                                    ${sp.giaVonDinhDang}
                                                </div>
                                                <c:if test="${not empty sp.giaVon and sp.giaVon > 0 and sp.giaNiemYet > 0}">
                                                    <div style="font-size: 11px; color: var(--slate-500);" title="Tỷ suất lợi nhuận gộp danh nghĩa">
                                                        Lãi gộp: <fmt:formatNumber value="${((sp.giaNiemYet - sp.giaVon) / sp.giaNiemYet) * 100}" maxFractionDigits="1"/>%
                                                    </div>
                                                </c:if>
                                            </td>
                                        </c:if>

                                        <!-- Trạng thái kinh doanh -->
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${sp.dangKinhDoanh}">
                                                    <span class="badge badge-active" title="Trạng thái: Đang kinh doanh">
                                                        <span class="material-symbols-outlined icon-xs" style="font-size: 13px;" aria-hidden="true">check_circle</span>
                                                        <span>Đang bán</span>
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge-inactive" title="Trạng thái: Ngừng kinh doanh">
                                                        <span class="material-symbols-outlined icon-xs" style="font-size: 13px;" aria-hidden="true">pause_circle</span>
                                                        <span>Ngừng bán</span>
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>

                                        <!-- AC4: Ràng buộc đã xuất hiện trong Báo giá -->
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${sp.daXuatHienTrongBaoGia}">
                                                    <span class="badge badge-quote"
                                                          title="Sản phẩm đã xuất hiện trong báo giá thực tế. Theo quy định, không thể xoá, chỉ được ngừng kinh doanh.">
                                                        Đã gắn BG
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="font-size: 13px; color: var(--slate-400);">—</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>

                                        <!-- Hành động / Thao tác -->
                                        <td style="text-align: center;">
                                            <div class="table-actions">
                                                <!-- AC2: Simulator Thử giá sàn trong báo giá -->
                                                <button type="button" class="btn-table-action btn-action-simulator"
                                                        data-id="${sp.id}"
                                                        data-ten="<c:out value='${sp.tenSanPham}'/>"
                                                        data-gia-niem-yet="${sp.giaNiemYet}"
                                                        data-gia-san="${sp.giaSan}"
                                                        onclick="moModalThuGiaSan(this)"
                                                        title="Thử giá sàn / Giả lập chiết khấu (AC2)"
                                                        aria-label="Thử giá sàn">
                                                    <span class="material-symbols-outlined" aria-hidden="true">calculate</span>
                                                </button>

                                                <c:if test="${coQuyenQuanLy}">
                                                    <!-- Sửa thông tin & bảng giá -->
                                                    <a href="${pageContext.request.contextPath}/san-pham/sua?id=${sp.id}"
                                                       class="btn-table-action btn-action-edit"
                                                       title="Sửa thông tin sản phẩm và bảng giá"
                                                       aria-label="Sửa sản phẩm">
                                                        <span class="material-symbols-outlined" aria-hidden="true">edit</span>
                                                    </a>

                                                    <!-- Ngừng kinh doanh / Bán lại -->
                                                    <form method="post" action="${pageContext.request.contextPath}/san-pham/trang-thai" style="display:inline; margin: 0;">
                                                        <input type="hidden" name="id" value="${sp.id}">
                                                        <c:choose>
                                                            <c:when test="${sp.dangKinhDoanh}">
                                                                <input type="hidden" name="trangThaiMoi" value="NGUNG_KINH_DOANH">
                                                                <button type="submit" class="btn-table-action btn-action-pause"
                                                                        onclick="return confirm('Bạn có chắc chắn muốn chuyển sản phẩm này sang trạng thái Ngừng kinh doanh?')"
                                                                        title="Chuyển sang Ngừng kinh doanh (không cho lên báo giá mới)"
                                                                        aria-label="Ngừng kinh doanh">
                                                                    <span class="material-symbols-outlined" aria-hidden="true">pause_circle</span>
                                                                </button>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <input type="hidden" name="trangThaiMoi" value="DANG_KINH_DOANH">
                                                                <button type="submit" class="btn-table-action btn-action-play"
                                                                        title="Mở bán lại (cho phép chọn trong báo giá)"
                                                                        aria-label="Mở bán lại">
                                                                    <span class="material-symbols-outlined" aria-hidden="true">play_circle</span>
                                                                </button>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </form>

                                                    <!-- AC4: Xóa sản phẩm -->
                                                    <c:choose>
                                                        <c:when test="${sp.daXuatHienTrongBaoGia}">
                                                            <!-- Đã có trong báo giá -> Nút xóa bị cấm -->
                                                            <button type="button" class="btn-table-action btn-action-lock"
                                                                    data-ten="<c:out value='${sp.tenSanPham}'/>"
                                                                    onclick="moModalKhongTheXoa(this)"
                                                                    title="Sản phẩm đã xuất hiện trong báo giá, không thể xóa (AC4)"
                                                                    aria-label="Đã dùng trong báo giá">
                                                                <span class="material-symbols-outlined" aria-hidden="true">lock</span>
                                                            </button>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <!-- Chưa có trong báo giá -> Được phép xóa -->
                                                            <form method="post" action="${pageContext.request.contextPath}/san-pham/xoa" style="display:inline; margin: 0;">
                                                                <input type="hidden" name="id" value="${sp.id}">
                                                                <button type="submit" class="btn-table-action btn-action-delete"
                                                                        onclick="return confirm('CẢNH BÁO: Bạn có chắc chắn muốn xoá hoàn toàn sản phẩm này khỏi hệ thống? Thao tác này không thể hoàn tác.')"
                                                                        title="Xoá vĩnh viễn khỏi danh mục"
                                                                        aria-label="Xóa sản phẩm">
                                                                    <span class="material-symbols-outlined" aria-hidden="true">delete</span>
                                                                </button>
                                                            </form>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </c:if>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                        </tbody>
                    </table>
                </div>

                <!-- Phân trang -->
                <c:if test="${phanTrang.tongSoTrang > 1}">
                    <nav class="pagination" aria-label="Phân trang danh mục">
                        <div>
                            Hiển thị trang <strong>${phanTrang.trangHienTai}</strong> / <strong>${phanTrang.tongSoTrang}</strong>
                            (Tổng số <strong>${phanTrang.tongSoBanGhi}</strong> sản phẩm)
                        </div>
                        <div class="pagination-links">
                            <c:if test="${phanTrang.coTrangTruoc}">
                                <a href="${pageContext.request.contextPath}/san-pham?page=${phanTrang.trangHienTai - 1}&tuKhoa=${tuKhoa}&loai=${loaiChon}&trangThai=${trangThaiChon}"
                                   class="page-btn">
                                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">chevron_left</span>
                                    <span>Trang trước</span>
                                </a>
                            </c:if>

                            <c:forEach var="i" begin="1" end="${phanTrang.tongSoTrang}">
                                <a href="${pageContext.request.contextPath}/san-pham?page=${i}&tuKhoa=${tuKhoa}&loai=${loaiChon}&trangThai=${trangThaiChon}"
                                   class="page-btn ${i == phanTrang.trangHienTai ? 'active' : ''}">
                                    ${i}
                                </a>
                            </c:forEach>

                            <c:if test="${phanTrang.coTrangSau}">
                                <a href="${pageContext.request.contextPath}/san-pham?page=${phanTrang.trangHienTai + 1}&tuKhoa=${tuKhoa}&loai=${loaiChon}&trangThai=${trangThaiChon}"
                                   class="page-btn">
                                    <span>Trang sau</span>
                                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">chevron_right</span>
                                </a>
                            </c:if>
                        </div>
                    </nav>
                </c:if>
            </main>

        </div>
    </main>

    <!-- =======================================================================
         MODAL: KHAI BÁO SẢN PHẨM MỚI (Đồng bộ chuẩn Modal /danh-muc-ban-hang)
         ======================================================================= -->
    <c:if test="${coQuyenQuanLy}">
        <div id="modalTaoSanPham" class="modal-overlay" role="dialog" aria-modal="true" aria-labelledby="modalTaoSanPhamTitle" style="display: none;">
            <div class="modal-dialog">
                <form id="formTaoSanPhamModal" method="post" action="${pageContext.request.contextPath}/san-pham/tao" class="modal-form" novalidate>
                    <div class="modal-header">
                        <div class="modal-title-wrap">
                            <span class="modal-icon-badge icon-add">
                                <span class="material-symbols-outlined" aria-hidden="true">add_circle</span>
                            </span>
                            <div>
                                <h3 class="modal-title" id="modalTaoSanPhamTitle">Khai báo Sản phẩm / Dịch vụ mới</h3>
                                <span class="modal-subtitle">Thiết lập danh mục và bảng giá niêm yết chuẩn (Story S2-05)</span>
                            </div>
                        </div>
                        <button type="button" id="btnCloseModalTao" class="modal-close-btn" aria-label="Đóng cửa sổ" title="Đóng cửa sổ (ESC)">
                            <span class="material-symbols-outlined" aria-hidden="true">close</span>
                        </button>
                    </div>

                    <div class="modal-body">
                        <div class="form-grid" style="padding: 0;">
                            <!-- Mã sản phẩm (AC1) -->
                            <div class="form-group">
                                <label for="taoSp_maSanPham">
                                    Mã sản phẩm / dịch vụ <span class="required" style="color: var(--sp-danger);">*</span>
                                </label>
                                <input type="text" id="taoSp_maSanPham" name="maSanPham"
                                       placeholder="VD: CRM-PRO-YR, CLOUD-STORAGE"
                                       style="text-transform: uppercase; font-family: var(--sp-font-mono); font-weight: 700;"
                                       required autocomplete="off">
                                <span id="errModal_maSanPham" class="field-error" style="display: none;"></span>
                                <span class="form-hint">Mã duy nhất trong hệ thống (2 - 50 ký tự: chữ, số, dấu gạch ngang).</span>
                            </div>

                            <!-- Loại sản phẩm (AC1) -->
                            <div class="form-group">
                                <label for="taoSp_loai">
                                    Loại mặt hàng <span class="required" style="color: var(--sp-danger);">*</span>
                                </label>
                                <select id="taoSp_loai" name="loai" required>
                                    <c:forEach var="l" items="${danhSachLoai}">
                                        <option value="${l.maLoai}">
                                            ${l.tenHienThi} (${l.moTa})
                                        </option>
                                    </c:forEach>
                                </select>
                                <span class="form-hint">Phân biệt sản phẩm mua đứt một lần hoặc dịch vụ thuê bao định kỳ.</span>
                            </div>

                            <!-- Tên sản phẩm (AC1) -->
                            <div class="form-group full-width">
                                <label for="taoSp_tenSanPham">
                                    Tên sản phẩm / dịch vụ <span class="required" style="color: var(--sp-danger);">*</span>
                                </label>
                                <input type="text" id="taoSp_tenSanPham" name="tenSanPham"
                                       placeholder="VD: Phần mềm Quản trị khách hàng CRM - Bản Doanh nghiệp Enterprise"
                                       required autocomplete="off">
                                <span id="errModal_tenSanPham" class="field-error" style="display: none;"></span>
                            </div>

                            <!-- Đơn vị tính (AC1) -->
                            <div class="form-group">
                                <label for="taoSp_donViTinh">
                                    Đơn vị tính (ĐVT) <span class="required" style="color: var(--sp-danger);">*</span>
                                </label>
                                <input type="text" id="taoSp_donViTinh" name="donViTinh"
                                       placeholder="VD: Người dùng/Tháng, Buổi, Bản quyền, Gói"
                                       list="danhSachDVTModal" required autocomplete="off">
                                <datalist id="danhSachDVTModal">
                                    <option value="Người dùng/Tháng">
                                    <option value="Người dùng/Năm">
                                    <option value="Gói triển khai">
                                    <option value="Buổi đào tạo">
                                    <option value="Giờ tư vấn">
                                    <option value="Bản quyền vĩnh viễn">
                                    <option value="Bộ thiết bị">
                                </datalist>
                                <span id="errModal_donViTinh" class="field-error" style="display: none;"></span>
                            </div>

                            <!-- Trạng thái kinh doanh -->
                            <div class="form-group">
                                <label for="taoSp_trangThai">
                                    Trạng thái kinh doanh
                                </label>
                                <select id="taoSp_trangThai" name="trangThai">
                                    <c:forEach var="tt" items="${danhSachTrangThai}">
                                        <option value="${tt.maTrangThai}">
                                            ${tt.tenHienThi}
                                        </option>
                                    </c:forEach>
                                </select>
                                <span class="form-hint">Mặt hàng "Ngừng kinh doanh" sẽ không hiển thị khi tạo báo giá mới.</span>
                            </div>

                            <!-- Giá niêm yết (AC1) -->
                            <div class="form-group">
                                <label for="taoSp_giaNiemYet">
                                    Giá niêm yết (VNĐ) <span class="required" style="color: var(--sp-danger);">*</span>
                                </label>
                                <input type="number" id="taoSp_giaNiemYet" name="giaNiemYet"
                                       min="0" step="1000" placeholder="VD: 15000000" required>
                                <span id="errModal_giaNiemYet" class="field-error" style="display: none;"></span>
                                <div id="liveTag_taoSp_giaNiemYet" class="currency-live-tag" style="display: none;"></div>
                                <span class="form-hint">Đơn giá niêm yết chính thức, mặc định điền vào dòng báo giá ban đầu.</span>
                            </div>

                            <!-- Giá sàn (AC1, AC2) -->
                            <div class="form-group">
                                <label for="taoSp_giaSan">
                                    Giá sàn (VNĐ) <span class="required" style="color: var(--sp-danger);">*</span>
                                </label>
                                <input type="number" id="taoSp_giaSan" name="giaSan"
                                       min="0" step="1000" placeholder="VD: 12000000" required>
                                <span id="errModal_giaSan" class="field-error" style="display: none;"></span>
                                <div id="liveTag_taoSp_giaSan" class="currency-live-tag" style="display: none;"></div>
                                <div id="taoSp_thresholdInfoBox" style="margin-top: 8px;"></div>
                                <span class="form-hint" style="color: #b45309; font-weight: 500; display: inline-flex; align-items: center; gap: 4px;">
                                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">bolt</span>
                                    <span><strong>Ngưỡng duyệt chiết khấu:</strong> Đơn giá bán dưới mức này sẽ yêu cầu Giám đốc phê duyệt.</span>
                                </span>
                            </div>

                            <!-- AC3: Giá vốn chỉ Giám đốc kinh doanh xem và sửa được -->
                            <c:choose>
                                <c:when test="${coQuyenGiaVon}">
                                    <div class="form-group full-width">
                                        <div class="security-cost-box authorized">
                                            <div class="security-cost-header">
                                                <span class="security-cost-title" style="display: inline-flex; align-items: center; gap: 6px;">
                                                    <span class="material-symbols-outlined icon-sm" style="color: #166534;" aria-hidden="true">lock</span>
                                                    <span>Giá vốn (Cost Price) - Phân quyền Giám đốc kinh doanh</span>
                                                </span>
                                                <span class="badge badge-director-only">
                                                    CHỈ GIÁM ĐỐC KINH DOANH (DIRECTOR)
                                                </span>
                                            </div>
                                            <label for="taoSp_giaVon" style="font-size: 13px; font-weight: 600; color: #166534;">
                                                Nhập giá vốn sản phẩm (VNĐ):
                                            </label>
                                            <input type="number" id="taoSp_giaVon" name="giaVon"
                                                   min="0" step="1000" placeholder="VD: 8000000" style="margin-top: 6px;">
                                            <span id="errModal_giaVon" class="field-error" style="display: none;"></span>
                                            <div id="liveTag_taoSp_giaVon" class="currency-live-tag" style="display: none;"></div>
                                            <div id="taoSp_marginInfoBox" style="margin-top: 8px;"></div>
                                            <div class="form-hint" style="margin-top: 6px; color: #15803d;">
                                                <strong>Chính sách bảo mật AC3:</strong> Giá vốn được kiểm soát hoàn toàn ở phía máy chủ. Nhân viên kinh doanh không thể xem được giá trị này trong API hoặc giao diện.
                                            </div>
                                        </div>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="form-group full-width">
                                        <div class="security-cost-box">
                                            <div style="display: flex; align-items: center; gap: 8px; color: var(--sp-slate-600); font-size: 13.5px;">
                                                <span class="material-symbols-outlined icon-sm" aria-hidden="true">lock</span>
                                                <span><strong>Giá vốn (Cost Price):</strong> Chỉ Giám đốc kinh doanh mới có quyền xem và cấu hình giá vốn. Dữ liệu này được bảo mật hoàn toàn.</span>
                                            </div>
                                        </div>
                                    </div>
                                </c:otherwise>
                            </c:choose>

                            <!-- Mô tả sản phẩm -->
                            <div class="form-group full-width">
                                <label for="taoSp_moTa">Mô tả sản phẩm / Dịch vụ</label>
                                <textarea id="taoSp_moTa" name="moTa" rows="3"
                                          placeholder="Nhập thông tin giới thiệu, phạm vi chuyển giao hoặc ghi chú kỹ thuật..."></textarea>
                            </div>
                        </div>
                    </div>

                    <div class="modal-footer">
                        <button type="button" id="btnCancelModalTao" class="btn btn-outline">
                            <span class="material-symbols-outlined icon-xs" aria-hidden="true">close</span>
                            <span>Hủy bỏ</span>
                        </button>
                        <button type="submit" class="btn btn-primary" id="btnSubmitModalTao">
                            <span class="material-symbols-outlined icon-xs" aria-hidden="true">save</span>
                            <span>Lưu sản phẩm và Bảng giá</span>
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </c:if>

    <!-- AC2: MODAL SIMULATOR KIỂM TRA NGƯỠNG GIÁ SÀN BÁO GIÁ -->
    <div id="modalKiemTraGiaSan" class="modal-overlay" role="dialog" aria-modal="true">
        <div class="modal-card">
            <div class="modal-header">
                <h3>Kiểm tra Ngưỡng Giá sàn Báo giá</h3>
                <button type="button" class="modal-close-btn" onclick="dongModalGiaSan()" title="Đóng" aria-label="Đóng">
                    <span class="material-symbols-outlined icon-sm" aria-hidden="true">close</span>
                </button>
            </div>

            <p style="font-size: 13.5px; color: var(--slate-600); margin-bottom: 16px; line-height: 1.5;">
                Quy định nghiệp vụ (AC2): Giá sàn là ngưỡng để xác định báo giá có cần duyệt chiết khấu hay không. Bất kỳ báo giá nào có đơn giá dưới Giá sàn bắt buộc phải gửi Giám đốc kinh doanh duyệt.
            </p>

            <div class="modal-meta-box">
                <div style="font-size: 14.5px; font-weight: 700; color: var(--dark);" id="modalTenSp">
                    Tên sản phẩm
                </div>
                <div style="display: flex; justify-content: space-between; margin-top: 8px; font-size: 13px;">
                    <span>Giá niêm yết: <strong id="modalGiaNiemYet" style="color: var(--primary);">0 ₫</strong></span>
                    <span>Giá sàn (Ngưỡng duyệt): <strong id="modalGiaSan" style="color: #b45309;">0 ₫</strong></span>
                </div>
            </div>

            <input type="hidden" id="modalSanPhamId">

            <div class="form-group" style="margin-bottom: 12px;">
                <label for="modalDonGiaInput">Nhập thử đơn giá đề xuất trong Báo giá (VNĐ):</label>
                <input type="number" id="modalDonGiaInput" min="0" step="1000"
                       style="padding: 12px 14px; font-size: 16px; font-weight: 800; color: var(--dark);"
                       oninput="tinhKiemTraGiaSanClientSide()">
                <span class="form-hint">Nhập số tiền để xem báo giá có kích hoạt quy trình phê duyệt hay không.</span>
            </div>

            <div id="modalKetQuaBox"></div>

            <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 24px;">
                <button type="button" class="btn btn-outline" onclick="dongModalGiaSan()">Đóng</button>
                <button type="button" class="btn btn-primary" onclick="tinhKiemTraGiaSan()">
                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">sync</span>
                    <span>Kiểm tra với máy chủ</span>
                </button>
            </div>
        </div>
    </div>

    <!-- AC4: MODAL GIẢI THÍCH SẢN PHẨM KHÔNG THỂ XOÁ -->
    <div id="modalKhongTheXoa" class="modal-overlay" role="dialog" aria-modal="true">
        <div class="modal-card">
            <div class="modal-header">
                <h3 style="color: var(--danger); display: flex; align-items: center; gap: 6px;">
                    <span class="material-symbols-outlined icon-sm" aria-hidden="true">lock</span>
                    <span>Không thể xoá sản phẩm</span>
                </h3>
                <button type="button" class="modal-close-btn" onclick="dongModalKhongTheXoa()" title="Đóng" aria-label="Đóng">
                    <span class="material-symbols-outlined icon-sm" aria-hidden="true">close</span>
                </button>
            </div>

            <div style="background: #fef2f2; border: 1px solid #fecaca; border-radius: 10px; padding: 16px; margin-bottom: 18px; color: #991b1b; font-size: 14px; line-height: 1.5;">
                Sản phẩm <strong id="modalTenSpXoa"></strong> đã xuất hiện trong ít nhất một báo giá kinh doanh trong hệ thống.
            </div>

            <div style="font-size: 13.5px; color: var(--slate-600); line-height: 1.6; margin-bottom: 20px;">
                <p><strong>Theo quy tắc toàn vẹn dữ liệu (Acceptance Criteria 4):</strong></p>
                <ul style="margin-left: 20px; margin-top: 6px;">
                    <li>Sản phẩm đã có mặt trong báo giá khách hàng thì <strong>tuyệt đối không được xoá</strong> khỏi cơ sở dữ liệu để bảo toàn tính pháp lý và lịch sử giao dịch.</li>
                    <li>Nếu không còn cung cấp mặt hàng này nữa, bạn hãy chuyển trạng thái sang <strong>Ngừng kinh doanh</strong>.</li>
                </ul>
            </div>

            <div style="display: flex; justify-content: flex-end;">
                <button type="button" class="btn btn-outline" onclick="dongModalKhongTheXoa()">Đã hiểu</button>
            </div>
        </div>
    </div>

    <script>
        window.APP_CONTEXT = '${pageContext.request.contextPath}';
    </script>
    <script src="${pageContext.request.contextPath}/assets/js/san-pham/san-pham.js?v=20261018_modal"></script>
</body>
</html>
