/**
 * JavaScript Chuyên biệt Module Cấu hình Chấm điểm Lead (Story S4-05)
 * Đồng bộ với giao diện Enterprise SaaS mới
 */

document.addEventListener('DOMContentLoaded', function () {
    const inputWarm = document.getElementById('warmMin');
    const inputHot = document.getElementById('hotMin');
    const segCold = document.getElementById('segCold');
    const segWarm = document.getElementById('segWarm');
    const segHot = document.getElementById('segHot');
    const tickWarm = document.getElementById('tickWarm');
    const tickHot = document.getElementById('tickHot');
    const labelColdMax = document.getElementById('labelColdMax');
    const rangeSummaryText = document.getElementById('rangeSummaryText');

    const simScoreCard = document.getElementById('simScoreCard');
    const simScoreValue = document.getElementById('simScoreValue');
    const simTagContainer = document.getElementById('simTagContainer');
    const simHint = document.getElementById('simHint');

    const selIndustry = document.getElementById('sel_INDUSTRY_FIT');
    const selSize = document.getElementById('sel_COMPANY_SIZE');
    const selSource = document.getElementById('sel_SOURCE');
    const selInterest = document.getElementById('sel_INTEREST_LEVEL');

    const ptsBadgeIndustry = document.getElementById('ptsBadge_INDUSTRY_FIT');
    const ptsBadgeSize = document.getElementById('ptsBadge_COMPANY_SIZE');
    const ptsBadgeSource = document.getElementById('ptsBadge_SOURCE');
    const ptsBadgeInterest = document.getElementById('ptsBadge_INTEREST_LEVEL');

    // Cập nhật Visual Range Bar
    function updateRangeBar() {
        if (!inputWarm || !inputHot) return;
        let warm = parseInt(inputWarm.value, 10) || 40;
        let hot = parseInt(inputHot.value, 10) || 70;

        if (warm < 0) warm = 0;
        if (hot > 100) hot = 100;

        const coldPct = Math.min(Math.max(warm, 0), 100);
        const warmPct = Math.min(Math.max(hot - warm, 0), 100 - coldPct);
        const hotPct = Math.min(Math.max(100 - hot, 0), 100);

        if (segCold) segCold.style.width = coldPct + '%';
        if (segWarm) segWarm.style.width = warmPct + '%';
        if (segHot) segHot.style.width = hotPct + '%';

        if (tickWarm) tickWarm.textContent = warm + 'đ';
        if (tickHot) tickHot.textContent = hot + 'đ';
        if (labelColdMax) labelColdMax.textContent = warm;

        if (rangeSummaryText) {
            rangeSummaryText.textContent = 'Lạnh: 0 - ' + (warm - 1) + 'đ | Ấm: ' + warm + ' - ' + (hot - 1) + 'đ | Nóng: ≥ ' + hot + 'đ';
        }

        updateSimulation();
    }

    if (inputWarm) inputWarm.addEventListener('input', updateRangeBar);
    if (inputHot) inputHot.addEventListener('input', updateRangeBar);

    // Bật / Tắt Tiêu chí
    const switches = document.querySelectorAll('.switch input[type="checkbox"]');
    switches.forEach(function (sw) {
        sw.addEventListener('change', function () {
            const key = this.getAttribute('data-key');
            const row = document.getElementById('row_' + key);
            const ptsInput = document.getElementById('points_' + key);

            if (this.checked) {
                if (row) row.classList.remove('is-disabled');
                if (ptsInput) ptsInput.disabled = false;
            } else {
                if (row) row.classList.add('is-disabled');
                if (ptsInput) ptsInput.disabled = true;
            }
            recalcMaxPoints();
            updateSimulation();
        });
    });

    // Cập nhật lại tổng điểm tối đa
    function recalcMaxPoints() {
        let total = 0;
        ['INDUSTRY_FIT', 'COMPANY_SIZE', 'SOURCE', 'INTEREST_LEVEL'].forEach(function (k) {
            const chk = document.getElementById('enabled_' + k);
            const pts = document.getElementById('points_' + k);
            if (chk && chk.checked && pts) {
                total += parseInt(pts.value, 10) || 0;
            }
        });
        const disp = document.getElementById('totalPointsDisplay');
        if (disp) disp.textContent = total + 'đ';
    }

    // Cập nhật Simulator tính điểm
    function updateSimulation() {
        if (!simScoreCard || !simScoreValue) return;

        let warm = parseInt(inputWarm ? inputWarm.value : 40, 10) || 40;
        let hot = parseInt(inputHot ? inputHot.value : 70, 10) || 70;

        const chkInd = document.getElementById('enabled_INDUSTRY_FIT');
        const chkSize = document.getElementById('enabled_COMPANY_SIZE');
        const chkSrc = document.getElementById('enabled_SOURCE');
        const chkInt = document.getElementById('enabled_INTEREST_LEVEL');

        const ptsInd = (chkInd && chkInd.checked && selIndustry) ? (parseInt(selIndustry.value, 10) || 0) : 0;
        const ptsSize = (chkSize && chkSize.checked && selSize) ? (parseInt(selSize.value, 10) || 0) : 0;
        const ptsSrc = (chkSrc && chkSrc.checked && selSource) ? (parseInt(selSource.value, 10) || 0) : 0;
        const ptsInt = (chkInt && chkInt.checked && selInterest) ? (parseInt(selInterest.value, 10) || 0) : 0;

        if (ptsBadgeIndustry) ptsBadgeIndustry.textContent = '+' + ptsInd + 'đ';
        if (ptsBadgeSize) ptsBadgeSize.textContent = '+' + ptsSize + 'đ';
        if (ptsBadgeSource) ptsBadgeSource.textContent = '+' + ptsSrc + 'đ';
        if (ptsBadgeInterest) ptsBadgeInterest.textContent = '+' + ptsInt + 'đ';

        const total = ptsInd + ptsSize + ptsSrc + ptsInt;
        simScoreValue.textContent = total;

        simScoreCard.classList.remove('state-hot', 'state-warm', 'state-cold');

        if (total >= hot) {
            simScoreCard.classList.add('state-hot');
            if (simTagContainer) simTagContainer.innerHTML = '<span class="lead-tag tag-hot">Lead Nóng</span>';
            if (simHint) simHint.textContent = 'Khả năng chốt đơn cao; ưu tiên phân bổ và liên hệ ngay trong 5 phút.';
        } else if (total >= warm) {
            simScoreCard.classList.add('state-warm');
            if (simTagContainer) simTagContainer.innerHTML = '<span class="lead-tag tag-warm">Lead Ấm</span>';
            if (simHint) simHint.textContent = 'Có mối quan tâm rõ ràng; phân bổ nhân viên liên hệ chăm sóc trong ngày.';
        } else {
            simScoreCard.classList.add('state-cold');
            if (simTagContainer) simTagContainer.innerHTML = '<span class="lead-tag tag-cold">Lead Lạnh</span>';
            if (simHint) simHint.textContent = 'Nhu cầu chưa cấp thiết; chuyển sang luồng nuôi dưỡng định kỳ qua Marketing.';
        }
    }

    [selIndustry, selSize, selSource, selInterest].forEach(function (sel) {
        if (sel) sel.addEventListener('change', updateSimulation);
    });

    // Nút đặt lại mặc định
    const btnReset = document.getElementById('btnDatLaiMacDinh');
    if (btnReset) {
        btnReset.addEventListener('click', function () {
            if (inputWarm) inputWarm.value = 40;
            if (inputHot) inputHot.value = 70;
            switches.forEach(function (sw) { sw.checked = true; });
            document.querySelectorAll('.criterion-row').forEach(function (r) { r.classList.remove('is-disabled'); });
            const pInd = document.getElementById('points_INDUSTRY_FIT');
            const pSize = document.getElementById('points_COMPANY_SIZE');
            const pSrc = document.getElementById('points_SOURCE');
            const pInt = document.getElementById('points_INTEREST_LEVEL');
            if (pInd) pInd.value = 25;
            if (pSize) pSize.value = 20;
            if (pSrc) pSrc.value = 15;
            if (pInt) pInt.value = 40;

            recalcMaxPoints();
            updateRangeBar();
            showToast('Đã khôi phục các giá trị cấu hình về mặc định ban đầu.', false);
        });
    }

    // Client validation khi submit form
    const formConfig = document.getElementById('formCauHinhChamDiem');
    if (formConfig) {
        formConfig.addEventListener('submit', function (e) {
            const warm = parseInt(inputWarm ? inputWarm.value : 0, 10);
            const hot = parseInt(inputHot ? inputHot.value : 0, 10);

            if (hot <= warm) {
                e.preventDefault();
                showToast('Lỗi: Ngưỡng Nóng (' + hot + 'đ) phải lớn hơn ngưỡng Ấm (' + warm + 'đ)!', true);
                return;
            }
        });
    }

    function showToast(msg, isError) {
        const toast = document.getElementById('toast');
        const toastText = document.getElementById('toastText');
        if (!toast || !toastText) return;
        toastText.textContent = msg;
        toast.style.background = isError ? '#b91c1c' : '#0f172a';
        toast.classList.add('show');
        setTimeout(function () { toast.classList.remove('show'); }, 3500);
    }

    updateRangeBar();
    recalcMaxPoints();
});
