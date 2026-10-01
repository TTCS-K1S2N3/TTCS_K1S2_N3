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
        this.phamViToiDa = PhamViDuLieu.CA_NHAN; // Fail-closed: Mặc định tối thiểu CA_NHAN trừ khi DB chỉ định
    }

    public VaiTro(int id, String maVaiTro, String tenVaiTro, String moTa, PhamViDuLieu phamViToiDa) {
        this.id = id;
        this.maVaiTro = maVaiTro;
        this.tenVaiTro = tenVaiTro;
        this.moTa = moTa;
        this.phamViToiDa = (phamViToiDa != null) ? phamViToiDa : PhamViDuLieu.CA_NHAN;
    }

    public VaiTro(VaiTroEnum vaiTroEnum) {
        if (vaiTroEnum != null) {
            this.maVaiTro = vaiTroEnum.getMaVaiTro();
            this.tenVaiTro = vaiTroEnum.getTenTiengViet();
            this.moTa = vaiTroEnum.getTenTiengAnh();
            this.phamViToiDa = PhamViDuLieu.CA_NHAN; // Fail-closed
        }
    }

    public VaiTro(VaiTroEnum vaiTroEnum, PhamViDuLieu phamViToiDa) {
        if (vaiTroEnum != null) {
            this.maVaiTro = vaiTroEnum.getMaVaiTro();
            this.tenVaiTro = vaiTroEnum.getTenTiengViet();
            this.moTa = vaiTroEnum.getTenTiengAnh();
            this.phamViToiDa = (phamViToiDa != null) ? phamViToiDa : PhamViDuLieu.CA_NHAN;
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
        return (phamViToiDa != null) ? phamViToiDa : PhamViDuLieu.CA_NHAN; // Fail-closed: Luôn là CA_NHAN nếu thiếu dữ liệu DB
    }

    public void setPhamViToiDa(PhamViDuLieu phamViToiDa) {
        this.phamViToiDa = (phamViToiDa != null) ? phamViToiDa : PhamViDuLieu.CA_NHAN;
    }

    public void setPhamViToiDa(String phamViToiDaStr) {
        PhamViDuLieu p = PhamViDuLieu.tuMa(phamViToiDaStr);
        this.phamViToiDa = (p != null) ? p : PhamViDuLieu.CA_NHAN;
    }

    public VaiTroEnum getVaiTroEnum() {
        return VaiTroEnum.tuMa(maVaiTro);
    }

    public String getTenHienThi() {
        return (tenVaiTro != null && !tenVaiTro.isBlank()) ? tenVaiTro : maVaiTro;
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
