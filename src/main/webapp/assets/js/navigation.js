/**
 * CRM BÁN HÀNG - NAVIGATION JAVASCRIPT (STORY S1-06)
 * Xử lý mở/đóng menu điều hướng linh hoạt và mượt mà trên màn hình nhỏ (360px)
 * và thiết bị di động bằng Vanilla JavaScript.
 */
document.addEventListener('DOMContentLoaded', function () {
    const btnToggle = document.getElementById('btn-menu-toggle');
    const btnClose = document.getElementById('btn-close-sidebar');
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
        // Khóa cuộn trang nền khi mở drawer trên điện thoại
        document.body.style.overflow = 'hidden';
    }

    function closeMenu() {
        sidebar.classList.remove('crm-sidebar-open');
        if (overlay) {
            overlay.classList.remove('active');
        }
        if (btnToggle) {
            btnToggle.setAttribute('aria-expanded', 'false');
        }
        document.body.style.overflow = '';
    }

    if (btnToggle) {
        btnToggle.addEventListener('click', function (e) {
            e.stopPropagation();
            if (sidebar.classList.contains('crm-sidebar-open')) {
                closeMenu();
            } else {
                openMenu();
            }
        });
    }

    if (btnClose) {
        btnClose.addEventListener('click', function (e) {
            e.stopPropagation();
            closeMenu();
        });
    }

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

    // Tự động đóng drawer khi người dùng xoay màn hình hoặc mở rộng > 768px
    window.addEventListener('resize', function () {
        if (window.innerWidth > 768 && sidebar.classList.contains('crm-sidebar-open')) {
            closeMenu();
        }
    });

    // Hỗ trợ đóng menu khi người dùng bấm vào một link điều hướng trên mobile 360px
    const navLinks = sidebar.querySelectorAll('.crm-menu-link');
    navLinks.forEach(function (link) {
        link.addEventListener('click', function () {
            if (window.innerWidth <= 768) {
                closeMenu();
            }
        });
    });
});
