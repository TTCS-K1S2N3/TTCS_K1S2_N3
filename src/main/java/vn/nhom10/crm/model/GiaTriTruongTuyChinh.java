package vn.nhom10.crm.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * Model đại diện cho giá trị của một trường tuỳ chỉnh gắn với Khách hàng hoặc Cơ hội.
 * Tương ứng với bảng `gia_tri_truong_tuy_chinh_khach_hang` và `gia_tri_truong_tuy_chinh_co_hoi`.
 */
public class GiaTriTruongTuyChinh implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long doiTuongId; // ID của khach_hang hoặc co_hoi
    private Long truongTuyChinhId;
    private String maTruong; // Technical code khi join
    private String tenTruong; // Label khi join
    private String kieuDuLieu;
    private String giaTriVanBan;
    private BigDecimal giaTriSo;
    private LocalDate giaTriNgay;
    private String giaTriJson;
    private Timestamp updatedAt;

    public GiaTriTruongTuyChinh() {
    }

    public GiaTriTruongTuyChinh(Long doiTuongId, Long truongTuyChinhId) {
        this.doiTuongId = doiTuongId;
        this.truongTuyChinhId = truongTuyChinhId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDoiTuongId() {
        return doiTuongId;
    }

    public void setDoiTuongId(Long doiTuongId) {
        this.doiTuongId = doiTuongId;
    }

    public Long getTruongTuyChinhId() {
        return truongTuyChinhId;
    }

    public void setTruongTuyChinhId(Long truongTuyChinhId) {
        this.truongTuyChinhId = truongTuyChinhId;
    }

    public String getMaTruong() {
        return maTruong;
    }

    public void setMaTruong(String maTruong) {
        this.maTruong = maTruong;
    }

    public String getTenTruong() {
        return tenTruong;
    }

    public void setTenTruong(String tenTruong) {
        this.tenTruong = tenTruong;
    }

    public String getKieuDuLieu() {
        return kieuDuLieu;
    }

    public void setKieuDuLieu(String kieuDuLieu) {
        this.kieuDuLieu = kieuDuLieu;
    }

    public String getGiaTriVanBan() {
        return giaTriVanBan;
    }

    public void setGiaTriVanBan(String giaTriVanBan) {
        this.giaTriVanBan = giaTriVanBan;
    }

    public BigDecimal getGiaTriSo() {
        return giaTriSo;
    }

    public void setGiaTriSo(BigDecimal giaTriSo) {
        this.giaTriSo = giaTriSo;
    }

    public LocalDate getGiaTriNgay() {
        return giaTriNgay;
    }

    public void setGiaTriNgay(LocalDate giaTriNgay) {
        this.giaTriNgay = giaTriNgay;
    }

    public String getGiaTriJson() {
        return giaTriJson;
    }

    public void setGiaTriJson(String giaTriJson) {
        this.giaTriJson = giaTriJson;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Lấy giá trị chuỗi hiển thị thống nhất theo kiểu dữ liệu.
     */
    public String layGiaTriChuoi() {
        if ("SO".equalsIgnoreCase(kieuDuLieu)) {
            if (giaTriSo != null) {
                // Định dạng hiển thị gọn
                return giaTriSo.stripTrailingZeros().toPlainString();
            }
            return "";
        } else if ("NGAY".equalsIgnoreCase(kieuDuLieu)) {
            return giaTriNgay != null ? giaTriNgay.toString() : "";
        } else if ("DANH_SACH_CHON".equalsIgnoreCase(kieuDuLieu)) {
            if (giaTriVanBan != null && !giaTriVanBan.isBlank()) {
                return giaTriVanBan;
            }
            return giaTriJson != null ? giaTriJson : "";
        } else {
            return giaTriVanBan != null ? giaTriVanBan : "";
        }
    }

    /**
     * Gán giá trị từ chuỗi người dùng nhập (Form / API).
     */
    public void ganGiaTriTuChuoi(String kieuDuLieu, String rawValue) {
        this.kieuDuLieu = kieuDuLieu;
        if (rawValue == null || rawValue.trim().isEmpty()) {
            this.giaTriVanBan = null;
            this.giaTriSo = null;
            this.giaTriNgay = null;
            this.giaTriJson = null;
            return;
        }

        String val = rawValue.trim();
        if ("SO".equalsIgnoreCase(kieuDuLieu)) {
            try {
                this.giaTriSo = new BigDecimal(val.replace(",", ""));
                this.giaTriVanBan = val;
            } catch (Exception e) {
                this.giaTriSo = null;
                this.giaTriVanBan = val;
            }
        } else if ("NGAY".equalsIgnoreCase(kieuDuLieu)) {
            try {
                this.giaTriNgay = LocalDate.parse(val);
                this.giaTriVanBan = val;
            } catch (Exception e) {
                this.giaTriNgay = null;
                this.giaTriVanBan = val;
            }
        } else if ("DANH_SACH_CHON".equalsIgnoreCase(kieuDuLieu)) {
            this.giaTriVanBan = val;
            this.giaTriJson = "\"" + val.replace("\"", "\\\"") + "\"";
        } else {
            this.giaTriVanBan = val;
        }
    }
}
