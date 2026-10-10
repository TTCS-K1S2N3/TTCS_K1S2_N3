package vn.nhom10.crm.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Model ánh xạ bảng vai_tro_module trong cơ sở dữ liệu.
 * Đại diện cho quyền của một vai trò trên một module nghiệp vụ cụ thể.
 */
public class VaiTroModule implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private int vaiTroId;
    private String maVaiTro;
    private String tenVaiTro;
    private PhamViDuLieu phamViToiDaVaiTro;

    private int moduleId;
    private String maModule;
    private String tenModule;
    private String moTaModule;
    private int thuTuHienThi;

    private MucQuyen mucQuyen = MucQuyen.NONE;
    private PhamViDuLieu phamViDuLieu;
    private Timestamp createdAt;

    public VaiTroModule() {
    }

    public VaiTroModule(int vaiTroId, int moduleId, MucQuyen mucQuyen, PhamViDuLieu phamViDuLieu) {
        this.vaiTroId = vaiTroId;
        this.moduleId = moduleId;
        this.mucQuyen = mucQuyen != null ? mucQuyen : MucQuyen.NONE;
        this.phamViDuLieu = phamViDuLieu;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getVaiTroId() {
        return vaiTroId;
    }

    public void setVaiTroId(int vaiTroId) {
        this.vaiTroId = vaiTroId;
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

    public PhamViDuLieu getPhamViToiDaVaiTro() {
        return phamViToiDaVaiTro;
    }

    public void setPhamViToiDaVaiTro(PhamViDuLieu phamViToiDaVaiTro) {
        this.phamViToiDaVaiTro = phamViToiDaVaiTro;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public String getMaModule() {
        return maModule;
    }

    public void setMaModule(String maModule) {
        this.maModule = maModule;
    }

    public String getTenModule() {
        return tenModule;
    }

    public void setTenModule(String tenModule) {
        this.tenModule = tenModule;
    }

    public String getMoTaModule() {
        return moTaModule;
    }

    public void setMoTaModule(String moTaModule) {
        this.moTaModule = moTaModule;
    }

    public int getThuTuHienThi() {
        return thuTuHienThi;
    }

    public void setThuTuHienThi(int thuTuHienThi) {
        this.thuTuHienThi = thuTuHienThi;
    }

    public MucQuyen getMucQuyen() {
        return mucQuyen != null ? mucQuyen : MucQuyen.NONE;
    }

    public void setMucQuyen(MucQuyen mucQuyen) {
        this.mucQuyen = mucQuyen != null ? mucQuyen : MucQuyen.NONE;
    }

    public void setMucQuyen(String mucQuyenStr) {
        this.mucQuyen = MucQuyen.tuMa(mucQuyenStr);
    }

    public PhamViDuLieu getPhamViDuLieu() {
        return phamViDuLieu;
    }

    public void setPhamViDuLieu(PhamViDuLieu phamViDuLieu) {
        this.phamViDuLieu = phamViDuLieu;
    }

    public void setPhamViDuLieu(String phamViDuLieuStr) {
        this.phamViDuLieu = (phamViDuLieuStr != null && !phamViDuLieuStr.isBlank())
                ? PhamViDuLieu.tuMa(phamViDuLieuStr)
                : null;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isXem() {
        return getMucQuyen().isXem();
    }

    public boolean isTao() {
        return getMucQuyen().isTao();
    }

    public boolean isSua() {
        return getMucQuyen().isSua();
    }

    public boolean isXoa() {
        return getMucQuyen().isXoa();
    }

    public boolean coQuyen(MucQuyen required) {
        return getMucQuyen().baoGom(required);
    }
}
