<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Danh mục Sản phẩm & Bảng giá niêm yết | CRM Bán hàng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/san-pham/san-pham.css">
</head>
<body>

<div class="crm-container">

    <!-- Header Section -->
    <header class="crm-header">
        <div class="crm-header-title">
            <h1>
                <span>🏷️</span> Danh mục Sản phẩm & Bảng giá niêm yết
            </h1>
            <p>
                Quản lý danh mục hàng hóa, dịch vụ và khung giá chuẩn của doanh nghiệp. Mọi báo giá kinh doanh đều bắt buộc xuất phát từ bảng giá chuẩn này thay vì giá tự tính.
            </p>
        </div>

        <div class="header-actions">
            <!-- Badge & Chuyển đổi vai trò người xem -->
            <div class="role-badge-box">
                <span style="font-size: 13px; color: var(--slate-500); font-weight: 500;">Góc nhìn:</span>
                <c:choose>
                    <c:when test="${coQuyenGiaVon}">
                        <span class="role-badge role-badge-director" title="Đang ở vai trò Giám đốc: Toàn quyền quản trị và xem/sửa Giá vốn">
                            👔 Giám đốc kinh doanh (Xem & Sửa Giá vốn)
                        </span>
                        <a href="${pageContext.request.contextPath}/san-pham?role=SALES_REP" 
                           class="btn btn-outline btn-sm" 
                           title="Mô phỏng góc nhìn của Nhân viên kinh doanh">
                            👁️ Xem quyền Sales Rep
                        </a>
                    </c:when>
                    <c:otherwise>
                        <span class="role-badge role-badge-sales" title="Đang ở vai trò Nhân viên Sales: Chỉ xem Giá niêm yết & Giá sàn, ẩn Giá vốn">
                            👤 Nhân viên Sales (Không xem Giá vốn)
                        </span>
                        <a href="${pageContext.request.contextPath}/san-pham?role=DIRECTOR" 
                           class="btn btn-outline btn-sm" 
                           title="Trở lại quyền Giám đốc kinh doanh">
                            🔄 Đổi lại Giám đốc
                        </a>
                    </c:otherwise>
                </c:choose>
            </div>

            <c:if test="${coQuyenQuanLy}">
                <a href="${pageContext.request.contextPath}/san-pham/tao${not empty param.role ? '?role='.concat(param.role) : ''}" 
                   class="btn btn-primary" id="btnThemSanPhamMoi">
                    <span>➕</span> Khai báo sản phẩm mới
                </a>
            </c:if>
        </div>
    </header>

    <!-- Thông báo Flash Messages -->
    <c:if test="${not empty thongBaoThanhCong}">
        <div class="alert alert-success" role="alert">
            <div style="display: flex; align-items: center; gap: 8px;">
                <span style="font-size: 18px;">✅</span>
                <span><strong>Thành công:</strong> ${thongBaoThanhCong}</span>
            </div>
            <span class="alert-close" onclick="this.parentElement.style.display='none';">&times;</span>
        </div>
    </c:if>

    <c:if test="${not empty thongBaoLoi}">
        <div class="alert alert-danger" role="alert">
            <div style="display: flex; align-items: center; gap: 8px;">
                <span style="font-size: 18px;">⚠️</span>
                <span><strong>Thông báo:</strong> ${thongBaoLoi}</span>
            </div>
            <span class="alert-close" onclick="this.parentElement.style.display='none';">&times;</span>
        </div>
    </c:if>

    <!-- Thống kê tổng quan nhanh -->
    <section class="stats-grid">
        <div class="stat-card">
            <div class="stat-icon primary">📦</div>
            <div class="stat-content">
                <span class="stat-value">${phanTrang.tongSoBanGhi}</span>
                <span class="stat-label">Tổng mặt hàng trong danh mục</span>
            </div>
        </div>

        <div class="stat-card">
            <div class="stat-icon purple">🔄</div>
            <div class="stat-content">
                <span class="stat-value">Dịch vụ & Sản phẩm</span>
                <span class="stat-label">Thuê bao định kỳ / Một lần</span>
            </div>
        </div>

        <div class="stat-card">
            <div class="stat-icon warning">⚡</div>
            <div class="stat-content">
                <span class="stat-value">Giá sàn niêm yết</span>
                <span class="stat-label">Ngưỡng kiểm soát chiết khấu</span>
            </div>
        </div>

        <div class="stat-card">
            <div class="stat-icon success">🔒</div>
            <div class="stat-content">
                <span class="stat-value">${coQuyenGiaVon ? 'Được bảo mật' : 'Ẩn hoàn toàn'}</span>
                <span class="stat-label">Giá vốn & Tỷ suất lợi nhuận</span>
            </div>
        </div>
    </section>

    <!-- Card danh sách & Bộ lọc -->
    <main class="crm-card">
        <!-- Bộ lọc tìm kiếm -->
        <form method="get" action="${pageContext.request.contextPath}/san-pham" class="filter-bar">
            <c:if test="${not empty param.role}">
                <input type="hidden" name="role" value="${param.role}">
            </c:if>

            <div class="filter-input-group">
                <span class="filter-icon">🔍</span>
                <input type="text" name="tuKhoa" value="${tuKhoa}" 
                       placeholder="Tìm theo mã hàng, tên sản phẩm hoặc dịch vụ..." 
                       class="filter-input" autocomplete="off">
            </div>

            <select name="loai" class="filter-select" aria-label="Lọc theo loại sản phẩm">
                <option value="">-- Tất cả loại sản phẩm --</option>
                <c:forEach var="l" items="${danhSachLoai}">
                    <option value="${l.maLoai}" ${loaiChon == l.maLoai ? 'selected' : ''}>
                        ${l.tenHienThi}
                    </option>
                </c:forEach>
            </select>

            <select name="trangThai" class="filter-select" aria-label="Lọc theo trạng thái kinh doanh">
                <option value="">-- Tất cả trạng thái --</option>
                <c:forEach var="tt" items="${danhSachTrangThai}">
                    <option value="${tt.maTrangThai}" ${trangThaiChon == tt.maTrangThai ? 'selected' : ''}>
                        ${tt.tenHienThi}
                    </option>
                </c:forEach>
            </select>

            <button type="submit" class="btn btn-outline" id="btnLocSanPham">
                Lọc dữ liệu
            </button>
            <a href="${pageContext.request.contextPath}/san-pham${not empty param.role ? '?role='.concat(param.role) : ''}" 
               class="btn btn-outline" style="color: var(--slate-500);">
                Đặt lại
            </a>
        </form>

        <!-- Bảng danh sách sản phẩm & Bảng giá -->
        <div class="table-responsive">
            <table class="crm-table" id="bangSanPham">
                <thead>
                <tr>
                    <th style="width: 130px;">Mã sản phẩm</th>
                    <th style="min-width: 220px;">Tên sản phẩm / Dịch vụ</th>
                    <th style="width: 140px;">Loại</th>
                    <th style="width: 90px;">ĐVT</th>
                    <th style="text-align: right; width: 140px;">Giá niêm yết</th>
                    <th style="text-align: right; width: 170px;" title="Ngưỡng duyệt chiết khấu báo giá (AC2)">
                        Giá sàn (Ngưỡng duyệt)
                    </th>
                    <c:if test="${coQuyenGiaVon}">
                        <th style="text-align: right; width: 160px; background-color: #f1f5f9;" title="Chỉ Giám đốc kinh doanh xem được (AC3)">
                            🔒 Giá vốn (Director)
                        </th>
                    </c:if>
                    <th style="text-align: center; width: 130px;">Trạng thái</th>
                    <th style="text-align: center; width: 130px;" title="Ràng buộc xóa khi đã có trong báo giá (AC4)">
                        Báo giá liên kết
                    </th>
                    <th style="text-align: center; width: 220px;">Thao tác</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${empty phanTrang.danhSach}">
                        <tr>
                            <td colspan="${coQuyenGiaVon ? 10 : 9}" style="text-align: center; padding: 48px 16px; color: var(--slate-500);">
                                <div style="font-size: 32px; margin-bottom: 8px;">🔍</div>
                                <div style="font-weight: 600; font-size: 15px; color: var(--slate-700);">Không tìm thấy sản phẩm nào</div>
                                <div style="font-size: 13px; margin-top: 4px;">Thử thay đổi từ khóa hoặc điều kiện lọc để hiển thị kết quả.</div>
                            </td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="sp" items="${phanTrang.danhSach}">
                            <tr id="row-sp-${sp.id}">
                                <td>
                                    <span class="product-code-pill">${sp.maSanPham}</span>
                                </td>
                                <td>
                                    <div class="product-name">${sp.tenSanPham}</div>
                                    <c:if test="${not empty sp.moTa}">
                                        <div class="product-desc" title="${sp.moTa}">${sp.moTa}</div>
                                    </c:if>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${sp.dichVuThueBao}">
                                            <span class="badge badge-subscription">
                                                🔄 ${sp.tenLoai}
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge-onetime">
                                                📦 ${sp.tenLoai}
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td><strong>${sp.donViTinh}</strong></td>
                                
                                <!-- Giá niêm yết -->
                                <td style="text-align: right;">
                                    <span style="font-weight: 800; color: var(--primary); font-size: 14.5px;">
                                        ${sp.giaNiemYetDinhDang}
                                    </span>
                                </td>

                                <!-- Giá sàn (Ngưỡng duyệt chiết khấu) -->
                                <td style="text-align: right;">
                                    <div class="floor-threshold-pill">
                                        <span class="floor-price-val">${sp.giaSanDinhDang}</span>
                                        <span class="floor-price-sub">Ngưỡng duyệt</span>
                                    </div>
                                </td>

                                <!-- AC3: Giá vốn chỉ Giám đốc kinh doanh xem được -->
                                <c:if test="${coQuyenGiaVon}">
                                    <td style="text-align: right; background-color: #fafafa;">
                                        <div style="font-weight: 700; color: #166534;">
                                            ${sp.giaVonDinhDang}
                                        </div>
                                        <c:if test="${not empty sp.giaVon and sp.giaVon > 0 and sp.giaNiemYet > 0}">
                                            <div style="font-size: 11px; color: var(--slate-500);" title="Tỷ suất lợi nhuận gộp danh nghĩa">
                                                Lãi gộp: <fmt:formatNumber value="${((sp.giaNiemYet - sp.giaVon) / sp.giaNiemYet) * 100}" maxFractionDigits="1"/>%
                                            </div>
                                        </c:if>
                                    </td>
                                </c:if>

                                <!-- Trạng thái kinh doanh -->
                                <td style="text-align: center;">
                                    <c:choose>
                                        <c:when test="${sp.dangKinhDoanh}">
                                            <span class="badge badge-active">
                                                ● Đang kinh doanh
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge-inactive">
                                                ○ Ngừng kinh doanh
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <!-- AC4: Ràng buộc đã xuất hiện trong Báo giá -->
                                <td style="text-align: center;">
                                    <c:choose>
                                        <c:when test="${sp.daXuatHienTrongBaoGia}">
                                            <span class="badge badge-quote" 
                                                  title="Sản phẩm đã xuất hiện trong báo giá thực tế. Theo quy định, không thể xoá, chỉ được ngừng kinh doanh.">
                                                🔒 Đã có báo giá
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="font-size: 12px; color: var(--slate-400);">Chưa có</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <!-- Hành động / Thao tác -->
                                <td style="text-align: center;">
                                    <div class="table-actions">
                                        <!-- AC2: Simulator Thử giá sàn trong báo giá -->
                                        <button type="button" class="btn btn-outline btn-sm"
                                                data-id="${sp.id}"
                                                data-ten="<c:out value='${sp.tenSanPham}'/>"
                                                data-gia-niem-yet="${sp.giaNiemYet}"
                                                data-gia-san="${sp.giaSan}"
                                                onclick="moModalThuGiaSan(this)"
                                                title="Kiểm tra xem đơn giá báo giá có cần Giám đốc duyệt chiết khấu không">
                                            ⚡ Thử giá sàn
                                        </button>

                                        <c:if test="${coQuyenQuanLy}">
                                            <!-- Sửa thông tin & bảng giá -->
                                            <a href="${pageContext.request.contextPath}/san-pham/sua?id=${sp.id}${not empty param.role ? '&role='.concat(param.role) : ''}"
                                               class="btn btn-outline btn-sm" title="Sửa thông tin sản phẩm và bảng giá">
                                                ✏️ Sửa
                                            </a>

                                            <!-- Ngừng kinh doanh / Bán lại -->
                                            <form method="post" action="${pageContext.request.contextPath}/san-pham/trang-thai" style="display:inline;">
                                                <c:if test="${not empty param.role}">
                                                    <input type="hidden" name="role" value="${param.role}">
                                                </c:if>
                                                <input type="hidden" name="id" value="${sp.id}">
                                                <c:choose>
                                                    <c:when test="${sp.dangKinhDoanh}">
                                                        <input type="hidden" name="trangThaiMoi" value="NGUNG_KINH_DOANH">
                                                        <button type="submit" class="btn btn-outline btn-sm" style="color: #b45309;"
                                                                onclick="return confirm('Bạn có chắc chắn muốn chuyển sản phẩm này sang trạng thái Ngừng kinh doanh?')"
                                                                title="Ngừng đưa vào các báo giá mới">
                                                            ⏸️ Ngừng KD
                                                        </button>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <input type="hidden" name="trangThaiMoi" value="DANG_KINH_DOANH">
                                                        <button type="submit" class="btn btn-outline btn-sm" style="color: #15803d;"
                                                                title="Kích hoạt bán lại trong báo giá">
                                                            ▶️ Mở bán lại
                                                        </button>
                                                    </c:otherwise>
                                                </c:choose>
                                            </form>

                                            <!-- AC4: Xóa sản phẩm -->
                                            <c:choose>
                                                <c:when test="${sp.daXuatHienTrongBaoGia}">
                                                    <!-- Đã có trong báo giá -> Nút xóa bị cấm -->
                                                    <button type="button" class="btn btn-outline btn-sm btn-disabled"
                                                            data-ten="<c:out value='${sp.tenSanPham}'/>"
                                                            onclick="moModalKhongTheXoa(this)"
                                                            title="Sản phẩm đã xuất hiện trong báo giá thì không xoá được, chỉ ngừng kinh doanh (AC4)">
                                                        🚫 Không thể xoá
                                                    </button>
                                                </c:when>
                                                <c:otherwise>
                                                    <!-- Chưa có trong báo giá -> Được phép xóa -->
                                                    <form method="post" action="${pageContext.request.contextPath}/san-pham/xoa" style="display:inline;">
                                                        <c:if test="${not empty param.role}">
                                                            <input type="hidden" name="role" value="${param.role}">
                                                        </c:if>
                                                        <input type="hidden" name="id" value="${sp.id}">
                                                        <button type="submit" class="btn btn-danger btn-sm"
                                                                onclick="return confirm('CẢNH BÁO: Bạn có chắc chắn muốn xoá hoàn toàn sản phẩm này khỏi hệ thống? Thao tác này không thể hoàn tác.')"
                                                                title="Xoá vĩnh viễn khỏi danh mục">
                                                            🗑️ Xoá
                                                        </button>
                                                    </form>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:if>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>

        <!-- Phân trang -->
        <c:if test="${phanTrang.tongSoTrang > 1}">
            <nav class="pagination" aria-label="Phân trang danh mục">
                <div>
                    Hiển thị trang <strong>${phanTrang.trangHienTai}</strong> / <strong>${phanTrang.tongSoTrang}</strong> 
                    (Tổng số <strong>${phanTrang.tongSoBanGhi}</strong> sản phẩm)
                </div>
                <div class="pagination-links">
                    <c:if test="${phanTrang.coTrangTruoc}">
                        <a href="${pageContext.request.contextPath}/san-pham?page=${phanTrang.trangHienTai - 1}&tuKhoa=${tuKhoa}&loai=${loaiChon}&trangThai=${trangThaiChon}${not empty param.role ? '&role='.concat(param.role) : ''}" 
                           class="page-btn">
                            &laquo; Trang trước
                        </a>
                    </c:if>

                    <c:forEach var="i" begin="1" end="${phanTrang.tongSoTrang}">
                        <a href="${pageContext.request.contextPath}/san-pham?page=${i}&tuKhoa=${tuKhoa}&loai=${loaiChon}&trangThai=${trangThaiChon}${not empty param.role ? '&role='.concat(param.role) : ''}"
                           class="page-btn ${i == phanTrang.trangHienTai ? 'active' : ''}">
                            ${i}
                        </a>
                    </c:forEach>

                    <c:if test="${phanTrang.coTrangSau}">
                        <a href="${pageContext.request.contextPath}/san-pham?page=${phanTrang.trangHienTai + 1}&tuKhoa=${tuKhoa}&loai=${loaiChon}&trangThai=${trangThaiChon}${not empty param.role ? '&role='.concat(param.role) : ''}" 
                           class="page-btn">
                            Trang sau &raquo;
                        </a>
                    </c:if>
                </div>
            </nav>
        </c:if>
    </main>

