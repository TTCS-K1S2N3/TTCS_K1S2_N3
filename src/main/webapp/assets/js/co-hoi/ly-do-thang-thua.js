/**
 * JavaScript: Xử lý tương tác danh mục Lý do thắng thua và Đối thủ cạnh tranh (Story S2-10)
 * Hỗ trợ chuyển tab, live filter, AJAX toggle trạng thái, modal CRUD và Sprint 5 opportunity validator.
 */
document.addEventListener('DOMContentLoaded', function () {
    const contextPath = window.CRM_CONTEXT_PATH || '';

    // =========================================================================
    // 1. CHUYỂN ĐỔI TAB (Tự động cập nhật tiêu đề nút thêm mới & URL)
    // =========================================================================
    const tabButtons = document.querySelectorAll('.crm-nav-tab-btn');
    const tabPanes = document.querySelectorAll('.crm-tab-pane');
    const btnAddNew = document.getElementById('btn-add-new-record');

    function switchTab(tabId) {
        tabButtons.forEach(btn => {
            const isTarget = btn.getAttribute('data-tab') === tabId;
            btn.classList.toggle('active', isTarget);
            btn.setAttribute('aria-selected', isTarget ? 'true' : 'false');
        });

        tabPanes.forEach(pane => {
            const isTarget = pane.getAttribute('id') === 'tab-pane-' + tabId;
            pane.style.display = isTarget ? 'block' : 'none';
        });

        // Cập nhật nút Thêm mới theo tab
        if (btnAddNew) {
            if (tabId === 'thang') {
                btnAddNew.style.display = 'inline-flex';
                btnAddNew.innerHTML = `
                    <svg class="crm-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                        <line x1="12" y1="5" x2="12" y2="19"></line>
                        <line x1="5" y1="12" x2="19" y2="12"></line>
                    </svg>
                    Thêm lý do thắng
                `;
            } else if (tabId === 'thua') {
                btnAddNew.style.display = 'inline-flex';
                btnAddNew.innerHTML = `
                    <svg class="crm-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                        <line x1="12" y1="5" x2="12" y2="19"></line>
                        <line x1="5" y1="12" x2="19" y2="12"></line>
                    </svg>
                    Thêm lý do thua
                `;
            } else if (tabId === 'doi-thu') {
                btnAddNew.style.display = 'inline-flex';
                btnAddNew.innerHTML = `
                    <svg class="crm-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                        <line x1="12" y1="5" x2="12" y2="19"></line>
                        <line x1="5" y1="12" x2="19" y2="12"></line>
                    </svg>
                    Thêm đối thủ cạnh tranh
                `;
            } else {
                btnAddNew.style.display = 'none';
            }
        }

        // Cập nhật history URL không reload trang
        const url = new URL(window.location.href);
        url.searchParams.set('tab', tabId);
        window.history.replaceState({}, '', url.toString());
    }

    tabButtons.forEach(btn => {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            const tabId = this.getAttribute('data-tab');
            switchTab(tabId);
        });
    });

    // =========================================================================
    // 2. LIVE SEARCH VÀ LỌC BẢNG DỮ LIỆU
    // =========================================================================
    const searchInput = document.getElementById('catalog-search-input');
    const statusFilter = document.getElementById('catalog-status-filter');

    function applyFilter() {
        const query = (searchInput ? searchInput.value : '').trim().toLowerCase();
        const status = (statusFilter ? statusFilter.value : '').toLowerCase();

        const activePane = document.querySelector('.crm-tab-pane[style*="block"]') || document.querySelector('.crm-tab-pane');
        if (!activePane) return;

        const rows = activePane.querySelectorAll('tbody tr[data-search]');
        rows.forEach(row => {
            const searchData = row.getAttribute('data-search').toLowerCase();
            const rowStatus = row.getAttribute('data-status').toLowerCase();

            const matchQuery = !query || searchData.includes(query);
            let matchStatus = true;
            if (status === 'active' || status === '1') {
                matchStatus = (rowStatus === 'active' || rowStatus === '1');
            } else if (status === 'inactive' || status === '0') {
                matchStatus = (rowStatus === 'inactive' || rowStatus === '0');
            }

            row.style.display = (matchQuery && matchStatus) ? '' : 'none';
        });
    }

    if (searchInput) searchInput.addEventListener('input', applyFilter);
    if (statusFilter) statusFilter.addEventListener('change', applyFilter);

    // =========================================================================
    // 3. QUẢN LÝ MODAL THÊM / SỬA LÝ DO THẮNG THUA
    // =========================================================================
    const modalLyDo = document.getElementById('modal-ly-do');
    const formLyDo = document.getElementById('form-ly-do');
    const modalLyDoTitle = document.getElementById('modal-ly-do-title');
    const inputLyDoId = document.getElementById('ly-do-id');
    const inputLyDoAction = document.getElementById('ly-do-action');
    const inputLyDoMa = document.getElementById('ly-do-ma');
    const inputLyDoTen = document.getElementById('ly-do-ten');
    const selectLyDoLoai = document.getElementById('ly-do-loai');
    const inputLyDoThuTu = document.getElementById('ly-do-thu-tu');
    const checkLyDoHoatDong = document.getElementById('ly-do-hoat-dong');

    function openModalLyDo(isEdit, data) {
        if (!modalLyDo) return;
        formLyDo.reset();

        const activeTabBtn = document.querySelector('.crm-nav-tab-btn.active');
        const currentTab = activeTabBtn ? activeTabBtn.getAttribute('data-tab') : 'thang';

        if (isEdit && data) {
            modalLyDoTitle.textContent = 'Chỉnh sửa lý do ' + (data.loai === 'THANG' ? 'thắng' : 'thua');
            inputLyDoAction.value = 'sua-ly-do';
            inputLyDoId.value = data.id || '';
            inputLyDoMa.value = data.ma || '';
            inputLyDoTen.value = data.ten || '';
            selectLyDoLoai.value = data.loai || 'THANG';
            inputLyDoThuTu.value = data.thuTu || 0;
            checkLyDoHoatDong.checked = (data.hoatDong === 'true' || data.hoatDong === true);
        } else {
            const loaiMacDinh = (currentTab === 'thua') ? 'THUA' : 'THANG';
            modalLyDoTitle.textContent = 'Thêm mới lý do ' + (loaiMacDinh === 'THANG' ? 'thắng' : 'thua');
            inputLyDoAction.value = 'them-ly-do';
            inputLyDoId.value = '';
            inputLyDoMa.value = (loaiMacDinh === 'THANG' ? 'WIN_' : 'LOSS_');
            inputLyDoTen.value = '';
            selectLyDoLoai.value = loaiMacDinh;
            inputLyDoThuTu.value = 0;
            checkLyDoHoatDong.checked = true;
        }

        modalLyDo.classList.add('open');
        inputLyDoTen.focus();
    }

    function closeModalLyDo() {
        if (modalLyDo) modalLyDo.classList.remove('open');
    }

    // =========================================================================
    // 4. QUẢN LÝ MODAL THÊM / SỬA ĐỐI THỦ CẠNH TRANH
    // =========================================================================
    const modalDoiThu = document.getElementById('modal-doi-thu');
    const formDoiThu = document.getElementById('form-doi-thu');
    const modalDoiThuTitle = document.getElementById('modal-doi-thu-title');
    const inputDoiThuId = document.getElementById('doi-thu-id');
    const inputDoiThuAction = document.getElementById('doi-thu-action');
    const inputDoiThuMa = document.getElementById('doi-thu-ma');
    const inputDoiThuTen = document.getElementById('doi-thu-ten');
    const inputDoiThuWebsite = document.getElementById('doi-thu-website');
    const inputDoiThuGhiChu = document.getElementById('doi-thu-ghi-chu');
    const checkDoiThuHoatDong = document.getElementById('doi-thu-hoat-dong');

    function openModalDoiThu(isEdit, data) {
        if (!modalDoiThu) return;
        formDoiThu.reset();

        if (isEdit && data) {
            modalDoiThuTitle.textContent = 'Chỉnh sửa đối thủ cạnh tranh';
            inputDoiThuAction.value = 'sua-doi-thu';
            inputDoiThuId.value = data.id || '';
            inputDoiThuMa.value = data.ma || '';
            inputDoiThuTen.value = data.ten || '';
            inputDoiThuWebsite.value = data.website || '';
            inputDoiThuGhiChu.value = data.ghiChu || '';
            checkDoiThuHoatDong.checked = (data.hoatDong === 'true' || data.hoatDong === true);
        } else {
            modalDoiThuTitle.textContent = 'Thêm mới đối thủ cạnh tranh';
            inputDoiThuAction.value = 'them-doi-thu';
            inputDoiThuId.value = '';
            inputDoiThuMa.value = 'DT_';
            inputDoiThuTen.value = '';
            inputDoiThuWebsite.value = '';
            inputDoiThuGhiChu.value = '';
            checkDoiThuHoatDong.checked = true;
        }

        modalDoiThu.classList.add('open');
        inputDoiThuTen.focus();
    }

    function closeModalDoiThu() {
        if (modalDoiThu) modalDoiThu.classList.remove('open');
    }

    // Nút kích hoạt mở modal thêm mới chung
    if (btnAddNew) {
        btnAddNew.addEventListener('click', function () {
            const activeTabBtn = document.querySelector('.crm-nav-tab-btn.active');
            const currentTab = activeTabBtn ? activeTabBtn.getAttribute('data-tab') : 'thang';
            if (currentTab === 'doi-thu') {
                openModalDoiThu(false, null);
            } else {
                openModalLyDo(false, null);
            }
        });
    }

    // Gắn sự kiện sửa Lý do
    document.querySelectorAll('.btn-edit-ly-do').forEach(btn => {
        btn.addEventListener('click', function () {
            const data = {
                id: this.getAttribute('data-id'),
                ma: this.getAttribute('data-ma'),
                ten: this.getAttribute('data-ten'),
                loai: this.getAttribute('data-loai'),
                thuTu: this.getAttribute('data-thutu'),
                hoatDong: this.getAttribute('data-hoatdong') === 'true'
            };
            openModalLyDo(true, data);
        });
    });

    // Gắn sự kiện sửa Đối thủ
    document.querySelectorAll('.btn-edit-doi-thu').forEach(btn => {
        btn.addEventListener('click', function () {
            const data = {
                id: this.getAttribute('data-id'),
                ma: this.getAttribute('data-ma'),
                ten: this.getAttribute('data-ten'),
                website: this.getAttribute('data-website'),
                ghiChu: this.getAttribute('data-ghichu'),
                hoatDong: this.getAttribute('data-hoatdong') === 'true'
            };
            openModalDoiThu(true, data);
        });
    });

    // Đóng Modal qua nút X và nút Hủy
    document.querySelectorAll('.crm-modal-close-btn, .btn-cancel-modal').forEach(btn => {
        btn.addEventListener('click', function () {
            closeModalLyDo();
            closeModalDoiThu();
            closeModalXoa();
        });
    });

    // Đóng modal khi bấm Escape
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') {
            closeModalLyDo();
            closeModalDoiThu();
            closeModalXoa();
        }
    });

    // =========================================================================
    // 5. MODAL XÁC NHẬN XÓA AN TOÀN
    // =========================================================================
    const modalXoa = document.getElementById('modal-xoa');
    const formXoa = document.getElementById('form-xoa');
    const inputXoaAction = document.getElementById('xoa-action');
    const inputXoaId = document.getElementById('xoa-id');
    const xoaWarningText = document.getElementById('xoa-warning-text');
    const xoaObjectName = document.getElementById('xoa-object-name');

    function openModalXoa(action, id, ten, soThamChieu) {
        if (!modalXoa) return;
        inputXoaAction.value = action;
        inputXoaId.value = id;
        xoaObjectName.textContent = ten || '';

        if (soThamChieu > 0) {
            xoaWarningText.innerHTML = `
                <div style="background:#fef2f2; border:1px solid #fecaca; color:#991b1b; padding:12px; border-radius:8px; margin-top:8px;">
                    <strong>Cảnh báo quan trọng:</strong> Mục này đang được <strong>${soThamChieu}</strong> cơ hội bán hàng tham chiếu.<br>
                    Hệ thống sẽ <strong>chặn xóa</strong> theo ràng buộc cơ sở dữ liệu. Khuyến nghị quý khách chuyển trạng thái sang <em>Ngừng hoạt động</em>.
                </div>
            `;
        } else {
            xoaWarningText.innerHTML = `Bạn có chắc chắn muốn xóa bản ghi này? Thao tác không thể hoàn tác.`;
        }

        modalXoa.classList.add('open');
    }

    function closeModalXoa() {
        if (modalXoa) modalXoa.classList.remove('open');
    }

    document.querySelectorAll('.btn-delete-record').forEach(btn => {
        btn.addEventListener('click', function () {
            const action = this.getAttribute('data-action');
            const id = this.getAttribute('data-id');
            const ten = this.getAttribute('data-name');
            const soThamChieu = parseInt(this.getAttribute('data-ref') || '0', 10);
            openModalXoa(action, id, ten, soThamChieu);
        });
    });

    // =========================================================================
    // 6. TOGGLE TRẠNG THÁI HOẠT ĐỘNG QUA AJAX
    // =========================================================================
    document.querySelectorAll('.crm-ajax-status-toggle').forEach(checkbox => {
        checkbox.addEventListener('change', function () {
            const targetType = this.getAttribute('data-type'); // 'ly-do' hoặc 'doi-thu'
            const id = this.getAttribute('data-id');
            const hoatDong = this.checked;
            const labelSpan = this.parentElement.parentElement.querySelector('.crm-status-label');

            const action = (targetType === 'doi-thu') ? 'doi-trang-thai-doi-thu' : 'doi-trang-thai-ly-do';

            // Optimistic update nhãn
            if (labelSpan) {
                labelSpan.textContent = hoatDong ? 'Đang hoạt động' : 'Ngừng hoạt động';
                labelSpan.className = 'crm-status-label ' + (hoatDong ? 'crm-status-active' : 'crm-status-inactive');
            }

            const formData = new URLSearchParams();
            formData.append('action', action);
            formData.append('id', id);
            formData.append('hoatDong', hoatDong ? '1' : '0');

            fetch(contextPath + '/danh-muc/ly-do-thang-thua', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: formData.toString()
            })
            .then(res => res.json())
            .then(data => {
                if (!data.thanhCong) {
                    alert('Lỗi: ' + (data.thongBao || 'Không thể cập nhật trạng thái'));
                    checkbox.checked = !hoatDong; // Rollback
                    if (labelSpan) {
                        labelSpan.textContent = !hoatDong ? 'Đang hoạt động' : 'Ngừng hoạt động';
                        labelSpan.className = 'crm-status-label ' + (!hoatDong ? 'crm-status-active' : 'crm-status-inactive');
                    }
                }
            })
            .catch(err => {
                console.error('AJAX error:', err);
                checkbox.checked = !hoatDong; // Rollback
                if (labelSpan) {
                    labelSpan.textContent = !hoatDong ? 'Đang hoạt động' : 'Ngừng hoạt động';
                    labelSpan.className = 'crm-status-label ' + (!hoatDong ? 'crm-status-active' : 'crm-status-inactive');
                }
            });
        });
    });

    // =========================================================================
    // 7. SPRINT 5 OPPORTUNITY CLOSING SIMULATOR & VALIDATOR (AC3)
    // =========================================================================
    const simRadioThang = document.getElementById('sim-radio-thang');
    const simRadioThua = document.getElementById('sim-radio-thua');
    const simGroupThangReason = document.getElementById('sim-group-thang-reason');
    const simGroupThuaReason = document.getElementById('sim-group-thua-reason');
    const simGroupGiaTri = document.getElementById('sim-group-gia-tri');
    const simGroupNgayKy = document.getElementById('sim-group-ngay-ky');
    const simGroupDoiThu = document.getElementById('sim-group-doi-thu');
    const btnSimulateValidate = document.getElementById('btn-simulate-validate');
    const simResultBox = document.getElementById('sim-result-box');

    function updateSimulatorUI() {
        const isThang = simRadioThang && simRadioThang.checked;

        if (simGroupThangReason) simGroupThangReason.style.display = isThang ? 'flex' : 'none';
        if (simGroupGiaTri) simGroupGiaTri.style.display = isThang ? 'flex' : 'none';
        if (simGroupNgayKy) simGroupNgayKy.style.display = isThang ? 'flex' : 'none';

        if (simGroupThuaReason) simGroupThuaReason.style.display = isThang ? 'none' : 'flex';
        if (simGroupDoiThu) simGroupDoiThu.style.display = isThang ? 'none' : 'flex';

        if (simResultBox) simResultBox.classList.remove('show');
    }

    if (simRadioThang) simRadioThang.addEventListener('change', updateSimulatorUI);
    if (simRadioThua) simRadioThua.addEventListener('change', updateSimulatorUI);

    if (btnSimulateValidate) {
        btnSimulateValidate.addEventListener('click', function () {
            const isThang = simRadioThang && simRadioThang.checked;
            const trangThaiDong = isThang ? 'THANG' : 'THUA';

            const lyDoSelect = isThang ? document.getElementById('sim-select-ly-do-thang') : document.getElementById('sim-select-ly-do-thua');
            const lyDoId = lyDoSelect ? lyDoSelect.value : '';
            const doiThuSelect = document.getElementById('sim-select-doi-thu');
            const doiThuId = (!isThang && doiThuSelect) ? doiThuSelect.value : '';
            const giaTriInput = document.getElementById('sim-input-gia-tri');
            const giaTriChotThucTe = isThang && giaTriInput ? giaTriInput.value : '';
            const ngayKyInput = document.getElementById('sim-input-ngay-ky');
            const ngayKy = isThang && ngayKyInput ? ngayKyInput.value : '';

            const formData = new URLSearchParams();
            formData.append('action', 'kiem-tra-dong-co-hoi');
            formData.append('trangThaiDong', trangThaiDong);
            formData.append('lyDoThangThuaId', lyDoId);
            formData.append('doiThuId', doiThuId);
            formData.append('giaTriChotThucTe', giaTriChotThucTe);
            formData.append('ngayKy', ngayKy);

            fetch(contextPath + '/danh-muc/ly-do-thang-thua', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: formData.toString()
            })
            .then(res => res.json())
            .then(data => {
                if (!simResultBox) return;
                simResultBox.classList.remove('crm-sim-result-success', 'crm-sim-result-error');

                if (data.hopLe) {
                    simResultBox.classList.add('crm-sim-result-success');
                    simResultBox.innerHTML = `
                        <div style="display:flex; align-items:flex-start; gap:10px;">
                            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2.5" style="color:#059669; flex-shrink:0;">
                                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                                <polyline points="22 4 12 14.01 9 11.01"></polyline>
                            </svg>
                            <div>
                                <strong style="font-size:15px; display:block; margin-bottom:4px;">Quy tắc Sprint 5 (S5-05) ĐẠT HỢP LỆ!</strong>
                                <p style="font-size:13px; line-height:1.5;">${data.thongBao}</p>
                            </div>
                        </div>
                    `;
                } else {
                    simResultBox.classList.add('crm-sim-result-error');
                    let listHtml = '';
                    if (data.danhSachLoi && data.danhSachLoi.length > 0) {
                        listHtml = '<ul style="margin-top:8px; padding-left:20px; font-size:13px; line-height:1.5;">' +
                            data.danhSachLoi.map(err => `<li>${err}</li>`).join('') +
                            '</ul>';
                    }
                    simResultBox.innerHTML = `
                        <div style="display:flex; align-items:flex-start; gap:10px;">
                            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2.5" style="color:#dc2626; flex-shrink:0;">
                                <circle cx="12" cy="12" r="10"></circle>
                                <line x1="12" y1="8" x2="12" y2="12"></line>
                                <line x1="12" y1="16" x2="12.01" y2="16"></line>
                            </svg>
                            <div>
                                <strong style="font-size:15px; display:block; margin-bottom:4px;">Chưa đủ điều kiện đóng cơ hội theo Sprint 5!</strong>
                                <p style="font-size:13px; line-height:1.5;">${data.thongBao}</p>
                                ${listHtml}
                            </div>
                        </div>
                    `;
                }
                simResultBox.classList.add('show');
            })
            .catch(err => {
                console.error('Validation error:', err);
                if (simResultBox) {
                    simResultBox.className = 'crm-sim-result-box crm-sim-result-error show';
                    simResultBox.textContent = 'Đã xảy ra lỗi khi kiểm tra quy tắc đóng cơ hội.';
                }
            });
    // Nút nạp nhanh dữ liệu mẫu vào form kiểm thử
    const btnSimulateAutofill = document.getElementById('btn-simulate-autofill');
    if (btnSimulateAutofill) {
        btnSimulateAutofill.addEventListener('click', function () {
            const isThang = simRadioThang && simRadioThang.checked;
            if (isThang) {
                const selectThang = document.getElementById('sim-select-ly-do-thang');
                if (selectThang && selectThang.options.length > 1) {
                    selectThang.selectedIndex = 1;
                }
                const inputGiaTri = document.getElementById('sim-input-gia-tri');
                if (inputGiaTri) inputGiaTri.value = '150000000';

                const inputNgayKy = document.getElementById('sim-input-ngay-ky');
                if (inputNgayKy) {
                    const today = new Date().toISOString().split('T')[0];
                    inputNgayKy.value = today;
                }
            } else {
                const selectThua = document.getElementById('sim-select-ly-do-thua');
                if (selectThua && selectThua.options.length > 1) {
                    selectThua.selectedIndex = 1;
                }
                const selectDoiThu = document.getElementById('sim-select-doi-thu');
                if (selectDoiThu && selectDoiThu.options.length > 1) {
                    selectDoiThu.selectedIndex = 1;
                }
            }

            // Tự động kích hoạt kiểm tra tính hợp lệ
            if (btnSimulateValidate) {
                btnSimulateValidate.click();
            }
        });
    }

    // Khởi tạo trạng thái tab ban đầu
    const initialUrlTab = new URL(window.location.href).searchParams.get('tab');
    if (initialUrlTab) {
        switchTab(initialUrlTab);
    }
});
