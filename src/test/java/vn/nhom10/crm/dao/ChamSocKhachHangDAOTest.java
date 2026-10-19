package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dto.KhachHangChamSocDTO;
import vn.nhom10.crm.model.PhamViDuLieu;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử ChamSocKhachHangDAO - Thực thi SQL thật và Transaction (S3-09)")
class ChamSocKhachHangDAOTest {

    private static final String H2_URL = "jdbc:h2:mem:crm_cham_soc_dao_test;MODE=MySQL;DB_CLOSE_DELAY=-1";
    private static ChamSocKhachHangDAO dao;

    @BeforeAll
    static void setUpAll() throws Exception {
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection(H2_URL, "sa", "");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        dao = new ChamSocKhachHangDAO();

        try (Connection conn = DriverManager.getConnection(H2_URL, "sa", "");
             Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS cau_hinh_he_thong (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_cau_hinh VARCHAR(100) NOT NULL UNIQUE, " +
                    "nhom_cau_hinh VARCHAR(60), kieu_du_lieu VARCHAR(30), gia_tri TEXT)");

            st.execute("CREATE TABLE IF NOT EXISTS nguoi_dung (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(150), nhom_kinh_doanh_id BIGINT)");

            st.execute("CREATE TABLE IF NOT EXISTS nhom_kinh_doanh (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_nhom VARCHAR(50), ten_nhom VARCHAR(150))");

            st.execute("CREATE TABLE IF NOT EXISTS khach_hang (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_khach_hang VARCHAR(50), ten_cong_ty VARCHAR(255), " +
                    "nguoi_so_huu_id BIGINT, nhom_kinh_doanh_id BIGINT, " +
                    "doanh_thu_uoc_tinh DECIMAL(18,2) DEFAULT 0, " +
                    "lan_tuong_tac_cuoi TIMESTAMP NULL, " +
                    "trang_thai VARCHAR(50), ngay_tao DATE, " +
                    "mo_ta_chi_tiet TEXT, updated_at TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS hop_dong (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "so_hop_dong VARCHAR(80), khach_hang_id BIGINT, " +
                    "ngay_ky DATE, gia_tri_hop_dong DECIMAL(18,2), trang_thai VARCHAR(30))");

            st.execute("CREATE TABLE IF NOT EXISTS hoat_dong (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_hoat_dong VARCHAR(50) UNIQUE, tieu_de VARCHAR(255), " +
                    "loai_hoat_dong VARCHAR(50), khach_hang_id BIGINT, " +
                    "nguoi_phu_trach_id BIGINT, nhom_kinh_doanh_id BIGINT, " +
                    "thoi_gian_bat_dau TIMESTAMP, thoi_gian_ket_thuc TIMESTAMP, " +
                    "trang_thai VARCHAR(50), noi_dung TEXT, ket_qua TEXT, " +
                    "la_ghi_nhan_nhanh INT, ngay_tao DATE)");
        }
    }

    @BeforeEach
    void resetData() throws Exception {
        try (Connection conn = DriverManager.getConnection(H2_URL, "sa", "");
             Statement st = conn.createStatement()) {
            st.execute("DELETE FROM hoat_dong");
            st.execute("DELETE FROM hop_dong");
            st.execute("DELETE FROM khach_hang");
            st.execute("DELETE FROM nhom_kinh_doanh");
            st.execute("DELETE FROM nguoi_dung");
            st.execute("DELETE FROM cau_hinh_he_thong");

            // Seed cau hinh
            st.execute("INSERT INTO cau_hinh_he_thong (ma_cau_hinh, nhom_cau_hinh, kieu_du_lieu, gia_tri) " +
                    "VALUES ('SO_NGAY_CHAM_SOC_DINH_KY', 'KHACH_HANG', 'INTEGER', '30')");

            // Seed nguoi dung & nhom
            st.execute("INSERT INTO nhom_kinh_doanh (id, ma_nhom, ten_nhom) VALUES (1, 'MB', 'Nhóm Miền Bắc')");
            st.execute("INSERT INTO nhom_kinh_doanh (id, ma_nhom, ten_nhom) VALUES (2, 'MN', 'Nhóm Miền Nam')");

            st.execute("INSERT INTO nguoi_dung (id, ho_ten, nhom_kinh_doanh_id) VALUES (101, 'Nguyễn Văn A', 1)");
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, nhom_kinh_doanh_id) VALUES (102, 'Trần Thị B', 1)");
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, nhom_kinh_doanh_id) VALUES (201, 'Lê Văn C', 2)");

