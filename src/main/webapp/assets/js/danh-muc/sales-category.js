/**
 * JavaScript cho Story S2-07: Khai báo các danh mục dùng chung của bán hàng
 * Tác giả: Ban Thi Linh (FE)
 * Tuân thủ CODING_RULES.md (Vanilla JS)
 */
document.addEventListener("DOMContentLoaded", function () {
    khoiTaoModalThemMuc();
    khoiTaoModalSuaMuc();
    khoiTaoModalXoaMuc();
    khoiTaoTimKiemRealtime();
});

/**
 * Xử lý mở/đóng Modal Thêm mới
 */
function khoiTaoModalThemMuc() {
    const btnThem = document.getElementById("btnOpenAddModal");
    const modal = document.getElementById("modalAddCategory");
    const btnClose = document.getElementById("btnCloseAddModal");
    const btnCancel = document.getElementById("btnCancelAddModal");

    if (btnThem && modal) {
        btnThem.addEventListener("click", function () {
            modal.classList.add("show");
            const inputMa = document.getElementById("addMaMuc");
            if (inputMa) inputMa.focus();
        });
    }

    const dongModal = function () {
        if (modal) modal.classList.remove("show");
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
 * Xử lý mở/đóng Modal Chỉnh sửa và nạp dữ liệu
 */
function khoiTaoModalSuaMuc() {
    const modal = document.getElementById("modalEditCategory");
    const btnClose = document.getElementById("btnCloseEditModal");
    const btnCancel = document.getElementById("btnCancelEditModal");
    const editBtns = document.querySelectorAll(".btn-edit-item");

    editBtns.forEach(function (btn) {
        btn.addEventListener("click", function () {
            const id = btn.getAttribute("data-id");
            const ma = btn.getAttribute("data-ma");
            const ten = btn.getAttribute("data-ten");
            const mota = btn.getAttribute("data-mota");
            const kichhoat = btn.getAttribute("data-kichhoat") === "true";

            document.getElementById("editId").value = id;
            document.getElementById("editMaMuc").value = ma;
            document.getElementById("editTenMuc").value = ten;
            document.getElementById("editMoTa").value = mota;
            document.getElementById("editKichHoat").checked = kichhoat;

            if (modal) modal.classList.add("show");
        });
    });

    const dongModal = function () {
        if (modal) modal.classList.remove("show");
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
 * Xử lý Modal Xác nhận Xóa (AC2)
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
            const usage = parseInt(btn.getAttribute("data-usage") || "0", 10);

            if (usage > 0) {
                alert("⛔ KHÔNG THỂ XÓA: Mục '" + ten + "' hiện đang có " + usage + " bản ghi nghiệp vụ tham chiếu. Vui lòng chuyển trạng thái sang 'Tạm ngưng'.");
                return;
            }

            document.getElementById("deleteId").value = id;
            document.getElementById("deleteTenMucDisplay").innerText = ten;

            if (modal) modal.classList.add("show");
        });
    });

    const dongModal = function () {
        if (modal) modal.classList.remove("show");
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
 * Tìm kiếm nhanh realtime trên bảng client-side
 */
function khoiTaoTimKiemRealtime() {
    const searchInput = document.getElementById("categorySearchInput");
    const tableBody = document.getElementById("categoryTableBody");
    if (!searchInput || !tableBody) return;

    searchInput.addEventListener("input", function () {
        const query = searchInput.value.toLowerCase().trim();
        const rows = tableBody.querySelectorAll("tr");
        let visibleCount = 0;

        rows.forEach(function (row) {
            if (row.classList.contains("empty-row")) return;
            const textContent = row.textContent.toLowerCase();
            if (textContent.includes(query)) {
                row.style.display = "";
                visibleCount++;
            } else {
                row.style.display = "none";
            }
        });

        const countDisplay = document.getElementById("visibleItemsCount");
        if (countDisplay) {
            countDisplay.innerText = visibleCount;
        }
    });
}
