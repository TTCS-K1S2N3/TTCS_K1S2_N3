package vn.nhom10.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dao.NhomKinhDoanhDAO;
import vn.nhom10.crm.dao.VaiTroDAO;
import vn.nhom10.crm.dto.GanVaiTroNhomDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhomKinhDoanh;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Nghiệp vụ Gán Vai Trò & Nhóm Kinh Doanh (Story S1-09)")
class PhanQuyenServiceTest {

    static class FakeNguoiDungDAO extends NguoiDungDAO {
        final Map<Integer, NguoiDung> users = new HashMap<>();
        boolean throwException = false;
        int lastUpdatedUserId = 0;
        List<Integer> lastUpdatedRoles = null;
        Integer lastUpdatedTeamId = null;
        int callCount = 0;

        @Override
        public NguoiDung timTheoId(int id) {
            return users.get(id);
        }

        @Override
        public boolean capNhatVaiTroVaNhomTransaction(int nguoiDungId, List<Integer> danhSachVaiTroId, Integer nhomKinhDoanhId) throws SQLException {
            callCount++;
            if (throwException) {
                throw new SQLException("Database connection dropped");
            }
            this.lastUpdatedUserId = nguoiDungId;
            this.lastUpdatedRoles = danhSachVaiTroId;
            this.lastUpdatedTeamId = nhomKinhDoanhId;
            return true;
        }
    }

    static class FakeVaiTroDAO extends VaiTroDAO {
        final Map<Integer, VaiTro> roles = new HashMap<>();

        @Override
        public VaiTro timTheoId(int id) {
            return roles.get(id);
        }
    }

    static class FakeNhomKinhDoanhDAO extends NhomKinhDoanhDAO {
        final Map<Integer, NhomKinhDoanh> teams = new HashMap<>();

        @Override
        public NhomKinhDoanh timTheoId(int id) {
            return teams.get(id);
        }
    }

    private FakeNguoiDungDAO fakeNguoiDungDAO;
    private FakeVaiTroDAO fakeVaiTroDAO;
    private FakeNhomKinhDoanhDAO fakeNhomKinhDoanhDAO;
    private PhanQuyenService phanQuyenService;

    private VaiTro vaiTroAdmin;
    private VaiTro vaiTroTeamLead;
    private VaiTro vaiTroSalesRep;
    private VaiTro vaiTroMarketing;
    private NhomKinhDoanh nhomMienBac;

    @BeforeEach
    void setUp() {
        fakeNguoiDungDAO = new FakeNguoiDungDAO();
        fakeVaiTroDAO = new FakeVaiTroDAO();
        fakeNhomKinhDoanhDAO = new FakeNhomKinhDoanhDAO();

        phanQuyenService = new PhanQuyenService(fakeNguoiDungDAO, fakeVaiTroDAO, fakeNhomKinhDoanhDAO);

        vaiTroAdmin = new VaiTro(1, "ADMIN", "Quản trị hệ thống", "Admin");
        vaiTroTeamLead = new VaiTro(3, "TEAM_LEAD", "Trưởng nhóm kinh doanh", "Team Lead");
        vaiTroSalesRep = new VaiTro(4, "SALES_REP", "Nhân viên kinh doanh", "Sales Rep");
        vaiTroMarketing = new VaiTro(5, "MARKETING", "Nhân viên Marketing", "Marketing");

        fakeVaiTroDAO.roles.put(1, vaiTroAdmin);
        fakeVaiTroDAO.roles.put(3, vaiTroTeamLead);
        fakeVaiTroDAO.roles.put(4, vaiTroSalesRep);
        fakeVaiTroDAO.roles.put(5, vaiTroMarketing);

        nhomMienBac = new NhomKinhDoanh(2, "KD_MIEN_BAC", "Nhóm Kinh Doanh Miền Bắc", "Mô tả", 1);
        fakeNhomKinhDoanhDAO.teams.put(2, nhomMienBac);
    }

