package vn.nhom10.crm.dto;

import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.VaiTroEnum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DTO chứa thông tin phiên người dùng và các quyền phạm vi dữ liệu sở hữu (Story S1-05).
 */
public class NguoiDungDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String hoTen;
    private String email;
    private VaiTroEnum vaiTro;
    private Long nhomKinhDoanhId;
    private String tenNhom;
    private PhamViDuLieu phamViHienTai;

    public NguoiDungDTO() {
    }

    public NguoiDungDTO(Long id, String hoTen, String email, VaiTroEnum vaiTro, Long nhomKinhDoanhId, String tenNhom) {
        this.id = id;
        this.hoTen = hoTen;
        this.email = email;
        this.vaiTro = vaiTro;
        this.nhomKinhDoanhId = nhomKinhDoanhId;
        this.tenNhom = tenNhom;
        // Mặc định ban đầu chọn phạm vi tối đa user được phép hoặc CA_NHAN
        this.phamViHienTai = (vaiTro != null) ? vaiTro.getPhamViToiDa() : PhamViDuLieu.CA_NHAN;
    }

    /**
     * Chuyển đổi từ model NguoiDung chuẩn trong session sang NguoiDungDTO.
     */
    public static NguoiDungDTO tuNguoiDung(NguoiDung nd) {
        if (nd == null) {
            return null;
        }

        VaiTroEnum vaiTro = VaiTroEnum.SALES_REP;
        if (nd.coVaiTro("ADMIN")) {
            vaiTro = VaiTroEnum.ADMIN;
        } else if (nd.coVaiTro("DIRECTOR")) {
            vaiTro = VaiTroEnum.DIRECTOR;
        } else if (nd.coVaiTro("ACCOUNTANT")) {
            vaiTro = VaiTroEnum.ACCOUNTANT;
        } else if (nd.coVaiTro("TEAM_LEAD")) {
            vaiTro = VaiTroEnum.TEAM_LEAD;
        } else if (nd.coVaiTro("MARKETING")) {
            vaiTro = VaiTroEnum.MARKETING;
        } else if (nd.coVaiTro("CUST_SUCCESS")) {
            vaiTro = VaiTroEnum.CUST_SUCCESS;
        } else if (nd.coVaiTro("SALES_REP")) {
            vaiTro = VaiTroEnum.SALES_REP;
        }

        Long nhomId = nd.getNhomKinhDoanhId() != null ? nd.getNhomKinhDoanhId().longValue() : null;
        String tenNhom = nd.getTenNhomKinhDoanh() != null ? nd.getTenNhomKinhDoanh() : "Khối Kinh Doanh";

        return new NguoiDungDTO(nd.getId(), nd.getHoTen(), nd.getEmail(), vaiTro, nhomId, tenNhom);
    }

    /**
     * Danh sách các phạm vi dữ liệu mà vai trò người dùng được phép chọn.
     * - SALES_REP / MARKETING / CUST_SUCCESS: Chỉ được chọn CA_NHAN ("Của tôi")
     * - TEAM_LEAD: Được chọn CA_NHAN và NHOM ("Nhóm của tôi")
     * - DIRECTOR / ADMIN / ACCOUNTANT: Được chọn cả CA_NHAN, NHOM và TOAN_BO ("Tất cả")
     */
    public List<PhamViDuLieu> getDanhSachPhamViChoPhep() {
        if (vaiTro == null) {
            return Collections.singletonList(PhamViDuLieu.CA_NHAN);
        }

        List<PhamViDuLieu> list = new ArrayList<>();
        list.add(PhamViDuLieu.CA_NHAN);

        if (vaiTro == VaiTroEnum.TEAM_LEAD || vaiTro == VaiTroEnum.DIRECTOR 
                || vaiTro == VaiTroEnum.ADMIN || vaiTro == VaiTroEnum.ACCOUNTANT) {
            list.add(PhamViDuLieu.NHOM);
        }

        if (vaiTro == VaiTroEnum.DIRECTOR || vaiTro == VaiTroEnum.ADMIN 
                || vaiTro == VaiTroEnum.ACCOUNTANT) {
            list.add(PhamViDuLieu.TOAN_BO);
        }

        return list;
    }

    /**
     * Kiểm tra người dùng có quyền chọn phạm vi dữ liệu yêu cầu hay không.
     */
    public boolean coQuyenChonPhamVi(PhamViDuLieu phamVi) {
        if (phamVi == null) {
            return false;
        }
        return getDanhSachPhamViChoPhep().contains(phamVi);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public VaiTroEnum getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(VaiTroEnum vaiTro) {
        this.vaiTro = vaiTro;
    }

    public Long getNhomKinhDoanhId() {
        return nhomKinhDoanhId;
    }

    public void setNhomKinhDoanhId(Long nhomKinhDoanhId) {
        this.nhomKinhDoanhId = nhomKinhDoanhId;
    }

    public String getTenNhom() {
        return tenNhom;
    }

    public void setTenNhom(String tenNhom) {
        this.tenNhom = tenNhom;
    }

    public PhamViDuLieu getPhamViHienTai() {
        return phamViHienTai;
    }

    public void setPhamViHienTai(PhamViDuLieu phamViHienTai) {
        this.phamViHienTai = phamViHienTai;
    }
}
