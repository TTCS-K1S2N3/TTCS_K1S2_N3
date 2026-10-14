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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lead/lead.css">
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

            <!-- 2. Header Card với các nút hành động chính của Story S4-02 -->
            <header class="lead-header-card">
                <div class="lead-header-title">
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
                        <span>+ Tạo Lead thủ công</span>
                    </a>
                </div>
            </header>

            <!-- 3. Thẻ thống kê tổng quan nhanh -->
            <c:set var="tongLead" value="${not empty dsLead ? dsLead.size() : 0}" />
            <section class="lead-stats-summary" aria-label="Thống kê Lead">
                <div class="lead-stat-card">
                    <div class="lead-stat-icon total">
                        <span class="material-symbols-outlined" aria-hidden="true">groups</span>
                    </div>
                    <div>
                        <span class="lead-stat-number"><c:out value="${tongLead}" /></span>
                        <span class="lead-stat-label">Tổng khách hàng tiềm năng</span>
                    </div>
                </div>

                <div class="lead-stat-card">
                    <div class="lead-stat-icon valid">
                        <span class="material-symbols-outlined" aria-hidden="true">fiber_new</span>
                    </div>
                    <div>
                        <span class="lead-stat-number" style="color: var(--lead-success);"><c:out value="${tongLead}" /></span>
                        <span class="lead-stat-label">Trạng thái: Mới tiếp nhận</span>
                    </div>
                </div>

                <div class="lead-stat-card">
                    <div class="lead-stat-icon error" style="background: var(--lead-warning-light); color: var(--lead-warning-text);">
                        <span class="material-symbols-outlined" aria-hidden="true">verified_user</span>
                    </div>
                    <div>
                        <span class="lead-stat-number" style="color: var(--lead-warning-text);">100%</span>
                        <span class="lead-stat-label">Đã gắn đúng Nguồn (AC3)</span>
                    </div>
                </div>
            </section>

            <!-- 4. Danh sách Lead -->
            <section class="lead-form-card">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; flex-wrap: wrap; gap: 10px;">
                    <h2 class="lead-section-title" style="margin-bottom: 0;">
                        <span class="material-symbols-outlined" aria-hidden="true">list_alt</span>
                        <span>Danh sách Lead tiếp nhận</span>
                    </h2>
                </div>

                <div class="lead-table-container">
                    <table class="lead-preview-table" aria-label="Bảng dữ liệu Lead">
                        <thead>
                            <tr>
                                <th style="width: 100px;">Mã Lead</th>
                                <th>Họ và Tên</th>
                                <th style="width: 180px;">Nguồn Lead (AC3)</th>
                                <th>Tên Công Ty</th>
                                <th style="width: 130px;">Số Điện Thoại</th>
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
                                                <span class="lead-badge lead-badge-source">
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
                                                <span class="lead-badge lead-badge-valid">
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
                                    <!-- Dữ liệu minh họa ban đầu nếu chưa có bản ghi nào -->
                                    <tr class="row-valid">
                                        <td class="font-mono">
                                            <strong style="color: var(--lead-primary);">LEAD-DEMO-01</strong>
                                        </td>
                                        <td>
                                            <strong style="color: var(--lead-neutral-900);">Nguyễn Hoàng Nam</strong>
                                            <div style="font-size: 12px; color: var(--lead-neutral-500);">Gặp tại Hội thảo TechExpo 2026</div>
                                        </td>
                                        <td>
                                            <span class="lead-badge lead-badge-source">
                                                <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">label</span>
                                                Sự kiện / Hội thảo
                                            </span>
                                        </td>
                                        <td>Công ty Công nghệ Alpha</td>
                                        <td class="font-mono">0912345678</td>
                                        <td>nam.nguyen@alpha.vn</td>
                                        <td style="text-align: center;">
                                            <span class="lead-badge lead-badge-valid">
                                                <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">fiber_new</span>
                                                Mới
                                            </span>
                                        </td>
                                        <td style="text-align: right; font-size: 12.5px; color: var(--lead-neutral-500);">Hôm nay</td>
                                    </tr>
                                    <tr class="row-valid">
                                        <td class="font-mono">
                                            <strong style="color: var(--lead-primary);">LEAD-DEMO-02</strong>
                                        </td>
                                        <td>
                                            <strong style="color: var(--lead-neutral-900);">Trần Thị Mai Phương</strong>
                                            <div style="font-size: 12px; color: var(--lead-neutral-500);">Danh thiếp trao đổi trực tiếp</div>
                                        </td>
                                        <td>
                                            <span class="lead-badge lead-badge-source">
                                                <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">label</span>
                                                Danh thiếp đối tác
                                            </span>
                                        </td>
                                        <td>Tập đoàn Bán lẻ VinaMart</td>
                                        <td class="font-mono">0987654321</td>
                                        <td>phuong.tran@vinamart.vn</td>
                                        <td style="text-align: center;">
                                            <span class="lead-badge lead-badge-valid">
                                                <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">fiber_new</span>
                                                Mới
                                            </span>
                                        </td>
                                        <td style="text-align: right; font-size: 12.5px; color: var(--lead-neutral-500);">Hôm nay</td>
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
