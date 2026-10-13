<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>403 - Không có quyền truy cập | CRM Bán Hàng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <style>
        .error-container {
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 80vh;
            padding: 20px;
        }
        .error-card {
            background: #ffffff;
            border-radius: 12px;
            padding: 32px 24px;
            max-width: 480px;
            width: 100%;
            text-align: center;
            box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08);
            border: 1px solid #e2e8f0;
        }
        .error-badge {
            display: inline-block;
            background: #fee2e2;
            color: #dc2626;
            font-size: 14px;
            font-weight: 700;
            padding: 6px 16px;
            border-radius: 9999px;
            margin-bottom: 16px;
        }
        .error-title {
            font-size: 22px;
            font-weight: 700;
            color: #1e293b;
            margin-bottom: 12px;
        }
        .error-desc {
            font-size: 15px;
            color: #64748b;
            line-height: 1.6;
            margin-bottom: 24px;
        }
        .btn-back {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            background: #2563eb;
            color: #ffffff;
            text-decoration: none;
            font-weight: 600;
            padding: 10px 20px;
            border-radius: 8px;
            transition: background 0.2s ease;
        }
        .btn-back:hover {
            background: #1d4ed8;
        }
    </style>
</head>
<body class="crm-body">
    <div class="error-container">
        <div class="error-card">
            <div style="margin-bottom: 12px; color: #dc2626;"><span class="material-symbols-outlined" style="font-size: 56px;" aria-hidden="true">gpp_bad</span></div>
            <div class="error-badge">LỖI 403 - TỪ CHỐI TRUY CẬP</div>
            <h1 class="error-title">Bạn không có quyền truy cập</h1>
            <p class="error-desc">
                ${requestScope.errorMessage != null ? requestScope.errorMessage : "Chức năng này không thuộc phạm vi quyền hạn của vai trò hiện tại của bạn trong hệ thống CRM."}
            </p>
            <a href="${pageContext.request.contextPath}/trang-chu" class="btn-back">
                <span class="material-symbols-outlined icon-xs" style="margin-right: 6px;" aria-hidden="true">arrow_back</span> Về trang chủ
            </a>
        </div>
    </div>
</body>
</html>
