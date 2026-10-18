/**
 * ==========================================================================
 * JavaScript cho module Danh mục sản phẩm & Bảng giá niêm yết (Story S2-05)
 * Hỗ trợ tương tác thời gian thực, tính toán ngưỡng duyệt chiết khấu,
 * kiểm thử giá sàn (Quote Simulator) và kiểm tra tính hợp lệ dữ liệu.
 * ==========================================================================
 */

document.addEventListener("DOMContentLoaded", function () {
    // 1. Tự động ẩn flash message sau 5 giây với hiệu ứng mượt mà
    const alerts = document.querySelectorAll(".alert");
    alerts.forEach(function (alert) {
        setTimeout(function () {
            alert.style.transition = "opacity 0.5s ease, transform 0.5s ease";
            alert.style.opacity = "0";
            alert.style.transform = "translateY(-8px)";
            setTimeout(function () {
                alert.style.display = "none";
            }, 500);
        }, 5000);
    });

    // 2. Format tiền tệ thời gian thực cho các ô nhập liệu giá (List Price, Floor Price, Cost Price)
    khoiTaoFormatTienTeRealtime();

    // 3. Khởi tạo tính toán thời gian thực cho Form thêm / sửa sản phẩm
    khoiTaoFormValidationVaTinhToan();

    // 4. Khởi tạo Modal Khai báo sản phẩm mới trên trang /san-pham
    khoiTaoModalTaoSanPham();
});

/**
 * Định dạng số tiền sang định dạng tiền Việt Nam (VNĐ).
 */
function dinhDangTien(soTien) {
    if (isNaN(soTien) || soTien === null || soTien === undefined) return "0 ₫";
    return Number(soTien).toLocaleString("vi-VN") + " ₫";
}

/**
 * Lấy context path hiện tại của ứng dụng.
 */
function layContextPath() {
    if (typeof window.APP_CONTEXT !== "undefined" && window.APP_CONTEXT !== null) {
        return window.APP_CONTEXT;
    }
    const pathName = window.location.pathname;
    const secondSlash = pathName.indexOf("/", 1);
    if (secondSlash === -1) return "";
    const ctx = pathName.substring(0, secondSlash);
    // Nếu context path chính là /san-pham thì không có context path phụ
    return ctx === "/san-pham" ? "" : ctx;
}

/**
 * Khởi tạo hỗ trợ hiển thị chuỗi tiền tệ live dưới các ô input số tiền
 */
function khoiTaoFormatTienTeRealtime() {
    const cacInputGia = [
        { id: "giaNiemYet", label: "Giá niêm yết" },
        { id: "giaSan", label: "Giá sàn" },
        { id: "giaVon", label: "Giá vốn" }
    ];

    cacInputGia.forEach(function (item) {
        const input = document.getElementById(item.id);
        if (!input) return;

        // Tạo thẻ tag live format bên dưới input
        let liveTag = document.getElementById("liveTag_" + item.id);
        if (!liveTag) {
            liveTag = document.createElement("div");
            liveTag.id = "liveTag_" + item.id;
            liveTag.className = "currency-live-tag";
            liveTag.style.display = "none";
            input.parentNode.appendChild(liveTag);
        }

        function capNhatLiveTag() {
            const val = parseFloat(input.value);
            if (!isNaN(val) && val > 0) {
                liveTag.textContent = item.label + ": " + dinhDangTien(val);
                liveTag.style.display = "inline-block";
            } else {
                liveTag.style.display = "none";
            }
        }

        input.addEventListener("input", capNhatLiveTag);
        capNhatLiveTag(); // Chạy khởi tạo nếu đã có sẵn giá trị
    });
}

/**
 * Khởi tạo validation ràng buộc: Giá sàn không được lớn hơn Giá niêm yết (AC1, AC2)
 * Đồng thời tự động tính toán Ngưỡng chiết khấu tối đa cho phép và Tỷ suất lợi nhuận (nếu có giá vốn).
 */
