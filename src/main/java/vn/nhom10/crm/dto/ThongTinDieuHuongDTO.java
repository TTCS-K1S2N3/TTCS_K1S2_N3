package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO chứa thông tin điều hướng của người dùng hiện tại (họ tên, vai trò, nhóm kinh doanh, menu).
 */
public class ThongTinDieuHuongDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String hoTen;
    private String email;
    private String vaiTroHienThi;
    private String tenNhomKinhDoanh;
    private String tenVietTat;
    private boolean daDangNhap;
    private List<MucMenuDTO> danhSachMucMenu = new ArrayList<>();

    public ThongTinDieuHuongDTO() {
    }

    public ThongTinDieuHuongDTO(String hoTen, String email, String vaiTroHienThi, String tenNhomKinhDoanh, String tenVietTat, boolean daDangNhap, List<MucMenuDTO> danhSachMucMenu) {
        this.hoTen = hoTen;
        this.email = email;
        this.vaiTroHienThi = vaiTroHienThi;
        this.tenNhomKinhDoanh = tenNhomKinhDoanh;
        this.tenVietTat = tenVietTat;
        this.daDangNhap = daDangNhap;
        this.danhSachMucMenu = danhSachMucMenu != null ? danhSachMucMenu : new ArrayList<>();
    }

    public String getHoTen() {
        return hoTen != null ? hoTen : "Khách";
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return email != null ? email : "";
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVaiTroHienThi() {
        return vaiTroHienThi != null ? vaiTroHienThi : "Chưa phân vai trò";
    }

    public void setVaiTroHienThi(String vaiTroHienThi) {
        this.vaiTroHienThi = vaiTroHienThi;
    }

    public String getTenNhomKinhDoanh() {
        return tenNhomKinhDoanh != null ? tenNhomKinhDoanh : "Chưa phân nhóm";
    }

    public void setTenNhomKinhDoanh(String tenNhomKinhDoanh) {
        this.tenNhomKinhDoanh = tenNhomKinhDoanh;
    }

    public String getTenVietTat() {
        return tenVietTat != null ? tenVietTat : "CRM";
    }

    public void setTenVietTat(String tenVietTat) {
        this.tenVietTat = tenVietTat;
    }

    public boolean isDaDangNhap() {
        return daDangNhap;
    }

    public void setDaDangNhap(boolean daDangNhap) {
        this.daDangNhap = daDangNhap;
    }

    public List<MucMenuDTO> getDanhSachMucMenu() {
        return danhSachMucMenu;
    }

    public void setDanhSachMucMenu(List<MucMenuDTO> danhSachMucMenu) {
        this.danhSachMucMenu = danhSachMucMenu != null ? danhSachMucMenu : new ArrayList<>();
    }

    public int getSoLuongMenu() {
        return danhSachMucMenu != null ? danhSachMucMenu.size() : 0;
    }
}
