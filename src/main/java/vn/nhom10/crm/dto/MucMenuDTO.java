package vn.nhom10.crm.dto;

import java.io.Serializable;

/**
 * DTO đại diện cho một mục menu điều hướng được hiển thị ra giao diện.
 */
public class MucMenuDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maModule;
    private String tenHienThi;
    private String url;
    private String bieuTuong;
    private int thuTu;
    private boolean active;

    public MucMenuDTO() {
    }

    public MucMenuDTO(String maModule, String tenHienThi, String url, String bieuTuong, int thuTu, boolean active) {
        this.maModule = maModule;
        this.tenHienThi = tenHienThi;
        this.url = url;
        this.bieuTuong = bieuTuong;
        this.thuTu = thuTu;
        this.active = active;
    }

    public String getMaModule() {
        return maModule;
    }

    public void setMaModule(String maModule) {
        this.maModule = maModule;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }

    public void setTenHienThi(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBieuTuong() {
        return bieuTuong;
    }

    public void setBieuTuong(String bieuTuong) {
        this.bieuTuong = bieuTuong;
    }

    public int getThuTu() {
        return thuTu;
    }

    public void setThuTu(int thuTu) {
        this.thuTu = thuTu;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return tenHienThi + " (" + url + ")";
    }
}
