<%--
    Tạo Tài khoản Người Dùng - S1-08 FE
    AC:
     - Tạo tài khoản gửi email kích hoạt kèm mật khẩu tạm
     - Email trùng bị từ chối kèm thông báo cụ thể
    URL: /nguoi-dung/tao  (GET hiển thị form, POST xử lý)
    Servlet: NguoiDungServlet (BE - dependency còn thiếu)
    Attributes truyền từ Servlet:
     - dsVaiTro  : List<VaiTro>
     - dsNhom    : List<NhomKinhDoanh>
     - formError : Map<String,String>  (key: tên field, value: thông báo lỗi)
     - oldInput  : NguoiDung (dữ liệu cũ khi validate fail để giữ lại)
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tạo Tài khoản Người dùng - CRM</title>
    <meta name="description" content="Tạo tài khoản mới, gán vai trò và gửi email kích hoạt cho nhân viên kinh doanh.">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/nguoi-dung/nguoi-dung.css">
</head>
<body class="crm-body">

    <!-- Thanh điều hướng và Sidebar chuẩn hệ thống S1-06 -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content" id="crm-main-content">
        <div class="nguoi-dung-container">

    <!-- Breadcrumb điều hướng chuẩn -->
    <nav class="crm-breadcrumb" style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
        <div style="font-size: 13.5px; color: var(--slate-500, #64748b);">
            <a href="${pageContext.request.contextPath}/dieu-huong" style="color: var(--primary, #2563eb); text-decoration: none;">Trang chủ</a>
            <span style="margin: 0 6px;">/</span>
            <a href="${pageContext.request.contextPath}/nguoi-dung" style="color: var(--primary, #2563eb); text-decoration: none;">Người dùng & Phân quyền</a>
            <span style="margin: 0 6px;">/</span>
            <span style="color: var(--slate-700, #334155); font-weight: 500;">Tạo tài khoản mới</span>
        </div>
        <a href="${pageContext.request.contextPath}/nguoi-dung" class="btn btn-outline btn-sm">&larr; Quay lại danh sách</a>
    </nav>

    <!-- ===== HEADER ===== -->
    <div class="page-header">
        <h1>Tạo Tài khoản Mới</h1>
        <a href="${pageContext.request.contextPath}/nguoi-dung"
           class="btn btn-secondary">
            &larr; Quay lại danh sách
        </a>
    </div>

    <!-- ===== THÔNG BÁO LỖI TỔNG QUÁT (vd: email trùng) ===== -->
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
                Địa chỉ email <strong><c:out value="${oldInput.email}"/></strong>
                đã được sử dụng bởi một tài khoản khác trong hệ thống.
                Vui lòng nhập địa chỉ email khác.
            </span>
        </div>
    </c:if>

    <!-- ===== FORM TẠO TÀI KHOẢN ===== -->
    <div class="form-card">
        <div class="form-card-header">
            <h2>Thông tin tài khoản</h2>
            <p>Hệ thống sẽ tự động tạo mật khẩu tạm và gửi email kích hoạt đến người dùng.</p>
        </div>

        <form id="form-tao-tai-khoan"
              method="POST"
              action="${pageContext.request.contextPath}/nguoi-dung/tao"
              novalidate>

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
                               value="<c:out value='${oldInput.hoTen}'/>">
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
                               placeholder="nguyenvana@congty.vn"
                               class="<c:if test='${not empty formError["email"]}'>input-error</c:if>"
                               value="<c:out value='${oldInput.email}'/>">
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
                        <span class="form-help">Email sẽ nhận mật khẩu tạm và link kích hoạt.</span>
                    </div>
                </div>

                <!-- Nhóm kinh doanh -->
                <div class="form-group">
                    <label for="nhomId">Nhóm kinh doanh</label>
                    <select id="nhomId" name="nhomId">
                        <option value="">-- Chưa gán nhóm --</option>
                        <c:forEach var="nhom" items="${dsNhom}">
                            <option value="${nhom.id}"
                                <c:if test="${nhom.id == oldInput.nhomId}">selected</c:if>>
                                <c:out value="${nhom.tenNhom}"/>
                            </option>
                        </c:forEach>
                    </select>
                    <span class="form-error-msg <c:if test='${not empty formError["nhomId"]}'>visible</c:if>"
                          id="nhomId-error">
                        <c:out value="${formError['nhomId']}"/>
                    </span>
                    <span class="form-help">
                        Một nhân viên thuộc đúng một nhóm kinh doanh tại một thời điểm.
                    </span>
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
                                               data-code="${vt.maVaiTro}"
                                               <c:if test="${not empty oldInput.dsVaiTroIds and oldInput.dsVaiTroIds.contains(vt.id)}">checked</c:if>>
                                        <span><c:out value="${vt.tenVaiTro}"/></span>
                                    </label>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <p class="form-help">
                                    &#9888; Chưa có dữ liệu vai trò.
                                    Vui lòng liên hệ quản trị viên để cấu hình vai trò trước.
                                </p>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <span class="form-error-msg <c:if test='${not empty formError["vaiTro"]}'>visible</c:if>"
                          id="vaiTro-error">
                        <c:out value="${formError['vaiTro']}"/>
                    </span>
                    <span class="form-help">Có thể chọn nhiều vai trò cùng lúc.</span>
                </div>

                <!-- Thông báo về mật khẩu tạm -->
                <div class="alert alert-warning" style="margin-top:4px">
                    <span class="alert-icon">&#128274;</span>
                    <span>
                        Sau khi tạo, hệ thống sẽ tự động sinh mật khẩu ngẫu nhiên và gửi email kích hoạt
                        kèm mật khẩu tạm đến địa chỉ email trên. Người dùng cần đổi mật khẩu khi đăng nhập lần đầu.
                    </span>
                </div>

            </div><%-- end form-card-body --%>

            <div class="form-card-footer">
                <a href="${pageContext.request.contextPath}/nguoi-dung"
                   class="btn btn-secondary">
                    Hủy
                </a>
                <button type="submit" id="btn-luu-tai-khoan" class="btn btn-primary">
                    &#10003; Tạo tài khoản &amp; Gửi email
                </button>
            </div>

        </form>
    </div><%-- end form-card --%>

    </div><%-- end nguoi-dung-container --%>
    </main>

<script src="${pageContext.request.contextPath}/assets/js/navigation.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/nguoi-dung/nguoi-dung.js"></script>
</body>
</html>
