/**
 * JavaScript xử lý tương tác module Hồ sơ & Tải lên ảnh đại diện (Story S2-03)
 * Người làm: Bàn Thị Linh (FE)
 * Acceptance Criteria:
 * - Chấp nhận JPG/PNG tối đa 2MB
 * - Ảnh được cắt vuông và tạo bản thu nhỏ (thumbnail)
 * - Tối ưu tương tác mượt mà trên mọi thiết bị (đặc biệt màn hình 360px)
 */

document.addEventListener("DOMContentLoaded", function () {
    const MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB tối đa (Acceptance Criteria)
    const ALLOWED_EXTENSIONS = ["jpg", "jpeg", "png"];

    // DOM Elements - Form & Dropzone
    const avatarInput = document.getElementById("avatarInput");
    const uploadDropzone = document.getElementById("uploadDropzone");
    const btnTriggerFileSelect = document.getElementById("btnTriggerFileSelect");
    const selectedFileInfo = document.getElementById("selectedFileInfo");
    const fileNameSpan = document.getElementById("fileName");
    const fileSizeSpan = document.getElementById("fileSize");
    const btnClearFile = document.getElementById("btnClearFile");
    const clientErrorBox = document.getElementById("clientErrorBox");
    const clientErrorText = document.getElementById("clientErrorText");
    const btnOpenCropModal = document.getElementById("btnOpenCropModal");
    const btnSubmitDirect = document.getElementById("btnSubmitDirect");
    const uploadForm = document.getElementById("avatarUploadForm");

    // DOM Elements - Previews on main page
    const avatarPreviewLarge = document.getElementById("avatarPreviewLarge");
    const avatarPreviewThumb = document.getElementById("avatarPreviewThumb");
    const avatarPreviewThumbMini = document.getElementById("avatarPreviewThumbMini");
    const avatarPreviewHeader = document.getElementById("avatarPreviewHeader");

    // DOM Elements - Crop Modal & Canvas
    const cropModal = document.getElementById("cropModal");
    const cropModalBackdrop = document.getElementById("cropModalBackdrop");
    const btnCloseCropModal = document.getElementById("btnCloseCropModal");
    const btnCancelCrop = document.getElementById("btnCancelCrop");
    const btnConfirmCropAndUpload = document.getElementById("btnConfirmCropAndUpload");
    const btnConfirmText = document.getElementById("btnConfirmText");
    const cropCanvasWrapper = document.getElementById("cropCanvasWrapper");
    const cropCanvas = document.getElementById("cropCanvas");
    const previewSquareCanvas = document.getElementById("previewSquareCanvas");
    const previewSquareRectCanvas = document.getElementById("previewSquareRectCanvas");
    const previewThumbCanvas = document.getElementById("previewThumbCanvas");
    const zoomSlider = document.getElementById("zoomSlider");
    const btnZoomIn = document.getElementById("btnZoomIn");
    const btnZoomOut = document.getElementById("btnZoomOut");
    const btnRotateLeft = document.getElementById("btnRotateLeft");
    const btnRotateRight = document.getElementById("btnRotateRight");
    const btnResetCrop = document.getElementById("btnResetCrop");
    const cropFormatLabel = document.getElementById("cropFormatLabel");
    const cropEstimatedSize = document.getElementById("cropEstimatedSize");

    // DOM Elements - Sidebar Mobile
    const sidebarToggleBtn = document.getElementById("sidebarToggleBtn");
    const sidebarCloseBtn = document.getElementById("sidebarCloseBtn");
    const crmSidebar = document.getElementById("crmSidebar");
    const sidebarBackdrop = document.getElementById("sidebarBackdrop");

    // State Variables
    let selectedRawFile = null;
    let loadedImage = null;
    let imageFileType = "image/jpeg";
    let imageFileName = "avatar.jpg";

    // Canvas Cropper State
    let scale = 1.0;
    let baseScale = 1.0;
    let posX = 0;
    let posY = 0;
    let rotation = 0; // 0, 90, 180, 270
    let isDragging = false;
    let dragStartX = 0;
    let dragStartY = 0;
    let initialPosX = 0;
    let initialPosY = 0;

    // ==========================================================================
    // 1. SIDEBAR MOBILE NAVIGATION
    // ==========================================================================
    if (sidebarToggleBtn && crmSidebar && sidebarBackdrop) {
        sidebarToggleBtn.addEventListener("click", function () {
            crmSidebar.classList.add("open");
            sidebarBackdrop.classList.add("active");
        });

        if (sidebarCloseBtn) {
            sidebarCloseBtn.addEventListener("click", dongSidebarMobile);
        }

        sidebarBackdrop.addEventListener("click", dongSidebarMobile);
    }

    function dongSidebarMobile() {
        if (crmSidebar) crmSidebar.classList.remove("open");
        if (sidebarBackdrop) sidebarBackdrop.classList.remove("active");
    }

    if (!avatarInput) return;

    // ==========================================================================
    // 2. DRAG AND DROP & FILE SELECTION
    // ==========================================================================
    if (btnTriggerFileSelect) {
        btnTriggerFileSelect.addEventListener("click", function () {
            avatarInput.click();
        });
    }

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
            xuLyKiemTraFile(files[0]);
        }
    });

    avatarInput.addEventListener("change", function () {
        if (this.files && this.files.length > 0) {
            xuLyKiemTraFile(this.files[0]);
        }
    });

    if (btnClearFile) {
        btnClearFile.addEventListener("click", datLaiForm);
    }

    // ==========================================================================
    // 3. CLIENT-SIDE VALIDATION (AC: JPG/PNG tối đa 2MB)
    // ==========================================================================
    function xuLyKiemTraFile(file) {
        anThongBaoLoi();

        if (!file) {
            hienThongBaoLoi("Vui lòng chọn một file ảnh để tải lên.");
            datLaiForm();
            return;
        }

        const fileName = file.name || "";
        const fileExt = fileName.split(".").pop().toLowerCase();

        // 1. Kiểm tra định dạng (Acceptance Criteria: Chấp nhận JPG/PNG)
        if (!ALLOWED_EXTENSIONS.includes(fileExt)) {
            hienThongBaoLoi("Định dạng file ." + fileExt + " không được hỗ trợ. Hệ thống chỉ chấp nhận ảnh JPG, JPEG hoặc PNG.");
            showToast("error", "Chỉ chấp nhận định dạng ảnh JPG hoặc PNG.");
            datLaiForm();
            return;
        }

        // 2. Kiểm tra dung lượng (Acceptance Criteria: Tối đa 2MB)
        if (file.size > MAX_FILE_SIZE) {
            const sizeInMb = (file.size / (1024 * 1024)).toFixed(2);
            hienThongBaoLoi("Dung lượng ảnh (" + sizeInMb + " MB) vượt quá giới hạn tối đa 2MB cho phép.");
            showToast("error", "Dung lượng ảnh vượt quá giới hạn 2MB.");
            datLaiForm();
            return;
        }

        if (file.size <= 0) {
            hienThongBaoLoi("File ảnh đã chọn không có dữ liệu hoặc bị lỗi.");
            datLaiForm();
            return;
        }

        // File hợp lệ
        selectedRawFile = file;
        imageFileName = fileName;
        imageFileType = fileExt === "png" ? "image/png" : "image/jpeg";

        // Cập nhật thông tin file hiển thị
        const sizeFormatted = file.size > 1024 * 1024
            ? (file.size / (1024 * 1024)).toFixed(2) + " MB"
            : (file.size / 1024).toFixed(1) + " KB";

        if (fileNameSpan) fileNameSpan.textContent = fileName;
        if (fileSizeSpan) fileSizeSpan.textContent = sizeFormatted;
        if (selectedFileInfo) selectedFileInfo.style.display = "block";

        if (btnOpenCropModal) btnOpenCropModal.disabled = false;
        if (btnSubmitDirect) btnSubmitDirect.disabled = false;

        // Đọc ảnh vào bộ nhớ để chuẩn bị cắt và xem trước
        const reader = new FileReader();
        reader.onload = function (e) {
            const img = new Image();
            img.onload = function () {
                loadedImage = img;
                // Tự động mở modal cắt vuông để người dùng trải nghiệm ngay (hoặc người dùng bấm nút)
                moModalCatAnh();
            };
            img.src = e.target.result;
        };
        reader.readAsDataURL(file);
    }

    function hienThongBaoLoi(msg) {
        if (clientErrorBox && clientErrorText) {
            clientErrorText.textContent = msg;
            clientErrorBox.style.display = "flex";
        }
    }

    function anThongBaoLoi() {
        if (clientErrorBox && clientErrorText) {
            clientErrorText.textContent = "";
            clientErrorBox.style.display = "none";
        }
    }

    function datLaiForm() {
        avatarInput.value = "";
        selectedRawFile = null;
        loadedImage = null;
        if (selectedFileInfo) selectedFileInfo.style.display = "none";
        if (btnOpenCropModal) btnOpenCropModal.disabled = true;
        if (btnSubmitDirect) btnSubmitDirect.disabled = true;
    }

    // ==========================================================================
    // 4. CANVAS INTERACTIVE CROP ENGINE (AC: Ảnh được cắt vuông & tạo thumbnail)
    // ==========================================================================
    if (btnOpenCropModal) {
        btnOpenCropModal.addEventListener("click", function () {
            if (loadedImage) {
                moModalCatAnh();
            } else if (selectedRawFile) {
                xuLyKiemTraFile(selectedRawFile);
            }
        });
    }

    function moModalCatAnh() {
        if (!loadedImage) return;

        // Reset trạng thái crop
        rotation = 0;
        khoiTaoThongSoCrop();

        if (cropModal && cropModalBackdrop) {
            cropModal.style.display = "block";
            cropModalBackdrop.style.display = "block";
            document.body.style.overflow = "hidden"; // Khóa cuộn trang nền
        }

        if (cropFormatLabel) {
            cropFormatLabel.textContent = imageFileType === "image/png" ? "PNG" : "JPG";
        }

        veCropCanvas();
    }

    function dongModalCatAnh() {
        if (cropModal && cropModalBackdrop) {
            cropModal.style.display = "none";
            cropModalBackdrop.style.display = "none";
            document.body.style.overflow = "";
        }
    }

    if (btnCloseCropModal) btnCloseCropModal.addEventListener("click", dongModalCatAnh);
    if (btnCancelCrop) btnCancelCrop.addEventListener("click", dongModalCatAnh);
    if (cropModalBackdrop) cropModalBackdrop.addEventListener("click", dongModalCatAnh);

    // Phím ESC đóng modal
    document.addEventListener("keydown", function (e) {
        if (e.key === "Escape" && cropModal && cropModal.style.display === "block") {
            dongModalCatAnh();
        }
    });

    function khoiTaoThongSoCrop() {
        if (!loadedImage || !cropCanvas) return;

        const cw = cropCanvas.width;
        const ch = cropCanvas.height;

        // Tính toán tỉ lệ để ảnh bao phủ toàn bộ khung vuông 1:1
        const iw = (rotation % 180 === 0) ? loadedImage.naturalWidth : loadedImage.naturalHeight;
        const ih = (rotation % 180 === 0) ? loadedImage.naturalHeight : loadedImage.naturalWidth;

        baseScale = Math.max(cw / iw, ch / ih);
        scale = baseScale;

        // Đặt vị trí ảnh vào chính giữa khung vuông
        posX = cw / 2;
        posY = ch / 2;

        if (zoomSlider) {
            zoomSlider.min = (baseScale * 0.75).toFixed(2);
            zoomSlider.max = (baseScale * 3.5).toFixed(2);
            zoomSlider.value = scale.toFixed(2);
        }
    }

    function veCropCanvas() {
        if (!loadedImage || !cropCanvas) return;

        const ctx = cropCanvas.getContext("2d");
        const cw = cropCanvas.width;
        const ch = cropCanvas.height;

        ctx.clearRect(0, 0, cw, ch);
        ctx.save();

        // 1. Dịch chuyển tới tâm và áp dụng Scale & Rotate
        ctx.translate(posX, posY);
        ctx.rotate((rotation * Math.PI) / 180);
        ctx.scale(scale, scale);

        // 2. Vẽ ảnh tại tâm
        const iw = loadedImage.naturalWidth;
        const ih = loadedImage.naturalHeight;
        ctx.drawImage(loadedImage, -iw / 2, -ih / 2, iw, ih);

        ctx.restore();

        // 3. Cập nhật các bản xem trước trực tiếp (Live Previews)
        capNhatBaoXemTruoc();
    }

    function capNhatBaoXemTruoc() {
        if (!cropCanvas) return;

        // A. Cập nhật Xem trước Ảnh vuông chuẩn (1:1)
        if (previewSquareCanvas) {
            const ctxSq = previewSquareCanvas.getContext("2d");
            ctxSq.clearRect(0, 0, previewSquareCanvas.width, previewSquareCanvas.height);
            ctxSq.drawImage(cropCanvas, 0, 0, previewSquareCanvas.width, previewSquareCanvas.height);
        }

        if (previewSquareRectCanvas) {
            const ctxRect = previewSquareRectCanvas.getContext("2d");
            ctxRect.clearRect(0, 0, previewSquareRectCanvas.width, previewSquareRectCanvas.height);
            ctxRect.drawImage(cropCanvas, 0, 0, previewSquareRectCanvas.width, previewSquareRectCanvas.height);
        }

        // B. Cập nhật Xem trước Bản thu nhỏ Thumbnail (120x120 -> 48x48)
        if (previewThumbCanvas) {
            const ctxTh = previewThumbCanvas.getContext("2d");
            ctxTh.clearRect(0, 0, previewThumbCanvas.width, previewThumbCanvas.height);
            ctxTh.drawImage(cropCanvas, 0, 0, previewThumbCanvas.width, previewThumbCanvas.height);
        }
    }

    // ==========================================================================
    // 5. TƯƠNG TÁC KÉO THẢ & ZOOM TRÊN WORKSPACE (HỖ TRỢ MOUSE & TOUCH 360PX)
    // ==========================================================================
    if (cropCanvasWrapper) {
        // Chuột (Desktop)
        cropCanvasWrapper.addEventListener("mousedown", batDauKeoAnh);
        window.addEventListener("mousemove", dangKeoAnh);
        window.addEventListener("mouseup", ketThucKeoAnh);

        // Chạm vuốt (Mobile & Tablet - đặc biệt 360px)
        cropCanvasWrapper.addEventListener("touchstart", function (e) {
            if (e.touches.length === 1) {
                const touch = e.touches[0];
                batDauKeoAnh({ clientX: touch.clientX, clientY: touch.clientY, preventDefault: () => e.preventDefault() });
            }
        }, { passive: false });

        window.addEventListener("touchmove", function (e) {
            if (isDragging && e.touches.length === 1) {
                const touch = e.touches[0];
                dangKeoAnh({ clientX: touch.clientX, clientY: touch.clientY, preventDefault: () => e.preventDefault() });
            }
        }, { passive: false });

        window.addEventListener("touchend", ketThucKeoAnh);
    }

    function batDauKeoAnh(e) {
        if (!loadedImage) return;
        if (e.preventDefault) e.preventDefault();
        isDragging = true;
        dragStartX = e.clientX;
        dragStartY = e.clientY;
        initialPosX = posX;
        initialPosY = posY;
    }

    function dangKeoAnh(e) {
        if (!isDragging) return;
        if (e.preventDefault) e.preventDefault();
        const deltaX = e.clientX - dragStartX;
        const deltaY = e.clientY - dragStartY;
        posX = initialPosX + deltaX;
        posY = initialPosY + deltaY;
        veCropCanvas();
    }

    function ketThucKeoAnh() {
        isDragging = false;
    }

    // Điều khiển Zoom slider
    if (zoomSlider) {
        zoomSlider.addEventListener("input", function () {
            scale = parseFloat(this.value);
            veCropCanvas();
        });
    }

    if (btnZoomIn) {
        btnZoomIn.addEventListener("click", function () {
            scale = Math.min(scale * 1.15, parseFloat(zoomSlider.max || "3.5"));
            if (zoomSlider) zoomSlider.value = scale.toFixed(2);
            veCropCanvas();
        });
    }

    if (btnZoomOut) {
        btnZoomOut.addEventListener("click", function () {
            scale = Math.max(scale / 1.15, parseFloat(zoomSlider.min || "0.2"));
            if (zoomSlider) zoomSlider.value = scale.toFixed(2);
            veCropCanvas();
        });
    }

    // Điều khiển Rotate 90 độ
    if (btnRotateRight) {
        btnRotateRight.addEventListener("click", function () {
            rotation = (rotation + 90) % 360;
            veCropCanvas();
        });
    }

    if (btnRotateLeft) {
        btnRotateLeft.addEventListener("click", function () {
            rotation = (rotation - 90 + 360) % 360;
            veCropCanvas();
        });
    }

    if (btnResetCrop) {
        btnResetCrop.addEventListener("click", function () {
            rotation = 0;
            khoiTaoThongSoCrop();
            veCropCanvas();
        });
    }

    // ==========================================================================
    // 6. XUẤT ẢNH VUÔNG VÀ UPLOAD LÊN SERVER (AJAX FETCH HOẶC FALLBACK)
    // ==========================================================================
    if (btnConfirmCropAndUpload) {
        btnConfirmCropAndUpload.addEventListener("click", function () {
            if (!cropCanvas) return;

            // Đặt trạng thái loading
            btnConfirmCropAndUpload.disabled = true;
            if (btnConfirmText) {
                btnConfirmText.innerHTML = '<span class="spinner-border-sm"></span> Đang xử lý & tạo thumbnail...';
            }

            // Tạo offscreen canvas chuẩn vuông 400x400
            const exportSize = 400; // Kích thước avatar chuẩn vuông
            const offscreenCanvas = document.createElement("canvas");
            offscreenCanvas.width = exportSize;
            offscreenCanvas.height = exportSize;
            const offCtx = offscreenCanvas.getContext("2d");

            // Vẽ ảnh đã crop từ cropCanvas sang offscreenCanvas
            offCtx.drawImage(cropCanvas, 0, 0, exportSize, exportSize);

            // Chuyển đổi sang Blob chuẩn JPG hoặc PNG
            const mimeType = imageFileType === "image/png" ? "image/png" : "image/jpeg";
            const quality = 0.92;

            offscreenCanvas.toBlob(function (blob) {
                if (!blob) {
                    showToast("error", "Lỗi tạo file ảnh cắt vuông.");
                    khoiPhucNutUpload();
                    return;
                }

                // Kiểm tra dung lượng sau cắt
                if (blob.size > MAX_FILE_SIZE) {
                    showToast("error", "Ảnh sau khi cắt vượt quá giới hạn 2MB.");
                    khoiPhucNutUpload();
                    return;
                }

                // Tiến hành upload lên server bằng AJAX
                guiFileLenServer(blob, "avatar_cropped." + (mimeType === "image/png" ? "png" : "jpg"));
            }, mimeType, quality);
        });
    }

    function guiFileLenServer(fileOrBlob, tenFile) {
        const formData = new FormData();
        formData.append("avatar", fileOrBlob, tenFile);

        const uploadUrl = uploadForm ? uploadForm.getAttribute("action") : "/avatar";

        fetch(uploadUrl, {
            method: "POST",
            body: formData,
            headers: {
                "X-Requested-With": "XMLHttpRequest",
                "Accept": "application/json"
            }
        })
        .then(function (response) {
            return response.json().then(function (data) {
                return { ok: response.ok, status: response.status, data: data };
            }).catch(function () {
                return { ok: response.ok, status: response.status, data: null };
            });
        })
        .then(function (res) {
            khoiPhucNutUpload();

            if (res.ok && res.data && res.data.success) {
                const data = res.data;
                const timestamp = Date.now();

                // Cập nhật tất cả các ảnh đại diện trên trang lập tức (Real-time DOM update)
                const avatarFullUrl = data.avatarUrl + (data.avatarUrl.includes("?") ? "&t=" : "?t=") + timestamp;
                const avatarThumbUrl = data.thumbUrl + (data.thumbUrl.includes("?") ? "&t=" : "?t=") + timestamp;

                if (avatarPreviewLarge) avatarPreviewLarge.src = avatarFullUrl;
                if (avatarPreviewThumb) avatarPreviewThumb.src = avatarThumbUrl;
                if (avatarPreviewThumbMini) avatarPreviewThumbMini.src = avatarThumbUrl;
                if (avatarPreviewHeader) avatarPreviewHeader.src = avatarThumbUrl;

                // Cập nhật cả avatar trên navigation chung nếu có
                const navAvatars = document.querySelectorAll(".crm-user-avatar-img, .user-header-thumb");
                navAvatars.forEach(img => { img.src = avatarThumbUrl; });

                dongModalCatAnh();
                datLaiForm();

                showToast("success", data.message || "Tải lên và xử lý ảnh đại diện thành công! Đồng nghiệp sẽ nhận diện được bạn.");
            } else {
                const errMsg = (res.data && res.data.message) ? res.data.message : "Tải lên thất bại (Mã lỗi: " + res.status + ")";
                showToast("error", errMsg);
                hienThongBaoLoi(errMsg);
            }
        })
        .catch(function (error) {
            khoiPhucNutUpload();
            showToast("error", "Lỗi kết nối mạng: " + error.message);
            hienThongBaoLoi("Lỗi kết nối mạng khi tải lên ảnh: " + error.message);
        });
    }

    function khoiPhucNutUpload() {
        if (btnConfirmCropAndUpload) {
            btnConfirmCropAndUpload.disabled = false;
        }
        if (btnConfirmText) {
            btnConfirmText.innerHTML = '<span class="material-symbols-outlined icon-xs" aria-hidden="true">check_circle</span> Cắt & Lưu ảnh đại diện';
        }
    }

    // Xử lý Submit form trực tiếp không qua modal (Fallback)
    if (uploadForm) {
        uploadForm.addEventListener("submit", function (e) {
            if (btnSubmitDirect && !btnSubmitDirect.disabled) {
                btnSubmitDirect.disabled = true;
                btnSubmitDirect.innerHTML = '<span class="spinner-border-sm"></span> Đang tải lên...';
            }
        });
    }

    // ==========================================================================
    // 7. TOAST NOTIFICATION HELPER
    // ==========================================================================
    function showToast(type, message) {
        const container = document.getElementById("crmToastContainer");
        if (!container) return;

        const toast = document.createElement("div");
        toast.className = "crm-toast " + (type === "success" ? "success" : "error");

        const iconName = type === "success" ? "check_circle" : "error";

        toast.innerHTML =
            '<span class="material-symbols-outlined crm-toast-icon" aria-hidden="true">' + iconName + '</span>' +
            '<div class="crm-toast-body">' + escapeHtml(message) + '</div>' +
            '<button type="button" class="crm-toast-close" aria-label="Đóng" title="Đóng"><span class="material-symbols-outlined icon-sm" aria-hidden="true">close</span></button>';

        container.appendChild(toast);

        const closeBtn = toast.querySelector(".crm-toast-close");
        if (closeBtn) {
            closeBtn.addEventListener("click", function () {
                xoaToast(toast);
            });
        }

        // Tự động đóng sau 4.5 giây
        setTimeout(function () {
            xoaToast(toast);
        }, 4500);
    }

    function xoaToast(toast) {
        toast.style.opacity = "0";
        toast.style.transform = "translateY(10px)";
        setTimeout(function () {
            if (toast.parentNode) toast.parentNode.removeChild(toast);
        }, 250);
    }

    function escapeHtml(str) {
        if (!str) return "";
        return str
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }
});
