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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/co-cau-to-chuc/index.css">
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
                                                        <div class="tree-indent-box tree-level-${nhom.capDo}">
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
                                                        <a href="javascript:void(0)"
                                                           data-id="${nhom.idLong}"
                                                           data-ten="${fn:escapeXml(nhom.tenNhom)}"
                                                           onclick="xemThanhVien(this)"
                                                           style="color: #2563eb; text-decoration: none; font-weight: 600;">
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
                                                                data-id="${nhom.idLong}"
                                                                data-ma-nhom="${fn:escapeXml(nhom.maNhom)}"
                                                                data-ten-nhom="${fn:escapeXml(nhom.tenNhom)}"
                                                                data-mo-ta="${fn:escapeXml(nhom.moTa)}"
                                                                data-nhom-cha-id="${nhom.nhomChaIdLong}"
                                                                data-khu-vuc-id="${nhom.khuVucId}"
                                                                data-truong-nhom-id="${nhom.truongNhomId}"
                                                                data-hoat-dong="${nhom.hoatDong}"
                                                                onclick="moModalSuaNhom(this)">
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
                                                                data-id="${kv.id}"
                                                                data-ma-khu-vuc="${fn:escapeXml(kv.maKhuVuc)}"
                                                                data-ten-khu-vuc="${fn:escapeXml(kv.tenKhuVuc)}"
                                                                data-loai-khu-vuc="${fn:escapeXml(kv.loaiKhuVuc)}"
                                                                data-khu-vuc-cha-id="${kv.khuVucChaId}"
                                                                data-thu-tu="${kv.thuTuHienThi}"
                                                                data-hoat-dong="${kv.hoatDong}"
                                                                onclick="moModalSuaKhuVuc(this)">
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
                                                                data-id="${nv.id}"
                                                                data-ho-ten="${fn:escapeXml(nv.hoTen)}"
                                                                onclick="moModalChuyenNhomChoNhanVien(this)">
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

        function moModalSuaNhom(elementOrId, maNhom, tenNhom, moTa, nhomChaId, khuVucId, truongNhomId, hoatDong) {
            let id = elementOrId;
            if (elementOrId && typeof elementOrId === 'object' && elementOrId.getAttribute) {
                id = elementOrId.getAttribute('data-id');
                maNhom = elementOrId.getAttribute('data-ma-nhom');
                tenNhom = elementOrId.getAttribute('data-ten-nhom');
                moTa = elementOrId.getAttribute('data-mo-ta');
                nhomChaId = elementOrId.getAttribute('data-nhom-cha-id');
                khuVucId = elementOrId.getAttribute('data-khu-vuc-id');
                truongNhomId = elementOrId.getAttribute('data-truong-nhom-id');
                hoatDong = elementOrId.getAttribute('data-hoat-dong');
            }

            document.getElementById('modalNhomTitle').innerText = 'Chỉnh sửa nhóm kinh doanh';
            document.getElementById('formNhomAction').value = 'cap-nhat-nhom';
            document.getElementById('formNhomId').value = id || '';
            document.getElementById('maNhom').value = maNhom || '';
            document.getElementById('maNhom').readOnly = true;
            document.getElementById('tenNhom').value = tenNhom || '';
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

        function moModalSuaKhuVuc(elementOrId, maKhuVuc, tenKhuVuc, loaiKhuVuc, khuVucChaId, thuTu, hoatDong) {
            let id = elementOrId;
            if (elementOrId && typeof elementOrId === 'object' && elementOrId.getAttribute) {
                id = elementOrId.getAttribute('data-id');
                maKhuVuc = elementOrId.getAttribute('data-ma-khu-vuc');
                tenKhuVuc = elementOrId.getAttribute('data-ten-khu-vuc');
                loaiKhuVuc = elementOrId.getAttribute('data-loai-khu-vuc');
                khuVucChaId = elementOrId.getAttribute('data-khu-vuc-cha-id');
                thuTu = elementOrId.getAttribute('data-thu-tu');
                hoatDong = elementOrId.getAttribute('data-hoat-dong');
            }

            document.getElementById('modalKhuVucTitle').innerText = 'Chỉnh sửa khu vực địa lý';
            document.getElementById('formKhuVucAction').value = 'cap-nhat-khu-vuc';
            document.getElementById('formKhuVucId').value = id || '';
            document.getElementById('maKhuVuc').value = maKhuVuc || '';
            document.getElementById('maKhuVuc').readOnly = true;
            document.getElementById('tenKhuVuc').value = tenKhuVuc || '';
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

        function moModalChuyenNhomChoNhanVien(elementOrId, userName) {
            let userId = elementOrId;
            if (elementOrId && typeof elementOrId === 'object' && elementOrId.getAttribute) {
                userId = elementOrId.getAttribute('data-id');
            }
            document.getElementById('chuyenNguoiDungId').value = userId || '';
            document.getElementById('chuyenNhomId').value = '';
            document.getElementById('modalChuyenNhom').classList.add('active');
        }

        function xemThanhVien(elementOrId, tenNhom) {
            let nhomId = elementOrId;
            if (elementOrId && typeof elementOrId === 'object' && elementOrId.getAttribute) {
                nhomId = elementOrId.getAttribute('data-id');
                tenNhom = elementOrId.getAttribute('data-ten');
            }

            document.getElementById('modalThanhVienTitle').innerText = 'Danh sách thành viên: ' + (tenNhom || '');
            const container = document.getElementById('modalThanhVienContent');
            container.innerHTML = '<p style="color: #64748b; text-align: center;">Đang tải danh sách thành viên...</p>';
            document.getElementById('modalThanhVien').classList.add('active');

            fetch('${pageContext.request.contextPath}/co-cau-to-chuc/api/nhom?id=' + encodeURIComponent(nhomId))
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
