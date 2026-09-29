package vn.nhom10.crm.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
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
    public static final String TRANG_THAI_CHO_KICH_HOAT = "CHO_KICH_HOAT";
    public static final String TRANG_THAI_KHOA = "KHOA";

    private int id;
    private String hoTen;
    private String email;
    private String matKhau;
    private String soDienThoai;
    private String trangThai = TRANG_THAI_CHO_KICH_HOAT;
    private Integer nhomKinhDoanhId;
    private NhomKinhDoanh nhomKinhDoanh;
    private Set<VaiTro> danhSachVaiTro = new HashSet<>();
    private Timestamp createdAt;
    private String ngayTao;

    public NguoiDung() {
    }

    public NguoiDung(int id, String hoTen, String email) {
        this.id = id;
        this.hoTen = hoTen;
        this.email = email;
    }

    public NguoiDung(int id, String hoTen, String email, String trangThai, Integer nhomKinhDoanhId) {
        this.id = id;
        this.hoTen = hoTen;
        this.email = email;
        this.trangThai = trangThai;
        this.nhomKinhDoanhId = nhomKinhDoanhId;
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

    public Integer getNhomId() {
        return nhomKinhDoanhId;
    }

    public void setNhomId(Integer nhomId) {
        this.nhomKinhDoanhId = nhomId;
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

    public String getTenNhom() {
        if (nhomKinhDoanh != null && nhomKinhDoanh.getTenNhom() != null) {
            return nhomKinhDoanh.getTenNhom();
        }
        return null;
    }

    public Set<VaiTro> getDanhSachVaiTro() {
        return danhSachVaiTro;
    }

    public void setDanhSachVaiTro(Set<VaiTro> danhSachVaiTro) {
        this.danhSachVaiTro = danhSachVaiTro != null ? danhSachVaiTro : new HashSet<>();
    }

    public Set<VaiTro> getDsVaiTro() {
        return danhSachVaiTro;
    }

    public void setDsVaiTro(Set<VaiTro> dsVaiTro) {
        setDanhSachVaiTro(dsVaiTro);
    }

    public Set<Integer> getDsVaiTroIds() {
        if (danhSachVaiTro == null) {
            return new HashSet<>();
        }
        return danhSachVaiTro.stream().map(VaiTro::getId).collect(Collectors.toSet());
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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
        if (createdAt != null) {
            this.ngayTao = createdAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
    }

    public String getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(String ngayTao) {
        this.ngayTao = ngayTao;
    }

    public boolean coVaiTro(String maVaiTro) {
        if (maVaiTro == null || maVaiTro.isBlank() || danhSachVaiTro == null) {
            return false;
        }
        return danhSachVaiTro.stream().anyMatch(vt -> maVaiTro.equalsIgnoreCase(vt.getMaVaiTro()));
    }

    public boolean coVaiTro(VaiTroEnum vaiTroEnum) {
        if (vaiTroEnum == null) return false;
        return coVaiTro(vaiTroEnum.getMaVaiTro());
    }

    public boolean dangHoatDong() {
        return TRANG_THAI_HOAT_DONG.equalsIgnoreCase(trangThai);
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NguoiDung nguoiDung = (NguoiDung) o;
        return id == nguoiDung.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