function khoiTaoFormValidationVaTinhToan() {
    const formSanPham = document.getElementById("formSanPham");
    if (!formSanPham) return;

    const inputGiaNiemYet = document.getElementById("giaNiemYet");
    const inputGiaSan = document.getElementById("giaSan");
    const inputGiaVon = document.getElementById("giaVon");
    const errGiaSan = document.getElementById("errGiaSan");
    const btnSubmit = formSanPham.querySelector("button[type='submit']");

    // Khung hiển thị phân tích ngưỡng chiết khấu an toàn
    let thresholdInfoBox = document.getElementById("thresholdInfoBox");
    if (!thresholdInfoBox && inputGiaSan) {
        thresholdInfoBox = document.createElement("div");
        thresholdInfoBox.id = "thresholdInfoBox";
        thresholdInfoBox.style.marginTop = "10px";
        thresholdInfoBox.style.fontSize = "12.5px";
        thresholdInfoBox.style.lineHeight = "1.5";
        inputGiaSan.parentNode.appendChild(thresholdInfoBox);
    }

    // Khung phân tích tỷ suất lợi nhuận cho Giám đốc (AC3)
    let marginInfoBox = document.getElementById("marginInfoBox");
    if (!marginInfoBox && inputGiaVon) {
        marginInfoBox = document.createElement("div");
        marginInfoBox.id = "marginInfoBox";
        marginInfoBox.style.marginTop = "8px";
        marginInfoBox.style.fontSize = "12.5px";
        inputGiaVon.parentNode.appendChild(marginInfoBox);
    }

    function capNhatPhanTichVaKiemTra() {
        const giaNiemYet = parseFloat(inputGiaNiemYet ? inputGiaNiemYet.value : 0) || 0;
        const giaSan = parseFloat(inputGiaSan ? inputGiaSan.value : 0) || 0;
        const giaVon = inputGiaVon ? parseFloat(inputGiaVon.value) || 0 : 0;

        let hopLe = true;

        // AC2: Giá sàn không được vượt quá Giá niêm yết
        if (inputGiaSan && inputGiaNiemYet) {
            if (giaSan > giaNiemYet && giaNiemYet > 0) {
                hopLe = false;
                if (errGiaSan) {
                    errGiaSan.textContent = "Giá sàn (" + dinhDangTien(giaSan) + ") không thể lớn hơn Giá niêm yết (" + dinhDangTien(giaNiemYet) + ").";
                    errGiaSan.style.color = "var(--danger)";
                }
                inputGiaSan.style.borderColor = "var(--danger)";
                if (thresholdInfoBox) {
                    thresholdInfoBox.innerHTML = "<span style='color: var(--danger); font-weight: 600; display: inline-flex; align-items: center; gap: 4px;'><span class='material-symbols-outlined icon-xs' aria-hidden='true'>cancel</span> Cấu hình giá không hợp lệ: Giá sàn vi phạm trần giá niêm yết.</span>";
                }
            } else {
                if (errGiaSan) errGiaSan.textContent = "";
                inputGiaSan.style.borderColor = "";

                if (thresholdInfoBox && giaNiemYet > 0) {
                    const discountMax = giaNiemYet - giaSan;
                    const discountPercent = ((discountMax / giaNiemYet) * 100).toFixed(1);

                    thresholdInfoBox.innerHTML =
                        "<div style='background: #fffbeb; border: 1px solid #fde68a; padding: 10px 14px; border-radius: 8px; color: #92400e;'>" +
                        "<div style='display: flex; align-items: center; gap: 4px; margin-bottom: 4px;'><span class='material-symbols-outlined icon-xs' aria-hidden='true'>bolt</span><strong>Ngưỡng duyệt chiết khấu tự động:</strong></div>" +
                        "• Nhân viên Sales được giảm giá tối đa: <strong>" + discountPercent + "%</strong> (" + dinhDangTien(discountMax) + ").<br/>" +
                        "• Nếu đơn giá báo giá < <strong>" + dinhDangTien(giaSan) + "</strong>: Hệ thống bắt buộc phải gửi <strong>Giám đốc duyệt chiết khấu</strong>." +
                        "</div>";
                } else if (thresholdInfoBox) {
                    thresholdInfoBox.innerHTML = "";
                }
            }
        }

        // AC3: Phân tích Gross Margin cho Giám đốc kinh doanh nếu nhập giá vốn
        if (marginInfoBox && giaNiemYet > 0 && giaVon > 0) {
            const grossProfit = giaNiemYet - giaVon;
            const grossMarginPercent = ((grossProfit / giaNiemYet) * 100).toFixed(1);
            const isProfitPositive = grossProfit >= 0;

            marginInfoBox.innerHTML =
                "<div style='background: " + (isProfitPositive ? "#f0fdf4" : "#fef2f2") + "; border: 1px solid " + (isProfitPositive ? "#86efac" : "#fecaca") + "; padding: 8px 12px; border-radius: 8px; color: " + (isProfitPositive ? "#166534" : "#991b1b") + ";'>" +
                "<div style='display: flex; align-items: center; gap: 4px; margin-bottom: 4px;'><span class='material-symbols-outlined icon-xs' aria-hidden='true'>analytics</span><strong>Phân tích lợi nhuận Giám đốc:</strong></div>" +
                "• Lợi nhuận gộp niêm yết: <strong>" + dinhDangTien(grossProfit) + "</strong><br/>" +
                "• Tỷ suất biên lợi nhuận gộp (Gross Margin): <strong>" + grossMarginPercent + "%</strong>" +
                "</div>";
        } else if (marginInfoBox) {
            marginInfoBox.innerHTML = "";
        }

        if (btnSubmit) {
            btnSubmit.disabled = !hopLe;
            if (!hopLe) {
                btnSubmit.classList.add("btn-disabled");
            } else {
                btnSubmit.classList.remove("btn-disabled");
            }
        }

        return hopLe;
    }

    if (inputGiaNiemYet) inputGiaNiemYet.addEventListener("input", capNhatPhanTichVaKiemTra);
    if (inputGiaSan) inputGiaSan.addEventListener("input", capNhatPhanTichVaKiemTra);
    if (inputGiaVon) inputGiaVon.addEventListener("input", capNhatPhanTichVaKiemTra);

    // Kích hoạt tính toán ban đầu
    capNhatPhanTichVaKiemTra();

    formSanPham.addEventListener("submit", function (e) {
        if (!capNhatPhanTichVaKiemTra()) {
            e.preventDefault();
            alert("Vui lòng chỉnh sửa lỗi: Giá sàn không được lớn hơn Giá niêm yết!");
        }
    });
}

