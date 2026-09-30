/**
 * dang-nhap.js - Xử lý tương tác giao diện Đăng Nhập CRM (Story S1-01)
 * Các tính năng:
 * 1. Client-side validation: Kiểm tra email hợp lệ, mật khẩu không rỗng
 * 2. Ẩn / hiện mật khẩu linh hoạt với icon SVG
 * 3. Đồng hồ đếm ngược (Countdown Timer) khi tài khoản bị khóa tạm 15 phút
 * 4. Trạng thái Loading spinner chống double-submit
 * 5. Điền nhanh tài khoản mẫu kiểm thử (Quick Credential Chips)
 */

document.addEventListener('DOMContentLoaded', function () {
    const loginForm = document.getElementById('loginForm');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('matKhau');
    const togglePasswordBtn = document.getElementById('btnTogglePassword');
    const submitBtn = document.getElementById('btnSubmit');
    const emailError = document.getElementById('emailError');
    const passwordError = document.getElementById('passwordError');
    const alertMessage = document.getElementById('alertMessage');
    const countdownTimerContainer = document.getElementById('countdownTimerContainer');
    const countdownTimeDisplay = document.getElementById('countdownTimeDisplay');

    // 1. ẨN / HIỆN MẬT KHẨU
    if (togglePasswordBtn && passwordInput) {
        togglePasswordBtn.addEventListener('click', function () {
            const isPassword = passwordInput.getAttribute('type') === 'password';
            passwordInput.setAttribute('type', isPassword ? 'text' : 'password');

            // Cập nhật SVG icon mắt
            if (isPassword) {
                togglePasswordBtn.innerHTML = `
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                        <line x1="1" y1="1" x2="23" y2="23"></line>
                    </svg>
                `;
                togglePasswordBtn.setAttribute('aria-label', 'Ẩn mật khẩu');
            } else {
                togglePasswordBtn.innerHTML = `
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                        <circle cx="12" cy="12" r="3"></circle>
                    </svg>
                `;
                togglePasswordBtn.setAttribute('aria-label', 'Hiện mật khẩu');
            }
        });
    }

    // 2. CLIENT-SIDE VALIDATION
    function validateEmail(email) {
        if (!email || email.trim() === '') {
            return 'Vui lòng nhập địa chỉ email công ty.';
        }
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email.trim())) {
            return 'Định dạng email không hợp lệ (ví dụ: sales@crm.vn).';
        }
        return '';
    }

    function validatePassword(password) {
        if (!password || password.trim() === '') {
            return 'Vui lòng nhập mật khẩu.';
        }
        return '';
    }

    function showFieldError(inputElem, errorElem, message) {
        if (!inputElem || !errorElem) return;
        if (message) {
            inputElem.classList.add('is-invalid');
            errorElem.textContent = message;
            errorElem.style.display = 'block';
        } else {
            inputElem.classList.remove('is-invalid');
            errorElem.textContent = '';
            errorElem.style.display = 'none';
        }
    }

    if (emailInput && emailError) {
        emailInput.addEventListener('blur', function () {
            showFieldError(emailInput, emailError, validateEmail(emailInput.value));
        });
        emailInput.addEventListener('input', function () {
            if (emailInput.classList.contains('is-invalid')) {
                showFieldError(emailInput, emailError, validateEmail(emailInput.value));
            }
        });
    }

    if (passwordInput && passwordError) {
        passwordInput.addEventListener('blur', function () {
            showFieldError(passwordInput, passwordError, validatePassword(passwordInput.value));
        });
        passwordInput.addEventListener('input', function () {
            if (passwordInput.classList.contains('is-invalid')) {
                showFieldError(passwordInput, passwordError, validatePassword(passwordInput.value));
            }
        });
    }

    // 3. SUBMIT FORM VÀ HIỆU ỨNG LOADING
    if (loginForm && submitBtn) {
        loginForm.addEventListener('submit', function (e) {
            const emailMsg = validateEmail(emailInput.value);
            const pwdMsg = validatePassword(passwordInput.value);

            showFieldError(emailInput, emailError, emailMsg);
            showFieldError(passwordInput, passwordError, pwdMsg);

            if (emailMsg || pwdMsg) {
                e.preventDefault();
                if (emailMsg) emailInput.focus();
                else passwordInput.focus();
                return;
            }

            // Hiển thị trạng thái đang xác thực
            submitBtn.disabled = true;
            submitBtn.classList.add('btn-loading');
            submitBtn.innerHTML = `
                <span class="spinner-border" role="status" aria-hidden="true"></span>
                <span>Đang xác thực bảo mật...</span>
            `;
        });
    }

    // 4. ĐỒNG HỒ ĐẾM NGƯỢC KHÓA TẠM 15 PHÚT (Story AC: Khóa tạm 15 phút sau 5 lần sai liên tiếp)
    if (alertMessage && alertMessage.classList.contains('alert-lockout')) {
        let lockMinutes = 15;
        // Trích xuất số phút từ nội dung thông báo server nếu có
        const matchMinutes = alertMessage.textContent.match(/thử lại sau\s+(\d+)\s+phút/i);
        if (matchMinutes && matchMinutes[1]) {
            lockMinutes = parseInt(matchMinutes[1], 10);
            if (isNaN(lockMinutes) || lockMinutes <= 0) lockMinutes = 15;
        }

        let totalSeconds = lockMinutes * 60;

        if (countdownTimerContainer && countdownTimeDisplay) {
            countdownTimerContainer.style.display = 'flex';

            const updateCountdown = function () {
                const minutes = Math.floor(totalSeconds / 60);
                const seconds = totalSeconds % 60;
                countdownTimeDisplay.textContent = 
                    (minutes < 10 ? '0' : '') + minutes + ':' + 
                    (seconds < 10 ? '0' : '') + seconds;

                if (totalSeconds <= 0) {
                    clearInterval(countdownInterval);
                    countdownTimeDisplay.textContent = '00:00';
                    alertMessage.classList.remove('alert-lockout');
                    alertMessage.classList.add('alert-success');
                    alertMessage.innerHTML = `
                        <div class="alert-icon-wrapper">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                                <polyline points="22 4 12 14.01 9 11.01"></polyline>
                            </svg>
                        </div>
                        <div class="alert-body">
                            <div class="alert-title">Đã hết thời gian khóa</div>
                            <div class="alert-desc">Thời gian khóa tạm đã kết thúc. Bạn có thể thử đăng nhập lại ngay bây giờ.</div>
                        </div>
                    `;
                    if (submitBtn) {
                        submitBtn.disabled = false;
                    }
                }
                totalSeconds--;
            };

            updateCountdown();
            const countdownInterval = setInterval(updateCountdown, 1000);
        }
    }

    // 5. CHỨC NĂNG ĐIỀN NHANH TÀI KHOẢN MẪU KIỂM THỬ (Quick Fill Chips)
    const quickChips = document.querySelectorAll('.quick-fill-chip');
    quickChips.forEach(function (chip) {
        chip.addEventListener('click', function () {
            const sampleEmail = chip.getAttribute('data-email');
            const samplePassword = chip.getAttribute('data-password');

            if (emailInput && sampleEmail) {
                emailInput.value = sampleEmail;
                showFieldError(emailInput, emailError, '');
            }
            if (passwordInput && samplePassword) {
                passwordInput.value = samplePassword;
                showFieldError(passwordInput, passwordError, '');
            }

            // Hiệu ứng visual active nhẹ cho chip
            quickChips.forEach(c => c.classList.remove('active'));
            chip.classList.add('active');

            if (passwordInput) {
                passwordInput.focus();
            }
        });
    });
});
