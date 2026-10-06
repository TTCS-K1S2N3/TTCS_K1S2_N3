package vn.nhom10.crm.model;

import java.util.Arrays;
import java.util.List;

/**
 * Trạng thái khách hàng chuẩn theo Acceptance Criteria Story S3-01:
 * - Tiềm năng (TIEM_NANG)
 * - Đang giao dịch (DANG_GIAO_DICH)
 * - Khách hàng (KHACH_HANG)
 * - Ngừng hợp tác (NGUNG_HOP_TAC)
 */
public enum TrangThaiKhachHangEnum {
    TIEM_NANG("TIEM_NANG", "Tiềm năng", "Khách hàng mới tiếp cận hoặc đang tìm hiểu nhu cầu"),
    DANG_GIAO_DICH("DANG_GIAO_DICH", "Đang giao dịch", "Khách hàng đang trong quá trình báo giá, đàm phán hợp đồng"),
    KHACH_HANG("KHACH_HANG", "Khách hàng", "Khách hàng chính thức đã ký hợp đồng hoặc có giao dịch thành công"),
    NGUNG_HOP_TAC("NGUNG_HOP_TAC", "Ngừng hợp tác", "Khách hàng tạm dừng hoặc chấm dứt quan hệ giao dịch");

    private final String ma;
    private final String tenHienThi;
    private final String moTa;

    TrangThaiKhachHangEnum(String ma, String tenHienThi, String moTa) {
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
    public static TrangThaiKhachHangEnum tuChuoi(String giaTri) {
        if (giaTri == null || giaTri.trim().isEmpty()) {
            return null;
        }
        String clean = giaTri.trim();
        for (TrangThaiKhachHangEnum item : values()) {
            if (item.ma.equalsIgnoreCase(clean)
                    || item.name().equalsIgnoreCase(clean)
                    || item.tenHienThi.equalsIgnoreCase(clean)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Kiểm tra xem chuỗi có đại diện cho một trạng thái hợp lệ trong 4 trạng thái của S3-01 hay không.
     */
    public static boolean laHopLe(String giaTri) {
        return tuChuoi(giaTri) != null;
    }

    /**
     * Chuẩn hóa trạng thái về tên hiển thị chuẩn (Tiềm năng, Đang giao dịch, Khách hàng, Ngừng hợp tác)
     * hoặc trả về giá trị mặc định "Tiềm năng" nếu null/rỗng.
     */
    public static String chuanHoaTen(String giaTri) {
        TrangThaiKhachHangEnum en = tuChuoi(giaTri);
        return en != null ? en.getTenHienThi() : TIEM_NANG.getTenHienThi();
    }

    /**
     * Chuẩn hóa trạng thái về mã lưu trữ DB (TIEM_NANG, DANG_GIAO_DICH, KHACH_HANG, NGUNG_HOP_TAC).
     */
    public static String chuanHoaMa(String giaTri) {
        TrangThaiKhachHangEnum en = tuChuoi(giaTri);
        return en != null ? en.getMa() : TIEM_NANG.getMa();
    }

    public static List<TrangThaiKhachHangEnum> danhSachTatCa() {
        return Arrays.asList(values());
    }
}
