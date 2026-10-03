package vn.nhom10.crm.model;

/**
 * Phân loại giai đoạn trong Pipeline bán hàng (S2-09).
 * Tương ứng với cột loai_ket_thuc trong bảng giai_doan_pipeline:
 * - DANG_TIEN_HANH: loai_ket_thuc IS NULL hoặc 'TIEN_HANH'
 * - THANH_CONG: loai_ket_thuc = 'WIN'
 * - THAT_BAI: loai_ket_thuc = 'LOST'
 */
public enum LoaiGiaiDoanEnum {
    DANG_TIEN_HANH("DANG_TIEN_HANH", "Đang tiến hành", "Giai đoạn đang xúc tiến cơ hội", null),
    THANH_CONG("THANH_CONG", "Chốt thành công", "Cơ hội thắng (Win), đạt 100% doanh số", "WIN"),
    THAT_BAI("THAT_BAI", "Đóng thất bại", "Cơ hội thua (Lost), 0% doanh số", "LOST");

    private final String maLoai;
    private final String tenHienThi;
    private final String moTa;
    private final String maDb;

    LoaiGiaiDoanEnum(String maLoai, String tenHienThi, String moTa, String maDb) {
        this.maLoai = maLoai;
        this.tenHienThi = tenHienThi;
        this.moTa = moTa;
        this.maDb = maDb;
    }

    public String getMaLoai() {
        return maLoai;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }

    public String getMoTa() {
        return moTa;
    }

    public String getMaDb() {
        return maDb;
    }

    public static LoaiGiaiDoanEnum tuMa(String ma) {
        if (ma == null || ma.isBlank()) {
            return DANG_TIEN_HANH;
        }
        for (LoaiGiaiDoanEnum e : values()) {
            if (e.maLoai.equalsIgnoreCase(ma.trim()) || e.name().equalsIgnoreCase(ma.trim())) {
                return e;
            }
            if (e.maDb != null && e.maDb.equalsIgnoreCase(ma.trim())) {
                return e;
            }
        }
        return DANG_TIEN_HANH;
    }
}
