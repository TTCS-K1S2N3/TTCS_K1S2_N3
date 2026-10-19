package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO mô phỏng và kiểm tra tính hợp lệ của quy tắc nghiệp vụ Sprint 5 (Story S2-10 AC3 & S5-05).
 * Kiểm tra ràng buộc bắt buộc khi đóng cơ hội bán hàng:
 * - Đóng Thắng: Bắt buộc chọn Lý do thắng, nhập Giá trị chốt thực tế > 0 và Ngày ký hợp lệ.
 * - Đóng Thua: Bắt buộc chọn Lý do thua và Đối thủ cạnh tranh thắng thầu nếu có.
 */
public class KetQuaKiemTraDongCoHoiDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String trangThaiDong; // "THANG" hoặc "THUA"
    private Long lyDoThangThuaId;
    private String tenLyDo;
    private Long doiThuId;
    private String tenDoiThu;
    private BigDecimal giaTriChotThucTe;
    private LocalDate ngayKy;
    private String ghiChu;

    private boolean hopLe;
    private List<String> danhSachLoi;
    private String thongBaoChiTiet;

    public KetQuaKiemTraDongCoHoiDTO() {
        this.danhSachLoi = new ArrayList<>();
        this.hopLe = false;
    }

    public void themLoi(String loi) {
        if (this.danhSachLoi == null) {
            this.danhSachLoi = new ArrayList<>();
        }
        this.danhSachLoi.add(loi);
        this.hopLe = false;
    }

    public String getTrangThaiDong() {
        return trangThaiDong;
    }

    public void setTrangThaiDong(String trangThaiDong) {
        this.trangThaiDong = trangThaiDong;
    }

    public Long getLyDoThangThuaId() {
        return lyDoThangThuaId;
    }

    public void setLyDoThangThuaId(Long lyDoThangThuaId) {
        this.lyDoThangThuaId = lyDoThangThuaId;
    }

    public String getTenLyDo() {
        return tenLyDo;
    }

    public void setTenLyDo(String tenLyDo) {
        this.tenLyDo = tenLyDo;
    }

    public Long getDoiThuId() {
        return doiThuId;
    }

    public void setDoiThuId(Long doiThuId) {
        this.doiThuId = doiThuId;
    }

    public String getTenDoiThu() {
        return tenDoiThu;
    }

    public void setTenDoiThu(String tenDoiThu) {
        this.tenDoiThu = tenDoiThu;
    }

    public BigDecimal getGiaTriChotThucTe() {
        return giaTriChotThucTe;
    }

    public void setGiaTriChotThucTe(BigDecimal giaTriChotThucTe) {
        this.giaTriChotThucTe = giaTriChotThucTe;
    }

    public LocalDate getNgayKy() {
        return ngayKy;
    }

    public void setNgayKy(LocalDate ngayKy) {
        this.ngayKy = ngayKy;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public boolean isHopLe() {
        return hopLe;
    }

    public void setHopLe(boolean hopLe) {
        this.hopLe = hopLe;
    }

    public List<String> getDanhSachLoi() {
        return danhSachLoi;
    }

    public void setDanhSachLoi(List<String> danhSachLoi) {
        this.danhSachLoi = danhSachLoi;
    }

    public String getThongBaoChiTiet() {
        return thongBaoChiTiet;
    }

    public void setThongBaoChiTiet(String thongBaoChiTiet) {
        this.thongBaoChiTiet = thongBaoChiTiet;
    }
}
