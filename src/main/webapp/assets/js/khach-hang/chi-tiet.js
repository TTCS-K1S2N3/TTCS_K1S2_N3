/**
 * chi-tiet.js - Xử lý tương tác giao diện Chi Tiết Khách Hàng & Quan Hệ Công Ty Mẹ - Con (Story S3-05).
 * - Mở / đóng các hộp thoại khai báo công ty con và công ty mẹ
 * - Hỗ trợ phím tắt ESC và click ra ngoài backdrop để đóng modal
 * - Client-side validation và ngăn chặn submit trùng lặp
 */
(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        initModals();
        initForms();
    });

    /**
     * Khởi tạo các Modal: Gắn Công Ty Con và Khai Báo Công Ty Mẹ
     */
    function initModals() {
        var modalGanCongTyCon = document.getElementById('modalGanCongTyCon');
        var modalChonCongTyMe = document.getElementById('modalChonCongTyMe');

        function moModal(modal, focusElementId) {
            if (!modal) return;
            modal.style.display = 'flex';
            document.body.style.overflow = 'hidden';
            if (focusElementId) {
                var el = document.getElementById(focusElementId);
                if (el) {
                    setTimeout(function () { el.focus(); }, 100);
                }
            }
        }

        function dongModal(modal) {
            if (!modal) return;
            modal.style.display = 'none';
            // Chỉ khôi phục cuộn trang nếu cả 2 modal đều đã đóng
            var m1Open = modalGanCongTyCon && modalGanCongTyCon.style.display === 'flex';
            var m2Open = modalChonCongTyMe && modalChonCongTyMe.style.display === 'flex';
            if (!m1Open && !m2Open) {
                document.body.style.overflow = '';
            }
        }

        // Đăng ký toàn cục để tương thích các nút bấm
        window.moModalGanCongTyCon = function () {
            moModal(modalGanCongTyCon, 'selectCongTyCon');
        };

        window.dongModalGanCongTyCon = function () {
            dongModal(modalGanCongTyCon);
        };

        window.moModalChonCongTyMe = function () {
            moModal(modalChonCongTyMe, 'selectCongTyMe');
        };

        window.dongModalChonCongTyMe = function () {
            dongModal(modalChonCongTyMe);
        };

        // Đóng khi click ngoài hộp thoại modal
        window.addEventListener('click', function (e) {
            if (e.target === modalGanCongTyCon) dongModal(modalGanCongTyCon);
            if (e.target === modalChonCongTyMe) dongModal(modalChonCongTyMe);
        });

        // Đóng khi bấm phím Escape
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape') {
                if (modalGanCongTyCon && modalGanCongTyCon.style.display === 'flex') {
                    dongModal(modalGanCongTyCon);
                }
                if (modalChonCongTyMe && modalChonCongTyMe.style.display === 'flex') {
                    dongModal(modalChonCongTyMe);
                }
            }
        });
    }

    /**
     * Khởi tạo validation và hiệu ứng khi submit form
     */
    function initForms() {
        var formGanCon = document.getElementById('formGanCongTyCon');
        var selectCon = document.getElementById('selectCongTyCon');
        var btnXacNhanGanCon = document.getElementById('btnXacNhanGanCon');

        if (formGanCon) {
            formGanCon.addEventListener('submit', function (e) {
                if (selectCon && (!selectCon.value || selectCon.value.trim() === '')) {
                    e.preventDefault();
                    alert('Vui lòng chọn một khách hàng để gắn làm công ty con.');
                    selectCon.focus();
                    return false;
                }

                if (btnXacNhanGanCon) {
                    btnXacNhanGanCon.disabled = true;
                    btnXacNhanGanCon.innerHTML = '<span class="material-symbols-outlined icon-spin" aria-hidden="true" style="font-size: 16px;">progress_activity</span> Đang xử lý...';
                }
            });
        }

        var formDoiMe = document.getElementById('formCapNhatCongTyMe');
        var btnXacNhanDoiMe = document.getElementById('btnXacNhanDoiMe');

        if (formDoiMe && btnXacNhanDoiMe) {
            formDoiMe.addEventListener('submit', function () {
                btnXacNhanDoiMe.disabled = true;
                btnXacNhanDoiMe.innerHTML = '<span class="material-symbols-outlined icon-spin" aria-hidden="true" style="font-size: 16px;">progress_activity</span> Đang lưu...';
            });
        }
    }
})();
