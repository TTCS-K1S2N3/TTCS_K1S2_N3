/**
 * JavaScript cho Story S2-07: Khai báo các danh mục dùng chung của bán hàng
 * Tác giả: Bàn Thị Linh (FE)
 * Tuân thủ CODING_RULES.md (Vanilla JavaScript thuần)
 * Acceptance Criteria:
 * - 4 nhóm danh mục: Ngành nghề, Quy mô, Nguồn lead, Loại hoạt động (AC1)
 * - Giá trị đang được tham chiếu thì KHÔNG xóa được (AC2)
 * - Sắp xếp được thứ tự hiển thị (AC3)
 */

document.addEventListener("DOMContentLoaded", function () {
    khoiTaoModalThemMuc();
    khoiTaoModalSuaMuc();
    khoiTaoModalXoaMuc();
    khoiTaoModalChanXoaAC2();
    khoiTaoTimKiemVaLocNhanh();
    khoiTaoKeoThaThuTuHang();
    khoiTaoPhimTat();
});

/**
 * ==========================================================================
 * 1. MODAL THÊM MỚI MỤC DANH MỤC (AC1)
 * ==========================================================================
 */
function khoiTaoModalThemMuc() {
    const btnThem = document.getElementById("btnOpenAddModal");
    const modal = document.getElementById("modalAddCategory");
    const btnClose = document.getElementById("btnCloseAddModal");
    const btnCancel = document.getElementById("btnCancelAddModal");
    const inputMa = document.getElementById("addMaMuc");
    const formAdd = document.getElementById("formAddCategory");

    if (btnThem && modal) {
        btnThem.addEventListener("click", function () {
            modal.style.display = "flex";
            document.body.style.overflow = "hidden";
            if (inputMa) {
                inputMa.value = "";
                inputMa.focus();
            }
        });
    }

    const dongModal = function () {
        if (modal) {
            modal.style.display = "none";
            document.body.style.overflow = "";
        }
    };

    if (btnClose) btnClose.addEventListener("click", dongModal);
    if (btnCancel) btnCancel.addEventListener("click", dongModal);
    if (modal) {
        modal.addEventListener("click", function (e) {
            if (e.target === modal) dongModal();
        });
    }

    // Tự động chuẩn hóa mã: Chuyển in hoa và thay ký tự đặc biệt thành dấu gạch dưới
    if (inputMa) {
        inputMa.addEventListener("input", function () {
            this.value = this.value.toUpperCase().replace(/\s+/g, "_");
        });
    }

    // Validate form thêm mới
    if (formAdd) {
        formAdd.addEventListener("submit", function (e) {
            const tenMuc = document.getElementById("addTenMuc");
            if (inputMa && !inputMa.value.trim()) {
                e.preventDefault();
                showToast("error", "Vui lòng nhập mã định danh cho mục danh mục.");
                inputMa.focus();
                return;
            }
            if (tenMuc && !tenMuc.value.trim()) {
                e.preventDefault();
                showToast("error", "Vui lòng nhập tên mục hiển thị.");
                tenMuc.focus();
                return;
            }
        });
    }
}

/**
 * ==========================================================================
 * 2. MODAL CHỈNH SỬA MỤC DANH MỤC (AC1)
 * ==========================================================================
 */
