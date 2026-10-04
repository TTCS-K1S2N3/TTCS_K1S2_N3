<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Khai báo sản phẩm mới | Bảng giá CRM</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/san-pham/san-pham.css">
</head>
<body>

<div class="crm-container" style="max-width: 920px;">

    <!-- Header & Navigation -->
    <header class="crm-header">
        <div class="crm-header-title">
            <h1>Khai báo Sản phẩm / Dịch vụ mới</h1>
            <p>Thiết lập danh mục và bảng giá niêm yết chuẩn để chuẩn hóa quy trình xuất báo giá kinh doanh</p>
        </div>
        <div class="header-actions">
            <a href="${pageContext.request.contextPath}/san-pham"
               class="btn btn-outline" id="btnQuayLai">
                <span>&larr;</span> Quay lại danh sách
            </a>
        </div>
    </header>

    <!-- Thông báo lỗi tổng quát nếu có -->
    <c:if test="${not empty thongBaoLoi}">
        <div class="alert alert-danger" role="alert">
            <div style="display: flex; align-items: center; gap: 8px;">
                <span><strong>Lỗi nhập liệu:</strong> ${thongBaoLoi}</span>
            </div>
            <span class="alert-close" onclick="this.parentElement.style.display='none';">&times;</span>
        </div>
    </c:if>

    <!-- Form Khai báo sản phẩm -->
    <main class="crm-card">
        <form id="formSanPham" method="post" action="${pageContext.request.contextPath}/san-pham/tao" novalidate>
            <div class="form-grid">
                <!-- Mã sản phẩm (AC1) -->
                <div class="form-group">
                    <label for="maSanPham">
                        Mã sản phẩm / dịch vụ <span class="required">*</span>
                    </label>
                    <input type="text" id="maSanPham" name="maSanPham" value="${sanPham.maSanPham}"
                           placeholder="VD: CRM-PRO-YR, CLOUD-STORAGE"
                           style="text-transform: uppercase; font-family: ui-monospace, monospace; font-weight: 700;"
                           required autofocus>
                    <c:if test="${not empty danhSachLoi['maSanPham']}">
                        <span class="field-error">${danhSachLoi['maSanPham']}</span>
                    </c:if>
                    <span class="form-hint">Mã duy nhất trong hệ thống (2 - 50 ký tự: chữ, số, dấu gạch ngang).</span>
                </div>

                <!-- Loại sản phẩm (AC1) -->
                <div class="form-group">
                    <label for="loai">
                        Loại mặt hàng <span class="required">*</span>
                    </label>
                    <select id="loai" name="loai" required>
                        <c:forEach var="l" items="${danhSachLoai}">
                            <option value="${l.maLoai}" ${sanPham.maLoai == l.maLoai ? 'selected' : ''}>
                                ${l.maLoai == 'DICH_VU_THUE_BAO' ? '🔄 ' : '📦 '}${l.tenHienThi} (${l.moTa})
                            </option>
                        </c:forEach>
                    </select>
                    <c:if test="${not empty danhSachLoi['loai']}">
                        <span class="field-error">${danhSachLoi['loai']}</span>
                    </c:if>
                    <span class="form-hint">Phân biệt sản phẩm mua đứt một lần hoặc dịch vụ thanh toán thuê bao định kỳ.</span>
                </div>

                <!-- Tên sản phẩm (AC1) -->
                <div class="form-group form-full">
                    <label for="tenSanPham">
                        Tên sản phẩm / dịch vụ <span class="required">*</span>
                    </label>
                    <input type="text" id="tenSanPham" name="tenSanPham" value="${sanPham.tenSanPham}"
                           placeholder="VD: Phần mềm Quản trị khách hàng CRM - Bản Doanh nghiệp Enterprise" required>
                    <c:if test="${not empty danhSachLoi['tenSanPham']}">
                        <span class="field-error">${danhSachLoi['tenSanPham']}</span>
                    </c:if>
                </div>

                <!-- Đơn vị tính (AC1) -->
                <div class="form-group">
                    <label for="donViTinh">
                        Đơn vị tính (ĐVT) <span class="required">*</span>
                    </label>
                    <input type="text" id="donViTinh" name="donViTinh" value="${sanPham.donViTinh}"
                           placeholder="VD: Người dùng/Tháng, Buổi, Bản quyền, Gói"
                           list="danhSachDVT" required>
                    <datalist id="danhSachDVT">
                        <option value="Người dùng/Tháng">
                        <option value="Người dùng/Năm">
                        <option value="Gói triển khai">
                        <option value="Buổi đào tạo">
                        <option value="Giờ tư vấn">
                        <option value="Bản quyền vĩnh viễn">
                        <option value="Bộ thiết bị">
                    </datalist>
                    <c:if test="${not empty danhSachLoi['donViTinh']}">
                        <span class="field-error">${danhSachLoi['donViTinh']}</span>
                    </c:if>
                </div>

                <!-- Trạng thái kinh doanh -->
                <div class="form-group">
                    <label for="trangThai">
                        Trạng thái kinh doanh
                    </label>
                    <select id="trangThai" name="trangThai">
                        <c:forEach var="tt" items="${danhSachTrangThai}">
                            <option value="${tt.maTrangThai}" ${sanPham.maTrangThai == tt.maTrangThai ? 'selected' : ''}>
                                ${tt.tenHienThi}
                            </option>
                        </c:forEach>
                    </select>
                    <span class="form-hint">Mặt hàng "Ngừng kinh doanh" sẽ không hiển thị trên danh mục tạo báo giá mới.</span>
                </div>

                <!-- Giá niêm yết (AC1) -->
                <div class="form-group">
                    <label for="giaNiemYet">
                        Giá niêm yết (VNĐ) <span class="required">*</span>
                    </label>
                    <input type="number" id="giaNiemYet" name="giaNiemYet" value="${sanPham.giaNiemYet}"
                           min="0" step="1000" placeholder="VD: 15000000" required>
                    <c:if test="${not empty danhSachLoi['giaNiemYet']}">
                        <span class="field-error">${danhSachLoi['giaNiemYet']}</span>
                    </c:if>
                    <span class="form-hint">Đơn giá niêm yết chính thức, mặc định điền vào dòng báo giá ban đầu.</span>
                </div>

                <!-- Giá sàn (AC1, AC2) -->
                <div class="form-group">
                    <label for="giaSan">
                        Giá sàn (VNĐ) <span class="required">*</span>
                    </label>
                    <input type="number" id="giaSan" name="giaSan" value="${sanPham.giaSan}"
                           min="0" step="1000" placeholder="VD: 12000000" required>
                    <span id="errGiaSan" class="field-error">${danhSachLoi['giaSan']}</span>
                    <span class="form-hint" style="color: #b45309; font-weight: 500;">
                        ⚡ <strong>Ngưỡng duyệt chiết khấu:</strong> Đơn giá bán dưới mức này sẽ yêu cầu Giám đốc phê duyệt.
                    </span>
                </div>

                <!-- AC3: Giá vốn chỉ Giám đốc kinh doanh xem và sửa được -->
                <c:choose>
                    <c:when test="${coQuyenGiaVon}">
                        <div class="form-group form-full">
                            <div class="security-cost-box authorized">
                                <div class="security-cost-header">
                                    <span class="security-cost-title">
                                        🔒 Giá vốn (Cost Price) - Phân quyền Giám đốc kinh doanh
                                    </span>
                                    <span class="badge badge-director-only">
                                        CHỈ GIÁM ĐỐC KINH DOANH (DIRECTOR)
                                    </span>
                                </div>
                                <label for="giaVon" style="font-size: 13px; font-weight: 600; color: #166534;">
                                    Nhập giá vốn sản phẩm (VNĐ):
                                </label>
                                <input type="number" id="giaVon" name="giaVon" value="${sanPham.giaVon}"
                                       min="0" step="1000" placeholder="VD: 8000000" style="margin-top: 6px;">
                                <c:if test="${not empty danhSachLoi['giaVon']}">
                                    <span class="field-error">${danhSachLoi['giaVon']}</span>
                                </c:if>
                                <div class="form-hint" style="margin-top: 6px; color: #15803d;">
                                    <strong>Chính sách bảo mật AC3:</strong> Giá vốn được kiểm soát hoàn toàn ở phía máy chủ. Nhân viên kinh doanh không thể xem được giá trị này trong API hoặc giao diện.
                                </div>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="form-group form-full">
                            <div class="security-cost-box">
                                <div style="display: flex; align-items: center; gap: 8px; color: var(--slate-600); font-size: 13.5px;">
                                    <span><strong>Giá vốn (Cost Price):</strong> Chỉ Giám đốc kinh doanh mới có quyền xem và cấu hình giá vốn. Dữ liệu này được bảo mật hoàn toàn.</span>
                                </div>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>

                <!-- Mô tả sản phẩm -->
                <div class="form-group form-full">
                    <label for="moTa">Mô tả sản phẩm / Dịch vụ</label>
                    <textarea id="moTa" name="moTa" rows="3"
                              placeholder="Nhập thông tin giới thiệu, phạm vi chuyển giao hoặc ghi chú kỹ thuật...">${sanPham.moTa}</textarea>
                </div>
            </div>

            <!-- Nút hành động -->
            <div style="margin-top: 28px; display: flex; justify-content: flex-end; align-items: center; gap: 12px; padding-top: 20px; border-top: 1px solid var(--border-color);">
                <a href="${pageContext.request.contextPath}/san-pham"
                   class="btn btn-outline">
                    Hủy bỏ
                </a>
                <button type="submit" class="btn btn-primary" id="btnLuuSanPham">
                    Lưu sản phẩm vào Bảng giá
                </button>
            </div>
        </form>
    </main>

</div>

<script>
    window.APP_CONTEXT = '${pageContext.request.contextPath}';
</script>
<script src="${pageContext.request.contextPath}/assets/js/san-pham/san-pham.js"></script>
</body>
</html>
