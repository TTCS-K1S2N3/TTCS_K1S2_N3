<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quy tắc chấm điểm Lead - MSys CRM</title>

    <!-- CSS dùng chung toàn hệ thống -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">

    <!-- Google Font & CSS Chuyên biệt Module Chấm điểm Lead -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lead/cham-diem.css">
</head>
<body class="crm-body">
    <!-- Navigation layout dùng chung S1-06 (đã bao gồm sidebar, thông tin user và Material Symbols) -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <div class="cd-container">

            <!-- Breadcrumb điều hướng -->
            <nav class="cd-breadcrumb" aria-label="Breadcrumb">
                <ol class="cd-breadcrumb-nav">
                    <li><a href="${pageContext.request.contextPath}/dieu-huong" class="cd-breadcrumb-link">Trang chủ</a></li>
                    <span class="cd-breadcrumb-sep">/</span>
                    <li><a href="${pageContext.request.contextPath}/lead" class="cd-breadcrumb-link">Lead & Phân bổ</a></li>
                    <span class="cd-breadcrumb-sep">/</span>
                    <li class="cd-breadcrumb-current">Quy tắc chấm điểm Lead</li>
                </ol>
                <div style="font-size: 0.75rem; color: #64748b;">
                    Phiên bản cấu hình: <strong style="color: #0f172a;">v${scoringConfig.phienBan}.0</strong>
                </div>
            </nav>

            <!-- Page Header & Hành động -->
            <form id="formCauHinhChamDiem" action="${pageContext.request.contextPath}/lead/cham-diem" method="POST">
                <div class="cd-header-card">
                    <div class="cd-header-title">
                        <h1>Quy tắc chấm điểm Lead</h1>
                        <p>Khai báo tiêu chí đánh giá tiềm năng và phân loại Nóng, Ấm, Lạnh giúp đội ngũ kinh doanh ưu tiên chăm sóc khách hàng hiệu quả.</p>
                    </div>
                    <div class="cd-header-actions">
                        <c:if test="${canEditScoringConfig}">
                            <button type="button" class="btn btn-secondary" id="btnDatLaiMacDinh">
                                <span class="material-symbols-outlined" style="font-size: 18px;">restart_alt</span>
                                <span>Đặt lại mặc định</span>
                            </button>
                            <button type="submit" class="btn btn-primary" id="btnLuuCauHinh">
                                <span class="material-symbols-outlined" style="font-size: 18px;">save</span>
                                <span>Lưu cấu hình</span>
                            </button>
                        </c:if>
                    </div>
                </div>

                <!-- Banners Thông Báo -->
                <c:if test="${not empty thongBaoThanhCong}">
                    <div class="cd-alert cd-alert-success" role="alert">
                        <span class="material-symbols-outlined cd-alert-icon">check_circle</span>
                        <div>${thongBaoThanhCong}</div>
                    </div>
                </c:if>

                <c:if test="${not empty thongBaoLoi}">
                    <div class="cd-alert cd-alert-danger" role="alert">
                        <span class="material-symbols-outlined cd-alert-icon">error</span>
                        <div>${thongBaoLoi}</div>
                    </div>
                </c:if>

                <c:if test="${not canEditScoringConfig}">
                    <div class="cd-alert cd-alert-info" role="status">
                        <span class="material-symbols-outlined cd-alert-icon">info</span>
                        <div>Chế độ chỉ xem: Chỉ Giám đốc kinh doanh hoặc Quản trị viên mới có quyền cập nhật cấu hình này.</div>
                    </div>
                </c:if>

                <!-- Bố Cục 2 Cột -->
                <div class="cd-layout-grid">

                    <!-- CỘT CHÍNH -->
                    <div class="cd-main-column">

                        <!-- 1. NGƯỠNG PHÂN LOẠI LEAD -->
                        <section class="cd-card">
                            <div class="cd-card-header">
                                <div class="cd-card-title">
                                    <span class="material-symbols-outlined icon">tune</span>
                                    <h2>Ngưỡng phân loại Lead</h2>
                                </div>
                            </div>
                            <div class="cd-card-body">
                                <div class="cd-thresholds-grid">
                                    <!-- Lạnh -->
                                    <div class="cd-threshold-card cold-card">
                                        <div class="cd-threshold-header">
                                            <span class="lead-tag tag-cold">Lead Lạnh</span>
                                        </div>
                                        <div class="cd-threshold-input-group">
                                            <span class="cd-threshold-input-label">Dưới</span>
                                            <span id="labelColdMax" style="font-size: 1.125rem; font-weight: 700; color: #0284c7;">${thresholds.warmMin}</span>
                                            <span class="cd-threshold-desc">điểm</span>
                                        </div>
                                        <p class="cd-threshold-desc">Nhu cầu chưa cấp thiết; chuyển sang luồng nuôi dưỡng định kỳ qua Marketing.</p>
                                    </div>

                                    <!-- Ấm -->
                                    <div class="cd-threshold-card warm-card">
                                        <div class="cd-threshold-header">
                                            <span class="lead-tag tag-warm">Lead Ấm</span>
                                        </div>
                                        <div class="cd-threshold-input-group">
                                            <span class="cd-threshold-input-label">Từ</span>
                                            <input type="number" id="warmMin" name="warmMin" class="cd-threshold-number-input"
                                                   value="${thresholds.warmMin}" min="0" max="99" required
                                                   <c:if test="${not canEditScoringConfig}">disabled</c:if> />
                                            <span class="cd-threshold-desc">điểm</span>
                                        </div>
                                        <p class="cd-threshold-desc">Có mối quan tâm rõ ràng; phân bổ nhân viên liên hệ chăm sóc trong ngày.</p>
                                    </div>

                                    <!-- Nóng -->
                                    <div class="cd-threshold-card hot-card">
                                        <div class="cd-threshold-header">
                                            <span class="lead-tag tag-hot">Lead Nóng</span>
                                        </div>
                                        <div class="cd-threshold-input-group">
                                            <span class="cd-threshold-input-label">Từ</span>
                                            <input type="number" id="hotMin" name="hotMin" class="cd-threshold-number-input"
                                                   value="${thresholds.hotMin}" min="1" max="100" required
                                                   <c:if test="${not canEditScoringConfig}">disabled</c:if> />
                                            <span class="cd-threshold-desc">điểm</span>
                                        </div>
                                        <p class="cd-threshold-desc">Khả năng chốt đơn cao; ưu tiên phân bổ và liên hệ ngay trong 5 phút.</p>
                                    </div>
                                </div>

                                <!-- Visual Bar -->
                                <div class="range-wrapper">
                                    <div class="range-header">
                                        <span>Phân bổ thang điểm (0 – 100 điểm)</span>
                                        <span id="rangeSummaryText">Lạnh: 0 - ${thresholds.warmMin - 1}đ | Ấm: ${thresholds.warmMin} - ${thresholds.hotMin - 1}đ | Nóng: ≥ ${thresholds.hotMin}đ</span>
                                    </div>
                                    <div class="range-bar">
                                        <div class="range-segment seg-cold" id="segCold"></div>
                                        <div class="range-segment seg-warm" id="segWarm"></div>
                                        <div class="range-segment seg-hot" id="segHot"></div>
                                    </div>
                                    <div class="range-scale">
                                        <span>0đ</span>
                                        <span id="tickWarm">${thresholds.warmMin}đ</span>
                                        <span id="tickHot">${thresholds.hotMin}đ</span>
                                        <span>100đ</span>
                                    </div>
                                </div>
                            </div>
                        </section>

                        <!-- 2. TIÊU CHÍ CHẤM ĐIỂM (BẢNG THOÁNG ĐÃNG, NGĂN NẮP, MỞ RỘNG CHI TIẾT) -->
                        <section class="cd-card">
                            <div class="cd-card-header">
                                <div class="cd-card-title">
                                    <span class="material-symbols-outlined icon">checklist</span>
                                    <h2>Tiêu chí & Trọng số điểm</h2>
                                </div>
                                <span style="font-size: 0.8125rem; color: var(--cd-text-muted);">
                                    Tổng điểm tối đa: <strong id="totalPointsDisplay" style="color: var(--cd-primary);">${scoringConfig.tongDiemToiDa}đ</strong>
                                </span>
                            </div>
                            <div class="cd-card-body" style="padding: 16px 24px;">
                                <div class="criteria-table-container">
                                    <c:forEach items="${criteriaRows}" var="tc">
                                        <div class="criteria-row-item <c:if test='${not tc.kichHoat}'>is-disabled</c:if>" id="row_${tc.key}">
                                            <div class="criteria-row-header" onclick="toggleDetail('row_${tc.key}')">
                                                <div class="criteria-main-info">
                                                    <div class="criteria-icon-box">
                                                        <span class="material-symbols-outlined">${tc.icon}</span>
                                                    </div>
                                                    <div class="criteria-text-group">
                                                        <div class="criteria-text-title">
                                                            <h3>${tc.tenTieuChi}</h3>
                                                            <span class="criteria-summary-badge">${tc.tuyChonGiaTri.size()} phân nhóm áp dụng</span>
                                                        </div>
                                                        <p class="criteria-text-subtitle">${tc.moTa}</p>
                                                    </div>
                                                </div>
                                                <div class="criteria-actions" onclick="event.stopPropagation();">
                                                    <div class="criteria-input-wrapper" title="Điểm tối đa của tiêu chí">
                                                        <span class="label">Tối đa:</span>
                                                        <input type="number" id="points_${tc.key}" name="points_${tc.key}"
                                                               value="${tc.soDiem}" min="0" max="100"
                                                               <c:if test="${not tc.kichHoat || not canEditScoringConfig}">disabled</c:if> />
                                                        <span class="unit">đ</span>
                                                    </div>
                                                    <label class="switch" title="Bật / Tắt tiêu chí">
                                                        <input type="checkbox" id="enabled_${tc.key}" name="enabled_${tc.key}"
                                                               data-key="${tc.key}"
                                                               <c:if test="${tc.kichHoat}">checked</c:if>
                                                               <c:if test="${not canEditScoringConfig}">disabled</c:if> />
                                                        <span class="slider"></span>
                                                    </label>
                                                    <button type="button" class="btn-toggle-expand" onclick="toggleDetail('row_${tc.key}')" title="Xem chi tiết các mức điểm con">
                                                        <span class="material-symbols-outlined" style="font-size: 20px;">expand_more</span>
                                                    </button>
                                                </div>
                                            </div>
                                            <div class="criteria-detail-drawer">
                                                <table class="criteria-subtable">
                                                    <thead>
                                                        <tr>
                                                            <th>Phân nhóm giá trị</th>
                                                            <th style="text-align: right; width: 120px;">Điểm cộng</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        <c:forEach items="${tc.tuyChonGiaTri}" var="opt">
                                                            <tr>
                                                                <td>${opt.ten}</td>
                                                                <td class="pts-col">+${opt.diem} điểm</td>
                                                            </tr>
                                                        </c:forEach>
                                                    </tbody>
                                                </table>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </div>
                        </section>

                        <!-- Ghi chú nguyên tắc hệ thống -->
                        <div class="policy-footnote">
                            <span class="material-symbols-outlined icon">verified_user</span>
                            <span><strong>Quy tắc hệ thống:</strong> Điểm số được sử dụng để tối ưu thứ tự tiếp cận cho nhân viên bán hàng; hệ thống tuyệt đối không tự động đóng, xóa hoặc loại bỏ các Lead có điểm thấp.</span>
                        </div>

                    </div>

                    <!-- CỘT PHẢI: BỘ KIỂM TRA MÔ PHỎNG -->
                    <aside class="sidebar-column">
                        <div class="simulator-panel">
                            <div class="sim-header">
                                <h2>
                                    <span class="material-symbols-outlined" style="font-size: 20px; color: var(--cd-primary);">science</span>
                                    <span>Kiểm tra tính điểm</span>
                                </h2>
                                <span class="sim-badge-live">Thời gian thực</span>
                            </div>

                            <!-- Thẻ Điểm Số -->
                            <div class="sim-score-card state-hot" id="simScoreCard">
                                <div class="sim-score-value" id="simScoreValue">100</div>
                                <div class="sim-score-max">/ 100 điểm khả dụng</div>
                                <div id="simTagContainer">
                                    <span class="lead-tag tag-hot" id="simTag">Lead Nóng</span>
                                </div>
                                <div class="sim-action-hint" id="simHint">
                                    Khả năng chốt đơn cao; ưu tiên phân bổ và liên hệ ngay trong 5 phút.
                                </div>
                            </div>

                            <!-- Dropdowns Thử Nghiệm -->
                            <div class="sim-field">
                                <div class="sim-label">
                                    <span>Ngành nghề</span>
                                    <span class="sim-pts-badge" id="ptsBadge_INDUSTRY_FIT">+25đ</span>
                                </div>
                                <select class="sim-select" id="sel_INDUSTRY_FIT">
                                    <option value="25" selected>Phần mềm / CNTT (+25đ)</option>
                                    <option value="25">Tài chính / Ngân hàng (+25đ)</option>
                                    <option value="20">Sản xuất &amp; Bán buôn (+20đ)</option>
                                    <option value="15">Bất động sản (+15đ)</option>
                                    <option value="0">Chưa xác định (0đ)</option>
                                </select>
                            </div>

                            <div class="sim-field">
                                <div class="sim-label">
                                    <span>Quy mô nhân sự</span>
                                    <span class="sim-pts-badge" id="ptsBadge_COMPANY_SIZE">+20đ</span>
                                </div>
                                <select class="sim-select" id="sel_COMPANY_SIZE">
                                    <option value="20" selected>Lớn (&gt; 500 nhân sự) (+20đ)</option>
                                    <option value="15">Vừa lớn (100 - 500 nhân sự) (+15đ)</option>
                                    <option value="10">Vừa (20 - 100 nhân sự) (+10đ)</option>
                                    <option value="5">Nhỏ (&lt; 20 nhân sự) (+5đ)</option>
                                    <option value="0">Chưa xác định (0đ)</option>
                                </select>
                            </div>

                            <div class="sim-field">
                                <div class="sim-label">
                                    <span>Nguồn tiếp cận</span>
                                    <span class="sim-pts-badge" id="ptsBadge_SOURCE">+15đ</span>
                                </div>
                                <select class="sim-select" id="sel_SOURCE">
                                    <option value="15" selected>Khách cũ / Đối tác giới thiệu (+15đ)</option>
                                    <option value="15">Đăng ký Form Website (+15đ)</option>
                                    <option value="12">Hội thảo / Sự kiện (+12đ)</option>
                                    <option value="10">Google Search Ads (+10đ)</option>
                                    <option value="0">Chưa rõ nguồn (0đ)</option>
                                </select>
                            </div>

                            <div class="sim-field">
                                <div class="sim-label">
                                    <span>Mức độ quan tâm</span>
                                    <span class="sim-pts-badge" id="ptsBadge_INTEREST_LEVEL">+40đ</span>
                                </div>
                                <select class="sim-select" id="sel_INTEREST_LEVEL">
                                    <option value="40" selected>Cần triển khai ngay trong tháng (+40đ)</option>
                                    <option value="30">Quan tâm cao — Đang xin ngân sách (+30đ)</option>
                                    <option value="15">Đang tìm hiểu &amp; so sánh giải pháp (+15đ)</option>
                                    <option value="0">Chưa có kế hoạch cụ thể (0đ)</option>
                                </select>
                            </div>
                        </div>
                    </aside>

                </div>
            </form>

        </div>
    </main>

    <!-- Toast Notification -->
    <div id="toast" class="toast">
        <span class="material-symbols-outlined" style="font-size: 20px; color: #60a5fa;">check_circle</span>
        <span id="toastText">Đã lưu cấu hình thành công!</span>
    </div>

    <!-- JS chuyên biệt -->
    <script>
        function toggleDetail(rowId) {
            const row = document.getElementById(rowId);
            if (row) {
                row.classList.toggle('is-expanded');
            }
        }
    </script>
    <script src="${pageContext.request.contextPath}/assets/js/lead/cham-diem.js"></script>
</body>
</html>
