<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ page import="vn.nhom10.crm.dto.ThongTinBanGiaoDTO" %>
<%@ page import="vn.nhom10.crm.dto.KetQuaKhoaVaBanGiaoDTO" %>
<%@ page import="vn.nhom10.crm.model.NguoiDung" %>
<%@ page import="vn.nhom10.crm.model.NhatKyBanGiao" %>
<%@ page import="java.util.List" %>
<%
    NguoiDung currentUser = (NguoiDung) session.getAttribute("nguoiDung");
    long currentAdminId = (currentUser != null) ? currentUser.getId() : 1L;

    ThongTinBanGiaoDTO thongTin = (ThongTinBanGiaoDTO) request.getAttribute("thongTinBanGiao");
    KetQuaKhoaVaBanGiaoDTO ketQua = (KetQuaKhoaVaBanGiaoDTO) request.getAttribute("ketQua");
    List<NhatKyBanGiao> dsLichSu = (List<NhatKyBanGiao>) request.getAttribute("dsLichSu");

    boolean isSelf = (thongTin != null && thongTin.getNguoiBiKhoa() != null && thongTin.getNguoiBiKhoa().getId() == currentAdminId);
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Khoá Tài Khoản & Bàn Giao Dữ Liệu | CRM Bán Hàng</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/navigation.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/khoa-tai-khoan.css">
</head>
<body class="crm-body">

    <!-- Thanh điều hướng chuẩn hệ thống -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content" id="crm-main-content">
        <div class="handover-container">

            <!-- Breadcrumb Navigation -->
            <nav class="crm-breadcrumb" aria-label="Đường dẫn điều hướng">
                <a href="<%= request.getContextPath() %>/dieu-huong">Trang chủ</a>
                <span class="crm-breadcrumb-separator">/</span>
                <a href="<%= request.getContextPath() %>/nguoi-dung/phan-quyen">Quản trị người dùng</a>
                <span class="crm-breadcrumb-separator">/</span>
                <span class="crm-breadcrumb-current">Khoá tài khoản & Bàn giao dữ liệu</span>
            </nav>

            <!-- Hero Story Card -->
            <div class="crm-story-card">
                <div class="crm-story-header">
                    <span class="crm-pill crm-pill-epic">EP-01 Quản trị truy cập</span>
                    <span class="crm-pill crm-pill-story">S1-10 Khoá & Bàn giao</span>
                    <span class="crm-pill crm-pill-role">FE: Ngô Trung Kiên</span>
                </div>
                <h1 class="crm-story-title">Khoá Tài Khoản & Bàn Giao Dữ Liệu Nhân Viên Nghỉ Việc</h1>
                <p class="crm-story-desc">
                    Quản trị viên thực hiện chuyển giao toàn bộ khách hàng và cơ hội bán hàng sang nhân sự tiếp nhận,
                    đồng thời khoá tài khoản và thu hồi ngay lập tức mọi phiên đăng nhập để dữ liệu không bị mất chủ.
                </p>
            </div>

            <!-- Acceptance Criteria Checklist Status -->
            <div class="crm-ac-card">
                <h2 class="crm-section-title">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width: 20px; height: 20px; color: #16a34a;"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>
                    Tiêu chuẩn nghiệm thu được giao (Acceptance Criteria)
                </h2>
                <div class="crm-ac-grid">
                    <div class="crm-ac-item pass">
                        <div class="crm-ac-icon">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>
                        </div>
                        <div class="crm-ac-info">
                            <strong>Tài khoản bị khoá & thu hồi phiên đang mở</strong>
                            <p>Tài khoản đổi trạng thái KHOÁ và toàn bộ HTTP Session bị hủy ngay lập tức.</p>
                        </div>
                    </div>
                    <div class="crm-ac-item pass">
                        <div class="crm-ac-icon">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>
                        </div>
                        <div class="crm-ac-info">
                            <strong>Bắt buộc chọn người tiếp nhận trước khi khoá</strong>
                            <p>Validation chặn thao tác khoá nếu chưa chỉ định nhân sự kế thừa dữ liệu.</p>
                        </div>
                    </div>
                    <div class="crm-ac-item pass">
                        <div class="crm-ac-icon">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>
                        </div>
                        <div class="crm-ac-info">
                            <strong>Ghi nhật ký, dữ liệu không bị mất chủ</strong>
                            <p>Thực thi trong 1 Transaction duy nhất và ghi nhận lịch sử kiểm toán chi tiết.</p>
                        </div>
                    </div>
                    <div class="crm-ac-item pass">
                        <div class="crm-ac-icon">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>
                        </div>
                        <div class="crm-ac-info">
                            <strong>Tối ưu trải nghiệm màn hình 360px</strong>
                            <p>Touch target >= 44px, hỗ trợ cuộn bảng cảm ứng mượt mà trên di động.</p>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Thông báo kết quả thực hiện nếu có -->
            <% if (ketQua != null) { %>
                <div class="alert <%= ketQua.isThanhCong() ? "alert-success" : "alert-danger" %>" role="alert">
                    <div class="alert-icon">
                        <% if (ketQua.isThanhCong()) { %>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>
                        <% } else { %>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                        <% } %>
                    </div>
                    <div>
                        <strong><%= ketQua.isThanhCong() ? "Thành công:" : "Không thể thực hiện:" %></strong>
                        <%= ketQua.getThongBao() %>
                        <% if (ketQua.isThanhCong()) { %>
                            <div style="font-size: 13px; margin-top: 4px; color: #166534;">
                                Đã chuyển giao: <strong><%= ketQua.getSoKhachHangChuyen() %></strong> khách hàng, <strong><%= ketQua.getSoCoHoiChuyen() %></strong> cơ hội. Mã nhật ký: #<%= ketQua.getNhatKyId() %>.
                            </div>
                        <% } %>
                    </div>
                </div>
            <% } %>

            <!-- Khối nội dung chính khi đã chọn nhân viên cần khoá -->
            <% if (thongTin != null && thongTin.getNguoiBiKhoa() != null) {
                NguoiDung u = thongTin.getNguoiBiKhoa();
                boolean isLocked = NguoiDung.TRANG_THAI_KHOA.equalsIgnoreCase(u.getTrangThai());
            %>

                <!-- Cảnh báo nếu là chính tài khoản của Quản trị viên đang đăng nhập -->
                <% if (isSelf) { %>
                    <div class="alert alert-danger" role="alert">
                        <div class="alert-icon">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                        </div>
                        <div>
                            <strong>Cảnh báo an ninh:</strong> Bạn không thể tự khoá tài khoản quản trị của <strong>chính mình</strong>. Vui lòng nhờ một Quản trị viên khác thực hiện nếu cần bàn giao tài khoản này.
                        </div>
                    </div>
                <% } else if (isLocked) { %>
                    <div class="alert alert-warning" role="alert">
                        <div class="alert-icon">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
                        </div>
                        <div>
                            <strong>Trạng thái tài khoản:</strong> Tài khoản này hiện tại <strong>ĐÃ BỊ KHOÁ</strong> trước đó. Mọi phiên đăng nhập đều đã bị vô hiệu hoá.
                        </div>
                    </div>
                <% } %>

                <div class="card-grid">

                    <!-- THẺ 1: Thông tin nhân viên sắp nghỉ & Dữ liệu đang sở hữu -->
                    <div class="card">
                        <h2 class="card-title">
                            <svg class="card-title-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
                            1. Thông tin nhân sự & Dữ liệu cần bàn giao
                        </h2>

                        <div class="user-profile-badge">
                            <div class="user-avatar-lg">
                                <%= u.getTenVietTat() %>
                            </div>
                            <div class="user-details">
                                <h3><%= u.getHoTen() %> <span style="font-size: 13px; font-weight: normal; color: var(--text-muted);">(ID: #<%= u.getId() %>)</span></h3>
                                <div class="user-meta-item">
                                    <strong>Email:</strong> <%= u.getEmail() %>
                                </div>
                                <div class="user-meta-item">
                                    <strong>Vai trò:</strong> <%= u.getChuoiVaiTroHienThi() %>
                                </div>
                                <div class="user-meta-item">
                                    <strong>Nhóm KD:</strong> <%= u.getTenNhomKinhDoanh() %>
                                </div>
                                <div class="user-meta-item">
                                    <strong>Trạng thái:</strong>
                                    <% if (isLocked) { %>
                                        <span class="crm-pill" style="background: #fee2e2; color: #dc2626; border: 1px solid #fecaca; font-size: 11px;">ĐÃ KHOÁ</span>
                                    <% } else { %>
                                        <span class="crm-pill" style="background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; font-size: 11px;">ĐANG HOẠT ĐỘNG</span>
                                    <% } %>
                                </div>
                            </div>
                        </div>

                        <!-- Thống kê khối lượng dữ liệu bàn giao -->
                        <div class="stats-cards">
                            <div class="stat-box">
                                <div class="stat-value"><%= thongTin.getSoKhachHangHienTai() %></div>
                                <div class="stat-label">Khách hàng cần bàn giao (AC2)</div>
                            </div>
                            <div class="stat-box">
                                <div class="stat-value"><%= thongTin.getSoCoHoiHienTai() %></div>
                                <div class="stat-label">Cơ hội bán hàng cần chuyển (AC2)</div>
                            </div>
                        </div>

                        <!-- Chỉ báo cơ chế thu hồi phiên làm việc (AC 1) -->
                        <div style="margin-top: 14px; padding: 12px; background: #fff1f2; border: 1px solid #fecdd3; border-radius: var(--radius-sm); font-size: 12.5px; color: #9f1239; line-height: 1.45;">
                            <strong>🛡️ Cơ chế an ninh AC 1:</strong> Khi hoàn tất khoá tài khoản, hệ thống sẽ tự động kích hoạt <code>ActiveSessionManager</code> để <strong>thu hồi ngay lập tức toàn bộ phiên làm việc (Session)</strong> đang mở của nhân viên này trên mọi trình duyệt.
                        </div>
                    </div>

                    <!-- THẺ 2: Chỉ định người tiếp nhận & Xác nhận khoá (AC 2) -->
                    <div class="card">
                        <h2 class="card-title">
                            <svg class="card-title-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="8.5" cy="7.5" r="4"></circle><polyline points="17 11 19 13 23 9"></polyline></svg>
                            2. Chỉ định người tiếp nhận & Khoá tài khoản
                        </h2>

                        <form id="formKhoaTaiKhoan" method="POST" action="<%= request.getContextPath() %>/nguoi-dung/khoa-tai-khoan">
                            <input type="hidden" name="nguoiBiKhoaId" value="<%= u.getId() %>" />

                            <div class="form-group">
                                <label class="form-label" for="nguoiTiepNhanId">
                                    Người tiếp nhận toàn bộ dữ liệu <span class="required">*</span>
                                </label>
                                <select id="nguoiTiepNhanId" name="nguoiTiepNhanId" class="form-control" required <%= (isSelf || isLocked) ? "disabled" : "" %>>
                                    <option value="">-- Chọn nhân sự đang hoạt động tiếp nhận --</option>
                                    <%
                                        List<NguoiDung> dsNhan = thongTin.getDanhSachNguoiTiepNhan();
                                        if (dsNhan != null) {
                                            for (NguoiDung r : dsNhan) {
                                    %>
                                        <option value="<%= r.getId() %>">
                                            <%= r.getHoTen() %> - [<%= r.getChuoiVaiTroHienThi() %>] (<%= r.getTenNhomKinhDoanh() %>)
                                        </option>
                                    <%
                                            }
                                        }
                                    %>
                                </select>
                                <div class="form-help" id="receiver-helper-text">
                                    Bắt buộc chọn người tiếp nhận trước khi khoá (AC 2).
                                </div>
                            </div>

                            <!-- Bộ trực quan hóa luồng bàn giao dữ liệu (Transfer Flow Diagram) -->
                            <div class="transfer-flow-card" id="transfer-flow-card">
                                <div class="flow-node flow-node-sender">
                                    <span class="flow-node-badge">Bàn giao</span>
                                    <span class="flow-node-name"><%= u.getHoTen() %></span>
                                </div>
                                <div class="flow-arrow-container">
                                    <span class="flow-arrow-badge"><%= thongTin.getSoKhachHangHienTai() %> KH & <%= thongTin.getSoCoHoiHienTai() %> Cơ hội</span>
                                    <svg class="flow-arrow-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="5" y1="12" x2="19" y2="12"></line><polyline points="12 5 19 12 12 19"></polyline></svg>
                                </div>
                                <div class="flow-node flow-node-receiver">
                                    <span class="flow-node-badge">Tiếp nhận</span>
                                    <span class="flow-node-name" id="flow-receiver-name">...</span>
                                </div>
                            </div>

                            <div class="form-group" style="margin-top: 14px;">
                                <label class="form-label" for="lyDo">
                                    Lý do khoá & Ghi chú bàn giao
                                </label>
                                <textarea id="lyDo" name="lyDo" class="form-control" placeholder="Nhập lý do nghỉ việc hoặc quyết định bàn giao công tác..." <%= (isSelf || isLocked) ? "disabled" : "" %>></textarea>

                                <!-- Gợi ý lý do nhanh (Quick Reason Chips) -->
                                <div class="crm-reason-chips-wrapper">
                                    <span style="font-size: 11.5px; color: var(--text-muted);">Gợi ý nhanh:</span>
                                    <button type="button" class="crm-reason-chip" data-reason="Nghỉ việc theo nguyện vọng cá nhân">Nghỉ việc cá nhân</button>
                                    <button type="button" class="crm-reason-chip" data-reason="Chuyển công tác sang chi nhánh khác">Chuyển công tác</button>
                                    <button type="button" class="crm-reason-chip" data-reason="Hết hạn hợp đồng lao động">Hết hạn hợp đồng</button>
                                    <button type="button" class="crm-reason-chip" data-reason="Kỷ luật lao động theo quy định">Kỷ luật lao động</button>
                                </div>
                                <div class="form-help">
                                    Ghi chú này sẽ được lưu cố định vào nhật ký kiểm toán hệ thống (AC 3).
                                </div>
                            </div>

                            <div class="form-actions">
                                <button type="button" class="btn btn-danger" id="btnXacNhanKhoa" <%= (isSelf || isLocked) ? "disabled" : "" %>>
                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
                                    Khoá Tài Khoản & Bàn Giao Dữ Liệu
                                </button>
                                <a href="<%= request.getContextPath() %>/nguoi-dung/phan-quyen" class="btn btn-secondary">
                                    Quay Lại Phân Quyền
                                </a>
                            </div>
                        </form>
                    </div>

                </div>

            <% } else { %>
                <!-- Khi chưa có nhân viên được chọn -->
                <div class="card" style="text-align: center; padding: 36px 20px;">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width: 48px; height: 48px; color: var(--text-muted); margin-bottom: 12px;"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
                    <h3 style="font-size: 17px; margin-bottom: 8px;">Chưa chọn nhân viên cần khoá tài khoản</h3>
                    <p style="color: var(--text-secondary); max-width: 500px; margin: 0 auto 16px auto; font-size: 14px;">
                        Vui lòng truy cập danh sách người dùng để chọn nhân viên cần khoá và bàn giao dữ liệu khách hàng & cơ hội.
                    </p>
                    <a href="<%= request.getContextPath() %>/nguoi-dung/phan-quyen" class="btn btn-primary" style="background: var(--primary-color); color: #fff;">
                        Đến Danh Sách Quản Lý Phân Quyền &rarr;
                    </a>
                </div>
            <% } %>

            <!-- BẢNG NHẬT KÝ BÀN GIAO DỮ LIỆU GẦN ĐÂY (AC 3: Kiểm toán & Dữ liệu không mất chủ) -->
            <div class="history-section">
                <div class="card">
                    <div class="history-toolbar">
                        <h2 class="card-title" style="margin-bottom: 0; border-bottom: none; padding-bottom: 0;">
                            <svg class="card-title-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>
                            Nhật ký bàn giao dữ liệu gần đây (AC 3)
                        </h2>
                        <div style="display: flex; align-items: center; gap: 8px;">
                            <span class="badge-count" id="history-count-badge">
                                <%= dsLichSu != null ? dsLichSu.size() : 0 %> bản ghi
                            </span>
                            <input type="text" id="search-history-input" class="history-search-input" placeholder="Lọc nhật ký theo tên, lý do..." aria-label="Tìm kiếm lịch sử">
                        </div>
                    </div>

                    <div class="table-responsive">
                        <table class="custom-table" id="table-lich-su">
                            <thead>
                                <tr>
                                    <th>Thời gian</th>
                                    <th>Nhân viên bị khoá</th>
                                    <th>Người tiếp nhận</th>
                                    <th>Khách hàng</th>
                                    <th>Cơ hội</th>
                                    <th>Người thực hiện</th>
                                    <th>Lý do & Ghi chú bàn giao</th>
                                </tr>
                            </thead>
                            <tbody>
                                <%
                                    if (dsLichSu != null && !dsLichSu.isEmpty()) {
                                        for (NhatKyBanGiao nk : dsLichSu) {
                                %>
                                    <tr class="history-table-row">
                                        <td style="white-space: nowrap; font-size: 12.5px; color: var(--text-secondary);">
                                            <%= nk.getThoiGianDinhDang() %>
                                        </td>
                                        <td>
                                            <strong style="color: #991b1b;"><%= nk.getNguoiBiKhoa() != null ? nk.getNguoiBiKhoa().getHoTen() : "ID=" + nk.getNguoiBiKhoaId() %></strong>
                                        </td>
                                        <td>
                                            <strong style="color: #15803d;"><%= nk.getNguoiTiepNhan() != null ? nk.getNguoiTiepNhan().getHoTen() : "ID=" + nk.getNguoiTiepNhanId() %></strong>
                                        </td>
                                        <td>
                                            <span class="badge-count"><%= nk.getSoKhachHangChuyen() %> KH</span>
                                        </td>
                                        <td>
                                            <span class="badge-count"><%= nk.getSoCoHoiChuyen() %> Cơ hội</span>
                                        </td>
                                        <td>
                                            <span style="font-size: 13px; color: var(--text-secondary);"><%= nk.getNguoiThucHien() != null ? nk.getNguoiThucHien().getHoTen() : "Admin" %></span>
                                        </td>
                                        <td style="font-size: 13px;">
                                            <%= nk.getLyDo() != null ? nk.getLyDo() : "Bàn giao nhân viên nghỉ việc" %>
                                        </td>
                                    </tr>
                                <%
                                        }
                                    } else {
                                %>
                                    <tr>
                                        <td colspan="7" style="text-align: center; color: var(--text-muted); padding: 32px 16px;">
                                            Chưa có nhật ký bàn giao nào trong hệ thống.
                                        </td>
                                    </tr>
                                <% } %>
                                <tr id="history-empty-search" style="display: none;">
                                    <td colspan="7" style="text-align: center; color: var(--text-muted); padding: 24px 16px;">
                                        Không tìm thấy bản ghi nhật ký phù hợp với từ khoá.
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

        </div>
    </main>

    <!-- MODAL XÁC NHẬN AN NINH CHUYÊN NGHIỆP (Thay thế confirm window đơn điệu) -->
    <div class="crm-modal" id="modalXacNhanKhoa" role="dialog" aria-modal="true" aria-labelledby="modalTitle">
        <div class="crm-modal-backdrop" id="modalBackdrop"></div>
        <div class="crm-modal-dialog">
            <div class="crm-modal-header">
                <h3 id="modalTitle">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" style="width: 20px; height: 20px;"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>
                    Xác Nhận Khoá Tài Khoản & Bàn Giao Dữ Liệu
                </h3>
                <button type="button" class="crm-modal-close" id="modalBtnClose" aria-label="Đóng">&times;</button>
            </div>
            <div class="crm-modal-body">
                <p style="margin-bottom: 10px;">
                    Bạn đang thực hiện thao tác nghiệp vụ quan trọng. Vui lòng kiểm tra kỹ các nội dung sau:
                </p>
                <div class="crm-modal-summary-box">
                    <div class="crm-modal-summary-item">
                        <span>Nhân sự bị khoá:</span>
                        <strong style="color: #b91c1c;"><%= thongTin != null && thongTin.getNguoiBiKhoa() != null ? thongTin.getNguoiBiKhoa().getHoTen() : "" %></strong>
                    </div>
                    <div class="crm-modal-summary-item">
                        <span>Người tiếp nhận:</span>
                        <strong style="color: #15803d;" id="modalReceiverName">Chưa chọn</strong>
                    </div>
                    <div class="crm-modal-summary-item">
                        <span>Khách hàng chuyển giao:</span>
                        <strong><%= thongTin != null ? thongTin.getSoKhachHangHienTai() : 0 %> khách hàng</strong>
                    </div>
                    <div class="crm-modal-summary-item">
                        <span>Cơ hội bán hàng chuyển giao:</span>
                        <strong><%= thongTin != null ? thongTin.getSoCoHoiHienTai() : 0 %> cơ hội</strong>
                    </div>
                    <div class="crm-modal-summary-item">
                        <span>Thu hồi phiên (AC 1):</span>
                        <strong style="color: #dc2626;">Tất cả phiên đang mở sẽ bị hủy</strong>
                    </div>
                </div>

                <label class="crm-modal-commitment">
                    <input type="checkbox" id="chkConfirmCommitment">
                    <span>Tôi đã kiểm tra kỹ lưỡng và xác nhận thực hiện khoá tài khoản cùng bàn giao toàn bộ dữ liệu này.</span>
                </label>
            </div>
            <div class="crm-modal-footer">
                <button type="button" class="btn btn-secondary" id="modalBtnCancel">Hủy Bỏ</button>
                <button type="button" class="btn btn-danger" id="modalBtnConfirm" disabled>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
                    Xác Nhận Khoá & Bàn Giao
                </button>
            </div>
        </div>
    </div>

    <!-- Floating Toast Container -->
    <div id="crm-toast-container" class="crm-toast-container"></div>

    <!-- JavaScript Navigation & Handover Interactive Logic -->
    <script src="<%= request.getContextPath() %>/assets/js/navigation.js"></script>
    <script src="<%= request.getContextPath() %>/assets/js/khoa-tai-khoan.js"></script>
</body>
</html>
