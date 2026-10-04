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
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color: var(--primary-color); vertical-align: -3px; margin-right: 4px;" aria-hidden="true">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                </svg>Phân Quyền Theo Dữ Liệu Sở Hữu (Data Scope)
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
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="flex-shrink:0; margin-top: 2px;" aria-hidden="true">
                <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            <div><%= thongBaoCanhBao %></div>
        </div>
    <% } %>

    <% if (thongBaoThanhCong != null && !thongBaoThanhCong.trim().isEmpty()) { %>
        <div class="alert-box alert-success">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="flex-shrink:0; margin-top: 2px;" aria-hidden="true">
                <polyline points="20 6 9 17 4 12"/>
            </svg>
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
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
                    </svg>
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
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                    </svg>
                    <span>Nhóm của tôi</span>
                    <% if (!canChonNhom) { %>
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color: var(--gray-500);" aria-label="Bị khóa">
                            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                        </svg>
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
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                        <circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
                    </svg>
                    <span>Tất cả</span>
                    <% if (!canChonToanBo) { %>
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color: var(--gray-500);" aria-label="Bị khóa">
                            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                        </svg>
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
                <svg class="search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
                </svg>
                <input type="text" id="searchBoxInput" name="tuKhoa" value="<%= tuKhoaHienTai %>"
                       class="search-input" placeholder="Tìm theo tên, mã, người phụ trách...">
            </form>

            <!-- NÚT XUẤT EXCEL TỰ ĐỘNG LỌC THEO PHẠM VI (AC2) -->
            <a href="${pageContext.request.contextPath}/phan-quyen-du-lieu?phamVi=<%= phamViHienTai.getMa() %>&loai=<%= loaiHienTai %>&tuKhoa=<%= tuKhoaHienTai %>&xuatExcel=true"
               class="btn-export" title="Xuất dữ liệu theo đúng phạm vi hiện tại ra file Excel (.xlsx)">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/>
                </svg>
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
                           style="color: var(--danger-color); text-decoration: none; margin-left: 6px;">✕ Xóa lọc</a>
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
                                        <svg width="44" height="44" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" style="color: var(--gray-400);">
                                            <path d="M22 12h-6l-2 3h-4l-2-3H2v7a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-7z"/>
                                            <path d="M5.45 5.11L2 12v7a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-7l-3.45-6.89A2 2 0 0 0 16.76 4H7.24a2 2 0 0 0-1.79 1.11z"/>
                                        </svg>
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
                                       class="action-link" title="Xem chi tiết bản ghi">
                                        Xem chi tiết
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
            Kiểm thử nhanh hành vi truy cập ngoài phạm vi (Acceptance Criteria 3 & 4)
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
