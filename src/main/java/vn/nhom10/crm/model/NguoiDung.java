package vn.nhom10.crm.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Đại diện cho người dùng hệ thống (nguoi_dung).
 */
public class NguoiDung implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String hoTen;
    private String email;
    private String matKhauHash;
    private String soDienThoai;
    private String chuKyEmail;
    private String anhDaiDienPath;
    private String anhDaiDienThumbPath;
    private String trangThai; // CHO_KICH_HOAT, HOAT_DONG, BI_KHOA
    private int soLanDangNhapSai;
    private Timestamp khoaDen;
    private boolean batBuocDoiMatKhau;
    private Timestamp ngayDoiMatKhau;
    private int sessionVersion;
    private Long nhomKinhDoanhId;
    private Timestamp emailDaXacThucLuc;
    private Timestamp lanDangNhapCuoi;
    private Long createdBy;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    private List<VaiTro> danhSachVaiTro = new ArrayList<>();

    public NguoiDung() {
        this.trangThai = "CHO_KICH_HOAT";
        this.soLanDangNhapSai = 0;
        this.sessionVersion = 1;
        this.batBuocDoiMatKhau = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMatKhauHash() {
        return matKhauHash;
    }

    public void setMatKhauHash(String matKhauHash) {
        this.matKhauHash = matKhauHash;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getChuKyEmail() {
        return chuKyEmail;
    }

    public void setChuKyEmail(String chuKyEmail) {
        this.chuKyEmail = chuKyEmail;
    }

    public String getAnhDaiDienPath() {
        return anhDaiDienPath;
    }

    public void setAnhDaiDienPath(String anhDaiDienPath) {
        this.anhDaiDienPath = anhDaiDienPath;
    }

    public String getAnhDaiDienThumbPath() {
        return anhDaiDienThumbPath;
    }

    public void setAnhDaiDienThumbPath(String anhDaiDienThumbPath) {
        this.anhDaiDienThumbPath = anhDaiDienThumbPath;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public int getSoLanDangNhapSai() {
        return soLanDangNhapSai;
    }

    public void setSoLanDangNhapSai(int soLanDangNhapSai) {
        this.soLanDangNhapSai = soLanDangNhapSai;
    }

    public Timestamp getKhoaDen() {
        return khoaDen;
    }

    public void setKhoaDen(Timestamp khoaDen) {
        this.khoaDen = khoaDen;
    }

    public boolean isBatBuocDoiMatKhau() {
        return batBuocDoiMatKhau;
    }

    public void setBatBuocDoiMatKhau(boolean batBuocDoiMatKhau) {
        this.batBuocDoiMatKhau = batBuocDoiMatKhau;
    }

    public Timestamp getNgayDoiMatKhau() {
        return ngayDoiMatKhau;
    }

    public void setNgayDoiMatKhau(Timestamp ngayDoiMatKhau) {
        this.ngayDoiMatKhau = ngayDoiMatKhau;
    }

    public int getSessionVersion() {
        return sessionVersion;
    }

    public void setSessionVersion(int sessionVersion) {
        this.sessionVersion = sessionVersion;
    }

    public Long getNhomKinhDoanhId() {
        return nhomKinhDoanhId;
    }

    public void setNhomKinhDoanhId(Long nhomKinhDoanhId) {
        this.nhomKinhDoanhId = nhomKinhDoanhId;
    }

    public Timestamp getEmailDaXacThucLuc() {
        return emailDaXacThucLuc;
    }

    public void setEmailDaXacThucLuc(Timestamp emailDaXacThucLuc) {
        this.emailDaXacThucLuc = emailDaXacThucLuc;
    }

    public Timestamp getLanDangNhapCuoi() {
        return lanDangNhapCuoi;
    }

    public void setLanDangNhapCuoi(Timestamp lanDangNhapCuoi) {
        this.lanDangNhapCuoi = lanDangNhapCuoi;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<VaiTro> getDanhSachVaiTro() {
        return danhSachVaiTro;
    }

    public void setDanhSachVaiTro(List<VaiTro> danhSachVaiTro) {
        this.danhSachVaiTro = (danhSachVaiTro != null) ? danhSachVaiTro : new ArrayList<>();
    }

    /**
     * Lấy vai trò chính hiển thị (ưu tiên ADMIN > DIRECTOR > TEAM_LEAD > SALES_REP > vai trò khác).
     */
    public VaiTro getVaiTroChinh() {
        if (danhSachVaiTro == null || danhSachVaiTro.isEmpty()) {
            return null;
        }
        for (String code : new String[]{"ADMIN", "DIRECTOR", "TEAM_LEAD", "SALES_REP", "MARKETING", "CUST_SUCCESS", "ACCOUNTANT"}) {
            for (VaiTro vt : danhSachVaiTro) {
                if (code.equalsIgnoreCase(vt.getMaVaiTro())) {
                    return vt;
                }
            }
        }
        return danhSachVaiTro.get(0);
    }

    public boolean coVaiTro(String maVaiTro) {
        if (danhSachVaiTro == null || maVaiTro == null) {
            return false;
        }
        for (VaiTro vt : danhSachVaiTro) {
            if (maVaiTro.equalsIgnoreCase(vt.getMaVaiTro())) {
                return true;
            }
        }
        return false;
    }
}