function khoiTaoModalSuaMuc() {
    const modal = document.getElementById("modalEditCategory");
    const btnClose = document.getElementById("btnCloseEditModal");
    const btnCancel = document.getElementById("btnCancelEditModal");
    const editBtns = document.querySelectorAll(".btn-edit-item");
    const editUsageAlert = document.getElementById("editUsageAlert");
    const editUsageCount = document.getElementById("editUsageCount");

    editBtns.forEach(function (btn) {
        btn.addEventListener("click", function () {
            const id = btn.getAttribute("data-id");
            const ma = btn.getAttribute("data-ma");
            const ten = btn.getAttribute("data-ten");
            const mota = btn.getAttribute("data-mota");
            const kichhoat = (btn.getAttribute("data-kichhoat") === "true");
            const usage = parseInt(btn.getAttribute("data-usage") || "0", 10);

            const inputId = document.getElementById("editId");
            const inputMa = document.getElementById("editMaMuc");
            const inputTen = document.getElementById("editTenMuc");
            const inputMoTa = document.getElementById("editMoTa");
            const checkKichHoat = document.getElementById("editKichHoat");

            if (inputId) inputId.value = id;
            if (inputMa) inputMa.value = ma;
            if (inputTen) inputTen.value = ten;
            if (inputMoTa) inputMoTa.value = mota;
            if (checkKichHoat) checkKichHoat.checked = kichhoat;

            // Hiển thị cảnh báo nếu mục đang có bản ghi tham chiếu
            if (editUsageAlert && editUsageCount) {
                if (usage > 0) {
                    editUsageCount.textContent = usage;
                    editUsageAlert.style.display = "flex";
                } else {
                    editUsageAlert.style.display = "none";
                }
            }

            if (modal) {
                modal.style.display = "flex";
                document.body.style.overflow = "hidden";
                if (inputTen) inputTen.focus();
            }
        });
    });

    const dongModal = function () {
        if (modal) {
            modal.style.display = "none";
            document.body.style.overflow = "";
        }
    };

    if (btnClose) btnClose.addEventListener("click", dongModal);
    if (btnCancel) btnCancel.addEventListener("click", dongModal);
    if (modal) {
        modal.addEventListener("click", function (e) {
            if (e.target === modal) dongModal();
        });
    }
}

/**
 * ==========================================================================
 * 3. MODAL XÁC NHẬN XÓA (AC2: Giá trị đang tham chiếu thì KHÔNG xóa được)
 * ==========================================================================
 */
function khoiTaoModalXoaMuc() {
    const modal = document.getElementById("modalDeleteCategory");
    const btnClose = document.getElementById("btnCloseDeleteModal");
    const btnCancel = document.getElementById("btnCancelDeleteModal");
    const deleteBtns = document.querySelectorAll(".btn-delete-item");

    deleteBtns.forEach(function (btn) {
        btn.addEventListener("click", function () {
            const id = btn.getAttribute("data-id");
            const ten = btn.getAttribute("data-ten");
            const ma = btn.getAttribute("data-ma");
            const usage = parseInt(btn.getAttribute("data-usage") || "0", 10);

            // Kiểm tra bảo vệ AC2 tại client
            if (usage > 0) {
                moModalChanXoaAC2(ten, usage);
                return;
            }

            const inputDeleteId = document.getElementById("deleteId");
            const displayTen = document.getElementById("deleteTenMucDisplay");
            const displayMa = document.getElementById("deleteMaMucDisplay");

            if (inputDeleteId) inputDeleteId.value = id;
            if (displayTen) displayTen.textContent = ten;
            if (displayMa) displayMa.textContent = ma;

            if (modal) {
                modal.style.display = "flex";
                document.body.style.overflow = "hidden";
            }
        });
    });

    const dongModal = function () {
        if (modal) {
            modal.style.display = "none";
            document.body.style.overflow = "";
        }
    };

    if (btnClose) btnClose.addEventListener("click", dongModal);
    if (btnCancel) btnCancel.addEventListener("click", dongModal);
    if (modal) {
        modal.addEventListener("click", function (e) {
            if (e.target === modal) dongModal();
        });
    }
}

/**
 * ==========================================================================
 * 4. MODAL CẢNH BÁO CHẶN XÓA THEO AC2 (DÀNH CHO NÚT BỊ KHÓA)
 * ==========================================================================
 */
function khoiTaoModalChanXoaAC2() {
    const modal = document.getElementById("modalBlockedDelete");
    const btnClose = document.getElementById("btnCloseBlockedModal");
    const btnDismiss = document.getElementById("btnDismissBlockedModal");
    const disabledDeleteBtns = document.querySelectorAll(".btn-delete-disabled");

    disabledDeleteBtns.forEach(function (btn) {
        btn.addEventListener("click", function () {
            const ten = btn.getAttribute("data-ten") || "Mục danh mục";
            const usage = btn.getAttribute("data-usage") || "nhiều";
            moModalChanXoaAC2(ten, usage);
        });
    });

    const dongModal = function () {
        if (modal) {
            modal.style.display = "none";
            document.body.style.overflow = "";
        }
    };

    if (btnClose) btnClose.addEventListener("click", dongModal);
    if (btnDismiss) btnDismiss.addEventListener("click", dongModal);
    if (modal) {
        modal.addEventListener("click", function (e) {
            if (e.target === modal) dongModal();
        });
    }
}

