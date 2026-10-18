/**
 * ==========================================================================
 * JAVASCRIPT XỬ LÝ GIAO DIỆN LỌC DANH SÁCH LEAD (STORY S4-09)
 * - AC1: Lọc theo 5 tiêu chí (AND), validate ngày từ - đến
 * - AC2: Cảnh báo và highlight Lead quá hạn SLA
 * - AC3: Lưu bộ lọc (Modal, validation, preview điều kiện, chống double-click)
 * - Navigation: Dropdown nghiệp vụ Lead (S4-01 đến S4-06)
 * ==========================================================================
 */

/**
 * Hàm toggle bộ lọc nâng cao (chuẩn hóa giống trang Khách hàng)
 */
window.toggleAdvancedFilter = function (forceOpen) {
    const advancedFilterPanel = document.getElementById('advancedFilterPanel');
    const btnToggleAdvancedFilter = document.getElementById('btnToggleAdvancedFilter');
    const iconFilterArrow = document.getElementById('iconFilterArrow');
    if (!advancedFilterPanel) return;

    const isOpen = advancedFilterPanel.classList.contains('is-open') ||
        (advancedFilterPanel.style.display !== 'none' && advancedFilterPanel.style.display !== '');
    const willOpen = typeof forceOpen === 'boolean' ? forceOpen : !isOpen;

    if (willOpen) {
        advancedFilterPanel.classList.add('is-open');
        advancedFilterPanel.style.display = 'block';
        advancedFilterPanel.setAttribute('aria-hidden', 'false');
        if (btnToggleAdvancedFilter) {
            btnToggleAdvancedFilter.setAttribute('aria-expanded', 'true');
            btnToggleAdvancedFilter.classList.add('is-active');
        }
        if (iconFilterArrow) {
            iconFilterArrow.textContent = 'expand_less';
        }
        // Focus vào input/select đầu tiên
        const firstInput = advancedFilterPanel.querySelector('select, input');
        if (firstInput) {
            setTimeout(function () { firstInput.focus(); }, 100);
        }
    } else {
        advancedFilterPanel.classList.remove('is-open');
        advancedFilterPanel.style.display = 'none';
        advancedFilterPanel.setAttribute('aria-hidden', 'true');
        if (btnToggleAdvancedFilter) {
            btnToggleAdvancedFilter.setAttribute('aria-expanded', 'false');
            const badge = document.getElementById('filterCountBadge');
            if (!badge) {
                btnToggleAdvancedFilter.classList.remove('is-active');
            }
        }
        if (iconFilterArrow) {
            iconFilterArrow.textContent = 'expand_more';
        }
    }
};

document.addEventListener('DOMContentLoaded', function () {
    initAdvancedFilterToggle();
    initNghiepVuDropdown();
    initFilterForm();
    initActiveFilterChips();
    initSavedFiltersModal();
    initQuickMorningFilters();
});

/**
 * 0. Khởi tạo Toggle Bộ Lọc Nâng Cao
 */
function initAdvancedFilterToggle() {
    const btnToggle = document.getElementById('btnToggleAdvancedFilter');
    const btnDong = document.getElementById('btnDongFilterPanel');
    const btnDongBottom = document.getElementById('btnDongFilterPanelBottom');

    if (btnToggle) {
        btnToggle.addEventListener('click', function (e) {
            e.preventDefault();
            e.stopPropagation();
            window.toggleAdvancedFilter();
        });
    }

    if (btnDong) {
        btnDong.addEventListener('click', function (e) {
            e.preventDefault();
            window.toggleAdvancedFilter(false);
            if (btnToggle) btnToggle.focus();
        });
    }

    if (btnDongBottom) {
        btnDongBottom.addEventListener('click', function (e) {
            e.preventDefault();
            window.toggleAdvancedFilter(false);
            if (btnToggle) btnToggle.focus();
        });
    }
}

/**
 * 0.1 Khởi tạo Dropdown Nghiệp Vụ Lead
 */
function initNghiepVuDropdown() {
    const wrap = document.getElementById('dropdownNghiepVuWrap');
    const btnToggle = document.getElementById('btnToggleNghiepVu');
    const menu = document.getElementById('menuNghiepVuLead');
    const arrowIcon = document.getElementById('iconDropdownArrow');

    if (!btnToggle || !menu) return;

    function openMenu() {
        menu.style.display = 'block';
        btnToggle.setAttribute('aria-expanded', 'true');
        if (arrowIcon) arrowIcon.textContent = 'expand_less';
    }

    function closeMenu() {
        menu.style.display = 'none';
        btnToggle.setAttribute('aria-expanded', 'false');
        if (arrowIcon) arrowIcon.textContent = 'expand_more';
    }

    btnToggle.addEventListener('click', function (e) {
        e.preventDefault();
        e.stopPropagation();
        if (menu.style.display === 'block') {
            closeMenu();
        } else {
            openMenu();
        }
    });

    // Đóng khi click ngoài dropdown
    document.addEventListener('click', function (e) {
        if (wrap && !wrap.contains(e.target)) {
            closeMenu();
        }
    });

    // Đóng khi ấn Escape
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && menu.style.display === 'block') {
            closeMenu();
            btnToggle.focus();
        }
    });
}

