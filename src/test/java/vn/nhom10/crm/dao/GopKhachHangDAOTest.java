package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dto.KetQuaGopKhachHangDTO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử GopKhachHangDAO - Giao dịch gộp bảo toàn dữ liệu và Rollback (Story S3-04, AC3)")
class GopKhachHangDAOTest {

    private static Connection h2Connection;
    private static GopKhachHangDAO dao;

    @BeforeAll
    static void setUpDatabase() throws Exception {
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection("jdbc:h2:mem:crm_s3_04_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        dao = new GopKhachHangDAO();

        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS nguoi_dung (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, ho_ten VARCHAR(150), email VARCHAR(200))");

            st.execute("CREATE TABLE IF NOT EXISTS khach_hang (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, ma_khach_hang VARCHAR(50), ten_cong_ty VARCHAR(255), " +
                    "nguoi_so_huu_id BIGINT, nhom_kinh_doanh_id BIGINT, gop_vao_khach_hang_id BIGINT NULL, " +
                    "trang_thai VARCHAR(50), updated_at TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS nguoi_lien_he (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, khach_hang_id BIGINT, ho_ten VARCHAR(150), email VARCHAR(255))");

            st.execute("CREATE TABLE IF NOT EXISTS co_hoi (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, khach_hang_id BIGINT, ten_co_hoi VARCHAR(255), gia_tri_du_kien DECIMAL(18,2))");

            st.execute("CREATE TABLE IF NOT EXISTS hoat_dong (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, khach_hang_id BIGINT, tieu_de VARCHAR(255), loai_hoat_dong VARCHAR(50))");

            st.execute("CREATE TABLE IF NOT EXISTS tep_dinh_kem (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, khach_hang_id BIGINT, ten_file_goc VARCHAR(255))");

            st.execute("CREATE TABLE IF NOT EXISTS lich_su_gop_khach_hang (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, khach_hang_nguon_id BIGINT, khach_hang_dich_id BIGINT, " +
                    "thuc_hien_boi_id BIGINT, du_lieu_so_sanh_json TEXT, ket_qua_chuyen_json TEXT, ly_do VARCHAR(500), created_at TIMESTAMP)");
        }
    }

    @BeforeEach
    void resetTestData() throws Exception {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement()) {
            st.execute("DELETE FROM lich_su_gop_khach_hang");
            st.execute("DELETE FROM tep_dinh_kem");
            st.execute("DELETE FROM hoat_dong");
            st.execute("DELETE FROM co_hoi");
            st.execute("DELETE FROM nguoi_lien_he");
            st.execute("DELETE FROM khach_hang");
            st.execute("DELETE FROM nguoi_dung");

            // Seed user
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email) VALUES (1, 'Trưởng nhóm Bắc', 'lead@crm.vn')");

            // Seed 2 customers: ID 10 (Dich), ID 20 (Nguon)
            st.execute("INSERT INTO khach_hang (id, ma_khach_hang, ten_cong_ty, nguoi_so_huu_id, nhom_kinh_doanh_id, gop_vao_khach_hang_id, trang_thai) " +
                    "VALUES (10, 'KH-DICH', 'FPT Chính', 101, 1, NULL, 'TIEM_NANG')");
            st.execute("INSERT INTO khach_hang (id, ma_khach_hang, ten_cong_ty, nguoi_so_huu_id, nhom_kinh_doanh_id, gop_vao_khach_hang_id, trang_thai) " +
                    "VALUES (20, 'KH-NGUON', 'FPT Phụ', 102, 1, NULL, 'TIEM_NANG')");

            // Seed 2 contacts for customer 20
            st.execute("INSERT INTO nguoi_lien_he (id, khach_hang_id, ho_ten, email) VALUES (1, 20, 'Liên hệ 1', 'lh1@fpt.vn')");
            st.execute("INSERT INTO nguoi_lien_he (id, khach_hang_id, ho_ten, email) VALUES (2, 20, 'Liên hệ 2', 'lh2@fpt.vn')");

            // Seed 1 deal for customer 20
            st.execute("INSERT INTO co_hoi (id, khach_hang_id, ten_co_hoi, gia_tri_du_kien) VALUES (1, 20, 'Cơ hội ERP FPT', 500000000)");

            // Seed 2 activities for customer 20
            st.execute("INSERT INTO hoat_dong (id, khach_hang_id, tieu_de, loai_hoat_dong) VALUES (1, 20, 'Cuộc gọi chào hàng', 'CUOC_GOI')");
            st.execute("INSERT INTO hoat_dong (id, khach_hang_id, tieu_de, loai_hoat_dong) VALUES (2, 20, 'Họp demo', 'HOP')");

