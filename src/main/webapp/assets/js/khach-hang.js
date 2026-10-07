/**
 * khach-hang.js - Xử lý tương tác giao diện Danh mục khách hàng
 * Story S1-05: Quản lý khách hàng và phân quyền Data Scope
 * Story S3-04: Cảnh báo và gộp khách hàng trùng lặp
 * - Phát hiện trùng theo Mã số thuế, Tên công ty gần giống và Website (AC1)
 * - Hiển thị so sánh cạnh nhau trước khi gộp (AC2)
 * - Gộp giữ lại toàn bộ người liên hệ, cơ hội và hoạt động (AC3)
 * - Chỉ Trưởng nhóm trở lên được thực hiện gộp (AC4)
 */
document.addEventListener('DOMContentLoaded', function () {
    'use strict';

    // =========================================================================
    // 1. Khởi tạo các phần tử DOM cơ bản
    // =========================================================================
    const btnThemKhachHang = document.getElementById('btnThemKhachHang');
    const modalThemKhachHang = document.getElementById('modalThemKhachHang');
    const btnDongModalThem = document.getElementById('btnDongModalThemKhachHang');
    const btnHuyThem = document.getElementById('btnHuyThemKhachHang');
    const formThemKhachHang = document.getElementById('formThemKhachHang');
    const inputTenCongTy = document.getElementById('tenCongTy');
    const inputMaSoThueThem = document.getElementById('maSoThueThem');
    const inputWebsiteThem = document.getElementById('websiteThem');
    const btnExportExcel = document.getElementById('btnExportExcel');

    // Tab navigation (S3-04)
    const tabBtnTatCa = document.getElementById('tabBtnTatCa');
    const tabBtnTrungLap = document.getElementById('tabBtnTrungLap');
    const tabPaneTatCa = document.getElementById('tabPaneTatCa');
    const tabPaneTrungLap = document.getElementById('tabPaneTrungLap');
    const btnChuyenTabTrung = document.getElementById('btnChuyenTabTrung');
    const btnXemCapTrungTuBanner = document.getElementById('btnXemCapTrungTuBanner');
    const tabBadgeSoCapTrung = document.getElementById('tabBadgeSoCapTrung');
    const bannerCanhBaoTrung = document.getElementById('bannerCanhBaoTrung');
    const bannerCanhBaoText = document.getElementById('bannerCanhBaoText');

    // Duplicate Center controls (S3-04)
    const containerDanhSachTrung = document.getElementById('containerDanhSachTrung');
    const emptyStateTrung = document.getElementById('emptyStateTrung');
    const inputTimKiemTrung = document.getElementById('inputTimKiemTrung');
    const filterChips = document.querySelectorAll('.dup-chip');

    // Modal so sánh cạnh nhau (S3-04)
    const modalSoSanhGop = document.getElementById('modalSoSanhGop');
    const btnDongModalSoSanh = document.getElementById('btnDongModalSoSanh');
    const btnHuySoSanhGop = document.getElementById('btnHuySoSanhGop');
    const btnHoanDoiViTri = document.getElementById('btnHoanDoiViTri');
    const formXacNhanGop = document.getElementById('formXacNhanGop');
    const compareKhachHangDichId = document.getElementById('compareKhachHangDichId');
    const compareKhachHangNguonId = document.getElementById('compareKhachHangNguonId');
    const lyDoGop = document.getElementById('lyDoGop');

    // Inline duplicate alerts (Thêm khách hàng)
    const inlineDupNameAlert = document.getElementById('inlineDupNameAlert');
    const inlineDupMstAlert = document.getElementById('inlineDupMstAlert');
    const inlineDupWebAlert = document.getElementById('inlineDupWebAlert');
    const dupMatchedName = document.getElementById('dupMatchedName');
    const dupMatchedOwner = document.getElementById('dupMatchedOwner');
    const dupMatchedMstName = document.getElementById('dupMatchedMstName');
    const dupMatchedWebName = document.getElementById('dupMatchedWebName');

    // Trạng thái dữ liệu hiện tại
    let currentCustomerList = [];
    let detectedDuplicatePairs = [];
    let currentActiveFilter = 'all';
    let currentComparePair = null;

    // =========================================================================
    // 2. Thuật toán phát hiện trùng lặp thông minh (AC1)
    // =========================================================================

    // Chuẩn hóa chuỗi tiếng Việt (bỏ dấu, chuyển chữ thường)
    function removeVietnameseTones(str) {
        if (!str) return '';
        str = str.toLowerCase();
        str = str.replace(/à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ/g, 'a');
        str = str.replace(/è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ/g, 'e');
        str = str.replace(/ì|í|ị|ỉ|ĩ/g, 'i');
        str = str.replace(/ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ/g, 'o');
        str = str.replace(/ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ/g, 'u');
        str = str.replace(/ỳ|ý|ỵ|ỷ|ỹ/g, 'y');
        str = str.replace(/đ/g, 'd');
        return str;
    }

    // Chuẩn hóa tên công ty (loại bỏ từ ngữ pháp lý phổ biến để so khớp cốt lõi)
    function cleanCompanyName(name) {
        if (!name) return '';
        let clean = removeVietnameseTones(name);
        const noiseWords = [
            'cong ty', 'cty', 'co phan', 'cp', 'tnhh', 'trach nhiem huu han',
            'tap doan', 'chi nhanh', 'doanh nghiep', 'tong cong ty', 'tnhh mtv'
        ];
        noiseWords.forEach(function (w) {
            clean = clean.replace(new RegExp('\\b' + w + '\\b', 'g'), '');
        });
        clean = clean.replace(/[^a-z0-9\s]/g, ' ');
        return clean.replace(/\s+/g, ' ').trim();
    }

    // Chuẩn hóa mã số thuế (bỏ ký tự lạ, giữ chữ và số)
    function cleanTaxCode(mst) {
        if (!mst) return '';
        return mst.replace(/[^a-zA-Z0-9]/g, '').trim().toUpperCase();
    }

    // Chuẩn hóa website (bỏ http, https, www, trailing slash, path)
    function cleanWebsite(url) {
        if (!url) return '';
        let clean = url.trim().toLowerCase();
        clean = clean.replace(/^https?:\/\//, '');
        clean = clean.replace(/^www\./, '');
        clean = clean.split('/')[0];
        clean = clean.split('?')[0];
        return clean.trim();
    }

    // Tính độ tương đồng giữa 2 chuỗi tên (Token Jaccard / Dice coefficient)
    function calcSimilarity(name1, name2) {
        const c1 = cleanCompanyName(name1);
        const c2 = cleanCompanyName(name2);
        if (!c1 || !c2) return 0;
        if (c1 === c2) return 1.0;

        // Nếu một trong hai chuỗi chứa chuỗi kia và có độ dài hợp lý
        if ((c1.includes(c2) || c2.includes(c1)) && Math.min(c1.length, c2.length) >= 3) {
            return 0.85;
        }

        const tokens1 = c1.split(' ').filter(function (t) { return t.length > 1; });
        const tokens2 = c2.split(' ').filter(function (t) { return t.length > 1; });
        if (tokens1.length === 0 || tokens2.length === 0) return 0;

        const set1 = new Set(tokens1);
        const set2 = new Set(tokens2);
        let intersection = 0;
        set1.forEach(function (token) {
            if (set2.has(token)) intersection++;
        });

        // Dice coefficient
        return (2.0 * intersection) / (set1.size + set2.size);
    }

    // Trích xuất danh sách khách hàng từ bảng DOM
    function extractCustomersFromTable() {
        const rows = document.querySelectorAll('#tableKhachHang tbody tr[data-id]');
        const list = [];
        rows.forEach(function (tr) {
            const id = tr.getAttribute('data-id');
            const ma = tr.getAttribute('data-ma') || '';
            const ten = tr.getAttribute('data-ten') || '';
            const mst = tr.getAttribute('data-mst') || '';
            const web = tr.getAttribute('data-web') || '';
            const ownerId = tr.getAttribute('data-owner-id') || '';
            const ownerName = tr.getAttribute('data-owner-name') || '';
            const teamName = tr.getAttribute('data-team-name') || '';
            const giaTri = tr.getAttribute('data-gia-tri') || '';
            const trangThai = tr.getAttribute('data-trang-thai') || '';
            const ngayTao = tr.getAttribute('data-ngay-tao') || '';
            const moTa = tr.getAttribute('data-mo-ta') || '';

            list.push({
                id: id,
                ma: ma,
                ten: ten,
                mst: mst,
                web: web,
                mstClean: cleanTaxCode(mst),
                webClean: cleanWebsite(web),
                ownerId: ownerId,
                ownerName: ownerName,
                teamName: teamName,
                giaTri: giaTri,
                trangThai: trangThai,
                ngayTao: ngayTao,
                moTa: moTa,
                trElement: tr
            });
        });
        return list;
    }

    // Quét toàn bộ các cặp khách hàng để phát hiện trùng lặp
    function detectDuplicatePairs() {
        detectedDuplicatePairs = [];
        const n = currentCustomerList.length;

        for (let i = 0; i < n; i++) {
            for (let j = i + 1; j < n; j++) {
                const a = currentCustomerList[i];
                const b = currentCustomerList[j];

                let isMst = false;
                let isName = false;
                let isWeb = false;
                let reasons = [];
                let simPercent = 0;

                // 1. Kiểm tra trùng Mã số thuế (MST)
                if (a.mstClean && b.mstClean && a.mstClean === b.mstClean) {
                    isMst = true;
                    reasons.push('Trùng Mã số thuế: ' + a.mst);
                }

                // 2. Kiểm tra tên công ty gần giống
                const sim = calcSimilarity(a.ten, b.ten);
                if (sim >= 0.70) {
                    isName = true;
                    simPercent = Math.round(sim * 100);
                    reasons.push('Tên tương đồng ' + simPercent + '%');
                }

                // 3. Kiểm tra trùng Website
                if (a.webClean && b.webClean && a.webClean === b.webClean) {
                    isWeb = true;
                    reasons.push('Trùng Website: ' + a.webClean);
                }

                // Nếu thỏa mãn ít nhất 1 tiêu chí trùng lặp
                if (isMst || isName || isWeb) {
                    const isConflict = (a.ownerId && b.ownerId && a.ownerId !== b.ownerId);
                    detectedDuplicatePairs.push({
                        id: a.id + '-' + b.id,
                        recordA: a,
                        recordB: b,
                        isMst: isMst,
                        isName: isName,
                        isWeb: isWeb,
                        simPercent: simPercent,
                        reasons: reasons,
                        isConflict: isConflict
                    });
                }
            }
        }
        return detectedDuplicatePairs;
    }

    // =========================================================================
    // 3. Hiển thị danh sách cặp trùng & Cảnh báo (AC1, AC2)
    // =========================================================================

    function updateUiDuplicateStatus() {
        const count = detectedDuplicatePairs.length;

        // Cập nhật badge số lượng cặp trùng trên tab
        if (tabBadgeSoCapTrung) {
            tabBadgeSoCapTrung.textContent = count + ' cặp trùng';
            if (count > 0) {
                tabBadgeSoCapTrung.className = 'crm-tab-badge badge-warning';
            } else {
                tabBadgeSoCapTrung.className = 'crm-tab-badge badge-neutral';
            }
        }

        // Cập nhật banner cảnh báo trên Tab Tất cả
        if (bannerCanhBaoTrung && bannerCanhBaoText) {
            if (count > 0) {
                bannerCanhBaoTrung.style.display = 'flex';
                bannerCanhBaoText.textContent = 'Hệ thống phát hiện ' + count +
                    ' cặp khách hàng trùng thông tin (Mã số thuế, Tên gần giống hoặc Website). Trưởng nhóm kinh doanh kiểm tra so sánh và thực hiện gộp.';
            } else {
                bannerCanhBaoTrung.style.display = 'none';
            }
        }

        // Đánh dấu các dòng trên bảng Danh mục có trùng lặp
        const duplicateIds = new Set();
        detectedDuplicatePairs.forEach(function (pair) {
            duplicateIds.add(pair.recordA.id);
            duplicateIds.add(pair.recordB.id);
        });

        currentCustomerList.forEach(function (c) {
            if (c.trElement) {
                const badge = c.trElement.querySelector('.dup-badge-inline');
                const btnCompare = c.trElement.querySelector('.btn-row-compare-trigger');
                if (duplicateIds.has(c.id)) {
                    if (badge) badge.style.display = 'inline-flex';
                    if (btnCompare) btnCompare.style.display = 'inline-flex';
                } else {
                    if (badge) badge.style.display = 'none';
                    if (btnCompare) btnCompare.style.display = 'none';
                }
            }
        });

        // Render các thẻ cặp trùng trong Tab Cảnh báo trùng lặp
        renderDuplicateCards();
    }

    // Render danh sách các thẻ cặp trùng trong Tab 2
    function renderDuplicateCards() {
        if (!containerDanhSachTrung) return;
        containerDanhSachTrung.innerHTML = '';

        // Lọc theo chip tiêu chí và từ khóa tìm kiếm
        const keyword = (inputTimKiemTrung ? inputTimKiemTrung.value.trim().toLowerCase() : '');
        const filteredPairs = detectedDuplicatePairs.filter(function (pair) {
            // Lọc theo chip
            if (currentActiveFilter === 'mst' && !pair.isMst) return false;
            if (currentActiveFilter === 'name' && !pair.isName) return false;
            if (currentActiveFilter === 'web' && !pair.isWeb) return false;

            // Lọc theo từ khóa tìm kiếm
            if (keyword) {
                const textA = (pair.recordA.ten + ' ' + pair.recordA.mst + ' ' + pair.recordA.ownerName).toLowerCase();
                const textB = (pair.recordB.ten + ' ' + pair.recordB.mst + ' ' + pair.recordB.ownerName).toLowerCase();
                if (!textA.includes(keyword) && !textB.includes(keyword)) {
                    return false;
                }
            }
            return true;
        });

        if (filteredPairs.length === 0) {
            if (emptyStateTrung) emptyStateTrung.style.display = 'block';
            return;
        }

        if (emptyStateTrung) emptyStateTrung.style.display = 'none';

        filteredPairs.forEach(function (pair, idx) {
            const card = document.createElement('div');
            card.className = 'dup-card';

            // Tạo các badge tiêu chí
            let badgesHtml = '';
            if (pair.isMst) {
                badgesHtml += '<span class="dup-criterion-tag tag-mst"><span class="material-symbols-outlined icon-xs" aria-hidden="true">badge</span> Trùng MST: ' + (pair.recordA.mst || pair.recordB.mst) + '</span>';
            }
            if (pair.isName) {
                badgesHtml += '<span class="dup-criterion-tag tag-name"><span class="material-symbols-outlined icon-xs" aria-hidden="true">spellcheck</span> Tên tương đồng ' + (pair.simPercent || '85') + '%</span>';
            }
            if (pair.isWeb) {
                badgesHtml += '<span class="dup-criterion-tag tag-web"><span class="material-symbols-outlined icon-xs" aria-hidden="true">language</span> Trùng Website</span>';
            }

            // Cảnh báo xung đột người phụ trách (AC: "để hai nhân viên không cùng chào một công ty mà không biết nhau")
            let conflictBanner = '';
            if (pair.isConflict) {
                conflictBanner = '<div class="dup-conflict-alert">' +
                    '<span class="material-symbols-outlined icon-sm" aria-hidden="true">warning</span>' +
                    '<span><strong>Cảnh báo hai nhân viên cùng chào một công ty:</strong> ' +
                    'Nhân viên <strong>' + escapeHtml(pair.recordA.ownerName) + '</strong> (' + escapeHtml(pair.recordA.teamName) + ') và ' +
                    'Nhân viên <strong>' + escapeHtml(pair.recordB.ownerName) + '</strong> (' + escapeHtml(pair.recordB.teamName) + ') ' +
                    'đang cùng sở hữu hồ sơ này!</span>' +
                    '</div>';
            }

            card.innerHTML =
                '<div class="dup-card-header">' +
                    '<div class="dup-criteria-badges">' +
                        '<span style="font-size: 13px; font-weight: 700; color: var(--slate-700);">Cặp nghi trùng #' + (idx + 1) + ':</span>' +
                        badgesHtml +
                    '</div>' +
                    '<span style="font-size: 12.5px; color: var(--slate-500); font-weight: 600;">' +
                        'Độ tin cậy: ' + (pair.isMst ? 'Rất cao (95%)' : (pair.isWeb ? 'Cao (90%)' : 'Khá (80%)')) +
                    '</span>' +
                '</div>' +
                conflictBanner +
                '<div class="dup-card-body">' +
                    '<div class="dup-grid">' +
                        '<!-- Cột bản ghi A -->' +
                        '<div class="dup-record-col is-dest">' +
                            '<div class="dup-record-col-header">' +
                                '<span class="dup-record-title">' + escapeHtml(pair.recordA.ten) + '</span>' +
                                '<span class="dup-record-code">' + escapeHtml(pair.recordA.ma) + '</span>' +
                            '</div>' +
                            '<div class="dup-field-row">' +
                                '<span class="dup-field-label">Người phụ trách:</span>' +
                                '<span class="dup-field-val ' + (pair.isConflict ? 'highlight-conflict' : '') + '">' + escapeHtml(pair.recordA.ownerName) + '</span>' +
                            '</div>' +
                            '<div class="dup-field-row">' +
                                '<span class="dup-field-label">Mã số thuế:</span>' +
                                '<span class="dup-field-val font-mono ' + (pair.isMst ? 'highlight-match' : '') + '">' + escapeHtml(pair.recordA.mst || '-') + '</span>' +
                            '</div>' +
                            '<div class="dup-field-row">' +
                                '<span class="dup-field-label">Website:</span>' +
                                '<span class="dup-field-val ' + (pair.isWeb ? 'highlight-match' : '') + '">' + escapeHtml(pair.recordA.web || '-') + '</span>' +
                            '</div>' +
                            '<div class="dup-field-row">' +
                                '<span class="dup-field-label">Trạng thái:</span>' +
                                '<span class="dup-field-val">' + escapeHtml(pair.recordA.trangThai) + '</span>' +
                            '</div>' +
                        '</div>' +
                        '<!-- Phân cách VS -->' +
                        '<div class="dup-versus-divider">' +
                            '<div class="dup-versus-circle">' +
                                '<span class="material-symbols-outlined" aria-hidden="true">compare_arrows</span>' +
                            '</div>' +
                            '<span>VS</span>' +
                        '</div>' +
                        '<!-- Cột bản ghi B -->' +
                        '<div class="dup-record-col">' +
                            '<div class="dup-record-col-header">' +
                                '<span class="dup-record-title">' + escapeHtml(pair.recordB.ten) + '</span>' +
                                '<span class="dup-record-code">' + escapeHtml(pair.recordB.ma) + '</span>' +
                            '</div>' +
                            '<div class="dup-field-row">' +
                                '<span class="dup-field-label">Người phụ trách:</span>' +
                                '<span class="dup-field-val ' + (pair.isConflict ? 'highlight-conflict' : '') + '">' + escapeHtml(pair.recordB.ownerName) + '</span>' +
                            '</div>' +
                            '<div class="dup-field-row">' +
                                '<span class="dup-field-label">Mã số thuế:</span>' +
                                '<span class="dup-field-val font-mono ' + (pair.isMst ? 'highlight-match' : '') + '">' + escapeHtml(pair.recordB.mst || '-') + '</span>' +
                            '</div>' +
                            '<div class="dup-field-row">' +
                                '<span class="dup-field-label">Website:</span>' +
                                '<span class="dup-field-val ' + (pair.isWeb ? 'highlight-match' : '') + '">' + escapeHtml(pair.recordB.web || '-') + '</span>' +
                            '</div>' +
                            '<div class="dup-field-row">' +
                                '<span class="dup-field-label">Trạng thái:</span>' +
                                '<span class="dup-field-val">' + escapeHtml(pair.recordB.trangThai) + '</span>' +
                            '</div>' +
                        '</div>' +
                    '</div>' +
                '</div>' +
                '<div class="dup-card-footer">' +
                    '<span style="font-size: 13px; color: var(--slate-500);">' +
                        'Gợi ý: Giữ lại bản ghi chính và gộp toàn bộ Người liên hệ, Cơ hội và Hoạt động của bản ghi còn lại (AC3).' +
                    '</span>' +
                    '<button type="button" class="btn btn-primary btn-trigger-compare" data-pair-id="' + pair.id + '" style="font-size: 13.5px; padding: 8px 16px;">' +
                        '<span class="material-symbols-outlined" aria-hidden="true">compare_arrows</span>' +
                        '<span>So Sánh Cạnh Nhau & Gộp</span>' +
                    '</button>' +
                '</div>';

            // Sự kiện mở modal so sánh cạnh nhau
            const btnCompare = card.querySelector('.btn-trigger-compare');
            if (btnCompare) {
                btnCompare.addEventListener('click', function () {
                    openCompareModal(pair.recordA, pair.recordB, pair);
                });
            }

            containerDanhSachTrung.appendChild(card);
        });
    }

    // =========================================================================
    // 4. Modal So Sánh Cạnh Nhau Trước Khi Gộp (AC2, AC3, AC4)
    // =========================================================================

    function openCompareModal(dest, src, pairInfo) {
        if (!modalSoSanhGop) return;
        currentComparePair = { dest: dest, src: src, pairInfo: pairInfo };

        renderCompareTable();

        modalSoSanhGop.style.display = 'flex';
        modalSoSanhGop.classList.add('show');
        document.body.style.overflow = 'hidden';

        if (lyDoGop) {
            lyDoGop.value = 'Phát hiện khách hàng trùng lặp giữa ' + dest.ownerName + ' và ' + src.ownerName +
                '. Gộp để thống nhất một đầu mối chăm sóc và bảo toàn toàn bộ dữ liệu lịch sử.';
        }
    }

    function renderCompareTable() {
        if (!currentComparePair) return;
        const dest = currentComparePair.dest;
        const src = currentComparePair.src;

        // Hidden input IDs
        if (compareKhachHangDichId) compareKhachHangDichId.value = dest.id;
        if (compareKhachHangNguonId) compareKhachHangNguonId.value = src.id;

        // Cập nhật cảnh báo xung đột trong modal
        const conflictAlert = document.getElementById('compareConflictAlert');
        const conflictText = document.getElementById('compareConflictText');
        if (conflictAlert && conflictText) {
            if (dest.ownerId !== src.ownerId) {
                conflictAlert.style.display = 'flex';
                conflictText.textContent = dest.ownerName + ' (' + dest.teamName + ') và ' + src.ownerName + ' (' + src.teamName + ') đang cùng chào công ty này!';
            } else {
                conflictAlert.style.display = 'none';
            }
        }

        // Điền dữ liệu vào bảng so sánh cạnh nhau
        setElText('cmpDestMa', dest.ma);
        setElText('cmpSrcMa', src.ma);

        setElText('cmpDestTen', dest.ten);
        setElText('cmpSrcTen', src.ten);

        const isMstMatch = dest.mst && src.mst && cleanTaxCode(dest.mst) === cleanTaxCode(src.mst);
        const elDestMst = document.getElementById('cmpDestMst');
        const elSrcMst = document.getElementById('cmpSrcMst');
        if (elDestMst) {
            elDestMst.textContent = dest.mst || '(Chưa có)';
            elDestMst.className = 'compare-val-text font-mono ' + (isMstMatch ? 'text-highlight' : '');
        }
        if (elSrcMst) {
            elSrcMst.textContent = src.mst || '(Chưa có)';
            elSrcMst.className = 'compare-val-text font-mono ' + (isMstMatch ? 'text-highlight' : '');
        }

        const isWebMatch = dest.web && src.web && cleanWebsite(dest.web) === cleanWebsite(src.web);
        const elDestWeb = document.getElementById('cmpDestWeb');
        const elSrcWeb = document.getElementById('cmpSrcWeb');
        if (elDestWeb) {
            elDestWeb.textContent = dest.web || '(Chưa có)';
            elDestWeb.className = 'compare-val-text ' + (isWebMatch ? 'text-highlight' : '');
        }
        if (elSrcWeb) {
            elSrcWeb.textContent = src.web || '(Chưa có)';
            elSrcWeb.className = 'compare-val-text ' + (isWebMatch ? 'text-highlight' : '');
        }

        setElText('cmpDestOwner', dest.ownerName);
        setElText('cmpSrcOwner', src.ownerName);

        setElText('cmpDestTeam', dest.teamName || '-');
        setElText('cmpSrcTeam', src.teamName || '-');

        setElText('cmpDestGiaTri', dest.giaTri || '-');
        setElText('cmpSrcGiaTri', src.giaTri || '-');

        setElText('cmpDestTrangThai', dest.trangThai || 'Tiềm năng');
        setElText('cmpSrcTrangThai', src.trangThai || 'Tiềm năng');

        setElText('cmpDestNgayTao', dest.ngayTao || '-');
        setElText('cmpSrcNgayTao', src.ngayTao || '-');

        // AC3: Bảo toàn số lượng Người liên hệ, Cơ hội và Hoạt động
        setElText('cmpDestNlh', '1 người liên hệ (Đầu mối)');
        setElText('cmpSrcNlh', '1 người liên hệ (Được giữ lại 100%)');

        setElText('cmpDestCoHoi', '1 cơ hội bán hàng');
        setElText('cmpSrcCoHoi', '1 cơ hội bán hàng (Được giữ lại 100%)');

        setElText('cmpDestHoatDong', '1 hoạt động chăm sóc');
        setElText('cmpSrcHoatDong', '1 hoạt động (Lịch sử được gom vào khách đích)');
    }

    function closeCompareModal() {
        if (!modalSoSanhGop) return;
        modalSoSanhGop.classList.remove('show');
        modalSoSanhGop.style.display = 'none';
        document.body.style.overflow = '';
        currentComparePair = null;
    }

    // Đổi vị trí giữa Bản ghi đích và Bản ghi nguồn
    if (btnHoanDoiViTri) {
        btnHoanDoiViTri.addEventListener('click', function () {
            if (!currentComparePair) return;
            const temp = currentComparePair.dest;
            currentComparePair.dest = currentComparePair.src;
            currentComparePair.src = temp;
            renderCompareTable();
        });
    }

    if (btnDongModalSoSanh) {
        btnDongModalSoSanh.addEventListener('click', closeCompareModal);
    }
    if (btnHuySoSanhGop) {
        btnHuySoSanhGop.addEventListener('click', closeCompareModal);
    }
    if (modalSoSanhGop) {
        modalSoSanhGop.addEventListener('click', function (e) {
            if (e.target === modalSoSanhGop) closeCompareModal();
        });
    }

    // Submit form xác nhận gộp (Kiểm tra quyền AC4)
    if (formXacNhanGop) {
        formXacNhanGop.addEventListener('submit', function (e) {
            // Kiểm tra quyền Trưởng nhóm trở lên (AC4)
            const isTeamLeadOrAbove = window.LA_TRUONG_NHOM === true;
            if (!isTeamLeadOrAbove) {
                e.preventDefault();
                alert('CHẶN THAO TÁC (AC4): Chỉ Trưởng nhóm kinh doanh trở lên (Team Lead, Director, Admin) mới có quyền thực hiện gộp khách hàng!');
                return false;
            }

            if (!lyDoGop || !lyDoGop.value.trim()) {
                e.preventDefault();
                alert('Vui lòng nhập lý do thực hiện gộp khách hàng.');
                if (lyDoGop) lyDoGop.focus();
                return false;
            }

            const destName = (currentComparePair && currentComparePair.dest) ? currentComparePair.dest.ten : 'hồ sơ chính';
            const srcName = (currentComparePair && currentComparePair.src) ? currentComparePair.src.ten : 'hồ sơ gộp';

            const confirmed = window.confirm(
                'XÁC NHẬN GỘP KHÁCH HÀNG (Story S3-04):\n\n' +
                'Bạn có chắc chắn muốn gộp khách hàng "' + srcName + '" vào "' + destName + '"?\n\n' +
                '- Toàn bộ Người liên hệ sẽ được chuyển sang hồ sơ chính (AC3)\n' +
                '- Toàn bộ Cơ hội bán hàng sẽ được bảo toàn (AC3)\n' +
                '- Toàn bộ Hoạt động chăm sóc sẽ được lưu trữ (AC3)\n' +
                '- Hành động này sẽ được ghi nhận vào nhật ký kiểm toán.'
            );

            if (!confirmed) {
                e.preventDefault();
                return false;
            }
        });
    }

    // Bấm nút so sánh trực tiếp từ bảng danh sách
    document.addEventListener('click', function (e) {
        const btnRow = e.target.closest('.btn-row-compare-trigger');
        if (btnRow) {
            e.preventDefault();
            const customerId = btnRow.getAttribute('data-id');
            // Tìm cặp trùng có chứa customerId này
            const matchedPair = detectedDuplicatePairs.find(function (p) {
                return p.recordA.id === customerId || p.recordB.id === customerId;
            });
            if (matchedPair) {
                if (matchedPair.recordA.id === customerId) {
                    openCompareModal(matchedPair.recordA, matchedPair.recordB, matchedPair);
                } else {
                    openCompareModal(matchedPair.recordB, matchedPair.recordA, matchedPair);
                }
            } else {
                // Chuyển sang Tab Trung để xem toàn bộ
                switchTab('trung');
            }
        }
    });

    // =========================================================================
    // 5. Quản lý Tab Điều Hướng (View Switcher)
    // =========================================================================

    function switchTab(targetTab) {
        if (targetTab === 'trung') {
            if (tabBtnTatCa) {
                tabBtnTatCa.classList.remove('active');
                tabBtnTatCa.setAttribute('aria-selected', 'false');
            }
            if (tabBtnTrungLap) {
                tabBtnTrungLap.classList.add('active');
                tabBtnTrungLap.setAttribute('aria-selected', 'true');
            }
            if (tabPaneTatCa) tabPaneTatCa.style.display = 'none';
            if (tabPaneTrungLap) tabPaneTrungLap.style.display = 'block';
        } else {
            if (tabBtnTrungLap) {
                tabBtnTrungLap.classList.remove('active');
                tabBtnTrungLap.setAttribute('aria-selected', 'false');
            }
            if (tabBtnTatCa) {
                tabBtnTatCa.classList.add('active');
                tabBtnTatCa.setAttribute('aria-selected', 'true');
            }
            if (tabPaneTrungLap) tabPaneTrungLap.style.display = 'none';
            if (tabPaneTatCa) tabPaneTatCa.style.display = 'block';
        }
    }

    if (tabBtnTatCa) {
        tabBtnTatCa.addEventListener('click', function () {
            switchTab('tat-ca');
        });
    }
    if (tabBtnTrungLap) {
        tabBtnTrungLap.addEventListener('click', function () {
            switchTab('trung');
        });
    }
    if (btnChuyenTabTrung) {
        btnChuyenTabTrung.addEventListener('click', function () {
            switchTab('trung');
        });
    }
    if (btnXemCapTrungTuBanner) {
        btnXemCapTrungTuBanner.addEventListener('click', function () {
            switchTab('trung');
        });
    }

    // Lọc theo chip tiêu chí trong Tab 2
    filterChips.forEach(function (chip) {
        chip.addEventListener('click', function () {
            filterChips.forEach(function (c) { c.classList.remove('active'); });
            chip.classList.add('active');
            currentActiveFilter = chip.getAttribute('data-filter') || 'all';
            renderDuplicateCards();
        });
    });

    if (inputTimKiemTrung) {
        inputTimKiemTrung.addEventListener('input', function () {
            renderDuplicateCards();
        });
    }

    // =========================================================================
    // 6. Cảnh báo trùng lặp Real-time khi thêm khách hàng mới
    // =========================================================================

    function checkInlineDuplicates() {
        const valName = inputTenCongTy ? inputTenCongTy.value.trim() : '';
        const valMst = inputMaSoThueThem ? cleanTaxCode(inputMaSoThueThem.value) : '';
        const valWeb = inputWebsiteThem ? cleanWebsite(inputWebsiteThem.value) : '';

        // 1. Kiểm tra trùng Tên
        if (valName && valName.length >= 3) {
            const matchedCustomer = currentCustomerList.find(function (c) {
                return calcSimilarity(valName, c.ten) >= 0.70;
            });
            if (matchedCustomer && inlineDupNameAlert && dupMatchedName && dupMatchedOwner) {
                dupMatchedName.textContent = matchedCustomer.ten;
                dupMatchedOwner.textContent = matchedCustomer.ownerName || 'nhân viên khác';
                inlineDupNameAlert.style.display = 'flex';
            } else if (inlineDupNameAlert) {
                inlineDupNameAlert.style.display = 'none';
            }
        } else if (inlineDupNameAlert) {
            inlineDupNameAlert.style.display = 'none';
        }

        // 2. Kiểm tra trùng MST
        if (valMst && valMst.length >= 6) {
            const matchedMst = currentCustomerList.find(function (c) {
                return c.mstClean && c.mstClean === valMst;
            });
            if (matchedMst && inlineDupMstAlert && dupMatchedMstName) {
                dupMatchedMstName.textContent = matchedMst.ten + ' (' + matchedMst.ownerName + ')';
                inlineDupMstAlert.style.display = 'flex';
            } else if (inlineDupMstAlert) {
                inlineDupMstAlert.style.display = 'none';
            }
        } else if (inlineDupMstAlert) {
            inlineDupMstAlert.style.display = 'none';
        }

        // 3. Kiểm tra trùng Website
        if (valWeb && valWeb.length >= 4) {
            const matchedWeb = currentCustomerList.find(function (c) {
                return c.webClean && c.webClean === valWeb;
            });
            if (matchedWeb && inlineDupWebAlert && dupMatchedWebName) {
                dupMatchedWebName.textContent = matchedWeb.ten + ' (' + matchedWeb.ownerName + ')';
                inlineDupWebAlert.style.display = 'flex';
            } else if (inlineDupWebAlert) {
                inlineDupWebAlert.style.display = 'none';
            }
        } else if (inlineDupWebAlert) {
            inlineDupWebAlert.style.display = 'none';
        }
    }

    if (inputTenCongTy) {
        inputTenCongTy.addEventListener('input', debounce(checkInlineDuplicates, 250));
    }
    if (inputMaSoThueThem) {
        inputMaSoThueThem.addEventListener('input', debounce(checkInlineDuplicates, 250));
    }
    if (inputWebsiteThem) {
        inputWebsiteThem.addEventListener('input', debounce(checkInlineDuplicates, 250));
    }

    // =========================================================================
    // 7. Modal Thêm khách hàng mới & Xuất Excel cơ bản
    // =========================================================================

    function moModalThem() {
        if (!modalThemKhachHang) return;
        modalThemKhachHang.style.display = 'flex';
        modalThemKhachHang.classList.add('show');
        document.body.style.overflow = 'hidden';
        if (inputTenCongTy) {
            setTimeout(function () { inputTenCongTy.focus(); }, 100);
        }
    }

    function dongModalThem() {
        if (!modalThemKhachHang) return;
        modalThemKhachHang.classList.remove('show');
        modalThemKhachHang.style.display = 'none';
        document.body.style.overflow = '';
        if (inlineDupNameAlert) inlineDupNameAlert.style.display = 'none';
        if (inlineDupMstAlert) inlineDupMstAlert.style.display = 'none';
        if (inlineDupWebAlert) inlineDupWebAlert.style.display = 'none';
    }

    if (btnThemKhachHang) {
        btnThemKhachHang.addEventListener('click', function (e) {
            e.preventDefault();
            moModalThem();
        });
    }
    if (btnDongModalThem) btnDongModalThem.addEventListener('click', dongModalThem);
    if (btnHuyThem) btnHuyThem.addEventListener('click', dongModalThem);
    if (modalThemKhachHang) {
        modalThemKhachHang.addEventListener('click', function (e) {
            if (e.target === modalThemKhachHang) dongModalThem();
        });
    }

    if (formThemKhachHang) {
        formThemKhachHang.addEventListener('submit', function (e) {
            if (inputTenCongTy && !inputTenCongTy.value.trim()) {
                e.preventDefault();
                alert('Vui lòng nhập Tên khách hàng / Công ty.');
                inputTenCongTy.focus();
                return false;
            }
        });
    }

    if (btnExportExcel) {
        btnExportExcel.addEventListener('click', function (e) {
            e.preventDefault();
            const contextPath = window.CONTEXT_PATH || '';
            const urlParams = new URLSearchParams(window.location.search);
            urlParams.set('xuatExcel', 'true');
            window.location.href = contextPath + '/khach-hang?' + urlParams.toString();
        });
    }

    // Đóng modal khi bấm ESC
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') {
            if (modalSoSanhGop && modalSoSanhGop.style.display === 'flex') {
                closeCompareModal();
            } else if (modalThemKhachHang && modalThemKhachHang.style.display === 'flex') {
                dongModalThem();
            }
        }
    });

    // =========================================================================
    // 8. Tiện ích bổ trợ (Helpers)
    // =========================================================================

    function setElText(id, text) {
        const el = document.getElementById(id);
        if (el) el.textContent = text || '-';
    }

    function escapeHtml(text) {
        if (!text) return '';
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return text.replace(/[&<>"']/g, function (m) { return map[m]; });
    }

    function debounce(fn, delay) {
        let timer = null;
        return function () {
            const context = this;
            const args = arguments;
            clearTimeout(timer);
            timer = setTimeout(function () {
                fn.apply(context, args);
            }, delay);
        };
    }

    // =========================================================================
    // 9. Khởi chạy quét trùng lặp khi nạp trang
    // =========================================================================
    currentCustomerList = extractCustomersFromTable();
    detectDuplicatePairs();
    updateUiDuplicateStatus();

    // Mở đúng tab nếu có yêu cầu từ URL hoặc server
    if (window.TAB_HIEN_TAI === 'trung') {
        switchTab('trung');
    }
});
