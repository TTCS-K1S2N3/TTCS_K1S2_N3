package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.NguoiDung;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử NguoiDungDAO với cơ sở dữ liệu H2 in-memory")
class NguoiDungDAOTest {

    private Connection connection;
    private NguoiDungDAO nguoiDungDAO;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:test_crm_dao;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection("jdbc:h2:mem:test_crm_dao;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
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
                    "mo_ta VARCHAR(255)" +
                    ")");

            st.execute("CREATE TABLE nguoi_dung (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(150) NOT NULL, " +
                    "email VARCHAR(200) NOT NULL UNIQUE, " +
                    "mat_khau VARCHAR(255) NOT NULL, " +
                    "so_dien_thoai VARCHAR(20), " +
                    "trang_thai VARCHAR(30) DEFAULT 'CHO_KICH_HOAT', " +
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

            // Seed dữ liệu mẫu
            st.execute("INSERT INTO nhom_kinh_doanh (id, ma_nhom, ten_nhom) VALUES (1, 'KD_BAC', 'Kinh Doanh Miền Bắc')");
            st.execute("INSERT INTO nhom_kinh_doanh (id, ma_nhom, ten_nhom) VALUES (2, 'KD_NAM', 'Kinh Doanh Miền Nam')");

            st.execute("INSERT INTO vai_tro (id, ma_vai_tro, ten_vai_tro) VALUES (1, 'ADMIN', 'Quản trị hệ thống')");
            st.execute("INSERT INTO vai_tro (id, ma_vai_tro, ten_vai_tro) VALUES (4, 'SALES_REP', 'Nhân viên kinh doanh')");

            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email, mat_khau, trang_thai, nhom_kinh_doanh_id) " +
                    "VALUES (1, 'Admin He Thong', 'admin@crm.vn', 'hashpass', 'HOAT_DONG', 1)");
            st.execute("INSERT INTO nguoi_dung_vai_tro (nguoi_dung_id, vai_tro_id) VALUES (1, 1)");
        }

        VaiTroDAO vaiTroDAO = new VaiTroDAO();
        NhomKinhDoanhDAO nhomKinhDoanhDAO = new NhomKinhDoanhDAO();
        nguoiDungDAO = new NguoiDungDAO(vaiTroDAO, nhomKinhDoanhDAO);
    }

    @AfterEach
    void tearDown() throws Exception {
        DatabaseConfig.resetConnectionSupplier();
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @DisplayName("AC: Kiểm tra email trùng lặp chính xác (không phân biệt hoa thường)")
    void testKiemTraEmailTonTai() {
        assertTrue(nguoiDungDAO.kiemTraEmailTonTai("admin@crm.vn", null));
        assertTrue(nguoiDungDAO.kiemTraEmailTonTai("ADMIN@CRM.VN", null));
        assertFalse(nguoiDungDAO.kiemTraEmailTonTai("chuaco@crm.vn", null));

        // Khi excludeId chính là id của user đó -> không tính là trùng
        assertFalse(nguoiDungDAO.kiemTraEmailTonTai("admin@crm.vn", 1));
        // Nhưng nếu excludeId là người khác -> tính là trùng
        assertTrue(nguoiDungDAO.kiemTraEmailTonTai("admin@crm.vn", 99));
    }

    @Test
    @DisplayName("AC: Thêm người dùng mới và phân quyền vai trò qua transaction")
    void testThemNguoiDung() throws Exception {
        NguoiDung nd = new NguoiDung();
        nd.setHoTen("Nguyễn Văn Mới");
        nd.setEmail("nvmoi@crm.vn");
        nd.setMatKhau("temphash123");
        nd.setNhomKinhDoanhId(1);
        nd.setTrangThai(NguoiDung.TRANG_THAI_CHO_KICH_HOAT);

        int newId = nguoiDungDAO.themNguoiDung(nd, Arrays.asList(4));
        assertTrue(newId > 0);

        NguoiDung timThay = nguoiDungDAO.timTheoId(newId);
        assertNotNull(timThay);
        assertEquals("Nguyễn Văn Mới", timThay.getHoTen());
        assertEquals("nvmoi@crm.vn", timThay.getEmail());
        assertEquals(NguoiDung.TRANG_THAI_CHO_KICH_HOAT, timThay.getTrangThai());
        assertEquals(1, timThay.getDsVaiTro().size());
        assertTrue(timThay.coVaiTro("SALES_REP"));
    }

    @Test
    @DisplayName("AC: Tìm theo tên, email, nhóm, lọc theo vai trò và trạng thái; phân trang")
    void testTimKiemVaPhanTrang() throws Exception {
        // Thêm 25 nhân viên kinh doanh để test phân trang mặc định 20 dòng
        for (int i = 2; i <= 26; i++) {
            NguoiDung u = new NguoiDung();
            u.setHoTen("Nhân viên " + i);
            u.setEmail("sales" + i + "@crm.vn");
            u.setMatKhau("hash");
            u.setNhomKinhDoanhId(i % 2 == 0 ? 1 : 2);
            u.setTrangThai(i % 3 == 0 ? NguoiDung.TRANG_THAI_KHOA : NguoiDung.TRANG_THAI_HOAT_DONG);
            nguoiDungDAO.themNguoiDung(u, Collections.singletonList(4));
        }

        // 1. Phân trang trang 1 (limit 20, offset 0)
        List<NguoiDung> trang1 = nguoiDungDAO.timKiemVaPhanTrang(null, null, null, null, 20, 0);
        assertEquals(20, trang1.size());

        // 2. Phân trang trang 2 (limit 20, offset 20) -> tổng cộng 26 user, trang 2 có 6 user
        List<NguoiDung> trang2 = nguoiDungDAO.timKiemVaPhanTrang(null, null, null, null, 20, 20);
        assertEquals(6, trang2.size());

        // 3. Tìm kiếm theo tên
        List<NguoiDung> ketQuaTen = nguoiDungDAO.timKiemVaPhanTrang("Nhân viên 10", null, null, null, 20, 0);
        assertEquals(1, ketQuaTen.size());
        assertEquals("sales10@crm.vn", ketQuaTen.get(0).getEmail());

        // 4. Tìm kiếm theo email
        List<NguoiDung> ketQuaEmail = nguoiDungDAO.timKiemVaPhanTrang("admin@", null, null, null, 20, 0);
        assertEquals(1, ketQuaEmail.size());
        assertEquals("admin@crm.vn", ketQuaEmail.get(0).getEmail());

        // 5. Lọc theo nhóm 1
        int countNhom1 = nguoiDungDAO.demSoLuong(null, 1, null, null);
        assertTrue(countNhom1 > 0);

        // 6. Lọc theo vai trò SALES_REP (id=4)
        int countSales = nguoiDungDAO.demSoLuong(null, null, 4, null);
        assertEquals(25, countSales);

        // 7. Lọc theo vai trò ADMIN (id=1)
        int countAdmin = nguoiDungDAO.demSoLuong(null, null, 1, null);
        assertEquals(1, countAdmin);
    }

    @Test
    @DisplayName("Cập nhật tài khoản người dùng và thay đổi vai trò qua transaction")
    void testCapNhatNguoiDung() throws Exception {
        NguoiDung nd = nguoiDungDAO.timTheoId(1);
        assertNotNull(nd);

        nd.setHoTen("Admin Đã Đổi Tên");
        nd.setTrangThai(NguoiDung.TRANG_THAI_KHOA);
        nd.setNhomKinhDoanhId(2);

        boolean updated = nguoiDungDAO.capNhatNguoiDung(nd, Arrays.asList(1, 4));
        assertTrue(updated);

        NguoiDung sauCapNhat = nguoiDungDAO.timTheoId(1);
        assertEquals("Admin Đã Đổi Tên", sauCapNhat.getHoTen());
        assertEquals(NguoiDung.TRANG_THAI_KHOA, sauCapNhat.getTrangThai());
        assertEquals(2, sauCapNhat.getNhomKinhDoanhId());
        assertEquals(2, sauCapNhat.getDsVaiTro().size());
        assertTrue(sauCapNhat.coVaiTro("ADMIN"));
        assertTrue(sauCapNhat.coVaiTro("SALES_REP"));
    }
}
