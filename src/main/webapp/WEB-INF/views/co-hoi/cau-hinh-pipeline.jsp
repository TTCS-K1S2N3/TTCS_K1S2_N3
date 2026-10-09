<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cấu hình Giai đoạn Pipeline & Dự báo Doanh số | CRM Bán Hàng</title>
    <meta name="description" content="Quản lý cấu hình chuỗi giai đoạn pipeline bán hàng, xác suất thắng mặc định và điều kiện bắt buộc rời giai đoạn để dự báo doanh số chính xác theo số liệu thực tế.">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/co-hoi/pipeline.css">
</head>
<body>

<div class="crm-container">

    <!-- Breadcrumb Navigation -->
    <nav class="breadcrumb-nav" aria-label="Đường dẫn điều hướng">
        <a href="${pageContext.request.contextPath}/dieu-huong">Trang chủ</a>
        <span class="sep">/</span>
        <a href="${pageContext.request.contextPath}/danh-muc">Danh mục & Cấu hình</a>
        <span class="sep">/</span>
        <span class="current">Cấu hình Pipeline & Xác suất Thắng</span>
    </nav>

    <!-- Header & Giả lập phân quyền xem -->
    <header class="crm-header">
        <div class="crm-header-title">
            <h1>Cấu hình Giai đoạn Pipeline & Xác suất Thắng</h1>
            <p>
                Thiết lập chuỗi quy trình bán hàng chuẩn hoá, xác suất thắng trọng số theo từng bước và tiêu chuẩn bắt buộc rời giai đoạn để con số dự báo doanh số có cơ sở khoa học thay vì dựa vào cảm nhận.
            </p>
        </div>

        <div class="role-switcher-card" style="display: flex; align-items: center; gap: 10px;">
            <a href="${pageContext.request.contextPath}/danh-muc" class="btn btn-outline" id="btnQuayLaiDanhMuc" title="Quay lại Danh mục cấu hình">
                <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span> Danh mục cấu hình
            </a>
            <span class="role-label">Vai trò:</span>
            <c:choose>
                <c:when test="${coQuyenCauHinh}">
                    <span class="role-pill role-pill-director" title="Tài khoản có quyền cấu hình pipeline">
                        Giám đốc kinh doanh / Quản trị
                    </span>
                </c:when>
                <c:otherwise>
                    <span class="role-pill role-pill-sales" title="Tài khoản chỉ xem">
                        Nhân viên kinh doanh (Chỉ xem)
                    </span>
                </c:otherwise>
            </c:choose>
        </div>
    </header>

    <!-- Flash Messages (Thông báo thành công / lỗi) -->
    <c:if test="${not empty thongBaoThanhCong}">
        <div class="alert alert-success" role="alert">
            <span><strong>Thành công:</strong> <c:out value="${thongBaoThanhCong}"/></span>
            <button type="button" class="alert-close-btn" onclick="dongThongBao(this)" aria-label="Đóng" title="Đóng">
                <span class="material-symbols-outlined" aria-hidden="true">close</span>
            </button>
        </div>
    </c:if>

    <c:if test="${not empty thongBaoLoi}">
        <div class="alert alert-danger" role="alert">
            <span><strong>Cảnh báo:</strong> <c:out value="${thongBaoLoi}"/></span>
            <button type="button" class="alert-close-btn" onclick="dongThongBao(this)" aria-label="Đóng" title="Đóng">
                <span class="material-symbols-outlined" aria-hidden="true">close</span>
            </button>
        </div>
    </c:if>

    <!-- KPI Summary Metrics Bar -->
    <section class="kpi-grid" aria-label="Chỉ số tổng quan quy trình pipeline">
        <!-- 1. Tổng số giai đoạn -->
        <div class="kpi-card">
            <div class="kpi-icon-box icon-blue">
                <span class="material-symbols-outlined icon-md" aria-hidden="true">view_timeline</span>
            </div>
            <div class="kpi-info">
                <div class="kpi-label">Số bước quy trình</div>
                <div class="kpi-value">${danhSachGiaiDoan.size()} bước</div>
                <div class="kpi-sub">Chuỗi phễu bán hàng chuẩn</div>
            </div>
        </div>

        <!-- 2. Tính toán tổng số cơ hội và giá trị -->
        <c:set var="tongSoCoHoi" value="0"/>
        <c:set var="tongGiaTriRaw" value="0"/>
        <c:set var="tongDuBaoRaw" value="0"/>
        <c:forEach var="item" items="${duBaoPipeline}">
            <c:set var="tongSoCoHoi" value="${tongSoCoHoi + item.soLuongCoHoi}"/>
            <c:set var="tongGiaTriRaw" value="${tongGiaTriRaw + item.tongGiaTriCoHoi}"/>
            <c:set var="tongDuBaoRaw" value="${tongDuBaoRaw + item.doanhSoDuBao}"/>
        </c:forEach>

        <!-- 3. Tổng cơ hội mở -->
        <div class="kpi-card">
            <div class="kpi-icon-box icon-blue">
                <span class="material-symbols-outlined icon-md" aria-hidden="true">schedule</span>
            </div>
            <div class="kpi-info">
                <div class="kpi-label">Cơ hội đang chạy (AC 4)</div>
                <div class="kpi-value">${tongSoCoHoi} cơ hội</div>
                <div class="kpi-sub">Bảo toàn nguyên vẹn khi cấu hình</div>
            </div>
        </div>

        <!-- 4. Tổng giá trị cơ hội mở -->
        <div class="kpi-card">
            <div class="kpi-icon-box icon-blue">
                <span class="material-symbols-outlined icon-md" aria-hidden="true">trending_up</span>
            </div>
            <div class="kpi-info">
                <div class="kpi-label">Tổng giá trị phễu mở</div>
                <div class="kpi-value">
                    <fmt:formatNumber value="${tongGiaTriRaw}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                </div>
                <div class="kpi-sub">Giá trị danh nghĩa kỳ vọng 100%</div>
            </div>
        </div>

        <!-- 5. Doanh số dự báo có trọng số (Weighted Forecast - AC 2) -->
        <div class="kpi-card">
            <div class="kpi-icon-box icon-emerald">
                <span class="material-symbols-outlined icon-md" aria-hidden="true">payments</span>
            </div>
            <div class="kpi-info">
                <div class="kpi-label">Doanh số dự báo trọng số (AC 2)</div>
                <div class="kpi-value" style="color: var(--success-600);">
                    <fmt:formatNumber value="${tongDuBaoRaw}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                </div>
                <div class="kpi-sub">Tính từ xác suất từng giai đoạn</div>
            </div>
        </div>
    </section>

    <!-- AC 1: Chuỗi quy trình Pipeline bán hàng trực quan (Visual Stepper Flow) -->
    <section class="crm-card" aria-labelledby="heading-flow">
        <div class="card-header-bar">
            <div class="card-heading">
                <div>
                    <h2 id="heading-flow">Chuỗi quy trình Pipeline bán hàng chuẩn hoá (AC 1)</h2>
                    <p class="card-subtext">Quy trình tuyến tính nối tiếp: Tiếp cận <span class="material-symbols-outlined icon-xs" aria-hidden="true" style="vertical-align: -3px;">arrow_forward</span> Xác định nhu cầu <span class="material-symbols-outlined icon-xs" aria-hidden="true" style="vertical-align: -3px;">arrow_forward</span> Đề xuất giải pháp <span class="material-symbols-outlined icon-xs" aria-hidden="true" style="vertical-align: -3px;">arrow_forward</span> Báo giá <span class="material-symbols-outlined icon-xs" aria-hidden="true" style="vertical-align: -3px;">arrow_forward</span> Đàm phán <span class="material-symbols-outlined icon-xs" aria-hidden="true" style="vertical-align: -3px;">arrow_forward</span> Chốt</p>
                </div>
            </div>

            <c:if test="${coQuyenCauHinh}">
                <a href="${pageContext.request.contextPath}/pipeline/giai-doan/tao"
                   class="btn btn-primary"
                   id="btnThemGiaiDoan">
                    <span class="material-symbols-outlined" aria-hidden="true">add</span> Thêm giai đoạn mới
                </a>
            </c:if>
        </div>

        <c:choose>
            <c:when test="${empty danhSachGiaiDoan}">
                <div class="empty-state">
                    <div class="empty-state-icon">
                        <span class="material-symbols-outlined" style="font-size: 48px;" aria-hidden="true">inbox</span>
                    </div>
                    <h3>Chưa có giai đoạn pipeline nào</h3>
                    <p>Hệ thống cần ít nhất một giai đoạn để nhân viên kinh doanh có thể ghi nhận cơ hội bán hàng.</p>
                    <c:if test="${coQuyenCauHinh}">
                        <a href="${pageContext.request.contextPath}/pipeline/giai-doan/tao" class="btn btn-primary">
                            Khởi tạo giai đoạn đầu tiên
                        </a>
                    </c:if>
                </div>
            </c:when>
            <c:otherwise>
                <div class="pipeline-stepper-container">
                    <div class="pipeline-stepper">
                        <c:forEach var="gd" items="${danhSachGiaiDoan}">
                            <div class="stepper-item ${gd.thanhCong ? 'item-won' : (gd.thatBai ? 'item-lost' : (gd.dangApDung ? 'item-active' : 'item-inactive'))}">
                                <div class="stepper-top">
                                    <span class="stepper-order">Bước ${gd.thuTu}</span>
                                    <span class="stepper-prob-badge ${gd.thanhCong ? 'prob-won' : (gd.thatBai ? 'prob-lost' : '')}">
                                        ${gd.xacSuatThang}%
                                    </span>
                                </div>

                                <div class="stepper-name" title="<c:out value="${gd.tenGiaiDoan}"/>"><c:out value="${gd.tenGiaiDoan}"/></div>

                                <!-- Progress bar xác suất -->
                                <div class="stepper-progress">
                                    <div class="stepper-progress-fill ${gd.thanhCong ? 'fill-won' : (gd.thatBai ? 'fill-lost' : '')}"
                                         style="width: ${gd.xacSuatThang}%;"></div>
                                </div>

                                <!-- Thẻ số lượng cơ hội đang chạy (AC 4) -->
                                <c:if test="${gd.soCoHoiHienTai > 0}">
                                    <div class="stepper-deals-tag" title="Đang có ${gd.soCoHoiHienTai} cơ hội hoạt động ở bước này">
                                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">push_pin</span> ${gd.soCoHoiHienTai} cơ hội mở
                                    </div>
                                </c:if>

                                <!-- Điều kiện rời giai đoạn (AC 3) -->
                                <c:choose>
                                    <c:when test="${not empty gd.dieuKienBatBuoc}">
                                        <div class="stepper-rule-tag" title="Điều kiện bắt buộc: <c:out value="${gd.dieuKienBatBuoc}"/>">
                                            <span class="material-symbols-outlined icon-xs" aria-hidden="true">lock</span> <c:out value="${gd.dieuKienBatBuoc}"/>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div style="font-size: 11px; color: var(--gray-400); font-style: italic;">
                                            Không có điều kiện đặc biệt
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <!-- AC 2: Bảng dự báo doanh số trọng số & Máy tính mô phỏng thời gian thực -->
    <section class="crm-card" aria-labelledby="heading-forecast">
        <div class="card-header-bar">
            <div class="card-heading">
                <span class="material-symbols-outlined" aria-hidden="true" style="color: var(--primary-600);">trending_up</span>
                <div>
                    <h2 id="heading-forecast">Dự báo doanh số theo xác suất thắng giai đoạn (AC 2)</h2>
                    <p class="card-subtext">
                        Công thức: <strong>Doanh số dự báo = Giá trị cơ hội &times; (Xác suất thắng / 100)</strong>
                    </p>
                </div>
            </div>
        </div>

        <div class="forecast-summary-box">
            <!-- Bảng dự báo theo giai đoạn -->
            <div class="table-responsive">
                <table class="crm-table">
                    <thead>
                    <tr>
                        <th>Giai đoạn</th>
                        <th style="text-align: center;">Xác suất thắng</th>
                        <th style="text-align: center;">Cơ hội mở</th>
                        <th style="text-align: right;">Tổng giá trị phễu</th>
                        <th style="text-align: right; color: var(--primary-700);">Doanh số dự báo</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="db" items="${duBaoPipeline}">
                        <tr>
                            <td>
                                <strong><c:out value="${db.tenGiaiDoan}"/></strong>
                            </td>
                            <td style="text-align: center;">
                                <span class="badge badge-prob">${db.xacSuatThang}%</span>
                            </td>
                            <td style="text-align: center;">
                                <c:choose>
                                    <c:when test="${db.soLuongCoHoi > 0}">
                                        <span class="badge badge-open-deals">${db.soLuongCoHoi}</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span style="color: var(--gray-400);">0</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td style="text-align: right; font-weight: 500;">
                                ${db.tongGiaTriDinhDang}
                            </td>
                            <td style="text-align: right; font-weight: 700; color: var(--primary-600);">
                                ${db.doanhSoDuBaoDinhDang}
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                    <tfoot>
                    <tr>
                        <td colspan="2">TỔNG CỘNG DỰ BÁO TOÀN PIPELINE:</td>
                        <td style="text-align: center;">${tongSoCoHoi} cơ hội</td>
                        <td style="text-align: right;">
                            <fmt:formatNumber value="${tongGiaTriRaw}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                        </td>
                        <td style="text-align: right; color: var(--success-700); font-size: 15px;">
                            <fmt:formatNumber value="${tongDuBaoRaw}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                        </td>
                    </tr>
                    </tfoot>
                </table>
            </div>

            <!-- Bộ mô phỏng tính nhanh dự báo doanh số thời gian thực (Live Forecast Simulator) -->
            <div class="simulator-card">
                <div class="simulator-header">
                    <span style="display: flex; align-items: center; gap: 6px;">
                        <span class="material-symbols-outlined" aria-hidden="true" style="color: var(--warning-500);">bolt</span>
                        Máy tính dự báo cơ hội mẫu (Real-time Simulator)
                    </span>
                </div>
                <p style="font-size: 12.5px; color: var(--gray-600); margin-bottom: 14px;">
                    Thử nghiệm công thức dự báo ngay lập tức để kiểm chứng tính toán theo từng kịch bản xác suất:
                </p>

                <!-- Input giá trị -->
                <div class="sim-input-group">
                    <label for="simGiaTriCoHoi">Giá trị cơ hội (VNĐ):</label>
                    <input type="number" id="simGiaTriCoHoi" class="form-group" value="200000000" min="0" step="5000000"
                           style="width: 100%; font-weight: 700; font-size: 15px; padding: 8px 12px; border: 1.5px solid var(--gray-300); border-radius: var(--radius-md);">
                    <div class="sim-presets">
                        <button type="button" class="btn-preset" onclick="datPresetGiaTri(50000000)">50 triệu</button>
                        <button type="button" class="btn-preset" onclick="datPresetGiaTri(100000000)">100 triệu</button>
                        <button type="button" class="btn-preset" onclick="datPresetGiaTri(300000000)">300 triệu</button>
                        <button type="button" class="btn-preset" onclick="datPresetGiaTri(500000000)">500 triệu</button>
                        <button type="button" class="btn-preset" onclick="datPresetGiaTri(1000000000)">10 tỷ</button>
                    </div>
                </div>

                <!-- Chọn giai đoạn -->
                <div class="sim-input-group">
                    <label for="simGiaiDoan">Giai đoạn thực tế của cơ hội:</label>
                    <select id="simGiaiDoan" style="width: 100%; padding: 8px 12px; border: 1.5px solid var(--gray-300); border-radius: var(--radius-md); font-family: inherit;">
                        <c:forEach var="gd" items="${danhSachGiaiDoan}">
                            <c:if test="${gd.dangApDung}">
                                <option value="${gd.xacSuatThang}" ${gd.thuTu == 4 ? 'selected' : ''}>
                                    Bước ${gd.thuTu}: ${gd.tenGiaiDoan} (Xác suất: ${gd.xacSuatThang}%)
                                </option>
                            </c:if>
                        </c:forEach>
                    </select>
                </div>

                <!-- Tùy chỉnh slider xác suất -->
                <div class="sim-input-group">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px;">
                        <label for="simSliderProb" style="margin-bottom: 0;">Trọng số xác suất:</label>
                        <strong id="simValProb" style="color: var(--primary-600); font-size: 14px;">70%</strong>
                    </div>
                    <input type="range" id="simSliderProb" class="slider-range" min="0" max="100" value="70">
                </div>

                <!-- Kết quả hiển thị tức thì -->
                <div class="simulator-result-box">
                    <div class="sim-result-label">Doanh số dự báo trọng số (Forecast)</div>
                    <div class="sim-result-value" id="simKetQuaDuBao">140.000.000 ₫</div>
                    <div class="sim-result-formula" id="simFormulaText">Công thức: 200.000.000 ₫ &times; 70% = 140.000.000 ₫</div>
                </div>
            </div>
        </div>
    </section>

    <!-- AC 1, AC 3, AC 4: Chi tiết cấu hình chuỗi giai đoạn & Điều kiện bắt buộc rời giai đoạn -->
    <section class="crm-card" aria-labelledby="heading-stages-table">
        <div class="card-header-bar">
            <div class="card-heading">
                <span class="material-symbols-outlined" aria-hidden="true" style="color: var(--primary-600);">tune</span>
                <div>
                    <h2 id="heading-stages-table">Danh sách chi tiết cấu hình giai đoạn & Điều kiện rời (AC 3, AC 4)</h2>
                    <p class="card-subtext">Quản lý thứ tự chuỗi quy trình, tiêu chuẩn bắt buộc rời giai đoạn và bảo toàn cơ hội đang chạy</p>
                </div>
            </div>

            <!-- Ô tìm kiếm / lọc nhanh giai đoạn -->
            <div style="display: flex; gap: 10px; align-items: center;">
                <input type="text" id="timKiemGiaiDoan" placeholder="Lọc theo tên hoặc mã..."
                       style="padding: 7px 14px; border: 1.5px solid var(--gray-300); border-radius: var(--radius-md); font-size: 13px; width: 220px; outline: none;">
            </div>
        </div>

        <div class="table-responsive">
            <table class="crm-table" id="bangGiaiDoan">
                <thead>
                <tr>
                    <th style="width: 70px; text-align: center;">Thứ tự</th>
                    <th style="width: 110px;">Mã</th>
                    <th>Tên giai đoạn & Phân loại</th>
                    <th style="text-align: center; width: 100px;">Xác suất (AC 2)</th>
                    <th>Điều kiện bắt buộc để rời giai đoạn (AC 3)</th>
                    <th style="text-align: center; width: 110px;">Cơ hội đang chạy (AC 4)</th>
                    <th style="text-align: center; width: 120px;">Trạng thái</th>
                    <th style="text-align: center; width: 220px;">Thao tác</th>
                </tr>
                </thead>
                <tbody id="bangGiaiDoanBody">
                <c:forEach var="gd" items="${danhSachGiaiDoan}" varStatus="loop">
                    <tr>
                        <!-- Thứ tự và nút di chuyển lên xuống -->
                        <td style="text-align: center;">
                            <div style="display: inline-flex; align-items: center; gap: 4px;">
                                <strong style="font-size: 15px; color: var(--gray-900);">${gd.thuTu}</strong>
                                <c:if test="${coQuyenCauHinh}">
                                    <div class="order-control-box">
                                        <c:if test="${not loop.first}">
                                            <form method="post" action="${pageContext.request.contextPath}/pipeline/giai-doan/doi-thu-tu" style="margin: 0;">
                                                <input type="hidden" name="id1" value="${gd.id}">
                                                <input type="hidden" name="id2" value="${danhSachGiaiDoan[loop.index - 1].id}">
                                                <button type="submit" class="btn-order" title="Chuyển lên trước bước <c:out value="${danhSachGiaiDoan[loop.index - 1].tenGiaiDoan}"/>" aria-label="Chuyển lên trước">
                                                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">arrow_upward</span>
                                                </button>
                                            </form>
                                        </c:if>
                                        <c:if test="${not loop.last}">
                                            <form method="post" action="${pageContext.request.contextPath}/pipeline/giai-doan/doi-thu-tu" style="margin: 0;">
                                                <input type="hidden" name="id1" value="${gd.id}">
                                                <input type="hidden" name="id2" value="${danhSachGiaiDoan[loop.index + 1].id}">
                                                <button type="submit" class="btn-order" title="Chuyển xuống sau bước <c:out value="${danhSachGiaiDoan[loop.index + 1].tenGiaiDoan}"/>" aria-label="Chuyển xuống sau">
                                                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">arrow_downward</span>
                                                </button>
                                            </form>
                                        </c:if>
                                    </div>
                                </c:if>
                            </div>
                        </td>

                        <!-- Mã giai đoạn -->
                        <td>
                            <code style="background: var(--gray-100); padding: 2px 6px; border-radius: var(--radius-sm); font-size: 12px; font-weight: 600; color: var(--primary-700);">
                                <c:out value="${gd.maGiaiDoan}"/>
                            </code>
                        </td>

                        <!-- Tên giai đoạn & Phân loại -->
                        <td>
                            <strong style="color: var(--gray-900); font-size: 14.5px;"><c:out value="${gd.tenGiaiDoan}"/></strong>
                            <div style="font-size: 12px; color: var(--gray-500); margin-top: 2px;">
                                <c:choose>
                                    <c:when test="${gd.thanhCong}">
                                         <span style="color: var(--success-700); font-weight: 600;">
                                             <span class="material-symbols-outlined icon-xs" aria-hidden="true" style="color: var(--success-700);">flag</span> Chốt thành công
                                         </span>
                                    </c:when>
                                    <c:when test="${gd.thatBai}">
                                         <span style="color: var(--danger-600); font-weight: 600;">
                                             <span class="material-symbols-outlined icon-xs" aria-hidden="true" style="color: var(--danger-600);">cancel</span> Đóng thất bại
                                         </span>
                                    </c:when>
                                    <c:otherwise>
                                         <span>
                                             <span class="material-symbols-outlined icon-xs" aria-hidden="true">sync</span> Đang tiến hành bán hàng
                                         </span>
                                    </c:otherwise>
                                </c:choose>
                                &bull; Đình trệ: &gt;${gd.soNgayCanhBaoDinhTre} ngày
                            </div>
                        </td>

                        <!-- Xác suất thắng mặc định (AC 2) -->
                        <td style="text-align: center;">
                            <span class="badge badge-prob" style="font-size: 13px;">${gd.xacSuatThang}%</span>
                        </td>

                        <!-- Điều kiện bắt buộc để rời một giai đoạn (AC 3) -->
                        <td>
                            <c:choose>
                                <c:when test="${not empty gd.dieuKienBatBuoc}">
                                    <div style="font-size: 13.5px; font-weight: 600; color: #0f172a;">
                                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">lock</span> <c:out value="${gd.dieuKienBatBuoc}"/>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <span style="font-size: 12.5px; color: var(--gray-400); font-style: italic;">
                                        Chưa có mô tả quy định
                                    </span>
                                </c:otherwise>
                            </c:choose>

                            <!-- Danh sách các thẻ tiêu chí định lượng -->
                            <div class="rule-pill-list">
                                <c:if test="${gd.soCuocGapToiThieu > 0}">
                                    <span class="rule-pill rule-pill-meeting" title="Bắt buộc có ít nhất ${gd.soCuocGapToiThieu} cuộc gặp">
                                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">handshake</span> &ge; ${gd.soCuocGapToiThieu} cuộc gặp
                                    </span>
                                </c:if>
                                <c:if test="${gd.soCuocGoiToiThieu > 0}">
                                    <span class="rule-pill rule-pill-call" title="Bắt buộc có ít nhất ${gd.soCuocGoiToiThieu} cuộc gọi">
                                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">call</span> &ge; ${gd.soCuocGoiToiThieu} cuộc gọi
                                    </span>
                                </c:if>
                                <c:if test="${gd.yeuCauBaoGia}">
                                    <span class="rule-pill rule-pill-quote" title="Bắt buộc phải tạo báo giá niêm yết">
                                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">description</span> Cần gửi báo giá
                                    </span>
                                </c:if>
                                <c:if test="${gd.yeuCauKhaoSatNhuCau}">
                                    <span class="rule-pill rule-pill-survey" title="Bắt buộc xác nhận khảo sát nhu cầu">
                                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">assignment</span> Khảo sát nhu cầu
                                    </span>
                                </c:if>
                                <c:if test="${gd.soCuocGapToiThieu == 0 && gd.soCuocGoiToiThieu == 0 && !gd.yeuCauBaoGia && !gd.yeuCauKhaoSatNhuCau}">
                                    <span class="rule-pill" style="color: var(--gray-400);">Không ràng buộc định lượng</span>
                                </c:if>
                            </div>
                        </td>

                        <!-- Số cơ hội đang chạy & Bảo vệ dữ liệu (AC 4) -->
                        <td style="text-align: center;">
                            <c:choose>
                                <c:when test="${gd.soCoHoiHienTai > 0}">
                                    <span class="badge badge-open-deals" title="Có ${gd.soCoHoiHienTai} cơ hội đang chạy. Hệ thống bảo vệ dữ liệu, không làm gián đoạn hay mất mát.">
                                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">push_pin</span> ${gd.soCoHoiHienTai} cơ hội
                                    </span>
                                </c:when>
                                <c:otherwise>
                                    <span style="font-size: 13px; color: var(--gray-400);">0 cơ hội</span>
                                </c:otherwise>
                            </c:choose>
                        </td>

                        <!-- Trạng thái áp dụng -->
                        <td style="text-align: center;">
                            <c:choose>
                                <c:when test="${gd.dangApDung}">
                                    <span class="badge badge-active">Đang áp dụng</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge badge-inactive">Ngừng áp dụng</span>
                                </c:otherwise>
                            </c:choose>
                        </td>

                        <!-- Cột Thao tác -->
                        <td style="text-align: center; white-space: nowrap;">
                            <!-- Nút thử nghiệm điều kiện rời giai đoạn (AC 3) -->
                            <button type="button" class="btn btn-outline btn-sm"
                                    data-giai-doan-id="${gd.id}"
                                    data-stage-id="${gd.id}"
                                    data-id="${gd.id}"
                                    data-ten-giai-doan="<c:out value='${gd.tenGiaiDoan}'/>"
                                    data-cuoc-gap="${gd.soCuocGapToiThieu}"
                                    data-cuoc-goi="${gd.soCuocGoiToiThieu}"
                                    data-bao-gia="${gd.yeuCauBaoGia}"
                                    data-khao-sat="${gd.yeuCauKhaoSatNhuCau}"
                                    data-dieu-kien-bat-buoc="<c:out value='${gd.dieuKienBatBuoc}'/>"
                                    onclick="moModalDieuKienTuBtn(this)"
                                    title="Thử nghiệm kiểm tra cơ hội có đủ điều kiện rời giai đoạn hay không">
                                <span class="material-symbols-outlined icon-xs" aria-hidden="true">tune</span> Thử điều kiện
                            </button>

                        <c:if test="${coQuyenCauHinh}">
                            <!-- Nút Sửa -->
                            <a href="${pageContext.request.contextPath}/pipeline/giai-doan/sua?id=${gd.id}"
                               class="btn btn-outline btn-sm"
                               title="Chỉnh sửa xác suất, tên, hoặc tiêu chuẩn điều kiện">
                                <span class="material-symbols-outlined icon-xs" aria-hidden="true">edit</span> Sửa
                            </a>

                            <!-- Nút Chuyển trạng thái (Đang áp dụng <-> Ngừng áp dụng) -->
                            <form method="post" action="${pageContext.request.contextPath}/pipeline/giai-doan/trang-thai" style="display:inline;">
                                <input type="hidden" name="id" value="${gd.id}">
                                <c:choose>
                                    <c:when test="${gd.dangApDung}">
                                        <input type="hidden" name="trangThai" value="NGUNG_AP_DUNG">
                                        <button type="submit" class="btn btn-outline btn-sm" style="color: var(--warning-600);"
                                                onclick="return confirm('Chuyển giai đoạn này sang trạng thái Ngừng áp dụng? Các cơ hội đang chạy vẫn được bảo toàn nguyên vẹn.')"
                                                title="Tạm ngừng nhận cơ hội mới vào giai đoạn này">
                                            <span class="material-symbols-outlined icon-xs" aria-hidden="true">pause_circle</span> Ngừng
                                        </button>
                                    </c:when>
                                    <c:otherwise>
                                        <input type="hidden" name="trangThai" value="DANG_AP_DUNG">
                                        <button type="submit" class="btn btn-outline btn-sm" style="color: var(--success-600);"
                                                title="Kích hoạt lại giai đoạn này trong chuỗi pipeline">
                                            <span class="material-symbols-outlined icon-xs" aria-hidden="true">play_circle</span> Dùng lại
                                        </button>
                                    </c:otherwise>
                                </c:choose>
                            </form>

                            <!-- AC 4: Nút Xóa có ràng buộc bảo vệ cơ hội đang chạy -->
                            <c:choose>
                                <c:when test="${gd.soCoHoiHienTai > 0}">
                                    <!-- Đang có cơ hội: Chặn xóa cứng, giải thích cho người dùng -->
                                    <button type="button" class="btn btn-outline btn-sm" style="color: var(--gray-400); cursor: help;"
                                            data-ten-giai-doan="<c:out value='${gd.tenGiaiDoan}'/>"
                                            data-so-co-hoi="${gd.soCoHoiHienTai}"
                                            onclick="xacNhanXoaTuBtn(null, this)"
                                            title="Đang có ${gd.soCoHoiHienTai} cơ hội đang chạy. Hệ thống bảo vệ dữ liệu, không cho phép xoá cứng. Nhấn để xem chi tiết.">
                                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">block</span> Không thể xoá
                                    </button>
                                </c:when>
                                <c:otherwise>
                                    <!-- Không có cơ hội nào: Cho phép xóa với modal xác nhận -->
                                    <form method="post" action="${pageContext.request.contextPath}/pipeline/giai-doan/xoa" style="display:inline;">
                                        <input type="hidden" name="id" value="${gd.id}">
                                        <button type="button" class="btn btn-danger btn-sm"
                                                data-ten-giai-doan="<c:out value='${gd.tenGiaiDoan}'/>"
                                                data-so-co-hoi="0"
                                                onclick="xacNhanXoaTuBtn(this.form, this)"
                                                title="Xoá vĩnh viễn giai đoạn khỏi hệ thống">
                                            <span class="material-symbols-outlined icon-xs" aria-hidden="true">delete</span> Xoá
                                        </button>
                                    </form>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </section>

