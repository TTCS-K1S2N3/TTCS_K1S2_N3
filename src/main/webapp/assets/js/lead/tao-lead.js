/**
 * tao-lead.js - Xử lý tương tác & Thẩm định Form tạo Lead thủ công (Story S4-02 / AC1, AC3)
 * Dự án: MSys CRM - Module Lead & Phân bổ
 * Tuân thủ quy tắc FE:
 * - Validate chặt chẽ phía Client trước khi gửi (Họ tên, Nguồn lead bắt buộc theo AC3)
 * - Chống double-submit
 * - Accessible error handling & focus management
 */

(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        const form = document.getElementById('formTaoLead');
        if (!form) return;

        const inputHoTen = document.getElementById('ho_ten');
        const selectNguonLead = document.getElementById('nguon_lead_id');
        const inputEmail = document.getElementById('email');
        const inputSoDienThoai = document.getElementById('so_dien_thoai');
        const btnSubmit = document.getElementById('btnLuuLead');
        const btnLuuVaTiepTuc = document.getElementById('btnLuuVaTiepTuc');

        // Regex kiểm tra định dạng
        const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        const PHONE_REGEX = /^(0|\+84)[0-9]{9,10}$/;

        /**
         * Hiển thị thông báo lỗi cho một input cụ thể
         */
        function showError(inputElement, errorMsg) {
            if (!inputElement) return;
            inputElement.classList.add('is-invalid');
            const parent = inputElement.closest('.lead-form-group');
            if (parent) {
                let errorContainer = parent.querySelector('.lead-error-msg');
                if (!errorContainer) {
                    errorContainer = document.createElement('div');
                    errorContainer.className = 'lead-error-msg';
                    errorContainer.setAttribute('role', 'alert');
                    parent.appendChild(errorContainer);
                }
                errorContainer.innerHTML = '<span class="material-symbols-outlined" style="font-size:16px;">error</span> ' + errorMsg;
            }
        }

        /**
         * Xóa thông báo lỗi của input
         */
        function clearError(inputElement) {
            if (!inputElement) return;
            inputElement.classList.remove('is-invalid');
            const parent = inputElement.closest('.lead-form-group');
            if (parent) {
                const errorContainer = parent.querySelector('.lead-error-msg');
                if (errorContainer) {
                    errorContainer.remove();
                }
            }
        }

        /**
         * Thẩm định toàn bộ form
         */
        function validateForm() {
            let isValid = true;
            let firstInvalidElement = null;

            // 1. Thẩm định Họ và tên (Bắt buộc)
            if (!inputHoTen || !inputHoTen.value.trim()) {
                showError(inputHoTen, 'Vui lòng nhập họ và tên của khách hàng tiềm năng.');
                isValid = false;
                if (!firstInvalidElement) firstInvalidElement = inputHoTen;
            } else if (inputHoTen.value.trim().length > 150) {
                showError(inputHoTen, 'Họ và tên không được vượt quá 150 ký tự.');
                isValid = false;
                if (!firstInvalidElement) firstInvalidElement = inputHoTen;
            } else {
                clearError(inputHoTen);
            }

            // 2. Thẩm định Nguồn Lead (Bắt buộc - AC3)
            if (!selectNguonLead || !selectNguonLead.value.trim()) {
                showError(selectNguonLead, 'Mỗi lead nhập vào đều bắt buộc có nguồn (Tiêu chí AC3). Vui lòng chọn nguồn lead.');
                isValid = false;
                if (!firstInvalidElement) firstInvalidElement = selectNguonLead;
            } else {
                clearError(selectNguonLead);
            }

            // 3. Thẩm định Email (Nếu có nhập thì phải đúng định dạng)
            if (inputEmail && inputEmail.value.trim()) {
                const emailVal = inputEmail.value.trim();
                if (!EMAIL_REGEX.test(emailVal)) {
                    showError(inputEmail, 'Địa chỉ email không đúng định dạng (Ví dụ: ten@congty.vn).');
                    isValid = false;
                    if (!firstInvalidElement) firstInvalidElement = inputEmail;
                } else {
                    clearError(inputEmail);
                }
            } else if (inputEmail) {
                clearError(inputEmail);
            }

            // 4. Thẩm định Số điện thoại (Nếu có nhập thì phải hợp lệ)
            if (inputSoDienThoai && inputSoDienThoai.value.trim()) {
                const phoneVal = inputSoDienThoai.value.trim().replace(/[\s.-]/g, '');
                if (!PHONE_REGEX.test(phoneVal)) {
                    showError(inputSoDienThoai, 'Số điện thoại phải gồm 10 chữ số (bắt đầu bằng 0 hoặc +84).');
                    isValid = false;
                    if (!firstInvalidElement) firstInvalidElement = inputSoDienThoai;
                } else {
                    clearError(inputSoDienThoai);
                }
            } else if (inputSoDienThoai) {
                clearError(inputSoDienThoai);
            }

            if (!isValid && firstInvalidElement) {
                firstInvalidElement.focus();
            }

            return isValid;
        }

        // Lắng nghe sự kiện input/change để xóa lỗi ngay khi người dùng gõ
        if (inputHoTen) {
            inputHoTen.addEventListener('input', function () {
                if (inputHoTen.value.trim()) clearError(inputHoTen);
            });
        }

        if (selectNguonLead) {
            selectNguonLead.addEventListener('change', function () {
                if (selectNguonLead.value.trim()) clearError(selectNguonLead);
            });
        }

        if (inputEmail) {
            inputEmail.addEventListener('input', function () {
                clearError(inputEmail);
            });
        }

        if (inputSoDienThoai) {
            inputSoDienThoai.addEventListener('input', function () {
                clearError(inputSoDienThoai);
            });
        }

        // Xử lý submit
        form.addEventListener('submit', function (e) {
            if (!validateForm()) {
                e.preventDefault();
                return false;
            }

            // Chống double-click
            if (btnSubmit) {
                btnSubmit.disabled = true;
                const originalHtml = btnSubmit.innerHTML;
                btnSubmit.innerHTML = '<span class="material-symbols-outlined" style="animation: spin 1s linear infinite;">sync</span> Đang lưu...';
            }
        });

        // Nút Lưu và Tiếp tục
        if (btnLuuVaTiepTuc) {
            btnLuuVaTiepTuc.addEventListener('click', function (e) {
                e.preventDefault();
                let inputAction = form.querySelector('input[name="tiepTuc"]');
                if (!inputAction) {
                    inputAction = document.createElement('input');
                    inputAction.type = 'hidden';
                    inputAction.name = 'tiepTuc';
                    inputAction.value = 'true';
                    form.appendChild(inputAction);
                } else {
                    inputAction.value = 'true';
                }
                form.requestSubmit ? form.requestSubmit() : form.submit();
            });
        }
    });
})();
