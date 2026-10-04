<%--
    Danh sách Người Dùng - S1-08 FE
    Story: Quản trị hệ thống tạo, sửa và tìm kiếm tài khoản người dùng
    AC:
     - Tìm theo tên, email, nhóm; lọc theo vai trò và trạng thái
     - Danh sách phân trang, mặc định 20 dòng
    URL: /nguoi-dung
    Servlet: NguoiDungServlet (BE - dependency còn thiếu)
    Attributes truyền từ Servlet:
     - dsNguoiDung      : List<NguoiDung>
     - dsVaiTro         : List<VaiTro>
     - dsNhom           : List<NhomKinhDoanh>
     - tongSoBanGhi     : Integer
     - trangHienTai     : Integer
     - soBanGhiMoiTrang : Integer (mặc định 20)
     - tuKhoaTim        : String
     - filterVaiTro     : String
     - filterTrangThai  : String
     - filterNhom       : String
     - thongBaoThanhCong: String
     - thongBaoLoi      : String
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Tài khoản Người dùng - CRM</title>
    <meta name="description" content="Quản trị tài khoản, vai trò và phân quyền cho nhân viên kinh doanh trong hệ thống CRM.">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/nguoi-dung/nguoi-dung.css">
</head>
<body class="crm-body">

    <!-- Thanh điều hướng và Sidebar chuẩn hệ thống S1-06 -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content" id="crm-main-content">
        <div class="nguoi-dung-container">

    <!-- ===== HEADER ===== -->
    <div class="page-header">
        <h1>Quản lý Tài khoản Người dùng</h1>
        <div style="display: flex; gap: 10px; flex-wrap: wrap;">
            <a id="btn-co-cau-to-chuc"
               href="${pageContext.request.contextPath}/co-cau-to-chuc"
               class="btn btn-secondary" style="display: inline-flex; align-items: center; gap: 6px;">
                Cơ cấu tổ chức
            </a>
            <a id="btn-nhat-ky-thay-doi"
               href="${pageContext.request.contextPath}/nhat-ky-thay-doi"
               class="btn btn-secondary" style="display: inline-flex; align-items: center; gap: 6px;">
                Nhật ký dữ liệu nhạy cảm
            </a>
            <a id="btn-import-excel"
               href="${pageContext.request.contextPath}/nguoi-dung/import"
               class="btn btn-secondary" style="display: inline-flex; align-items: center; gap: 6px;">
                Nhập từ Excel
            </a>
            <a id="btn-tao-tai-khoan"
               href="${pageContext.request.contextPath}/nguoi-dung/tao"
               class="btn btn-primary">
                + Tạo tài khoản mới
            </a>
        </div>
    </div>

    <!-- ===== THÔNG BÁO ===== -->
    <c:if test="${not empty thongBaoThanhCong}">
        <div class="alert alert-success" role="alert">
            <span class="alert-icon">&#10003;</span>
            <span><c:out value="${thongBaoThanhCong}"/></span>
        </div>
    </c:if>
    <c:if test="${not empty thongBaoLoi}">
        <div class="alert alert-error" role="alert">
            <span class="alert-icon">&#9888;</span>
            <span><c:out value="${thongBaoLoi}"/></span>
        </div>
    </c:if>

    <!-- ===== BỘ LỌC / TÌM KIẾM ===== -->
    <div class="filter-bar">
        <form id="filter-form"
              method="GET"
              action="${pageContext.request.contextPath}/nguoi-dung">

            <!-- Tìm theo tên hoặc email -->
            <div class="filter-group search-group">
                <label for="tuKhoaTim">Tìm kiếm</label>
                <input type="text"
                       id="tuKhoaTim"
                       name="tuKhoaTim"
                       placeholder="Nhập tên hoặc email..."
                       value="<c:out value='${tuKhoaTim}'/>"
                       maxlength="200"
                       autocomplete="off">
            </div>

            <!-- Lọc theo nhóm kinh doanh -->
            <div class="filter-group">
                <label for="filterNhom">Nhóm kinh doanh</label>
                <select id="filterNhom" name="filterNhom">
                    <option value="">-- Tất cả nhóm --</option>
                    <c:forEach var="nhom" items="${dsNhom}">
                        <option value="${nhom.id}"
                            <c:if test="${nhom.id == filterNhom}">selected</c:if>>
                            <c:out value="${nhom.tenNhom}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>

            <!-- Lọc theo vai trò -->
            <div class="filter-group">
                <label for="filterVaiTro">Vai trò</label>
                <select id="filterVaiTro" name="filterVaiTro">
                    <option value="">-- Tất cả vai trò --</option>
                    <c:forEach var="vt" items="${dsVaiTro}">
                        <option value="${vt.id}"
                            <c:if test="${vt.id == filterVaiTro}">selected</c:if>>
                            <c:out value="${vt.tenVaiTro}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>

            <!-- Lọc theo trạng thái -->
            <div class="filter-group">
                <label for="filterTrangThai">Trạng thái</label>
                <select id="filterTrangThai" name="filterTrangThai">
                    <option value="">-- Tất cả --</option>
                    <option value="HOAT_DONG"
                        <c:if test="${filterTrangThai == 'HOAT_DONG'}">selected</c:if>>Hoạt động</option>
                    <option value="KHOA"
                        <c:if test="${filterTrangThai == 'KHOA'}">selected</c:if>>Bị khoá</option>
                    <option value="CHO_KICH_HOAT"
                        <c:if test="${filterTrangThai == 'CHO_KICH_HOAT'}">selected</c:if>>Chờ kích hoạt</option>
                </select>
            </div>

            <!-- Nút hành động -->
            <div class="filter-actions">
                <button type="submit" class="btn btn-primary btn-sm" id="btn-tim-kiem">
                    &#128269; Tìm kiếm
                </button>
                <button type="button" class="btn btn-secondary btn-sm" id="btn-clear-filter">
                    &#10005; Xóa lọc
                </button>
            </div>
        </form>
    </div>

    <!-- ===== BẢNG DANH SÁCH ===== -->
    <div class="table-wrapper">

        <div class="table-header-bar">
            <div class="result-count">
                <c:choose>
                    <c:when test="${tongSoBanGhi != null}">
                        Tổng: <strong>${tongSoBanGhi}</strong> tài khoản
                        <c:if test="${not empty tuKhoaTim or not empty filterVaiTro or not empty filterTrangThai or not empty filterNhom}">
                            &nbsp;(đang lọc)
                        </c:if>
                    </c:when>
                    <c:otherwise>
                        &mdash;
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <c:choose>
            <c:when test="${empty dsNguoiDung}">
                <div class="empty-state">
                    <div class="empty-icon">&#128100;</div>
                    <p><strong>Không có tài khoản nào</strong></p>
                    <c:choose>
                        <c:when test="${not empty tuKhoaTim or not empty filterVaiTro or not empty filterTrangThai or not empty filterNhom}">
                            <p>Không tìm thấy kết quả phù hợp với bộ lọc hiện tại.</p>
                        </c:when>
                        <c:otherwise>
                            <p>Chưa có tài khoản nào. Hãy tạo tài khoản đầu tiên.</p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </c:when>
            <c:otherwise>
                <table class="nguoi-dung-table" aria-label="Danh sách tài khoản người dùng">
                    <thead>
                        <tr>
                            <th>Người dùng</th>
                            <th>Nhóm kinh doanh</th>
                            <th>Vai trò</th>
                            <th>Trạng thái</th>
                            <th>Ngày tạo</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="nd" items="${dsNguoiDung}">
                            <tr>
                                <!-- Tên + Email -->
                                <td>
                                    <div class="user-info-cell">
                                        <%-- Avatar: lấy chữ đầu của họ tên --%>
                                        <div class="user-avatar" aria-hidden="true">
                                            ${fn:substring(nd.hoTen, 0, 1)}
                                        </div>
                                        <div>
                                            <div class="user-full-name">
                                                <c:out value="${nd.hoTen}"/>
                                            </div>
                                            <div class="user-email-small">
                                                <c:out value="${nd.email}"/>
                                            </div>
                                        </div>
                                    </div>
                                </td>

                                <!-- Nhóm kinh doanh -->
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty nd.tenNhom}">
                                            <c:out value="${nd.tenNhom}"/>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="color:#cbd5e1">—</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <!-- Vai trò -->
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty nd.dsVaiTro}">
                                            <c:forEach var="vt" items="${nd.dsVaiTro}">
                                                <span class="role-badge <c:if test='${vt.maVaiTro == "QUAN_TRI"}'>admin</c:if>">
                                                    <c:out value="${vt.tenVaiTro}"/>
                                                </span>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="color:#cbd5e1">—</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <!-- Trạng thái -->
                                <td>
                                    <c:choose>
                                        <c:when test="${nd.trangThai == 'HOAT_DONG'}">
                                            <span class="badge badge-active">Hoạt động</span>
                                        </c:when>
                                        <c:when test="${nd.trangThai == 'KHOA'}">
                                            <span class="badge badge-inactive">Bị khoá</span>
                                        </c:when>
                                        <c:when test="${nd.trangThai == 'CHO_KICH_HOAT'}">
                                            <span class="badge badge-pending">Chờ kích hoạt</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge"><c:out value="${nd.trangThai}"/></span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <!-- Ngày tạo -->
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty nd.ngayTao}">
                                            <c:out value="${nd.ngayTao}"/>
                                        </c:when>
                                        <c:otherwise><span style="color:#cbd5e1">—</span></c:otherwise>
                                    </c:choose>
                                </td>

                                <!-- Thao tác -->
                                <td>
                                    <a id="btn-sua-${nd.id}"
                                       href="${pageContext.request.contextPath}/nguoi-dung/sua?id=${nd.id}"
                                       class="btn btn-secondary btn-sm">
                                        &#9998; Sửa
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>

                <!-- ===== PHÂN TRANG ===== -->
                <c:if test="${tongSoBanGhi > soBanGhiMoiTrang}">
                    <%-- Tính toán số trang --%>
                    <c:set var="tongSoTrang"
                           value="${(tongSoBanGhi + soBanGhiMoiTrang - 1) / soBanGhiMoiTrang}"/>
                    <c:set var="tongSoTrang" value="${fn:substringBefore(tongSoTrang, '.')}"/>

                    <div class="pagination-bar">
                        <div class="pagination-info">
                            Trang <strong>${trangHienTai}</strong> / <strong>${tongSoTrang}</strong>
                            &nbsp;·&nbsp;
                            ${soBanGhiMoiTrang} dòng mỗi trang
                        </div>

                        <div class="pagination-links" role="navigation" aria-label="Phân trang">

                            <%-- Trang trước --%>
                            <c:choose>
                                <c:when test="${trangHienTai > 1}">
                                    <a id="btn-trang-truoc"
                                       href="${pageContext.request.contextPath}/nguoi-dung?trang=${trangHienTai - 1}&tuKhoaTim=${tuKhoaTim}&filterVaiTro=${filterVaiTro}&filterTrangThai=${filterTrangThai}&filterNhom=${filterNhom}"
                                       aria-label="Trang trước">&#8592;</a>
                                </c:when>
                                <c:otherwise>
                                    <span class="disabled" aria-disabled="true">&#8592;</span>
                                </c:otherwise>
                            </c:choose>

                            <%-- Các trang --%>
                            <c:forEach begin="1" end="${tongSoTrang}" var="p">
                                <c:choose>
                                    <c:when test="${p == trangHienTai}">
                                        <span class="current-page" aria-current="page">${p}</span>
                                    </c:when>
                                    <c:otherwise>
                                        <a id="btn-trang-${p}"
                                           href="${pageContext.request.contextPath}/nguoi-dung?trang=${p}&tuKhoaTim=${tuKhoaTim}&filterVaiTro=${filterVaiTro}&filterTrangThai=${filterTrangThai}&filterNhom=${filterNhom}">
                                            ${p}
                                        </a>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>

                            <%-- Trang sau --%>
                            <c:choose>
                                <c:when test="${trangHienTai < tongSoTrang}">
                                    <a id="btn-trang-sau"
                                       href="${pageContext.request.contextPath}/nguoi-dung?trang=${trangHienTai + 1}&tuKhoaTim=${tuKhoaTim}&filterVaiTro=${filterVaiTro}&filterTrangThai=${filterTrangThai}&filterNhom=${filterNhom}"
                                       aria-label="Trang sau">&#8594;</a>
                                </c:when>
                                <c:otherwise>
                                    <span class="disabled" aria-disabled="true">&#8594;</span>
                                </c:otherwise>
                            </c:choose>

                        </div>
                    </div>
                </c:if>

            </c:otherwise>
        </c:choose>

    </div><%-- end table-wrapper --%>

    </div><%-- end nguoi-dung-container --%>
    </main>

<script src="${pageContext.request.contextPath}/assets/js/navigation.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/nguoi-dung/nguoi-dung.js"></script>
</body>
</html>
