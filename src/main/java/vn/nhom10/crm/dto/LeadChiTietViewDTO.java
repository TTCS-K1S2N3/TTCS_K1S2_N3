package vn.nhom10.crm.dto;

import vn.nhom10.crm.model.Lead;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO dữ liệu hiển thị chi tiết Lead và thông tin SLA (Story S4-07, Mục 2.3 Contract).
 */
public class LeadChiTietViewDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum TrangThaiSlaEnum {
        DUNG_HAN,
        QUA_HAN,
        KHONG_XAC_DINH
    }

    private Lead lead;
    private TrangThaiSlaEnum trangThaiSla = TrangThaiSlaEnum.KHONG_XAC_DINH;
    private boolean coTheNhan = false;
    private boolean coTheTuChoi = false;
    private List<DanhMucLyDoTuChoiDTO> danhMucLyDoTuChoi;
    private String maLyDoTuChoiDaChon;
    private String lyDoTuChoiDaNhap;
    private String thongBao;
    private Map<String, String> loiTruong;
    private int soThongBaoChuaDoc = 0;

    public LeadChiTietViewDTO() {
    }

    public Lead getLead() {
        return lead;
    }

    public void setLead(Lead lead) {
        this.lead = lead;
    }

    public TrangThaiSlaEnum getTrangThaiSla() {
        return trangThaiSla;
    }

    public void setTrangThaiSla(TrangThaiSlaEnum trangThaiSla) {
        this.trangThaiSla = trangThaiSla;
    }

    public boolean isCoTheNhan() {
        return coTheNhan;
    }

    public void setCoTheNhan(boolean coTheNhan) {
        this.coTheNhan = coTheNhan;
    }

    public boolean isCoTheTuChoi() {
        return coTheTuChoi;
    }

    public void setCoTheTuChoi(boolean coTheTuChoi) {
        this.coTheTuChoi = coTheTuChoi;
    }

    public List<DanhMucLyDoTuChoiDTO> getDanhMucLyDoTuChoi() {
        return danhMucLyDoTuChoi;
    }

    public void setDanhMucLyDoTuChoi(List<DanhMucLyDoTuChoiDTO> danhMucLyDoTuChoi) {
        this.danhMucLyDoTuChoi = danhMucLyDoTuChoi;
    }

    public String getMaLyDoTuChoiDaChon() {
        return maLyDoTuChoiDaChon;
    }

    public void setMaLyDoTuChoiDaChon(String maLyDoTuChoiDaChon) {
        this.maLyDoTuChoiDaChon = maLyDoTuChoiDaChon;
    }

    public String getLyDoTuChoiDaNhap() {
        return lyDoTuChoiDaNhap;
    }

    public void setLyDoTuChoiDaNhap(String lyDoTuChoiDaNhap) {
        this.lyDoTuChoiDaNhap = lyDoTuChoiDaNhap;
    }

    public String getThongBao() {
        return thongBao;
    }

    public void setThongBao(String thongBao) {
        this.thongBao = thongBao;
    }

    public Map<String, String> getLoiTruong() {
        return loiTruong;
    }

    public void setLoiTruong(Map<String, String> loiTruong) {
        this.loiTruong = loiTruong;
    }

    public int getSoThongBaoChuaDoc() {
        return soThongBaoChuaDoc;
    }

    public void setSoThongBaoChuaDoc(int soThongBaoChuaDoc) {
        this.soThongBaoChuaDoc = soThongBaoChuaDoc;
    }
}
