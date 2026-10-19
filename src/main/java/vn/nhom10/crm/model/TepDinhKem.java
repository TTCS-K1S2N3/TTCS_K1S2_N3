package vn.nhom10.crm.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

/**
 * Model tệp đính kèm và tài liệu (bảng `tep_dinh_kem`).
 * Phục vụ danh sách tệp đính kèm trên trang 360 (Story S3-03).
 */
public class TepDinhKem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long khachHangId;
    private Long coHoiId;
    private Long hoatDongId;
    private Long baoGiaId;
    private Long hopDongId;
    private String loaiTep; // HOP_DONG, BAO_GIA, HO_SO_NANG_LUC, BIEN_BAN, TAI_LIEU
    private String tenFileGoc;
    private String tenFileLuu;
    private String duongDan;
    private String mimeType;
    private Long kichThuocByte = 0L;
    private Long nguoiTaiLenId;
    private Timestamp createdAt;

    // Joined / display fields
    private String tenNguoiTaiLen;

    public TepDinhKem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getKhachHangId() {
        return khachHangId;
    }

    public void setKhachHangId(Long khachHangId) {
        this.khachHangId = khachHangId;
    }

    public Long getCoHoiId() {
        return coHoiId;
    }

    public void setCoHoiId(Long coHoiId) {
        this.coHoiId = coHoiId;
    }

    public Long getHoatDongId() {
        return hoatDongId;
    }

    public void setHoatDongId(Long hoatDongId) {
        this.hoatDongId = hoatDongId;
    }

    public Long getBaoGiaId() {
        return baoGiaId;
    }

    public void setBaoGiaId(Long baoGiaId) {
        this.baoGiaId = baoGiaId;
    }

    public Long getHopDongId() {
        return hopDongId;
    }

    public void setHopDongId(Long hopDongId) {
        this.hopDongId = hopDongId;
    }

    public String getLoaiTep() {
        return loaiTep;
    }

    public void setLoaiTep(String loaiTep) {
        this.loaiTep = loaiTep;
    }

    public String getTenFileGoc() {
        return tenFileGoc;
    }

    public void setTenFileGoc(String tenFileGoc) {
        this.tenFileGoc = tenFileGoc;
    }

    public String getTenFileLuu() {
        return tenFileLuu;
    }

    public void setTenFileLuu(String tenFileLuu) {
        this.tenFileLuu = tenFileLuu;
    }

    public String getDuongDan() {
        return duongDan;
    }

    public void setDuongDan(String duongDan) {
        this.duongDan = duongDan;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getKichThuocByte() {
        return kichThuocByte != null ? kichThuocByte : 0L;
    }

    public void setKichThuocByte(Long kichThuocByte) {
        this.kichThuocByte = kichThuocByte != null ? kichThuocByte : 0L;
    }

    public Long getNguoiTaiLenId() {
        return nguoiTaiLenId;
    }

    public void setNguoiTaiLenId(Long nguoiTaiLenId) {
        this.nguoiTaiLenId = nguoiTaiLenId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getTenNguoiTaiLen() {
        return tenNguoiTaiLen != null ? tenNguoiTaiLen : "Người dùng";
    }

    public void setTenNguoiTaiLen(String tenNguoiTaiLen) {
        this.tenNguoiTaiLen = tenNguoiTaiLen;
    }

    public String getDungLuongHienThi() {
        if (kichThuocByte == null || kichThuocByte <= 0) {
            return "0 KB";
        }
        if (kichThuocByte < 1024 * 1024) {
            long kb = kichThuocByte / 1024;
            return kb + " KB";
        }
        double mb = kichThuocByte / (1024.0 * 1024.0);
        return String.format(java.util.Locale.US, "%.1f MB", mb);
    }

    public String getIconClass() {
        String name = tenFileGoc != null ? tenFileGoc.toLowerCase() : "";
        if (name.endsWith(".pdf")) return "pdf";
        if (name.endsWith(".xls") || name.endsWith(".xlsx")) return "excel";
        if (name.endsWith(".doc") || name.endsWith(".docx")) return "word";
        if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image";
        return "other";
    }

    public String getIconName() {
        String name = tenFileGoc != null ? tenFileGoc.toLowerCase() : "";
        if (name.endsWith(".pdf")) return "picture_as_pdf";
        if (name.endsWith(".xls") || name.endsWith(".xlsx")) return "table_view";
        if (name.endsWith(".doc") || name.endsWith(".docx")) return "description";
        if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image";
        return "attachment";
    }

    public String getTenLoaiHienThi() {
        if ("HOP_DONG".equalsIgnoreCase(loaiTep)) return "Hợp đồng đã ký";
        if ("BAO_GIA".equalsIgnoreCase(loaiTep)) return "Báo giá";
        if ("HO_SO_NANG_LUC".equalsIgnoreCase(loaiTep)) return "Hồ sơ năng lực";
        if ("BIEN_BAN".equalsIgnoreCase(loaiTep)) return "Biên bản khảo sát";
        return "Tài liệu dự án";
    }

    public String getNgayTaiLenDinhDang() {
        if (createdAt != null) {
            return createdAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        return "-";
    }
}
