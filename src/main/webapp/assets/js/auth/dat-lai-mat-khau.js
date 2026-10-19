/**
 * JavaScript: dat-lai-mat-khau.js
 * Story: S1-03 - Đặt lại mật khẩu mới qua token
 * Author: Hứa Lan Hương (FE)
 * Project: CRM Bán Hàng - Nhóm 10
 */

(function () {
    'use strict';

    const form = document.getElementById('form-dat-lai-mat-khau');
    const inputMoi = document.getElementById('matKhauMoi');
    const inputXacNhan = document.getElementById('xacNhanMatKhau');
    const errMoi = document.getElementById('err-mat-khau-moi');
    const errXacNhan = document.getElementById('err-xac-nhan-mat-khau');
    const matchStatus = document.getElementById('match-status');

    // Checklist
    const ruleMinLength = document.getElementById('rule-min-length');
    const ruleHasLetter = document.getElementById('rule-has-letter');
    const ruleHasNumber = document.getElementById('rule-has-number');

    // Strength
    const strengthText = document.getElementById('strength-text');
    const bars = [
        document.getElementById('bar-1'),
        document.getElementById('bar-2'),
        document.getElementById('bar-3'),
        document.getElementById('bar-4')
    ];

    // Buttons
    const btnSubmit = document.getElementById('btn-xac-nhan-doi-mat-khau');
    const btnSpinner = document.getElementById('btn-spinner');
    const btnText = document.getElementById('btn-text');
    const toggleBtns = document.querySelectorAll('.btn-toggle-pwd');

    function init() {
        initPasswordToggles();

        if (inputMoi) {
            inputMoi.addEventListener('input', handleNewPasswordInput);
            inputMoi.addEventListener('blur', validateNewPassword);
        }

        if (inputXacNhan) {
            inputXacNhan.addEventListener('input', handleConfirmPasswordInput);
            inputXacNhan.addEventListener('blur', validateConfirmPassword);
        }

        if (form) {
            form.addEventListener('submit', handleFormSubmit);
        }
    }

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
                    if (eyeOpen) eyeOpen.classList.add('d-none');
                    if (eyeClosed) eyeClosed.classList.remove('d-none');
                    btn.setAttribute('aria-label', 'Ẩn mật khẩu');
                } else {
                    if (eyeOpen) eyeOpen.classList.remove('d-none');
                    if (eyeClosed) eyeClosed.classList.add('d-none');
                    btn.setAttribute('aria-label', 'Hiện mật khẩu');
                }
            });
        });
    }

    function handleNewPasswordInput() {
        const val = inputMoi.value;
        clearError(inputMoi, errMoi);
        updateChecklist(val);
        updateStrength(val);

        if (inputXacNhan && inputXacNhan.value) {
            checkPasswordMatch();
        }
    }

    function updateChecklist(val) {
        const isMinLength = val.length >= 8;
        const hasLetter = /[a-zA-Z]/.test(val);
        const hasNumber = /[0-9]/.test(val);

        setRuleStatus(ruleMinLength, isMinLength);
        setRuleStatus(ruleHasLetter, hasLetter);
        setRuleStatus(ruleHasNumber, hasNumber);
    }

    function setRuleStatus(ruleEl, isMet) {
        if (!ruleEl) return;
        const icon = ruleEl.querySelector('.rule-icon');
        if (isMet) {
            ruleEl.classList.add('met');
            if (icon) icon.innerHTML = '<span class="material-symbols-outlined icon-xs" aria-hidden="true">check</span>';
        } else {
            ruleEl.classList.remove('met');
            if (icon) icon.innerHTML = '<span class="material-symbols-outlined icon-xs" aria-hidden="true">close</span>';
        }
    }

    function updateStrength(val) {
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
        if (/[^a-zA-Z0-9]/.test(val) || val.length >= 12) score++;

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
            if (bar) bar.style.backgroundColor = 'var(--slate-200)';
        });
    }

    function paintBars(count, color) {
        for (let i = 0; i < count; i++) {
            if (bars[i]) bars[i].style.backgroundColor = color;
        }
    }

    function validateNewPassword() {
        if (!inputMoi) return true;
        const val = inputMoi.value;

        if (!val || val.length === 0) {
            showError(inputMoi, errMoi, 'Vui lòng nhập mật khẩu mới.');
            return false;
        }

        if (val.length < 8) {
            showError(inputMoi, errMoi, 'Mật khẩu phải có tối thiểu 8 ký tự.');
            return false;
        }

        if (!/[a-zA-Z]/.test(val)) {
            showError(inputMoi, errMoi, 'Mật khẩu phải chứa ít nhất một chữ cái (a-z, A-Z).');
            return false;
        }

        if (!/[0-9]/.test(val)) {
            showError(inputMoi, errMoi, 'Mật khẩu phải chứa ít nhất một chữ số (0-9).');
            return false;
        }

        clearError(inputMoi, errMoi);
        return true;
    }

    function handleConfirmPasswordInput() {
        clearError(inputXacNhan, errXacNhan);
        checkPasswordMatch();
    }

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
            matchStatus.innerHTML = '<span class="material-symbols-outlined icon-xs" aria-hidden="true">check_circle</span> Mật khẩu xác nhận trùng khớp';
            matchStatus.className = 'match-status match';
            clearError(inputXacNhan, errXacNhan);
            return true;
        } else {
            matchStatus.innerHTML = '<span class="material-symbols-outlined icon-xs" aria-hidden="true">cancel</span> Mật khẩu xác nhận chưa trùng khớp';
            matchStatus.className = 'match-status mismatch';
            return false;
        }
    }

    function validateConfirmPassword() {
        if (!inputXacNhan) return true;
        const confirm = inputXacNhan.value;
        const pass = inputMoi ? inputMoi.value : '';

        if (!confirm || confirm.length === 0) {
            showError(inputXacNhan, errXacNhan, 'Vui lòng xác nhận mật khẩu mới.');
            return false;
        }

        if (confirm !== pass) {
            showError(inputXacNhan, errXacNhan, 'Mật khẩu xác nhận không trùng khớp.');
            return false;
        }

        clearError(inputXacNhan, errXacNhan);
        return true;
    }

    function showError(inputEl, errorEl, message) {
        if (inputEl) {
            inputEl.classList.add('is-invalid');
            inputEl.setAttribute('aria-invalid', 'true');
        }
        if (errorEl) {
            errorEl.textContent = message;
            errorEl.classList.add('active');
        }
    }

    function clearError(inputEl, errorEl) {
        if (inputEl) {
            inputEl.classList.remove('is-invalid');
            inputEl.removeAttribute('aria-invalid');
        }
        if (errorEl) {
            errorEl.textContent = '';
            errorEl.classList.remove('active');
        }
    }

    function handleFormSubmit(e) {
        const isNewValid = validateNewPassword();
        const isConfirmValid = validateConfirmPassword();

        if (!isNewValid || !isConfirmValid) {
            e.preventDefault();
            if (!isNewValid && inputMoi) inputMoi.focus();
            else if (!isConfirmValid && inputXacNhan) inputXacNhan.focus();
            return false;
        }

        if (btnSubmit) {
            btnSubmit.disabled = true;
            if (btnSpinner) btnSpinner.classList.remove('d-none');
            if (btnText) btnText.textContent = 'Đang cập nhật mật khẩu...';
        }

        return true;
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
