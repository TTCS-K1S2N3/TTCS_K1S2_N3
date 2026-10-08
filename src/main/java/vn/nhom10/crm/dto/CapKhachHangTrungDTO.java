package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO đại diện cho một cặp khách hàng bị phát hiện nghi trùng lặp (Story S3-04, AC1).
 * Tiêu chí trùng:
 * - Mã số thuế (MST) trùng khớp
 * - Tên công ty gần giống (độ tương đồng cao)
 * - Website trùng khớp
 */
public class CapKhachHangTrungDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id; // ID cặp: ví dụ "1-2"
    private BanGhiNghiepVuDTO banGhiA;
    private BanGhiNghiepVuDTO banGhiB;
    private boolean trungMst;
    private boolean trungTen;
    private int tyLeTuongDongTen;
    private boolean trungWebsite;
    private boolean xungDotNhanVien; // Hai nhân viên khác nhau cùng chào một công ty
    private List<String> danhSachLyDo = new ArrayList<>();
    private String doTinCay; // Rất cao / Cao / Khá

    public CapKhachHangTrungDTO() {
    }

    public CapKhachHangTrungDTO(String id, BanGhiNghiepVuDTO banGhiA, BanGhiNghiepVuDTO banGhiB) {
        this.id = id;
        this.banGhiA = banGhiA;
        this.banGhiB = banGhiB;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BanGhiNghiepVuDTO getBanGhiA() {
        return banGhiA;
    }

    public void setBanGhiA(BanGhiNghiepVuDTO banGhiA) {
        this.banGhiA = banGhiA;
    }

    public BanGhiNghiepVuDTO getBanGhiB() {
        return banGhiB;
    }

    public void setBanGhiB(BanGhiNghiepVuDTO banGhiB) {
        this.banGhiB = banGhiB;
    }

    public boolean isTrungMst() {
        return trungMst;
    }

    public void setTrungMst(boolean trungMst) {
        this.trungMst = trungMst;
    }

    public boolean isTrungTen() {
        return trungTen;
    }

    public void setTrungTen(boolean trungTen) {
        this.trungTen = trungTen;
    }

    public int getTyLeTuongDongTen() {
        return tyLeTuongDongTen;
    }

    public void setTyLeTuongDongTen(int tyLeTuongDongTen) {
        this.tyLeTuongDongTen = tyLeTuongDongTen;
    }

    public boolean isTrungWebsite() {
        return trungWebsite;
    }

    public void setTrungWebsite(boolean trungWebsite) {
        this.trungWebsite = trungWebsite;
    }

    public boolean isXungDotNhanVien() {
        return xungDotNhanVien;
    }

    public void setXungDotNhanVien(boolean xungDotNhanVien) {
        this.xungDotNhanVien = xungDotNhanVien;
    }

    public List<String> getDanhSachLyDo() {
        return danhSachLyDo;
    }

    public void setDanhSachLyDo(List<String> danhSachLyDo) {
        this.danhSachLyDo = danhSachLyDo;
    }

    public String getDoTinCay() {
        return doTinCay;
    }

    public void setDoTinCay(String doTinCay) {
        this.doTinCay = doTinCay;
    }
}
