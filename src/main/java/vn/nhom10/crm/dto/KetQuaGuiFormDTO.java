package vn.nhom10.crm.dto;

/**
 * DTO kết quả xử lý gửi biểu mẫu lead (Story S4-01).
 */
public class KetQuaGuiFormDTO {

    private boolean thanhCong;
    private String thongBao;
    private String maLead;
    private Long leadId;
    private String lyDoTuChoi;
    private int maHttp;

    public KetQuaGuiFormDTO() {
    }

    public static KetQuaGuiFormDTO thanhCong(String maLead, Long leadId, String thongBao) {
        KetQuaGuiFormDTO dto = new KetQuaGuiFormDTO();
        dto.setThanhCong(true);
        dto.setMaLead(maLead);
        dto.setLeadId(leadId);
        dto.setThongBao(thongBao);
        dto.setMaHttp(200);
        return dto;
    }

    public static KetQuaGuiFormDTO thanhCong(String maLead, String thongBao) {
        return thanhCong(maLead, null, thongBao);
    }

    public static KetQuaGuiFormDTO thatBai(int maHttp, String thongBao, String lyDoTuChoi) {
        KetQuaGuiFormDTO dto = new KetQuaGuiFormDTO();
        dto.setThanhCong(false);
        dto.setMaHttp(maHttp);
        dto.setThongBao(thongBao);
        dto.setLyDoTuChoi(lyDoTuChoi);
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

    public String getMaLead() {
        return maLead;
    }

    public void setMaLead(String maLead) {
        this.maLead = maLead;
    }

    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

    public String getLyDoTuChoi() {
        return lyDoTuChoi;
    }

    public void setLyDoTuChoi(String lyDoTuChoi) {
        this.lyDoTuChoi = lyDoTuChoi;
    }

    public int getMaHttp() {
        return maHttp;
    }

    public void setMaHttp(int maHttp) {
        this.maHttp = maHttp;
    }
}
