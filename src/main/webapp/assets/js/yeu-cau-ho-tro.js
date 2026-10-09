/**
 * CRM BÁN HÀNG - JAVASCRIPT YÊU CẦU HỖ TRỢ SAU BÁN & GẮN CỜ RỦI RO (STORY S3-08)
 * Vanilla JavaScript xử lý modal, kiểm tra dữ liệu form và phím tắt trợ năng.
 */
(function () {
    'use strict';

    /**
     * Mở modal ghi nhận yêu cầu hỗ trợ mới
     * @param {string|number} [khachHangId] - ID khách hàng chọn sẵn (nếu mở từ trang 360)
     */
    function moModalGhiNhanYeuCau(khachHangId) {
        var modal = document.getElementById('modalGhiNhanYeuCau');
        if (!modal) return;

        modal.classList.add('is-active');
        modal.style.display = 'flex';
        modal.setAttribute('aria-hidden', 'false');

        // Khóa cuộn trang nền
        document.body.style.overflow = 'hidden';

        // Nếu có truyền ID khách hàng và có select chọn khách hàng
        if (khachHangId) {
            var selectKh = document.getElementById('selectKhachHang');
            if (selectKh) {
                selectKh.value = String(khachHangId);
            }
        }

        // Tự động focus vào tiêu đề yêu cầu
        var inputTieuDe = document.getElementById('tieuDeYeuCau');
        if (inputTieuDe) {
            setTimeout(function () {
                inputTieuDe.focus();
            }, 100);
        }
    }

    /**
     * Đóng modal ghi nhận yêu cầu hỗ trợ
     */
    function dongModalGhiNhanYeuCau() {
        var modal = document.getElementById('modalGhiNhanYeuCau');
        if (!modal) return;

        modal.classList.remove('is-active');
        modal.style.display = 'none';
        modal.setAttribute('aria-hidden', 'true');

        // Mở lại cuộn trang nền
        document.body.style.overflow = '';
    }

    /**
     * Validate dữ liệu form trước khi submit
     * @param {HTMLFormElement} form
     * @returns {boolean}
     */
    function kiemTraDuLieuForm(form) {
        if (!form) return true;

        var inputTieuDe = form.querySelector('[name="tieuDe"]');
        if (inputTieuDe) {
            var tieuDe = inputTieuDe.value.trim();
            if (tieuDe.length === 0) {
                alert('Vui lòng nhập tiêu đề yêu cầu hỗ trợ.');
                inputTieuDe.focus();
                return false;
            }
            if (tieuDe.length < 3) {
                alert('Tiêu đề yêu cầu hỗ trợ phải có ít nhất 3 ký tự.');
                inputTieuDe.focus();
                return false;
            }
        }

        var selectKh = form.querySelector('[name="khachHangId"]');
        if (selectKh && !selectKh.value) {
            alert('Vui lòng chọn khách hàng cần ghi nhận hỗ trợ.');
            selectKh.focus();
            return false;
        }

        return true;
    }

    // Đăng ký sự kiện khi DOM sẵn sàng
    document.addEventListener('DOMContentLoaded', function () {
        var modal = document.getElementById('modalGhiNhanYeuCau');
        if (modal) {
            // Đóng khi click ngoài backdrop
            modal.addEventListener('click', function (e) {
                if (e.target === modal) {
                    dongModalGhiNhanYeuCau();
                }
            });
        }

        // Đóng khi nhấn phím Escape
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape' || e.keyCode === 27) {
                dongModalGhiNhanYeuCau();
            }
        });

        // Gắn listener kiểm tra submit cho form ghi nhận
        var formGhiNhan = document.getElementById('formGhiNhanYeuCau');
        if (formGhiNhan) {
            formGhiNhan.addEventListener('submit', function (e) {
                if (!kiemTraDuLieuForm(formGhiNhan)) {
                    e.preventDefault();
                }
            });
        }
    });

    // Xuất ra global scope cho các thuộc tính onclick trong HTML/JSP
    window.moModalGhiNhanYeuCau = moModalGhiNhanYeuCau;
    window.dongModalGhiNhanYeuCau = dongModalGhiNhanYeuCau;
})();
