<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Quản lý biểu mẫu nhúng thu thập lead từ website - CRM Bán Hàng Nhóm 10">
    <title>Biểu Mẫu Thu Thập Lead Website - CRM Bán Hàng</title>
    <!-- CSS dùng chung toàn hệ thống (Navigation và App Tokens) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <!-- CSS chuyên biệt module Biểu mẫu Lead (Story S4-01) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lead/bieu-mau.css">
</head>
<body class="crm-body">
    <!-- Navigation layout dùng chung S1-06 (đã bao gồm sidebar, thông tin user và Material Symbols) -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <div class="bml-container">

            <!-- 1. Breadcrumb điều hướng chuẩn -->
            <nav class="bml-breadcrumb" aria-label="Breadcrumb">
                <ol class="bml-breadcrumb-nav">
                    <li><a href="${pageContext.request.contextPath}/dieu-huong" class="bml-breadcrumb-link">Trang chủ</a></li>
                    <span class="bml-breadcrumb-sep" aria-hidden="true">/</span>
                    <li><a href="${pageContext.request.contextPath}/lead" class="bml-breadcrumb-link">Lead & Phân bổ</a></li>
                    <span class="bml-breadcrumb-sep" aria-hidden="true">/</span>
                    <li class="bml-breadcrumb-current" aria-current="page">Biểu mẫu thu thập Lead</li>
                </ol>
                <div>
                    <a href="${pageContext.request.contextPath}/dieu-huong" class="bml-btn bml-btn-outline bml-btn-sm" id="btnQuayLaiTrangChu">
                        <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span>
                        <span>Trang chủ</span>
                    </a>
                </div>
            </nav>

            <!-- 2. Header Card & Hành động tạo mới -->
            <header class="bml-header-card">
                <div class="bml-header-title">
                    <h1>
                        <span class="material-symbols-outlined" style="color: var(--bml-primary); font-size: 28px;" aria-hidden="true">dynamic_form</span>
                        <span>Biểu Mẫu Nhúng Thu Thập Lead Website</span>
                    </h1>
                    <p>Sinh mã nhúng dán vào landing page hoặc website bất kỳ, tự động thu thập lead mới và gắn đúng nguồn (Story S4-01).</p>
                </div>
                <div class="bml-header-actions">
                    <a href="${pageContext.request.contextPath}/bieu-mau/tao" class="bml-btn bml-btn-primary" id="btnTaoBieuMau">
                        <span class="material-symbols-outlined" aria-hidden="true">add</span>
                        <span>Tạo biểu mẫu mới</span>
                    </a>
                </div>
            </header>

            <!-- 3. Thống kê tổng quan nhanh (Executive View) -->
            <c:set var="tongSo" value="${dsBieuMau != null ? dsBieuMau.size() : 0}" />
            <c:set var="dangHoatDong" value="0" />
            <c:set var="coHoneypot" value="0" />
            <c:forEach items="${dsBieuMau}" var="item">
                <c:if test="${item.hoatDong}">
                    <c:set var="dangHoatDong" value="${dangHoatDong + 1}" />
                </c:if>
                <c:if test="${item.batHoneypot}">
                    <c:set var="coHoneypot" value="${coHoneypot + 1}" />
                </c:if>
            </c:forEach>
            <section class="bml-stats-grid" aria-label="Thống kê biểu mẫu">
                <div class="bml-stat-card">
                    <div class="bml-stat-icon primary">
                        <span class="material-symbols-outlined" aria-hidden="true">list_alt</span>
                    </div>
                    <div class="bml-stat-content">
                        <span class="bml-stat-value">${tongSo}</span>
                        <span class="bml-stat-label">Tổng biểu mẫu đã tạo</span>
                    </div>
                </div>
                <div class="bml-stat-card">
                    <div class="bml-stat-icon success">
                        <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                    </div>
                    <div class="bml-stat-content">
                        <span class="bml-stat-value">${dangHoatDong}</span>
                        <span class="bml-stat-label">Đang tiếp nhận Lead</span>
                    </div>
                </div>
                <div class="bml-stat-card">
                    <div class="bml-stat-icon warning">
                        <span class="material-symbols-outlined" aria-hidden="true">pause_circle</span>
                    </div>
                    <div class="bml-stat-content">
                        <span class="bml-stat-value">${tongSo - dangHoatDong}</span>
                        <span class="bml-stat-label">Tạm dừng nhận thông tin</span>
                    </div>
                </div>
                <div class="bml-stat-card">
                    <div class="bml-stat-icon primary">
                        <span class="material-symbols-outlined" aria-hidden="true">shield</span>
                    </div>
                    <div class="bml-stat-content">
                        <span class="bml-stat-value">${coHoneypot}</span>
                        <span class="bml-stat-label">Biểu mẫu bật Honeypot</span>
                    </div>
                </div>
            </section>

            <!-- 4. Thông báo hệ thống Flash Messages -->
            <c:if test="${not empty sessionScope.thongBaoThanhCong}">
                <div class="bml-alert bml-alert-success" role="alert">
                    <div class="bml-alert-content">
                        <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                        <span><c:out value="${sessionScope.thongBaoThanhCong}" /></span>
                    </div>
                    <button type="button" class="bml-alert-close" onclick="this.parentElement.style.display='none';" aria-label="Đóng thông báo">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>
                <c:remove var="thongBaoThanhCong" scope="session" />
            </c:if>

            <c:if test="${not empty requestScope.thongBaoLoi}">
                <div class="bml-alert bml-alert-danger" role="alert">
                    <div class="bml-alert-content">
                        <span class="material-symbols-outlined" aria-hidden="true">error</span>
                        <span><c:out value="${requestScope.thongBaoLoi}" /></span>
                    </div>
                    <button type="button" class="bml-alert-close" onclick="this.parentElement.style.display='none';" aria-label="Đóng thông báo">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>
            </c:if>

            <!-- 5. Thanh công cụ tìm kiếm và lọc biểu mẫu nhanh -->
            <div class="bml-toolbar">
                <div class="bml-search-box">
                    <span class="material-symbols-outlined bml-search-icon" aria-hidden="true">search</span>
                    <input type="text" id="bmlSearchInput" class="bml-search-input"
                           placeholder="Tìm kiếm theo mã hoặc tên biểu mẫu..."
                           aria-label="Tìm kiếm biểu mẫu" />
                </div>
                <div class="bml-filter-group">
                    <label for="bmlFilterStatus" style="font-size: 0.8125rem; font-weight: 600; color: var(--bml-neutral-600);">Trạng thái:</label>
                    <select id="bmlFilterStatus" class="bml-filter-select">
                        <option value="ALL">Tất cả trạng thái</option>
                        <option value="ACTIVE">Đang hoạt động</option>
                        <option value="INACTIVE">Tạm dừng</option>
                    </select>
                </div>
            </div>

            <!-- 6. Bảng danh sách biểu mẫu nhúng -->
            <div class="bml-table-card">
                <div class="bml-table-responsive">
                    <table class="bml-table" id="bmlTable">
                        <thead>
                            <tr>
                                <th>Mã Biểu Mẫu</th>
                                <th>Tên Biểu Mẫu</th>
                                <th>Nguồn Lead (AC4)</th>
                                <th>Chiến Dịch</th>
                                <th>Giới Hạn IP (AC3)</th>
                                <th>Chống Spam (AC3)</th>
                                <th>Trạng Thái</th>
                                <th style="text-align: right;">Thao Tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty dsBieuMau}">
                                    <tr id="bmlRowEmptyData">
                                        <td colspan="8">
                                            <div class="bml-empty-state">
                                                <span class="material-symbols-outlined bml-empty-icon" aria-hidden="true">post_add</span>
                                                <div class="bml-empty-title">Chưa có biểu mẫu thu thập lead nào</div>
                                                <div class="bml-empty-desc">Tạo biểu mẫu đầu tiên để nhận mã nhúng, dán vào website và tự động thu thập thông tin khách hàng tiềm năng.</div>
                                                <a href="${pageContext.request.contextPath}/bieu-mau/tao" class="bml-btn bml-btn-primary bml-btn-sm">
                                                    <span class="material-symbols-outlined" aria-hidden="true">add</span>
                                                    <span>Tạo biểu mẫu mới</span>
                                                </a>
                                            </div>
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach items="${dsBieuMau}" var="bm">
                                        <tr data-ma="${bm.maBieuMau}" data-ten="${bm.tenBieuMau}" data-trang-thai="${bm.hoatDong ? 'ACTIVE' : 'INACTIVE'}">
                                            <td>
                                                <strong style="color: var(--bml-primary);"><c:out value="${bm.maBieuMau}" /></strong>
                                            </td>
                                            <td>
                                                <div style="font-weight: 600; color: var(--bml-neutral-900);"><c:out value="${bm.tenBieuMau}" /></div>
                                                <div style="font-size: 0.75rem; color: var(--bml-neutral-400); font-family: monospace;">Token: <c:out value="${bm.tokenCongKhai}" /></div>
                                            </td>
                                            <td>
                                                <span class="bml-badge bml-badge-primary">
                                                    <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">share</span>
                                                    <c:out value="${bm.tenNguonLead != null ? bm.tenNguonLead : 'Website Form'}" />
                                                </span>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty bm.tenChienDich}">
                                                        <div style="font-weight: 500;"><c:out value="${bm.tenChienDich}" /></div>
                                                        <c:if test="${not empty bm.maChienDich}">
                                                            <div style="font-size: 0.75rem; color: var(--bml-neutral-400); font-family: monospace;"><c:out value="${bm.maChienDich}" /></div>
                                                        </c:if>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span style="color: var(--bml-neutral-400);">-</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <span style="font-weight: 500;"><c:out value="${bm.gioiHanGuiMoiGio}" /></span>
                                                <span style="font-size: 0.75rem; color: var(--bml-neutral-500);">lượt/giờ/IP</span>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${bm.batHoneypot}">
                                                        <span class="bml-badge bml-badge-success" title="Chống bot spam bằng trường bẫy Honeypot ẩn">
                                                            <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">verified_user</span>
                                                            <span>Bật Honeypot</span>
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="bml-badge bml-badge-secondary">Tắt</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${bm.hoatDong}">
                                                        <span class="bml-badge bml-badge-success">
                                                            <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">check</span>
                                                            <span>Đang nhận Lead</span>
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="bml-badge bml-badge-secondary">
                                                            <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">pause</span>
                                                            <span>Tạm dừng</span>
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <div class="bml-table-actions">
                                                    <!-- AC1: Nút Lấy mã nhúng -->
                                                    <button type="button" class="bml-btn bml-btn-outline bml-btn-sm"
                                                            onclick="hienThiMaNhung(${bm.id})" title="Lấy mã nhúng Iframe/Script để dán vào website">
                                                        <span class="material-symbols-outlined" aria-hidden="true">code</span>
                                                        <span>Mã nhúng</span>
                                                    </button>

                                                    <!-- Nút Xem thử biểu mẫu -->
                                                    <a href="${pageContext.request.contextPath}/bieu-mau/nhung?token=${bm.tokenCongKhai}"
                                                       target="_blank" rel="noopener noreferrer"
                                                       class="bml-btn bml-btn-outline bml-btn-sm" title="Mở biểu mẫu nhúng ở tab mới để xem trước">
                                                        <span class="material-symbols-outlined" aria-hidden="true">open_in_new</span>
                                                        <span>Xem trước</span>
                                                    </a>

                                                    <!-- Nút Bật/Tắt trạng thái hoạt động -->
                                                    <form action="${pageContext.request.contextPath}/bieu-mau/trang-thai" method="POST" style="display:inline; margin:0;">
                                                        <input type="hidden" name="id" value="${bm.id}" />
                                                        <input type="hidden" name="hoat_dong" value="${bm.hoatDong ? '0' : '1'}" />
                                                        <button type="submit" class="bml-btn bml-btn-outline bml-btn-sm"
                                                                title="${bm.hoatDong ? 'Tạm dừng biểu mẫu này' : 'Kích hoạt lại biểu mẫu'}">
                                                            <span class="material-symbols-outlined" aria-hidden="true">${bm.hoatDong ? 'pause' : 'play_arrow'}</span>
                                                            <span>${bm.hoatDong ? 'Tạm dừng' : 'Bật lại'}</span>
                                                        </button>
                                                    </form>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    <tr id="bmlRowEmptySearch" style="display: none;">
                                        <td colspan="8">
                                            <div class="bml-empty-state">
                                                <span class="material-symbols-outlined bml-empty-icon" aria-hidden="true">search_off</span>
                                                <div class="bml-empty-title">Không tìm thấy biểu mẫu phù hợp</div>
                                                <div class="bml-empty-desc">Không có biểu mẫu nào khớp với từ khóa tìm kiếm hoặc bộ lọc hiện tại.</div>
                                            </div>
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>

        </div>
    </main>

    <!-- 7. Modal hiển thị mã nhúng website (AC1) -->
    <div id="modalMaNhung" class="bml-modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="modalMaNhungTitle">
        <div class="bml-modal">
            <div class="bml-modal-header">
                <h3 id="modalMaNhungTitle">
                    <span class="material-symbols-outlined" style="color: var(--bml-primary);" aria-hidden="true">integration_instructions</span>
                    <span>Mã Nhúng Biểu Mẫu Website (AC1)</span>
                </h3>
                <button type="button" class="bml-modal-close-btn" onclick="dongModalMaNhung()" aria-label="Đóng cửa sổ mã nhúng">
                    <span class="material-symbols-outlined" aria-hidden="true">close</span>
                </button>
            </div>
            <div class="bml-modal-body">
                <p class="bml-modal-desc">
                    Sao chép một trong các định dạng mã bên dưới và dán vào mã nguồn website, blog hoặc Landing Page bất kỳ (WordPress, Webflow, HTML tĩnh) để hiển thị biểu mẫu thu thập lead:
                </p>

                <!-- 1. Iframe Embed Code (Khuyến nghị) -->
                <div class="bml-embed-section">
                    <div class="bml-embed-header">
                        <span class="bml-embed-title">
                            <span class="material-symbols-outlined" style="font-size: 16px; color: var(--bml-primary);" aria-hidden="true">featured_video</span>
                            <span>1. Mã nhúng Iframe (Khuyến nghị an toàn & tương thích cao)</span>
                        </span>
                        <button type="button" class="bml-btn bml-btn-outline bml-btn-sm" onclick="saoChepMaNhung('codeIframe', 'Mã nhúng Iframe')">
                            <span class="material-symbols-outlined" aria-hidden="true">content_copy</span>
                            <span>Sao chép</span>
                        </button>
                    </div>
                    <div id="codeIframe" class="bml-code-box"></div>
                </div>

                <!-- 2. Script Tag Embed Code -->
                <div class="bml-embed-section">
                    <div class="bml-embed-header">
                        <span class="bml-embed-title">
                            <span class="material-symbols-outlined" style="font-size: 16px; color: var(--bml-primary);" aria-hidden="true">javascript</span>
                            <span>2. Mã nhúng JavaScript Widget</span>
                        </span>
                        <button type="button" class="bml-btn bml-btn-outline bml-btn-sm" onclick="saoChepMaNhung('codeScript', 'Mã nhúng Script')">
                            <span class="material-symbols-outlined" aria-hidden="true">content_copy</span>
                            <span>Sao chép</span>
                        </button>
                    </div>
                    <div id="codeScript" class="bml-code-box"></div>
                </div>

                <!-- 3. Direct Public Link -->
                <div class="bml-embed-section">
                    <div class="bml-embed-header">
                        <span class="bml-embed-title">
                            <span class="material-symbols-outlined" style="font-size: 16px; color: var(--bml-primary);" aria-hidden="true">link</span>
                            <span>3. Đường dẫn trực tiếp (Gắn vào nút bấm hoặc chiến dịch Email/Zalo)</span>
                        </span>
                        <button type="button" class="bml-btn bml-btn-outline bml-btn-sm" onclick="saoChepMaNhung('codeUrl', 'Đường dẫn trực tiếp')">
                            <span class="material-symbols-outlined" aria-hidden="true">content_copy</span>
                            <span>Sao chép</span>
                        </button>
                    </div>
                    <div id="codeUrl" class="bml-code-box"></div>
                </div>
            </div>
            <div class="bml-modal-footer">
                <button type="button" class="bml-btn bml-btn-outline" onclick="dongModalMaNhung()">Đóng</button>
            </div>
        </div>
    </div>

    <!-- Scripts điều hướng hệ thống và xử lý module -->
    <script>window.__CRM_CONTEXT_PATH = '${pageContext.request.contextPath}';</script>
    <script src="${pageContext.request.contextPath}/assets/js/navigation.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/lead/bieu-mau.js"></script>
</body>
</html>
