/**
 * MODULE: CẢNH BÁO VÀ GỘP LEAD TRÙNG (STORY S4-04)
 * Xử lý JavaScript tương tác giao diện người dùng:
 * - Xác nhận trước khi gắn Khách hàng (AC2)
 * - Xác nhận và chống submit form gộp nhiều lần (AC3)
 * - Tự động ẩn thông báo flash message sau 5 giây
 */

document.addEventListener('DOMContentLoaded', function () {
    khoiTaoXacNhanGopLead();
    khoiTaoFlashAlert();
});

/**
 * 1. Khởi tạo xác nhận và chống submit form gộp Lead (AC3)
 */
function khoiTaoXacNhanGopLead() {
    const formGop = document.getElementById('formGopLead');
    const chkDongY = document.getElementById('chkDongYGop');
    const btnSubmit = document.getElementById('btnSubmitGopLead');

    if (!formGop || !btnSubmit) return;

    if (chkDongY) {
        chkDongY.addEventListener('change', function () {
            btnSubmit.disabled = !this.checked;
        });
    }

    formGop.addEventListener('submit', function (e) {
        if (chkDongY && !chkDongY.checked) {
            e.preventDefault();
            alert('Vui lòng tích chọn xác nhận trước khi thực hiện gộp Lead.');
            chkDongY.focus();
            return false;
        }

        const xacNhan = confirm('Hành động gộp không thể hoàn tác trực tiếp. Bạn có chắc chắn muốn gộp Lead nguồn vào Lead giữ lại không?');
        if (!xacNhan) {
            e.preventDefault();
            return false;
        }

        // Chống gửi trùng
        btnSubmit.disabled = true;
        btnSubmit.innerHTML = '<span class="material-symbols-outlined" style="animation: spin 1s linear infinite;">sync</span> <span>Đang xử lý gộp dữ liệu...</span>';
    });
}

/**
 * 2. Tự động đóng thông báo flash message sau 5 giây
 */
function khoiTaoFlashAlert() {
    const alerts = document.querySelectorAll('.gl-alert');
    alerts.forEach(function (alert) {
        const closeBtn = alert.querySelector('.gl-alert-close');
        if (closeBtn) {
            closeBtn.addEventListener('click', function () {
                alert.style.opacity = '0';
                setTimeout(function () {
                    alert.remove();
                }, 300);
            });
        }

        setTimeout(function () {
            if (alert && alert.parentElement) {
                alert.style.transition = 'opacity 0.5s ease';
                alert.style.opacity = '0';
                setTimeout(function () {
                    alert.remove();
                }, 500);
            }
        }, 5000);
    });
}
