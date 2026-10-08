<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Chi tiết hồ sơ khách hàng doanh nghiệp - Hệ thống CRM Bán Hàng">
    <title>Hồ Sơ Khách Hàng: <c:out value="${not empty khachHang ? khachHang.tenCongTy : banGhi.tieuDe}" /> - CRM Bán Hàng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/khach-hang/khach-hang.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <script>
        window.CONTEXT_PATH = "${pageContext.request.contextPath}";
    </script>
    <style>
        .breadcrumb-nav { margin-bottom: 20px; }
        .breadcrumb-list { display: flex; align-items: center; list-style: none; gap: 8px; font-size: 13.5px; color: var(--slate-500); flex-wrap: wrap; }
        .breadcrumb-item a { color: var(--primary); text-decoration: none; font-weight: 500; }
        .breadcrumb-item a:hover { text-decoration: underline; }
        .breadcrumb-separator { color: var(--slate-400); }
        .breadcrumb-item.active { color: var(--slate-700); font-weight: 600; }
        .detail-card { background: #ffffff; border-radius: var(--radius-lg); border: 1px solid var(--border-color); box-shadow: var(--shadow-sm); overflow: hidden; margin-bottom: 24px; }
        .detail-header { padding: 20px 24px; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 16px; background-color: #ffffff; }
        .detail-title-group h1 { font-size: 20px; font-weight: 800; color: var(--slate-900); display: flex; align-items: center; gap: 10px; }
        .detail-title-group p { font-size: 13px; color: var(--slate-500); margin-top: 4px; }
        .detail-body { padding: 24px; }
        .detail-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 20px; margin-bottom: 24px; }
        .detail-item { display: flex; flex-direction: column; gap: 6px; }
        .detail-label { font-size: 12px; font-weight: 600; color: var(--slate-500); text-transform: uppercase; letter-spacing: 0.03em; }
        .detail-value { font-size: 14.5px; color: var(--slate-900); font-weight: 500; word-break: break-word; }
        .detail-value.font-mono { font-family: monospace; font-size: 14px; }
        .detail-desc-box { background-color: var(--slate-50); border: 1px solid var(--slate-200); border-radius: var(--radius-md); padding: 16px; color: var(--slate-700); font-size: 14px; line-height: 1.6; white-space: pre-wrap; min-height: 80px; }
        .detail-footer { padding: 16px 24px; background-color: var(--slate-50); border-top: 1px solid var(--border-color); display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
    </style>
</head>
<body class="crm-body">
    <!-- Thanh điều hướng và Sidebar chuẩn hệ thống S1-06 -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <!-- Breadcrumb -->
        <nav class="breadcrumb-nav" aria-label="Breadcrumb">
            <ol class="breadcrumb-list">
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/dieu-huong">Trang chủ</a></li>
                <span class="breadcrumb-separator">/</span>
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/khach-hang">Khách hàng</a></li>
                <span class="breadcrumb-separator">/</span>
                <li class="breadcrumb-item active" aria-current="page"><c:out value="${not empty khachHang ? khachHang.tenCongTy : banGhi.tieuDe}" /></li>
            </ol>
        </nav>

        <!-- Thông báo thành công / trạng thái -->
        <c:if test="${not empty thongBaoThanhCong}">
            <div class="alert alert-success" style="margin-bottom: 20px;">
                <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                <span><c:out value="${thongBaoThanhCong}" /></span>
            </div>
        </c:if>
        <c:if test="${not empty thongBaoLoi}">
            <div class="alert alert-danger" style="margin-bottom: 20px;">
                <span class="material-symbols-outlined" aria-hidden="true">error</span>
                <span><c:out value="${thongBaoLoi}" /></span>
            </div>
        </c:if>

        <c:set var="tenHienThi" value="${not empty khachHang ? khachHang.tenCongTy : banGhi.tieuDe}" />
        <c:set var="maHienThi" value="${not empty khachHang ? khachHang.maKhachHang : banGhi.maBanGhi}" />
        <c:set var="currentStatus" value="${not empty khachHang ? khachHang.trangThaiHienThi : banGhi.trangThai}" />

        <!-- Thẻ chi tiết bản ghi -->
        <div class="detail-card">
            <div class="detail-header">
                <div class="detail-title-group">
                    <h1>
                        <span class="material-symbols-outlined" aria-hidden="true" style="color: var(--primary);">business</span>
                        <span><c:out value="${tenHienThi}" /></span>
                    </h1>
                    <p>Mã KH: <span class="font-mono"><c:out value="${maHienThi}" /></span> &bull; Hồ sơ doanh nghiệp chuẩn (Story S3-01)</p>
                </div>
                <div style="display: flex; align-items: center; gap: 10px; flex-wrap: wrap;">
                    <c:choose>
                        <c:when test="${currentStatus == 'Tiềm năng' || currentStatus == 'TIEM_NANG'}">
                            <span class="badge-status-tiem-nang"><span class="badge-dot" aria-hidden="true"></span>Tiềm năng</span>
                        </c:when>
                        <c:when test="${currentStatus == 'Đang giao dịch' || currentStatus == 'DANG_GIAO_DICH'}">
                            <span class="badge-status-dang-giao-dich"><span class="badge-dot" aria-hidden="true"></span>Đang giao dịch</span>
                        </c:when>
                        <c:when test="${currentStatus == 'Khách hàng' || currentStatus == 'KHACH_HANG'}">
                            <span class="badge-status-khach-hang"><span class="badge-dot" aria-hidden="true"></span>Khách hàng</span>
                        </c:when>
                        <c:when test="${currentStatus == 'Ngừng hợp tác' || currentStatus == 'NGUNG_HOP_TAC'}">
                            <span class="badge-status-ngung-hop-tac"><span class="badge-dot" aria-hidden="true"></span>Ngừng hợp tác</span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge badge-success"><c:out value="${currentStatus}" /></span>
                        </c:otherwise>
                    </c:choose>

                    <button type="button" class="btn btn-primary btn-sua-khach-hang" id="btnSuaKhachHangChiTiet"
                        data-id="${not empty khachHang ? khachHang.id : banGhi.id}"
                        data-ten="<c:out value="${tenHienThi}" />"
                        data-makh="<c:out value="${maHienThi}" />"
                        data-mst="<c:out value="${not empty khachHang ? khachHang.maSoThue : ''}" />"
                        data-nganh="${not empty khachHang ? khachHang.nganhNgheId : ''}"
                        data-quymo="${not empty khachHang ? khachHang.quyMoId : ''}"
                        data-website="<c:out value="${not empty khachHang ? khachHang.website : ''}" />"
                        data-diachi="<c:out value="${not empty khachHang ? khachHang.diaChi : ''}" />"
                        data-trangthai="<c:out value="${not empty khachHang ? khachHang.trangThai : banGhi.trangThai}" />"
                        data-gia="<c:out value="${not empty khachHang ? khachHang.doanhThuUocTinh : banGhi.giaTri}" />"
                        data-nguoisohuu="${not empty khachHang ? khachHang.nguoiSoHuuId : banGhi.nguoiPhuTrachId}"
                        data-mota="<c:out value="${not empty khachHang ? khachHang.moTaChiTiet : banGhi.moTaChiTiet}" />">
                        <span class="material-symbols-outlined" aria-hidden="true">edit</span>
                        <span>Sửa hồ sơ</span>
                    </button>

                    <a href="${pageContext.request.contextPath}/khach-hang" class="btn btn-outline" id="btnBackKhachHang">
                        <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span>
                        <span>Quay lại danh sách</span>
                    </a>
                </div>
            </div>

            <div class="detail-body">
                <div class="detail-grid">
                    <div class="detail-item">
                        <span class="detail-label">Mã khách hàng</span>
                        <span class="detail-value font-mono"><c:out value="${maHienThi}" /></span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Tên công ty / Khách hàng (AC1)</span>
                        <span class="detail-value" style="font-weight: 700; color: var(--slate-900);"><c:out value="${tenHienThi}" /></span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Mã số thuế (AC1, AC2)</span>
                        <span class="detail-value font-mono">
                            <c:choose>
                                <c:when test="${not empty khachHang and not empty khachHang.maSoThue}">
                                    <code class="mst-badge"><c:out value="${khachHang.maSoThue}" /></code>
                                </c:when>
                                <c:otherwise>
                                    <span class="text-muted-italic">Chưa khai báo</span>
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Ngành nghề (AC1)</span>
                        <span class="detail-value"><c:out value="${not empty khachHang && not empty khachHang.tenNganhNghe ? khachHang.tenNganhNghe : 'Chưa phân ngành'}" /></span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Quy mô doanh nghiệp (AC1)</span>
                        <span class="detail-value"><c:out value="${not empty khachHang && not empty khachHang.tenQuyMo ? khachHang.tenQuyMo : 'Chưa phân loại'}" /></span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Website (AC1)</span>
                        <span class="detail-value">
                            <c:choose>
                                <c:when test="${not empty khachHang and not empty khachHang.website}">
                                    <a href="${khachHang.website.startsWith('http') ? khachHang.website : 'https://'.concat(khachHang.website)}" target="_blank" rel="noopener noreferrer" class="link-website">
                                        <c:out value="${khachHang.website}" />
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <span class="text-muted-italic">Chưa khai báo</span>
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Địa chỉ trụ sở (AC1)</span>
                        <span class="detail-value"><c:out value="${not empty khachHang && not empty khachHang.diaChi ? khachHang.diaChi : 'Chưa cập nhật'}" /></span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Người sở hữu (AC1, AC4)</span>
                        <span class="detail-value"><c:out value="${not empty khachHang ? khachHang.tenNguoiSoHuu : banGhi.tenNguoiPhuTrach}" /></span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Nhóm kinh doanh (AC4)</span>
                        <span class="detail-value"><c:out value="${not empty khachHang ? khachHang.tenNhomKinhDoanh : banGhi.tenNhom}" /></span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Doanh thu ước tính</span>
                        <span class="detail-value"><c:out value="${not empty khachHang ? khachHang.doanhThuUocTinh : banGhi.giaTri}" /> VND</span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Trạng thái (AC3)</span>
                        <span class="detail-value"><c:out value="${currentStatus}" /></span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Ngày tạo hồ sơ</span>
                        <span class="detail-value"><c:out value="${not empty khachHang ? khachHang.ngayTaoDinhDang : banGhi.ngayTao}" /></span>
                    </div>
                </div>

                <div class="detail-item">
                    <span class="detail-label" style="margin-bottom: 6px;">Mô tả / Ghi chú chi tiết</span>
                    <div class="detail-desc-box">
                        <c:set var="desc" value="${not empty khachHang ? khachHang.moTaChiTiet : banGhi.moTaChiTiet}" />
                        <c:choose>
                            <c:when test="${not empty desc}">
                                <c:out value="${desc}" />
                            </c:when>
                            <c:otherwise>
                                <span class="text-muted-italic">Chưa có mô tả chi tiết cho hồ sơ khách hàng này.</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

            <div class="detail-footer">
                <span style="font-size: 13px; color: var(--slate-500);">
                    Chế độ: <strong>Hồ sơ khách hàng doanh nghiệp</strong> &bull; Phân quyền Data Scope tự động (AC4).
                </span>
                <div style="display: flex; gap: 8px;">
                    <a href="${pageContext.request.contextPath}/khach-hang" class="btn btn-outline">
                        <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span>
                        <span>Quay lại danh sách</span>
                    </a>
                </div>
            </div>
        </div>

        <!-- Modal Sửa Khách Hàng (Story S3-01 & S1-05) -->
        <div class="modal-backdrop" id="modalSuaKhachHang" style="display: none;" role="dialog" aria-modal="true" aria-labelledby="modalSuaKhachHangTieuDe">
            <div class="modal-card">
                <div class="modal-header">
                    <div>
                        <h2 class="modal-title" id="modalSuaKhachHangTieuDe">Chỉnh Sửa Hồ Sơ Khách Hàng</h2>
                        <p class="modal-subtitle">Cập nhật thông tin hồ sơ khách hàng doanh nghiệp (Story S3-01)</p>
                    </div>
                    <button type="button" class="modal-close-btn" id="btnDongModalSuaKhachHang" aria-label="Đóng" title="Đóng">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>
                <form id="formSuaKhachHang" method="POST" action="${pageContext.request.contextPath}/khach-hang">
                    <input type="hidden" name="action" value="sua">
                    <input type="hidden" id="suaKhachHangId" name="id" value="">

                    <div class="modal-body">
                        <!-- Tên công ty & Mã khách hàng -->
                        <div class="form-row">
                            <div class="form-col" style="flex: 2;">
                                <label class="form-label" for="suaTenCongTy">Tên công ty / Khách hàng (AC1) <span class="required">*</span></label>
                                <input type="text" id="suaTenCongTy" name="tenCongTy" class="form-input" required autocomplete="off">
                            </div>
                            <div class="form-col" style="flex: 1;">
                                <label class="form-label" for="suaMaKhachHang">Mã khách hàng</label>
                                <input type="text" id="suaMaKhachHang" name="maKhachHang" class="form-input" readonly style="background-color: var(--slate-100); font-family: monospace;">
                            </div>
                        </div>

                        <!-- Mã số thuế & Website -->
                        <div class="form-row">
                            <div class="form-col">
                                <label class="form-label" for="suaMaSoThue">Mã số thuế (AC1, AC2) <small style="color: var(--slate-500); font-weight: normal;">(Nếu có phải duy nhất)</small></label>
                                <input type="text" id="suaMaSoThue" name="maSoThue" class="form-input" autocomplete="off">
                            </div>
                            <div class="form-col">
                                <label class="form-label" for="suaWebsite">Website (AC1)</label>
                                <input type="text" id="suaWebsite" name="website" class="form-input" autocomplete="off">
                            </div>
                        </div>

                        <!-- Địa chỉ -->
                        <div class="form-group">
                            <label class="form-label" for="suaDiaChi">Địa chỉ trụ sở / văn phòng (AC1)</label>
                            <input type="text" id="suaDiaChi" name="diaChi" class="form-input" autocomplete="off">
                        </div>

                        <!-- Trạng thái & Doanh thu ước tính -->
                        <div class="form-row">
                            <div class="form-col">
                                <label class="form-label" for="suaTrangThai">Trạng thái khách hàng (AC3) <span class="required">*</span></label>
                                <select id="suaTrangThai" name="trangThai" class="form-select" required>
                                    <option value="Tiềm năng">Tiềm năng</option>
                                    <option value="Đang giao dịch">Đang giao dịch</option>
                                    <option value="Khách hàng">Khách hàng</option>
                                    <option value="Ngừng hợp tác">Ngừng hợp tác</option>
                                </select>
                            </div>
                            <div class="form-col">
                                <label class="form-label" for="suaDoanhThuUocTinh">Doanh thu ước tính (VND)</label>
                                <input type="text" id="suaDoanhThuUocTinh" name="doanhThuUocTinh" class="form-input" autocomplete="off">
                            </div>
                        </div>

                        <!-- Ghi chú chi tiết -->
                        <div class="form-group">
                            <label class="form-label" for="suaMoTaChiTiet">Ghi chú / Mô tả chi tiết</label>
                            <textarea id="suaMoTaChiTiet" name="moTaChiTiet" class="form-textarea" rows="3"></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline" id="btnHuySuaKhachHang">Hủy bỏ</button>
                        <button type="submit" class="btn btn-primary" id="btnXacNhanSuaKhachHang">
                            <span class="material-symbols-outlined" aria-hidden="true">save</span>
                            <span>Lưu Thay Đổi</span>
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </main>

    <script src="${pageContext.request.contextPath}/assets/js/navigation.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/khach-hang.js"></script>
</body>
</html>
