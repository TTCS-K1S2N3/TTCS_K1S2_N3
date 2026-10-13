<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Xác nhận tiếp nhận thông tin yêu cầu tư vấn thành công">
    <title>Gửi Thông Tin Thành Công - CRM Bán Hàng</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200">
    <style>
        :root {
            --bme-primary: #2563eb;
            --bme-primary-hover: #1d4ed8;
            --bme-primary-light: #eff6ff;

            --bme-success: #10b981;
            --bme-success-light: #ecfdf5;
            --bme-success-border: #a7f3d0;
            --bme-success-text: #065f46;

            --bme-neutral-200: #e2e8f0;
            --bme-neutral-500: #64748b;
            --bme-neutral-700: #334155;
            --bme-neutral-900: #0f172a;

            --bme-radius: 12px;
            --bme-shadow: 0 10px 25px -5px rgba(15, 23, 42, 0.08);
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: 'Plus Jakarta Sans', system-ui, -apple-system, sans-serif;
            background-color: transparent;
            color: var(--bme-neutral-900);
            padding: 24px 16px;
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
        }

        .result-card {
            background: #ffffff;
            border: 1px solid var(--bme-neutral-200);
            border-radius: var(--bme-radius);
            box-shadow: var(--bme-shadow);
            max-width: 520px;
            width: 100%;
            padding: 36px 28px;
            text-align: center;
        }

        .result-icon-wrap {
            width: 64px;
            height: 64px;
            background-color: var(--bme-success-light);
            color: var(--bme-success);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 20px;
        }

        .result-icon {
            font-size: 36px;
        }

        .result-card h1 {
            font-size: 1.375rem;
            font-weight: 700;
            color: var(--bme-neutral-900);
            margin-bottom: 10px;
            letter-spacing: -0.01em;
        }

        .result-card p {
            color: var(--bme-neutral-500);
            font-size: 0.9375rem;
            line-height: 1.6;
            margin-bottom: 24px;
        }

        .result-meta-box {
            background-color: #f8fafc;
            border: 1px solid var(--bme-neutral-200);
            border-radius: 8px;
            padding: 14px 18px;
            margin-bottom: 24px;
            font-size: 0.875rem;
            color: var(--bme-neutral-700);
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
        }

        .result-code {
            font-family: ui-monospace, SFMono-Regular, monospace;
            font-size: 0.9375rem;
            color: var(--bme-primary);
            font-weight: 700;
        }

        .btn-back {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 6px;
            background-color: var(--bme-primary);
            color: #ffffff;
            text-decoration: none;
            padding: 11px 24px;
            border-radius: 6px;
            font-size: 0.875rem;
            font-weight: 600;
            transition: background-color 0.2s;
            min-height: 44px;
        }

        .btn-back:hover {
            background-color: var(--bme-primary-hover);
        }
    </style>
</head>
<body>
    <div class="result-card">
        <div class="result-icon-wrap">
            <span class="material-symbols-outlined result-icon" aria-hidden="true">check_circle</span>
        </div>
        <h1>Gửi Yêu Cầu Thành Công!</h1>
        <p><c:out value="${thongBaoThanhCong != null ? thongBaoThanhCong : 'Cảm ơn quý khách đã gửi thông tin. Đội ngũ chuyên viên sẽ chủ động liên hệ lại trong thời gian sớm nhất.'}" /></p>

        <c:if test="${not empty maLead}">
            <div class="result-meta-box">
                <span class="material-symbols-outlined" style="font-size: 18px; color: var(--bme-primary);" aria-hidden="true">tag</span>
                <span>Mã yêu cầu của quý khách: </span>
                <span class="result-code"><c:out value="${maLead}" /></span>
            </div>
        </c:if>

        <div>
            <a href="javascript:history.back()" class="btn-back">
                <span class="material-symbols-outlined" style="font-size: 18px;" aria-hidden="true">arrow_back</span>
                <span>Quay lại trang biểu mẫu</span>
            </a>
        </div>
    </div>
</body>
</html>
