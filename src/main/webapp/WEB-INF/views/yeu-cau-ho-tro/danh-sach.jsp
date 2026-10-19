<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Quản lý yêu cầu hỗ trợ sau bán và phát hiện khách hàng có rủi ro rời bỏ - CRM Bán Hàng">
    <title>Yêu Cầu Hỗ Trợ Sau Bán & Gắn Cờ Rủi Ro - CRM Bán Hàng</title>
    <!-- CSS dùng chung toàn hệ thống -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <!-- CSS chuyên biệt module Yêu cầu hỗ trợ (Story S3-08) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/khach-hang/yeu-cau-ho-tro.css">
</head>
<body class="crm-body">
    <!-- Navigation layout dùng chung S1-06 (đã bao gồm Material Symbols Outlined) -->
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
                    <li class="ycht-breadcrumb-current" aria-current="page">Yêu cầu hỗ trợ sau bán</li>
                </ol>
                <div>
                    <a href="${pageContext.request.contextPath}/khach-hang" class="ycht-btn ycht-btn-outline ycht-btn-sm" id="btnQuayLaiKhachHang">
                        <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span>
                        <span>Danh sách khách hàng</span>
                    </a>
                </div>
            </nav>

            <!-- 2. Header & Nút hành động chính -->
            <header class="ycht-header-card">
                <div class="ycht-header-title-group">
                    <h1>
                        <span class="material-symbols-outlined" style="color: var(--ycht-primary);" aria-hidden="true">support_agent</span>
                        <span>Yêu Cầu Hỗ Trợ Sau Bán & Gắn Cờ Rủi Ro</span>
                    </h1>
                    <p>
                        Ghi nhận sự cố kỹ thuật, theo dõi mức độ ưu tiên, người xử lý và tự động gắn cờ khách có rủi ro rời bỏ (Story S3-08).
                    </p>
                </div>
                <div class="ycht-header-actions">
                    <button type="button" class="ycht-btn ycht-btn-primary" onclick="moModalGhiNhanYeuCau()" id="btnThemYeuCau">
                        <span class="material-symbols-outlined" aria-hidden="true">add</span>
                        <span>Ghi nhận yêu cầu hỗ trợ</span>
                    </button>
                </div>
            </header>

            <!-- 3. Thông báo hệ thống (Success / Error) -->
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

            <!-- 4. Thống kê nhanh KPI -->
            <c:set var="demTongSo" value="${not empty danhSachYeuCau ? danhSachYeuCau.size() : 0}" />
            <c:set var="demDangXuLy" value="0" />
            <c:set var="demDaXong" value="0" />
            <c:forEach var="item" items="${danhSachYeuCau}">
                <c:if test="${item.chuaXuLy}">
                    <c:set var="demDangXuLy" value="${demDangXuLy + 1}" />
                </c:if>
                <c:if test="${not item.chuaXuLy}">
                    <c:set var="demDaXong" value="${demDaXong + 1}" />
                </c:if>
            </c:forEach>
            <c:set var="demKhachCoRuiRo" value="0" />
            <c:forEach var="khItem" items="${danhSachKhachHang}">
                <c:if test="${khItem.coRuiRo}">
                    <c:set var="demKhachCoRuiRo" value="${demKhachCoRuiRo + 1}" />
                </c:if>
            </c:forEach>

            <section class="ycht-kpi-grid" aria-label="Thống kê nhanh yêu cầu hỗ trợ">
                <div class="ycht-kpi-card">
                    <div class="ycht-kpi-icon-wrap primary">
                        <span class="material-symbols-outlined" aria-hidden="true">confirmation_number</span>
                    </div>
                    <div class="ycht-kpi-meta">
                        <div class="ycht-kpi-label">Tổng số yêu cầu</div>
                        <div class="ycht-kpi-value"><c:out value="${demTongSo}" /></div>
                        <div class="ycht-kpi-sub">Trong bộ lọc hiện tại</div>
                    </div>
                </div>

                <div class="ycht-kpi-card">
                    <div class="ycht-kpi-icon-wrap amber">
                        <span class="material-symbols-outlined" aria-hidden="true">pending_actions</span>
                    </div>
                    <div class="ycht-kpi-meta">
                        <div class="ycht-kpi-label">Chưa xử lý</div>
                        <div class="ycht-kpi-value" style="color: var(--ycht-amber-dark);"><c:out value="${demDangXuLy}" /></div>
                        <div class="ycht-kpi-sub">Cần ưu tiên xử lý sớm</div>
                    </div>
                </div>

                <div class="ycht-kpi-card">
                    <div class="ycht-kpi-icon-wrap green">
                        <span class="material-symbols-outlined" aria-hidden="true">task_alt</span>
                    </div>
                    <div class="ycht-kpi-meta">
                        <div class="ycht-kpi-label">Đã hoàn tất / Đóng</div>
                        <div class="ycht-kpi-value" style="color: var(--ycht-green);"><c:out value="${demDaXong}" /></div>
                        <div class="ycht-kpi-sub">Đã phản hồi thỏa đáng</div>
                    </div>
                </div>

                <div class="ycht-kpi-card">
                    <div class="ycht-kpi-icon-wrap red">
                        <span class="material-symbols-outlined" aria-hidden="true">warning</span>
                    </div>
                    <div class="ycht-kpi-meta">
                        <div class="ycht-kpi-label">Khách cờ rủi ro</div>
                        <div class="ycht-kpi-value" style="color: var(--ycht-red-dark);"><c:out value="${demKhachCoRuiRo}" /></div>
                        <div class="ycht-kpi-sub">Nguy cơ rời bỏ cao (Churn)</div>
                    </div>
                </div>
            </section>

            <!-- 5. Thanh lọc & Tìm kiếm dữ liệu thật -->
            <section class="ycht-filter-card">
                <form action="${pageContext.request.contextPath}/yeu-cau-ho-tro" method="GET" class="ycht-filter-grid" id="formBoLocYeuCau">
                    <div class="ycht-form-field">
                        <label class="ycht-form-label" for="filterKhachHang">Khách hàng / Doanh nghiệp</label>
                        <select id="filterKhachHang" name="khachHangId" class="ycht-form-select">
                            <option value="">-- Tất cả khách hàng --</option>
                            <c:forEach var="kh" items="${danhSachKhachHang}">
                                <option value="${kh.id}" ${kh.id == khachHangIdChon ? 'selected' : ''}>
                                    <c:out value="${kh.tenCongTy}" />
                                    <c:if test="${kh.coRuiRo}"> [CỜ RỦI RO]</c:if>
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="ycht-form-field">
                        <label class="ycht-form-label" for="filterTrangThai">Trạng thái xử lý</label>
                        <select id="filterTrangThai" name="trangThai" class="ycht-form-select">
                            <option value="TAT_CA">-- Tất cả trạng thái --</option>
                            <c:forEach var="tt" items="${trangThaiList}">
                                <option value="${tt.ma}" ${tt.ma == trangThaiChon ? 'selected' : ''}>
                                    <c:out value="${tt.tenHienThi}" />
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="ycht-form-field">
                        <label class="ycht-form-label" for="filterMucUuTien">Mức độ ưu tiên</label>
                        <select id="filterMucUuTien" name="mucUuTien" class="ycht-form-select">
                            <option value="TAT_CA">-- Tất cả mức độ --</option>
                            <c:forEach var="ut" items="${mucUuTienList}">
                                <option value="${ut.ma}" ${ut.ma == mucUuTienChon ? 'selected' : ''}>
                                    <c:out value="${ut.tenHienThi}" />
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="ycht-form-field">
                        <label class="ycht-form-label" for="filterTuKhoa">Từ khóa tìm kiếm</label>
                        <input type="text" id="filterTuKhoa" name="tuKhoa" class="ycht-form-input" placeholder="Mã ticket, tiêu đề yêu cầu..." value="${tuKhoa}">
                    </div>

                    <div class="ycht-filter-actions">
                        <button type="submit" class="ycht-btn ycht-btn-primary" id="btnApDungLoc">
                            <span class="material-symbols-outlined" aria-hidden="true">search</span>
                            <span>Lọc dữ liệu</span>
                        </button>
                        <a href="${pageContext.request.contextPath}/yeu-cau-ho-tro" class="ycht-btn ycht-btn-outline" title="Đặt lại bộ lọc" id="btnDatLaiLoc">
                            <span class="material-symbols-outlined" aria-hidden="true">refresh</span>
                            <span>Đặt lại</span>
                        </a>
                    </div>
                </form>
            </section>

            <!-- 6. Bảng danh sách yêu cầu hỗ trợ sau bán -->
            <section class="ycht-table-card">
                <div class="ycht-table-header">
                    <div class="ycht-table-title">
                        <span class="material-symbols-outlined" style="color: var(--ycht-primary);" aria-hidden="true">view_list</span>
                        <span>Danh Sách Ticket Hỗ Trợ Sau Bán</span>
                        <span class="ycht-badge ycht-badge-moi" style="background: var(--ycht-slate-100); color: var(--ycht-slate-600); margin-left: 6px;">
                            ${demTongSo} bản ghi
                        </span>
                    </div>
                </div>

                <div class="ycht-table-responsive">
                    <table class="ycht-data-table" id="bangYeuCauHoTro">
                        <thead>
                            <tr>
                                <th style="width: 130px;">Mã Ticket</th>
                                <th style="min-width: 220px;">Khách Hàng / Công Ty</th>
                                <th style="min-width: 260px;">Tiêu Đề Yêu Cầu</th>
                                <th style="width: 140px; text-align: center;">Ưu Tiên</th>
                                <th style="width: 170px;">Người Xử Lý</th>
                                <th style="width: 140px; text-align: center;">Trạng Thái</th>
                                <th style="width: 140px;">Ngày Tiếp Nhận</th>
                                <th style="width: 150px; text-align: center;">Đổi Trạng Thái</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty danhSachYeuCau}">
                                    <c:forEach var="yc" items="${danhSachYeuCau}">
                                        <tr>
                                            <td>
                                                <span class="ycht-ticket-code"><c:out value="${yc.maYeuCau}" /></span>
                                            </td>
                                            <td>
                                                <div>
                                                    <a href="${pageContext.request.contextPath}/chi-tiet-ban-ghi?id=${yc.khachHangId}" class="ycht-company-link" title="Xem hồ sơ 360 độ khách hàng">
                                                        <span><c:out value="${yc.tenKhachHang}" /></span>
                                                    </a>
                                                </div>
                                            </td>
                                            <td>
                                                <div class="ycht-ticket-title"><c:out value="${yc.tieuDe}" /></div>
                                                <c:if test="${not empty yc.noiDung}">
                                                    <div class="ycht-ticket-desc" title="<c:out value="${yc.noiDung}" />"><c:out value="${yc.noiDung}" /></div>
                                                </c:if>
                                                <c:if test="${not empty yc.tenNguoiLienHe}">
                                                    <div class="ycht-ticket-contact">
                                                        <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">person</span>
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
                                                <form action="${pageContext.request.contextPath}/yeu-cau-ho-tro" method="POST" class="ycht-inline-status-form">
                                                    <input type="hidden" name="action" value="cap-nhat-trang-thai">
                                                    <input type="hidden" name="id" value="${yc.id}">
                                                    <input type="hidden" name="redirectUrl" value="${pageContext.request.contextPath}/yeu-cau-ho-tro">
                                                    <select name="trangThai" class="ycht-inline-status-select" onchange="this.form.submit()" aria-label="Thay đổi trạng thái yêu cầu">
                                                        <option value="MOI" ${yc.trangThai == 'MOI' ? 'selected' : ''}>Mới tiếp nhận</option>
                                                        <option value="DANG_XU_LY" ${yc.trangThai == 'DANG_XU_LY' ? 'selected' : ''}>Đang xử lý</option>
                                                        <option value="CHO_KHACH_HANG" ${yc.trangThai == 'CHO_KHACH_HANG' ? 'selected' : ''}>Chờ khách hàng</option>
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
                                        <td colspan="8">
                                            <div class="ycht-empty-state">
                                                <span class="material-symbols-outlined ycht-empty-icon" aria-hidden="true">inbox</span>
                                                <div class="ycht-empty-title">Không tìm thấy yêu cầu hỗ trợ nào</div>
                                                <p class="ycht-empty-desc">
                                                    Không có ticket hỗ trợ nào khớp với điều kiện lọc hiện tại. Bạn có thể thay đổi bộ lọc hoặc ghi nhận một yêu cầu hỗ trợ mới.
                                                </p>
                                                <div style="margin-top: 14px;">
                                                    <button type="button" class="ycht-btn ycht-btn-primary ycht-btn-sm" onclick="moModalGhiNhanYeuCau()">
                                                        <span class="material-symbols-outlined" aria-hidden="true">add</span>
                                                        <span>Ghi nhận yêu cầu hỗ trợ ngay</span>
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

        <!-- 7. Modal Ghi Nhận Yêu Cầu Hỗ Trợ Mới (Story S3-08 AC1) -->
        <div class="ycht-modal-backdrop" id="modalGhiNhanYeuCau" role="dialog" aria-modal="true" aria-labelledby="modalGhiNhanTieuDe" aria-hidden="true">
            <div class="ycht-modal-card">
                <div class="ycht-modal-header">
                    <div>
                        <h2 class="ycht-modal-title" id="modalGhiNhanTieuDe">Ghi Nhận Yêu Cầu Hỗ Trợ Sau Bán</h2>
                        <p class="ycht-modal-subtitle">Ghi nhận thông tin sự cố, mức độ ưu tiên và người xử lý</p>
                    </div>
                    <button type="button" class="ycht-modal-close-btn" onclick="dongModalGhiNhanYeuCau()" aria-label="Đóng cửa sổ">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>

                <form action="${pageContext.request.contextPath}/yeu-cau-ho-tro" method="POST" id="formGhiNhanYeuCau">
                    <input type="hidden" name="action" value="tao">
                    <input type="hidden" name="redirectUrl" value="${pageContext.request.contextPath}/yeu-cau-ho-tro">

                    <div class="ycht-modal-body">
                        <div class="ycht-form-field">
                            <label class="ycht-form-label" for="selectKhachHang">
                                Khách hàng / Công ty <span class="ycht-form-required">*</span>
                            </label>
                            <select id="selectKhachHang" name="khachHangId" class="ycht-form-select" required>
                                <option value="">-- Chọn khách hàng nhận hỗ trợ --</option>
                                <c:forEach var="kh" items="${danhSachKhachHang}">
                                    <option value="${kh.id}">
                                        <c:out value="${kh.tenCongTy}" /> <c:if test="${not empty kh.maKhachHang}">(${kh.maKhachHang})</c:if>
                                        <c:if test="${kh.coRuiRo}"> [CỜ RỦI RO]</c:if>
                                    </option>
                                </c:forEach>
                            </select>
                            <span class="ycht-form-help">Khách hàng có nhiều yêu cầu chưa xử lý sẽ được hệ thống gắn cờ rủi ro tự động.</span>
                        </div>

                        <div class="ycht-form-field">
                            <label class="ycht-form-label" for="tieuDeYeuCau">
                                Tiêu đề yêu cầu hỗ trợ <span class="ycht-form-required">*</span>
                            </label>
                            <input type="text" id="tieuDeYeuCau" name="tieuDe" class="ycht-form-input" placeholder="Ví dụ: Lỗi đồng bộ tài khoản, chậm xử lý đơn hàng..." required autocomplete="off" minlength="3">
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
                                <label class="ycht-form-label" for="trangThaiBanDau">
                                    Trạng thái ban đầu <span class="ycht-form-required">*</span>
                                </label>
                                <select id="trangThaiBanDau" name="trangThai" class="ycht-form-select" required>
                                    <option value="MOI" selected>Mới tiếp nhận</option>
                                    <option value="DANG_XU_LY">Đang xử lý</option>
                                    <option value="CHO_KHACH_HANG">Chờ khách hàng phản hồi</option>
                                </select>
                            </div>
                        </div>

                        <div class="ycht-form-field">
                            <label class="ycht-form-label" for="nguoiXuLyId">Người xử lý (CSKH / Kỹ thuật)</label>
                            <select id="nguoiXuLyId" name="nguoiXuLyId" class="ycht-form-select">
                                <option value="">-- Chưa phân công (Tiếp nhận sau) --</option>
                                <c:forEach var="nv" items="${danhSachNhanVien}">
                                    <option value="${nv.id}">
                                        <c:out value="${nv.hoTen}" /> (<c:out value="${nv.email}" />)
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="ycht-form-field">
                            <label class="ycht-form-label" for="noiDungYeuCau">Nội dung chi tiết sự cố / yêu cầu</label>
                            <textarea id="noiDungYeuCau" name="noiDung" class="ycht-form-textarea" rows="3" placeholder="Mô tả cụ thể sự cố phát sinh, thông tin log hoặc phản ánh của khách hàng..."></textarea>
                        </div>
                    </div>

                    <div class="ycht-modal-footer">
                        <button type="button" class="ycht-btn ycht-btn-outline" onclick="dongModalGhiNhanYeuCau()">Hủy</button>
                        <button type="submit" class="ycht-btn ycht-btn-primary" id="btnLuuYeuCau">
                            <span class="material-symbols-outlined" aria-hidden="true">save</span>
                            <span>Lưu Yêu Cầu Hỗ Trợ</span>
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </main>

    <!-- Script tương tác chuẩn hệ thống -->
    <script src="${pageContext.request.contextPath}/assets/js/navigation.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/yeu-cau-ho-tro.js"></script>
</body>
</html>
