<%--
    Form Tạo Trường Tuỳ Chỉnh - S2-08 FE
    Story: Là Quản trị hệ thống, tôi muốn khai báo trường tuỳ chỉnh cho khách hàng và cơ hội, để đưa được những cột mà nhân viên đang tự thêm trong Excel vào hệ thống.
    Acceptance Criteria:
     - Thêm trường kiểu văn bản, số, ngày, danh sách chọn
     - Đặt được trường là bắt buộc hay không
     - Trường tuỳ chỉnh xuất hiện trong biểu mẫu, bộ lọc và bản xuất Excel
    URL: /truong-tuy-chinh/tao  (GET hiển thị form, POST xử lý)
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Khai báo Trường Tuỳ Chỉnh Mới | CRM Doanh nghiệp</title>
    <meta name="description" content="Khai báo trường tuỳ chỉnh mới cho Khách hàng hoặc Cơ hội trong hệ thống CRM.">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/truong-tuy-chinh/truong-tuy-chinh.css">
</head>
<body>

<div class="ttc-container" style="max-width: 960px;">

    <!-- ===== HEADER ===== -->
    <header class="page-header">
        <div class="page-header-title">
            <h1>Khai báo Trường Tuỳ Chỉnh Mới</h1>
            <p>Định nghĩa trường mở rộng cho phép nhân sự thu thập các thông tin đặc thù của doanh nghiệp.</p>
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

    <!-- ===== FORM TẠO TRƯỜNG ===== -->
    <div class="form-card">
        <div class="form-card-header">
            <h2>Cấu hình thuộc tính trường</h2>
            <p>Trường sau khi lưu sẽ tự động xuất hiện trong biểu mẫu nhập liệu, thanh bộ lọc và file xuất Excel.</p>
        </div>

        <form id="form-tao-truong"
              method="POST"
              action="${pageContext.request.contextPath}/truong-tuy-chinh/tao"
              novalidate>

            <div class="form-card-body">

                <!-- 1. THÔNG TIN CƠ BẢN -->
                <p class="form-section-title">1. Thông tin định danh & Kiểu dữ liệu</p>

                <!-- Đối tượng áp dụng -->
                <div class="form-group">
                    <label for="doiTuong">
                        Đối tượng dữ liệu áp dụng <span class="required">*</span>
                    </label>
                    <select id="doiTuong" name="doiTuong" required>
                        <option value="KHACH_HANG"
                            <c:if test="${empty oldInput.doiTuong and (empty doiTuongMacDinh or doiTuongMacDinh == 'KHACH_HANG') or oldInput.doiTuong == 'KHACH_HANG'}">selected</c:if>>
                            Khách hàng (Doanh nghiệp & Tổ chức)
                        </option>
                        <option value="CO_HOI"
                            <c:if test="${doiTuongMacDinh == 'CO_HOI' or oldInput.doiTuong == 'CO_HOI'}">selected</c:if>>
                            Cơ hội (Deals & Pipeline bán hàng)
                        </option>
                    </select>
                    <span class="form-error-msg <c:if test='${not empty formError["doiTuong"]}'>visible</c:if>"
                          id="doiTuong-error">
                        <c:out value="${formError['doiTuong']}"/>
                    </span>
                    <span class="form-help">Chọn đối tượng cần mở rộng thêm các cột dữ liệu.</span>
                </div>

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
                               placeholder="VD: Nguồn giới thiệu, Quy mô chi nhánh, Đối thủ chính"
                               class="<c:if test='${not empty formError[\"nhanHien\"]}'>input-error</c:if>"
                               value="<c:out value='${oldInput.nhanHien}'/>">
                        <span class="form-error-msg <c:if test='${not empty formError["nhanHien"]}'>visible</c:if>"
                              id="nhanHien-error">
                            <c:out value="${formError['nhanHien']}"/>
                        </span>
                        <span class="form-help">Tên trường hiển thị cho người dùng trên form và thanh bộ lọc.</span>
                    </div>

                    <!-- Tên kỹ thuật -->
                    <div class="form-group">
                        <label for="tenTruong">
                            Tên kỹ thuật (Mã cột hệ thống) <span class="required">*</span>
                        </label>
                        <input type="text"
                               id="tenTruong"
                               name="tenTruong"
                               required
                               maxlength="80"
                               placeholder="VD: nguon_gioi_thieu, quy_mo_chi_nhanh"
                               class="<c:if test='${not empty formError[\"tenTruong\"]}'>input-error</c:if>"
                               value="<c:out value='${oldInput.tenTruong}'/>">
                        <span class="form-error-msg <c:if test='${not empty formError["tenTruong"]}'>visible</c:if>"
                              id="tenTruong-error">
                            <c:out value="${formError['tenTruong']}"/>
                        </span>
                        <span class="form-help">
                            Tự động sinh từ nhãn hiển thị (dạng <code>snake_case</code>, chỉ gồm chữ thường, số, dấu _).
                        </span>
                    </div>
                </div>

                <!-- Kiểu dữ liệu (AC1) -->
                <div class="form-group">
                    <label for="kieuDuLieu">
                        Kiểu dữ liệu trường (AC1) <span class="required">*</span>
                    </label>
                    <select id="kieuDuLieu" name="kieuDuLieu" required>
                        <option value="">-- Chọn kiểu dữ liệu phù hợp --</option>
                        <option value="VAN_BAN"
                            <c:if test="${oldInput.kieuDuLieu == 'VAN_BAN'}">selected</c:if>>
                            Văn bản (Text) - Đoạn chữ, ghi chú, mã số linh hoạt
                        </option>
                        <option value="SO"
                            <c:if test="${oldInput.kieuDuLieu == 'SO'}">selected</c:if>>
                            Số (Number) - Số lượng, doanh thu, ngân sách, số nguyên hoặc thập phân
                        </option>
                        <option value="NGAY"
                            <c:if test="${oldInput.kieuDuLieu == 'NGAY'}">selected</c:if>>
                            Ngày (Date) - Ngày ký kết, mốc hạn, ngày tiếp cận, thời điểm
                        </option>
                        <option value="DANH_SACH_CHON"
                            <c:if test="${oldInput.kieuDuLieu == 'DANH_SACH_CHON'}">selected</c:if>>
                            Danh sách chọn (Dropdown) - Cho phép chọn một giá trị từ các lựa chọn định sẵn
                        </option>
                    </select>
                    <span class="form-error-msg <c:if test='${not empty formError["kieuDuLieu"]}'>visible</c:if>"
                          id="kieuDuLieu-error">
                        <c:out value="${formError['kieuDuLieu']}"/>
                    </span>
                    <span class="form-help">Chọn định dạng chuẩn để hệ thống tự động sinh ô nhập và bộ lọc tương ứng.</span>
                </div>

                <!-- Panel lựa chọn (chỉ hiện khi kiểu là DANH_SACH_CHON) -->
                <div id="options-panel"
                     class="options-panel <c:if test='${oldInput.kieuDuLieu == \"DANH_SACH_CHON\"}'>visible</c:if>">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
                        <span style="font-size:0.88rem;font-weight:700;color:#6b21a8; display: flex; align-items: center; gap: 4px;">
                            <span class="material-symbols-outlined icon-xs" aria-hidden="true">list</span>
                            <span>Khai báo các lựa chọn cho danh sách thả xuống</span>
                        </span>
                        <span style="font-size: 0.78rem; color: #7e22ce;">Tối thiểu 1 lựa chọn</span>
                    </div>

                    <div id="options-container">
                        <c:choose>
                            <c:when test="${not empty oldInput.danhSachLuaChon}">
                                <c:forEach var="opt" items="${oldInput.danhSachLuaChon}">
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
                            </c:when>
                            <c:otherwise>
                                <%-- JS sẽ khởi tạo 2 dòng mặc định nếu người dùng chọn DANH_SACH_CHON --%>
                            </c:otherwise>
                        </c:choose>
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

                <!-- 2. CÀI ĐẶT RÀNG BUỘC & HIỂN THỊ (AC2, AC3) -->
                <p class="form-section-title" style="margin-top:28px">2. Quy định nghiệp vụ & Xuất hiện trong hệ thống (AC2, AC3)</p>

                <!-- Bắt buộc (AC2) -->
                <div class="form-group">
                    <label class="toggle-group">
                        <input type="checkbox"
                               id="batBuoc"
                               name="batBuoc"
                               value="true"
                               <c:if test="${oldInput.batBuoc}">checked</c:if>>
                        <div>
                            <div class="toggle-label" style="display: flex; align-items: center; gap: 4px;">
                                <span class="material-symbols-outlined icon-xs" style="color: #d97706;" aria-hidden="true">star</span>
                                <span>Đặt làm trường bắt buộc nhập (AC2)</span>
                            </div>
                            <div class="toggle-desc">
                                Khi bật, người dùng bắt buộc phải điền dữ liệu trường này khi lưu Khách hàng hoặc Cơ hội. Biểu mẫu sẽ hiển thị dấu hoa thị đỏ (*).
                            </div>
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
                               <c:if test="${empty oldInput.hienThiBoDac or oldInput.hienThiBoDac}">checked</c:if>>
                        <div>
                            <div class="toggle-label" style="display: flex; align-items: center; gap: 4px;">
                                <span class="material-symbols-outlined icon-xs" style="color: #2563eb;" aria-hidden="true">search</span>
                                <span>Xuất hiện trong thanh bộ lọc danh sách (AC3)</span>
                            </div>
                            <div class="toggle-desc">
                                Tự động tạo ô tìm kiếm tương ứng theo kiểu dữ liệu (văn bản, số, khoảng ngày, hoặc dropdown) trên thanh lọc.
                            </div>
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
                               <c:if test="${empty oldInput.hienThiExcel or oldInput.hienThiExcel}">checked</c:if>>
                        <div>
                            <div class="toggle-label" style="display: flex; align-items: center; gap: 4px;">
                                <span class="material-symbols-outlined icon-xs" style="color: #059669;" aria-hidden="true">table_view</span>
                                <span>Tự động xuất ra file Excel (AC3)</span>
                            </div>
                            <div class="toggle-desc">
                                Khi nhân sự xuất danh sách Khách hàng hoặc Cơ hội ra file Excel/CSV, trường này sẽ tự động được thêm thành một cột dữ liệu.
                            </div>
                        </div>
                    </label>
                </div>

                <!-- Thứ tự hiển thị -->
                <div class="form-group">
                    <label for="thuTu">Thứ tự ưu tiên hiển thị</label>
                    <input type="number"
                           id="thuTu"
                           name="thuTu"
                           min="1"
                           max="999"
                           style="max-width:140px"
                           value="<c:out value='${not empty oldInput.thuTu ? oldInput.thuTu : \"1\"}'/>">
                    <span class="form-help">Số thứ tự càng nhỏ sẽ hiển thị càng lên trước trên biểu mẫu.</span>
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
                            <span id="previewLabelText">Tên trường</span>
                            <span id="previewRequiredStar" class="required" style="display:none;">*</span>
                        </label>
                        <div id="previewInputArea">
                            <input type="text" placeholder="Nhập giá trị văn bản..." disabled style="background:#fff; border: 1.5px solid #cbd5e1; border-radius: 8px; padding: 10px 14px; width: 100%;">
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
                <button type="submit" id="btn-luu-truong" class="btn btn-primary" style="display: inline-flex; align-items: center; gap: 4px;">
                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">save</span>
                    <span>Lưu trường tuỳ chỉnh</span>
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
