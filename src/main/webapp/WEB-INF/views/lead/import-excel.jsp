<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nhập Danh Sách Lead Từ Excel - CRM Bán Hàng</title>
    <!-- CSS Hệ thống chung -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <!-- CSS Chuyên biệt Module Lead S4-02 (Modern SaaS Design) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lead/lead.css?v=2.1">
</head>
<body class="crm-body">

    <!-- Navigation chung hệ thống S1-06 -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <div class="lead-container">

            <!-- 1. Breadcrumb tinh giản -->
            <nav class="lead-breadcrumb" aria-label="Đường dẫn điều hướng">
                <ol class="lead-breadcrumb-nav">
                    <li><a href="${pageContext.request.contextPath}/dieu-huong" class="lead-breadcrumb-link">Trang chủ</a></li>
                    <span class="lead-breadcrumb-sep">/</span>
                    <li><a href="${pageContext.request.contextPath}/lead" class="lead-breadcrumb-link">Lead & Phân bổ</a></li>
                    <span class="lead-breadcrumb-sep">/</span>
                    <li class="lead-breadcrumb-current">Nhập từ Excel</li>
                </ol>
            </nav>

            <!-- 2. Header Bar thanh lịch -->
            <div class="lead-page-header">
                <div class="lead-title-wrap">
                    <h1>
                        <span>Nhập Hàng Loạt Lead Từ Excel</span>
                        <span class="lead-header-pill">Hội thảo / Sự kiện</span>
                    </h1>
                    <p>Nhập nhanh danh sách khách hàng tiềm năng, xem trước và thẩm định lỗi chi tiết từng dòng.</p>
                </div>
                <div class="lead-header-actions">
                    <a href="${pageContext.request.contextPath}/lead/tai-tep-mau" class="lead-btn lead-btn-outline" id="btnTaiTepMau">
                        <span class="material-symbols-outlined" style="font-size: 18px;">download</span>
                        <span>Tải tệp mẫu (.xlsx)</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/lead" class="lead-btn lead-btn-outline">
                        <span class="material-symbols-outlined" style="font-size: 18px;">arrow_back</span>
                        <span>Quay lại</span>
                    </a>
                </div>
            </div>

            <!-- Thông báo Lỗi / Thành công nếu có -->
            <c:if test="${not empty thongBaoLoi}">
                <div class="lead-pill-badge lead-pill-error" style="width: 100%; padding: 12px 16px; margin-bottom: 20px; border-radius: 8px;" role="alert">
                    <span class="material-symbols-outlined" style="font-size: 18px;">error</span>
                    <span><c:out value="${thongBaoLoi}" /></span>
                </div>
            </c:if>
            <c:if test="${not empty thongBaoThanhCong}">
                <div class="lead-pill-badge lead-pill-valid" style="width: 100%; padding: 12px 16px; margin-bottom: 20px; border-radius: 8px;" role="alert">
                    <span class="material-symbols-outlined" style="font-size: 18px;">check_circle</span>
                    <span><c:out value="${thongBaoThanhCong}" /></span>
                </div>
            </c:if>

            <!-- 3. Minimalist Stepper Wizard -->
            <div class="lead-stepper-modern">
                <div class="lead-step-node ${empty baoCao ? 'active' : 'completed'}">
                    <div class="lead-step-num">1</div>
                    <span class="lead-step-name">Chọn tệp Excel</span>
                </div>
                <div class="lead-step-divider"></div>
                <div class="lead-step-node ${cheDo == 'xem-truoc' ? 'active' : (cheDo == 'ket-qua' ? 'completed' : '')}">
                    <div class="lead-step-num">2</div>
                    <span class="lead-step-name">Xem trước & Lọc lỗi</span>
                </div>
                <div class="lead-step-divider"></div>
                <div class="lead-step-node ${cheDo == 'ket-qua' ? 'active' : ''}">
                    <div class="lead-step-num">3</div>
                    <span class="lead-step-name">Hoàn tất nhập</span>
                </div>
            </div>

            <!-- BƯỚC 1: DROPZONE TẢI LÊN & HƯỚNG DẪN TỆP MẪU -->
            <div class="lead-main-card" style="margin-bottom: 24px;">
                <form id="formUploadExcel"
                      action="${pageContext.request.contextPath}/lead/import-excel"
                      method="POST"
                      enctype="multipart/form-data">
                    <input type="hidden" name="action" value="xem-truoc">

                    <div class="lead-dropzone-modern" id="dropzoneExcel" role="button" tabindex="0">
                        <div class="lead-dropzone-icon-wrap">
                            <span class="material-symbols-outlined" style="font-size: 28px;">cloud_upload</span>
                        </div>
                        <div class="lead-dropzone-title">Kéo & thả tệp Excel vào đây, hoặc bấm để chọn tệp</div>
                        <div class="lead-dropzone-desc">Định dạng .xlsx hoặc .xls • Dung lượng tối đa 10 MB</div>
                        <input type="file" id="fileExcel" name="fileExcel" class="lead-file-input" accept=".xlsx, .xls">

                        <div class="lead-selected-file" id="selectedFileInfo" style="${not empty tenTep ? 'display:flex;' : 'display:none;'}">
                            <span class="material-symbols-outlined" style="font-size: 18px; color: var(--lead-primary);">description</span>
                            <span class="lead-file-name" id="selectedFileName"><c:out value="${not empty tenTep ? tenTep : ''}" /></span>
                            <span class="lead-file-size" id="selectedFileSize"></span>
                            <button type="button" class="lead-btn-clear-file" id="btnClearFile" title="Hủy chọn tệp">
                                <span class="material-symbols-outlined" style="font-size: 16px;">close</span>
                            </button>
                        </div>
                    </div>

                    <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 18px; flex-wrap: wrap; gap: 12px;">
                        <div style="font-size: 13px; color: var(--lead-neutral-500); display: flex; align-items: center; gap: 6px;">
                            <span class="material-symbols-outlined" style="font-size: 16px; color: var(--lead-primary);">info</span>
                            <span>Mỗi dòng lead bắt buộc phải có <strong>Nguồn Lead</strong> và <strong>Họ tên</strong>.</span>
                        </div>
                        <button type="submit" class="lead-btn lead-btn-primary" id="btnSubmitPreview">
                            <span class="material-symbols-outlined" style="font-size: 18px;">preview</span>
                            <span>Xem trước dữ liệu</span>
                        </button>
                    </div>
                </form>
            </div>

            <!-- BƯỚC 2: BẢNG XEM TRƯỚC VÀ BÁO LỖI TỪNG DÒNG (AC2, AC3) -->
            <c:if test="${cheDo == 'xem-truoc' && not empty baoCao}">
                <div class="lead-main-card">
                    <!-- Thẻ thống kê 3 ô thanh lịch -->
                    <div class="lead-stats-row">
                        <div class="lead-stat-box">
                            <span class="material-symbols-outlined" style="font-size: 28px; color: var(--lead-neutral-500);">format_list_numbered</span>
                            <div>
                                <div class="lead-stat-num"><c:out value="${baoCao.tongSoDong}" /></div>
                                <div class="lead-stat-lbl">Tổng số dòng</div>
                            </div>
                        </div>
                        <div class="lead-stat-box" style="border-color: var(--lead-success-border);">
                            <span class="material-symbols-outlined" style="font-size: 28px; color: var(--lead-success);">check_circle</span>
                            <div>
                                <div class="lead-stat-num" style="color: var(--lead-success);"><c:out value="${baoCao.soDongHopLe}" /></div>
                                <div class="lead-stat-lbl">Hợp lệ (Sẵn sàng)</div>
                            </div>
                        </div>
                        <div class="lead-stat-box" style="border-color: var(--lead-danger-border);">
                            <span class="material-symbols-outlined" style="font-size: 28px; color: var(--lead-danger);">error</span>
                            <div>
                                <div class="lead-stat-num" style="color: var(--lead-danger);"><c:out value="${baoCao.soDongLoi}" /></div>
                                <div class="lead-stat-lbl">Dòng có lỗi</div>
                            </div>
                        </div>
                    </div>

                    <!-- Filter Control Segmented Tabs -->
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; flex-wrap: wrap; gap: 10px;">
                        <div class="lead-segmented-tabs" role="tablist">
                            <button type="button" class="lead-seg-btn active" data-filter="all">Tất cả (${baoCao.tongSoDong})</button>
                            <button type="button" class="lead-seg-btn" data-filter="valid">Hợp lệ (${baoCao.soDongHopLe})</button>
                            <button type="button" class="lead-seg-btn" data-filter="error">Có lỗi (${baoCao.soDongLoi})</button>
                        </div>
                        <div style="font-size: 13px; color: var(--lead-neutral-500);">
                            Hiển thị kết quả thẩm định theo từng dòng (AC2)
                        </div>
                    </div>

                    <!-- Bảng xem trước -->
                    <form id="formConfirmImport" action="${pageContext.request.contextPath}/lead/import-excel" method="POST">
                        <input type="hidden" name="action" value="nhap-du-lieu">

                        <div class="lead-preview-wrap">
                            <table class="lead-table-modern">
                                <thead>
                                    <tr>
                                        <th style="width: 50px; text-align: center;">Dòng</th>
                                        <th>Họ và Tên</th>
                                        <th>Nguồn Lead (AC3)</th>
                                        <th>Số Điện Thoại</th>
                                        <th>Email</th>
                                        <th>Tên Công Ty</th>
                                        <th style="text-align: center; width: 110px;">Trạng Thái</th>
                                        <th style="width: 240px;">Thông Báo Lỗi</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="dong" items="${baoCao.danhSachTatCaDong}">
                                        <tr class="${dong.hopLe ? 'row-valid' : 'row-error'}">
                                            <td style="text-align: center; font-weight: 600; color: var(--lead-neutral-400);"><c:out value="${dong.soDong}" /></td>
                                            <td><strong><c:out value="${dong.hoTen}" /></strong></td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty dong.tenNguonLead}">
                                                        <span class="lead-pill-badge lead-pill-source"><c:out value="${dong.tenNguonLead}" /></span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="lead-pill-badge lead-pill-error">Thiếu nguồn</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td><c:out value="${not empty dong.soDienThoai ? dong.soDienThoai : '—'}" /></td>
                                            <td><c:out value="${not empty dong.email ? dong.email : '—'}" /></td>
                                            <td><c:out value="${not empty dong.tenCongTy ? dong.tenCongTy : '—'}" /></td>
                                            <td style="text-align: center;">
                                                <span class="lead-pill-badge ${dong.hopLe ? 'lead-pill-valid' : 'lead-pill-error'}">
                                                    ${dong.hopLe ? 'Hợp lệ' : 'Lỗi'}
                                                </span>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${!dong.hopLe}">
                                                        <span style="font-size: 12.5px; color: var(--lead-danger-text); font-weight: 500;">
                                                            <c:forEach var="loi" items="${dong.danhSachLoi}">
                                                                <div>• <c:out value="${loi}" /></div>
                                                            </c:forEach>
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span style="font-size: 12.5px; color: var(--lead-success-text);">Sẵn sàng nạp vào CRM</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>

                        <!-- Submit actions -->
                        <div style="display: flex; justify-content: flex-end; gap: 10px;">
                            <a href="${pageContext.request.contextPath}/lead/import-excel" class="lead-btn lead-btn-outline">Hủy & Chọn lại tệp</a>
                            <button type="submit" class="lead-btn lead-btn-primary" id="btnConfirmImport" ${baoCao.soDongHopLe == 0 ? 'disabled' : ''}>
                                <span class="material-symbols-outlined" style="font-size: 18px;">check</span>
                                <span>Xác nhận nhập dữ liệu (<c:out value="${baoCao.soDongHopLe}" /> dòng)</span>
                            </button>
                        </div>
                    </form>
                </div>
            </c:if>

            <!-- BƯỚC 3: KẾT QUẢ TỔNG KẾT SAU KHI NHẬP THÀNH CÔNG -->
            <c:if test="${cheDo == 'ket-qua' && not empty baoCao}">
                <div class="lead-main-card" style="text-align: center; padding: 40px 24px;">
                    <div style="width: 56px; height: 56px; border-radius: 50%; background: #ecfdf5; color: var(--lead-success); display: inline-flex; align-items: center; justify-content: center; margin-bottom: 16px;">
                        <span class="material-symbols-outlined" style="font-size: 32px;">task_alt</span>
                    </div>
                    <h2 style="font-size: 20px; font-weight: 700; color: var(--lead-neutral-900); margin: 0 0 6px 0;">Nhập danh sách Lead hoàn tất!</h2>
                    <p style="font-size: 14px; color: var(--lead-neutral-500); margin: 0 0 24px 0;"><c:out value="${baoCao.thongDiep}" /></p>

                    <div style="display: inline-flex; gap: 12px;">
                        <a href="${pageContext.request.contextPath}/lead/import-excel" class="lead-btn lead-btn-outline">Nhập thêm tệp khác</a>
                        <a href="${pageContext.request.contextPath}/lead" class="lead-btn lead-btn-primary">Xem danh sách Lead</a>
                    </div>
                </div>
            </c:if>

        </div>
    </main>

    <script src="${pageContext.request.contextPath}/assets/js/lead/import-excel.js"></script>
</body>
</html>