/**
 * ==========================================================================
 * AC2: MODAL KIỂM TRA NGƯỠNG GIÁ SÀN CHO BÁO GIÁ (QUOTE DISCOUNT SIMULATOR)
 * Cho phép Giám đốc và Sales nhập thử đơn giá báo giá để xem hệ thống
 * có kích hoạt quy trình duyệt chiết khấu hay không.
 * ==========================================================================
 */
let currentProductData = {};

function kiemTraGiaSanBaoGia(sanPhamId, tenSanPham, giaNiemYet, giaSan) {
    const modal = document.getElementById("modalKiemTraGiaSan");
    if (!modal) return;

    currentProductData = {
        id: sanPhamId,
        ten: tenSanPham,
        giaNiemYet: parseFloat(giaNiemYet) || 0,
        giaSan: parseFloat(giaSan) || 0
    };

    document.getElementById("modalTenSp").textContent = tenSanPham;
    document.getElementById("modalGiaNiemYet").textContent = dinhDangTien(currentProductData.giaNiemYet);
    document.getElementById("modalGiaSan").textContent = dinhDangTien(currentProductData.giaSan);
    document.getElementById("modalSanPhamId").value = sanPhamId;

    // Mặc định đề xuất đơn giá bằng giá niêm yết
    const donGiaInput = document.getElementById("modalDonGiaInput");
    donGiaInput.value = currentProductData.giaNiemYet;

    // Reset kết quả
    tinhKiemTraGiaSanClientSide();

    modal.style.display = "flex";
    donGiaInput.focus();
}

function dongModalGiaSan() {
    const modal = document.getElementById("modalKiemTraGiaSan");
    if (modal) modal.style.display = "none";
}

// Bắt phím Escape để đóng các modal
window.addEventListener("keydown", function (e) {
    if (e.key === "Escape") {
        const modalTao = document.getElementById("modalTaoSanPham");
        if (modalTao && modalTao.style.display !== "none") {
            dongModalTaoSanPham(false);
            return;
        }
        dongModalGiaSan();
        dongModalKhongTheXoa();
    }
});

/**
 * Tính toán nhanh và gọi API kiểm tra ngưỡng giá sàn
 */
function tinhKiemTraGiaSan() {
    const spId = document.getElementById("modalSanPhamId").value;
    const donGiaInput = document.getElementById("modalDonGiaInput");
    const donGia = donGiaInput ? parseFloat(donGiaInput.value) : 0;
    const resultBox = document.getElementById("modalKetQuaBox");

    if (isNaN(donGia) || donGia <= 0) {
        resultBox.innerHTML = "<div class='alert alert-danger'>Vui lòng nhập đơn giá đề xuất hợp lệ lớn hơn 0.</div>";
        return;
    }

    // Hiển thị trạng thái đang kiểm tra
    resultBox.innerHTML = "<div style='text-align: center; padding: 16px; color: var(--slate-500); display: flex; align-items: center; justify-content: center; gap: 6px;'><span class='material-symbols-outlined icon-sm' aria-hidden='true'>hourglass_top</span><span>Đang phân tích ngưỡng duyệt chiết khấu...</span></div>";

    const ctx = layContextPath();
    const url = ctx + "/san-pham/kiem-tra-gia-san?id=" + encodeURIComponent(spId) + "&donGia=" + encodeURIComponent(donGia);

    fetch(url)
        .then(function (res) { return res.json(); })
        .then(function (data) {
            hienThiKetQuaGiaSan(data, donGia);
        })
        .catch(function (err) {
            // Nếu fetch lỗi mạng, fallback tính toán client-side đảm bảo không bao giờ bị đứng
            tinhKiemTraGiaSanClientSide();
        });
}

