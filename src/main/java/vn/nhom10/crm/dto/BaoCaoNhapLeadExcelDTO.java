package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO chứa báo cáo tổng hợp tiến trình xem trước và kết quả nhập Lead từ Excel.
 * Phục vụ Story S4-02:
 * - AC2: Xem trước, đếm số dòng hợp lệ/lỗi và báo cáo sau khi nhập.
 */
public class BaoCaoNhapLeadExcelDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String tenTep;
    private int tongSoDong = 0;
    private int soDongHopLe = 0;
    private int soDongLoi = 0;
    private int soDongThanhCong = 0;
    private int soDongThatBai = 0;

    private List<DongExcelLeadDTO> danhSachTatCaDong = new ArrayList<>();
    private List<DongExcelLeadDTO> danhSachDongHopLe = new ArrayList<>();
    private List<DongExcelLeadDTO> danhSachDongLoi = new ArrayList<>();

    private String thongDiep;
    private boolean daThucHienNhap = false;
    private Long dotNhapId;

    public BaoCaoNhapLeadExcelDTO() {
    }

    public BaoCaoNhapLeadExcelDTO(String tenTep) {
        this.tenTep = tenTep;
    }

    /**
     * Nạp danh sách các dòng đọc được từ Excel và tự động phân loại, tính toán thống kê.
     */
    public void napDanhSachDong(List<DongExcelLeadDTO> danhSach) {
        this.danhSachTatCaDong = (danhSach != null) ? danhSach : new ArrayList<>();
        this.danhSachDongHopLe.clear();
        this.danhSachDongLoi.clear();

        this.tongSoDong = this.danhSachTatCaDong.size();
        for (DongExcelLeadDTO dong : this.danhSachTatCaDong) {
            if (dong.isHopLe()) {
                this.danhSachDongHopLe.add(dong);
            } else {
                this.danhSachDongLoi.add(dong);
            }
        }
        this.soDongHopLe = this.danhSachDongHopLe.size();
        this.soDongLoi = this.danhSachDongLoi.size();
    }

    /**
     * Cập nhật kết quả sau khi thực hiện ghi vào cơ sở dữ liệu cho một dòng.
     */
    public void capNhatKetQuaNhap(DongExcelLeadDTO dong, boolean thanhCong) {
        dong.setDaNhap(true);
        dong.setThanhCong(thanhCong);
        if (thanhCong) {
            this.soDongThanhCong++;
        } else {
            this.soDongThatBai++;
        }
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

    public List<DongExcelLeadDTO> getDanhSachTatCaDong() {
        return danhSachTatCaDong;
    }

    public void setDanhSachTatCaDong(List<DongExcelLeadDTO> danhSachTatCaDong) {
        napDanhSachDong(danhSachTatCaDong);
    }

    public List<DongExcelLeadDTO> getDanhSachDongHopLe() {
        return danhSachDongHopLe;
    }

    public List<DongExcelLeadDTO> getDanhSachDongLoi() {
        return danhSachDongLoi;
    }

    public String getThongDiep() {
        return thongDiep;
    }

    public void setThongDiep(String thongDiep) {
        this.thongDiep = thongDiep;
    }

    public boolean isDaThucHienNhap() {
        return daThucHienNhap;
    }

    public void setDaThucHienNhap(boolean daThucHienNhap) {
        this.daThucHienNhap = daThucHienNhap;
    }

    public Long getDotNhapId() {
        return dotNhapId;
    }

    public void setDotNhapId(Long dotNhapId) {
        this.dotNhapId = dotNhapId;
    }
}
