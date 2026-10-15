package vn.nhom10.crm.dto;

import java.io.Serializable;

/**
 * DTO hiển thị giai đoạn pipeline ở chế độ chỉ đọc trong form xem trước chuyển đổi Lead (Story S4-08).
 */
public class GiaiDoanOptionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String ten;
    private Integer xacSuat;
    private Integer thuTu;
    private boolean dangSuDung;

    public GiaiDoanOptionDTO() {
    }

    public GiaiDoanOptionDTO(Long id, String ten, Integer xacSuat, Integer thuTu, boolean dangSuDung) {
        this.id = id;
        this.ten = ten;
        this.xacSuat = xacSuat;
        this.thuTu = thuTu;
        this.dangSuDung = dangSuDung;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public Integer getXacSuat() {
        return xacSuat;
    }

    public void setXacSuat(Integer xacSuat) {
        this.xacSuat = xacSuat;
    }

    public Integer getThuTu() {
        return thuTu;
    }

    public void setThuTu(Integer thuTu) {
        this.thuTu = thuTu;
    }

    public boolean isDangSuDung() {
        return dangSuDung;
    }

    public void setDangSuDung(boolean dangSuDung) {
        this.dangSuDung = dangSuDung;
    }
}
