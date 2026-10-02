package vn.nhom10.crm.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Đại diện cho vai trò người dùng trong hệ thống (vai_tro).
 */
public class VaiTro implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String maVaiTro;
    private String tenVaiTro;
    private String moTa;
    private String phamViMacDinh;
    private boolean hoatDong;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public VaiTro() {
    }

    public VaiTro(Long id, String maVaiTro, String tenVaiTro, String moTa, String phamViMacDinh) {
        this.id = id;
        this.maVaiTro = maVaiTro;
        this.tenVaiTro = tenVaiTro;
        this.moTa = moTa;
        this.phamViMacDinh = phamViMacDinh;
        this.hoatDong = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaVaiTro() {
        return maVaiTro;
    }

    public void setMaVaiTro(String maVaiTro) {
        this.maVaiTro = maVaiTro;
    }

    public String getTenVaiTro() {
        return tenVaiTro;
    }

    public void setTenVaiTro(String tenVaiTro) {
        this.tenVaiTro = tenVaiTro;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getPhamViMacDinh() {
        return phamViMacDinh;
    }

    public void setPhamViMacDinh(String phamViMacDinh) {
        this.phamViMacDinh = phamViMacDinh;
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

    @Override
    public String toString() {
        return "VaiTro{" +
                "id=" + id +
                ", maVaiTro='" + maVaiTro + '\'' +
                ", tenVaiTro='" + tenVaiTro + '\'' +
                '}';
    }
}
