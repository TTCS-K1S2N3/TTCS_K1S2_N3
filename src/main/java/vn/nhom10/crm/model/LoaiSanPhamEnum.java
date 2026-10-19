package vn.nhom10.crm.model;

/**
 * Phân loại danh mục sản phẩm dịch vụ theo Acceptance Criteria Story S2-05:
 * Sản phẩm một lần hoặc dịch vụ thuê bao.
 */
public enum LoaiSanPhamEnum {
    SAN_PHAM_MOT_LAN("SAN_PHAM_MOT_LAN", "Sản phẩm một lần", "Mua đứt hoặc chuyển giao một lần"),
    DICH_VU_THUE_BAO("DICH_VU_THUE_BAO", "Dịch vụ thuê bao", "Thuê bao định kỳ hàng tháng/năm");

    private final String maLoai;
    private final String tenHienThi;
    private final String moTa;

    LoaiSanPhamEnum(String maLoai, String tenHienThi, String moTa) {
        this.maLoai = maLoai;
        this.tenHienThi = tenHienThi;
        this.moTa = moTa;
    }

    public String getMaLoai() {
        return maLoai;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }

    public String getMoTa() {
        return moTa;
    }

    public static LoaiSanPhamEnum tuMa(String ma) {
        if (ma == null || ma.isBlank()) {
            return null;
        }
        String clean = ma.trim().toUpperCase().replace(" ", "_");
        for (LoaiSanPhamEnum l : values()) {
            if (l.maLoai.equalsIgnoreCase(clean) || l.name().equalsIgnoreCase(clean)) {
                return l;
            }
        }
        return null;
    }
}
