package vn.nhom10.crm.dto;

import vn.nhom10.crm.model.GiaiDoanPipeline;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO chứa kết quả xử lý nghiệp vụ cấu hình pipeline (S2-09).
 */
public class KetQuaGiaiDoanDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String MA_LOI_TRUNG_MA = "TRUNG_MA";
    public static final String MA_LOI_DANG_CO_CO_HOI = "DANG_CO_CO_HOI";
    public static final String MA_LOI_KHONG_CO_QUYEN = "KHONG_CO_QUYEN";

    private boolean thanhCong;
    private String thongBao;
    private GiaiDoanPipeline giaiDoan;
    private Map<String, String> danhSachLoi = new HashMap<>();

    public KetQuaGiaiDoanDTO() {
    }

    public static KetQuaGiaiDoanDTO thanhCong(GiaiDoanPipeline giaiDoan, String thongBao) {
        KetQuaGiaiDoanDTO dto = new KetQuaGiaiDoanDTO();
        dto.thanhCong = true;
        dto.giaiDoan = giaiDoan;
        dto.thongBao = thongBao;
        return dto;
    }

    public static KetQuaGiaiDoanDTO thatBai(String thongBao) {
        KetQuaGiaiDoanDTO dto = new KetQuaGiaiDoanDTO();
        dto.thanhCong = false;
        dto.thongBao = thongBao;
        return dto;
    }

    public static KetQuaGiaiDoanDTO loiTrungMa(String maGiaiDoan) {
        KetQuaGiaiDoanDTO dto = new KetQuaGiaiDoanDTO();
        dto.thanhCong = false;
        dto.thongBao = "Mã giai đoạn '" + maGiaiDoan + "' đã tồn tại trong hệ thống. Vui lòng chọn mã khác.";
        dto.danhSachLoi.put("maGiaiDoan", MA_LOI_TRUNG_MA);
        return dto;
    }

    public static KetQuaGiaiDoanDTO loiDangCoCoHoi(String tenGiaiDoan, int soCoHoi) {
        KetQuaGiaiDoanDTO dto = new KetQuaGiaiDoanDTO();
        dto.thanhCong = false;
        dto.thongBao = "Giai đoạn '" + tenGiaiDoan + "' đang có " + soCoHoi + " cơ hội đang chạy. Để đảm bảo không làm hỏng cơ hội, hệ thống không cho phép xoá mà chỉ cho phép chuyển sang 'Ngừng áp dụng'.";
        dto.danhSachLoi.put("_global", MA_LOI_DANG_CO_CO_HOI);
        return dto;
    }

    public void themLoi(String truong, String thongDiep) {
        this.danhSachLoi.put(truong, thongDiep);
        this.thanhCong = false;
    }

    public boolean isThanhCong() {
        return thanhCong;
    }

    public void setThanhCong(boolean thanhCong) {
        this.thanhCong = thanhCong;
    }

    public String getThongBao() {
        return thongBao;
    }

    public void setThongBao(String thongBao) {
        this.thongBao = thongBao;
    }

    public GiaiDoanPipeline getGiaiDoan() {
        return giaiDoan;
    }

    public void setGiaiDoan(GiaiDoanPipeline giaiDoan) {
        this.giaiDoan = giaiDoan;
    }

    public Map<String, String> getDanhSachLoi() {
        return danhSachLoi;
    }

    public void setDanhSachLoi(Map<String, String> danhSachLoi) {
        this.danhSachLoi = danhSachLoi != null ? danhSachLoi : new HashMap<>();
    }
}
