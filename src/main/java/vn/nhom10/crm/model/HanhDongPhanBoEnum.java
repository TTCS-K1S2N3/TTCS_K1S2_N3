package vn.nhom10.crm.model;

/**
 * Hành động phân bổ ghi nhận vào nhật ký 'lich_su_phan_bo_lead' (Story S4-06 & S4-07):
 * - TU_DONG_THEO_QUY_TAC: Hệ thống tự động gán theo quy tắc phân bổ (S4-06).
 * - VAO_HANG_CHO: Lead không khớp quy tắc, được đưa vào hàng chờ phân tay (S4-06).
 * - TRUONG_NHOM_PHAN_TAY: Trưởng nhóm hoặc Giám đốc gán thủ công từ hàng chờ (S4-06).
 * - PHAN_BO: Phân bổ lead (S4-07).
 * - CHUYEN_GIAO: Chuyển giao lead giữa các nhân viên (S4-07).
 * - NHAN: Nhân viên tiếp nhận lead (S4-07).
 * - TU_CHOI: Nhân viên từ chối lead và trả về hàng chờ (S4-07).
 */
public enum HanhDongPhanBoEnum {
    TU_DONG_THEO_QUY_TAC("TU_DONG_THEO_QUY_TAC", "Phân bổ tự động theo quy tắc"),
    VAO_HANG_CHO("VAO_HANG_CHO", "Chuyển vào hàng chờ phân bổ"),
    TRUONG_NHOM_PHAN_TAY("TRUONG_NHOM_PHAN_TAY", "Trưởng nhóm phân bổ thủ công"),
    PHAN_BO("PHAN_BO", "Phân bổ lead"),
    CHUYEN_GIAO("CHUYEN_GIAO", "Chuyển giao lead"),
    NHAN("NHAN", "Tiếp nhận lead"),
    TU_CHOI("TU_CHOI", "Từ chối lead");

    private final String ma;
    private final String tenHienThi;

    HanhDongPhanBoEnum(String ma, String tenHienThi) {
        this.ma = ma;
        this.tenHienThi = tenHienThi;
    }

    public String getMa() {
        return ma;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }

    public static HanhDongPhanBoEnum tuChuoi(String giaTri) {
        if (giaTri == null || giaTri.trim().isEmpty()) {
            return null;
        }
        String clean = giaTri.trim();
        for (HanhDongPhanBoEnum item : values()) {
            if (item.ma.equalsIgnoreCase(clean) || item.name().equalsIgnoreCase(clean)) {
                return item;
            }
        }
        return null;
    }
}
