package vn.nhom10.crm.dto;

/**
 * DTO nhận dữ liệu gửi từ biểu mẫu nhúng trên website (Story S4-01).
 * Gồm: họ tên, email, số điện thoại, công ty, nhu cầu quan tâm, token và trường chống spam.
 */
public class GuiFormLeadDTO {

    private String token;
    private String hoTen;
    private String email;
    private String soDienThoai;
    private String tenCongTy;
    private String nhuCauQuanTam;
    private String hpWebsite; // Honeypot field (ẩn ở giao diện, bot điền vào sẽ bị chặn spam)
    private String diaChiIp;

    public GuiFormLeadDTO() {
    }

    public GuiFormLeadDTO(String token, String hoTen, String email, String soDienThoai, String tenCongTy, String nhuCauQuanTam) {
        this.token = token;
        this.hoTen = hoTen;
        this.email = email;
        this.soDienThoai = soDienThoai;
        this.tenCongTy = tenCongTy;
        this.nhuCauQuanTam = nhuCauQuanTam;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getTenCongTy() {
        return tenCongTy;
    }

    public void setTenCongTy(String tenCongTy) {
        this.tenCongTy = tenCongTy;
    }

    public String getNhuCauQuanTam() {
        return nhuCauQuanTam;
    }

    public void setNhuCauQuanTam(String nhuCauQuanTam) {
        this.nhuCauQuanTam = nhuCauQuanTam;
    }

    public String getHpWebsite() {
        return hpWebsite;
    }

    public void setHpWebsite(String hpWebsite) {
        this.hpWebsite = hpWebsite;
    }

    public String getDiaChiIp() {
        return diaChiIp;
    }

    public void setDiaChiIp(String diaChiIp) {
        this.diaChiIp = diaChiIp;
    }
}
