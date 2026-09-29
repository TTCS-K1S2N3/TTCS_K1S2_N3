<%--
    Sửa Tài khoản Người Dùng - S1-08 FE
    AC:
     - Sửa thông tin tài khoản (họ tên, nhóm, vai trò)
     - Email trùng bị từ chối kèm thông báo cụ thể
    URL: /nguoi-dung/sua?id={id}  (GET hiển thị form, POST xử lý)
    Servlet: NguoiDungServlet (BE - dependency còn thiếu)
    Attributes truyền từ Servlet:
     - nguoiDung : NguoiDung (dữ liệu hiện tại)
     - dsVaiTro  : List<VaiTro>
     - dsNhom    : List<NhomKinhDoanh>
     - formError : Map<String,String>
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sửa Tài khoản - CRM</title>
    <meta name="description" content="Chỉnh sửa thông tin, vai trò của tài khoản người dùng trong hệ thống CRM.">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/nguoi-dung/nguoi-dung.css">
</head>
<body>

<div class="nguoi-dung-container">

    <!-- ===== HEADER ===== -->
    <div class="page-header">
        <h1>&#9998; Sửa Tài khoản</h1>
        <a href="${pageContext.request.contextPath}/nguoi-dung"
           class="btn btn-secondary">
            &#8592; Quay lại danh sách
        </a>
    </div>

    <!-- ===== THÔNG BÁO LỖI TỔNG QUÁT ===== -->
    <c:if test="${not empty formError['_global']}">
        <div class="alert alert-error" role="alert">
            <span class="alert-icon">&#9888;</span>
            <span><c:out value="${formError['_global']}"/></span>
        </div>
    </c:if>
    <c:if test="${not empty formError['email'] and formError['email'] == 'EMAIL_TRUNG'}">
        <div class="alert alert-error" role="alert" id="alert-email-trung">
            <span class="alert-icon">&#9888;</span>
            <span>
                Địa chỉ email <strong><c:out value="${nguoiDung.email}"/></strong>
                đã được sử dụng bởi tài khoản khác trong hệ thống.
                Vui lòng nhập địa chỉ email khác.
            </span>
        </div>
    </c:if>

    <!-- ===== FORM SỬA TÀI KHOẢN ===== -->
    <div class="form-card">
        <div class="form-card-header">
            <h2>Thông tin tài khoản</h2>
            <p>
                Người dùng:
                <strong><c:out value="${nguoiDung.email}"/></strong>
            </p>
        </div>

        <form id="form-sua-tai-khoan"
              method="POST"
              action="${pageContext.request.contextPath}/nguoi-dung/sua"
              novalidate>

            <!-- Hidden: ID người dùng -->
            <input type="hidden" name="id" value="${nguoiDung.id}">

            <div class="form-card-body">

                <!-- Thông tin cá nhân -->
                <p class="form-section-title">Thông tin cá nhân</p>

                <div class="form-row">
                    <!-- Họ tên -->
                    <div class="form-group">
                        <label for="hoTen">
                            Họ và tên <span class="required">*</span>
                        </label>
                        <input type="text"
                               id="hoTen"
                               name="hoTen"
                               required
                               maxlength="150"
                               placeholder="Nguyễn Văn A"
                               class="<c:if test='${not empty formError["hoTen"]}'>input-error</c:if>"
                               value="<c:out value='${nguoiDung.hoTen}'/>">
                        <span class="form-error-msg <c:if test='${not empty formError["hoTen"]}'>visible</c:if>"
                              id="hoTen-error">
                            <c:out value="${formError['hoTen']}"/>
                        </span>
                    </div>

                    <!-- Email -->
                    <div class="form-group">
                        <label for="email">
                            Email công ty <span class="required">*</span>
                        </label>
                        <input type="email"
                               id="email"
                               name="email"
                               required
                               maxlength="200"
                               class="<c:if test='${not empty formError["email"]}'>input-error</c:if>"
                               value="<c:out value='${nguoiDung.email}'/>">
                        <span class="form-error-msg <c:if test='${not empty formError["email"]}'>visible</c:if>"
                              id="email-error">
                            <c:choose>
                                <c:when test="${formError['email'] == 'EMAIL_TRUNG'}">
                                    Email này đã được sử dụng. Vui lòng nhập email khác.
                                </c:when>
                                <c:otherwise>
                                    <c:out value="${formError['email']}"/>
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                </div>

                <!-- Nhóm kinh doanh -->
                <div class="form-group">
                    <label for="nhomId">Nhóm kinh doanh</label>
                    <select id="nhomId" name="nhomId">
                        <option value="">-- Chưa gán nhóm --</option>
                        <c:forEach var="nhom" items="${dsNhom}">
                            <option value="${nhom.id}"
                                <c:if test="${nhom.id == nguoiDung.nhomId}">selected</c:if>>
                                <c:out value="${nhom.tenNhom}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <!-- Vai trò -->
                <div class="form-group">
                    <label>
                        Vai trò <span class="required">*</span>
                    </label>
                    <div class="checkbox-group" id="checkbox-vai-tro">
                        <c:choose>
                            <c:when test="${not empty dsVaiTro}">
                                <c:forEach var="vt" items="${dsVaiTro}">
                                    <label class="checkbox-item">
                                        <input type="checkbox"
                                               name="vaiTroIds"
                                               value="${vt.id}"
                                               <c:if test="${not empty nguoiDung.dsVaiTroIds and nguoiDung.dsVaiTroIds.contains(vt.id)}">checked</c:if>>
                                        <span><c:out value="${vt.tenVaiTro}"/></span>
                                    </label>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <p class="form-help">&#9888; Chưa có dữ liệu vai trò.</p>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <span class="form-error-msg <c:if test='${not empty formError["vaiTro"]}'>visible</c:if>"
                          id="vaiTro-error">
                        <c:out value="${formError['vaiTro']}"/>
                    </span>
                    <span class="form-help">Có thể chọn nhiều vai trò cùng lúc.</span>
                </div>

                <!-- Trạng thái -->
                <div class="form-group">
                    <label for="trangThai">Trạng thái tài khoản</label>
                    <select id="trangThai" name="trangThai">
                        <option value="HOAT_DONG"
                            <c:if test="${nguoiDung.trangThai == 'HOAT_DONG'}">selected</c:if>>
                            Hoạt động
                        </option>
                        <option value="CHO_KICH_HOAT"
                            <c:if test="${nguoiDung.trangThai == 'CHO_KICH_HOAT'}">selected</c:if>>
                            Chờ kích hoạt
                        </option>
                        <option value="KHOA"
                            <c:if test="${nguoiDung.trangThai == 'KHOA'}">selected</c:if>>
                            Bị khoá
                        </option>
                    </select>
                    <span class="form-help">
                        Khoá tài khoản tại đây chỉ vô hiệu hoá đăng nhập.
                        Để bàn giao dữ liệu, dùng chức năng khoá chuyên biệt (S1-10).
                    </span>
                </div>

            </div><%-- end form-card-body --%>

            <div class="form-card-footer">
                <a href="${pageContext.request.contextPath}/nguoi-dung"
                   class="btn btn-secondary">
                    Hủy
                </a>
                <button type="submit" id="btn-luu-sua-tai-khoan" class="btn btn-primary">
                    &#10003; Lưu thay đổi
                </button>
            </div>

        </form>
    </div><%-- end form-card --%>

</div><%-- end nguoi-dung-container --%>

<script src="${pageContext.request.contextPath}/assets/js/nguoi-dung/nguoi-dung.js"></script>
</body>
</html>
