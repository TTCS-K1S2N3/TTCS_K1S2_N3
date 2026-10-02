/**
 * JavaScript: quen-mat-khau.js
 * Story: S1-03 - Quên mật khẩu qua email
 * Author: Hứa Lan Hương (FE)
 * Project: CRM Bán Hàng - Nhóm 10
 */

(function () {
    'use strict';

    const form = document.getElementById('form-quen-mat-khau');
    const inputEmail = document.getElementById('email');
    const errEmail = document.getElementById('err-email');
    const btnSubmit = document.getElementById('btn-gui-yeu-cau');
    const btnSpinner = document.getElementById('btn-spinner');
    const btnText = document.getElementById('btn-text');
    const btnIcon = document.getElementById('btn-icon');
    const resendBtn = document.getElementById('btn-resend-countdown');

    /**
     * Khởi tạo sự kiện
     */
    function init() {
        if (inputEmail) {
            inputEmail.addEventListener('input', handleEmailInput);
            inputEmail.addEventListener('blur', validateEmail);
        }

        if (form) {
            form.addEventListener('submit', handleFormSubmit);
        }

        // Đếm ngược gửi lại nếu có hộp thông báo thành công
        if (resendBtn) {
            startResendCountdown(60);
        }
    }

    /**
     * Xử lý input email real-time
     */
    function handleEmailInput() {
        clearError();
    }

    /**
     * Kiểm tra định dạng email
     * @returns {boolean}
     */
    function validateEmail() {
        if (!inputEmail) return true;
        const val = inputEmail.value.trim();

        if (val.length === 0) {
            showError('Vui lòng nhập địa chỉ email công ty của bạn.');
            return false;
        }

        const emailRegex = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
        if (!emailRegex.test(val)) {
            showError('Định dạng email chưa đúng (Ví dụ: sales@crmbanhang.vn).');
            return false;
        }

        clearError();
        return true;
    }

    function showError(message) {
        if (inputEmail) {
            inputEmail.classList.add('is-invalid');
            inputEmail.setAttribute('aria-invalid', 'true');
        }
        if (errEmail) {
            errEmail.textContent = message;
            errEmail.classList.add('active');
        }
    }

    function clearError() {
        if (inputEmail) {
            inputEmail.classList.remove('is-invalid');
            inputEmail.removeAttribute('aria-invalid');
        }
        if (errEmail) {
            errEmail.textContent = '';
            errEmail.classList.remove('active');
        }
    }

    /**
     * Xử lý gửi form
     */
    function handleFormSubmit(e) {
        if (!validateEmail()) {
            e.preventDefault();
            if (inputEmail) inputEmail.focus();
            return false;
        }

        // Bật trạng thái loading để tránh bấm đúp gửi nhiều lần
        if (btnSubmit) {
            btnSubmit.disabled = true;
            if (btnSpinner) btnSpinner.classList.remove('d-none');
            if (btnIcon) btnIcon.classList.add('d-none');
            if (btnText) btnText.textContent = 'Đang gửi liên kết...';
        }

        return true;
    }

    /**
     * Đếm ngược 60 giây gửi lại email
     */
    function startResendCountdown(seconds) {
        let count = seconds;
        resendBtn.disabled = true;
        resendBtn.style.pointerEvents = 'none';
        resendBtn.style.opacity = '0.6';

        const timer = setInterval(function () {
            count--;
            if (count > 0) {
                resendBtn.textContent = 'Gửi lại sau (' + count + 's)';
            } else {
                clearInterval(timer);
                resendBtn.textContent = 'Gửi lại yêu cầu';
                resendBtn.disabled = false;
                resendBtn.style.pointerEvents = 'auto';
                resendBtn.style.opacity = '1';
                resendBtn.addEventListener('click', function () {
                    if (form) {
                        if (typeof form.requestSubmit === 'function') {
                            form.requestSubmit();
                        } else {
                            form.submit();
                        }
                    }
                });
            }
        }, 1000);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
