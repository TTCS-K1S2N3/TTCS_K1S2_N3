package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dao.NhomKinhDoanhDAO;
import vn.nhom10.crm.dao.VaiTroDAO;
import vn.nhom10.crm.dto.GanVaiTroNhomDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhomKinhDoanh;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service xử lý nghiệp vụ gán vai trò và gắn người dùng vào nhóm kinh doanh (Story S1-09).
 * Đảm bảo các tiêu chuẩn nghiệm thu (Acceptance Criteria):
 * 1. Một người dùng có thể giữ nhiều vai trò cùng lúc.
 * 2. Người giữ vai trò Trưởng nhóm phải được gán một nhóm cụ thể.
 * 3. Không thể tự thu hồi vai trò quản trị của chính mình.
 */
public class PhanQuyenService {

    private static final Logger LOGGER = Logger.getLogger(PhanQuyenService.class.getName());

    private final NguoiDungDAO nguoiDungDAO;
    private final VaiTroDAO vaiTroDAO;
    private final NhomKinhDoanhDAO nhomKinhDoanhDAO;

    private static final PhanQuyenService INSTANCE = new PhanQuyenService();

    public static PhanQuyenService getInstance() {
        return INSTANCE;
    }

    public PhanQuyenService() {
        this.nguoiDungDAO = new NguoiDungDAO();
        this.vaiTroDAO = new VaiTroDAO();
        this.nhomKinhDoanhDAO = new NhomKinhDoanhDAO();
    }

    // Constructor hỗ trợ Dependency Injection phục vụ Unit Test độc lập
    public PhanQuyenService(NguoiDungDAO nguoiDungDAO, VaiTroDAO vaiTroDAO, NhomKinhDoanhDAO nhomKinhDoanhDAO) {
        this.nguoiDungDAO = nguoiDungDAO;
        this.vaiTroDAO = vaiTroDAO;
        this.nhomKinhDoanhDAO = nhomKinhDoanhDAO;
    }

    /**
     * Gán danh sách vai trò và nhóm kinh doanh cho một người dùng.
     *
     * @param nguoiDungId       ID người dùng cần phân quyền
     * @param danhSachVaiTroId  Danh sách các ID vai trò muốn gán
     * @param nhomKinhDoanhId   ID nhóm kinh doanh (có thể null nếu không thuộc nhóm nào, trừ Trưởng nhóm)
     * @param nguoiThucHienId   ID của quản trị viên đang thực hiện thao tác
     * @return DTO chứa trạng thái thành công/thất bại và thông báo phản hồi
     */
    public GanVaiTroNhomDTO ganVaiTroVaNhomKinhDoanh(int nguoiDungId, List<Integer> danhSachVaiTroId, Integer nhomKinhDoanhId, int nguoiThucHienId) {
        // 1. Kiểm tra người dùng tồn tại
        NguoiDung nguoiDung = nguoiDungDAO.timTheoId(nguoiDungId);
        if (nguoiDung == null) {
            return GanVaiTroNhomDTO.thatBai("Người dùng không tồn tại trong hệ thống.");
        }

        // 2. Kiểm tra danh sách vai trò
        if (danhSachVaiTroId == null || danhSachVaiTroId.isEmpty()) {
            return GanVaiTroNhomDTO.thatBai("Người dùng phải được gán ít nhất một vai trò.");
        }

        // Tải danh sách các đối tượng VaiTro tương ứng
        List<VaiTro> dsVaiTroMoi = new ArrayList<>();
        boolean coVaiTroTruongNhom = false;
        boolean coVaiTroAdminMoi = false;

        for (Integer vaiTroId : danhSachVaiTroId) {
            if (vaiTroId == null) continue;
            VaiTro vt = vaiTroDAO.timTheoId(vaiTroId);
            if (vt == null) {
                return GanVaiTroNhomDTO.thatBai("Vai trò có mã ID=" + vaiTroId + " không hợp lệ.");
            }
            dsVaiTroMoi.add(vt);
            if (VaiTroEnum.TEAM_LEAD.getMaVaiTro().equalsIgnoreCase(vt.getMaVaiTro())) {
                coVaiTroTruongNhom = true;
            }
            if (VaiTroEnum.ADMIN.getMaVaiTro().equalsIgnoreCase(vt.getMaVaiTro())) {
                coVaiTroAdminMoi = true;
            }
        }

        // 3. AC: Người giữ vai trò Trưởng nhóm (TEAM_LEAD) phải được gán một nhóm cụ thể
        if (coVaiTroTruongNhom) {
            if (nhomKinhDoanhId == null || nhomKinhDoanhId <= 0) {
                return GanVaiTroNhomDTO.thatBai("Người giữ vai trò Trưởng nhóm kinh doanh bắt buộc phải được gán vào một nhóm kinh doanh cụ thể.");
            }
            NhomKinhDoanh nhom = nhomKinhDoanhDAO.timTheoId(nhomKinhDoanhId);
            if (nhom == null) {
                return GanVaiTroNhomDTO.thatBai("Nhóm kinh doanh được chọn không tồn tại trong hệ thống.");
            }
        }

        // 4. AC: Không thể tự thu hồi vai trò quản trị (Admin) của chính mình
        if (nguoiDungId == nguoiThucHienId) {
            boolean hienTaiLaAdmin = nguoiDung.coVaiTro(VaiTroEnum.ADMIN);
            if (hienTaiLaAdmin && !coVaiTroAdminMoi) {
                return GanVaiTroNhomDTO.thatBai("Không thể tự thu hồi vai trò quản trị (Admin) của chính mình.");
            }
        }

        // 5. Thực thi cập nhật với Transaction trong DAO
        try {
            boolean kq = nguoiDungDAO.capNhatVaiTroVaNhomTransaction(nguoiDungId, danhSachVaiTroId, nhomKinhDoanhId);
            if (kq) {
                return GanVaiTroNhomDTO.thanhCong("Gán vai trò và nhóm kinh doanh thành công cho người dùng " + nguoiDung.getHoTen() + ".");
            } else {
                return GanVaiTroNhomDTO.thatBai("Không thể cập nhật phân quyền cho người dùng.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật phân quyền: " + e.getMessage(), e);
            return GanVaiTroNhomDTO.thatBai("Lỗi hệ thống khi cập nhật cơ sở dữ liệu: " + e.getMessage());
        }
    }

    public List<VaiTro> layDanhSachTatCaVaiTro() {
        return vaiTroDAO.layTatCa();
    }

    public List<NhomKinhDoanh> layDanhSachTatCaNhomKinhDoanh() {
        return nhomKinhDoanhDAO.layTatCa();
    }

    public List<NguoiDung> layDanhSachNguoiDung() {
        return nguoiDungDAO.layTatCa();
    }

    public NguoiDung layThongTinNguoiDung(int nguoiDungId) {
        return nguoiDungDAO.timTheoId(nguoiDungId);
    }
}
