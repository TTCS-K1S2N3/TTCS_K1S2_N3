/**
 * JavaScript cho màn hình Nhật Ký Thay Đổi Dữ Liệu Nhạy Cảm (Story S2-04)
 * Hệ thống Quản lý Khách hàng & Quy trình Bán hàng CRM
 */

document.addEventListener('DOMContentLoaded', function () {
    khoiTaoBoLocNhanh();
    khoiTaoValidationNgay();
    khoiTaoNutXoaTuKhoa();
    khoiTaoSuKienModal();
    khoiTaoNutXuatDuLieu();
});

/**
 * 1. Khởi tạo các nút chọn mốc thời gian nhanh (Hôm nay, 7 ngày, Tháng này, Quý 3 chốt số)
 */
function khoiTaoBoLocNhanh() {
    const quickButtons = document.querySelectorAll('.quick-btn');
    const quickInput = document.getElementById('quickPeriod');
    const form = document.getElementById('crm-filter-form');
    const tuNgayInput = document.getElementById('filter-tu-ngay');
    const denNgayInput = document.getElementById('filter-den-ngay');

    if (!quickButtons || !form) return;

    quickButtons.forEach(btn => {
        btn.addEventListener('click', function () {
            const period = this.getAttribute('data-period');
            if (quickInput) {
                quickInput.value = period;
            }

            const today = new Date();
            const yyyy = today.getFullYear();
            const mm = String(today.getMonth() + 1).padStart(2, '0');
            const dd = String(today.getDate()).padStart(2, '0');
            const todayStr = `${yyyy}-${mm}-${dd}`;

            if (period === 'today') {
                if (tuNgayInput) tuNgayInput.value = todayStr;
                if (denNgayInput) denNgayInput.value = todayStr;
            } else if (period === 'week') {
                const past7 = new Date();
                past7.setDate(today.getDate() - 7);
                const past7Str = `${past7.getFullYear()}-${String(past7.getMonth() + 1).padStart(2, '0')}-${String(past7.getDate()).padStart(2, '0')}`;
                if (tuNgayInput) tuNgayInput.value = past7Str;
                if (denNgayInput) denNgayInput.value = todayStr;
            } else if (period === 'month') {
                const monthStart = `${yyyy}-${mm}-01`;
                if (tuNgayInput) tuNgayInput.value = monthStart;
                if (denNgayInput) denNgayInput.value = todayStr;
            } else if (period === 'quarter') {
                // Quý 3 (01/07 - 30/09) - Mốc chốt số quan trọng theo bối cảnh Story S2-04
                if (tuNgayInput) tuNgayInput.value = `${yyyy}-07-01`;
                if (denNgayInput) denNgayInput.value = `${yyyy}-09-30`;
            } else if (period === 'all') {
                if (tuNgayInput) tuNgayInput.value = '';
                if (denNgayInput) denNgayInput.value = '';
            }

            // Đánh dấu active button
            quickButtons.forEach(b => b.classList.remove('active'));
            this.classList.add('active');

            // Tự động submit để lọc ngay lập tức
            form.submit();
        });
    });
}

/**
 * 2. Kiểm tra tính hợp lệ của khoảng thời gian trước khi gửi form (Client-side validation)
 */
function khoiTaoValidationNgay() {
    const form = document.getElementById('crm-filter-form');
    const tuNgayInput = document.getElementById('filter-tu-ngay');
    const denNgayInput = document.getElementById('filter-den-ngay');

    if (!form || !tuNgayInput || !denNgayInput) return;

    form.addEventListener('submit', function (e) {
        const tuNgay = tuNgayInput.value.trim();
        const denNgay = denNgayInput.value.trim();

        if (tuNgay && denNgay) {
            const dateTu = new Date(tuNgay);
            const dateDen = new Date(denNgay);

            if (dateTu > dateDen) {
                e.preventDefault();
                alert('Khoảng thời gian không hợp lệ: "Từ ngày" (' + tuNgay + ') không được lớn hơn "Đến ngày" (' + denNgay + ')!');
                tuNgayInput.focus();
            }
        }
    });
}

/**
 * 3. Nút xóa nhanh từ khóa tìm kiếm
 */
function khoiTaoNutXoaTuKhoa() {
    const btnClear = document.getElementById('btn-clear-keyword');
    const searchInput = document.getElementById('filter-tu-khoa');
    const form = document.getElementById('crm-filter-form');

    if (btnClear && searchInput && form) {
        btnClear.addEventListener('click', function () {
            searchInput.value = '';
            form.submit();
        });
    }
}

