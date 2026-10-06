<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Khai báo danh mục lý do thắng thua và đối thủ cạnh tranh - Phục vụ đóng cơ hội bán hàng Sprint 5">
    <title>Danh Mục Lý Do Thắng Thua & Đối Thủ - CRM Bán Hàng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/co-hoi/ly-do-thang-thua.css">
    <script>
        window.CRM_CONTEXT_PATH = "${pageContext.request.contextPath}";
    </script>
</head>
<body class="crm-body">
    <!-- Thanh điều hướng và Sidebar chuẩn hệ thống S1-06 -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <div class="crm-catalog-page">

            <!-- Breadcrumb điều hướng -->
            <nav class="crm-catalog-breadcrumb" aria-label="Đường dẫn điều hướng" style="display:flex; justify-content:space-between; align-items:center;">
                <div>
                    <a href="${pageContext.request.contextPath}/dieu-huong">Trang chủ</a>
                    <span class="separator">/</span>
                    <a href="${pageContext.request.contextPath}/danh-muc">Danh mục & Cấu hình</a>
                    <span class="separator">/</span>
                    <span class="current">Lý do Thắng Thua & Đối thủ</span>
                </div>
                <a href="${pageContext.request.contextPath}/danh-muc" class="crm-btn crm-btn-outline" style="padding: 4px 12px; font-size: 13px; text-decoration: none;">
                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">arrow_back</span> Quay lại Danh mục
                </a>
            </nav>

            <!-- Page Header Banner -->
            <header class="crm-catalog-header">
                <div class="crm-header-info">
                    <div class="crm-header-badges">
                        <span class="crm-badge-module">Epic EP-02 / Story S2-10</span>
                        <span class="crm-badge-role-target">
                            <span class="material-symbols-outlined icon-xs" aria-hidden="true">shield</span>
                            <c:out value="${not empty thongTinDieuHuong.vaiTroHienThi ? thongTinDieuHuong.vaiTroHienThi : (not empty userHienTai.chuoiVaiTroHienThi ? userHienTai.chuoiVaiTroHienThi : 'Giám đốc kinh doanh')}" />
                        </span>
                        <span class="crm-badge-sprint-rule">
                            <span class="material-symbols-outlined icon-xs" aria-hidden="true">info</span>
                            Dữ liệu bắt buộc đóng cơ hội (Sprint 5)
                        </span>
                    </div>
                    <h1 class="crm-catalog-title">Danh Mục Lý Do Thắng Thua & Đối Thủ Cạnh Tranh</h1>
                    <p class="crm-catalog-subtitle">
                        Chuẩn hóa danh mục lý do chốt thắng/thất bại và theo dõi đối thủ thị trường để toàn bộ khối kinh doanh
                        học hỏi từ các thương vụ, không thua lại chỗ đã thua, và bắt buộc áp dụng khi đóng cơ hội bán hàng.
                    </p>
                </div>
                <div class="crm-header-actions">
                    <c:if test="${coQuyenQuanLy}">
                        <form method="POST" action="${pageContext.request.contextPath}/danh-muc/ly-do-thang-thua" style="display:inline;">
                            <input type="hidden" name="action" value="nap-du-lieu-mau">
                            <input type="hidden" name="tab" value="${currentTab}">
                            <button type="submit" class="crm-btn crm-btn-outline" id="btn-seed-sample" title="Tự động nạp danh mục lý do và đối thủ mẫu chuẩn">
                                <span class="material-symbols-outlined" aria-hidden="true">sync</span>
                                Nạp dữ liệu mẫu
                            </button>
                        </form>
                        <button type="button" class="crm-btn crm-btn-primary" id="btn-add-new-record">
                            <span class="material-symbols-outlined" aria-hidden="true">add</span>
                            Thêm mới
                        </button>
                    </c:if>
                    <c:if test="${!coQuyenQuanLy}">
                        <span class="crm-badge crm-badge-info" title="Chế độ xem cho nhân viên kinh doanh">
                            <span class="material-symbols-outlined icon-xs" aria-hidden="true">visibility</span>
                            Chế độ xem tra cứu
                        </span>
                    </c:if>
                </div>
            </header>

            <!-- Thông báo trạng thái Flash Message -->
            <c:if test="${param.msg == 'success'}">
                <div class="crm-alert-toast crm-alert-success" role="alert">
                    <div class="crm-alert-content">
                        <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                        <span><c:out value="${param.info != null ? param.info : 'Thao tác đã được thực hiện thành công.'}" /></span>
                    </div>
                    <button type="button" class="crm-alert-close" onclick="this.parentElement.remove();" aria-label="Đóng" title="Đóng">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>
            </c:if>
            <c:if test="${param.msg == 'error'}">
                <div class="crm-alert-toast crm-alert-error" role="alert">
                    <div class="crm-alert-content">
                        <span class="material-symbols-outlined" aria-hidden="true">error</span>
                        <span><c:out value="${param.info != null ? param.info : 'Đã xảy ra lỗi trong quá trình thực hiện.'}" /></span>
                    </div>
                    <button type="button" class="crm-alert-close" onclick="this.parentElement.remove();" aria-label="Đóng" title="Đóng">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>
            </c:if>

            <!-- Khối thẻ KPIs Thống kê tổng quan -->
            <div class="crm-kpi-grid">
                <!-- Card 1: Lý do thắng -->
                <div class="crm-kpi-card" id="kpi-win-reasons">
                    <div class="crm-kpi-icon-wrap crm-kpi-icon-win">
                        <span class="material-symbols-outlined icon-lg" aria-hidden="true">emoji_events</span>
                    </div>
                    <div class="crm-kpi-content">
                        <div class="crm-kpi-label">Lý do Thắng (Won)</div>
                        <div class="crm-kpi-value-row">
                            <span class="crm-kpi-num">${tongLyDoThang}</span>
                            <span class="crm-kpi-sub">(${soThangHoatDong} đang dùng)</span>
                        </div>
                    </div>
                </div>

                <!-- Card 2: Lý do thua -->
                <div class="crm-kpi-card" id="kpi-loss-reasons">
                    <div class="crm-kpi-icon-wrap crm-kpi-icon-loss">
                        <span class="material-symbols-outlined icon-lg" aria-hidden="true">trending_down</span>
                    </div>
                    <div class="crm-kpi-content">
                        <div class="crm-kpi-label">Lý do Thua (Lost)</div>
                        <div class="crm-kpi-value-row">
                            <span class="crm-kpi-num">${tongLyDoThua}</span>
                            <span class="crm-kpi-sub">(${soThuaHoatDong} đang dùng)</span>
                        </div>
                    </div>
                </div>

                <!-- Card 3: Đối thủ cạnh tranh -->
                <div class="crm-kpi-card" id="kpi-competitors">
                    <div class="crm-kpi-icon-wrap crm-kpi-icon-competitor">
                        <span class="material-symbols-outlined icon-lg" aria-hidden="true">groups</span>
                    </div>
                    <div class="crm-kpi-content">
                        <div class="crm-kpi-label">Đối thủ Cạnh tranh</div>
                        <div class="crm-kpi-value-row">
                            <span class="crm-kpi-num">${tongDoiThu}</span>
                            <span class="crm-kpi-sub">(${soDoiThuHoatDong} đang theo dõi)</span>
                        </div>
                    </div>
                </div>

                <!-- Card 4: Cơ hội tham chiếu -->
                <div class="crm-kpi-card" id="kpi-references">
                    <div class="crm-kpi-icon-wrap crm-kpi-icon-ref">
                        <span class="material-symbols-outlined icon-lg" aria-hidden="true">receipt_long</span>
                    </div>
                    <div class="crm-kpi-content">
                        <div class="crm-kpi-label">Thương vụ Tham chiếu</div>
                        <div class="crm-kpi-value-row">
                            <span class="crm-kpi-num">${tongThamChieu}</span>
                            <span class="crm-kpi-sub">(Sprint 5 liên kết)</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Thanh điều hướng Tab chuẩn AC1 & AC2 -->
            <div class="crm-nav-tabs-wrapper">
                <ul class="crm-nav-tabs" role="tablist">
                    <!-- Tab 1: Lý do thắng (AC1) -->
                    <li class="crm-nav-tab-item">
                        <button type="button" class="crm-nav-tab-btn ${currentTab == 'thang' ? 'active' : ''}" data-tab="thang" role="tab" id="tab-btn-thang">
                            <span class="material-symbols-outlined icon-sm" aria-hidden="true">emoji_events</span>
                            Lý do Thắng
                            <span class="crm-nav-tab-badge">${tongLyDoThang}</span>
                        </button>
                    </li>

                    <!-- Tab 2: Lý do thua (AC1) -->
                    <li class="crm-nav-tab-item">
                        <button type="button" class="crm-nav-tab-btn ${currentTab == 'thua' ? 'active' : ''}" data-tab="thua" role="tab" id="tab-btn-thua">
                            <span class="material-symbols-outlined icon-sm" aria-hidden="true">trending_down</span>
                            Lý do Thua
                            <span class="crm-nav-tab-badge">${tongLyDoThua}</span>
                        </button>
                    </li>

                    <!-- Tab 3: Đối thủ cạnh tranh (AC2) -->
                    <li class="crm-nav-tab-item">
                        <button type="button" class="crm-nav-tab-btn ${currentTab == 'doi-thu' ? 'active' : ''}" data-tab="doi-thu" role="tab" id="tab-btn-doi-thu">
                            <span class="material-symbols-outlined icon-sm" aria-hidden="true">groups</span>
                            Đối thủ Cạnh tranh
                            <span class="crm-nav-tab-badge">${tongDoiThu}</span>
                        </button>
                    </li>

                    <!-- Tab 4: Kiểm tra quy tắc đóng cơ hội Sprint 5 (AC3) -->
                    <li class="crm-nav-tab-item">
                        <button type="button" class="crm-nav-tab-btn ${currentTab == 'sprint5' ? 'active' : ''}" data-tab="sprint5" role="tab" id="tab-btn-sprint5">
                            <span class="material-symbols-outlined icon-sm" aria-hidden="true">verified</span>
                            Quy tắc Đóng Cơ hội (Sprint 5)
                            <span class="crm-nav-tab-badge" style="background:#dbeafe; color:#1e40af;">S5-05</span>
                        </button>
                    </li>
                </ul>
            </div>

            <!-- Toolbar Tìm kiếm và Lọc -->
            <div class="crm-catalog-toolbar">
                <div class="crm-toolbar-left">
                    <div class="crm-search-box">
                        <span class="material-symbols-outlined crm-search-icon" aria-hidden="true">search</span>
                        <input type="text" class="crm-search-input" id="catalog-search-input" placeholder="Tìm theo mã hoặc tên..." value="<c:out value="${tuKhoa}" />">
                    </div>
                    <select class="crm-filter-select" id="catalog-status-filter">
                        <option value="">Tất cả trạng thái</option>
                        <option value="active" ${trangThaiLoc == 'active' ? 'selected' : ''}>Đang hoạt động / theo dõi</option>
                        <option value="inactive" ${trangThaiLoc == 'inactive' ? 'selected' : ''}>Ngừng hoạt động / theo dõi</option>
                    </select>
                </div>
            </div>

            <!-- ===============================================================
                 NỘI DUNG CÁC TAB
                 =============================================================== -->

            <!-- PANE 1: DANH SÁCH LÝ DO THẮNG (AC1) -->
            <div class="crm-tab-pane" id="tab-pane-thang" style="display: ${currentTab == 'thang' ? 'block' : 'none'};">
                <div class="crm-table-card">
                    <div class="crm-table-responsive">
                        <table class="crm-data-table" id="table-ly-do-thang">
                            <thead>
                                <tr>
                                    <th style="width: 140px;">Mã Lý Do</th>
                                    <th>Tên Lý Do Thắng</th>
                                    <th style="width: 100px; text-align: center;">Thứ Tự</th>
                                    <th style="width: 180px;">Trạng Thái</th>
                                    <th style="width: 150px; text-align: center;">Cơ Hội Áp Dụng</th>
                                    <c:if test="${coQuyenQuanLy}">
                                        <th style="width: 120px; text-align: right;">Thao Tác</th>
                                    </c:if>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="item" items="${dsLyDoThang}">
                                    <tr data-search="${item.maLyDo} ${item.tenLyDo}" data-status="${item.hoatDong ? 'active' : 'inactive'}">
                                        <td>
                                            <span class="crm-code-chip">${item.maLyDo}</span>
                                        </td>
                                        <td class="crm-title-cell">
                                            <c:out value="${item.tenLyDo}" />
                                        </td>
                                        <td style="text-align: center;">
                                            <span class="crm-sort-order-pill">${item.thuTuHienThi}</span>
                                        </td>
                                        <td>
                                            <c:if test="${coQuyenQuanLy}">
                                                <label class="crm-switch">
                                                    <input type="checkbox" class="crm-ajax-status-toggle" data-type="ly-do" data-id="${item.id}" ${item.hoatDong ? 'checked' : ''}>
                                                    <span class="crm-slider"></span>
                                                </label>
                                            </c:if>
                                            <span class="crm-status-label ${item.hoatDong ? 'crm-status-active' : 'crm-status-inactive'}">
                                                ${item.hoatDong ? 'Đang hoạt động' : 'Ngừng hoạt động'}
                                            </span>
                                        </td>
                                        <td style="text-align: center;">
                                            <span class="crm-badge-ref ${item.soCoHoiThamChieu > 0 ? 'has-ref' : ''}">
                                                ${item.soCoHoiThamChieu} cơ hội
                                            </span>
                                        </td>
                                        <c:if test="${coQuyenQuanLy}">
                                            <td style="text-align: right;">
                                                <div class="crm-action-buttons" style="justify-content: flex-end;">
                                                    <button type="button" class="crm-btn-icon btn-edit-ly-do" title="Chỉnh sửa lý do thắng" aria-label="Chỉnh sửa lý do thắng"
                                                            data-id="${item.id}" data-ma="${item.maLyDo}" data-ten="${item.tenLyDo}"
                                                            data-loai="${item.loai}" data-thutu="${item.thuTuHienThi}" data-hoatdong="${item.hoatDong}">
                                                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">edit</span>
                                                    </button>
                                                    <button type="button" class="crm-btn-icon crm-btn-delete btn-delete-record" title="Xóa lý do thắng" aria-label="Xóa lý do thắng"
                                                            data-action="xoa-ly-do" data-id="${item.id}" data-name="${item.tenLyDo}" data-ref="${item.soCoHoiThamChieu}">
                                                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">delete</span>
                                                    </button>
                                                </div>
                                            </td>
                                        </c:if>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty dsLyDoThang}">
                                    <tr>
                                        <td colspan="${coQuyenQuanLy ? 6 : 5}">
                                            <div class="crm-empty-state">
                                                <span class="material-symbols-outlined crm-empty-icon" style="font-size: 48px;" aria-hidden="true">emoji_events</span>
                                                <div class="crm-empty-title">Chưa có lý do thắng nào được khai báo</div>
                                                <p class="crm-empty-desc">Nhấn nút "Thêm lý do thắng" để tạo mới các lý do chốt thành công cơ hội.</p>
                                            </div>
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <!-- PANE 2: DANH SÁCH LÝ DO THUA (AC1) -->
            <div class="crm-tab-pane" id="tab-pane-thua" style="display: ${currentTab == 'thua' ? 'block' : 'none'};">
                <div class="crm-table-card">
                    <div class="crm-table-responsive">
                        <table class="crm-data-table" id="table-ly-do-thua">
                            <thead>
                                <tr>
                                    <th style="width: 140px;">Mã Lý Do</th>
                                    <th>Tên Lý Do Thua</th>
                                    <th style="width: 100px; text-align: center;">Thứ Tự</th>
                                    <th style="width: 180px;">Trạng Thái</th>
                                    <th style="width: 150px; text-align: center;">Cơ Hội Áp Dụng</th>
                                    <c:if test="${coQuyenQuanLy}">
                                        <th style="width: 120px; text-align: right;">Thao Tác</th>
                                    </c:if>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="item" items="${dsLyDoThua}">
                                    <tr data-search="${item.maLyDo} ${item.tenLyDo}" data-status="${item.hoatDong ? 'active' : 'inactive'}">
                                        <td>
                                            <span class="crm-code-chip">${item.maLyDo}</span>
                                        </td>
                                        <td class="crm-title-cell">
                                            <c:out value="${item.tenLyDo}" />
                                        </td>
                                        <td style="text-align: center;">
                                            <span class="crm-sort-order-pill">${item.thuTuHienThi}</span>
                                        </td>
                                        <td>
                                            <c:if test="${coQuyenQuanLy}">
                                                <label class="crm-switch">
                                                    <input type="checkbox" class="crm-ajax-status-toggle" data-type="ly-do" data-id="${item.id}" ${item.hoatDong ? 'checked' : ''}>
                                                    <span class="crm-slider"></span>
                                                </label>
                                            </c:if>
                                            <span class="crm-status-label ${item.hoatDong ? 'crm-status-active' : 'crm-status-inactive'}">
                                                ${item.hoatDong ? 'Đang hoạt động' : 'Ngừng hoạt động'}
                                            </span>
                                        </td>
                                        <td style="text-align: center;">
                                            <span class="crm-badge-ref ${item.soCoHoiThamChieu > 0 ? 'has-ref' : ''}">
                                                ${item.soCoHoiThamChieu} cơ hội
                                            </span>
                                        </td>
                                        <c:if test="${coQuyenQuanLy}">
                                            <td style="text-align: right;">
                                                <div class="crm-action-buttons" style="justify-content: flex-end;">
                                                    <button type="button" class="crm-btn-icon btn-edit-ly-do" title="Chỉnh sửa lý do thua" aria-label="Chỉnh sửa lý do thua"
                                                            data-id="${item.id}" data-ma="${item.maLyDo}" data-ten="${item.tenLyDo}"
                                                            data-loai="${item.loai}" data-thutu="${item.thuTuHienThi}" data-hoatdong="${item.hoatDong}">
                                                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">edit</span>
                                                    </button>
                                                    <button type="button" class="crm-btn-icon crm-btn-delete btn-delete-record" title="Xóa lý do thua" aria-label="Xóa lý do thua"
                                                            data-action="xoa-ly-do" data-id="${item.id}" data-name="${item.tenLyDo}" data-ref="${item.soCoHoiThamChieu}">
                                                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">delete</span>
                                                    </button>
                                                </div>
                                            </td>
                                        </c:if>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty dsLyDoThua}">
                                    <tr>
                                        <td colspan="${coQuyenQuanLy ? 6 : 5}">
                                            <div class="crm-empty-state">
                                                <span class="material-symbols-outlined crm-empty-icon" style="font-size: 48px;" aria-hidden="true">trending_down</span>
                                                <div class="crm-empty-title">Chưa có lý do thua nào được khai báo</div>
                                                <p class="crm-empty-desc">Nhấn nút "Thêm lý do thua" để tạo các nguyên nhân thương vụ thất bại phục vụ rút kinh nghiệm.</p>
                                            </div>
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <!-- PANE 3: DANH SÁCH ĐỐI THỦ CẠNH TRANH (AC2) -->
            <div class="crm-tab-pane" id="tab-pane-doi-thu" style="display: ${currentTab == 'doi-thu' ? 'block' : 'none'};">
                <div class="crm-table-card">
                    <div class="crm-table-responsive">
                        <table class="crm-data-table" id="table-doi-thu">
                            <thead>
                                <tr>
                                    <th style="width: 140px;">Mã Đối Thủ</th>
                                    <th style="width: 240px;">Tên Đối Thủ Cạnh Tranh</th>
                                    <th style="width: 200px;">Website</th>
                                    <th>Ghi Chú Phân Tích (Điểm mạnh / Yếu)</th>
                                    <th style="width: 170px;">Trạng Thái</th>
                                    <th style="width: 140px; text-align: center;">Cơ Hội Thua</th>
                                    <c:if test="${coQuyenQuanLy}">
                                        <th style="width: 120px; text-align: right;">Thao Tác</th>
                                    </c:if>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="item" items="${dsDoiThu}">
                                    <tr data-search="${item.maDoiThu} ${item.tenDoiThu} ${item.website} ${item.ghiChu}" data-status="${item.hoatDong ? 'active' : 'inactive'}">
                                        <td>
                                            <span class="crm-code-chip">${item.maDoiThu}</span>
                                        </td>
                                        <td class="crm-title-cell">
                                            <c:out value="${item.tenDoiThu}" />
                                        </td>
                                        <td>
                                            <c:if test="${not empty item.website}">
                                                <a href="${item.website.startsWith('http') ? item.website : 'https://'.concat(item.website)}"
                                                   target="_blank" rel="noopener noreferrer" class="crm-website-link">
                                                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">language</span>
                                                    <c:out value="${item.website}" />
                                                </a>
                                            </c:if>
                                            <c:if test="${empty item.website}">
                                                <span style="color: var(--slate-400); font-style: italic;">Chưa có website</span>
                                            </c:if>
                                        </td>
                                        <td>
                                            <span style="color: var(--slate-600); font-size: 13px;">
                                                <c:out value="${item.ghiChu != null ? item.ghiChu : 'Chưa có ghi chú'}" />
                                            </span>
                                        </td>
                                        <td>
                                            <c:if test="${coQuyenQuanLy}">
                                                <label class="crm-switch">
                                                    <input type="checkbox" class="crm-ajax-status-toggle" data-type="doi-thu" data-id="${item.id}" ${item.hoatDong ? 'checked' : ''}>
                                                    <span class="crm-slider"></span>
                                                </label>
                                            </c:if>
                                            <span class="crm-status-label ${item.hoatDong ? 'crm-status-active' : 'crm-status-inactive'}">
                                                ${item.hoatDong ? 'Đang theo dõi' : 'Ngừng theo dõi'}
                                            </span>
                                        </td>
                                        <td style="text-align: center;">
                                            <span class="crm-badge-ref ${item.soCoHoiThamChieu > 0 ? 'has-ref' : ''}">
                                                ${item.soCoHoiThamChieu} cơ hội
                                            </span>
                                        </td>
                                        <c:if test="${coQuyenQuanLy}">
                                            <td style="text-align: right;">
                                                <div class="crm-action-buttons" style="justify-content: flex-end;">
                                                    <button type="button" class="crm-btn-icon btn-edit-doi-thu" title="Chỉnh sửa đối thủ" aria-label="Chỉnh sửa đối thủ"
                                                            data-id="${item.id}" data-ma="${item.maDoiThu}" data-ten="${item.tenDoiThu}"
                                                            data-website="${item.website}" data-ghichu="${item.ghiChu}" data-hoatdong="${item.hoatDong}">
                                                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">edit</span>
                                                    </button>
                                                    <button type="button" class="crm-btn-icon crm-btn-delete btn-delete-record" title="Xóa đối thủ" aria-label="Xóa đối thủ"
                                                            data-action="xoa-doi-thu" data-id="${item.id}" data-name="${item.tenDoiThu}" data-ref="${item.soCoHoiThamChieu}">
                                                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">delete</span>
                                                    </button>
                                                </div>
                                            </td>
                                        </c:if>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty dsDoiThu}">
                                    <tr>
                                        <td colspan="${coQuyenQuanLy ? 7 : 6}">
                                            <div class="crm-empty-state">
                                                <span class="material-symbols-outlined crm-empty-icon" style="font-size: 48px;" aria-hidden="true">groups</span>
                                                <div class="crm-empty-title">Chưa có đối thủ cạnh tranh nào</div>
                                                <p class="crm-empty-desc">Nhấn nút "Thêm đối thủ cạnh tranh" để lập danh bạ theo dõi năng lực thị trường.</p>
                                            </div>
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <!-- PANE 4: QUY TẮC ĐÓNG CƠ HỘI SPRINT 5 & TRÌNH MÔ PHỎNG (AC3) -->
            <div class="crm-tab-pane" id="tab-pane-sprint5" style="display: ${currentTab == 'sprint5' ? 'block' : 'none'};">
                <div class="crm-sprint5-section">
                    <div class="crm-sprint5-header">
                        <h2 class="crm-sprint5-title">
                            <span class="material-symbols-outlined icon-md" aria-hidden="true" style="color: var(--primary);">description</span>
                            Quy Định Bắt Buộc Khi Đóng Cơ Hội (Sprint 5 - S5-05)
                        </h2>
                        <p class="crm-sprint5-subtitle">
                            Hệ thống kiểm soát chặt chẽ dữ liệu đầu vào khi đóng thương vụ để phục vụ phân tích báo cáo doanh số và chỉ tiêu.
                        </p>
                    </div>

                    <!-- 2 Thẻ quy tắc Thắng / Thua -->
                    <div class="crm-rule-cards-grid">
                        <!-- Quy tắc đóng Thắng -->
                        <div class="crm-rule-card crm-rule-card-win">
                            <h3 class="crm-rule-card-title">
                                <span class="material-symbols-outlined icon-md" aria-hidden="true" style="color:#059669;">emoji_events</span>
                                Khi Đóng THẮNG (Won)
                            </h3>
                            <ul class="crm-rule-list">
                                <li>
                                    <span class="material-symbols-outlined icon-sm" aria-hidden="true" style="color:#10b981;">check</span>
                                    <span><strong>Bắt buộc chọn Lý do thắng</strong> từ danh mục lý do đang hoạt động.</span>
                                </li>
                                <li>
                                    <span class="material-symbols-outlined icon-sm" aria-hidden="true" style="color:#10b981;">check</span>
                                    <span><strong>Bắt buộc nhập Giá trị chốt thực tế</strong> (lớn hơn 0) để tính vào chỉ tiêu doanh số của nhân viên sở hữu.</span>
                                </li>
                                <li>
                                    <span class="material-symbols-outlined icon-sm" aria-hidden="true" style="color:#10b981;">check</span>
                                    <span><strong>Bắt buộc nhập Ngày ký hợp đồng</strong> thực tế.</span>
                                </li>
                            </ul>
                        </div>

                        <!-- Quy tắc đóng Thua -->
                        <div class="crm-rule-card crm-rule-card-loss">
                            <h3 class="crm-rule-card-title">
                                <span class="material-symbols-outlined icon-md" aria-hidden="true" style="color:#ef4444;">trending_down</span>
                                Khi Đóng THUA (Lost)
                            </h3>
                            <ul class="crm-rule-list">
                                <li>
                                    <span class="material-symbols-outlined icon-sm" aria-hidden="true" style="color:#ef4444;">check</span>
                                    <span><strong>Bắt buộc chọn Lý do thua</strong> từ danh mục đang hoạt động để phân tích lỗ hổng bán hàng.</span>
                                </li>
                                <li>
                                    <span class="material-symbols-outlined icon-sm" aria-hidden="true" style="color:#ef4444;">check</span>
                                    <span><strong>Chọn Đối thủ cạnh tranh thắng thầu</strong> (nếu thất bại do cạnh tranh) để ghi nhận dữ liệu phân tích đối thủ.</span>
                                </li>
                                <li>
                                    <span class="material-symbols-outlined icon-sm" aria-hidden="true" style="color:#ef4444;">check</span>
                                    <span>Cơ hội đã đóng chuyển sang chế độ <strong>chỉ đọc</strong>, chỉ Trưởng nhóm trở lên mới mở lại được kèm lý do.</span>
                                </li>
                            </ul>
                        </div>
                    </div>

                    <!-- Hộp công cụ Mô phỏng & Kiểm chứng Trực tiếp -->
                    <div class="crm-simulator-box">
                        <div class="crm-simulator-title">
                            <span class="material-symbols-outlined icon-md" aria-hidden="true">fact_check</span>
                            Trình Kiểm Thử Ràng Buộc Dữ Liệu Sprint 5 (Live Validation Simulator)
                        </div>

                        <div class="crm-sim-form-grid">
                            <!-- Chọn Kết quả đóng -->
                            <div class="crm-form-group">
                                <label class="crm-form-label">Kết quả đóng cơ hội <span class="required">*</span></label>
                                <div style="display:flex; gap:16px; margin-top:6px;">
                                    <label style="display:flex; align-items:center; gap:6px; cursor:pointer;">
                                        <input type="radio" name="sim-trang-thai" id="sim-radio-thang" value="THANG" checked>
                                        <span style="font-weight:600; color:#065f46; display:inline-flex; align-items:center; gap:4px;">
                                            <span class="material-symbols-outlined icon-sm" aria-hidden="true">emoji_events</span> Đóng THẮNG
                                        </span>
                                    </label>
                                    <label style="display:flex; align-items:center; gap:6px; cursor:pointer;">
                                        <input type="radio" name="sim-trang-thai" id="sim-radio-thua" value="THUA">
                                        <span style="font-weight:600; color:#991b1b; display:inline-flex; align-items:center; gap:4px;">
                                            <span class="material-symbols-outlined icon-sm" aria-hidden="true">trending_down</span> Đóng THUA
                                        </span>
                                    </label>
                                </div>
                            </div>

                            <!-- Dropdown Lý do Thắng (hiện khi đóng Thắng) -->
                            <div class="crm-form-group" id="sim-group-thang-reason">
                                <label class="crm-form-label" for="sim-select-ly-do-thang">Lý do thắng <span class="required">*</span></label>
                                <select class="crm-form-control" id="sim-select-ly-do-thang">
                                    <option value="">-- Chọn lý do thắng --</option>
                                    <c:forEach var="ld" items="${dsLyDoThangKhaDung}">
                                        <option value="${ld.id}">[${ld.maLyDo}] ${ld.tenLyDo}</option>
                                    </c:forEach>
                                </select>
                            </div>

                            <!-- Dropdown Lý do Thua (hiện khi đóng Thua) -->
                            <div class="crm-form-group" id="sim-group-thua-reason" style="display:none;">
                                <label class="crm-form-label" for="sim-select-ly-do-thua">Lý do thua <span class="required">*</span></label>
                                <select class="crm-form-control" id="sim-select-ly-do-thua">
                                    <option value="">-- Chọn lý do thua --</option>
                                    <c:forEach var="ld" items="${dsLyDoThuaKhaDung}">
                                        <option value="${ld.id}">[${ld.maLyDo}] ${ld.tenLyDo}</option>
                                    </c:forEach>
                                </select>
                            </div>

                            <!-- Giá trị chốt thực tế (hiện khi đóng Thắng) -->
                            <div class="crm-form-group" id="sim-group-gia-tri">
                                <label class="crm-form-label" for="sim-input-gia-tri">Giá trị chốt thực tế (VNĐ) <span class="required">*</span></label>
                                <input type="number" class="crm-form-control" id="sim-input-gia-tri" placeholder="Ví dụ: 150000000" min="1">
                            </div>

                            <!-- Ngày ký hợp đồng (hiện khi đóng Thắng) -->
                            <div class="crm-form-group" id="sim-group-ngay-ky">
                                <label class="crm-form-label" for="sim-input-ngay-ky">Ngày ký hợp đồng <span class="required">*</span></label>
                                <input type="date" class="crm-form-control" id="sim-input-ngay-ky">
                            </div>

                            <!-- Dropdown Đối thủ cạnh tranh (hiện khi đóng Thua) -->
                            <div class="crm-form-group" id="sim-group-doi-thu" style="display:none;">
                                <label class="crm-form-label" for="sim-select-doi-thu">Đối thủ thắng thầu</label>
                                <select class="crm-form-control" id="sim-select-doi-thu">
                                    <option value="">-- Chọn đối thủ nếu có --</option>
                                    <c:forEach var="dt" items="${dsDoiThuKhaDung}">
                                        <option value="${dt.id}">[${dt.maDoiThu}] ${dt.tenDoiThu}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>

                        <div style="display:flex; gap:12px; flex-wrap:wrap; align-items:center;">
                            <button type="button" class="crm-btn crm-btn-primary" id="btn-simulate-validate">
                                <span class="material-symbols-outlined icon-sm" aria-hidden="true">check_circle</span>
                                Kiểm tra tính hợp lệ đóng cơ hội
                            </button>
                            <button type="button" class="crm-btn crm-btn-outline" id="btn-simulate-autofill" title="Tự động điền dữ liệu mẫu để kiểm thử nhanh quy tắc Sprint 5">
                                <span class="material-symbols-outlined icon-sm" aria-hidden="true">bolt</span>
                                Nạp dữ liệu kiểm thử nhanh
                            </button>
                        </div>

                        <!-- Khối hiển thị kết quả kiểm thử -->
                        <div class="crm-sim-result-box" id="sim-result-box"></div>
                    </div>
                </div>
            </div>

        </div>
    </main>

    <!-- =======================================================================
         MODAL 1: THÊM / SỬA LÝ DO THẮNG THUA (AC1)
         ======================================================================= -->
    <div class="crm-modal-backdrop" id="modal-ly-do" role="dialog" aria-modal="true" aria-labelledby="modal-ly-do-title">
        <div class="crm-modal-card">
            <div class="crm-modal-header">
                <h3 class="crm-modal-title" id="modal-ly-do-title">Khai báo lý do thắng thua</h3>
                <button type="button" class="crm-modal-close-btn" aria-label="Đóng" title="Đóng">
                    <span class="material-symbols-outlined" aria-hidden="true">close</span>
                </button>
            </div>
            <form id="form-ly-do" method="POST" action="${pageContext.request.contextPath}/danh-muc/ly-do-thang-thua">
                <input type="hidden" name="action" id="ly-do-action" value="them-ly-do">
                <input type="hidden" name="id" id="ly-do-id" value="">

                <div class="crm-modal-body">
                    <div class="crm-form-group">
                        <label class="crm-form-label" for="ly-do-loai">Loại lý do <span class="required">*</span></label>
                        <select class="crm-form-control" name="loai" id="ly-do-loai" required>
                            <option value="THANG">Lý do Thắng (Won Reason)</option>
                            <option value="THUA">Lý do Thua (Lost Reason)</option>
                        </select>
                    </div>

                    <div class="crm-form-group">
                        <label class="crm-form-label" for="ly-do-ma">Mã lý do (duy nhất) <span class="required">*</span></label>
                        <input type="text" class="crm-form-control" name="maLyDo" id="ly-do-ma" required
                               placeholder="Ví dụ: WIN_PRICE, LOSS_FEATURE" maxlength="50" style="text-transform:uppercase;">
                        <span style="font-size:11px; color:var(--slate-500);">Chỉ chứa chữ cái hoa, số và gạch dưới (VD: WIN_GIA_TOT).</span>
                    </div>

                    <div class="crm-form-group">
                        <label class="crm-form-label" for="ly-do-ten">Tên lý do hiển thị <span class="required">*</span></label>
                        <input type="text" class="crm-form-control" name="tenLyDo" id="ly-do-ten" required
                               placeholder="Ví dụ: Giá thành cạnh tranh vượt trội" maxlength="150">
                    </div>

                    <div class="crm-form-group">
                        <label class="crm-form-label" for="ly-do-thu-tu">Thứ tự hiển thị</label>
                        <input type="number" class="crm-form-control" name="thuTuHienThi" id="ly-do-thu-tu" value="0" min="0">
                    </div>

                    <div class="crm-form-group" style="flex-direction:row; align-items:center; gap:8px;">
                        <input type="checkbox" name="hoatDong" id="ly-do-hoat-dong" value="1" checked>
                        <label for="ly-do-hoat-dong" style="font-size:13px; font-weight:600; cursor:pointer;">Kích hoạt sử dụng ngay (Hoạt động)</label>
                    </div>
                </div>

                <div class="crm-modal-footer">
                    <button type="button" class="crm-btn crm-btn-outline btn-cancel-modal">Hủy bỏ</button>
                    <button type="submit" class="crm-btn crm-btn-primary" id="btn-save-ly-do">Lưu thông tin</button>
                </div>
            </form>
        </div>
    </div>

    <!-- =======================================================================
         MODAL 2: THÊM / SỬA ĐỐI THỦ CẠNH TRANH (AC2)
         ======================================================================= -->
    <div class="crm-modal-backdrop" id="modal-doi-thu" role="dialog" aria-modal="true" aria-labelledby="modal-doi-thu-title">
        <div class="crm-modal-card">
            <div class="crm-modal-header">
                <h3 class="crm-modal-title" id="modal-doi-thu-title">Khai báo đối thủ cạnh tranh</h3>
                <button type="button" class="crm-modal-close-btn" aria-label="Đóng" title="Đóng">
                    <span class="material-symbols-outlined" aria-hidden="true">close</span>
                </button>
            </div>
            <form id="form-doi-thu" method="POST" action="${pageContext.request.contextPath}/danh-muc/ly-do-thang-thua">
                <input type="hidden" name="action" id="doi-thu-action" value="them-doi-thu">
                <input type="hidden" name="id" id="doi-thu-id" value="">

                <div class="crm-modal-body">
                    <div class="crm-form-group">
                        <label class="crm-form-label" for="doi-thu-ma">Mã đối thủ (duy nhất) <span class="required">*</span></label>
                        <input type="text" class="crm-form-control" name="maDoiThu" id="doi-thu-ma" required
                               placeholder="Ví dụ: DT_MISA, DT_FAST" maxlength="50" style="text-transform:uppercase;">
                    </div>

                    <div class="crm-form-group">
                        <label class="crm-form-label" for="doi-thu-ten">Tên đối thủ cạnh tranh <span class="required">*</span></label>
                        <input type="text" class="crm-form-control" name="tenDoiThu" id="doi-thu-ten" required
                               placeholder="Ví dụ: Công ty Cổ phần MISA" maxlength="200">
                    </div>

                    <div class="crm-form-group">
                        <label class="crm-form-label" for="doi-thu-website">Website doanh nghiệp</label>
                        <input type="text" class="crm-form-control" name="website" id="doi-thu-website"
                               placeholder="https://www.example.com" maxlength="255">
                    </div>

                    <div class="crm-form-group">
                        <label class="crm-form-label" for="doi-thu-ghi-chu">Phân tích điểm mạnh, điểm yếu</label>
                        <textarea class="crm-form-control" name="ghiChu" id="doi-thu-ghi-chu" rows="3"
                                  placeholder="Ghi chú về phân khúc khách hàng, điểm mạnh sản phẩm, chiến thuật chào hàng..."></textarea>
                    </div>

                    <div class="crm-form-group" style="flex-direction:row; align-items:center; gap:8px;">
                        <input type="checkbox" name="hoatDong" id="doi-thu-hoat-dong" value="1" checked>
                        <label for="doi-thu-hoat-dong" style="font-size:13px; font-weight:600; cursor:pointer;">Đang theo dõi trên thị trường</label>
                    </div>
                </div>

                <div class="crm-modal-footer">
                    <button type="button" class="crm-btn crm-btn-outline btn-cancel-modal">Hủy bỏ</button>
                    <button type="submit" class="crm-btn crm-btn-primary" id="btn-save-doi-thu">Lưu đối thủ</button>
                </div>
            </form>
        </div>
    </div>

    <!-- =======================================================================
         MODAL 3: XÁC NHẬN XÓA AN TOÀN
         ======================================================================= -->
    <div class="crm-modal-backdrop" id="modal-xoa" role="dialog" aria-modal="true">
        <div class="crm-modal-card" style="max-width:440px;">
            <div class="crm-modal-header">
                <h3 class="crm-modal-title" style="color:#ef4444;">Xác nhận xóa bản ghi</h3>
                <button type="button" class="crm-modal-close-btn" aria-label="Đóng" title="Đóng">
                    <span class="material-symbols-outlined" aria-hidden="true">close</span>
                </button>
            </div>
            <form id="form-xoa" method="POST" action="${pageContext.request.contextPath}/danh-muc/ly-do-thang-thua">
                <input type="hidden" name="action" id="xoa-action" value="">
                <input type="hidden" name="id" id="xoa-id" value="">

                <div class="crm-modal-body">
                    <p style="font-size:14px; color:var(--slate-800);">
                        Đối tượng: <strong id="xoa-object-name"></strong>
                    </p>
                    <div id="xoa-warning-text" style="font-size:13px; color:var(--slate-600);"></div>
                </div>

                <div class="crm-modal-footer">
                    <button type="button" class="crm-btn crm-btn-outline btn-cancel-modal">Đóng</button>
                    <button type="submit" class="crm-btn" style="background:#ef4444; color:#ffffff;" id="btn-confirm-delete">Xác nhận xóa</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Scripts chuẩn của hệ thống -->
    <script>
        window.CRM_CONTEXT_PATH = '${pageContext.request.contextPath}';
    </script>
    <script src="${pageContext.request.contextPath}/assets/js/navigation.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/co-hoi/ly-do-thang-thua.js"></script>
</body>
</html>
