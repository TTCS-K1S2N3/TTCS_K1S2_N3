<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Chi tiết Chiến dịch Marketing và Báo cáo Attribution Lead & Cơ hội - CRM Bán Hàng Nhóm 10">
    <title><c:out value="${campaign.name}" /> - Chi Tiết Chiến Dịch Marketing</title>

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
    <!-- Header và Sidebar dùng chung -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <div class="cd-container">

            <!-- 1. Breadcrumb điều hướng -->
            <nav class="cd-breadcrumb" aria-label="Breadcrumb">
                <ol class="cd-breadcrumb-nav">
                    <li><a href="${pageContext.request.contextPath}/dieu-huong" class="cd-breadcrumb-link">Trang chủ</a></li>
                    <span class="cd-breadcrumb-sep" aria-hidden="true">/</span>
                    <li><a href="${pageContext.request.contextPath}/lead" class="cd-breadcrumb-link">Lead & Phân bổ</a></li>
                    <span class="cd-breadcrumb-sep" aria-hidden="true">/</span>
                    <li><a href="${pageContext.request.contextPath}/marketing/campaigns" class="cd-breadcrumb-link">Chiến dịch marketing</a></li>
                    <span class="cd-breadcrumb-sep" aria-hidden="true">/</span>
                    <li class="cd-breadcrumb-current" aria-current="page"><c:out value="${campaign.name}" /></li>
                </ol>
                <div>
                    <a href="${pageContext.request.contextPath}/marketing/campaigns" class="cd-btn cd-btn-outline cd-btn-sm" id="btnQuayLaiDanhSach">
                        <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span>
                        <span>Danh sách chiến dịch</span>
                    </a>
                </div>
            </nav>

            <!-- 2. Header Card -->
            <header class="cd-header-card">
                <div class="cd-header-title">
                    <div style="display: flex; align-items: center; gap: 10px; flex-wrap: wrap;">
                        <span class="cd-campaign-code" style="font-size: 0.875rem; background: var(--cd-neutral-100); padding: 2px 8px; border-radius: var(--cd-radius-sm);">
                            <c:out value="${campaign.maChienDich}" />
                        </span>
                        <!-- Kênh badge -->
                        <c:choose>
                            <c:when test="${campaign.channelCode == 'EVENT'}">
                                <span class="cd-badge cd-badge-channel-event">
                                    <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">event</span>
                                    <c:out value="${campaign.channelLabel}" />
                                </span>
                            </c:when>
                            <c:when test="${campaign.channelCode == 'SOCIAL'}">
                                <span class="cd-badge cd-badge-channel-social">
                                    <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">share</span>
                                    <c:out value="${campaign.channelLabel}" />
                                </span>
                            </c:when>
                            <c:when test="${campaign.channelCode == 'SEARCH'}">
                                <span class="cd-badge cd-badge-channel-search">
                                    <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">search</span>
                                    <c:out value="${campaign.channelLabel}" />
                                </span>
                            </c:when>
                            <c:when test="${campaign.channelCode == 'EMAIL'}">
                                <span class="cd-badge cd-badge-channel-email">
                                    <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">mail</span>
                                    <c:out value="${campaign.channelLabel}" />
                                </span>
                            </c:when>
                            <c:when test="${campaign.channelCode == 'PARTNER'}">
                                <span class="cd-badge cd-badge-channel-partner">
                                    <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">handshake</span>
                                    <c:out value="${campaign.channelLabel}" />
                                </span>
                            </c:when>
                            <c:otherwise>
                                <span class="cd-badge cd-badge-channel-other">
                                    <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">ads_click</span>
                                    <c:out value="${campaign.channelLabel}" />
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <h1>
                        <span class="material-symbols-outlined" style="color: var(--cd-primary); font-size: 32px;" aria-hidden="true">insights</span>
                        <span><c:out value="${campaign.name}" /></span>
                    </h1>
                    <p>Báo cáo hiệu quả đầu tư và theo dõi chỉ số chuyển đổi attribution từ Lead đến Cơ hội chốt Thắng (Story S4-03).</p>
                </div>
                <div class="cd-header-actions">
                    <c:if test="${permissions.canCreate}">
                        <a href="${pageContext.request.contextPath}/marketing/campaign?action=new" class="cd-btn cd-btn-outline" id="btnThemChienDichMoi">
                            <span class="material-symbols-outlined" aria-hidden="true">add</span>
                            <span>Tạo thêm chiến dịch</span>
                        </a>
                    </c:if>
                </div>
            </header>

            <!-- 3. KPI Highlights (AC3: 3 chỉ số theo từng chiến dịch) -->
            <section class="cd-stats-grid" aria-label="3 chỉ số hiệu quả chiến dịch (AC3)">
                <!-- 1. Số Lead -->
                <div class="cd-stat-card">
                    <div class="cd-stat-icon primary">
                        <span class="material-symbols-outlined" aria-hidden="true">groups</span>
                    </div>
                    <div class="cd-stat-content">
                        <span class="cd-stat-value">${metrics.leadCount}</span>
                        <span class="cd-stat-label">Số Lead ghi nhận (AC3)</span>
                    </div>
                </div>

                <!-- 2. Số Cơ hội -->
                <div class="cd-stat-card">
                    <div class="cd-stat-icon purple">
                        <span class="material-symbols-outlined" aria-hidden="true">trending_up</span>
                    </div>
                    <div class="cd-stat-content">
                        <span class="cd-stat-value">${metrics.opportunityCount}</span>
                        <span class="cd-stat-label">Số Cơ hội chuyển đổi (AC3)</span>
                    </div>
                </div>

                <!-- 3. Giá trị đã chốt -->
                <div class="cd-stat-card">
                    <div class="cd-stat-icon success">
                        <span class="material-symbols-outlined" aria-hidden="true">monetization_on</span>
                    </div>
                    <div class="cd-stat-content">
                        <span class="cd-stat-value cd-closed-won-highlight">
                            <fmt:formatNumber value="${metrics.closedWonAmount}" pattern="#,##0" /> ₫
                        </span>
                        <span class="cd-stat-label">Giá trị đã chốt (AC3)</span>
                    </div>
                </div>

                <!-- 4. Ngân sách dự toán -->
                <div class="cd-stat-card">
                    <div class="cd-stat-icon warning">
                        <span class="material-symbols-outlined" aria-hidden="true">payments</span>
                    </div>
                    <div class="cd-stat-content">
                        <span class="cd-stat-value">
                            <fmt:formatNumber value="${campaign.budgetAmount}" pattern="#,##0" /> ₫
                        </span>
                        <span class="cd-stat-label">Ngân sách dự toán (AC1)</span>
                    </div>
                </div>
            </section>

            <!-- 4. Chi tiết cấu hình & Thông tin chiến dịch (AC1) -->
            <div class="cd-detail-grid">

                <!-- Thẻ thông tin chi tiết -->
                <div class="cd-form-card" style="padding: 24px;">
                    <h2 class="cd-form-section-title">
                        <span class="material-symbols-outlined" style="color: var(--cd-primary);" aria-hidden="true">info</span>
                        <span>Thông Tin Khai Báo Chiến Dịch (AC1)</span>
                    </h2>

                    <table class="cd-detail-info-table">
                        <tbody>
                            <tr>
                                <th scope="row">Mã chiến dịch:</th>
                                <td><code style="font-weight: 600;"><c:out value="${campaign.maChienDich}" /></code></td>
                            </tr>
                            <tr>
                                <th scope="row">Tên chiến dịch:</th>
                                <td><strong><c:out value="${campaign.name}" /></strong></td>
                            </tr>
                            <tr>
                                <th scope="row">Kênh marketing:</th>
                                <td><c:out value="${campaign.channelLabel}" /> (<code>${campaign.channelCode}</code>)</td>
                            </tr>
                            <tr>
                                <th scope="row">Ngân sách dự toán:</th>
                                <td>
                                    <span style="font-weight: 700; color: var(--cd-neutral-900);">
                                        <fmt:formatNumber value="${campaign.budgetAmount}" pattern="#,##0" /> ₫
                                    </span>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">Thời gian vận hành:</th>
                                <td>
                                    <span class="material-symbols-outlined" style="font-size: 16px; vertical-align: middle; color: var(--cd-neutral-500);" aria-hidden="true">calendar_today</span>
                                    <span><c:out value="${campaign.startDate}" /></span> 
                                    <span style="color: var(--cd-neutral-400); margin: 0 4px;">đến</span> 
                                    <span><c:out value="${campaign.endDate}" /></span>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">Trạng thái:</th>
                                <td>
                                    <span class="cd-badge" style="background: var(--cd-success-light); color: var(--cd-success-text); border: 1px solid var(--cd-success-border);">
                                        <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">play_circle</span>
                                        <span>Đang chạy</span>
                                    </span>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">Mô tả mục tiêu:</th>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty campaign.moTa}">
                                            <div style="white-space: pre-line; line-height: 1.5; color: var(--cd-neutral-700);"><c:out value="${campaign.moTa}" /></div>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="color: var(--cd-neutral-400); font-style: italic;">Chưa có mô tả chi tiết.</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <!-- Thẻ Phân tích Hiệu quả Đầu tư (ROI) -->
                <aside class="cd-sidebar">
                    <div class="cd-sidebar-card">
                        <h3 class="cd-sidebar-title">
                            <span class="material-symbols-outlined" style="color: var(--cd-success);" aria-hidden="true">query_stats</span>
                            <span>Hiệu Quả Đầu Tư (AC3)</span>
                        </h3>

                        <c:set var="nganSach" value="${campaign.budgetAmount}" />
                        <c:set var="doanhThuChot" value="${metrics.closedWonAmount}" />
                        <c:choose>
                            <c:when test="${nganSach > 0 && doanhThuChot > 0}">
                                <div style="display: flex; flex-direction: column; gap: 14px;">
                                    <div style="background: var(--cd-neutral-50); padding: 14px; border-radius: var(--cd-radius-md); border: 1px solid var(--cd-neutral-200);">
                                        <div style="font-size: 0.75rem; color: var(--cd-neutral-500); text-transform: uppercase; font-weight: 600;">Chênh lệch Doanh số / Ngân sách</div>
                                        <div style="font-size: 1.25rem; font-weight: 700; color: ${doanhThuChot >= nganSach ? 'var(--cd-success)' : 'var(--cd-danger)'}; margin-top: 4px;">
                                            <fmt:formatNumber value="${doanhThuChot - nganSach}" pattern="#,##0" /> ₫
                                        </div>
                                    </div>
                                    <p style="font-size: 0.8125rem; color: var(--cd-neutral-600); margin: 0; line-height: 1.5;">
                                        Chiến dịch đã mang lại <strong><fmt:formatNumber value="${doanhThuChot}" pattern="#,##0" /> ₫</strong> giá trị hợp đồng chốt Thắng từ <strong>${metrics.opportunityCount}</strong> cơ hội thành công.
                                    </p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div style="padding: 16px; background: var(--cd-neutral-50); border-radius: var(--cd-radius-md); text-align: center; color: var(--cd-neutral-500); font-size: 0.8125rem;">
                                    <span class="material-symbols-outlined" style="font-size: 28px; margin-bottom: 4px;" aria-hidden="true">hourglass_empty</span>
                                    <div>Chưa có hợp đồng nào được đóng Thắng từ chiến dịch này để tính tỷ lệ hoàn vốn.</div>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="cd-sidebar-card">
                        <h3 class="cd-sidebar-title">
                            <span class="material-symbols-outlined" style="color: var(--cd-primary);" aria-hidden="true">link</span>
                            <span>Liên Kết Nhanh</span>
                        </h3>
                        <div style="display: flex; flex-direction: column; gap: 8px;">
                            <a href="${pageContext.request.contextPath}/marketing/campaigns" class="cd-btn cd-btn-outline" style="justify-content: flex-start;">
                                <span class="material-symbols-outlined" aria-hidden="true">list</span>
                                <span>Xem toàn bộ chiến dịch</span>
                            </a>
                        </div>
                    </div>
                </aside>

            </div>

            <!-- 5. Luồng Truy Vết Nguồn Chiến Dịch (AC2: Lead & Cơ hội giữ liên kết tới chiến dịch) -->
            <section class="cd-attribution-flow" aria-label="Sơ đồ truy vết Attribution (AC2)">
                <h2 class="cd-form-section-title" style="margin-bottom: 8px;">
                    <span class="material-symbols-outlined" style="color: var(--cd-purple);" aria-hidden="true">account_tree</span>
                    <span>Quy Trình Gắn Kết Nguồn Chiến Dịch (Story S4-03, AC2)</span>
                </h2>
                <p style="font-size: 0.875rem; color: var(--cd-neutral-500); margin: 0 0 16px 0;">
                    Mọi Lead và Cơ hội sinh ra từ chiến dịch này đều giữ vững liên kết toàn vẹn dữ liệu trong suốt vòng đời kinh doanh:
                </p>

                <div class="cd-flow-steps">
                    <!-- Bước 1: Tiếp nhận Lead -->
                    <div class="cd-flow-step">
                        <div class="cd-flow-step-header">
                            <span class="material-symbols-outlined" style="color: var(--cd-primary); font-size: 24px;" aria-hidden="true">person_add</span>
                            <span>1. Thu Thập Lead Nguồn</span>
                        </div>
                        <div class="cd-flow-step-desc">
                            Lead được thu thập qua Biểu mẫu nhúng (S4-01) hoặc Nhập Excel (S4-02), được gắn tự động mã chiến dịch <code>${campaign.maChienDich}</code>.
                        </div>
                        <div class="cd-flow-step-badge">
                            ${metrics.leadCount} Lead ghi nhận
                        </div>
                    </div>

                    <!-- Bước 2: Chuyển đổi Cơ hội -->
                    <div class="cd-flow-step">
                        <div class="cd-flow-step-header">
                            <span class="material-symbols-outlined" style="color: var(--cd-purple); font-size: 24px;" aria-hidden="true">swap_horiz</span>
                            <span>2. Chuyển Đổi Thành Cơ Hội</span>
                        </div>
                        <div class="cd-flow-step-desc">
                            Khi nhân viên Sales chuyển đổi Lead thành Cơ hội kinh doanh (S4-08), liên kết chiến dịch nguồn được kế thừa tự động và bất biến.
                        </div>
                        <div class="cd-flow-step-badge" style="color: var(--cd-purple);">
                            ${metrics.opportunityCount} Cơ hội mở ra
                        </div>
                    </div>

                    <!-- Bước 3: Đóng Thắng & Doanh thu -->
                    <div class="cd-flow-step">
                        <div class="cd-flow-step-header">
                            <span class="material-symbols-outlined" style="color: var(--cd-success); font-size: 24px;" aria-hidden="true">verified</span>
                            <span>3. Đóng Thắng Hợp Đồng</span>
                        </div>
                        <div class="cd-flow-step-desc">
                            Khi cơ hội được đánh dấu Đóng Thắng (S5-05), giá trị chốt thực tế được ghi nhận và tự động cộng dồn vào doanh số của chiến dịch.
                        </div>
                        <div class="cd-flow-step-badge" style="color: var(--cd-success);">
                            <fmt:formatNumber value="${metrics.closedWonAmount}" pattern="#,##0" /> ₫ đã chốt
                        </div>
                    </div>
                </div>
            </section>

        </div>
    </main>

    <!-- JavaScript hỗ trợ tương tác -->
    <script src="${pageContext.request.contextPath}/assets/js/lead/chien-dich.js"></script>
</body>
</html>
