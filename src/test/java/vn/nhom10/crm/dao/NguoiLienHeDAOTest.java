package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.LichSuLienHeCongTy;
import vn.nhom10.crm.model.NguoiLienHe;
import vn.nhom10.crm.model.VaiTroQuyetDinhEnum;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử NguoiLienHeDAO - Quản lý người liên hệ & vai trò quyết định mua (Story S3-02)")
class NguoiLienHeDAOTest {

    private static final String H2_URL = "jdbc:h2:mem:crm_s3_02_dao_test;MODE=MySQL;DB_CLOSE_DELAY=-1";
    private static Connection rootConnection;
    private NguoiLienHeDAO dao;

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
            st.execute("CREATE TABLE IF NOT EXISTS khach_hang (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_khach_hang VARCHAR(50), " +
                    "ten_cong_ty VARCHAR(255) NOT NULL, " +
                    "ma_so_thue VARCHAR(50), " +
                    "nganh_nghe_id BIGINT, quy_mo_id BIGINT, khu_vuc_id BIGINT, " +
                    "website VARCHAR(255), dia_chi VARCHAR(500), " +
                    "nguoi_so_huu_id BIGINT NOT NULL, " +
                    "nhom_kinh_doanh_id BIGINT, " +
                    "trang_thai VARCHAR(50) NOT NULL, " +
                    "doanh_thu_uoc_tinh DECIMAL(15,2), " +
                    "mo_ta_chi_tiet TEXT, " +
                    "created_at DATETIME, updated_at DATETIME)");

            st.execute("CREATE TABLE IF NOT EXISTS nguoi_lien_he (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "khach_hang_id BIGINT NOT NULL, " +
                    "ho_ten VARCHAR(150) NOT NULL, " +
                    "chuc_danh VARCHAR(150), " +
                    "email VARCHAR(255), " +
                    "so_dien_thoai VARCHAR(20), " +
                    "vai_tro_quyet_dinh VARCHAR(50), " +
                    "la_dau_moi_chinh TINYINT(1) DEFAULT 0, " +
                    "trang_thai VARCHAR(30) DEFAULT 'DANG_HOAT_DONG', " +
                    "created_at DATETIME, updated_at DATETIME)");

            st.execute("CREATE TABLE IF NOT EXISTS lich_su_lien_he_cong_ty (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "nguoi_lien_he_id BIGINT NOT NULL, " +
                    "khach_hang_id BIGINT NOT NULL, " +
                    "chuc_danh VARCHAR(150), " +
                    "vai_tro_quyet_dinh VARCHAR(50), " +
                    "tu_ngay DATE, " +
                    "den_ngay DATE, " +
                    "ghi_chu VARCHAR(500), " +
                    "created_at DATETIME)");
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
    void cleanAndSeedData() throws Exception {
        dao = new NguoiLienHeDAO();
        try (Statement st = rootConnection.createStatement()) {
            st.execute("DELETE FROM lich_su_lien_he_cong_ty");
            st.execute("DELETE FROM nguoi_lien_he");
            st.execute("DELETE FROM khach_hang");

            // Seed 2 công ty test
            st.execute("INSERT INTO khach_hang (id, ma_khach_hang, ten_cong_ty, ma_so_thue, nguoi_so_huu_id, trang_thai) " +
                    "VALUES (1, 'KH-001', 'Công ty FPT Software', '0101234567', 101, 'TIEM_NANG')");
            st.execute("INSERT INTO khach_hang (id, ma_khach_hang, ten_cong_ty, ma_so_thue, nguoi_so_huu_id, trang_thai) " +
                    "VALUES (2, 'KH-002', 'Tập đoàn Viettel Telecom', '0107654321', 102, 'KHACH_HANG')");
        }
    }

    @Test
    @DisplayName("AC1: Mỗi khách hàng có nhiều người liên hệ với chức danh, email, số điện thoại")
    void testThemNhieuNguoiLienHeChoKhachHang() {
        NguoiLienHe nlh1 = new NguoiLienHe(null, 1L, "Nguyễn Văn An", "Trưởng phòng Mua sắm", "an.nv@fpt.com", "0912345678", VaiTroQuyetDinhEnum.NGUOI_ANH_HUONG, true);
        NguoiLienHe nlh2 = new NguoiLienHe(null, 1L, "Trần Thị Bình", "Chuyên viên Kỹ thuật", "binh.tt@fpt.com", "0987654321", VaiTroQuyetDinhEnum.NGUOI_DUNG_CUOI, false);

        long id1 = dao.themNguoiLienHe(nlh1);
        long id2 = dao.themNguoiLienHe(nlh2);

        assertTrue(id1 > 0);
        assertTrue(id2 > 0);

        List<NguoiLienHe> list = dao.layDanhSachTheoKhachHang(1L);
        assertEquals(2, list.size());
        assertEquals("Nguyễn Văn An", list.get(0).getHoTen());
        assertTrue(list.get(0).isLaDauMoiChinh(), "Đầu mối chính phải được sắp xếp lên trước");
        assertEquals("Trần Thị Bình", list.get(1).getHoTen());
    }

