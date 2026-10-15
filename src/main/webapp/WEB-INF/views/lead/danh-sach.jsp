<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Danh sách khách hàng tiềm năng với bộ lọc nâng cao và bộ lọc lưu sẵn - Story S4-09 MSys CRM">
    <title>Danh Sách Khách Hàng Tiềm Năng (Lead) - MSys CRM</title>

    <!-- Google Fonts & Material Symbols -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&family=JetBrains+Mono:wght@500;700&display=swap" rel="stylesheet">

    <!-- CSS chung toàn hệ thống -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/material-symbols.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">

    <!-- CSS chuyên biệt cho Danh sách & Bộ lọc Lead (S4-09) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lead/danh-sach-lead.css">
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

            <!-- 2. Header Banner & Quick Actions -->
            <header class="lead-header-banner">
                <div class="lead-header-title-wrap">
                    <div class="lead-header-icon-box" aria-hidden="true">
                        <span class="material-symbols-outlined">filter_alt</span>
                    </div>
                    <div class="lead-header-text">
                        <h1>Khách Hàng Tiềm Năng (Lead)</h1>
                        <p>Lọc, theo dõi và quản lý các cơ hội tiếp cận khách hàng theo thời gian thực</p>
                    </div>
                </div>
                <div class="lead-header-actions">
                    <a href="${pageContext.request.contextPath}/lead/tao-moi" class="lead-btn lead-btn-primary" id="btnThemLeadMoi">
                        <span class="material-symbols-outlined" aria-hidden="true">person_add</span>
                        <span>+ Tạo Lead thủ công</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/lead/import-excel" class="lead-btn lead-btn-outline" id="btnNhapLeadExcel">
                        <span class="material-symbols-outlined" aria-hidden="true">upload_file</span>
                        <span>Nhập Excel</span>
                    </a>
                </div>
            </header>

            <!-- 3. Thông báo hệ thống (Thành công / Lỗi) -->
            <c:if test="${not empty thongBaoThanhCong}">
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

            <!-- 5. Saved Filters Bar (AC3: Thanh Bộ Lọc Đã Lưu Sẵn) -->
            <section class="lead-saved-filters-section" aria-label="Bộ lọc đã lưu sẵn">
                <div class="lead-saved-filters-left">
                    <span class="lead-saved-filter-label">
                        <span class="material-symbols-outlined" aria-hidden="true">bookmark</span>
                        <span>Bộ lọc đã lưu:</span>
                    </span>

                    <div class="lead-saved-filter-chips" id="savedFilterChipsWrap">
                        <c:choose>
                            <c:when test="${not empty dsBoLocDaLuu}">
                                <c:forEach var="bl" items="${dsBoLocDaLuu}">
                                    <c:set var="isActiveFilter" value="${not empty boLocHienTai and boLocHienTai.savedFilterId == bl.id}" />
                                    <a href="${pageContext.request.contextPath}/lead?savedFilterId=${bl.id}"
                                       class="lead-saved-filter-chip ${isActiveFilter ? 'is-active' : ''}"
                                       title="Bấm để áp dụng bộ lọc: ${bl.tenBoLoc}">
                                        <span class="material-symbols-outlined" aria-hidden="true">filter_list</span>
                                        <span><c:out value="${bl.tenBoLoc}" /></span>
                                        <c:if test="${isActiveFilter}">
                                            <span class="material-symbols-outlined" style="font-size: 14px; color: var(--lead-primary);" aria-hidden="true">check</span>
                                        </c:if>
                                    </a>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <span class="lead-saved-filter-empty-text">Chưa có bộ lọc cá nhân nào được lưu.</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="lead-saved-filters-right">
                    <button type="button" class="lead-btn-save-filter-trigger" id="btnOpenModalLuuBoLoc" title="Lưu lại các tiêu chí lọc đang chọn">
                        <span class="material-symbols-outlined" aria-hidden="true">bookmark_add</span>
                        <span>⭐ Lưu bộ lọc hiện tại</span>
                    </button>
                </div>
            </section>

            <!-- 6. Form Bộ Lọc 5 Tiêu Chí (AC1: Trạng thái, Nguồn, Phân loại, Người phụ trách, Thời gian) -->
            <section class="lead-filter-card" aria-label="Bộ lọc tiêu chí Lead">
                <div class="lead-filter-header">
                    <div class="lead-filter-header-title">
                        <span class="material-symbols-outlined" aria-hidden="true">tune</span>
                        <span>Bộ lọc tìm kiếm (AND giữa 5 tiêu chí)</span>
                        <c:set var="activeCount" value="0" />
                        <c:if test="${not empty param.trangThai}"><c:set var="activeCount" value="${activeCount + 1}" /></c:if>
                        <c:if test="${not empty param.nguonLeadId}"><c:set var="activeCount" value="${activeCount + 1}" /></c:if>
                        <c:if test="${not empty param.phanLoai}"><c:set var="activeCount" value="${activeCount + 1}" /></c:if>
                        <c:if test="${not empty param.nguoiPhuTrachId}"><c:set var="activeCount" value="${activeCount + 1}" /></c:if>
                        <c:if test="${not empty param.tuNgay or not empty param.denNgay}"><c:set var="activeCount" value="${activeCount + 1}" /></c:if>

                        <c:if test="${activeCount > 0}">
                            <span class="lead-active-filter-counter" id="activeFilterBadge">${activeCount} đang chọn</span>
                        </c:if>
                    </div>
                    <div>
                        <a href="${pageContext.request.contextPath}/lead" class="lead-btn lead-btn-outline lead-btn-sm" id="btnResetFilter" title="Xóa tất cả tiêu chí lọc về mặc định">
                            <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">restart_alt</span>
                            <span>Đặt lại</span>
                        </a>
                    </div>
                </div>

                <form method="GET" action="${pageContext.request.contextPath}/lead" id="formLocLead">
                    <div class="lead-filter-grid">

                        <!-- Tiêu chí 1: Trạng thái -->
                        <div class="lead-filter-field">
                            <label for="filterTrangThai">
                                <span class="material-symbols-outlined" aria-hidden="true">flag</span>
                                <span>1. Trạng thái</span>
                            </label>
                            <select name="trangThai" id="filterTrangThai" class="lead-select">
                                <option value="">-- Tất cả trạng thái --</option>
                                <option value="MOI" ${param.trangThai == 'MOI' or boLocHienTai.trangThai == 'MOI' ? 'selected' : ''}>Mới tiếp nhận</option>
                                <option value="DANG_CHAM_SOC" ${param.trangThai == 'DANG_CHAM_SOC' or boLocHienTai.trangThai == 'DANG_CHAM_SOC' ? 'selected' : ''}>Đang chăm sóc</option>
                                <option value="DA_CHUYEN_DOI" ${param.trangThai == 'DA_CHUYEN_DOI' or boLocHienTai.trangThai == 'DA_CHUYEN_DOI' ? 'selected' : ''}>Đã chuyển đổi</option>
                                <option value="TU_CHOI" ${param.trangThai == 'TU_CHOI' or boLocHienTai.trangThai == 'TU_CHOI' ? 'selected' : ''}>Từ chối</option>
                                <option value="DA_GOP" ${param.trangThai == 'DA_GOP' or boLocHienTai.trangThai == 'DA_GOP' ? 'selected' : ''}>Đã gộp trùng</option>
                            </select>
                        </div>

                        <!-- Tiêu chí 2: Nguồn Lead -->
                        <div class="lead-filter-field">
                            <label for="filterNguonLead">
                                <span class="material-symbols-outlined" aria-hidden="true">share</span>
                                <span>2. Nguồn Lead</span>
                            </label>
                            <select name="nguonLeadId" id="filterNguonLead" class="lead-select">
                                <option value="">-- Tất cả nguồn --</option>
                                <c:choose>
                                    <c:when test="${not empty luaChonNguon}">
                                        <c:forEach var="nguon" items="${luaChonNguon}">
                                            <option value="${nguon.id}" ${(param.nguonLeadId == nguon.id) or (boLocHienTai.nguonLeadId == nguon.id) ? 'selected' : ''}>
                                                <c:out value="${nguon.tenNguon != null ? nguon.tenNguon : nguon.tenMuc}" />
                                            </option>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <option value="1" ${param.nguonLeadId == '1' ? 'selected' : ''}>Website / Biểu mẫu nhúng</option>
                                        <option value="2" ${param.nguonLeadId == '2' ? 'selected' : ''}>Nhập tệp Excel</option>
                                        <option value="3" ${param.nguonLeadId == '3' ? 'selected' : ''}>Sự kiện / Hội thảo</option>
                                        <option value="4" ${param.nguonLeadId == '4' ? 'selected' : ''}>Chiến dịch Quảng cáo</option>
                                        <option value="5" ${param.nguonLeadId == '5' ? 'selected' : ''}>Giới thiệu (Referral)</option>
                                    </c:otherwise>
                                </c:choose>
                            </select>
                        </div>

                        <!-- Tiêu chí 3: Phân loại Nóng / Ấm / Lạnh -->
                        <div class="lead-filter-field">
                            <label for="filterPhanLoai">
                                <span class="material-symbols-outlined" aria-hidden="true">thermostat</span>
                                <span>3. Phân loại</span>
                            </label>
                            <select name="phanLoai" id="filterPhanLoai" class="lead-select">
                                <option value="">-- Tất cả phân loại --</option>
                                <option value="NONG" ${param.phanLoai == 'NONG' or boLocHienTai.phanLoai == 'NONG' ? 'selected' : ''}>🔥 Nóng (Hot)</option>
                                <option value="AM" ${param.phanLoai == 'AM' or boLocHienTai.phanLoai == 'AM' ? 'selected' : ''}>☀️ Ấm (Warm)</option>
                                <option value="LANH" ${param.phanLoai == 'LANH' or boLocHienTai.phanLoai == 'LANH' ? 'selected' : ''}>❄️ Lạnh (Cold)</option>
                            </select>
                        </div>

                        <!-- Tiêu chí 4: Người phụ trách -->
                        <div class="lead-filter-field">
                            <label for="filterNguoiPhuTrach">
                                <span class="material-symbols-outlined" aria-hidden="true">support_agent</span>
                                <span>4. Người phụ trách</span>
                            </label>
                            <select name="nguoiPhuTrachId" id="filterNguoiPhuTrach" class="lead-select">
                                <option value="">-- Tất cả trong phạm vi --</option>
                                <c:choose>
                                    <c:when test="${not empty luaChonNguoiPhuTrach}">
                                        <c:forEach var="user" items="${luaChonNguoiPhuTrach}">
                                            <option value="${user.id}" ${(param.nguoiPhuTrachId == user.id) or (boLocHienTai.nguoiPhuTrachId == user.id) ? 'selected' : ''}>
                                                <c:out value="${user.hoTen}" /> (<c:out value="${user.tenDangNhap}" />)
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
                        <div class="lead-filter-field">
                            <label>
                                <span class="material-symbols-outlined" aria-hidden="true">calendar_month</span>
                                <span>5. Khoảng thời gian tạo</span>
                            </label>
                            <div class="lead-date-range-row">
                                <input type="date" name="tuNgay" id="filterTuNgay" class="lead-input"
                                       value="<c:out value='${not empty param.tuNgay ? param.tuNgay : boLocHienTai.tuNgay}' />"
                                       title="Từ ngày tạo" placeholder="Từ ngày">
                                <input type="date" name="denNgay" id="filterDenNgay" class="lead-input"
                                       value="<c:out value='${not empty param.denNgay ? param.denNgay : boLocHienTai.denNgay}' />"
                                       title="Đến ngày tạo" placeholder="Đến ngày">
                            </div>
                        </div>

                    </div>

                    <!-- Bottom Bar: Nút Lọc và hiển thị Tag tiêu chí đang áp dụng -->
                    <div class="lead-filter-actions">
                        <div class="lead-active-chips-bar" id="activeFilterTagsBar">
                            <span class="lead-active-chip-title">Điều kiện đang áp dụng:</span>
                            <c:if test="${not empty param.trangThai or not empty boLocHienTai.trangThai}">
                                <span class="lead-active-tag">Trạng thái: <strong>${not empty param.trangThai ? param.trangThai : boLocHienTai.trangThai}</strong></span>
                            </c:if>
                            <c:if test="${not empty param.phanLoai or not empty boLocHienTai.phanLoai}">
                                <span class="lead-active-tag">Phân loại: <strong>${not empty param.phanLoai ? param.phanLoai : boLocHienTai.phanLoai}</strong></span>
                            </c:if>
                            <c:if test="${not empty param.tuNgay}">
                                <span class="lead-active-tag">Từ ngày: <strong>${param.tuNgay}</strong></span>
                            </c:if>
                            <c:if test="${not empty param.denNgay}">
                                <span class="lead-active-tag">Đến ngày: <strong>${param.denNgay}</strong></span>
                            </c:if>
                            <c:if test="${empty param.trangThai and empty param.phanLoai and empty param.tuNgay and empty param.denNgay and empty param.nguonLeadId and empty param.nguoiPhuTrachId and empty boLocHienTai.savedFilterId}">
                                <span style="font-size: 12px; color: #94a3b8; font-style: italic;">Mặc định (Toàn bộ trong phạm vi phân quyền)</span>
                            </c:if>
                        </div>

                        <div class="lead-filter-actions-right">
                            <button type="submit" class="lead-btn lead-btn-primary" id="btnSubmitFilter">
                                <span class="material-symbols-outlined" aria-hidden="true">filter_alt</span>
                                <span>Áp dụng lọc</span>
                            </button>
                        </div>
                    </div>
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
                                <th style="width: 110px;">Mã Lead</th>
                                <th>Khách Hàng / Công Ty</th>
                                <th style="width: 140px;">Thông Tin Liên Hệ</th>
                                <th style="width: 130px; text-align: center;">Phân Loại (AC1)</th>
                                <th style="width: 150px;">Nguồn Lead (AC1)</th>
                                <th style="width: 140px; text-align: center;">Trạng Thái (AC1)</th>
                                <th style="width: 160px;">Người Phụ Trách</th>
                                <th style="width: 140px; text-align: center;">Cảnh Báo SLA (AC2)</th>
                                <th style="width: 140px; text-align: right;">Ngày Tạo</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty dsLead}">
                                    <c:forEach var="lead" items="${dsLead}">
                                        <!-- AC2: Tô viền đỏ và nền cảnh báo nếu Lead quá hạn SLA -->
                                        <tr class="${lead.quaSla ? 'row-sla-overdue' : ''}">

                                            <!-- 1. Mã Lead -->
                                            <td class="lead-code-cell">
                                                <a href="${pageContext.request.contextPath}/lead/chi-tiet?id=${lead.id}" title="Xem chi tiết lead" style="color: inherit; text-decoration: none;">
                                                    <c:out value="${lead.maLead}" />
                                                </a>
                                            </td>

                                            <!-- 2. Họ tên & Công ty -->
                                            <td>
                                                <div class="lead-name-cell">
                                                    <a href="${pageContext.request.contextPath}/lead/chi-tiet?id=${lead.id}" class="lead-fullname">
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

                                            <!-- 4. Phân loại Nóng / Ấm / Lạnh (AC1) -->
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

                                            <!-- 5. Nguồn Lead (AC1) -->
                                            <td>
                                                <span class="lead-badge lead-badge-source">
                                                    <span class="material-symbols-outlined" aria-hidden="true">label</span>
                                                    <span><c:out value="${not empty lead.tenNguonLead ? lead.tenNguonLead : 'Website'}" /></span>
                                                </span>
                                            </td>

                                            <!-- 6. Trạng thái Lead (AC1) -->
                                            <td style="text-align: center;">
                                                <c:choose>
                                                    <c:when test="${lead.trangThai != null && (lead.trangThai == 'MOI' || lead.trangThai.name() == 'MOI')}">
                                                        <span class="lead-badge lead-badge-status-new">
                                                            <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">fiber_new</span>
                                                            <span>Mới tiếp nhận</span>
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
                                                            <c:out value="${lead.trangThai != null ? (lead.trangThai.tenHienThi != null ? lead.trangThai.tenHienThi : lead.trangThai) : 'Mới'}" />
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>

                                            <!-- 7. Người phụ trách (AC1) -->
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
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <!-- Empty State khi không tìm thấy Lead nào -->
                                    <tr>
                                        <td colspan="9">
                                            <div class="lead-empty-state-wrap">
                                                <div class="lead-empty-icon-circle" aria-hidden="true">
                                                    <span class="material-symbols-outlined">search_off</span>
                                                </div>
                                                <h3 class="lead-empty-title">Không tìm thấy khách hàng tiềm năng nào</h3>
                                                <p class="lead-empty-desc">
                                                    Không có Lead nào khớp với các tiêu chí lọc hiện tại. Thử thay đổi hoặc đặt lại bộ lọc để tìm kiếm rộng hơn.
                                                </p>
                                                <div>
                                                    <a href="${pageContext.request.contextPath}/lead" class="lead-btn lead-btn-primary lead-btn-sm" id="btnEmptyResetFilter">
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
    <div class="lead-modal-backdrop" id="modalLuuBoLoc" aria-hidden="true" role="dialog" aria-labelledby="modalLuuBoLocTitle">
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

    <!-- JS chuyên biệt -->
    <script src="${pageContext.request.contextPath}/assets/js/lead/danh-sach-lead.js"></script>
</body>
</html>
