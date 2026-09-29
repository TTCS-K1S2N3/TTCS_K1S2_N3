<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="vn.nhom10.crm.model.NguoiDung" %>
<%@ page import="vn.nhom10.crm.model.VaiTro" %>
<%@ page import="vn.nhom10.crm.model.NhomKinhDoanh" %>
<%@ page import="vn.nhom10.crm.model.VaiTroEnum" %>
<%@ page import="java.util.List" %>
<%
    List<NguoiDung> dsNguoiDung = (List<NguoiDung>) request.getAttribute("dsNguoiDung");
    List<VaiTro> dsVaiTro = (List<VaiTro>) request.getAttribute("dsVaiTro");
    List<NhomKinhDoanh> dsNhomKinhDoanh = (List<NhomKinhDoanh>) request.getAttribute("dsNhomKinhDoanh");
    NguoiDung selectedUser = (NguoiDung) request.getAttribute("nguoiDungDuocChon");
    String thongBao = (String) request.getAttribute("thongBao");
    Boolean thanhCong = (Boolean) request.getAttribute("thanhCong");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gán Vai Trò & Nhóm Kinh Doanh | CRM Bán Hàng</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/navigation.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/phan-quyen.css">
</head>
<body class="crm-body">

    <!-- Thanh điều hướng và Sidebar chuẩn hệ thống -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <!-- Nội dung chính -->
    <main class="crm-main-content" id="crm-main-content">
        <div class="crm-container">

            <!-- Banner Story Info -->
            <div class="crm-story-card">
                <div class="crm-story-header">
                    <span class="crm-pill crm-pill-epic">EP-01</span>
                    <span class="crm-pill crm-pill-story">S1-09</span>
                    <span class="crm-pill crm-pill-role">BE: Thào A Khua</span>
                </div>
                <h1 class="crm-story-title">Gán Vai Trò & Gắn Người Dùng Vào Nhóm Kinh Doanh</h1>
                <p class="crm-story-desc">
                    Quản trị viên thiết lập vai trò và nhóm kinh doanh cho nhân sự để cây tổ chức
                    quyết định đúng phạm vi dữ liệu và quyền truy cập chức năng của từng người.
                </p>
            </div>

            <!-- Acceptance Criteria Checklist Status -->
            <div class="crm-ac-card">
                <h2 class="crm-section-title">Tiêu chuẩn nghiệm thu (Acceptance Criteria)</h2>
                <div class="crm-ac-grid">
                    <div class="crm-ac-item pass">
                        <div class="crm-ac-icon">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>
                        </div>
                        <div class="crm-ac-info">
                            <strong>Một người dùng có thể giữ nhiều vai trò cùng lúc</strong>
                            <p>Hỗ trợ gán đồng thời nhiều vai trò qua hệ thống checkbox (N-N).</p>
                        </div>
                    </div>
                    <div class="crm-ac-item pass">
                        <div class="crm-ac-icon">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>
                        </div>
                        <div class="crm-ac-info">
                            <strong>Trưởng nhóm bắt buộc phải được gán nhóm cụ thể</strong>
                            <p>Validation server-side ngăn chặn gán Trưởng nhóm khi chưa chọn nhóm.</p>
                        </div>
                    </div>
                    <div class="crm-ac-item pass">
                        <div class="crm-ac-icon">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>
                        </div>
                        <div class="crm-ac-info">
                            <strong>Không thể tự thu hồi vai trò quản trị của chính mình</strong>
                            <p>Bảo vệ tài khoản quản trị viên hiện tại không bị tước mất quyền Admin.</p>
                        </div>
                    </div>
                    <div class="crm-ac-item pass">
                        <div class="crm-ac-icon">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>
                        </div>
                        <div class="crm-ac-info">
                            <strong>Dùng được thuận tiện trên màn hình 360px</strong>
                            <p>Giao diện co giãn thông minh, phím bấm >= 44px, không tràn ngang viền.</p>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Thông báo phản hồi (Alert Banner) -->
            <% if (thongBao != null) { %>
                <div class="crm-alert <%= (thanhCong != null && thanhCong) ? "crm-alert-success" : "crm-alert-danger" %>" role="alert">
                    <div class="crm-alert-icon">
                        <% if (thanhCong != null && thanhCong) { %>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>
                        <% } else { %>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                        <% } %>
                    </div>
                    <div class="crm-alert-text">
                        <strong><%= (thanhCong != null && thanhCong) ? "Thành công:" : "Không thể thực hiện:" %></strong>
                        <%= thongBao %>
                    </div>
                </div>
            <% } %>

            <!-- Bộ chọn người dùng (User Selector) -->
            <div class="crm-card">
                <div class="crm-card-header">
                    <h2 class="crm-card-title">1. Chọn người dùng cần phân quyền & gán nhóm</h2>
                    <span class="crm-badge crm-badge-count">Tổng: <%= dsNguoiDung != null ? dsNguoiDung.size() : 0 %> người dùng</span>
                </div>
                <div class="crm-user-selector-grid">
                    <% if (dsNguoiDung != null) {
                        for (NguoiDung nd : dsNguoiDung) {
                            boolean isCurrent = selectedUser != null && selectedUser.getId() == nd.getId();
                    %>
                        <a href="?id=<%= nd.getId() %>" class="crm-user-select-item <%= isCurrent ? "active" : "" %>">
                            <div class="crm-user-item-avatar"><%= nd.getTenVietTat() %></div>
                            <div class="crm-user-item-info">
                                <div class="crm-user-item-name"><%= nd.getHoTen() %></div>
                                <div class="crm-user-item-email"><%= nd.getEmail() %></div>
                                <div class="crm-user-item-role"><%= nd.getChuoiVaiTroHienThi() %></div>
                            </div>
                        </a>
                    <%  }
                    } %>
                </div>
            </div>

            <!-- Form Gán vai trò & nhóm kinh doanh -->
            <% if (selectedUser != null) { %>
            <form action="<%= request.getContextPath() %>/nguoi-dung/phan-quyen" method="POST" class="crm-form-role-assignment" id="form-assign-role">
                <input type="hidden" name="nguoiDungId" value="<%= selectedUser.getId() %>">

                <!-- Header người dùng đang chọn -->
                <div class="crm-selected-user-card">
                    <div class="crm-selected-user-avatar"><%= selectedUser.getTenVietTat() %></div>
                    <div class="crm-selected-user-info">
                        <div class="crm-selected-user-name"><%= selectedUser.getHoTen() %> (ID: #<%= selectedUser.getId() %>)</div>
                        <div class="crm-selected-user-meta">
                            <span>Email: <strong><%= selectedUser.getEmail() %></strong></span>
                            <span>Trạng thái: <strong class="text-success"><%= selectedUser.getTrangThai() %></strong></span>
                        </div>
                    </div>
                    <div style="margin-left: auto; display: flex; align-items: center;">
                        <a href="<%= request.getContextPath() %>/nguoi-dung/khoa-tai-khoan?id=<%= selectedUser.getId() %>"
                           class="crm-btn crm-btn-danger"
                           style="display: inline-flex; align-items: center; gap: 0.35rem; font-size: 0.85rem; padding: 0.5rem 0.85rem; background-color: #dc2626; color: #fff; text-decoration: none; border-radius: 6px; font-weight: 600;">
                            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
                            Khoá & Bàn giao
                        </a>
                    </div>
                </div>

                <!-- 2. Gán Vai Trò (Hỗ trợ Nhiều Vai Trò Cùng Lúc) -->
                <div class="crm-card">
                    <div class="crm-card-header">
                        <h2 class="crm-card-title">2. Gán vai trò (Có thể chọn nhiều vai trò cùng lúc)</h2>
                        <span class="crm-badge crm-badge-info">Tiêu chí: Đa vai trò</span>
                    </div>
                    <p class="crm-field-hint">Tích chọn các vai trò mà người dùng này được phép đảm nhiệm trong hệ thống:</p>

                    <div class="crm-roles-grid">
                        <% if (dsVaiTro != null) {
                            for (VaiTro vt : dsVaiTro) {
                                boolean checked = selectedUser.coVaiTro(vt.getMaVaiTro());
                                boolean isTeamLead = "TEAM_LEAD".equalsIgnoreCase(vt.getMaVaiTro());
                                boolean isAdmin = "ADMIN".equalsIgnoreCase(vt.getMaVaiTro());
                        %>
                            <label class="crm-role-checkbox-card <%= checked ? "checked" : "" %>" id="role-card-<%= vt.getId() %>">
                                <div class="crm-checkbox-header">
                                    <input type="checkbox"
                                           name="vaiTroIds"
                                           value="<%= vt.getId() %>"
                                           data-code="<%= vt.getMaVaiTro() %>"
                                           class="crm-checkbox-input"
                                           <%= checked ? "checked" : "" %>
                                           id="chk-role-<%= vt.getId() %>">
                                    <span class="crm-role-title"><%= vt.getTenVaiTro() %></span>
                                </div>
                                <div class="crm-role-desc"><%= vt.getMoTa() != null ? vt.getMoTa() : "" %></div>
                                <div class="crm-role-badges">
                                    <span class="crm-role-code-badge"><%= vt.getMaVaiTro() %></span>
                                    <% if (isTeamLead) { %>
                                        <span class="crm-badge-required-team">Cần nhóm KD</span>
                                    <% } %>
                                    <% if (isAdmin) { %>
                                        <span class="crm-badge-admin-safe">Bảo vệ quản trị</span>
                                    <% } %>
                                </div>
                            </label>
                        <%  }
                        } %>
                    </div>
                </div>

                <!-- 3. Gắn Nhóm Kinh Doanh (Cây tổ chức) -->
                <div class="crm-card">
                    <div class="crm-card-header">
                        <h2 class="crm-card-title">3. Gắn vào nhóm kinh doanh (Cây tổ chức)</h2>
                        <span class="crm-badge crm-badge-warning">Bắt buộc cho Trưởng nhóm</span>
                    </div>
                    <div class="crm-form-group">
                        <label for="select-nhom-kd" class="crm-label">
                            Chọn nhóm kinh doanh trực thuộc:
                        </label>
                        <select name="nhomKinhDoanhId" id="select-nhom-kd" class="crm-select">
                            <option value="">-- Chưa phân nhóm kinh doanh --</option>
                            <% if (dsNhomKinhDoanh != null) {
                                for (NhomKinhDoanh nkd : dsNhomKinhDoanh) {
                                    boolean isSelected = selectedUser.getNhomKinhDoanhId() != null && selectedUser.getNhomKinhDoanhId().equals(nkd.getId());
                            %>
                                <option value="<%= nkd.getId() %>" <%= isSelected ? "selected" : "" %>>
                                    <%= nkd.getTenNhom() %> (<%= nkd.getMaNhom() %>)
                                </option>
                            <%  }
                            } %>
                        </select>
                        <p class="crm-input-helper" id="helper-team-lead">
                            <strong>Lưu ý:</strong> Người dùng giữ vai trò <em>Trưởng nhóm kinh doanh</em> bắt buộc phải được gắn vào một nhóm cụ thể để xác định cây phân quyền dữ liệu.
                        </p>
                    </div>
                </div>

                <!-- Nút thao tác -->
                <div class="crm-form-actions">
                    <button type="submit" class="crm-btn-primary" id="btn-save-assignment">
                        <svg class="crm-btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>
                        Lưu Phân Quyền & Nhóm Kinh Doanh
                    </button>
                    <a href="?id=<%= selectedUser.getId() %>" class="crm-btn-secondary">
                        Hủy Bỏ / Tải Lại
                    </a>
                    <a href="<%= request.getContextPath() %>/dieu-huong" class="crm-btn-link">
                        Xem Menu Điều Hướng Phân Quyền &rarr;
                    </a>
                </div>
            </form>
            <% } %>

        </div>
    </main>

    <!-- JavaScript Navigation & Form Validation -->
    <script src="<%= request.getContextPath() %>/assets/js/navigation.js"></script>
    <script src="<%= request.getContextPath() %>/assets/js/phan-quyen.js"></script>
</body>
</html>