function tinhKiemTraGiaSanClientSide() {
    const donGiaInput = document.getElementById("modalDonGiaInput");
    if (!donGiaInput) return;
    const valStr = donGiaInput.value ? donGiaInput.value.trim() : "";
    const resultBox = document.getElementById("modalKetQuaBox");

    if (!valStr || parseFloat(valStr) <= 0) {
        if (resultBox) {
            resultBox.innerHTML = "<div style='background: #f8fafc; border: 1.5px dashed #cbd5e1; border-radius: 10px; padding: 14px; text-align: center; color: var(--slate-500); font-size: 13px; margin-top: 14px; display: flex; align-items: center; justify-content: center; gap: 6px;'><span class='material-symbols-outlined icon-xs' style='color: #f59e0b;' aria-hidden='true'>lightbulb</span><span>Hãy nhập đơn giá bán dự kiến trong báo giá để hệ thống đối soát tự động với Giá sàn.</span></div>";
        }
        return;
    }

    const donGia = parseFloat(valStr);
    const giaNiemYet = currentProductData.giaNiemYet;
    const giaSan = currentProductData.giaSan;

    const canDuyet = donGia < giaSan;
    const data = {
        thanhCong: true,
        sanPhamId: currentProductData.id,
        giaNiemYet: giaNiemYet,
        giaSan: giaSan,
        donGia: donGia,
        canDuyetChiMon: canDuyet,
        canhBao: canDuyet
            ? "Đơn giá thấp hơn Giá sàn niêm yết (" + dinhDangTien(giaSan) + "). Báo giá này sẽ cần Giám đốc kinh doanh phê duyệt!"
            : "Đơn giá hợp lệ theo khung giá niêm yết."
    };

    hienThiKetQuaGiaSan(data, donGia);
}

function hienThiKetQuaGiaSan(data, donGia) {
    const resultBox = document.getElementById("modalKetQuaBox");
    if (!resultBox) return;

    if (!data.thanhCong) {
        resultBox.innerHTML = "<div class='alert alert-danger' style='display: flex; align-items: center; gap: 6px;'><span class='material-symbols-outlined icon-xs' aria-hidden='true'>error</span><span>" + (data.thongDiep || "Có lỗi khi kiểm tra.") + "</span></div>";
        return;
    }

    const giaNiemYet = parseFloat(data.giaNiemYet) || currentProductData.giaNiemYet;
    const giaSan = parseFloat(data.giaSan) || currentProductData.giaSan;
    const canDuyet = data.canDuyetChiMon;

    const discountAmount = Math.max(0, giaNiemYet - donGia);
    const discountPercent = giaNiemYet > 0 ? ((discountAmount / giaNiemYet) * 100).toFixed(1) : 0;
    const chenhLechGiaSan = Math.abs(donGia - giaSan);

    let html = "";
    if (canDuyet) {
        html =
            "<div style='background: #fef2f2; border: 1.5px solid #f87171; border-radius: 12px; padding: 18px; margin-top: 14px;'>" +
                "<div style='display: flex; align-items: center; gap: 8px; color: #b91c1c; font-weight: 800; font-size: 15px; margin-bottom: 6px;'>" +
                    "<span class='material-symbols-outlined icon-sm' aria-hidden='true'>warning</span><span>BẮT BUỘC PHẢI DUYỆT CHIẾT KHẤU</span>" +
                "</div>" +
                "<p style='color: #7f1d1d; font-size: 13.5px; margin-bottom: 12px;'>" +
                    "Đơn giá đề xuất <strong>" + dinhDangTien(donGia) + "</strong> thấp hơn Giá sàn (" + dinhDangTien(giaSan) + ") là <strong>" + dinhDangTien(chenhLechGiaSan) + "</strong>. " +
                    "Mức chiết khấu đạt <strong>" + discountPercent + "%</strong>, vượt quá quyền hạn của Sales Rep." +
                "</p>" +
                "<div style='background: #ffffff; border-radius: 8px; padding: 10px 14px; font-size: 12.5px; color: #991b1b; display: flex; align-items: center; gap: 6px;'>" +
                    "<span class='material-symbols-outlined icon-xs' aria-hidden='true'>admin_panel_settings</span><span><strong>Quy trình xử lý:</strong> Báo giá khi gửi sẽ tự động chuyển sang trạng thái <em>CHỜ DUYỆT CHIẾT KHẤU</em> gửi tới Giám đốc kinh doanh.</span>" +
                "</div>" +
            "</div>";
    } else {
        html =
            "<div style='background: #ecfdf5; border: 1.5px solid #34d399; border-radius: 12px; padding: 18px; margin-top: 14px;'>" +
                "<div style='display: flex; align-items: center; gap: 8px; color: #047857; font-weight: 800; font-size: 15px; margin-bottom: 6px;'>" +
                    "<span class='material-symbols-outlined icon-sm' aria-hidden='true'>check_circle</span><span>ĐƠN GIÁ HỢP LỆ - KHÔNG CẦN DUYỆT</span>" +
                "</div>" +
                "<p style='color: #065f46; font-size: 13.5px; margin-bottom: 12px;'>" +
                    "Đơn giá đề xuất <strong>" + dinhDangTien(donGia) + "</strong> nằm trong khung giá niêm yết (cao hơn hoặc bằng Giá sàn " + dinhDangTien(giaSan) + "). " +
                    "Chiết khấu: <strong>" + discountPercent + "%</strong> (" + dinhDangTien(discountAmount) + ")." +
                "</p>" +
                "<div style='background: #ffffff; border-radius: 8px; padding: 10px 14px; font-size: 12.5px; color: #065f46; display: flex; align-items: center; gap: 6px;'>" +
                    "<span class='material-symbols-outlined icon-xs' aria-hidden='true'>rocket_launch</span><span><strong>Quy hạn:</strong> Nhân viên kinh doanh được phép xuất và gửi báo giá chính thức cho khách hàng ngay lập tức.</span>" +
                "</div>" +
            "</div>";
    }

    resultBox.innerHTML = html;
}

