package vn.nhom10.crm.dto;

import vn.nhom10.crm.model.PhamViDuLieu;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Data Transfer Object đóng gói các tiêu chí lọc danh sách Lead (Story S4-09):
 * - AC1: Lọc theo trạng thái, nguồn lead, phân loại nóng/ấm/lạnh, người phụ trách, khoảng thời gian
 * - AC2: Hỗ trợ hiển thị và ưu tiên sắp xếp Lead quá SLA
 * - AC3: Hỗ trợ tuần tự hóa/giải tuần tự JSON (toJson / tuJson) để lưu và tái sử dụng bộ lọc
 */
public class BoLocLeadDTO {

    // S4-09 Search Keyword: Tìm kiếm nhanh theo họ tên, tên công ty, mã Lead, email, số điện thoại
    private String tuKhoa;

    // AC1: 5 nhóm tiêu chí lọc (AND)
    private String trangThai;           // Mã trạng thái (MOI, DANG_CHAM_SOC, DA_CHUYEN_DOI, TU_CHOI, DA_GOP)
    private Long nguonLeadId;           // ID nguồn lead
    private String phanLoai;            // Phân loại: NONG, AM, LANH
    private Long nguoiPhuTrachId;       // ID người phụ trách
    private String tuNgay;              // YYYY-MM-DD lọc theo created_at
    private String denNgay;             // YYYY-MM-DD lọc theo created_at

    // AC3: Metadata bộ lọc đã lưu
    private Long savedFilterId;         // ID của bộ lọc đã lưu (nếu đang áp dụng)
    private String tenBoLoc;            // Tên do người dùng đặt (1-80 ký tự)
    private boolean macDinh = false;    // Đặt làm bộ lọc mặc định

    // Server-side enforcement: Data Scope
    private PhamViDuLieu phamVi;

    public BoLocLeadDTO() {
    }

    public boolean coDieuKienLoc() {
        return (tuKhoa != null && !tuKhoa.isBlank())
                || (trangThai != null && !trangThai.isBlank())
                || (nguonLeadId != null && nguonLeadId > 0)
                || (phanLoai != null && !phanLoai.isBlank())
                || (nguoiPhuTrachId != null && nguoiPhuTrachId > 0)
                || (tuNgay != null && !tuNgay.isBlank())
                || (denNgay != null && !denNgay.isBlank())
                || (savedFilterId != null && savedFilterId > 0);
    }

    public int soDieuKienLoc() {
        int count = 0;
        if (tuKhoa != null && !tuKhoa.isBlank()) count++;
        if (trangThai != null && !trangThai.isBlank()) count++;
        if (nguonLeadId != null && nguonLeadId > 0) count++;
        if (phanLoai != null && !phanLoai.isBlank()) count++;
        if (nguoiPhuTrachId != null && nguoiPhuTrachId > 0) count++;
        if ((tuNgay != null && !tuNgay.isBlank()) || (denNgay != null && !denNgay.isBlank())) count++;
        return count;
    }

    /**
     * Chuyển đổi DTO thành chuỗi JSON hợp lệ để lưu vào cột `tieu_chi_json` của bảng `bo_loc_da_luu`.
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;

        if (tuKhoa != null && !tuKhoa.isBlank()) {
            appendJsonString(sb, "tuKhoa", tuKhoa.trim(), first);
            first = false;
        }
        if (trangThai != null && !trangThai.isBlank()) {
            appendJsonString(sb, "trangThai", trangThai.trim(), first);
            first = false;
        }
        if (nguonLeadId != null && nguonLeadId > 0) {
            appendJsonNumber(sb, "nguonLeadId", nguonLeadId, first);
            first = false;
        }
        if (phanLoai != null && !phanLoai.isBlank()) {
            appendJsonString(sb, "phanLoai", phanLoai.trim(), first);
            first = false;
        }
        if (nguoiPhuTrachId != null && nguoiPhuTrachId > 0) {
            appendJsonNumber(sb, "nguoiPhuTrachId", nguoiPhuTrachId, first);
            first = false;
        }
        if (tuNgay != null && !tuNgay.isBlank()) {
            appendJsonString(sb, "tuNgay", tuNgay.trim(), first);
            first = false;
        }
        if (denNgay != null && !denNgay.isBlank()) {
            appendJsonString(sb, "denNgay", denNgay.trim(), first);
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
     * Chuyển đổi chuỗi JSON từ cột `tieu_chi_json` ngược lại thành đối tượng BoLocLeadDTO.
     */
    public static BoLocLeadDTO tuJson(String json) {
        BoLocLeadDTO dto = new BoLocLeadDTO();
        if (json == null || json.trim().isEmpty() || "{}".equals(json.trim())) {
            return dto;
        }

        dto.setTuKhoa(docJsonString(json, "tuKhoa"));
        dto.setTrangThai(docJsonString(json, "trangThai"));
        dto.setNguonLeadId(docJsonLong(json, "nguonLeadId"));
        dto.setPhanLoai(docJsonString(json, "phanLoai"));
        dto.setNguoiPhuTrachId(docJsonLong(json, "nguoiPhuTrachId"));
        dto.setTuNgay(docJsonString(json, "tuNgay"));
        dto.setDenNgay(docJsonString(json, "denNgay"));

        String pvStr = docJsonString(json, "phamVi");
        if (pvStr != null) {
            dto.setPhamVi(PhamViDuLieu.tuMa(pvStr));
        }

        return dto;
    }

