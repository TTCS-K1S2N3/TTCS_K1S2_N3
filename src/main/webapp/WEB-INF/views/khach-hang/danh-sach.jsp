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
    <style>
        .badge-status-tiem-nang { background-color: #e0f2fe; color: #0369a1; font-weight: 600; padding: 4px 10px; border-radius: 9999px; font-size: 12px; display: inline-block; }
        .badge-status-dang-giao-dich { background-color: #fef3c7; color: #b45309; font-weight: 600; padding: 4px 10px; border-radius: 9999px; font-size: 12px; display: inline-block; }
        .badge-status-khach-hang { background-color: #dcfce7; color: #15803d; font-weight: 600; padding: 4px 10px; border-radius: 9999px; font-size: 12px; display: inline-block; }
        .badge-status-ngung-hop-tac { background-color: #fee2e2; color: #b91c1c; font-weight: 600; padding: 4px 10px; border-radius: 9999px; font-size: 12px; display: inline-block; }
    </style>
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
        <c:if test="${not empty thongBaoCanhBao}">
            <div class="alert alert-warning" id="alertWarning">
                <span class="material-symbols-outlined" aria-hidden="true">warning</span>
                <span><c:out value="${thongBaoCanhBao}" /></span>
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

        <!-- Bộ lọc và tìm kiếm khách hàng -->
        <div class="card" style="margin-bottom: 24px;">
            <form method="GET" action="${pageContext.request.contextPath}/khach-hang" class="filter-form">
                <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 14px; align-items: end;">
                    <div class="form-group" style="margin-bottom: 0;">
                        <label class="form-label" for="filterTuKhoa">Từ khóa tìm kiếm</label>
                        <input type="text" id="filterTuKhoa" name="tuKhoa" class="form-input"
                               placeholder="Tên công ty, MST, website, người sở hữu..."
                               value="<c:out value="${tuKhoaHienTai}" />" autocomplete="off">
                    </div>
                    <div class="form-group" style="margin-bottom: 0;">
                        <label class="form-label" for="filterTrangThai">Trạng thái (AC3)</label>
                        <select id="filterTrangThai" name="trangThai" class="form-select">
                            <option value="">-- Tất cả trạng thái --</option>
                            <option value="Tiềm năng" ${trangThaiHienTai == 'Tiềm năng' || trangThaiHienTai == 'TIEM_NANG' ? 'selected' : ''}>Tiềm năng</option>
                            <option value="Đang giao dịch" ${trangThaiHienTai == 'Đang giao dịch' || trangThaiHienTai == 'DANG_GIAO_DICH' ? 'selected' : ''}>Đang giao dịch</option>
                            <option value="Khách hàng" ${trangThaiHienTai == 'Khách hàng' || trangThaiHienTai == 'KHACH_HANG' ? 'selected' : ''}>Khách hàng</option>
                            <option value="Ngừng hợp tác" ${trangThaiHienTai == 'Ngừng hợp tác' || trangThaiHienTai == 'NGUNG_HOP_TAC' ? 'selected' : ''}>Ngừng hợp tác</option>
                        </select>
                    </div>
                    <c:if test="${not empty dsNganhNghe}">
                        <div class="form-group" style="margin-bottom: 0;">
                            <label class="form-label" for="filterNganhNghe">Ngành nghề</label>
                            <select id="filterNganhNghe" name="nganhNgheId" class="form-select">
                                <option value="">-- Tất cả ngành nghề --</option>
                                <c:forEach var="nn" items="${dsNganhNghe}">
                                    <option value="${nn.id}" ${nganhNgheIdHienTai == nn.id ? 'selected' : ''}>
                                        <c:out value="${nn.tenMuc}" />
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </c:if>
                    <c:if test="${not empty dsQuyMo}">
                        <div class="form-group" style="margin-bottom: 0;">
                            <label class="form-label" for="filterQuyMo">Quy mô</label>
                            <select id="filterQuyMo" name="quyMoId" class="form-select">
                                <option value="">-- Tất cả quy mô --</option>
                                <c:forEach var="qm" items="${dsQuyMo}">
                                    <option value="${qm.id}" ${quyMoIdHienTai == qm.id ? 'selected' : ''}>
                                        <c:out value="${qm.tenMuc}" />
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </c:if>
                    <c:if test="${not empty danhSachPhamViChoPhep and danhSachPhamViChoPhep.size() > 1}">
                        <div class="form-group" style="margin-bottom: 0;">
                            <label class="form-label" for="filterPhamVi">Phạm vi dữ liệu (AC4)</label>
                            <select id="filterPhamVi" name="phamVi" class="form-select">
                                <c:forEach var="pv" items="${danhSachPhamViChoPhep}">
                                    <option value="${pv.ma}" ${phamViHienTai == pv ? 'selected' : ''}>
                                        <c:out value="${pv.tenHienThi}" />
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </c:if>
                    <div style="display: flex; gap: 8px;">
                        <button type="submit" class="btn btn-primary" style="flex: 1;">
                            <span class="material-symbols-outlined" aria-hidden="true">search</span>
                            <span>Tìm kiếm</span>
                        </button>
                        <a href="${pageContext.request.contextPath}/khach-hang" class="btn btn-outline" title="Làm mới bộ lọc">Làm mới</a>
                    </div>
                </div>
            </form>
        </div>

        <!-- Tiêu đề trang & Thanh công cụ danh sách khách hàng -->
        <div class="page-header">
            <div>
                <h1 class="page-title">Danh Mục Khách Hàng</h1>
                <p class="page-subtitle">Quản lý và chăm sóc danh mục khách hàng thuộc quyền phụ trách</p>
            </div>
            <div class="page-actions">
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
            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width: 100px;">Mã KH</th>
                        <th>Tên Công Ty / Khách Hàng</th>
                        <th>Mã Số Thuế</th>
                        <th>Ngành Nghề & Quy Mô</th>
                        <th>Website & Địa Chỉ</th>
                        <th>Người Sở Hữu (AC4)</th>
                        <th>Trạng Thái (AC3)</th>
                        <th style="width: 100px; text-align: center;">Thao Tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty danhSachKhachHangModel}">
                            <c:forEach var="kh" items="${danhSachKhachHangModel}">
                                <tr>
                                    <td class="font-mono"><c:out value="${kh.maKhachHang}" /></td>
                                    <td>
                                        <div class="customer-name" style="font-weight: 600; color: var(--slate-900);"><c:out value="${kh.tenCongTy}" /></div>
                                        <c:if test="${not empty kh.moTaChiTiet}">
                                            <div class="customer-sub" style="font-size: 12px; color: var(--slate-500);"><c:out value="${kh.moTaChiTiet}" /></div>
                                        </c:if>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty kh.maSoThue}">
                                                <code style="background: #f1f5f9; padding: 2px 6px; border-radius: 4px; font-size: 12px;"><c:out value="${kh.maSoThue}" /></code>
                                            </c:when>
                                            <c:otherwise>
                                                <span style="color: var(--slate-400); font-style: italic;">Chưa có</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <div><c:out value="${not empty kh.tenNganhNghe ? kh.tenNganhNghe : 'Chưa chọn ngành'}" /></div>
                                        <div style="font-size: 12px; color: var(--slate-500);"><c:out value="${not empty kh.tenQuyMo ? kh.tenQuyMo : '-'}" /></div>
                                    </td>
                                    <td>
                                        <c:if test="${not empty kh.website}">
                                            <div>
                                                <a href="${kh.website.startsWith('http') ? kh.website : 'https://'.concat(kh.website)}" target="_blank" rel="noopener noreferrer" style="color: var(--primary); font-size: 13px; text-decoration: none;">
                                                    <c:out value="${kh.website}" />
                                                </a>
                                            </div>
                                        </c:if>
                                        <c:if test="${not empty kh.diaChi}">
                                            <div style="font-size: 12px; color: var(--slate-500); max-width: 220px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;" title="<c:out value="${kh.diaChi}" />">
                                                <c:out value="${kh.diaChi}" />
                                            </div>
                                        </c:if>
                                        <c:if test="${empty kh.website && empty kh.diaChi}">
                                            <span style="color: var(--slate-400); font-style: italic;">Chưa cập nhật</span>
                                        </c:if>
                                    </td>
                                    <td>
                                        <div style="font-weight: 500;"><c:out value="${kh.tenNguoiSoHuu}" /></div>
                                        <div style="font-size: 12px; color: var(--slate-500);"><c:out value="${kh.tenNhomKinhDoanh}" /></div>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${kh.trangThai == 'TIEM_NANG' || kh.trangThai == 'Tiềm năng'}">
                                                <span class="badge-status-tiem-nang">● Tiềm năng</span>
                                            </c:when>
                                            <c:when test="${kh.trangThai == 'DANG_GIAO_DICH' || kh.trangThai == 'Đang giao dịch'}">
                                                <span class="badge-status-dang-giao-dich">● Đang giao dịch</span>
                                            </c:when>
                                            <c:when test="${kh.trangThai == 'KHACH_HANG' || kh.trangThai == 'Khách hàng'}">
                                                <span class="badge-status-khach-hang">● Khách hàng</span>
                                            </c:when>
                                            <c:when test="${kh.trangThai == 'NGUNG_HOP_TAC' || kh.trangThai == 'Ngừng hợp tác'}">
                                                <span class="badge-status-ngung-hop-tac">● Ngừng hợp tác</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge badge-info"><c:out value="${kh.trangThaiHienThi}" /></span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="text-align: center;">
                                        <a href="${pageContext.request.contextPath}/khach-hang/chi-tiet?id=${kh.id}" class="btn-action" title="Xem chi tiết hồ sơ khách hàng" aria-label="Xem chi tiết hồ sơ <c:out value="${kh.tenCongTy}" />">
                                            <span class="material-symbols-outlined" aria-hidden="true">visibility</span>
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:when test="${not empty danhSachKhachHang}">
                            <c:forEach var="kh" items="${danhSachKhachHang}">
                                <tr>
                                    <td class="font-mono"><c:out value="${kh.maBanGhi}" /></td>
                                    <td>
                                        <div class="customer-name" style="font-weight: 600;"><c:out value="${kh.tieuDe}" /></div>
                                        <div class="customer-sub" style="font-size: 12px; color: var(--slate-500);"><c:out value="${kh.moTaChiTiet}" /></div>
                                    </td>
                                    <td><span style="color: var(--slate-400); font-style: italic;">Chưa có</span></td>
                                    <td><div>-</div></td>
                                    <td><span style="color: var(--slate-400); font-style: italic;">Chưa cập nhật</span></td>
                                    <td>
                                        <div style="font-weight: 500;"><c:out value="${kh.tenNguoiPhuTrach}" /></div>
                                        <div style="font-size: 12px; color: var(--slate-500);"><c:out value="${kh.tenNhom}" /></div>
                                    </td>
                                    <td><span class="badge badge-success"><c:out value="${kh.trangThai}" /></span></td>
                                    <td style="text-align: center;">
                                        <a href="${pageContext.request.contextPath}/khach-hang/chi-tiet?id=${kh.id}" class="btn-action" title="Xem chi tiết khách hàng" aria-label="Xem chi tiết khách hàng <c:out value="${kh.tieuDe}" />">
                                            <span class="material-symbols-outlined" aria-hidden="true">visibility</span>
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="8" style="text-align: center; padding: 32px; color: var(--slate-500);">
                                    Không tìm thấy khách hàng nào trong phạm vi dữ liệu tài khoản của bạn.
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>

        <!-- Modal Thêm Khách Hàng Mới (Story S3-01 & S1-05) -->
        <div class="modal-backdrop" id="modalThemKhachHang" style="display: none;" role="dialog" aria-modal="true" aria-labelledby="modalThemKhachHangTieuDe">
            <div class="modal-card">
                <div class="modal-header">
                    <div>
                        <h2 class="modal-title" id="modalThemKhachHangTieuDe">Thêm Khách Hàng Doanh Nghiệp Mới</h2>
                        <p class="modal-subtitle">Khai báo thông tin hồ sơ khách hàng chuẩn hệ thống (Story S3-01)</p>
                    </div>
                    <button type="button" class="modal-close-btn" id="btnDongModalThemKhachHang" aria-label="Đóng" title="Đóng">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>
                <form id="formThemKhachHang" method="POST" action="${pageContext.request.contextPath}/khach-hang">
                    <input type="hidden" name="action" value="them">
                    <div class="modal-body">
                        <!-- Tên công ty & Mã khách hàng -->
                        <div class="form-row">
                            <div class="form-col" style="flex: 2;">
                                <label class="form-label" for="tenCongTy">Tên công ty / Khách hàng (AC1) <span class="required" style="color: #ef4444;">*</span></label>
                                <input type="text" id="tenCongTy" name="tenCongTy" class="form-input" placeholder="Ví dụ: Công ty Cổ phần Công nghệ ABC" required autocomplete="off">
                            </div>
                            <div class="form-col" style="flex: 1;">
                                <label class="form-label" for="maKhachHang">Mã khách hàng</label>
                                <input type="text" id="maKhachHang" name="maKhachHang" class="form-input" placeholder="Tự sinh nếu để trống" autocomplete="off">
                            </div>
                        </div>

                        <!-- Mã số thuế & Website -->
                        <div class="form-row">
                            <div class="form-col">
                                <label class="form-label" for="maSoThue">Mã số thuế (AC1, AC2) <small style="color: var(--slate-500); font-weight: normal;">(Nếu có phải duy nhất)</small></label>
                                <input type="text" id="maSoThue" name="maSoThue" class="form-input" placeholder="Ví dụ: 0101234567" autocomplete="off">
                            </div>
                            <div class="form-col">
                                <label class="form-label" for="website">Website (AC1)</label>
                                <input type="text" id="website" name="website" class="form-input" placeholder="Ví dụ: https://congtyabc.vn" autocomplete="off">
                            </div>
                        </div>

                        <!-- Ngành nghề & Quy mô -->
                        <div class="form-row">
                            <div class="form-col">
                                <label class="form-label" for="nganhNgheId">Ngành nghề (AC1)</label>
                                <select id="nganhNgheId" name="nganhNgheId" class="form-select">
                                    <option value="">-- Chọn ngành nghề --</option>
                                    <c:forEach var="nn" items="${dsNganhNghe}">
                                        <option value="${nn.id}"><c:out value="${nn.tenMuc}" /></option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="form-col">
                                <label class="form-label" for="quyMoId">Quy mô doanh nghiệp (AC1)</label>
                                <select id="quyMoId" name="quyMoId" class="form-select">
                                    <option value="">-- Chọn quy mô --</option>
                                    <c:forEach var="qm" items="${dsQuyMo}">
                                        <option value="${qm.id}"><c:out value="${qm.tenMuc}" /></option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>

                        <!-- Địa chỉ -->
                        <div class="form-group">
                            <label class="form-label" for="diaChi">Địa chỉ trụ sở / văn phòng (AC1)</label>
                            <input type="text" id="diaChi" name="diaChi" class="form-input" placeholder="Số nhà, đường, quận/huyện, tỉnh/thành phố..." autocomplete="off">
                        </div>

                        <!-- Trạng thái & Doanh thu ước tính -->
                        <div class="form-row">
                            <div class="form-col">
                                <label class="form-label" for="trangThai">Trạng thái khách hàng (AC3) <span class="required" style="color: #ef4444;">*</span></label>
                                <select id="trangThai" name="trangThai" class="form-select" required>
                                    <option value="Tiềm năng" selected>Tiềm năng</option>
                                    <option value="Đang giao dịch">Đang giao dịch</option>
                                    <option value="Khách hàng">Khách hàng</option>
                                    <option value="Ngừng hợp tác">Ngừng hợp tác</option>
                                </select>
                            </div>
                            <div class="form-col">
                                <label class="form-label" for="doanhThuUocTinh">Doanh thu ước tính (VND)</label>
                                <input type="text" id="doanhThuUocTinh" name="doanhThuUocTinh" class="form-input" placeholder="Ví dụ: 200,000,000" autocomplete="off">
                            </div>
                        </div>

                        <!-- Người sở hữu (Data Scope Enforcement AC1, AC4) -->
                        <div class="form-group">
                            <label class="form-label">Người sở hữu hồ sơ (AC1, AC4)</label>
                            <c:choose>
                                <c:when test="${coQuyenChonOwner and not empty dsNhanVienSoHuu}">
                                    <select id="nguoiSoHuuId" name="nguoiSoHuuId" class="form-select">
                                        <c:forEach var="nv" items="${dsNhanVienSoHuu}">
                                            <option value="${nv.id}" ${nv.id == (not empty currentUser ? currentUser.id : sessionScope.nguoiDung.id) ? 'selected' : ''}>
                                                <c:out value="${nv.hoTen}" /> - <c:out value="${nv.email}" /> (<c:out value="${nv.tenNhom}" />)
                                            </option>
                                        </c:forEach>
                                    </select>
                                    <small style="color: var(--slate-500); display: block; margin-top: 4px;">
                                        Trưởng nhóm / Giám đốc / Quản trị viên có thể gán người phụ trách cho nhân viên trong nhóm.
                                    </small>
                                </c:when>
                                <c:otherwise>
                                    <div class="form-readonly-badge">
                                        <span class="material-symbols-outlined icon-sm" aria-hidden="true">person</span>
                                        <span>Chủ sở hữu: <strong><c:out value="${not empty currentUser ? currentUser.hoTen : sessionScope.nguoiDung.hoTen}" /></strong> &bull; <c:out value="${not empty currentUser ? currentUser.tenNhom : sessionScope.nguoiDung.tenNhomKinhDoanh}" /></span>
                                    </div>
                                    <small style="color: var(--slate-500); display: block; margin-top: 4px;">
                                        Nhân viên kinh doanh tự động sở hữu khách hàng do mình tạo (Phạm vi cá nhân).
                                    </small>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <!-- Ghi chú chi tiết -->
                        <div class="form-group">
                            <label class="form-label" for="moTaChiTiet">Ghi chú / Mô tả chi tiết</label>
                            <textarea id="moTaChiTiet" name="moTaChiTiet" class="form-textarea" rows="3" placeholder="Nhập thêm thông tin nhu cầu, liên hệ, ghi chú ban đầu..."></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline" id="btnHuyThemKhachHang">Hủy bỏ</button>
                        <button type="submit" class="btn btn-primary" id="btnXacNhanThemKhachHang">
                            <span class="material-symbols-outlined" aria-hidden="true">save</span>
                            <span>Lưu Khách Hàng</span>
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </main>

    <script src="${pageContext.request.contextPath}/assets/js/navigation.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/session-keep-alive.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/khach-hang.js"></script>
</body>
</html>
