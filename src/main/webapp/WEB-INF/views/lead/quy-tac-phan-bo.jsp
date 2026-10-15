<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cấu hình Quy Tắc Phân Bổ Lead Tự Động (S4-06) | CRM Bán Hàng</title>
    <meta name="description" content="Quản lý cấu hình chuỗi quy tắc phân bổ khách hàng tiềm năng tự động theo khu vực, ngành nghề hoặc xoay vòng đều (Round-Robin). First-Match-Wins, hoàn tất trong <= 5 phút.">

    <!-- CSS dùng chung toàn hệ thống -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <!-- CSS chuyên biệt module Phân bổ Lead (Story S4-06) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lead/phan-bo.css">

    <script>
        window.CONTEXT_PATH = "${pageContext.request.contextPath}";
        window.HAS_BACKEND_SUBMIT = ${not empty danhSachQuyTac ? 'true' : 'false'};
    </script>
</head>
<body class="crm-body">

    <!-- Navigation layout dùng chung S1-06 (đã bao gồm sidebar, thông tin user và Material Symbols) -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <div class="pb-container">

            <!-- 1. Breadcrumb điều hướng chuẩn -->
            <nav class="pb-breadcrumb" aria-label="Đường dẫn điều hướng">
                <div class="pb-breadcrumb-links">
                    <a href="${pageContext.request.contextPath}/dieu-huong">Trang chủ</a>
                    <span class="sep">/</span>
                    <a href="${pageContext.request.contextPath}/danh-muc">Cấu hình & Tự động hoá</a>
                    <span class="sep">/</span>
                    <span class="current">Phân bổ lead tự động (S4-06)</span>
                </div>
                <div style="display: flex; gap: 8px;">
                    <a href="${pageContext.request.contextPath}/lead/hang-cho-phan-bo" class="btn btn-outline btn-sm" id="btn-link-hang-cho-top" title="Xem Hàng chờ phân bổ (AC3)">
                        <span class="material-symbols-outlined icon-xs">inbox</span> Hàng chờ (${not empty soLuongHangCho ? soLuongHangCho : 0})
                    </a>
                    <a href="${pageContext.request.contextPath}/danh-muc" class="btn btn-outline btn-sm" id="btn-quay-lai-danh-muc">
                        <span class="material-symbols-outlined icon-xs">arrow_back</span> Danh mục cấu hình
                    </a>
                </div>
            </nav>

            <!-- 2. Header giao diện chính -->
            <header class="pb-header">
                <div class="pb-header-content">
                    <div class="pb-header-badge">
                        <span class="material-symbols-outlined icon-xs">alt_route</span> Story S4-06 — Lead Engine
                    </div>
                    <h1 class="pb-header-title">Cấu hình Quy Tắc Phân Bổ Lead Tự Động</h1>
                    <p class="pb-header-desc">
                        Thiết lập chuỗi quy tắc phân bổ khách hàng tiềm năng theo khu vực địa lý, ngành nghề kinh doanh hoặc cơ chế xoay vòng chia đều (Round-Robin).
                        Hệ thống áp dụng nguyên tắc <strong>First-Match-Wins</strong> (quy tắc ưu tiên trên cùng khớp sẽ dừng duyệt) và tự động hoàn tất gán lead trong &le; 5 phút.
                    </p>
                </div>

                <div class="pb-header-actions">
                    <!-- Action PBL-07: Chạy phân bổ ngay lập tức (AC4) -->
                    <button type="button" class="btn btn-secondary" id="btn-chay-ngay" title="Kích hoạt tiến trình quét và phân bổ lead ngay lập tức">
                        <span class="material-symbols-outlined">play_arrow</span> Chạy phân bổ ngay
                    </button>

                    <!-- Nút xem nhanh Hàng chờ phân bổ (Action PBL-05, AC3) -->
                    <a href="${pageContext.request.contextPath}/lead/hang-cho-phan-bo" class="btn btn-secondary" id="btn-xem-hang-cho" title="Xem các lead không khớp quy tắc đang nằm trong hàng chờ">
                        <span class="material-symbols-outlined">inbox</span> Hàng chờ (<span id="badge-queue-count">${not empty soLuongHangCho ? soLuongHangCho : '0'}</span>)
                    </a>

                    <!-- Action PBL-02: Tạo quy tắc mới (AC1) -->
                    <button type="button" class="btn btn-primary" id="btn-them-quy-tac" title="Thêm quy tắc phân bổ lead tự động mới">
                        <span class="material-symbols-outlined">add</span> + Thêm quy tắc phân bổ
                    </button>
                </div>
            </header>

            <!-- 3. Thông báo kết quả thao tác Flash Message -->
            <c:if test="${not empty sessionScope.successMessage}">
                <div class="pb-alert pb-alert-success" role="alert">
                    <div class="pb-alert-content">
                        <span class="material-symbols-outlined">check_circle</span>
                        <span><strong>Thành công:</strong> <c:out value="${sessionScope.successMessage}"/></span>
                    </div>
                    <button type="button" class="pb-alert-close" onclick="this.parentElement.remove();" aria-label="Đóng">
                        <span class="material-symbols-outlined">close</span>
                    </button>
                </div>
                <c:remove var="successMessage" scope="session"/>
            </c:if>

            <c:if test="${not empty requestScope.errorMessage}">
                <div class="pb-alert pb-alert-danger" role="alert">
                    <div class="pb-alert-content">
                        <span class="material-symbols-outlined">error</span>
                        <span><strong>Lỗi:</strong> <c:out value="${requestScope.errorMessage}"/></span>
                    </div>
                    <button type="button" class="pb-alert-close" onclick="this.parentElement.remove();" aria-label="Đóng">
                        <span class="material-symbols-outlined">close</span>
                    </button>
                </div>
            </c:if>

            <!-- 4. Khối Thống kê Tổng quan (KPI & Status) -->
            <section class="pb-stats-grid" aria-label="Thống kê phân bổ lead">
                <div class="pb-stat-card">
                    <div class="pb-stat-icon-wrap primary">
                        <span class="material-symbols-outlined">rule_settings</span>
                    </div>
                    <div class="pb-stat-info">
                        <span class="pb-stat-label">Tổng quy tắc cấu hình</span>
                        <span class="pb-stat-value" id="stat-total-rules">${not empty danhSachQuyTac ? fn:length(danhSachQuyTac) : '4'}</span>
                        <span class="pb-stat-note" id="stat-active-rules">Đang hoạt động trong chuỗi</span>
                    </div>
                </div>

                <div class="pb-stat-card">
                    <div class="pb-stat-icon-wrap warning">
                        <span class="material-symbols-outlined">hourglass_top</span>
                    </div>
                    <div class="pb-stat-info">
                        <span class="pb-stat-label">Lead trong hàng chờ (AC3)</span>
                        <span class="pb-stat-value" id="stat-queue-count">${not empty soLuongHangCho ? soLuongHangCho : '0'}</span>
                        <span class="pb-stat-note warning-text">Chờ Trưởng nhóm phân tay</span>
                    </div>
                </div>

                <div class="pb-stat-card">
                    <div class="pb-stat-icon-wrap success">
                        <span class="material-symbols-outlined">speed</span>
                    </div>
                    <div class="pb-stat-info">
                        <span class="pb-stat-label">Cam kết SLA phân bổ (AC4)</span>
                        <span class="pb-stat-value">&le; 5 phút</span>
                        <span class="pb-stat-note success-text">Tiến trình quét nền mỗi 30s</span>
                    </div>
                </div>

                <div class="pb-stat-card">
                    <div class="pb-stat-icon-wrap info">
                        <span class="material-symbols-outlined">low_priority</span>
                    </div>
                    <div class="pb-stat-info">
                        <span class="pb-stat-label">Thuật toán đánh giá (AC2)</span>
                        <span class="pb-stat-value" style="font-size: 18px; line-height: 28px;">First-Match-Wins</span>
                        <span class="pb-stat-note">Khớp quy tắc trên dừng duyệt</span>
                    </div>
                </div>
            </section>

            <!-- 5. Điều hướng Tab chức năng -->
            <div class="pb-tabs" role="tablist">
                <button type="button" class="pb-tab-btn active" role="tab" aria-selected="true" data-tab="tab-quy-tac" id="tab-btn-rules">
                    <span class="material-symbols-outlined icon-xs">format_list_numbered</span>
                    Danh sách quy tắc phân bổ (AC1, AC2)
                </button>
                <button type="button" class="pb-tab-btn" role="tab" aria-selected="false" data-tab="tab-sandbox" id="tab-btn-sandbox">
                    <span class="material-symbols-outlined icon-xs">science</span>
                    Bộ mô phỏng & Kiểm thử First-Match (Sandbox)
                </button>
            </div>

            <!-- ========================================================
                 TAB 1: DANH SÁCH QUY TẮC PHÂN BỔ (AC1, AC2)
                 ======================================================== -->
            <div class="pb-tab-pane active" id="tab-quy-tac" role="tabpanel">
                <div class="pb-card">
                    <!-- Thanh công cụ lọc & tìm kiếm -->
                    <div class="pb-toolbar">
                        <div class="pb-toolbar-left">
                            <div class="pb-search-box">
                                <span class="material-symbols-outlined icon-xs">search</span>
                                <input type="text" id="input-search-rule" placeholder="Tìm theo tên quy tắc, mã quy tắc, nhóm nhận..." aria-label="Tìm kiếm quy tắc">
                            </div>
                            <div class="pb-filter-select">
                                <select id="select-filter-type" aria-label="Lọc theo kiểu phân bổ">
                                    <option value="ALL">Tất cả kiểu phân bổ</option>
                                    <option value="THEO_KHU_VUC">Theo khu vực</option>
                                    <option value="THEO_NGANH_NGHE">Theo ngành nghề</option>
                                    <option value="XOAY_VONG">Xoay vòng nhóm</option>
                                </select>
                            </div>
                            <div class="pb-filter-select">
                                <select id="select-filter-status" aria-label="Lọc theo trạng thái">
                                    <option value="ALL">Tất cả trạng thái</option>
                                    <option value="ACTIVE">Đang bật</option>
                                    <option value="INACTIVE">Tạm tắt</option>
                                </select>
                            </div>
                        </div>

                        <div class="pb-toolbar-right">
                            <span style="font-size: 13px; color: var(--pb-slate-500);">
                                <span class="material-symbols-outlined icon-xs" style="vertical-align: middle;">drag_indicator</span> Kéo thả hàng hoặc dùng nút <span class="material-symbols-outlined icon-xs" style="vertical-align: middle;">expand_less</span> <span class="material-symbols-outlined icon-xs" style="vertical-align: middle;">expand_more</span> để đổi thứ tự ưu tiên
                            </span>
                        </div>
                    </div>

                    <!-- Bảng Dữ Liệu Quy Tắc Phân Bổ (Action PBL-01, AC1, AC2) -->
                    <div class="pb-table-wrap">
                        <table class="pb-table" id="pb-rules-table">
                            <thead>
                                <tr>
                                    <th style="width: 140px;">Ưu tiên (AC2)</th>
                                    <th style="min-width: 240px;">Tên & Mã quy tắc</th>
                                    <th style="width: 160px;">Kiểu phân bổ (AC1)</th>
                                    <th style="min-width: 250px;">Điều kiện áp dụng</th>
                                    <th style="min-width: 200px;">Đích phân bổ</th>
                                    <th style="width: 130px;">Trạng thái</th>
                                    <th style="width: 110px; text-align: center;">Hành động</th>
                                </tr>
                            </thead>
                            <tbody id="pb-rules-tbody">
                                <c:choose>
                                    <c:when test="${not empty danhSachQuyTac}">
                                        <c:forEach var="rule" items="${danhSachQuyTac}" varStatus="status">
                                            <tr data-rule-id="${rule.id}"
                                                data-rule-code="${rule.maQuyTac}"
                                                data-rule-name="${rule.tenQuyTac}"
                                                data-rule-priority="${rule.thuTuUuTien}"
                                                data-rule-type="${rule.kieuPhanBo}"
                                                data-rule-condition="${rule.dieuKienMoTa}"
                                                data-rule-condition-json="${fn:escapeXml(rule.dieuKienJson)}"
                                                data-rule-target-team-id="${rule.nhomDichId}"
                                                data-rule-target-team="${rule.tenNhomDich}"
                                                data-rule-target-user-id="${rule.nguoiDungDichId}"
                                                data-rule-target-user="${rule.tenNguoiDungDich}"
                                                data-rule-active="${rule.hoatDong}"
                                                data-rule-updated="${rule.updatedAt}"
                                                class="${rule.hoatDong ? '' : 'row-disabled'}"
                                                draggable="true">

                                                <!-- Thứ tự ưu tiên & Nút Lên/Xuống (Action PBL-03) -->
                                                <td>
                                                    <div class="pb-priority-cell">
                                                        <span class="pb-drag-handle" title="Kéo để đổi thứ tự"><span class="material-symbols-outlined">drag_indicator</span></span>
                                                        <span class="pb-rank-badge ${rule.thuTuUuTien == 1 ? 'rank-1' : (rule.thuTuUuTien == 2 ? 'rank-2' : (rule.thuTuUuTien == 3 ? 'rank-3' : 'rank-n'))}">
                                                            Top ${rule.thuTuUuTien}
                                                        </span>
                                                        <div class="pb-arrow-btns">
                                                            <button type="button" class="pb-arrow-btn btn-move-up" ${status.first ? 'disabled' : ''} title="Nâng lên ưu tiên cao hơn (PBL-03)">
                                                                <span class="material-symbols-outlined">expand_less</span>
                                                            </button>
                                                            <button type="button" class="pb-arrow-btn btn-move-down" ${status.last ? 'disabled' : ''} title="Hạ xuống ưu tiên thấp hơn (PBL-03)">
                                                                <span class="material-symbols-outlined">expand_more</span>
                                                            </button>
                                                        </div>
                                                    </div>
                                                </td>

                                                <!-- Tên & Mã quy tắc -->
                                                <td>
                                                    <div class="pb-rule-info">
                                                        <span class="pb-rule-name"><c:out value="${rule.tenQuyTac}"/></span>
                                                        <span class="pb-rule-code"><c:out value="${rule.maQuyTac}"/></span>
                                                    </div>
                                                </td>

                                                <!-- Kiểu phân bổ -->
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${rule.kieuPhanBo == 'THEO_KHU_VUC'}">
                                                            <span class="pb-type-badge type-khu-vuc"><span class="material-symbols-outlined">location_on</span> Khu vực</span>
                                                        </c:when>
                                                        <c:when test="${rule.kieuPhanBo == 'THEO_NGANH_NGHE'}">
                                                            <span class="pb-type-badge type-nganh-nghe"><span class="material-symbols-outlined">category</span> Ngành nghề</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="pb-type-badge type-xoay-vong"><span class="material-symbols-outlined">autorenew</span> Xoay vòng</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>

                                                <!-- Điều kiện áp dụng -->
                                                <td>
                                                    <div class="pb-criteria-text">
                                                        <c:out value="${not empty rule.dieuKienMoTa ? rule.dieuKienMoTa : 'Tất cả lead'}"/>
                                                    </div>
                                                </td>

                                                <!-- Đích phân bổ -->
                                                <td>
                                                    <div class="pb-target-badge">
                                                        <span class="material-symbols-outlined">${not empty rule.nguoiDungDichId ? 'person' : 'groups'}</span>
                                                        <span><c:out value="${not empty rule.tenNhomDich ? rule.tenNhomDich : rule.tenNguoiDungDich}"/></span>
                                                    </div>
                                                    <span class="pb-target-sub">
                                                        <c:choose>
                                                            <c:when test="${not empty rule.tenNguoiDungDich and not empty rule.nhomDichId}">
                                                                Xoay vòng nhân viên
                                                            </c:when>
                                                            <c:otherwise>
                                                                <c:out value="${not empty rule.tenNguoiDungDich ? rule.tenNguoiDungDich : 'Nhóm kinh doanh'}"/>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </span>
                                                </td>

                                                <!-- Trạng thái (Action PBL-04) -->
                                                <td>
                                                    <label class="pb-switch" title="Bấm để bật hoặc tạm dừng quy tắc">
                                                        <input type="checkbox" class="pb-status-toggle" data-rule-id="${rule.id}" ${rule.hoatDong ? 'checked' : ''}>
                                                        <span class="pb-slider"></span>
                                                        <span class="pb-switch-label">${rule.hoatDong ? 'Đang bật' : 'Tạm tắt'}</span>
                                                    </label>
                                                </td>

                                                <!-- Hành động -->
                                                <td>
                                                    <div class="pb-actions-cell">
                                                        <button type="button" class="btn btn-outline btn-icon-only btn-edit-rule" title="Chỉnh sửa quy tắc">
                                                            <span class="material-symbols-outlined">edit</span>
                                                        </button>
                                                        <button type="button" class="btn btn-outline btn-icon-only btn-delete-rule" title="Xóa quy tắc">
                                                            <span class="material-symbols-outlined">delete</span>
                                                        </button>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <!-- Fallback hiển thị dữ liệu nếu danh sách trống -->
                                        <tr>
                                            <td colspan="7" style="text-align: center; padding: 40px 20px; color: var(--pb-slate-500);">
                                                <span class="material-symbols-outlined" style="font-size: 48px; color: var(--pb-slate-400); display: block; margin-bottom: 8px;">rule</span>
                                                <strong>Chưa có quy tắc phân bổ nào.</strong><br>
                                                <span style="font-size: 13px;">Bấm nút "+ Thêm quy tắc phân bổ" để cấu hình quy tắc đầu tiên cho hệ thống.</span>
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <!-- ========================================================
                 TAB 2: BỘ MÔ PHỎNG & KIỂM THỬ FIRST-MATCH (SANDBOX)
                 ======================================================== -->
            <div class="pb-tab-pane" id="tab-sandbox" role="tabpanel">
                <div class="pb-card">
                    <div class="pb-sandbox-wrap">
                        <div class="pb-sandbox-header">
                            <span class="material-symbols-outlined" style="color: var(--pb-primary); font-size: 26px;">science</span>
                            <div>
                                <h3 style="font-size: 16px; font-weight: 700; color: var(--pb-slate-900); margin: 0;">Mô phỏng Luồng So Khớp Lead (First-Match-Wins Engine)</h3>
                                <p style="font-size: 13px; color: var(--pb-slate-500); margin: 3px 0 0;">
                                    Nhập thông tin mẫu của một khách hàng tiềm năng giả định để kiểm tra xem hệ thống sẽ khớp quy tắc nào trước và gán cho ai.
                                </p>
                            </div>
                        </div>

                        <div class="pb-sandbox-body">
                            <div class="pb-sandbox-form">
                                <div class="form-group">
                                    <label class="form-label" for="sim-lead-name">Tên khách hàng / Công ty mẫu</label>
                                    <input type="text" id="sim-lead-name" class="form-control" value="Công ty TNHH Giải Pháp Công Nghệ Á Châu">
                                </div>

                                <div class="form-group">
                                    <label class="form-label" for="sim-lead-area">Khu vực địa lý của Lead</label>
                                    <select id="sim-lead-area" class="form-control">
                                        <option value="">-- Khách không cung cấp khu vực (NULL) --</option>
                                        <c:forEach var="kv" items="${danhSachKhuVuc}">
                                            <option value="${kv.id}"><c:out value="${kv.tenKhuVuc}"/></option>
                                        </c:forEach>
                                        <option value="1" selected>Hà Nội (Miền Bắc)</option>
                                        <option value="2">Hải Phòng</option>
                                        <option value="4">TP. Hồ Chí Minh (Miền Nam)</option>
                                        <option value="99">Tỉnh khác</option>
                                    </select>
                                </div>

                                <div class="form-group">
                                    <label class="form-label" for="sim-lead-industry">Ngành nghề kinh doanh của Lead</label>
                                    <select id="sim-lead-industry" class="form-control">
                                        <option value="">-- Khách không chọn ngành nghề (NULL) --</option>
                                        <c:forEach var="nn" items="${danhSachNganhNghe}">
                                            <option value="${nn.id}"><c:out value="${nn.tenMuc}"/></option>
                                        </c:forEach>
                                        <option value="5">Tài chính - Ngân hàng</option>
                                        <option value="8" selected>Công nghệ thông tin</option>
                                        <option value="12">Bất động sản</option>
                                        <option value="99">Ngành nghề khác</option>
                                    </select>
                                </div>

                                <button type="button" class="btn btn-primary" id="btn-run-simulation" style="width: 100%; margin-top: 10px;">
                                    <span class="material-symbols-outlined">play_circle</span> Chạy Thử Nghiệm Mô Phỏng
                                </button>
                            </div>

                            <div class="pb-sandbox-result" id="sandbox-result-box">
                                <div class="result-placeholder">
                                    <span class="material-symbols-outlined" style="font-size: 36px; color: var(--pb-slate-300);">psychology</span>
                                    <div>Bấm nút "Chạy Thử Nghiệm" để xem kết quả đánh giá First-Match qua từng quy tắc.</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

        </div>
    </main>

    <!-- ========================================================
         MODAL THÊM MỚI / CHỈNH SỬA QUY TẮC (ACTION PBL-02)
         ======================================================== -->
    <div class="pb-modal-overlay" id="modal-quy-tac" role="dialog" aria-modal="true" aria-labelledby="modal-rule-title">
        <div class="pb-modal-dialog">
            <div class="pb-modal-header">
                <div style="display: flex; align-items: center; gap: 10px;">
                    <span class="material-symbols-outlined" id="modal-icon" style="color: var(--pb-primary); font-size: 24px;">add_box</span>
                    <h3 class="pb-modal-title" id="modal-rule-title">Thêm Quy Tắc Phân Bổ Mới (AC1)</h3>
                </div>
                <button type="button" class="pb-modal-close" id="btn-close-rule-modal" aria-label="Đóng modal">
                    <span class="material-symbols-outlined">close</span>
                </button>
            </div>

            <form id="form-quy-tac" action="${pageContext.request.contextPath}/admin/lead/phan-bo/tao-moi" method="POST">
                <div class="pb-modal-body">
                    <!-- ID ẩn dùng khi sửa -->
                    <input type="hidden" id="quy-tac-id" name="id" value="">

                    <!-- 1. Tên quy tắc (ten_quy_tac) -->
                    <div class="form-group">
                        <label class="form-label" for="ten_quy_tac">
                            <span>Tên quy tắc phân bổ <span class="required">*</span></span>
                            <span style="font-size: 11.5px; color: var(--pb-slate-400);">Ví dụ: Lead Miền Bắc phân cho Nhóm Hà Nội</span>
                        </label>
                        <input type="text" class="form-control" id="ten_quy_tac" name="ten_quy_tac" placeholder="Nhập tên mô tả rõ ràng mục đích của quy tắc..." required maxlength="200">
                        <div class="form-error-msg" id="err-ten-quy-tac">Vui lòng nhập tên quy tắc (tối đa 200 ký tự)</div>
                    </div>

                    <!-- 2. Kiểu phân bổ (kieu_phan_bo: THEO_KHU_VUC, THEO_NGANH_NGHE, XOAY_VONG) -->
                    <div class="form-group">
                        <label class="form-label" for="kieu_phan_bo">
                            <span>Kiểu phân bổ (AC1) <span class="required">*</span></span>
                        </label>
                        <select class="form-control" id="kieu_phan_bo" name="kieu_phan_bo" required>
                            <option value="THEO_KHU_VUC">Phân bổ theo Khu Vực Địa Lý (khu_vuc_ids)</option>
                            <option value="THEO_NGANH_NGHE">Phân bổ theo Ngành Nghề Kinh Doanh (nganh_nghe_ids)</option>
                            <option value="XOAY_VONG">Phân bổ Xoay Vòng Chia Đều (Round-Robin trong nhóm)</option>
                        </select>
                    </div>

                    <!-- 3. Dynamic Conditions: Checkbox list Khu vực (khu_vuc_ids) -->
                    <div class="pb-condition-panel active" id="panel-khu-vuc">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                            <span style="font-size: 13px; font-weight: 700; color: var(--pb-slate-700);">Chọn các khu vực áp dụng:</span>
                            <div style="display: flex; gap: 6px;">
                                <button type="button" class="btn btn-outline btn-xs" id="btn-chon-het-kv">Chọn tất cả</button>
                                <button type="button" class="btn btn-outline btn-xs" id="btn-bo-chon-kv">Bỏ chọn</button>
                            </div>
                        </div>
                        <div class="pb-checkbox-grid">
                            <c:choose>
                                <c:when test="${not empty danhSachKhuVuc}">
                                    <c:forEach var="kv" items="${danhSachKhuVuc}">
                                        <label class="pb-checkbox-item">
                                            <input type="checkbox" name="khu_vuc_ids" value="${kv.id}" data-label="${kv.tenKhuVuc}">
                                            <span><c:out value="${kv.tenKhuVuc}"/></span>
                                        </label>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <!-- Fallback Mock Khu vực -->
                                    <label class="pb-checkbox-item"><input type="checkbox" name="khu_vuc_ids" value="1" data-label="Hà Nội" checked> <span>Hà Nội</span></label>
                                    <label class="pb-checkbox-item"><input type="checkbox" name="khu_vuc_ids" value="2" data-label="Hải Phòng" checked> <span>Hải Phòng</span></label>
                                    <label class="pb-checkbox-item"><input type="checkbox" name="khu_vuc_ids" value="3" data-label="Quảng Ninh" checked> <span>Quảng Ninh</span></label>
                                    <label class="pb-checkbox-item"><input type="checkbox" name="khu_vuc_ids" value="4" data-label="TP. Hồ Chí Minh"> <span>TP. Hồ Chí Minh</span></label>
                                    <label class="pb-checkbox-item"><input type="checkbox" name="khu_vuc_ids" value="5" data-label="Bình Dương"> <span>Bình Dương</span></label>
                                    <label class="pb-checkbox-item"><input type="checkbox" name="khu_vuc_ids" value="6" data-label="Đà Nẵng"> <span>Đà Nẵng</span></label>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="form-error-msg" id="err-khu-vuc">Vui lòng chọn ít nhất một khu vực áp dụng</div>
                    </div>

                    <!-- 4. Dynamic Conditions: Checkbox list Ngành nghề (nganh_nghe_ids) -->
                    <div class="pb-condition-panel" id="panel-nganh-nghe">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                            <span style="font-size: 13px; font-weight: 700; color: var(--pb-slate-700);">Chọn các ngành nghề áp dụng:</span>
                            <div style="display: flex; gap: 6px;">
                                <button type="button" class="btn btn-outline btn-xs" id="btn-chon-het-nganh">Chọn tất cả</button>
                                <button type="button" class="btn btn-outline btn-xs" id="btn-bo-chon-nganh">Bỏ chọn</button>
                            </div>
                        </div>
                        <div class="pb-checkbox-grid">
                            <c:choose>
                                <c:when test="${not empty danhSachNganhNghe}">
                                    <c:forEach var="nn" items="${danhSachNganhNghe}">
                                        <label class="pb-checkbox-item">
                                            <input type="checkbox" name="nganh_nghe_ids" value="${nn.id}" data-label="${nn.tenMuc}">
                                            <span><c:out value="${nn.tenMuc}"/></span>
                                        </label>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <!-- Fallback Mock Ngành nghề -->
                                    <label class="pb-checkbox-item"><input type="checkbox" name="nganh_nghe_ids" value="5" data-label="Tài chính - Ngân hàng" checked> <span>Tài chính - Ngân hàng</span></label>
                                    <label class="pb-checkbox-item"><input type="checkbox" name="nganh_nghe_ids" value="8" data-label="Công nghệ thông tin" checked> <span>Công nghệ thông tin</span></label>
                                    <label class="pb-checkbox-item"><input type="checkbox" name="nganh_nghe_ids" value="12" data-label="Bất động sản"> <span>Bất động sản</span></label>
                                    <label class="pb-checkbox-item"><input type="checkbox" name="nganh_nghe_ids" value="15" data-label="Sản xuất & Công nghiệp"> <span>Sản xuất & Công nghiệp</span></label>
                                    <label class="pb-checkbox-item"><input type="checkbox" name="nganh_nghe_ids" value="18" data-label="Bán lẻ & Thương mại"> <span>Bán lẻ & Thương mại</span></label>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="form-error-msg" id="err-nganh-nghe">Vui lòng chọn ít nhất một ngành nghề áp dụng</div>
                    </div>

                    <!-- 5. Dynamic Conditions: Ghi chú Xoay vòng -->
                    <div class="pb-condition-panel" id="panel-xoay-vong">
                        <div style="display: flex; align-items: center; gap: 10px; color: var(--pb-warning-text); background: var(--pb-warning-light); padding: 12px; border-radius: var(--pb-radius-md); border: 1px solid var(--pb-warning-border);">
                            <span class="material-symbols-outlined" style="font-size: 24px; color: var(--pb-warning);">info</span>
                            <span style="font-size: 13px;">
                                Cơ chế <strong>Xoay vòng Round-Robin</strong> sẽ tự động chia đều lead cho tất cả nhân viên kinh doanh đang hoạt động trong nhóm đích bên dưới.
                            </span>
                        </div>
                    </div>

                    <!-- 6. Đích đến phân bổ (nhom_dich_id & nguoi_dung_dich_id) -->
                    <div class="form-group" style="margin-top: 16px;">
                        <label class="form-label" for="nhom_dich_id">
                            <span>Nhóm kinh doanh tiếp nhận (nhom_dich_id) <span class="required">*</span></span>
                        </label>
                        <select class="form-control" id="nhom_dich_id" name="nhom_dich_id" required>
                            <option value="">-- Chọn nhóm kinh doanh tiếp nhận --</option>
                            <c:choose>
                                <c:when test="${not empty danhSachNhomKinhDoanh}">
                                    <c:forEach var="nhom" items="${danhSachNhomKinhDoanh}">
                                        <option value="${nhom.id}"><c:out value="${nhom.tenNhom}"/> (${nhom.soLuongThanhVien} thành viên)</option>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <!-- Fallback Mock Nhóm -->
                                    <option value="101" selected>Nhóm Kinh Doanh Miền Bắc (8 nhân viên)</option>
                                    <option value="102">Nhóm Khách hàng Doanh nghiệp Lớn (5 nhân viên)</option>
                                    <option value="103">Nhóm Inside Sales (6 nhân viên)</option>
                                    <option value="104">Nhóm Kinh Doanh Miền Nam (10 nhân viên)</option>
                                </c:otherwise>
                            </c:choose>
                        </select>
                        <div class="form-error-msg" id="err-nhom-dich">Vui lòng chọn nhóm kinh doanh tiếp nhận</div>
                    </div>

                    <!-- 7. Kích hoạt quy tắc ngay (hoat_dong: boolean) -->
                    <div class="form-group" style="margin-top: 4px;">
                        <label class="pb-switch" style="cursor: pointer;">
                            <input type="checkbox" name="hoat_dong" id="modal-hoat-dong" value="true" checked>
                            <span class="pb-slider"></span>
                            <span class="pb-switch-label" style="font-size: 13.5px; font-weight: 600; color: var(--pb-slate-800);">
                                Kích hoạt quy tắc phân bổ ngay sau khi lưu
                            </span>
                        </label>
                    </div>
                </div>

                <div class="pb-modal-footer">
                    <button type="button" class="btn btn-outline" id="btn-cancel-rule-modal">Hủy bỏ</button>
                    <button type="submit" class="btn btn-primary" id="btn-submit-rule">
                        <span class="material-symbols-outlined">save</span> Lưu quy tắc phân bổ
                    </button>
                </div>
            </form>
        </div>
    </div>

    <!-- ========================================================
         MODAL KẾT QUẢ CHẠY PHÂN BỔ NGAY (AC4, ACTION PBL-07)
         ======================================================== -->
    <div class="pb-modal-overlay" id="modal-ket-qua-chay" role="dialog" aria-modal="true" aria-labelledby="modal-res-title">
        <div class="pb-modal-dialog" style="max-width: 500px;">
            <div class="pb-modal-header">
                <div style="display: flex; align-items: center; gap: 10px;">
                    <span class="material-symbols-outlined" style="color: var(--pb-success); font-size: 26px;">task_alt</span>
                    <h3 class="pb-modal-title" id="modal-res-title">Kết Quả Phân Bổ Nền Tự Động (AC4)</h3>
                </div>
                <button type="button" class="pb-modal-close" id="btn-close-res-modal" aria-label="Đóng">
                    <span class="material-symbols-outlined">close</span>
                </button>
            </div>

            <div class="pb-modal-body">
                <div style="background: var(--pb-slate-50); border: 1px solid var(--pb-slate-200); border-radius: var(--pb-radius-lg); padding: 18px; margin-bottom: 16px;">
                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px;">
                        <div>
                            <span style="font-size: 12px; color: var(--pb-slate-500); display: block;">Số lead đã quét:</span>
                            <strong style="font-size: 22px; color: var(--pb-slate-900);" id="res-lead-quet">0</strong>
                        </div>
                        <div>
                            <span style="font-size: 12px; color: var(--pb-slate-500); display: block;">Phân bổ thành công:</span>
                            <strong style="font-size: 22px; color: var(--pb-success);" id="res-lead-thanh-cong">0</strong>
                        </div>
                        <div>
                            <span style="font-size: 12px; color: var(--pb-slate-500); display: block;">Đưa vào hàng chờ (AC3):</span>
                            <strong style="font-size: 22px; color: var(--pb-warning);" id="res-lead-hang-cho">0</strong>
                        </div>
                        <div>
                            <span style="font-size: 12px; color: var(--pb-slate-500); display: block;">Thời gian xử lý:</span>
                            <strong style="font-size: 22px; color: var(--pb-primary);" id="res-lead-latency">0 ms</strong>
                        </div>
                    </div>
                </div>
                <p style="font-size: 13px; color: var(--pb-slate-600); margin: 0; line-height: 1.5;">
                    Tiến trình nền chạy với chu kỳ 30s đã quét toàn bộ các lead mới chưa có người phụ trách và hoàn tất gán nhân viên theo chuỗi quy tắc First-Match-Wins.
                </p>
            </div>

            <div class="pb-modal-footer">
                <button type="button" class="btn btn-primary" id="btn-done-res-modal" style="width: 100%;">
                    Xác nhận & Hoàn tất
                </button>
            </div>
        </div>
    </div>

    <!-- Container Toast thông báo nổi -->
    <div class="pb-toast-container" id="pb-toast-container" aria-live="polite"></div>

    <!-- Scripts logic -->
    <script src="${pageContext.request.contextPath}/assets/js/lead/phan-bo.js"></script>
</body>
</html>
