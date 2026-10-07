/**
 * khach-hang.js - Xử lý tương tác giao diện Danh mục khách hàng (Story S1-05 & Story S3-07)
 * - Mở/Đóng Modal Thêm khách hàng mới
 * - Mở/Đóng Modal Lưu bộ lọc đã lưu (Story S3-07 AC3)
 * - Đồng bộ các tiêu chí tìm kiếm/lọc sang Modal Lưu bộ lọc
 * - Chuyển đổi và áp dụng bộ lọc đã lưu
 * - Xóa và đặt làm mặc định bộ lọc đã lưu
 * - Nút xóa nhanh từ khóa tìm kiếm & xóa từng tiêu chí active
 * - Client-side validation và submit form
 */

// Hàm toàn cục chuyển đổi bộ lọc khi người dùng chọn từ dropdown (AC3)
window.chuyenBoLoc = function (boLocId) {
    var contextPath = window.CONTEXT_PATH || '';
    if (boLocId) {
        window.location.href = contextPath + '/khach-hang?boLocId=' + encodeURIComponent(boLocId);
    } else {
        window.location.href = contextPath + '/khach-hang?reset=1';
    }
};

document.addEventListener('DOMContentLoaded', function () {
    'use strict';

    var contextPath = window.CONTEXT_PATH || '';

    // Elements Modal Thêm khách hàng (S1-05)
    var btnThemKhachHang = document.getElementById('btnThemKhachHang');
    var modalThemKhachHang = document.getElementById('modalThemKhachHang');
    var btnDongModalThem = document.getElementById('btnDongModalThemKhachHang');
    var btnHuyThem = document.getElementById('btnHuyThemKhachHang');
    var formThemKhachHang = document.getElementById('formThemKhachHang');
    var inputTenCongTy = document.getElementById('tenCongTy');

    // Elements Modal Lưu bộ lọc (S3-07 AC3)
    var btnMoModalLuuBoLoc = document.getElementById('btnMoModalLuuBoLoc');
    var modalLuuBoLoc = document.getElementById('modalLuuBoLoc');
    var btnDongModalLuuBoLoc = document.getElementById('btnDongModalLuuBoLoc');
    var btnHuyLuuBoLoc = document.getElementById('btnHuyLuuBoLoc');
    var formLuuBoLoc = document.getElementById('formLuuBoLoc');
    var tenBoLocInput = document.getElementById('tenBoLocInput');
    var tenBoLocError = document.getElementById('tenBoLocError');
    var modalFilterSummaryContent = document.getElementById('modalFilterSummaryContent');

    // Elements Thao tác tìm kiếm & lọc (S3-07 AC1, AC2)
    var formLocKhachHang = document.getElementById('formLocKhachHang');
    var tuKhoaInput = document.getElementById('tuKhoa');
    var btnClearTuKhoa = document.getElementById('btnClearTuKhoa');
    var btnXoaBoLocHienTai = document.getElementById('btnXoaBoLocHienTai');
    var btnDatMacDinhHienTai = document.getElementById('btnDatMacDinhHienTai');
    var btnExportExcel = document.getElementById('btnExportExcel');

    // Helper mở modal
    function moModal(modal, focusEl) {
        if (!modal) return;
        modal.style.display = 'flex';
        modal.setAttribute('aria-hidden', 'false');
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
        modal.style.display = 'none';
        modal.setAttribute('aria-hidden', 'true');
        document.body.style.overflow = '';
    }

    // Đồng bộ các tiêu chí từ form lọc chính sang form trong modal lưu bộ lọc (S3-07 AC3)
    function dongBoTieuChiSangModalLuu() {
        if (!formLocKhachHang || !formLuuBoLoc) return;

        var fields = [
            'tuKhoa', 'tenCongTy', 'maSoThue', 'soDienThoai',
            'trangThai', 'nganhNgheId', 'quyMoId', 'khuVucId', 'nguoiSoHuuId'
        ];

        var summaryItems = [];

        fields.forEach(function (field) {
            var srcEl = formLocKhachHang.querySelector('[name="' + field + '"]');
            var targetHidden = formLuuBoLoc.querySelector('input[type="hidden"][name="' + field + '"]');

            if (srcEl && targetHidden) {
                var val = srcEl.value.trim();
                targetHidden.value = val;

                if (val) {
                    var labelText = '';
                    if (srcEl.tagName === 'INPUT') {
                        var labelEl = formLocKhachHang.querySelector('label[for="' + srcEl.id + '"]');
                        var title = labelEl ? labelEl.textContent.trim() : field;
                        labelText = title + ': "' + val + '"';
                    } else if (srcEl.tagName === 'SELECT') {
                        var selectedOption = srcEl.options[srcEl.selectedIndex];
                        if (selectedOption && selectedOption.value) {
                            var labelEl = formLocKhachHang.querySelector('label[for="' + srcEl.id + '"]');
                            var groupTitle = labelEl ? labelEl.textContent.trim() : field;
                            labelText = groupTitle + ': ' + selectedOption.textContent.trim();
                        }
                    }

                    if (labelText) {
                        summaryItems.push(labelText);
                    }
                }
            }
        });

        // Cập nhật tóm tắt trực quan trong modal
        if (modalFilterSummaryContent) {
            modalFilterSummaryContent.innerHTML = '';
            if (summaryItems.length > 0) {
                summaryItems.forEach(function (item) {
                    var tag = document.createElement('span');
                    tag.className = 'filter-tag';
                    tag.textContent = item;
                    modalFilterSummaryContent.appendChild(tag);
                });
            } else {
                var muted = document.createElement('span');
                muted.className = 'filter-tag-muted';
                muted.textContent = 'Toàn bộ danh sách khách hàng (không lọc tiêu chí cụ thể)';
                modalFilterSummaryContent.appendChild(muted);
            }
        }
    }

    // Modal Thêm Khách Hàng (S1-05)
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

    // Modal Lưu Bộ Lọc (S3-07 AC3)
    if (btnMoModalLuuBoLoc) {
        btnMoModalLuuBoLoc.addEventListener('click', function (e) {
            e.preventDefault();
            dongBoTieuChiSangModalLuu();
            if (tenBoLocInput) {
                if (!tenBoLocInput.value) {
                    var d = new Date();
                    var ngayText = d.getDate() + '/' + (d.getMonth() + 1);
                    tenBoLocInput.placeholder = 'Ví dụ: Khách gọi tuần này (' + ngayText + ')';
                }
            }
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

    // Đóng khi click ngoài backdrop
    [modalThemKhachHang, modalLuuBoLoc].forEach(function (modal) {
        if (modal) {
            modal.addEventListener('click', function (e) {
                if (e.target === modal) {
                    dongModal(modal);
                }
            });
        }
    });

    // Đóng khi bấm phím Escape
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' || e.key === 'Esc') {
            if (modalThemKhachHang && modalThemKhachHang.style.display === 'flex') {
                dongModal(modalThemKhachHang);
            }
            if (modalLuuBoLoc && modalLuuBoLoc.style.display === 'flex') {
                dongModal(modalLuuBoLoc);
            }
        }
    });

    // Nút xóa nhanh từ khóa tìm kiếm (AC2)
    if (tuKhoaInput && btnClearTuKhoa) {
        function capNhatNutClear() {
            btnClearTuKhoa.style.display = tuKhoaInput.value.trim().length > 0 ? 'inline-flex' : 'none';
        }

        tuKhoaInput.addEventListener('input', capNhatNutClear);
        capNhatNutClear();

        btnClearTuKhoa.addEventListener('click', function () {
            tuKhoaInput.value = '';
            capNhatNutClear();
            tuKhoaInput.focus();
        });
    }

    // Xóa từng tiêu chí active (Filter chips)
    var chipRemoveButtons = document.querySelectorAll('.filter-chip-remove');
    chipRemoveButtons.forEach(function (btn) {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            var fieldName = this.getAttribute('data-field');
            if (fieldName && formLocKhachHang) {
                var fieldEl = formLocKhachHang.querySelector('[name="' + fieldName + '"]');
                if (fieldEl) {
                    fieldEl.value = '';
                    formLocKhachHang.submit();
                }
            }
        });
    });

    // Thao tác Xóa bộ lọc hiện tại (S3-07 AC3)
    if (btnXoaBoLocHienTai) {
        btnXoaBoLocHienTai.addEventListener('click', function (e) {
            e.preventDefault();
            var urlParams = new URLSearchParams(window.location.search);
            var boLocId = urlParams.get('boLocId');
            if (!boLocId) return;

            if (confirm('Bạn có chắc chắn muốn xóa bộ lọc đã lưu này không?')) {
                var form = document.createElement('form');
                form.method = 'POST';
                form.action = contextPath + '/khach-hang';

                var actInput = document.createElement('input');
                actInput.type = 'hidden';
                actInput.name = 'action';
                actInput.value = 'xoa-bo-loc';
                form.appendChild(actInput);

                var idInput = document.createElement('input');
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
            var urlParams = new URLSearchParams(window.location.search);
            var boLocId = urlParams.get('boLocId');
            if (!boLocId) return;

            var form = document.createElement('form');
            form.method = 'POST';
            form.action = contextPath + '/khach-hang';

            var actInput = document.createElement('input');
            actInput.type = 'hidden';
            actInput.name = 'action';
            actInput.value = 'dat-mac-dinh';
            form.appendChild(actInput);

            var idInput = document.createElement('input');
            idInput.type = 'hidden';
            idInput.name = 'boLocId';
            idInput.value = boLocId;
            form.appendChild(idInput);

            document.body.appendChild(form);
            form.submit();
        });
    }

    // Submit form thêm khách hàng với validation
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

    // Submit form lưu bộ lọc (Client-side validation inline)
    if (formLuuBoLoc) {
        formLuuBoLoc.addEventListener('submit', function (e) {
            if (tenBoLocInput && !tenBoLocInput.value.trim()) {
                e.preventDefault();
                if (tenBoLocError) {
                    tenBoLocError.style.display = 'block';
                }
                tenBoLocInput.focus();
                return false;
            }
            if (tenBoLocError) {
                tenBoLocError.style.display = 'none';
            }
        });

        if (tenBoLocInput) {
            tenBoLocInput.addEventListener('input', function () {
                if (tenBoLocError && tenBoLocInput.value.trim()) {
                    tenBoLocError.style.display = 'none';
                }
            });
        }
    }

    // Nút Xuất Excel
    if (btnExportExcel) {
        btnExportExcel.addEventListener('click', function (e) {
            e.preventDefault();
            var urlParams = new URLSearchParams(window.location.search);
            urlParams.set('xuatExcel', 'true');
            window.location.href = contextPath + '/khach-hang?' + urlParams.toString();
        });
    }
});
