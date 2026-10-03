package vn.nhom10.crm.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Model đại diện cho một giai đoạn trong chuỗi Pipeline bán hàng (S2-09).
 * Ánh xạ chuẩn với bảng giai_doan_pipeline và các điều kiện trong dieu_kien_giai_doan.
 */
public class GiaiDoanPipeline implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final DecimalFormat TIEN_TE_FORMAT = new DecimalFormat("#,##0");

    private int id;
    private long pipelineId = 0;
    private String maGiaiDoan;
    private String tenGiaiDoan;
    private int thuTu = 1;
    private int xacSuatThang = 0; // % xác suất thắng (0-100) -> xac_suat_mac_dinh
    private int soNgayCanhBaoDinhTre = 7; // so_ngay_dinh_tre
    private LoaiGiaiDoanEnum loaiGiaiDoan = LoaiGiaiDoanEnum.DANG_TIEN_HANH; // loai_ket_thuc
    private TrangThaiGiaiDoanEnum trangThai = TrangThaiGiaiDoanEnum.DANG_AP_DUNG; // hoat_dong
    private int soCoHoiHienTai = 0; // Số cơ hội đang chạy ở giai đoạn này (tính từ bảng co_hoi)
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // AC 3: Khai báo điều kiện bắt buộc để rời một giai đoạn
    private String dieuKienBatBuoc;
    private int soCuocGapToiThieu = 0;
    private int soCuocGoiToiThieu = 0;
    private boolean yeuCauBaoGia = false;
    private boolean yeuCauKhaoSatNhuCau = false;
    private List<DieuKienGiaiDoan> danhSachDieuKien = new ArrayList<>();

    public GiaiDoanPipeline() {
    }

    public GiaiDoanPipeline(int id, String maGiaiDoan, String tenGiaiDoan, int thuTu, int xacSuatThang, String dieuKienBatBuoc) {
        this.id = id;
        this.maGiaiDoan = maGiaiDoan;
        this.tenGiaiDoan = tenGiaiDoan;
        this.thuTu = thuTu;
        this.xacSuatThang = xacSuatThang;
        this.dieuKienBatBuoc = dieuKienBatBuoc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public String getMaGiaiDoan() {
        return maGiaiDoan;
    }

    public void setMaGiaiDoan(String maGiaiDoan) {
        this.maGiaiDoan = maGiaiDoan;
    }

    public String getTenGiaiDoan() {
        return tenGiaiDoan;
    }

    public void setTenGiaiDoan(String tenGiaiDoan) {
        this.tenGiaiDoan = tenGiaiDoan;
    }

    public int getThuTu() {
        return thuTu;
    }

    public void setThuTu(int thuTu) {
        this.thuTu = thuTu;
    }

    public int getXacSuatThang() {
        return xacSuatThang;
    }

    public void setXacSuatThang(int xacSuatThang) {
        this.xacSuatThang = xacSuatThang;
    }

    public int getSoNgayCanhBaoDinhTre() {
        return soNgayCanhBaoDinhTre;
    }

    public void setSoNgayCanhBaoDinhTre(int soNgayCanhBaoDinhTre) {
        this.soNgayCanhBaoDinhTre = soNgayCanhBaoDinhTre;
    }

    public LoaiGiaiDoanEnum getLoaiGiaiDoan() {
        return loaiGiaiDoan;
    }

    public void setLoaiGiaiDoan(LoaiGiaiDoanEnum loaiGiaiDoan) {
        this.loaiGiaiDoan = loaiGiaiDoan;
    }

    public String getMaLoaiGiaiDoan() {
        return loaiGiaiDoan != null ? loaiGiaiDoan.getMaLoai() : null;
    }

    public TrangThaiGiaiDoanEnum getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiGiaiDoanEnum trangThai) {
        this.trangThai = trangThai != null ? trangThai : TrangThaiGiaiDoanEnum.DANG_AP_DUNG;
    }

    public String getMaTrangThai() {
        return trangThai != null ? trangThai.getMaTrangThai() : null;
    }

    public int getSoCoHoiHienTai() {
        return soCoHoiHienTai;
    }

    public void setSoCoHoiHienTai(int soCoHoiHienTai) {
        this.soCoHoiHienTai = soCoHoiHienTai;
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

    public String getDieuKienBatBuoc() {
        return dieuKienBatBuoc;
    }

    public void setDieuKienBatBuoc(String dieuKienBatBuoc) {
        this.dieuKienBatBuoc = dieuKienBatBuoc;
    }

    public int getSoCuocGapToiThieu() {
        return soCuocGapToiThieu;
    }

    public void setSoCuocGapToiThieu(int soCuocGapToiThieu) {
        this.soCuocGapToiThieu = Math.max(0, soCuocGapToiThieu);
    }

    public int getSoCuocGoiToiThieu() {
        return soCuocGoiToiThieu;
    }

    public void setSoCuocGoiToiThieu(int soCuocGoiToiThieu) {
        this.soCuocGoiToiThieu = Math.max(0, soCuocGoiToiThieu);
    }

    public boolean isYeuCauBaoGia() {
        return yeuCauBaoGia;
    }

    public void setYeuCauBaoGia(boolean yeuCauBaoGia) {
        this.yeuCauBaoGia = yeuCauBaoGia;
    }

    public boolean isYeuCauKhaoSatNhuCau() {
        return yeuCauKhaoSatNhuCau;
    }

    public void setYeuCauKhaoSatNhuCau(boolean yeuCauKhaoSatNhuCau) {
        this.yeuCauKhaoSatNhuCau = yeuCauKhaoSatNhuCau;
    }

    public List<DieuKienGiaiDoan> getDanhSachDieuKien() {
        return danhSachDieuKien;
    }

    public void setDanhSachDieuKien(List<DieuKienGiaiDoan> danhSachDieuKien) {
        this.danhSachDieuKien = danhSachDieuKien != null ? danhSachDieuKien : new ArrayList<>();
    }

    public boolean isDangApDung() {
        return this.trangThai == TrangThaiGiaiDoanEnum.DANG_AP_DUNG;
    }

    public boolean isThanhCong() {
        return this.loaiGiaiDoan == LoaiGiaiDoanEnum.THANH_CONG;
    }

    public boolean isThatBai() {
        return this.loaiGiaiDoan == LoaiGiaiDoanEnum.THAT_BAI;
    }

    /**
     * AC 2: Mỗi giai đoạn có xác suất thắng mặc định dùng để tính dự báo.
     * Dự báo = Giá trị cơ hội * (Xác suất / 100)
     */
    public BigDecimal tinhDuBaoDoanhSo(BigDecimal giaTriCoHoi) {
        if (giaTriCoHoi == null || giaTriCoHoi.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return giaTriCoHoi.multiply(new BigDecimal(this.xacSuatThang))
                .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
    }

    public String getDuBaoDoanhSoDinhDang(BigDecimal giaTriCoHoi) {
        BigDecimal duBao = tinhDuBaoDoanhSo(giaTriCoHoi);
        return TIEN_TE_FORMAT.format(duBao) + " ₫";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GiaiDoanPipeline that = (GiaiDoanPipeline) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
