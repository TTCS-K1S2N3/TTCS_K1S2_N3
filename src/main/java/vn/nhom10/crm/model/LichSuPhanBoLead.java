package vn.nhom10.crm.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Model ánh xạ bảng `lich_su_phan_bo_lead` (Schema 001).
 * Ghi nhận lịch sử phân bổ, chuyển giao, nhận lead và từ chối lead (Story S4-07).
 */
public class LichSuPhanBoLead implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long leadId;
    private Long quyTacId;
    private Long nguoiDungCuId;
    private Long nguoiDungMoiId;
    private Long nhomId;
    private String hanhDong; // PHAN_BO, CHUYEN_GIAO, NHAN, TU_CHOI
    private String lyDo;
    private Long thucHienBoiId;
    private LocalDateTime createdAt;

    public LichSuPhanBoLead() {
    }

    public LichSuPhanBoLead(Long leadId, Long nguoiDungCuId, Long nguoiDungMoiId, Long nhomId, String hanhDong, String lyDo, Long thucHienBoiId) {
        this.leadId = leadId;
        this.nguoiDungCuId = nguoiDungCuId;
        this.nguoiDungMoiId = nguoiDungMoiId;
        this.nhomId = nhomId;
        this.hanhDong = hanhDong;
        this.lyDo = lyDo;
        this.thucHienBoiId = thucHienBoiId;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

    public Long getQuyTacId() {
        return quyTacId;
    }

    public void setQuyTacId(Long quyTacId) {
        this.quyTacId = quyTacId;
    }

    public Long getNguoiDungCuId() {
        return nguoiDungCuId;
    }

    public void setNguoiDungCuId(Long nguoiDungCuId) {
        this.nguoiDungCuId = nguoiDungCuId;
    }

    public Long getNguoiDungMoiId() {
        return nguoiDungMoiId;
    }

    public void setNguoiDungMoiId(Long nguoiDungMoiId) {
        this.nguoiDungMoiId = nguoiDungMoiId;
    }

    public Long getNhomId() {
        return nhomId;
    }

    public void setNhomId(Long nhomId) {
        this.nhomId = nhomId;
    }

    public String getHanhDong() {
        return hanhDong;
    }

    public void setHanhDong(String hanhDong) {
        this.hanhDong = hanhDong;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public Long getThucHienBoiId() {
        return thucHienBoiId;
    }

    public void setThucHienBoiId(Long thucHienBoiId) {
        this.thucHienBoiId = thucHienBoiId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
