<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="vn.nhom10.crm.dto.NguoiDungDTO" %>
<%@ page import="vn.nhom10.crm.dto.BanGhiNghiepVuDTO" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Từ Chối Truy Cập - Bản Ghi Ngoài Phạm Vi Dữ Liệu</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/phan-quyen/data-scope.css">
</head>
<body>

<%
    NguoiDungDTO currentUser = (NguoiDungDTO) request.getAttribute("currentUser");
    String thongBaoLoi = (String) request.getAttribute("thongBaoLoi");
    BanGhiNghiepVuDTO banGhi = (BanGhiNghiepVuDTO) request.getAttribute("banGhi");
%>

<div class="error-screen-wrapper">
    <div class="error-card">
        <div class="error-card-header">
            <div class="error-shield-icon">
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                    <line x1="12" y1="8" x2="12" y2="12"/>
                    <line x1="12" y1="16" x2="12.01" y2="16"/>
                </svg>
            </div>
            <h1 class="error-card-title">Từ Chối Quyền Truy Cập Dữ Liệu</h1>
            <div class="error-card-subtitle">Quy tắc phân quyền phạm vi sở hữu (Data Scope Policy)</div>
        </div>

        <div class="error-card-body">
            <!-- THÔNG BÁO LỖI TIẾNG VIỆT RÕ RÀNG (AC3) -->
            <div class="error-reason-box">
                <strong>Chi tiết từ chối:</strong><br>
                <%= thongBaoLoi != null ? thongBaoLoi : "Bạn không có quyền xem bản ghi này vì nó nằm ngoài phạm vi dữ liệu được chỉ định của tài khoản." %>
            </div>

            <div class="error-rule-explanation">
                <strong>Chính sách phân quyền hệ thống:</strong>
                <ul style="margin: 8px 0 0 20px; line-height: 1.6;">
                    <li><strong>Nhân viên kinh doanh (Sales Rep):</strong> Chỉ được xem dữ liệu cá nhân của chính mình. Không thể xem khách hàng của nhân viên khác.</li>
                    <li><strong>Trưởng nhóm (Team Lead):</strong> Chỉ được xem dữ liệu của các thành viên trực thuộc nhóm mình quản lý.</li>
                    <li><strong>Giám đốc (Director):</strong> Có quyền xem toàn bộ dữ liệu trên toàn hệ thống.</li>
                </ul>
            </div>

            <div class="error-actions">
                <a href="${pageContext.request.contextPath}/phan-quyen-du-lieu" class="btn-primary-action">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
                    <span>Quay lại danh sách dữ liệu của bạn</span>
                </a>

                <a href="${pageContext.request.contextPath}/khach-hang" class="btn-secondary-action">
                    <span>Về trang Khách hàng</span>
                </a>
            </div>
        </div>
    </div>
</div>

</body>
</html>
