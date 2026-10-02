package vn.nhom10.crm.model;

/**
 * 4 nhóm danh mục bán hàng dùng chung theo Story S2-07:
 * - NGANH_NGHE: Ngành nghề khách hàng (bảng `nganh_nghe`)
 * - QUY_MO: Quy mô doanh nghiệp (bảng `quy_mo_doanh_nghiep`)
 * - NGUON_LEAD: Nguồn khách tiềm năng (bảng `nguon_lead`)
 * - LOAI_HOAT_DONG: Loại hoạt động chăm sóc (bảng `loai_hoat_dong`)
 */
public enum LoaiDanhMuc {
    NGANH_NGHE("NGANH_NGHE", "Ngành nghề khách hàng", "Danh mục phân loại lĩnh vực hoạt động của khách hàng doanh nghiệp", "🏢", "nganh_nghe", "ma_nganh", "ten_nganh"),
    QUY_MO("QUY_MO", "Quy mô doanh nghiệp", "Danh mục phân loại số lượng nhân sự hoặc quy mô doanh nghiệp", "👥", "quy_mo_doanh_nghiep", "ma_quy_mo", "ten_quy_mo"),
    NGUON_LEAD("NGUON_LEAD", "Nguồn khách tiềm năng", "Danh mục nguồn gốc phát sinh khách tiềm năng để đo lường hiệu quả", "🎯", "nguon_lead", "ma_nguon", "ten_nguon"),
    LOAI_HOAT_DONG("LOAI_HOAT_DONG", "Loại hoạt động chăm sóc", "Danh mục các hình thức tương tác với khách hàng", "📞", "loai_hoat_dong", "ma_loai", "ten_loai");

    private final String ma;
    private final String tenHienThi;
    private final String moTa;
    private final String icon;
    private final String tenBang;
    private final String cotMa;
    private final String cotTen;

    LoaiDanhMuc(String ma, String tenHienThi, String moTa, String icon, String tenBang, String cotMa, String cotTen) {
        this.ma = ma;
        this.tenHienThi = tenHienThi;
        this.moTa = moTa;
        this.icon = icon;
        this.tenBang = tenBang;
        this.cotMa = cotMa;
        this.cotTen = cotTen;
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

    public String getIcon() {
        return icon;
    }

    public String getTenBang() {
        return tenBang;
    }

    public String getCotMa() {
        return cotMa;
    }

    public String getCotTen() {
        return cotTen;
    }

    public static LoaiDanhMuc tuMa(String ma) {
        if (ma == null || ma.trim().isEmpty()) {
            return NGANH_NGHE;
        }
        for (LoaiDanhMuc item : values()) {
            if (item.ma.equalsIgnoreCase(ma.trim())) {
                return item;
            }
        }
        return NGANH_NGHE;
    }
}
