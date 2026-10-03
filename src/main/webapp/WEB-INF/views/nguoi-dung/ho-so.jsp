<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hồ sơ cá nhân & Chữ ký email - CRM Bán Hàng</title>
    <meta name="description" content="Xem và cập nhật hồ sơ cá nhân, ảnh đại diện cắt vuông và chữ ký email phục vụ gửi báo giá cho khách hàng.">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/nguoi-dung/nguoi-dung.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/nguoi-dung/ho-so.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ho-so/avatar.css">
</head>
<body class="crm-body">

    <!-- Thanh điều hướng và Sidebar chuẩn hệ thống S1-06 -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content" id="crm-main-content">
        <div class="ho-so-container">

            <!-- Hero Banner hồ sơ -->
            <div class="ho-so-hero">
                <div class="ho-so-avatar-large" id="avatar-display" title="${nguoiDung.hoTen}" style="position: relative; overflow: hidden; padding: 0;">
                    <img src="${pageContext.request.contextPath}/avatar?id=${nguoiDung.id}"
                         alt="${nguoiDung.hoTen}"
                         id="avatarPreviewLarge"
                         class="avatar-large-img"
                         style="width: 100%; height: 100%; object-fit: cover; border-radius: 50%;"
                         onerror="this.style.display='none'; if(this.nextElementSibling) this.nextElementSibling.style.display='flex';">
                    <span style="display: none; width: 100%; height: 100%; align-items: center; justify-content: center;"><c:out value="${nguoiDung.tenVietTat}"/></span>
                </div>
                <div class="ho-so-hero-info" style="flex: 1;">
                    <h1 id="hero-user-name"><c:out value="${nguoiDung.hoTen}"/></h1>
                    <div class="ho-so-hero-badges">
                        <span class="badge-pill badge-pill-role" title="Vai trò người dùng">
                            &#128100; <c:out value="${nguoiDung.chuoiVaiTroHienThi}"/>
                        </span>
                        <span class="badge-pill badge-pill-team" title="Nhóm kinh doanh">
                            &#128101; <c:out value="${nguoiDung.tenNhomKinhDoanh}"/>
                        </span>
                    </div>
                </div>
            </div>

            <!-- Thông báo thành công -->
            <c:if test="${not empty thongBaoThanhCong}">
                <div class="alert alert-success" role="alert" id="alert-thanh-cong">
                    <span class="alert-icon">&#10004;</span>
                    <span><c:out value="${thongBaoThanhCong}"/></span>
                </div>
            </c:if>

            <!-- Thông báo lỗi tổng quát -->
            <c:if test="${not empty thongBaoLoi}">
                <div class="alert alert-error" role="alert" id="alert-loi">
                    <span class="alert-icon">&#9888;</span>
                    <span><c:out value="${thongBaoLoi}"/></span>
                </div>
            </c:if>

            <!-- ===============================================================
                 THẺ TẢI LÊN ẢNH ĐẠI DIỆN & CẮT VUÔNG (Story S2-03)
                 =============================================================== -->
            <div class="ho-so-card" style="margin-bottom: 24px;">
                <div class="ho-so-card-header" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 8px;">
                    <h2 style="margin: 0; font-size: 1.15rem;"><i class="bi bi-camera-fill"></i> Tải ảnh đại diện (Cắt vuông & Thumbnail)</h2>
                    <span class="hint-text" style="background: #e0f2fe; color: #0369a1; padding: 4px 10px; border-radius: 20px; font-weight: 500;">
                        <i class="bi bi-check2-circle"></i> Chấp nhận JPG/PNG &le; 2MB
                    </span>
                </div>
                <div class="ho-so-card-body">
                    <form id="avatarUploadForm"
                          action="${pageContext.request.contextPath}/avatar"
                          method="post"
                          enctype="multipart/form-data"
                          class="avatar-form">

                        <!-- Vùng kéo thả file (Drag and Drop Zone) -->
                        <div class="upload-dropzone" id="uploadDropzone" tabindex="0" role="button" aria-label="Kéo thả file ảnh hoặc nhấn để chọn">
                            <input type="file"
                                   name="avatar"
                                   id="avatarInput"
                                   accept=".jpg,.jpeg,.png,image/jpeg,image/png"
                                   aria-label="Chọn file ảnh đại diện">
                            <div class="dropzone-inner">
                                <div class="upload-icon-circle">
                                    <i class="bi bi-cloud-arrow-up-fill upload-icon" style="font-size: 2rem;"></i>
                                </div>
                                <div class="upload-text" style="font-weight: 600; font-size: 1rem; margin-top: 8px;">Kéo thả ảnh vào đây</div>
                                <div class="upload-subtext" style="color: #64748b; font-size: 0.9rem;">hoặc <span class="browse-link" id="btnTriggerFileSelect" style="color: #2563eb; text-decoration: underline; cursor: pointer;">nhấn để duyệt file</span></div>
                                <div class="upload-hint" style="color: #94a3b8; font-size: 0.8rem; margin-top: 4px;">
                                    <i class="bi bi-info-circle"></i> Định dạng JPG, PNG • Tối đa 2MB
                                </div>
                            </div>
                        </div>

                        <!-- Box thông tin file được chọn kèm kiểm tra hợp lệ -->
                        <div class="selected-file-info" id="selectedFileInfo" style="display: none; margin-top: 12px; padding: 12px; background: #f8fafc; border: 1px solid #cbd5e1; border-radius: 8px;">
                            <div class="file-info-header" style="display: flex; align-items: center; justify-content: space-between;">
                                <div class="file-info-text">
                                    <div class="file-name" id="fileName" style="font-weight: 600; color: #1e293b;">chua-chon-file.png</div>
                                    <div class="file-meta" style="font-size: 0.85rem; color: #64748b; margin-top: 2px;">
                                        <span id="fileSize">0 KB</span> •
                                        <span class="badge-valid-status" id="badgeValidStatus" style="color: #10b981; font-weight: 600;">
                                            <i class="bi bi-check-circle-fill"></i> Hợp lệ (&le; 2MB)
                                        </span>
                                    </div>
                                </div>
                                <button type="button" class="btn-clear-file" id="btnClearFile" title="Hủy chọn file" style="background: none; border: none; font-size: 1.2rem; cursor: pointer; color: #ef4444;">
                                    <i class="bi bi-x-circle-fill"></i>
                                </button>
                            </div>
                        </div>

                        <!-- Hộp thông báo lỗi phía Client (Validate AC: JPG/PNG <= 2MB) -->
                        <div class="client-error-box" id="clientErrorBox" style="display: none; margin-top: 12px; padding: 10px; background: #fef2f2; border: 1px solid #fca5a5; border-radius: 6px; color: #b91c1c; font-size: 0.9rem;">
                            <i class="bi bi-exclamation-octagon-fill"></i>
                            <span id="clientErrorText"></span>
                        </div>

                        <!-- Hàng nút hành động: Cắt vuông trực quan & Tải lên -->
                        <div class="upload-action-buttons" style="display: flex; gap: 12px; margin-top: 16px; flex-wrap: wrap;">
                            <button type="button" class="btn btn-primary" id="btnOpenCropModal" disabled style="padding: 10px 20px; background: #2563eb; color: #fff; border: 1px solid #1d4ed8; border-radius: 6px; font-weight: 600; cursor: pointer;">
                                <i class="bi bi-crop"></i>
                                <span>Cắt vuông & Tải lên</span>
                            </button>
                            <button type="submit" class="btn btn-secondary" id="btnSubmitDirect" disabled style="padding: 10px 20px; background: #f1f5f9; color: #334155; border: 1px solid #cbd5e1; border-radius: 6px; font-weight: 600; cursor: pointer;">
                                <i class="bi bi-arrow-up-circle"></i>
                                <span>Tải lên nhanh</span>
                            </button>
                        </div>
                    </form>
                </div>
            </div>

            <!-- ===============================================================
                 FORM CẬP NHẬT HỒ SƠ CÁ NHÂN & CHỮ KÝ (Story S2-02)
                 =============================================================== -->
            <div class="ho-so-grid">
                <div class="ho-so-card">
                    <div class="ho-so-card-header">
                        <h2>&#9998; Chỉnh sửa thông tin cá nhân & Chữ ký</h2>
                        <span class="hint-text">Dấu (<span class="required" style="color: #dc2626;">*</span>) là trường bắt buộc</span>
                    </div>

                    <div class="ho-so-card-body">
                        <form id="form-ho-so"
                              method="POST"
                              action="${pageContext.request.contextPath}/ho-so"
                              novalidate>

                            <!-- AC1: Họ và tên -->
                            <div class="form-group" style="margin-bottom: 20px;">
                                <label for="hoTen" style="font-weight: 600; display: block; margin-bottom: 6px;">
                                    Họ và tên <span class="required" style="color: #dc2626;">*</span>
                                </label>
                                <input type="text"
                                       id="hoTen"
                                       name="hoTen"
                                       required
                                       maxlength="150"
                                       placeholder="Ví dụ: Nguyễn Văn A"
                                       class="<c:if test='${not empty formError["hoTen"]}'>input-error</c:if>"
                                       value="<c:out value='${nguoiDung.hoTen}'/>"
                                       style="width: 100%; padding: 10px 12px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.95rem;">
                                <span class="form-error-msg <c:if test='${not empty formError["hoTen"]}'>visible</c:if>"
                                      id="hoTen-error" style="color: #dc2626; font-size: 0.85rem; display: ${not empty formError['hoTen'] ? 'block' : 'none'}; margin-top: 4px;">
                                    <c:out value="${formError['hoTen']}"/>
                                </span>
                            </div>

                            <!-- AC1 & AC3: Số điện thoại Việt Nam -->
                            <div class="form-group" style="margin-bottom: 20px;">
                                <label for="soDienThoai" style="font-weight: 600; display: block; margin-bottom: 6px;">
                                    Số điện thoại <span class="required" style="color: #dc2626;">*</span>
                                </label>
                                <input type="tel"
                                       id="soDienThoai"
                                       name="soDienThoai"
                                       required
                                       maxlength="20"
                                       placeholder="Ví dụ: 0901234567 hoặc +84901234567"
                                       class="<c:if test='${not empty formError["soDienThoai"]}'>input-error</c:if>"
                                       value="<c:out value='${nguoiDung.soDienThoai}'/>"
                                       style="width: 100%; padding: 10px 12px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.95rem;">
                                <div class="hint-text" style="font-size: 0.8rem; color: #64748b; margin-top: 4px;">
                                    &#9432; Định dạng số điện thoại Việt Nam (10 chữ số di động 03x, 05x, 07x, 08x, 09x hoặc 11 chữ số cố định). Chấp nhận đầu số quốc tế +84.
                                </div>
                                <span class="form-error-msg <c:if test='${not empty formError["soDienThoai"]}'>visible</c:if>"
                                      id="soDienThoai-error" style="color: #dc2626; font-size: 0.85rem; display: ${not empty formError['soDienThoai'] ? 'block' : 'none'}; margin-top: 4px;">
                                    <c:out value="${formError['soDienThoai']}"/>
                                </span>
                            </div>

                            <!-- AC1: Chữ ký email -->
                            <div class="form-group" style="margin-bottom: 20px;">
                                <label for="chuKyEmail" style="font-weight: 600; display: block; margin-bottom: 6px;">
                                    Chữ ký email (sử dụng khi gửi báo giá)
                                </label>
                                <textarea id="chuKyEmail"
                                          name="chuKyEmail"
                                          rows="6"
                                          placeholder="Ví dụ:&#10;Trân trọng,&#10;Nguyễn Văn A - Chuyên viên Kinh doanh&#10;Công ty Cổ phần Giải pháp Doanh nghiệp CRM&#10;Hotline: 0901234567 | Email: a.nguyen@crm.vn"
                                          class="<c:if test='${not empty formError["chuKyEmail"]}'>input-error</c:if>"
                                          style="width: 100%; padding: 10px 12px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.95rem; font-family: inherit; resize: vertical;"><c:out value="${nguoiDung.chuKyEmail}"/></textarea>
                                <div class="hint-text" style="font-size: 0.8rem; color: #64748b; margin-top: 4px;">
                                    &#9432; Chữ ký này sẽ được tự động chèn vào phần cuối email khi bạn gửi báo giá hoặc tài liệu cho khách hàng.
                                </div>
                                <span class="form-error-msg <c:if test='${not empty formError["chuKyEmail"]}'>visible</c:if>"
                                      id="chuKyEmail-error" style="color: #dc2626; font-size: 0.85rem; display: ${not empty formError['chuKyEmail'] ? 'block' : 'none'}; margin-top: 4px;">
                                    <c:out value="${formError['chuKyEmail']}"/>
                                </span>

                                <!-- Khung xem trước chữ ký email thực tế -->
                                <div class="signature-preview-box" id="signature-preview-box">
                                    <div class="signature-preview-title">&#9993; Xem trước chữ ký trong email báo giá</div>
                                    <div style="font-style: italic; color: #64748b; margin-bottom: 8px;">Kính gửi Quý khách hàng, chúng tôi xin gửi báo giá chi tiết theo yêu cầu...</div>
                                    <hr style="border: 0; border-top: 1px dashed #cbd5e1; margin: 8px 0;">
                                    <div class="signature-preview-content" id="signature-preview-text"><c:out value="${not empty nguoiDung.chuKyEmail ? nguoiDung.chuKyEmail : '[Chưa thiết lập chữ ký email]'}" /></div>
                                </div>
                            </div>

                            <!-- AC2: Các thông tin KHÔNG được tự đổi (Chỉ đọc) -->
                            <div style="margin-top: 28px; padding-top: 20px; border-top: 1px solid #e2e8f0;">
                                <h3 style="font-size: 1rem; font-weight: 600; color: #475569; margin-bottom: 16px;">
                                    &#128274; Thông tin tài khoản hệ thống (Không thể tự thay đổi)
                                </h3>

                                <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 16px;">
                                    <!-- Email công ty (AC2: không tự đổi được) -->
                                    <div class="form-group">
                                        <label style="font-weight: 600; display: block; margin-bottom: 6px; color: #64748b;">
                                            Email công ty
                                        </label>
                                        <div class="readonly-field-wrap">
                                            <span class="readonly-val" id="readonly-email"><c:out value="${nguoiDung.email}"/></span>
                                            <span class="lock-tag">&#128274; Cố định</span>
                                        </div>
                                    </div>

                                    <!-- Nhóm kinh doanh (AC2: không tự đổi được) -->
                                    <div class="form-group">
                                        <label style="font-weight: 600; display: block; margin-bottom: 6px; color: #64748b;">
                                            Nhóm kinh doanh
                                        </label>
                                        <div class="readonly-field-wrap">
                                            <span class="readonly-val" id="readonly-team"><c:out value="${nguoiDung.tenNhomKinhDoanh}"/></span>
                                            <span class="lock-tag">&#128274; Cố định</span>
                                        </div>
                                    </div>

                                    <!-- Vai trò (AC2: không tự đổi được) -->
                                    <div class="form-group" style="grid-column: 1 / -1;">
                                        <label style="font-weight: 600; display: block; margin-bottom: 6px; color: #64748b;">
                                            Vai trò được cấp phép
                                        </label>
                                        <div class="readonly-field-wrap">
                                            <span class="readonly-val" id="readonly-role"><c:out value="${nguoiDung.chuoiVaiTroHienThi}"/></span>
                                            <span class="lock-tag">&#128274; Do Quản trị viên cấp</span>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Nút thao tác -->
                            <div class="form-actions">
                                <a href="${pageContext.request.contextPath}/ho-so"
                                   class="btn btn-secondary"
                                   style="padding: 10px 20px; background: #f1f5f9; border: 1px solid #cbd5e1; border-radius: 6px; color: #334155; text-decoration: none; font-weight: 600;">
                                    Làm mới
                                </a>
                                <button type="submit"
                                        id="btn-submit-ho-so"
                                        class="btn btn-primary"
                                        style="padding: 10px 24px; background: #2563eb; border: 1px solid #1d4ed8; border-radius: 6px; color: #ffffff; font-weight: 600; cursor: pointer;">
                                    Lưu thay đổi hồ sơ
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

        </div>
    </main>

    <!-- =======================================================================
         MODAL CẮT ẢNH VUÔNG (1:1) VÀ TẠO BẢN THU NHỎ THUMBNAIL (Story S2-03)
         ======================================================================= -->
    <div class="crop-modal-backdrop" id="cropModalBackdrop" style="display: none;"></div>
    <div class="crop-modal" id="cropModal" role="dialog" aria-modal="true" aria-labelledby="cropModalTitle" style="display: none;">
        <div class="crop-modal-content">

            <!-- Modal Header -->
            <div class="crop-modal-header">
                <div class="crop-modal-title-wrap">
                    <i class="bi bi-crop crop-modal-icon"></i>
                    <div>
                        <h3 class="crop-modal-title" id="cropModalTitle">Cắt ảnh vuông & Tạo thumbnail</h3>
                        <div class="crop-modal-subtitle">Điều chỉnh khung hình vuông (1:1) để nhận diện rõ nét nhất</div>
                    </div>
                </div>
                <button type="button" class="crop-modal-close" id="btnCloseCropModal" aria-label="Đóng cửa sổ">
                    <i class="bi bi-x-lg"></i>
                </button>
            </div>

            <!-- Modal Body: Workspace + Dual Previews -->
            <div class="crop-modal-body">

                <!-- Workspace: Canvas tương tác di chuyển, phóng to, xoay -->
                <div class="crop-workspace-container">
                    <div class="crop-canvas-wrapper" id="cropCanvasWrapper">
                        <canvas id="cropCanvas" width="360" height="360"></canvas>
                        <div class="crop-overlay-guide">
                            <div class="crop-rule-of-thirds">
                                <span class="grid-line grid-v1"></span>
                                <span class="grid-line grid-v2"></span>
                                <span class="grid-line grid-h1"></span>
                                <span class="grid-line grid-h2"></span>
                            </div>
                        </div>
                    </div>

                    <!-- Thanh công cụ điều khiển tương tác (Zoom, Rotate, Reset) -->
                    <div class="crop-toolbar">
                        <button type="button" class="btn-tool" id="btnZoomOut" title="Thu nhỏ (-)">
                            <i class="bi bi-dash-lg"></i>
                        </button>
                        <div class="zoom-slider-wrap">
                            <i class="bi bi-zoom-in slider-icon"></i>
                            <input type="range"
                                   id="zoomSlider"
                                   min="0.5"
                                   max="3.0"
                                   step="0.05"
                                   value="1.0"
                                   aria-label="Thanh trượt thu phóng ảnh">
                        </div>
                        <button type="button" class="btn-tool" id="btnZoomIn" title="Phóng to (+)">
                            <i class="bi bi-plus-lg"></i>
                        </button>
                        <div class="tool-divider"></div>
                        <button type="button" class="btn-tool" id="btnRotateLeft" title="Xoay trái 90°">
                            <i class="bi bi-arrow-counterclockwise"></i>
                        </button>
                        <button type="button" class="btn-tool" id="btnRotateRight" title="Xoay phải 90°">
                            <i class="bi bi-arrow-clockwise"></i>
                        </button>
                        <button type="button" class="btn-tool" id="btnResetCrop" title="Đặt lại vị trí ban đầu">
                            <i class="bi bi-aspect-ratio"></i> Đặt lại
                        </button>
                    </div>
                    <div class="crop-drag-hint">
                        <i class="bi bi-arrows-move"></i> Kéo giữ chuột hoặc chạm vuốt trên điện thoại để điều chỉnh vị trí ảnh
                    </div>
                </div>

                <!-- Preview Sidebar: Xem trước đồng thời Ảnh vuông chuẩn & Thumbnail -->
                <div class="crop-preview-sidebar">
                    <div class="preview-group">
                        <div class="preview-group-title">
                            <i class="bi bi-aspect-ratio"></i> Ảnh vuông chuẩn (1:1 - 400x400)
                        </div>
                        <div class="preview-circle-large-wrap">
                            <canvas id="previewSquareCanvas" width="140" height="140" class="preview-canvas-round"></canvas>
                            <canvas id="previewSquareRectCanvas" width="80" height="80" class="preview-canvas-rect"></canvas>
                        </div>
                        <div class="preview-note">Hiển thị trong trang Hồ sơ cá nhân</div>
                    </div>

                    <div class="preview-group">
                        <div class="preview-group-title">
                            <i class="bi bi-eye"></i> Bản thu nhỏ (Thumbnail - 120x120)
                        </div>
                        <div class="preview-thumb-demo-wrap">
                            <canvas id="previewThumbCanvas" width="48" height="48" class="preview-canvas-thumb"></canvas>
                            <div class="thumb-demo-meta">
                                <span class="thumb-demo-name"><c:out value="${nguoiDung.hoTen}"/></span>
                                <span class="thumb-demo-tag">Nhận diện người phụ trách</span>
                            </div>
                        </div>
                        <div class="preview-note">Hiển thị ở Header, Danh bạ & Hồ sơ khách hàng</div>
                    </div>

                    <div class="crop-meta-box">
                        <div class="meta-row">
                            <span>Tỷ lệ cắt:</span>
                            <strong class="text-success">1 : 1 (Vuông chuẩn)</strong>
                        </div>
                        <div class="meta-row">
                            <span>Định dạng xuất:</span>
                            <strong id="cropFormatLabel">JPG</strong>
                        </div>
                        <div class="meta-row">
                            <span>Dung lượng sau cắt:</span>
                            <strong id="cropEstimatedSize">&le; 200 KB (&le; 2MB)</strong>
                        </div>
                    </div>
                </div>

            </div>

            <!-- Modal Footer -->
            <div class="crop-modal-footer">
                <button type="button" class="btn btn-light" id="btnCancelCrop">
                    Hủy bỏ
                </button>
                <button type="button" class="btn btn-primary" id="btnConfirmCropAndUpload">
                    <i class="bi bi-check2-circle"></i>
                    <span id="btnConfirmText">Cắt & Lưu ảnh đại diện</span>
                </button>
            </div>

        </div>
    </div>

    <!-- Toast Container cho các thông báo tương tác nhanh -->
    <div class="crm-toast-container" id="crmToastContainer" aria-live="polite"></div>

    <!-- JavaScript xem trước chữ ký thời gian thực và validation phía client -->
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            const chuKyInput = document.getElementById('chuKyEmail');
            const previewText = document.getElementById('signature-preview-text');

            if (chuKyInput && previewText) {
                chuKyInput.addEventListener('input', function () {
                    const val = chuKyInput.value.trim();
                    previewText.textContent = val ? chuKyInput.value : '[Chưa thiết lập chữ ký email]';
                });
            }

            // Client-side quick check
            const form = document.getElementById('form-ho-so');
            if (form) {
                form.addEventListener('submit', function (e) {
                    const sdt = document.getElementById('soDienThoai').value.trim();
                    const sdtError = document.getElementById('soDienThoai-error');
                    // Regex kiểm tra số điện thoại VN: 10 số di động hoặc 11 số cố định
                    const cleanPhone = sdt.replace(/[\s.-]/g, '');
                    const phoneRegex = /^(?:\+?84|0)(?:(?:3[2-9]|5[25689]|7[06-9]|8[1-9]|9[0-9])[0-9]{7}|2[0-9]{9})$/;
                    if (sdt && !phoneRegex.test(cleanPhone)) {
                        e.preventDefault();
                        if (sdtError) {
                            sdtError.textContent = 'Số điện thoại không đúng định dạng Việt Nam hợp lệ (ví dụ: 0901234567 hoặc +84901234567).';
                            sdtError.style.display = 'block';
                        }
                        document.getElementById('soDienThoai').focus();
                    }
                });
            }
        });
    </script>
    <script src="${pageContext.request.contextPath}/assets/js/ho-so/avatar.js"></script>
</body>
</html>
