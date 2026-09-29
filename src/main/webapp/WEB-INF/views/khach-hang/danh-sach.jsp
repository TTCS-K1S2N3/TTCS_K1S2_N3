<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Danh sách khách hàng & Ghi chú cuộc gặp - CRM</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <script>
        window.CONTEXT_PATH = "${pageContext.request.contextPath}";
    </script>
</head>
<body>

<header class="navbar">
    <div class="nav-brand">
        🏢 <span>CRM Bán Hàng</span>
    </div>

    <div class="nav-user-info">
        <div class="session-indicator" title="Trạng thái phiên làm việc">
            <span class="session-dot"></span>
            <span id="session-status-text">Phiên đang hoạt động</span>
        </div>

        <c:if test="${not empty nguoiDung}">
            <span class="user-badge">
                👤 <c:out value="${nguoiDung.hoTen}" /> (<c:out value="${nguoiDung.email}" />)
            </span>
        </c:if>

        <%-- Nút đăng xuất an toàn (AC2) --%>
        <form action="${pageContext.request.contextPath}/dang-xuat" method="post" style="display:inline;" id="form-logout">
            <button type="submit" class="btn-logout" id="btn-logout">Đăng xuất</button>
        </form>
    </div>
</header>

<main class="container">
    <%-- Khu vực soạn thảo ghi chú cuộc gặp (Story S1-02: ngồi ở quán cà phê không bị mất ghi chú) --%>
    <div class="card">
        <div class="card-title">
            <span>📝 Ghi chú cuộc gặp khách hàng (Bản nháp tự động gia hạn phiên)</span>
            <span class="badge-info">Tự động lưu & duy trì phiên</span>
        </div>

        <p style="color: var(--text-muted); font-size: 14px; margin-bottom: 12px;">
            Khi bạn đang ngồi tại quán cà phê soạn thảo ghi chú, hệ thống sẽ tự động gửi tín hiệu gia hạn phiên làm việc (AC1)
            và lưu bản nháp dự phòng để tránh mất nội dung khi kết nối chập chờn.
        </p>

        <textarea id="ghi-chu-cuoc-gap" class="note-editor"
                  placeholder="Nhập nội dung trao đổi cuộc gặp với khách hàng tại đây (ví dụ: nhu cầu mua hàng, thời gian bàn giao dự kiến, phản hồi ngân sách)..."></textarea>

        <div class="note-footer">
            <div class="sync-status" id="session-sync-status">
                Phiên làm việc sẵn sàng
            </div>
            <div>
                <small>Thời gian phiên tối đa: 30 phút không hoạt động</small>
            </div>
        </div>
    </div>

    <%-- Danh sách khách hàng mẫu minh họa không gian làm việc --%>
    <div class="card">
        <div class="card-title">
            <span>👥 Khách hàng phụ trách</span>
        </div>
        <table style="width: 100%; border-collapse: collapse; font-size: 14px; text-align: left;">
            <thead>
                <tr style="border-bottom: 2px solid var(--border); color: var(--text-muted);">
                    <th style="padding: 10px;">Mã KH</th>
                    <th style="padding: 10px;">Tên khách hàng / Doanh nghiệp</th>
                    <th style="padding: 10px;">Số điện thoại</th>
                    <th style="padding: 10px;">Giai đoạn</th>
                </tr>
            </thead>
            <tbody>
                <tr style="border-bottom: 1px solid var(--border);">
                    <td style="padding: 12px 10px;">KH001</td>
                    <td style="padding: 12px 10px; font-weight: 600;">Công ty TNHH Công nghệ An Phát</td>
                    <td style="padding: 12px 10px;">0901 234 567</td>
                    <td style="padding: 12px 10px;"><span class="badge-info">Đang đàm phán</span></td>
                </tr>
                <tr style="border-bottom: 1px solid var(--border);">
                    <td style="padding: 12px 10px;">KH002</td>
                    <td style="padding: 12px 10px; font-weight: 600;">Tập đoàn Dược phẩm Đại Nam</td>
                    <td style="padding: 12px 10px;">0988 765 432</td>
                    <td style="padding: 12px 10px;"><span class="badge-info">Đã gửi báo giá</span></td>
                </tr>
            </tbody>
        </table>
    </div>
</main>

<script src="${pageContext.request.contextPath}/assets/js/session-keep-alive.js"></script>
</body>
</html>
