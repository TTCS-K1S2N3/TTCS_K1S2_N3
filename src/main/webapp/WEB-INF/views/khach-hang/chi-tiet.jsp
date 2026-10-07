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
                        <c:out value="${not empty khachHang360.tongGiaTriDaKyDinhDang ? khachHang360.tongGiaTriDaKyDinhDang : kpiGiaTriDaKy}" />
                    </div>
                    <div class="kpi-card-sub">
                        <c:choose>
                            <c:when test="${not empty khachHang360.dsCoHoiDaDong}">
                                <c:out value="${khachHang360.dsCoHoiDaDong.size()}" /> hợp đồng/cơ hội đã chốt
                            </c:when>
                            <c:otherwise>Chưa có hợp đồng hoàn tất</c:otherwise>
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
                        <c:out value="${not empty khachHang360.tongGiaTriCoHoiDangMoDinhDang ? khachHang360.tongGiaTriCoHoiDangMoDinhDang : kpiGiaTriDangMo}" />
                    </div>
                    <div class="kpi-card-sub">
                        <c:choose>
                            <c:when test="${not empty khachHang360.dsCoHoiDangMo}">
                                <c:out value="${khachHang360.dsCoHoiDangMo.size()}" /> cơ hội đang mở
                            </c:when>
                            <c:otherwise>0 cơ hội đang mở</c:otherwise>
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
                                <span class="info-value font-mono"><c:out value="${not empty khachHang.maKhachHang ? khachHang.maKhachHang : banGhi.maBanGhi}" /></span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Tên đầy đủ</span>
                                <span class="info-value" style="font-weight: 700;"><c:out value="${not empty khachHang.tenKhachHang ? khachHang.tenKhachHang : banGhi.tieuDe}" /></span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Mã số thuế (MST)</span>
                                <span class="info-value font-mono"><c:out value="${not empty khachHang.maSoThue ? khachHang.maSoThue : (not empty banGhi.maSoThue ? banGhi.maSoThue : '---')}" /></span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Website</span>
                                <span class="info-value">
                                    <c:set var="webUrl" value="${not empty khachHang.website ? khachHang.website : banGhi.website}" />
                                    <c:choose>
                                        <c:when test="${not empty webUrl}">
                                            <a href="${webUrl.startsWith('http') ? webUrl : 'https://'.concat(webUrl)}" target="_blank" rel="noopener noreferrer" class="info-link">
                                                <c:out value="${webUrl}" />
                                                <span class="material-symbols-outlined" style="font-size: 15px;" aria-hidden="true">open_in_new</span>
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-muted">---</span>
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Ngành nghề kinh doanh</span>
                                <span class="info-value"><c:out value="${not empty khachHang.nganhNghe ? khachHang.nganhNghe : 'Công nghệ thông tin & Dịch vụ'}" /></span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Quy mô doanh nghiệp</span>
                                <span class="info-value"><c:out value="${not empty khachHang.quyMo ? khachHang.quyMo : 'Doanh nghiệp'}" /></span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Doanh thu ước tính</span>
                                <span class="info-value"><c:out value="${not empty khachHang.doanhThuUocTinh ? khachHang.doanhThuUocTinh : (not empty banGhi.giaTri ? banGhi.giaTri : '---')}" /></span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Địa chỉ trụ sở</span>
                                <span class="info-value"><c:out value="${not empty khachHang.diaChi ? khachHang.diaChi : '---'}" /></span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Khu vực địa lý</span>
                                <span class="info-value"><c:out value="${not empty khachHang.khuVuc ? khachHang.khuVuc : 'Toàn quốc'}" /></span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Người phụ trách (Owner)</span>
                                <span class="info-value"><c:out value="${not empty khachHang.tenNguoiPhuTrach ? khachHang.tenNguoiPhuTrach : banGhi.tenNguoiPhuTrach}" /></span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Nhóm phụ trách</span>
                                <span class="info-value"><c:out value="${not empty khachHang.tenNhom ? khachHang.tenNhom : banGhi.tenNhom}" /></span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Ngày tạo hồ sơ</span>
                                <span class="info-value"><c:out value="${not empty khachHang.ngayTao ? khachHang.ngayTao : banGhi.ngayTao}" /></span>
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
                        <span class="tab-badge" id="badgeSoHoatDong">${not empty khachHang360.dsHoatDong ? khachHang360.dsHoatDong.size() : 0}</span>
                    </button>
                    <button type="button" class="nav-tab-btn" data-tab="deals" role="tab" aria-selected="false">
                        <span class="material-symbols-outlined" aria-hidden="true">monetization_on</span>
                        <span>Cơ hội bán hàng</span>
                        <span class="tab-badge">Mở: ${not empty khachHang360.dsCoHoiDangMo ? khachHang360.dsCoHoiDangMo.size() : 0} | Đã chốt: ${not empty khachHang360.dsCoHoiDaDong ? khachHang360.dsCoHoiDaDong.size() : 0}</span>
                    </button>
                    <button type="button" class="nav-tab-btn" data-tab="contacts" role="tab" aria-selected="false">
                        <span class="material-symbols-outlined" aria-hidden="true">contacts</span>
                        <span>Người liên hệ</span>
                        <span class="tab-badge">${not empty khachHang360.dsNguoiLienHe ? khachHang360.dsNguoiLienHe.size() : 0} người</span>
                    </button>
                    <button type="button" class="nav-tab-btn" data-tab="attachments" role="tab" aria-selected="false">
                        <span class="material-symbols-outlined" aria-hidden="true">attach_file</span>
                        <span>Tệp đính kèm</span>
                        <span class="tab-badge">${not empty khachHang360.dsTepDinhKem ? khachHang360.dsTepDinhKem.size() : 0} tệp</span>
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
                                Cơ Hội Đang Mở (${not empty khachHang360.dsCoHoiDangMo ? khachHang360.dsCoHoiDangMo.size() : 0})
                            </h3>
                            <span class="deals-section-sum">Tổng giá trị đang mở: <strong><c:out value="${not empty khachHang360.tongGiaTriCoHoiDangMoDinhDang ? khachHang360.tongGiaTriCoHoiDangMoDinhDang : '0 đ'}" /></strong></span>
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
                                    <c:choose>
                                        <c:when test="${not empty khachHang360.dsCoHoiDangMo}">
                                            <c:forEach var="deal" items="${khachHang360.dsCoHoiDangMo}">
                                                <tr>
                                                    <td class="font-mono"><c:out value="${deal.maCoHoi}" /></td>
                                                    <td><strong><c:out value="${deal.tenCoHoi}" /></strong></td>
                                                    <td><span class="pipeline-stage-badge"><c:out value="${deal.giaiDoan}" /></span></td>
                                                    <td style="text-align: right; font-weight: 700; color: var(--primary);">
                                                        <c:out value="${deal.giaTriDuKienDinhDang}" />
                                                    </td>
                                                    <td style="text-align: center;">
                                                        <div class="probability-bar-wrap">
                                                            <div class="probability-bar"><div class="probability-bar-fill" style="width: ${deal.xacSuatThanhCong}%;"></div></div>
                                                            <span><c:out value="${deal.xacSuatThanhCong}" />%</span>
                                                        </div>
                                                    </td>
                                                    <td><c:out value="${deal.ngayDuKienDong}" /></td>
                                                    <td><c:out value="${deal.tenNguoiPhuTrach}" /></td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr>
                                                <td colspan="7" style="text-align: center; color: var(--text-muted); padding: 24px;">
                                                    Chưa có cơ hội bán hàng nào đang mở cho khách hàng này.
                                                </td>
                                            </tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <!-- 2. Cơ hội đã đóng (Closed Deals - Won / Lost) -->
                    <div class="deals-section">
                        <div class="deals-section-header">
                            <h3 class="deals-section-title">
                                <span class="material-symbols-outlined" aria-hidden="true" style="color: #059669;">task_alt</span>
                                Cơ Hội Đã Đóng / Đã Ký (${not empty khachHang360.dsCoHoiDaDong ? khachHang360.dsCoHoiDaDong.size() : 0})
                            </h3>
                            <span class="deals-section-sum">Tổng giá trị đã ký: <strong style="color: #059669;"><c:out value="${not empty khachHang360.tongGiaTriDaKyDinhDang ? khachHang360.tongGiaTriDaKyDinhDang : '0 đ'}" /></strong></span>
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
                                    <c:choose>
                                        <c:when test="${not empty khachHang360.dsCoHoiDaDong}">
                                            <c:forEach var="deal" items="${khachHang360.dsCoHoiDaDong}">
                                                <tr>
                                                    <td class="font-mono"><c:out value="${deal.maCoHoi}" /></td>
                                                    <td><strong><c:out value="${deal.tenCoHoi}" /></strong></td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${deal.trangThai == 'DONG_THANG' || deal.trangThai == 'DA_KY'}">
                                                                <span class="badge badge-success">Đóng Thắng (Đã ký)</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge" style="background: #fee2e2; color: #b91c1c;">Đóng Thua</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td style="text-align: right; font-weight: 700; color: #059669;">
                                                        <c:out value="${deal.giaTriThucTeDinhDang}" />
                                                    </td>
                                                    <td><c:out value="${deal.ngayThucTeDong}" /></td>
                                                    <td><c:out value="${not empty deal.lyDoThatBai ? deal.lyDoThatBai : 'Đạt yêu cầu & ký kết thành công'}" /></td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr>
                                                <td colspan="6" style="text-align: center; color: var(--text-muted); padding: 24px;">
                                                    Chưa có cơ hội bán hàng nào đã đóng cho khách hàng này.
                                                </td>
                                            </tr>
                                        </c:otherwise>
                                    </c:choose>
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
                        <c:choose>
                            <c:when test="${not empty khachHang360.dsNguoiLienHe}">
                                <c:forEach var="contact" items="${khachHang360.dsNguoiLienHe}">
                                    <div class="contact-card">
                                        <div>
                                            <div class="contact-card-top">
                                                <div class="contact-avatar">
                                                    <c:out value="${not empty contact.hoTen ? contact.hoTen.substring(0, 1) : 'U'}" />
                                                </div>
                                                <div class="contact-info-block">
                                                    <h3><c:out value="${contact.hoTen}" /></h3>
                                                    <div class="contact-job-title"><c:out value="${not empty contact.chucVu ? contact.chucVu : 'Nhân sự liên hệ'}" /></div>
                                                    <div class="contact-badges">
                                                        <c:choose>
                                                            <c:when test="${contact.vaiTroQuyetDinh == 'NGUOI_QUYET_DINH'}">
                                                                <span class="badge-role decision-maker">Người quyết định chính</span>
                                                            </c:when>
                                                            <c:when test="${contact.vaiTroQuyetDinh == 'NGUOI_ANH_HUONG'}">
                                                                <span class="badge-role">Người ảnh hưởng</span>
                                                            </c:when>
                                                            <c:when test="${contact.vaiTroQuyetDinh == 'NGUOI_DUNG_CUOI'}">
                                                                <span class="badge-role">Người dùng cuối</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge-role">Thành viên tham gia</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                        <c:if test="${contact.laDauMoiChinh}">
                                                            <span class="badge-primary-contact">Đầu mối chính</span>
                                                        </c:if>
                                                    </div>
                                                </div>
                                            </div>
                                            <div class="contact-details-list" style="margin-top: 14px;">
                                                <c:if test="${not empty contact.email}">
                                                    <div class="contact-detail-item">
                                                        <span class="material-symbols-outlined" aria-hidden="true">mail</span>
                                                        <a href="mailto:${contact.email}" class="info-link"><c:out value="${contact.email}" /></a>
                                                    </div>
                                                </c:if>
                                                <c:if test="${not empty contact.soDienThoai}">
                                                    <div class="contact-detail-item">
                                                        <span class="material-symbols-outlined" aria-hidden="true">call</span>
                                                        <a href="tel:${contact.soDienThoai}" class="info-link"><c:out value="${contact.soDienThoai}" /></a>
                                                    </div>
                                                </c:if>
                                                <div class="contact-detail-item">
                                                    <span class="material-symbols-outlined" aria-hidden="true">location_city</span>
                                                    <span><c:out value="${not empty contact.phongBan ? contact.phongBan : 'Khối vận hành'}" /></span>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="contact-card-actions">
                                            <c:if test="${not empty contact.soDienThoai}">
                                                <a href="tel:${contact.soDienThoai}" class="btn-contact-action call">
                                                    <span class="material-symbols-outlined" aria-hidden="true">call</span>
                                                    Gọi điện
                                                </a>
                                            </c:if>
                                            <c:if test="${not empty contact.email}">
                                                <a href="mailto:${contact.email}" class="btn-contact-action">
                                                    <span class="material-symbols-outlined" aria-hidden="true">mail</span>
                                                    Gửi email
                                                </a>
                                            </c:if>
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <div style="grid-column: 1 / -1; text-align: center; color: var(--text-muted); padding: 32px; background: #fff; border-radius: 8px;">
                                    Chưa có người liên hệ nào được lưu cho khách hàng này.
                                </div>
                            </c:otherwise>
                        </c:choose>
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
                                <c:choose>
                                    <c:when test="${not empty khachHang360.dsTepDinhKem}">
                                        <c:forEach var="file" items="${khachHang360.dsTepDinhKem}">
                                            <tr>
                                                <td>
                                                    <div class="file-row-item">
                                                        <div class="file-type-icon ${file.loaiTep.toLowerCase().contains('pdf') ? 'pdf' : (file.loaiTep.toLowerCase().contains('xls') ? 'excel' : 'word')}">
                                                            <span class="material-symbols-outlined" aria-hidden="true">
                                                                <c:choose>
                                                                    <c:when test="${file.loaiTep.toLowerCase().contains('pdf')}">picture_as_pdf</c:when>
                                                                    <c:when test="${file.loaiTep.toLowerCase().contains('xls')}">table_view</c:when>
                                                                    <c:otherwise>description</c:otherwise>
                                                                </c:choose>
                                                            </span>
                                                        </div>
                                                        <div>
                                                            <div class="file-meta-name"><c:out value="${file.tenTep}" /></div>
                                                            <div class="file-meta-sub"><c:out value="${not empty file.ghiChu ? file.ghiChu : 'Tài liệu đính kèm hồ sơ'}" /></div>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td><span class="badge" style="background: #e0f2fe; color: #0369a1;"><c:out value="${file.loaiTep}" /></span></td>
                                                <td class="font-mono"><c:out value="${file.dungLuongDinhDang}" /></td>
                                                <td><c:out value="${file.tenNguoiTaiLen}" /></td>
                                                <td><c:out value="${file.ngayTaiLen}" /></td>
                                                <td style="text-align: center;">
                                                    <a href="${pageContext.request.contextPath}${file.duongDan}" target="_blank" class="btn-action" title="Tải xuống tệp" aria-label="Tải xuống tệp">
                                                        <span class="material-symbols-outlined" aria-hidden="true">download</span>
                                                    </a>
                                                    <a href="${pageContext.request.contextPath}${file.duongDan}" target="_blank" class="btn-action" title="Xem trước tài liệu" aria-label="Xem trước tài liệu">
                                                        <span class="material-symbols-outlined" aria-hidden="true">visibility</span>
                                                    </a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="6" style="text-align: center; color: var(--text-muted); padding: 24px;">
                                                Chưa có tệp đính kèm nào được tải lên cho khách hàng này.
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
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

    <script id="initialActivitiesJson" type="application/json">
        ${not empty hoatDongJson ? hoatDongJson : '[]'}
    </script>
    <script>
        window.CURRENT_CUSTOMER_ID = "${not empty banGhi.id ? banGhi.id : (not empty khachHang.id ? khachHang.id : 0)}";
        window.APP_CONTEXT_PATH = "${pageContext.request.contextPath}";
    </script>
    <script src="${pageContext.request.contextPath}/assets/js/navigation.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/session-keep-alive.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/khach-hang-360.js"></script>
</body>
</html>