function moModalChanXoaAC2(tenMuc, soThamChieu) {
    const modal = document.getElementById("modalBlockedDelete");
    const nameSpan = document.getElementById("blockedItemName");
    const usageSpan = document.getElementById("blockedItemUsageCount");

    if (nameSpan) nameSpan.textContent = tenMuc;
    if (usageSpan) usageSpan.textContent = soThamChieu;

    if (modal) {
        modal.style.display = "flex";
        document.body.style.overflow = "hidden";
    }
    showToast("warning", "Mục '" + tenMuc + "' đang có " + soThamChieu + " bản ghi tham chiếu, không thể xóa theo AC2.");
}

/**
 * ==========================================================================
 * 5. TÌM KIẾM & BỘ LỌC REALTIME TRÊN CLIENT
 * ==========================================================================
 */
function khoiTaoTimKiemVaLocNhanh() {
    const searchInput = document.getElementById("categorySearchInput");
    const btnClearSearch = document.getElementById("btnClearSearch");
    const tableBody = document.getElementById("categoryTableBody");
    const filterChips = document.querySelectorAll(".filter-chip");
    const visibleCountSpan = document.getElementById("visibleItemsCount");

    let currentFilter = "all"; // all | active | inactive | referenced

    function apDungLocVaTimKiem() {
        if (!tableBody) return;
        const query = (searchInput ? searchInput.value : "").toLowerCase().trim();
        const rows = tableBody.querySelectorAll("tr.category-data-row");
        let visibleCount = 0;

        if (btnClearSearch) {
            btnClearSearch.style.display = query ? "block" : "none";
        }

        rows.forEach(function (row) {
            const status = row.getAttribute("data-status"); // active | inactive
            const usage = parseInt(row.getAttribute("data-usage") || "0", 10);
            const textContent = row.textContent.toLowerCase();

            // 1. Kiểm tra từ khóa tìm kiếm
            const matchQuery = !query || textContent.includes(query);

            // 2. Kiểm tra bộ lọc
            let matchFilter = true;
            if (currentFilter === "active") {
                matchFilter = (status === "active");
            } else if (currentFilter === "inactive") {
                matchFilter = (status === "inactive");
            } else if (currentFilter === "referenced") {
                matchFilter = (usage > 0);
            }

            if (matchQuery && matchFilter) {
                row.style.display = "";
                visibleCount++;
            } else {
                row.style.display = "none";
            }
        });

        if (visibleCountSpan) {
            visibleCountSpan.textContent = visibleCount;
        }

        // Kiểm tra hiển thị empty state
        const emptyRow = document.getElementById("emptyStateRow");
        if (emptyRow) {
            emptyRow.style.display = (visibleCount === 0 && rows.length > 0) ? "" : "none";
        }
    }

    if (searchInput) {
        searchInput.addEventListener("input", apDungLocVaTimKiem);
    }

    if (btnClearSearch) {
        btnClearSearch.addEventListener("click", function () {
            if (searchInput) {
                searchInput.value = "";
                searchInput.focus();
            }
            apDungLocVaTimKiem();
        });
    }

    filterChips.forEach(function (chip) {
        chip.addEventListener("click", function () {
            filterChips.forEach(c => c.classList.remove("active"));
            chip.classList.add("active");
            currentFilter = chip.getAttribute("data-filter") || "all";
            apDungLocVaTimKiem();
        });
    });
}

/**
 * ==========================================================================
 * 6. KÉO THẢ SẮP XẾP THỨ TỰ TRÊN BẢNG (AC3: Sắp xếp được thứ tự hiển thị)
 * ==========================================================================
 */