    @Test
    @DisplayName("AC1: Một người dùng có thể giữ nhiều vai trò cùng lúc thành công")
    void testGanNhieuVaiTroCungLucThanhCong() {
        NguoiDung user = new NguoiDung(4, "Thào A Khua", "sales@crm.vn");
        fakeNguoiDungDAO.users.put(4, user);

        List<Integer> roles = Arrays.asList(3, 4); // Team Lead + Sales Rep
        GanVaiTroNhomDTO ketQua = phanQuyenService.ganVaiTroVaNhomKinhDoanh(4, roles, 2, 1);

        assertTrue(ketQua.isThanhCong(), "Gán nhiều vai trò cùng lúc phải thành công");
        assertTrue(ketQua.getThongBao().contains("thành công"));
        assertEquals(1, fakeNguoiDungDAO.callCount);
        assertEquals(4, fakeNguoiDungDAO.lastUpdatedUserId);
        assertEquals(roles, fakeNguoiDungDAO.lastUpdatedRoles);
        assertEquals(2, fakeNguoiDungDAO.lastUpdatedTeamId);
    }

    @Test
    @DisplayName("AC2: Người giữ vai trò Trưởng nhóm kinh doanh được gán nhóm cụ thể thì thành công")
    void testTruongNhomCoNhomCuTheThanhCong() {
        NguoiDung user = new NguoiDung(3, "Lê Trưởng Nhóm", "teamlead@crm.vn");
        fakeNguoiDungDAO.users.put(3, user);

        List<Integer> roles = Collections.singletonList(3);
        GanVaiTroNhomDTO ketQua = phanQuyenService.ganVaiTroVaNhomKinhDoanh(3, roles, 2, 1);

        assertTrue(ketQua.isThanhCong());
        assertEquals(1, fakeNguoiDungDAO.callCount);
        assertEquals(3, fakeNguoiDungDAO.lastUpdatedUserId);
        assertEquals(2, fakeNguoiDungDAO.lastUpdatedTeamId);
    }

    @Test
    @DisplayName("AC2: Người giữ vai trò Trưởng nhóm kinh doanh KHÔNG được gán nhóm cụ thể (null/0) thì BỊ TỪ CHỐI")
    void testTruongNhomKhongGanNhomBiTuChoi() {
        NguoiDung user = new NguoiDung(3, "Lê Trưởng Nhóm", "teamlead@crm.vn");
        fakeNguoiDungDAO.users.put(3, user);

        List<Integer> roles = Collections.singletonList(3);
        GanVaiTroNhomDTO ketQuaNull = phanQuyenService.ganVaiTroVaNhomKinhDoanh(3, roles, null, 1);

        assertFalse(ketQuaNull.isThanhCong(), "Trưởng nhóm không có nhóm phải thất bại");
        assertTrue(ketQuaNull.getThongBao().contains("bắt buộc phải được gán vào một nhóm"));

        GanVaiTroNhomDTO ketQuaZero = phanQuyenService.ganVaiTroVaNhomKinhDoanh(3, roles, 0, 1);
        assertFalse(ketQuaZero.isThanhCong());

        assertEquals(0, fakeNguoiDungDAO.callCount);
    }

    @Test
    @DisplayName("AC3: Quản trị viên KHÔNG THỂ tự thu hồi vai trò quản trị (Admin) của chính mình")
    void testKhongTheTuThuHoiVaiTroAdminCuaChinhMinh() {
        NguoiDung admin = new NguoiDung(1, "Nguyễn Quản Trị", "admin@crm.vn");
        admin.themVaiTro(vaiTroAdmin);
        fakeNguoiDungDAO.users.put(1, admin);

        List<Integer> roles = Collections.singletonList(4);
        GanVaiTroNhomDTO ketQua = phanQuyenService.ganVaiTroVaNhomKinhDoanh(1, roles, 2, 1);

        assertFalse(ketQua.isThanhCong(), "Không được phép tự thu hồi vai trò quản trị của chính mình");
        assertTrue(ketQua.getThongBao().contains("thu hồi vai trò quản trị"));
        assertEquals(0, fakeNguoiDungDAO.callCount);
    }

