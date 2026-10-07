<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Danh mục khách hàng, tìm kiếm & lọc đa điều kiện, lưu bộ lọc hay dùng - Hệ thống CRM Bán Hàng">
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

    <main class="crm-main-content" id="crm-main-content">
        <div class="page-container">

            <!-- Breadcrumb điều hướng chuẩn hệ thống -->
            <nav class="crm-breadcrumb" aria-label="Đường dẫn điều hướng">
                <a href="${pageContext.request.contextPath}/dieu-huong" class="breadcrumb-item">Trang chủ</a>
                <span class="breadcrumb-separator">/</span>
                <span class="breadcrumb-item active" aria-current="page">Danh mục khách hàng</span>
            </nav>

            <!-- Tiêu đề trang & Thanh công cụ danh sách khách hàng -->
            <header class="crm-page-header">
                <div class="crm-header-main">
                    <div class="crm-title-area">
                        <div class="crm-icon-badge" aria-hidden="true">
                            <span class="material-symbols-outlined" style="font-size: 26px;">contacts</span>
                        </div>
                        <div>
                            <h1 class="crm-page-title">Danh Mục Khách Hàng</h1>
                            <p class="crm-page-subtitle">Tìm kiếm, lọc đa điều kiện và lưu bộ lọc dựng nhanh danh sách khách hàng cần gọi trong tuần (Story S3-07)</p>
                        </div>
                    </div>
                    <div class="crm-header-actions">
                        <button type="button" class="btn btn-outline" id="btnExportExcel" title="Tải danh sách khách hàng dưới dạng file Excel (.xlsx)">
                            <span class="material-symbols-outlined" aria-hidden="true">download</span>
                            <span>Xuất Excel</span>
                        </button>
                        <button type="button" class="btn btn-primary" id="btnThemKhachHang">
                            <span class="material-symbols-outlined" aria-hidden="true">person_add</span>
                            <span>Thêm Khách Hàng</span>
                        </button>
                    </div>
                </div>
            </header>

            <!-- Thông báo kết quả thao tác -->
            <c:if test="${not empty thongBaoThanhCong}">
                <div class="alert alert-success" id="alertSuccess" role="alert">
                    <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                    <span><c:out value="${thongBaoThanhCong}" /></span>
                </div>
            </c:if>
            <c:if test="${not empty thongBaoCanhBao}">
                <div class="alert alert-warning" id="alertWarning" role="alert">
                    <span class="material-symbols-outlined" aria-hidden="true">warning</span>
                    <span><c:out value="${thongBaoCanhBao}" /></span>
                </div>
            </c:if>
            <c:if test="${not empty thongBaoLoi}">
                <div class="alert alert-danger" id="alertError" role="alert">
                    <span class="material-symbols-outlined" aria-hidden="true">error</span>
                    <span><c:out value="${thongBaoLoi}" /></span>
                </div>
            </c:if>

            <!-- Story S1-02: Khu vực soạn thảo ghi chú cuộc gặp (Ngồi ở quán cà phê không bị mất ghi chú) -->
            <div class="card">
                <div class="card-title">
                    <div class="card-title-left">
                        <span class="material-symbols-outlined crm-icon-primary" aria-hidden="true">edit_note</span>
                        <span>Ghi chú cuộc gặp khách hàng (Duy trì phiên & Tự động lưu)</span>
                    </div>
                    <div style="display: flex; align-items: center; gap: 12px; flex-wrap: wrap;">
                        <!-- Story S1-02: Thanh trạng thái phiên đăng nhập bảo mật và đếm ngược -->
                        <div class="session-indicator" id="session-indicator-box" title="Trạng thái phiên đăng nhập bảo mật">
                            <span class="session-dot active" id="session-status-dot"></span>
                            <span id="session-status-text">Phiên hoạt động</span>
                            <span class="session-countdown" id="session-countdown-timer" title="Thời gian phiên còn lại">30:00</span>
                        </div>
                        <span class="badge badge-info">
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

            <!-- Story S3-07: Khung Tìm Kiếm, Lọc Đa Điều Kiện & Quản Lý Bộ Lọc Đã Lưu -->
            <section class="filter-card" id="filterCard" aria-label="Tìm kiếm và lọc khách hàng">

                <!-- Thanh quản lý Bộ lọc đã lưu (AC3) -->
                <div class="saved-filters-bar">
                    <div class="saved-filters-group">
                        <span class="saved-filter-label">
                            <span class="material-symbols-outlined crm-icon-primary" aria-hidden="true">bookmark</span>
                            <label for="selectBoLocDaLuu">Bộ lọc đã lưu:</label>
                        </span>
                        <select id="selectBoLocDaLuu" class="saved-filter-select" onchange="chuyenBoLoc(this.value)">
                            <option value="">-- Chọn bộ lọc đã lưu --</option>
                            <c:forEach var="bl" items="${dsBoLocDaLuu}">
                                <option value="${bl.id}" ${not empty boLocHienTai and boLocHienTai.boLocId == bl.id ? 'selected' : ''}>
                                    <c:out value="${bl.tenBoLoc}" /> ${bl.macDinh ? ' [Mặc định]' : ''}
                                </option>
                            </c:forEach>
                        </select>

                        <c:if test="${not empty boLocHienTai and not empty boLocHienTai.boLocId}">
                            <span class="saved-filter-active-pill" title="Bộ lọc đang được áp dụng">
                                <span class="material-symbols-outlined icon-xs" aria-hidden="true">check</span>
                                <c:out value="${boLocHienTai.tenBoLoc}" />
                            </span>
                            <c:if test="${not boLocHienTai.macDinh}">
                                <button type="button" class="btn btn-outline btn-sm" id="btnDatMacDinhHienTai" title="Đặt làm bộ lọc mặc định khi mở danh sách">
                                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">star</span> Đặt làm mặc định
                                </button>
                            </c:if>
                            <button type="button" class="btn btn-danger-outline btn-sm" id="btnXoaBoLocHienTai" title="Xóa bộ lọc đã lưu này">
                                <span class="material-symbols-outlined icon-xs" aria-hidden="true">delete</span> Xóa bộ lọc
                            </button>
                        </c:if>
                    </div>
                    <div>
                        <button type="button" class="btn btn-outline" id="btnMoModalLuuBoLoc">
                            <span class="material-symbols-outlined icon-sm" aria-hidden="true">bookmark_add</span>
                            <span>Lưu bộ lọc hiện tại</span>
                        </button>
                    </div>
                </div>

                <!-- Form tìm kiếm & Lọc đa điều kiện (AC1, AC2) -->
                <form id="formLocKhachHang" method="GET" action="${pageContext.request.contextPath}/khach-hang">
                    <input type="hidden" name="phamVi" value="${param.phamVi != null ? param.phamVi : (not empty phamViHienTai ? phamViHienTai.ma : '')}">
                    <c:if test="${not empty boLocHienTai and not empty boLocHienTai.boLocId}">
                        <input type="hidden" name="boLocId" value="${boLocHienTai.boLocId}">
                    </c:if>

                    <!-- AC2: Tìm theo từ khóa tổng hợp -->
                    <div class="quick-search-wrapper">
                        <span class="material-symbols-outlined quick-search-icon" aria-hidden="true">search</span>
                        <input type="text" id="tuKhoa" name="tuKhoa" class="quick-search-input"
                               placeholder="Tìm kiếm nhanh theo tên công ty, mã số thuế hoặc số điện thoại người liên hệ..."
                               value="<c:out value='${not empty boLocHienTai ? boLocHienTai.tuKhoa : param.tuKhoa}' />" autocomplete="off">
                        <button type="button" id="btnClearTuKhoa" class="quick-search-clear" aria-label="Xóa từ khóa tìm kiếm" title="Xóa từ khóa">
                            <span class="material-symbols-outlined icon-sm" aria-hidden="true">close</span>
                        </button>
                    </div>

                    <!-- Lưới các trường tìm kiếm chi tiết & phân loại (AC1, AC2) -->
                    <div class="filter-grid">
                        <!-- AC2: Tìm theo Tên công ty -->
                        <div class="filter-field">
                            <label class="filter-label" for="tenCongTyFilter">Tên khách hàng / Công ty</label>
                            <input type="text" id="tenCongTyFilter" name="tenCongTy" class="filter-input"
                                   placeholder="Ví dụ: FPT, Viettel..."
                                   value="<c:out value='${not empty boLocHienTai ? boLocHienTai.tenCongTy : param.tenCongTy}' />" autocomplete="off">
                        </div>

                        <!-- AC2: Tìm theo Mã số thuế -->
                        <div class="filter-field">
                            <label class="filter-label" for="maSoThueFilter">Mã số thuế</label>
                            <input type="text" id="maSoThueFilter" name="maSoThue" class="filter-input"
                                   placeholder="Ví dụ: 0101234567"
                                   value="<c:out value='${not empty boLocHienTai ? boLocHienTai.maSoThue : param.maSoThue}' />" autocomplete="off">
                        </div>

                        <!-- AC2: Tìm theo SĐT người liên hệ -->
                        <div class="filter-field">
                            <label class="filter-label" for="soDienThoaiFilter">SĐT người liên hệ</label>
                            <input type="text" id="soDienThoaiFilter" name="soDienThoai" class="filter-input"
                                   placeholder="Ví dụ: 0912..."
                                   value="<c:out value='${not empty boLocHienTai ? boLocHienTai.soDienThoai : param.soDienThoai}' />" autocomplete="off">
                        </div>

                        <!-- AC1: Lọc theo Trạng thái -->
                        <div class="filter-field">
                            <label class="filter-label" for="trangThaiFilter">Trạng thái khách hàng</label>
                            <select id="trangThaiFilter" name="trangThai" class="filter-select">
                                <option value="">-- Tất cả trạng thái --</option>
                                <c:forEach var="tt" items="${dsTrangThai}">
                                    <option value="${tt.ma}" ${not empty boLocHienTai and (boLocHienTai.trangThai == tt.ma or boLocHienTai.trangThai == tt.tenHienThi) ? 'selected' : ''}>
                                        <c:out value="${tt.tenHienThi}" />
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <!-- AC1: Lọc theo Ngành nghề -->
                        <div class="filter-field">
                            <label class="filter-label" for="nganhNgheIdFilter">Ngành nghề</label>
                            <select id="nganhNgheIdFilter" name="nganhNgheId" class="filter-select">
                                <option value="">-- Tất cả ngành nghề --</option>
                                <c:forEach var="nn" items="${dsNganhNghe}">
                                    <option value="${nn.id}" ${not empty boLocHienTai and boLocHienTai.nganhNgheId == nn.id ? 'selected' : ''}>
                                        <c:out value="${nn.tenMuc}" />
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <!-- AC1: Lọc theo Quy mô -->
                        <div class="filter-field">
                            <label class="filter-label" for="quyMoIdFilter">Quy mô doanh nghiệp</label>
                            <select id="quyMoIdFilter" name="quyMoId" class="filter-select">
                                <option value="">-- Tất cả quy mô --</option>
                                <c:forEach var="qm" items="${dsQuyMo}">
                                    <option value="${qm.id}" ${not empty boLocHienTai and boLocHienTai.quyMoId == qm.id ? 'selected' : ''}>
                                        <c:out value="${qm.tenMuc}" />
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <!-- AC1: Lọc theo Khu vực -->
                        <div class="filter-field">
                            <label class="filter-label" for="khuVucIdFilter">Khu vực địa lý</label>
                            <select id="khuVucIdFilter" name="khuVucId" class="filter-select">
                                <option value="">-- Tất cả khu vực --</option>
                                <c:forEach var="kv" items="${dsKhuVuc}">
                                    <option value="${kv.id}" ${not empty boLocHienTai and boLocHienTai.khuVucId == kv.id ? 'selected' : ''}>
                                        <c:out value="${kv.tenKhuVuc}" />
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <!-- AC1: Lọc theo Người sở hữu (Nếu có quyền) -->
                        <c:if test="${coQuyenChonOwner}">
                            <div class="filter-field">
                                <label class="filter-label" for="nguoiSoHuuIdFilter">Người sở hữu / Phụ trách</label>
                                <select id="nguoiSoHuuIdFilter" name="nguoiSoHuuId" class="filter-select">
                                    <option value="">-- Tất cả nhân viên --</option>
                                    <c:forEach var="nv" items="${dsNguoiSoHuu}">
                                        <option value="${nv.id}" ${not empty boLocHienTai and boLocHienTai.nguoiSoHuuId == nv.id ? 'selected' : ''}>
                                            <c:out value="${nv.hoTen}" /> (${nv.email})
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                        </c:if>
                    </div>

                    <!-- Thanh thao tác Lọc & Hiển thị các tiêu chí đang áp dụng -->
                    <div class="filter-actions">
                        <div class="filter-actions-left">
                            <c:if test="${not empty boLocHienTai and boLocHienTai.coDieuKienLoc()}">
                                <div class="active-filter-chips" id="activeFilterChips">
                                    <span style="font-size: 12px; font-weight: 700; color: var(--slate-600); text-transform: uppercase;">Đang lọc:</span>
                                    <c:if test="${not empty boLocHienTai.tuKhoa}">
                                        <span class="filter-chip">
                                            Từ khóa: "<c:out value='${boLocHienTai.tuKhoa}' />"
                                            <button type="button" class="filter-chip-remove" data-field="tuKhoa" aria-label="Xóa điều kiện từ khóa" title="Xóa từ khóa">&times;</button>
                                        </span>
                                    </c:if>
                                    <c:if test="${not empty boLocHienTai.tenCongTy}">
                                        <span class="filter-chip">
                                            Tên: "<c:out value='${boLocHienTai.tenCongTy}' />"
                                            <button type="button" class="filter-chip-remove" data-field="tenCongTy" aria-label="Xóa điều kiện tên công ty" title="Xóa tên công ty">&times;</button>
                                        </span>
                                    </c:if>
                                    <c:if test="${not empty boLocHienTai.maSoThue}">
                                        <span class="filter-chip">
                                            MST: <c:out value='${boLocHienTai.maSoThue}' />
                                            <button type="button" class="filter-chip-remove" data-field="maSoThue" aria-label="Xóa điều kiện mã số thuế" title="Xóa mã số thuế">&times;</button>
                                        </span>
                                    </c:if>
                                    <c:if test="${not empty boLocHienTai.soDienThoai}">
                                        <span class="filter-chip">
                                            SĐT: <c:out value='${boLocHienTai.soDienThoai}' />
                                            <button type="button" class="filter-chip-remove" data-field="soDienThoai" aria-label="Xóa điều kiện số điện thoại" title="Xóa SĐT">&times;</button>
                                        </span>
                                    </c:if>
                                    <c:if test="${not empty boLocHienTai.trangThai}">
                                        <span class="filter-chip">
                                            Trạng thái: <c:out value='${boLocHienTai.trangThai}' />
                                            <button type="button" class="filter-chip-remove" data-field="trangThai" aria-label="Xóa điều kiện trạng thái" title="Xóa trạng thái">&times;</button>
                                        </span>
                                    </c:if>
                                    <c:if test="${not empty boLocHienTai.nganhNgheId and boLocHienTai.nganhNgheId > 0}">
                                        <c:forEach var="nn" items="${dsNganhNghe}">
                                            <c:if test="${nn.id == boLocHienTai.nganhNgheId}">
                                                <span class="filter-chip">
                                                    Ngành: <c:out value='${nn.tenMuc}' />
                                                    <button type="button" class="filter-chip-remove" data-field="nganhNgheId" aria-label="Xóa điều kiện ngành nghề" title="Xóa ngành nghề">&times;</button>
                                                </span>
                                            </c:if>
                                        </c:forEach>
                                    </c:if>
                                    <c:if test="${not empty boLocHienTai.quyMoId and boLocHienTai.quyMoId > 0}">
                                        <c:forEach var="qm" items="${dsQuyMo}">
                                            <c:if test="${qm.id == boLocHienTai.quyMoId}">
                                                <span class="filter-chip">
                                                    Quy mô: <c:out value='${qm.tenMuc}' />
                                                    <button type="button" class="filter-chip-remove" data-field="quyMoId" aria-label="Xóa điều kiện quy mô" title="Xóa quy mô">&times;</button>
                                                </span>
                                            </c:if>
                                        </c:forEach>
                                    </c:if>
                                    <c:if test="${not empty boLocHienTai.khuVucId and boLocHienTai.khuVucId > 0}">
                                        <c:forEach var="kv" items="${dsKhuVuc}">
                                            <c:if test="${kv.id == boLocHienTai.khuVucId}">
                                                <span class="filter-chip">
                                                    Khu vực: <c:out value='${kv.tenKhuVuc}' />
                                                    <button type="button" class="filter-chip-remove" data-field="khuVucId" aria-label="Xóa điều kiện khu vực" title="Xóa khu vực">&times;</button>
                                                </span>
                                            </c:if>
                                        </c:forEach>
                                    </c:if>
                                    <c:if test="${not empty boLocHienTai.nguoiSoHuuId and boLocHienTai.nguoiSoHuuId > 0}">
                                        <c:forEach var="nv" items="${dsNguoiSoHuu}">
                                            <c:if test="${nv.id == boLocHienTai.nguoiSoHuuId}">
                                                <span class="filter-chip">
                                                    Phụ trách: <c:out value='${nv.hoTen}' />
                                                    <button type="button" class="filter-chip-remove" data-field="nguoiSoHuuId" aria-label="Xóa điều kiện người phụ trách" title="Xóa người phụ trách">&times;</button>
                                                </span>
                                            </c:if>
                                        </c:forEach>
                                    </c:if>
                                </div>
                            </c:if>
                        </div>
                        <div class="filter-actions-right">
                            <a href="${pageContext.request.contextPath}/khach-hang?reset=1" class="btn btn-outline" title="Xóa tất cả điều kiện và quay về mặc định">
                                <span class="material-symbols-outlined icon-sm" aria-hidden="true">restart_alt</span>
                                <span>Đặt lại bộ lọc</span>
                            </a>
                            <button type="submit" class="btn btn-primary">
                                <span class="material-symbols-outlined icon-sm" aria-hidden="true">search</span>
                                <span>Tìm kiếm & Lọc</span>
                            </button>
                        </div>
                    </div>
                </form>
            </section>

            <!-- Bảng danh sách khách hàng lọc theo Data Scope & Tiêu chí Story S3-07 -->
            <section class="table-card" aria-label="Bảng kết quả khách hàng">
                <div class="table-header-bar">
                    <div class="table-meta-info">
                        <span class="table-meta-item">
                            Phạm vi dữ liệu: <strong><c:out value="${not empty phamViHienTai ? phamViHienTai.tenHienThi : 'Của tôi'}" /></strong>
                        </span>
                        <span>&bull;</span>
                        <span class="table-meta-item">
                            Đang hiển thị: <strong><c:out value="${not empty tongSoKhachHang ? tongSoKhachHang : 0}" /></strong> khách hàng
                        </span>
                        <c:if test="${not empty boLocHienTai and not empty boLocHienTai.tenBoLoc}">
                            <span>&bull;</span>
                            <span class="table-meta-item">
                                Bộ lọc: <span style="font-weight: 700; color: var(--primary);"><c:out value="${boLocHienTai.tenBoLoc}" /></span>
                            </span>
                        </c:if>
                    </div>
                    <div class="table-header-links">
                        <c:set var="userHienTai" value="${not empty nguoiDung ? nguoiDung : sessionScope.nguoiDung}" />
                        <c:set var="laQuanTriDanhMuc" value="${coQuyenDanhMuc or (not empty userHienTai and (userHienTai.coVaiTro('ADMIN') or userHienTai.coVaiTro('DIRECTOR')))}" />
                        <c:if test="${!laQuanTriDanhMuc}">
                            <a href="${pageContext.request.contextPath}/san-pham" class="table-header-link" id="linkSanPhamBangGia">
                                <span>Sản phẩm & Bảng giá</span>
                                <span class="material-symbols-outlined icon-xs" aria-hidden="true">arrow_forward</span>
                            </a>
                        </c:if>
                        <a href="${pageContext.request.contextPath}/phan-quyen-du-lieu" class="table-header-link">
                            <span>Quản lý 4 nghiệp vụ Data Scope</span>
                            <span class="material-symbols-outlined icon-xs" aria-hidden="true">arrow_forward</span>
                        </a>
                    </div>
                </div>

                <div class="crm-table-wrapper">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th style="width: 100px;">Mã KH</th>
                                <th style="min-width: 220px;">Tên Khách Hàng / Công Ty</th>
                                <th style="width: 120px;">Mã Số Thuế</th>
                                <th style="min-width: 200px;">Đầu Mối Liên Hệ & SĐT Gọi Nhanh</th>
                                <th style="width: 160px;">Ngành Nghề / Quy Mô</th>
                                <th style="width: 130px;">Khu Vực</th>
                                <th style="min-width: 160px;">Người Phụ Trách</th>
                                <th style="width: 130px;">Trạng Thái</th>
                                <th style="width: 70px; text-align: center;">Thao Tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <%-- Ưu tiên hiển thị danh sách KhachHang model đầy đủ thông tin liên hệ và SĐT --%>
                                <c:when test="${not empty dsKhachHangModel}">
                                    <c:forEach var="kh" items="${dsKhachHangModel}">
                                        <tr>
                                            <td class="font-mono"><c:out value="${not empty kh.maKhachHang ? kh.maKhachHang : '-'}" /></td>
                                            <td>
                                                <div class="customer-name"><c:out value="${kh.tenCongTy}" /></div>
                                                <c:if test="${not empty kh.website}">
                                                    <div class="customer-sub">
                                                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">language</span>
                                                        <a href="https://${kh.website}" target="_blank" rel="noopener noreferrer"><c:out value="${kh.website}" /></a>
                                                    </div>
                                                </c:if>
                                            </td>
                                            <td class="font-mono">
                                                <c:out value="${not empty kh.maSoThue ? kh.maSoThue : '-'}" />
                                            </td>
                                            <td>
                                                <%-- SĐT gọi nhanh phục vụ Sales Rep dựng danh sách cần gọi trong tuần (S3-07) --%>
                                                <c:choose>
                                                    <c:when test="${not empty kh.soDienThoaiLienHe}">
                                                        <div class="contact-person-name">
                                                            <c:out value="${not empty kh.tenNguoiLienHeChinh ? kh.tenNguoiLienHeChinh : 'Người liên hệ'}" />
                                                        </div>
                                                        <div style="margin-top: 4px;">
                                                            <a href="tel:${kh.soDienThoaiLienHe}" class="phone-call-badge" title="Bấm để gọi ngay cho khách hàng">
                                                                <span class="material-symbols-outlined icon-xs" aria-hidden="true">call</span>
                                                                <c:out value="${kh.soDienThoaiLienHe}" />
                                                            </a>
                                                        </div>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span style="color: var(--slate-400); font-size: 13px;">Chưa có SĐT</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <div style="font-weight: 500;"><c:out value="${not empty kh.tenNganhNghe ? kh.tenNganhNghe : '-'}" /></div>
                                                <div style="font-size: 12px; color: var(--slate-400);"><c:out value="${not empty kh.tenQuyMo ? kh.tenQuyMo : ''}" /></div>
                                            </td>
                                            <td><c:out value="${not empty kh.tenKhuVuc ? kh.tenKhuVuc : '-'}" /></td>
                                            <td>
                                                <div style="font-weight: 600; color: var(--slate-800);"><c:out value="${kh.tenNguoiSoHuu}" /></div>
                                                <div style="font-size: 12px; color: var(--slate-400);"><c:out value="${kh.tenNhomKinhDoanh}" /></div>
                                            </td>
                                            <td>
                                                <span class="badge ${kh.trangThai == 'TIEM_NANG' ? 'badge-info' : (kh.trangThai == 'DANG_GIAO_DICH' or kh.trangThai == 'DANG_TIEP_CAN' ? 'badge-warning' : (kh.trangThai == 'KHACH_HANG' ? 'badge-success' : 'badge-danger'))}">
                                                    <c:out value="${kh.trangThaiHienThi}" />
                                                </span>
                                            </td>
                                            <td style="text-align: center;">
                                                <a href="${pageContext.request.contextPath}/chi-tiet-ban-ghi?id=${kh.id}" class="crm-btn-action" title="Xem chi tiết khách hàng" aria-label="Xem chi tiết khách hàng ${kh.tenCongTy}">
                                                    <span class="material-symbols-outlined" aria-hidden="true">visibility</span>
                                                </a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <%-- Fallback danh sách BanGhiNghiepVuDTO nếu dsKhachHangModel chưa nạp --%>
                                <c:when test="${not empty danhSachKhachHang}">
                                    <c:forEach var="kh" items="${danhSachKhachHang}">
                                        <tr>
                                            <td class="font-mono"><c:out value="${kh.maBanGhi}" /></td>
                                            <td>
                                                <div class="customer-name"><c:out value="${kh.tieuDe}" /></div>
                                                <div class="customer-sub"><c:out value="${kh.moTaChiTiet}" /></div>
                                            </td>
                                            <td>-</td>
                                            <td>-</td>
                                            <td><c:out value="${kh.giaTri}" /></td>
                                            <td>-</td>
                                            <td><c:out value="${kh.tenNguoiPhuTrach}" /></td>
                                            <td><span class="badge badge-success"><c:out value="${kh.trangThai}" /></span></td>
                                            <td style="text-align: center;">
                                                <a href="${pageContext.request.contextPath}/chi-tiet-ban-ghi?id=${kh.id}" class="crm-btn-action" title="Xem chi tiết khách hàng" aria-label="Xem chi tiết khách hàng">
                                                    <span class="material-symbols-outlined" aria-hidden="true">visibility</span>
                                                </a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="9" class="empty-state-cell">
                                            <div class="empty-state-wrap">
                                                <div class="empty-state-icon" aria-hidden="true">
                                                    <span class="material-symbols-outlined icon-lg">search_off</span>
                                                </div>
                                                <h3 class="empty-state-title">Không tìm thấy khách hàng nào phù hợp</h3>
                                                <p class="empty-state-desc">Không có khách hàng nào thỏa mãn các điều kiện tìm kiếm và lọc hiện tại. Hãy thử chọn tiêu chí khác hoặc đặt lại bộ lọc.</p>
                                                <a href="${pageContext.request.contextPath}/khach-hang?reset=1" class="btn btn-outline" style="margin-top: 4px;">
                                                    <span class="material-symbols-outlined icon-sm" aria-hidden="true">restart_alt</span>
                                                    <span>Đặt lại bộ lọc</span>
                                                </a>
                                            </div>
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </section>

            <!-- Modal Lưu Bộ Lọc Hay Dùng (Story S3-07, AC3) -->
            <div class="modal-backdrop" id="modalLuuBoLoc" style="display: none;" role="dialog" aria-modal="true" aria-labelledby="modalLuuBoLocTieuDe" aria-hidden="true">
                <div class="modal-card">
                    <div class="modal-header">
                        <div class="modal-title-wrap">
                            <span class="material-symbols-outlined crm-icon-primary" aria-hidden="true">bookmark_add</span>
                            <div>
                                <h2 class="modal-title" id="modalLuuBoLocTieuDe">Lưu Bộ Lọc Hay Dùng</h2>
                                <p class="modal-subtitle">Đặt tên cho bộ lọc hiện tại để dựng nhanh danh sách gọi trong tuần</p>
                            </div>
                        </div>
                        <button type="button" class="modal-close-btn" id="btnDongModalLuuBoLoc" aria-label="Đóng modal" title="Đóng">
                            <span class="material-symbols-outlined" aria-hidden="true">close</span>
                        </button>
                    </div>
                    <form id="formLuuBoLoc" method="POST" action="${pageContext.request.contextPath}/khach-hang">
                        <input type="hidden" name="action" value="luu-bo-loc">
                        <!-- Đồng bộ các tiêu chí lọc đang được thiết lập -->
                        <input type="hidden" name="tuKhoa" value="<c:out value='${not empty boLocHienTai ? boLocHienTai.tuKhoa : param.tuKhoa}' />">
                        <input type="hidden" name="tenCongTy" value="<c:out value='${not empty boLocHienTai ? boLocHienTai.tenCongTy : param.tenCongTy}' />">
                        <input type="hidden" name="maSoThue" value="<c:out value='${not empty boLocHienTai ? boLocHienTai.maSoThue : param.maSoThue}' />">
                        <input type="hidden" name="soDienThoai" value="<c:out value='${not empty boLocHienTai ? boLocHienTai.soDienThoai : param.soDienThoai}' />">
                        <input type="hidden" name="trangThai" value="<c:out value='${not empty boLocHienTai ? boLocHienTai.trangThai : param.trangThai}' />">
                        <input type="hidden" name="nganhNgheId" value="<c:out value='${not empty boLocHienTai ? boLocHienTai.nganhNgheId : param.nganhNgheId}' />">
                        <input type="hidden" name="quyMoId" value="<c:out value='${not empty boLocHienTai ? boLocHienTai.quyMoId : param.quyMoId}' />">
                        <input type="hidden" name="khuVucId" value="<c:out value='${not empty boLocHienTai ? boLocHienTai.khuVucId : param.khuVucId}' />">
                        <input type="hidden" name="nguoiSoHuuId" value="<c:out value='${not empty boLocHienTai ? boLocHienTai.nguoiSoHuuId : param.nguoiSoHuuId}' />">

                        <div class="modal-body">
                            <!-- Hộp tóm tắt các điều kiện sẽ được lưu -->
                            <div class="filter-summary-box">
                                <div class="filter-summary-title">
                                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">filter_list</span>
                                    <span>Các tiêu chí lọc sẽ được lưu:</span>
                                </div>
                                <div class="filter-summary-tags" id="modalFilterSummaryContent">
                                    <!-- Được render động từ khach-hang.js -->
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="form-label" for="tenBoLocInput">Tên bộ lọc <span class="required">*</span></label>
                                <input type="text" id="tenBoLocInput" name="tenBoLoc" class="form-input"
                                       placeholder="Ví dụ: Khách CNTT cần gọi thứ 3, Khách VIP Hà Nội..."
                                       maxlength="150" required autocomplete="off">
                                <div class="form-error-inline" id="tenBoLocError">Vui lòng nhập tên cho bộ lọc (tối đa 150 ký tự).</div>
                            </div>
                            <div class="form-group">
                                <label class="form-checkbox-label">
                                    <input type="checkbox" id="macDinhCheckbox" name="macDinh" value="1">
                                    <span>Đặt làm bộ lọc mặc định khi mở Danh mục khách hàng</span>
                                </label>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-outline" id="btnHuyLuuBoLoc">Hủy bỏ</button>
                            <button type="submit" class="btn btn-primary" id="btnXacNhanLuuBoLoc">
                                <span class="material-symbols-outlined icon-sm" aria-hidden="true">save</span>
                                <span>Lưu Bộ Lọc</span>
                            </button>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Modal Thêm Khách Hàng Mới (Story S1-05) -->
            <div class="modal-backdrop" id="modalThemKhachHang" style="display: none;" role="dialog" aria-modal="true" aria-labelledby="modalThemKhachHangTieuDe" aria-hidden="true">
                <div class="modal-card">
                    <div class="modal-header">
                        <div class="modal-title-wrap">
                            <span class="material-symbols-outlined crm-icon-primary" aria-hidden="true">person_add</span>
                            <div>
                                <h2 class="modal-title" id="modalThemKhachHangTieuDe">Thêm Khách Hàng Mới</h2>
                                <p class="modal-subtitle">Tạo mới khách hàng thuộc phạm vi sở hữu của tài khoản hiện tại (S1-05)</p>
                            </div>
                        </div>
                        <button type="button" class="modal-close-btn" id="btnDongModalThemKhachHang" aria-label="Đóng modal" title="Đóng">
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
                                    <div class="form-group">
                                        <label class="form-label" for="maKhachHang">Mã khách hàng</label>
                                        <input type="text" id="maKhachHang" name="maKhachHang" class="form-input" placeholder="Tự sinh nếu để trống" autocomplete="off">
                                    </div>
                                </div>
                                <div class="form-col">
                                    <div class="form-group">
                                        <label class="form-label" for="doanhThuUocTinh">Doanh thu ước tính</label>
                                        <input type="text" id="doanhThuUocTinh" name="doanhThuUocTinh" class="form-input" placeholder="Ví dụ: 100,000,000 đ" autocomplete="off">
                                    </div>
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
                                <span class="material-symbols-outlined icon-sm" aria-hidden="true">save</span>
                                <span>Lưu Khách Hàng</span>
                            </button>
                        </div>
                    </form>
                </div>
            </div>

        </div>
    </main>

    <script src="${pageContext.request.contextPath}/assets/js/navigation.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/session-keep-alive.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/khach-hang.js"></script>
</body>
</html>
