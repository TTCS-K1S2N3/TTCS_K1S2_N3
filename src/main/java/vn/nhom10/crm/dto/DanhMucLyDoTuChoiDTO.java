package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DTO định nghĩa danh mục lý do từ chối lead (Story S4-07, AC2, Q4).
 * Cung cấp danh mục các mã lý do đang hoạt động và chuyển đổi nhãn hiển thị.
 */
public class DanhMucLyDoTuChoiDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String ma;
    private String ten;
    private String moTa;
    private boolean hoatDong;

    public DanhMucLyDoTuChoiDTO() {
    }

    public DanhMucLyDoTuChoiDTO(String ma, String ten, String moTa, boolean hoatDong) {
        this.ma = ma;
        this.ten = ten;
        this.moTa = moTa;
        this.hoatDong = hoatDong;
    }

    public static List<DanhMucLyDoTuChoiDTO> layDanhMucMacDinh() {
        List<DanhMucLyDoTuChoiDTO> ds = new ArrayList<>();
        ds.add(new DanhMucLyDoTuChoiDTO("SAI_KHU_VUC", "Không thuộc khu vực phụ trách", "Lead thuộc địa bàn ngoài vùng quản lý", true));
        ds.add(new DanhMucLyDoTuChoiDTO("KHONG_PHU_HOP", "Lead không phù hợp", "Nhu cầu hoặc quy mô không khớp sản phẩm/dịch vụ", true));
        ds.add(new DanhMucLyDoTuChoiDTO("KHONG_LIEN_LAC_DUOC", "Không liên lạc được thông tin", "Số điện thoại/email không tồn tại hoặc sai thông tin", true));
        ds.add(new DanhMucLyDoTuChoiDTO("TRUNG_LAP", "Trùng lặp khách hàng đã có", "Khách hàng đã có nhân viên khác phụ trách", true));
        ds.add(new DanhMucLyDoTuChoiDTO("KHAC", "Lý do khác", "Lý do phát sinh khác cần giải thích chi tiết", true));
        return Collections.unmodifiableList(ds);
    }

    public static boolean laMaHopLe(String ma) {
        if (ma == null || ma.isBlank()) {
            return false;
        }
        String clean = ma.trim();
        for (DanhMucLyDoTuChoiDTO item : layDanhMucMacDinh()) {
            if (item.isHoatDong() && item.getMa().equalsIgnoreCase(clean)) {
                return true;
            }
        }
        return false;
    }

    public static String layTenTheoMa(String ma) {
        if (ma == null || ma.isBlank()) {
            return "";
        }
        for (DanhMucLyDoTuChoiDTO item : layDanhMucMacDinh()) {
            if (item.getMa().equalsIgnoreCase(ma.trim())) {
                return item.getTen();
            }
        }
        return ma;
    }

    public String getMa() {
        return ma;
    }

    public void setMa(String ma) {
        this.ma = ma;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public boolean isHoatDong() {
        return hoatDong;
    }

    public void setHoatDong(boolean hoatDong) {
        this.hoatDong = hoatDong;
    }
}
