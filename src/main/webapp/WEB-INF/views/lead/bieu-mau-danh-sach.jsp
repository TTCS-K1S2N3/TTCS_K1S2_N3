<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Quản lý biểu mẫu nhúng thu thập lead từ website - CRM Bán Hàng">
    <title>Biểu Mẫu Thu Thập Lead - CRM Bán Hàng</title>
    <!-- CSS dùng chung toàn hệ thống -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" />
    <style>
        .bml-container {
            padding: 24px 32px;
            max-width: 1400px;
            margin: 0 auto;
        }
        .bml-breadcrumb {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 20px;
        }
        .bml-breadcrumb-nav {
            display: flex;
            align-items: center;
            gap: 8px;
            list-style: none;
            padding: 0;
            margin: 0;
            font-size: 0.875rem;
        }
        .bml-breadcrumb-nav a {
            color: var(--color-primary, #1e40af);
            text-decoration: none;
        }
        .bml-breadcrumb-sep {
            color: #9ca3af;
        }
        .bml-header-card {
            background: #ffffff;
            border: 1px solid #e5e7eb;
            border-radius: 8px;
            padding: 20px 24px;
            margin-bottom: 24px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
        }
        .bml-header-title h1 {
            font-size: 1.375rem;
            font-weight: 700;
            color: #111827;
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 6px;
        }
        .bml-header-title p {
            font-size: 0.875rem;
            color: #6b7280;
            margin: 0;
        }
        .bml-btn {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 8px 16px;
            font-size: 0.875rem;
            font-weight: 600;
            border-radius: 6px;
            border: 1px solid transparent;
            cursor: pointer;
            text-decoration: none;
            transition: all 0.2s;
        }
        .bml-btn-primary {
            background-color: #1e40af;
            color: #ffffff;
        }
        .bml-btn-primary:hover {
            background-color: #1d4ed8;
        }
        .bml-btn-outline {
            background-color: #ffffff;
            border-color: #d1d5db;
            color: #374151;
        }
        .bml-btn-outline:hover {
            background-color: #f9fafb;
            border-color: #9ca3af;
        }
        .bml-btn-sm {
            padding: 4px 10px;
            font-size: 0.8125rem;
        }
        .bml-table-card {
            background: #ffffff;
            border: 1px solid #e5e7eb;
            border-radius: 8px;
            overflow: hidden;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
        }
        .bml-table {
            width: 100%;
            border-collapse: collapse;
            text-align: left;
            font-size: 0.875rem;
        }
        .bml-table th {
            background-color: #f9fafb;
            color: #4b5563;
            font-weight: 600;
            padding: 12px 16px;
            border-bottom: 1px solid #e5e7eb;
        }
        .bml-table td {
            padding: 14px 16px;
            border-bottom: 1px solid #f3f4f6;
            vertical-align: middle;
        }
        .bml-table tr:hover td {
            background-color: #f9fafb;
        }
        .bml-badge {
            display: inline-block;
            padding: 3px 8px;
            font-size: 0.75rem;
            font-weight: 600;
            border-radius: 4px;
        }
        .bml-badge-success {
            background-color: #dcfce7;
            color: #15803d;
        }
        .bml-badge-secondary {
            background-color: #f3f4f6;
            color: #4b5563;
        }
        .bml-alert {
            padding: 12px 16px;
            border-radius: 6px;
            font-size: 0.875rem;
            margin-bottom: 16px;
        }
        .bml-alert-success {
            background-color: #dcfce7;
            color: #15803d;
            border: 1px solid #bbf7d0;
        }

        /* Modal hiển thị mã nhúng */
        .bml-modal-backdrop {
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(0, 0, 0, 0.5);
            display: none;
            align-items: center;
            justify-content: center;
            z-index: 1000;
        }
        .bml-modal {
            background: #ffffff;
            border-radius: 8px;
            max-width: 650px;
            width: 90%;
            padding: 24px;
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
        }
        .bml-modal-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 16px;
        }
        .bml-modal-header h3 {
            font-size: 1.125rem;
            font-weight: 700;
            margin: 0;
        }
        .bml-code-box {
            background: #f8fafc;
            border: 1px solid #cbd5e1;
            border-radius: 6px;
            padding: 12px;
            font-family: monospace;
            font-size: 0.8125rem;
            white-space: pre-wrap;
            word-break: break-all;
            margin-bottom: 8px;
            max-height: 140px;
            overflow-y: auto;
        }
    </style>
