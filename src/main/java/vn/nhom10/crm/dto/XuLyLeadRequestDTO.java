package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO nhận dữ liệu xử lý tiếp nhận hoặc từ chối Lead (Story S4-07).
 * Hỗ trợ validate server-side đầy đủ cả hai hành động NHAN và TU_CHOI.
 */
public class XuLyLeadRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String HANH_DONG_NHAN = "NHAN";
    public static final String HANH_DONG_TU_CHOI = "TU_CHOI";

    private Long leadId;
    private String hanhDong;
    private String maLyDoTuChoi;
    private String lyDoTuChoi;

    public XuLyLeadRequestDTO() {
    }

    public XuLyLeadRequestDTO(Long leadId, String hanhDong) {
        this.leadId = leadId;
        this.hanhDong = hanhDong;
    }

    public XuLyLeadRequestDTO(Long leadId, String hanhDong, String maLyDoTuChoi, String lyDoTuChoi) {
        this.leadId = leadId;
        this.hanhDong = hanhDong;
        this.maLyDoTuChoi = maLyDoTuChoi;
        this.lyDoTuChoi = lyDoTuChoi;
    }

    /**
     * Kiểm tra tính hợp lệ của request xử lý Lead.
     * @return Map chứa lỗi các trường, rỗng nếu hợp lệ.
     */
    public Map<String, String> kiemTraHopLe() {
        Map<String, String> loi = new HashMap<>();

        if (leadId == null || leadId <= 0) {
            loi.put("leadId", "Mã định danh Lead không hợp lệ");
        }

        if (hanhDong == null || hanhDong.isBlank()) {
            loi.put("hanhDong", "Hành động xử lý không được để trống");
        } else {
            String hd = hanhDong.trim().toUpperCase();
            if (!HANH_DONG_NHAN.equals(hd) && !HANH_DONG_TU_CHOI.equals(hd)) {
                loi.put("hanhDong", "Hành động không hợp lệ (chỉ chấp nhận NHAN hoặc TU_CHOI)");
            } else if (HANH_DONG_TU_CHOI.equals(hd)) {
                // AC2 + Q4: Bắt buộc chọn mã danh mục hợp lệ VÀ giải thích tự do 1-500 ký tự
                if (maLyDoTuChoi == null || maLyDoTuChoi.isBlank()) {
                    loi.put("maLyDoTuChoi", "Vui lòng chọn lý do từ chối từ danh mục");
                } else if (!DanhMucLyDoTuChoiDTO.laMaHopLe(maLyDoTuChoi)) {
                    loi.put("maLyDoTuChoi", "Lý do từ chối đã chọn không nằm trong danh mục hợp lệ");
                }

                if (lyDoTuChoi == null || lyDoTuChoi.trim().isEmpty()) {
                    loi.put("lyDoTuChoi", "Vui lòng nhập văn bản giải thích chi tiết lý do từ chối");
                } else {
                    String cleanLyDo = lyDoTuChoi.trim();
                    if (cleanLyDo.codePointCount(0, cleanLyDo.length()) > 500) {
                        loi.put("lyDoTuChoi", "Nội dung giải thích không được vượt quá 500 ký tự");
                    }
                }
            }
        }

        return loi;
    }

    public String layLyDoTongHop() {
        if (!HANH_DONG_TU_CHOI.equalsIgnoreCase(hanhDong)) {
            return null;
        }
        String tenMa = DanhMucLyDoTuChoiDTO.layTenTheoMa(maLyDoTuChoi);
        String chiTiet = (lyDoTuChoi != null) ? lyDoTuChoi.trim() : "";
        if (tenMa.isBlank()) {
            return chiTiet;
        }
        return "[" + tenMa + "] " + chiTiet;
    }

    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

    public String getHanhDong() {
        return hanhDong;
    }

    public void setHanhDong(String hanhDong) {
        this.hanhDong = hanhDong;
    }

    public String getMaLyDoTuChoi() {
        return maLyDoTuChoi;
    }

    public void setMaLyDoTuChoi(String maLyDoTuChoi) {
        this.maLyDoTuChoi = maLyDoTuChoi;
    }

    public String getLyDoTuChoi() {
        return lyDoTuChoi;
    }

    public void setLyDoTuChoi(String lyDoTuChoi) {
        this.lyDoTuChoi = lyDoTuChoi;
    }
}
