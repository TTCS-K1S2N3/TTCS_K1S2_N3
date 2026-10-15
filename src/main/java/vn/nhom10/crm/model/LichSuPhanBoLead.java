package vn.nhom10.crm.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Model ánh xạ bảng `lich_su_phan_bo_lead` (Schema 001).
 * Hợp nhất nghiệp vụ của cả Story S4-06 (Phân bổ Lead tự động, hàng chờ, phân bổ thủ công)
 * và Story S4-07 (Tiếp nhận lead, từ chối lead và kiểm soát SLA).
 */
public class LichSuPhanBoLead implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long leadId;
    private Long quyTacId;
    private Long nguoiDungCuId;
    private Long nguoiDungMoiId;
    private Long nhomId;
    private String hanhDong; // Lưu mã hành động chuẩn trong MySQL VARCHAR(30)
    private String lyDo;
    private Long thucHienBoiId;
    private LocalDateTime createdAt;

    // Joined fields (phục vụ hiển thị chi tiết lịch sử phân bổ)
    private String tenLead;
    private String tenQuyTac;
    private String tenNguoiDungCu;
    private String tenNguoiDungMoi;
    private String tenThucHienBoi;

    public LichSuPhanBoLead() {
    }

    /**
     * Constructor phục vụ Story S4-06 (phân bổ tự động theo quy tắc, hàng chờ, hoặc phân bổ thủ công).
     */
    public LichSuPhanBoLead(Long leadId, Long quyTacId, Long nguoiDungMoiId, Long nhomId, HanhDongPhanBoEnum hanhDong, String lyDo, Long thucHienBoiId) {
        this.leadId = leadId;
        this.quyTacId = quyTacId;
        this.nguoiDungMoiId = nguoiDungMoiId;
        this.nhomId = nhomId;
        this.hanhDong = hanhDong != null ? hanhDong.getMa() : null;
        this.lyDo = lyDo;
        this.thucHienBoiId = thucHienBoiId;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructor phục vụ Story S4-07 (tiếp nhận lead 'NHAN', từ chối lead 'TU_CHOI', chuyển giao).
     */
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

    /**
     * Constructor đầy đủ tất cả các trường.
     */
    public LichSuPhanBoLead(Long leadId, Long quyTacId, Long nguoiDungCuId, Long nguoiDungMoiId, Long nhomId, String hanhDong, String lyDo, Long thucHienBoiId) {
        this.leadId = leadId;
        this.quyTacId = quyTacId;
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

    public void setHanhDong(HanhDongPhanBoEnum hanhDong) {
        this.hanhDong = hanhDong != null ? hanhDong.getMa() : null;
    }

    public HanhDongPhanBoEnum getHanhDongEnum() {
        return HanhDongPhanBoEnum.tuChuoi(this.hanhDong);
    }

    public String getTenHanhDongHienThi() {
        HanhDongPhanBoEnum en = getHanhDongEnum();
        return en != null ? en.getTenHienThi() : this.hanhDong;
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

    public String getTenNguoiDungCu() {
        return tenNguoiDungCu;
    }

    public void setTenNguoiDungCu(String tenNguoiDungCu) {
        this.tenNguoiDungCu = tenNguoiDungCu;
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
