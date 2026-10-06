/**
 * CRM BÁN HÀNG - XỬ LÝ KHOÁ TÀI KHOẢN & BÀN GIAO DỮ LIỆU (STORY S1-10 - FE)
 * Tác giả: Ngô Trung Kiên (FE)
 * Tiêu chuẩn nghiệm thu & Tính năng:
 * - AC1: Tài khoản bị khoá không đăng nhập được và bị thu hồi phiên đang mở
 * - AC2: Bắt buộc chọn người tiếp nhận toàn bộ khách hàng và cơ hội trước khi khoá
 * - AC3: Việc bàn giao được ghi nhật ký, dữ liệu không bị mất chủ sở hữu
 * - UX: Modal xác nhận an ninh chuyên nghiệp, bộ trực quan hóa luồng bàn giao (Transfer Flow Visualizer),
 *       gợi ý lý do nhanh (Quick Reason Chips), tìm kiếm nhật ký và tối ưu chạm cho 360px.
 */

document.addEventListener('DOMContentLoaded', function () {
    // 1. DOM Elements
    const form = document.getElementById('formKhoaTaiKhoan');
    const selectNguoiNhan = document.getElementById('nguoiTiepNhanId');
    const textareaLyDo = document.getElementById('lyDo');
    const btnOpenModal = document.getElementById('btnXacNhanKhoa');
    const reasonChips = document.querySelectorAll('.crm-reason-chip');
    const transferFlowCard = document.getElementById('transfer-flow-card');
    const flowReceiverName = document.getElementById('flow-receiver-name');
    const receiverHelperText = document.getElementById('receiver-helper-text');

    // Modal Elements
    const modal = document.getElementById('modalXacNhanKhoa');
    const modalBackdrop = document.getElementById('modalBackdrop');
    const modalBtnCancel = document.getElementById('modalBtnCancel');
    const modalBtnClose = document.getElementById('modalBtnClose');
    const modalBtnConfirm = document.getElementById('modalBtnConfirm');
    const modalReceiverName = document.getElementById('modalReceiverName');
    const chkConfirmCommitment = document.getElementById('chkConfirmCommitment');

    // History Table Search
    const searchHistoryInput = document.getElementById('search-history-input');
    const historyRows = document.querySelectorAll('.history-table-row');
    const historyCountBadge = document.getElementById('history-count-badge');
    const historyEmptySearch = document.getElementById('history-empty-search');

    // ====================================================================
    // A. TOAST NOTIFICATION UTILITY
    // ====================================================================
    let toastContainer = document.getElementById('crm-toast-container');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.id = 'crm-toast-container';
        toastContainer.className = 'crm-toast-container';
        document.body.appendChild(toastContainer);
    }

    function showToast(type, message, duration = 4500) {
        const toast = document.createElement('div');
        toast.className = `crm-toast crm-toast-${type}`;

        let iconSvg = '';
        if (type === 'success') {
            iconSvg = '<span class="material-symbols-outlined crm-toast-icon" aria-hidden="true">check_circle</span>';
        } else if (type === 'warning') {
            iconSvg = '<span class="material-symbols-outlined crm-toast-icon" aria-hidden="true">warning</span>';
        } else {
            iconSvg = '<span class="material-symbols-outlined crm-toast-icon" aria-hidden="true">error</span>';
        }

        toast.innerHTML = `
            ${iconSvg}
            <div style="flex: 1; line-height: 1.4;">${message}</div>
            <button type="button" class="crm-toast-close" aria-label="Đóng"><span class="material-symbols-outlined" style="font-size: 18px;" aria-hidden="true">close</span></button>
        `;

        const closeBtn = toast.querySelector('.crm-toast-close');
        closeBtn.addEventListener('click', function () {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 250);
        });

        toastContainer.appendChild(toast);
        requestAnimationFrame(() => toast.classList.add('show'));

        setTimeout(() => {
            if (toast.parentElement) {
                toast.classList.remove('show');
                setTimeout(() => toast.remove(), 250);
            }
        }, duration);
    }

    // ====================================================================
    // B. AC 2: BẮT BUỘC CHỌN NGƯỜI TIẾP NHẬN & MÔ PHỎNG LUỒNG CHUYỂN GIAO
    // ====================================================================
    function capNhatLuongChuyenGiao() {
        if (!selectNguoiNhan) return;

        const val = selectNguoiNhan.value;
        if (val && val > 0) {
            const selectedText = selectNguoiNhan.options[selectNguoiNhan.selectedIndex].text;
            selectNguoiNhan.classList.remove('input-error');
            selectNguoiNhan.classList.add('input-success');

            if (flowReceiverName) {
                flowReceiverName.textContent = selectedText;
            }
            if (transferFlowCard) {
                transferFlowCard.style.display = 'flex';
            }
            if (receiverHelperText) {
                receiverHelperText.className = 'form-help valid';
                receiverHelperText.innerHTML = `<strong><span class="material-symbols-outlined icon-sm" style="vertical-align: -2px;" aria-hidden="true">check</span> Đã chọn:</strong> Dữ liệu sẽ được chuyển giao sang <em>${selectedText}</em>.`;
            }
        } else {
            selectNguoiNhan.classList.remove('input-success');
            if (transferFlowCard) {
                transferFlowCard.style.display = 'none';
            }
            if (receiverHelperText) {
                receiverHelperText.className = 'form-help';
                receiverHelperText.innerHTML = 'Bắt buộc chọn người tiếp nhận trước khi khoá (AC 2).';
            }
        }
    }

    if (selectNguoiNhan) {
        selectNguoiNhan.addEventListener('change', capNhatLuongChuyenGiao);
        capNhatLuongChuyenGiao();
    }

    // ====================================================================
    // C. GỢI Ý LÝ DO NHANH (QUICK REASON CHIPS)
    // ====================================================================
    if (reasonChips.length > 0 && textareaLyDo) {
        reasonChips.forEach(function (chip) {
            chip.addEventListener('click', function () {
                const text = this.getAttribute('data-reason');
                if (text) {
                    if (textareaLyDo.value && !textareaLyDo.value.includes(text)) {
                        textareaLyDo.value = textareaLyDo.value + '; ' + text;
                    } else {
                        textareaLyDo.value = text;
                    }
                    textareaLyDo.focus();
                    showToast('success', 'Đã thêm lý do bàn giao vào ghi chú.');
                }
            });
        });
    }

    // ====================================================================
    // D. MODAL XÁC NHẬN AN NINH (CUSTOM SECURITY CONFIRMATION DIALOG)
    // ====================================================================
    function openConfirmModal() {
        if (!selectNguoiNhan || !selectNguoiNhan.value) {
            if (selectNguoiNhan) {
                selectNguoiNhan.classList.add('input-error', 'shake');
                setTimeout(() => selectNguoiNhan.classList.remove('shake'), 400);
                selectNguoiNhan.focus();
            }
            showToast('error', '<strong>Tiêu chuẩn nghiệm thu S1-10:</strong> Bắt buộc phải chọn người tiếp nhận toàn bộ khách hàng và cơ hội trước khi khoá tài khoản!');
            return;
        }

        const selectedText = selectNguoiNhan.options[selectNguoiNhan.selectedIndex].text;
        if (modalReceiverName) {
            modalReceiverName.textContent = selectedText;
        }

        if (chkConfirmCommitment) {
            chkConfirmCommitment.checked = false;
        }
        if (modalBtnConfirm) {
            modalBtnConfirm.disabled = true;
        }

        if (modal) {
            modal.classList.add('show');
            document.body.style.overflow = 'hidden'; // Ngăn cuộn trang ngầm
        }
    }

    function closeConfirmModal() {
        if (modal) {
            modal.classList.remove('show');
            document.body.style.overflow = '';
        }
    }

    if (btnOpenModal) {
        btnOpenModal.addEventListener('click', function (e) {
            e.preventDefault();
            openConfirmModal();
        });
    }

    if (modalBtnCancel) modalBtnCancel.addEventListener('click', closeConfirmModal);
    if (modalBtnClose) modalBtnClose.addEventListener('click', closeConfirmModal);
    if (modalBackdrop) modalBackdrop.addEventListener('click', closeConfirmModal);

    // Bật nút xác nhận khoá khi checkbox cam kết được tích
    if (chkConfirmCommitment && modalBtnConfirm) {
        chkConfirmCommitment.addEventListener('change', function () {
            modalBtnConfirm.disabled = !this.checked;
        });
    }

    // Xác nhận và thực hiện submit
    if (modalBtnConfirm && form) {
        modalBtnConfirm.addEventListener('click', function () {
            modalBtnConfirm.disabled = true;
            modalBtnConfirm.innerHTML = '<span class="crm-spinner"></span> Đang thực hiện bàn giao...';
            if (btnOpenModal) {
                btnOpenModal.disabled = true;
                btnOpenModal.innerHTML = '<span class="crm-spinner"></span> Đang xử lý...';
            }
            form.submit();
        });
    }

    // Đóng modal bằng phím ESC
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && modal && modal.classList.contains('show')) {
            closeConfirmModal();
        }
    });

    // ====================================================================
    // E. AC 3: TÌM KIẾM NHANH TRONG NHẬT KÝ BÀN GIAO (AUDIT LOG FILTER)
    // ====================================================================
    if (searchHistoryInput && historyRows.length > 0) {
        searchHistoryInput.addEventListener('input', function () {
            const query = this.value.trim().toLowerCase();
            let count = 0;

            historyRows.forEach(function (row) {
                const text = row.textContent.toLowerCase();
                if (!query || text.includes(query)) {
                    row.style.display = '';
                    count++;
                } else {
                    row.style.display = 'none';
                }
            });

            if (historyCountBadge) {
                historyCountBadge.textContent = `${count} bản ghi`;
            }

            if (historyEmptySearch) {
                historyEmptySearch.style.display = count === 0 ? 'table-row' : 'none';
            }
        });
    }
});
