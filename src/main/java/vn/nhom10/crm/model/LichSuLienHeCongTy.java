package vn.nhom10.crm.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model biểu diễn lịch sử công ty của Người liên hệ (bảng lich_su_lien_he_cong_ty).
 * Phục vụ Acceptance Criteria AC4:
 * "Một người liên hệ chuyển sang công ty khác thì gắn lại được sang khách hàng mới, giữ nguyên lịch sử"
 */
public class LichSuLienHeCongTy implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long nguoiLienHeId;
    private Long khachHangId;
    private String chucDanh;
    private VaiTroQuyetDinhEnum vaiTroQuyetDinh;
    private LocalDate tuNgay;
    private LocalDate denNgay;
    private String ghiChu;
    private LocalDateTime createdAt;

    // Các trường join thông tin khách hàng/công ty
    private String tenCongTy;
    private String maKhachHang;
    private String maSoThue;

    public LichSuLienHeCongTy() {
    }

    public LichSuLienHeCongTy(Long id, Long nguoiLienHeId, Long khachHangId, String chucDanh,
                              VaiTroQuyetDinhEnum vaiTroQuyetDinh, LocalDate tuNgay, LocalDate denNgay,
                              String ghiChu, LocalDateTime createdAt) {
        this.id = id;
        this.nguoiLienHeId = nguoiLienHeId;
        this.khachHangId = khachHangId;
        this.chucDanh = chucDanh;
        this.vaiTroQuyetDinh = vaiTroQuyetDinh;
        this.tuNgay = tuNgay;
        this.denNgay = denNgay;
        this.ghiChu = ghiChu;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNguoiLienHeId() {
        return nguoiLienHeId;
    }

    public void setNguoiLienHeId(Long nguoiLienHeId) {
        this.nguoiLienHeId = nguoiLienHeId;
    }

    public Long getKhachHangId() {
        return khachHangId;
    }

    public void setKhachHangId(Long khachHangId) {
        this.khachHangId = khachHangId;
    }

    public String getChucDanh() {
        return chucDanh;
    }

    public void setChucDanh(String chucDanh) {
        this.chucDanh = chucDanh;
    }

    public VaiTroQuyetDinhEnum getVaiTroQuyetDinh() {
        return vaiTroQuyetDinh;
    }

    public void setVaiTroQuyetDinh(VaiTroQuyetDinhEnum vaiTroQuyetDinh) {
        this.vaiTroQuyetDinh = vaiTroQuyetDinh;
    }

    public LocalDate getTuNgay() {
        return tuNgay;
    }

    public void setTuNgay(LocalDate tuNgay) {
        this.tuNgay = tuNgay;
    }

    public LocalDate getDenNgay() {
        return denNgay;
    }

    public void setDenNgay(LocalDate denNgay) {
        this.denNgay = denNgay;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getTenCongTy() {
        return tenCongTy;
    }

    public void setTenCongTy(String tenCongTy) {
        this.tenCongTy = tenCongTy;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getMaSoThue() {
        return maSoThue;
    }

    public void setMaSoThue(String maSoThue) {
        this.maSoThue = maSoThue;
    }

    public String getTenVaiTroHienThi() {
        return vaiTroQuyetDinh != null ? vaiTroQuyetDinh.getTenHienThi() : "Chưa xác định";
    }

    public String getBadgeClassVaiTro() {
        return vaiTroQuyetDinh != null ? vaiTroQuyetDinh.getBadgeClass() : "badge-secondary";
    }

    public boolean isHienTai() {
        return denNgay == null;
    }
}
