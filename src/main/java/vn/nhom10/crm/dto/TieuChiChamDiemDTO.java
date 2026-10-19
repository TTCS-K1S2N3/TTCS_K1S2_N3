package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO đại diện cho một tiêu chí trong quy tắc chấm điểm Lead (Story S4-05 - AC1).
 * Gồm 4 nhóm tiêu chí chuẩn:
 * - INDUSTRY_FIT   : Ngành nghề phù hợp
 * - COMPANY_SIZE   : Quy mô doanh nghiệp
 * - SOURCE         : Nguồn thu thập lead
 * - INTEREST_LEVEL : Mức độ quan tâm
 */
public class TieuChiChamDiemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String key;
    private String maQuyTac;
    private String tenTieuChi;
    private String moTa;
    private String icon;
    private boolean kichHoat;
    private int soDiem;
    private int thuTu;
    private List<GiaTriTuyChonDTO> tuyChonGiaTri;

    public TieuChiChamDiemDTO() {
        this.kichHoat = true;
        this.soDiem = 0;
        this.tuyChonGiaTri = new ArrayList<>();
    }

    public TieuChiChamDiemDTO(String key, String maQuyTac, String tenTieuChi, String moTa,
                              String icon, boolean kichHoat, int soDiem, int thuTu) {
        this.key = key;
        this.maQuyTac = maQuyTac;
        this.tenTieuChi = tenTieuChi;
        this.moTa = moTa;
        this.icon = icon;
        this.kichHoat = kichHoat;
        this.soDiem = soDiem;
        this.thuTu = thuTu;
        this.tuyChonGiaTri = new ArrayList<>();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMaQuyTac() {
        return maQuyTac;
    }

    public void setMaQuyTac(String maQuyTac) {
        this.maQuyTac = maQuyTac;
    }

    public String getTenTieuChi() {
        return tenTieuChi;
    }

    public void setTenTieuChi(String tenTieuChi) {
        this.tenTieuChi = tenTieuChi;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isKichHoat() {
        return kichHoat;
    }

    public void setKichHoat(boolean kichHoat) {
        this.kichHoat = kichHoat;
    }

    public int getSoDiem() {
        return soDiem;
    }

    public void setSoDiem(int soDiem) {
        this.soDiem = soDiem;
    }

    public int getThuTu() {
        return thuTu;
    }

    public void setThuTu(int thuTu) {
        this.thuTu = thuTu;
    }

    public List<GiaTriTuyChonDTO> getTuyChonGiaTri() {
        return tuyChonGiaTri;
    }

    public void setTuyChonGiaTri(List<GiaTriTuyChonDTO> tuyChonGiaTri) {
        this.tuyChonGiaTri = tuyChonGiaTri;
    }

    public void themTuyChon(String ma, String ten, int diem) {
        this.tuyChonGiaTri.add(new GiaTriTuyChonDTO(ma, ten, diem));
    }

    /**
     * DTO con biểu diễn giá trị cụ thể trong nhóm tiêu chí
     */
    public static class GiaTriTuyChonDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private String ma;
        private String ten;
        private int diem;

        public GiaTriTuyChonDTO() {}

        public GiaTriTuyChonDTO(String ma, String ten, int diem) {
            this.ma = ma;
            this.ten = ten;
            this.diem = diem;
        }

        public String getMa() { return ma; }
        public void setMa(String ma) { this.ma = ma; }
        public String getTen() { return ten; }
        public void setTen(String ten) { this.ten = ten; }
        public int getDiem() { return diem; }
        public void setDiem(int diem) { this.diem = diem; }
    }
}