    private void appendJsonString(StringBuilder sb, String key, String value, boolean first) {
        if (!first) {
            sb.append(",");
        }
        sb.append("\"").append(key).append("\":\"").append(escapeJson(value)).append("\"");
    }

    private void appendJsonNumber(StringBuilder sb, String key, Number value, boolean first) {
        if (!first) {
            sb.append(",");
        }
        sb.append("\"").append(key).append("\":").append(value);
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

    private static String docJsonString(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return unescapeJson(matcher.group(1));
        }
        return null;
    }

    private static Long docJsonLong(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private static String unescapeJson(String raw) {
        if (raw == null) return null;
        return raw.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

    // Getters and Setters
    public String getTuKhoa() {
        return tuKhoa;
    }

    public void setTuKhoa(String tuKhoa) {
        this.tuKhoa = tuKhoa;
    }

    public int soDieuKienNangCao() {
        int count = 0;
        if (trangThai != null && !trangThai.isBlank()) count++;
        if (nguonLeadId != null && nguonLeadId > 0) count++;
        if (phanLoai != null && !phanLoai.isBlank()) count++;
        if (nguoiPhuTrachId != null && nguoiPhuTrachId > 0) count++;
        if ((tuNgay != null && !tuNgay.isBlank()) || (denNgay != null && !denNgay.isBlank())) count++;
        return count;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Long getNguonLeadId() {
        return nguonLeadId;
    }

    public void setNguonLeadId(Long nguonLeadId) {
        this.nguonLeadId = nguonLeadId;
    }

    public String getPhanLoai() {
        return phanLoai;
    }

    public void setPhanLoai(String phanLoai) {
        this.phanLoai = phanLoai;
    }

    public Long getNguoiPhuTrachId() {
        return nguoiPhuTrachId;
    }

    public void setNguoiPhuTrachId(Long nguoiPhuTrachId) {
        this.nguoiPhuTrachId = nguoiPhuTrachId;
    }

    public String getTuNgay() {
        return tuNgay;
    }

    public void setTuNgay(String tuNgay) {
        this.tuNgay = tuNgay;
    }

    public String getDenNgay() {
        return denNgay;
    }

    public void setDenNgay(String denNgay) {
        this.denNgay = denNgay;
    }

    public Long getSavedFilterId() {
        return savedFilterId;
    }

    public void setSavedFilterId(Long savedFilterId) {
        this.savedFilterId = savedFilterId;
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

    public PhamViDuLieu getPhamVi() {
        return phamVi;
    }

    public void setPhamVi(PhamViDuLieu phamVi) {
        this.phamVi = phamVi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoLocLeadDTO that = (BoLocLeadDTO) o;
        return Objects.equals(tuKhoa, that.tuKhoa) &&
                Objects.equals(trangThai, that.trangThai) &&
                Objects.equals(nguonLeadId, that.nguonLeadId) &&
                Objects.equals(phanLoai, that.phanLoai) &&
                Objects.equals(nguoiPhuTrachId, that.nguoiPhuTrachId) &&
                Objects.equals(tuNgay, that.tuNgay) &&
                Objects.equals(denNgay, that.denNgay);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tuKhoa, trangThai, nguonLeadId, phanLoai, nguoiPhuTrachId, tuNgay, denNgay);
    }

    @Override
    public String toString() {
        return "BoLocLeadDTO{" +
                "tuKhoa='" + tuKhoa + '\'' +
                ", trangThai='" + trangThai + '\'' +
                ", nguonLeadId=" + nguonLeadId +
                ", phanLoai='" + phanLoai + '\'' +
                ", nguoiPhuTrachId=" + nguoiPhuTrachId +
                ", tuNgay='" + tuNgay + '\'' +
                ", denNgay='" + denNgay + '\'' +
                ", savedFilterId=" + savedFilterId +
                ", tenBoLoc='" + tenBoLoc + '\'' +
                '}';
    }
}
