package vn.nhom10.crm.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Model biểu diễn Người liên hệ (bảng `nguoi_lien_he`).
 * Phục vụ danh sách người liên hệ và vai trò quyết định trong trang 360 (Story S3-03).
 */
public class NguoiLienHe implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long khachHangId;
    private String hoTen;
    private String chucDanh;
    private String email;
    private String soDienThoai;
    private String vaiTroQuyetDinh; // NGUOI_QUYET_DINH, NGUOI_ANH_HUONG, NGUOI_DUNG_CUOI, NGUOI_CAN_TRO
    private boolean laDauMoiChinh;
    private String trangThai = "DANG_HOAT_DONG";
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Joined / display fields
    private String tenKhachHang;
    private String maKhachHang;

    public NguoiLienHe() {
        this.laDauMoiChinh = false;
        this.trangThai = "DANG_HOAT_DONG";
    }

    public NguoiLienHe(Long id, Long khachHangId, String hoTen, String chucDanh, String email,
                       String soDienThoai, String vaiTroQuyetDinh, boolean laDauMoiChinh) {
        this.id = id;
        this.khachHangId = khachHangId;
        this.hoTen = hoTen;
        this.chucDanh = chucDanh;
        this.email = email;
        this.soDienThoai = soDienThoai;
        this.vaiTroQuyetDinh = vaiTroQuyetDinh;
        this.laDauMoiChinh = laDauMoiChinh;
        this.trangThai = "DANG_HOAT_DONG";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getKhachHangId() {
        return khachHangId;
    }

    public void setKhachHangId(Long khachHangId) {
        this.khachHangId = khachHangId;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getChucDanh() {
        return chucDanh;
    }

    public void setChucDanh(String chucDanh) {
        this.chucDanh = chucDanh;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getVaiTroQuyetDinh() {
        return vaiTroQuyetDinh;
    }

    public void setVaiTroQuyetDinh(String vaiTroQuyetDinh) {
        this.vaiTroQuyetDinh = vaiTroQuyetDinh;
    }

    public VaiTroQuyetDinhEnum getVaiTroQuyetDinhEnum() {
        return VaiTroQuyetDinhEnum.fromMa(this.vaiTroQuyetDinh);
    }

    public boolean isLaDauMoiChinh() {
        return laDauMoiChinh;
    }

    public void setLaDauMoiChinh(boolean laDauMoiChinh) {
        this.laDauMoiChinh = laDauMoiChinh;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getTenVaiTroHienThi() {
        VaiTroQuyetDinhEnum en = getVaiTroQuyetDinhEnum();
        if (en != null) {
            return en.getTenHienThi();
        }
        return vaiTroQuyetDinh != null && !vaiTroQuyetDinh.isBlank() ? vaiTroQuyetDinh : "Chưa xác định";
    }

    public String getBadgeClassVaiTro() {
        VaiTroQuyetDinhEnum en = getVaiTroQuyetDinhEnum();
        return en != null ? en.getBadgeClass() : "badge-role";
    }

    public String getChuCaiDau() {
        if (hoTen == null || hoTen.trim().isEmpty()) {
            return "?";
        }
        String[] parts = hoTen.trim().split("\\s+");
        String last = parts[parts.length - 1];
        return last.substring(0, 1).toUpperCase();
    }
}
