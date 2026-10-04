package vn.nhom10.crm.model;

/**
 * Định nghĩa hành động tác động lên dữ liệu nhạy cảm trong hệ thống CRM.
 */
public enum HanhDongThayDoi {
    CAP_NHAT("CAP_NHAT", "Cập nhật", "badge-hanh-dong-cap-nhat"),
    THEM_MOI("THEM_MOI", "Thêm mới", "badge-hanh-dong-them"),
    XOA("XOA", "Xóa", "badge-hanh-dong-xoa"),
    CHUYEN_QUYEN("CHUYEN_QUYEN", "Chuyển giao quyền", "badge-hanh-dong-chuyen-quyen");

    private final String ma;
    private final String tenHienThi;
    private final String classMauSac;

    HanhDongThayDoi(String ma, String tenHienThi, String classMauSac) {
        this.ma = ma;
        this.tenHienThi = tenHienThi;
        this.classMauSac = classMauSac;
    }

    public String getMa() {
        return ma;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }

    public String getClassMauSac() {
        return classMauSac;
    }

    public static HanhDongThayDoi tuMa(String ma) {
        if (ma == null || ma.trim().isEmpty()) {
            return null;
        }
        String clean = ma.trim().toUpperCase();
        for (HanhDongThayDoi item : values()) {
            if (item.ma.equalsIgnoreCase(clean) || item.name().equalsIgnoreCase(clean)) {
                return item;
            }
        }
        return null;
    }
}
