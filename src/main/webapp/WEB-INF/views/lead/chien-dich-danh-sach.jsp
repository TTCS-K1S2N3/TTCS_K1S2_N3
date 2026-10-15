<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Quản lý Chiến dịch Marketing và Theo dõi Lead & Doanh số theo Chiến dịch - CRM Bán Hàng Nhóm 10">
    <title>Chiến Dịch Marketing & Attribution - CRM Bán Hàng</title>

    <!-- Google Fonts & Navigation Styles -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    
    <!-- CSS Chuyên biệt Module Chiến dịch (Story S4-03) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lead/chien-dich.css">
</head>
<body class="crm-body">
    <!-- Header và Sidebar dùng chung toàn hệ thống -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <div class="cd-container">

            <!-- 1. Breadcrumb điều hướng -->
            <nav class="cd-breadcrumb" aria-label="Breadcrumb">
                <ol class="cd-breadcrumb-nav">
                    <li><a href="${pageContext.request.contextPath}/dieu-huong" class="cd-breadcrumb-link">Trang chủ</a></li>
                    <span class="cd-breadcrumb-sep" aria-hidden="true">/</span>
                    <li><a href="${pageContext.request.contextPath}/marketing/campaigns" class="cd-breadcrumb-link">Lead & Phân bổ</a></li>
                    <span class="cd-breadcrumb-sep" aria-hidden="true">/</span>
                    <li class="cd-breadcrumb-current" aria-current="page">Chiến dịch marketing</li>
                </ol>
                <div>
                    <a href="${pageContext.request.contextPath}/dieu-huong" class="cd-btn cd-btn-outline cd-btn-sm" id="btnQuayLaiTrangChu">
                        <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span>
                        <span>Trang chủ</span>
                    </a>
                </div>
            </nav>

            <!-- 2. Thông báo Flash (nếu có sau khi tạo hoặc thao tác) -->
            <c:if test="${not empty flashMessage}">
                <div class="cd-alert cd-alert-success" role="alert" id="flashAlert">
                    <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                    <span><c:out value="${flashMessage}" /></span>
                    <button type="button" class="cd-alert-close" aria-label="Đóng thông báo">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>
            </c:if>

            <c:if test="${not empty errorMessage}">
                <div class="cd-alert cd-alert-danger" role="alert">
                    <span class="material-symbols-outlined" aria-hidden="true">error</span>
                    <span><c:out value="${errorMessage}" /></span>
                    <button type="button" class="cd-alert-close" aria-label="Đóng thông báo">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>
            </c:if>

            <!-- 3. Header Card & Hành động tạo mới -->
            <header class="cd-header-card">
                <div class="cd-header-title">
                    <h1>
                        <span class="material-symbols-outlined" style="color: var(--cd-primary); font-size: 32px;" aria-hidden="true">campaign</span>
                        <span>Theo Dõi Chiến Dịch Marketing & Attribution</span>
                    </h1>
                    <p>Theo dõi lead và cơ hội theo từng chiến dịch, đo lường chính xác chiến dịch nào thực sự đem lại doanh thu đã chốt (Story S4-03).</p>
                </div>
                <div class="cd-header-actions">
                    <c:if test="${permissions.canCreate}">
                        <a href="${pageContext.request.contextPath}/marketing/campaign?action=new" class="cd-btn cd-btn-primary" id="btnKhaiBaoChienDich">
                            <span class="material-symbols-outlined" aria-hidden="true">add_circle</span>
                            <span>Khai báo chiến dịch mới</span>
                        </a>
                    </c:if>
                </div>
            </header>

            <!-- 4. Thống kê tổng quan nhanh (Executive KPI Cards - AC3) -->
            <c:set var="tongSoChienDich" value="${pagination != null ? pagination.totalItems : 0}" />
            <c:set var="tongLead" value="0" />
            <c:set var="tongCoHoi" value="0" />
            <c:set var="tongDoanhThuChot" value="0" />

            <c:if test="${not empty campaignRows}">
                <c:forEach items="${campaignRows}" var="item">
                    <c:set var="tongLead" value="${tongLead + item.metrics.leadCount}" />
                    <c:set var="tongCoHoi" value="${tongCoHoi + item.metrics.opportunityCount}" />
                    <c:set var="tongDoanhThuChot" value="${tongDoanhThuChot + item.metrics.closedWonAmount}" />
                </c:forEach>
            </c:if>

            <section class="cd-stats-grid" aria-label="Chỉ số hiệu quả tổng quan">
                <div class="cd-stat-card">
                    <div class="cd-stat-icon primary">
                        <span class="material-symbols-outlined" aria-hidden="true">campaign</span>
                    </div>
                    <div class="cd-stat-content">
                        <span class="cd-stat-value">${tongSoChienDich}</span>
                        <span class="cd-stat-label">Tổng số chiến dịch</span>
                    </div>
                </div>

                <div class="cd-stat-card">
                    <div class="cd-stat-icon primary">
                        <span class="material-symbols-outlined" aria-hidden="true">groups</span>
                    </div>
                    <div class="cd-stat-content">
                        <span class="cd-stat-value">${tongLead}</span>
                        <span class="cd-stat-label">Số Lead ghi nhận (Trang này)</span>
                    </div>
                </div>

                <div class="cd-stat-card">
                    <div class="cd-stat-icon purple">
                        <span class="material-symbols-outlined" aria-hidden="true">trending_up</span>
                    </div>
                    <div class="cd-stat-content">
                        <span class="cd-stat-value">${tongCoHoi}</span>
                        <span class="cd-stat-label">Cơ hội chuyển đổi</span>
                    </div>
                </div>

                <div class="cd-stat-card">
                    <div class="cd-stat-icon success">
                        <span class="material-symbols-outlined" aria-hidden="true">monetization_on</span>
                    </div>
                    <div class="cd-stat-content">
                        <span class="cd-stat-value cd-closed-won-highlight">
                            <fmt:formatNumber value="${tongDoanhThuChot}" pattern="#,##0" /> ₫
                        </span>
                        <span class="cd-stat-label">Tổng giá trị đã chốt</span>
                    </div>
                </div>
            </section>

            <!-- 5. Bảng danh sách chiến dịch & 3 chỉ số KPI (AC1, AC2, AC3) -->
            <section class="cd-table-card" aria-label="Bảng dữ liệu chiến dịch">
                <!-- Toolbar tìm kiếm nhanh -->
                <div class="cd-table-toolbar">
                    <div class="cd-search-box">
                        <span class="material-symbols-outlined cd-search-icon" aria-hidden="true">search</span>
                        <input type="text" id="searchCampaignInput" class="cd-search-input" placeholder="Tìm kiếm theo tên chiến dịch, mã hoặc kênh..." aria-label="Tìm kiếm chiến dịch">
                    </div>
                    <div style="font-size: 0.8125rem; color: var(--cd-neutral-500);">
                        <span>Hiển thị <strong>${campaignRows != null ? campaignRows.size() : 0}</strong> / ${pagination.totalItems} chiến dịch</span>
                    </div>
                </div>

                <div class="cd-table-wrapper">
                    <table class="cd-table" id="campaignTable">
                        <thead>
                            <tr>
                                <th scope="col" style="width: 28%;">Chiến dịch</th>
                                <th scope="col" style="width: 14%;">Kênh</th>
                                <th scope="col" style="width: 14%; text-align: right;">Ngân sách</th>
                                <th scope="col" style="width: 16%;">Thời gian chạy</th>
                                <th scope="col" style="width: 8%; text-align: center;">Số Lead</th>
                                <th scope="col" style="width: 8%; text-align: center;">Số Cơ hội</th>
                                <th scope="col" style="width: 12%; text-align: right;">Giá trị đã chốt</th>
                            </tr>
                        </thead>
                        <tbody id="campaignTableBody">
                            <c:choose>
                                <c:when test="${not empty campaignRows}">
                                    <c:forEach items="${campaignRows}" var="row">
                                        <tr class="cd-data-row">
                                            <!-- Tên & Mã chiến dịch -->
                                            <td>
                                                <div class="cd-table-campaign-name">
                                                    <a href="${pageContext.request.contextPath}/marketing/campaign?action=detail&id=${row.campaign.id}" class="cd-campaign-title-link" title="Xem chi tiết chiến dịch">
                                                        <c:out value="${row.campaign.name}" />
                                                    </a>
                                                    <span class="cd-campaign-code"><c:out value="${row.campaign.maChienDich}" /></span>
                                                </div>
                                            </td>

                                            <!-- Kênh marketing -->
                                            <td>
                                                <c:choose>
                                                    <c:when test="${row.campaign.channelCode == 'EVENT'}">
                                                        <span class="cd-badge cd-badge-channel-event">
                                                            <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">event</span>
                                                            <c:out value="${row.campaign.channelLabel}" />
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${row.campaign.channelCode == 'SOCIAL'}">
                                                        <span class="cd-badge cd-badge-channel-social">
                                                            <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">share</span>
                                                            <c:out value="${row.campaign.channelLabel}" />
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${row.campaign.channelCode == 'SEARCH'}">
                                                        <span class="cd-badge cd-badge-channel-search">
                                                            <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">search</span>
                                                            <c:out value="${row.campaign.channelLabel}" />
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${row.campaign.channelCode == 'EMAIL'}">
                                                        <span class="cd-badge cd-badge-channel-email">
                                                            <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">mail</span>
                                                            <c:out value="${row.campaign.channelLabel}" />
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${row.campaign.channelCode == 'PARTNER'}">
                                                        <span class="cd-badge cd-badge-channel-partner">
                                                            <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">handshake</span>
                                                            <c:out value="${row.campaign.channelLabel}" />
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="cd-badge cd-badge-channel-other">
                                                            <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">ads_click</span>
                                                            <c:out value="${row.campaign.channelLabel}" />
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>

                                            <!-- Ngân sách -->
                                            <td style="text-align: right; font-weight: 600;">
                                                <fmt:formatNumber value="${row.campaign.budgetAmount}" pattern="#,##0" /> ₫
                                            </td>

                                            <!-- Thời gian chạy -->
                                            <td style="font-size: 0.8125rem; color: var(--cd-neutral-600);">
                                                <span class="cd-date-range">
                                                    <c:out value="${row.campaign.startDate}" /> 
                                                    <span style="color: var(--cd-neutral-400); margin: 0 2px;">→</span> 
                                                    <c:out value="${row.campaign.endDate}" />
                                                </span>
                                            </td>

                                            <!-- AC3: Số Lead -->
                                            <td style="text-align: center;">
                                                <span class="cd-kpi-pill lead">
                                                    ${row.metrics.leadCount}
                                                </span>
                                            </td>

                                            <!-- AC3: Số Cơ hội -->
                                            <td style="text-align: center;">
                                                <span class="cd-kpi-pill opportunity">
                                                    ${row.metrics.opportunityCount}
                                                </span>
                                            </td>

                                            <!-- AC3: Giá trị đã chốt -->
                                            <td style="text-align: right;">
                                                <span class="cd-closed-won-highlight">
                                                    <fmt:formatNumber value="${row.metrics.closedWonAmount}" pattern="#,##0" /> ₫
                                                </span>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <!-- MOCK 02: Empty state khi chưa có chiến dịch -->
                                    <tr>
                                        <td colspan="7">
                                            <div class="cd-empty-state">
                                                <div class="cd-empty-icon" aria-hidden="true">
                                                    <span class="material-symbols-outlined">campaign</span>
                                                </div>
                                                <h3 class="cd-empty-title">Chưa có chiến dịch marketing nào</h3>
                                                <p class="cd-empty-desc">Hệ thống chưa ghi nhận chiến dịch nào. Hãy khai báo chiến dịch mới để bắt đầu theo dõi nguồn lead và doanh thu thực tế mang lại.</p>
                                                <c:if test="${permissions.canCreate}">
                                                    <a href="${pageContext.request.contextPath}/marketing/campaign?action=new" class="cd-btn cd-btn-primary" style="margin-top: 8px;">
                                                        <span class="material-symbols-outlined" aria-hidden="true">add_circle</span>
                                                        <span>Khai báo chiến dịch đầu tiên</span>
                                                    </a>
                                                </c:if>
                                            </div>
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>

                            <!-- Hàng thông báo không tìm thấy kết quả khi gõ filter -->
                            <tr id="noMatchRow" style="display: none;">
                                <td colspan="7" style="text-align: center; padding: 32px; color: var(--cd-neutral-500);">
                                    <span class="material-symbols-outlined" style="font-size: 32px; vertical-align: middle; margin-right: 6px;">search_off</span>
                                    <span>Không tìm thấy chiến dịch nào khớp với từ khóa tìm kiếm.</span>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <!-- Phân trang (Pagination) -->
                <c:if test="${pagination != null && pagination.totalPages > 1}">
                    <div class="cd-pagination-bar">
                        <div>
                            <span>Trang <strong>${pagination.page}</strong> / ${pagination.totalPages}</span>
                        </div>
                        <nav class="cd-pagination-controls" aria-label="Phân trang danh sách chiến dịch">
                            <!-- Nút Trang trước -->
                            <a href="${pageContext.request.contextPath}/marketing/campaigns?page=${pagination.page - 1}&pageSize=${pagination.pageSize}" 
                               class="cd-page-btn ${pagination.page <= 1 ? 'disabled' : ''}" 
                               aria-label="Trang trước"
                               <c:if test="${pagination.page <= 1}">tabindex="-1" aria-disabled="true"</c:if>>
                                <span class="material-symbols-outlined" aria-hidden="true">chevron_left</span>
                            </a>

                            <!-- Các số trang -->
                            <c:forEach begin="1" end="${pagination.totalPages}" var="p">
                                <a href="${pageContext.request.contextPath}/marketing/campaigns?page=${p}&pageSize=${pagination.pageSize}" 
                                   class="cd-page-btn ${p == pagination.page ? 'active' : ''}"
                                   <c:if test="${p == pagination.page}">aria-current="page"</c:if>>
                                    ${p}
                                </a>
                            </c:forEach>

                            <!-- Nút Trang sau -->
                            <a href="${pageContext.request.contextPath}/marketing/campaigns?page=${pagination.page + 1}&pageSize=${pagination.pageSize}" 
                               class="cd-page-btn ${pagination.page >= pagination.totalPages ? 'disabled' : ''}" 
                               aria-label="Trang sau"
                               <c:if test="${pagination.page >= pagination.totalPages}">tabindex="-1" aria-disabled="true"</c:if>>
                                <span class="material-symbols-outlined" aria-hidden="true">chevron_right</span>
                            </a>
                        </nav>
                    </div>
                </c:if>
            </section>

        </div>
    </main>

    <!-- JavaScript hỗ trợ tương tác giao diện -->
    <script src="${pageContext.request.contextPath}/assets/js/lead/chien-dich.js"></script>
</body>
</html>
