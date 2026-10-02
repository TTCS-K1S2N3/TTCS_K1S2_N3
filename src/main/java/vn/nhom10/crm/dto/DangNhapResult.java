package vn.nhom10.crm.dto;

import vn.nhom10.crm.model.NguoiDung;

import java.io.Serializable;

/**
 * Kết quả xử lý đăng nhập từ AuthService.
 */
public class DangNhapResult implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Status {
        THANH_CONG,
        SAI_THONG_TIN,
        KHOA_TAM_15_PHUT,
        TAI_KHOAN_BI_KHOA
    }

    private final Status status;
    private final String thongBao;
    private final NguoiDung nguoiDung;

    public DangNhapResult(Status status, String thongBao, NguoiDung nguoiDung) {
        this.status = status;
        this.thongBao = thongBao;
        this.nguoiDung = nguoiDung;
    }

    public static DangNhapResult thanhCong(NguoiDung nguoiDung) {
        return new DangNhapResult(Status.THANH_CONG, "Đăng nhập thành công", nguoiDung);
    }

    public static DangNhapResult saiThongTin(String thongBao) {
        return new DangNhapResult(Status.SAI_THONG_TIN, thongBao, null);
    }

    public static DangNhapResult khoaTam15Phut(String thongBao) {
        return new DangNhapResult(Status.KHOA_TAM_15_PHUT, thongBao, null);
    }

    public static DangNhapResult taiKhoanBiKhoa(String thongBao) {
        return new DangNhapResult(Status.TAI_KHOAN_BI_KHOA, thongBao, null);
    }

    public boolean isThanhCong() {
        return status == Status.THANH_CONG;
    }

    public Status getStatus() {
        return status;
    }

    public String getThongBao() {
        return thongBao;
    }

    public NguoiDung getNguoiDung() {
        return nguoiDung;
    }
}
