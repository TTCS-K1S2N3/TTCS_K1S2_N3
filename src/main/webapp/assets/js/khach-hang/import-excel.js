/**
 * Xử lý tương tác giao diện tính năng Nhập danh sách khách hàng từ Excel (Story S3-06).
 * - Kéo thả tệp và kiểm tra định dạng/dung lượng client-side
 * - Bộ lọc bản ghi xem trước: Tất cả / Hợp lệ / Bị trùng / Có lỗi
 * - Xử lý trùng lặp hàng loạt (Đồng bộ lựa chọn Bỏ qua / Cập nhật cho toàn bộ bản ghi trùng)
 */
(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        initDropzone();
        initFilterTabs();
        initBatchDuplicateAction();
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

        if (!dropzone || !fileInput) return;

        // Click vào dropzone để mở dialog chọn file
        dropzone.addEventListener('click', function (e) {
            if (e.target !== fileInput) {
                fileInput.click();
            }
        });

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
                if (btnPreview) btnPreview.disabled = true;
                return;
            }

            // Giới hạn 10MB
            if (size > 10 * 1024 * 1024) {
                alert('Dung lượng tệp vượt quá giới hạn 10MB! Vui lòng chọn tệp nhỏ hơn.');
                fileInput.value = '';
                if (fileInfo) fileInfo.style.display = 'none';
                if (btnPreview) btnPreview.disabled = true;
                return;
            }

            if (fileNameDisplay) fileNameDisplay.textContent = name;
            if (fileSizeDisplay) fileSizeDisplay.textContent = formatFileSize(size);
            if (fileInfo) fileInfo.style.display = 'flex';
            if (btnPreview) btnPreview.disabled = false;
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
        var rows = document.querySelectorAll('.preview-table tbody tr');

        if (!tabBtns || tabBtns.length === 0 || !rows || rows.length === 0) return;

        tabBtns.forEach(function (btn) {
            btn.addEventListener('click', function () {
                var filter = btn.getAttribute('data-filter') || 'all';

                // Cập nhật active tab
                tabBtns.forEach(function (b) { b.classList.remove('active'); });
                btn.classList.add('active');

                // Lọc các dòng
                rows.forEach(function (row) {
                    var isError = row.classList.contains('row-error');
                    var isDuplicate = row.classList.contains('row-duplicate');
                    var isValid = !isError;

                    if (filter === 'all') {
                        row.style.display = '';
                    } else if (filter === 'valid') {
                        row.style.display = isValid ? '' : 'none';
                    } else if (filter === 'duplicate') {
                        row.style.display = isDuplicate ? '' : 'none';
                    } else if (filter === 'error') {
                        row.style.display = isError ? '' : 'none';
                    }
                });
            });
        });
    }

    /**
     * Khởi tạo bộ chọn xử lý trùng lặp hàng loạt
     */
    function initBatchDuplicateAction() {
        var batchSelect = document.getElementById('selectBatchDuplicateAction');
        var rowSelects = document.querySelectorAll('.row-action-select');

        if (!batchSelect) return;

        batchSelect.addEventListener('change', function () {
            var selectedAction = batchSelect.value; // "BO_QUA" hoặc "CAP_NHAT"
            var inputChung = document.getElementById('inputXuLyTrungLapChung');
            if (inputChung) {
                inputChung.value = selectedAction;
            }
            if (rowSelects && rowSelects.length > 0) {
                rowSelects.forEach(function (select) {
                    select.value = selectedAction;
                });
            }
        });
    }
})();
