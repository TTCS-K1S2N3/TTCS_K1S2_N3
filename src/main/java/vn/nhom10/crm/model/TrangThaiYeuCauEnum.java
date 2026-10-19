package vn.nhom10.crm.model;

/**
 * Trạng thái xử lý của Yêu cầu hỗ trợ (Story S3-08).
 * Tương ứng với cột `trang_thai` VARCHAR(30) trong bảng `yeu_cau_ho_tro`.
 */
public enum TrangThaiYeuCauEnum {
    MOI("MOI", "Mới tiếp nhận", "badge-info", true),
    DANG_XU_LY("DANG_XU_LY", "Đang xử lý", "badge-primary", true),
    CHO_KHACH_HANG("CHO_KHACH_HANG", "Chờ khách hàng", "badge-warning", true),
    DA_XU_LY("DA_XU_LY", "Đã xử lý", "badge-success", false),
    DONG("DONG", "Đã đóng", "badge-secondary", false),
    HUY("HUY", "Đã hủy", "badge-dark", false);

    private final String ma;
    private final String tenHienThi;
    private final String cssClass;
    private final boolean chuaXuLy;

    TrangThaiYeuCauEnum(String ma, String tenHienThi, String cssClass, boolean chuaXuLy) {
        this.ma = ma;
        this.tenHienThi = tenHienThi;
        this.cssClass = cssClass;
        this.chuaXuLy = chuaXuLy;
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

    /**
     * Kiểm tra yêu cầu này có thuộc nhóm "chưa xử lý" hay không.
     * Các yêu cầu MOI, DANG_XU_LY, CHO_KHACH_HANG là chưa xử lý (tính vào cờ rủi ro rời bỏ).
     */
    public boolean isChuaXuLy() {
        return chuaXuLy;
    }

    public static TrangThaiYeuCauEnum tuMa(String ma) {
        if (ma == null || ma.isBlank()) {
            return MOI;
        }
        for (TrangThaiYeuCauEnum item : values()) {
            if (item.ma.equalsIgnoreCase(ma.trim())) {
                return item;
            }
        }
        return MOI;
    }
}
