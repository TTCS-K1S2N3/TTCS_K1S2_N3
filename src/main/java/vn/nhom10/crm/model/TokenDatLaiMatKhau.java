package vn.nhom10.crm.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Model ánh xạ bảng token_dat_lai_mat_khau (S1-03).
 */
public class TokenDatLaiMatKhau implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long nguoiDungId;
    private String tokenHash;
    private Timestamp taoLuc;
    private Timestamp hetHanLuc;
    private Timestamp daSuDungLuc;
    private String diaChiIpYeuCau;

    // Thuộc tính tiện ích khi join dữ liệu
    private String userEmail;
    private String userTrangThai;

    public TokenDatLaiMatKhau() {
    }

    public TokenDatLaiMatKhau(Long nguoiDungId, String tokenHash, Timestamp hetHanLuc, String diaChiIpYeuCau) {
        this.nguoiDungId = nguoiDungId;
        this.tokenHash = tokenHash;
        this.hetHanLuc = hetHanLuc;
        this.diaChiIpYeuCau = diaChiIpYeuCau;
    }

    public boolean isDaSuDung() {
        return daSuDungLuc != null;
    }

    public boolean isHetHan(Timestamp currentDbTime) {
        if (hetHanLuc == null) {
            return true;
        }
        if (currentDbTime != null) {
            return !hetHanLuc.after(currentDbTime);
        }
        return !hetHanLuc.after(new Timestamp(System.currentTimeMillis()));
    }

    public boolean isHopLe(Timestamp currentDbTime) {
        return !isDaSuDung() && !isHetHan(currentDbTime);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNguoiDungId() {
        return nguoiDungId;
    }

    public void setNguoiDungId(Long nguoiDungId) {
        this.nguoiDungId = nguoiDungId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Timestamp getTaoLuc() {
        return taoLuc;
    }

    public void setTaoLuc(Timestamp taoLuc) {
        this.taoLuc = taoLuc;
    }

    public Timestamp getHetHanLuc() {
        return hetHanLuc;
    }

    public void setHetHanLuc(Timestamp hetHanLuc) {
        this.hetHanLuc = hetHanLuc;
    }

    public Timestamp getDaSuDungLuc() {
        return daSuDungLuc;
    }

    public void setDaSuDungLuc(Timestamp daSuDungLuc) {
        this.daSuDungLuc = daSuDungLuc;
    }

    public String getDiaChiIpYeuCau() {
        return diaChiIpYeuCau;
    }

    public void setDiaChiIpYeuCau(String diaChiIpYeuCau) {
        this.diaChiIpYeuCau = diaChiIpYeuCau;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserTrangThai() {
        return userTrangThai;
    }

    public void setUserTrangThai(String userTrangThai) {
        this.userTrangThai = userTrangThai;
    }
}
