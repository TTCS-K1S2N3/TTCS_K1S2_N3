<%--
    Form Tạo Trường Tuỳ Chỉnh - S2-08 FE
    AC:
     - Thêm trường kiểu văn bản, số, ngày, danh sách chọn
     - Đặt được trường là bắt buộc hay không
     - Trường tuỳ chỉnh xuất hiện trong biểu mẫu, bộ lọc và bản xuất Excel
    URL: /truong-tuy-chinh/tao  (GET hiển thị form, POST xử lý)
    Servlet: TruongTuyChinhServlet (BE - dependency còn thiếu)
    Attributes truyền từ Servlet:
     - doiTuongMacDinh : String (KHACH_HANG | CO_HOI)
     - formError       : Map<String, String>
     - oldInput        : TruongTuyChinhDTO
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thêm Trường Tuỳ Chỉnh - CRM</title>
    <meta name="description" content="Khai báo trường tuỳ chỉnh mới cho Khách hàng hoặc Cơ hội trong hệ thống CRM.">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/truong-tuy-chinh/truong-tuy-chinh.css">
</head>
<body>

<div class="ttc-container">

    <!-- ===== HEADER ===== -->
    <div class="page-header">
        <h1>&#43; Thêm Trường Tuỳ Chỉnh</h1>
        <a href="${pageContext.request.contextPath}/truong-tuy-chinh"
           class="btn btn-secondary">
            &#8592; Quay lại danh sách
        </a>
    </div>

    <!-- ===== THÔNG BÁO LỖI ===== -->
    <c:if test="${not empty formError['_global']}">
        <div class="alert alert-error" role="alert">
            &#9888;&nbsp;<c:out value="${formError['_global']}"/>
        </div>
    </c:if>

    <!-- ===== FORM TẠO TRƯỜNG ===== -->
    <div class="form-card">
        <div class="form-card-header">
            <h2>Khai báo trường tuỳ chỉnh</h2>
            <p>Trường này sẽ xuất hiện trong biểu mẫu, bộ lọc và xuất Excel sau khi khai báo.</p>
        </div>

        <form id="form-tao-truong"
              method="POST"
              action="${pageContext.request.contextPath}/truong-tuy-chinh/tao"
              novalidate>

            <div class="form-card-body">

                <!-- THÔNG TIN CƠ BẢN -->
                <p class="form-section-title">Thông tin cơ bản</p>

                <!-- Đối tượng áp dụng -->
                <div class="form-group">
                    <label for="doiTuong">
                        Áp dụng cho <span class="required">*</span>
                    </label>
                    <select id="doiTuong" name="doiTuong" required>
                        <option value="KHACH_HANG"
                            <c:if test="${empty oldInput.doiTuong and (empty doiTuongMacDinh or doiTuongMacDinh == 'KHACH_HANG') or oldInput.doiTuong == 'KHACH_HANG'}">selected</c:if>>
                            Khách hàng
                        </option>
                        <option value="CO_HOI"
                            <c:if test="${doiTuongMacDinh == 'CO_HOI' or oldInput.doiTuong == 'CO_HOI'}">selected</c:if>>
                            Cơ hội
                        </option>
                    </select>
                    <span class="form-error-msg <c:if test='${not empty formError["doiTuong"]}'>visible</c:if>"
                          id="doiTuong-error">
                        <c:out value="${formError['doiTuong']}"/>
                    </span>
                </div>

                <div class="form-row">
                    <!-- Nhãn hiển thị -->
                    <div class="form-group">
                        <label for="nhanHien">
                            Nhãn hiển thị <span class="required">*</span>
                        </label>
                        <input type="text"
                               id="nhanHien"
                               name="nhanHien"
                               required
                               maxlength="100"
                               placeholder="VD: Nguồn khách hàng"
                               class="<c:if test='${not empty formError[\"nhanHien\"]}'>input-error</c:if>"
                               value="<c:out value='${oldInput.nhanHien}'/>">
                        <span class="form-error-msg <c:if test='${not empty formError["nhanHien"]}'>visible</c:if>"
                              id="nhanHien-error">
                            <c:out value="${formError['nhanHien']}"/>
                        </span>
                        <span class="form-help">Tên hiển thị trên form và bộ lọc.</span>
                    </div>

                    <!-- Tên kỹ thuật -->
                    <div class="form-group">
                        <label for="tenTruong">
                            Tên kỹ thuật <span class="required">*</span>
                        </label>
                        <input type="text"
                               id="tenTruong"
                               name="tenTruong"
                               required
                               maxlength="80"
                               placeholder="VD: nguon_khach_hang"
                               class="<c:if test='${not empty formError[\"tenTruong\"]}'>input-error</c:if>"
                               value="<c:out value='${oldInput.tenTruong}'/>">
                        <span class="form-error-msg <c:if test='${not empty formError["tenTruong"]}'>visible</c:if>"
                              id="tenTruong-error">
                            <c:out value="${formError['tenTruong']}"/>
                        </span>
                        <span class="form-help">
                            Tự động tạo từ nhãn. Chỉ dùng chữ thường, số, dấu _.
                        </span>
                    </div>
                </div>

                <!-- Kiểu dữ liệu -->
                <div class="form-group">
                    <label for="kieuDuLieu">
                        Kiểu dữ liệu <span class="required">*</span>
                    </label>
                    <select id="kieuDuLieu" name="kieuDuLieu" required>
                        <option value="">-- Chọn kiểu dữ liệu --</option>
                        <option value="VAN_BAN"
                            <c:if test="${oldInput.kieuDuLieu == 'VAN_BAN'}">selected</c:if>>
                            &#128214; Văn bản
                        </option>
                        <option value="SO"
                            <c:if test="${oldInput.kieuDuLieu == 'SO'}">selected</c:if>>
                            &#128290; Số
                        </option>
                        <option value="NGAY"
                            <c:if test="${oldInput.kieuDuLieu == 'NGAY'}">selected</c:if>>
                            &#128197; Ngày
                        </option>
                        <option value="DANH_SACH_CHON"
                            <c:if test="${oldInput.kieuDuLieu == 'DANH_SACH_CHON'}">selected</c:if>>
                            &#9660; Danh sách chọn
                        </option>
                    </select>
                    <span class="form-error-msg <c:if test='${not empty formError["kieuDuLieu"]}'>visible</c:if>"
                          id="kieuDuLieu-error">
                        <c:out value="${formError['kieuDuLieu']}"/>
                    </span>
                </div>

                <!-- Panel lựa chọn (chỉ hiện khi kiểu là DANH_SACH_CHON) -->
                <div id="options-panel"
                     class="options-panel <c:if test='${oldInput.kieuDuLieu == \"DANH_SACH_CHON\"}'>visible</c:if>">
                    <p style="font-size:0.82rem;font-weight:700;color:#7c3aed;margin:0 0 10px">
                        &#9660; Danh sách lựa chọn
                    </p>
                    <div id="options-container">
                        <c:choose>
                            <c:when test="${not empty oldInput.danhSachLuaChon}">
                                <c:forEach var="opt" items="${oldInput.danhSachLuaChon}">
                                    <div class="option-row">
                                        <input type="text"
                                               name="danhSachLuaChon[]"
                                               value="<c:out value='${opt}'/>"
                                               maxlength="200">
                                        <button type="button" class="btn-remove-option" title="Xóa">&#215;</button>
                                    </div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <%-- JS sẽ thêm 2 dòng mặc định nếu chọn kiểu DANH_SACH_CHON --%>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <button type="button" id="btn-them-lua-chon">&#43; Thêm lựa chọn</button>
                    <span class="form-error-msg <c:if test='${not empty formError["danhSachLuaChon"]}'>visible</c:if>"
                          id="options-error">
                        <c:out value="${formError['danhSachLuaChon']}"/>
                    </span>
                </div>

                <!-- CÀI ĐẶT TRƯỜNG -->
                <p class="form-section-title" style="margin-top:24px">Cài đặt</p>

                <!-- Bắt buộc -->
                <div class="form-group">
                    <label class="toggle-group">
                        <input type="checkbox"
                               id="batBuoc"
                               name="batBuoc"
                               value="true"
                               <c:if test="${oldInput.batBuoc}">checked</c:if>>
                        <div>
                            <div class="toggle-label">Bắt buộc nhập</div>
                            <div class="toggle-desc">
                                Người dùng bắt buộc phải điền trường này khi tạo/sửa bản ghi.
                            </div>
                        </div>
                    </label>
                </div>

                <!-- Hiện trong bộ lọc -->
                <div class="form-group">
                    <label class="toggle-group">
                        <input type="checkbox"
                               id="hienThiBoDac"
                               name="hienThiBoDac"
                               value="true"
                               <c:if test="${oldInput.hienThiBoDac}">checked</c:if>>
                        <div>
                            <div class="toggle-label">Hiển thị trong bộ lọc</div>
                            <div class="toggle-desc">
                                Trường này sẽ xuất hiện trong thanh bộ lọc của danh sách Khách hàng / Cơ hội.
                            </div>
                        </div>
                    </label>
                </div>

                <!-- Hiện trong Excel -->
                <div class="form-group">
                    <label class="toggle-group">
                        <input type="checkbox"
                               id="hienThiExcel"
                               name="hienThiExcel"
                               value="true"
                               <c:if test="${oldInput.hienThiExcel}">checked</c:if>>
                        <div>
                            <div class="toggle-label">Xuất ra Excel</div>
                            <div class="toggle-desc">
                                Trường này sẽ có trong file Excel khi xuất danh sách.
                            </div>
                        </div>
                    </label>
                </div>

                <!-- Thứ tự hiển thị -->
                <div class="form-group">
                    <label for="thuTu">Thứ tự hiển thị</label>
                    <input type="number"
                           id="thuTu"
                           name="thuTu"
                           min="1"
                           max="999"
                           placeholder="VD: 1"
                           style="max-width:120px"
                           value="<c:out value='${not empty oldInput.thuTu ? oldInput.thuTu : \"1\"}'/>">>
                    <span class="form-help">
                        Số nhỏ hơn hiển thị trước. Có thể điều chỉnh sau.
                    </span>
                </div>

            </div><%-- end form-card-body --%>

            <div class="form-card-footer">
                <a href="${pageContext.request.contextPath}/truong-tuy-chinh"
                   class="btn btn-secondary">Hủy</a>
                <button type="submit" id="btn-luu-truong" class="btn btn-primary">
                    &#10003; Lưu trường tuỳ chỉnh
                </button>
            </div>

        </form>
    </div><%-- end form-card --%>

</div><%-- end ttc-container --%>

<script src="${pageContext.request.contextPath}/assets/js/truong-tuy-chinh/truong-tuy-chinh.js"></script>
</body>
</html>
