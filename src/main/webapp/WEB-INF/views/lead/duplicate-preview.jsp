<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Xem trước và so sánh dữ liệu trước khi gộp 2 bản ghi Lead trùng lặp - CRM Bán Hàng Nhóm 10">
    <title>Xem Trước & Gộp Lead Trùng - CRM Bán Hàng</title>

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
                    <li><a href="${pageContext.request.contextPath}/lead/duplicate?action=check&leadId=${targetLead.id}" class="gl-breadcrumb-link">Cảnh báo trùng</a></li>
                    <span class="gl-breadcrumb-sep" aria-hidden="true">/</span>
                    <li class="gl-breadcrumb-current" aria-current="page">So sánh & Gộp Lead</li>
                </ol>
                <div style="display: flex; gap: 8px;">
                    <a href="${pageContext.request.contextPath}/lead/duplicate?action=check&leadId=${targetLead.id}" class="gl-btn gl-btn-outline gl-btn-sm" id="btnHuyBoQuayLai">
                        <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span>
                        <span>Quay lại</span>
                    </a>
                </div>
            </nav>

            <!-- 2. Thông báo lỗi hoặc cảnh báo nếu có -->
            <c:if test="${not empty errorMessage}">
                <div class="gl-alert gl-alert-danger" role="alert">
                    <span class="material-symbols-outlined" aria-hidden="true">error</span>
                    <span><c:out value="${errorMessage}" /></span>
                    <button type="button" class="gl-alert-close" aria-label="Đóng thông báo">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>
            </c:if>

            <c:if test="${not empty soSanh.thongDiepCanhBao}">
                <div class="gl-alert gl-alert-warning" role="alert">
                    <span class="material-symbols-outlined" aria-hidden="true">warning</span>
                    <span><c:out value="${soSanh.thongDiepCanhBao}" /></span>
                </div>
            </c:if>

            <!-- 3. Header Card -->
            <header class="gl-header-card">
                <div class="gl-header-title">
                    <h1>
                        <span class="material-symbols-outlined" style="color: var(--gl-primary); font-size: 32px;" aria-hidden="true">merge_type</span>
                        <span>So Sánh Chi Tiết & Xác Nhận Gộp Lead</span>
                    </h1>
                    <p>Đối chiếu song song thông tin của Lead nguồn và Lead đích. Toàn bộ lịch sử chăm sóc sẽ được bảo toàn nguyên vẹn sang Lead giữ lại (AC3).</p>
                </div>
                <div class="gl-header-actions">
                    <!-- Nút đổi chiều gộp (Swap Target và Source) -->
                    <a href="${pageContext.request.contextPath}/lead/duplicate?action=preview&sourceLeadId=${targetLead.id}&targetLeadId=${sourceLead.id}" 
                       class="gl-btn gl-btn-outline" 
                       id="btnDoiChieuGop"
                       title="Đảo vai trò: Giữ lại Lead nguồn và gộp Lead đích vào">
                        <span class="material-symbols-outlined" aria-hidden="true">swap_horiz</span>
                        <span>Đổi chiều gộp</span>
                    </a>
                </div>
            </header>

            <!-- 4. Thống kê bảo toàn lịch sử giao dịch & tương tác (AC3) -->
            <section class="gl-history-preserve-card" aria-label="Thống kê bảo toàn lịch sử">
                <h2 class="gl-section-title" style="margin-bottom: 0;">
                    <span style="display: flex; align-items: center; gap: 8px;">
                        <span class="material-symbols-outlined" style="color: var(--gl-success);" aria-hidden="true">verified_user</span>
                        <span>Cam Kết Bảo Toàn Lịch Sử Của Cả Hai Bản Ghi (AC3)</span>
                    </span>
                    <span style="font-size: 0.8125rem; font-weight: normal; color: var(--gl-neutral-500);">
                        Sau khi gộp, Lead nguồn sẽ chuyển trạng thái <code>ĐÃ GỘP</code>
                    </span>
                </h2>

                <div class="gl-history-stats-grid">
                    <div class="gl-history-stat-box">
                        <div class="gl-history-stat-icon call">
                            <span class="material-symbols-outlined" aria-hidden="true">call_log</span>
                        </div>
                        <div class="gl-history-stat-content">
                            <div class="gl-history-stat-num">${soSanh.soHoatDongNguon} + ${soSanh.soHoatDongDich}</div>
                            <div class="gl-history-stat-label">Lịch sử cuộc gọi, họp, email</div>
                        </div>
                    </div>

                    <div class="gl-history-stat-box">
                        <div class="gl-history-stat-icon task">
                            <span class="material-symbols-outlined" aria-hidden="true">task_alt</span>
                        </div>
                        <div class="gl-history-stat-content">
                            <div class="gl-history-stat-num">${soSanh.soCongViecNguon} + ${soSanh.soCongViecDich}</div>
                            <div class="gl-history-stat-label">Công việc & Lịch nhắc hẹn</div>
                        </div>
                    </div>

                    <div class="gl-history-stat-box">
                        <div class="gl-history-stat-icon opportunity">
                            <span class="material-symbols-outlined" aria-hidden="true">trending_up</span>
                        </div>
                        <div class="gl-history-stat-content">
                            <div class="gl-history-stat-num">${soSanh.soCoHoiNguon} + ${soSanh.soCoHoiDich}</div>
                            <div class="gl-history-stat-label">Cơ hội kinh doanh liên quan</div>
                        </div>
                    </div>

                    <div class="gl-history-stat-box" style="background: var(--gl-success-light); border-color: var(--gl-success-border);">
                        <div class="gl-history-stat-icon total">
                            <span class="material-symbols-outlined" aria-hidden="true">all_inclusive</span>
                        </div>
                        <div class="gl-history-stat-content">
                            <div class="gl-history-stat-num" style="color: var(--gl-success-text);">${soSanh.tongLichSuSauGop} bản ghi</div>
                            <div class="gl-history-stat-label" style="color: var(--gl-success-text);">Tổng dữ liệu bảo toàn</div>
                        </div>
                    </div>
                </div>
            </section>

            <!-- 5. Bảng so sánh song song Side-by-Side -->
            <section class="gl-card">
                <h2 class="gl-section-title">
                    <span style="display: flex; align-items: center; gap: 8px;">
                        <span class="material-symbols-outlined" style="color: var(--gl-primary);" aria-hidden="true">compare</span>
                        <span>So Sánh Chi Tiết Từng Thuộc Tính</span>
                    </span>
                </h2>

                <div class="gl-compare-table-wrapper">
                    <table class="gl-compare-table">
                        <thead>
                            <tr>
                                <th class="col-field" scope="col">Thuộc tính hồ sơ</th>
                                <th class="col-source" scope="col">
                                    <div style="display: flex; align-items: center; gap: 6px;">
                                        <span class="material-symbols-outlined" style="font-size: 18px;" aria-hidden="true">archive</span>
                                        <span>LEAD NGUỒN (SẼ BỊ GỘP)</span>
                                    </div>
                                    <div style="font-size: 0.75rem; font-weight: normal; margin-top: 4px;">
                                        ID: #${sourceLead.id} - <c:out value="${sourceLead.hoTen}" />
                                    </div>
                                </th>
                                <th class="col-target" scope="col">
                                    <div style="display: flex; align-items: center; gap: 6px;">
                                        <span class="material-symbols-outlined" style="font-size: 18px;" aria-hidden="true">check_circle</span>
                                        <span>LEAD ĐÍCH (ĐƯỢC GIỮ LẠI)</span>
                                    </div>
                                    <div style="font-size: 0.75rem; font-weight: normal; margin-top: 4px;">
                                        ID: #${targetLead.id} - <c:out value="${targetLead.hoTen}" />
                                    </div>
                                </th>
                            </tr>
                        </thead>
                        <tbody>
                            <!-- Họ và tên -->
                            <tr>
                                <td class="field-name">Họ và tên</td>
                                <td class="source-val"><c:out value="${sourceLead.hoTen}" /></td>
                                <td class="target-val"><c:out value="${targetLead.hoTen}" /></td>
                            </tr>

                            <!-- Email -->
                            <tr>
                                <td class="field-name">Email liên hệ</td>
                                <td class="source-val"><c:out value="${sourceLead.email != null ? sourceLead.email : '—'}" /></td>
                                <td class="target-val"><c:out value="${targetLead.email != null ? targetLead.email : '—'}" /></td>
                            </tr>

                            <!-- Số điện thoại -->
                            <tr>
                                <td class="field-name">Số điện thoại</td>
                                <td class="source-val"><c:out value="${sourceLead.soDienThoai != null ? sourceLead.soDienThoai : '—'}" /></td>
                                <td class="target-val"><c:out value="${targetLead.soDienThoai != null ? targetLead.soDienThoai : '—'}" /></td>
                            </tr>

                            <!-- Tên công ty -->
                            <tr>
                                <td class="field-name">Tên công ty</td>
                                <td class="source-val"><c:out value="${sourceLead.tenCongTy != null ? sourceLead.tenCongTy : '—'}" /></td>
                                <td class="target-val"><c:out value="${targetLead.tenCongTy != null ? targetLead.tenCongTy : '—'}" /></td>
                            </tr>

                            <!-- Nguồn Lead -->
                            <tr>
                                <td class="field-name">Nguồn thu thập</td>
                                <td class="source-val"><c:out value="${sourceLead.tenNguonLead != null ? sourceLead.tenNguonLead : 'Tự nhiên / Trực tiếp'}" /></td>
                                <td class="target-val"><c:out value="${targetLead.tenNguonLead != null ? targetLead.tenNguonLead : 'Tự nhiên / Trực tiếp'}" /></td>
                            </tr>

                            <!-- Chiến dịch marketing -->
                            <tr>
                                <td class="field-name">Chiến dịch marketing</td>
                                <td class="source-val"><c:out value="${sourceLead.tenChienDich != null ? sourceLead.tenChienDich : 'Không gắn chiến dịch'}" /></td>
                                <td class="target-val"><c:out value="${targetLead.tenChienDich != null ? targetLead.tenChienDich : 'Không gắn chiến dịch'}" /></td>
                            </tr>

                            <!-- Người phụ trách -->
                            <tr>
                                <td class="field-name">Người phụ trách</td>
                                <td class="source-val"><c:out value="${sourceLead.tenNguoiPhuTrach != null ? sourceLead.tenNguoiPhuTrach : 'Chưa phân bổ'}" /></td>
                                <td class="target-val"><c:out value="${targetLead.tenNguoiPhuTrach != null ? targetLead.tenNguoiPhuTrach : 'Chưa phân bổ'}" /></td>
                            </tr>

                            <!-- Trạng thái -->
                            <tr>
                                <td class="field-name">Trạng thái hồ sơ</td>
                                <td class="source-val">
                                    <span class="gl-badge-reason" style="background: var(--gl-neutral-100); color: var(--gl-neutral-700);">
                                        <c:out value="${sourceLead.trangThai != null ? sourceLead.trangThai.tenHienThi : 'Mới'}" />
                                    </span>
                                </td>
                                <td class="target-val">
                                    <span class="gl-badge-reason" style="background: var(--gl-success-light); color: var(--gl-success-text); border: 1px solid var(--gl-success-border);">
                                        <c:out value="${targetLead.trangThai != null ? targetLead.trangThai.tenHienThi : 'Mới'}" />
                                    </span>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <!-- 6. Form thực thi Gộp (POST action=merge) -->
                <form action="${pageContext.request.contextPath}/lead/duplicate?action=merge" method="POST" id="formGopLead">
                    <input type="hidden" name="sourceLeadId" value="${sourceLead.id}">
                    <input type="hidden" name="targetLeadId" value="${targetLead.id}">

                    <div style="margin-bottom: 20px;">
                        <label for="lyDoGop" style="display: block; font-size: 0.875rem; font-weight: 600; color: var(--gl-neutral-800); margin-bottom: 6px;">
                            Lý do thực hiện gộp Lead <span style="color: var(--gl-danger);">*</span>
                        </label>
                        <input type="text" 
                               id="lyDoGop" 
                               name="lyDo" 
                               value="Gộp lead trùng thông tin liên hệ (Email/SĐT/Công ty)" 
                               required
                               placeholder="Ghi rõ lý do gộp phục vụ lưu vết nhật ký hệ thống..."
                               style="width: 100%; padding: 10px 14px; font-size: 0.875rem; border: 1px solid var(--gl-neutral-300); border-radius: var(--gl-radius-md); box-sizing: border-box;">
                    </div>

                    <!-- Checkbox cam kết xác nhận -->
                    <div style="margin-bottom: 24px; background: var(--gl-neutral-50); padding: 14px 18px; border-radius: var(--gl-radius-md); border: 1px solid var(--gl-neutral-200);">
                        <label style="display: flex; align-items: flex-start; gap: 10px; cursor: pointer; font-size: 0.875rem; color: var(--gl-neutral-800); user-select: none;">
                            <input type="checkbox" id="chkDongYGop" style="margin-top: 3px; cursor: pointer; width: 16px; height: 16px;">
                            <span>
                                <strong>Tôi đã kiểm tra kỹ lưỡng và xác nhận gộp hai bản ghi:</strong> Toàn bộ lịch sử chăm sóc từ Lead nguồn (#${sourceLead.id}) sẽ được bảo toàn và chuyển sang Lead giữ lại (#${targetLead.id}). Thao tác này không thể hoàn tác trực tiếp.
                            </span>
                        </label>
                    </div>

                    <div style="display: flex; align-items: center; gap: 14px; flex-wrap: wrap;">
                        <button type="submit" class="gl-btn gl-btn-primary" id="btnSubmitGopLead" disabled>
                            <span class="material-symbols-outlined" aria-hidden="true">merge_type</span>
                            <span>Xác nhận thực hiện gộp Lead</span>
                        </button>
                        <a href="${pageContext.request.contextPath}/lead/duplicate?action=check&leadId=${targetLead.id}" class="gl-btn gl-btn-outline" id="btnHuyBoGop">
                            <span class="material-symbols-outlined" aria-hidden="true">cancel</span>
                            <span>Hủy bỏ</span>
                        </a>
                    </div>
                </form>
            </section>

        </div>
    </main>

    <!-- JavaScript hỗ trợ -->
    <script src="${pageContext.request.contextPath}/assets/js/lead/gop-lead.js"></script>
</body>
</html>
