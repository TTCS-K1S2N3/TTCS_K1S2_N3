/**
 * CRM BÁN HÀNG - XỬ LÝ FORM GÁN VAI TRÒ & NHÓM KINH DOANH (STORY S1-09)
 * Tương tác giao diện thời gian thực và kiểm tra tính hợp lệ trước khi submit.
 */
document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('form-assign-role');
    const chkRoles = document.querySelectorAll('.crm-checkbox-input');
    const selectNhom = document.getElementById('select-nhom-kd');
    const helperTeamLead = document.getElementById('helper-team-lead');

    // Cập nhật giao diện card khi checkbox thay đổi
    chkRoles.forEach(function (chk) {
        chk.addEventListener('change', function () {
            const card = this.closest('.crm-role-checkbox-card');
            if (card) {
                if (this.checked) {
                    card.classList.add('checked');
                } else {
                    card.classList.remove('checked');
                }
            }
            kiemTraVaiTroTruongNhom();
        });
    });

    function kiemTraVaiTroTruongNhom() {
        let isTeamLeadChecked = false;
        chkRoles.forEach(function (chk) {
            if (chk.checked && chk.getAttribute('data-code') === 'TEAM_LEAD') {
                isTeamLeadChecked = true;
            }
        });

        if (helperTeamLead && selectNhom) {
            if (isTeamLeadChecked) {
                helperTeamLead.style.color = '#b45309';
                helperTeamLead.style.fontWeight = '600';
                if (!selectNhom.value) {
                    selectNhom.style.borderColor = '#f59e0b';
                }
            } else {
                helperTeamLead.style.color = '';
                helperTeamLead.style.fontWeight = '';
                selectNhom.style.borderColor = '';
            }
        }
    }

    if (selectNhom) {
        selectNhom.addEventListener('change', function () {
            if (this.value) {
                this.style.borderColor = '#16a34a';
            } else {
                kiemTraVaiTroTruongNhom();
            }
        });
    }

    if (form) {
        form.addEventListener('submit', function (e) {
            let hasAtLeastOneRole = false;
            let isTeamLeadSelected = false;

            chkRoles.forEach(function (chk) {
                if (chk.checked) {
                    hasAtLeastOneRole = true;
                    if (chk.getAttribute('data-code') === 'TEAM_LEAD') {
                        isTeamLeadSelected = true;
                    }
                }
            });

            if (!hasAtLeastOneRole) {
                e.preventDefault();
                alert('Vui lòng chọn ít nhất một vai trò cho người dùng!');
                return;
            }

            if (isTeamLeadSelected && (!selectNhom || !selectNhom.value)) {
                e.preventDefault();
                alert('Tiêu chuẩn nghiệm thu S1-09: Người giữ vai trò Trưởng nhóm kinh doanh bắt buộc phải được gán vào một nhóm kinh doanh cụ thể!');
                if (selectNhom) {
                    selectNhom.focus();
                    selectNhom.style.borderColor = '#ef4444';
                }
                return;
            }
        });
    }

    // Khởi tạo kiểm tra ban đầu
    kiemTraVaiTroTruongNhom();
});
