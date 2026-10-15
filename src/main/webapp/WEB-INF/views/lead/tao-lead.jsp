<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tạo Khách Hàng Tiềm Năng - CRM Bán Hàng</title>
    <!-- CSS Hệ thống chung -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <!-- CSS Chuyên biệt Module Lead S4-02 (Modern SaaS Design) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lead/lead.css?v=2.1">
</head>
<body class="crm-body">

    <!-- Navigation chung hệ thống S1-06 -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <div class="lead-container">

            <!-- 1. Breadcrumb tinh giản -->
            <nav class="lead-breadcrumb" aria-label="Đường dẫn điều hướng">
                <ol class="lead-breadcrumb-nav">
                    <li><a href="${pageContext.request.contextPath}/dieu-huong" class="lead-breadcrumb-link">Trang chủ</a></li>
                    <span class="lead-breadcrumb-sep">/</span>
                    <li><a href="${pageContext.request.contextPath}/lead" class="lead-breadcrumb-link">Lead & Phân bổ</a></li>
                    <span class="lead-breadcrumb-sep">/</span>
                    <li class="lead-breadcrumb-current">Tạo Lead mới</li>
                </ol>
            </nav>

            <!-- 2. Header Bar thanh lịch -->
            <div class="lead-page-header">
                <div class="lead-title-wrap">
                    <h1>
                        <span>Tạo Mới Khách Hàng Tiềm Năng</span>
                        <span class="lead-header-pill">Lead mới</span>
                    </h1>
                    <p>Nhập thông tin khách hàng từ sự kiện, hội thảo hoặc danh thiếp tiếp xúc ban đầu.</p>
                </div>
                <div class="lead-header-actions">
                    <a href="${pageContext.request.contextPath}/lead" class="lead-btn lead-btn-outline" id="btnHuyTop">
                        <span>Hủy bỏ</span>
                    </a>
                    <button type="button" class="lead-btn lead-btn-outline" id="btnLuuVaTiepTuc">
                        <span>Lưu & Thêm tiếp</span>
                    </button>
                    <button type="button" class="lead-btn lead-btn-primary" onclick="document.getElementById('formTaoLead').requestSubmit()">
                        <span class="material-symbols-outlined" style="font-size: 18px;">save</span>
                        <span>Lưu thông tin</span>
                    </button>
                </div>
            </div>

            <!-- Thông báo Lỗi / Thành công nếu có -->
            <c:if test="${not empty thongBaoLoi}">
                <div class="lead-pill-badge lead-pill-error" style="width: 100%; padding: 12px 16px; margin-bottom: 20px; border-radius: 8px;" role="alert">
                    <span class="material-symbols-outlined" style="font-size: 18px;">error</span>
                    <span><c:out value="${thongBaoLoi}" /></span>
                </div>
            </c:if>
            <c:if test="${not empty thongBaoThanhCong}">
                <div class="lead-pill-badge lead-pill-valid" style="width: 100%; padding: 12px 16px; margin-bottom: 20px; border-radius: 8px;" role="alert">
                    <span class="material-symbols-outlined" style="font-size: 18px;">check_circle</span>
                    <span><c:out value="${thongBaoThanhCong}" /></span>
                </div>
            </c:if>

            <!-- 3. Bố cục 2 Cột Hiện đại (Split Layout) -->
            <div class="lead-split-layout">

                <!-- CỘT CHÍNH (TRÁI): FORM NHẬP LIỆU THANH THOÁT -->
                <div class="lead-main-card">
                    <form action="${pageContext.request.contextPath}/lead/tao-moi" method="POST" id="formTaoLead" novalidate>
                        <input type="hidden" name="action" value="tao-thu-cong">

                        <!-- Khối 1: Người liên hệ & Công ty -->
                        <div class="lead-form-section">
                            <div class="lead-section-heading">
                                <span class="lead-section-title">
                                    <span class="material-symbols-outlined">person</span>
                                    <span>Thông tin liên hệ & Doanh nghiệp</span>
                                </span>
                            </div>

                            <div class="lead-grid-2">
                                <!-- Họ tên (Bắt buộc) -->
                                <div class="lead-form-group">
                                    <label class="lead-label" for="ho_ten">
                                        <span>Họ và tên khách hàng <span class="required-dot">*</span></span>
                                    </label>
                                    <div class="lead-input-wrap">
                                        <span class="material-symbols-outlined lead-input-icon">badge</span>
                                        <input type="text"
                                               id="ho_ten"
                                               name="ho_ten"
                                               class="lead-input ${not empty errors.hoTen ? 'is-invalid' : ''}"
                                               placeholder="Ví dụ: Nguyễn Văn An"
                                               maxlength="150"
                                               required
                                               value="<c:out value="${formData != null ? formData.hoTen : param.ho_ten}" />"
                                               autocomplete="name" />
                                    </div>
                                    <c:if test="${not empty errors.hoTen}">
                                        <div class="lead-error-msg"><c:out value="${errors.hoTen}" /></div>
                                    </c:if>
                                </div>

                                <!-- Tên công ty -->
                                <div class="lead-form-group">
                                    <label class="lead-label" for="ten_cong_ty">
                                        <span>Tên công ty / Doanh nghiệp</span>
                                    </label>
                                    <div class="lead-input-wrap">
                                        <span class="material-symbols-outlined lead-input-icon">business</span>
                                        <input type="text"
                                               id="ten_cong_ty"
                                               name="ten_cong_ty"
                                               class="lead-input"
                                               placeholder="Ví dụ: Công ty Cổ phần TechVina"
                                               maxlength="255"
                                               value="<c:out value="${formData != null ? formData.tenCongTy : param.ten_cong_ty}" />"
                                               autocomplete="organization" />
                                    </div>
                                </div>
                            </div>

                            <div class="lead-grid-2">
                                <!-- Số điện thoại -->
                                <div class="lead-form-group">
                                    <label class="lead-label" for="so_dien_thoai">
                                        <span>Số điện thoại</span>
                                    </label>
                                    <div class="lead-input-wrap">
                                        <span class="material-symbols-outlined lead-input-icon">call</span>
                                        <input type="tel"
                                               id="so_dien_thoai"
                                               name="so_dien_thoai"
                                               class="lead-input ${not empty errors.soDienThoai ? 'is-invalid' : ''}"
                                               placeholder="0912 345 678"
                                               maxlength="20"
                                               value="<c:out value="${formData != null ? formData.soDienThoai : param.so_dien_thoai}" />"
                                               autocomplete="tel" />
                                    </div>
                                    <c:if test="${not empty errors.soDienThoai}">
                                        <div class="lead-error-msg"><c:out value="${errors.soDienThoai}" /></div>
                                    </c:if>
                                </div>

                                <!-- Email -->
                                <div class="lead-form-group">
                                    <label class="lead-label" for="email">
                                        <span>Địa chỉ Email</span>
                                    </label>
                                    <div class="lead-input-wrap">
                                        <span class="material-symbols-outlined lead-input-icon">mail</span>
                                        <input type="email"
                                               id="email"
                                               name="email"
                                               class="lead-input ${not empty errors.email ? 'is-invalid' : ''}"
                                               placeholder="an.nguyen@techvina.vn"
                                               maxlength="255"
                                               value="<c:out value="${formData != null ? formData.email : param.email}" />"
                                               autocomplete="email" />
                                    </div>
                                    <c:if test="${not empty errors.email}">
                                        <div class="lead-error-msg"><c:out value="${errors.email}" /></div>
                                    </c:if>
                                </div>
                            </div>
                        </div>

                        <!-- Khối 2: Nguồn tiếp nhận (AC3) & Nhu cầu trao đổi -->
                        <div class="lead-form-section">
                            <div class="lead-section-heading">
                                <span class="lead-section-title">
                                    <span class="material-symbols-outlined">category</span>
                                    <span>Nguồn tiếp nhận & Phân loại</span>
                                </span>
                                <span class="lead-label-badge">AC3: Nguồn bắt buộc</span>
                            </div>

                            <!-- Chips chọn nhanh Nguồn Lead -->
                            <div class="lead-form-group">
                                <label class="lead-label">
                                    <span>Chọn kênh tiếp nhận <span class="required-dot">*</span></span>
                                </label>
                                <c:set var="chonNguon" value="${formData != null && formData.nguonLeadId != null ? formData.nguonLeadId : (not empty param.nguon_lead_id ? param.nguon_lead_id : (not empty dsNguonLead ? dsNguonLead[0].id : 1))}" />
                                <div class="lead-source-chips" role="radiogroup" aria-label="Nguồn tiếp nhận Lead">
                                    <c:choose>
                                        <c:when test="${not empty dsNguonLead}">
                                            <c:forEach items="${dsNguonLead}" var="nguon" varStatus="loop">
                                                <div class="lead-source-chip ${(chonNguon == nguon.id || (chonNguon == null && loop.first)) ? 'selected' : ''}" data-value="${nguon.id}" role="radio" aria-checked="${chonNguon == nguon.id}" tabindex="0">
                                                    <span class="material-symbols-outlined">
                                                        <c:choose>
                                                            <c:when test="${nguon.maMuc == 'SU_KIEN'}">festival</c:when>
                                                            <c:when test="${nguon.maMuc == 'DANH_THIEP'}">contact_page</c:when>
                                                            <c:when test="${nguon.maMuc == 'GIOI_THIEU'}">handshake</c:when>
                                                            <c:when test="${nguon.maMuc == 'WEBSITE'}">globe</c:when>
                                                            <c:when test="${nguon.maMuc == 'DIEN_THOAI'}">support_agent</c:when>
                                                            <c:otherwise>category</c:otherwise>
                                                        </c:choose>
                                                    </span>
                                                    <span><c:out value="${nguon.tenMuc}" /></span>
                                                </div>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="lead-source-chip ${chonNguon == 1 ? 'selected' : ''}" data-value="1" role="radio" aria-checked="${chonNguon == 1}" tabindex="0">
                                                <span class="material-symbols-outlined">festival</span>
                                                <span>Sự kiện / Hội thảo</span>
                                            </div>
                                            <div class="lead-source-chip ${chonNguon == 2 ? 'selected' : ''}" data-value="2" role="radio" aria-checked="${chonNguon == 2}" tabindex="0">
                                                <span class="material-symbols-outlined">contact_page</span>
                                                <span>Danh thiếp đối tác</span>
                                            </div>
                                            <div class="lead-source-chip ${chonNguon == 3 ? 'selected' : ''}" data-value="3" role="radio" aria-checked="${chonNguon == 3}" tabindex="0">
                                                <span class="material-symbols-outlined">handshake</span>
                                                <span>Khách giới thiệu</span>
                                            </div>
                                            <div class="lead-source-chip ${chonNguon == 4 ? 'selected' : ''}" data-value="4" role="radio" aria-checked="${chonNguon == 4}" tabindex="0">
                                                <span class="material-symbols-outlined">globe</span>
                                                <span>Website / Biểu mẫu</span>
                                            </div>
                                            <div class="lead-source-chip ${chonNguon == 5 ? 'selected' : ''}" data-value="5" role="radio" aria-checked="${chonNguon == 5}" tabindex="0">
                                                <span class="material-symbols-outlined">support_agent</span>
                                                <span>Gọi điện thoại</span>
                                            </div>
                                            <div class="lead-source-chip ${chonNguon == 6 ? 'selected' : ''}" data-value="6" role="radio" aria-checked="${chonNguon == 6}" tabindex="0">
                                                <span class="material-symbols-outlined">more_horiz</span>
                                                <span>Nguồn khác</span>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <!-- Select ẩn đồng bộ với form POST -->
                                <select id="nguon_lead_id" name="nguon_lead_id" class="lead-select" style="display:none;" required>
                                    <c:choose>
                                        <c:when test="${not empty dsNguonLead}">
                                            <c:forEach items="${dsNguonLead}" var="nguon" varStatus="loop">
                                                <option value="${nguon.id}" ${(chonNguon == nguon.id || (chonNguon == null && loop.first)) ? 'selected' : ''}><c:out value="${nguon.tenMuc}" /></option>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <option value="1" ${chonNguon == 1 ? 'selected' : ''}>Sự kiện / Hội thảo</option>
                                            <option value="2" ${chonNguon == 2 ? 'selected' : ''}>Danh thiếp đối tác</option>
                                            <option value="3" ${chonNguon == 3 ? 'selected' : ''}>Khách giới thiệu</option>
                                            <option value="4" ${chonNguon == 4 ? 'selected' : ''}>Website / Biểu mẫu</option>
                                            <option value="5" ${chonNguon == 5 ? 'selected' : ''}>Gọi điện thoại</option>
                                            <option value="6" ${chonNguon == 6 ? 'selected' : ''}>Nguồn khác</option>
                                        </c:otherwise>
                                    </c:choose>
                                </select>
                                <c:if test="${not empty errors.nguonLeadId}">
                                    <div class="lead-error-msg"><c:out value="${errors.nguonLeadId}" /></div>
                                </c:if>
                            </div>

                            <div class="lead-grid-2">
                                <div class="lead-form-group">
                                    <label class="lead-label" for="muc_do_quan_tam">
                                        <span>Mức độ quan tâm</span>
                                    </label>
                                    <select id="muc_do_quan_tam" name="muc_do_quan_tam" class="lead-select">
                                        <option value="CAO">Ưu tiên cao - Nhu cầu gấp</option>
                                        <option value="TRUNG_BINH" selected>Trung bình - Đang khảo sát</option>
                                        <option value="THAP">Thấp - Nhận diện ban đầu</option>
                                    </select>
                                </div>

                                <div class="lead-form-group">
                                    <label class="lead-label" for="nhu_cau_quan_tam">
                                        <span>Nhu cầu quan tâm</span>
                                    </label>
                                    <input type="text"
                                           id="nhu_cau_quan_tam"
                                           name="nhu_cau_quan_tam"
                                           class="lead-input"
                                           placeholder="Ví dụ: CRM quản lý 30 sales, báo giá Quý 4..."
                                           value="<c:out value="${formData != null ? formData.nhuCauQuanTam : param.nhu_cau_quan_tam}" />" />
                                </div>
                            </div>

                            <!-- Ghi chú bối cảnh sự kiện / danh thiếp -->
                            <div class="lead-form-group">
                                <label class="lead-label" for="ghi_chu">
                                    <span>Ghi chú bối cảnh sự kiện / danh thiếp (AC1)</span>
                                </label>
                                <textarea id="ghi_chu"
                                          name="ghi_chu"
                                          class="lead-textarea"
                                          rows="2"
                                          placeholder="Ví dụ: Gặp tại Hội thảo TechExpo, khách đề nghị gửi slide giới thiệu giải pháp vào thứ Hai..."><c:out value="${formData != null ? formData.ghiChu : param.ghi_chu}" /></textarea>
                            </div>
                        </div>

                        <!-- Footer Actions -->
                        <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 24px; padding-top: 20px; border-top: 1px solid var(--lead-neutral-100);">
                            <a href="${pageContext.request.contextPath}/lead" class="lead-btn lead-btn-outline">Hủy bỏ</a>
                            <button type="submit" class="lead-btn lead-btn-primary" id="btnLuuLead">
                                <span class="material-symbols-outlined" style="font-size: 18px;">save</span>
                                <span>Lưu thông tin Lead</span>
                            </button>
                        </div>
                    </form>
                </div>

                <!-- CỘT PHỤ (PHẢI): LIVE BUSINESS CARD PREVIEW (THẺ DANH THIẾP THỜI GIAN THỰC) -->
                <aside class="lead-side-card">
                    <div class="lead-preview-card-wrap">
                        <div class="lead-preview-header">
                            <span class="lead-preview-title">
                                <span class="material-symbols-outlined" style="font-size: 16px;">visibility</span>
                                <span>Xem trước danh thiếp</span>
                            </span>
                            <span class="lead-preview-live-indicator">
                                <span class="lead-preview-live-dot"></span>
                                <span>Cập nhật trực tiếp</span>
                            </span>
                        </div>

                        <!-- Thẻ danh thiếp mô phỏng -->
                        <div class="lead-biz-card">
                            <div class="lead-biz-top">
                                <div class="lead-biz-avatar" id="bizCardAvatar">VA</div>
                                <div class="lead-biz-info">
                                    <h3 id="bizCardName">Nguyễn Văn An</h3>
                                    <div class="lead-biz-company" id="bizCardCompany">Công ty Cổ phần TechVina</div>
                                </div>
                            </div>

                            <div class="lead-biz-details">
                                <div class="lead-biz-row">
                                    <span class="material-symbols-outlined">call</span>
                                    <span id="bizCardPhone">0912 345 678</span>
                                </div>
                                <div class="lead-biz-row">
                                    <span class="material-symbols-outlined">mail</span>
                                    <span id="bizCardEmail">an.nguyen@techvina.vn</span>
                                </div>
                            </div>

                            <div>
                                <span class="lead-biz-badge" id="bizCardSource">
                                    🎪 Sự kiện / Hội thảo
                                </span>
                            </div>
                        </div>
                    </div>
                </aside>

            </div>
        </div>
    </main>

    <!-- JavaScript chuyên biệt cho tạo Lead thủ công (Live Preview & Validation) -->
    <script src="${pageContext.request.contextPath}/assets/js/lead/tao-lead.js"></script>
</body>
</html>
