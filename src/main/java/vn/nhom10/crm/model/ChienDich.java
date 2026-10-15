package vn.nhom10.crm.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model ánh xạ bảng 'chien_dich' phục vụ quản lý Chiến dịch Marketing (Epic EP-04, Story S4-03).
 * AC1: Khai báo chiến dịch với ngân sách, thời gian chạy, kênh.
 * AC2: Lead và cơ hội giữ liên kết tới chiến dịch đã sinh ra chúng.
 * AC3: Xem được số lead, số cơ hội và giá trị đã chốt của từng chiến dịch.
 */
public class ChienDich implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String maChienDich;
    private String tenChienDich;
    private BigDecimal nganSach = BigDecimal.ZERO;
    private String kenh;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private String trangThai = "NHAP"; // NHAP, DANG_CHAY, KET_THUC, TAM_DUNG
    private String moTa;
    private Long createdBy;
    private String tenNguoiTao;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ChienDich() {
    }

    public ChienDich(String maChienDich, String tenChienDich, BigDecimal nganSach, String kenh, LocalDate ngayBatDau, LocalDate ngayKetThuc) {
        this.maChienDich = maChienDich;
        this.tenChienDich = tenChienDich;
        this.nganSach = nganSach != null ? nganSach : BigDecimal.ZERO;
        this.kenh = kenh;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.trangThai = "DANG_CHAY";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaChienDich() {
        return maChienDich;
    }

    public void setMaChienDich(String maChienDich) {
        this.maChienDich = maChienDich;
    }

    public String getTenChienDich() {
        return tenChienDich;
    }

    public void setTenChienDich(String tenChienDich) {
        this.tenChienDich = tenChienDich;
    }

    public BigDecimal getNganSach() {
        return nganSach;
    }

    public void setNganSach(BigDecimal nganSach) {
        this.nganSach = nganSach;
    }

    public String getKenh() {
        return kenh;
    }

    public void setKenh(String kenh) {
        this.kenh = kenh;
    }

    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDate getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getTenNguoiTao() {
        return tenNguoiTao;
    }

    public void setTenNguoiTao(String tenNguoiTao) {
        this.tenNguoiTao = tenNguoiTao;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
