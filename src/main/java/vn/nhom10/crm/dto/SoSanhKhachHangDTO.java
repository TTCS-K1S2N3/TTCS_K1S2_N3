package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO dữ liệu so sánh cạnh nhau trước khi gộp (Story S3-04, AC2).
 * Cung cấp thông tin hai bản ghi nguồn và đích cùng số lượng dữ liệu liên quan
 * (người liên hệ, cơ hội, hoạt động, tệp đính kèm) để người quản lý đánh giá trước khi quyết định gộp.
 */
public class SoSanhKhachHangDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private BanGhiNghiepVuDTO khachHangNguon;
    private BanGhiNghiepVuDTO khachHangDich;

    // Thống kê dữ liệu bảo toàn (AC3)
    private int soLuongNguoiLienHeNguon;
    private int soLuongNguoiLienHeDich;

    private int soLuongCoHoiNguon;
    private int soLuongCoHoiDich;

    private int soLuongHoatDongNguon;
    private int soLuongHoatDongDich;

    private int soLuongTepDinhKemNguon;
    private int soLuongTepDinhKemDich;

    // Chi tiết phân tích trùng khớp
    private boolean trungMst;
    private boolean trungWebsite;
    private boolean trungTen;
    private int tyLeTuongDongTen;
    private boolean xungDotNhanVien;

    private List<String> danhSachLyDo = new ArrayList<>();
    private String thongDiepCanhBao;

    public SoSanhKhachHangDTO() {
    }

    public SoSanhKhachHangDTO(BanGhiNghiepVuDTO khachHangNguon, BanGhiNghiepVuDTO khachHangDich) {
        this.khachHangNguon = khachHangNguon;
        this.khachHangDich = khachHangDich;
    }

    public BanGhiNghiepVuDTO getKhachHangNguon() {
        return khachHangNguon;
    }

    public void setKhachHangNguon(BanGhiNghiepVuDTO khachHangNguon) {
        this.khachHangNguon = khachHangNguon;
    }

    public BanGhiNghiepVuDTO getKhachHangDich() {
        return khachHangDich;
    }

    public void setKhachHangDich(BanGhiNghiepVuDTO khachHangDich) {
        this.khachHangDich = khachHangDich;
    }

    public int getSoLuongNguoiLienHeNguon() {
        return soLuongNguoiLienHeNguon;
    }

    public void setSoLuongNguoiLienHeNguon(int soLuongNguoiLienHeNguon) {
        this.soLuongNguoiLienHeNguon = soLuongNguoiLienHeNguon;
    }

    public int getSoLuongNguoiLienHeDich() {
        return soLuongNguoiLienHeDich;
    }

    public void setSoLuongNguoiLienHeDich(int soLuongNguoiLienHeDich) {
        this.soLuongNguoiLienHeDich = soLuongNguoiLienHeDich;
    }

    public int getSoLuongCoHoiNguon() {
        return soLuongCoHoiNguon;
    }

    public void setSoLuongCoHoiNguon(int soLuongCoHoiNguon) {
        this.soLuongCoHoiNguon = soLuongCoHoiNguon;
    }

    public int getSoLuongCoHoiDich() {
        return soLuongCoHoiDich;
    }

    public void setSoLuongCoHoiDich(int soLuongCoHoiDich) {
        this.soLuongCoHoiDich = soLuongCoHoiDich;
    }

    public int getSoLuongHoatDongNguon() {
        return soLuongHoatDongNguon;
    }

    public void setSoLuongHoatDongNguon(int soLuongHoatDongNguon) {
        this.soLuongHoatDongNguon = soLuongHoatDongNguon;
    }

    public int getSoLuongHoatDongDich() {
        return soLuongHoatDongDich;
    }

    public void setSoLuongHoatDongDich(int soLuongHoatDongDich) {
        this.soLuongHoatDongDich = soLuongHoatDongDich;
    }

    public int getSoLuongTepDinhKemNguon() {
        return soLuongTepDinhKemNguon;
    }

    public void setSoLuongTepDinhKemNguon(int soLuongTepDinhKemNguon) {
        this.soLuongTepDinhKemNguon = soLuongTepDinhKemNguon;
    }

    public int getSoLuongTepDinhKemDich() {
        return soLuongTepDinhKemDich;
    }

    public void setSoLuongTepDinhKemDich(int soLuongTepDinhKemDich) {
        this.soLuongTepDinhKemDich = soLuongTepDinhKemDich;
    }

    public boolean isTrungMst() {
        return trungMst;
    }

    public void setTrungMst(boolean trungMst) {
        this.trungMst = trungMst;
    }

    public boolean isTrungWebsite() {
        return trungWebsite;
    }

    public void setTrungWebsite(boolean trungWebsite) {
        this.trungWebsite = trungWebsite;
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

    public String getThongDiepCanhBao() {
        return thongDiepCanhBao;
    }

    public void setThongDiepCanhBao(String thongDiepCanhBao) {
        this.thongDiepCanhBao = thongDiepCanhBao;
    }
}
