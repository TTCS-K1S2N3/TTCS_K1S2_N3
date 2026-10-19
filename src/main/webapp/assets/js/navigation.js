/**
 * CRM BÁN HÀNG - NAVIGATION JAVASCRIPT (STORY S1-06)
 * Xử lý mở/đóng menu điều hướng linh hoạt, tối ưu tương tác touch/swipe
 * cho màn hình 360px và thiết bị di động bằng Vanilla JavaScript chuẩn mực.
 */
document.addEventListener('DOMContentLoaded', function () {
    const btnToggle = document.getElementById('btn-menu-toggle');
    const btnClose = document.getElementById('btn-close-sidebar');
    const btnMobileAvatar = document.getElementById('btn-mobile-user-profile');
    const sidebar = document.getElementById('crm-sidebar');
    const overlay = document.getElementById('crm-sidebar-overlay');

    if (!sidebar) {
        return;
    }

    function openMenu() {
        sidebar.classList.add('crm-sidebar-open');
        if (overlay) {
            overlay.classList.add('active');
        }
        if (btnToggle) {
            btnToggle.setAttribute('aria-expanded', 'true');
        }
        document.body.classList.add('crm-drawer-open');

        // Accessibility: đưa focus vào nút đóng hoặc mục đầu tiên
        if (btnClose) {
            setTimeout(function () {
                btnClose.focus();
            }, 100);
        }
    }

    function closeMenu() {
        sidebar.classList.remove('crm-sidebar-open');
        if (overlay) {
            overlay.classList.remove('active');
        }
        if (btnToggle) {
            btnToggle.setAttribute('aria-expanded', 'false');
            btnToggle.focus();
        }
        document.body.classList.remove('crm-drawer-open');
    }

    function toggleMenu() {
        if (sidebar.classList.contains('crm-sidebar-open')) {
            closeMenu();
        } else {
            openMenu();
        }
    }

    // Toggle qua nút Hamburger
    if (btnToggle) {
        btnToggle.addEventListener('click', function (e) {
            e.stopPropagation();
            toggleMenu();
        });
    }

    // Toggle qua Avatar trên thanh top bar điện thoại
    if (btnMobileAvatar) {
        btnMobileAvatar.addEventListener('click', function (e) {
            e.stopPropagation();
            toggleMenu();
        });
    }

    // Đóng drawer qua nút đóng [X]
    if (btnClose) {
        btnClose.addEventListener('click', function (e) {
            e.stopPropagation();
            closeMenu();
        });
    }

    // Đóng drawer khi click ra backdrop overlay
    if (overlay) {
        overlay.addEventListener('click', function () {
            closeMenu();
        });
    }

    // Đóng drawer khi nhấn phím ESC
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && sidebar.classList.contains('crm-sidebar-open')) {
            closeMenu();
        }
    });

    // Tự động đóng drawer khi màn hình mở rộng > 768px (trở về giao diện desktop)
    window.addEventListener('resize', function () {
        if (window.innerWidth > 768 && sidebar.classList.contains('crm-sidebar-open')) {
            closeMenu();
        }
    });

    // Đóng menu khi bấm vào bất kỳ liên kết điều hướng nào trên màn hình nhỏ
    const navLinks = sidebar.querySelectorAll('.crm-menu-link');
    navLinks.forEach(function (link) {
        link.addEventListener('click', function () {
            if (window.innerWidth <= 768) {
                closeMenu();
            }
        });
    });

    // ====================================================================
    // CỬ CHỈ CHẠM VUỐT (TOUCH SWIPE GESTURES CHO MÀN HÌNH 360PX)
    // ====================================================================
    let touchStartX = 0;
    let touchStartY = 0;
    let touchEndX = 0;
    let touchEndY = 0;

    document.addEventListener('touchstart', function (e) {
        touchStartX = e.changedTouches[0].screenX;
        touchStartY = e.changedTouches[0].screenY;
    }, { passive: true });

    document.addEventListener('touchend', function (e) {
        touchEndX = e.changedTouches[0].screenX;
        touchEndY = e.changedTouches[0].screenY;
        handleSwipeGesture();
    }, { passive: true });

    function handleSwipeGesture() {
        const deltaX = touchEndX - touchStartX;
        const deltaY = touchEndY - touchStartY;

        // Chỉ xử lý nếu cử chỉ vuốt chủ yếu theo phương ngang
        if (Math.abs(deltaY) > 60) {
            return;
        }

        // 1. Vuốt sang trái (Swipe Left) khi menu đang mở -> Đóng menu
        if (sidebar.classList.contains('crm-sidebar-open') && deltaX < -45) {
            closeMenu();
        }

        // 2. Vuốt sang phải (Swipe Right) từ mép trái màn hình (x < 35px) -> Mở menu thuận tiện
        if (!sidebar.classList.contains('crm-sidebar-open') && touchStartX < 35 && deltaX > 55) {
            openMenu();
        }
    }

    // ====================================================================
    // HỖ TRỢ ĐIỀU KHIỂN TỪ TRANG KIỂM THỬ (SIMULATOR CROSS-WINDOW)
    // ====================================================================
    window.addEventListener('message', function (event) {
        if (!event.data) return;
        if (event.data === 'toggle-menu' || event.data.action === 'toggle-menu') {
            toggleMenu();
        } else if (event.data === 'open-menu' || event.data.action === 'open-menu') {
            openMenu();
        } else if (event.data === 'close-menu' || event.data.action === 'close-menu') {
            closeMenu();
        }
    });

    // ====================================================================
    // TRÌNH ĐIỀU KHIỂN MÔ PHỎNG 360PX TRÊN TRANG DEMO
    // ====================================================================
    const btnSimToggleMenu = document.getElementById('btn-simulator-toggle-menu');
    const simIframe = document.querySelector('.crm-simulator-iframe');
    const simPhone = document.getElementById('simulator-phone');
    const sizeButtons = document.querySelectorAll('.crm-btn-size-preset');

    if (btnSimToggleMenu && simIframe) {
        btnSimToggleMenu.addEventListener('click', function () {
            try {
                simIframe.contentWindow.postMessage('toggle-menu', '*');
            } catch (err) {
                console.warn('Không thể gửi message đến iframe:', err);
            }
        });
    }

    if (sizeButtons.length > 0 && simPhone) {
        sizeButtons.forEach(function (btn) {
            btn.addEventListener('click', function () {
                sizeButtons.forEach(function (b) { b.classList.remove('active'); });
                btn.classList.add('active');
                const targetWidth = btn.getAttribute('data-width');
                const targetHeight = btn.getAttribute('data-height');

                if (targetWidth === '100%') {
                    simPhone.style.width = '100%';
                    simPhone.style.maxWidth = '100%';
                    simPhone.style.height = '600px';
                    simPhone.style.borderRadius = '16px';
                } else {
                    simPhone.style.width = targetWidth + 'px';
                    simPhone.style.maxWidth = targetWidth + 'px';
                    simPhone.style.height = targetHeight + 'px';
                    simPhone.style.borderRadius = '36px';
                }

                const indicator = document.getElementById('simulator-dimensions-badge');
                if (indicator) {
                    indicator.textContent = targetWidth === '100%' ? 'Đáp ứng toàn màn hình' : targetWidth + ' × ' + targetHeight + ' px';
                }
            });
        });
    }
});

