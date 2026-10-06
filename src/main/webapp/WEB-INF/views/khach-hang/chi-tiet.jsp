<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Chi tiết khách hàng & Quan hệ công ty mẹ - con - Hệ thống CRM Bán Hàng">
    <title><c:out value="${not empty banGhi.tieuDe ? banGhi.tieuDe : 'Chi Tiết Khách Hàng'}" /> - CRM Bán Hàng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/khach-hang/khach-hang.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/khach-hang/chi-tiet.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
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
                <li class="breadcrumb-item active" aria-current="page"><c:out value="${not empty banGhi.tieuDe ? banGhi.tieuDe : 'Chi tiết'}" /></li>
            </ol>
        </nav>

        <!-- Thông báo thành công / trạng thái -->
        <c:if test="${not empty thongBaoThanhCong}">
            <div class="alert alert-success" style="margin-bottom: 20px;" id="alertSuccess">
                <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                <span><c:out value="${thongBaoThanhCong}" /></span>
            </div>
        </c:if>
        <c:if test="${not empty thongBaoLoi}">
            <div class="alert alert-danger" style="margin-bottom: 20px;" id="alertError">
                <span class="material-symbols-outlined" aria-hidden="true">error</span>
                <span><c:out value="${thongBaoLoi}" /></span>
            </div>
        </c:if>

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
                </div>

                <div class="detail-item">
                    <span class="detail-label" style="margin-bottom: 6px;">Mô tả / Ghi chú chi tiết</span>
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

        <!-- MODAL 2: KHAI BÁO / ĐỔI CÔNG TY MẸ -->
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

    </main>

    <script src="${pageContext.request.contextPath}/assets/js/khach-hang/chi-tiet.js"></script>
</body>
</html>
