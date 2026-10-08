package vn.nhom10.crm.dto;

import vn.nhom10.crm.model.PhamViDuLieu;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Data Transfer Object đóng gói các tiêu chí tìm kiếm và lọc khách hàng (Story S3-07):
 * - AC1: Lọc theo trạng thái, ngành nghề, quy mô, khu vực, người sở hữu
 * - AC2: Tìm theo tên, mã số thuế, số điện thoại người liên hệ
 * - AC3: Hỗ trợ chuyển đổi JSON hai chiều (toJson / tuJson) để lưu và áp dụng bộ lọc
 */
public class BoLocKhachHangDTO {

    // AC1: Tiêu chí lọc
    private String trangThai;
    private Long nganhNgheId;
    private Long quyMoId;
    private Long khuVucId;
    private Long nguoiSoHuuId;

    // AC2: Tiêu chí tìm kiếm
    private String tuKhoa;        // Từ khóa tìm kiếm tổng hợp
    private String tenCongTy;      // Tìm chính xác hoặc tương đối theo tên
    private String maSoThue;      // Tìm theo mã số thuế
    private String soDienThoai;    // Tìm theo số điện thoại người liên hệ

    // Data Scope & metadata bộ lọc (AC3)
    private PhamViDuLieu phamVi;
    private Long boLocId;
    private String tenBoLoc;
    private boolean macDinh = false;

    public BoLocKhachHangDTO() {
    }

    public boolean coDieuKienLoc() {
        return (trangThai != null && !trangThai.isBlank())
                || (nganhNgheId != null && nganhNgheId > 0)
                || (quyMoId != null && quyMoId > 0)
                || (khuVucId != null && khuVucId > 0)
                || (nguoiSoHuuId != null && nguoiSoHuuId > 0)
                || (tuKhoa != null && !tuKhoa.isBlank())
                || (tenCongTy != null && !tenCongTy.isBlank())
                || (maSoThue != null && !maSoThue.isBlank())
                || (soDienThoai != null && !soDienThoai.isBlank());
    }

    /**
     * Chuyển đổi DTO thành chuỗi JSON hợp lệ để lưu vào cột `tieu_chi_json` của bảng `bo_loc_da_luu`.
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;

        if (trangThai != null && !trangThai.isBlank()) {
            appendJsonString(sb, "trangThai", trangThai.trim(), first);
            first = false;
        }
        if (nganhNgheId != null && nganhNgheId > 0) {
            appendJsonNumber(sb, "nganhNgheId", nganhNgheId, first);
            first = false;
        }
        if (quyMoId != null && quyMoId > 0) {
            appendJsonNumber(sb, "quyMoId", quyMoId, first);
            first = false;
        }
        if (khuVucId != null && khuVucId > 0) {
            appendJsonNumber(sb, "khuVucId", khuVucId, first);
            first = false;
        }
        if (nguoiSoHuuId != null && nguoiSoHuuId > 0) {
            appendJsonNumber(sb, "nguoiSoHuuId", nguoiSoHuuId, first);
            first = false;
        }
        if (tuKhoa != null && !tuKhoa.isBlank()) {
            appendJsonString(sb, "tuKhoa", tuKhoa.trim(), first);
            first = false;
        }
        if (tenCongTy != null && !tenCongTy.isBlank()) {
            appendJsonString(sb, "tenCongTy", tenCongTy.trim(), first);
            first = false;
        }
        if (maSoThue != null && !maSoThue.isBlank()) {
            appendJsonString(sb, "maSoThue", maSoThue.trim(), first);
            first = false;
        }
        if (soDienThoai != null && !soDienThoai.isBlank()) {
            appendJsonString(sb, "soDienThoai", soDienThoai.trim(), first);
            first = false;
        }
        if (phamVi != null) {
            appendJsonString(sb, "phamVi", phamVi.getMa(), first);
            first = false;
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Phục hồi DTO từ chuỗi JSON lưu trong database hoặc gửi từ client.
     */
    public static BoLocKhachHangDTO tuJson(String json) {
        BoLocKhachHangDTO dto = new BoLocKhachHangDTO();
        if (json == null || json.trim().isEmpty() || !json.contains("{")) {
            return dto;
        }

        // Regex chuẩn JSON token: chuỗi có chứa escape (\", \\...), số, boolean hoặc null
        Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(\"(?:\\\\.|[^\"\\\\])*\"|\\d+|true|false|null)");
        Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {
            String key = matcher.group(1);
            String rawVal = matcher.group(2);

            if ("null".equals(rawVal)) {
                continue;
            }

            String val = rawVal;
            if (val.startsWith("\"") && val.endsWith("\"") && val.length() >= 2) {
                val = val.substring(1, val.length() - 1);
                val = unescapeJson(val);
            }

            switch (key) {
                case "trangThai":
                    dto.setTrangThai(val);
                    break;
                case "nganhNgheId":
                    dto.setNganhNgheId(parseLongSafe(val));
                    break;
                case "quyMoId":
                    dto.setQuyMoId(parseLongSafe(val));
                    break;
                case "khuVucId":
                    dto.setKhuVucId(parseLongSafe(val));
                    break;
                case "nguoiSoHuuId":
                    dto.setNguoiSoHuuId(parseLongSafe(val));
                    break;
                case "tuKhoa":
                    dto.setTuKhoa(val);
                    break;
                case "tenCongTy":
                    dto.setTenCongTy(val);
                    break;
                case "maSoThue":
                    dto.setMaSoThue(val);
                    break;
                case "soDienThoai":
                    dto.setSoDienThoai(val);
                    break;
                case "phamVi":
                    dto.setPhamVi(PhamViDuLieu.tuMa(val));
                    break;
                default:
                    break;
            }
        }

        return dto;
    }

