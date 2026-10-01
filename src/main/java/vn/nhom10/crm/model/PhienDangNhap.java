package vn.nhom10.crm.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Model quản lý phiên đăng nhập người dùng (Story S1-02, S1-04).
 */
public class PhienDangNhap implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String TRANG_THAI_HOAT_DONG = "HOAT_DONG";
    public static final String TRANG_THAI_DA_DANG_XUAT = "DA_DANG_XUAT";
    public static final String TRANG_THAI_DA_THU_HOI = "DA_THU_HOI";
    public static final String TRANG_THAI_HET_HAN = "HET_HAN";

    private long id;
    private long nguoiDungId;
    private String maPhien;
    private String diaChiIp;
    private String thongTinThietBi;
    private String trangThai = TRANG_THAI_HOAT_DONG;
    private Timestamp thoiGianTao;
    private Timestamp thoiGianHoatDongCuoi;
    private Timestamp thoiGianThuHoi;

    public PhienDangNhap() {
    }

    public PhienDangNhap(long nguoiDungId, String maPhien, String diaChiIp, String thongTinThietBi) {
        this.nguoiDungId = nguoiDungId;
        this.maPhien = maPhien;
        this.diaChiIp = diaChiIp;
        this.thongTinThietBi = thongTinThietBi;
        this.trangThai = TRANG_THAI_HOAT_DONG;
        Timestamp now = new Timestamp(System.currentTimeMillis());
        this.thoiGianTao = now;
        this.thoiGianHoatDongCuoi = now;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getNguoiDungId() {
        return nguoiDungId;
    }

    public void setNguoiDungId(long nguoiDungId) {
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

    public Timestamp getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(Timestamp thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }

    public Timestamp getThoiGianHoatDongCuoi() {
        return thoiGianHoatDongCuoi;
    }

    public void setThoiGianHoatDongCuoi(Timestamp thoiGianHoatDongCuoi) {
        this.thoiGianHoatDongCuoi = thoiGianHoatDongCuoi;
    }

    public Timestamp getThoiGianThuHoi() {
        return thoiGianThuHoi;
    }

    public void setThoiGianThuHoi(Timestamp thoiGianThuHoi) {
        this.thoiGianThuHoi = thoiGianThuHoi;
    }

    public boolean isDangHoatDong() {
        return TRANG_THAI_HOAT_DONG.equalsIgnoreCase(trangThai);
    }

    public boolean isDaThuHoi() {
        return TRANG_THAI_DA_THU_HOI.equalsIgnoreCase(trangThai);
    }

    /**
     * Kiểm tra phiên có bị hết hạn so với thời gian không hoạt động hay không.
     *
     * @param timeoutPhut Thời gian timeout phiên (ví dụ 30 phút)
     * @return true nếu phiên đã hết hạn
     */
    public boolean isHetHan(int timeoutPhut) {
        if (!isDangHoatDong()) {
            return true;
        }
        if (thoiGianHoatDongCuoi == null) {
            return false;
        }
        long diff = System.currentTimeMillis() - thoiGianHoatDongCuoi.getTime();
        return diff > (timeoutPhut * 60 * 1000L);
    }

    /**
     * Tính số giây còn lại trước khi phiên hết hạn.
     */
    public long tinhSoGiayConLai(int timeoutPhut) {
        if (!isDangHoatDong() || thoiGianHoatDongCuoi == null) {
            return 0;
        }
        long passedMillis = System.currentTimeMillis() - thoiGianHoatDongCuoi.getTime();
        long maxMillis = timeoutPhut * 60 * 1000L;
        long remainMillis = maxMillis - passedMillis;
        return Math.max(0, remainMillis / 1000L);
    }
}
