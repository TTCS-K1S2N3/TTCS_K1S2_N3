<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cơ cấu tổ chức kinh doanh - CRM</title>
    <meta name="description" content="Khai báo cây cơ cấu tổ chức kinh doanh, phân bổ trưởng nhóm và khu vực địa lý.">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <style>
        .cctc-container {
            padding: 24px;
            max-width: 1380px;
            margin: 0 auto;
        }
        .cctc-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 24px;
            flex-wrap: wrap;
            gap: 16px;
        }
        .cctc-title h1 {
            font-size: 24px;
            font-weight: 700;
            color: #1e293b;
            margin: 0 0 4px 0;
        }
        .cctc-title p {
            color: #64748b;
            font-size: 14px;
            margin: 0;
        }
        .cctc-actions {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
        }
        .btn-cctc {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 8px 16px;
            border-radius: 6px;
            font-size: 14px;
            font-weight: 500;
            cursor: pointer;
            border: 1px solid transparent;
            text-decoration: none;
            transition: all 0.2s;
        }
        .btn-cctc-primary {
            background-color: #2563eb;
            color: #ffffff;
        }
        .btn-cctc-primary:hover {
            background-color: #1d4ed8;
        }
        .btn-cctc-secondary {
            background-color: #ffffff;
            color: #334155;
            border-color: #cbd5e1;
        }
        .btn-cctc-secondary:hover {
            background-color: #f1f5f9;
        }
        .btn-cctc-sm {
            padding: 4px 10px;
            font-size: 12px;
        }
        .cctc-alert {
            padding: 12px 16px;
            border-radius: 6px;
            margin-bottom: 20px;
            font-size: 14px;
        }
        .cctc-alert-success {
            background-color: #ecfdf5;
            color: #065f46;
            border: 1px solid #a7f3d0;
        }
        .cctc-alert-danger {
            background-color: #fef2f2;
            color: #991b1b;
            border: 1px solid #fecaca;
        }
        .cctc-grid {
            display: grid;
            grid-template-columns: 2fr 1fr;
            gap: 24px;
        }
        @media (max-width: 1024px) {
            .cctc-grid {
                grid-template-columns: 1fr;
            }
        }
        .cctc-card {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.05);
            margin-bottom: 24px;
            overflow: hidden;
        }
        .cctc-card-header {
            padding: 16px 20px;
            border-bottom: 1px solid #e2e8f0;
            background-color: #f8fafc;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .cctc-card-header h2 {
            font-size: 16px;
            font-weight: 600;
            color: #0f172a;
            margin: 0;
        }
        .cctc-table-wrap {
            overflow-x: auto;
        }
        .cctc-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 13px;
            text-align: left;
        }
        .cctc-table th {
            background-color: #f1f5f9;
            color: #475569;
            padding: 10px 14px;
            font-weight: 600;
            border-bottom: 1px solid #e2e8f0;
            white-space: nowrap;
        }
        .cctc-table td {
            padding: 12px 14px;
            border-bottom: 1px solid #f1f5f9;
            vertical-align: middle;
        }
        .cctc-table tr:hover td {
            background-color: #f8fafc;
        }
        .badge-leader {
            display: inline-block;
            background-color: #dbeafe;
            color: #1e40af;
            padding: 2px 8px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: 600;
        }
        .badge-region {
            display: inline-block;
            background-color: #fef3c7;
            color: #92400e;
            padding: 2px 8px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: 500;
        }
        .badge-empty {
            color: #94a3b8;
            font-style: italic;
            font-size: 12px;
        }
        .badge-status-active {
            background-color: #dcfce7;
            color: #15803d;
            padding: 2px 6px;
            border-radius: 4px;
            font-size: 11px;
            font-weight: 600;
        }
        .badge-status-inactive {
            background-color: #f1f5f9;
            color: #64748b;
            padding: 2px 6px;
            border-radius: 4px;
            font-size: 11px;
        }
        .tree-indent {
            display: inline-block;
        }
        .tree-branch-icon {
            color: #94a3b8;
            margin-right: 6px;
        }
        /* Modal */
        .cctc-modal {
            display: none;
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(15, 23, 42, 0.5);
            z-index: 1000;
            align-items: center;
            justify-content: center;
        }
        .cctc-modal.active {
            display: flex;
        }
        .cctc-modal-content {
            background: #ffffff;
            border-radius: 8px;
            width: 100%;
            max-width: 560px;
            padding: 24px;
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
            max-height: 90vh;
            overflow-y: auto;
        }
        .cctc-modal-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
            border-bottom: 1px solid #e2e8f0;
            padding-bottom: 12px;
        }
        .cctc-modal-header h3 {
            margin: 0;
            font-size: 18px;
            color: #0f172a;
        }
        .btn-close {
            background: transparent;
            border: none;
            font-size: 20px;
            cursor: pointer;
            color: #64748b;
        }
        .cctc-form-group {
            margin-bottom: 16px;
        }
        .cctc-form-group label {
            display: block;
            margin-bottom: 6px;
            font-size: 13px;
            font-weight: 500;
            color: #334155;
        }
        .cctc-form-control {
            width: 100%;
            padding: 8px 12px;
            border: 1px solid #cbd5e1;
            border-radius: 6px;
            font-size: 14px;
            box-sizing: border-box;
        }
        .cctc-form-control:focus {
            border-color: #2563eb;
            outline: none;
            box-shadow: 0 0 0 2px rgba(37,99,235,0.15);
        }
        .cctc-modal-footer {
            display: flex;
            justify-content: flex-end;
            gap: 10px;
            margin-top: 24px;
            border-top: 1px solid #e2e8f0;
            padding-top: 16px;
        }
    </style>
