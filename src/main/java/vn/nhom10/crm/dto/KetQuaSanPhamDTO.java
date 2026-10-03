package vn.nhom10.crm.dto;

import vn.nhom10.crm.model.SanPham;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO chứa kết quả xử lý nghiệp vụ danh mục sản phẩm và bảng giá (S2-05).
 */
public class KetQuaSanPhamDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String MA_LOI_TRUNG_MA = "TRUNG_MA";
    public static final String MA_LOI_DA_CO_TRONG_BAO_GIA = "DA_CO_TRONG_BAO_GIA";
    public static final String MA_LOI_KHONG_CO_QUYEN = "KHONG_CO_QUYEN";

    private boolean thanhCong;
    private String thongBao;
    private SanPham sanPham;
    private Map<String, String> danhSachLoi = new HashMap<>();

    public KetQuaSanPhamDTO() {
    }

    public static KetQuaSanPhamDTO thanhCong(SanPham sanPham, String thongBao) {
        KetQuaSanPhamDTO dto = new KetQuaSanPhamDTO();
        dto.thanhCong = true;
        dto.sanPham = sanPham;
        dto.thongBao = thongBao;
        return dto;
    }

    public static KetQuaSanPhamDTO thatBai(String thongBao) {
        KetQuaSanPhamDTO dto = new KetQuaSanPhamDTO();
        dto.thanhCong = false;
        dto.thongBao = thongBao;
        return dto;
    }

    public static KetQuaSanPhamDTO loiTrungMa(String maSanPham) {
        KetQuaSanPhamDTO dto = new KetQuaSanPhamDTO();
        dto.thanhCong = false;
        dto.thongBao = "Mã sản phẩm '" + maSanPham + "' đã tồn tại trong hệ thống. Vui lòng nhập mã khác.";
        dto.danhSachLoi.put("maSanPham", MA_LOI_TRUNG_MA);
        return dto;
    }

    public static KetQuaSanPhamDTO loiDaCoTrongBaoGia(String tenSanPham) {
        KetQuaSanPhamDTO dto = new KetQuaSanPhamDTO();
        dto.thanhCong = false;
        dto.thongBao = "Sản phẩm '" + tenSanPham + "' đã xuất hiện trong báo giá nên không thể xoá. Vui lòng chuyển trạng thái sang Ngừng kinh doanh.";
        dto.danhSachLoi.put("_global", MA_LOI_DA_CO_TRONG_BAO_GIA);
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

    public SanPham getSanPham() {
        return sanPham;
    }

    public void setSanPham(SanPham sanPham) {
        this.sanPham = sanPham;
    }

    public Map<String, String> getDanhSachLoi() {
        return danhSachLoi;
    }

    public void setDanhSachLoi(Map<String, String> danhSachLoi) {
        this.danhSachLoi = danhSachLoi != null ? danhSachLoi : new HashMap<>();
    }
}
