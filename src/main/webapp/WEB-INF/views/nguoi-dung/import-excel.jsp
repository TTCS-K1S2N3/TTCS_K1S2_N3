<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nháº­p danh sÃ¡ch ngÆ°á»i dÃ¹ng tá»« Excel - CRM BÃ¡n HÃ ng</title>
    <meta name="description" content="Nháº­p danh sÃ¡ch ngÆ°á»i dÃ¹ng hÃ ng loáº¡t tá»« tá»‡p Excel, xem trÆ°á»›c lá»—i tá»«ng dÃ²ng vÃ  tá»± Ä‘á»™ng bá» qua dÃ²ng lá»—i cho Quáº£n trá»‹ há»‡ thá»‘ng CRM.">
    <!-- CSS Há»‡ thá»‘ng -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navigation.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/nguoi-dung/nguoi-dung.css">
    <!-- CSS ChuyÃªn biá»‡t cho tÃ­nh nÄƒng Nháº­p Excel S2-01 -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/nguoi-dung/import-excel.css">
</head>
<body class="crm-body">

    <!-- Thanh Ä‘iá»u hÆ°á»›ng vÃ  Sidebar chuáº©n há»‡ thá»‘ng S1-06 -->
    <jsp:include page="/WEB-INF/views/layout/navigation.jsp" />

    <main class="crm-main-content" id="crm-main-content">
        <div class="import-container">

            <!-- Breadcrumb Ä‘iá»u hÆ°á»›ng chuáº©n -->
            <nav class="crm-breadcrumb" style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
                <div style="font-size: 13.5px; color: var(--slate-500, #64748b);">
                    <a href="${pageContext.request.contextPath}/dieu-huong" style="color: var(--primary, #2563eb); text-decoration: none;">Trang chá»§</a>
                    <span style="margin: 0 6px;">/</span>
                    <a href="${pageContext.request.contextPath}/nguoi-dung" style="color: var(--primary, #2563eb); text-decoration: none;">NgÆ°á»i dÃ¹ng & PhÃ¢n quyá»n</a>
                    <span style="margin: 0 6px;">/</span>
                    <span style="color: var(--slate-700, #334155); font-weight: 500;">Nháº­p danh sÃ¡ch Excel</span>
                </div>
                <a href="${pageContext.request.contextPath}/nguoi-dung" class="btn btn-outline btn-sm" style="display: inline-flex; align-items: center; gap: 4px;">
                    <span class="material-symbols-outlined icon-xs" aria-hidden="true">arrow_back</span>
                    Quay láº¡i Quáº£n lÃ½ tÃ i khoáº£n
                </a>
            </nav>

            <!-- HEADER TRANG -->
            <div class="import-page-header">
                <div class="import-header-title">
                    <h1>Nháº­p danh sÃ¡ch ngÆ°á»i dÃ¹ng tá»« tá»‡p Excel</h1>
                    <p>Táº¡o tÃ i khoáº£n hÃ ng loáº¡t cho khá»‘i kinh doanh, xem trÆ°á»›c lá»—i tá»«ng dÃ²ng vÃ  tá»± Ä‘á»™ng bá» qua dÃ²ng lá»—i.</p>
                </div>
                <div>
                    <a href="${pageContext.request.contextPath}/nguoi-dung" class="btn-back-link" id="link-ve-danh-sach" style="display: inline-flex; align-items: center; gap: 4px;">
                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">arrow_back</span>
                        Vá» danh sÃ¡ch ngÆ°á»i dÃ¹ng
                    </a>
                </div>
            </div>

            <!-- STEPPER WIZARD -->
            <div class="import-stepper" aria-label="Tiáº¿n trÃ¬nh nháº­p dá»¯ liá»‡u">
                <div class="step-item ${empty baoCao ? 'active' : 'completed'}">
                    <div class="step-circle">1</div>
                    <div class="step-text">
                        <span class="step-title">BÆ°á»›c 1: Tá»‡p máº«u</span>
                        <span class="step-desc">Chuáº©n bá»‹ dá»¯ liá»‡u chuáº©n</span>
                    </div>
                </div>
                <div class="step-item ${cheDo == 'xem-truoc' ? 'active' : (cheDo == 'ket-qua' ? 'completed' : '')}">
                    <div class="step-circle">2</div>
                    <div class="step-text">
                        <span class="step-title">BÆ°á»›c 2: Xem trÆ°á»›c</span>
                        <span class="step-desc">Tháº©m Ä‘á»‹nh & BÃ¡o lá»—i</span>
                    </div>
                </div>
                <div class="step-item ${cheDo == 'ket-qua' ? 'active' : ''}">
                    <div class="step-circle">3</div>
                    <div class="step-text">
                        <span class="step-title">BÆ°á»›c 3: Nháº­p dá»¯ liá»‡u</span>
                        <span class="step-desc">BÃ¡o cÃ¡o tá»•ng káº¿t</span>
                    </div>
                </div>
            </div>

            <!-- THÃ”NG BÃO Lá»–I Há»† THá»NG Náº¾U CÃ“ -->
            <c:if test="${not empty thongBaoLoi}">
                <div class="alert alert-error" role="alert" style="margin-bottom: 20px;">
                    <div>
                        <strong>ThÃ´ng bÃ¡o lá»—i:</strong> <c:out value="${thongBaoLoi}" />
                    </div>
                </div>
            </c:if>

            <!-- CARD 1: HÆ¯á»šNG DáºªN & Táº¢I Tá»†P MáºªU (AC 1) -->
            <section class="import-card" aria-labelledby="card-title-step1">
                <div class="card-header-flex">
                    <div>
                        <h2 id="card-title-step1" class="card-title-lg">BÆ°á»›c 1: Chuáº©n bá»‹ dá»¯ liá»‡u theo tá»‡p máº«u chuáº©n</h2>
                        <div class="card-subtitle-text">Táº£i tá»‡p máº«u Excel cÃ³ Ä‘á»‹nh dáº¡ng chuáº©n kÃ¨m cÃ¡c hÆ°á»›ng dáº«n vai trÃ² vÃ  nhÃ³m kinh doanh.</div>
                    </div>
                    <div>
                        <a id="btn-tai-tep-mau"
                           href="${pageContext.request.contextPath}/nguoi-dung/tai-tep-mau"
                           class="btn-download-template"
                           title="Táº£i tá»‡p máº«u Excel vá» mÃ¡y tÃ­nh"
                           style="display: inline-flex; align-items: center; gap: 6px;">
                            <span class="material-symbols-outlined icon-sm" aria-hidden="true">download</span>
                            Táº£i tá»‡p máº«u Excel (.xlsx)
                        </a>
                    </div>
                </div>

                <div class="guideline-box">
                    <div class="guideline-title">Quy táº¯c kiá»ƒm tra dá»¯ liá»‡u Ä‘áº§u vÃ o:</div>
                    <ul class="guideline-list">
                        <li>CÃ¡c trÆ°á»ng thÃ´ng tin báº¯t buá»™c: <strong>Há» vÃ  tÃªn</strong>, <strong>Email cÃ´ng ty</strong> vÃ  <strong>MÃ£ vai trÃ²</strong>.</li>
                        <li>MÃ£ vai trÃ² há»£p lá»‡ trong há»‡ thá»‘ng:
                            <span class="role-tag">ADMIN</span>
                            <span class="role-tag">DIRECTOR</span>
                            <span class="role-tag">TEAM_LEAD</span>
                            <span class="role-tag">SALES_REP</span>
                            <span class="role-tag">MARKETING</span>
                            <span class="role-tag">CUST_SUCCESS</span>
                            <span class="role-tag">ACCOUNTANT</span>.
                        </li>
                        <li><strong>RÃ ng buá»™c cÆ¡ cáº¥u tá»• chá»©c:</strong> NgÆ°á»i giá»¯ vai trÃ² TrÆ°á»Ÿng nhÃ³m (<span class="role-tag">TEAM_LEAD</span>) báº¯t buá»™c pháº£i Ä‘Æ°á»£c gÃ¡n vÃ o má»™t nhÃ³m kinh doanh cá»¥ thá»ƒ.</li>
                        <li><strong>Máº­t kháº©u:</strong> Náº¿u cá»™t máº­t kháº©u trong tá»‡p Ä‘á»ƒ trá»‘ng, há»‡ thá»‘ng sáº½ tá»± Ä‘á»™ng sinh máº­t kháº©u táº¡m máº¡nh ngáº«u nhiÃªn vÃ  gá»­i danh sÃ¡ch trong bÃ¡o cÃ¡o tá»•ng káº¿t.</li>
                        <li>Há»‡ thá»‘ng tá»± Ä‘á»™ng phÃ¡t hiá»‡n email trÃ¹ng láº·p ngay trong tá»‡p táº£i lÃªn hoáº·c Ä‘Ã£ tá»“n táº¡i trong cÆ¡ sá»Ÿ dá»¯ liá»‡u.</li>
                    </ul>
                </div>
            </section>

            <!-- CARD 2: Táº¢I Tá»†P LÃŠN & THAO TÃC (AC 2 & AC 3) -->
            <section class="import-card" aria-labelledby="card-title-step2">
                <div class="card-header-flex">
                    <div>
                        <h2 id="card-title-step2" class="card-title-lg">BÆ°á»›c 2: Táº£i tá»‡p Excel lÃªn há»‡ thá»‘ng</h2>
                        <div class="card-subtitle-text">Chá»n hoáº·c kÃ©o tháº£ tá»‡p Excel chá»©a danh sÃ¡ch ngÆ°á»i dÃ¹ng Ä‘á»ƒ há»‡ thá»‘ng kiá»ƒm tra vÃ  xá»­ lÃ½.</div>
                    </div>
                </div>

                <form id="form-import-excel"
                      action="${pageContext.request.contextPath}/nguoi-dung/import"
                      method="post"
                      enctype="multipart/form-data">

                    <input type="hidden" name="previewToken" id="form-preview-token" value="${previewToken}" />

                    <!-- VÃ™NG KÃ‰O THáº¢ Tá»†P (DROPZONE) -->
                    <div class="dropzone-container" id="dropzone-container">
                        <div class="dropzone-icon">
                            <span class="material-symbols-outlined icon-2xl" aria-hidden="true">upload_file</span>
                        </div>
                        <div class="dropzone-text-main">KÃ©o vÃ  tháº£ tá»‡p Excel vÃ o Ä‘Ã¢y, hoáº·c báº¥m Ä‘á»ƒ duyá»‡t tá»‡p</div>
                        <div class="dropzone-text-sub">Cháº¥p nháº­n tá»‡p Ä‘á»‹nh dáº¡ng <strong>.xlsx</strong> hoáº·c <strong>.xls</strong> (Dung lÆ°á»£ng tá»‘i Ä‘a 10MB)</div>
                        <input type="file" id="fileExcel" name="fileExcel" accept=".xlsx, .xls" class="dropzone-input-hidden" ${empty baoCao ? 'required' : ''} />
                    </div>

                    <!-- THáºº HIá»‚N THá»Š THÃ”NG TIN Tá»†P ÄÃƒ CHá»ŒN -->
                    <div class="selected-file-card" id="selected-file-card">
                        <div class="selected-file-info">
                            <span class="file-excel-badge">EXCEL</span>
                            <div class="file-details-text">
                                <span class="file-name-display" id="file-name-display">tep_nguoi_dung.xlsx</span>
                                <span class="file-size-display" id="file-size-display">0 KB</span>
                            </div>
                        </div>
                        <button type="button" class="btn-remove-selected-file" id="btn-remove-selected-file" title="XÃ³a tá»‡p Ä‘Ã£ chá»n" style="display: inline-flex; align-items: center; gap: 4px;">
                            <span class="material-symbols-outlined icon-sm" aria-hidden="true">close</span>
                            Chá»n tá»‡p khÃ¡c
                        </button>
                    </div>

                    <!-- NHÃ“M NÃšT THAO TÃC -->
                    <div class="action-buttons-wrap" style="margin-top: 20px;">
                        <button type="submit"
                                name="action"
                                value="xem-truoc"
                                id="btn-xem-truoc"
                                class="btn-action btn-action-preview"
                                style="display: inline-flex; align-items: center; gap: 6px;">
                            <span class="material-symbols-outlined icon-sm" aria-hidden="true">visibility</span>
                            Xem trÆ°á»›c vÃ  kiá»ƒm tra lá»—i
                        </button>

                        <button type="button"
                                id="btn-nhap-du-lieu"
                                class="btn-action btn-action-import"
                                style="display: inline-flex; align-items: center; gap: 6px;">
                            <span class="material-symbols-outlined icon-sm" aria-hidden="true">check_circle</span>
                            Tiáº¿n hÃ nh nháº­p dá»¯ liá»‡u
                        </button>
                    </div>
                </form>
            </section>

            <!-- CARD 3: BÃO CÃO XEM TRÆ¯á»šC / Káº¾T QUáº¢ Tá»”NG Káº¾T (AC 2 & AC 3) -->
            <c:if test="${not empty baoCao}">
                <section class="import-card" id="khu-vuc-bao-cao" aria-labelledby="card-title-step3">
                    <div class="card-header-flex">
                        <div>
                            <h2 id="card-title-step3" class="card-title-lg" style="display: flex; align-items: center; gap: 8px;">
                                <c:choose>
                                    <c:when test="${cheDo == 'ket-qua'}">
                                        <span class="material-symbols-outlined icon-md" aria-hidden="true">assessment</span>
                                        BÃ¡o cÃ¡o tá»•ng káº¿t Ä‘á»£t nháº­p ngÆ°á»i dÃ¹ng: <c:out value="${tenTep}" />
                                    </c:when>
                                    <c:otherwise>
                                        <span class="material-symbols-outlined icon-md" aria-hidden="true">visibility</span>
                                        Káº¿t quáº£ xem trÆ°á»›c vÃ  kiá»ƒm tra lá»—i: <c:out value="${tenTep}" />
                                    </c:otherwise>
                                </c:choose>
                            </h2>
                            <div class="card-subtitle-text">
                                <c:choose>
                                    <c:when test="${cheDo == 'ket-qua'}">
                                        Há»‡ thá»‘ng Ä‘Ã£ tá»± Ä‘á»™ng bá» qua cÃ¡c dÃ²ng lá»—i vÃ  hoÃ n táº¥t nháº­p cÃ¡c tÃ i khoáº£n há»£p lá»‡.
                                    </c:when>
                                    <c:otherwise>
                                        Kiá»ƒm tra tráº¡ng thÃ¡i tá»«ng dÃ²ng dÆ°á»›i Ä‘Ã¢y trÆ°á»›c khi quyáº¿t Ä‘á»‹nh báº¥m "Tiáº¿n hÃ nh nháº­p dá»¯ liá»‡u".
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <!-- CÃC NÃšT THAO TÃC TRÃŠN BÃO CÃO -->
                        <div style="display: flex; gap: 8px;">
                            <c:if test="${cheDo == 'ket-qua' and baoCao.soDongThanhCong > 0}">
                                <button type="button"
                                        class="btn-action btn-action-outline"
                                        id="btn-copy-all-credentials"
                                        title="Sao chÃ©p toÃ n bá»™ danh sÃ¡ch tÃ i khoáº£n vá»«a táº¡o"
                                        style="display: inline-flex; align-items: center; gap: 6px;">
                                    <span class="material-symbols-outlined icon-sm" aria-hidden="true">content_copy</span>
                                    Sao chÃ©p tÃ i khoáº£n & Máº­t kháº©u
                                </button>
                            </c:if>
                            <button type="button"
                                    class="btn-action btn-action-outline"
                                    id="btn-print-report"
                                    title="In hoáº·c lÆ°u bÃ¡o cÃ¡o dÆ°á»›i dáº¡ng PDF"
                                    style="display: inline-flex; align-items: center; gap: 6px;">
                                <span class="material-symbols-outlined icon-sm" aria-hidden="true">print</span>
                                In bÃ¡o cÃ¡o
                            </button>
                        </div>
                    </div>

                    <!-- THÃ”NG BÃO Tá»”NG Káº¾T KHI NHáº¬P THÃ€NH CÃ”NG (Story S2-01) -->
                    <c:if test="${cheDo == 'ket-qua'}">
                        <div class="alert alert-success" id="alert-import-success" role="alert" style="margin-top: 16px; margin-bottom: 20px; background-color: #f0fdf4; border: 1px solid #bbf7d0; color: #166534; padding: 16px 20px; border-radius: 8px; font-weight: 600; font-size: 1.05rem; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 12px;">
                            <div style="display: flex; align-items: center; gap: 6px;">
                                <span class="material-symbols-outlined icon-sm" aria-hidden="true">check_circle</span>
                                <c:out value="${not empty thongBaoThanhCong ? thongBaoThanhCong : 'ÄÃ£ nháº­p thÃ nh cÃ´ng ' += baoCao.soDongThanhCong += '/' += baoCao.tongSoDong += ' tÃ i khoáº£n. ' += baoCao.soDongThatBai += ' dÃ²ng lá»—i Ä‘Ã£ Ä‘Æ°á»£c bá» qua.'}" />
                            </div>
                            <a href="${pageContext.request.contextPath}/nguoi-dung/import" class="btn-action btn-action-outline" id="btn-nhap-tep-moi" style="font-size: 0.85rem; text-decoration: none; display: inline-flex; align-items: center; gap: 4px;">
                                Nháº­p Ä‘á»£t tá»‡p má»›i <span class="material-symbols-outlined icon-xs" aria-hidden="true">arrow_forward</span>
                            </a>
                        </div>
                    </c:if>

                    <!-- THANH HÃ€NH Äá»˜NG PREVIEW TRá»°C TIáº¾P (Story S2-01 Review Fix) -->
                    <c:if test="${cheDo == 'xem-truoc'}">
                        <div class="preview-cta-toolbar" style="margin-top: 16px; margin-bottom: 20px; padding: 16px 20px; background: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 8px; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 12px;">
                            <div>
                                <div style="font-weight: 600; font-size: 1rem; color: #166534; display: flex; align-items: center; gap: 6px;">
                                    <span class="material-symbols-outlined icon-sm" aria-hidden="true">visibility</span>
                                    Káº¿t quáº£ xem trÆ°á»›c:
                                    <c:choose>
                                        <c:when test="${baoCao.soDongHopLe > 0}">
                                            CÃ³ <strong>${baoCao.soDongHopLe}</strong> dÃ²ng há»£p lá»‡ sáºµn sÃ ng nháº­p trá»±c tiáº¿p vÃ o há»‡ thá»‘ng.
                                        </c:when>
                                        <c:otherwise>
                                            <span style="color: #dc2626;">Tá»‡p khÃ´ng cÃ³ dÃ²ng nÃ o há»£p lá»‡ Ä‘á»ƒ nháº­p.</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div style="font-size: 0.85rem; color: #15803d; margin-top: 2px;">
                                    Báº¥m "Thá»±c hiá»‡n nháº­p" Ä‘á»ƒ lÆ°u tÃ i khoáº£n há»£p lá»‡ ngay mÃ  khÃ´ng cáº§n táº£i láº¡i tá»‡p.
                                </div>
                            </div>
                            <div style="display: flex; gap: 12px; align-items: center;">
                                <a href="${pageContext.request.contextPath}/nguoi-dung/import" class="btn-action btn-action-outline" id="btn-chon-tep-khac" style="text-decoration: none; display: inline-flex; align-items: center; gap: 4px;">
                                    <span class="material-symbols-outlined icon-sm" aria-hidden="true">close</span>
                                    Chá»n tá»‡p khÃ¡c
                                </a>
                                <form id="form-thuc-hien-nhap" action="${pageContext.request.contextPath}/nguoi-dung/import" method="post" style="margin: 0; display: inline-block;">
                                    <input type="hidden" name="action" value="nhap-du-lieu" />
                                    <input type="hidden" name="previewToken" value="${previewToken}" />
                                    <button type="button"
                                            id="btn-thuc-hien-nhap"
                                            class="btn-action btn-action-import"
                                            ${baoCao.soDongHopLe > 0 ? '' : 'disabled="disabled"'}
                                            style="${baoCao.soDongHopLe > 0 ? 'display: inline-flex; align-items: center; gap: 6px;' : 'display: inline-flex; align-items: center; gap: 6px; opacity: 0.5; cursor: not-allowed;'}">
                                        <span class="material-symbols-outlined icon-sm" aria-hidden="true">check_circle</span>
                                        Thá»±c hiá»‡n nháº­p
                                    </button>
                                </form>
                            </div>
                        </div>
                    </c:if>

                    <!-- THá»NG KÃŠ Tá»”NG Káº¾T Äá»¢T NHáº¬P -->
                    <div class="stats-cards-grid">
                        <div class="stat-item-box total">
                            <div class="stat-header-sub">
                                <span class="stat-item-label">Tá»•ng sá»‘ dÃ²ng</span>
                                <span class="material-symbols-outlined stat-item-icon" aria-hidden="true">description</span>
                            </div>
                            <div class="stat-item-value" id="stat-num-total"><c:out value="${baoCao.tongSoDong}" /></div>
                            <div class="stat-item-subtext">Báº£n ghi trong tá»‡p Excel</div>
                        </div>

                        <div class="stat-item-box valid">
                            <div class="stat-header-sub">
                                <span class="stat-item-label">DÃ²ng há»£p lá»‡</span>
                                <span class="material-symbols-outlined stat-item-icon" aria-hidden="true">check_circle</span>
                            </div>
                            <div class="stat-item-value" id="stat-num-valid"><c:out value="${baoCao.soDongHopLe}" /></div>
                            <div class="stat-item-subtext">Äáº¡t chuáº©n nghiá»‡p vá»¥</div>
                        </div>

                        <div class="stat-item-box error">
                            <div class="stat-header-sub">
                                <span class="stat-item-label">DÃ²ng cÃ³ lá»—i</span>
                                <span class="material-symbols-outlined stat-item-icon" aria-hidden="true">warning</span>
                            </div>
                            <div class="stat-item-value" id="stat-num-error"><c:out value="${baoCao.soDongLoi}" /></div>
                            <div class="stat-item-subtext">Sáº½ tá»± Ä‘á»™ng bá» qua</div>
                        </div>

                        <c:if test="${cheDo == 'ket-qua'}">
                            <div class="stat-item-box imported">
                                <div class="stat-header-sub">
                                    <span class="stat-item-label">ÄÃ£ táº¡o thÃ nh cÃ´ng</span>
                                    <span class="material-symbols-outlined stat-item-icon" aria-hidden="true">celebration</span>
                                </div>
                                <div class="stat-item-value" id="stat-num-imported"><c:out value="${baoCao.soDongThanhCong}" /></div>
                                <div class="stat-item-subtext">TÃ i khoáº£n sáºµn sÃ ng dÃ¹ng</div>
                            </div>
                        </c:if>
                    </div>

                    <!-- THANH TIáº¾N TRÃŒNH Tá»¶ Lá»† Há»¢P Lá»† / Lá»–I -->
                    <c:if test="${baoCao.tongSoDong > 0}">
                        <div class="progress-stacked-wrap">
                            <div class="progress-header-info">
                                <span>Tá»· lá»‡ há»£p lá»‡: <strong>${Math.round(baoCao.soDongHopLe * 100.0 / baoCao.tongSoDong)}%</strong></span>
                                <span>Tá»· lá»‡ lá»—i: <strong>${Math.round(baoCao.soDongLoi * 100.0 / baoCao.tongSoDong)}%</strong></span>
                            </div>
                            <div class="progress-bar-track">
                                <div class="progress-bar-fill-valid" style="width: ${baoCao.soDongHopLe * 100.0 / baoCao.tongSoDong}%;"></div>
                                <div class="progress-bar-fill-error" style="width: ${baoCao.soDongLoi * 100.0 / baoCao.tongSoDong}%;"></div>
                            </div>
                        </div>
                    </c:if>

                    <!-- THÃ”NG ÄIá»†P Tá»”NG Káº¾T Há»† THá»NG -->
                    <div class="alert alert-info" style="margin-bottom: 20px; background-color: #EBF8FF; color: #2B6CB0; border: 1px solid #BEE3F8; border-radius: 8px; padding: 14px 18px; display: flex; align-items: center; gap: 6px;">
                        <span class="material-symbols-outlined icon-sm" aria-hidden="true">info</span>
                        <div><strong>TÃ³m táº¯t Ä‘á»£t xá»­ lÃ½:</strong> <c:out value="${baoCao.thongDiep}" /></div>
                    </div>

                    <!-- TOOLBAR: Bá»˜ Lá»ŒC THEO TAB VÃ€ TÃŒM KIáº¾M NHANH -->
                    <div class="table-toolbar-box">
                        <div class="tab-filter-container">
                            <button type="button" class="tab-filter-btn active" data-filter="all" id="tab-tat-ca">
                                Táº¥t cáº£ <span class="tab-count-badge"><c:out value="${baoCao.tongSoDong}" /></span>
                            </button>
                            <button type="button" class="tab-filter-btn" data-filter="valid" id="tab-hop-le">
                                <c:choose>
                                    <c:when test="${cheDo == 'ket-qua'}">
                                        ÄÃ£ nháº­p thÃ nh cÃ´ng <span class="tab-count-badge"><c:out value="${baoCao.soDongThanhCong}" /></span>
                                    </c:when>
                                    <c:otherwise>
                                        Há»£p lá»‡ <span class="tab-count-badge"><c:out value="${baoCao.soDongHopLe}" /></span>
                                    </c:otherwise>
                                </c:choose>
                            </button>
                            <button type="button" class="tab-filter-btn" data-filter="error" id="tab-loi">
                                <c:choose>
                                    <c:when test="${cheDo == 'ket-qua'}">
                                        Bá»‹ bá» qua do lá»—i <span class="tab-count-badge"><c:out value="${baoCao.soDongLoi}" /></span>
                                    </c:when>
                                    <c:otherwise>
                                        CÃ³ lá»—i <span class="tab-count-badge"><c:out value="${baoCao.soDongLoi}" /></span>
                                    </c:otherwise>
                                </c:choose>
                            </button>
                        </div>

                        <!-- Ã” TÃŒM KIáº¾M TRONG Báº¢NG -->
                        <div class="table-search-wrapper">
                            <span class="material-symbols-outlined table-search-icon" aria-hidden="true">search</span>
                            <input type="text"
                                   id="timKiemBang"
                                   class="table-search-input"
                                   placeholder="Lá»c theo tÃªn, email, vai trÃ² hoáº·c lá»—i..."
                                   aria-label="TÃ¬m kiáº¿m trong báº£ng xem trÆ°á»›c" />
                        </div>
                    </div>

                    <!-- Báº¢NG CHI TIáº¾T Tá»ªNG DÃ’NG (AC 2 & AC 3) -->
                    <div class="preview-table-container">
                        <table class="import-preview-table" id="bang-ket-qua-excel">
                            <thead>
                                <tr>
                                    <th style="width: 60px; text-align: center;">DÃ²ng</th>
                                    <th>Há» vÃ  tÃªn</th>
                                    <th>Email</th>
                                    <th>Sá»‘ Ä‘iá»‡n thoáº¡i</th>
                                    <th>Vai trÃ²</th>
                                    <th>NhÃ³m kinhdong</th>
                                    <th style="width: 150px; text-align: center;">Tráº¡ng thÃ¡i</th>
                                    <th>Chi tiáº¿t tháº©m Ä‘á»‹nh / Máº­t kháº©u táº¡m</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="dong" items="${baoCao.danhSachTatCaDong}">
                                    <c:set var="statusKey" value="${cheDo == 'ket-qua' ? (dong.daNhap ? 'imported' : 'skipped') : (dong.hopLe ? 'valid' : 'error')}" />
                                    <tr data-status="${statusKey}" class="${not dong.hopLe ? 'row-error-highlight' : ''}">
                                        <td class="row-index-cell"><c:out value="${dong.soDong}" /></td>
                                        <td class="user-name-cell"><c:out value="${dong.hoTen}" /></td>
                                        <td class="user-email-cell"><code><c:out value="${dong.email}" /></code></td>
                                        <td><c:out value="${empty dong.soDienThoai ? '-' : dong.soDienThoai}" /></td>
                                        <td>
                                            <span class="role-tag"><c:out value="${dong.chuoiVaiTroHienThi}" /></span>
                                        </td>
                                        <td><c:out value="${empty dong.tenNhomGiaiQuyet ? '-' : dong.tenNhomGiaiQuyet}" /></td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${cheDo == 'ket-qua'}">
                                                    <c:choose>
                                                        <c:when test="${dong.daNhap}">
                                                            <span class="badge-state badge-state-imported" title="ÄÃ£ lÆ°u vÃ o cÆ¡ sá»Ÿ dá»¯ liá»‡u vá»›i ID: ${dong.idNguoiDung}">
                                                                <span class="material-symbols-outlined icon-xs" aria-hidden="true">check_circle</span> ÄÃ£ táº¡o (ID: ${dong.idNguoiDung})
                                                            </span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge-state badge-state-skipped" title="DÃ²ng bá»‹ lá»—i Ä‘Ã£ Ä‘Æ°á»£c tá»± Ä‘á»™ng bá» qua">
                                                                <span class="material-symbols-outlined icon-xs" aria-hidden="true">cancel</span> Bá»‹ bá» qua
                                                            </span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:choose>
                                                        <c:when test="${dong.hopLe}">
                                                            <span class="badge-state badge-state-valid" title="Dá»¯ liá»‡u Ä‘Ã¡p á»©ng táº¥t cáº£ quy táº¯c">
                                                                <span class="material-symbols-outlined icon-xs" aria-hidden="true">check_circle</span> Há»£p lá»‡
                                                            </span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge-state badge-state-error" title="Dá»¯ liá»‡u vi pháº¡m quy táº¯c">
                                                                <span class="material-symbols-outlined icon-xs" aria-hidden="true">warning</span> DÃ²ng lá»—i
                                                            </span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty dong.danhSachLoi}">
                                                    <ul class="error-list-pill">
                                                        <c:forEach var="loi" items="${dong.danhSachLoi}">
                                                            <li class="error-item-tag">
                                                                <span class="material-symbols-outlined icon-xs" aria-hidden="true">warning</span>
                                                                <span><c:out value="${loi}" /></span>
                                                            </li>
                                                        </c:forEach>
                                                    </ul>
                                                </c:when>
                                                <c:when test="${dong.daNhap and not empty dong.matKhauTam}">
                                                    <div class="credential-display-wrap">
                                                        <span style="font-size: 0.8rem; color: #475569;">Máº­t kháº©u:</span>
                                                        <span class="credential-code"><c:out value="${dong.matKhauTam}" /></span>
                                                        <button type="button"
                                                                class="btn-copy-mini btn-copy-pwd"
                                                                data-pwd="<c:out value='${dong.matKhauTam}' />"
                                                                title="Sao chÃ©p máº­t kháº©u táº¡m"
                                                                aria-label="Sao chÃ©p máº­t kháº©u táº¡m">
                                                            <span class="material-symbols-outlined icon-xs" aria-hidden="true">content_copy</span>
                                                        </button>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="success-ready-tag" style="display: inline-flex; align-items: center; gap: 4px;">
                                                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">check_circle</span> Dá»¯ liá»‡u sáºµn sÃ ng nháº­p
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>

                        <!-- THÃ”NG BÃO KHI TÃŒM KIáº¾M HOáº¶C Lá»ŒC KHÃ”NG CÃ“ Káº¾T QUáº¢ -->
                        <div id="empty-filter-notice" class="empty-filter-state" style="display: none;">
                            <div class="empty-filter-icon"><span class="material-symbols-outlined icon-2xl" aria-hidden="true">search_off</span></div>
                            <div style="font-weight: 600; font-size: 0.95rem;">KhÃ´ng tÃ¬m tháº¥y dÃ²ng dá»¯ liá»‡u nÃ o phÃ¹ há»£p</div>
                            <div style="font-size: 0.85rem;">Vui lÃ²ng thá»­ tÃ¬m vá»›i tá»« khÃ³a khÃ¡c hoáº·c chuyá»ƒn tab lá»c.</div>
                        </div>
                    </div>

                    <!-- NÃšT THAO TÃC PHÃA DÆ¯á»šI Báº¢NG XEM TRÆ¯á»šC (S2-01 Review Fix) -->
                    <c:if test="${cheDo == 'xem-truoc'}">
                        <div class="preview-bottom-actions" style="margin-top: 20px; padding-top: 16px; border-top: 1px solid #e2e8f0; display: flex; justify-content: flex-end; gap: 12px; align-items: center;">
                            <a href="${pageContext.request.contextPath}/nguoi-dung/import" class="btn-action btn-action-outline" id="btn-chon-tep-khac-bottom" style="text-decoration: none; display: inline-flex; align-items: center; gap: 4px;">
                                <span class="material-symbols-outlined icon-sm" aria-hidden="true">close</span>
                                Chá»n tá»‡p khÃ¡c
                            </a>
                            <button type="button"
                                    id="btn-thuc-hien-nhap-bottom"
                                    class="btn-action btn-action-import"
                                    ${baoCao.soDongHopLe > 0 ? '' : 'disabled="disabled"'}
                                    style="${baoCao.soDongHopLe > 0 ? 'display: inline-flex; align-items: center; gap: 6px;' : 'display: inline-flex; align-items: center; gap: 6px; opacity: 0.5; cursor: not-allowed;'}">
                                <span class="material-symbols-outlined icon-sm" aria-hidden="true">check_circle</span>
                                Thá»±c hiá»‡n nháº­p
                            </button>
                        </div>
                    </c:if>

                </section>
            </c:if>

        </div>
    </main>

    <!-- MODAL XÃC NHáº¬N NHáº¬P Dá»® LIá»†U THÃ”NG MINH -->
    <div class="crm-modal-backdrop" id="modal-xac-nhan-nhap" role="dialog" aria-modal="true" aria-labelledby="modal-confirm-title">
        <div class="crm-modal-box">
            <div class="modal-header-flex">
                <div class="modal-icon-alert">
                    <span class="material-symbols-outlined icon-lg" aria-hidden="true">warning</span>
                </div>
                <div>
                    <h3 class="modal-title-text" id="modal-confirm-title">XÃ¡c nháº­n nháº­p danh sÃ¡ch ngÆ°á»i dÃ¹ng</h3>
                    <div style="font-size: 0.84rem; color: #64748B;">Há»‡ thá»‘ng sáº½ thá»±c hiá»‡n theo quy táº¯c Acceptance Criteria</div>
                </div>
            </div>

            <div class="modal-body-desc">
                Báº¡n Ä‘ang chuáº©n bá»‹ táº¡o tÃ i khoáº£n hÃ ng loáº¡t tá»« tá»‡p Excel vÃ o cÆ¡ sá»Ÿ dá»¯ liá»‡u CRM.
            </div>

            <div class="modal-summary-box">
                <div class="modal-summary-row">
                    <span style="color: #2E7D32; font-weight: 600; display: inline-flex; align-items: center; gap: 4px;">
                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">check_circle</span> Sá»‘ dÃ²ng há»£p lá»‡ sáº½ Ä‘Æ°á»£c nháº­p:
                    </span>
                    <strong id="modal-count-valid" style="color: #2E7D32;">${empty baoCao ? '-' : baoCao.soDongHopLe}</strong>
                </div>
                <div class="modal-summary-row">
                    <span style="color: #C62828; font-weight: 600; display: inline-flex; align-items: center; gap: 4px;">
                        <span class="material-symbols-outlined icon-xs" aria-hidden="true">cancel</span> Sá»‘ dÃ²ng lá»—i sáº½ tá»± Ä‘á»™ng bá» qua:
                    </span>
                    <strong id="modal-count-error" style="color: #C62828;">${empty baoCao ? '-' : baoCao.soDongLoi}</strong>
                </div>
            </div>

            <div class="modal-actions-flex">
                <button type="button" class="btn-action btn-action-outline" id="modal-btn-cancel">
                    Há»§y bá»
                </button>
                <button type="button" class="btn-action btn-action-import" id="modal-btn-confirm" style="display: inline-flex; align-items: center; gap: 6px;">
                    <span class="material-symbols-outlined icon-sm" aria-hidden="true">check_circle</span>
                    Äá»“ng Ã½ vÃ  Tiáº¿p tá»¥c nháº­p
                </button>
            </div>
        </div>
    </div>

    <!-- JavaScript chuyÃªn biá»‡t cho tÃ­nh nÄƒng S2-01 -->
    <script src="${pageContext.request.contextPath}/assets/js/nguoi-dung/import-excel.js"></script>
</body>
</html>
