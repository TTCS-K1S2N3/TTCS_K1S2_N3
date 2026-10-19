package vn.nhom10.crm.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Model ánh xạ bảng `thong_bao` (Story S3-08).
 * Lưu thông báo cảnh báo rủi ro rời bỏ gửi cho nhân viên kinh doanh phụ trách khách hàng.
 */
public class ThongBao implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long nguoiDungId;
    private Long loaiThongBaoId; // 10: KHACH_HANG_RUI_RO
    private String tieuDe;
    private String noiDung;
    private String loaiDoiTuong; // KHACH_HANG
    private Long doiTuongId;    // khach_hang_id
    private String duongDanMo;  // /chi-tiet-ban-ghi?id=...
    private boolean daDoc;
    private LocalDateTime docLuc;
    private LocalDateTime createdAt;

    public ThongBao() {
        this.daDoc = false;
        this.createdAt = LocalDateTime.now();
    }

    public ThongBao(Long nguoiDungId, Long loaiThongBaoId, String tieuDe, String noiDung,
                    String loaiDoiTuong, Long doiTuongId, String duongDanMo) {
        this();
        this.nguoiDungId = nguoiDungId;
        this.loaiThongBaoId = loaiThongBaoId;
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.loaiDoiTuong = loaiDoiTuong;
        this.doiTuongId = doiTuongId;
        this.duongDanMo = duongDanMo;
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

    public Long getLoaiThongBaoId() {
        return loaiThongBaoId;
    }

    public void setLoaiThongBaoId(Long loaiThongBaoId) {
        this.loaiThongBaoId = loaiThongBaoId;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getLoaiDoiTuong() {
        return loaiDoiTuong;
    }

    public void setLoaiDoiTuong(String loaiDoiTuong) {
        this.loaiDoiTuong = loaiDoiTuong;
    }

    public Long getDoiTuongId() {
        return doiTuongId;
    }

    public void setDoiTuongId(Long doiTuongId) {
        this.doiTuongId = doiTuongId;
    }

    public String getDuongDanMo() {
        return duongDanMo;
    }

    public void setDuongDanMo(String duongDanMo) {
        this.duongDanMo = duongDanMo;
    }

    public boolean isDaDoc() {
        return daDoc;
    }

    public void setDaDoc(boolean daDoc) {
        this.daDoc = daDoc;
    }

    public LocalDateTime getDocLuc() {
        return docLuc;
    }

    public void setDocLuc(LocalDateTime docLuc) {
        this.docLuc = docLuc;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
