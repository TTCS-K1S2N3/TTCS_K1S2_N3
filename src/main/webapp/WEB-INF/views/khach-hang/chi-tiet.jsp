<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Chi tiết khách hàng - Hệ thống CRM Bán Hàng">
    <title>Chi Tiết Khách Hàng - CRM Bán Hàng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/khach-hang/khach-hang.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <style>
        .breadcrumb-nav {
            margin-bottom: 20px;
        }
        .breadcrumb-list {
            display: flex;
            align-items: center;
            list-style: none;
            gap: 8px;
            font-size: 13.5px;
            color: var(--slate-500);
            flex-wrap: wrap;
        }
        .breadcrumb-item a {
            color: var(--primary);
            text-decoration: none;
            font-weight: 500;
        }
        .breadcrumb-item a:hover {
            text-decoration: underline;
        }
        .breadcrumb-separator {
            color: var(--slate-400);
        }
        .breadcrumb-item.active {
            color: var(--slate-700);
            font-weight: 600;
        }
        .detail-card {
            background: #ffffff;
            border-radius: var(--radius-lg);
            border: 1px solid var(--border-color);
            box-shadow: var(--shadow-sm);
            overflow: hidden;
            margin-bottom: 24px;
        }
        .detail-header {
            padding: 20px 24px;
            border-bottom: 1px solid var(--border-color);
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 16px;
            background-color: #ffffff;
        }
        .detail-title-group h1 {
            font-size: 20px;
            font-weight: 800;
            color: var(--slate-900);
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .detail-title-group p {
            font-size: 13px;
            color: var(--slate-500);
            margin-top: 4px;
        }
        .detail-body {
            padding: 24px;
        }
        .detail-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
            gap: 20px;
            margin-bottom: 24px;
        }
        .detail-item {
            display: flex;
            flex-direction: column;
            gap: 6px;
        }
        .detail-label {
            font-size: 12.5px;
            font-weight: 600;
            color: var(--slate-500);
            text-transform: uppercase;
            letter-spacing: 0.03em;
        }
        .detail-value {
            font-size: 15px;
            color: var(--slate-900);
            font-weight: 500;
            word-break: break-word;
        }
        .detail-value.font-mono {
            font-family: monospace;
            font-size: 14.5px;
            color: var(--slate-700);
        }
        .detail-desc-box {
            background-color: var(--slate-50);
            border: 1px solid var(--slate-200);
            border-radius: var(--radius-md);
            padding: 16px;
            color: var(--slate-700);
            font-size: 14px;
            line-height: 1.6;
            white-space: pre-wrap;
            min-height: 80px;
        }
        .detail-footer {
            padding: 16px 24px;
            background-color: var(--slate-50);
            border-top: 1px solid var(--border-color);
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 12px;
        }
        @media (max-width: 640px) {
            .detail-header, .detail-body, .detail-footer {
                padding: 16px;
            }
            .detail-title-group h1 {
                font-size: 18px;
            }
        }
    </style>
</head>
<body class="crm-body">
    <!-- Thanh điều hướng và Sidebar chuẩn hệ thống S1-06 -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <!-- Breadcrumb -->
        <nav class="breadcrumb-nav" aria-label="Breadcrumb">
            <ol class="breadcrumb-list">
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/">Trang chủ</a></li>
                <span class="breadcrumb-separator">/</span>
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/khach-hang">Khách hàng</a></li>
                <span class="breadcrumb-separator">/</span>
                <li class="breadcrumb-item active" aria-current="page"><c:out value="${not empty banGhi.tieuDe ? banGhi.tieuDe : 'Chi tiết'}" /></li>
            </ol>
        </nav>

        <!-- Thông báo thành công / trạng thái -->
        <c:if test="${not empty thongBaoThanhCong}">
            <div class="alert alert-success" style="margin-bottom: 20px;">
                <span class="material-symbols-outlined" aria-hidden="true">check_circle</span>
                <span><c:out value="${thongBaoThanhCong}" /></span>
            </div>
        </c:if>

        <!-- Thẻ chi tiết bản ghi (Read-Only) -->
        <div class="detail-card">
            <div class="detail-header">
                <div class="detail-title-group">
                    <h1>
                        <span class="material-symbols-outlined" aria-hidden="true" style="color: var(--primary);">person</span>
                        <span><c:out value="${banGhi.tieuDe}" /></span>
                    </h1>
                    <p>Mã bản ghi: <span class="font-mono"><c:out value="${banGhi.maBanGhi}" /></span> • Bản ghi nằm trong phạm vi dữ liệu được phép truy cập</p>
                </div>
                <div style="display: flex; align-items: center; gap: 10px; flex-wrap: wrap;">
                    <span class="badge badge-success" style="font-size: 13px; padding: 6px 12px;">
                        <c:out value="${not empty banGhi.trangThai ? banGhi.trangThai : 'Hoạt động'}" />
                    </span>
                    <a href="${pageContext.request.contextPath}/khach-hang" class="btn btn-outline" id="btnBackKhachHang">
                        <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span>
                        <span>Quay lại danh sách khách hàng</span>
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
                        <span class="detail-label">Phân loại / Doanh thu ước tính</span>
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
                        <span class="detail-label">Loại nghiệp vụ</span>
                        <span class="detail-value"><c:out value="${not empty banGhi.loaiNghiepVu ? banGhi.loaiNghiepVu.tenHienThi : 'Khách hàng'}" /></span>
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

            <div class="detail-footer">
                <span style="font-size: 13px; color: var(--slate-500);">
                    Chế độ: <strong>Xem thông tin (Read-Only)</strong> • Quyền sở hữu dữ liệu được kiểm soát tự động theo Data Scope.
                </span>
                <a href="${pageContext.request.contextPath}/khach-hang" class="btn btn-outline">
                    <span class="material-symbols-outlined" aria-hidden="true">arrow_back</span> Quay lại danh sách khách hàng
                </a>
            </div>
        </div>
    </main>
</body>
</html>
