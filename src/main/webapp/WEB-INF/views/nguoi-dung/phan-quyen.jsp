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

    // Xác định người dùng đăng nhập hiện tại từ session
    NguoiDung nguoiDungHienTai = (NguoiDung) session.getAttribute("nguoiDung");
    int currentUserId = (nguoiDungHienTai != null) ? nguoiDungHienTai.getId() : 1;
    boolean isEditingSelf = (selectedUser != null && selectedUser.getId() == currentUserId);
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

            <!-- Breadcrumb Navigation -->
            <nav class="crm-breadcrumb" aria-label="Đường dẫn điều hướng">
                <a href="<%= request.getContextPath() %>/dieu-huong">Trang chủ</a>
                <span class="crm-breadcrumb-separator">/</span>
                <span>Quản trị hệ thống</span>
                <span class="crm-breadcrumb-separator">/</span>
                <span class="crm-breadcrumb-current">Gán vai trò & nhóm kinh doanh</span>
            </nav>

            <!-- Banner Story Info -->
            <div class="crm-story-card">
                <div class="crm-story-header">
                    <span class="crm-pill crm-pill-epic">EP-01 Quản trị truy cập</span>
                    <span class="crm-pill crm-pill-story">S1-09 Phân quyền & Cây tổ chức</span>
                    <span class="crm-pill crm-pill-role">FE: Ngô Trung Kiên</span>
                </div>
                <h1 class="crm-story-title">Gán Vai Trò & Gắn Người Dùng Vào Nhóm Kinh Doanh</h1>
                <p class="crm-story-desc">
                    Quản trị viên thiết lập vai trò và nhóm kinh doanh cho nhân sự để cây tổ chức
                    quyết định đúng phạm vi dữ liệu (toàn bộ, theo nhóm hoặc cá nhân) và quyền truy cập chức năng của từng người.
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
                            <strong>Một người dùng có thể giữ nhiều vai trò cùng lúc</strong>
                            <p>Hỗ trợ gán đồng thời nhiều vai trò theo mô hình N-N linh hoạt.</p>
                        </div>
                    </div>
                    <div class="crm-ac-item pass">
                        <div class="crm-ac-icon">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>
                        </div>
                        <div class="crm-ac-info">
                            <strong>Trưởng nhóm bắt buộc phải được gán một nhóm cụ thể</strong>
                            <p>Ràng buộc trực tiếp trên giao diện và backend ngăn chặn thiếu nhóm.</p>
                        </div>
                    </div>
                    <div class="crm-ac-item pass">
                        <div class="crm-ac-icon">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>
                        </div>
                        <div class="crm-ac-info">
                            <strong>Không thể tự thu hồi vai trò quản trị của chính mình</strong>
                            <p>Tự động phát hiện tài khoản cá nhân và khóa bảo vệ quyền Admin (AC3).</p>
                        </div>
                    </div>
                    <div class="crm-ac-item pass">
                        <div class="crm-ac-icon">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>
                        </div>
                        <div class="crm-ac-info">
                            <strong>Tối ưu trải nghiệm màn hình 360px</strong>
                            <p>Phím bấm >= 44px, không tràn viền ngang, tương tác chạm mượt mà.</p>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Thông báo phản hồi từ Server (Alert Banner) -->
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

            <!-- BƯỚC 1: Bộ chọn người dùng kèm tìm kiếm tức thì -->
            <div class="crm-card">
                <div class="crm-card-header">
                    <h2 class="crm-card-title">
                        <svg class="crm-card-title-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
                        1. Chọn người dùng cần phân quyền & gán nhóm
                    </h2>
                    <span class="crm-badge crm-badge-count" id="user-display-count">
                        Hiển thị: <%= dsNguoiDung != null ? dsNguoiDung.size() : 0 %> / <%= dsNguoiDung != null ? dsNguoiDung.size() : 0 %> người dùng
                    </span>
                </div>

                <!-- Ô tìm kiếm người dùng nhanh -->
                <div class="crm-user-search-bar">
                    <svg class="crm-search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>
                    <input type="text"
                           id="user-search-input"
                           class="crm-search-input"
                           placeholder="Tìm nhanh nhân sự theo họ tên, email hoặc vai trò hiện tại..."
                           aria-label="Tìm kiếm người dùng">
                    <button type="button" id="user-search-clear" class="crm-search-clear" title="Xóa tìm kiếm">&times;</button>
                </div>

                <!-- Grid danh sách nhân sự -->
                <div class="crm-user-selector-grid" id="user-selector-grid">
                    <% if (dsNguoiDung != null) {
                        for (NguoiDung nd : dsNguoiDung) {
                            boolean isCurrent = selectedUser != null && selectedUser.getId() == nd.getId();
                            String roleStr = nd.getChuoiVaiTroHienThi();
                            String teamStr = nd.getTenNhomKinhDoanh();
                    %>
                        <a href="?id=<%= nd.getId() %>"
                           class="crm-user-select-item <%= isCurrent ? "active" : "" %>"
                           data-name="<%= nd.getHoTen() %>"
                           data-email="<%= nd.getEmail() %>"
                           data-roles="<%= roleStr %>">
                            <div class="crm-user-item-avatar"><%= nd.getTenVietTat() %></div>
                            <div class="crm-user-item-info">
                                <div class="crm-user-item-name"><%= nd.getHoTen() %></div>
                                <div class="crm-user-item-email"><%= nd.getEmail() %></div>
                                <div class="crm-user-item-role">
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width: 12px; height: 12px; flex-shrink: 0;"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 14 14"></polyline></svg>
                                    <span><%= roleStr %></span>
                                </div>
                            </div>
                        </a>
                    <%  }
                    } %>

                    <!-- Trạng thái rỗng khi tìm kiếm không có kết quả -->
                    <div class="crm-user-empty-state" id="user-empty-state" style="display: none;">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>
                        <p style="margin-bottom: 4px; font-weight: 600;">Không tìm thấy nhân sự phù hợp với từ khóa</p>
                        <span style="font-size: 12px;">Vui lòng thử tìm kiếm bằng tên hoặc địa chỉ email khác.</span>
                    </div>
                </div>
            </div>

            <!-- BƯỚC 2: Form Gán vai trò & nhóm kinh doanh -->
            <% if (selectedUser != null) { %>
            <form action="<%= request.getContextPath() %>/nguoi-dung/phan-quyen"
                  method="POST"
                  class="crm-form-role-assignment"
                  id="form-assign-role"
                  data-is-self="<%= isEditingSelf %>">
                <input type="hidden" name="nguoiDungId" value="<%= selectedUser.getId() %>">

                <!-- Header người dùng đang chọn -->
                <div class="crm-selected-user-card">
                    <div class="crm-selected-user-left">
                        <div class="crm-selected-user-avatar"><%= selectedUser.getTenVietTat() %></div>
                        <div class="crm-selected-user-info">
                            <div class="crm-selected-user-name">
                                <%= selectedUser.getHoTen() %>
                                <span class="crm-user-id-badge">ID: #<%= selectedUser.getId() %></span>
                            </div>
                            <div class="crm-selected-user-meta">
                                <span class="crm-meta-item">
                                    <svg class="crm-meta-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path><polyline points="22,6 12,13 2,6"></polyline></svg>
                                    <strong><%= selectedUser.getEmail() %></strong>
                                </span>
                                <span class="crm-meta-item">
                                    <svg class="crm-meta-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 14 14"></polyline></svg>
                                    Trạng thái: <strong class="crm-badge-success" style="padding: 2px 8px; font-size: 11px;"><%= selectedUser.getTrangThai() %></strong>
                                </span>
                                <span class="crm-meta-item">
                                    <svg class="crm-meta-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle></svg>
                                    Nhóm hiện tại: <strong><%= selectedUser.getTenNhomKinhDoanh() %></strong>
                                </span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Banner bảo vệ tài khoản quản trị cá nhân (AC 3) -->
                <% if (isEditingSelf && selectedUser.coVaiTro(VaiTroEnum.ADMIN)) { %>
                    <div class="crm-self-protect-banner" role="alert">
                        <svg class="crm-self-protect-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg>
                        <div>
                            <strong>Bảo vệ an toàn hệ thống (AC 3):</strong>
                            Bạn đang chỉnh sửa tài khoản của <strong>chính mình</strong>. Theo quy tắc an ninh, bạn không thể tự thu hồi vai trò <em>Quản trị hệ thống (Admin)</em> để tránh việc hệ thống bị mất quyền quản trị viên duy nhất.
                        </div>
                    </div>
                <% } %>

                <!-- 2. GÁN VAI TRÒ (AC 1 & AC 3) -->
                <div class="crm-card">
                    <div class="crm-card-header">
                        <h2 class="crm-card-title">
                            <svg class="crm-card-title-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg>
                            2. Gán vai trò (Có thể chọn nhiều vai trò cùng lúc)
                        </h2>
                        <span class="crm-badge crm-badge-info" id="selected-role-count">Đã chọn: 0 vai trò</span>
                    </div>

                    <div class="crm-roles-toolbar">
                        <p class="crm-field-hint" style="margin-bottom: 0;">
                            Tích chọn các vai trò mà nhân sự này được phép đảm nhiệm trong quy trình bán hàng:
                        </p>
                        <div class="crm-roles-quick-actions">
                            <button type="button" class="crm-btn-chip" id="btn-reset-form" title="Khôi phục trạng thái khi vừa mở trang">
                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width: 14px; height: 14px; vertical-align: middle; margin-right: 4px;"><polyline points="1 4 1 10 7 10"></polyline><path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"></path></svg>
                                Khôi phục ban đầu
                            </button>
                        </div>
                    </div>

                    <div class="crm-roles-grid">
                        <% if (dsVaiTro != null) {
                            for (VaiTro vt : dsVaiTro) {
                                boolean checked = selectedUser.coVaiTro(vt.getMaVaiTro());
                                boolean isTeamLead = "TEAM_LEAD".equalsIgnoreCase(vt.getMaVaiTro());
                                boolean isAdmin = "ADMIN".equalsIgnoreCase(vt.getMaVaiTro());
                                boolean isProtectedAdmin = isEditingSelf && isAdmin && checked;

                                // Xác định icon box style theo mã vai trò
                                String iconBoxClass = "crm-icon-sales";
                                if (isAdmin) iconBoxClass = "crm-icon-admin";
                                else if ("DIRECTOR".equalsIgnoreCase(vt.getMaVaiTro())) iconBoxClass = "crm-icon-director";
                                else if (isTeamLead) iconBoxClass = "crm-icon-team-lead";
                                else if ("MARKETING".equalsIgnoreCase(vt.getMaVaiTro())) iconBoxClass = "crm-icon-marketing";
                                else if ("CUST_SUCCESS".equalsIgnoreCase(vt.getMaVaiTro())) iconBoxClass = "crm-icon-cskh";
                                else if ("ACCOUNTANT".equalsIgnoreCase(vt.getMaVaiTro())) iconBoxClass = "crm-icon-accountant";
                        %>
                            <label class="crm-role-checkbox-card <%= checked ? "checked" : "" %> <%= isProtectedAdmin ? "protected" : "" %>"
                                   id="role-card-<%= vt.getId() %>"
                                   title="<%= isProtectedAdmin ? "Không thể tự thu hồi vai trò Quản trị của chính mình (AC 3)" : vt.getTenVaiTro() %>">
                                <div class="crm-checkbox-header">
                                    <div class="crm-role-icon-box <%= iconBoxClass %>">
                                        <% if (isAdmin) { %>
                                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg>
                                        <% } else if ("DIRECTOR".equalsIgnoreCase(vt.getMaVaiTro())) { %>
                                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>
                                        <% } else if (isTeamLead) { %>
                                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
                                        <% } else if ("MARKETING".equalsIgnoreCase(vt.getMaVaiTro())) { %>
                                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>
                                        <% } else if ("CUST_SUCCESS".equalsIgnoreCase(vt.getMaVaiTro())) { %>
                                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path></svg>
                                        <% } else if ("ACCOUNTANT".equalsIgnoreCase(vt.getMaVaiTro())) { %>
                                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="4" width="20" height="16" rx="2"></rect><line x1="12" y1="8" x2="12" y2="16"></line><line x1="8" y1="12" x2="16" y2="12"></line></svg>
                                        <% } else { %>
                                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"></polyline><polyline points="17 6 23 6 23 12"></polyline></svg>
                                        <% } %>
                                    </div>
                                    <span class="crm-role-title"><%= vt.getTenVaiTro() %></span>
                                    <div class="crm-checkbox-wrapper">
                                        <input type="checkbox"
                                               name="vaiTroIds"
                                               value="<%= vt.getId() %>"
                                               data-code="<%= vt.getMaVaiTro() %>"
                                               data-name="<%= vt.getTenVaiTro() %>"
                                               class="crm-checkbox-input"
                                               <%= checked ? "checked" : "" %>
                                               id="chk-role-<%= vt.getId() %>">
                                    </div>
                                </div>
                                <div class="crm-role-desc"><%= vt.getMoTa() != null ? vt.getMoTa() : "" %></div>
                                <div class="crm-role-badges">
                                    <span class="crm-role-code-badge"><%= vt.getMaVaiTro() %></span>
                                    <% if (isTeamLead) { %>
                                        <span class="crm-badge-required-team">
                                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width: 10px; height: 10px;"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                                            Cần nhóm KD (AC2)
                                        </span>
                                    <% } %>
                                    <% if (isProtectedAdmin) { %>
                                        <span class="crm-badge-admin-safe">
                                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width: 10px; height: 10px;"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
                                            Khóa an toàn (AC3)
                                        </span>
                                    <% } %>
                                </div>
                            </label>
                        <%  }
                        } %>
                    </div>
                </div>

                <!-- XEM TRƯỚC PHẠM VI DỮ LIỆU THỜI GIAN THỰC (LIVE SCOPE PREVIEW) -->
                <div class="crm-preview-card" id="preview-scope-card">
                    <div class="crm-preview-header">
                        <div class="crm-preview-title">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width: 18px; height: 18px; color: var(--crm-primary);"><circle cx="12" cy="12" r="10"></circle><line x1="2" y1="12" x2="22" y2="12"></line><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path></svg>
                            Xem trước phạm vi dữ liệu & Quyền hạn thực tế:
                        </div>
                        <span class="crm-badge crm-badge-info" style="font-size: 11px;">Mô phỏng thời gian thực</span>
                    </div>

                    <div class="crm-scope-box">
                        <div class="crm-scope-indicator" id="preview-scope-indicator"></div>
                        <div class="crm-scope-text">
                            <strong id="preview-scope-title">Đang xác định...</strong>
                            <span id="preview-scope-desc">Đang tải cấu hình phân quyền...</span>
                        </div>
                    </div>

                    <div style="font-size: 12px; font-weight: 700; color: #475569; margin-bottom: 6px;">Quyền hạn phân hệ được kích hoạt:</div>
                    <div class="crm-preview-permissions" id="preview-perm-list">
                        <!-- Render động qua JavaScript -->
                    </div>
                </div>

                <!-- 3. GẮN NHÓM KINH DOANH (AC 2) -->
                <div class="crm-card crm-team-card" id="card-nhom-kd">
                    <div class="crm-card-header">
                        <h2 class="crm-card-title">
                            <svg class="crm-card-title-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22"></polyline></svg>
                            3. Gắn vào nhóm kinh doanh (Cây tổ chức)
                        </h2>
                        <span class="crm-badge crm-badge-warning" id="team-required-badge" style="display: none;">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width: 12px; height: 12px;"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                            Bắt buộc cho Trưởng nhóm (AC2)
                        </span>
                    </div>

                    <div class="crm-form-group">
                        <label for="select-nhom-kd" class="crm-label">
                            Chọn nhóm kinh doanh trực thuộc:
                            <span class="crm-required-mark" id="label-team-required" style="display: none;">*</span>
                        </label>
                        <div class="crm-select-wrapper">
                            <select name="nhomKinhDoanhId" id="select-nhom-kd" class="crm-select">
                                <option value="">-- Chưa gán nhóm --</option>
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
                            <svg class="crm-select-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"></polyline></svg>
                        </div>

                        <!-- Cảnh báo inline lỗi validation -->
                        <div class="crm-inline-error" id="team-error-message">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width: 14px; height: 14px;"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                            Vui lòng chọn một nhóm kinh doanh cụ thể cho người giữ vai trò Trưởng nhóm (AC2)!
                        </div>

                        <p class="crm-input-helper" id="helper-team-lead">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width: 14px; height: 14px; flex-shrink: 0; margin-top: 2px;"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>
                            <span>Người dùng trực thuộc nhóm kinh doanh để cây tổ chức xác định đúng dữ liệu họ và cấp dưới nhìn thấy.</span>
                        </p>
                    </div>
                </div>

                <!-- Nút thao tác (Touch Target >= 44px) -->
                <div class="crm-form-actions">
                    <button type="submit" class="crm-btn-primary" id="btn-save-assignment">
                        <svg class="crm-btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>
                        Lưu Phân Quyền & Nhóm Kinh Doanh
                    </button>
                    <a href="?id=<%= selectedUser.getId() %>" class="crm-btn-secondary" id="btn-reload-page">
                        <svg class="crm-btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="1 4 1 10 7 10"></polyline><path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"></path></svg>
                        Tải Lại Dữ Liệu
                    </a>
                    <a href="<%= request.getContextPath() %>/dieu-huong" class="crm-btn-link">
                        Xem Menu Điều Hướng Phân Quyền &rarr;
                    </a>
                </div>
            </form>
            <% } %>

        </div>
    </main>

    <!-- Floating Toast Container -->
    <div id="crm-toast-container" class="crm-toast-container"></div>

    <!-- JavaScript Navigation & Form Interactive Logic -->
    <script src="<%= request.getContextPath() %>/assets/js/navigation.js"></script>
    <script src="<%= request.getContextPath() %>/assets/js/phan-quyen.js"></script>
</body>
</html>
