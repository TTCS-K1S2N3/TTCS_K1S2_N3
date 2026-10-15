package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO kết quả thực hiện giao dịch chuyển đổi Lead (Story S4-08).
 * Chứa thông tin các bản ghi được sinh ra trong cùng một atomic transaction:
 * - Khách hàng (khach_hang)
 * - Người liên hệ (nguoi_lien_he)
 * - Cơ hội bán hàng (co_hoi)
 * - Số lượng hoạt động đã bảo toàn (hoat_dong)
 */
public class KetQuaChuyenDoiLeadDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean thanhCong;
    private Long leadId;
    private Long khachHangId;
    private Long nguoiLienHeId;
    private Long coHoiId;
    private String trangThaiLead;
    private int soHoatDongBaoToan;
    private LocalDateTime thoiDiemChuyenDoi;
    private String maLoi;
    private String thongBao;
    private Map<String, String> loiTruong = new HashMap<>();

    public KetQuaChuyenDoiLeadDTO() {
    }

    public static KetQuaChuyenDoiLeadDTO thanhCong(Long leadId, Long khachHangId, Long nguoiLienHeId, Long coHoiId, int soHoatDong, LocalDateTime thoiDiem) {
        KetQuaChuyenDoiLeadDTO kq = new KetQuaChuyenDoiLeadDTO();
        kq.setThanhCong(true);
        kq.setLeadId(leadId);
        kq.setKhachHangId(khachHangId);
        kq.setNguoiLienHeId(nguoiLienHeId);
        kq.setCoHoiId(coHoiId);
        kq.setTrangThaiLead("DA_CHUYEN_DOI");
        kq.setSoHoatDongBaoToan(soHoatDong);
        kq.setThoiDiemChuyenDoi(thoiDiem);
        kq.setThongBao("Chuyển đổi lead thành công");
        return kq;
    }

    public static KetQuaChuyenDoiLeadDTO thatBai(String maLoi, String thongBao) {
        KetQuaChuyenDoiLeadDTO kq = new KetQuaChuyenDoiLeadDTO();
        kq.setThanhCong(false);
        kq.setMaLoi(maLoi);
        kq.setThongBao(thongBao);
        return kq;
    }

    public static KetQuaChuyenDoiLeadDTO thatBai(String maLoi, String thongBao, Map<String, String> loiTruong) {
        KetQuaChuyenDoiLeadDTO kq = thatBai(maLoi, thongBao);
        if (loiTruong != null) {
            kq.setLoiTruong(loiTruong);
        }
        return kq;
    }

    public boolean isThanhCong() {
        return thanhCong;
    }

    public void setThanhCong(boolean thanhCong) {
        this.thanhCong = thanhCong;
    }

    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

    public Long getKhachHangId() {
        return khachHangId;
    }

    public void setKhachHangId(Long khachHangId) {
        this.khachHangId = khachHangId;
    }

    public Long getNguoiLienHeId() {
        return nguoiLienHeId;
    }

    public void setNguoiLienHeId(Long nguoiLienHeId) {
        this.nguoiLienHeId = nguoiLienHeId;
    }

    public Long getCoHoiId() {
        return coHoiId;
    }

    public void setCoHoiId(Long coHoiId) {
        this.coHoiId = coHoiId;
    }

    public String getTrangThaiLead() {
        return trangThaiLead;
    }

    public void setTrangThaiLead(String trangThaiLead) {
        this.trangThaiLead = trangThaiLead;
    }

    public int getSoHoatDongBaoToan() {
        return soHoatDongBaoToan;
    }

    public void setSoHoatDongBaoToan(int soHoatDongBaoToan) {
        this.soHoatDongBaoToan = soHoatDongBaoToan;
    }

    public LocalDateTime getThoiDiemChuyenDoi() {
        return thoiDiemChuyenDoi;
    }

    public void setThoiDiemChuyenDoi(LocalDateTime thoiDiemChuyenDoi) {
        this.thoiDiemChuyenDoi = thoiDiemChuyenDoi;
    }

    public String getMaLoi() {
        return maLoi;
    }

    public void setMaLoi(String maLoi) {
        this.maLoi = maLoi;
    }

    public String getThongBao() {
        return thongBao;
    }

    public void setThongBao(String thongBao) {
        this.thongBao = thongBao;
    }

    public Map<String, String> getLoiTruong() {
        return loiTruong;
    }

    public void setLoiTruong(Map<String, String> loiTruong) {
        this.loiTruong = loiTruong;
    }
}
