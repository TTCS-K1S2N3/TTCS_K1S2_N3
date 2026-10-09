package vn.nhom10.crm.dto;

import java.io.Serializable;

/**
 * DTO kết quả thực hiện gộp khách hàng (Story S3-04, AC3).
 * Xác nhận việc chuyển giao bảo toàn 100% người liên hệ, cơ hội và hoạt động.
 */
public class KetQuaGopKhachHangDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean thanhCong;
    private String thongBao;
    private Long khachHangNguonId;
    private Long khachHangDichId;
    private String tenKhachHangNguon;
    private String tenKhachHangDich;

    // Số lượng bản ghi liên quan đã được di chuyển bảo toàn
    private int soNguoiLienHeDaChuyen;
    private int soCoHoiDaChuyen;
    private int soHoatDongDaChuyen;
    private int soTepDinhKemDaChuyen;
    private int soBaoGiaDaChuyen;
    private int soHopDongDaChuyen;

    private Long lichSuGopId;

    public KetQuaGopKhachHangDTO() {
    }

    public static KetQuaGopKhachHangDTO thatBai(String thongBao) {
        KetQuaGopKhachHangDTO dto = new KetQuaGopKhachHangDTO();
        dto.setThanhCong(false);
        dto.setThongBao(thongBao);
        return dto;
    }

    public static KetQuaGopKhachHangDTO thanhCong(String thongBao, Long nguonId, Long dichId) {
        KetQuaGopKhachHangDTO dto = new KetQuaGopKhachHangDTO();
        dto.setThanhCong(true);
        dto.setThongBao(thongBao);
        dto.setKhachHangNguonId(nguonId);
        dto.setKhachHangDichId(dichId);
        return dto;
    }

    public boolean isThanhCong() {
        return thanhCong;
    }

    public void setThanhCong(boolean thanhCong) {
        this.thanhCong = thanhCong;
    }

    public String getThongBao() {
        return thongBao;
    }

    public void setThongBao(String thongBao) {
        this.thongBao = thongBao;
    }

    public Long getKhachHangNguonId() {
        return khachHangNguonId;
    }

    public void setKhachHangNguonId(Long khachHangNguonId) {
        this.khachHangNguonId = khachHangNguonId;
    }

    public Long getKhachHangDichId() {
        return khachHangDichId;
    }

    public void setKhachHangDichId(Long khachHangDichId) {
        this.khachHangDichId = khachHangDichId;
    }

    public String getTenKhachHangNguon() {
        return tenKhachHangNguon;
    }

    public void setTenKhachHangNguon(String tenKhachHangNguon) {
        this.tenKhachHangNguon = tenKhachHangNguon;
    }

    public String getTenKhachHangDich() {
        return tenKhachHangDich;
    }

    public void setTenKhachHangDich(String tenKhachHangDich) {
        this.tenKhachHangDich = tenKhachHangDich;
    }

    public int getSoNguoiLienHeDaChuyen() {
        return soNguoiLienHeDaChuyen;
    }

    public void setSoNguoiLienHeDaChuyen(int soNguoiLienHeDaChuyen) {
        this.soNguoiLienHeDaChuyen = soNguoiLienHeDaChuyen;
    }

    public int getSoCoHoiDaChuyen() {
        return soCoHoiDaChuyen;
    }

    public void setSoCoHoiDaChuyen(int soCoHoiDaChuyen) {
        this.soCoHoiDaChuyen = soCoHoiDaChuyen;
    }

    public int getSoHoatDongDaChuyen() {
        return soHoatDongDaChuyen;
    }

    public void setSoHoatDongDaChuyen(int soHoatDongDaChuyen) {
        this.soHoatDongDaChuyen = soHoatDongDaChuyen;
    }

    public int getSoTepDinhKemDaChuyen() {
        return soTepDinhKemDaChuyen;
    }

    public void setSoTepDinhKemDaChuyen(int soTepDinhKemDaChuyen) {
        this.soTepDinhKemDaChuyen = soTepDinhKemDaChuyen;
    }

    public int getSoBaoGiaDaChuyen() {
        return soBaoGiaDaChuyen;
    }

    public void setSoBaoGiaDaChuyen(int soBaoGiaDaChuyen) {
        this.soBaoGiaDaChuyen = soBaoGiaDaChuyen;
    }

    public int getSoHopDongDaChuyen() {
        return soHopDongDaChuyen;
    }

    public void setSoHopDongDaChuyen(int soHopDongDaChuyen) {
        this.soHopDongDaChuyen = soHopDongDaChuyen;
    }

    public Long getLichSuGopId() {
        return lichSuGopId;
    }

    public void setLichSuGopId(Long lichSuGopId) {
        this.lichSuGopId = lichSuGopId;
    }
}