/**
 * 1. Khởi tạo và xử lý Form Bộ Lọc
 */
function initFilterForm() {
    const filterForm = document.getElementById('formLocLead');
    const inputTuNgay = document.getElementById('filterTuNgay');
    const inputDenNgay = document.getElementById('filterDenNgay');
    const btnResetFilter = document.getElementById('btnResetFilter');

    if (!filterForm) return;

    // Validate khoảng thời gian: Từ ngày <= Đến ngày
    if (inputTuNgay && inputDenNgay) {
        inputTuNgay.addEventListener('change', function () {
            if (inputDenNgay.value && inputTuNgay.value > inputDenNgay.value) {
                inputDenNgay.setCustomValidity('Đến ngày không được nhỏ hơn Từ ngày');
            } else {
                inputDenNgay.setCustomValidity('');
            }
        });

        inputDenNgay.addEventListener('change', function () {
            if (inputTuNgay.value && inputDenNgay.value < inputTuNgay.value) {
                inputDenNgay.setCustomValidity('Đến ngày không được nhỏ hơn Từ ngày');
            } else {
                inputDenNgay.setCustomValidity('');
            }
        });
    }

    // Xóa bộ lọc (Reset form về mặc định)
    if (btnResetFilter) {
        btnResetFilter.addEventListener('click', function (e) {
            e.preventDefault();
            const url = new URL(window.location.href);
            window.location.href = url.pathname;
        });
    }
}

/**
 * 1.1 Khởi tạo xử lý gỡ chip điều kiện lọc
 */
function initActiveFilterChips() {
    const removeBtns = document.querySelectorAll('.filter-chip-remove');
    const formLocLead = document.getElementById('formLocLead');

    removeBtns.forEach(function (btn) {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            const fieldId = btn.getAttribute('data-field');
            if (fieldId) {
                const targetEl = document.getElementById(fieldId);
                if (targetEl) {
                    targetEl.value = '';
                }
            }
            if (formLocLead) {
                formLocLead.submit();
            }
        });
    });
}

/**
 * 2. Khởi tạo Modal Lưu Bộ Lọc (AC3)
 */
function initSavedFiltersModal() {
    const btnOpenModal = document.getElementById('btnOpenModalLuuBoLoc');
    const modalBackdrop = document.getElementById('modalLuuBoLoc');
    const btnCloseModal = document.getElementById('btnCloseModalLuuBoLoc');
    const btnCancelModal = document.getElementById('btnCancelModalLuuBoLoc');
    const inputTenBoLoc = document.getElementById('modalTenBoLoc');
    const charCountEl = document.getElementById('modalCharCount');
    const formLuuBoLoc = document.getElementById('formLuuBoLoc');
    const btnSubmitLuu = document.getElementById('btnSubmitLuuBoLoc');
    const previewContainer = document.getElementById('modalFilterPreviewTags');

    if (!modalBackdrop) return;

    function openModal() {
        renderFilterPreviewTags(previewContainer);
        modalBackdrop.style.display = 'flex';
        modalBackdrop.classList.add('is-open');
        modalBackdrop.setAttribute('aria-hidden', 'false');
        if (inputTenBoLoc) {
            setTimeout(function () {
                inputTenBoLoc.focus();
            }, 100);
        }
    }

    function closeModal() {
        modalBackdrop.style.display = 'none';
        modalBackdrop.classList.remove('is-open');
        modalBackdrop.setAttribute('aria-hidden', 'true');
        if (inputTenBoLoc) {
            inputTenBoLoc.setCustomValidity('');
        }
    }

    const openTriggers = document.querySelectorAll('#btnOpenModalLuuBoLoc, #btnOpenModalLuuBoLocFilter, .btn-save-filter-action, .lead-btn-save-filter-trigger');
    openTriggers.forEach(btn => btn.addEventListener('click', openModal));

    if (btnCloseModal) {
        btnCloseModal.addEventListener('click', closeModal);
    }

    if (btnCancelModal) {
        btnCancelModal.addEventListener('click', closeModal);
    }

    // Đóng khi click ra ngoài backdrop
    modalBackdrop.addEventListener('click', function (e) {
        if (e.target === modalBackdrop) {
            closeModal();
        }
    });

    // Đóng khi bấm phím Escape
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && (modalBackdrop.classList.contains('is-open') || modalBackdrop.style.display === 'flex')) {
            closeModal();
        }
    });

    // Đếm số ký tự và validate độ dài (1-80 ký tự)
    if (inputTenBoLoc && charCountEl) {
        inputTenBoLoc.addEventListener('input', function () {
            const val = inputTenBoLoc.value;
            charCountEl.textContent = val.length + '/80 ký tự';
            if (val.trim().length === 0) {
                inputTenBoLoc.setCustomValidity('Vui lòng nhập tên bộ lọc');
            } else if (val.length > 80) {
                inputTenBoLoc.setCustomValidity('Tên bộ lọc không được vượt quá 80 ký tự');
            } else {
                inputTenBoLoc.setCustomValidity('');
            }
        });
    }

    // Submit form lưu bộ lọc kèm chống double-click
    if (formLuuBoLoc && btnSubmitLuu) {
        formLuuBoLoc.addEventListener('submit', function (e) {
            const val = inputTenBoLoc ? inputTenBoLoc.value.trim() : '';
            if (!val) {
                e.preventDefault();
                inputTenBoLoc.focus();
                inputTenBoLoc.setCustomValidity('Vui lòng nhập tên bộ lọc');
                return;
            }

            // Đồng bộ các tiêu chí hiện tại vào form lưu
            syncCurrentFiltersToModalForm(formLuuBoLoc);

            // Chống spam double-click
            btnSubmitLuu.disabled = true;
            btnSubmitLuu.innerHTML = '<span class="material-symbols-outlined" style="animation: spin 1s infinite linear;">sync</span> Đang lưu...';
        });
    }
}

