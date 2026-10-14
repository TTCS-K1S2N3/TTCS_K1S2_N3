/**
 * import-excel.js - Xử lý Kéo thả tệp, Xem trước & Thẩm định lỗi từng dòng cho Nhập Lead từ Excel
 * Story S4-02: AC2 (Tệp mẫu, xem trước & báo lỗi từng dòng) & AC3 (Bắt buộc có nguồn)
 * Dự án: MSys CRM - Lead & Phân bổ
 */

(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        initDropzone();
        initFilterTabs();
        initFormSubmissions();
    });

    /**
     * Khởi tạo tính năng kéo thả tệp Excel (Dropzone)
     */
    function initDropzone() {
        const dropzone = document.getElementById('dropzoneExcel');
        const fileInput = document.getElementById('fileExcel');
        const fileInfo = document.getElementById('selectedFileInfo');
        const fileNameSpan = document.getElementById('selectedFileName');
        const fileSizeSpan = document.getElementById('selectedFileSize');
        const btnClear = document.getElementById('btnClearFile');
        const btnSubmitPreview = document.getElementById('btnSubmitPreview');

        if (!dropzone || !fileInput) return;

        // Click vào dropzone để mở dialog chọn file
        dropzone.addEventListener('click', function (e) {
            if (e.target.closest('#btnClearFile')) return;
            fileInput.click();
        });

        // Hỗ trợ phím Enter/Space khi focus vào dropzone
        dropzone.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                fileInput.click();
            }
        });

        // Drag & drop events
        ['dragenter', 'dragover'].forEach(function (eventName) {
            dropzone.addEventListener(eventName, function (e) {
                e.preventDefault();
                e.stopPropagation();
                dropzone.classList.add('dragover');
            });
        });

        ['dragleave', 'drop'].forEach(function (eventName) {
            dropzone.addEventListener(eventName, function (e) {
                e.preventDefault();
                e.stopPropagation();
                dropzone.classList.remove('dragover');
            });
        });

        dropzone.addEventListener('drop', function (e) {
            const dt = e.dataTransfer;
            const files = dt.files;
            if (files && files.length > 0) {
                handleFileSelect(files[0]);
            }
        });

        fileInput.addEventListener('change', function () {
            if (fileInput.files && fileInput.files.length > 0) {
                handleFileSelect(fileInput.files[0]);
            }
        });

        function handleFileSelect(file) {
            // Kiểm tra phần mở rộng file
            const fileName = file.name;
            const ext = fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
            if (ext !== '.xlsx' && ext !== '.xls') {
                alert('Vui lòng chọn tệp định dạng Excel (.xlsx hoặc .xls).');
                fileInput.value = '';
                return;
            }

            // Kiểm tra dung lượng (10MB)
            const MAX_SIZE = 10 * 1024 * 1024;
            if (file.size > MAX_SIZE) {
                alert('Dung lượng tệp vượt quá 10MB cho phép. Vui lòng kiểm tra lại.');
                fileInput.value = '';
                return;
            }

            // Hiển thị thông tin file đã chọn
            if (fileNameSpan) fileNameSpan.textContent = fileName;
            if (fileSizeSpan) fileSizeSpan.textContent = '(' + formatFileSize(file.size) + ')';
            if (fileInfo) fileInfo.style.display = 'flex';
            if (btnSubmitPreview) btnSubmitPreview.disabled = false;
        }

        if (btnClear) {
            btnClear.addEventListener('click', function (e) {
                e.stopPropagation();
                fileInput.value = '';
                if (fileInfo) fileInfo.style.display = 'none';
                if (fileNameSpan) fileNameSpan.textContent = '';
                if (fileSizeSpan) fileSizeSpan.textContent = '';
                if (btnSubmitPreview) btnSubmitPreview.disabled = true;
            });
        }
    }

    /**
     * Định dạng kích thước file sang KB / MB
     */
    function formatFileSize(bytes) {
        if (bytes < 1024) return bytes + ' bytes';
        else if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
        else return (bytes / 1048576).toFixed(1) + ' MB';
    }

    /**
     * Khởi tạo các tab bộ lọc danh sách xem trước (Tất cả, Hợp lệ, Có lỗi)
     */
    function initFilterTabs() {
        const tabBtns = document.querySelectorAll('.lead-tab-btn');
        const rows = document.querySelectorAll('.lead-preview-table tbody tr:not(#previewEmptyRow)');
        const emptyRow = document.getElementById('previewEmptyRow');

        if (!tabBtns.length) return;

        tabBtns.forEach(function (btn) {
            btn.addEventListener('click', function () {
                tabBtns.forEach(function (b) { b.classList.remove('active'); });
                btn.classList.add('active');

                const filter = btn.getAttribute('data-filter');
                let visibleCount = 0;

                rows.forEach(function (row) {
                    if (filter === 'all') {
                        row.style.display = '';
                        visibleCount++;
                    } else if (filter === 'valid') {
                        if (row.classList.contains('row-valid')) {
                            row.style.display = '';
                            visibleCount++;
                        } else {
                            row.style.display = 'none';
                        }
                    } else if (filter === 'error') {
                        if (row.classList.contains('row-error')) {
                            row.style.display = '';
                            visibleCount++;
                        } else {
                            row.style.display = 'none';
                        }
                    }
                });

                if (emptyRow) {
                    emptyRow.style.display = visibleCount === 0 ? '' : 'none';
                }
            });
        });
    }

    /**
     * Quản lý hành vi submit và chống spam click
     */
    function initFormSubmissions() {
        const formUpload = document.getElementById('formUploadExcel');
        const formConfirm = document.getElementById('formConfirmImport');

        if (formUpload) {
            formUpload.addEventListener('submit', function (e) {
                const fileInput = document.getElementById('fileExcel');
                if (!fileInput || !fileInput.files || fileInput.files.length === 0) {
                    e.preventDefault();
                    alert('Vui lòng chọn một tệp Excel trước khi bấm Xem trước dữ liệu.');
                    return false;
                }
                const btn = document.getElementById('btnSubmitPreview');
                if (btn) {
                    btn.disabled = true;
                    btn.innerHTML = '<span class="material-symbols-outlined" style="animation: spin 1s linear infinite;">sync</span> Đang đọc tệp...';
                }
            });
        }

        if (formConfirm) {
            formConfirm.addEventListener('submit', function () {
                const btn = document.getElementById('btnConfirmImport');
                if (btn) {
                    btn.disabled = true;
                    btn.innerHTML = '<span class="material-symbols-outlined" style="animation: spin 1s linear infinite;">sync</span> Đang lưu vào hệ thống...';
                }
            });
        }
    }
})();
