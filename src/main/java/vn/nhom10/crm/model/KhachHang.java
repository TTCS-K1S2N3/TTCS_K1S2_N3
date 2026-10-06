package vn.nhom10.crm.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Model đại diện cho khách hàng doanh nghiệp trong hệ thống CRM (Story S3-01).
 * Ánh xạ chuẩn với bảng `khach_hang` trong cơ sở dữ liệu.
 */
public class KhachHang implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String maKhachHang;
    private String tenCongTy;
    private String tenChuanHoa;
    private String maSoThue;
    private Long nganhNgheId;
    private String tenNganhNghe;
    private Long quyMoId;
    private String tenQuyMo;
    private String website;
    private String websiteChuanHoa;
    private String diaChi;
    private Long khuVucId;
    private String tenKhuVuc;
    private Long nguoiSoHuuId;
    private String tenNguoiSoHuu;
    private Long nhomKinhDoanhId;
    private String tenNhomKinhDoanh;
    private BigDecimal doanhThuUocTinh = BigDecimal.ZERO;
    private Long congTyMeId;
    private String tenCongTyMe;
    private String trangThai = TrangThaiKhachHangEnum.TIEM_NANG.getMa();
    private boolean coRuiRo = false;
    private Timestamp ruiRoCapNhatLuc;
    private Timestamp lanTuongTacCuoi;
    private Long gopVaoKhachHangId;
    private String moTaChiTiet;
    private LocalDate ngayTao = LocalDate.now();
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public KhachHang() {
    }

    public KhachHang(Long id, String tenCongTy, Long nguoiSoHuuId) {
        this.id = id;
        this.tenCongTy = tenCongTy;
        this.nguoiSoHuuId = nguoiSoHuuId;
    }

    public KhachHang(String tenCongTy, String maSoThue, Long nganhNgheId, Long quyMoId,
                     String website, String diaChi, Long nguoiSoHuuId, Long nhomKinhDoanhId) {
        this.tenCongTy = tenCongTy;
        this.maSoThue = maSoThue;
        this.nganhNgheId = nganhNgheId;
        this.quyMoId = quyMoId;
        this.website = website;
        this.diaChi = diaChi;
        this.nguoiSoHuuId = nguoiSoHuuId;
        this.nhomKinhDoanhId = nhomKinhDoanhId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getTenCongTy() {
        return tenCongTy;
    }

    public void setTenCongTy(String tenCongTy) {
        this.tenCongTy = tenCongTy;
    }

    public String getTenChuanHoa() {
        return tenChuanHoa;
    }

    public void setTenChuanHoa(String tenChuanHoa) {
        this.tenChuanHoa = tenChuanHoa;
    }

    public String getMaSoThue() {
        return maSoThue;
    }

    public void setMaSoThue(String maSoThue) {
        this.maSoThue = maSoThue;
    }

    public Long getNganhNgheId() {
        return nganhNgheId;
    }

    public void setNganhNgheId(Long nganhNgheId) {
        this.nganhNgheId = nganhNgheId;
    }

    public String getTenNganhNghe() {
        return tenNganhNghe;
    }

    public void setTenNganhNghe(String tenNganhNghe) {
        this.tenNganhNghe = tenNganhNghe;
    }

    public Long getQuyMoId() {
        return quyMoId;
    }

    public void setQuyMoId(Long quyMoId) {
        this.quyMoId = quyMoId;
    }

    public String getTenQuyMo() {
        return tenQuyMo;
    }

    public void setTenQuyMo(String tenQuyMo) {
        this.tenQuyMo = tenQuyMo;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getWebsiteChuanHoa() {
        return websiteChuanHoa;
    }

    public void setWebsiteChuanHoa(String websiteChuanHoa) {
        this.websiteChuanHoa = websiteChuanHoa;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public Long getKhuVucId() {
        return khuVucId;
    }

    public void setKhuVucId(Long khuVucId) {
        this.khuVucId = khuVucId;
    }

    public String getTenKhuVuc() {
        return tenKhuVuc;
    }

    public void setTenKhuVuc(String tenKhuVuc) {
        this.tenKhuVuc = tenKhuVuc;
    }

    public Long getNguoiSoHuuId() {
        return nguoiSoHuuId;
    }

    public void setNguoiSoHuuId(Long nguoiSoHuuId) {
        this.nguoiSoHuuId = nguoiSoHuuId;
    }

    public String getTenNguoiSoHuu() {
        return tenNguoiSoHuu != null ? tenNguoiSoHuu : "Chưa xác định";
    }

    public void setTenNguoiSoHuu(String tenNguoiSoHuu) {
        this.tenNguoiSoHuu = tenNguoiSoHuu;
    }

    public Long getNhomKinhDoanhId() {
        return nhomKinhDoanhId;
    }

    public void setNhomKinhDoanhId(Long nhomKinhDoanhId) {
        this.nhomKinhDoanhId = nhomKinhDoanhId;
    }

    public String getTenNhomKinhDoanh() {
        return tenNhomKinhDoanh != null ? tenNhomKinhDoanh : "Chưa phân nhóm";
    }

    public void setTenNhomKinhDoanh(String tenNhomKinhDoanh) {
        this.tenNhomKinhDoanh = tenNhomKinhDoanh;
    }

    public BigDecimal getDoanhThuUocTinh() {
        return doanhThuUocTinh != null ? doanhThuUocTinh : BigDecimal.ZERO;
    }

    public void setDoanhThuUocTinh(BigDecimal doanhThuUocTinh) {
        this.doanhThuUocTinh = doanhThuUocTinh != null ? doanhThuUocTinh : BigDecimal.ZERO;
    }

    public Long getCongTyMeId() {
        return congTyMeId;
    }

    public void setCongTyMeId(Long congTyMeId) {
        this.congTyMeId = congTyMeId;
    }

    public String getTenCongTyMe() {
        return tenCongTyMe;
    }

    public void setTenCongTyMe(String tenCongTyMe) {
        this.tenCongTyMe = tenCongTyMe;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public TrangThaiKhachHangEnum getTrangThaiEnum() {
        return TrangThaiKhachHangEnum.tuChuoi(this.trangThai);
    }

    public String getTrangThaiHienThi() {
        TrangThaiKhachHangEnum en = getTrangThaiEnum();
        return en != null ? en.getTenHienThi() : (trangThai != null ? trangThai : "Tiềm năng");
    }

    public boolean isCoRuiRo() {
        return coRuiRo;
    }

    public void setCoRuiRo(boolean coRuiRo) {
        this.coRuiRo = coRuiRo;
    }

    public Timestamp getRuiRoCapNhatLuc() {
        return ruiRoCapNhatLuc;
    }

    public void setRuiRoCapNhatLuc(Timestamp ruiRoCapNhatLuc) {
        this.ruiRoCapNhatLuc = ruiRoCapNhatLuc;
    }

    public Timestamp getLanTuongTacCuoi() {
        return lanTuongTacCuoi;
    }

    public void setLanTuongTacCuoi(Timestamp lanTuongTacCuoi) {
        this.lanTuongTacCuoi = lanTuongTacCuoi;
    }

    public Long getGopVaoKhachHangId() {
        return gopVaoKhachHangId;
    }

    public void setGopVaoKhachHangId(Long gopVaoKhachHangId) {
        this.gopVaoKhachHangId = gopVaoKhachHangId;
    }

    public String getMoTaChiTiet() {
        return moTaChiTiet;
    }

    public void setMoTaChiTiet(String moTaChiTiet) {
        this.moTaChiTiet = moTaChiTiet;
    }

    public LocalDate getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDate ngayTao) {
        this.ngayTao = ngayTao;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getNgayTaoDinhDang() {
        if (ngayTao != null) {
            return ngayTao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        if (createdAt != null) {
            return createdAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        return "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KhachHang khachHang = (KhachHang) o;
        return Objects.equals(id, khachHang.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "KhachHang{" +
                "id=" + id +
                ", maKhachHang='" + maKhachHang + '\'' +
                ", tenCongTy='" + tenCongTy + '\'' +
                ", maSoThue='" + maSoThue + '\'' +
                ", nguoiSoHuuId=" + nguoiSoHuuId +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
