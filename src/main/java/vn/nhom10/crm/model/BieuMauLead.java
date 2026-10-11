package vn.nhom10.crm.model;

import java.time.LocalDateTime;

/**
 * Model ánh xạ bảng 'bieu_mau_lead' phục vụ Story S4-01:
 * Thu thập lead từ biểu mẫu nhúng trên website.
 */
public class BieuMauLead {

    private Long id;
    private String maBieuMau;
    private String tenBieuMau;
    private String tokenCongKhai;
    private Long nguonLeadId;
    private String tenNguonLead;
    private Long chienDichId;
    private String tenChienDich;
    private String cauHinhTruongJson;
    private Integer gioiHanGuiMoiGio;
    private boolean batHoneypot = true;
    private boolean hoatDong = true;
    private Long createdBy;
    private String tenNguoiTao;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BieuMauLead() {
    }

    public BieuMauLead(String maBieuMau, String tenBieuMau, String tokenCongKhai, Long nguonLeadId) {
        this.maBieuMau = maBieuMau;
        this.tenBieuMau = tenBieuMau;
        this.tokenCongKhai = tokenCongKhai;
        this.nguonLeadId = nguonLeadId;
        this.batHoneypot = true;
        this.hoatDong = true;
        this.gioiHanGuiMoiGio = 10;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getNguonLeadId() {
        return nguonLeadId;
    }

    public void setNguonLeadId(Long nguonLeadId) {
        this.nguonLeadId = nguonLeadId;
    }

    public String getTenNguonLead() {
        return tenNguonLead;
    }

    public void setTenNguonLead(String tenNguonLead) {
        this.tenNguonLead = tenNguonLead;
    }

    public Long getChienDichId() {
        return chienDichId;
    }

    public void setChienDichId(Long chienDichId) {
        this.chienDichId = chienDichId;
    }

    public String getTenChienDich() {
        return tenChienDich;
    }

    public void setTenChienDich(String tenChienDich) {
        this.tenChienDich = tenChienDich;
    }

    public String getCauHinhTruongJson() {
        return cauHinhTruongJson;
    }

    public void setCauHinhTruongJson(String cauHinhTruongJson) {
        this.cauHinhTruongJson = cauHinhTruongJson;
    }

    public Integer getGioiHanGuiMoiGio() {
        return gioiHanGuiMoiGio;
    }

    public void setGioiHanGuiMoiGio(Integer gioiHanGuiMoiGio) {
        this.gioiHanGuiMoiGio = gioiHanGuiMoiGio;
    }

    public boolean isBatHoneypot() {
        return batHoneypot;
    }

    public void setBatHoneypot(boolean batHoneypot) {
        this.batHoneypot = batHoneypot;
    }

    public boolean isHoatDong() {
        return hoatDong;
    }

    public void setHoatDong(boolean hoatDong) {
        this.hoatDong = hoatDong;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getTenNguoiTao() {
        return tenNguoiTao;
    }

    public void setTenNguoiTao(String tenNguoiTao) {
        this.tenNguoiTao = tenNguoiTao;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Sinh mã nhúng Iframe tiêu chuẩn để dán vào website bất kỳ (AC1).
     */
    public String sinhMaNhungIframe(String baseUrl) {
        String url = sinhDuongDanNhung(baseUrl);
        return "<iframe src=\"" + url + "\" width=\"100%\" height=\"520\" frameborder=\"0\" " +
                "style=\"border:none;max-width:600px;width:100%;height:520px;display:block;margin:0 auto;\" " +
                "loading=\"lazy\" title=\"" + (tenBieuMau != null ? tenBieuMau : "Form thu thập Lead") + "\"></iframe>";
    }

    /**
     * Sinh mã nhúng Script nhúng trực tiếp vào trang web (AC1).
     */
    public String sinhMaNhungScript(String baseUrl) {
        String cleanBase = (baseUrl != null) ? baseUrl.replaceAll("/+$", "") : "";
        String token = (tokenCongKhai != null) ? tokenCongKhai : "";
        return "<div id=\"crm-form-" + token + "\"></div>\n" +
                "<script src=\"" + cleanBase + "/bieu-mau/embed.js?token=" + token + "\" async></script>";
    }

    /**
     * Sinh đường dẫn nhúng trực tiếp.
     */
    public String sinhDuongDanNhung(String baseUrl) {
        String cleanBase = (baseUrl != null) ? baseUrl.replaceAll("/+$", "") : "";
        String token = (tokenCongKhai != null) ? tokenCongKhai : "";
        return cleanBase + "/bieu-mau/nhung?token=" + token;
    }
}
