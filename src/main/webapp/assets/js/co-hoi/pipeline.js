/**
 * JavaScript cho module Cấu hình Giai đoạn Pipeline & Dự báo Doanh số (Story S2-09 - FE Hoàng văn kim).
 * Cung cấp:
 * 1. Đồng bộ thanh trượt Slider & Input số cho Xác suất thắng (%) (AC 2)
 * 2. Bộ mô phỏng tính nhanh dự báo doanh số trực tiếp thời gian thực (Live Forecast Simulator - AC 2)
 * 3. Client-side Form Validation chặt chẽ, hiển thị lỗi inline và chống double-submit
 * 4. Modal kiểm tra điều kiện rời giai đoạn qua AJAX API với checklist trực quan (AC 3)
 * 5. Modal cảnh báo & xác nhận xoá an toàn, bảo vệ cơ hội đang chạy (AC 4)
 * 6. Bộ lọc / Tìm kiếm nhanh giai đoạn trong bảng cấu hình
 */

document.addEventListener("DOMContentLoaded", function () {
    khoiTaoThongBaoTuDong();
    khoiTaoDongBoXacSuat();
    khoiTaoFormValidation();
    khoiTaoBoLocGiaiDoan();
    khoiTaoSimDuBao();
});

/**
 * 1. Tự động ẩn thông báo flash sau 5 giây với hiệu ứng mờ dần
 */
function khoiTaoThongBaoTuDong() {
    const alerts = document.querySelectorAll(".alert");
    alerts.forEach(function (alert) {
        setTimeout(function () {
            alert.style.transition = "opacity 0.4s ease, transform 0.4s ease";
            alert.style.opacity = "0";
            alert.style.transform = "translateY(-6px)";
            setTimeout(function () {
                alert.style.display = "none";
            }, 400);
        }, 5000);
    });
}

function dongThongBao(btn) {
    const alertBox = btn.closest(".alert");
    if (alertBox) {
        alertBox.style.transition = "opacity 0.25s ease";
        alertBox.style.opacity = "0";
        setTimeout(function () {
            alertBox.style.display = "none";
        }, 250);
    }
}

/**
 * 2. Đồng bộ thanh trượt Slider và ô nhập số cho Xác suất thắng (%) (AC 2)
 */
function khoiTaoDongBoXacSuat() {
    const inputXacSuat = document.getElementById("xacSuatThang");
    const sliderXacSuat = document.getElementById("sliderXacSuat");
    const previewBox = document.getElementById("probLivePreview");

    if (!inputXacSuat || !sliderXacSuat) return;

    function capNhatPreview(val) {
        if (previewBox) {
            const num = parseInt(val, 10) || 0;
            const duBaoMau = (100000000 * (num / 100));
            previewBox.innerHTML = "Doanh số dự báo cho cơ hội 100.000.000 ₫: <strong style='color: var(--primary-600);'>" +
                dinhDangTienTe(duBaoMau) + "</strong> (" + num + "%)";
        }
    }

    sliderXacSuat.addEventListener("input", function () {
        inputXacSuat.value = this.value;
        capNhatPreview(this.value);
    });

    inputXacSuat.addEventListener("input", function () {
        let val = parseInt(this.value, 10);
        if (isNaN(val)) val = 0;
        if (val < 0) val = 0;
        if (val > 100) val = 100;
        this.value = val;
        sliderXacSuat.value = val;
        capNhatPreview(val);
    });

    // Khởi tạo ban đầu
    capNhatPreview(inputXacSuat.value || 0);
}

/**
 * 3. Client-side Form Validation chặt chẽ cho Tạo và Sửa giai đoạn
 */