</div>

<!-- AC2: MODAL SIMULATOR KIỂM TRA NGƯỠNG GIÁ SÀN BÁO GIÁ -->
<div id="modalKiemTraGiaSan" class="modal-overlay" role="dialog" aria-modal="true">
    <div class="modal-card">
        <div class="modal-header">
            <h3>⚡ Kiểm tra Ngưỡng Giá sàn Báo giá</h3>
            <button type="button" class="modal-close-btn" onclick="dongModalGiaSan()">&times;</button>
        </div>

        <p style="font-size: 13.5px; color: var(--slate-600); margin-bottom: 16px; line-height: 1.5;">
            Quy định nghiệp vụ (AC2): Giá sàn là ngưỡng để xác định báo giá có cần duyệt chiết khấu hay không. Bất kỳ báo giá nào có đơn giá dưới Giá sàn bắt buộc phải gửi Giám đốc kinh doanh duyệt.
        </p>

        <div class="modal-meta-box">
            <div style="font-size: 14.5px; font-weight: 700; color: var(--dark);" id="modalTenSp">
                Tên sản phẩm
            </div>
            <div style="display: flex; justify-content: space-between; margin-top: 8px; font-size: 13px;">
                <span>Giá niêm yết: <strong id="modalGiaNiemYet" style="color: var(--primary);">0 ₫</strong></span>
                <span>Giá sàn (Ngưỡng duyệt): <strong id="modalGiaSan" style="color: #b45309;">0 ₫</strong></span>
            </div>
        </div>

        <input type="hidden" id="modalSanPhamId">
        
        <div class="form-group" style="margin-bottom: 12px;">
            <label for="modalDonGiaInput">Nhập thử đơn giá đề xuất trong Báo giá (VNĐ):</label>
            <input type="number" id="modalDonGiaInput" min="0" step="1000" 
                   style="padding: 12px 14px; font-size: 16px; font-weight: 800; color: var(--dark);" 
                   oninput="tinhKiemTraGiaSanClientSide()">
            <span class="form-hint">Nhập số tiền để xem báo giá có kích hoạt quy trình phê duyệt hay không.</span>
        </div>

        <div id="modalKetQuaBox"></div>

        <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 24px;">
            <button type="button" class="btn btn-outline" onclick="dongModalGiaSan()">Đóng</button>
            <button type="button" class="btn btn-primary" onclick="tinhKiemTraGiaSan()">Kiểm tra với máy chủ</button>
        </div>
    </div>