    @Test
    @DisplayName("AC2: Đánh dấu các vai trò trong quyết định mua (quyết định, ảnh hưởng, dùng cuối, cản trở)")
    void testDanhDauCacVaiTroQuyetDinhMua() {
        NguoiLienHe nlhQuyetDinh = new NguoiLienHe(null, 1L, "Phạm Quyết Định", "CEO", "ceo@fpt.com", "0901111111", VaiTroQuyetDinhEnum.NGUOI_QUYET_DINH, false);
        NguoiLienHe nlhCanTro = new NguoiLienHe(null, 1L, "Vũ Cản Trở", "Kế toán trưởng", "ktt@fpt.com", "0902222222", VaiTroQuyetDinhEnum.NGUOI_CAN_TRO, false);

        long id1 = dao.themNguoiLienHe(nlhQuyetDinh);
        long id2 = dao.themNguoiLienHe(nlhCanTro);

        Optional<NguoiLienHe> opt1 = dao.timTheoId(id1);
        Optional<NguoiLienHe> opt2 = dao.timTheoId(id2);

        assertTrue(opt1.isPresent());
        assertEquals(VaiTroQuyetDinhEnum.NGUOI_QUYET_DINH, opt1.get().getVaiTroQuyetDinh());
        assertEquals("Người quyết định", opt1.get().getTenVaiTroHienThi());

        assertTrue(opt2.isPresent());
        assertEquals(VaiTroQuyetDinhEnum.NGUOI_CAN_TRO, opt2.get().getVaiTroQuyetDinh());
        assertEquals("Người cản trở", opt2.get().getTenVaiTroHienThi());
    }

    @Test
    @DisplayName("AC3: Đánh dấu một người là đầu mối chính và duy nhất 1 đầu mối chính tại một thời điểm")
    void testDatLamDauMoiChinh_TuDongResetNguoiCu() {
        NguoiLienHe nlh1 = new NguoiLienHe(null, 1L, "Người A", "Trưởng phòng", "a@fpt.com", "0911", VaiTroQuyetDinhEnum.NGUOI_ANH_HUONG, true);
        NguoiLienHe nlh2 = new NguoiLienHe(null, 1L, "Người B", "Phó phòng", "b@fpt.com", "0922", VaiTroQuyetDinhEnum.NGUOI_DUNG_CUOI, false);

        long id1 = dao.themNguoiLienHe(nlh1);
        long id2 = dao.themNguoiLienHe(nlh2);

        // Ban đầu A là đầu mối chính
        assertTrue(dao.timTheoId(id1).get().isLaDauMoiChinh());
        assertFalse(dao.timTheoId(id2).get().isLaDauMoiChinh());

        // Đổi B làm đầu mối chính
        boolean updated = dao.datLamDauMoiChinh(id2, 1L);
        assertTrue(updated);

        // B trở thành đầu mối chính, A bị hủy cờ đầu mối chính
        assertFalse(dao.timTheoId(id1).get().isLaDauMoiChinh(), "Người A phải mất cờ đầu mối chính");
        assertTrue(dao.timTheoId(id2).get().isLaDauMoiChinh(), "Người B phải là đầu mối chính mới");
    }

