package vn.nhom10.crm.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Model cơ hội bán hàng (bảng `co_hoi`).
 * Phục vụ hiển thị cơ hội đang mở và đã đóng trên trang 360 (Story S3-03).
 */
public class CoHoi implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String maCoHoi;
    private String tenCoHoi;
    private Long khachHangId;
    private Long nguoiLienHeChinhId;
    private Long leadId;
    private Long chienDichId;
    private Long nguonLeadId;
    private Long giaiDoanId;
    private Long nguoiPhuTrachId;
    private Long nhomKinhDoanhId;
    private BigDecimal giaTriDuKien = BigDecimal.ZERO;
    private Integer xacSuat = 0;
    private String lyDoSuaXacSuat;
    private LocalDate ngayChotDuKien;
    private String trangThai = "MO"; // MO, DONG_THANG, DONG_THUA
    private BigDecimal giaTriChotThucTe;
    private LocalDate ngayKy;
    private Long lyDoThangThuaId;
    private Long doiThuId;
    private Timestamp ngayHoatDongCuoi;
    private boolean biDinhTre = false;
    private String moTaChiTiet;
    private LocalDate ngayTao = LocalDate.now();
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Joined / display fields
    private String tenGiaiDoan;
    private String tenNguoiPhuTrach;
    private String tenNguoiLienHeChinh;
    private String tenLyDoThangThua;

    public CoHoi() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaCoHoi() {
        return maCoHoi;
    }

    public void setMaCoHoi(String maCoHoi) {
        this.maCoHoi = maCoHoi;
    }

    public String getTenCoHoi() {
        return tenCoHoi;
    }

    public void setTenCoHoi(String tenCoHoi) {
        this.tenCoHoi = tenCoHoi;
    }

    public Long getKhachHangId() {
        return khachHangId;
    }

    public void setKhachHangId(Long khachHangId) {
        this.khachHangId = khachHangId;
    }

    public Long getNguoiLienHeChinhId() {
        return nguoiLienHeChinhId;
    }

    public void setNguoiLienHeChinhId(Long nguoiLienHeChinhId) {
        this.nguoiLienHeChinhId = nguoiLienHeChinhId;
    }

    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

    public Long getChienDichId() {
        return chienDichId;
    }

    public void setChienDichId(Long chienDichId) {
        this.chienDichId = chienDichId;
    }

    public Long getNguonLeadId() {
        return nguonLeadId;
    }

    public void setNguonLeadId(Long nguonLeadId) {
        this.nguonLeadId = nguonLeadId;
    }

    public Long getGiaiDoanId() {
        return giaiDoanId;
    }

    public void setGiaiDoanId(Long giaiDoanId) {
        this.giaiDoanId = giaiDoanId;
    }

    public Long getNguoiPhuTrachId() {
        return nguoiPhuTrachId;
    }

    public void setNguoiPhuTrachId(Long nguoiPhuTrachId) {
        this.nguoiPhuTrachId = nguoiPhuTrachId;
    }

    public Long getNhomKinhDoanhId() {
        return nhomKinhDoanhId;
    }

    public void setNhomKinhDoanhId(Long nhomKinhDoanhId) {
        this.nhomKinhDoanhId = nhomKinhDoanhId;
    }

    public BigDecimal getGiaTriDuKien() {
        return giaTriDuKien != null ? giaTriDuKien : BigDecimal.ZERO;
    }

    public void setGiaTriDuKien(BigDecimal giaTriDuKien) {
        this.giaTriDuKien = giaTriDuKien != null ? giaTriDuKien : BigDecimal.ZERO;
    }

    public Integer getXacSuat() {
        return xacSuat != null ? xacSuat : 0;
    }

    public void setXacSuat(Integer xacSuat) {
        this.xacSuat = xacSuat != null ? xacSuat : 0;
    }

    public String getLyDoSuaXacSuat() {
        return lyDoSuaXacSuat;
    }

    public void setLyDoSuaXacSuat(String lyDoSuaXacSuat) {
        this.lyDoSuaXacSuat = lyDoSuaXacSuat;
    }

    public LocalDate getNgayChotDuKien() {
        return ngayChotDuKien;
    }

    public void setNgayChotDuKien(LocalDate ngayChotDuKien) {
        this.ngayChotDuKien = ngayChotDuKien;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public BigDecimal getGiaTriChotThucTe() {
        return giaTriChotThucTe;
    }

    public void setGiaTriChotThucTe(BigDecimal giaTriChotThucTe) {
        this.giaTriChotThucTe = giaTriChotThucTe;
    }

    public LocalDate getNgayKy() {
        return ngayKy;
    }

    public void setNgayKy(LocalDate ngayKy) {
        this.ngayKy = ngayKy;
    }

    public Long getLyDoThangThuaId() {
        return lyDoThangThuaId;
    }

    public void setLyDoThangThuaId(Long lyDoThangThuaId) {
        this.lyDoThangThuaId = lyDoThangThuaId;
    }

    public Long getDoiThuId() {
        return doiThuId;
    }

    public void setDoiThuId(Long doiThuId) {
        this.doiThuId = doiThuId;
    }

    public Timestamp getNgayHoatDongCuoi() {
        return ngayHoatDongCuoi;
    }

    public void setNgayHoatDongCuoi(Timestamp ngayHoatDongCuoi) {
        this.ngayHoatDongCuoi = ngayHoatDongCuoi;
    }

    public boolean isBiDinhTre() {
        return biDinhTre;
    }

    public void setBiDinhTre(boolean biDinhTre) {
        this.biDinhTre = biDinhTre;
    }

    public String getMoTaChiTiet() {
        return moTaChiTiet;
    }

    public void setMoTaChiTiet(String moTaChiTiet) {
        this.moTaChiTiet = moTaChiTiet;
    }

    public LocalDate getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDate ngayTao) {
        this.ngayTao = ngayTao;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getTenGiaiDoan() {
        return tenGiaiDoan != null ? tenGiaiDoan : "Đang xử lý";
    }

    public void setTenGiaiDoan(String tenGiaiDoan) {
        this.tenGiaiDoan = tenGiaiDoan;
    }

    public String getTenNguoiPhuTrach() {
        return tenNguoiPhuTrach != null ? tenNguoiPhuTrach : "Chưa phân công";
    }

    public void setTenNguoiPhuTrach(String tenNguoiPhuTrach) {
        this.tenNguoiPhuTrach = tenNguoiPhuTrach;
    }

    public String getTenNguoiLienHeChinh() {
        return tenNguoiLienHeChinh;
    }

    public void setTenNguoiLienHeChinh(String tenNguoiLienHeChinh) {
        this.tenNguoiLienHeChinh = tenNguoiLienHeChinh;
    }

    public String getTenLyDoThangThua() {
        return tenLyDoThangThua != null ? tenLyDoThangThua : "-";
    }

    public void setTenLyDoThangThua(String tenLyDoThangThua) {
        this.tenLyDoThangThua = tenLyDoThangThua;
    }

    public boolean isDangMo() {
        return "MO".equalsIgnoreCase(this.trangThai) ||
               (this.trangThai != null && !this.trangThai.toUpperCase().startsWith("DONG"));
    }

    public boolean isDongThang() {
        return "DONG_THANG".equalsIgnoreCase(this.trangThai);
    }

    public boolean isDongThua() {
        return "DONG_THUA".equalsIgnoreCase(this.trangThai);
    }

    public String getTrangThaiHienThi() {
        if (isDongThang()) {
            return "Đóng Thắng (Đã ký)";
        } else if (isDongThua()) {
            return "Đóng Thua";
        }
        return "Đang mở";
    }

    public String getGiaTriDuKienDinhDang() {
        return dinhDangTien(this.giaTriDuKien);
    }

    public String getGiaTriChotDinhDang() {
        BigDecimal val = this.giaTriChotThucTe != null ? this.giaTriChotThucTe : this.giaTriDuKien;
        return dinhDangTien(val);
    }

    public String getNgayChotDuKienDinhDang() {
        if (ngayChotDuKien != null) {
            return ngayChotDuKien.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        return "-";
    }

    public String getNgayKyDinhDang() {
        if (ngayKy != null) {
            return ngayKy.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        if (ngayTao != null) {
            return ngayTao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        return "-";
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