/**
 * 4. Thay đổi số lượng bản ghi hiển thị trên trang
 */
function doiSoBanGhiTrenTrang(soLuong) {
    const url = new URL(window.location.href);
    url.searchParams.set('soBanGhi', soLuong);
    url.searchParams.set('trang', '1'); // Quay về trang 1
    window.location.href = url.toString();
}

/**
 * 5. Quản lý Modal Chi Tiết Truy Vết So Sánh (Diff Inspector)
 */
let currentModalLogCode = '';

function moModalChiTiet(logId) {
    const modal = document.getElementById('audit-detail-modal');
    const spinner = document.getElementById('modal-loading-spinner');
    const realContent = document.getElementById('modal-real-content');

    if (!modal) return;

    modal.classList.add('open');
    modal.setAttribute('aria-hidden', 'false');
    document.body.style.overflow = 'hidden'; // Khóa cuộn trang

    if (spinner) spinner.style.display = 'flex';
    if (realContent) realContent.style.display = 'none';

    // Gọi API lấy dữ liệu JSON chi tiết
    const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1)) || '';
    const fetchUrl = `${contextPath}/nhat-ky-thay-doi/chi-tiet?id=${logId}`;

    fetch(fetchUrl, {
        headers: { 'Accept': 'application/json' }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Lỗi tải dữ liệu chi tiết nhật ký: ' + response.statusText);
        }
        return response.json();
    })
    .then(data => {
        renderModalData(data);
        if (spinner) spinner.style.display = 'none';
        if (realContent) realContent.style.display = 'block';
    })
    .catch(err => {
        console.error('Lỗi truy vấn AJAX:', err);
        // Fallback: Lấy dữ liệu trực tiếp từ dòng của bảng nếu mạng chậm
        fallbackRenderFromRow(logId);
        if (spinner) spinner.style.display = 'none';
        if (realContent) realContent.style.display = 'block';
    });
}

function dongModalChiTiet() {
    const modal = document.getElementById('audit-detail-modal');
    if (!modal) return;

    modal.classList.remove('open');
    modal.setAttribute('aria-hidden', 'true');
    document.body.style.overflow = ''; // Mở khóa cuộn trang
}

function renderModalData(data) {
    currentModalLogCode = data.maTruyVet || '';

    setText('modal-log-title', `Chi Tiết Nhật Ký Truy Vết: ${data.maTruyVet}`);
    setText('modal-meta-log-code', data.maTruyVet);
    setText('modal-meta-time', data.thoiDiem);
    setText('modal-meta-actor', `${data.tenNguoiThucHien} (${data.emailNguoiThucHien || ''})`);
    setText('modal-meta-role', data.vaiTroNguoiThucHien);
    setText('modal-meta-ip', data.diaChiIp || '192.168.1.1');
    setText('modal-meta-device', data.thietBi || 'Chrome / Windows 11');

    // Đối tượng
    const targetTypeEl = document.getElementById('modal-target-type');
    if (targetTypeEl) {
        targetTypeEl.textContent = data.loaiDoiTuongTen;
        targetTypeEl.className = 'badge ' + (data.loaiDoiTuongClass || '');
    }
    setText('modal-target-code', data.maDoiTuong);
    setText('modal-target-name', data.tenDoiTuong);

    const actionEl = document.getElementById('modal-target-action');
    if (actionEl) {
        actionEl.textContent = data.hanhDong || 'Cập nhật';
    }

    // So sánh Diff
    setText('modal-diff-field-name', data.truongThayDoi);
    setText('modal-diff-before-value', data.giaTriTruoc);
    setText('modal-diff-after-value', data.giaTriSau);

    // Lý do
    setText('modal-reason-content', data.lyDoThayDoi || 'Không có ghi chú giải trình bổ sung.');
}

