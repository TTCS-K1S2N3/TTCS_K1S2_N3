package vn.nhom10.crm.model;

/**
 * Danh sách 7 vai trò chuẩn của hệ thống CRM.
 */
public enum VaiTroEnum {
    ADMIN("ADMIN", "Quản trị hệ thống", "Admin", PhamViDuLieu.TOAN_BO),
    DIRECTOR("DIRECTOR", "Giám đốc kinh doanh", "Director", PhamViDuLieu.TOAN_BO),
    TEAM_LEAD("TEAM_LEAD", "Trưởng nhóm kinh doanh", "Team Lead", PhamViDuLieu.NHOM),
    SALES_REP("SALES_REP", "Nhân viên kinh doanh", "Sales Rep", PhamViDuLieu.CA_NHAN),
    MARKETING("MARKETING", "Nhân viên Marketing", "Marketing", PhamViDuLieu.CA_NHAN),
    CUST_SUCCESS("CUST_SUCCESS", "Chăm sóc khách hàng", "Cust. Success", PhamViDuLieu.CA_NHAN),
    ACCOUNTANT("ACCOUNTANT", "Kế toán", "Accountant", PhamViDuLieu.TOAN_BO);

    private final String maVaiTro;
    private final String tenTiengViet;
    private final String tenTiengAnh;
    private final PhamViDuLieu phamViToiDa;

    VaiTroEnum(String maVaiTro, String tenTiengViet, String tenTiengAnh, PhamViDuLieu phamViToiDa) {
        this.maVaiTro = maVaiTro;
        this.tenTiengViet = tenTiengViet;
        this.tenTiengAnh = tenTiengAnh;
        this.phamViToiDa = phamViToiDa;
    }

    public String getMaVaiTro() {
        return maVaiTro;
    }

    public String getMa() {
        return maVaiTro;
    }

    public String getTenTiengViet() {
        return tenTiengViet;
    }

    public String getTenHienThi() {
        return tenTiengViet;
    }

    public String getTenTiengAnh() {
        return tenTiengAnh;
    }

    public PhamViDuLieu getPhamViToiDa() {
        return phamViToiDa;
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
