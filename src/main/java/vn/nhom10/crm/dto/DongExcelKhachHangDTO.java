package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO đại diện cho một dòng dữ liệu đọc từ tệp Excel danh sách khách hàng.
 * Phục vụ Story S3-06:
 * - Xem trước dữ liệu và báo lỗi theo từng dòng
 * - Đánh dấu bản ghi trùng (Mã số thuế, Mã KH hoặc Tên công ty) để chọn bỏ qua hoặc cập nhật
 */
public class DongExcelKhachHangDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int soDong;
    private String maKhachHang;
    private String tenCongTy;
    private String maSoThue;
    private String nganhNghe;
    private String quyMo;
    private String website;
    private String diaChi;
    private String doanhThuUocTinh;
    private String trangThai;
    private String moTaChiTiet;

    // Trạng thái thẩm định
    private boolean hopLe = true;
    private List<String> danhSachLoi = new ArrayList<>();

    // Trạng thái trùng lặp (AC 2)
    private boolean biTrung = false;
    private String loaiTrung;              // Ví dụ: TRUNG_MST, TRUNG_TEN, TRUNG_MA, TRUNG_TRONG_TEP
    private String moTaTrung;              // Diễn giải chi tiết lý do trùng
    private Long idKhachHangTrung;         // ID bản ghi đã có trong hệ thống nếu tìm thấy
    private String luaChonXuLy = "BO_QUA"; // Mặc định: "BO_QUA" hoặc "CAP_NHAT"

    // Kết quả sau khi nhập
    private boolean daNhap = false;
    private boolean thanhCong = false;
    private String ghiChuKetQua;

    public DongExcelKhachHangDTO() {
    }

    public DongExcelKhachHangDTO(int soDong) {
        this.soDong = soDong;
    }

    public void themLoi(String thongBaoLoi) {
        if (thongBaoLoi != null && !thongBaoLoi.isBlank()) {
            this.danhSachLoi.add(thongBaoLoi.trim());
            this.hopLe = false;
        }
    }

    public String getChuoiLoi() {
        if (danhSachLoi == null || danhSachLoi.isEmpty()) {
            return "";
        }
        return String.join("; ", danhSachLoi);
    }

    public void danhDauTrung(String loaiTrung, String moTaTrung, Long idKhachHangTrung) {
        this.biTrung = true;
        this.loaiTrung = loaiTrung;
        this.moTaTrung = moTaTrung;
        this.idKhachHangTrung = idKhachHangTrung;
    }

    // Getters and Setters

    public int getSoDong() {
        return soDong;
    }

    public void setSoDong(int soDong) {
        this.soDong = soDong;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getTenCongTy() {
        return tenCongTy;
    }

    public void setTenCongTy(String tenCongTy) {
        this.tenCongTy = tenCongTy;
    }

    public String getMaSoThue() {
        return maSoThue;
    }

    public void setMaSoThue(String maSoThue) {
        this.maSoThue = maSoThue;
    }

    public String getNganhNghe() {
        return nganhNghe;
    }

    public void setNganhNghe(String nganhNghe) {
        this.nganhNghe = nganhNghe;
    }

    public String getQuyMo() {
        return quyMo;
    }

    public void setQuyMo(String quyMo) {
        this.quyMo = quyMo;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getDoanhThuUocTinh() {
        return doanhThuUocTinh;
    }

    public void setDoanhThuUocTinh(String doanhThuUocTinh) {
        this.doanhThuUocTinh = doanhThuUocTinh;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getMoTaChiTiet() {
        return moTaChiTiet;
    }

    public void setMoTaChiTiet(String moTaChiTiet) {
        this.moTaChiTiet = moTaChiTiet;
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
        if (!this.danhSachLoi.isEmpty()) {
            this.hopLe = false;
        }
    }

    public boolean isBiTrung() {
        return biTrung;
    }

    public void setBiTrung(boolean biTrung) {
        this.biTrung = biTrung;
    }

    public String getLoaiTrung() {
        return loaiTrung;
    }

    public void setLoaiTrung(String loaiTrung) {
        this.loaiTrung = loaiTrung;
    }

    public String getMoTaTrung() {
        return moTaTrung;
    }

    public void setMoTaTrung(String moTaTrung) {
        this.moTaTrung = moTaTrung;
    }

    public Long getIdKhachHangTrung() {
        return idKhachHangTrung;
    }

    public void setIdKhachHangTrung(Long idKhachHangTrung) {
        this.idKhachHangTrung = idKhachHangTrung;
    }

    public String getLuaChonXuLy() {
        return luaChonXuLy;
    }

    public void setLuaChonXuLy(String luaChonXuLy) {
        this.luaChonXuLy = luaChonXuLy;
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
}
