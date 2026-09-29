/**
 * JavaScript duy trì phiên đăng nhập và tự động gia hạn khi có hoạt động (Story S1-02).
 * Phục vụ trường hợp người dùng ngồi quán cà phê đang soạn thảo ghi chú cuộc gặp:
 * - Tự động gửi tín hiệu keep-alive khi người dùng gõ phím / tương tác
 * - Tự động lưu bản nháp ghi chú vào LocalStorage phòng ngừa sự cố mạng
 * - Xử lý thông báo và điều hướng an toàn khi phiên thực sự hết hạn
 */
(function () {
    'use strict';

    const KHOANG_CACH_PING_TOI_THIEU_MS = 60 * 1000; // Tối thiểu 1 phút giữa các lần ping khi gõ liên tục
    const CHU_KY_KIEM_TRA_DINH_KY_MS = 3 * 60 * 1000; // 3 phút kiểm tra định kỳ
    const CONTEXT_PATH = window.CONTEXT_PATH || '';
    const API_GIA_HAN = CONTEXT_PATH + '/api/phien/gia-han';
    const LOGIN_URL = CONTEXT_PATH + '/dang-nhap?error=session_expired';

    let lanHoatDongCuoi = Date.now();
    let lanPingCuoi = 0;
    let dangGuiPing = false;

    // Lắng nghe các tương tác của người dùng trên trang
    function ghiNhanHoatDong() {
        lanHoatDongCuoi = Date.now();
        const bayGio = Date.now();

        // Nếu đã quá khoảng cách tối thiểu kể từ lần ping trước thì gửi ping gia hạn
        if (bayGio - lanPingCuoi >= KHOANG_CACH_PING_TOI_THIEU_MS && !dangGuiPing) {
            guiTinHieuGiaHan();
        }
    }

    // Gửi request AJAX gia hạn phiên đến server (AC1)
    function guiTinHieuGiaHan() {
        dangGuiPing = true;
        lanPingCuoi = Date.now();

        const statusEl = document.getElementById('session-sync-status');
        if (statusEl) {
            statusEl.textContent = 'Đang đồng bộ phiên...';
        }

        fetch(API_GIA_HAN, {
            method: 'POST',
            headers: {
                'X-Requested-With': 'XMLHttpRequest',
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
            }
        })
        .then(function (response) {
            if (response.status === 401) {
                // Phiên đã hết hạn (AC3)
                xuLyPhienHetHan();
                return null;
            }
            return response.json();
        })
        .then(function (data) {
            dangGuiPing = false;
            if (data && data.thanhCong) {
                if (statusEl) {
                    statusEl.textContent = 'Phiên hoạt động tốt (đã gia hạn)';
                    statusEl.classList.add('active');
                }
            }
        })
        .catch(function (error) {
            dangGuiPing = false;
            console.warn('Lỗi kết nối khi gia hạn phiên:', error);
            if (statusEl) {
                statusEl.textContent = 'Mất kết nối tạm thời';
                statusEl.classList.remove('active');
            }
        });
    }

    // Xử lý khi phiên hết hạn phía server (AC3)
    function xuLyPhienHetHan() {
        // Tự động lưu bản nháp ghi chú để không mất nội dung khi ở quán cà phê
        const ghiChuTextarea = document.getElementById('ghi-chu-cuoc-gap');
        if (ghiChuTextarea && ghiChuTextarea.value) {
            try {
                localStorage.setItem('crm_ban_nhap_ghi_chu', ghiChuTextarea.value);
            } catch (e) {
                console.error('Không thể lưu localStorage:', e);
            }
        }

        alert('Phiên làm việc của bạn đã hết hạn do không có hoạt động. Hệ thống sẽ chuyển hướng bạn về trang đăng nhập.');
        window.location.href = LOGIN_URL;
    }

    // Gắn sự kiện theo dõi hoạt động
    ['click', 'keydown', 'input', 'mousemove', 'scroll'].forEach(function (eventName) {
        window.addEventListener(eventName, ghiNhanHoatDong, { passive: true });
    });

    // Định kỳ gửi heartbeat nếu trong 3 phút qua có bất kỳ hoạt động nào
    setInterval(function () {
        const bayGio = Date.now();
        if (bayGio - lanHoatDongCuoi < CHU_KY_KIEM_TRA_DINH_KY_MS && !dangGuiPing) {
            guiTinHieuGiaHan();
        }
    }, CHU_KY_KIEM_TRA_DINH_KY_MS);

    // Khôi phục bản nháp ghi chú (nếu có) khi tải trang
    window.addEventListener('DOMContentLoaded', function () {
        const ghiChuTextarea = document.getElementById('ghi-chu-cuoc-gap');
        if (ghiChuTextarea) {
            const savedDraft = localStorage.getItem('crm_ban_nhap_ghi_chu');
            if (savedDraft && !ghiChuTextarea.value) {
                ghiChuTextarea.value = savedDraft;
                const statusEl = document.getElementById('session-sync-status');
                if (statusEl) {
                    statusEl.textContent = 'Đã khôi phục ghi chú nhập dở từ lần trước';
                }
            }

            // Tự động sao lưu bản nháp mỗi khi người dùng gõ
            ghiChuTextarea.addEventListener('input', function () {
                localStorage.setItem('crm_ban_nhap_ghi_chu', ghiChuTextarea.value);
                ghiNhanHoatDong();
            });
        }
    });

    // Public helper để xóa bản nháp khi hoàn thành lưu hoặc đăng xuất
    window.xoaBanNhapGhiChu = function () {
        localStorage.removeItem('crm_ban_nhap_ghi_chu');
    };
})();
