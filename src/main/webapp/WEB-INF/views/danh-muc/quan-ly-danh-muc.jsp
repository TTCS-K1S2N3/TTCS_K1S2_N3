<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="vn.nhom10.crm.model.LoaiDanhMuc" %>
<%@ page import="vn.nhom10.crm.dto.MucDanhMucDTO" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Khai Báo Danh Mục Dùng Chung Bán Hàng - CRM (Story S2-07)</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/danh-muc/sales-category.css">
</head>
<body>

<div class="category-container">

    <%
        LoaiDanhMuc loaiHienTai = (LoaiDanhMuc) request.getAttribute("loaiHienTai");
        LoaiDanhMuc[] danhSachLoai = (LoaiDanhMuc[]) request.getAttribute("danhSachLoaiDanhMuc");
        List<MucDanhMucDTO> danhSachMuc = (List<MucDanhMucDTO>) request.getAttribute("danhSachMuc");
        Long tongSoMuc = (Long) request.getAttribute("tongSoMuc");
        Long soMucKichHoat = (Long) request.getAttribute("soMucKichHoat");
        Long tongSoThamChieu = (Long) request.getAttribute("tongSoThamChieu");
        String tuKhoaHienTai = (String) request.getAttribute("tuKhoaHienTai");
        String thongBaoThanhCong = (String) request.getAttribute("thongBaoThanhCong");
        String thongBaoLoi = (String) request.getAttribute("thongBaoLoi");
    %>

    <!-- 1. HEADER CHÍNH -->
    <div class="category-header">
        <div class="header-title-group">
            <h1>⚙️ Khai Báo Danh Mục Bán Hàng Dùng Chung</h1>
            <p>Story S2-07: Chuẩn hóa dữ liệu nguồn lead, ngành nghề, quy mô và hoạt động cho toàn khối kinh doanh</p>
        </div>

        <div class="director-badge">
            <div class="director-avatar">👑</div>
            <div>
                <div class="director-name">Bàn Thị Linh</div>
                <div class="director-role">Giám đốc kinh doanh (Director)</div>
            </div>
        </div>
    </div>

    <!-- CÁC THÔNG BÁO HỆ THỐNG -->
    <% if (thongBaoThanhCong != null && !thongBaoThanhCong.trim().isEmpty()) { %>
        <div class="alert-box alert-success">
            <span>✓</span>
            <div><%= thongBaoThanhCong %></div>
        </div>
    <% } %>

    <% if (thongBaoLoi != null && !thongBaoLoi.trim().isEmpty()) { %>
        <div class="alert-box alert-danger">
            <span>⚠️</span>
            <div><%= thongBaoLoi %></div>
        </div>
    <% } %>

    <!-- 2. THỐNG KÊ NHANH (METRICS ROW) -->
    <div class="metrics-row">
        <div class="metric-card">
            <div>
                <div class="metric-label">Tổng số mục trong nhóm</div>
                <div class="metric-value"><%= tongSoMuc != null ? tongSoMuc : 0 %></div>
            </div>
            <div class="metric-icon icon-blue">📂</div>
        </div>

        <div class="metric-card">
            <div>
                <div class="metric-label">Đang áp dụng (Kích hoạt)</div>
                <div class="metric-value" style="color: var(--success-color);"><%= soMucKichHoat != null ? soMucKichHoat : 0 %></div>
            </div>
            <div class="metric-icon icon-green">✅</div>
        </div>

        <div class="metric-card">
            <div>
                <div class="metric-label">Tổng bản ghi đang tham chiếu</div>
                <div class="metric-value" style="color: var(--purple-color);"><%= tongSoThamChieu != null ? tongSoThamChieu : 0 %></div>
            </div>
            <div class="metric-icon icon-purple">🔗</div>
        </div>
    </div>

    <!-- 3. TABS CHỌN 4 LOẠI DANH MỤC (AC1) -->
    <div class="category-tabs-nav">
        <% for (LoaiDanhMuc loai : danhSachLoai) {
            boolean isActive = loai == loaiHienTai;
        %>
            <a href="${pageContext.request.contextPath}/danh-muc-ban-hang?loai=<%= loai.getMa() %>"
               class="category-tab-btn <%= isActive ? "active" : "" %>">
                <div class="tab-icon-box"><%= loai.getIcon() %></div>
                <div>
                    <div class="tab-text-title"><%= loai.getTenHienThi() %></div>
                    <div class="tab-text-sub"><%= loai.getMa() %></div>
                </div>
            </a>
        <% } %>
    </div>

    <!-- 4. THANH CÔNG CỤ TÌM KIẾM VÀ NÚT THÊM MỚI -->
    <div class="table-toolbar">
        <form action="${pageContext.request.contextPath}/danh-muc-ban-hang" method="get" class="toolbar-search">
            <input type="hidden" name="loai" value="<%= loaiHienTai.getMa() %>">
            <span class="search-icon">🔍</span>
            <input type="text" id="categorySearchInput" name="tuKhoa" value="<%= tuKhoaHienTai %>"
                   class="search-input" placeholder="Tìm theo tên hoặc mã trong nhóm <%= loaiHienTai.getTenHienThi() %>...">
        </form>

        <button type="button" id="btnOpenAddModal" class="btn-primary-add">
            <span>➕</span>
            <span>Thêm mục <%= loaiHienTai.getTenHienThi() %> mới</span>
        </button>
    </div>

    <!-- 5. BẢNG DỮ LIỆU DANH MỤC (AC1, AC2, AC3) -->
    <div class="table-card">
        <div class="table-meta-bar">
            <div>
                Nhóm đang quản trị: <strong><%= loaiHienTai.getTenHienThi() %></strong> —
                Hiển thị: <span id="visibleItemsCount" style="font-weight: 700; color: var(--primary-color);"><%= danhSachMuc != null ? danhSachMuc.size() : 0 %></span> mục
            </div>
            <div>
                <span style="font-size: 12px; color: var(--gray-500);">💡 Dùng nút mũi tên ⬆️ ⬇️ để thay đổi thứ tự hiển thị</span>
            </div>
        </div>

        <div class="table-responsive">
            <table class="category-table">
                <thead>
                    <tr>
                        <th style="width: 90px;">Thứ tự</th>
                        <th style="width: 130px;">Mã định danh</th>
                        <th>Tên mục danh mục</th>
                        <th>Mô tả chi tiết</th>
                        <th style="width: 140px;">Trạng thái</th>
                        <th style="width: 150px;">Bản ghi tham chiếu</th>
                        <th style="width: 110px;">Ngày tạo</th>
                        <th style="text-align: right; width: 140px;">Thao tác</th>
                    </tr>
                </thead>
                <tbody id="categoryTableBody">
                    <% if (danhSachMuc == null || danhSachMuc.isEmpty()) { %>
                        <tr class="empty-row">
                            <td colspan="8" style="text-align: center; padding: 48px 20px;">
                                <div style="font-size: 36px; margin-bottom: 8px;">📭</div>
                                <div style="font-weight: 600; color: var(--gray-700);">Chưa có mục danh mục nào trong nhóm này</div>
                                <div style="font-size: 13px; color: var(--gray-500); margin-top: 4px;">Bấm nút "Thêm mục mới" phía trên để tạo mục đầu tiên.</div>
                            </td>
                        </tr>
                    <% } else {
                        int total = danhSachMuc.size();
                        for (int i = 0; i < total; i++) {
                            MucDanhMucDTO item = danhSachMuc.get(i);
                            boolean isFirst = (i == 0);
                            boolean isLast = (i == total - 1);
                            boolean hasUsage = item.getSoBanGhiDangSuDung() > 0;
                    %>
                        <tr>
                            <!-- THỨ TỰ & NÚT SẮP XẾP (AC3) -->
                            <td>
                                <div class="order-cell">
                                    <span class="order-badge"><%= item.getThuTuHienThi() %></span>
                                    <div class="order-btn-group">
                                        <form action="${pageContext.request.contextPath}/danh-muc-ban-hang" method="post" style="display:inline;">
                                            <input type="hidden" name="action" value="doi-thu-tu">
                                            <input type="hidden" name="loaiDanhMuc" value="<%= loaiHienTai.getMa() %>">
                                            <input type="hidden" name="id" value="<%= item.getId() %>">
                                            <input type="hidden" name="huong" value="len">
                                            <button type="submit" class="btn-order-move" title="Di chuyển lên trên" <%= isFirst ? "disabled" : "" %>>▲</button>
                                        </form>
                                        <form action="${pageContext.request.contextPath}/danh-muc-ban-hang" method="post" style="display:inline;">
                                            <input type="hidden" name="action" value="doi-thu-tu">
                                            <input type="hidden" name="loaiDanhMuc" value="<%= loaiHienTai.getMa() %>">
                                            <input type="hidden" name="id" value="<%= item.getId() %>">
                                            <input type="hidden" name="huong" value="xuong">
                                            <button type="submit" class="btn-order-move" title="Di chuyển xuống dưới" <%= isLast ? "disabled" : "" %>>▼</button>
                                        </form>
                                    </div>
                                </div>
                            </td>

                            <!-- MÃ ĐỊNH DANH -->
                            <td>
                                <span class="code-tag"><%= item.getMaMuc() %></span>
                            </td>

                            <!-- TÊN MỤC -->
                            <td>
                                <div style="font-weight: 600; color: var(--gray-900);"><%= item.getTenMuc() %></div>
                            </td>

                            <!-- MÔ TẢ -->
                            <td style="color: var(--gray-600); font-size: 13px;">
                                <%= item.getMoTa() != null && !item.getMoTa().isEmpty() ? item.getMoTa() : "<em style='color:var(--gray-400);'>Chưa có mô tả</em>" %>
                            </td>

                            <!-- TRẠNG THÁI KÍCH HOẠT -->
                            <td>
                                <form action="${pageContext.request.contextPath}/danh-muc-ban-hang" method="post" style="display:inline;">
                                    <input type="hidden" name="action" value="chuyen-trang-thai">
                                    <input type="hidden" name="loaiDanhMuc" value="<%= loaiHienTai.getMa() %>">
                                    <input type="hidden" name="id" value="<%= item.getId() %>">
                                    <button type="submit" style="background:none; border:none; cursor:pointer;" title="Bấm để bật/tắt áp dụng">
                                        <% if (item.isKichHoat()) { %>
                                            <span class="status-badge status-active">● Áp dụng</span>
                                        <% } else { %>
                                            <span class="status-badge status-inactive">○ Tạm ngưng</span>
                                        <% } %>
                                    </button>
                                </form>
                            </td>

                            <!-- SỐ BẢN GHI THAM CHIẾU (AC2) -->
                            <td>
                                <% if (hasUsage) { %>
                                    <span class="usage-pill usage-in-use" title="Đang có <%= item.getSoBanGhiDangSuDung() %> bản ghi nghiệp vụ sử dụng mục này">
                                        🔗 <%= item.getSoBanGhiDangSuDung() %> bản ghi
                                    </span>
                                <% } else { %>
                                    <span class="usage-pill usage-zero" title="Chưa có dữ liệu nào liên kết">
                                        0 (Chưa dùng)
                                    </span>
                                <% } %>
                            </td>

                            <!-- NGÀY TẠO -->
                            <td style="font-size: 13px; color: var(--gray-500);">
                                <%= item.getNgayTao() %>
                            </td>

                            <!-- THAO TÁC SỬA / XÓA (AC2) -->
                            <td>
                                <div class="action-buttons-cell">
                                    <button type="button" class="btn-action-edit btn-edit-item"
                                            data-id="<%= item.getId() %>"
                                            data-ma="<%= item.getMaMuc() %>"
                                            data-ten="<%= item.getTenMuc() %>"
                                            data-mota="<%= item.getMoTa() != null ? item.getMoTa() : "" %>"
                                            data-kichhoat="<%= item.isKichHoat() %>">
                                        Sửa
                                    </button>

                                    <button type="button"
                                            class="btn-action-delete btn-delete-item"
                                            data-id="<%= item.getId() %>"
                                            data-ten="<%= item.getTenMuc() %>"
                                            data-usage="<%= item.getSoBanGhiDangSuDung() %>"
                                            <%= hasUsage ? "disabled title='Không thể xóa vì đang được tham chiếu bởi " + item.getSoBanGhiDangSuDung() + " bản ghi nghiệp vụ'" : "title='Xóa mục này'" %>>
                                        Xóa
                                    </button>
                                </div>
                            </td>
                        </tr>
                    <% } } %>
                </tbody>
            </table>
        </div>
    </div>

