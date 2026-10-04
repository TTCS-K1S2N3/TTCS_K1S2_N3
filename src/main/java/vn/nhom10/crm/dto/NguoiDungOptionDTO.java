package vn.nhom10.crm.dto;

import java.io.Serializable;

/**
 * DTO đại diện cho mục người dùng trong bộ lọc nhật ký hệ thống.
 */
public class NguoiDungOptionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String hoTen;
    private String email;
    private String vaiTro;

    public NguoiDungOptionDTO() {
    }

    public NguoiDungOptionDTO(Long id, String hoTen, String email, String vaiTro) {
        this.id = id;
        this.hoTen = hoTen;
        this.email = email;
        this.vaiTro = vaiTro;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }

    public String getTenHienThiDropdown() {
        if (vaiTro != null && !vaiTro.isEmpty()) {
            return hoTen + " (" + vaiTro + ")";
        }
        return hoTen;
    }
}
