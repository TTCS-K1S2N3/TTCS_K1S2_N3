package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * DTO đại diện cho khách hàng trong danh sách chăm sóc định kỳ (Story S3-09).
 * Đáp ứng các tiêu chí:
 * • AC1: Khách chưa có tương tác nào trong N ngày
 * • AC2: Sắp xếp theo giá trị hợp đồng giảm dần
 * • AC3: Đánh dấu đã liên hệ ngay trên danh sách
 */
public class KhachHangChamSocDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String maKhachHang;
    private String tenCongTy;
    private Long nguoiPhuTrachId;
    private String tenNguoiPhuTrach;
    private Long nhomKinhDoanhId;
    private String tenNhom;
    private String soHopDong;
    private BigDecimal tongGiaTriHopDong;
    private Timestamp lanTuongTacCuoi;
    private int soNgayChuaTuongTac;
    private boolean daLienHeHomNay;
    private String trangThai;
    private String ngayTao;
    private String moTaChiTiet;

    public KhachHangChamSocDTO() {
        this.tongGiaTriHopDong = BigDecimal.ZERO;
    }

    public KhachHangChamSocDTO(Long id, String maKhachHang, String tenCongTy,
                               Long nguoiPhuTrachId, String tenNguoiPhuTrach,
                               Long nhomKinhDoanhId, String tenNhom,
                               String soHopDong, BigDecimal tongGiaTriHopDong,
                               Timestamp lanTuongTacCuoi, int soNgayChuaTuongTac,
                               boolean daLienHeHomNay, String trangThai,
                               String ngayTao, String moTaChiTiet) {
        this.id = id;
        this.maKhachHang = maKhachHang;
        this.tenCongTy = tenCongTy;
        this.nguoiPhuTrachId = nguoiPhuTrachId;
        this.tenNguoiPhuTrach = tenNguoiPhuTrach;
        this.nhomKinhDoanhId = nhomKinhDoanhId;
        this.tenNhom = tenNhom;
        this.soHopDong = soHopDong;
        this.tongGiaTriHopDong = tongGiaTriHopDong != null ? tongGiaTriHopDong : BigDecimal.ZERO;
        this.lanTuongTacCuoi = lanTuongTacCuoi;
        this.soNgayChuaTuongTac = soNgayChuaTuongTac;
        this.daLienHeHomNay = daLienHeHomNay;
        this.trangThai = trangThai;
        this.ngayTao = ngayTao;
        this.moTaChiTiet = moTaChiTiet;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getTenCongTy() {
        return tenCongTy;
    }

    public void setTenCongTy(String tenCongTy) {
        this.tenCongTy = tenCongTy;
    }

    public Long getNguoiPhuTrachId() {
        return nguoiPhuTrachId;
    }

    public void setNguoiPhuTrachId(Long nguoiPhuTrachId) {
        this.nguoiPhuTrachId = nguoiPhuTrachId;
    }

    public String getTenNguoiPhuTrach() {
        return tenNguoiPhuTrach != null && !tenNguoiPhuTrach.isBlank() ? tenNguoiPhuTrach : "Chưa phân công";
    }

    public void setTenNguoiPhuTrach(String tenNguoiPhuTrach) {
        this.tenNguoiPhuTrach = tenNguoiPhuTrach;
    }

    public Long getNhomKinhDoanhId() {
        return nhomKinhDoanhId;
    }

    public void setNhomKinhDoanhId(Long nhomKinhDoanhId) {
        this.nhomKinhDoanhId = nhomKinhDoanhId;
    }

    public String getTenNhom() {
        return tenNhom != null && !tenNhom.isBlank() ? tenNhom : "Chưa phân nhóm";
    }

    public void setTenNhom(String tenNhom) {
        this.tenNhom = tenNhom;
    }

    public String getSoHopDong() {
        return soHopDong != null && !soHopDong.isBlank() ? soHopDong : "HĐ-CHUA-KY";
    }

    public void setSoHopDong(String soHopDong) {
        this.soHopDong = soHopDong;
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

    public Timestamp getLanTuongTacCuoi() {
        return lanTuongTacCuoi;
    }

    public void setLanTuongTacCuoi(Timestamp lanTuongTacCuoi) {
        this.lanTuongTacCuoi = lanTuongTacCuoi;
    }

    public String getLanTuongTacCuoiDinhDang() {
        if (lanTuongTacCuoi == null) {
            return "Chưa có tương tác";
        }
        return lanTuongTacCuoi.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public int getSoNgayChuaTuongTac() {
        return soNgayChuaTuongTac;
    }

    public void setSoNgayChuaTuongTac(int soNgayChuaTuongTac) {
        this.soNgayChuaTuongTac = soNgayChuaTuongTac;
    }

    public boolean isDaLienHeHomNay() {
        return daLienHeHomNay;
    }

    public void setDaLienHeHomNay(boolean daLienHeHomNay) {
        this.daLienHeHomNay = daLienHeHomNay;
    }

    public String getTrangThai() {
        return trangThai != null ? trangThai : "TIEM_NANG";
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getNgayTao() {
        return ngayTao != null ? ngayTao : "";
    }

    public void setNgayTao(String ngayTao) {
        this.ngayTao = ngayTao;
    }

    public String getMoTaChiTiet() {
        return moTaChiTiet != null ? moTaChiTiet : "";
    }

    public void setMoTaChiTiet(String moTaChiTiet) {
        this.moTaChiTiet = moTaChiTiet;
    }

    // --- Tương thích với các thuộc tính của BanGhiNghiepVuDTO trên JSP cũ ---

    public String getMaBanGhi() {
        return getMaKhachHang();
    }

    public String getTieuDe() {
        return getTenCongTy();
    }

    public String getGiaTri() {
        return getTongGiaTriHopDongDinhDang();
    }
}
