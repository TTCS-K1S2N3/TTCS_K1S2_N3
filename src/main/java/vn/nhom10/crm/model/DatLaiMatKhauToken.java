package vn.nhom10.crm.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Model biểu diễn token đặt lại mật khẩu của người dùng.
 * Phục vụ AC:
 * - Hiệu lực 30 phút.
 * - Chỉ sử dụng một lần.
 */
public class DatLaiMatKhauToken implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long nguoiDungId;
    private String token;
    private LocalDateTime thoiGianTao;
    private LocalDateTime thoiGianHetHan;
    private boolean daSuDung;
    private LocalDateTime thoiGianSuDung;

    public DatLaiMatKhauToken() {
    }

    public DatLaiMatKhauToken(Long id, Long nguoiDungId, String token, LocalDateTime thoiGianTao, 
                              LocalDateTime thoiGianHetHan, boolean daSuDung, LocalDateTime thoiGianSuDung) {
        this.id = id;
        this.nguoiDungId = nguoiDungId;
        this.token = token;
        this.thoiGianTao = thoiGianTao;
        this.thoiGianHetHan = thoiGianHetHan;
        this.daSuDung = daSuDung;
        this.thoiGianSuDung = thoiGianSuDung;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(LocalDateTime thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }

    public LocalDateTime getThoiGianHetHan() {
        return thoiGianHetHan;
    }

    public void setThoiGianHetHan(LocalDateTime thoiGianHetHan) {
        this.thoiGianHetHan = thoiGianHetHan;
    }

    public boolean isDaSuDung() {
        return daSuDung;
    }

    public void setDaSuDung(boolean daSuDung) {
        this.daSuDung = daSuDung;
    }

    public LocalDateTime getThoiGianSuDung() {
        return thoiGianSuDung;
    }

    public void setThoiGianSuDung(LocalDateTime thoiGianSuDung) {
        this.thoiGianSuDung = thoiGianSuDung;
    }

    /**
     * Kiểm tra token đã hết hạn chưa so với thời điểm hiện tại.
     *
     * @return true nếu đã quá thời gian hết hạn
     */
    public boolean isHetHan() {
        if (this.thoiGianHetHan == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(this.thoiGianHetHan);
    }

    /**
     * Kiểm tra token có còn hợp lệ để đặt lại mật khẩu không (chưa sử dụng và chưa hết hạn).
     *
     * @return true nếu còn hợp lệ
     */
    public boolean isHopLe() {
        return !this.daSuDung && !isHetHan();
    }
}
