<%--
    Quản lý Trường Tuỳ Chỉnh - S2-08 FE
    Story: Là Quản trị hệ thống, tôi muốn khai báo trường tuỳ chỉnh cho khách hàng và cơ hội, để đưa được những cột mà nhân viên đang tự thêm trong Excel vào hệ thống.
    Acceptance Criteria:
     - Thêm trường kiểu văn bản, số, ngày, danh sách chọn
     - Đặt được trường là bắt buộc hay không
     - Trường tuỳ chỉnh xuất hiện trong biểu mẫu, bộ lọc và bản xuất Excel
    URL: /truong-tuy-chinh
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Trường Tuỳ Chỉnh | CRM Doanh nghiệp</title>
    <meta name="description" content="Khai báo và quản lý trường tuỳ chỉnh cho Khách hàng và Cơ hội trong hệ thống CRM. Tự động đồng bộ vào biểu mẫu, bộ lọc và xuất Excel.">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/truong-tuy-chinh/truong-tuy-chinh.css">
</head>
<body>

<div class="ttc-container">

    <!-- Breadcrumb điều hướng chuẩn -->
    <nav class="crm-breadcrumb" style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
        <div style="font-size: 13.5px; color: var(--slate-500, #64748b);">
            <a href="${pageContext.request.contextPath}/dieu-huong" style="color: var(--primary, #2563eb); text-decoration: none;">Trang chủ</a>
            <span style="margin: 0 6px;">/</span>
            <a href="${pageContext.request.contextPath}/danh-muc" style="color: var(--primary, #2563eb); text-decoration: none;">Danh mục & Cấu hình</a>
            <span style="margin: 0 6px;">/</span>
            <span style="color: var(--slate-700, #334155); font-weight: 500;">Trường tùy chỉnh</span>
        </div>
        <a href="${pageContext.request.contextPath}/danh-muc" class="btn btn-outline btn-sm"><span class="material-symbols-outlined icon-xs" aria-hidden="true">arrow_back</span> Quay lại Danh mục</a>
    </nav>

    <!-- ===== HEADER ===== -->
    <header class="page-header">
        <div class="page-header-title">
            <h1>Trường Tuỳ Chỉnh Hệ Thống</h1>
            <p>
                Khai báo và chuẩn hóa các cột mở rộng mà nhân sự kinh doanh thường thêm trong file Excel để đưa trực tiếp vào dữ liệu Khách hàng & Cơ hội của hệ thống CRM.
            </p>
        </div>
        <div class="page-header-actions">
            <!-- Nút tải xuất file Excel mẫu -->
            <a id="btn-xuat-excel-top"
               href="${pageContext.request.contextPath}/truong-tuy-chinh/xuat-excel?doiTuong=${not empty tabHienTai ? tabHienTai : 'KHACH_HANG'}"
               class="btn btn-excel"
               title="Tải bản xuất Excel chứa các cột chuẩn và các trường tuỳ chỉnh đã khai báo">
                <span class="material-symbols-outlined icon-sm" aria-hidden="true">table_view</span> Tải file Excel mẫu
            </a>
            <!-- Nút thêm trường mới -->
            <a id="btn-them-truong"
               href="${pageContext.request.contextPath}/truong-tuy-chinh/tao?doiTuong=${not empty tabHienTai ? tabHienTai : 'KHACH_HANG'}"
               class="btn btn-primary"
               data-base-url="${pageContext.request.contextPath}/truong-tuy-chinh/tao">
                <span class="material-symbols-outlined icon-sm" aria-hidden="true">add</span> Khai báo trường mới
            </a>
        </div>
    </header>

    <!-- ===== THÔNG BÁO FLASH ===== -->
    <c:if test="${not empty thongBaoThanhCong}">
        <div class="alert alert-success" role="alert">
            <div><strong>Thành công:</strong> <c:out value="${thongBaoThanhCong}"/></div>
            <button type="button" class="alert-close" onclick="this.parentElement.style.display='none';" title="Đóng" aria-label="Đóng"><span class="material-symbols-outlined icon-xs" aria-hidden="true">close</span></button>
        </div>
    </c:if>
    <c:if test="${not empty thongBaoLoi}">
        <div class="alert alert-error" role="alert">
            <div><strong>Thông báo:</strong> <c:out value="${thongBaoLoi}"/></div>
            <button type="button" class="alert-close" onclick="this.parentElement.style.display='none';" title="Đóng" aria-label="Đóng"><span class="material-symbols-outlined icon-xs" aria-hidden="true">close</span></button>
        </div>
    </c:if>

    <!-- ===== STATS OVERVIEW CARDS ===== -->
    <section class="stats-grid">
        <div class="stat-card">
            <div class="stat-icon primary"><span class="material-symbols-outlined" aria-hidden="true">domain</span></div>
            <div class="stat-content">
                <span class="stat-value">${fn:length(dsTruongKhachHang)}</span>
                <span class="stat-label">Trường Khách hàng</span>
            </div>
        </div>

        <div class="stat-card">
            <div class="stat-icon info"><span class="material-symbols-outlined" aria-hidden="true">conversion_path</span></div>
            <div class="stat-content">
                <span class="stat-value">${fn:length(dsTruongCoHoi)}</span>
                <span class="stat-label">Trường Cơ hội bán hàng</span>
            </div>
        </div>

        <div class="stat-card">
            <div class="stat-icon warning"><span class="material-symbols-outlined" aria-hidden="true">star</span></div>
            <div class="stat-content">
                <c:set var="demBatBuoc" value="0"/>
                <c:forEach var="t" items="${dsTruongKhachHang}">
                    <c:if test="${t.batBuoc}"><c:set var="demBatBuoc" value="${demBatBuoc + 1}"/></c:if>
                </c:forEach>
                <c:forEach var="t" items="${dsTruongCoHoi}">
                    <c:if test="${t.batBuoc}"><c:set var="demBatBuoc" value="${demBatBuoc + 1}"/></c:if>
                </c:forEach>
                <span class="stat-value">${demBatBuoc}</span>
                <span class="stat-label">Trường bắt buộc nhập (AC2)</span>
            </div>
        </div>

        <div class="stat-card">
            <div class="stat-icon success"><span class="material-symbols-outlined" aria-hidden="true">table_view</span></div>
            <div class="stat-content">
                <c:set var="demExcel" value="0"/>
                <c:forEach var="t" items="${dsTruongKhachHang}">
                    <c:if test="${t.hienThiExcel}"><c:set var="demExcel" value="${demExcel + 1}"/></c:if>
                </c:forEach>
                <c:forEach var="t" items="${dsTruongCoHoi}">
                    <c:if test="${t.hienThiExcel}"><c:set var="demExcel" value="${demExcel + 1}"/></c:if>
                </c:forEach>
                <span class="stat-value">${demExcel}</span>
                <span class="stat-label">Tự động xuất ra Excel (AC3)</span>
            </div>
        </div>
    </section>

    <!-- ===== TABS ĐỐI TƯỢNG ===== -->
    <div class="tab-bar" role="tablist">
        <button class="tab-btn ${empty tabHienTai or tabHienTai == 'KHACH_HANG' ? 'active' : ''}"
                data-doi-tuong="KHACH_HANG"
                role="tab"
                id="tab-khach-hang"
                aria-selected="${empty tabHienTai or tabHienTai == 'KHACH_HANG'}">
            Khách hàng (${fn:length(dsTruongKhachHang)})
        </button>
        <button class="tab-btn ${tabHienTai == 'CO_HOI' ? 'active' : ''}"
                data-doi-tuong="CO_HOI"
                role="tab"
                id="tab-co-hoi"
                aria-selected="${tabHienTai == 'CO_HOI'}">
            Cơ hội bán hàng (${fn:length(dsTruongCoHoi)})
        </button>
        <button class="tab-btn tab-btn-preview"
                data-doi-tuong="PREVIEW_AC3"
                role="tab"
                id="tab-preview-ac3"
                title="Trực quan hoá trường tuỳ chỉnh xuất hiện trong biểu mẫu, bộ lọc và bản xuất Excel">
            Trực quan AC3: Biểu mẫu, Bộ lọc & Excel
        </button>
    </div>

    <!-- ===== BẢNG 1: KHÁCH HÀNG ===== -->
    <div class="table-wrapper" data-doi-tuong="KHACH_HANG">
        <div class="table-header-bar">
            <div>
                <h2>Danh sách trường tuỳ chỉnh — Khách hàng</h2>
                <p style="font-size: 0.82rem; color: #64748b; margin-top: 2px;">
                    Các trường này tự động tích hợp vào form tạo/sửa khách hàng, bộ lọc danh sách và file xuất Excel.
                </p>
            </div>
            <div style="display:flex; align-items:center; gap:10px;">
                <span class="table-count">${fn:length(dsTruongKhachHang)} trường</span>
                <a href="${pageContext.request.contextPath}/truong-tuy-chinh/xuat-excel?doiTuong=KHACH_HANG"
                   class="btn btn-excel btn-sm" title="Tải mẫu Excel Khách hàng kèm các trường tuỳ chỉnh">
                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">table_view</span> Xuất Excel
                </a>
            </div>
        </div>

        <c:choose>
            <c:when test="${empty dsTruongKhachHang}">
                <div class="empty-state">
                    <p><strong>Chưa có trường tuỳ chỉnh nào cho Khách hàng</strong></p>
                    <p>Nhấn nút "Khai báo trường mới" để thêm các cột mở rộng như: Số nhân sự, Ngân sách dự kiến, Nguồn giới thiệu...</p>
                    <a href="${pageContext.request.contextPath}/truong-tuy-chinh/tao?doiTuong=KHACH_HANG" class="btn btn-primary" style="margin-top: 12px;">
                        <span class="material-symbols-outlined icon-sm" aria-hidden="true">add</span> Thêm trường Khách hàng đầu tiên
                    </a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-responsive">
                    <table class="ttc-table" aria-label="Danh sách trường tuỳ chỉnh Khách hàng">
                        <thead>
                            <tr>
                                <th style="width:50px; text-align:center;">#</th>
                                <th>Nhãn hiển thị / Tên kỹ thuật</th>
                                <th style="width:170px;">Kiểu dữ liệu (AC1)</th>
                                <th style="width:130px; text-align:center;">Bắt buộc (AC2)</th>
                                <th style="width:130px; text-align:center;">Trong bộ lọc (AC3)</th>
                                <th style="width:130px; text-align:center;">Trong Excel (AC3)</th>
                                <th style="width:120px; text-align:center;">Trạng thái</th>
                                <th style="width:170px; text-align:center;">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="t" items="${dsTruongKhachHang}">
                                <tr>
                                    <td style="text-align:center;">
                                        <span class="thu-tu-num">${t.thuTu}</span>
                                    </td>
                                    <td>
                                        <div class="field-name-cell">
                                            <div class="field-label"><c:out value="${t.nhanHien}"/></div>
                                            <div class="field-key"><code><c:out value="${t.tenTruong}"/></code></div>
                                        </div>
                                    </td>
                                    <td>
                                        <span class="kieu-badge kieu-${t.kieuDuLieu}">
                                            <c:choose>
                                                <c:when test="${t.kieuDuLieu == 'VAN_BAN'}">Văn bản (Text)</c:when>
                                                <c:when test="${t.kieuDuLieu == 'SO'}">Số (Number)</c:when>
                                                <c:when test="${t.kieuDuLieu == 'NGAY'}">Ngày (Date)</c:when>
                                                <c:when test="${t.kieuDuLieu == 'DANH_SACH_CHON'}">
                                                    Danh sách (${fn:length(t.danhSachLuaChon)} lựa chọn)
                                                </c:when>
                                                <c:otherwise><c:out value="${t.kieuDuLieu}"/></c:otherwise>
                                            </c:choose>
                                        </span>
                                    </td>
                                    <td style="text-align:center;">
                                        <c:choose>
                                            <c:when test="${t.batBuoc}">
                                                <span class="bat-buoc-badge bat-buoc-yes" title="Bắt buộc người dùng phải nhập"><span class="material-symbols-outlined icon-xs" aria-hidden="true">star</span> Bắt buộc</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="bat-buoc-badge bat-buoc-no" title="Không bắt buộc">Tùy chọn</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="text-align:center;">
                                        <c:choose>
                                            <c:when test="${t.hienThiBoDac}">
                                                <span class="feature-active" title="Xuất hiện trong thanh tìm kiếm/lọc"><span class="material-symbols-outlined icon-xs" aria-hidden="true">check</span> Có</span>
                                            </c:when>
                                            <c:otherwise><span class="feature-inactive">—</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="text-align:center;">
                                        <c:choose>
                                            <c:when test="${t.hienThiExcel}">
                                                <span class="feature-active" title="Xuất hiện thành cột trong file xuất Excel"><span class="material-symbols-outlined icon-xs" aria-hidden="true">check</span> Có</span>
                                            </c:when>
                                            <c:otherwise><span class="feature-inactive">—</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="text-align:center;">
                                        <c:choose>
                                            <c:when test="${t.dangHoatDong}">
                                                <span class="trang-thai-badge trang-thai-on"><span class="material-symbols-outlined icon-xs" aria-hidden="true">check_circle</span> Bật</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="trang-thai-badge trang-thai-off"><span class="material-symbols-outlined icon-xs" aria-hidden="true">cancel</span> Tắt</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="text-align:center;">
                                        <div class="row-actions">
                                            <a id="btn-sua-kh-${t.id}"
                                               href="${pageContext.request.contextPath}/truong-tuy-chinh/sua?id=${t.id}"
                                               class="btn btn-secondary btn-sm" title="Sửa nhãn và cài đặt">
                                                <span class="material-symbols-outlined icon-xs" aria-hidden="true">edit</span> Sửa
                                            </a>
                                            <form method="post" action="${pageContext.request.contextPath}/truong-tuy-chinh/trang-thai" style="display:inline;">
                                                <input type="hidden" name="id" value="${t.id}">
                                                <input type="hidden" name="trangThai" value="${!t.dangHoatDong}">
                                                <button type="submit" class="btn btn-outline btn-sm"
                                                        title="${t.dangHoatDong ? 'Tạm ngưng kích hoạt trường' : 'Kích hoạt lại trường'}">
                                                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">${t.dangHoatDong ? 'pause_circle' : 'play_circle'}</span> ${t.dangHoatDong ? 'Tắt' : 'Bật'}
                                                </button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- ===== BẢNG 2: CƠ HỘI BÁN HÀNG ===== -->
    <div class="table-wrapper" data-doi-tuong="CO_HOI">
        <div class="table-header-bar">
            <div>
                <h2>Danh sách trường tuỳ chỉnh — Cơ hội bán hàng</h2>
                <p style="font-size: 0.82rem; color: #64748b; margin-top: 2px;">
                    Các trường mở rộng theo dõi tiến độ deal, nhu cầu riêng biệt của khách và ghi chú phân loại thương vụ.
                </p>
            </div>
            <div style="display:flex; align-items:center; gap:10px;">
                <span class="table-count">${fn:length(dsTruongCoHoi)} trường</span>
                <a href="${pageContext.request.contextPath}/truong-tuy-chinh/xuat-excel?doiTuong=CO_HOI"
                   class="btn btn-excel btn-sm" title="Tải mẫu Excel Cơ hội kèm các trường tuỳ chỉnh">
                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">table_view</span> Xuất Excel
                </a>
            </div>
        </div>

        <c:choose>
            <c:when test="${empty dsTruongCoHoi}">
                <div class="empty-state">
                    <p><strong>Chưa có trường tuỳ chỉnh nào cho Cơ hội</strong></p>
                    <p>Nhấn nút "Khai báo trường mới" để thêm các trường như: Lý do thắng thua dự kiến, Đối thủ cạnh tranh, Kênh chốt deal...</p>
                    <a href="${pageContext.request.contextPath}/truong-tuy-chinh/tao?doiTuong=CO_HOI" class="btn btn-primary" style="margin-top: 12px;">
                        <span class="material-symbols-outlined icon-sm" aria-hidden="true">add</span> Thêm trường Cơ hội đầu tiên
                    </a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-responsive">
                    <table class="ttc-table" aria-label="Danh sách trường tuỳ chỉnh Cơ hội">
                        <thead>
                            <tr>
                                <th style="width:50px; text-align:center;">#</th>
                                <th>Nhãn hiển thị / Tên kỹ thuật</th>
                                <th style="width:170px;">Kiểu dữ liệu (AC1)</th>
                                <th style="width:130px; text-align:center;">Bắt buộc (AC2)</th>
                                <th style="width:130px; text-align:center;">Trong bộ lọc (AC3)</th>
                                <th style="width:130px; text-align:center;">Trong Excel (AC3)</th>
                                <th style="width:120px; text-align:center;">Trạng thái</th>
                                <th style="width:170px; text-align:center;">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="t" items="${dsTruongCoHoi}">
                                <tr>
                                    <td style="text-align:center;">
                                        <span class="thu-tu-num">${t.thuTu}</span>
                                    </td>
                                    <td>
                                        <div class="field-name-cell">
                                            <div class="field-label"><c:out value="${t.nhanHien}"/></div>
                                            <div class="field-key"><code><c:out value="${t.tenTruong}"/></code></div>
                                        </div>
                                    </td>
                                    <td>
                                        <span class="kieu-badge kieu-${t.kieuDuLieu}">
                                            <c:choose>
                                                <c:when test="${t.kieuDuLieu == 'VAN_BAN'}">Văn bản (Text)</c:when>
                                                <c:when test="${t.kieuDuLieu == 'SO'}">Số (Number)</c:when>
                                                <c:when test="${t.kieuDuLieu == 'NGAY'}">Ngày (Date)</c:when>
                                                <c:when test="${t.kieuDuLieu == 'DANH_SACH_CHON'}">
                                                    Danh sách (${fn:length(t.danhSachLuaChon)} lựa chọn)
                                                </c:when>
                                                <c:otherwise><c:out value="${t.kieuDuLieu}"/></c:otherwise>
                                            </c:choose>
                                        </span>
                                    </td>
                                    <td style="text-align:center;">
                                        <c:choose>
                                            <c:when test="${t.batBuoc}">
                                                <span class="bat-buoc-badge bat-buoc-yes" title="Bắt buộc người dùng phải nhập"><span class="material-symbols-outlined icon-xs" aria-hidden="true">star</span> Bắt buộc</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="bat-buoc-badge bat-buoc-no" title="Không bắt buộc">Tùy chọn</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="text-align:center;">
                                        <c:choose>
                                            <c:when test="${t.hienThiBoDac}">
                                                <span class="feature-active" title="Xuất hiện trong thanh tìm kiếm/lọc"><span class="material-symbols-outlined icon-xs" aria-hidden="true">check</span> Có</span>
                                            </c:when>
                                            <c:otherwise><span class="feature-inactive">—</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="text-align:center;">
                                        <c:choose>
                                            <c:when test="${t.hienThiExcel}">
                                                <span class="feature-active" title="Xuất hiện thành cột trong file xuất Excel"><span class="material-symbols-outlined icon-xs" aria-hidden="true">check</span> Có</span>
                                            </c:when>
                                            <c:otherwise><span class="feature-inactive">—</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="text-align:center;">
                                        <c:choose>
                                            <c:when test="${t.dangHoatDong}">
                                                <span class="trang-thai-badge trang-thai-on"><span class="material-symbols-outlined icon-xs" aria-hidden="true">check_circle</span> Bật</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="trang-thai-badge trang-thai-off"><span class="material-symbols-outlined icon-xs" aria-hidden="true">cancel</span> Tắt</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="text-align:center;">
                                        <div class="row-actions">
                                            <a id="btn-sua-co-${t.id}"
                                               href="${pageContext.request.contextPath}/truong-tuy-chinh/sua?id=${t.id}"
                                               class="btn btn-secondary btn-sm" title="Sửa nhãn và cài đặt">
                                                <span class="material-symbols-outlined icon-xs" aria-hidden="true">edit</span> Sửa
                                            </a>
                                            <form method="post" action="${pageContext.request.contextPath}/truong-tuy-chinh/trang-thai" style="display:inline;">
                                                <input type="hidden" name="id" value="${t.id}">
                                                <input type="hidden" name="trangThai" value="${!t.dangHoatDong}">
                                                <button type="submit" class="btn btn-outline btn-sm"
                                                        title="${t.dangHoatDong ? 'Tạm ngưng kích hoạt trường' : 'Kích hoạt lại trường'}">
                                                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">${t.dangHoatDong ? 'pause_circle' : 'play_circle'}</span> ${t.dangHoatDong ? 'Tắt' : 'Bật'}
                                                </button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- ===== TAB 3: TRỰC QUAN HOÁ AC3 (BIỂU MẪU, BỘ LỌC, XUẤT EXCEL) ===== -->
    <div class="table-wrapper preview-wrapper" data-doi-tuong="PREVIEW_AC3" style="display:none;">
        <div class="table-header-bar" style="background: linear-gradient(135deg, #f0fdf4 0%, #ecfdf5 100%); border-bottom: 1.5px solid #a7f3d0;">
            <div>
                <h2 style="color: #065f46;">Trực quan hoá Acceptance Criteria 3: Biểu mẫu, Bộ lọc & Bản xuất Excel</h2>
                <p style="font-size: 0.82rem; color: #047857; margin-top: 3px;">
                    Mọi trường tuỳ chỉnh sau khi khai báo sẽ ngay lập tức được hệ thống tự động đưa vào 3 vị trí nghiệp vụ quan trọng.
                </p>
            </div>
            <div class="preview-mode-switch">
                <button type="button" class="preview-tab-btn active" onclick="chuyenCheDoPreview('FORM', this)">
                    1. Biểu mẫu (Form)
                </button>
                <button type="button" class="preview-tab-btn" onclick="chuyenCheDoPreview('FILTER', this)">
                    2. Bộ lọc (Filter)
                </button>
                <button type="button" class="preview-tab-btn" onclick="chuyenCheDoPreview('EXCEL', this)">
                    3. Xuất Excel
                </button>
            </div>
        </div>

        <div style="padding: 24px;">

            <!-- 1. BIỂU MẪU NHẬP LIỆU (FORM PREVIEW) -->
            <div id="preview-section-form" class="preview-panel active">
                <div class="preview-badge-hint">
                    <strong>Biểu mẫu động:</strong> Khi nhân viên tạo hoặc sửa Khách hàng / Cơ hội, các trường tuỳ chỉnh đang kích hoạt sẽ tự động render tại khu vực "Thông tin bổ sung tuỳ chỉnh" với đầy đủ validation và đánh dấu bắt buộc (*).
                </div>

                <div class="mock-form-card">
                    <h3 style="font-size: 1.1rem; color: #1e293b; margin-bottom: 16px; border-bottom: 1px solid #e2e8f0; padding-bottom: 10px;">
                        Biểu mẫu: Khách hàng mới (Minh họa trực tiếp)
                    </h3>
                    <div class="mock-form-grid">
                        <div class="form-group">
                            <label>Tên công ty / Khách hàng <span class="required">*</span></label>
                            <input type="text" value="Công ty TNHH Giải Pháp Công Nghệ Toàn Cầu" readonly style="background:#f8fafc;">
                        </div>
                        <div class="form-group">
                            <label>Mã số thuế</label>
                            <input type="text" value="0109887766" readonly style="background:#f8fafc;">
                        </div>
                    </div>

                    <!-- NHÚNG FRAGMENT HIỂN THỊ TRƯỜNG TUỲ CHỈNH -->
                    <div style="margin-top: 20px; padding: 18px; background: #faf5ff; border: 1.5px dashed #c084fc; border-radius: 10px;">
                        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 12px;">
                            <span style="font-size: 0.92rem; font-weight: 700; color: #6b21a8;">
                                Các trường tuỳ chỉnh Khách hàng đang có hiệu lực (${fn:length(dsTruongKhachHangHoatDong)} trường)
                            </span>
                            <span style="font-size: 0.78rem; background: #e9d5ff; color: #581c87; padding: 3px 8px; border-radius: 6px; font-weight: 600;">
                                Dynamic Form Rendering
                            </span>
                        </div>

                        <c:choose>
                            <c:when test="${empty dsTruongKhachHangHoatDong}">
                                <div style="color: #7e22ce; font-size: 0.88rem; font-style: italic;">
                                    Chưa có trường tuỳ chỉnh nào đang ở trạng thái Hoạt động. Hãy bật trạng thái để xem trên biểu mẫu.
                                </div>
                            </c:when>
                            <c:otherwise>
                                <c:set var="dsTruongTuyChinhHienThi" value="${dsTruongKhachHangHoatDong}" scope="request"/>
                                <jsp:include page="/WEB-INF/views/truong-tuy-chinh/fragment-hien-thi.jsp"/>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

            <!-- 2. BỘ LỌC TÌM KIẾM (FILTER PREVIEW) -->
            <div id="preview-section-filter" class="preview-panel" style="display:none;">
                <div class="preview-badge-hint" style="background:#eff6ff; border-color:#bfdbfe; color:#1e40af;">
                    <strong>Thanh lọc động:</strong> Các trường tuỳ chỉnh được tích chọn <em>"Hiển thị trong bộ lọc"</em> sẽ xuất hiện ngay trong thanh tìm kiếm nâng cao của danh sách khách hàng hoặc cơ hội.
                </div>

                <div class="mock-filter-card">
                    <div style="margin-bottom: 12px; font-weight: 700; font-size: 0.95rem; color: #334155;">
                        Thanh bộ lọc tìm kiếm Khách hàng (Tích hợp trường tuỳ chỉnh)
                    </div>

                    <!-- NHÚNG FRAGMENT BỘ LỌC -->
                    <c:set var="dsTruongBoDacTuyChinh" value="${dsTruongKhachHangHoatDong}" scope="request"/>
                    <jsp:include page="/WEB-INF/views/truong-tuy-chinh/fragment-bo-loc.jsp"/>

                    <div style="margin-top: 14px; display: flex; justify-content: flex-end; gap: 8px;">
                        <button type="button" class="btn btn-secondary btn-sm" onclick="alert('Demo: Đã làm mới các điều kiện lọc!')"><span class="material-symbols-outlined icon-xs" aria-hidden="true">restart_alt</span> Đặt lại</button>
                        <button type="button" class="btn btn-primary btn-sm" onclick="alert('Demo: Đang lọc theo các trường tuỳ chỉnh đã chọn!')"><span class="material-symbols-outlined icon-xs" aria-hidden="true">filter_alt</span> Áp dụng lọc</button>
                    </div>
                </div>
            </div>

            <!-- 3. BẢN XUẤT EXCEL (EXCEL PREVIEW & DOWNLOAD) -->
            <div id="preview-section-excel" class="preview-panel" style="display:none;">
                <div class="preview-badge-hint" style="background:#ecfdf5; border-color:#a7f3d0; color:#065f46;">
                    <strong>Bản xuất Excel:</strong> Các trường có tích chọn <em>"Xuất ra Excel"</em> sẽ được tự động xuất thành các cột tương ứng trong file Excel tải về mà không cần lập trình lại.
                </div>

                <div class="excel-preview-box">
                    <div class="excel-ribbon">
                        <div style="display: flex; align-items: center; gap: 8px;">
                            <strong>Microsoft Excel Preview: danh-sach-khach-hang.csv</strong>
                        </div>
                        <a href="${pageContext.request.contextPath}/truong-tuy-chinh/xuat-excel?doiTuong=KHACH_HANG" class="btn btn-excel btn-sm">
                            <span class="material-symbols-outlined icon-xs" aria-hidden="true">download</span> Tải file CSV / Excel thực tế
                        </a>
                    </div>

                    <div class="excel-grid-scroll">
                        <table class="excel-table">
                            <thead>
                                <tr>
                                    <th>A (Mã KH)</th>
                                    <th>B (Tên công ty)</th>
                                    <th>C (Mã số thuế)</th>
                                    <th>D (Điện thoại)</th>
                                    <th>E (Email)</th>
                                    <!-- CÁC CỘT TRƯỜNG TUỲ CHỈNH -->
                                    <c:forEach var="t" items="${dsTruongKhachHangHoatDong}">
                                        <c:if test="${t.hienThiExcel}">
                                            <th class="excel-custom-col">
                                                <c:out value="${t.nhanHien}"/> <span style="font-size:10px; color:#15803d;">[Custom]</span>
                                            </th>
                                        </c:if>
                                    </c:forEach>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td>KH-001</td>
                                    <td>Công ty Cổ phần Công nghệ FPT</td>
                                    <td>0101248141</td>
                                    <td>02473007300</td>
                                    <td>contact@fpt.com.vn</td>
                                    <c:forEach var="t" items="${dsTruongKhachHangHoatDong}">
                                        <c:if test="${t.hienThiExcel}">
                                            <td class="excel-custom-cell">
                                                <c:choose>
                                                    <c:when test="${t.kieuDuLieu == 'VAN_BAN'}">Hà Nội / Miền Bắc</c:when>
                                                    <c:when test="${t.kieuDuLieu == 'SO'}">500</c:when>
                                                    <c:when test="${t.kieuDuLieu == 'NGAY'}">2026-10-15</c:when>
                                                    <c:when test="${t.kieuDuLieu == 'DANH_SACH_CHON'}">
                                                        ${not empty t.danhSachLuaChon ? t.danhSachLuaChon[0] : 'Đã chọn'}
                                                    </c:when>
                                                    <c:otherwise>Dữ liệu mẫu</c:otherwise>
                                                </c:choose>
                                            </td>
                                        </c:if>
                                    </c:forEach>
                                </tr>
                                <tr>
                                    <td>KH-002</td>
                                    <td>Tập đoàn Công nghiệp Viettel</td>
                                    <td>0100109106</td>
                                    <td>02462556789</td>
                                    <td>cskh@viettel.com.vn</td>
                                    <c:forEach var="t" items="${dsTruongKhachHangHoatDong}">
                                        <c:if test="${t.hienThiExcel}">
                                            <td class="excel-custom-cell">
                                                <c:choose>
                                                    <c:when test="${t.kieuDuLieu == 'VAN_BAN'}">Trực tiếp / Đối tác VIP</c:when>
                                                    <c:when test="${t.kieuDuLieu == 'SO'}">1200</c:when>
                                                    <c:when test="${t.kieuDuLieu == 'NGAY'}">2026-11-20</c:when>
                                                    <c:when test="${t.kieuDuLieu == 'DANH_SACH_CHON'}">
                                                        ${fn:length(t.danhSachLuaChon) > 1 ? t.danhSachLuaChon[1] : (not empty t.danhSachLuaChon ? t.danhSachLuaChon[0] : 'Ưu tiên')}
                                                    </c:when>
                                                    <c:otherwise>Dữ liệu mẫu</c:otherwise>
                                                </c:choose>
                                            </td>
                                        </c:if>
                                    </c:forEach>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

        </div>
    </div>

</div><%-- end ttc-container --%>

<script>
    window.APP_CONTEXT = '${pageContext.request.contextPath}';
</script>
<script src="${pageContext.request.contextPath}/assets/js/truong-tuy-chinh/truong-tuy-chinh.js"></script>
</body>
</html>
