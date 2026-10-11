package vn.nhom10.crm.model;

/**
 * Trạng thái của Lead (Khách hàng tiềm năng):
 * - MOI: Mới tạo từ biểu mẫu hoặc nguồn khác, chưa tiếp nhận/xử lý (AC4 của S4-01).
 * - DANG_CHAM_SOC: Nhân viên kinh doanh đã nhận và đang liên hệ (S4-07).
 * - DA_CHUYEN_DOI: Đã đủ điều kiện và chuyển đổi thành Khách hàng & Cơ hội (S4-08).
 * - TU_CHOI: Lead không phù hợp hoặc bị từ chối.
 * - DA_GOP: Lead bị gộp vào bản ghi khác do trùng thông tin (S4-04).
 */
public enum TrangThaiLeadEnum {
    MOI("MOI", "Mới", "Lead mới thu thập được, chưa tiếp nhận xử lý"),
    DANG_CHAM_SOC("DANG_CHAM_SOC", "Đang chăm sóc", "Nhân viên kinh doanh đang chủ động liên hệ"),
    DA_CHUYEN_DOI("DA_CHUYEN_DOI", "Đã chuyển đổi", "Đã chuyển đổi thành hồ sơ khách hàng chính thức"),
    TU_CHOI("TU_CHOI", "Từ chối", "Lead không tiềm năng hoặc từ chối hợp tác"),
    DA_GOP("DA_GOP", "Đã gộp", "Lead đã được gộp vào bản ghi khác");

    private final String ma;
    private final String tenHienThi;
    private final String moTa;

    TrangThaiLeadEnum(String ma, String tenHienThi, String moTa) {
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

    /**
     * Chuyển đổi linh hoạt từ chuỗi (mã hoặc tên hiển thị) sang Enum.
     * Trả về null nếu không hợp lệ.
     */
    public static TrangThaiLeadEnum tuChuoi(String giaTri) {
        if (giaTri == null || giaTri.trim().isEmpty()) {
            return null;
        }
        String clean = giaTri.trim();
        for (TrangThaiLeadEnum item : values()) {
            if (item.ma.equalsIgnoreCase(clean)
                    || item.name().equalsIgnoreCase(clean)
                    || item.tenHienThi.equalsIgnoreCase(clean)) {
                return item;
            }
        }
        return null;
    }
}
