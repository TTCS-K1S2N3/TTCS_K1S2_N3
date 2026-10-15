package vn.nhom10.crm.model;

import java.time.LocalDateTime;

/**
 * Model ánh xạ bảng 'lich_su_gop_lead' phục vụ lưu vết lịch sử khi gộp Lead trùng (Story S4-04, AC3).
 */
public class LichSuGopLead {

    private Long id;
    private Long leadNguonId;
    private Long leadDichId;
    private Long thucHienBoiId;
    private String tenNguoiThucHien;
    private String duLieuGopJson;
    private LocalDateTime createdAt;

    public LichSuGopLead() {
    }

    public LichSuGopLead(Long leadNguonId, Long leadDichId, Long thucHienBoiId, String duLieuGopJson) {
        this.leadNguonId = leadNguonId;
        this.leadDichId = leadDichId;
        this.thucHienBoiId = thucHienBoiId;
        this.duLieuGopJson = duLieuGopJson;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLeadNguonId() {
        return leadNguonId;
    }

    public void setLeadNguonId(Long leadNguonId) {
        this.leadNguonId = leadNguonId;
    }

    public Long getLeadDichId() {
        return leadDichId;
    }

    public void setLeadDichId(Long leadDichId) {
        this.leadDichId = leadDichId;
    }

    public Long getThucHienBoiId() {
        return thucHienBoiId;
    }

    public void setThucHienBoiId(Long thucHienBoiId) {
        this.thucHienBoiId = thucHienBoiId;
    }

    public String getTenNguoiThucHien() {
        return tenNguoiThucHien;
    }

    public void setTenNguoiThucHien(String tenNguoiThucHien) {
        this.tenNguoiThucHien = tenNguoiThucHien;
    }

    public String getDuLieuGopJson() {
        return duLieuGopJson;
    }

    public void setDuLieuGopJson(String duLieuGopJson) {
        this.duLieuGopJson = duLieuGopJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
