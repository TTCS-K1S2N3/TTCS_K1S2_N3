package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO đại diện cho một dòng dữ liệu người dùng đọc từ tệp Excel (Story S2-01).
 * Lưu trữ dữ liệu gốc, thông tin phân giải và danh sách lỗi thẩm định của từng dòng.
 */
public class DongExcelNguoiDungDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int soDong;
    private String hoTen;
    private String email;
    private String soDienThoai;
    private String vaiTroNhap;
    private String nhomNhap;
    private String matKhau;

    // Dữ liệu sau khi giải quyết (resolved)
    private List<Integer> dsVaiTroIds = new ArrayList<>();
    private List<String> tenVaiTroGiaiQuyet = new ArrayList<>();
    private Integer nhomKinhDoanhId;
    private String tenNhomGiaiQuyet;

    // Mật khẩu tạm được sinh ra nếu không cung cấp trong file
    private String matKhauTam;

    // Trạng thái kiểm tra và kết quả nhập
    private boolean hopLe = true;
    private List<String> danhSachLoi = new ArrayList<>();
    private boolean daNhap = false;
    private Long idNguoiDung;

    public DongExcelNguoiDungDTO() {
    }

    public DongExcelNguoiDungDTO(int soDong, String hoTen, String email, String soDienThoai, String vaiTroNhap, String nhomNhap) {
        this.soDong = soDong;
        this.hoTen = hoTen;
        this.email = email;
        this.soDienThoai = soDienThoai;
        this.vaiTroNhap = vaiTroNhap;
        this.nhomNhap = nhomNhap;
    }

    public void themLoi(String loi) {
        if (loi != null && !loi.isBlank()) {
            this.danhSachLoi.add(loi);
            this.hopLe = false;
        }
    }

    public int getSoDong() {
        return soDong;
    }

    public void setSoDong(int soDong) {
        this.soDong = soDong;
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

    public String getVaiTroNhap() {
        return vaiTroNhap;
    }

    public void setVaiTroNhap(String vaiTroNhap) {
        this.vaiTroNhap = vaiTroNhap;
    }

    public String getNhomNhap() {
        return nhomNhap;
    }

    public void setNhomNhap(String nhomNhap) {
        this.nhomNhap = nhomNhap;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public List<Integer> getDsVaiTroIds() {
        return dsVaiTroIds;
    }

    public void setDsVaiTroIds(List<Integer> dsVaiTroIds) {
        this.dsVaiTroIds = dsVaiTroIds != null ? dsVaiTroIds : new ArrayList<>();
    }

    public List<String> getTenVaiTroGiaiQuyet() {
        return tenVaiTroGiaiQuyet;
    }

    public void setTenVaiTroGiaiQuyet(List<String> tenVaiTroGiaiQuyet) {
        this.tenVaiTroGiaiQuyet = tenVaiTroGiaiQuyet != null ? tenVaiTroGiaiQuyet : new ArrayList<>();
    }

    public String getChuoiVaiTroHienThi() {
        if (tenVaiTroGiaiQuyet != null && !tenVaiTroGiaiQuyet.isEmpty()) {
            return String.join(", ", tenVaiTroGiaiQuyet);
        }
        return vaiTroNhap != null ? vaiTroNhap : "";
    }

    public Integer getNhomKinhDoanhId() {
        return nhomKinhDoanhId;
    }

    public void setNhomKinhDoanhId(Integer nhomKinhDoanhId) {
        this.nhomKinhDoanhId = nhomKinhDoanhId;
    }

    public String getTenNhomGiaiQuyet() {
        return tenNhomGiaiQuyet != null ? tenNhomGiaiQuyet : (nhomNhap != null ? nhomNhap : "Chưa phân nhóm");
    }

    public void setTenNhomGiaiQuyet(String tenNhomGiaiQuyet) {
        this.tenNhomGiaiQuyet = tenNhomGiaiQuyet;
    }

    public String getMatKhauTam() {
        return matKhauTam;
    }

    public void setMatKhauTam(String matKhauTam) {
        this.matKhauTam = matKhauTam;
    }

    public boolean isHopLe() {
        return hopLe;
    }

    public void setHopLe(boolean hopLe) {
        this.hopLe = hopLe;
    }

    public List<String> getDanhSachLoi() {
        return danhSachLoi;
    }

    public void setDanhSachLoi(List<String> danhSachLoi) {
        this.danhSachLoi = danhSachLoi != null ? danhSachLoi : new ArrayList<>();
        if (!this.danhSachLoi.isEmpty()) {
            this.hopLe = false;
        }
    }

    public String getChuoiLoi() {
        if (danhSachLoi == null || danhSachLoi.isEmpty()) {
            return "";
        }
        return String.join("; ", danhSachLoi);
    }

    public boolean isDaNhap() {
        return daNhap;
    }

    public void setDaNhap(boolean daNhap) {
        this.daNhap = daNhap;
    }

    public Long getIdNguoiDung() {
        return idNguoiDung;
    }

    public void setIdNguoiDung(Long idNguoiDung) {
        this.idNguoiDung = idNguoiDung;
    }
}
