package vn.nhom10.crm.dto;

import java.time.LocalDateTime;

/**
 * DTO đại diện cho một lead trong hàng chờ phân bổ (Story S4-06, AC3).
 */
public class LeadHangChoDTO {

    private Long id;
    private String maLead;
    private String hoTen;
    private String email;
    private String soDienThoai;
    private String tenCongTy;
    private String nhuCauQuanTam;
    private Long khuVucId;
    private String tenKhuVuc;
    private Long nganhNgheId;
    private String tenNganhNghe;
    private Long nguonLeadId;
    private String tenNguonLead;
    private int diem;
    private String phanLoai;
    private LocalDateTime createdAt;
    private String thoiGianChoFormatted;

    public LeadHangChoDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaLead() {
        return maLead;
    }

    public void setMaLead(String maLead) {
        this.maLead = maLead;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
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

    public String getNhuCauQuanTam() {
        return nhuCauQuanTam;
    }

    public void setNhuCauQuanTam(String nhuCauQuanTam) {
        this.nhuCauQuanTam = nhuCauQuanTam;
    }

    public Long getKhuVucId() {
        return khuVucId;
    }

    public void setKhuVucId(Long khuVucId) {
        this.khuVucId = khuVucId;
    }

    public String getTenKhuVuc() {
        return tenKhuVuc;
    }

    public void setTenKhuVuc(String tenKhuVuc) {
        this.tenKhuVuc = tenKhuVuc;
    }

    public Long getNganhNgheId() {
        return nganhNgheId;
    }

    public void setNganhNgheId(Long nganhNgheId) {
        this.nganhNgheId = nganhNgheId;
    }

    public String getTenNganhNghe() {
        return tenNganhNghe;
    }

    public void setTenNganhNghe(String tenNganhNghe) {
        this.tenNganhNghe = tenNganhNghe;
    }

    public Long getNguonLeadId() {
        return nguonLeadId;
    }

    public void setNguonLeadId(Long nguonLeadId) {
        this.nguonLeadId = nguonLeadId;
    }

    public String getTenNguonLead() {
        return tenNguonLead;
    }

    public void setTenNguonLead(String tenNguonLead) {
        this.tenNguonLead = tenNguonLead;
    }

    public int getDiem() {
        return diem;
    }

    public void setDiem(int diem) {
        this.diem = diem;
    }

    public String getPhanLoai() {
        return phanLoai;
    }

    public void setPhanLoai(String phanLoai) {
        this.phanLoai = phanLoai;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getThoiGianChoFormatted() {
        return thoiGianChoFormatted;
    }

    public void setThoiGianChoFormatted(String thoiGianChoFormatted) {
        this.thoiGianChoFormatted = thoiGianChoFormatted;
    }
}
