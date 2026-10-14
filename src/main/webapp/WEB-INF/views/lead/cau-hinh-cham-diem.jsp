<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Cấu hình quy tắc chấm điểm và phân loại Lead (Story S4-05) - CRM Bán Hàng">
    <title>Cấu Hình Chấm Điểm Lead - CRM Bán Hàng</title>

    <!-- CSS dùng chung toàn hệ thống -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">

    <!-- CSS chuyên biệt module Chấm điểm Lead (Story S4-05) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lead/cham-diem.css">
</head>
<body class="crm-body">
    <!-- Navigation layout dùng chung S1-06 (đã bao gồm sidebar, thông tin user và Material Symbols) -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <div class="cd-container">

            <!-- 1. Breadcrumb điều hướng chuẩn -->
            <nav class="cd-breadcrumb" aria-label="Breadcrumb">
                <ol class="cd-breadcrumb-nav">
                    <li><a href="${pageContext.request.contextPath}/dieu-huong" class="cd-breadcrumb-link">Trang chủ</a></li>
                    <span class="cd-breadcrumb-sep" aria-hidden="true">/</span>
                    <li><a href="${pageContext.request.contextPath}/lead" class="cd-breadcrumb-link">Lead & Phân bổ</a></li>
                    <span class="cd-breadcrumb-sep" aria-hidden="true">/</span>
                    <li class="cd-breadcrumb-current" aria-current="page">Cấu hình chấm điểm Lead</li>
                </ol>
                <div>
                    <a href="${pageContext.request.contextPath}/dieu-huong" class="cd-btn cd-btn-outline" id="btnQuayLaiTrangChu">
                        <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span>
                        <span>Trang chủ</span>
                    </a>
                </div>
            </nav>

            <!-- 2. Header Card & Trạng thái phiên bản cấu hình -->
            <header class="cd-header-card">
                <div class="cd-header-title">
                    <h1>
                        <span class="material-symbols-outlined" style="color: #60a5fa; font-size: 32px;" aria-hidden="true">tune</span>
                        <span>Cấu Hình Chấm Điểm Lead (Lead Scoring Rules)</span>
                    </h1>
                    <p>Thiết lập tiêu chí đánh giá tiềm năng và phân loại Nóng, Ấm, Lạnh nhằm tối ưu hóa thứ tự tiếp cận của nhân viên kinh doanh (Story S4-05).</p>
                </div>
                <div class="cd-version-tag">
                    <span class="material-symbols-outlined" style="font-size: 18px;" aria-hidden="true">history</span>
                    <span>Phiên bản: <strong>v${scoringConfig.phienBan}</strong></span>
                </div>
            </header>

            <!-- 3. Thông báo Thành công / Thất bại (Flash Alert) -->
            <c:if test="${not empty thongBaoThanhCong}">
                <div class="cd-alert cd-alert-success" role="alert">
                    <span class="material-symbols-outlined cd-alert-icon" aria-hidden="true">check_circle</span>
                    <div>
                        <strong>Thành công!</strong> ${thongBaoThanhCong}
                    </div>
                </div>
            </c:if>

            <c:if test="${not empty thongBaoLoi}">
                <div class="cd-alert cd-alert-danger" role="alert">
                    <span class="material-symbols-outlined cd-alert-icon" aria-hidden="true">error</span>
                    <div>
                        <strong>Lỗi:</strong> ${thongBaoLoi}
                    </div>
                </div>
            </c:if>

            <c:if test="${not canEditScoringConfig}">
                <div class="cd-alert cd-alert-info" role="status">
                    <span class="material-symbols-outlined cd-alert-icon" aria-hidden="true">info</span>
                    <div>
                        <strong>Chế độ chỉ xem:</strong> Bạn đang xem cấu hình chấm điểm lead hiện hành. Chỉ vai trò <strong>Giám đốc kinh doanh</strong> hoặc <strong>Quản trị viên</strong> mới có quyền cập nhật các thông số này.
                    </div>
                </div>
            </c:if>

            <!-- 4. Form Cấu hình Chấm điểm (POST /lead/cham-diem) -->
            <form id="formCauHinhChamDiem" action="${pageContext.request.contextPath}/lead/cham-diem" method="POST">
                <div class="cd-layout-grid">

                    <!-- CỘT CHÍNH: CẤU HÌNH NGƯỠNG & TIÊU CHÍ -->
                    <div class="cd-main-column">

                        <!-- SECTION 1: CẤU HÌNH NGƯỠNG PHÂN LOẠI (AC3) -->
                        <section class="cd-card">
                            <div class="cd-card-header">
                                <div>
                                    <h2 class="cd-card-title">
                                        <span class="material-symbols-outlined" style="color: var(--cd-hot);" aria-hidden="true">speed</span>
                                        <span>1. Ngưỡng Phân Loại Lead (AC3)</span>
                                    </h2>
                                    <p class="cd-card-desc">Thiết lập mốc điểm số để hệ thống tự động gắn nhãn mức độ tiềm năng Nóng, Ấm, Lạnh cho từng Lead.</p>
                                </div>
                            </div>

                            <div class="cd-thresholds-grid">
                                <!-- Khối Ngưỡng Lạnh -->
                                <div class="cd-threshold-box cold">
                                    <div class="cd-threshold-header">
                                        <span class="cd-threshold-badge cold">
                                            <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">ac_unit</span>
                                            <span>LEAD LẠNH</span>
                                        </span>
                                    </div>
                                    <div class="cd-threshold-explain">
                                        Từ <strong>0</strong> đến dưới <span id="labelColdMax">${thresholds.warmMin}</span> điểm
                                    </div>
                                    <p class="cd-threshold-explain" style="margin: 0; font-size: 0.75rem;">
                                        Nhu cầu chưa cấp thiết; nuôi dưỡng định kỳ qua email/marketing.
                                    </p>
                                </div>

                                <!-- Khối Ngưỡng Ấm -->
                                <div class="cd-threshold-box warm">
                                    <div class="cd-threshold-header">
                                        <span class="cd-threshold-badge warm">
                                            <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">wb_sunny</span>
                                            <span>LEAD ẤM</span>
                                        </span>
                                    </div>
                                    <div class="cd-threshold-input-wrap">
                                        <span style="font-size: 0.8125rem; font-weight: 600;">Từ:</span>
                                        <input type="number" id="warmMin" name="warmMin" class="cd-threshold-input"
                                               value="${thresholds.warmMin}" min="0" max="99" required
                                               <c:if test="${not canEditScoringConfig}">disabled</c:if> />
                                        <span style="font-size: 0.8125rem; font-weight: 600;">điểm</span>
                                    </div>
                                    <p class="cd-threshold-explain" style="margin: 0; font-size: 0.75rem;">
                                        Có nhu cầu rõ ràng; phân bổ cho Sales liên hệ trong 24 giờ.
                                    </p>
                                </div>

                                <!-- Khối Ngưỡng Nóng -->
                                <div class="cd-threshold-box hot">
                                    <div class="cd-threshold-header">
                                        <span class="cd-threshold-badge hot">
                                            <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">local_fire_department</span>
                                            <span>LEAD NÓNG</span>
                                        </span>
                                    </div>
                                    <div class="cd-threshold-input-wrap">
                                        <span style="font-size: 0.8125rem; font-weight: 600;">Từ:</span>
                                        <input type="number" id="hotMin" name="hotMin" class="cd-threshold-input"
                                               value="${thresholds.hotMin}" min="1" max="100" required
                                               <c:if test="${not canEditScoringConfig}">disabled</c:if> />
                                        <span style="font-size: 0.8125rem; font-weight: 600;">điểm</span>
                                    </div>
                                    <p class="cd-threshold-explain" style="margin: 0; font-size: 0.75rem;">
                                        Cần giải pháp gấp & ngân sách sẵn sàng; ưu tiên xử lý trong 5 phút!
                                    </p>
                                </div>
                            </div>

                            <!-- Thanh dải trực quan Visual Range Bar -->
                            <div class="cd-range-bar-container">
                                <div class="cd-range-bar-label">
                                    <span>Trực quan hóa dải phân bổ điểm số:</span>
                                    <span>Thang điểm: 0 – 100 điểm</span>
                                </div>
                                <div class="cd-range-track" aria-label="Biểu đồ dải ngưỡng điểm">
                                    <div class="cd-range-segment cold" id="segCold" title="Dải điểm Lạnh"></div>
                                    <div class="cd-range-segment warm" id="segWarm" title="Dải điểm Ấm"></div>
                                    <div class="cd-range-segment hot" id="segHot" title="Dải điểm Nóng"></div>
                                </div>
                                <div class="cd-range-ticks">
                                    <span>0đ</span>
                                    <span id="tickWarm">${thresholds.warmMin}đ</span>
                                    <span id="tickHot">${thresholds.hotMin}đ</span>
                                    <span>100đ</span>
                                </div>
                            </div>
                        </section>

                        <!-- SECTION 2: CẤU HÌNH TIÊU CHÍ CHẤM ĐIỂM (AC1) -->
                        <section class="cd-card">
                            <div class="cd-card-header">
                                <div>
                                    <h2 class="cd-card-title">
                                        <span class="material-symbols-outlined" style="color: var(--cd-primary);" aria-hidden="true">fact_check</span>
                                        <span>2. Tiêu Chí & Trọng Số Điểm (AC1)</span>
                                    </h2>
                                    <p class="cd-card-desc">Khai báo và bật/tắt 4 nhóm tiêu chí chấm điểm theo đúng hợp đồng nghiệp vụ.</p>
                                </div>
                            </div>

                            <div class="cd-criteria-list">
                                <c:forEach items="${criteriaRows}" var="tc">
                                    <div class="cd-criterion-card <c:if test='${not tc.kichHoat}'>disabled</c:if>">
                                        <div class="cd-criterion-main-row">
                                            <div class="cd-criterion-info">
                                                <div class="cd-criterion-icon">
                                                    <span class="material-symbols-outlined" aria-hidden="true">${tc.icon}</span>
                                                </div>
                                                <div>
                                                    <div class="cd-criterion-name">
                                                        <span>${tc.tenTieuChi}</span>
                                                    </div>
                                                    <div class="cd-criterion-desc">${tc.moTa}</div>
                                                </div>
                                            </div>

                                            <div class="cd-criterion-controls">
                                                <div class="cd-points-input-wrap">
                                                    <label for="points_${tc.key}" class="cd-points-label">Điểm tối đa:</label>
                                                    <input type="number" id="points_${tc.key}" name="points_${tc.key}"
                                                           class="cd-points-input" value="${tc.soDiem}" min="0" max="100"
                                                           <c:if test="${not tc.kichHoat || not canEditScoringConfig}">disabled</c:if> />
                                                    <span class="cd-points-label">đ</span>
                                                </div>

                                                <!-- Switch Toggle -->
                                                <label class="cd-switch" title="Bật/Tắt tiêu chí">
                                                    <input type="checkbox" id="enabled_${tc.key}" name="enabled_${tc.key}"
                                                           data-criterion-key="${tc.key}"
                                                           <c:if test="${tc.kichHoat}">checked</c:if>
                                                           <c:if test="${not canEditScoringConfig}">disabled</c:if> />
                                                    <span class="cd-slider"></span>
                                                </label>
                                            </div>
                                        </div>

                                        <!-- Chi tiết các mức điểm con thuộc nhóm -->
                                        <div class="cd-criterion-subtable">
                                            <div class="cd-subtable-title">Các giá trị và điểm thành phần được áp dụng:</div>
                                            <div class="cd-subtable-rows">
                                                <c:forEach items="${tc.tuyChonGiaTri}" var="opt">
                                                    <div class="cd-subtable-item">
                                                        <span class="cd-subtable-item-name" title="${opt.ten}">${opt.ten}</span>
                                                        <span class="cd-subtable-item-points">+${opt.diem}đ</span>
                                                    </div>
                                                </c:forEach>
                                            </div>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </section>

                        <!-- SECTION 3: NGUYÊN TẮC HỆ THỐNG AC4 -->
                        <section class="cd-notice-box">
                            <span class="material-symbols-outlined cd-notice-icon" aria-hidden="true">verified_user</span>
                            <div class="cd-notice-content">
                                <h3>Nguyên Tắc Hệ Thống: Không Tự Động Loại Lead (AC4)</h3>
                                <p>
                                    Điểm số và nhãn phân loại (Nóng, Ấm, Lạnh) chỉ phục vụ mục đích <strong>sắp xếp mức độ ưu tiên tiếp cận</strong> cho đội ngũ kinh doanh. Hệ thống <strong>tuyệt đối không tự động hủy, xóa, đóng hoặc chuyển trạng thái</strong> của bất kỳ lead nào chỉ vì điểm số thấp. Toàn bộ lead tiếp nhận đều được lưu trữ và có kế hoạch chăm sóc phù hợp.
                                </p>
                            </div>
                        </section>

                        <!-- HÀNH ĐỘNG SUBMIT -->
                        <c:if test="${canEditScoringConfig}">
                            <div class="cd-form-actions">
                                <a href="${pageContext.request.contextPath}/lead/cham-diem" class="cd-btn cd-btn-outline" id="btnHuy">
                                    <span class="material-symbols-outlined" aria-hidden="true">refresh</span>
                                    <span>Hủy thay đổi</span>
                                </a>
                                <button type="submit" class="cd-btn cd-btn-primary" id="btnLuuCauHinh">
                                    <span class="cd-spinner" id="spinnerLuu"></span>
                                    <span class="material-symbols-outlined" aria-hidden="true">save</span>
                                    <span>Lưu Cấu Hình Chấm Điểm</span>
                                </button>
                            </div>
                        </c:if>

                    </div>

                    <!-- CỘT PHỤ: MÔ PHỎNG TÍNH ĐIỂM THỜI GIAN THỰC (LIVE SIMULATOR - AC2 & AC4) -->
                    <aside class="cd-simulator-card" aria-label="Hộp kiểm tra tính điểm tự động">
                        <div class="cd-simulator-header">
                            <span class="material-symbols-outlined" style="color: var(--cd-primary); font-size: 26px;" aria-hidden="true">calculate</span>
                            <h2>Mô Phỏng Tính Điểm (AC2)</h2>
                        </div>

                        <p style="margin: 0; font-size: 0.8125rem; color: var(--cd-text-muted); line-height: 1.45;">
                            Chọn nhanh thông tin thử nghiệm để kiểm tra thuật toán tính điểm và phân loại tức thì khi thông tin Lead thay đổi.
                        </p>

                        <!-- Kết quả Điểm số & Nhãn phân loại -->
                        <div class="cd-sim-result-box" id="simResultBox">
                            <div style="display: flex; align-items: baseline; gap: 4px;">
                                <span class="cd-sim-score-number" id="simScoreNumber">0</span>
                                <span class="cd-sim-score-total">/ 100đ</span>
                            </div>
                            <div class="cd-sim-badge" id="simBadge">
                                <span class="material-symbols-outlined" style="font-size: 18px;" aria-hidden="true">local_fire_department</span>
                                <span id="simBadgeText">LEAD NÓNG</span>
                            </div>
                            <div class="cd-sim-status-note" id="simStatusNote">
                                Đang tính toán tự động...
                            </div>
                        </div>

                        <!-- Các trường dữ liệu mẫu thử nghiệm -->
                        <div class="cd-sim-form-group">
                            <label for="simNganhNghe" class="cd-sim-label">
                                <span>Ngành nghề:</span>
                                <span class="cd-sim-points-awarded" id="simPtsNganh">+0đ</span>
                            </label>
                            <select id="simNganhNghe" class="cd-sim-select">
                                <option value="IT_PHAN_MEM" data-diem="25" selected>Công nghệ thông tin / Phần mềm (+25đ)</option>
                                <option value="TAI_CHINH_NGAN_HANG" data-diem="25">Tài chính / Ngân hàng (+25đ)</option>
                                <option value="SAN_XUAT_PHAN_PHOI" data-diem="20">Sản xuất / Phân phối (+20đ)</option>
                                <option value="BAT_DONG_SAN" data-diem="15">Bất động sản & Xây dựng (+15đ)</option>
                                <option value="BAN_LE_FMCG" data-diem="15">Bán lẻ / FMCG (+15đ)</option>
                                <option value="DICH_VU_KHAC" data-diem="10">Dịch vụ thương mại khác (+10đ)</option>
                                <option value="NULL_CHOICE" data-diem="0">-- Chưa có thông tin (0đ) --</option>
                            </select>
                        </div>

                        <div class="cd-sim-form-group">
                            <label for="simQuyMo" class="cd-sim-label">
                                <span>Quy mô doanh nghiệp:</span>
                                <span class="cd-sim-points-awarded" id="simPtsQuyMo">+0đ</span>
                            </label>
                            <select id="simQuyMo" class="cd-sim-select">
                                <option value="TREN_500" data-diem="20" selected>Doanh nghiệp lớn (> 500 nhân sự) (+20đ)</option>
                                <option value="100_500" data-diem="15">Doanh nghiệp vừa lớn (100 - 500 nhân sự) (+15đ)</option>
                                <option value="20_100" data-diem="10">Doanh nghiệp vừa (20 - 100 nhân sự) (+10đ)</option>
                                <option value="DUOI_20" data-diem="5">Doanh nghiệp nhỏ (< 20 nhân sự) (+5đ)</option>
                                <option value="NULL_CHOICE" data-diem="0">-- Chưa có thông tin (0đ) --</option>
                            </select>
                        </div>

                        <div class="cd-sim-form-group">
                            <label for="simNguonLead" class="cd-sim-label">
                                <span>Nguồn thu thập:</span>
                                <span class="cd-sim-points-awarded" id="simPtsNguon">+0đ</span>
                            </label>
                            <select id="simNguonLead" class="cd-sim-select">
                                <option value="GIOI_THIEU" data-diem="15" selected>Khách cũ / Đối tác giới thiệu (+15đ)</option>
                                <option value="WEB_FORM" data-diem="15">Đăng ký biểu mẫu Website (+15đ)</option>
                                <option value="HOI_THAO" data-diem="12">Sự kiện / Hội thảo chuyên ngành (+12đ)</option>
                                <option value="QUANG_CAO_GOOGLE" data-diem="10">Quảng cáo Google Search (+10đ)</option>
                                <option value="MANG_XA_HOI" data-diem="8">Mạng xã hội Facebook Ads (+8đ)</option>
                                <option value="OUTBOUND_COLD" data-diem="5">Gọi điện Outbound tiếp cận (+5đ)</option>
                                <option value="NULL_CHOICE" data-diem="0">-- Chưa rõ nguồn (0đ) --</option>
                            </select>
                        </div>

                        <div class="cd-sim-form-group">
                            <label for="simMucDoQuanTam" class="cd-sim-label">
                                <span>Mức độ quan tâm:</span>
                                <span class="cd-sim-points-awarded" id="simPtsQuanTam">+0đ</span>
                            </label>
                            <select id="simMucDoQuanTam" class="cd-sim-select">
                                <option value="CAN_GAP" data-diem="40" selected>Rất cấp thiết — Cần triển khai ngay (+40đ)</option>
                                <option value="QUAN_TAM_CAO" data-diem="30">Quan tâm cao — Đang xin ngân sách (+30đ)</option>
                                <option value="TIM_HIEU" data-diem="15">Đang tìm hiểu & so sánh giải pháp (+15đ)</option>
                                <option value="CHUA_RO" data-diem="5">Chưa có kế hoạch cụ thể (+5đ)</option>
                                <option value="NULL_CHOICE" data-diem="0">-- Chưa xác định (0đ) --</option>
                            </select>
                        </div>
                    </aside>

                </div>
            </form>

        </div>
    </main>

    <!-- Toast Notification -->
    <div id="cdToast" class="cd-toast" role="status" aria-live="polite">
        <span class="material-symbols-outlined" style="color: #60a5fa;" aria-hidden="true">info</span>
        <span id="cdToastMsg">Thông báo</span>
    </div>

    <!-- JS chuyên biệt Module Chấm Điểm Lead (Story S4-05) -->
    <script src="${pageContext.request.contextPath}/assets/js/lead/cham-diem.js"></script>
</body>
</html>
