package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dao.NhatKyThayDoiDAO;
import vn.nhom10.crm.dto.BoLocNhatKyDTO;
import vn.nhom10.crm.dto.KetQuaPhanTrangDTO;
import vn.nhom10.crm.dto.NhatKyThayDoiDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.service.NguoiDungService;
import vn.nhom10.crm.service.NhatKyThayDoiService;
import vn.nhom10.crm.service.PhanQuyenService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử Khởi tạo/Startup/Re-deploy không tự sinh Audit Log giả (Story S2-04)")
class NhatKyThayDoiStartupTest {

    private Connection connection;
    private AutoCloseable closeable;

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
        closeable = MockitoAnnotations.openMocks(this);
        connection = DriverManager.getConnection("jdbc:h2:mem:test_crm_audit_startup;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection("jdbc:h2:mem:test_crm_audit_startup;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        try (Statement st = connection.createStatement()) {
            st.execute("DROP ALL OBJECTS");

            st.execute("CREATE TABLE nguoi_dung (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(150) NOT NULL, " +
                    "email VARCHAR(200) NOT NULL UNIQUE, " +
                    "mat_khau VARCHAR(255) NOT NULL, " +
                    "trang_thai VARCHAR(30) DEFAULT 'HOAT_DONG'" +
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

            st.execute("CREATE TABLE vai_tro (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_vai_tro VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_vai_tro VARCHAR(100) NOT NULL, " +
                    "mo_ta VARCHAR(255) NULL" +
                    ")");

            st.execute("CREATE TABLE nguoi_dung_vai_tro (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "nguoi_dung_id BIGINT NOT NULL, " +
                    "vai_tro_id BIGINT NOT NULL" +
                    ")");

            // Seed Roles
            st.execute("INSERT INTO vai_tro (id, ma_vai_tro, ten_vai_tro) VALUES " +
                    "(1, 'ADMIN', 'Quản trị hệ thống'), " +
                    "(2, 'SALES_REP', 'Nhân viên kinh doanh'), " +
                    "(3, 'MARKETING', 'Nhân viên Marketing')");

            // Seed Admin (ID 1) có 1 vai trò Admin
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email, mat_khau, trang_thai) " +
                    "VALUES (1, 'Quản Trị Viên', 'admin@crm.vn', 'hash_admin', 'HOAT_DONG')");
            st.execute("INSERT INTO nguoi_dung_vai_tro (nguoi_dung_id, vai_tro_id) VALUES (1, 1)");

            // Seed Target User s201_dup@crm.vn (ID 12) có 2 vai trò: SALES_REP + MARKETING
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email, mat_khau, trang_thai) " +
                    "VALUES (12, 'Nguyễn Văn Kinh Doanh', 's201_dup@crm.vn', 'hash_user', 'HOAT_DONG')");
            st.execute("INSERT INTO nguoi_dung_vai_tro (nguoi_dung_id, vai_tro_id) VALUES (12, 2), (12, 3)");
        }

        adminUser = new NguoiDung(1, "Quản Trị Viên", "admin@crm.vn");
        adminUser.themVaiTro(new VaiTro(1, "ADMIN", "Quản trị hệ thống", "Admin"));
    }

    @AfterEach
    void tearDown() throws Exception {
        DatabaseConfig.resetConnectionSupplier();
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        if (closeable != null) {
            closeable.close();
        }
    }

    private long demSoBanGhiAudit() throws SQLException {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM nhat_ky_he_thong")) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0;
    }

    @Test
    @DisplayName("DB có 1 audit row thật -> Khởi tạo lại các Service/Servlet -> COUNT vẫn đúng = 1")
    void testKhoiTaoService_KhiDbCo1Row_CountVanBang1() throws Exception {
        // Given: DB có sẵn đúng 1 row audit thật
        try (Statement st = connection.createStatement()) {
            st.execute("INSERT INTO nhat_ky_he_thong (id, nguoi_thuc_hien_id, hanh_dong, loai_doi_tuong, doi_tuong_id, " +
                    "gia_tri_truoc_json, gia_tri_sau_json, ly_do, dia_chi_ip, thong_tin_thiet_bi) " +
                    "VALUES (1, 1, 'CAP_NHAT', 'VAI_TRO_NGUOI_DUNG', 12, '[\"SALES_REP\"]', '[\"SALES_REP\",\"MARKETING\"]', " +
                    "'Cập nhật vai trò tài khoản', '127.0.0.1', 'Chrome')");
        }
        assertEquals(1, demSoBanGhiAudit(), "Ban đầu phải có đúng 1 audit row");

        // When: Khởi tạo lại tất cả component tương ứng khi app startup / redeploy
        NhatKyThayDoiService service1 = new NhatKyThayDoiService();
        NhatKyThayDoiServlet servlet1 = new NhatKyThayDoiServlet();
        PhanQuyenServlet servlet2 = new PhanQuyenServlet();
        NguoiDungServlet servlet3 = new NguoiDungServlet();
        NguoiDungService service2 = new NguoiDungService();
        PhanQuyenService service3 = new PhanQuyenService();

        // Then: COUNT audit vẫn = 1, tuyệt đối không tạo thêm row
        assertEquals(1, demSoBanGhiAudit(), "Sau khi khởi tạo lại service/servlet, count audit vẫn phải = 1");
    }

    @Test
    @DisplayName("DB audit trống (0 rows) -> Khởi tạo lại Service/Servlet -> COUNT vẫn đúng = 0")
    void testKhoiTaoService_KhiDbTrong_CountVanBang0() throws Exception {
        assertEquals(0, demSoBanGhiAudit(), "Ban đầu DB audit trống = 0");

        // When: Khởi tạo lại
        new NhatKyThayDoiService();
        new NhatKyThayDoiServlet();
        new NguoiDungService();
        new PhanQuyenService();

        // Then: COUNT audit vẫn = 0
        assertEquals(0, demSoBanGhiAudit(), "Không được auto-seed bất kỳ log giả nào khi DB trống");
    }

    @Test
    @DisplayName("Khởi tạo NhatKyThayDoiService nhiều lần -> Tuyệt đối không INSERT thêm bản ghi")
    void testKhoiTaoNhatKyServiceNhieuLan_KhongInsert() throws Exception {
        try (Statement st = connection.createStatement()) {
            st.execute("INSERT INTO nhat_ky_he_thong (id, nguoi_thuc_hien_id, hanh_dong, loai_doi_tuong, doi_tuong_id) " +
                    "VALUES (100, 1, 'CAP_NHAT', 'CHIET_KHAU', 5)");
        }
        assertEquals(1, demSoBanGhiAudit());

        // Khởi tạo lặp lại 10 lần
        for (int i = 0; i < 10; i++) {
            new NhatKyThayDoiService();
            new NhatKyThayDoiDAO();
        }

        assertEquals(1, demSoBanGhiAudit(), "Sau 10 lần khởi tạo, count vẫn phải = 1");
    }

    @Test
    @DisplayName("GET /nhat-ky-thay-doi nhiều lần -> Tuyệt đối không side-effect INSERT")
    void testDoGetNhatKyNhieuLan_KhongInsert() throws Exception {
        try (Statement st = connection.createStatement()) {
            st.execute("INSERT INTO nhat_ky_he_thong (id, nguoi_thuc_hien_id, hanh_dong, loai_doi_tuong, doi_tuong_id) " +
                    "VALUES (1, 1, 'CAP_NHAT', 'VAI_TRO_NGUOI_DUNG', 12)");
        }
        assertEquals(1, demSoBanGhiAudit());

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(adminUser);
        when(request.getServletPath()).thenReturn("/nhat-ky-thay-doi");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        NhatKyThayDoiServlet servlet = new NhatKyThayDoiServlet(new NhatKyThayDoiService());

        // Gọi doGet 5 lần (cùng package nên truy cập doGet hợp lệ)
        for (int i = 0; i < 5; i++) {
            servlet.doGet(request, response);
        }

        assertEquals(1, demSoBanGhiAudit(), "Sau 5 lần GET /nhat-ky-thay-doi, count vẫn phải = 1");
    }

    @Test
    @DisplayName("Bonus UI: Target mapping cho VAI_TRO_NGUOI_DUNG hiển thị đúng tên và email user, không hiện null")
    void testTargetMapping_KhongHienNull_HienDungTenVaEmail() throws Exception {
        try (Statement st = connection.createStatement()) {
            st.execute("INSERT INTO nhat_ky_he_thong (id, nguoi_thuc_hien_id, hanh_dong, loai_doi_tuong, doi_tuong_id, " +
                    "gia_tri_truoc_json, gia_tri_sau_json, ly_do) " +
                    "VALUES (1, 1, 'CAP_NHAT', 'VAI_TRO_NGUOI_DUNG', 12, '[\"SALES_REP\"]', '[\"SALES_REP\",\"MARKETING\"]', 'Sửa vai trò')");
        }

        NhatKyThayDoiDAO dao = new NhatKyThayDoiDAO();
        KetQuaPhanTrangDTO<NhatKyThayDoiDTO> kq = new NhatKyThayDoiService(dao).timKiemNhatKy(new BoLocNhatKyDTO());

        assertNotNull(kq);
        assertEquals(1, kq.getDanhSach().size());

        NhatKyThayDoiDTO item = kq.getDanhSach().get(0);
        // Tên đối tượng phải lấy từ bảng nguoi_dung (Nguyễn Văn Kinh Doanh), không được null
        assertEquals("Nguyễn Văn Kinh Doanh", item.getTenDoiTuong());
        assertNotNull(item.getTenDoiTuong());
        assertNotEquals("null", item.getTenDoiTuong());

        // Mã đối tượng phải là email người dùng đích (s201_dup@crm.vn)
        assertEquals("s201_dup@crm.vn", item.getMaDoiTuong());
        assertNotNull(item.getMaDoiTuong());
        assertNotEquals("null", item.getMaDoiTuong());
    }

    @Test
    @DisplayName("Actor Role: Hiển thị đúng vai trò đơn (Admin 1 role) - Quản trị hệ thống")
    void testVaiTroNguoiThucHien_DonVaiTro_Admin() throws Exception {
        try (Statement st = connection.createStatement()) {
            st.execute("INSERT INTO nhat_ky_he_thong (id, nguoi_thuc_hien_id, hanh_dong, loai_doi_tuong, doi_tuong_id) " +
                    "VALUES (1, 1, 'CAP_NHAT', 'CHIET_KHAU', 10)");
        }

        NhatKyThayDoiDAO dao = new NhatKyThayDoiDAO();
        KetQuaPhanTrangDTO<NhatKyThayDoiDTO> kq = new NhatKyThayDoiService(dao).timKiemNhatKy(new BoLocNhatKyDTO());

        assertNotNull(kq);
        assertEquals(1, kq.getDanhSach().size());
        NhatKyThayDoiDTO item = kq.getDanhSach().get(0);
        assertEquals("Quản trị hệ thống", item.getVaiTroNguoiThucHien());
        assertNotEquals("null", item.getVaiTroNguoiThucHien());
    }

    @Test
    @DisplayName("Actor Role: Hiển thị đầy đủ vai trò người dùng giữ nhiều role, không duplicate row")
    void testVaiTroNguoiThucHien_DaVaiTro_KhongDuplicateRow() throws Exception {
        try (Statement st = connection.createStatement()) {
            st.execute("INSERT INTO nhat_ky_he_thong (id, nguoi_thuc_hien_id, hanh_dong, loai_doi_tuong, doi_tuong_id) " +
                    "VALUES (2, 12, 'CAP_NHAT', 'CHI_TIEU', 20)");
        }

        NhatKyThayDoiDAO dao = new NhatKyThayDoiDAO();
        KetQuaPhanTrangDTO<NhatKyThayDoiDTO> kq = new NhatKyThayDoiService(dao).timKiemNhatKy(new BoLocNhatKyDTO());

        assertNotNull(kq);
        // Mỗi bản ghi nhật ký chỉ xuất hiện đúng 1 row, không bị duplicate do multi-role
        assertEquals(1, kq.getDanhSach().size());
        NhatKyThayDoiDTO item = kq.getDanhSach().get(0);
        assertTrue(item.getVaiTroNguoiThucHien().contains("Nhân viên kinh doanh"));
        assertTrue(item.getVaiTroNguoiThucHien().contains("Nhân viên Marketing"));
        assertEquals("Nhân viên kinh doanh, Nhân viên Marketing", item.getVaiTroNguoiThucHien());
    }

    @Test
    @DisplayName("Actor Role: Khi nguoi_thuc_hien_id là NULL, UI hiển thị fallback 'Không xác định', không render literal null")
    void testVaiTroNguoiThucHien_ActorNull_FallbackKhongXacDinh() throws Exception {
        try (Statement st = connection.createStatement()) {
            st.execute("INSERT INTO nhat_ky_he_thong (id, nguoi_thuc_hien_id, hanh_dong, loai_doi_tuong, doi_tuong_id) " +
                    "VALUES (3, NULL, 'CAP_NHAT', 'CHI_TIEU', 30)");
        }

        NhatKyThayDoiDAO dao = new NhatKyThayDoiDAO();
        KetQuaPhanTrangDTO<NhatKyThayDoiDTO> kq = new NhatKyThayDoiService(dao).timKiemNhatKy(new BoLocNhatKyDTO());

        assertNotNull(kq);
        assertEquals(1, kq.getDanhSach().size());
        NhatKyThayDoiDTO item = kq.getDanhSach().get(0);
        assertEquals("Không xác định", item.getVaiTroNguoiThucHien());
        assertNotEquals("null", item.getVaiTroNguoiThucHien());
        assertEquals("Hệ thống", item.getTenNguoiThucHien());
        assertEquals("-", item.getEmailNguoiThucHien());
    }
}
