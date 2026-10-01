package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO;
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO.LoaiNghiepVu;
import vn.nhom10.crm.model.PhamViDuLieu;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử DAO phân quyền phạm vi dữ liệu SQL - Story S1-05 (AC1, AC2, AC4)")
class PhanQuyenDuLieuDAOTest {

    private static Connection h2Connection;
    private static PhanQuyenDuLieuDAO dao;

    @BeforeAll
    static void setUpDatabase() throws Exception {
        // Khởi tạo kết nối H2 In-Memory mô phỏng MySQL
        h2Connection = DriverManager.getConnection("jdbc:h2:mem:crm_s1_05_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                if (h2Connection.isClosed()) {
                    h2Connection = DriverManager.getConnection("jdbc:h2:mem:crm_s1_05_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
                }
            } catch (SQLException ignored) {}
            return h2Connection;
        });

        dao = new PhanQuyenDuLieuDAO();

        // Tạo cấu trúc bảng
        try (Statement st = h2Connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS nhom_kinh_doanh (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_nhom VARCHAR(50), ten_nhom VARCHAR(150), mo_ta TEXT, nhom_cha_id BIGINT)");

            st.execute("CREATE TABLE IF NOT EXISTS nguoi_dung (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(150), email VARCHAR(200), mat_khau VARCHAR(255), " +
                    "so_dien_thoai VARCHAR(20), vai_tro VARCHAR(50), nhom_kinh_doanh_id BIGINT, trang_thai VARCHAR(30))");

            st.execute("CREATE TABLE IF NOT EXISTS khach_hang (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_ban_ghi VARCHAR(50), ten_cong_ty VARCHAR(255), tieu_de VARCHAR(255), " +
                    "nguoi_phu_trach_id BIGINT, nguoi_so_huu_id BIGINT, nhom_kinh_doanh_id BIGINT, " +
                    "doanh_thu_uoc_tinh DECIMAL(15, 2), trang_thai VARCHAR(50), mo_ta_chi_tiet TEXT, ngay_tao DATE)");

            st.execute("CREATE TABLE IF NOT EXISTS co_hoi (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_ban_ghi VARCHAR(50), ma_co_hoi VARCHAR(50), ten_co_hoi VARCHAR(255), tieu_de VARCHAR(255), " +
                    "khach_hang_id BIGINT, nguoi_phu_trach_id BIGINT, nhom_kinh_doanh_id BIGINT, " +
                    "gia_tri_du_kien DECIMAL(15, 2), xac_suat INT, trang_thai VARCHAR(50), mo_ta_chi_tiet TEXT, ngay_tao DATE)");

            st.execute("CREATE TABLE IF NOT EXISTS bao_gia (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_ban_ghi VARCHAR(50), ma_bao_gia VARCHAR(50), tieu_de VARCHAR(255), co_hoi_id BIGINT, " +
                    "khach_hang_id BIGINT, nguoi_phu_trach_id BIGINT, nhom_kinh_doanh_id BIGINT, phien_ban INT, " +
                    "tong_tien DECIMAL(15, 2), trang_thai VARCHAR(50), mo_ta_chi_tiet TEXT, ngay_tao DATE)");

            st.execute("CREATE TABLE IF NOT EXISTS hoat_dong (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_ban_ghi VARCHAR(50), tieu_de VARCHAR(255), loai_hoat_dong VARCHAR(50), " +
                    "khach_hang_id BIGINT, co_hoi_id BIGINT, nguoi_phu_trach_id BIGINT, nhom_kinh_doanh_id BIGINT, " +
                    "chi_phi DECIMAL(15, 2), trang_thai VARCHAR(50), mo_ta_chi_tiet TEXT, ngay_tao DATE)");

            // Nạp dữ liệu mẫu
            st.execute("INSERT INTO nhom_kinh_doanh (id, ma_nhom, ten_nhom) VALUES " +
                    "(1, 'KD_BAC', 'Nhóm Miền Bắc'), (2, 'KD_NAM', 'Nhóm Miền Nam')");

            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email, vai_tro, nhom_kinh_doanh_id) VALUES " +
                    "(1, 'Bàn Thị Linh (Giám đốc)', 'director@crm.vn', 'DIRECTOR', NULL), " +
                    "(100, 'Lê Thị Trưởng Nhóm', 'lead.bac@crm.vn', 'TEAM_LEAD', 1), " +
                    "(101, 'Nguyễn Văn A (Sales)', 'sales.a@crm.vn', 'SALES_REP', 1), " +
                    "(102, 'Trần Thị B (Sales)', 'sales.b@crm.vn', 'SALES_REP', 1), " +
                    "(201, 'Lê Văn C (Sales HCM)', 'sales.c@crm.vn', 'SALES_REP', 2)");

            // Khách hàng
            st.execute("INSERT INTO khach_hang (id, ma_ban_ghi, tieu_de, nguoi_phu_trach_id, nhom_kinh_doanh_id, doanh_thu_uoc_tinh, trang_thai, mo_ta_chi_tiet, ngay_tao) VALUES " +
                    "(1, 'KH-001', 'Công ty FPT', 101, 1, 50000000.00, 'Đang hợp tác', 'Khách FPT', CURRENT_DATE), " +
                    "(5, 'KH-002', 'Tập đoàn Viettel', 102, 1, 150000000.00, 'Tiềm năng', 'Khách Viettel', CURRENT_DATE), " +
                    "(9, 'KH-003', 'Công ty VNG', 201, 2, 80000000.00, 'Đang hợp tác', 'Khách VNG', CURRENT_DATE)");

            // Cơ hội
            st.execute("INSERT INTO co_hoi (id, ma_ban_ghi, tieu_de, khach_hang_id, nguoi_phu_trach_id, nhom_kinh_doanh_id, gia_tri_du_kien, trang_thai, mo_ta_chi_tiet, ngay_tao) VALUES " +
                    "(2, 'CH-101', 'CRM cho FPT', 1, 101, 1, 850000000.00, 'Đàm phán', 'Cơ hội FPT', CURRENT_DATE), " +
                    "(6, 'CH-102', 'CRM Viettel IDC', 5, 102, 1, 1200000000.00, 'Khảo sát', 'Cơ hội Viettel', CURRENT_DATE)");

            // Báo giá
            st.execute("INSERT INTO bao_gia (id, ma_ban_ghi, tieu_de, co_hoi_id, khach_hang_id, nguoi_phu_trach_id, nhom_kinh_doanh_id, tong_tien, trang_thai, mo_ta_chi_tiet, ngay_tao) VALUES " +
                    "(3, 'BG-201', 'Báo giá FPT 100 User', 2, 1, 101, 1, 850000000.00, 'Đã gửi', 'Báo giá FPT', CURRENT_DATE), " +
                    "(7, 'BG-202', 'Báo giá Viettel IDC', 6, 5, 102, 1, 1200000000.00, 'Chờ duyệt', 'Báo giá Viettel', CURRENT_DATE)");

            // Hoạt động
            st.execute("INSERT INTO hoat_dong (id, ma_ban_ghi, tieu_de, loai_hoat_dong, khach_hang_id, nguoi_phu_trach_id, nhom_kinh_doanh_id, chi_phi, trang_thai, mo_ta_chi_tiet, ngay_tao) VALUES " +
                    "(4, 'HD-301', 'Demo bảo mật FPT', 'HOP', 1, 101, 1, 500000.00, 'Hoàn thành', 'Demo FPT', CURRENT_DATE), " +
                    "(8, 'HD-302', 'Gọi Viettel IDC', 'GOI', 5, 102, 1, 0.00, 'Hoàn thành', 'Gọi Viettel', CURRENT_DATE)");
        }
    }

    @AfterAll
    static void tearDown() throws Exception {
        DatabaseConfig.resetConnectionSupplier();
        if (h2Connection != null && !h2Connection.isClosed()) {
            h2Connection.close();
        }
    }

    @Test
    @DisplayName("AC4: SQL DAO chỉ lấy khách hàng của chính mình (CA_NHAN), nhân viên A không đọc được khách của B")
    void testDAO_NhanVienA_ChilayDuocBanGhiCuaMinh() throws SQLException {
        // Nhân viên A (ID: 101) truy vấn danh sách khách hàng phạm vi CA_NHAN
        List<BanGhiNghiepVuDTO> danhSachA = dao.layDanhSachTheoBang(
                LoaiNghiepVu.KHACH_HANG, 101L, 1L, PhamViDuLieu.CA_NHAN, null
        );

        assertFalse(danhSachA.isEmpty(), "Nhân viên A phải lấy được khách hàng của chính mình");
        for (BanGhiNghiepVuDTO bg : danhSachA) {
            assertEquals(101L, bg.getNguoiPhuTrachId(), "Mọi bản ghi trả về phải do A phụ trách");
            assertNotEquals(102L, bg.getNguoiPhuTrachId(), "Bản ghi của B (102) tuyệt đối không được xuất hiện trong SQL query của A");
            assertNotEquals(201L, bg.getNguoiPhuTrachId(), "Bản ghi của C (201) tuyệt đối không được xuất hiện trong SQL query của A");
        }
    }

    @Test
    @DisplayName("AC1: SQL DAO lọc theo phạm vi NHOM lấy toàn bộ dữ liệu trong nhóm, không lấy nhóm khác")
    void testDAO_TruongNhom_LayDuocBanGhiTrongNhom() throws SQLException {
        // Trưởng nhóm Miền Bắc (nhóm 1) truy vấn phạm vi NHOM
        List<BanGhiNghiepVuDTO> danhSachNhom = dao.layDanhSachTheoBang(
                LoaiNghiepVu.KHACH_HANG, 100L, 1L, PhamViDuLieu.NHOM, null
        );

        assertEquals(2, danhSachNhom.size(), "Nhóm 1 có 2 khách hàng (FPT của A và Viettel của B)");
        for (BanGhiNghiepVuDTO bg : danhSachNhom) {
            assertEquals(1L, bg.getNhomKinhDoanhId(), "Bản ghi phải thuộc nhóm 1");
            assertNotEquals(2L, bg.getNhomKinhDoanhId(), "Bản ghi không được thuộc nhóm 2 (Miền Nam)");
        }
    }

    @Test
    @DisplayName("AC1: SQL DAO phạm vi TOAN_BO cho phép Giám đốc xem tất cả dữ liệu hệ thống")
    void testDAO_GiamDoc_LayDuocTatCa() throws SQLException {
        List<BanGhiNghiepVuDTO> danhSachToanBo = dao.layDanhSachTheoBang(
                LoaiNghiepVu.KHACH_HANG, 1L, null, PhamViDuLieu.TOAN_BO, null
        );

        assertEquals(3, danhSachToanBo.size(), "Toàn bộ hệ thống có 3 khách hàng mẫu");
    }

    @Test
    @DisplayName("AC2: SQL DAO tìm kiếm từ khóa tuân thủ chặt chẽ Data Scope (WHERE AND)")
    void testDAO_TimKiemTuKhoa_TuanThuPhamVi() throws SQLException {
        // Nhân viên A tìm từ khóa "Viettel" (thuộc B) trong phạm vi CA_NHAN của A
        List<BanGhiNghiepVuDTO> kqTimA = dao.layDanhSachTheoBang(
                LoaiNghiepVu.KHACH_HANG, 101L, 1L, PhamViDuLieu.CA_NHAN, "Viettel"
        );
        assertTrue(kqTimA.isEmpty(), "Tìm kiếm trong SQL không được trả về bản ghi ngoài phạm vi CA_NHAN");

        // Nhân viên B tìm từ khóa "Viettel" trong phạm vi CA_NHAN của B -> Có kết quả
        List<BanGhiNghiepVuDTO> kqTimB = dao.layDanhSachTheoBang(
                LoaiNghiepVu.KHACH_HANG, 102L, 1L, PhamViDuLieu.CA_NHAN, "Viettel"
        );
        assertEquals(1, kqTimB.size());
        assertEquals("KH-002", kqTimB.get(0).getMaBanGhi());
    }

    @Test
    @DisplayName("AC1: SQL DAO truy vấn tổng hợp cả 4 đối tượng nghiệp vụ cốt lõi")
    void testDAO_TruyVanTongHopBonNghiepVu() throws SQLException {
        List<BanGhiNghiepVuDTO> tongHopA = dao.layDanhSachTongHopTheoPhamVi(
                101L, 1L, PhamViDuLieu.CA_NHAN, null, null
        );

        // A có 1 KH, 1 CH, 1 BG, 1 HD = 4 bản ghi
        assertEquals(4, tongHopA.size(), "Nhân viên A phải có tổng cộng 4 bản ghi trải dài 4 loại nghiệp vụ");
        for (BanGhiNghiepVuDTO bg : tongHopA) {
            assertEquals(101L, bg.getNguoiPhuTrachId());
        }
    }

    @Test
    @DisplayName("AC3: Tìm bản ghi theo ID qua DAO")
    void testDAO_TimBanGhiTheoId() throws SQLException {
        BanGhiNghiepVuDTO kh = dao.timBanGhiTheoId(1L, LoaiNghiepVu.KHACH_HANG);
        assertNotNull(kh);
        assertEquals("KH-001", kh.getMaBanGhi());
        assertEquals("Công ty FPT", kh.getTieuDe());
        assertEquals(101L, kh.getNguoiPhuTrachId());
    }

    @Test
    @DisplayName("Cập nhật bản ghi với giá trị tiền tệ DECIMAL")
    void testDAO_CapNhatBanGhi_Decimal() throws SQLException {
        BanGhiNghiepVuDTO ch = dao.timBanGhiTheoId(2L, LoaiNghiepVu.CO_HOI);
        assertNotNull(ch);
        ch.setTieuDe("CRM cho FPT - Gói Enterprise");
        ch.setGiaTri("990000000.00");
        ch.setTrangThai("Thành công");

        boolean ok = dao.capNhatBanGhi(ch);
        assertTrue(ok);

        BanGhiNghiepVuDTO updated = dao.timBanGhiTheoId(2L, LoaiNghiepVu.CO_HOI);
        assertNotNull(updated);
        assertEquals("CRM cho FPT - Gói Enterprise", updated.getTieuDe());
        assertEquals("990000000.00", updated.getGiaTri());
        assertEquals("Thành công", updated.getTrangThai());
    }
}
