package vn.nhom10.crm.model;

/**
 * Các vai trò người dùng trong hệ thống CRM tương ứng với phạm vi dữ liệu tối đa được cấp:
 * - SALES_REP: Nhân viên kinh doanh -> Tối đa: CA_NHAN
 * - TEAM_LEAD: Trưởng nhóm kinh doanh -> Tối đa: NHOM
 * - DIRECTOR: Giám đốc kinh doanh -> Tối đa: TOAN_BO
 * - ADMIN: Quản trị viên hệ thống -> Tối đa: TOAN_BO
 */
public enum VaiTroNguoiDung {
    SALES_REP("SALES_REP", "Nhân viên kinh doanh", PhamViDuLieu.CA_NHAN),
    TEAM_LEAD("TEAM_LEAD", "Trưởng nhóm kinh doanh", PhamViDuLieu.NHOM),
    DIRECTOR("DIRECTOR", "Giám đốc kinh doanh", PhamViDuLieu.TOAN_BO),
    ADMIN("ADMIN", "Quản trị hệ thống", PhamViDuLieu.TOAN_BO);

    private final String ma;
    private final String tenHienThi;
    private final PhamViDuLieu phamViToiDa;

    VaiTroNguoiDung(String ma, String tenHienThi, PhamViDuLieu phamViToiDa) {
        this.ma = ma;
        this.tenHienThi = tenHienThi;
        this.phamViToiDa = phamViToiDa;
    }

    public String getMa() {
        return ma;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }

    public PhamViDuLieu getPhamViToiDa() {
        return phamViToiDa;
    }

    public static VaiTroNguoiDung tuMa(String ma) {
        if (ma == null || ma.trim().isEmpty()) {
            return SALES_REP;
        }
        for (VaiTroNguoiDung vt : values()) {
            if (vt.ma.equalsIgnoreCase(ma.trim())) {
                return vt;
            }
        }
        return SALES_REP;
    }
}
