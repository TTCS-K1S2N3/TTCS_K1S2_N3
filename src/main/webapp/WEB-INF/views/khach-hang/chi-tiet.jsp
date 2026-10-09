<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Trang 360° Khách hàng, Quan hệ công ty mẹ - con & Dịch vụ hỗ trợ sau bán - CRM Bán Hàng">
    <title>Hồ Sơ 360°: <c:out value="${not empty banGhi.tieuDe ? banGhi.tieuDe : 'Khách hàng'}" /> - CRM Bán Hàng</title>
    <!-- CSS dùng chung toàn hệ thống -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/khach-hang/khach-hang.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/khach-hang/chi-tiet.css">
    <!-- CSS chuyên biệt module Yêu cầu hỗ trợ & Cờ rủi ro (Story S3-08) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/khach-hang/yeu-cau-ho-tro.css">
</head>
<body class="crm-body">
    <!-- Thanh điều hướng và Sidebar chuẩn hệ thống S1-06 -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <c:set var="currentStatus" value="${not empty khachHang ? khachHang.trangThaiHienThi : banGhi.trangThai}" />
        <c:set var="tenHienThi" value="${not empty khachHang.tenCongTy ? khachHang.tenCongTy : banGhi.tieuDe}" />
        <c:set var="maHienThi" value="${not empty khachHang.maKhachHang ? khachHang.maKhachHang : banGhi.maBanGhi}" />
        <!-- Breadcrumb chuẩn hệ thống -->
        <nav class="breadcrumb-nav" aria-label="Breadcrumb">
            <ol class="breadcrumb-list">
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/dieu-huong">Trang chủ</a></li>
                <span class="breadcrumb-separator">/</span>
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/khach-hang">Khách hàng</a></li>
                <span class="breadcrumb-separator">/</span>
                <li class="breadcrumb-item active" aria-current="page">Hồ sơ 360°: <c:out value="${banGhi.tieuDe}" /></li>
            </ol>
        </nav>

        <!-- Thông báo phản hồi nếu có -->
        <c:if test="${not empty thongBaoThanhCong}">
            <div class="alert alert-success" style="margin-bottom: 20px;" id="alertSuccess">
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
        <c:if test="${not empty thongBaoLoi}">
            <div class="alert alert-danger" style="margin-bottom: 20px;" id="alertError">
                <span class="material-symbols-outlined" aria-hidden="true">error</span>
                <span><c:out value="${thongBaoLoi}" /></span>
            </div>
        </c:if>
        <c:if test="${not empty sessionScope.thongBaoLoi}">
            <div class="alert alert-danger" style="margin-bottom: 20px;">
                <span class="material-symbols-outlined" aria-hidden="true">error</span>
                <span><c:out value="${sessionScope.thongBaoLoi}" /></span>
            </div>
            <c:remove var="thongBaoLoi" scope="session" />
        </c:if>
        <c:if test="${not empty thongBaoCanhBao}">
            <div class="alert alert-warning" style="margin-bottom: 20px;">
                <span class="material-symbols-outlined" aria-hidden="true">warning</span>
                <span><c:out value="${thongBaoCanhBao}" /></span>
            </div>
        </c:if>

        <!-- STORY S3-08 AC3: CỜ RỦI RO HIỂN THỊ TRÊN TRANG 360 VÀ CẢNH BÁO CHO NHÂN VIÊN KINH DOANH PHỤ TRÁCH -->
        <c:choose>
            <c:when test="${banGhi.coRuiRo or (not empty thongTinRuiRo and thongTinRuiRo.coRuiRo)}">
                <section class="ycht-risk-banner" id="bannerCoRuiRoRoiBo" role="alert" aria-live="assertive" style="margin-bottom: 24px;">
                    <div class="ycht-risk-banner-icon" aria-hidden="true">
                        <span class="material-symbols-outlined" style="font-size: 28px;">warning</span>
                    </div>
                    <div class="ycht-risk-banner-content">
                        <h2 class="ycht-risk-banner-title">
                            <span>CẢNH BÁO RỦI RO RỜI BỎ (CHURN RISK)</span>
                            <span class="ycht-risk-banner-badge">MỨC NGUY CƠ CAO</span>
                        </h2>
                        <p class="ycht-risk-banner-desc">
                            Khách hàng này hiện đang có <strong><c:out value="${not empty thongTinRuiRo ? thongTinRuiRo.soYeuCauChuaXuLy : banGhi.soYeuCauChuaXuLy}" /></strong> yêu cầu hỗ trợ sau bán chưa được xử lý
                            (vượt ngưỡng cảnh báo <strong><c:out value="${not empty thongTinRuiRo ? thongTinRuiRo.nguongRuiRo : 3}" /></strong> yêu cầu).
                            Hệ thống đã tự động gắn cờ rủi ro rời bỏ và gửi thông báo cảnh báo tới nhân viên kinh doanh phụ trách
                            (<strong><c:out value="${banGhi.tenNguoiPhuTrach}" /></strong>). Cần chủ động phối hợp với đội Chăm sóc khách hàng để giải quyết dứt điểm!
                        </p>
                        <div class="ycht-risk-banner-meta">
                            <span class="ycht-risk-meta-item">
                                <span class="material-symbols-outlined" aria-hidden="true">schedule</span>
                                <span>Thời điểm gắn cờ: <strong><c:out value="${not empty thongTinRuiRo.ruiRoCapNhatLuc ? thongTinRuiRo.ruiRoCapNhatLuc : 'Gần đây'}" /></strong></span>
                            </span>
                            <span class="ycht-risk-meta-item">
                                <span class="material-symbols-outlined" aria-hidden="true">person</span>
                                <span>NVKD phụ trách: <strong><c:out value="${banGhi.tenNguoiPhuTrach}" /></strong></span>
                            </span>
                            <span class="ycht-risk-meta-item">
                                <span class="material-symbols-outlined" aria-hidden="true">groups</span>
                                <span>Nhóm: <strong><c:out value="${banGhi.tenNhom}" /></strong></span>
                            </span>
                        </div>
                    </div>
                    <div>
                        <button type="button" class="ycht-btn ycht-btn-danger" onclick="moModalGhiNhanYeuCau()" id="btnXuLyRuiRo">
                            <span class="material-symbols-outlined" aria-hidden="true">add_task</span>
                            <span>Ghi nhận hỗ trợ</span>
                        </button>
                    </div>
                </section>
            </c:when>
            <c:otherwise>
                <section class="ycht-safe-banner" id="bannerTrangThaiAnToan" style="margin-bottom: 24px;">
                    <span class="material-symbols-outlined" aria-hidden="true">verified_user</span>
                    <div>
                        Trạng thái chăm sóc: <strong>ỔN ĐỊNH</strong> &bull; Hiện có <strong><c:out value="${not empty thongTinRuiRo ? thongTinRuiRo.soYeuCauChuaXuLy : 0}" /></strong>/<c:out value="${not empty thongTinRuiRo ? thongTinRuiRo.nguongRuiRo : 3}" /> yêu cầu hỗ trợ chưa xử lý (dưới ngưỡng rủi ro rời bỏ).
                    </div>
                </section>
            </c:otherwise>
        </c:choose>

        <!-- 1. Header Hồ Sơ 360° Khách Hàng (Story S3-03) -->
        <div class="customer-360-header">
            <div class="customer-360-profile">
                <div class="customer-avatar-large">
                    <span class="material-symbols-outlined" aria-hidden="true">corporate_fare</span>
                </div>
                <div class="customer-title-group">
                    <h1>
                        <span><c:out value="${banGhi.tieuDe}" /></span>
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
                        <c:if test="${banGhi.coRuiRo or (not empty thongTinRuiRo and thongTinRuiRo.coRuiRo)}">
                            <span class="badge" style="background: #fef3c7; color: #92400e; border: 1px solid #fde68a;">
                                <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">warning</span>
                                Cảnh báo rủi ro rời bỏ
                            </span>
                        </c:if>
                        <c:if test="${not empty thongKeNhomCongTy.congTyMe.congTyMeId}">
                            <span class="badge" style="background: #e0f2fe; color: #0369a1; border: 1px solid #bae6fd;">
                                <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">account_tree</span>
                                Công ty con của: <c:out value="${thongKeNhomCongTy.congTyMe.tenCongTyMe}" />
                            </span>
                        </c:if>
                    </h1>
                    <div class="customer-meta-row">
                        <span>Mã KH: <strong class="font-mono"><c:out value="${banGhi.maBanGhi}" /></strong></span>
                        <span class="customer-meta-sep">&bull;</span>
                        <c:choose>
                            <c:when test="${not empty khachHang and not empty khachHang.maSoThue}">
                                <span>Mã số thuế: <code class="mst-badge"><c:out value="${khachHang.maSoThue}" /></code></span>
                            </c:when>
                            <c:otherwise>
                                <span>Mã số thuế: <span class="text-muted-italic">Chưa khai báo</span></span>
                            </c:otherwise>
                        </c:choose>
                        <c:if test="${not empty khachHang and not empty khachHang.website}">
                            <span class="customer-meta-sep">&bull;</span>
                            <span>Website: <a href="${khachHang.website.startsWith('http') ? khachHang.website : 'https://'.concat(khachHang.website)}" target="_blank" rel="noopener noreferrer" class="link-website"><c:out value="${khachHang.website}" /></a></span>
                        </c:if>
                        <c:if test="${not empty khachHang and not empty khachHang.diaChi}">
                            <span class="customer-meta-sep">&bull;</span>
                            <span>Địa chỉ: <c:out value="${khachHang.diaChi}" /></span>
                        </c:if>
                        <c:if test="${not empty khachHang and not empty khachHang.tenNganhNghe}">
                            <span class="customer-meta-sep">&bull;</span>
                            <span>Ngành nghề: <strong><c:out value="${khachHang.tenNganhNghe}" /></strong></span>
                        </c:if>
                        <c:if test="${not empty khachHang and not empty khachHang.tenQuyMo}">
                            <span class="customer-meta-sep">&bull;</span>
                            <span>Quy mô: <strong><c:out value="${khachHang.tenQuyMo}" /></strong></span>
                        </c:if>
                        <span class="customer-meta-sep">&bull;</span>
                        <span>Phụ trách: <strong><c:out value="${banGhi.tenNguoiPhuTrach}" /></strong> (<c:out value="${banGhi.tenNhom}" />)</span>
                        <span class="customer-meta-sep">&bull;</span>
                        <span>Phạm vi: <strong style="color: var(--primary);">Hợp lệ (Data Scope)</strong></span>
                    </div>
                </div>
            </div>

            <div class="header-actions-group">
                <a href="${pageContext.request.contextPath}/khach-hang" class="btn btn-outline" id="btnBackKhachHang">
                    <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span>
                    <span>Danh sách khách hàng</span>
                </a>
                <c:choose>
                    <c:when test="${not empty thongKeNhomCongTy.congTyMe.congTyMeId}">
                        <button type="button" class="btn btn-outline" onclick="moModalChonCongTyMe()" id="btnDoiCongTyMe">
                            <span class="material-symbols-outlined" aria-hidden="true">account_tree</span>
                            <span>Đổi công ty mẹ</span>
                        </button>
                    </c:when>
                    <c:otherwise>
                        <button type="button" class="btn btn-outline" onclick="moModalChonCongTyMe()" id="btnKhaiBaoCongTyMe">
                            <span class="material-symbols-outlined" aria-hidden="true">account_tree</span>
                            <span>Gán vào công ty mẹ</span>
                        </button>
                    </c:otherwise>
                </c:choose>
                <button type="button" class="btn btn-outline" onclick="moModalGanCongTyCon()" id="btnGanCongTyCon">
                    <span class="material-symbols-outlined" aria-hidden="true">add_circle</span>
                    <span>Gán công ty con</span>
                </button>
                <button type="button" class="btn btn-primary" id="btnThemHoatDongNhanh">
                    <span class="material-symbols-outlined" aria-hidden="true">add_task</span>
                    <span>Ghi nhận hoạt động</span>
                </button>
            </div>
        </div>

        <!-- 2. Thẻ Tóm Tắt Giá Trị & Chỉ Số KPI (S3-03 AC2, AC3 & S3-05 AC2) -->
        <div class="kpi-summary-grid">
            <!-- Thẻ 1: Tổng giá trị đã ký (S3-03 AC2) -->
            <div class="kpi-card">
                <div class="kpi-card-header">
                    <span class="kpi-card-label">Tổng giá trị đã ký</span>
                    <div class="kpi-card-icon success">
                        <span class="material-symbols-outlined" aria-hidden="true">verified</span>
                    </div>
                </div>
                <div>
                    <div class="kpi-card-value text-success" id="kpiGiaTriDaKy">
                        <c:out value="${not empty khachHang360.tongGiaTriDaKyDinhDang ? khachHang360.tongGiaTriDaKyDinhDang : (not empty kpiGiaTriDaKy ? kpiGiaTriDaKy : '0 đ')}" />
                    </div>
                    <div class="kpi-card-sub">
                        <c:choose>
                            <c:when test="${not empty khachHang360.dsCoHoiDaDong}">
                                <c:out value="${khachHang360.dsCoHoiDaDong.size()}" /> hợp đồng/cơ hội đã chốt
                            </c:when>
                            <c:otherwise>Chưa có hợp đồng hoàn tất</c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

            <!-- Thẻ 2: Giá trị cơ hội đang mở (S3-03 AC2) -->
            <div class="kpi-card">
                <div class="kpi-card-header">
                    <span class="kpi-card-label">Giá trị cơ hội đang mở</span>
                    <div class="kpi-card-icon primary">
                        <span class="material-symbols-outlined" aria-hidden="true">trending_up</span>
                    </div>
                </div>
                <div>
                    <div class="kpi-card-value text-primary" id="kpiGiaTriDangMo">
                        <c:out value="${not empty khachHang360.tongGiaTriCoHoiDangMoDinhDang ? khachHang360.tongGiaTriCoHoiDangMoDinhDang : (not empty kpiGiaTriDangMo ? kpiGiaTriDangMo : '0 đ')}" />
                    </div>
                    <div class="kpi-card-sub">
                        <c:choose>
                            <c:when test="${not empty khachHang360.dsCoHoiDangMo}">
                                <c:out value="${khachHang360.dsCoHoiDangMo.size()}" /> cơ hội đang mở
                            </c:when>
                            <c:otherwise>0 cơ hội đang mở</c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

            <!-- Thẻ 3: Tổng Giá Trị Hợp Đồng Nhóm Công Ty (Story S3-05 AC2) -->
            <div class="kpi-card highlight" id="kpiTongGiaTriTapDoan">
                <div class="kpi-card-header">
                    <span class="kpi-card-label">Tổng Hợp Đồng Nhóm Công Ty</span>
                    <div class="kpi-card-icon" style="background: #e0f2fe; color: #0284c7;">
                        <span class="material-symbols-outlined" aria-hidden="true">hub</span>
                    </div>
                </div>
                <div>
                    <div class="kpi-card-value" id="valTongGiaTriTapDoan" style="color: #0369a1;">
                        <c:out value="${not empty thongKeNhomCongTy ? thongKeNhomCongTy.tongGiaTriHopDongNhomCongTyDinhDang : '0 ₫'}" />
                    </div>
                    <div class="kpi-card-sub">
                        Gồm công ty mẹ + <strong><c:out value="${not empty thongKeNhomCongTy ? thongKeNhomCongTy.soLuongCongTyCon : 0}" /></strong> công ty con trực thuộc
                    </div>
                </div>
            </div>

            <!-- Thẻ 4: Hiệu năng tải 500 hoạt động (S3-03 AC3) -->
            <div class="kpi-card">
                <div class="kpi-card-header">
                    <span class="kpi-card-label">Hiệu năng tải (AC3)</span>
                    <div class="kpi-card-icon primary">
                        <span class="material-symbols-outlined" aria-hidden="true">speed</span>
                    </div>
                </div>
                <div>
                    <div class="kpi-card-value text-primary" id="metricRenderTime">~18 ms</div>
                    <div class="kpi-card-sub">
                        Tiêu chuẩn AC3: <strong>&lt; 1,5 giây / 500 hoạt động</strong>
                    </div>
                    <button type="button" class="btn-benchmark" id="btnRunBenchmark" title="Đo kiểm tải 500 hoạt động thực tế">
                        <span class="material-symbols-outlined" aria-hidden="true">play_arrow</span>
                        Đo kiểm 500 hoạt động
                    </button>
                </div>
            </div>
        </div>

        <!-- 3. Bố Cục 360° Đa Chiều: Hồ sơ công ty & 4 Khối dữ liệu tích hợp (AC1) -->
        <div class="customer-360-layout">
            <!-- Cột Trái: Thông tin công ty & Briefing chuẩn bị trước cuộc gặp (AC1) -->
            <aside class="customer-left-col" aria-label="Thông tin công ty">
                <!-- Card Thông tin chi tiết công ty -->
                <div class="profile-card">
                    <div class="profile-card-header">
                        <span class="material-symbols-outlined" aria-hidden="true">domain</span>
                        <h2>Thông Tin Công Ty</h2>
                    </div>
                    <div class="profile-card-body">
                        <div class="info-list">
                            <div class="info-row">
                                <span class="info-label">Mã khách hàng</span>
                                <span class="info-value font-mono"><c:out value="${not empty khachHang.maKhachHang ? khachHang.maKhachHang : banGhi.maBanGhi}" /></span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Tên đầy đủ</span>
                                <span class="info-value" style="font-weight: 700;"><c:out value="${not empty khachHang.tenCongTy ? khachHang.tenCongTy : banGhi.tieuDe}" /></span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Mã số thuế (MST)</span>
                                <span class="info-value font-mono"><c:out value="${not empty khachHang.maSoThue ? khachHang.maSoThue : '---'}" /></span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Công ty mẹ</span>
                                <span class="info-value">
                                    <c:choose>
                                        <c:when test="${not empty thongKeNhomCongTy.congTyMe.congTyMeId}">
                                            <a href="${pageContext.request.contextPath}/khach-hang/chi-tiet?id=${thongKeNhomCongTy.congTyMe.congTyMeId}" style="color: var(--primary); font-weight: 600; text-decoration: none;">
                                                <c:out value="${thongKeNhomCongTy.congTyMe.tenCongTyMe}" />
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="color: var(--slate-400); font-style: italic;">Độc lập (hoặc là công ty mẹ)</span>
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Website</span>
                                <span class="info-value">
                                    <c:set var="webUrl" value="${not empty khachHang.website ? khachHang.website : ''}" />
                                    <c:choose>
                                        <c:when test="${not empty webUrl}">
                                            <a href="${webUrl.startsWith('http') ? webUrl : 'https://'.concat(webUrl)}" target="_blank" rel="noopener noreferrer" class="info-link">
                                                <c:out value="${webUrl}" />
                                                <span class="material-symbols-outlined" style="font-size: 15px;" aria-hidden="true">open_in_new</span>
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-muted">---</span>
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Ngành nghề</span>
                                <span class="info-value"><c:out value="${not empty khachHang.tenNganhNghe ? khachHang.tenNganhNghe : 'Chưa xác định'}" /></span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Quy mô</span>
                                <span class="info-value"><c:out value="${not empty khachHang.tenQuyMo ? khachHang.tenQuyMo : 'Chưa xác định'}" /></span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Doanh thu ước tính</span>
                                <span class="info-value"><c:out value="${not empty khachHang.doanhThuUocTinh ? khachHang.doanhThuUocTinh : (not empty banGhi.giaTri ? banGhi.giaTri : '---')}" /></span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Địa chỉ trụ sở</span>
                                <span class="info-value"><c:out value="${not empty khachHang.diaChi ? khachHang.diaChi : '---'}" /></span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Khu vực địa lý</span>
                                <span class="info-value"><c:out value="${not empty khachHang.tenKhuVuc ? khachHang.tenKhuVuc : 'Chưa xác định'}" /></span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Người phụ trách</span>
                                <span class="info-value"><c:out value="${not empty khachHang.tenNguoiSoHuu ? khachHang.tenNguoiSoHuu : banGhi.tenNguoiPhuTrach}" /></span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Nhóm phụ trách</span>
                                <span class="info-value"><c:out value="${not empty khachHang.tenNhomKinhDoanh ? khachHang.tenNhomKinhDoanh : banGhi.tenNhom}" /></span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Trạng thái</span>
                                <span class="info-value"><c:out value="${banGhi.trangThai}" /></span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Cờ rủi ro</span>
                                <span class="info-value">
                                    <c:choose>
                                        <c:when test="${banGhi.coRuiRo or (not empty thongTinRuiRo and thongTinRuiRo.coRuiRo)}">
                                            <strong style="color: #ef4444;">Có cờ rủi ro rời bỏ</strong>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="color: #16a34a;">Bình thường / Ổn định</span>
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                            <c:if test="${not empty banGhi.moTaChiTiet}">
                                <div class="info-row" style="flex-direction: column; align-items: flex-start;">
                                    <span class="info-label">Mô tả chi tiết</span>
                                    <span class="info-value" style="margin-top: 4px;"><c:out value="${banGhi.moTaChiTiet}" /></span>
                                </div>
                            </c:if>
                        </div>
                    </div>
                </div>

                <!-- Card Tóm tắt bối cảnh trước cuộc gặp (Briefing Notes) -->
                <div class="briefing-card">
                    <div class="briefing-card-header">
                        <span class="material-symbols-outlined" aria-hidden="true">lightbulb</span>
                        <span>Bối Cảnh Trước Cuộc Gặp</span>
                    </div>
                    <div class="briefing-card-body">
                        <p style="margin-bottom: 8px;">
                            <strong>Mục tiêu then chốt:</strong> Trình bày giải pháp CRM, bám sát các cơ hội đang mở và giải quyết triệt để các vướng mắc sau bán hàng.
                        </p>
                        <p style="margin-bottom: 8px;">
                            <strong>Người liên hệ chủ chốt:</strong> Ưu tiên trao đổi với đầu mối chính và người có vai trò quyết định mua sắm hoặc ảnh hưởng kỹ thuật.
                        </p>
                        <p>
                            <strong>Hợp đồng & Doanh thu:</strong> Đã ký kết <strong style="color: #059669;"><c:out value="${not empty khachHang360.tongGiaTriDaKyDinhDang ? khachHang360.tongGiaTriDaKyDinhDang : '0 đ'}" /></strong>. Cần khai thác thêm các cơ hội gia hạn và mở rộng quy mô.
                        </p>
                    </div>
                </div>
            </aside>

            <!-- Cột Phải: Workspace Gom 4 Nhóm Thông Tin Bằng Hệ Thống Tabs (AC1) -->
            <section class="customer-right-col" aria-label="Không gian dữ liệu 360 độ">
                <!-- Thanh chuyển Tab điều hướng nhanh -->
                <div class="tabs-nav-bar" role="tablist">
                    <button type="button" class="nav-tab-btn active" data-tab="timeline" role="tab" aria-selected="true">
                        <span class="material-symbols-outlined" aria-hidden="true">timeline</span>
                        <span>Dòng thời gian hoạt động</span>
                        <span class="tab-badge" id="badgeSoHoatDong">${not empty khachHang360.dsHoatDong ? khachHang360.dsHoatDong.size() : 0}</span>
                    </button>
                    <button type="button" class="nav-tab-btn" data-tab="deals" role="tab" aria-selected="false">
                        <span class="material-symbols-outlined" aria-hidden="true">monetization_on</span>
                        <span>Cơ hội bán hàng</span>
                        <span class="tab-badge">Mở: ${not empty khachHang360.dsCoHoiDangMo ? khachHang360.dsCoHoiDangMo.size() : 0} | Đã chốt: ${not empty khachHang360.dsCoHoiDaDong ? khachHang360.dsCoHoiDaDong.size() : 0}</span>
                    </button>
                    <button type="button" class="nav-tab-btn" data-tab="contacts" role="tab" aria-selected="false">
                        <span class="material-symbols-outlined" aria-hidden="true">contacts</span>
                        <span>Người liên hệ</span>
                        <span class="tab-badge">${not empty dsNguoiLienHe ? dsNguoiLienHe.size() : (not empty khachHang360.dsNguoiLienHe ? khachHang360.dsNguoiLienHe.size() : 0)} người</span>
                    </button>
                    <button type="button" class="nav-tab-btn" data-tab="attachments" role="tab" aria-selected="false">
                        <span class="material-symbols-outlined" aria-hidden="true">attach_file</span>
                        <span>Tệp & Hợp đồng</span>
                        <span class="tab-badge">${(not empty khachHang360.dsTepDinhKem ? khachHang360.dsTepDinhKem.size() : 0) + (not empty khachHang360.dsHopDong ? khachHang360.dsHopDong.size() : 0)} mục</span>
                    </button>
                    <button type="button" class="nav-tab-btn" data-tab="all" role="tab" aria-selected="false">
                        <span class="material-symbols-outlined" aria-hidden="true">view_agenda</span>
                        <span>Xem toàn bộ gom chung</span>
                    </button>
                </div>

                <!-- PANEL 1: Dòng Thời Gian Hoạt Động (AC1 & AC3) -->
                <div class="tab-panel" id="panel-timeline">
                    <div class="panel-section-title">
                        <span class="material-symbols-outlined" aria-hidden="true">history</span>
                        <span>Dòng Thời Gian Tương Tác & Hoạt Động (AC1 & AC3)</span>
                    </div>

                    <!-- Toolbar Lọc & Tìm Kiếm Timeline -->
                    <div class="timeline-toolbar">
                        <div class="timeline-filters">
                            <button type="button" class="timeline-filter-btn active" data-type="ALL">Tất cả</button>
                            <button type="button" class="timeline-filter-btn" data-type="CUOC_GOI">Cuộc gọi</button>
                            <button type="button" class="timeline-filter-btn" data-type="CUOC_HOP">Cuộc họp</button>
                            <button type="button" class="timeline-filter-btn" data-type="EMAIL">Email</button>
                            <button type="button" class="timeline-filter-btn" data-type="GHI_CHU">Ghi chú</button>
                        </div>

                        <div class="timeline-search-box">
                            <span class="material-symbols-outlined timeline-search-icon" aria-hidden="true">search</span>
                            <input type="text" id="inputSearchActivity" class="timeline-search-input" placeholder="Tìm theo nội dung, người trao đổi...">
                        </div>
                    </div>

                    <div class="timeline-status-info">
                        Đang hiển thị <strong id="soHoatDongHienThi">${not empty khachHang360.dsHoatDong ? khachHang360.dsHoatDong.size() : 0}</strong> hoạt động &bull; Đã tối ưu batch rendering dưới <strong>1,5 giây với 500 hoạt động</strong> (AC3).
                    </div>

                    <!-- Container Danh Sách Hoạt Động (Render bằng JS DocumentFragment) -->
                    <div class="timeline-container" id="timelineContainer">
                        <!-- Nạp động qua khach-hang-360.js -->
                    </div>

                    <div class="timeline-actions-footer">
                        <button type="button" class="btn btn-outline" id="btnTaiThemHoatDong">
                            <span class="material-symbols-outlined" aria-hidden="true">expand_more</span>
                            <span>Tải thêm 50 hoạt động</span>
                        </button>
                        <button type="button" class="btn btn-outline" id="btnTaiTatCa500">
                            <span class="material-symbols-outlined" aria-hidden="true">all_inclusive</span>
                            <span>Tải toàn bộ hoạt động</span>
                        </button>
                    </div>
                </div>

                <!-- PANEL 2: Cơ Hội Bán Hàng (Đang Mở & Đã Đóng) (AC1 & AC2) -->
                <div class="tab-panel" id="panel-deals" style="display: none;">
                    <div class="panel-section-title">
                        <span class="material-symbols-outlined" aria-hidden="true">payments</span>
                        <span>Cơ Hội Bán Hàng (Mở & Đã Đóng) (AC1 & AC2)</span>
                    </div>

                    <!-- 1. Cơ hội đang mở (Open Deals) -->
                    <div class="deals-section" id="sectionOpenDeals">
                        <div class="deals-section-header">
                            <h3 class="deals-section-title">
                                <span class="material-symbols-outlined" aria-hidden="true" style="color: var(--primary);">trending_up</span>
                                Cơ Hội Đang Mở (${not empty khachHang360.dsCoHoiDangMo ? khachHang360.dsCoHoiDangMo.size() : 0})
                            </h3>
                            <span class="deals-section-sum">Tổng giá trị đang mở: <strong><c:out value="${not empty khachHang360.tongGiaTriCoHoiDangMoDinhDang ? khachHang360.tongGiaTriCoHoiDangMoDinhDang : '0 đ'}" /></strong></span>
                        </div>
                        <div class="table-responsive">
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th style="width: 100px;">Mã Deal</th>
                                        <th>Tên Cơ Hội</th>
                                        <th>Giai Đoạn</th>
                                        <th style="text-align: right;">Giá Trị Dự Kiến</th>
                                        <th style="text-align: center;">Xác Suất</th>
                                        <th>Dự Kiến Chốt</th>
                                        <th>Người Phụ Trách</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty khachHang360.dsCoHoiDangMo}">
                                            <c:forEach var="deal" items="${khachHang360.dsCoHoiDangMo}">
                                                <tr>
                                                    <td class="font-mono"><c:out value="${deal.maCoHoi}" /></td>
                                                    <td><strong><c:out value="${deal.tenCoHoi}" /></strong></td>
                                                    <td><span class="pipeline-stage-badge"><c:out value="${deal.tenGiaiDoan}" /></span></td>
                                                    <td style="text-align: right; font-weight: 700; color: var(--primary);">
                                                        <c:out value="${deal.giaTriDuKienDinhDang}" />
                                                    </td>
                                                    <td style="text-align: center;">
                                                        <div class="probability-bar-wrap">
                                                            <div class="probability-bar"><div class="probability-bar-fill" style="width: ${deal.xacSuat}%;"></div></div>
                                                            <span><c:out value="${deal.xacSuat}" />%</span>
                                                        </div>
                                                    </td>
                                                    <td><c:out value="${deal.ngayChotDuKienDinhDang}" /></td>
                                                    <td><c:out value="${deal.tenNguoiPhuTrach}" /></td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr>
                                                <td colspan="7" style="text-align: center; color: var(--text-muted); padding: 24px;">
                                                    Chưa có cơ hội bán hàng nào đang mở cho khách hàng này.
                                                </td>
                                            </tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <!-- 2. Cơ hội đã đóng (Closed Deals - Won / Lost) -->
                    <div class="deals-section">
                        <div class="deals-section-header">
                            <h3 class="deals-section-title">
                                <span class="material-symbols-outlined" aria-hidden="true" style="color: #059669;">task_alt</span>
                                Cơ Hội Đã Đóng / Đã Ký (${not empty khachHang360.dsCoHoiDaDong ? khachHang360.dsCoHoiDaDong.size() : 0})
                            </h3>
                            <span class="deals-section-sum">Tổng giá trị đã ký: <strong style="color: #059669;"><c:out value="${not empty khachHang360.tongGiaTriDaKyDinhDang ? khachHang360.tongGiaTriDaKyDinhDang : '0 đ'}" /></strong></span>
                        </div>
                        <div class="table-responsive">
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th style="width: 100px;">Mã Deal</th>
                                        <th>Tên Cơ Hội</th>
                                        <th>Kết Quả</th>
                                        <th style="text-align: right;">Giá Trị Đã Ký</th>
                                        <th>Ngày Ký / Đóng</th>
                                        <th>Lý Do Thắng / Thua</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty khachHang360.dsCoHoiDaDong}">
                                            <c:forEach var="deal" items="${khachHang360.dsCoHoiDaDong}">
                                                <tr>
                                                    <td class="font-mono"><c:out value="${deal.maCoHoi}" /></td>
                                                    <td><strong><c:out value="${deal.tenCoHoi}" /></strong></td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${deal.trangThai == 'DONG_THANG' || deal.trangThai == 'DA_KY'}">
                                                                <span class="badge badge-success">Đóng Thắng (Đã ký)</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge" style="background: #fee2e2; color: #b91c1c;">Đóng Thua</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td style="text-align: right; font-weight: 700; color: #059669;">
                                                        <c:out value="${deal.giaTriChotDinhDang}" />
                                                    </td>
                                                    <td><c:out value="${deal.ngayKyDinhDang}" /></td>
                                                    <td><c:out value="${deal.tenLyDoThangThua}" /></td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr>
                                                <td colspan="6" style="text-align: center; color: var(--text-muted); padding: 24px;">
                                                    Chưa có cơ hội bán hàng nào đã đóng cho khách hàng này.
                                                </td>
                                            </tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <!-- PANEL 3: Danh Sách Người Liên Hệ (Contacts) (AC1) -->
                <!-- PANEL 3: Danh Sách Người Liên Hệ & Vai Trò Quyết Định (Story S3-02 AC1, AC2, AC3, AC4) -->
                <div class="tab-panel" id="panel-contacts" style="display: none;">
                    <div class="detail-card" id="cardNguoiLienHe" style="margin-bottom: 0; box-shadow: none; border: 1px solid var(--border-color, #e2e8f0); border-radius: 8px;">
                        <div class="detail-header" style="background-color: #f8fafc; padding: 16px 20px; border-bottom: 1px solid var(--border-color, #e2e8f0); display: flex; justify-content: space-between; align-items: center; border-radius: 8px 8px 0 0;">
                            <div class="detail-title-group">
                                <h2 style="font-size: 18px; font-weight: 800; color: var(--slate-900); display: flex; align-items: center; gap: 8px; margin: 0;">
                                    <span class="material-symbols-outlined" style="color: var(--primary); font-size: 24px;" aria-hidden="true">group</span>
                                    <span>Danh Sách Người Liên Hệ &amp; Vai Trò Quyết Định (Story S3-02)</span>
                                </h2>
                                <p style="margin: 4px 0 0; color: var(--slate-500); font-size: 13px;">Mỗi khách hàng có nhiều người liên hệ; đánh dấu vai trò quyết định mua, đầu mối chính và lịch sử chuyển công ty</p>
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
                                        <th style="text-align: center; min-width: 130px;">Thao tác</th>
                                    </tr>
                                </thead>
                                <tbody id="tbodyNguoiLienHe">
                                    <c:set var="contactList" value="${not empty dsNguoiLienHe ? dsNguoiLienHe : khachHang360.dsNguoiLienHe}" />
                                    <c:choose>
                                        <c:when test="${not empty contactList}">
                                            <c:forEach var="nlh" items="${contactList}">
                                                <tr id="row-nlh-${nlh.id}">
                                                    <td>
                                                        <div class="contact-name-cell">
                                                            <strong style="color: var(--slate-900);"><c:out value="${nlh.hoTen}" /></strong>
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
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${not empty nlh.soDienThoai}">
                                                                <a href="tel:${nlh.soDienThoai}" style="color: var(--slate-700); text-decoration: none;">
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
                                                                    <c:when test="${nlh.vaiTroQuyetDinh.ma == 'NGUOI_QUYET_DINH' || nlh.vaiTroQuyetDinh == 'NGUOI_QUYET_DINH'}">
                                                                        <span class="badge-vai-tro badge-quyet-dinh" title="Người có thẩm quyền quyết định cuối cùng (Decision Maker)">
                                                                            <span class="material-symbols-outlined" aria-hidden="true">verified_user</span>
                                                                            <span>Người quyết định</span>
                                                                        </span>
                                                                    </c:when>
                                                                    <c:when test="${nlh.vaiTroQuyetDinh.ma == 'NGUOI_ANH_HUONG' || nlh.vaiTroQuyetDinh == 'NGUOI_ANH_HUONG'}">
                                                                        <span class="badge-vai-tro badge-anh-huong" title="Người có tiếng nói ảnh hưởng quan trọng (Influencer)">
                                                                            <span class="material-symbols-outlined" aria-hidden="true">insights</span>
                                                                            <span>Người ảnh hưởng</span>
                                                                        </span>
                                                                    </c:when>
                                                                    <c:when test="${nlh.vaiTroQuyetDinh.ma == 'NGUOI_DUNG_CUOI' || nlh.vaiTroQuyetDinh == 'NGUOI_DUNG_CUOI'}">
                                                                        <span class="badge-vai-tro badge-dung-cuoi" title="Người trực tiếp sử dụng sản phẩm dịch vụ (End User)">
                                                                            <span class="material-symbols-outlined" aria-hidden="true">person</span>
                                                                            <span>Người dùng cuối</span>
                                                                        </span>
                                                                    </c:when>
                                                                    <c:when test="${nlh.vaiTroQuyetDinh.ma == 'NGUOI_CAN_TRO' || nlh.vaiTroQuyetDinh == 'NGUOI_CAN_TRO'}">
                                                                        <span class="badge-vai-tro badge-can-tro" title="Người có khả năng cản trở thương vụ (Blocker)">
                                                                            <span class="material-symbols-outlined" aria-hidden="true">block</span>
                                                                            <span>Người cản trở</span>
                                                                        </span>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <span class="badge-vai-tro badge-chua-xac-dinh">
                                                                            <c:out value="${not empty nlh.tenVaiTroHienThi ? nlh.tenVaiTroHienThi : 'Chưa xác định'}" />
                                                                        </span>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge-vai-tro badge-chua-xac-dinh">Chưa xác định</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td style="text-align: center;" class="cell-dau-moi-action">
                                                        <c:choose>
                                                            <c:when test="${nlh.laDauMoiChinh}">
                                                                <button type="button" class="btn btn-sm btn-dau-moi-active" title="Đầu mối chính hiện tại" aria-label="Đầu mối chính cho <c:out value="${nlh.hoTen}" />" onclick="doiDauMoiChinh(${nlh.id}, ${not empty nlh.khachHangId ? nlh.khachHangId : (not empty khachHang ? khachHang.id : banGhi.id)}, false, this)">
                                                                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">star</span>
                                                                    <span>Đầu mối</span>
                                                                </button>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <button type="button" class="btn btn-sm btn-outline btn-dau-moi-inactive" title="Đặt làm đầu mối chính của khách hàng" aria-label="Đặt <c:out value="${nlh.hoTen}" /> làm đầu mối chính" onclick="doiDauMoiChinh(${nlh.id}, ${not empty nlh.khachHangId ? nlh.khachHangId : (not empty khachHang ? khachHang.id : banGhi.id)}, true, this)">
                                                                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">star_outline</span>
                                                                    <span>Đặt chính</span>
                                                                </button>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td style="text-align: center;">
                                                        <div class="btn-action-group">
                                                            <button type="button" class="btn-action" title="Chỉnh sửa thông tin" aria-label="Chỉnh sửa thông tin người liên hệ <c:out value="${nlh.hoTen}" />"
                                                                    data-id="${nlh.id}"
                                                                    data-ho-ten="<c:out value="${nlh.hoTen}" />"
                                                                    data-chuc-danh="<c:out value="${nlh.chucDanh}" />"
                                                                    data-email="<c:out value="${nlh.email}" />"
                                                                    data-sdt="<c:out value="${nlh.soDienThoai}" />"
                                                                    data-vai-tro="${nlh.vaiTroQuyetDinh != null ? (nlh.vaiTroQuyetDinh.ma != null ? nlh.vaiTroQuyetDinh.ma : nlh.vaiTroQuyetDinh) : ''}"
                                                                    onclick="moModalSuaNlhTuElement(this)">
                                                                <span class="material-symbols-outlined icon-sm" aria-hidden="true">edit</span>
                                                            </button>
                                                            <button type="button" class="btn-action btn-action-transfer" title="Chuyển sang công ty khác (AC4)" aria-label="Chuyển công ty cho <c:out value="${nlh.hoTen}" />"
                                                                    data-id="${nlh.id}"
                                                                    data-ho-ten="<c:out value="${nlh.hoTen}" />"
                                                                    data-chuc-danh="<c:out value="${nlh.chucDanh}" />"
                                                                    data-vai-tro="${nlh.vaiTroQuyetDinh != null ? (nlh.vaiTroQuyetDinh.ma != null ? nlh.vaiTroQuyetDinh.ma : nlh.vaiTroQuyetDinh) : ''}"
                                                                    onclick="moModalChuyenCongTyTuElement(this)">
                                                                <span class="material-symbols-outlined icon-sm" aria-hidden="true">swap_horiz</span>
                                                            </button>
                                                            <button type="button" class="btn-action btn-action-history" title="Xem lịch sử làm việc (AC4)" aria-label="Xem lịch sử công tác của <c:out value="${nlh.hoTen}" />"
                                                                    data-id="${nlh.id}"
                                                                    data-ho-ten="<c:out value="${nlh.hoTen}" />"
                                                                    onclick="xemLichSuCongTyTuElement(this)">
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
                </div>

                <div class="tab-panel" id="panel-attachments" style="display: none;">
                    <div class="panel-section-title">
                        <span class="material-symbols-outlined" aria-hidden="true">attachment</span>
                        <span>Tệp Đính Kèm & Tài Liệu Dự Án (AC1)</span>
                    </div>

                    <div class="table-responsive" style="margin-bottom: 24px;">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Tên Tài Liệu / Tệp</th>
                                    <th>Loại Tài Liệu</th>
                                    <th>Dung Lượng</th>
                                    <th>Người Tải Lên</th>
                                    <th>Ngày Tải Lên</th>
                                    <th style="text-align: center; width: 140px;">Thao Tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty khachHang360.dsTepDinhKem}">
                                        <c:forEach var="file" items="${khachHang360.dsTepDinhKem}">
                                            <tr>
                                                <td>
                                                    <div class="file-row-item">
                                                        <div class="file-type-icon ${file.iconClass}">
                                                            <span class="material-symbols-outlined" aria-hidden="true">${file.iconName}</span>
                                                        </div>
                                                        <div>
                                                            <div class="file-meta-name"><c:out value="${file.tenFileGoc}" /></div>
                                                            <div class="file-meta-sub"><c:out value="${file.tenLoaiHienThi}" /></div>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td><span class="badge" style="background: #e0f2fe; color: #0369a1;"><c:out value="${file.loaiTep}" /></span></td>
                                                <td class="font-mono"><c:out value="${file.dungLuongHienThi}" /></td>
                                                <td><c:out value="${file.tenNguoiTaiLen}" /></td>
                                                <td><c:out value="${file.ngayTaiLenDinhDang}" /></td>
                                                <td style="text-align: center;">
                                                    <a href="${pageContext.request.contextPath}${file.duongDan}" target="_blank" class="btn-action" title="Tải xuống tệp" aria-label="Tải xuống tệp">
                                                        <span class="material-symbols-outlined" aria-hidden="true">download</span>
                                                    </a>
                                                    <a href="${pageContext.request.contextPath}${file.duongDan}" target="_blank" class="btn-action" title="Xem trước tài liệu" aria-label="Xem trước tài liệu">
                                                        <span class="material-symbols-outlined" aria-hidden="true">visibility</span>
                                                    </a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="6" style="text-align: center; color: var(--text-muted); padding: 24px;">
                                                Chưa có tệp đính kèm nào được tải lên cho khách hàng này.
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>

                    <!-- Bảng hợp đồng riêng của khách hàng -->
                    <c:if test="${not empty khachHang360.dsHopDong}">
                        <div class="panel-section-title" style="margin-top: 16px;">
                            <span class="material-symbols-outlined" aria-hidden="true">description</span>
                            <span>Hợp Đồng Pháp Lý Đã Ký (${khachHang360.dsHopDong.size()})</span>
                        </div>
                        <div class="table-responsive">
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>Số Hợp Đồng</th>
                                        <th style="text-align: right;">Giá Trị Hợp Đồng</th>
                                        <th>Ngày Ký</th>
                                        <th>Hiệu Lực</th>
                                        <th>Hết Hạn</th>
                                        <th>Trạng Thái</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="hd" items="${khachHang360.dsHopDong}">
                                        <tr>
                                            <td class="font-mono" style="font-weight: 600;"><c:out value="${hd.soHopDong}" /></td>
                                            <td style="text-align: right; font-weight: 700; color: #16a34a;">
                                                <c:out value="${thongKeNhomCongTy.dinhDangTienTe(hd.giaTriHopDong)}" />
                                            </td>
                                            <td><c:out value="${hd.ngayKyDinhDang}" /></td>
                                            <td><c:out value="${hd.ngayHieuLuc != null ? hd.ngayHieuLuc : '-'}" /></td>
                                            <td><c:out value="${hd.ngayHetHan != null ? hd.ngayHetHan : '-'}" /></td>
                                            <td><span class="badge badge-success"><c:out value="${hd.trangThai}" /></span></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:if>
                </div>
            </section>
        </div>

        <!-- STORY S3-05 AC1 & AC2: BẢNG DANH SÁCH CÁC CÔNG TY CON TRỰC THUỘC -->
        <div class="table-container" style="margin-bottom: 24px;" id="khuVucCongTyCon">
            <div style="padding: 16px 20px; background: #f8fafc; border-bottom: 1px solid var(--slate-200); display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px;">
                <div>
                    <h3 style="font-size: 16px; font-weight: 700; color: var(--slate-900); margin: 0; display: flex; align-items: center; gap: 8px;">
                        <span class="material-symbols-outlined" aria-hidden="true" style="color: var(--primary); font-size: 20px;">domain</span>
                        <span>Danh Sách Công Ty Con Trực Thuộc Tập Đoàn</span>
                    </h3>
                    <p style="font-size: 13px; color: var(--slate-500); margin: 4px 0 0 0;">
                        Toàn bộ các pháp nhân con được gắn vào công ty mẹ này. Doanh thu hợp đồng được tổng hợp tự động vào nhóm.
                    </p>
                </div>
                <div>
                    <button type="button" class="btn btn-outline" onclick="moModalGanCongTyCon()" id="btnThemCongTyConBang">
                        <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 16px;">add</span>
                        <span>Gắn Thêm Công Ty Con</span>
                    </button>
                </div>
            </div>

            <table class="data-table" id="bangCongTyCon">
                <thead>
                    <tr>
                        <th style="width: 60px; text-align: center;">STT</th>
                        <th style="width: 110px;">Mã KH</th>
                        <th>Tên Công Ty Con</th>
                        <th>Người Phụ Trách</th>
                        <th>Nhóm Kinh Doanh</th>
                        <th>Trạng Thái</th>
                        <th style="text-align: right;">Giá Trị Hợp Đồng</th>
                        <th style="width: 140px; text-align: center;">Thao Tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty thongKeNhomCongTy.danhSachCongTyCon}">
                            <c:forEach var="con" items="${thongKeNhomCongTy.danhSachCongTyCon}" varStatus="loop">
                                <tr>
                                    <td style="text-align: center; color: var(--slate-500); font-weight: 500;">${loop.index + 1}</td>
                                    <td class="font-mono"><c:out value="${con.maKhachHang}" /></td>
                                    <td>
                                        <div style="font-weight: 600; color: var(--slate-900);">
                                            <a href="${pageContext.request.contextPath}/khach-hang/chi-tiet?id=${con.id}" style="color: var(--primary); text-decoration: none;">
                                                <c:out value="${con.tenCongTy}" />
                                            </a>
                                        </div>
                                        <c:if test="${not empty con.moTaChiTiet}">
                                            <div style="font-size: 12.5px; color: var(--slate-500);"><c:out value="${con.moTaChiTiet}" /></div>
                                        </c:if>
                                    </td>
                                    <td><c:out value="${con.tenNguoiSoHuu}" /></td>
                                    <td><c:out value="${con.tenNhomKinhDoanh}" /></td>
                                    <td>
                                        <span class="badge badge-success"><c:out value="${con.trangThai}" /></span>
                                    </td>
                                    <td style="text-align: right; font-weight: 700; color: #16a34a;">
                                        <c:out value="${thongKeNhomCongTy.dinhDangTienTe(con.tongGiaTriHopDong)}" />
                                    </td>
                                    <td style="text-align: center;">
                                        <div style="display: flex; align-items: center; justify-content: center; gap: 8px;">
                                            <a href="${pageContext.request.contextPath}/khach-hang/chi-tiet?id=${con.id}" class="btn-action" title="Xem chi tiết công ty con" aria-label="Xem chi tiết công ty con">
                                                <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 16px;">visibility</span>
                                            </a>
                                            <!-- Nút gỡ bỏ khỏi công ty mẹ -->
                                            <form method="POST" action="${pageContext.request.contextPath}/khach-hang" style="display: inline;" onsubmit="return confirm('Bạn có chắc chắn muốn gỡ công ty \'${con.tenCongTy}\' khỏi nhóm công ty mẹ này?');">
                                                <input type="hidden" name="action" value="go-cong-ty-con">
                                                <input type="hidden" name="congTyConId" value="${con.id}">
                                                <input type="hidden" name="congTyMeId" value="${banGhi.id}">
                                                <button type="submit" class="btn-action" title="Gỡ bỏ khỏi công ty mẹ" aria-label="Gỡ bỏ khỏi công ty mẹ" style="color: #ef4444; border-color: #fecaca; background: #fff5f5;">
                                                    <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 16px;">link_off</span>
                                                </button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="8" style="text-align: center; padding: 36px 20px; color: var(--slate-500);">
                                    <div style="font-size: 14px; font-weight: 500;">Công ty này hiện chưa có công ty con nào trực thuộc.</div>
                                    <div style="font-size: 13px; color: var(--slate-400); margin-top: 4px;">
                                        Bấm nút <strong>"Gán Công Ty Con"</strong> phía trên để khai báo quan hệ tập đoàn mẹ - con.
                                    </div>
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>

        <!-- BẢNG DANH SÁCH TẤT CẢ HỢP ĐỒNG CỦA TOÀN BỘ NHÓM CÔNG TY (STORY S3-05 AC2) -->
        <div class="table-container" style="margin-bottom: 30px;" id="khuVucHopDongNhom">
            <div style="padding: 16px 20px; background: #f8fafc; border-bottom: 1px solid var(--slate-200);">
                <h3 style="font-size: 16px; font-weight: 700; color: var(--slate-900); margin: 0; display: flex; align-items: center; gap: 8px;">
                    <span class="material-symbols-outlined" aria-hidden="true" style="color: var(--primary); font-size: 20px;">description</span>
                    <span>Danh Sách Hợp Đồng Của Toàn Nhóm Công Ty (Tập Đoàn)</span>
                </h3>
                <p style="font-size: 13px; color: var(--slate-500); margin: 4px 0 0 0;">
                    Tổng hợp hợp đồng từ công ty mẹ và toàn bộ các công ty con trực thuộc.
                </p>
            </div>
            <table class="data-table" id="bangHopDongNhom">
                <thead>
                    <tr>
                        <th style="width: 140px;">Số Hợp Đồng</th>
                        <th>Pháp Nhân Ký Kết</th>
                        <th>Vai Trò Nhóm</th>
                        <th style="text-align: right;">Giá Trị Hợp Đồng</th>
                        <th>Ngày Ký</th>
                        <th>Trạng Thái</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty thongKeNhomCongTy.danhSachHopDongNhom}">
                            <c:forEach var="hd" items="${thongKeNhomCongTy.danhSachHopDongNhom}">
                                <tr>
                                    <td class="font-mono" style="font-weight: 600;"><c:out value="${hd.soHopDong}" /></td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/khach-hang/chi-tiet?id=${hd.khachHangId}" style="color: var(--primary); text-decoration: none; font-weight: 600;">
                                            <c:out value="${hd.tenKhachHang}" />
                                        </a>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${hd.khachHangId == banGhi.id}">
                                                <span class="badge" style="background: #e0f2fe; color: #0369a1;">Công ty mẹ</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge" style="background: #f1f5f9; color: #475569;">Công ty con</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="text-align: right; font-weight: 700; color: #16a34a;">
                                        <c:out value="${thongKeNhomCongTy.dinhDangTienTe(hd.giaTriHopDong)}" />
                                    </td>
                                    <td><c:out value="${hd.ngayKyDinhDang}" /></td>
                                    <td><span class="badge badge-success"><c:out value="${hd.trangThai}" /></span></td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="6" style="text-align: center; padding: 36px 20px; color: var(--slate-500);">
                                    <div style="font-size: 14px; font-weight: 500;">Chưa có hợp đồng nào được ghi nhận cho nhóm công ty này.</div>
                                    <div style="font-size: 13px; color: var(--slate-400); margin-top: 4px;">
                                        Hợp đồng của công ty mẹ và các công ty con khi phát sinh sẽ được tự động tổng hợp tại đây.
                                    </div>
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>

        <!-- STORY S3-08 AC1 & AC3: BẢNG YÊU CẦU HỖ TRỢ SAU BÁN TRÊN TRANG 360 -->
        <section class="ycht-table-card" id="khuVucYeuCauHoTro" style="margin-bottom: 30px;">
            <div class="ycht-table-header" style="padding: 18px 24px; background-color: var(--ycht-slate-50);">
                <div>
                    <div class="ycht-table-title" style="font-size: 16px;">
                        <span class="material-symbols-outlined" style="color: var(--ycht-primary);" aria-hidden="true">support_agent</span>
                        <span>Yêu Cầu Hỗ Trợ Sau Bán & Giữ Chân Khách Hàng</span>
                        <span class="ycht-badge ycht-badge-moi" style="background: var(--ycht-slate-200); color: var(--ycht-slate-700); font-size: 11.5px; margin-left: 8px;">
                            Tổng số: <c:out value="${not empty danhSachYeuCauHoTro ? danhSachYeuCauHoTro.size() : 0}" />
                        </span>
                        <c:if test="${(not empty thongTinRuiRo and thongTinRuiRo.soYeuCauChuaXuLy > 0) or banGhi.soYeuCauChuaXuLy > 0}">
                            <span class="ycht-badge ycht-priority-cao" style="font-size: 11.5px; margin-left: 4px;">
                                Chưa xử lý: <c:out value="${not empty thongTinRuiRo ? thongTinRuiRo.soYeuCauChuaXuLy : banGhi.soYeuCauChuaXuLy}" />
                            </span>
                        </c:if>
                    </div>
                    <p style="margin: 4px 0 0; font-size: 13px; color: var(--ycht-slate-500);">
                        Ghi nhận sự cố, theo dõi xử lý kỹ thuật và ngăn chặn nguy cơ khách hàng rời bỏ.
                    </p>
                </div>
                <div style="display: flex; gap: 10px; flex-wrap: wrap;">
                    <a href="${pageContext.request.contextPath}/yeu-cau-ho-tro?khachHangId=${banGhi.id}" class="ycht-btn ycht-btn-outline ycht-btn-sm" id="btnXemTatCaYeuCau">
                        <span class="material-symbols-outlined" aria-hidden="true">list_alt</span>
                        <span>Xem tất cả yêu cầu</span>
                    </a>
                    <button type="button" class="ycht-btn ycht-btn-primary ycht-btn-sm" onclick="moModalGhiNhanYeuCau()" id="btnGhiNhanYeuCauMoi">
                        <span class="material-symbols-outlined" aria-hidden="true">add</span>
                        <span>Ghi nhận yêu cầu hỗ trợ</span>
                    </button>
                </div>
            </div>

            <div class="ycht-table-responsive">
                <table class="ycht-data-table" id="bangYeuCauHoTroTrang360">
                    <thead>
                        <tr>
                            <th style="width: 130px;">Mã Ticket</th>
                            <th style="min-width: 250px;">Tiêu Đề / Nội Dung Yêu Cầu</th>
                            <th style="width: 140px; text-align: center;">Mức Độ Ưu Tiên</th>
                            <th style="width: 170px;">Người Xử Lý</th>
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
                                        <td>
                                            <span class="ycht-ticket-code"><c:out value="${yc.maYeuCau}" /></span>
                                        </td>
                                        <td>
                                            <div class="ycht-ticket-title"><c:out value="${yc.tieuDe}" /></div>
                                            <c:if test="${not empty yc.noiDung}">
                                                <div class="ycht-ticket-desc" title="<c:out value="${yc.noiDung}" />"><c:out value="${yc.noiDung}" /></div>
                                            </c:if>
                                            <c:if test="${not empty yc.tenNguoiLienHe}">
                                                <div class="ycht-ticket-contact">
                                                    <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">contact_mail</span>
                                                    <span>Liên hệ: <c:out value="${yc.tenNguoiLienHe}" /></span>
                                                    <c:if test="${not empty yc.sdtNguoiLienHe}">
                                                        <span>• <c:out value="${yc.sdtNguoiLienHe}" /></span>
                                                    </c:if>
                                                </div>
                                            </c:if>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${yc.mucUuTien == 'KHAN_CAP'}">
                                                    <span class="ycht-badge ycht-priority-khan-cap">
                                                        <span class="material-symbols-outlined" style="font-size: 13px;" aria-hidden="true">warning</span>
                                                        <c:out value="${yc.mucUuTienEnum.tenHienThi}" />
                                                    </span>
                                                </c:when>
                                                <c:when test="${yc.mucUuTien == 'CAO'}">
                                                    <span class="ycht-badge ycht-priority-cao">
                                                        <c:out value="${yc.mucUuTienEnum.tenHienThi}" />
                                                    </span>
                                                </c:when>
                                                <c:when test="${yc.mucUuTien == 'BINH_THUONG'}">
                                                    <span class="ycht-badge ycht-priority-binh-thuong">
                                                        <c:out value="${yc.mucUuTienEnum.tenHienThi}" />
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="ycht-badge ycht-priority-thap">
                                                        <c:out value="${yc.mucUuTienEnum.tenHienThi}" />
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div class="ycht-user-cell">
                                                <div class="ycht-user-avatar-sm" aria-hidden="true">
                                                    <c:choose>
                                                        <c:when test="${not empty yc.tenNguoiXuLy}">
                                                            <c:out value="${yc.tenNguoiXuLy.substring(0, 1)}" />
                                                        </c:when>
                                                        <c:otherwise>?</c:otherwise>
                                                    </c:choose>
                                                </div>
                                                <div class="ycht-user-info-text">
                                                    <div class="ycht-user-name">
                                                        <c:out value="${not empty yc.tenNguoiXuLy ? yc.tenNguoiXuLy : 'Chưa phân công'}" />
                                                    </div>
                                                    <c:if test="${not empty yc.emailNguoiXuLy}">
                                                        <div class="ycht-user-email"><c:out value="${yc.emailNguoiXuLy}" /></div>
                                                    </c:if>
                                                </div>
                                            </div>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${yc.trangThai == 'MOI'}">
                                                    <span class="ycht-badge ycht-status-moi"><c:out value="${yc.trangThaiEnum.tenHienThi}" /></span>
                                                </c:when>
                                                <c:when test="${yc.trangThai == 'DANG_XU_LY'}">
                                                    <span class="ycht-badge ycht-status-dang-xu-ly"><c:out value="${yc.trangThaiEnum.tenHienThi}" /></span>
                                                </c:when>
                                                <c:when test="${yc.trangThai == 'CHO_KHACH_HANG'}">
                                                    <span class="ycht-badge ycht-status-cho-khach-hang"><c:out value="${yc.trangThaiEnum.tenHienThi}" /></span>
                                                </c:when>
                                                <c:when test="${yc.trangThai == 'DA_XU_LY'}">
                                                    <span class="ycht-badge ycht-status-da-xu-ly">
                                                        <span class="material-symbols-outlined" style="font-size: 13px;" aria-hidden="true">check</span>
                                                        <c:out value="${yc.trangThaiEnum.tenHienThi}" />
                                                    </span>
                                                </c:when>
                                                <c:when test="${yc.trangThai == 'DONG'}">
                                                    <span class="ycht-badge ycht-status-dong"><c:out value="${yc.trangThaiEnum.tenHienThi}" /></span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="ycht-badge ycht-status-huy"><c:out value="${yc.trangThaiEnum.tenHienThi}" /></span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="font-size: 12.5px; color: var(--ycht-slate-600); white-space: nowrap;">
                                            <c:out value="${yc.taoLuc}" />
                                        </td>
                                        <td style="text-align: center;">
                                            <!-- Form cập nhật trạng thái nhanh kích hoạt tự động đánh giá cờ rủi ro -->
                                            <form action="${pageContext.request.contextPath}/yeu-cau-ho-tro" method="POST" class="ycht-inline-status-form">
                                                <input type="hidden" name="action" value="cap-nhat-trang-thai">
                                                <input type="hidden" name="id" value="${yc.id}">
                                                <input type="hidden" name="redirectUrl" value="${pageContext.request.contextPath}/khach-hang/chi-tiet?id=${banGhi.id}">
                                                <select name="trangThai" class="ycht-inline-status-select" onchange="this.form.submit()" aria-label="Cập nhật trạng thái yêu cầu">
                                                    <option value="MOI" ${yc.trangThai == 'MOI' ? 'selected' : ''}>Mới tiếp nhận</option>
                                                    <option value="DANG_XU_LY" ${yc.trangThai == 'DANG_XU_LY' ? 'selected' : ''}>Đang xử lý</option>
                                                    <option value="CHO_KHACH_HANG" ${yc.trangThai == 'CHO_KHACH_HANG' ? 'selected' : ''}>Chờ khách</option>
                                                    <option value="DA_XU_LY" ${yc.trangThai == 'DA_XU_LY' ? 'selected' : ''}>Đã xử lý</option>
                                                    <option value="DONG" ${yc.trangThai == 'DONG' ? 'selected' : ''}>Đã đóng</option>
                                                    <option value="HUY" ${yc.trangThai == 'HUY' ? 'selected' : ''}>Đã hủy</option>
                                                </select>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="7">
                                        <div class="ycht-empty-state" style="padding: 36px 20px;">
                                            <span class="material-symbols-outlined ycht-empty-icon" aria-hidden="true">sentiment_satisfied</span>
                                            <div class="ycht-empty-title">Chưa có yêu cầu hỗ trợ nào phát sinh</div>
                                            <p class="ycht-empty-desc">
                                                Khách hàng hiện đang hoạt động bình thường và chưa có ticket hỗ trợ nào cần xử lý.
                                            </p>
                                            <div style="margin-top: 14px;">
                                                <button type="button" class="ycht-btn ycht-btn-primary ycht-btn-sm" onclick="moModalGhiNhanYeuCau()">
                                                    <span class="material-symbols-outlined" aria-hidden="true">add</span>
                                                    <span>Ghi nhận yêu cầu hỗ trợ mới</span>
                                                </button>
                                            </div>
                                        </div>
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </section>

        <!-- Modal Ghi Nhanh Hoạt Động (Story S3-03) -->
        <div class="modal-backdrop" id="modalThemHoatDong" style="display: none;" role="dialog" aria-modal="true" aria-labelledby="modalThemHoatDongTieuDe">
            <div class="modal-card">
                <div class="modal-header">
                    <div>
                        <h2 class="modal-title" id="modalThemHoatDongTieuDe">Ghi Nhận Hoạt Động Mới</h2>
                        <p class="modal-subtitle">Ghi nhận tức thời cuộc gọi, cuộc họp hoặc ghi chú với khách hàng</p>
                    </div>
                    <button type="button" class="modal-close-btn" id="btnDongModalHoatDong" aria-label="Đóng" title="Đóng">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>
                <form id="formThemHoatDong">
                    <div class="modal-body">
                        <div class="form-group">
                            <label class="form-label" for="hoatDongLoai">Loại hoạt động <span class="required">*</span></label>
                            <select id="hoatDongLoai" class="form-select">
                                <option value="CUOC_GOI">Cuộc gọi điện thoại</option>
                                <option value="CUOC_HOP">Cuộc họp trao đổi</option>
                                <option value="EMAIL">Gửi / Nhận Email</option>
                                <option value="GHI_CHU">Ghi chú bối cảnh</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="hoatDongTieuDe">Tiêu đề hoạt động <span class="required">*</span></label>
                            <input type="text" id="hoatDongTieuDe" class="form-input" placeholder="Ví dụ: Trao đổi điều khoản thanh toán..." required autocomplete="off">
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="hoatDongNoiDung">Tóm tắt nội dung & Kết quả</label>
                            <textarea id="hoatDongNoiDung" class="form-textarea" rows="3" placeholder="Ghi nhận tóm tắt thỏa thuận hoặc bước tiếp theo..."></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline" id="btnHuyModalHoatDong">Hủy bỏ</button>
                        <button type="submit" class="btn btn-primary" id="btnLuuHoatDong">
                            <span class="material-symbols-outlined" aria-hidden="true">save</span>
                            Lưu hoạt động
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <!-- MODAL 1: GẮN KHÁCH HÀNG LÀM CÔNG TY CON (STORY S3-05 AC1) -->
        <div class="modal-overlay" id="modalGanCongTyCon" style="display: none;">
            <div class="modal-box">
                <div class="modal-box-header">
                    <h3>Gắn Công Ty Con Vào Nhóm</h3>
                    <button type="button" class="btn-close-modal" onclick="dongModalGanCongTyCon()" aria-label="Đóng cửa sổ" title="Đóng cửa sổ">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>
                <form method="POST" action="${pageContext.request.contextPath}/khach-hang" id="formGanCongTyCon">
                    <input type="hidden" name="action" value="gan-cong-ty-con">
                    <input type="hidden" name="congTyMeId" value="${banGhi.id}">
                    <div class="modal-box-body">
                        <p style="font-size: 13.5px; color: var(--slate-600); margin-top: 0; margin-bottom: 16px;">
                            Khách hàng được chọn dưới đây sẽ được khai báo là <strong>Công ty con</strong> trực thuộc công ty <strong><c:out value="${banGhi.tieuDe}" /></strong>. Doanh thu hợp đồng của công ty con sẽ tự động được cộng dồn vào tổng giá trị tập đoàn.
                        </p>
                        <div class="form-group">
                            <label class="form-label" for="selectCongTyCon">Chọn khách hàng làm công ty con <span class="required">*</span></label>
                            <select id="selectCongTyCon" name="congTyConId" class="form-select" required style="width: 100%; padding: 10px; border-radius: 6px; border: 1px solid #cbd5e1;">
                                <option value="" disabled selected>-- Chọn khách hàng trong danh mục --</option>
                                <c:forEach var="khKhaDung" items="${dsKhaDungLamCon}">
                                    <option value="${khKhaDung.id}">
                                        <c:out value="${khKhaDung.tenCongTy}" /> (<c:out value="${not empty khKhaDung.maKhachHang ? khKhaDung.maKhachHang : 'ID:' += khKhaDung.id}" /> - <c:out value="${khKhaDung.tenNguoiSoHuu}" />)
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>
                    <div class="modal-box-footer">
                        <button type="button" class="btn btn-outline" onclick="dongModalGanCongTyCon()">Hủy</button>
                        <button type="submit" class="btn btn-primary" id="btnXacNhanGanCon">
                            Xác Nhận Gắn Công Ty Con
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <!-- MODAL 2: KHAI BÁO / ĐỔI CÔNG TY MẸ (STORY S3-05 AC1) -->
        <div class="modal-overlay" id="modalChonCongTyMe" style="display: none;">
            <div class="modal-box">
                <div class="modal-box-header">
                    <h3>Khai Báo Quan Hệ Công Ty Mẹ</h3>
                    <button type="button" class="btn-close-modal" onclick="dongModalChonCongTyMe()" aria-label="Đóng cửa sổ" title="Đóng cửa sổ">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>
                <form method="POST" action="${pageContext.request.contextPath}/khach-hang" id="formCapNhatCongTyMe">
                    <input type="hidden" name="action" value="cap-nhat-cong-ty-me">
                    <input type="hidden" name="khachHangId" value="${banGhi.id}">
                    <div class="modal-box-body">
                        <p style="font-size: 13.5px; color: var(--slate-600); margin-top: 0; margin-bottom: 16px;">
                            Chọn công ty mẹ cho khách hàng <strong><c:out value="${banGhi.tieuDe}" /></strong>:
                        </p>
                        <div class="form-group">
                            <label class="form-label" for="selectCongTyMe">Chọn công ty mẹ</label>
                            <select id="selectCongTyMe" name="congTyMeId" class="form-select" style="width: 100%; padding: 10px; border-radius: 6px; border: 1px solid #cbd5e1;">
                                <option value="0">-- Độc lập (Không có công ty mẹ) --</option>
                                <c:forEach var="khMeKhaDung" items="${dsKhaDungLamMe}">
                                    <option value="${khMeKhaDung.id}" ${thongKeNhomCongTy.congTyMe.congTyMeId == khMeKhaDung.id ? 'selected' : ''}>
                                        <c:out value="${khMeKhaDung.tenCongTy}" /> (<c:out value="${not empty khMeKhaDung.maKhachHang ? khMeKhaDung.maKhachHang : 'ID:' += khMeKhaDung.id}" />)
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>
                    <div class="modal-box-footer">
                        <button type="button" class="btn btn-outline" onclick="dongModalChonCongTyMe()">Hủy</button>
                        <button type="submit" class="btn btn-primary" id="btnXacNhanDoiMe">
                            Lưu Thay Đổi
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <!-- MODAL 3: GHI NHẬN YÊU CẦU HỖ TRỢ CHO KHÁCH HÀNG HIỆN TẠI (STORY S3-08 AC1) -->
        <div class="ycht-modal-backdrop" id="modalGhiNhanYeuCau" role="dialog" aria-modal="true" aria-labelledby="modalGhiNhanYeuCauTieuDe" aria-hidden="true" style="display: none;">
            <div class="ycht-modal-card">
                <div class="ycht-modal-header">
                    <div>
                        <h2 class="ycht-modal-title" id="modalGhiNhanYeuCauTieuDe">Ghi Nhận Yêu Cầu Hỗ Trợ Sau Bán</h2>
                        <p class="ycht-modal-subtitle">Ghi nhận thông tin sự cố, thắc mắc và người xử lý cho khách hàng</p>
                    </div>
                    <button type="button" class="ycht-modal-close-btn" onclick="dongModalGhiNhanYeuCau()" aria-label="Đóng cửa sổ">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>

                <form action="${pageContext.request.contextPath}/yeu-cau-ho-tro" method="POST" id="formGhiNhanYeuCau">
                    <input type="hidden" name="action" value="tao">
                    <input type="hidden" name="khachHangId" value="${banGhi.id}">
                    <input type="hidden" name="redirectUrl" value="${pageContext.request.contextPath}/khach-hang/chi-tiet?id=${banGhi.id}">

                    <div class="ycht-modal-body">
                        <div class="ycht-form-field">
                            <label class="ycht-form-label">Khách hàng / Công ty</label>
                            <input type="text" class="ycht-form-input" value="${banGhi.tieuDe}" readonly style="background-color: var(--ycht-slate-100); color: var(--ycht-slate-700); font-weight: 600;">
                        </div>

                        <div class="ycht-form-field">
                            <label class="ycht-form-label" for="tieuDeYeuCau">
                                Tiêu đề yêu cầu hỗ trợ <span class="ycht-form-required">*</span>
                            </label>
                            <input type="text" id="tieuDeYeuCau" name="tieuDe" class="ycht-form-input" placeholder="Ví dụ: Lỗi đồng bộ dữ liệu hoá đơn điện tử..." required autocomplete="off" minlength="3">
                        </div>

                        <div class="ycht-form-row-2">
                            <div class="ycht-form-field">
                                <label class="ycht-form-label" for="mucUuTien">
                                    Mức độ ưu tiên <span class="ycht-form-required">*</span>
                                </label>
                                <select id="mucUuTien" name="mucUuTien" class="ycht-form-select" required>
                                    <option value="BINH_THUONG" selected>Bình thường</option>
                                    <option value="THAP">Thấp</option>
                                    <option value="CAO">Cao</option>
                                    <option value="KHAN_CAP">Khẩn cấp (Sự cố nghiêm trọng)</option>
                                </select>
                            </div>
                            <div class="ycht-form-field">
                                <label class="ycht-form-label" for="trangThaiYeuCau">
                                    Trạng thái ban đầu <span class="ycht-form-required">*</span>
                                </label>
                                <select id="trangThaiYeuCau" name="trangThai" class="ycht-form-select" required>
                                    <option value="MOI" selected>Mới tiếp nhận</option>
                                    <option value="DANG_XU_LY">Đang xử lý</option>
                                    <option value="CHO_KHACH_HANG">Chờ khách hàng phản hồi</option>
                                </select>
                            </div>
                        </div>

                        <div class="ycht-form-field">
                            <label class="ycht-form-label" for="nguoiXuLyId">Người xử lý (CSKH / Kỹ thuật)</label>
                            <select id="nguoiXuLyId" name="nguoiXuLyId" class="ycht-form-select">
                                <option value="">-- Chưa phân công --</option>
                                <c:forEach var="nv" items="${danhSachNhanVien}">
                                    <option value="${nv.id}">
                                        <c:out value="${nv.hoTen}" /> (<c:out value="${nv.email}" />)
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="ycht-form-field">
                            <label class="ycht-form-label" for="noiDungYeuCau">Nội dung chi tiết / Phản hồi từ khách</label>
                            <textarea id="noiDungYeuCau" name="noiDung" class="ycht-form-textarea" rows="3" placeholder="Mô tả chi tiết sự cố, yêu cầu cần xử lý..."></textarea>
                        </div>
                    </div>

                    <div class="ycht-modal-footer">
                        <button type="button" class="ycht-btn ycht-btn-outline" onclick="dongModalGhiNhanYeuCau()">Hủy bỏ</button>
                        <button type="submit" class="ycht-btn ycht-btn-primary" id="btnSubmitGhiNhanYeuCau">
                            <span class="material-symbols-outlined" aria-hidden="true">save</span>
                            <span>Lưu Yêu Cầu Hỗ Trợ</span>
                        </button>
                    </div>
                </form>
            </div>
        </div>

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

        <!-- ========================================== -->
        <!-- MODAL THÊM / SỬA NGƯỜI LIÊN HỆ (Story S3-02 AC1, AC2, AC3) -->
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
                    <input type="hidden" name="returnUrl" value="/khach-hang/360?id=${not empty khachHang ? khachHang.id : banGhi.id}">

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
                                    <option value="">-- Chọn vai trò trong quyết định mua --</option>
                                    <c:forEach var="vt" items="${dsVaiTroQuyetDinh}">
                                        <option value="${vt.ma}">${not empty vt.tenTiengViet ? vt.tenTiengViet : vt.tenHienThi} (${vt.moTa})</option>
                                    </c:forEach>
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
                            <label class="form-label" style="display: flex; align-items: center; gap: 8px; cursor: pointer;">
                                <input type="checkbox" id="nlhLaDauMoi" name="laDauMoiChinh" value="true" style="width: 17px; height: 17px;">
                                <span style="font-weight: 600; color: var(--slate-800);">Đánh dấu là đầu mối chính của khách hàng (AC3)</span>
                            </label>
                            <span style="font-size: 12px; color: var(--slate-500); display: block; margin-left: 25px;">
                                Mỗi khách hàng chỉ có tối đa một đầu mối chính. Đặt người này sẽ tự động thay thế đầu mối chính hiện tại.
                            </span>
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
        <!-- MODAL CHUYỂN CÔNG TY CHO LIÊN HỆ (Story S3-02 AC4) -->
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
                    <input type="hidden" name="returnUrl" value="/khach-hang/360?id=${not empty khachHang ? khachHang.id : banGhi.id}">

                    <div class="modal-body">
                        <div style="background-color: #f1f5f9; padding: 12px 14px; border-radius: 6px; margin-bottom: 14px; font-size: 13px; color: var(--slate-700);">
                            <span class="material-symbols-outlined" style="vertical-align: middle; font-size: 18px; color: var(--primary);" aria-hidden="true">info</span>
                            Đang chuyển: <strong id="chuyenTenNlh"></strong> (Hiện tại: <c:out value="${not empty khachHang ? khachHang.tenCongTy : banGhi.tieuDe}" />)
                        </div>

                        <div class="form-group" style="margin-bottom: 14px;">
                            <label class="form-label" for="chuyenKhachHangMoi">Công ty / Khách hàng mới <span style="color: #ef4444;">*</span></label>
                            <select id="chuyenKhachHangMoi" name="khachHangMoiId" class="form-select" required>
                                <option value="">-- Chọn công ty chuyển tới --</option>
                                <c:forEach var="khMoi" items="${dsKhachHangChuyen}">
                                    <c:if test="${khMoi.id != (not empty khachHang ? khachHang.id : banGhi.id)}">
                                        <option value="${khMoi.id}">
                                            <c:out value="${khMoi.tenCongTy}" /> (<c:out value="${khMoi.maKhachHang}" />)
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
                                    <option value="">-- Giữ nguyên hoặc chọn vai trò mới --</option>
                                    <c:forEach var="vt" items="${dsVaiTroQuyetDinh}">
                                        <option value="${vt.ma}">${not empty vt.tenTiengViet ? vt.tenTiengViet : vt.tenHienThi} (${vt.moTa})</option>
                                    </c:forEach>
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
        <!-- MODAL LỊCH SỬ CÔNG TY CỦA LIÊN HỆ (Story S3-02 AC4) -->
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

                <div class="modal-body" style="max-height: 60vh; overflow-y: auto;">
                    <div id="loadingLichSu" class="timeline-loading">
                        <span class="material-symbols-outlined" style="font-size: 28px; animation: spin 1s linear infinite;" aria-hidden="true">refresh</span>
                        <span style="font-size: 13px;">Đang tải lịch sử công tác...</span>
                    </div>

                    <div id="timelineLichSu" style="display: none;">
                        <ul class="timeline-container" id="timelineList">
                            <!-- Render bằng JavaScript từ fetch API -->
                        </ul>
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-outline" onclick="dongModalLichSu()">Đóng</button>
                </div>
            </div>
        </div>
    </main>

    <!-- Scripts chuẩn hệ thống và các module -->
    <script id="initialActivitiesJson" type="application/json">
        ${not empty hoatDongJson ? hoatDongJson : '[]'}
    </script>
    <script>
        window.CURRENT_CUSTOMER_ID = "${not empty banGhi.id ? banGhi.id : (not empty khachHang.id ? khachHang.id : 0)}";
        window.APP_CONTEXT_PATH = "${pageContext.request.contextPath}";
        window.CONTEXT_PATH = "${pageContext.request.contextPath}";
    </script>
    <script src="${pageContext.request.contextPath}/assets/js/navigation.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/session-keep-alive.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/khach-hang.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/khach-hang/chi-tiet.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/yeu-cau-ho-tro.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/khach-hang-360.js?v=20261008_tabfix"></script>
    <!-- Scripts Quản lý người liên hệ & Vai trò quyết định (Story S3-02) -->
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
            document.getElementById('nlhHoTen').value = hoTen || '';
            document.getElementById('nlhChucDanh').value = chucDanh || '';
            document.getElementById('nlhEmail').value = email || '';
            document.getElementById('nlhSdt').value = sdt || '';
            document.getElementById('nlhVaiTro').value = vaiTro || '';
            document.getElementById('groupDauMoiChinh').style.display = 'none'; // Sửa cờ đầu mối qua nút chuyên dụng
            anLoiFormNlh();
            hienThiModal('modalNguoiLienHe');
            document.getElementById('nlhHoTen').focus();
        }

        function moModalSuaNlhTuElement(btn) {
            var id = btn.getAttribute('data-id');
            var hoTen = btn.getAttribute('data-ho-ten') || '';
            var chucDanh = btn.getAttribute('data-chuc-danh') || '';
            var email = btn.getAttribute('data-email') || '';
            var sdt = btn.getAttribute('data-sdt') || '';
            var vaiTro = btn.getAttribute('data-vai-tro') || '';
            moModalSuaNlh(id, hoTen, chucDanh, email, sdt, vaiTro);
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

        var dangDoiDauMoi = false;
        function doiDauMoiChinh(nlhId, khachHangId, datChinh, btn) {
            if (dangDoiDauMoi) return;

            // BUG-02 / S3-02 AC3: Khách hàng có người liên hệ bắt buộc phải có ít nhất 1 đầu mối chính
            // Không cho phép tự ý bỏ đầu mối chính cuối cùng khi chưa chỉ định người thay thế
            if (!datChinh) {
                hienThiThongBaoToast('Mỗi khách hàng phải có ít nhất một đầu mối chính. Để thay đổi, vui lòng bấm "Đặt chính" tại người liên hệ mà bạn muốn chọn làm đầu mối mới!', 'warning');
                return;
            }

            dangDoiDauMoi = true;
            if (btn) {
                btn.disabled = true;
                btn.classList.add('loading');
                btn.style.pointerEvents = 'none';
                btn.style.opacity = '0.6';
            }

            var params = new URLSearchParams();
            params.append('action', 'set-main');
            params.append('id', nlhId);
            params.append('khachHangId', khachHangId);

            fetch('${pageContext.request.contextPath}/nguoi-lien-he', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
                    'X-Requested-With': 'XMLHttpRequest',
                    'Accept': 'application/json'
                },
                body: params.toString()
            })
            .then(function (response) {
                return response.json().then(function (data) {
                    return { ok: response.ok, status: response.status, data: data };
                });
            })
            .then(function (result) {
                if (result.ok && result.data && result.data.success) {
                    capNhatGiaoDienDauMoiChinh(nlhId, khachHangId);
                    hienThiThongBaoToast(result.data.message || 'Đã đánh dấu đầu mối chính thành công!', 'success');
                } else {
                    var msg = (result.data && result.data.message) ? result.data.message : 'Có lỗi khi cập nhật đầu mối chính.';
                    hienThiThongBaoToast(msg, 'error');
                }
            })
            .catch(function (error) {
                console.error('Lỗi gọi API đổi đầu mối chính:', error);
                hienThiThongBaoToast('Không thể kết nối tới máy chủ. Vui lòng kiểm tra lại.', 'error');
            })
            .finally(function () {
                dangDoiDauMoi = false;
                if (btn) {
                    btn.disabled = false;
                    btn.classList.remove('loading');
                    btn.style.pointerEvents = '';
                    btn.style.opacity = '1';
                }
            });
        }

        function capNhatGiaoDienDauMoiChinh(mainNlhId, khachHangId) {
            var tbody = document.getElementById('tbodyNguoiLienHe');
            if (!tbody) return;

            var rows = tbody.querySelectorAll('tr[id^="row-nlh-"]');
            rows.forEach(function (row) {
                var rowIdStr = row.id.replace('row-nlh-', '');
                var isThisMain = (rowIdStr === String(mainNlhId));

                // 1. Cột Tên người liên hệ: Cập nhật huy hiệu đầu mối chính
                var nameCell = row.querySelector('.contact-name-cell');
                if (nameCell) {
                    var existingBadge = nameCell.querySelector('.badge-dau-moi-chinh');
                    if (isThisMain) {
                        if (!existingBadge) {
                            var badge = document.createElement('span');
                            badge.className = 'badge-dau-moi-chinh';
                            badge.title = 'Đầu mối chính của khách hàng';
                            badge.innerHTML = '<span class="material-symbols-outlined" aria-hidden="true">star</span> <span>Đầu mối chính</span>';
                            nameCell.appendChild(badge);
                        }
                    } else {
                        if (existingBadge) {
                            existingBadge.remove();
                        }
                    }
                }

                // 2. Cột Đầu mối chính (AC3): Cập nhật nút Đặt chính / Đầu mối
                var cells = row.getElementsByTagName('td');
                for (var i = 0; i < cells.length; i++) {
                    var cell = cells[i];
                    if (cell.querySelector('.btn-dau-moi-active') || cell.querySelector('.btn-dau-moi-inactive')) {
                        if (isThisMain) {
                            cell.innerHTML = '<button type="button" class="btn btn-sm btn-dau-moi-active" title="Đầu mối chính hiện tại" aria-label="Đầu mối chính" onclick="doiDauMoiChinh(' + rowIdStr + ', ' + khachHangId + ', false, this)">' +
                                '<span class="material-symbols-outlined icon-xs" aria-hidden="true">star</span>' +
                                ' <span>Đầu mối</span>' +
                                '</button>';
                        } else {
                            cell.innerHTML = '<button type="button" class="btn btn-sm btn-outline btn-dau-moi-inactive" title="Đặt làm đầu mối chính của khách hàng" aria-label="Đặt làm đầu mối chính" onclick="doiDauMoiChinh(' + rowIdStr + ', ' + khachHangId + ', true, this)">' +
                                '<span class="material-symbols-outlined icon-xs" aria-hidden="true">star_outline</span>' +
                                ' <span>Đặt chính</span>' +
                                '</button>';
                        }
                        break;
                    }
                }
            });
        }

        function hienThiThongBaoToast(message, loai) {
            var container = document.getElementById('crmToastContainer');
            if (!container) {
                container = document.createElement('div');
                container.id = 'crmToastContainer';
                container.className = 'crm-toast-container';
                document.body.appendChild(container);
            }

            var toastClass = 'crm-toast-info';
            var iconName = 'info';
            if (loai === 'success') {
                toastClass = 'crm-toast-success';
                iconName = 'check_circle';
            } else if (loai === 'error') {
                toastClass = 'crm-toast-danger';
                iconName = 'error';
            } else if (loai === 'warning') {
                toastClass = 'crm-toast-warning';
                iconName = 'warning';
            }

            var toast = document.createElement('div');
            toast.className = 'crm-toast ' + toastClass;
            toast.innerHTML = '<span class="material-symbols-outlined crm-toast-icon" aria-hidden="true">' + iconName + '</span>' +
                '<div class="crm-toast-body">' + escapeHtmlNlh(message) + '</div>' +
                '<button type="button" class="crm-toast-close" aria-label="Đóng">&times;</button>';

            var closeBtn = toast.querySelector('.crm-toast-close');
            if (closeBtn) {
                closeBtn.addEventListener('click', function () {
                    toast.remove();
                });
            }

            container.appendChild(toast);

            setTimeout(function () {
                if (toast.parentNode) {
                    toast.style.opacity = '0';
                    toast.style.transform = 'translateX(20px)';
                    setTimeout(function () {
                        if (toast.parentNode) toast.parentNode.removeChild(toast);
                    }, 300);
                }
            }, 4000);
        }

        function moModalChuyenCongTy(id, hoTen, chucDanh, vaiTro) {
            document.getElementById('chuyenNlhId').value = id;
            document.getElementById('chuyenTenNlh').innerText = hoTen || '';
            document.getElementById('chuyenChucDanhMoi').value = chucDanh || '';
            document.getElementById('chuyenVaiTroMoi').value = vaiTro || '';
            document.getElementById('chuyenKhachHangMoi').value = '';
            document.getElementById('chuyenGhiChu').value = '';
            var err = document.getElementById('errChuyenKh');
            if (err) err.style.display = 'none';
            hienThiModal('modalChuyenCongTy');
        }

        function moModalChuyenCongTyTuElement(btn) {
            var id = btn.getAttribute('data-id');
            var hoTen = btn.getAttribute('data-ho-ten') || '';
            var chucDanh = btn.getAttribute('data-chuc-danh') || '';
            var vaiTro = btn.getAttribute('data-vai-tro') || '';
            moModalChuyenCongTy(id, hoTen, chucDanh, vaiTro);
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
            document.getElementById('modalLichSuSubtitle').innerText = 'Lịch sử công tác của: ' + (hoTen || '');
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
                        var rCode = item.vaiTroQuyetDinh || item.vaiTro;
                        if (rCode) {
                            if (rCode === 'NGUOI_QUYET_DINH') {
                                roleBadgeHtml = '<span class="badge-vai-tro badge-quyet-dinh"><span class="material-symbols-outlined" aria-hidden="true">verified_user</span>' + escapeHtmlNlh(item.tenVaiTro || 'Người quyết định') + '</span>';
                            } else if (rCode === 'NGUOI_ANH_HUONG') {
                                roleBadgeHtml = '<span class="badge-vai-tro badge-anh-huong"><span class="material-symbols-outlined" aria-hidden="true">insights</span>' + escapeHtmlNlh(item.tenVaiTro || 'Người ảnh hưởng') + '</span>';
                            } else if (rCode === 'NGUOI_DUNG_CUOI') {
                                roleBadgeHtml = '<span class="badge-vai-tro badge-dung-cuoi"><span class="material-symbols-outlined" aria-hidden="true">person</span>' + escapeHtmlNlh(item.tenVaiTro || 'Người dùng cuối') + '</span>';
                            } else if (rCode === 'NGUOI_CAN_TRO') {
                                roleBadgeHtml = '<span class="badge-vai-tro badge-can-tro"><span class="material-symbols-outlined" aria-hidden="true">block</span>' + escapeHtmlNlh(item.tenVaiTro || 'Người cản trở') + '</span>';
                            } else {
                                roleBadgeHtml = '<span class="badge-vai-tro badge-chua-xac-dinh">' + escapeHtmlNlh(item.tenVaiTro || 'Chưa xác định') + '</span>';
                            }
                        } else {
                            roleBadgeHtml = '<span class="badge-vai-tro badge-chua-xac-dinh">Chưa xác định</span>';
                        }

                        var html = '<span class="' + dotClass + '"></span>' +
                            '<div class="timeline-card">' +
                                '<div class="timeline-header">' +
                                    '<span class="timeline-company-name">' + escapeHtmlNlh(item.tenCongTy) + '</span>' +
                                    '<span class="timeline-period">' +
                                        '<span>' + (item.tuNgay || '...') + ' &rarr; </span>' +
                                        '<span class="' + periodBadgeClass + '">' + escapeHtmlNlh(item.denNgay) + '</span>' +
                                    '</span>' +
                                '</div>' +
                                '<div class="timeline-position">' +
                                    '<span>Chức danh: <strong>' + escapeHtmlNlh(item.chucDanh || '—') + '</strong></span>' +
                                    '<span style="color: #94a3b8;">&bull;</span>' +
                                    '<span>Vai trò: </span>' + roleBadgeHtml +
                                '</div>';

                        if (item.ghiChu) {
                            html += '<div class="timeline-note">' + escapeHtmlNlh(item.ghiChu) + '</div>';
                        }

                        html += '</div>';
                        li.innerHTML = html;
                        ul.appendChild(li);
                    });
                } else {
                    ul.innerHTML = '<li style="color: #94a3b8; font-style: italic; padding: 12px 0;">Chưa ghi nhận lịch sử công tác nào cho người liên hệ này.</li>';
                }
                document.getElementById('timelineLichSu').style.display = 'block';
            })
            .catch(function(err) {
                document.getElementById('loadingLichSu').style.display = 'none';
                document.getElementById('timelineLichSu').style.display = 'block';
                document.getElementById('timelineList').innerHTML = '<li style="color: #ef4444; padding: 12px 0;">Lỗi tải lịch sử công tác: ' + escapeHtmlNlh(err.message) + '</li>';
            });
        }

        function xemLichSuCongTyTuElement(btn) {
            var id = btn.getAttribute('data-id');
            var hoTen = btn.getAttribute('data-ho-ten') || '';
            xemLichSuCongTy(id, hoTen);
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

        // Bắt sự kiện phím ESC và click ra ngoài backdrop để đóng modal S3-02
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

        function escapeHtmlNlh(text) {
            if (!text) return '';
            var map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
            return text.replace(/[&<>"']/g, function(m) { return map[m]; });
        }
    </script>
</body>
</html>
