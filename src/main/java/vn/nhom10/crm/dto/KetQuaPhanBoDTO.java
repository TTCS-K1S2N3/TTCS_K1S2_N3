package vn.nhom10.crm.dto;

/**
 * DTO kết quả thực thi phân bổ tự động cho batch hoặc cho 1 lead (Story S4-06, AC4).
 */
public class KetQuaPhanBoDTO {

    private int soLeadDaQuet;
    private int soLeadPhanBoThanhCong;
    private int soLeadVaoHangCho;
    private long thoiGianXuLyMs;
    private String thongDiep;

    public KetQuaPhanBoDTO() {
    }

    public KetQuaPhanBoDTO(int soLeadDaQuet, int soLeadPhanBoThanhCong, int soLeadVaoHangCho, long thoiGianXuLyMs) {
        this.soLeadDaQuet = soLeadDaQuet;
        this.soLeadPhanBoThanhCong = soLeadPhanBoThanhCong;
        this.soLeadVaoHangCho = soLeadVaoHangCho;
        this.thoiGianXuLyMs = thoiGianXuLyMs;
    }

    public int getSoLeadDaQuet() {
        return soLeadDaQuet;
    }

    public void setSoLeadDaQuet(int soLeadDaQuet) {
        this.soLeadDaQuet = soLeadDaQuet;
    }

    public int getSoLeadPhanBoThanhCong() {
        return soLeadPhanBoThanhCong;
    }

    public void setSoLeadPhanBoThanhCong(int soLeadPhanBoThanhCong) {
        this.soLeadPhanBoThanhCong = soLeadPhanBoThanhCong;
    }

    public int getSoLeadVaoHangCho() {
        return soLeadVaoHangCho;
    }

    public void setSoLeadVaoHangCho(int soLeadVaoHangCho) {
        this.soLeadVaoHangCho = soLeadVaoHangCho;
    }

    public long getThoiGianXuLyMs() {
        return thoiGianXuLyMs;
    }

    public void setThoiGianXuLyMs(long thoiGianXuLyMs) {
        this.thoiGianXuLyMs = thoiGianXuLyMs;
    }

    public String getThongDiep() {
        return thongDiep;
    }

    public void setThongDiep(String thongDiep) {
        this.thongDiep = thongDiep;
    }
}