</div>

<!-- 6. MODAL THÊM MỚI MỤC DANH MỤC (AC1) -->
<div id="modalAddCategory" class="modal-overlay">
    <div class="modal-dialog">
        <form action="${pageContext.request.contextPath}/danh-muc-ban-hang" method="post">
            <input type="hidden" name="action" value="them">
            <input type="hidden" name="loaiDanhMuc" value="<%= loaiHienTai.getMa() %>">

            <div class="modal-header">
                <div class="modal-title">➕ Thêm mục mới vào: <%= loaiHienTai.getTenHienThi() %></div>
                <button type="button" id="btnCloseAddModal" class="modal-close-btn">&times;</button>
            </div>

            <div class="modal-body">
                <div class="form-group">
                    <label for="addMaMuc">Mã định danh <span style="color:var(--danger-color);">*</span></label>
                    <input type="text" id="addMaMuc" name="maMuc" required class="form-control"
                           placeholder="Ví dụ: CNTT, BAN_LE, WEB_FORM..." style="text-transform: uppercase;">
                    <small style="color:var(--gray-500); font-size: 11px;">Mã viết hoa, không dấu, không trùng trong nhóm.</small>
                </div>

                <div class="form-group">
                    <label for="addTenMuc">Tên mục hiển thị <span style="color:var(--danger-color);">*</span></label>
                    <input type="text" id="addTenMuc" name="tenMuc" required class="form-control"
                           placeholder="Ví dụ: Công nghệ thông tin & Viễn thông">
                </div>

                <div class="form-group">
                    <label for="addMoTa">Mô tả / Ghi chú</label>
                    <textarea id="addMoTa" name="moTa" rows="3" class="form-control"
                              placeholder="Diễn giải phạm vi áp dụng của danh mục này..."></textarea>
                </div>

                <div class="form-group">
                    <label class="form-checkbox-label">
                        <input type="checkbox" name="kichHoat" value="true" checked>
                        <span>Áp dụng ngay sau khi tạo</span>
                    </label>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" id="btnCancelAddModal" class="btn-secondary">Hủy</button>
                <button type="submit" class="btn-primary-add">Lưu mục mới</button>
            </div>
        </form>
    </div>
