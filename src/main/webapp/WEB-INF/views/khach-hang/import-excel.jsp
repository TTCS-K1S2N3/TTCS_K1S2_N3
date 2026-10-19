<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nhập danh sách khách hàng từ Excel - CRM Bán Hàng</title>
    <meta name="description" content="Nhập danh sách khách hàng hàng loạt từ tệp Excel, xem trước lỗi từng dòng và chủ động xử lý bản ghi trùng lặp cho Nhân viên kinh doanh CRM.">
    <!-- CSS Hệ thống -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/khach-hang/khach-hang.css">
    <!-- CSS Chuyên biệt cho tính năng Nhập Excel S3-06 -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/khach-hang/import-excel.css">
</head>
<body class="crm-body">

    <!-- Thanh điều hướng và Sidebar chuẩn hệ thống S1-06 -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <div class="import-container">

            <!-- Breadcrumb điều hướng chuẩn hệ thống -->
            <nav class="crm-breadcrumb" aria-label="Đường dẫn điều hướng" style="display:flex; justify-content:space-between; align-items:center; margin-bottom:18px;">
                <div style="font-size: 13.5px; color: var(--slate-500, #64748b);">
                    <a href="${pageContext.request.contextPath}/dieu-huong" style="color: var(--primary, #2563eb); text-decoration: none;">Trang chủ</a>
                    <span style="margin: 0 6px;">/</span>
                    <a href="${pageContext.request.contextPath}/khach-hang" style="color: var(--primary, #2563eb); text-decoration: none;">Khách hàng & Liên hệ</a>
                    <span style="margin: 0 6px;">/</span>
                    <span style="color: var(--slate-700, #334155); font-weight: 600;">Nhập danh sách Excel</span>
                </div>
                <a href="${pageContext.request.contextPath}/khach-hang" class="btn-outline" id="btn-back-danh-sach" style="min-height: 38px; padding: 6px 14px; font-size: 13px;">
                    <span class="material-symbols-outlined icon-sm" aria-hidden="true">arrow_back</span>
                    <span>Quay lại danh sách khách hàng</span>
                </a>
            </nav>

            <!-- TIÊU ĐỀ TRANG -->
            <div class="import-page-header">
                <div class="import-header-title">
                    <h1>Nhập danh sách khách hàng từ tệp Excel</h1>
                    <p>Đưa danh mục khách hàng doanh nghiệp vào hệ thống nhanh chóng, thẩm định lỗi theo từng dòng và đối chiếu trùng lặp.</p>
                </div>
            </div>

            <!-- TIẾN TRÌNH STEPPER WIZARD -->
            <div class="import-stepper" aria-label="Tiến trình nhập dữ liệu">
                <div class="step-item ${empty baoCao ? 'active' : 'completed'}">
                    <div class="step-circle">1</div>
                    <div class="step-text">
                        <span class="step-title">Bước 1: Tệp mẫu</span>
                        <span class="step-desc">Chuẩn bị dữ liệu theo chuẩn</span>
                    </div>
                </div>
                <div class="step-item ${cheDo == 'xem-truoc' ? 'active' : (cheDo == 'ket-qua' ? 'completed' : '')}">
                    <div class="step-circle">2</div>
                    <div class="step-text">
                        <span class="step-title">Bước 2: Xem trước</span>
                        <span class="step-desc">Thẩm định & Đối chiếu trùng</span>
                    </div>
                </div>
                <div class="step-item ${cheDo == 'ket-qua' ? 'active' : ''}">
                    <div class="step-circle">3</div>
                    <div class="step-text">
                        <span class="step-title">Bước 3: Nhập dữ liệu</span>
                        <span class="step-desc">Báo cáo kết quả tổng kết</span>
                    </div>
                </div>
            </div>

            <!-- THÔNG BÁO LỖI NẾU CÓ -->
            <c:if test="${not empty thongBaoLoi}">
                <div class="alert alert-danger" role="alert">
                    <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 20px;">error</span>
                    <div>
                        <strong>Thông báo:</strong> <c:out value="${thongBaoLoi}" />
                    </div>
                </div>
            </c:if>

            <!-- CARD 1: HƯỚNG DẪN & TẢI TỆP MẪU EXCEL (AC 1) -->
            <section class="import-card" aria-labelledby="heading-step1">
                <div class="card-header-flex">
                    <div>
                        <h2 id="heading-step1" class="card-title-lg">Bước 1: Tải tệp mẫu Excel chuẩn</h2>
                        <div class="card-subtitle-text">Tải tệp mẫu định dạng .xlsx có sẵn cấu trúc cột và dữ liệu minh họa chuẩn cho khách hàng.</div>
                    </div>
                    <div>
                        <a id="btn-tai-tep-mau"
                           href="${pageContext.request.contextPath}/khach-hang/tai-tep-mau"
                           class="btn-download-template"
                           title="Tải tệp mẫu Excel về máy tính">
                            <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 18px;">download</span>
                            Tải tệp mẫu Excel (.xlsx)
                        </a>
                    </div>
                </div>

                <!-- Quy tắc dữ liệu & xử lý trùng lặp -->
                <div class="template-rules-grid">
                    <div class="rule-box">
                        <div class="rule-box-title">
                            <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 18px; color: var(--primary);">description</span>
                            Trường bắt buộc
                        </div>
                        <div class="rule-box-desc">
                            Cột <strong>Tên công ty / Khách hàng (*)</strong> là bắt buộc. Dòng thiếu tên sẽ bị đánh dấu lỗi và tự động bỏ qua.
                        </div>
                    </div>

                    <div class="rule-box">
                        <div class="rule-box-title">
                            <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 18px; color: var(--warning);">sync_problem</span>
                            Kiểm tra trùng lặp (AC 2)
                        </div>
                        <div class="rule-box-desc">
                            Hệ thống tự động phát hiện trùng theo <strong>Mã số thuế</strong>, <strong>Mã khách hàng</strong> hoặc <strong>Tên công ty</strong> với dữ liệu đang có trong CRM.
                        </div>
                    </div>

                    <div class="rule-box">
                        <div class="rule-box-title">
                            <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 18px; color: var(--success);">rule</span>
                            Lựa chọn xử lý trùng
                        </div>
                        <div class="rule-box-desc">
                            Với bản ghi trùng, bạn có thể chủ động chọn <strong>[Bỏ qua]</strong> để giữ nguyên CRM hoặc chọn <strong>[Cập nhật]</strong> để ghi đè thông tin mới.
                        </div>
                    </div>
                </div>
            </section>

            <!-- CARD 2: TẢI TỆP LÊN & XEM TRƯỚC (AC 1) -->
            <section class="import-card" aria-labelledby="heading-step2">
                <div class="card-header-flex">
                    <div>
                        <h2 id="heading-step2" class="card-title-lg">Bước 2: Tải lên tệp Excel chứa danh sách khách hàng</h2>
                        <div class="card-subtitle-text">Chọn tệp bảng tính Excel (.xlsx hoặc .xls) từ máy tính của bạn để hệ thống đọc và thẩm định.</div>
                    </div>
                </div>

                <form id="formUploadExcel"
                      action="${pageContext.request.contextPath}/khach-hang/import"
                      method="POST"
                      enctype="multipart/form-data">
                    <input type="hidden" name="action" value="xem-truoc">

                    <div class="dropzone-container" id="dropzoneExcel" role="button" tabindex="0" aria-label="Khu vực kéo thả tệp Excel">
                        <div class="dropzone-icon">
                            <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 44px; color: var(--primary);">cloud_upload</span>
                        </div>
                        <div class="dropzone-title">Kéo & thả tệp Excel vào đây, hoặc bấm để duyệt tệp</div>
                        <div class="dropzone-desc">Hỗ trợ tệp định dạng .xlsx, .xls • Dung lượng tối đa 10 MB</div>
                        <input type="file" id="fileExcel" name="fileExcel" class="file-input-hidden" accept=".xlsx, .xls" aria-label="Tệp Excel danh sách khách hàng">

                        <!-- Hiển thị tên file sau khi chọn -->
                        <div class="selected-file-info" id="selectedFileInfo" style="${not empty tenTep ? 'display:flex;' : 'display:none;'}">
                            <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 20px; color: var(--primary); flex-shrink: 0;">table_chart</span>
                            <span class="selected-file-name" id="selectedFileName"><c:out value="${not empty tenTep ? tenTep : 'Chưa có tệp nào được chọn'}" /></span>
                            <span class="selected-file-size" id="selectedFileSize"></span>
                            <button type="button" class="btn-clear-file" id="btnClearFile" aria-label="Hủy chọn tệp này" title="Hủy chọn tệp này">
                                <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 18px;">close</span>
                            </button>
                        </div>
                    </div>

                    <div class="import-action-bar">
                        <button type="submit" class="btn-primary" id="btnSubmitPreview">
                            <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 18px;">preview</span>
                            Xem trước dữ liệu
                        </button>
                    </div>
                </form>
            </section>

            <!-- CARD 3: BẢNG XEM TRƯỚC, BÁO LỖI DÒNG VÀ ĐÁNH DẤU TRÙNG LẶP (AC 1 & AC 2) -->
            <c:if test="${cheDo == 'xem-truoc' && not empty baoCao}">
                <section class="import-card" aria-labelledby="heading-preview">
                    <div class="card-header-flex">
                        <div>
                            <h2 id="heading-preview" class="card-title-lg">Bản xem trước dữ liệu & Thẩm định đối chiếu (AC 1 & AC 2)</h2>
                            <div class="card-subtitle-text">Kiểm tra kết quả thẩm định theo từng dòng. Bản ghi trùng đã được đánh dấu rõ để bạn chọn bỏ qua hoặc cập nhật.</div>
                        </div>
                    </div>

                    <!-- THẺ THỐNG KÊ TỔNG HỢP -->
                    <div class="stats-summary-grid">
                        <div class="stat-card stat-card-total">
                            <div class="stat-info">
                                <span class="stat-number"><c:out value="${baoCao.tongSoDong}" /></span>
                                <span class="stat-label">Tổng số dòng đọc được</span>
                            </div>
                        </div>
                        <div class="stat-card stat-card-valid">
                            <div class="stat-info">
                                <span class="stat-number" style="color:var(--success);"><c:out value="${baoCao.soDongHopLe}" /></span>
                                <span class="stat-label">Dòng hợp lệ (Sẵn sàng)</span>
                            </div>
                        </div>
                        <div class="stat-card stat-card-duplicate">
                            <div class="stat-info">
                                <span class="stat-number" style="color:var(--warning);"><c:out value="${baoCao.soDongBiTrung}" /></span>
                                <span class="stat-label">Bản ghi trùng (Cần chọn xử lý)</span>
                            </div>
                        </div>
                        <div class="stat-card stat-card-error">
                            <div class="stat-info">
                                <span class="stat-number" style="color:var(--danger);"><c:out value="${baoCao.soDongLoi}" /></span>
                                <span class="stat-label">Dòng có lỗi (Sẽ bỏ qua)</span>
                            </div>
                        </div>
                    </div>

                    <!-- THANH CÔNG CỤ BỘ LỌC VÀ XỬ LÝ TRÙNG LẶP HÀNG LOẠT -->
                    <div class="preview-toolbar">
                        <div class="filter-tab-group" role="tablist" aria-label="Bộ lọc dòng xem trước">
                            <button type="button" class="filter-tab-btn active" data-filter="all">Tất cả (${baoCao.tongSoDong})</button>
                            <button type="button" class="filter-tab-btn" data-filter="valid">Hợp lệ (${baoCao.soDongHopLe})</button>
                            <button type="button" class="filter-tab-btn" data-filter="duplicate">Bị trùng (${baoCao.soDongBiTrung})</button>
                            <button type="button" class="filter-tab-btn" data-filter="error">Có lỗi (${baoCao.soDongLoi})</button>
                        </div>

                        <!-- Lựa chọn xử lý trùng lặp đồng loạt (AC 2) -->
                        <div class="batch-action-group">
                            <label for="selectBatchDuplicateAction" class="batch-action-label">Xử lý toàn bộ bản ghi trùng:</label>
                            <select id="selectBatchDuplicateAction" class="batch-select">
                                <option value="BO_QUA">Bỏ qua tất cả bản ghi trùng (Mặc định)</option>
                                <option value="CAP_NHAT">Cập nhật tất cả bản ghi trùng vào CRM</option>
                            </select>
                        </div>
                    </div>

                    <!-- FORM XÁC NHẬN NHẬP DỮ LIỆU -->
                    <form id="formConfirmImport"
                          action="${pageContext.request.contextPath}/khach-hang/import"
                          method="POST">
                        <input type="hidden" name="action" value="nhap-du-lieu">
                        <input type="hidden" name="xuLyTrungLapChung" id="inputXuLyTrungLapChung" value="BO_QUA">

                        <!-- BẢNG XEM TRƯỚC CHI TIẾT -->
                        <div class="preview-table-container">
                            <table class="preview-table">
                                <thead>
                                    <tr>
                                        <th style="width: 60px; text-align: center;">Dòng</th>
                                        <th style="width: 90px;">Mã KH</th>
                                        <th>Tên Công Ty / Khách Hàng</th>
                                        <th style="width: 110px;">Mã Số Thuế</th>
                                        <th>Ngành / Quy Mô</th>
                                        <th style="text-align: right; width: 120px;">Doanh Thu (VNĐ)</th>
                                        <th style="width: 140px; text-align: center;">Trạng Thái Dòng</th>
                                        <th style="width: 220px;">Chi Tiết Lỗi / Cảnh Báo</th>
                                        <th style="width: 150px; text-align: center;">Xử Lý Trùng (AC 2)</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="dong" items="${baoCao.danhSachTatCaDong}">
                                        <tr class="${!dong.hopLe ? 'row-error' : (dong.biTrung ? 'row-duplicate' : '')}">
                                            <td style="text-align: center; font-weight: 600; color: var(--slate-500);">
                                                <c:out value="${dong.soDong}" />
                                            </td>
                                            <td class="font-mono">
                                                <c:out value="${not empty dong.maKhachHang ? dong.maKhachHang : '—'}" />
                                            </td>
                                            <td>
                                                <strong style="color: var(--slate-900);"><c:out value="${dong.tenCongTy}" /></strong>
                                                <c:if test="${not empty dong.diaChi}">
                                                    <div style="font-size: 11.5px; color: var(--slate-500);"><c:out value="${dong.diaChi}" /></div>
                                                </c:if>
                                            </td>
                                            <td class="font-mono">
                                                <c:out value="${not empty dong.maSoThue ? dong.maSoThue : '—'}" />
                                            </td>
                                            <td>
                                                <div style="font-size: 12px; color: var(--slate-700);"><c:out value="${not empty dong.nganhNghe ? dong.nganhNghe : '—'}" /></div>
                                                <div style="font-size: 11.5px; color: var(--slate-500);"><c:out value="${not empty dong.quyMo ? dong.quyMo : ''}" /></div>
                                            </td>
                                            <td style="text-align: right; font-weight: 600; color: var(--slate-800);">
                                                <c:out value="${not empty dong.doanhThuUocTinh ? dong.doanhThuUocTinh : '0'}" />
                                            </td>
                                            <td style="text-align: center;">
                                                <c:choose>
                                                    <c:when test="${!dong.hopLe}">
                                                        <span class="badge badge-error">Lỗi dữ liệu</span>
                                                    </c:when>
                                                    <c:when test="${dong.biTrung}">
                                                        <span class="badge badge-duplicate">Trùng lặp</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge badge-valid">Hợp lệ (Mới)</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:if test="${!dong.hopLe}">
                                                    <ul class="row-error-list">
                                                        <c:forEach var="loi" items="${dong.danhSachLoi}">
                                                            <li><c:out value="${loi}" /></li>
                                                        </c:forEach>
                                                    </ul>
                                                </c:if>
                                                <c:if test="${dong.hopLe && dong.biTrung}">
                                                    <span style="font-size: 12px; color: var(--warning-text); font-weight: 500;">
                                                        <c:out value="${dong.moTaTrung}" />
                                                    </span>
                                                </c:if>
                                                <c:if test="${dong.hopLe && !dong.biTrung}">
                                                    <span style="font-size: 12px; color: var(--success-text);">Sẵn sàng thêm mới</span>
                                                </c:if>
                                            </td>
                                            <td style="text-align: center;">
                                                <c:choose>
                                                    <c:when test="${dong.biTrung}">
                                                        <!-- AC 2: Chọn bỏ qua hoặc cập nhật cho từng dòng trùng -->
                                                        <select name="xuLyDong_${dong.soDong}" class="row-action-select" data-so-dong="${dong.soDong}" aria-label="Lựa chọn xử lý trùng dòng ${dong.soDong}">
                                                            <option value="BO_QUA" selected>Bỏ qua</option>
                                                            <option value="CAP_NHAT">Cập nhật</option>
                                                        </select>
                                                    </c:when>
                                                    <c:when test="${!dong.hopLe}">
                                                        <span style="font-size: 12px; color: var(--slate-400);">Tự bỏ qua</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span style="font-size: 12px; color: var(--success-text); font-weight: 600;">Thêm mới</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    <tr id="previewEmptyFilterRow" style="display: none;">
                                        <td colspan="9" style="text-align: center; padding: 32px 16px; color: var(--slate-500);">
                                            <span class="material-symbols-outlined icon-lg" aria-hidden="true" style="color: var(--slate-400); margin-bottom: 6px; display: block;">filter_alt_off</span>
                                            Không có dòng nào phù hợp với bộ lọc này.
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>

                        <!-- Cần truyền lại file qua input file ẩn nếu người dùng submit form nhập -->
                        <div class="import-action-bar">
                            <a href="${pageContext.request.contextPath}/khach-hang/import" class="btn-outline">
                                <span class="material-symbols-outlined icon-sm" aria-hidden="true">replay</span>
                                <span>Hủy & Chọn lại tệp</span>
                            </a>
                            <button type="submit" class="btn-primary" id="btnConfirmImport" ${baoCao.soDongHopLe == 0 ? 'disabled' : ''}>
                                <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 18px;">check_circle</span>
                                Xác nhận nhập dữ liệu (<c:out value="${baoCao.soDongHopLe}" /> dòng hợp lệ)
                            </button>
                        </div>
                    </form>
                </section>
            </c:if>

            <!-- CARD 4: KẾT QUẢ TỔNG KẾT SAU KHI NHẬP DỮ LIỆU -->
            <c:if test="${cheDo == 'ket-qua' && not empty baoCao}">
                <section class="import-card" aria-labelledby="heading-result">
                    <div class="card-header-flex">
                        <div>
                            <h2 id="heading-result" class="card-title-lg">Bước 3: Kết quả nhập danh sách khách hàng</h2>
                            <div class="card-subtitle-text"><c:out value="${baoCao.thongDiep}" /></div>
                        </div>
                    </div>

                    <div class="alert alert-success" style="margin-bottom: 20px;">
                        <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 24px; color: var(--success); flex-shrink: 0;">check_circle</span>
                        <div>
                            <strong>Hoàn tất:</strong> Đã xử lý xong toàn bộ tệp <strong><c:out value="${baoCao.tenTep}" /></strong>.
                            Dữ liệu khách hàng đã được lưu trữ an toàn trong CRM.
                        </div>
                    </div>

                    <div class="stats-summary-grid">
                        <div class="stat-card stat-card-valid">
                            <div class="stat-info">
                                <span class="stat-number" style="color:var(--success);"><c:out value="${baoCao.soDongThanhCong}" /></span>
                                <span class="stat-label">Khách hàng thêm mới thành công</span>
                            </div>
                        </div>
                        <div class="stat-card stat-card-duplicate">
                            <div class="stat-info">
                                <span class="stat-number" style="color:var(--primary);"><c:out value="${baoCao.soDongCapNhat}" /></span>
                                <span class="stat-label">Bản ghi trùng đã cập nhật</span>
                            </div>
                        </div>
                        <div class="stat-card" style="border-left: 4px solid var(--slate-400);">
                            <div class="stat-info">
                                <span class="stat-number" style="color:var(--slate-600);"><c:out value="${baoCao.soDongBoQua}" /></span>
                                <span class="stat-label">Bản ghi trùng đã bỏ qua</span>
                            </div>
                        </div>
                        <div class="stat-card stat-card-error">
                            <div class="stat-info">
                                <span class="stat-number" style="color:var(--danger);"><c:out value="${baoCao.soDongThatBai}" /></span>
                                <span class="stat-label">Dòng lỗi không thể nhập</span>
                            </div>
                        </div>
                    </div>

                    <!-- Bảng kết quả từng dòng -->
                    <div class="preview-table-container">
                        <table class="preview-table">
                            <thead>
                                <tr>
                                    <th style="width: 60px; text-align: center;">Dòng</th>
                                    <th>Tên Khách Hàng / Công Ty</th>
                                    <th style="width: 120px;">Mã Số Thuế</th>
                                    <th style="width: 160px; text-align: center;">Kết Quả Xử Lý</th>
                                    <th>Ghi Chú Chi Tiết</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="dong" items="${baoCao.danhSachTatCaDong}">
                                    <tr>
                                        <td style="text-align: center; font-weight: 600;"><c:out value="${dong.soDong}" /></td>
                                        <td><strong><c:out value="${dong.tenCongTy}" /></strong></td>
                                        <td class="font-mono"><c:out value="${not empty dong.maSoThue ? dong.maSoThue : '—'}" /></td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${dong.thanhCong && dong.luaChonXuLy == 'CAP_NHAT'}">
                                                    <span class="badge badge-duplicate">Đã cập nhật</span>
                                                </c:when>
                                                <c:when test="${dong.thanhCong && dong.luaChonXuLy == 'BO_QUA'}">
                                                    <span class="badge" style="background:var(--slate-100); color:var(--slate-600);">Đã bỏ qua</span>
                                                </c:when>
                                                <c:when test="${dong.thanhCong}">
                                                    <span class="badge badge-valid">Thêm mới thành công</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge-error">Bỏ qua do lỗi</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="font-size: 12.5px; color: var(--slate-600);">
                                            <c:out value="${dong.ghiChuKetQua}" />
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>

                    <div class="import-action-bar">
                        <a href="${pageContext.request.contextPath}/khach-hang/import" class="btn-outline">
                            <span class="material-symbols-outlined icon-sm" aria-hidden="true">upload_file</span>
                            <span>Tiếp tục nhập tệp khác</span>
                        </a>
                        <a href="${pageContext.request.contextPath}/khach-hang" class="btn-primary">
                            <span>Đến danh sách khách hàng</span>
                            <span class="material-symbols-outlined icon-sm" aria-hidden="true">arrow_forward</span>
                        </a>
                    </div>
                </section>
            </c:if>

        </div>
    </main>

    <!-- JS Tương tác kéo thả, lọc xem trước và xử lý trùng lặp -->
    <script src="${pageContext.request.contextPath}/assets/js/khach-hang/import-excel.js"></script>
</body>
</html>
