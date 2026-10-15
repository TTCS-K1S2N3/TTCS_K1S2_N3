<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Khai báo chiến dịch marketing mới với ngân sách, thời gian chạy và kênh triển khai - CRM Bán Hàng Nhóm 10">
    <title>Khai Báo Chiến Dịch Marketing - CRM Bán Hàng</title>

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
                    <li class="cd-breadcrumb-current" aria-current="page">Khai báo chiến dịch mới</li>
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
                    <h1>
                        <span class="material-symbols-outlined" style="color: var(--cd-primary); font-size: 32px;" aria-hidden="true">add_chart</span>
                        <span>Khai Báo Chiến Dịch Marketing Mới</span>
                    </h1>
                    <p>Thiết lập thông tin ngân sách dự toán, thời gian vận hành và kênh truyền thông để gắn kết tự động với các lead thu thập được (AC1).</p>
                </div>
            </header>

            <!-- Thông báo lỗi chung nếu có -->
            <c:if test="${not empty fieldErrors['general']}">
                <div class="cd-alert cd-alert-danger" role="alert">
                    <span class="material-symbols-outlined" aria-hidden="true">error</span>
                    <span><c:out value="${fieldErrors['general']}" /></span>
                </div>
            </c:if>

            <!-- 3. Form Layout: Form nhập liệu 2 cột (Desktop) và Sidebar hướng dẫn -->
            <div class="cd-form-layout">

                <!-- Form Card chính -->
                <div class="cd-form-card">
                    <h2 class="cd-form-section-title">
                        <span class="material-symbols-outlined" style="color: var(--cd-primary);" aria-hidden="true">edit_note</span>
                        <span>Thông Tin Chiến Dịch (AC1)</span>
                    </h2>

                    <form action="${pageContext.request.contextPath}/marketing/campaign?action=create" method="POST" id="campaignForm" novalidate>
                        
                        <!-- 1. Tên chiến dịch -->
                        <div class="cd-form-group">
                            <label for="name" class="cd-label">
                                Tên chiến dịch marketing <span class="cd-required-mark" aria-hidden="true">*</span>
                            </label>
                            <input type="text" 
                                   id="name" 
                                   name="name" 
                                   class="cd-input ${not empty fieldErrors['name'] ? 'is-invalid' : ''}" 
                                   value="<c:out value="${campaignForm.name != null ? campaignForm.name : ''}" />" 
                                   placeholder="Ví dụ: Chiến dịch Ra mắt Sản phẩm Mới Q4 2026" 
                                   maxlength="200" 
                                   required 
                                   aria-required="true"
                                   <c:if test="${not empty fieldErrors['name']}">aria-invalid="true" aria-describedby="nameError"</c:if>>
                            <c:if test="${not empty fieldErrors['name']}">
                                <div class="cd-field-error" id="nameError" role="alert">
                                    <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">error</span>
                                    <span><c:out value="${fieldErrors['name']}" /></span>
                                </div>
                            </c:if>
                            <div class="cd-input-hint">Đặt tên rõ ràng, kèm mốc thời gian hoặc mục tiêu sản phẩm để dễ nhận diện khi phân bổ lead.</div>
                        </div>

                        <!-- 2. Kênh triển khai & Ngân sách dự toán (2 cột) -->
                        <div class="cd-form-row-2">
                            <!-- Kênh triển khai -->
                            <div class="cd-form-group">
                                <label for="channelCode" class="cd-label">
                                    Kênh triển khai <span class="cd-required-mark" aria-hidden="true">*</span>
                                </label>
                                <select id="channelCode" 
                                        name="channelCode" 
                                        class="cd-select ${not empty fieldErrors['channelCode'] ? 'is-invalid' : ''}" 
                                        required 
                                        aria-required="true"
                                        <c:if test="${not empty fieldErrors['channelCode']}">aria-invalid="true" aria-describedby="channelError"</c:if>>
                                    <option value="">-- Chọn kênh truyền thông --</option>
                                    <c:forEach items="${channelOptions}" var="opt">
                                        <option value="${opt.code}" 
                                            <c:if test="${campaignForm.channelCode == opt.code}">selected="selected"</c:if>>
                                            <c:out value="${opt.label}" /> (${opt.code})
                                        </option>
                                    </c:forEach>
                                </select>
                                <c:if test="${not empty fieldErrors['channelCode']}">
                                    <div class="cd-field-error" id="channelError" role="alert">
                                        <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">error</span>
                                        <span><c:out value="${fieldErrors['channelCode']}" /></span>
                                    </div>
                                </c:if>
                            </div>

                            <!-- Ngân sách dự toán -->
                            <div class="cd-form-group">
                                <label for="budgetAmount" class="cd-label">
                                    Ngân sách dự toán (VNĐ) <span class="cd-required-mark" aria-hidden="true">*</span>
                                </label>
                                <c:set var="budgetVal" value="" />
                                <c:choose>
                                    <c:when test="${not empty campaignForm.budgetAmountStr}">
                                        <c:set var="budgetVal" value="${campaignForm.budgetAmountStr}" />
                                    </c:when>
                                    <c:when test="${campaignForm.budgetAmount != null}">
                                        <c:set var="budgetVal" value="${campaignForm.budgetAmount}" />
                                    </c:when>
                                </c:choose>
                                <input type="text" 
                                       id="budgetAmount" 
                                       name="budgetAmount" 
                                       class="cd-input ${not empty fieldErrors['budgetAmount'] ? 'is-invalid' : ''}" 
                                       value="<c:out value="${budgetVal}" />" 
                                       placeholder="Ví dụ: 15000000 hoặc 15,000,000" 
                                       required 
                                       aria-required="true"
                                       <c:if test="${not empty fieldErrors['budgetAmount']}">aria-invalid="true" aria-describedby="budgetError"</c:if>>
                                <c:if test="${not empty fieldErrors['budgetAmount']}">
                                    <div class="cd-field-error" id="budgetError" role="alert">
                                        <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">error</span>
                                        <span><c:out value="${fieldErrors['budgetAmount']}" /></span>
                                    </div>
                                </c:if>
                                <div class="cd-input-hint">Nhập số tiền không âm (>= 0), hệ thống tự động quy về đơn vị VNĐ.</div>
                            </div>
                        </div>

                        <!-- 3. Thời gian chạy: Ngày bắt đầu & Ngày kết thúc (2 cột) -->
                        <div class="cd-form-row-2">
                            <!-- Ngày bắt đầu -->
                            <div class="cd-form-group">
                                <label for="startDate" class="cd-label">
                                    Ngày bắt đầu <span class="cd-required-mark" aria-hidden="true">*</span>
                                </label>
                                <c:set var="startVal" value="" />
                                <c:choose>
                                    <c:when test="${not empty campaignForm.startDateStr}">
                                        <c:set var="startVal" value="${campaignForm.startDateStr}" />
                                    </c:when>
                                    <c:when test="${campaignForm.startDate != null}">
                                        <c:set var="startVal" value="${campaignForm.startDate}" />
                                    </c:when>
                                </c:choose>
                                <input type="date" 
                                       id="startDate" 
                                       name="startDate" 
                                       class="cd-input ${not empty fieldErrors['startDate'] ? 'is-invalid' : ''}" 
                                       value="<c:out value="${startVal}" />" 
                                       required 
                                       aria-required="true"
                                       <c:if test="${not empty fieldErrors['startDate']}">aria-invalid="true" aria-describedby="startDateError"</c:if>>
                                <c:if test="${not empty fieldErrors['startDate']}">
                                    <div class="cd-field-error" id="startDateError" role="alert">
                                        <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">error</span>
                                        <span><c:out value="${fieldErrors['startDate']}" /></span>
                                    </div>
                                </c:if>
                            </div>

                            <!-- Ngày kết thúc -->
                            <div class="cd-form-group">
                                <label for="endDate" class="cd-label">
                                    Ngày kết thúc <span class="cd-required-mark" aria-hidden="true">*</span>
                                </label>
                                <c:set var="endVal" value="" />
                                <c:choose>
                                    <c:when test="${not empty campaignForm.endDateStr}">
                                        <c:set var="endVal" value="${campaignForm.endDateStr}" />
                                    </c:when>
                                    <c:when test="${campaignForm.endDate != null}">
                                        <c:set var="endVal" value="${campaignForm.endDate}" />
                                    </c:when>
                                </c:choose>
                                <input type="date" 
                                       id="endDate" 
                                       name="endDate" 
                                       class="cd-input ${not empty fieldErrors['endDate'] ? 'is-invalid' : ''}" 
                                       value="<c:out value="${endVal}" />" 
                                       required 
                                       aria-required="true"
                                       <c:if test="${not empty fieldErrors['endDate']}">aria-invalid="true" aria-describedby="endDateError"</c:if>>
                                <c:if test="${not empty fieldErrors['endDate']}">
                                    <div class="cd-field-error" id="endDateError" role="alert">
                                        <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">error</span>
                                        <span><c:out value="${fieldErrors['endDate']}" /></span>
                                    </div>
                                </c:if>
                                <!-- Vùng hiển thị lỗi logic ngày kết thúc < ngày bắt đầu bằng JS client-side -->
                                <div class="cd-field-error" id="dateLogicError" style="display: none;" role="alert"></div>
                            </div>
                        </div>

                        <!-- 4. Mô tả / Ghi chú mục tiêu -->
                        <div class="cd-form-group">
                            <label for="moTa" class="cd-label">Mô tả mục tiêu chiến dịch</label>
                            <textarea id="moTa" 
                                      name="moTa" 
                                      rows="3" 
                                      class="cd-textarea" 
                                      placeholder="Ghi chú thêm về mục tiêu số lượng lead, thông điệp truyền thông hoặc đối tượng khách hàng mục tiêu..."><c:out value="${campaignForm.moTa != null ? campaignForm.moTa : ''}" /></textarea>
                        </div>

                        <!-- Nút lưu & hủy bỏ -->
                        <div class="cd-form-actions">
                            <button type="submit" class="cd-btn cd-btn-primary" id="btnSubmitCampaign">
                                <span class="material-symbols-outlined" aria-hidden="true">save</span>
                                <span>Lưu chiến dịch</span>
                            </button>
                            <a href="${pageContext.request.contextPath}/marketing/campaigns" class="cd-btn cd-btn-outline" id="btnHuyBo">
                                <span class="material-symbols-outlined" aria-hidden="true">cancel</span>
                                <span>Hủy bỏ</span>
                            </a>
                        </div>
                    </form>
                </div>

                <!-- Sidebar Hướng Dẫn & Quy Tắc Khai Báo (AC1 & AC2) -->
                <aside class="cd-sidebar">
                    <div class="cd-sidebar-card">
                        <h3 class="cd-sidebar-title">
                            <span class="material-symbols-outlined" style="color: var(--cd-primary);" aria-hidden="true">tips_and_updates</span>
                            <span>Quy Chuẩn Khai Báo (AC1)</span>
                        </h3>
                        <ul class="cd-guide-list">
                            <li class="cd-guide-item">
                                <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                                <div><strong>Tên nhận diện:</strong> Giúp nhân viên Marketing và Sales dễ dàng phân biệt chiến dịch khi gắn lead nguồn.</div>
                            </li>
                            <li class="cd-guide-item">
                                <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                                <div><strong>Kênh truyền thông:</strong> Chọn đúng kênh (Sự kiện, Mạng xã hội, Email...) để báo cáo thống kê chính xác.</div>
                            </li>
                            <li class="cd-guide-item">
                                <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                                <div><strong>Ngân sách:</strong> Nhập số tiền dự toán để hệ thống tính toán ROI và chi phí thu hút lead (CPL).</div>
                            </li>
                            <li class="cd-guide-item">
                                <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                                <div><strong>Thời gian chạy:</strong> Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc.</div>
                            </li>
                        </ul>
                    </div>

                    <div class="cd-sidebar-card">
                        <h3 class="cd-sidebar-title">
                            <span class="material-symbols-outlined" style="color: var(--cd-purple);" aria-hidden="true">account_tree</span>
                            <span>Theo Dấu Nguồn (AC2)</span>
                        </h3>
                        <p style="font-size: 0.8125rem; color: var(--cd-neutral-600); line-height: 1.5; margin: 0;">
                            Sau khi tạo, mã chiến dịch sẽ sẵn sàng để liên kết với Biểu mẫu nhúng website (S4-01), Nhập Lead từ Excel (S4-02) và tự động kế thừa khi chuyển đổi thành Cơ hội kinh doanh (S4-08).
                        </p>
                    </div>
                </aside>

            </div>

        </div>
    </main>

    <!-- JavaScript hỗ trợ tương tác và validate form -->
    <script src="${pageContext.request.contextPath}/assets/js/lead/chien-dich.js"></script>
</body>
</html>
