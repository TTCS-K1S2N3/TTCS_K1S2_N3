package vn.nhom10.crm.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Model đại diện cho khu vực địa lý trong hệ thống CRM (Story S2-06).
 */
public class KhuVucDiaLy implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String maKhuVuc;
    private String tenKhuVuc;
    private String loaiKhuVuc; // MIEN, TINH_THANH, QUAN_HUYEN, KHAC
    private Long khuVucChaId;
    private int thuTuHienThi = 0;
    private boolean hoatDong = true;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Các trường tiện ích hiển thị cây phân cấp
    private String tenKhuVucCha;
    private int capDo = 1;
    private int soLuongNhom = 0;
    private List<KhuVucDiaLy> dsKhuVucCon = new ArrayList<>();

    public KhuVucDiaLy() {
    }

    public KhuVucDiaLy(long id, String maKhuVuc, String tenKhuVuc) {
        this.id = id;
        this.maKhuVuc = maKhuVuc;
        this.tenKhuVuc = tenKhuVuc;
    }

    public KhuVucDiaLy(long id, String maKhuVuc, String tenKhuVuc, String loaiKhuVuc, Long khuVucChaId, int thuTuHienThi, boolean hoatDong) {
        this.id = id;
        this.maKhuVuc = maKhuVuc;
        this.tenKhuVuc = tenKhuVuc;
        this.loaiKhuVuc = loaiKhuVuc;
        this.khuVucChaId = khuVucChaId;
        this.thuTuHienThi = thuTuHienThi;
        this.hoatDong = hoatDong;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMaKhuVuc() {
        return maKhuVuc;
    }

    public void setMaKhuVuc(String maKhuVuc) {
        this.maKhuVuc = maKhuVuc;
    }

    public String getTenKhuVuc() {
        return tenKhuVuc;
    }

    public void setTenKhuVuc(String tenKhuVuc) {
        this.tenKhuVuc = tenKhuVuc;
    }

    public String getLoaiKhuVuc() {
        return loaiKhuVuc;
    }

    public void setLoaiKhuVuc(String loaiKhuVuc) {
        this.loaiKhuVuc = loaiKhuVuc;
    }

    public Long getKhuVucChaId() {
        return khuVucChaId;
    }

    public void setKhuVucChaId(Long khuVucChaId) {
        this.khuVucChaId = khuVucChaId;
    }

    public int getThuTuHienThi() {
        return thuTuHienThi;
    }

    public void setThuTuHienThi(int thuTuHienThi) {
        this.thuTuHienThi = thuTuHienThi;
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

    public String getTenKhuVucCha() {
        return tenKhuVucCha;
    }

    public void setTenKhuVucCha(String tenKhuVucCha) {
        this.tenKhuVucCha = tenKhuVucCha;
    }

    public int getCapDo() {
        return capDo;
    }

    public void setCapDo(int capDo) {
        this.capDo = capDo;
    }

    public int getSoLuongNhom() {
        return soLuongNhom;
    }

    public void setSoLuongNhom(int soLuongNhom) {
        this.soLuongNhom = soLuongNhom;
    }

    public List<KhuVucDiaLy> getDsKhuVucCon() {
        return dsKhuVucCon;
    }

    public void setDsKhuVucCon(List<KhuVucDiaLy> dsKhuVucCon) {
        this.dsKhuVucCon = dsKhuVucCon;
    }

    public void themKhuVucCon(KhuVucDiaLy con) {
        if (this.dsKhuVucCon == null) {
            this.dsKhuVucCon = new ArrayList<>();
        }
        this.dsKhuVucCon.add(con);
    }
}
