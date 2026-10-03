/**
 * JavaScript xử lý tương tác module Hồ sơ và Tải lên ảnh đại diện (Story S2-03)
 * Tuân thủ quy định: Vanilla JavaScript thuần, tối ưu tương tác trên màn hình 360px.
 */

document.addEventListener("DOMContentLoaded", function () {
    const MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB tối đa (Acceptance Criteria)
    const ALLOWED_EXTENSIONS = ["jpg", "jpeg", "png"];

    // DOM Elements
    const avatarInput = document.getElementById("avatarInput");
    const uploadDropzone = document.getElementById("uploadDropzone");
    const avatarPreviewLarge = document.getElementById("avatarPreviewLarge");
    const avatarPreviewThumb = document.getElementById("avatarPreviewThumb");
    const avatarPreviewHeader = document.getElementById("avatarPreviewHeader");
    const selectedFileInfo = document.getElementById("selectedFileInfo");
    const fileNameSpan = document.getElementById("fileName");
    const fileSizeSpan = document.getElementById("fileSize");
    const clientErrorBox = document.getElementById("clientErrorBox");
    const btnSubmitAvatar = document.getElementById("btnSubmitAvatar");
    const uploadForm = document.getElementById("avatarUploadForm");

    // Sidebar Mobile Toggle
    const sidebarToggleBtn = document.getElementById("sidebarToggleBtn");
    const crmSidebar = document.getElementById("crmSidebar");
    const sidebarBackdrop = document.getElementById("sidebarBackdrop");

    if (sidebarToggleBtn && crmSidebar && sidebarBackdrop) {
        sidebarToggleBtn.addEventListener("click", function () {
            crmSidebar.classList.toggle("open");
            sidebarBackdrop.classList.toggle("active");
        });

        sidebarBackdrop.addEventListener("click", function () {
            crmSidebar.classList.remove("open");
            sidebarBackdrop.classList.remove("active");
        });
    }

    if (!avatarInput) return;

    // Drag and Drop Effects
    ["dragenter", "dragover"].forEach(eventName => {
        uploadDropzone.addEventListener(eventName, function (e) {
            e.preventDefault();
            e.stopPropagation();
            uploadDropzone.classList.add("drag-over");
        }, false);
    });

    ["dragleave", "drop"].forEach(eventName => {
        uploadDropzone.addEventListener(eventName, function (e) {
            e.preventDefault();
            e.stopPropagation();
            uploadDropzone.classList.remove("drag-over");
        }, false);
    });

    uploadDropzone.addEventListener("drop", function (e) {
        const dt = e.dataTransfer;
        const files = dt.files;
        if (files && files.length > 0) {
            avatarInput.files = files;
            xuLyChonFile(files[0]);
        }
    });

    avatarInput.addEventListener("change", function () {
        if (this.files && this.files.length > 0) {
            xuLyChonFile(this.files[0]);
        }
    });

    function xuLyChonFile(file) {
        anThongBaoLoi();

        // 1. Kiểm tra định dạng đuôi file
        const fileName = file.name;
        const fileExt = fileName.split(".").pop().toLowerCase();
        if (!ALLOWED_EXTENSIONS.includes(fileExt)) {
            hienThongBaoLoi("Định dạng file ." + fileExt + " không được hỗ trợ. Chỉ chấp nhận ảnh JPG, JPEG hoặc PNG.");
            datLaiForm();
            return;
        }

        // 2. Kiểm tra dung lượng tối đa 2MB (Acceptance Criteria)
        if (file.size > MAX_FILE_SIZE) {
            const sizeInMb = (file.size / (1024 * 1024)).toFixed(2);
            hienThongBaoLoi("Dung lượng ảnh (" + sizeInMb + " MB) vượt quá giới hạn tối đa 2MB cho phép.");
            datLaiForm();
            return;
        }

        if (file.size <= 0) {
            hienThongBaoLoi("File ảnh đã chọn rỗng hoặc bị lỗi.");
            datLaiForm();
            return;
        }

        // 3. Hiển thị thông tin file được chọn
        const sizeKb = (file.size / 1024).toFixed(1);
        fileNameSpan.textContent = fileName;
        fileSizeSpan.textContent = sizeKb + " KB";
        selectedFileInfo.style.display = "block";
        btnSubmitAvatar.disabled = false;

        // 4. Preview ảnh trực tiếp bằng FileReader
        const reader = new FileReader();
        reader.onload = function (e) {
            const imgSrc = e.target.result;
            if (avatarPreviewLarge) avatarPreviewLarge.src = imgSrc;
            if (avatarPreviewThumb) avatarPreviewThumb.src = imgSrc;
        };
        reader.readAsDataURL(file);
    }

    function hienThongBaoLoi(msg) {
        if (clientErrorBox) {
            clientErrorBox.textContent = msg;
            clientErrorBox.style.display = "block";
        }
    }

    function anThongBaoLoi() {
        if (clientErrorBox) {
            clientErrorBox.textContent = "";
            clientErrorBox.style.display = "none";
        }
    }

    function datLaiForm() {
        avatarInput.value = "";
        selectedFileInfo.style.display = "none";
        btnSubmitAvatar.disabled = true;
    }

    // Submit form với trạng thái loading
    if (uploadForm) {
        uploadForm.addEventListener("submit", function () {
            btnSubmitAvatar.disabled = true;
            btnSubmitAvatar.innerHTML = '<span class="spinner-border"></span> Đang tải lên và xử lý ảnh...';
        });
    }
});