function khoiTaoFormValidation() {
    const form = document.getElementById("formGiaiDoan");
    if (!form) return;

    const maGiaiDoan = document.getElementById("maGiaiDoan");
    const tenGiaiDoan = document.getElementById("tenGiaiDoan");
    const thuTu = document.getElementById("thuTu");
    const xacSuatThang = document.getElementById("xacSuatThang");
    const btnSubmit = document.getElementById("btnSubmitForm");

    // Real-time clean maGiaiDoan
    if (maGiaiDoan) {
        maGiaiDoan.addEventListener("input", function () {
            // Tự động chuyển hoa và thay ký tự đặc biệt thành gạch dưới
            this.value = this.value.toUpperCase().replace(/[^A-Z0-9_-]/g, "");
            xoaLoiField(this);
        });
    }

    if (tenGiaiDoan) {
        tenGiaiDoan.addEventListener("input", function () {
            xoaLoiField(this);
        });
    }

    form.addEventListener("submit", function (e) {
        let hopLe = true;
        let firstInvalid = null;

        // Reset all error states
        document.querySelectorAll(".field-error").forEach(el => el.remove());
        document.querySelectorAll(".is-invalid").forEach(el => el.classList.remove("is-invalid"));

        // 1. Kiểm tra Mã giai đoạn
        if (maGiaiDoan) {
            const val = maGiaiDoan.value.trim();
            const regex = /^[A-Z0-9_-]{2,50}$/;
            if (!val) {
                hienLoiField(maGiaiDoan, "Vui lòng nhập mã giai đoạn.");
                hopLe = false;
                if (!firstInvalid) firstInvalid = maGiaiDoan;
            } else if (!regex.test(val)) {
                hienLoiField(maGiaiDoan, "Mã giai đoạn từ 2-50 ký tự, chỉ gồm chữ cái in hoa, số, gạch ngang (-) hoặc gạch dưới (_).");
                hopLe = false;
                if (!firstInvalid) firstInvalid = maGiaiDoan;
            }
        }

        // 2. Kiểm tra Tên giai đoạn
        if (tenGiaiDoan) {
            const val = tenGiaiDoan.value.trim();
            if (!val) {
                hienLoiField(tenGiaiDoan, "Vui lòng nhập tên giai đoạn.");
                hopLe = false;
                if (!firstInvalid) firstInvalid = tenGiaiDoan;
            } else if (val.length < 2 || val.length > 100) {
                hienLoiField(tenGiaiDoan, "Tên giai đoạn từ 2 đến 100 ký tự.");
                hopLe = false;
                if (!firstInvalid) firstInvalid = tenGiaiDoan;
            }
        }

        // 3. Kiểm tra Thứ tự
        if (thuTu) {
            const val = parseInt(thuTu.value, 10);
            if (isNaN(val) || val <= 0) {
                hienLoiField(thuTu, "Thứ tự giai đoạn phải là số nguyên dương lớn hơn 0.");
                hopLe = false;
                if (!firstInvalid) firstInvalid = thuTu;
            }
        }

        // 4. Kiểm tra Xác suất thắng (%)
        if (xacSuatThang) {
            const val = parseInt(xacSuatThang.value, 10);
            if (isNaN(val) || val < 0 || val > 100) {
                hienLoiField(xacSuatThang, "Xác suất thắng phải từ 0% đến 100%.");
                hopLe = false;
                if (!firstInvalid) firstInvalid = xacSuatThang;
            }
        }

        if (!hopLe) {
            e.preventDefault();
            if (firstInvalid) {
                firstInvalid.focus();
            }
            return false;
        }

        // Hiển thị trạng thái loading khi submit để chống double-click
        if (btnSubmit) {
            btnSubmit.disabled = true;
            btnSubmit.innerHTML = "⏳ Đang lưu cấu hình...";
            btnSubmit.classList.add("btn-disabled");
        }
        return true;
    });
}

function hienLoiField(inputEl, msg) {
    inputEl.classList.add("is-invalid");
    const errSpan = document.createElement("span");
    errSpan.className = "field-error";
    errSpan.innerHTML = "⚠️ " + msg;
    inputEl.parentElement.appendChild(errSpan);
}

