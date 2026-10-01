/**
 * JS Module: Quản lý Người Dùng (S1-08 FE)
 * Xử lý: tìm kiếm, validation form tạo/sửa tài khoản, checkbox vai trò
 */

'use strict';

// ============================================================
// VALIDATION FORM TẠO / SỬA TÀI KHOẢN
// ============================================================

const NguoiDungForm = {

    /**
     * Khởi tạo validation form
     * @param {string} formId - ID của form
     */
    init(formId) {
        const form = document.getElementById(formId);
        if (!form) return;

        form.addEventListener('submit', (e) => {
            if (!this.validateForm(form)) {
                e.preventDefault();
            }
        });

        // Real-time validation khi người dùng rời field
        form.querySelectorAll('input, select').forEach(field => {
            field.addEventListener('blur', () => {
                this.validateField(field);
            });
            field.addEventListener('input', () => {
                this.clearFieldError(field);
            });
        });

        // Xử lý checkbox group vai trò
        this.initCheckboxGroup();
    },

    /**
     * Validate toàn bộ form
     * @returns {boolean}
     */
    validateForm(form) {
        let valid = true;
        const fields = form.querySelectorAll('input[required], select[required]');
        fields.forEach(field => {
            if (!this.validateField(field)) valid = false;
        });

        // Kiểm tra ít nhất 1 vai trò được chọn
        const roleCheckboxes = form.querySelectorAll('input[name="vaiTroIds"]');
        if (roleCheckboxes.length > 0) {
            const atLeastOne = Array.from(roleCheckboxes).some(cb => cb.checked);
            if (!atLeastOne) {
                const roleError = document.getElementById('vaiTro-error');
                if (roleError) {
                    roleError.textContent = 'Phải chọn ít nhất một vai trò.';
                    roleError.classList.add('visible');
                }
                valid = false;
            }
        }

        return valid;
    },

    /**
     * Validate một field
     * @param {HTMLElement} field
     * @returns {boolean}
     */
    validateField(field) {
        const value = field.value.trim();
        const fieldId = field.id;
        let errorMsg = '';

        if (field.required && !value) {
            errorMsg = 'Trường này không được để trống.';
        } else if (fieldId === 'email' && value) {
            if (!this.isValidEmail(value)) {
                errorMsg = 'Địa chỉ email không đúng định dạng.';
            }
        } else if (fieldId === 'hoTen' && value && value.length < 2) {
            errorMsg = 'Họ tên phải có ít nhất 2 ký tự.';
        }

        if (errorMsg) {
            this.showFieldError(field, errorMsg);
            return false;
        }

        this.clearFieldError(field);
        return true;
    },

    isValidEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    },

    showFieldError(field, msg) {
        field.classList.add('input-error');
        const errorEl = document.getElementById(field.id + '-error');
        if (errorEl) {
            errorEl.textContent = msg;
            errorEl.classList.add('visible');
        }
    },

    clearFieldError(field) {
        field.classList.remove('input-error');
        const errorEl = document.getElementById(field.id + '-error');
        if (errorEl) {
            errorEl.textContent = '';
            errorEl.classList.remove('visible');
        }
    },

    /**
     * Khởi tạo checkbox group vai trò - visual toggle
     */
    initCheckboxGroup() {
        document.querySelectorAll('.checkbox-item').forEach(item => {
            const cb = item.querySelector('input[type=checkbox]');
            if (!cb) return;

            // Set trạng thái ban đầu
            if (cb.checked) item.classList.add('checked');

            item.addEventListener('click', (e) => {
                if (e.target !== cb) {
                    cb.checked = !cb.checked;
                }
                item.classList.toggle('checked', cb.checked);

                // Xóa lỗi vai trò khi chọn ít nhất 1
                const roleError = document.getElementById('vaiTro-error');
                if (roleError && cb.checked) {
                    roleError.textContent = '';
                    roleError.classList.remove('visible');
                }
            });
        });
    }
};

// ============================================================
// DANH SÁCH NGƯỜI DÙNG - TÌM KIẾM & LỌC
// ============================================================

const NguoiDungList = {

    init() {
        // Tự động submit form filter khi đổi select
        ['filterVaiTro', 'filterTrangThai', 'filterNhom'].forEach(id => {
            const el = document.getElementById(id);
            if (el) {
                el.addEventListener('change', () => {
                    document.getElementById('filter-form')?.submit();
                });
            }
        });

        // Nút xóa bộ lọc
        const btnClear = document.getElementById('btn-clear-filter');
        if (btnClear) {
            btnClear.addEventListener('click', () => {
                const form = document.getElementById('filter-form');
                if (!form) return;
                form.querySelectorAll('input, select').forEach(f => {
                    f.value = '';
                });
                form.submit();
            });
        }

        // Thêm hiệu ứng loading cho nút submit form filter
        const filterForm = document.getElementById('filter-form');
        if (filterForm) {
            filterForm.addEventListener('submit', () => {
                const btn = filterForm.querySelector('[type=submit]');
                if (btn) btn.classList.add('btn-loading');
            });
        }
    }
};

// ============================================================
// KHỞI ĐỘNG
// ============================================================

document.addEventListener('DOMContentLoaded', () => {
    // Danh sách
    if (document.getElementById('filter-form')) {
        NguoiDungList.init();
    }

    // Form tạo tài khoản
    if (document.getElementById('form-tao-tai-khoan')) {
        NguoiDungForm.init('form-tao-tai-khoan');
    }

    // Form sửa tài khoản
    if (document.getElementById('form-sua-tai-khoan')) {
        NguoiDungForm.init('form-sua-tai-khoan');
    }

    // Tự đóng alert sau 5 giây
    setTimeout(() => {
        document.querySelectorAll('.alert').forEach(el => {
            el.style.transition = 'opacity 0.5s';
            el.style.opacity = '0';
            setTimeout(() => el.remove(), 500);
        });
    }, 5000);
});
