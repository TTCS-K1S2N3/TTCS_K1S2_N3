package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * DTO chứa thông tin thống kê 4 thẻ tóm tắt cho giao diện Chăm sóc định kỳ (Story S3-09).
 */
public class ThongKeChamSocDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int tongSoCanChamSoc;
    private int soQuaHanNghiemTrong;
    private BigDecimal tongGiaTriHopDong;
    private int soDaLienHeHomNay;

    public ThongKeChamSocDTO() {
        this.tongGiaTriHopDong = BigDecimal.ZERO;
    }

    public ThongKeChamSocDTO(int tongSoCanChamSoc, int soQuaHanNghiemTrong,
                            BigDecimal tongGiaTriHopDong, int soDaLienHeHomNay) {
        this.tongSoCanChamSoc = tongSoCanChamSoc;
        this.soQuaHanNghiemTrong = soQuaHanNghiemTrong;
        this.tongGiaTriHopDong = tongGiaTriHopDong != null ? tongGiaTriHopDong : BigDecimal.ZERO;
        this.soDaLienHeHomNay = soDaLienHeHomNay;
    }

    public int getTongSoCanChamSoc() {
        return tongSoCanChamSoc;
    }

    public void setTongSoCanChamSoc(int tongSoCanChamSoc) {
        this.tongSoCanChamSoc = tongSoCanChamSoc;
    }

    public int getSoQuaHanNghiemTrong() {
        return soQuaHanNghiemTrong;
    }

    public void setSoQuaHanNghiemTrong(int soQuaHanNghiemTrong) {
        this.soQuaHanNghiemTrong = soQuaHanNghiemTrong;
    }

    public BigDecimal getTongGiaTriHopDong() {
        return tongGiaTriHopDong != null ? tongGiaTriHopDong : BigDecimal.ZERO;
    }

    public void setTongGiaTriHopDong(BigDecimal tongGiaTriHopDong) {
        this.tongGiaTriHopDong = tongGiaTriHopDong != null ? tongGiaTriHopDong : BigDecimal.ZERO;
    }

    public String getTongGiaTriHopDongDinhDang() {
        if (tongGiaTriHopDong == null || tongGiaTriHopDong.compareTo(BigDecimal.ZERO) <= 0) {
            return "0 đ";
        }
        NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
        return nf.format(tongGiaTriHopDong) + " đ";
    }

    public int getSoDaLienHeHomNay() {
        return soDaLienHeHomNay;
    }

    public void setSoDaLienHeHomNay(int soDaLienHeHomNay) {
        this.soDaLienHeHomNay = soDaLienHeHomNay;
    }
}
