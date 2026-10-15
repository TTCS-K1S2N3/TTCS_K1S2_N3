package vn.nhom10.crm.dto;

import vn.nhom10.crm.model.KieuPhanBoEnum;

import java.time.LocalDateTime;

/**
 * Data Transfer Object phục vụ hiển thị danh sách quy tắc phân bổ trên giao diện JSP/JSON (Story S4-06).
 */
public class QuyTacPhanBoDTO {

    private Long id;
    private String maQuyTac;
    private String tenQuyTac;
    private int thuTuUuTien;
    private KieuPhanBoEnum kieuPhanBo;
    private String kieuPhanBoTenHienThi;
    private String dieuKienJson;
    private String dieuKienMoTa;
    private Long nhomDichId;
    private String tenNhomDich;
    private Long nguoiDungDichId;
    private String tenNguoiDungDich;
    private Long nguoiDungLanCuoiId;
    private String tenNguoiDungLanCuoi;
    private boolean hoatDong;
    private LocalDateTime updatedAt;

    public QuyTacPhanBoDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaQuyTac() {
        return maQuyTac;
    }

    public void setMaQuyTac(String maQuyTac) {
        this.maQuyTac = maQuyTac;
    }

    public String getTenQuyTac() {
        return tenQuyTac;
    }

    public void setTenQuyTac(String tenQuyTac) {
        this.tenQuyTac = tenQuyTac;
    }

    public int getThuTuUuTien() {
        return thuTuUuTien;
    }

    public void setThuTuUuTien(int thuTuUuTien) {
        this.thuTuUuTien = thuTuUuTien;
    }

    public KieuPhanBoEnum getKieuPhanBo() {
        return kieuPhanBo;
    }

    public void setKieuPhanBo(KieuPhanBoEnum kieuPhanBo) {
        this.kieuPhanBo = kieuPhanBo;
    }

    public String getKieuPhanBoTenHienThi() {
        return kieuPhanBoTenHienThi;
    }

    public void setKieuPhanBoTenHienThi(String kieuPhanBoTenHienThi) {
        this.kieuPhanBoTenHienThi = kieuPhanBoTenHienThi;
    }

    public String getDieuKienJson() {
        return dieuKienJson;
    }

    public void setDieuKienJson(String dieuKienJson) {
        this.dieuKienJson = dieuKienJson;
    }

    public String getDieuKienMoTa() {
        return dieuKienMoTa;
    }

    public void setDieuKienMoTa(String dieuKienMoTa) {
        this.dieuKienMoTa = dieuKienMoTa;
    }

    public Long getNhomDichId() {
        return nhomDichId;
    }

    public void setNhomDichId(Long nhomDichId) {
        this.nhomDichId = nhomDichId;
    }

    public String getTenNhomDich() {
        return tenNhomDich;
    }

    public void setTenNhomDich(String tenNhomDich) {
        this.tenNhomDich = tenNhomDich;
    }

    public Long getNguoiDungDichId() {
        return nguoiDungDichId;
    }

    public void setNguoiDungDichId(Long nguoiDungDichId) {
        this.nguoiDungDichId = nguoiDungDichId;
    }

    public String getTenNguoiDungDich() {
        return tenNguoiDungDich;
    }

    public void setTenNguoiDungDich(String tenNguoiDungDich) {
        this.tenNguoiDungDich = tenNguoiDungDich;
    }

    public Long getNguoiDungLanCuoiId() {
        return nguoiDungLanCuoiId;
    }

    public void setNguoiDungLanCuoiId(Long nguoiDungLanCuoiId) {
        this.nguoiDungLanCuoiId = nguoiDungLanCuoiId;
    }

    public String getTenNguoiDungLanCuoi() {
        return tenNguoiDungLanCuoi;
    }

    public void setTenNguoiDungLanCuoi(String tenNguoiDungLanCuoi) {
        this.tenNguoiDungLanCuoi = tenNguoiDungLanCuoi;
    }

    public boolean isHoatDong() {
        return hoatDong;
    }

    public void setHoatDong(boolean hoatDong) {
        this.hoatDong = hoatDong;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
