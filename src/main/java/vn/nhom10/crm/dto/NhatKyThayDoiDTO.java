package vn.nhom10.crm.dto;

import vn.nhom10.crm.model.HanhDongThayDoi;
import vn.nhom10.crm.model.LoaiDoiTuongNhayCam;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DTO đại diện cho một bản ghi nhật ký thay đổi trên dữ liệu nhạy cảm (Story S2-04).
 * Đảm bảo đầy đủ: người thực hiện, thời điểm, giá trị trước và sau, lý do, loại đối tượng.
 */
public class NhatKyThayDoiDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER_FULL = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter FORMATTER_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATTER_GIO = DateTimeFormatter.ofPattern("HH:mm:ss");

    private Long id;
    private String maTruyVet;
    private Long nguoiThucHienId;
    private String tenNguoiThucHien;
    private String emailNguoiThucHien;
    private String vaiTroNguoiThucHien;
    private LocalDateTime thoiDiem;
    private LoaiDoiTuongNhayCam loaiDoiTuong;
    private String maDoiTuong;
    private String tenDoiTuong;
    private String truongThayDoi;
    private String giaTriTruoc;
    private String giaTriSau;
    private HanhDongThayDoi hanhDong;
    private String lyDoThayDoi;
    private String diaChiIp;
    private String thietBi;

    public NhatKyThayDoiDTO() {
    }

    public NhatKyThayDoiDTO(Long id, String maTruyVet, Long nguoiThucHienId, String tenNguoiThucHien,
                           String emailNguoiThucHien, String vaiTroNguoiThucHien, LocalDateTime thoiDiem,
                           LoaiDoiTuongNhayCam loaiDoiTuong, String maDoiTuong, String tenDoiTuong,
                           String truongThayDoi, String giaTriTruoc, String giaTriSau,
                           HanhDongThayDoi hanhDong, String lyDoThayDoi, String diaChiIp, String thietBi) {
        this.id = id;
        this.maTruyVet = maTruyVet;
        this.nguoiThucHienId = nguoiThucHienId;
        this.tenNguoiThucHien = tenNguoiThucHien;
        this.emailNguoiThucHien = emailNguoiThucHien;
        this.vaiTroNguoiThucHien = vaiTroNguoiThucHien;
        this.thoiDiem = thoiDiem;
        this.loaiDoiTuong = loaiDoiTuong;
        this.maDoiTuong = maDoiTuong;
        this.tenDoiTuong = tenDoiTuong;
        this.truongThayDoi = truongThayDoi;
        this.giaTriTruoc = giaTriTruoc;
        this.giaTriSau = giaTriSau;
        this.hanhDong = hanhDong;
        this.lyDoThayDoi = lyDoThayDoi;
        this.diaChiIp = diaChiIp;
        this.thietBi = thietBi;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaTruyVet() {
        return maTruyVet;
    }

    public void setMaTruyVet(String maTruyVet) {
        this.maTruyVet = maTruyVet;
    }

    public Long getNguoiThucHienId() {
        return nguoiThucHienId;
    }

    public void setNguoiThucHienId(Long nguoiThucHienId) {
        this.nguoiThucHienId = nguoiThucHienId;
    }

    public String getTenNguoiThucHien() {
        if (tenNguoiThucHien != null && !tenNguoiThucHien.isBlank() && !"null".equalsIgnoreCase(tenNguoiThucHien.trim())) {
            return tenNguoiThucHien;
        }
        return (nguoiThucHienId != null && nguoiThucHienId > 0) ? ("Người dùng #" + nguoiThucHienId) : "Hệ thống";
    }

    public void setTenNguoiThucHien(String tenNguoiThucHien) {
        this.tenNguoiThucHien = tenNguoiThucHien;
    }

    public String getEmailNguoiThucHien() {
        if (emailNguoiThucHien != null && !emailNguoiThucHien.isBlank() && !"null".equalsIgnoreCase(emailNguoiThucHien.trim())) {
            return emailNguoiThucHien;
        }
        return "-";
    }

    public void setEmailNguoiThucHien(String emailNguoiThucHien) {
        this.emailNguoiThucHien = emailNguoiThucHien;
    }

    public String getVaiTroNguoiThucHien() {
        if (vaiTroNguoiThucHien != null && !vaiTroNguoiThucHien.isBlank() && !"null".equalsIgnoreCase(vaiTroNguoiThucHien.trim())) {
            return vaiTroNguoiThucHien;
        }
        return "Không xác định";
    }

    public void setVaiTroNguoiThucHien(String vaiTroNguoiThucHien) {
        this.vaiTroNguoiThucHien = vaiTroNguoiThucHien;
    }

    public LocalDateTime getThoiDiem() {
        return thoiDiem;
    }

    public void setThoiDiem(LocalDateTime thoiDiem) {
        this.thoiDiem = thoiDiem;
    }

    public LoaiDoiTuongNhayCam getLoaiDoiTuong() {
        return loaiDoiTuong;
    }

    public void setLoaiDoiTuong(LoaiDoiTuongNhayCam loaiDoiTuong) {
        this.loaiDoiTuong = loaiDoiTuong;
    }

    public String getMaDoiTuong() {
        return maDoiTuong;
    }

    public void setMaDoiTuong(String maDoiTuong) {
        this.maDoiTuong = maDoiTuong;
    }

    public String getTenDoiTuong() {
        return tenDoiTuong;
    }

    public void setTenDoiTuong(String tenDoiTuong) {
        this.tenDoiTuong = tenDoiTuong;
    }

    public String getTruongThayDoi() {
        return truongThayDoi;
    }

    public void setTruongThayDoi(String truongThayDoi) {
        this.truongThayDoi = truongThayDoi;
    }

    public String getGiaTriTruoc() {
        return giaTriTruoc;
    }

    public void setGiaTriTruoc(String giaTriTruoc) {
        this.giaTriTruoc = giaTriTruoc;
    }

    public String getGiaTriSau() {
        return giaTriSau;
    }

    public void setGiaTriSau(String giaTriSau) {
        this.giaTriSau = giaTriSau;
    }

    public HanhDongThayDoi getHanhDong() {
        return hanhDong;
    }

    public void setHanhDong(HanhDongThayDoi hanhDong) {
        this.hanhDong = hanhDong;
    }

    public String getLyDoThayDoi() {
        return lyDoThayDoi;
    }

    public void setLyDoThayDoi(String lyDoThayDoi) {
        this.lyDoThayDoi = lyDoThayDoi;
    }

    public String getDiaChiIp() {
        return diaChiIp;
    }

    public void setDiaChiIp(String diaChiIp) {
        this.diaChiIp = diaChiIp;
    }

    public String getThietBi() {
        return thietBi;
    }

    public void setThietBi(String thietBi) {
        this.thietBi = thietBi;
    }

    // Các tiện ích định dạng hiển thị cho JSP
    public String getThoiDiemDinhDang() {
        return thoiDiem != null ? thoiDiem.format(FORMATTER_FULL) : "";
    }

    public String getNgayDinhDang() {
        return thoiDiem != null ? thoiDiem.format(FORMATTER_NGAY) : "";
    }

    public String getGioDinhDang() {
        return thoiDiem != null ? thoiDiem.format(FORMATTER_GIO) : "";
    }

    public String getChuCaiDau() {
        if (tenNguoiThucHien == null || tenNguoiThucHien.trim().isEmpty()) {
            return "?";
        }
        String[] tu = tenNguoiThucHien.trim().split("\\s+");
        return tu[tu.length - 1].substring(0, 1).toUpperCase();
    }
}
