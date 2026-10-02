package vn.nhom10.crm.model;

/**
 * Ba phạm vi dữ liệu bắt buộc theo Story S1-05 và Kiến trúc hệ thống CRM:
 * - CA_NHAN: Chỉ xem/sửa dữ liệu do chính người dùng sở hữu hoặc phụ trách trực tiếp.
 * - NHOM: Xem/sửa dữ liệu thuộc nhóm kinh doanh hiện tại và các nhóm con.
 * - TOAN_BO: Xem/sửa toàn bộ dữ liệu trên toàn hệ thống công ty.
 */
public enum PhamViDuLieu {
    CA_NHAN("CA_NHAN", "Của tôi", "Chỉ dữ liệu do chính bạn phụ trách hoặc sở hữu"),
    NHOM("NHOM", "Nhóm của tôi", "Dữ liệu thuộc nhóm kinh doanh bạn đang phụ trách quản lý"),
    TOAN_BO("TOAN_BO", "Tất cả", "Toàn bộ dữ liệu khách hàng và bán hàng trên toàn hệ thống");

    private final String ma;
    private final String tenHienThi;
    private final String moTa;

    PhamViDuLieu(String ma, String tenHienThi, String moTa) {
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
     * Chuyển đổi từ chuỗi mã sang enum. Mặc định trả về CA_NHAN nếu không hợp lệ để đảm bảo an toàn.
     */
    public static PhamViDuLieu tuMa(String ma) {
        if (ma == null || ma.trim().isEmpty()) {
            return CA_NHAN;
        }
        for (PhamViDuLieu pv : values()) {
            if (pv.ma.equalsIgnoreCase(ma.trim())) {
                return pv;
            }
        }
        return CA_NHAN;
    }
}
