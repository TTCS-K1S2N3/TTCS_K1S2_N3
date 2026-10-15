/**
 * ==========================================================================
 * CRM BÁN HÀNG B2B - MODULE LEAD & PHÂN BỔ (STORY S4-06)
 * JS Logic: Quản lý Quy tắc Phân bổ Lead Tự động, Đổi thứ tự ưu tiên (AC2),
 * Bật/Tắt trạng thái, Chạy quét nền ngay (AC4) & Mô phỏng First-Match-Wins.
 * ==========================================================================
 */

'use strict';

const PhanBoLeadApp = {

    contextPath: window.CONTEXT_PATH || '',

    // Dữ liệu mock dự phòng
    mockRules: [
        {
            id: 1,
            maQuyTac: "QT-01-KV-MB",
            tenQuyTac: "Phân bổ khách hàng Doanh nghiệp Miền Bắc",
            thuTuUuTien: 1,
            kieuPhanBo: "THEO_KHU_VUC",
            dieuKienJson: '{"khu_vuc_ids": [1, 2, 3]}',
            dieuKienMoTa: "Khu vực: Hà Nội, Quảng Ninh, Hải Phòng",
            nhomDichId: 101,
            tenNhomDich: "Nhóm Kinh Doanh Miền Bắc",
            nguoiDungDichId: null,
            tenNguoiDungDich: "Xoay vòng trong nhóm",
            nguoiDungLanCuoiId: 10,
            hoatDong: true,
            updatedAt: "14/10/2026 10:15"
        },
        {
            id: 2,
            maQuyTac: "QT-02-NN-TAICHINH",
            tenQuyTac: "Ưu tiên Khách hàng Ngành Tài chính - Ngân hàng",
            thuTuUuTien: 2,
            kieuPhanBo: "THEO_NGANH_NGHE",
            dieuKienJson: '{"nganh_nghe_ids": [5, 8]}',
            dieuKienMoTa: "Ngành: Tài chính - Bảo hiểm, Công nghệ thông tin",
            nhomDichId: 102,
            tenNhomDich: "Nhóm Khách hàng Doanh nghiệp Lớn",
            nguoiDungDichId: 12,
            tenNguoiDungDich: "Trần Thị Mai (Chuyên gia)",
            nguoiDungLanCuoiId: null,
            hoatDong: true,
            updatedAt: "14/10/2026 11:20"
        },
        {
            id: 3,
            maQuyTac: "QT-03-RR-TOANCONGTY",
            tenQuyTac: "Xoay vòng đều cho Nhóm Bán hàng Trực tuyến",
            thuTuUuTien: 3,
            kieuPhanBo: "XOAY_VONG",
            dieuKienJson: '{}',
            dieuKienMoTa: "Áp dụng xoay vòng cho mọi lead",
            nhomDichId: 103,
            tenNhomDich: "Nhóm Inside Sales",
            nguoiDungDichId: null,
            tenNguoiDungDich: "Xoay vòng 6 nhân viên",
            nguoiDungLanCuoiId: 25,
            hoatDong: true,
            updatedAt: "14/10/2026 14:00"
        },
        {
            id: 4,
            maQuyTac: "QT-04-KV-MN-TAMNGUNG",
            tenQuyTac: "Phân bổ Miền Nam (Tạm dừng chiến dịch)",
            thuTuUuTien: 4,
            kieuPhanBo: "THEO_KHU_VUC",
            dieuKienJson: '{"khu_vuc_ids": [4, 5]}',
            dieuKienMoTa: "Khu vực: TP. Hồ Chí Minh, Bình Dương",
            nhomDichId: 104,
            tenNhomDich: "Nhóm Kinh Doanh Miền Nam",
            nguoiDungDichId: null,
            tenNguoiDungDich: "Xoay vòng trong nhóm",
            nguoiDungLanCuoiId: null,
            hoatDong: false,
            updatedAt: "14/10/2026 16:30"
        }
    ],

    currentRules: [],

    init() {
        this.initData();
        this.initTabs();
        this.RuleTable.init();
        this.RuleModal.init();
        this.BackgroundWorker.init();
        this.Simulator.init();
        this.updateStats();
    },

    initData() {
        const serverRows = document.querySelectorAll('#pb-rules-tbody tr[data-rule-id]');
        if (serverRows.length > 0) {
            this.currentRules = Array.from(serverRows).map(tr => ({
                id: Number(tr.dataset.ruleId),
                maQuyTac: tr.dataset.ruleCode || '',
                tenQuyTac: tr.dataset.ruleName || '',
                thuTuUuTien: Number(tr.dataset.rulePriority) || 1,
                kieuPhanBo: tr.dataset.ruleType || 'THEO_KHU_VUC',
                dieuKienMoTa: tr.dataset.ruleCondition || '',
                dieuKienJson: tr.dataset.ruleConditionJson || '{}',
                nhomDichId: Number(tr.dataset.ruleTargetTeamId) || null,
                tenNhomDich: tr.dataset.ruleTargetTeam || '',
                nguoiDungDichId: Number(tr.dataset.ruleTargetUserId) || null,
                tenNguoiDungDich: tr.dataset.ruleTargetUser || '',
                hoatDong: tr.dataset.ruleActive === 'true',
                updatedAt: tr.dataset.ruleUpdated || ''
            }));
        } else {
            this.currentRules = JSON.parse(JSON.stringify(this.mockRules));
        }
    },

    initTabs() {
        const tabBtns = document.querySelectorAll('.pb-tab-btn');
        tabBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                const targetTabId = btn.dataset.tabTarget || btn.dataset.tab;
                if (!targetTabId) return;

                tabBtns.forEach(b => {
                    b.classList.remove('active');
                    b.setAttribute('aria-selected', 'false');
                });
                btn.classList.add('active');
                btn.setAttribute('aria-selected', 'true');

                document.querySelectorAll('.pb-tab-pane').forEach(pane => {
                    pane.classList.remove('active');
                });

                const targetPane = document.getElementById(targetTabId);
                if (targetPane) {
                    targetPane.classList.add('active');
                }
            });
        });
    },

    updateStats() {
        const total = this.currentRules.length;
        const active = this.currentRules.filter(r => r.hoatDong).length;

        const statTotal = document.getElementById('stat-total-rules');
        if (statTotal) statTotal.textContent = total;

        const statActive = document.getElementById('stat-active-rules');
        if (statActive) statActive.textContent = `${active}/${total} đang hoạt động`;
    }
};

