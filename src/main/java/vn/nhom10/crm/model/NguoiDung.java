package vn.nhom10.crm.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Model đại diện cho người dùng hệ thống trong bảng nguoi_dung.
 */
public class NguoiDung implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String TRANG_THAI_HOAT_DONG = "HOAT_DONG";
    public static final String TRANG_THAI_KHOA = "KHOA";
    public static final String TRANG_THAI_CHUA_KICH_HOAT = "CHUA_KICH_HOAT";

    private int id;
    private String hoTen;
    private String email;
    private String matKhau;
    private String soDienThoai;
    private String trangThai = TRANG_THAI_HOAT_DONG;
    private Integer nhomKinhDoanhId;
    private NhomKinhDoanh nhomKinhDoanh;
    private Set<VaiTro> danhSachVaiTro = new HashSet<>();

    public NguoiDung() {
    }

    public NguoiDung(int id, String hoTen, String email) {
        this.id = id;
        this.hoTen = hoTen;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Integer getNhomKinhDoanhId() {
        return nhomKinhDoanhId;
    }

    public void setNhomKinhDoanhId(Integer nhomKinhDoanhId) {
        this.nhomKinhDoanhId = nhomKinhDoanhId;
    }

    public NhomKinhDoanh getNhomKinhDoanh() {
        return nhomKinhDoanh;
    }

    public void setNhomKinhDoanh(NhomKinhDoanh nhomKinhDoanh) {
        this.nhomKinhDoanh = nhomKinhDoanh;
        if (nhomKinhDoanh != null) {
            this.nhomKinhDoanhId = nhomKinhDoanh.getId();
        }
    }

    public Set<VaiTro> getDanhSachVaiTro() {
        return danhSachVaiTro;
    }

    public void setDanhSachVaiTro(Set<VaiTro> danhSachVaiTro) {
        this.danhSachVaiTro = danhSachVaiTro != null ? danhSachVaiTro : new HashSet<>();
    }

    public void themVaiTro(VaiTro vaiTro) {
        if (vaiTro != null) {
            this.danhSachVaiTro.add(vaiTro);
        }
    }

    public void themVaiTro(VaiTroEnum vaiTroEnum) {
        if (vaiTroEnum != null) {
            this.danhSachVaiTro.add(new VaiTro(vaiTroEnum));
        }
    }

    /**
     * Lấy tên nhóm kinh doanh người dùng đang thuộc về.
     */
    public String getTenNhomKinhDoanh() {
        if (nhomKinhDoanh != null && nhomKinhDoanh.getTenNhom() != null && !nhomKinhDoanh.getTenNhom().isBlank()) {
            return nhomKinhDoanh.getTenNhom();
        }
        return "Chưa phân nhóm";
    }

    /**
     * Lấy chuỗi hiển thị các vai trò cách nhau bởi dấu phẩy.
     */
    public String getChuoiVaiTroHienThi() {
        if (danhSachVaiTro == null || danhSachVaiTro.isEmpty()) {
            return "Chưa phân vai trò";
        }
        return danhSachVaiTro.stream()
                .map(vt -> vt.getTenVaiTro() != null ? vt.getTenVaiTro() : vt.getMaVaiTro())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }

    /**
     * Lấy danh sách VaiTroEnum của người dùng.
     */
    public Set<VaiTroEnum> getDanhSachVaiTroEnum() {
        Set<VaiTroEnum> ketQua = new HashSet<>();
        if (danhSachVaiTro != null) {
            for (VaiTro vt : danhSachVaiTro) {
                VaiTroEnum vte = vt.getVaiTroEnum();
                if (vte != null) {
                    ketQua.add(vte);
                }
            }
        }
        return ketQua;
    }

    /**
     * Kiểm tra người dùng có một vai trò cụ thể hay không.
     */
    public boolean coVaiTro(VaiTroEnum vaiTroEnum) {
        if (vaiTroEnum == null) return false;
        return getDanhSachVaiTroEnum().contains(vaiTroEnum);
    }

    /**
     * Kiểm tra người dùng có một vai trò cụ thể qua mã chuỗi hay không.
     */
    public boolean coVaiTro(String maVaiTro) {
        if (maVaiTro == null || maVaiTro.isBlank()) return false;
        VaiTroEnum vte = VaiTroEnum.tuMa(maVaiTro);
        return coVaiTro(vte);
    }

    /**
     * Lấy chữ viết tắt đại diện họ tên để hiển thị trên avatar.
     */
    public String getTenVietTat() {
        if (hoTen == null || hoTen.isBlank()) {
            return "CRM";
        }
        String[] tu = hoTen.trim().split("\\s+");
        if (tu.length == 1) {
            return tu[0].substring(0, Math.min(2, tu[0].length())).toUpperCase();
        }
        String dau = tu[0].substring(0, 1).toUpperCase();
        String cuoi = tu[tu.length - 1].substring(0, 1).toUpperCase();
        return dau + cuoi;
    }

    public boolean dangHoatDong() {
        return TRANG_THAI_HOAT_DONG.equalsIgnoreCase(trangThai);
    }
}
