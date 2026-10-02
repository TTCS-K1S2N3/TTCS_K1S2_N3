package vn.nhom10.crm.model;

/**
 * Trạng thái kinh doanh của sản phẩm dịch vụ theo Acceptance Criteria Story S2-05:
 * Sản phẩm đã xuất hiện trong báo giá thì không xoá được, chỉ ngừng kinh doanh.
 */
public enum TrangThaiSanPhamEnum {
    DANG_KINH_DOANH("DANG_KINH_DOANH", "Đang kinh doanh"),
    NGUNG_KINH_DOANH("NGUNG_KINH_DOANH", "Ngừng kinh doanh");

    private final String maTrangThai;
    private final String tenHienThi;

    TrangThaiSanPhamEnum(String maTrangThai, String tenHienThi) {
        this.maTrangThai = maTrangThai;
        this.tenHienThi = tenHienThi;
    }

    public String getMaTrangThai() {
        return maTrangThai;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }

    public static TrangThaiSanPhamEnum tuMa(String ma) {
        if (ma == null || ma.isBlank()) {
            return null;
        }
        String clean = ma.trim().toUpperCase().replace(" ", "_");
        for (TrangThaiSanPhamEnum tt : values()) {
            if (tt.maTrangThai.equalsIgnoreCase(clean) || tt.name().equalsIgnoreCase(clean)) {
                return tt;
            }
        }
        return null;
    }
}
