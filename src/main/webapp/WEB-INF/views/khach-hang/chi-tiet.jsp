<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Trang 360 Khách hàng - Hệ thống CRM Bán Hàng">
    <title>Trang 360 Khách Hàng - CRM Bán Hàng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/khach-hang/khach-hang.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <style>
        .breadcrumb-nav {
            margin-bottom: 20px;
        }
        .breadcrumb-list {
            display: flex;
            align-items: center;
            list-style: none;
            gap: 8px;
            font-size: 13.5px;
            color: var(--slate-500);
            flex-wrap: wrap;
        }
        .breadcrumb-item a {
            color: var(--primary);
            text-decoration: none;
            font-weight: 500;
        }
        .breadcrumb-item a:hover {
            text-decoration: underline;
        }
        .breadcrumb-separator {
            color: var(--slate-400);
        }
        .breadcrumb-item.active {
            color: var(--slate-700);
            font-weight: 600;
        }
        .detail-card {
            background: #ffffff;
            border-radius: var(--radius-lg);
            border: 1px solid var(--border-color);
            box-shadow: var(--shadow-sm);
            overflow: hidden;
            margin-bottom: 24px;
        }
        .detail-header {
            padding: 20px 24px;
            border-bottom: 1px solid var(--border-color);
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 16px;
            background-color: #ffffff;
        }
        .detail-title-group h1 {
            font-size: 20px;
            font-weight: 800;
            color: var(--slate-900);
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .detail-title-group p {
            font-size: 13px;
            color: var(--slate-500);
            margin-top: 4px;
        }
        .detail-body {
            padding: 24px;
        }
        .detail-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
            gap: 20px;
            margin-bottom: 24px;
        }
        .detail-item {
            display: flex;
            flex-direction: column;
            gap: 6px;
        }
        .detail-label {
            font-size: 12.5px;
            font-weight: 600;
            color: var(--slate-500);
            text-transform: uppercase;
            letter-spacing: 0.03em;
        }
        .detail-value {
            font-size: 15px;
            color: var(--slate-900);
            font-weight: 500;
            word-break: break-word;
        }
        .detail-value.font-mono {
            font-family: monospace;
            font-size: 14.5px;
            color: var(--slate-700);
        }
        .detail-desc-box {
            background-color: var(--slate-50);
            border: 1px solid var(--slate-200);
            border-radius: var(--radius-md);
            padding: 16px;
            color: var(--slate-700);
            font-size: 14px;
            line-height: 1.6;
            white-space: pre-wrap;
            min-height: 80px;
        }
        .detail-footer {
            padding: 16px 24px;
            background-color: var(--slate-50);
            border-top: 1px solid var(--border-color);
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 12px;
        }

        /* Styles Cờ Cảnh Báo Rủi Ro Rời Bỏ (Story S3-08 AC3) */
        .risk-alert-card {
            background: linear-gradient(135deg, #fff1f2 0%, #fee2e2 100%);
            border: 2px solid #ef4444;
            border-radius: var(--radius-lg);
            padding: 20px 24px;
            margin-bottom: 24px;
            box-shadow: 0 4px 12px rgba(239, 68, 68, 0.15);
            display: flex;
            align-items: flex-start;
            gap: 18px;
            position: relative;
            animation: pulseRisk 2s infinite ease-in-out;
        }
        @keyframes pulseRisk {
            0% { box-shadow: 0 4px 12px rgba(239, 68, 68, 0.15); }
            50% { box-shadow: 0 4px 20px rgba(239, 68, 68, 0.35); }
            100% { box-shadow: 0 4px 12px rgba(239, 68, 68, 0.15); }
        }
        .risk-alert-icon {
            background-color: #ef4444;
            color: #ffffff;
            width: 44px;
            height: 44px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
            box-shadow: 0 2px 6px rgba(239, 68, 68, 0.3);
        }
        .risk-alert-content {
            flex: 1;
        }
        .risk-alert-title {
            font-size: 17px;
            font-weight: 800;
            color: #991b1b;
            margin: 0 0 6px 0;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .risk-alert-desc {
            font-size: 14px;
            color: #7f1d1d;
            margin: 0 0 10px 0;
            line-height: 1.5;
        }
        .risk-alert-meta {
            font-size: 12.5px;
            color: #b91c1c;
            display: flex;
            align-items: center;
            gap: 16px;
            flex-wrap: wrap;
        }

        .safe-status-card {
            background-color: #f0fdf4;
            border: 1px solid #86efac;
            border-radius: var(--radius-md);
            padding: 12px 18px;
            margin-bottom: 24px;
            display: flex;
            align-items: center;
            gap: 12px;
            color: #166534;
            font-size: 13.5px;
        }

        /* Section Yêu Cầu Hỗ Trợ Sau Bán */
        .support-card {
            background: #ffffff;
            border-radius: var(--radius-lg);
            border: 1px solid var(--border-color);
            box-shadow: var(--shadow-sm);
            overflow: hidden;
            margin-bottom: 24px;
        }
        .support-header {
            padding: 18px 24px;
            border-bottom: 1px solid var(--border-color);
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 14px;
            background-color: #f8fafc;
        }
        .support-header-title {
            font-size: 16px;
            font-weight: 700;
            color: var(--slate-900);
            display: flex;
            align-items: center;
            gap: 8px;
        }

        /* Badges mức độ ưu tiên */
        .priority-badge {
            font-size: 11.5px;
            font-weight: 700;
            padding: 3px 8px;
            border-radius: 9999px;
            display: inline-flex;
            align-items: center;
            text-transform: uppercase;
            letter-spacing: 0.04em;
        }
        .priority-THAP { background: #f1f5f9; color: #475569; }
        .priority-BINH_THUONG { background: #e0f2fe; color: #0369a1; }
        .priority-CAO { background: #fef3c7; color: #b45309; }
        .priority-KHAN_CAP { background: #fee2e2; color: #b91c1c; font-weight: 800; animation: blink 1.5s infinite; }
        @keyframes blink {
            50% { opacity: 0.7; }
        }

        /* Badges trạng thái */
        .status-badge {
            font-size: 12px;
            font-weight: 600;
            padding: 4px 9px;
            border-radius: 9999px;
            display: inline-block;
        }
        .status-MOI { background: #dbeafe; color: #1e40af; }
        .status-DANG_XU_LY { background: #e0e7ff; color: #4338ca; }
        .status-CHO_KHACH_HANG { background: #fef9c3; color: #854d0e; }
        .status-DA_XU_LY { background: #dcfce7; color: #15803d; }
        .status-DONG { background: #f1f5f9; color: #64748b; }
        .status-HUY { background: #fee2e2; color: #991b1b; }

        @media (max-width: 640px) {
            .detail-header, .detail-body, .detail-footer, .support-header {
                padding: 16px;
            }
            .detail-title-group h1 {
                font-size: 18px;
            }
            .risk-alert-card {
                flex-direction: column;
                gap: 12px;
            }
        }
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
                <li class="breadcrumb-item active" aria-current="page"><c:out value="${not empty banGhi.tieuDe ? banGhi.tieuDe : 'Trang 360'}" /></li>
            </ol>
        </nav>

        <!-- Thông báo thành công / trạng thái -->
        <c:if test="${not empty thongBaoThanhCong}">
            <div class="alert alert-success" style="margin-bottom: 20px;">
                <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                <span><c:out value="${thongBaoThanhCong}" /></span>
            </div>
        </c:if>
        <c:if test="${not empty sessionScope.thongBaoThanhCong}">
            <div class="alert alert-success" style="margin-bottom: 20px;">
                <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                <span><c:out value="${sessionScope.thongBaoThanhCong}" /></span>
            </div>
            <c:remove var="thongBaoThanhCong" scope="session" />
        </c:if>
        <c:if test="${not empty sessionScope.thongBaoLoi}">
            <div class="alert alert-danger" style="margin-bottom: 20px;">
                <span class="material-symbols-outlined" aria-hidden="true">error</span>
                <span><c:out value="${sessionScope.thongBaoLoi}" /></span>
            </div>
            <c:remove var="thongBaoLoi" scope="session" />
        </c:if>

        <!-- STORY S3-08 AC3: CỜ RỦI RO HIỂN THỊ TRÊN TRANG 360 VÀ CẢNH BÁO CHO NHÂN VIÊN KINH DOANH PHỤ TRÁCH -->
        <c:choose>
            <c:when test="${banGhi.coRuiRo or (not empty thongTinRuiRo and thongTinRuiRo.coRuiRo)}">
                <div class="risk-alert-card" id="bannerCoRuiRoRoiBo" role="alert" aria-live="assertive">
                    <div class="risk-alert-icon">
                        <span class="material-symbols-outlined" style="font-size: 28px;" aria-hidden="true">warning</span>
                    </div>
                    <div class="risk-alert-content">
                        <h2 class="risk-alert-title">
                            <span>CẢNH BÁO RỦI RO RỜI BỎ (CHURN RISK)</span>
                            <span class="badge badge-danger" style="background: #dc2626; color: #fff; font-size: 11px;">MỨC NGUY CƠ CAO</span>
                        </h2>
                        <p class="risk-alert-desc">
                            Khách hàng này hiện đang có <strong><c:out value="${not empty thongTinRuiRo ? thongTinRuiRo.soYeuCauChuaXuLy : banGhi.soYeuCauChuaXuLy}" /></strong> yêu cầu hỗ trợ sau bán chưa được xử lý
                            (vượt ngưỡng cảnh báo <strong><c:out value="${not empty thongTinRuiRo ? thongTinRuiRo.nguongRuiRo : 3}" /></strong> yêu cầu).
                            Hệ thống đã tự động gắn cờ rủi ro rời bỏ và gửi thông báo khẩn cấp tới nhân viên kinh doanh phụ trách
                            (<strong><c:out value="${banGhi.tenNguoiPhuTrach}" /></strong>). Cần chủ động phối hợp giải quyết ngay!
                        </p>
                        <div class="risk-alert-meta">
                            <span><span class="material-symbols-outlined" style="font-size: 15px; vertical-align: middle;">schedule</span> Thời điểm gắn cờ: <c:out value="${not empty thongTinRuiRo.ruiRoCapNhatLuc ? thongTinRuiRo.ruiRoCapNhatLuc : 'Gần đây'}" /></span>
                            <span><span class="material-symbols-outlined" style="font-size: 15px; vertical-align: middle;">person</span> NVKD phụ trách: <strong><c:out value="${banGhi.tenNguoiPhuTrach}" /></strong></span>
                            <span><span class="material-symbols-outlined" style="font-size: 15px; vertical-align: middle;">group</span> Nhóm: <c:out value="${banGhi.tenNhom}" /></span>
                        </div>
                    </div>
                    <div>
                        <button type="button" class="btn btn-primary" onclick="moModalGhiNhanYeuCau()" style="background: #dc2626; border-color: #b91c1c; white-space: nowrap;">
                            <span class="material-symbols-outlined" aria-hidden="true">add_task</span>
                            <span>Ghi nhận hỗ trợ</span>
                        </button>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="safe-status-card">
                    <span class="material-symbols-outlined" style="color: #16a34a;" aria-hidden="true">verified_user</span>
                    <span>
                        Trạng thái chăm sóc: <strong>ỔN ĐỊNH</strong> &bull; Hiện có <strong><c:out value="${not empty thongTinRuiRo ? thongTinRuiRo.soYeuCauChuaXuLy : 0}" /></strong>/<c:out value="${not empty thongTinRuiRo ? thongTinRuiRo.nguongRuiRo : 3}" /> yêu cầu hỗ trợ chưa xử lý (dưới ngưỡng rủi ro rời bỏ).
                    </span>
                </div>
            </c:otherwise>
        </c:choose>

        <!-- Thẻ chi tiết khách hàng Trang 360 -->
        <div class="detail-card">
            <div class="detail-header">
                <div class="detail-title-group">
                    <h1>
                        <span class="material-symbols-outlined" aria-hidden="true" style="color: var(--primary);">business</span>
                        <span><c:out value="${banGhi.tieuDe}" /></span>
                    </h1>
                    <p>Mã khách hàng: <span class="font-mono"><c:out value="${banGhi.maBanGhi}" /></span> • Trang hồ sơ 360 độ khách hàng</p>
                </div>
                <div style="display: flex; align-items: center; gap: 10px; flex-wrap: wrap;">
                    <c:choose>
                        <c:when test="${banGhi.coRuiRo or (not empty thongTinRuiRo and thongTinRuiRo.coRuiRo)}">
                            <span class="badge" style="background: #ef4444; color: #fff; font-size: 13px; padding: 6px 12px; font-weight: 700;">
                                <span class="material-symbols-outlined" style="font-size: 15px; vertical-align: middle;">flag</span> RỦI RO RỜI BỎ
                            </span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge badge-success" style="font-size: 13px; padding: 6px 12px;">
                                <span class="material-symbols-outlined" style="font-size: 15px; vertical-align: middle;">check_circle</span> Hoạt động tốt
                            </span>
                        </c:otherwise>
                    </c:choose>
                    <a href="${pageContext.request.contextPath}/khach-hang" class="btn btn-outline" id="btnBackKhachHang">
                        <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span>
                        <span>Danh sách khách hàng</span>
                    </a>
                </div>
            </div>

            <div class="detail-body">
                <div class="detail-grid">
                    <div class="detail-item">
                        <span class="detail-label">Mã khách hàng</span>
                        <span class="detail-value font-mono"><c:out value="${banGhi.maBanGhi}" /></span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Tên khách hàng / Công ty</span>
                        <span class="detail-value" style="font-weight: 700; color: var(--slate-900);"><c:out value="${banGhi.tieuDe}" /></span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Người sở hữu / Phụ trách</span>
                        <span class="detail-value"><c:out value="${banGhi.tenNguoiPhuTrach}" /></span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Nhóm kinh doanh</span>
                        <span class="detail-value"><c:out value="${banGhi.tenNhom}" /></span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Doanh thu ước tính / Phân loại</span>
                        <span class="detail-value"><c:out value="${not empty banGhi.giaTri ? banGhi.giaTri : '-'}" /></span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Trạng thái quan hệ</span>
                        <span class="detail-value"><c:out value="${banGhi.trangThai}" /></span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Ngày tạo hồ sơ</span>
                        <span class="detail-value"><c:out value="${not empty banGhi.ngayTao ? banGhi.ngayTao : '-'}" /></span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Cờ rủi ro rời bỏ</span>
                        <span class="detail-value">
                            <c:choose>
                                <c:when test="${banGhi.coRuiRo or (not empty thongTinRuiRo and thongTinRuiRo.coRuiRo)}">
                                    <strong style="color: #dc2626;">Đang gắn cờ rủi ro (Nguy cơ rời bỏ)</strong>
                                </c:when>
                                <c:otherwise>
                                    <span style="color: #16a34a;">Bình thường / Chưa phát hiện rủi ro</span>
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                </div>

                <div class="detail-item">
                    <span class="detail-label" style="margin-bottom: 6px;">Mô tả / Ghi chú chăm sóc</span>
                    <div class="detail-desc-box">
                        <c:choose>
                            <c:when test="${not empty banGhi.moTaChiTiet}">
                                <c:out value="${banGhi.moTaChiTiet}" />
                            </c:when>
                            <c:otherwise>
                                <span style="color: var(--slate-400); font-style: italic;">Chưa có mô tả chi tiết cho bản ghi này.</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>

        <!-- STORY S3-08 AC1 & AC3: BẢNG YÊU CẦU HỖ TRỢ SAU BÁN TRÊN TRANG 360 -->
        <div class="support-card" id="khuVucYeuCauHoTro">
            <div class="support-header">
                <div>
                    <h2 class="support-header-title">
                        <span class="material-symbols-outlined" style="color: var(--primary);" aria-hidden="true">support_agent</span>
                        <span>Yêu cầu hỗ trợ sau bán & Giữ chân khách hàng</span>
                        <span class="badge badge-info" style="font-size: 12px; margin-left: 8px;">
                            Tổng số: <c:out value="${not empty danhSachYeuCauHoTro ? danhSachYeuCauHoTro.size() : 0}" />
                        </span>
                        <c:if test="${(not empty thongTinRuiRo and thongTinRuiRo.soYeuCauChuaXuLy > 0) or banGhi.soYeuCauChuaXuLy > 0}">
                            <span class="badge badge-warning" style="font-size: 12px; margin-left: 4px;">
                                Chưa xử lý: <c:out value="${not empty thongTinRuiRo ? thongTinRuiRo.soYeuCauChuaXuLy : banGhi.soYeuCauChuaXuLy}" />
                            </span>
                        </c:if>
                    </h2>
                    <p style="margin: 4px 0 0; font-size: 13px; color: var(--slate-500);">
                        Ghi nhận sự cố, yêu cầu kỹ thuật và theo dõi tiến độ xử lý để kịp thời phòng ngừa rủi ro rời bỏ.
                    </p>
                </div>
                <div style="display: flex; gap: 10px;">
                    <a href="${pageContext.request.contextPath}/yeu-cau-ho-tro" class="btn btn-outline" style="font-size: 13px;">
                        <span class="material-symbols-outlined" aria-hidden="true">list_alt</span>
                        <span>Xem tất cả yêu cầu</span>
                    </a>
                    <button type="button" class="btn btn-primary" onclick="moModalGhiNhanYeuCau()" id="btnGhiNhanYeuCauMoi">
                        <span class="material-symbols-outlined" aria-hidden="true">add</span>
                        <span>Ghi nhận yêu cầu hỗ trợ</span>
                    </button>
                </div>
            </div>

            <div style="overflow-x: auto;">
                <table class="data-table" style="width: 100%; margin: 0;">
                    <thead>
                        <tr>
                            <th style="width: 130px;">Mã Ticket</th>
                            <th>Tiêu Đề / Nội Dung Yêu Cầu</th>
                            <th style="width: 130px; text-align: center;">Mức Độ Ưu Tiên</th>
                            <th style="width: 160px;">Người Xử Lý</th>
                            <th style="width: 140px; text-align: center;">Trạng Thái</th>
                            <th style="width: 140px;">Thời Gian Tạo</th>
                            <th style="width: 150px; text-align: center;">Cập Nhật Nhanh</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty danhSachYeuCauHoTro}">
                                <c:forEach var="yc" items="${danhSachYeuCauHoTro}">
                                    <tr>
                                        <td class="font-mono" style="font-weight: 600; font-size: 13px;">
                                            <c:out value="${yc.maYeuCau}" />
                                        </td>
                                        <td>
                                            <div style="font-weight: 600; color: var(--slate-900); font-size: 14px;">
                                                <c:out value="${yc.tieuDe}" />
                                            </div>
                                            <c:if test="${not empty yc.noiDung}">
                                                <div style="font-size: 12.5px; color: var(--slate-500); margin-top: 3px; max-width: 450px;">
                                                    <c:out value="${yc.noiDung}" />
                                                </div>
                                            </c:if>
                                            <c:if test="${not empty yc.tenNguoiLienHe}">
                                                <div style="font-size: 12px; color: var(--primary); margin-top: 2px;">
                                                    <span class="material-symbols-outlined" style="font-size: 13px; vertical-align: middle;">contact_mail</span>
                                                    Liên hệ: <c:out value="${yc.tenNguoiLienHe}" />
                                                    <c:if test="${not empty yc.sdtNguoiLienHe}"> - <c:out value="${yc.sdtNguoiLienHe}" /></c:if>
                                                </div>
                                            </c:if>
                                        </td>
                                        <td style="text-align: center;">
                                            <span class="priority-badge priority-${yc.mucUuTien}">
                                                <c:out value="${yc.mucUuTienEnum.tenHienThi}" />
                                            </span>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty yc.tenNguoiXuLy}">
                                                    <div style="font-weight: 500; font-size: 13px;"><c:out value="${yc.tenNguoiXuLy}" /></div>
                                                    <div style="font-size: 11.5px; color: var(--slate-400);"><c:out value="${yc.emailNguoiXuLy}" /></div>
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="color: var(--slate-400); font-style: italic; font-size: 12.5px;">Chưa phân công</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center;">
                                            <span class="status-badge status-${yc.trangThai}">
                                                <c:out value="${yc.trangThaiEnum.tenHienThi}" />
                                            </span>
                                        </td>
                                        <td style="font-size: 13px; color: var(--slate-600);">
                                            <c:out value="${yc.taoLuc}" />
                                        </td>
                                        <td style="text-align: center;">
                                            <!-- Form cập nhật trạng thái nhanh kích hoạt tự động đánh giá cờ rủi ro -->
                                            <form action="${pageContext.request.contextPath}/yeu-cau-ho-tro" method="POST" style="display: inline-block;">
                                                <input type="hidden" name="action" value="cap-nhat-trang-thai">
                                                <input type="hidden" name="id" value="${yc.id}">
                                                <input type="hidden" name="redirectUrl" value="${pageContext.request.contextPath}/chi-tiet-ban-ghi?id=${banGhi.id}">
                                                <select name="trangThai" class="form-select" style="font-size: 12px; padding: 4px 6px; width: auto;" onchange="this.form.submit()">
                                                    <option value="MOI" ${yc.trangThai == 'MOI' ? 'selected' : ''}>Mới</option>
                                                    <option value="DANG_XU_LY" ${yc.trangThai == 'DANG_XU_LY' ? 'selected' : ''}>Đang xử lý</option>
                                                    <option value="CHO_KHACH_HANG" ${yc.trangThai == 'CHO_KHACH_HANG' ? 'selected' : ''}>Chờ khách</option>
                                                    <option value="DA_XU_LY" ${yc.trangThai == 'DA_XU_LY' ? 'selected' : ''}>Đã xử lý</option>
                                                    <option value="DONG" ${yc.trangThai == 'DONG' ? 'selected' : ''}>Đóng</option>
                                                </select>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="7" style="text-align: center; padding: 36px 20px; color: var(--slate-500);">
                                        <span class="material-symbols-outlined" style="font-size: 36px; color: var(--slate-300); display: block; margin-bottom: 8px;">thumb_up</span>
                                        Khách hàng hiện chưa có yêu cầu hỗ trợ sau bán nào phát sinh.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- MODAL GHI NHẬN YÊU CẦU HỖ TRỢ SAU BÁN (STORY S3-08 AC1) -->
        <div class="modal-backdrop" id="modalGhiNhanYeuCau" style="display: none;" role="dialog" aria-modal="true" aria-labelledby="modalGhiNhanYeuCauTieuDe">
            <div class="modal-card" style="max-width: 580px;">
                <div class="modal-header">
                    <div>
                        <h2 class="modal-title" id="modalGhiNhanYeuCauTieuDe">Ghi Nhận Yêu Cầu Hỗ Trợ Sau Bán</h2>
                        <p class="modal-subtitle">Ghi nhận thông tin sự cố, thắc mắc và người xử lý cho khách hàng</p>
                    </div>
                    <button type="button" class="modal-close-btn" onclick="dongModalGhiNhanYeuCau()" aria-label="Đóng" title="Đóng">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>
                <form action="${pageContext.request.contextPath}/yeu-cau-ho-tro" method="POST" id="formGhiNhanYeuCau">
                    <input type="hidden" name="action" value="tao">
                    <input type="hidden" name="khachHangId" value="${banGhi.id}">
                    <input type="hidden" name="redirectUrl" value="${pageContext.request.contextPath}/chi-tiet-ban-ghi?id=${banGhi.id}">

                    <div class="modal-body">
                        <div class="form-group">
                            <label class="form-label">Khách hàng / Công ty</label>
                            <input type="text" class="form-input" value="${banGhi.tieuDe}" readonly style="background-color: var(--slate-100); color: var(--slate-700);">
                        </div>

                        <div class="form-group">
                            <label class="form-label" for="tieuDeYeuCau">Tiêu đề yêu cầu hỗ trợ <span class="required" style="color: red;">*</span></label>
                            <input type="text" id="tieuDeYeuCau" name="tieuDe" class="form-input" placeholder="Ví dụ: Lỗi đồng bộ dữ liệu hoá đơn điện tử..." required autocomplete="off">
                        </div>

                        <div class="form-row" style="display: flex; gap: 16px;">
                            <div class="form-col" style="flex: 1;">
                                <label class="form-label" for="mucUuTien">Mức độ ưu tiên <span class="required" style="color: red;">*</span></label>
                                <select id="mucUuTien" name="mucUuTien" class="form-select" required>
                                    <option value="BINH_THUONG" selected>Bình thường</option>
                                    <option value="THAP">Thấp</option>
                                    <option value="CAO">Cao</option>
                                    <option value="KHAN_CAP">Khẩn cấp (Sự cố nghiêm trọng)</option>
                                </select>
                            </div>
                            <div class="form-col" style="flex: 1;">
                                <label class="form-label" for="trangThaiYeuCau">Trạng thái ban đầu <span class="required" style="color: red;">*</span></label>
                                <select id="trangThaiYeuCau" name="trangThai" class="form-select" required>
                                    <option value="MOI" selected>Mới tiếp nhận</option>
                                    <option value="DANG_XU_LY">Đang xử lý</option>
                                    <option value="CHO_KHACH_HANG">Chờ khách hàng</option>
                                </select>
                            </div>
                        </div>

                        <div class="form-group" style="margin-top: 14px;">
                            <label class="form-label" for="nguoiXuLyId">Người xử lý (CSKH / Kỹ thuật)</label>
                            <select id="nguoiXuLyId" name="nguoiXuLyId" class="form-select">
                                <option value="">-- Chưa phân công --</option>
                                <c:forEach var="nv" items="${danhSachNhanVien}">
                                    <option value="${nv.id}">
                                        <c:out value="${nv.hoTen}" /> (<c:out value="${nv.email}" />)
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="form-group" style="margin-top: 14px;">
                            <label class="form-label" for="noiDungYeuCau">Nội dung chi tiết / Phản hồi từ khách</label>
                            <textarea id="noiDungYeuCau" name="noiDung" class="form-textarea" rows="3" placeholder="Mô tả chi tiết sự cố, yêu cầu cần xử lý..."></textarea>
                        </div>
                    </div>

                    <div class="modal-footer" style="padding: 16px 24px; border-top: 1px solid var(--border-color); display: flex; justify-content: flex-end; gap: 12px; background: #f8fafc;">
                        <button type="button" class="btn btn-outline" onclick="dongModalGhiNhanYeuCau()">Hủy bỏ</button>
                        <button type="submit" class="btn btn-primary" id="btnSubmitGhiNhanYeuCau">
                            <span class="material-symbols-outlined" aria-hidden="true">save</span>
                            Lưu Yêu Cầu Hỗ Trợ
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </main>

    <script src="${pageContext.request.contextPath}/assets/js/navigation.js"></script>
    <script>
        function moModalGhiNhanYeuCau() {
            var modal = document.getElementById('modalGhiNhanYeuCau');
            if (modal) {
                modal.style.display = 'flex';
                var input = document.getElementById('tieuDeYeuCau');
                if (input) input.focus();
            }
        }

        function dongModalGhiNhanYeuCau() {
            var modal = document.getElementById('modalGhiNhanYeuCau');
            if (modal) {
                modal.style.display = 'none';
            }
        }

        // Đóng modal khi bấm phím Escape
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                dongModalGhiNhanYeuCau();
            }
        });
    </script>
</body>
</html>
