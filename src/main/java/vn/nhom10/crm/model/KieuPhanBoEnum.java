package vn.nhom10.crm.model;

/**
 * Kiểu phân bổ Lead tự động (Story S4-06, AC1):
 * - THEO_KHU_VUC: Phân bổ dựa trên khu vực địa lý của Lead.
 * - THEO_NGANH_NGHE: Phân bổ dựa trên ngành nghề kinh doanh của Lead.
 * - XOAY_VONG: Phân bổ xoay vòng đều (Round-Robin) giữa các nhân viên kinh doanh trong nhóm.
 */
public enum KieuPhanBoEnum {
    THEO_KHU_VUC("THEO_KHU_VUC", "Theo khu vực", "Phân bổ theo khu vực địa lý của khách hàng"),
    THEO_NGANH_NGHE("THEO_NGANH_NGHE", "Theo ngành nghề", "Phân bổ theo ngành nghề kinh doanh"),
    XOAY_VONG("XOAY_VONG", "Xoay vòng nhóm", "Phân bổ xoay vòng đều cho các nhân viên trong nhóm");

    private final String ma;
    private final String tenHienThi;
    private final String moTa;

    KieuPhanBoEnum(String ma, String tenHienThi, String moTa) {
        this.ma = ma;
        this.tenHienThi = tenHienThi;
        this.moTa = moTa;
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

    public static KieuPhanBoEnum tuChuoi(String giaTri) {
        if (giaTri == null || giaTri.trim().isEmpty()) {
            return null;
        }
        String clean = giaTri.trim();
        for (KieuPhanBoEnum item : values()) {
            if (item.ma.equalsIgnoreCase(clean)
                    || item.name().equalsIgnoreCase(clean)
                    || item.tenHienThi.equalsIgnoreCase(clean)) {
                return item;
            }
        }
        return null;
    }
}
