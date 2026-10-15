package vn.nhom10.crm.dto;

import java.io.Serializable;

/**
 * DTO ghép cặp một chiến dịch và các chỉ số thống kê (Story S4-03, AC3).
 * Khớp đúng cấu trúc CampaignRowView trong Contract FE-BE (mục 3.2).
 */
public class ChienDichRowDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private ChienDichDTO campaign;
    private ChienDichThongKeDTO metrics;

    public ChienDichRowDTO() {
    }

    public ChienDichRowDTO(ChienDichDTO campaign, ChienDichThongKeDTO metrics) {
        this.campaign = campaign;
        this.metrics = metrics;
    }

    public ChienDichDTO getCampaign() {
        return campaign;
    }

    public void setCampaign(ChienDichDTO campaign) {
        this.campaign = campaign;
    }

    // Alias tiếng Việt
    public ChienDichDTO getChienDich() {
        return campaign;
    }

    public void setChienDich(ChienDichDTO chienDich) {
        this.campaign = chienDich;
    }

    public ChienDichThongKeDTO getMetrics() {
        return metrics;
    }

    public void setMetrics(ChienDichThongKeDTO metrics) {
        this.metrics = metrics;
    }

    // Alias tiếng Việt
    public ChienDichThongKeDTO getThongKe() {
        return metrics;
    }

    public void setThongKe(ChienDichThongKeDTO thongKe) {
        this.metrics = thongKe;
    }
}
