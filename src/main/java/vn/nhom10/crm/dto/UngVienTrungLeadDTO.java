package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO đại diện cho một ứng viên trùng lặp (Lead hoặc Khách hàng) theo Contract Story S4-04 (AC1, AC2).
 */
public class UngVienTrungLeadDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String candidateKind; // "LEAD" hoặc "CUSTOMER"
    private Long recordId;
    private String maBanGhi;
    private String displayName;
    private String email;
    private String soDienThoai;
    private String tenCongTy;
    private String trangThai;
    private Long nguoiPhuTrachId;
    private String tenNguoiPhuTrach;
    private Long nhomPhanBoId;
    private List<String> matchReasons = new ArrayList<>(); // "EMAIL", "PHONE", "COMPANY"
    private String maskedLabel;
    private String maskedContact;
    private boolean canViewDetail = true;
    private LocalDateTime createdAt;

    public UngVienTrungLeadDTO() {
    }

    public UngVienTrungLeadDTO(String candidateKind, Long recordId, String maBanGhi, String displayName) {
        this.candidateKind = candidateKind;
        this.recordId = recordId;
        this.maBanGhi = maBanGhi;
        this.displayName = displayName;
    }

    public String getCandidateKind() {
        return candidateKind;
    }

    public void setCandidateKind(String candidateKind) {
        this.candidateKind = candidateKind;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public String getMaBanGhi() {
        return maBanGhi;
    }

    public void setMaBanGhi(String maBanGhi) {
        this.maBanGhi = maBanGhi;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getTenCongTy() {
        return tenCongTy;
    }

    public void setTenCongTy(String tenCongTy) {
        this.tenCongTy = tenCongTy;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Long getNguoiPhuTrachId() {
        return nguoiPhuTrachId;
    }

    public void setNguoiPhuTrachId(Long nguoiPhuTrachId) {
        this.nguoiPhuTrachId = nguoiPhuTrachId;
    }

    public String getTenNguoiPhuTrach() {
        return tenNguoiPhuTrach;
    }

    public void setTenNguoiPhuTrach(String tenNguoiPhuTrach) {
        this.tenNguoiPhuTrach = tenNguoiPhuTrach;
    }

    public Long getNhomPhanBoId() {
        return nhomPhanBoId;
    }

    public void setNhomPhanBoId(Long nhomPhanBoId) {
        this.nhomPhanBoId = nhomPhanBoId;
    }

    public List<String> getMatchReasons() {
        return matchReasons;
    }

    public void setMatchReasons(List<String> matchReasons) {
        this.matchReasons = matchReasons != null ? matchReasons : new ArrayList<>();
    }

    public void themMatchReason(String reason) {
        if (reason != null && !matchReasons.contains(reason)) {
            matchReasons.add(reason);
        }
    }

    public String getMaskedLabel() {
        return maskedLabel;
    }

    public void setMaskedLabel(String maskedLabel) {
        this.maskedLabel = maskedLabel;
    }

    public String getMaskedContact() {
        return maskedContact;
    }

    public void setMaskedContact(String maskedContact) {
        this.maskedContact = maskedContact;
    }

    public boolean isCanViewDetail() {
        return canViewDetail;
    }

    public void setCanViewDetail(boolean canViewDetail) {
        this.canViewDetail = canViewDetail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
