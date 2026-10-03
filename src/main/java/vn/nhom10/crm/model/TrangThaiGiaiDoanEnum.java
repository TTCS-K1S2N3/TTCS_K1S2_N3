package vn.nhom10.crm.model;

/**
 * Trạng thái áp dụng của giai đoạn Pipeline (S2-09).
 * Ánh xạ tới cột hoat_dong trong bảng giai_doan_pipeline:
 * - DANG_AP_DUNG: hoat_dong = 1
 * - NGUNG_AP_DUNG: hoat_dong = 0
 * Hỗ trợ AC 4: Thay đổi cấu hình không làm hỏng cơ hội đang chạy (Ngừng áp dụng thay vì xóa cứng).
 */
public enum TrangThaiGiaiDoanEnum {
    DANG_AP_DUNG("DANG_AP_DUNG", "Đang áp dụng", "Áp dụng cho các cơ hội hiện tại và tạo mới", 1),
    NGUNG_AP_DUNG("NGUNG_AP_DUNG", "Ngừng áp dụng", "Không nhận cơ hội mới, nhưng giữ nguyên cơ hội cũ", 0);

    private final String maTrangThai;
    private final String tenHienThi;
    private final String moTa;
    private final int giaTriDb;

    TrangThaiGiaiDoanEnum(String maTrangThai, String tenHienThi, String moTa, int giaTriDb) {
        this.maTrangThai = maTrangThai;
        this.tenHienThi = tenHienThi;
        this.moTa = moTa;
        this.giaTriDb = giaTriDb;
    }

    public String getMaTrangThai() {
        return maTrangThai;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }

    public String getMoTa() {
        return moTa;
    }

    public int getGiaTriDb() {
        return giaTriDb;
    }

    public static TrangThaiGiaiDoanEnum tuMa(String ma) {
        if (ma == null || ma.isBlank()) {
            return DANG_AP_DUNG;
        }
        for (TrangThaiGiaiDoanEnum e : values()) {
            if (e.maTrangThai.equalsIgnoreCase(ma.trim()) || e.name().equalsIgnoreCase(ma.trim())) {
                return e;
            }
        }
        return DANG_AP_DUNG;
    }

    public static TrangThaiGiaiDoanEnum tuMaStrict(String ma) {
        if (ma == null || ma.isBlank()) {
            return null;
        }
        for (TrangThaiGiaiDoanEnum e : values()) {
            if (e.maTrangThai.equalsIgnoreCase(ma.trim()) || e.name().equalsIgnoreCase(ma.trim())) {
                return e;
            }
        }
        return null;
    }

    public static TrangThaiGiaiDoanEnum tuGiaTriDb(int giaTri) {
        return giaTri == 1 ? DANG_AP_DUNG : NGUNG_AP_DUNG;
    }
}
