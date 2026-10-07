package vn.nhom10.crm.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * Model hợp đồng (bảng `hop_dong`).
 * Phục vụ tính toán tổng giá trị đã ký trên trang 360 (Story S3-03, AC2).
 */
public class HopDong implements Serializable {

    private static final long serialVersionUID = 1L;

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
    private String trangThai = "DA_KY"; // DA_KY, DANG_HIEU_LUC, HET_HAN, DA_HUY
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public HopDong() {
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
}
