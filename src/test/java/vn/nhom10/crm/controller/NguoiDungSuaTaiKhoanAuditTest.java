package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dao.NhomKinhDoanhDAO;
import vn.nhom10.crm.dao.VaiTroDAO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.service.EmailService;
import vn.nhom10.crm.service.NguoiDungService;
import vn.nhom10.crm.service.NhatKyThayDoiService;
import vn.nhom10.crm.service.PhanQuyenService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Kiểm thử tích hợp Audit Log khi cập nhật vai trò người dùng (Story S2-04 & S1-09)")
class NguoiDungSuaTaiKhoanAuditTest {

    private Connection connection;
    private NguoiDungService nguoiDungService;
    private PhanQuyenService phanQuyenService;
    private NguoiDungServlet nguoiDungServlet;
    private PhanQuyenServlet phanQuyenServlet;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher dispatcher;

    private NguoiDung adminUser;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:test_crm_audit_integration;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection("jdbc:h2:mem:test_crm_audit_integration;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        try (Statement st = connection.createStatement()) {
            st.execute("DROP ALL OBJECTS");

            st.execute("CREATE TABLE nhom_kinh_doanh (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_nhom VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_nhom VARCHAR(255) NOT NULL, " +
                    "mo_ta TEXT, " +
                    "nhom_cha_id INT" +
                    ")");

            st.execute("CREATE TABLE vai_tro (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_vai_tro VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_vai_tro VARCHAR(100) NOT NULL, " +
                    "mo_ta VARCHAR(255), " +
                    "pham_vi_toi_da VARCHAR(50) DEFAULT 'CA_NHAN'" +
                    ")");

            st.execute("CREATE TABLE nguoi_dung (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(150) NOT NULL, " +
                    "email VARCHAR(200) NOT NULL UNIQUE, " +
                    "mat_khau VARCHAR(255) NOT NULL, " +
                    "so_dien_thoai VARCHAR(20), " +
                    "chu_ky_email TEXT, " +
                    "trang_thai VARCHAR(30) DEFAULT 'HOAT_DONG', " +
                    "so_lan_sai INT DEFAULT 0, " +
                    "thoi_gian_khoa TIMESTAMP NULL, " +
                    "nhom_kinh_doanh_id INT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            st.execute("CREATE TABLE nguoi_dung_vai_tro (" +
                    "nguoi_dung_id INT NOT NULL, " +
                    "vai_tro_id INT NOT NULL, " +
                    "PRIMARY KEY (nguoi_dung_id, vai_tro_id)" +
                    ")");

            st.execute("CREATE TABLE nhat_ky_he_thong (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "nguoi_thuc_hien_id BIGINT NULL, " +
                    "hanh_dong VARCHAR(80) NOT NULL, " +
                    "loai_doi_tuong VARCHAR(80) NOT NULL, " +
                    "doi_tuong_id BIGINT NULL, " +
                    "gia_tri_truoc_json VARCHAR(2000) NULL, " +
                    "gia_tri_sau_json VARCHAR(2000) NULL, " +
                    "ly_do VARCHAR(1000) NULL, " +
                    "dia_chi_ip VARCHAR(45) NULL, " +
                    "thong_tin_thiet_bi VARCHAR(500) NULL, " +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // Seed Nhóm
            st.execute("INSERT INTO nhom_kinh_doanh (id, ma_nhom, ten_nhom) VALUES (1, 'KD_TONG', 'Khối Kinh Doanh')");
            st.execute("INSERT INTO nhom_kinh_doanh (id, ma_nhom, ten_nhom) VALUES (2, 'KD_BAC', 'Nhóm Miền Bắc')");

            // Seed Vai trò
            st.execute("INSERT INTO vai_tro (id, ma_vai_tro, ten_vai_tro, pham_vi_toi_da) VALUES (1, 'ADMIN', 'Quản trị hệ thống', 'TOAN_BO')");
            st.execute("INSERT INTO vai_tro (id, ma_vai_tro, ten_vai_tro, pham_vi_toi_da) VALUES (3, 'TEAM_LEAD', 'Trưởng nhóm kinh doanh', 'NHOM')");
            st.execute("INSERT INTO vai_tro (id, ma_vai_tro, ten_vai_tro, pham_vi_toi_da) VALUES (4, 'SALES_REP', 'Nhân viên kinh doanh', 'CA_NHAN')");
            st.execute("INSERT INTO vai_tro (id, ma_vai_tro, ten_vai_tro, pham_vi_toi_da) VALUES (5, 'MARKETING', 'Nhân viên Marketing', 'CA_NHAN')");

            // Seed Admin (ID 1)
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email, mat_khau, trang_thai, nhom_kinh_doanh_id) " +
                    "VALUES (1, 'Quản Trị Viên', 'admin@crm.vn', 'hash_admin_secret', 'HOAT_DONG', 1)");
            st.execute("INSERT INTO nguoi_dung_vai_tro (nguoi_dung_id, vai_tro_id) VALUES (1, 1)");

            // Seed Target User s201_dup@crm.vn (ID 10) với Role SALES_REP (4)
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email, mat_khau, trang_thai, nhom_kinh_doanh_id) " +
                    "VALUES (10, 'Nguyễn Văn Kinh Doanh', 's201_dup@crm.vn', 'hash_user_secret', 'HOAT_DONG', 1)");
            st.execute("INSERT INTO nguoi_dung_vai_tro (nguoi_dung_id, vai_tro_id) VALUES (10, 4)");
        }

        VaiTroDAO vaiTroDAO = new VaiTroDAO();
        NhomKinhDoanhDAO nhomKinhDoanhDAO = new NhomKinhDoanhDAO();
        NguoiDungDAO nguoiDungDAO = new NguoiDungDAO(vaiTroDAO, nhomKinhDoanhDAO);
        EmailService emailService = mock(EmailService.class);
        NhatKyThayDoiService nhatKyService = new NhatKyThayDoiService();

        nguoiDungService = new NguoiDungService(nguoiDungDAO, vaiTroDAO, nhomKinhDoanhDAO, emailService, nhatKyService);
        phanQuyenService = new PhanQuyenService(nguoiDungDAO, vaiTroDAO, nhomKinhDoanhDAO, nhatKyService);

        nguoiDungServlet = new NguoiDungServlet(nguoiDungService);
        phanQuyenServlet = new PhanQuyenServlet();

        adminUser = new NguoiDung(1, "Quản Trị Viên", "admin@crm.vn");
        adminUser.themVaiTro(new VaiTro(1, "ADMIN", "Quản trị hệ thống", "Admin"));
    }

    @AfterEach
    void tearDown() throws Exception {
        DatabaseConfig.resetConnectionSupplier();
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @DisplayName("POST /nguoi-dung/sua: Thay đổi role từ SALES_REP sang SALES_REP + MARKETING ghi đúng 1 audit log")
    void testSuaTaiKhoan_ThayDoiRole_GhiDung1AuditRow() throws Exception {
        when(request.getServletPath()).thenReturn("/nguoi-dung/sua");
        when(request.getParameter("id")).thenReturn("10");
        when(request.getParameter("hoTen")).thenReturn("Nguyễn Văn Kinh Doanh");
        when(request.getParameter("email")).thenReturn("s201_dup@crm.vn");
        when(request.getParameter("nhomId")).thenReturn("1");
        when(request.getParameter("trangThai")).thenReturn("HOAT_DONG");
        when(request.getParameter("soDienThoai")).thenReturn("0912345678");
        when(request.getParameterValues("vaiTroIds")).thenReturn(new String[]{"4", "5"}); // SALES_REP + MARKETING
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(adminUser);
        when(request.getRemoteAddr()).thenReturn("192.168.1.88");
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 CRM-Test-Client");

        nguoiDungServlet.doPost(request, response);

        verify(response).sendRedirect("/crm/nguoi-dung");

        // 1. Kiểm tra bảng nguoi_dung_vai_tro đã cập nhật thành công (Role 4 + 5)
        NguoiDungDAO dao = new NguoiDungDAO();
        Set<VaiTro> roles = dao.layDanhSachVaiTroTheoNguoiDungId(10);
        assertEquals(2, roles.size(), "User phải có đủ 2 vai trò");
        assertTrue(roles.stream().anyMatch(r -> "SALES_REP".equals(r.getMaVaiTro())));
        assertTrue(roles.stream().anyMatch(r -> "MARKETING".equals(r.getMaVaiTro())));

        // 2. Kiểm tra nhat_ky_he_thong có ĐÚNG 1 bản ghi
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM nhat_ky_he_thong")) {

            assertTrue(rs.next(), "Phải có ít nhất 1 bản ghi audit log");
            assertEquals(1L, rs.getLong("nguoi_thuc_hien_id"), "Actor phải là Admin (ID 1)");
            assertEquals(10L, rs.getLong("doi_tuong_id"), "Target user phải là ID 10");
            assertEquals("VAI_TRO_NGUOI_DUNG", rs.getString("loai_doi_tuong"));
            assertEquals("CAP_NHAT", rs.getString("hanh_dong"));

            String giaTriTruoc = rs.getString("gia_tri_truoc_json");
            String giaTriSau = rs.getString("gia_tri_sau_json");

            assertNotNull(giaTriTruoc);
            assertNotNull(giaTriSau);
            assertTrue(giaTriTruoc.contains("SALES_REP"), "Before phải chứa SALES_REP");
            assertFalse(giaTriTruoc.contains("MARKETING"), "Before không được có MARKETING");

            assertTrue(giaTriSau.contains("SALES_REP"), "After phải có SALES_REP");
            assertTrue(giaTriSau.contains("MARKETING"), "After phải có MARKETING");

            assertEquals("192.168.1.88", rs.getString("dia_chi_ip"));
            assertEquals("Mozilla/5.0 CRM-Test-Client", rs.getString("thong_tin_thiet_bi"));

            assertFalse(rs.next(), "Tuyệt đối không được có bản ghi audit thứ 2 (không duplicate)");
        }
    }

    @Test
    @DisplayName("A. Role không đổi: Sửa thông tin nhưng giữ nguyên vai trò -> 0 audit row mới")
    void testSuaTaiKhoan_RoleKhongDoi_KhongGhiAudit() throws Exception {
        when(request.getServletPath()).thenReturn("/nguoi-dung/sua");
        when(request.getParameter("id")).thenReturn("10");
        when(request.getParameter("hoTen")).thenReturn("Nguyễn Văn Kinh Doanh Đã Đổi Tên");
        when(request.getParameter("email")).thenReturn("s201_dup@crm.vn");
        when(request.getParameter("nhomId")).thenReturn("1");
        when(request.getParameter("trangThai")).thenReturn("HOAT_DONG");
        when(request.getParameterValues("vaiTroIds")).thenReturn(new String[]{"4"}); // Vẫn là SALES_REP (4)
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(adminUser);
        when(request.getRemoteAddr()).thenReturn("192.168.1.88");
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");

        nguoiDungServlet.doPost(request, response);

        verify(response).sendRedirect("/crm/nguoi-dung");

        // Xác nhận số bản ghi audit vẫn là 0
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM nhat_ky_he_thong")) {
            assertTrue(rs.next());
            assertEquals(0, rs.getInt(1), "Khi role không đổi, số lượng audit log phải là 0");
        }
    }

    @Test
    @DisplayName("A2. Cùng tập role nhưng khác thứ tự checkbox -> Không ghi audit")
    void testSuaTaiKhoan_CungTapRoleKhacThuTu_KhongGhiAudit() throws Exception {
        // Cho user 10 có sẵn 2 role: 4 (SALES_REP) và 5 (MARKETING)
        try (Statement st = connection.createStatement()) {
            st.execute("INSERT INTO nguoi_dung_vai_tro (nguoi_dung_id, vai_tro_id) VALUES (10, 5)");
        }

        when(request.getServletPath()).thenReturn("/nguoi-dung/sua");
        when(request.getParameter("id")).thenReturn("10");
        when(request.getParameter("hoTen")).thenReturn("Nguyễn Văn Kinh Doanh");
        when(request.getParameter("email")).thenReturn("s201_dup@crm.vn");
        when(request.getParameter("nhomId")).thenReturn("1");
        when(request.getParameter("trangThai")).thenReturn("HOAT_DONG");
        // Checkbox gửi lên thứ tự ngược lại: 5 trước, 4 sau
        when(request.getParameterValues("vaiTroIds")).thenReturn(new String[]{"5", "4"});
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(adminUser);

        nguoiDungServlet.doPost(request, response);

        verify(response).sendRedirect("/crm/nguoi-dung");

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM nhat_ky_he_thong")) {
            assertTrue(rs.next());
            assertEquals(0, rs.getInt(1), "Cùng tập role dù khác thứ tự checkbox thì không được ghi audit");
        }
    }

    @Test
    @DisplayName("B. Audit insert fail -> Toàn bộ role update rollback, trả lỗi (Fail-Closed)")
    void testSuaTaiKhoan_AuditInsertFail_RollbackRoleMutation() throws Exception {
        // Làm cho bảng nhat_ky_he_thong gặp lỗi khi insert (ví dụ xóa bảng)
        try (Statement st = connection.createStatement()) {
            st.execute("DROP TABLE nhat_ky_he_thong");
        }

        when(request.getServletPath()).thenReturn("/nguoi-dung/sua");
        when(request.getParameter("id")).thenReturn("10");
        when(request.getParameter("hoTen")).thenReturn("Nguyễn Văn Kinh Doanh");
        when(request.getParameter("email")).thenReturn("s201_dup@crm.vn");
        when(request.getParameter("nhomId")).thenReturn("1");
        when(request.getParameter("trangThai")).thenReturn("HOAT_DONG");
        when(request.getParameterValues("vaiTroIds")).thenReturn(new String[]{"4", "5"}); // Cố gắng thêm MARKETING
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(adminUser);
        when(request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/sua-tai-khoan.jsp")).thenReturn(dispatcher);

        nguoiDungServlet.doPost(request, response);

        // Response không được redirect thành công, mà phải forward lại trang lỗi
        verify(response, never()).sendRedirect("/crm/nguoi-dung");
        verify(dispatcher).forward(request, response);

        // Kiểm tra cơ sở dữ liệu: Role của user 10 KHÔNG ĐƯỢC thêm MARKETING, vẫn giữ nguyên role cũ (chỉ có 4)
        NguoiDungDAO dao = new NguoiDungDAO();
        Set<VaiTro> roles = dao.layDanhSachVaiTroTheoNguoiDungId(10);
        assertEquals(1, roles.size(), "Do audit fail, role mutation phải được ROLLBACK 100%");
        assertTrue(roles.stream().anyMatch(r -> "SALES_REP".equals(r.getMaVaiTro())));
        assertFalse(roles.stream().anyMatch(r -> "MARKETING".equals(r.getMaVaiTro())));
    }

    @Test
    @DisplayName("C. /nguoi-dung/phan-quyen thay role: Ghi đúng 1 audit row, không duplicate")
    void testPhanQuyen_ThayDoiRole_GhiDung1AuditRowKhongDuplicate() throws Exception {
        // Gán vai trò qua PhanQuyenService
        var result = phanQuyenService.ganVaiTroVaNhomKinhDoanh(
                10, java.util.List.of(4, 5), 1, 1, "10.0.0.1", "Chrome");

        assertTrue(result.isThanhCong());

        // Kiểm tra audit log có đúng 1 bản ghi
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM nhat_ky_he_thong")) {

            assertTrue(rs.next());
            assertEquals(1L, rs.getLong("nguoi_thuc_hien_id"));
            assertEquals(10L, rs.getLong("doi_tuong_id"));
            assertEquals("VAI_TRO_NGUOI_DUNG", rs.getString("loai_doi_tuong"));
            assertTrue(rs.getString("gia_tri_truoc_json").contains("SALES_REP"));
            assertTrue(rs.getString("gia_tri_sau_json").contains("MARKETING"));

            assertFalse(rs.next(), "Không được duplicate audit log");
        }
    }

    @Test
    @DisplayName("D. Before/After trong audit log không chứa password, token, credential")
    void testAuditLog_KhongChuaPasswordTokenCredential() throws Exception {
        when(request.getServletPath()).thenReturn("/nguoi-dung/sua");
        when(request.getParameter("id")).thenReturn("10");
        when(request.getParameter("hoTen")).thenReturn("Nguyễn Văn Kinh Doanh");
        when(request.getParameter("email")).thenReturn("s201_dup@crm.vn");
        when(request.getParameter("nhomId")).thenReturn("1");
        when(request.getParameter("trangThai")).thenReturn("HOAT_DONG");
        when(request.getParameterValues("vaiTroIds")).thenReturn(new String[]{"4", "5"});
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(adminUser);

        nguoiDungServlet.doPost(request, response);

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM nhat_ky_he_thong")) {
            assertTrue(rs.next());
            String truoc = rs.getString("gia_tri_truoc_json");
            String sau = rs.getString("gia_tri_sau_json");

            assertFalse(truoc.contains("hash_user_secret"));
            assertFalse(truoc.contains("password"));
            assertFalse(truoc.contains("token"));

            assertFalse(sau.contains("hash_user_secret"));
            assertFalse(sau.contains("password"));
            assertFalse(sau.contains("token"));
        }
    }
}
