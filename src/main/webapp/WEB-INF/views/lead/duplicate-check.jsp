<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Kết quả phát hiện trùng lặp Lead và gợi ý liên kết Khách hàng - CRM Bán Hàng Nhóm 10">
    <title>Cảnh Báo Trùng Cho Lead #${lead.id} - CRM Bán Hàng</title>

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
                    <li><a href="${pageContext.request.contextPath}/lead/duplicates" class="gl-breadcrumb-link">Lead & Phân bổ</a></li>
                    <span class="gl-breadcrumb-sep" aria-hidden="true">/</span>
                    <li><a href="${pageContext.request.contextPath}/lead/duplicates" class="gl-breadcrumb-link">Cảnh báo trùng</a></li>
                    <span class="gl-breadcrumb-sep" aria-hidden="true">/</span>
                    <li class="gl-breadcrumb-current" aria-current="page">Lead #${lead.id} (<c:out value="${lead.hoTen}" />)</li>
                </ol>
                <div style="display: flex; gap: 8px;">
                    <a href="${pageContext.request.contextPath}/lead/duplicates" class="gl-btn gl-btn-outline gl-btn-sm" id="btnQuayLaiDanhSach">
                        <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span>
                        <span>Trung tâm cảnh báo</span>
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

            <!-- 3. Header Card: Tóm tắt thông tin Lead đang được rà soát trùng -->
            <header class="gl-header-card">
                <div class="gl-header-title">
                    <div style="display: flex; align-items: center; gap: 10px; flex-wrap: wrap;">
                        <span style="font-family: monospace; font-size: 0.8125rem; background: var(--gl-neutral-100); padding: 2px 8px; border-radius: var(--gl-radius-sm);">
                            Mã: <c:out value="${lead.maLead != null ? lead.maLead : lead.id}" />
                        </span>
                        <span class="gl-badge-reason" style="background: var(--gl-primary-light); color: var(--gl-primary); border: 1px solid var(--gl-primary-border);">
                            <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">person</span>
                            <span>Trạng thái: <c:out value="${lead.trangThai != null ? lead.trangThai.tenHienThi : 'Mới'}" /></span>
                        </span>
                    </div>
                    <h1>
                        <span class="material-symbols-outlined" style="color: var(--gl-primary); font-size: 32px;" aria-hidden="true">person_search</span>
                        <span>Rà Soát Trùng: <c:out value="${lead.hoTen}" /></span>
                    </h1>
                    <p>Hệ thống tự động quét và đối chiếu theo 3 tiêu chí: Email, Số điện thoại và Tên công ty (Story S4-04).</p>
                </div>
            </header>

            <!-- 4. Chi tiết thuộc tính nhận diện của Lead đang kiểm tra -->
            <section class="gl-card">
                <h2 class="gl-section-title">
                    <span style="display: flex; align-items: center; gap: 8px;">
                        <span class="material-symbols-outlined" style="color: var(--gl-primary);" aria-hidden="true">badge</span>
                        <span>Hồ Sơ Nhận Diện Lead Gốc</span>
                    </span>
                </h2>

                <div class="gl-info-grid">
                    <div class="gl-info-item">
                        <span class="gl-info-label">Họ và tên</span>
                        <span class="gl-info-value"><c:out value="${lead.hoTen}" /></span>
                    </div>

                    <div class="gl-info-item">
                        <span class="gl-info-label">Email liên hệ</span>
                        <span class="gl-info-value">
                            <c:choose>
                                <c:when test="${not empty lead.email}">
                                    <span class="material-symbols-outlined" style="font-size: 14px; vertical-align: middle; color: var(--gl-neutral-400);" aria-hidden="true">mail</span>
                                    <c:out value="${lead.email}" />
                                </c:when>
                                <c:otherwise><span style="color: var(--gl-neutral-400); font-style: italic;">Chưa có</span></c:otherwise>
                            </c:choose>
                        </span>
                    </div>

                    <div class="gl-info-item">
                        <span class="gl-info-label">Số điện thoại</span>
                        <span class="gl-info-value">
                            <c:choose>
                                <c:when test="${not empty lead.soDienThoai}">
                                    <span class="material-symbols-outlined" style="font-size: 14px; vertical-align: middle; color: var(--gl-neutral-400);" aria-hidden="true">call</span>
                                    <c:out value="${lead.soDienThoai}" />
                                </c:when>
                                <c:otherwise><span style="color: var(--gl-neutral-400); font-style: italic;">Chưa có</span></c:otherwise>
                            </c:choose>
                        </span>
                    </div>

                    <div class="gl-info-item">
                        <span class="gl-info-label">Tên công ty / Tổ chức</span>
                        <span class="gl-info-value">
                            <c:choose>
                                <c:when test="${not empty lead.tenCongTy}">
                                    <span class="material-symbols-outlined" style="font-size: 14px; vertical-align: middle; color: var(--gl-neutral-400);" aria-hidden="true">domain</span>
                                    <c:out value="${lead.tenCongTy}" />
                                </c:when>
                                <c:otherwise><span style="color: var(--gl-neutral-400); font-style: italic;">Chưa có</span></c:otherwise>
                            </c:choose>
                        </span>
                    </div>

                    <div class="gl-info-item">
                        <span class="gl-info-label">Nguồn Lead</span>
                        <span class="gl-info-value"><c:out value="${lead.tenNguonLead != null ? lead.tenNguonLead : 'Tự nhiên / Trực tiếp'}" /></span>
                    </div>

                    <div class="gl-info-item">
                        <span class="gl-info-label">Người phụ trách</span>
                        <span class="gl-info-value"><c:out value="${lead.tenNguoiPhuTrach != null ? lead.tenNguoiPhuTrach : 'Chưa phân bổ'}" /></span>
                    </div>

                    <div class="gl-info-item">
                        <span class="gl-info-label">Điểm tiềm năng</span>
                        <span class="gl-info-value" style="color: var(--gl-primary);">${lead.diem} điểm</span>
                    </div>

                    <div class="gl-info-item">
                        <span class="gl-info-label">Chiến dịch marketing</span>
                        <span class="gl-info-value"><c:out value="${lead.tenChienDich != null ? lead.tenChienDich : 'Không gắn chiến dịch'}" /></span>
                    </div>
                </div>
            </section>

            <!-- 5. PHẦN 1: LEAD TRÙNG PHÁT HIỆN ĐƯỢC (AC1) -->
            <section class="gl-card">
                <h2 class="gl-section-title">
                    <span style="display: flex; align-items: center; gap: 8px;">
                        <span class="material-symbols-outlined" style="color: var(--gl-warning);" aria-hidden="true">warning</span>
                        <span>Lead Trùng Lặp Phát Hiện Được (${leadCandidates != null ? leadCandidates.size() : 0} bản ghi)</span>
                    </span>
                </h2>
                <p style="font-size: 0.875rem; color: var(--gl-neutral-500); margin: 0 0 16px 0;">
                    Các Lead này có thông tin liên hệ hoặc tên công ty trùng khớp. Hãy tiến hành xem trước và gộp vào một bản ghi chính thức để không để 2 nhân viên cùng gọi một khách hàng (AC1, AC3):
                </p>

                <c:choose>
                    <c:when test="${not empty leadCandidates}">
                        <div class="gl-candidates-grid">
                            <c:forEach items="${leadCandidates}" var="cand">
                                <article class="gl-candidate-card">
                                    <div class="gl-candidate-header">
                                        <div>
                                            <div class="gl-candidate-name">
                                                <c:choose>
                                                    <c:when test="${not empty cand.displayName}">
                                                        <c:out value="${cand.displayName}" />
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:out value="${cand.maskedLabel}" />
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                            <div class="gl-candidate-code">Lead ID: #${cand.recordId} ${cand.maBanGhi != null ? ' - ' : ''}${cand.maBanGhi}</div>
                                        </div>

                                        <span class="gl-badge-reason" style="background: var(--gl-neutral-100); color: var(--gl-neutral-700);">
                                            <c:out value="${cand.trangThai != null ? cand.trangThai : 'MỚI'}" />
                                        </span>
                                    </div>

                                    <div class="gl-candidate-body">
                                        <!-- Email -->
                                        <div class="gl-candidate-row">
                                            <span class="material-symbols-outlined" aria-hidden="true">mail</span>
                                            <span>
                                                <c:choose>
                                                    <c:when test="${not empty cand.email}"><c:out value="${cand.email}" /></c:when>
                                                    <c:when test="${not empty cand.maskedContact}"><c:out value="${cand.maskedContact}" /></c:when>
                                                    <c:otherwise><span style="color: var(--gl-neutral-400); font-style: italic;">Không có email</span></c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>

                                        <!-- Số điện thoại -->
                                        <div class="gl-candidate-row">
                                            <span class="material-symbols-outlined" aria-hidden="true">call</span>
                                            <span>
                                                <c:choose>
                                                    <c:when test="${not empty cand.soDienThoai}"><c:out value="${cand.soDienThoai}" /></c:when>
                                                    <c:otherwise><span style="color: var(--gl-neutral-400); font-style: italic;">Không có SĐT</span></c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>

                                        <!-- Công ty -->
                                        <div class="gl-candidate-row">
                                            <span class="material-symbols-outlined" aria-hidden="true">domain</span>
                                            <span>
                                                <c:choose>
                                                    <c:when test="${not empty cand.tenCongTy}"><c:out value="${cand.tenCongTy}" /></c:when>
                                                    <c:otherwise><span style="color: var(--gl-neutral-400); font-style: italic;">Không có tên công ty</span></c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>

                                        <!-- Người phụ trách -->
                                        <div class="gl-candidate-row">
                                            <span class="material-symbols-outlined" aria-hidden="true">account_circle</span>
                                            <span>Phụ trách: <c:out value="${cand.tenNguoiPhuTrach != null ? cand.tenNguoiPhuTrach : 'Chưa phân bổ'}" /></span>
                                        </div>

                                        <!-- Lý do trùng khớp -->
                                        <div class="gl-candidate-reasons">
                                            <c:forEach items="${cand.matchReasons}" var="r">
                                                <c:choose>
                                                    <c:when test="${r == 'EMAIL'}">
                                                        <span class="gl-badge-reason gl-badge-reason-email">
                                                            <span class="material-symbols-outlined" style="font-size: 13px;" aria-hidden="true">mail</span>
                                                            <span>Trùng Email</span>
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${r == 'PHONE'}">
                                                        <span class="gl-badge-reason gl-badge-reason-phone">
                                                            <span class="material-symbols-outlined" style="font-size: 13px;" aria-hidden="true">call</span>
                                                            <span>Trùng SĐT</span>
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${r == 'COMPANY'}">
                                                        <span class="gl-badge-reason gl-badge-reason-company">
                                                            <span class="material-symbols-outlined" style="font-size: 13px;" aria-hidden="true">domain</span>
                                                            <span>Trùng Công ty</span>
                                                        </span>
                                                    </c:when>
                                                </c:choose>
                                            </c:forEach>
                                        </div>
                                    </div>

                                    <div class="gl-candidate-footer">
                                        <c:if test="${permissions.canMerge}">
                                            <!-- Nút so sánh & gộp: Gộp ứng viên vào Lead gốc đang xem -->
                                            <a href="${pageContext.request.contextPath}/lead/duplicate?action=preview&sourceLeadId=${cand.recordId}&targetLeadId=${lead.id}" 
                                               class="gl-btn gl-btn-primary gl-btn-sm" 
                                               title="Gộp bản ghi này vào Lead hiện tại">
                                                <span class="material-symbols-outlined" aria-hidden="true">merge_type</span>
                                                <span>So sánh & Gộp vào Lead này</span>
                                            </a>
                                        </c:if>
                                    </div>
                                </article>
                            </c:forEach>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="gl-empty-card">
                            <span class="material-symbols-outlined">check_circle</span>
                            <div><strong>Không phát hiện Lead nào trùng lặp!</strong></div>
                            <div>Cơ sở dữ liệu không ghi nhận Lead nào trùng Email, Số điện thoại hoặc Tên công ty với Lead này.</div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>

            <!-- 6. PHẦN 2: KHÁCH HÀNG TRÙNG KHỚP ĐƯỢC GỢI Ý (AC2) -->
            <section class="gl-card">
                <h2 class="gl-section-title">
                    <span style="display: flex; align-items: center; gap: 8px;">
                        <span class="material-symbols-outlined" style="color: var(--gl-primary);" aria-hidden="true">corporate_fare</span>
                        <span>Khách Hàng Đã Có Trong Hệ Thống Được Gợi Ý Gắn (AC2)</span>
                    </span>
                </h2>
                <p style="font-size: 0.875rem; color: var(--gl-neutral-500); margin: 0 0 16px 0;">
                    Lead này có dấu hiệu thuộc về một Khách hàng doanh nghiệp đã tồn tại. Bạn có thể gắn thẳng Lead vào Khách hàng đó để tiếp tục chăm sóc tập trung:
                </p>

                <c:choose>
                    <c:when test="${not empty customerCandidates}">
                        <div class="gl-candidates-grid">
                            <c:forEach items="${customerCandidates}" var="cust">
                                <article class="gl-candidate-card" style="border-left-color: var(--gl-success);">
                                    <div class="gl-candidate-header">
                                        <div>
                                            <div class="gl-candidate-name">
                                                <c:choose>
                                                    <c:when test="${not empty cust.displayName}"><c:out value="${cust.displayName}" /></c:when>
                                                    <c:otherwise><c:out value="${cust.maskedLabel}" /></c:otherwise>
                                                </c:choose>
                                            </div>
                                            <div class="gl-candidate-code">Khách hàng ID: #${cust.recordId} ${cust.maBanGhi != null ? ' - ' : ''}${cust.maBanGhi}</div>
                                        </div>
                                        <span class="gl-badge-reason" style="background: var(--gl-success-light); color: var(--gl-success-text); border: 1px solid var(--gl-success-border);">
                                            Khách hàng
                                        </span>
                                    </div>

                                    <div class="gl-candidate-body">
                                        <div class="gl-candidate-row">
                                            <span class="material-symbols-outlined" aria-hidden="true">domain</span>
                                            <span>
                                                <c:choose>
                                                    <c:when test="${not empty cust.tenCongTy}"><c:out value="${cust.tenCongTy}" /></c:when>
                                                    <c:otherwise><c:out value="${cust.displayName}" /></c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>

                                        <div class="gl-candidate-row">
                                            <span class="material-symbols-outlined" aria-hidden="true">call</span>
                                            <span>
                                                <c:choose>
                                                    <c:when test="${not empty cust.soDienThoai}"><c:out value="${cust.soDienThoai}" /></c:when>
                                                    <c:otherwise><span style="color: var(--gl-neutral-400); font-style: italic;">Không có SĐT</span></c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>

                                        <div class="gl-candidate-row">
                                            <span class="material-symbols-outlined" aria-hidden="true">mail</span>
                                            <span>
                                                <c:choose>
                                                    <c:when test="${not empty cust.email}"><c:out value="${cust.email}" /></c:when>
                                                    <c:otherwise><span style="color: var(--gl-neutral-400); font-style: italic;">Không có email</span></c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>

                                        <!-- Lý do trùng khớp -->
                                        <div class="gl-candidate-reasons">
                                            <c:forEach items="${cust.matchReasons}" var="r">
                                                <c:choose>
                                                    <c:when test="${r == 'EMAIL'}">
                                                        <span class="gl-badge-reason gl-badge-reason-email">
                                                            <span class="material-symbols-outlined" style="font-size: 13px;" aria-hidden="true">mail</span>
                                                            <span>Trùng Email</span>
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${r == 'PHONE'}">
                                                        <span class="gl-badge-reason gl-badge-reason-phone">
                                                            <span class="material-symbols-outlined" style="font-size: 13px;" aria-hidden="true">call</span>
                                                            <span>Trùng SĐT</span>
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${r == 'COMPANY'}">
                                                        <span class="gl-badge-reason gl-badge-reason-company">
                                                            <span class="material-symbols-outlined" style="font-size: 13px;" aria-hidden="true">domain</span>
                                                            <span>Trùng Tên công ty</span>
                                                        </span>
                                                    </c:when>
                                                </c:choose>
                                            </c:forEach>
                                        </div>
                                    </div>

                                    <div class="gl-candidate-footer">
                                        <c:if test="${permissions.canLinkCustomer}">
                                            <!-- Form POST gắn khách hàng (AC2) -->
                                            <form action="${pageContext.request.contextPath}/lead/duplicate?action=linkCustomer" method="POST" style="margin: 0;">
                                                <input type="hidden" name="leadId" value="${lead.id}">
                                                <input type="hidden" name="customerId" value="${cust.recordId}">
                                                <button type="submit" 
                                                        class="gl-btn gl-btn-success gl-btn-sm" 
                                                        onclick="return confirm('Bạn có chắc chắn muốn liên kết Lead này vào Khách hàng [${cust.displayName}] không?');">
                                                    <span class="material-symbols-outlined" aria-hidden="true">link</span>
                                                    <span>Gắn vào Khách hàng này</span>
                                                </button>
                                            </form>
                                        </c:if>
                                    </div>
                                </article>
                            </c:forEach>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="gl-empty-card">
                            <span class="material-symbols-outlined">corporate_fare</span>
                            <div><strong>Không có Khách hàng nào trùng khớp!</strong></div>
                            <div>Hệ thống không tìm thấy hồ sơ Khách hàng nào trùng thông tin với Lead này.</div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>

        </div>
    </main>

    <!-- JavaScript hỗ trợ -->
    <script src="${pageContext.request.contextPath}/assets/js/lead/gop-lead.js"></script>
</body>
</html>
