package vn.nhom10.crm.model;

import java.time.LocalDateTime;

/**
 * Model ánh xạ bảng 'nhat_ky_gui_form_lead' phục vụ kiểm tra chống spam
 * và giới hạn tần suất theo địa chỉ IP (AC3 của Story S4-01).
 */
public class NhatKyGuiFormLead {

    private Long id;
    private Long bieuMauId;
    private String diaChiIp;
    private boolean thanhCong;
    private String lyDoTuChoi;
    private LocalDateTime createdAt;

    public NhatKyGuiFormLead() {
    }

    public NhatKyGuiFormLead(Long bieuMauId, String diaChiIp, boolean thanhCong, String lyDoTuChoi) {
        this.bieuMauId = bieuMauId;
        this.diaChiIp = diaChiIp;
        this.thanhCong = thanhCong;
        this.lyDoTuChoi = lyDoTuChoi;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBieuMauId() {
        return bieuMauId;
    }

    public void setBieuMauId(Long bieuMauId) {
        this.bieuMauId = bieuMauId;
    }

    public String getDiaChiIp() {
        return diaChiIp;
    }

    public void setDiaChiIp(String diaChiIp) {
        this.diaChiIp = diaChiIp;
    }

    public boolean isThanhCong() {
        return thanhCong;
    }

    public void setThanhCong(boolean thanhCong) {
        this.thanhCong = thanhCong;
    }

    public String getLyDoTuChoi() {
        return lyDoTuChoi;
    }

    public void setLyDoTuChoi(String lyDoTuChoi) {
        this.lyDoTuChoi = lyDoTuChoi;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
