<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nhập danh sách người dùng từ Excel - CRM Bán Hàng</title>
    <meta name="description" content="Nhập danh sách người dùng hàng loạt từ tệp Excel, xem trước lỗi từng dòng và tự động bỏ qua dòng lỗi cho Quản trị hệ thống CRM.">
    <!-- CSS Hệ thống -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/nguoi-dung/nguoi-dung.css">
    <!-- CSS Chuyên biệt cho tính năng Nhập Excel S2-01 -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/nguoi-dung/import-excel.css">
</head>
<body class="crm-body">

    <!-- Thanh điều hướng và Sidebar chuẩn hệ thống S1-06 -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content" id="crm-main-content">
        <div class="import-container">

            <!-- HEADER TRANG -->
            <div class="import-page-header">
                <div class="import-header-title">
                    <h1>&#128203; Nhập danh sách người dùng từ tệp Excel</h1>
                    <p>Tạo tài khoản hàng loạt cho khối kinh doanh, xem trước lỗi từng dòng và tự động bỏ qua dòng lỗi.</p>
                </div>
                <div>
                    <a href="${pageContext.request.contextPath}/nguoi-dung" class="btn-back-link" id="link-ve-danh-sach">
                        &larr; Về danh sách người dùng
                    </a>
                </div>
            </div>

            <!-- STEPPER WIZARD -->
            <div class="import-stepper" aria-label="Tiến trình nhập dữ liệu">
                <div class="step-item ${empty baoCao ? 'active' : 'completed'}">
                    <div class="step-circle">&#49;</div>
                    <div class="step-text">
                        <span class="step-title">Bước 1: Tệp mẫu</span>
                        <span class="step-desc">Chuẩn bị dữ liệu chuẩn</span>
                    </div>
                </div>
                <div class="step-item ${cheDo == 'xem-truoc' ? 'active' : (cheDo == 'ket-qua' ? 'completed' : '')}">
                    <div class="step-circle">&#50;</div>
                    <div class="step-text">
                        <span class="step-title">Bước 2: Xem trước</span>
                        <span class="step-desc">Thẩm định & Báo lỗi</span>
                    </div>
                </div>
                <div class="step-item ${cheDo == 'ket-qua' ? 'active' : ''}">
                    <div class="step-circle">&#51;</div>
                    <div class="step-text">
                        <span class="step-title">Bước 3: Nhập dữ liệu</span>
                        <span class="step-desc">Báo cáo tổng kết</span>
                    </div>
                </div>
            </div>

            <!-- THÔNG BÁO LỖI HỆ THỐNG NẾU CÓ -->
            <c:if test="${not empty thongBaoLoi}">
                <div class="alert alert-error" role="alert" style="margin-bottom: 20px;">
                    <span class="alert-icon">&#9888;</span>
                    <div>
                        <strong>Thông báo lỗi:</strong> <c:out value="${thongBaoLoi}" />
                    </div>
                </div>
            </c:if>

            <!-- CARD 1: HƯỚNG DẪN & TẢI TỆP MẪU (AC 1) -->
            <section class="import-card" aria-labelledby="card-title-step1">
                <div class="card-header-flex">
                    <div>
                        <h2 id="card-title-step1" class="card-title-lg">&#128229; Bước 1: Chuẩn bị dữ liệu theo tệp mẫu chuẩn</h2>
                        <div class="card-subtitle-text">Tải tệp mẫu Excel có định dạng chuẩn kèm các hướng dẫn vai trò và nhóm kinh doanh.</div>
                    </div>
                    <div>
                        <a id="btn-tai-tep-mau"
                           href="${pageContext.request.contextPath}/nguoi-dung/tai-tep-mau"
                           class="btn-download-template"
                           title="Tải tệp mẫu Excel về máy tính">
                            &#11015; Tải tệp mẫu Excel (.xlsx)
                        </a>
                    </div>
                </div>

                <div class="guideline-box">
                    <div class="guideline-title">&#128161; Quy tắc kiểm tra dữ liệu đầu vào:</div>
                    <ul class="guideline-list">
                        <li>Các trường thông tin bắt buộc: <strong>Họ và tên</strong>, <strong>Email công ty</strong> và <strong>Mã vai trò</strong>.</li>
                        <li>Mã vai trò hợp lệ trong hệ thống:
                            <span class="role-tag">ADMIN</span>
                            <span class="role-tag">DIRECTOR</span>
                            <span class="role-tag">TEAM_LEAD</span>
                            <span class="role-tag">SALES_REP</span>
                            <span class="role-tag">MARKETING</span>
                            <span class="role-tag">CUST_SUCCESS</span>
                            <span class="role-tag">ACCOUNTANT</span>.
                        </li>
                        <li><strong>Ràng buộc cơ cấu tổ chức:</strong> Người giữ vai trò Trưởng nhóm (<span class="role-tag">TEAM_LEAD</span>) bắt buộc phải được gán vào một nhóm kinh doanh cụ thể.</li>
                        <li><strong>Mật khẩu:</strong> Nếu cột mật khẩu trong tệp để trống, hệ thống sẽ tự động sinh mật khẩu tạm mạnh ngẫu nhiên và gửi danh sách trong báo cáo tổng kết.</li>
                        <li>Hệ thống tự động phát hiện email trùng lặp ngay trong tệp tải lên hoặc đã tồn tại trong cơ sở dữ liệu.</li>
                    </ul>
                </div>
            </section>

            <!-- CARD 2: TẢI TỆP LÊN & THAO TÁC (AC 2 & AC 3) -->
            <section class="import-card" aria-labelledby="card-title-step2">
                <div class="card-header-flex">
                    <div>
                        <h2 id="card-title-step2" class="card-title-lg">&#128228; Bước 2: Tải tệp Excel lên hệ thống</h2>
                        <div class="card-subtitle-text">Chọn hoặc kéo thả tệp Excel chứa danh sách người dùng để hệ thống kiểm tra và xử lý.</div>
                    </div>
                </div>

                <form id="form-import-excel"
                      action="${pageContext.request.contextPath}/nguoi-dung/import"
                      method="post"
                      enctype="multipart/form-data">

                    <!-- VÙNG KÉO THẢ TỆP (DROPZONE) -->
                    <div class="dropzone-container" id="dropzone-container">
                        <div class="dropzone-icon">&#128196;</div>
                        <div class="dropzone-text-main">Kéo và thả tệp Excel vào đây, hoặc bấm để duyệt tệp</div>
                        <div class="dropzone-text-sub">Chấp nhận tệp định dạng <strong>.xlsx</strong> hoặc <strong>.xls</strong> (Dung lượng tối đa 10MB)</div>
                        <input type="file" id="fileExcel" name="fileExcel" accept=".xlsx, .xls" class="dropzone-input-hidden" required />
                    </div>

                    <!-- THẺ HIỂN THỊ THÔNG TIN TỆP ĐÃ CHỌN -->
                    <div class="selected-file-card" id="selected-file-card">
                        <div class="selected-file-info">
                            <span class="file-excel-badge">EXCEL</span>
                            <div class="file-details-text">
                                <span class="file-name-display" id="file-name-display">tep_nguoi_dung.xlsx</span>
                                <span class="file-size-display" id="file-size-display">0 KB</span>
                            </div>
                        </div>
                        <button type="button" class="btn-remove-selected-file" id="btn-remove-selected-file" title="Xóa tệp đã chọn">
                            &#10005; Chọn tệp khác
                        </button>
                    </div>

                    <!-- NHÓM NÚT THAO TÁC -->
                    <div class="action-buttons-wrap" style="margin-top: 20px;">
                        <button type="submit"
                                name="action"
                                value="xem-truoc"
                                id="btn-xem-truoc"
                                class="btn-action btn-action-preview">
                            &#128065; Xem trước và kiểm tra lỗi
                        </button>

                        <button type="button"
                                id="btn-nhap-du-lieu"
                                class="btn-action btn-action-import">
                            &#9989; Tiến hành nhập dữ liệu
                        </button>
                    </div>
                </form>
            </section>

            <!-- CARD 3: BÁO CÁO XEM TRƯỚC / KẾT QUẢ TỔNG KẾT (AC 2 & AC 3) -->
            <c:if test="${not empty baoCao}">
                <section class="import-card" id="khu-vuc-bao-cao" aria-labelledby="card-title-step3">
                    <div class="card-header-flex">
                        <div>
                            <h2 id="card-title-step3" class="card-title-lg">
                                <c:choose>
                                    <c:when test="${cheDo == 'ket-qua'}">
                                        &#128202; Báo cáo tổng kết đợt nhập người dùng: <c:out value="${tenTep}" />
                                    </c:when>
                                    <c:otherwise>
                                        &#128065; Kết quả xem trước và kiểm tra lỗi: <c:out value="${tenTep}" />
                                    </c:otherwise>
                                </c:choose>
                            </h2>
                            <div class="card-subtitle-text">
                                <c:choose>
                                    <c:when test="${cheDo == 'ket-qua'}">
                                        Hệ thống đã tự động bỏ qua các dòng lỗi và hoàn tất nhập các tài khoản hợp lệ.
                                    </c:when>
                                    <c:otherwise>
                                        Kiểm tra trạng thái từng dòng dưới đây trước khi quyết định bấm "Tiến hành nhập dữ liệu".
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <!-- CÁC NÚT THAO TÁC TRÊN BÁO CÁO -->
                        <div style="display: flex; gap: 8px;">
                            <c:if test="${cheDo == 'ket-qua' and baoCao.soDongThanhCong > 0}">
                                <button type="button"
                                        class="btn-action btn-action-outline"
                                        id="btn-copy-all-credentials"
                                        title="Sao chép toàn bộ danh sách tài khoản vừa tạo">
                                    &#128203; Sao chép tài khoản & Mật khẩu
                                </button>
                            </c:if>
                            <button type="button"
                                    class="btn-action btn-action-outline"
                                    id="btn-print-report"
                                    title="In hoặc lưu báo cáo dưới dạng PDF">
                                &#128424; In báo cáo
                            </button>
                        </div>
                    </div>

                    <!-- THỐNG KÊ TỔNG KẾT ĐỢT NHẬP -->
                    <div class="stats-cards-grid">
                        <div class="stat-item-box total">
                            <div class="stat-header-sub">
                                <span class="stat-item-label">Tổng số dòng</span>
                                <span class="stat-item-icon">&#128196;</span>
                            </div>
                            <div class="stat-item-value" id="stat-num-total"><c:out value="${baoCao.tongSoDong}" /></div>
                            <div class="stat-item-subtext">Bản ghi trong tệp Excel</div>
                        </div>

                        <div class="stat-item-box valid">
                            <div class="stat-header-sub">
                                <span class="stat-item-label">Dòng hợp lệ</span>
                                <span class="stat-item-icon">&#9989;</span>
                            </div>
                            <div class="stat-item-value" id="stat-num-valid"><c:out value="${baoCao.soDongHopLe}" /></div>
                            <div class="stat-item-subtext">Đạt chuẩn nghiệp vụ</div>
                        </div>

                        <div class="stat-item-box error">
                            <div class="stat-header-sub">
                                <span class="stat-item-label">Dòng có lỗi</span>
                                <span class="stat-item-icon">&#9888;</span>
                            </div>
                            <div class="stat-item-value" id="stat-num-error"><c:out value="${baoCao.soDongLoi}" /></div>
                            <div class="stat-item-subtext">Sẽ tự động bỏ qua</div>
                        </div>

                        <c:if test="${cheDo == 'ket-qua'}">
                            <div class="stat-item-box imported">
                                <div class="stat-header-sub">
                                    <span class="stat-item-label">Đã tạo thành công</span>
                                    <span class="stat-item-icon">&#127881;</span>
                                </div>
                                <div class="stat-item-value" id="stat-num-imported"><c:out value="${baoCao.soDongThanhCong}" /></div>
                                <div class="stat-item-subtext">Tài khoản sẵn sàng dùng</div>
                            </div>
                        </c:if>
                    </div>

                    <!-- THANH TIẾN TRÌNH TỶ LỆ HỢP LỆ / LỖI -->
                    <c:if test="${baoCao.tongSoDong > 0}">
                        <div class="progress-stacked-wrap">
                            <div class="progress-header-info">
                                <span>Tỷ lệ hợp lệ: <strong>${Math.round(baoCao.soDongHopLe * 100.0 / baoCao.tongSoDong)}%</strong></span>
                                <span>Tỷ lệ lỗi: <strong>${Math.round(baoCao.soDongLoi * 100.0 / baoCao.tongSoDong)}%</strong></span>
                            </div>
                            <div class="progress-bar-track">
                                <div class="progress-bar-fill-valid" style="width: ${baoCao.soDongHopLe * 100.0 / baoCao.tongSoDong}%;"></div>
                                <div class="progress-bar-fill-error" style="width: ${baoCao.soDongLoi * 100.0 / baoCao.tongSoDong}%;"></div>
                            </div>
                        </div>
                    </c:if>

                    <!-- THÔNG ĐIỆP TỔNG KẾT HỆ THỐNG -->
                    <div class="alert alert-info" style="margin-bottom: 20px; background-color: #EBF8FF; color: #2B6CB0; border: 1px solid #BEE3F8; border-radius: 8px; padding: 14px 18px;">
                        <strong>&#8505; Tóm tắt đợt xử lý:</strong> <c:out value="${baoCao.thongDiep}" />
                    </div>

                    <!-- TOOLBAR: BỘ LỌC THEO TAB VÀ TÌM KIẾM NHANH -->
                    <div class="table-toolbar-box">
                        <div class="tab-filter-container">
                            <button type="button" class="tab-filter-btn active" data-filter="all" id="tab-tat-ca">
                                Tất cả <span class="tab-count-badge"><c:out value="${baoCao.tongSoDong}" /></span>
                            </button>
                            <button type="button" class="tab-filter-btn" data-filter="valid" id="tab-hop-le">
                                <c:choose>
                                    <c:when test="${cheDo == 'ket-qua'}">
                                        Đã nhập thành công <span class="tab-count-badge"><c:out value="${baoCao.soDongThanhCong}" /></span>
                                    </c:when>
                                    <c:otherwise>
                                        Hợp lệ <span class="tab-count-badge"><c:out value="${baoCao.soDongHopLe}" /></span>
                                    </c:otherwise>
                                </c:choose>
                            </button>
                            <button type="button" class="tab-filter-btn" data-filter="error" id="tab-loi">
                                <c:choose>
                                    <c:when test="${cheDo == 'ket-qua'}">
                                        Bị bỏ qua do lỗi <span class="tab-count-badge"><c:out value="${baoCao.soDongLoi}" /></span>
                                    </c:when>
                                    <c:otherwise>
                                        Có lỗi <span class="tab-count-badge"><c:out value="${baoCao.soDongLoi}" /></span>
                                    </c:otherwise>
                                </c:choose>
                            </button>
                        </div>

                        <!-- Ô TÌM KIẾM TRONG BẢNG -->
                        <div class="table-search-wrapper">
                            <span class="table-search-icon">&#128269;</span>
                            <input type="text"
                                   id="timKiemBang"
                                   class="table-search-input"
                                   placeholder="Lọc theo tên, email, vai trò hoặc lỗi..."
                                   aria-label="Tìm kiếm trong bảng xem trước" />
                        </div>
                    </div>

                    <!-- BẢNG CHI TIẾT TỪNG DÒNG (AC 2 & AC 3) -->
                    <div class="preview-table-container">
                        <table class="import-preview-table" id="bang-ket-qua-excel">
                            <thead>
                                <tr>
                                    <th style="width: 60px; text-align: center;">Dòng</th>
                                    <th>Họ và tên</th>
                                    <th>Email</th>
                                    <th>Số điện thoại</th>
                                    <th>Vai trò</th>
                                    <th>Nhóm kinh doanh</th>
                                    <th style="width: 150px; text-align: center;">Trạng thái</th>
                                    <th>Chi tiết thẩm định / Mật khẩu tạm</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="dong" items="${baoCao.danhSachTatCaDong}">
                                    <c:set var="statusKey" value="${cheDo == 'ket-qua' ? (dong.daNhap ? 'imported' : 'skipped') : (dong.hopLe ? 'valid' : 'error')}" />
                                    <tr data-status="${statusKey}" class="${not dong.hopLe ? 'row-error-highlight' : ''}">
                                        <td class="row-index-cell"><c:out value="${dong.soDong}" /></td>
                                        <td class="user-name-cell"><c:out value="${dong.hoTen}" /></td>
                                        <td class="user-email-cell"><code><c:out value="${dong.email}" /></code></td>
                                        <td><c:out value="${empty dong.soDienThoai ? '-' : dong.soDienThoai}" /></td>
                                        <td>
                                            <span class="role-tag"><c:out value="${dong.chuoiVaiTroHienThi}" /></span>
                                        </td>
                                        <td><c:out value="${empty dong.tenNhomGiaiQuyet ? '-' : dong.tenNhomGiaiQuyet}" /></td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${cheDo == 'ket-qua'}">
                                                    <c:choose>
                                                        <c:when test="${dong.daNhap}">
                                                            <span class="badge-state badge-state-imported" title="Đã lưu vào cơ sở dữ liệu với ID: ${dong.idNguoiDung}">
                                                                &#10003; Đã tạo (ID: ${dong.idNguoiDung})
                                                            </span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge-state badge-state-skipped" title="Dòng bị lỗi đã được tự động bỏ qua">
                                                                &#10007; Bị bỏ qua
                                                            </span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:choose>
                                                        <c:when test="${dong.hopLe}">
                                                            <span class="badge-state badge-state-valid" title="Dữ liệu đáp ứng tất cả quy tắc">
                                                                &#10003; Hợp lệ
                                                            </span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge-state badge-state-error" title="Dữ liệu vi phạm quy tắc">
                                                                &#9888; Dòng lỗi
                                                            </span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty dong.danhSachLoi}">
                                                    <ul class="error-list-pill">
                                                        <c:forEach var="loi" items="${dong.danhSachLoi}">
                                                            <li class="error-item-tag">
                                                                <span>&#9888;</span>
                                                                <span><c:out value="${loi}" /></span>
                                                            </li>
                                                        </c:forEach>
                                                    </ul>
                                                </c:when>
                                                <c:when test="${dong.daNhap and not empty dong.matKhauTam}">
                                                    <div class="credential-display-wrap">
                                                        <span style="font-size: 0.8rem; color: #475569;">Mật khẩu:</span>
                                                        <span class="credential-code"><c:out value="${dong.matKhauTam}" /></span>
                                                        <button type="button"
                                                                class="btn-copy-mini btn-copy-pwd"
                                                                data-pwd="<c:out value='${dong.matKhauTam}' />"
                                                                title="Sao chép mật khẩu tạm">
                                                            &#128203;
                                                        </button>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="success-ready-tag">
                                                        &#10003; Dữ liệu sẵn sàng nhập
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>

                        <!-- THÔNG BÁO KHI TÌM KIẾM HOẶC LỌC KHÔNG CÓ KẾT QUẢ -->
                        <div id="empty-filter-notice" class="empty-filter-state" style="display: none;">
                            <div class="empty-filter-icon">&#128269;</div>
                            <div style="font-weight: 600; font-size: 0.95rem;">Không tìm thấy dòng dữ liệu nào phù hợp</div>
                            <div style="font-size: 0.85rem;">Vui lòng thử tìm với từ khóa khác hoặc chuyển tab lọc.</div>
                        </div>
                    </div>

                </section>
            </c:if>

        </div>
    </main>

    <!-- MODAL XÁC NHẬN NHẬP DỮ LIỆU THÔNG MINH -->
    <div class="crm-modal-backdrop" id="modal-xac-nhan-nhap" role="dialog" aria-modal="true" aria-labelledby="modal-confirm-title">
        <div class="crm-modal-box">
            <div class="modal-header-flex">
                <div class="modal-icon-alert">&#9888;</div>
                <div>
                    <h3 class="modal-title-text" id="modal-confirm-title">Xác nhận nhập danh sách người dùng</h3>
                    <div style="font-size: 0.84rem; color: #64748B;">Hệ thống sẽ thực hiện theo quy tắc Acceptance Criteria</div>
                </div>
            </div>

            <div class="modal-body-desc">
                Bạn đang chuẩn bị tạo tài khoản hàng loạt từ tệp Excel vào cơ sở dữ liệu CRM.
            </div>

            <div class="modal-summary-box">
                <div class="modal-summary-row">
                    <span style="color: #2E7D32; font-weight: 600;">&#10003; Số dòng hợp lệ sẽ được nhập:</span>
                    <strong id="modal-count-valid" style="color: #2E7D32;">${empty baoCao ? '-' : baoCao.soDongHopLe}</strong>
                </div>
                <div class="modal-summary-row">
                    <span style="color: #C62828; font-weight: 600;">&#10007; Số dòng lỗi sẽ tự động bỏ qua:</span>
                    <strong id="modal-count-error" style="color: #C62828;">${empty baoCao ? '-' : baoCao.soDongLoi}</strong>
                </div>
            </div>

            <div class="modal-actions-flex">
                <button type="button" class="btn-action btn-action-outline" id="modal-btn-cancel">
                    Hủy bỏ
                </button>
                <button type="button" class="btn-action btn-action-import" id="modal-btn-confirm">
                    &#9989; Đồng ý và Tiếp tục nhập
                </button>
            </div>
        </div>
    </div>

    <!-- JavaScript chuyên biệt cho tính năng S2-01 -->
    <script src="${pageContext.request.contextPath}/assets/js/nguoi-dung/import-excel.js"></script>
</body>
</html>
