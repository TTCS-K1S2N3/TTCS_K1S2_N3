package vn.nhom10.crm.dto;

import java.io.Serializable;

/**
 * Data Transfer Object đóng gói thông tin báo lỗi và hành động gợi ý quay lại luồng làm việc.
 * Phục vụ Story S1-07:
 * - AC1: Trang báo lỗi dùng chung giao diện ứng dụng.
 * - AC2: Mỗi trang lỗi có một hành động gợi ý để quay lại luồng làm việc.
 */
public class ThongTinLoi implements Serializable {

    private static final long serialVersionUID = 1L;

    private int maLoi;
    private String tieuDe;
    private String moTa;
    private String chiTiet;
    private String maThamChieu;
    private String urlHanhDongChinh;
    private String tenHanhDongChinh;
    private String urlHanhDongPhu;
    private String tenHanhDongPhu;
    private String loaiGiaoDien; // warning, danger, info
    private String bieuTuong;

    public ThongTinLoi() {
    }

    public ThongTinLoi(int maLoi, String tieuDe, String moTa, String urlHanhDongChinh, String tenHanhDongChinh) {
        this.maLoi = maLoi;
        this.tieuDe = tieuDe;
        this.moTa = moTa;
        this.urlHanhDongChinh = urlHanhDongChinh;
        this.tenHanhDongChinh = tenHanhDongChinh;
    }

    public int getMaLoi() {
        return maLoi;
    }

    public void setMaLoi(int maLoi) {
        this.maLoi = maLoi;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getChiTiet() {
        return chiTiet;
    }

    public void setChiTiet(String chiTiet) {
        this.chiTiet = chiTiet;
    }

    public String getMaThamChieu() {
        return maThamChieu;
    }

    public void setMaThamChieu(String maThamChieu) {
        this.maThamChieu = maThamChieu;
    }

    public String getUrlHanhDongChinh() {
        return urlHanhDongChinh;
    }

    public void setUrlHanhDongChinh(String urlHanhDongChinh) {
        this.urlHanhDongChinh = urlHanhDongChinh;
    }

    public String getTenHanhDongChinh() {
        return tenHanhDongChinh;
    }

    public void setTenHanhDongChinh(String tenHanhDongChinh) {
        this.tenHanhDongChinh = tenHanhDongChinh;
    }

    public String getUrlHanhDongPhu() {
        return urlHanhDongPhu;
    }

    public void setUrlHanhDongPhu(String urlHanhDongPhu) {
        this.urlHanhDongPhu = urlHanhDongPhu;
    }

    public String getTenHanhDongPhu() {
        return tenHanhDongPhu;
    }

    public void setTenHanhDongPhu(String tenHanhDongPhu) {
        this.tenHanhDongPhu = tenHanhDongPhu;
    }

    public String getLoaiGiaoDien() {
        return loaiGiaoDien;
    }

    public void setLoaiGiaoDien(String loaiGiaoDien) {
        this.loaiGiaoDien = loaiGiaoDien;
    }

    public String getBieuTuong() {
        return bieuTuong;
    }

    public void setBieuTuong(String bieuTuong) {
        this.bieuTuong = bieuTuong;
    }
}
