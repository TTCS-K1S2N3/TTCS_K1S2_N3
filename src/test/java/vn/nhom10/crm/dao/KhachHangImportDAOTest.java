package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Kiểm thử KhachHangImportDAO (Story S3-06)")
public class KhachHangImportDAOTest {

    private static Connection initConn;
    private static KhachHangImportDAO dao;

    @BeforeAll
    static void setUpAll() throws Exception {
        initConn = DriverManager.getConnection("jdbc:h2:mem:crm_import_dao_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection("jdbc:h2:mem:crm_import_dao_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        dao = new KhachHangImportDAO();

        try (Statement st = initConn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS khach_hang (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_khach_hang VARCHAR(50) UNIQUE, " +
                    "ten_cong_ty VARCHAR(255) NOT NULL, " +
                    "ten_chuan_hoa VARCHAR(255), " +
                    "ma_so_thue VARCHAR(50) UNIQUE, " +
                    "nganh_nghe_id BIGINT, " +
                    "quy_mo_id BIGINT, " +
                    "website VARCHAR(255), " +
                    "dia_chi VARCHAR(500), " +
                    "khu_vuc_id BIGINT, " +
                    "nguoi_so_huu_id BIGINT NOT NULL, " +
                    "nhom_kinh_doanh_id BIGINT, " +
                    "doanh_thu_uoc_tinh DECIMAL(18,2) DEFAULT 0.00, " +
                    "cong_ty_me_id BIGINT, " +
                    "trang_thai VARCHAR(50) DEFAULT 'TIEM_NANG', " +
                    "mo_ta_chi_tiet TEXT, " +
                    "ngay_tao DATE DEFAULT CURRENT_DATE, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        }
    }

    @AfterAll
    static void tearDownAll() throws Exception {
        DatabaseConfig.resetConnectionSupplier();
        if (initConn != null && !initConn.isClosed()) {
            initConn.close();
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        try (Connection conn = DatabaseConnection.layKetNoi();
             Statement st = conn.createStatement()) {
            st.execute("DELETE FROM khach_hang");
        }
    }

    @Test
    @DisplayName("Thêm mới khách hàng vào database thành công")
    void testThemKhachHang() throws Exception {
        KhachHang kh = new KhachHang();
        kh.setMaKhachHang("KH-TEST-001");
        kh.setTenCongTy("Công ty Công nghệ Việt Hàn");
        kh.setMaSoThue("0109988771");
        kh.setWebsite("https://viethan.vn");
        kh.setDiaChi("Cầu Giấy, Hà Nội");
        kh.setDoanhThuUocTinh(new BigDecimal("2500000000"));
        kh.setTrangThai("TIEM_NANG");
        kh.setNguoiSoHuuId(1L);
        kh.setNhomKinhDoanhId(10L);
        kh.setMoTaChiTiet("Khách hàng qua giới thiệu");

        Long id = dao.themKhachHang(kh);
        assertNotNull(id, "ID sinh ra không được null");

        KhachHang found = dao.timTheoId(id);
        assertNotNull(found);
        assertEquals("KH-TEST-001", found.getMaKhachHang());
        assertEquals("Công ty Công nghệ Việt Hàn", found.getTenCongTy());
        assertEquals("0109988771", found.getMaSoThue());
        assertEquals(1L, found.getNguoiSoHuuId());
        assertEquals(10L, found.getNhomKinhDoanhId());
    }

    @Test
    @DisplayName("Cập nhật thông tin khách hàng bị trùng thành công")
    void testCapNhatKhachHang() throws Exception {
        KhachHang kh = new KhachHang();
        kh.setMaKhachHang("KH-UPDATE-01");
        kh.setTenCongTy("Công ty ABC Cũ");
        kh.setMaSoThue("0101112223");
        kh.setNguoiSoHuuId(1L);
        kh.setDoanhThuUocTinh(BigDecimal.ZERO);
        Long id = dao.themKhachHang(kh);

        // Cập nhật với dữ liệu mới từ Excel
        kh.setId(id);
        kh.setTenCongTy("Công ty ABC Mới Cập Nhật");
        kh.setDoanhThuUocTinh(new BigDecimal("5000000000"));
        kh.setDiaChi("Tòa nhà Landmark 72");
        kh.setWebsite("https://abc-updated.com");
        kh.setTrangThai("DANG_CHAM_SOC");

        boolean ok = dao.capNhatKhachHang(kh);
        assertTrue(ok, "Cập nhật phải trả về true");

        KhachHang updated = dao.timTheoId(id);
        assertEquals("Công ty ABC Mới Cập Nhật", updated.getTenCongTy());
        assertEquals(0, new BigDecimal("5000000000").compareTo(updated.getDoanhThuUocTinh()));
        assertEquals("DANG_CHAM_SOC", updated.getTrangThai());
    }

    @Test
    @DisplayName("Lấy danh sách đối chiếu trùng lặp theo Data Scope: Cá nhân, Nhóm, Toàn bộ")
    void testLayDanhSachDoiChieuTrung() throws Exception {
        KhachHang kh1 = new KhachHang();
        kh1.setMaKhachHang("KH-01");
        kh1.setTenCongTy("Công ty User1 Nhóm 1");
        kh1.setMaSoThue("010001");
        kh1.setNguoiSoHuuId(1L);
        kh1.setNhomKinhDoanhId(10L);
        dao.themKhachHang(kh1);

        KhachHang kh2 = new KhachHang();
        kh2.setMaKhachHang("KH-02");
        kh2.setTenCongTy("Công ty User2 Cùng Nhóm 1");
        kh2.setMaSoThue("010002");
        kh2.setNguoiSoHuuId(2L);
        kh2.setNhomKinhDoanhId(10L);
        dao.themKhachHang(kh2);

        KhachHang kh3 = new KhachHang();
        kh3.setMaKhachHang("KH-03");
        kh3.setTenCongTy("Công ty User3 Nhóm Khác");
        kh3.setMaSoThue("010003");
        kh3.setNguoiSoHuuId(3L);
        kh3.setNhomKinhDoanhId(20L);
        dao.themKhachHang(kh3);

        // 1. Phạm vi CA_NHAN của user 1 -> chỉ thấy 1 bản ghi
        List<KhachHang> dsCaNhan = dao.layDanhSachDoiChieuTrung(1L, 10L, PhamViDuLieu.CA_NHAN);
        assertEquals(1, dsCaNhan.size());
        assertEquals("KH-01", dsCaNhan.get(0).getMaKhachHang());

        // 2. Phạm vi NHOM của nhóm 10 -> thấy 2 bản ghi (kh1, kh2)
        List<KhachHang> dsNhom = dao.layDanhSachDoiChieuTrung(1L, 10L, PhamViDuLieu.NHOM);
        assertEquals(2, dsNhom.size());

        // 3. Phạm vi TOAN_BO -> thấy toàn bộ 3 bản ghi
        List<KhachHang> dsToanBo = dao.layDanhSachDoiChieuTrung(1L, 10L, PhamViDuLieu.TOAN_BO);
        assertEquals(3, dsToanBo.size());
    }

    @Test
    @DisplayName("Kiểm tra tồn tại mã khách hàng")
    void testKiemTraTonTaiMaKhachHang() throws Exception {
        KhachHang kh = new KhachHang();
        kh.setMaKhachHang("KH-UNIQUE-123");
        kh.setTenCongTy("Công ty Kiểm Tra Mã");
        kh.setNguoiSoHuuId(1L);
        Long id = dao.themKhachHang(kh);

        assertTrue(dao.kiemTraTonTaiMaKhachHang("KH-UNIQUE-123", null));
        assertTrue(dao.kiemTraTonTaiMaKhachHang("kh-unique-123", null)); // Không phân biệt hoa thường
        assertFalse(dao.kiemTraTonTaiMaKhachHang("KH-UNIQUE-123", id)); // Exclude chính nó
        assertFalse(dao.kiemTraTonTaiMaKhachHang("KH-NOT-EXIST", null));
    }
}
