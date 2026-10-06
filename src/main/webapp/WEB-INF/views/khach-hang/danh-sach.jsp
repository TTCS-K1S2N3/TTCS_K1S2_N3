<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Danh mục khách hàng & Chăm sóc định kỳ sau ký hợp đồng - Hệ thống CRM Bán Hàng">
    <title>Khách Hàng & Chăm Sóc Định Kỳ - CRM Bán Hàng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/khach-hang/khach-hang.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <script>
        window.CONTEXT_PATH = "${pageContext.request.contextPath}";
    </script>
</head>
<body class="crm-body">
    <!-- Thanh điều hướng và Sidebar chuẩn hệ thống S1-06 -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <!-- Thông báo kết quả thao tác -->
        <c:if test="${not empty thongBaoThanhCong}">
            <div class="alert alert-success" id="alertSuccess">
                <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                <span><c:out value="${thongBaoThanhCong}" /></span>
            </div>
        </c:if>
        <c:if test="${not empty thongBaoLoi}">
            <div class="alert alert-danger" id="alertError">
                <span class="material-symbols-outlined" aria-hidden="true">error</span>
                <span><c:out value="${thongBaoLoi}" /></span>
            </div>
        </c:if>

        <!-- Tiêu đề trang & Thanh công cụ chung -->
        <div class="page-header">
            <div>
                <h1 class="page-title">Khách Hàng & Chăm Sóc Định Kỳ</h1>
                <p class="page-subtitle">Quản lý danh mục khách hàng và theo dõi chăm sóc định kỳ cho khách đã ký hợp đồng</p>
            </div>
            <div class="page-actions">
                <button type="button" class="btn btn-outline" id="btnExportExcel" title="Tải danh sách khách hàng dưới dạng file Excel (.xlsx)">
                    <span class="material-symbols-outlined" aria-hidden="true">table_view</span>
                    <span>Xuất Excel</span>
                </button>
                <c:set var="userHienTai" value="${not empty nguoiDung ? nguoiDung : sessionScope.nguoiDung}" />
                <c:set var="coQuyenThemKhach" value="${empty userHienTai or userHienTai.coVaiTro('ADMIN') or userHienTai.coVaiTro('DIRECTOR') or userHienTai.coVaiTro('TEAM_LEAD') or userHienTai.coVaiTro('SALES_REP')}" />
                <c:if test="${coQuyenThemKhach}">
                    <button type="button" class="btn btn-primary" id="btnThemKhachHang">
                        <span class="material-symbols-outlined" aria-hidden="true">add</span>
                        <span>Thêm Khách Hàng</span>
                    </button>
                </c:if>
            </div>
        </div>

        <!-- Thanh điều hướng Tab chính -->
        <div class="crm-tabs-nav" role="tablist" aria-label="Phân hệ khách hàng">
            <button type="button" class="crm-tab-btn ${tabHienTai ne 'cham-soc' ? 'active' : ''}" id="tabBtnDanhSach" role="tab" aria-selected="${tabHienTai ne 'cham-soc' ? 'true' : 'false'}" aria-controls="paneDanhSach" data-tab="tat-ca">
                <span class="material-symbols-outlined" aria-hidden="true">group</span>
                <span>Tất cả khách hàng</span>
                <span class="crm-tab-badge"><c:out value="${not empty tongSoKhachHang ? tongSoKhachHang : 0}" /></span>
            </button>
            <button type="button" class="crm-tab-btn ${tabHienTai eq 'cham-soc' ? 'active' : ''}" id="tabBtnChamSoc" role="tab" aria-selected="${tabHienTai eq 'cham-soc' ? 'true' : 'false'}" aria-controls="paneChamSoc" data-tab="cham-soc">
                <span class="material-symbols-outlined" aria-hidden="true">support_agent</span>
                <span>Chăm sóc định kỳ</span>
                <span class="crm-tab-badge badge-amber" id="badgeSoKhachCanChamSoc">S3-09</span>
            </button>
        </div>

        <!-- ===================================================================
             TAB 1: TẤT CẢ KHÁCH HÀNG (Bao gồm S1-02 và S1-05)
             =================================================================== -->
        <div class="crm-tab-pane ${tabHienTai ne 'cham-soc' ? 'active' : ''}" id="paneDanhSach" role="tabpanel" aria-labelledby="tabBtnDanhSach">
            <!-- Story S1-02: Khu vực soạn thảo ghi chú cuộc gặp (Duy trì phiên & Tự động lưu) -->
            <div class="card" style="margin-bottom: 24px;">
                <div class="card-title">
                    <div class="card-title-left">
                        <span class="material-symbols-outlined" aria-hidden="true" style="color: var(--primary);">edit_note</span>
                        <span>Ghi chú cuộc gặp khách hàng (Duy trì phiên & Tự động lưu)</span>
                    </div>
                    <div style="display: flex; align-items: center; gap: 12px; flex-wrap: wrap;">
                        <div class="session-indicator" id="session-indicator-box" title="Trạng thái phiên đăng nhập bảo mật">
                            <span class="session-dot active" id="session-status-dot"></span>
                            <span id="session-status-text">Phiên hoạt động</span>
                            <span class="session-countdown" id="session-countdown-timer" title="Thời gian phiên còn lại">30:00</span>
                        </div>
                        <span class="badge-info">
                            <span class="material-symbols-outlined icon-xs" aria-hidden="true">check_circle</span>
                            Auto Keep-Alive Active
                        </span>
                    </div>
                </div>

                <p style="color: var(--slate-500); font-size: 13.5px; margin-bottom: 14px;">
                    Khi bạn đang ngồi tại quán cà phê soạn thảo ghi chú, hệ thống sẽ tự động gửi tín hiệu gia hạn phiên làm việc (AC1)
                    và liên tục lưu bản nháp dự phòng để tránh mất nội dung khi kết nối mạng chập chờn.
                </p>

                <!-- Banner cảnh báo phát hiện bản nháp chưa lưu -->
                <div class="draft-alert-banner" id="draft-alert-banner">
                    <div>
                        <strong><span class="material-symbols-outlined icon-sm" aria-hidden="true" style="vertical-align: text-bottom;">lightbulb</span> Phát hiện bản nháp trước đó:</strong>
                        <span>Bạn có một ghi chú chưa xóa được lưu lúc <span id="draft-saved-time-text">gần đây</span>.</span>
                    </div>
                    <div class="draft-alert-actions">
                        <button type="button" class="btn-draft-action btn-draft-restore" id="btn-restore-draft">Khôi phục ghi chú</button>
                        <button type="button" class="btn-draft-action btn-draft-discard" id="btn-discard-draft">Hủy bản nháp</button>
                    </div>
                </div>

                <div class="editor-wrapper">
                    <textarea id="ghi-chu-cuoc-gap" class="note-editor"
                              placeholder="Nhập nội dung trao đổi cuộc gặp với khách hàng tại đây (ví dụ: nhu cầu mua hàng, thời gian bàn giao dự kiến, phản hồi ngân sách, yêu cầu kỹ thuật)..."></textarea>
                </div>

                <div class="note-footer">
                    <div class="note-stats-group">
                        <span class="sync-status active" id="session-sync-status">
                            <span class="material-symbols-outlined icon-sm" aria-hidden="true">save</span>
                            Phiên làm việc sẵn sàng
                        </span>
                        <span class="note-stat-item" id="note-last-saved" style="color: var(--slate-400);"></span>
                        <span class="note-stat-item" id="note-word-count" style="color: var(--slate-600); font-weight: 600;">0 từ</span>
                        <span class="note-stat-item" id="note-char-count" style="color: var(--slate-400);">0 ký tự</span>
                    </div>
                    <div class="note-actions-group">
                        <button type="button" class="btn-secondary" id="btn-manual-save" title="Lưu nháp ngay vào bộ nhớ trình duyệt">
                            <span class="material-symbols-outlined icon-sm" aria-hidden="true">save</span>
                            Lưu nháp
                        </button>
                        <button type="button" class="btn-secondary" id="btn-manual-sync" title="Gửi tín hiệu gia hạn phiên ngay">
                            <span class="material-symbols-outlined icon-sm" aria-hidden="true">sync</span>
                            Gia hạn phiên
                        </button>
                    </div>
                </div>
            </div>

            <!-- Bảng danh sách khách hàng lọc theo Data Scope (S1-05) -->
            <div class="table-container">
                <div style="padding: 12px 16px; background: #f8fafc; border-bottom: 1px solid var(--slate-200); display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 8px;">
                    <span style="font-size: 13px; color: var(--slate-600);">
                        Phạm vi dữ liệu: <strong><c:out value="${not empty phamViHienTai ? phamViHienTai.tenHienThi : 'Của tôi'}" /></strong>
                        • Đang hiển thị: <strong><c:out value="${not empty tongSoKhachHang ? tongSoKhachHang : 0}" /></strong> khách hàng
                    </span>
                    <div style="display: flex; align-items: center; gap: 16px;">
                        <c:set var="laQuanTriDanhMuc" value="${coQuyenDanhMuc or (not empty userHienTai and (userHienTai.coVaiTro('ADMIN') or userHienTai.coVaiTro('DIRECTOR')))}" />
                        <c:if test="${!laQuanTriDanhMuc}">
                            <a href="${pageContext.request.contextPath}/san-pham" style="font-size: 13px; color: var(--primary); text-decoration: none; font-weight: 600; display: inline-flex; align-items: center; gap: 4px;" id="linkSanPhamBangGia">
                                Sản phẩm & Bảng giá <span class="material-symbols-outlined icon-xs" aria-hidden="true">arrow_forward</span>
                            </a>
                        </c:if>
                        <a href="${pageContext.request.contextPath}/phan-quyen-du-lieu" style="font-size: 13px; color: var(--primary); text-decoration: none; font-weight: 600; display: inline-flex; align-items: center; gap: 4px;">
                            Quản lý 4 nghiệp vụ Data Scope <span class="material-symbols-outlined icon-xs" aria-hidden="true">arrow_forward</span>
                        </a>
                    </div>
                </div>
                <table class="data-table">
                    <thead>
                        <tr>
                            <th style="width: 100px;">Mã KH</th>
                            <th>Tên Khách Hàng / Công Ty</th>
                            <th>Người Phụ Trách</th>
                            <th>Nhóm Kinh Doanh</th>
                            <th>Phân Loại</th>
                            <th>Trạng Thái</th>
                            <th style="width: 120px; text-align: center;">Thao Tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty danhSachKhachHang}">
                                <c:forEach var="kh" items="${danhSachKhachHang}">
                                    <tr>
                                        <td class="font-mono"><c:out value="${kh.maBanGhi}" /></td>
                                        <td>
                                            <div class="customer-name"><c:out value="${kh.tieuDe}" /></div>
                                            <div class="customer-sub"><c:out value="${kh.moTaChiTiet}" /></div>
                                        </td>
                                        <td><c:out value="${kh.tenNguoiPhuTrach}" /></td>
                                        <td><c:out value="${kh.tenNhom}" /></td>
                                        <td><c:out value="${kh.giaTri}" /></td>
                                        <td><span class="badge badge-success"><c:out value="${kh.trangThai}" /></span></td>
                                        <td style="text-align: center;">
                                            <a href="${pageContext.request.contextPath}/chi-tiet-ban-ghi?id=${kh.id}" class="btn-action" title="Xem chi tiết khách hàng" aria-label="Xem chi tiết khách hàng">
                                                <span class="material-symbols-outlined" aria-hidden="true">visibility</span>
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="7" style="text-align: center; padding: 32px; color: var(--slate-500);">
                                        Không tìm thấy khách hàng nào trong phạm vi dữ liệu tài khoản của bạn.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- ===================================================================
             TAB 2: CHĂM SÓC ĐỊNH KỲ (Story S3-09)
             Acceptance Criteria:
             • AC1: Danh sách khách chưa có tương tác nào trong N ngày, N cấu hình được
             • AC2: Sắp xếp theo giá trị hợp đồng giảm dần
             • AC3: Đánh dấu đã liên hệ ngay trên danh sách
             =================================================================== -->
        <div class="crm-tab-pane ${tabHienTai eq 'cham-soc' ? 'active' : ''}" id="paneChamSoc" role="tabpanel" aria-labelledby="tabBtnChamSoc">
            <!-- 4 Thẻ tóm tắt số liệu chăm sóc khách hàng -->
            <div class="cs-stats-grid">
                <div class="cs-stat-card">
                    <div class="cs-stat-icon-wrapper cs-stat-icon-amber">
                        <span class="material-symbols-outlined" aria-hidden="true">alarm</span>
                    </div>
                    <div class="cs-stat-content">
                        <div class="cs-stat-value" id="statCanChamSoc">0</div>
                        <div class="cs-stat-label">Cần chăm sóc định kỳ</div>
                    </div>
                </div>

                <div class="cs-stat-card">
                    <div class="cs-stat-icon-wrapper cs-stat-icon-red">
                        <span class="material-symbols-outlined" aria-hidden="true">warning</span>
                    </div>
                    <div class="cs-stat-content">
                        <div class="cs-stat-value" id="statQuaHanNghiemTrong">0</div>
                        <div class="cs-stat-label">Quá hạn cao (&gt; 60 ngày)</div>
                    </div>
                </div>

                <div class="cs-stat-card">
                    <div class="cs-stat-icon-wrapper cs-stat-icon-primary">
                        <span class="material-symbols-outlined" aria-hidden="true">payments</span>
                    </div>
                    <div class="cs-stat-content">
                        <div class="cs-stat-value" id="statTongGiaTriHopDong">0 đ</div>
                        <div class="cs-stat-label">Giá trị HĐ cần bảo vệ</div>
                    </div>
                </div>

                <div class="cs-stat-card">
                    <div class="cs-stat-icon-wrapper cs-stat-icon-green">
                        <span class="material-symbols-outlined" aria-hidden="true">task_alt</span>
                    </div>
                    <div class="cs-stat-content">
                        <div class="cs-stat-value" id="statDaLienHeHomNay">0</div>
                        <div class="cs-stat-label">Đã liên hệ hôm nay</div>
                    </div>
                </div>
            </div>

            <!-- Khung cấu hình chu kỳ N ngày & Bộ lọc (AC1) -->
            <div class="cs-config-card">
                <div class="cs-config-header">
                    <div>
                        <div class="cs-config-title">
                            <span class="material-symbols-outlined" aria-hidden="true" style="color: var(--primary);">tune</span>
                            <span>Cấu hình chu kỳ chăm sóc & Bộ lọc (Story S3-09)</span>
                        </div>
                        <p class="cs-config-desc">
                            Lọc danh sách khách hàng đã ký hợp đồng chưa có bất kỳ tương tác nào trong <strong>N ngày</strong> để kịp thời chăm sóc trước lúc gia hạn.
                        </p>
                    </div>
                </div>
                <div class="cs-config-controls">
                    <div class="cs-config-left">
                        <div class="cs-input-group">
                            <label for="inputSoNgay">Chu kỳ chưa tương tác:</label>
                            <input type="number" id="inputSoNgay" class="cs-number-input" min="1" max="365" value="${not empty soNgayCauHinh ? soNgayCauHinh : 30}" aria-label="Số ngày chưa tương tác N">
                            <span style="font-size: 13.5px; font-weight: 600; color: var(--slate-600);">ngày</span>
                        </div>

                        <!-- Các mốc chọn nhanh chu kỳ N ngày -->
                        <div class="cs-chip-group" role="group" aria-label="Chọn nhanh chu kỳ N ngày">
                            <button type="button" class="cs-chip-btn" data-days="15">15 ngày</button>
                            <button type="button" class="cs-chip-btn active" data-days="30">30 ngày</button>
                            <button type="button" class="cs-chip-btn" data-days="45">45 ngày</button>
                            <button type="button" class="cs-chip-btn" data-days="60">60 ngày</button>
                            <button type="button" class="cs-chip-btn" data-days="90">90 ngày</button>
                        </div>

                        <button type="button" class="btn btn-primary" id="btnApDungSoNgay" style="height: 38px; min-height: 38px;">
                            <span class="material-symbols-outlined" aria-hidden="true">filter_alt</span>
                            <span>Áp dụng</span>
                        </button>

                        <button type="button" class="btn btn-outline" id="btnResetBoLoc" style="height: 38px; min-height: 38px;" title="Đặt lại bộ lọc về mặc định 30 ngày">
                            <span class="material-symbols-outlined" aria-hidden="true">restart_alt</span>
                            <span>Đặt lại</span>
                        </button>
                    </div>

                    <div class="cs-config-right">
                        <div class="cs-search-wrap">
                            <span class="material-symbols-outlined cs-search-icon" aria-hidden="true">search</span>
                            <input type="text" id="searchChamSoc" class="cs-search-input" placeholder="Tìm theo tên KH, mã KH, HĐ..." autocomplete="off">
                        </div>
                    </div>
                </div>
            </div>

            <!-- Bảng danh sách khách hàng cần chăm sóc định kỳ (AC2: Sắp xếp theo giá trị hợp đồng giảm dần) -->
            <div class="table-container">
                <div style="padding: 12px 16px; background: #f8fafc; border-bottom: 1px solid var(--slate-200); display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 8px;">
                    <span style="font-size: 13px; color: var(--slate-600);">
                        Tiêu chí: Chưa tương tác <strong>&ge; <span id="textSoNgayTieuChi">30</span> ngày</strong>
                        &bull; Thứ tự hiển thị: <strong style="color: var(--primary);">Giá trị hợp đồng giảm dần (AC2)</strong>
                        &bull; Đang hiển thị: <strong id="countHienThi">0</strong> / <strong id="countTongSo">0</strong> khách hàng
                    </span>
                    <span style="font-size: 12.5px; color: var(--slate-500); display: inline-flex; align-items: center; gap: 4px;">
                        <span class="material-symbols-outlined icon-xs" aria-hidden="true" style="color: var(--primary);">swap_vert</span>
                        Click vào cột Giá trị hợp đồng để chuyển đổi thứ tự
                    </span>
                </div>

                <table class="data-table" id="tableChamSocDinhKy">
                    <thead>
                        <tr>
                            <th style="width: 50px; text-align: center;">STT</th>
                            <th style="width: 90px;">Mã KH</th>
                            <th>Khách Hàng / Doanh Nghiệp</th>
                            <th class="th-sort-active" id="thGiaTriHopDong" style="cursor: pointer; width: 170px;" title="Nhấn để đảo chiều sắp xếp theo giá trị hợp đồng">
                                <span class="th-sort-wrap">
                                    <span>Giá Trị Hợp Đồng</span>
                                    <span class="material-symbols-outlined icon-xs" id="iconSortGiaTri" aria-hidden="true">arrow_downward</span>
                                </span>
                            </th>
                            <th style="width: 170px;">Tương Tác Gần Nhất</th>
                            <th style="width: 160px;">Người Phụ Trách</th>
                            <th style="width: 130px; text-align: center;">Trạng Thái</th>
                            <th style="width: 160px; text-align: center;">Thao Tác</th>
                        </tr>
                    </thead>
                    <tbody id="tbodyChamSocDinhKy">
                        <!-- Danh sách khách hàng trích xuất từ dữ liệu thực tế của servlet -->
                        <c:choose>
                            <c:when test="${not empty danhSachKhachHang}">
                                <c:forEach var="kh" items="${danhSachKhachHang}" varStatus="status">
                                    <tr class="row-khach-hang"
                                        data-kh-id="${kh.id}"
                                        data-ma-kh="<c:out value='${kh.maBanGhi}' />"
                                        data-ten-kh="<c:out value='${kh.tieuDe}' />"
                                        data-nguoi-phu-trach="<c:out value='${kh.tenNguoiPhuTrach}' />"
                                        data-ten-nhom="<c:out value='${kh.tenNhom}' />"
                                        data-ngay-tao="<c:out value='${kh.ngayTao}' />"
                                        data-gia-tri-raw="<c:out value='${kh.giaTri}' />"
                                        data-trang-thai="<c:out value='${kh.trangThai}' />"
                                        data-mo-ta="<c:out value='${kh.moTaChiTiet}' />">
                                        <td style="text-align: center;" class="cell-stt">${status.index + 1}</td>
                                        <td class="font-mono"><c:out value="${kh.maBanGhi}" /></td>
                                        <td>
                                            <div class="customer-name">
                                                <a href="${pageContext.request.contextPath}/chi-tiet-ban-ghi?id=${kh.id}" style="color: inherit; text-decoration: none;">
                                                    <c:out value="${kh.tieuDe}" />
                                                </a>
                                            </div>
                                            <div class="customer-sub"><c:out value="${kh.moTaChiTiet}" /></div>
                                        </td>
                                        <td>
                                            <div class="col-contract-highlight cell-gia-tri-hd">--- đ</div>
                                            <span class="contract-code-tag cell-ma-hd">HĐ-CHUA-KY</span>
                                        </td>
                                        <td>
                                            <div class="cell-overdue-wrap">
                                                <span class="overdue-badge badge-amber cell-overdue-badge">
                                                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">schedule</span>
                                                    <span class="text-so-ngay-chua-tt">-- ngày</span>
                                                </span>
                                                <span class="overdue-date-text cell-ngay-tt-cuoi">Chưa xác định</span>
                                            </div>
                                        </td>
                                        <td>
                                            <div><c:out value="${kh.tenNguoiPhuTrach}" /></div>
                                            <div style="font-size: 12px; color: var(--slate-500);"><c:out value="${kh.tenNhom}" /></div>
                                        </td>
                                        <td style="text-align: center;">
                                            <span class="badge cell-badge-trang-thai badge-warning">Cần liên hệ</span>
                                        </td>
                                        <td style="text-align: center;">
                                            <div class="actions-cell-flex">
                                                <c:set var="coQuyenChamSoc" value="${empty userHienTai or userHienTai.coVaiTro('ADMIN') or userHienTai.coVaiTro('DIRECTOR') or userHienTai.coVaiTro('TEAM_LEAD') or userHienTai.coVaiTro('SALES_REP') or userHienTai.coVaiTro('CUST_SUCCESS')}" />
                                                <c:choose>
                                                    <c:when test="${coQuyenChamSoc}">
                                                        <button type="button" class="btn-contact-mark btn-action-mark"
                                                                data-kh-id="${kh.id}"
                                                                data-ten-kh="<c:out value='${kh.tieuDe}' />"
                                                                title="Đánh dấu đã liên hệ khách hàng này ngay trên danh sách"
                                                                aria-label="Đánh dấu đã liên hệ với ${kh.tieuDe}">
                                                            <span class="material-symbols-outlined icon-xs" aria-hidden="true">phone_in_talk</span>
                                                            <span>Đã liên hệ</span>
                                                        </button>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <button type="button" class="btn-contact-mark" disabled title="Tài khoản kế toán chỉ có quyền xem dữ liệu" style="opacity: 0.6; cursor: not-allowed;">
                                                            <span class="material-symbols-outlined icon-xs" aria-hidden="true">visibility</span>
                                                            <span>Chỉ xem</span>
                                                        </button>
                                                    </c:otherwise>
                                                </c:choose>
                                                <a href="${pageContext.request.contextPath}/chi-tiet-ban-ghi?id=${kh.id}" class="btn-action" title="Xem chi tiết hồ sơ khách hàng" aria-label="Xem chi tiết ${kh.tieuDe}">
                                                    <span class="material-symbols-outlined" aria-hidden="true">visibility</span>
                                                </a>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="8" style="text-align: center; padding: 36px; color: var(--slate-500);">
                                        Không tìm thấy khách hàng nào trong hệ thống thuộc phạm vi dữ liệu của bạn.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- ===================================================================
             MODAL GHI NHẬN LIÊN HỆ CHĂM SÓC (Story S3-09 - AC3)
             =================================================================== -->
        <div class="modal-backdrop" id="modalGhiNhanLienHe" style="display: none;" role="dialog" aria-modal="true" aria-labelledby="modalGhiNhanTieuDe">
            <div class="modal-card">
                <div class="modal-header">
                    <div>
                        <h2 class="modal-title" id="modalGhiNhanTieuDe">Ghi Nhận Chăm Sóc Khách Hàng</h2>
                        <p class="modal-subtitle">Đánh dấu đã liên hệ và lưu lại tóm tắt kết quả tương tác (Story S3-09)</p>
                    </div>
                    <button type="button" class="modal-close-btn" id="btnDongModalGhiNhan" aria-label="Đóng" title="Đóng">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>
                <form id="formGhiNhanLienHe" method="POST" action="${pageContext.request.contextPath}/khach-hang">
                    <input type="hidden" name="action" value="danhDauLienHe">
                    <input type="hidden" id="modalKhachHangId" name="khachHangId" value="">
                    <input type="hidden" id="modalTenCongTyHidden" name="tenCongTy" value="">

                    <div class="modal-body">
                        <!-- Tóm tắt khách hàng đang chọn -->
                        <div class="form-group">
                            <label class="form-label">Khách hàng được chăm sóc</label>
                            <div class="form-readonly-badge" style="background-color: var(--primary-light); border-color: #bfdbfe;">
                                <span class="material-symbols-outlined icon-sm" aria-hidden="true" style="color: var(--primary);">domain</span>
                                <span style="color: var(--slate-900); font-size: 14px;">
                                    <strong id="modalTenKhachHangHienThi">Công ty Cổ phần Công nghệ FPT</strong>
                                    &bull; Mã KH: <span id="modalMaKhachHangHienThi" class="font-mono">KH-001</span>
                                </span>
                            </div>
                        </div>

                        <!-- Thông tin hợp đồng & chu kỳ quá hạn -->
                        <div class="form-row">
                            <div class="form-col">
                                <label class="form-label">Giá trị hợp đồng đang bảo vệ</label>
                                <div style="font-size: 15px; font-weight: 700; color: var(--primary); padding: 8px 12px; background: #f8fafc; border: 1px solid var(--slate-200); border-radius: var(--radius-sm);" id="modalGiaTriHdHienThi">
                                    850,000,000 đ
                                </div>
                            </div>
                            <div class="form-col">
                                <label class="form-label">Thời gian chưa tương tác</label>
                                <div style="font-size: 14px; font-weight: 600; color: #b45309; padding: 8px 12px; background: #fffbeb; border: 1px solid #fde68a; border-radius: var(--radius-sm);" id="modalSoNgayChuaTtHienThi">
                                    15 ngày chưa liên hệ
                                </div>
                            </div>
                        </div>

                        <!-- Chọn kênh tương tác -->
                        <div class="form-group">
                            <label class="form-label">Kênh liên hệ thực tế <span class="required">*</span></label>
                            <div class="channel-grid">
                                <label class="channel-radio-label">
                                    <input type="radio" name="kenhLienHe" value="CUOC_GOI" checked>
                                    <span class="channel-info">
                                        <span class="material-symbols-outlined icon-sm" aria-hidden="true">call</span>
                                        <span>Cuộc gọi điện thoại</span>
                                    </span>
                                </label>
                                <label class="channel-radio-label">
                                    <input type="radio" name="kenhLienHe" value="GAP_MAT">
                                    <span class="channel-info">
                                        <span class="material-symbols-outlined icon-sm" aria-hidden="true">handshake</span>
                                        <span>Gặp mặt trực tiếp</span>
                                    </span>
                                </label>
                                <label class="channel-radio-label">
                                    <input type="radio" name="kenhLienHe" value="EMAIL">
                                    <span class="channel-info">
                                        <span class="material-symbols-outlined icon-sm" aria-hidden="true">mail</span>
                                        <span>Email trao đổi</span>
                                    </span>
                                </label>
                                <label class="channel-radio-label">
                                    <input type="radio" name="kenhLienHe" value="ZALO_TIN_NHAN">
                                    <span class="channel-info">
                                        <span class="material-symbols-outlined icon-sm" aria-hidden="true">chat</span>
                                        <span>Zalo / Tin nhắn</span>
                                    </span>
                                </label>
                            </div>
                        </div>

                        <!-- Thời điểm liên hệ -->
                        <div class="form-group">
                            <label class="form-label" for="inputThoiGianLienHe">Thời điểm liên hệ <span class="required">*</span></label>
                            <input type="datetime-local" id="inputThoiGianLienHe" name="thoiGianLienHe" class="form-input" required>
                        </div>

                        <!-- Ghi chú nội dung chăm sóc -->
                        <div class="form-group">
                            <label class="form-label" for="inputGhiChuLienHe">Nội dung trao đổi &amp; Tình trạng gia hạn <span class="required">*</span></label>
                            <textarea id="inputGhiChuLienHe" name="ghiChu" class="form-textarea" rows="3" required
                                      placeholder="Ví dụ: Đã gọi điện hỏi thăm mức độ hài lòng về phần mềm CRM, khách hàng sử dụng ổn định và hẹn gửi bản dự thảo gia hạn hợp đồng vào thứ Sáu tới..."></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline" id="btnHuyGhiNhan">Hủy bỏ</button>
                        <button type="submit" class="btn btn-primary" id="btnXacNhanGhiNhan">
                            <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                            <span>Xác nhận đã liên hệ</span>
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <!-- ===================================================================
             MODAL THÊM KHÁCH HÀNG MỚI (Story S1-05)
             =================================================================== -->
        <div class="modal-backdrop" id="modalThemKhachHang" style="display: none;" role="dialog" aria-modal="true" aria-labelledby="modalThemKhachHangTieuDe">
            <div class="modal-card">
                <div class="modal-header">
                    <div>
                        <h2 class="modal-title" id="modalThemKhachHangTieuDe">Thêm Khách Hàng Mới</h2>
                        <p class="modal-subtitle">Tạo mới khách hàng thuộc phạm vi sở hữu của tài khoản hiện tại (S1-05)</p>
                    </div>
                    <button type="button" class="modal-close-btn" id="btnDongModalThemKhachHang" aria-label="Đóng" title="Đóng">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>
                <form id="formThemKhachHang" method="POST" action="${pageContext.request.contextPath}/khach-hang">
                    <input type="hidden" name="action" value="them">
                    <div class="modal-body">
                        <div class="form-group">
                            <label class="form-label" for="tenCongTy">Tên khách hàng / Công ty <span class="required">*</span></label>
                            <input type="text" id="tenCongTy" name="tenCongTy" class="form-input" placeholder="Ví dụ: Công ty Cổ phần Công nghệ ABC" required autocomplete="off">
                        </div>
                        <div class="form-row">
                            <div class="form-col">
                                <label class="form-label" for="maKhachHang">Mã khách hàng</label>
                                <input type="text" id="maKhachHang" name="maKhachHang" class="form-input" placeholder="Tự sinh nếu để trống" autocomplete="off">
                            </div>
                            <div class="form-col">
                                <label class="form-label" for="doanhThuUocTinh">Doanh thu ước tính</label>
                                <input type="text" id="doanhThuUocTinh" name="doanhThuUocTinh" class="form-input" placeholder="Ví dụ: 100,000,000 đ" autocomplete="off">
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="trangThai">Trạng thái</label>
                            <select id="trangThai" name="trangThai" class="form-select">
                                <option value="Tiềm năng" selected>Tiềm năng</option>
                                <option value="Đang tiếp cận">Đang tiếp cận</option>
                                <option value="Đang hợp tác">Đang hợp tác</option>
                                <option value="Khách hàng VIP">Khách hàng VIP</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Người sở hữu (Data Scope Server Enforcement)</label>
                            <div class="form-readonly-badge">
                                <span class="material-symbols-outlined icon-sm" aria-hidden="true">person</span>
                                <span>Chủ sở hữu: <strong><c:out value="${not empty currentUser ? currentUser.hoTen : sessionScope.nguoiDung.hoTen}" /></strong> &bull; <c:out value="${not empty currentUser ? currentUser.tenNhom : sessionScope.nguoiDung.tenNhomKinhDoanh}" /></span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="moTaChiTiet">Ghi chú / Mô tả chi tiết</label>
                            <textarea id="moTaChiTiet" name="moTaChiTiet" class="form-textarea" rows="3" placeholder="Nhập thêm nhu cầu, lĩnh vực, liên hệ..."></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline" id="btnHuyThemKhachHang">Hủy bỏ</button>
                        <button type="submit" class="btn btn-primary" id="btnXacNhanThemKhachHang">
                            <span class="material-symbols-outlined" aria-hidden="true">save</span>
                            Lưu Khách Hàng
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <!-- Toast Notifications Container -->
        <div class="crm-toast-container" id="crmToastContainer" aria-live="polite"></div>
    </main>

    <script src="${pageContext.request.contextPath}/assets/js/navigation.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/session-keep-alive.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/khach-hang.js"></script>
</body>
</html>
