package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO đại diện cho báo cáo tổng hợp xem trước và kết quả nhập khách hàng từ Excel.
 * Phục vụ Story S3-06:
 * - Thống kê số dòng tổng số, hợp lệ, dòng lỗi, dòng bị trùng
 * - Thống kê kết quả nhập: thêm mới, cập nhật, bỏ qua
 */
public class BaoCaoNhapKhachHangExcelDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String tenTep;
    private int tongSoDong = 0;
    private int soDongHopLe = 0;
    private int soDongLoi = 0;
    private int soDongBiTrung = 0;

    private int soDongThanhCong = 0;
    private int soDongCapNhat = 0;
    private int soDongBoQua = 0;
    private int soDongThatBai = 0;

    private boolean daThucHienNhap = false;
    private String thongDiep;

    private List<DongExcelKhachHangDTO> danhSachTatCaDong = new ArrayList<>();

    public BaoCaoNhapKhachHangExcelDTO() {
    }

    public BaoCaoNhapKhachHangExcelDTO(String tenTep) {
        this.tenTep = tenTep;
    }

    public void napDanhSachDong(List<DongExcelKhachHangDTO> danhSach) {
        this.danhSachTatCaDong = (danhSach != null) ? danhSach : new ArrayList<>();
        this.tongSoDong = this.danhSachTatCaDong.size();
        this.soDongHopLe = (int) this.danhSachTatCaDong.stream().filter(DongExcelKhachHangDTO::isHopLe).count();
        this.soDongLoi = this.tongSoDong - this.soDongHopLe;
        this.soDongBiTrung = (int) this.danhSachTatCaDong.stream().filter(DongExcelKhachHangDTO::isBiTrung).count();
    }

    public List<DongExcelKhachHangDTO> getDanhSachDongHopLe() {
        return danhSachTatCaDong.stream()
                .filter(DongExcelKhachHangDTO::isHopLe)
                .collect(Collectors.toList());
    }

    public List<DongExcelKhachHangDTO> getDanhSachDongLoi() {
        return danhSachTatCaDong.stream()
                .filter(d -> !d.isHopLe())
                .collect(Collectors.toList());
    }

    public List<DongExcelKhachHangDTO> getDanhSachDongBiTrung() {
        return danhSachTatCaDong.stream()
                .filter(DongExcelKhachHangDTO::isBiTrung)
                .collect(Collectors.toList());
    }

    public List<DongExcelKhachHangDTO> getDanhSachDongMoiHopLe() {
        return danhSachTatCaDong.stream()
                .filter(d -> d.isHopLe() && !d.isBiTrung())
                .collect(Collectors.toList());
    }

    // Getters and Setters

    public String getTenTep() {
        return tenTep;
    }

    public void setTenTep(String tenTep) {
        this.tenTep = tenTep;
    }

    public int getTongSoDong() {
        return tongSoDong;
    }

    public void setTongSoDong(int tongSoDong) {
        this.tongSoDong = tongSoDong;
    }

    public int getSoDongHopLe() {
        return soDongHopLe;
    }

    public void setSoDongHopLe(int soDongHopLe) {
        this.soDongHopLe = soDongHopLe;
    }

    public int getSoDongLoi() {
        return soDongLoi;
    }

    public void setSoDongLoi(int soDongLoi) {
        this.soDongLoi = soDongLoi;
    }

    public int getSoDongBiTrung() {
        return soDongBiTrung;
    }

    public void setSoDongBiTrung(int soDongBiTrung) {
        this.soDongBiTrung = soDongBiTrung;
    }

    public int getSoDongThanhCong() {
        return soDongThanhCong;
    }

    public void setSoDongThanhCong(int soDongThanhCong) {
        this.soDongThanhCong = soDongThanhCong;
    }

    public int getSoDongCapNhat() {
        return soDongCapNhat;
    }

    public void setSoDongCapNhat(int soDongCapNhat) {
        this.soDongCapNhat = soDongCapNhat;
    }

    public int getSoDongBoQua() {
        return soDongBoQua;
    }

    public void setSoDongBoQua(int soDongBoQua) {
        this.soDongBoQua = soDongBoQua;
    }

    public int getSoDongThatBai() {
        return soDongThatBai;
    }

    public void setSoDongThatBai(int soDongThatBai) {
        this.soDongThatBai = soDongThatBai;
    }

    public boolean isDaThucHienNhap() {
        return daThucHienNhap;
    }

    public void setDaThucHienNhap(boolean daThucHienNhap) {
        this.daThucHienNhap = daThucHienNhap;
    }

    public String getThongDiep() {
        return thongDiep;
    }

    public void setThongDiep(String thongDiep) {
        this.thongDiep = thongDiep;
    }

    public List<DongExcelKhachHangDTO> getDanhSachTatCaDong() {
        return danhSachTatCaDong;
    }

    public void setDanhSachTatCaDong(List<DongExcelKhachHangDTO> danhSachTatCaDong) {
        this.danhSachTatCaDong = danhSachTatCaDong;
    }
}
