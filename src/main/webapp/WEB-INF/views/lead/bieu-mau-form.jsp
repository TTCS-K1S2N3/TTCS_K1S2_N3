<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Tạo biểu mẫu nhúng thu thập lead mới - CRM Bán Hàng">
    <title>Tạo Biểu Mẫu Lead Mới - CRM Bán Hàng</title>
    <!-- CSS dùng chung toàn hệ thống -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" />
    <style>
        .bmf-container {
            padding: 24px 32px;
            max-width: 900px;
            margin: 0 auto;
        }
        .bmf-breadcrumb {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 20px;
        }
        .bmf-breadcrumb-nav {
            display: flex;
            align-items: center;
            gap: 8px;
            list-style: none;
            padding: 0;
            margin: 0;
            font-size: 0.875rem;
        }
        .bmf-breadcrumb-nav a {
            color: var(--color-primary, #1e40af);
            text-decoration: none;
        }
        .bmf-card {
            background: #ffffff;
            border: 1px solid #e5e7eb;
            border-radius: 8px;
            padding: 28px;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
        }
        .bmf-card h1 {
            font-size: 1.25rem;
            font-weight: 700;
            color: #111827;
            margin-bottom: 6px;
        }
        .bmf-card p {
            font-size: 0.875rem;
            color: #6b7280;
            margin-bottom: 24px;
        }
        .bmf-form-group {
            margin-bottom: 20px;
        }
        .bmf-label {
            display: block;
            font-size: 0.875rem;
            font-weight: 600;
            color: #374151;
            margin-bottom: 6px;
        }
        .bmf-label .required {
            color: #dc2626;
        }
        .bmf-input, .bmf-select {
            width: 100%;
            padding: 10px 14px;
            font-size: 0.875rem;
            border: 1px solid #d1d5db;
            border-radius: 6px;
            background-color: #ffffff;
            box-sizing: border-box;
        }
        .bmf-input:focus, .bmf-select:focus {
            outline: none;
            border-color: #1e40af;
            box-shadow: 0 0 0 3px rgba(30, 64, 175, 0.15);
        }
        .bmf-row {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 16px;
        }
        .bmf-checkbox-label {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 0.875rem;
            color: #374151;
            cursor: pointer;
        }
        .bmf-actions {
            display: flex;
            align-items: center;
            justify-content: flex-end;
            gap: 12px;
            margin-top: 30px;
            padding-top: 20px;
            border-top: 1px solid #e5e7eb;
        }
        .bmf-btn {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 10px 20px;
            font-size: 0.875rem;
            font-weight: 600;
            border-radius: 6px;
            cursor: pointer;
            text-decoration: none;
            border: 1px solid transparent;
        }
        .bmf-btn-primary {
            background-color: #1e40af;
            color: #ffffff;
        }
        .bmf-btn-primary:hover {
            background-color: #1d4ed8;
        }
        .bmf-btn-outline {
            background-color: #ffffff;
            border-color: #d1d5db;
            color: #374151;
        }
        .bmf-alert-danger {
            background-color: #fef2f2;
            color: #dc2626;
            border: 1px solid #fecaca;
            padding: 12px 16px;
            border-radius: 6px;
            font-size: 0.875rem;
            margin-bottom: 20px;
        }
    </style>
</head>
<body class="crm-body">
    <!-- Navigation layout dùng chung -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <div class="bmf-container">
            <!-- Breadcrumb -->
            <nav class="bmf-breadcrumb" aria-label="Breadcrumb">
                <ol class="bmf-breadcrumb-nav">
                    <li><a href="${pageContext.request.contextPath}/dieu-huong">Trang chủ</a></li>
                    <span style="color: #9ca3af;">/</span>
                    <li><a href="${pageContext.request.contextPath}/lead">Lead & Phân bổ</a></li>
                    <span style="color: #9ca3af;">/</span>
                    <li><a href="${pageContext.request.contextPath}/bieu-mau">Biểu mẫu</a></li>
                    <span style="color: #9ca3af;">/</span>
                    <li aria-current="page" style="color: #374151; font-weight: 500;">Tạo mới</li>
                </ol>
            </nav>

            <div class="bmf-card">
                <h1>Tạo Biểu Mẫu Nhúng Mới (Story S4-01)</h1>
                <p>Khai báo thông tin biểu mẫu để sinh mã nhúng tự động dán vào website bất kỳ.</p>

                <c:if test="${not empty thongBaoLoi}">
                    <div class="bmf-alert-danger">
                        <c:out value="${thongBaoLoi}" />
                    </div>
                </c:if>

                <form action="${pageContext.request.contextPath}/bieu-mau/tao" method="POST">
                    <div class="bmf-row">
                        <div class="bmf-form-group">
                            <label class="bmf-label" for="ma_bieu_mau">Mã biểu mẫu <span class="required">*</span></label>
                            <input type="text" id="ma_bieu_mau" name="ma_bieu_mau" class="bmf-input"
                                   placeholder="VD: BM_WEB_TU_VAN" required maxlength="60"
                                   value="<c:out value="${ma_bieu_mau != null ? ma_bieu_mau : ''}" />" />
                        </div>
                        <div class="bmf-form-group">
                            <label class="bmf-label" for="ten_bieu_mau">Tên biểu mẫu <span class="required">*</span></label>
                            <input type="text" id="ten_bieu_mau" name="ten_bieu_mau" class="bmf-input"
                                   placeholder="VD: Form đăng ký tư vấn giải pháp CRM" required maxlength="200"
                                   value="<c:out value="${ten_bieu_mau != null ? ten_bieu_mau : ''}" />" />
                        </div>
                    </div>

                    <div class="bmf-row">
                        <div class="bmf-form-group">
                            <label class="bmf-label" for="nguon_lead_id">Nguồn Lead gắn cho biểu mẫu <span class="required">*</span></label>
                            <select id="nguon_lead_id" name="nguon_lead_id" class="bmf-select" required>
                                <option value="">-- Chọn nguồn lead --</option>
                                <c:forEach items="${dsNguonLead}" var="nguon">
                                    <option value="${nguon.id}" <c:if test="${nguon_lead_id == nguon.id}">selected</c:if>>
                                        <c:out value="${nguon.tenMuc}" /> (<c:out value="${nguon.maMuc}" />)
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="bmf-form-group">
                            <label class="bmf-label" for="gioi_han_gui_moi_gio">Giới hạn tần suất gửi (lượt / IP / giờ) <span class="required">*</span></label>
                            <input type="number" id="gioi_han_gui_moi_gio" name="gioi_han_gui_moi_gio" class="bmf-input"
                                   min="1" max="500" value="<c:out value="${gioi_han_gui_moi_gio != null ? gioi_han_gui_moi_gio : 10}" />" required />
                        </div>
                    </div>

                    <div class="bmf-form-group">
                        <label class="bmf-checkbox-label">
                            <input type="checkbox" name="bat_honeypot" value="1" checked />
                            <span><strong>Bật cơ chế chống spam thông minh (Honeypot)</strong>: Tự động bẫy và chặn bot điền trường ẩn (AC3)</span>
                        </label>
                    </div>

                    <div class="bmf-actions">
                        <a href="${pageContext.request.contextPath}/bieu-mau" class="bmf-btn bmf-btn-outline">Hủy bỏ</a>
                        <button type="submit" class="bmf-btn bmf-btn-primary" id="btnLuuBieuMau">
                            <span class="material-symbols-outlined" style="font-size: 18px;">save</span>
                            <span>Lưu và Sinh Mã Nhúng</span>
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </main>
</body>
</html>
