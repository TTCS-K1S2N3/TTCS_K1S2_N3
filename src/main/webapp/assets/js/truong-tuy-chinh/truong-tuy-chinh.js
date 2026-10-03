/**
 * JS Module: Trường Tuỳ Chỉnh (S2-08 FE)
 * Xử lý: form tạo/sửa trường, quản lý danh sách lựa chọn,
 *         hiển thị/ẩn panel theo kiểu dữ liệu, validation giao diện
 */

'use strict';

// ============================================================
// FORM QUẢN LÝ TRƯỜNG TUỲ CHỈNH
// ============================================================

const TruongTuyChinhForm = {

    init(formId) {
        const form = document.getElementById(formId);
        if (!form) return;

        // Lắng nghe thay đổi kiểu dữ liệu → show/hide panel lựa chọn
        const selectKieu = document.getElementById('kieuDuLieu');
        if (selectKieu) {
            selectKieu.addEventListener('change', () => {
                this.toggleOptionsPanel(selectKieu.value);
            });
            // Set trạng thái ban đầu
            this.toggleOptionsPanel(selectKieu.value);
        }

        // Nút thêm dòng lựa chọn
        const btnThem = document.getElementById('btn-them-lua-chon');
        if (btnThem) {
            btnThem.addEventListener('click', () => {
                this.themDongLuaChon();
            });
        }

        // Validation khi submit
        form.addEventListener('submit', (e) => {
            if (!this.validateForm(form)) {
                e.preventDefault();
            }
        });

        // Real-time clear error
        form.querySelectorAll('input, select, textarea').forEach(field => {
            field.addEventListener('input', () => this.clearError(field));
            field.addEventListener('change', () => this.clearError(field));
        });

        // Nút xóa dòng lựa chọn (đã có sẵn trên form sửa)
        this.initRemoveButtons();
    },

    /**
     * Hiển thị/ẩn panel danh sách lựa chọn theo kiểu dữ liệu
     */
    toggleOptionsPanel(kieuDuLieu) {
        const panel = document.getElementById('options-panel');
        if (!panel) return;

        if (kieuDuLieu === 'DANH_SACH_CHON') {
            panel.classList.add('visible');
            // Nếu chưa có dòng nào, thêm 2 dòng mặc định
            const rows = panel.querySelectorAll('.option-row');
            if (rows.length === 0) {
                this.themDongLuaChon();
                this.themDongLuaChon();
            }
        } else {
            panel.classList.remove('visible');
        }
    },

    /**
     * Thêm dòng nhập giá trị lựa chọn
     */
    themDongLuaChon() {
        const container = document.getElementById('options-container');
        if (!container) return;

        const soThuTu = container.querySelectorAll('.option-row').length + 1;
        const div = document.createElement('div');
        div.className = 'option-row';
        div.innerHTML = `
            <input type="text"
                   name="danhSachLuaChon[]"
                   placeholder="Lựa chọn ${soThuTu}"
                   maxlength="200">
            <button type="button" class="btn-remove-option" title="Xóa lựa chọn này">&#215;</button>
        `;
        container.appendChild(div);

        // Gán sự kiện xóa cho nút mới
        const btnXoa = div.querySelector('.btn-remove-option');
        btnXoa.addEventListener('click', () => {
            div.remove();
            this.capNhatSoThuTuLuaChon();
        });

        // Focus vào input mới
        div.querySelector('input').focus();
    },

    /**
     * Khởi tạo nút xóa cho các dòng lựa chọn đã có sẵn (form sửa)
     */
    initRemoveButtons() {
        document.querySelectorAll('.btn-remove-option').forEach(btn => {
            btn.addEventListener('click', () => {
                btn.closest('.option-row').remove();
                this.capNhatSoThuTuLuaChon();
            });
        });
    },

    capNhatSoThuTuLuaChon() {
        const container = document.getElementById('options-container');
        if (!container) return;
        container.querySelectorAll('.option-row input').forEach((inp, idx) => {
            inp.placeholder = `Lựa chọn ${idx + 1}`;
        });
    },

    /**
     * Validate form
     */
    validateForm(form) {
        let valid = true;

        // Nhãn hiển thị bắt buộc
        const nhanHien = document.getElementById('nhanHien');
        if (nhanHien && !nhanHien.value.trim()) {
            this.showError(nhanHien, 'Nhãn hiển thị không được để trống.');
            valid = false;
        }

        // Tên kỹ thuật bắt buộc và định dạng snake_case
        const tenTruong = document.getElementById('tenTruong');
        if (tenTruong) {
            const val = tenTruong.value.trim();
            if (!val) {
                this.showError(tenTruong, 'Tên kỹ thuật không được để trống.');
                valid = false;
            } else if (!/^[a-z][a-z0-9_]*$/.test(val)) {
                this.showError(tenTruong, 'Tên kỹ thuật chỉ gồm chữ thường, số và dấu _. Bắt đầu bằng chữ cái.');
                valid = false;
            }
        }

        // Kiểu dữ liệu bắt buộc
        const kieuDuLieu = document.getElementById('kieuDuLieu');
        if (kieuDuLieu && !kieuDuLieu.value) {
            this.showError(kieuDuLieu, 'Vui lòng chọn kiểu dữ liệu.');
            valid = false;
        }

        // Kiểm tra danh sách lựa chọn khi kiểu là DANH_SACH_CHON
        if (kieuDuLieu && kieuDuLieu.value === 'DANH_SACH_CHON') {
            const inputs = document.querySelectorAll('input[name="danhSachLuaChon[]"]');
            const filled = Array.from(inputs).filter(i => i.value.trim());
            if (filled.length === 0) {
                const errEl = document.getElementById('options-error');
                if (errEl) {
                    errEl.textContent = 'Phải nhập ít nhất một lựa chọn.';
                    errEl.classList.add('visible');
                }
                valid = false;
            }
        }

        return valid;
    },

    showError(field, msg) {
        field.classList.add('input-error');
        const errEl = document.getElementById(field.id + '-error');
        if (errEl) {
            errEl.textContent = msg;
            errEl.classList.add('visible');
        }
    },

    clearError(field) {
        field.classList.remove('input-error');
        const errEl = document.getElementById(field.id + '-error');
        if (errEl) {
            errEl.textContent = '';
            errEl.classList.remove('visible');
        }
    }
};

