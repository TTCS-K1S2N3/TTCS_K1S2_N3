package vn.nhom10.crm.dto;

import vn.nhom10.crm.model.Lead;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO dữ liệu so sánh hai Lead trước khi thực hiện gộp (Story S4-04, AC3).
 */
public class SoSanhLeadDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Lead sourceLead;
    private Lead targetLead;

    // Thống kê lịch sử bảo toàn
    private int soHoatDongNguon = 0;
    private int soHoatDongDich = 0;
    private int soCongViecNguon = 0;
    private int soCongViecDich = 0;
    private int soCoHoiNguon = 0;
    private int soCoHoiDich = 0;

    private List<String> matchReasons = new ArrayList<>();
    private boolean canMerge = false;
    private String thongDiepCanhBao;

    public SoSanhLeadDTO() {
    }

    public SoSanhLeadDTO(Lead sourceLead, Lead targetLead) {
        this.sourceLead = sourceLead;
        this.targetLead = targetLead;
    }

    public Lead getSourceLead() {
        return sourceLead;
    }

    public void setSourceLead(Lead sourceLead) {
        this.sourceLead = sourceLead;
    }

    public Lead getTargetLead() {
        return targetLead;
    }

    public void setTargetLead(Lead targetLead) {
        this.targetLead = targetLead;
    }

    public int getSoHoatDongNguon() {
        return soHoatDongNguon;
    }

    public void setSoHoatDongNguon(int soHoatDongNguon) {
        this.soHoatDongNguon = soHoatDongNguon;
    }

    public int getSoHoatDongDich() {
        return soHoatDongDich;
    }

    public void setSoHoatDongDich(int soHoatDongDich) {
        this.soHoatDongDich = soHoatDongDich;
    }

    public int getSoCongViecNguon() {
        return soCongViecNguon;
    }

    public void setSoCongViecNguon(int soCongViecNguon) {
        this.soCongViecNguon = soCongViecNguon;
    }

    public int getSoCongViecDich() {
        return soCongViecDich;
    }

    public void setSoCongViecDich(int soCongViecDich) {
        this.soCongViecDich = soCongViecDich;
    }

    public int getSoCoHoiNguon() {
        return soCoHoiNguon;
    }

    public void setSoCoHoiNguon(int soCoHoiNguon) {
        this.soCoHoiNguon = soCoHoiNguon;
    }

    public int getSoCoHoiDich() {
        return soCoHoiDich;
    }

    public void setSoCoHoiDich(int soCoHoiDich) {
        this.soCoHoiDich = soCoHoiDich;
    }

    public int getTongLichSuNguon() {
        return soHoatDongNguon + soCongViecNguon + soCoHoiNguon;
    }

    public int getTongLichSuDich() {
        return soHoatDongDich + soCongViecDich + soCoHoiDich;
    }

    public int getTongLichSuSauGop() {
        return getTongLichSuNguon() + getTongLichSuDich();
    }

    public List<String> getMatchReasons() {
        return matchReasons;
    }

    public void setMatchReasons(List<String> matchReasons) {
        this.matchReasons = matchReasons != null ? matchReasons : new ArrayList<>();
    }

    public boolean isCanMerge() {
        return canMerge;
    }

    public void setCanMerge(boolean canMerge) {
        this.canMerge = canMerge;
    }

    public String getThongDiepCanhBao() {
        return thongDiepCanhBao;
    }

    public void setThongDiepCanhBao(String thongDiepCanhBao) {
        this.thongDiepCanhBao = thongDiepCanhBao;
    }
}
