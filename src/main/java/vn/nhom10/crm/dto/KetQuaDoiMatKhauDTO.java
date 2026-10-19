package vn.nhom10.crm.dto;

import java.io.Serializable;

/**
 * DTO chứa kết quả thực hiện nghiệp vụ Đổi mật khẩu và thu hồi phiên.
 */
public class KetQuaDoiMatKhauDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean thanhCong;
    private String thongBao;
    private String maLoi;
    private int soPhienDaThuHoi;

    public KetQuaDoiMatKhauDTO() {
    }

    public KetQuaDoiMatKhauDTO(boolean thanhCong, String thongBao, String maLoi, int soPhienDaThuHoi) {
        this.thanhCong = thanhCong;
        this.thongBao = thongBao;
        this.maLoi = maLoi;
        this.soPhienDaThuHoi = soPhienDaThuHoi;
    }

    public static KetQuaDoiMatKhauDTO thanhCong(String thongBao, int soPhienDaThuHoi) {
        return new KetQuaDoiMatKhauDTO(true, thongBao, null, soPhienDaThuHoi);
    }

    public static KetQuaDoiMatKhauDTO thatBai(String thongBao, String maLoi) {
        return new KetQuaDoiMatKhauDTO(false, thongBao, maLoi, 0);
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

    public String getMaLoi() {
        return maLoi;
    }

    public void setMaLoi(String maLoi) {
        this.maLoi = maLoi;
    }

    public int getSoPhienDaThuHoi() {
        return soPhienDaThuHoi;
    }

    public void setSoPhienDaThuHoi(int soPhienDaThuHoi) {
        this.soPhienDaThuHoi = soPhienDaThuHoi;
    }
}
