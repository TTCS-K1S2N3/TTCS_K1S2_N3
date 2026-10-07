package vn.nhom10.crm.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Model đại diện cho thực thể Hợp đồng bán hàng trong bảng 'hop_dong'.
 * Phục vụ tính toán tổng giá trị hợp đồng của khách hàng và nhóm công ty (Story S3-05).
 */
public class HopDong {

    private Long id;
    private String soHopDong;
    private Long baoGiaId;
    private Long phienBanBaoGiaId;
    private Long coHoiId;
    private Long khachHangId;
    private LocalDate ngayKy;
    private LocalDate ngayHieuLuc;
    private LocalDate ngayHetHan;
    private BigDecimal giaTriHopDong = BigDecimal.ZERO;
    private String dieuKhoanThanhToan;
    private String trangThai = "DA_KY";
    private Long hopDongGocId;
    private Long coHoiGiaHanId;
    private Long createdBy;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Thuộc tính bổ trợ
    private String tenKhachHang;
    private String maKhachHang;

    public HopDong() {
    }

    public HopDong(Long id, String soHopDong, Long khachHangId, BigDecimal giaTriHopDong, String trangThai) {
        this.id = id;
        this.soHopDong = soHopDong;
        this.khachHangId = khachHangId;
        this.giaTriHopDong = giaTriHopDong != null ? giaTriHopDong : BigDecimal.ZERO;
        this.trangThai = trangThai;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSoHopDong() {
        return soHopDong;
    }

    public void setSoHopDong(String soHopDong) {
        this.soHopDong = soHopDong;
    }

    public Long getBaoGiaId() {
        return baoGiaId;
    }

    public void setBaoGiaId(Long baoGiaId) {
        this.baoGiaId = baoGiaId;
    }

    public Long getPhienBanBaoGiaId() {
        return phienBanBaoGiaId;
    }

    public void setPhienBanBaoGiaId(Long phienBanBaoGiaId) {
        this.phienBanBaoGiaId = phienBanBaoGiaId;
    }

    public Long getCoHoiId() {
        return coHoiId;
    }

    public void setCoHoiId(Long coHoiId) {
        this.coHoiId = coHoiId;
    }

    public Long getKhachHangId() {
        return khachHangId;
    }

    public void setKhachHangId(Long khachHangId) {
        this.khachHangId = khachHangId;
    }

    public LocalDate getNgayKy() {
        return ngayKy;
    }

    public void setNgayKy(LocalDate ngayKy) {
        this.ngayKy = ngayKy;
    }

    public LocalDate getNgayHieuLuc() {
        return ngayHieuLuc;
    }

    public void setNgayHieuLuc(LocalDate ngayHieuLuc) {
        this.ngayHieuLuc = ngayHieuLuc;
    }

    public LocalDate getNgayHetHan() {
        return ngayHetHan;
    }

    public void setNgayHetHan(LocalDate ngayHetHan) {
        this.ngayHetHan = ngayHetHan;
    }

    public BigDecimal getGiaTriHopDong() {
        return giaTriHopDong != null ? giaTriHopDong : BigDecimal.ZERO;
    }

    public void setGiaTriHopDong(BigDecimal giaTriHopDong) {
        this.giaTriHopDong = giaTriHopDong != null ? giaTriHopDong : BigDecimal.ZERO;
    }

    public String getDieuKhoanThanhToan() {
        return dieuKhoanThanhToan;
    }

    public void setDieuKhoanThanhToan(String dieuKhoanThanhToan) {
        this.dieuKhoanThanhToan = dieuKhoanThanhToan;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Long getHopDongGocId() {
        return hopDongGocId;
    }

    public void setHopDongGocId(Long hopDongGocId) {
        this.hopDongGocId = hopDongGocId;
    }

    public Long getCoHoiGiaHanId() {
        return coHoiGiaHanId;
    }

    public void setCoHoiGiaHanId(Long coHoiGiaHanId) {
        this.coHoiGiaHanId = coHoiGiaHanId;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
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

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getNgayKyDinhDang() {
        if (ngayKy != null) {
            return ngayKy.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        return "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HopDong hopDong = (HopDong) o;
        return Objects.equals(id, hopDong.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "HopDong{" +
                "id=" + id +
                ", soHopDong='" + soHopDong + '\'' +
                ", khachHangId=" + khachHangId +
                ", giaTriHopDong=" + giaTriHopDong +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
