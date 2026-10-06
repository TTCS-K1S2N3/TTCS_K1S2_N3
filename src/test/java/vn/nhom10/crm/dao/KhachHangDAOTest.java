package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.KhachHang;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class KhachHangDAOTest {

    private static Connection h2Connection;
    private final KhachHangDAO dao = new KhachHangDAO();

    @BeforeAll
    public static void setUp() throws Exception {
        Class.forName("org.h2.Driver");
        h2Connection = DriverManager.getConnection("jdbc:h2:mem:crm_test_khachhang;MODE=MySQL;DB_CLOSE_DELAY=-1");

        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS nguoi_dung (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(255) NOT NULL, " +
                    "email VARCHAR(255) NOT NULL UNIQUE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS nhom_kinh_doanh (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ten_nhom VARCHAR(255) NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS khach_hang (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_khach_hang VARCHAR(50) NULL UNIQUE, " +
                    "ten_cong_ty VARCHAR(255) NOT NULL, " +
                    "ten_chuan_hoa VARCHAR(255) NULL, " +
                    "ma_so_thue VARCHAR(50) NULL, " +
                    "nganh_nghe_id BIGINT NULL, " +
                    "quy_mo_id BIGINT NULL, " +
                    "website VARCHAR(255) NULL, " +
                    "website_chuan_hoa VARCHAR(255) NULL, " +
                    "dia_chi VARCHAR(500) NULL, " +
                    "khu_vuc_id BIGINT NULL, " +
                    "nguoi_so_huu_id BIGINT NOT NULL, " +
                    "nhom_kinh_doanh_id BIGINT NULL, " +
                    "doanh_thu_uoc_tinh DECIMAL(18,2) DEFAULT 0.00, " +
                    "cong_ty_me_id BIGINT NULL, " +
                    "trang_thai VARCHAR(50) DEFAULT 'TIEM_NANG', " +
                    "co_rui_ro TINYINT(1) DEFAULT 0, " +
                    "rui_ro_cap_nhat_luc TIMESTAMP NULL, " +
                    "lan_tuong_tac_cuoi TIMESTAMP NULL, " +
                    "gop_vao_khach_hang_id BIGINT NULL, " +
                    "mo_ta_chi_tiet TEXT NULL, " +
                    "ngay_tao DATE DEFAULT CURRENT_DATE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.execute("INSERT INTO nguoi_dung (id, ho_ten, email) VALUES (1, 'Nguyễn Văn Sales', 'sales@crm.vn')");
            stmt.execute("INSERT INTO nhom_kinh_doanh (id, ten_nhom) VALUES (1, 'Nhóm Miền Bắc')");
        }

        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection("jdbc:h2:mem:crm_test_khachhang;MODE=MySQL");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @AfterAll
    public static void tearDown() throws Exception {
        DatabaseConfig.resetConnectionSupplier();
        if (h2Connection != null && !h2Connection.isClosed()) {
            h2Connection.close();
        }
    }

    @Test
    public void testThemVaCapNhatCoRuiRo() {
        KhachHang kh = new KhachHang("Tập đoàn Công nghệ Demo", 1L);
        kh.setMaKhachHang("KH-DEMO-01");
        kh.setDoanhThuUocTinh(new BigDecimal("500000000.00"));
        kh.setNhomKinhDoanhId(1L);

        Long id = dao.themKhachHang(kh);
        assertNotNull(id);
        assertTrue(id > 0);

        KhachHang timDuoc = dao.timTheoId(id);
        assertNotNull(timDuoc);
        assertEquals("Tập đoàn Công nghệ Demo", timDuoc.getTenCongTy());
        assertEquals("Nguyễn Văn Sales", timDuoc.getTenNguoiSoHuu());
        assertFalse(timDuoc.isCoRuiRo());

        // Gắn cờ rủi ro rời bỏ
        boolean updated = dao.capNhatCoRuiRo(id, true);
        assertTrue(updated);
        assertTrue(dao.kiemTraCoRuiRo(id));

        List<KhachHang> dsRuiRo = dao.layDanhSachKhachHangRuiRo(1L);
        assertEquals(1, dsRuiRo.size());
        assertEquals(id, dsRuiRo.get(0).getId());

        // Gỡ cờ rủi ro
        boolean unflagged = dao.capNhatCoRuiRo(id, false);
        assertTrue(unflagged);
        assertFalse(dao.kiemTraCoRuiRo(id));
    }
}