/**
 * Hiển thị tóm tắt các điều kiện lọc đang chọn trong Modal Preview
 */
function renderFilterPreviewTags(container) {
    if (!container) return;
    container.innerHTML = '';

    const filterForm = document.getElementById('formLocLead');
    if (!filterForm) return;

    const fields = [
        { id: 'filterTrangThai', label: 'Trạng thái' },
        { id: 'filterNguonLead', label: 'Nguồn' },
        { id: 'filterPhanLoai', label: 'Phân loại' },
        { id: 'filterNguoiPhuTrach', label: 'Người phụ trách' },
        { id: 'filterTuNgay', label: 'Từ ngày' },
        { id: 'filterDenNgay', label: 'Đến ngày' }
    ];

    let hasCriteria = false;

    fields.forEach(function (f) {
        const el = document.getElementById(f.id);
        if (!el || !el.value || el.value.trim() === '') return;

        let displayText = el.value;
        if (el.tagName === 'SELECT') {
            const selectedOpt = el.options[el.selectedIndex];
            if (selectedOpt && selectedOpt.text) {
                displayText = selectedOpt.text;
            }
        }

        const tag = document.createElement('span');
        tag.className = 'lead-modal-preview-tag';
        tag.textContent = f.label + ': ' + displayText;
        container.appendChild(tag);
        hasCriteria = true;
    });

    if (!hasCriteria) {
        const emptyTag = document.createElement('span');
        emptyTag.className = 'lead-modal-preview-tag';
        emptyTag.style.color = '#94a3b8';
        emptyTag.textContent = 'Mặc định (Không có điều kiện lọc nào)';
        container.appendChild(emptyTag);
    }
}

/**
 * Sao chép các tham số lọc hiện tại vào hidden inputs của form modal trước khi POST
 */
function syncCurrentFiltersToModalForm(modalForm) {
    const filterForm = document.getElementById('formLocLead');
    if (!filterForm) return;

    const filterInputs = filterForm.querySelectorAll('input, select');
    filterInputs.forEach(function (input) {
        if (!input.name || input.name === 'tenBoLoc') return;
        if (input.value && input.value.trim() !== '') {
            let hidden = modalForm.querySelector('input[type="hidden"][name="' + input.name + '"]');
            if (!hidden) {
                hidden = document.createElement('input');
                hidden.type = 'hidden';
                hidden.name = input.name;
                modalForm.appendChild(hidden);
            }
            hidden.value = input.value;
        }
    });
}

/**
 * 3. Thao tác nhanh cho Sales Rep mở máy buổi sáng
 */
function initQuickMorningFilters() {
    // Click vào KPI SLA Quá hạn -> Lọc nhanh danh sách quá hạn hoặc cuộn tới vị trí đầu
    const cardSlaAlert = document.getElementById('kpiCardSlaAlert');
    if (cardSlaAlert) {
        cardSlaAlert.addEventListener('click', function (e) {
            // Nếu có các hàng quá SLA trên trang, cuộn tới hàng đầu tiên
            const firstOverdueRow = document.querySelector('.row-sla-overdue');
            if (firstOverdueRow) {
                e.preventDefault();
                firstOverdueRow.scrollIntoView({ behavior: 'smooth', block: 'center' });
                firstOverdueRow.style.transition = 'outline 0.3s ease';
                firstOverdueRow.style.outline = '3px solid #dc2626';
                setTimeout(function () {
                    firstOverdueRow.style.outline = '';
                }, 2000);
            }
        });
    }
}
