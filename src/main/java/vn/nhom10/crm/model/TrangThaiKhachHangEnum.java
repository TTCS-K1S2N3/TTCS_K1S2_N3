package vn.nhom10.crm.model;

import java.util.Arrays;
import java.util.List;

/**
 * Trạng thái khách hàng theo quy chuẩn CRM:
 * - TIEM_NANG: Tiềm năng
 * - DANG_GIAO_DICH: Đang giao dịch
 * - KHACH_HANG: Khách hàng
 * - NGUNG_HOP_TAC: Ngừng hợp tác
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

    public static boolean laHopLe(String giaTri) {
        return tuChuoi(giaTri) != null;
    }

    public static String chuanHoaTen(String giaTri) {
        TrangThaiKhachHangEnum en = tuChuoi(giaTri);
        return en != null ? en.getTenHienThi() : TIEM_NANG.getTenHienThi();
    }

    public static String chuanHoaMa(String giaTri) {
        TrangThaiKhachHangEnum en = tuChuoi(giaTri);
        return en != null ? en.getMa() : TIEM_NANG.getMa();
    }

    public static List<TrangThaiKhachHangEnum> danhSachTatCa() {
        return Arrays.asList(values());
    }
}
