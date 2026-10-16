/**
 * tao-lead.js - Tương tác Form tạo Lead & Live Business Card Preview
 * Story S4-02: AC1 (Tạo thủ công sự kiện/danh thiếp) & AC3 (Bắt buộc nguồn lead)
 */

(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        const form = document.getElementById('formTaoLead');
        if (!form) return;

        // Elements form input
        const inputHoTen = document.getElementById('ho_ten');
        const inputCongTy = document.getElementById('ten_cong_ty');
        const inputPhone = document.getElementById('so_dien_thoai');
        const inputEmail = document.getElementById('email');
        const selectNguon = document.getElementById('nguon_lead_id');
        const btnSubmit = document.getElementById('btnLuuLead');
        const btnLuuTiep = document.getElementById('btnLuuVaTiepTuc');

        // Elements Live Business Card Preview
        const bizName = document.getElementById('bizCardName');
        const bizCompany = document.getElementById('bizCardCompany');
        const bizPhone = document.getElementById('bizCardPhone');
        const bizEmail = document.getElementById('bizCardEmail');
        const bizAvatar = document.getElementById('bizCardAvatar');
        const bizSource = document.getElementById('bizCardSource');

        // Chips chọn nguồn Lead (AC3)
        const chips = document.querySelectorAll('.lead-source-chip');

        /**
         * Lấy 1-2 chữ cái viết tắt từ họ tên làm Avatar
         */
        function getInitials(name) {
            if (!name || !name.trim()) return 'LD';
            const parts = name.trim().split(/\s+/);
            if (parts.length === 1) return parts[0].substring(0, 2).toUpperCase();
            return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
        }

        /**
         * Cập nhật thời gian thực vào thẻ danh thiếp mô phỏng
         */
        function updateLiveCard() {
            if (bizName && inputHoTen) {
                const val = inputHoTen.value.trim();
                bizName.textContent = val ? val : 'Họ và tên khách hàng';
                if (bizAvatar) bizAvatar.textContent = getInitials(val);
            }
            if (bizCompany && inputCongTy) {
                const val = inputCongTy.value.trim();
                bizCompany.textContent = val ? val : 'Doanh nghiệp / Tổ chức';
            }
            if (bizPhone && inputPhone) {
                const val = inputPhone.value.trim();
                bizPhone.textContent = val ? val : 'Chưa nhập số điện thoại';
            }
            if (bizEmail && inputEmail) {
                const val = inputEmail.value.trim();
                bizEmail.textContent = val ? val : 'Chưa nhập email liên hệ';
            }
        }

        // Lắng nghe sự kiện gõ phím để Live Preview
        [inputHoTen, inputCongTy, inputPhone, inputEmail].forEach(function (input) {
            if (input) {
                input.addEventListener('input', updateLiveCard);
                input.addEventListener('change', updateLiveCard);
            }
        });

        // Xử lý Chips chọn nguồn Lead (AC3)
        if (chips.length > 0) {
            chips.forEach(function (chip) {
                chip.addEventListener('click', function () {
                    chips.forEach(function (c) { c.classList.remove('selected'); });
                    chip.classList.add('selected');

                    const val = chip.getAttribute('data-value');
                    const text = chip.querySelector('span:last-child').textContent;

                    if (selectNguon) {
                        selectNguon.value = val;
                        clearError(selectNguon);
                    }
                    if (bizSource) {
                        bizSource.textContent = text;
                    }
                });
            });
        }

        // Khởi tạo thẻ danh thiếp ngay khi load trang
        updateLiveCard();
        if (selectNguon && selectNguon.value) {
            chips.forEach(function (c) {
                if (c.getAttribute('data-value') === selectNguon.value) {
                    c.classList.add('selected');
                    if (bizSource) bizSource.textContent = c.querySelector('span:last-child').textContent;
                } else {
                    c.classList.remove('selected');
                }
            });
        }

        /**
         * Thẩm định lỗi trực quan, tinh tế
         */
        function showError(inputEl, msg) {
            if (!inputEl) return;
            inputEl.classList.add('is-invalid');
            const parent = inputEl.closest('.lead-form-group');
            if (parent) {
                let err = parent.querySelector('.lead-error-msg');
                if (!err) {
                    err = document.createElement('div');
                    err.className = 'lead-error-msg';
                    parent.appendChild(err);
                }
                err.innerHTML = '<span class="material-symbols-outlined" style="font-size:15px;">error</span> ' + msg;
            }
        }

        function clearError(inputEl) {
            if (!inputEl) return;
            inputEl.classList.remove('is-invalid');
            const parent = inputEl.closest('.lead-form-group');
            if (parent) {
                const err = parent.querySelector('.lead-error-msg');
                if (err) err.remove();
            }
        }

        if (inputHoTen) {
            inputHoTen.addEventListener('input', function () {
                if (inputHoTen.value.trim()) clearError(inputHoTen);
            });
        }

        // Submit form
        form.addEventListener('submit', function (e) {
            let isValid = true;
            let firstInvalid = null;

            // 1. Kiểm tra Họ tên
            if (!inputHoTen || !inputHoTen.value.trim()) {
                showError(inputHoTen, 'Vui lòng nhập họ tên của khách hàng.');
                isValid = false;
                if (!firstInvalid) firstInvalid = inputHoTen;
            } else {
                clearError(inputHoTen);
            }

            // 2. Kiểm tra Nguồn Lead (AC3)
            if (!selectNguon || !selectNguon.value.trim()) {
                showError(selectNguon, 'Tiêu chí AC3: Bắt buộc chọn nguồn lead tiếp nhận.');
                isValid = false;
                if (!firstInvalid) firstInvalid = selectNguon;
            } else {
                clearError(selectNguon);
            }

            // 3. Email regex nếu có
            if (inputEmail && inputEmail.value.trim()) {
                const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                if (!emailRegex.test(inputEmail.value.trim())) {
                    showError(inputEmail, 'Email không đúng định dạng chuẩn.');
                    isValid = false;
                    if (!firstInvalid) firstInvalid = inputEmail;
                } else {
                    clearError(inputEmail);
                }
            }

            if (!isValid) {
                e.preventDefault();
                if (firstInvalid) firstInvalid.focus();
                return false;
            }

            // Chống spam double click
            if (btnSubmit) {
                btnSubmit.disabled = true;
                btnSubmit.innerHTML = '<span class="material-symbols-outlined" style="font-size:16px; animation: spin 1s linear infinite;">sync</span> Đang lưu...';
            }
        });

        if (btnLuuTiep) {
            btnLuuTiep.addEventListener('click', function (e) {
                e.preventDefault();
                let hiddenTiep = form.querySelector('input[name="tiepTuc"]');
                if (!hiddenTiep) {
                    hiddenTiep = document.createElement('input');
                    hiddenTiep.type = 'hidden';
                    hiddenTiep.name = 'tiepTuc';
                    hiddenTiep.value = 'true';
                    form.appendChild(hiddenTiep);
                }
                form.requestSubmit ? form.requestSubmit() : form.submit();
            });
        }
    });
})();