</div>

<!-- MODAL 1: Kiểm tra Điều kiện Rời Giai đoạn (AC 3) -->
<div id="modalKiemTraDieuKien" class="modal-overlay" role="dialog" aria-modal="true" aria-labelledby="modalTitleDieuKien"
     data-endpoint="${pageContext.request.contextPath}/pipeline/giai-doan/kiem-tra-dieu-kien">
    <div class="modal-card">
        <h3 id="modalTitleDieuKien" class="modal-title">
            <span>Kiểm tra Điều kiện Rời Giai đoạn (AC 3)</span>
        </h3>
        <p class="modal-desc">
            Hệ thống tự động kiểm tra xem hoạt động thực tế của cơ hội đã đủ điều kiện để chuyển sang giai đoạn tiếp theo hay chưa.
        </p>

        <!-- Thông tin giai đoạn đang thử nghiệm -->
        <div style="background: var(--gray-50); border: 1px solid var(--gray-200); padding: 14px; border-radius: var(--radius-md); margin-bottom: 16px;">
            <div style="font-size: 14px;"><strong>Giai đoạn:</strong> <span id="modalTenGiaiDoan" style="color: var(--primary-600); font-weight: 700;"></span></div>
            <div style="font-size: 12.5px; color: #b45309; margin-top: 4px;"><strong>Quy định:</strong> <span id="modalMoTaDieuKien"></span></div>
            <div id="modalChecklistQuyDinh"></div>
        </div>

        <input type="hidden" id="modalGiaiDoanId">

        <!-- Dữ liệu mô phỏng của cơ hội -->
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-bottom: 14px;">
            <div class="form-group">
                <label for="testSoGap">Số cuộc gặp đã tổ chức:</label>
                <input type="number" id="testSoGap" min="0" value="0">
                <span class="form-hint">Số lần gặp gỡ trực tiếp với khách</span>
            </div>
            <div class="form-group">
                <label for="testSoGoi">Số cuộc gọi kết nối:</label>
                <input type="number" id="testSoGoi" min="0" value="0">
                <span class="form-hint">Số cuộc gọi đã thực hiện</span>
            </div>
        </div>

        <div style="display: flex; flex-direction: column; gap: 10px; margin-bottom: 16px;">
            <label class="checkbox-group">
                <input type="checkbox" id="testBaoGia">
                <span>Đã tạo và gửi Báo giá niêm yết cho khách hàng</span>
            </label>
            <label class="checkbox-group">
                <input type="checkbox" id="testNhuCau">
                <span>Đã hoàn thành và xác nhận bảng khảo sát nhu cầu</span>
            </label>
        </div>

        <!-- Hộp kết quả kiểm tra AJAX -->
        <div id="modalKetQuaDieuKien"></div>

        <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px;">
            <button type="button" class="btn btn-outline" onclick="dongModalDieuKien()">Đóng</button>
            <button type="button" class="btn btn-primary" onclick="thucHienKiemTraDieuKien()">
                Xác minh điều kiện
            </button>
        </div>
    </div>
