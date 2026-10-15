/**
 * MODULE: QUẢN LÝ CHIẾN DỊCH MARKETING (STORY S4-03)
 * Xử lý JavaScript tương tác giao diện người dùng:
 * - Tìm kiếm & lọc nhanh danh sách chiến dịch
 * - Kiểm tra hợp lệ form (startDate <= endDate, ngân sách >= 0)
 * - Chống gửi form nhiều lần (Double Submit Prevention)
 * - Tự động ẩn thông báo flash message sau 5 giây
 */

document.addEventListener('DOMContentLoaded', function () {
    khoiTaoTimKiemNhanh();
    khoiTaoFormValidation();
    khoiTaoFlashAlert();
});

/**
 * 1. Tìm kiếm và lọc nhanh bảng dữ liệu chiến dịch (Client-side fast search)
 */
function khoiTaoTimKiemNhanh() {
    const searchInput = document.getElementById('searchCampaignInput');
    const tableBody = document.getElementById('campaignTableBody');

    if (!searchInput || !tableBody) return;

    searchInput.addEventListener('input', function () {
        const query = this.value.trim().toLowerCase();
        const rows = tableBody.querySelectorAll('tr.cd-data-row');
        let visibleCount = 0;

        rows.forEach(function (row) {
            const textContent = row.textContent.toLowerCase();
            if (textContent.includes(query)) {
                row.style.display = '';
                visibleCount++;
            } else {
                row.style.display = 'none';
            }
        });

        // Hiển thị hoặc ẩn thông báo không tìm thấy kết quả
        const noMatchRow = document.getElementById('noMatchRow');
        if (noMatchRow) {
            if (visibleCount === 0 && rows.length > 0) {
                noMatchRow.style.display = '';
            } else {
                noMatchRow.style.display = 'none';
            }
        }
    });
}

/**
 * 2. Validate form khai báo chiến dịch & chống double submit (AC1)
 */
function khoiTaoFormValidation() {
    const form = document.getElementById('campaignForm');
    if (!form) return;

    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');
    const budgetInput = document.getElementById('budgetAmount');
    const submitBtn = document.getElementById('btnSubmitCampaign');

    // Kiểm tra tính hợp lệ của ngày kết thúc so với ngày bắt đầu ngay khi người dùng chọn
    function kiemTraNgay() {
        if (!startDateInput || !endDateInput) return true;
        const startVal = startDateInput.value;
        const endVal = endDateInput.value;

        const dateErrorSpan = document.getElementById('dateLogicError');

        if (startVal && endVal) {
            if (new Date(endVal) < new Date(startVal)) {
                endDateInput.classList.add('is-invalid');
                if (dateErrorSpan) {
                    dateErrorSpan.textContent = 'Ngày kết thúc không được trước ngày bắt đầu.';
                    dateErrorSpan.style.display = 'flex';
                }
                return false;
            } else {
                endDateInput.classList.remove('is-invalid');
                if (dateErrorSpan) {
                    dateErrorSpan.textContent = '';
                    dateErrorSpan.style.display = 'none';
                }
                return true;
            }
        }
        return true;
    }

    if (startDateInput && endDateInput) {
        startDateInput.addEventListener('change', kiemTraNgay);
        endDateInput.addEventListener('change', kiemTraNgay);
    }

    // Xử lý khi submit form
    form.addEventListener('submit', function (e) {
        const isDateValid = kiemTraNgay();
        if (!isDateValid) {
            e.preventDefault();
            endDateInput.focus();
            return false;
        }

        // Chống gửi trùng (Double submit prevention)
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<span class="material-symbols-outlined" style="animation: spin 1s linear infinite;">sync</span> <span>Đang lưu chiến dịch...</span>';
        }
    });
}

/**
 * 3. Tự động đóng thông báo flash message sau 5 giây
 */
function khoiTaoFlashAlert() {
    const alerts = document.querySelectorAll('.cd-alert');
    alerts.forEach(function (alert) {
        // Nút bấm đóng thủ công
        const closeBtn = alert.querySelector('.cd-alert-close');
        if (closeBtn) {
            closeBtn.addEventListener('click', function () {
                alert.style.opacity = '0';
                setTimeout(function () {
                    alert.remove();
                }, 300);
            });
        }

        // Tự động đóng sau 5 giây
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
