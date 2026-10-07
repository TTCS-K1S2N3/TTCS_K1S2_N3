<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Danh mục khách hàng & Ghi chú cuộc gặp - Hệ thống CRM Bán Hàng">
    <title>Danh Mục Khách Hàng - CRM Bán Hàng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/khach-hang/khach-hang.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <script>
        window.CONTEXT_PATH = "${pageContext.request.contextPath}";
    </script>
</head>
<body class="crm-body">
    <!-- Thanh điều hướng và Sidebar chuẩn hệ thống S1-06 -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <!-- Thông báo kết quả thao tác -->
        <c:if test="${not empty thongBaoThanhCong}">
            <div class="alert alert-success" id="alertSuccess">
                <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                <span><c:out value="${thongBaoThanhCong}" /></span>
            </div>
        </c:if>
        <c:if test="${not empty thongBaoLoi}">
            <div class="alert alert-danger" id="alertError">
                <span class="material-symbols-outlined" aria-hidden="true">error</span>
                <span><c:out value="${thongBaoLoi}" /></span>
            </div>
        </c:if>

        <!-- Story S1-02: Khu vực soạn thảo ghi chú cuộc gặp (Ngồi ở quán cà phê không bị mất ghi chú) -->
        <div class="card" style="margin-bottom: 24px;">
            <div class="card-title">
                <div class="card-title-left">
                    <span class="material-symbols-outlined" aria-hidden="true" style="color: var(--primary);">edit_note</span>
                    <span>Ghi chú cuộc gặp khách hàng (Duy trì phiên & Tự động lưu)</span>
                </div>
                <div style="display: flex; align-items: center; gap: 12px; flex-wrap: wrap;">
                    <!-- Story S1-02: Thanh trạng thái phiên đăng nhập bảo mật và đếm ngược -->
                    <div class="session-indicator" id="session-indicator-box" title="Trạng thái phiên đăng nhập bảo mật">
                        <span class="session-dot active" id="session-status-dot"></span>
                        <span id="session-status-text">Phiên hoạt động</span>
                        <span class="session-countdown" id="session-countdown-timer" title="Thời gian phiên còn lại">30:00</span>
                    </div>
                    <span class="badge-info">
                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">check_circle</span>
                        Auto Keep-Alive Active
                    </span>
                </div>
            </div>

            <p style="color: var(--slate-500); font-size: 13.5px; margin-bottom: 14px;">
                Khi bạn đang ngồi tại quán cà phê soạn thảo ghi chú, hệ thống sẽ tự động gửi tín hiệu gia hạn phiên làm việc (AC1)
                và liên tục lưu bản nháp dự phòng để tránh mất nội dung khi kết nối mạng chập chờn.
            </p>

            <!-- Banner cảnh báo phát hiện bản nháp chưa lưu -->
            <div class="draft-alert-banner" id="draft-alert-banner">
                <div>
                    <strong><span class="material-symbols-outlined icon-sm" aria-hidden="true" style="vertical-align: text-bottom;">lightbulb</span> Phát hiện bản nháp trước đó:</strong>
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
                        <span class="material-symbols-outlined icon-sm" aria-hidden="true">save</span>
                        Phiên làm việc sẵn sàng
                    </span>
                    <span class="note-stat-item" id="note-last-saved" style="color: var(--slate-400);"></span>
                    <span class="note-stat-item" id="note-word-count" style="color: var(--slate-600); font-weight: 600;">0 từ</span>
                    <span class="note-stat-item" id="note-char-count" style="color: var(--slate-400);">0 ký tự</span>
                </div>
                <div class="note-actions-group">
                    <button type="button" class="btn-secondary" id="btn-manual-save" title="Lưu nháp ngay vào bộ nhớ trình duyệt">
                        <span class="material-symbols-outlined icon-sm" aria-hidden="true">save</span>
                        Lưu nháp
                    </button>
                    <button type="button" class="btn-secondary" id="btn-manual-sync" title="Gửi tín hiệu gia hạn phiên ngay">
                        <span class="material-symbols-outlined icon-sm" aria-hidden="true">sync</span>
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
                <button type="button" class="btn btn-outline" id="btnExportExcel" title="Tải danh sách khách hàng dưới dạng file Excel (.xlsx)">
                    <span class="material-symbols-outlined" aria-hidden="true">table_view</span>
                    <span>Xuất Excel</span>
                </button>
                <button type="button" class="btn btn-primary" id="btnThemKhachHang">
                    <span class="material-symbols-outlined" aria-hidden="true">add</span>
                    <span>Thêm Khách Hàng</span>
                </button>
            </div>
        </div>

        <!-- Bảng danh sách khách hàng lọc theo Data Scope -->
        <div class="table-container">
            <div style="padding: 12px 16px; background: #f8fafc; border-bottom: 1px solid var(--slate-200); display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 8px;">
                <span style="font-size: 13px; color: var(--slate-600);">
                    Phạm vi dữ liệu: <strong><c:out value="${not empty phamViHienTai ? phamViHienTai.tenHienThi : 'Của tôi'}" /></strong>
                    • Đang hiển thị: <strong><c:out value="${not empty tongSoKhachHang ? tongSoKhachHang : 0}" /></strong> khách hàng
                </span>
                <div style="display: flex; align-items: center; gap: 16px;">
                    <c:set var="userHienTai" value="${not empty nguoiDung ? nguoiDung : sessionScope.nguoiDung}" />
                    <c:set var="laQuanTriDanhMuc" value="${coQuyenDanhMuc or (not empty userHienTai and (userHienTai.coVaiTro('ADMIN') or userHienTai.coVaiTro('DIRECTOR')))}" />
                    <c:if test="${!laQuanTriDanhMuc}">
                        <a href="${pageContext.request.contextPath}/san-pham" style="font-size: 13px; color: var(--primary); text-decoration: none; font-weight: 600; display: inline-flex; align-items: center; gap: 4px;" id="linkSanPhamBangGia">
                            Sản phẩm & Bảng giá <span class="material-symbols-outlined icon-xs" aria-hidden="true">arrow_forward</span>
                        </a>
                    </c:if>
                    <a href="${pageContext.request.contextPath}/phan-quyen-du-lieu" style="font-size: 13px; color: var(--primary); text-decoration: none; font-weight: 600; display: inline-flex; align-items: center; gap: 4px;">
                        Quản lý 4 nghiệp vụ Data Scope <span class="material-symbols-outlined icon-xs" aria-hidden="true">arrow_forward</span>
                    </a>
                </div>
            </div>
            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width: 100px;">Mã KH</th>
                        <th>Tên Khách Hàng / Công Ty</th>
                        <th>Người Phụ Trách</th>
                        <th>Nhóm Kinh Doanh</th>
                        <th>Phân Loại</th>
                        <th>Trạng Thái</th>
                        <th style="width: 120px; text-align: center;">Thao Tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty danhSachKhachHang}">
                            <c:forEach var="kh" items="${danhSachKhachHang}">
                                <tr>
                                    <td class="font-mono"><c:out value="${kh.maBanGhi}" /></td>
                                    <td>
                                        <div class="customer-name"><c:out value="${kh.tieuDe}" /></div>
                                        <div class="customer-sub"><c:out value="${kh.moTaChiTiet}" /></div>
                                    </td>
                                    <td><c:out value="${kh.tenNguoiPhuTrach}" /></td>
                                    <td><c:out value="${kh.tenNhom}" /></td>
                                    <td><c:out value="${kh.giaTri}" /></td>
                                    <td><span class="badge badge-success"><c:out value="${kh.trangThai}" /></span></td>
                                    <td style="text-align: center;">
                                        <a href="${pageContext.request.contextPath}/khach-hang/chi-tiet?id=${kh.id}" class="btn-action" title="Xem hồ sơ 360° khách hàng" aria-label="Xem hồ sơ 360° khách hàng">
                                            <span class="material-symbols-outlined" aria-hidden="true">visibility</span>
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="7" style="text-align: center; padding: 32px; color: var(--slate-500);">
                                    Không tìm thấy khách hàng nào trong phạm vi dữ liệu tài khoản của bạn.
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>

        <!-- Modal Thêm Khách Hàng Mới (Story S1-05) -->
        <div class="modal-backdrop" id="modalThemKhachHang" style="display: none;" role="dialog" aria-modal="true" aria-labelledby="modalThemKhachHangTieuDe">
            <div class="modal-card">
                <div class="modal-header">
                    <div>
                        <h2 class="modal-title" id="modalThemKhachHangTieuDe">Thêm Khách Hàng Mới</h2>
                        <p class="modal-subtitle">Tạo mới khách hàng thuộc phạm vi sở hữu của tài khoản hiện tại (S1-05)</p>
                    </div>
                    <button type="button" class="modal-close-btn" id="btnDongModalThemKhachHang" aria-label="Đóng" title="Đóng">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>
                <form id="formThemKhachHang" method="POST" action="${pageContext.request.contextPath}/khach-hang">
                    <input type="hidden" name="action" value="them">
                    <div class="modal-body">
                        <div class="form-group">
                            <label class="form-label" for="tenCongTy">Tên khách hàng / Công ty <span class="required">*</span></label>
                            <input type="text" id="tenCongTy" name="tenCongTy" class="form-input" placeholder="Ví dụ: Công ty Cổ phần Công nghệ ABC" required autocomplete="off">
                        </div>
                        <div class="form-row">
                            <div class="form-col">
                                <label class="form-label" for="maKhachHang">Mã khách hàng</label>
                                <input type="text" id="maKhachHang" name="maKhachHang" class="form-input" placeholder="Tự sinh nếu để trống" autocomplete="off">
                            </div>
                            <div class="form-col">
                                <label class="form-label" for="doanhThuUocTinh">Doanh thu ước tính</label>
                                <input type="text" id="doanhThuUocTinh" name="doanhThuUocTinh" class="form-input" placeholder="Ví dụ: 100,000,000 đ" autocomplete="off">
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="trangThai">Trạng thái</label>
                            <select id="trangThai" name="trangThai" class="form-select">
                                <option value="Tiềm năng" selected>Tiềm năng</option>
                                <option value="Đang tiếp cận">Đang tiếp cận</option>
                                <option value="Đang hợp tác">Đang hợp tác</option>
                                <option value="Khách hàng VIP">Khách hàng VIP</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Người sở hữu (Data Scope Server Enforcement)</label>
                            <div class="form-readonly-badge">
                                <span class="material-symbols-outlined icon-sm" aria-hidden="true">person</span>
                                <span>Chủ sở hữu: <strong><c:out value="${not empty currentUser ? currentUser.hoTen : sessionScope.nguoiDung.hoTen}" /></strong> &bull; <c:out value="${not empty currentUser ? currentUser.tenNhom : sessionScope.nguoiDung.tenNhomKinhDoanh}" /></span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="moTaChiTiet">Ghi chú / Mô tả chi tiết</label>
                            <textarea id="moTaChiTiet" name="moTaChiTiet" class="form-textarea" rows="3" placeholder="Nhập thêm nhu cầu, lĩnh vực, liên hệ..."></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline" id="btnHuyThemKhachHang">Hủy bỏ</button>
                        <button type="submit" class="btn btn-primary" id="btnXacNhanThemKhachHang">
                            <span class="material-symbols-outlined" aria-hidden="true">save</span>
                            Lưu Khách Hàng
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </main>

    <script src="${pageContext.request.contextPath}/assets/js/navigation.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/session-keep-alive.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/khach-hang.js"></script>
</body>
</html>