            // Seed khach hang
            // KH 1: FPT (Nhân viên 101, nhóm 1), tương tác 45 ngày trước, HĐ 850 triệu
            st.execute("INSERT INTO khach_hang (id, ma_khach_hang, ten_cong_ty, nguoi_so_huu_id, nhom_kinh_doanh_id, " +
                    "lan_tuong_tac_cuoi, trang_thai, ngay_tao) " +
                    "VALUES (1, 'KH-001', 'Công ty FPT', 101, 1, DATEADD('DAY', -45, CURRENT_TIMESTAMP), 'DANG_HOP_TAC', DATEADD('DAY', -60, CURRENT_DATE))");
            st.execute("INSERT INTO hop_dong (id, so_hop_dong, khach_hang_id, ngay_ky, gia_tri_hop_dong, trang_thai) " +
                    "VALUES (1, 'HD-001', 1, CURRENT_DATE, 850000000, 'DA_KY')");

            // KH 2: Viettel (Nhân viên 101, nhóm 1), tương tác 75 ngày trước, HĐ 1.2 tỷ
            st.execute("INSERT INTO khach_hang (id, ma_khach_hang, ten_cong_ty, nguoi_so_huu_id, nhom_kinh_doanh_id, " +
                    "lan_tuong_tac_cuoi, trang_thai, ngay_tao) " +
                    "VALUES (2, 'KH-002', 'Tập đoàn Viettel', 101, 1, DATEADD('DAY', -75, CURRENT_TIMESTAMP), 'DANG_HOP_TAC', DATEADD('DAY', -90, CURRENT_DATE))");
            st.execute("INSERT INTO hop_dong (id, so_hop_dong, khach_hang_id, ngay_ky, gia_tri_hop_dong, trang_thai) " +
                    "VALUES (2, 'HD-002', 2, CURRENT_DATE, 1200000000, 'DA_KY')");

            // KH 3: VNPT (Nhân viên 102, nhóm 1), tương tác 10 ngày trước (< 30 ngày), HĐ 500 triệu
            st.execute("INSERT INTO khach_hang (id, ma_khach_hang, ten_cong_ty, nguoi_so_huu_id, nhom_kinh_doanh_id, " +
                    "lan_tuong_tac_cuoi, trang_thai, ngay_tao) " +
                    "VALUES (3, 'KH-003', 'Tập đoàn VNPT', 102, 1, DATEADD('DAY', -10, CURRENT_TIMESTAMP), 'DANG_HOP_TAC', DATEADD('DAY', -20, CURRENT_DATE))");
            st.execute("INSERT INTO hop_dong (id, so_hop_dong, khach_hang_id, ngay_ky, gia_tri_hop_dong, trang_thai) " +
                    "VALUES (3, 'HD-003', 3, CURRENT_DATE, 500000000, 'DA_KY')");