</div>

<!-- MODAL 2: Cảnh báo bảo vệ cơ hội đang chạy (AC 4) -->
<div id="modalBaoVeCoHoi" class="modal-overlay" role="dialog" aria-modal="true" aria-labelledby="modalTitleBaoVe">
    <div class="modal-card">
        <h3 id="modalTitleBaoVe" class="modal-title" style="color: var(--danger-600);">
            <span>Bảo vệ Cơ hội Đang Chạy (AC 4)</span>
        </h3>
        <p class="modal-desc">
            Hệ thống không cho phép xoá cứng giai đoạn đang liên kết với các cơ hội bán hàng nhằm bảo toàn dữ liệu lịch sử và phễu doanh thu.
        </p>

        <div style="background: var(--danger-50); border: 1px solid var(--danger-200); padding: 16px; border-radius: var(--radius-md); margin-bottom: 18px;">
            <div style="font-weight: 700; color: var(--danger-700); margin-bottom: 6px;">
                Giai đoạn: <span id="tenGiaiDoanBaoVe"></span>
            </div>
            <div style="font-size: 13.5px; color: var(--gray-700); line-height: 1.5;">
                Hiện đang có <strong id="soCoHoiBaoVe" style="color: var(--danger-700);"></strong> cơ hội bán hàng đang ở giai đoạn này.
                Nếu xoá cứng, các cơ hội trên sẽ bị mồ côi hoặc sai lệch báo cáo dự báo.
            </div>
            <div style="font-size: 13px; color: var(--gray-600); margin-top: 10px;">
                <strong>Giải pháp đề xuất:</strong> Bạn hãy chuyển giai đoạn này sang trạng thái <strong>Ngừng áp dụng</strong>. Khi đó, không cơ hội mới nào được đưa vào giai đoạn này, đồng thời các cơ hội cũ vẫn được lưu giữ an toàn.
            </div>
        </div>

        <div style="display: flex; justify-content: flex-end; gap: 10px;">
            <button type="button" class="btn btn-primary" onclick="dongModalBaoVeCoHoi()">Tôi đã hiểu</button>
        </div>
    </div>
