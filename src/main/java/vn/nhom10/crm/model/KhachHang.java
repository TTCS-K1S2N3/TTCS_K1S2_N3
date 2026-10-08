package vn.nhom10.crm.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Model đại diện cho thực thể Khách hàng doanh nghiệp trong bảng 'khach_hang'.
 * - Story S3-01: Quản lý hồ sơ doanh nghiệp (tên, MST, ngành nghề, quy mô, website, địa chỉ, người sở hữu).
 * - Story S3-03: Hồ sơ 360° khách hàng.
 * - Story S3-05: Quan hệ công ty mẹ - công ty con (cong_ty_me_id).
 * - Story S3-06: Import khách hàng từ file Excel.
 * - Story S3-08: Cảnh báo rủi ro rời bỏ (co_rui_ro) và dịch vụ sau bán.
 * - Story S3-09: Chăm sóc định kỳ và tương tác khách hàng.
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
    private String maCongTyMe;
    private String trangThai = TrangThaiKhachHangEnum.TIEM_NANG.getMa();
    private boolean coRuiRo = false;
    private Timestamp ruiRoCapNhatLuc;
    private Timestamp lanTuongTacCuoi;
    private Long gopVaoKhachHangId;
    private String moTaChiTiet;
    private LocalDate ngayTao;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Thuộc tính bổ trợ thống kê hợp đồng (Story S3-05)
    private BigDecimal tongGiaTriHopDong = BigDecimal.ZERO;
    private int soLuongHopDong = 0;

    // Thuộc tính bổ trợ số yêu cầu hỗ trợ chưa xử lý (Story S3-08)
    private int soYeuCauChuaXuLy = 0;

    public KhachHang() {
    }

    public KhachHang(String tenCongTy, Long nguoiSoHuuId) {
        this.tenCongTy = tenCongTy;
        this.nguoiSoHuuId = nguoiSoHuuId;
    }

    public KhachHang(Long id, String tenCongTy, Long nguoiSoHuuId) {
        this.id = id;
        this.tenCongTy = tenCongTy;
        this.nguoiSoHuuId = nguoiSoHuuId;
    }

    public KhachHang(String maKhachHang, String tenCongTy, Long nguoiSoHuuId, Long nhomKinhDoanhId) {
        this.maKhachHang = maKhachHang;
        this.tenCongTy = tenCongTy;
        this.nguoiSoHuuId = nguoiSoHuuId;
        this.nhomKinhDoanhId = nhomKinhDoanhId;
    }

    public KhachHang(Long id, String maKhachHang, String tenCongTy, String maSoThue, Long nguoiSoHuuId, Long nhomKinhDoanhId) {
        this.id = id;
        this.maKhachHang = maKhachHang;
        this.tenCongTy = tenCongTy;
        this.maSoThue = maSoThue;
        this.nguoiSoHuuId = nguoiSoHuuId;
        this.nhomKinhDoanhId = nhomKinhDoanhId;
    }

    public KhachHang(Long id, String maKhachHang, String tenCongTy, String trangThai, Long nguoiSoHuuId, String tenNguoiSoHuu) {
        this.id = id;
        this.maKhachHang = maKhachHang;
        this.tenCongTy = tenCongTy;
        this.trangThai = trangThai;
        this.nguoiSoHuuId = nguoiSoHuuId;
        this.tenNguoiSoHuu = tenNguoiSoHuu;
    }

    public KhachHang(Long id, String maKhachHang, String tenCongTy, String maSoThue, Long nguoiSoHuuId, Long congTyMeId, String trangThai) {
        this.id = id;
        this.maKhachHang = maKhachHang;
        this.tenCongTy = tenCongTy;
        this.maSoThue = maSoThue;
        this.nguoiSoHuuId = nguoiSoHuuId;
        this.congTyMeId = congTyMeId;
        this.trangThai = trangThai;
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
        return tenNguoiSoHuu;
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
        return tenNhomKinhDoanh;
    }

    public void setTenNhomKinhDoanh(String tenNhomKinhDoanh) {
        this.tenNhomKinhDoanh = tenNhomKinhDoanh;
    }

    public BigDecimal getDoanhThuUocTinh() {
        return doanhThuUocTinh != null ? doanhThuUocTinh : BigDecimal.ZERO;
    }

    public void setDoanhThuUocTinh(BigDecimal doanhThuUocTinh) {
        this.doanhThuUocTinh = doanhThuUocTinh;
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

    public String getMaCongTyMe() {
        return maCongTyMe;
    }

    public void setMaCongTyMe(String maCongTyMe) {
        this.maCongTyMe = maCongTyMe;
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

    public void setTrangThaiEnum(TrangThaiKhachHangEnum trangThaiEnum) {
        this.trangThai = (trangThaiEnum != null) ? trangThaiEnum.getMa() : TrangThaiKhachHangEnum.TIEM_NANG.getMa();
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

    public boolean isCoRuiRoiRoiBo() {
        return coRuiRo;
    }

    public void setCoRuiRoiRoiBo(boolean coRuiRoiRoiBo) {
        this.coRuiRo = coRuiRoiRoiBo;
    }

    public LocalDateTime getRuiRoCapNhatLuc() {
        return ruiRoCapNhatLuc != null ? ruiRoCapNhatLuc.toLocalDateTime() : null;
    }

    public Timestamp getRuiRoCapNhatLucTimestamp() {
        return ruiRoCapNhatLuc;
    }

    public void setRuiRoCapNhatLuc(Timestamp ruiRoCapNhatLuc) {
        this.ruiRoCapNhatLuc = ruiRoCapNhatLuc;
    }

    public void setRuiRoCapNhatLuc(LocalDateTime ruiRoCapNhatLuc) {
        this.ruiRoCapNhatLuc = ruiRoCapNhatLuc != null ? Timestamp.valueOf(ruiRoCapNhatLuc) : null;
    }

    public Timestamp getLanTuongTacCuoi() {
        return lanTuongTacCuoi;
    }

    public void setLanTuongTacCuoi(Timestamp lanTuongTacCuoi) {
        this.lanTuongTacCuoi = lanTuongTacCuoi;
    }

    public void setLanTuongTacCuoi(LocalDateTime lanTuongTacCuoi) {
        this.lanTuongTacCuoi = lanTuongTacCuoi != null ? Timestamp.valueOf(lanTuongTacCuoi) : null;
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt != null ? Timestamp.valueOf(createdAt) : null;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt != null ? Timestamp.valueOf(updatedAt) : null;
    }

    public BigDecimal getTongGiaTriHopDong() {
        return tongGiaTriHopDong != null ? tongGiaTriHopDong : BigDecimal.ZERO;
    }

    public void setTongGiaTriHopDong(BigDecimal tongGiaTriHopDong) {
        this.tongGiaTriHopDong = tongGiaTriHopDong != null ? tongGiaTriHopDong : BigDecimal.ZERO;
    }

    public int getSoLuongHopDong() {
        return soLuongHopDong;
    }

    public void setSoLuongHopDong(int soLuongHopDong) {
        this.soLuongHopDong = soLuongHopDong;
    }

    public int getSoYeuCauChuaXuLy() {
        return soYeuCauChuaXuLy;
    }

    public void setSoYeuCauChuaXuLy(int soYeuCauChuaXuLy) {
        this.soYeuCauChuaXuLy = soYeuCauChuaXuLy;
    }

    public String getNgayTaoDinhDang() {
        if (ngayTao != null) {
            return ngayTao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        if (createdAt != null) {
            return createdAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        return "Chưa cập nhật";
    }

    public String getDoanhThuDinhDang() {
        if (doanhThuUocTinh == null || doanhThuUocTinh.compareTo(BigDecimal.ZERO) == 0) {
            return "0 ₫";
        }
        return String.format("%,.0f ₫", doanhThuUocTinh);
    }

    public String getTongGiaTriHopDongDinhDang() {
        if (tongGiaTriHopDong == null || tongGiaTriHopDong.compareTo(BigDecimal.ZERO) == 0) {
            return "0 ₫";
        }
        return String.format("%,.0f ₫", tongGiaTriHopDong);
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
                ", congTyMeId=" + congTyMeId +
                ", nguoiSoHuuId=" + nguoiSoHuuId +
                ", trangThai='" + trangThai + '\'' +
                ", coRuiRo=" + coRuiRo +
                '}';
    }
}
