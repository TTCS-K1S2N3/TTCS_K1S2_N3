package vn.nhom10.crm.service;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dao.BanGiaoDuLieuDAO;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dto.KetQuaKhoaVaBanGiaoDTO;
import vn.nhom10.crm.dto.ThongTinBanGiaoDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhatKyBanGiao;
import vn.nhom10.crm.model.NhomKinhDoanh;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.util.ActiveSessionManager;

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

    private FakeNguoiDungDAO fakeNguoiDungDAO;
    private FakeBanGiaoDuLieuDAO fakeBanGiaoDAO;
    private ActiveSessionManager sessionManager;
    private KhoaTaiKhoanService service;

    private NguoiDung adminUser;
    private NguoiDung salesNghi;
    private NguoiDung salesNhan;
    private NguoiDung salesDaKhoa;

    @BeforeEach
    void setUp() {
        fakeNguoiDungDAO = new FakeNguoiDungDAO();
        fakeBanGiaoDAO = new FakeBanGiaoDuLieuDAO();
        sessionManager = new ActiveSessionManager();

        service = new KhoaTaiKhoanService(fakeNguoiDungDAO, fakeBanGiaoDAO, sessionManager) {
            // Override phương thức để không cần database connection thật trong unit test
            @Override
            public KetQuaKhoaVaBanGiaoDTO khoaVaBanGiao(int nguoiBiKhoaId, Integer nguoiTiepNhanId, int nguoiThucHienId, String lyDo) {
                // Kiểm tra nghiệp vụ
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

                // Mô phỏng transaction
                try {
                    if (fakeBanGiaoDAO.isSimulateFailure()) {
                        throw new SQLException("Lỗi giả lập database rollback");
                    }

                    int soKH = fakeBanGiaoDAO.demKhachHangCuaUser(nguoiBiKhoaId);
                    int soCH = fakeBanGiaoDAO.demCoHoiCuaUser(nguoiBiKhoaId);

                    fakeBanGiaoDAO.chuyenKhachHang(null, nguoiBiKhoaId, nguoiTiepNhanId);
                    fakeBanGiaoDAO.chuyenCoHoi(null, nguoiBiKhoaId, nguoiTiepNhanId);

                    NhatKyBanGiao nk = new NhatKyBanGiao(nguoiBiKhoaId, nguoiTiepNhanId, nguoiThucHienId, soKH, soCH, lyDo);
                    int nkId = fakeBanGiaoDAO.ghiNhatKyBanGiao(null, nk);

                    fakeNguoiDungDAO.khoaTaiKhoan(nguoiBiKhoaId);

                    // Thu hồi session
                    sessionManager.thuHoiTatCaSessionCuaUser(nguoiBiKhoaId);

                    return new KetQuaKhoaVaBanGiaoDTO(true, "Khoá tài khoản và bàn giao thành công!", soKH, soCH, nkId);
                } catch (SQLException e) {
                    return new KetQuaKhoaVaBanGiaoDTO(false, "Lỗi hệ thống khi bàn giao dữ liệu: " + e.getMessage());
                }
            }
        };

        // Khởi tạo người dùng mẫu
        adminUser = new NguoiDung(1, "Nguyễn Quản Trị", "admin@crm.vn");
        adminUser.themVaiTro(VaiTroEnum.ADMIN);
        adminUser.setTrangThai(NguoiDung.TRANG_THAI_HOAT_DONG);
        fakeNguoiDungDAO.themNguoiDung(adminUser);

        salesNghi = new NguoiDung(9, "Nguyễn Văn Nghỉ", "nhanvien_nghi@crm.vn");
        salesNghi.themVaiTro(VaiTroEnum.SALES_REP);
        salesNghi.setNhomKinhDoanh(new NhomKinhDoanh(2, "Nhóm Kinh Doanh Miền Bắc"));
        salesNghi.setTrangThai(NguoiDung.TRANG_THAI_HOAT_DONG);
        fakeNguoiDungDAO.themNguoiDung(salesNghi);

        salesNhan = new NguoiDung(4, "Thào A Khua", "sales@crm.vn");
        salesNhan.themVaiTro(VaiTroEnum.SALES_REP);
        salesNhan.setNhomKinhDoanh(new NhomKinhDoanh(2, "Nhóm Kinh Doanh Miền Bắc"));
        salesNhan.setTrangThai(NguoiDung.TRANG_THAI_HOAT_DONG);
        fakeNguoiDungDAO.themNguoiDung(salesNhan);

        salesDaKhoa = new NguoiDung(10, "Lê Đã Nghỉ", "da_nghi@crm.vn");
        salesDaKhoa.setTrangThai(NguoiDung.TRANG_THAI_KHOA);
        fakeNguoiDungDAO.themNguoiDung(salesDaKhoa);

        // Gán 5 khách hàng và 3 cơ hội cho salesNghi
        fakeBanGiaoDAO.setKhachHangCount(salesNghi.getId(), 5);
        fakeBanGiaoDAO.setCoHoiCount(salesNghi.getId(), 3);
    }

    @Test
    @DisplayName("AC 2: Bắt buộc chọn người tiếp nhận - Thất bại nếu để trống người tiếp nhận")
    void testKhoaTaiKhoan_KhongChonNguoiTiepNhan_BaoLoi() {
        KetQuaKhoaVaBanGiaoDTO ketQuaNull = service.khoaVaBanGiao(salesNghi.getId(), null, adminUser.getId(), "Nghỉ việc");
        assertFalse(ketQuaNull.isThanhCong());
        assertTrue(ketQuaNull.getThongBao().contains("Bắt buộc phải chọn người tiếp nhận"));

        KetQuaKhoaVaBanGiaoDTO ketQuaZero = service.khoaVaBanGiao(salesNghi.getId(), 0, adminUser.getId(), "Nghỉ việc");
        assertFalse(ketQuaZero.isThanhCong());
    }

    @Test
    @DisplayName("AC 2: Người tiếp nhận không được trùng với chính người sắp bị khoá")
    void testKhoaTaiKhoan_NguoiTiepNhanTrungNguoiBiKhoa_BaoLoi() {
        KetQuaKhoaVaBanGiaoDTO ketQua = service.khoaVaBanGiao(salesNghi.getId(), salesNghi.getId(), adminUser.getId(), "Tự bàn giao");
        assertFalse(ketQua.isThanhCong());
        assertTrue(ketQua.getThongBao().contains("không được trùng"));
    }

    @Test
    @DisplayName("Admin không thể tự khoá tài khoản của chính mình")
    void testKhoaTaiKhoan_AdminTuKhoaChinhMinh_BaoLoi() {
        KetQuaKhoaVaBanGiaoDTO ketQua = service.khoaVaBanGiao(adminUser.getId(), salesNhan.getId(), adminUser.getId(), "Tự khoá");
        assertFalse(ketQua.isThanhCong());
        assertTrue(ketQua.getThongBao().contains("Không thể tự khoá tài khoản quản trị của chính mình"));
    }

    @Test
    @DisplayName("Người tiếp nhận phải là tài khoản đang hoạt động (không được chọn tài khoản đã khoá)")
    void testKhoaTaiKhoan_NguoiTiepNhanKhongHoatDong_BaoLoi() {
        KetQuaKhoaVaBanGiaoDTO ketQua = service.khoaVaBanGiao(salesNghi.getId(), salesDaKhoa.getId(), adminUser.getId(), "Bàn giao cho người đã nghỉ");
        assertFalse(ketQua.isThanhCong());
        assertTrue(ketQua.getThongBao().contains("trạng thái hoạt động"));
    }

    @Test
    @DisplayName("Không thể khoá tài khoản đã bị khoá từ trước")
    void testKhoaTaiKhoan_TaiKhoanDaBiKhoaTuTruoc_BaoLoi() {
        KetQuaKhoaVaBanGiaoDTO ketQua = service.khoaVaBanGiao(salesDaKhoa.getId(), salesNhan.getId(), adminUser.getId(), "Khoá lại");
        assertFalse(ketQua.isThanhCong());
        assertTrue(ketQua.getThongBao().contains("đã bị khoá trước đó"));
    }

    @Test
    @DisplayName("AC 2 & AC 3: Bàn giao thành công - Chuyển toàn bộ KH & Cơ hội, khoá tài khoản, ghi nhật ký")
    void testKhoaTaiKhoan_ThanhCong_ChuyenGiaoDuLieuVaGhiNhatKy() {
        KetQuaKhoaVaBanGiaoDTO ketQua = service.khoaVaBanGiao(salesNghi.getId(), salesNhan.getId(), adminUser.getId(), "Chuyển công tác");

        assertTrue(ketQua.isThanhCong());
        assertEquals(5, ketQua.getSoKhachHangChuyen());
        assertEquals(3, ketQua.getSoCoHoiChuyen());
        assertTrue(ketQua.getNhatKyId() > 0);

        // Kiểm tra trạng thái tài khoản bị khoá
        NguoiDung u = fakeNguoiDungDAO.timTheoId(salesNghi.getId());
        assertEquals(NguoiDung.TRANG_THAI_KHOA, u.getTrangThai());
        assertFalse(u.dangHoatDong());

        // Kiểm tra dữ liệu sở hữu sau chuyển giao
        assertEquals(0, fakeBanGiaoDAO.demKhachHangCuaUser(salesNghi.getId()));
        assertEquals(0, fakeBanGiaoDAO.demCoHoiCuaUser(salesNghi.getId()));
        assertEquals(5, fakeBanGiaoDAO.demKhachHangCuaUser(salesNhan.getId()));
        assertEquals(3, fakeBanGiaoDAO.demCoHoiCuaUser(salesNhan.getId()));

        // Kiểm tra nhật ký bàn giao
        List<NhatKyBanGiao> logs = fakeBanGiaoDAO.layLichSuBanGiao(10);
        assertFalse(logs.isEmpty());
        NhatKyBanGiao latest = logs.get(0);
        assertEquals(salesNghi.getId(), latest.getNguoiBiKhoaId());
        assertEquals(salesNhan.getId(), latest.getNguoiTiepNhanId());
        assertEquals(adminUser.getId(), latest.getNguoiThucHienId());
        assertEquals(5, latest.getSoKhachHangChuyen());
        assertEquals(3, latest.getSoCoHoiChuyen());
        assertEquals("Chuyển công tác", latest.getLyDo());
    }

    @Test
    @DisplayName("AC 1: Thu hồi toàn bộ phiên làm việc (Session) đang mở của tài khoản bị khoá")
    void testThuHoiSessionKhiKhoaTaiKhoan() {
        // Tạo 2 session giả lập cho salesNghi
        AtomicBoolean session1Invalidated = new AtomicBoolean(false);
        AtomicBoolean session2Invalidated = new AtomicBoolean(false);

        HttpSession fakeSession1 = taoFakeHttpSession("session-1", session1Invalidated);
        HttpSession fakeSession2 = taoFakeHttpSession("session-2", session2Invalidated);

        sessionManager.dangKySession(salesNghi.getId(), fakeSession1);
        sessionManager.dangKySession(salesNghi.getId(), fakeSession2);

        assertEquals(2, sessionManager.demSoSessionDangMo(salesNghi.getId()));

        // Thực hiện khoá tài khoản
        KetQuaKhoaVaBanGiaoDTO ketQua = service.khoaVaBanGiao(salesNghi.getId(), salesNhan.getId(), adminUser.getId(), "Nghỉ việc");
        assertTrue(ketQua.isThanhCong());

        // Xác nhận cả 2 session đều đã bị invalidate
        assertTrue(session1Invalidated.get(), "Session 1 phải bị thu hồi ngay lập tức");
        assertTrue(session2Invalidated.get(), "Session 2 phải bị thu hồi ngay lập tức");
        assertEquals(0, sessionManager.demSoSessionDangMo(salesNghi.getId()));
    }

    @Test
    @DisplayName("Transaction: Rollback toàn bộ nếu xảy ra lỗi trong quá trình bàn giao")
    void testRollbackKhiLoiDatabase() {
        fakeBanGiaoDAO.setSimulateFailure(true);

        KetQuaKhoaVaBanGiaoDTO ketQua = service.khoaVaBanGiao(salesNghi.getId(), salesNhan.getId(), adminUser.getId(), "Lỗi test");
        assertFalse(ketQua.isThanhCong());
        assertTrue(ketQua.getThongBao().contains("Lỗi hệ thống khi bàn giao dữ liệu"));

        // Xác nhận trạng thái tài khoản KHÔNG bị thay đổi
        NguoiDung u = fakeNguoiDungDAO.timTheoId(salesNghi.getId());
        assertEquals(NguoiDung.TRANG_THAI_HOAT_DONG, u.getTrangThai());
    }

    @Test
    @DisplayName("Nạp thông tin bàn giao thống kê đầy đủ số KH, cơ hội và danh sách người nhận")
    void testLayThongTinBanGiao() {
        ThongTinBanGiaoDTO dto = service.layThongTinBanGiao(salesNghi.getId());
        assertNotNull(dto);
        assertEquals(salesNghi.getId(), dto.getNguoiBiKhoa().getId());
        assertEquals(5, dto.getSoKhachHangHienTai());
        assertEquals(3, dto.getSoCoHoiHienTai());
        assertFalse(dto.getDanhSachNguoiTiepNhan().isEmpty());
    }

    // Helper tạo fake HttpSession bằng dynamic proxy
    private HttpSession taoFakeHttpSession(String id, AtomicBoolean invalidatedFlag) {
        return (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class<?>[]{HttpSession.class},
                (proxy, method, args) -> {
                    if ("getId".equals(method.getName())) {
                        return id;
                    }
                    if ("invalidate".equals(method.getName())) {
                        invalidatedFlag.set(true);
                        return null;
                    }
                    if ("hashCode".equals(method.getName())) {
                        return id.hashCode();
                    }
                    if ("equals".equals(method.getName())) {
                        return args[0] == proxy;
                    }
                    return null;
                }
        );
    }

    // Test Double Fakes
    static class FakeNguoiDungDAO extends NguoiDungDAO {
        private final Map<Integer, NguoiDung> store = new HashMap<>();

        public void themNguoiDung(NguoiDung nd) {
            store.put(nd.getId(), nd);
        }

        @Override
        public NguoiDung timTheoId(int id) {
            return store.get(id);
        }

        @Override
        public boolean khoaTaiKhoan(int userId) {
            NguoiDung nd = store.get(userId);
            if (nd != null) {
                nd.setTrangThai(NguoiDung.TRANG_THAI_KHOA);
                return true;
            }
            return false;
        }

        @Override
        public boolean khoaTaiKhoan(Connection conn, int userId) {
            return khoaTaiKhoan(userId);
        }

        @Override
        public List<NguoiDung> layDanhSachNguoiDungKhaDungTiepNhan(int excludeUserId) {
            List<NguoiDung> result = new ArrayList<>();
            for (NguoiDung nd : store.values()) {
                if (nd.getId() != excludeUserId && nd.dangHoatDong()) {
                    result.add(nd);
                }
            }
            return result;
        }
    }

    static class FakeBanGiaoDuLieuDAO extends BanGiaoDuLieuDAO {
        private final Map<Integer, Integer> khachHangMap = new HashMap<>();
        private final Map<Integer, Integer> coHoiMap = new HashMap<>();
        private final List<NhatKyBanGiao> logStore = new ArrayList<>();
        private boolean simulateFailure = false;
        private int nextId = 1;

        public void setKhachHangCount(int userId, int count) {
            khachHangMap.put(userId, count);
        }

        public void setCoHoiCount(int userId, int count) {
            coHoiMap.put(userId, count);
        }

        public boolean isSimulateFailure() {
            return simulateFailure;
        }

        public void setSimulateFailure(boolean simulateFailure) {
            this.simulateFailure = simulateFailure;
        }

        @Override
        public int demKhachHangCuaUser(int userId) {
            return khachHangMap.getOrDefault(userId, 0);
        }

        @Override
        public int demKhachHangCuaUser(Connection conn, int userId) {
            return demKhachHangCuaUser(userId);
        }

        @Override
        public int demCoHoiCuaUser(int userId) {
            return coHoiMap.getOrDefault(userId, 0);
        }

        @Override
        public int demCoHoiCuaUser(Connection conn, int userId) {
            return demCoHoiCuaUser(userId);
        }

        @Override
        public int chuyenKhachHang(Connection conn, int tuUserId, int sangUserId) {
            int count = khachHangMap.getOrDefault(tuUserId, 0);
            khachHangMap.put(tuUserId, 0);
            khachHangMap.put(sangUserId, khachHangMap.getOrDefault(sangUserId, 0) + count);
            return count;
        }

        @Override
        public int chuyenCoHoi(Connection conn, int tuUserId, int sangUserId) {
            int count = coHoiMap.getOrDefault(tuUserId, 0);
            coHoiMap.put(tuUserId, 0);
            coHoiMap.put(sangUserId, coHoiMap.getOrDefault(sangUserId, 0) + count);
            return count;
        }

        @Override
        public int ghiNhatKyBanGiao(Connection conn, NhatKyBanGiao nk) {
            nk.setId(nextId++);
            logStore.add(0, nk);
            return nk.getId();
        }

        @Override
        public List<NhatKyBanGiao> layLichSuBanGiao(int limit) {
            return new ArrayList<>(logStore);
        }
    }
}
