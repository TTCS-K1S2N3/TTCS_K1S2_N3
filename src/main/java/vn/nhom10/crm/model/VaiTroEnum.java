package vn.nhom10.crm.model;

/**
 * Danh sách 7 vai trò chuẩn của hệ thống CRM.
 */
public enum VaiTroEnum {
    ADMIN("ADMIN", "Quản trị hệ thống", "Admin"),
    DIRECTOR("DIRECTOR", "Giám đốc kinh doanh", "Director"),
    TEAM_LEAD("TEAM_LEAD", "Trưởng nhóm kinh doanh", "Team Lead"),
    SALES_REP("SALES_REP", "Nhân viên kinh doanh", "Sales Rep"),
    MARKETING("MARKETING", "Nhân viên Marketing", "Marketing"),
    CUST_SUCCESS("CUST_SUCCESS", "Chăm sóc khách hàng", "Cust. Success"),
    ACCOUNTANT("ACCOUNTANT", "Kế toán", "Accountant");

    private final String maVaiTro;
    private final String tenTiengViet;
    private final String tenTiengAnh;

    VaiTroEnum(String maVaiTro, String tenTiengViet, String tenTiengAnh) {
        this.maVaiTro = maVaiTro;
        this.tenTiengViet = tenTiengViet;
        this.tenTiengAnh = tenTiengAnh;
    }

    public String getMaVaiTro() {
        return maVaiTro;
    }

    public String getTenTiengViet() {
        return tenTiengViet;
    }

    public String getTenTiengAnh() {
        return tenTiengAnh;
    }

    public static VaiTroEnum tuMa(String ma) {
        if (ma == null || ma.isBlank()) {
            return null;
        }
        String chuanHoa = ma.trim().toUpperCase().replace(" ", "_");
        for (VaiTroEnum vt : values()) {
            if (vt.maVaiTro.equalsIgnoreCase(chuanHoa)
                    || vt.name().equalsIgnoreCase(chuanHoa)
                    || vt.tenTiengAnh.equalsIgnoreCase(ma.trim())) {
                return vt;
            }
        }
        return null;
    }
}
