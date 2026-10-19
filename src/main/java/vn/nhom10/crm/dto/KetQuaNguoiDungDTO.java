package vn.nhom10.crm.dto;

import vn.nhom10.crm.model.NguoiDung;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO chứa kết quả xử lý nghiệp vụ người dùng từ tầng Service trả về Controller.
 */
public class KetQuaNguoiDungDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String MA_LOI_EMAIL_TRUNG = "EMAIL_TRUNG";

    private boolean thanhCong;
    private String thongBao;
    private NguoiDung nguoiDung;
    private String matKhauTam;
    private Map<String, String> danhSachLoi = new HashMap<>();

    public KetQuaNguoiDungDTO() {
    }

    public static KetQuaNguoiDungDTO thanhCong(NguoiDung nguoiDung, String thongBao, String matKhauTam) {
        KetQuaNguoiDungDTO dto = new KetQuaNguoiDungDTO();
        dto.thanhCong = true;
        dto.nguoiDung = nguoiDung;
        dto.thongBao = thongBao;
        dto.matKhauTam = matKhauTam;
        return dto;
    }

    public static KetQuaNguoiDungDTO thatBai(String thongBao) {
        KetQuaNguoiDungDTO dto = new KetQuaNguoiDungDTO();
        dto.thanhCong = false;
        dto.thongBao = thongBao;
        return dto;
    }

    public static KetQuaNguoiDungDTO loiEmailTrung(String email) {
        KetQuaNguoiDungDTO dto = new KetQuaNguoiDungDTO();
        dto.thanhCong = false;
        dto.thongBao = "Địa chỉ email " + email + " đã được sử dụng bởi một tài khoản khác trong hệ thống.";
        dto.danhSachLoi.put("email", MA_LOI_EMAIL_TRUNG);
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

    public NguoiDung getNguoiDung() {
        return nguoiDung;
    }

    public void setNguoiDung(NguoiDung nguoiDung) {
        this.nguoiDung = nguoiDung;
    }

    public String getMatKhauTam() {
        return matKhauTam;
    }

    public void setMatKhauTam(String matKhauTam) {
        this.matKhauTam = matKhauTam;
    }

    public Map<String, String> getDanhSachLoi() {
        return danhSachLoi;
    }

    public void setDanhSachLoi(Map<String, String> danhSachLoi) {
        this.danhSachLoi = danhSachLoi != null ? danhSachLoi : new HashMap<>();
    }
}
