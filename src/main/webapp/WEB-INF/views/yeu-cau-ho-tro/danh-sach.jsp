<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Quản lý yêu cầu hỗ trợ sau bán - Hệ thống CRM Bán Hàng">
    <title>Yêu Cầu Hỗ Trợ Sau Bán - CRM Bán Hàng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/khach-hang/khach-hang.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <style>
        .filter-bar {
            background: #ffffff;
            border-radius: var(--radius-lg);
            border: 1px solid var(--border-color);
            padding: 18px 20px;
            margin-bottom: 20px;
            box-shadow: var(--shadow-sm);
        }
        .filter-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 14px;
            align-items: flex-end;
        }
        .priority-badge {
            font-size: 11.5px;
            font-weight: 700;
            padding: 3px 8px;
            border-radius: 9999px;
            display: inline-flex;
            align-items: center;
            text-transform: uppercase;
        }
        .priority-THAP { background: #f1f5f9; color: #475569; }
        .priority-BINH_THUONG { background: #e0f2fe; color: #0369a1; }
        .priority-CAO { background: #fef3c7; color: #b45309; }
        .priority-KHAN_CAP { background: #fee2e2; color: #b91c1c; font-weight: 800; }
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
    </style>
</head>
<body class="crm-body">
    <!-- Navigation layout S1-06 -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <!-- Breadcrumb -->
        <div class="breadcrumb-container" style="margin-bottom: 16px;">
            <a href="${pageContext.request.contextPath}/" class="breadcrumb-item">Trang chủ</a>
            <span class="breadcrumb-separator">/</span>
            <a href="${pageContext.request.contextPath}/khach-hang" class="breadcrumb-item">Khách hàng</a>
            <span class="breadcrumb-separator">/</span>
            <span class="breadcrumb-current">Yêu cầu hỗ trợ sau bán</span>
        </div>

        <!-- Header -->
        <div class="page-header" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 12px;">
            <div>
                <h1 style="font-size: 22px; font-weight: 800; color: var(--slate-900); margin: 0; display: flex; align-items: center; gap: 8px;">
                    <span class="material-symbols-outlined" style="color: var(--primary);">support_agent</span>
                    Yêu Cầu Hỗ Trợ Sau Bán & Gắn Cờ Rủi Ro
                </h1>
                <p style="margin: 4px 0 0; font-size: 13.5px; color: var(--slate-500);">
                    Ghi nhận yêu cầu hỗ trợ, theo dõi mức độ ưu tiên và phát hiện khách hàng có rủi ro rời bỏ (Story S3-08).
                </p>
            </div>
            <div>
                <button type="button" class="btn btn-primary" onclick="moModalGhiNhanYeuCau()" id="btnThemYeuCau">
                    <span class="material-symbols-outlined" aria-hidden="true">add</span>
                    Ghi nhận yêu cầu hỗ trợ
                </button>
            </div>
        </div>

        <!-- Alerts -->
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

        <!-- Filter Bar -->
        <div class="filter-bar">
            <form action="${pageContext.request.contextPath}/yeu-cau-ho-tro" method="GET" class="filter-grid">
                <div>
                    <label class="form-label" for="filterKhachHang">Khách hàng</label>
                    <select id="filterKhachHang" name="khachHangId" class="form-select">
                        <option value="">-- Tất cả khách hàng --</option>
                        <c:forEach var="kh" items="${danhSachKhachHang}">
                            <option value="${kh.id}" ${kh.id == khachHangIdChon ? 'selected' : ''}>
                                <c:out value="${kh.tenCongTy}" /> ${kh.coRuiRo ? '[CỜ RỦI RO]' : ''}
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div>
                    <label class="form-label" for="filterTrangThai">Trạng thái</label>
                    <select id="filterTrangThai" name="trangThai" class="form-select">
                        <option value="TAT_CA">-- Tất cả trạng thái --</option>
                        <c:forEach var="tt" items="${trangThaiList}">
                            <option value="${tt.ma}" ${tt.ma == trangThaiChon ? 'selected' : ''}>
                                <c:out value="${tt.tenHienThi}" />
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div>
                    <label class="form-label" for="filterMucUuTien">Mức độ ưu tiên</label>
                    <select id="filterMucUuTien" name="mucUuTien" class="form-select">
                        <option value="TAT_CA">-- Tất cả mức độ --</option>
                        <c:forEach var="ut" items="${mucUuTienList}">
                            <option value="${ut.ma}" ${ut.ma == mucUuTienChon ? 'selected' : ''}>
                                <c:out value="${ut.tenHienThi}" />
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div>
                    <label class="form-label" for="filterTuKhoa">Từ khóa tìm kiếm</label>
                    <input type="text" id="filterTuKhoa" name="tuKhoa" class="form-input" placeholder="Mã ticket, tiêu đề, công ty..." value="${tuKhoa}">
                </div>
                <div style="display: flex; gap: 8px;">
                    <button type="submit" class="btn btn-primary" style="flex: 1;">
                        <span class="material-symbols-outlined" aria-hidden="true">search</span>
                        Lọc
                    </button>
                    <a href="${pageContext.request.contextPath}/yeu-cau-ho-tro" class="btn btn-outline" title="Đặt lại bộ lọc">
                        <span class="material-symbols-outlined" aria-hidden="true">refresh</span>
                    </a>
                </div>
            </form>
        </div>

        <!-- Support Tickets Table -->
        <div class="table-container" style="background: #ffffff; border-radius: var(--radius-lg); border: 1px solid var(--border-color); overflow: hidden;">
            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width: 120px;">Mã Ticket</th>
                        <th>Khách Hàng / Công Ty</th>
                        <th>Tiêu Đề Yêu Cầu</th>
                        <th style="width: 120px; text-align: center;">Ưu Tiên</th>
                        <th style="width: 150px;">Người Xử Lý</th>
                        <th style="width: 130px; text-align: center;">Trạng Thái</th>
                        <th style="width: 130px;">Ngày Tạo</th>
                        <th style="width: 150px; text-align: center;">Đổi Trạng Thái</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty danhSachYeuCau}">
                            <c:forEach var="yc" items="${danhSachYeuCau}">
                                <tr>
                                    <td class="font-mono" style="font-weight: 600;">
                                        <c:out value="${yc.maYeuCau}" />
                                    </td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/chi-tiet-ban-ghi?id=${yc.khachHangId}" style="font-weight: 600; color: var(--primary); text-decoration: none;">
                                            <c:out value="${yc.tenKhachHang}" />
                                        </a>
                                    </td>
                                    <td>
                                        <div style="font-weight: 500;"><c:out value="${yc.tieuDe}" /></div>
                                        <c:if test="${not empty yc.noiDung}">
                                            <div style="font-size: 12px; color: var(--slate-500); margin-top: 2px;"><c:out value="${yc.noiDung}" /></div>
                                        </c:if>
                                    </td>
                                    <td style="text-align: center;">
                                        <span class="priority-badge priority-${yc.mucUuTien}">
                                            <c:out value="${yc.mucUuTienEnum.tenHienThi}" />
                                        </span>
                                    </td>
                                    <td>
                                        <c:out value="${not empty yc.tenNguoiXuLy ? yc.tenNguoiXuLy : 'Chưa phân công'}" />
                                    </td>
                                    <td style="text-align: center;">
                                        <span class="status-badge status-${yc.trangThai}">
                                            <c:out value="${yc.trangThaiEnum.tenHienThi}" />
                                        </span>
                                    </td>
                                    <td style="font-size: 12.5px; color: var(--slate-600);">
                                        <c:out value="${yc.taoLuc}" />
                                    </td>
                                    <td style="text-align: center;">
                                        <form action="${pageContext.request.contextPath}/yeu-cau-ho-tro" method="POST" style="margin: 0;">
                                            <input type="hidden" name="action" value="cap-nhat-trang-thai">
                                            <input type="hidden" name="id" value="${yc.id}">
                                            <input type="hidden" name="redirectUrl" value="${pageContext.request.contextPath}/yeu-cau-ho-tro">
                                            <select name="trangThai" class="form-select" style="font-size: 12px; padding: 3px 6px; width: auto;" onchange="this.form.submit()">
                                                <option value="MOI" ${yc.trangThai == 'MOI' ? 'selected' : ''}>Mới</option>
                                                <option value="DANG_XU_LY" ${yc.trangThai == 'DANG_XU_LY' ? 'selected' : ''}>Đang xử lý</option>
                                                <option value="CHO_KHACH_HANG" ${yc.trangThai == 'CHO_KHACH_HANG' ? 'selected' : ''}>Chờ khách</option>
                                                <option value="DA_XU_LY" ${yc.trangThai == 'DA_XU_LY' ? 'selected' : ''}>Đã xử lý</option>
                                                <option value="DONG" ${yc.trangThai == 'DONG' ? 'selected' : ''}>Đóng</option>
                                                <option value="HUY" ${yc.trangThai == 'HUY' ? 'selected' : ''}>Hủy</option>
                                            </select>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="8" style="text-align: center; padding: 36px; color: var(--slate-500);">
                                    Không tìm thấy yêu cầu hỗ trợ nào phù hợp với điều kiện tìm kiếm.
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>

        <!-- Modal Ghi Nhận Yêu Cầu Hỗ Trợ Mới -->
        <div class="modal-backdrop" id="modalGhiNhanYeuCau" style="display: none;" role="dialog" aria-modal="true">
            <div class="modal-card" style="max-width: 580px;">
                <div class="modal-header">
                    <div>
                        <h2 class="modal-title">Ghi Nhận Yêu Cầu Hỗ Trợ Sau Bán</h2>
                        <p class="modal-subtitle">Ghi nhận thông tin sự cố, mức độ ưu tiên và người xử lý</p>
                    </div>
                    <button type="button" class="modal-close-btn" onclick="dongModalGhiNhanYeuCau()" aria-label="Đóng">
                        <span class="material-symbols-outlined" aria-hidden="true">close</span>
                    </button>
                </div>
                <form action="${pageContext.request.contextPath}/yeu-cau-ho-tro" method="POST">
                    <input type="hidden" name="action" value="tao">
                    <input type="hidden" name="redirectUrl" value="${pageContext.request.contextPath}/yeu-cau-ho-tro">

                    <div class="modal-body">
                        <div class="form-group">
                            <label class="form-label" for="selectKhachHang">Khách hàng / Công ty <span class="required" style="color: red;">*</span></label>
                            <select id="selectKhachHang" name="khachHangId" class="form-select" required>
                                <option value="">-- Chọn khách hàng --</option>
                                <c:forEach var="kh" items="${danhSachKhachHang}">
                                    <option value="${kh.id}">
                                        <c:out value="${kh.tenCongTy}" /> <c:if test="${not empty kh.maKhachHang}">(${kh.maKhachHang})</c:if>
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="form-group">
                            <label class="form-label" for="tieuDeYeuCau">Tiêu đề yêu cầu <span class="required" style="color: red;">*</span></label>
                            <input type="text" id="tieuDeYeuCau" name="tieuDe" class="form-input" placeholder="Ví dụ: Lỗi đồng bộ tài khoản..." required autocomplete="off">
                        </div>

                        <div class="form-row" style="display: flex; gap: 16px;">
                            <div class="form-col" style="flex: 1;">
                                <label class="form-label" for="mucUuTien">Mức độ ưu tiên</label>
                                <select id="mucUuTien" name="mucUuTien" class="form-select" required>
                                    <option value="BINH_THUONG" selected>Bình thường</option>
                                    <option value="THAP">Thấp</option>
                                    <option value="CAO">Cao</option>
                                    <option value="KHAN_CAP">Khẩn cấp</option>
                                </select>
                            </div>
                            <div class="form-col" style="flex: 1;">
                                <label class="form-label" for="trangThai">Trạng thái ban đầu</label>
                                <select id="trangThai" name="trangThai" class="form-select" required>
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
                            <label class="form-label" for="noiDung">Nội dung chi tiết</label>
                            <textarea id="noiDung" name="noiDung" class="form-textarea" rows="3" placeholder="Mô tả sự cố hoặc yêu cầu..."></textarea>
                        </div>
                    </div>

                    <div class="modal-footer" style="padding: 16px 24px; border-top: 1px solid var(--border-color); display: flex; justify-content: flex-end; gap: 12px; background: #f8fafc;">
                        <button type="button" class="btn btn-outline" onclick="dongModalGhiNhanYeuCau()">Hủy</button>
                        <button type="submit" class="btn btn-primary">
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
            var m = document.getElementById('modalGhiNhanYeuCau');
            if (m) m.style.display = 'flex';
        }
        function dongModalGhiNhanYeuCau() {
            var m = document.getElementById('modalGhiNhanYeuCau');
            if (m) m.style.display = 'none';
        }
    </script>
</body>
</html>
