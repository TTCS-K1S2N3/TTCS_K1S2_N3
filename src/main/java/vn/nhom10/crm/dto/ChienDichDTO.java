package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO dữ liệu hiển thị và form khai báo Chiến dịch (Story S4-03, AC1).
 * Tương thích cấu trúc CampaignView và CampaignFormView theo Contract FE-BE đã duyệt.
 */
public class ChienDichDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long campaignId;
    private String maChienDich;
    private String name;               // Tên chiến dịch (tenChienDich)
    private BigDecimal budgetAmount;   // Ngân sách (nganSach)
    private String currencyCode = "VND";
    private LocalDate startDate;       // Ngày bắt đầu (ngayBatDau)
    private LocalDate endDate;         // Ngày kết thúc (ngayKetThuc)
    private String channelCode;        // Mã kênh (kenh)
    private String channelLabel;       // Nhãn kênh hiển thị
    private String trangThai;
    private String moTa;

    // Các trường String hỗ trợ bind form khi có lỗi validation (CampaignFormView)
    private String budgetAmountStr;
    private String startDateStr;
    private String endDateStr;

    public ChienDichDTO() {
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    // Alias cho id
    public Long getId() {
        return campaignId;
    }

    public void setId(Long id) {
        this.campaignId = id;
    }

    public String getMaChienDich() {
        return maChienDich;
    }

    public void setMaChienDich(String maChienDich) {
        this.maChienDich = maChienDich;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Alias cho tenChienDich
    public String getTenChienDich() {
        return name;
    }

    public void setTenChienDich(String tenChienDich) {
        this.name = tenChienDich;
    }

    public BigDecimal getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(BigDecimal budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    // Alias cho nganSach
    public BigDecimal getNganSach() {
        return budgetAmount;
    }

    public void setNganSach(BigDecimal nganSach) {
        this.budgetAmount = nganSach;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    // Alias cho ngayBatDau
    public LocalDate getNgayBatDau() {
        return startDate;
    }

    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.startDate = ngayBatDau;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    // Alias cho ngayKetThuc
    public LocalDate getNgayKetThuc() {
        return endDate;
    }

    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.endDate = ngayKetThuc;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    // Alias cho kenh
    public String getKenh() {
        return channelCode;
    }

    public void setKenh(String kenh) {
        this.channelCode = kenh;
    }

    public String getChannelLabel() {
        return channelLabel;
    }

    public void setChannelLabel(String channelLabel) {
        this.channelLabel = channelLabel;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getBudgetAmountStr() {
        return budgetAmountStr;
    }

    public void setBudgetAmountStr(String budgetAmountStr) {
        this.budgetAmountStr = budgetAmountStr;
    }

    public String getStartDateStr() {
        return startDateStr;
    }

    public void setStartDateStr(String startDateStr) {
        this.startDateStr = startDateStr;
    }

    public String getEndDateStr() {
        return endDateStr;
    }

    public void setEndDateStr(String endDateStr) {
        this.endDateStr = endDateStr;
    }
}
