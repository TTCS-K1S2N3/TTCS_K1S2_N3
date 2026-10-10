package vn.nhom10.crm.model;

/**
 * Phân cấp 4 mức quyền hệ thống theo schema vai_tro_module:
 * NONE (0) -> READ (1) -> WRITE (2) -> FULL (3)
 *
 * Mapping CRUD theo thiết kế bảo mật:
 * - NONE:  Xem: false, Tạo: false, Sửa: false, Xóa: false
 * - READ:  Xem: true,  Tạo: false, Sửa: false, Xóa: false
 * - WRITE: Xem: true,  Tạo: true,  Sửa: true,  Xóa: false
 * - FULL:  Xem: true,  Tạo: true,  Sửa: true,  Xóa: true
 */
public enum MucQuyen {
    NONE("NONE", 0, "Không có quyền", false, false, false, false),
    READ("READ", 1, "Chỉ xem", true, false, false, false),
    WRITE("WRITE", 2, "Xem và sửa", true, true, true, false),
    FULL("FULL", 3, "Toàn quyền", true, true, true, true);

    private final String ma;
    private final int capDo;
    private final String tenHienThi;
    private final boolean xem;
    private final boolean tao;
    private final boolean sua;
    private final boolean xoa;

    MucQuyen(String ma, int capDo, String tenHienThi, boolean xem, boolean tao, boolean sua, boolean xoa) {
        this.ma = ma;
        this.capDo = capDo;
        this.tenHienThi = tenHienThi;
        this.xem = xem;
        this.tao = tao;
        this.sua = sua;
        this.xoa = xoa;
    }

    public String getMa() {
        return ma;
    }

    public int getCapDo() {
        return capDo;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }

    public boolean isXem() {
        return xem;
    }

    public boolean isTao() {
        return tao;
    }

    public boolean isSua() {
        return sua;
    }

    public boolean isXoa() {
        return xoa;
    }

    /**
     * Kiểm tra xem mức quyền hiện tại có bao gồm (>=) mức quyền yêu cầu hay không.
     * Ví dụ: WRITE bao gồm READ, FULL bao gồm WRITE.
     */
    public boolean baoGom(MucQuyen yeuCau) {
        if (yeuCau == null || yeuCau == NONE) {
            return true;
        }
        return this.capDo >= yeuCau.capDo;
    }

    public static MucQuyen tuMa(String ma) {
        if (ma == null || ma.isBlank()) {
            return NONE;
        }
        for (MucQuyen mq : values()) {
            if (mq.ma.equalsIgnoreCase(ma.trim())) {
                return mq;
            }
        }
        return NONE;
    }

    /**
     * Tính toán mức quyền từ tổ hợp CRUD theo quy tắc phân cấp chặt chẽ:
     * NONE -> READ -> WRITE -> FULL
     */
    public static MucQuyen tuCrud(boolean xem, boolean tao, boolean sua, boolean xoa) {
        if (xoa) {
            return FULL;
        }
        if (tao || sua) {
            return WRITE;
        }
        if (xem) {
            return READ;
        }
        return NONE;
    }
}
