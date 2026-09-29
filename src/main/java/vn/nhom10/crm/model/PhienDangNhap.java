package vn.nhom10.crm.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Model biểu diễn phiên đăng nhập người dùng trong bảng phien_dang_nhap.
 * Hỗ trợ thu hồi phiên đăng nhập khi đổi mật khẩu (AC 3).
 */
public class PhienDangNhap implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String TRANG_THAI_HOAT_DONG = "HOAT_DONG";
    public static final String TRANG_THAI_DA_THU_HOI = "DA_THU_HOI";
    public static final String TRANG_THAI_HET_HAN = "HET_HAN";

    private Long id;
    private Long nguoiDungId;
    private String maPhien;
    private String diaChiIp;
    private String thongTinThietBi;
    private String trangThai = TRANG_THAI_HOAT_DONG;
    private LocalDateTime thoiGianTao;
    private LocalDateTime thoiGianHoatDongCuoi;
    private LocalDateTime thoiGianThuHoi;

    public PhienDangNhap() {
    }

    public PhienDangNhap(Long nguoiDungId, String maPhien, String diaChiIp, String thongTinThietBi) {
        this.nguoiDungId = nguoiDungId;
        this.maPhien = maPhien;
        this.diaChiIp = diaChiIp;
        this.thongTinThietBi = thongTinThietBi;
        this.trangThai = TRANG_THAI_HOAT_DONG;
        this.thoiGianTao = LocalDateTime.now();
        this.thoiGianHoatDongCuoi = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNguoiDungId() {
        return nguoiDungId;
    }

    public void setNguoiDungId(Long nguoiDungId) {
        this.nguoiDungId = nguoiDungId;
    }

    public String getMaPhien() {
        return maPhien;
    }

    public void setMaPhien(String maPhien) {
        this.maPhien = maPhien;
    }

    public String getDiaChiIp() {
        return diaChiIp;
    }

    public void setDiaChiIp(String diaChiIp) {
        this.diaChiIp = diaChiIp;
    }

    public String getThongTinThietBi() {
        return thongTinThietBi;
    }

    public void setThongTinThietBi(String thongTinThietBi) {
        this.thongTinThietBi = thongTinThietBi;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(LocalDateTime thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }

    public LocalDateTime getThoiGianHoatDongCuoi() {
        return thoiGianHoatDongCuoi;
    }

    public void setThoiGianHoatDongCuoi(LocalDateTime thoiGianHoatDongCuoi) {
        this.thoiGianHoatDongCuoi = thoiGianHoatDongCuoi;
    }

    public LocalDateTime getThoiGianThuHoi() {
        return thoiGianThuHoi;
    }

    public void setThoiGianThuHoi(LocalDateTime thoiGianThuHoi) {
        this.thoiGianThuHoi = thoiGianThuHoi;
    }

    public boolean isDangHoatDong() {
        return TRANG_THAI_HOAT_DONG.equalsIgnoreCase(this.trangThai);
    }

    public boolean isDaThuHoi() {
        return TRANG_THAI_DA_THU_HOI.equalsIgnoreCase(this.trangThai);
    }
}
