package vn.nhom10.crm.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Model đại diện cho danh mục lý do thắng và lý do thua của cơ hội bán hàng (Story S2-10).
 * Tương ứng bảng 'ly_do_thang_thua' trong database.
 * Phục vụ phân tích nguyên nhân thành công/thất bại và bắt buộc nhập khi đóng cơ hội (Sprint 5).
 */
public class LyDoThangThua implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String LOAI_THANG = "THANG";
    public static final String LOAI_THUA = "THUA";

    private Long id;
    private String maLyDo;
    private String tenLyDo;
    private String loai; // "THANG" hoặc "THUA"
    private int thuTuHienThi;
    private boolean hoatDong;
    private Timestamp createdAt;

    // Trường bổ sung hiển thị số lượng cơ hội đang tham chiếu
    private int soCoHoiThamChieu;

    public LyDoThangThua() {
        this.hoatDong = true;
        this.thuTuHienThi = 0;
    }

    public LyDoThangThua(String maLyDo, String tenLyDo, String loai, int thuTuHienThi, boolean hoatDong) {
        this.maLyDo = maLyDo;
        this.tenLyDo = tenLyDo;
        this.loai = loai;
        this.thuTuHienThi = thuTuHienThi;
        this.hoatDong = hoatDong;
    }

    public LyDoThangThua(Long id, String maLyDo, String tenLyDo, String loai, int thuTuHienThi, boolean hoatDong, Timestamp createdAt) {
        this.id = id;
        this.maLyDo = maLyDo;
        this.tenLyDo = tenLyDo;
        this.loai = loai;
        this.thuTuHienThi = thuTuHienThi;
        this.hoatDong = hoatDong;
        this.createdAt = createdAt;
    }

    public boolean laLyDoThang() {
        return LOAI_THANG.equalsIgnoreCase(this.loai);
    }

    public boolean laLyDoThua() {
        return LOAI_THUA.equalsIgnoreCase(this.loai);
    }

    public boolean dangHoatDong() {
        return this.hoatDong;
    }

    public String getTenLoaiHienThi() {
        if (laLyDoThang()) {
            return "Lý do thắng";
        } else if (laLyDoThua()) {
            return "Lý do thua";
        }
        return this.loai;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaLyDo() {
        return maLyDo;
    }

    public void setMaLyDo(String maLyDo) {
        this.maLyDo = maLyDo;
    }

    public String getTenLyDo() {
        return tenLyDo;
    }

    public void setTenLyDo(String tenLyDo) {
        this.tenLyDo = tenLyDo;
    }

    public String getLoai() {
        return loai;
    }

    public void setLoai(String loai) {
        this.loai = loai;
    }

    public int getThuTuHienThi() {
        return thuTuHienThi;
    }

    public void setThuTuHienThi(int thuTuHienThi) {
        this.thuTuHienThi = thuTuHienThi;
    }

    public boolean isHoatDong() {
        return hoatDong;
    }

    public void setHoatDong(boolean hoatDong) {
        this.hoatDong = hoatDong;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public int getSoCoHoiThamChieu() {
        return soCoHoiThamChieu;
    }

    public void setSoCoHoiThamChieu(int soCoHoiThamChieu) {
        this.soCoHoiThamChieu = soCoHoiThamChieu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LyDoThangThua that = (LyDoThangThua) o;
        return Objects.equals(id, that.id) || Objects.equals(maLyDo, that.maLyDo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, maLyDo);
    }

    @Override
    public String toString() {
        return "LyDoThangThua{" +
                "id=" + id +
                ", maLyDo='" + maLyDo + '\'' +
                ", tenLyDo='" + tenLyDo + '\'' +
                ", loai='" + loai + '\'' +
                ", thuTuHienThi=" + thuTuHienThi +
                ", hoatDong=" + hoatDong +
                '}';
    }
}
