package vn.nhom10.crm.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Model biểu diễn thông tin người dùng trong hệ thống CRM.
 */
public class NguoiDung implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String hoTen;
    private String email;
    private String matKhau;
    private String soDienThoai;
    private String trangThai; // HOAT_DONG, KHOA, VO_HIEU_HOA
    private int soLanSai;
    private LocalDateTime thoiGianKhoa;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;

    public NguoiDung() {
    }

    public NguoiDung(Long id, String hoTen, String email, String matKhau, String soDienThoai, 
                     String trangThai, int soLanSai, LocalDateTime thoiGianKhoa, 
                     LocalDateTime ngayTao, LocalDateTime ngayCapNhat) {
        this.id = id;
        this.hoTen = hoTen;
        this.email = email;
        this.matKhau = matKhau;
        this.soDienThoai = soDienThoai;
        this.trangThai = trangThai;
        this.soLanSai = soLanSai;
        this.thoiGianKhoa = thoiGianKhoa;
        this.ngayTao = ngayTao;
        this.ngayCapNhat = ngayCapNhat;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public int getSoLanSai() {
        return soLanSai;
    }

    public void setSoLanSai(int soLanSai) {
        this.soLanSai = soLanSai;
    }

    public LocalDateTime getThoiGianKhoa() {
        return thoiGianKhoa;
    }

    public void setThoiGianKhoa(LocalDateTime thoiGianKhoa) {
        this.thoiGianKhoa = thoiGianKhoa;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public LocalDateTime getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(LocalDateTime ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }

    public boolean isDangHoatDong() {
        return "HOAT_DONG".equalsIgnoreCase(this.trangThai);
    }
}
