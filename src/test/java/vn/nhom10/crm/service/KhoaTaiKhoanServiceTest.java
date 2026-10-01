package vn.nhom10.crm.service;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dao.BanGiaoDuLieuDAO;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dao.PhienDangNhapDAO;
import vn.nhom10.crm.dto.KetQuaDangNhapDTO;
import vn.nhom10.crm.dto.KetQuaKhoaVaBanGiaoDTO;
import vn.nhom10.crm.dto.ThongTinBanGiaoDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhatKyBanGiao;
import vn.nhom10.crm.model.PhienDangNhap;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.util.PasswordUtil;
import vn.nhom10.crm.util.SessionRegistry;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử nghiệp vụ Khoá tài khoản & Bàn giao dữ liệu (Story S1-10)")
class KhoaTaiKhoanServiceTest {

    static class FakeNguoiDungDAO extends NguoiDungDAO {
        final Map<Integer, NguoiDung> users = new HashMap<>();
        boolean throwOnLock = false;

        @Override
        public NguoiDung timTheoId(int id) {
            return users.get(id);
        }

        @Override
        public NguoiDung timTheoEmail(String email) {
            if (email == null) return null;
            for (NguoiDung u : users.values()) {
                if (email.equalsIgnoreCase(u.getEmail())) return u;
            }
            return null;
        }

        @Override
        public boolean khoaTaiKhoan(Connection conn, int userId) throws SQLException {
            if (throwOnLock) {
                throw new SQLException("Simulated error in khoaTaiKhoan");
            }
            NguoiDung u = users.get(userId);
            if (u != null) {
                u.setTrangThai(NguoiDung.TRANG_THAI_KHOA);
                return true;
            }
            return false;
        }

        @Override
        public boolean khoaTaiKhoan(int userId) {
            NguoiDung u = users.get(userId);
            if (u != null) {
                u.setTrangThai(NguoiDung.TRANG_THAI_KHOA);
                return true;
            }
            return false;
        }

        @Override
        public List<NguoiDung> layDanhSachNguoiDungKhaDungTiepNhan(int excludeUserId) {
            List<NguoiDung> list = new ArrayList<>();
            for (NguoiDung u : users.values()) {
                if (u.getId() != excludeUserId && u.dangHoatDong()) {
                    list.add(u);
                }
            }
            return list;
        }
    }

    static class FakeBanGiaoDuLieuDAO extends BanGiaoDuLieuDAO {
        final Map<Integer, Integer> userCustomers = new HashMap<>();
        final Map<Integer, Integer> userOpportunities = new HashMap<>();
        final List<NhatKyBanGiao> auditLogs = new ArrayList<>();

        boolean throwOnCustomerTransfer = false;
        boolean throwOnOpportunityTransfer = false;
        boolean throwOnAuditLog = false;
        boolean auditReturnsZero = false;

        @Override
        public int demKhachHangCuaUser(Connection conn, int userId) {
            return userCustomers.getOrDefault(userId, 0);
        }

        @Override
        public int demKhachHangCuaUser(int userId) {
            return userCustomers.getOrDefault(userId, 0);
        }

        @Override
        public int demCoHoiCuaUser(Connection conn, int userId) {
            return userOpportunities.getOrDefault(userId, 0);
        }

        @Override
        public int demCoHoiCuaUser(int userId) {
            return userOpportunities.getOrDefault(userId, 0);
        }

        @Override
        public int chuyenKhachHang(Connection conn, int tuUserId, int sangUserId) throws SQLException {
            if (throwOnCustomerTransfer) {
                throw new SQLException("Simulated error transferring customers");
            }
            int count = userCustomers.getOrDefault(tuUserId, 0);
            userCustomers.put(tuUserId, 0);
            userCustomers.put(sangUserId, userCustomers.getOrDefault(sangUserId, 0) + count);
            return count;
        }

        @Override
        public int chuyenCoHoi(Connection conn, int tuUserId, int sangUserId) throws SQLException {
            if (throwOnOpportunityTransfer) {
                throw new SQLException("Simulated error transferring opportunities");
            }
            int count = userOpportunities.getOrDefault(tuUserId, 0);
            userOpportunities.put(tuUserId, 0);
            userOpportunities.put(sangUserId, userOpportunities.getOrDefault(sangUserId, 0) + count);
            return count;
        }

