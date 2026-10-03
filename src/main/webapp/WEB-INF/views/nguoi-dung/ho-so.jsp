<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hồ sơ cá nhân & Chữ ký email - CRM Bán Hàng</title>
    <meta name="description" content="Xem và cập nhật hồ sơ cá nhân, chữ ký email phục vụ gửi báo giá cho khách hàng.">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/nguoi-dung/nguoi-dung.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/nguoi-dung/ho-so.css">
</head>
<body class="crm-body">

    <!-- Thanh điều hướng và Sidebar chuẩn hệ thống S1-06 -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content" id="crm-main-content">
        <div class="ho-so-container">

            <!-- Hero Banner hồ sơ -->
            <div class="ho-so-hero">
                <div class="ho-so-avatar-large" id="avatar-display" title="${nguoiDung.hoTen}">
                    <c:out value="${nguoiDung.tenVietTat}"/>
                </div>
                <div class="ho-so-hero-info">
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

            <!-- Form cập nhật hồ sơ -->
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
</body>
</html>