</div>

<!-- 7. MODAL CHỈNH SỬA MỤC DANH MỤC (AC1) -->
<div id="modalEditCategory" class="modal-overlay">
    <div class="modal-dialog">
        <form action="${pageContext.request.contextPath}/danh-muc-ban-hang" method="post">
            <input type="hidden" name="action" value="sua">
            <input type="hidden" name="loaiDanhMuc" value="<%= loaiHienTai.getMa() %>">
            <input type="hidden" id="editId" name="id" value="">

            <div class="modal-header">
                <div class="modal-title">✏️ Chỉnh sửa mục danh mục</div>
                <button type="button" id="btnCloseEditModal" class="modal-close-btn">&times;</button>
            </div>

            <div class="modal-body">
                <div class="form-group">
                    <label for="editMaMuc">Mã định danh (Không đổi)</label>
                    <input type="text" id="editMaMuc" name="maMuc" readonly class="form-control"
                           style="background: var(--gray-100); color: var(--gray-500); cursor: not-allowed;">
                </div>

                <div class="form-group">
                    <label for="editTenMuc">Tên mục hiển thị <span style="color:var(--danger-color);">*</span></label>
                    <input type="text" id="editTenMuc" name="tenMuc" required class="form-control">
                </div>

                <div class="form-group">
                    <label for="editMoTa">Mô tả / Ghi chú</label>
                    <textarea id="editMoTa" name="moTa" rows="3" class="form-control"></textarea>
                </div>

                <div class="form-group">
                    <label class="form-checkbox-label">
                        <input type="checkbox" id="editKichHoat" name="kichHoat" value="true">
                        <span>Đang áp dụng</span>
                    </label>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" id="btnCancelEditModal" class="btn-secondary">Hủy</button>
                <button type="submit" class="btn-primary-add">Cập nhật thay đổi</button>
            </div>
        </form>
    </div>
