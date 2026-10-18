<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Tạo biểu mẫu nhúng thu thập lead mới từ website - CRM Bán Hàng Nhóm 10">
    <title>Tạo Biểu Mẫu Lead Mới - CRM Bán Hàng</title>
    <!-- CSS dùng chung toàn hệ thống -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <!-- CSS chuyên biệt module Biểu mẫu Lead (Story S4-01) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lead/bieu-mau.css">
</head>
<body class="crm-body">
    <!-- Navigation layout dùng chung S1-06 (đã bao gồm sidebar, thông tin user và Material Symbols) -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <div class="bml-container" style="max-width: 960px;">

            <!-- 1. Breadcrumb điều hướng chuẩn -->
            <nav class="bml-breadcrumb" aria-label="Breadcrumb">
                <ol class="bml-breadcrumb-nav">
                    <li><a href="${pageContext.request.contextPath}/dieu-huong" class="bml-breadcrumb-link">Trang chủ</a></li>
                    <span class="bml-breadcrumb-sep" aria-hidden="true">/</span>
                    <li><a href="${pageContext.request.contextPath}/lead" class="bml-breadcrumb-link">Lead & Phân bổ</a></li>
                    <span class="bml-breadcrumb-sep" aria-hidden="true">/</span>
                    <li class="bml-breadcrumb-current" aria-current="page">Tạo biểu mẫu mới</li>
                </ol>
                <div>
                    <a href="${pageContext.request.contextPath}/bieu-mau" class="bml-btn bml-btn-outline bml-btn-sm" id="btnQuayLaiDanhSach">
                        <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span>
                        <span>Quay lại danh sách</span>
                    </a>
                </div>
            </nav>

            <!-- 2. Form Card tạo biểu mẫu -->
            <div class="bmf-card">
                <header class="bmf-card-header">
                    <h1>
                        <span class="material-symbols-outlined" style="color: var(--bml-primary);" aria-hidden="true">add_circle</span>
                        <span>Thiết Lập Biểu Mẫu Thu Thập Lead Website</span>
                    </h1>
                    <p>
                        Khai báo thông tin cấu hình biểu mẫu, nguồn tiếp nhận và chính sách phòng chống spam (Story S4-01). Sau khi tạo thành công, hệ thống sẽ cấp mã nhúng Iframe và Script để gắn vào website bất kỳ.
                    </p>
                </header>

                <!-- Thông báo lỗi nếu có -->
                <c:if test="${not empty thongBaoLoi}">
                    <div class="bml-alert bml-alert-danger" role="alert">
                        <div class="bml-alert-content">
                            <span class="material-symbols-outlined" aria-hidden="true">error</span>
                            <span><c:out value="${thongBaoLoi}" /></span>
                        </div>
                    </div>
                </c:if>

                <form action="${pageContext.request.contextPath}/bieu-mau/tao" method="POST" id="formTaoBieuMau">
                    <!-- Thông tin cơ bản -->
                    <div class="bmf-grid-2">
                        <div class="bmf-form-group">
                            <label class="bmf-label" for="ma_bieu_mau">
                                Mã biểu mẫu <span class="required">*</span>
                            </label>
                            <input type="text" id="ma_bieu_mau" name="ma_bieu_mau" class="bmf-input"
                                   placeholder="Ví dụ: BM-LANDING-01" required maxlength="50"
                                   value="<c:out value="${ma_bieu_mau}" />" autocomplete="off" />
                            <div class="bmf-hint">Mã định danh duy nhất (chữ cái, số, gạch nối).</div>
                        </div>

                        <div class="bmf-form-group">
                            <label class="bmf-label" for="ten_bieu_mau">
                                Tên biểu mẫu <span class="required">*</span>
                            </label>
                            <input type="text" id="ten_bieu_mau" name="ten_bieu_mau" class="bmf-input"
                                   placeholder="Ví dụ: Form Tư Vấn Website Chính Thức" required maxlength="255"
                                   value="<c:out value="${ten_bieu_mau}" />" />
                            <div class="bmf-hint">Tên hiển thị tiêu đề trên đầu biểu mẫu nhúng.</div>
                        </div>
                    </div>

                    <!-- Gắn nguồn và chiến dịch (AC4, S4-03 AC2) -->
                    <div class="bmf-grid-2">
                        <div class="bmf-form-group">
                            <label class="bmf-label" for="nguon_lead_id">
                                Nguồn Lead tiếp nhận (AC4) <span class="required">*</span>
                            </label>
                            <select id="nguon_lead_id" name="nguon_lead_id" class="bmf-select" required>
                                <option value="">-- Chọn nguồn lead cho biểu mẫu --</option>
                                <c:forEach items="${dsNguonLead}" var="nguon">
                                    <option value="${nguon.id}" ${nguon_lead_id == nguon.id ? 'selected' : ''}>
                                        <c:out value="${nguon.ten}" />
                                    </option>
                                </c:forEach>
                            </select>
                            <div class="bmf-hint">Mọi lead gửi từ biểu mẫu này sẽ tự động gắn đúng nguồn này vào hồ sơ.</div>
                        </div>

                        <div class="bmf-form-group">
                            <label class="bmf-label" for="chien_dich_id">
                                Chiến dịch Marketing (S4-03 AC2)
                            </label>
                            <select id="chien_dich_id" name="chien_dich_id" class="bmf-select">
                                <option value="">-- Không gắn chiến dịch --</option>
                                <c:forEach items="${dsChienDich}" var="cd">
                                    <option value="${cd.campaignId}" ${chien_dich_id == cd.campaignId ? 'selected' : ''}>
                                        <c:out value="${cd.name}" /> (<c:out value="${cd.maChienDich}" />)
                                    </option>
                                </c:forEach>
                            </select>
                            <div class="bmf-hint">Tùy chọn: Gắn biểu mẫu với chiến dịch marketing đang chạy để đo lường ROI.</div>
                        </div>
                    </div>

                    <div class="bmf-grid-2">
                        <div class="bmf-form-group">
                            <label class="bmf-label" for="gioi_han_gui_moi_gio">
                                Giới hạn tần suất gửi / IP (AC3)
                            </label>
                            <input type="number" id="gioi_han_gui_moi_gio" name="gioi_han_gui_moi_gio" class="bmf-input"
                                   min="1" max="500" value="<c:out value="${gioi_han_gui_moi_gio != null ? gioi_han_gui_moi_gio : '10'}" />" />
                            <div class="bmf-hint">Số lượt gửi tối đa cho phép từ cùng 1 địa chỉ IP trong 1 giờ (Mặc định: 10).</div>
                        </div>
                    </div>

                    <!-- Cấu hình chống spam Honeypot (AC3) -->
                    <div class="bmf-form-group" style="background-color: var(--bml-neutral-50); border: 1px solid var(--bml-neutral-200); border-radius: var(--bml-radius-md); padding: 16px 20px;">
                        <label class="bmf-switch-label" for="bat_honeypot">
                            <input type="checkbox" id="bat_honeypot" name="bat_honeypot" class="bmf-switch-input" value="1" checked />
                            <span>Bật cơ chế chống Spam Honeypot (Khuyến nghị - AC3)</span>
                        </label>
                        <p style="font-size: 0.8125rem; color: var(--bml-neutral-500); margin: 6px 0 0 28px; line-height: 1.5;">
                            Hệ thống sẽ tự động chèn trường bẫy ẩn trong biểu mẫu. Bot spam tự động điền trường này sẽ bị phát hiện và ngăn chặn tạo lead rác vào cơ sở dữ liệu.
                        </p>
                    </div>

                    <!-- Nút thao tác -->
                    <div class="bmf-actions">
                        <a href="${pageContext.request.contextPath}/bieu-mau" class="bml-btn bml-btn-outline">
                            <span>Hủy bỏ</span>
                        </a>
                        <button type="submit" class="bml-btn bml-btn-primary" id="btnLuuBieuMau">
                            <span class="material-symbols-outlined" aria-hidden="true">check</span>
                            <span>Lưu & Sinh Mã Nhúng</span>
                        </button>
                    </div>
                </form>
            </div>

        </div>
    </main>

    <!-- Scripts điều hướng hệ thống -->
    <script src="${pageContext.request.contextPath}/assets/js/navigation.js"></script>
</body>
</html>
