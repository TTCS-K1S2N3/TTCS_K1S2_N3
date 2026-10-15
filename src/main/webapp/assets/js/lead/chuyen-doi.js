/**
 * MODULE: CHUYỂN ĐỔI LEAD THÀNH KHÁCH HÀNG, LIÊN HỆ VÀ CƠ HỘI (Story S4-08)
 * Xử lý tương tác phía Client:
 * - Chống bấm đúp (double-click submit)
 * - Hiển thị trạng thái xử lý giao dịch nguyên tử
 * - Đảm bảo chỉ gửi đúng leadId và csrfToken
 */

document.addEventListener('DOMContentLoaded', function () {
    const formChuyenDoi = document.getElementById('formChuyenDoiLead');
    const btnXacNhan = document.getElementById('btnXacNhanChuyenDoi');

    if (formChuyenDoi && btnXacNhan) {
        formChuyenDoi.addEventListener('submit', function (e) {
            if (btnXacNhan.disabled) {
                e.preventDefault();
                return;
            }

            // Chống gửi trùng lặp nhiều lần (Idempotency bảo vệ phía UI)
            btnXacNhan.disabled = true;
            btnXacNhan.innerHTML = '<span class="material-symbols-outlined" style="animation: spin 1s linear infinite;" aria-hidden="true">progress_activity</span><span>Đang xử lý chuyển đổi...</span>';
        });
    }
});
