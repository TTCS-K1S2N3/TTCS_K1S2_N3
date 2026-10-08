<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Danh mục khách hàng, tìm kiếm & lọc đa điều kiện, lưu bộ lọc, chăm sóc định kỳ & phát hiện trùng lặp - CRM Bán Hàng">
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

        <!-- Tiêu đề trang & Thanh công cụ chung -->
        <div class="page-header">
            <div>
                <h1 class="page-title">Khách Hàng & Chăm Sóc Định Kỳ</h1>
                <p class="page-subtitle">Quản lý danh mục khách hàng và theo dõi chăm sóc định kỳ cho khách đã ký hợp đồng</p>
            </div>
            <div class="page-actions">
                <button type="button" class="btn btn-outline" id="btnChuyenTabTrung" title="Xem cảnh báo và gộp khách hàng trùng lặp (Story S3-04)">
                    <span class="material-symbols-outlined" aria-hidden="true">call_merge</span>
                    <span>Kiểm Tra Trùng & Gộp</span>
                </button>
                <a href="${pageContext.request.contextPath}/khach-hang/import-excel" class="btn btn-outline" id="btnImportExcel" title="Nhập danh sách khách hàng từ file Excel (.xlsx, .xls)">
                    <span class="material-symbols-outlined" aria-hidden="true">upload_file</span>
                    <span>Nhập Excel</span>
                </a>
                <a href="${pageContext.request.contextPath}/yeu-cau-ho-tro" class="btn btn-outline" id="btnYeuCauHoTro" title="Quản lý yêu cầu hỗ trợ sau bán và khách hàng có rủi ro rời bỏ (Story S3-08)">
                    <span class="material-symbols-outlined" aria-hidden="true">support_agent</span>
                    <span>Yêu Cầu Hỗ Trợ & Cờ Rủi Ro</span>
                </a>
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

                <c:if test="${not empty khachHangChiTiet or not empty banGhiChiTiet}">
            <c:set var="detailKH" value="${not empty khachHangChiTiet ? khachHangChiTiet : null}" />
            <c:set var="detailBG" value="${not empty banGhiChiTiet ? banGhiChiTiet : null}" />
            <c:set var="tenKH" value="${not empty detailKH ? detailKH.tenCongTy : detailBG.tieuDe}" />
            <c:set var="maKH" value="${not empty detailKH ? detailKH.maKhachHang : detailBG.maBanGhi}" />
            <c:set var="statusKH" value="${not empty detailKH ? detailKH.trangThaiHienThi : detailBG.trangThai}" />

            <div class="customer-detail-card" id="customerDetailCard">
                <div class="customer-detail-header">
                    <div class="customer-detail-title-group">
                        <h2>
                            <span class="material-symbols-outlined" aria-hidden="true" style="color: var(--primary);">business</span>
                            <span><c:out value="${tenKH}" /></span>
                        </h2>
                        <p>
                            Mã khách hàng: <code class="mst-badge"><c:out value="${maKH}" /></code>
                            &bull; Hồ sơ doanh nghiệp chuẩn (Story S3-01)
                        </p>
                    </div>
                    <div style="display: flex; align-items: center; gap: 10px; flex-wrap: wrap;">
                        <c:choose>
                            <c:when test="${statusKH == 'Tiềm năng' || statusKH == 'TIEM_NANG'}">
                                <span class="badge-status-tiem-nang"><span class="badge-dot" aria-hidden="true"></span>Tiềm năng</span>
                            </c:when>
                            <c:when test="${statusKH == 'Đang giao dịch' || statusKH == 'DANG_GIAO_DICH'}">
                                <span class="badge-status-dang-giao-dich"><span class="badge-dot" aria-hidden="true"></span>Đang giao dịch</span>
                            </c:when>
                            <c:when test="${statusKH == 'Khách hàng' || statusKH == 'KHACH_HANG'}">
                                <span class="badge-status-khach-hang"><span class="badge-dot" aria-hidden="true"></span>Khách hàng</span>
                            </c:when>
                            <c:when test="${statusKH == 'Ngừng hợp tác' || statusKH == 'NGUNG_HOP_TAC'}">
                                <span class="badge-status-ngung-hop-tac"><span class="badge-dot" aria-hidden="true"></span>Ngừng hợp tác</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge badge-info"><c:out value="${statusKH}" /></span>
                            </c:otherwise>
                        </c:choose>

                        <button type="button" class="btn btn-outline btn-sua-khach-hang" id="btnSuaTuDetail"
                            data-id="${not empty detailKH ? detailKH.id : detailBG.id}"
                            data-ten="<c:out value="${tenKH}" />"
                            data-makh="<c:out value="${maKH}" />"
                            data-mst="<c:out value="${not empty detailKH ? detailKH.maSoThue : ''}" />"
                            data-nganh="${not empty detailKH ? detailKH.nganhNgheId : ''}"
                            data-quymo="${not empty detailKH ? detailKH.quyMoId : ''}"
                            data-website="<c:out value="${not empty detailKH ? detailKH.website : ''}" />"
                            data-diachi="<c:out value="${not empty detailKH ? detailKH.diaChi : ''}" />"
                            data-trangthai="<c:out value="${not empty detailKH ? detailKH.trangThai : detailBG.trangThai}" />"
                            data-gia="<c:out value="${not empty detailKH ? detailKH.doanhThuUocTinh : detailBG.giaTri}" />"
                            data-nguoisohuu="${not empty detailKH ? detailKH.nguoiSoHuuId : detailBG.nguoiPhuTrachId}"
                            data-mota="<c:out value="${not empty detailKH ? detailKH.moTaChiTiet : detailBG.moTaChiTiet}" />">
                            <span class="material-symbols-outlined" aria-hidden="true">edit</span>
                            <span>Sửa hồ sơ</span>
                        </button>

                        <button type="button" class="btn btn-outline" id="btnDongDetailCard" title="Đóng khung chi tiết">
                            <span class="material-symbols-outlined" aria-hidden="true">close</span>
                            <span>Đóng</span>
                        </button>
                    </div>
                </div>

                <div class="customer-detail-body">
                    <div class="detail-grid">
                        <div class="detail-field-item">
                            <span class="detail-field-label">Mã khách hàng</span>
                            <span class="detail-field-value font-mono"><c:out value="${maKH}" /></span>
                        </div>
                        <div class="detail-field-item">
                            <span class="detail-field-label">Tên công ty / Khách hàng (AC1)</span>
                            <span class="detail-field-value" style="font-weight: 700; color: var(--slate-900);"><c:out value="${tenKH}" /></span>
                        </div>
                        <div class="detail-field-item">
                            <span class="detail-field-label">Mã số thuế (AC1, AC2)</span>
                            <span class="detail-field-value">
                                <c:choose>
                                    <c:when test="${not empty detailKH && not empty detailKH.maSoThue}">
                                        <code class="mst-badge"><c:out value="${detailKH.maSoThue}" /></code>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="text-muted-italic">Chưa khai báo</span>
                                    </c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                        <div class="detail-field-item">
                            <span class="detail-field-label">Ngành nghề (AC1)</span>
                            <span class="detail-field-value"><c:out value="${not empty detailKH && not empty detailKH.tenNganhNghe ? detailKH.tenNganhNghe : 'Chưa phân ngành'}" /></span>
                        </div>
                        <div class="detail-field-item">
                            <span class="detail-field-label">Quy mô doanh nghiệp (AC1)</span>
                            <span class="detail-field-value"><c:out value="${not empty detailKH && not empty detailKH.tenQuyMo ? detailKH.tenQuyMo : 'Chưa phân loại'}" /></span>
                        </div>
                        <div class="detail-field-item">
                            <span class="detail-field-label">Website (AC1)</span>
                            <span class="detail-field-value">
                                <c:choose>
                                    <c:when test="${not empty detailKH && not empty detailKH.website}">
                                        <a href="${detailKH.website.startsWith('http') ? detailKH.website : 'https://'.concat(detailKH.website)}" target="_blank" rel="noopener noreferrer" class="link-website">
                                            <c:out value="${detailKH.website}" />
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="text-muted-italic">Chưa khai báo</span>
                                    </c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                        <div class="detail-field-item">
                            <span class="detail-field-label">Địa chỉ trụ sở (AC1)</span>
                            <span class="detail-field-value"><c:out value="${not empty detailKH && not empty detailKH.diaChi ? detailKH.diaChi : 'Chưa cập nhật'}" /></span>
                        </div>
                        <div class="detail-field-item">
                            <span class="detail-field-label">Người sở hữu (AC1, AC4)</span>
                            <span class="detail-field-value"><c:out value="${not empty detailKH ? detailKH.tenNguoiSoHuu : detailBG.tenNguoiPhuTrach}" /></span>
                        </div>
                        <div class="detail-field-item">
                            <span class="detail-field-label">Nhóm kinh doanh (AC4)</span>
                            <span class="detail-field-value"><c:out value="${not empty detailKH ? detailKH.tenNhomKinhDoanh : detailBG.tenNhom}" /></span>
                        </div>
                        <div class="detail-field-item">
                            <span class="detail-field-label">Doanh thu ước tính</span>
                            <span class="detail-field-value"><c:out value="${not empty detailKH ? detailKH.doanhThuUocTinh : detailBG.giaTri}" /> VND</span>
                        </div>
                        <div class="detail-field-item">
                            <span class="detail-field-label">Trạng thái (AC3)</span>
                            <span class="detail-field-value"><c:out value="${statusKH}" /></span>
                        </div>
                        <div class="detail-field-item">
                            <span class="detail-field-label">Ngày tạo hồ sơ</span>
                            <span class="detail-field-value"><c:out value="${not empty detailKH ? detailKH.ngayTaoDinhDang : detailBG.ngayTao}" /></span>
                        </div>
                    </div>

                    <div class="detail-field-item">
                        <span class="detail-field-label" style="margin-bottom: 6px;">Mô tả / Ghi chú chi tiết</span>
                        <div class="detail-desc-box">
                            <c:set var="mota" value="${not empty detailKH ? detailKH.moTaChiTiet : detailBG.moTaChiTiet}" />
                            <c:choose>
                                <c:when test="${not empty mota}">
                                    <c:out value="${mota}" />
                                </c:when>
                                <c:otherwise>
                                    <span class="text-muted-italic">Chưa có mô tả chi tiết cho hồ sơ khách hàng này.</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </div>
        </c:if>


        <div class="crm-tabs-nav" role="tablist" aria-label="Phân hệ khách hàng">
            <button type="button" class="crm-tab-btn ${tabHienTai ne 'cham-soc' and tabHienTai ne 'trung' ? 'active' : ''}" id="tabBtnDanhSach" role="tab" aria-selected="${tabHienTai ne 'cham-soc' and tabHienTai ne 'trung' ? 'true' : 'false'}" aria-controls="paneDanhSach" data-tab="tat-ca">
                <span class="material-symbols-outlined" aria-hidden="true">group</span>
                <span>Tất cả khách hàng</span>
                <span class="crm-tab-badge"><c:out value="${not empty tongSoKhachHang ? tongSoKhachHang : 0}" /></span>
            </button>
            <button type="button" class="crm-tab-btn ${tabHienTai eq 'cham-soc' ? 'active' : ''}" id="tabBtnChamSoc" role="tab" aria-selected="${tabHienTai eq 'cham-soc' ? 'true' : 'false'}" aria-controls="paneChamSoc" data-tab="cham-soc">
                <span class="material-symbols-outlined" aria-hidden="true">support_agent</span>
                <span>Chăm sóc định kỳ</span>
                <span class="crm-tab-badge badge-amber" id="badgeSoKhachCanChamSoc">S3-09</span>
            </button>
            <button type="button" class="crm-tab-btn ${tabHienTai eq 'trung' ? 'active' : ''}" id="tabBtnTrungLap" role="tab" aria-selected="${tabHienTai eq 'trung' ? 'true' : 'false'}" aria-controls="paneTrungLap" data-tab="trung">
                <span class="material-symbols-outlined" aria-hidden="true">call_merge</span>
                <span>Cảnh báo trùng lặp & Gộp</span>
                <span class="crm-tab-badge badge-warning" id="tabBadgeSoCapTrung"><c:out value="${not empty soCapTrung ? soCapTrung : 0}" /></span>
            </button>
        </div>

        <!-- ===================================================================
             TAB 1: TẤT CẢ KHÁCH HÀNG (Bao gồm S1-02 và S1-05)
             =================================================================== -->
        <div class="crm-tab-pane ${tabHienTai ne 'cham-soc' and tabHienTai ne 'trung' ? 'active' : ''}" id="paneDanhSach" role="tabpanel" aria-labelledby="tabBtnDanhSach">
            <!-- Banner cảnh báo phát hiện trùng lặp trong danh mục (Story S3-04) -->
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

            <!-- Bảng danh sách khách hàng (Story S1-05, S3-01, S3-07) -->
            <div class="table-card">
                <div class="table-header-bar">
                    <div class="table-meta-info">
                        <span class="table-meta-item">
                            Tổng số: <strong style="color: var(--primary);"><c:out value="${tongSoKhachHang}" /></strong> khách hàng
                        </span>
                        <c:if test="${not empty boLocHienTai and not empty boLocHienTai.tenBoLoc}">
                            <span class="table-meta-item" style="border-left: 1px solid var(--slate-200); padding-left: 12px;">
                                Đang áp dụng bộ lọc: <strong style="color: var(--primary);"><c:out value="${boLocHienTai.tenBoLoc}" /></strong>
                            </span>
                        </c:if>
                    </div>
                    <div class="table-header-links">
                        <a href="${pageContext.request.contextPath}/khach-hang" class="table-header-link">
                            <span class="material-symbols-outlined icon-xs" aria-hidden="true">refresh</span>
                            Làm mới
                        </a>
                    </div>
                </div>

                <div class="table-responsive">
                    <table class="data-table" id="tableKhachHang">
                        <thead>
                            <tr>
                                <th style="width: 100px;">Mã KH</th>
                                <th style="min-width: 220px;">Tên Khách Hàng / Doanh Nghiệp</th>
                                <th style="width: 120px;">Mã Số Thuế</th>
                                <th style="min-width: 200px;">Đầu Mối Liên Hệ & SĐT Gọi Nhanh</th>
                                <th style="width: 160px;">Ngành Nghề / Quy Mô</th>
                                <th style="width: 120px;">Khu Vực</th>
                                <th style="min-width: 160px;">Người Phụ Trách</th>
                                <th style="width: 130px;">Trạng Thái</th>
                                <th style="width: 120px; text-align: center;">Thao Tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty dsKhachHangModel}">
                                    <c:forEach var="kh" items="${dsKhachHangModel}">
                                        <tr data-khach-hang-id="${kh.id}" ${kh.coRuiRo ? 'class="row-rui-ro"' : ''}>
                                            <td class="font-mono">
                                                <a href="${pageContext.request.contextPath}/khach-hang/360?id=${kh.id}" style="color: var(--primary); font-weight: 600;">
                                                    <c:out value="${not empty kh.maKhachHang ? kh.maKhachHang : ('KH-' += kh.id)}" />
                                                </a>
                                            </td>
                                            <td>
                                                <div class="customer-name" style="display: flex; align-items: center; gap: 6px; flex-wrap: wrap;">
                                                    <a href="${pageContext.request.contextPath}/khach-hang/360?id=${kh.id}" style="color: inherit; text-decoration: none; font-weight: 600;">
                                                        <c:out value="${kh.tenCongTy}" />
                                                    </a>
                                                    <c:if test="${kh.coRuiRo}">
                                                        <span class="badge badge-danger" title="Khách hàng có cờ rủi ro rời bỏ hoặc ticket tồn đọng">
                                                            <span class="material-symbols-outlined icon-xs" aria-hidden="true">warning</span>
                                                            Rủi ro
                                                        </span>
                                                    </c:if>
                                                </div>
                                                <c:if test="${not empty kh.congTyMeId}">
                                                    <div style="font-size: 12px; color: var(--slate-500); margin-top: 2px;">
                                                        <span class="material-symbols-outlined icon-xs" aria-hidden="true" style="vertical-align: middle;">account_tree</span>
                                                        Công ty mẹ: <c:out value="${kh.tenCongTyMe}" />
                                                    </div>
                                                </c:if>
                                                <c:if test="${not empty kh.website}">
                                                    <div class="customer-sub" style="margin-top: 2px;">
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
                                                <div style="display: flex; align-items: center; justify-content: center; gap: 6px;">
                                                    <a href="${pageContext.request.contextPath}/khach-hang/360?id=${kh.id}" class="crm-btn-action" title="Hồ sơ 360° khách hàng (S3-03)" aria-label="Xem chi tiết hồ sơ 360 khách hàng ${kh.tenCongTy}">
                                                        <span class="material-symbols-outlined" aria-hidden="true">visibility</span>
                                                    </a>
                                                    <button type="button" class="crm-btn-action btn-edit-customer btnSuaKhachHang btn-sua-khach-hang" title="Chỉnh sửa thông tin khách hàng"
                                                            data-id="${kh.id}"
                                                            data-ma="${kh.maKhachHang}"
                                                            data-makh="${kh.maKhachHang}"
                                                            data-ten="<c:out value="${kh.tenCongTy}" />"
                                                            data-mst="<c:out value="${kh.maSoThue}" />"
                                                            data-nganh="${kh.nganhNgheId}"
                                                            data-quymo="${kh.quyMoId}"
                                                            data-khuvuc="${kh.khuVucId}"
                                                            data-website="<c:out value="${kh.website}" />"
                                                            data-diachi="<c:out value="${kh.diaChi}" />"
                                                            data-sohuu="${kh.nguoiSoHuuId}"
                                                            data-nguoisohuu="${kh.nguoiSoHuuId}"
                                                            data-trangthai="${kh.trangThai}"
                                                            data-doanhthu="${kh.doanhThuUocTinh}"
                                                            data-gia="${kh.doanhThuUocTinh}"
                                                            data-mota="<c:out value="${kh.moTaChiTiet}" />">
                                                        <span class="material-symbols-outlined" aria-hidden="true">edit</span>
                                                    </button>
                                                </div>
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

                <!-- Phân trang danh sách khách hàng -->
                <c:if test="${tongSoTrang > 1}">
                    <div class="crm-pagination" style="display: flex; justify-content: space-between; align-items: center; padding: 14px 20px; border-top: 1px solid var(--slate-200); flex-wrap: wrap; gap: 12px;">
                        <span style="font-size: 13.5px; color: var(--slate-500);">
                            Hiển thị trang <strong>${trangHienTai}</strong> / <strong>${tongSoTrang}</strong> (Tổng số: ${tongSoKhachHang} khách hàng)
                        </span>
                        <div style="display: flex; gap: 6px;">
                            <c:if test="${trangHienTai > 1}">
                                <a href="${pageContext.request.contextPath}/khach-hang?trang=${trangHienTai - 1}&tuKhoa=${tuKhoaHienTai}&trangThai=${trangThaiHienTai}&nganhNgheId=${nganhNgheIdHienTai}&quyMoId=${quyMoIdHienTai}&boLocId=${boLocHienTai.boLocId}" class="btn btn-outline btn-sm">Trước</a>
                            </c:if>
                            <c:forEach var="p" begin="1" end="${tongSoTrang}">
                                <c:if test="${p == trangHienTai}">
                                    <span class="btn btn-primary btn-sm" style="font-weight: 700;">${p}</span>
                                </c:if>
                                <c:if test="${p != trangHienTai && (p == 1 || p == tongSoTrang || (p >= trangHienTai - 2 && p <= trangHienTai + 2))}">
                                    <a href="${pageContext.request.contextPath}/khach-hang?trang=${p}&tuKhoa=${tuKhoaHienTai}&trangThai=${trangThaiHienTai}&nganhNgheId=${nganhNgheIdHienTai}&quyMoId=${quyMoIdHienTai}&boLocId=${boLocHienTai.boLocId}" class="btn btn-outline btn-sm">${p}</a>
                                </c:if>
                            </c:forEach>
                            <c:if test="${trangHienTai < tongSoTrang}">
                                <a href="${pageContext.request.contextPath}/khach-hang?trang=${trangHienTai + 1}&tuKhoa=${tuKhoaHienTai}&trangThai=${trangThaiHienTai}&nganhNgheId=${nganhNgheIdHienTai}&quyMoId=${quyMoIdHienTai}&boLocId=${boLocHienTai.boLocId}" class="btn btn-outline btn-sm">Sau</a>
                            </c:if>
                        </div>
                    </div>
                </c:if>
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
                        <div class="cs-stat-value" id="statCanChamSoc">${not empty thongKeChamSoc ? thongKeChamSoc.tongSoCanChamSoc : 0}</div>
                        <div class="cs-stat-label">Cần chăm sóc định kỳ</div>
                    </div>
                </div>

                <div class="cs-stat-card">
                    <div class="cs-stat-icon-wrapper cs-stat-icon-red">
                        <span class="material-symbols-outlined" aria-hidden="true">warning</span>
                    </div>
                    <div class="cs-stat-content">
                        <div class="cs-stat-value" id="statQuaHanNghiemTrong">${not empty thongKeChamSoc ? thongKeChamSoc.soQuaHanNghiemTrong : 0}</div>
                        <div class="cs-stat-label">Quá hạn cao (&gt; 60 ngày)</div>
                    </div>
                </div>

                <div class="cs-stat-card">
                    <div class="cs-stat-icon-wrapper cs-stat-icon-primary">
                        <span class="material-symbols-outlined" aria-hidden="true">payments</span>
                    </div>
                    <div class="cs-stat-content">
                        <div class="cs-stat-value" id="statTongGiaTriHopDong">${not empty thongKeChamSoc ? thongKeChamSoc.tongGiaTriHopDongDinhDang : '0 đ'}</div>
                        <div class="cs-stat-label">Giá trị HĐ cần bảo vệ</div>
                    </div>
                </div>

                <div class="cs-stat-card">
                    <div class="cs-stat-icon-wrapper cs-stat-icon-green">
                        <span class="material-symbols-outlined" aria-hidden="true">task_alt</span>
                    </div>
                    <div class="cs-stat-content">
                        <div class="cs-stat-value" id="statDaLienHeHomNay">${not empty thongKeChamSoc ? thongKeChamSoc.soDaLienHeHomNay : 0}</div>
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
                        <c:set var="dsHienThiChamSoc" value="${not empty danhSachChamSoc ? danhSachChamSoc : danhSachKhachHang}" />
                        <c:choose>
                            <c:when test="${not empty dsHienThiChamSoc}">
                                <c:forEach var="kh" items="${dsHienThiChamSoc}" varStatus="status">
                                    <tr class="row-khach-hang"
                                        data-kh-id="${kh.id}"
                                        data-ma-kh="<c:out value='${not empty kh.maKhachHang ? kh.maKhachHang : kh.maBanGhi}' />"
                                        data-ten-kh="<c:out value='${not empty kh.tenCongTy ? kh.tenCongTy : kh.tieuDe}' />"
                                        data-nguoi-phu-trach="<c:out value='${kh.tenNguoiPhuTrach}' />"
                                        data-ten-nhom="<c:out value='${kh.tenNhom}' />"
                                        data-ngay-tao="<c:out value='${kh.ngayTao}' />"
                                        data-gia-tri-hd="${not empty kh.tongGiaTriHopDong ? kh.tongGiaTriHopDong : ''}"
                                        data-ma-hd="<c:out value='${not empty kh.soHopDong ? kh.soHopDong : \"\"}' />"
                                        data-so-ngay-chua-tt="${kh.soNgayChuaTuongTac}"
                                        data-da-lien-he="${kh.daLienHeHomNay ? 'true' : 'false'}"
                                        data-gia-tri-raw="<c:out value='${kh.giaTri}' />"
                                        data-trang-thai="<c:out value='${kh.trangThai}' />"
                                        data-mo-ta="<c:out value='${kh.moTaChiTiet}' />">
                                        <td style="text-align: center;" class="cell-stt">${status.index + 1}</td>
                                        <td class="font-mono"><c:out value="${not empty kh.maKhachHang ? kh.maKhachHang : kh.maBanGhi}" /></td>
                                        <td>
                                            <div class="customer-name">
                                                <a href="${pageContext.request.contextPath}/khach-hang/chi-tiet?id=${kh.id}" style="color: inherit; text-decoration: none;">
                                                    <c:out value="${not empty kh.tenCongTy ? kh.tenCongTy : kh.tieuDe}" />
                                                </a>
                                            </div>
                                            <div class="customer-sub"><c:out value="${kh.moTaChiTiet}" /></div>
                                        </td>
                                        <td>
                                            <div class="col-contract-highlight cell-gia-tri-hd">${not empty kh.tongGiaTriHopDongDinhDang ? kh.tongGiaTriHopDongDinhDang : '--- đ'}</div>
                                            <span class="contract-code-tag cell-ma-hd"><c:out value="${not empty kh.soHopDong ? kh.soHopDong : 'HĐ-CHUA-KY'}" /></span>
                                        </td>
                                        <td>
                                            <div class="cell-overdue-wrap">
                                                <c:set var="isOverdue60" value="${kh.soNgayChuaTuongTac gt 60}" />
                                                <span class="overdue-badge ${kh.daLienHeHomNay ? 'badge-green' : (isOverdue60 ? 'badge-red' : 'badge-amber')} cell-overdue-badge">
                                                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">${kh.daLienHeHomNay ? 'check_circle' : 'schedule'}</span>
                                                    <span class="text-so-ngay-chua-tt">${kh.daLienHeHomNay ? 'Đã liên hệ hôm nay' : (kh.soNgayChuaTuongTac gt 0 ? kh.soNgayChuaTuongTac.toString().concat(' ngày chưa liên hệ') : '-- ngày')}</span>
                                                </span>
                                                <span class="overdue-date-text cell-ngay-tt-cuoi">${not empty kh.lanTuongTacCuoiDinhDang ? kh.lanTuongTacCuoiDinhDang : 'Chưa có tương tác'}</span>
                                            </div>
                                        </td>
                                        <td>
                                            <div><c:out value="${kh.tenNguoiPhuTrach}" /></div>
                                            <div style="font-size: 12px; color: var(--slate-500);"><c:out value="${kh.tenNhom}" /></div>
                                        </td>
                                        <td style="text-align: center;">
                                            <span class="badge cell-badge-trang-thai ${kh.daLienHeHomNay ? 'badge-success' : (kh.soNgayChuaTuongTac gt 60 ? 'badge-danger' : 'badge-warning')}">
                                                ${kh.daLienHeHomNay ? 'Đã chăm sóc' : (kh.soNgayChuaTuongTac gt 60 ? 'Quá hạn cao' : 'Cần liên hệ')}
                                            </span>
                                        </td>
                                        <td style="text-align: center;">
                                            <div class="actions-cell-flex">
                                                <c:set var="coQuyenChamSoc" value="${empty userHienTai or userHienTai.coVaiTro('ADMIN') or userHienTai.coVaiTro('DIRECTOR') or userHienTai.coVaiTro('TEAM_LEAD') or userHienTai.coVaiTro('SALES_REP') or userHienTai.coVaiTro('CUST_SUCCESS')}" />
                                                <c:choose>
                                                    <c:when test="${coQuyenChamSoc}">
                                                        <button type="button" class="btn-contact-mark btn-action-mark ${kh.daLienHeHomNay ? 'is-contacted' : ''}"
                                                                data-kh-id="${kh.id}"
                                                                data-ten-kh="<c:out value='${not empty kh.tenCongTy ? kh.tenCongTy : kh.tieuDe}' />"
                                                                ${kh.daLienHeHomNay ? 'disabled' : ''}
                                                                title="Đánh dấu đã liên hệ khách hàng này ngay trên danh sách"
                                                                aria-label="Đánh dấu đã liên hệ với ${not empty kh.tenCongTy ? kh.tenCongTy : kh.tieuDe}">
                                                            <span class="material-symbols-outlined icon-xs" aria-hidden="true">${kh.daLienHeHomNay ? 'check' : 'phone_in_talk'}</span>
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
                                                <a href="${pageContext.request.contextPath}/khach-hang/chi-tiet?id=${kh.id}" class="btn-action" title="Xem chi tiết hồ sơ khách hàng" aria-label="Xem chi tiết ${not empty kh.tenCongTy ? kh.tenCongTy : kh.tieuDe}">
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


        <!-- TAB 2: Trung Tâm Cảnh Báo Trùng Lặp & Gộp Khách Hàng (Story S3-04) -->
        <div class="crm-tab-pane tab-pane ${tabHienTai eq 'trung' ? 'active' : ''}" id="paneTrungLap" role="tabpanel" aria-labelledby="tabBtnTrungLap" style="${tabHienTai eq 'trung' ? 'display: block;' : 'display: none;'}">
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

                <!-- ========================================================================= -->
        <!-- MODAL LƯU BỘ LỌC HAY DÙNG (STORY S3-07 AC3) -->
        <!-- ========================================================================= -->
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
                                <label class="form-label" for="tenCongTy">Tên công ty / Khách hàng (AC1) <span class="required">*</span></label>
                                <input type="text" id="tenCongTy" name="tenCongTy" class="form-input" placeholder="Ví dụ: Công ty Cổ phần Công nghệ ABC" required autocomplete="off">
                            <!-- Cảnh báo trùng lặp thời gian thực khi gõ tên công ty (S3-04) -->
                            <div class="inline-dup-alert" id="inlineDupNameAlert" style="display: none;">
                                <span class="material-symbols-outlined icon-sm" aria-hidden="true">warning</span>
                                <div><strong>Cảnh báo tên gần giống:</strong> Có thể trùng với khách hàng <strong id="dupMatchedName"></strong> do <strong id="dupMatchedOwner"></strong> phụ trách!</div>
                            </div>
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
                                <!-- Cảnh báo trùng MST thời gian thực (S3-04) -->
                                <div class="inline-dup-alert" id="inlineDupMstAlert" style="display: none;">
                                    <span class="material-symbols-outlined icon-sm" aria-hidden="true">error</span>
                                    <div><strong>Cảnh báo trùng MST:</strong> Mã số thuế đã tồn tại cho khách hàng <strong id="dupMatchedMstName"></strong> do <strong id="dupMatchedMstOwner"></strong> phụ trách!</div>
                                </div>
                            </div>
                            <div class="form-col">
                                <label class="form-label" for="website">Website (AC1)</label>
                                <input type="text" id="website" name="website" class="form-input" placeholder="Ví dụ: https://congtyabc.vn" autocomplete="off">
                                <!-- Cảnh báo trùng Website thời gian thực (S3-04) -->
                                <div class="inline-dup-alert" id="inlineDupWebAlert" style="display: none;">
                                    <span class="material-symbols-outlined icon-sm" aria-hidden="true">warning</span>
                                    <div><strong>Cảnh báo trùng Website:</strong> Website trùng với khách hàng <strong id="dupMatchedWebName"></strong> do <strong id="dupMatchedWebOwner"></strong> phụ trách!</div>
                                </div>
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
                                <label class="form-label" for="trangThai">Trạng thái khách hàng (AC3) <span class="required">*</span></label>
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

        <!-- Modal Sửa Khách Hàng (Story S3-01 & S1-05) -->
        <div class="modal-backdrop" id="modalSuaKhachHang" style="display: none;" role="dialog" aria-modal="true" aria-labelledby="modalSuaKhachHangTieuDe">
            <div class="modal-card">
                <div class="modal-header">
                    <div>
                        <h2 class="modal-title" id="modalSuaKhachHangTieuDe">Chỉnh Sửa Hồ Sơ Khách Hàng</h2>
                        <p class="modal-subtitle">Cập nhật thông tin hồ sơ khách hàng doanh nghiệp (Story S3-01)</p>
                    </div>
                    <button type="button" class="modal-close-btn" id="btnDongModalSuaKhachHang" aria-label="Đóng" title="Đóng">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>
                <form id="formSuaKhachHang" method="POST" action="${pageContext.request.contextPath}/khach-hang">
                    <input type="hidden" name="action" value="sua">
                    <input type="hidden" id="suaKhachHangId" name="id" value="">

                    <div class="modal-body">
                        <!-- Tên công ty & Mã khách hàng -->
                        <div class="form-row">
                            <div class="form-col" style="flex: 2;">
                                <label class="form-label" for="suaTenCongTy">Tên công ty / Khách hàng (AC1) <span class="required">*</span></label>
                                <input type="text" id="suaTenCongTy" name="tenCongTy" class="form-input" required autocomplete="off">
                            </div>
                            <div class="form-col" style="flex: 1;">
                                <label class="form-label" for="suaMaKhachHang">Mã khách hàng</label>
                                <input type="text" id="suaMaKhachHang" name="maKhachHang" class="form-input" readonly style="background-color: var(--slate-100); font-family: monospace;">
                            </div>
                        </div>

                        <!-- Mã số thuế & Website -->
                        <div class="form-row">
                            <div class="form-col">
                                <label class="form-label" for="suaMaSoThue">Mã số thuế (AC1, AC2) <small style="color: var(--slate-500); font-weight: normal;">(Nếu có phải duy nhất)</small></label>
                                <input type="text" id="suaMaSoThue" name="maSoThue" class="form-input" autocomplete="off">
                            </div>
                            <div class="form-col">
                                <label class="form-label" for="suaWebsite">Website (AC1)</label>
                                <input type="text" id="suaWebsite" name="website" class="form-input" autocomplete="off">
                            </div>
                        </div>

                        <!-- Ngành nghề & Quy mô -->
                        <div class="form-row">
                            <div class="form-col">
                                <label class="form-label" for="suaNganhNgheId">Ngành nghề (AC1)</label>
                                <select id="suaNganhNgheId" name="nganhNgheId" class="form-select">
                                    <option value="">-- Chọn ngành nghề --</option>
                                    <c:forEach var="nn" items="${dsNganhNghe}">
                                        <option value="${nn.id}"><c:out value="${nn.tenMuc}" /></option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="form-col">
                                <label class="form-label" for="suaQuyMoId">Quy mô doanh nghiệp (AC1)</label>
                                <select id="suaQuyMoId" name="quyMoId" class="form-select">
                                    <option value="">-- Chọn quy mô --</option>
                                    <c:forEach var="qm" items="${dsQuyMo}">
                                        <option value="${qm.id}"><c:out value="${qm.tenMuc}" /></option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>

                        <!-- Địa chỉ -->
                        <div class="form-group">
                            <label class="form-label" for="suaDiaChi">Địa chỉ trụ sở / văn phòng (AC1)</label>
                            <input type="text" id="suaDiaChi" name="diaChi" class="form-input" autocomplete="off">
                        </div>

                        <!-- Trạng thái & Doanh thu ước tính -->
                        <div class="form-row">
                            <div class="form-col">
                                <label class="form-label" for="suaTrangThai">Trạng thái khách hàng (AC3) <span class="required">*</span></label>
                                <select id="suaTrangThai" name="trangThai" class="form-select" required>
                                    <option value="Tiềm năng">Tiềm năng</option>
                                    <option value="Đang giao dịch">Đang giao dịch</option>
                                    <option value="Khách hàng">Khách hàng</option>
                                    <option value="Ngừng hợp tác">Ngừng hợp tác</option>
                                </select>
                            </div>
                            <div class="form-col">
                                <label class="form-label" for="suaDoanhThuUocTinh">Doanh thu ước tính (VND)</label>
                                <input type="text" id="suaDoanhThuUocTinh" name="doanhThuUocTinh" class="form-input" autocomplete="off">
                            </div>
                        </div>

                        <!-- Người sở hữu (Data Scope Enforcement AC1, AC4) -->
                        <div class="form-group">
                            <label class="form-label">Người sở hữu hồ sơ (AC1, AC4)</label>
                            <c:choose>
                                <c:when test="${coQuyenChonOwner and not empty dsNhanVienSoHuu}">
                                    <select id="suaNguoiSoHuuId" name="nguoiSoHuuId" class="form-select">
                                        <c:forEach var="nv" items="${dsNhanVienSoHuu}">
                                            <option value="${nv.id}">
                                                <c:out value="${nv.hoTen}" /> - <c:out value="${nv.email}" /> (<c:out value="${nv.tenNhom}" />)
                                            </option>
                                        </c:forEach>
                                    </select>
                                    <small style="color: var(--slate-500); display: block; margin-top: 4px;">
                                        Trưởng nhóm / Giám đốc / Quản trị viên có quyền điều chuyển người phụ trách trong phạm vi cho phép.
                                    </small>
                                </c:when>
                                <c:otherwise>
                                    <div class="form-readonly-badge">
                                        <span class="material-symbols-outlined icon-sm" aria-hidden="true">lock</span>
                                        <span>Nhân viên kinh doanh không được tự chuyển quyền sở hữu cho người khác.</span>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <!-- Ghi chú chi tiết -->
                        <div class="form-group">
                            <label class="form-label" for="suaMoTaChiTiet">Ghi chú / Mô tả chi tiết</label>
                            <textarea id="suaMoTaChiTiet" name="moTaChiTiet" class="form-textarea" rows="3"></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline" id="btnHuySuaKhachHang">Hủy bỏ</button>
                        <button type="submit" class="btn btn-primary" id="btnXacNhanSuaKhachHang">
                            <span class="material-symbols-outlined" aria-hidden="true">save</span>
                            <span>Lưu Thay Đổi</span>
                        </button>
                    </div>
                </form>
            </div>
        </div>


        <!-- Toast Notifications Container -->
        <div class="crm-toast-container" id="crmToastContainer" aria-live="polite"></div>
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
