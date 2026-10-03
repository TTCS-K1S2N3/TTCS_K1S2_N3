package vn.nhom10.crm.dto;

import vn.nhom10.crm.model.LoaiDanhMuc;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO đại diện cho một mục danh mục dùng chung của khối bán hàng (Story S2-07).
 */
public class MucDanhMucDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private LoaiDanhMuc loaiDanhMuc;
    private String maMuc;
    private String tenMuc;
    private String moTa;
    private int thuTuHienThi;
    private boolean kichHoat;
    private int soBanGhiDangSuDung;
    private LocalDate ngayTao;
    private String nguoiTao;

    public MucDanhMucDTO() {
        this.kichHoat = true;
        this.thuTuHienThi = 1;
        this.soBanGhiDangSuDung = 0;
        this.ngayTao = LocalDate.now();
    }

    public MucDanhMucDTO(Long id, LoaiDanhMuc loaiDanhMuc, String maMuc, String tenMuc,
                         String moTa, int thuTuHienThi, boolean kichHoat,
                         int soBanGhiDangSuDung, LocalDate ngayTao, String nguoiTao) {
        this.id = id;
        this.loaiDanhMuc = loaiDanhMuc;
        this.maMuc = maMuc;
        this.tenMuc = tenMuc;
        this.moTa = moTa;
        this.thuTuHienThi = thuTuHienThi;
        this.kichHoat = kichHoat;
        this.soBanGhiDangSuDung = soBanGhiDangSuDung;
        this.ngayTao = ngayTao;
        this.nguoiTao = nguoiTao;
    }

    public boolean isDangDuocSuDung() {
        return soBanGhiDangSuDung > 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LoaiDanhMuc getLoaiDanhMuc() {
        return loaiDanhMuc;
    }

    public void setLoaiDanhMuc(LoaiDanhMuc loaiDanhMuc) {
        this.loaiDanhMuc = loaiDanhMuc;
    }

    public String getMaMuc() {
        return maMuc;
    }

    public void setMaMuc(String maMuc) {
        this.maMuc = maMuc;
    }

    public String getTenMuc() {
        return tenMuc;
    }

    public void setTenMuc(String tenMuc) {
        this.tenMuc = tenMuc;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public int getThuTuHienThi() {
        return thuTuHienThi;
    }

    public void setThuTuHienThi(int thuTuHienThi) {
        this.thuTuHienThi = thuTuHienThi;
    }

    public boolean isKichHoat() {
        return kichHoat;
    }

    public void setKichHoat(boolean kichHoat) {
        this.kichHoat = kichHoat;
    }

    public int getSoBanGhiDangSuDung() {
        return soBanGhiDangSuDung;
    }

    public void setSoBanGhiDangSuDung(int soBanGhiDangSuDung) {
        this.soBanGhiDangSuDung = soBanGhiDangSuDung;
    }

    public LocalDate getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDate ngayTao) {
        this.ngayTao = ngayTao;
    }

    public String getNguoiTao() {
        return nguoiTao;
    }

    public void setNguoiTao(String nguoiTao) {
        this.nguoiTao = nguoiTao;
    }
}