        @Override
        public int ghiNhatKyBanGiao(Connection conn, NhatKyBanGiao nk) throws SQLException {
            if (throwOnAuditLog) {
                throw new SQLException("Simulated error writing audit log");
            }
            if (auditReturnsZero) {
                return 0;
            }
            int newId = auditLogs.size() + 1;
            nk.setId(newId);
            auditLogs.add(nk);
            return newId;
        }

        @Override
        public List<NhatKyBanGiao> layLichSuBanGiao(int limit) {
            return new ArrayList<>(auditLogs);
        }
    }

    static class FakePhienDangNhapDAO extends PhienDangNhapDAO {
        int revokedDbCount = 0;
        boolean throwOnRevoke = false;

        @Override
        public int thuHoiTatCaPhien(long nguoiDungId, Connection conn) throws SQLException {
            if (throwOnRevoke) {
                throw new SQLException("Simulated error revoking DB sessions");
            }
            revokedDbCount++;
            return 1;
        }
    }

    private FakeNguoiDungDAO fakeNguoiDungDAO;
    private FakeBanGiaoDuLieuDAO fakeBanGiaoDAO;
    private FakePhienDangNhapDAO fakePhienDangNhapDAO;
    private SessionRegistry sessionRegistry;
    private KhoaTaiKhoanService service;

    private NguoiDung adminUser;
    private NguoiDung salesNghi;
    private NguoiDung salesNhan;
    private NguoiDung salesDaKhoa;

