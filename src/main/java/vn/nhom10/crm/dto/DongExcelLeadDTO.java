package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO đại diện cho một dòng dữ liệu đọc từ tệp Excel nhập khách hàng tiềm năng.
 * Phục vụ Story S4-02:
 * - AC2: Xem trước và báo lỗi theo từng dòng
 * - AC3: Bắt buộc có nguồn lead cho mọi dòng
 */
public class DongExcelLeadDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int soDong;
    private String hoTen;
    private Long nguonLeadId;
    private String tenNguonLead;
    private String soDienThoai;
    private String email;
    private String tenCongTy;
    private String nhuCauQuanTam;
    private String mucDoQuanTam;

    private boolean hopLe = true;
    private List<String> danhSachLoi = new ArrayList<>();

    private boolean daNhap = false;
    private boolean thanhCong = false;
    private String ghiChuKetQua;
    private String maLead;
    private Long idLead;

    public DongExcelLeadDTO() {
    }

    public DongExcelLeadDTO(int soDong) {
        this.soDong = soDong;
    }

    public void themLoi(String loi) {
        if (loi != null && !loi.isBlank()) {
            this.danhSachLoi.add(loi.trim());
            this.hopLe = false;
        }
    }

    public String getChuoiLoi() {
        if (danhSachLoi == null || danhSachLoi.isEmpty()) {
            return "";
        }
        return String.join("; ", danhSachLoi);
    }

    public int getSoDong() {
        return soDong;
    }

    public void setSoDong(int soDong) {
        this.soDong = soDong;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
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

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getMucDoQuanTam() {
        return mucDoQuanTam;
    }

    public void setMucDoQuanTam(String mucDoQuanTam) {
        this.mucDoQuanTam = mucDoQuanTam;
    }

    public boolean isHopLe() {
        return hopLe;
    }

    public void setHopLe(boolean hopLe) {
        this.hopLe = hopLe;
    }

    public List<String> getDanhSachLoi() {
        return danhSachLoi;
    }

    public void setDanhSachLoi(List<String> danhSachLoi) {
        this.danhSachLoi = danhSachLoi != null ? danhSachLoi : new ArrayList<>();
        this.hopLe = this.danhSachLoi.isEmpty();
    }

    public boolean isDaNhap() {
        return daNhap;
    }

    public void setDaNhap(boolean daNhap) {
        this.daNhap = daNhap;
    }

    public boolean isThanhCong() {
        return thanhCong;
    }

    public void setThanhCong(boolean thanhCong) {
        this.thanhCong = thanhCong;
    }

    public String getGhiChuKetQua() {
        return ghiChuKetQua;
    }

    public void setGhiChuKetQua(String ghiChuKetQua) {
        this.ghiChuKetQua = ghiChuKetQua;
    }

    public String getMaLead() {
        return maLead;
    }

    public void setMaLead(String maLead) {
        this.maLead = maLead;
    }

    public Long getIdLead() {
        return idLead;
    }

    public void setIdLead(Long idLead) {
        this.idLead = idLead;
    }
}
