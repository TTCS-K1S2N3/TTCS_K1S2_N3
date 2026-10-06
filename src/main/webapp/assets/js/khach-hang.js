/**
 * khach-hang.js - Xử lý tương tác giao diện Danh mục khách hàng (Story S1-05 & Story S3-07)
 * - Mở/Đóng Modal Thêm khách hàng mới
 * - Mở/Đóng Modal Lưu bộ lọc đã lưu (Story S3-07 AC3)
 * - Chuyển đổi và áp dụng bộ lọc đã lưu
 * - Xóa và đặt làm mặc định bộ lọc đã lưu
 * - Client-side validation và submit form
 */

// Hàm toàn cục chuyển đổi bộ lọc khi người dùng chọn từ dropdown
window.chuyenBoLoc = function (boLocId) {
    const contextPath = window.CONTEXT_PATH || '';
    if (boLocId) {
        window.location.href = contextPath + '/khach-hang?boLocId=' + encodeURIComponent(boLocId);
    } else {
        window.location.href = contextPath + '/khach-hang?reset=1';
    }
};

document.addEventListener('DOMContentLoaded', function () {
    'use strict';

    const contextPath = window.CONTEXT_PATH || '';

    // Elements Modal Thêm khách hàng
    const btnThemKhachHang = document.getElementById('btnThemKhachHang');
    const modalThemKhachHang = document.getElementById('modalThemKhachHang');
    const btnDongModalThem = document.getElementById('btnDongModalThemKhachHang');
    const btnHuyThem = document.getElementById('btnHuyThemKhachHang');
    const formThemKhachHang = document.getElementById('formThemKhachHang');
    const inputTenCongTy = document.getElementById('tenCongTy');

    // Elements Modal Lưu bộ lọc (S3-07 AC3)
    const btnMoModalLuuBoLoc = document.getElementById('btnMoModalLuuBoLoc');
    const modalLuuBoLoc = document.getElementById('modalLuuBoLoc');
    const btnDongModalLuuBoLoc = document.getElementById('btnDongModalLuuBoLoc');
    const btnHuyLuuBoLoc = document.getElementById('btnHuyLuuBoLoc');
    const formLuuBoLoc = document.getElementById('formLuuBoLoc');
    const tenBoLocInput = document.getElementById('tenBoLocInput');

    // Elements thao tác bộ lọc hiện tại
    const btnXoaBoLocHienTai = document.getElementById('btnXoaBoLocHienTai');
    const btnDatMacDinhHienTai = document.getElementById('btnDatMacDinhHienTai');
    const btnExportExcel = document.getElementById('btnExportExcel');

    // Helper mở modal
    function moModal(modal, focusEl) {
        if (!modal) return;
        modal.style.display = 'flex';
        modal.classList.add('show');
        document.body.style.overflow = 'hidden';
        if (focusEl) {
            setTimeout(function () {
                focusEl.focus();
            }, 100);
        }
    }

    // Helper đóng modal
    function dongModal(modal) {
        if (!modal) return;
        modal.classList.remove('show');
        modal.style.display = 'none';
        document.body.style.overflow = '';
    }

    // Modal Thêm Khách Hàng
    if (btnThemKhachHang) {
        btnThemKhachHang.addEventListener('click', function (e) {
            e.preventDefault();
            moModal(modalThemKhachHang, inputTenCongTy);
        });
    }

    if (btnDongModalThem) {
        btnDongModalThem.addEventListener('click', function (e) {
            e.preventDefault();
            dongModal(modalThemKhachHang);
        });
    }

    if (btnHuyThem) {
        btnHuyThem.addEventListener('click', function (e) {
            e.preventDefault();
            dongModal(modalThemKhachHang);
        });
    }

    // Modal Lưu Bộ Lọc
    if (btnMoModalLuuBoLoc) {
        btnMoModalLuuBoLoc.addEventListener('click', function (e) {
            e.preventDefault();
            moModal(modalLuuBoLoc, tenBoLocInput);
        });
    }

    if (btnDongModalLuuBoLoc) {
        btnDongModalLuuBoLoc.addEventListener('click', function (e) {
            e.preventDefault();
            dongModal(modalLuuBoLoc);
        });
    }

    if (btnHuyLuuBoLoc) {
        btnHuyLuuBoLoc.addEventListener('click', function (e) {
            e.preventDefault();
            dongModal(modalLuuBoLoc);
        });
    }

    // Đóng khi click ngoài modal
    [modalThemKhachHang, modalLuuBoLoc].forEach(function (modal) {
        if (modal) {
            modal.addEventListener('click', function (e) {
                if (e.target === modal) {
                    dongModal(modal);
                }
            });
        }
    });

    // Đóng khi bấm Escape
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') {
            if (modalThemKhachHang && modalThemKhachHang.style.display === 'flex') {
                dongModal(modalThemKhachHang);
            }
            if (modalLuuBoLoc && modalLuuBoLoc.style.display === 'flex') {
                dongModal(modalLuuBoLoc);
            }
        }
    });

    // Thao tác Xóa bộ lọc hiện tại (S3-07 AC3)
    if (btnXoaBoLocHienTai) {
        btnXoaBoLocHienTai.addEventListener('click', function (e) {
            e.preventDefault();
            const urlParams = new URLSearchParams(window.location.search);
            const boLocId = urlParams.get('boLocId');
            if (!boLocId) return;

            if (confirm('Bạn có chắc chắn muốn xóa bộ lọc đã lưu này không?')) {
                const form = document.createElement('form');
                form.method = 'POST';
                form.action = contextPath + '/khach-hang';

                const actInput = document.createElement('input');
                actInput.type = 'hidden';
                actInput.name = 'action';
                actInput.value = 'xoa-bo-loc';
                form.appendChild(actInput);

                const idInput = document.createElement('input');
                idInput.type = 'hidden';
                idInput.name = 'boLocId';
                idInput.value = boLocId;
                form.appendChild(idInput);

                document.body.appendChild(form);
                form.submit();
            }
        });
    }

    // Thao tác Đặt làm mặc định (S3-07 AC3)
    if (btnDatMacDinhHienTai) {
        btnDatMacDinhHienTai.addEventListener('click', function (e) {
            e.preventDefault();
            const urlParams = new URLSearchParams(window.location.search);
            const boLocId = urlParams.get('boLocId');
            if (!boLocId) return;

            const form = document.createElement('form');
            form.method = 'POST';
            form.action = contextPath + '/khach-hang';

            const actInput = document.createElement('input');
            actInput.type = 'hidden';
            actInput.name = 'action';
            actInput.value = 'dat-mac-dinh';
            form.appendChild(actInput);

            const idInput = document.createElement('input');
            idInput.type = 'hidden';
            idInput.name = 'boLocId';
            idInput.value = boLocId;
            form.appendChild(idInput);

            document.body.appendChild(form);
            form.submit();
        });
    }

    // Submit form thêm khách hàng
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

    // Submit form lưu bộ lọc (Client-side validation)
    if (formLuuBoLoc) {
        formLuuBoLoc.addEventListener('submit', function (e) {
            if (tenBoLocInput && !tenBoLocInput.value.trim()) {
                e.preventDefault();
                alert('Vui lòng nhập tên cho bộ lọc.');
                tenBoLocInput.focus();
                return false;
            }
        });
    }

    // Nút Xuất Excel
    if (btnExportExcel) {
        btnExportExcel.addEventListener('click', function (e) {
            e.preventDefault();
            const urlParams = new URLSearchParams(window.location.search);
            urlParams.set('xuatExcel', 'true');
            window.location.href = contextPath + '/khach-hang?' + urlParams.toString();
        });
    }
});
