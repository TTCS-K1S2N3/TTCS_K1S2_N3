/**
 * Xử lý tương tác giao diện tính năng Nhập danh sách khách hàng từ Excel (Story S3-06).
 * - Kéo thả tệp và kiểm tra định dạng/dung lượng client-side
 * - Xóa tệp đã chọn và hỗ trợ phím Enter/Space cho vùng dropzone (Accessibility)
 * - Bộ lọc bản ghi xem trước: Tất cả / Hợp lệ / Bị trùng / Có lỗi, kèm hàng thông báo rỗng
 * - Xử lý trùng lặp hàng loạt và từng dòng (Đồng bộ lựa chọn Bỏ qua / Cập nhật, đổi kiểu hiển thị)
 * - Hiệu ứng xoay và khóa nút khi submit form tránh gửi trùng
 */
(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        initDropzone();
        initFilterTabs();
        initBatchDuplicateAction();
        initFormSubmissions();
    });

    /**
     * Khởi tạo khu vực tải tệp (Dropzone & File Input)
     */
    function initDropzone() {
        var dropzone = document.getElementById('dropzoneExcel');
        var fileInput = document.getElementById('fileExcel');
        var fileInfo = document.getElementById('selectedFileInfo');
        var fileNameDisplay = document.getElementById('selectedFileName');
        var fileSizeDisplay = document.getElementById('selectedFileSize');
        var btnPreview = document.getElementById('btnSubmitPreview');
        var btnClear = document.getElementById('btnClearFile');

        if (!dropzone || !fileInput) return;

        // Click vào dropzone để mở dialog chọn file
        dropzone.addEventListener('click', function (e) {
            if (e.target !== fileInput && !e.target.closest('#btnClearFile')) {
                fileInput.click();
            }
        });

        // Hỗ trợ truy cập bàn phím (Enter hoặc Space)
        dropzone.addEventListener('keydown', function (e) {
            if ((e.key === 'Enter' || e.key === ' ') && !e.target.closest('#btnClearFile')) {
                e.preventDefault();
                fileInput.click();
            }
        });

        // Nút hủy/xóa tệp đã chọn
        if (btnClear) {
            btnClear.addEventListener('click', function (e) {
                e.preventDefault();
                e.stopPropagation();
                fileInput.value = '';
                if (fileInfo) fileInfo.style.display = 'none';
                if (fileNameDisplay) fileNameDisplay.textContent = 'Chưa có tệp nào được chọn';
                if (fileSizeDisplay) fileSizeDisplay.textContent = '';
                if (btnPreview) btnPreview.disabled = false;
            });
        }

        // Bắt sự kiện kéo thả (Drag and drop)
        ['dragenter', 'dragover'].forEach(function (eventName) {
            dropzone.addEventListener(eventName, function (e) {
                e.preventDefault();
                e.stopPropagation();
                dropzone.classList.add('drag-over');
            });
        });

        ['dragleave', 'drop'].forEach(function (eventName) {
            dropzone.addEventListener(eventName, function (e) {
                e.preventDefault();
                e.stopPropagation();
                dropzone.classList.remove('drag-over');
            });
        });

        dropzone.addEventListener('drop', function (e) {
            var files = e.dataTransfer.files;
            if (files && files.length > 0) {
                fileInput.files = files;
                handleFileSelection(files[0]);
            }
        });

        fileInput.addEventListener('change', function () {
            if (fileInput.files && fileInput.files.length > 0) {
                handleFileSelection(fileInput.files[0]);
            }
        });

        function handleFileSelection(file) {
            if (!file) return;

            var name = file.name || '';
            var size = file.size || 0;
            var ext = name.toLowerCase().split('.').pop();

            if (ext !== 'xlsx' && ext !== 'xls') {
                alert('Định dạng tệp không hợp lệ! Vui lòng chọn tệp bảng tính Excel (.xlsx hoặc .xls).');
                fileInput.value = '';
                if (fileInfo) fileInfo.style.display = 'none';
                return;
            }

            // Giới hạn 10MB
            if (size > 10 * 1024 * 1024) {
                alert('Dung lượng tệp vượt quá giới hạn 10MB! Vui lòng chọn tệp nhỏ hơn.');
                fileInput.value = '';
                if (fileInfo) fileInfo.style.display = 'none';
                return;
            }

            if (fileNameDisplay) fileNameDisplay.textContent = name;
            if (fileSizeDisplay) fileSizeDisplay.textContent = formatFileSize(size);
            if (fileInfo) fileInfo.style.display = 'flex';
        }

        function formatFileSize(bytes) {
            if (bytes === 0) return '0 B';
            var k = 1024;
            var sizes = ['B', 'KB', 'MB', 'GB'];
            var i = Math.floor(Math.log(bytes) / Math.log(k));
            return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
        }
    }

    /**
     * Khởi tạo bộ lọc bản ghi xem trước (Tabs filter)
     */
    function initFilterTabs() {
        var tabBtns = document.querySelectorAll('.filter-tab-btn');
        var rows = document.querySelectorAll('.preview-table tbody tr:not(#previewEmptyFilterRow)');
        var emptyRow = document.getElementById('previewEmptyFilterRow');

        if (!tabBtns || tabBtns.length === 0 || !rows || rows.length === 0) return;

        tabBtns.forEach(function (btn) {
            btn.addEventListener('click', function () {
                var filter = btn.getAttribute('data-filter') || 'all';

                // Cập nhật active tab
                tabBtns.forEach(function (b) { b.classList.remove('active'); });
                btn.classList.add('active');

                var visibleCount = 0;

                // Lọc các dòng
                rows.forEach(function (row) {
                    var isError = row.classList.contains('row-error');
                    var isDuplicate = row.classList.contains('row-duplicate');
                    var isValid = !isError;
                    var show = false;

                    if (filter === 'all') {
                        show = true;
                    } else if (filter === 'valid') {
                        show = isValid;
                    } else if (filter === 'duplicate') {
                        show = isDuplicate;
                    } else if (filter === 'error') {
                        show = isError;
                    }

                    if (show) {
                        row.style.display = '';
                        visibleCount++;
                    } else {
                        row.style.display = 'none';
                    }
                });

                if (emptyRow) {
                    emptyRow.style.display = (visibleCount === 0) ? '' : 'none';
                }
            });
        });
    }

    /**
     * Khởi tạo bộ chọn xử lý trùng lặp hàng loạt và từng dòng (AC 2)
     */
    function initBatchDuplicateAction() {
        var batchSelect = document.getElementById('selectBatchDuplicateAction');
        var rowSelects = document.querySelectorAll('.row-action-select');
        var inputChung = document.getElementById('inputXuLyTrungLapChung');

        if (!rowSelects || rowSelects.length === 0) return;

        // Cập nhật kiểu dáng cho select từng dòng theo giá trị
        function capNhatKieuDangSelect(select) {
            if (select.value === 'CAP_NHAT') {
                select.classList.add('action-update');
                select.classList.remove('action-skip');
            } else {
                select.classList.add('action-skip');
                select.classList.remove('action-update');
            }
        }

        // Khởi tạo kiểu ban đầu
        rowSelects.forEach(function (select) {
            capNhatKieuDangSelect(select);

            // Bắt sự kiện khi đổi từng dòng lẻ
            select.addEventListener('change', function () {
                capNhatKieuDangSelect(select);
            });
        });

        // Xử lý khi đổi lựa chọn hàng loạt
        if (batchSelect) {
            batchSelect.addEventListener('change', function () {
                var selectedAction = batchSelect.value; // "BO_QUA" hoặc "CAP_NHAT"
                if (inputChung) {
                    inputChung.value = selectedAction;
                }
                rowSelects.forEach(function (select) {
                    select.value = selectedAction;
                    capNhatKieuDangSelect(select);
                });
            });
        }
    }

    /**
     * Xử lý xác thực và hiệu ứng khi submit form
     */
    function initFormSubmissions() {
        var formUpload = document.getElementById('formUploadExcel');
        var fileInput = document.getElementById('fileExcel');
        var dropzone = document.getElementById('dropzoneExcel');
        var btnPreview = document.getElementById('btnSubmitPreview');

        if (formUpload && fileInput) {
            formUpload.addEventListener('submit', function (e) {
                if (!fileInput.files || fileInput.files.length === 0) {
                    e.preventDefault();
                    alert('Vui lòng chọn một tệp Excel (.xlsx hoặc .xls) từ máy tính của bạn trước khi bấm xem trước!');
                    if (dropzone) {
                        dropzone.classList.add('dropzone-error');
                        setTimeout(function () {
                            dropzone.classList.remove('dropzone-error');
                        }, 2500);
                    }
                    return false;
                }

                if (btnPreview) {
                    btnPreview.disabled = true;
                    btnPreview.innerHTML = '<span class="material-symbols-outlined icon-spin" aria-hidden="true">progress_activity</span> Đang phân tích tệp...';
                }
            });
        }

        var formConfirm = document.getElementById('formConfirmImport');
        var btnConfirm = document.getElementById('btnConfirmImport');

        if (formConfirm && btnConfirm) {
            formConfirm.addEventListener('submit', function () {
                btnConfirm.disabled = true;
                btnConfirm.innerHTML = '<span class="material-symbols-outlined icon-spin" aria-hidden="true">progress_activity</span> Đang xử lý nhập dữ liệu...';
            });
        }
    }
})();