            // Seed 1 attachment for customer 20
            st.execute("INSERT INTO tep_dinh_kem (id, khach_hang_id, ten_file_goc) VALUES (1, 20, 'bang_gia.pdf')");
        }
    }

    @AfterAll
    static void tearDownDatabase() throws Exception {
        DatabaseConfig.resetConnectionSupplier();
    }

    @Test
    @DisplayName("AC3 Happy Path: Gộp thành công bảo toàn 100% người liên hệ, cơ hội, hoạt động và tệp đính kèm")
    void testGopKhachHang_ThanhCong_BaoToanDuLieu() throws Exception {
        KetQuaGopKhachHangDTO ketQua = dao.thucHienGop(20L, 10L, 1L, "Trùng khách hàng", "{}");

        assertTrue(ketQua.isThanhCong(), "Gộp phải thành công");
        assertEquals(2, ketQua.getSoNguoiLienHeDaChuyen(), "Phải chuyển đúng 2 người liên hệ");
        assertEquals(1, ketQua.getSoCoHoiDaChuyen(), "Phải chuyển đúng 1 cơ hội");
        assertEquals(2, ketQua.getSoHoatDongDaChuyen(), "Phải chuyển đúng 2 hoạt động");
        assertEquals(1, ketQua.getSoTepDinhKemDaChuyen(), "Phải chuyển đúng 1 tệp đính kèm");

        // Xác nhận dữ liệu thật trong DB: tất cả đã trỏ về khách hàng đích 10
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement()) {
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM nguoi_lien_he WHERE khach_hang_id = 10")) {
                assertTrue(rs.next());
                assertEquals(2, rs.getInt(1), "Toàn bộ người liên hệ phải chuyển sang khách hàng 10");
            }
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM co_hoi WHERE khach_hang_id = 10")) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1), "Cơ hội phải chuyển sang khách hàng 10");
            }
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM hoat_dong WHERE khach_hang_id = 10")) {
                assertTrue(rs.next());
                assertEquals(2, rs.getInt(1), "Hoạt động phải chuyển sang khách hàng 10");
            }

            // Kiểm tra trạng thái khách hàng nguồn 20
            try (ResultSet rs = st.executeQuery("SELECT gop_vao_khach_hang_id, trang_thai FROM khach_hang WHERE id = 20")) {
                assertTrue(rs.next());
                assertEquals(10L, rs.getLong("gop_vao_khach_hang_id"), "gop_vao_khach_hang_id phải bằng 10");
                assertEquals("DA_GOP", rs.getString("trang_thai"), "trang_thai phải là DA_GOP");
            }

            // Kiểm tra lưu vết kiểm toán vào lich_su_gop_khach_hang
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM lich_su_gop_khach_hang WHERE khach_hang_nguon_id = 20 AND khach_hang_dich_id = 10")) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1), "Phải tạo 1 bản ghi lịch sử gộp");
            }
        }
    }

    @Test
    @DisplayName("AC3 Transaction Rollback: Khi xảy ra lỗi giữa chừng, toàn bộ thay đổi phải được Rollback nguyên vẹn")
    void testGopKhachHang_PhatSinhLoi_RollbackToanBo() throws Exception {
        try (Connection testConn = DatabaseConfig.getConnection()) {
            assertThrows(SQLException.class, () -> {
                dao.thucHienGopTrenKetNoi(testConn, 20L, 10L, 1L, "Lý do", "{}", true);
            }, "Phải ném SQLException khi cố tình gây lỗi giao dịch");
        }

        // Xác nhận dữ liệu đã được ROLLBACK an toàn: người liên hệ, cơ hội, hoạt động vẫn thuộc về khách hàng 20
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement()) {
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM nguoi_lien_he WHERE khach_hang_id = 20")) {
                assertTrue(rs.next());
                assertEquals(2, rs.getInt(1), "Người liên hệ phải còn nguyên ở khách hàng 20 do Rollback");
            }
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM co_hoi WHERE khach_hang_id = 20")) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1), "Cơ hội phải còn nguyên ở khách hàng 20 do Rollback");
            }
            try (ResultSet rs = st.executeQuery("SELECT trang_thai FROM khach_hang WHERE id = 20")) {
                assertTrue(rs.next());
                assertEquals("TIEM_NANG", rs.getString("trang_thai"), "Trạng thái khách hàng 20 không được đổi thành DA_GOP");
            }
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM lich_su_gop_khach_hang")) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(1), "Lịch sử gộp không được ghi nếu giao dịch bị Rollback");
            }
        } catch (SQLException e) {
            fail("Lỗi kiểm tra trạng thái sau rollback: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Validation: Không thể gộp khách hàng vào chính nó")
    void testGopKhachHang_VaoChinhNo_NemNgoaiLe() {
        assertThrows(IllegalArgumentException.class, () -> {
            dao.thucHienGop(10L, 10L, 1L, "Lý do", "{}");
        });
    }

    @Test
    @DisplayName("Validation: Khách hàng nguồn đã bị gộp trước đó không thể gộp tiếp")
    void testGopKhachHang_DaBiGopTruocDo_NemNgoaiLe() throws Exception {
        // Gộp lần 1 thành công
        dao.thucHienGop(20L, 10L, 1L, "Lần 1", "{}");

        // Gộp lần 2 với nguồn đã gộp -> Báo lỗi
        assertThrows(IllegalStateException.class, () -> {
            dao.thucHienGop(20L, 10L, 1L, "Lần 2", "{}");
        });
    }
}