/**
 * ==========================================================================
 * AC4: GIẢI THÍCH SẢN PHẨM KHÔNG THỂ XOÁ VÌ ĐÃ CÓ TRONG BÁO GIÁ
 * ==========================================================================
 */
function thongBaoKhongTheXoa(tenSanPham) {
    const modal = document.getElementById("modalKhongTheXoa");
    if (!modal) {
        alert("Sản phẩm '" + tenSanPham + "' đã xuất hiện trong các báo giá kinh doanh của hệ thống. Theo quy định dữ liệu, sản phẩm này KHÔNG THỂ XOÁ mà chỉ có thể chuyển sang trạng thái 'Ngừng kinh doanh'!");
        return;
    }
    document.getElementById("modalTenSpXoa").textContent = tenSanPham;
    modal.style.display = "flex";
}

function dongModalKhongTheXoa() {
    const modal = document.getElementById("modalKhongTheXoa");
    if (modal) modal.style.display = "none";
}

/**
 * Trình kích hoạt modal từ các nút data-attribute (đảm bảo an toàn không lỗi quote ký tự)
 */
function moModalThuGiaSan(btn) {
    if (!btn) return;
    const id = btn.getAttribute("data-id");
    const ten = btn.getAttribute("data-ten");
    const giaNiemYet = btn.getAttribute("data-gia-niem-yet");
    const giaSan = btn.getAttribute("data-gia-san");
    kiemTraGiaSanBaoGia(id, ten, giaNiemYet, giaSan);
}

function moModalKhongTheXoa(btn) {
    if (!btn) return;
    const ten = btn.getAttribute("data-ten");
    thongBaoKhongTheXoa(ten);
}

/**
 * ==========================================================================
 * MODAL KHAI BÁO SẢN PHẨM MỚI (Đồng bộ chuẩn /danh-muc-ban-hang)
 * ==========================================================================
 */
