<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="vn.nhom10.crm.dto.NguoiDungDTO" %>
<%@ page import="vn.nhom10.crm.dto.BanGhiNghiepVuDTO" %>
<%@ page import="vn.nhom10.crm.model.PhamViDuLieu" %>
<%@ page import="vn.nhom10.crm.model.VaiTroEnum" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Phân Quyền Theo Dữ Liệu Sở Hữu - CRM Bán Hàng (S1-05)</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/phan-quyen/data-scope.css">
</head>
<body class="crm-body">

    <!-- Thanh điều hướng và Sidebar chuẩn hệ thống S1-06 -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content" id="crm-main-content">
        <div class="scope-container">

    <%
        NguoiDungDTO currentUser = (NguoiDungDTO) request.getAttribute("currentUser");
        PhamViDuLieu phamViHienTai = (PhamViDuLieu) request.getAttribute("phamViHienTai");
        String loaiHienTai = (String) request.getAttribute("loaiHienTai");
        String tuKhoaHienTai = (String) request.getAttribute("tuKhoaHienTai");
        List<BanGhiNghiepVuDTO> danhSachBanGhi = (List<BanGhiNghiepVuDTO>) request.getAttribute("danhSachBanGhi");
        int tongSoBanGhi = (Integer) request.getAttribute("tongSoBanGhi");
        int tongSoBanGhiGoc = (Integer) request.getAttribute("tongSoBanGhiGoc");
        String thongBaoCanhBao = (String) request.getAttribute("thongBaoCanhBao");
        String thongBaoThanhCong = (String) request.getAttribute("thongBaoThanhCong");
    %>

    <!-- 2. HEADER THÔNG TIN NGƯỜI DÙNG & QUYỀN HẠN -->
    <div class="page-header">
        <div class="page-title-group">
            <h1>
                <span class="material-symbols-outlined" style="color: var(--primary-color); vertical-align: -3px; margin-right: 4px;" aria-hidden="true">security</span>Phân Quyền Theo Dữ Liệu Sở Hữu (Data Scope)
            </h1>
            <p>Story S1-05: Quản lý và lọc dữ liệu đa cấp cho Khách hàng, Cơ hội, Báo giá và Hoạt động</p>
        </div>

        <div class="user-identity-badge">
            <div class="user-avatar">
                <%= currentUser != null && currentUser.getHoTen() != null ? currentUser.getHoTen().substring(0, 1) : "U" %>
            </div>
            <div>
                <div class="user-meta-name"><%= currentUser != null ? currentUser.getHoTen() : "Khách" %></div>
                <div class="user-meta-role">
                    <span><%= currentUser != null && currentUser.getVaiTro() != null ? currentUser.getVaiTro().getTenHienThi() : "" %></span>
                    <span>•</span>
                    <span><%= currentUser != null && currentUser.getTenNhom() != null ? currentUser.getTenNhom() : "Không thuộc nhóm" %></span>
                </div>
            </div>
        </div>
    </div>

    <!-- CÁC THÔNG BÁO HỆ THỐNG -->
    <% if (thongBaoCanhBao != null && !thongBaoCanhBao.trim().isEmpty()) { %>
        <div class="alert-box alert-warning">
            <span class="material-symbols-outlined" style="flex-shrink:0; margin-top: 2px;" aria-hidden="true">warning</span>
            <div><%= thongBaoCanhBao %></div>
        </div>
    <% } %>

    <% if (thongBaoThanhCong != null && !thongBaoThanhCong.trim().isEmpty()) { %>
        <div class="alert-box alert-success">
            <span class="material-symbols-outlined" style="flex-shrink:0; margin-top: 2px;" aria-hidden="true">check_circle</span>
            <div><%= thongBaoThanhCong %></div>
        </div>
    <% } %>

    <!-- 3. BỘ CHỌN PHẠM VI DỮ LIỆU SỞ HỮU (DATA SCOPE SELECTOR - AC1) -->
    <div class="scope-selector-card">
        <%
            boolean canChonCaNhan = currentUser != null && currentUser.coQuyenChonPhamVi(PhamViDuLieu.CA_NHAN);
            boolean canChonNhom = currentUser != null && currentUser.coQuyenChonPhamVi(PhamViDuLieu.NHOM);
            boolean canChonToanBo = currentUser != null && currentUser.coQuyenChonPhamVi(PhamViDuLieu.TOAN_BO);

            boolean isCurrentCaNhan = phamViHienTai == PhamViDuLieu.CA_NHAN;
            boolean isCurrentNhom = phamViHienTai == PhamViDuLieu.NHOM;
            boolean isCurrentToanBo = phamViHienTai == PhamViDuLieu.TOAN_BO;
        %>

        <!-- TAB: CỦA TÔI -->
        <a href="${pageContext.request.contextPath}/phan-quyen-du-lieu?phamVi=CA_NHAN&loai=<%= loaiHienTai %>&tuKhoa=<%= tuKhoaHienTai %>"
           class="scope-tab scope-canhan <%= isCurrentCaNhan ? "active" : "" %>">
            <div>
                <div class="scope-tab-title">
                    <span class="material-symbols-outlined" style="font-size: 18px; vertical-align: -3px;" aria-hidden="true">person</span>
                    <span>Của tôi</span>
                </div>
                <div class="scope-tab-desc">Chỉ dữ liệu do chính bạn phụ trách</div>
            </div>
            <span class="scope-tag tag-canhan">Cá nhân</span>
        </a>

        <!-- TAB: NHÓM CỦA TÔI -->
        <a href="<%= canChonNhom ? request.getContextPath() + "/phan-quyen-du-lieu?phamVi=NHOM&loai=" + loaiHienTai + "&tuKhoa=" + tuKhoaHienTai : "#" %>"
           class="scope-tab scope-nhom <%= isCurrentNhom ? "active" : "" %> <%= !canChonNhom ? "disabled" : "" %>">
            <div>
                <div class="scope-tab-title">
                    <span class="material-symbols-outlined" style="font-size: 18px; vertical-align: -3px;" aria-hidden="true">groups</span>
                    <span>Nhóm của tôi</span>
                    <% if (!canChonNhom) { %>
                        <span class="material-symbols-outlined icon-xs" style="color: var(--gray-500); vertical-align: -2px;" aria-label="Bị khóa">lock</span>
                    <% } %>
                </div>
                <div class="scope-tab-desc">Toàn bộ dữ liệu của <%= currentUser != null ? currentUser.getTenNhom() : "nhóm" %></div>
            </div>
            <span class="scope-tag <%= canChonNhom ? "tag-nhom" : "tag-locked" %>">
                <%= canChonNhom ? "Nhóm" : "Bị khóa" %>
            </span>
        </a>

        <!-- TAB: TẤT CẢ (TOÀN BỘ) -->
        <a href="<%= canChonToanBo ? request.getContextPath() + "/phan-quyen-du-lieu?phamVi=TOAN_BO&loai=" + loaiHienTai + "&tuKhoa=" + tuKhoaHienTai : "#" %>"
           class="scope-tab scope-toanbo <%= isCurrentToanBo ? "active" : "" %> <%= !canChonToanBo ? "disabled" : "" %>">
            <div>
                <div class="scope-tab-title">
                    <span class="material-symbols-outlined" style="font-size: 18px; vertical-align: -3px;" aria-hidden="true">public</span>
                    <span>Tất cả</span>
                    <% if (!canChonToanBo) { %>
                        <span class="material-symbols-outlined icon-xs" style="color: var(--gray-500); vertical-align: -2px;" aria-label="Bị khóa">lock</span>
                    <% } %>
                </div>
                <div class="scope-tab-desc">Toàn bộ khách hàng & giao dịch toàn công ty</div>
            </div>
            <span class="scope-tag <%= canChonToanBo ? "tag-toanbo" : "tag-locked" %>">
                <%= canChonToanBo ? "Toàn bộ" : "Bị khóa" %>
            </span>
        </a>
    </div>

    <!-- 4. THANH BỘ LỌC NGHIỆP VỤ, TÌM KIẾM VÀ XUẤT EXCEL (AC2) -->
    <div class="toolbar-card">
        <div class="filter-pills">
            <a href="${pageContext.request.contextPath}/phan-quyen-du-lieu?phamVi=<%= phamViHienTai.getMa() %>&loai=ALL&tuKhoa=<%= tuKhoaHienTai %>"
               class="pill-link <%= "ALL".equals(loaiHienTai) ? "active" : "" %>">Tất cả nghiệp vụ</a>
            <a href="${pageContext.request.contextPath}/phan-quyen-du-lieu?phamVi=<%= phamViHienTai.getMa() %>&loai=KHACH_HANG&tuKhoa=<%= tuKhoaHienTai %>"
               class="pill-link <%= "KHACH_HANG".equals(loaiHienTai) ? "active" : "" %>">Khách hàng</a>
            <a href="${pageContext.request.contextPath}/phan-quyen-du-lieu?phamVi=<%= phamViHienTai.getMa() %>&loai=CO_HOI&tuKhoa=<%= tuKhoaHienTai %>"
               class="pill-link <%= "CO_HOI".equals(loaiHienTai) ? "active" : "" %>">Cơ hội</a>
            <a href="${pageContext.request.contextPath}/phan-quyen-du-lieu?phamVi=<%= phamViHienTai.getMa() %>&loai=BAO_GIA&tuKhoa=<%= tuKhoaHienTai %>"
               class="pill-link <%= "BAO_GIA".equals(loaiHienTai) ? "active" : "" %>">Báo giá</a>
            <a href="${pageContext.request.contextPath}/phan-quyen-du-lieu?phamVi=<%= phamViHienTai.getMa() %>&loai=HOAT_DONG&tuKhoa=<%= tuKhoaHienTai %>"
               class="pill-link <%= "HOAT_DONG".equals(loaiHienTai) ? "active" : "" %>">Hoạt động</a>
        </div>

        <div class="toolbar-actions">
            <!-- Ô TÌM KIẾM TỰ ĐỘNG LỌC THEO PHẠM VI (AC2) -->
            <form action="${pageContext.request.contextPath}/phan-quyen-du-lieu" method="get" class="search-box">
                <input type="hidden" name="phamVi" value="<%= phamViHienTai.getMa() %>">
                <input type="hidden" name="loai" value="<%= loaiHienTai %>">
                <span class="material-symbols-outlined search-icon icon-sm" aria-hidden="true">search</span>
                <input type="text" id="searchBoxInput" name="tuKhoa" value="<%= tuKhoaHienTai %>"
                       class="search-input" placeholder="Tìm theo tên, mã, người phụ trách...">
            </form>

            <!-- NÚT XUẤT EXCEL TỰ ĐỘNG LỌC THEO PHẠM VI (AC2) -->
            <a href="${pageContext.request.contextPath}/phan-quyen-du-lieu?phamVi=<%= phamViHienTai.getMa() %>&loai=<%= loaiHienTai %>&tuKhoa=<%= tuKhoaHienTai %>&xuatExcel=true"
               class="btn-export" title="Xuất dữ liệu theo đúng phạm vi hiện tại ra file Excel (.xlsx)">
                <span class="material-symbols-outlined icon-sm" aria-hidden="true">table_view</span>
                <span>Xuất Excel</span>
            </a>
        </div>
    </div>

    <!-- 5. BẢNG DỮ LIỆU HIỂN THỊ (DATA TABLE) -->
    <div class="table-card">
        <div class="table-header-meta">
            <div class="table-count-info">
                Đang hiển thị: <span id="visibleCountDisplay" style="color: var(--primary-color);"><%= tongSoBanGhi %></span> bản ghi
                (Phạm vi áp dụng: <strong><%= phamViHienTai.getTenHienThi() %></strong> / Tổng hệ thống: <%= tongSoBanGhiGoc %>)
            </div>
            <div>
                <% if (tuKhoaHienTai != null && !tuKhoaHienTai.trim().isEmpty()) { %>
                    <span style="font-size: 13px; color: var(--gray-500);">
                        Từ khóa: "<strong><%= tuKhoaHienTai %></strong>"
                        <a href="${pageContext.request.contextPath}/phan-quyen-du-lieu?phamVi=<%= phamViHienTai.getMa() %>&loai=<%= loaiHienTai %>"
                           style="color: var(--danger-color); text-decoration: none; margin-left: 6px; display: inline-flex; align-items: center; gap: 2px;">
                            <span class="material-symbols-outlined icon-xs" aria-hidden="true">close</span> Xóa lọc
                        </a>
                    </span>
                <% } %>
            </div>
        </div>

        <div class="table-responsive">
            <table class="crm-table">
                <thead>
                    <tr>
                        <th>Mã</th>
                        <th>Nghiệp vụ</th>
                        <th>Tiêu đề / Đối tượng</th>
                        <th>Người phụ trách</th>
                        <th>Nhóm kinh doanh</th>
                        <th>Giá trị / Chi tiết</th>
                        <th>Trạng thái</th>
                        <th>Ngày tạo</th>
                        <th style="text-align: right;">Thao tác</th>
                    </tr>
                </thead>
                <tbody id="dataTableBody">
                    <% if (danhSachBanGhi == null || danhSachBanGhi.isEmpty()) { %>
                        <tr class="empty-row">
                            <td colspan="9">
                                <div class="empty-state">
                                    <div class="empty-icon">
                                        <span class="material-symbols-outlined" style="font-size: 48px; color: var(--gray-400);" aria-hidden="true">inbox</span>
                                    </div>
                                    <div class="empty-title">Không tìm thấy dữ liệu trong phạm vi này</div>
                                    <div class="empty-desc">
                                        Không có bản ghi nào phù hợp với phạm vi "<strong><%= phamViHienTai.getTenHienThi() %></strong>"
                                        hoặc bộ lọc bạn đã chọn.
                                    </div>
                                </div>
                            </td>
                        </tr>
                    <% } else { %>
                        <% for (BanGhiNghiepVuDTO bg : danhSachBanGhi) {
                            boolean isMine = currentUser != null && bg.getNguoiPhuTrachId().equals(currentUser.getId());
                        %>
                            <tr>
                                <td style="font-weight: 600; color: var(--gray-700);"><%= bg.getMaBanGhi() %></td>
                                <td>
                                    <span class="badge-module badge-<%= bg.getLoaiNghiepVu().getMa().toLowerCase().replace("_", "-") %>">
                                        <%= bg.getLoaiNghiepVu().getTenHienThi() %>
                                    </span>
                                </td>
                                <td>
                                    <div style="font-weight: 600; color: var(--gray-900);"><%= bg.getTieuDe() %></div>
                                    <div style="font-size: 12px; color: var(--gray-500);"><%= bg.getMoTaChiTiet() %></div>
                                </td>
                                <td>
                                    <div class="badge-owner <%= isMine ? "badge-owner-mine" : "" %>">
                                        <%= bg.getTenNguoiPhuTrach() %> <%= isMine ? "(Bạn)" : "" %>
                                    </div>
                                </td>
                                <td>
                                    <span class="badge-team"><%= bg.getTenNhom() %></span>
                                </td>
                                <td style="font-weight: 500;"><%= bg.getGiaTri() %></td>
                                <td>
                                    <span style="font-size: 12px; padding: 2px 8px; border-radius: 4px; background: var(--gray-100); color: var(--gray-700);">
                                        <%= bg.getTrangThai() %>
                                    </span>
                                </td>
                                <td style="font-size: 13px; color: var(--gray-500);"><%= bg.getNgayTao() %></td>
                                <td style="text-align: right;">
                                    <a href="${pageContext.request.contextPath}/chi-tiet-ban-ghi?id=<%= bg.getId() %>"
                                       class="action-link" title="Xem chi tiết bản ghi" style="display: inline-flex; align-items: center; gap: 4px;">
                                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">visibility</span>
                                        <span>Xem chi tiết</span>
                                    </a>
                                </td>
                            </tr>
                        <% } %>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>

    <!-- 6. KHUNG KIỂM TRA TRUY CẬP NGOÀI PHẠM VI (AC3 & AC4 TEST PANEL) -->
    <div style="margin-top: 32px; background: #ffffff; border-radius: var(--radius-md); padding: 20px 24px; border: 1px dashed var(--gray-300);">
        <h3 style="font-size: 15px; font-weight: 700; color: var(--gray-900); display: flex; align-items: center; gap: 8px;">
            <span class="material-symbols-outlined icon-sm" style="color: var(--primary-color);" aria-hidden="true">science</span>
            <span>Kiểm thử nhanh hành vi truy cập ngoài phạm vi (Acceptance Criteria 3 & 4)</span>
        </h3>
        <p style="font-size: 13px; color: var(--gray-500); margin: 6px 0 14px;">
            Khi bạn đang đăng nhập với tư cách <strong><%= currentUser != null ? currentUser.getHoTen() : "" %></strong> (<%= currentUser != null ? currentUser.getVaiTro().getTenHienThi() : "" %>),
            hãy thử bấm vào các liên kết trực tiếp dưới đây để kiểm chứng hệ thống chặn truy cập ngoài phạm vi:
        </p>

        <div style="display: flex; flex-wrap: wrap; gap: 10px;">
            <a href="${pageContext.request.contextPath}/chi-tiet-ban-ghi?id=1"
               class="simulator-btn" style="color: var(--gray-700); background: var(--gray-100); border-color: var(--gray-300);">
                Thử xem FPT (Của Nhân viên A)
            </a>
            <a href="${pageContext.request.contextPath}/chi-tiet-ban-ghi?id=5"
               class="simulator-btn" style="color: var(--gray-700); background: var(--gray-100); border-color: var(--gray-300);">
                Thử xem Viettel (Của Nhân viên B - Cùng nhóm Bắc)
            </a>
            <a href="${pageContext.request.contextPath}/chi-tiet-ban-ghi?id=9"
               class="simulator-btn" style="color: var(--gray-700); background: var(--gray-100); border-color: var(--gray-300);">
                Thử xem VNG (Của Nhân viên C - Khác nhóm Nam)
            </a>
        </div>
    </div>
    </main>

<script src="${pageContext.request.contextPath}/assets/js/navigation.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/phan-quyen/data-scope.js"></script>
</body>
</html>
