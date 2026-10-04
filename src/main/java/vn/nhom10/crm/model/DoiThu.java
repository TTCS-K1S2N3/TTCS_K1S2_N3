package vn.nhom10.crm.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Model đại diện cho đối thủ cạnh tranh của doanh nghiệp (Story S2-10).
 * Tương ứng bảng 'doi_thu' trong database.
 * Phục vụ phân tích thị phần và bắt buộc chọn khi đóng cơ hội thua trong Sprint 5.
 */
public class DoiThu implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String maDoiThu;
    private String tenDoiThu;
    private String website;
    private String ghiChu;
    private boolean hoatDong;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Trường bổ sung hiển thị số lượng cơ hội đang tham chiếu
    private int soCoHoiThamChieu;

    public DoiThu() {
        this.hoatDong = true;
    }

    public DoiThu(String maDoiThu, String tenDoiThu, String website, String ghiChu, boolean hoatDong) {
        this.maDoiThu = maDoiThu;
        this.tenDoiThu = tenDoiThu;
        this.website = website;
        this.ghiChu = ghiChu;
        this.hoatDong = hoatDong;
    }

    public DoiThu(Long id, String maDoiThu, String tenDoiThu, String website, String ghiChu, boolean hoatDong, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.maDoiThu = maDoiThu;
        this.tenDoiThu = tenDoiThu;
        this.website = website;
        this.ghiChu = ghiChu;
        this.hoatDong = hoatDong;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public boolean dangHoatDong() {
        return this.hoatDong;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaDoiThu() {
        return maDoiThu;
    }

    public void setMaDoiThu(String maDoiThu) {
        this.maDoiThu = maDoiThu;
    }

    public String getTenDoiThu() {
        return tenDoiThu;
    }

    public void setTenDoiThu(String tenDoiThu) {
        this.tenDoiThu = tenDoiThu;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public boolean isHoatDong() {
        return hoatDong;
    }

    public void setHoatDong(boolean hoatDong) {
        this.hoatDong = hoatDong;
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

    public int getSoCoHoiThamChieu() {
        return soCoHoiThamChieu;
    }

    public void setSoCoHoiThamChieu(int soCoHoiThamChieu) {
        this.soCoHoiThamChieu = soCoHoiThamChieu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DoiThu doiThu = (DoiThu) o;
        return Objects.equals(id, doiThu.id) || Objects.equals(maDoiThu, doiThu.maDoiThu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, maDoiThu);
    }

    @Override
    public String toString() {
        return "DoiThu{" +
                "id=" + id +
                ", maDoiThu='" + maDoiThu + '\'' +
                ", tenDoiThu='" + tenDoiThu + '\'' +
                ", website='" + website + '\'' +
                ", hoatDong=" + hoatDong +
                '}';
    }
}
