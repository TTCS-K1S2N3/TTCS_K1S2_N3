package vn.nhom10.crm.dto;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * DTO đại diện cho báo giá hợp lệ có thể liên kết để tạo hợp đồng pháp lý (Story S3-03 & Task 3).
 * Chỉ gồm các báo giá của đúng khách hàng, chưa được sử dụng cho hợp đồng nào khác.
 */
public class BaoGiaHopLeDTO {

    private Long id;
    private String maBaoGia;
    private String tieuDe;
    private Long khachHangId;
    private Long coHoiId;
    private String tenCoHoi;
    private Long phienBanId;
    private BigDecimal giaTriBaoGia;
    private String trangThai;
    private String dieuKhoanThanhToan;
    private Long nguoiPhuTrachId;
    private Long nhomKinhDoanhId;

    public BaoGiaHopLeDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaBaoGia() {
        return maBaoGia;
    }

    public void setMaBaoGia(String maBaoGia) {
        this.maBaoGia = maBaoGia;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public Long getKhachHangId() {
        return khachHangId;
    }

    public void setKhachHangId(Long khachHangId) {
        this.khachHangId = khachHangId;
    }

    public Long getCoHoiId() {
        return coHoiId;
    }

    public void setCoHoiId(Long coHoiId) {
        this.coHoiId = coHoiId;
    }

    public String getTenCoHoi() {
        return tenCoHoi;
    }

    public void setTenCoHoi(String tenCoHoi) {
        this.tenCoHoi = tenCoHoi;
    }

    public Long getPhienBanId() {
        return phienBanId;
    }

    public void setPhienBanId(Long phienBanId) {
        this.phienBanId = phienBanId;
    }

    public BigDecimal getGiaTriBaoGia() {
        return giaTriBaoGia;
    }

    public void setGiaTriBaoGia(BigDecimal giaTriBaoGia) {
        this.giaTriBaoGia = giaTriBaoGia;
    }

    public String getTongTienDinhDang() {
        if (giaTriBaoGia == null) {
            return "0 đ";
        }
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return nf.format(giaTriBaoGia) + " đ";
    }

    public String getGiaTriDinhDang() {
        return getTongTienDinhDang();
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getDieuKhoanThanhToan() {
        return dieuKhoanThanhToan;
    }

    public void setDieuKhoanThanhToan(String dieuKhoanThanhToan) {
        this.dieuKhoanThanhToan = dieuKhoanThanhToan;
    }

    public Long getNguoiPhuTrachId() {
        return nguoiPhuTrachId;
    }

    public void setNguoiPhuTrachId(Long nguoiPhuTrachId) {
        this.nguoiPhuTrachId = nguoiPhuTrachId;
    }

    public Long getNhomKinhDoanhId() {
        return nhomKinhDoanhId;
    }

    public void setNhomKinhDoanhId(Long nhomKinhDoanhId) {
        this.nhomKinhDoanhId = nhomKinhDoanhId;
    }
}