function xoaLoiField(inputEl) {
    inputEl.classList.remove("is-invalid");
    const existing = inputEl.parentElement.querySelector(".field-error");
    if (existing) existing.remove();
}

/**
 * 4. Bộ mô phỏng tính nhanh dự báo doanh số thời gian thực (Live Forecast Simulator - AC 2)
 */
function khoiTaoSimDuBao() {
    const inputGiaTri = document.getElementById("simGiaTriCoHoi");
    const selectGiaiDoan = document.getElementById("simGiaiDoan");
    const sliderProb = document.getElementById("simSliderProb");
    const valProb = document.getElementById("simValProb");

    if (!inputGiaTri || !selectGiaiDoan) return;

    // Khi chọn giai đoạn từ dropdown, cập nhật xác suất tương ứng
    selectGiaiDoan.addEventListener("change", function () {
        const prob = parseInt(this.value, 10) || 0;
        if (sliderProb) sliderProb.value = prob;
        if (valProb) valProb.textContent = prob + "%";
        tinhNhanhDuBao();
    });

    if (sliderProb && valProb) {
        sliderProb.addEventListener("input", function () {
            valProb.textContent = this.value + "%";
            tinhNhanhDuBao();
        });
    }

    inputGiaTri.addEventListener("input", function () {
        tinhNhanhDuBao();
    });

    // Tính lần đầu
    tinhNhanhDuBao();
}

function datPresetGiaTri(soTien) {
    const inputGiaTri = document.getElementById("simGiaTriCoHoi");
    if (inputGiaTri) {
        inputGiaTri.value = soTien;
        tinhNhanhDuBao();
    }
}

function tinhNhanhDuBao() {
    const inputGiaTri = document.getElementById("simGiaTriCoHoi");
    const selectGiaiDoan = document.getElementById("simGiaiDoan");
    const sliderProb = document.getElementById("simSliderProb");
    const outResult = document.getElementById("simKetQuaDuBao");
    const outFormula = document.getElementById("simFormulaText");

    if (!inputGiaTri || !outResult) return;

    let giaTri = parseFloat(inputGiaTri.value) || 0;
    let xacSuat = 0;

    if (sliderProb) {
        xacSuat = parseInt(sliderProb.value, 10) || 0;
    } else if (selectGiaiDoan) {
        xacSuat = parseInt(selectGiaiDoan.value, 10) || 0;
    }

    const duBao = giaTri * (xacSuat / 100);

    outResult.textContent = dinhDangTienTe(duBao);
    if (outFormula) {
        outFormula.innerHTML = "Công thức: " + dinhDangTienTe(giaTri) + " &times; " + xacSuat + "% = " + dinhDangTienTe(duBao);
    }
}

