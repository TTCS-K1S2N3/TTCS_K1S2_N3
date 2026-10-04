package vn.nhom10.crm.dto;

import java.io.Serializable;

/**
 * DTO chứa số liệu thống kê tổng hợp nhật ký thay đổi dữ liệu nhạy cảm.
 */
public class ThongKeNhatKyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private long tongSoBanGhi;
    private long soThayDoiChietKhau;
    private long soThayDoiChiTieu;
    private long soThayDoiQuyenSoHuu;
    private long soThayDoiVaiTro;
    private long soNguoiThucHien;

    public ThongKeNhatKyDTO() {
    }

    public ThongKeNhatKyDTO(long tongSoBanGhi, long soThayDoiChietKhau, long soThayDoiChiTieu,
                           long soThayDoiQuyenSoHuu, long soThayDoiVaiTro, long soNguoiThucHien) {
        this.tongSoBanGhi = tongSoBanGhi;
        this.soThayDoiChietKhau = soThayDoiChietKhau;
        this.soThayDoiChiTieu = soThayDoiChiTieu;
        this.soThayDoiQuyenSoHuu = soThayDoiQuyenSoHuu;
        this.soThayDoiVaiTro = soThayDoiVaiTro;
        this.soNguoiThucHien = soNguoiThucHien;
    }

    public long getTongSoBanGhi() {
        return tongSoBanGhi;
    }

    public void setTongSoBanGhi(long tongSoBanGhi) {
        this.tongSoBanGhi = tongSoBanGhi;
    }

    public long getSoThayDoiChietKhau() {
        return soThayDoiChietKhau;
    }

    public void setSoThayDoiChietKhau(long soThayDoiChietKhau) {
        this.soThayDoiChietKhau = soThayDoiChietKhau;
    }

    public long getSoThayDoiChiTieu() {
        return soThayDoiChiTieu;
    }

    public void setSoThayDoiChiTieu(long soThayDoiChiTieu) {
        this.soThayDoiChiTieu = soThayDoiChiTieu;
    }

    public long getSoThayDoiQuyenSoHuu() {
        return soThayDoiQuyenSoHuu;
    }

    public void setSoThayDoiQuyenSoHuu(long soThayDoiQuyenSoHuu) {
        this.soThayDoiQuyenSoHuu = soThayDoiQuyenSoHuu;
    }

    public long getSoThayDoiVaiTro() {
        return soThayDoiVaiTro;
    }

    public void setSoThayDoiVaiTro(long soThayDoiVaiTro) {
        this.soThayDoiVaiTro = soThayDoiVaiTro;
    }

    public long getSoNguoiThucHien() {
        return soNguoiThucHien;
    }

    public void setSoNguoiThucHien(long soNguoiThucHien) {
        this.soNguoiThucHien = soNguoiThucHien;
    }
}
