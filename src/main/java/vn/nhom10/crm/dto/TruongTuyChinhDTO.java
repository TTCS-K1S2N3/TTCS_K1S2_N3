package vn.nhom10.crm.dto;

import vn.nhom10.crm.model.TruongTuyChinh;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object cho Trường tuỳ chỉnh (Story S2-08).
 * Dùng để bind dữ liệu form, kiểm tra hợp lệ và truyền dữ liệu ra view JSP.
 */
public class TruongTuyChinhDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String doiTuong;       // KHACH_HANG, CO_HOI
    private String tenTruong;      // Tên kỹ thuật: nguon_khach_hang
    private String nhanHien;       // Nhãn hiển thị: Nguồn khách hàng
    private String kieuDuLieu;     // VAN_BAN, SO, NGAY, DANH_SACH_CHON
    private boolean batBuoc = false;
    private boolean hienThiBoDac = true;
    private boolean hienThiExcel = true;
    private boolean dangHoatDong = true;
    private int thuTu = 1;
    private List<String> danhSachLuaChon = new ArrayList<>();

    public TruongTuyChinhDTO() {
    }

    public static TruongTuyChinhDTO tuModel(TruongTuyChinh model) {
        if (model == null) return null;
        TruongTuyChinhDTO dto = new TruongTuyChinhDTO();
        dto.setId(model.getId());
        dto.setDoiTuong(model.getLoaiDoiTuong());
        dto.setTenTruong(model.getMaTruong());
        dto.setNhanHien(model.getTenNhanGoc());
        dto.setKieuDuLieu(model.getKieuDuLieu());
        dto.setBatBuoc(model.isBatBuoc());
        dto.setHienThiBoDac(model.isHienThiBoDac());
        dto.setHienThiExcel(model.isHienThiExcel());
        dto.setDangHoatDong(model.isHoatDong());
        dto.setThuTu(model.getThuTuHienThi());
        dto.setDanhSachLuaChon(new ArrayList<>(model.getDanhSachLuaChon()));
        return dto;
    }

    public TruongTuyChinh chuyenSangModel() {
        TruongTuyChinh model = new TruongTuyChinh();
        model.setId(this.id);
        model.setLoaiDoiTuong(this.doiTuong);
        model.setMaTruong(this.tenTruong != null ? this.tenTruong.trim().toLowerCase() : "");
        model.setTenTruong(this.nhanHien != null ? this.nhanHien.trim() : "");
        model.setKieuDuLieu(this.kieuDuLieu);
        model.setBatBuoc(this.batBuoc);
        model.setHienThiBoDac(this.hienThiBoDac);
        model.setHienThiExcel(this.hienThiExcel);
        model.setHoatDong(this.dangHoatDong);
        model.setThuTuHienThi(this.thuTu);
        model.setDanhSachLuaChon(this.danhSachLuaChon);
        return model;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDoiTuong() {
        return doiTuong;
    }

    public void setDoiTuong(String doiTuong) {
        this.doiTuong = doiTuong;
    }

    public String getLoaiDoiTuong() {
        return doiTuong;
    }

    public void setLoaiDoiTuong(String loaiDoiTuong) {
        this.doiTuong = loaiDoiTuong;
    }

    public String getTenTruong() {
        return tenTruong;
    }

    public void setTenTruong(String tenTruong) {
        this.tenTruong = tenTruong;
    }

    public String getMaTruong() {
        return tenTruong;
    }

    public void setMaTruong(String maTruong) {
        this.tenTruong = maTruong;
    }

    public String getNhanHien() {
        return nhanHien;
    }

    public void setNhanHien(String nhanHien) {
        this.nhanHien = nhanHien;
    }

    public String getKieuDuLieu() {
        return kieuDuLieu;
    }

    public void setKieuDuLieu(String kieuDuLieu) {
        this.kieuDuLieu = kieuDuLieu;
    }

    public boolean isBatBuoc() {
        return batBuoc;
    }

    public void setBatBuoc(boolean batBuoc) {
        this.batBuoc = batBuoc;
    }

    public boolean isHienThiBoDac() {
        return hienThiBoDac;
    }

    public void setHienThiBoDac(boolean hienThiBoDac) {
        this.hienThiBoDac = hienThiBoDac;
    }

    public boolean isHienThiExcel() {
        return hienThiExcel;
    }

    public void setHienThiExcel(boolean hienThiExcel) {
        this.hienThiExcel = hienThiExcel;
    }

    public boolean isDangHoatDong() {
        return dangHoatDong;
    }

    public void setDangHoatDong(boolean dangHoatDong) {
        this.dangHoatDong = dangHoatDong;
    }

    public int getThuTu() {
        return thuTu;
    }

    public void setThuTu(int thuTu) {
        this.thuTu = thuTu;
    }

    public List<String> getDanhSachLuaChon() {
        if (danhSachLuaChon == null) {
            danhSachLuaChon = new ArrayList<>();
        }
        return danhSachLuaChon;
    }

    public void setDanhSachLuaChon(List<String> danhSachLuaChon) {
        this.danhSachLuaChon = danhSachLuaChon != null ? danhSachLuaChon : new ArrayList<>();
    }
}
