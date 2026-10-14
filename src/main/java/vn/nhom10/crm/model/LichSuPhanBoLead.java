package vn.nhom10.crm.model;

import java.time.LocalDateTime;

/**
 * Model ánh xạ bảng 'lich_su_phan_bo_lead' (Story S4-06).
 * Ghi lại vết kiểm toán khi một lead được phân bổ tự động, đưa vào hàng chờ, hoặc phân bổ thủ công.
 */
public class LichSuPhanBoLead {

    private Long id;
    private Long leadId;
    private Long quyTacId;
    private Long nguoiDungCuId;
    private Long nguoiDungMoiId;
    private Long nhomId;
    private HanhDongPhanBoEnum hanhDong;
    private String lyDo;
    private Long thucHienBoiId;
    private LocalDateTime createdAt;

    // Joined fields
    private String tenLead;
    private String tenQuyTac;
    private String tenNguoiDungMoi;
    private String tenThucHienBoi;

    public LichSuPhanBoLead() {
    }

    public LichSuPhanBoLead(Long leadId, Long quyTacId, Long nguoiDungMoiId, Long nhomId, HanhDongPhanBoEnum hanhDong, String lyDo, Long thucHienBoiId) {
        this.leadId = leadId;
        this.quyTacId = quyTacId;
        this.nguoiDungMoiId = nguoiDungMoiId;
        this.nhomId = nhomId;
        this.hanhDong = hanhDong;
        this.lyDo = lyDo;
        this.thucHienBoiId = thucHienBoiId;
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

    public HanhDongPhanBoEnum getHanhDong() {
        return hanhDong;
    }

    public void setHanhDong(HanhDongPhanBoEnum hanhDong) {
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

    public String getTenLead() {
        return tenLead;
    }

    public void setTenLead(String tenLead) {
        this.tenLead = tenLead;
    }

    public String getTenQuyTac() {
        return tenQuyTac;
    }

    public void setTenQuyTac(String tenQuyTac) {
        this.tenQuyTac = tenQuyTac;
    }

    public String getTenNguoiDungMoi() {
        return tenNguoiDungMoi;
    }

    public void setTenNguoiDungMoi(String tenNguoiDungMoi) {
        this.tenNguoiDungMoi = tenNguoiDungMoi;
    }

    public String getTenThucHienBoi() {
        return tenThucHienBoi;
    }

    public void setTenThucHienBoi(String tenThucHienBoi) {
        this.tenThucHienBoi = tenThucHienBoi;
    }
}