    private static void appendJsonString(StringBuilder sb, String key, String val, boolean first) {
        if (!first) sb.append(",");
        sb.append("\"").append(key).append("\":\"").append(escapeJson(val)).append("\"");
    }

    private static void appendJsonNumber(StringBuilder sb, String key, Number val, boolean first) {
        if (!first) sb.append(",");
        sb.append("\"").append(key).append("\":").append(val);
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String unescapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\b", "\b")
                .replace("\\f", "\f")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

    private static Long parseLongSafe(String s) {
        try {
            return Long.parseLong(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    // Getters and Setters

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Long getNganhNgheId() {
        return nganhNgheId;
    }

    public void setNganhNgheId(Long nganhNgheId) {
        this.nganhNgheId = nganhNgheId;
    }

    public Long getQuyMoId() {
        return quyMoId;
    }

    public void setQuyMoId(Long quyMoId) {
        this.quyMoId = quyMoId;
    }

    public Long getKhuVucId() {
        return khuVucId;
    }

    public void setKhuVucId(Long khuVucId) {
        this.khuVucId = khuVucId;
    }

    public Long getNguoiSoHuuId() {
        return nguoiSoHuuId;
    }

    public void setNguoiSoHuuId(Long nguoiSoHuuId) {
        this.nguoiSoHuuId = nguoiSoHuuId;
    }

    public String getTuKhoa() {
        return tuKhoa;
    }

    public void setTuKhoa(String tuKhoa) {
        this.tuKhoa = tuKhoa;
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

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public PhamViDuLieu getPhamVi() {
        return phamVi;
    }

    public void setPhamVi(PhamViDuLieu phamVi) {
        this.phamVi = phamVi;
    }

    public Long getBoLocId() {
        return boLocId;
    }

    public void setBoLocId(Long boLocId) {
        this.boLocId = boLocId;
    }

    public String getTenBoLoc() {
        return tenBoLoc;
    }

    public void setTenBoLoc(String tenBoLoc) {
        this.tenBoLoc = tenBoLoc;
    }

    public boolean isMacDinh() {
        return macDinh;
    }

    public void setMacDinh(boolean macDinh) {
        this.macDinh = macDinh;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoLocKhachHangDTO that = (BoLocKhachHangDTO) o;
        return Objects.equals(trangThai, that.trangThai) &&
                Objects.equals(nganhNgheId, that.nganhNgheId) &&
                Objects.equals(quyMoId, that.quyMoId) &&
                Objects.equals(khuVucId, that.khuVucId) &&
                Objects.equals(nguoiSoHuuId, that.nguoiSoHuuId) &&
                Objects.equals(tuKhoa, that.tuKhoa) &&
                Objects.equals(tenCongTy, that.tenCongTy) &&
                Objects.equals(maSoThue, that.maSoThue) &&
                Objects.equals(soDienThoai, that.soDienThoai);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trangThai, nganhNgheId, quyMoId, khuVucId, nguoiSoHuuId,
                tuKhoa, tenCongTy, maSoThue, soDienThoai);
    }

    @Override
    public String toString() {
        return "BoLocKhachHangDTO{" +
                "trangThai='" + trangThai + '\'' +
                ", nganhNgheId=" + nganhNgheId +
                ", quyMoId=" + quyMoId +
                ", khuVucId=" + khuVucId +
                ", nguoiSoHuuId=" + nguoiSoHuuId +
                ", tuKhoa='" + tuKhoa + '\'' +
                ", tenCongTy='" + tenCongTy + '\'' +
                ", maSoThue='" + maSoThue + '\'' +
                ", soDienThoai='" + soDienThoai + '\'' +
                '}';
    }
}
