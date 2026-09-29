package vn.nhom10.crm.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Model đại diện cho vai trò người dùng trong hệ thống.
 */
public class VaiTro implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String maVaiTro;
    private String tenVaiTro;
    private String moTa;

    public VaiTro() {
    }

    public VaiTro(int id, String maVaiTro, String tenVaiTro, String moTa) {
        this.id = id;
        this.maVaiTro = maVaiTro;
        this.tenVaiTro = tenVaiTro;
        this.moTa = moTa;
    }

    public VaiTro(VaiTroEnum vaiTroEnum) {
        if (vaiTroEnum != null) {
            this.maVaiTro = vaiTroEnum.getMaVaiTro();
            this.tenVaiTro = vaiTroEnum.getTenTiengViet();
            this.moTa = vaiTroEnum.getTenTiengAnh();
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VaiTro vaiTro = (VaiTro) o;
        return Objects.equals(maVaiTro, vaiTro.maVaiTro);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maVaiTro);
    }

    @Override
    public String toString() {
        return tenVaiTro != null ? tenVaiTro : maVaiTro;
    }
}