function khoiTaoModalTaoSanPham() {
    const btnThem = document.getElementById("btnThemSanPhamMoi");
    const modal = document.getElementById("modalTaoSanPham");
    if (!modal) return;

    const btnClose = document.getElementById("btnCloseModalTao");
    const btnCancel = document.getElementById("btnCancelModalTao");
    const form = document.getElementById("formTaoSanPhamModal");
    const inputMa = document.getElementById("taoSp_maSanPham");
    const inputTen = document.getElementById("taoSp_tenSanPham");
    const inputDonViTinh = document.getElementById("taoSp_donViTinh");
    const inputGiaNiemYet = document.getElementById("taoSp_giaNiemYet");
    const inputGiaSan = document.getElementById("taoSp_giaSan");
    const inputGiaVon = document.getElementById("taoSp_giaVon");

    // 1. Mở modal khi nhấn "Khai báo sản phẩm mới"
    if (btnThem) {
        btnThem.addEventListener("click", function (e) {
            e.preventDefault();
            moModalTaoSanPham();
        });
    }

    // 2. Đóng modal qua nút X và nút Hủy
    if (btnClose) {
        btnClose.addEventListener("click", function () {
            dongModalTaoSanPham(false);
        });
    }

    if (btnCancel) {
        btnCancel.addEventListener("click", function () {
            dongModalTaoSanPham(false);
        });
    }

    // 3. Đóng khi click ngoài vùng backdrop
    modal.addEventListener("click", function (e) {
        if (e.target === modal) {
            dongModalTaoSanPham(false);
        }
    });

    // 4. Chuẩn hóa mã sản phẩm: in hoa, tự động chuyển khoảng trắng thành gạch nối
    if (inputMa) {
        inputMa.addEventListener("input", function () {
            this.value = this.value.toUpperCase().replace(/\s+/g, "-");
        });
    }

    // 5. Khởi tạo tính toán tiền tệ và ngưỡng chiết khấu trong modal
    khoiTaoTinhToanTrongModal(inputGiaNiemYet, inputGiaSan, inputGiaVon);

    // 6. Xử lý validation client-side khi submit
    if (form) {
        form.addEventListener("submit", function (e) {
            let hopLe = true;

            // Reset trạng thái lỗi
            modal.querySelectorAll(".field-error").forEach(function (el) {
                el.style.display = "none";
                el.textContent = "";
            });
            modal.querySelectorAll("input, select").forEach(function (el) {
                el.style.borderColor = "";
            });

            // Kiểm tra Mã sản phẩm
            if (inputMa && !inputMa.value.trim()) {
                datLoiInputModal(inputMa, "errModal_maSanPham", "Vui lòng nhập mã sản phẩm / dịch vụ.");
                hopLe = false;
            } else if (inputMa && !/^[A-Za-z0-9_-]{2,50}$/.test(inputMa.value.trim())) {
                datLoiInputModal(inputMa, "errModal_maSanPham", "Mã sản phẩm phải từ 2 - 50 ký tự (chữ, số, dấu gạch nối hoặc gạch dưới).");
                hopLe = false;
            }

            // Kiểm tra Tên sản phẩm
            if (inputTen && !inputTen.value.trim()) {
                datLoiInputModal(inputTen, "errModal_tenSanPham", "Vui lòng nhập tên sản phẩm / dịch vụ.");
                hopLe = false;
            }

            // Kiểm tra Đơn vị tính
            if (inputDonViTinh && !inputDonViTinh.value.trim()) {
                datLoiInputModal(inputDonViTinh, "errModal_donViTinh", "Vui lòng nhập đơn vị tính.");
                hopLe = false;
            }

            // Kiểm tra Giá niêm yết
            const giaNiemYetVal = inputGiaNiemYet ? parseFloat(inputGiaNiemYet.value) : NaN;
            if (inputGiaNiemYet && (isNaN(giaNiemYetVal) || giaNiemYetVal < 0)) {
                datLoiInputModal(inputGiaNiemYet, "errModal_giaNiemYet", "Vui lòng nhập giá niêm yết hợp lệ (≥ 0).");
                hopLe = false;
            }

            // Kiểm tra Giá sàn
            const giaSanVal = inputGiaSan ? parseFloat(inputGiaSan.value) : NaN;
            if (inputGiaSan && (isNaN(giaSanVal) || giaSanVal < 0)) {
                datLoiInputModal(inputGiaSan, "errModal_giaSan", "Vui lòng nhập giá sàn hợp lệ (≥ 0).");
                hopLe = false;
            } else if (!isNaN(giaSanVal) && !isNaN(giaNiemYetVal) && giaSanVal > giaNiemYetVal) {
                datLoiInputModal(inputGiaSan, "errModal_giaSan", "Giá sàn (" + dinhDangTien(giaSanVal) + ") không thể lớn hơn Giá niêm yết (" + dinhDangTien(giaNiemYetVal) + ").");
                hopLe = false;
            }

            // Kiểm tra Giá vốn nếu có nhập
            if (inputGiaVon && inputGiaVon.value.trim() !== "") {
                const giaVonVal = parseFloat(inputGiaVon.value);
                if (isNaN(giaVonVal) || giaVonVal < 0) {
                    datLoiInputModal(inputGiaVon, "errModal_giaVon", "Giá vốn không hợp lệ (≥ 0).");
                    hopLe = false;
                }
            }

            if (!hopLe) {
                e.preventDefault();
                const firstErr = modal.querySelector(".field-error[style*='block']");
                if (firstErr) {
                    const prev = firstErr.previousElementSibling;
                    if (prev && typeof prev.focus === "function") {
                        prev.focus();
                    }
                }
            }
        });
    }
}

/**
 * Mở modal tạo sản phẩm mới
 */
