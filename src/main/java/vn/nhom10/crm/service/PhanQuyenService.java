package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dao.NhomKinhDoanhDAO;
import vn.nhom10.crm.dao.VaiTroDAO;
import vn.nhom10.crm.dto.GanVaiTroNhomDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhomKinhDoanh;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;

import vn.nhom10.crm.model.HanhDongThayDoi;
import vn.nhom10.crm.model.LoaiDoiTuongNhayCam;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Service xử lý nghiệp vụ gán vai trò và gắn người dùng vào nhóm kinh doanh (Story S1-09).
 * Acceptance Criteria:
 * 1. Một người dùng có thể giữ nhiều vai trò cùng lúc.
 * 2. Người giữ vai trò Trưởng nhóm phải được gán một nhóm cụ thể.
 * 3. Không thể tự thu hồi vai trò quản trị của chính mình.
 */
public class PhanQuyenService {

    private static final Logger LOGGER = Logger.getLogger(PhanQuyenService.class.getName());

    private final NguoiDungDAO nguoiDungDAO;
    private final VaiTroDAO vaiTroDAO;
    private final NhomKinhDoanhDAO nhomKinhDoanhDAO;
    private final NhatKyThayDoiService nhatKyThayDoiService;

    private static final PhanQuyenService INSTANCE = new PhanQuyenService();

    public static PhanQuyenService getInstance() {
        return INSTANCE;
    }

    public PhanQuyenService() {
        this(new NguoiDungDAO(), new VaiTroDAO(), new NhomKinhDoanhDAO(), new NhatKyThayDoiService());
    }

    /** Constructor hỗ trợ Dependency Injection cho Unit Test. */
    public PhanQuyenService(NguoiDungDAO nguoiDungDAO, VaiTroDAO vaiTroDAO, NhomKinhDoanhDAO nhomKinhDoanhDAO) {
        this(nguoiDungDAO, vaiTroDAO, nhomKinhDoanhDAO, new NhatKyThayDoiService());
    }

    public PhanQuyenService(NguoiDungDAO nguoiDungDAO, VaiTroDAO vaiTroDAO, NhomKinhDoanhDAO nhomKinhDoanhDAO, NhatKyThayDoiService nhatKyThayDoiService) {
        this.nguoiDungDAO = nguoiDungDAO;
        this.vaiTroDAO = vaiTroDAO;
        this.nhomKinhDoanhDAO = nhomKinhDoanhDAO;
        this.nhatKyThayDoiService = nhatKyThayDoiService != null ? nhatKyThayDoiService : new NhatKyThayDoiService();
    }

    /**
     * Gán danh sách vai trò và nhóm kinh doanh cho một người dùng.
     *
     * @param nguoiDungId      ID người dùng cần phân quyền
     * @param danhSachVaiTroId Danh sách ID vai trò muốn gán
     * @param nhomKinhDoanhId  ID nhóm kinh doanh (null nếu không có nhóm, trừ Trưởng nhóm)
     * @param nguoiThucHienId  ID quản trị viên đang thực hiện thao tác
     * @return DTO chứa trạng thái thành công/thất bại và thông báo phản hồi
     */
    public GanVaiTroNhomDTO ganVaiTroVaNhomKinhDoanh(int nguoiDungId, List<Integer> danhSachVaiTroId,
                                                      Integer nhomKinhDoanhId, int nguoiThucHienId) {
        return ganVaiTroVaNhomKinhDoanh(nguoiDungId, danhSachVaiTroId, nhomKinhDoanhId, nguoiThucHienId, null, null);
    }

