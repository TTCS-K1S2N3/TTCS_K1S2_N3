<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Äá»•i Máº­t Kháº©u | CRM BÃ¡n HÃ ng</title>
    <meta name="description" content="Äá»•i máº­t kháº©u ngÆ°á»i dÃ¹ng vÃ  thu há»“i cÃ¡c phiÃªn Ä‘Äƒng nháº­p khÃ¡c Ä‘á»ƒ báº£o vá»‡ an toÃ n danh má»¥c khÃ¡ch hÃ ng trong há»‡ thá»‘ng CRM.">

    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">

    <!-- CSS Module -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth/doi-mat-khau.css">
</head>
<body class="crm-body">

    <!-- ===== TOP HEADER NAVIGATION ===== -->
    <header class="crm-header" role="banner">
        <div class="header-inner">
            <div class="header-brand">
                <a href="${pageContext.request.contextPath}/" class="brand-link" id="link-brand-home">
                    <span class="brand-logo-icon">
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
                            <circle cx="9" cy="7" r="4"></circle>
                            <path d="M22 21v-2a4 4 0 0 0-3-3.87"></path>
                            <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                        </svg>
                    </span>
                    <span class="brand-text">CRM BÃ¡n HÃ ng</span>
                </a>
                <span class="brand-badge">Báº£o máº­t</span>
            </div>

            <!-- Breadcrumb -->
            <nav class="crm-breadcrumb" aria-label="ÄÆ°á»ng dáº«n Ä‘iá»u hÆ°á»›ng">
                <ol class="breadcrumb-list">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/" id="crumb-trang-chu">Trang chá»§</a></li>
                    <li class="breadcrumb-separator" aria-hidden="true">/</li>
                    <li class="breadcrumb-item"><span class="breadcrumb-muted">TÃ i khoáº£n</span></li>
                    <li class="breadcrumb-separator" aria-hidden="true">/</li>
                    <li class="breadcrumb-item active" aria-current="page">Äá»•i máº­t kháº©u</li>
                </ol>
            </nav>

            <!-- User quick info -->
            <div class="header-user">
                <div class="user-avatar" id="header-user-avatar" aria-hidden="true">
                    <span>${not empty sessionScope.nguoiDung.hoTen ? sessionScope.nguoiDung.hoTen.substring(0,1).toUpperCase() : 'U'}</span>
                </div>
                <div class="user-details">
                    <span class="user-name" id="header-user-name">${not empty sessionScope.nguoiDung.hoTen ? sessionScope.nguoiDung.hoTen : 'NgÆ°á»i dÃ¹ng há»‡ thá»‘ng'}</span>
                    <span class="user-role-tag">${not empty sessionScope.nguoiDung.email ? sessionScope.nguoiDung.email : 'user@crm.vn'}</span>
                </div>
            </div>
        </div>
    </header>

    <!-- ===== MAIN CONTENT CONTAINER ===== -->
    <main class="main-wrapper" id="main-content">
        <div class="content-container">

            <!-- Page Title Section -->
            <section class="page-intro">
                <div class="intro-icon-wrapper" aria-hidden="true">
                    <span class="material-symbols-outlined icon-2xl">lock</span>
                </div>
                <div class="intro-text">
                    <h1 class="page-title" id="page-title">Äá»•i Máº­t Kháº©u Äang ÄÄƒng Nháº­p</h1>
                    <p class="page-subtitle">
                        Chá»§ Ä‘á»™ng báº£o vá»‡ danh má»¥c khÃ¡ch hÃ ng, thÃ´ng tin liÃªn há»‡ vÃ  cÆ¡ há»™i bÃ¡n hÃ ng báº±ng viá»‡c cáº­p nháº­t máº­t kháº©u Ä‘á»‹nh ká»³ vÃ  thu há»“i cÃ¡c phiÃªn Ä‘Äƒng nháº­p khÃ¡c.
                    </p>
                </div>
            </section>

            <!-- Server Notifications (náº¿u cÃ³ tá»« Controller) -->
            <div id="alert-server-error" class="alert alert-error ${empty thongBaoLoi ? 'd-none' : ''}" role="alert">
                <div class="alert-icon" aria-hidden="true">
                    <span class="material-symbols-outlined icon-sm">error</span>
                </div>
                <div class="alert-content">
                    <strong class="alert-heading">Äá»•i máº­t kháº©u khÃ´ng thÃ nh cÃ´ng</strong>
                    <p id="server-error-text" class="alert-message">${thongBaoLoi}</p>
                </div>
                <button type="button" class="btn-alert-close" aria-label="ÄÃ³ng thÃ´ng bÃ¡o lá»—i" onclick="this.closest('.alert').classList.add('d-none');"><span class="material-symbols-outlined icon-xs" aria-hidden="true">close</span></button>
            </div>

            <div id="alert-server-success" class="alert alert-success ${empty thongBaoThanhCong ? 'd-none' : ''}" role="alert">
                <div class="alert-icon" aria-hidden="true">
                    <span class="material-symbols-outlined icon-sm">check_circle</span>
                </div>
                <div class="alert-content">
                    <strong class="alert-heading">Äá»•i máº­t kháº©u thÃ nh cÃ´ng!</strong>
                    <p id="server-success-text" class="alert-message">${thongBaoThanhCong}</p>
                </div>
                <button type="button" class="btn-alert-close" aria-label="ÄÃ³ng thÃ´ng bÃ¡o thÃ nh cÃ´ng" onclick="this.closest('.alert').classList.add('d-none');"><span class="material-symbols-outlined icon-xs" aria-hidden="true">close</span></button>
            </div>

            <!-- Client-side Dynamic Feedback Alert -->
            <div id="alert-client-feedback" class="alert d-none" role="alert" aria-live="assertive">
                <div class="alert-icon" id="feedback-icon" aria-hidden="true"></div>
                <div class="alert-content">
                    <strong class="alert-heading" id="feedback-heading">ThÃ´ng bÃ¡o</strong>
                    <p class="alert-message" id="feedback-message"></p>
                </div>
                <button type="button" class="btn-alert-close" aria-label="ÄÃ³ng thÃ´ng bÃ¡o" id="btn-close-feedback"><span class="material-symbols-outlined icon-xs" aria-hidden="true">close</span></button>
            </div>

            <!-- Main Layout Grid: Form Column & Security Guidelines Column -->
            <div class="grid-layout">

                <!-- Left Column: Change Password Card Form -->
                <div class="card-form-wrapper">
                    <div class="crm-card">
                        <div class="card-header">
                            <h2 class="card-title">Cáº­p nháº­t thÃ´ng tin máº­t kháº©u</h2>
                            <p class="card-desc">Vui lÃ²ng Ä‘iá»n Ä‘áº§y Ä‘á»§ cÃ¡c thÃ´ng tin bÃªn dÆ°á»›i. CÃ¡c trÆ°á»ng cÃ³ dáº¥u (<span class="required-star">*</span>) lÃ  báº¯t buá»™c.</p>
                        </div>

                        <form id="form-doi-mat-khau" action="${pageContext.request.contextPath}/doi-mat-khau" method="POST" class="auth-form" novalidate autocomplete="off">
                            <!-- CSRF Token (náº¿u server há»— trá»£) -->
                            <input type="hidden" id="csrf-token" name="csrfToken" value="${csrfToken != null ? csrfToken : ''}" />

                            <!-- ===== TRÆ¯á»œNG 1: Máº¬T KHáº¨U HIá»†N Táº I (AC 1) ===== -->
                            <div class="form-group" id="group-mat-khau-hien-tai">
                                <label for="mat-khau-hien-tai" class="form-label">
                                    Máº­t kháº©u hiá»‡n táº¡i <span class="required-star">*</span>
                                </label>
                                <div class="input-wrapper">
                                    <span class="input-icon-left" aria-hidden="true">
                                        <span class="material-symbols-outlined icon-sm">lock</span>
                                    </span>
                                    <input
                                        type="password"
                                        id="mat-khau-hien-tai"
                                        name="matKhauHienTai"
                                        class="form-control"
                                        placeholder="Nháº­p máº­t kháº©u báº¡n Ä‘ang dÃ¹ng"
                                        autocomplete="current-password"
                                        required
                                        aria-required="true"
                                        aria-describedby="err-mat-khau-hien-tai hint-mat-khau-hien-tai"
                                    />
                                    <button
                                        type="button"
                                        id="btn-toggle-mat-khau-hien-tai"
                                        class="btn-toggle-pwd"
                                        aria-label="Hiá»‡n máº­t kháº©u hiá»‡n táº¡i"
                                        data-target="mat-khau-hien-tai"
                                        tabindex="0"
                                    >
                                        <span class="material-symbols-outlined eye-open icon-sm" aria-hidden="true">visibility</span>
                                        <span class="material-symbols-outlined eye-closed d-none icon-sm" aria-hidden="true">visibility_off</span>
                                    </button>
                                </div>
                                <span id="hint-mat-khau-hien-tai" class="field-hint">Báº¯t buá»™c pháº£i nháº­p máº­t kháº©u hiá»‡n táº¡i Ä‘á»ƒ xÃ¡c thá»±c báº¡n lÃ  chá»§ tÃ i khoáº£n.</span>
                                <div id="err-mat-khau-hien-tai" class="field-error" aria-live="polite"></div>
                            </div>

                            <hr class="form-divider" />

                            <!-- ===== TRÆ¯á»œNG 2: Máº¬T KHáº¨U Má»šI (AC 2) ===== -->
                            <div class="form-group" id="group-mat-khau-moi">
                                <label for="mat-khau-moi" class="form-label">
                                    Máº­t kháº©u má»›i <span class="required-star">*</span>
                                </label>
                                <div class="input-wrapper">
                                    <span class="input-icon-left" aria-hidden="true">
                                        <span class="material-symbols-outlined icon-sm">key</span>
                                    </span>
                                    <input
                                        type="password"
                                        id="mat-khau-moi"
                                        name="matKhauMoi"
                                        class="form-control"
                                        placeholder="Tá»‘i thiá»ƒu 8 kÃ½ tá»±, gá»“m cáº£ chá»¯ vÃ  sá»‘"
                                        autocomplete="new-password"
                                        required
                                        minlength="8"
                                        aria-required="true"
                                        aria-describedby="err-mat-khau-moi checklist-mat-khau-moi"
                                    />
                                    <button
                                        type="button"
                                        id="btn-toggle-mat-khau-moi"
                                        class="btn-toggle-pwd"
                                        aria-label="Hiá»‡n máº­t kháº©u má»›i"
                                        data-target="mat-khau-moi"
                                        tabindex="0"
                                    >
                                        <span class="material-symbols-outlined eye-open icon-sm" aria-hidden="true">visibility</span>
                                        <span class="material-symbols-outlined eye-closed d-none icon-sm" aria-hidden="true">visibility_off</span>
                                    </button>
                                </div>
                                <div id="err-mat-khau-moi" class="field-error" aria-live="polite"></div>

                                <!-- Thanh Ä‘o Ä‘á»™ máº¡nh máº­t kháº©u (Password Strength Meter) -->
                                <div class="strength-meter-container" id="strength-container" aria-live="polite">
                                    <div class="strength-header">
                                        <span class="strength-title">Äá»™ máº¡nh máº­t kháº©u:</span>
                                        <span class="strength-level" id="strength-text">ChÆ°a nháº­p</span>
                                    </div>
                                    <div class="strength-bars" aria-hidden="true">
                                        <span class="bar" id="bar-1"></span>
                                        <span class="bar" id="bar-2"></span>
                                        <span class="bar" id="bar-3"></span>
                                        <span class="bar" id="bar-4"></span>
                                    </div>
                                </div>

                                <!-- TiÃªu chuáº©n máº­t kháº©u (Checklist) -->
                                <div class="password-checklist-card" id="checklist-mat-khau-moi">
                                    <span class="checklist-heading">Quy Ä‘á»‹nh máº­t kháº©u má»›i:</span>
                                    <ul class="checklist-items">
                                        <li class="checklist-item" id="rule-min-length" data-rule="length">
                                            <span class="rule-icon" aria-hidden="true"><span class="material-symbols-outlined icon-xs">close</span></span>
                                            <span class="rule-label">Tá»‘i thiá»ƒu <strong>8 kÃ½ tá»±</strong></span>
                                        </li>
                                        <li class="checklist-item" id="rule-has-letter" data-rule="letter">
                                            <span class="rule-icon" aria-hidden="true"><span class="material-symbols-outlined icon-xs">close</span></span>
                                            <span class="rule-label">Chá»©a Ã­t nháº¥t <strong>1 chá»¯ cÃ¡i</strong> (a-z hoáº·c A-Z)</span>
                                        </li>
                                        <li class="checklist-item" id="rule-has-number" data-rule="number">
                                            <span class="rule-icon" aria-hidden="true"><span class="material-symbols-outlined icon-xs">close</span></span>
                                            <span class="rule-label">Chá»©a Ã­t nháº¥t <strong>1 chá»¯ sá»‘</strong> (0-9)</span>
                                        </li>
                                        <li class="checklist-item" id="rule-diff-current" data-rule="different">
                                            <span class="rule-icon" aria-hidden="true"><span class="material-symbols-outlined icon-xs">close</span></span>
                                            <span class="rule-label">KhÃ´ng trÃ¹ng vá»›i <strong>máº­t kháº©u hiá»‡n táº¡i</strong></span>
                                        </li>
                                    </ul>
                                </div>
                            </div>

                            <!-- ===== TRÆ¯á»œNG 3: XÃC NHáº¬N Máº¬T KHáº¨U Má»šI ===== -->
                            <div class="form-group" id="group-xac-nhan-mat-khau">
                                <label for="xac-nhan-mat-khau" class="form-label">
                                    XÃ¡c nháº­n máº­t kháº©u má»›i <span class="required-star">*</span>
                                </label>
                                <div class="input-wrapper">
                                    <span class="input-icon-left" aria-hidden="true">
                                        <span class="material-symbols-outlined icon-sm">lock_clock</span>
                                    </span>
                                    <input
                                        type="password"
                                        id="xac-nhan-mat-khau"
                                        name="xacNhanMatKhau"
                                        class="form-control"
                                        placeholder="Nháº­p láº¡i máº­t kháº©u má»›i vá»«a Ä‘áº·t"
                                        autocomplete="new-password"
                                        required
                                        aria-required="true"
                                        aria-describedby="err-xac-nhan-mat-khau match-status"
                                    />
                                    <button
                                        type="button"
                                        id="btn-toggle-xac-nhan-mat-khau"
                                        class="btn-toggle-pwd"
                                        aria-label="Hiá»‡n xÃ¡c nháº­n máº­t kháº©u má»›i"
                                        data-target="xac-nhan-mat-khau"
                                        tabindex="0"
                                    >
                                        <span class="material-symbols-outlined eye-open icon-sm" aria-hidden="true">visibility</span>
                                        <span class="material-symbols-outlined eye-closed d-none icon-sm" aria-hidden="true">visibility_off</span>
                                    </button>
                                </div>
                                <div id="match-status" class="match-status" aria-live="polite"></div>
                                <div id="err-xac-nhan-mat-khau" class="field-error" aria-live="polite"></div>
                            </div>

                            <hr class="form-divider" />

                            <!-- ===== TRÆ¯á»œNG 4: THU Há»’I CÃC PHIÃŠN ÄÄ‚NG NHáº¬P KHÃC (AC 3) ===== -->
                            <div class="session-revocation-card" id="card-thu-hoi-phien">
                                <div class="switch-box">
                                    <label class="custom-switch" for="thu-hoi-phien-khac">
                                        <input
                                            type="checkbox"
                                            id="thu-hoi-phien-khac"
                                            name="thuHoiPhienKhac"
                                            value="true"
                                            checked
                                            class="switch-checkbox"
                                            aria-describedby="desc-thu-hoi-phien"
                                        />
                                        <span class="switch-slider" aria-hidden="true"></span>
                                    </label>
                                    <div class="switch-content">
                                        <div class="switch-title-row">
                                            <label for="thu-hoi-phien-khac" class="switch-title">
                                                Thu há»“i cÃ¡c phiÃªn Ä‘Äƒng nháº­p khÃ¡c
                                            </label>
                                            <span class="badge-recommended">Khuyáº¿n nghá»‹ báº£o máº­t</span>
                                        </div>
                                        <p id="desc-thu-hoi-phien" class="switch-desc">
                                            Tá»± Ä‘á»™ng Ä‘Äƒng xuáº¥t tÃ i khoáº£n khá»i táº¥t cáº£ cÃ¡c trÃ¬nh duyá»‡t, mÃ¡y tÃ­nh, Ä‘iá»‡n thoáº¡i khÃ¡c ngay sau khi Ä‘á»•i máº­t kháº©u thÃ nh cÃ´ng. Chá»‰ duy trÃ¬ phiÃªn lÃ m viá»‡c hiá»‡n táº¡i trÃªn thiáº¿t bá»‹ nÃ y.
                                        </p>
                                    </div>
                                </div>

                                <!-- Session warning tip -->
                                <div class="session-notice">
                                    <span class="notice-icon" aria-hidden="true">
                                        <span class="material-symbols-outlined icon-xs">info</span>
                                    </span>
                                    <span class="notice-text">
                                        Náº¿u báº¡n nghi ngá» máº­t kháº©u Ä‘Ã£ bá»‹ lá»™ hoáº·c vá»«a Ä‘Äƒng nháº­p tá»« mÃ¡y tÃ­nh cÃ´ng cá»™ng, tÃ­nh nÄƒng nÃ y Ä‘áº£m báº£o khÃ´ng ai khÃ¡c cÃ³ thá»ƒ tiáº¿p tá»¥c truy cáº­p dá»¯ liá»‡u khÃ¡ch hÃ ng cá»§a báº¡n.
                                    </span>
                                </div>
                            </div>

                            <!-- ===== FORM ACTIONS ===== -->
                            <div class="form-actions">
                                <button
                                    type="submit"
                                    id="btn-submit-doi-mat-khau"
                                    class="btn btn-primary btn-submit"
                                >
                                    <span class="btn-spinner d-none" id="btn-spinner" aria-hidden="true"></span>
                                    <span class="btn-icon" id="btn-icon" aria-hidden="true">
                                        <span class="material-symbols-outlined icon-sm">save</span>
                                    </span>
                                    <span class="btn-text" id="btn-text">Äá»•i Máº­t Kháº©u vÃ  Cáº­p Nháº­t Báº£o Máº­t</span>
                                </button>

                                <a
                                    href="${pageContext.request.contextPath}/"
                                    id="btn-huy-doi-mat-khau"
                                    class="btn btn-secondary"
                                >
                                    Há»§y bá»
                                </a>
                            </div>

                        </form>
                    </div>
                </div>

                <!-- Right Column: Security Sidebar & Guidelines -->
                <aside class="sidebar-wrapper" aria-label="ThÃ´ng tin báº£o máº­t bá»• sung">

                    <!-- Security Tips Card -->
                    <div class="sidebar-card card-tips">
                        <div class="sidebar-card-header">
                            <span class="sidebar-icon-wrapper" aria-hidden="true">
                                <span class="material-symbols-outlined icon-sm">shield</span>
                            </span>
                            <h3 class="sidebar-title">Báº£o Vá»‡ Danh Má»¥c KhÃ¡ch HÃ ng</h3>
                        </div>
                        <div class="sidebar-card-body">
                            <p class="tips-intro">
                                Danh má»¥c khÃ¡ch hÃ ng, sá»‘ Ä‘iá»‡n thoáº¡i, bÃ¡o giÃ¡ vÃ  doanh sá»‘ lÃ  tÃ i sáº£n quan trá»ng. Viá»‡c giá»¯ máº­t kháº©u an toÃ n giÃºp báº¡n:
                            </p>
                            <ul class="tips-list">
                                <li>
                                    <strong>TrÃ¡nh rÃ² rá»‰ dá»¯ liá»‡u khÃ¡ch hÃ ng</strong> sang Ä‘á»‘i thá»§ hoáº·c cÃ¡ nhÃ¢n khÃ´ng cÃ³ tháº©m quyá»n.
                                </li>
                                <li>
                                    <strong>NgÄƒn cháº·n giáº£ máº¡o thao tÃ¡c</strong> nhÆ° chuyá»ƒn Ä‘á»•i ngÆ°á»i phá»¥ trÃ¡ch hoáº·c sá»­a Ä‘á»•i bÃ¡o giÃ¡ sai lá»‡ch.
                                </li>
                                <li>
                                    <strong>Äáº£m báº£o tÃ­nh chá»‹u trÃ¡ch nhiá»‡m</strong> cho tá»«ng hoáº¡t Ä‘á»™ng bÃ¡n hÃ ng ghi nháº­n trÃªn há»‡ thá»‘ng.
                                </li>
                            </ul>
                        </div>
                    </div>

                    <!-- Current Session Status Card -->
                    <div class="sidebar-card card-session-info">
                        <div class="sidebar-card-header">
                            <span class="sidebar-icon-wrapper icon-session" aria-hidden="true">
                                <span class="material-symbols-outlined icon-sm">devices</span>
                            </span>
                            <h3 class="sidebar-title">PhiÃªn ÄÄƒng Nháº­p Cá»§a Báº¡n</h3>
                        </div>
                        <div class="sidebar-card-body">
                            <div class="session-item current">
                                <div class="session-dot active" aria-hidden="true"></div>
                                <div class="session-info">
                                    <span class="session-device">Thiáº¿t bá»‹ hiá»‡n táº¡i</span>
                                    <span class="session-status">Äang hoáº¡t Ä‘á»™ng &bull; ÄÆ°á»£c giá»¯ láº¡i</span>
                                </div>
                            </div>
                            <div class="session-item other">
                                <div class="session-dot revoked" aria-hidden="true"></div>
                                <div class="session-info">
                                    <span class="session-device">CÃ¡c phiÃªn / thiáº¿t bá»‹ khÃ¡c</span>
                                    <span class="session-status text-warning">Sáº½ thu há»“i khi Ä‘á»•i máº­t kháº©u</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Password Best Practices Card -->
                    <div class="sidebar-card card-best-practices">
                        <div class="sidebar-card-header">
                            <span class="sidebar-icon-wrapper icon-idea" aria-hidden="true">
                                <span class="material-symbols-outlined icon-sm">lightbulb</span>
                            </span>
                            <h3 class="sidebar-title">Gá»£i Ã Máº­t Kháº©u Máº¡nh</h3>
                        </div>
                        <div class="sidebar-card-body">
                            <ul class="best-practice-list">
                                <li>NÃªn káº¿t há»£p cáº£ chá»¯ hoa, chá»¯ thÆ°á»ng, sá»‘ vÃ  kÃ½ tá»± Ä‘áº·c biá»‡t (!@#$%^&*).</li>
                                <li>KhÃ´ng sá»­ dá»¥ng thÃ´ng tin dá»… Ä‘oÃ¡n nhÆ° ngÃ y sinh, sá»‘ Ä‘iá»‡n thoáº¡i hoáº·c biá»ƒn sá»‘ xe.</li>
                                <li>KhÃ´ng dÃ¹ng láº¡i máº­t kháº©u cá»§a email cÃ¡ nhÃ¢n hoáº·c tÃ i khoáº£n máº¡ng xÃ£ há»™i.</li>
                            </ul>
                        </div>
                    </div>

                </aside>

            </div><%-- end grid-layout --%>

        </div><%-- end content-container --%>
    </main>

    <!-- ===== FOOTER ===== -->
    <footer class="crm-footer" role="contentinfo">
        <div class="footer-inner">
            <p>&copy; 2026 CRM BÃ¡n HÃ ng - NhÃ³m 10. ToÃ n bá»™ thÃ´ng tin Ä‘Æ°á»£c báº£o máº­t theo quy Ä‘á»‹nh.</p>
        </div>
    </footer>

    <!-- JavaScript Module -->
    <script src="${pageContext.request.contextPath}/assets/js/auth/doi-mat-khau.js"></script>
</body>
</html>
