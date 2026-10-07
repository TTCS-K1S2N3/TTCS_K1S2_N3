<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Chi tiết khách hàng 360 độ & Quan hệ công ty mẹ - con - Hệ thống CRM Bán Hàng">
    <title><c:out value="${not empty banGhi.tieuDe ? banGhi.tieuDe : 'Chi Tiết Khách Hàng'}" /> - CRM Bán Hàng</title>
    <!-- CSS dùng chung toàn hệ thống -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/khach-hang/khach-hang.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/khach-hang/chi-tiet.css">
    <!-- CSS chuyên biệt module Yêu cầu hỗ trợ & Cờ rủi ro (Story S3-08) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/khach-hang/yeu-cau-ho-tro.css">
</head>
<body class="crm-body">
    <!-- Thanh điều hướng và Sidebar chuẩn hệ thống S1-06 (đã bao gồm Material Symbols Outlined) -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <!-- Breadcrumb -->
        <nav class="breadcrumb-nav" aria-label="Breadcrumb">
            <ol class="breadcrumb-list">
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/dieu-huong">Trang chủ</a></li>
                <span class="breadcrumb-separator">/</span>
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/khach-hang">Khách hàng</a></li>
                <span class="breadcrumb-separator">/</span>
                <li class="breadcrumb-item active" aria-current="page"><c:out value="${not empty banGhi.tieuDe ? banGhi.tieuDe : 'Chi tiết'}" /></li>
            </ol>
        </nav>

        <!-- Thông báo thành công / lỗi -->
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

        <!-- STORY S3-05: BANNER KPI TỔNG GIÁ TRỊ HỢP ĐỒNG CỦA CẢ NHÓM CÔNG TY -->
        <div class="group-summary-banner">
            <div class="kpi-card highlight" id="kpiTongGiaTriTapDoan">
                <span class="kpi-label">Tổng Giá Trị Hợp Đồng Nhóm Công Ty</span>
                <span class="kpi-value" id="valTongGiaTriTapDoan">
                    <c:out value="${not empty thongKeNhomCongTy ? thongKeNhomCongTy.tongGiaTriHopDongNhomCongTyDinhDang : '0 ₫'}" />
                </span>
                <span class="kpi-desc">
                    Gồm công ty mẹ + <strong><c:out value="${not empty thongKeNhomCongTy ? thongKeNhomCongTy.soLuongCongTyCon : 0}" /></strong> công ty con trực thuộc
                </span>
            </div>

            <div class="kpi-card" id="kpiGiaTriMe">
                <span class="kpi-label">Hợp Đồng Công Ty Này</span>
                <span class="kpi-value" id="valGiaTriMe" style="color: #0284c7;">
                    <c:out value="${not empty thongKeNhomCongTy ? thongKeNhomCongTy.tongGiaTriHopDongCongTyMeDinhDang : '0 ₫'}" />
                </span>
                <span class="kpi-desc">Doanh số ký kết của riêng pháp nhân này</span>
            </div>

            <div class="kpi-card" id="kpiGiaTriCacCon">
                <span class="kpi-label">Hợp Đồng Các Công Ty Con</span>
                <span class="kpi-value" id="valGiaTriCacCon" style="color: #16a34a;">
                    <c:out value="${not empty thongKeNhomCongTy ? thongKeNhomCongTy.tongGiaTriHopDongCacCongTyConDinhDang : '0 ₫'}" />
                </span>
                <span class="kpi-desc">Tổng giá trị từ tất cả các chi nhánh / công ty con</span>
            </div>

            <div class="kpi-card" id="kpiSoLuongHopDong">
                <span class="kpi-label">Quy Mô Nhóm Công Ty</span>
                <span class="kpi-value" style="color: #475569;">
                    <c:out value="${not empty thongKeNhomCongTy ? thongKeNhomCongTy.soLuongCongTyCon : 0}" /> <span style="font-size: 15px; font-weight: 500;">công ty con</span>
                </span>
                <span class="kpi-desc">Tổng số hợp đồng toàn nhóm: <strong><c:out value="${not empty thongKeNhomCongTy ? thongKeNhomCongTy.tongSoHopDong : 0}" /></strong></span>
            </div>
        </div>

        <!-- Thẻ chi tiết bản ghi (Khách hàng) -->
        <div class="detail-card">
            <div class="detail-header">
                <div class="detail-title-group">
                    <h1>
                        <span class="material-symbols-outlined" aria-hidden="true" style="color: var(--primary);">corporate_fare</span>
                        <span><c:out value="${banGhi.tieuDe}" /></span>
                    </h1>
                    <p>
                        Mã khách hàng: <span class="font-mono" style="font-weight: 600;"><c:out value="${banGhi.maBanGhi}" /></span> •
                        Thuộc quyền: <strong><c:out value="${banGhi.tenNguoiPhuTrach}" /></strong> (<c:out value="${banGhi.tenNhom}" />)
                    </p>
                </div>
                <div style="display: flex; align-items: center; gap: 10px; flex-wrap: wrap;">
                    <c:if test="${banGhi.coRuiRo or (not empty thongTinRuiRo and thongTinRuiRo.coRuiRo)}">
                        <span class="ycht-risk-flag-badge">
                            <span class="material-symbols-outlined" aria-hidden="true">flag</span>
                            <span>RỦI RO RỜI BỎ</span>
                        </span>
                    </c:if>

                    <!-- Thông tin công ty mẹ nếu có -->
                    <c:choose>
                        <c:when test="${not empty thongKeNhomCongTy.congTyMe.congTyMeId}">
                            <div class="parent-info-badge" title="Khách hàng này là công ty con">
                                <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 18px;">account_tree</span>
                                <span>Công ty mẹ: </span>
                                <a href="${pageContext.request.contextPath}/khach-hang/chi-tiet?id=${thongKeNhomCongTy.congTyMe.congTyMeId}">
                                    <c:out value="${thongKeNhomCongTy.congTyMe.tenCongTyMe}" />
                                </a>
                                <button type="button" class="btn-link" onclick="moModalChonCongTyMe()" title="Thay đổi hoặc hủy công ty mẹ" style="color: #64748b; margin-left: 4px; font-size: 12px; cursor: pointer; border: none; background: none; text-decoration: underline;">
                                    (Đổi)
                                </button>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <button type="button" class="btn btn-outline" onclick="moModalChonCongTyMe()" id="btnKhaiBaoCongTyMe" style="font-size: 13px;">
                                <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 16px;">account_tree</span>
                                <span>Gán Vào Công Ty Mẹ</span>
                            </button>
                        </c:otherwise>
                    </c:choose>

                    <button type="button" class="btn btn-primary" onclick="moModalGanCongTyCon()" id="btnGanCongTyCon" style="font-size: 13px;">
                        <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 16px;">add_circle</span>
                        <span>Gán Công Ty Con</span>
                    </button>

                    <a href="${pageContext.request.contextPath}/khach-hang" class="btn btn-outline" id="btnBackKhachHang">
                        <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span>
                        <span>Danh sách</span>
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
                        <span class="detail-label">Người phụ trách</span>
                        <span class="detail-value"><c:out value="${banGhi.tenNguoiPhuTrach}" /></span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Nhóm kinh doanh</span>
                        <span class="detail-value"><c:out value="${banGhi.tenNhom}" /></span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Doanh thu ước tính</span>
                        <span class="detail-value"><c:out value="${not empty banGhi.giaTri ? banGhi.giaTri : '-'}" /></span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Trạng thái</span>
                        <span class="detail-value"><c:out value="${banGhi.trangThai}" /></span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Ngày tạo</span>
                        <span class="detail-value"><c:out value="${not empty banGhi.ngayTao ? banGhi.ngayTao : '-'}" /></span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Công ty mẹ</span>
                        <span class="detail-value">
                            <c:choose>
                                <c:when test="${not empty thongKeNhomCongTy.congTyMe.congTyMeId}">
                                    <a href="${pageContext.request.contextPath}/khach-hang/chi-tiet?id=${thongKeNhomCongTy.congTyMe.congTyMeId}" style="color: var(--primary); font-weight: 600; text-decoration: none;">
                                        <c:out value="${thongKeNhomCongTy.congTyMe.tenCongTyMe}" />
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <span style="color: var(--slate-400); font-style: italic;">Là công ty độc lập (hoặc công ty mẹ)</span>
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>

                    <div class="detail-item">
                        <span class="detail-label">Cờ rủi ro rời bỏ</span>
                        <span class="detail-value">
                            <c:choose>
                                <c:when test="${banGhi.coRuiRo or (not empty thongTinRuiRo and thongTinRuiRo.coRuiRo)}">
                                    <strong style="color: #ef4444;">Đang gắn cờ rủi ro rời bỏ</strong>
                                </c:when>
                                <c:otherwise>
                                    <span style="color: #16a34a;">Bình thường / Chưa phát hiện rủi ro</span>
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>

                    <c:if test="${not empty banGhi.moTaChiTiet}">
                        <div class="detail-item" style="grid-column: 1 / -1;">
                            <span class="detail-label">Mô tả chi tiết</span>
                            <span class="detail-value"><c:out value="${banGhi.moTaChiTiet}" /></span>
                        </div>
                    </c:if>
                </div>
            </div>
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
                                        Bấm nút <strong>"Gắn Công Ty Con"</strong> phía trên để khai báo quan hệ tập đoàn mẹ - con.
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

    </main>

    <!-- Scripts chuẩn hệ thống và các module -->
    <script src="${pageContext.request.contextPath}/assets/js/navigation.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/khach-hang/chi-tiet.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/yeu-cau-ho-tro.js"></script>
</body>
</html>