</div>

<!-- MODAL 3: Xác nhận xoá giai đoạn rỗng -->
<div id="modalXacNhanXoa" class="modal-overlay" role="dialog" aria-modal="true" aria-labelledby="modalTitleXoa">
    <div class="modal-card">
        <h3 id="modalTitleXoa" class="modal-title" style="color: var(--danger-600);">
            <span>Xác nhận Xoá Giai đoạn</span>
        </h3>
        <p class="modal-desc">
            Hành động này sẽ xoá hoàn toàn giai đoạn khỏi chuỗi quy trình pipeline. Giai đoạn này hiện không có cơ hội nào đang chạy.
        </p>

        <div style="background: var(--gray-50); border: 1px solid var(--gray-200); padding: 14px; border-radius: var(--radius-md); margin-bottom: 18px;">
            <div>Bạn có chắc chắn muốn xoá giai đoạn: <strong id="tenGiaiDoanCanXoa" style="color: var(--gray-900);"></strong>?</div>
        </div>

        <div style="display: flex; justify-content: flex-end; gap: 10px;">
            <button type="button" class="btn btn-outline" onclick="dongModalXacNhanXoa()">Hủy bỏ</button>
            <button type="button" class="btn btn-danger" onclick="dongYThucHienXoa()">Xác nhận Xoá</button>
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/assets/js/co-hoi/pipeline.js"></script>
</body>
</html>
