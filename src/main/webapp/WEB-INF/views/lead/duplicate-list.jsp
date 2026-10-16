<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Trung tâm Cảnh báo và Gộp Lead trùng lặp - CRM Bán Hàng Nhóm 10">
    <title>Cảnh Báo & Gộp Lead Trùng - CRM Bán Hàng</title>

    <!-- Google Fonts & Navigation Styles -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    
    <!-- CSS Chuyên biệt Module Gộp Lead (Story S4-04) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lead/gop-lead.css">
</head>
<body class="crm-body">
    <!-- Header và Sidebar dùng chung -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <div class="gl-container">

            <!-- 1. Breadcrumb điều hướng -->
            <nav class="gl-breadcrumb" aria-label="Breadcrumb">
                <ol class="gl-breadcrumb-nav">
                    <li><a href="${pageContext.request.contextPath}/dieu-huong" class="gl-breadcrumb-link">Trang chủ</a></li>
                    <span class="gl-breadcrumb-sep" aria-hidden="true">/</span>
                    <li><a href="${pageContext.request.contextPath}/lead" class="gl-breadcrumb-link">Danh sách Lead</a></li>
                    <span class="gl-breadcrumb-sep" aria-hidden="true">/</span>
                    <li class="gl-breadcrumb-current" aria-current="page">Cảnh báo và gộp lead trùng (S4-04)</li>
                </ol>
                <div style="display: flex; gap: 8px;">
                    <a href="${pageContext.request.contextPath}/lead" class="gl-btn gl-btn-outline gl-btn-sm" id="btnQuayLaiDanhSachLead">
                        <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span>
                        <span>Danh sách Lead</span>
                    </a>
                </div>
            </nav>

            <!-- 2. Thông báo Flash nếu có -->
            <c:if test="${not empty flashMessage}">
                <div class="gl-alert gl-alert-success" role="alert">
                    <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                    <span><c:out value="${flashMessage}" /></span>
                    <button type="button" class="gl-alert-close" aria-label="Đóng thông báo">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>
            </c:if>

            <c:if test="${not empty errorMessage}">
                <div class="gl-alert gl-alert-danger" role="alert">
                    <span class="material-symbols-outlined" aria-hidden="true">error</span>
                    <span><c:out value="${errorMessage}" /></span>
                    <button type="button" class="gl-alert-close" aria-label="Đóng thông báo">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>
            </c:if>

            <!-- 3. Header Card -->
            <header class="gl-header-card">
                <div class="gl-header-title">
                    <h1>
                        <span class="material-symbols-outlined" style="color: var(--gl-primary); font-size: 32px;" aria-hidden="true">content_copy</span>
                        <span>Trung Tâm Cảnh Báo & Gộp Lead Trùng</span>
                    </h1>
                    <p>Phát hiện thông minh theo Email, Số điện thoại, Tên công ty để ngăn ngừa 2 nhân viên cùng chăm sóc một khách hàng và giữ trọn vẹn toàn bộ lịch sử (Story S4-04).</p>
                </div>
            </header>

            <!-- 4. Công cụ kiểm tra trùng nhanh theo mã Lead -->
            <section class="gl-card">
                <h2 class="gl-section-title">
                    <span style="display: flex; align-items: center; gap: 8px;">
                        <span class="material-symbols-outlined" style="color: var(--gl-primary);" aria-hidden="true">search_check</span>
                        <span>Tra Cứu & Kiểm Tra Trùng Cho Bản Ghi Lead</span>
                    </span>
                </h2>
                <p style="font-size: 0.875rem; color: var(--gl-neutral-600); margin: 0 0 20px 0;">
                    Nhập mã định danh (Lead ID) để hệ thống quét tự động toàn bộ cơ sở dữ liệu và tìm các Lead hoặc Khách hàng trùng lặp:
                </p>

                <form action="${pageContext.request.contextPath}/lead/duplicate" method="GET" style="display: flex; gap: 12px; flex-wrap: wrap; align-items: center; max-width: 600px;">
                    <input type="hidden" name="action" value="check">
                    <div style="position: relative; flex: 1; min-width: 240px;">
                        <span class="material-symbols-outlined" style="position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: var(--gl-neutral-400);" aria-hidden="true">pin</span>
                        <input type="number" 
                               name="leadId" 
                               id="inputLeadId" 
                               placeholder="Nhập mã ID của Lead (Ví dụ: 101, 102...)" 
                               required 
                               min="1"
                               style="width: 100%; padding: 10px 14px 10px 40px; font-size: 0.875rem; border: 1px solid var(--gl-neutral-300); border-radius: var(--gl-radius-md); box-sizing: border-box;">
                    </div>
                    <button type="submit" class="gl-btn gl-btn-primary" id="btnKiemTraTrung">
                        <span class="material-symbols-outlined" aria-hidden="true">travel_explore</span>
                        <span>Kiểm tra trùng</span>
                    </button>
                </form>
            </section>

            <!-- 5. 3 Trụ cột Acceptance Criteria (AC1, AC2, AC3) -->
            <section style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; margin-bottom: 24px;">
                <!-- AC1 Card -->
                <div class="gl-card" style="margin-bottom: 0;">
                    <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 12px;">
                        <div style="width: 40px; height: 40px; border-radius: var(--gl-radius-md); background: #fff7ed; color: #c2410c; display: flex; align-items: center; justify-content: center;">
                            <span class="material-symbols-outlined" aria-hidden="true">rule</span>
                        </div>
                        <h3 style="margin: 0; font-size: 1rem; font-weight: 700; color: var(--gl-neutral-900);">1. Tiêu Chí So Khớp (AC1)</h3>
                    </div>
                    <p style="font-size: 0.8125rem; color: var(--gl-neutral-600); line-height: 1.5; margin: 0 0 14px 0;">
                        Tự động chuẩn hóa và đối chiếu trên 3 tín hiệu chính xác:
                    </p>
                    <div style="display: flex; flex-direction: column; gap: 8px;">
                        <span class="gl-badge-reason gl-badge-reason-email" style="width: fit-content;">
                            <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">mail</span>
                            <span>Trùng Email liên hệ</span>
                        </span>
                        <span class="gl-badge-reason gl-badge-reason-phone" style="width: fit-content;">
                            <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">call</span>
                            <span>Trùng Số điện thoại (E.164)</span>
                        </span>
                        <span class="gl-badge-reason gl-badge-reason-company" style="width: fit-content;">
                            <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">domain</span>
                            <span>Trùng Tên công ty / Tổ chức</span>
                        </span>
                    </div>
                </div>

                <!-- AC2 Card -->
                <div class="gl-card" style="margin-bottom: 0;">
                    <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 12px;">
                        <div style="width: 40px; height: 40px; border-radius: var(--gl-radius-md); background: #eff6ff; color: #1d4ed8; display: flex; align-items: center; justify-content: center;">
                            <span class="material-symbols-outlined" aria-hidden="true">link</span>
                        </div>
                        <h3 style="margin: 0; font-size: 1rem; font-weight: 700; color: var(--gl-neutral-900);">2. Gắn Khách Hàng (AC2)</h3>
                    </div>
                    <p style="font-size: 0.8125rem; color: var(--gl-neutral-600); line-height: 1.5; margin: 0 0 14px 0;">
                        Khi Lead mới phát sinh trùng với hồ sơ Khách hàng đã tồn tại trong CRM, hệ thống tự động gợi ý gắn thẳng Lead vào Khách hàng đó, giúp quy tụ toàn bộ điểm chạm kinh doanh.
                    </p>
                    <div style="font-size: 0.75rem; color: var(--gl-neutral-500); background: var(--gl-neutral-50); padding: 8px 12px; border-radius: var(--gl-radius-sm); border: 1px solid var(--gl-neutral-200);">
                        <span class="material-symbols-outlined" style="font-size: 14px; vertical-align: middle; color: var(--gl-success);" aria-hidden="true">verified</span>
                        <span>Bảo toàn phân cấp Khách hàng & Người liên hệ</span>
                    </div>
                </div>

                <!-- AC3 Card -->
                <div class="gl-card" style="margin-bottom: 0;">
                    <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 12px;">
                        <div style="width: 40px; height: 40px; border-radius: var(--gl-radius-md); background: #ecfdf5; color: #065f46; display: flex; align-items: center; justify-content: center;">
                            <span class="material-symbols-outlined" aria-hidden="true">history</span>
                        </div>
                        <h3 style="margin: 0; font-size: 1rem; font-weight: 700; color: var(--gl-neutral-900);">3. Bảo Toàn Lịch Sử (AC3)</h3>
                    </div>
                    <p style="font-size: 0.8125rem; color: var(--gl-neutral-600); line-height: 1.5; margin: 0 0 14px 0;">
                        Khi gộp 2 bản ghi Lead, toàn bộ lịch sử tương tác, nhật ký cuộc gọi, công việc nhắc hẹn và các cơ hội đang mở đều được chuyển giao nguyên vẹn sang Lead giữ lại trong cùng 1 Transaction an toàn.
                    </p>
                    <div style="font-size: 0.75rem; color: var(--gl-neutral-500); background: var(--gl-neutral-50); padding: 8px 12px; border-radius: var(--gl-radius-sm); border: 1px solid var(--gl-neutral-200);">
                        <span class="material-symbols-outlined" style="font-size: 14px; vertical-align: middle; color: var(--gl-primary);" aria-hidden="true">shield</span>
                        <span>Không làm mất dữ liệu của bất kỳ nhân viên nào</span>
                    </div>
                </div>
            </section>

        </div>
    </main>

    <!-- JavaScript hỗ trợ -->
    <script src="${pageContext.request.contextPath}/assets/js/lead/gop-lead.js"></script>
</body>
</html>
