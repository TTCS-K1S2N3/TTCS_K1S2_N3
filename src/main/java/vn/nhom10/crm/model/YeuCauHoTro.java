package vn.nhom10.crm.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Model ánh xạ bảng `yeu_cau_ho_tro` (Story S3-08).
 * Quản lý yêu cầu hỗ trợ sau bán, mức độ ưu tiên, người xử lý và trạng thái.
 */
public class YeuCauHoTro implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String maYeuCau;
    private Long khachHangId;
    private Long nguoiLienHeId;
    private Long nguoiXuLyId;
    private String tieuDe;
    private String noiDung;
    private String mucUuTien; // THAP, BINH_THUONG, CAO, KHAN_CAP
    private String trangThai;  // MOI, DANG_XU_LY, CHO_KHACH_HANG, DA_XU_LY, DONG, HUY
    private LocalDateTime taoLuc;
    private LocalDateTime xuLyLuc;
    private LocalDateTime hoanTatLuc;

    // Các trường hiển thị bổ trợ (JOIN từ bảng khach_hang, nguoi_lien_he, nguoi_dung)
    private String tenKhachHang;
    private String maKhachHang;
    private String tenNguoiLienHe;
    private String sdtNguoiLienHe;
    private String tenNguoiXuLy;
    private String emailNguoiXuLy;

    public YeuCauHoTro() {
        this.mucUuTien = MucUuTienYeuCauEnum.BINH_THUONG.getMa();
        this.trangThai = TrangThaiYeuCauEnum.MOI.getMa();
        this.taoLuc = LocalDateTime.now();
    }

    public YeuCauHoTro(Long khachHangId, String tieuDe, String noiDung, String mucUuTien, Long nguoiXuLyId) {
        this();
        this.khachHangId = khachHangId;
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        if (mucUuTien != null && !mucUuTien.isBlank()) {
            this.mucUuTien = mucUuTien;
        }
        this.nguoiXuLyId = nguoiXuLyId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaYeuCau() {
        return maYeuCau;
    }

    public void setMaYeuCau(String maYeuCau) {
        this.maYeuCau = maYeuCau;
    }

    public Long getKhachHangId() {
        return khachHangId;
    }

    public void setKhachHangId(Long khachHangId) {
        this.khachHangId = khachHangId;
    }

    public Long getNguoiLienHeId() {
        return nguoiLienHeId;
    }

    public void setNguoiLienHeId(Long nguoiLienHeId) {
        this.nguoiLienHeId = nguoiLienHeId;
    }

    public Long getNguoiXuLyId() {
        return nguoiXuLyId;
    }

    public void setNguoiXuLyId(Long nguoiXuLyId) {
        this.nguoiXuLyId = nguoiXuLyId;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getMucUuTien() {
        return mucUuTien;
    }

    public void setMucUuTien(String mucUuTien) {
        this.mucUuTien = mucUuTien;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getTaoLuc() {
        return taoLuc;
    }

    public void setTaoLuc(LocalDateTime taoLuc) {
        this.taoLuc = taoLuc;
    }

    public LocalDateTime getXuLyLuc() {
        return xuLyLuc;
    }

    public void setXuLyLuc(LocalDateTime xuLyLuc) {
        this.xuLyLuc = xuLyLuc;
    }

    public LocalDateTime getHoanTatLuc() {
        return hoanTatLuc;
    }

    public void setHoanTatLuc(LocalDateTime hoanTatLuc) {
        this.hoanTatLuc = hoanTatLuc;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getTenNguoiLienHe() {
        return tenNguoiLienHe;
    }

    public void setTenNguoiLienHe(String tenNguoiLienHe) {
        this.tenNguoiLienHe = tenNguoiLienHe;
    }

    public String getSdtNguoiLienHe() {
        return sdtNguoiLienHe;
    }

    public void setSdtNguoiLienHe(String sdtNguoiLienHe) {
        this.sdtNguoiLienHe = sdtNguoiLienHe;
    }

    public String getTenNguoiXuLy() {
        return tenNguoiXuLy;
    }

    public void setTenNguoiXuLy(String tenNguoiXuLy) {
        this.tenNguoiXuLy = tenNguoiXuLy;
    }

    public String getEmailNguoiXuLy() {
        return emailNguoiXuLy;
    }

    public void setEmailNguoiXuLy(String emailNguoiXuLy) {
        this.emailNguoiXuLy = emailNguoiXuLy;
    }

    public MucUuTienYeuCauEnum getMucUuTienEnum() {
        return MucUuTienYeuCauEnum.tuMa(this.mucUuTien);
    }

    public TrangThaiYeuCauEnum getTrangThaiEnum() {
        return TrangThaiYeuCauEnum.tuMa(this.trangThai);
    }

    public boolean isChuaXuLy() {
        return getTrangThaiEnum().isChuaXuLy();
    }
}
