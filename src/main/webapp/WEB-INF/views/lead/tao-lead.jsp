<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Tạo mới khách hàng tiềm năng (Lead) thủ công từ danh thiếp hoặc sự kiện - CRM Bán Hàng Nhóm 10">
    <title>Tạo Mới Lead Thủ Công - CRM Bán Hàng</title>
    <!-- CSS Hệ thống chung (Navigation & Typography) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <!-- CSS Chuyên biệt Module Lead (Story S4-02) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lead/lead.css">
</head>
<body class="crm-body">

    <!-- Navigation & Sidebar dùng chung toàn hệ thống -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <div class="lead-container" style="max-width: 980px;">

            <!-- 1. Breadcrumb điều hướng -->
            <nav class="lead-breadcrumb" aria-label="Đường dẫn điều hướng">
                <ol class="lead-breadcrumb-nav">
                    <li><a href="${pageContext.request.contextPath}/dieu-huong" class="lead-breadcrumb-link">Trang chủ</a></li>
                    <span class="lead-breadcrumb-sep" aria-hidden="true">/</span>
                    <li><a href="${pageContext.request.contextPath}/lead" class="lead-breadcrumb-link">Lead & Phân bổ</a></li>
                    <span class="lead-breadcrumb-sep" aria-hidden="true">/</span>
                    <li class="lead-breadcrumb-current" aria-current="page">Tạo Lead thủ công</li>
                </ol>
                <div>
                    <a href="${pageContext.request.contextPath}/lead" class="lead-btn lead-btn-outline lead-btn-sm" id="btnQuayLaiDanhSach">
                        <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 18px;">arrow_back</span>
                        <span>Quay lại danh sách Lead</span>
                    </a>
                </div>
            </nav>

            <!-- 2. Header Card -->
            <header class="lead-header-card">
                <div class="lead-header-title">
                    <h1>
                        <span class="material-symbols-outlined" style="color: var(--lead-primary); font-size: 26px;" aria-hidden="true">person_add</span>
                        <span>Nhập Thủ Công Khách Hàng Tiềm Năng (Lead)</span>
                        <span class="lead-header-badge">Sự kiện / Danh thiếp</span>
                    </h1>
                    <p>Nhập tay một lead thu thập từ hội thảo, sự kiện hoặc danh thiếp kinh doanh vào CRM (Story S4-02). Mọi lead bắt buộc phải có nguồn tiếp nhận (AC3).</p>
                </div>
            </header>

            <!-- Thông báo Lỗi nếu có -->
            <c:if test="${not empty thongBaoLoi}">
                <div class="lead-alert lead-alert-danger" role="alert">
                    <span class="material-symbols-outlined" aria-hidden="true">error</span>
                    <div>
                        <strong>Lỗi:</strong> <c:out value="${thongBaoLoi}" />
                    </div>
                </div>
            </c:if>

            <!-- Thông báo Thành công nếu có -->
            <c:if test="${not empty thongBaoThanhCong}">
                <div class="lead-alert lead-alert-success" role="alert">
                    <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                    <div>
                        <strong>Thành công:</strong> <c:out value="${thongBaoThanhCong}" />
                    </div>
                </div>
            </c:if>

            <!-- 3. Form Card Tạo Lead -->
            <div class="lead-form-card">
                <!-- Callout Tiêu chí AC3 -->
                <div class="lead-ac3-callout" role="note">
                    <span class="material-symbols-outlined" aria-hidden="true">verified_user</span>
                    <div class="lead-ac3-callout-text">
                        <h4>Quy tắc bắt buộc (Tiêu chí AC3)</h4>
                        <p>Mỗi lead nhập vào hệ thống bắt buộc phải có thông tin <strong>Nguồn Lead</strong> (Hội thảo, Danh thiếp, Giới thiệu...). Điều này giúp đội ngũ Marketing và Kinh doanh theo dõi chính xác hiệu quả từng kênh tiếp xúc.</p>
                    </div>
                </div>

                <form action="${pageContext.request.contextPath}/lead/tao-moi" method="POST" id="formTaoLead" novalidate>
                    <input type="hidden" name="action" value="tao-thu-cong">

                    <!-- Section 1: Thông tin người liên hệ & Công ty -->
                    <div class="lead-form-section">
                        <h2 class="lead-section-title">
                            <span class="material-symbols-outlined" aria-hidden="true">badge</span>
                            <span>1. Thông tin người liên hệ & Doanh nghiệp</span>
                        </h2>

                        <div class="lead-grid-2">
                            <!-- Họ và tên (Bắt buộc) -->
                            <div class="lead-form-group">
                                <label class="lead-label" for="ho_ten">
                                    <span>Họ và tên khách hàng</span>
                                    <span class="required" title="Bắt buộc">*</span>
                                </label>
                                <input type="text"
                                       id="ho_ten"
                                       name="ho_ten"
                                       class="lead-input ${not empty errors.hoTen ? 'is-invalid' : ''}"
                                       placeholder="Ví dụ: Nguyễn Văn An"
                                       maxlength="150"
                                       required
                                       value="<c:out value="${formData != null ? formData.hoTen : param.ho_ten}" />"
                                       autocomplete="name" />
                                <div class="lead-hint">Họ tên ghi trên danh thiếp hoặc danh sách tham dự hội thảo.</div>
                                <c:if test="${not empty errors.hoTen}">
                                    <div class="lead-error-msg" role="alert">
                                        <span class="material-symbols-outlined" style="font-size:16px;">error</span>
                                        <c:out value="${errors.hoTen}" />
                                    </div>
                                </c:if>
                            </div>

                            <!-- Tên công ty / Doanh nghiệp -->
                            <div class="lead-form-group">
                                <label class="lead-label" for="ten_cong_ty">
                                    <span>Tên công ty / Tổ chức</span>
                                </label>
                                <input type="text"
                                       id="ten_cong_ty"
                                       name="ten_cong_ty"
                                       class="lead-input"
                                       placeholder="Ví dụ: Công ty Cổ phần Công nghệ ABC"
                                       maxlength="255"
                                       value="<c:out value="${formData != null ? formData.tenCongTy : param.ten_cong_ty}" />"
                                       autocomplete="organization" />
                                <div class="lead-hint">Tên doanh nghiệp hoặc cơ quan đối tác.</div>
                            </div>
                        </div>

                        <div class="lead-grid-2">
                            <!-- Số điện thoại -->
                            <div class="lead-form-group">
                                <label class="lead-label" for="so_dien_thoai">
                                    <span>Số điện thoại</span>
                                </label>
                                <input type="tel"
                                       id="so_dien_thoai"
                                       name="so_dien_thoai"
                                       class="lead-input ${not empty errors.soDienThoai ? 'is-invalid' : ''}"
                                       placeholder="Ví dụ: 0912345678"
                                       maxlength="20"
                                       value="<c:out value="${formData != null ? formData.soDienThoai : param.so_dien_thoai}" />"
                                       autocomplete="tel" />
                                <div class="lead-hint">10 chữ số, định dạng chuẩn liên lạc Việt Nam.</div>
                                <c:if test="${not empty errors.soDienThoai}">
                                    <div class="lead-error-msg" role="alert">
                                        <span class="material-symbols-outlined" style="font-size:16px;">error</span>
                                        <c:out value="${errors.soDienThoai}" />
                                    </div>
                                </c:if>
                            </div>

                            <!-- Địa chỉ Email -->
                            <div class="lead-form-group">
                                <label class="lead-label" for="email">
                                    <span>Địa chỉ Email</span>
                                </label>
                                <input type="email"
                                       id="email"
                                       name="email"
                                       class="lead-input ${not empty errors.email ? 'is-invalid' : ''}"
                                       placeholder="Ví dụ: an.nguyen@congty.vn"
                                       maxlength="255"
                                       value="<c:out value="${formData != null ? formData.email : param.email}" />"
                                       autocomplete="email" />
                                <div class="lead-hint">Email doanh nghiệp để gửi thông tin báo giá & giải pháp.</div>
                                <c:if test="${not empty errors.email}">
                                    <div class="lead-error-msg" role="alert">
                                        <span class="material-symbols-outlined" style="font-size:16px;">error</span>
                                        <c:out value="${errors.email}" />
                                    </div>
                                </c:if>
                            </div>
                        </div>
                    </div>

                    <!-- Section 2: Nguồn Lead & Nhu cầu quan tâm (AC3) -->
                    <div class="lead-form-section">
                        <h2 class="lead-section-title">
                            <span class="material-symbols-outlined" aria-hidden="true">category</span>
                            <span>2. Nguồn tiếp nhận & Nhu cầu trao đổi</span>
                        </h2>

                        <div class="lead-grid-2">
                            <!-- Nguồn Lead (Bắt buộc theo AC3) -->
                            <div class="lead-form-group">
                                <label class="lead-label" for="nguon_lead_id">
                                    <span>Nguồn Lead tiếp nhận</span>
                                    <span class="required" title="Bắt buộc">*</span>
                                    <span class="badge-ac">AC3 Bắt buộc</span>
                                </label>
                                <select id="nguon_lead_id"
                                        name="nguon_lead_id"
                                        class="lead-select ${not empty errors.nguonLeadId ? 'is-invalid' : ''}"
                                        required>
                                    <option value="">-- Vui lòng chọn nguồn Lead (Bắt buộc) --</option>
                                    <c:choose>
                                        <c:when test="${not empty dsNguonLead}">
                                            <c:forEach items="${dsNguonLead}" var="nguon">
                                                <option value="${nguon.id}" ${(formData != null && formData.nguonLeadId == nguon.id) || param.nguon_lead_id == nguon.id ? 'selected' : ''}>
                                                    <c:out value="${nguon.ten}" />
                                                </option>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <!-- Fallback danh mục nguồn chuẩn theo nghiệp vụ S4-02 -->
                                            <option value="1" ${param.nguon_lead_id == '1' ? 'selected' : ''}>Sự kiện / Hội thảo</option>
                                            <option value="2" ${param.nguon_lead_id == '2' ? 'selected' : ''}>Danh thiếp đối tác</option>
                                            <option value="3" ${param.nguon_lead_id == '3' ? 'selected' : ''}>Khách hàng giới thiệu</option>
                                            <option value="4" ${param.nguon_lead_id == '4' ? 'selected' : ''}>Website / Biểu mẫu trực tuyến</option>
                                            <option value="5" ${param.nguon_lead_id == '5' ? 'selected' : ''}>Gọi điện thoại trực tiếp</option>
                                            <option value="6" ${param.nguon_lead_id == '6' ? 'selected' : ''}>Nguồn tiếp cận khác</option>
                                        </c:otherwise>
                                    </c:choose>
                                </select>
                                <div class="lead-hint">Kênh tiếp cận khách hàng tiềm năng lần đầu tiên.</div>
                                <c:if test="${not empty errors.nguonLeadId}">
                                    <div class="lead-error-msg" role="alert">
                                        <span class="material-symbols-outlined" style="font-size:16px;">error</span>
                                        <c:out value="${errors.nguonLeadId}" />
                                    </div>
                                </c:if>
                            </div>

                            <!-- Mức độ quan tâm -->
                            <div class="lead-form-group">
                                <label class="lead-label" for="muc_do_quan_tam">
                                    <span>Mức độ quan tâm ban đầu</span>
                                </label>
                                <select id="muc_do_quan_tam" name="muc_do_quan_tam" class="lead-select">
                                    <option value="CAO" ${param.muc_do_quan_tam == 'CAO' ? 'selected' : ''}>Cao - Nhu cầu cấp thiết</option>
                                    <option value="TRUNG_BINH" ${param.muc_do_quan_tam == 'TRUNG_BINH' || empty param.muc_do_quan_tam ? 'selected' : ''}>Trung bình - Đang tìm hiểu</option>
                                    <option value="THAP" ${param.muc_do_quan_tam == 'THAP' ? 'selected' : ''}>Thấp - Nhận diện ban đầu</option>
                                </select>
                                <div class="lead-hint">Đánh giá sơ bộ phục vụ phân loại và phân bổ nhân sự chăm sóc.</div>
                            </div>
                        </div>

                        <!-- Nhu cầu quan tâm -->
                        <div class="lead-form-group">
                            <label class="lead-label" for="nhu_cau_quan_tam">
                                <span>Nhu cầu quan tâm / Dịch vụ tư vấn</span>
                            </label>
                            <textarea id="nhu_cau_quan_tam"
                                      name="nhu_cau_quan_tam"
                                      class="lead-textarea"
                                      rows="3"
                                      placeholder="Ví dụ: Cần triển khai hệ thống CRM quản lý đội ngũ 30 nhân viên kinh doanh, dự kiến đưa vào vận hành trong Quý 4..."><c:out value="${formData != null ? formData.nhuCauQuanTam : param.nhu_cau_quan_tam}" /></textarea>
                        </div>

                        <!-- Ghi chú sự kiện / danh thiếp -->
                        <div class="lead-form-group">
                            <label class="lead-label" for="ghi_chu">
                                <span>Ghi chú bối cảnh sự kiện / danh thiếp (AC1)</span>
                            </label>
                            <textarea id="ghi_chu"
                                      name="ghi_chu"
                                      class="lead-textarea"
                                      rows="2"
                                      placeholder="Ví dụ: Trao đổi danh thiếp tại Hội thảo TechExpo 2026 ngày 14/10, người đại diện kỹ thuật hẹn gửi báo giá chi tiết..."><c:out value="${formData != null ? formData.ghiChu : param.ghi_chu}" /></textarea>
                            <div class="lead-hint">Lưu lại bối cảnh tiếp xúc giúp nhân viên kinh doanh tiếp cận tự nhiên hơn.</div>
                        </div>
                    </div>

                    <!-- 4. Form Action Footer -->
                    <footer class="lead-form-actions">
                        <a href="${pageContext.request.contextPath}/lead" class="lead-btn lead-btn-outline" id="btnHuyTaoLead">
                            <span>Hủy bỏ</span>
                        </a>
                        <button type="button" class="lead-btn lead-btn-outline" id="btnLuuVaTiepTuc">
                            <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 18px;">add_task</span>
                            <span>Lưu & Tiếp tục thêm</span>
                        </button>
                        <button type="submit" class="lead-btn lead-btn-primary" id="btnLuuLead">
                            <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 18px;">save</span>
                            <span>Lưu thông tin Lead</span>
                        </button>
                    </footer>
                </form>
            </div>
        </div>
    </main>

    <!-- JavaScript chuyên biệt cho tạo Lead thủ công (Client-side validation & Double-submit protection) -->
    <script src="${pageContext.request.contextPath}/assets/js/lead/tao-lead.js"></script>
</body>
</html>
