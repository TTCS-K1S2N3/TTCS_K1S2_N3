<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Danh mục khách hàng & Ghi chú cuộc gặp - Hệ thống CRM Bán Hàng">
    <title>Danh Mục Khách Hàng - CRM Bán Hàng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/khach-hang/khach-hang.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <script>
        window.CONTEXT_PATH = "${pageContext.request.contextPath}";
    </script>
</head>
<body>
    <!-- Top Navigation Bar -->
    <header class="navbar">
        <a href="${pageContext.request.contextPath}/khach-hang" class="nav-brand">
            <div class="nav-brand-logo">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                </svg>
            </div>
            <span>CRM Doanh Nghiệp</span>
        </a>

        <div class="nav-user-panel">
            <!-- Story S1-02: Thanh trạng thái phiên đăng nhập bảo mật và đếm ngược -->
            <div class="session-indicator" id="session-indicator-box" title="Trạng thái phiên đăng nhập bảo mật">
                <span class="session-dot active" id="session-status-dot"></span>
                <span id="session-status-text">Phiên hoạt động</span>
                <span class="session-countdown" id="session-countdown-timer" title="Thời gian phiên còn lại">30:00</span>
            </div>

            <div class="user-avatar" title="Tài khoản cá nhân">
                <c:out value="${not empty nguoiDung ? nguoiDung.tenVietTat : (not empty nguoiDungHienTai ? nguoiDungHienTai.tenVietTat : 'CRM')}" />
            </div>
            <div class="user-info">
                <span class="user-name">
                    <c:out value="${not empty nguoiDung ? nguoiDung.hoTen : (not empty nguoiDungHienTai ? nguoiDungHienTai.hoTen : 'Nhân viên kinh doanh')}" />
                </span>
                <span class="user-role">
                    <c:choose>
                        <c:when test="${not empty nguoiDung and not empty nguoiDung.tenNhomKinhDoanh}">
                            <c:out value="${nguoiDung.tenNhomKinhDoanh}" />
                        </c:when>
                        <c:when test="${not empty nguoiDungHienTai and not empty nguoiDungHienTai.tenNhomKinhDoanh}">
                            <c:out value="${nguoiDungHienTai.tenNhomKinhDoanh}" />
                        </c:when>
                        <c:otherwise>Khối Kinh Doanh</c:otherwise>
                    </c:choose>
                </span>
            </div>

            <!-- AC2: Đăng xuất an toàn bằng POST -->
            <form action="${pageContext.request.contextPath}/dang-xuat" method="post" style="display:inline; margin:0;" id="form-logout">
                <button type="submit" class="btn-logout" id="btn-logout" title="Đăng xuất và hủy phiên bảo mật trên server">
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                        <polyline points="16 17 21 12 16 7"></polyline>
                        <line x1="21" y1="12" x2="9" y2="12"></line>
                    </svg>
                    <span>Đăng xuất</span>
                </button>
            </form>
        </div>
    </header>

    <main class="page-container">
        <!-- Story S1-02: Khu vực soạn thảo ghi chú cuộc gặp (Ngồi ở quán cà phê không bị mất ghi chú) -->
        <div class="card" style="margin-bottom: 24px;">
            <div class="card-title">
                <div class="card-title-left">
                    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color: var(--primary);">
                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                        <polyline points="14 2 14 8 20 8"></polyline>
                        <line x1="16" y1="13" x2="8" y2="13"></line>
                        <line x1="16" y1="17" x2="8" y2="17"></line>
                        <polyline points="10 9 9 9 8 9"></polyline>
                    </svg>
                    <span>Ghi chú cuộc gặp khách hàng (Duy trì phiên & Tự động lưu)</span>
                </div>
                <span class="badge-info">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                        <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                    Auto Keep-Alive Active
                </span>
            </div>

            <p style="color: var(--slate-500); font-size: 13.5px; margin-bottom: 14px;">
                Khi bạn đang ngồi tại quán cà phê soạn thảo ghi chú, hệ thống sẽ tự động gửi tín hiệu gia hạn phiên làm việc (AC1)
                và liên tục lưu bản nháp dự phòng để tránh mất nội dung khi kết nối mạng chập chờn.
            </p>

            <!-- Banner cảnh báo phát hiện bản nháp chưa lưu -->
            <div class="draft-alert-banner" id="draft-alert-banner">
                <div>
                    <strong>💡 Phát hiện bản nháp trước đó:</strong>
                    <span>Bạn có một ghi chú chưa xóa được lưu lúc <span id="draft-saved-time-text">gần đây</span>.</span>
                </div>
                <div class="draft-alert-actions">
                    <button type="button" class="btn-draft-action btn-draft-restore" id="btn-restore-draft">Khôi phục ghi chú</button>
                    <button type="button" class="btn-draft-action btn-draft-discard" id="btn-discard-draft">Hủy bản nháp</button>
                </div>
            </div>

            <div class="editor-wrapper">
                <textarea id="ghi-chu-cuoc-gap" class="note-editor"
                          placeholder="Nhập nội dung trao đổi cuộc gặp với khách hàng tại đây (ví dụ: nhu cầu mua hàng, thời gian bàn giao dự kiến, phản hồi ngân sách, yêu cầu kỹ thuật)..."></textarea>
            </div>

            <div class="note-footer">
                <div class="note-stats-group">
                    <span class="sync-status active" id="session-sync-status">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path>
                        </svg>
                        Phiên làm việc sẵn sàng
                    </span>
                    <span class="note-stat-item" id="note-last-saved" style="color: var(--slate-400);"></span>
                    <span class="note-stat-item" id="note-word-count" style="color: var(--slate-600); font-weight: 600;">0 từ</span>
                    <span class="note-stat-item" id="note-char-count" style="color: var(--slate-400);">0 ký tự</span>
                </div>
                <div class="note-actions-group">
                    <button type="button" class="btn-secondary" id="btn-manual-save" title="Lưu nháp ngay vào bộ nhớ trình duyệt">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path>
                        </svg>
                        Lưu nháp
                    </button>
                    <button type="button" class="btn-secondary" id="btn-manual-sync" title="Gửi tín hiệu gia hạn phiên ngay">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <polyline points="23 4 23 10 17 10"></polyline>
                            <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"></path>
                        </svg>
                        Gia hạn phiên
                    </button>
                </div>
            </div>
        </div>

        <!-- Tiêu đề trang & Thanh công cụ danh sách khách hàng -->
        <div class="page-header">
            <div>
                <h1 class="page-title">Danh Mục Khách Hàng</h1>
                <p class="page-subtitle">Quản lý và chăm sóc danh mục khách hàng thuộc quyền phụ trách</p>
            </div>
            <div class="page-actions">
                <button type="button" class="btn btn-outline" id="btnExportExcel">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                        <polyline points="7 10 12 15 17 10"></polyline>
                        <line x1="12" y1="15" x2="12" y2="3"></line>
                    </svg>
                    Xuất Excel
                </button>
                <button type="button" class="btn btn-primary" id="btnThemKhachHang">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                        <line x1="12" y1="5" x2="12" y2="19"></line>
                        <line x1="5" y1="12" x2="19" y2="12"></line>
                    </svg>
                    Thêm Khách Hàng
                </button>
            </div>
        </div>

        <!-- Bảng danh sách khách hàng -->
        <div class="table-container">
            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width: 100px;">Mã KH</th>
                        <th>Tên Khách Hàng</th>
                        <th>Người Liên Hệ</th>
                        <th>Số Điện Thoại</th>
                        <th>Email</th>
                        <th>Trạng Thái</th>
                        <th style="width: 120px; text-align: center;">Thao Tác</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td class="font-mono">KH-001</td>
                        <td>
                            <div class="customer-name">Công ty Cổ phần Công nghệ ABC</div>
                            <div class="customer-sub">Hà Nội • CNTT</div>
                        </td>
                        <td>Nguyễn Văn An</td>
                        <td>0912 345 678</td>
                        <td>an.nguyen@abc-tech.vn</td>
                        <td><span class="badge badge-success">Đang Chăm Sóc</span></td>
                        <td style="text-align: center;">
                            <a href="#" class="btn-action" title="Xem chi tiết">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                            </a>
                        </td>
                    </tr>
                    <tr>
                        <td class="font-mono">KH-002</td>
                        <td>
                            <div class="customer-name">Tập đoàn Viễn thông & Bán lẻ XYZ</div>
                            <div class="customer-sub">TP. Hồ Chí Minh • Bán lẻ</div>
                        </td>
                        <td>Trần Thị Bích</td>
                        <td>0988 765 432</td>
                        <td>bich.tran@xyzcorp.com</td>
                        <td><span class="badge badge-warning">Tiềm Năng Cao</span></td>
                        <td style="text-align: center;">
                            <a href="#" class="btn-action" title="Xem chi tiết">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                            </a>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </main>

    <script src="${pageContext.request.contextPath}/assets/js/session-keep-alive.js"></script>
</body>
</html>
