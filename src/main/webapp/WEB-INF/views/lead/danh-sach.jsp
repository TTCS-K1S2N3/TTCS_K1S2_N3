<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Quản lý danh sách khách hàng tiềm năng (Lead) - CRM Bán Hàng Nhóm 10">
    <title>Danh Sách Khách Hàng Tiềm Năng (Lead) - CRM Bán Hàng</title>
    <!-- CSS Hệ thống chung (Navigation & Typography) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <!-- CSS Chuyên biệt Module Lead (Story S4-02 & S4-05) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lead/lead.css?v=2.5">
</head>
<body class="crm-body">

    <!-- Navigation & Sidebar dùng chung toàn hệ thống -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <div class="lead-container">

            <!-- 1. Breadcrumb điều hướng -->
            <nav class="lead-breadcrumb" aria-label="Đường dẫn điều hướng">
                <ol class="lead-breadcrumb-nav">
                    <li><a href="${pageContext.request.contextPath}/dieu-huong" class="lead-breadcrumb-link">Trang chủ</a></li>
                    <span class="lead-breadcrumb-sep" aria-hidden="true">/</span>
                    <li class="lead-breadcrumb-current" aria-current="page">Lead & Phân bổ</li>
                </ol>
            </nav>

            <!-- Thông báo thành công từ thao tác trước -->
            <c:if test="${param.thanhCong == 'tao-moi'}">
                <div class="lead-pill-badge lead-pill-valid" style="width: 100%; padding: 12px 16px; margin-bottom: 20px; border-radius: 8px; font-size: 13.5px;" role="alert">
                    <span class="material-symbols-outlined" style="font-size: 18px;">check_circle</span>
                    <span>Tạo khách hàng tiềm năng thành công! Mã Lead: <strong><c:out value="${param.maLead}" /></strong></span>
                </div>
            </c:if>
            <c:if test="${param.thanhCong == 'cap-nhat'}">
                <div class="lead-pill-badge lead-pill-valid" style="width: 100%; padding: 12px 16px; margin-bottom: 20px; border-radius: 8px; font-size: 13.5px;" role="alert">
                    <span class="material-symbols-outlined" style="font-size: 18px;">check_circle</span>
                    <span>Cập nhật khách hàng tiềm năng thành công! Điểm và phân loại đã được tự động tính lại. Mã Lead: <strong><c:out value="${param.maLead}" /></strong></span>
                </div>
            </c:if>
            <c:if test="${param.thanhCong == 'xoa'}">
                <div class="lead-pill-badge lead-pill-valid" style="width: 100%; padding: 12px 16px; margin-bottom: 20px; border-radius: 8px; font-size: 13.5px;" role="alert">
                    <span class="material-symbols-outlined" style="font-size: 18px;">delete</span>
                    <span>Đã xóa khách hàng tiềm năng thành công khỏi hệ thống!</span>
                </div>
            </c:if>
            <c:if test="${param.thanhCong == 'cham-diem'}">
                <div class="lead-pill-badge lead-pill-valid" style="width: 100%; padding: 12px 16px; margin-bottom: 20px; border-radius: 8px; font-size: 13.5px;" role="alert">
                    <span class="material-symbols-outlined" style="font-size: 18px;">award_star</span>
                    <span>Đã chấm điểm và cập nhật phân loại thành công cho Lead: <strong><c:out value="${param.maLead}" /></strong>!</span>
                </div>
            </c:if>
            <c:if test="${param.thanhCong == 'tinh-lai-diem'}">
                <div class="lead-pill-badge lead-pill-valid" style="width: 100%; padding: 12px 16px; margin-bottom: 20px; border-radius: 8px; font-size: 13.5px;" role="alert">
                    <span class="material-symbols-outlined" style="font-size: 18px;">award_star</span>
                    <span>Đã tính lại điểm và cập nhật phân loại Nóng/Ấm/Lạnh thành công cho toàn bộ khách hàng tiềm năng (Story S4-05)<c:if test="${not empty param.count}"> (Tổng cộng: <strong><c:out value="${param.count}" /></strong> lead)</c:if>!</span>
                </div>
            </c:if>
            <c:if test="${param.loi == 'xoa-that-bai'}">
                <div class="lead-pill-badge lead-pill-error" style="width: 100%; padding: 12px 16px; margin-bottom: 20px; border-radius: 8px; font-size: 13.5px;" role="alert">
                    <span class="material-symbols-outlined" style="font-size: 18px;">error</span>
                    <span>Không thể xóa khách hàng tiềm năng. Vui lòng kiểm tra lại liên kết dữ liệu.</span>
                </div>
            </c:if>

            <!-- 2. Header Card với các nút hành động chính của Story S4-02 & Story S4-05 -->
            <header class="lead-header-card lead-page-header">
                <div class="lead-header-title lead-title-wrap">
                    <h1>
                        <span class="material-symbols-outlined" style="color: var(--lead-primary); font-size: 28px;" aria-hidden="true">target</span>
                        <span>Khách Hàng Tiềm Năng (Leads)</span>
                    </h1>
                    <p>Quản lý toàn bộ danh sách lead tiếp nhận từ hội thảo, danh thiếp sự kiện, nhập hàng loạt từ Excel hoặc qua biểu mẫu nhúng website (Epic EP-04 / Story S4-02 & S4-05).</p>
                </div>
                <div class="lead-header-actions">
                    <!-- Nút Chấm điểm của phần S4-05 -->
                    <a href="${pageContext.request.contextPath}/lead/cham-diem" class="lead-btn lead-btn-outline" id="btnChamDiemLead" title="Cấu hình quy tắc chấm điểm và phân loại Lead (Story S4-05)">
                        <span class="material-symbols-outlined" style="color: #7c3aed;" aria-hidden="true">award_star</span>
                        <span>Cấu hình chấm điểm</span>
                    </a>
                    <!-- Nút Tính lại điểm toàn bộ S4-05 -->
                    <form action="${pageContext.request.contextPath}/lead/tinh-lai-diem" method="POST" style="display: inline-flex; margin: 0;">
                        <button type="submit" class="lead-btn lead-btn-outline" id="btnTinhLaiDiem" title="Chấm điểm lại toàn bộ danh sách Lead theo quy tắc hiện tại (Story S4-05)">
                            <span class="material-symbols-outlined" aria-hidden="true">refresh</span>
                            <span>Tính lại điểm</span>
                        </button>
                    </form>
                    <a href="${pageContext.request.contextPath}/bieu-mau" class="lead-btn lead-btn-outline" id="btnBieuMauNhung" title="Quản lý biểu mẫu nhúng website">
                        <span class="material-symbols-outlined" aria-hidden="true">dynamic_form</span>
                        <span>Biểu mẫu nhúng</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/lead/import-excel" class="lead-btn lead-btn-outline" id="btnNhapExcel" title="Nhập hàng loạt lead từ tệp Excel (AC2)">
                        <span class="material-symbols-outlined" aria-hidden="true">upload_file</span>
                        <span>Nhập Excel</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/lead/tao-moi" class="lead-btn lead-btn-primary" id="btnTaoLeadMoi" title="Tạo thủ công lead từ sự kiện hoặc danh thiếp (AC1)">
                        <span class="material-symbols-outlined" aria-hidden="true">add</span>
                        <span>Tạo Lead thủ công</span>
                    </a>
                </div>
            </header>

            <!-- 3. Thẻ thống kê tổng quan nhanh -->
            <c:set var="tongLead" value="${not empty dsLead ? dsLead.size() : 0}" />
            <section class="lead-stats-summary lead-stats-row" aria-label="Thống kê Lead">
                <div class="lead-stat-card lead-stat-box">
                    <div class="lead-stat-icon total">
                        <span class="material-symbols-outlined" aria-hidden="true">groups</span>
                    </div>
                    <div>
                        <span class="lead-stat-number lead-stat-num"><c:out value="${tongLead}" /></span>
                        <span class="lead-stat-label lead-stat-lbl">Tổng khách hàng tiềm năng</span>
                    </div>
                </div>

                <div class="lead-stat-card lead-stat-box">
                    <div class="lead-stat-icon valid">
                        <span class="material-symbols-outlined" aria-hidden="true">fiber_new</span>
                    </div>
                    <div>
                        <span class="lead-stat-number lead-stat-num" style="color: var(--lead-success);"><c:out value="${tongLead}" /></span>
                        <span class="lead-stat-label lead-stat-lbl">Trạng thái: Mới tiếp nhận</span>
                    </div>
                </div>

                <div class="lead-stat-card lead-stat-box">
                    <div class="lead-stat-icon error">
                        <span class="material-symbols-outlined" aria-hidden="true">verified_user</span>
                    </div>
                    <div>
                        <span class="lead-stat-number lead-stat-num" style="color: var(--lead-warning-text);"><c:out value="${tongLead > 0 ? '100%' : '0'}" /></span>
                        <span class="lead-stat-label lead-stat-lbl">Đã gắn đúng Nguồn (AC3)</span>
                    </div>
                </div>
            </section>

            <!-- 4. Danh sách Lead -->
            <section class="lead-form-card lead-main-card">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; flex-wrap: wrap; gap: 10px;">
                    <h2 class="lead-section-title" style="margin-bottom: 0;">
                        <span class="material-symbols-outlined" aria-hidden="true">list_alt</span>
                        <span>Danh sách Lead tiếp nhận</span>
                    </h2>
                </div>

                <div class="lead-table-container lead-preview-wrap">
                    <table class="lead-preview-table lead-table-modern" aria-label="Bảng dữ liệu Lead">
                        <thead>
                            <tr>
                                <th style="width: 120px;">Mã Lead</th>
                                <th>Họ và Tên</th>
                                <th style="width: 160px;">Nguồn Lead (AC3)</th>
                                <th>Tên Công Ty</th>
                                <th style="width: 130px;">Số Điện Thoại</th>
                                <th>Email</th>
                                <th style="width: 130px; text-align: center;">Điểm (S4-05)</th>
                                <th style="width: 110px; text-align: center;">Trạng Thái</th>
                                <th style="width: 130px; text-align: right;">Thời Gian Tạo</th>
                                <th style="width: 210px; text-align: center;">Thao Tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty dsLead}">
                                    <c:forEach var="lead" items="${dsLead}">
                                        <tr class="row-valid">
                                            <td class="font-mono">
                                                <a href="${pageContext.request.contextPath}/lead/chi-tiet?leadId=${lead.id}"
                                                   class="lead-code-link"
                                                   title="Xem chi tiết khách hàng tiềm năng ${lead.maLead}">
                                                    <strong><c:out value="${lead.maLead}" /></strong>
                                                </a>
                                            </td>
                                            <td>
                                                <strong style="color: var(--lead-neutral-900);"><c:out value="${lead.hoTen}" /></strong>
                                            </td>
                                            <td>
                                                <span class="lead-badge lead-badge-source lead-pill-badge lead-pill-source">
                                                    <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">label</span>
                                                    <c:out value="${not empty lead.tenNguonLead ? lead.tenNguonLead : 'Sự kiện / Hội thảo'}" />
                                                </span>
                                            </td>
                                            <td>
                                                <c:out value="${not empty lead.tenCongTy ? lead.tenCongTy : '—'}" />
                                            </td>
                                            <td class="font-mono">
                                                <c:out value="${not empty lead.soDienThoai ? lead.soDienThoai : '—'}" />
                                            </td>
                                            <td>
                                                <c:out value="${not empty lead.email ? lead.email : '—'}" />
                                            </td>
                                            <!-- Cột Điểm & Phân loại của Story S4-05 -->
                                            <td style="text-align: center;">
                                                <c:choose>
                                                    <c:when test="${lead.phanLoai == 'NONG'}">
                                                        <span class="lead-badge lead-badge-nong" title="Điểm: ${lead.diem} - Phân loại: Nóng">
                                                            <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">local_fire_department</span>
                                                            <strong><c:out value="${lead.diem}" />đ</strong> • Nóng
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${lead.phanLoai == 'AM'}">
                                                        <span class="lead-badge lead-badge-am" title="Điểm: ${lead.diem} - Phân loại: Ấm">
                                                            <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">wb_sunny</span>
                                                            <strong><c:out value="${lead.diem}" />đ</strong> • Ấm
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${lead.phanLoai == 'LANH'}">
                                                        <span class="lead-badge lead-badge-lanh" title="Điểm: ${lead.diem} - Phân loại: Lạnh">
                                                            <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">ac_unit</span>
                                                            <strong><c:out value="${lead.diem}" />đ</strong> • Lạnh
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="lead-badge lead-badge-source" title="Điểm: ${lead.diem}">
                                                            <span class="material-symbols-outlined" style="font-size: 13px;" aria-hidden="true">award_star</span>
                                                            <strong><c:out value="${lead.diem}" />đ</strong>
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td style="text-align: center;">
                                                <span class="lead-badge lead-badge-valid lead-pill-badge lead-pill-valid">
                                                    <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">fiber_new</span>
                                                    <c:out value="${lead.trangThai != null ? lead.trangThai.moTa : 'Mới'}" />
                                                </span>
                                            </td>
                                            <td style="text-align: right; font-size: 12.5px; color: var(--lead-neutral-500);">
                                                <c:out value="${lead.createdAt != null ? lead.createdAt : 'Vừa xong'}" />
                                            </td>
                                            <!-- Cột Thao tác đầy đủ: Chi tiết, Chấm điểm, Sửa, Xóa -->
                                            <td style="text-align: center; white-space: nowrap;">
                                                <div style="display: inline-flex; align-items: center; justify-content: center; gap: 5px;">
                                                    <!-- Chi tiết -->
                                                    <a href="${pageContext.request.contextPath}/lead/chi-tiet?leadId=${lead.id}"
                                                       class="lead-btn-action"
                                                       id="btnXemChiTiet_${lead.id}"
                                                       title="Xem chi tiết Lead ${lead.maLead}">
                                                        <span class="material-symbols-outlined" style="font-size: 15px;" aria-hidden="true">visibility</span>
                                                        <span>Chi tiết</span>
                                                    </a>

                                                    <!-- Chấm điểm 1 Lead (S4-05) -->
                                                    <form action="${pageContext.request.contextPath}/lead/cham-diem-lead" method="POST" style="display: inline; margin: 0;">
                                                        <input type="hidden" name="id" value="${lead.id}">
                                                        <button type="submit"
                                                                class="lead-btn-action lead-btn-score"
                                                                id="btnChamDiem_${lead.id}"
                                                                title="Chấm điểm lại Lead ${lead.maLead} theo quy tắc S4-05">
                                                            <span class="material-symbols-outlined" style="font-size: 15px;" aria-hidden="true">award_star</span>
                                                            <span>Chấm điểm</span>
                                                        </button>
                                                    </form>

                                                    <!-- Sửa (S4-02) -->
                                                    <a href="${pageContext.request.contextPath}/lead/sua?id=${lead.id}"
                                                       class="lead-btn-action lead-btn-edit"
                                                       id="btnSuaLead_${lead.id}"
                                                       title="Sửa thông tin Lead ${lead.maLead}">
                                                        <span class="material-symbols-outlined" style="font-size: 15px;" aria-hidden="true">edit</span>
                                                        <span>Sửa</span>
                                                    </a>

                                                    <!-- Xóa (S4-02) -->
                                                    <button type="button"
                                                            class="lead-btn-action lead-btn-delete"
                                                            id="btnXoaLead_${lead.id}"
                                                            onclick="xacNhanXoaLead('${lead.id}', '<c:out value="${lead.hoTen}" />', '<c:out value="${lead.maLead}" />')"
                                                            title="Xóa Lead ${lead.maLead}">
                                                        <span class="material-symbols-outlined" style="font-size: 15px;" aria-hidden="true">delete</span>
                                                        <span>Xóa</span>
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <!-- Trạng thái rỗng chuẩn khi chưa có Lead nào trong hệ thống -->
                                    <tr>
                                        <td colspan="10">
                                            <div class="lead-empty-state">
                                                <div class="lead-empty-icon" aria-hidden="true">
                                                    <span class="material-symbols-outlined">person_search</span>
                                                </div>
                                                <h3 class="lead-empty-title">Chưa có khách hàng tiềm năng</h3>
                                                <p class="lead-empty-desc">
                                                    Bắt đầu tiếp nhận lead bằng cách tạo thủ công từ danh thiếp sự kiện hoặc nhập hàng loạt từ tệp Excel.
                                                </p>
                                                <div class="lead-empty-actions">
                                                    <a href="${pageContext.request.contextPath}/lead/tao-moi" class="lead-btn lead-btn-primary lead-btn-sm" id="btnEmptyTaoLead">
                                                        <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">add</span>
                                                        <span>Tạo Lead thủ công</span>
                                                    </a>
                                                    <a href="${pageContext.request.contextPath}/lead/import-excel" class="lead-btn lead-btn-outline lead-btn-sm" id="btnEmptyNhapExcel">
                                                        <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">upload_file</span>
                                                        <span>Nhập từ Excel</span>
                                                    </a>
                                                    <a href="${pageContext.request.contextPath}/lead/cham-diem" class="lead-btn lead-btn-outline lead-btn-sm" id="btnEmptyChamDiem">
                                                        <span class="material-symbols-outlined" style="font-size: 16px; color: #7c3aed;" aria-hidden="true">award_star</span>
                                                        <span>Cấu hình chấm điểm</span>
                                                    </a>
                                                </div>
                                            </div>
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </section>
        </div>
    </main>

    <!-- Modal Xác nhận Xóa Lead (Story S4-02) -->
    <div id="modalXoaLead" class="lead-modal-backdrop" style="display: none;" role="dialog" aria-modal="true" aria-labelledby="modalXoaTieuDe">
        <div class="lead-modal-card">
            <div class="lead-modal-header" style="display: flex; align-items: center; gap: 8px; color: #dc2626; border-bottom: 1px solid var(--lead-neutral-100); padding-bottom: 12px;">
                <span class="material-symbols-outlined" style="font-size: 26px;">warning</span>
                <h3 id="modalXoaTieuDe" style="margin: 0; font-size: 17px; font-weight: 700;">Xác nhận xóa khách hàng tiềm năng</h3>
            </div>
            <div class="lead-modal-body" style="padding: 18px 0; color: #374151; line-height: 1.5; font-size: 14px;">
                Bạn có chắc chắn muốn xóa khách hàng tiềm năng <strong id="tenLeadXoa" style="color: #111827;"></strong> (Mã: <span id="maLeadXoa" style="font-family: monospace; font-weight: bold; color: var(--lead-primary);"></span>) không?
                <p style="margin: 10px 0 0; color: #b91c1c; font-size: 12.5px;">
                    <span class="material-symbols-outlined" style="font-size: 14px; vertical-align: -2px;">info</span>
                    Thao tác này sẽ xóa vĩnh viễn lead khỏi hệ thống và không thể hoàn tác.
                </p>
            </div>
            <div class="lead-modal-footer" style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 8px; border-top: 1px solid var(--lead-neutral-100); padding-top: 14px;">
                <button type="button" class="lead-btn lead-btn-outline" onclick="dongModalXoaLead()">Hủy bỏ</button>
                <form action="${pageContext.request.contextPath}/lead/xoa" method="POST" id="formXacNhanXoa" style="margin: 0;">
                    <input type="hidden" name="id" id="idLeadCanXoa" value="">
                    <button type="submit" class="lead-btn lead-btn-danger" id="btnDongYXoa">
                        <span class="material-symbols-outlined" style="font-size: 16px;">delete</span>
                        <span>Xác nhận xóa</span>
                    </button>
                </form>
            </div>
        </div>
    </div>

    <!-- Script xử lý Modal Xác nhận Xóa -->
    <script>
        function xacNhanXoaLead(id, hoTen, maLead) {
            document.getElementById('idLeadCanXoa').value = id;
            document.getElementById('tenLeadXoa').textContent = hoTen || 'Lead #' + id;
            document.getElementById('maLeadXoa').textContent = maLead || '#' + id;
            var modal = document.getElementById('modalXoaLead');
            modal.style.display = 'flex';
        }

        function dongModalXoaLead() {
            var modal = document.getElementById('modalXoaLead');
            modal.style.display = 'none';
        }

        // Đóng modal khi nhấn phím Escape
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                dongModalXoaLead();
            }
        });

        // Đóng modal khi click ra ngoài backdrop
        document.getElementById('modalXoaLead').addEventListener('click', function(e) {
            if (e.target === this) {
                dongModalXoaLead();
            }
        });
    </script>
</body>
</html>
