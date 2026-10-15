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
    <!-- CSS Chuyên biệt Module Lead (Story S4-02) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lead/lead.css?v=2.4">
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

            <!-- Thông báo thành công từ thao tác trước (Tạo lead thủ công / Nhập Excel) -->
            <c:if test="${param.thanhCong == 'tao-moi'}">
                <div class="lead-pill-badge lead-pill-valid" style="width: 100%; padding: 12px 16px; margin-bottom: 20px; border-radius: 8px; font-size: 13.5px;" role="alert">
                    <span class="material-symbols-outlined" style="font-size: 18px;">check_circle</span>
                    <span>Tạo khách hàng tiềm năng thành công! Mã Lead: <strong><c:out value="${param.maLead}" /></strong></span>
                </div>
            </c:if>

            <!-- 2. Header Card với các nút hành động chính của Story S4-02 -->
            <header class="lead-header-card lead-page-header">
                <div class="lead-header-title lead-title-wrap">
                    <h1>
                        <span class="material-symbols-outlined" style="color: var(--lead-primary); font-size: 28px;" aria-hidden="true">target</span>
                        <span>Khách Hàng Tiềm Năng (Leads)</span>
                    </h1>
                    <p>Quản lý toàn bộ danh sách lead tiếp nhận từ hội thảo, danh thiếp sự kiện, nhập hàng loạt từ Excel hoặc qua biểu mẫu nhúng website (Epic EP-04 / Story S4-02).</p>
                </div>
                <div class="lead-header-actions">
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
                                <th style="width: 180px;">Nguồn Lead (AC3)</th>
                                <th>Tên Công Ty</th>
                                <th style="width: 140px;">Số Điện Thoại</th>
                                <th>Email</th>
                                <th style="width: 120px; text-align: center;">Trạng Thái</th>
                                <th style="width: 140px; text-align: right;">Thời Gian Tạo</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty dsLead}">
                                    <c:forEach var="lead" items="${dsLead}">
                                        <tr class="row-valid">
                                            <td class="font-mono">
                                                <strong style="color: var(--lead-primary);"><c:out value="${lead.maLead}" /></strong>
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
                                            <td style="text-align: center;">
                                                <span class="lead-badge lead-badge-valid lead-pill-badge lead-pill-valid">
                                                    <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">fiber_new</span>
                                                    <c:out value="${lead.trangThai != null ? lead.trangThai.moTa : 'Mới'}" />
                                                </span>
                                            </td>
                                            <td style="text-align: right; font-size: 12.5px; color: var(--lead-neutral-500);">
                                                <c:out value="${lead.createdAt != null ? lead.createdAt : 'Vừa xong'}" />
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <!-- Trạng thái rỗng chuẩn khi chưa có Lead nào trong hệ thống -->
                                    <tr>
                                        <td colspan="8">
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
</body>
</html>
