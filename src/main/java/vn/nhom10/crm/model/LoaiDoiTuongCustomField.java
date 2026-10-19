package vn.nhom10.crm.model;

/**
 * Các loại đối tượng hỗ trợ trường tuỳ chỉnh theo Story S2-08:
 * - Khách hàng (KHACH_HANG)
 * - Cơ hội (CO_HOI)
 */
public enum LoaiDoiTuongCustomField {
    KHACH_HANG("KHACH_HANG", "Khách hàng"),
    CO_HOI("CO_HOI", "Cơ hội");

    private final String ma;
    private final String tenHienThi;

    LoaiDoiTuongCustomField(String ma, String tenHienThi) {
        this.ma = ma;
        this.tenHienThi = tenHienThi;
    }

    public String getMa() {
        return ma;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }

    public static LoaiDoiTuongCustomField tuMa(String ma) {
        if (ma == null || ma.isBlank()) {
            return null;
        }
        for (LoaiDoiTuongCustomField l : values()) {
            if (l.ma.equalsIgnoreCase(ma.trim()) || l.name().equalsIgnoreCase(ma.trim())) {
                return l;
            }
        }
        return null;
    }
}
