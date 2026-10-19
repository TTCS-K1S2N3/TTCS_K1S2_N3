package vn.nhom10.crm.dto;

import vn.nhom10.crm.model.CoHoi;
import vn.nhom10.crm.model.HoatDong;
import vn.nhom10.crm.model.HopDong;
import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.model.NguoiLienHe;
import vn.nhom10.crm.model.TepDinhKem;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Data Transfer Object tổng hợp toàn bộ bức tranh 360 độ của Khách hàng (Story S3-03).
 * Đáp ứng đầy đủ các Tiêu chí chấp nhận (AC):
 * - AC1: Gom thông tin công ty, danh sách người liên hệ, cơ hội đang mở và đã đóng,
 *        dòng thời gian hoạt động, tệp đính kèm.
 * - AC2: Tổng giá trị đã ký và giá trị cơ hội đang mở.
 * - AC3: Tối ưu dữ liệu tải 500 hoạt động dưới 1.5 giây.
 */
public class KhachHang360DTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 1. Thông tin công ty (AC1)
    private KhachHang khachHang;

    // 2. Danh sách người liên hệ & vai trò quyết định (AC1)
    private List<NguoiLienHe> dsNguoiLienHe = new ArrayList<>();

    // 3. Cơ hội bán hàng: đang mở và đã đóng (AC1)
    private List<CoHoi> dsCoHoiDangMo = new ArrayList<>();
    private List<CoHoi> dsCoHoiDaDong = new ArrayList<>();

    // 4. Dòng thời gian hoạt động (AC1 & AC3)
    private List<HoatDong> dsHoatDong = new ArrayList<>();
    private int tongSoHoatDong = 0;

    // 5. Tệp đính kèm & tài liệu (AC1)
    private List<TepDinhKem> dsTepDinhKem = new ArrayList<>();

    // 6. Hợp đồng pháp lý
    private List<HopDong> dsHopDong = new ArrayList<>();

    // 7. Chỉ số tài chính & KPI (AC2)
    private BigDecimal tongGiaTriDaKy = BigDecimal.ZERO;
    private BigDecimal tongGiaTriCoHoiDangMo = BigDecimal.ZERO;

    public KhachHang360DTO() {
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public List<NguoiLienHe> getDsNguoiLienHe() {
        return dsNguoiLienHe != null ? dsNguoiLienHe : new ArrayList<>();
    }

    public void setDsNguoiLienHe(List<NguoiLienHe> dsNguoiLienHe) {
        this.dsNguoiLienHe = dsNguoiLienHe != null ? dsNguoiLienHe : new ArrayList<>();
    }

    public List<CoHoi> getDsCoHoiDangMo() {
        return dsCoHoiDangMo != null ? dsCoHoiDangMo : new ArrayList<>();
    }

    public void setDsCoHoiDangMo(List<CoHoi> dsCoHoiDangMo) {
        this.dsCoHoiDangMo = dsCoHoiDangMo != null ? dsCoHoiDangMo : new ArrayList<>();
    }

    public List<CoHoi> getDsCoHoiDaDong() {
        return dsCoHoiDaDong != null ? dsCoHoiDaDong : new ArrayList<>();
    }

    public void setDsCoHoiDaDong(List<CoHoi> dsCoHoiDaDong) {
        this.dsCoHoiDaDong = dsCoHoiDaDong != null ? dsCoHoiDaDong : new ArrayList<>();
    }

    public List<HoatDong> getDsHoatDong() {
        return dsHoatDong != null ? dsHoatDong : new ArrayList<>();
    }

    public void setDsHoatDong(List<HoatDong> dsHoatDong) {
        this.dsHoatDong = dsHoatDong != null ? dsHoatDong : new ArrayList<>();
    }

    public int getTongSoHoatDong() {
        return tongSoHoatDong > 0 ? tongSoHoatDong : (dsHoatDong != null ? dsHoatDong.size() : 0);
    }

    public void setTongSoHoatDong(int tongSoHoatDong) {
        this.tongSoHoatDong = tongSoHoatDong;
    }

    public List<TepDinhKem> getDsTepDinhKem() {
        return dsTepDinhKem != null ? dsTepDinhKem : new ArrayList<>();
    }

    public void setDsTepDinhKem(List<TepDinhKem> dsTepDinhKem) {
        this.dsTepDinhKem = dsTepDinhKem != null ? dsTepDinhKem : new ArrayList<>();
    }

    public List<HopDong> getDsHopDong() {
        return dsHopDong != null ? dsHopDong : new ArrayList<>();
    }

    public void setDsHopDong(List<HopDong> dsHopDong) {
        this.dsHopDong = dsHopDong != null ? dsHopDong : new ArrayList<>();
    }

    public BigDecimal getTongGiaTriDaKy() {
        return tongGiaTriDaKy != null ? tongGiaTriDaKy : BigDecimal.ZERO;
    }

    public void setTongGiaTriDaKy(BigDecimal tongGiaTriDaKy) {
        this.tongGiaTriDaKy = tongGiaTriDaKy != null ? tongGiaTriDaKy : BigDecimal.ZERO;
    }

    public BigDecimal getTongGiaTriCoHoiDangMo() {
        return tongGiaTriCoHoiDangMo != null ? tongGiaTriCoHoiDangMo : BigDecimal.ZERO;
    }

    public void setTongGiaTriCoHoiDangMo(BigDecimal tongGiaTriCoHoiDangMo) {
        this.tongGiaTriCoHoiDangMo = tongGiaTriCoHoiDangMo != null ? tongGiaTriCoHoiDangMo : BigDecimal.ZERO;
    }

    public int getSoCoHoiDangMo() {
        return dsCoHoiDangMo != null ? dsCoHoiDangMo.size() : 0;
    }

    public int getSoCoHoiDaDong() {
        return dsCoHoiDaDong != null ? dsCoHoiDaDong.size() : 0;
    }

    public int getTongSoNguoiLienHe() {
        return dsNguoiLienHe != null ? dsNguoiLienHe.size() : 0;
    }

    public int getTongSoTepDinhKem() {
        return dsTepDinhKem != null ? dsTepDinhKem.size() : 0;
    }

    public int getSoHopDongDaKy() {
        return dsHopDong != null ? dsHopDong.size() : 0;
    }

    public String getTongGiaTriDaKyDinhDang() {
        return dinhDangTien(this.tongGiaTriDaKy);
    }

    public String getTongGiaTriCoHoiDangMoDinhDang() {
        return dinhDangTien(this.tongGiaTriCoHoiDangMo);
    }

    private static String dinhDangTien(BigDecimal tien) {
        if (tien == null) {
            return "0 đ";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        DecimalFormat df = new DecimalFormat("#,###", symbols);
        return df.format(tien) + " đ";
    }
}
