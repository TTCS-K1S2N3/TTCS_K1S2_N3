package vn.nhom10.crm.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Model biểu diễn Người liên hệ (bảng nguoi_lien_he) trong Story S3-02.
 * Mỗi khách hàng có nhiều người liên hệ với chức danh, email, số điện thoại.
 * Hỗ trợ đánh dấu vai trò quyết định mua và đánh dấu đầu mối chính.
 */
public class NguoiLienHe implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long khachHangId;
    private String hoTen;
    private String chucDanh;
    private String email;
    private String soDienThoai;
    private VaiTroQuyetDinhEnum vaiTroQuyetDinh;
    private boolean laDauMoiChinh;
    private String trangThai; // DANG_HOAT_DONG, NGUNG_HOAT_DONG
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Các trường join thông tin khách hàng (phục vụ hiển thị)
    private String tenKhachHang;
    private String maKhachHang;
    private String maSoThueKhachHang;

    public NguoiLienHe() {
        this.laDauMoiChinh = false;
        this.trangThai = "DANG_HOAT_DONG";
    }

    public NguoiLienHe(Long id, Long khachHangId, String hoTen, String chucDanh, String email,
                       String soDienThoai, VaiTroQuyetDinhEnum vaiTroQuyetDinh, boolean laDauMoiChinh) {
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

    public VaiTroQuyetDinhEnum getVaiTroQuyetDinh() {
        return vaiTroQuyetDinh;
    }

    public void setVaiTroQuyetDinh(VaiTroQuyetDinhEnum vaiTroQuyetDinh) {
        this.vaiTroQuyetDinh = vaiTroQuyetDinh;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
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

    public String getMaSoThueKhachHang() {
        return maSoThueKhachHang;
    }

    public void setMaSoThueKhachHang(String maSoThueKhachHang) {
        this.maSoThueKhachHang = maSoThueKhachHang;
    }

    public String getTenVaiTroHienThi() {
        return vaiTroQuyetDinh != null ? vaiTroQuyetDinh.getTenHienThi() : "Chưa xác định";
    }

    public String getBadgeClassVaiTro() {
        return vaiTroQuyetDinh != null ? vaiTroQuyetDinh.getBadgeClass() : "badge-secondary";
    }

    @Override
    public String toString() {
        return "NguoiLienHe{" +
                "id=" + id +
                ", khachHangId=" + khachHangId +
                ", hoTen='" + hoTen + '\'' +
                ", chucDanh='" + chucDanh + '\'' +
                ", email='" + email + '\'' +
                ", soDienThoai='" + soDienThoai + '\'' +
                ", vaiTroQuyetDinh=" + (vaiTroQuyetDinh != null ? vaiTroQuyetDinh.getMa() : null) +
                ", laDauMoiChinh=" + laDauMoiChinh +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
