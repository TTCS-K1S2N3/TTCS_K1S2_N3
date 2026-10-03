package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO đại diện cho báo cáo tổng kết xem trước và kết quả nhập Excel (Story S2-01).
 */
public class BaoCaoNhapExcelDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String tenTep;
    private int tongSoDong = 0;
    private int soDongHopLe = 0;
    private int soDongLoi = 0;
    private int soDongThanhCong = 0;
    private int soDongThatBai = 0;

    private Long dotNhapId;
    private boolean daThucHienNhap = false;
    private String thongDiep;

    private List<DongExcelNguoiDungDTO> danhSachTatCaDong = new ArrayList<>();

    public BaoCaoNhapExcelDTO() {
    }

    public BaoCaoNhapExcelDTO(String tenTep) {
        this.tenTep = tenTep;
    }

    /**
     * Nạp toàn bộ các dòng và cập nhật thống kê ban đầu (xem trước).
     */
    public void napDanhSachDong(List<DongExcelNguoiDungDTO> danhSach) {
        this.danhSachTatCaDong = (danhSach != null) ? danhSach : new ArrayList<>();
        this.tongSoDong = this.danhSachTatCaDong.size();
        this.soDongHopLe = (int) this.danhSachTatCaDong.stream().filter(DongExcelNguoiDungDTO::isHopLe).count();
        this.soDongLoi = this.tongSoDong - this.soDongHopLe;
    }

    /**
     * Cập nhật kết quả sau khi thực hiện nhập một dòng.
     */
    public void capNhatKetQuaNhap(DongExcelNguoiDungDTO dong, boolean thanhCong) {
        this.daThucHienNhap = true;
        if (thanhCong) {
            this.soDongThanhCong++;
        } else {
            this.soDongThatBai++;
        }
    }

    public List<DongExcelNguoiDungDTO> getDanhSachDongHopLe() {
        return danhSachTatCaDong.stream()
                .filter(DongExcelNguoiDungDTO::isHopLe)
                .collect(Collectors.toList());
    }

    public List<DongExcelNguoiDungDTO> getDanhSachDongLoi() {
        return danhSachTatCaDong.stream()
                .filter(d -> !d.isHopLe())
                .collect(Collectors.toList());
    }

    public List<DongExcelNguoiDungDTO> getDanhSachDongThanhCong() {
        return danhSachTatCaDong.stream()
                .filter(DongExcelNguoiDungDTO::isDaNhap)
                .collect(Collectors.toList());
    }

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

    public int getSoDongThanhCong() {
        return soDongThanhCong;
    }

    public void setSoDongThanhCong(int soDongThanhCong) {
        this.soDongThanhCong = soDongThanhCong;
    }

    public int getSoDongThatBai() {
        return soDongThatBai;
    }

    public void setSoDongThatBai(int soDongThatBai) {
        this.soDongThatBai = soDongThatBai;
    }

    public Long getDotNhapId() {
        return dotNhapId;
    }

    public void setDotNhapId(Long dotNhapId) {
        this.dotNhapId = dotNhapId;
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

    public List<DongExcelNguoiDungDTO> getDanhSachTatCaDong() {
        return danhSachTatCaDong;
    }

    public void setDanhSachTatCaDong(List<DongExcelNguoiDungDTO> danhSachTatCaDong) {
        this.danhSachTatCaDong = danhSachTatCaDong != null ? danhSachTatCaDong : new ArrayList<>();
    }
}
