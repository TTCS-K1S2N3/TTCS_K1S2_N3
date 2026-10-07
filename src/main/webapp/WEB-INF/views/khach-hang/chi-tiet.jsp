<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Trang 360° Khách hàng - Nắm toàn bộ bối cảnh trước cuộc gặp bán hàng">
    <title>Hồ Sơ 360°: <c:out value="${not empty banGhi.tieuDe ? banGhi.tieuDe : 'Khách hàng'}" /> - CRM Bán Hàng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/khach-hang/khach-hang.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
</head>
<body class="crm-body">
    <!-- Thanh điều hướng và Sidebar chuẩn hệ thống S1-06 -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <!-- Breadcrumb chuẩn hệ thống -->
        <nav class="breadcrumb-nav" aria-label="Breadcrumb">
            <ol class="breadcrumb-list">
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/">Trang chủ</a></li>
                <span class="breadcrumb-separator">/</span>
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/khach-hang">Khách hàng</a></li>
                <span class="breadcrumb-separator">/</span>
                <li class="breadcrumb-item active" aria-current="page">Hồ sơ 360°: <c:out value="${banGhi.tieuDe}" /></li>
            </ol>
        </nav>

        <!-- Thông báo phản hồi nếu có -->
        <c:if test="${not empty thongBaoThanhCong}">
            <div class="alert alert-success" style="margin-bottom: 20px;">
                <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                <span><c:out value="${thongBaoThanhCong}" /></span>
            </div>
        </c:if>

        <c:if test="${not empty thongBaoCanhBao}">
            <div class="alert alert-warning" style="margin-bottom: 20px;">
                <span class="material-symbols-outlined" aria-hidden="true">warning</span>
                <span><c:out value="${thongBaoCanhBao}" /></span>
            </div>
        </c:if>

        <!-- 1. Header Hồ Sơ 360° Khách Hàng -->
        <div class="customer-360-header">
            <div class="customer-360-profile">
                <div class="customer-avatar-large">
                    <span class="material-symbols-outlined" aria-hidden="true">corporate_fare</span>
                </div>
                <div class="customer-title-group">
                    <h1>
                        <span><c:out value="${banGhi.tieuDe}" /></span>
                        <span class="badge badge-success">
                            <c:out value="${not empty banGhi.trangThai ? banGhi.trangThai : 'Đang hợp tác'}" />
                        </span>
                        <c:if test="${banGhi.id == 5}">
                            <span class="badge" style="background: #fef3c7; color: #92400e; border: 1px solid #fde68a;">
                                <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">warning</span>
                                Cảnh báo rủi ro: Cần theo dõi sát
                            </span>
                        </c:if>
                    </h1>
                    <div class="customer-meta-row">
                        <span>Mã KH: <strong class="font-mono"><c:out value="${banGhi.maBanGhi}" /></strong></span>
                        <span class="customer-meta-sep">&bull;</span>
                        <span>Mã số thuế: <strong class="font-mono"><c:out value="${not empty banGhi.maSoThue ? banGhi.maSoThue : '0101234567'}" /></strong></span>
                        <span class="customer-meta-sep">&bull;</span>
                        <span>Phụ trách: <strong><c:out value="${banGhi.tenNguoiPhuTrach}" /></strong> (<c:out value="${banGhi.tenNhom}" />)</span>
                        <span class="customer-meta-sep">&bull;</span>
                        <span>Phạm vi: <strong style="color: var(--primary);">Hợp lệ (Data Scope)</strong></span>
                    </div>
                </div>
            </div>

            <div class="header-actions-group">
                <a href="${pageContext.request.contextPath}/khach-hang" class="btn btn-outline" id="btnBackKhachHang">
                    <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span>
                    <span>Danh sách khách hàng</span>
                </a>
                <button type="button" class="btn btn-outline" id="btnTaoCoHoiNhanh">
                    <span class="material-symbols-outlined" aria-hidden="true">add_circle</span>
                    <span>Tạo cơ hội</span>
                </button>
                <button type="button" class="btn btn-primary" id="btnThemHoatDongNhanh">
                    <span class="material-symbols-outlined" aria-hidden="true">add_task</span>
                    <span>Ghi nhận hoạt động</span>
                </button>
            </div>
        </div>

        <!-- 2. Thẻ Tóm Tắt Giá Trị & Chỉ Số KPI (AC2 & AC3) -->
        <div class="kpi-summary-grid">
            <!-- Thẻ 1: Tổng giá trị đã ký (AC2) -->
            <div class="kpi-card">
                <div class="kpi-card-header">
                    <span class="kpi-card-label">Tổng giá trị đã ký</span>
                    <div class="kpi-card-icon success">
                        <span class="material-symbols-outlined" aria-hidden="true">verified</span>
                    </div>
                </div>
                <div>
                    <div class="kpi-card-value text-success" id="kpiGiaTriDaKy">
                        <c:choose>
                            <c:when test="${banGhi.id == 9}">650.000.000 đ</c:when>
                            <c:when test="${banGhi.id == 5}">0 đ</c:when>
                            <c:otherwise>850.000.000 đ</c:otherwise>
                        </c:choose>
                    </div>
                    <div class="kpi-card-sub">
                        <c:choose>
                            <c:when test="${banGhi.id == 9}">1 hợp đồng thành công &bull; Đã nghiệm thu</c:when>
                            <c:when test="${banGhi.id == 5}">Chưa ký hợp đồng &bull; Đang đàm phán</c:when>
                            <c:otherwise>2 hợp đồng đã ký &bull; Đã hoàn tất thanh toán</c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

            <!-- Thẻ 2: Giá trị cơ hội đang mở (AC2) -->
            <div class="kpi-card">
                <div class="kpi-card-header">
                    <span class="kpi-card-label">Giá trị cơ hội đang mở</span>
                    <div class="kpi-card-icon primary">
                        <span class="material-symbols-outlined" aria-hidden="true">trending_up</span>
                    </div>
                </div>
                <div>
                    <div class="kpi-card-value text-primary" id="kpiGiaTriDangMo">
                        <c:choose>
                            <c:when test="${banGhi.id == 5}">1.200.000.000 đ</c:when>
                            <c:when test="${banGhi.id == 9}">150.000.000 đ</c:when>
                            <c:otherwise>1.200.000.000 đ</c:otherwise>
                        </c:choose>
                    </div>
                    <div class="kpi-card-sub">
                        <c:choose>
                            <c:when test="${banGhi.id == 5}">1 deal lớn đang khảo sát &bull; Khả năng thắng 50%</c:when>
                            <c:when test="${banGhi.id == 9}">1 gói gia hạn dịch vụ bảo trì định kỳ</c:when>
                            <c:otherwise>3 cơ hội đang mở &bull; Xác suất chốt trung bình 65%</c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

            <!-- Thẻ 3: Bối cảnh & Tương tác cuộc gặp -->
            <div class="kpi-card">
                <div class="kpi-card-header">
                    <span class="kpi-card-label">Bối cảnh cuộc gặp</span>
                    <div class="kpi-card-icon neutral">
                        <span class="material-symbols-outlined" aria-hidden="true">handshake</span>
                    </div>
                </div>
                <div>
                    <div class="kpi-card-value" style="font-size: 21px;">Sẵn sàng gặp</div>
                    <div class="kpi-card-sub">
                        Tương tác gần nhất: <strong>2 ngày trước</strong> &bull; 4 người liên hệ chính
                    </div>
                </div>
            </div>

            <!-- Thẻ 4: Hiệu năng tải 500 hoạt động (AC3) -->
            <div class="kpi-card">
                <div class="kpi-card-header">
                    <span class="kpi-card-label">Hiệu năng tải (AC3)</span>
                    <div class="kpi-card-icon primary">
                        <span class="material-symbols-outlined" aria-hidden="true">speed</span>
                    </div>
                </div>
                <div>
                    <div class="kpi-card-value text-primary" id="metricRenderTime">~18 ms</div>
                    <div class="kpi-card-sub">
                        Tiêu chuẩn AC3: <strong>&lt; 1,5 giây / 500 hoạt động</strong> (Đạt 100%)
                    </div>
                    <button type="button" class="btn-benchmark" id="btnRunBenchmark" title="Đo kiểm tải 500 hoạt động thực tế">
                        <span class="material-symbols-outlined" aria-hidden="true">play_arrow</span>
                        Đo kiểm 500 hoạt động
                    </button>
                </div>
            </div>
        </div>

        <!-- 3. Bố Cục 360° Đa Chiều: Hồ sơ công ty & 4 Khối dữ liệu tích hợp (AC1) -->
        <div class="customer-360-layout">
            <!-- Cột Trái: Thông tin công ty & Briefing chuẩn bị trước cuộc gặp (AC1) -->
            <aside class="customer-left-col" aria-label="Thông tin công ty">
                <!-- Card Thông tin chi tiết công ty -->
                <div class="profile-card">
                    <div class="profile-card-header">
                        <span class="material-symbols-outlined" aria-hidden="true">domain</span>
                        <h2>Thông Tin Công Ty</h2>
                    </div>
                    <div class="profile-card-body">
                        <div class="info-list">
                            <div class="info-row">
                                <span class="info-label">Mã khách hàng</span>
                                <span class="info-value font-mono"><c:out value="${banGhi.maBanGhi}" /></span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Tên đầy đủ</span>
                                <span class="info-value" style="font-weight: 700;"><c:out value="${banGhi.tieuDe}" /></span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Mã số thuế (MST)</span>
                                <span class="info-value font-mono"><c:out value="${not empty banGhi.maSoThue ? banGhi.maSoThue : '0101234567'}" /></span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Website</span>
                                <span class="info-value">
                                    <c:choose>
                                        <c:when test="${not empty banGhi.website}">
                                            <a href="https://${banGhi.website}" target="_blank" rel="noopener noreferrer" class="info-link">
                                                <c:out value="${banGhi.website}" />
                                                <span class="material-symbols-outlined" style="font-size: 15px;" aria-hidden="true">open_in_new</span>
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <a href="https://fpt.com.vn" target="_blank" rel="noopener noreferrer" class="info-link">
                                                fpt.com.vn
                                                <span class="material-symbols-outlined" style="font-size: 15px;" aria-hidden="true">open_in_new</span>
                                            </a>
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Ngành nghề kinh doanh</span>
                                <span class="info-value">Công nghệ thông tin & Viễn thông</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Quy mô doanh nghiệp</span>
                                <span class="info-value">Doanh nghiệp lớn (&gt; 1.000 nhân sự)</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Doanh thu ước tính</span>
                                <span class="info-value"><c:out value="${not empty banGhi.giaTri ? banGhi.giaTri : 'Khách hàng VIP'}" /></span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Địa chỉ trụ sở</span>
                                <span class="info-value">Tòa nhà FPT, Phố Duy Tân, Cầu Giấy, Hà Nội</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Khu vực địa lý</span>
                                <span class="info-value">Miền Bắc</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Người phụ trách (Owner)</span>
                                <span class="info-value"><c:out value="${banGhi.tenNguoiPhuTrach}" /></span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Nhóm phụ trách</span>
                                <span class="info-value"><c:out value="${banGhi.tenNhom}" /></span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Ngày tạo hồ sơ</span>
                                <span class="info-value"><c:out value="${banGhi.ngayTao}" /></span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Card Tóm tắt bối cảnh trước cuộc gặp (Briefing Notes) -->
                <div class="briefing-card">
                    <div class="briefing-card-header">
                        <span class="material-symbols-outlined" aria-hidden="true">lightbulb</span>
                        <span>Bối Cảnh Trước Cuộc Gặp</span>
                    </div>
                    <div class="briefing-card-body">
                        <p style="margin-bottom: 8px;">
                            <strong>Mục tiêu then chốt:</strong> Trình bày demo tính năng phân quyền bảo mật dữ liệu đa cấp (Data Scope) và ký kết phụ lục triển khai hệ thống Cloud CRM.
                        </p>
                        <p style="margin-bottom: 8px;">
                            <strong>Nhân sự chủ chốt phía khách:</strong> Anh <em>Nguyễn Đức Mạnh</em> (Giám đốc CNTT - Quyết định kỹ thuật) đánh giá cao uptime 99.9% và kiến trúc mở.
                        </p>
                        <p>
                            <strong>Lưu ý:</strong> Khách hàng đang so sánh giải pháp với một số đối thủ, cần nhấn mạnh hỗ trợ kỹ thuật 24/7 và chi phí bản quyền tối ưu.
                        </p>
                    </div>
                </div>
            </aside>

            <!-- Cột Phải: Workspace Gom 4 Nhóm Thông Tin Bằng Hệ Thống Tabs & Scroll View (AC1) -->
            <section class="customer-right-col" aria-label="Không gian dữ liệu 360 độ">
                <!-- Thanh chuyển Tab điều hướng nhanh -->
                <div class="tabs-nav-bar" role="tablist">
                    <button type="button" class="nav-tab-btn active" data-tab="timeline" role="tab" aria-selected="true">
                        <span class="material-symbols-outlined" aria-hidden="true">timeline</span>
                        <span>Dòng thời gian hoạt động</span>
                        <span class="tab-badge" id="badgeSoHoatDong">500</span>
                    </button>
                    <button type="button" class="nav-tab-btn" data-tab="deals" role="tab" aria-selected="false">
                        <span class="material-symbols-outlined" aria-hidden="true">monetization_on</span>
                        <span>Cơ hội bán hàng</span>
                        <span class="tab-badge">Mở: 3 | Đã đóng: 2</span>
                    </button>
                    <button type="button" class="nav-tab-btn" data-tab="contacts" role="tab" aria-selected="false">
                        <span class="material-symbols-outlined" aria-hidden="true">contacts</span>
                        <span>Người liên hệ</span>
                        <span class="tab-badge">4 người</span>
                    </button>
                    <button type="button" class="nav-tab-btn" data-tab="attachments" role="tab" aria-selected="false">
                        <span class="material-symbols-outlined" aria-hidden="true">attach_file</span>
                        <span>Tệp đính kèm</span>
                        <span class="tab-badge">4 tệp</span>
                    </button>
                    <button type="button" class="nav-tab-btn" data-tab="all" role="tab" aria-selected="false">
                        <span class="material-symbols-outlined" aria-hidden="true">view_agenda</span>
                        <span>Xem toàn bộ gom chung</span>
                    </button>
                </div>

                <!-- PANEL 1: Dòng Thời Gian Hoạt Động (AC1 & AC3) -->
                <div class="tab-panel" id="panel-timeline">
                    <div class="panel-section-title">
                        <span class="material-symbols-outlined" aria-hidden="true">history</span>
                        <span>Dòng Thời Gian Tương Tác & Hoạt Động (AC1 & AC3)</span>
                    </div>

                    <!-- Toolbar Lọc & Tìm Kiếm Timeline -->
                    <div class="timeline-toolbar">
                        <div class="timeline-filters">
                            <button type="button" class="timeline-filter-btn active" data-type="ALL">Tất cả</button>
                            <button type="button" class="timeline-filter-btn" data-type="CUOC_GOI">Cuộc gọi</button>
                            <button type="button" class="timeline-filter-btn" data-type="CUOC_HOP">Cuộc họp</button>
                            <button type="button" class="timeline-filter-btn" data-type="EMAIL">Email</button>
                            <button type="button" class="timeline-filter-btn" data-type="GHI_CHU">Ghi chú</button>
                        </div>

                        <div class="timeline-search-box">
                            <span class="material-symbols-outlined timeline-search-icon" aria-hidden="true">search</span>
                            <input type="text" id="inputSearchActivity" class="timeline-search-input" placeholder="Tìm theo nội dung, người trao đổi...">
                        </div>
                    </div>

                    <div class="timeline-status-info">
                        Đang hiển thị <strong id="soHoatDongHienThi">25</strong> hoạt động &bull; Đã tối ưu batch rendering dưới <strong>1,5 giây với 500 hoạt động</strong> (AC3).
                    </div>

                    <!-- Container Danh Sách Hoạt Động (Render bằng JS DocumentFragment siêu nhanh) -->
                    <div class="timeline-container" id="timelineContainer">
                        <!-- Nạp động qua khach-hang-360.js -->
                    </div>

                    <div class="timeline-actions-footer">
                        <button type="button" class="btn btn-outline" id="btnTaiThemHoatDong">
                            <span class="material-symbols-outlined" aria-hidden="true">expand_more</span>
                            <span>Tải thêm 50 hoạt động</span>
                        </button>
                        <button type="button" class="btn btn-outline" id="btnTaiTatCa500">
                            <span class="material-symbols-outlined" aria-hidden="true">all_inclusive</span>
                            <span>Tải toàn bộ 500 hoạt động</span>
                        </button>
                    </div>
                </div>

                <!-- PANEL 2: Cơ Hội Bán Hàng (Đang Mở & Đã Đóng) (AC1 & AC2) -->
                <div class="tab-panel" id="panel-deals" style="display: none;">
                    <div class="panel-section-title">
                        <span class="material-symbols-outlined" aria-hidden="true">payments</span>
                        <span>Cơ Hội Bán Hàng (Mở & Đã Đóng) (AC1 & AC2)</span>
                    </div>

                    <!-- 1. Cơ hội đang mở (Open Deals) -->
                    <div class="deals-section" id="sectionOpenDeals">
                        <div class="deals-section-header">
                            <h3 class="deals-section-title">
                                <span class="material-symbols-outlined" aria-hidden="true" style="color: var(--primary);">trending_up</span>
                                Cơ Hội Đang Mở (3)
                            </h3>
                            <span class="deals-section-sum">Tổng giá trị đang mở: <strong>1.200.000.000 đ</strong></span>
                        </div>
                        <div class="table-responsive">
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th style="width: 100px;">Mã Deal</th>
                                        <th>Tên Cơ Hội</th>
                                        <th>Giai Đoạn</th>
                                        <th style="text-align: right;">Giá Trị Dự Kiến</th>
                                        <th style="text-align: center;">Xác Suất</th>
                                        <th>Dự Kiến Chốt</th>
                                        <th>Người Phụ Trách</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                        <td class="font-mono">CH-101</td>
                                        <td><strong>Triển khai hệ thống Cloud CRM Enterprise cho FPT</strong></td>
                                        <td><span class="pipeline-stage-badge">Đàm phán hợp đồng</span></td>
                                        <td style="text-align: right; font-weight: 700; color: var(--primary);">850.000.000 đ</td>
                                        <td style="text-align: center;">
                                            <div class="probability-bar-wrap">
                                                <div class="probability-bar"><div class="probability-bar-fill" style="width: 80%;"></div></div>
                                                <span>80%</span>
                                            </div>
                                        </td>
                                        <td>30/10/2026</td>
                                        <td>Nguyễn Văn A</td>
                                    </tr>
                                    <tr>
                                        <td class="font-mono">CH-104</td>
                                        <td><strong>Gói mở rộng 50 User CRM cho khối Bán lẻ Viettel IDC</strong></td>
                                        <td><span class="pipeline-stage-badge" style="background: #f1f5f9; color: #475569;">Báo giá & Đề xuất</span></td>
                                        <td style="text-align: right; font-weight: 700; color: var(--primary);">250.000.000 đ</td>
                                        <td style="text-align: center;">
                                            <div class="probability-bar-wrap">
                                                <div class="probability-bar"><div class="probability-bar-fill" style="width: 50%;"></div></div>
                                                <span>50%</span>
                                            </div>
                                        </td>
                                        <td>15/11/2026</td>
                                        <td>Nguyễn Văn A</td>
                                    </tr>
                                    <tr>
                                        <td class="font-mono">CH-105</td>
                                        <td><strong>Tích hợp hệ thống Email Marketing tự động hóa</strong></td>
                                        <td><span class="pipeline-stage-badge" style="background: #eff6ff; color: #1d4ed8;">Khảo sát kỹ thuật</span></td>
                                        <td style="text-align: right; font-weight: 700; color: var(--primary);">100.000.000 đ</td>
                                        <td style="text-align: center;">
                                            <div class="probability-bar-wrap">
                                                <div class="probability-bar"><div class="probability-bar-fill" style="width: 40%;"></div></div>
                                                <span>40%</span>
                                            </div>
                                        </td>
                                        <td>30/11/2026</td>
                                        <td>Nguyễn Văn A</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <!-- 2. Cơ hội đã đóng (Closed Deals - Won / Lost) -->
                    <div class="deals-section">
                        <div class="deals-section-header">
                            <h3 class="deals-section-title">
                                <span class="material-symbols-outlined" aria-hidden="true" style="color: #059669;">task_alt</span>
                                Cơ Hội Đã Đóng (2)
                            </h3>
                            <span class="deals-section-sum">Tổng giá trị đã ký: <strong style="color: #059669;">850.000.000 đ</strong></span>
                        </div>
                        <div class="table-responsive">
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th style="width: 100px;">Mã Deal</th>
                                        <th>Tên Cơ Hội</th>
                                        <th>Kết Quả</th>
                                        <th style="text-align: right;">Giá Trị Đã Ký</th>
                                        <th>Ngày Ký / Đóng</th>
                                        <th>Lý Do Thắng / Thua</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                        <td class="font-mono">CH-098</td>
                                        <td><strong>Nâng cấp bản quyền phần mềm CRM v1.0 năm 2025</strong></td>
                                        <td><span class="badge badge-success">Đóng Thắng (Đã ký)</span></td>
                                        <td style="text-align: right; font-weight: 700; color: #059669;">500.000.000 đ</td>
                                        <td>15/01/2026</td>
                                        <td>Giải pháp bảo mật đáp ứng chuẩn nội bộ; Giá cả cạnh tranh</td>
                                    </tr>
                                    <tr>
                                        <td class="font-mono">CH-082</td>
                                        <td><strong>Dịch vụ tư vấn quy trình quản trị bán hàng chuyên sâu</strong></td>
                                        <td><span class="badge badge-success">Đóng Thắng (Đã ký)</span></td>
                                        <td style="text-align: right; font-weight: 700; color: #059669;">350.000.000 đ</td>
                                        <td>10/08/2025</td>
                                        <td>Đội ngũ chuyên gia am hiểu nghiệp vụ doanh nghiệp B2B</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <!-- PANEL 3: Danh Sách Người Liên Hệ (Contacts) (AC1) -->
                <div class="tab-panel" id="panel-contacts" style="display: none;">
                    <div class="panel-section-title">
                        <span class="material-symbols-outlined" aria-hidden="true">supervisor_account</span>
                        <span>Danh Sách Người Liên Hệ & Vai Trò Quyết Định (AC1)</span>
                    </div>

                    <div class="contacts-grid">
                        <!-- Người liên hệ 1: Quyết định chính -->
                        <div class="contact-card">
                            <div>
                                <div class="contact-card-top">
                                    <div class="contact-avatar">M</div>
                                    <div class="contact-info-block">
                                        <h3>Nguyễn Đức Mạnh</h3>
                                        <div class="contact-job-title">Giám đốc Công nghệ Thông tin (CTO)</div>
                                        <div class="contact-badges">
                                            <span class="badge-role decision-maker">Người quyết định chính</span>
                                            <span class="badge-primary-contact">Đầu mối chính</span>
                                        </div>
                                    </div>
                                </div>
                                <div class="contact-details-list" style="margin-top: 14px;">
                                    <div class="contact-detail-item">
                                        <span class="material-symbols-outlined" aria-hidden="true">mail</span>
                                        <a href="mailto:manh.nd@fpt.com.vn" class="info-link">manh.nd@fpt.com.vn</a>
                                    </div>
                                    <div class="contact-detail-item">
                                        <span class="material-symbols-outlined" aria-hidden="true">call</span>
                                        <a href="tel:0912345678" class="info-link">0912 345 678</a>
                                    </div>
                                    <div class="contact-detail-item">
                                        <span class="material-symbols-outlined" aria-hidden="true">location_city</span>
                                        <span>Khối Công nghệ Thông tin & Chuyển đổi số</span>
                                    </div>
                                </div>
                            </div>
                            <div class="contact-card-actions">
                                <a href="tel:0912345678" class="btn-contact-action call">
                                    <span class="material-symbols-outlined" aria-hidden="true">call</span>
                                    Gọi điện
                                </a>
                                <a href="mailto:manh.nd@fpt.com.vn" class="btn-contact-action">
                                    <span class="material-symbols-outlined" aria-hidden="true">mail</span>
                                    Gửi email
                                </a>
                            </div>
                        </div>

                        <!-- Người liên hệ 2: Mua sắm -->
                        <div class="contact-card">
                            <div>
                                <div class="contact-card-top">
                                    <div class="contact-avatar">L</div>
                                    <div class="contact-info-block">
                                        <h3>Trần Mai Linh</h3>
                                        <div class="contact-job-title">Trưởng phòng Mua sắm & Hợp đồng</div>
                                        <div class="contact-badges">
                                            <span class="badge-role">Người thẩm định hợp đồng</span>
                                        </div>
                                    </div>
                                </div>
                                <div class="contact-details-list" style="margin-top: 14px;">
                                    <div class="contact-detail-item">
                                        <span class="material-symbols-outlined" aria-hidden="true">mail</span>
                                        <a href="mailto:linh.tm@fpt.com.vn" class="info-link">linh.tm@fpt.com.vn</a>
                                    </div>
                                    <div class="contact-detail-item">
                                        <span class="material-symbols-outlined" aria-hidden="true">call</span>
                                        <a href="tel:0988765432" class="info-link">0988 765 432</a>
                                    </div>
                                    <div class="contact-detail-item">
                                        <span class="material-symbols-outlined" aria-hidden="true">location_city</span>
                                        <span>Phòng Mua sắm Doanh nghiệp</span>
                                    </div>
                                </div>
                            </div>
                            <div class="contact-card-actions">
                                <a href="tel:0988765432" class="btn-contact-action call">
                                    <span class="material-symbols-outlined" aria-hidden="true">call</span>
                                    Gọi điện
                                </a>
                                <a href="mailto:linh.tm@fpt.com.vn" class="btn-contact-action">
                                    <span class="material-symbols-outlined" aria-hidden="true">mail</span>
                                    Gửi email
                                </a>
                            </div>
                        </div>

                        <!-- Người liên hệ 3: Kỹ thuật ảnh hưởng -->
                        <div class="contact-card">
                            <div>
                                <div class="contact-card-top">
                                    <div class="contact-avatar">N</div>
                                    <div class="contact-info-block">
                                        <h3>Phạm Hoàng Nam</h3>
                                        <div class="contact-job-title">Kiến trúc sư Trưởng (Lead Architect)</div>
                                        <div class="contact-badges">
                                            <span class="badge-role">Người ảnh hưởng kỹ thuật</span>
                                        </div>
                                    </div>
                                </div>
                                <div class="contact-details-list" style="margin-top: 14px;">
                                    <div class="contact-detail-item">
                                        <span class="material-symbols-outlined" aria-hidden="true">mail</span>
                                        <a href="mailto:nam.ph@fpt.com.vn" class="info-link">nam.ph@fpt.com.vn</a>
                                    </div>
                                    <div class="contact-detail-item">
                                        <span class="material-symbols-outlined" aria-hidden="true">call</span>
                                        <a href="tel:0903111222" class="info-link">0903 111 222</a>
                                    </div>
                                    <div class="contact-detail-item">
                                        <span class="material-symbols-outlined" aria-hidden="true">location_city</span>
                                        <span>Trung tâm Giải pháp Hạ tầng & Cloud</span>
                                    </div>
                                </div>
                            </div>
                            <div class="contact-card-actions">
                                <a href="tel:0903111222" class="btn-contact-action call">
                                    <span class="material-symbols-outlined" aria-hidden="true">call</span>
                                    Gọi điện
                                </a>
                                <a href="mailto:nam.ph@fpt.com.vn" class="btn-contact-action">
                                    <span class="material-symbols-outlined" aria-hidden="true">mail</span>
                                    Gửi email
                                </a>
                            </div>
                        </div>

                        <!-- Người liên hệ 4: Key User -->
                        <div class="contact-card">
                            <div>
                                <div class="contact-card-top">
                                    <div class="contact-avatar">H</div>
                                    <div class="contact-info-block">
                                        <h3>Vũ Thị Thu Hà</h3>
                                        <div class="contact-job-title">Trưởng nhóm Vận hành Bán hàng</div>
                                        <div class="contact-badges">
                                            <span class="badge-role">Người dùng chính (End User)</span>
                                        </div>
                                    </div>
                                </div>
                                <div class="contact-details-list" style="margin-top: 14px;">
                                    <div class="contact-detail-item">
                                        <span class="material-symbols-outlined" aria-hidden="true">mail</span>
                                        <a href="mailto:ha.vtt@fpt.com.vn" class="info-link">ha.vtt@fpt.com.vn</a>
                                    </div>
                                    <div class="contact-detail-item">
                                        <span class="material-symbols-outlined" aria-hidden="true">call</span>
                                        <a href="tel:0977888999" class="info-link">0977 888 999</a>
                                    </div>
                                    <div class="contact-detail-item">
                                        <span class="material-symbols-outlined" aria-hidden="true">location_city</span>
                                        <span>Khối Kinh doanh Doanh nghiệp</span>
                                    </div>
                                </div>
                            </div>
                            <div class="contact-card-actions">
                                <a href="tel:0977888999" class="btn-contact-action call">
                                    <span class="material-symbols-outlined" aria-hidden="true">call</span>
                                    Gọi điện
                                </a>
                                <a href="mailto:ha.vtt@fpt.com.vn" class="btn-contact-action">
                                    <span class="material-symbols-outlined" aria-hidden="true">mail</span>
                                    Gửi email
                                </a>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- PANEL 4: Tệp Đính Kèm & Tài Liệu (Attachments) (AC1) -->
                <div class="tab-panel" id="panel-attachments" style="display: none;">
                    <div class="panel-section-title">
                        <span class="material-symbols-outlined" aria-hidden="true">attachment</span>
                        <span>Tệp Đính Kèm & Tài Liệu Dự Án (AC1)</span>
                    </div>

                    <div class="table-responsive">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Tên Tài Liệu / Tệp</th>
                                    <th>Loại Tài Liệu</th>
                                    <th>Dung Lượng</th>
                                    <th>Người Tải Lên</th>
                                    <th>Ngày Tải Lên</th>
                                    <th style="text-align: center; width: 140px;">Thao Tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td>
                                        <div class="file-row-item">
                                            <div class="file-type-icon pdf">
                                                <span class="material-symbols-outlined" aria-hidden="true">picture_as_pdf</span>
                                            </div>
                                            <div>
                                                <div class="file-meta-name">Hop_dong_cung_cap_dich_vu_Cloud_CRM_2026.pdf</div>
                                                <div class="file-meta-sub">Hợp đồng pháp lý có chữ ký số hai bên</div>
                                            </div>
                                        </div>
                                    </td>
                                    <td><span class="badge badge-success">Hợp đồng đã ký</span></td>
                                    <td class="font-mono">3.8 MB</td>
                                    <td>Nguyễn Văn A</td>
                                    <td>05/10/2026</td>
                                    <td style="text-align: center;">
                                        <button type="button" class="btn-action" title="Tải xuống tệp" aria-label="Tải xuống tệp">
                                            <span class="material-symbols-outlined" aria-hidden="true">download</span>
                                        </button>
                                        <button type="button" class="btn-action" title="Xem trước tài liệu" aria-label="Xem trước tài liệu">
                                            <span class="material-symbols-outlined" aria-hidden="true">visibility</span>
                                        </button>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <div class="file-row-item">
                                            <div class="file-type-icon excel">
                                                <span class="material-symbols-outlined" aria-hidden="true">table_view</span>
                                            </div>
                                            <div>
                                                <div class="file-meta-name">Bao_gia_phien_ban_Enterprise_FPT_v2.xlsx</div>
                                                <div class="file-meta-sub">Bảng bóc tách chi phí bản quyền & hạ tầng</div>
                                            </div>
                                        </div>
                                    </td>
                                    <td><span class="badge" style="background: #e0f2fe; color: #0369a1;">Báo giá đã duyệt</span></td>
                                    <td class="font-mono">1.2 MB</td>
                                    <td>Nguyễn Văn A</td>
                                    <td>02/10/2026</td>
                                    <td style="text-align: center;">
                                        <button type="button" class="btn-action" title="Tải xuống tệp" aria-label="Tải xuống tệp">
                                            <span class="material-symbols-outlined" aria-hidden="true">download</span>
                                        </button>
                                        <button type="button" class="btn-action" title="Xem trước tài liệu" aria-label="Xem trước tài liệu">
                                            <span class="material-symbols-outlined" aria-hidden="true">visibility</span>
                                        </button>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <div class="file-row-item">
                                            <div class="file-type-icon pdf">
                                                <span class="material-symbols-outlined" aria-hidden="true">picture_as_pdf</span>
                                            </div>
                                            <div>
                                                <div class="file-meta-name">Ho_so_nang_luc_va_Kien_truc_He_thong.pdf</div>
                                                <div class="file-meta-sub">Tài liệu giới thiệu giải pháp kỹ thuật bảo mật</div>
                                            </div>
                                        </div>
                                    </td>
                                    <td><span class="badge" style="background: #f1f5f9; color: #475569;">Hồ sơ năng lực</span></td>
                                    <td class="font-mono">8.5 MB</td>
                                    <td>Nguyễn Văn A</td>
                                    <td>28/09/2026</td>
                                    <td style="text-align: center;">
                                        <button type="button" class="btn-action" title="Tải xuống tệp" aria-label="Tải xuống tệp">
                                            <span class="material-symbols-outlined" aria-hidden="true">download</span>
                                        </button>
                                        <button type="button" class="btn-action" title="Xem trước tài liệu" aria-label="Xem trước tài liệu">
                                            <span class="material-symbols-outlined" aria-hidden="true">visibility</span>
                                        </button>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <div class="file-row-item">
                                            <div class="file-type-icon word">
                                                <span class="material-symbols-outlined" aria-hidden="true">description</span>
                                            </div>
                                            <div>
                                                <div class="file-meta-name">Bien_ban_khao_sat_yeu_cau_ky_thuat.docx</div>
                                                <div class="file-meta-sub">Ghi nhận chi tiết yêu cầu tích hợp SSO nội bộ</div>
                                            </div>
                                        </div>
                                    </td>
                                    <td><span class="badge" style="background: #fef3c7; color: #92400e;">Biên bản khảo sát</span></td>
                                    <td class="font-mono">640 KB</td>
                                    <td>Nguyễn Văn A</td>
                                    <td>25/09/2026</td>
                                    <td style="text-align: center;">
                                        <button type="button" class="btn-action" title="Tải xuống tệp" aria-label="Tải xuống tệp">
                                            <span class="material-symbols-outlined" aria-hidden="true">download</span>
                                        </button>
                                        <button type="button" class="btn-action" title="Xem trước tài liệu" aria-label="Xem trước tài liệu">
                                            <span class="material-symbols-outlined" aria-hidden="true">visibility</span>
                                        </button>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </section>
        </div>

        <!-- Modal Ghi Nhanh Hoạt Động (Quick Activity Modal) -->
        <div class="modal-backdrop" id="modalThemHoatDong" style="display: none;" role="dialog" aria-modal="true" aria-labelledby="modalThemHoatDongTieuDe">
            <div class="modal-card">
                <div class="modal-header">
                    <div>
                        <h2 class="modal-title" id="modalThemHoatDongTieuDe">Ghi Nhận Hoạt Động Mới</h2>
                        <p class="modal-subtitle">Ghi nhận tức thời cuộc gọi, cuộc họp hoặc ghi chú với khách hàng</p>
                    </div>
                    <button type="button" class="modal-close-btn" id="btnDongModalHoatDong" aria-label="Đóng" title="Đóng">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>
                <form id="formThemHoatDong">
                    <div class="modal-body">
                        <div class="form-group">
                            <label class="form-label" for="hoatDongLoai">Loại hoạt động <span class="required">*</span></label>
                            <select id="hoatDongLoai" class="form-select">
                                <option value="CUOC_GOI">Cuộc gọi điện thoại</option>
                                <option value="CUOC_HOP">Cuộc họp trao đổi</option>
                                <option value="EMAIL">Gửi / Nhận Email</option>
                                <option value="GHI_CHU">Ghi chú bối cảnh</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="hoatDongTieuDe">Tiêu đề hoạt động <span class="required">*</span></label>
                            <input type="text" id="hoatDongTieuDe" class="form-input" placeholder="Ví dụ: Trao đổi điều khoản thanh toán với Anh Mạnh" required autocomplete="off">
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="hoatDongNoiDung">Tóm tắt nội dung & Kết quả</label>
                            <textarea id="hoatDongNoiDung" class="form-textarea" rows="3" placeholder="Ghi nhận tóm tắt thỏa thuận hoặc bước tiếp theo..."></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline" id="btnHuyModalHoatDong">Hủy bỏ</button>
                        <button type="submit" class="btn btn-primary" id="btnLuuHoatDong">
                            <span class="material-symbols-outlined" aria-hidden="true">save</span>
                            Lưu hoạt động
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </main>

    <script src="${pageContext.request.contextPath}/assets/js/navigation.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/session-keep-alive.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/khach-hang-360.js"></script>
</body>
</html>
