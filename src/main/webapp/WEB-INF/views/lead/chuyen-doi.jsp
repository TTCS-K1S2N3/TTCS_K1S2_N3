<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%!
    public static String formatDate(java.time.LocalDate date) {
        if (date == null) return "Chưa xác định";
        try {
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return date.format(dtf);
        } catch (Exception e) {
            return date.toString();
        }
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Xem trước và chuyển đổi Lead thành Khách hàng, Người liên hệ và Cơ hội bán hàng - CRM Bán Hàng Nhóm 10">
    <title>Chuyển Đổi Lead: <c:out value="${leadChuyenDoi.hoTen}" /> - CRM Bán Hàng</title>
    <!-- CSS dùng chung toàn hệ thống -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <!-- CSS chuyên biệt module Chuyển đổi Lead (Story S4-08) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lead/chuyen-doi.css">
</head>
<body class="crm-body">
    <!-- Navigation layout dùng chung S1-06 (đã bao gồm sidebar, thông tin user và Material Symbols) -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <div class="cdl-container">

            <!-- 1. Breadcrumb điều hướng -->
            <nav class="cdl-breadcrumb" aria-label="Breadcrumb">
                <ol class="cdl-breadcrumb-nav">
                    <li><a href="${pageContext.request.contextPath}/dieu-huong" class="cdl-breadcrumb-link">Trang chủ</a></li>
                    <span class="cdl-breadcrumb-sep" aria-hidden="true">/</span>
                    <li><a href="${pageContext.request.contextPath}/lead" class="cdl-breadcrumb-link">Lead & Phân bổ</a></li>
                    <span class="cdl-breadcrumb-sep" aria-hidden="true">/</span>
                    <li><a href="${pageContext.request.contextPath}/lead/chi-tiet?leadId=${leadChuyenDoi.leadId}" class="cdl-breadcrumb-link">Chi tiết Lead #${leadChuyenDoi.leadId}</a></li>
                    <span class="cdl-breadcrumb-sep" aria-hidden="true">/</span>
                    <li class="cdl-breadcrumb-current" aria-current="page">Chuyển đổi thành Khách hàng</li>
                </ol>
                <div>
                    <a href="${pageContext.request.contextPath}/lead/chi-tiet?leadId=${leadChuyenDoi.leadId}" class="cdl-btn cdl-btn-outline" id="btnQuayLaiChiTietLead" title="Quay lại chi tiết Lead">
                        <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span>
                        <span>Chi tiết Lead</span>
                    </a>
                </div>
            </nav>

            <!-- 2. Thông báo / Lỗi nếu có -->
            <c:if test="${not empty thongBao}">
                <div class="cdl-alert ${fn:contains(thongBao, 'thành công') ? 'cdl-alert-success' : 'cdl-alert-danger'}" role="alert" id="alertThongBao">
                    <span class="material-symbols-outlined cdl-alert-icon" aria-hidden="true">
                        ${fn:contains(thongBao, 'thành công') ? 'check_circle' : 'error'}
                    </span>
                    <div class="cdl-alert-content">
                        <div class="cdl-alert-title">Thông báo hệ thống</div>
                        <div><c:out value="${thongBao}" /></div>
                    </div>
                </div>
            </c:if>

            <!-- 3. Header Card -->
            <header class="cdl-header-card">
                <div class="cdl-header-main">
                    <div class="cdl-header-icon-wrap" aria-hidden="true">
                        <span class="material-symbols-outlined">transform</span>
                    </div>
                    <div class="cdl-header-info">
                        <div class="cdl-header-title-row">
                            <h1 class="cdl-header-title" id="tieuDeChuyenDoi">
                                Chuyển Đổi Lead Thành Khách Hàng
                            </h1>
                            <span class="cdl-badge cdl-badge-primary" id="badgeMaLead">
                                Mã Lead: <c:out value="${leadChuyenDoi.maLead != null ? leadChuyenDoi.maLead : '#' += leadChuyenDoi.leadId}" />
                            </span>
                            <c:if test="${leadChuyenDoi.trangThaiLead == 'DA_CHUYEN_DOI'}">
                                <span class="cdl-badge cdl-badge-danger" id="badgeDaChuyenDoi">
                                    <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">lock</span>
                                    Đã chuyển đổi (Khóa sửa)
                                </span>
                            </c:if>
                        </div>
                        <div class="cdl-header-sub">
                            <span class="cdl-header-sub-item">
                                <span class="material-symbols-outlined" style="font-size: 18px;" aria-hidden="true">person</span>
                                Họ tên: <strong><c:out value="${leadChuyenDoi.hoTen}" /></strong>
                            </span>
                            <span class="cdl-header-sub-item">
                                <span class="material-symbols-outlined" style="font-size: 18px;" aria-hidden="true">business</span>
                                Doanh nghiệp: <strong><c:out value="${leadChuyenDoi.tenCongTy}" /></strong>
                            </span>
                            <span class="cdl-header-sub-item">
                                <span class="material-symbols-outlined" style="font-size: 18px;" aria-hidden="true">assignment_ind</span>
                                Phụ trách: <strong><c:out value="${leadChuyenDoi.tenChuSoHuu}" /></strong>
                            </span>
                        </div>
                    </div>
                </div>
            </header>

            <!-- 4. Banner Quy trình & Bảo toàn hoạt động (AC1, AC4) -->
            <section class="cdl-process-banner" aria-label="Tóm tắt quy trình chuyển đổi">
                <div class="cdl-process-steps">
                    <div class="cdl-process-step">
                        <span class="cdl-process-step-num">1</span>
                        <span>Khách hàng doanh nghiệp</span>
                    </div>
                    <span class="material-symbols-outlined cdl-process-step-arrow" aria-hidden="true">arrow_forward</span>
                    <div class="cdl-process-step">
                        <span class="cdl-process-step-num">2</span>
                        <span>Người liên hệ</span>
                    </div>
                    <span class="material-symbols-outlined cdl-process-step-arrow" aria-hidden="true">arrow_forward</span>
                    <div class="cdl-process-step">
                        <span class="cdl-process-step-num">3</span>
                        <span>Cơ hội bán hàng</span>
                    </div>
                </div>

                <!-- AC4: Bảo toàn toàn bộ hoạt động -->
                <div class="cdl-activity-counter" id="boxBaoToanHoatDong">
                    <span class="material-symbols-outlined" style="color: var(--cdl-primary); font-size: 20px;" aria-hidden="true">history</span>
                    <span>Bảo toàn <strong>${leadChuyenDoi.activityCount}</strong> hoạt động lịch sử (AC4)</span>
                </div>
            </section>

            <!-- 5. Cảnh báo lỗi điều kiện hoặc cảnh báo trùng lặp (Q2) -->
            <c:if test="${not empty loiTruong}">
                <div class="cdl-warning-card danger" id="cardLoiDieuKien">
                    <div class="cdl-warning-header">
                        <span class="material-symbols-outlined" style="color: var(--cdl-danger); font-size: 26px;" aria-hidden="true">error</span>
                        <h2 class="cdl-warning-title" style="color: var(--cdl-danger);">Chưa đủ điều kiện chuyển đổi Lead</h2>
                    </div>
                    <p class="cdl-warning-desc">
                        Dữ liệu trên Lead nguồn còn thiếu hoặc chưa hợp lệ. Vui lòng quay lại màn hình chi tiết Lead để cập nhật trước khi chuyển đổi:
                    </p>
                    <ul class="cdl-error-list">
                        <c:forEach items="${loiTruong}" var="entry">
                            <li class="cdl-error-list-item">
                                <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">cancel</span>
                                <span><strong>${entry.key}:</strong> <c:out value="${entry.value}" /></span>
                            </li>
                        </c:forEach>
                    </ul>
                </div>
            </c:if>

            <c:if test="${not empty canhBaoTrung}">
                <div class="cdl-warning-card ${canhBaoTrung[0].xacNhanTrung ? 'danger' : ''}" id="cardCanhBaoTrung">
                    <div class="cdl-warning-header">
                        <span class="material-symbols-outlined" style="color: ${canhBaoTrung[0].xacNhanTrung ? 'var(--cdl-danger)' : 'var(--cdl-warning)'}; font-size: 26px;" aria-hidden="true">
                            ${canhBaoTrung[0].xacNhanTrung ? 'block' : 'warning'}
                        </span>
                        <h2 class="cdl-warning-title">
                            ${canhBaoTrung[0].xacNhanTrung ? 'Cảnh báo: Doanh nghiệp đã tồn tại trong CRM (Q2)' : 'Cảnh báo: Phát hiện dữ liệu có thể trùng lặp'}
                        </h2>
                    </div>
                    <p class="cdl-warning-desc">
                        ${canhBaoTrung[0].xacNhanTrung ? 'Hệ thống chặn chuyển đổi tạo mới để tránh trùng lặp khách hàng doanh nghiệp.' : 'Vui lòng kiểm tra kỹ các thông tin nghi trùng sau đây:'}
                    </p>
                    <c:forEach items="${canhBaoTrung}" var="cb">
                        <div class="cdl-duplicate-item ${cb.xacNhanTrung ? 'danger' : ''}">
                            <span class="material-symbols-outlined" style="font-size: 18px;" aria-hidden="true">
                                ${cb.xacNhanTrung ? 'dangerous' : 'info'}
                            </span>
                            <span><c:out value="${cb.chiTiet}" /></span>
                        </div>
                    </c:forEach>
                </div>
            </c:if>

            <!-- Lý do không thể chuyển đổi nếu có -->
            <c:if test="${not leadChuyenDoi.canConvert && not empty leadChuyenDoi.canConvertReasons}">
                <div class="cdl-warning-card danger" id="cardLyDoKhongTheChuyenDoi">
                    <div class="cdl-warning-header">
                        <span class="material-symbols-outlined" style="color: var(--cdl-danger); font-size: 26px;" aria-hidden="true">rule</span>
                        <h2 class="cdl-warning-title">Không Thể Thực Hiện Chuyển Đổi</h2>
                    </div>
                    <ul class="cdl-error-list">
                        <c:forEach items="${leadChuyenDoi.canConvertReasons}" var="reason">
                            <li class="cdl-error-list-item">
                                <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">close</span>
                                <span><c:out value="${reason}" /></span>
                            </li>
                        </c:forEach>
                    </ul>
                </div>
            </c:if>

            <!-- 6. LƯỚI 3 CARD XEM TRƯỚC CHỈ ĐỌC (AC1, AC2) -->
            <div class="cdl-preview-grid">

                <!-- CARD 1: Khách Hàng Doanh Nghiệp Mới -->
                <article class="cdl-card" id="cardPreviewKhachHang">
                    <div class="cdl-card-header">
                        <div class="cdl-card-title-group">
                            <div class="cdl-card-icon customer" aria-hidden="true">
                                <span class="material-symbols-outlined">corporate_fare</span>
                            </div>
                            <h2 class="cdl-card-title">1. Khách Hàng Mới</h2>
                        </div>
                        <span class="cdl-badge cdl-badge-primary">Doanh nghiệp</span>
                    </div>
                    <div class="cdl-card-body">
                        <div class="cdl-field-row">
                            <span class="cdl-field-label">
                                <span>Tên công ty / Doanh nghiệp</span>
                                <span class="cdl-field-source">Từ Lead</span>
                            </span>
                            <div class="cdl-field-value highlight" id="valPreviewTenCongTy">
                                <c:out value="${thongTinTaoKhachHang.tenCongTy}" />
                            </div>
                        </div>

                        <div class="cdl-field-row">
                            <span class="cdl-field-label">
                                <span>Mã số thuế</span>
                                <span class="cdl-field-source">Từ Lead</span>
                            </span>
                            <div class="cdl-field-value" id="valPreviewMaSoThue">
                                <c:out value="${not empty thongTinTaoKhachHang.maSoThue ? thongTinTaoKhachHang.maSoThue : 'Chưa có'}" />
                            </div>
                        </div>

                        <div class="cdl-field-row">
                            <span class="cdl-field-label">
                                <span>Website</span>
                                <span class="cdl-field-source">Từ Lead</span>
                            </span>
                            <div class="cdl-field-value" id="valPreviewWebsite">
                                <c:out value="${not empty thongTinTaoKhachHang.website ? thongTinTaoKhachHang.website : 'Chưa có'}" />
                            </div>
                        </div>

                        <div class="cdl-field-row">
                            <span class="cdl-field-label">
                                <span>Trạng thái ban đầu</span>
                                <span class="cdl-field-source">Mặc định</span>
                            </span>
                            <div class="cdl-field-value" id="valPreviewTrangThaiKhachHang">
                                Tiềm năng
                            </div>
                        </div>

                        <div class="cdl-field-row">
                            <span class="cdl-field-label">
                                <span>Người phụ trách sở hữu</span>
                                <span class="cdl-field-source">Kế thừa Lead (Q5)</span>
                            </span>
                            <div class="cdl-field-value" id="valPreviewNguoiSoHuuKhachHang">
                                <c:out value="${leadChuyenDoi.tenChuSoHuu}" />
                            </div>
                        </div>
                    </div>
                </article>

                <!-- CARD 2: Người Liên Hệ Mới -->
                <article class="cdl-card" id="cardPreviewNguoiLienHe">
                    <div class="cdl-card-header">
                        <div class="cdl-card-title-group">
                            <div class="cdl-card-icon contact" aria-hidden="true">
                                <span class="material-symbols-outlined">person_pin</span>
                            </div>
                            <h2 class="cdl-card-title">2. Người Liên Hệ Mới</h2>
                        </div>
                        <span class="cdl-badge cdl-badge-success">Đầu mối chính</span>
                    </div>
                    <div class="cdl-card-body">
                        <div class="cdl-field-row">
                            <span class="cdl-field-label">
                                <span>Họ và tên người liên hệ</span>
                                <span class="cdl-field-source">Từ Lead</span>
                            </span>
                            <div class="cdl-field-value highlight" id="valPreviewHoTenLienHe">
                                <c:out value="${thongTinTaoLienHe.hoTen}" />
                            </div>
                        </div>

                        <div class="cdl-field-row">
                            <span class="cdl-field-label">
                                <span>Chức danh</span>
                                <span class="cdl-field-source">Từ Lead</span>
                            </span>
                            <div class="cdl-field-value" id="valPreviewChucDanh">
                                <c:out value="${not empty thongTinTaoLienHe.chucDanh ? thongTinTaoLienHe.chucDanh : 'Đại diện liên hệ'}" />
                            </div>
                        </div>

                        <div class="cdl-field-row">
                            <span class="cdl-field-label">
                                <span>Địa chỉ Email</span>
                                <span class="cdl-field-source">Từ Lead</span>
                            </span>
                            <div class="cdl-field-value" id="valPreviewEmail">
                                <c:out value="${not empty thongTinTaoLienHe.email ? thongTinTaoLienHe.email : 'Chưa có'}" />
                            </div>
                        </div>

                        <div class="cdl-field-row">
                            <span class="cdl-field-label">
                                <span>Số điện thoại</span>
                                <span class="cdl-field-source">Từ Lead</span>
                            </span>
                            <div class="cdl-field-value" id="valPreviewSoDienThoai">
                                <c:out value="${not empty thongTinTaoLienHe.soDienThoai ? thongTinTaoLienHe.soDienThoai : 'Chưa có'}" />
                            </div>
                        </div>

                        <div class="cdl-field-row">
                            <span class="cdl-field-label">
                                <span>Vai trò liên hệ</span>
                                <span class="cdl-field-source">Tự động gắn</span>
                            </span>
                            <div class="cdl-field-value" id="valPreviewVaiTroLienHe">
                                Người liên hệ chính của cơ hội
                            </div>
                        </div>
                    </div>
                </article>

                <!-- CARD 3: Cơ Hội Bán Hàng Mới (Mặc định Server theo Q3) -->
                <article class="cdl-card" id="cardPreviewCoHoi">
                    <div class="cdl-card-header">
                        <div class="cdl-card-title-group">
                            <div class="cdl-card-icon opportunity" aria-hidden="true">
                                <span class="material-symbols-outlined">trending_up</span>
                            </div>
                            <h2 class="cdl-card-title">3. Cơ Hội Mới</h2>
                        </div>
                        <span class="cdl-badge cdl-badge-purple">Tự động thiết lập (Q3)</span>
                    </div>
                    <div class="cdl-card-body">
                        <div class="cdl-field-row">
                            <span class="cdl-field-label">
                                <span>Tên cơ hội bán hàng</span>
                                <span class="cdl-field-source">Server tự sinh (Q3)</span>
                            </span>
                            <div class="cdl-field-value opportunity-auto" id="valPreviewTenCoHoi">
                                <c:out value="${thongTinTaoCoHoi.tenCoHoi}" />
                            </div>
                            <span class="cdl-field-hint">
                                <span class="material-symbols-outlined" style="font-size: 14px;">info</span>
                                Cấu trúc: "Cơ hội - [Tên doanh nghiệp]"
                            </span>
                        </div>

                        <div class="cdl-field-row">
                            <span class="cdl-field-label">
                                <span>Giai đoạn Pipeline ban đầu</span>
                                <span class="cdl-field-source">Giai đoạn đầu tiên (Q3)</span>
                            </span>
                            <div class="cdl-field-value" id="valPreviewGiaiDoan">
                                <c:out value="${thongTinTaoCoHoi.tenGiaiDoan}" />
                            </div>
                        </div>

                        <div class="cdl-field-row">
                            <span class="cdl-field-label">
                                <span>Giá trị dự kiến</span>
                                <span class="cdl-field-source">Mặc định 0 ₫ (Q3)</span>
                            </span>
                            <div class="cdl-field-value" id="valPreviewGiaTriDuKien">
                                0 ₫ (Chưa ước tính)
                            </div>
                        </div>

                        <div class="cdl-field-row">
                            <span class="cdl-field-label">
                                <span>Ngày dự kiến chốt</span>
                                <span class="cdl-field-source">+30 ngày lịch (Q3)</span>
                            </span>
                            <div class="cdl-field-value" id="valPreviewNgayChotDuKien">
                                <%= formatDate(((vn.nhom10.crm.dto.ChuyenDoiLeadPreviewDTO.CoHoiPreviewDTO) request.getAttribute("thongTinTaoCoHoi")).getNgayChotDuKien()) %>
                            </div>
                        </div>

                        <div class="cdl-field-row">
                            <span class="cdl-field-label">
                                <span>Nguồn thu thập</span>
                                <span class="cdl-field-source">Từ Lead</span>
                            </span>
                            <div class="cdl-field-value" id="valPreviewNguonCoHoi">
                                <c:out value="${not empty thongTinTaoCoHoi.nguon ? thongTinTaoCoHoi.nguon : 'Trực tiếp'}" />
                            </div>
                        </div>
                    </div>
                </article>

            </div>

            <!-- 7. THANH HÀNH ĐỘNG XÁC NHẬN CHUYỂN ĐỔI (STICKY ACTION BAR) -->
            <footer class="cdl-action-bar">
                <div class="cdl-action-note">
                    <span class="material-symbols-outlined" style="font-size: 20px; color: var(--cdl-primary); flex-shrink: 0;" aria-hidden="true">verified_user</span>
                    <span>
                        Sau khi nhấn <strong>"Xác nhận chuyển đổi"</strong>, hệ thống sẽ thực hiện tạo đồng thời 3 bản ghi trong 1 giao dịch duy nhất (AC1) và khóa Lead ở trạng thái <strong>"Đã chuyển đổi"</strong> (AC3).
                    </span>
                </div>

                <div class="cdl-action-buttons">
                    <a href="${pageContext.request.contextPath}/lead/chi-tiet?leadId=${leadChuyenDoi.leadId}" class="cdl-btn cdl-btn-outline" id="btnHuyChuyenDoi">
                        Hủy bỏ
                    </a>

                    <c:choose>
                        <%-- Trường hợp có thể chuyển đổi --%>
                        <c:when test="${leadChuyenDoi.canConvert && quyenChuyenDoi && (empty canhBaoTrung || not canhBaoTrung[0].xacNhanTrung)}">
                            <form action="${pageContext.request.contextPath}/lead/chuyen-doi" method="POST" id="formChuyenDoiLead" style="margin: 0;">
                                <!-- Chỉ gửi duy nhất ID Lead và CSRF Token theo đúng chuẩn Contract v1.1 -->
                                <input type="hidden" name="leadId" value="${leadChuyenDoi.leadId}">
                                <input type="hidden" name="csrfToken" value="${csrfToken}">

                                <button type="submit" class="cdl-btn cdl-btn-success" id="btnXacNhanChuyenDoi" title="Xác nhận chuyển đổi Lead thành Khách hàng, Liên hệ và Cơ hội">
                                    <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                                    <span>Xác nhận chuyển đổi</span>
                                </button>
                            </form>
                        </c:when>

                        <%-- Trường hợp thiếu thông tin Lead --%>
                        <c:when test="${not leadChuyenDoi.canConvert}">
                            <a href="${pageContext.request.contextPath}/lead/chi-tiet?leadId=${leadChuyenDoi.leadId}" class="cdl-btn cdl-btn-primary" id="btnSuaLeadTruocKhiChuyenDoi" title="Bổ sung thông tin Lead">
                                <span class="material-symbols-outlined" aria-hidden="true">edit</span>
                                <span>Quay lại sửa Lead trước</span>
                            </a>
                        </c:when>

                        <%-- Trường hợp trùng hoặc bị khóa --%>
                        <c:otherwise>
                            <button type="button" class="cdl-btn cdl-btn-outline" disabled title="Không thể chuyển đổi do vi phạm điều kiện hoặc trùng lặp dữ liệu">
                                <span class="material-symbols-outlined" aria-hidden="true">block</span>
                                <span>Không thể chuyển đổi</span>
                            </button>
                        </c:otherwise>
                    </c:choose>
                </div>
            </footer>

        </div>
    </main>

    <!-- Script tương tác client -->
    <script src="${pageContext.request.contextPath}/assets/js/lead/chuyen-doi.js"></script>
</body>
</html>
