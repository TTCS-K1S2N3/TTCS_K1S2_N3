package vn.nhom10.crm.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Model hoạt động tương tác khách hàng (bảng `hoat_dong`).
 * Phục vụ dòng thời gian hoạt động (Activity Timeline) trên trang 360 (Story S3-03).
 */
public class HoatDong implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String maHoatDong;
    private String tieuDe;
    private String loaiHoatDong = "CUOC_GOI"; // CUOC_GOI, CUOC_HOP, EMAIL, GHI_CHU
    private Long leadId;
    private Long khachHangId;
    private Long nguoiLienHeId;
    private Long coHoiId;
    private Long nguoiPhuTrachId;
    private Long nhomKinhDoanhId;
    private BigDecimal chiPhi = BigDecimal.ZERO;
    private Timestamp thoiGianBatDau;
    private Timestamp thoiGianKetThuc;
    private String trangThai = "HOAN_THANH";
    private String moTaChiTiet;
    private String noiDung;
    private String ketQua;
    private Integer thoiLuongPhut;
    private boolean laGhiNhanQuaKhu = false;
    private boolean laGhiNhanNhanh = false;
    private LocalDate ngayTao = LocalDate.now();
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Joined / display fields
    private String tenNguoiThucHien;
    private String tenNguoiLienHe;

    public HoatDong() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaHoatDong() {
        return maHoatDong;
    }

    public void setMaHoatDong(String maHoatDong) {
        this.maHoatDong = maHoatDong;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getLoaiHoatDong() {
        return loaiHoatDong;
    }

    public void setLoaiHoatDong(String loaiHoatDong) {
        this.loaiHoatDong = loaiHoatDong;
    }

    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

    public Long getKhachHangId() {
        return khachHangId;
    }

    public void setKhachHangId(Long khachHangId) {
        this.khachHangId = khachHangId;
    }

    public Long getNguoiLienHeId() {
        return nguoiLienHeId;
    }

    public void setNguoiLienHeId(Long nguoiLienHeId) {
        this.nguoiLienHeId = nguoiLienHeId;
    }

    public Long getCoHoiId() {
        return coHoiId;
    }

    public void setCoHoiId(Long coHoiId) {
        this.coHoiId = coHoiId;
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

    public BigDecimal getChiPhi() {
        return chiPhi;
    }

    public void setChiPhi(BigDecimal chiPhi) {
        this.chiPhi = chiPhi;
    }

    public Timestamp getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public void setThoiGianBatDau(Timestamp thoiGianBatDau) {
        this.thoiGianBatDau = thoiGianBatDau;
    }

    public Timestamp getThoiGianKetThuc() {
        return thoiGianKetThuc;
    }

    public void setThoiGianKetThuc(Timestamp thoiGianKetThuc) {
        this.thoiGianKetThuc = thoiGianKetThuc;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getMoTaChiTiet() {
        return moTaChiTiet;
    }

    public void setMoTaChiTiet(String moTaChiTiet) {
        this.moTaChiTiet = moTaChiTiet;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getKetQua() {
        return ketQua;
    }

    public void setKetQua(String ketQua) {
        this.ketQua = ketQua;
    }

    public Integer getThoiLuongPhut() {
        return thoiLuongPhut;
    }

    public void setThoiLuongPhut(Integer thoiLuongPhut) {
        this.thoiLuongPhut = thoiLuongPhut;
    }

    public boolean isLaGhiNhanQuaKhu() {
        return laGhiNhanQuaKhu;
    }

    public void setLaGhiNhanQuaKhu(boolean laGhiNhanQuaKhu) {
        this.laGhiNhanQuaKhu = laGhiNhanQuaKhu;
    }

    public boolean isLaGhiNhanNhanh() {
        return laGhiNhanNhanh;
    }

    public void setLaGhiNhanNhanh(boolean laGhiNhanNhanh) {
        this.laGhiNhanNhanh = laGhiNhanNhanh;
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

    public String getTenNguoiThucHien() {
        return tenNguoiThucHien != null ? tenNguoiThucHien : "Nhân viên kinh doanh";
    }

    public void setTenNguoiThucHien(String tenNguoiThucHien) {
        this.tenNguoiThucHien = tenNguoiThucHien;
    }

    public String getTenNguoiLienHe() {
        return tenNguoiLienHe != null ? tenNguoiLienHe : "Người liên hệ";
    }

    public void setTenNguoiLienHe(String tenNguoiLienHe) {
        this.tenNguoiLienHe = tenNguoiLienHe;
    }

    public String getTenLoaiHienThi() {
        if ("CUOC_GOI".equalsIgnoreCase(loaiHoatDong)) return "Cuộc gọi";
        if ("CUOC_HOP".equalsIgnoreCase(loaiHoatDong)) return "Cuộc họp";
        if ("EMAIL".equalsIgnoreCase(loaiHoatDong)) return "Email";
        if ("GHI_CHU".equalsIgnoreCase(loaiHoatDong)) return "Ghi chú";
        return "Hoạt động";
    }

    public String getIconName() {
        if ("CUOC_GOI".equalsIgnoreCase(loaiHoatDong)) return "call";
        if ("CUOC_HOP".equalsIgnoreCase(loaiHoatDong)) return "groups";
        if ("EMAIL".equalsIgnoreCase(loaiHoatDong)) return "mail";
        if ("GHI_CHU".equalsIgnoreCase(loaiHoatDong)) return "edit_note";
        return "notes";
    }

    public String getBadgeClass() {
        if ("CUOC_GOI".equalsIgnoreCase(loaiHoatDong)) return "badge-call";
        if ("CUOC_HOP".equalsIgnoreCase(loaiHoatDong)) return "badge-meeting";
        if ("EMAIL".equalsIgnoreCase(loaiHoatDong)) return "badge-email";
        if ("GHI_CHU".equalsIgnoreCase(loaiHoatDong)) return "badge-note";
        return "badge-note";
    }

    public String getThoiGianDinhDang() {
        if (thoiGianBatDau != null) {
            return thoiGianBatDau.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        if (createdAt != null) {
            return createdAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        return "";
    }
}
