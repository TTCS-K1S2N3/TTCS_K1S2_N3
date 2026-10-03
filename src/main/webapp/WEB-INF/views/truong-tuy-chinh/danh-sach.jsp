<%--
    Quản lý Trường Tuỳ Chỉnh - S2-08 FE
    Story: Quản trị hệ thống khai báo trường tuỳ chỉnh cho Khách hàng và Cơ hội
    AC:
     - Thêm trường kiểu văn bản, số, ngày, danh sách chọn
     - Đặt được trường là bắt buộc hay không
     - Trường tuỳ chỉnh xuất hiện trong biểu mẫu, bộ lọc và bản xuất Excel
    URL: /truong-tuy-chinh
    Servlet: TruongTuyChinhServlet (BE - dependency còn thiếu)
    Attributes truyền từ Servlet:
     - dsTruongKhachHang : List<TruongTuyChinhDTO> (cho đối tượng KHACH_HANG)
     - dsTruongCoHoi     : List<TruongTuyChinhDTO> (cho đối tượng CO_HOI)
     - tabHienTai        : String (KHACH_HANG | CO_HOI, mặc định KHACH_HANG)
     - thongBaoThanhCong : String
     - thongBaoLoi       : String
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Trường Tuỳ Chỉnh - CRM</title>
    <meta name="description" content="Khai báo và quản lý trường tuỳ chỉnh cho Khách hàng và Cơ hội trong hệ thống CRM.">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/truong-tuy-chinh/truong-tuy-chinh.css">
</head>
<body>

