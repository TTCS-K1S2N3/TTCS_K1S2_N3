/**
 * JavaScript Chuyên biệt Module Cấu hình Chấm điểm Lead (Story S4-05)
 * Xử lý:
 * - AC1: Bật/tắt tiêu chí và điều chỉnh điểm số tối đa
 * - AC2: Live Scoring Simulator - Tính điểm tức thì khi thay đổi thông tin Lead
 * - AC3: Cập nhật động thanh Visual Range Bar theo ngưỡng Nóng / Ấm / Lạnh
 * - AC4: Đảm bảo nguyên tắc Lead lạnh không bị loại, hiển thị trạng thái xếp thứ tự ưu tiên
 */

document.addEventListener('DOMContentLoaded', function () {
    // 1. Tham chiếu các phần tử DOM chính
    const formConfig = document.getElementById('formCauHinhChamDiem');
    const inputHotMin = document.getElementById('hotMin');
    const inputWarmMin = document.getElementById('warmMin');
    const segCold = document.getElementById('segCold');
    const segWarm = document.getElementById('segWarm');
    const segHot = document.getElementById('segHot');
    const tickWarm = document.getElementById('tickWarm');
    const tickHot = document.getElementById('tickHot');

    // Simulator DOM
    const simSelectNganh = document.getElementById('simNganhNghe');
    const simSelectQuyMo = document.getElementById('simQuyMo');
    const simSelectNguon = document.getElementById('simNguonLead');
    const simSelectQuanTam = document.getElementById('simMucDoQuanTam');
    const simResultBox = document.getElementById('simResultBox');
    const simScoreNumber = document.getElementById('simScoreNumber');
    const simBadge = document.getElementById('simBadge');
    const simBadgeText = document.getElementById('simBadgeText');
    const simStatusNote = document.getElementById('simStatusNote');

    const ptsNganhSpan = document.getElementById('simPtsNganh');
    const ptsQuyMoSpan = document.getElementById('simPtsQuyMo');
    const ptsNguonSpan = document.getElementById('simPtsNguon');
    const ptsQuanTamSpan = document.getElementById('simPtsQuanTam');

    const btnLuu = document.getElementById('btnLuuCauHinh');
    const spinnerLuu = document.getElementById('spinnerLuu');
    const toastNotification = document.getElementById('cdToast');
    const toastMessage = document.getElementById('cdToastMsg');

    // 2. Hàm cập nhật thanh dải trực quan Visual Range Bar (AC3)
    function capNhatRangeBar() {
        let hotVal = parseInt(inputHotMin.value, 10) || 70;
        let warmVal = parseInt(inputWarmMin.value, 10) || 40;

        // Giới hạn giá trị hợp lý
        if (warmVal < 0) warmVal = 0;
        if (hotVal > 100) hotVal = 100;

        const coldPercent = Math.min(Math.max(warmVal, 0), 100);
        const warmPercent = Math.min(Math.max(hotVal - warmVal, 0), 100 - coldPercent);
        const hotPercent = Math.min(Math.max(100 - hotVal, 0), 100);

        if (segCold) segCold.style.width = coldPercent + '%';
        if (segWarm) segWarm.style.width = warmPercent + '%';
        if (segHot) segHot.style.width = hotPercent + '%';

        if (tickWarm) tickWarm.textContent = warmVal + 'đ';
        if (tickHot) tickHot.textContent = hotVal + 'đ';

        // Cập nhật lại kết quả mô phỏng
        tinhDiemMoPhong();
    }

    if (inputHotMin && inputWarmMin) {
        inputHotMin.addEventListener('input', capNhatRangeBar);
        inputWarmMin.addEventListener('input', capNhatRangeBar);
    }

    // 3. Hàm xử lý Bật/Tắt tiêu chí (AC1)
    const switches = document.querySelectorAll('.cd-switch input[type="checkbox"]');
    switches.forEach(function (toggle) {
        toggle.addEventListener('change', function () {
            const card = this.closest('.cd-criterion-card');
            const key = this.getAttribute('data-criterion-key');
            const pointsInput = document.getElementById('points_' + key);

            if (this.checked) {
                card.classList.remove('disabled');
                if (pointsInput) pointsInput.disabled = false;
            } else {
                card.classList.add('disabled');
                if (pointsInput) pointsInput.disabled = true;
            }

            tinhDiemMoPhong();
        });
    });

    // 4. Lắng nghe thay đổi điểm số của tiêu chí
    const pointsInputs = document.querySelectorAll('.cd-points-input');
    pointsInputs.forEach(function (input) {
        input.addEventListener('input', function () {
            tinhDiemMoPhong();
        });
    });

    // 5. Hàm tính điểm mô phỏng tương tác thời gian thực (AC2 & AC4)
    function tinhDiemMoPhong() {
        if (!simResultBox) return;

        let hotVal = parseInt(inputHotMin.value, 10) || 70;
        let warmVal = parseInt(inputWarmMin.value, 10) || 40;

        // Điểm từng trường
        const chkNganh = document.getElementById('enabled_INDUSTRY_FIT');
        const chkQuyMo = document.getElementById('enabled_COMPANY_SIZE');
        const chkNguon = document.getElementById('enabled_SOURCE');
        const chkQuanTam = document.getElementById('enabled_INTEREST_LEVEL');

        const optNganh = simSelectNganh ? simSelectNganh.options[simSelectNganh.selectedIndex] : null;
        const optQuyMo = simSelectQuyMo ? simSelectQuyMo.options[simSelectQuyMo.selectedIndex] : null;
        const optNguon = simSelectNguon ? simSelectNguon.options[simSelectNguon.selectedIndex] : null;
        const optQuanTam = simSelectQuanTam ? simSelectQuanTam.options[simSelectQuanTam.selectedIndex] : null;

        let ptsNganh = (chkNganh && chkNganh.checked && optNganh) ? (parseInt(optNganh.getAttribute('data-diem'), 10) || 0) : 0;
        let ptsQuyMo = (chkQuyMo && chkQuyMo.checked && optQuyMo) ? (parseInt(optQuyMo.getAttribute('data-diem'), 10) || 0) : 0;
        let ptsNguon = (chkNguon && chkNguon.checked && optNguon) ? (parseInt(optNguon.getAttribute('data-diem'), 10) || 0) : 0;
        let ptsQuanTam = (chkQuanTam && chkQuanTam.checked && optQuanTam) ? (parseInt(optQuanTam.getAttribute('data-diem'), 10) || 0) : 0;

        // Cập nhật số điểm hiển thị cạnh label
        if (ptsNganhSpan) ptsNganhSpan.textContent = '+' + ptsNganh + 'đ';
        if (ptsQuyMoSpan) ptsQuyMoSpan.textContent = '+' + ptsQuyMo + 'đ';
        if (ptsNguonSpan) ptsNguonSpan.textContent = '+' + ptsNguon + 'đ';
        if (ptsQuanTamSpan) ptsQuanTamSpan.textContent = '+' + ptsQuanTam + 'đ';

        const tongDiem = ptsNganh + ptsQuyMo + ptsNguon + ptsQuanTam;

        if (simScoreNumber) {
            simScoreNumber.textContent = tongDiem;
        }

        // Xác định phân loại theo AC3
        simResultBox.classList.remove('hot', 'warm', 'cold');
        if (simBadge) simBadge.classList.remove('hot', 'warm', 'cold');

        if (tongDiem >= hotVal) {
            // NÓNG
            simResultBox.classList.add('hot');
            if (simBadge) {
                simBadge.classList.add('hot');
                simBadge.querySelector('.material-symbols-outlined').textContent = 'local_fire_department';
            }
            if (simBadgeText) simBadgeText.textContent = 'LEAD NÓNG';
            if (simStatusNote) simStatusNote.textContent = '🔥 Tiềm năng chuyển đổi rất cao — Ưu tiên liên hệ trong 5 phút!';
        } else if (tongDiem >= warmVal) {
            // ẤM
            simResultBox.classList.add('warm');
            if (simBadge) {
                simBadge.classList.add('warm');
                simBadge.querySelector('.material-symbols-outlined').textContent = 'wb_sunny';
            }
            if (simBadgeText) simBadgeText.textContent = 'LEAD ẤM';
            if (simStatusNote) simStatusNote.textContent = '☀️ Nhu cầu rõ ràng — Phân công liên hệ chăm sóc trong ngày.';
        } else {
            // LẠNH (AC4: Không tự loại lead, chỉ là ưu tiên thấp hơn)
            simResultBox.classList.add('cold');
            if (simBadge) {
                simBadge.classList.add('cold');
                simBadge.querySelector('.material-symbols-outlined').textContent = 'ac_unit';
            }
            if (simBadgeText) simBadgeText.textContent = 'LEAD LẠNH';
            if (simStatusNote) simStatusNote.textContent = '❄️ Chưa phát sinh nhu cầu gấp — Nuôi dưỡng qua chiến dịch Marketing (Không bị loại bỏ).';
        }
    }

    // Gắn sự kiện thay đổi select của Simulator
    [simSelectNganh, simSelectQuyMo, simSelectNguon, simSelectQuanTam].forEach(function (sel) {
        if (sel) {
            sel.addEventListener('change', tinhDiemMoPhong);
        }
    });

    // 6. Xử lý Form Submit qua Fetch API (hoặc fallback Form thường)
    if (formConfig) {
        formConfig.addEventListener('submit', function (e) {
            const hotVal = parseInt(inputHotMin.value, 10);
            const warmVal = parseInt(inputWarmMin.value, 10);

            // Client Validation nhanh
            if (isNaN(hotVal) || isNaN(warmVal)) {
                e.preventDefault();
                hienThiToast('Vui lòng nhập đầy đủ giá trị số cho các ngưỡng điểm.', 'danger');
                return;
            }

            if (hotVal <= warmVal) {
                e.preventDefault();
                hienThiToast('Ngưỡng điểm Nóng (' + hotVal + ') phải lớn hơn ngưỡng điểm Ấm (' + warmVal + ').', 'danger');
                inputHotMin.focus();
                return;
            }

            // Kiểm tra có ít nhất 1 tiêu chí được chọn
            let coTieuChi = false;
            switches.forEach(function (sw) {
                if (sw.checked) coTieuChi = true;
            });

            if (!coTieuChi) {
                e.preventDefault();
                hienThiToast('Bạn phải kích hoạt ít nhất một tiêu chí chấm điểm lead.', 'danger');
                return;
            }

            // Hiển thị trạng thái đang lưu
            if (btnLuu) btnLuu.disabled = true;
            if (spinnerLuu) spinnerLuu.style.display = 'inline-block';
        });
    }

    // 7. Hàm hiển thị Toast thông báo nhanh
    function hienThiToast(message, type) {
        if (!toastNotification || !toastMessage) return;
        toastMessage.textContent = message;
        toastNotification.className = 'cd-toast show';
        if (type === 'danger') {
            toastNotification.style.background = '#991b1b';
        } else if (type === 'success') {
            toastNotification.style.background = '#065f46';
        } else {
            toastNotification.style.background = '#1e293b';
        }

        setTimeout(function () {
            toastNotification.classList.remove('show');
        }, 4000);
    }

    // Khởi chạy tính toán ban đầu
    capNhatRangeBar();
    tinhDiemMoPhong();
});
