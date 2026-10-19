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
                    <span class="material-symbols-outlined" style="font-size: 20px; color: #16a34a;" aria-hidden="true">check_circle</span>
                    Tiêu chuẩn nghiệm thu được giao (Acceptance Criteria)
                </h2>
                <div class="crm-ac-grid">
                    <div class="crm-ac-item pass">
                        <div class="crm-ac-icon">
                            <span class="material-symbols-outlined" aria-hidden="true">check</span>
                        </div>
                        <div class="crm-ac-info">
                            <strong>Một người dùng có thể giữ nhiều vai trò cùng lúc</strong>
                            <p>Hỗ trợ gán đồng thời nhiều vai trò theo mô hình N-N linh hoạt.</p>
                        </div>
                    </div>
                    <div class="crm-ac-item pass">
                        <div class="crm-ac-icon">
                            <span class="material-symbols-outlined" aria-hidden="true">check</span>
                        </div>
                        <div class="crm-ac-info">
                            <strong>Trưởng nhóm bắt buộc phải được gán một nhóm cụ thể</strong>
                            <p>Ràng buộc trực tiếp trên giao diện và backend ngăn chặn thiếu nhóm.</p>
                        </div>
                    </div>
                    <div class="crm-ac-item pass">
                        <div class="crm-ac-icon">
                            <span class="material-symbols-outlined" aria-hidden="true">check</span>
                        </div>
                        <div class="crm-ac-info">
                            <strong>Không thể tự thu hồi vai trò quản trị của chính mình</strong>
                            <p>Tự động phát hiện tài khoản cá nhân và khóa bảo vệ quyền Admin (AC3).</p>
                        </div>
                    </div>
                    <div class="crm-ac-item pass">
                        <div class="crm-ac-icon">
                            <span class="material-symbols-outlined" aria-hidden="true">check</span>
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
                            <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                        <% } else { %>
                            <span class="material-symbols-outlined" aria-hidden="true">error</span>
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
                        <span class="material-symbols-outlined crm-card-title-icon" aria-hidden="true">person_search</span>
                        1. Chọn người dùng cần phân quyền & gán nhóm
                    </h2>
                    <span class="crm-badge crm-badge-count" id="user-display-count">
                        Hiển thị: <%= dsNguoiDung != null ? dsNguoiDung.size() : 0 %> / <%= dsNguoiDung != null ? dsNguoiDung.size() : 0 %> người dùng
                    </span>
                </div>

                <!-- Ô tìm kiếm người dùng nhanh -->
                <div class="crm-user-search-bar">
                    <span class="material-symbols-outlined crm-search-icon" aria-hidden="true">search</span>
                    <input type="text"
                           id="user-search-input"
                           class="crm-search-input"
                           placeholder="Tìm nhanh nhân sự theo họ tên, email hoặc vai trò hiện tại..."
                           aria-label="Tìm kiếm người dùng">
                    <button type="button" id="user-search-clear" class="crm-search-clear" title="Xóa tìm kiếm" aria-label="Xóa tìm kiếm"><span class="material-symbols-outlined" aria-hidden="true">close</span></button>
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
                                    <span class="material-symbols-outlined" style="font-size: 13px; vertical-align: -2px;" aria-hidden="true">badge</span>
                                    <span><%= roleStr %></span>
                                </div>
                            </div>
                        </a>
                    <%  }
                    } %>

                    <!-- Trạng thái rỗng khi tìm kiếm không có kết quả -->
                    <div class="crm-user-empty-state" id="user-empty-state" style="display: none;">
                        <span class="material-symbols-outlined" style="font-size: 32px; color: var(--crm-text-muted); margin-bottom: 8px;" aria-hidden="true">search_off</span>
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
                                    <span class="material-symbols-outlined crm-meta-icon" aria-hidden="true">mail</span>
                                    <strong><%= selectedUser.getEmail() %></strong>
                                </span>
                                <span class="crm-meta-item">
                                    <span class="material-symbols-outlined crm-meta-icon" aria-hidden="true">verified_user</span>
                                    Trạng thái: <strong class="crm-badge-success" style="padding: 2px 8px; font-size: 11px;"><%= selectedUser.getTrangThai() %></strong>
                                </span>
                                <span class="crm-meta-item">
                                    <span class="material-symbols-outlined crm-meta-icon" aria-hidden="true">groups</span>
                                    Nhóm hiện tại: <strong><%= selectedUser.getTenNhomKinhDoanh() %></strong>
                                </span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Banner bảo vệ tài khoản quản trị cá nhân (AC 3) -->
                <% if (isEditingSelf && selectedUser.coVaiTro(VaiTroEnum.ADMIN)) { %>
                    <div class="crm-self-protect-banner" role="alert">
                        <span class="material-symbols-outlined crm-self-protect-icon" aria-hidden="true">shield</span>
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
                            <span class="material-symbols-outlined crm-card-title-icon" aria-hidden="true">admin_panel_settings</span>
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
                                <span class="material-symbols-outlined" style="font-size: 15px; vertical-align: middle; margin-right: 4px;" aria-hidden="true">restart_alt</span>
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
                                            <span class="material-symbols-outlined" aria-hidden="true">admin_panel_settings</span>
                                        <% } else if ("DIRECTOR".equalsIgnoreCase(vt.getMaVaiTro())) { %>
                                            <span class="material-symbols-outlined" aria-hidden="true">military_tech</span>
                                        <% } else if (isTeamLead) { %>
                                            <span class="material-symbols-outlined" aria-hidden="true">group</span>
                                        <% } else if ("MARKETING".equalsIgnoreCase(vt.getMaVaiTro())) { %>
                                            <span class="material-symbols-outlined" aria-hidden="true">campaign</span>
                                        <% } else if ("CUST_SUCCESS".equalsIgnoreCase(vt.getMaVaiTro())) { %>
                                            <span class="material-symbols-outlined" aria-hidden="true">support_agent</span>
                                        <% } else if ("ACCOUNTANT".equalsIgnoreCase(vt.getMaVaiTro())) { %>
                                            <span class="material-symbols-outlined" aria-hidden="true">receipt_long</span>
                                        <% } else { %>
                                            <span class="material-symbols-outlined" aria-hidden="true">trending_up</span>
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
                                            <span class="material-symbols-outlined" style="font-size: 12px; vertical-align: -2px;" aria-hidden="true">warning</span>
                                            Cần nhóm KD (AC2)
                                        </span>
                                    <% } %>
                                    <% if (isProtectedAdmin) { %>
                                        <span class="crm-badge-admin-safe">
                                            <span class="material-symbols-outlined" style="font-size: 12px; vertical-align: -2px;" aria-hidden="true">lock</span>
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
                            <span class="material-symbols-outlined" style="font-size: 18px; color: var(--crm-primary); vertical-align: -3px;" aria-hidden="true">visibility</span>
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
                            <span class="material-symbols-outlined crm-card-title-icon" aria-hidden="true">account_tree</span>
                            3. Gắn vào nhóm kinh doanh (Cây tổ chức)
                        </h2>
                        <span class="crm-badge crm-badge-warning" id="team-required-badge" style="display: none;">
                            <span class="material-symbols-outlined" style="font-size: 13px; vertical-align: -2px;" aria-hidden="true">warning</span>
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
                            <span class="material-symbols-outlined crm-select-arrow" aria-hidden="true">expand_more</span>
                        </div>

                        <!-- Cảnh báo inline lỗi validation -->
                        <div class="crm-inline-error" id="team-error-message">
                            <span class="material-symbols-outlined" style="font-size: 14px; vertical-align: -2px;" aria-hidden="true">error</span>
                            Vui lòng chọn một nhóm kinh doanh cụ thể cho người giữ vai trò Trưởng nhóm (AC2)!
                        </div>

                        <p class="crm-input-helper" id="helper-team-lead">
                            <span class="material-symbols-outlined" style="font-size: 14px; flex-shrink: 0; margin-top: 2px;" aria-hidden="true">info</span>
                            <span>Người dùng trực thuộc nhóm kinh doanh để cây tổ chức xác định đúng dữ liệu họ và cấp dưới nhìn thấy.</span>
                        </p>
                    </div>
                </div>

                <!-- Nút thao tác (Touch Target >= 44px) -->
                <div class="crm-form-actions">
                    <button type="submit" class="crm-btn-primary" id="btn-save-assignment">
                        <span class="material-symbols-outlined crm-btn-icon" aria-hidden="true">save</span>
                        Lưu Phân Quyền & Nhóm Kinh Doanh
                    </button>
                    <a href="?id=<%= selectedUser.getId() %>" class="crm-btn-secondary" id="btn-reload-page">
                        <span class="material-symbols-outlined crm-btn-icon" aria-hidden="true">refresh</span>
                        Tải Lại Dữ Liệu
                    </a>
                    <a href="<%= request.getContextPath() %>/dieu-huong" class="crm-btn-link">
                        Xem Menu Điều Hướng Phân Quyền <span class="material-symbols-outlined" style="font-size: 16px; vertical-align: -3px;" aria-hidden="true">arrow_forward</span>
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
