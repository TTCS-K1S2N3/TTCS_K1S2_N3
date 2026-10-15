package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO chứa toàn bộ dữ liệu xem trước chỉ đọc phục vụ GET /lead/chuyen-doi (Story S4-08).
 * Đáp ứng AC1, AC2 và hợp đồng dữ liệu giao tiếp với JSP.
 */
public class ChuyenDoiLeadPreviewDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private LeadChuyenDoiViewDTO leadChuyenDoi;
    private KhachHangPreviewDTO thongTinTaoKhachHang;
    private NguoiLienHePreviewDTO thongTinTaoLienHe;
    private CoHoiPreviewDTO thongTinTaoCoHoi;
    private List<GiaiDoanOptionDTO> danhSachGiaiDoan = new ArrayList<>();
    private boolean quyenChuyenDoi;
    private Map<String, String> loiTruong = new HashMap<>();
    private List<CanhBaoTrungDTO> canhBaoTrung = new ArrayList<>();
    private String thongBao;
    private String csrfToken;

    public ChuyenDoiLeadPreviewDTO() {
    }

    public LeadChuyenDoiViewDTO getLeadChuyenDoi() {
        return leadChuyenDoi;
    }

    public void setLeadChuyenDoi(LeadChuyenDoiViewDTO leadChuyenDoi) {
        this.leadChuyenDoi = leadChuyenDoi;
    }

    public KhachHangPreviewDTO getThongTinTaoKhachHang() {
        return thongTinTaoKhachHang;
    }

    public void setThongTinTaoKhachHang(KhachHangPreviewDTO thongTinTaoKhachHang) {
        this.thongTinTaoKhachHang = thongTinTaoKhachHang;
    }

    public NguoiLienHePreviewDTO getThongTinTaoLienHe() {
        return thongTinTaoLienHe;
    }

    public void setThongTinTaoLienHe(NguoiLienHePreviewDTO thongTinTaoLienHe) {
        this.thongTinTaoLienHe = thongTinTaoLienHe;
    }

    public CoHoiPreviewDTO getThongTinTaoCoHoi() {
        return thongTinTaoCoHoi;
    }

    public void setThongTinTaoCoHoi(CoHoiPreviewDTO thongTinTaoCoHoi) {
        this.thongTinTaoCoHoi = thongTinTaoCoHoi;
    }

    public List<GiaiDoanOptionDTO> getDanhSachGiaiDoan() {
        return danhSachGiaiDoan;
    }

    public void setDanhSachGiaiDoan(List<GiaiDoanOptionDTO> danhSachGiaiDoan) {
        this.danhSachGiaiDoan = danhSachGiaiDoan;
    }

    public boolean isQuyenChuyenDoi() {
        return quyenChuyenDoi;
    }

    public void setQuyenChuyenDoi(boolean quyenChuyenDoi) {
        this.quyenChuyenDoi = quyenChuyenDoi;
    }

    public Map<String, String> getLoiTruong() {
        return loiTruong;
    }

    public void setLoiTruong(Map<String, String> loiTruong) {
        this.loiTruong = loiTruong;
    }

    public List<CanhBaoTrungDTO> getCanhBaoTrung() {
        return canhBaoTrung;
    }

    public void setCanhBaoTrung(List<CanhBaoTrungDTO> canhBaoTrung) {
        this.canhBaoTrung = canhBaoTrung;
    }

    public String getThongBao() {
        return thongBao;
    }

    public void setThongBao(String thongBao) {
        this.thongBao = thongBao;
    }

    public String getCsrfToken() {
        return csrfToken;
    }

    public void setCsrfToken(String csrfToken) {
        this.csrfToken = csrfToken;
    }

    // =========================================================================
    // INNER STATIC DTOs
    // =========================================================================

    public static class LeadChuyenDoiViewDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long leadId;
        private String maLead;
        private String trangThaiLead;
        private String tenCongTy;
        private String hoTen;
        private String email;
        private String soDienThoai;
        private String nguon;
        private String nhuCau;
        private Long chuSoHuuId;
        private String tenChuSoHuu;
        private int activityCount;
        private boolean canConvert;
        private List<String> canConvertReasons = new ArrayList<>();

        public LeadChuyenDoiViewDTO() {
        }

        public Long getLeadId() {
            return leadId;
        }

        public void setLeadId(Long leadId) {
            this.leadId = leadId;
        }

        public String getMaLead() {
            return maLead;
        }

        public void setMaLead(String maLead) {
            this.maLead = maLead;
        }

        public String getTrangThaiLead() {
            return trangThaiLead;
        }

        public void setTrangThaiLead(String trangThaiLead) {
            this.trangThaiLead = trangThaiLead;
        }

        public String getTenCongTy() {
            return tenCongTy;
        }

        public void setTenCongTy(String tenCongTy) {
            this.tenCongTy = tenCongTy;
        }

        public String getHoTen() {
            return hoTen;
        }

        public void setHoTen(String hoTen) {
            this.hoTen = hoTen;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getSoDienThoai() {
            return soDienThoai;
        }

        public void setSoDienThoai(String soDienThoai) {
            this.soDienThoai = soDienThoai;
        }

        public String getNguon() {
            return nguon;
        }

        public void setNguon(String nguon) {
            this.nguon = nguon;
        }

        public String getNhuCau() {
            return nhuCau;
        }

        public void setNhuCau(String nhuCau) {
            this.nhuCau = nhuCau;
        }

        public Long getChuSoHuuId() {
            return chuSoHuuId;
        }

        public void setChuSoHuuId(Long chuSoHuuId) {
            this.chuSoHuuId = chuSoHuuId;
        }

        public String getTenChuSoHuu() {
            return tenChuSoHuu;
        }

        public void setTenChuSoHuu(String tenChuSoHuu) {
            this.tenChuSoHuu = tenChuSoHuu;
        }

        public int getActivityCount() {
            return activityCount;
        }

        public void setActivityCount(int activityCount) {
            this.activityCount = activityCount;
        }

        public boolean isCanConvert() {
            return canConvert;
        }

        public void setCanConvert(boolean canConvert) {
            this.canConvert = canConvert;
        }

        public List<String> getCanConvertReasons() {
            return canConvertReasons;
        }

        public void setCanConvertReasons(List<String> canConvertReasons) {
            this.canConvertReasons = canConvertReasons;
        }
    }

    public static class KhachHangPreviewDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private String tenCongTy;
        private String maSoThue;
        private String website;
        private String diaChi;
        private Long nganhNgheId;
        private Long quyMoId;
        private Long khuVucId;
        private Long nguoiSoHuuId;
        private Long nhomKinhDoanhId;

        public KhachHangPreviewDTO() {
        }

        public String getTenCongTy() {
            return tenCongTy;
        }

        public void setTenCongTy(String tenCongTy) {
            this.tenCongTy = tenCongTy;
        }

        public String getMaSoThue() {
            return maSoThue;
        }

        public void setMaSoThue(String maSoThue) {
            this.maSoThue = maSoThue;
        }

        public String getWebsite() {
            return website;
        }

        public void setWebsite(String website) {
            this.website = website;
        }

        public String getDiaChi() {
            return diaChi;
        }

        public void setDiaChi(String diaChi) {
            this.diaChi = diaChi;
        }

        public Long getNganhNgheId() {
            return nganhNgheId;
        }

        public void setNganhNgheId(Long nganhNgheId) {
            this.nganhNgheId = nganhNgheId;
        }

        public Long getQuyMoId() {
            return quyMoId;
        }

        public void setQuyMoId(Long quyMoId) {
            this.quyMoId = quyMoId;
        }

        public Long getKhuVucId() {
            return khuVucId;
        }

        public void setKhuVucId(Long khuVucId) {
            this.khuVucId = khuVucId;
        }

        public Long getNguoiSoHuuId() {
            return nguoiSoHuuId;
        }

        public void setNguoiSoHuuId(Long nguoiSoHuuId) {
            this.nguoiSoHuuId = nguoiSoHuuId;
        }

        public Long getNhomKinhDoanhId() {
            return nhomKinhDoanhId;
        }

        public void setNhomKinhDoanhId(Long nhomKinhDoanhId) {
            this.nhomKinhDoanhId = nhomKinhDoanhId;
        }
    }

    public static class NguoiLienHePreviewDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private String hoTen;
        private String chucDanh;
        private String email;
        private String soDienThoai;
        private boolean laDauMoiChinh = true;

        public NguoiLienHePreviewDTO() {
        }

        public String getHoTen() {
            return hoTen;
        }

        public void setHoTen(String hoTen) {
            this.hoTen = hoTen;
        }

        public String getChucDanh() {
            return chucDanh;
        }

        public void setChucDanh(String chucDanh) {
            this.chucDanh = chucDanh;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getSoDienThoai() {
            return soDienThoai;
        }

        public void setSoDienThoai(String soDienThoai) {
            this.soDienThoai = soDienThoai;
        }

        public boolean isLaDauMoiChinh() {
            return laDauMoiChinh;
        }

        public void setLaDauMoiChinh(boolean laDauMoiChinh) {
            this.laDauMoiChinh = laDauMoiChinh;
        }
    }

    public static class CoHoiPreviewDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private String tenCoHoi;
        private Long giaiDoanId;
        private String tenGiaiDoan;
        private BigDecimal giaTriDuKien = BigDecimal.ZERO;
        private LocalDate ngayChotDuKien;
        private String nguon;
        private Long nguoiPhuTrachId;

        public CoHoiPreviewDTO() {
        }

        public String getTenCoHoi() {
            return tenCoHoi;
        }

        public void setTenCoHoi(String tenCoHoi) {
            this.tenCoHoi = tenCoHoi;
        }

        public Long getGiaiDoanId() {
            return giaiDoanId;
        }

        public void setGiaiDoanId(Long giaiDoanId) {
            this.giaiDoanId = giaiDoanId;
        }

        public String getTenGiaiDoan() {
            return tenGiaiDoan;
        }

        public void setTenGiaiDoan(String tenGiaiDoan) {
            this.tenGiaiDoan = tenGiaiDoan;
        }

        public BigDecimal getGiaTriDuKien() {
            return giaTriDuKien;
        }

        public void setGiaTriDuKien(BigDecimal giaTriDuKien) {
            this.giaTriDuKien = giaTriDuKien;
        }

        public LocalDate getNgayChotDuKien() {
            return ngayChotDuKien;
        }

        public void setNgayChotDuKien(LocalDate ngayChotDuKien) {
            this.ngayChotDuKien = ngayChotDuKien;
        }

        public String getNguon() {
            return nguon;
        }

        public void setNguon(String nguon) {
            this.nguon = nguon;
        }

        public Long getNguoiPhuTrachId() {
            return nguoiPhuTrachId;
        }

        public void setNguoiPhuTrachId(Long nguoiPhuTrachId) {
            this.nguoiPhuTrachId = nguoiPhuTrachId;
        }
    }
}