function escapeHtml(str) {
    if (str == null) return "";
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

/**
 * 5. AC 3: Modal Kiểm tra Điều kiện Rời Giai đoạn (AJAX API)
 */
function kiemTraDieuKienGiaiDoan(id, ten, soGap, soGoi, coBaoGia, coNhuCau, moTa) {
    const modal = document.getElementById("modalKiemTraDieuKien");
    if (!modal) return;

    // Hiển thị tên giai đoạn an toàn XSS
    const elTen = document.getElementById("modalTenGiaiDoan");
    if (elTen) elTen.textContent = ten || "";

    // Xử lý quy định: Nếu moTa có và không rỗng/null thì dùng moTa, ngược lại tự sinh từ điều kiện thực tế
    let quyDinhText = (moTa && moTa !== "null" && moTa.trim().length > 0) ? moTa.trim() : "";
    if (!quyDinhText) {
        let parts = [];
        if (soGap > 0) parts.push("ít nhất " + soGap + " cuộc gặp");
        if (soGoi > 0) parts.push("ít nhất " + soGoi + " cuộc gọi");
        if (coBaoGia) parts.push("gửi báo giá niêm yết");
        if (coNhuCau) parts.push("hoàn thành khảo sát nhu cầu");

        if (parts.length > 0) {
            quyDinhText = "Phải có " + parts.join(" và ");
        } else {
            quyDinhText = "Chưa thiết lập mô tả cụ thể";
        }
    }
    const elMoTa = document.getElementById("modalMoTaDieuKien");
    if (elMoTa) elMoTa.textContent = quyDinhText;

    const elId = document.getElementById("modalGiaiDoanId");
    if (elId) elId.value = id || "";

    // Reset inputs
    const elGap = document.getElementById("testSoGap");
    if (elGap) elGap.value = 0;
    const elGoi = document.getElementById("testSoGoi");
    if (elGoi) elGoi.value = 0;
    const elBaoGia = document.getElementById("testBaoGia");
    if (elBaoGia) elBaoGia.checked = false;
    const elNhuCau = document.getElementById("testNhuCau");
    if (elNhuCau) elNhuCau.checked = false;

    // Checklist preview các điều kiện cần đạt của giai đoạn này
    let checklistHtml = "<div style='font-size: 12px; color: var(--gray-600); margin-top: 6px; line-height: 1.5;'>";
    checklistHtml += "<strong>Tiêu chí bắt buộc:</strong><ul style='margin-left: 18px; margin-top: 4px;'>";
    if (soGap > 0) checklistHtml += "<li>Tối thiểu <strong>" + soGap + " cuộc gặp</strong></li>";
    if (soGoi > 0) checklistHtml += "<li>Tối thiểu <strong>" + soGoi + " cuộc gọi</strong></li>";
    if (coBaoGia) checklistHtml += "<li>Hoàn thành gửi báo giá niêm yết</li>";
    if (coNhuCau) checklistHtml += "<li>Hoàn thành khảo sát nhu cầu</li>";
    if (soGap === 0 && soGoi === 0 && !coBaoGia && !coNhuCau) {
        checklistHtml += "<li>Không có điều kiện định lượng đặc biệt</li>";
    }
    checklistHtml += "</ul></div>";

    const boxQuyDinh = document.getElementById("modalChecklistQuyDinh");
    if (boxQuyDinh) {
        boxQuyDinh.innerHTML = checklistHtml;
    }

    const boxKetQua = document.getElementById("modalKetQuaDieuKien");
    if (boxKetQua) boxKetQua.innerHTML = "";

    modal.style.display = "flex";
}

function dongModalDieuKien() {
    const modal = document.getElementById("modalKiemTraDieuKien");
    if (modal) modal.style.display = "none";
}

function thucHienKiemTraDieuKien() {
    const idInput = document.getElementById("modalGiaiDoanId");
    const id = idInput ? idInput.value : "";
    const soGap = document.getElementById("testSoGap") ? (document.getElementById("testSoGap").value || 0) : 0;
    const soGoi = document.getElementById("testSoGoi") ? (document.getElementById("testSoGoi").value || 0) : 0;
    const coBaoGia = document.getElementById("testBaoGia") ? document.getElementById("testBaoGia").checked : false;
    const coNhuCau = document.getElementById("testNhuCau") ? document.getElementById("testNhuCau").checked : false;
    const resultBox = document.getElementById("modalKetQuaDieuKien");

    if (!resultBox) return;

    if (!id || id === "null" || isNaN(id) || parseInt(id, 10) <= 0) {
        resultBox.innerHTML =
            "<div class='alert alert-danger' style='margin-top:14px; display:block;'>" +
            "<div style='font-weight: 700; margin-bottom: 4px;'>⚠️ LỖI XÁC THỰC</div>" +
            "<div>ID giai đoạn không hợp lệ.</div>" +
            "</div>";
        return;
    }

    resultBox.innerHTML = "<div style='padding: 10px; color: var(--gray-500); text-align: center;'>⏳ Đang xác minh điều kiện...</div>";

    const modal = document.getElementById("modalKiemTraDieuKien");
    let endpoint = (modal && modal.dataset && modal.dataset.endpoint) ? modal.dataset.endpoint : null;
    if (!endpoint) {
        const path = window.location.pathname;
        const idx = path.indexOf("/pipeline");
        const ctx = (idx > 0) ? path.substring(0, idx) : "";
        endpoint = ctx + "/pipeline/giai-doan/kiem-tra-dieu-kien";
    }

    const url = endpoint + "?giaiDoanId=" + encodeURIComponent(id) +
        "&soCuocGap=" + encodeURIComponent(soGap) +
        "&soCuocGoi=" + encodeURIComponent(soGoi) +
        "&daBaoGia=" + encodeURIComponent(coBaoGia) +
        "&daKhaoSat=" + encodeURIComponent(coNhuCau) +
        "&daCoBaoGia=" + encodeURIComponent(coBaoGia) +
        "&daKhaoSatNhuCau=" + encodeURIComponent(coNhuCau);

    fetch(url)
        .then(function (res) { return res.json(); })
        .then(function (data) {
            if (data.thoaDieuKien) {
                resultBox.innerHTML =
                    "<div class='alert alert-success' style='margin-top:14px; display:block;'>" +
                    "<div style='font-weight: 700; margin-bottom: 4px; display:flex; align-items:center; gap:6px;'><span class='material-symbols-outlined icon-sm' aria-hidden='true'>check_circle</span> THỎA MÃN ĐIỀU KIỆN RỜI GIAI ĐOẠN</div>" +
                    "<div>Cơ hội này đã đáp ứng đầy đủ tiêu chuẩn quy định để chuyển sang bước kế tiếp trong quy trình bán hàng.</div>" +
                    "</div>";
            } else {
                let listHtml = "<ul style='margin-left: 20px; margin-top: 6px; line-height: 1.6;'>";
                if (data.danhSachYeuCauThieu && data.danhSachYeuCauThieu.length > 0) {
                    data.danhSachYeuCauThieu.forEach(function (req) {
                        listHtml += "<li>" + escapeHtml(req) + "</li>";
                    });
                } else if (data.thongBao) {
                    listHtml += "<li>" + escapeHtml(data.thongBao) + "</li>";
                }
                listHtml += "</ul>";

                resultBox.innerHTML =
                    "<div class='alert alert-danger' style='margin-top:14px; display:block;'>" +
                    "<div style='font-weight: 700; margin-bottom: 4px; display:flex; align-items:center; gap:6px;'><span class='material-symbols-outlined icon-sm' aria-hidden='true'>warning</span> CHƯA ĐỦ ĐIỀU KIỆN RỜI GIAI ĐOẠN (AC 3)</div>" +
                    "<div style='font-size: 13px;'>" + escapeHtml(data.thongBao || "Hệ thống ngăn không cho chuyển giai đoạn nhằm đảm bảo tính xác thực của dữ liệu:") + "</div>" +
                    listHtml +
                    "</div>";
            }
        })
        .catch(function (err) {
            resultBox.innerHTML = "<div class='alert alert-danger' style='margin-top:14px; display:block;'>Lỗi kết nối kiểm tra: " + escapeHtml(err.message) + "</div>";
        });
}

/**
 * 6. AC 4: Modal Xóa An toàn & Cảnh báo bảo vệ cơ hội đang chạy
 */
let formXoaHienTai = null;

function xacNhanXoaGiaiDoan(formEl, tenGiaiDoan, soCoHoi) {
    if (soCoHoi > 0) {
        // Cấm xóa cứng - hiển thị modal giải thích quy định AC 4
        hienModalBaoVeCoHoi(tenGiaiDoan, soCoHoi);
        return false;
    }

    // Nếu không có cơ hội đang chạy, mở modal xác nhận xóa
    formXoaHienTai = formEl;
    const modalXacNhan = document.getElementById("modalXacNhanXoa");
    if (modalXacNhan) {
        document.getElementById("tenGiaiDoanCanXoa").textContent = tenGiaiDoan;
        modalXacNhan.style.display = "flex";
    } else {
        if (confirm("Bạn có chắc chắn muốn xoá vĩnh viễn giai đoạn '" + tenGiaiDoan + "' khỏi chuỗi quy trình pipeline?")) {
            formEl.submit();
        }
    }
    return false;
}

function dongModalXacNhanXoa() {
    const modal = document.getElementById("modalXacNhanXoa");
    if (modal) modal.style.display = "none";
    formXoaHienTai = null;
}

function dongYThucHienXoa() {
    if (formXoaHienTai) {
        formXoaHienTai.submit();
    }
}

function hienModalBaoVeCoHoi(tenGiaiDoan, soCoHoi) {
    const modal = document.getElementById("modalBaoVeCoHoi");
    if (modal) {
        document.getElementById("tenGiaiDoanBaoVe").textContent = tenGiaiDoan;
        document.getElementById("soCoHoiBaoVe").textContent = soCoHoi;
        modal.style.display = "flex";
    } else {
        alert("KHÔNG THỂ XOÁ: Giai đoạn '" + tenGiaiDoan + "' đang có " + soCoHoi + " cơ hội đang chạy. Hãy sử dụng chức năng 'Ngừng áp dụng' để bảo toàn dữ liệu.");
    }
}

function dongModalBaoVeCoHoi() {
    const modal = document.getElementById("modalBaoVeCoHoi");
    if (modal) modal.style.display = "none";
}

/**
 * 7. Bộ lọc / Tìm kiếm nhanh giai đoạn trong bảng
 */
function khoiTaoBoLocGiaiDoan() {
    const inputSearch = document.getElementById("timKiemGiaiDoan");
    if (!inputSearch) return;

    inputSearch.addEventListener("input", function () {
        const query = this.value.toLowerCase().trim();
        const rows = document.querySelectorAll("#bangGiaiDoanBody tr");

        rows.forEach(function (row) {
            const text = row.textContent.toLowerCase();
            if (text.includes(query)) {
                row.style.display = "";
            } else {
                row.style.display = "none";
            }
        });
    });
}

/**
 * Hàm tiện ích: Định dạng số tiền tệ VNĐ (ví dụ: 100.000.000 ₫)
 */
function dinhDangTienTe(soTien) {
    if (isNaN(soTien)) soTien = 0;
    return Number(Math.round(soTien)).toLocaleString("vi-VN") + " ₫";
}

function moModalDieuKienTuBtn(btn) {
    if (!btn) return;
    var id = btn.getAttribute('data-giai-doan-id') || btn.getAttribute('data-stage-id') || btn.getAttribute('data-id');
    var ten = btn.getAttribute('data-ten-giai-doan') || btn.getAttribute('data-ten') || '';
    var gap = parseInt(btn.getAttribute('data-cuoc-gap') || btn.getAttribute('data-gap'), 10) || 0;
    var goi = parseInt(btn.getAttribute('data-cuoc-goi') || btn.getAttribute('data-goi'), 10) || 0;
    var baogia = (btn.getAttribute('data-bao-gia') || btn.getAttribute('data-baogia')) === 'true';
    var khaosat = (btn.getAttribute('data-khao-sat') || btn.getAttribute('data-khaosat')) === 'true';
    var mota = btn.getAttribute('data-dieu-kien-bat-buoc') || btn.getAttribute('data-mota') || '';
    kiemTraDieuKienGiaiDoan(id, ten, gap, goi, baogia, khaosat, mota);
}

function xacNhanXoaTuBtn(formEl, btn) {
    if (!btn) return false;
    var ten = btn.getAttribute('data-ten-giai-doan') || btn.getAttribute('data-ten') || '';
    var cohoi = parseInt(btn.getAttribute('data-so-co-hoi') || btn.getAttribute('data-cohoi'), 10) || 0;
    return xacNhanXoaGiaiDoan(formEl, ten, cohoi);
}