</div>

<!-- AC4: MODAL GIẢI THÍCH SẢN PHẨM KHÔNG THỂ XOÁ -->
<div id="modalKhongTheXoa" class="modal-overlay" role="dialog" aria-modal="true">
    <div class="modal-card">
        <div class="modal-header">
            <h3 style="color: var(--danger);">🚫 Không thể xoá sản phẩm</h3>
            <button type="button" class="modal-close-btn" onclick="dongModalKhongTheXoa()">&times;</button>
        </div>

        <div style="background: #fef2f2; border: 1px solid #fecaca; border-radius: 10px; padding: 16px; margin-bottom: 18px; color: #991b1b; font-size: 14px; line-height: 1.5;">
            Sản phẩm <strong id="modalTenSpXoa"></strong> đã xuất hiện trong ít nhất một báo giá kinh doanh trong hệ thống.
        </div>

        <div style="font-size: 13.5px; color: var(--slate-600); line-height: 1.6; margin-bottom: 20px;">
            <p><strong>Theo quy tắc toàn vẹn dữ liệu (Acceptance Criteria 4):</strong></p>
            <ul style="margin-left: 20px; margin-top: 6px;">
                <li>Sản phẩm đã có mặt trong báo giá khách hàng thì <strong>tuyệt đối không được xoá</strong> khỏi cơ sở dữ liệu để bảo toàn tính pháp lý và lịch sử giao dịch.</li>
                <li>Nếu không còn cung cấp mặt hàng này nữa, bạn hãy chuyển trạng thái sang <strong>Ngừng kinh doanh</strong>.</li>
            </ul>
        </div>

        <div style="display: flex; justify-content: flex-end;">
            <button type="button" class="btn btn-outline" onclick="dongModalKhongTheXoa()">Đã hiểu</button>
        </div>
    </div>
</div>

<script>
    window.APP_CONTEXT = '${pageContext.request.contextPath}';
</script>
<script src="${pageContext.request.contextPath}/assets/js/san-pham/san-pham.js"></script>
</body>
</html>
