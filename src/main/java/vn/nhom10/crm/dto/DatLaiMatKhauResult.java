package vn.nhom10.crm.dto;

import java.io.Serializable;

/**
 * DTO kết quả xử lý yêu cầu đặt lại mật khẩu (S1-03).
 */
public class DatLaiMatKhauResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean thanhCong;
    private String thongBao;
    private boolean tokenHopLe;

    public DatLaiMatKhauResult(boolean thanhCong, String thongBao, boolean tokenHopLe) {
        this.thanhCong = thanhCong;
        this.thongBao = thongBao;
        this.tokenHopLe = tokenHopLe;
    }

    public static DatLaiMatKhauResult thanhCong(String thongBao) {
        return new DatLaiMatKhauResult(true, thongBao, true);
    }

    public static DatLaiMatKhauResult thatBai(String thongBao) {
        return new DatLaiMatKhauResult(false, thongBao, false);
    }

    public static DatLaiMatKhauResult tokenHopLe() {
        return new DatLaiMatKhauResult(true, "Token hợp lệ", true);
    }

    public static DatLaiMatKhauResult tokenKhongHopLe(String thongBao) {
        return new DatLaiMatKhauResult(false, thongBao, false);
    }

    public boolean isThanhCong() {
        return thanhCong;
    }

    public String getThongBao() {
        return thongBao;
    }

    public boolean isTokenHopLe() {
        return tokenHopLe;
    }
}
