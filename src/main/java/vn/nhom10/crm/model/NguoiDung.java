package vn.nhom10.crm.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Model đại diện cho người dùng hệ thống CRM.
 */
public class NguoiDung implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String TRANG_THAI_HOAT_DONG = "HOAT_DONG";
    public static final String TRANG_THAI_KHOA = "KHOA";
    public static final String TRANG_THAI_CHO_KICH_HOAT = "CHO_KICH_HOAT";
    public static final String TRANG_THAI_NGUNG_HOAT_DONG = "NGUNG_HOAT_DONG";

    private long id;
    private String hoTen;
    private String email;
    private String matKhau;
    private String soDienThoai;
    private String chuKyEmail;
    private String anhDaiDienPath;
    private String anhDaiDienThumbPath;
    private String trangThai = TRANG_THAI_HOAT_DONG;
    private int soLanSai = 0;
    private Timestamp thoiGianKhoa;
    private Integer nhomKinhDoanhId;
    private String tenNhomKinhDoanh;
    private NhomKinhDoanh nhomKinhDoanh;
    private Set<VaiTro> danhSachVaiTro = new HashSet<>();
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public NguoiDung() {
    }

    public NguoiDung(long id, String hoTen, String email) {
        this.id = id;
        this.hoTen = hoTen;
        this.email = email;
    }

    public NguoiDung(long id, String hoTen, String email, String trangThai, Integer nhomKinhDoanhId) {
        this.id = id;
        this.hoTen = hoTen;
        this.email = email;
        this.trangThai = trangThai;
        this.nhomKinhDoanhId = nhomKinhDoanhId;
    }

    public NguoiDung(long id, String hoTen, String email, String matKhau, String soDienThoai, String trangThai) {
        this.id = id;
        this.hoTen = hoTen;
        this.email = email;
        this.matKhau = matKhau;
        this.soDienThoai = soDienThoai;
        this.trangThai = trangThai;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public String getChuKyEmail() {
        return chuKyEmail;
    }

    public void setChuKyEmail(String chuKyEmail) {
        this.chuKyEmail = chuKyEmail;
    }

    public String getAnhDaiDienPath() {
        return anhDaiDienPath;
    }

    public void setAnhDaiDienPath(String anhDaiDienPath) {
        this.anhDaiDienPath = anhDaiDienPath;
    }

    public String getAnhDaiDienThumbPath() {
        return anhDaiDienThumbPath;
    }

    public void setAnhDaiDienThumbPath(String anhDaiDienThumbPath) {
        this.anhDaiDienThumbPath = anhDaiDienThumbPath;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public int getSoLanSai() {
        return soLanSai;
    }

    public void setSoLanSai(int soLanSai) {
        this.soLanSai = soLanSai;
    }

    public Timestamp getThoiGianKhoa() {
        return thoiGianKhoa;
    }

    public void setThoiGianKhoa(Timestamp thoiGianKhoa) {
        this.thoiGianKhoa = thoiGianKhoa;
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
            this.tenNhomKinhDoanh = nhomKinhDoanh.getTenNhom();
        }
    }

    public String getTenNhom() {
        return getTenNhomKinhDoanh();
    }

    public String getTenNhomKinhDoanh() {
        if (nhomKinhDoanh != null && nhomKinhDoanh.getTenNhom() != null && !nhomKinhDoanh.getTenNhom().isBlank()) {
            return nhomKinhDoanh.getTenNhom();
        }
        if (tenNhomKinhDoanh != null && !tenNhomKinhDoanh.isBlank()) {
            return tenNhomKinhDoanh;
        }
        return "Chưa phân nhóm";
    }

    public void setTenNhomKinhDoanh(String tenNhomKinhDoanh) {
        this.tenNhomKinhDoanh = tenNhomKinhDoanh;
    }

    public Set<VaiTro> getDanhSachVaiTro() {
        return danhSachVaiTro;
    }

    public void setDanhSachVaiTro(Set<VaiTro> danhSachVaiTro) {
        this.danhSachVaiTro = (danhSachVaiTro != null) ? danhSachVaiTro : new HashSet<>();
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

    /**
     * Lấy chuỗi hiển thị các vai trò cách nhau bởi dấu phẩy.
     */
    public String getChuoiVaiTroHienThi() {
        if (danhSachVaiTro == null || danhSachVaiTro.isEmpty()) {
            return "Chưa phân vai trò";
        }
        return danhSachVaiTro.stream()
                .map(vt -> vt.getTenHienThi() != null ? vt.getTenHienThi() : vt.getMaVaiTro())
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.joining(", "));
    }

    /**
     * Lấy danh sách VaiTroEnum của người dùng.
     */
    public Set<VaiTroEnum> getDanhSachVaiTroEnum() {
        Set<VaiTroEnum> ketQua = new HashSet<>();
        if (danhSachVaiTro != null) {
            for (VaiTro vt : danhSachVaiTro) {
                if (vt != null) {
                    VaiTroEnum vte = vt.getVaiTroEnum();
                    if (vte != null) {
                        ketQua.add(vte);
                    } else if (vt.getMaVaiTro() != null) {
                        VaiTroEnum e = VaiTroEnum.tuMa(vt.getMaVaiTro());
                        if (e != null) {
                            ketQua.add(e);
                        }
                    }
                }
            }
        }
        return ketQua;
    }

    /**
     * Xác định phạm vi dữ liệu tối đa của người dùng dựa trên các vai trò thực tế nạp từ DB (Fail-Closed).
     * Mặc định là CA_NHAN nếu không có vai trò hoặc DB lỗi.
     */
    public PhamViDuLieu layPhamViToiDa() {
        if (danhSachVaiTro == null || danhSachVaiTro.isEmpty()) {
            return PhamViDuLieu.CA_NHAN;
        }
        PhamViDuLieu maxScope = PhamViDuLieu.CA_NHAN;
        for (VaiTro vt : danhSachVaiTro) {
            PhamViDuLieu p = vt.getPhamViToiDa();
            if (p == PhamViDuLieu.TOAN_BO) {
                return PhamViDuLieu.TOAN_BO;
            }
            if (p == PhamViDuLieu.NHOM) {
                maxScope = PhamViDuLieu.NHOM;
            }
        }
        return maxScope;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getNgayTao() {
        if (createdAt != null) {
            return createdAt.toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        return "";
    }

    public void setNgayTao(String ngayTao) {
        // Hỗ trợ trường hợp gán từ view nếu có
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Kiểm tra người dùng có giữ vai trò cụ thể theo mã không.
     */
    public boolean coVaiTro(String maVaiTro) {
        if (maVaiTro == null || maVaiTro.isBlank() || danhSachVaiTro == null) {
            return false;
        }
        return danhSachVaiTro.stream().anyMatch(vt -> maVaiTro.equalsIgnoreCase(vt.getMaVaiTro()));
    }

    /**
     * Kiểm tra người dùng có giữ vai trò theo enum không.
     */
    public boolean coVaiTro(VaiTroEnum vaiTroEnum) {
        if (vaiTroEnum == null) {
            return false;
        }
        return coVaiTro(vaiTroEnum.getMaVaiTro());
    }

    /**
     * Kiểm tra tài khoản có ở trạng thái hoạt động bình thường hay không.
     */
    public boolean dangHoatDong() {
        return TRANG_THAI_HOAT_DONG.equalsIgnoreCase(trangThai);
    }

    /**
     * Kiểm tra tài khoản có đang trong thời gian bị khóa tạm hay không.
     */
    public boolean coBiKhoaTam() {
        if (thoiGianKhoa == null) {
            return false;
        }
        return thoiGianKhoa.getTime() > System.currentTimeMillis();
    }

    /**
     * Lấy số phút còn lại trong thời gian khóa tạm.
     */
    public long getSoPhutKhoaConLai() {
        if (!coBiKhoaTam()) {
            return 0;
        }
        long diffMillis = thoiGianKhoa.getTime() - System.currentTimeMillis();
        return Math.max(1, (diffMillis + 59999) / 60000);
    }

    /**
     * Lấy tên viết tắt hiển thị avatar (ví dụ "Phan Duy Hưng" -> "PH").
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