</head>
<body class="crm-body">

    <!-- Header Navigation -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content" id="crm-main-content">
        <div class="cctc-container">

            <!-- Title & Quick Actions -->
            <div class="cctc-header">
                <div class="cctc-title">
                    <h1>&#128188; Cơ cấu tổ chức kinh doanh</h1>
                    <p>Khai báo cây tổ chức, phân bổ Trưởng nhóm và gắn khu vực địa lý cho các nhóm kinh doanh (Story S2-06)</p>
                </div>
                <div class="cctc-actions">
                    <button class="btn-cctc btn-cctc-primary" onclick="moModalThemNhom()">
                        &#43; Thêm nhóm kinh doanh
                    </button>
                    <button class="btn-cctc btn-cctc-secondary" onclick="moModalThemKhuVuc()">
                        &#127757; Khai báo khu vực
                    </button>
                    <button class="btn-cctc btn-cctc-secondary" onclick="moModalChuyenNhom()">
                        &#128101; Chuyển nhóm nhân viên
                    </button>
                </div>
            </div>

            <!-- Flash Notifications -->
            <c:if test="${not empty sessionScope.flashSuccess}">
                <div class="cctc-alert cctc-alert-success">
                    &#10004; ${sessionScope.flashSuccess}
                </div>
                <c:remove var="flashSuccess" scope="session" />
            </c:if>

            <c:if test="${not empty sessionScope.flashError}">
                <div class="cctc-alert cctc-alert-danger">
                    &#9888; ${sessionScope.flashError}
                </div>
                <c:remove var="flashError" scope="session" />
            </c:if>

            <!-- Main Layout Grid -->
            <div class="cctc-grid">

                <!-- Left Column: Cây tổ chức kinh doanh (AC1, AC2, AC3) -->
                <div>
                    <div class="cctc-card">
                        <div class="cctc-card-header">
                            <h2>&#128451; Cây cơ cấu nhóm kinh doanh</h2>
                            <span style="font-size: 12px; color: #64748b;">(Tổng: ${fn:length(dsNhom)} nhóm)</span>
                        </div>
                        <div class="cctc-table-wrap">
                            <table class="cctc-table" id="bang-nhom-kinh-doanh">
                                <thead>
                                    <tr>
                                        <th>Mã / Tên nhóm</th>
                                        <th>Trưởng nhóm</th>
                                        <th>Khu vực địa lý</th>
                                        <th>Nhân viên</th>
                                        <th>Trạng thái</th>
                                        <th style="text-align: right;">Thao tác</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${empty dsNhom}">
                                            <tr>
                                                <td colspan="6" style="text-align: center; color: #94a3b8; padding: 30px;">
                                                    Chưa có nhóm kinh doanh nào. Hãy nhấn "Thêm nhóm kinh doanh" để khởi tạo cơ cấu tổ chức.
                                                </td>
                                            </tr>
                                        </c:when>
                                        <c:otherwise>
                                            <c:forEach var="nhom" items="${dsNhom}">
                                                <tr>
                                                    <td>
                                                        <div style="padding-left: ${(nhom.capDo - 1) * 20}px;">
                                                            <c:if test="${nhom.capDo > 1}">
                                                                <span class="tree-branch-icon">&#8627;</span>
                                                            </c:if>
                                                            <strong>${nhom.maNhom}</strong> - ${nhom.tenNhom}
                                                            <c:if test="${not empty nhom.tenNhomCha}">
                                                                <div style="font-size: 11px; color: #94a3b8;">(Thuộc: ${nhom.tenNhomCha})</div>
                                                            </c:if>
                                                        </div>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${not empty nhom.tenTruongNhom}">
                                                                <span class="badge-leader">&#128100; ${nhom.tenTruongNhom}</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge-empty">Chưa có</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${not empty nhom.tenKhuVuc}">
                                                                <span class="badge-region">&#127757; ${nhom.tenKhuVuc}</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge-empty">Chưa gán</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <a href="javascript:void(0)" onclick="xemThanhVien(${nhom.idLong}, '${nhom.tenNhom}')" style="color: #2563eb; text-decoration: none; font-weight: 600;">
                                                            ${nhom.soLuongThanhVien} thành viên
                                                        </a>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${nhom.hoatDong}">
                                                                <span class="badge-status-active">Hoạt động</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge-status-inactive">Ngừng</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td style="text-align: right; white-space: nowrap;">
                                                        <button class="btn-cctc btn-cctc-secondary btn-cctc-sm"
                                                                onclick="moModalSuaNhom(${nhom.idLong}, '${nhom.maNhom}', '${nhom.tenNhom}', '${nhom.moTa}', '${nhom.nhomChaIdLong}', '${nhom.khuVucId}', '${nhom.truongNhomId}', ${nhom.hoatDong})">
                                                            Sửa
                                                        </button>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <!-- Right Column: Khu vực địa lý (AC4) & Nhân sự chưa có nhóm (AC2) -->
                <div>
                    <!-- Danh mục khu vực địa lý -->
                    <div class="cctc-card">
                        <div class="cctc-card-header">
                            <h2>&#127757; Khu vực địa lý</h2>
                            <button class="btn-cctc btn-cctc-secondary btn-cctc-sm" onclick="moModalThemKhuVuc()">
                                &#43; Thêm
                            </button>
                        </div>
                        <div class="cctc-table-wrap">
                            <table class="cctc-table">
                                <thead>
                                    <tr>
                                        <th>Mã / Khu vực</th>
                                        <th>Loại</th>
                                        <th>Số nhóm</th>
                                        <th>Thao tác</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${empty dsKhuVuc}">
                                            <tr>
                                                <td colspan="4" style="text-align: center; color: #94a3b8; padding: 20px;">
                                                    Chưa có khu vực địa lý nào.
                                                </td>
                                            </tr>
                                        </c:when>
                                        <c:otherwise>
                                            <c:forEach var="kv" items="${dsKhuVuc}">
                                                <tr>
                                                    <td>
                                                        <strong>${kv.maKhuVuc}</strong> - ${kv.tenKhuVuc}
                                                        <c:if test="${not empty kv.tenKhuVucCha}">
                                                            <div style="font-size: 11px; color: #94a3b8;">(Thuộc: ${kv.tenKhuVucCha})</div>
                                                        </c:if>
                                                    </td>
                                                    <td><span style="font-size: 11px; color: #64748b;">${kv.loaiKhuVuc}</span></td>
                                                    <td><strong>${kv.soLuongNhom}</strong></td>
                                                    <td>
                                                        <button class="btn-cctc btn-cctc-secondary btn-cctc-sm"
                                                                onclick="moModalSuaKhuVuc(${kv.id}, '${kv.maKhuVuc}', '${kv.tenKhuVuc}', '${kv.loaiKhuVuc}', '${kv.khuVucChaId}', ${kv.thuTuHienThi}, ${kv.hoatDong})">
                                                            Sửa
                                                        </button>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <!-- Nhân viên chưa thuộc nhóm nào (AC2: mỗi nhân viên thuộc đúng 1 nhóm) -->
                    <div class="cctc-card">
                        <div class="cctc-card-header">
                            <h2>&#9888; Nhân sự chưa gán nhóm</h2>
                            <span style="font-size: 12px; color: #ef4444; font-weight: 600;">(${fn:length(dsNhanVienChuaCoNhom)})</span>
                        </div>
                        <div class="cctc-table-wrap">
                            <table class="cctc-table">
                                <thead>
                                    <tr>
                                        <th>Họ tên</th>
                                        <th>Email</th>
                                        <th>Thao tác</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${empty dsNhanVienChuaCoNhom}">
                                            <tr>
                                                <td colspan="3" style="text-align: center; color: #16a34a; padding: 16px; font-size: 12px;">
                                                    &#10004; Toàn bộ nhân viên đã được phân bổ vào các nhóm kinh doanh.
                                                </td>
                                            </tr>
                                        </c:when>
                                        <c:otherwise>
                                            <c:forEach var="nv" items="${dsNhanVienChuaCoNhom}">
                                                <tr>
                                                    <td><strong>${nv.hoTen}</strong></td>
                                                    <td><span style="font-size: 12px; color: #64748b;">${nv.email}</span></td>
                                                    <td>
                                                        <button class="btn-cctc btn-cctc-primary btn-cctc-sm"
                                                                onclick="moModalChuyenNhomChoNhanVien(${nv.id}, '${nv.hoTen}')">
                                                            Gán nhóm
                                                        </button>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

            </div>

        </div>
    </main>

    <!-- ==================== MODAL THÊM / SỬA NHÓM KINH DOANH ==================== -->
    <div class="cctc-modal" id="modalNhom">
        <div class="cctc-modal-content">
            <div class="cctc-modal-header">
                <h3 id="modalNhomTitle">Thêm nhóm kinh doanh mới</h3>
                <button type="button" class="btn-close" onclick="dongModal('modalNhom')">&times;</button>
            </div>
            <form action="${pageContext.request.contextPath}/co-cau-to-chuc" method="POST" id="formNhom">
                <input type="hidden" name="action" id="formNhomAction" value="them-nhom">
                <input type="hidden" name="id" id="formNhomId" value="">

                <div class="cctc-form-group">
                    <label for="maNhom">Mã nhóm kinh doanh *</label>
                    <input type="text" class="cctc-form-control" id="maNhom" name="maNhom" required placeholder="Ví dụ: KD-MB, TEAM-HN1">
                </div>

                <div class="cctc-form-group">
                    <label for="tenNhom">Tên nhóm kinh doanh *</label>
                    <input type="text" class="cctc-form-control" id="tenNhom" name="tenNhom" required placeholder="Ví dụ: Khối Kinh doanh Miền Bắc">
                </div>

                <div class="cctc-form-group">
                    <label for="nhomChaId">Nhóm cấp trên (Cây phân cấp - AC1)</label>
                    <select class="cctc-form-control" id="nhomChaId" name="nhomChaId">
                        <option value="">-- Không có (Nhóm cấp cao nhất / Root) --</option>
                        <c:forEach var="n" items="${dsNhom}">
                            <option value="${n.idLong}">${n.maNhom} - ${n.tenNhom}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="cctc-form-group">
                    <label for="khuVucId">Khu vực địa lý (AC4)</label>
                    <select class="cctc-form-control" id="khuVucId" name="khuVucId">
                        <option value="">-- Chưa gán khu vực --</option>
                        <c:forEach var="kv" items="${dsKhuVuc}">
                            <option value="${kv.id}">${kv.maKhuVuc} - ${kv.tenKhuVuc}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="cctc-form-group">
                    <label for="truongNhomId">Trưởng nhóm (AC1: mỗi nhóm có một trưởng nhóm)</label>
                    <select class="cctc-form-control" id="truongNhomId" name="truongNhomId">
                        <option value="">-- Chọn trưởng nhóm --</option>
                        <c:forEach var="u" items="${dsTatCaNguoiDung}">
                            <option value="${u.id}">${u.hoTen} (${u.email})</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="cctc-form-group">
                    <label for="moTa">Mô tả nhiệm vụ / ghi chú</label>
                    <textarea class="cctc-form-control" id="moTa" name="moTa" rows="2" placeholder="Ghi chú về địa bàn hoạt động hoặc mục tiêu nhóm..."></textarea>
                </div>

                <div class="cctc-form-group" id="groupHoatDongNhom" style="display: none;">
                    <label>
                        <input type="checkbox" id="hoatDongNhom" name="hoatDong" value="1" checked>
                        Đang hoạt động
                    </label>
                </div>

                <div class="cctc-modal-footer">
                    <button type="button" class="btn-cctc btn-cctc-secondary" onclick="dongModal('modalNhom')">Hủy</button>
                    <button type="submit" class="btn-cctc btn-cctc-primary" id="btnSubmitNhom">Lưu thông tin</button>
                </div>
            </form>
        </div>
    </div>

    <!-- ==================== MODAL THÊM / SỬA KHU VỰC ĐỊA LÝ (AC4) ==================== -->
    <div class="cctc-modal" id="modalKhuVuc">
        <div class="cctc-modal-content">
            <div class="cctc-modal-header">
                <h3 id="modalKhuVucTitle">Khai báo khu vực địa lý</h3>
                <button type="button" class="btn-close" onclick="dongModal('modalKhuVuc')">&times;</button>
            </div>
            <form action="${pageContext.request.contextPath}/co-cau-to-chuc" method="POST" id="formKhuVuc">
                <input type="hidden" name="action" id="formKhuVucAction" value="them-khu-vuc">
                <input type="hidden" name="id" id="formKhuVucId" value="">

                <div class="cctc-form-group">
                    <label for="maKhuVuc">Mã khu vực *</label>
                    <input type="text" class="cctc-form-control" id="maKhuVuc" name="maKhuVuc" required placeholder="Ví dụ: MB, MN, HN, HCM">
                </div>

                <div class="cctc-form-group">
                    <label for="tenKhuVuc">Tên khu vực *</label>
                    <input type="text" class="cctc-form-control" id="tenKhuVuc" name="tenKhuVuc" required placeholder="Ví dụ: Miền Bắc, Hà Nội">
                </div>

                <div class="cctc-form-group">
                    <label for="loaiKhuVuc">Loại khu vực</label>
                    <select class="cctc-form-control" id="loaiKhuVuc" name="loaiKhuVuc">
                        <option value="MIEN">Vùng / Miền</option>
                        <option value="TINH_THANH" selected>Tỉnh / Thành phố</option>
                        <option value="QUAN_HUYEN">Quận / Huyện</option>
                        <option value="KHAC">Khác</option>
                    </select>
                </div>

                <div class="cctc-form-group">
                    <label for="khuVucChaId">Khu vực cấp trên (nếu có)</label>
                    <select class="cctc-form-control" id="khuVucChaId" name="khuVucChaId">
                        <option value="">-- Không có (Khu vực cao nhất) --</option>
                        <c:forEach var="kv" items="${dsKhuVuc}">
                            <option value="${kv.id}">${kv.maKhuVuc} - ${kv.tenKhuVuc}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="cctc-form-group">
                    <label for="thuTuHienThi">Thứ tự hiển thị</label>
                    <input type="number" class="cctc-form-control" id="thuTuHienThi" name="thuTuHienThi" value="0">
                </div>

                <div class="cctc-form-group" id="groupHoatDongKhuVuc" style="display: none;">
                    <label>
                        <input type="checkbox" id="hoatDongKhuVuc" name="hoatDong" value="1" checked>
                        Đang hoạt động
                    </label>
                </div>

                <div class="cctc-modal-footer">
                    <button type="button" class="btn-cctc btn-cctc-secondary" onclick="dongModal('modalKhuVuc')">Hủy</button>
                    <button type="submit" class="btn-cctc btn-cctc-primary">Lưu khu vực</button>
                </div>
            </form>
        </div>
    </div>

    <!-- ==================== MODAL CHUYỂN NHÓM NHÂN VIÊN (AC2) ==================== -->
    <div class="cctc-modal" id="modalChuyenNhom">
        <div class="cctc-modal-content">
            <div class="cctc-modal-header">
                <h3>Gán / Chuyển nhóm nhân viên</h3>
                <button type="button" class="btn-close" onclick="dongModal('modalChuyenNhom')">&times;</button>
            </div>
            <form action="${pageContext.request.contextPath}/co-cau-to-chuc" method="POST">
                <input type="hidden" name="action" value="chuyen-nhom-nhan-vien">

                <div class="cctc-form-group">
                    <label for="chuyenNguoiDungId">Nhân viên cần gán / chuyển *</label>
                    <select class="cctc-form-control" id="chuyenNguoiDungId" name="nguoiDungId" required>
                        <option value="">-- Chọn nhân viên --</option>
                        <c:forEach var="u" items="${dsTatCaNguoiDung}">
                            <option value="${u.id}">${u.hoTen} (${u.email})</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="cctc-form-group">
                    <label for="chuyenNhomId">Nhóm kinh doanh đích * (Mỗi nhân viên chỉ thuộc 1 nhóm - AC2)</label>
                    <select class="cctc-form-control" id="chuyenNhomId" name="nhomId" required>
                        <option value="">-- Chọn nhóm đích --</option>
                        <c:forEach var="n" items="${dsNhom}">
                            <option value="${n.idLong}">${n.maNhom} - ${n.tenNhom}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="cctc-modal-footer">
                    <button type="button" class="btn-cctc btn-cctc-secondary" onclick="dongModal('modalChuyenNhom')">Hủy</button>
                    <button type="submit" class="btn-cctc btn-cctc-primary">Xác nhận chuyển nhóm</button>
                </div>
            </form>
        </div>
    </div>

    <!-- ==================== MODAL XEM THÀNH VIÊN TRONG NHÓM ==================== -->
    <div class="cctc-modal" id="modalThanhVien">
        <div class="cctc-modal-content">
            <div class="cctc-modal-header">
                <h3 id="modalThanhVienTitle">Thành viên nhóm</h3>
                <button type="button" class="btn-close" onclick="dongModal('modalThanhVien')">&times;</button>
            </div>
            <div id="modalThanhVienContent" style="min-height: 100px;">
                Đang tải dữ liệu...
            </div>
            <div class="cctc-modal-footer">
                <button type="button" class="btn-cctc btn-cctc-secondary" onclick="dongModal('modalThanhVien')">Đóng</button>
            </div>
        </div>
    </div>

    <script>
        function dongModal(id) {
            document.getElementById(id).classList.remove('active');
        }

        function moModalThemNhom() {
            document.getElementById('modalNhomTitle').innerText = 'Thêm nhóm kinh doanh mới';
            document.getElementById('formNhomAction').value = 'them-nhom';
            document.getElementById('formNhomId').value = '';
            document.getElementById('maNhom').value = '';
            document.getElementById('maNhom').readOnly = false;
            document.getElementById('tenNhom').value = '';
            document.getElementById('nhomChaId').value = '';
            document.getElementById('khuVucId').value = '';
            document.getElementById('truongNhomId').value = '';
            document.getElementById('moTa').value = '';
            document.getElementById('groupHoatDongNhom').style.display = 'none';
            document.getElementById('modalNhom').classList.add('active');
        }

        function moModalSuaNhom(id, maNhom, tenNhom, moTa, nhomChaId, khuVucId, truongNhomId, hoatDong) {
            document.getElementById('modalNhomTitle').innerText = 'Chỉnh sửa nhóm kinh doanh';
            document.getElementById('formNhomAction').value = 'cap-nhat-nhom';
            document.getElementById('formNhomId').value = id;
            document.getElementById('maNhom').value = maNhom;
            document.getElementById('maNhom').readOnly = true;
            document.getElementById('tenNhom').value = tenNhom;
            document.getElementById('nhomChaId').value = nhomChaId || '';
            document.getElementById('khuVucId').value = khuVucId || '';
            document.getElementById('truongNhomId').value = truongNhomId || '';
            document.getElementById('moTa').value = moTa || '';
            document.getElementById('groupHoatDongNhom').style.display = 'block';
            document.getElementById('hoatDongNhom').checked = (hoatDong === true || hoatDong === 'true');
            document.getElementById('modalNhom').classList.add('active');
        }

        function moModalThemKhuVuc() {
            document.getElementById('modalKhuVucTitle').innerText = 'Khai báo khu vực địa lý mới';
            document.getElementById('formKhuVucAction').value = 'them-khu-vuc';
            document.getElementById('formKhuVucId').value = '';
            document.getElementById('maKhuVuc').value = '';
            document.getElementById('maKhuVuc').readOnly = false;
            document.getElementById('tenKhuVuc').value = '';
            document.getElementById('loaiKhuVuc').value = 'TINH_THANH';
            document.getElementById('khuVucChaId').value = '';
            document.getElementById('thuTuHienThi').value = '0';
            document.getElementById('groupHoatDongKhuVuc').style.display = 'none';
            document.getElementById('modalKhuVuc').classList.add('active');
        }

        function moModalSuaKhuVuc(id, maKhuVuc, tenKhuVuc, loaiKhuVuc, khuVucChaId, thuTu, hoatDong) {
            document.getElementById('modalKhuVucTitle').innerText = 'Chỉnh sửa khu vực địa lý';
            document.getElementById('formKhuVucAction').value = 'cap-nhat-khu-vuc';
            document.getElementById('formKhuVucId').value = id;
            document.getElementById('maKhuVuc').value = maKhuVuc;
            document.getElementById('maKhuVuc').readOnly = true;
            document.getElementById('tenKhuVuc').value = tenKhuVuc;
            document.getElementById('loaiKhuVuc').value = loaiKhuVuc || 'TINH_THANH';
            document.getElementById('khuVucChaId').value = khuVucChaId || '';
            document.getElementById('thuTuHienThi').value = thuTu || 0;
            document.getElementById('groupHoatDongKhuVuc').style.display = 'block';
            document.getElementById('hoatDongKhuVuc').checked = (hoatDong === true || hoatDong === 'true');
            document.getElementById('modalKhuVuc').classList.add('active');
        }

        function moModalChuyenNhom() {
            document.getElementById('chuyenNguoiDungId').value = '';
            document.getElementById('chuyenNhomId').value = '';
            document.getElementById('modalChuyenNhom').classList.add('active');
        }

        function moModalChuyenNhomChoNhanVien(userId, userName) {
            document.getElementById('chuyenNguoiDungId').value = userId;
            document.getElementById('chuyenNhomId').value = '';
            document.getElementById('modalChuyenNhom').classList.add('active');
        }

        function xemThanhVien(nhomId, tenNhom) {
            document.getElementById('modalThanhVienTitle').innerText = 'Danh sách thành viên: ' + tenNhom;
            const container = document.getElementById('modalThanhVienContent');
            container.innerHTML = '<p style="color: #64748b; text-align: center;">Đang tải danh sách thành viên...</p>';
            document.getElementById('modalThanhVien').classList.add('active');

            fetch('${pageContext.request.contextPath}/co-cau-to-chuc/api/nhom?id=' + nhomId)
                .then(res => res.json())
                .then(data => {
                    if (data.error) {
                        container.innerHTML = '<p style="color: #ef4444;">' + data.error + '</p>';
                        return;
                    }
                    if (!data.thanhVien || data.thanhVien.length === 0) {
                        container.innerHTML = '<p style="color: #94a3b8; text-align: center; padding: 20px;">Nhóm hiện chưa có thành viên nào.</p>';
                        return;
                    }

                    let html = '<table class="cctc-table"><thead><tr><th>Họ tên</th><th>Email</th><th>Số điện thoại</th></tr></thead><tbody>';
                    data.thanhVien.forEach(tv => {
                        html += '<tr><td><strong>' + escapeHtml(tv.hoTen) + '</strong></td><td>' + escapeHtml(tv.email) + '</td><td>' + (tv.soDienThoai ? escapeHtml(tv.soDienThoai) : '-') + '</td></tr>';
                    });
                    html += '</tbody></table>';
                    container.innerHTML = html;
                })
                .catch(err => {
                    container.innerHTML = '<p style="color: #ef4444;">Không thể tải dữ liệu: ' + err + '</p>';
                });
        }

        function escapeHtml(text) {
            if (!text) return '';
            return text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
        }
    </script>
</body>
</html>
