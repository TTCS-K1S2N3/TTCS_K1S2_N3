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
                    "chu_ky_email TEXT, " +
                    "anh_dai_dien_path VARCHAR(255), " +
                    "anh_dai_dien_thumb_path VARCHAR(255), " +
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

    @Test
    @DisplayName("DAO: Kích hoạt tài khoản từ CHO_KICH_HOAT sang HOAT_DONG và không tác động KHOA")
    void testKichHoatTaiKhoan() throws Exception {
        NguoiDung user = new NguoiDung();
        user.setHoTen("User Chờ Kích Hoạt");
        user.setEmail("cho.kichhoat@crm.vn");
        user.setMatKhau("hash123");
        user.setTrangThai(NguoiDung.TRANG_THAI_CHO_KICH_HOAT);

        int newId = nguoiDungDAO.themNguoiDung(user, Collections.singletonList(4));
        assertTrue(newId > 0);

        NguoiDung banDau = nguoiDungDAO.timTheoId(newId);
        assertEquals(NguoiDung.TRANG_THAI_CHO_KICH_HOAT, banDau.getTrangThai());

        // 1. Kích hoạt lần đầu thành công
        boolean daKichHoat = nguoiDungDAO.kichHoatTaiKhoan(newId);
        assertTrue(daKichHoat, "Tài khoản CHO_KICH_HOAT phải kích hoạt thành công sang HOAT_DONG");

        NguoiDung sauKichHoat = nguoiDungDAO.timTheoId(newId);
        assertEquals(NguoiDung.TRANG_THAI_HOAT_DONG, sauKichHoat.getTrangThai());

        // 2. Kích hoạt lại khi đã HOAT_DONG -> trả về false (không cập nhật bản ghi nào)
        boolean kichHoatLai = nguoiDungDAO.kichHoatTaiKhoan(newId);
        assertFalse(kichHoatLai, "Tài khoản đã HOAT_DONG thì không cập nhật lại");

        // 3. Khóa tài khoản -> gọi kichHoatTaiKhoan không được mở khóa KHOA
        nguoiDungDAO.khoaTaiKhoan(newId);
        NguoiDung biKhoa = nguoiDungDAO.timTheoId(newId);
        assertEquals(NguoiDung.TRANG_THAI_KHOA, biKhoa.getTrangThai());

        boolean coMoKhoa = nguoiDungDAO.kichHoatTaiKhoan(newId);
        assertFalse(coMoKhoa, "Tuyệt đối không tự mở khóa tài khoản KHOA");

        NguoiDung vanKhoa = nguoiDungDAO.timTheoId(newId);
        assertEquals(NguoiDung.TRANG_THAI_KHOA, vanKhoa.getTrangThai(), "Trạng thái KHOA phải được bảo toàn");
    }

    @Test
    @DisplayName("S1-09 Regression DAO: Cập nhật thành công cả 2 role và nhóm kinh doanh trong cùng transaction")
    void testCapNhatVaiTroVaNhomTransaction_ThanhCong() throws Exception {
        // User 1 ban đầu có nhom=1, vai tro=[ADMIN(1)]
        NguoiDung truoc = nguoiDungDAO.timTheoId(1);
        assertEquals(1, truoc.getNhomKinhDoanhId());
        assertEquals(1, truoc.getDsVaiTro().size());

        // Cập nhật sang nhom=2, vai tro=[ADMIN(1), SALES_REP(4)]
        boolean kq = nguoiDungDAO.capNhatVaiTroVaNhomTransaction(1, Arrays.asList(1, 4), 2);
        assertTrue(kq, "Cập nhật thành công");

        NguoiDung sau = nguoiDungDAO.timTheoId(1);
        assertEquals(2, sau.getNhomKinhDoanhId(), "Nhóm kinh doanh phải được cập nhật sang 2");
        assertEquals(2, sau.getDsVaiTro().size(), "Phải có đủ 2 vai trò");
        assertTrue(sau.coVaiTro("ADMIN"));
        assertTrue(sau.coVaiTro("SALES_REP"));
    }

    @Test
    @DisplayName("S1-09 Regression DAO: Lỗi khi cập nhật role sau khi update nhóm -> rollback nhóm")
    void testCapNhatVaiTroVaNhomTransaction_LoiCapNhatRole_RollbackNhom() throws Exception {
        // User 1 ban đầu: nhom=1, vai tro=[1]
        NguoiDung truoc = nguoiDungDAO.timTheoId(1);
        assertEquals(1, truoc.getNhomKinhDoanhId());

        // Thêm vai_tro_id không hợp lệ hoặc gây lỗi SQLException
        // Bảng nguoi_dung_vai_tro có foreign key hoặc ta cố tình kích hoạt lỗi SQL bằng connection hoặc invalid input
        // Ở đây ta thêm foreign key constraint để trigger SQLException khi chèn role không tồn tại
        try (Statement st = connection.createStatement()) {
            st.execute("ALTER TABLE nguoi_dung_vai_tro ADD CONSTRAINT fk_test_vaitro FOREIGN KEY (vai_tro_id) REFERENCES vai_tro(id)");
        }

        try {
            // Cố tình gán vai_tro_id = 999999 không tồn tại trong bảng vai_tro -> executeBatch() sẽ ném SQLException
            assertThrows(SQLException.class, () -> {
                nguoiDungDAO.capNhatVaiTroVaNhomTransaction(1, Arrays.asList(1, 999999), 2);
            }, "Phải ném SQLException khi cập nhật role lỗi");

            // Kiểm tra tính nguyên tử của Transaction: nhóm kinh doanh không được partial update thành 2 mà phải rollback về 1
            NguoiDung sauLoi = nguoiDungDAO.timTheoId(1);
            assertEquals(1, sauLoi.getNhomKinhDoanhId(), "Nhóm kinh doanh phải rollback về giá trị ban đầu (1)");
        } finally {
            try (Statement st = connection.createStatement()) {
                st.execute("ALTER TABLE nguoi_dung_vai_tro DROP CONSTRAINT fk_test_vaitro");
            } catch (Exception ignored) {}
        }
    }

    @Test
    @DisplayName("S1-09 Regression DAO: Lỗi khi update nhóm (user không tồn tại) -> không thay đổi role")
    void testCapNhatVaiTroVaNhomTransaction_UserKhongTonTai_KhongDoiRole() throws Exception {
        boolean kq = nguoiDungDAO.capNhatVaiTroVaNhomTransaction(99999, Arrays.asList(1, 4), 2);
        assertFalse(kq, "Cập nhật với user không tồn tại phải trả về false");
    }

    @Test
    @DisplayName("S2-02 DAO: Cập nhật thành công họ tên, số điện thoại và chữ ký email")
    void testCapNhatHoSo() throws Exception {
        boolean kq = nguoiDungDAO.capNhatHoSo(1, "Nguyễn Văn Quản Trị", "0909999999", "Chữ ký email báo giá");
        assertTrue(kq, "Cập nhật hồ sơ phải trả về true");

        NguoiDung nd = nguoiDungDAO.timTheoId(1);
        assertNotNull(nd);
        assertEquals("Nguyễn Văn Quản Trị", nd.getHoTen());
        assertEquals("0909999999", nd.getSoDienThoai());
        assertEquals("Chữ ký email báo giá", nd.getChuKyEmail());
        // Đảm bảo email và nhóm không bị thay đổi
        assertEquals("admin@crm.vn", nd.getEmail());
        assertEquals(1, nd.getNhomKinhDoanhId());
    }

    @Test
    @DisplayName("S2-03 DAO: timTheoId trả về đúng anh_dai_dien_path và anh_dai_dien_thumb_path khi DB có dữ liệu")
    void testTimTheoId_CoAvatarPath() throws Exception {
        // Cập nhật avatar path cho user 1
        boolean capNhat = nguoiDungDAO.capNhatAnhDaiDien(1, "user_1/avatar_123.jpg", "user_1/thumb_123.jpg");
        assertTrue(capNhat);

        NguoiDung nd = nguoiDungDAO.timTheoId(1);
        assertNotNull(nd);
        assertEquals("user_1/avatar_123.jpg", nd.getAnhDaiDienPath());
        assertEquals("user_1/thumb_123.jpg", nd.getAnhDaiDienThumbPath());
        assertTrue(nd.coAnhDaiDien());
    }

    @Test
    @DisplayName("S2-03 DAO: timTheoEmail trả về đúng anh_dai_dien_path và anh_dai_dien_thumb_path khi DB có dữ liệu")
    void testTimTheoEmail_CoAvatarPath() throws Exception {
        // Cập nhật avatar path cho user 1
        boolean capNhat = nguoiDungDAO.capNhatAnhDaiDien(1, "user_1/avatar_abc.jpg", "user_1/thumb_abc.jpg");
        assertTrue(capNhat);

        NguoiDung nd = nguoiDungDAO.timTheoEmail("admin@crm.vn");
        assertNotNull(nd);
        assertEquals("user_1/avatar_abc.jpg", nd.getAnhDaiDienPath());
        assertEquals("user_1/thumb_abc.jpg", nd.getAnhDaiDienThumbPath());
        assertTrue(nd.coAnhDaiDien());
    }

    @Test
    @DisplayName("S2-03 DAO: user chưa upload avatar thì timTheoId trả về path null và coAnhDaiDien là false")
    void testTimTheoId_KhongCoAvatar() {
        NguoiDung nd = nguoiDungDAO.timTheoId(1);
        assertNotNull(nd);
        assertNull(nd.getAnhDaiDienPath());
        assertNull(nd.getAnhDaiDienThumbPath());
        assertFalse(nd.coAnhDaiDien());
    }
}