function moModalTaoSanPham() {
    const modal = document.getElementById("modalTaoSanPham");
    if (!modal) return;
    modal.style.display = "flex";
    document.body.style.overflow = "hidden";
    const inputMa = document.getElementById("taoSp_maSanPham");
    if (inputMa) {
        inputMa.focus();
    }
}

/**
 * Kiểm tra xem người dùng đã nhập bất kỳ dữ liệu nào vào modal hay chưa
 */
function modalTaoDaCoDuLieu() {
    const modal = document.getElementById("modalTaoSanPham");
    if (!modal) return false;
    const ma = document.getElementById("taoSp_maSanPham");
    const ten = document.getElementById("taoSp_tenSanPham");
    const dvt = document.getElementById("taoSp_donViTinh");
    const giaNiemYet = document.getElementById("taoSp_giaNiemYet");
    const giaSan = document.getElementById("taoSp_giaSan");
    const giaVon = document.getElementById("taoSp_giaVon");
    const moTa = document.getElementById("taoSp_moTa");

    return (ma && ma.value.trim() !== "") ||
           (ten && ten.value.trim() !== "") ||
           (dvt && dvt.value.trim() !== "") ||
           (giaNiemYet && giaNiemYet.value.trim() !== "") ||
           (giaSan && giaSan.value.trim() !== "") ||
           (giaVon && giaVon.value.trim() !== "") ||
           (moTa && moTa.value.trim() !== "");
}

/**
 * Đóng modal tạo sản phẩm; có cảnh báo nếu người dùng đã nhập liệu dở dang
 */
function dongModalTaoSanPham(force) {
    const modal = document.getElementById("modalTaoSanPham");
    if (!modal || modal.style.display === "none") return;

    if (!force && modalTaoDaCoDuLieu()) {
        if (!confirm("Bạn có dữ liệu chưa lưu trong biểu mẫu tạo sản phẩm. Bạn có chắc chắn muốn đóng và hủy bỏ các thông tin đã nhập?")) {
            return;
        }
    }

    modal.style.display = "none";
    document.body.style.overflow = "";

    // Reset dữ liệu form
    const form = document.getElementById("formTaoSanPhamModal");
    if (form) form.reset();

    // Reset hiển thị lỗi và live tags
    modal.querySelectorAll(".field-error").forEach(function (el) {
        el.style.display = "none";
        el.textContent = "";
    });
    modal.querySelectorAll("input, select").forEach(function (el) {
        el.style.borderColor = "";
    });
    modal.querySelectorAll(".currency-live-tag").forEach(function (el) {
        el.style.display = "none";
    });
    const thresholdBox = document.getElementById("taoSp_thresholdInfoBox");
    if (thresholdBox) thresholdBox.innerHTML = "";
    const marginBox = document.getElementById("taoSp_marginInfoBox");
    if (marginBox) marginBox.innerHTML = "";
}

/**
 * Đặt thông báo lỗi cho ô input trong modal
 */
function datLoiInputModal(inputEl, errElId, thongDiep) {
    if (inputEl) inputEl.style.borderColor = "var(--sp-danger)";
    const errEl = document.getElementById(errElId);
    if (errEl) {
        errEl.textContent = thongDiep;
        errEl.style.display = "block";
    }
}

/**
 * Khởi tạo tính toán realtime cho các trường tiền tệ trong modal tạo sản phẩm
 */
