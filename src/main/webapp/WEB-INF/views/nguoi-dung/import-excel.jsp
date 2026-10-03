<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nhập danh sách người dùng từ Excel - CRM Bán Hàng</title>
    <meta name="description" content="Nhập danh sách người dùng hàng loạt từ tệp Excel, xem trước lỗi từng dòng và xuất báo cáo tổng kết.">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/nguoi-dung/nguoi-dung.css">
    <style>
        .import-container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 24px;
        }

        .card-import {
            background: #ffffff;
            border-radius: 8px;
            border: 1px solid #DEE2E6;
            padding: 20px;
            margin-bottom: 24px;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
        }

        .card-header-custom {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 16px;
            padding-bottom: 12px;
            border-bottom: 1px solid #ECEFF1;
        }

        .card-title-custom {
            font-size: 18px;
            font-weight: 700;
            color: #1B365D;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .upload-dropzone {
            border: 2px dashed #90CAF9;
            background-color: #F8FAFC;
            border-radius: 8px;
            padding: 28px;
            text-align: center;
            margin-bottom: 16px;
            transition: all 0.2s ease;
        }

        .upload-dropzone:hover {
            border-color: #1976D2;
            background-color: #F0F7FF;
        }

        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 16px;
            margin-bottom: 20px;
        }

        .stat-card {
            background: #ffffff;
            border: 1px solid #E0E0E0;
            border-radius: 8px;
            padding: 16px;
            text-align: center;
        }

        .stat-card.total { border-top: 4px solid #1B365D; }
        .stat-card.valid { border-top: 4px solid #2E7D32; }
        .stat-card.error { border-top: 4px solid #C62828; }
        .stat-card.imported { border-top: 4px solid #0288D1; }

        .stat-num {
            font-size: 26px;
            font-weight: 800;
            line-height: 1.2;
            margin-top: 4px;
        }

        .stat-label {
            font-size: 13px;
            color: #616161;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .table-responsive {
            overflow-x: auto;
            border: 1px solid #E0E0E0;
            border-radius: 6px;
        }

        .table-preview {
            width: 100%;
            border-collapse: collapse;
            font-size: 14px;
        }

        .table-preview th {
            background-color: #F5F7FA;
            color: #37474F;
            font-weight: 700;
            padding: 12px;
            text-align: left;
            border-bottom: 2px solid #CFD8DC;
            white-space: nowrap;
        }

        .table-preview td {
            padding: 10px 12px;
            border-bottom: 1px solid #ECEFF1;
            vertical-align: top;
        }

        .table-preview tr:hover {
            background-color: #FAFAFA;
        }

        .badge-status {
            display: inline-block;
            padding: 4px 10px;
            border-radius: 12px;
            font-size: 12px;
            font-weight: 700;
            white-space: nowrap;
        }

        .badge-valid { background-color: #E8F5E9; color: #2E7D32; }
        .badge-error { background-color: #FFEBEE; color: #C62828; }
        .badge-imported { background-color: #E1F5FE; color: #0288D1; }
        .badge-skipped { background-color: #FFF3E0; color: #EF6C00; }

        .text-error-list {
            margin: 0;
            padding-left: 18px;
            color: #C62828;
            font-size: 13px;
        }

        .btn-group-actions {
            display: flex;
            gap: 12px;
            flex-wrap: wrap;
            align-items: center;
        }

        .credential-box {
            font-family: monospace;
            background: #ECEFF1;
            padding: 2px 6px;
            border-radius: 4px;
            font-size: 13px;
            color: #263238;
        }
    </style>
</head>
<body class="crm-body">

    <!-- Thanh điều hướng và Sidebar chuẩn hệ thống S1-06 -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content" id="crm-main-content">
        <div class="import-container">

            <!-- HEADER -->
            <div class="page-header" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
                <div>
                    <h1 style="font-size: 24px; color: #1B365D; margin-bottom: 4px;">&#128203; Nhập danh sách người dùng từ tệp Excel</h1>
                    <p style="font-size: 14px; color: #546E7A;">Tạo tài khoản hàng loạt cho khối kinh doanh, xem trước lỗi từng dòng và tự động bỏ qua dòng lỗi.</p>
                </div>
                <div>
                    <a href="${pageContext.request.contextPath}/nguoi-dung" class="btn btn-secondary">
                        &larr; Về danh sách người dùng
                    </a>
                </div>
            </div>

            <!-- THÔNG BÁO LỖI HỆ THỐNG -->
            <c:if test="${not empty thongBaoLoi}">
                <div class="alert alert-danger" style="background-color: #FFEBEE; color: #C62828; padding: 14px; border-radius: 6px; margin-bottom: 20px; border: 1px solid #FFCDD2;">
                    <strong>&#9888; Thông báo:</strong> <c:out value="${thongBaoLoi}" />
                </div>
            </c:if>

            <!-- CARD 1: HƯỚNG DẪN & TẢI TỆP MẪU -->
            <div class="card-import">
                <div class="card-header-custom">
                    <div class="card-title-custom">&#128229; Bước 1: Chuẩn bị dữ liệu theo tệp mẫu chuẩn</div>
                    <a id="btn-tai-tep-mau" href="${pageContext.request.contextPath}/nguoi-dung/tai-tep-mau" class="btn btn-outline-primary" style="display: inline-flex; align-items: center; gap: 6px; padding: 8px 16px; border: 1px solid #1B365D; color: #1B365D; border-radius: 6px; text-decoration: none; font-weight: 600;">
                        &#11015; Tải tệp mẫu Excel (.xlsx)
                    </a>
                </div>
                <div style="font-size: 14px; color: #455A64; line-height: 1.6;">
                    <p><strong>Quy tắc nhập dữ liệu:</strong></p>
                    <ul style="padding-left: 20px; margin-top: 6px;">
                        <li>Các trường bắt buộc: <strong>Họ và tên</strong>, <strong>Email công ty</strong> và <strong>Mã vai trò</strong>.</li>
                        <li>Mã vai trò hợp lệ: <code>ADMIN</code>, <code>DIRECTOR</code>, <code>TEAM_LEAD</code>, <code>SALES_REP</code>, <code>MARKETING</code>, <code>CUST_SUCCESS</code>, <code>ACCOUNTANT</code>.</li>
                        <li>Người giữ vai trò Trưởng nhóm (<code>TEAM_LEAD</code>) bắt buộc phải gán nhóm kinh doanh cụ thể.</li>
                        <li>Nếu cột mật khẩu để trống, hệ thống sẽ tự sinh mật khẩu tạm mạnh ngẫu nhiên và gửi thông tin trong báo cáo tổng kết.</li>
                    </ul>
                </div>
            </div>

            <!-- CARD 2: TẢI TỆP LÊN & THAO TÁC -->
            <div class="card-import">
                <div class="card-header-custom">
                    <div class="card-title-custom">&#128228; Bước 2: Tải tệp Excel lên hệ thống</div>
                </div>

                <form id="form-import-excel" action="${pageContext.request.contextPath}/nguoi-dung/import" method="post" enctype="multipart/form-data">
                    <div class="upload-dropzone">
                        <input type="file" id="fileExcel" name="fileExcel" accept=".xlsx, .xls" required style="font-size: 15px; margin-bottom: 8px;" />
                        <div style="font-size: 13px; color: #78909C; margin-top: 6px;">
                            Hỗ trợ định dạng Excel: <strong>.xlsx</strong> hoặc <strong>.xls</strong> (Dung lượng tối đa 10MB)
                        </div>
                    </div>

                    <div class="btn-group-actions">
                        <button type="submit" name="action" value="xem-truoc" id="btn-xem-truoc" class="btn btn-primary" style="background-color: #1B365D; color: #fff; padding: 10px 20px; border-radius: 6px; border: none; font-weight: 600; cursor: pointer;">
                            &#128065; Xem trước và kiểm tra lỗi
                        </button>

                        <button type="submit" name="action" value="nhap-du-lieu" id="btn-nhap-du-lieu" class="btn btn-success" style="background-color: #2E7D32; color: #fff; padding: 10px 20px; border-radius: 6px; border: none; font-weight: 600; cursor: pointer;"
                                onclick="return confirm('Hệ thống sẽ nhập các dòng hợp lệ và tự động bỏ qua các dòng bị lỗi. Bạn có chắc chắn muốn tiến hành?');">
                            &#9989; Tiến hành nhập dữ liệu
                        </button>
                    </div>
                </form>
            </div>

            <!-- CARD 3: BÁO CÁO XEM TRƯỚC / KẾT QUẢ TỔNG KẾT -->
            <c:if test="${not empty baoCao}">
                <div class="card-import">
                    <div class="card-header-custom">
                        <div class="card-title-custom">
                            <c:choose>
                                <c:when test="${cheDo == 'ket-qua'}">
                                    &#128202; Báo cáo tổng kết đợt nhập người dùng: <c:out value="${tenTep}" />
                                </c:when>
                                <c:otherwise>
                                    &#128065; Kết quả xem trước và thẩm định tệp: <c:out value="${tenTep}" />
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <!-- THỐNG KÊ TỔNG KẾT -->
                    <div class="stats-grid">
                        <div class="stat-card total">
                            <div class="stat-label">Tổng số dòng</div>
                            <div class="stat-num" style="color: #1B365D;"><c:out value="${baoCao.tongSoDong}" /></div>
                        </div>
                        <div class="stat-card valid">
                            <div class="stat-label">Dòng hợp lệ</div>
                            <div class="stat-num" style="color: #2E7D32;"><c:out value="${baoCao.soDongHopLe}" /></div>
                        </div>
                        <div class="stat-card error">
                            <div class="stat-label">Dòng có lỗi</div>
                            <div class="stat-num" style="color: #C62828;"><c:out value="${baoCao.soDongLoi}" /></div>
                        </div>
                        <c:if test="${cheDo == 'ket-qua'}">
                            <div class="stat-card imported">
                                <div class="stat-label">Đã nhập thành công</div>
                                <div class="stat-num" style="color: #0288D1;"><c:out value="${baoCao.soDongThanhCong}" /></div>
                            </div>
                        </c:if>
                    </div>

                    <!-- THÔNG ĐIỆP BÁO CÁO -->
                    <div class="alert" style="background-color: #E3F2FD; color: #0D47A1; padding: 12px 16px; border-radius: 6px; margin-bottom: 20px; border-left: 4px solid #1976D2;">
                        <strong>&#8505; Tóm tắt:</strong> <c:out value="${baoCao.thongDiep}" />
                    </div>

                    <!-- BẢNG CHI TIẾT TỪNG DÒNG -->
                    <div class="table-responsive">
                        <table class="table-preview" id="bang-ket-qua-excel">
                            <thead>
                                <tr>
                                    <th style="width: 60px; text-align: center;">Dòng</th>
                                    <th>Họ và tên</th>
                                    <th>Email</th>
                                    <th>Số điện thoại</th>
                                    <th>Vai trò</th>
                                    <th>Nhóm kinh doanh</th>
                                    <th style="width: 130px; text-align: center;">Trạng thái</th>
                                    <th>Chi tiết lỗi / Mật khẩu tạm</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="dong" items="${baoCao.danhSachTatCaDong}">
                                    <tr>
                                        <td style="text-align: center; font-weight: 600; color: #546E7A;"><c:out value="${dong.soDong}" /></td>
                                        <td style="font-weight: 600;"><c:out value="${dong.hoTen}" /></td>
                                        <td><code><c:out value="${dong.email}" /></code></td>
                                        <td><c:out value="${dong.soDienThoai}" /></td>
                                        <td><c:out value="${dong.chuoiVaiTroHienThi}" /></td>
                                        <td><c:out value="${dong.tenNhomGiaiQuyet}" /></td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${cheDo == 'ket-qua'}">
                                                    <c:choose>
                                                        <c:when test="${dong.daNhap}">
                                                            <span class="badge-status badge-imported">&#10003; Đã nhập (ID: ${dong.idNguoiDung})</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge-status badge-skipped">&#10007; Bị bỏ qua</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:choose>
                                                        <c:when test="${dong.hopLe}">
                                                            <span class="badge-status badge-valid">&#10003; Hợp lệ</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge-status badge-error">&#9888; Lỗi</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty dong.danhSachLoi}">
                                                    <ul class="text-error-list">
                                                        <c:forEach var="loi" items="${dong.danhSachLoi}">
                                                            <li><c:out value="${loi}" /></li>
                                                        </c:forEach>
                                                    </ul>
                                                </c:when>
                                                <c:when test="${dong.daNhap and not empty dong.matKhauTam}">
                                                    <span style="font-size: 13px; color: #2E7D32;">Mật khẩu:</span>
                                                    <span class="credential-box"><c:out value="${dong.matKhauTam}" /></span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="color: #689F38; font-size: 13px;">Dữ liệu sẵn sàng</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>

                </div>
            </c:if>

        </div>
    </main>

</body>
</html>
