/**
 * session-keep-alive.js - Duy trì phiên đăng nhập & Tự động gia hạn khi có hoạt động (Story S1-02)
 * 
 * Nghiệp vụ cốt lõi:
 * 1. AC1: Phiên được gia hạn tự động khi còn hoạt động (người dùng đang gõ ghi chú cuộc gặp tại quán cà phê).
 * 2. AC2: Đăng xuất làm mất hiệu lực phiên ngay lập tức phía server.
 * 3. AC3: Phiên hết hạn đưa về trang đăng nhập kèm thông báo rõ ràng (kèm bảo lưu bản nháp an toàn).
 */
(function () {
    'use strict';

    const KHOANG_CACH_PING_TOI_THIEU_MS = 45 * 1000; // Tối thiểu 45 giây giữa các lần ping khi gõ liên tục
    const CHU_KY_KIEM_TRA_DINH_KY_MS = 2 * 60 * 1000; // 2 phút kiểm tra định kỳ
    const CONTEXT_PATH = window.CONTEXT_PATH || '';
    const API_GIA_HAN = CONTEXT_PATH + '/api/phien/gia-han';
    const LOGIN_URL = CONTEXT_PATH + '/dang-nhap?error=session_expired';
    const DRAFT_STORAGE_KEY = 'crm_ban_nhap_ghi_chu';
    const DRAFT_TIME_KEY = 'crm_ban_nhap_thoi_gian';

    let lanHoatDongCuoi = Date.now();
    let lanPingCuoi = 0;
    let dangGuiPing = false;
    let soGiayConLai = 30 * 60; // Mặc định 30 phút (1800 giây)
    let countdownIntervalId = null;

    // Cập nhật trạng thái hiển thị trên giao diện
    function capNhatTrangThaiUI(trangThai, thongDiep, loai) {
        const statusEl = document.getElementById('session-sync-status');
        const statusDot = document.getElementById('session-status-dot');
        const statusText = document.getElementById('session-status-text');

        if (statusEl) {
            statusEl.textContent = thongDiep;
            statusEl.className = 'sync-status ' + (loai || '');
        }

        if (statusDot) {
            statusDot.className = 'session-dot ' + (loai || 'active');
        }

        if (statusText) {
            statusText.textContent = trangThai;
        }
    }

    // Định dạng giây thành mm:ss
    function dinhDangThoiGian(tongSoGiay) {
        if (tongSoGiay <= 0) return '00:00';
        const phut = Math.floor(tongSoGiay / 60);
        const giay = tongSoGiay % 60;
        return (phut < 10 ? '0' : '') + phut + ':' + (giay < 10 ? '0' : '') + giay;
    }

    // Bắt đầu đếm ngược thời gian phiên còn lại
    function khoiTaoCountdown() {
        if (countdownIntervalId) {
            clearInterval(countdownIntervalId);
        }

        const countdownEl = document.getElementById('session-countdown-timer');
        if (!countdownEl) return;

        countdownIntervalId = setInterval(function () {
            soGiayConLai--;
            countdownEl.textContent = dinhDangThoiGian(soGiayConLai);

            // Cảnh báo khi còn dưới 3 phút
            if (soGiayConLai <= 180 && soGiayConLai > 0) {
                capNhatTrangThaiUI('Phiên sắp hết hạn', 'Phiên sắp hết hạn trong ' + dinhDangThoiGian(soGiayConLai), 'warning');
            }

            // Phiên hết hạn phía client
            if (soGiayConLai <= 0) {
                clearInterval(countdownIntervalId);
                xuLyPhienHetHan();
            }
        }, 1000);
    }

    // Lắng nghe tương tác của người dùng trên toàn trang (AC1)
    function ghiNhanHoatDong() {
        lanHoatDongCuoi = Date.now();
        const bayGio = Date.now();

        // Nếu đã vượt quá khoảng cách tối thiểu kể từ lần ping trước thì gửi ping gia hạn
        if (bayGio - lanPingCuoi >= KHOANG_CACH_PING_TOI_THIEU_MS && !dangGuiPing) {
            guiTinHieuGiaHan();
        }
    }

    // Gửi request AJAX gia hạn phiên đến server (AC1)
    function guiTinHieuGiaHan() {
        dangGuiPing = true;
        lanPingCuoi = Date.now();

        capNhatTrangThaiUI('Đang đồng bộ...', 'Đang gia hạn phiên làm việc...', 'syncing');

        fetch(API_GIA_HAN, {
            method: 'POST',
            headers: {
                'X-Requested-With': 'XMLHttpRequest',
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
            }
        })
        .then(function (response) {
            if (response.status === 401) {
                // Phiên đã hết hạn phía server (AC3)
                xuLyPhienHetHan();
                return null;
            }
            return response.json();
        })
        .then(function (data) {
            dangGuiPing = false;
            if (data && data.thanhCong) {
                if (data.thoiGianConLai) {
                    soGiayConLai = data.thoiGianConLai;
                } else {
                    soGiayConLai = 30 * 60;
                }

                const bayGioStr = new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
                capNhatTrangThaiUI('Phiên đang hoạt động', 'Đã tự động gia hạn phiên lúc ' + bayGioStr, 'active');
            }
        })
        .catch(function (error) {
            dangGuiPing = false;
            console.warn('Lỗi kết nối khi gia hạn phiên:', error);
            capNhatTrangThaiUI('Kết nối chập chờn', 'Không thể kết nối server, bản nháp vẫn được lưu an toàn', 'warning');
        });
    }

    // Xử lý khi phiên hết hạn phía server (AC3)
    function xuLyPhienHetHan() {
        // Tự động sao lưu bản nháp ghi chú để không mất nội dung khi ở quán cà phê
        const ghiChuTextarea = document.getElementById('ghi-chu-cuoc-gap');
        if (ghiChuTextarea && ghiChuTextarea.value.trim()) {
            try {
                localStorage.setItem(DRAFT_STORAGE_KEY, ghiChuTextarea.value);
                localStorage.setItem(DRAFT_TIME_KEY, new Date().toISOString());
            } catch (e) {
                console.error('Không thể lưu localStorage:', e);
            }
        }

        // Chuyển hướng ngay về trang đăng nhập với thông báo phiên hết hạn (AC3)
        window.location.href = LOGIN_URL;
    }

    // Đếm số từ và số ký tự của ghi chú
    function capNhatBoDem(text) {
        const countWordsEl = document.getElementById('note-word-count');
        const countCharsEl = document.getElementById('note-char-count');
        if (!countWordsEl && !countCharsEl) return;

        const charCount = text.length;
        const words = text.trim() ? text.trim().split(/\s+/).length : 0;

        if (countWordsEl) countWordsEl.textContent = words + ' từ';
        if (countCharsEl) countCharsEl.textContent = charCount + ' ký tự';
    }

    // Lưu bản nháp vào LocalStorage
    function luuBanNhap(noiDung) {
        try {
            localStorage.setItem(DRAFT_STORAGE_KEY, noiDung);
            const now = new Date();
            localStorage.setItem(DRAFT_TIME_KEY, now.toISOString());
            
            const lastSavedEl = document.getElementById('note-last-saved');
            if (lastSavedEl) {
                const timeStr = now.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
                lastSavedEl.textContent = 'Đã lưu nháp: ' + timeStr;
            }
        } catch (e) {
            console.error('Lỗi lưu LocalStorage:', e);
        }
    }

    // Gắn các sự kiện hoạt động người dùng
    ['click', 'keydown', 'input', 'mousemove', 'scroll', 'touchstart'].forEach(function (eventName) {
        window.addEventListener(eventName, ghiNhanHoatDong, { passive: true });
    });

    // Định kỳ gửi heartbeat nếu có bất kỳ hoạt động nào trong 2 phút qua
    setInterval(function () {
        const bayGio = Date.now();
        if (bayGio - lanHoatDongCuoi < CHU_KY_KIEM_TRA_DINH_KY_MS && !dangGuiPing) {
            guiTinHieuGiaHan();
        }
    }, CHU_KY_KIEM_TRA_DINH_KY_MS);

    // Khởi tạo trang khi DOM sẵn sàng
    window.addEventListener('DOMContentLoaded', function () {
        khoiTaoCountdown();

        const ghiChuTextarea = document.getElementById('ghi-chu-cuoc-gap');
        const draftAlertBanner = document.getElementById('draft-alert-banner');
        const btnRestoreDraft = document.getElementById('btn-restore-draft');
        const btnDiscardDraft = document.getElementById('btn-discard-draft');
        const btnManualSave = document.getElementById('btn-manual-save');
        const btnManualSync = document.getElementById('btn-manual-sync');

        if (ghiChuTextarea) {
            // Kiểm tra xem có bản nháp từ phiên trước không
            const savedDraft = localStorage.getItem(DRAFT_STORAGE_KEY);
            const savedTime = localStorage.getItem(DRAFT_TIME_KEY);

            if (savedDraft && savedDraft.trim() && !ghiChuTextarea.value.trim()) {
                if (draftAlertBanner) {
                    draftAlertBanner.style.display = 'flex';
                    const draftTimeText = document.getElementById('draft-saved-time-text');
                    if (draftTimeText && savedTime) {
                        try {
                            const dateObj = new Date(savedTime);
                            draftTimeText.textContent = dateObj.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) + ' ' + dateObj.toLocaleDateString('vi-VN');
                        } catch (e) {
                            draftTimeText.textContent = 'lần làm việc trước';
                        }
                    }
                } else {
                    // Nếu không có banner thì khôi phục trực tiếp
                    ghiChuTextarea.value = savedDraft;
                    capNhatBoDem(savedDraft);
                }
            }

            // Tự động sao lưu và gia hạn phiên mỗi khi người dùng gõ
            ghiChuTextarea.addEventListener('input', function () {
                luuBanNhap(ghiChuTextarea.value);
                capNhatBoDem(ghiChuTextarea.value);
                ghiNhanHoatDong();
            });

            // Nút khôi phục bản nháp
            if (btnRestoreDraft) {
                btnRestoreDraft.addEventListener('click', function () {
                    const draft = localStorage.getItem(DRAFT_STORAGE_KEY);
                    if (draft) {
                        ghiChuTextarea.value = draft;
                        capNhatBoDem(draft);
                        if (draftAlertBanner) draftAlertBanner.style.display = 'none';
                        luuBanNhap(draft);
                        capNhatTrangThaiUI('Phiên đang hoạt động', 'Đã khôi phục ghi chú thành công!', 'active');
                    }
                });
            }

            // Nút hủy bản nháp cũ
            if (btnDiscardDraft) {
                btnDiscardDraft.addEventListener('click', function () {
                    localStorage.removeItem(DRAFT_STORAGE_KEY);
                    localStorage.removeItem(DRAFT_TIME_KEY);
                    if (draftAlertBanner) draftAlertBanner.style.display = 'none';
                });
            }

            // Nút Lưu nháp thủ công
            if (btnManualSave) {
                btnManualSave.addEventListener('click', function () {
                    luuBanNhap(ghiChuTextarea.value);
                    guiTinHieuGiaHan();
                });
            }

            // Nút Đồng bộ phiên thủ công
            if (btnManualSync) {
                btnManualSync.addEventListener('click', function () {
                    guiTinHieuGiaHan();
                });
            }
        }

        // Xử lý nút Đăng xuất an toàn (AC2)
        const logoutForm = document.getElementById('form-logout');
        if (logoutForm) {
            logoutForm.addEventListener('submit', function (e) {
                // Kiểm tra nếu có nội dung ghi chú chưa xóa
                const draftContent = ghiChuTextarea ? ghiChuTextarea.value.trim() : '';
                if (draftContent.length > 0) {
                    const xacNhan = confirm('Bạn đang có ghi chú cuộc gặp chưa xóa. Bạn có chắc chắn muốn đăng xuất an toàn không? (Bản nháp vẫn được lưu trên máy)');
                    if (!xacNhan) {
                        e.preventDefault();
                    }
                }
            });
        }
    });

    // Helper toàn cục để xóa bản nháp khi cần
    window.xoaBanNhapGhiChu = function () {
        localStorage.removeItem(DRAFT_STORAGE_KEY);
        localStorage.removeItem(DRAFT_TIME_KEY);
    };
})();
