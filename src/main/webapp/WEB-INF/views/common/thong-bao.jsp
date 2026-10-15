<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%!
    public static String formatDateTime(java.time.LocalDateTime ldt) {
        if (ldt == null) return "--";
        try {
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return ldt.format(dtf);
        } catch (Exception e) {
            return ldt.toString();
        }
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Trung tâm thông báo hệ thống và cảnh báo quá SLA - CRM Bán Hàng">
    <title>Thông Báo Hệ Thống - CRM Bán Hàng</title>
    <!-- CSS dùng chung -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <style>
        .tb-container {
            max-width: 960px;
            margin: 0 auto;
            padding: 24px 28px;
            width: 100%;
            box-sizing: border-box;
        }
        .tb-header {
            background: #ffffff;
            border: 1px solid var(--slate-200);
            border-radius: var(--radius-lg);
            padding: 20px 24px;
            margin-bottom: 24px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            flex-wrap: wrap;
            gap: 16px;
            box-shadow: var(--shadow-sm);
        }
        .tb-title-group h1 {
            font-size: 1.4rem;
            font-weight: 800;
            color: var(--slate-900);
            margin: 0 0 4px 0;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .tb-title-group p {
            font-size: 0.9rem;
            color: var(--slate-500);
            margin: 0;
        }
        .tb-badge-unread {
            background: var(--danger);
            color: #ffffff;
            padding: 4px 12px;
            border-radius: 9999px;
            font-size: 0.85rem;
            font-weight: 700;
            display: inline-flex;
            align-items: center;
            gap: 6px;
        }
        .tb-list {
            display: flex;
            flex-direction: column;
            gap: 12px;
        }
        .tb-item {
            background: #ffffff;
            border: 1px solid var(--slate-200);
            border-radius: var(--radius-md);
            padding: 16px 20px;
            transition: all 0.2s ease;
            display: flex;
            align-items: flex-start;
            justify-content: space-between;
            gap: 16px;
        }
        .tb-item:hover {
            border-color: var(--primary);
            box-shadow: var(--shadow-md);
            transform: translateY(-1px);
        }
        .tb-item.unread {
            background: #fffdfa;
            border-left: 4px solid var(--warning);
        }
        .tb-item-content {
            display: flex;
            align-items: flex-start;
            gap: 14px;
            flex: 1;
        }
        .tb-item-icon {
            width: 40px;
            height: 40px;
            border-radius: 10px;
            background: var(--warning-light);
            color: var(--warning-text);
            display: flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
            font-size: 20px;
        }
        .tb-item-icon.danger {
            background: var(--danger-light);
            color: var(--danger);
        }
        .tb-item-body {
            flex: 1;
        }
        .tb-item-title {
            font-size: 1rem;
            font-weight: 700;
            color: var(--slate-900);
            margin-bottom: 4px;
        }
        .tb-item-desc {
            font-size: 0.9rem;
            color: var(--slate-600);
            line-height: 1.5;
            margin-bottom: 6px;
        }
        .tb-item-meta {
            font-size: 0.8rem;
            color: var(--slate-400);
            display: flex;
            align-items: center;
            gap: 12px;
        }
        .tb-item-action {
            flex-shrink: 0;
        }
        .tb-btn-open {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 8px 16px;
            font-size: 0.875rem;
            font-weight: 600;
            color: var(--primary);
            background: var(--primary-light);
            border: 1px solid var(--primary-border);
            border-radius: var(--radius-md);
            cursor: pointer;
            transition: all 0.15s ease;
        }
        .tb-btn-open:hover {
            background: var(--primary);
            color: #ffffff;
        }
        .tb-empty {
            background: #ffffff;
            border: 1px dashed var(--slate-300);
            border-radius: var(--radius-lg);
            padding: 48px 24px;
            text-align: center;
            color: var(--slate-500);
        }
        .tb-empty-icon {
            font-size: 48px;
            color: var(--slate-300);
            margin-bottom: 12px;
        }
    </style>
</head>
<body class="crm-body">
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content page-container" id="crm-main-content">
        <div class="tb-container">

            <!-- Breadcrumb -->
            <nav class="crm-breadcrumb" aria-label="Breadcrumb" style="display: flex; gap: 8px; font-size: 13.5px; color: #64748b; margin-bottom: 16px;">
                <a href="${pageContext.request.contextPath}/dieu-huong" style="color: #2563eb; text-decoration: none;">Trang chủ</a>
                <span>/</span>
                <span style="color: #0f172a; font-weight: 600;">Thông báo</span>
            </nav>

            <!-- Header -->
            <header class="tb-header">
                <div class="tb-title-group">
                    <h1>
                        <span class="material-symbols-outlined" style="color: var(--primary);" aria-hidden="true">notifications_active</span>
                        <span>Trung Tâm Thông Báo</span>
                    </h1>
                    <p>Cảnh báo quá SLA phản hồi và thông báo quan trọng từ hệ thống (Story S4-07, AC3, Q5)</p>
                </div>
                <div>
                    <span class="tb-badge-unread" id="badgeSoChuaDoc">
                        <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">mark_email_unread</span>
                        <span>${soThongBaoChuaDoc} chưa đọc</span>
                    </span>
                </div>
            </header>

            <!-- Danh sách thông báo -->
            <section class="tb-list" aria-label="Danh sách thông báo của bạn">
                <c:choose>
                    <c:when test="${not empty danhSachThongBaoCuaToi}">
                        <c:forEach items="${danhSachThongBaoCuaToi}" var="tb">
                            <article class="tb-item ${tb.daDoc ? '' : 'unread'}" id="thongBaoItem-${tb.id}">
                                <div class="tb-item-content">
                                    <div class="tb-item-icon danger" aria-hidden="true">
                                        <span class="material-symbols-outlined">notification_important</span>
                                    </div>
                                    <div class="tb-item-body">
                                        <div class="tb-item-title"><c:out value="${tb.tieuDe}" /></div>
                                        <div class="tb-item-desc"><c:out value="${tb.noiDung}" /></div>
                                        <div class="tb-item-meta">
                                            <span><%= formatDateTime(((vn.nhom10.crm.model.ThongBao) pageContext.getAttribute("tb")).getCreatedAt()) %></span>
                                            <c:if test="${tb.daDoc}">
                                                <span style="color: var(--success-text); display: inline-flex; align-items: center; gap: 2px;">
                                                    <span class="material-symbols-outlined" style="font-size: 14px;">done_all</span> Đã đọc
                                                </span>
                                            </c:if>
                                            <c:if test="${not tb.daDoc}">
                                                <span style="color: var(--danger); font-weight: 700;">Chưa đọc</span>
                                            </c:if>
                                        </div>
                                    </div>
                                </div>
                                <div class="tb-item-action">
                                    <!-- Q5: Mở thông báo bằng POST để server đánh dấu đọc và chuyển hướng an toàn -->
                                    <form action="${pageContext.request.contextPath}/thong-bao/mo" method="POST" style="margin: 0;">
                                        <input type="hidden" name="thongBaoId" value="${tb.id}">
                                        <button type="submit" class="tb-btn-open" id="btnMoThongBao-${tb.id}">
                                            <span>Xem chi tiết</span>
                                            <span class="material-symbols-outlined" style="font-size: 18px;" aria-hidden="true">arrow_forward</span>
                                        </button>
                                    </form>
                                </div>
                            </article>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <div class="tb-empty">
                            <span class="material-symbols-outlined tb-empty-icon" aria-hidden="true">notifications_paused</span>
                            <h3>Không có thông báo nào</h3>
                            <p>Tất cả thông báo và cảnh báo SLA của bạn sẽ được hiển thị tại đây.</p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>

        </div>
    </main>
</body>
</html>