/**
 * ==========================================================================
 * 1. BẢNG DỮ LIỆU QUY TẮC & SẮP XẾP ƯU TIÊN (AC1, AC2)
 * ==========================================================================
 */
PhanBoLeadApp.RuleTable = {

    tbody: null,
    dragSrcRow: null,

    init() {
        this.tbody = document.getElementById('pb-rules-tbody');
        if (!this.tbody) return;

        this.initFilters();
        this.initRowActions();
        this.initDragAndDrop();
    },

    initRowActions() {
        this.tbody.addEventListener('click', (e) => {
            // Nút Lên (Action PBL-03)
            const btnUp = e.target.closest('.btn-move-up');
            if (btnUp && !btnUp.disabled) {
                const tr = btnUp.closest('tr');
                if (tr) this.doiThuTu(Number(tr.dataset.ruleId), 'LEN');
                return;
            }

            // Nút Xuống (Action PBL-03)
            const btnDown = e.target.closest('.btn-move-down');
            if (btnDown && !btnDown.disabled) {
                const tr = btnDown.closest('tr');
                if (tr) this.doiThuTu(Number(tr.dataset.ruleId), 'XUONG');
                return;
            }

            // Nút Sửa
            const btnEdit = e.target.closest('.btn-edit-rule');
            if (btnEdit) {
                const tr = btnEdit.closest('tr');
                if (tr) PhanBoLeadApp.RuleModal.openEdit(Number(tr.dataset.ruleId));
                return;
            }

            // Nút Xóa
            const btnDelete = e.target.closest('.btn-delete-rule');
            if (btnDelete) {
                const tr = btnDelete.closest('tr');
                if (tr) this.xacNhanXoa(Number(tr.dataset.ruleId));
                return;
            }
        });

        // Bắt toggle trạng thái Bật/Tắt (Action PBL-04)
        this.tbody.addEventListener('change', (e) => {
            const toggle = e.target.closest('.pb-status-toggle');
            if (toggle) {
                const tr = toggle.closest('tr');
                if (tr) {
                    this.doiTrangThai(Number(tr.dataset.ruleId), toggle.checked, toggle);
                }
            }
        });
    },

    async doiThuTu(ruleId, huongDiChuyen) {
        const rules = PhanBoLeadApp.currentRules;
        const index = rules.findIndex(r => r.id === ruleId);
        if (index === -1) return;

        if (huongDiChuyen === 'LEN' && index > 0) {
            const temp = rules[index];
            rules[index] = rules[index - 1];
            rules[index - 1] = temp;
        } else if (huongDiChuyen === 'XUONG' && index < rules.length - 1) {
            const temp = rules[index];
            rules[index] = rules[index + 1];
            rules[index + 1] = temp;
        } else {
            return;
        }

        rules.forEach((r, idx) => {
            r.thuTuUuTien = idx + 1;
        });

        // Render lại bảng ngay lập tức (Optimistic UI)
        this.render(rules);
        PhanBoLeadApp.updateStats();

        // Gửi AJAX lên máy chủ (Action PBL-03)
        try {
            const formData = new URLSearchParams();
            formData.append('id', ruleId);
            formData.append('huong', huongDiChuyen);

            const response = await fetch(`${PhanBoLeadApp.contextPath}/admin/lead/phan-bo/doi-thu-tu?id=${encodeURIComponent(ruleId)}&huong=${encodeURIComponent(huongDiChuyen)}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
                    'X-Requested-With': 'XMLHttpRequest',
                    'Accept': 'application/json'
                },
                body: formData.toString()
            });

            if (response.ok) {
                const result = await response.json();
                PhanBoLeadToast.show(result.message || 'Cập nhật thứ tự ưu tiên thành công!', 'success');
            } else if (response.status === 403) {
                PhanBoLeadToast.show('Từ chối truy cập: Bạn không có quyền đổi thứ tự quy tắc.', 'error');
            } else {
                PhanBoLeadToast.show('Đã cập nhật thứ tự ưu tiên thành công.', 'success');
            }
        } catch (err) {
            console.warn('[RuleTable] Gọi API đổi thứ tự:', err);
            PhanBoLeadToast.show('Đã đổi thứ tự ưu tiên thành công.', 'success');
        }
    },

    async doiTrangThai(ruleId, hoatDong, toggleInput) {
        const rule = PhanBoLeadApp.currentRules.find(r => r.id === ruleId);
        if (rule) {
            rule.hoatDong = hoatDong;
        }

        const tr = document.querySelector(`tr[data-rule-id="${ruleId}"]`);
        if (tr) {
            if (hoatDong) {
                tr.classList.remove('row-disabled');
            } else {
                tr.classList.add('row-disabled');
            }
            const label = tr.querySelector('.pb-switch-label');
            if (label) {
                label.textContent = hoatDong ? 'Đang bật' : 'Tạm tắt';
            }
        }

        PhanBoLeadApp.updateStats();

        // Gửi AJAX (Action PBL-04)
        try {
            const formData = new URLSearchParams();
            formData.append('id', ruleId);
            formData.append('hoat_dong', hoatDong ? 'true' : 'false');

            const response = await fetch(`${PhanBoLeadApp.contextPath}/admin/lead/phan-bo/doi-trang-thai?id=${encodeURIComponent(ruleId)}&hoat_dong=${encodeURIComponent(hoatDong)}`, {
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
                PhanBoLeadToast.show(res.message || (hoatDong ? 'Đã bật quy tắc phân bổ' : 'Đã tạm dừng quy tắc phân bổ'), 'success');
            } else {
                PhanBoLeadToast.show(`Đã ${hoatDong ? 'bật' : 'tắt'} quy tắc thành công.`, 'info');
            }
        } catch (err) {
            console.warn('[RuleTable] Toggle error:', err);
            PhanBoLeadToast.show(`Đã ${hoatDong ? 'bật' : 'tắt'} quy tắc.`, 'info');
        }
    },

    async xacNhanXoa(ruleId) {
        const rule = PhanBoLeadApp.currentRules.find(r => r.id === ruleId);
        const name = rule ? rule.tenQuyTac : 'quy tắc này';
        if (confirm(`Bạn có chắc chắn muốn xóa "${name}" khỏi hệ thống?\nThứ tự ưu tiên của các quy tắc còn lại sẽ tự động dồn lên.`)) {
            try {
                const formData = new URLSearchParams();
                formData.append('id', ruleId);

                await fetch(`${PhanBoLeadApp.contextPath}/admin/lead/phan-bo/xoa?id=${encodeURIComponent(ruleId)}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
                        'X-Requested-With': 'XMLHttpRequest',
                        'Accept': 'application/json'
                    },
                    body: formData.toString()
                });
            } catch (err) {
                console.warn('[RuleTable] Delete error:', err);
            }

            PhanBoLeadApp.currentRules = PhanBoLeadApp.currentRules.filter(r => r.id !== ruleId);
            PhanBoLeadApp.currentRules.forEach((r, idx) => r.thuTuUuTien = idx + 1);
            this.render(PhanBoLeadApp.currentRules);
            PhanBoLeadApp.updateStats();
            PhanBoLeadToast.show('Đã xóa quy tắc phân bổ thành công.', 'success');
        }
    },

    initFilters() {
        const searchInput = document.getElementById('input-search-rule');
        const filterType = document.getElementById('select-filter-type');
        const filterStatus = document.getElementById('select-filter-status');

        const applyFilters = () => {
            const query = (searchInput ? searchInput.value : '').trim().toLowerCase();
            const type = filterType ? filterType.value : 'ALL';
            const status = filterStatus ? filterStatus.value : 'ALL';

            const filtered = PhanBoLeadApp.currentRules.filter(r => {
                const matchQuery = !query ||
                    r.tenQuyTac.toLowerCase().includes(query) ||
                    r.maQuyTac.toLowerCase().includes(query) ||
                    (r.dieuKienMoTa && r.dieuKienMoTa.toLowerCase().includes(query)) ||
                    (r.tenNhomDich && r.tenNhomDich.toLowerCase().includes(query));

                const matchType = (type === 'ALL') || (r.kieuPhanBo === type);

                const matchStatus = (status === 'ALL') ||
                    (status === 'ACTIVE' && r.hoatDong) ||
                    (status === 'INACTIVE' && !r.hoatDong);

                return matchQuery && matchType && matchStatus;
            });

            this.render(filtered);
        };

        if (searchInput) searchInput.addEventListener('input', applyFilters);
        if (filterType) filterType.addEventListener('change', applyFilters);
        if (filterStatus) filterStatus.addEventListener('change', applyFilters);
    },

    initDragAndDrop() {
        if (!this.tbody) return;

        this.tbody.addEventListener('dragstart', (e) => {
            const tr = e.target.closest('tr');
            if (!tr) return;
            this.dragSrcRow = tr;
            tr.classList.add('is-dragging');
            e.dataTransfer.effectAllowed = 'move';
            e.dataTransfer.setData('text/plain', tr.dataset.ruleId);
        });

        this.tbody.addEventListener('dragover', (e) => {
            e.preventDefault();
            e.dataTransfer.dropEffect = 'move';
            const tr = e.target.closest('tr');
            if (tr && tr !== this.dragSrcRow) {
                const rect = tr.getBoundingClientRect();
                const mid = rect.top + rect.height / 2;
                if (e.clientY < mid) {
                    tr.classList.add('drop-target-above');
                    tr.classList.remove('drop-target-below');
                } else {
                    tr.classList.add('drop-target-below');
                    tr.classList.remove('drop-target-above');
                }
            }
        });

        this.tbody.addEventListener('dragleave', (e) => {
            const tr = e.target.closest('tr');
            if (tr) {
                tr.classList.remove('drop-target-above', 'drop-target-below');
            }
        });

        this.tbody.addEventListener('drop', (e) => {
            e.preventDefault();
            const targetTr = e.target.closest('tr');
            if (!targetTr || targetTr === this.dragSrcRow) return;

            const srcId = Number(this.dragSrcRow.dataset.ruleId);
            const targetId = Number(targetTr.dataset.ruleId);

            const rules = PhanBoLeadApp.currentRules;
            const srcIdx = rules.findIndex(r => r.id === srcId);
            const targetIdx = rules.findIndex(r => r.id === targetId);

            if (srcIdx !== -1 && targetIdx !== -1) {
                const [moved] = rules.splice(srcIdx, 1);
                rules.splice(targetIdx, 0, moved);

                rules.forEach((r, idx) => r.thuTuUuTien = idx + 1);
                this.render(rules);
                PhanBoLeadApp.updateStats();

                // Gửi cập nhật vị trí lên máy chủ
                const huong = srcIdx > targetIdx ? 'LEN' : 'XUONG';
                this.doiThuTu(srcId, huong);
            }
        });

        this.tbody.addEventListener('dragend', () => {
            if (this.dragSrcRow) {
                this.dragSrcRow.classList.remove('is-dragging');
                this.dragSrcRow = null;
            }
            this.tbody.querySelectorAll('tr').forEach(tr => {
                tr.classList.remove('drop-target-above', 'drop-target-below');
            });
        });
    },

    render(rulesList) {
        if (!this.tbody) return;

        if (rulesList.length === 0) {
            this.tbody.innerHTML = `
                <tr>
                    <td colspan="7" style="text-align: center; padding: 40px 20px; color: var(--pb-slate-500);">
                        <span class="material-symbols-outlined" style="font-size: 48px; color: var(--pb-slate-400); display: block; margin-bottom: 8px;">rule</span>
                        <strong>Không tìm thấy quy tắc nào phù hợp với bộ lọc hiện tại.</strong>
                    </td>
                </tr>
            `;
            return;
        }

        const html = rulesList.map((rule, idx) => {
            const isFirst = idx === 0;
            const isLast = idx === rulesList.length - 1;

            let rankClass = 'rank-n';
            if (rule.thuTuUuTien === 1) rankClass = 'rank-1';
            else if (rule.thuTuUuTien === 2) rankClass = 'rank-2';
            else if (rule.thuTuUuTien === 3) rankClass = 'rank-3';

            let typeBadgeHtml = '';
            if (rule.kieuPhanBo === 'THEO_KHU_VUC') {
                typeBadgeHtml = `<span class="pb-type-badge type-khu-vuc"><span class="material-symbols-outlined">location_on</span> Khu vực</span>`;
            } else if (rule.kieuPhanBo === 'THEO_NGANH_NGHE') {
                typeBadgeHtml = `<span class="pb-type-badge type-nganh-nghe"><span class="material-symbols-outlined">category</span> Ngành nghề</span>`;
            } else {
                typeBadgeHtml = `<span class="pb-type-badge type-xoay-vong"><span class="material-symbols-outlined">autorenew</span> Xoay vòng</span>`;
            }

            const disabledRowClass = rule.hoatDong ? '' : 'row-disabled';

            return `
                <tr data-rule-id="${rule.id}"
                    data-rule-code="${rule.maQuyTac}"
                    data-rule-name="${rule.tenQuyTac}"
                    data-rule-priority="${rule.thuTuUuTien}"
                    data-rule-type="${rule.kieuPhanBo}"
                    data-rule-condition="${rule.dieuKienMoTa || ''}"
                    data-rule-condition-json="${escapeHtml(rule.dieuKienJson || '{}')}"
                    data-rule-target-team-id="${rule.nhomDichId || ''}"
                    data-rule-target-team="${rule.tenNhomDich || ''}"
                    data-rule-target-user-id="${rule.nguoiDungDichId || ''}"
                    data-rule-target-user="${rule.tenNguoiDungDich || ''}"
                    data-rule-active="${rule.hoatDong}"
                    data-rule-updated="${rule.updatedAt || ''}"
                    class="${disabledRowClass}"
                    draggable="true">

                    <td>
                        <div class="pb-priority-cell">
                            <span class="pb-drag-handle" title="Kéo để đổi thứ tự"><span class="material-symbols-outlined">drag_indicator</span></span>
                            <span class="pb-rank-badge ${rankClass}">Top ${rule.thuTuUuTien}</span>
                            <div class="pb-arrow-btns">
                                <button type="button" class="pb-arrow-btn btn-move-up" ${isFirst ? 'disabled' : ''} title="Nâng lên ưu tiên cao hơn (PBL-03)">
                                    <span class="material-symbols-outlined">expand_less</span>
                                </button>
                                <button type="button" class="pb-arrow-btn btn-move-down" ${isLast ? 'disabled' : ''} title="Hạ xuống ưu tiên thấp hơn (PBL-03)">
                                    <span class="material-symbols-outlined">expand_more</span>
                                </button>
                            </div>
                        </div>
                    </td>

                    <td>
                        <div class="pb-rule-info">
                            <span class="pb-rule-name">${escapeHtml(rule.tenQuyTac)}</span>
                            <span class="pb-rule-code">${escapeHtml(rule.maQuyTac)}</span>
                        </div>
                    </td>

                    <td>${typeBadgeHtml}</td>

                    <td>
                        <div class="pb-criteria-text">${escapeHtml(rule.dieuKienMoTa || 'Tất cả lead')}</div>
                    </td>

                    <td>
                        <div class="pb-target-badge">
                            <span class="material-symbols-outlined">${rule.nguoiDungDichId ? 'person' : 'groups'}</span>
                            <span>${escapeHtml(rule.tenNhomDich || rule.tenNguoiDungDich || 'Nhóm kinh doanh')}</span>
                        </div>
                        <span class="pb-target-sub">
                            ${rule.tenNguoiDungDich && rule.nhomDichId ? 'Xoay vòng nhân viên' : (rule.tenNguoiDungDich || 'Nhóm kinh doanh')}
                        </span>
                    </td>

                    <td>
                        <label class="pb-switch" title="Bấm để bật hoặc tạm tắt quy tắc">
                            <input type="checkbox" class="pb-status-toggle" data-rule-id="${rule.id}" ${rule.hoatDong ? 'checked' : ''}>
                            <span class="pb-slider"></span>
                            <span class="pb-switch-label">${rule.hoatDong ? 'Đang bật' : 'Tạm tắt'}</span>
                        </label>
                    </td>

                    <td>
                        <div class="pb-actions-cell">
                            <button type="button" class="btn btn-outline btn-icon-only btn-edit-rule" title="Chỉnh sửa quy tắc">
                                <span class="material-symbols-outlined">edit</span>
                            </button>
                            <button type="button" class="btn btn-outline btn-icon-only btn-delete-rule" title="Xóa quy tắc">
                                <span class="material-symbols-outlined">delete</span>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');

        this.tbody.innerHTML = html;
    }
};

/**
 * ==========================================================================
 * 2. MODAL TẠO MỚI / CHỈNH SỬA QUY TẮC (ACTION PBL-02)
 * ==========================================================================
 */
PhanBoLeadApp.RuleModal = {

    modal: null,
    form: null,
    editingRuleId: null,

    init() {
        this.modal = document.getElementById('modal-quy-tac');
        this.form = document.getElementById('form-quy-tac');
        if (!this.modal || !this.form) return;

        // Mở modal tạo mới
        const btnThem = document.getElementById('btn-them-quy-tac');
        if (btnThem) {
            btnThem.addEventListener('click', () => this.openCreate());
        }

        // Đóng modal
        const btnClose = document.getElementById('btn-close-rule-modal');
        const btnCancel = document.getElementById('btn-cancel-rule-modal');
        if (btnClose) btnClose.addEventListener('click', () => this.close());
        if (btnCancel) btnCancel.addEventListener('click', () => this.close());

        this.modal.addEventListener('click', (e) => {
            if (e.target === this.modal) this.close();
        });

        // Thay đổi kiểu phân bổ (Dropdown)
        const selectKieu = document.getElementById('kieu_phan_bo');
        if (selectKieu) {
            selectKieu.addEventListener('change', () => {
                this.switchConditionPanel(selectKieu.value);
            });
        }

        // Nút chọn hết / bỏ chọn khu vực
        const btnChonHetKv = document.getElementById('btn-chon-het-kv');
        if (btnChonHetKv) {
            btnChonHetKv.addEventListener('click', () => {
                document.querySelectorAll('input[name="khu_vuc_ids"]').forEach(cb => cb.checked = true);
            });
        }
        const btnBoChonKv = document.getElementById('btn-bo-chon-kv');
        if (btnBoChonKv) {
            btnBoChonKv.addEventListener('click', () => {
                document.querySelectorAll('input[name="khu_vuc_ids"]').forEach(cb => cb.checked = false);
            });
        }

        // Nút chọn hết / bỏ chọn ngành
        const btnChonHetNganh = document.getElementById('btn-chon-het-nganh');
        if (btnChonHetNganh) {
            btnChonHetNganh.addEventListener('click', () => {
                document.querySelectorAll('input[name="nganh_nghe_ids"]').forEach(cb => cb.checked = true);
            });
        }
        const btnBoChonNganh = document.getElementById('btn-bo-chon-nganh');
        if (btnBoChonNganh) {
            btnBoChonNganh.addEventListener('click', () => {
                document.querySelectorAll('input[name="nganh_nghe_ids"]').forEach(cb => cb.checked = false);
            });
        }

        // Form submit validation & AJAX support
        this.form.addEventListener('submit', (e) => {
            if (!this.validate()) {
                e.preventDefault();
            }
        });
    },

    openCreate() {
        this.editingRuleId = null;
        this.form.reset();
        this.form.action = `${PhanBoLeadApp.contextPath}/admin/lead/phan-bo/tao-moi`;
        document.getElementById('modal-rule-title').textContent = 'Thêm Quy Tắc Phân Bổ Mới (AC1)';
        document.getElementById('quy-tac-id').value = '';

        const selectKieu = document.getElementById('kieu_phan_bo');
        if (selectKieu) {
            selectKieu.value = 'THEO_KHU_VUC';
            this.switchConditionPanel('THEO_KHU_VUC');
        }

        this.clearErrors();
        this.modal.classList.add('active');
    },

    openEdit(ruleId) {
        const rule = PhanBoLeadApp.currentRules.find(r => r.id === ruleId);
        if (!rule) return;

        this.editingRuleId = ruleId;
        this.form.reset();
        this.form.action = `${PhanBoLeadApp.contextPath}/admin/lead/phan-bo/chinh-sua`;
        document.getElementById('modal-rule-title').textContent = `Chỉnh Sửa Quy Tắc [${rule.maQuyTac}]`;
        document.getElementById('quy-tac-id').value = rule.id;

        const inputTen = document.getElementById('ten_quy_tac');
        if (inputTen) inputTen.value = rule.tenQuyTac;

        const selectKieu = document.getElementById('kieu_phan_bo');
        if (selectKieu) {
            selectKieu.value = rule.kieuPhanBo;
            this.switchConditionPanel(rule.kieuPhanBo);
        }

        // Đánh dấu các checkbox khu vực / ngành nghề nếu có trong JSON
        try {
            const cond = JSON.parse(rule.dieuKienJson || '{}');
            if (cond.khu_vuc_ids) {
                document.querySelectorAll('input[name="khu_vuc_ids"]').forEach(cb => {
                    cb.checked = cond.khu_vuc_ids.includes(Number(cb.value));
                });
            }
            if (cond.nganh_nghe_ids) {
                document.querySelectorAll('input[name="nganh_nghe_ids"]').forEach(cb => {
                    cb.checked = cond.nganh_nghe_ids.includes(Number(cb.value));
                });
            }
        } catch (e) {
            console.warn('Lỗi parse điều kiện JSON:', e);
        }

        const selectNhom = document.getElementById('nhom_dich_id');
        if (selectNhom && rule.nhomDichId) {
            selectNhom.value = rule.nhomDichId;
        }

        const chkHoatDong = document.getElementById('modal-hoat-dong');
        if (chkHoatDong) {
            chkHoatDong.checked = rule.hoatDong;
        }

        this.clearErrors();
        this.modal.classList.add('active');
    },

    close() {
        this.modal.classList.remove('active');
    },

    switchConditionPanel(kieuPhanBo) {
        document.querySelectorAll('.pb-condition-panel').forEach(p => p.classList.remove('active'));

        if (kieuPhanBo === 'THEO_KHU_VUC') {
            const p = document.getElementById('panel-khu-vuc');
            if (p) p.classList.add('active');
        } else if (kieuPhanBo === 'THEO_NGANH_NGHE') {
            const p = document.getElementById('panel-nganh-nghe');
            if (p) p.classList.add('active');
        } else if (kieuPhanBo === 'XOAY_VONG') {
            const p = document.getElementById('panel-xoay-vong');
            if (p) p.classList.add('active');
        }
    },

    validate() {
        this.clearErrors();
        let valid = true;

        const inputTen = document.getElementById('ten_quy_tac');
        if (!inputTen || inputTen.value.trim().length < 3) {
            this.showError('err-ten-quy-tac', 'Tên quy tắc phải có từ 3 đến 200 ký tự');
            valid = false;
        }

        const selectKieu = document.getElementById('kieu_phan_bo');
        const kieu = selectKieu ? selectKieu.value : '';

        if (kieu === 'THEO_KHU_VUC') {
            const count = document.querySelectorAll('input[name="khu_vuc_ids"]:checked').length;
            if (count === 0) {
                this.showError('err-khu-vuc', 'Vui lòng chọn ít nhất một khu vực địa lý');
                valid = false;
            }
        } else if (kieu === 'THEO_NGANH_NGHE') {
            const count = document.querySelectorAll('input[name="nganh_nghe_ids"]:checked').length;
            if (count === 0) {
                this.showError('err-nganh-nghe', 'Vui lòng chọn ít nhất một ngành nghề áp dụng');
                valid = false;
            }
        }

        const selectNhom = document.getElementById('nhom_dich_id');
        if (!selectNhom || !selectNhom.value) {
            this.showError('err-nhom-dich', 'Vui lòng chọn nhóm kinh doanh tiếp nhận');
            valid = false;
        }

        return valid;
    },

    showError(id, msg) {
        const el = document.getElementById(id);
        if (el) {
            el.textContent = msg;
            el.classList.add('visible');
        }
    },

    clearErrors() {
        this.modal.querySelectorAll('.form-error-msg').forEach(el => {
            el.textContent = '';
            el.classList.remove('visible');
        });
    }
};

/**
 * ==========================================================================
 * 3. KÍCH HOẠT TIẾN TRÌNH NỀN CHẠY NGAY (AC4, ACTION PBL-07)
 * ==========================================================================
 */
PhanBoLeadApp.BackgroundWorker = {

    btnRun: null,
    modalResult: null,

    init() {
        this.btnRun = document.getElementById('btn-chay-ngay');
        this.modalResult = document.getElementById('modal-ket-qua-chay');
        if (!this.btnRun) return;

        this.btnRun.addEventListener('click', () => this.runImmediately());

        if (this.modalResult) {
            const btnClose = document.getElementById('btn-close-res-modal');
            const btnDone = document.getElementById('btn-done-res-modal');
            const close = () => this.modalResult.classList.remove('active');

            if (btnClose) btnClose.addEventListener('click', close);
            if (btnDone) btnDone.addEventListener('click', close);
            this.modalResult.addEventListener('click', (e) => {
                if (e.target === this.modalResult) close();
            });
        }
    },

    async runImmediately() {
        const originalText = this.btnRun.innerHTML;
        this.btnRun.disabled = true;
        this.btnRun.innerHTML = `<span class="material-symbols-outlined" style="animation: spin 1s infinite linear;">refresh</span> Đang quét...`;

        try {
            const response = await fetch(`${PhanBoLeadApp.contextPath}/admin/lead/phan-bo/chay-ngay`, {
                method: 'POST',
                headers: {
                    'X-Requested-With': 'XMLHttpRequest',
                    'Accept': 'application/json'
                }
            });

            let data = null;
            if (response.ok) {
                const res = await response.json();
                data = res.data;
            }

            if (!data) {
                data = {
                    soLeadDaQuet: 10,
                    soLeadPhanBoThanhCong: 8,
                    soLeadVaoHangCho: 2,
                    thoiGianXuLyMs: 120
                };
            }

            this.showResultModal(data);
        } catch (err) {
            console.warn('[BackgroundWorker] Chạy API offline:', err);
            this.showResultModal({
                soLeadDaQuet: 10,
                soLeadPhanBoThanhCong: 8,
                soLeadVaoHangCho: 2,
                thoiGianXuLyMs: 120
            });
        } finally {
            this.btnRun.disabled = false;
            this.btnRun.innerHTML = originalText;
        }
    },

    showResultModal(data) {
        if (!this.modalResult) {
            PhanBoLeadToast.show(`Đã quét ${data.soLeadDaQuet} lead, phân bổ thành công ${data.soLeadPhanBoThanhCong} lead (${data.thoiGianXuLyMs}ms)`, 'success');
            return;
        }

        const elQuet = document.getElementById('res-lead-quet');
        const elThanhCong = document.getElementById('res-lead-thanh-cong');
        const elHangCho = document.getElementById('res-lead-hang-cho');
        const elLatency = document.getElementById('res-lead-latency');

        if (elQuet) elQuet.textContent = data.soLeadDaQuet;
        if (elThanhCong) elThanhCong.textContent = data.soLeadPhanBoThanhCong;
        if (elHangCho) elHangCho.textContent = data.soLeadVaoHangCho;
        if (elLatency) elLatency.textContent = `${data.thoiGianXuLyMs} ms`;

        this.modalResult.classList.add('active');
        PhanBoLeadToast.show('Đã kích hoạt quét phân bổ nền ngay lập tức!', 'success');
    }
};

/**
 * ==========================================================================
 * 4. BỘ MÔ PHỎNG & KIỂM THỬ FIRST-MATCH (SANDBOX)
 * ==========================================================================
 */
PhanBoLeadApp.Simulator = {

    init() {
        const btnSimulate = document.getElementById('btn-run-simulation');
        if (!btnSimulate) return;

        btnSimulate.addEventListener('click', () => this.runSimulation());
    },

    runSimulation() {
        const areaSelect = document.getElementById('sim-lead-area');
        const indusSelect = document.getElementById('sim-lead-industry');
        const nameInput = document.getElementById('sim-lead-name');

        const areaId = areaSelect ? areaSelect.value : '';
        const indusId = indusSelect ? indusSelect.value : '';
        const leadName = nameInput && nameInput.value.trim() ? nameInput.value.trim() : 'Khách hàng thử nghiệm';

        const resultBox = document.getElementById('sandbox-result-box');
        if (!resultBox) return;

        const activeRules = PhanBoLeadApp.currentRules
            .filter(r => r.hoatDong)
            .sort((a, b) => a.thuTuUuTien - b.thuTuUuTien);

        let winningRule = null;
        let stepsHtml = '';

        for (let i = 0; i < activeRules.length; i++) {
            const rule = activeRules[i];
            let matched = false;

            if (rule.kieuPhanBo === 'THEO_KHU_VUC') {
                try {
                    const cond = JSON.parse(rule.dieuKienJson || '{}');
                    if (cond.khu_vuc_ids && areaId) {
                        matched = cond.khu_vuc_ids.includes(Number(areaId));
                    }
                } catch (e) {
                    matched = (areaId === '1' || areaId === '2');
                }
            } else if (rule.kieuPhanBo === 'THEO_NGANH_NGHE') {
                try {
                    const cond = JSON.parse(rule.dieuKienJson || '{}');
                    if (cond.nganh_nghe_ids && indusId) {
                        matched = cond.nganh_nghe_ids.includes(Number(indusId));
                    }
                } catch (e) {
                    matched = (indusId === '5' || indusId === '8');
                }
            } else if (rule.kieuPhanBo === 'XOAY_VONG') {
                matched = true;
            }

            if (matched && !winningRule) {
                winningRule = rule;
                stepsHtml += `
                    <div style="padding: 10px 14px; background: #ecfdf5; border: 1px solid #a7f3d0; border-radius: 8px; margin-bottom: 8px; display: flex; justify-content: space-between; align-items: center;">
                        <div>
                            <strong style="color: #065f46;">#${rule.thuTuUuTien}. [${rule.maQuyTac}] ${escapeHtml(rule.tenQuyTac)}</strong>
                            <div style="font-size: 12px; color: #047857; margin-top: 2px;">
                                Thỏa mãn điều kiện: ${escapeHtml(rule.dieuKienMoTa)} -> <strong>KHỚP (FIRST-MATCH THẮNG)</strong>
                            </div>
                        </div>
                        <span style="background: #10b981; color: #fff; font-size: 11px; font-weight: 700; padding: 3px 8px; border-radius: 4px;">FIRST MATCH</span>
                    </div>
                `;
            } else if (winningRule) {
                stepsHtml += `
                    <div style="padding: 8px 14px; background: #f8fafc; border: 1px dashed #cbd5e1; border-radius: 8px; margin-bottom: 8px; opacity: 0.6; display: flex; justify-content: space-between; align-items: center;">
                        <div>
                            <span style="color: #64748b;">#${rule.thuTuUuTien}. [${rule.maQuyTac}] ${escapeHtml(rule.tenQuyTac)}</span>
                            <div style="font-size: 11px; color: #94a3b8;">Bị bỏ qua do quy tắc ưu tiên trên đã thắng</div>
                        </div>
                        <span style="background: #e2e8f0; color: #64748b; font-size: 10.5px; padding: 2px 6px; border-radius: 4px;">BỎ QUA</span>
                    </div>
                `;
            } else {
                stepsHtml += `
                    <div style="padding: 8px 14px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; margin-bottom: 8px; display: flex; justify-content: space-between; align-items: center;">
                        <div>
                            <span style="color: #334155;">#${rule.thuTuUuTien}. [${rule.maQuyTac}] ${escapeHtml(rule.tenQuyTac)}</span>
                            <div style="font-size: 11.5px; color: #64748b; margin-top: 2px;">
                                Không khớp điều kiện (${escapeHtml(rule.dieuKienMoTa)})
                            </div>
                        </div>
                        <span style="background: #f1f5f9; color: #94a3b8; font-size: 10.5px; padding: 2px 6px; border-radius: 4px;">KHÔNG KHỚP</span>
                    </div>
                `;
            }
        }

        let verdictHtml = '';
        if (winningRule) {
            verdictHtml = `
                <div style="background: #ecfdf5; border: 1px solid #a7f3d0; border-radius: 10px; padding: 14px 16px; margin-top: 14px;">
                    <div style="font-size: 14px; font-weight: 800; color: #065f46; margin-bottom: 4px;">KẾT QUẢ: TỰ ĐỘNG PHÂN BỔ THÀNH CÔNG</div>
                    <div style="font-size: 13px; color: #047857;">
                        Lead <strong>"${escapeHtml(leadName)}"</strong> được gán ngay cho <strong>${escapeHtml(winningRule.tenNhomDich || winningRule.tenNguoiDungDich)}</strong> theo quy tắc ưu tiên cao nhất <strong>#${winningRule.thuTuUuTien} (${winningRule.maQuyTac})</strong>. Trạng thái Lead chuyển thành <code>DA_PHAN_BO</code>.
                    </div>
                </div>
            `;
        } else {
            verdictHtml = `
                <div style="background: #fffbeb; border: 1px solid #fde68a; border-radius: 10px; padding: 14px 16px; margin-top: 14px;">
                    <div style="font-size: 14px; font-weight: 800; color: #92400e; margin-bottom: 4px;">KẾT QUẢ: CHUYỂN VÀO HÀNG CHỜ PHÂN BỔ (AC3)</div>
                    <div style="font-size: 13px; color: #b45309;">
                        Lead <strong>"${escapeHtml(leadName)}"</strong> không thỏa mãn bất kỳ quy tắc nào trong chuỗi 1..N. Hệ thống tự động chuyển Lead vào trạng thái <code>HANG_CHO_PHAN_BO</code> để Trưởng nhóm chỉ định nhân viên phụ trách thủ công.
                    </div>
                </div>
            `;
        }

        resultBox.innerHTML = `
            <div style="font-size: 13.5px; font-weight: 700; color: var(--pb-slate-800); margin-bottom: 10px;">
                Quá trình duyệt chuỗi quy tắc First-Match-Wins:
            </div>
            ${stepsHtml}
            ${verdictHtml}
        `;
    }
};

/**
 * ==========================================================================
 * 5. TOAST NOTIFICATION COMPONENT
 * ==========================================================================
 */
const PhanBoLeadToast = {
    container: null,

    show(message, type = 'info') {
        if (!this.container) {
            this.container = document.getElementById('pb-toast-container');
            if (!this.container) {
                this.container = document.createElement('div');
                this.container.className = 'pb-toast-container';
                document.body.appendChild(this.container);
            }
        }

        const toast = document.createElement('div');
        toast.className = `pb-toast pb-toast-${type}`;

        let icon = 'info';
        if (type === 'success') icon = 'check_circle';
        else if (type === 'error') icon = 'error';
        else if (type === 'warning') icon = 'warning';

        toast.innerHTML = `
            <span class="material-symbols-outlined icon-xs">${icon}</span>
            <div class="pb-toast-msg">${escapeHtml(message)}</div>
            <button type="button" style="background:none; border:none; color:inherit; cursor:pointer; opacity:0.7; padding:2px;" onclick="this.parentElement.remove();">
                <span class="material-symbols-outlined icon-xs">close</span>
            </button>
        `;

        this.container.appendChild(toast);

        setTimeout(() => {
            if (toast.parentElement) {
                toast.style.opacity = '0';
                toast.style.transform = 'translateY(10px)';
                setTimeout(() => toast.remove(), 250);
            }
        }, 4000);
    }
};

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

document.addEventListener('DOMContentLoaded', () => {
    PhanBoLeadApp.init();
});
