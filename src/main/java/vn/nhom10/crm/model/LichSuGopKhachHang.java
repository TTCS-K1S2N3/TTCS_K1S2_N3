package vn.nhom10.crm.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entity ánh xạ bảng `lich_su_gop_khach_hang` (Story S3-04).
 * Lưu vết kiểm toán khi Trưởng nhóm kinh doanh thực hiện gộp khách hàng trùng lặp.
 */
public class LichSuGopKhachHang implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long khachHangNguonId;
    private Long khachHangDichId;
    private Long thucHienBoiId;
    private String duLieuSoSanhJson;
    private String ketQuaChuyenJson;
    private String lyDo;
    private LocalDateTime createdAt;

    // Bổ sung thông tin hiển thị tiện ích
    private String tenKhachHangNguon;
    private String tenKhachHangDich;
    private String tenNguoiThucHien;

    public LichSuGopKhachHang() {
    }

    public LichSuGopKhachHang(Long id, Long khachHangNguonId, Long khachHangDichId,
                              Long thucHienBoiId, String duLieuSoSanhJson,
                              String ketQuaChuyenJson, String lyDo, LocalDateTime createdAt) {
        this.id = id;
        this.khachHangNguonId = khachHangNguonId;
        this.khachHangDichId = khachHangDichId;
        this.thucHienBoiId = thucHienBoiId;
        this.duLieuSoSanhJson = duLieuSoSanhJson;
        this.ketQuaChuyenJson = ketQuaChuyenJson;
        this.lyDo = lyDo;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getKhachHangNguonId() {
        return khachHangNguonId;
    }

    public void setKhachHangNguonId(Long khachHangNguonId) {
        this.khachHangNguonId = khachHangNguonId;
    }

    public Long getKhachHangDichId() {
        return khachHangDichId;
    }

    public void setKhachHangDichId(Long khachHangDichId) {
        this.khachHangDichId = khachHangDichId;
    }

    public Long getThucHienBoiId() {
        return thucHienBoiId;
    }

    public void setThucHienBoiId(Long thucHienBoiId) {
        this.thucHienBoiId = thucHienBoiId;
    }

    public String getDuLieuSoSanhJson() {
        return duLieuSoSanhJson;
    }

    public void setDuLieuSoSanhJson(String duLieuSoSanhJson) {
        this.duLieuSoSanhJson = duLieuSoSanhJson;
    }

    public String getKetQuaChuyenJson() {
        return ketQuaChuyenJson;
    }

    public void setKetQuaChuyenJson(String ketQuaChuyenJson) {
        this.ketQuaChuyenJson = ketQuaChuyenJson;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getTenKhachHangNguon() {
        return tenKhachHangNguon;
    }

    public void setTenKhachHangNguon(String tenKhachHangNguon) {
        this.tenKhachHangNguon = tenKhachHangNguon;
    }

    public String getTenKhachHangDich() {
        return tenKhachHangDich;
    }

    public void setTenKhachHangDich(String tenKhachHangDich) {
        this.tenKhachHangDich = tenKhachHangDich;
    }

    public String getTenNguoiThucHien() {
        return tenNguoiThucHien;
    }

    public void setTenNguoiThucHien(String tenNguoiThucHien) {
        this.tenNguoiThucHien = tenNguoiThucHien;
    }
}