    public GanVaiTroNhomDTO ganVaiTroVaNhomKinhDoanh(int nguoiDungId, List<Integer> danhSachVaiTroId,
                                                      Integer nhomKinhDoanhId, int nguoiThucHienId,
                                                      String diaChiIp, String thietBi) {
        // 1. Kiểm tra người dùng tồn tại
        NguoiDung nguoiDung = nguoiDungDAO.timTheoId(nguoiDungId);
        if (nguoiDung == null) {
            return GanVaiTroNhomDTO.thatBai("Người dùng không tồn tại trong hệ thống.");
        }

        // 2. Kiểm tra danh sách vai trò không rỗng
        if (danhSachVaiTroId == null || danhSachVaiTroId.isEmpty()) {
            return GanVaiTroNhomDTO.thatBai("Người dùng phải được gán ít nhất một vai trò.");
        }

        // Nạp đối tượng VaiTro theo ID và kiểm tra tính hợp lệ
        List<VaiTro> dsVaiTroMoi = new ArrayList<>();
        boolean coVaiTroTruongNhom = false;
        boolean coVaiTroAdminMoi = false;

        for (Integer vaiTroId : danhSachVaiTroId) {
            if (vaiTroId == null) continue;
            VaiTro vt = vaiTroDAO.timTheoId((int) vaiTroId);
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

        // 3. AC2: Người giữ vai trò TEAM_LEAD bắt buộc phải được gán nhóm cụ thể
        if (coVaiTroTruongNhom) {
            if (nhomKinhDoanhId == null || nhomKinhDoanhId <= 0) {
                return GanVaiTroNhomDTO.thatBai(
                        "Người giữ vai trò Trưởng nhóm kinh doanh bắt buộc phải được gán vào một nhóm kinh doanh cụ thể.");
            }
            NhomKinhDoanh nhom = nhomKinhDoanhDAO.timTheoId(nhomKinhDoanhId);
            if (nhom == null) {
                return GanVaiTroNhomDTO.thatBai("Nhóm kinh doanh được chọn không tồn tại trong hệ thống.");
            }
        }

        // 4. AC3: Không thể tự thu hồi vai trò quản trị (Admin) của chính mình
        if (nguoiDungId == nguoiThucHienId) {
            boolean hienTaiLaAdmin = nguoiDung.coVaiTro(VaiTroEnum.ADMIN);
            if (hienTaiLaAdmin && !coVaiTroAdminMoi) {
                return GanVaiTroNhomDTO.thatBai("Không thể tự thu hồi vai trò quản trị (Admin) của chính mình.");
            }
        }

        // 5. Thực thi cập nhật với Transaction
        try {
            boolean kq = nguoiDungDAO.capNhatVaiTroVaNhomTransaction(nguoiDungId, danhSachVaiTroId, nhomKinhDoanhId);
            if (kq) {
                // Hook ghi nhật ký thay đổi dữ liệu nhạy cảm (Story S2-04)
                try {
                    String vaiTroTruoc = nguoiDung.getChuoiVaiTroHienThi();
                    String vaiTroSau = dsVaiTroMoi.stream().map(VaiTro::getTenVaiTro).collect(Collectors.joining(", "));
                    NguoiDung actor = nguoiDungDAO.timTheoId(nguoiThucHienId);
                    String actorName = actor != null ? actor.getHoTen() : "Quản trị hệ thống";
                    String actorEmail = actor != null ? actor.getEmail() : "";

                    nhatKyThayDoiService.ghiNhatKyThayDoi(
                            (long) nguoiThucHienId,
                            actorName,
                            actorEmail,
                            LoaiDoiTuongNhayCam.VAI_TRO_NGUOI_DUNG,
                            "ND-" + nguoiDungId,
                            "Tài khoản: " + nguoiDung.getHoTen(),
                            "Vai trò người dùng",
                            vaiTroTruoc != null && !vaiTroTruoc.isBlank() ? vaiTroTruoc : "Chưa có vai trò",
                            vaiTroSau,
                            HanhDongThayDoi.CAP_NHAT,
                            "Phân quyền vai trò tài khoản người dùng",
                            diaChiIp != null ? diaChiIp : "127.0.0.1",
                            thietBi != null ? thietBi : "Trình duyệt CRM"
                    );
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Không thể ghi nhật ký audit log cho phân quyền: " + ex.getMessage(), ex);
                }

                return GanVaiTroNhomDTO.thanhCong(
                        "Gán vai trò và nhóm kinh doanh thành công cho người dùng " + nguoiDung.getHoTen() + ".");
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
