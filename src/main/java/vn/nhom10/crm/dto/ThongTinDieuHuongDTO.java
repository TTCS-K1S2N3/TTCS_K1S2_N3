package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO chứa toàn bộ thông tin cần thiết để render thanh điều hướng (Navigation Bar / Sidebar),
 * bao gồm thông tin cá nhân (họ tên, vai trò, nhóm kinh doanh, ảnh đại diện) và danh sách menu được phép.
 */
public class ThongTinDieuHuongDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int nguoiDungId;
    private String hoTen;
    private String email;
    private String vaiTroHienThi;
    private String tenNhomKinhDoanh;
    private String tenVietTat;
    private boolean daDangNhap;
    private String anhDaiDienUrl;
    private String anhDaiDienThumbUrl;
    private boolean coAnhDaiDien;
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

    public ThongTinDieuHuongDTO(int nguoiDungId, String hoTen, String email, String vaiTroHienThi,
                               String tenNhomKinhDoanh, String tenVietTat, boolean daDangNhap,
                               String anhDaiDienUrl, String anhDaiDienThumbUrl,
                               List<MucMenuDTO> danhSachMucMenu) {
        this.nguoiDungId = nguoiDungId;
        this.hoTen = hoTen;
        this.email = email;
        this.vaiTroHienThi = vaiTroHienThi;
        this.tenNhomKinhDoanh = tenNhomKinhDoanh;
        this.tenVietTat = tenVietTat;
        this.daDangNhap = daDangNhap;
        this.anhDaiDienUrl = anhDaiDienUrl;
        this.anhDaiDienThumbUrl = anhDaiDienThumbUrl;
        this.coAnhDaiDien = (anhDaiDienUrl != null && !anhDaiDienUrl.isBlank());
        this.danhSachMucMenu = danhSachMucMenu != null ? danhSachMucMenu : new ArrayList<>();
    }

    public int getNguoiDungId() {
        return nguoiDungId;
    }

    public void setNguoiDungId(int nguoiDungId) {
        this.nguoiDungId = nguoiDungId;
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

    public String getAnhDaiDienUrl() {
        return anhDaiDienUrl;
    }

    public void setAnhDaiDienUrl(String anhDaiDienUrl) {
        this.anhDaiDienUrl = anhDaiDienUrl;
        this.coAnhDaiDien = (anhDaiDienUrl != null && !anhDaiDienUrl.isBlank());
    }

    public String getAnhDaiDienThumbUrl() {
        return anhDaiDienThumbUrl;
    }

    public void setAnhDaiDienThumbUrl(String anhDaiDienThumbUrl) {
        this.anhDaiDienThumbUrl = anhDaiDienThumbUrl;
    }

    public boolean isCoAnhDaiDien() {
        return coAnhDaiDien;
    }

    public void setCoAnhDaiDien(boolean coAnhDaiDien) {
        this.coAnhDaiDien = coAnhDaiDien;
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
