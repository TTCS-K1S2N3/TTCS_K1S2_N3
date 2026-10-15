package vn.nhom10.crm.dto;

import java.io.Serializable;

/**
 * DTO cảnh báo trùng lặp thông tin khách hàng khi chuyển đổi Lead (Story S4-08).
 * Đáp ứng Q2: Trùng MST chặn tạo mới (409); website/tên trùng cảnh báo rà soát.
 */
public class CanhBaoTrungDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String loai; // MA_SO_THUE, TEN_CONG_TY, WEBSITE
    private boolean coTheTrung;
    private boolean xacNhanTrung;
    private boolean hienThiChiTiet;
    private String chiTiet;

    public CanhBaoTrungDTO() {
    }

    public CanhBaoTrungDTO(String loai, boolean coTheTrung, boolean xacNhanTrung, boolean hienThiChiTiet, String chiTiet) {
        this.loai = loai;
        this.coTheTrung = coTheTrung;
        this.xacNhanTrung = xacNhanTrung;
        this.hienThiChiTiet = hienThiChiTiet;
        this.chiTiet = chiTiet;
    }

    public String getLoai() {
        return loai;
    }

    public void setLoai(String loai) {
        this.loai = loai;
    }

    public boolean isCoTheTrung() {
        return coTheTrung;
    }

    public void setCoTheTrung(boolean coTheTrung) {
        this.coTheTrung = coTheTrung;
    }

    public boolean isXacNhanTrung() {
        return xacNhanTrung;
    }

    public void setXacNhanTrung(boolean xacNhanTrung) {
        this.xacNhanTrung = xacNhanTrung;
    }

    public boolean isHienThiChiTiet() {
        return hienThiChiTiet;
    }

    public void setHienThiChiTiet(boolean hienThiChiTiet) {
        this.hienThiChiTiet = hienThiChiTiet;
    }

    public String getChiTiet() {
        return chiTiet;
    }

    public void setChiTiet(String chiTiet) {
        this.chiTiet = chiTiet;
    }
}
