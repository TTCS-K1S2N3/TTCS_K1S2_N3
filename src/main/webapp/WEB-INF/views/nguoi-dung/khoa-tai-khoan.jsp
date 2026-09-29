<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="vn.nhom10.crm.dto.ThongTinBanGiaoDTO" %>
<%@ page import="vn.nhom10.crm.dto.KetQuaKhoaVaBanGiaoDTO" %>
<%@ page import="vn.nhom10.crm.model.NguoiDung" %>
<%@ page import="vn.nhom10.crm.model.NhatKyBanGiao" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Khoá tài khoản & Bàn giao dữ liệu | CRM Bán Hàng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/khoa-tai-khoan.css">
</head>
<body class="crm-body">

    <%-- Thanh điều hướng chuẩn hệ thống (AC: Menu phân quyền & hiển thị tên, vai trò, nhóm) --%>
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content">
        <div class="handover-container">

            <%-- Tiêu đề trang --%>
            <div class="handover-header">
                <h1>
                    <span>Khoá tài khoản & Bàn giao dữ liệu</span>
                    <span class="badge-lock">Quản trị hệ thống</span>
                </h1>
                <p class="handover-subtitle">
                    Thực hiện bàn giao toàn bộ khách hàng và cơ hội khi nhân viên nghỉ việc, đồng thời khoá tài khoản và thu hồi các phiên đăng nhập.
                </p>
            </div>

            <%-- Thông báo kết quả thực hiện nếu có --%>
            <%
                KetQuaKhoaVaBanGiaoDTO ketQua = (KetQuaKhoaVaBanGiaoDTO) request.getAttribute("ketQua");
                if (ketQua != null) {
                    if (ketQua.isThanhCong()) {
            %>
                <div class="alert alert-success">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                        <polyline points="22 4 12 14.01 9 11.01"></polyline>
                    </svg>
                    <div>
                        <strong>Thành công!</strong> <%= ketQua.getThongBao() %>
                    </div>
                </div>
            <%      } else { %>
                <div class="alert alert-danger">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="12" cy="12" r="10"></circle>
                        <line x1="12" y1="8" x2="12" y2="12"></line>
                        <line x1="12" y1="16" x2="12.01" y2="16"></line>
                    </svg>
                    <div>
                        <strong>Lỗi:</strong> <%= ketQua.getThongBao() %>
                    </div>
                </div>
            <%
                    }
                }
            %>

            <%
                ThongTinBanGiaoDTO thongTin = (ThongTinBanGiaoDTO) request.getAttribute("thongTinBanGiao");
                if (thongTin != null && thongTin.getNguoiBiKhoa() != null) {
                    NguoiDung u = thongTin.getNguoiBiKhoa();
            %>

            <%-- Cảnh báo bắt buộc bàn giao & thu hồi session --%>
            <div class="alert alert-warning">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
                    <line x1="12" y1="9" x2="12" y2="13"></line>
                    <line x1="12" y1="17" x2="12.01" y2="17"></line>
                </svg>
                <div>
                    <strong>Cảnh báo quan trọng:</strong> Khi nhấn xác nhận khoá tài khoản:
                    <ul style="margin: 0.35rem 0 0 1rem; padding: 0;">
                        <li>Toàn bộ phiên làm việc (Session) đang mở của tài khoản này sẽ bị thu hồi ngay lập tức.</li>
                        <li>Toàn bộ <strong><%= thongTin.getSoKhachHangHienTai() %></strong> khách hàng và <strong><%= thongTin.getSoCoHoiHienTai() %></strong> cơ hội sẽ được chuyển quyền sở hữu sang người tiếp nhận.</li>
                        <li>Hệ thống ghi nhận nhật ký bàn giao đầy đủ để đối soát.</li>
                    </ul>
                </div>
            </div>

            <div class="card-grid">
                <%-- Thẻ thông tin nhân viên nghỉ việc --%>
                <div class="card">
                    <h2 class="card-title">1. Thông tin nhân viên sắp nghỉ</h2>

                    <div class="user-profile-badge">
                        <div class="user-avatar-lg">
                            <%= u.getTenVietTat() %>
                        </div>
                        <div class="user-details">
                            <h3><%= u.getHoTen() %></h3>
                            <div class="user-meta-item">
                                <strong>Email:</strong> <%= u.getEmail() %>
                            </div>
                            <div class="user-meta-item">
                                <strong>Vai trò:</strong> <%= u.getChuoiVaiTroHienThi() %>
                            </div>
                            <div class="user-meta-item">
                                <strong>Nhóm kinh doanh:</strong> <%= u.getTenNhomKinhDoanh() %>
                            </div>
                            <div class="user-meta-item">
                                <strong>Trạng thái hiện tại:</strong>
                                <% if (u.daKhoa()) { %>
                                    <span style="color: var(--danger-color); font-weight: 600;">ĐÃ KHOÁ</span>
                                <% } else { %>
                                    <span style="color: #059669; font-weight: 600;">ĐANG HOẠT ĐỘNG</span>
                                <% } %>
                            </div>
                        </div>
                    </div>

                    <%-- Thống kê số lượng dữ liệu cần chuyển giao --%>
                    <div class="stats-cards">
                        <div class="stat-box">
                            <div class="stat-value"><%= thongTin.getSoKhachHangHienTai() %></div>
                            <div class="stat-label">Khách hàng cần bàn giao</div>
                        </div>
                        <div class="stat-box">
                            <div class="stat-value"><%= thongTin.getSoCoHoiHienTai() %></div>
                            <div class="stat-label">Cơ hội bán hàng cần chuyển</div>
                        </div>
                    </div>
                </div>

                <%-- Form chỉ định người tiếp nhận & lý do --%>
                <div class="card">
                    <h2 class="card-title">2. Chỉ định bàn giao & Xác nhận khoá</h2>

                    <form id="formKhoaTaiKhoan" method="POST" action="${pageContext.request.contextPath}/nguoi-dung/khoa-tai-khoan">
                        <input type="hidden" name="nguoiBiKhoaId" value="<%= u.getId() %>" />

                        <div class="form-group">
                            <label class="form-label" for="nguoiTiepNhanId">
                                Người tiếp nhận dữ liệu <span class="required">*</span>
                            </label>
                            <select id="nguoiTiepNhanId" name="nguoiTiepNhanId" class="form-control" required>
                                <option value="">-- Chọn nhân viên tiếp nhận --</option>
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
                            <div class="form-help">
                                Bắt buộc chọn người tiếp nhận trước khi khoá (AC 2).
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="form-label" for="lyDo">
                                Lý do khoá & Ghi chú bàn giao
                            </label>
                            <textarea id="lyDo" name="lyDo" class="form-control" placeholder="Ví dụ: Nhân viên nghỉ việc theo quyết định số..."></textarea>
                            <div class="form-help">
                                Ghi chú này sẽ được lưu vào nhật ký bàn giao (AC 3).
                            </div>
                        </div>

                        <div class="form-actions">
                            <button type="button" class="btn btn-danger" id="btnXacNhanKhoa">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                                    <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                                </svg>
                                Xác nhận khoá & Bàn giao
                            </button>
                            <a href="${pageContext.request.contextPath}/nguoi-dung/phan-quyen" class="btn btn-secondary">
                                Huỷ bỏ / Quay lại
                            </a>
                        </div>
                    </form>
                </div>
            </div>

            <% } else { %>
            <div class="card">
                <p style="color: var(--text-secondary); margin: 0;">
                    Vui lòng chọn nhân viên cần khoá và bàn giao dữ liệu từ
                    <a href="${pageContext.request.contextPath}/nguoi-dung/phan-quyen" style="color: var(--primary-color); font-weight: 600;">
                        Danh sách quản lý phân quyền người dùng
                    </a>.
                </p>
            </div>
            <% } %>

            <%-- Bảng lịch sử / Nhật ký bàn giao (AC 3: Việc bàn giao được ghi nhật ký) --%>
            <div class="history-section">
                <div class="card">
                    <h2 class="card-title">Nhật ký bàn giao dữ liệu gần đây</h2>
                    <div class="table-responsive">
                        <table class="custom-table">
                            <thead>
                                <tr>
                                    <th>Thời gian</th>
                                    <th>Nhân viên bị khoá</th>
                                    <th>Người tiếp nhận</th>
                                    <th>Khách hàng</th>
                                    <th>Cơ hội</th>
                                    <th>Người thực hiện</th>
                                    <th>Lý do / Ghi chú</th>
                                </tr>
                            </thead>
                            <tbody>
                                <%
                                    List<NhatKyBanGiao> dsLichSu = (List<NhatKyBanGiao>) request.getAttribute("dsLichSu");
                                    if (dsLichSu != null && !dsLichSu.isEmpty()) {
                                        for (NhatKyBanGiao nk : dsLichSu) {
                                %>
                                    <tr>
                                        <td><%= nk.getThoiGianDinhDang() %></td>
                                        <td>
                                            <strong><%= nk.getNguoiBiKhoa() != null ? nk.getNguoiBiKhoa().getHoTen() : "ID=" + nk.getNguoiBiKhoaId() %></strong>
                                        </td>
                                        <td>
                                            <span style="color: #059669; font-weight: 600;">
                                                <%= nk.getNguoiTiepNhan() != null ? nk.getNguoiTiepNhan().getHoTen() : "ID=" + nk.getNguoiTiepNhanId() %>
                                            </span>
                                        </td>
                                        <td>
                                            <span class="badge-count"><%= nk.getSoKhachHangChuyen() %> KH</span>
                                        </td>
                                        <td>
                                            <span class="badge-count"><%= nk.getSoCoHoiChuyen() %> CH</span>
                                        </td>
                                        <td>
                                            <%= nk.getNguoiThucHien() != null ? nk.getNguoiThucHien().getHoTen() : "Admin" %>
                                        </td>
                                        <td><%= nk.getLyDo() != null ? nk.getLyDo() : "Bàn giao nhân viên nghỉ" %></td>
                                    </tr>
                                <%
                                        }
                                    } else {
                                %>
                                    <tr>
                                        <td colspan="7" style="text-align: center; color: var(--text-muted); padding: 1.5rem;">
                                            Chưa có nhật ký bàn giao nào trong hệ thống.
                                        </td>
                                    </tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

        </div>
    </main>

    <script src="${pageContext.request.contextPath}/assets/js/navigation.js"></script>
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            var btnXacNhan = document.getElementById('btnXacNhanKhoa');
            var form = document.getElementById('formKhoaTaiKhoan');
            var selectNguoiNhan = document.getElementById('nguoiTiepNhanId');

            if (btnXacNhan && form) {
                btnXacNhan.addEventListener('click', function(e) {
                    if (!selectNguoiNhan.value) {
                        alert('Bắt buộc phải chọn người tiếp nhận dữ liệu trước khi khoá tài khoản!');
                        selectNguoiNhan.focus();
                        return;
                    }

                    var tenNguoiNhan = selectNguoiNhan.options[selectNguoiNhan.selectedIndex].text;
                    var xacNhan = confirm(
                        'CẢNH BÁO XÁC NHẬN:\n\n' +
                        'Bạn có chắc chắn muốn khoá tài khoản này không?\n\n' +
                        '- Toàn bộ khách hàng và cơ hội sẽ được chuyển sang: ' + tenNguoiNhan + '\n' +
                        '- Tất cả phiên làm việc đang mở của người này sẽ bị thu hồi ngay lập tức!\n\n' +
                        'Nhấn OK để thực hiện khoá và bàn giao.'
                    );

                    if (xacNhan) {
                        btnXacNhan.disabled = true;
                        btnXacNhan.textContent = 'Đang xử lý bàn giao...';
                        form.submit();
                    }
                });
            }
        });
    </script>
</body>
</html>
