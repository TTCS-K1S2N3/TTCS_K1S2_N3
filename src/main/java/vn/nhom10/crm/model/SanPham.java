package vn.nhom10.crm.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Objects;

/**
 * Model đại diện cho danh mục sản phẩm dịch vụ và bảng giá niêm yết trong bảng san_pham (S2-05).
 */
public class SanPham implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final DecimalFormat TIEN_TE_FORMAT = new DecimalFormat("#,##0");

    private int id;
    private String maSanPham;
    private String tenSanPham;
    private LoaiSanPhamEnum loai = LoaiSanPhamEnum.SAN_PHAM_MOT_LAN;
    private String donViTinh;
    private BigDecimal giaNiemYet = BigDecimal.ZERO;
    private BigDecimal giaSan = BigDecimal.ZERO;
    private BigDecimal giaVon; // Chỉ Giám đốc kinh doanh xem và sửa được
    private String moTa;
    private TrangThaiSanPhamEnum trangThai = TrangThaiSanPhamEnum.DANG_KINH_DOANH;
    private boolean daXuatHienTrongBaoGia = false;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public SanPham() {
    }

    public SanPham(int id, String maSanPham, String tenSanPham, LoaiSanPhamEnum loai, String donViTinh,
                   BigDecimal giaNiemYet, BigDecimal giaSan, BigDecimal giaVon) {
        this.id = id;
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.loai = loai;
        this.donViTinh = donViTinh;
        this.giaNiemYet = giaNiemYet;
        this.giaSan = giaSan;
        this.giaVon = giaVon;
        this.trangThai = TrangThaiSanPhamEnum.DANG_KINH_DOANH;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMaSanPham() {
        return maSanPham;
    }

    public void setMaSanPham(String maSanPham) {
        this.maSanPham = maSanPham;
    }

    public String getTenSanPham() {
        return tenSanPham;
    }

    public void setTenSanPham(String tenSanPham) {
        this.tenSanPham = tenSanPham;
    }

    public LoaiSanPhamEnum getLoai() {
        return loai;
    }

    public void setLoai(LoaiSanPhamEnum loai) {
        this.loai = loai != null ? loai : LoaiSanPhamEnum.SAN_PHAM_MOT_LAN;
    }

    public String getMaLoai() {
        return loai != null ? loai.getMaLoai() : null;
    }

    public String getTenLoai() {
        return loai != null ? loai.getTenHienThi() : "";
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public BigDecimal getGiaNiemYet() {
        return giaNiemYet;
    }

    public void setGiaNiemYet(BigDecimal giaNiemYet) {
        this.giaNiemYet = giaNiemYet != null ? giaNiemYet : BigDecimal.ZERO;
    }

    public BigDecimal getGiaSan() {
        return giaSan;
    }

    public void setGiaSan(BigDecimal giaSan) {
        this.giaSan = giaSan != null ? giaSan : BigDecimal.ZERO;
    }

    public BigDecimal getGiaVon() {
        return giaVon;
    }

    public void setGiaVon(BigDecimal giaVon) {
        this.giaVon = giaVon;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public TrangThaiSanPhamEnum getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiSanPhamEnum trangThai) {
        this.trangThai = trangThai != null ? trangThai : TrangThaiSanPhamEnum.DANG_KINH_DOANH;
    }

    public String getMaTrangThai() {
        return trangThai != null ? trangThai.getMaTrangThai() : null;
    }

    public String getTenTrangThai() {
        return trangThai != null ? trangThai.getTenHienThi() : "";
    }

    public boolean isDaXuatHienTrongBaoGia() {
        return daXuatHienTrongBaoGia;
    }

    public void setDaXuatHienTrongBaoGia(boolean daXuatHienTrongBaoGia) {
        this.daXuatHienTrongBaoGia = daXuatHienTrongBaoGia;
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

    /**
     * AC: Giá sàn là ngưỡng để xác định báo giá có cần duyệt chiết khấu hay không.
     * Kiểm tra đơn giá đề xuất trong báo giá có thấp hơn giá sàn niêm yết không.
     *
     * @param donGia đơn giá bán trong báo giá
     * @return true nếu đơn giá < giá sàn (cần duyệt chiết khấu đặc biệt từ Giám đốc)
     */
    public boolean kiemTraDuoiGiaSan(BigDecimal donGia) {
        if (donGia == null || this.giaSan == null) {
            return false;
        }
        return donGia.compareTo(this.giaSan) < 0;
    }

    public boolean isDangKinhDoanh() {
        return this.trangThai == TrangThaiSanPhamEnum.DANG_KINH_DOANH;
    }

    public boolean isDichVuThueBao() {
        return this.loai == LoaiSanPhamEnum.DICH_VU_THUE_BAO;
    }

    public String getGiaNiemYetDinhDang() {
        return giaNiemYet != null ? TIEN_TE_FORMAT.format(giaNiemYet) + " ₫" : "0 ₫";
    }

    public String getGiaSanDinhDang() {
        return giaSan != null ? TIEN_TE_FORMAT.format(giaSan) + " ₫" : "0 ₫";
    }

    public String getGiaVonDinhDang() {
        return giaVon != null ? TIEN_TE_FORMAT.format(giaVon) + " ₫" : "—";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SanPham sanPham = (SanPham) o;
        return id == sanPham.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
