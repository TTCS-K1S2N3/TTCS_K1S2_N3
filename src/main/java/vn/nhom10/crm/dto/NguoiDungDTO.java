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
    private PhamViDuLieu phamViToiDa;
    private PhamViDuLieu phamViHienTai;

    public NguoiDungDTO() {
        this.phamViToiDa = PhamViDuLieu.CA_NHAN;
        this.phamViHienTai = PhamViDuLieu.CA_NHAN;
    }

    public NguoiDungDTO(Long id, String hoTen, String email, VaiTroEnum vaiTro, Long nhomKinhDoanhId, String tenNhom, PhamViDuLieu phamViToiDa) {
        this.id = id;
        this.hoTen = hoTen;
        this.email = email;
        this.vaiTro = vaiTro;
        this.nhomKinhDoanhId = nhomKinhDoanhId;
        this.tenNhom = tenNhom;
        // Fail-closed: Mặc định tối thiểu CA_NHAN nếu phamViToiDa null
        this.phamViToiDa = (phamViToiDa != null) ? phamViToiDa : PhamViDuLieu.CA_NHAN;
        this.phamViHienTai = this.phamViToiDa;
    }

    public NguoiDungDTO(Long id, String hoTen, String email, VaiTroEnum vaiTro, Long nhomKinhDoanhId, String tenNhom) {
        this(id, hoTen, email, vaiTro, nhomKinhDoanhId, tenNhom, PhamViDuLieu.CA_NHAN);
    }

    /**
     * Chuyển đổi từ model NguoiDung chuẩn trong session sang NguoiDungDTO.
     * Nguồn sự thật phamViToiDa được lấy từ danh sách VaiTro đã nạp từ DB (Fail-Closed).
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
        PhamViDuLieu phamViDb = nd.layPhamViToiDa(); // Nạp từ DB (Fail-closed)

        return new NguoiDungDTO(nd.getId(), nd.getHoTen(), nd.getEmail(), vaiTro, nhomId, tenNhom, phamViDb);
    }

    /**
     * Danh sách các phạm vi dữ liệu được phép chọn dựa trên phamViToiDa từ DB (Fail-Closed).
     * Tuyệt đối không tự nâng quyền theo tên vai trò nếu DB không cho phép.
     */
    public List<PhamViDuLieu> getDanhSachPhamViChoPhep() {
        List<PhamViDuLieu> list = new ArrayList<>();
        list.add(PhamViDuLieu.CA_NHAN);

        PhamViDuLieu max = getPhamViToiDa();
        if (max == PhamViDuLieu.NHOM || max == PhamViDuLieu.TOAN_BO) {
            list.add(PhamViDuLieu.NHOM);
        }
        if (max == PhamViDuLieu.TOAN_BO) {
            list.add(PhamViDuLieu.TOAN_BO);
        }

        return list;
    }

    public PhamViDuLieu getPhamViToiDa() {
        return (phamViToiDa != null) ? phamViToiDa : PhamViDuLieu.CA_NHAN;
    }

    public void setPhamViToiDa(PhamViDuLieu phamViToiDa) {
        this.phamViToiDa = (phamViToiDa != null) ? phamViToiDa : PhamViDuLieu.CA_NHAN;
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
