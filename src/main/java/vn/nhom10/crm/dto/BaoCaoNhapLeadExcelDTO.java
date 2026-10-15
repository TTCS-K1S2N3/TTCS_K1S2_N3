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
    private int soDongHopLeXemTruoc = 0;
    private int soDongLoiXemTruoc = 0;
    private int soDongThanhCong = 0;
    private int soDongThatBai = 0;
    private int soDongLoiLuuDb = 0;

    private List<DongExcelLeadDTO> danhSachTatCaDong = new ArrayList<>();
    private List<DongExcelLeadDTO> danhSachDongHopLe = new ArrayList<>();
    private List<DongExcelLeadDTO> danhSachDongLoi = new ArrayList<>();
    private List<DongExcelLeadDTO> danhSachDongThanhCong = new ArrayList<>();
    private List<DongExcelLeadDTO> danhSachDongLoiLuuDb = new ArrayList<>();

    private String thongDiep;
    private boolean daThucHienNhap = false;
    private Long dotNhapId;

    public BaoCaoNhapLeadExcelDTO() {
    }

    public BaoCaoNhapLeadExcelDTO(String tenTep) {
        this.tenTep = tenTep;
    }

    /**
     * Nạp danh sách các dòng đọc được từ Excel và tự động phân loại, tính toán thống kê xem trước.
     */
    public void napDanhSachDong(List<DongExcelLeadDTO> danhSach) {
        this.danhSachTatCaDong = (danhSach != null) ? danhSach : new ArrayList<>();
        this.danhSachDongHopLe.clear();
        this.danhSachDongLoi.clear();
        this.danhSachDongThanhCong.clear();
        this.danhSachDongLoiLuuDb.clear();

        this.tongSoDong = this.danhSachTatCaDong.size();
        for (DongExcelLeadDTO dong : this.danhSachTatCaDong) {
            if (dong.isHopLe()) {
                this.danhSachDongHopLe.add(dong);
            } else {
                this.danhSachDongLoi.add(dong);
            }
        }
        this.soDongHopLeXemTruoc = this.danhSachDongHopLe.size();
        this.soDongLoiXemTruoc = this.danhSachDongLoi.size();
        this.soDongHopLe = this.soDongHopLeXemTruoc;
        this.soDongLoi = this.soDongLoiXemTruoc;
    }

    /**
     * Khởi tạo bộ đếm và danh sách cho tiến trình ghi dữ liệu thực tế.
     */
    public void khoiTaoTienTrinhNhap() {
        this.danhSachDongThanhCong.clear();
        this.danhSachDongLoiLuuDb.clear();
        this.soDongThanhCong = 0;
        this.soDongLoiLuuDb = 0;
    }

    /**
     * Ghi nhận một dòng lưu thành công vào cơ sở dữ liệu.
     */
    public void ghiNhanThanhCong(DongExcelLeadDTO dong) {
        if (dong == null) return;
        dong.setThanhCong(true);
        dong.setDaNhap(true);
        if (!this.danhSachDongThanhCong.contains(dong)) {
            this.danhSachDongThanhCong.add(dong);
        }
        this.soDongThanhCong = this.danhSachDongThanhCong.size();
    }

    /**
     * Ghi nhận một dòng hợp lệ lúc xem trước nhưng thất bại khi lưu MySQL.
     * Không tính trùng nếu đã có trong danh sách lỗi.
     */
    public void ghiNhanLoiLuuDb(DongExcelLeadDTO dong) {
        if (dong == null) return;
        dong.setThanhCong(false);
        dong.setDaNhap(false);
        this.danhSachDongHopLe.remove(dong);
        if (!this.danhSachDongLoiLuuDb.contains(dong)) {
            this.danhSachDongLoiLuuDb.add(dong);
        }
        if (!this.danhSachDongLoi.contains(dong)) {
            this.danhSachDongLoi.add(dong);
        }
        this.soDongLoiLuuDb = this.danhSachDongLoiLuuDb.size();
    }

    /**
     * Hoàn tất tiến trình nhập và đồng bộ các chỉ số thống kê thực tế.
     */
    public void hoanTatTienTrinhNhap() {
        this.soDongThanhCong = this.danhSachDongThanhCong.size();
        this.soDongLoiLuuDb = this.danhSachDongLoiLuuDb.size();
        this.soDongThatBai = this.danhSachDongLoi.size();
        this.soDongLoi = this.soDongThatBai;
        this.soDongHopLe = this.soDongThanhCong;
    }

    /**
     * Cập nhật kết quả sau khi thực hiện ghi vào cơ sở dữ liệu cho một dòng (tương thích ngược).
     */
    public void capNhatKetQuaNhap(DongExcelLeadDTO dong, boolean thanhCong) {
        if (thanhCong) {
            ghiNhanThanhCong(dong);
        } else {
            ghiNhanLoiLuuDb(dong);
        }
        hoanTatTienTrinhNhap();
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

    public int getSoDongHopLeXemTruoc() {
        return soDongHopLeXemTruoc;
    }

    public void setSoDongHopLeXemTruoc(int soDongHopLeXemTruoc) {
        this.soDongHopLeXemTruoc = soDongHopLeXemTruoc;
    }

    public int getSoDongLoiXemTruoc() {
        return soDongLoiXemTruoc;
    }

    public void setSoDongLoiXemTruoc(int soDongLoiXemTruoc) {
        this.soDongLoiXemTruoc = soDongLoiXemTruoc;
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

    public int getSoDongLoiLuuDb() {
        return soDongLoiLuuDb;
    }

    public void setSoDongLoiLuuDb(int soDongLoiLuuDb) {
        this.soDongLoiLuuDb = soDongLoiLuuDb;
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

    public List<DongExcelLeadDTO> getDanhSachDongThanhCong() {
        return danhSachDongThanhCong;
    }

    public List<DongExcelLeadDTO> getDanhSachDongLoiLuuDb() {
        return danhSachDongLoiLuuDb;
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
