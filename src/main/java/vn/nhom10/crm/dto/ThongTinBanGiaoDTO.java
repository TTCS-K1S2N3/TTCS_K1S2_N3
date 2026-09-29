package vn.nhom10.crm.dto;

import vn.nhom10.crm.model.NguoiDung;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO chứa thông tin hiện tại của nhân viên sắp bị khoá để chuẩn bị bàn giao.
 */
public class ThongTinBanGiaoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private NguoiDung nguoiBiKhoa;
    private int soKhachHangHienTai;
    private int soCoHoiHienTai;
    private List<NguoiDung> danhSachNguoiTiepNhan = new ArrayList<>();

    public ThongTinBanGiaoDTO() {
    }

    public ThongTinBanGiaoDTO(NguoiDung nguoiBiKhoa, int soKhachHangHienTai, int soCoHoiHienTai, List<NguoiDung> danhSachNguoiTiepNhan) {
        this.nguoiBiKhoa = nguoiBiKhoa;
        this.soKhachHangHienTai = soKhachHangHienTai;
        this.soCoHoiHienTai = soCoHoiHienTai;
        this.danhSachNguoiTiepNhan = danhSachNguoiTiepNhan != null ? danhSachNguoiTiepNhan : new ArrayList<>();
    }

    public NguoiDung getNguoiBiKhoa() {
        return nguoiBiKhoa;
    }

    public void setNguoiBiKhoa(NguoiDung nguoiBiKhoa) {
        this.nguoiBiKhoa = nguoiBiKhoa;
    }

    public int getSoKhachHangHienTai() {
        return soKhachHangHienTai;
    }

    public void setSoKhachHangHienTai(int soKhachHangHienTai) {
        this.soKhachHangHienTai = soKhachHangHienTai;
    }

    public int getSoCoHoiHienTai() {
        return soCoHoiHienTai;
    }

    public void setSoCoHoiHienTai(int soCoHoiHienTai) {
        this.soCoHoiHienTai = soCoHoiHienTai;
    }

    public List<NguoiDung> getDanhSachNguoiTiepNhan() {
        return danhSachNguoiTiepNhan;
    }

    public void setDanhSachNguoiTiepNhan(List<NguoiDung> danhSachNguoiTiepNhan) {
        this.danhSachNguoiTiepNhan = danhSachNguoiTiepNhan != null ? danhSachNguoiTiepNhan : new ArrayList<>();
    }
}
