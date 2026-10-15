<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hàng Chờ Phân Bổ Lead (AC3) | CRM Bán Hàng</title>
    <meta name="description" content="Danh sách khách hàng tiềm năng không thỏa mãn bất kỳ quy tắc phân bổ tự động nào. Trưởng nhóm và Giám đốc phân tay cho nhân viên phụ trách.">

    <!-- CSS dùng chung toàn hệ thống -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <!-- CSS chuyên biệt module Phân bổ Lead (Story S4-06) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lead/phan-bo.css">

    <script>
        window.CONTEXT_PATH = "${pageContext.request.contextPath}";
    </script>
</head>
<body class="crm-body">

    <!-- Navigation layout dùng chung S1-06 -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <div class="pb-container">

            <!-- 1. Breadcrumb điều hướng -->
            <nav class="pb-breadcrumb" aria-label="Đường dẫn điều hướng">
                <div class="pb-breadcrumb-links">
                    <a href="${pageContext.request.contextPath}/dieu-huong">Trang chủ</a>
                    <span class="sep">/</span>
                    <a href="${pageContext.request.contextPath}/lead">Lead & Phân bổ</a>
                    <span class="sep">/</span>
                    <span class="current">Hàng chờ phân bổ (AC3)</span>
                </div>
                <div style="display: flex; gap: 8px;">
                    <a href="${pageContext.request.contextPath}/admin/lead/phan-bo" class="btn btn-outline btn-sm" id="btn-ve-quy-tac">
                        <span class="material-symbols-outlined icon-xs">settings_suggest</span> Cấu hình quy tắc
                    </a>
                </div>
            </nav>

            <!-- 2. Header giao diện chính -->
            <header class="pb-header">
                <div class="pb-header-content">
                    <div class="pb-header-badge" style="background: rgba(245, 158, 11, 0.12); color: #d97706; border-color: rgba(245, 158, 11, 0.25);">
                        <span class="material-symbols-outlined icon-xs">inbox</span> AC3 — Unmatched Leads Queue
                    </div>
                    <h1 class="pb-header-title">Hàng Chờ Phân Bổ Lead (AC3)</h1>
                    <p class="pb-header-desc">
                        Danh sách các khách hàng tiềm năng không thỏa mãn bất kỳ quy tắc phân bổ tự động nào.
                        <strong>Trưởng nhóm kinh doanh</strong> (phân bổ trong nhóm mình) và <strong>Giám đốc</strong> (toàn công ty) chỉ định nhân viên phụ trách thủ công (phân tay) để đảm bảo không thất lạc cơ hội bán hàng.
                    </p>
                </div>

                <div class="pb-header-actions">
                    <button type="button" class="btn btn-secondary" onclick="window.location.reload();" title="Tải lại danh sách mới nhất">
                        <span class="material-symbols-outlined">refresh</span> Làm mới
                    </button>
                    <a href="${pageContext.request.contextPath}/admin/lead/phan-bo" class="btn btn-primary" title="Chuyển đến màn hình Cấu hình quy tắc phân bổ tự động">
                        <span class="material-symbols-outlined">alt_route</span> Cấu hình quy tắc phân bổ
                    </a>
                </div>
            </header>

            <!-- 3. Thông báo kết quả thao tác Flash Message -->
            <c:if test="${not empty sessionScope.successMessage}">
                <div class="pb-alert pb-alert-success" role="alert">
                    <div class="pb-alert-content">
                        <span class="material-symbols-outlined">check_circle</span>
                        <span><strong>Thành công:</strong> <c:out value="${sessionScope.successMessage}"/></span>
                    </div>
                    <button type="button" class="pb-alert-close" onclick="this.parentElement.remove();" aria-label="Đóng">
                        <span class="material-symbols-outlined">close</span>
                    </button>
                </div>
                <c:remove var="successMessage" scope="session"/>
            </c:if>

            <c:if test="${not empty requestScope.errorMessage}">
                <div class="pb-alert pb-alert-danger" role="alert">
                    <div class="pb-alert-content">
                        <span class="material-symbols-outlined">error</span>
                        <span><strong>Lỗi:</strong> <c:out value="${requestScope.errorMessage}"/></span>
                    </div>
                    <button type="button" class="pb-alert-close" onclick="this.parentElement.remove();" aria-label="Đóng">
                        <span class="material-symbols-outlined">close</span>
                    </button>
                </div>
            </c:if>

            <!-- 4. Khối Thống kê Tổng quan (KPI) -->
            <section class="pb-stats-grid" aria-label="Thống kê hàng chờ">
                <div class="pb-stat-card">
                    <div class="pb-stat-icon-wrap warning">
                        <span class="material-symbols-outlined">pending_actions</span>
                    </div>
                    <div class="pb-stat-info">
                        <span class="pb-stat-label">Số lead đang chờ phân tay</span>
                        <span class="pb-stat-value" id="stat-total-queue">${not empty tongSoLeadHangCho ? tongSoLeadHangCho : '0'}</span>
                        <span class="pb-stat-note warning-text">Cần chỉ định phụ trách ngay</span>
                    </div>
                </div>

                <div class="pb-stat-card">
                    <div class="pb-stat-icon-wrap primary">
                        <span class="material-symbols-outlined">timer</span>
                    </div>
                    <div class="pb-stat-info">
                        <span class="pb-stat-label">Cam kết SLA phản hồi</span>
                        <span class="pb-stat-value">24 Giờ</span>
                        <span class="pb-stat-note">Kể từ khi hoàn tất gán lead</span>
                    </div>
                </div>

                <div class="pb-stat-card">
                    <div class="pb-stat-icon-wrap info">
                        <span class="material-symbols-outlined">badge</span>
                    </div>
                    <div class="pb-stat-info">
                        <span class="pb-stat-label">Nhân sự nhận phân bổ</span>
                        <span class="pb-stat-value" id="stat-sales-count">${not empty dsNhanVienPhanBo ? fn:length(dsNhanVienPhanBo) : '0'}</span>
                        <span class="pb-stat-note">Nhân viên sẵn sàng nhận lead</span>
                    </div>
                </div>

                <div class="pb-stat-card">
                    <div class="pb-stat-icon-wrap success">
                        <span class="material-symbols-outlined">security</span>
                    </div>
                    <div class="pb-stat-info">
                        <span class="pb-stat-label">Phạm vi quyền (Data Scope)</span>
                        <span class="pb-stat-value" style="font-size: 18px; line-height: 28px;">Nhóm / Toàn quyền</span>
                        <span class="pb-stat-note success-text">Server-side RBAC Verified</span>
                    </div>
                </div>
            </section>

            <!-- 5. Card danh sách Lead trong Hàng chờ -->
            <div class="pb-card">
                <!-- Toolbar tìm kiếm và lọc -->
                <div class="pb-toolbar" style="background: #fffbeb;">
                    <div class="pb-toolbar-left">
                        <div class="pb-search-box">
                            <span class="material-symbols-outlined icon-xs">search</span>
                            <input type="text" id="input-search-queue" placeholder="Tìm theo tên khách, công ty, email, SĐT, mã lead..." aria-label="Tìm kiếm lead trong hàng chờ">
                        </div>
                        <div class="pb-filter-select">
                            <select id="select-filter-queue-area" aria-label="Lọc theo khu vực">
                                <option value="ALL">Tất cả khu vực</option>
                                <option value="CHUA_CO">Chưa có khu vực</option>
                            </select>
                        </div>
                    </div>

                    <div class="pb-toolbar-right">
                        <span style="font-size: 13px; color: #92400e; font-weight: 600;">
                            <span class="material-symbols-outlined icon-xs" style="vertical-align: middle;">info</span> Bấm "Phân tay ngay" để chỉ định nhân viên phụ trách cho từng lead
                        </span>
                    </div>
                </div>

                <!-- Bảng danh sách lead hàng chờ (Action PBL-05) -->
                <div class="pb-table-wrap">
                    <table class="pb-table" id="pb-queue-table">
                        <thead>
                            <tr>
                                <th style="width: 130px;">Mã Lead</th>
                                <th style="min-width: 220px;">Khách hàng / Công ty</th>
                                <th style="min-width: 200px;">Thông tin liên hệ</th>
                                <th style="width: 150px;">Khu vực</th>
                                <th style="width: 160px;">Ngành nghề</th>
                                <th style="width: 130px;">Điểm / Phân loại</th>
                                <th style="width: 150px;">Thời gian vào</th>
                                <th style="width: 140px; text-align: center;">Thao tác (AC3)</th>
                            </tr>
                        </thead>
                        <tbody id="pb-queue-tbody">
                            <c:choose>
                                <c:when test="${not empty dsLeadHangCho}">
                                    <c:forEach var="lead" items="${dsLeadHangCho}">
                                        <tr data-lead-id="${lead.id}"
                                            data-lead-code="${lead.maLead}"
                                            data-lead-name="${lead.hoTen}"
                                            data-lead-company="${lead.tenCongTy}"
                                            data-lead-email="${lead.email}"
                                            data-lead-phone="${lead.soDienThoai}"
                                            data-lead-area="${lead.tenKhuVuc}"
                                            data-lead-industry="${lead.tenNganhNghe}"
                                            data-lead-needs="${lead.nhuCauQuanTam}"
                                            data-lead-score="${lead.diem}"
                                            data-lead-category="${lead.phanLoai}"
                                            data-lead-time="${lead.createdAt}">

                                            <!-- Mã Lead -->
                                            <td>
                                                <span class="pb-rule-code"><c:out value="${lead.maLead}"/></span>
                                            </td>

                                            <!-- Khách hàng / Công ty -->
                                            <td>
                                                <div style="font-weight: 700; color: var(--pb-slate-900);"><c:out value="${lead.hoTen}"/></div>
                                                <div style="font-size: 12px; color: var(--pb-slate-500);"><c:out value="${not empty lead.tenCongTy ? lead.tenCongTy : 'Cá nhân'}"/></div>
                                            </td>

                                            <!-- Liên hệ -->
                                            <td>
                                                <div style="font-size: 13px; color: var(--pb-slate-800);"><c:out value="${lead.email}"/></div>
                                                <div style="font-size: 12px; color: var(--pb-slate-500);"><c:out value="${lead.soDienThoai}"/></div>
                                            </td>

                                            <!-- Khu vực -->
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty lead.tenKhuVuc}">
                                                        <span class="pb-chip"><c:out value="${lead.tenKhuVuc}"/></span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="pb-chip" style="background: var(--pb-slate-100); color: var(--pb-slate-400);">Chưa xác định</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>

                                            <!-- Ngành nghề -->
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty lead.tenNganhNghe}">
                                                        <span class="pb-chip"><c:out value="${lead.tenNganhNghe}"/></span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="pb-chip" style="background: var(--pb-slate-100); color: var(--pb-slate-400);">Chưa xác định</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>

                                            <!-- Điểm & Phân loại -->
                                            <td>
                                                <span class="pb-score-badge" style="font-weight: 700; color: var(--pb-primary);">${lead.diem} đ</span>
                                                <c:choose>
                                                    <c:when test="${lead.phanLoai == 'NONG'}">
                                                        <span class="pb-category-badge" style="background: #fef2f2; color: #b91c1c; font-size: 11px; padding: 2px 6px; border-radius: 4px; font-weight: 600;">Nóng</span>
                                                    </c:when>
                                                    <c:when test="${lead.phanLoai == 'AM'}">
                                                        <span class="pb-category-badge" style="background: #fffbeb; color: #b45309; font-size: 11px; padding: 2px 6px; border-radius: 4px; font-weight: 600;">Ấm</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="pb-category-badge" style="background: #f1f5f9; color: #475569; font-size: 11px; padding: 2px 6px; border-radius: 4px; font-weight: 600;">Lạnh</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>

                                            <!-- Thời gian vào hàng chờ -->
                                            <td>
                                                <span style="font-size: 12.5px; color: var(--pb-slate-600);">
                                                    <c:out value="${not empty lead.thoiGianChoFormatted ? lead.thoiGianChoFormatted : lead.createdAt}"/>
                                                </span>
                                            </td>

                                            <!-- Thao tác phân tay (Action PBL-06) -->
                                            <td style="text-align: center;">
                                                <button type="button" class="btn btn-primary btn-sm btn-phan-tay" title="Chỉ định nhân viên phụ trách cho lead này">
                                                    <span class="material-symbols-outlined icon-xs">assignment_ind</span> Phân tay ngay
                                                </button>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="8" style="text-align: center; padding: 48px 20px;">
                                            <div class="pb-empty-state">
                                                <span class="material-symbols-outlined" style="font-size: 54px; color: var(--pb-success); display: block; margin-bottom: 12px;">task_alt</span>
                                                <h3 style="font-size: 16px; font-weight: 700; color: var(--pb-slate-800); margin: 0 0 6px;">Không có lead nào đang chờ phân bổ!</h3>
                                                <p style="font-size: 13.5px; color: var(--pb-slate-500); margin: 0; max-width: 480px; margin: 0 auto;">
                                                    Tất cả khách hàng tiềm năng đã được tự động so khớp theo chuỗi quy tắc phân bổ First-Match-Wins hoặc đã được chỉ định nhân viên phụ trách.
                                                </p>
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

    <!-- ========================================================
         MODAL CHỈ ĐỊNH PHÂN TAY LEAD (ACTION PBL-06, AC3)
         ======================================================== -->
    <div class="pb-modal-overlay" id="modal-phan-tay" role="dialog" aria-modal="true" aria-labelledby="modal-pt-title">
        <div class="pb-modal-dialog" style="max-width: 540px;">
            <div class="pb-modal-header">
                <div style="display: flex; align-items: center; gap: 10px;">
                    <span class="material-symbols-outlined" style="color: var(--pb-primary); font-size: 24px;">assignment_ind</span>
                    <h3 class="pb-modal-title" id="modal-pt-title">Chỉ Định Nhân Viên Phụ Trách (AC3)</h3>
                </div>
                <button type="button" class="pb-modal-close" id="btn-close-pt-modal" aria-label="Đóng">
                    <span class="material-symbols-outlined">close</span>
                </button>
            </div>

            <form id="form-phan-tay" action="${pageContext.request.contextPath}/lead/hang-cho-phan-bo/phan-tay" method="POST">
                <div class="pb-modal-body">
                    <!-- ID Lead ẩn -->
                    <input type="hidden" id="pt-lead-id" name="lead_id" value="">

                    <!-- Card tóm tắt Lead đang được phân bổ -->
                    <div style="background: var(--pb-slate-50); border: 1px solid var(--pb-slate-200); border-radius: var(--pb-radius-md); padding: 14px 16px; margin-bottom: 16px;">
                        <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 6px;">
                            <strong style="font-size: 15px; color: var(--pb-slate-900);" id="pt-lead-name">---</strong>
                            <span class="pb-rule-code" id="pt-lead-code">---</span>
                        </div>
                        <div style="font-size: 13px; color: var(--pb-slate-600); margin-bottom: 4px;" id="pt-lead-company">---</div>
                        <div style="display: flex; gap: 14px; font-size: 12.5px; color: var(--pb-slate-500); flex-wrap: wrap;">
                            <span id="pt-lead-email">Email: ---</span>
                            <span id="pt-lead-phone">SĐT: ---</span>
                        </div>
                        <div style="margin-top: 8px; font-size: 12px; color: #b45309; background: #fffbeb; padding: 6px 10px; border-radius: 4px;">
                            <strong>Nhu cầu:</strong> <span id="pt-lead-needs">---</span>
                        </div>
                    </div>

                    <!-- Chọn nhân viên phụ trách -->
                    <div class="form-group">
                        <label class="form-label" for="nhan_vien_id">
                            <span>Nhân viên kinh doanh phụ trách (Sales Rep) <span class="required">*</span></span>
                        </label>
                        <select class="form-control" id="nhan_vien_id" name="nhan_vien_id" required>
                            <option value="">-- Chọn nhân viên kinh doanh tiếp nhận --</option>
                            <c:choose>
                                <c:when test="${not empty dsNhanVienPhanBo}">
                                    <c:forEach var="nv" items="${dsNhanVienPhanBo}">
                                        <option value="${nv.id}">
                                            <c:out value="${nv.hoTen}"/> (${nv.email})
                                        </option>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <option value="2">Nguyễn Văn An (Sales Rep - Miền Bắc)</option>
                                    <option value="3">Trần Thị Bình (Sales Rep - Miền Bắc)</option>
                                    <option value="4">Lê Hoàng Long (Sales Rep - Miền Nam)</option>
                                </c:otherwise>
                            </c:choose>
                        </select>
                        <div class="form-error-msg" id="err-nhan-vien">Vui lòng chọn nhân viên phụ trách</div>
                    </div>

                    <!-- Ghi chú phân bổ -->
                    <div class="form-group">
                        <label class="form-label" for="ghi_chu">
                            <span>Lý do / Ghi chú phân bổ</span>
                            <span style="font-size: 11.5px; color: var(--pb-slate-400);">Lưu vào nhật ký phân bổ lead</span>
                        </label>
                        <textarea class="form-control" id="ghi_chu" name="ghi_chu" rows="3" placeholder="Nhập lý do chỉ định hoặc hướng dẫn chăm sóc ban đầu..." maxlength="500"></textarea>
                    </div>
                </div>

                <div class="pb-modal-footer">
                    <button type="button" class="btn btn-outline" id="btn-cancel-pt-modal">Hủy bỏ</button>
                    <button type="submit" class="btn btn-primary" id="btn-submit-pt">
                        <span class="material-symbols-outlined">how_to_reg</span> Xác nhận phân bổ lead
                    </button>
                </div>
            </form>
        </div>
    </div>

    <!-- Container Toast thông báo -->
    <div class="pb-toast-container" id="pb-toast-container" aria-live="polite"></div>

    <!-- Script điều khiển Hàng chờ -->
    <script src="${pageContext.request.contextPath}/assets/js/lead/hang-cho.js"></script>
</body>
</html>
