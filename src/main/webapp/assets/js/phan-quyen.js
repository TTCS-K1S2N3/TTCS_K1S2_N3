/**
 * CRM BÁN HÀNG - XỬ LÝ GÁN VAI TRÒ & NHÓM KINH DOANH (STORY S1-09 - FE)
 * Tác giả: Ngô Trung Kiên (FE)
 * Nghiệp vụ & Tiêu chuẩn nghiệm thu:
 * - AC1: Một người dùng có thể giữ nhiều vai trò cùng lúc (Đa vai trò)
 * - AC2: Người giữ vai trò Trưởng nhóm (TEAM_LEAD) bắt buộc phải được gán một nhóm kinh doanh cụ thể
 * - AC3: Không thể tự thu hồi vai trò quản trị (Admin) của chính mình
 * - UX: Lọc tìm kiếm người dùng tức thì, xem trước phạm vi dữ liệu thời gian thực, Toast thông báo,
 *       ngăn chặn double-click submit và hỗ trợ hoàn hảo cho màn hình 360px.
 */

document.addEventListener('DOMContentLoaded', function () {
    // 1. DOM Elements
    const form = document.getElementById('form-assign-role');
    const chkRoles = document.querySelectorAll('.crm-checkbox-input');
    const selectNhom = document.getElementById('select-nhom-kd');
    const cardNhom = document.getElementById('card-nhom-kd');
    const helperTeamLead = document.getElementById('helper-team-lead');
    const teamErrorMessage = document.getElementById('team-error-message');
    const teamRequiredBadge = document.getElementById('team-required-badge');
    const roleCounter = document.getElementById('selected-role-count');
    const btnSubmit = document.getElementById('btn-save-assignment');
    const btnReset = document.getElementById('btn-reset-form');

    // Scope Preview Elements
    const previewScopeIndicator = document.getElementById('preview-scope-indicator');
    const previewScopeTitle = document.getElementById('preview-scope-title');
    const previewScopeDesc = document.getElementById('preview-scope-desc');
    const previewPermList = document.getElementById('preview-perm-list');

    // User Search Elements
    const searchInput = document.getElementById('user-search-input');
    const searchClearBtn = document.getElementById('user-search-clear');
    const userItems = document.querySelectorAll('.crm-user-select-item');
    const userCountBadge = document.getElementById('user-display-count');
    const userEmptyState = document.getElementById('user-empty-state');

    // Kiểm tra cờ tài khoản hiện tại (AC 3: Không thể tự thu hồi Admin)
    const isEditingSelf = form && form.getAttribute('data-is-self') === 'true';

    // Lưu trữ trạng thái ban đầu để hỗ trợ nút "Khôi phục ban đầu"
    const initialRoleStates = {};
    chkRoles.forEach(function (chk) {
        initialRoleStates[chk.value] = chk.checked;
    });
    const initialTeamValue = selectNhom ? selectNhom.value : '';

    // ====================================================================
    // A. TOAST NOTIFICATION SYSTEM
    // ====================================================================
    let toastContainer = document.getElementById('crm-toast-container');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.id = 'crm-toast-container';
        toastContainer.className = 'crm-toast-container';
        document.body.appendChild(toastContainer);
    }

    function showToast(type, message, duration = 4500) {
        const toast = document.createElement('div');
        toast.className = `crm-toast crm-toast-${type}`;

        let iconSvg = '';
        if (type === 'success') {
            iconSvg = '<svg class="crm-toast-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>';
        } else if (type === 'warning') {
            iconSvg = '<svg class="crm-toast-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>';
        } else {
            iconSvg = '<svg class="crm-toast-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line></svg>';
        }

        toast.innerHTML = `
            ${iconSvg}
            <div style="flex: 1;">${message}</div>
            <button type="button" class="crm-toast-close" aria-label="Đóng">&times;</button>
        `;

        const closeBtn = toast.querySelector('.crm-toast-close');
        closeBtn.addEventListener('click', function () {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 250);
        });

        toastContainer.appendChild(toast);
        requestAnimationFrame(() => toast.classList.add('show'));

        setTimeout(() => {
            if (toast.parentElement) {
                toast.classList.remove('show');
                setTimeout(() => toast.remove(), 250);
            }
        }, duration);
    }

    // ====================================================================
    // B. USER SEARCH & INSTANT FILTERING
    // ====================================================================
    if (searchInput && userItems.length > 0) {
        searchInput.addEventListener('input', function () {
            const query = this.value.trim().toLowerCase();
            let visibleCount = 0;

            if (searchClearBtn) {
                searchClearBtn.style.display = query ? 'flex' : 'none';
            }

            userItems.forEach(function (item) {
                const name = (item.getAttribute('data-name') || '').toLowerCase();
                const email = (item.getAttribute('data-email') || '').toLowerCase();
                const roles = (item.getAttribute('data-roles') || '').toLowerCase();

                if (!query || name.includes(query) || email.includes(query) || roles.includes(query)) {
                    item.style.display = 'flex';
                    visibleCount++;
                } else {
                    item.style.display = 'none';
                }
            });

            if (userCountBadge) {
                userCountBadge.textContent = `Hiển thị: ${visibleCount} / ${userItems.length}`;
            }

            if (userEmptyState) {
                userEmptyState.style.display = visibleCount === 0 ? 'block' : 'none';
            }
        });

        if (searchClearBtn) {
            searchClearBtn.addEventListener('click', function () {
                searchInput.value = '';
                searchInput.dispatchEvent(new Event('input'));
                searchInput.focus();
            });
        }
    }

    // ====================================================================
    // C. AC 3: BẢO VỆ VAI TRÒ QUẢN TRỊ VIÊN CỦA CHÍNH MÌNH
    // ====================================================================
    function setupAdminProtection() {
        if (!isEditingSelf) return;

        chkRoles.forEach(function (chk) {
            const code = chk.getAttribute('data-code');
            if (code === 'ADMIN' && chk.checked) {
                const card = chk.closest('.crm-role-checkbox-card');
                if (card) {
                    card.classList.add('protected');
                }

                // Chặn hành động bỏ tick trực tiếp trên checkbox
                chk.addEventListener('click', function (e) {
                    if (!this.checked) {
                        e.preventDefault();
                        this.checked = true;
                        if (card) {
                            card.classList.add('shake');
                            setTimeout(() => card.classList.remove('shake'), 400);
                        }
                        showToast('warning', '<strong>Quy tắc bảo mật (AC 3):</strong> Bạn không thể tự thu hồi vai trò Quản trị hệ thống (Admin) của chính tài khoản mình đang đăng nhập!');
                    }
                });
            }
        });
    }

    // ====================================================================
    // D. AC 1: ĐA VAI TRÒ & XEM TRƯỚC PHÂN QUYỀN THỜI GIAN THỰC
    // ====================================================================
    function updateRoleUI() {
        let count = 0;
        const selectedCodes = [];

        chkRoles.forEach(function (chk) {
            const card = chk.closest('.crm-role-checkbox-card');
            const code = chk.getAttribute('data-code');

            if (chk.checked) {
                count++;
                selectedCodes.push(code);
                if (card) card.classList.add('checked');
            } else {
                if (card) card.classList.remove('checked');
            }
        });

        if (roleCounter) {
            roleCounter.textContent = `Đã chọn: ${count} vai trò`;
            if (count === 0) {
                roleCounter.className = 'crm-badge crm-badge-warning';
            } else {
                roleCounter.className = 'crm-badge crm-badge-info';
            }
        }

        // Cập nhật xem trước phạm vi dữ liệu (Live Scope Preview)
        updateScopePreview(selectedCodes);

        // Kiểm tra điều kiện AC 2 (Trưởng nhóm bắt buộc có nhóm kinh doanh)
        kiemTraDieuKienTruongNhom(selectedCodes.includes('TEAM_LEAD'));
    }

    function updateScopePreview(codes) {
        if (!previewScopeTitle || !previewScopeDesc || !previewPermList) return;

        const permChips = [];

        if (codes.includes('ADMIN')) {
            permChips.push('Quản trị toàn hệ thống', 'Quản lý người dùng & phân quyền', 'Xem nhật ký bảo mật', 'Cấu hình CRM');
        }
        if (codes.includes('DIRECTOR')) {
            permChips.push('Toàn quyền xem khách hàng cty', 'Duyệt báo giá toàn công ty', 'Xem báo cáo doanh thu tổng');
        }
        if (codes.includes('TEAM_LEAD')) {
            permChips.push('Quản lý thành viên nhóm', 'Duyệt chiết khấu cấp nhóm', 'Xem khách hàng của nhóm', 'Giao khách hàng trong nhóm');
        }
        if (codes.includes('SALES_REP')) {
            permChips.push('Quản lý khách hàng cá nhân', 'Tạo cơ hội & báo giá', 'Chăm sóc hợp đồng được giao');
        }
        if (codes.includes('MARKETING')) {
            permChips.push('Thu thập & quản lý Lead', 'Thiết lập chiến dịch tiếp thị', 'Chuyển đổi khách tiềm năng');
        }
        if (codes.includes('CUST_SUCCESS')) {
            permChips.push('Chăm sóc khách sau bán', 'Tiếp nhận yêu cầu hỗ trợ', 'Đánh giá độ hài lòng KH');
        }
        if (codes.includes('ACCOUNTANT')) {
            permChips.push('Theo dõi công nợ', 'Xác nhận thanh toán hợp đồng', 'Báo cáo tài chính đơn hàng');
        }

        // Xác định Data Scope cao nhất
        if (codes.includes('ADMIN') || codes.includes('DIRECTOR')) {
            previewScopeIndicator.style.background = '#7c3aed';
            previewScopeIndicator.style.boxShadow = '0 0 0 4px rgba(124, 58, 237, 0.2)';
            previewScopeTitle.textContent = 'TOÀN BỘ HỆ THỐNG (All Data Scope)';
            previewScopeDesc.textContent = 'Nhìn thấy toàn bộ danh sách khách hàng, cơ hội, báo giá và doanh thu của toàn doanh nghiệp.';
        } else if (codes.includes('TEAM_LEAD')) {
            previewScopeIndicator.style.background = '#d97706';
            previewScopeIndicator.style.boxShadow = '0 0 0 4px rgba(217, 119, 6, 0.2)';
            previewScopeTitle.textContent = 'THEO NHÓM KINH DOANH (Team Scope)';
            previewScopeDesc.textContent = 'Nhìn thấy và quản lý dữ liệu của toàn bộ nhân viên kinh doanh trực thuộc nhóm kinh doanh được gán.';
        } else if (codes.length > 0) {
            previewScopeIndicator.style.background = '#2563eb';
            previewScopeIndicator.style.boxShadow = '0 0 0 4px rgba(37, 99, 235, 0.2)';
            previewScopeTitle.textContent = 'CÁ NHÂN & PHÂN CÔNG (Personal Scope)';
            previewScopeDesc.textContent = 'Chỉ nhìn thấy các khách hàng, cơ hội và dữ liệu được phân công trực tiếp cho cá nhân người dùng.';
        } else {
            previewScopeIndicator.style.background = '#94a3b8';
            previewScopeIndicator.style.boxShadow = '0 0 0 4px rgba(148, 163, 184, 0.2)';
            previewScopeTitle.textContent = 'CHƯA XÁC ĐỊNH (Chưa chọn vai trò nào)';
            previewScopeDesc.textContent = 'Vui lòng chọn ít nhất một vai trò để xác định quyền hạn và phạm vi dữ liệu.';
        }

        // Render chips
        if (permChips.length > 0) {
            previewPermList.innerHTML = permChips.map(p => `<span class="crm-perm-chip">✓ ${p}</span>`).join('');
        } else {
            previewPermList.innerHTML = '<span style="font-size: 12px; color: #94a3b8;">Chưa có quyền hạn nào được cấp.</span>';
        }
    }

    // ====================================================================
    // E. AC 2: RÀNG BUỘC TRƯỞNG NHÓM PHẢI GẮN VÀO NHÓM CỤ THỂ
    // ====================================================================
    function kiemTraDieuKienTruongNhom(isTeamLead) {
        if (!cardNhom || !selectNhom) return;

        if (isTeamLead) {
            cardNhom.classList.add('team-required-active');
            if (teamRequiredBadge) {
                teamRequiredBadge.style.display = 'inline-flex';
            }

            if (!selectNhom.value) {
                selectNhom.className = 'crm-select error';
                if (helperTeamLead) {
                    helperTeamLead.className = 'crm-input-helper warn';
                    helperTeamLead.innerHTML = '<strong>⚠️ Tiêu chuẩn nghiệm thu S1-09:</strong> Người giữ vai trò <em>Trưởng nhóm kinh doanh</em> bắt buộc phải được gán vào một nhóm kinh doanh cụ thể!';
                }
            } else {
                selectNhom.className = 'crm-select success';
                if (teamErrorMessage) teamErrorMessage.style.display = 'none';
                if (helperTeamLead) {
                    helperTeamLead.className = 'crm-input-helper valid';
                    const selectedText = selectNhom.options[selectNhom.selectedIndex].text;
                    helperTeamLead.innerHTML = `<strong>✓ Hợp lệ:</strong> Đã gắn vào <strong>${selectedText}</strong>. Cây phân quyền nhóm đã được kích hoạt.`;
                }
            }
        } else {
            cardNhom.classList.remove('team-required-active', 'team-error');
            if (teamRequiredBadge) {
                teamRequiredBadge.style.display = 'none';
            }
            if (teamErrorMessage) teamErrorMessage.style.display = 'none';

            if (selectNhom.value) {
                selectNhom.className = 'crm-select success';
                if (helperTeamLead) {
                    helperTeamLead.className = 'crm-input-helper';
                    helperTeamLead.innerHTML = 'Người dùng trực thuộc nhóm kinh doanh để kế thừa phạm vi dữ liệu.';
                }
            } else {
                selectNhom.className = 'crm-select';
                if (helperTeamLead) {
                    helperTeamLead.className = 'crm-input-helper';
                    helperTeamLead.innerHTML = 'Người dùng chưa phân nhóm. Lưu ý: Nếu gán vai trò Trưởng nhóm sẽ bắt buộc chọn nhóm.';
                }
            }
        }
    }

    // Gắn sự kiện lắng nghe thay đổi checkbox vai trò
    chkRoles.forEach(function (chk) {
        chk.addEventListener('change', function () {
            updateRoleUI();
        });
    });

    if (selectNhom) {
        selectNhom.addEventListener('change', function () {
            let isTeamLead = false;
            chkRoles.forEach(function (chk) {
                if (chk.checked && chk.getAttribute('data-code') === 'TEAM_LEAD') {
                    isTeamLead = true;
                }
            });
            kiemTraDieuKienTruongNhom(isTeamLead);
        });
    }

    // ====================================================================
    // F. NÚT TIỆN ÍCH: KHÔI PHỤC BAN ĐẦU
    // ====================================================================
    if (btnReset) {
        btnReset.addEventListener('click', function (e) {
            e.preventDefault();
            chkRoles.forEach(function (chk) {
                chk.checked = !!initialRoleStates[chk.value];
            });
            if (selectNhom) {
                selectNhom.value = initialTeamValue;
            }
            updateRoleUI();
            showToast('success', 'Đã khôi phục trạng thái phân quyền và nhóm ban đầu.');
        });
    }

    // ====================================================================
    // G. FORM SUBMIT VALIDATION & LOADING STATE
    // ====================================================================
    if (form) {
        form.addEventListener('submit', function (e) {
            let hasAtLeastOneRole = false;
            let isTeamLeadSelected = false;
            let isAdminSelected = false;

            chkRoles.forEach(function (chk) {
                if (chk.checked) {
                    hasAtLeastOneRole = true;
                    const code = chk.getAttribute('data-code');
                    if (code === 'TEAM_LEAD') isTeamLeadSelected = true;
                    if (code === 'ADMIN') isAdminSelected = true;
                }
            });

            // 1. Kiểm tra phải chọn ít nhất một vai trò
            if (!hasAtLeastOneRole) {
                e.preventDefault();
                showToast('error', '<strong>Lỗi phân quyền:</strong> Người dùng phải được gán ít nhất một vai trò trong hệ thống!');
                const rolesCard = document.querySelector('.crm-roles-grid');
                if (rolesCard) {
                    rolesCard.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
                return;
            }

            // 2. AC 2: Trưởng nhóm bắt buộc phải có nhóm kinh doanh cụ thể
            if (isTeamLeadSelected && (!selectNhom || !selectNhom.value || selectNhom.value <= 0)) {
                e.preventDefault();
                if (cardNhom) {
                    cardNhom.classList.add('team-error', 'shake');
                    setTimeout(() => cardNhom.classList.remove('shake'), 400);
                    cardNhom.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
                if (selectNhom) {
                    selectNhom.className = 'crm-select error';
                    selectNhom.focus();
                }
                if (teamErrorMessage) {
                    teamErrorMessage.style.display = 'flex';
                }
                showToast('error', '<strong>Tiêu chuẩn nghiệm thu S1-09:</strong> Người giữ vai trò Trưởng nhóm kinh doanh bắt buộc phải được gán vào một nhóm kinh doanh cụ thể!');
                return;
            }

            // 3. AC 3: Không thể tự thu hồi quyền Admin của chính mình
            if (isEditingSelf && !isAdminSelected) {
                e.preventDefault();
                showToast('error', '<strong>Quy tắc bảo mật (AC 3):</strong> Bạn không thể tự thu hồi vai trò Quản trị hệ thống (Admin) của chính tài khoản mình đang đăng nhập!');
                return;
            }

            // Hợp lệ: Kích hoạt trạng thái loading cho nút bấm để ngăn submit trùng lặp
            if (btnSubmit) {
                btnSubmit.disabled = true;
                btnSubmit.innerHTML = '<span class="crm-spinner"></span> Đang lưu phân quyền...';
            }
        });
    }

    // ====================================================================
    // H. KHỞI TẠO BAN ĐẦU KHI TẢI TRANG
    // ====================================================================
    setupAdminProtection();
    updateRoleUI();
});
