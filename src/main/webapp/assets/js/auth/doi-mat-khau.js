/**
 * JavaScript: doi-mat-khau.js
 * Story: S1-04 - Đổi mật khẩu và thu hồi các phiên đăng nhập khác
 * Author: Hứa Lan Hương (FE)
 * Project: CRM Bán Hàng - Nhóm 10
 */

(function () {
    'use strict';

    // DOM Elements
    const form = document.getElementById('form-doi-mat-khau');
    const inputHienTai = document.getElementById('mat-khau-hien-tai');
    const inputMoi = document.getElementById('mat-khau-moi');
    const inputXacNhan = document.getElementById('xac-nhan-mat-khau');
    const checkThuHoiPhien = document.getElementById('thu-hoi-phien-khac');
    
    // Error & Feedback Elements
    const errHienTai = document.getElementById('err-mat-khau-hien-tai');
    const errMoi = document.getElementById('err-mat-khau-moi');
    const errXacNhan = document.getElementById('err-xac-nhan-mat-khau');
    const matchStatus = document.getElementById('match-status');
    const alertFeedback = document.getElementById('alert-client-feedback');
    const feedbackHeading = document.getElementById('feedback-heading');
    const feedbackMessage = document.getElementById('feedback-message');
    const feedbackIcon = document.getElementById('feedback-icon');
    const btnCloseFeedback = document.getElementById('btn-close-feedback');
    
    // Checklist Rules (AC 2)
    const ruleMinLength = document.getElementById('rule-min-length');
    const ruleHasLetter = document.getElementById('rule-has-letter');
    const ruleHasNumber = document.getElementById('rule-has-number');
    const ruleDiffCurrent = document.getElementById('rule-diff-current');
    
    // Strength Meter Elements
    const strengthText = document.getElementById('strength-text');
    const bars = [
        document.getElementById('bar-1'),
        document.getElementById('bar-2'),
        document.getElementById('bar-3'),
        document.getElementById('bar-4')
    ];
    
    // Buttons
    const btnSubmit = document.getElementById('btn-submit-doi-mat-khau');
    const btnSpinner = document.getElementById('btn-spinner');
    const btnIcon = document.getElementById('btn-icon');
    const btnText = document.getElementById('btn-text');
    const toggleBtns = document.querySelectorAll('.btn-toggle-pwd');

    /**
     * Khởi tạo các sự kiện khi DOM tải xong
     */
    function init() {
        if (!form) return;

        // Toggle ẩn/hiện mật khẩu
        initPasswordToggles();

        // Lắng nghe sự kiện input trường Mật khẩu hiện tại
        if (inputHienTai) {
            inputHienTai.addEventListener('input', handleCurrentPasswordInput);
            inputHienTai.addEventListener('blur', validateCurrentPassword);
        }

        // Lắng nghe sự kiện input trường Mật khẩu mới (Real-time Checklist & Strength Meter)
        if (inputMoi) {
            inputMoi.addEventListener('input', handleNewPasswordInput);
            inputMoi.addEventListener('blur', validateNewPassword);
        }

        // Lắng nghe sự kiện input trường Xác nhận mật khẩu mới
        if (inputXacNhan) {
            inputXacNhan.addEventListener('input', handleConfirmPasswordInput);
            inputXacNhan.addEventListener('blur', validateConfirmPassword);
        }

        // Đóng alert phản hồi
        if (btnCloseFeedback) {
            btnCloseFeedback.addEventListener('click', hideClientAlert);
        }

        // Submit form
        form.addEventListener('submit', handleFormSubmit);
    }

    /**
     * Khởi tạo tính năng ẩn/hiện mật khẩu (Eye button)
     */
    function initPasswordToggles() {
        toggleBtns.forEach(function (btn) {
            btn.addEventListener('click', function () {
                const targetId = btn.getAttribute('data-target');
                const targetInput = document.getElementById(targetId);
                if (!targetInput) return;

                const isPassword = targetInput.type === 'password';
                targetInput.type = isPassword ? 'text' : 'password';

                const eyeOpen = btn.querySelector('.eye-open');
                const eyeClosed = btn.querySelector('.eye-closed');

                if (isPassword) {
                    eyeOpen.classList.add('d-none');
                    eyeClosed.classList.remove('d-none');
                    btn.setAttribute('aria-label', 'Ẩn mật khẩu');
                } else {
                    eyeOpen.classList.remove('d-none');
                    eyeClosed.classList.add('d-none');
                    btn.setAttribute('aria-label', 'Hiện mật khẩu');
                }
            });
        });
    }

    /**
     * Xử lý khi người dùng nhập mật khẩu hiện tại
     */
    function handleCurrentPasswordInput() {
        clearFieldError(inputHienTai, errHienTai);
        // Cập nhật lại quy tắc khác mật khẩu hiện tại
        if (inputMoi && inputMoi.value) {
            updateChecklist(inputMoi.value);
        }
    }

    /**
     * AC 1: Bắt buộc nhập mật khẩu hiện tại
     * @returns {boolean}
     */
    function validateCurrentPassword() {
        if (!inputHienTai) return true;
        const val = inputHienTai.value.trim();
        if (val.length === 0) {
            setFieldError(inputHienTai, errHienTai, 'Bắt buộc nhập mật khẩu hiện tại để xác thực tài khoản.');
            return false;
        }
        clearFieldError(inputHienTai, errHienTai);
        return true;
    }

    /**
     * Xử lý real-time khi người dùng nhập Mật khẩu mới
     */
    function handleNewPasswordInput() {
        const val = inputMoi.value;
        clearFieldError(inputMoi, errMoi);
        updateChecklist(val);
        updateStrengthMeter(val);

        // Nếu trường xác nhận đã có giá trị thì kiểm tra khớp lại
        if (inputXacNhan && inputXacNhan.value) {
            checkPasswordMatch();
        }
    }

    /**
     * AC 2: Cập nhật danh sách tiêu chuẩn mật khẩu mới
     * - Tối thiểu 8 ký tự
     * - Có ít nhất 1 chữ cái
     * - Có ít nhất 1 chữ số
     * - Khác mật khẩu hiện tại
     * @param {string} val
     */
    function updateChecklist(val) {
        const currentVal = inputHienTai ? inputHienTai.value : '';

        const isMinLength = val.length >= 8;
        const hasLetter = /[a-zA-Z]/.test(val);
        const hasNumber = /[0-9]/.test(val);
        const isDifferent = val.length > 0 && val !== currentVal;

        setRuleStatus(ruleMinLength, isMinLength);
        setRuleStatus(ruleHasLetter, hasLetter);
        setRuleStatus(ruleHasNumber, hasNumber);
        setRuleStatus(ruleDiffCurrent, isDifferent);
    }

    /**
     * Cập nhật trạng thái từng dòng tiêu chuẩn (Check/Cross icon)
     * @param {HTMLElement} ruleEl
     * @param {boolean} isMet
     */
    function setRuleStatus(ruleEl, isMet) {
        if (!ruleEl) return;
        const icon = ruleEl.querySelector('.rule-icon');
        if (isMet) {
            ruleEl.classList.add('met');
            if (icon) icon.innerHTML = '&#10003;'; // Checkmark
        } else {
            ruleEl.classList.remove('met');
            if (icon) icon.innerHTML = '&#10005;'; // Cross
        }
    }

    /**
     * AC 2: Tính toán và hiển thị độ mạnh mật khẩu (Strength Meter)
     * @param {string} val
     */
    function updateStrengthMeter(val) {
        if (!strengthText) return;

        if (!val || val.length === 0) {
            strengthText.textContent = 'Chưa nhập';
            strengthText.className = 'strength-level';
            resetBars();
            return;
        }

        let score = 0;
        if (val.length >= 8) score++;
        if (/[a-zA-Z]/.test(val)) score++;
        if (/[0-9]/.test(val)) score++;
        if (/[^a-zA-Z0-9]/.test(val) || val.length >= 12) score++; // Bonus ký tự đặc biệt hoặc rất dài

        resetBars();

        if (score <= 1) {
            strengthText.textContent = 'Yếu';
            strengthText.className = 'strength-level weak';
            paintBars(1, '#ef4444');
        } else if (score === 2) {
            strengthText.textContent = 'Trung bình';
            strengthText.className = 'strength-level medium';
            paintBars(2, '#f59e0b');
        } else if (score === 3) {
            strengthText.textContent = 'Khá';
            strengthText.className = 'strength-level good';
            paintBars(3, '#0284c7');
        } else {
            strengthText.textContent = 'Mạnh';
            strengthText.className = 'strength-level strong';
            paintBars(4, '#10b981');
        }
    }

    function resetBars() {
        bars.forEach(function (bar) {
            if (bar) {
                bar.style.backgroundColor = 'var(--slate-200)';
            }
        });
    }

    function paintBars(count, color) {
        for (let i = 0; i < count; i++) {
            if (bars[i]) {
                bars[i].style.backgroundColor = color;
            }
        }
    }

    /**
     * AC 2: Kiểm tra mật khẩu mới hợp lệ
     * @returns {boolean}
     */
    function validateNewPassword() {
        if (!inputMoi) return true;
        const val = inputMoi.value;
        const currentVal = inputHienTai ? inputHienTai.value : '';

        if (!val || val.length === 0) {
            setFieldError(inputMoi, errMoi, 'Vui lòng nhập mật khẩu mới.');
            return false;
        }

        if (val.length < 8) {
            setFieldError(inputMoi, errMoi, 'Mật khẩu mới phải có tối thiểu 8 ký tự.');
            return false;
        }

        if (!/[a-zA-Z]/.test(val)) {
            setFieldError(inputMoi, errMoi, 'Mật khẩu mới phải chứa ít nhất một chữ cái (a-z hoặc A-Z).');
            return false;
        }

        if (!/[0-9]/.test(val)) {
            setFieldError(inputMoi, errMoi, 'Mật khẩu mới phải chứa ít nhất một chữ số (0-9).');
            return false;
        }

        if (currentVal && val === currentVal) {
            setFieldError(inputMoi, errMoi, 'Mật khẩu mới không được trùng với mật khẩu hiện tại.');
            return false;
        }

        clearFieldError(inputMoi, errMoi);
        return true;
    }

    /**
     * Xử lý real-time khi người dùng nhập Xác nhận mật khẩu mới
     */
    function handleConfirmPasswordInput() {
        clearFieldError(inputXacNhan, errXacNhan);
        checkPasswordMatch();
    }

    /**
     * Kiểm tra hai mật khẩu có khớp nhau không
     * @returns {boolean}
     */
    function checkPasswordMatch() {
        if (!inputXacNhan || !inputMoi || !matchStatus) return false;
        const pass = inputMoi.value;
        const confirm = inputXacNhan.value;

        if (confirm.length === 0) {
            matchStatus.textContent = '';
            matchStatus.className = 'match-status';
            return false;
        }

        if (pass === confirm) {
            matchStatus.innerHTML = '&#10003; Mật khẩu xác nhận trùng khớp';
            matchStatus.className = 'match-status match';
            clearFieldError(inputXacNhan, errXacNhan);
            return true;
        } else {
            matchStatus.innerHTML = '&#10005; Mật khẩu xác nhận chưa trùng khớp';
            matchStatus.className = 'match-status mismatch';
            return false;
        }
    }

    /**
     * Validate trường xác nhận mật khẩu khi blur
     * @returns {boolean}
     */
    function validateConfirmPassword() {
        if (!inputXacNhan) return true;
        const confirm = inputXacNhan.value;
        const pass = inputMoi ? inputMoi.value : '';

        if (!confirm || confirm.length === 0) {
            setFieldError(inputXacNhan, errXacNhan, 'Vui lòng xác nhận lại mật khẩu mới.');
            return false;
        }

        if (confirm !== pass) {
            setFieldError(inputXacNhan, errXacNhan, 'Mật khẩu xác nhận không khớp với mật khẩu mới.');
            return false;
        }

        clearFieldError(inputXacNhan, errXacNhan);
        return true;
    }

    /**
     * Gắn lỗi cho trường nhập liệu
     */
    function setFieldError(inputEl, errorEl, message) {
        if (inputEl) {
            inputEl.classList.add('is-invalid');
            inputEl.setAttribute('aria-invalid', 'true');
        }
        if (errorEl) {
            errorEl.textContent = message;
            errorEl.classList.add('active');
        }
    }

    /**
     * Xóa lỗi trường nhập liệu
     */
    function clearFieldError(inputEl, errorEl) {
        if (inputEl) {
            inputEl.classList.remove('is-invalid');
            inputEl.removeAttribute('aria-invalid');
        }
        if (errorEl) {
            errorEl.textContent = '';
            errorEl.classList.remove('active');
        }
    }

    /**
     * Hiển thị alert phản hồi phía client
     */
    function showClientAlert(type, title, message) {
        if (!alertFeedback) return;
        alertFeedback.className = 'alert ' + (type === 'success' ? 'alert-success' : 'alert-error');
        if (feedbackHeading) feedbackHeading.textContent = title;
        if (feedbackMessage) feedbackMessage.textContent = message;
        if (feedbackIcon) {
            if (type === 'success') {
                feedbackIcon.innerHTML = '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>';
            } else {
                feedbackIcon.innerHTML = '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>';
            }
        }
        alertFeedback.classList.remove('d-none');
        alertFeedback.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }

    function hideClientAlert() {
        if (alertFeedback) {
            alertFeedback.classList.add('d-none');
        }
    }

    /**
     * Xử lý submit Form đổi mật khẩu
     * Kiểm tra toàn bộ Acceptance Criteria trước khi gửi
     */
    function handleFormSubmit(e) {
        hideClientAlert();

        const isCurrentValid = validateCurrentPassword();
        const isNewValid = validateNewPassword();
        const isConfirmValid = validateConfirmPassword();

        if (!isCurrentValid || !isNewValid || !isConfirmValid) {
            e.preventDefault();
            
            // Focus vào trường không hợp lệ đầu tiên
            if (!isCurrentValid && inputHienTai) {
                inputHienTai.focus();
            } else if (!isNewValid && inputMoi) {
                inputMoi.focus();
            } else if (!isConfirmValid && inputXacNhan) {
                inputXacNhan.focus();
            }

            showClientAlert('error', 'Dữ liệu chưa hợp lệ', 'Vui lòng kiểm tra lại các trường thông tin có viền đỏ bên dưới.');
            return false;
        }

        // AC 3: Kiểm tra thu hồi các phiên đăng nhập khác
        const isRevokeOtherSessions = checkThuHoiPhien && checkThuHoiPhien.checked;

        // Trạng thái Loading của nút Submit
        if (btnSubmit) {
            btnSubmit.disabled = true;
            if (btnSpinner) btnSpinner.classList.remove('d-none');
            if (btnIcon) btnIcon.classList.add('d-none');
            if (btnText) {
                btnText.textContent = isRevokeOtherSessions
                    ? 'Đang đổi mật khẩu và thu hồi phiên...'
                    : 'Đang lưu mật khẩu mới...';
            }
        }

        // Form hợp lệ, tiếp tục gửi POST request tới Servlet
        return true;
    }

    // Khởi chạy khi DOM sẵn sàng
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();
