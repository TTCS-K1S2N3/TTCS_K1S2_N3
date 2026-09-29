package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTro;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NguoiDungDAOTest {

    private static final String H2_URL = "jdbc:h2:mem:crm_test_db;DB_CLOSE_DELAY=-1;MODE=MySQL";
    private static Connection keepAliveConnection;
    private NguoiDungDAO nguoiDungDAO;

    @BeforeAll
    static void initDatabase() throws Exception {
        keepAliveConnection = DriverManager.getConnection(H2_URL);
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection(H2_URL);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        try (Connection conn = DriverManager.getConnection(H2_URL);
             Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE vai_tro ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "ma_vai_tro VARCHAR(50) NOT NULL UNIQUE, "
                    + "ten_vai_tro VARCHAR(100) NOT NULL, "
                    + "mo_ta VARCHAR(255) NULL)");

            st.execute("CREATE TABLE nguoi_dung ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "ho_ten VARCHAR(150) NOT NULL, "
                    + "email VARCHAR(200) NOT NULL UNIQUE, "
                    + "mat_khau VARCHAR(255) NOT NULL, "
                    + "so_dien_thoai VARCHAR(20) NULL, "
                    + "trang_thai VARCHAR(30) NOT NULL DEFAULT 'HOAT_DONG', "
                    + "so_lan_sai INT NOT NULL DEFAULT 0, "
                    + "thoi_gian_khoa TIMESTAMP NULL, "
                    + "nhom_kinh_doanh_id INT NULL, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE nguoi_dung_vai_tro ("
                    + "nguoi_dung_id BIGINT NOT NULL, "
                    + "vai_tro_id INT NOT NULL, "
                    + "PRIMARY KEY (nguoi_dung_id, vai_tro_id))");
        }
    }

    @AfterAll
    static void tearDown() throws Exception {
        DatabaseConfig.resetConnectionSupplier();
        if (keepAliveConnection != null && !keepAliveConnection.isClosed()) {
            keepAliveConnection.close();
        }
    }

    @BeforeEach
    void setUpData() throws Exception {
        nguoiDungDAO = new NguoiDungDAO();
        try (Connection conn = DriverManager.getConnection(H2_URL);
             Statement st = conn.createStatement()) {
            st.execute("DELETE FROM nguoi_dung_vai_tro");
            st.execute("DELETE FROM nguoi_dung");
            st.execute("DELETE FROM vai_tro");

            st.execute("INSERT INTO vai_tro (id, ma_vai_tro, ten_vai_tro, mo_ta) "
                    + "VALUES (1, 'SALES_REP', 'Nhân viên kinh doanh', 'Bán hàng')");

            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email, mat_khau, so_dien_thoai, trang_thai, so_lan_sai, thoi_gian_khoa) "
                    + "VALUES (1, 'Phan Duy Hưng', 'hung@crm.vn', '$2a$12$dummyhash', '0901234567', 'HOAT_DONG', 0, NULL)");

            st.execute("INSERT INTO nguoi_dung_vai_tro (nguoi_dung_id, vai_tro_id) VALUES (1, 1)");
        }
    }

    @Test
    @DisplayName("DAO: Tìm người dùng theo email và nạp danh sách vai trò")
    void testTimTheoEmail() {
        NguoiDung user = nguoiDungDAO.timTheoEmail("HUNG@CRM.VN");

        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("hung@crm.vn", user.getEmail());
        assertEquals(0, user.getSoLanSai());
        assertNull(user.getThoiGianKhoa());

        Set<VaiTro> vaiTros = user.getDanhSachVaiTro();
        assertEquals(1, vaiTros.size());
        assertTrue(user.coVaiTro("SALES_REP"));
    }

    @Test
    @DisplayName("DAO: Tăng số lần sai")
    void testTangSoLanSai() {
        nguoiDungDAO.tangSoLanSai(1L);
        NguoiDung user = nguoiDungDAO.timTheoId(1L);
        assertEquals(1, user.getSoLanSai());

        nguoiDungDAO.tangSoLanSai(1L);
        user = nguoiDungDAO.timTheoId(1L);
        assertEquals(2, user.getSoLanSai());
    }

    @Test
    @DisplayName("DAO: Khóa tạm 15 phút")
    void testKhoaTam() {
        nguoiDungDAO.khoaTam(1L, 15);

        NguoiDung user = nguoiDungDAO.timTheoId(1L);
        assertEquals(5, user.getSoLanSai());
        assertNotNull(user.getThoiGianKhoa());
        assertTrue(user.coBiKhoaTam());
        assertTrue(user.getSoPhutKhoaConLai() > 0);
    }

    @Test
    @DisplayName("DAO: Reset số lần sai và thời gian khóa tạm")
    void testResetSoLanSai() {
        nguoiDungDAO.khoaTam(1L, 15);
        nguoiDungDAO.resetSoLanSai(1L);

        NguoiDung user = nguoiDungDAO.timTheoId(1L);
        assertEquals(0, user.getSoLanSai());
        assertNull(user.getThoiGianKhoa());
        assertFalse(user.coBiKhoaTam());
    }
}
