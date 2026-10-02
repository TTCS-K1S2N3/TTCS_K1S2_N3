package vn.nhom10.crm.model;

import java.io.Serializable;

/**
 * Model đại diện cho bảng dieu_kien_giai_doan theo schema canonical 8 sprints.
 * Khai báo điều kiện bắt buộc để rời một giai đoạn (AC 3).
 */
public class DieuKienGiaiDoan implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private long giaiDoanNguonId;
    private Long giaiDoanDichId;
    private String maDieuKien;
    private String tenDieuKien;
    private String loaiDieuKien; // CUOC_GAP, CUOC_GOI, BAO_GIA, KHAO_SAT, TUY_CHINH
    private String cauHinhJson;
    private String thongBaoThieu;
    private int thuTu = 0;
    private boolean hoatDong = true;

    public DieuKienGiaiDoan() {
    }

    public DieuKienGiaiDoan(long giaiDoanNguonId, String maDieuKien, String tenDieuKien,
                           String loaiDieuKien, String cauHinhJson, String thongBaoThieu) {
        this.giaiDoanNguonId = giaiDoanNguonId;
        this.maDieuKien = maDieuKien;
        this.tenDieuKien = tenDieuKien;
        this.loaiDieuKien = loaiDieuKien;
        this.cauHinhJson = cauHinhJson;
        this.thongBaoThieu = thongBaoThieu;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getGiaiDoanNguonId() {
        return giaiDoanNguonId;
    }

    public void setGiaiDoanNguonId(long giaiDoanNguonId) {
        this.giaiDoanNguonId = giaiDoanNguonId;
    }

    public Long getGiaiDoanDichId() {
        return giaiDoanDichId;
    }

    public void setGiaiDoanDichId(Long giaiDoanDichId) {
        this.giaiDoanDichId = giaiDoanDichId;
    }

    public String getMaDieuKien() {
        return maDieuKien;
    }

    public void setMaDieuKien(String maDieuKien) {
        this.maDieuKien = maDieuKien;
    }

    public String getTenDieuKien() {
        return tenDieuKien;
    }

    public void setTenDieuKien(String tenDieuKien) {
        this.tenDieuKien = tenDieuKien;
    }

    public String getLoaiDieuKien() {
        return loaiDieuKien;
    }

    public void setLoaiDieuKien(String loaiDieuKien) {
        this.loaiDieuKien = loaiDieuKien;
    }

    public String getCauHinhJson() {
        return cauHinhJson;
    }

    public void setCauHinhJson(String cauHinhJson) {
        this.cauHinhJson = cauHinhJson;
    }

    public String getThongBaoThieu() {
        return thongBaoThieu;
    }

    public void setThongBaoThieu(String thongBaoThieu) {
        this.thongBaoThieu = thongBaoThieu;
    }

    public int getThuTu() {
        return thuTu;
    }

    public void setThuTu(int thuTu) {
        this.thuTu = thuTu;
    }

    public boolean isHoatDong() {
        return hoatDong;
    }

    public void setHoatDong(boolean hoatDong) {
        this.hoatDong = hoatDong;
    }
}