    @Test
    @DisplayName("AC3: Quản trị viên tự cập nhật thông tin nhưng VẪN GIỮ vai trò Admin thì thành công")
    void testAdminTuCapNhatVaGiuVaiTroAdminThanhCong() {
        NguoiDung admin = new NguoiDung(1, "Nguyễn Quản Trị", "admin@crm.vn");
        admin.themVaiTro(vaiTroAdmin);
        fakeNguoiDungDAO.users.put(1, admin);

        List<Integer> roles = Arrays.asList(1, 4);
        GanVaiTroNhomDTO ketQua = phanQuyenService.ganVaiTroVaNhomKinhDoanh(1, roles, null, 1);

        assertTrue(ketQua.isThanhCong());
        assertEquals(1, fakeNguoiDungDAO.callCount);
        assertEquals(1, fakeNguoiDungDAO.lastUpdatedUserId);
    }

    @Test
    @DisplayName("AC3: Quản trị viên có thể thu hồi vai trò Admin của tài khoản Admin KHÁC")
    void testAdminThuHoiAdminCuaNguoiKhacThanhCong() {
        NguoiDung otherAdmin = new NguoiDung(2, "Admin Khác", "otheradmin@crm.vn");
        otherAdmin.themVaiTro(vaiTroAdmin);
        fakeNguoiDungDAO.users.put(2, otherAdmin);

        List<Integer> roles = Collections.singletonList(4);
        GanVaiTroNhomDTO ketQua = phanQuyenService.ganVaiTroVaNhomKinhDoanh(2, roles, null, 1);

        assertTrue(ketQua.isThanhCong(), "Quản trị viên khác được phép điều chỉnh vai trò của tài khoản khác");
        assertEquals(1, fakeNguoiDungDAO.callCount);
        assertEquals(2, fakeNguoiDungDAO.lastUpdatedUserId);
    }

    @Test
    @DisplayName("Validation: Danh sách vai trò rỗng bị từ chối")
    void testDanhSachVaiTroRongBiTuChoi() {
        NguoiDung user = new NguoiDung(4, "Thào A Khua", "sales@crm.vn");
        fakeNguoiDungDAO.users.put(4, user);

        GanVaiTroNhomDTO ketQua = phanQuyenService.ganVaiTroVaNhomKinhDoanh(4, Collections.emptyList(), 2, 1);

        assertFalse(ketQua.isThanhCong());
        assertTrue(ketQua.getThongBao().contains("ít nhất một vai trò"));
        assertEquals(0, fakeNguoiDungDAO.callCount);
    }

    @Test
    @DisplayName("Validation: Người dùng không tồn tại bị từ chối")
    void testNguoiDungKhongTonTaiBiTuChoi() {
        GanVaiTroNhomDTO ketQua = phanQuyenService.ganVaiTroVaNhomKinhDoanh(999, Arrays.asList(3, 4), 2, 1);

        assertFalse(ketQua.isThanhCong());
        assertTrue(ketQua.getThongBao().contains("không tồn tại"));
        assertEquals(0, fakeNguoiDungDAO.callCount);
    }

    @Test
    @DisplayName("Transaction: Rollback khi có lỗi database SQLException")
    void testRollbackKhiLoiDatabase() {
        NguoiDung user = new NguoiDung(4, "Thào A Khua", "sales@crm.vn");
        fakeNguoiDungDAO.users.put(4, user);
        fakeNguoiDungDAO.throwException = true;

        List<Integer> roles = Collections.singletonList(4);
        GanVaiTroNhomDTO ketQua = phanQuyenService.ganVaiTroVaNhomKinhDoanh(4, roles, null, 1);

        assertFalse(ketQua.isThanhCong());
        assertTrue(ketQua.getThongBao().contains("Lỗi hệ thống khi cập nhật"));
        assertEquals(1, fakeNguoiDungDAO.callCount);
    }
}