</div>

<!-- 8. MODAL XÁC NHẬN XÓA (AC2) -->
<div id="modalDeleteCategory" class="modal-overlay">
    <div class="modal-dialog">
        <form action="${pageContext.request.contextPath}/danh-muc-ban-hang" method="post">
            <input type="hidden" name="action" value="xoa">
            <input type="hidden" name="loaiDanhMuc" value="<%= loaiHienTai.getMa() %>">
            <input type="hidden" id="deleteId" name="id" value="">

            <div class="modal-header" style="background: var(--danger-light); color: var(--danger-color);">
                <div class="modal-title" style="color: var(--danger-color);">🗑️ Xác nhận xóa mục danh mục</div>
                <button type="button" id="btnCloseDeleteModal" class="modal-close-btn">&times;</button>
            </div>

            <div class="modal-body">
                <p style="font-size: 14px; color: var(--gray-700);">
                    Bạn có chắc chắn muốn xóa mục danh mục <strong id="deleteTenMucDisplay" style="color: var(--gray-900);"></strong> không?
                </p>
                <div style="margin-top: 12px; background: var(--gray-50); padding: 12px; border-radius: var(--radius-sm); font-size: 12px; color: var(--gray-600);">
                    ⚠️ Lưu ý theo chính sách nghiệp vụ (AC2): Hệ thống chỉ cho phép xóa những mục <strong>chưa có bất kỳ bản ghi nào tham chiếu</strong>.
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" id="btnCancelDeleteModal" class="btn-secondary">Hủy</button>
                <button type="submit" style="background: var(--danger-color); color: #fff; border:none; padding: 8px 16px; border-radius: var(--radius-sm); font-weight: 600; cursor: pointer;">
                    Xác nhận xóa
                </button>
            </div>
        </form>
    </div>
</div>

<script src="${pageContext.request.contextPath}/assets/js/danh-muc/sales-category.js"></script>
</body>
</html>
