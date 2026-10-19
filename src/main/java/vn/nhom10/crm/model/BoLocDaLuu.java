package vn.nhom10.crm.model;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Model ánh xạ bảng `bo_loc_da_luu` theo schema canonical (Story S3-07).
 * Cho phép nhân viên kinh doanh lưu lại các bộ lọc hay dùng để tái sử dụng nhanh chóng.
 */
public class BoLocDaLuu {

    public static final String LOAI_KHACH_HANG = "KHACH_HANG";

    private Long id;
    private Long nguoiDungId;
    private String loaiDoiTuong = LOAI_KHACH_HANG;
    private String tenBoLoc;
    private String tieuChiJson;
    private boolean macDinh = false;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public BoLocDaLuu() {
    }

    public BoLocDaLuu(Long nguoiDungId, String tenBoLoc, String tieuChiJson) {
        this.nguoiDungId = nguoiDungId;
        this.tenBoLoc = tenBoLoc;
        this.tieuChiJson = tieuChiJson;
        this.loaiDoiTuong = LOAI_KHACH_HANG;
    }

    public BoLocDaLuu(Long id, Long nguoiDungId, String loaiDoiTuong, String tenBoLoc,
                      String tieuChiJson, boolean macDinh) {
        this.id = id;
        this.nguoiDungId = nguoiDungId;
        this.loaiDoiTuong = loaiDoiTuong != null ? loaiDoiTuong : LOAI_KHACH_HANG;
        this.tenBoLoc = tenBoLoc;
        this.tieuChiJson = tieuChiJson;
        this.macDinh = macDinh;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNguoiDungId() {
        return nguoiDungId;
    }

    public void setNguoiDungId(Long nguoiDungId) {
        this.nguoiDungId = nguoiDungId;
    }

    public String getLoaiDoiTuong() {
        return loaiDoiTuong;
    }

    public void setLoaiDoiTuong(String loaiDoiTuong) {
        this.loaiDoiTuong = loaiDoiTuong;
    }

    public String getTenBoLoc() {
        return tenBoLoc;
    }

    public void setTenBoLoc(String tenBoLoc) {
        this.tenBoLoc = tenBoLoc;
    }

    public String getTieuChiJson() {
        return tieuChiJson;
    }

    public void setTieuChiJson(String tieuChiJson) {
        this.tieuChiJson = tieuChiJson;
    }

    public boolean isMacDinh() {
        return macDinh;
    }

    public void setMacDinh(boolean macDinh) {
        this.macDinh = macDinh;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoLocDaLuu that = (BoLocDaLuu) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "BoLocDaLuu{" +
                "id=" + id +
                ", nguoiDungId=" + nguoiDungId +
                ", loaiDoiTuong='" + loaiDoiTuong + '\'' +
                ", tenBoLoc='" + tenBoLoc + '\'' +
                ", macDinh=" + macDinh +
                '}';
    }
}