</head>
<body class="crm-body">
    <!-- Navigation layout dùng chung -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <div class="bml-container">
            <!-- Breadcrumb -->
            <nav class="bml-breadcrumb" aria-label="Breadcrumb">
                <ol class="bml-breadcrumb-nav">
                    <li><a href="${pageContext.request.contextPath}/dieu-huong">Trang chủ</a></li>
                    <span class="bml-breadcrumb-sep">/</span>
                    <li><a href="${pageContext.request.contextPath}/lead">Lead & Phân bổ</a></li>
                    <span class="bml-breadcrumb-sep">/</span>
                    <li aria-current="page" style="color: #374151; font-weight: 500;">Biểu mẫu thu thập Lead</li>
                </ol>
            </nav>

            <!-- Header Card -->
            <header class="bml-header-card">
                <div class="bml-header-title">
                    <h1>
                        <span class="material-symbols-outlined" style="color: #1e40af;" aria-hidden="true">dynamic_form</span>
                        <span>Biểu Mẫu Nhúng Thu Thập Lead Website</span>
                    </h1>
                    <p>Sinh mã nhúng dán vào landing page/website bất kỳ, tự động thu thập lead mới và gắn đúng nguồn (Story S4-01).</p>
                </div>
                <div>
                    <a href="${pageContext.request.contextPath}/bieu-mau/tao" class="bml-btn bml-btn-primary" id="btnTaoBieuMau">
                        <span class="material-symbols-outlined" aria-hidden="true">add</span>
                        <span>Tạo biểu mẫu mới</span>
                    </a>
                </div>
            </header>

            <!-- Alert -->
            <c:if test="${not empty sessionScope.thongBaoThanhCong}">
                <div class="bml-alert bml-alert-success">
                    <c:out value="${sessionScope.thongBaoThanhCong}" />
                </div>
                <c:remove var="thongBaoThanhCong" scope="session" />
            </c:if>

            <!-- Table Card -->
            <div class="bml-table-card">
                <table class="bml-table">
                    <thead>
                        <tr>
                            <th>Mã Biểu Mẫu</th>
                            <th>Tên Biểu Mẫu</th>
                            <th>Nguồn Lead</th>
                            <th>Chiến Dịch</th>
                            <th>Giới Hạn / Giờ</th>
                            <th>Chống Spam</th>
                            <th>Trạng Thái</th>
                            <th style="text-align: right;">Thao Tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty dsBieuMau}">
                                <tr>
                                    <td colspan="8" style="text-align: center; padding: 40px; color: #6b7280;">
                                        Chưa có biểu mẫu nào được tạo. Nhấn "Tạo biểu mẫu mới" để bắt đầu thu thập lead!
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach items="${dsBieuMau}" var="bm">
                                    <tr>
                                        <td>
                                            <strong style="color: #1e40af;"><c:out value="${bm.maBieuMau}" /></strong>
                                        </td>
                                        <td>
                                            <c:out value="${bm.tenBieuMau}" />
                                        </td>
                                        <td>
                                            <span class="bml-badge bml-badge-secondary">
                                                <c:out value="${bm.tenNguonLead != null ? bm.tenNguonLead : 'Chưa gắn'}" />
                                            </span>
                                        </td>
                                        <td>
                                            <c:out value="${bm.tenChienDich != null ? bm.tenChienDich : '-'}" />
                                        </td>
                                        <td>
                                            <c:out value="${bm.gioiHanGuiMoiGio}" /> lượt/IP
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${bm.batHoneypot}">
                                                    <span style="color: #15803d; font-weight: 500;">✓ Honeypot</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="color: #6b7280;">Tắt</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${bm.hoatDong}">
                                                    <span class="bml-badge bml-badge-success">Đang hoạt động</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="bml-badge bml-badge-secondary">Tạm dừng</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: right;">
                                            <button type="button" class="bml-btn bml-btn-outline bml-btn-sm"
                                                    onclick="hienThiMaNhung(${bm.id})" title="Lấy mã nhúng">
                                                <span class="material-symbols-outlined" style="font-size: 16px;">code</span>
                                                <span>Mã nhúng</span>
                                            </button>
                                            <a href="${pageContext.request.contextPath}/bieu-mau/nhung?token=${bm.tokenCongKhai}"
                                               target="_blank" class="bml-btn bml-btn-outline bml-btn-sm" title="Xem thử form">
                                                <span class="material-symbols-outlined" style="font-size: 16px;">open_in_new</span>
                                                <span>Xem thử</span>
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </main>

    <!-- Modal hiển thị mã nhúng -->
    <div id="modalMaNhung" class="bml-modal-backdrop">
        <div class="bml-modal">
            <div class="bml-modal-header">
                <h3>Mã Nhúng Biểu Mẫu Website (AC1)</h3>
                <button type="button" onclick="dongModalMaNhung()" style="border:none;background:none;cursor:pointer;font-size:20px;">✕</button>
            </div>
            <div>
                <p style="font-size: 0.875rem; color: #4b5563; margin-bottom: 12px;">
                    Sao chép một trong các mã bên dưới và dán vào bất kỳ website, blog hoặc landing page nào để hiển thị biểu mẫu thu thập lead:
                </p>

                <!-- Iframe Code -->
                <div style="margin-bottom: 16px;">
                    <label style="font-size: 0.8125rem; font-weight: 600; color: #374151;">1. Mã nhúng Iframe (Khuyến nghị cho mọi website):</label>
                    <div id="codeIframe" class="bml-code-box"></div>
                    <button type="button" class="bml-btn bml-btn-outline bml-btn-sm" onclick="saoChep('codeIframe')">
                        <span class="material-symbols-outlined" style="font-size: 16px;">content_copy</span>
                        <span>Sao chép mã Iframe</span>
                    </button>
                </div>

                <!-- Script Code -->
                <div style="margin-bottom: 16px;">
                    <label style="font-size: 0.8125rem; font-weight: 600; color: #374151;">2. Mã nhúng Script JS:</label>
                    <div id="codeScript" class="bml-code-box"></div>
                    <button type="button" class="bml-btn bml-btn-outline bml-btn-sm" onclick="saoChep('codeScript')">
                        <span class="material-symbols-outlined" style="font-size: 16px;">content_copy</span>
                        <span>Sao chép mã Script</span>
                    </button>
                </div>

                <!-- Direct Link -->
                <div>
                    <label style="font-size: 0.8125rem; font-weight: 600; color: #374151;">3. Đường dẫn trực tiếp:</label>
                    <div id="codeUrl" class="bml-code-box"></div>
                    <button type="button" class="bml-btn bml-btn-outline bml-btn-sm" onclick="saoChep('codeUrl')">
                        <span class="material-symbols-outlined" style="font-size: 16px;">content_copy</span>
                        <span>Sao chép đường dẫn</span>
                    </button>
                </div>
            </div>
        </div>
    </div>

    <script>
        function hienThiMaNhung(id) {
            fetch('${pageContext.request.contextPath}/bieu-mau/ma-nhung?id=' + id)
                .then(function(res) { return res.json(); })
                .then(function(data) {
                    if (data.thanhCong) {
                        document.getElementById('codeIframe').textContent = data.maIframe;
                        document.getElementById('codeScript').textContent = data.maScript;
                        document.getElementById('codeUrl').textContent = data.duongDanTrucTiep;
                        document.getElementById('modalMaNhung').style.display = 'flex';
                    } else {
                        alert(data.thongBao || 'Lỗi lấy mã nhúng');
                    }
                })
                .catch(function(err) {
                    alert('Lỗi kết nối máy chủ');
                });
        }

        function dongModalMaNhung() {
            document.getElementById('modalMaNhung').style.display = 'none';
        }

        function saoChep(elementId) {
            var text = document.getElementById(elementId).textContent;
            navigator.clipboard.writeText(text).then(function() {
                alert('Đã sao chép vào bộ nhớ tạm!');
            });
        }
    </script>
</body>
</html>
