/**
 * MODULE: CHI TIẾT LEAD & TIẾP NHẬN / TỪ CHỐI / SLA (Story S4-07)
 * Xử lý tương tác phía Client:
 * - Đóng / mở Modal từ chối Lead (AC2, Q4)
 * - Tự động mở modal khi có lỗi validation từ server
 * - Đếm ký tự thời gian thực cho lý do từ chối (1 - 500 ký tự)
 * - Chống bấm đúp (double-click submit) cho cả Nhận Lead và Từ chối Lead
 * - Quản lý Accessibility (Focus, Escape key, ARIA)
 */

document.addEventListener('DOMContentLoaded', function () {
    initTuChoiModal();
    initCharCounter();
    initFormSubmissions();
});

/**
 * Khởi tạo Modal từ chối Lead
 */
function initTuChoiModal() {
    const btnMoModal = document.getElementById('btnMoModalTuChoi');
    const modalBackdrop = document.getElementById('modalTuChoiLead');
    const btnDongModal = document.getElementById('btnDongModalTuChoi');
    const btnHuyModal = document.getElementById('btnHuyTuChoi');
    const selectLyDo = document.getElementById('maLyDoTuChoi');
    const textareaLyDo = document.getElementById('lyDoTuChoi');

    if (!modalBackdrop) return;

    function moModal() {
        modalBackdrop.classList.add('active');
        modalBackdrop.setAttribute('aria-hidden', 'false');
        document.body.style.overflow = 'hidden';
        if (selectLyDo) {
            setTimeout(() => selectLyDo.focus(), 100);
        }
    }

    function dongModal() {
        modalBackdrop.classList.remove('active');
        modalBackdrop.setAttribute('aria-hidden', 'true');
        document.body.style.overflow = '';
        if (btnMoModal) {
            btnMoModal.focus();
        }
    }

    if (btnMoModal) {
        btnMoModal.addEventListener('click', moModal);
    }

    if (btnDongModal) {
        btnDongModal.addEventListener('click', dongModal);
    }

    if (btnHuyModal) {
        btnHuyModal.addEventListener('click', dongModal);
    }

    // Đóng khi click vào backdrop
    modalBackdrop.addEventListener('click', function (e) {
        if (e.target === modalBackdrop) {
            dongModal();
        }
    });

    // Đóng khi nhấn phím Escape
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && modalBackdrop.classList.contains('active')) {
            dongModal();
        }
    });

    // Tự động mở modal nếu có lỗi validation từ server trả về
    const coLoiServer = document.getElementById('hasServerValidationError');
    if (coLoiServer && coLoiServer.value === 'true') {
        moModal();
    }
}

/**
 * Đếm ký tự trực tiếp cho ô giải thích chi tiết lý do (1-500 ký tự)
 */
function initCharCounter() {
    const textarea = document.getElementById('lyDoTuChoi');
    const counterDisplay = document.getElementById('lyDoCharCount');
    const counterWrap = document.getElementById('charCounterWrap');

    if (!textarea || !counterDisplay) return;

    function capNhatDem() {
        const val = textarea.value || '';
        const count = Array.from(val).length;
        counterDisplay.textContent = count;

        if (counterWrap) {
            if (count >= 500) {
                counterWrap.classList.add('limit-reached');
            } else {
                counterWrap.classList.remove('limit-reached');
            }
        }
    }

    textarea.addEventListener('input', capNhatDem);
    textarea.addEventListener('paste', () => setTimeout(capNhatDem, 10));
    capNhatDem();
}

/**
 * Chống bấm đúp và hiển thị trạng thái loading cho các form
 */
function initFormSubmissions() {
    // 1. Form Nhận Lead
    const formNhanLead = document.getElementById('formNhanLead');
    const btnNhanLead = document.getElementById('btnNhanLead');

    if (formNhanLead && btnNhanLead) {
        formNhanLead.addEventListener('submit', function (e) {
            if (btnNhanLead.disabled) {
                e.preventDefault();
                return;
            }
            btnNhanLead.disabled = true;
            btnNhanLead.innerHTML = '<span class="material-symbols-outlined" style="animation: spin 1s linear infinite;" aria-hidden="true">progress_activity</span><span>Đang xử lý tiếp nhận...</span>';
        });
    }

    // 2. Form Từ Chối Lead
    const formTuChoiLead = document.getElementById('formTuChoiLead');
    const btnXacNhanTuChoi = document.getElementById('btnXacNhanTuChoi');
    const selectLyDo = document.getElementById('maLyDoTuChoi');
    const textareaLyDo = document.getElementById('lyDoTuChoi');

    if (formTuChoiLead && btnXacNhanTuChoi) {
        formTuChoiLead.addEventListener('submit', function (e) {
            // Client-side quick check
            let hopLe = true;

            if (selectLyDo && (!selectLyDo.value || selectLyDo.value.trim() === '')) {
                selectLyDo.classList.add('is-invalid');
                hopLe = false;
            } else if (selectLyDo) {
                selectLyDo.classList.remove('is-invalid');
            }

            if (textareaLyDo) {
                const text = textareaLyDo.value.trim();
                if (text.length === 0 || Array.from(text).length > 500) {
                    textareaLyDo.classList.add('is-invalid');
                    hopLe = false;
                } else {
                    textareaLyDo.classList.remove('is-invalid');
                }
            }

            if (!hopLe) {
                e.preventDefault();
                return;
            }

            if (btnXacNhanTuChoi.disabled) {
                e.preventDefault();
                return;
            }

            btnXacNhanTuChoi.disabled = true;
            btnXacNhanTuChoi.innerHTML = '<span class="material-symbols-outlined" style="animation: spin 1s linear infinite;" aria-hidden="true">progress_activity</span><span>Đang chuyển về hàng chờ...</span>';
        });
    }
}
