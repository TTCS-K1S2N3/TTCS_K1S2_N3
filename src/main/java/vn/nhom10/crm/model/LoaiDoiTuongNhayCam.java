package vn.nhom10.crm.model;

/**
 * Định nghĩa các loại đối tượng nhạy cảm bắt buộc phải ghi lại nhật ký thay đổi (Story S2-04).
 * Bao gồm: Chiết khấu, Chỉ tiêu doanh số, Quyền sở hữu dữ liệu, Vai trò người dùng.
 */
public enum LoaiDoiTuongNhayCam {
    CHIET_KHAU(
            "CHIET_KHAU",
            "Chiết khấu",
            "Chiết khấu báo giá, hợp đồng, phụ lục bán hàng",
            "badge-chiet-khau",
            "percent"
    ),
    CHI_TIEU(
            "CHI_TIEU",
            "Chỉ tiêu doanh số",
            "Chỉ tiêu doanh số nhóm, KPI nhân viên, hạn ngạch kinh doanh",
            "badge-chi-tieu",
            "target"
    ),
    QUYEN_SO_HUU(
            "QUYEN_SO_HUU",
            "Quyền sở hữu dữ liệu",
            "Người phụ trách khách hàng, người sở hữu cơ hội và đầu mối",
            "badge-quyen-so-huu",
            "user-check"
    ),
    VAI_TRO_NGUOI_DUNG(
            "VAI_TRO_NGUOI_DUNG",
            "Vai trò người dùng",
            "Phân quyền hệ thống, vai trò tài khoản và nhóm kinh doanh",
            "badge-vai-tro",
            "shield"
    );

    private final String ma;
    private final String tenHienThi;
    private final String moTa;
    private final String classMauSac;
    private final String bieuTuong;

    LoaiDoiTuongNhayCam(String ma, String tenHienThi, String moTa, String classMauSac, String bieuTuong) {
        this.ma = ma;
        this.tenHienThi = tenHienThi;
        this.moTa = moTa;
        this.classMauSac = classMauSac;
        this.bieuTuong = bieuTuong;
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

    public String getClassMauSac() {
        return classMauSac;
    }

    public String getBieuTuong() {
        return bieuTuong;
    }

    public static LoaiDoiTuongNhayCam tuMa(String ma) {
        if (ma == null || ma.trim().isEmpty()) {
            return null;
        }
        String clean = ma.trim().toUpperCase();
        for (LoaiDoiTuongNhayCam item : values()) {
            if (item.ma.equalsIgnoreCase(clean) || item.name().equalsIgnoreCase(clean)) {
                return item;
            }
        }
        return null;
    }
}
