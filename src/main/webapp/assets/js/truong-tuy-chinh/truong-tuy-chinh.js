/**
 * ==========================================================================
 * JS Module: Quản lý Trường Tuỳ Chỉnh (Story S2-08 FE)
 * Tương tác form, Live Field Preview, quản lý dropdown options động,
 * tab switcher và trực quan hoá AC3 (Biểu mẫu, Bộ lọc, Bản xuất Excel).
 * ==========================================================================
 */

'use strict';

// ============================================================
// 1. FORM QUẢN LÝ TRƯỜNG TUỲ CHỈNH & LIVE PREVIEW
// ============================================================

const TruongTuyChinhForm = {

    init(formId) {
        const form = document.getElementById(formId);
        if (!form) return;

        const selectKieu = document.getElementById('kieuDuLieu');
        const inputNhan = document.getElementById('nhanHien');
        const checkBatBuoc = document.getElementById('batBuoc');

        // Lắng nghe thay đổi kiểu dữ liệu → show/hide panel lựa chọn & cập nhật live preview
        if (selectKieu) {
            selectKieu.addEventListener('change', () => {
                this.toggleOptionsPanel(selectKieu.value);
                this.capNhatLivePreview();
            });
            this.toggleOptionsPanel(selectKieu.value);
        }

        // Lắng nghe thay đổi nhãn hiển thị & bắt buộc → cập nhật live preview
        if (inputNhan) {
            inputNhan.addEventListener('input', () => this.capNhatLivePreview());
        }
        if (checkBatBuoc) {
            checkBatBuoc.addEventListener('change', () => this.capNhatLivePreview());
        }

        // Nút thêm dòng lựa chọn
        const btnThem = document.getElementById('btn-them-lua-chon');
        if (btnThem) {
            btnThem.addEventListener('click', () => {
                this.themDongLuaChon();
                this.capNhatLivePreview();
            });
        }

        // Khởi tạo các nút xóa lựa chọn đã có sẵn
        this.initRemoveButtons();

        // Validation khi submit
        form.addEventListener('submit', (e) => {
            if (!this.validateForm(form)) {
                e.preventDefault();
            }
        });

        // Real-time clear error khi người dùng gõ lại
        form.querySelectorAll('input, select, textarea').forEach(field => {
            field.addEventListener('input', () => this.clearError(field));
            field.addEventListener('change', () => this.clearError(field));
        });

        // Cập nhật live preview ban đầu
        this.capNhatLivePreview();
    },

    /**
     * Hiển thị hoặc ẩn panel khai báo danh sách lựa chọn (chỉ dành cho kiểu DANH_SACH_CHON)
     */
    toggleOptionsPanel(kieuDuLieu) {
        const panel = document.getElementById('options-panel');
        if (!panel) return;

        if (kieuDuLieu === 'DANH_SACH_CHON') {
            panel.classList.add('visible');
            const rows = panel.querySelectorAll('.option-row');
            if (rows.length === 0) {
                this.themDongLuaChon('Lựa chọn 1');
                this.themDongLuaChon('Lựa chọn 2');
            }
        } else {
            panel.classList.remove('visible');
        }
    },

    /**
     * Thêm một dòng lựa chọn mới vào options-container
     */
    themDongLuaChon(defaultVal) {
        const container = document.getElementById('options-container');
        if (!container) return;

        const soThuTu = container.querySelectorAll('.option-row').length + 1;
        const div = document.createElement('div');
        div.className = 'option-row';
        div.innerHTML = `
            <input type="text"
                   name="danhSachLuaChon[]"
                   placeholder="Nhập giá trị lựa chọn ${soThuTu}..."
                   maxlength="200"
                   value="${defaultVal ? defaultVal : ''}">
            <button type="button" class="btn-remove-option" title="Xóa lựa chọn này">&times;</button>
        `;
        container.appendChild(div);

        const inp = div.querySelector('input');
        const btnXoa = div.querySelector('.btn-remove-option');

        btnXoa.addEventListener('click', () => {
            div.remove();
            this.capNhatSoThuTuLuaChon();
            this.capNhatLivePreview();
        });

        inp.addEventListener('input', () => {
            this.capNhatLivePreview();
        });

        if (!defaultVal) {
            inp.focus();
        }
    },

    initRemoveButtons() {
        document.querySelectorAll('.btn-remove-option').forEach(btn => {
            btn.addEventListener('click', () => {
                btn.closest('.option-row').remove();
                this.capNhatSoThuTuLuaChon();
                this.capNhatLivePreview();
            });
        });

        document.querySelectorAll('input[name="danhSachLuaChon[]"]').forEach(inp => {
            inp.addEventListener('input', () => this.capNhatLivePreview());
        });
    },

    capNhatSoThuTuLuaChon() {
        const container = document.getElementById('options-container');
        if (!container) return;
        container.querySelectorAll('.option-row input').forEach((inp, idx) => {
            inp.placeholder = `Nhập giá trị lựa chọn ${idx + 1}...`;
        });
    },

    /**
     * Cập nhật widget Xem trước thời gian thực (Live Field Preview)
     */
    capNhatLivePreview() {
        const inputNhan = document.getElementById('nhanHien');
        const selectKieu = document.getElementById('kieuDuLieu');
        const checkBatBuoc = document.getElementById('batBuoc');

        const previewLabelText = document.getElementById('previewLabelText');
        const previewRequiredStar = document.getElementById('previewRequiredStar');
        const previewInputArea = document.getElementById('previewInputArea');
        const previewHelpText = document.getElementById('previewHelpText');

        if (!previewLabelText || !previewInputArea) return;

        // 1. Cập nhật nhãn
        const nhanVal = inputNhan && inputNhan.value.trim() ? inputNhan.value.trim() : 'Tên trường tuỳ chỉnh';
        previewLabelText.textContent = nhanVal;

        // 2. Cập nhật dấu bắt buộc
        if (previewRequiredStar) {
            previewRequiredStar.style.display = (checkBatBuoc && checkBatBuoc.checked) ? 'inline' : 'none';
        }

        // 3. Cập nhật ô nhập tương ứng kiểu dữ liệu
        const kieu = selectKieu ? selectKieu.value : 'VAN_BAN';
        let inputHtml = '';
        let helpText = 'Trường tuỳ chỉnh trên biểu mẫu CRM';

        if (kieu === 'SO') {
            inputHtml = '<input type="number" placeholder="Nhập số..." disabled style="background:#fff; border: 1.5px solid #cbd5e1; border-radius: 8px; padding: 10px 14px; width: 100%;">';
            helpText = 'Kiểu số: Chỉ cho phép nhập số nguyên hoặc thập phân.';
        } else if (kieu === 'NGAY') {
            inputHtml = '<input type="date" disabled style="background:#fff; border: 1.5px solid #cbd5e1; border-radius: 8px; padding: 10px 14px; width: 100%;">';
            helpText = 'Kiểu ngày: Tích hợp lịch chọn ngày tháng chuẩn ISO (YYYY-MM-DD).';
        } else if (kieu === 'DANH_SACH_CHON') {
            const inputs = document.querySelectorAll('input[name="danhSachLuaChon[]"]');
            let optionsHtml = '<option value="">-- Vui lòng chọn ' + nhanVal + ' --</option>';
            let count = 0;
            inputs.forEach(inp => {
                if (inp.value.trim()) {
                    optionsHtml += `<option>${inp.value.trim()}</option>`;
                    count++;
                }
            });
            if (count === 0) {
                optionsHtml += '<option>Lựa chọn mẫu 1</option><option>Lựa chọn mẫu 2</option>';
            }
            inputHtml = `<select disabled style="background:#fff; border: 1.5px solid #cbd5e1; border-radius: 8px; padding: 10px 14px; width: 100%;">${optionsHtml}</select>`;
            helpText = 'Kiểu danh sách chọn: Người dùng chọn 1 giá trị từ menu thả xuống.';
        } else {
            // VAN_BAN
            inputHtml = '<input type="text" placeholder="Nhập văn bản..." disabled style="background:#fff; border: 1.5px solid #cbd5e1; border-radius: 8px; padding: 10px 14px; width: 100%;">';
            helpText = 'Kiểu văn bản: Dùng cho ghi chú, mô tả ngắn, mã phân loại tự do.';
        }

        previewInputArea.innerHTML = inputHtml;
        if (previewHelpText) {
            previewHelpText.textContent = helpText;
        }
    },

    /**
     * Validate toàn bộ form phía client trước khi gửi lên server
     */
    validateForm(form) {
        let valid = true;

        // 1. Nhãn hiển thị
        const nhanHien = document.getElementById('nhanHien');
        if (nhanHien && !nhanHien.value.trim()) {
            this.showError(nhanHien, 'Nhãn hiển thị không được để trống.');
            valid = false;
        } else if (nhanHien && nhanHien.value.trim().length > 150) {
            this.showError(nhanHien, 'Nhãn hiển thị tối đa 150 ký tự.');
            valid = false;
        }

        // 2. Tên kỹ thuật
        const tenTruong = document.getElementById('tenTruong');
        if (tenTruong && !tenTruong.readOnly) {
            const val = tenTruong.value.trim();
            if (!val) {
                this.showError(tenTruong, 'Tên kỹ thuật không được để trống.');
                valid = false;
            } else if (!/^[a-z][a-z0-9_]*$/.test(val)) {
                this.showError(tenTruong, 'Tên kỹ thuật chỉ gồm chữ thường, số và dấu gạch dưới (_). Bắt đầu bằng chữ cái.');
                valid = false;
            } else if (val.length > 80) {
                this.showError(tenTruong, 'Tên kỹ thuật tối đa 80 ký tự.');
                valid = false;
            }
        }

        // 3. Kiểu dữ liệu
        const kieuDuLieu = document.getElementById('kieuDuLieu');
        if (kieuDuLieu && !kieuDuLieu.value) {
            this.showError(kieuDuLieu, 'Vui lòng chọn kiểu dữ liệu cho trường.');
            valid = false;
        }

        // 4. Nếu kiểu là DANH_SACH_CHON -> bắt buộc ít nhất 1 lựa chọn
        if (kieuDuLieu && kieuDuLieu.value === 'DANH_SACH_CHON') {
            const inputs = document.querySelectorAll('input[name="danhSachLuaChon[]"]');
            const filled = Array.from(inputs).filter(i => i.value.trim());
            const errEl = document.getElementById('options-error');
            if (filled.length === 0) {
                if (errEl) {
                    errEl.textContent = 'Danh sách chọn phải có ít nhất một giá trị lựa chọn.';
                    errEl.classList.add('visible');
                }
                valid = false;
            } else if (errEl) {
                errEl.textContent = '';
                errEl.classList.remove('visible');
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
// 2. DANH SÁCH & TAB SWITCHER & PREVIEW CONTROLLER
// ============================================================

const TruongTuyChinhList = {

    init() {
        document.querySelectorAll('.tab-btn[data-doi-tuong]').forEach(btn => {
            btn.addEventListener('click', () => {
                const doiTuong = btn.dataset.doiTuong;
                this.chuyenTab(doiTuong);
            });
        });
    },

    chuyenTab(doiTuong) {
        // Cập nhật tab active
        document.querySelectorAll('.tab-btn[data-doi-tuong]').forEach(btn => {
            const isActive = btn.dataset.doiTuong === doiTuong;
            btn.classList.toggle('active', isActive);
            btn.setAttribute('aria-selected', isActive);
        });

        // Cập nhật URL các nút thêm trường & xuất excel
        const btnThem = document.getElementById('btn-them-truong');
        const targetDoiTuong = (doiTuong === 'PREVIEW_AC3') ? 'KHACH_HANG' : doiTuong;
        if (btnThem) {
            const base = btnThem.dataset.baseUrl || btnThem.href.split('?')[0];
            btnThem.dataset.baseUrl = base;
            btnThem.href = base + '?doiTuong=' + targetDoiTuong;
        }

        const btnExcel = document.getElementById('btn-xuat-excel-top');
        if (btnExcel) {
            const base = btnExcel.href.split('?')[0];
            btnExcel.href = base + '?doiTuong=' + targetDoiTuong;
        }

        // Hiện/ẩn wrapper tương ứng
        document.querySelectorAll('.table-wrapper[data-doi-tuong]').forEach(el => {
            el.style.display = (el.dataset.doiTuong === doiTuong) ? '' : 'none';
        });
    }
};

/**
 * Điều khiển chuyển chế độ xem trước trong tab PREVIEW_AC3:
 * mode: 'FORM' | 'FILTER' | 'EXCEL'
 */
function chuyenCheDoPreview(mode, btn) {
    if (!btn) return;
    document.querySelectorAll('.preview-tab-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');

    const secForm = document.getElementById('preview-section-form');
    const secFilter = document.getElementById('preview-section-filter');
    const secExcel = document.getElementById('preview-section-excel');

    if (secForm) secForm.style.display = (mode === 'FORM') ? 'block' : 'none';
    if (secFilter) secFilter.style.display = (mode === 'FILTER') ? 'block' : 'none';
    if (secExcel) secExcel.style.display = (mode === 'EXCEL') ? 'block' : 'none';
}

// ============================================================
// 3. TỰ ĐỘNG TẠO SLUG tenTruong TỪ nhanHien (snake_case)
// ============================================================

function autoGenTenTruong() {
    const nhanHienEl = document.getElementById('nhanHien');
    const tenTruongEl = document.getElementById('tenTruong');
    if (!nhanHienEl || !tenTruongEl) return;

    let isAutoGen = !tenTruongEl.value.trim();

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
        isAutoGen = false;
    });
}

// ============================================================
// 4. KHỞI TẠO DOM
// ============================================================

document.addEventListener('DOMContentLoaded', () => {
    // 1. Tự động ẩn flash message sau 5s
    setTimeout(() => {
        document.querySelectorAll('.alert').forEach(el => {
            el.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
            el.style.opacity = '0';
            el.style.transform = 'translateY(-6px)';
            setTimeout(() => el.remove(), 500);
        });
    }, 5000);

    // 2. Khởi tạo danh sách trường
    if (document.querySelector('.tab-btn[data-doi-tuong]')) {
        TruongTuyChinhList.init();
        const activeTab = document.querySelector('.tab-btn.active[data-doi-tuong]');
        if (activeTab) {
            TruongTuyChinhList.chuyenTab(activeTab.dataset.doiTuong);
        }
    }

    // 3. Khởi tạo form tạo
    if (document.getElementById('form-tao-truong')) {
        TruongTuyChinhForm.init('form-tao-truong');
        autoGenTenTruong();
    }

    // 4. Khởi tạo form sửa
    if (document.getElementById('form-sua-truong')) {
        TruongTuyChinhForm.init('form-sua-truong');
        const kieuEl = document.getElementById('kieuDuLieu');
        if (kieuEl) TruongTuyChinhForm.toggleOptionsPanel(kieuEl.value);
    }
});
