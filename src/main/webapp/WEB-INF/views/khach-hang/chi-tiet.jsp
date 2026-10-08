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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/material-symbols.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/khach-hang/khach-hang.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
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
        .badge-status-tiem-nang { background-color: #e0f2fe; color: #0369a1; font-weight: 600; padding: 4px 10px; border-radius: 9999px; font-size: 12px; }
        .badge-status-dang-giao-dich { background-color: #fef3c7; color: #b45309; font-weight: 600; padding: 4px 10px; border-radius: 9999px; font-size: 12px; }
        .badge-status-khach-hang { background-color: #dcfce7; color: #15803d; font-weight: 600; padding: 4px 10px; border-radius: 9999px; font-size: 12px; }
        .badge-status-ngung-hop-tac { background-color: #fee2e2; color: #b91c1c; font-weight: 600; padding: 4px 10px; border-radius: 9999px; font-size: 12px; }
    </style>
</head>
<body class="crm-body">
    <!-- Thanh điều hướng và Sidebar chuẩn hệ thống S1-06 -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <!-- Breadcrumb -->
        <nav class="breadcrumb-nav" aria-label="Breadcrumb">
            <ol class="breadcrumb-list">
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/">Trang chủ</a></li>
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
        <c:if test="${not empty sessionScope.flashSuccess}">
            <div class="alert alert-success" style="margin-bottom: 20px;">
                <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                <span><c:out value="${sessionScope.flashSuccess}" /></span>
            </div>
            <c:remove var="flashSuccess" scope="session" />
        </c:if>
        <c:if test="${not empty thongBaoLoi}">
            <div class="alert alert-danger" style="margin-bottom: 20px;">
                <span class="material-symbols-outlined" aria-hidden="true">error</span>
                <span><c:out value="${thongBaoLoi}" /></span>
            </div>
        </c:if>
        <c:if test="${not empty sessionScope.flashError}">
            <div class="alert alert-danger" style="margin-bottom: 20px;">
                <span class="material-symbols-outlined" aria-hidden="true">error</span>
                <span><c:out value="${sessionScope.flashError}" /></span>
            </div>
            <c:remove var="flashError" scope="session" />
        </c:if>

        <!-- Thẻ chi tiết bản ghi -->
        <div class="detail-card">
            <div class="detail-header">
                <div class="detail-title-group">
                    <h1>
                        <span class="material-symbols-outlined" style="color: var(--primary); font-size: 24px;" aria-hidden="true">domain</span>
                        <span><c:out value="${not empty khachHang ? khachHang.tenCongTy : banGhi.tieuDe}" /></span>
                    </h1>
                    <p>Mã KH: <span class="font-mono"><c:out value="${not empty khachHang ? khachHang.maKhachHang : banGhi.maBanGhi}" /></span> • Hồ sơ doanh nghiệp chuẩn (Story S3-01)</p>
                </div>
                <div style="display: flex; align-items: center; gap: 10px; flex-wrap: wrap;">
                    <c:set var="currentStatus" value="${not empty khachHang ? khachHang.trangThaiHienThi : banGhi.trangThai}" />
                    <c:choose>
                        <c:when test="${currentStatus == 'Tiềm năng' || currentStatus == 'TIEM_NANG'}">
                            <span class="badge-status-tiem-nang">● Tiềm năng</span>
                        </c:when>
                        <c:when test="${currentStatus == 'Đang giao dịch' || currentStatus == 'DANG_GIAO_DICH'}">
                            <span class="badge-status-dang-giao-dich">● Đang giao dịch</span>
                        </c:when>
                        <c:when test="${currentStatus == 'Khách hàng' || currentStatus == 'KHACH_HANG'}">
                            <span class="badge-status-khach-hang">● Khách hàng</span>
                        </c:when>
                        <c:when test="${currentStatus == 'Ngừng hợp tác' || currentStatus == 'NGUNG_HOP_TAC'}">
                            <span class="badge-status-ngung-hop-tac">● Ngừng hợp tác</span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge badge-success"><c:out value="${currentStatus}" /></span>
                        </c:otherwise>
                    </c:choose>

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
                        <span class="detail-value font-mono"><c:out value="${not empty khachHang ? khachHang.maKhachHang : banGhi.maBanGhi}" /></span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Tên công ty / Khách hàng (AC1)</span>
                        <span class="detail-value" style="font-weight: 700; color: var(--slate-900);"><c:out value="${not empty khachHang ? khachHang.tenCongTy : banGhi.tieuDe}" /></span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Mã số thuế (AC1, AC2)</span>
                        <span class="detail-value font-mono">
                            <c:choose>
                                <c:when test="${not empty khachHang and not empty khachHang.maSoThue}">
                                    <c:out value="${khachHang.maSoThue}" />
                                </c:when>
                                <c:otherwise>
                                    <span style="color: var(--slate-400); font-style: italic;">Chưa khai báo</span>
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
                                    <a href="${khachHang.website.startsWith('http') ? khachHang.website : 'https://'.concat(khachHang.website)}" target="_blank" rel="noopener noreferrer" style="color: var(--primary);">
                                        <c:out value="${khachHang.website}" />
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <span style="color: var(--slate-400); font-style: italic;">Chưa khai báo</span>
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
                                <span style="color: var(--slate-400); font-style: italic;">Chưa có mô tả chi tiết cho hồ sơ khách hàng này.</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

            <div class="detail-footer">
                <span style="font-size: 13px; color: var(--slate-500);">
                    Chế độ: <strong>Hồ sơ khách hàng doanh nghiệp</strong> • Phân quyền Data Scope tự động (AC4).
                </span>
                <a href="${pageContext.request.contextPath}/khach-hang" class="btn btn-outline">
                    <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span>
                    <span>Quay lại danh sách khách hàng</span>
                </a>
            </div>
        </div>

        <!-- ========================================== -->
        <!-- STORY S3-02: QUẢN LÝ NGƯỜI LIÊN HỆ & VAI TRÒ TRONG QUYẾT ĐỊNH MUA -->
        <!-- ========================================== -->
        <div class="detail-card" id="cardNguoiLienHe">
            <div class="detail-header" style="background-color: #f8fafc;">
                <div class="detail-title-group">
                    <h2 style="font-size: 18px; font-weight: 800; color: var(--slate-900); display: flex; align-items: center; gap: 8px;">
                        <span class="material-symbols-outlined" style="color: var(--primary); font-size: 24px;" aria-hidden="true">group</span>
                        <span>Danh Sách Người Liên Hệ & Vai Trò Quyết Định (Story S3-02)</span>
                    </h2>
                    <p>Mỗi khách hàng có nhiều người liên hệ; đánh dấu vai trò quyết định mua, đầu mối chính và lịch sử chuyển công ty</p>
                </div>
                <div>
                    <button type="button" class="btn btn-primary" id="btnMoModalThemNlh" onclick="moModalThemNlh()" style="display: inline-flex; align-items: center; gap: 6px;">
                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">person_add</span>
                        <span>Thêm Người Liên Hệ</span>
                    </button>
                </div>
            </div>

            <!-- Bảng danh sách người liên hệ -->
            <div style="overflow-x: auto;">
                <table class="contact-table crm-table" id="tableNguoiLienHe">
                    <thead>
                        <tr>
                            <th style="min-width: 180px;">Họ và tên</th>
                            <th style="min-width: 150px;">Chức danh</th>
                            <th style="min-width: 170px;">Email</th>
                            <th style="min-width: 130px;">Số điện thoại</th>
                            <th style="text-align: center; min-width: 170px;">Vai trò quyết định mua (AC2)</th>
                            <th style="text-align: center; min-width: 120px;">Đầu mối chính (AC3)</th>
                            <th style="text-align: center; min-width: 120px;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody id="tbodyNguoiLienHe">
                        <c:choose>
                            <c:when test="${not empty dsNguoiLienHe}">
                                <c:forEach var="nlh" items="${dsNguoiLienHe}">
                                    <tr id="row-nlh-${nlh.id}">
                                        <td>
                                            <div class="contact-name-cell">
                                                <span><c:out value="${nlh.hoTen}" /></span>
                                                <c:if test="${nlh.laDauMoiChinh}">
                                                    <span class="badge-dau-moi-chinh" title="Đầu mối chính của khách hàng">
                                                        <span class="material-symbols-outlined" aria-hidden="true">star</span>
                                                        <span>Đầu mối chính</span>
                                                    </span>
                                                </c:if>
                                            </div>
                                        </td>
                                        <td style="color: var(--slate-700);">
                                            <c:out value="${not empty nlh.chucDanh ? nlh.chucDanh : '—'}" />
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty nlh.email}">
                                                    <a href="mailto:${nlh.email}" style="color: var(--primary); text-decoration: none;">
                                                        <c:out value="${nlh.email}" />
                                                    </a>
                                                </c:when>
                                                <c:otherwise><span style="color: var(--slate-400); font-style: italic;">—</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="font-family: monospace;">
                                            <c:choose>
                                                <c:when test="${not empty nlh.soDienThoai}">
                                                    <a href="tel:${nlh.soDienThoai}" style="color: #0284c7; text-decoration: none; font-weight: 500;">
                                                        <c:out value="${nlh.soDienThoai}" />
                                                    </a>
                                                </c:when>
                                                <c:otherwise><span style="color: var(--slate-400); font-style: italic;">—</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${nlh.vaiTroQuyetDinh != null}">
                                                    <c:choose>
                                                        <c:when test="${nlh.vaiTroQuyetDinh.ma == 'NGUOI_QUYET_DINH'}">
                                                            <span class="badge-vai-tro badge-quyet-dinh" title="Người quyết định cuối cùng (Decision Maker)">
                                                                <span class="material-symbols-outlined" aria-hidden="true">verified_user</span>
                                                                <span>Người quyết định</span>
                                                            </span>
                                                        </c:when>
                                                        <c:when test="${nlh.vaiTroQuyetDinh.ma == 'NGUOI_ANH_HUONG'}">
                                                            <span class="badge-vai-tro badge-anh-huong" title="Người có tiếng nói ảnh hưởng quan trọng (Influencer)">
                                                                <span class="material-symbols-outlined" aria-hidden="true">insights</span>
                                                                <span>Người ảnh hưởng</span>
                                                            </span>
                                                        </c:when>
                                                        <c:when test="${nlh.vaiTroQuyetDinh.ma == 'NGUOI_DUNG_CUOI'}">
                                                            <span class="badge-vai-tro badge-dung-cuoi" title="Người trực tiếp sử dụng sản phẩm dịch vụ (End User)">
                                                                <span class="material-symbols-outlined" aria-hidden="true">person</span>
                                                                <span>Người dùng cuối</span>
                                                            </span>
                                                        </c:when>
                                                        <c:when test="${nlh.vaiTroQuyetDinh.ma == 'NGUOI_CAN_TRO'}">
                                                            <span class="badge-vai-tro badge-can-tro" title="Người có khả năng cản trở thương vụ (Blocker)">
                                                                <span class="material-symbols-outlined" aria-hidden="true">block</span>
                                                                <span>Người cản trở</span>
                                                            </span>
                                                        </c:when>
                                                    </c:choose>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge-vai-tro badge-chua-xac-dinh">Chưa xác định</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${nlh.laDauMoiChinh}">
                                                    <button type="button" class="btn btn-sm btn-dau-moi-active" title="Bỏ đánh dấu đầu mối chính" aria-label="Bỏ đánh dấu đầu mối chính cho <c:out value="${nlh.hoTen}" />" onclick="doiDauMoiChinh(${nlh.id}, ${nlh.khachHangId}, false)">
                                                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">star</span>
                                                        <span>Đầu mối</span>
                                                    </button>
                                                </c:when>
                                                <c:otherwise>
                                                    <button type="button" class="btn btn-sm btn-outline btn-dau-moi-inactive" title="Đặt làm đầu mối chính của khách hàng" aria-label="Đặt <c:out value="${nlh.hoTen}" /> làm đầu mối chính" onclick="doiDauMoiChinh(${nlh.id}, ${nlh.khachHangId}, true)">
                                                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">star_outline</span>
                                                        <span>Đặt chính</span>
                                                    </button>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center;">
                                            <div class="btn-action-group">
                                                <button type="button" class="btn-action" title="Chỉnh sửa thông tin" aria-label="Chỉnh sửa thông tin người liên hệ <c:out value="${nlh.hoTen}" />"
                                                        onclick="moModalSuaNlh(${nlh.id}, '<c:out value="${nlh.hoTen}" />', '<c:out value="${nlh.chucDanh}" />', '<c:out value="${nlh.email}" />', '<c:out value="${nlh.soDienThoai}" />', '${nlh.vaiTroQuyetDinh != null ? nlh.vaiTroQuyetDinh.ma : ""}')">
                                                    <span class="material-symbols-outlined icon-sm" aria-hidden="true">edit</span>
                                                </button>
                                                <button type="button" class="btn-action btn-action-transfer" title="Chuyển sang công ty khác (AC4)" aria-label="Chuyển công ty cho <c:out value="${nlh.hoTen}" />"
                                                        onclick="moModalChuyenCongTy(${nlh.id}, '<c:out value="${nlh.hoTen}" />', '<c:out value="${nlh.chucDanh}" />', '${nlh.vaiTroQuyetDinh != null ? nlh.vaiTroQuyetDinh.ma : ""}')">
                                                    <span class="material-symbols-outlined icon-sm" aria-hidden="true">swap_horiz</span>
                                                </button>
                                                <button type="button" class="btn-action btn-action-history" title="Xem lịch sử làm việc (AC4)" aria-label="Xem lịch sử công tác của <c:out value="${nlh.hoTen}" />"
                                                        onclick="xemLichSuCongTy(${nlh.id}, '<c:out value="${nlh.hoTen}" />')">
                                                    <span class="material-symbols-outlined icon-sm" aria-hidden="true">history</span>
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="7" style="padding: 0;">
                                        <div class="empty-state-wrapper">
                                            <span class="material-symbols-outlined empty-state-icon" aria-hidden="true">contact_page</span>
                                            <div class="empty-state-title">Chưa có người liên hệ nào</div>
                                            <div class="empty-state-desc">Khách hàng này hiện chưa được khai báo người liên hệ. Hãy thêm người liên hệ để ghi nhận vai trò quyết định mua và đầu mối chính.</div>
                                            <button type="button" class="btn btn-primary btn-sm" onclick="moModalThemNlh()" style="margin-top: 14px;">
                                                <span class="material-symbols-outlined icon-xs" aria-hidden="true">person_add</span>
                                                <span>Thêm Người Liên Hệ</span>
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- ========================================== -->
        <!-- MODAL THÊM / SỬA NGƯỜI LIÊN HỆ (AC1, AC2, AC3) -->
        <!-- ========================================== -->
        <div class="modal-backdrop" id="modalNguoiLienHe" style="display: none;" role="dialog" aria-modal="true" aria-labelledby="modalNlhTitle">
            <div class="modal-card">
                <div class="modal-header">
                    <div>
                        <h2 class="modal-title" id="modalNlhTitle">Khai Báo Người Liên Hệ</h2>
                        <p class="modal-subtitle">Gán vai trò quyết định mua và đánh dấu đầu mối chính (Story S3-02)</p>
                    </div>
                    <button type="button" class="modal-close-btn" aria-label="Đóng" onclick="dongModalNlh()">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>
                <form id="formNguoiLienHe" method="POST" action="${pageContext.request.contextPath}/nguoi-lien-he" onsubmit="return validateFormNlh()">
                    <input type="hidden" name="action" id="nlhAction" value="create">
                    <input type="hidden" name="id" id="nlhId" value="">
                    <input type="hidden" name="khachHangId" id="nlhKhachHangId" value="${not empty khachHang ? khachHang.id : banGhi.id}">

                    <div class="modal-body">
                        <div class="form-group" style="margin-bottom: 14px;">
                            <label class="form-label" for="nlhHoTen">Họ và tên <span style="color: #ef4444;">*</span></label>
                            <input type="text" id="nlhHoTen" name="hoTen" class="form-input" placeholder="Ví dụ: Nguyễn Văn Hoàng" required autocomplete="off">
                            <span id="errNlhHoTen" style="color: #ef4444; font-size: 12px; display: none; margin-top: 4px;">Vui lòng nhập họ và tên người liên hệ.</span>
                        </div>

                        <div class="form-row" style="margin-bottom: 14px;">
                            <div class="form-col">
                                <label class="form-label" for="nlhChucDanh">Chức danh / Vị trí</label>
                                <input type="text" id="nlhChucDanh" name="chucDanh" class="form-input" placeholder="Ví dụ: Giám đốc CNTT (CIO)" autocomplete="off">
                            </div>
                            <div class="form-col">
                                <label class="form-label" for="nlhVaiTro">Vai trò quyết định mua (AC2)</label>
                                <select id="nlhVaiTro" name="vaiTroQuyetDinh" class="form-select">
                                    <option value="">-- Chưa xác định --</option>
                                    <option value="NGUOI_QUYET_DINH">Người quyết định (Decision Maker)</option>
                                    <option value="NGUOI_ANH_HUONG">Người ảnh hưởng (Influencer)</option>
                                    <option value="NGUOI_DUNG_CUOI">Người dùng cuối (End User)</option>
                                    <option value="NGUOI_CAN_TRO">Người cản trở (Blocker)</option>
                                </select>
                            </div>
                        </div>

                        <div class="form-row" style="margin-bottom: 14px;">
                            <div class="form-col">
                                <label class="form-label" for="nlhEmail">Địa chỉ Email</label>
                                <input type="email" id="nlhEmail" name="email" class="form-input" placeholder="hoang.nv@congty.com" autocomplete="off">
                                <span id="errNlhEmail" style="color: #ef4444; font-size: 12px; display: none; margin-top: 4px;">Email không đúng định dạng.</span>
                            </div>
                            <div class="form-col">
                                <label class="form-label" for="nlhSdt">Số điện thoại</label>
                                <input type="tel" id="nlhSdt" name="soDienThoai" class="form-input" placeholder="0912345678" autocomplete="off">
                            </div>
                        </div>

                        <div class="form-group" id="groupDauMoiChinh" style="margin-top: 10px;">
                            <label style="display: flex; align-items: center; gap: 8px; cursor: pointer; font-size: 14px; font-weight: 500; color: #1e293b;">
                                <input type="checkbox" id="nlhLaDauMoi" name="laDauMoiChinh" value="true" style="width: 17px; height: 17px;">
                                <span>Đánh dấu là đầu mối chính của khách hàng (AC3)</span>
                            </label>
                            <small style="color: #64748b; display: block; margin-left: 25px; margin-top: 3px;">
                                Mỗi khách hàng chỉ có duy nhất 1 đầu mối chính. Đánh dấu người này sẽ tự động thay thế đầu mối cũ.
                            </small>
                        </div>
                    </div>

                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline" onclick="dongModalNlh()">Hủy</button>
                        <button type="submit" class="btn btn-primary" id="btnLuuNlh">Lưu Người Liên Hệ</button>
                    </div>
                </form>
            </div>
        </div>

        <!-- ========================================== -->
        <!-- MODAL CHUYỂN CÔNG TY CHO LIÊN HỆ (AC4) -->
        <!-- ========================================== -->
        <div class="modal-backdrop" id="modalChuyenCongTy" style="display: none;" role="dialog" aria-modal="true" aria-labelledby="modalChuyenCongTyTitle">
            <div class="modal-card">
                <div class="modal-header">
                    <div>
                        <h2 class="modal-title" id="modalChuyenCongTyTitle">Chuyển Người Liên Hệ Sang Công Ty Mới</h2>
                        <p class="modal-subtitle">Gắn sang khách hàng mới và lưu toàn bộ lịch sử công tác (Story S3-02 AC4)</p>
                    </div>
                    <button type="button" class="modal-close-btn" aria-label="Đóng" onclick="dongModalChuyenCongTy()">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>
                <form id="formChuyenCongTy" method="POST" action="${pageContext.request.contextPath}/nguoi-lien-he" onsubmit="return validateFormChuyenCongTy()">
                    <input type="hidden" name="action" value="transfer-company">
                    <input type="hidden" name="id" id="chuyenNlhId" value="">

                    <div class="modal-body">
                        <div style="background: #eff6ff; border: 1px solid #bfdbfe; border-radius: 8px; padding: 12px 16px; margin-bottom: 16px;">
                            <span style="font-size: 13.5px; color: #1e40af;">
                                Đang chuyển: <strong id="chuyenTenNlh"></strong> (Hiện tại: <c:out value="${not empty khachHang ? khachHang.tenCongTy : banGhi.tieuDe}" />)
                            </span>
                        </div>

                        <div class="form-group" style="margin-bottom: 14px;">
                            <label class="form-label" for="chuyenKhachHangMoi">Chọn công ty / Khách hàng mới <span style="color: #ef4444;">*</span></label>
                            <select id="chuyenKhachHangMoi" name="khachHangMoiId" class="form-select" required>
                                <option value="">-- Chọn khách hàng đích --</option>
                                <c:forEach var="kh" items="${dsKhachHangChuyen}">
                                    <c:if test="${kh.id != (not empty khachHang ? khachHang.id : banGhi.id)}">
                                        <option value="${kh.id}">
                                            <c:out value="${kh.tenCongTy}" /> (<c:out value="${kh.maKhachHang}" />) - Phụ trách: <c:out value="${kh.tenNguoiSoHuu}" />
                                        </option>
                                    </c:if>
                                </c:forEach>
                            </select>
                            <span id="errChuyenKh" style="color: #ef4444; font-size: 12px; display: none; margin-top: 4px;">Vui lòng chọn khách hàng mới.</span>
                        </div>

                        <div class="form-row" style="margin-bottom: 14px;">
                            <div class="form-col">
                                <label class="form-label" for="chuyenChucDanhMoi">Chức danh tại công ty mới</label>
                                <input type="text" id="chuyenChucDanhMoi" name="chucDanhMoi" class="form-input" placeholder="Ví dụ: Giám đốc Mua hàng">
                            </div>
                            <div class="form-col">
                                <label class="form-label" for="chuyenVaiTroMoi">Vai trò quyết định mới (AC2)</label>
                                <select id="chuyenVaiTroMoi" name="vaiTroMoi" class="form-select">
                                    <option value="">-- Giữ nguyên / Chưa xác định --</option>
                                    <option value="NGUOI_QUYET_DINH">Người quyết định</option>
                                    <option value="NGUOI_ANH_HUONG">Người ảnh hưởng</option>
                                    <option value="NGUOI_DUNG_CUOI">Người dùng cuối</option>
                                    <option value="NGUOI_CAN_TRO">Người cản trở</option>
                                </select>
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="form-label" for="chuyenGhiChu">Ghi chú chuyển công tác</label>
                            <textarea id="chuyenGhiChu" name="ghiChu" class="form-textarea" rows="2" placeholder="Ví dụ: Chuyển công tác từ tháng 10/2026 sang phụ trách mảng mua sắm của công ty mới..."></textarea>
                        </div>
                    </div>

                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline" onclick="dongModalChuyenCongTy()">Hủy</button>
                        <button type="submit" class="btn btn-primary" id="btnXacNhanChuyenCongTy" style="display: inline-flex; align-items: center; gap: 6px;">
                            <span class="material-symbols-outlined icon-xs" aria-hidden="true">swap_horiz</span>
                            <span>Xác Nhận Chuyển Công Ty</span>
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <!-- ========================================== -->
        <!-- MODAL LỊCH SỬ CÔNG TY CỦA LIÊN HỆ (AC4) -->
        <!-- ========================================== -->
        <div class="modal-backdrop" id="modalLichSuCongTy" style="display: none;" role="dialog" aria-modal="true" aria-labelledby="modalLichSuTitle">
            <div class="modal-card" style="max-width: 650px;">
                <div class="modal-header">
                    <div>
                        <h2 class="modal-title" id="modalLichSuTitle">Lịch Sử Công Tác Qua Các Công Ty</h2>
                        <p class="modal-subtitle" id="modalLichSuSubtitle">Dòng thời gian làm việc (Story S3-02 AC4)</p>
                    </div>
                    <button type="button" class="modal-close-btn" aria-label="Đóng" onclick="dongModalLichSu()">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>
                <div class="modal-body">
                    <div id="loadingLichSu" class="timeline-loading">
                        <span class="material-symbols-outlined spin" aria-hidden="true">sync</span>
                        <span>Đang nạp dữ liệu lịch sử công tác...</span>
                    </div>
                    <div id="timelineLichSu" style="display: none;">
                        <ul class="timeline-container" id="timelineList">
                            <!-- Được fill động bằng JavaScript -->
                        </ul>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline" onclick="dongModalLichSu()">Đóng</button>
                </div>
            </div>
        </div>
    </main>

    <script>
        function moModalThemNlh() {
            document.getElementById('modalNlhTitle').innerText = 'Thêm Người Liên Hệ Mới';
            document.getElementById('nlhAction').value = 'create';
            document.getElementById('nlhId').value = '';
            document.getElementById('nlhHoTen').value = '';
            document.getElementById('nlhChucDanh').value = '';
            document.getElementById('nlhEmail').value = '';
            document.getElementById('nlhSdt').value = '';
            document.getElementById('nlhVaiTro').value = '';
            document.getElementById('nlhLaDauMoi').checked = false;
            document.getElementById('groupDauMoiChinh').style.display = 'block';
            anLoiFormNlh();
            hienThiModal('modalNguoiLienHe');
            document.getElementById('nlhHoTen').focus();
        }

        function moModalSuaNlh(id, hoTen, chucDanh, email, sdt, vaiTro) {
            document.getElementById('modalNlhTitle').innerText = 'Chỉnh Sửa Người Liên Hệ';
            document.getElementById('nlhAction').value = 'update';
            document.getElementById('nlhId').value = id;
            document.getElementById('nlhHoTen').value = hoTen;
            document.getElementById('nlhChucDanh').value = chucDanh;
            document.getElementById('nlhEmail').value = email;
            document.getElementById('nlhSdt').value = sdt;
            document.getElementById('nlhVaiTro').value = vaiTro;
            document.getElementById('groupDauMoiChinh').style.display = 'none'; // Sửa cờ đầu mối qua nút chuyên dụng
            anLoiFormNlh();
            hienThiModal('modalNguoiLienHe');
            document.getElementById('nlhHoTen').focus();
        }

        function dongModalNlh() {
            anModal('modalNguoiLienHe');
        }

        function anLoiFormNlh() {
            var errName = document.getElementById('errNlhHoTen');
            if (errName) errName.style.display = 'none';
            var errEmail = document.getElementById('errNlhEmail');
            if (errEmail) errEmail.style.display = 'none';
        }

        function validateFormNlh() {
            var hoTen = document.getElementById('nlhHoTen').value.trim();
            var email = document.getElementById('nlhEmail').value.trim();
            var hopLe = true;

            if (!hoTen) {
                var errName = document.getElementById('errNlhHoTen');
                if (errName) errName.style.display = 'block';
                document.getElementById('nlhHoTen').focus();
                hopLe = false;
            } else {
                var errName = document.getElementById('errNlhHoTen');
                if (errName) errName.style.display = 'none';
            }

            if (email) {
                var emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                if (!emailRegex.test(email)) {
                    var errEmail = document.getElementById('errNlhEmail');
                    if (errEmail) errEmail.style.display = 'block';
                    hopLe = false;
                } else {
                    var errEmail = document.getElementById('errNlhEmail');
                    if (errEmail) errEmail.style.display = 'none';
                }
            }

            return hopLe;
        }

        function doiDauMoiChinh(nlhId, khachHangId, datChinh) {
            var actionName = datChinh ? 'set-main' : 'unset-main';
            var form = document.createElement('form');
            form.method = 'POST';
            form.action = '${pageContext.request.contextPath}/nguoi-lien-he';
            
            var inpAction = document.createElement('input');
            inpAction.type = 'hidden';
            inpAction.name = 'action';
            inpAction.value = actionName;
            form.appendChild(inpAction);

            var inpId = document.createElement('input');
            inpId.type = 'hidden';
            inpId.name = 'id';
            inpId.value = nlhId;
            form.appendChild(inpId);

            var inpKhId = document.createElement('input');
            inpKhId.type = 'hidden';
            inpKhId.name = 'khachHangId';
            inpKhId.value = khachHangId;
            form.appendChild(inpKhId);

            document.body.appendChild(form);
            form.submit();
        }

        function moModalChuyenCongTy(id, hoTen, chucDanh, vaiTro) {
            document.getElementById('chuyenNlhId').value = id;
            document.getElementById('chuyenTenNlh').innerText = hoTen;
            document.getElementById('chuyenChucDanhMoi').value = chucDanh;
            document.getElementById('chuyenVaiTroMoi').value = vaiTro;
            document.getElementById('chuyenKhachHangMoi').value = '';
            document.getElementById('chuyenGhiChu').value = '';
            var err = document.getElementById('errChuyenKh');
            if (err) err.style.display = 'none';
            hienThiModal('modalChuyenCongTy');
        }

        function dongModalChuyenCongTy() {
            anModal('modalChuyenCongTy');
        }

        function validateFormChuyenCongTy() {
            var khMoi = document.getElementById('chuyenKhachHangMoi').value;
            if (!khMoi) {
                var err = document.getElementById('errChuyenKh');
                if (err) err.style.display = 'block';
                return false;
            }
            return true;
        }

        function xemLichSuCongTy(id, hoTen) {
            document.getElementById('modalLichSuSubtitle').innerText = 'Lịch sử công tác của: ' + hoTen;
            document.getElementById('loadingLichSu').style.display = 'flex';
            document.getElementById('timelineLichSu').style.display = 'none';
            hienThiModal('modalLichSuCongTy');

            fetch('${pageContext.request.contextPath}/nguoi-lien-he?action=lich-su&id=' + id, {
                headers: { 'Accept': 'application/json' }
            })
            .then(function(res) { return res.json(); })
            .then(function(json) {
                document.getElementById('loadingLichSu').style.display = 'none';
                var ul = document.getElementById('timelineList');
                ul.innerHTML = '';
                if (json.success && json.data && json.data.length > 0) {
                    json.data.forEach(function(item) {
                        var li = document.createElement('li');
                        li.className = 'timeline-item';

                        var isCurrent = (item.denNgay === 'Hiện tại');
                        var dotClass = isCurrent ? 'timeline-dot current' : 'timeline-dot';
                        var periodBadgeClass = isCurrent ? 'timeline-period-badge current' : 'timeline-period-badge past';

                        var roleBadgeHtml = '';
                        if (item.vaiTroQuyetDinh) {
                            var rCode = item.vaiTroQuyetDinh;
                            if (rCode === 'NGUOI_QUYET_DINH') {
                                roleBadgeHtml = '<span class="badge-vai-tro badge-quyet-dinh"><span class="material-symbols-outlined" aria-hidden="true">verified_user</span>' + escapeHtml(item.tenVaiTro || 'Người quyết định') + '</span>';
                            } else if (rCode === 'NGUOI_ANH_HUONG') {
                                roleBadgeHtml = '<span class="badge-vai-tro badge-anh-huong"><span class="material-symbols-outlined" aria-hidden="true">insights</span>' + escapeHtml(item.tenVaiTro || 'Người ảnh hưởng') + '</span>';
                            } else if (rCode === 'NGUOI_DUNG_CUOI') {
                                roleBadgeHtml = '<span class="badge-vai-tro badge-dung-cuoi"><span class="material-symbols-outlined" aria-hidden="true">person</span>' + escapeHtml(item.tenVaiTro || 'Người dùng cuối') + '</span>';
                            } else if (rCode === 'NGUOI_CAN_TRO') {
                                roleBadgeHtml = '<span class="badge-vai-tro badge-can-tro"><span class="material-symbols-outlined" aria-hidden="true">block</span>' + escapeHtml(item.tenVaiTro || 'Người cản trở') + '</span>';
                            } else {
                                roleBadgeHtml = '<span class="badge-vai-tro badge-chua-xac-dinh">' + escapeHtml(item.tenVaiTro || 'Chưa xác định') + '</span>';
                            }
                        } else {
                            roleBadgeHtml = '<span class="badge-vai-tro badge-chua-xac-dinh">Chưa xác định</span>';
                        }

                        var html = '<span class="' + dotClass + '"></span>' +
                            '<div class="timeline-card">' +
                                '<div class="timeline-header">' +
                                    '<span class="timeline-company-name">' + escapeHtml(item.tenCongTy) + '</span>' +
                                    '<span class="timeline-period">' +
                                        '<span>' + (item.tuNgay || '...') + ' &rarr; </span>' +
                                        '<span class="' + periodBadgeClass + '">' + escapeHtml(item.denNgay) + '</span>' +
                                    '</span>' +
                                '</div>' +
                                '<div class="timeline-position">' +
                                    '<span>Chức danh: <strong>' + escapeHtml(item.chucDanh || '—') + '</strong></span>' +
                                    '<span style="color: #94a3b8;">&bull;</span>' +
                                    '<span>Vai trò: </span>' + roleBadgeHtml +
                                '</div>';

                        if (item.ghiChu) {
                            html += '<div class="timeline-note">' + escapeHtml(item.ghiChu) + '</div>';
                        }

                        html += '</div>';
                        li.innerHTML = html;
                        ul.appendChild(li);
                    });
                } else {
                    ul.innerHTML = '<li style="color: #94a3b8; font-style: italic; padding: 12px 0;">Chưa ghi nhận lịch sử công ty nào cho người liên hệ này.</li>';
                }
                document.getElementById('timelineLichSu').style.display = 'block';
            })
            .catch(function(err) {
                document.getElementById('loadingLichSu').style.display = 'none';
                document.getElementById('timelineLichSu').style.display = 'block';
                document.getElementById('timelineList').innerHTML = '<li style="color: #ef4444; padding: 12px 0;">Lỗi tải lịch sử công tác: ' + escapeHtml(err.message) + '</li>';
            });
        }

        function dongModalLichSu() {
            anModal('modalLichSuCongTy');
        }

        function hienThiModal(modalId) {
            var el = document.getElementById(modalId);
            if (el) {
                el.style.display = 'flex';
                document.body.style.overflow = 'hidden';
            }
        }

        function anModal(modalId) {
            var el = document.getElementById(modalId);
            if (el) {
                el.style.display = 'none';
                document.body.style.overflow = '';
            }
        }

        // Bắt sự kiện phím ESC và click ra ngoài backdrop để đóng modal
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                ['modalNguoiLienHe', 'modalChuyenCongTy', 'modalLichSuCongTy'].forEach(function(id) {
                    var m = document.getElementById(id);
                    if (m && m.style.display === 'flex') {
                        anModal(id);
                    }
                });
            }
        });

        ['modalNguoiLienHe', 'modalChuyenCongTy', 'modalLichSuCongTy'].forEach(function(id) {
            var m = document.getElementById(id);
            if (m) {
                m.addEventListener('click', function(e) {
                    if (e.target === m) {
                        anModal(id);
                    }
                });
            }
        });

        function escapeHtml(text) {
            if (!text) return '';
            var map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
            return text.replace(/[&<>"']/g, function(m) { return map[m]; });
        }
    </script>
</body>
</html>
