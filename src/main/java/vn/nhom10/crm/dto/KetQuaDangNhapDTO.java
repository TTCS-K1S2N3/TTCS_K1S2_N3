package vn.nhom10.crm.dto;

import vn.nhom10.crm.model.NguoiDung;

import java.io.Serializable;

/**
 * Data Transfer Object đóng gói kết quả xác thực đăng nhập.
 */
public class KetQuaDangNhapDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean thanhCong;
    private NguoiDung nguoiDung;
    private String thongBaoLoi;
    private String trangChuUrl;
    private boolean biKhoaTam;
    private long soPhutKhoaConLai;

    public KetQuaDangNhapDTO() {
    }

    public static KetQuaDangNhapDTO thanhCong(NguoiDung nguoiDung, String trangChuUrl) {
        KetQuaDangNhapDTO dto = new KetQuaDangNhapDTO();
        dto.thanhCong = true;
        dto.nguoiDung = nguoiDung;
        dto.trangChuUrl = trangChuUrl;
        return dto;
    }

    public static KetQuaDangNhapDTO thatBai(String thongBaoLoi) {
        KetQuaDangNhapDTO dto = new KetQuaDangNhapDTO();
        dto.thanhCong = false;
        dto.thongBaoLoi = thongBaoLoi;
        return dto;
    }

    public static KetQuaDangNhapDTO khoaTam(long soPhutKhoaConLai) {
        KetQuaDangNhapDTO dto = new KetQuaDangNhapDTO();
        dto.thanhCong = false;
        dto.biKhoaTam = true;
        dto.soPhutKhoaConLai = soPhutKhoaConLai;
        dto.thongBaoLoi = "Tài khoản tạm thời bị khóa do nhập sai quá 5 lần liên tiếp. Vui lòng thử lại sau " 
                + soPhutKhoaConLai + " phút.";
        return dto;
    }

    public static KetQuaDangNhapDTO taiKhoanBiKhoa() {
        KetQuaDangNhapDTO dto = new KetQuaDangNhapDTO();
        dto.thanhCong = false;
        dto.thongBaoLoi = "Tài khoản của bạn đã bị khóa hoặc ngừng hoạt động. Vui lòng liên hệ quản trị viên.";
        return dto;
    }

    public boolean isThanhCong() {
        return thanhCong;
    }

    public void setThanhCong(boolean thanhCong) {
        this.thanhCong = thanhCong;
    }

    public NguoiDung getNguoiDung() {
        return nguoiDung;
    }

    public void setNguoiDung(NguoiDung nguoiDung) {
        this.nguoiDung = nguoiDung;
    }

    public String getThongBaoLoi() {
        return thongBaoLoi;
    }

    public void setThongBaoLoi(String thongBaoLoi) {
        this.thongBaoLoi = thongBaoLoi;
    }

    public String getTrangChuUrl() {
        return trangChuUrl;
    }

    public void setTrangChuUrl(String trangChuUrl) {
        this.trangChuUrl = trangChuUrl;
    }

    public boolean isBiKhoaTam() {
        return biKhoaTam;
    }

    public void setBiKhoaTam(boolean biKhoaTam) {
        this.biKhoaTam = biKhoaTam;
    }

    public long getSoPhutKhoaConLai() {
        return soPhutKhoaConLai;
    }

    public void setSoPhutKhoaConLai(long soPhutKhoaConLai) {
        this.soPhutKhoaConLai = soPhutKhoaConLai;
    }
}
