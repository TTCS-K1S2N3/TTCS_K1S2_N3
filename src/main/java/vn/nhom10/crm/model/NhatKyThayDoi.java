package vn.nhom10.crm.model;

import vn.nhom10.crm.dto.NhatKyThayDoiDTO;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Model đại diện thực thể bản ghi nhật ký thay đổi dữ liệu nhạy cảm trong CSDL (bảng nhat_ky_he_thong).
 * Đáp ứng Story S2-04: Giám sát toàn diện thay đổi chiết khấu, chỉ tiêu, quyền sở hữu dữ liệu và vai trò người dùng.
 */
public class NhatKyThayDoi implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String maTruyVet;
    private Long nguoiThucHienId;
    private String tenNguoiThucHien;
    private String emailNguoiThucHien;
    private String vaiTroNguoiThucHien;
    private LocalDateTime thoiDiem;
    private LoaiDoiTuongNhayCam loaiDoiTuong;
    private Long doiTuongId;
    private String maDoiTuong;
    private String tenDoiTuong;
    private String truongThayDoi;
    private String giaTriTruoc;
    private String giaTriSau;
    private String giaTriTruocJson;
    private String giaTriSauJson;
    private HanhDongThayDoi hanhDong;
    private String lyDoThayDoi;
    private String diaChiIp;
    private String thietBi;
    private LocalDateTime createdAt;

    public NhatKyThayDoi() {
    }

    public NhatKyThayDoi(Long id, String maTruyVet, Long nguoiThucHienId, String tenNguoiThucHien,
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
        if (this.maTruyVet == null && id != null) {
            this.maTruyVet = "LOG-" + String.format("%06d", id);
        }
    }

    public String getMaTruyVet() {
        if (maTruyVet == null || maTruyVet.isBlank()) {
            if (id != null) {
                return "LOG-" + String.format("%06d", id);
            }
            return "LOG-NEW";
        }
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
        return tenNguoiThucHien;
    }

    public void setTenNguoiThucHien(String tenNguoiThucHien) {
        this.tenNguoiThucHien = tenNguoiThucHien;
    }

    public String getEmailNguoiThucHien() {
        return emailNguoiThucHien;
    }

    public void setEmailNguoiThucHien(String emailNguoiThucHien) {
        this.emailNguoiThucHien = emailNguoiThucHien;
    }

    public String getVaiTroNguoiThucHien() {
        return vaiTroNguoiThucHien;
    }

    public void setVaiTroNguoiThucHien(String vaiTroNguoiThucHien) {
        this.vaiTroNguoiThucHien = vaiTroNguoiThucHien;
    }

    public LocalDateTime getThoiDiem() {
        return thoiDiem != null ? thoiDiem : createdAt;
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

    public Long getDoiTuongId() {
        return doiTuongId;
    }

    public void setDoiTuongId(Long doiTuongId) {
        this.doiTuongId = doiTuongId;
    }

    public String getMaDoiTuong() {
        if (maDoiTuong != null && !maDoiTuong.isBlank()) {
            return maDoiTuong;
        }
        if (doiTuongId != null && doiTuongId > 0) {
            if (loaiDoiTuong == LoaiDoiTuongNhayCam.VAI_TRO_NGUOI_DUNG) {
                return "ND-" + doiTuongId;
            }
            return "DT-" + doiTuongId;
        }
        return "-";
    }

    public void setMaDoiTuong(String maDoiTuong) {
        this.maDoiTuong = maDoiTuong;
    }

    public String getTenDoiTuong() {
        if (tenDoiTuong != null && !tenDoiTuong.isBlank()) {
            return tenDoiTuong;
        }
        if (doiTuongId != null && doiTuongId > 0) {
            if (loaiDoiTuong == LoaiDoiTuongNhayCam.VAI_TRO_NGUOI_DUNG) {
                return "Người dùng #" + doiTuongId;
            }
            return "Đối tượng #" + doiTuongId;
        }
        return "-";
    }

    public void setTenDoiTuong(String tenDoiTuong) {
        this.tenDoiTuong = tenDoiTuong;
    }

    public String getTruongThayDoi() {
        return truongThayDoi;
    }

    public static final Pattern SENSITIVE_KEY_PATTERN = Pattern.compile(
            "(?i).*(password|mat_khau|matkhau|token|secret|smtp_password|api_key|authorization|session_id|sessiontoken).*"
    );

    public static String cheGiaTriNhayCam(String truong, String giaTri) {
        if (giaTri == null || giaTri.isBlank()) {
            return giaTri;
        }
        if (truong != null && SENSITIVE_KEY_PATTERN.matcher(truong).matches()) {
            return "******";
        }
        String result = giaTri;
        result = result.replaceAll("(?i)(\"(?:password|mat_khau|matkhau|token|secret|smtp_password|api_key|authorization|session_id|sessiontoken)\"\\s*:\\s*)\"(?:\\\\.|[^\"\\\\])*\"", "$1\"******\"");
        result = result.replaceAll("(?i)(\"(?:password|mat_khau|matkhau|token|secret|smtp_password|api_key|authorization|session_id|sessiontoken)\"\\s*:\\s*)[^,}\\]\\s]+", "$1\"******\"");
        result = result.replaceAll("(?i)\\b(password|mat_khau|matkhau|token|secret|smtp_password|api_key|authorization|session_id|sessiontoken)\\s*[:=]\\s*[^\\s,;}]+", "$1: ******");
        return result;
    }

    public String getGiaTriTruoc() {
        return giaTriTruoc;
    }

    public void setGiaTriTruoc(String giaTriTruoc) {
        this.giaTriTruoc = cheGiaTriNhayCam(this.truongThayDoi, giaTriTruoc);
    }

    public String getGiaTriSau() {
        return giaTriSau;
    }

    public void setGiaTriSau(String giaTriSau) {
        this.giaTriSau = cheGiaTriNhayCam(this.truongThayDoi, giaTriSau);
    }

    public String getGiaTriTruocJson() {
        if (giaTriTruocJson != null && !giaTriTruocJson.isBlank()) {
            return giaTriTruocJson;
        }
        return dongGoiJson(maDoiTuong, tenDoiTuong, truongThayDoi, giaTriTruoc);
    }

    public void setGiaTriTruocJson(String giaTriTruocJson) {
        this.giaTriTruocJson = cheGiaTriNhayCam(null, giaTriTruocJson);
        giaiMaJson(this.giaTriTruocJson, true);
    }

    public String getGiaTriSauJson() {
        if (giaTriSauJson != null && !giaTriSauJson.isBlank()) {
            return giaTriSauJson;
        }
        return dongGoiJson(maDoiTuong, tenDoiTuong, truongThayDoi, giaTriSau);
    }

    public void setGiaTriSauJson(String giaTriSauJson) {
        this.giaTriSauJson = cheGiaTriNhayCam(null, giaTriSauJson);
        giaiMaJson(this.giaTriSauJson, false);
    }

    public void setTruongThayDoi(String truongThayDoi) {
        this.truongThayDoi = truongThayDoi;
        if (this.giaTriTruoc != null) {
            this.giaTriTruoc = cheGiaTriNhayCam(truongThayDoi, this.giaTriTruoc);
        }
        if (this.giaTriSau != null) {
            this.giaTriSau = cheGiaTriNhayCam(truongThayDoi, this.giaTriSau);
        }
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        if (this.thoiDiem == null) {
            this.thoiDiem = createdAt;
        }
    }

    /**
     * Chuyển đổi Model sang DTO phục vụ hiển thị trên giao diện hoặc API.
     */
    public NhatKyThayDoiDTO toDTO() {
        return new NhatKyThayDoiDTO(
                this.id,
                getMaTruyVet(),
                this.nguoiThucHienId,
                this.tenNguoiThucHien,
                this.emailNguoiThucHien,
                this.vaiTroNguoiThucHien,
                getThoiDiem(),
                this.loaiDoiTuong,
                getMaDoiTuong(),
                this.tenDoiTuong,
                this.truongThayDoi != null ? this.truongThayDoi : "Dữ liệu đối tượng",
                this.giaTriTruoc,
                this.giaTriSau,
                this.hanhDong != null ? this.hanhDong : HanhDongThayDoi.CAP_NHAT,
                this.lyDoThayDoi,
                this.diaChiIp,
                this.thietBi
        );
    }

    // =========================================================================
    // TIỆN ÍCH ĐÓNG GÓI VÀ GIẢI MÃ JSON AN TOÀN CHO BẢNG nhat_ky_he_thong
    // =========================================================================

    public static String dongGoiJson(String maDoiTuong, String tenDoiTuong, String truongThayDoi, String giaTri) {
        String safeGiaTri = cheGiaTriNhayCam(truongThayDoi, giaTri);
        StringBuilder sb = new StringBuilder("{");
        appendJsonField(sb, "maDoiTuong", maDoiTuong);
        sb.append(",");
        appendJsonField(sb, "tenDoiTuong", tenDoiTuong);
        sb.append(",");
        appendJsonField(sb, "truongThayDoi", truongThayDoi);
        sb.append(",");
        appendJsonField(sb, "giaTri", safeGiaTri);
        sb.append("}");
        return sb.toString();
    }

    private static void appendJsonField(StringBuilder sb, String key, String value) {
        sb.append("\"").append(key).append("\":");
        if (value == null) {
            sb.append("null");
        } else {
            sb.append("\"").append(escapeJson(value)).append("\"");
        }
    }

    private static String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private void giaiMaJson(String json, boolean laTruoc) {
        if (json == null || json.isBlank()) {
            return;
        }
        String val = trichXuatJson(json, "giaTri");
        if (val != null && !val.isBlank()) {
            if (laTruoc) {
                this.giaTriTruoc = val;
            } else {
                this.giaTriSau = val;
            }
        } else if ((laTruoc && (this.giaTriTruoc == null || this.giaTriTruoc.isBlank()))
                || (!laTruoc && (this.giaTriSau == null || this.giaTriSau.isBlank()))) {
            // Nếu không có cấu trúc object chuẩn, lưu toàn bộ chuỗi
            if (laTruoc) this.giaTriTruoc = json;
            else this.giaTriSau = json;
        }

        String ma = trichXuatJson(json, "maDoiTuong");
        if (ma != null && !ma.isBlank() && (this.maDoiTuong == null || this.maDoiTuong.isBlank())) {
            this.maDoiTuong = ma;
        }

        String ten = trichXuatJson(json, "tenDoiTuong");
        if (ten != null && !ten.isBlank() && (this.tenDoiTuong == null || this.tenDoiTuong.isBlank())) {
            this.tenDoiTuong = ten;
        }

        String truong = trichXuatJson(json, "truongThayDoi");
        if (truong != null && !truong.isBlank() && (this.truongThayDoi == null || this.truongThayDoi.isBlank())) {
            this.truongThayDoi = truong;
        }
    }

    public static String trichXuatJson(String json, String key) {
        if (json == null || json.isBlank() || key == null) {
            return null;
        }
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return unescapeJson(matcher.group(1));
        }

        Pattern patternRaw = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*([^,}\\]\\s]+)");
        Matcher matcherRaw = patternRaw.matcher(json);
        if (matcherRaw.find()) {
            String rawVal = matcherRaw.group(1).trim();
            if ("null".equalsIgnoreCase(rawVal)) {
                return null;
            }
            return rawVal;
        }
        return null;
    }

    private static String unescapeJson(String text) {
        if (text == null) return null;
        return text.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\b", "\b")
                .replace("\\f", "\f");
    }
}
