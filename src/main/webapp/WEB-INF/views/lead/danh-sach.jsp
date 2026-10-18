<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Danh sách khách hàng tiềm năng với bộ lọc nâng cao và bộ lọc lưu sẵn - Story S4-09 MSys CRM">
    <title>Danh Sách Khách Hàng Tiềm Năng (Lead) - CRM Bán Hàng</title>

    <!-- Google Fonts & Material Symbols -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&family=JetBrains+Mono:wght@500;700&display=swap" rel="stylesheet">

    <!-- CSS chung toàn hệ thống -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/material-symbols.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">

    <!-- CSS Module Lead (S4-02 & S4-09) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lead/lead.css?v=20261016_ui_fix2">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lead/danh-sach-lead.css?v=20261016_ui_fix2">
</head>
<body class="crm-body">

    <!-- Navigation Bar & Sidebar chung của CRM -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <div class="lead-page-container">

            <!-- 1. Breadcrumb điều hướng -->
            <nav class="lead-breadcrumb" aria-label="Breadcrumb">
                <ul class="lead-breadcrumb-list">
                    <li>
                        <a href="${pageContext.request.contextPath}/dieu-huong" class="lead-breadcrumb-link">Trang chủ</a>
                    </li>
                    <li class="lead-breadcrumb-separator" aria-hidden="true">/</li>
                    <li>
                        <a href="${pageContext.request.contextPath}/lead" class="lead-breadcrumb-link">Lead & Phân bổ</a>
                    </li>
                    <li class="lead-breadcrumb-separator" aria-hidden="true">/</li>
                    <li class="lead-breadcrumb-active" aria-current="page">Danh sách Lead & Bộ lọc</li>
                </ul>
                <div>
                    <a href="${pageContext.request.contextPath}/dieu-huong" class="lead-btn lead-btn-outline lead-btn-sm" id="btnQuayLaiTrangChu">
                        <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">arrow_back</span>
                        <span>Trang chủ</span>
                    </a>
                </div>
            </nav>

            <%-- Xác thực vai trò người dùng từ session/request phục vụ hiển thị các nút điều hướng S4-06 --%>
            <c:set var="userXacThuc" value="${not empty nguoiDung ? nguoiDung : sessionScope.nguoiDung}" />
            <c:set var="vaiTroDto" value="${not empty currentUser and not empty currentUser.vaiTro ? currentUser.vaiTro.name() : ''}" />
            <c:set var="laAdmin" value="${(not empty userXacThuc and userXacThuc.coVaiTro('ADMIN')) or vaiTroDto == 'ADMIN'}" />
            <c:set var="laDirector" value="${(not empty userXacThuc and userXacThuc.coVaiTro('DIRECTOR')) or vaiTroDto == 'DIRECTOR'}" />
            <c:set var="laTeamLead" value="${(not empty userXacThuc and userXacThuc.coVaiTro('TEAM_LEAD')) or vaiTroDto == 'TEAM_LEAD'}" />
            <c:set var="coQuyenHangCho" value="${laAdmin or laDirector or laTeamLead}" />
            <c:set var="coQuyenQuyTac" value="${laAdmin or laDirector}" />

            <!-- 2. Header Banner & Quick Actions (Hợp nhất đầy đủ nút dev S4-02, S4-03, S4-06) -->
            <header class="lead-header-banner">
                <div class="lead-header-title-wrap">
                    <div class="lead-header-icon-box" aria-hidden="true">
                        <span class="material-symbols-outlined">filter_alt</span>
                    </div>
                    <div class="lead-header-text">
                        <h1>Khách Hàng Tiềm Năng (Leads)</h1>
                        <p>Quản lý toàn bộ danh sách lead tiếp nhận từ hội thảo, danh thiếp sự kiện, nhập hàng loạt từ Excel hoặc qua biểu mẫu nhúng website</p>
                    </div>
                </div>
                <div class="lead-header-actions">
                    <!-- Dropdown Nghiệp vụ Lead (S4-01, S4-03, S4-04, S4-05, S4-06) -->
                    <div class="lead-dropdown" id="dropdownNghiepVuWrap">
                        <button type="button" class="lead-btn lead-btn-outline" id="btnToggleNghiepVu" aria-haspopup="true" aria-expanded="false" title="Menu các nghiệp vụ liên quan đến Lead (Story S4-01 đến S4-06)">
                            <span class="material-symbols-outlined" aria-hidden="true">settings_suggest</span>
                            <span>Nghiệp vụ Lead</span>
                            <span class="material-symbols-outlined dropdown-arrow-icon" id="iconDropdownArrow" style="font-size: 18px;" aria-hidden="true">expand_more</span>
                        </button>
                        <div class="lead-dropdown-menu" id="menuNghiepVuLead" role="menu" aria-label="Nghiệp vụ Lead" style="display: none;">
                            <!-- S4-04: Cảnh báo & gộp Lead trùng -->
                            <a href="${pageContext.request.contextPath}/lead/duplicates" class="lead-dropdown-item" role="menuitem" id="menuItemGopTrung" title="Cảnh báo và so sánh gộp khách hàng tiềm năng trùng lặp (Story S4-04)">
                                <span class="material-symbols-outlined item-icon icon-s404" aria-hidden="true">call_merge</span>
                                <div class="lead-dropdown-item-text">
                                    <div class="item-title">Cảnh báo & Gộp Lead trùng <span class="story-tag">S4-04</span></div>
                                    <small>Phát hiện và hợp nhất lead trùng số điện thoại, email, họ tên</small>
                                </div>
                            </a>

                            <!-- S4-05: Cấu hình quy tắc chấm điểm Lead -->
                            <a href="${pageContext.request.contextPath}/lead/cham-diem" class="lead-dropdown-item" role="menuitem" id="menuItemChamDiem" title="Cấu hình ma trận chấm điểm và phân loại Nóng/Ấm/Lạnh (Story S4-05)">
                                <span class="material-symbols-outlined item-icon icon-s405" aria-hidden="true">score</span>
                                <div class="lead-dropdown-item-text">
                                    <div class="item-title">Cấu hình chấm điểm Lead <span class="story-tag">S4-05</span></div>
                                    <small>Thiết lập ngưỡng điểm đánh giá tiềm năng Nóng, Ấm, Lạnh</small>
                                </div>
                            </a>

                            <div class="lead-dropdown-divider"></div>

                            <!-- S4-06: Hàng chờ phân bổ Lead -->
                            <c:if test="${coQuyenHangCho}">
                                <a href="${pageContext.request.contextPath}/lead/hang-cho-phan-bo" class="lead-dropdown-item" role="menuitem" id="menuItemHangCho" title="Hàng chờ phân bổ lead chưa ghép quy tắc (Story S4-06)">
                                    <span class="material-symbols-outlined item-icon icon-s406" aria-hidden="true">inbox</span>
                                    <div class="lead-dropdown-item-text">
                                        <div class="item-title">Hàng chờ phân bổ Lead <span class="story-tag">S4-06</span></div>
                                        <small>Tiếp nhận và gán thủ công lead không khớp quy tắc tự động</small>
                                    </div>
                                </a>
                            </c:if>

                            <!-- S4-06: Quy tắc phân bổ tự động -->
                            <c:if test="${coQuyenQuyTac}">
                                <a href="${pageContext.request.contextPath}/admin/lead/phan-bo" class="lead-dropdown-item" role="menuitem" id="menuItemQuyTacPhanBo" title="Cấu hình quy tắc phân bổ tự động Round-Robin & Tiêu chí (Story S4-06)">
                                    <span class="material-symbols-outlined item-icon icon-s406-rules" aria-hidden="true">alt_route</span>
                                    <div class="lead-dropdown-item-text">
                                        <div class="item-title">Quy tắc phân bổ tự động <span class="story-tag">S4-06</span></div>
                                        <small>Cấu hình chia tải tự động theo vòng tròn, khu vực, nguồn</small>
                                    </div>
                                </a>
                            </c:if>

                            <div class="lead-dropdown-divider"></div>

                            <!-- S4-03: Quản lý chiến dịch Marketing -->
                            <a href="${pageContext.request.contextPath}/marketing/campaigns" class="lead-dropdown-item" role="menuitem" id="menuItemChienDich" title="Quản lý chiến dịch marketing và nguồn phát sinh lead (Story S4-03)">
                                <span class="material-symbols-outlined item-icon icon-s403" aria-hidden="true">campaign</span>
                                <div class="lead-dropdown-item-text">
                                    <div class="item-title">Chiến dịch Marketing <span class="story-tag">S4-03</span></div>
                                    <small>Theo dõi chiến dịch và nguồn tiếp nhận khách hàng tiềm năng</small>
                                </div>
                            </a>

                            <!-- S4-01: Biểu mẫu nhúng website -->
                            <a href="${pageContext.request.contextPath}/bieu-mau" class="lead-dropdown-item" role="menuitem" id="menuItemBieuMau" title="Quản lý biểu mẫu web form thu thập thông tin lead tự động (Story S4-01)">
                                <span class="material-symbols-outlined item-icon icon-s401" aria-hidden="true">dynamic_form</span>
                                <div class="lead-dropdown-item-text">
                                    <div class="item-title">Biểu mẫu nhúng website <span class="story-tag">S4-01</span></div>
                                    <small>Tạo mã nhúng HTML form thu thập lead trên website</small>
                                </div>
                            </a>
                        </div>
                    </div>

                    <!-- S4-02: Nhập Excel -->
                    <a href="${pageContext.request.contextPath}/lead/import-excel" class="lead-btn lead-btn-outline" id="btnNhapExcel" title="Nhập hàng loạt lead từ tệp Excel (Story S4-02)">
                        <span class="material-symbols-outlined" aria-hidden="true">upload_file</span>
                        <span>Nhập Excel</span>
                    </a>

                    <!-- S4-02: Tạo Lead thủ công -->
                    <a href="${pageContext.request.contextPath}/lead/tao-moi" class="lead-btn lead-btn-primary" id="btnThemLeadMoi" title="Tạo thủ công lead từ sự kiện hoặc danh thiếp (Story S4-02)">
                        <span class="material-symbols-outlined" aria-hidden="true">add</span>
                        <span>Tạo Lead thủ công</span>
                    </a>
                </div>
            </header>

            <!-- 3. Thông báo hệ thống (Thành công / Lỗi) -->
            <c:if test="${param.thanhCong == 'tao-moi'}">
                <div class="lead-alert lead-alert-success" role="alert">
                    <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                    <span>Tạo khách hàng tiềm năng thành công! Mã Lead: <strong><c:out value="${param.maLead}" /></strong></span>
                </div>
            </c:if>

            <c:if test="${not empty thongBaoThanhCong and param.thanhCong != 'tao-moi'}">
                <div class="lead-alert lead-alert-success" role="alert">
                    <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                    <span><c:out value="${thongBaoThanhCong}" /></span>
                </div>
            </c:if>

            <c:if test="${not empty thongBaoLoi}">
                <div class="lead-alert lead-alert-error" role="alert">
                    <span class="material-symbols-outlined" aria-hidden="true">error</span>
                    <span><c:out value="${thongBaoLoi}" /></span>
                </div>
            </c:if>

            <!-- 4. Morning KPI Cards (AC2: Sales Rep mở máy buổi sáng biết ngay cần gọi ai) -->
            <section class="lead-morning-kpis" aria-label="Thống kê nhanh đầu ngày">
                <!-- Card 1: AC2 - Lead Quá Hạn SLA -->
                <div class="lead-kpi-card kpi-sla-alert" id="kpiCardSlaAlert" title="Bấm để cuộn nhanh đến các Lead quá hạn phản hồi">
                    <div class="lead-kpi-icon-wrap" aria-hidden="true">
                        <span class="material-symbols-outlined">alarm_on</span>
                    </div>
                    <div class="lead-kpi-info">
                        <div class="lead-kpi-value">
                            <c:set var="countSla" value="0" />
                            <c:forEach var="item" items="${dsLead}">
                                <c:if test="${item.quaSla}">
                                    <c:set var="countSla" value="${countSla + 1}" />
                                </c:if>
                            </c:forEach>
                            <span>${countSla}</span>
                            <c:if test="${countSla > 0}">
                                <span class="lead-sla-pulse-dot" title="Đang có cảnh báo SLA" aria-label="Có cảnh báo"></span>
                            </c:if>
                        </div>
                        <div class="lead-kpi-label">Lead Quá SLA (Cần gọi ngay)</div>
                    </div>
                </div>

                <!-- Card 2: Phân loại Nóng -->
                <div class="lead-kpi-card kpi-hot" id="kpiCardNong">
                    <div class="lead-kpi-icon-wrap" aria-hidden="true">
                        <span class="material-symbols-outlined">local_fire_department</span>
                    </div>
                    <div class="lead-kpi-info">
                        <div class="lead-kpi-value">
                            <c:set var="countNong" value="0" />
                            <c:forEach var="item" items="${dsLead}">
                                <c:if test="${item.phanLoai == 'NONG' || item.phanLoai == 'Nóng'}">
                                    <c:set var="countNong" value="${countNong + 1}" />
                                </c:if>
                            </c:forEach>
                            <span>${countNong}</span>
                        </div>
                        <div class="lead-kpi-label">Lead Nóng (Tiềm năng cao)</div>
                    </div>
                </div>

                <!-- Card 3: Mới tiếp nhận -->
                <div class="lead-kpi-card kpi-new" id="kpiCardMoi">
                    <div class="lead-kpi-icon-wrap" aria-hidden="true">
                        <span class="material-symbols-outlined">fiber_new</span>
                    </div>
                    <div class="lead-kpi-info">
                        <div class="lead-kpi-value">
                            <c:set var="countMoi" value="0" />
                            <c:forEach var="item" items="${dsLead}">
                                <c:if test="${item.trangThai != null && (item.trangThai == 'MOI' || item.trangThai.name() == 'MOI')}">
                                    <c:set var="countMoi" value="${countMoi + 1}" />
                                </c:if>
                            </c:forEach>
                            <span>${countMoi}</span>
                        </div>
                        <div class="lead-kpi-label">Mới tiếp nhận (Chưa liên hệ)</div>
                    </div>
                </div>

                <!-- Card 4: Tổng số Lead -->
                <div class="lead-kpi-card kpi-total" id="kpiCardTong">
                    <div class="lead-kpi-icon-wrap" aria-hidden="true">
                        <span class="material-symbols-outlined">groups</span>
                    </div>
                    <div class="lead-kpi-info">
                        <div class="lead-kpi-value">
                            <c:out value="${not empty tongBanGhi ? tongBanGhi : (not empty dsLead ? dsLead.size() : 0)}" />
                        </div>
                        <div class="lead-kpi-label">Tổng Lead trong phạm vi</div>
                    </div>
                </div>
            </section>

            <!-- 5. BỘ LỌC LEAD TINH GỌN (1 HÀNG CHÍNH + BỘ LỌC NÂNG CAO THU GỌN - CHUẨN TRANG KHÁCH HÀNG) -->
            <c:set var="activeCount" value="0" />
            <c:if test="${not empty param.trangThai or not empty boLocHienTai.trangThai}"><c:set var="activeCount" value="${activeCount + 1}" /></c:if>
            <c:if test="${not empty param.nguonLeadId or not empty boLocHienTai.nguonLeadId}"><c:set var="activeCount" value="${activeCount + 1}" /></c:if>
            <c:if test="${not empty param.phanLoai or not empty boLocHienTai.phanLoai}"><c:set var="activeCount" value="${activeCount + 1}" /></c:if>
            <c:if test="${not empty param.nguoiPhuTrachId or not empty boLocHienTai.nguoiPhuTrachId}"><c:set var="activeCount" value="${activeCount + 1}" /></c:if>
            <c:if test="${not empty param.tuNgay or not empty param.denNgay or not empty boLocHienTai.tuNgay or not empty boLocHienTai.denNgay}"><c:set var="activeCount" value="${activeCount + 1}" /></c:if>

            <section class="compact-filter-card" id="compactFilterCard" aria-label="Tìm kiếm và lọc Lead">
                <form method="GET" action="${pageContext.request.contextPath}/lead" id="formLocLead">

                    <!-- HÀNG THAO TÁC CHÍNH (COMPACT ROW TRÊN DESKTOP: TÌM KIẾM NHANH + BỘ LỌC ĐÃ LƯU + NÚT BỘ LỌC + NÚT TÌM KIẾM + NÚT ĐẶT LẠI + LƯU BỘ LỌC) -->
                    <div class="search-main-row">
                        <!-- S4-09: Thanh tìm kiếm nhanh Lead (Họ tên, Tên công ty, Mã Lead, Email, SĐT) -->
                        <div class="quick-search-box">
                            <span class="material-symbols-outlined quick-search-icon" aria-hidden="true">search</span>
                            <input type="text" id="tuKhoa" name="tuKhoa" class="quick-search-input"
                                   placeholder="Tìm họ tên, công ty, mã Lead, email, SĐT..."
                                   value="<c:out value='${not empty param.tuKhoa ? param.tuKhoa : boLocHienTai.tuKhoa}' />" autocomplete="off">
                            <button type="button" id="btnClearTuKhoa" class="quick-search-clear" aria-label="Xóa từ khóa tìm kiếm" title="Xóa từ khóa">
                                <span class="material-symbols-outlined icon-sm" aria-hidden="true">close</span>
                            </button>
                        </div>

                        <!-- Quick Saved Filters Dropdown -->
                        <div class="quick-filter-box">
                            <span class="material-symbols-outlined quick-filter-icon" aria-hidden="true">bookmark</span>
                            <select id="selectBoLocDaLuuQuick" class="quick-filter-select" onchange="if(this.value){window.location.href='${pageContext.request.contextPath}/lead?savedFilterId='+this.value;} else {window.location.href='${pageContext.request.contextPath}/lead';}" title="Chọn nhanh bộ lọc cá nhân đã lưu">
                                <option value="">-- Bộ lọc cá nhân đã lưu --</option>
                                <c:forEach var="bl" items="${dsBoLocDaLuu}">
                                    <option value="${bl.id}" ${not empty boLocHienTai and boLocHienTai.savedFilterId == bl.id ? 'selected' : ''}>
                                        <c:out value="${bl.tenBoLoc}" />
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <!-- Toggle Advanced Filter Button -->
                        <button type="button" class="btn-filter-toggle ${activeCount > 0 ? 'is-active' : ''}"
                                id="btnToggleAdvancedFilter" aria-expanded="false" aria-controls="advancedFilterPanel" title="Mở bộ lọc 5 tiêu chí Lead">
                            <span class="material-symbols-outlined" aria-hidden="true">tune</span>
                            <span>Bộ lọc tiêu chí</span>
                            <c:if test="${activeCount > 0}">
                                <span class="filter-count-badge" id="filterCountBadge">${activeCount}</span>
                            </c:if>
                            <span class="material-symbols-outlined filter-arrow-icon" id="iconFilterArrow" aria-hidden="true">expand_more</span>
                        </button>

                        <!-- Submit Filter Button -->
                        <button type="submit" class="btn-search-submit" id="btnSubmitFilter" title="Tìm kiếm & Áp dụng bộ lọc">
                            <span class="material-symbols-outlined" aria-hidden="true">search</span>
                            <span>Tìm kiếm</span>
                        </button>

                        <!-- Reset Filter Button -->
                        <a href="${pageContext.request.contextPath}/lead" class="btn-reset-filters" id="btnResetFilters" title="Xóa bộ lọc và quay về mặc định">
                            <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">restart_alt</span>
                            <span>Đặt lại</span>
                        </a>

                        <!-- Save Filter Button -->
                        <button type="button" class="btn-save-filter-action" id="btnOpenModalLuuBoLoc" title="Lưu lại các tiêu chí lọc đang chọn thành bộ lọc cá nhân">
                            <span class="material-symbols-outlined" aria-hidden="true">bookmark_add</span>
                            <span>Lưu bộ lọc</span>
                        </button>
                    </div>

                    <!-- KHUNG / PANEL BỘ LỌC NÂNG CAO (MẶC ĐỊNH ĐÓNG, MỞ RA KHI BẤM NÚT 'BỘ LỌC TIÊU CHÍ') -->
                    <div class="advanced-filter-panel" id="advancedFilterPanel" style="display: none;" aria-hidden="true">
                        <div class="panel-header-mini">
                            <div class="panel-title-mini">
                                <span class="material-symbols-outlined" style="font-size: 18px; color: var(--lead-primary);" aria-hidden="true">tune</span>
                                <strong>Bộ lọc nâng cao Lead (5 nhóm tiêu chí S4-09)</strong>
                                <span class="text-muted" style="font-size: 12.5px;">(Điều kiện kết hợp AND)</span>
                            </div>
                            <button type="button" class="panel-close-mini" id="btnDongFilterPanel" title="Đóng bộ lọc" aria-label="Đóng bộ lọc">
                                <span class="material-symbols-outlined" style="font-size: 18px;" aria-hidden="true">close</span>
                            </button>
                        </div>

                        <!-- Nếu đang áp dụng bộ lọc cá nhân đã lưu -->
                        <c:if test="${not empty boLocHienTai and not empty boLocHienTai.savedFilterId}">
                            <div class="panel-saved-filter-section">
                                <span class="saved-filter-active-pill">
                                    <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">bookmark</span>
                                    Đang áp dụng bộ lọc cá nhân: <strong><c:out value="${boLocHienTai.tenBoLoc}" /></strong>
                                </span>
                                <a href="${pageContext.request.contextPath}/lead" class="btn-clear-saved-filter" title="Bỏ áp dụng bộ lọc này">
                                    <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">close</span>
                                    <span>Bỏ chọn</span>
                                </a>
                            </div>
                        </c:if>

                        <!-- LƯỚI 5 TIÊU CHÍ LỌC LEAD (S4-09) -->
                        <div class="filter-grid">
                            <!-- Tiêu chí 1: Trạng thái -->
                            <div class="filter-field">
                                <label class="filter-label" for="filterTrangThai">
                                    <span class="material-symbols-outlined filter-label-icon" aria-hidden="true">flag</span>
                                    <span>1. Trạng thái</span>
                                </label>
                                <select name="trangThai" id="filterTrangThai" class="filter-select">
                                    <option value="">-- Tất cả trạng thái --</option>
                                    <option value="MOI" ${param.trangThai == 'MOI' or boLocHienTai.trangThai == 'MOI' ? 'selected' : ''}>Mới tiếp nhận</option>
                                    <option value="HANG_CHO_PHAN_BO" ${param.trangThai == 'HANG_CHO_PHAN_BO' or boLocHienTai.trangThai == 'HANG_CHO_PHAN_BO' ? 'selected' : ''}>Hàng chờ phân bổ</option>
                                    <option value="DA_PHAN_BO" ${param.trangThai == 'DA_PHAN_BO' or boLocHienTai.trangThai == 'DA_PHAN_BO' ? 'selected' : ''}>Đã phân bổ</option>
                                    <option value="DANG_CHAM_SOC" ${param.trangThai == 'DANG_CHAM_SOC' or boLocHienTai.trangThai == 'DANG_CHAM_SOC' ? 'selected' : ''}>Đang chăm sóc</option>
                                    <option value="DA_CHUYEN_DOI" ${param.trangThai == 'DA_CHUYEN_DOI' or boLocHienTai.trangThai == 'DA_CHUYEN_DOI' ? 'selected' : ''}>Đã chuyển đổi</option>
                                    <option value="TU_CHOI" ${param.trangThai == 'TU_CHOI' or boLocHienTai.trangThai == 'TU_CHOI' ? 'selected' : ''}>Từ chối</option>
                                    <option value="DA_GOP" ${param.trangThai == 'DA_GOP' or boLocHienTai.trangThai == 'DA_GOP' ? 'selected' : ''}>Đã gộp trùng</option>
                                </select>
                            </div>

                            <!-- Tiêu chí 2: Nguồn Lead -->
                            <div class="filter-field">
                                <label class="filter-label" for="filterNguonLead">
                                    <span class="material-symbols-outlined filter-label-icon" aria-hidden="true">share</span>
                                    <span>2. Nguồn Lead</span>
                                </label>
                                <select name="nguonLeadId" id="filterNguonLead" class="filter-select">
                                    <option value="">-- Tất cả nguồn --</option>
                                    <c:if test="${not empty luaChonNguon}">
                                        <c:forEach var="nguon" items="${luaChonNguon}">
                                            <option value="${nguon.id}" ${(param.nguonLeadId == nguon.id) or (boLocHienTai.nguonLeadId == nguon.id) ? 'selected' : ''}>
                                                <c:out value="${not empty nguon.tenMuc ? nguon.tenMuc : (not empty nguon.ten ? nguon.ten : nguon.tenNguon)}" />
                                            </option>
                                        </c:forEach>
                                    </c:if>
                                </select>
                            </div>

                            <!-- Tiêu chí 3: Phân loại Nóng / Ấm / Lạnh -->
                            <div class="filter-field">
                                <label class="filter-label" for="filterPhanLoai">
                                    <span class="material-symbols-outlined filter-label-icon" aria-hidden="true">thermostat</span>
                                    <span>3. Phân loại</span>
                                </label>
                                <select name="phanLoai" id="filterPhanLoai" class="filter-select">
                                    <option value="">-- Tất cả phân loại --</option>
                                    <option value="NONG" ${param.phanLoai == 'NONG' or boLocHienTai.phanLoai == 'NONG' ? 'selected' : ''}>🔥 Nóng (Hot)</option>
                                    <option value="AM" ${param.phanLoai == 'AM' or boLocHienTai.phanLoai == 'AM' ? 'selected' : ''}>☀️ Ấm (Warm)</option>
                                    <option value="LANH" ${param.phanLoai == 'LANH' or boLocHienTai.phanLoai == 'LANH' ? 'selected' : ''}>❄️ Lạnh (Cold)</option>
                                </select>
                            </div>

                            <!-- Tiêu chí 4: Người phụ trách -->
                            <div class="filter-field">
                                <label class="filter-label" for="filterNguoiPhuTrach">
                                    <span class="material-symbols-outlined filter-label-icon" aria-hidden="true">support_agent</span>
                                    <span>4. Người phụ trách</span>
                                </label>
                                <select name="nguoiPhuTrachId" id="filterNguoiPhuTrach" class="filter-select">
                                    <option value="">-- Tất cả trong phạm vi --</option>
                                    <c:choose>
                                        <c:when test="${not empty luaChonNguoiPhuTrach}">
                                            <c:forEach var="u" items="${luaChonNguoiPhuTrach}">
                                                <option value="${u.id}" ${(param.nguoiPhuTrachId == u.id) or (boLocHienTai.nguoiPhuTrachId == u.id) ? 'selected' : ''}>
                                                    <c:out value="${u.hoTen}" /> (<c:out value="${u.email}" />)
                                                </option>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <c:if test="${not empty sessionScope.nguoiDung}">
                                                <option value="${sessionScope.nguoiDung.id}" ${param.nguoiPhuTrachId == sessionScope.nguoiDung.id ? 'selected' : ''}>
                                                    Chính tôi (<c:out value="${sessionScope.nguoiDung.hoTen}" />)
                                                </option>
                                            </c:if>
                                        </c:otherwise>
                                    </c:choose>
                                </select>
                            </div>

                            <!-- Tiêu chí 5: Khoảng thời gian tạo Lead -->
                            <div class="filter-field filter-field-date-range">
                                <label class="filter-label">
                                    <span class="material-symbols-outlined filter-label-icon" aria-hidden="true">calendar_month</span>
                                    <span>5. Khoảng thời gian tạo</span>
                                </label>
                                <div class="lead-date-range-inputs">
                                    <input type="date" name="tuNgay" id="filterTuNgay" class="filter-input"
                                           value="<c:out value='${not empty param.tuNgay ? param.tuNgay : boLocHienTai.tuNgay}' />"
                                           title="Từ ngày tạo" placeholder="Từ ngày">
                                    <span class="date-range-separator">&rarr;</span>
                                    <input type="date" name="denNgay" id="filterDenNgay" class="filter-input"
                                           value="<c:out value='${not empty param.denNgay ? param.denNgay : boLocHienTai.denNgay}' />"
                                           title="Đến ngày tạo" placeholder="Đến ngày">
                                </div>
                            </div>
                        </div>

                        <!-- Thao tác panel lọc (Đóng, Đặt lại, Áp dụng) -->
                        <div class="panel-filter-actions">
                            <button type="button" class="lead-btn lead-btn-outline lead-btn-sm" id="btnDongFilterPanelBottom">
                                <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">close</span>
                                <span>Đóng</span>
                            </button>
                            <div style="display: flex; gap: 8px;">
                                <a href="${pageContext.request.contextPath}/lead" class="lead-btn lead-btn-outline lead-btn-sm" title="Xóa tất cả điều kiện lọc">
                                    <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">restart_alt</span>
                                    <span>Đặt lại</span>
                                </a>
                                <button type="submit" class="lead-btn lead-btn-primary lead-btn-sm">
                                    <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">filter_alt</span>
                                    <span>Áp dụng</span>
                                </button>
                            </div>
                        </div>
                    </div>

                    <!-- THANH HIỂN THỊ CÁC TIÊU CHÍ ĐANG ÁP DỤNG (CHIPS) -->
                    <c:if test="${activeCount > 0 or not empty param.tuKhoa or not empty boLocHienTai.tuKhoa or (not empty boLocHienTai and boLocHienTai.savedFilterId != null)}">
                        <div class="active-filter-chips-bar" id="activeFilterChips">
                            <span class="active-chips-label">Đang lọc:</span>
                            <c:if test="${not empty param.tuKhoa or not empty boLocHienTai.tuKhoa}">
                                <span class="filter-chip">
                                    Từ khóa: "<strong><c:out value="${not empty param.tuKhoa ? param.tuKhoa : boLocHienTai.tuKhoa}" /></strong>"
                                    <button type="button" class="filter-chip-remove" data-field="tuKhoa" aria-label="Xóa từ khóa tìm kiếm" title="Xóa">&times;</button>
                                </span>
                            </c:if>
                            <c:if test="${not empty param.trangThai or not empty boLocHienTai.trangThai}">
                                <c:set var="curTrangThai" value="${not empty param.trangThai ? param.trangThai : boLocHienTai.trangThai}" />
                                <span class="filter-chip">
                                    Trạng thái: <strong>
                                        <c:choose>
                                            <c:when test="${curTrangThai == 'MOI'}">Mới tiếp nhận</c:when>
                                            <c:when test="${curTrangThai == 'HANG_CHO_PHAN_BO'}">Hàng chờ phân bổ</c:when>
                                            <c:when test="${curTrangThai == 'DA_PHAN_BO'}">Đã phân bổ</c:when>
                                            <c:when test="${curTrangThai == 'DANG_CHAM_SOC'}">Đang chăm sóc</c:when>
                                            <c:when test="${curTrangThai == 'DA_CHUYEN_DOI'}">Đã chuyển đổi</c:when>
                                            <c:when test="${curTrangThai == 'TU_CHOI'}">Từ chối</c:when>
                                            <c:when test="${curTrangThai == 'DA_GOP'}">Đã gộp trùng</c:when>
                                            <c:otherwise><c:out value="${curTrangThai}" /></c:otherwise>
                                        </c:choose>
                                    </strong>
                                    <button type="button" class="filter-chip-remove" data-field="filterTrangThai" aria-label="Xóa điều kiện trạng thái" title="Xóa">&times;</button>
                                </span>
                            </c:if>
                            <c:if test="${not empty param.phanLoai or not empty boLocHienTai.phanLoai}">
                                <c:set var="curPhanLoai" value="${not empty param.phanLoai ? param.phanLoai : boLocHienTai.phanLoai}" />
                                <span class="filter-chip">
                                    Phân loại: <strong>
                                        <c:choose>
                                            <c:when test="${curPhanLoai == 'NONG'}">Nóng</c:when>
                                            <c:when test="${curPhanLoai == 'AM'}">Ấm</c:when>
                                            <c:when test="${curPhanLoai == 'LANH'}">Lạnh</c:when>
                                            <c:otherwise><c:out value="${curPhanLoai}" /></c:otherwise>
                                        </c:choose>
                                    </strong>
                                    <button type="button" class="filter-chip-remove" data-field="filterPhanLoai" aria-label="Xóa điều kiện phân loại" title="Xóa">&times;</button>
                                </span>
                            </c:if>
                            <c:if test="${not empty param.nguonLeadId or not empty boLocHienTai.nguonLeadId}">
                                <c:set var="curNguonId" value="${not empty param.nguonLeadId ? param.nguonLeadId : boLocHienTai.nguonLeadId}" />
                                <c:set var="tenNguonHienThi" value="" />
                                <c:forEach var="nguon" items="${luaChonNguon}">
                                    <c:if test="${nguon.id == curNguonId}">
                                        <c:set var="tenNguonHienThi" value="${nguon.tenMuc}" />
                                    </c:if>
                                </c:forEach>
                                <span class="filter-chip">
                                    Nguồn: <strong><c:out value="${not empty tenNguonHienThi ? tenNguonHienThi : '#' += curNguonId}" /></strong>
                                    <button type="button" class="filter-chip-remove" data-field="filterNguonLead" aria-label="Xóa điều kiện nguồn" title="Xóa">&times;</button>
                                </span>
                            </c:if>
                            <c:if test="${not empty param.nguoiPhuTrachId or not empty boLocHienTai.nguoiPhuTrachId}">
                                <c:set var="curNptId" value="${not empty param.nguoiPhuTrachId ? param.nguoiPhuTrachId : boLocHienTai.nguoiPhuTrachId}" />
                                <c:set var="tenNptHienThi" value="" />
                                <c:forEach var="npt" items="${luaChonNguoiPhuTrach}">
                                    <c:if test="${npt.id == curNptId}">
                                        <c:set var="tenNptHienThi" value="${npt.hoTen}" />
                                    </c:if>
                                </c:forEach>
                                <span class="filter-chip">
                                    Phụ trách: <strong><c:out value="${not empty tenNptHienThi ? tenNptHienThi : '#' += curNptId}" /></strong>
                                    <button type="button" class="filter-chip-remove" data-field="filterNguoiPhuTrach" aria-label="Xóa điều kiện phụ trách" title="Xóa">&times;</button>
                                </span>
                            </c:if>
                            <c:if test="${not empty param.tuNgay or not empty boLocHienTai.tuNgay}">
                                <span class="filter-chip">
                                    Từ ngày: <strong>${not empty param.tuNgay ? param.tuNgay : boLocHienTai.tuNgay}</strong>
                                    <button type="button" class="filter-chip-remove" data-field="filterTuNgay" aria-label="Xóa điều kiện từ ngày" title="Xóa">&times;</button>
                                </span>
                            </c:if>
                            <c:if test="${not empty param.denNgay or not empty boLocHienTai.denNgay}">
                                <span class="filter-chip">
                                    Đến ngày: <strong>${not empty param.denNgay ? param.denNgay : boLocHienTai.denNgay}</strong>
                                    <button type="button" class="filter-chip-remove" data-field="filterDenNgay" aria-label="Xóa điều kiện đến ngày" title="Xóa">&times;</button>
                                </span>
                            </c:if>
                        </div>
                    </c:if>
                </form>
            </section>

            <!-- 7. Bảng Dữ Liệu Lead & SLA Highlight (AC1, AC2) -->
            <section class="lead-table-card" aria-label="Bảng dữ liệu Lead">
                <div class="lead-table-header-meta">
                    <div class="lead-table-count">
                        Hiển thị <strong><c:out value="${not empty dsLead ? dsLead.size() : 0}" /></strong> Lead phù hợp
                        <c:if test="${not empty boLocHienTai.tenBoLoc}">
                            (Áp dụng bộ lọc: <strong style="color: var(--lead-primary);"><c:out value="${boLocHienTai.tenBoLoc}" /></strong>)
                        </c:if>
                    </div>
                    <div style="font-size: 13px; color: var(--lead-text-muted);">
                        Sắp xếp: <strong>Lead quá SLA lên trước</strong>, ngày tạo mới nhất
                    </div>
                </div>

                <div class="lead-table-wrap">
                    <table class="lead-table" aria-label="Danh sách Lead">
                        <thead>
                            <tr>
                                <th style="width: 120px;">Mã Lead</th>
                                <th>Khách Hàng / Công Ty</th>
                                <th style="width: 160px;">Thông Tin Liên Hệ</th>
                                <th style="width: 130px; text-align: center;">Phân Loại</th>
                                <th style="width: 160px;">Nguồn Lead</th>
                                <th style="width: 140px; text-align: center;">Trạng Thái</th>
                                <th style="width: 160px;">Người Phụ Trách</th>
                                <th style="width: 130px; text-align: center;">Cảnh Báo SLA</th>
                                <th style="width: 130px; text-align: right;">Ngày Tạo</th>
                                <th style="width: 150px; text-align: center;">Thao Tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty dsLead}">
                                    <c:forEach var="lead" items="${dsLead}">
                                        <!-- AC2: Tô viền đỏ và nền cảnh báo nếu Lead quá hạn SLA -->
                                        <tr class="${lead.quaSla ? 'row-sla-overdue' : 'row-valid'}">

                                            <!-- 1. Mã Lead -->
                                            <td class="lead-code-cell font-mono">
                                                <a href="${pageContext.request.contextPath}/lead/chi-tiet?leadId=${lead.id}"
                                                   class="lead-code-link"
                                                   title="Xem chi tiết lead ${lead.maLead}">
                                                    <strong><c:out value="${lead.maLead}" /></strong>
                                                </a>
                                            </td>

                                            <!-- 2. Họ tên & Công ty -->
                                            <td>
                                                <div class="lead-name-cell">
                                                    <a href="${pageContext.request.contextPath}/lead/chi-tiet?leadId=${lead.id}" class="lead-fullname" title="Xem chi tiết lead ${lead.maLead}">
                                                        <c:out value="${lead.hoTen}" />
                                                    </a>
                                                    <span class="lead-company">
                                                        <c:out value="${not empty lead.tenCongTy ? lead.tenCongTy : '—'}" />
                                                    </span>
                                                </div>
                                            </td>

                                            <!-- 3. Liên hệ -->
                                            <td>
                                                <div class="lead-contact-info">
                                                    <c:if test="${not empty lead.soDienThoai}">
                                                        <span><span class="material-symbols-outlined" style="font-size: 13px; vertical-align: -2px;" aria-hidden="true">call</span> <c:out value="${lead.soDienThoai}" /></span>
                                                    </c:if>
                                                    <c:if test="${not empty lead.email}">
                                                        <span><span class="material-symbols-outlined" style="font-size: 13px; vertical-align: -2px;" aria-hidden="true">mail</span> <c:out value="${lead.email}" /></span>
                                                    </c:if>
                                                    <c:if test="${empty lead.soDienThoai and empty lead.email}">
                                                        <span style="color: #94a3b8;">—</span>
                                                    </c:if>
                                                </div>
                                            </td>

                                            <!-- 4. Phân loại Nóng / Ấm / Lạnh -->
                                            <td style="text-align: center;">
                                                <c:choose>
                                                    <c:when test="${lead.phanLoai == 'NONG' || lead.phanLoai == 'Nóng'}">
                                                        <span class="lead-badge lead-badge-hot">
                                                            <span class="material-symbols-outlined" aria-hidden="true">local_fire_department</span>
                                                            <span>Nóng</span>
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${lead.phanLoai == 'AM' || lead.phanLoai == 'Ấm'}">
                                                        <span class="lead-badge lead-badge-warm">
                                                            <span class="material-symbols-outlined" aria-hidden="true">sunny</span>
                                                            <span>Ấm</span>
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${lead.phanLoai == 'LANH' || lead.phanLoai == 'Lạnh'}">
                                                        <span class="lead-badge lead-badge-cold">
                                                            <span class="material-symbols-outlined" aria-hidden="true">ac_unit</span>
                                                            <span>Lạnh</span>
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="lead-badge lead-badge-warm">
                                                            <span class="material-symbols-outlined" aria-hidden="true">help_outline</span>
                                                            <span><c:out value="${not empty lead.phanLoai ? lead.phanLoai : 'Chưa xếp'}" /></span>
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>

                                            <!-- 5. Nguồn Lead -->
                                            <td>
                                                <span class="lead-badge lead-badge-source">
                                                    <span class="material-symbols-outlined" aria-hidden="true">label</span>
                                                    <span><c:out value="${not empty lead.tenNguonLead ? lead.tenNguonLead : 'Website'}" /></span>
                                                </span>
                                            </td>

                                            <!-- 6. Trạng thái Lead -->
                                            <td style="text-align: center;">
                                                <c:choose>
                                                    <c:when test="${lead.trangThai != null && (lead.trangThai == 'MOI' || lead.trangThai.name() == 'MOI')}">
                                                        <span class="lead-badge lead-badge-status-new">
                                                            <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">fiber_new</span>
                                                            <span>Mới tiếp nhận</span>
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${lead.trangThai != null && (lead.trangThai == 'DA_PHAN_BO' || lead.trangThai.name() == 'DA_PHAN_BO')}">
                                                        <span class="lead-badge lead-badge-status-progress" style="background: rgba(3, 105, 161, 0.12); color: #0284c7; border-color: rgba(3, 105, 161, 0.25);">
                                                            <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">assignment_ind</span>
                                                            <span>Đã phân bổ</span>
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${lead.trangThai != null && (lead.trangThai == 'HANG_CHO_PHAN_BO' || lead.trangThai.name() == 'HANG_CHO_PHAN_BO')}">
                                                        <span class="lead-badge lead-badge-status-new" style="background: rgba(217, 119, 6, 0.12); color: #d97706; border-color: rgba(217, 119, 6, 0.25);">
                                                            <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">hourglass_empty</span>
                                                            <span>Hàng chờ phân bổ</span>
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${lead.trangThai != null && (lead.trangThai == 'DANG_CHAM_SOC' || lead.trangThai.name() == 'DANG_CHAM_SOC')}">
                                                        <span class="lead-badge lead-badge-status-progress">
                                                            <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">pending</span>
                                                            <span>Đang chăm sóc</span>
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${lead.trangThai != null && (lead.trangThai == 'DA_CHUYEN_DOI' || lead.trangThai.name() == 'DA_CHUYEN_DOI')}">
                                                        <span class="lead-badge lead-badge-status-converted">
                                                            <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">check_circle</span>
                                                            <span>Đã chuyển đổi</span>
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="lead-badge lead-badge-status-rejected">
                                                            <c:out value="${lead.trangThai != null ? (lead.trangThai.moTa != null ? lead.trangThai.moTa : lead.trangThai) : 'Mới'}" />
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>

                                            <!-- 7. Người phụ trách -->
                                            <td>
                                                <div class="lead-assignee-cell">
                                                    <c:choose>
                                                        <c:when test="${not empty lead.tenNguoiPhuTrach}">
                                                            <div class="lead-avatar-mini" aria-hidden="true">
                                                                <c:out value="${lead.tenNguoiPhuTrach.substring(0, 1)}" />
                                                            </div>
                                                            <span style="font-weight: 500; font-size: 13px;"><c:out value="${lead.tenNguoiPhuTrach}" /></span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="lead-unassigned-tag">Chưa phân công</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </td>

                                            <!-- 8. Cảnh báo SLA (AC2) -->
                                            <td style="text-align: center;">
                                                <c:choose>
                                                    <c:when test="${lead.quaSla}">
                                                        <span class="lead-badge lead-badge-sla-danger" title="Lead đã vượt quá thời hạn phản hồi SLA!">
                                                            <span class="material-symbols-outlined" aria-hidden="true">warning</span>
                                                            <span>Quá SLA</span>
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="lead-badge lead-badge-sla-normal">
                                                            <span class="material-symbols-outlined" style="color: #10b981;" aria-hidden="true">check</span>
                                                            <span>Đúng hạn</span>
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>

                                            <!-- 9. Ngày tạo -->
                                            <td style="text-align: right; font-size: 12.5px; color: var(--lead-text-muted);">
                                                <c:out value="${lead.createdAt != null ? lead.createdAt : 'Hôm nay'}" />
                                            </td>

                                            <!-- 10. Thao tác -->
                                            <td style="text-align: center;">
                                                <div class="lead-action-btn-group" style="display: inline-flex; align-items: center; justify-content: center; gap: 4px; flex-wrap: wrap;">
                                                    <!-- S4-07: Tiếp nhận/Chi tiết Lead -->
                                                    <a href="${pageContext.request.contextPath}/lead/chi-tiet?leadId=${lead.id}"
                                                       class="lead-btn-action"
                                                       id="btnXemChiTiet_${lead.id}"
                                                       title="Xem chi tiết, tiếp nhận hoặc từ chối Lead ${lead.maLead} (Story S4-07)">
                                                        <span class="material-symbols-outlined" style="font-size: 15px;" aria-hidden="true">visibility</span>
                                                        <span>Chi tiết</span>
                                                    </a>

                                                    <!-- S4-08: Chuyển đổi Lead sang Khách hàng -->
                                                    <c:if test="${lead.trangThai != null && (lead.trangThai == 'DANG_CHAM_SOC' || lead.trangThai.name() == 'DANG_CHAM_SOC')}">
                                                        <a href="${pageContext.request.contextPath}/lead/chuyen-doi?leadId=${lead.id}&id=${lead.id}"
                                                           class="lead-btn-action lead-btn-action-convert"
                                                           id="btnChuyenDoi_${lead.id}"
                                                           title="Chuyển đổi sang Khách hàng chính thức (Story S4-08)">
                                                            <span class="material-symbols-outlined" style="font-size: 15px;" aria-hidden="true">transform</span>
                                                            <span>Chuyển đổi</span>
                                                        </a>
                                                    </c:if>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <!-- Empty State khi không tìm thấy Lead nào -->
                                    <tr>
                                        <td colspan="10">
                                            <div class="lead-empty-state-wrap">
                                                <div class="lead-empty-icon" aria-hidden="true">
                                                    <span class="material-symbols-outlined">person_search</span>
                                                </div>
                                                <h3 class="lead-empty-title">Không tìm thấy khách hàng tiềm năng</h3>
                                                <p class="lead-empty-desc">
                                                    Không có khách hàng tiềm năng nào phù hợp với các tiêu chí lọc đang chọn hoặc trong phạm vi quyền được xem.
                                                </p>
                                                <div class="lead-empty-actions" style="display: flex; gap: 8px; justify-content: center; flex-wrap: wrap;">
                                                    <a href="${pageContext.request.contextPath}/lead/tao-moi" class="lead-btn lead-btn-primary lead-btn-sm" id="btnEmptyTaoLead">
                                                        <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">add</span>
                                                        <span>Tạo Lead thủ công</span>
                                                    </a>
                                                    <a href="${pageContext.request.contextPath}/lead/import-excel" class="lead-btn lead-btn-outline lead-btn-sm" id="btnEmptyNhapExcel">
                                                        <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">upload_file</span>
                                                        <span>Nhập từ Excel</span>
                                                    </a>
                                                    <a href="${pageContext.request.contextPath}/lead" class="lead-btn lead-btn-outline lead-btn-sm" id="btnEmptyResetFilter">
                                                        <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">restart_alt</span>
                                                        <span>Xóa bộ lọc hiện tại</span>
                                                    </a>
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
    </main>

    <!-- 8. Modal Lưu Bộ Lọc Cá Nhân (AC3) -->
    <div class="lead-modal-backdrop" id="modalLuuBoLoc" aria-hidden="true" role="dialog" aria-labelledby="modalLuuBoLocTitle" style="display: none;">
        <div class="lead-modal-dialog">
            <div class="lead-modal-header">
                <div class="lead-modal-header-title" id="modalLuuBoLocTitle">
                    <span class="material-symbols-outlined" aria-hidden="true">bookmark_add</span>
                    <span>Lưu bộ lọc tìm kiếm</span>
                </div>
                <button type="button" class="lead-modal-close-btn" id="btnCloseModalLuuBoLoc" aria-label="Đóng hộp thoại">
                    <span class="material-symbols-outlined" aria-hidden="true">close</span>
                </button>
            </div>

            <form method="POST" action="${pageContext.request.contextPath}/lead/bo-loc/luu" id="formLuuBoLoc">
                <input type="hidden" name="csrfToken" value="${csrfToken}" />
                <div class="lead-modal-body">
                    <div class="lead-modal-group">
                        <label for="modalTenBoLoc" class="lead-modal-label">
                            Tên bộ lọc <span style="color: #ef4444;">*</span>
                        </label>
                        <input type="text" name="tenBoLoc" id="modalTenBoLoc" class="lead-input"
                               placeholder="VD: Lead Nóng chưa gọi sáng nay..."
                               maxlength="80" required autocomplete="off">
                        <div class="lead-modal-char-count" id="modalCharCount">0/80 ký tự</div>
                    </div>

                    <!-- Hiển thị tóm tắt các điều kiện lọc sẽ được lưu -->
                    <div class="lead-modal-preview-box">
                        <div class="lead-modal-preview-title">
                            <span class="material-symbols-outlined" style="font-size: 16px; color: var(--lead-primary);" aria-hidden="true">checklist</span>
                            <span>Tiêu chí đang áp dụng sẽ được lưu:</span>
                        </div>
                        <div class="lead-modal-preview-tags" id="modalFilterPreviewTags">
                            <!-- Sẽ được JavaScript render tự động dựa trên form lọc -->
                        </div>
                    </div>
                </div>

                <div class="lead-modal-footer">
                    <button type="button" class="lead-btn lead-btn-outline" id="btnCancelModalLuuBoLoc">Hủy bỏ</button>
                    <button type="submit" class="lead-btn lead-btn-primary" id="btnSubmitLuuBoLoc">
                        <span class="material-symbols-outlined" aria-hidden="true">save</span>
                        <span>Lưu bộ lọc</span>
                    </button>
                </div>
            </form>
        </div>
    </div>

    <!-- JS chuyên biệt Danh sách & Bộ lọc Lead -->
    <script src="${pageContext.request.contextPath}/assets/js/lead/danh-sach-lead.js?v=20261016_ui_fix2"></script>
</body>
</html>