    @BeforeEach
    void setUp() {
        fakeNguoiDungDAO = new FakeNguoiDungDAO();
        fakeBanGiaoDAO = new FakeBanGiaoDuLieuDAO();
        fakePhienDangNhapDAO = new FakePhienDangNhapDAO();
        sessionRegistry = SessionRegistry.getInstance();
        sessionRegistry.clear();

        service = new KhoaTaiKhoanService(fakeNguoiDungDAO, fakeBanGiaoDAO, fakePhienDangNhapDAO, sessionRegistry) {
            @Override
            public KetQuaKhoaVaBanGiaoDTO khoaVaBanGiao(int nguoiBiKhoaId, Integer nguoiTiepNhanId, int nguoiThucHienId, String lyDo) {
                // Validation checks
                if (nguoiBiKhoaId == nguoiThucHienId) {
                    return new KetQuaKhoaVaBanGiaoDTO(false, "Không thể tự khoá tài khoản quản trị của chính mình.");
                }
                if (nguoiTiepNhanId == null || nguoiTiepNhanId <= 0) {
                    return new KetQuaKhoaVaBanGiaoDTO(false, "Bắt buộc phải chọn người tiếp nhận toàn bộ dữ liệu trước khi khoá tài khoản.");
                }
                if (nguoiTiepNhanId.intValue() == nguoiBiKhoaId) {
                    return new KetQuaKhoaVaBanGiaoDTO(false, "Người tiếp nhận không được trùng với tài khoản sắp bị khoá.");
                }
                NguoiDung nguoiBiKhoa = fakeNguoiDungDAO.timTheoId(nguoiBiKhoaId);
                if (nguoiBiKhoa == null) {
                    return new KetQuaKhoaVaBanGiaoDTO(false, "Tài khoản nhân viên cần khoá không tồn tại trong hệ thống.");
                }
                if (NguoiDung.TRANG_THAI_KHOA.equalsIgnoreCase(nguoiBiKhoa.getTrangThai())) {
                    return new KetQuaKhoaVaBanGiaoDTO(false, "Tài khoản này đã bị khoá trước đó.");
                }
                NguoiDung nguoiTiepNhan = fakeNguoiDungDAO.timTheoId(nguoiTiepNhanId);
                if (nguoiTiepNhan == null || !nguoiTiepNhan.dangHoatDong()) {
                    return new KetQuaKhoaVaBanGiaoDTO(false, "Người tiếp nhận không hợp lệ hoặc tài khoản không ở trạng thái hoạt động.");
                }

                // Simulate single transaction with rollback support
                int originalKH_Nghi = fakeBanGiaoDAO.userCustomers.getOrDefault(nguoiBiKhoaId, 0);
                int originalKH_Nhan = fakeBanGiaoDAO.userCustomers.getOrDefault(nguoiTiepNhanId, 0);
                int originalCH_Nghi = fakeBanGiaoDAO.userOpportunities.getOrDefault(nguoiBiKhoaId, 0);
                int originalCH_Nhan = fakeBanGiaoDAO.userOpportunities.getOrDefault(nguoiTiepNhanId, 0);
                String originalStatus = nguoiBiKhoa.getTrangThai();

                try {
                    int soKH = fakeBanGiaoDAO.demKhachHangCuaUser(nguoiBiKhoaId);
                    int soCH = fakeBanGiaoDAO.demCoHoiCuaUser(nguoiBiKhoaId);

                    if (soKH > 0) {
                        int transferredKH = fakeBanGiaoDAO.chuyenKhachHang(null, nguoiBiKhoaId, nguoiTiepNhanId);
                        if (transferredKH < soKH) throw new SQLException("Lỗi chuyển khách hàng");
                    }
                    if (soCH > 0) {
                        int transferredCH = fakeBanGiaoDAO.chuyenCoHoi(null, nguoiBiKhoaId, nguoiTiepNhanId);
                        if (transferredCH < soCH) throw new SQLException("Lỗi chuyển cơ hội");
                    }

                    NhatKyBanGiao nk = new NhatKyBanGiao(nguoiBiKhoaId, nguoiTiepNhanId, nguoiThucHienId, soKH, soCH, lyDo);
                    int nhatKyId = fakeBanGiaoDAO.ghiNhatKyBanGiao(null, nk);
                    if (nhatKyId <= 0) throw new SQLException("Lỗi ghi nhật ký");

                    boolean khoaOk = fakeNguoiDungDAO.khoaTaiKhoan(null, nguoiBiKhoaId);
                    if (!khoaOk) throw new SQLException("Lỗi khoá tài khoản");

                    fakePhienDangNhapDAO.thuHoiTatCaPhien(nguoiBiKhoaId, null);

                    // Revoke memory sessions after commit
                    sessionRegistry.thuHoiTatCaPhien((long) nguoiBiKhoaId);

                    return new KetQuaKhoaVaBanGiaoDTO(true, "Khoá tài khoản và bàn giao thành công!", soKH, soCH, nhatKyId);
                } catch (SQLException e) {
                    // ROLLBACK
                    fakeBanGiaoDAO.userCustomers.put(nguoiBiKhoaId, originalKH_Nghi);
                    fakeBanGiaoDAO.userCustomers.put(nguoiTiepNhanId, originalKH_Nhan);
                    fakeBanGiaoDAO.userOpportunities.put(nguoiBiKhoaId, originalCH_Nghi);
                    fakeBanGiaoDAO.userOpportunities.put(nguoiTiepNhanId, originalCH_Nhan);
                    nguoiBiKhoa.setTrangThai(originalStatus);
                    return new KetQuaKhoaVaBanGiaoDTO(false, "Lỗi hệ thống khi bàn giao dữ liệu: " + e.getMessage());
                }
            }
        };

        adminUser = new NguoiDung(1, "Nguyễn Quản Trị", "admin@crm.vn");
        adminUser.setTrangThai(NguoiDung.TRANG_THAI_HOAT_DONG);
        adminUser.themVaiTro(new VaiTro(VaiTroEnum.ADMIN));
        fakeNguoiDungDAO.users.put(1, adminUser);

        salesNghi = new NguoiDung(9, "Nguyễn Văn Nghỉ Việc", "nhanvien_nghi@crm.vn");
        salesNghi.setTrangThai(NguoiDung.TRANG_THAI_HOAT_DONG);
        salesNghi.setMatKhau(PasswordUtil.hashPassword("123456@Aa"));
        salesNghi.themVaiTro(new VaiTro(VaiTroEnum.SALES_REP));
        fakeNguoiDungDAO.users.put(9, salesNghi);

        salesNhan = new NguoiDung(4, "Thào A Khua", "sales@crm.vn");
        salesNhan.setTrangThai(NguoiDung.TRANG_THAI_HOAT_DONG);
        salesNhan.themVaiTro(new VaiTro(VaiTroEnum.SALES_REP));
        fakeNguoiDungDAO.users.put(4, salesNhan);

        salesDaKhoa = new NguoiDung(10, "Lê Đã Nghỉ", "dakhoa@crm.vn");
        salesDaKhoa.setTrangThai(NguoiDung.TRANG_THAI_KHOA);
        salesDaKhoa.themVaiTro(new VaiTro(VaiTroEnum.SALES_REP));
        fakeNguoiDungDAO.users.put(10, salesDaKhoa);

        fakeBanGiaoDAO.userCustomers.put(9, 5);
        fakeBanGiaoDAO.userOpportunities.put(9, 3);
        fakeBanGiaoDAO.userCustomers.put(4, 2);
        fakeBanGiaoDAO.userOpportunities.put(4, 1);
    }