function khoiTaoTinhToanTrongModal(inputGiaNiemYet, inputGiaSan, inputGiaVon) {
    const liveTagNiemYet = document.getElementById("liveTag_taoSp_giaNiemYet");
    const liveTagSan = document.getElementById("liveTag_taoSp_giaSan");
    const liveTagVon = document.getElementById("liveTag_taoSp_giaVon");
    const thresholdBox = document.getElementById("taoSp_thresholdInfoBox");
    const marginBox = document.getElementById("taoSp_marginInfoBox");
    const errGiaSan = document.getElementById("errModal_giaSan");

    function capNhatModalGia() {
        const giaNiemYet = parseFloat(inputGiaNiemYet ? inputGiaNiemYet.value : 0) || 0;
        const giaSan = parseFloat(inputGiaSan ? inputGiaSan.value : 0) || 0;
        const giaVon = inputGiaVon ? parseFloat(inputGiaVon.value) || 0 : 0;

        // 1. Live tags tiền tệ
        if (liveTagNiemYet) {
            if (giaNiemYet > 0) {
                liveTagNiemYet.textContent = "Giá niêm yết: " + dinhDangTien(giaNiemYet);
                liveTagNiemYet.style.display = "inline-block";
            } else {
                liveTagNiemYet.style.display = "none";
            }
        }

        if (liveTagSan) {
            if (giaSan > 0) {
                liveTagSan.textContent = "Giá sàn: " + dinhDangTien(giaSan);
                liveTagSan.style.display = "inline-block";
            } else {
                liveTagSan.style.display = "none";
            }
        }

        if (liveTagVon) {
            if (giaVon > 0) {
                liveTagVon.textContent = "Giá vốn: " + dinhDangTien(giaVon);
                liveTagVon.style.display = "inline-block";
            } else {
                liveTagVon.style.display = "none";
            }
        }

        // 2. AC2: Kiểm tra Giá sàn so với Giá niêm yết
        if (inputGiaSan && inputGiaNiemYet) {
            if (giaSan > giaNiemYet && giaNiemYet > 0) {
                if (errGiaSan) {
                    errGiaSan.textContent = "Giá sàn (" + dinhDangTien(giaSan) + ") không thể lớn hơn Giá niêm yết (" + dinhDangTien(giaNiemYet) + ").";
                    errGiaSan.style.display = "block";
                }
                inputGiaSan.style.borderColor = "var(--sp-danger)";
                if (thresholdBox) {
                    thresholdBox.innerHTML = "<span style='color: var(--sp-danger); font-weight: 600; display: inline-flex; align-items: center; gap: 4px;'><span class='material-symbols-outlined icon-xs' aria-hidden='true'>cancel</span> Cấu hình giá không hợp lệ: Giá sàn vi phạm trần giá niêm yết.</span>";
                }
            } else {
                if (errGiaSan && errGiaSan.textContent.includes("không thể lớn hơn")) {
                    errGiaSan.textContent = "";
                    errGiaSan.style.display = "none";
                    inputGiaSan.style.borderColor = "";
                }

                if (thresholdBox && giaNiemYet > 0) {
                    const discountMax = giaNiemYet - giaSan;
                    const discountPercent = ((discountMax / giaNiemYet) * 100).toFixed(1);

                    thresholdBox.innerHTML =
                        "<div style='background: #fffbeb; border: 1px solid #fde68a; padding: 10px 14px; border-radius: 8px; color: #92400e; font-size: 12.5px; line-height: 1.5;'>" +
                        "<div style='display: flex; align-items: center; gap: 4px; margin-bottom: 4px;'><span class='material-symbols-outlined icon-xs' aria-hidden='true'>bolt</span><strong>Ngưỡng duyệt chiết khấu tự động:</strong></div>" +
                        "• Nhân viên Sales được giảm tối đa: <strong>" + discountPercent + "%</strong> (" + dinhDangTien(discountMax) + ").<br/>" +
                        "• Nếu đơn giá báo giá < <strong>" + dinhDangTien(giaSan) + "</strong>: Hệ thống bắt buộc phải gửi <strong>Giám đốc duyệt chiết khấu</strong>." +
                        "</div>";
                } else if (thresholdBox) {
                    thresholdBox.innerHTML = "";
                }
            }
        }

        // 3. AC3: Phân tích Gross Margin cho Giám đốc kinh doanh nếu có nhập giá vốn
        if (marginBox && giaNiemYet > 0 && giaVon > 0) {
            const grossProfit = giaNiemYet - giaVon;
            const grossMarginPercent = ((grossProfit / giaNiemYet) * 100).toFixed(1);
            const isProfitPositive = grossProfit >= 0;

            marginBox.innerHTML =
                "<div style='background: " + (isProfitPositive ? "#f0fdf4" : "#fef2f2") + "; border: 1px solid " + (isProfitPositive ? "#86efac" : "#fecaca") + "; padding: 8px 12px; border-radius: 8px; color: " + (isProfitPositive ? "#166534" : "#991b1b") + "; font-size: 12.5px; line-height: 1.5;'>" +
                "<div style='display: flex; align-items: center; gap: 4px; margin-bottom: 4px;'><span class='material-symbols-outlined icon-xs' aria-hidden='true'>analytics</span><strong>Phân tích lợi nhuận Giám đốc:</strong></div>" +
                "• Lợi nhuận gộp niêm yết: <strong>" + dinhDangTien(grossProfit) + "</strong><br/>" +
                "• Tỷ suất biên lợi nhuận gộp (Gross Margin): <strong>" + grossMarginPercent + "%</strong>" +
                "</div>";
        } else if (marginBox) {
            marginBox.innerHTML = "";
        }
    }

    if (inputGiaNiemYet) inputGiaNiemYet.addEventListener("input", capNhatModalGia);
    if (inputGiaSan) inputGiaSan.addEventListener("input", capNhatModalGia);
    if (inputGiaVon) inputGiaVon.addEventListener("input", capNhatModalGia);
}
