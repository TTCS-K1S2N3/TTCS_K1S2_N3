package vn.nhom10.crm.dto;

import vn.nhom10.crm.model.HopDong;
import vn.nhom10.crm.model.KhachHang;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * DTO tổng hợp thông tin nhóm công ty (công ty mẹ và các công ty con) cùng số liệu hợp đồng.
 * Đáp ứng Acceptance Criteria: "Trang công ty mẹ hiển thị tổng giá trị hợp đồng của cả nhóm công ty" (Story S3-05).
 */
public class ThongKeNhomCongTyDTO {

    private KhachHang congTyMe;
    private List<KhachHang> danhSachCongTyCon = new ArrayList<>();
    private List<HopDong> danhSachHopDongNhom = new ArrayList<>();

    private BigDecimal tongGiaTriHopDongCongTyMe = BigDecimal.ZERO;
    private BigDecimal tongGiaTriHopDongCacCongTyCon = BigDecimal.ZERO;
    private BigDecimal tongGiaTriHopDongNhomCongTy = BigDecimal.ZERO;

    private int soLuongCongTyCon = 0;
    private int tongSoHopDong = 0;

    public ThongKeNhomCongTyDTO() {
    }

    public ThongKeNhomCongTyDTO(KhachHang congTyMe) {
        this.congTyMe = congTyMe;
    }

    public KhachHang getCongTyMe() {
        return congTyMe;
    }

    public void setCongTyMe(KhachHang congTyMe) {
        this.congTyMe = congTyMe;
    }

    public List<KhachHang> getDanhSachCongTyCon() {
        return danhSachCongTyCon;
    }

    public void setDanhSachCongTyCon(List<KhachHang> danhSachCongTyCon) {
        this.danhSachCongTyCon = danhSachCongTyCon != null ? danhSachCongTyCon : new ArrayList<>();
        this.soLuongCongTyCon = this.danhSachCongTyCon.size();
    }

    public List<HopDong> getDanhSachHopDongNhom() {
        return danhSachHopDongNhom;
    }

    public void setDanhSachHopDongNhom(List<HopDong> danhSachHopDongNhom) {
        this.danhSachHopDongNhom = danhSachHopDongNhom != null ? danhSachHopDongNhom : new ArrayList<>();
        this.tongSoHopDong = this.danhSachHopDongNhom.size();
    }

    public BigDecimal getTongGiaTriHopDongCongTyMe() {
        return tongGiaTriHopDongCongTyMe != null ? tongGiaTriHopDongCongTyMe : BigDecimal.ZERO;
    }

    public void setTongGiaTriHopDongCongTyMe(BigDecimal tongGiaTriHopDongCongTyMe) {
        this.tongGiaTriHopDongCongTyMe = tongGiaTriHopDongCongTyMe != null ? tongGiaTriHopDongCongTyMe : BigDecimal.ZERO;
    }

    public BigDecimal getTongGiaTriHopDongCacCongTyCon() {
        return tongGiaTriHopDongCacCongTyCon != null ? tongGiaTriHopDongCacCongTyCon : BigDecimal.ZERO;
    }

    public void setTongGiaTriHopDongCacCongTyCon(BigDecimal tongGiaTriHopDongCacCongTyCon) {
        this.tongGiaTriHopDongCacCongTyCon = tongGiaTriHopDongCacCongTyCon != null ? tongGiaTriHopDongCacCongTyCon : BigDecimal.ZERO;
    }

    public BigDecimal getTongGiaTriHopDongNhomCongTy() {
        return tongGiaTriHopDongNhomCongTy != null ? tongGiaTriHopDongNhomCongTy : BigDecimal.ZERO;
    }

    public void setTongGiaTriHopDongNhomCongTy(BigDecimal tongGiaTriHopDongNhomCongTy) {
        this.tongGiaTriHopDongNhomCongTy = tongGiaTriHopDongNhomCongTy != null ? tongGiaTriHopDongNhomCongTy : BigDecimal.ZERO;
    }

    public int getSoLuongCongTyCon() {
        return soLuongCongTyCon;
    }

    public void setSoLuongCongTyCon(int soLuongCongTyCon) {
        this.soLuongCongTyCon = soLuongCongTyCon;
    }

    public int getTongSoHopDong() {
        return tongSoHopDong;
    }

    public void setTongSoHopDong(int tongSoHopDong) {
        this.tongSoHopDong = tongSoHopDong;
    }

    public boolean coCongTyCon() {
        return danhSachCongTyCon != null && !danhSachCongTyCon.isEmpty();
    }

    public static String dinhDangTienTe(BigDecimal soTien) {
        if (soTien == null) {
            return "0 ₫";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("#,##0 ₫", symbols);
        return df.format(soTien);
    }

    public String getTongGiaTriHopDongCongTyMeDinhDang() {
        return dinhDangTienTe(getTongGiaTriHopDongCongTyMe());
    }

    public String getTongGiaTriHopDongCacCongTyConDinhDang() {
        return dinhDangTienTe(getTongGiaTriHopDongCacCongTyCon());
    }

    public String getTongGiaTriHopDongNhomCongTyDinhDang() {
        return dinhDangTienTe(getTongGiaTriHopDongNhomCongTy());
    }
}
