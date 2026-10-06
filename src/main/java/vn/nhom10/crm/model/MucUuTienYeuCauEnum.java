package vn.nhom10.crm.model;

/**
 * Mức độ ưu tiên của Yêu cầu hỗ trợ (Story S3-08).
 * Tương ứng với cột `muc_uu_tien` VARCHAR(20) trong bảng `yeu_cau_ho_tro`.
 */
public enum MucUuTienYeuCauEnum {
    THAP("THAP", "Thấp", "badge-secondary", 1),
    BINH_THUONG("BINH_THUONG", "Bình thường", "badge-info", 2),
    CAO("CAO", "Cao", "badge-warning", 3),
    KHAN_CAP("KHAN_CAP", "Khẩn cấp", "badge-danger", 4);

    private final String ma;
    private final String tenHienThi;
    private final String cssClass;
    private final int capDo;

    MucUuTienYeuCauEnum(String ma, String tenHienThi, String cssClass, int capDo) {
        this.ma = ma;
        this.tenHienThi = tenHienThi;
        this.cssClass = cssClass;
        this.capDo = capDo;
    }

    public String getMa() {
        return ma;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }

    public String getCssClass() {
        return cssClass;
    }

    public int getCapDo() {
        return capDo;
    }

    public static MucUuTienYeuCauEnum tuMa(String ma) {
        if (ma == null || ma.isBlank()) {
            return BINH_THUONG;
        }
        for (MucUuTienYeuCauEnum item : values()) {
            if (item.ma.equalsIgnoreCase(ma.trim())) {
                return item;
            }
        }
        return BINH_THUONG;
    }
}
