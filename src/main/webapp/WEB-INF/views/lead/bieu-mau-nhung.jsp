<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Biểu mẫu đăng ký tư vấn và thu thập thông tin khách hàng tiềm năng">
    <title><c:out value="${bieuMau != null ? bieuMau.tenBieuMau : 'Đăng Ký Tư Vấn Giải Pháp'}" /></title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200">
    <style>
        :root {
            --bme-primary: #2563eb;
            --bme-primary-hover: #1d4ed8;
            --bme-primary-light: #eff6ff;
            --bme-primary-border: #bfdbfe;

            --bme-neutral-50: #f8fafc;
            --bme-neutral-100: #f1f5f9;
            --bme-neutral-200: #e2e8f0;
            --bme-neutral-300: #cbd5e1;
            --bme-neutral-500: #64748b;
            --bme-neutral-700: #334155;
            --bme-neutral-800: #1e293b;
            --bme-neutral-900: #0f172a;

            --bme-success: #10b981;
            --bme-success-light: #ecfdf5;
            --bme-success-border: #a7f3d0;
            --bme-success-text: #065f46;

            --bme-danger: #ef4444;
            --bme-danger-light: #fef2f2;
            --bme-danger-border: #fecaca;
            --bme-danger-text: #991b1b;

            --bme-radius: 8px;
            --bme-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.08), 0 2px 4px -2px rgba(0, 0, 0, 0.05);
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: 'Plus Jakarta Sans', system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            background-color: transparent;
            color: var(--bme-neutral-900);
            line-height: 1.5;
            padding: 12px;
            -webkit-font-smoothing: antialiased;
        }

        .lead-embed-card {
            background: #ffffff;
            border: 1px solid var(--bme-neutral-200);
            border-radius: var(--bme-radius);
            box-shadow: var(--bme-shadow);
            max-width: 580px;
            margin: 0 auto;
            padding: 28px 24px;
        }

        .lead-embed-header {
            margin-bottom: 22px;
            text-align: center;
        }

        .lead-embed-header h2 {
            font-size: 1.3125rem;
            font-weight: 700;
            color: var(--bme-neutral-900);
            margin-bottom: 6px;
            letter-spacing: -0.01em;
        }

        .lead-embed-header p {
            font-size: 0.875rem;
            color: var(--bme-neutral-500);
            line-height: 1.5;
        }

        .lead-alert {
            padding: 12px 16px;
            border-radius: 6px;
            font-size: 0.875rem;
            margin-bottom: 16px;
            display: flex;
            align-items: center;
            gap: 8px;
            line-height: 1.4;
        }

        .lead-alert-danger {
            background-color: var(--bme-danger-light);
            color: var(--bme-danger-text);
            border: 1px solid var(--bme-danger-border);
        }

        .lead-alert-success {
            background-color: var(--bme-success-light);
            color: var(--bme-success-text);
            border: 1px solid var(--bme-success-border);
        }

        .lead-form-group {
            margin-bottom: 16px;
        }

        .lead-form-label {
            display: block;
            font-size: 0.875rem;
            font-weight: 600;
            color: var(--bme-neutral-700);
            margin-bottom: 6px;
        }

        .lead-form-label .required {
            color: var(--bme-danger);
        }

        .lead-form-control {
            width: 100%;
            padding: 10px 14px;
            font-size: 0.875rem;
            color: var(--bme-neutral-900);
            background-color: #ffffff;
            border: 1px solid var(--bme-neutral-300);
            border-radius: 6px;
            transition: border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
            font-family: inherit;
        }

        .lead-form-control:focus {
            outline: none;
            border-color: var(--bme-primary);
            box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
        }

        .lead-form-control.is-invalid {
            border-color: var(--bme-danger);
            background-color: var(--bme-danger-light);
        }

        .lead-field-error {
            font-size: 0.75rem;
            color: var(--bme-danger);
            margin-top: 4px;
            display: none;
        }

        textarea.lead-form-control {
            resize: vertical;
            min-height: 84px;
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
            .lead-embed-card {
                padding: 20px 16px;
            }
        }

        /* AC3: Honeypot field - Ẩn hoàn toàn khỏi người dùng */
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
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            width: 100%;
            padding: 12px 20px;
            font-size: 0.9375rem;
            font-weight: 600;
            color: #ffffff;
            background-color: var(--bme-primary);
            border: none;
            border-radius: 6px;
            cursor: pointer;
            transition: background-color 0.2s, opacity 0.2s;
            text-align: center;
            font-family: inherit;
            min-height: 44px;
        }

        .lead-btn-submit:hover:not(:disabled) {
            background-color: var(--bme-primary-hover);
        }

        .lead-btn-submit:disabled {
            opacity: 0.65;
            cursor: not-allowed;
        }

        .lead-footer-note {
            margin-top: 14px;
            font-size: 0.75rem;
            color: var(--bme-neutral-500);
            text-align: center;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 4px;
        }
    </style>
