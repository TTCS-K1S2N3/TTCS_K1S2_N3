package vn.nhom10.crm.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Model đại diện cho nhật ký bàn giao dữ liệu khi khoá tài khoản (Story S1-10).
 * Lưu vết lịch sử chuyển giao toàn bộ khách hàng và cơ hội sang người tiếp nhận.
 */
public class NhatKyBanGiao implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int nguoiBiKhoaId;
    private NguoiDung nguoiBiKhoa;
    private int nguoiTiepNhanId;
    private NguoiDung nguoiTiepNhan;
    private int nguoiThucHienId;
    private NguoiDung nguoiThucHien;
    private int soKhachHangChuyen;
    private int soCoHoiChuyen;
    private String lyDo;
    private Timestamp createdAt;

    public NhatKyBanGiao() {
    }

    public NhatKyBanGiao(int nguoiBiKhoaId, int nguoiTiepNhanId, int nguoiThucHienId,
                         int soKhachHangChuyen, int soCoHoiChuyen, String lyDo) {
        this.nguoiBiKhoaId = nguoiBiKhoaId;
        this.nguoiTiepNhanId = nguoiTiepNhanId;
        this.nguoiThucHienId = nguoiThucHienId;
        this.soKhachHangChuyen = soKhachHangChuyen;
        this.soCoHoiChuyen = soCoHoiChuyen;
        this.lyDo = lyDo;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNguoiBiKhoaId() {
        return nguoiBiKhoaId;
    }

    public void setNguoiBiKhoaId(int nguoiBiKhoaId) {
        this.nguoiBiKhoaId = nguoiBiKhoaId;
    }

    public NguoiDung getNguoiBiKhoa() {
        return nguoiBiKhoa;
    }

    public void setNguoiBiKhoa(NguoiDung nguoiBiKhoa) {
        this.nguoiBiKhoa = nguoiBiKhoa;
        if (nguoiBiKhoa != null) {
            this.nguoiBiKhoaId = nguoiBiKhoa.getId();
        }
    }

    public int getNguoiTiepNhanId() {
        return nguoiTiepNhanId;
    }

    public void setNguoiTiepNhanId(int nguoiTiepNhanId) {
        this.nguoiTiepNhanId = nguoiTiepNhanId;
    }

    public NguoiDung getNguoiTiepNhan() {
        return nguoiTiepNhan;
    }

    public void setNguoiTiepNhan(NguoiDung nguoiTiepNhan) {
        this.nguoiTiepNhan = nguoiTiepNhan;
        if (nguoiTiepNhan != null) {
            this.nguoiTiepNhanId = nguoiTiepNhan.getId();
        }
    }

    public int getNguoiThucHienId() {
        return nguoiThucHienId;
    }

    public void setNguoiThucHienId(int nguoiThucHienId) {
        this.nguoiThucHienId = nguoiThucHienId;
    }

    public NguoiDung getNguoiThucHien() {
        return nguoiThucHien;
    }

    public void setNguoiThucHien(NguoiDung nguoiThucHien) {
        this.nguoiThucHien = nguoiThucHien;
        if (nguoiThucHien != null) {
            this.nguoiThucHienId = nguoiThucHien.getId();
        }
    }

    public int getSoKhachHangChuyen() {
        return soKhachHangChuyen;
    }

    public void setSoKhachHangChuyen(int soKhachHangChuyen) {
        this.soKhachHangChuyen = soKhachHangChuyen;
    }

    public int getSoCoHoiChuyen() {
        return soCoHoiChuyen;
    }

    public void setSoCoHoiChuyen(int soCoHoiChuyen) {
        this.soCoHoiChuyen = soCoHoiChuyen;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getThoiGianDinhDang() {
        if (createdAt == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        return sdf.format(createdAt);
    }
}
