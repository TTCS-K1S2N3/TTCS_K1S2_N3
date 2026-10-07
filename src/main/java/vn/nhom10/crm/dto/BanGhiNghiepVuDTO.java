package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO đại diện cho các bản ghi nghiệp vụ trong CRM chịu sự chi phối của Data Scope (Story S1-05):
 * - Khách hàng (Customer)
 * - Cơ hội bán hàng (Opportunity)
 * - Báo giá (Quote)
 * - Hoạt động (Activity)
 */
public class BanGhiNghiepVuDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum LoaiNghiepVu {
        KHACH_HANG("KHACH_HANG", "Khách hàng"),
        CO_HOI("CO_HOI", "Cơ hội bán hàng"),
        BAO_GIA("BAO_GIA", "Báo giá"),
        HOAT_DONG("HOAT_DONG", "Hoạt động chăm sóc");

        private final String ma;
        private final String tenHienThi;

        LoaiNghiepVu(String ma, String tenHienThi) {
            this.ma = ma;
            this.tenHienThi = tenHienThi;
        }

        public String getMa() {
            return ma;
        }

        public String getTenHienThi() {
            return tenHienThi;
        }
    }

    private Long id;
    private String maBanGhi;
    private String tieuDe;
    private LoaiNghiepVu loaiNghiepVu;
    private Long nguoiPhuTrachId;
    private String tenNguoiPhuTrach;
    private Long nhomKinhDoanhId;
    private String tenNhom;
    private String giaTri;
    private String trangThai;
    private LocalDate ngayTao;
    private String moTaChiTiet;
    private String maSoThue;
    private String website;

    public BanGhiNghiepVuDTO() {
    }

    public BanGhiNghiepVuDTO(Long id, String maBanGhi, String tieuDe, LoaiNghiepVu loaiNghiepVu,
                            Long nguoiPhuTrachId, String tenNguoiPhuTrach,
                            Long nhomKinhDoanhId, String tenNhom,
                            String giaTri, String trangThai, LocalDate ngayTao, String moTaChiTiet) {
        this.id = id;
        this.maBanGhi = maBanGhi;
        this.tieuDe = tieuDe;
        this.loaiNghiepVu = loaiNghiepVu;
        this.nguoiPhuTrachId = nguoiPhuTrachId;
        this.tenNguoiPhuTrach = tenNguoiPhuTrach;
        this.nhomKinhDoanhId = nhomKinhDoanhId;
        this.tenNhom = tenNhom;
        this.giaTri = giaTri;
        this.trangThai = trangThai;
        this.ngayTao = ngayTao;
        this.moTaChiTiet = moTaChiTiet;
    }

    public BanGhiNghiepVuDTO(Long id, String maBanGhi, String tieuDe, LoaiNghiepVu loaiNghiepVu,
                            Long nguoiPhuTrachId, String tenNguoiPhuTrach,
                            Long nhomKinhDoanhId, String tenNhom,
                            String giaTri, String trangThai, LocalDate ngayTao, String moTaChiTiet,
                            String maSoThue, String website) {
        this(id, maBanGhi, tieuDe, loaiNghiepVu, nguoiPhuTrachId, tenNguoiPhuTrach, nhomKinhDoanhId, tenNhom, giaTri, trangThai, ngayTao, moTaChiTiet);
        this.maSoThue = maSoThue;
        this.website = website;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaBanGhi() {
        return maBanGhi;
    }

    public void setMaBanGhi(String maBanGhi) {
        this.maBanGhi = maBanGhi;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public LoaiNghiepVu getLoaiNghiepVu() {
        return loaiNghiepVu;
    }

    public void setLoaiNghiepVu(LoaiNghiepVu loaiNghiepVu) {
        this.loaiNghiepVu = loaiNghiepVu;
    }

    public Long getNguoiPhuTrachId() {
        return nguoiPhuTrachId;
    }

    public void setNguoiPhuTrachId(Long nguoiPhuTrachId) {
        this.nguoiPhuTrachId = nguoiPhuTrachId;
    }

    public String getTenNguoiPhuTrach() {
        return tenNguoiPhuTrach;
    }

    public void setTenNguoiPhuTrach(String tenNguoiPhuTrach) {
        this.tenNguoiPhuTrach = tenNguoiPhuTrach;
    }

    public Long getNhomKinhDoanhId() {
        return nhomKinhDoanhId;
    }

    public void setNhomKinhDoanhId(Long nhomKinhDoanhId) {
        this.nhomKinhDoanhId = nhomKinhDoanhId;
    }

    public String getTenNhom() {
        return tenNhom;
    }

    public void setTenNhom(String tenNhom) {
        this.tenNhom = tenNhom;
    }

    public String getGiaTri() {
        return giaTri;
    }

    public void setGiaTri(String giaTri) {
        this.giaTri = giaTri;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDate getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDate ngayTao) {
        this.ngayTao = ngayTao;
    }

    public String getMoTaChiTiet() {
        return moTaChiTiet;
    }

    public void setMoTaChiTiet(String moTaChiTiet) {
        this.moTaChiTiet = moTaChiTiet;
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
}
