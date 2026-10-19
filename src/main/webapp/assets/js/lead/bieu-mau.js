/**
 * CRM BÁN HÀNG - JAVASCRIPT QUẢN LÝ BIỂU MẪU LEAD WEBSITE (STORY S4-01)
 * Xử lý:
 * - Lấy mã nhúng Iframe/Script/URL từ server và mở modal (AC1)
 * - Sao chép mã nhúng vào clipboard kèm toast thông báo
 * - Tìm kiếm và lọc biểu mẫu nhanh trên bảng dữ liệu (Client-side fast filter)
 * - Đóng mở modal với phím ESC và phím Tab
 */

(function () {
    'use strict';

    // Khởi tạo các sự kiện khi DOM tải xong
    document.addEventListener('DOMContentLoaded', function () {
        khoiTaoTimKiemVaLoc();
        khoiTaoModalMaNhung();
    });

    /**
     * 1. Xử lý tìm kiếm và lọc biểu mẫu trên bảng
     */
    function khoiTaoTimKiemVaLoc() {
        var searchInput = document.getElementById('bmlSearchInput');
        var filterStatus = document.getElementById('bmlFilterStatus');
        var table = document.getElementById('bmlTable');

        if (!table) {
            return;
        }

        var rows = table.querySelectorAll('tbody tr');
        var emptyRow = document.getElementById('bmlRowEmptySearch');

        function applyFilter() {
            var keyword = searchInput ? searchInput.value.trim().toLowerCase() : '';
            var status = filterStatus ? filterStatus.value : 'ALL';
            var visibleCount = 0;

            rows.forEach(function (row) {
                if (row.id === 'bmlRowEmptySearch' || row.id === 'bmlRowEmptyData') {
                    return;
                }

                var maBm = (row.getAttribute('data-ma') || '').toLowerCase();
                var tenBm = (row.getAttribute('data-ten') || '').toLowerCase();
                var trangThai = row.getAttribute('data-trang-thai') || '';

                var matchKeyword = !keyword || maBm.indexOf(keyword) !== -1 || tenBm.indexOf(keyword) !== -1;
                var matchStatus = (status === 'ALL') || (trangThai === status);

                if (matchKeyword && matchStatus) {
                    row.style.display = '';
                    visibleCount++;
                } else {
                    row.style.display = 'none';
                }
            });

            if (emptyRow) {
                emptyRow.style.display = (visibleCount === 0 && rows.length > 0) ? '' : 'none';
            }
        }

        if (searchInput) {
            searchInput.addEventListener('input', applyFilter);
        }
        if (filterStatus) {
            filterStatus.addEventListener('change', applyFilter);
        }
    }

    /**
     * 2. Xử lý Modal hiển thị mã nhúng (AC1)
     */
    function khoiTaoModalMaNhung() {
        var modal = document.getElementById('modalMaNhung');
        if (!modal) {
            return;
        }

        // Đóng modal khi bấm vào nền ngoài
        modal.addEventListener('click', function (e) {
            if (e.target === modal) {
                dongModalMaNhung();
            }
        });

        // Đóng modal khi bấm ESC
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape' && modal.style.display === 'flex') {
                dongModalMaNhung();
            }
        });
    }

    /**
     * Mở modal và gọi API lấy mã nhúng
     */
    window.hienThiMaNhung = function (id) {
        var modal = document.getElementById('modalMaNhung');
        var modalTitle = document.getElementById('modalMaNhungTitle');
        var codeIframe = document.getElementById('codeIframe');
        var codeScript = document.getElementById('codeScript');
        var codeUrl = document.getElementById('codeUrl');
        var contextPath = window.__CRM_CONTEXT_PATH || '';

        if (!modal) return;

        // Reset nội dung
        if (codeIframe) codeIframe.textContent = 'Đang tải mã nhúng...';
        if (codeScript) codeScript.textContent = 'Đang tải...';
        if (codeUrl) codeUrl.textContent = 'Đang tải...';

        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';

        fetch(contextPath + '/bieu-mau/ma-nhung?id=' + encodeURIComponent(id))
            .then(function (res) {
                if (!res.ok) {
                    throw new Error('Mã phản hồi HTTP: ' + res.status);
                }
                return res.json();
            })
            .then(function (data) {
                if (data.thanhCong) {
                    if (modalTitle) {
                        modalTitle.textContent = 'Mã Nhúng: ' + data.tenBieuMau;
                    }
                    if (codeIframe) codeIframe.textContent = data.maIframe;
                    if (codeScript) codeScript.textContent = data.maScript;
                    if (codeUrl) codeUrl.textContent = data.duongDanTrucTiep;
                } else {
                    hienThiToast('Lỗi: ' + (data.thongBao || 'Không thể lấy mã nhúng'), 'danger');
                    dongModalMaNhung();
                }
            })
            .catch(function (err) {
                hienThiToast('Lỗi kết nối khi lấy mã nhúng: ' + err.message, 'danger');
                dongModalMaNhung();
            });
    };

    /**
     * Đóng modal
     */
    window.dongModalMaNhung = function () {
        var modal = document.getElementById('modalMaNhung');
        if (modal) {
            modal.style.display = 'none';
            document.body.style.overflow = '';
        }
    };

    /**
     * Sao chép mã nhúng vào Clipboard
     */
    window.saoChepMaNhung = function (elementId, loaiMa) {
        var el = document.getElementById(elementId);
        if (!el) return;

        var text = el.textContent || '';
        if (!text || text === 'Đang tải...') return;

        if (navigator.clipboard && window.isSecureContext) {
            navigator.clipboard.writeText(text).then(function () {
                hienThiToast('Đã sao chép ' + (loaiMa || 'mã nhúng') + ' vào bộ nhớ tạm!', 'success');
            }).catch(function () {
                fallbackCopyText(text, loaiMa);
            });
        } else {
            fallbackCopyText(text, loaiMa);
        }
    };

    function fallbackCopyText(text, loaiMa) {
        var textarea = document.createElement('textarea');
        textarea.value = text;
        textarea.style.position = 'fixed';
        textarea.style.left = '-9999px';
        document.body.appendChild(textarea);
        textarea.select();
        try {
            document.execCommand('copy');
            hienThiToast('Đã sao chép ' + (loaiMa || 'mã nhúng') + ' vào bộ nhớ tạm!', 'success');
        } catch (err) {
            hienThiToast('Không thể tự động sao chép. Vui lòng chọn và nhấn Ctrl+C', 'warning');
        }
        document.body.removeChild(textarea);
    }

    /**
     * Toast notification tự động biến mất
     */
    function hienThiToast(message, type) {
        var toast = document.getElementById('bmlToast');
        if (!toast) {
            toast = document.createElement('div');
            toast.id = 'bmlToast';
            toast.className = 'bml-toast';
            document.body.appendChild(toast);
        }

        var iconName = 'check_circle';
        if (type === 'danger') {
            iconName = 'error';
            toast.style.backgroundColor = '#991b1b';
        } else if (type === 'warning') {
            iconName = 'warning';
            toast.style.backgroundColor = '#92400e';
        } else {
            iconName = 'check_circle';
            toast.style.backgroundColor = '#0f172a';
        }

        toast.innerHTML = '<span class="material-symbols-outlined" style="font-size:20px;" aria-hidden="true">' + iconName + '</span>' +
                          '<span>' + escapeHtml(message) + '</span>';

        toast.classList.add('show');

        clearTimeout(window.__bmlToastTimer);
        window.__bmlToastTimer = setTimeout(function () {
            toast.classList.remove('show');
        }, 3200);
    }

    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }
})();
