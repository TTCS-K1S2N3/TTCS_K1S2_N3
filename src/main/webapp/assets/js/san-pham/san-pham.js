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
                    errGiaSan.textContent = "⚠️ Giá sàn (" + dinhDangTien(giaSan) + ") không thể lớn hơn Giá niêm yết (" + dinhDangTien(giaNiemYet) + ").";
                    errGiaSan.style.color = "var(--danger)";
                }
                inputGiaSan.style.borderColor = "var(--danger)";
                if (thresholdInfoBox) {
                    thresholdInfoBox.innerHTML = "<span style='color: var(--danger); font-weight: 600;'>❌ Cấu hình giá không hợp lệ: Giá sàn vi phạm trần giá niêm yết.</span>";
                }
            } else {
                if (errGiaSan) errGiaSan.textContent = "";
                inputGiaSan.style.borderColor = "";

                if (thresholdInfoBox && giaNiemYet > 0) {
                    const discountMax = giaNiemYet - giaSan;
                    const discountPercent = ((discountMax / giaNiemYet) * 100).toFixed(1);

                    thresholdInfoBox.innerHTML = 
                        "<div style='background: #fffbeb; border: 1px solid #fde68a; padding: 10px 14px; border-radius: 8px; color: #92400e;'>" +
                        "⚡ <strong>Ngưỡng duyệt chiết khấu tự động:</strong><br/>" +
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
                "📊 <strong>Phân tích lợi nhuận Giám đốc:</strong><br/>" +
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

// Bắt phím Escape để đóng modal
window.addEventListener("keydown", function (e) {
    if (e.key === "Escape") {
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
    resultBox.innerHTML = "<div style='text-align: center; padding: 16px; color: var(--slate-500);'>⏳ Đang phân tích ngưỡng duyệt chiết khấu...</div>";

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
            resultBox.innerHTML = "<div style='background: #f8fafc; border: 1.5px dashed #cbd5e1; border-radius: 10px; padding: 14px; text-align: center; color: var(--slate-500); font-size: 13px; margin-top: 14px;'>💡 Hãy nhập đơn giá bán dự kiến trong báo giá để hệ thống đối soát tự động với Giá sàn.</div>";
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
        resultBox.innerHTML = "<div class='alert alert-danger'>⚠️ " + (data.thongDiep || "Có lỗi khi kiểm tra.") + "</div>";
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
                    "⚠️ BẮT BUỘC PHẢI DUYỆT CHIẾT KHẤU" +
                "</div>" +
                "<p style='color: #7f1d1d; font-size: 13.5px; margin-bottom: 12px;'>" +
                    "Đơn giá đề xuất <strong>" + dinhDangTien(donGia) + "</strong> thấp hơn Giá sàn (" + dinhDangTien(giaSan) + ") là <strong>" + dinhDangTien(chenhLechGiaSan) + "</strong>. " +
                    "Mức chiết khấu đạt <strong>" + discountPercent + "%</strong>, vượt quá quyền hạn của Sales Rep." +
                "</p>" +
                "<div style='background: #ffffff; border-radius: 8px; padding: 10px 14px; font-size: 12.5px; color: #991b1b;'>" +
                    "👔 <strong>Quy trình xử lý:</strong> Báo giá khi gửi sẽ tự động chuyển sang trạng thái <em>CHỜ DUYỆT CHIẾT KHẤU</em> gửi tới Giám đốc kinh doanh." +
                "</div>" +
            "</div>";
    } else {
        html = 
            "<div style='background: #ecfdf5; border: 1.5px solid #34d399; border-radius: 12px; padding: 18px; margin-top: 14px;'>" +
                "<div style='display: flex; align-items: center; gap: 8px; color: #047857; font-weight: 800; font-size: 15px; margin-bottom: 6px;'>" +
                    "✅ ĐƠN GIÁ HỢP LỆ - KHÔNG CẦN DUYỆT" +
                "</div>" +
                "<p style='color: #065f46; font-size: 13.5px; margin-bottom: 12px;'>" +
                    "Đơn giá đề xuất <strong>" + dinhDangTien(donGia) + "</strong> nằm trong khung giá niêm yết (cao hơn hoặc bằng Giá sàn " + dinhDangTien(giaSan) + "). " +
                    "Chiết khấu: <strong>" + discountPercent + "%</strong> (" + dinhDangTien(discountAmount) + ")." +
                "</p>" +
                "<div style='background: #ffffff; border-radius: 8px; padding: 10px 14px; font-size: 12.5px; color: #065f46;'>" +
                    "🚀 <strong>Quyền hạn:</strong> Nhân viên kinh doanh được phép xuất và gửi báo giá chính thức cho khách hàng ngay lập tức." +
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
