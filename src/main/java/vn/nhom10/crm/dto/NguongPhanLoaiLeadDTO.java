package vn.nhom10.crm.dto;

import java.io.Serializable;

/**
 * DTO đại diện cho ngưỡng phân loại Nóng, Ấm, Lạnh của Lead (Story S4-05 - AC3).
 */
public class NguongPhanLoaiLeadDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int hotMin;   // Ngưỡng điểm bắt đầu phân loại NÓNG (score >= hotMin)
    private int warmMin;  // Ngưỡng điểm bắt đầu phân loại ẤM (warmMin <= score < hotMin)
    // Dưới warmMin tự động là LẠNH (score < warmMin)

    public NguongPhanLoaiLeadDTO() {
        this.hotMin = 70;
        this.warmMin = 40;
    }

    public NguongPhanLoaiLeadDTO(int hotMin, int warmMin) {
        this.hotMin = hotMin;
        this.warmMin = warmMin;
    }

    public int getHotMin() {
        return hotMin;
    }

    public void setHotMin(int hotMin) {
        this.hotMin = hotMin;
    }

    public int getWarmMin() {
        return warmMin;
    }

    public void setWarmMin(int warmMin) {
        this.warmMin = warmMin;
    }

    /**
     * Xác định phân loại lead dựa trên điểm và ngưỡng hiện tại
     */
    public String phanLoai(Integer diem) {
        if (diem == null) {
            return "CHUA_CHAM";
        }
        if (diem >= hotMin) {
            return "NONG";
        }
        if (diem >= warmMin) {
            return "AM";
        }
        return "LANH";
    }

    public String getTenPhanLoai(Integer diem) {
        String code = phanLoai(diem);
        switch (code) {
            case "NONG": return "Nóng";
            case "AM": return "Ấm";
            case "LANH": return "Lạnh";
            default: return "Chưa chấm";
        }
    }
}
