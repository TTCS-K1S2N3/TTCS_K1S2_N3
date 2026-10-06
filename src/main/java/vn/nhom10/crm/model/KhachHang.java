package vn.nhom10.crm.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model ánh xạ bảng `khach_hang` (Story S3-08).
 * Bao gồm thông tin cờ rủi ro rời bỏ `co_rui_ro` và thời điểm cập nhật `rui_ro_cap_nhat_luc`.
 */
public class KhachHang implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String maKhachHang;
    private String tenCongTy;
    private String tenChuanHoa;
    private String maSoThue;
    private Long nganhNgheId;
    private Long quyMoId;
    private String website;
    private String websiteChuanHoa;
    private String diaChi;
    private Long khuVucId;
    private Long nguoiSoHuuId;
    private Long nhomKinhDoanhId;
    private BigDecimal doanhThuUocTinh;
    private Long congTyMeId;
    private String trangThai; // TIEM_NANG, DANG_GIAO_DICH, KHACH_HANG, NGUNG_HOP_TAC
    private boolean coRuiRo;
    private LocalDateTime ruiRoCapNhatLuc;
    private LocalDateTime lanTuongTacCuoi;
    private Long gopVaoKhachHangId;
    private String moTaChiTiet;
    private LocalDate ngayTao;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Các trường hiển thị bổ trợ
    private String tenNguoiSoHuu;
    private String tenNhomKinhDoanh;
    private int soYeuCauChuaXuLy;

    public KhachHang() {
        this.trangThai = "TIEM_NANG";
        this.coRuiRo = false;
        this.ngayTao = LocalDate.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public KhachHang(String tenCongTy, Long nguoiSoHuuId) {
        this();
        this.tenCongTy = tenCongTy;
        this.nguoiSoHuuId = nguoiSoHuuId;
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

    public Long getQuyMoId() {
        return quyMoId;
    }

    public void setQuyMoId(Long quyMoId) {
        this.quyMoId = quyMoId;
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

    public Long getNguoiSoHuuId() {
        return nguoiSoHuuId;
    }

    public void setNguoiSoHuuId(Long nguoiSoHuuId) {
        this.nguoiSoHuuId = nguoiSoHuuId;
    }

    public Long getNhomKinhDoanhId() {
        return nhomKinhDoanhId;
    }

    public void setNhomKinhDoanhId(Long nhomKinhDoanhId) {
        this.nhomKinhDoanhId = nhomKinhDoanhId;
    }

    public BigDecimal getDoanhThuUocTinh() {
        return doanhThuUocTinh;
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

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public boolean isCoRuiRo() {
        return coRuiRo;
    }

    public void setCoRuiRo(boolean coRuiRo) {
        this.coRuiRo = coRuiRo;
    }

    public LocalDateTime getRuiRoCapNhatLuc() {
        return ruiRoCapNhatLuc;
    }

    public void setRuiRoCapNhatLuc(LocalDateTime ruiRoCapNhatLuc) {
        this.ruiRoCapNhatLuc = ruiRoCapNhatLuc;
    }

    public LocalDateTime getLanTuongTacCuoi() {
        return lanTuongTacCuoi;
    }

    public void setLanTuongTacCuoi(LocalDateTime lanTuongTacCuoi) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getTenNguoiSoHuu() {
        return tenNguoiSoHuu;
    }

    public void setTenNguoiSoHuu(String tenNguoiSoHuu) {
        this.tenNguoiSoHuu = tenNguoiSoHuu;
    }

    public String getTenNhomKinhDoanh() {
        return tenNhomKinhDoanh;
    }

    public void setTenNhomKinhDoanh(String tenNhomKinhDoanh) {
        this.tenNhomKinhDoanh = tenNhomKinhDoanh;
    }

    public int getSoYeuCauChuaXuLy() {
        return soYeuCauChuaXuLy;
    }

    public void setSoYeuCauChuaXuLy(int soYeuCauChuaXuLy) {
        this.soYeuCauChuaXuLy = soYeuCauChuaXuLy;
    }
}
