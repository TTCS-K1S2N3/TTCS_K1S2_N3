package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dto.KhachHang360DTO;
import vn.nhom10.crm.model.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử KhachHang360DAO với H2 in-memory (AC1, AC2, AC3)")
class KhachHang360DAOTest {

    private Connection connection;
    private KhachHang360DAO khachHang360DAO;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:test_khach_hang_360;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection("jdbc:h2:mem:test_khach_hang_360;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        try (Statement st = connection.createStatement()) {
            st.execute("DROP ALL OBJECTS");

            st.execute("CREATE TABLE nhom_kinh_doanh (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_nhom VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_nhom VARCHAR(255) NOT NULL" +
                    ")");

            st.execute("CREATE TABLE nguoi_dung (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(150) NOT NULL, " +
                    "email VARCHAR(200) NOT NULL, " +
                    "nhom_kinh_doanh_id BIGINT" +
                    ")");

            st.execute("CREATE TABLE nganh_nghe (id BIGINT AUTO_INCREMENT PRIMARY KEY, ma_nganh VARCHAR(50), ten_nganh VARCHAR(255))");
            st.execute("CREATE TABLE quy_mo_doanh_nghiep (id BIGINT AUTO_INCREMENT PRIMARY KEY, ma_quy_mo VARCHAR(50), ten_quy_mo VARCHAR(255))");
            st.execute("CREATE TABLE khu_vuc_dia_ly (id BIGINT AUTO_INCREMENT PRIMARY KEY, ma_khu_vuc VARCHAR(50), ten_khu_vuc VARCHAR(255))");
            st.execute("CREATE TABLE giai_doan_pipeline (id BIGINT AUTO_INCREMENT PRIMARY KEY, ma_giai_doan VARCHAR(50), ten_giai_doan VARCHAR(255))");
            st.execute("CREATE TABLE ly_do_thang_thua (id BIGINT AUTO_INCREMENT PRIMARY KEY, ma_ly_do VARCHAR(50), ten_ly_do VARCHAR(255))");

            st.execute("CREATE TABLE khach_hang (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_khach_hang VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_cong_ty VARCHAR(255) NOT NULL, " +
                    "ten_chuan_hoa VARCHAR(255), " +
                    "ma_so_thue VARCHAR(50), " +
                    "nganh_nghe_id BIGINT, " +
                    "quy_mo_id BIGINT, " +
                    "website VARCHAR(255), " +
                    "website_chuan_hoa VARCHAR(255), " +
                    "dia_chi TEXT, " +
                    "khu_vuc_id BIGINT, " +
                    "nguoi_so_huu_id BIGINT, " +
                    "nhom_kinh_doanh_id BIGINT, " +
                    "doanh_thu_uoc_tinh DECIMAL(18,2) DEFAULT 0, " +
                    "cong_ty_me_id BIGINT, " +
                    "trang_thai VARCHAR(50) DEFAULT 'TIEM_NANG', " +
                    "co_rui_ro BOOLEAN DEFAULT FALSE, " +
                    "rui_ro_cap_nhat_luc TIMESTAMP, " +
                    "lan_tuong_tac_cuoi TIMESTAMP, " +
                    "gop_vao_khach_hang_id BIGINT, " +
                    "mo_ta_chi_tiet TEXT, " +
                    "ngay_tao DATE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            st.execute("CREATE TABLE nguoi_lien_he (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "khach_hang_id BIGINT NOT NULL, " +
                    "ho_ten VARCHAR(150) NOT NULL, " +
                    "chuc_danh VARCHAR(100), " +
                    "email VARCHAR(150), " +
                    "so_dien_thoai VARCHAR(50), " +
                    "vai_tro_quyet_dinh VARCHAR(50) DEFAULT 'NGUOI_ANH_HUONG', " +
                    "la_dau_moi_chinh BOOLEAN DEFAULT FALSE, " +
                    "trang_thai VARCHAR(50) DEFAULT 'DANG_HOAT_DONG', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            st.execute("CREATE TABLE co_hoi (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_co_hoi VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_co_hoi VARCHAR(255) NOT NULL, " +
                    "khach_hang_id BIGINT NOT NULL, " +
                    "nguoi_lien_he_chinh_id BIGINT, " +
                    "lead_id BIGINT, " +
                    "chien_dich_id BIGINT, " +
                    "nguon_lead_id BIGINT, " +
                    "giai_doan_id BIGINT, " +
                    "nguoi_phu_trach_id BIGINT, " +
                    "nhom_kinh_doanh_id BIGINT, " +
                    "gia_tri_du_kien DECIMAL(18,2) DEFAULT 0, " +
                    "xac_suat INT DEFAULT 50, " +
                    "ly_do_sua_xac_suat VARCHAR(255), " +
                    "ngay_chot_du_kien DATE, " +
                    "trang_thai VARCHAR(50) DEFAULT 'MO', " +
                    "gia_tri_chot_thuc_te DECIMAL(18,2) DEFAULT 0, " +
                    "ngay_ky DATE, " +
                    "ly_do_thang_thua_id BIGINT, " +
                    "doi_thu_id BIGINT, " +
                    "ngay_hoat_dong_cuoi TIMESTAMP, " +
                    "bi_dinh_tre BOOLEAN DEFAULT FALSE, " +
                    "mo_ta_chi_tiet TEXT, " +
                    "ngay_tao DATE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            st.execute("CREATE TABLE hop_dong (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "so_hop_dong VARCHAR(100) NOT NULL UNIQUE, " +
                    "bao_gia_id BIGINT, " +
                    "phien_ban_bao_gia_id BIGINT, " +
                    "co_hoi_id BIGINT, " +
                    "khach_hang_id BIGINT NOT NULL, " +
                    "ngay_ky DATE, " +
                    "ngay_hieu_luc DATE, " +
                    "ngay_het_han DATE, " +
                    "gia_tri_hop_dong DECIMAL(18,2) NOT NULL, " +
                    "dieu_khoan_thanh_toan VARCHAR(255), " +
                    "trang_thai VARCHAR(50) DEFAULT 'DA_KY', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            st.execute("CREATE TABLE hoat_dong (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_hoat_dong VARCHAR(50) NOT NULL UNIQUE, " +
                    "tieu_de VARCHAR(255) NOT NULL, " +
                    "loai_hoat_dong VARCHAR(50) NOT NULL, " +
                    "lead_id BIGINT, " +
                    "khach_hang_id BIGINT NOT NULL, " +
                    "nguoi_lien_he_id BIGINT, " +
                    "co_hoi_id BIGINT, " +
                    "nguoi_phu_trach_id BIGINT, " +
                    "nhom_kinh_doanh_id BIGINT, " +
                    "chi_phi DECIMAL(18,2) DEFAULT 0, " +
                    "thoi_gian_bat_dau TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "thoi_gian_ket_thuc TIMESTAMP, " +
                    "trang_thai VARCHAR(50) DEFAULT 'HOAN_THANH', " +
                    "mo_ta_chi_tiet TEXT, " +
                    "noi_dung TEXT, " +
                    "ket_qua TEXT, " +
                    "thoi_luong_phut INT, " +
                    "la_ghi_nhan_qua_khu BOOLEAN DEFAULT FALSE, " +
                    "la_ghi_nhan_nhanh BOOLEAN DEFAULT FALSE, " +
                    "ngay_tao DATE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            st.execute("CREATE TABLE tep_dinh_kem (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "khach_hang_id BIGINT NOT NULL, " +
                    "co_hoi_id BIGINT, " +
                    "hoat_dong_id BIGINT, " +
                    "bao_gia_id BIGINT, " +
                    "hop_dong_id BIGINT, " +
                    "loai_tep VARCHAR(50), " +
                    "ten_file_goc VARCHAR(255) NOT NULL, " +
                    "ten_file_luu VARCHAR(255), " +
                    "duong_dan VARCHAR(500) NOT NULL, " +
                    "mime_type VARCHAR(100), " +
                    "kich_thuoc_byte BIGINT DEFAULT 0, " +
                    "nguoi_tai_len_id BIGINT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // Seed master data
            st.execute("INSERT INTO nhom_kinh_doanh (id, ma_nhom, ten_nhom) VALUES (1, 'KD_ENTERPRISE', 'Khối Doanh nghiệp Lớn')");
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email, nhom_kinh_doanh_id) VALUES (1, 'Nguyễn Văn A', 'a.nguyen@crm.vn', 1)");

            // Seed Khách hàng
            st.execute("INSERT INTO khach_hang (id, ma_khach_hang, ten_cong_ty, ma_so_thue, website, dia_chi, nguoi_so_huu_id, nhom_kinh_doanh_id) " +
                    "VALUES (10, 'KH-0010', 'Tập đoàn FPT', '0101234567', 'fpt.com.vn', 'Cầu Giấy, Hà Nội', 1, 1)");

            // Seed Người liên hệ
            st.execute("INSERT INTO nguoi_lien_he (id, khach_hang_id, ho_ten, chuc_danh, email, so_dien_thoai, vai_tro_quyet_dinh, la_dau_moi_chinh) " +
                    "VALUES (1, 10, 'Nguyễn Đức Mạnh', 'CTO', 'manh.nd@fpt.com.vn', '0912345678', 'NGUOI_QUYET_DINH', TRUE)");
            st.execute("INSERT INTO nguoi_lien_he (id, khach_hang_id, ho_ten, chuc_danh, email, so_dien_thoai, vai_tro_quyet_dinh, la_dau_moi_chinh) " +
                    "VALUES (2, 10, 'Trần Mai Linh', 'Procurement', 'linh.tm@fpt.com.vn', '0988765432', 'NGUOI_ANH_HUONG', FALSE)");

            // Seed Cơ hội (1 mở 500tr, 1 đóng thắng 350tr)
            st.execute("INSERT INTO co_hoi (id, ma_co_hoi, ten_co_hoi, khach_hang_id, giai_doan_id, gia_tri_du_kien, gia_tri_chot_thuc_te, xac_suat, trang_thai, nguoi_phu_trach_id) " +
                    "VALUES (1, 'CH-001', 'Cloud CRM Enterprise', 10, 1, 500000000, 0, 80, 'MO', 1)");
            st.execute("INSERT INTO co_hoi (id, ma_co_hoi, ten_co_hoi, khach_hang_id, giai_doan_id, gia_tri_du_kien, gia_tri_chot_thuc_te, xac_suat, trang_thai, nguoi_phu_trach_id) " +
                    "VALUES (2, 'CH-002', 'Tư vấn bán hàng B2B', 10, 1, 350000000, 350000000, 100, 'DONG_THANG', 1)");

            // Seed Hợp đồng đã ký (500tr)
            st.execute("INSERT INTO hop_dong (id, so_hop_dong, khach_hang_id, gia_tri_hop_dong, trang_thai) " +
                    "VALUES (1, 'HD-2026-001', 10, 500000000, 'DA_KY')");

            // Seed Tệp đính kèm
            st.execute("INSERT INTO tep_dinh_kem (id, khach_hang_id, ten_file_goc, duong_dan, loai_tep, kich_thuoc_byte, nguoi_tai_len_id) " +
                    "VALUES (1, 10, 'Hop_dong_2026.pdf', '/uploads/hd.pdf', 'HOP_DONG', 1048576, 1)");
        }

        khachHang360DAO = new KhachHang360DAO();
    }

    @AfterEach
    void tearDown() throws Exception {
        DatabaseConfig.resetConnectionSupplier();
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @DisplayName("AC1: Lấy đầy đủ 5 khối thông tin của khách hàng 360")
    void testTimThongTinCongTyVaCacKhoiLienQuan() throws Exception {
        KhachHang kh = khachHang360DAO.timKhachHangTheoId(10L);
        assertNotNull(kh);
        assertEquals("KH-0010", kh.getMaKhachHang());
        assertEquals("Tập đoàn FPT", kh.getTenCongTy());
        assertEquals("Nguyễn Văn A", kh.getTenNguoiSoHuu());
        assertEquals("Khối Doanh nghiệp Lớn", kh.getTenNhomKinhDoanh());

        List<NguoiLienHe> contacts = khachHang360DAO.layDsNguoiLienHe(10L);
        assertEquals(2, contacts.size());
        assertTrue(contacts.get(0).isLaDauMoiChinh());
        assertEquals("NGUOI_QUYET_DINH", contacts.get(0).getVaiTroQuyetDinh());

        List<CoHoi> deals = khachHang360DAO.layDsCoHoi(10L);
        assertEquals(2, deals.size());

        List<TepDinhKem> files = khachHang360DAO.layDsTepDinhKem(10L);
        assertEquals(1, files.size());
        assertEquals("Hop_dong_2026.pdf", files.get(0).getTenFileGoc());
    }

    @Test
    @DisplayName("AC2: Tính toán chính xác tổng giá trị đã ký và giá trị cơ hội đang mở")
    void testTinhTongGiaTriKpi() throws Exception {
        BigDecimal daKy = khachHang360DAO.tinhTongGiaTriDaKy(10L);
        assertEquals(new BigDecimal("500000000.00"), daKy);

        BigDecimal dangMo = khachHang360DAO.tinhTongGiaTriCoHoiDangMo(10L);
        assertEquals(new BigDecimal("500000000.00"), dangMo);
    }

    @Test
    @DisplayName("AC3: Tải và xử lý 500 hoạt động dưới 1.5 giây (hiệu năng cao)")
    void testHieuNangTai500HoatDong() throws Exception {
        // Seed 500 hoạt động vào H2
        try (Statement st = connection.createStatement()) {
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO hoat_dong (ma_hoat_dong, khach_hang_id, loai_hoat_dong, tieu_de, noi_dung, nguoi_phu_trach_id) VALUES ");
            for (int i = 1; i <= 500; i++) {
                if (i > 1) sb.append(", ");
                sb.append("('HD_PERF_").append(i).append("', 10, 'CUOC_GOI', 'Hoạt động trao đổi số ").append(i).append("', 'Nội dung chi tiết trao đổi số ").append(i).append("', 1)");
            }
            st.execute(sb.toString());
        }

        long start = System.currentTimeMillis();
        List<HoatDong> dsHoatDong = khachHang360DAO.layDsHoatDong(10L, 500, 0);
        long elapsed = System.currentTimeMillis() - start;

        assertEquals(500, dsHoatDong.size());
        // Tiêu chuẩn AC3: Tải xong dưới 1,5 giây (1500 ms) với 500 hoạt động
        assertTrue(elapsed < 1500, "Thời gian tải 500 hoạt động phải < 1500ms, thực tế: " + elapsed + "ms");
    }

    @Test
    @DisplayName("Ghi nhận hoạt động tương tác mới thành công")
    void testThemHoatDongMoi() throws Exception {
        HoatDong hd = new HoatDong();
        hd.setMaHoatDong("HD_TEST_NEW");
        hd.setKhachHangId(10L);
        hd.setLoaiHoatDong("CUOC_HOP");
        hd.setTieuDe("Họp thẩm định bảo mật");
        hd.setNoiDung("Khách hàng đồng ý phương án");
        hd.setNguoiPhuTrachId(1L);

        Long id = khachHang360DAO.themHoatDong(hd);
        assertNotNull(id);
        assertTrue(id > 0);

        List<HoatDong> list = khachHang360DAO.layDsHoatDong(10L, 10, 0);
        assertFalse(list.isEmpty());
        assertEquals("Họp thẩm định bảo mật", list.get(0).getTieuDe());
    }
}
