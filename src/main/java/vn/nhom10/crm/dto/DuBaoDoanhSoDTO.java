package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * DTO mô phỏng và tính toán dự báo doanh số theo xác suất thắng của giai đoạn (S2-09 - AC 2).
 */
public class DuBaoDoanhSoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final DecimalFormat TIEN_TE_FORMAT = new DecimalFormat("#,##0");

    private int giaiDoanId;
    private String tenGiaiDoan;
    private int xacSuatThang;
    private BigDecimal tongGiaTriCoHoi = BigDecimal.ZERO;
    private BigDecimal doanhSoDuBao = BigDecimal.ZERO;
    private int soLuongCoHoi = 0;

    public DuBaoDoanhSoDTO() {
    }

    public DuBaoDoanhSoDTO(int giaiDoanId, String tenGiaiDoan, int xacSuatThang, BigDecimal tongGiaTriCoHoi, int soLuongCoHoi) {
        this.giaiDoanId = giaiDoanId;
        this.tenGiaiDoan = tenGiaiDoan;
        this.xacSuatThang = xacSuatThang;
        this.tongGiaTriCoHoi = tongGiaTriCoHoi != null ? tongGiaTriCoHoi : BigDecimal.ZERO;
        this.soLuongCoHoi = soLuongCoHoi;
        this.doanhSoDuBao = this.tongGiaTriCoHoi.multiply(new BigDecimal(xacSuatThang))
                .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal tinhDuBao(BigDecimal giaTri, int xacSuat) {
        if (giaTri == null || giaTri.compareTo(BigDecimal.ZERO) <= 0 || xacSuat <= 0) {
            return BigDecimal.ZERO;
        }
        return giaTri.multiply(new BigDecimal(xacSuat)).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
    }

    public int getGiaiDoanId() {
        return giaiDoanId;
    }

    public void setGiaiDoanId(int giaiDoanId) {
        this.giaiDoanId = giaiDoanId;
    }

    public String getTenGiaiDoan() {
        return tenGiaiDoan;
    }

    public void setTenGiaiDoan(String tenGiaiDoan) {
        this.tenGiaiDoan = tenGiaiDoan;
    }

    public int getXacSuatThang() {
        return xacSuatThang;
    }

    public void setXacSuatThang(int xacSuatThang) {
        this.xacSuatThang = xacSuatThang;
    }

    public BigDecimal getTongGiaTriCoHoi() {
        return tongGiaTriCoHoi;
    }

    public void setTongGiaTriCoHoi(BigDecimal tongGiaTriCoHoi) {
        this.tongGiaTriCoHoi = tongGiaTriCoHoi != null ? tongGiaTriCoHoi : BigDecimal.ZERO;
    }

    public BigDecimal getDoanhSoDuBao() {
        return doanhSoDuBao;
    }

    public void setDoanhSoDuBao(BigDecimal doanhSoDuBao) {
        this.doanhSoDuBao = doanhSoDuBao;
    }

    public int getSoLuongCoHoi() {
        return soLuongCoHoi;
    }

    public void setSoLuongCoHoi(int soLuongCoHoi) {
        this.soLuongCoHoi = soLuongCoHoi;
    }

    public String getTongGiaTriDinhDang() {
        return TIEN_TE_FORMAT.format(tongGiaTriCoHoi) + " ₫";
    }

    public String getDoanhSoDuBaoDinhDang() {
        return TIEN_TE_FORMAT.format(doanhSoDuBao) + " ₫";
    }
}
