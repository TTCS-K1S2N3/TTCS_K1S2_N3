package vn.nhom10.crm.model;

import java.time.LocalDateTime;

/**
 * Model ánh xạ bảng 'quy_tac_phan_bo_lead' (Story S4-06).
 * Quản lý cấu hình quy tắc phân bổ lead tự động của Giám đốc kinh doanh.
 */
public class QuyTacPhanBoLead {

    private Long id;
    private String maQuyTac;
    private String tenQuyTac;
    private int thuTuUuTien;
    private KieuPhanBoEnum kieuPhanBo;
    private String dieuKienJson;
    private Long nhomDichId;
    private Long nguoiDungDichId;
    private Long nguoiDungLanCuoiId;
    private boolean hoatDong = true;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Các trường hiển thị bổ sung (Joined)
    private String tenNhomDich;
    private String tenNguoiDungDich;
    private String tenNguoiDungLanCuoi;

    public QuyTacPhanBoLead() {
    }

    public QuyTacPhanBoLead(String maQuyTac, String tenQuyTac, int thuTuUuTien, KieuPhanBoEnum kieuPhanBo, String dieuKienJson, Long nhomDichId, Long nguoiDungDichId) {
        this.maQuyTac = maQuyTac;
        this.tenQuyTac = tenQuyTac;
        this.thuTuUuTien = thuTuUuTien;
        this.kieuPhanBo = kieuPhanBo;
        this.dieuKienJson = dieuKienJson;
        this.nhomDichId = nhomDichId;
        this.nguoiDungDichId = nguoiDungDichId;
        this.hoatDong = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaQuyTac() {
        return maQuyTac;
    }

    public void setMaQuyTac(String maQuyTac) {
        this.maQuyTac = maQuyTac;
    }

    public String getTenQuyTac() {
        return tenQuyTac;
    }

    public void setTenQuyTac(String tenQuyTac) {
        this.tenQuyTac = tenQuyTac;
    }

    public int getThuTuUuTien() {
        return thuTuUuTien;
    }

    public void setThuTuUuTien(int thuTuUuTien) {
        this.thuTuUuTien = thuTuUuTien;
    }

    public KieuPhanBoEnum getKieuPhanBo() {
        return kieuPhanBo;
    }

    public void setKieuPhanBo(KieuPhanBoEnum kieuPhanBo) {
        this.kieuPhanBo = kieuPhanBo;
    }

    public String getDieuKienJson() {
        return dieuKienJson;
    }

    public void setDieuKienJson(String dieuKienJson) {
        this.dieuKienJson = dieuKienJson;
    }

    public Long getNhomDichId() {
        return nhomDichId;
    }

    public void setNhomDichId(Long nhomDichId) {
        this.nhomDichId = nhomDichId;
    }

    public Long getNguoiDungDichId() {
        return nguoiDungDichId;
    }

    public void setNguoiDungDichId(Long nguoiDungDichId) {
        this.nguoiDungDichId = nguoiDungDichId;
    }

    public Long getNguoiDungLanCuoiId() {
        return nguoiDungLanCuoiId;
    }

    public void setNguoiDungLanCuoiId(Long nguoiDungLanCuoiId) {
        this.nguoiDungLanCuoiId = nguoiDungLanCuoiId;
    }

    public boolean isHoatDong() {
        return hoatDong;
    }

    public void setHoatDong(boolean hoatDong) {
        this.hoatDong = hoatDong;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
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

    public String getTenNhomDich() {
        return tenNhomDich;
    }

    public void setTenNhomDich(String tenNhomDich) {
        this.tenNhomDich = tenNhomDich;
    }

    public String getTenNguoiDungDich() {
        return tenNguoiDungDich;
    }

    public void setTenNguoiDungDich(String tenNguoiDungDich) {
        this.tenNguoiDungDich = tenNguoiDungDich;
    }

    public String getTenNguoiDungLanCuoi() {
        return tenNguoiDungLanCuoi;
    }

    public void setTenNguoiDungLanCuoi(String tenNguoiDungLanCuoi) {
        this.tenNguoiDungLanCuoi = tenNguoiDungLanCuoi;
    }
}
