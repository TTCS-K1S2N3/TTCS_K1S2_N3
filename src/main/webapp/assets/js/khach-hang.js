/**
 * khach-hang.js - Xử lý giao diện quản lý hồ sơ khách hàng doanh nghiệp (Story S3-01)
 * Đáp ứng các Acceptance Criteria:
 * - AC1: Khai báo tên công ty, mã số thuế, ngành nghề, quy mô, website, địa chỉ, người sở hữu
 * - AC2: Mã số thuế nếu có thì phải là duy nhất
 * - AC3: Khách hàng có 4 trạng thái: Tiềm năng, Đang giao dịch, Khách hàng, Ngừng hợp tác
 * - AC4: Data Scope enforcement (Chống owner spoofing, tuân thủ vai trò)
 */
document.addEventListener('DOMContentLoaded', function () {
    'use strict';

    // 1. DOM Elements - Modal Thêm khách hàng
    const btnThemKhachHang = document.getElementById('btnThemKhachHang');
    const modalThemKhachHang = document.getElementById('modalThemKhachHang');
    const btnDongModalThem = document.getElementById('btnDongModalThemKhachHang');
    const btnHuyThem = document.getElementById('btnHuyThemKhachHang');
    const formThemKhachHang = document.getElementById('formThemKhachHang');
    const inputTenCongTy = document.getElementById('tenCongTy');
    const inputMaSoThue = document.getElementById('maSoThue');
    const inputWebsite = document.getElementById('website');

    // 2. DOM Elements - Modal Sửa khách hàng
    const modalSuaKhachHang = document.getElementById('modalSuaKhachHang');
    const btnDongModalSua = document.getElementById('btnDongModalSuaKhachHang');
    const btnHuySua = document.getElementById('btnHuySuaKhachHang');
    const formSuaKhachHang = document.getElementById('formSuaKhachHang');
    const suaKhachHangId = document.getElementById('suaKhachHangId');
    const suaTenCongTy = document.getElementById('suaTenCongTy');
    const suaMaKhachHang = document.getElementById('suaMaKhachHang');
    const suaMaSoThue = document.getElementById('suaMaSoThue');
    const suaNganhNgheId = document.getElementById('suaNganhNgheId');
    const suaQuyMoId = document.getElementById('suaQuyMoId');
    const suaWebsite = document.getElementById('suaWebsite');
    const suaDiaChi = document.getElementById('suaDiaChi');
    const suaTrangThai = document.getElementById('suaTrangThai');
    const suaDoanhThuUocTinh = document.getElementById('suaDoanhThuUocTinh');
    const suaNguoiSoHuuId = document.getElementById('suaNguoiSoHuuId');
    const suaMoTaChiTiet = document.getElementById('suaMoTaChiTiet');

    // 3. Nút xuất Excel và các nút thao tác khác
    const btnExportExcel = document.getElementById('btnExportExcel');
    const btnDongDetailCard = document.getElementById('btnDongDetailCard');
    const btnsThemKHEmpty = document.querySelectorAll('.btn-them-kh-empty');

    // Helper: Mở modal
    function moModal(modal, focusInput) {
        if (!modal) return;
        modal.style.display = 'flex';
        modal.classList.add('show');
        document.body.style.overflow = 'hidden';
        if (focusInput) {
            setTimeout(function () {
                focusInput.focus();
            }, 100);
        }
    }

    // Helper: Đóng modal
    function dongModal(modal) {
        if (!modal) return;
        modal.classList.remove('show');
        modal.style.display = 'none';
        document.body.style.overflow = '';
    }

    // Gắn sự kiện Modal Thêm
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

    // Gắn sự kiện nút thêm ở empty state
    btnsThemKHEmpty.forEach(function (btn) {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            moModal(modalThemKhachHang, inputTenCongTy);
        });
    });

    // Gắn sự kiện Modal Sửa
    if (btnDongModalSua) {
        btnDongModalSua.addEventListener('click', function (e) {
            e.preventDefault();
            dongModal(modalSuaKhachHang);
        });
    }

    if (btnHuySua) {
        btnHuySua.addEventListener('click', function (e) {
            e.preventDefault();
            dongModal(modalSuaKhachHang);
        });
    }

    // Hàm điền dữ liệu và mở modal sửa
    function moModalSua(dataset) {
        if (!modalSuaKhachHang || !dataset) return;

        if (suaKhachHangId) suaKhachHangId.value = dataset.id || '';
        if (suaTenCongTy) suaTenCongTy.value = dataset.ten || '';
        if (suaMaKhachHang) suaMaKhachHang.value = dataset.makh || '';
        if (suaMaSoThue) suaMaSoThue.value = dataset.mst || '';
        if (suaNganhNgheId) suaNganhNgheId.value = dataset.nganh || '';
        if (suaQuyMoId) suaQuyMoId.value = dataset.quymo || '';
        if (suaWebsite) suaWebsite.value = dataset.website || '';
        if (suaDiaChi) suaDiaChi.value = dataset.diachi || '';

        if (suaTrangThai) {
            var tt = dataset.trangthai || '';
            // Chuẩn hóa trạng thái nếu là mã enum sang tên tiếng Việt
            if (tt === 'TIEM_NANG') tt = 'Tiềm năng';
            else if (tt === 'DANG_GIAO_DICH') tt = 'Đang giao dịch';
            else if (tt === 'KHACH_HANG') tt = 'Khách hàng';
            else if (tt === 'NGUNG_HOP_TAC') tt = 'Ngừng hợp tác';
            suaTrangThai.value = tt;
        }

        if (suaDoanhThuUocTinh) suaDoanhThuUocTinh.value = dataset.gia || '';
        if (suaNguoiSoHuuId && dataset.nguoisohuu) {
            suaNguoiSoHuuId.value = dataset.nguoisohuu;
        }
        if (suaMoTaChiTiet) suaMoTaChiTiet.value = dataset.mota || '';

        moModal(modalSuaKhachHang, suaTenCongTy);
    }

    // Gắn sự kiện cho các nút Sửa trong bảng và trong Thẻ chi tiết
    document.addEventListener('click', function (e) {
        var btnSua = e.target.closest('.btn-sua-khach-hang');
        if (btnSua) {
            e.preventDefault();
            moModalSua(btnSua.dataset);
        }
    });

    // Đóng modal khi click ra ngoài vùng backdrop
    [modalThemKhachHang, modalSuaKhachHang].forEach(function (m) {
        if (m) {
            m.addEventListener('click', function (e) {
                if (e.target === m) {
                    dongModal(m);
                }
            });
        }
    });

    // Đóng modal khi ấn Escape
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') {
            if (modalThemKhachHang && modalThemKhachHang.style.display === 'flex') {
                dongModal(modalThemKhachHang);
            }
            if (modalSuaKhachHang && modalSuaKhachHang.style.display === 'flex') {
                dongModal(modalSuaKhachHang);
            }
        }
    });

    // Client-side validation Form Thêm
    if (formThemKhachHang) {
        formThemKhachHang.addEventListener('submit', function (e) {
            if (inputTenCongTy && !inputTenCongTy.value.trim()) {
                e.preventDefault();
                alert('Vui lòng nhập tên công ty / khách hàng (Bắt buộc).');
                inputTenCongTy.focus();
                return false;
            }

            if (inputMaSoThue && inputMaSoThue.value.trim()) {
                var mstClean = inputMaSoThue.value.trim();
                // MST chuẩn Việt Nam gồm 10 hoặc 13 ký tự số (có thể có dấu gạch ngang)
                var mstRegex = /^[0-9]{10}(-[0-9]{3})?$/;
                if (!mstRegex.test(mstClean) && !/^[0-9]{10,14}$/.test(mstClean)) {
                    var confirmMST = confirm('Mã số thuế "' + mstClean + '" có định dạng khác thông thường (chuẩn 10 hoặc 13 số). Bạn có chắc chắn muốn tiếp tục lưu?');
                    if (!confirmMST) {
                        e.preventDefault();
                        inputMaSoThue.focus();
                        return false;
                    }
                }
            }
        });
    }

    // Client-side validation Form Sửa
    if (formSuaKhachHang) {
        formSuaKhachHang.addEventListener('submit', function (e) {
            if (suaTenCongTy && !suaTenCongTy.value.trim()) {
                e.preventDefault();
                alert('Vui lòng nhập tên công ty / khách hàng (Bắt buộc).');
                suaTenCongTy.focus();
                return false;
            }
        });
    }

    // Đóng thẻ chi tiết khách hàng và trở về danh sách thuần
    if (btnDongDetailCard) {
        btnDongDetailCard.addEventListener('click', function (e) {
            e.preventDefault();
            var card = document.getElementById('customerDetailCard');
            if (card) {
                card.style.display = 'none';
            }
            // Loại bỏ param id trên URL nếu trình duyệt hỗ trợ history API
            if (window.history && window.history.replaceState) {
                var currentUrl = new URL(window.location.href);
                currentUrl.searchParams.delete('id');
                window.history.replaceState({}, document.title, currentUrl.pathname + (currentUrl.search ? currentUrl.search : ''));
            }
        });
    }

    // Nút Xuất Excel: giữ nguyên toàn bộ bộ lọc tìm kiếm hiện tại
    if (btnExportExcel) {
        btnExportExcel.addEventListener('click', function (e) {
            e.preventDefault();
            var contextPath = window.CONTEXT_PATH || '';
            var urlParams = new URLSearchParams(window.location.search);
            urlParams.set('xuatExcel', 'true');
            window.location.href = contextPath + '/khach-hang?' + urlParams.toString();
        });
    }
});