</head>
<body>
    <div class="lead-embed-card">
        <header class="lead-embed-header">
            <h2><c:out value="${bieuMau != null ? bieuMau.tenBieuMau : 'Đăng Ký Tư Vấn Giải Pháp'}" /></h2>
            <p>Vui lòng gửi thông tin yêu cầu, đội ngũ chuyên viên sẽ liên hệ và hỗ trợ quý khách sớm nhất.</p>
        </header>

        <!-- Thông báo lỗi hoặc thành công -->
        <div id="alertSuccess" class="lead-alert lead-alert-success" style="display: none;" role="status"></div>
        <div id="alertError" class="lead-alert lead-alert-danger" <c:if test="${empty thongBaoLoi}">style="display: none;"</c:if> role="alert">
            <span class="material-symbols-outlined" style="font-size: 18px;" aria-hidden="true">error</span>
            <span id="alertErrorText"><c:out value="${thongBaoLoi}" /></span>
        </div>

        <c:choose>
            <c:when test="${not empty bieuMau and not bieuMau.hoatDong}">
                <div class="lead-alert lead-alert-danger" style="margin-top: 10px;">
                    <span class="material-symbols-outlined" style="font-size: 18px;" aria-hidden="true">pause_circle</span>
                    <span>Biểu mẫu này hiện đang tạm dừng tiếp nhận thông tin. Quý khách vui lòng liên hệ lại sau.</span>
                </div>
            </c:when>
            <c:otherwise>
                <form id="leadForm" action="${pageContext.request.contextPath}/bieu-mau/gui" method="POST" novalidate>
                    <input type="hidden" name="token" value="<c:out value="${bieuMau != null ? bieuMau.tokenCongKhai : param.token}" />" />

                    <!-- AC3: Chống spam Honeypot ẩn hoàn toàn khỏi người dùng -->
                    <div class="lead-hp-trap" aria-hidden="true">
                        <label for="website_url">Website URL (Vui lòng không điền vào trường này)</label>
                        <input type="text" id="website_url" name="website_url" tabindex="-1" autocomplete="off" />
                    </div>

                    <!-- AC2: Họ và tên -->
                    <div class="lead-form-group">
                        <label class="lead-form-label" for="ho_ten">Họ và tên <span class="required">*</span></label>
                        <input type="text" id="ho_ten" name="ho_ten" class="lead-form-control"
                               placeholder="Nguyễn Văn A" required maxlength="150"
                               value="<c:out value="${dto != null ? dto.hoTen : ''}" />" />
                        <div class="lead-field-error" id="err_ho_ten">Vui lòng nhập họ và tên của bạn.</div>
                    </div>

                    <!-- AC2: Email & Số điện thoại -->
                    <div class="lead-row">
                        <div class="lead-form-group">
                            <label class="lead-form-label" for="email">Địa chỉ Email</label>
                            <input type="email" id="email" name="email" class="lead-form-control"
                                   placeholder="email@company.com" maxlength="255"
                                   value="<c:out value="${dto != null ? dto.email : ''}" />" />
                            <div class="lead-field-error" id="err_email">Định dạng email chưa hợp lệ.</div>
                        </div>
                        <div class="lead-form-group">
                            <label class="lead-form-label" for="so_dien_thoai">Số điện thoại</label>
                            <input type="tel" id="so_dien_thoai" name="so_dien_thoai" class="lead-form-control"
                                   placeholder="0912 345 678" maxlength="20"
                                   value="<c:out value="${dto != null ? dto.soDienThoai : ''}" />" />
                            <div class="lead-field-error" id="err_so_dien_thoai">Số điện thoại phải từ 9 đến 15 chữ số.</div>
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
                                  placeholder="Mô tả nhu cầu, tính năng mong muốn hoặc quy mô người sử dụng..."><c:out value="${dto != null ? dto.nhuCauQuanTam : ''}" /></textarea>
                    </div>

                    <!-- Nút Gửi thông tin (AC2) -->
                    <button type="submit" class="lead-btn-submit" id="btnSubmitLead">
                        <span class="material-symbols-outlined" id="submitIcon" aria-hidden="true">send</span>
                        <span id="submitText">Gửi Thông Tin Yêu Cầu</span>
                    </button>

                    <p class="lead-footer-note">
                        <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">lock</span>
                        <span>Thông tin của quý khách được cam kết bảo mật theo chính sách quyền riêng tư.</span>
                    </p>
                </form>
            </c:otherwise>
        </c:choose>
    </div>

    <script>
        (function() {
            'use strict';
            var form = document.getElementById('leadForm');
            if (!form) return;

            var alertSuccess = document.getElementById('alertSuccess');
            var alertError = document.getElementById('alertError');
            var alertErrorText = document.getElementById('alertErrorText');
            var btnSubmit = document.getElementById('btnSubmitLead');
            var submitText = document.getElementById('submitText');
            var submitIcon = document.getElementById('submitIcon');

            var inpHoTen = document.getElementById('ho_ten');
            var inpEmail = document.getElementById('email');
            var inpSdt = document.getElementById('so_dien_thoai');

            form.addEventListener('submit', function(e) {
                var hoTen = inpHoTen.value.trim();
                var email = inpEmail.value.trim();
                var sdt = inpSdt.value.trim();
                var isValid = true;

                // Reset validation
                inpHoTen.classList.remove('is-invalid');
                inpEmail.classList.remove('is-invalid');
                inpSdt.classList.remove('is-invalid');
                document.getElementById('err_ho_ten').style.display = 'none';
                document.getElementById('err_email').style.display = 'none';
                document.getElementById('err_so_dien_thoai').style.display = 'none';
                alertError.style.display = 'none';
                alertSuccess.style.display = 'none';

                // Validate Họ tên bắt buộc
                if (!hoTen) {
                    inpHoTen.classList.add('is-invalid');
                    document.getElementById('err_ho_ten').style.display = 'block';
                    isValid = false;
                }

                // Validate Email hoặc SĐT bắt buộc phải có ít nhất 1
                if (!email && !sdt) {
                    alertErrorText.textContent = 'Vui lòng cung cấp ít nhất một phương thức liên lạc: Địa chỉ Email hoặc Số điện thoại.';
                    alertError.style.display = 'flex';
                    inpEmail.classList.add('is-invalid');
                    inpSdt.classList.add('is-invalid');
                    isValid = false;
                }

                // Validate định dạng email nếu có nhập
                if (email) {
                    var emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                    if (!emailRegex.test(email)) {
                        inpEmail.classList.add('is-invalid');
                        document.getElementById('err_email').style.display = 'block';
                        isValid = false;
                    }
                }

                // Validate định dạng số điện thoại nếu có nhập
                if (sdt) {
                    var phoneRegex = /^[0-9+\s\-().]{8,20}$/;
                    if (!phoneRegex.test(sdt)) {
                        inpSdt.classList.add('is-invalid');
                        document.getElementById('err_so_dien_thoai').style.display = 'block';
                        isValid = false;
                    }
                }

                if (!isValid) {
                    e.preventDefault();
                    return;
                }

                // Xử lý gửi AJAX mượt mà
                if (window.fetch) {
                    e.preventDefault();
                    btnSubmit.disabled = true;
                    submitText.textContent = 'Đang xử lý gửi thông tin...';
                    if (submitIcon) submitIcon.textContent = 'hourglass_empty';

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
                        submitText.textContent = 'Gửi Thông Tin Yêu Cầu';
                        if (submitIcon) submitIcon.textContent = 'send';

                        if (resObj.ok && resObj.data && resObj.data.thanhCong) {
                            alertSuccess.innerHTML = '<span class="material-symbols-outlined" style="font-size: 18px;" aria-hidden="true">check_circle</span>' +
                                                     '<span>' + (resObj.data.thongBao || 'Gửi thông tin thành công!') +
                                                     (resObj.data.maLead ? ' (Mã yêu cầu: <strong>' + resObj.data.maLead + '</strong>)' : '') + '</span>';
                            alertSuccess.style.display = 'flex';
                            form.reset();
                        } else {
                            var errMsg = (resObj.data && resObj.data.thongBao) ? resObj.data.thongBao : 'Gửi thông tin không thành công. Vui lòng kiểm tra lại.';
                            alertErrorText.textContent = errMsg;
                            alertError.style.display = 'flex';
                        }
                    })
                    .catch(function(err) {
                        btnSubmit.disabled = false;
                        submitText.textContent = 'Gửi Thông Tin Yêu Cầu';
                        if (submitIcon) submitIcon.textContent = 'send';
                        alertErrorText.textContent = 'Không thể kết nối đến máy chủ. Quý khách vui lòng kiểm tra lại đường truyền mạng.';
                        alertError.style.display = 'flex';
                    });
                }
            });
        })();
    </script>
</body>
</html>
