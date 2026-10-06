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
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"></polyline></svg>
                <span><c:out value="${thongBaoThanhCong}" /></span>
            </div>
        </c:if>

        <!-- Thẻ chi tiết bản ghi -->
        <div class="detail-card">
            <div class="detail-header">
                <div class="detail-title-group">
                    <h1>
                        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color: var(--primary);">
                            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                            <circle cx="12" cy="7" r="4"></circle>
                        </svg>
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
                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                            <line x1="19" y1="12" x2="5" y2="12"></line>
                            <polyline points="12 19 5 12 12 5"></polyline>
                        </svg>
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
                    &larr; Quay lại danh sách khách hàng
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
                        <span class="material-symbols-outlined" style="color: #6366f1; font-size: 24px;" aria-hidden="true">group</span>
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
                <table class="crm-table" style="width: 100%; border-collapse: collapse; font-size: 13.5px;">
                    <thead>
                        <tr style="background: #f1f5f9; border-bottom: 2px solid #e2e8f0; text-align: left; color: #475569;">
                            <th style="padding: 12px 16px;">Họ và tên</th>
                            <th style="padding: 12px 16px;">Chức danh</th>
                            <th style="padding: 12px 16px;">Email</th>
                            <th style="padding: 12px 16px;">Số điện thoại</th>
                            <th style="padding: 12px 16px; text-align: center;">Vai trò quyết định mua (AC2)</th>
                            <th style="padding: 12px 16px; text-align: center;">Đầu mối chính (AC3)</th>
                            <th style="padding: 12px 16px; text-align: center;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody id="tbodyNguoiLienHe">
                        <c:choose>
                            <c:when test="${not empty dsNguoiLienHe}">
                                <c:forEach var="nlh" items="${dsNguoiLienHe}">
                                    <tr style="border-bottom: 1px solid #f1f5f9;" id="row-nlh-${nlh.id}">
                                        <td style="padding: 12px 16px; font-weight: 600; color: #1e293b;">
                                            <div style="display: flex; align-items: center; gap: 8px;">
                                                <span><c:out value="${nlh.hoTen}" /></span>
                                                <c:if test="${nlh.laDauMoiChinh}">
                                                    <span title="Đầu mối chính của khách hàng" style="display: inline-flex; align-items: center; gap: 3px; background: #fef3c7; color: #b45309; padding: 2px 7px; border-radius: 9999px; font-size: 11px; font-weight: 700; border: 1px solid #fde68a;">
                                                        ★ Đầu mối chính
                                                    </span>
                                                </c:if>
                                            </div>
                                        </td>
                                        <td style="padding: 12px 16px; color: #334155;">
                                            <c:out value="${not empty nlh.chucDanh ? nlh.chucDanh : '—'}" />
                                        </td>
                                        <td style="padding: 12px 16px;">
                                            <c:choose>
                                                <c:when test="${not empty nlh.email}">
                                                    <a href="mailto:${nlh.email}" style="color: var(--primary); text-decoration: none;">
                                                        <c:out value="${nlh.email}" />
                                                    </a>
                                                </c:when>
                                                <c:otherwise><span style="color: #94a3b8; font-style: italic;">—</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="padding: 12px 16px; font-family: monospace;">
                                            <c:choose>
                                                <c:when test="${not empty nlh.soDienThoai}">
                                                    <a href="tel:${nlh.soDienThoai}" style="color: #0284c7; text-decoration: none; font-weight: 500;">
                                                        <c:out value="${nlh.soDienThoai}" />
                                                    </a>
                                                </c:when>
                                                <c:otherwise><span style="color: #94a3b8; font-style: italic;">—</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="padding: 12px 16px; text-align: center;">
                                            <c:choose>
                                                <c:when test="${nlh.vaiTroQuyetDinh != null}">
                                                    <c:choose>
                                                        <c:when test="${nlh.vaiTroQuyetDinh.ma == 'NGUOI_QUYET_DINH'}">
                                                            <span style="background: #e0e7ff; color: #3730a3; padding: 4px 10px; border-radius: 9999px; font-size: 12px; font-weight: 600; display: inline-block;">
                                                                👑 Người quyết định
                                                            </span>
                                                        </c:when>
                                                        <c:when test="${nlh.vaiTroQuyetDinh.ma == 'NGUOI_ANH_HUONG'}">
                                                            <span style="background: #e0f2fe; color: #0369a1; padding: 4px 10px; border-radius: 9999px; font-size: 12px; font-weight: 600; display: inline-block;">
                                                                ⚡ Người ảnh hưởng
                                                            </span>
                                                        </c:when>
                                                        <c:when test="${nlh.vaiTroQuyetDinh.ma == 'NGUOI_DUNG_CUOI'}">
                                                            <span style="background: #dcfce7; color: #15803d; padding: 4px 10px; border-radius: 9999px; font-size: 12px; font-weight: 600; display: inline-block;">
                                                                👤 Người dùng cuối
                                                            </span>
                                                        </c:when>
                                                        <c:when test="${nlh.vaiTroQuyetDinh.ma == 'NGUOI_CAN_TRO'}">
                                                            <span style="background: #fee2e2; color: #991b1b; padding: 4px 10px; border-radius: 9999px; font-size: 12px; font-weight: 600; display: inline-block;">
                                                                🛑 Người cản trở
                                                            </span>
                                                        </c:when>
                                                    </c:choose>
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="color: #94a3b8; font-style: italic;">Chưa xác định</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="padding: 12px 16px; text-align: center;">
                                            <c:choose>
                                                <c:when test="${nlh.laDauMoiChinh}">
                                                    <button type="button" class="btn btn-sm btn-outline" style="color: #b45309; border-color: #fde68a; background: #fffbeb;" title="Bỏ đầu mối chính" onclick="doiDauMoiChinh(${nlh.id}, ${nlh.khachHangId}, false)">
                                                        ★ Chính
                                                    </button>
                                                </c:when>
                                                <c:otherwise>
                                                    <button type="button" class="btn btn-sm btn-outline" style="color: #64748b;" title="Đặt làm đầu mối chính" onclick="doiDauMoiChinh(${nlh.id}, ${nlh.khachHangId}, true)">
                                                        ☆ Đặt chính
                                                    </button>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="padding: 12px 16px; text-align: center;">
                                            <div style="display: flex; gap: 6px; justify-content: center; align-items: center;">
                                                <button type="button" class="btn-action" title="Chỉnh sửa thông tin"
                                                        onclick="moModalSuaNlh(${nlh.id}, '<c:out value="${nlh.hoTen}" />', '<c:out value="${nlh.chucDanh}" />', '<c:out value="${nlh.email}" />', '<c:out value="${nlh.soDienThoai}" />', '${nlh.vaiTroQuyetDinh != null ? nlh.vaiTroQuyetDinh.ma : ""}')">
                                                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">edit</span>
                                                </button>
                                                <button type="button" class="btn-action" title="Chuyển sang công ty khác (AC4)" style="color: #0284c7;"
                                                        onclick="moModalChuyenCongTy(${nlh.id}, '<c:out value="${nlh.hoTen}" />', '<c:out value="${nlh.chucDanh}" />', '${nlh.vaiTroQuyetDinh != null ? nlh.vaiTroQuyetDinh.ma : ""}')">
                                                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">swap_horiz</span>
                                                </button>
                                                <button type="button" class="btn-action" title="Xem lịch sử làm việc (AC4)" style="color: #6366f1;"
                                                        onclick="xemLichSuCongTy(${nlh.id}, '<c:out value="${nlh.hoTen}" />')">
                                                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">history</span>
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="7" style="text-align: center; padding: 28px; color: #94a3b8; font-style: italic;">
                                        Chưa có người liên hệ nào cho khách hàng này. Nhấn "Thêm Người Liên Hệ" để bắt đầu khai báo.
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
            <div class="modal-card" style="max-width: 580px; width: 95%;">
                <div class="modal-header">
                    <div>
                        <h2 class="modal-title" id="modalNlhTitle">Khai Báo Người Liên Hệ</h2>
                        <p class="modal-subtitle">Gán vai trò quyết định mua và đánh dấu đầu mối chính (S3-02)</p>
                    </div>
                    <button type="button" class="modal-close-btn" onclick="dongModalNlh()">&times;</button>
                </div>
                <form id="formNguoiLienHe" method="POST" action="${pageContext.request.contextPath}/nguoi-lien-he">
                    <input type="hidden" name="action" id="nlhAction" value="create">
                    <input type="hidden" name="id" id="nlhId" value="">
                    <input type="hidden" name="khachHangId" id="nlhKhachHangId" value="${not empty khachHang ? khachHang.id : banGhi.id}">

                    <div class="modal-body">
                        <div class="form-group" style="margin-bottom: 14px;">
                            <label class="form-label" for="nlhHoTen">Họ và tên <span style="color: #ef4444;">*</span></label>
                            <input type="text" id="nlhHoTen" name="hoTen" class="form-input" placeholder="Ví dụ: Nguyễn Văn Hoàng" required autocomplete="off">
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
                                    <option value="NGUOI_QUYET_DINH">👑 Người quyết định (Decision Maker)</option>
                                    <option value="NGUOI_ANH_HUONG">⚡ Người ảnh hưởng (Influencer)</option>
                                    <option value="NGUOI_DUNG_CUOI">👤 Người dùng cuối (End User)</option>
                                    <option value="NGUOI_CAN_TRO">🛑 Người cản trở (Blocker)</option>
                                </select>
                            </div>
                        </div>

                        <div class="form-row" style="margin-bottom: 14px;">
                            <div class="form-col">
                                <label class="form-label" for="nlhEmail">Địa chỉ Email</label>
                                <input type="email" id="nlhEmail" name="email" class="form-input" placeholder="hoang.nv@congty.com" autocomplete="off">
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
            <div class="modal-card" style="max-width: 580px; width: 95%;">
                <div class="modal-header">
                    <div>
                        <h2 class="modal-title" id="modalChuyenCongTyTitle">Chuyển Người Liên Hệ Sang Công Ty Mới</h2>
                        <p class="modal-subtitle">Gắn sang khách hàng mới và lưu toàn bộ lịch sử công tác (Story S3-02 AC4)</p>
                    </div>
                    <button type="button" class="modal-close-btn" onclick="dongModalChuyenCongTy()">&times;</button>
                </div>
                <form id="formChuyenCongTy" method="POST" action="${pageContext.request.contextPath}/nguoi-lien-he">
                    <input type="hidden" name="action" value="transfer-company">
                    <input type="hidden" name="id" id="chuyenNlhId" value="">

                    <div class="modal-body">
                        <div style="background: #eff6ff; border: 1px solid #bfdbfe; border-radius: 6px; padding: 12px; margin-bottom: 16px;">
                            <span style="font-size: 13px; color: #1e40af;">
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
                                    <option value="NGUOI_QUYET_DINH">👑 Người quyết định</option>
                                    <option value="NGUOI_ANH_HUONG">⚡ Người ảnh hưởng</option>
                                    <option value="NGUOI_DUNG_CUOI">👤 Người dùng cuối</option>
                                    <option value="NGUOI_CAN_TRO">🛑 Người cản trở</option>
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
                        <button type="submit" class="btn btn-primary" style="background: #0284c7; border-color: #0284c7;">
                            Xác Nhận Chuyển Công Ty
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <!-- ========================================== -->
        <!-- MODAL LỊCH SỬ CÔNG TY CỦA LIÊN HỆ (AC4) -->
        <!-- ========================================== -->
        <div class="modal-backdrop" id="modalLichSuCongTy" style="display: none;" role="dialog" aria-modal="true" aria-labelledby="modalLichSuTitle">
            <div class="modal-card" style="max-width: 650px; width: 95%;">
                <div class="modal-header">
                    <div>
                        <h2 class="modal-title" id="modalLichSuTitle">Lịch Sử Công Tác Qua Các Công Ty</h2>
                        <p class="modal-subtitle" id="modalLichSuSubtitle">Dòng thời gian làm việc (Story S3-02 AC4)</p>
                    </div>
                    <button type="button" class="modal-close-btn" onclick="dongModalLichSu()">&times;</button>
                </div>
                <div class="modal-body">
                    <div id="loadingLichSu" style="text-align: center; padding: 24px; color: #64748b;">
                        Đang nạp dữ liệu lịch sử...
                    </div>
                    <div id="timelineLichSu" style="display: none;">
                        <ul style="list-style: none; padding-left: 20px; border-left: 2px solid #e2e8f0; margin: 10px 0;">
                            <!-- Sẽ được fill bằng JavaScript -->
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
            document.getElementById('modalNguoiLienHe').style.display = 'flex';
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
            document.getElementById('modalNguoiLienHe').style.display = 'flex';
        }

        function dongModalNlh() {
            document.getElementById('modalNguoiLienHe').style.display = 'none';
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
            document.getElementById('chuyenGhiChu').value = '';
            document.getElementById('modalChuyenCongTy').style.display = 'flex';
        }

        function dongModalChuyenCongTy() {
            document.getElementById('modalChuyenCongTy').style.display = 'none';
        }

        function xemLichSuCongTy(id, hoTen) {
            document.getElementById('modalLichSuSubtitle').innerText = 'Lịch sử công tác của: ' + hoTen;
            document.getElementById('loadingLichSu').style.display = 'block';
            document.getElementById('timelineLichSu').style.display = 'none';
            document.getElementById('modalLichSuCongTy').style.display = 'flex';

            fetch('${pageContext.request.contextPath}/nguoi-lien-he?action=lich-su&id=' + id, {
                headers: { 'Accept': 'application/json' }
            })
            .then(function(res) { return res.json(); })
            .then(function(json) {
                document.getElementById('loadingLichSu').style.display = 'none';
                var ul = document.querySelector('#timelineLichSu ul');
                ul.innerHTML = '';
                if (json.success && json.data && json.data.length > 0) {
                    json.data.forEach(function(item) {
                        var li = document.createElement('li');
                        li.style.position = 'relative';
                        li.style.marginBottom = '20px';
                        li.style.paddingLeft = '15px';

                        var dot = '<span style="position: absolute; left: -26px; top: 3px; width: 12px; height: 12px; border-radius: 50%; background: ' + (item.denNgay === 'Hiện tại' ? '#10b981' : '#64748b') + '; border: 2px solid #fff; box-shadow: 0 0 0 2px #cbd5e1;"></span>';

                        var html = dot +
                            '<div style="font-weight: 700; font-size: 14.5px; color: #1e293b;">' + escapeHtml(item.tenCongTy) + '</div>' +
                            '<div style="font-size: 13px; color: #475569; margin-top: 2px;">' +
                                'Chức danh: <strong>' + escapeHtml(item.chucDanh || '—') + '</strong> &bull; Vai trò: <span style="font-weight:600;">' + escapeHtml(item.tenVaiTro || 'Chưa xác định') + '</span>' +
                            '</div>' +
                            '<div style="font-size: 12px; color: #64748b; margin-top: 4px;">' +
                                'Thời gian: ' + (item.tuNgay || '...') + ' &rarr; <span style="font-weight: 600; color: ' + (item.denNgay === 'Hiện tại' ? '#15803d' : '#475569') + ';">' + item.denNgay + '</span>' +
                            '</div>';
                        if (item.ghiChu) {
                            html += '<div style="font-size: 12.5px; color: #64748b; font-style: italic; margin-top: 4px; background: #f8fafc; padding: 6px 10px; border-radius: 4px; border: 1px solid #f1f5f9;">' +
                                        escapeHtml(item.ghiChu) +
                                    '</div>';
                        }
                        li.innerHTML = html;
                        ul.appendChild(li);
                    });
                } else {
                    ul.innerHTML = '<li style="color: #94a3b8; font-style: italic;">Chưa ghi nhận lịch sử công ty nào.</li>';
                }
                document.getElementById('timelineLichSu').style.display = 'block';
            })
            .catch(function(err) {
                document.getElementById('loadingLichSu').style.display = 'none';
                document.getElementById('timelineLichSu').style.display = 'block';
                document.querySelector('#timelineLichSu ul').innerHTML = '<li style="color: #ef4444;">Lỗi tải lịch sử công ty: ' + err.message + '</li>';
            });
        }

        function dongModalLichSu() {
            document.getElementById('modalLichSuCongTy').style.display = 'none';
        }

        function escapeHtml(text) {
            if (!text) return '';
            var map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
            return text.replace(/[&<>"']/g, function(m) { return map[m]; });
        }
    </script>
</body>
</html>