// ============================================================
// DANH SÁCH TRƯỜNG TUỲ CHỈNH
// ============================================================

const TruongTuyChinhList = {

    init() {
        // Tabs đối tượng (KHACH_HANG / CO_HOI)
        document.querySelectorAll('.tab-btn[data-doi-tuong]').forEach(btn => {
            btn.addEventListener('click', () => {
                const doiTuong = btn.dataset.doiTuong;
                this.chuyenTab(doiTuong);
            });
        });

        // Auto-đóng alert sau 5 giây
        setTimeout(() => {
            document.querySelectorAll('.alert').forEach(el => {
                el.style.transition = 'opacity 0.5s';
                el.style.opacity = '0';
                setTimeout(() => el.remove(), 500);
            });
        }, 5000);
    },

    chuyenTab(doiTuong) {
        // Cập nhật tab active
        document.querySelectorAll('.tab-btn[data-doi-tuong]').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.doiTuong === doiTuong);
        });

        // Cập nhật nút "Thêm trường mới"
        const btnThem = document.getElementById('btn-them-truong');
        if (btnThem) {
            const base = btnThem.dataset.baseUrl || btnThem.href.split('?')[0];
            btnThem.dataset.baseUrl = base;
            btnThem.href = base + '?doiTuong=' + doiTuong;
        }

        // Hiện/ẩn bảng tương ứng
        document.querySelectorAll('.table-wrapper[data-doi-tuong]').forEach(el => {
            el.style.display = el.dataset.doiTuong === doiTuong ? '' : 'none';
        });
    }
};

// ============================================================
// TỰ ĐỘNG SINH tenTruong từ nhanHien (snake_case)
// ============================================================

function autoGenTenTruong() {
    const nhanHienEl = document.getElementById('nhanHien');
    const tenTruongEl = document.getElementById('tenTruong');
    if (!nhanHienEl || !tenTruongEl) return;

    // Chỉ auto-gen khi trường tenTruong còn trống hoặc đang auto-gen
    let isAutoGen = true;

    nhanHienEl.addEventListener('input', () => {
        if (!isAutoGen) return;
        const slug = nhanHienEl.value
            .toLowerCase()
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .replace(/đ/g, 'd').replace(/Đ/g, 'd')
            .replace(/[^a-z0-9\s]/g, '')
            .trim()
            .replace(/\s+/g, '_');
        tenTruongEl.value = slug;
        TruongTuyChinhForm.clearError(tenTruongEl);
    });

    tenTruongEl.addEventListener('input', () => {
        // Nếu người dùng tự sửa thì không auto-gen nữa
        isAutoGen = false;
    });

    // Nếu đang sửa (đã có giá trị), không auto-gen
    if (tenTruongEl.value.trim()) {
        isAutoGen = false;
    }
}

// ============================================================
// KHỞI ĐỘNG
// ============================================================

document.addEventListener('DOMContentLoaded', () => {
    // Danh sách
    if (document.querySelector('.tab-btn[data-doi-tuong]')) {
        TruongTuyChinhList.init();
        // Kích hoạt tab đang active
        const activeTab = document.querySelector('.tab-btn.active[data-doi-tuong]');
        if (activeTab) {
            TruongTuyChinhList.chuyenTab(activeTab.dataset.doiTuong);
        }
    }

    // Form tạo
    if (document.getElementById('form-tao-truong')) {
        TruongTuyChinhForm.init('form-tao-truong');
        autoGenTenTruong();
    }

    // Form sửa
    if (document.getElementById('form-sua-truong')) {
        TruongTuyChinhForm.init('form-sua-truong');
        // Khởi tạo trạng thái panel lựa chọn theo kiểu hiện tại
        const kieuEl = document.getElementById('kieuDuLieu');
        if (kieuEl) TruongTuyChinhForm.toggleOptionsPanel(kieuEl.value);
    }

    // Auto-đóng alert
    setTimeout(() => {
        document.querySelectorAll('.alert').forEach(el => {
            el.style.transition = 'opacity 0.5s';
            el.style.opacity = '0';
            setTimeout(() => el.remove(), 500);
        });
    }, 5000);
});
