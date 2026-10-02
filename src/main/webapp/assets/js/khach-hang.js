/**
 * khach-hang.js - Xử lý tương tác giao diện Danh mục khách hàng (Story S1-05)
 * - Mở/Đóng Modal Thêm khách hàng mới
 * - Client-side validation và submit form
 * - Tự động hiển thị và quản lý thông báo phản hồi
 */
document.addEventListener('DOMContentLoaded', function () {
    'use strict';

    const btnThemKhachHang = document.getElementById('btnThemKhachHang');
    const modalThemKhachHang = document.getElementById('modalThemKhachHang');
    const btnDongModal = document.getElementById('btnDongModalThemKhachHang');
    const btnHuyThem = document.getElementById('btnHuyThemKhachHang');
    const formThemKhachHang = document.getElementById('formThemKhachHang');
    const inputTenCongTy = document.getElementById('tenCongTy');
    const btnExportExcel = document.getElementById('btnExportExcel');

    // Mở Modal
    function moModal() {
        if (!modalThemKhachHang) return;
        modalThemKhachHang.style.display = 'flex';
        modalThemKhachHang.classList.add('show');
        document.body.style.overflow = 'hidden'; // Khóa cuộn trang khi modal mở
        if (inputTenCongTy) {
            setTimeout(function () {
                inputTenCongTy.focus();
            }, 100);
        }
    }

    // Đóng Modal
    function dongModal() {
        if (!modalThemKhachHang) return;
        modalThemKhachHang.classList.remove('show');
        modalThemKhachHang.style.display = 'none';
        document.body.style.overflow = '';
    }

    if (btnThemKhachHang) {
        btnThemKhachHang.addEventListener('click', function (e) {
            e.preventDefault();
            moModal();
        });
    }

    if (btnDongModal) {
        btnDongModal.addEventListener('click', function (e) {
            e.preventDefault();
            dongModal();
        });
    }

    if (btnHuyThem) {
        btnHuyThem.addEventListener('click', function (e) {
            e.preventDefault();
            dongModal();
        });
    }

    // Đóng khi click bên ngoài nội dung modal
    if (modalThemKhachHang) {
        modalThemKhachHang.addEventListener('click', function (e) {
            if (e.target === modalThemKhachHang) {
                dongModal();
            }
        });
    }

    // Đóng khi bấm phím ESC
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && modalThemKhachHang && modalThemKhachHang.style.display === 'flex') {
            dongModal();
        }
    });

    // Client-side validation khi submit
    if (formThemKhachHang) {
        formThemKhachHang.addEventListener('submit', function (e) {
            if (inputTenCongTy && !inputTenCongTy.value.trim()) {
                e.preventDefault();
                alert('Vui lòng nhập Tên khách hàng / Công ty.');
                inputTenCongTy.focus();
                return false;
            }
        });
    }

    // Nút Xuất Excel
    if (btnExportExcel) {
        btnExportExcel.addEventListener('click', function (e) {
            e.preventDefault();
            const contextPath = window.CONTEXT_PATH || '';
            window.location.href = contextPath + '/phan-quyen-du-lieu?xuat=csv&loai=KHACH_HANG';
        });
    }
});
