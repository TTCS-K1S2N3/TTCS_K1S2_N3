/**
 * JavaScript Module: Nhập người dùng từ Excel (Story S2-01 - FE)
 * Tác giả: Hứa Lan Hương (FE)
 *
 * Chức năng:
 * - Kéo thả tệp Excel, thẩm định dung lượng và định dạng trước khi gửi
 * - Chuyển đổi tab xem trước: Tất cả, Dòng hợp lệ, Dòng có lỗi
 * - Tìm kiếm realtime trên bảng dữ liệu xem trước và báo cáo tổng kết
 * - Sao chép mật khẩu tạm / sao chép toàn bộ danh sách tài khoản tạo mới
 * - Modal xác nhận nhập thông minh: hiển thị rõ số dòng hợp lệ và số dòng bị bỏ qua
 * - Trạng thái loading khi xử lý tệp
 */

'use strict';

(function () {
    const NguoiDungImport = {
        init() {
            this.initDropzone();
            this.initTableFilterAndSearch();
            this.initCopyCredentials();
            this.initConfirmModal();
            this.initFormSubmitState();
            this.initPrintReport();
        },

        /**
         * 1. Xử lý kéo thả và chọn tệp Excel
         */
        initDropzone() {
            const dropzone = document.getElementById('dropzone-container');
            const fileInput = document.getElementById('fileExcel');
            const fileCard = document.getElementById('selected-file-card');
            const fileNameEl = document.getElementById('file-name-display');
            const fileSizeEl = document.getElementById('file-size-display');
            const btnRemove = document.getElementById('btn-remove-selected-file');

            if (!dropzone || !fileInput) return;

            const updateFileDisplay = (file) => {
                if (!file) {
                    if (fileCard) fileCard.style.display = 'none';
                    return;
                }

                // Kiểm tra đuôi tệp
                const validExtensions = ['.xlsx', '.xls'];
                const fileNameLower = file.name.toLowerCase();
                const isValidExtension = validExtensions.some(ext => fileNameLower.endsWith(ext));

                if (!isValidExtension) {
                    alert('Định dạng tệp không được hỗ trợ. Vui lòng chỉ chọn tệp bảng tính Excel (.xlsx hoặc .xls).');
                    fileInput.value = '';
                    if (fileCard) fileCard.style.display = 'none';
                    return;
                }

                // Kiểm tra dung lượng (10 MB = 10 * 1024 * 1024 bytes)
                const maxBytes = 10 * 1024 * 1024;
                if (file.size > maxBytes) {
                    alert('Dung lượng tệp vượt quá giới hạn 10MB. Vui lòng tối ưu lại tệp trước khi tải lên.');
                    fileInput.value = '';
                    if (fileCard) fileCard.style.display = 'none';
                    return;
                }

                if (fileNameEl) fileNameEl.textContent = file.name;
                if (fileSizeEl) {
                    const sizeKB = (file.size / 1024).toFixed(1);
                    const sizeMB = (file.size / (1024 * 1024)).toFixed(2);
                    fileSizeEl.textContent = file.size > 1024 * 1024 ? `${sizeMB} MB` : `${sizeKB} KB`;
                }
                if (fileCard) fileCard.style.display = 'flex';
            };

            // Sự kiện khi người dùng chọn file bằng dialog
            fileInput.addEventListener('change', function () {
                if (this.files && this.files.length > 0) {
                    updateFileDisplay(this.files[0]);
                }
            });

            // Sự kiện kéo thả (Drag and drop)
            ['dragenter', 'dragover'].forEach(eventName => {
                dropzone.addEventListener(eventName, (e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    dropzone.classList.add('drag-over');
                }, false);
            });

            ['dragleave', 'drop'].forEach(eventName => {
                dropzone.addEventListener(eventName, (e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    dropzone.classList.remove('drag-over');
                }, false);
            });

            dropzone.addEventListener('drop', (e) => {
                const dt = e.dataTransfer;
                if (dt && dt.files && dt.files.length > 0) {
                    fileInput.files = dt.files;
                    updateFileDisplay(dt.files[0]);
                }
            });

            // Xóa tệp đã chọn
            if (btnRemove) {
                btnRemove.addEventListener('click', (e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    fileInput.value = '';
                    if (fileCard) fileCard.style.display = 'none';
                });
            }
        },

        /**
         * 2. Lọc theo tab (Tất cả / Hợp lệ / Có lỗi) và tìm kiếm realtime
         */
        initTableFilterAndSearch() {
            const table = document.getElementById('bang-ket-qua-excel');
            const searchInput = document.getElementById('timKiemBang');
            const tabButtons = document.querySelectorAll('.tab-filter-btn');
            const emptyNotice = document.getElementById('empty-filter-notice');

            if (!table) return;

            let currentFilter = 'all'; // 'all' | 'valid' | 'error' | 'imported' | 'skipped'
            let currentKeyword = '';

            const applyFilter = () => {
                const rows = table.querySelectorAll('tbody tr:not(.empty-row)');
                let visibleCount = 0;

                rows.forEach(row => {
                    const rowStatus = row.getAttribute('data-status'); // 'valid', 'error', 'imported', 'skipped'
                    const rowText = row.innerText.toLowerCase();

                    // Điều kiện tab
                    let matchesTab = false;
                    if (currentFilter === 'all') {
                        matchesTab = true;
                    } else if (currentFilter === 'valid' && (rowStatus === 'valid' || rowStatus === 'imported')) {
                        matchesTab = true;
                    } else if (currentFilter === 'error' && (rowStatus === 'error' || rowStatus === 'skipped')) {
                        matchesTab = true;
                    }

                    // Điều kiện từ khóa tìm kiếm
                    const matchesSearch = currentKeyword === '' || rowText.includes(currentKeyword);

                    if (matchesTab && matchesSearch) {
                        row.style.display = '';
                        visibleCount++;
                    } else {
                        row.style.display = 'none';
                    }
                });

                if (emptyNotice) {
                    emptyNotice.style.display = visibleCount === 0 ? 'block' : 'none';
                }
            };

            // Sự kiện chọn tab
            tabButtons.forEach(btn => {
                btn.addEventListener('click', function () {
                    tabButtons.forEach(b => b.classList.remove('active'));
                    this.classList.add('active');
                    currentFilter = this.getAttribute('data-filter') || 'all';
                    applyFilter();
                });
            });

            // Sự kiện tìm kiếm realtime
            if (searchInput) {
                searchInput.addEventListener('input', function () {
                    currentKeyword = this.value.trim().toLowerCase();
                    applyFilter();
                });
            }
        },

        /**
         * 3. Sao chép thông tin tài khoản và mật khẩu tạm
         */
        initCopyCredentials() {
            // Sao chép từng mật khẩu
            const copyButtons = document.querySelectorAll('.btn-copy-pwd');
            copyButtons.forEach(btn => {
                btn.addEventListener('click', function () {
                    const pwd = this.getAttribute('data-pwd');
                    if (!pwd) return;

                    navigator.clipboard.writeText(pwd).then(() => {
                        const originalText = this.innerHTML;
                        this.classList.add('tooltip-copied', 'show');
                        setTimeout(() => {
                            this.classList.remove('show');
                        }, 1800);
                    }).catch(err => {
                        console.error('Không thể sao chép mật khẩu: ', err);
                    });
                });
            });

            // Sao chép toàn bộ danh sách tài khoản đã tạo thành công
            const btnCopyAll = document.getElementById('btn-copy-all-credentials');
            if (btnCopyAll) {
                btnCopyAll.addEventListener('click', function () {
                    const rows = document.querySelectorAll('#bang-ket-qua-excel tbody tr[data-status="imported"]');
                    if (rows.length === 0) {
                        alert('Không có tài khoản nào được nhập thành công để sao chép.');
                        return;
                    }

                    let outputText = 'DANH SÁCH TÀI KHOẢN VỪA KHỞI TẠO TỪ EXCEL:\n';
                    outputText += 'STT\tHọ và tên\tEmail\tMật khẩu tạm\n';

                    rows.forEach((row, idx) => {
                        const name = row.querySelector('.user-name-cell')?.textContent?.trim() || '';
                        const email = row.querySelector('.user-email-cell')?.textContent?.trim() || '';
                        const pwd = row.querySelector('.credential-code')?.textContent?.trim() || 'Theo file tải lên';
                        outputText += `${idx + 1}\t${name}\t${email}\t${pwd}\n`;
                    });

                    navigator.clipboard.writeText(outputText).then(() => {
                        alert(`Đã sao chép danh sách ${rows.length} tài khoản vào khay nhớ tạm (Clipboard) thành công!`);
                    }).catch(err => {
                        console.error('Lỗi khi sao chép toàn bộ tài khoản: ', err);
                    });
                });
            }
        },

        /**
         * 4. Modal xác nhận thông minh khi bấm "Thực hiện nhập" / "Tiến hành nhập dữ liệu" (Story S2-01)
         */
        initConfirmModal() {
            const formImport = document.getElementById('form-import-excel');
            const formThucHien = document.getElementById('form-thuc-hien-nhap');
            const btnNhapCard2 = document.getElementById('btn-nhap-du-lieu');
            const btnThucHienTop = document.getElementById('btn-thuc-hien-nhap');
            const btnThucHienBottom = document.getElementById('btn-thuc-hien-nhap-bottom');
            const modal = document.getElementById('modal-xac-nhan-nhap');
            const btnModalConfirm = document.getElementById('modal-btn-confirm');
            const btnModalCancel = document.getElementById('modal-btn-cancel');

            if (!modal) return;

            let targetFormToSubmit = formThucHien || formImport;

            const moModalXacNhan = (formTarget) => {
                targetFormToSubmit = formTarget;

                // Lấy thông tin thống kê số dòng hợp lệ / lỗi
                const statsValid = document.getElementById('stat-num-valid');
                const statsError = document.getElementById('stat-num-error');

                const countValid = statsValid ? parseInt(statsValid.textContent.trim(), 10) : null;
                const countError = statsError ? parseInt(statsError.textContent.trim(), 10) : 0;

                if (countValid !== null && countValid === 0) {
                    alert('Tệp Excel hiện tại không có dòng nào hợp lệ để nhập. Vui lòng kiểm tra lại báo cáo lỗi và cập nhật tệp trước khi nhập.');
                    return;
                }

                // Cập nhật số liệu hiển thị trong modal
                const modalValidEl = document.getElementById('modal-count-valid');
                const modalErrorEl = document.getElementById('modal-count-error');

                if (modalValidEl && countValid !== null) modalValidEl.textContent = countValid;
                if (modalErrorEl && countError !== null) modalErrorEl.textContent = countError;

                modal.style.display = 'flex';
            };

            // Sự kiện khi bấm "Thực hiện nhập" ngay trên Preview (Review Fix S2-01)
            [btnThucHienTop, btnThucHienBottom].forEach(btn => {
                if (btn) {
                    btn.addEventListener('click', (e) => {
                        e.preventDefault();
                        if (btn.disabled) return;
                        moModalXacNhan(formThucHien || formImport);
                    });
                }
            });

            // Sự kiện khi bấm "Tiến hành nhập dữ liệu" ở Card 2
            if (btnNhapCard2) {
                btnNhapCard2.removeAttribute('onclick');
                btnNhapCard2.addEventListener('click', (e) => {
                    e.preventDefault();
                    if (formThucHien) {
                        moModalXacNhan(formThucHien);
                        return;
                    }
                    const fileInput = document.getElementById('fileExcel');
                    if (!fileInput || !fileInput.files || fileInput.files.length === 0) {
                        alert('Vui lòng chọn một tệp Excel (.xlsx hoặc .xls) trước khi bấm nhập.');
                        return;
                    }
                    moModalXacNhan(formImport);
                });
            }

            if (btnModalCancel) {
                btnModalCancel.addEventListener('click', () => {
                    modal.style.display = 'none';
                });
            }

            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    modal.style.display = 'none';
                }
            });

            if (btnModalConfirm) {
                btnModalConfirm.addEventListener('click', () => {
                    modal.style.display = 'none';

                    // Chống double click và hiển thị loading
                    btnModalConfirm.disabled = true;
                    [btnThucHienTop, btnThucHienBottom, btnNhapCard2].forEach(b => {
                        if (b) {
                            b.disabled = true;
                            b.innerHTML = '&#9203; Đang nhập dữ liệu...';
                        }
                    });

                    if (targetFormToSubmit) {
                        // Đảm bảo action là nhap-du-lieu
                        let actionInput = targetFormToSubmit.querySelector('input[name="action"]');
                        if (!actionInput) {
                            actionInput = document.createElement('input');
                            actionInput.type = 'hidden';
                            actionInput.name = 'action';
                            targetFormToSubmit.appendChild(actionInput);
                        }
                        actionInput.value = 'nhap-du-lieu';
                        targetFormToSubmit.submit();
                    }
                });
            }
        },

        /**
         * 5. Hiển thị loading và ngăn chặn submit lặp lại
         */
        initFormSubmitState() {
            const formImport = document.getElementById('form-import-excel');
            const formThucHien = document.getElementById('form-thuc-hien-nhap');

            if (formImport) {
                formImport.addEventListener('submit', function () {
                    const btnXemTruoc = document.getElementById('btn-xem-truoc');
                    const btnNhap = document.getElementById('btn-nhap-du-lieu');

                    if (btnXemTruoc) {
                        btnXemTruoc.disabled = true;
                        btnXemTruoc.innerHTML = '&#9203; Đang phân tích tệp...';
                    }
                    if (btnNhap) {
                        btnNhap.disabled = true;
                        btnNhap.innerHTML = '&#9203; Đang nhập dữ liệu...';
                    }
                });
            }

            if (formThucHien) {
                formThucHien.addEventListener('submit', function () {
                    const btns = [
                        document.getElementById('btn-thuc-hien-nhap'),
                        document.getElementById('btn-thuc-hien-nhap-bottom'),
                        document.getElementById('modal-btn-confirm')
                    ];
                    btns.forEach(b => {
                        if (b) {
                            b.disabled = true;
                            b.innerHTML = '&#9203; Đang nhập dữ liệu...';
                        }
                    });
                });
            }
        },

        /**
         * 6. In và lưu báo cáo
         */
        initPrintReport() {
            const btnPrint = document.getElementById('btn-print-report');
            if (btnPrint) {
                btnPrint.addEventListener('click', () => {
                    window.print();
                });
            }
        }
    };

    // Khởi tạo khi DOM sẵn sàng
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => NguoiDungImport.init());
    } else {
        NguoiDungImport.init();
    }
})();
