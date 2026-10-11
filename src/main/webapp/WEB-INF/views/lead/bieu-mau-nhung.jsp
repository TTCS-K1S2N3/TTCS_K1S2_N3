<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Biểu mẫu đăng ký tư vấn và thu thập thông tin khách hàng tiềm năng">
    <title><c:out value="${bieuMau.tenBieuMau != null ? bieuMau.tenBieuMau : 'Đăng Ký Tư Vấn Giải Pháp'}" /></title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --crm-primary: #1e40af;
            --crm-primary-hover: #1d4ed8;
            --crm-primary-light: #eff6ff;
            --crm-success: #16a34a;
            --crm-success-light: #f0fdf4;
            --crm-danger: #dc2626;
            --crm-danger-light: #fef2f2;
            --crm-gray-50: #f9fafb;
            --crm-gray-100: #f3f4f6;
            --crm-gray-200: #e5e7eb;
            --crm-gray-300: #d1d5db;
            --crm-gray-500: #6b7280;
            --crm-gray-700: #374151;
            --crm-gray-900: #111827;
            --crm-radius: 8px;
            --crm-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -2px rgba(0, 0, 0, 0.05);
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: 'Inter', system-ui, -apple-system, sans-serif;
            background-color: transparent;
            color: var(--crm-gray-900);
            line-height: 1.5;
            padding: 12px;
        }

        .lead-embed-card {
            background: #ffffff;
            border: 1px solid var(--crm-gray-200);
            border-radius: var(--crm-radius);
            box-shadow: var(--crm-shadow);
            max-width: 560px;
            margin: 0 auto;
            padding: 24px;
        }

        .lead-embed-header {
            margin-bottom: 20px;
            text-align: center;
        }

        .lead-embed-header h2 {
            font-size: 1.25rem;
            font-weight: 700;
            color: var(--crm-gray-900);
            margin-bottom: 6px;
        }

        .lead-embed-header p {
            font-size: 0.875rem;
            color: var(--crm-gray-500);
        }

        .lead-alert {
            padding: 12px 16px;
            border-radius: 6px;
            font-size: 0.875rem;
            margin-bottom: 16px;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .lead-alert-danger {
            background-color: var(--crm-danger-light);
            color: var(--crm-danger);
            border: 1px solid #fecaca;
        }

        .lead-alert-success {
            background-color: var(--crm-success-light);
            color: var(--crm-success);
            border: 1px solid #bbf7d0;
        }

        .lead-form-group {
            margin-bottom: 16px;
        }

        .lead-form-label {
            display: block;
            font-size: 0.875rem;
            font-weight: 600;
            color: var(--crm-gray-700);
            margin-bottom: 6px;
        }

        .lead-form-label .required {
            color: var(--crm-danger);
        }

        .lead-form-control {
            width: 100%;
            padding: 10px 14px;
            font-size: 0.875rem;
            color: var(--crm-gray-900);
            background-color: #ffffff;
            border: 1px solid var(--crm-gray-300);
            border-radius: 6px;
            transition: border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
            font-family: inherit;
        }

        .lead-form-control:focus {
            outline: none;
            border-color: var(--crm-primary);
            box-shadow: 0 0 0 3px rgba(30, 64, 175, 0.15);
        }

        textarea.lead-form-control {
            resize: vertical;
            min-height: 80px;
        }

        .lead-row {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 12px;
        }

        @media (max-width: 480px) {
            .lead-row {
                grid-template-columns: 1fr;
            }
        }

        /* Honeypot field - ẩn hoàn toàn khỏi người dùng */
        .lead-hp-trap {
            position: absolute !important;
            left: -9999px !important;
            top: -9999px !important;
            width: 1px !important;
            height: 1px !important;
            opacity: 0 !important;
            pointer-events: none !important;
        }

        .lead-btn-submit {
            display: block;
            width: 100%;
            padding: 12px 20px;
            font-size: 0.9375rem;
            font-weight: 600;
            color: #ffffff;
            background-color: var(--crm-primary);
            border: none;
            border-radius: 6px;
            cursor: pointer;
            transition: background-color 0.2s;
            text-align: center;
        }

        .lead-btn-submit:hover:not(:disabled) {
            background-color: var(--crm-primary-hover);
        }

        .lead-btn-submit:disabled {
            opacity: 0.65;
            cursor: not-allowed;
        }

        .lead-footer-note {
            margin-top: 12px;
            font-size: 0.75rem;
            color: var(--crm-gray-500);
            text-align: center;
        }
    </style>
</head>
<body>
    <div class="lead-embed-card">
        <header class="lead-embed-header">
            <h2><c:out value="${bieuMau != null ? bieuMau.tenBieuMau : 'Liên Hệ Tư Vấn'}" /></h2>
            <p>Vui lòng điền thông tin bên dưới, chuyên viên chúng tôi sẽ phản hồi trong thời gian sớm nhất.</p>
        </header>

        <!-- Thông báo lỗi hoặc thành công -->
        <div id="alertSuccess" class="lead-alert lead-alert-success" style="display: none;"></div>
        <div id="alertError" class="lead-alert lead-alert-danger" <c:if test="${empty thongBaoLoi}">style="display: none;"</c:if>>
            <c:out value="${thongBaoLoi}" />
        </div>

        <c:choose>
            <c:when test="${not empty bieuMau and not bieuMau.hoatDong}">
                <div class="lead-alert lead-alert-danger" style="margin-top: 10px;">
                    Biểu mẫu này hiện đang tạm dừng tiếp nhận thông tin. Quý khách vui lòng liên hệ lại sau.
                </div>
            </c:when>
            <c:otherwise>
                <form id="leadForm" action="${pageContext.request.contextPath}/bieu-mau/gui" method="POST">
                    <input type="hidden" name="token" value="<c:out value="${bieuMau != null ? bieuMau.tokenCongKhai : param.token}" />" />

                    <!-- AC3: Chống spam Honeypot ẩn với người dùng -->
                    <div class="lead-hp-trap" aria-hidden="true">
                        <label for="website_url">Website URL (Không điền vào trường này)</label>
                        <input type="text" id="website_url" name="website_url" tabindex="-1" autocomplete="off" />
                    </div>

                    <!-- AC2: Họ và tên -->
                    <div class="lead-form-group">
                        <label class="lead-form-label" for="ho_ten">Họ và tên <span class="required">*</span></label>
                        <input type="text" id="ho_ten" name="ho_ten" class="lead-form-control"
                               placeholder="Nguyễn Văn A" required maxlength="150"
                               value="<c:out value="${dto != null ? dto.hoTen : ''}" />" />
                    </div>

                    <!-- AC2: Email & Số điện thoại -->
                    <div class="lead-row">
                        <div class="lead-form-group">
                            <label class="lead-form-label" for="email">Địa chỉ Email</label>
                            <input type="email" id="email" name="email" class="lead-form-control"
                                   placeholder="email@company.com" maxlength="255"
                                   value="<c:out value="${dto != null ? dto.email : ''}" />" />
                        </div>
                        <div class="lead-form-group">
                            <label class="lead-form-label" for="so_dien_thoai">Số điện thoại</label>
                            <input type="tel" id="so_dien_thoai" name="so_dien_thoai" class="lead-form-control"
                                   placeholder="0912 345 678" maxlength="20"
                                   value="<c:out value="${dto != null ? dto.soDienThoai : ''}" />" />
                        </div>
                    </div>

                    <!-- AC2: Tên công ty -->
                    <div class="lead-form-group">
                        <label class="lead-form-label" for="ten_cong_ty">Công ty / Doanh nghiệp</label>
                        <input type="text" id="ten_cong_ty" name="ten_cong_ty" class="lead-form-control"
                               placeholder="Công ty TNHH Giải Pháp Doanh Nghiệp" maxlength="255"
                               value="<c:out value="${dto != null ? dto.tenCongTy : ''}" />" />
                    </div>

                    <!-- AC2: Nhu cầu quan tâm -->
                    <div class="lead-form-group">
                        <label class="lead-form-label" for="nhu_cau_quan_tam">Nhu cầu quan tâm</label>
                        <textarea id="nhu_cau_quan_tam" name="nhu_cau_quan_tam" class="lead-form-control"
                                  placeholder="Mô tả nhu cầu, tính năng mong muốn hoặc số lượng người sử dụng..."><c:out value="${dto != null ? dto.nhuCauQuanTam : ''}" /></textarea>
                    </div>

                    <button type="submit" class="lead-btn-submit" id="btnSubmitLead">Gửi Thông Tin Yêu Cầu</button>

                    <p class="lead-footer-note">Thông tin của bạn được cam kết bảo mật theo quy định quyền riêng tư.</p>
                </form>
            </c:otherwise>
        </c:choose>
    </div>

    <script>
        (function() {
            var form = document.getElementById('leadForm');
            if (!form) return;

            var alertSuccess = document.getElementById('alertSuccess');
            var alertError = document.getElementById('alertError');
            var btnSubmit = document.getElementById('btnSubmitLead');

            form.addEventListener('submit', function(e) {
                // Bắt buộc có email hoặc SĐT
                var email = document.getElementById('email').value.trim();
                var sdt = document.getElementById('so_dien_thoai').value.trim();

                if (!email && !sdt) {
                    e.preventDefault();
                    alertError.style.display = 'block';
                    alertError.textContent = 'Vui lòng cung cấp ít nhất một phương thức liên lạc: Email hoặc Số điện thoại.';
                    return;
                }

                // Hỗ trợ AJAX submit mượt mà
                if (window.fetch) {
                    e.preventDefault();
                    alertError.style.display = 'none';
                    alertSuccess.style.display = 'none';
                    btnSubmit.disabled = true;
                    btnSubmit.textContent = 'Đang gửi...';

                    var formData = new FormData(form);
                    var urlParams = new URLSearchParams(formData);

                    fetch(form.action, {
                        method: 'POST',
                        body: urlParams,
                        headers: {
                            'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
                            'X-Requested-With': 'XMLHttpRequest',
                            'Accept': 'application/json'
                        }
                    })
                    .then(function(res) {
                        return res.json().then(function(data) {
                            return { status: res.status, ok: res.ok, data: data };
                        });
                    })
                    .then(function(resObj) {
                        btnSubmit.disabled = false;
                        btnSubmit.textContent = 'Gửi Thông Tin Yêu Cầu';

                        if (resObj.ok && resObj.data.thanhCong) {
                            alertSuccess.style.display = 'block';
                            alertSuccess.textContent = resObj.data.thongBao || 'Gửi thông tin thành công!';
                            form.reset();
                        } else {
                            alertError.style.display = 'block';
                            alertError.textContent = (resObj.data && resObj.data.thongBao) ? resObj.data.thongBao : 'Đã có lỗi xảy ra. Vui lòng thử lại sau.';
                        }
                    })
                    .catch(function(err) {
                        btnSubmit.disabled = false;
                        btnSubmit.textContent = 'Gửi Thông Tin Yêu Cầu';
                        alertError.style.display = 'block';
                        alertError.textContent = 'Không thể kết nối đến máy chủ. Vui lòng kiểm tra lại đường truyền mạng.';
                    });
                }
            });
        })();
    </script>
</body>
</html>
