<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gửi Thông Tin Thành Công - CRM Bán Hàng</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        body {
            font-family: 'Inter', system-ui, -apple-system, sans-serif;
            background-color: transparent;
            color: #111827;
            padding: 24px 12px;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .result-card {
            background: #ffffff;
            border: 1px solid #e5e7eb;
            border-radius: 8px;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
            max-width: 520px;
            width: 100%;
            padding: 32px 24px;
            text-align: center;
        }
        .result-icon {
            width: 56px;
            height: 56px;
            background-color: #dcfce7;
            color: #16a34a;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 16px;
            font-size: 28px;
            font-weight: bold;
        }
        .result-card h2 {
            font-size: 1.25rem;
            color: #111827;
            margin-bottom: 8px;
        }
        .result-card p {
            color: #4b5563;
            font-size: 0.9375rem;
            line-height: 1.5;
            margin-bottom: 20px;
        }
        .result-code {
            display: inline-block;
            background-color: #f3f4f6;
            padding: 6px 12px;
            border-radius: 4px;
            font-family: monospace;
            font-size: 0.875rem;
            color: #1e40af;
            font-weight: 600;
            margin-bottom: 20px;
        }
        .btn-back {
            display: inline-block;
            background-color: #1e40af;
            color: #ffffff;
            text-decoration: none;
            padding: 10px 20px;
            border-radius: 6px;
            font-size: 0.875rem;
            font-weight: 500;
        }
        .btn-back:hover {
            background-color: #1d4ed8;
        }
    </style>
</head>
<body>
    <div class="result-card">
        <div class="result-icon">✓</div>
        <h2>Gửi Yêu Cầu Thành Công!</h2>
        <p><c:out value="${thongBaoThanhCong != null ? thongBaoThanhCong : 'Cảm ơn quý khách đã gửi thông tin. Đội ngũ chuyên viên sẽ chủ động liên hệ lại trong thời gian sớm nhất.'}" /></p>
        <c:if test="${not empty maLead}">
            <div>
                <span>Mã yêu cầu của bạn: </span>
                <span class="result-code"><c:out value="${maLead}" /></span>
            </div>
        </c:if>
        <div>
            <a href="javascript:history.back()" class="btn-back">Quay lại trang biểu mẫu</a>
        </div>
    </div>
</body>
</html>
