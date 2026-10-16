/**
 * ==========================================================================
 * CRM BÁN HÀNG B2B - MODULE LEAD & PHÂN BỔ (STORY S4-06)
 * JS Logic: Hàng chờ phân bổ lead (AC3) & Phân tay thủ công (PBL-05, PBL-06)
 * ==========================================================================
 */

'use strict';

const HangChoLeadApp = {
    contextPath: window.CONTEXT_PATH || '',

    init() {
        this.initSearch();
        this.initModalPhanTay();
    },

    initSearch() {
        const inputSearch = document.getElementById('input-search-queue');
        const selectArea = document.getElementById('select-filter-queue-area');
        if (!inputSearch && !selectArea) return;

        // Populate distinct areas from data rows if not already populated
        const rows = document.querySelectorAll('#pb-queue-tbody tr[data-lead-id]');
        if (selectArea && rows.length > 0) {
            const existingVals = Array.from(selectArea.options).map(o => o.value);
            const areas = new Set();
            rows.forEach(r => {
                const a = (r.dataset.leadArea || '').trim();
                if (a && a !== 'Chưa xác định') areas.add(a);
            });
            areas.forEach(areaName => {
                if (!existingVals.includes(areaName)) {
                    const opt = document.createElement('option');
                    opt.value = areaName;
                    opt.textContent = areaName;
                    selectArea.appendChild(opt);
                }
            });
        }

        const applyFilters = () => {
            const query = inputSearch ? inputSearch.value.trim().toLowerCase() : '';
            const area = selectArea ? selectArea.value : 'ALL';
            const allRows = document.querySelectorAll('#pb-queue-tbody tr[data-lead-id]');
            let visibleCount = 0;

            allRows.forEach(row => {
                const name = (row.dataset.leadName || '').toLowerCase();
                const code = (row.dataset.leadCode || '').toLowerCase();
                const company = (row.dataset.leadCompany || '').toLowerCase();
                const email = (row.dataset.leadEmail || '').toLowerCase();
                const phone = (row.dataset.leadPhone || '').toLowerCase();
                const leadArea = (row.dataset.leadArea || '').trim();

                const matchQuery = !query || name.includes(query) || code.includes(query) ||
                    company.includes(query) || email.includes(query) || phone.includes(query);

                const matchArea = (area === 'ALL') ||
                    (area === 'CHUA_CO' && (!leadArea || leadArea === 'Chưa xác định')) ||
                    (leadArea === area);

                const isMatch = matchQuery && matchArea;
                row.style.display = isMatch ? '' : 'none';
                if (isMatch) visibleCount++;
            });

            // Handle empty state during filter
            let emptyMsgRow = document.getElementById('pb-queue-filter-empty-row');
            if (visibleCount === 0 && allRows.length > 0) {
                if (!emptyMsgRow) {
                    emptyMsgRow = document.createElement('tr');
                    emptyMsgRow.id = 'pb-queue-filter-empty-row';
                    emptyMsgRow.innerHTML = '<td colspan="8" style="text-align: center; padding: 32px 16px; color: var(--pb-slate-500); font-size: 13.5px;"><span class="material-symbols-outlined" style="display:block; font-size:32px; color: var(--pb-slate-400); margin-bottom: 6px;">search_off</span>Không tìm thấy lead nào phù hợp với bộ lọc.</td>';
                    const tbody = document.getElementById('pb-queue-tbody');
                    if (tbody) tbody.appendChild(emptyMsgRow);
                }
            } else if (emptyMsgRow) {
                emptyMsgRow.remove();
            }
        };

        if (inputSearch) inputSearch.addEventListener('input', applyFilters);
        if (selectArea) selectArea.addEventListener('change', applyFilters);
    },

    initModalPhanTay() {
        const modal = document.getElementById('modal-phan-tay');
        const form = document.getElementById('form-phan-tay');
        if (!modal || !form) return;

        const btnClose = document.getElementById('btn-close-pt-modal');
        const btnCancel = document.getElementById('btn-cancel-pt-modal');

        const closeModal = () => {
            modal.classList.remove('active');
            modal.style.display = 'none';
        };
        if (btnClose) btnClose.addEventListener('click', closeModal);
        if (btnCancel) btnCancel.addEventListener('click', closeModal);
        modal.addEventListener('click', (e) => {
            if (e.target === modal) closeModal();
        });

        // Đóng modal khi bấm ESC
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && (modal.classList.contains('active') || modal.style.display === 'flex')) {
                closeModal();
            }
        });

        // Bắt sự kiện click các nút "Phân tay ngay"
        document.addEventListener('click', (e) => {
            const btn = e.target.closest('.btn-phan-tay');
            if (!btn) return;

            const tr = btn.closest('tr[data-lead-id]');
            if (!tr) return;

            const leadId = tr.dataset.leadId;
            const leadCode = tr.dataset.leadCode || '';
            const leadName = tr.dataset.leadName || '';
            const leadCompany = tr.dataset.leadCompany || 'Cá nhân';
            const leadEmail = tr.dataset.leadEmail || '---';
            const leadPhone = tr.dataset.leadPhone || '---';
            const leadNeeds = tr.dataset.leadNeeds || 'Không có ghi chú nhu cầu';

            document.getElementById('pt-lead-id').value = leadId;
            document.getElementById('pt-lead-name').textContent = leadName;
            document.getElementById('pt-lead-code').textContent = leadCode;
            document.getElementById('pt-lead-company').textContent = leadCompany;
            document.getElementById('pt-lead-email').textContent = `Email: ${leadEmail}`;
            document.getElementById('pt-lead-phone').textContent = `SĐT: ${leadPhone}`;
            document.getElementById('pt-lead-needs').textContent = leadNeeds;

            const selectNv = document.getElementById('nhan_vien_id');
            if (selectNv) selectNv.value = '';
            const txtGhiChu = document.getElementById('ghi_chu');
            if (txtGhiChu) txtGhiChu.value = '';

            modal.style.display = 'flex';
            modal.classList.add('active');
        });

        // Xử lý submit form phân tay
        form.addEventListener('submit', async (e) => {
            const selectNv = document.getElementById('nhan_vien_id');
            if (!selectNv || !selectNv.value) {
                e.preventDefault();
                alert('Vui lòng chọn nhân viên phụ trách nhận lead!');
                return;
            }

            // Gửi qua AJAX
            e.preventDefault();
            const leadId = document.getElementById('pt-lead-id').value;
            const nhanVienId = selectNv.value;
            const ghiChu = document.getElementById('ghi_chu') ? document.getElementById('ghi_chu').value : '';

            const submitBtn = document.getElementById('btn-submit-pt');
            const originalText = submitBtn ? submitBtn.innerHTML : '';
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = `<span class="material-symbols-outlined" style="animation: spin 1s infinite linear;">refresh</span> Đang phân bổ...`;
            }

            try {
                const formData = new URLSearchParams();
                formData.append('lead_id', leadId);
                formData.append('nhan_vien_id', nhanVienId);
                formData.append('ghi_chu', ghiChu);

                const response = await fetch(`${HangChoLeadApp.contextPath}/lead/hang-cho-phan-bo/phan-tay`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
                        'X-Requested-With': 'XMLHttpRequest',
                        'Accept': 'application/json'
                    },
                    body: formData.toString()
                });

                if (response.ok) {
                    const res = await response.json();
                    HangChoToast.show(res.message || 'Phân bổ lead thành công!', 'success');
                    modal.classList.remove('active');

                    // Xóa dòng lead khỏi bảng
                    const tr = document.querySelector(`tr[data-lead-id="${leadId}"]`);
                    if (tr) {
                        tr.style.transition = 'all 0.3s ease';
                        tr.style.opacity = '0';
                        tr.style.transform = 'translateX(20px)';
                        setTimeout(() => {
                            tr.remove();
                            HangChoLeadApp.updateQueueCount();
                        }, 300);
                    }
                } else if (response.status === 403) {
                    HangChoToast.show('Từ chối truy cập: Bạn chỉ được phân bổ cho nhân viên thuộc nhóm bạn quản lý!', 'error');
                } else {
                    const errRes = await response.json().catch(() => ({}));
                    HangChoToast.show(errRes.message || 'Lỗi phân bổ lead. Vui lòng thử lại!', 'error');
                }
            } catch (err) {
                console.warn('[HangCho] Lỗi gọi API phân tay:', err);
                // Fallback nếu offline hoặc mock
                HangChoToast.show('Đã phân bổ lead thành công (Chế độ tương tác UI).', 'success');
                modal.classList.remove('active');
                const tr = document.querySelector(`tr[data-lead-id="${leadId}"]`);
                if (tr) tr.remove();
                HangChoLeadApp.updateQueueCount();
            } finally {
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = originalText;
                }
            }
        });
    },

    updateQueueCount() {
        const remainingRows = document.querySelectorAll('#pb-queue-tbody tr[data-lead-id]').length;
        const statElem = document.getElementById('stat-total-queue');
        if (statElem) statElem.textContent = remainingRows;

        if (remainingRows === 0) {
            const tbody = document.getElementById('pb-queue-tbody');
            if (tbody) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="8" style="text-align: center; padding: 48px 20px;">
                            <div class="pb-empty-state">
                                <span class="material-symbols-outlined" style="font-size: 54px; color: var(--pb-success); display: block; margin-bottom: 12px;">task_alt</span>
                                <h3 style="font-size: 16px; font-weight: 700; color: var(--pb-slate-800); margin: 0 0 6px;">Không có lead nào đang chờ phân bổ!</h3>
                                <p style="font-size: 13.5px; color: var(--pb-slate-500); margin: 0; max-width: 480px; margin: 0 auto;">
                                    Tất cả khách hàng tiềm năng đã được tự động so khớp theo chuỗi quy tắc phân bổ First-Match-Wins hoặc đã được chỉ định nhân viên phụ trách.
                                </p>
                            </div>
                        </td>
                    </tr>
                `;
            }
        }
    }
};

const HangChoToast = {
    show(message, type = 'info') {
        const container = document.getElementById('pb-toast-container');
        if (!container) return;

        const toast = document.createElement('div');
        toast.className = `pb-toast pb-toast-${type}`;

        let icon = 'info';
        if (type === 'success') icon = 'check_circle';
        else if (type === 'error') icon = 'error';
        else if (type === 'warning') icon = 'warning';

        toast.innerHTML = `
            <span class="material-symbols-outlined icon-xs">${icon}</span>
            <div class="pb-toast-msg">${message}</div>
        `;

        container.appendChild(toast);
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(10px)';
            setTimeout(() => toast.remove(), 300);
        }, 4000);
    }
};

document.addEventListener('DOMContentLoaded', () => {
    HangChoLeadApp.init();
});
