package vn.nhom10.crm.model;

/**
 * Enum đại diện cho các vai trò trong quyết định mua của Người liên hệ (Story S3-02, S3-03).
 * Theo Acceptance Criteria:
 * - Người quyết định (Decision Maker)
 * - Người ảnh hưởng (Influencer)
 * - Người dùng cuối (End User)
 * - Người cản trở (Blocker)
 */
public enum VaiTroQuyetDinhEnum {
    NGUOI_QUYET_DINH("NGUOI_QUYET_DINH", "Người quyết định", "Người có thẩm quyền đưa ra quyết định mua hàng cuối cùng", "badge-role decision-maker", "#4f46e5"),
    NGUOI_ANH_HUONG("NGUOI_ANH_HUONG", "Người ảnh hưởng", "Người tư vấn, đề xuất hoặc tác động lớn đến quyết định mua", "badge-role", "#0ea5e9"),
    NGUOI_DUNG_CUOI("NGUOI_DUNG_CUOI", "Người dùng cuối", "Người trực tiếp vận hành hoặc sử dụng giải pháp/sản phẩm", "badge-role", "#10b981"),
    NGUOI_CAN_TRO("NGUOI_CAN_TRO", "Người cản trở", "Người có xu hướng phản đối, nghi ngại hoặc cản trở tiến trình thương vụ", "badge-role danger", "#ef4444");

    private final String ma;
    private final String tenHienThi;
    private final String moTa;
    private final String badgeClass;
    private final String mauSac;

    VaiTroQuyetDinhEnum(String ma, String tenHienThi, String moTa, String badgeClass, String mauSac) {
        this.ma = ma;
        this.tenHienThi = tenHienThi;
        this.moTa = moTa;
        this.badgeClass = badgeClass;
        this.mauSac = mauSac;
    }

    public String getMa() {
        return ma;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }

    public String getMoTa() {
        return moTa;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public String getMauSac() {
        return mauSac;
    }

    /**
     * Parse chuỗi mã hoặc tên hiển thị sang VaiTroQuyetDinhEnum.
     * Trả về null nếu giá trị đầu vào null hoặc rỗng.
     */
    public static VaiTroQuyetDinhEnum fromMa(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String clean = value.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        for (VaiTroQuyetDinhEnum v : values()) {
            if (v.ma.equalsIgnoreCase(clean) || v.name().equalsIgnoreCase(clean)) {
                return v;
            }
        }
        for (VaiTroQuyetDinhEnum v : values()) {
            if (v.tenHienThi.equalsIgnoreCase(value.trim())) {
                return v;
            }
        }
        return null;
    }
}
