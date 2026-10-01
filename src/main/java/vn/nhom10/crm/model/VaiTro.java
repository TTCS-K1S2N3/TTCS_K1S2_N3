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
    private PhamViDuLieu phamViToiDa;

    public VaiTro() {
    }

    public VaiTro(int id, String maVaiTro, String tenVaiTro, String moTa) {
        this.id = id;
        this.maVaiTro = maVaiTro;
        this.tenVaiTro = tenVaiTro;
        this.moTa = moTa;
        this.phamViToiDa = (VaiTroEnum.tuMa(maVaiTro) != null) ? VaiTroEnum.tuMa(maVaiTro).getPhamViToiDa() : PhamViDuLieu.CA_NHAN;
    }

    public VaiTro(int id, String maVaiTro, String tenVaiTro, String moTa, PhamViDuLieu phamViToiDa) {
        this.id = id;
        this.maVaiTro = maVaiTro;
        this.tenVaiTro = tenVaiTro;
        this.moTa = moTa;
        this.phamViToiDa = phamViToiDa;
    }

    public VaiTro(VaiTroEnum vaiTroEnum) {
        if (vaiTroEnum != null) {
            this.maVaiTro = vaiTroEnum.getMaVaiTro();
            this.tenVaiTro = vaiTroEnum.getTenTiengViet();
            this.moTa = vaiTroEnum.getTenTiengAnh();
            this.phamViToiDa = vaiTroEnum.getPhamViToiDa();
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

    public PhamViDuLieu getPhamViToiDa() {
        if (phamViToiDa != null) {
            return phamViToiDa;
        }
        VaiTroEnum en = VaiTroEnum.tuMa(maVaiTro);
        return en != null ? en.getPhamViToiDa() : PhamViDuLieu.CA_NHAN;
    }

    public void setPhamViToiDa(PhamViDuLieu phamViToiDa) {
        this.phamViToiDa = phamViToiDa;
    }

    public void setPhamViToiDa(String phamViToiDaStr) {
        this.phamViToiDa = PhamViDuLieu.tuMa(phamViToiDaStr);
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
