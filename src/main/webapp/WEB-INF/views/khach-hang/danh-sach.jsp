<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Danh mục khách hàng & Ghi chú cuộc gặp - Hệ thống CRM Bán Hàng">
    <title>Danh Mục Khách Hàng - CRM Bán Hàng</title>
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

        <!-- Story S1-02: Khu vực soạn thảo ghi chú cuộc gặp (Ngồi ở quán cà phê không bị mất ghi chú) -->
        <div class="card" style="margin-bottom: 24px;">
            <div class="card-title">
                <div class="card-title-left">
                    <span class="material-symbols-outlined" aria-hidden="true" style="color: var(--primary);">edit_note</span>
                    <span>Ghi chú cuộc gặp khách hàng (Duy trì phiên & Tự động lưu)</span>
                </div>
                <div style="display: flex; align-items: center; gap: 12px; flex-wrap: wrap;">
                    <!-- Story S1-02: Thanh trạng thái phiên đăng nhập bảo mật và đếm ngược -->
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

        <!-- Tiêu đề trang & Thanh công cụ danh sách khách hàng -->
        <div class="page-header">
            <div>
                <h1 class="page-title">Danh Mục Khách Hàng</h1>
                <p class="page-subtitle">Quản lý và chăm sóc danh mục khách hàng thuộc quyền phụ trách</p>
            </div>
            <div class="page-actions">
                <button type="button" class="btn btn-outline" id="btnChuyenTabTrung" title="Xem cảnh báo và gộp khách hàng trùng lặp (S3-04)">
                    <span class="material-symbols-outlined" aria-hidden="true">call_merge</span>
                    <span>Kiểm Tra Trùng & Gộp</span>
                </button>
                <button type="button" class="btn btn-outline" id="btnExportExcel" title="Tải danh sách khách hàng dưới dạng file Excel (.xlsx)">
                    <span class="material-symbols-outlined" aria-hidden="true">table_view</span>
                    <span>Xuất Excel</span>
                </button>
                <button type="button" class="btn btn-primary" id="btnThemKhachHang">
                    <span class="material-symbols-outlined" aria-hidden="true">add</span>
                    <span>Thêm Khách Hàng</span>
                </button>
            </div>
        </div>

        <!-- Thanh Tab Điều Hướng (Story S3-04) -->
        <div class="crm-tab-navigation" role="tablist" aria-label="Phân loại danh sách khách hàng">
            <button type="button" class="crm-tab-btn ${tabHienTai != 'trung' ? 'active' : ''}" id="tabBtnTatCa" role="tab" aria-selected="${tabHienTai != 'trung'}" aria-controls="tabPaneTatCa">
                <span class="material-symbols-outlined" aria-hidden="true">groups</span>
                <span>Tất cả khách hàng</span>
                <span class="crm-tab-badge badge-neutral"><c:out value="${not empty tongSoKhachHang ? tongSoKhachHang : 0}" /></span>
            </button>
            <button type="button" class="crm-tab-btn ${tabHienTai == 'trung' ? 'active' : ''}" id="tabBtnTrungLap" role="tab" aria-selected="${tabHienTai == 'trung'}" aria-controls="tabPaneTrungLap">
                <span class="material-symbols-outlined" aria-hidden="true">call_merge</span>
                <span>Cảnh báo trùng lặp & Gộp khách hàng</span>
                <span class="crm-tab-badge badge-warning" id="tabBadgeSoCapTrung">0</span>
            </button>
        </div>

        <!-- TAB 1: Danh sách tất cả khách hàng (View mặc định) -->
        <div class="tab-pane" id="tabPaneTatCa" style="display: ${tabHienTai == 'trung' ? 'none' : 'block'};">
            <!-- Banner cảnh báo phát hiện trùng lặp trong danh mục -->
            <div class="dup-banner" id="bannerCanhBaoTrung" style="display: none;">
                <div class="dup-banner-left">
                    <span class="material-symbols-outlined dup-banner-icon" aria-hidden="true">warning</span>
                    <div>
                        <strong>Phát hiện khách hàng nghi trùng lặp:</strong>
                        <span id="bannerCanhBaoText">Hệ thống phát hiện có cặp khách hàng trùng thông tin (MST, Tên hoặc Website) giữa các nhân viên.</span>
                    </div>
                </div>
                <button type="button" class="btn btn-outline" id="btnXemCapTrungTuBanner" style="font-size: 13px; padding: 6px 14px;">
                    <span class="material-symbols-outlined" aria-hidden="true">call_merge</span>
                    <span>Xem & Gộp Trùng Ngay</span>
                </button>
            </div>

            <!-- Bảng danh sách khách hàng lọc theo Data Scope -->
            <div class="table-container">
                <div style="padding: 12px 16px; background: #f8fafc; border-bottom: 1px solid var(--slate-200); display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 8px;">
                    <span style="font-size: 13px; color: var(--slate-600);">
                        Phạm vi dữ liệu: <strong><c:out value="${not empty phamViHienTai ? phamViHienTai.tenHienThi : 'Của tôi'}" /></strong>
                        • Đang hiển thị: <strong><c:out value="${not empty tongSoKhachHang ? tongSoKhachHang : 0}" /></strong> khách hàng
                    </span>
                    <div style="display: flex; align-items: center; gap: 16px;">
                        <c:set var="userHienTai" value="${not empty nguoiDung ? nguoiDung : sessionScope.nguoiDung}" />
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
                <table class="data-table" id="tableKhachHang">
                    <thead>
                        <tr>
                            <th style="width: 100px;">Mã KH</th>
                            <th>Tên Khách Hàng / Công Ty</th>
                            <th>Người Phụ Trách</th>
                            <th>Nhóm Kinh Doanh</th>
                            <th>Phân Loại</th>
                            <th>Trạng Thái</th>
                            <th style="width: 130px; text-align: center;">Thao Tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty danhSachKhachHang}">
                                <c:forEach var="kh" items="${danhSachKhachHang}">
                                    <tr data-id="${kh.id}"
                                        data-ma="${kh.maBanGhi}"
                                        data-ten="${kh.tieuDe}"
                                        data-mst="${not empty kh.maSoThue ? kh.maSoThue : ''}"
                                        data-web="${not empty kh.website ? kh.website : ''}"
                                        data-owner-id="${kh.nguoiPhuTrachId}"
                                        data-owner-name="${kh.tenNguoiPhuTrach}"
                                        data-team-name="${kh.tenNhom}"
                                        data-gia-tri="${kh.giaTri}"
                                        data-trang-thai="${kh.trangThai}"
                                        data-ngay-tao="${kh.ngayTao}"
                                        data-mo-ta="${kh.moTaChiTiet}">
                                        <td class="font-mono"><c:out value="${kh.maBanGhi}" /></td>
                                        <td>
                                            <div class="customer-name-wrapper" style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
                                                <span class="customer-name"><c:out value="${kh.tieuDe}" /></span>
                                                <span class="badge badge-warning dup-badge-inline" style="display: none;" title="Phát hiện trùng lặp tiềm năng">
                                                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">warning</span> Trùng lặp
                                                </span>
                                            </div>
                                            <div class="customer-sub">
                                                <c:if test="${not empty kh.maSoThue}">
                                                    <span style="font-family: monospace; color: var(--slate-600);">MST: <c:out value="${kh.maSoThue}" /></span>
                                                </c:if>
                                                <c:if test="${not empty kh.website}">
                                                    <span> &bull; <c:out value="${kh.website}" /></span>
                                                </c:if>
                                                <c:if test="${empty kh.maSoThue and empty kh.website}">
                                                    <c:out value="${kh.moTaChiTiet}" />
                                                </c:if>
                                            </div>
                                        </td>
                                        <td><c:out value="${kh.tenNguoiPhuTrach}" /></td>
                                        <td><c:out value="${kh.tenNhom}" /></td>
                                        <td><c:out value="${kh.giaTri}" /></td>
                                        <td><span class="badge badge-success"><c:out value="${kh.trangThai}" /></span></td>
                                        <td style="text-align: center;">
                                            <div style="display: inline-flex; align-items: center; gap: 6px;">
                                                <button type="button" class="btn-action btn-action-warning btn-row-compare-trigger" data-id="${kh.id}" style="display: none;" title="Khách hàng nghi trùng - Bấm để so sánh & gộp" aria-label="So sánh gộp trùng">
                                                    <span class="material-symbols-outlined" aria-hidden="true">call_merge</span>
                                                </button>
                                                <a href="${pageContext.request.contextPath}/chi-tiet-ban-ghi?id=${kh.id}" class="btn-action" title="Xem chi tiết khách hàng" aria-label="Xem chi tiết khách hàng">
                                                    <span class="material-symbols-outlined" aria-hidden="true">visibility</span>
                                                </a>
                                            </div>
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

        <!-- TAB 2: Trung Tâm Cảnh Báo Trùng Lặp & Gộp Khách Hàng (Story S3-04) -->
        <div class="tab-pane" id="tabPaneTrungLap" style="display: ${tabHienTai == 'trung' ? 'block' : 'none'};">
            <!-- Thẻ giới thiệu quy trình phát hiện và gộp -->
            <div class="card" style="margin-bottom: 20px;">
                <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; flex-wrap: wrap;">
                    <div>
                        <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 6px;">
                            <span class="material-symbols-outlined" aria-hidden="true" style="color: var(--primary); font-size: 24px;">call_merge</span>
                            <h2 style="font-size: 17px; font-weight: 800; color: var(--slate-900);">Trung Tâm Cảnh Báo Trùng Lặp & Gộp Khách Hàng</h2>
                        </div>
                        <p style="color: var(--slate-600); font-size: 13.5px; line-height: 1.5; max-width: 860px;">
                            Hệ thống tự động phát hiện trùng theo <strong>Mã số thuế</strong>, <strong>Tên công ty gần giống</strong> và <strong>Website</strong>.
                            Trưởng nhóm kinh doanh kiểm tra so sánh cạnh nhau và thực hiện gộp để hai nhân viên không cùng chào một công ty mà không biết nhau.
                        </p>
                    </div>
                    <div>
                        <c:choose>
                            <c:when test="${laTruongNhomTroLen}">
                                <span class="badge badge-success" style="padding: 6px 12px; font-size: 12.5px;">
                                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">verified_user</span>
                                    Quyền: Trưởng nhóm trở lên (Được phép gộp)
                                </span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge badge-warning" style="padding: 6px 12px; font-size: 12.5px;" title="Chỉ Trưởng nhóm kinh doanh trở lên có quyền thực hiện gộp khách hàng">
                                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">lock</span>
                                    Chế độ xem (Chỉ Trưởng nhóm được gộp)
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

            <!-- Thanh điều khiển lọc và tìm kiếm cặp trùng -->
            <div class="dup-controls-bar">
                <div class="dup-filter-chips" role="group" aria-label="Lọc theo tiêu chí phát hiện">
                    <span style="font-size: 13px; font-weight: 600; color: var(--slate-600); margin-right: 4px;">Tiêu chí:</span>
                    <button type="button" class="dup-chip active" data-filter="all">Tất cả tiêu chí</button>
                    <button type="button" class="dup-chip" data-filter="mst">Trùng Mã số thuế</button>
                    <button type="button" class="dup-chip" data-filter="name">Tên gần giống</button>
                    <button type="button" class="dup-chip" data-filter="web">Trùng Website</button>
                </div>
                <div class="dup-search-box">
                    <span class="material-symbols-outlined dup-search-icon" aria-hidden="true">search</span>
                    <input type="text" id="inputTimKiemTrung" class="dup-search-input" placeholder="Tìm theo tên công ty, MST..." aria-label="Tìm kiếm cặp khách hàng trùng">
                </div>
            </div>

            <!-- Danh sách các thẻ cặp khách hàng trùng lặp -->
            <div class="dup-cards-list" id="containerDanhSachTrung">
                <!-- JS sẽ render các thẻ cặp khách hàng trùng lặp tại đây -->
            </div>

            <!-- Trạng thái không có bản ghi trùng lặp -->
            <div class="card" id="emptyStateTrung" style="display: none; text-align: center; padding: 48px 24px; color: var(--slate-500);">
                <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 48px; color: #16a34a; margin-bottom: 12px;">check_circle</span>
                <h3 style="font-size: 16px; font-weight: 700; color: var(--slate-800); margin-bottom: 6px;">Không Phát Hiện Khách Hàng Trùng Lặp</h3>
                <p style="font-size: 13.5px; max-width: 500px; margin: 0 auto;">
                    Dữ liệu khách hàng trong phạm vi của bạn hiện tại không có xung đột về Mã số thuế, Tên công ty hay Website.
                </p>
            </div>
        </div>

        <!-- ==========================================================================
             MODAL SO SÁNH CẠNH NHAU TRƯỚC KHI GỘP (Story S3-04 - AC2, AC3, AC4)
             ========================================================================== -->
        <div class="modal-backdrop" id="modalSoSanhGop" style="display: none;" role="dialog" aria-modal="true" aria-labelledby="modalSoSanhGopTieuDe">
            <div class="modal-compare-card">
                <div class="modal-header">
                    <div>
                        <h2 class="modal-title" id="modalSoSanhGopTieuDe" style="display: flex; align-items: center; gap: 8px;">
                            <span class="material-symbols-outlined" aria-hidden="true" style="color: var(--primary);">compare_arrows</span>
                            So Sánh Khách Hàng Trùng Lặp Cạnh Nhau
                        </h2>
                        <p class="modal-subtitle">Kiểm tra thông tin trước khi gộp. Toàn bộ người liên hệ, cơ hội và hoạt động sẽ được giữ lại (AC3).</p>
                    </div>
                    <button type="button" class="modal-close-btn" id="btnDongModalSoSanh" aria-label="Đóng cửa sổ" title="Đóng">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>

                <form id="formXacNhanGop" method="POST" action="${pageContext.request.contextPath}/khach-hang" style="display: flex; flex-direction: column; overflow: hidden; height: 100%;">
                    <input type="hidden" name="action" value="gop">
                    <input type="hidden" name="khachHangDichId" id="compareKhachHangDichId">
                    <input type="hidden" name="khachHangNguonId" id="compareKhachHangNguonId">

                    <div class="modal-compare-body">
                        <!-- Toolbar điều khiển Nguồn/Đích -->
                        <div class="compare-toolbar">
                            <div style="font-size: 13px; color: var(--slate-600);">
                                <span class="material-symbols-outlined icon-xs" aria-hidden="true" style="vertical-align: text-bottom; color: var(--primary);">info</span>
                                <strong>Bản ghi đích (Cột trái):</strong> Giữ lại làm hồ sơ chính &bull; <strong>Bản ghi nguồn (Cột phải):</strong> Gộp vào và lưu lịch sử.
                            </div>
                            <button type="button" class="btn btn-outline" id="btnHoanDoiViTri" style="font-size: 13px; padding: 6px 14px;">
                                <span class="material-symbols-outlined" aria-hidden="true">swap_horiz</span>
                                <span>Đổi Vị Trí Đích / Nguồn</span>
                            </button>
                        </div>

                        <!-- Cảnh báo xung đột 2 nhân viên (nếu có) -->
                        <div class="dup-conflict-alert" id="compareConflictAlert" style="display: none; border-radius: var(--radius-sm);">
                            <span class="material-symbols-outlined icon-sm" aria-hidden="true" style="color: #dc2626;">warning</span>
                            <span><strong>Cảnh báo hai nhân viên cùng chào một công ty:</strong> <span id="compareConflictText"></span></span>
                        </div>

                        <!-- Bảng so sánh 2 cột cạnh nhau (Side-by-Side Table - AC2) -->
                        <div class="compare-table-wrap">
                            <table class="compare-table">
                                <thead>
                                    <tr>
                                        <th class="compare-field-name">Trường thông tin</th>
                                        <th class="col-dest">
                                            <div style="display: flex; align-items: center; justify-content: space-between; gap: 8px;">
                                                <span>BẢN GHI ĐÍCH (GIỮ LẠI CHÍNH)</span>
                                                <span class="badge badge-success" style="font-size: 11px;">Hồ sơ chính</span>
                                            </div>
                                        </th>
                                        <th class="col-source">
                                            <div style="display: flex; align-items: center; justify-content: space-between; gap: 8px;">
                                                <span>BẢN GHI NGUỒN (GỘP VÀO)</span>
                                                <span class="badge badge-warning" style="font-size: 11px;">Gộp & Lưu lịch sử</span>
                                            </div>
                                        </th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                        <td class="compare-field-name">Mã khách hàng</td>
                                        <td class="font-mono" id="cmpDestMa">-</td>
                                        <td class="font-mono" id="cmpSrcMa">-</td>
                                    </tr>
                                    <tr>
                                        <td class="compare-field-name">Tên công ty</td>
                                        <td class="compare-val-cell">
                                            <label class="compare-val-box">
                                                <input type="radio" name="chonTenCongTy" value="dest" class="compare-radio" checked>
                                                <span class="compare-val-text" id="cmpDestTen">-</span>
                                            </label>
                                        </td>
                                        <td class="compare-val-cell">
                                            <label class="compare-val-box">
                                                <input type="radio" name="chonTenCongTy" value="source" class="compare-radio">
                                                <span class="compare-val-text" id="cmpSrcTen">-</span>
                                            </label>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="compare-field-name">Mã số thuế (MST)</td>
                                        <td class="compare-val-cell">
                                            <span class="compare-val-text font-mono" id="cmpDestMst">-</span>
                                        </td>
                                        <td class="compare-val-cell">
                                            <span class="compare-val-text font-mono" id="cmpSrcMst">-</span>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="compare-field-name">Website</td>
                                        <td class="compare-val-cell">
                                            <span class="compare-val-text" id="cmpDestWeb">-</span>
                                        </td>
                                        <td class="compare-val-cell">
                                            <span class="compare-val-text" id="cmpSrcWeb">-</span>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="compare-field-name">Người phụ trách</td>
                                        <td class="compare-val-cell">
                                            <label class="compare-val-box">
                                                <input type="radio" name="chonNguoiPhuTrach" value="dest" class="compare-radio" checked>
                                                <span class="compare-val-text" id="cmpDestOwner" style="font-weight: 700;">-</span>
                                            </label>
                                        </td>
                                        <td class="compare-val-cell">
                                            <label class="compare-val-box">
                                                <input type="radio" name="chonNguoiPhuTrach" value="source" class="compare-radio">
                                                <span class="compare-val-text" id="cmpSrcOwner" style="font-weight: 700;">-</span>
                                            </label>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="compare-field-name">Nhóm kinh doanh</td>
                                        <td id="cmpDestTeam">-</td>
                                        <td id="cmpSrcTeam">-</td>
                                    </tr>
                                    <tr>
                                        <td class="compare-field-name">Doanh thu ước tính</td>
                                        <td id="cmpDestGiaTri">-</td>
                                        <td id="cmpSrcGiaTri">-</td>
                                    </tr>
                                    <tr>
                                        <td class="compare-field-name">Trạng thái</td>
                                        <td><span class="badge badge-success" id="cmpDestTrangThai">-</span></td>
                                        <td><span class="badge badge-neutral" id="cmpSrcTrangThai">-</span></td>
                                    </tr>
                                    <tr>
                                        <td class="compare-field-name">Ngày tạo</td>
                                        <td id="cmpDestNgayTao">-</td>
                                        <td id="cmpSrcNgayTao">-</td>
                                    </tr>
                                    <!-- AC3: Bảo toàn số lượng Người liên hệ, Cơ hội và Hoạt động -->
                                    <tr style="background-color: #f0fdf4;">
                                        <td class="compare-field-name" style="color: #166534; font-weight: 700;">
                                            <span class="material-symbols-outlined icon-xs" aria-hidden="true" style="vertical-align: text-bottom;">person_add</span>
                                            Người liên hệ
                                        </td>
                                        <td style="color: #166534; font-weight: 600;" id="cmpDestNlh">1 người liên hệ</td>
                                        <td style="color: #166534; font-weight: 600;" id="cmpSrcNlh">1 người liên hệ (Được giữ lại 100%)</td>
                                    </tr>
                                    <tr style="background-color: #f0fdf4;">
                                        <td class="compare-field-name" style="color: #166534; font-weight: 700;">
                                            <span class="material-symbols-outlined icon-xs" aria-hidden="true" style="vertical-align: text-bottom;">briefcase</span>
                                            Cơ hội bán hàng
                                        </td>
                                        <td style="color: #166534; font-weight: 600;" id="cmpDestCoHoi">1 cơ hội</td>
                                        <td style="color: #166534; font-weight: 600;" id="cmpSrcCoHoi">1 cơ hội (Được giữ lại 100%)</td>
                                    </tr>
                                    <tr style="background-color: #f0fdf4;">
                                        <td class="compare-field-name" style="color: #166534; font-weight: 700;">
                                            <span class="material-symbols-outlined icon-xs" aria-hidden="true" style="vertical-align: text-bottom;">calendar_month</span>
                                            Lịch sử hoạt động
                                        </td>
                                        <td style="color: #166534; font-weight: 600;" id="cmpDestHoatDong">1 hoạt động</td>
                                        <td style="color: #166534; font-weight: 600;" id="cmpSrcHoatDong">1 hoạt động (Được giữ lại 100%)</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>

                        <!-- Cam kết bảo toàn dữ liệu khi gộp (AC3) -->
                        <div class="data-preservation-card">
                            <div class="preservation-title">
                                <span class="material-symbols-outlined" aria-hidden="true">security</span>
                                Cam Kết Bảo Toàn Dữ Liệu Sau Khi Gộp (Acceptance Criteria AC3)
                            </div>
                            <ul class="preservation-list">
                                <li class="preservation-item">
                                    <span class="material-symbols-outlined icon-check" aria-hidden="true">check_circle</span>
                                    <span>Toàn bộ <strong>Người liên hệ</strong> của cả hai bản ghi được giữ lại và chuyển về khách hàng chính.</span>
                                </li>
                                <li class="preservation-item">
                                    <span class="material-symbols-outlined icon-check" aria-hidden="true">check_circle</span>
                                    <span>Toàn bộ <strong>Cơ hội bán hàng</strong> (Pipeline) đang chăm sóc của cả hai bên đều được bảo toàn.</span>
                                </li>
                                <li class="preservation-item">
                                    <span class="material-symbols-outlined icon-check" aria-hidden="true">check_circle</span>
                                    <span>Toàn bộ <strong>Lịch sử hoạt động</strong> (Cuộc gọi, Họp, Ghi chú) được gom đầy đủ vào dòng thời gian khách hàng chính.</span>
                                </li>
                                <li class="preservation-item">
                                    <span class="material-symbols-outlined icon-check" aria-hidden="true">check_circle</span>
                                    <span>Lưu vết kiểm toán vào bảng <strong>lich_su_gop_khach_hang</strong> phục vụ tra cứu minh bạch về sau.</span>
                                </li>
                            </ul>
                        </div>

                        <!-- Lý do gộp khách hàng -->
                        <div class="form-group" style="margin-bottom: 0;">
                            <label class="form-label" for="lyDoGop">Lý do thực hiện gộp khách hàng <span class="required">*</span></label>
                            <textarea id="lyDoGop" name="lyDoGop" class="form-textarea" rows="2" placeholder="Ví dụ: Hai nhân viên A và B cùng chào một công ty, thống nhất gộp lại và phân bổ cho nhân viên chính chăm sóc..." required></textarea>
                        </div>

                        <!-- Kiểm tra quyền thực hiện gộp (AC4) -->
                        <c:if test="${!laTruongNhomTroLen}">
                            <div class="alert alert-warning" style="margin-top: 10px;">
                                <span class="material-symbols-outlined" aria-hidden="true">lock</span>
                                <span><strong>Giới hạn quyền hạn (AC4):</strong> Chỉ Trưởng nhóm kinh doanh trở lên (Team Lead, Director, Admin) mới có quyền thực hiện gộp khách hàng. Tài khoản của bạn có thể xem so sánh dữ liệu nhưng không thể gửi yêu cầu gộp.</span>
                            </div>
                        </c:if>
                    </div>

                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline" id="btnHuySoSanhGop">Đóng</button>
                        <c:choose>
                            <c:when test="${laTruongNhomTroLen}">
                                <button type="submit" class="btn btn-primary" id="btnXacNhanThucHienGop">
                                    <span class="material-symbols-outlined" aria-hidden="true">call_merge</span>
                                    <span>Xác Nhận Gộp Khách Hàng</span>
                                </button>
                            </c:when>
                            <c:otherwise>
                                <button type="button" class="btn btn-primary" disabled title="Chỉ Trưởng nhóm kinh doanh trở lên có quyền thực hiện gộp">
                                    <span class="material-symbols-outlined" aria-hidden="true">lock</span>
                                    <span>Yêu Cầu Quyền Trưởng Nhóm</span>
                                </button>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </form>
            </div>
        </div>

        <!-- Modal Thêm Khách Hàng Mới (Story S1-05 & Tích hợp cảnh báo trùng S3-04) -->
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
                            <input type="text" id="tenCongTy" name="tenCongTy" class="form-input" placeholder="Ví dụ: Công ty Cổ phần Công nghệ FPT" required autocomplete="off">
                            <!-- Cảnh báo trùng lặp thời gian thực khi gõ tên công ty (S3-04) -->
                            <div class="inline-dup-alert" id="inlineDupNameAlert" style="display: none;">
                                <span class="material-symbols-outlined icon-sm" aria-hidden="true">warning</span>
                                <div><strong>Cảnh báo tên gần giống:</strong> Có thể trùng với khách hàng <strong id="dupMatchedName"></strong> do <strong id="dupMatchedOwner"></strong> phụ trách!</div>
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-col">
                                <label class="form-label" for="maSoThueThem">Mã số thuế</label>
                                <input type="text" id="maSoThueThem" name="maSoThue" class="form-input" placeholder="Ví dụ: 0101234567" autocomplete="off">
                                <!-- Cảnh báo trùng MST (S3-04) -->
                                <div class="inline-dup-alert" id="inlineDupMstAlert" style="display: none;">
                                    <span class="material-symbols-outlined icon-sm" aria-hidden="true">warning</span>
                                    <div><strong>Trùng mã số thuế:</strong> MST này đã tồn tại trên hồ sơ <strong id="dupMatchedMstName"></strong>!</div>
                                </div>
                            </div>
                            <div class="form-col">
                                <label class="form-label" for="websiteThem">Website</label>
                                <input type="text" id="websiteThem" name="website" class="form-input" placeholder="Ví dụ: https://fpt.com.vn" autocomplete="off">
                                <!-- Cảnh báo trùng Website (S3-04) -->
                                <div class="inline-dup-alert" id="inlineDupWebAlert" style="display: none;">
                                    <span class="material-symbols-outlined icon-sm" aria-hidden="true">warning</span>
                                    <div><strong>Trùng Website:</strong> Tên miền website đã được ghi nhận trên <strong id="dupMatchedWebName"></strong>!</div>
                                </div>
                            </div>
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
    </main>

    <script>
        window.LA_TRUONG_NHOM = ${laTruongNhomTroLen ? 'true' : 'false'};
        window.TAB_HIEN_TAI = "${not empty tabHienTai ? tabHienTai : 'tat-ca'}";
    </script>

    <script src="${pageContext.request.contextPath}/assets/js/navigation.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/session-keep-alive.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/khach-hang.js"></script>
</body>
</html>
