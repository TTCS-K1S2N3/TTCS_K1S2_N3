package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO chứa 3 chỉ số thống kê KPI của Chiến dịch theo AC3 (Story S4-03).
 * Khớp hoàn toàn với schema CampaignMetricsView trong Contract FE-BE.
 */
public class ChienDichThongKeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long campaignId;
    private long leadCount = 0;              // Số lượng Lead distinct gắn chiến dịch
    private long opportunityCount = 0;       // Số lượng Cơ hội distinct gắn chiến dịch
    private BigDecimal closedWonAmount = BigDecimal.ZERO; // Tổng giá trị chốt thực tế của cơ hội Thắng
    private String currencyCode = "VND";

    public ChienDichThongKeDTO() {
    }

    public ChienDichThongKeDTO(Long campaignId, long leadCount, long opportunityCount, BigDecimal closedWonAmount) {
        this.campaignId = campaignId;
        this.leadCount = leadCount;
        this.opportunityCount = opportunityCount;
        this.closedWonAmount = closedWonAmount != null ? closedWonAmount : BigDecimal.ZERO;
        this.currencyCode = "VND";
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public long getLeadCount() {
        return leadCount;
    }

    public void setLeadCount(long leadCount) {
        this.leadCount = leadCount;
    }

    // Alias cho soLead
    public long getSoLead() {
        return leadCount;
    }

    public void setSoLead(long soLead) {
        this.leadCount = soLead;
    }

    public long getOpportunityCount() {
        return opportunityCount;
    }

    public void setOpportunityCount(long opportunityCount) {
        this.opportunityCount = opportunityCount;
    }

    // Alias cho soCoHoi
    public long getSoCoHoi() {
        return opportunityCount;
    }

    public void setSoCoHoi(long soCoHoi) {
        this.opportunityCount = soCoHoi;
    }

    public BigDecimal getClosedWonAmount() {
        return closedWonAmount;
    }

    public void setClosedWonAmount(BigDecimal closedWonAmount) {
        this.closedWonAmount = closedWonAmount != null ? closedWonAmount : BigDecimal.ZERO;
    }

    // Alias cho giaTriDaChot
    public BigDecimal getGiaTriDaChot() {
        return closedWonAmount;
    }

    public void setGiaTriDaChot(BigDecimal giaTriDaChot) {
        this.closedWonAmount = giaTriDaChot != null ? giaTriDaChot : BigDecimal.ZERO;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
}
