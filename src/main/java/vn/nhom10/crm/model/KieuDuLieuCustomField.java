package vn.nhom10.crm.model;

/**
 * Các kiểu dữ liệu được hỗ trợ cho trường tuỳ chỉnh theo Story S2-08:
 * - Văn bản (VAN_BAN)
 * - Số (SO)
 * - Ngày (NGAY)
 * - Danh sách chọn (DANH_SACH_CHON)
 */
public enum KieuDuLieuCustomField {
    VAN_BAN("VAN_BAN", "Văn bản"),
    SO("SO", "Số"),
    NGAY("NGAY", "Ngày"),
    DANH_SACH_CHON("DANH_SACH_CHON", "Danh sách chọn");

    private final String ma;
    private final String tenHienThi;

    KieuDuLieuCustomField(String ma, String tenHienThi) {
        this.ma = ma;
        this.tenHienThi = tenHienThi;
    }

    public String getMa() {
        return ma;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }

    public static KieuDuLieuCustomField tuMa(String ma) {
        if (ma == null || ma.isBlank()) {
            return null;
        }
        for (KieuDuLieuCustomField k : values()) {
            if (k.ma.equalsIgnoreCase(ma.trim()) || k.name().equalsIgnoreCase(ma.trim())) {
                return k;
            }
        }
        return null;
    }
}
