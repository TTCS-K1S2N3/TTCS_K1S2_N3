package vn.nhom10.crm.dto;

import java.io.Serializable;

/**
 * DTO trả về kết quả sau khi thực hiện nghiệp vụ khoá tài khoản và bàn giao dữ liệu (Story S1-10).
 */
public class KetQuaKhoaVaBanGiaoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean thanhCong;
    private String thongBao;
    private int soKhachHangChuyen;
    private int soCoHoiChuyen;
    private int nhatKyId;

    public KetQuaKhoaVaBanGiaoDTO() {
    }

    public KetQuaKhoaVaBanGiaoDTO(boolean thanhCong, String thongBao) {
        this.thanhCong = thanhCong;
        this.thongBao = thongBao;
    }

    public KetQuaKhoaVaBanGiaoDTO(boolean thanhCong, String thongBao, int soKhachHangChuyen, int soCoHoiChuyen, int nhatKyId) {
        this.thanhCong = thanhCong;
        this.thongBao = thongBao;
        this.soKhachHangChuyen = soKhachHangChuyen;
        this.soCoHoiChuyen = soCoHoiChuyen;
        this.nhatKyId = nhatKyId;
    }

    public boolean isThanhCong() {
        return thanhCong;
    }

    public void setThanhCong(boolean thanhCong) {
        this.thanhCong = thanhCong;
    }

    public String getThongBao() {
        return thongBao;
    }

    public void setThongBao(String thongBao) {
        this.thongBao = thongBao;
    }

    public int getSoKhachHangChuyen() {
        return soKhachHangChuyen;
    }

    public void setSoKhachHangChuyen(int soKhachHangChuyen) {
        this.soKhachHangChuyen = soKhachHangChuyen;
    }

    public int getSoCoHoiChuyen() {
        return soCoHoiChuyen;
    }

    public void setSoCoHoiChuyen(int soCoHoiChuyen) {
        this.soCoHoiChuyen = soCoHoiChuyen;
    }

    public int getNhatKyId() {
        return nhatKyId;
    }

    public void setNhatKyId(int nhatKyId) {
        this.nhatKyId = nhatKyId;
    }
}