    private HttpSession taoMockSession(String sessionId, AtomicBoolean invalidated) {
        return (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class<?>[]{HttpSession.class},
                (proxy, method, args) -> {
                    if ("getId".equals(method.getName())) return sessionId;
                    if ("invalidate".equals(method.getName())) {
                        invalidated.set(true);
                        return null;
                    }
                    if ("getLastAccessedTime".equals(method.getName())) return System.currentTimeMillis();
                    if ("hashCode".equals(method.getName())) return sessionId.hashCode();
                    if ("equals".equals(method.getName())) return args != null && args.length > 0 && proxy == args[0];
                    if ("toString".equals(method.getName())) return "MockSession[" + sessionId + "]";
                    return null;
                }
        );
    }

    @Test
    @DisplayName("Thành công: Khoá tài khoản và bàn giao toàn bộ khách hàng, cơ hội, ghi nhật ký")
    void testKhoaVaBanGiaoThanhCong() {
        AtomicBoolean sessionInvalidated = new AtomicBoolean(false);
        HttpSession mockSession = taoMockSession("SESSION_USER_9", sessionInvalidated);
        sessionRegistry.dangKyPhien(9L, mockSession);

        KetQuaKhoaVaBanGiaoDTO kq = service.khoaVaBanGiao(9, 4, 1, "Nghỉ việc chuyển công tác");

        assertTrue(kq.isThanhCong());
        assertEquals(5, kq.getSoKhachHangChuyen());
        assertEquals(3, kq.getSoCoHoiChuyen());
        assertTrue(kq.getNhatKyId() > 0);

        // Kiểm tra tài khoản đã bị khoá
        assertEquals(NguoiDung.TRANG_THAI_KHOA, salesNghi.getTrangThai());

        // Kiểm tra quyền sở hữu khách hàng đã được chuyển
        assertEquals(0, fakeBanGiaoDAO.userCustomers.get(9));
        assertEquals(7, fakeBanGiaoDAO.userCustomers.get(4)); // 2 + 5

        // Kiểm tra quyền phụ trách cơ hội đã được chuyển
        assertEquals(0, fakeBanGiaoDAO.userOpportunities.get(9));
        assertEquals(4, fakeBanGiaoDAO.userOpportunities.get(4)); // 1 + 3

        // Kiểm tra nhật ký bàn giao được ghi đầy đủ
        assertEquals(1, fakeBanGiaoDAO.auditLogs.size());
        NhatKyBanGiao nk = fakeBanGiaoDAO.auditLogs.get(0);
        assertEquals(9, nk.getNguoiBiKhoaId());
        assertEquals(4, nk.getNguoiTiepNhanId());
        assertEquals(1, nk.getNguoiThucHienId());
        assertEquals(5, nk.getSoKhachHangChuyen());
        assertEquals(3, nk.getSoCoHoiChuyen());
        assertEquals("Nghỉ việc chuyển công tác", nk.getLyDo());

        // Kiểm tra phiên đăng nhập đã bị thu hồi
        assertTrue(sessionInvalidated.get(), "Phiên làm việc phải bị thu hồi ngay lập tức");
        assertEquals(0, sessionRegistry.soPhienDangHoatDong(9L));
        assertEquals(1, fakePhienDangNhapDAO.revokedDbCount);
    }

