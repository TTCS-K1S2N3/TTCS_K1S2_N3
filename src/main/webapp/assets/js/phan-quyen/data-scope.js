/**
 * JavaScript cho Story S1-05: Phân quyền theo phạm vi dữ liệu sở hữu
 * Tác giả: Bàn Thị Linh (FE)
 * Tuân thủ quy chuẩn CODING_RULES.md (Vanilla JavaScript, không dùng thư viện ngoài)
 */
document.addEventListener("DOMContentLoaded", function () {
    khoiTaoXuLyPhamViBiKhoa();
    khoiTaoTimKiemNhanh();
});

/**
 * Hiển thị cảnh báo trực quan khi người dùng click vào tab phạm vi dữ liệu bị khóa do chưa đủ quyền.
 */
function khoiTaoXuLyPhamViBiKhoa() {
    const disabledTabs = document.querySelectorAll(".scope-tab.disabled");
    disabledTabs.forEach(function (tab) {
        tab.addEventListener("click", function (e) {
            e.preventDefault();
            const phamViName = tab.querySelector(".scope-tab-title")
                ? tab.querySelector(".scope-tab-title").innerText.trim()
                : "này";
            hienThiThongBaoToast(
                "Bạn không có quyền truy cập phạm vi '" + phamViName + "'. Vai trò của bạn không cho phép xem dữ liệu ngoài phạm vi được chỉ định.",
                "warning"
            );
        });
    });
}

/**
 * Tìm kiếm nhanh realtime trên bảng dữ liệu client-side.
 */
function khoiTaoTimKiemNhanh() {
    const searchInput = document.getElementById("searchBoxInput");
    const tableBody = document.getElementById("dataTableBody");
    if (!searchInput || !tableBody) return;

    searchInput.addEventListener("input", function () {
        const query = searchInput.value.toLowerCase().trim();
        const rows = tableBody.querySelectorAll("tr");
        let visibleCount = 0;

        rows.forEach(function (row) {
            // Bỏ qua dòng thông báo rỗng
            if (row.classList.contains("empty-row")) return;

            const textContent = row.textContent.toLowerCase();
            if (textContent.includes(query)) {
                row.style.display = "";
                visibleCount++;
            } else {
                row.style.display = "none";
            }
        });

        const countDisplay = document.getElementById("visibleCountDisplay");
        if (countDisplay) {
            countDisplay.innerText = visibleCount;
        }
    });
}

/**
 * Hiển thị thông báo Toast nhẹ nhàng ở góc màn hình.
 */
function hienThiThongBaoToast(message, type) {
    let toastContainer = document.getElementById("crmToastContainer");
    if (!toastContainer) {
        toastContainer = document.createElement("div");
        toastContainer.id = "crmToastContainer";
        toastContainer.style.position = "fixed";
        toastContainer.style.bottom = "24px";
        toastContainer.style.right = "24px";
        toastContainer.style.zIndex = "9999";
        toastContainer.style.display = "flex";
        toastContainer.style.flexDirection = "column";
        toastContainer.style.gap = "8px";
        document.body.appendChild(toastContainer);
    }

    const toast = document.createElement("div");
    toast.style.padding = "12px 18px";
    toast.style.borderRadius = "8px";
    toast.style.fontSize = "13px";
    toast.style.fontWeight = "500";
    toast.style.boxShadow = "0 4px 12px rgba(0,0,0,0.15)";
    toast.style.maxWidth = "360px";
    toast.style.transition = "all 0.3s ease";
    toast.style.opacity = "0";
    toast.style.transform = "translateY(10px)";

    if (type === "warning") {
        toast.style.background = "#fffbeb";
        toast.style.color = "#b45309";
        toast.style.border = "1px solid #fde68a";
        toast.innerText = "⚠️ " + message;
    } else if (type === "danger") {
        toast.style.background = "#fef2f2";
        toast.style.color = "#b91c1c";
        toast.style.border = "1px solid #fecaca";
        toast.innerText = "⛔ " + message;
    } else {
        toast.style.background = "#ecfdf5";
        toast.style.color = "#047857";
        toast.style.border = "1px solid #a7f3d0";
        toast.innerText = "✓ " + message;
    }

    toastContainer.appendChild(toast);
    requestAnimationFrame(function () {
        toast.style.opacity = "1";
        toast.style.transform = "translateY(0)";
    });

    setTimeout(function () {
        toast.style.opacity = "0";
        toast.style.transform = "translateY(10px)";
        setTimeout(function () {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 300);
    }, 4000);
}
