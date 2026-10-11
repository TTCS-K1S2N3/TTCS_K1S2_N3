package vn.nhom10.crm.dto;

/**
 * DTO chứa thông tin mã nhúng và liên kết biểu mẫu để dán vào website bất kỳ (AC1).
 */
public class MaNhungDTO {

    private String maBieuMau;
    private String tenBieuMau;
    private String tokenCongKhai;
    private String maIframe;
    private String maScript;
    private String duongDanTrucTiep;

    public MaNhungDTO() {
    }

    public MaNhungDTO(String maBieuMau, String tenBieuMau, String tokenCongKhai, String maIframe, String maScript, String duongDanTrucTiep) {
        this.maBieuMau = maBieuMau;
        this.tenBieuMau = tenBieuMau;
        this.tokenCongKhai = tokenCongKhai;
        this.maIframe = maIframe;
        this.maScript = maScript;
        this.duongDanTrucTiep = duongDanTrucTiep;
    }

    public String getMaBieuMau() {
        return maBieuMau;
    }

    public void setMaBieuMau(String maBieuMau) {
        this.maBieuMau = maBieuMau;
    }

    public String getTenBieuMau() {
        return tenBieuMau;
    }

    public void setTenBieuMau(String tenBieuMau) {
        this.tenBieuMau = tenBieuMau;
    }

    public String getTokenCongKhai() {
        return tokenCongKhai;
    }

    public void setTokenCongKhai(String tokenCongKhai) {
        this.tokenCongKhai = tokenCongKhai;
    }

    public String getMaIframe() {
        return maIframe;
    }

    public void setMaIframe(String maIframe) {
        this.maIframe = maIframe;
    }

    public String getMaScript() {
        return maScript;
    }

    public void setMaScript(String maScript) {
        this.maScript = maScript;
    }

    public String getDuongDanTrucTiep() {
        return duongDanTrucTiep;
    }

    public void setDuongDanTrucTiep(String duongDanTrucTiep) {
        this.duongDanTrucTiep = duongDanTrucTiep;
    }
}
