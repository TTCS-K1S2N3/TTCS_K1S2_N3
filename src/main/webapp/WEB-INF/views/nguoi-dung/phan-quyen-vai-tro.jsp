<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="vn.nhom10.crm.model.NguoiDung" %>
<%@ page import="vn.nhom10.crm.model.VaiTro" %>
<%@ page import="vn.nhom10.crm.model.VaiTroModule" %>
<%@ page import="vn.nhom10.crm.model.MucQuyen" %>
<%@ page import="vn.nhom10.crm.model.PhamViDuLieu" %>
<%@ page import="vn.nhom10.crm.service.RolePermissionCeilingPolicy" %>
<%@ page import="vn.nhom10.crm.service.RolePermissionCeilingPolicy.CeilingRule" %>
<%@ page import="java.util.List" %>
<%
    List<VaiTro> dsVaiTro = (List<VaiTro>) request.getAttribute("dsVaiTro");
    VaiTro selectedRole = (VaiTro) request.getAttribute("selectedRole");
    List<VaiTroModule> matrixRows = (List<VaiTroModule>) request.getAttribute("matrixRows");
    Boolean isReadOnly = (Boolean) request.getAttribute("isReadOnly");
    Boolean isAdmin = (Boolean) request.getAttribute("isAdmin");
    Boolean isDirector = (Boolean) request.getAttribute("isDirector");
    Boolean isRoleAdmin = (Boolean) request.getAttribute("isRoleAdmin");
    String thongBaoThanhCong = (String) request.getAttribute("thongBaoThanhCong");
    String thongBaoLoi = (String) request.getAttribute("thongBaoLoi");

    if (isReadOnly == null) isReadOnly = false;
    if (isAdmin == null) isAdmin = false;
    if (isDirector == null) isDirector = false;
    if (isRoleAdmin == null) isRoleAdmin = false;

    PhamViDuLieu maxScope = selectedRole != null ? selectedRole.getPhamViToiDa() : PhamViDuLieu.CA_NHAN;
    int maxScopeWeight = 1;
    if (maxScope == PhamViDuLieu.NHOM) maxScopeWeight = 2;
    else if (maxScope == PhamViDuLieu.TOAN_BO) maxScopeWeight = 3;
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Vai Trò & Phân Quyền | CRM Bán Hàng</title>
    <!-- Google Fonts & Navigation Styles -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/navigation.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/phan-quyen.css">
    <style>
        .pqv-container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 24px 16px;
        }
        .pqv-header {
            margin-bottom: 24px;
        }
        .pqv-title {
            font-size: 24px;
            font-weight: 700;
            color: #1e293b;
            margin: 0 0 8px 0;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .pqv-subtitle {
            font-size: 14px;
            color: #64748b;
            margin: 0;
        }
        /* Role Selector Tabs */
        .pqv-role-tabs {
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
            margin-bottom: 20px;
            background: #f8fafc;
            padding: 8px;
            border-radius: 12px;
            border: 1px solid #e2e8f0;
        }
        .pqv-role-tab {
            padding: 8px 16px;
            border-radius: 8px;
            text-decoration: none;
            font-weight: 600;
            font-size: 13px;
            color: #475569;
            background: transparent;
            transition: all 0.2s ease;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            border: 1px solid transparent;
        }
        .pqv-role-tab:hover {
            color: #2563eb;
            background: #eff6ff;
        }
        .pqv-role-tab.active {
            color: #ffffff;
            background: #2563eb;
            border-color: #2563eb;
            box-shadow: 0 2px 4px rgba(37, 99, 235, 0.2);
        }
        /* Card Panel */
        .pqv-card {
            background: #ffffff;
            border-radius: 14px;
            border: 1px solid #e2e8f0;
            box-shadow: 0 1px 3px rgba(0,0,0,0.05);
            padding: 24px;
            margin-bottom: 24px;
        }
        .pqv-card-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 12px;
            margin-bottom: 20px;
            padding-bottom: 16px;
            border-bottom: 1px solid #f1f5f9;
        }
        .pqv-role-info {
            display: flex;
            align-items: center;
            gap: 12px;
        }
        .pqv-role-badge {
            display: inline-flex;
            align-items: center;
            gap: 4px;
            padding: 4px 10px;
            border-radius: 6px;
            font-size: 12px;
            font-weight: 700;
            background: #e0e7ff;
            color: #3730a3;
        }
        .pqv-scope-badge {
            display: inline-flex;
            align-items: center;
            gap: 4px;
            padding: 4px 10px;
            border-radius: 6px;
            font-size: 12px;
            font-weight: 600;
            background: #f1f5f9;
            color: #475569;
        }
        /* Matrix Table */
        .pqv-table-wrapper {
            width: 100%;
            overflow-x: auto;
            -webkit-overflow-scrolling: touch;
            border-radius: 10px;
            border: 1px solid #e2e8f0;
            margin-bottom: 20px;
        }
        .pqv-table {
            width: 100%;
            border-collapse: collapse;
            text-align: left;
            min-width: 720px;
        }
        .pqv-table th {
            background: #f8fafc;
            color: #475569;
            font-size: 12px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.04em;
            padding: 12px 16px;
            border-bottom: 1px solid #e2e8f0;
        }
        .pqv-table td {
            padding: 14px 16px;
            border-bottom: 1px solid #f1f5f9;
            font-size: 14px;
            color: #1e293b;
            vertical-align: middle;
        }
        .pqv-table tr:hover td {
            background: #f8fafc;
        }
        .pqv-checkbox-wrap {
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 44px;
            min-width: 44px;
        }
        .pqv-checkbox {
            width: 20px;
            height: 20px;
            cursor: pointer;
            accent-color: #2563eb;
        }
        .pqv-checkbox:disabled {
            cursor: not-allowed;
            opacity: 0.7;
        }
        .pqv-scope-select {
            padding: 8px 12px;
            border-radius: 8px;
            border: 1px solid #cbd5e1;
            font-size: 13px;
            color: #1e293b;
            background: #ffffff;
            min-width: 150px;
            min-height: 40px;
        }
        .pqv-scope-select:disabled {
            background: #f8fafc;
            color: #94a3b8;
            cursor: not-allowed;
        }
        /* Alerts & Notices */
        .pqv-alert {
            padding: 12px 16px;
            border-radius: 8px;
            font-size: 14px;
            margin-bottom: 20px;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .pqv-alert-success {
            background: #ecfdf5;
            color: #065f46;
            border: 1px solid #a7f3d0;
        }
        .pqv-alert-error {
            background: #fef2f2;
            color: #991b1b;
            border: 1px solid #fecaca;
        }
        .pqv-alert-info {
            background: #eff6ff;
            color: #1e40af;
            border: 1px solid #bfdbfe;
        }
        .pqv-note {
            font-size: 13px;
            color: #64748b;
            background: #f8fafc;
            padding: 12px 16px;
            border-radius: 8px;
            border-left: 4px solid #94a3b8;
            margin-top: 16px;
        }
        /* Form Footer Actions */
        .pqv-footer {
            display: flex;
            justify-content: flex-end;
            gap: 12px;
            margin-top: 24px;
        }
        .pqv-btn-save {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 10px 24px;
            font-size: 14px;
            font-weight: 600;
            border-radius: 8px;
            background: #2563eb;
            color: #ffffff;
            border: none;
            cursor: pointer;
            min-height: 44px;
            box-shadow: 0 2px 4px rgba(37,99,235,0.25);
            transition: background 0.2s ease;
        }
        .pqv-btn-save:hover {
            background: #1d4ed8;
        }
    </style>
</head>
<body class="crm-body">

    <!-- Thanh điều hướng và Sidebar chuẩn hệ thống -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <!-- Nội dung chính -->
    <main class="crm-main-content" id="crm-main-content">
        <div class="pqv-container">

            <!-- Breadcrumb Navigation -->
            <nav class="crm-breadcrumb" aria-label="Đường dẫn điều hướng">
                <a href="<%= request.getContextPath() %>/dieu-huong">Trang chủ</a>
                <span class="crm-breadcrumb-separator">/</span>
                <a href="<%= request.getContextPath() %>/nguoi-dung">Người dùng & Nhật ký</a>
                <span class="crm-breadcrumb-separator">/</span>
                <span class="crm-breadcrumb-current">Vai trò & Phân quyền</span>
            </nav>

            <!-- Page Header -->
            <div class="pqv-header">
                <h1 class="pqv-title">
                    <span class="material-symbols-outlined" style="font-size: 32px; color: #2563eb;" aria-hidden="true">admin_panel_settings</span>
                    Vai trò & Phân quyền
                </h1>
                <p class="pqv-subtitle">
                    Quản lý ma trận phân quyền truy cập chức năng và phạm vi dữ liệu theo 7 vai trò chuẩn của hệ thống CRM.
                </p>
            </div>

            <!-- Notifications -->
            <% if (thongBaoThanhCong != null && !thongBaoThanhCong.isBlank()) { %>
            <div class="pqv-alert pqv-alert-success">
                <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                <span><%= thongBaoThanhCong %></span>
            </div>
            <% } %>

            <% if (thongBaoLoi != null && !thongBaoLoi.isBlank()) { %>
            <div class="pqv-alert pqv-alert-error">
                <span class="material-symbols-outlined" aria-hidden="true">error</span>
                <span><%= thongBaoLoi %></span>
            </div>
            <% } %>

            <% if (isDirector) { %>
            <div class="pqv-alert pqv-alert-info">
                <span class="material-symbols-outlined" aria-hidden="true">info</span>
                <span><strong>Chế độ xem quyền (Read-only):</strong> Bạn đang truy cập với vai trò Giám đốc kinh doanh. Chỉ Quản trị hệ thống (Admin) mới có quyền chỉnh sửa ma trận quyền.</span>
            </div>
            <% } else if (isRoleAdmin) { %>
            <div class="pqv-alert pqv-alert-info">
                <span class="material-symbols-outlined" aria-hidden="true">lock</span>
                <span><strong>Vai trò Quản trị hệ thống (Admin):</strong> Luôn có toàn quyền (FULL) trên tất cả các module và phạm vi Toàn hệ thống (TOAN_BO), được bảo vệ an toàn và không thể chỉnh sửa.</span>
            </div>
            <% } %>

            <!-- 7 Roles Selector Tabs -->
            <div class="pqv-role-tabs">
                <%
                    if (dsVaiTro != null) {
                        for (VaiTro vt : dsVaiTro) {
                            boolean isActive = selectedRole != null && vt.getMaVaiTro().equalsIgnoreCase(selectedRole.getMaVaiTro());
                %>
                <a href="<%= request.getContextPath() %>/nguoi-dung/phan-quyen-vai-tro?vaiTro=<%= vt.getMaVaiTro() %>"
                   class="pqv-role-tab <%= isActive ? "active" : "" %>">
                    <span class="material-symbols-outlined" style="font-size: 18px;" aria-hidden="true">badge</span>
                    <%= vt.getTenVaiTro() %>
                </a>
                <%
                        }
                    }
                %>
            </div>

            <!-- Matrix Form & Table -->
            <div class="pqv-card">
                <div class="pqv-card-header">
                    <div class="pqv-role-info">
                        <span class="pqv-role-badge">
                            <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">verified_user</span>
                            <%= selectedRole != null ? selectedRole.getTenVaiTro() : "Chưa chọn vai trò" %>
                        </span>
                        <span class="pqv-scope-badge">
                            Giới hạn tối đa: <strong><%= maxScope != null ? maxScope.getTenHienThi() : "Cá nhân" %></strong>
                        </span>
                    </div>
                    <div>
                        <span style="font-size: 13px; color: #64748b;">
                            10 Module chuẩn CSDL
                        </span>
                    </div>
                </div>

                <form id="form-phan-quyen" method="POST" action="<%= request.getContextPath() %>/nguoi-dung/phan-quyen-vai-tro">
                    <input type="hidden" name="maVaiTro" value="<%= selectedRole != null ? selectedRole.getMaVaiTro() : "" %>">

                    <div class="pqv-table-wrapper">
                        <table class="pqv-table">
                            <thead>
                                <tr>
                                    <th style="width: 250px;">Tên Module</th>
                                    <th style="text-align: center; width: 80px;">Xem</th>
                                    <th style="text-align: center; width: 80px;">Tạo</th>
                                    <th style="text-align: center; width: 80px;">Sửa</th>
                                    <th style="text-align: center; width: 80px;">Xóa</th>
                                    <th style="width: 220px;">Phạm vi dữ liệu</th>
                                </tr>
                            </thead>
                            <tbody>
                                <%
                                    String currentRoleCode = selectedRole != null ? selectedRole.getMaVaiTro() : "";
                                    if (matrixRows != null && !matrixRows.isEmpty()) {
                                        for (VaiTroModule row : matrixRows) {
                                            int modId = row.getModuleId();
                                            String maMod = row.getMaModule();
                                            MucQuyen mq = row.getMucQuyen();
                                            PhamViDuLieu pv = row.getPhamViDuLieu();
                                            boolean chkXem = mq.isXem();
                                            boolean chkTao = mq.isTao();
                                            boolean chkSua = mq.isSua();
                                            boolean chkXoa = mq.isXoa();

                                            CeilingRule ceiling = RolePermissionCeilingPolicy.getInstance().layTran(currentRoleCode, maMod);
                                            MucQuyen maxLvl = ceiling.getMaxLevel();
                                            PhamViDuLieu maxScp = ceiling.getMaxScope();
                                            boolean isVuotTran = RolePermissionCeilingPolicy.getInstance().coGrantVuotTran(currentRoleCode, maMod, mq, pv);

                                            boolean disableView = isReadOnly || !ceiling.isLevelAllowed(MucQuyen.READ);
                                            boolean disableCreate = isReadOnly || !ceiling.isLevelAllowed(MucQuyen.WRITE);
                                            boolean disableUpdate = isReadOnly || !ceiling.isLevelAllowed(MucQuyen.WRITE);
                                            boolean disableDelete = isReadOnly || !ceiling.isLevelAllowed(MucQuyen.FULL);
                                            boolean disableScope = isReadOnly || maxLvl == MucQuyen.NONE || maxScp == null || mq == MucQuyen.NONE;
                                            boolean scopeAllowedForMod = maxLvl != MucQuyen.NONE && maxScp != null;
                                %>
                                <tr data-mod-id="<%= modId %>">
                                    <td>
                                        <div style="font-weight: 600; color: #0f172a;"><%= row.getTenModule() %></div>
                                        <div style="font-size: 12px; color: #64748b;"><%= row.getMoTaModule() != null ? row.getMoTaModule() : "" %></div>
                                        <% if (isVuotTran) { %>
                                        <div style="margin-top: 4px;">
                                            <span style="display: inline-block; font-size: 11px; padding: 2px 6px; background-color: #fee2e2; color: #b91c1c; border-radius: 4px; font-weight: 600;">
                                                ⚠️ Vượt trần Sheet 2 (Đã ép trần an toàn ở runtime)
                                            </span>
                                        </div>
                                        <% } %>
                                        <!-- Hidden input lưu trữ mức quyền tính toán (NONE/READ/WRITE/FULL) -->
                                        <input type="hidden" name="mucQuyen_<%= modId %>" id="level_<%= modId %>" value="<%= mq.getMa() %>">
                                    </td>
                                    <td style="text-align: center;">
                                        <div class="pqv-checkbox-wrap">
                                            <input type="checkbox"
                                                   class="pqv-checkbox cb-view"
                                                   data-mod-id="<%= modId %>"
                                                   id="view_<%= modId %>"
                                                   <%= chkXem ? "checked" : "" %>
                                                   <%= disableView ? "disabled" : "" %>>
                                        </div>
                                    </td>
                                    <td style="text-align: center;">
                                        <div class="pqv-checkbox-wrap">
                                            <input type="checkbox"
                                                   class="pqv-checkbox cb-create"
                                                   data-mod-id="<%= modId %>"
                                                   id="create_<%= modId %>"
                                                   <%= chkTao ? "checked" : "" %>
                                                   <%= disableCreate ? "disabled" : "" %>>
                                        </div>
                                    </td>
                                    <td style="text-align: center;">
                                        <div class="pqv-checkbox-wrap">
                                            <input type="checkbox"
                                                   class="pqv-checkbox cb-update"
                                                   data-mod-id="<%= modId %>"
                                                   id="update_<%= modId %>"
                                                   <%= chkSua ? "checked" : "" %>
                                                   <%= disableUpdate ? "disabled" : "" %>>
                                        </div>
                                    </td>
                                    <td style="text-align: center;">
                                        <div class="pqv-checkbox-wrap">
                                            <input type="checkbox"
                                                   class="pqv-checkbox cb-delete"
                                                   data-mod-id="<%= modId %>"
                                                   id="delete_<%= modId %>"
                                                   <%= chkXoa ? "checked" : "" %>
                                                   <%= disableDelete ? "disabled" : "" %>>
                                        </div>
                                    </td>
                                    <td>
                                        <select name="phamVi_<%= modId %>"
                                                id="scope_<%= modId %>"
                                                class="pqv-scope-select"
                                                data-ceiling-scope-allowed="<%= scopeAllowedForMod %>"
                                                <%= disableScope ? "disabled" : "" %>>
                                            <option value="" <%= pv == null ? "selected" : "" %>>Không áp dụng</option>
                                            <option value="CA_NHAN" <%= pv == PhamViDuLieu.CA_NHAN ? "selected" : "" %>
                                                    <%= !ceiling.isScopeAllowed(PhamViDuLieu.CA_NHAN) ? "disabled" : "" %>>
                                                Của tôi (Cá nhân)
                                            </option>
                                            <option value="NHOM" <%= pv == PhamViDuLieu.NHOM ? "selected" : "" %>
                                                    <%= !ceiling.isScopeAllowed(PhamViDuLieu.NHOM) ? "disabled" : "" %>>
                                                Nhóm của tôi
                                            </option>
                                            <option value="TOAN_BO" <%= pv == PhamViDuLieu.TOAN_BO ? "selected" : "" %>
                                                    <%= !ceiling.isScopeAllowed(PhamViDuLieu.TOAN_BO) ? "disabled" : "" %>>
                                                Toàn hệ thống
                                            </option>
                                        </select>
                                    </td>
                                </tr>
                                <%
                                        }
                                    }
                                %>
                            </tbody>
                        </table>
                    </div>

                    <!-- Note nghiệp vụ đặc thù -->
                    <div class="pqv-note">
                        <span class="material-symbols-outlined" style="font-size: 16px; vertical-align: -2px;" aria-hidden="true">info</span>
                        Các quyền nghiệp vụ đặc thù được kiểm soát bởi chính sách nghiệp vụ của từng chức năng. Các ô bị vô hiệu hóa do vai trò không được cấp vượt quá trần quy định của Sheet 2.
                    </div>

                    <!-- Action buttons -->
                    <% if (isAdmin && !isRoleAdmin) { %>
                    <div class="pqv-footer">
                        <button type="submit" id="btn-save-permission" class="pqv-btn-save">
                            <span class="material-symbols-outlined" aria-hidden="true">save</span>
                            Lưu cấu hình phân quyền
                        </button>
                    </div>
                    <% } %>
                </form>
            </div>

        </div>
    </main>

    <!-- JavaScript xử lý quy tắc cấp quyền phân cấp (Hierarchical Checkboxes) -->
    <script>
    document.addEventListener("DOMContentLoaded", function () {
        var isReadOnly = <%= isReadOnly %>;
        if (isReadOnly) return;

        function updateRowHierarchy(modId) {
            var cbView = document.getElementById("view_" + modId);
            var cbCreate = document.getElementById("create_" + modId);
            var cbUpdate = document.getElementById("update_" + modId);
            var cbDelete = document.getElementById("delete_" + modId);
            var hiddenLevel = document.getElementById("level_" + modId);
            var scopeSelect = document.getElementById("scope_" + modId);

            var isXoa = cbDelete && cbDelete.checked && !cbDelete.disabled;
            var isTao = cbCreate && cbCreate.checked && !cbCreate.disabled;
            var isSua = cbUpdate && cbUpdate.checked && !cbUpdate.disabled;
            var isXem = cbView && cbView.checked && !cbView.disabled;

            var level = "NONE";
            if (isXoa) {
                level = "FULL";
            } else if (isTao || isSua) {
                level = "WRITE";
            } else if (isXem) {
                level = "READ";
            }
            if (hiddenLevel) {
                hiddenLevel.value = level;
            }

            if (scopeSelect && scopeSelect.getAttribute("data-ceiling-scope-allowed") === "true") {
                if (level === "NONE") {
                    scopeSelect.value = "";
                    scopeSelect.disabled = true;
                } else if (!isReadOnly) {
                    scopeSelect.disabled = false;
                }
            }
        }

        // Bắt sự kiện thay đổi trên các checkbox
        document.querySelectorAll("tr[data-mod-id]").forEach(function (row) {
            var modId = row.getAttribute("data-mod-id");
            var cbView = document.getElementById("view_" + modId);
            var cbCreate = document.getElementById("create_" + modId);
            var cbUpdate = document.getElementById("update_" + modId);
            var cbDelete = document.getElementById("delete_" + modId);

            // Bật Delete => FULL (bật Xem, Tạo, Sửa, Xóa)
            if (cbDelete) {
                cbDelete.addEventListener("change", function () {
                    if (cbDelete.checked) {
                        if (cbView && !cbView.disabled) cbView.checked = true;
                        if (cbCreate && !cbCreate.disabled) cbCreate.checked = true;
                        if (cbUpdate && !cbUpdate.disabled) cbUpdate.checked = true;
                    }
                    updateRowHierarchy(modId);
                });
            }

            // Bật Create hoặc Update => WRITE (bật Xem, Tạo, Sửa)
            if (cbCreate) {
                cbCreate.addEventListener("change", function () {
                    if (cbCreate.checked) {
                        if (cbView && !cbView.disabled) cbView.checked = true;
                        if (cbUpdate && !cbUpdate.disabled) cbUpdate.checked = true;
                    } else {
                        // Bỏ Create từ WRITE => READ (bỏ cả Sửa và Xóa)
                        if (cbUpdate && !cbUpdate.disabled) cbUpdate.checked = false;
                        if (cbDelete && !cbDelete.disabled) cbDelete.checked = false;
                    }
                    updateRowHierarchy(modId);
                });
            }

            if (cbUpdate) {
                cbUpdate.addEventListener("change", function () {
                    if (cbUpdate.checked) {
                        if (cbView && !cbView.disabled) cbView.checked = true;
                        if (cbCreate && !cbCreate.disabled) cbCreate.checked = true;
                    } else {
                        if (cbCreate && !cbCreate.disabled) cbCreate.checked = false;
                        if (cbDelete && !cbDelete.disabled) cbDelete.checked = false;
                    }
                    updateRowHierarchy(modId);
                });
            }

            // Bỏ View => NONE (bỏ toàn bộ Create, Update, Delete)
            if (cbView) {
                cbView.addEventListener("change", function () {
                    if (!cbView.checked) {
                        if (cbCreate && !cbCreate.disabled) cbCreate.checked = false;
                        if (cbUpdate && !cbUpdate.disabled) cbUpdate.checked = false;
                        if (cbDelete && !cbDelete.disabled) cbDelete.checked = false;
                    }
                    updateRowHierarchy(modId);
                });
            }
        });
    });
    </script>
</body>
</html>