<div class="ttc-container">

    <!-- ===== HEADER ===== -->
    <div class="page-header">
        <h1>&#9881; Trường Tuỳ Chỉnh</h1>
        <a id="btn-them-truong"
           href="${pageContext.request.contextPath}/truong-tuy-chinh/tao?doiTuong=${not empty tabHienTai ? tabHienTai : 'KHACH_HANG'}"
           class="btn btn-primary"
           data-base-url="${pageContext.request.contextPath}/truong-tuy-chinh/tao">
            &#43; Thêm trường mới
        </a>
    </div>

    <!-- ===== THÔNG BÁO ===== -->
    <c:if test="${not empty thongBaoThanhCong}">
        <div class="alert alert-success" role="alert">
            &#10003;&nbsp;<c:out value="${thongBaoThanhCong}"/>
        </div>
    </c:if>
    <c:if test="${not empty thongBaoLoi}">
        <div class="alert alert-error" role="alert">
            &#9888;&nbsp;<c:out value="${thongBaoLoi}"/>
        </div>
    </c:if>

    <!-- ===== TABS ĐỐI TƯỢNG ===== -->
    <div class="tab-bar" role="tablist">
        <button class="tab-btn ${empty tabHienTai or tabHienTai == 'KHACH_HANG' ? 'active' : ''}"
                data-doi-tuong="KHACH_HANG"
                role="tab"
                id="tab-khach-hang"
                aria-selected="${empty tabHienTai or tabHienTai == 'KHACH_HANG'}">
            &#128100; Khách hàng
        </button>
        <button class="tab-btn ${tabHienTai == 'CO_HOI' ? 'active' : ''}"
                data-doi-tuong="CO_HOI"
                role="tab"
                id="tab-co-hoi"
                aria-selected="${tabHienTai == 'CO_HOI'}">
            &#127919; Cơ hội
        </button>
    </div>

    <!-- ===== BẢNG: KHÁCH HÀNG ===== -->
    <div class="table-wrapper" data-doi-tuong="KHACH_HANG">
        <div class="table-header-bar">
            <h2>Trường tuỳ chỉnh — Khách hàng</h2>
            <span class="table-count">${fn:length(dsTruongKhachHang)} trường</span>
        </div>

        <c:choose>
            <c:when test="${empty dsTruongKhachHang}">
                <div class="empty-state">
                    <div class="empty-icon">&#128196;</div>
                    <p><strong>Chưa có trường tuỳ chỉnh nào</strong></p>
                    <p>Nhấn "Thêm trường mới" để khai báo trường đầu tiên cho Khách hàng.</p>
                </div>
            </c:when>
            <c:otherwise>
                <table class="ttc-table" aria-label="Danh sách trường tuỳ chỉnh Khách hàng">
                    <thead>
                        <tr>
                            <th style="width:40px">#</th>
                            <th>Nhãn hiển thị / Tên kỹ thuật</th>
                            <th>Kiểu dữ liệu</th>
                            <th>Bắt buộc</th>
                            <th>Trong bộ lọc</th>
                            <th>Trong Excel</th>
                            <th>Trạng thái</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="t" items="${dsTruongKhachHang}" varStatus="loop">
                            <tr>
                                <td>
                                    <span class="thu-tu-num">${t.thuTu}</span>
                                </td>
                                <td>
                                    <div class="field-name-cell">
                                        <div class="field-label"><c:out value="${t.nhanHien}"/></div>
                                        <div class="field-key"><c:out value="${t.tenTruong}"/></div>
                                    </div>
                                </td>
                                <td>
                                    <span class="kieu-badge kieu-${t.kieuDuLieu}">
                                        <c:choose>
                                            <c:when test="${t.kieuDuLieu == 'VAN_BAN'}">&#128214; Văn bản</c:when>
                                            <c:when test="${t.kieuDuLieu == 'SO'}">&#128290; Số</c:when>
                                            <c:when test="${t.kieuDuLieu == 'NGAY'}">&#128197; Ngày</c:when>
                                            <c:when test="${t.kieuDuLieu == 'DANH_SACH_CHON'}">&#9660; Danh sách chọn</c:when>
                                            <c:otherwise><c:out value="${t.kieuDuLieu}"/></c:otherwise>
                                        </c:choose>
                                    </span>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${t.batBuoc}">
                                            <span class="bat-buoc-badge bat-buoc-yes">Bắt buộc</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="bat-buoc-badge bat-buoc-no">Không</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${t.hienThiBoDac}">&#10003;</c:when>
                                        <c:otherwise><span style="color:#e2e8f0">—</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${t.hienThiExcel}">&#10003;</c:when>
                                        <c:otherwise><span style="color:#e2e8f0">—</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${t.dangHoatDong}">
                                            <span class="trang-thai-badge trang-thai-on">Bật</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="trang-thai-badge trang-thai-off">Tắt</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <a id="btn-sua-kh-${t.id}"
                                       href="${pageContext.request.contextPath}/truong-tuy-chinh/sua?id=${t.id}"
                                       class="btn btn-secondary btn-sm">&#9998; Sửa</a>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- ===== BẢNG: CƠ HỘI ===== -->
    <div class="table-wrapper" data-doi-tuong="CO_HOI">
        <div class="table-header-bar">
            <h2>Trường tuỳ chỉnh — Cơ hội</h2>
            <span class="table-count">${fn:length(dsTruongCoHoi)} trường</span>
        </div>

        <c:choose>
            <c:when test="${empty dsTruongCoHoi}">
                <div class="empty-state">
                    <div class="empty-icon">&#128196;</div>
                    <p><strong>Chưa có trường tuỳ chỉnh nào</strong></p>
                    <p>Nhấn "Thêm trường mới" để khai báo trường đầu tiên cho Cơ hội.</p>
                </div>
            </c:when>
            <c:otherwise>
                <table class="ttc-table" aria-label="Danh sách trường tuỳ chỉnh Cơ hội">
                    <thead>
                        <tr>
                            <th style="width:40px">#</th>
                            <th>Nhãn hiển thị / Tên kỹ thuật</th>
                            <th>Kiểu dữ liệu</th>
                            <th>Bắt buộc</th>
                            <th>Trong bộ lọc</th>
                            <th>Trong Excel</th>
                            <th>Trạng thái</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="t" items="${dsTruongCoHoi}" varStatus="loop">
                            <tr>
                                <td><span class="thu-tu-num">${t.thuTu}</span></td>
                                <td>
                                    <div class="field-name-cell">
                                        <div class="field-label"><c:out value="${t.nhanHien}"/></div>
                                        <div class="field-key"><c:out value="${t.tenTruong}"/></div>
                                    </div>
                                </td>
                                <td>
                                    <span class="kieu-badge kieu-${t.kieuDuLieu}">
                                        <c:choose>
                                            <c:when test="${t.kieuDuLieu == 'VAN_BAN'}">&#128214; Văn bản</c:when>
                                            <c:when test="${t.kieuDuLieu == 'SO'}">&#128290; Số</c:when>
                                            <c:when test="${t.kieuDuLieu == 'NGAY'}">&#128197; Ngày</c:when>
                                            <c:when test="${t.kieuDuLieu == 'DANH_SACH_CHON'}">&#9660; Danh sách chọn</c:when>
                                            <c:otherwise><c:out value="${t.kieuDuLieu}"/></c:otherwise>
                                        </c:choose>
                                    </span>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${t.batBuoc}">
                                            <span class="bat-buoc-badge bat-buoc-yes">Bắt buộc</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="bat-buoc-badge bat-buoc-no">Không</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${t.hienThiBoDac}">&#10003;</c:when>
                                        <c:otherwise><span style="color:#e2e8f0">—</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${t.hienThiExcel}">&#10003;</c:when>
                                        <c:otherwise><span style="color:#e2e8f0">—</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${t.dangHoatDong}">
                                            <span class="trang-thai-badge trang-thai-on">Bật</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="trang-thai-badge trang-thai-off">Tắt</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <a id="btn-sua-co-${t.id}"
                                       href="${pageContext.request.contextPath}/truong-tuy-chinh/sua?id=${t.id}"
                                       class="btn btn-secondary btn-sm">&#9998; Sửa</a>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </div>

</div><%-- end ttc-container --%>

<script src="${pageContext.request.contextPath}/assets/js/truong-tuy-chinh/truong-tuy-chinh.js"></script>
</body>
</html>
