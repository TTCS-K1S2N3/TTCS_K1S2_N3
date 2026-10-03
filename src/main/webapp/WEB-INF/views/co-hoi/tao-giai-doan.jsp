<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thêm Giai đoạn Pipeline Mới | CRM Bán Hàng</title>
    <meta name="description" content="Khai báo một giai đoạn mới trong chuỗi pipeline bán hàng, cài đặt xác suất thắng mặc định và điều kiện bắt buộc để rời giai đoạn.">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/co-hoi/pipeline.css">
</head>
<body>

<div class="crm-container" style="max-width: 920px;">

    <!-- Breadcrumb Navigation -->
    <nav class="breadcrumb-nav" aria-label="Đường dẫn điều hướng">
        <a href="${pageContext.request.contextPath}/">Trang chủ</a>
        <span class="sep">/</span>
        <a href="${pageContext.request.contextPath}/pipeline/giai-doan${not empty param.role ? '?role='.concat(param.role) : ''}">Cấu hình Pipeline</a>
        <span class="sep">/</span>
        <span class="current">Thêm giai đoạn mới</span>
    </nav>

    <!-- Header -->
    <header class="crm-header">
        <div class="crm-header-title">
            <h1>➕ Thêm Giai đoạn Pipeline Mới (AC 1)</h1>
            <p>Định nghĩa một bước trong chuỗi phễu bán hàng, xác suất thắng mặc định dùng để tính dự báo và tiêu chuẩn bắt buộc rời giai đoạn.</p>
        </div>
        <div>
            <a href="${pageContext.request.contextPath}/pipeline/giai-doan${not empty param.role ? '?role='.concat(param.role) : ''}"
               class="btn btn-outline"
               id="btnQuayLai">
                &larr; Quay lại danh sách
            </a>
        </div>
    </header>

    <!-- Flash Error nếu có -->
    <c:if test="${not empty thongBaoLoi}">
        <div class="alert alert-danger" role="alert">
            <span>⚠️ <strong>Lỗi:</strong> ${thongBaoLoi}</span>
            <button type="button" class="alert-close-btn" onclick="dongThongBao(this)" aria-label="Đóng">&times;</button>
        </div>
    </c:if>

    <!-- Form Tạo Giai Đoạn -->
    <div class="crm-card">
        <form id="formGiaiDoan" method="post" action="${pageContext.request.contextPath}/pipeline/giai-doan/tao" novalidate>
            <c:if test="${not empty param.role}">
                <input type="hidden" name="role" value="${param.role}">
            </c:if>

            <div class="form-grid">

                <!-- 1. Mã giai đoạn -->
                <div class="form-group">
                    <label for="maGiaiDoan">
                        Mã giai đoạn <span class="required">*</span>
                    </label>
                    <input type="text" id="maGiaiDoan" name="maGiaiDoan" value="${giaiDoan.maGiaiDoan}"
                           placeholder="VD: TIEP_CAN, BAO_GIA, DAM_PHAN"
                           class="${not empty danhSachLoi['maGiaiDoan'] ? 'is-invalid' : ''}"
                           required autofocus>
                    <c:if test="${not empty danhSachLoi['maGiaiDoan']}">
                        <span class="field-error">⚠️ ${danhSachLoi['maGiaiDoan']}</span>
                    </c:if>
                    <span class="form-hint">Mã viết in hoa không dấu, gồm chữ, số, gạch ngang (-) hoặc gạch dưới (_).</span>
                </div>

                <!-- 2. Tên giai đoạn -->
                <div class="form-group">
                    <label for="tenGiaiDoan">
                        Tên giai đoạn <span class="required">*</span>
                    </label>
                    <input type="text" id="tenGiaiDoan" name="tenGiaiDoan" value="${giaiDoan.tenGiaiDoan}"
                           placeholder="VD: Tiếp cận, Báo giá, Đàm phán..."
                           class="${not empty danhSachLoi['tenGiaiDoan'] ? 'is-invalid' : ''}"
                           required>
                    <c:if test="${not empty danhSachLoi['tenGiaiDoan']}">
                        <span class="field-error">⚠️ ${danhSachLoi['tenGiaiDoan']}</span>
                    </c:if>
                    <span class="form-hint">Tên hiển thị trên quy trình pipeline và thẻ cơ hội bán hàng.</span>
                </div>

                <!-- 3. Thứ tự trong chuỗi -->
                <div class="form-group">
                    <label for="thuTu">
                        Thứ tự trong chuỗi quy trình <span class="required">*</span>
                    </label>
                    <input type="number" id="thuTu" name="thuTu" value="${giaiDoan.thuTu}" min="1"
                           class="${not empty danhSachLoi['thuTu'] ? 'is-invalid' : ''}"
                           required>
                    <c:if test="${not empty danhSachLoi['thuTu']}">
                        <span class="field-error">⚠️ ${danhSachLoi['thuTu']}</span>
                    </c:if>
                    <span class="form-hint">Vị trí bước trong chuỗi phễu (1, 2, 3...).</span>
                </div>

                <!-- 4. Phân loại giai đoạn -->
                <div class="form-group">
                    <label for="loaiGiaiDoan">Phân loại giai đoạn</label>
                    <select id="loaiGiaiDoan" name="loaiGiaiDoan">
                        <c:forEach var="l" items="${danhSachLoai}">
                            <option value="${l.maLoai}" ${giaiDoan.loaiGiaiDoan.maLoai == l.maLoai ? 'selected' : ''}>
                                ${l.tenHienThi} &mdash; ${l.moTa}
                            </option>
                        </c:forEach>
                    </select>
                    <span class="form-hint">Đánh dấu giai đoạn này đang tiến hành hay là kết quả đóng phễu.</span>
                </div>

                <!-- AC 2: Xác suất thắng mặc định với Thanh trượt đồng bộ -->
                <div class="form-group form-full" style="background: var(--primary-50); border: 1px solid var(--primary-200); padding: 18px; border-radius: var(--radius-md);">
                    <label for="xacSuatThang" style="font-weight: 700; color: var(--primary-800); display: flex; align-items: center; justify-content: space-between;">
                        <span>📊 Xác suất thắng mặc định dùng để tính dự báo (AC 2) <span class="required">*</span></span>
                        <span id="probLivePreview" style="font-size: 13px; font-weight: 600; color: var(--gray-700);"></span>
                    </label>

                    <div class="slider-sync-box" style="margin-top: 10px;">
                        <input type="range" id="sliderXacSuat" class="slider-range" min="0" max="100" value="${giaiDoan.xacSuatThang > 0 ? giaiDoan.xacSuatThang : 20}">
                        <div style="display: flex; align-items: center; gap: 6px; width: 120px;">
                            <input type="number" id="xacSuatThang" name="xacSuatThang" value="${giaiDoan.xacSuatThang > 0 ? giaiDoan.xacSuatThang : 20}"
                                   min="0" max="100" class="${not empty danhSachLoi['xacSuatThang'] ? 'is-invalid' : ''}"
                                   style="font-weight: 800; font-size: 16px; text-align: center; width: 80px;" required>
                            <span style="font-weight: 700; color: var(--gray-600); font-size: 15px;">%</span>
                        </div>
                    </div>
                    <c:if test="${not empty danhSachLoi['xacSuatThang']}">
                        <span class="field-error">⚠️ ${danhSachLoi['xacSuatThang']}</span>
                    </c:if>
                    <span class="form-hint" style="color: var(--primary-700); margin-top: 6px;">
                        💡 Công thức dự báo tự động: <strong>Doanh số dự báo = Giá trị cơ hội &times; (Xác suất thắng / 100)</strong>
                    </span>
                </div>

                <!-- AC 3: Khai báo điều kiện bắt buộc để rời một giai đoạn -->
                <div class="form-group form-full">
                    <div class="condition-builder-card">
                        <div class="condition-builder-header">
                            <span style="font-size: 20px;">🔒</span>
                            <div>
                                <h3 style="font-size: 15px; font-weight: 700; color: #1e3a8a;">
                                    Tiêu chuẩn điều kiện bắt buộc để rời giai đoạn (AC 3)
                                </h3>
                                <p style="font-size: 13px; color: var(--gray-500); margin-top: 2px;">
                                    Nhân viên kinh doanh không thể tự ý đẩy cơ hội sang bước sau nếu chưa hoàn thành các điều kiện này.
                                </p>
                            </div>
                        </div>

                        <!-- Mô tả bằng văn bản -->
                        <div class="form-group" style="margin-bottom: 16px;">
                            <label for="dieuKienBatBuoc">Mô tả quy tắc điều kiện bắt buộc:</label>
                            <input type="text" id="dieuKienBatBuoc" name="dieuKienBatBuoc" value="${giaiDoan.dieuKienBatBuoc}"
                                   placeholder="VD: Phải có ít nhất một cuộc gặp trực tiếp và xác nhận bảng câu hỏi nhu cầu">
                            <span class="form-hint">Mô tả sẽ hiển thị trực tiếp trên thẻ giai đoạn để hướng dẫn nhân viên kinh doanh.</span>
                        </div>

                        <!-- Ràng buộc định lượng -->
                        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px;">
                            <div class="form-group">
                                <label for="soCuocGapToiThieu">🤝 Số cuộc gặp trực tiếp tối thiểu:</label>
                                <input type="number" id="soCuocGapToiThieu" name="soCuocGapToiThieu"
                                       value="${giaiDoan.soCuocGapToiThieu}" min="0">
                                <span class="form-hint">Ví dụ: nhập &ge; 1 để bắt buộc phải có ít nhất 1 cuộc gặp.</span>
                            </div>

                            <div class="form-group">
                                <label for="soCuocGoiToiThieu">📞 Số cuộc gọi kết nối tối thiểu:</label>
                                <input type="number" id="soCuocGoiToiThieu" name="soCuocGoiToiThieu"
                                       value="${giaiDoan.soCuocGoiToiThieu}" min="0">
                                <span class="form-hint">Số lần liên lạc thành công tối thiểu để được chuyển bước.</span>
                            </div>
                        </div>

                        <!-- Checkbox điều kiện nghiệp vụ -->
                        <div style="display: flex; gap: 28px; flex-wrap: wrap;">
                            <label class="checkbox-group">
                                <input type="checkbox" name="yeuCauBaoGia" ${giaiDoan.yeuCauBaoGia ? 'checked' : ''}>
                                <span>📄 Bắt buộc có Báo giá niêm yết được gửi cho khách hàng</span>
                            </label>

                            <label class="checkbox-group">
                                <input type="checkbox" name="yeuCauKhaoSatNhuCau" ${giaiDoan.yeuCauKhaoSatNhuCau ? 'checked' : ''}>
                                <span>📋 Bắt buộc hoàn thành bảng khảo sát nhu cầu</span>
                            </label>
                        </div>
                    </div>
                </div>

                <!-- Cảnh báo đình trệ -->
                <div class="form-group">
                    <label for="soNgayCanhBaoDinhTre">Số ngày không hoạt động để cảnh báo đình trệ:</label>
                    <input type="number" id="soNgayCanhBaoDinhTre" name="soNgayCanhBaoDinhTre"
                           value="${giaiDoan.soNgayCanhBaoDinhTre > 0 ? giaiDoan.soNgayCanhBaoDinhTre : 7}" min="1">
                    <span class="form-hint">Cơ hội nằm ở bước này quá số ngày trên sẽ được gắn cờ cảnh báo rủi ro trễ hạn.</span>
                </div>

                <!-- Trạng thái áp dụng -->
                <div class="form-group">
                    <label for="trangThai">Trạng thái áp dụng</label>
                    <select id="trangThai" name="trangThai">
                        <c:forEach var="tt" items="${danhSachTrangThai}">
                            <option value="${tt.maTrangThai}" ${giaiDoan.trangThai.maTrangThai == tt.maTrangThai ? 'selected' : ''}>
                                ${tt.tenHienThi} &mdash; ${tt.moTa}
                            </option>
                        </c:forEach>
                    </select>
                </div>

            </div>

            <!-- Form Actions -->
            <div style="margin-top: 28px; display: flex; justify-content: flex-end; gap: 14px; border-top: 1px solid var(--gray-200); padding-top: 20px;">
                <a href="${pageContext.request.contextPath}/pipeline/giai-doan${not empty param.role ? '?role='.concat(param.role) : ''}"
                   class="btn btn-outline">
                    Hủy bỏ
                </a>
                <button type="submit" class="btn btn-primary" id="btnSubmitForm">
                    💾 Lưu giai đoạn pipeline
                </button>
            </div>
        </form>
    </div>

</div>

<script src="${pageContext.request.contextPath}/assets/js/co-hoi/pipeline.js"></script>
</body>
</html>