function khoiTaoKeoThaThuTuHang() {
    const tableBody = document.getElementById("categoryTableBody");
    if (!tableBody) return;

    let draggedRow = null;

    const rows = tableBody.querySelectorAll("tr.category-data-row");
    rows.forEach(function (row) {
        const handle = row.querySelector(".order-drag-handle");
        if (!handle) return;

        row.setAttribute("draggable", "true");

        row.addEventListener("dragstart", function (e) {
            draggedRow = row;
            row.style.opacity = "0.5";
            e.dataTransfer.effectAllowed = "move";
        });

        row.addEventListener("dragend", function () {
            if (draggedRow) {
                draggedRow.style.opacity = "";
                draggedRow = null;
            }
            rows.forEach(r => r.style.borderTop = "");
        });

        row.addEventListener("dragover", function (e) {
            e.preventDefault();
            e.dataTransfer.dropEffect = "move";
            if (draggedRow && draggedRow !== row) {
                row.style.borderTop = "2px solid var(--primary)";
            }
        });

        row.addEventListener("dragleave", function () {
            row.style.borderTop = "";
        });

        row.addEventListener("drop", function (e) {
            e.preventDefault();
            row.style.borderTop = "";
            if (draggedRow && draggedRow !== row) {
                // Xác định hướng di chuyển (lên hay xuống)
                const isMoveUp = draggedRow.rowIndex > row.rowIndex;
                const itemId = draggedRow.getAttribute("data-id");

                // Tìm nút di chuyển tương ứng và kích hoạt submit form
                const moveBtn = draggedRow.querySelector(isMoveUp ? ".btn-move-up" : ".btn-move-down");
                if (moveBtn && !moveBtn.disabled) {
                    showToast("success", "Đang cập nhật thứ tự hiển thị...");
                    moveBtn.click();
                }
            }
        });
    });
}

/**
 * ==========================================================================
 * 7. PHÍM TẮT TIỆN ÍCH (ESC ĐÓNG MODAL, / TÌM KIẾM)
 * ==========================================================================
 */
function khoiTaoPhimTat() {
    document.addEventListener("keydown", function (e) {
        // Phím ESC đóng mọi modal đang mở
        if (e.key === "Escape") {
            const modals = document.querySelectorAll(".modal-overlay");
            modals.forEach(function (m) {
                if (m.style.display !== "none") {
                    m.style.display = "none";
                    document.body.style.overflow = "";
                }
            });
        }

        // Phím '/' tập trung vào ô tìm kiếm khi không ở trong input/textarea
        if (e.key === "/" && document.activeElement.tagName !== "INPUT" && document.activeElement.tagName !== "TEXTAREA") {
            e.preventDefault();
            const searchInput = document.getElementById("categorySearchInput");
            if (searchInput) searchInput.focus();
        }
    });
}

/**
 * ==========================================================================
 * 8. TOAST NOTIFICATION HELPER
 * ==========================================================================
 */
function showToast(type, message) {
    const container = document.getElementById("crmToastContainer");
    if (!container) return;

    const toast = document.createElement("div");
    toast.className = "crm-toast " + (type || "success");

    let iconClass = "bi-check-circle-fill";
    if (type === "error") iconClass = "bi-exclamation-octagon-fill";
    if (type === "warning") iconClass = "bi-exclamation-triangle-fill";

    toast.innerHTML =
        '<i class="bi ' + iconClass + ' crm-toast-icon"></i>' +
        '<div class="crm-toast-body">' + escapeHtml(message) + '</div>' +
        '<button type="button" class="crm-toast-close" aria-label="Đóng">&times;</button>';

    container.appendChild(toast);

    const closeBtn = toast.querySelector(".crm-toast-close");
    if (closeBtn) {
        closeBtn.addEventListener("click", function () {
            xoaToast(toast);
        });
    }

    setTimeout(function () {
        xoaToast(toast);
    }, 4200);
}

function xoaToast(toast) {
    toast.style.opacity = "0";
    toast.style.transform = "translateY(12px)";
    setTimeout(function () {
        if (toast.parentNode) toast.parentNode.removeChild(toast);
    }, 250);
}

function escapeHtml(str) {
    if (!str) return "";
    return str
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}
