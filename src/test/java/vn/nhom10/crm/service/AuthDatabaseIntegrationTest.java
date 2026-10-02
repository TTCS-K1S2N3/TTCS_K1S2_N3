package vn.nhom10.crm.service;

import org.junit.jupiter.api.*;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dao.CauHinhHeThongDAO;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dao.PhienDangNhapDAO;
import vn.nhom10.crm.dto.DangNhapResult;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử tích hợp AuthService với MySQL thực tế")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthDatabaseIntegrationTest {

    private static final String TEST_EMAIL = "test.s101.integration@crm.vn";
    private static final String RAW_PASSWORD = "CorrectPassword#123";

    private static AuthService authService;
    private static NguoiDungDAO nguoiDungDAO;
    private static CauHinhHeThongDAO cauHinhHeThongDAO;
    private static PhienDangNhapDAO phienDangNhapDAO;
    private static Long createdUserId;
    private static boolean dbAvailable = false;

    @BeforeAll
    static void initDatabaseAndSeedUser() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            dbAvailable = true;
            cauHinhHeThongDAO = new CauHinhHeThongDAO();
            nguoiDungDAO = new NguoiDungDAO();
            phienDangNhapDAO = new PhienDangNhapDAO();
            authService = new AuthService(nguoiDungDAO, cauHinhHeThongDAO, phienDangNhapDAO);

            // Xóa dữ liệu cũ nếu còn tồn đọng
            xoaDuLieuKiemThu(conn, TEST_EMAIL);

            // Tạo người dùng kiểm thử
            NguoiDung nd = new NguoiDung();
            nd.setHoTen("Nguyễn Văn Tích Hợp S101");
            nd.setEmail(TEST_EMAIL);
            nd.setMatKhauHash(PasswordUtil.hashPassword(RAW_PASSWORD));
            nd.setTrangThai("HOAT_DONG");
            nd.setSoLanDangNhapSai(0);
            nd.setBatBuocDoiMatKhau(false);
            nd.setSessionVersion(1);

            createdUserId = nguoiDungDAO.taoNguoiDung(nd);
            assertNotNull(createdUserId, "Phải tạo được người dùng kiểm thử trong MySQL");

            // Gán vai trò SALES_REP (id 1)
            VaiTro salesRole = nguoiDungDAO.timVaiTroTheoMa("SALES_REP");
            assertNotNull(salesRole, "Vai trò SALES_REP phải tồn tại trong canonical seed");
            nguoiDungDAO.ganVaiTro(createdUserId, salesRole.getId());

        } catch (Exception e) {
            System.err.println("Không kết nối được MySQL thực tế, bỏ qua kiểm thử tích hợp DB: " + e.getMessage());
            dbAvailable = false;
        }
    }

    @AfterAll
    static void cleanup() {
        if (dbAvailable) {
            try (Connection conn = DatabaseConfig.getConnection()) {
                xoaDuLieuKiemThu(conn, TEST_EMAIL);
            } catch (SQLException e) {
                System.err.println("Lỗi dọn dẹp dữ liệu: " + e.getMessage());
            }
        }
    }

    private static void xoaDuLieuKiemThu(Connection conn, String email) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM phien_dang_nhap WHERE nguoi_dung_id IN (SELECT id FROM nguoi_dung WHERE email = ?)")) {
            ps.setString(1, email);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM nguoi_dung_vai_tro WHERE nguoi_dung_id IN (SELECT id FROM nguoi_dung WHERE email = ?)")) {
            ps.setString(1, email);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM nguoi_dung WHERE email = ?")) {
            ps.setString(1, email);
            ps.executeUpdate();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Tích hợp AC1: Đăng nhập đúng vào MySQL trả về vai trò SALES_REP")
    void testDangNhapDung_MySQL() {
        Assumptions.assumeTrue(dbAvailable, "Bỏ qua nếu MySQL không khả dụng");

        DangNhapResult result = authService.dangNhap(TEST_EMAIL, RAW_PASSWORD, "127.0.0.1", "IntegrationTest", "raw-session-ac1");
        assertTrue(result.isThanhCong(), "Đăng nhập với mật khẩu đúng phải thành công");
        assertNotNull(result.getNguoiDung());
        assertEquals("SALES_REP", result.getNguoiDung().getVaiTroChinh().getMaVaiTro());
    }

    @Test
    @Order(2)
    @DisplayName("Tích hợp AC2: Email không tồn tại và sai mật khẩu trả về cùng thông báo chung")
    void testSaiThongTin_MySQL() {
        Assumptions.assumeTrue(dbAvailable, "Bỏ qua nếu MySQL không khả dụng");

        // Trường hợp 1: email không tồn tại
        DangNhapResult r1 = authService.dangNhap("nonexistent_email_999@crm.vn", RAW_PASSWORD, "127.0.0.1", "Test", "s1");
        assertFalse(r1.isThanhCong());
        assertEquals(AuthService.THONG_BAO_SAI_THONG_TIN, r1.getThongBao());

        // Trường hợp 2: email tồn tại nhưng mật khẩu sai
        DangNhapResult r2 = authService.dangNhap(TEST_EMAIL, "WrongPassword#999", "127.0.0.1", "Test", "s2");
        assertFalse(r2.isThanhCong());
        assertEquals(AuthService.THONG_BAO_SAI_THONG_TIN, r2.getThongBao());

        // Chứng minh thông báo giống hệt nhau
        assertEquals(r1.getThongBao(), r2.getThongBao());
    }

    @Test
    @Order(3)
    @DisplayName("Tích hợp AC3: Nhập sai đủ 5 lần liên tiếp trong DB sẽ khóa tạm 15 phút")
    void testKhoaTam15PhutSau5LanSai_MySQL() {
        Assumptions.assumeTrue(dbAvailable, "Bỏ qua nếu MySQL không khả dụng");

        // Đã sai 1 lần ở test trước (order 2), nhập sai thêm 3 lần nữa (tổng 4 lần)
        for (int i = 0; i < 3; i++) {
            DangNhapResult r = authService.dangNhap(TEST_EMAIL, "SaiPass" + i, "127.0.0.1", "Test", "s" + i);
            assertFalse(r.isThanhCong());
            assertEquals(AuthService.THONG_BAO_SAI_THONG_TIN, r.getThongBao());
        }

        // Lần thứ 5 sai -> Phải bị khóa tạm 15 phút
        DangNhapResult r5 = authService.dangNhap(TEST_EMAIL, "SaiPassLan5", "127.0.0.1", "Test", "s5");
        assertFalse(r5.isThanhCong());
        assertEquals(DangNhapResult.Status.KHOA_TAM_15_PHUT, r5.getStatus());
        assertEquals(AuthService.THONG_BAO_KHOA_TAM_15_PHUT, r5.getThongBao());

        // Thử lại ngay sau đó (kể cả với mật khẩu đúng) -> Vẫn phải bị khóa
        DangNhapResult rTiepTheo = authService.dangNhap(TEST_EMAIL, RAW_PASSWORD, "127.0.0.1", "Test", "sNext");
        assertFalse(rTiepTheo.isThanhCong());
        assertEquals(DangNhapResult.Status.KHOA_TAM_15_PHUT, rTiepTheo.getStatus());
        assertEquals(AuthService.THONG_BAO_DANG_BI_KHOA_TAM, rTiepTheo.getThongBao());
    }
}
