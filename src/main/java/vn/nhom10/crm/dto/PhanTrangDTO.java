package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO đại diện kết quả phân trang dùng chung cho hệ thống.
 */
public class PhanTrangDTO<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<T> danhSach = new ArrayList<>();
    private int trangHienTai = 1;
    private int soBanGhiMoiTrang = 20;
    private int tongSoBanGhi = 0;
    private int tongSoTrang = 1;

    public PhanTrangDTO() {
    }

    public PhanTrangDTO(List<T> danhSach, int trangHienTai, int soBanGhiMoiTrang, int tongSoBanGhi) {
        this.danhSach = danhSach != null ? danhSach : new ArrayList<>();
        this.trangHienTai = Math.max(trangHienTai, 1);
        this.soBanGhiMoiTrang = soBanGhiMoiTrang > 0 ? soBanGhiMoiTrang : 20;
        this.tongSoBanGhi = Math.max(tongSoBanGhi, 0);
        this.tongSoTrang = (int) Math.ceil((double) this.tongSoBanGhi / this.soBanGhiMoiTrang);
        if (this.tongSoTrang == 0) {
            this.tongSoTrang = 1;
        }
    }

    public List<T> getDanhSach() {
        return danhSach;
    }

    public void setDanhSach(List<T> danhSach) {
        this.danhSach = danhSach != null ? danhSach : new ArrayList<>();
    }

    public int getTrangHienTai() {
        return trangHienTai;
    }

    public void setTrangHienTai(int trangHienTai) {
        this.trangHienTai = trangHienTai;
    }

    public int getSoBanGhiMoiTrang() {
        return soBanGhiMoiTrang;
    }

    public void setSoBanGhiMoiTrang(int soBanGhiMoiTrang) {
        this.soBanGhiMoiTrang = soBanGhiMoiTrang;
    }

    public int getTongSoBanGhi() {
        return tongSoBanGhi;
    }

    public void setTongSoBanGhi(int tongSoBanGhi) {
        this.tongSoBanGhi = tongSoBanGhi;
    }

    public int getTongSoTrang() {
        return tongSoTrang;
    }

    public void setTongSoTrang(int tongSoTrang) {
        this.tongSoTrang = tongSoTrang;
    }

    public boolean isCoTrangTruoc() {
        return trangHienTai > 1;
    }

    public boolean isCoTrangSau() {
        return trangHienTai < tongSoTrang;
    }
}
