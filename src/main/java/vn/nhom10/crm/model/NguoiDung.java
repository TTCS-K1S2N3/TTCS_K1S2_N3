package vn.nhom10.crm.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Model biểu diễn thông tin người dùng trong hệ thống CRM.
 */
public class NguoiDung implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String TRANG_THAI_HOAT_DONG = "HOAT_DONG";
    public static final String TRANG_THAI_KHOA = "KHOA";
    public static final String TRANG_THAI_VO_HIEU_HOA = "VO_HIEU_HOA";

    private Long id;
    private String hoTen;
    private String email;
    private String matKhau;
    private String soDienThoai;
    private String trangThai = TRANG_THAI_HOAT_DONG;
    private int soLanSai;
    private LocalDateTime thoiGianKhoa;
    private LocalDateTime ngayDoiMatKhau;
    private int sessionVersion = 1;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;

    public NguoiDung() {
    }

    public NguoiDung(Long id, String hoTen, String email, String matKhau) {
        this.id = id;
        this.hoTen = hoTen;
        this.email = email;
        this.matKhau = matKhau;
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

    public LocalDateTime getNgayDoiMatKhau() {
        return ngayDoiMatKhau;
    }

    public void setNgayDoiMatKhau(LocalDateTime ngayDoiMatKhau) {
        this.ngayDoiMatKhau = ngayDoiMatKhau;
    }

    public int getSessionVersion() {
        return sessionVersion;
    }

    public void setSessionVersion(int sessionVersion) {
        this.sessionVersion = sessionVersion;
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
        return TRANG_THAI_HOAT_DONG.equalsIgnoreCase(this.trangThai);
    }

    public String getTenVietTat() {
        if (hoTen == null || hoTen.isBlank()) {
            return "U";
        }
        String[] tu = hoTen.trim().split("\\s+");
        if (tu.length == 1) {
            return tu[0].substring(0, Math.min(2, tu[0].length())).toUpperCase();
        }
        String dau = tu[0].substring(0, 1).toUpperCase();
        String cuoi = tu[tu.length - 1].substring(0, 1).toUpperCase();
        return dau + cuoi;
    }
}
