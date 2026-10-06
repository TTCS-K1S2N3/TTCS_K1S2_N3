package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.BoLocDaLuu;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử BoLocDaLuuDAO - CRUD & Transaction đặt mặc định (Story S3-07, AC3)")
class BoLocDaLuuDAOTest {

    private static Connection h2Connection;
    private static BoLocDaLuuDAO dao;

    @BeforeAll
    static void setUp() throws Exception {
        h2Connection = DriverManager.getConnection("jdbc:h2:mem:crm_s3_07_boloc_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                if (h2Connection.isClosed()) {
                    h2Connection = DriverManager.getConnection("jdbc:h2:mem:crm_s3_07_boloc_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
                }
            } catch (SQLException ignored) {}
            return h2Connection;
        });

        dao = new BoLocDaLuuDAO();

        try (Statement st = h2Connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS nguoi_dung (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(150), email VARCHAR(200))");

            st.execute("CREATE TABLE IF NOT EXISTS bo_loc_da_luu (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "nguoi_dung_id BIGINT NOT NULL, " +
                    "loai_doi_tuong VARCHAR(50) NOT NULL, " +
                    "ten_bo_loc VARCHAR(150) NOT NULL, " +
                    "tieu_chi_json TEXT NOT NULL, " +
                    "mac_dinh INT NOT NULL DEFAULT 0, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // Nạp người dùng test
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email) VALUES (1, 'Nguyễn Văn A', 'sales.a@crm.vn')");
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email) VALUES (2, 'Trần Văn B', 'sales.b@crm.vn')");
        }
    }

    @AfterAll
    static void tearDown() throws Exception {
        if (h2Connection != null && !h2Connection.isClosed()) {
            h2Connection.close();
        }
        DatabaseConfig.resetConnectionSupplier();
    }

    @Test
    @DisplayName("AC3: Lưu bộ lọc mới và tìm lại theo ID thành công")
    void testLuuBoLoc_Moi_ThanhCong() throws SQLException {
        BoLocDaLuu boLoc = new BoLocDaLuu();
        boLoc.setNguoiDungId(1L);
        boLoc.setLoaiDoiTuong(BoLocDaLuu.LOAI_KHACH_HANG);
        boLoc.setTenBoLoc("Khách tiềm năng Hà Nội");
        boLoc.setTieuChiJson("{\"trangThai\":\"TIEM_NANG\",\"khuVucId\":1}");
        boLoc.setMacDinh(false);

        long id = dao.luuBoLoc(boLoc);
        assertTrue(id > 0);

        BoLocDaLuu layRa = dao.timTheoId(id, 1L);
        assertNotNull(layRa);
        assertEquals("Khách tiềm năng Hà Nội", layRa.getTenBoLoc());
        assertEquals("{\"trangThai\":\"TIEM_NANG\",\"khuVucId\":1}", layRa.getTieuChiJson());
        assertFalse(layRa.isMacDinh());
    }

    @Test
    @DisplayName("AC3 Bảo mật: Người dùng B không thể truy cập bộ lọc đã lưu của người dùng A")
    void testTimTheoId_NguoiDungKhac_TraVeNull() throws SQLException {
        BoLocDaLuu boLoc = new BoLocDaLuu();
        boLoc.setNguoiDungId(1L);
        boLoc.setLoaiDoiTuong(BoLocDaLuu.LOAI_KHACH_HANG);
        boLoc.setTenBoLoc("Bộ lọc riêng của A");
        boLoc.setTieuChiJson("{\"trangThai\":\"TIEM_NANG\"}");

        long id = dao.luuBoLoc(boLoc);

        // User 2 (B) cố tình đọc bộ lọc của A
        BoLocDaLuu ketQua = dao.timTheoId(id, 2L);
        assertNull(ketQua, "Người dùng B không được phép đọc bộ lọc của người dùng A");
    }

    @Test
    @DisplayName("AC3: Cập nhật tiêu chí khi lưu lại bộ lọc đã có cùng tên")
    void testLuuBoLoc_TrungTen_CapNhatTieuChi() throws SQLException {
        BoLocDaLuu lan1 = new BoLocDaLuu(1L, "Khách cần gọi tuần này", "{\"trangThai\":\"TIEM_NANG\"}");
        long id1 = dao.luuBoLoc(lan1);

        BoLocDaLuu lan2 = new BoLocDaLuu(1L, "Khách cần gọi tuần này", "{\"trangThai\":\"DANG_GIAO_DICH\",\"nganhNgheId\":3}");
        long id2 = dao.luuBoLoc(lan2);

        assertEquals(id1, id2, "Cùng tên phải cập nhật bản ghi cũ thay vì tạo bản ghi rác");

        BoLocDaLuu ketQua = dao.timTheoId(id1, 1L);
        assertNotNull(ketQua);
        assertEquals("{\"trangThai\":\"DANG_GIAO_DICH\",\"nganhNgheId\":3}", ketQua.getTieuChiJson());
    }

    @Test
    @DisplayName("AC3: Lấy danh sách bộ lọc của người dùng và sắp xếp")
    void testLayDanhSachTheoNguoiDung() throws SQLException {
        dao.luuBoLoc(new BoLocDaLuu(1L, "Bộ lọc danh sách 1", "{}"));
        dao.luuBoLoc(new BoLocDaLuu(1L, "Bộ lọc danh sách 2", "{}"));

        List<BoLocDaLuu> dsA = dao.layDanhSachTheoNguoiDung(1L, BoLocDaLuu.LOAI_KHACH_HANG);
        assertFalse(dsA.isEmpty());
        for (BoLocDaLuu item : dsA) {
            assertEquals(1L, item.getNguoiDungId());
        }
    }

    @Test
    @DisplayName("AC3 Transaction: Đặt mặc định bộ lọc duy nhất, tự động gỡ mặc định các bộ lọc khác")
    void testDatMacDinh_TransactionDuyNhat() throws SQLException {
        long idA = dao.luuBoLoc(new BoLocDaLuu(1L, "Bộ lọc mặc định A", "{}"));
        long idB = dao.luuBoLoc(new BoLocDaLuu(1L, "Bộ lọc mặc định B", "{}"));

        // Đặt idA làm mặc định
        dao.datMacDinh(idA, 1L, BoLocDaLuu.LOAI_KHACH_HANG);
        assertTrue(dao.timTheoId(idA, 1L).isMacDinh());
        assertFalse(dao.timTheoId(idB, 1L).isMacDinh());

        // Đặt idB làm mặc định => idA tự động bị gỡ bỏ
        dao.datMacDinh(idB, 1L, BoLocDaLuu.LOAI_KHACH_HANG);
        assertFalse(dao.timTheoId(idA, 1L).isMacDinh());
        assertTrue(dao.timTheoId(idB, 1L).isMacDinh());

        // Tìm bộ lọc mặc định
        BoLocDaLuu macDinh = dao.timBoLocMacDinh(1L, BoLocDaLuu.LOAI_KHACH_HANG);
        assertNotNull(macDinh);
        assertEquals(idB, macDinh.getId());
    }

    @Test
    @DisplayName("AC3: Xóa bộ lọc thành công")
    void testXoaBoLoc() throws SQLException {
        long id = dao.luuBoLoc(new BoLocDaLuu(1L, "Bộ lọc cần xóa", "{}"));
        assertNotNull(dao.timTheoId(id, 1L));

        boolean daXoa = dao.xoaBoLoc(id, 1L);
        assertTrue(daXoa);
        assertNull(dao.timTheoId(id, 1L));
    }
}