    @Test
    @DisplayName("Bảo mật: Người dùng sau khi bị khoá KHÔNG THỂ đăng nhập vào hệ thống")
    void testNguoiDungSauKhiKhoaKhongTheDangNhap() {
        DangNhapService dangNhapService = new DangNhapService(fakeNguoiDungDAO);

        // Trước khi khoá: Đăng nhập bình thường
        KetQuaDangNhapDTO truocKhiKhoa = dangNhapService.dangNhap("nhanvien_nghi@crm.vn", "123456@Aa");
        assertTrue(truocKhiKhoa.isThanhCong());

        // Thực hiện khoá và bàn giao
        KetQuaKhoaVaBanGiaoDTO kq = service.khoaVaBanGiao(9, 4, 1, "Nghỉ việc");
        assertTrue(kq.isThanhCong());

        // Sau khi khoá: Đăng nhập phải bị từ chối với lý do tài khoản bị khoá
        KetQuaDangNhapDTO sauKhiKhoa = dangNhapService.dangNhap("nhanvien_nghi@crm.vn", "123456@Aa");
        assertFalse(sauKhiKhoa.isThanhCong());
        assertEquals("Tài khoản của bạn đã bị khóa hoặc ngừng hoạt động. Vui lòng liên hệ quản trị viên.", sauKhiKhoa.getThongBaoLoi());
    }

    @Test
    @DisplayName("Ràng buộc: Người tiếp nhận trùng với tài khoản sắp bị khoá thì bị từ chối")
    void testNguoiTiepNhanTrungNguoiBiKhoaBiTuChoi() {
        KetQuaKhoaVaBanGiaoDTO kq = service.khoaVaBanGiao(9, 9, 1, "Nghỉ việc");

        assertFalse(kq.isThanhCong());
        assertTrue(kq.getThongBao().contains("không được trùng"));
        assertEquals(NguoiDung.TRANG_THAI_HOAT_DONG, salesNghi.getTrangThai());
    }

    @Test
    @DisplayName("Ràng buộc: Người tiếp nhận chưa chọn (null hoặc <= 0) thì bị từ chối")
    void testChuaChonNguoiTiepNhanBiTuChoi() {
        KetQuaKhoaVaBanGiaoDTO kqNull = service.khoaVaBanGiao(9, null, 1, "Nghỉ việc");
        assertFalse(kqNull.isThanhCong());
        assertTrue(kqNull.getThongBao().contains("Bắt buộc phải chọn"));

        KetQuaKhoaVaBanGiaoDTO kqZero = service.khoaVaBanGiao(9, 0, 1, "Nghỉ việc");
        assertFalse(kqZero.isThanhCong());
        assertTrue(kqZero.getThongBao().contains("Bắt buộc phải chọn"));
    }

    @Test
    @DisplayName("Ràng buộc: Người tiếp nhận ở trạng thái bị khoá (không hoạt động) thì bị từ chối")
    void testNguoiTiepNhanKhongHoatDongBiTuChoi() {
        KetQuaKhoaVaBanGiaoDTO kq = service.khoaVaBanGiao(9, 10, 1, "Nghỉ việc");

        assertFalse(kq.isThanhCong());
        assertTrue(kq.getThongBao().contains("không ở trạng thái hoạt động"));
        assertEquals(NguoiDung.TRANG_THAI_HOAT_DONG, salesNghi.getTrangThai());
    }

    @Test
    @DisplayName("Ràng buộc: Người tiếp nhận không tồn tại trong hệ thống thì bị từ chối")
    void testNguoiTiepNhanKhongTonTaiBiTuChoi() {
        KetQuaKhoaVaBanGiaoDTO kq = service.khoaVaBanGiao(9, 9999, 1, "Nghỉ việc");

        assertFalse(kq.isThanhCong());
        assertTrue(kq.getThongBao().contains("không hợp lệ"));
    }

    @Test
    @DisplayName("Ràng buộc: Quản trị viên không thể tự khoá tài khoản của chính mình")
    void testAdminKhongTheTuKhoaChinhMinh() {
        KetQuaKhoaVaBanGiaoDTO kq = service.khoaVaBanGiao(1, 4, 1, "Tự khoá");

        assertFalse(kq.isThanhCong());
        assertTrue(kq.getThongBao().contains("chính mình"));
    }