function fallbackRenderFromRow(logId) {
    const row = document.querySelector(`.table-row-item[data-log-id="${logId}"]`);
    if (!row) return;

    const code = row.querySelector('.log-code-badge')?.textContent.trim() || '';
    const date = row.querySelector('.log-date')?.textContent.trim() || '';
    const time = row.querySelector('.log-time')?.textContent.trim() || '';
    const actor = row.querySelector('.actor-name')?.textContent.trim() || '';
    const email = row.querySelector('.actor-email')?.textContent.trim() || '';
    const role = row.querySelector('.actor-role-badge')?.textContent.trim() || '';
    const targetName = row.querySelector('.target-name')?.textContent.trim() || '';
    const targetCode = row.querySelector('.target-code code')?.textContent.trim() || '';
    const targetType = row.querySelector('.target-type-badge')?.textContent.trim() || '';
    const field = row.querySelector('.field-name-highlight')?.textContent.trim() || '';
    const before = row.querySelector('.diff-before .diff-text')?.textContent.trim() || '';
    const after = row.querySelector('.diff-after .diff-text')?.textContent.trim() || '';

    currentModalLogCode = code;

    setText('modal-log-title', `Chi Tiết Nhật Ký Truy Vết: ${code}`);
    setText('modal-meta-log-code', code);
    setText('modal-meta-time', `${date} ${time}`);
    setText('modal-meta-actor', `${actor} (${email})`);
    setText('modal-meta-role', role);
    setText('modal-target-code', targetCode);
    setText('modal-target-name', targetName);
    setText('modal-diff-field-name', field);
    setText('modal-diff-before-value', before);
    setText('modal-diff-after-value', after);
}

function setText(id, text) {
    const el = document.getElementById(id);
    if (el) el.textContent = text || '-';
}

function khoiTaoSuKienModal() {
    const modal = document.getElementById('audit-detail-modal');
    if (!modal) return;

    // Đóng khi click ngoài khung modal
    modal.addEventListener('click', function (e) {
        if (e.target === modal) {
            dongModalChiTiet();
        }
    });

    // Đóng khi nhấn phím Escape
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && modal.classList.contains('open')) {
            dongModalChiTiet();
        }
    });
}

/**
 * 6. Sao chép mã truy vết vào Clipboard kèm Toast thông báo
 */
function saoChepMa(text, buttonElement) {
    if (!navigator.clipboard) {
        fallbackCopyTextToClipboard(text);
        return;
    }

    navigator.clipboard.writeText(text).then(function () {
        hienThiToast(`Đã sao chép: ${text}`);
        if (buttonElement) {
            const oldText = buttonElement.innerHTML;
            buttonElement.innerHTML = '✓';
            setTimeout(() => {
                buttonElement.innerHTML = oldText;
            }, 1500);
        }
    }).catch(function (err) {
        console.error('Không thể sao chép:', err);
    });
}

function saoChepMaModal() {
    if (currentModalLogCode) {
        saoChepMa(currentModalLogCode);
    }
}

function fallbackCopyTextToClipboard(text) {
    const textArea = document.createElement("textarea");
    textArea.value = text;
    textArea.style.position = "fixed";
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();
    try {
        document.execCommand('copy');
        hienThiToast(`Đã sao chép: ${text}`);
    } catch (err) {
        console.error('Fallback sao chép lỗi', err);
    }
    document.body.removeChild(textArea);
}

function hienThiToast(message) {
    const toast = document.getElementById('crm-toast');
    if (!toast) return;

    toast.textContent = message;
    toast.classList.add('show');

    setTimeout(function () {
        toast.classList.remove('show');
    }, 2500);
}

/**
 * 7. Xuất dữ liệu kiểm toán ra file Excel/CSV (Demo client-side)
 */
function khoiTaoNutXuatDuLieu() {
    const btnExport = document.getElementById('btn-export-excel');
    if (!btnExport) return;

    btnExport.addEventListener('click', function () {
        const table = document.getElementById('crm-audit-table');
        if (!table) {
            hienThiToast('Không có dữ liệu bảng để xuất!');
            return;
        }

        hienThiToast('Đang kết xuất báo cáo kiểm toán số liệu cuối quý...');

        // Tạo dữ liệu CSV cơ bản từ bảng
        let csv = [];
        const rows = table.querySelectorAll('tr');

        for (let i = 0; i < rows.length; i++) {
            let row = [];
            const cols = rows[i].querySelectorAll('td, th');
            // Bỏ qua cột thao tác
            for (let j = 0; j < cols.length - 1; j++) {
                let cellText = cols[j].innerText.replace(/(\r\n|\n|\r)/gm, ' ').replace(/"/g, '""');
                row.push('"' + cellText.trim() + '"');
            }
            csv.push(row.join(','));
        }

        const csvString = '\uFEFF' + csv.join('\n'); // Thêm BOM để mở tiếng Việt trên Excel
        const blob = new Blob([csvString], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);
        const now = new Date();
        const fileName = `CRM_Nhat_Ky_Du_Lieu_Nhay_Cam_${now.getFullYear()}${String(now.getMonth()+1).padStart(2,'0')}${String(now.getDate()).padStart(2,'0')}.csv`;

        link.setAttribute('href', url);
        link.setAttribute('download', fileName);
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    });
}