            // KH 4: VNG (Nhân viên 201, nhóm 2), tương tác 40 ngày trước, HĐ 650 triệu
            st.execute("INSERT INTO khach_hang (id, ma_khach_hang, ten_cong_ty, nguoi_so_huu_id, nhom_kinh_doanh_id, " +
                    "lan_tuong_tac_cuoi, trang_thai, ngay_tao) " +
                    "VALUES (4, 'KH-004', 'Công ty VNG', 201, 2, DATEADD('DAY', -40, CURRENT_TIMESTAMP), 'DANG_HOP_TAC', DATEADD('DAY', -50, CURRENT_DATE))");
            st.execute("INSERT INTO hop_dong (id, so_hop_dong, khach_hang_id, ngay_ky, gia_tri_hop_dong, trang_thai) " +
                    "VALUES (4, 'HD-004', 4, CURRENT_DATE, 650000000, 'DA_KY')");
        }
    }

    @AfterAll
    static void tearDownAll() throws Exception {
        DatabaseConfig.resetConnectionSupplier();
    }

    @Test
    @DisplayName("AC1: Đọc cấu hình chu kỳ SO_NGAY_CHAM_SOC_DINH_KY từ database")
    void testLayCauHinhSoNgayMacDinh_ThanhCong() {
        int soNgay = dao.layCauHinhSoNgayMacDinh();
        assertEquals(30, soNgay, "Chu kỳ cấu hình trong DB phải là 30 ngày");
    }

    @Test
    @DisplayName("AC1 & AC2: Lọc khách chưa tương tác trong N ngày và sắp xếp giá trị HĐ giảm dần")
    void testLayDanhSachCanChamSoc_SapXepGiaTriGiamDan() throws Exception {
        // Truy vấn với scope TOAN_BO, N = 30 ngày
        List<KhachHangChamSocDTO> ds = dao.layDanhSachCanChamSoc(
                1L, Collections.emptySet(), PhamViDuLieu.TOAN_BO, 30, null, true
        );

        assertNotNull(ds);
        // KH 3 (VNPT) chỉ mới 10 ngày chưa tương tác -> Bị loại bỏ bởi điều kiện >= 30 ngày
        assertEquals(3, ds.size(), "Chỉ có 3 khách hàng thỏa mãn tiêu chí chưa tương tác >= 30 ngày");

        // AC2: Kiểm tra thứ tự sắp xếp giảm dần theo giá trị hợp đồng
        // Thứ tự mong đợi: Viettel (1.2 tỷ) -> FPT (850 tr) -> VNG (650 tr)
        assertEquals("KH-002", ds.get(0).getMaKhachHang(), "Hợp đồng lớn nhất (Viettel - 1.2 tỷ) phải xếp đầu tiên");
        assertEquals(new BigDecimal("1200000000.00"), ds.get(0).getTongGiaTriHopDong());

        assertEquals("KH-001", ds.get(1).getMaKhachHang(), "Hợp đồng thứ hai (FPT - 850 triệu) phải xếp thứ hai");
        assertEquals(new BigDecimal("850000000.00"), ds.get(1).getTongGiaTriHopDong());

        assertEquals("KH-004", ds.get(2).getMaKhachHang(), "Hợp đồng thứ ba (VNG - 650 triệu) phải xếp thứ ba");
        assertEquals(new BigDecimal("650000000.00"), ds.get(2).getTongGiaTriHopDong());
    }

    @Test
    @DisplayName("Data Scope: CA_NHAN chỉ thấy khách của mình, không thấy của người khác")
    void testLayDanhSachCanChamSoc_DataScope_CaNhan() throws Exception {
        // User 101 (Nhân viên A) với scope CA_NHAN
        List<KhachHangChamSocDTO> ds = dao.layDanhSachCanChamSoc(
                101L, Collections.emptySet(), PhamViDuLieu.CA_NHAN, 30, null, true
        );

        assertNotNull(ds);
        assertEquals(2, ds.size(), "Nhân viên 101 chỉ thấy 2 khách hàng do mình sở hữu (FPT, Viettel)");
        for (KhachHangChamSocDTO kh : ds) {
            assertEquals(101L, kh.getNguoiPhuTrachId());
            assertNotEquals("KH-004", kh.getMaKhachHang(), "Tuyệt đối không thấy khách hàng VNG của nhân viên 201");
        }
    }

    @Test
    @DisplayName("Data Scope: NHOM chỉ thấy khách trong nhóm của mình")
    void testLayDanhSachCanChamSoc_DataScope_Nhom() throws Exception {
        Set<Long> nhom1 = new HashSet<>();
        nhom1.add(1L);

        List<KhachHangChamSocDTO> ds = dao.layDanhSachCanChamSoc(
                100L, nhom1, PhamViDuLieu.NHOM, 30, null, true
        );

        assertNotNull(ds);
        assertEquals(2, ds.size(), "Nhóm 1 chỉ thấy FPT và Viettel, không thấy VNG (thuộc Nhóm 2)");
        for (KhachHangChamSocDTO kh : ds) {
            assertEquals(1L, kh.getNhomKinhDoanhId());
        }
    }

    @Test
    @DisplayName("AC3: Đánh dấu đã liên hệ thực thi transaction nguyên tử (Cập nhật KH và tạo Hoạt động)")
    void testDanhDauDaLienHe_TransactionNguyenTu() throws Exception {
        boolean ok = dao.danhDauDaLienHe(1L, 101L, 1L, "CUOC_GOI", "Đã gọi điện tư vấn gia hạn gói dịch vụ.");
        assertTrue(ok, "Đánh dấu liên hệ phải thành công");

        // Kiểm tra bảng khach_hang: lan_tuong_tac_cuoi đã được cập nhật thành ngày hôm nay
        try (Connection conn = DriverManager.getConnection(H2_URL, "sa", "");
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT lan_tuong_tac_cuoi FROM khach_hang WHERE id = 1")) {
            assertTrue(rs.next());
            assertNotNull(rs.getTimestamp("lan_tuong_tac_cuoi"));
        }

        // Kiểm tra bảng hoat_dong: phải có 1 bản ghi được tạo
        try (Connection conn = DriverManager.getConnection(H2_URL, "sa", "");
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM hoat_dong WHERE khach_hang_id = 1")) {
            assertTrue(rs.next());
            assertEquals("CUOC_GOI", rs.getString("loai_hoat_dong"));
            assertEquals("HOAN_THANH", rs.getString("trang_thai"));
            assertEquals("Đã gọi điện tư vấn gia hạn gói dịch vụ.", rs.getString("noi_dung"));
            assertTrue(rs.getString("ma_hoat_dong").startsWith("HD-CSKH-"));
        }
    }

    @Test
    @DisplayName("AC3: Transaction Rollback khi ID khách hàng không tồn tại")
    void testDanhDauDaLienHe_RollbackKhiKhachHangKhongTonTai() throws Exception {
        boolean ok = dao.danhDauDaLienHe(99999L, 101L, 1L, "CUOC_GOI", "Ghi chú thử nghiệm");
        assertFalse(ok, "Phải trả về false khi khách hàng không tồn tại");

        // Đảm bảo không có bản ghi mồ côi nào được tạo trong hoat_dong
        try (Connection conn = DriverManager.getConnection(H2_URL, "sa", "");
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) AS total FROM hoat_dong WHERE khach_hang_id = 99999")) {
            assertTrue(rs.next());
            assertEquals(0, rs.getInt("total"), "Không được tạo bản ghi hoạt động khi rollback");
        }
    }
}
