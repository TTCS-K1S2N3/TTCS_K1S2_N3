package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.TrangThaiKhachHangEnum;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử KhachHangDAO - Quản lý hồ sơ khách hàng doanh nghiệp & Data Scope (Story S3-01)")
class KhachHangDAOTest {

    private static final String H2_URL = "jdbc:h2:mem:crm_s3_01_dao_test;MODE=MySQL;DB_CLOSE_DELAY=-1";
    private static Connection rootConnection;
    private KhachHangDAO dao;

    @BeforeAll
    static void setUpDatabase() throws Exception {
        rootConnection = DriverManager.getConnection(H2_URL, "sa", "");
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection(H2_URL, "sa", "");
            } catch (SQLException e) {
                return null;
            }
        });

        try (Statement st = rootConnection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS nganh_nghe (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_nganh VARCHAR(50), ten_nganh VARCHAR(150), thu_tu_hien_thi INT, hoat_dong TINYINT, created_at DATETIME)");

            st.execute("CREATE TABLE IF NOT EXISTS quy_mo_doanh_nghiep (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_quy_mo VARCHAR(50), ten_quy_mo VARCHAR(150), thu_tu_hien_thi INT, hoat_dong TINYINT, created_at DATETIME)");

            st.execute("CREATE TABLE IF NOT EXISTS khu_vuc_dia_ly (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_khu_vuc VARCHAR(50), ten_khu_vuc VARCHAR(150))");

            st.execute("CREATE TABLE IF NOT EXISTS nhom_kinh_doanh (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_nhom VARCHAR(50), ten_nhom VARCHAR(150), nhom_cha_id BIGINT)");

            st.execute("CREATE TABLE IF NOT EXISTS nguoi_dung (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(150), email VARCHAR(200), nhom_kinh_doanh_id BIGINT)");

            st.execute("CREATE TABLE IF NOT EXISTS khach_hang (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_khach_hang VARCHAR(50) UNIQUE, " +
                    "ten_cong_ty VARCHAR(255) NOT NULL, " +
                    "ten_chuan_hoa VARCHAR(255), " +
                    "ma_so_thue VARCHAR(50) UNIQUE, " +
                    "nganh_nghe_id BIGINT, " +
                    "quy_mo_id BIGINT, " +
                    "website VARCHAR(255), " +
                    "website_chuan_hoa VARCHAR(255), " +
                    "dia_chi VARCHAR(500), " +
                    "khu_vuc_id BIGINT, " +
                    "nguoi_so_huu_id BIGINT NOT NULL, " +
                    "nhom_kinh_doanh_id BIGINT, " +
                    "doanh_thu_uoc_tinh DECIMAL(18,2) DEFAULT 0.00, " +
                    "cong_ty_me_id BIGINT, " +
                    "trang_thai VARCHAR(50) NOT NULL DEFAULT 'TIEM_NANG', " +
                    "co_rui_ro TINYINT DEFAULT 0, " +
                    "rui_ro_cap_nhat_luc DATETIME, " +
                    "lan_tuong_tac_cuoi DATETIME, " +
                    "gop_vao_khach_hang_id BIGINT, " +
                    "mo_ta_chi_tiet TEXT, " +
                    "ngay_tao DATE, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            // Nạp dữ liệu danh mục & người dùng mẫu
            st.execute("INSERT INTO nganh_nghe (id, ma_nganh, ten_nganh) VALUES (1, 'CNTT', 'Công nghệ thông tin'), (2, 'BAN_LE', 'Bán lẻ & Thương mại')");
            st.execute("INSERT INTO quy_mo_doanh_nghiep (id, ma_quy_mo, ten_quy_mo) VALUES (1, 'VUA', '100 - 500 nhân sự'), (2, 'LON', 'Trên 500 nhân sự')");
            st.execute("INSERT INTO nhom_kinh_doanh (id, ma_nhom, ten_nhom) VALUES (1, 'KD_BAC', 'Nhóm Miền Bắc'), (2, 'KD_NAM', 'Nhóm Miền Nam')");
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email, nhom_kinh_doanh_id) VALUES " +
                    "(101, 'Sales A', 'sales.a@crm.vn', 1), " +
                    "(102, 'Sales B', 'sales.b@crm.vn', 1), " +
                    "(103, 'Sales C', 'sales.c@crm.vn', 2)");
        }
    }

    @AfterAll
    static void tearDownDatabase() throws Exception {
        DatabaseConfig.resetConnectionSupplier();
        if (rootConnection != null && !rootConnection.isClosed()) {
            rootConnection.close();
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        dao = new KhachHangDAO();
        try (Statement st = rootConnection.createStatement()) {
            st.execute("DELETE FROM khach_hang");
        }
    }

    @Test
    @DisplayName("AC1: Khai báo thành công tên công ty, mã số thuế, ngành nghề, quy mô, website, địa chỉ, người sở hữu")
    void testThemVaLayChiTiet_DayDuTruongAC1() throws SQLException {
        KhachHang kh = new KhachHang();
        kh.setTenCongTy("Công ty Cổ phần Giải pháp Phần mềm ABC");
        kh.setMaSoThue("0109988776");
        kh.setNganhNgheId(1L);
        kh.setQuyMoId(1L);
        kh.setWebsite("https://abcsoftware.vn");
        kh.setDiaChi("Tầng 5, Tòa nhà FPT, Cầu Giấy, Hà Nội");
        kh.setNguoiSoHuuId(101L);
        kh.setNhomKinhDoanhId(1L);
        kh.setDoanhThuUocTinh(new BigDecimal("500000000.00"));
        kh.setTrangThai(TrangThaiKhachHangEnum.TIEM_NANG.getMa());
        kh.setMoTaChiTiet("Khách hàng tiềm năng khối Enterprise");

        Long id = dao.themKhachHang(kh);
        assertNotNull(id, "ID khách hàng tạo mới phải khác null");

        KhachHang timThay = dao.timTheoId(id);
        assertNotNull(timThay);
        assertEquals("Công ty Cổ phần Giải pháp Phần mềm ABC", timThay.getTenCongTy());
        assertEquals("0109988776", timThay.getMaSoThue());
        assertEquals(1L, timThay.getNganhNgheId());
        assertEquals("Công nghệ thông tin", timThay.getTenNganhNghe());
        assertEquals(1L, timThay.getQuyMoId());
        assertEquals("100 - 500 nhân sự", timThay.getTenQuyMo());
        assertEquals("https://abcsoftware.vn", timThay.getWebsite());
        assertEquals("Tầng 5, Tòa nhà FPT, Cầu Giấy, Hà Nội", timThay.getDiaChi());
        assertEquals(101L, timThay.getNguoiSoHuuId());
        assertEquals("Sales A", timThay.getTenNguoiSoHuu());
        assertEquals(1L, timThay.getNhomKinhDoanhId());
        assertEquals("Nhóm Miền Bắc", timThay.getTenNhomKinhDoanh());
        assertNotNull(timThay.getMaKhachHang(), "Mã KH phải được tự động sinh");
    }

    @Test
    @DisplayName("AC2: Mã số thuế nếu có thì phải là duy nhất - phát hiện trùng mã số thuế")
    void testKiemTraTrungMaSoThue_PhatHienTrung() throws SQLException {
        KhachHang kh1 = new KhachHang();
        kh1.setTenCongTy("Công ty A");
        kh1.setMaSoThue("0101234567");
        kh1.setNguoiSoHuuId(101L);
        Long id1 = dao.themKhachHang(kh1);

        // Kiểm tra mã số thuế của kh1 đã tồn tại
        assertTrue(dao.kiemTraTrungMaSoThue("0101234567", null), "MST đã tồn tại khi kiểm tra tạo mới");
        assertTrue(dao.kiemTraTrungMaSoThue(" 0101234567 ", null), "Khoảng trắng thừa phải được trim");

        // Kiểm tra loại trừ chính ID của nó khi cập nhật
        assertFalse(dao.kiemTraTrungMaSoThue("0101234567", id1), "Không được báo trùng với chính mình khi cập nhật");

        // Kiểm tra MST chưa tồn tại
        assertFalse(dao.kiemTraTrungMaSoThue("0999999999", null));
        // Kiểm tra MST null hoặc rỗng
        assertFalse(dao.kiemTraTrungMaSoThue(null, null));
        assertFalse(dao.kiemTraTrungMaSoThue("   ", null));
    }

    @Test
    @DisplayName("AC2: Cho phép nhiều khách hàng không khai báo mã số thuế (mã số thuế rỗng hoặc null)")
    void testMaSoThueRong_KhongViPhamUnique() throws SQLException {
        KhachHang kh1 = new KhachHang();
        kh1.setTenCongTy("Cửa hàng Tạp hóa Số 1");
        kh1.setMaSoThue(null);
        kh1.setNguoiSoHuuId(101L);
        Long id1 = dao.themKhachHang(kh1);

        KhachHang kh2 = new KhachHang();
        kh2.setTenCongTy("Cửa hàng Tạp hóa Số 2");
        kh2.setMaSoThue(null);
        kh2.setNguoiSoHuuId(101L);
        Long id2 = dao.themKhachHang(kh2);

        assertNotNull(id1);
        assertNotNull(id2);
        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("AC3: Lưu và lọc theo 4 trạng thái chuẩn: Tiềm năng, Đang giao dịch, Khách hàng, Ngừng hợp tác")
    void testTrangThaiKhachHang_BonTrangThaiChuan() throws SQLException {
        taoKhachHangMau("Khách 1", "010001", TrangThaiKhachHangEnum.TIEM_NANG.getMa(), 101L, 1L);
        taoKhachHangMau("Khách 2", "010002", TrangThaiKhachHangEnum.DANG_GIAO_DICH.getMa(), 101L, 1L);
        taoKhachHangMau("Khách 3", "010003", TrangThaiKhachHangEnum.KHACH_HANG.getMa(), 101L, 1L);
        taoKhachHangMau("Khách 4", "010004", TrangThaiKhachHangEnum.NGUNG_HOP_TAC.getMa(), 101L, 1L);

        // Lọc trạng thái Tiềm năng
        List<KhachHang> dsTiemNang = dao.layDanhSach(101L, null, PhamViDuLieu.TOAN_BO, null, "TIEM_NANG", null, null, 0, 10);
        assertEquals(1, dsTiemNang.size());
        assertEquals("Khách 1", dsTiemNang.get(0).getTenCongTy());

        // Lọc trạng thái Đang giao dịch bằng tên tiếng Việt
        List<KhachHang> dsDangGD = dao.layDanhSach(101L, null, PhamViDuLieu.TOAN_BO, null, "Đang giao dịch", null, null, 0, 10);
        assertEquals(1, dsDangGD.size());
        assertEquals("Khách 2", dsDangGD.get(0).getTenCongTy());

        // Lọc trạng thái Khách hàng
        List<KhachHang> dsKhachHang = dao.layDanhSach(101L, null, PhamViDuLieu.TOAN_BO, null, "Khách hàng", null, null, 0, 10);
        assertEquals(1, dsKhachHang.size());
        assertEquals("Khách 3", dsKhachHang.get(0).getTenCongTy());

        // Lọc trạng thái Ngừng hợp tác
        List<KhachHang> dsNgungHT = dao.layDanhSach(101L, null, PhamViDuLieu.TOAN_BO, null, "Ngừng hợp tác", null, null, 0, 10);
        assertEquals(1, dsNgungHT.size());
        assertEquals("Khách 4", dsNgungHT.get(0).getTenCongTy());
    }

    @Test
    @DisplayName("AC4: Data Scope - Nhân viên chỉ thấy khách hàng mình sở hữu; Trưởng nhóm thấy toàn nhóm")
    void testDataScope_NhanVienVaTruongNhom() throws SQLException {
        // Sales A (101, nhóm 1): sở hữu 2 khách
        taoKhachHangMau("Khách của A 1", "010101", "TIEM_NANG", 101L, 1L);
        taoKhachHangMau("Khách của A 2", "010102", "TIEM_NANG", 101L, 1L);

        // Sales B (102, nhóm 1): sở hữu 1 khách
        taoKhachHangMau("Khách của B", "010103", "TIEM_NANG", 102L, 1L);

        // Sales C (103, nhóm 2): sở hữu 1 khách
        taoKhachHangMau("Khách của C", "010104", "TIEM_NANG", 103L, 2L);

        // 1. Sales A truy cập với phạm vi CA_NHAN -> chỉ thấy 2 khách của mình
        List<KhachHang> dsSalesA = dao.layDanhSach(101L, Set.of(1L), PhamViDuLieu.CA_NHAN, null, null, null, null, 0, 10);
        assertEquals(2, dsSalesA.size());
        for (KhachHang kh : dsSalesA) {
            assertEquals(101L, kh.getNguoiSoHuuId(), "Mọi khách hàng phải do Sales A sở hữu");
        }

        // 2. Trưởng nhóm Miền Bắc (nhóm 1) truy cập với phạm vi NHOM -> thấy toàn bộ khách của A và B (3 khách), không thấy C
        Set<Long> dsNhom1 = Set.of(1L);
        List<KhachHang> dsTruongNhom = dao.layDanhSach(100L, dsNhom1, PhamViDuLieu.NHOM, null, null, null, null, 0, 10);
        assertEquals(3, dsTruongNhom.size(), "Trưởng nhóm 1 phải thấy đủ 3 khách hàng thuộc nhóm 1");
        for (KhachHang kh : dsTruongNhom) {
            assertEquals(1L, kh.getNhomKinhDoanhId(), "Không được chứa khách của nhóm 2");
            assertNotEquals("Khách của C", kh.getTenCongTy());
        }

        // 3. Giám đốc truy cập với phạm vi TOAN_BO -> thấy toàn bộ 4 khách
        List<KhachHang> dsGiamDoc = dao.layDanhSach(1L, null, PhamViDuLieu.TOAN_BO, null, null, null, null, 0, 10);
        assertEquals(4, dsGiamDoc.size(), "Giám đốc phải thấy toàn bộ khách hàng trên hệ thống");
    }

    private void taoKhachHangMau(String ten, String mst, String trangThai, Long nguoiSoHuuId, Long nhomId) throws SQLException {
        KhachHang kh = new KhachHang();
        kh.setTenCongTy(ten);
        kh.setMaSoThue(mst);
        kh.setTrangThai(trangThai);
        kh.setNguoiSoHuuId(nguoiSoHuuId);
        kh.setNhomKinhDoanhId(nhomId);
        kh.setNgayTao(LocalDate.now());
        dao.themKhachHang(kh);
    }
}
