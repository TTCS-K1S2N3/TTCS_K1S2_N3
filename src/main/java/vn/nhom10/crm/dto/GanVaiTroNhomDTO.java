package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO chứa thông tin yêu cầu và kết quả gán vai trò & nhóm kinh doanh cho người dùng (Story S1-09).
 */
public class GanVaiTroNhomDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int nguoiDungId;
    private List<Integer> danhSachVaiTroId = new ArrayList<>();
    private Integer nhomKinhDoanhId;
    private int nguoiThucHienId;
    private boolean thanhCong;
    private String thongBao;

    public GanVaiTroNhomDTO() {
    }

    public GanVaiTroNhomDTO(int nguoiDungId, List<Integer> danhSachVaiTroId, Integer nhomKinhDoanhId, int nguoiThucHienId) {
        this.nguoiDungId = nguoiDungId;
        this.danhSachVaiTroId = danhSachVaiTroId != null ? danhSachVaiTroId : new ArrayList<>();
        this.nhomKinhDoanhId = nhomKinhDoanhId;
        this.nguoiThucHienId = nguoiThucHienId;
    }

    public static GanVaiTroNhomDTO thanhCong(String thongBao) {
        GanVaiTroNhomDTO dto = new GanVaiTroNhomDTO();
        dto.setThanhCong(true);
        dto.setThongBao(thongBao);
        return dto;
    }

    public static GanVaiTroNhomDTO thatBai(String thongBao) {
        GanVaiTroNhomDTO dto = new GanVaiTroNhomDTO();
        dto.setThanhCong(false);
        dto.setThongBao(thongBao);
        return dto;
    }

    public int getNguoiDungId() { return nguoiDungId; }
    public void setNguoiDungId(int nguoiDungId) { this.nguoiDungId = nguoiDungId; }

    public List<Integer> getDanhSachVaiTroId() { return danhSachVaiTroId; }
    public void setDanhSachVaiTroId(List<Integer> danhSachVaiTroId) {
        this.danhSachVaiTroId = danhSachVaiTroId != null ? danhSachVaiTroId : new ArrayList<>();
    }

    public Integer getNhomKinhDoanhId() { return nhomKinhDoanhId; }
    public void setNhomKinhDoanhId(Integer nhomKinhDoanhId) { this.nhomKinhDoanhId = nhomKinhDoanhId; }

    public int getNguoiThucHienId() { return nguoiThucHienId; }
    public void setNguoiThucHienId(int nguoiThucHienId) { this.nguoiThucHienId = nguoiThucHienId; }

    public boolean isThanhCong() { return thanhCong; }
    public void setThanhCong(boolean thanhCong) { this.thanhCong = thanhCong; }

    public String getThongBao() { return thongBao; }
    public void setThongBao(String thongBao) { this.thongBao = thongBao; }
}