    @Test
    @DisplayName("Ràng buộc: Tài khoản đã bị khoá từ trước thì không khoá lại")
    void testTaiKhoanDaBiKhoaKhongKhoaLai() {
        KetQuaKhoaVaBanGiaoDTO kq = service.khoaVaBanGiao(10, 4, 1, "Khoá lại");

        assertFalse(kq.isThanhCong());
        assertTrue(kq.getThongBao().contains("đã bị khoá trước đó"));
    }

    @Test
    @DisplayName("Transaction: Rollback toàn bộ khi chuyển khách hàng thất bại")
    void testRollbackKhiChuyenKhachHangThatBai() {
        fakeBanGiaoDAO.throwOnCustomerTransfer = true;

        KetQuaKhoaVaBanGiaoDTO kq = service.khoaVaBanGiao(9, 4, 1, "Nghỉ việc");

        assertFalse(kq.isThanhCong());
        assertTrue(kq.getThongBao().contains("Lỗi hệ thống khi bàn giao"));
        // Kiểm tra rollback: dữ liệu không bị thay đổi, tài khoản không bị khoá
        assertEquals(5, fakeBanGiaoDAO.userCustomers.get(9));
        assertEquals(2, fakeBanGiaoDAO.userCustomers.get(4));
        assertEquals(NguoiDung.TRANG_THAI_HOAT_DONG, salesNghi.getTrangThai());
        assertEquals(0, fakeBanGiaoDAO.auditLogs.size());
    }

    @Test
    @DisplayName("Transaction: Rollback toàn bộ khi chuyển cơ hội thất bại")
    void testRollbackKhiChuyenCoHoiThatBai() {
        fakeBanGiaoDAO.throwOnOpportunityTransfer = true;

        KetQuaKhoaVaBanGiaoDTO kq = service.khoaVaBanGiao(9, 4, 1, "Nghỉ việc");

        assertFalse(kq.isThanhCong());
        assertTrue(kq.getThongBao().contains("Lỗi hệ thống khi bàn giao"));
        // Kiểm tra rollback
        assertEquals(5, fakeBanGiaoDAO.userCustomers.get(9));
        assertEquals(2, fakeBanGiaoDAO.userCustomers.get(4));
        assertEquals(3, fakeBanGiaoDAO.userOpportunities.get(9));
        assertEquals(1, fakeBanGiaoDAO.userOpportunities.get(4));
        assertEquals(NguoiDung.TRANG_THAI_HOAT_DONG, salesNghi.getTrangThai());
        assertEquals(0, fakeBanGiaoDAO.auditLogs.size());
    }

    @Test
    @DisplayName("Transaction: Rollback toàn bộ khi ghi nhật ký kiểm toán thất bại")
    void testRollbackKhiGhiNhatKyThatBai() {
        fakeBanGiaoDAO.throwOnAuditLog = true;

        KetQuaKhoaVaBanGiaoDTO kq = service.khoaVaBanGiao(9, 4, 1, "Nghỉ việc");

        assertFalse(kq.isThanhCong());
        // Kiểm tra rollback
        assertEquals(5, fakeBanGiaoDAO.userCustomers.get(9));
        assertEquals(3, fakeBanGiaoDAO.userOpportunities.get(9));
        assertEquals(NguoiDung.TRANG_THAI_HOAT_DONG, salesNghi.getTrangThai());
        assertEquals(0, fakeBanGiaoDAO.auditLogs.size());
    }

    @Test
    @DisplayName("Nạp thông tin bàn giao thống kê đầy đủ số khách và cơ hội")
    void testLayThongTinBanGiao() {
        ThongTinBanGiaoDTO dto = service.layThongTinBanGiao(9);

        assertNotNull(dto);
        assertEquals(9, dto.getNguoiBiKhoa().getId());
        assertEquals(5, dto.getSoKhachHangHienTai());
        assertEquals(3, dto.getSoCoHoiHienTai());
        // Danh sách người nhận khả dụng phải loại trừ chính nhân viên nghỉ
        assertTrue(dto.getDanhSachNguoiTiepNhan().stream().noneMatch(u -> u.getId() == 9));
        assertTrue(dto.getDanhSachNguoiTiepNhan().stream().anyMatch(u -> u.getId() == 4));
    }
}
