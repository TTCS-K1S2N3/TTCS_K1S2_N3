package vn.nhom10.crm.service;

import org.junit.jupiter.api.*;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dao.PhienDangNhapDAO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử tích hợp Quản lý phiên S1-02 với MySQL thực tế")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SessionDatabaseIntegrationTest {

    private static final String TEST_EMAIL = "test.s102.session@crm.vn";
    private static final String SESSION_ACTIVE_RAW = "raw-session-s102-active";
    private static final String SESSION_EXPIRED_RAW = "raw-session-s102-expired";

    private static String activeHash;
    private static String expiredHash;

    private static PhienDangNhapDAO phienDangNhapDAO;
    private static NguoiDungDAO nguoiDungDAO;
    private static Long testUserId;
    private static boolean dbAvailable = false;

    @BeforeAll
    static void setUp() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            dbAvailable = true;
            phienDangNhapDAO = new PhienDangNhapDAO();
            nguoiDungDAO = new NguoiDungDAO();

            activeHash = PasswordUtil.sha256Hex(SESSION_ACTIVE_RAW);
            expiredHash = PasswordUtil.sha256Hex(SESSION_EXPIRED_RAW);

            // Dọn dẹp dữ liệu cũ
            xoaDuLieuKiemThu(conn, TEST_EMAIL);

            // Tạo người dùng kiểm thử
            NguoiDung nd = new NguoiDung();
            nd.setHoTen("Nguyễn Phiên S102");
            nd.setEmail(TEST_EMAIL);
            nd.setMatKhauHash(PasswordUtil.hashPassword("Pass#123"));
            nd.setTrangThai("HOAT_DONG");
            nd.setSessionVersion(1);

            testUserId = nguoiDungDAO.taoNguoiDung(nd);
            assertNotNull(testUserId, "Phải tạo được user kiểm thử");

            // Tạo phiên đang hoạt động ban đầu (thời hạn 5 phút)
            phienDangNhapDAO.taoPhien(testUserId, activeHash, 1, "127.0.0.1", "BrowserTest", 5);

            // Tạo phiên quá hạn (het_han_luc = NOW() - INTERVAL 5 MINUTE)
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO phien_dang_nhap (nguoi_dung_id, ma_phien_hash, session_version, dia_chi_ip, " +
                    "thong_tin_thiet_bi, trang_thai, het_han_luc) VALUES (?, ?, 1, '127.0.0.1', 'Browser', 'HOAT_DONG', " +
                    "DATE_SUB(NOW(), INTERVAL 5 MINUTE))")) {
                ps.setLong(1, testUserId);
                ps.setString(2, expiredHash);
                ps.executeUpdate();
            }

        } catch (Exception e) {
            System.err.println("Không kết nối được MySQL thực tế, bỏ qua kiểm thử tích hợp phiên: " + e.getMessage());
            dbAvailable = false;
        }
    }

    @AfterAll
    static void tearDown() {
        if (dbAvailable) {
            try (Connection conn = DatabaseConfig.getConnection()) {
                xoaDuLieuKiemThu(conn, TEST_EMAIL);
            } catch (SQLException e) {
                System.err.println("Lỗi dọn dẹp dữ liệu phiên: " + e.getMessage());
            }
        }
    }

    private static void xoaDuLieuKiemThu(Connection conn, String email) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM phien_dang_nhap WHERE nguoi_dung_id IN (SELECT id FROM nguoi_dung WHERE email = ?)")) {
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
    @DisplayName("Tích hợp S1-02-AC1: Phiên được gia hạn tự động khi còn hoạt động (đẩy lùi het_han_luc thêm 30 phút)")
    void testGiaHanPhienTuDong_MySQL() {
        Assumptions.assumeTrue(dbAvailable, "Bỏ qua nếu MySQL không khả dụng");

        // Trước khi gia hạn: kiểm tra phiên hợp lệ
        assertTrue(phienDangNhapDAO.kiemTraPhienHopLe(activeHash), "Phiên ban đầu phải hợp lệ");

        Timestamp beforeGiaHan = phienDangNhapDAO.layThoiGianHetHan(activeHash);
        assertNotNull(beforeGiaHan);

        // Gọi gia hạn phiên thêm 30 phút
        boolean giaHanResult = phienDangNhapDAO.giaHanPhien(activeHash, 30);
        assertTrue(giaHanResult, "Gia hạn phiên trong DB phải thành công");

        Timestamp afterGiaHan = phienDangNhapDAO.layThoiGianHetHan(activeHash);
        assertNotNull(afterGiaHan);

        // Thời gian hết hạn mới phải lớn hơn thời gian hết hạn ban đầu ít nhất 20 phút
        assertTrue(afterGiaHan.getTime() > beforeGiaHan.getTime() + 20 * 60 * 1000L,
                "Thời điểm hết hạn sau khi gia hạn phải được đẩy lùi về tương lai xấp xỉ 30 phút");
    }

    @Test
    @Order(2)
    @DisplayName("Tích hợp S1-02-AC2: Đăng xuất làm mất hiệu lực phiên ngay lập tức phía server (chuyển THU_HOI)")
    void testDangXuatMatHieuLucPhien_MySQL() {
        Assumptions.assumeTrue(dbAvailable, "Bỏ qua nếu MySQL không khả dụng");

        // Gọi thu hồi phiên
        boolean thuHoiResult = phienDangNhapDAO.thuHoiPhien(activeHash, "Đăng xuất chủ động");
        assertTrue(thuHoiResult, "Thu hồi phiên trong DB phải thành công");

        // Kiểm tra phiên không còn hợp lệ
        assertFalse(phienDangNhapDAO.kiemTraPhienHopLe(activeHash),
                "Sau khi đăng xuất thu hồi, kiemTraPhienHopLe phải trả về false ngay lập tức");

        // Kiểm tra trạng thái trong DB là THU_HOI
        String trangThai = phienDangNhapDAO.layTrangThaiPhien(activeHash);
        assertEquals("THU_HOI", trangThai, "Trạng thái trong DB phải là THU_HOI");
    }

    @Test
    @Order(3)
    @DisplayName("Tích hợp S1-02-AC3: Phiên quá hạn bị từ chối và được cập nhật HET_HAN")
    void testPhienQuaHanVaDanhDauHetHan_MySQL() {
        Assumptions.assumeTrue(dbAvailable, "Bỏ qua nếu MySQL không khả dụng");

        // Phiên expiredHash có het_han_luc trong quá khứ -> kiểm tra không hợp lệ
        assertFalse(phienDangNhapDAO.kiemTraPhienHopLe(expiredHash),
                "Phiên có het_han_luc trong quá khứ phải bị từ chối");

        // Đánh dấu HET_HAN
        boolean danhDauResult = phienDangNhapDAO.danhDauHetHan(expiredHash);
        assertTrue(danhDauResult, "Đánh dấu HET_HAN trong DB phải thành công");

        String trangThai = phienDangNhapDAO.layTrangThaiPhien(expiredHash);
        assertEquals("HET_HAN", trangThai, "Trạng thái trong DB phải chuyển sang HET_HAN");
    }

    @Test
    @Order(4)
    @DisplayName("Test E (MySQL thật): Login tạo phiên, ép het_han_luc < NOW(), gọi protected URL qua filter -> MUST từ chối truy cập và chuyển hướng login?timeout=1")
    void testE_RealMySQLEndToEnd_EpQuaHan_FilterTuChoiVaChuyenHuongLoginTimeout() throws Exception {
        Assumptions.assumeTrue(dbAvailable, "Bỏ qua nếu MySQL không khả dụng");

        String eSessionRaw = "test-e-session-" + System.currentTimeMillis();
        String eMaPhienHash = PasswordUtil.sha256Hex(eSessionRaw);

        // 1. Tạo phiên đăng nhập ban đầu còn hiệu lực
        boolean taoOk = phienDangNhapDAO.taoPhien(testUserId, eMaPhienHash, 1, "127.0.0.1", "ChromeTest", 30);
        assertTrue(taoOk, "Phải tạo được phiên trong DB");

        // 2. Ép het_han_luc < NOW() (quá hạn 2 phút giống hệt lỗi QA ghi nhận: 18:47:40 vs 18:49:51)
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE phien_dang_nhap SET het_han_luc = DATE_SUB(NOW(), INTERVAL 2 MINUTE), trang_thai = 'HOAT_DONG' " +
                     "WHERE ma_phien_hash = ?")) {
            ps.setString(1, eMaPhienHash);
            int updated = ps.executeUpdate();
            assertEquals(1, updated, "Phải cập nhật được het_han_luc về quá khứ");
        }

        // Lấy lại het_han_luc trước khi filter chạy
        Timestamp hetHanTruocFilter = phienDangNhapDAO.layThoiGianHetHan(eMaPhienHash);
        assertNotNull(hetHanTruocFilter);

        // 3. Khởi tạo AuthFilter và mock Servlet context
        vn.nhom10.crm.filter.AuthFilter filter = new vn.nhom10.crm.filter.AuthFilter();
        filter.setPhienDangNhapDAO(phienDangNhapDAO);

        jakarta.servlet.http.HttpServletRequest mockReq = org.mockito.Mockito.mock(jakarta.servlet.http.HttpServletRequest.class);
        jakarta.servlet.http.HttpServletResponse mockResp = org.mockito.Mockito.mock(jakarta.servlet.http.HttpServletResponse.class);
        jakarta.servlet.http.HttpSession mockSession = org.mockito.Mockito.mock(jakarta.servlet.http.HttpSession.class);
        jakarta.servlet.FilterChain mockChain = org.mockito.Mockito.mock(jakarta.servlet.FilterChain.class);

        NguoiDung testUser = new NguoiDung();
        testUser.setId(testUserId);
        testUser.setEmail(TEST_EMAIL);

        org.mockito.Mockito.when(mockReq.getContextPath()).thenReturn("/crm-ban-hang");
        org.mockito.Mockito.when(mockReq.getRequestURI()).thenReturn("/crm-ban-hang/home");
        org.mockito.Mockito.when(mockReq.getSession(false)).thenReturn(mockSession);
        org.mockito.Mockito.when(mockSession.getAttribute("user")).thenReturn(testUser);
        org.mockito.Mockito.when(mockSession.getAttribute("maPhienHash")).thenReturn(eMaPhienHash);

        // 4. Gọi protected URL qua AuthFilter
        filter.doFilter(mockReq, mockResp, mockChain);

        // 5. Xác nhận kết quả:
        // - Request bị từ chối: KHÔNG chain.doFilter (không vào được /home)
        org.mockito.Mockito.verify(mockChain, org.mockito.Mockito.never()).doFilter(mockReq, mockResp);
        // - Invalidate session trên container
        org.mockito.Mockito.verify(mockSession).invalidate();
        // - Chuyển hướng tới login?timeout=1
        org.mockito.Mockito.verify(mockResp).sendRedirect("/crm-ban-hang/login?timeout=1");

        // - Xác nhận trong MySQL: trạng thái chuyển sang HET_HAN
        String trangThaiMoi = phienDangNhapDAO.layTrangThaiPhien(eMaPhienHash);
        assertEquals("HET_HAN", trangThaiMoi, "Trạng thái trong DB phải được cập nhật sang HET_HAN");

        // - Xác nhận het_han_luc KHÔNG bị gia hạn
        Timestamp hetHanSauFilter = phienDangNhapDAO.layThoiGianHetHan(eMaPhienHash);
        assertNotNull(hetHanSauFilter);
        assertEquals(hetHanTruocFilter.getTime(), hetHanSauFilter.getTime(), "Thời gian het_han_luc KHÔNG được gia hạn");
    }
}
