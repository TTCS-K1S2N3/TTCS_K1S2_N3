package vn.nhom10.crm.model;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Model biểu diễn bảng `nguoi_lien_he` theo schema chuẩn CRM.
 * Hỗ trợ tìm kiếm theo số điện thoại người liên hệ (Story S3-07).
 */
public class NguoiLienHe {

    private Long id;
    private Long khachHangId;
    private String hoTen;
    private String chucDanh;
    private String email;
    private String soDienThoai;
    private String vaiTroQuyetDinh;
    private boolean laDauMoiChinh = false;
    private String trangThai = "DANG_HOAT_DONG";
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public NguoiLienHe() {
    }

    public NguoiLienHe(Long khachHangId, String hoTen, String soDienThoai) {
        this.khachHangId = khachHangId;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NguoiLienHe that = (NguoiLienHe) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "NguoiLienHe{" +
                "id=" + id +
                ", khachHangId=" + khachHangId +
                ", hoTen='" + hoTen + '\'' +
                ", soDienThoai='" + soDienThoai + '\'' +
                ", laDauMoiChinh=" + laDauMoiChinh +
                '}';
    }
}
