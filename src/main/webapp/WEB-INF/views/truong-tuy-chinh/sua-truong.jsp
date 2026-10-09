<%--
    Form Sửa Trường Tuỳ Chỉnh - S2-08 FE
    Story: Là Quản trị hệ thống, tôi muốn khai báo trường tuỳ chỉnh cho khách hàng và cơ hội, để đưa được những cột mà nhân viên đang tự thêm trong Excel vào hệ thống.
    Acceptance Criteria:
     - Thêm trường kiểu văn bản, số, ngày, danh sách chọn
     - Đặt được trường là bắt buộc hay không
     - Trường tuỳ chỉnh xuất hiện trong biểu mẫu, bộ lọc và bản xuất Excel
    URL: /truong-tuy-chinh/sua?id={id}  (GET hiển thị, POST xử lý)
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cập nhật Trường Tuỳ Chỉnh | CRM Doanh nghiệp</title>
    <meta name="description" content="Chỉnh sửa trường tuỳ chỉnh cho Khách hàng hoặc Cơ hội trong hệ thống CRM.">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/truong-tuy-chinh/truong-tuy-chinh.css">
</head>
<body>

<div class="ttc-container" style="max-width: 960px;">

    <!-- ===== HEADER ===== -->
    <header class="page-header">
        <div class="page-header-title">
            <h1>Cập nhật Trường Tuỳ Chỉnh</h1>
            <p>
                Điều chỉnh nhãn hiển thị, cấu hình bắt buộc và phạm vi xuất hiện trên biểu mẫu / bộ lọc / Excel cho trường
                <strong style="color: #4f46e5;"><code><c:out value="${truong.tenTruong}"/></code> (<c:out value="${truong.nhanHien}"/>)</strong>
            </p>
        </div>
        <div class="page-header-actions">
            <a href="${pageContext.request.contextPath}/truong-tuy-chinh" class="btn btn-secondary" style="display: inline-flex; align-items: center; gap: 4px;">
                <span class="material-symbols-outlined icon-xs" aria-hidden="true">arrow_back</span>
                <span>Quay lại danh sách</span>
            </a>
        </div>
    </header>

    <!-- ===== THÔNG BÁO LỖI ===== -->
    <c:if test="${not empty formError['_global'] or not empty formError['global']}">
        <div class="alert alert-error" role="alert">
            <div style="display: flex; align-items: center; gap: 6px;">
                <span class="material-symbols-outlined icon-sm" aria-hidden="true">error</span>
                <span><strong>Lỗi:</strong> <c:out value="${not empty formError['_global'] ? formError['_global'] : formError['global']}"/></span>
            </div>
            <span class="alert-close" onclick="this.parentElement.style.display='none';" title="Đóng" aria-label="Đóng">
                <span class="material-symbols-outlined icon-xs" aria-hidden="true">close</span>
            </span>
        </div>
    </c:if>

    <div class="alert alert-info" style="margin-bottom:20px; display: flex; align-items: flex-start; gap: 8px;">
        <span class="material-symbols-outlined icon-sm" style="color: #0284c7; margin-top: 2px;" aria-hidden="true">info</span>
        <div>
            <strong>Lưu ý bảo toàn dữ liệu:</strong> Tên kỹ thuật và Kiểu dữ liệu được cố định để không làm sai lệch các bản ghi đã lưu trữ trong cơ sở dữ liệu. Bạn có thể thay đổi nhãn hiển thị, quy định bắt buộc, hiển thị bộ lọc/Excel và các giá trị lựa chọn.
        </div>
    </div>

    <!-- ===== FORM SỬA ===== -->
    <div class="form-card">
        <div class="form-card-header">
            <h2>Chỉnh sửa thông số trường</h2>
            <p>
                Đối tượng:
                <strong>
                    <c:choose>
                        <c:when test="${truong.doiTuong == 'KHACH_HANG'}">Khách hàng</c:when>
                        <c:when test="${truong.doiTuong == 'CO_HOI'}">Cơ hội</c:when>
                        <c:otherwise><c:out value="${truong.doiTuong}"/></c:otherwise>
                    </c:choose>
                </strong>
                &nbsp;·&nbsp; Kiểu dữ liệu:
                <strong>
                    <c:choose>
                        <c:when test="${truong.kieuDuLieu == 'VAN_BAN'}">Văn bản (Text)</c:when>
                        <c:when test="${truong.kieuDuLieu == 'SO'}">Số (Number)</c:when>
                        <c:when test="${truong.kieuDuLieu == 'NGAY'}">Ngày (Date)</c:when>
                        <c:when test="${truong.kieuDuLieu == 'DANH_SACH_CHON'}">Danh sách chọn</c:when>
                        <c:otherwise><c:out value="${truong.kieuDuLieu}"/></c:otherwise>
                    </c:choose>
                </strong>
            </p>
        </div>

        <form id="form-sua-truong"
              method="POST"
              action="${pageContext.request.contextPath}/truong-tuy-chinh/sua"
              novalidate>

            <input type="hidden" name="id" value="${truong.id}">
            <%-- kieuDuLieu và doiTuong không cho thay đổi sau khi tạo --%>
            <input type="hidden" id="kieuDuLieu" name="kieuDuLieu" value="${truong.kieuDuLieu}">
            <input type="hidden" id="doiTuong" name="doiTuong" value="${truong.doiTuong}">

            <div class="form-card-body">

                <!-- 1. THÔNG TIN CƠ BẢN -->
                <p class="form-section-title">1. Nhãn hiển thị & Định danh</p>

                <div class="form-row">
                    <!-- Nhãn hiển thị -->
                    <div class="form-group">
                        <label for="nhanHien">
                            Nhãn hiển thị (Tên tiếng Việt) <span class="required">*</span>
                        </label>
                        <input type="text"
                               id="nhanHien"
                               name="nhanHien"
                               required
                               maxlength="150"
                               class="<c:if test='${not empty formError[\"nhanHien\"]}'>input-error</c:if>"
                               value="<c:out value='${truong.nhanHien}'/>">
                        <span class="form-error-msg <c:if test='${not empty formError["nhanHien"]}'>visible</c:if>"
                              id="nhanHien-error">
                            <c:out value="${formError['nhanHien']}"/>
                        </span>
                        <span class="form-help">Nhãn mới sẽ cập nhật ngay trên tất cả biểu mẫu và thanh lọc.</span>
                    </div>

                    <!-- Tên kỹ thuật (readonly) -->
                    <div class="form-group">
                        <label for="tenTruong">Tên kỹ thuật (Mã hệ thống)</label>
                        <input type="text"
                               id="tenTruong"
                               name="tenTruong"
                               value="<c:out value='${truong.tenTruong}'/>"
                               readonly
                               style="background:#f1f5f9;color:#475569;font-family:monospace;font-weight:700;cursor:not-allowed">
                        <span class="form-help">
                            Cố định để đảm bảo tương thích dữ liệu đã lưu.
                        </span>
                    </div>
                </div>

                <!-- Danh sách lựa chọn (chỉ hiện khi kiểu DANH_SACH_CHON) -->
                <c:if test="${truong.kieuDuLieu == 'DANH_SACH_CHON'}">
                    <div id="options-panel" class="options-panel visible">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
                            <span style="font-size:0.88rem;font-weight:700;color:#6b21a8; display: flex; align-items: center; gap: 4px;">
                                <span class="material-symbols-outlined icon-xs" aria-hidden="true">list</span>
                                <span>Cập nhật danh sách các lựa chọn</span>
                            </span>
                            <span style="font-size:0.78rem; color:#7e22ce;">Tối thiểu 1 lựa chọn</span>
                        </div>

                        <div id="options-container">
                            <c:forEach var="opt" items="${truong.danhSachLuaChon}">
                                <div class="option-row">
                                    <input type="text"
                                           name="danhSachLuaChon[]"
                                           value="<c:out value='${opt}'/>"
                                           maxlength="200"
                                           placeholder="Nhập giá trị lựa chọn...">
                                    <button type="button" class="btn-remove-option" title="Xóa lựa chọn này" aria-label="Xóa lựa chọn này">
                                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">delete</span>
                                    </button>
                                </div>
                            </c:forEach>
                        </div>
                        <div style="margin-top: 10px;">
                            <button type="button" id="btn-them-lua-chon" class="btn btn-secondary btn-sm" style="display: inline-flex; align-items: center; gap: 4px;">
                                <span class="material-symbols-outlined icon-xs" aria-hidden="true">add</span>
                                <span>Thêm lựa chọn khác</span>
                            </button>
                        </div>
                        <span class="form-error-msg <c:if test='${not empty formError["danhSachLuaChon"]}'>visible</c:if>"
                              id="options-error">
                            <c:out value="${formError['danhSachLuaChon']}"/>
                        </span>
                    </div>
                </c:if>

                <!-- 2. CÀI ĐẶT RÀNG BUỘC & HIỂN THỊ (AC2, AC3) -->
                <p class="form-section-title" style="margin-top:28px">2. Quy định nghiệp vụ & Xuất hiện trong hệ thống (AC2, AC3)</p>

                <!-- Bắt buộc (AC2) -->
                <div class="form-group">
                    <label class="toggle-group">
                        <input type="checkbox"
                               id="batBuoc"
                               name="batBuoc"
                               value="true"
                               <c:if test="${truong.batBuoc}">checked</c:if>>
                        <div>
                            <div class="toggle-label" style="display: flex; align-items: center; gap: 4px;">
                                <span class="material-symbols-outlined icon-xs" style="color: #d97706;" aria-hidden="true">star</span>
                                <span>Đặt làm trường bắt buộc nhập (AC2)</span>
                            </div>
                            <div class="toggle-desc">Yêu cầu người dùng bắt buộc phải nhập giá trị cho trường này khi lưu dữ liệu.</div>
                        </div>
                    </label>
                </div>

                <!-- Hiện trong bộ lọc (AC3) -->
                <div class="form-group">
                    <label class="toggle-group">
                        <input type="checkbox"
                               id="hienThiBoDac"
                               name="hienThiBoDac"
                               value="true"
                               <c:if test="${truong.hienThiBoDac}">checked</c:if>>
                        <div>
                            <div class="toggle-label" style="display: flex; align-items: center; gap: 4px;">
                                <span class="material-symbols-outlined icon-xs" style="color: #2563eb;" aria-hidden="true">search</span>
                                <span>Xuất hiện trong thanh bộ lọc danh sách (AC3)</span>
                            </div>
                            <div class="toggle-desc">Cho phép tìm kiếm nhanh theo trường này trên danh sách Khách hàng / Cơ hội.</div>
                        </div>
                    </label>
                </div>

                <!-- Hiện trong Excel (AC3) -->
                <div class="form-group">
                    <label class="toggle-group">
                        <input type="checkbox"
                               id="hienThiExcel"
                               name="hienThiExcel"
                               value="true"
                               <c:if test="${truong.hienThiExcel}">checked</c:if>>
                        <div>
                            <div class="toggle-label" style="display: flex; align-items: center; gap: 4px;">
                                <span class="material-symbols-outlined icon-xs" style="color: #059669;" aria-hidden="true">table_view</span>
                                <span>Tự động xuất ra file Excel (AC3)</span>
                            </div>
                            <div class="toggle-desc">Trường sẽ có cột tương ứng trong file xuất Excel khi người dùng bấm Xuất Excel.</div>
                        </div>
                    </label>
                </div>

                <!-- Trạng thái -->
                <div class="form-group">
                    <label class="toggle-group">
                        <input type="checkbox"
                               id="dangHoatDong"
                               name="dangHoatDong"
                               value="true"
                               <c:if test="${truong.dangHoatDong}">checked</c:if>>
                        <div>
                            <div class="toggle-label" style="display: flex; align-items: center; gap: 4px;">
                                <span class="material-symbols-outlined icon-xs" aria-hidden="true">toggle_on</span>
                                <span>Kích hoạt hoạt động</span>
                            </div>
                            <div class="toggle-desc">Khi tắt, trường sẽ tạm thời ẩn khỏi biểu mẫu và bộ lọc nhưng không làm mất dữ liệu lịch sử.</div>
                        </div>
                    </label>
                </div>

                <!-- Thứ tự -->
                <div class="form-group">
                    <label for="thuTu">Thứ tự ưu tiên hiển thị</label>
                    <input type="number"
                           id="thuTu"
                           name="thuTu"
                           min="1"
                           max="999"
                           style="max-width:140px"
                           value="<c:out value='${truong.thuTu}'/>">
                </div>

                <!-- 3. LIVE FIELD PREVIEW WIDGET -->
                <div class="live-preview-box">
                    <div style="font-weight: 700; color: #1e293b; font-size: 0.95rem; margin-bottom: 8px; display: flex; align-items: center; gap: 8px;">
                        <span class="material-symbols-outlined icon-sm" style="color: #4f46e5;" aria-hidden="true">visibility</span>
                        <span>Trực quan hoá thời gian thực (Live Field Preview)</span>
                    </div>
                    <p style="font-size: 0.82rem; color: #64748b; margin-bottom: 14px;">
                        Hình ảnh thực tế mà nhân viên sẽ nhìn thấy khi nhập liệu trên form:
                    </p>

                    <div class="preview-field-container" id="previewFieldContainer">
                        <label id="previewLabel" style="font-weight: 600; font-size: 0.9rem; color: #334155; display: block; margin-bottom: 6px;">
                            <span id="previewLabelText"><c:out value="${truong.nhanHien}"/></span>
                            <span id="previewRequiredStar" class="required" style="${truong.batBuoc ? '' : 'display:none;'}">*</span>
                        </label>
                        <div id="previewInputArea">
                            <c:choose>
                                <c:when test="${truong.kieuDuLieu == 'VAN_BAN'}">
                                    <input type="text" placeholder="Nhập văn bản..." disabled style="background:#fff; border: 1.5px solid #cbd5e1; border-radius: 8px; padding: 10px 14px; width: 100%;">
                                </c:when>
                                <c:when test="${truong.kieuDuLieu == 'SO'}">
                                    <input type="number" placeholder="Nhập số..." disabled style="background:#fff; border: 1.5px solid #cbd5e1; border-radius: 8px; padding: 10px 14px; width: 100%;">
                                </c:when>
                                <c:when test="${truong.kieuDuLieu == 'NGAY'}">
                                    <input type="date" disabled style="background:#fff; border: 1.5px solid #cbd5e1; border-radius: 8px; padding: 10px 14px; width: 100%;">
                                </c:when>
                                <c:when test="${truong.kieuDuLieu == 'DANH_SACH_CHON'}">
                                    <select disabled style="background:#fff; border: 1.5px solid #cbd5e1; border-radius: 8px; padding: 10px 14px; width: 100%;">
                                        <c:forEach var="opt" items="${truong.danhSachLuaChon}">
                                            <option><c:out value="${opt}"/></option>
                                        </c:forEach>
                                    </select>
                                </c:when>
                            </c:choose>
                        </div>
                        <span id="previewHelpText" class="form-help" style="margin-top: 4px; display: block; color: #94a3b8;">
                            Trường tuỳ chỉnh trên biểu mẫu
                        </span>
                    </div>
                </div>

            </div><%-- end form-card-body --%>

            <div class="form-card-footer">
                <a href="${pageContext.request.contextPath}/truong-tuy-chinh" class="btn btn-secondary" style="display: inline-flex; align-items: center; gap: 4px;">
                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">close</span>
                    <span>Hủy bỏ</span>
                </a>
                <button type="submit" id="btn-luu-sua" class="btn btn-primary" style="display: inline-flex; align-items: center; gap: 4px;">
                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">save</span>
                    <span>Lưu các thay đổi</span>
                </button>
            </div>

        </form>
    </div><%-- end form-card --%>

</div><%-- end ttc-container --%>

<script>
    window.APP_CONTEXT = '${pageContext.request.contextPath}';
</script>
<script src="${pageContext.request.contextPath}/assets/js/truong-tuy-chinh/truong-tuy-chinh.js"></script>
</body>
</html>