    @Test
    @DisplayName("AC4: Chuyển người liên hệ sang công ty khác thì gắn lại sang khách hàng mới, giữ nguyên lịch sử")
    void testChuyenCongTy_GiuNguyenLichSuLamViec() {
        // Tạo liên hệ tại FPT (khách hàng id=1)
        NguoiLienHe nlh = new NguoiLienHe(null, 1L, "Nguyễn Chuyển Việc", "Chuyên viên Mua sắm", "chuyen@fpt.com", "0933333333", VaiTroQuyetDinhEnum.NGUOI_ANH_HUONG, true);
        long nlhId = dao.themNguoiLienHe(nlh);

        // Chuyển sang Viettel (khách hàng id=2) với chức danh và vai trò mới
        boolean ok = dao.chuyenCongTy(nlhId, 2L, "Trưởng ban Mua sắm Viettel", VaiTroQuyetDinhEnum.NGUOI_QUYET_DINH, "Chuyển công tác từ FPT sang Viettel");
        assertTrue(ok);

        // Kiểm tra thông tin người liên hệ hiện tại
        NguoiLienHe current = dao.timTheoId(nlhId).get();
        assertEquals(2L, current.getKhachHangId(), "Khách hàng hiện tại phải là Viettel (ID=2)");
        assertEquals("Trưởng ban Mua sắm Viettel", current.getChucDanh());
        assertEquals(VaiTroQuyetDinhEnum.NGUOI_QUYET_DINH, current.getVaiTroQuyetDinh());
        assertFalse(current.isLaDauMoiChinh(), "Chuyển sang công ty mới mặc định chưa phải đầu mối chính");

        // Kiểm tra lịch sử công tác (AC4)
        List<LichSuLienHeCongTy> history = dao.layLichSuCongTy(nlhId);
        assertEquals(2, history.size(), "Phải có đúng 2 giai đoạn công tác được ghi nhận");

        // Bản ghi 1 (mới nhất): Viettel, đang làm việc (denNgay == null)
        LichSuLienHeCongTy latest = history.get(0);
        assertEquals(2L, latest.getKhachHangId());
        assertEquals("Tập đoàn Viettel Telecom", latest.getTenCongTy());
        assertEquals("Trưởng ban Mua sắm Viettel", latest.getChucDanh());
        assertEquals(VaiTroQuyetDinhEnum.NGUOI_QUYET_DINH, latest.getVaiTroQuyetDinh());
        assertNull(latest.getDenNgay(), "Giai đoạn mới chưa có ngày kết thúc");

        // Bản ghi 2 (cũ): FPT, đã kết thúc (denNgay != null)
        LichSuLienHeCongTy oldJob = history.get(1);
        assertEquals(1L, oldJob.getKhachHangId());
        assertEquals("Công ty FPT Software", oldJob.getTenCongTy());
        assertNotNull(oldJob.getDenNgay(), "Giai đoạn cũ phải có ngày kết thúc");
    }

    @Test
    @DisplayName("AC4: Chặn chuyển sang chính công ty hiện tại")
    void testChuyenCongTy_KhongChoChuyenCungCongTy() {
        NguoiLienHe nlh = new NguoiLienHe(null, 1L, "Lê Văn Tồn", "Nhân viên", "ton@fpt.com", "0944444444", null, false);
        long nlhId = dao.themNguoiLienHe(nlh);

        assertThrows(IllegalArgumentException.class, () -> {
            dao.chuyenCongTy(nlhId, 1L, "Chức danh mới", VaiTroQuyetDinhEnum.NGUOI_QUYET_DINH, "Chuyển trùng công ty");
        });
    }

    @Test
    @DisplayName("AC1: Cập nhật thông tin người liên hệ thành công")
    void testCapNhatThongTinNguoiLienHe() {
        NguoiLienHe nlh = new NguoiLienHe(null, 1L, "Nguyễn Văn Cũ", "Nhân viên", "cu@fpt.com", "0955555555", VaiTroQuyetDinhEnum.NGUOI_DUNG_CUOI, false);
        long id = dao.themNguoiLienHe(nlh);

        nlh.setId(id);
        nlh.setHoTen("Nguyễn Văn Mới");
        nlh.setChucDanh("Giám đốc Kinh doanh");
        nlh.setEmail("moi@fpt.com");
        nlh.setSoDienThoai("0966666666");
        nlh.setVaiTroQuyetDinh(VaiTroQuyetDinhEnum.NGUOI_QUYET_DINH);

        boolean updated = dao.capNhatThongTin(nlh);
        assertTrue(updated);

        NguoiLienHe loaded = dao.timTheoId(id).get();
        assertEquals("Nguyễn Văn Mới", loaded.getHoTen());
        assertEquals("Giám đốc Kinh doanh", loaded.getChucDanh());
        assertEquals("moi@fpt.com", loaded.getEmail());
        assertEquals("0966666666", loaded.getSoDienThoai());
        assertEquals(VaiTroQuyetDinhEnum.NGUOI_QUYET_DINH, loaded.getVaiTroQuyetDinh());
    }
}
