<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%!
    public static String formatDateTime(java.time.LocalDateTime ldt) {
        if (ldt == null) return "Chưa ghi nhận";
        try {
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return ldt.format(dtf);
        } catch (Exception e) {
            return ldt.toString();
        }
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Chi tiết khách hàng tiềm năng, tiếp nhận, từ chối và theo dõi ràng buộc SLA phản hồi - CRM Bán Hàng Nhóm 10">
    <title>Chi Tiết Lead: <c:out value="${leadChiTiet.hoTen}" /> - CRM Bán Hàng</title>
    <!-- CSS dùng chung toàn hệ thống -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <!-- CSS chuyên biệt module Chi tiết Lead & SLA (Story S4-07) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lead/chi-tiet-lead.css">
</head>
<body class="crm-body">
    <!-- Navigation layout dùng chung S1-06 (đã bao gồm sidebar, thông tin user và Material Symbols) -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <div class="ctl-container">

            <!-- 1. Breadcrumb điều hướng chuẩn -->
            <nav class="ctl-breadcrumb" aria-label="Breadcrumb">
                <ol class="ctl-breadcrumb-nav">
                    <li><a href="${pageContext.request.contextPath}/dieu-huong" class="ctl-breadcrumb-link">Trang chủ</a></li>
                    <span class="ctl-breadcrumb-sep" aria-hidden="true">/</span>
                    <li><a href="${pageContext.request.contextPath}/lead" class="ctl-breadcrumb-link">Lead & Phân bổ</a></li>
                    <span class="ctl-breadcrumb-sep" aria-hidden="true">/</span>
                    <li class="ctl-breadcrumb-current" aria-current="page">Chi tiết Lead #${leadChiTiet.id}</li>
                </ol>
                <div>
                    <a href="${pageContext.request.contextPath}/lead" class="ctl-btn ctl-btn-outline" id="btnQuayLaiDanhSach" title="Quay lại danh sách Lead">
                        <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span>
                        <span>Danh sách Lead</span>
                    </a>
                </div>
            </nav>

            <!-- 2. Thông báo Flash (PRG) / Lỗi nếu có -->
            <c:if test="${not empty thongBao}">
                <div class="ctl-alert ctl-alert-success" role="alert" id="alertThanhCong">
                    <span class="material-symbols-outlined ctl-alert-icon" aria-hidden="true">check_circle</span>
                    <div class="ctl-alert-content">
                        <div class="ctl-alert-title">Thành công!</div>
                        <div><c:out value="${thongBao}" /></div>
                    </div>
                </div>
            </c:if>

            <c:if test="${not empty thongBaoLoi}">
                <div class="ctl-alert ctl-alert-danger" role="alert" id="alertLoi">
                    <span class="material-symbols-outlined ctl-alert-icon" aria-hidden="true">error</span>
                    <div class="ctl-alert-content">
                        <div class="ctl-alert-title">Lỗi thao tác!</div>
                        <div><c:out value="${thongBaoLoi}" /></div>
                    </div>
                </div>
            </c:if>

            <!-- 3. Header Card & Hành động chính (AC1, AC2) -->
            <header class="ctl-header-card">
                <div class="ctl-header-main">
                    <div class="ctl-lead-avatar" aria-hidden="true">
                        <span class="material-symbols-outlined" style="font-size: 32px;">person</span>
                    </div>
                    <div class="ctl-header-info">
                        <div class="ctl-header-title-row">
                            <h1 class="ctl-lead-name" id="leadHoTen">
                                <c:out value="${leadChiTiet.hoTen}" />
                            </h1>
                            <span class="ctl-badge ctl-badge-slate" id="leadMaHienThi">
                                Mã: <c:out value="${leadChiTiet.maLead != null ? leadChiTiet.maLead : '#' += leadChiTiet.id}" />
                            </span>
                            <!-- Badge Trạng thái Lead -->
                            <c:choose>
                                <c:when test="${leadChiTiet.trangThai == 'DANG_CHAM_SOC'}">
                                    <span class="ctl-badge ctl-badge-success" id="badgeTrangThaiLead">
                                        <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">support_agent</span>
                                        Đang chăm sóc
                                    </span>
                                </c:when>
                                <c:when test="${leadChiTiet.trangThai == 'TU_CHOI'}">
                                    <span class="ctl-badge ctl-badge-danger" id="badgeTrangThaiLead">
                                        <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">cancel</span>
                                        Từ chối
                                    </span>
                                </c:when>
                                <c:when test="${leadChiTiet.trangThai == 'DA_CHUYEN_DOI'}">
                                    <span class="ctl-badge ctl-badge-primary" id="badgeTrangThaiLead">
                                        <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">verified</span>
                                        Đã chuyển đổi
                                    </span>
                                </c:when>
                                <c:otherwise>
                                    <span class="ctl-badge ctl-badge-info" id="badgeTrangThaiLead">
                                        <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">pending_actions</span>
                                        Chờ tiếp nhận
                                    </span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="ctl-header-sub">
                            <c:if test="${not empty leadChiTiet.tenCongTy}">
                                <span class="ctl-header-sub-item">
                                    <span class="material-symbols-outlined" style="font-size: 18px;" aria-hidden="true">business</span>
                                    <c:out value="${leadChiTiet.tenCongTy}" />
                                </span>
                            </c:if>
                            <span class="ctl-header-sub-item">
                                <span class="material-symbols-outlined" style="font-size: 18px;" aria-hidden="true">assignment_ind</span>
                                Phụ trách: <strong><c:out value="${leadChiTiet.tenNguoiPhuTrach != null ? leadChiTiet.tenNguoiPhuTrach : 'Chưa phân bổ'}" /></strong>
                            </span>
                        </div>
                    </div>
                </div>

                <!-- Nút Hành Động (Nhận / Từ chối / Chuông thông báo) -->
                <div class="ctl-header-actions">
                    <!-- Chuông thông báo (Q5) -->
                    <a href="${pageContext.request.contextPath}/thong-bao" class="ctl-btn-bell" id="btnChuongThongBao" title="Thông báo hệ thống (AC3)">
                        <span class="material-symbols-outlined" aria-hidden="true">notifications</span>
                        <c:if test="${soThongBaoChuaDoc > 0}">
                            <span class="ctl-badge-bell-count" id="badgeSoThongBaoChuaDoc">${soThongBaoChuaDoc > 99 ? '99+' : soThongBaoChuaDoc}</span>
                        </c:if>
                    </a>

                    <!-- Chấm điểm Lead (S4-05) -->
                    <form action="${pageContext.request.contextPath}/lead/cham-diem-lead" method="POST" style="display: inline; margin: 0;">
                        <input type="hidden" name="id" value="${leadChiTiet.id}">
                        <button type="submit" class="ctl-btn ctl-btn-outline" id="btnChamDiemChiTiet" title="Chấm điểm lại Lead theo quy tắc S4-05">
                            <span class="material-symbols-outlined" style="color: #7c3aed;" aria-hidden="true">award_star</span>
                            <span>Chấm điểm</span>
                        </button>
                    </form>

                    <!-- AC1: Nút Nhận Lead -->
                    <c:if test="${coTheNhan}">
                        <form action="${pageContext.request.contextPath}/lead/xu-ly" method="POST" id="formNhanLead" style="display: inline;">
                            <input type="hidden" name="leadId" value="${leadChiTiet.id}">
                            <input type="hidden" name="hanhDong" value="NHAN">
                            <button type="submit" class="ctl-btn ctl-btn-primary" id="btnNhanLead" title="Tiếp nhận Lead để bắt đầu chăm sóc">
                                <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                                <span>Nhận Lead</span>
                            </button>
                        </form>
                    </c:if>

                    <!-- AC2: Nút Mở Modal Từ Chối Lead -->
                    <c:if test="${coTheTuChoi}">
                        <button type="button" class="ctl-btn ctl-btn-danger" id="btnMoModalTuChoi" title="Từ chối phân bổ Lead và nhập lý do">
                            <span class="material-symbols-outlined" aria-hidden="true">cancel</span>
                            <span>Từ chối</span>
                        </button>
                    </c:if>
                </div>
            </header>

            <!-- 4. KHU VỰC CẢNH BÁO RÀNG BUỘC SLA (AC3) -->
            <c:choose>
                <c:when test="${trangThaiSla == 'QUA_HAN' || leadChiTiet.quaSla}">
                    <section class="ctl-sla-banner ctl-sla-banner-overdue" id="slaBannerOverdue" aria-label="Cảnh báo quá hạn SLA">
                        <div class="ctl-sla-banner-icon-wrap" aria-hidden="true">
                            <span class="material-symbols-outlined" style="font-size: 28px;">notification_important</span>
                        </div>
                        <div class="ctl-sla-banner-body">
                            <div class="ctl-sla-banner-title">
                                <span>CẢNH BÁO: QUÁ HẠN PHẢN HỒI SLA</span>
                                <span class="ctl-badge ctl-badge-danger">Gắn cờ vi phạm</span>
                            </div>
                            <div class="ctl-sla-banner-desc">
                                Lead đã quá thời hạn phản hồi mà chưa có bằng chứng liên hệ thực tế (cuộc gọi, email hoặc cuộc gặp). Hệ thống đã tự động kích hoạt cảnh báo gửi tới Trưởng nhóm kinh doanh phụ trách theo AC3.
                            </div>
                            <div class="ctl-sla-banner-tags">
                                <span class="ctl-sla-tag">
                                    <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">alarm</span>
                                    Hạn chót SLA: <strong><%= formatDateTime(((vn.nhom10.crm.model.Lead) request.getAttribute("leadChiTiet")).getHanPhanHoiSla()) %></strong>
                                </span>
                                <span class="ctl-sla-tag">
                                    <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">contact_phone</span>
                                    Tình trạng liên hệ: <strong>Chưa có hoạt động liên hệ hợp lệ</strong>
                                </span>
                            </div>
                        </div>
                    </section>
                </c:when>

                <c:when test="${trangThaiSla == 'DUNG_HAN'}">
                    <section class="ctl-sla-banner ctl-sla-banner-ontime" id="slaBannerOnTime" aria-label="Thông tin SLA trong hạn">
                        <div class="ctl-sla-banner-icon-wrap" aria-hidden="true">
                            <span class="material-symbols-outlined" style="font-size: 28px;">timer</span>
                        </div>
                        <div class="ctl-sla-banner-body">
                            <div class="ctl-sla-banner-title">
                                <span>TRONG HẠN PHẢN HỒI SLA</span>
                                <span class="ctl-badge ctl-badge-success">Đúng hạn</span>
                            </div>
                            <div class="ctl-sla-banner-desc">
                                Ràng buộc SLA phản hồi bắt đầu tính từ thời điểm Lead được phân bổ cho nhân viên kinh doanh (Q1). Vui lòng thực hiện liên hệ (cuộc gọi, email, cuộc gặp) trước thời hạn quy định.
                            </div>
                            <div class="ctl-sla-banner-tags">
                                <span class="ctl-sla-tag">
                                    <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">schedule</span>
                                    Hạn chót SLA: <strong><%= formatDateTime(((vn.nhom10.crm.model.Lead) request.getAttribute("leadChiTiet")).getHanPhanHoiSla()) %></strong>
                                </span>
                                <span class="ctl-sla-tag">
                                    <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">history</span>
                                    Liên hệ đầu tiên: <strong><%= formatDateTime(((vn.nhom10.crm.model.Lead) request.getAttribute("leadChiTiet")).getLienHeDauLuc()) %></strong>
                                </span>
                            </div>
                        </div>
                    </section>
                </c:when>

                <c:otherwise>
                    <section class="ctl-sla-banner ctl-sla-banner-neutral" id="slaBannerNeutral" aria-label="Thông tin SLA">
                        <div class="ctl-sla-banner-icon-wrap" aria-hidden="true">
                            <span class="material-symbols-outlined" style="font-size: 28px;">info</span>
                        </div>
                        <div class="ctl-sla-banner-body">
                            <div class="ctl-sla-banner-title">
                                <span>THỜI HẠN SLA PHẢN HỒI</span>
                                <span class="ctl-badge ctl-badge-slate">Chưa áp dụng</span>
                            </div>
                            <div class="ctl-sla-banner-desc">
                                Lead chưa được phân bổ hoặc cấu hình SLA phản hồi đang ở trạng thái không xác định.
                            </div>
                        </div>
                    </section>
                </c:otherwise>
            </c:choose>

            <!-- 5. BỐ CỤC NỘI DUNG CHÍNH (2 CỘT) -->
            <div class="ctl-grid-layout">

                <!-- Cột trái: Thông tin liên hệ & nhu cầu Lead -->
                <div class="ctl-col-main">
                    <article class="ctl-card" id="cardThongTinLienHe">
                        <div class="ctl-card-header">
                            <h2 class="ctl-card-title">
                                <span class="material-symbols-outlined" style="color: var(--ctl-primary);" aria-hidden="true">contact_mail</span>
                                <span>Thông Tin Liên Hệ Khách Hàng Tiềm Năng</span>
                            </h2>
                        </div>
                        <div class="ctl-card-body">
                            <div class="ctl-info-grid">
                                <div class="ctl-info-item">
                                    <span class="ctl-info-label">Họ và tên</span>
                                    <span class="ctl-info-value" id="valHoTen"><c:out value="${leadChiTiet.hoTen}" /></span>
                                </div>
                                <div class="ctl-info-item">
                                    <span class="ctl-info-label">Số điện thoại</span>
                                    <span class="ctl-info-value" id="valSoDienThoai">
                                        <c:choose>
                                            <c:when test="${not empty leadChiTiet.soDienThoai}">
                                                <a href="tel:${leadChiTiet.soDienThoai}"><c:out value="${leadChiTiet.soDienThoai}" /></a>
                                            </c:when>
                                            <c:otherwise>--</c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                                <div class="ctl-info-item">
                                    <span class="ctl-info-label">Địa chỉ Email</span>
                                    <span class="ctl-info-value" id="valEmail">
                                        <c:choose>
                                            <c:when test="${not empty leadChiTiet.email}">
                                                <a href="mailto:${leadChiTiet.email}"><c:out value="${leadChiTiet.email}" /></a>
                                            </c:when>
                                            <c:otherwise>--</c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                                <div class="ctl-info-item">
                                    <span class="ctl-info-label">Công ty / Tổ chức</span>
                                    <span class="ctl-info-value" id="valCongTy"><c:out value="${leadChiTiet.tenCongTy != null ? leadChiTiet.tenCongTy : '--'}" /></span>
                                </div>
                                <div class="ctl-info-item" style="grid-column: 1 / -1;">
                                    <span class="ctl-info-label">Nhu cầu quan tâm</span>
                                    <span class="ctl-info-value" id="valNhuCau"><c:out value="${leadChiTiet.nhuCauQuanTam != null ? leadChiTiet.nhuCauQuanTam : '--'}" /></span>
                                </div>
                            </div>

                            <!-- Lịch sử từ chối nếu có -->
                            <c:if test="${not empty leadChiTiet.lyDoTuChoi}">
                                <div class="ctl-rejection-box" id="hopLyDoTuChoiCu">
                                    <div class="ctl-rejection-title">
                                        <span class="material-symbols-outlined" aria-hidden="true">history_toggle_off</span>
                                        <span>Lý do từ chối ghi nhận trước đó:</span>
                                    </div>
                                    <div class="ctl-rejection-content"><c:out value="${leadChiTiet.lyDoTuChoi}" /></div>
                                </div>
                            </c:if>
                        </div>
                    </article>

                    <article class="ctl-card" id="cardNguonGocLead">
                        <div class="ctl-card-header">
                            <h2 class="ctl-card-title">
                                <span class="material-symbols-outlined" style="color: var(--ctl-primary);" aria-hidden="true">source</span>
                                <span>Nguồn Gốc & Phân Loại Lead</span>
                            </h2>
                        </div>
                        <div class="ctl-card-body">
                            <div class="ctl-info-grid">
                                <div class="ctl-info-item">
                                    <span class="ctl-info-label">Nguồn Lead</span>
                                    <span class="ctl-info-value"><c:out value="${leadChiTiet.tenNguonLead != null ? leadChiTiet.tenNguonLead : '--'}" /></span>
                                </div>
                                <div class="ctl-info-item">
                                    <span class="ctl-info-label">Chiến dịch Marketing</span>
                                    <span class="ctl-info-value"><c:out value="${leadChiTiet.tenChienDich != null ? leadChiTiet.tenChienDich : '--'}" /></span>
                                </div>
                                <div class="ctl-info-item">
                                    <span class="ctl-info-label">Điểm tiềm năng (Lead Score)</span>
                                    <span class="ctl-info-value"><strong>${leadChiTiet.diem} điểm</strong></span>
                                </div>
                                <div class="ctl-info-item">
                                    <span class="ctl-info-label">Xếp loại</span>
                                    <span class="ctl-info-value"><c:out value="${leadChiTiet.phanLoai != null ? leadChiTiet.phanLoai : '--'}" /></span>
                                </div>
                            </div>
                        </div>
                    </article>
                </div>

                <!-- Cột phải: Timeline Mốc thời gian & SLA (AC1, AC3, Q1, Q2) -->
                <div class="ctl-col-side">
                    <article class="ctl-card" id="cardTienTrinhSla">
                        <div class="ctl-card-header">
                            <h2 class="ctl-card-title">
                                <span class="material-symbols-outlined" style="color: var(--ctl-primary);" aria-hidden="true">timeline</span>
                                <span>Tiến Trình Xử Lý & SLA</span>
                            </h2>
                        </div>
                        <div class="ctl-card-body">
                            <div class="ctl-timeline">
                                <!-- 1. Phân bổ (Q1: Bắt đầu tính SLA từ mốc này) -->
                                <div class="ctl-timeline-item">
                                    <span class="ctl-timeline-dot ${leadChiTiet.duocPhanBoLuc != null ? 'active' : ''}"></span>
                                    <div class="ctl-timeline-label">1. Phân bổ cho nhân viên</div>
                                    <div class="ctl-timeline-value" id="valDuocPhanBoLuc">
                                        <%= formatDateTime(((vn.nhom10.crm.model.Lead) request.getAttribute("leadChiTiet")).getDuocPhanBoLuc()) %>
                                    </div>
                                    <div class="ctl-timeline-note">Bắt đầu tính đồng hồ SLA (Q1)</div>
                                </div>

                                <!-- 2. Hạn phản hồi SLA -->
                                <div class="ctl-timeline-item">
                                    <span class="ctl-timeline-dot ${leadChiTiet.quaSla ? 'danger' : (leadChiTiet.hanPhanHoiSla != null ? 'active' : '')}"></span>
                                    <div class="ctl-timeline-label">2. Hạn chót phản hồi SLA</div>
                                    <div class="ctl-timeline-value" id="valHanPhanHoiSla">
                                        <%= formatDateTime(((vn.nhom10.crm.model.Lead) request.getAttribute("leadChiTiet")).getHanPhanHoiSla()) %>
                                    </div>
                                    <div class="ctl-timeline-note">
                                        <c:choose>
                                            <c:when test="${leadChiTiet.quaSla}">
                                                <span style="color: var(--ctl-danger); font-weight: 700;">Đã quá hạn phản hồi</span>
                                            </c:when>
                                            <c:otherwise>Áp dụng quy tắc SLA nhóm</c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>

                                <!-- 3. Tiếp nhận (AC1) -->
                                <div class="ctl-timeline-item">
                                    <span class="ctl-timeline-dot ${leadChiTiet.duocNhanLuc != null ? 'success' : ''}"></span>
                                    <div class="ctl-timeline-label">3. Nhân viên nhận Lead</div>
                                    <div class="ctl-timeline-value" id="valDuocNhanLuc">
                                        <%= formatDateTime(((vn.nhom10.crm.model.Lead) request.getAttribute("leadChiTiet")).getDuocNhanLuc()) %>
                                    </div>
                                    <div class="ctl-timeline-note">
                                        <c:choose>
                                            <c:when test="${leadChiTiet.duocNhanLuc != null}">
                                                Đã chuyển sang "Đang chăm sóc"
                                            </c:when>
                                            <c:otherwise>Chưa bấm nhận</c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>

                                <!-- 4. Bằng chứng liên hệ (AC3, Q2) -->
                                <div class="ctl-timeline-item">
                                    <span class="ctl-timeline-dot ${leadChiTiet.lienHeDauLuc != null ? 'success' : ''}"></span>
                                    <div class="ctl-timeline-label">4. Hoạt động liên hệ đầu tiên</div>
                                    <div class="ctl-timeline-value" id="valLienHeDauLuc">
                                        <%= formatDateTime(((vn.nhom10.crm.model.Lead) request.getAttribute("leadChiTiet")).getLienHeDauLuc()) %>
                                    </div>
                                    <div class="ctl-timeline-note">Cuộc gọi, email hoặc cuộc gặp (Q2)</div>
                                </div>
                            </div>
                        </div>
                    </article>
                </div>
            </div>

        </div>
    </main>

    <!-- 6. MODAL TỪ CHỐI LEAD (AC2, Q4) -->
    <div class="ctl-modal-backdrop" id="modalTuChoiLead" role="dialog" aria-modal="true" aria-labelledby="modalTuChoiTitle" aria-hidden="true">
        <div class="ctl-modal">
            <div class="ctl-modal-header">
                <h3 class="ctl-modal-title" id="modalTuChoiTitle">
                    <span class="material-symbols-outlined" style="color: var(--ctl-danger);" aria-hidden="true">do_not_disturb_on</span>
                    <span>Từ Chối Tiếp Nhận Lead</span>
                </h3>
                <button type="button" class="ctl-modal-close-btn" id="btnDongModalTuChoi" aria-label="Đóng cửa sổ">
                    <span class="material-symbols-outlined" aria-hidden="true">close</span>
                </button>
            </div>

            <form action="${pageContext.request.contextPath}/lead/xu-ly" method="POST" id="formTuChoiLead">
                <input type="hidden" name="leadId" value="${leadChiTiet.id}">
                <input type="hidden" name="hanhDong" value="TU_CHOI">

                <div class="ctl-modal-body">
                    <div class="ctl-modal-warning-box">
                        <span class="material-symbols-outlined" style="font-size: 20px; flex-shrink: 0;" aria-hidden="true">warning</span>
                        <span>
                            Sau khi từ chối, Lead sẽ lập tức được trả về <strong>hàng chờ phân bổ</strong> để Trưởng nhóm phân công lại (AC2). Thao tác này không thể hoàn tác.
                        </span>
                    </div>

                    <!-- 1. Danh mục lý do từ chối (Select) -->
                    <div class="ctl-form-group">
                        <label for="maLyDoTuChoi" class="ctl-form-label">
                            Lý do từ chối <span class="required">*</span>
                        </label>
                        <select name="maLyDoTuChoi" id="maLyDoTuChoi" class="ctl-form-select ${not empty loiTruong['maLyDoTuChoi'] ? 'is-invalid' : ''}" required>
                            <option value="">-- Chọn lý do từ danh mục * --</option>
                            <c:forEach items="${danhMucLyDoTuChoi}" var="dm">
                                <option value="${dm.ma}" ${maLyDoTuChoiDaChon == dm.ma ? 'selected' : ''}>
                                    <c:out value="${dm.ten}" />
                                </option>
                            </c:forEach>
                        </select>
                        <c:if test="${not empty loiTruong['maLyDoTuChoi']}">
                            <span class="ctl-form-error" id="errMaLyDo">
                                <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">error</span>
                                <c:out value="${loiTruong['maLyDoTuChoi']}" />
                            </span>
                        </c:if>
                    </div>

                    <!-- 2. Giải thích chi tiết lý do (Textarea 1 - 500 ký tự) -->
                    <div class="ctl-form-group">
                        <label for="lyDoTuChoi" class="ctl-form-label">
                            Giải thích chi tiết <span class="required">*</span>
                        </label>
                        <textarea name="lyDoTuChoi" id="lyDoTuChoi" class="ctl-form-textarea ${not empty loiTruong['lyDoTuChoi'] ? 'is-invalid' : ''}" rows="4" maxlength="500" placeholder="Nhập lý do cụ thể bạn không thể tiếp nhận Lead này (tối đa 500 ký tự)..." required><c:out value="${lyDoTuChoiDaNhap}" /></textarea>
                        <div class="ctl-char-counter" id="charCounterWrap">
                            <span id="lyDoCharCount">0</span>/500 ký tự
                        </div>
                        <c:if test="${not empty loiTruong['lyDoTuChoi']}">
                            <span class="ctl-form-error" id="errLyDo">
                                <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">error</span>
                                <c:out value="${loiTruong['lyDoTuChoi']}" />
                            </span>
                        </c:if>
                    </div>
                </div>

                <div class="ctl-modal-footer">
                    <button type="button" class="ctl-btn ctl-btn-outline" id="btnHuyTuChoi">Hủy</button>
                    <button type="submit" class="ctl-btn ctl-btn-danger-solid" id="btnXacNhanTuChoi">
                        <span class="material-symbols-outlined" aria-hidden="true">check</span>
                        <span>Xác nhận từ chối</span>
                    </button>
                </div>
            </form>
        </div>
    </div>

    <!-- Cờ đánh dấu mở modal nếu có lỗi validation từ server trả về -->
    <input type="hidden" id="hasServerValidationError" value="${not empty loiTruong ? 'true' : 'false'}">

    <!-- Script tương tác client -->
    <script src="${pageContext.request.contextPath}/assets/js/lead/chi-tiet-lead.js"></script>
</body>
</html>
