<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Nhập danh sách khách hàng tiềm năng (Lead) hàng loạt từ tệp Excel, xem trước dữ liệu và báo lỗi chi tiết từng dòng - CRM Bán Hàng Nhóm 10">
    <title>Nhập Danh Sách Lead Từ Excel - CRM Bán Hàng</title>
    <!-- CSS Hệ thống chung (Navigation & Typography) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <!-- CSS Chuyên biệt Module Lead (Story S4-02) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lead/lead.css">
</head>
<body class="crm-body">

    <!-- Navigation & Sidebar dùng chung toàn hệ thống -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <div class="lead-container">

            <!-- 1. Breadcrumb điều hướng -->
            <nav class="lead-breadcrumb" aria-label="Đường dẫn điều hướng">
                <ol class="lead-breadcrumb-nav">
                    <li><a href="${pageContext.request.contextPath}/dieu-huong" class="lead-breadcrumb-link">Trang chủ</a></li>
                    <span class="lead-breadcrumb-sep" aria-hidden="true">/</span>
                    <li><a href="${pageContext.request.contextPath}/lead" class="lead-breadcrumb-link">Lead & Phân bổ</a></li>
                    <span class="lead-breadcrumb-sep" aria-hidden="true">/</span>
                    <li class="lead-breadcrumb-current" aria-current="page">Nhập danh sách từ Excel</li>
                </ol>
                <div>
                    <a href="${pageContext.request.contextPath}/lead" class="lead-btn lead-btn-outline lead-btn-sm" id="btnBackLeadList">
                        <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 18px;">arrow_back</span>
                        <span>Quay lại danh sách Lead</span>
                    </a>
                </div>
            </nav>

            <!-- 2. Header Card -->
            <header class="lead-header-card">
                <div class="lead-header-title">
                    <h1>
                        <span class="material-symbols-outlined" style="color: var(--lead-primary); font-size: 26px;" aria-hidden="true">upload_file</span>
                        <span>Nhập Hàng Loạt Khách Hàng Tiềm Năng Từ Excel</span>
                        <span class="lead-header-badge">Story S4-02 / AC2 & AC3</span>
                    </h1>
                    <p>Đưa danh sách khách hàng thu được từ hội thảo, sự kiện hoặc chiến dịch tiếp thị vào CRM nhanh chóng. Có tệp mẫu chuẩn, hỗ trợ xem trước và thẩm định lỗi chi tiết theo từng dòng (AC2, AC3).</p>
                </div>
            </header>

            <!-- 3. Tiến trình Stepper Wizard (3 Bước chuẩn) -->
            <nav class="lead-stepper" aria-label="Tiến trình nhập dữ liệu">
                <div class="lead-step-item ${empty baoCao ? 'active' : 'completed'}">
                    <div class="lead-step-circle">1</div>
                    <div class="lead-step-text">
                        <span class="lead-step-title">Bước 1: Tệp mẫu</span>
                        <span class="lead-step-desc">Chuẩn bị dữ liệu chuẩn (.xlsx)</span>
                    </div>
                </div>
                <div class="lead-step-item ${cheDo == 'xem-truoc' ? 'active' : (cheDo == 'ket-qua' ? 'completed' : '')}">
                    <div class="lead-step-circle">2</div>
                    <div class="lead-step-text">
                        <span class="lead-step-title">Bước 2: Xem trước & Báo lỗi</span>
                        <span class="lead-step-desc">Thẩm định chi tiết theo từng dòng</span>
                    </div>
                </div>
                <div class="lead-step-item ${cheDo == 'ket-qua' ? 'active' : ''}">
                    <div class="lead-step-circle">3</div>
                    <div class="lead-step-text">
                        <span class="lead-step-title">Bước 3: Nhập dữ liệu</span>
                        <span class="lead-step-desc">Lưu vào CRM & Báo cáo</span>
                    </div>
                </div>
            </nav>

            <!-- Thông báo Lỗi nếu có -->
            <c:if test="${not empty thongBaoLoi}">
                <div class="lead-alert lead-alert-danger" role="alert">
                    <span class="material-symbols-outlined" aria-hidden="true">error</span>
                    <div>
                        <strong>Lỗi xử lý:</strong> <c:out value="${thongBaoLoi}" />
                    </div>
                </div>
            </c:if>

            <!-- CARD 1: HƯỚNG DẪN & TẢI TỆP MẪU EXCEL CHUẨN (AC2) -->
            <section class="lead-form-card" aria-labelledby="heading-step1">
                <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 14px; margin-bottom: 16px;">
                    <div>
                        <h2 id="heading-step1" class="lead-section-title" style="margin: 0 0 4px 0;">
                            <span class="material-symbols-outlined" aria-hidden="true">download_for_offline</span>
                            <span>Bước 1: Tải tệp mẫu Excel chuẩn (.xlsx)</span>
                        </h2>
                        <div class="lead-hint">Tải bảng tính mẫu đã được thiết lập đúng các cột dữ liệu theo chuẩn hệ thống CRM.</div>
                    </div>
                    <div>
                        <a id="btnTaiTepMau"
                           href="${pageContext.request.contextPath}/lead/tai-tep-mau"
                           class="lead-btn lead-btn-primary lead-btn-sm"
                           title="Tải tệp mẫu Excel về máy tính">
                            <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 18px;">download</span>
                            <span>Tải tệp mẫu Excel (.xlsx)</span>
                        </a>
                    </div>
                </div>

                <!-- Quy tắc dữ liệu quan trọng (AC2 & AC3) -->
                <div class="lead-rules-grid">
                    <div class="lead-rule-card">
                        <div class="lead-rule-header">
                            <span class="material-symbols-outlined" style="color: var(--lead-warning); font-size: 18px;" aria-hidden="true">verified_user</span>
                            <span>Nguồn Lead bắt buộc (AC3)</span>
                        </div>
                        <div class="lead-rule-desc">
                            Mỗi dòng lead <strong>bắt buộc phải có Nguồn</strong> (Ví dụ: Sự kiện, Danh thiếp, Hội thảo...). Dòng thiếu nguồn sẽ bị hệ thống đánh dấu lỗi và ngăn lưu.
                        </div>
                    </div>

                    <div class="lead-rule-card">
                        <div class="lead-rule-header">
                            <span class="material-symbols-outlined" style="color: var(--lead-primary); font-size: 18px;" aria-hidden="true">person</span>
                            <span>Họ tên khách hàng</span>
                        </div>
                        <div class="lead-rule-desc">
                            Cột <strong>Họ và tên (*)</strong> không được để trống. Độ dài tối đa 150 ký tự, hỗ trợ đầy đủ tiếng Việt có dấu.
                        </div>
                    </div>

                    <div class="lead-rule-card">
                        <div class="lead-rule-header">
                            <span class="material-symbols-outlined" style="color: var(--lead-success); font-size: 18px;" aria-hidden="true">rule</span>
                            <span>Xem trước & Báo lỗi dòng (AC2)</span>
                        </div>
                        <div class="lead-rule-desc">
                            Sau khi tải lên, hệ thống sẽ <strong>hiển thị toàn bộ bảng xem trước</strong> kèm thông báo lỗi cụ thể ở từng dòng để bạn chỉnh sửa kịp thời.
                        </div>
                    </div>
                </div>
            </section>

            <!-- CARD 2: TẢI TỆP LÊN & XEM TRƯỚC (AC2) -->
            <section class="lead-form-card" aria-labelledby="heading-step2">
                <h2 id="heading-step2" class="lead-section-title">
                    <span class="material-symbols-outlined" aria-hidden="true">cloud_upload</span>
                    <span>Bước 2: Tải lên tệp Excel chứa danh sách Lead</span>
                </h2>
                <div class="lead-hint" style="margin-bottom: 16px;">Chọn hoặc kéo thả bảng tính Excel (.xlsx hoặc .xls) từ máy tính của bạn.</div>

                <form id="formUploadExcel"
                      action="${pageContext.request.contextPath}/lead/import-excel"
                      method="POST"
                      enctype="multipart/form-data">
                    <input type="hidden" name="action" value="xem-truoc">

                    <div class="lead-dropzone" id="dropzoneExcel" role="button" tabindex="0" aria-label="Khu vực kéo thả tệp Excel">
                        <div class="lead-dropzone-icon">
                            <span class="material-symbols-outlined" aria-hidden="true">cloud_upload</span>
                        </div>
                        <div class="lead-dropzone-title">Kéo & thả tệp Excel vào đây, hoặc bấm để duyệt tệp</div>
                        <div class="lead-dropzone-desc">Định dạng hỗ trợ: .xlsx, .xls • Dung lượng tối đa: 10 MB</div>
                        <input type="file" id="fileExcel" name="fileExcel" class="lead-file-input" accept=".xlsx, .xls" aria-label="Tệp Excel danh sách lead">

                        <!-- Hiển thị tên file đã chọn -->
                        <div class="lead-selected-file" id="selectedFileInfo" style="${not empty tenTep ? 'display:flex;' : 'display:none;'}">
                            <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 20px; color: var(--lead-primary);">table_chart</span>
                            <span class="lead-file-name" id="selectedFileName"><c:out value="${not empty tenTep ? tenTep : ''}" /></span>
                            <span class="lead-file-size" id="selectedFileSize"></span>
                            <button type="button" class="lead-btn-clear-file" id="btnClearFile" aria-label="Hủy chọn tệp này" title="Hủy chọn tệp này">
                                <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 18px;">close</span>
                            </button>
                        </div>
                    </div>

                    <div class="lead-form-actions" style="margin-top: 18px; border-top: none; padding-top: 0;">
                        <button type="submit" class="lead-btn lead-btn-primary" id="btnSubmitPreview">
                            <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 18px;">preview</span>
                            <span>Xem trước dữ liệu tệp</span>
                        </button>
                    </div>
                </form>
            </section>

            <!-- CARD 3: BẢNG XEM TRƯỚC VÀ BÁO LỖI THEO TỪNG DÒNG (AC2, AC3) -->
            <c:if test="${cheDo == 'xem-truoc' && not empty baoCao}">
                <section class="lead-form-card" aria-labelledby="heading-preview">
                    <div style="margin-bottom: 20px;">
                        <h2 id="heading-preview" class="lead-section-title" style="margin-bottom: 4px;">
                            <span class="material-symbols-outlined" aria-hidden="true">table_view</span>
                            <span>Bảng xem trước & Thẩm định lỗi từng dòng (AC2, AC3)</span>
                        </h2>
                        <div class="lead-hint">Kiểm tra kết quả phân tích từng dòng. Dòng lỗi sẽ được thông báo nguyên nhân cụ thể để bạn xử lý.</div>
                    </div>

                    <!-- THẺ THỐNG KÊ TỔNG HỢP -->
                    <div class="lead-stats-summary">
                        <div class="lead-stat-card">
                            <div class="lead-stat-icon total">
                                <span class="material-symbols-outlined" aria-hidden="true">format_list_numbered</span>
                            </div>
                            <div>
                                <span class="lead-stat-number"><c:out value="${baoCao.tongSoDong}" /></span>
                                <span class="lead-stat-label">Tổng số dòng đọc được</span>
                            </div>
                        </div>

                        <div class="lead-stat-card">
                            <div class="lead-stat-icon valid">
                                <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                            </div>
                            <div>
                                <span class="lead-stat-number" style="color: var(--lead-success);"><c:out value="${baoCao.soDongHopLe}" /></span>
                                <span class="lead-stat-label">Dòng hợp lệ (Sẵn sàng nạp)</span>
                            </div>
                        </div>

                        <div class="lead-stat-card">
                            <div class="lead-stat-icon error">
                                <span class="material-symbols-outlined" aria-hidden="true">error</span>
                            </div>
                            <div>
                                <span class="lead-stat-number" style="color: var(--lead-danger);"><c:out value="${baoCao.soDongLoi}" /></span>
                                <span class="lead-stat-label">Dòng có lỗi (Cần sửa đổi)</span>
                            </div>
                        </div>
                    </div>

                    <!-- THANH CÔNG CỤ LỌC DÒNG -->
                    <div class="lead-filter-bar">
                        <div class="lead-tab-group" role="tablist" aria-label="Bộ lọc dòng xem trước">
                            <button type="button" class="lead-tab-btn active" data-filter="all">Tất cả (${baoCao.tongSoDong})</button>
                            <button type="button" class="lead-tab-btn" data-filter="valid">Hợp lệ (${baoCao.soDongHopLe})</button>
                            <button type="button" class="lead-tab-btn" data-filter="error">Có lỗi (${baoCao.soDongLoi})</button>
                        </div>
                    </div>

                    <!-- FORM XÁC NHẬN NHẬP DỮ LIỆU -->
                    <form id="formConfirmImport"
                          action="${pageContext.request.contextPath}/lead/import-excel"
                          method="POST">
                        <input type="hidden" name="action" value="nhap-du-lieu">

                        <!-- BẢNG XEM TRƯỚC CHI TIẾT DỮ LIỆU -->
                        <div class="lead-table-container">
                            <table class="lead-preview-table" aria-label="Bảng xem trước dữ liệu Lead từ Excel">
                                <thead>
                                    <tr>
                                        <th style="width: 60px; text-align: center;">Dòng</th>
                                        <th style="width: 170px;">Họ và Tên</th>
                                        <th style="width: 150px;">Nguồn Lead (AC3)</th>
                                        <th style="width: 120px;">Số Điện Thoại</th>
                                        <th style="width: 180px;">Email</th>
                                        <th style="width: 170px;">Tên Công Ty</th>
                                        <th style="width: 130px; text-align: center;">Trạng Thái</th>
                                        <th style="min-width: 200px;">Chi Tiết Lỗi / Báo Lỗi Dòng (AC2)</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="dong" items="${baoCao.danhSachTatCaDong}">
                                        <tr class="${dong.hopLe ? 'row-valid' : 'row-error'}">
                                            <td style="text-align: center; font-weight: 600; color: var(--lead-neutral-500);">
                                                <c:out value="${dong.soDong}" />
                                            </td>
                                            <td>
                                                <strong style="color: var(--lead-neutral-900);"><c:out value="${dong.hoTen}" /></strong>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty dong.tenNguonLead}">
                                                        <span class="lead-badge lead-badge-source">
                                                            <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">label</span>
                                                            <c:out value="${dong.tenNguonLead}" />
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="lead-badge lead-badge-error">
                                                            <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">warning</span>
                                                            Thiếu nguồn
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="font-mono">
                                                <c:out value="${not empty dong.soDienThoai ? dong.soDienThoai : '—'}" />
                                            </td>
                                            <td>
                                                <c:out value="${not empty dong.email ? dong.email : '—'}" />
                                            </td>
                                            <td>
                                                <c:out value="${not empty dong.tenCongTy ? dong.tenCongTy : '—'}" />
                                            </td>
                                            <td style="text-align: center;">
                                                <c:choose>
                                                    <c:when test="${dong.hopLe}">
                                                        <span class="lead-badge lead-badge-valid">
                                                            <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">check</span>
                                                            Hợp lệ
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="lead-badge lead-badge-error">
                                                            <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">close</span>
                                                            Lỗi dữ liệu
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${!dong.hopLe}">
                                                        <ul class="lead-row-error-list">
                                                            <c:forEach var="loi" items="${dong.danhSachLoi}">
                                                                <li><c:out value="${loi}" /></li>
                                                            </c:forEach>
                                                        </ul>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span style="font-size: 12.5px; color: var(--lead-success-text); font-weight: 500;">
                                                            Sẵn sàng thêm mới vào CRM
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    <tr id="previewEmptyRow" style="display: none;">
                                        <td colspan="8" style="text-align: center; padding: 32px 16px; color: var(--lead-neutral-500);">
                                            <span class="material-symbols-outlined" style="font-size: 32px; color: var(--lead-neutral-400); margin-bottom: 6px; display: block;" aria-hidden="true">filter_alt_off</span>
                                            Không có dòng nào phù hợp với bộ lọc đã chọn.
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>

                        <!-- HÀNH ĐỘNG XÁC NHẬN NHẬP DỮ LIỆU -->
                        <div class="lead-form-actions">
                            <a href="${pageContext.request.contextPath}/lead/import-excel" class="lead-btn lead-btn-outline" id="btnHuyPreview">
                                <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 18px;">replay</span>
                                <span>Hủy & Chọn lại tệp</span>
                            </a>
                            <button type="submit"
                                    class="lead-btn lead-btn-primary"
                                    id="btnConfirmImport"
                                    ${baoCao.soDongHopLe == 0 ? 'disabled' : ''}>
                                <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 18px;">check_circle</span>
                                <span>Xác nhận nhập dữ liệu (<c:out value="${baoCao.soDongHopLe}" /> dòng hợp lệ)</span>
                            </button>
                        </div>
                    </form>
                </section>
            </c:if>

            <!-- CARD 4: KẾT QUẢ TỔNG KẾT SAU KHI NHẬP THÀNH CÔNG -->
            <c:if test="${cheDo == 'ket-qua' && not empty baoCao}">
                <section class="lead-form-card" aria-labelledby="heading-result">
                    <h2 id="heading-result" class="lead-section-title">
                        <span class="material-symbols-outlined" style="color: var(--lead-success);" aria-hidden="true">task_alt</span>
                        <span>Bước 3: Kết quả nhập danh sách khách hàng tiềm năng</span>
                    </h2>

                    <div class="lead-alert lead-alert-success" style="margin-bottom: 20px;">
                        <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                        <div>
                            <strong>Hoàn tất thành công:</strong> <c:out value="${baoCao.thongDiep}" />
                        </div>
                    </div>

                    <div class="lead-stats-summary">
                        <div class="lead-stat-card">
                            <div class="lead-stat-icon valid">
                                <span class="material-symbols-outlined" aria-hidden="true">person_add</span>
                            </div>
                            <div>
                                <span class="lead-stat-number" style="color: var(--lead-success);"><c:out value="${baoCao.soDongThanhCong}" /></span>
                                <span class="lead-stat-label">Bản ghi Lead đã thêm mới</span>
                            </div>
                        </div>

                        <div class="lead-stat-card">
                            <div class="lead-stat-icon total">
                                <span class="material-symbols-outlined" aria-hidden="true">description</span>
                            </div>
                            <div>
                                <span class="lead-stat-number"><c:out value="${baoCao.tenTep}" /></span>
                                <span class="lead-stat-label">Tên tệp Excel đã xử lý</span>
                            </div>
                        </div>
                    </div>

                    <div class="lead-form-actions">
                        <a href="${pageContext.request.contextPath}/lead/import-excel" class="lead-btn lead-btn-outline">
                            <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 18px;">upload_file</span>
                            <span>Tiếp tục nhập tệp khác</span>
                        </a>
                        <a href="${pageContext.request.contextPath}/lead" class="lead-btn lead-btn-primary">
                            <span class="material-symbols-outlined" aria-hidden="true" style="font-size: 18px;">list_alt</span>
                            <span>Xem danh sách Lead trong CRM</span>
                        </a>
                    </div>
                </section>
            </c:if>

        </div>
    </main>

    <!-- JavaScript chuyên biệt cho Nhập Excel (Dropzone, Xem trước & Lọc lỗi dòng) -->
    <script src="${pageContext.request.contextPath}/assets/js/lead/import-excel.js"></script>
</body>
</html>
