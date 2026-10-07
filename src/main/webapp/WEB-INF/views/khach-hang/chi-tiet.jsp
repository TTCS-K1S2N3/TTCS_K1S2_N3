<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Trang 360 Khách hàng - Hệ thống CRM Bán Hàng">
    <title>Trang 360 Khách Hàng - CRM Bán Hàng</title>
    <!-- CSS dùng chung toàn hệ thống -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/khach-hang/khach-hang.css">
    <!-- CSS chuyên biệt module Yêu cầu hỗ trợ & Cờ rủi ro (Story S3-08) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/khach-hang/yeu-cau-ho-tro.css">
</head>
<body class="crm-body">
    <!-- Thanh điều hướng và Sidebar chuẩn hệ thống S1-06 (đã bao gồm Material Symbols Outlined) -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <div class="ycht-page-container">

            <!-- 1. Breadcrumb điều hướng chuẩn -->
            <nav class="ycht-breadcrumb" aria-label="Breadcrumb">
                <ol class="ycht-breadcrumb-nav">
                    <li><a href="${pageContext.request.contextPath}/dieu-huong" class="ycht-breadcrumb-link">Trang chủ</a></li>
                    <span class="ycht-breadcrumb-sep" aria-hidden="true">/</span>
                    <li><a href="${pageContext.request.contextPath}/khach-hang" class="ycht-breadcrumb-link">Khách hàng</a></li>
                    <span class="ycht-breadcrumb-sep" aria-hidden="true">/</span>
                    <li class="ycht-breadcrumb-current" aria-current="page"><c:out value="${not empty banGhi.tieuDe ? banGhi.tieuDe : 'Trang 360'}" /></li>
                </ol>
                <div>
                    <a href="${pageContext.request.contextPath}/khach-hang" class="ycht-btn ycht-btn-outline ycht-btn-sm" id="btnBackKhachHang">
                        <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span>
                        <span>Danh sách khách hàng</span>
                    </a>
                </div>
            </nav>

            <!-- 2. Thông báo hệ thống (Success / Error) -->
            <c:if test="${not empty thongBaoThanhCong}">
                <div class="ycht-alert ycht-alert-success" role="alert">
                    <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                    <span><c:out value="${thongBaoThanhCong}" /></span>
                </div>
            </c:if>
            <c:if test="${not empty sessionScope.thongBaoThanhCong}">
                <div class="ycht-alert ycht-alert-success" role="alert">
                    <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                    <span><c:out value="${sessionScope.thongBaoThanhCong}" /></span>
                </div>
                <c:remove var="thongBaoThanhCong" scope="session" />
            </c:if>
            <c:if test="${not empty sessionScope.thongBaoLoi}">
                <div class="ycht-alert ycht-alert-danger" role="alert">
                    <span class="material-symbols-outlined" aria-hidden="true">error</span>
                    <span><c:out value="${sessionScope.thongBaoLoi}" /></span>
                </div>
                <c:remove var="thongBaoLoi" scope="session" />
            </c:if>

            <!-- 3. STORY S3-08 AC3: CỜ RỦI RO HIỂN THỊ TRÊN TRANG 360 VÀ CẢNH BÁO CHO NHÂN VIÊN KINH DOANH PHỤ TRÁCH -->
            <c:choose>
                <c:when test="${banGhi.coRuiRo or (not empty thongTinRuiRo and thongTinRuiRo.coRuiRo)}">
                    <section class="ycht-risk-banner" id="bannerCoRuiRoRoiBo" role="alert" aria-live="assertive">
                        <div class="ycht-risk-banner-icon" aria-hidden="true">
                            <span class="material-symbols-outlined" style="font-size: 28px;">warning</span>
                        </div>
                        <div class="ycht-risk-banner-content">
                            <h2 class="ycht-risk-banner-title">
                                <span>CẢNH BÁO RỦI RO RỜI BỎ (CHURN RISK)</span>
                                <span class="ycht-risk-banner-badge">MỨC NGUY CƠ CAO</span>
                            </h2>
                            <p class="ycht-risk-banner-desc">
                                Khách hàng này hiện đang có <strong><c:out value="${not empty thongTinRuiRo ? thongTinRuiRo.soYeuCauChuaXuLy : banGhi.soYeuCauChuaXuLy}" /></strong> yêu cầu hỗ trợ sau bán chưa được xử lý
                                (vượt ngưỡng cảnh báo <strong><c:out value="${not empty thongTinRuiRo ? thongTinRuiRo.nguongRuiRo : 3}" /></strong> yêu cầu).
                                Hệ thống đã tự động gắn cờ rủi ro rời bỏ và gửi thông báo cảnh báo tới nhân viên kinh doanh phụ trách
                                (<strong><c:out value="${banGhi.tenNguoiPhuTrach}" /></strong>). Cần chủ động phối hợp với đội Chăm sóc khách hàng để giải quyết dứt điểm!
                            </p>
                            <div class="ycht-risk-banner-meta">
                                <span class="ycht-risk-meta-item">
                                    <span class="material-symbols-outlined" aria-hidden="true">schedule</span>
                                    <span>Thời điểm gắn cờ: <strong><c:out value="${not empty thongTinRuiRo.ruiRoCapNhatLuc ? thongTinRuiRo.ruiRoCapNhatLuc : 'Gần đây'}" /></strong></span>
                                </span>
                                <span class="ycht-risk-meta-item">
                                    <span class="material-symbols-outlined" aria-hidden="true">person</span>
                                    <span>NVKD phụ trách: <strong><c:out value="${banGhi.tenNguoiPhuTrach}" /></strong></span>
                                </span>
                                <span class="ycht-risk-meta-item">
                                    <span class="material-symbols-outlined" aria-hidden="true">groups</span>
                                    <span>Nhóm: <strong><c:out value="${banGhi.tenNhom}" /></strong></span>
                                </span>
                            </div>
                        </div>
                        <div>
                            <button type="button" class="ycht-btn ycht-btn-danger" onclick="moModalGhiNhanYeuCau()" id="btnXuLyRuiRo">
                                <span class="material-symbols-outlined" aria-hidden="true">add_task</span>
                                <span>Ghi nhận hỗ trợ</span>
                            </button>
                        </div>
                    </section>
                </c:when>
                <c:otherwise>
                    <section class="ycht-safe-banner" id="bannerTrangThaiAnToan">
                        <span class="material-symbols-outlined" aria-hidden="true">verified_user</span>
                        <div>
                            Trạng thái chăm sóc: <strong>ỔN ĐỊNH</strong> &bull; Hiện có <strong><c:out value="${not empty thongTinRuiRo ? thongTinRuiRo.soYeuCauChuaXuLy : 0}" /></strong>/<c:out value="${not empty thongTinRuiRo ? thongTinRuiRo.nguongRuiRo : 3}" /> yêu cầu hỗ trợ chưa xử lý (dưới ngưỡng rủi ro rời bỏ).
                        </div>
                    </section>
                </c:otherwise>
            </c:choose>

            <!-- 4. Thẻ thông tin 360 độ Khách hàng -->
            <section class="ycht-table-card">
                <div class="ycht-table-header" style="padding: 20px 24px;">
                    <div>
                        <h1 style="font-size: 20px; font-weight: 800; color: var(--ycht-slate-900); margin: 0 0 4px 0; display: flex; align-items: center; gap: 10px;">
                            <span class="material-symbols-outlined" aria-hidden="true" style="color: var(--ycht-primary);">business</span>
                            <span><c:out value="${banGhi.tieuDe}" /></span>
                        </h1>
                        <p style="font-size: 13px; color: var(--ycht-slate-500); margin: 0;">
                            Mã khách hàng: <span class="ycht-ticket-code" style="font-size: 12px;"><c:out value="${banGhi.maBanGhi}" /></span> • Trang hồ sơ 360 độ khách hàng
                        </p>
                    </div>
                    <div style="display: flex; align-items: center; gap: 10px; flex-wrap: wrap;">
                        <c:choose>
                            <c:when test="${banGhi.coRuiRo or (not empty thongTinRuiRo and thongTinRuiRo.coRuiRo)}">
                                <span class="ycht-risk-flag-badge">
                                    <span class="material-symbols-outlined" aria-hidden="true">flag</span>
                                    <span>RỦI RO RỜI BỎ</span>
                                </span>
                            </c:when>
                            <c:otherwise>
                                <span class="ycht-badge ycht-status-da-xu-ly" style="padding: 5px 12px; font-size: 12.5px;">
                                    <span class="material-symbols-outlined" style="font-size: 15px;" aria-hidden="true">check_circle</span>
                                    <span>Hoạt động tốt</span>
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div style="padding: 24px;">
                    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 20px; margin-bottom: 24px;">
                        <div class="ycht-form-field">
                            <span class="ycht-form-label" style="font-size: 11.5px; text-transform: uppercase;">Mã khách hàng</span>
                            <span style="font-family: monospace; font-size: 14.5px; font-weight: 600; color: var(--ycht-slate-800);"><c:out value="${banGhi.maBanGhi}" /></span>
                        </div>

                        <div class="ycht-form-field">
                            <span class="ycht-form-label" style="font-size: 11.5px; text-transform: uppercase;">Tên khách hàng / Công ty</span>
                            <span style="font-size: 15px; font-weight: 700; color: var(--ycht-slate-900);"><c:out value="${banGhi.tieuDe}" /></span>
                        </div>

                        <div class="ycht-form-field">
                            <span class="ycht-form-label" style="font-size: 11.5px; text-transform: uppercase;">Người sở hữu / Phụ trách</span>
                            <span style="font-size: 14.5px; color: var(--ycht-slate-800); font-weight: 600;"><c:out value="${banGhi.tenNguoiPhuTrach}" /></span>
                        </div>

                        <div class="ycht-form-field">
                            <span class="ycht-form-label" style="font-size: 11.5px; text-transform: uppercase;">Nhóm kinh doanh</span>
                            <span style="font-size: 14.5px; color: var(--ycht-slate-800);"><c:out value="${banGhi.tenNhom}" /></span>
                        </div>

                        <div class="ycht-form-field">
                            <span class="ycht-form-label" style="font-size: 11.5px; text-transform: uppercase;">Doanh thu / Phân loại</span>
                            <span style="font-size: 14.5px; color: var(--ycht-slate-800);"><c:out value="${not empty banGhi.giaTri ? banGhi.giaTri : '-'}" /></span>
                        </div>

                        <div class="ycht-form-field">
                            <span class="ycht-form-label" style="font-size: 11.5px; text-transform: uppercase;">Trạng thái quan hệ</span>
                            <span style="font-size: 14.5px; color: var(--ycht-slate-800);"><c:out value="${banGhi.trangThai}" /></span>
                        </div>

                        <div class="ycht-form-field">
                            <span class="ycht-form-label" style="font-size: 11.5px; text-transform: uppercase;">Ngày tạo hồ sơ</span>
                            <span style="font-size: 14px; color: var(--ycht-slate-600);"><c:out value="${not empty banGhi.ngayTao ? banGhi.ngayTao : '-'}" /></span>
                        </div>

                        <div class="ycht-form-field">
                            <span class="ycht-form-label" style="font-size: 11.5px; text-transform: uppercase;">Cờ rủi ro rời bỏ</span>
                            <span>
                                <c:choose>
                                    <c:when test="${banGhi.coRuiRo or (not empty thongTinRuiRo and thongTinRuiRo.coRuiRo)}">
                                        <strong style="color: var(--ycht-red-dark);">Đang gắn cờ rủi ro rời bỏ</strong>
                                    </c:when>
                                    <c:otherwise>
                                        <span style="color: var(--ycht-green);">Bình thường / Chưa phát hiện rủi ro</span>
                                    </c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                    </div>

                    <div class="ycht-form-field">
                        <span class="ycht-form-label" style="font-size: 11.5px; text-transform: uppercase;">Mô tả / Ghi chú chăm sóc</span>
                        <div style="background-color: var(--ycht-slate-50); border: 1px solid var(--ycht-border-color); border-radius: var(--ycht-radius-md); padding: 14px 18px; font-size: 13.5px; color: var(--ycht-slate-700); line-height: 1.6; min-height: 60px;">
                            <c:choose>
                                <c:when test="${not empty banGhi.moTaChiTiet}">
                                    <c:out value="${banGhi.moTaChiTiet}" />
                                </c:when>
                                <c:otherwise>
                                    <span style="color: var(--ycht-slate-400); font-style: italic;">Chưa có mô tả chi tiết cho bản ghi này.</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </section>

            <!-- 5. STORY S3-08 AC1 & AC3: BẢNG YÊU CẦU HỖ TRỢ SAU BÁN TRÊN TRANG 360 -->
            <section class="ycht-table-card" id="khuVucYeuCauHoTro">
                <div class="ycht-table-header" style="padding: 18px 24px; background-color: var(--ycht-slate-50);">
                    <div>
                        <div class="ycht-table-title" style="font-size: 16px;">
                            <span class="material-symbols-outlined" style="color: var(--ycht-primary);" aria-hidden="true">support_agent</span>
                            <span>Yêu Cầu Hỗ Trợ Sau Bán & Giữ Chân Khách Hàng</span>
                            <span class="ycht-badge ycht-badge-moi" style="background: var(--ycht-slate-200); color: var(--ycht-slate-700); font-size: 11.5px; margin-left: 8px;">
                                Tổng số: <c:out value="${not empty danhSachYeuCauHoTro ? danhSachYeuCauHoTro.size() : 0}" />
                            </span>
                            <c:if test="${(not empty thongTinRuiRo and thongTinRuiRo.soYeuCauChuaXuLy > 0) or banGhi.soYeuCauChuaXuLy > 0}">
                                <span class="ycht-badge ycht-priority-cao" style="font-size: 11.5px; margin-left: 4px;">
                                    Chưa xử lý: <c:out value="${not empty thongTinRuiRo ? thongTinRuiRo.soYeuCauChuaXuLy : banGhi.soYeuCauChuaXuLy}" />
                                </span>
                            </c:if>
                        </div>
                        <p style="margin: 4px 0 0; font-size: 13px; color: var(--ycht-slate-500);">
                            Ghi nhận sự cố, theo dõi xử lý kỹ thuật và ngăn chặn nguy cơ khách hàng rời bỏ.
                        </p>
                    </div>
                    <div style="display: flex; gap: 10px; flex-wrap: wrap;">
                        <a href="${pageContext.request.contextPath}/yeu-cau-ho-tro?khachHangId=${banGhi.id}" class="ycht-btn ycht-btn-outline ycht-btn-sm" id="btnXemTatCaYeuCau">
                            <span class="material-symbols-outlined" aria-hidden="true">list_alt</span>
                            <span>Xem tất cả yêu cầu</span>
                        </a>
                        <button type="button" class="ycht-btn ycht-btn-primary ycht-btn-sm" onclick="moModalGhiNhanYeuCau()" id="btnGhiNhanYeuCauMoi">
                            <span class="material-symbols-outlined" aria-hidden="true">add</span>
                            <span>Ghi nhận yêu cầu hỗ trợ</span>
                        </button>
                    </div>
                </div>

                <div class="ycht-table-responsive">
                    <table class="ycht-data-table" id="bangYeuCauHoTroTrang360">
                        <thead>
                            <tr>
                                <th style="width: 130px;">Mã Ticket</th>
                                <th style="min-width: 250px;">Tiêu Đề / Nội Dung Yêu Cầu</th>
                                <th style="width: 140px; text-align: center;">Mức Độ Ưu Tiên</th>
                                <th style="width: 170px;">Người Xử Lý</th>
                                <th style="width: 140px; text-align: center;">Trạng Thái</th>
                                <th style="width: 140px;">Thời Gian Tạo</th>
                                <th style="width: 150px; text-align: center;">Cập Nhật Nhanh</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty danhSachYeuCauHoTro}">
                                    <c:forEach var="yc" items="${danhSachYeuCauHoTro}">
                                        <tr>
                                            <td>
                                                <span class="ycht-ticket-code"><c:out value="${yc.maYeuCau}" /></span>
                                            </td>
                                            <td>
                                                <div class="ycht-ticket-title"><c:out value="${yc.tieuDe}" /></div>
                                                <c:if test="${not empty yc.noiDung}">
                                                    <div class="ycht-ticket-desc" title="<c:out value="${yc.noiDung}" />"><c:out value="${yc.noiDung}" /></div>
                                                </c:if>
                                                <c:if test="${not empty yc.tenNguoiLienHe}">
                                                    <div class="ycht-ticket-contact">
                                                        <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">contact_mail</span>
                                                        <span>Liên hệ: <c:out value="${yc.tenNguoiLienHe}" /></span>
                                                        <c:if test="${not empty yc.sdtNguoiLienHe}">
                                                            <span>• <c:out value="${yc.sdtNguoiLienHe}" /></span>
                                                        </c:if>
                                                    </div>
                                                </c:if>
                                            </td>
                                            <td style="text-align: center;">
                                                <c:choose>
                                                    <c:when test="${yc.mucUuTien == 'KHAN_CAP'}">
                                                        <span class="ycht-badge ycht-priority-khan-cap">
                                                            <span class="material-symbols-outlined" style="font-size: 13px;" aria-hidden="true">warning</span>
                                                            <c:out value="${yc.mucUuTienEnum.tenHienThi}" />
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${yc.mucUuTien == 'CAO'}">
                                                        <span class="ycht-badge ycht-priority-cao">
                                                            <c:out value="${yc.mucUuTienEnum.tenHienThi}" />
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${yc.mucUuTien == 'BINH_THUONG'}">
                                                        <span class="ycht-badge ycht-priority-binh-thuong">
                                                            <c:out value="${yc.mucUuTienEnum.tenHienThi}" />
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="ycht-badge ycht-priority-thap">
                                                            <c:out value="${yc.mucUuTienEnum.tenHienThi}" />
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <div class="ycht-user-cell">
                                                    <div class="ycht-user-avatar-sm" aria-hidden="true">
                                                        <c:choose>
                                                            <c:when test="${not empty yc.tenNguoiXuLy}">
                                                                <c:out value="${yc.tenNguoiXuLy.substring(0, 1)}" />
                                                            </c:when>
                                                            <c:otherwise>?</c:otherwise>
                                                        </c:choose>
                                                    </div>
                                                    <div class="ycht-user-info-text">
                                                        <div class="ycht-user-name">
                                                            <c:out value="${not empty yc.tenNguoiXuLy ? yc.tenNguoiXuLy : 'Chưa phân công'}" />
                                                        </div>
                                                        <c:if test="${not empty yc.emailNguoiXuLy}">
                                                            <div class="ycht-user-email"><c:out value="${yc.emailNguoiXuLy}" /></div>
                                                        </c:if>
                                                    </div>
                                                </div>
                                            </td>
                                            <td style="text-align: center;">
                                                <c:choose>
                                                    <c:when test="${yc.trangThai == 'MOI'}">
                                                        <span class="ycht-badge ycht-status-moi"><c:out value="${yc.trangThaiEnum.tenHienThi}" /></span>
                                                    </c:when>
                                                    <c:when test="${yc.trangThai == 'DANG_XU_LY'}">
                                                        <span class="ycht-badge ycht-status-dang-xu-ly"><c:out value="${yc.trangThaiEnum.tenHienThi}" /></span>
                                                    </c:when>
                                                    <c:when test="${yc.trangThai == 'CHO_KHACH_HANG'}">
                                                        <span class="ycht-badge ycht-status-cho-khach-hang"><c:out value="${yc.trangThaiEnum.tenHienThi}" /></span>
                                                    </c:when>
                                                    <c:when test="${yc.trangThai == 'DA_XU_LY'}">
                                                        <span class="ycht-badge ycht-status-da-xu-ly">
                                                            <span class="material-symbols-outlined" style="font-size: 13px;" aria-hidden="true">check</span>
                                                            <c:out value="${yc.trangThaiEnum.tenHienThi}" />
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${yc.trangThai == 'DONG'}">
                                                        <span class="ycht-badge ycht-status-dong"><c:out value="${yc.trangThaiEnum.tenHienThi}" /></span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="ycht-badge ycht-status-huy"><c:out value="${yc.trangThaiEnum.tenHienThi}" /></span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td style="font-size: 12.5px; color: var(--ycht-slate-600); white-space: nowrap;">
                                                <c:out value="${yc.taoLuc}" />
                                            </td>
                                            <td style="text-align: center;">
                                                <!-- Form cập nhật trạng thái nhanh kích hoạt tự động đánh giá cờ rủi ro -->
                                                <form action="${pageContext.request.contextPath}/yeu-cau-ho-tro" method="POST" class="ycht-inline-status-form">
                                                    <input type="hidden" name="action" value="cap-nhat-trang-thai">
                                                    <input type="hidden" name="id" value="${yc.id}">
                                                    <input type="hidden" name="redirectUrl" value="${pageContext.request.contextPath}/chi-tiet-ban-ghi?id=${banGhi.id}">
                                                    <select name="trangThai" class="ycht-inline-status-select" onchange="this.form.submit()" aria-label="Cập nhật trạng thái yêu cầu">
                                                        <option value="MOI" ${yc.trangThai == 'MOI' ? 'selected' : ''}>Mới tiếp nhận</option>
                                                        <option value="DANG_XU_LY" ${yc.trangThai == 'DANG_XU_LY' ? 'selected' : ''}>Đang xử lý</option>
                                                        <option value="CHO_KHACH_HANG" ${yc.trangThai == 'CHO_KHACH_HANG' ? 'selected' : ''}>Chờ khách</option>
                                                        <option value="DA_XU_LY" ${yc.trangThai == 'DA_XU_LY' ? 'selected' : ''}>Đã xử lý</option>
                                                        <option value="DONG" ${yc.trangThai == 'DONG' ? 'selected' : ''}>Đã đóng</option>
                                                        <option value="HUY" ${yc.trangThai == 'HUY' ? 'selected' : ''}>Đã hủy</option>
                                                    </select>
                                                </form>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="7">
                                            <div class="ycht-empty-state" style="padding: 36px 20px;">
                                                <span class="material-symbols-outlined ycht-empty-icon" aria-hidden="true">sentiment_satisfied</span>
                                                <div class="ycht-empty-title">Chưa có yêu cầu hỗ trợ nào phát sinh</div>
                                                <p class="ycht-empty-desc">
                                                    Khách hàng hiện đang hoạt động bình thường và chưa có ticket hỗ trợ nào cần xử lý.
                                                </p>
                                                <div style="margin-top: 14px;">
                                                    <button type="button" class="ycht-btn ycht-btn-primary ycht-btn-sm" onclick="moModalGhiNhanYeuCau()">
                                                        <span class="material-symbols-outlined" aria-hidden="true">add</span>
                                                        <span>Ghi nhận yêu cầu hỗ trợ mới</span>
                                                    </button>
                                                </div>
                                            </div>
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </section>

        </div>

        <!-- 6. MODAL GHI NHẬN YÊU CẦU HỖ TRỢ CHO KHÁCH HÀNG HIỆN TẠI (STORY S3-08 AC1) -->
        <div class="ycht-modal-backdrop" id="modalGhiNhanYeuCau" role="dialog" aria-modal="true" aria-labelledby="modalGhiNhanYeuCauTieuDe" aria-hidden="true">
            <div class="ycht-modal-card">
                <div class="ycht-modal-header">
                    <div>
                        <h2 class="ycht-modal-title" id="modalGhiNhanYeuCauTieuDe">Ghi Nhận Yêu Cầu Hỗ Trợ Sau Bán</h2>
                        <p class="ycht-modal-subtitle">Ghi nhận thông tin sự cố, thắc mắc và người xử lý cho khách hàng</p>
                    </div>
                    <button type="button" class="ycht-modal-close-btn" onclick="dongModalGhiNhanYeuCau()" aria-label="Đóng cửa sổ">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>

                <form action="${pageContext.request.contextPath}/yeu-cau-ho-tro" method="POST" id="formGhiNhanYeuCau">
                    <input type="hidden" name="action" value="tao">
                    <input type="hidden" name="khachHangId" value="${banGhi.id}">
                    <input type="hidden" name="redirectUrl" value="${pageContext.request.contextPath}/chi-tiet-ban-ghi?id=${banGhi.id}">

                    <div class="ycht-modal-body">
                        <div class="ycht-form-field">
                            <label class="ycht-form-label">Khách hàng / Công ty</label>
                            <input type="text" class="ycht-form-input" value="${banGhi.tieuDe}" readonly style="background-color: var(--ycht-slate-100); color: var(--ycht-slate-700); font-weight: 600;">
                        </div>

                        <div class="ycht-form-field">
                            <label class="ycht-form-label" for="tieuDeYeuCau">
                                Tiêu đề yêu cầu hỗ trợ <span class="ycht-form-required">*</span>
                            </label>
                            <input type="text" id="tieuDeYeuCau" name="tieuDe" class="ycht-form-input" placeholder="Ví dụ: Lỗi đồng bộ dữ liệu hoá đơn điện tử..." required autocomplete="off" minlength="3">
                        </div>

                        <div class="ycht-form-row-2">
                            <div class="ycht-form-field">
                                <label class="ycht-form-label" for="mucUuTien">
                                    Mức độ ưu tiên <span class="ycht-form-required">*</span>
                                </label>
                                <select id="mucUuTien" name="mucUuTien" class="ycht-form-select" required>
                                    <option value="BINH_THUONG" selected>Bình thường</option>
                                    <option value="THAP">Thấp</option>
                                    <option value="CAO">Cao</option>
                                    <option value="KHAN_CAP">Khẩn cấp (Sự cố nghiêm trọng)</option>
                                </select>
                            </div>
                            <div class="ycht-form-field">
                                <label class="ycht-form-label" for="trangThaiYeuCau">
                                    Trạng thái ban đầu <span class="ycht-form-required">*</span>
                                </label>
                                <select id="trangThaiYeuCau" name="trangThai" class="ycht-form-select" required>
                                    <option value="MOI" selected>Mới tiếp nhận</option>
                                    <option value="DANG_XU_LY">Đang xử lý</option>
                                    <option value="CHO_KHACH_HANG">Chờ khách hàng phản hồi</option>
                                </select>
                            </div>
                        </div>

                        <div class="ycht-form-field">
                            <label class="ycht-form-label" for="nguoiXuLyId">Người xử lý (CSKH / Kỹ thuật)</label>
                            <select id="nguoiXuLyId" name="nguoiXuLyId" class="ycht-form-select">
                                <option value="">-- Chưa phân công --</option>
                                <c:forEach var="nv" items="${danhSachNhanVien}">
                                    <option value="${nv.id}">
                                        <c:out value="${nv.hoTen}" /> (<c:out value="${nv.email}" />)
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="ycht-form-field">
                            <label class="ycht-form-label" for="noiDungYeuCau">Nội dung chi tiết / Phản hồi từ khách</label>
                            <textarea id="noiDungYeuCau" name="noiDung" class="ycht-form-textarea" rows="3" placeholder="Mô tả chi tiết sự cố, yêu cầu cần xử lý..."></textarea>
                        </div>
                    </div>

                    <div class="ycht-modal-footer">
                        <button type="button" class="ycht-btn ycht-btn-outline" onclick="dongModalGhiNhanYeuCau()">Hủy bỏ</button>
                        <button type="submit" class="ycht-btn ycht-btn-primary" id="btnSubmitGhiNhanYeuCau">
                            <span class="material-symbols-outlined" aria-hidden="true">save</span>
                            <span>Lưu Yêu Cầu Hỗ Trợ</span>
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </main>

    <!-- Script chuẩn hệ thống -->
    <script src="${pageContext.request.contextPath}/assets/js/navigation.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/yeu-cau-ho-tro.js"></script>
</body>
</html>
