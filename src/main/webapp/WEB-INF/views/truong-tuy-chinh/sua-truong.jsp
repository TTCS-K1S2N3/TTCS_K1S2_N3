<%--
    Form Sửa Trường Tuỳ Chỉnh - S2-08 FE
    URL: /truong-tuy-chinh/sua?id={id}  (GET hiển thị, POST xử lý)
    Servlet: TruongTuyChinhServlet (BE - dependency còn thiếu)
    Attributes truyền từ Servlet:
     - truong    : TruongTuyChinhDTO (dữ liệu hiện tại)
     - formError : Map<String, String>
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sửa Trường Tuỳ Chỉnh - CRM</title>
    <meta name="description" content="Chỉnh sửa trường tuỳ chỉnh cho Khách hàng hoặc Cơ hội trong hệ thống CRM.">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/truong-tuy-chinh/truong-tuy-chinh.css">
</head>
<body>

<div class="ttc-container">

    <!-- ===== HEADER ===== -->
    <div class="page-header">
        <h1>&#9998; Sửa Trường Tuỳ Chỉnh</h1>
        <a href="${pageContext.request.contextPath}/truong-tuy-chinh"
           class="btn btn-secondary">&#8592; Quay lại danh sách</a>
    </div>

    <c:if test="${not empty formError['_global']}">
        <div class="alert alert-error" role="alert">
            &#9888;&nbsp;<c:out value="${formError['_global']}"/>
        </div>
    </c:if>

    <div class="alert alert-info" style="margin-bottom:16px">
        &#9432;&nbsp;Không thể đổi kiểu dữ liệu sau khi đã có dữ liệu thực tế được nhập.
        Chỉ có thể sửa nhãn, cài đặt và danh sách lựa chọn.
    </div>

    <!-- ===== FORM SỬA ===== -->
    <div class="form-card">
        <div class="form-card-header">
            <h2>Chỉnh sửa trường tuỳ chỉnh</h2>
            <p>
                Đối tượng:
                <strong>
                    <c:choose>
                        <c:when test="${truong.doiTuong == 'KHACH_HANG'}">Khách hàng</c:when>
                        <c:when test="${truong.doiTuong == 'CO_HOI'}">Cơ hội</c:when>
                        <c:otherwise><c:out value="${truong.doiTuong}"/></c:otherwise>
                    </c:choose>
                </strong>
                &nbsp;·&nbsp; Kiểu:
                <strong>
                    <c:choose>
                        <c:when test="${truong.kieuDuLieu == 'VAN_BAN'}">Văn bản</c:when>
                        <c:when test="${truong.kieuDuLieu == 'SO'}">Số</c:when>
                        <c:when test="${truong.kieuDuLieu == 'NGAY'}">Ngày</c:when>
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
            <input type="hidden" name="kieuDuLieu" value="${truong.kieuDuLieu}">
            <input type="hidden" name="doiTuong" value="${truong.doiTuong}">

            <div class="form-card-body">

                <p class="form-section-title">Thông tin cơ bản</p>

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
                               class="<c:if test='${not empty formError[\"nhanHien\"]}'>input-error</c:if>"
                               value="<c:out value='${truong.nhanHien}'/>">
                        <span class="form-error-msg <c:if test='${not empty formError["nhanHien"]}'>visible</c:if>"
                              id="nhanHien-error">
                            <c:out value="${formError['nhanHien']}"/>
                        </span>
                    </div>

                    <!-- Tên kỹ thuật (readonly) -->
                    <div class="form-group">
                        <label for="tenTruong">Tên kỹ thuật</label>
                        <input type="text"
                               id="tenTruong"
                               name="tenTruong"
                               value="<c:out value='${truong.tenTruong}'/>"
                               readonly
                               style="background:#f1f5f9;color:#64748b;cursor:not-allowed">
                        <span class="form-help">
                            Tên kỹ thuật không thể thay đổi sau khi tạo.
                        </span>
                    </div>
                </div>

                <!-- Danh sách lựa chọn (chỉ hiện khi kiểu DANH_SACH_CHON) -->
                <c:if test="${truong.kieuDuLieu == 'DANH_SACH_CHON'}">
                    <%-- Panel luôn visible khi đang sửa DANH_SACH_CHON --%>
                    <div id="options-panel" class="options-panel visible">
                        <p style="font-size:0.82rem;font-weight:700;color:#7c3aed;margin:0 0 10px">
                            &#9660; Danh sách lựa chọn
                        </p>
                        <div id="options-container">
                            <c:forEach var="opt" items="${truong.danhSachLuaChon}">
                                <div class="option-row">
                                    <input type="text"
                                           name="danhSachLuaChon[]"
                                           value="<c:out value='${opt}'/>"
                                           maxlength="200">
                                    <button type="button" class="btn-remove-option" title="Xóa">&#215;</button>
                                </div>
                            </c:forEach>
                        </div>
                        <button type="button" id="btn-them-lua-chon">&#43; Thêm lựa chọn</button>
                        <span class="form-error-msg <c:if test='${not empty formError["danhSachLuaChon"]}'>visible</c:if>"
                              id="options-error">
                            <c:out value="${formError['danhSachLuaChon']}"/>
                        </span>
                        <p class="form-help">
                            Thêm/xóa lựa chọn sẽ ảnh hưởng đến dữ liệu đã nhập nếu lựa chọn cũ bị xóa.
                        </p>
                    </div>
                </c:if>

                <p class="form-section-title" style="margin-top:24px">Cài đặt</p>

                <!-- Bắt buộc -->
                <div class="form-group">
                    <label class="toggle-group">
                        <input type="checkbox"
                               id="batBuoc"
                               name="batBuoc"
                               value="true"
                               <c:if test="${truong.batBuoc}">checked</c:if>>
                        <div>
                            <div class="toggle-label">Bắt buộc nhập</div>
                            <div class="toggle-desc">Người dùng bắt buộc phải điền trường này.</div>
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
                               <c:if test="${truong.hienThiBoDac}">checked</c:if>>
                        <div>
                            <div class="toggle-label">Hiển thị trong bộ lọc</div>
                            <div class="toggle-desc">Trường xuất hiện trong thanh bộ lọc danh sách.</div>
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
                               <c:if test="${truong.hienThiExcel}">checked</c:if>>
                        <div>
                            <div class="toggle-label">Xuất ra Excel</div>
                            <div class="toggle-desc">Trường có trong file Excel khi xuất danh sách.</div>
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
                            <div class="toggle-label">Kích hoạt trường</div>
                            <div class="toggle-desc">Tắt để ẩn trường khỏi form mà không xóa dữ liệu.</div>
                        </div>
                    </label>
                </div>

                <!-- Thứ tự -->
                <div class="form-group">
                    <label for="thuTu">Thứ tự hiển thị</label>
                    <input type="number"
                           id="thuTu"
                           name="thuTu"
                           min="1"
                           max="999"
                           style="max-width:120px"
                           value="<c:out value='${truong.thuTu}'/>">
                </div>

            </div><%-- end form-card-body --%>

            <div class="form-card-footer">
                <a href="${pageContext.request.contextPath}/truong-tuy-chinh"
                   class="btn btn-secondary">Hủy</a>
                <button type="submit" id="btn-luu-sua" class="btn btn-primary">
                    &#10003; Lưu thay đổi
                </button>
            </div>

        </form>
    </div>

</div><%-- end ttc-container --%>

<script src="${pageContext.request.contextPath}/assets/js/truong-tuy-chinh/truong-tuy-chinh.js"></script>
</body>
</html>
