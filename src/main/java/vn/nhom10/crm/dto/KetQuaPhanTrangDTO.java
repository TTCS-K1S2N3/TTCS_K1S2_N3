package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * DTO đóng gói kết quả phân trang dùng chung cho giao diện CRM.
 */
public class KetQuaPhanTrangDTO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> danhSach = Collections.emptyList();
    private int trangHienTai = 1;
    private int soBanGhiTrenTrang = 10;
    private long tongSoBanGhi = 0;
    private int tongSoTrang = 0;

    public KetQuaPhanTrangDTO() {
    }

    public KetQuaPhanTrangDTO(List<T> danhSach, int trangHienTai, int soBanGhiTrenTrang, long tongSoBanGhi) {
        this.danhSach = danhSach != null ? danhSach : Collections.emptyList();
        this.trangHienTai = trangHienTai;
        this.soBanGhiTrenTrang = soBanGhiTrenTrang;
        this.tongSoBanGhi = tongSoBanGhi;
        if (soBanGhiTrenTrang > 0) {
            this.tongSoTrang = (int) Math.ceil((double) tongSoBanGhi / soBanGhiTrenTrang);
        } else {
            this.tongSoTrang = 0;
        }
        if (this.tongSoTrang == 0 && tongSoBanGhi > 0) {
            this.tongSoTrang = 1;
        }
    }

    public List<T> getDanhSach() {
        return danhSach;
    }

    public void setDanhSach(List<T> danhSach) {
        this.danhSach = danhSach;
    }

    public int getTrangHienTai() {
        return trangHienTai;
    }

    public void setTrangHienTai(int trangHienTai) {
        this.trangHienTai = trangHienTai;
    }

    public int getSoBanGhiTrenTrang() {
        return soBanGhiTrenTrang;
    }

    public void setSoBanGhiTrenTrang(int soBanGhiTrenTrang) {
        this.soBanGhiTrenTrang = soBanGhiTrenTrang;
    }

    public long getTongSoBanGhi() {
        return tongSoBanGhi;
    }

    public void setTongSoBanGhi(long tongSoBanGhi) {
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

    public long getBanGhiBatDau() {
        if (tongSoBanGhi == 0) return 0;
        return (long) (trangHienTai - 1) * soBanGhiTrenTrang + 1;
    }

    public long getBanGhiKetThuc() {
        if (tongSoBanGhi == 0) return 0;
        return Math.min((long) trangHienTai * soBanGhiTrenTrang, tongSoBanGhi);
    }
}
