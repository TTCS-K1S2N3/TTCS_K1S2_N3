package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * DTO chứa điều kiện lọc nhật ký thay đổi trên dữ liệu nhạy cảm (Story S2-04).
 * Tiêu chí: Lọc theo người dùng, loại đối tượng, khoảng thời gian, từ khóa tìm kiếm và phân trang.
 */
public class BoLocNhatKyDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private Long nguoiDungId;
    private String loaiDoiTuong;
    private LocalDate tuNgay;
    private LocalDate denNgay;
    private String tuKhoa;
    private String quickPeriod; // "all", "today", "week", "month", "quarter"
    private int trang = 1;
    private int soBanGhiTrenTrang = 10;

    public BoLocNhatKyDTO() {
    }

    public Long getNguoiDungId() {
        return nguoiDungId;
    }

    public void setNguoiDungId(Long nguoiDungId) {
        this.nguoiDungId = nguoiDungId;
    }

    public String getLoaiDoiTuong() {
        return loaiDoiTuong;
    }

    public void setLoaiDoiTuong(String loaiDoiTuong) {
        this.loaiDoiTuong = (loaiDoiTuong != null && !loaiDoiTuong.trim().isEmpty()) ? loaiDoiTuong.trim() : null;
    }

    public LocalDate getTuNgay() {
        return tuNgay;
    }

    public void setTuNgay(LocalDate tuNgay) {
        this.tuNgay = tuNgay;
    }

    public LocalDate getDenNgay() {
        return denNgay;
    }

    public void setDenNgay(LocalDate denNgay) {
        this.denNgay = denNgay;
    }

    public String getTuKhoa() {
        return tuKhoa;
    }

    public void setTuKhoa(String tuKhoa) {
        this.tuKhoa = (tuKhoa != null && !tuKhoa.trim().isEmpty()) ? tuKhoa.trim() : null;
    }

    public String getQuickPeriod() {
        return quickPeriod;
    }

    public void setQuickPeriod(String quickPeriod) {
        this.quickPeriod = quickPeriod;
    }

    public int getTrang() {
        return trang <= 0 ? 1 : trang;
    }

    public void setTrang(int trang) {
        this.trang = trang <= 0 ? 1 : trang;
    }

    public int getSoBanGhiTrenTrang() {
        return soBanGhiTrenTrang <= 0 ? 10 : soBanGhiTrenTrang;
    }

    public void setSoBanGhiTrenTrang(int soBanGhiTrenTrang) {
        this.soBanGhiTrenTrang = soBanGhiTrenTrang <= 0 ? 10 : soBanGhiTrenTrang;
    }

    public boolean isKhoangThoiGianHopLe() {
        if (tuNgay != null && denNgay != null) {
            return !tuNgay.isAfter(denNgay);
        }
        return true;
    }

    public boolean coBoLoc() {
        return nguoiDungId != null
                || (loaiDoiTuong != null && !loaiDoiTuong.isEmpty())
                || tuNgay != null
                || denNgay != null
                || (tuKhoa != null && !tuKhoa.isEmpty());
    }

    public String getTuNgayChuoi() {
        return tuNgay != null ? tuNgay.format(DATE_FORMATTER) : "";
    }

    public String getDenNgayChuoi() {
        return denNgay != null ? denNgay.format(DATE_FORMATTER) : "";
    }
}
