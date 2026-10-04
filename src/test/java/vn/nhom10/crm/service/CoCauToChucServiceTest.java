package vn.nhom10.crm.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dao.KhuVucDiaLyDAO;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dao.NhomKinhDoanhDAO;
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.model.KhuVucDiaLy;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhomKinhDoanh;
import vn.nhom10.crm.model.PhamViDuLieu;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử nghiệp vụ Cơ cấu tổ chức kinh doanh - Story S2-06 (AC1, AC2, AC3, AC4)")
class CoCauToChucServiceTest {

    private static Connection h2Connection;
    private static CoCauToChucService service;
    private static NhomKinhDoanhDAO nhomDAO;
    private static KhuVucDiaLyDAO khuVucDAO;
    private static NguoiDungDAO nguoiDungDAO;

    @BeforeAll
    static void setUp() throws Exception {
        h2Connection = DriverManager.getConnection("jdbc:h2:mem:crm_cctc_service_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                if (h2Connection.isClosed()) {
                    h2Connection = DriverManager.getConnection("jdbc:h2:mem:crm_cctc_service_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
                }
            } catch (SQLException ignored) {}
            return h2Connection;
        });

        nhomDAO = new NhomKinhDoanhDAO();
        khuVucDAO = new KhuVucDiaLyDAO();
        nguoiDungDAO = new NguoiDungDAO();
        service = new CoCauToChucService(nhomDAO, khuVucDAO, nguoiDungDAO);

        try (Statement st = h2Connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS khu_vuc_dia_ly (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_khu_vuc VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_khu_vuc VARCHAR(150) NOT NULL, " +
                    "loai_khu_vuc VARCHAR(50), " +
                    "khu_vuc_cha_id BIGINT, " +
                    "thuTuHienThi INT DEFAULT 0, " +
                    "thu_tu_hien_thi INT DEFAULT 0, " +
                    "hoat_dong BOOLEAN DEFAULT TRUE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS nguoi_dung (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(150), email VARCHAR(255) NOT NULL UNIQUE, mat_khau VARCHAR(255), " +
                    "so_dien_thoai VARCHAR(20), trang_thai VARCHAR(30) DEFAULT 'HOAT_DONG', " +
                    "so_lan_sai INT DEFAULT 0, thoi_gian_khoa TIMESTAMP, nhom_kinh_doanh_id BIGINT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS nhom_kinh_doanh (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_nhom VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_nhom VARCHAR(150) NOT NULL, " +
                    "mo_ta TEXT, " +
                    "nhom_cha_id BIGINT, " +
                    "khu_vuc_id BIGINT, " +
                    "truong_nhom_id BIGINT, " +
                    "hoat_dong BOOLEAN DEFAULT TRUE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS vai_tro (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_vai_tro VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_vai_tro VARCHAR(100) NOT NULL, " +
                    "mo_ta VARCHAR(500), " +
                    "pham_vi_toi_da VARCHAR(50) DEFAULT 'CA_NHAN')");

            st.execute("CREATE TABLE IF NOT EXISTS nguoi_dung_vai_tro (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "nguoi_dung_id BIGINT NOT NULL, " +
                    "vai_tro_id BIGINT NOT NULL)");

            // Seed user
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email, trang_thai) VALUES (10, 'Giám đốc Bắc', 'giamdoc_bac@crm.vn', 'HOAT_DONG')");
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email, trang_thai) VALUES (11, 'Trưởng nhóm HN1', 'lead_hn1@crm.vn', 'HOAT_DONG')");
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email, trang_thai) VALUES (12, 'Trưởng nhóm HN2', 'lead_hn2@crm.vn', 'HOAT_DONG')");
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email, trang_thai) VALUES (20, 'Trưởng nhóm MN', 'lead_mn@crm.vn', 'HOAT_DONG')");
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email, trang_thai) VALUES (101, 'Nhân viên Sales HN1', 'sales_hn1@crm.vn', 'HOAT_DONG')");
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email, trang_thai) VALUES (102, 'Nhân viên Sales HN2', 'sales_hn2@crm.vn', 'HOAT_DONG')");
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email, trang_thai) VALUES (103, 'Nhân viên Sales Cầu Giấy', 'sales_cg@crm.vn', 'HOAT_DONG')");
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email, trang_thai) VALUES (201, 'Nhân viên Sales HCM', 'sales_hcm@crm.vn', 'HOAT_DONG')");
        }
    }

    @AfterAll
    static void tearDown() throws Exception {
        if (h2Connection != null && !h2Connection.isClosed()) {
            h2Connection.close();
        }
    }

    @Test
    @DisplayName("AC1, AC4: Khai báo nhóm kinh doanh, cấu trúc cây và khu vực địa lý")
    void testThemNhomVaKhuVuc() {
        // Khai báo khu vực (AC4)
        KhuVucDiaLy kvMienBac = service.themKhuVucDiaLy("KV_BAC", "Vùng Bắc Bộ", "MIEN", null, 1);
        assertNotNull(kvMienBac);
        assertTrue(kvMienBac.getId() > 0);

        // Khai báo nhóm cấp 1 (Root)
        NhomKinhDoanh khoiBac = service.themNhomKinhDoanh("KHOI_BAC", "Khối Kinh Doanh Miền Bắc", "Quản lý toàn miền Bắc", null, kvMienBac.getId(), 10L);
        assertNotNull(khoiBac);
        assertTrue(khoiBac.getIdLong() > 0);

        // Khai báo nhóm cấp 2 (Con của Khối Bắc)
        NhomKinhDoanh phongHN1 = service.themNhomKinhDoanh("PHONG_HN1", "Phòng Kinh Doanh HN 1", "Địa bàn Hà Nội 1", khoiBac.getIdLong(), kvMienBac.getId(), 11L);
        assertNotNull(phongHN1);
        assertEquals(khoiBac.getIdLong(), phongHN1.getNhomChaIdLong());

        // Kiểm tra danh sách cây
        List<NhomKinhDoanh> cay = service.layCayNhomKinhDoanh();
        assertFalse(cay.isEmpty());
    }

    @Test
    @DisplayName("AC1: Chống vòng lặp cây tổ chức (Cycle Detection)")
    void testChongVongLapCayToChuc() {
        NhomKinhDoanh nhomX = service.themNhomKinhDoanh("NHOM_X", "Nhóm X", "Mô tả", null, null, null);
        NhomKinhDoanh nhomY = service.themNhomKinhDoanh("NHOM_Y", "Nhóm Y", "Mô tả", nhomX.getIdLong(), null, null);
        NhomKinhDoanh nhomZ = service.themNhomKinhDoanh("NHOM_Z", "Nhóm Z", "Mô tả", nhomY.getIdLong(), null, null);

        // 1. Không thể tự chọn chính mình làm nhóm cha
        assertThrows(IllegalArgumentException.class, () -> {
            service.capNhatNhomKinhDoanh(nhomX.getIdLong(), "Nhóm X", "", nhomX.getIdLong(), null, null, true);
        });

        // 2. Không thể chọn nhóm con/cháu (Y hoặc Z) làm nhóm cha của X (gây lặp vòng X -> Y -> Z -> X)
        assertThrows(IllegalArgumentException.class, () -> {
            service.capNhatNhomKinhDoanh(nhomX.getIdLong(), "Nhóm X", "", nhomY.getIdLong(), null, null, true);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            service.capNhatNhomKinhDoanh(nhomX.getIdLong(), "Nhóm X", "", nhomZ.getIdLong(), null, null, true);
        });
    }

    @Test
    @DisplayName("AC2: Mỗi nhân viên thuộc đúng một nhóm tại một thời điểm")
    void testMoiNhanVienThuocDungMotNhom() {
        NhomKinhDoanh nA = service.themNhomKinhDoanh("TEAM_ALPHA", "Alpha", "", null, null, null);
        NhomKinhDoanh nB = service.themNhomKinhDoanh("TEAM_BETA", "Beta", "", null, null, null);

        // Gán nhân viên 101 vào Alpha
        boolean ok1 = service.chuyenNhomNhanVien(101L, nA.getIdLong());
        assertTrue(ok1);
        List<NguoiDung> memA = service.layDsNhanVienThuocNhom(nA.getIdLong());
        assertTrue(memA.stream().anyMatch(u -> u.getId() == 101L));

        // Chuyển nhân viên 101 sang Beta
        boolean ok2 = service.chuyenNhomNhanVien(101L, nB.getIdLong());
        assertTrue(ok2);

        // Xác nhận nhân viên 101 KHÔNG còn ở Alpha và CHỈ ở Beta
        List<NguoiDung> memASau = service.layDsNhanVienThuocNhom(nA.getIdLong());
        assertFalse(memASau.stream().anyMatch(u -> u.getId() == 101L));

        List<NguoiDung> memBSau = service.layDsNhanVienThuocNhom(nB.getIdLong());
        assertTrue(memBSau.stream().anyMatch(u -> u.getId() == 101L));
    }

    @Test
    @DisplayName("AC3: Cây tổ chức quyết định phạm vi dữ liệu mà Trưởng nhóm nhìn thấy")
    void testCayToChucQuyetDinhPhamViDuLieuTruongNhom() {
        // Thiết lập cây:
        // Cấp 1: Root Bắc (Leader: 10)
        // Cấp 2: HN 1 (Leader: 11) [con Root Bắc], HN 2 (Leader: 12) [con Root Bắc]
        // Cấp 3: Cầu Giấy (thuộc HN 1)
        // Cấp 1 khác: Miền Nam (Leader: 20)
        NhomKinhDoanh rootBac = service.themNhomKinhDoanh("TREE_BAC", "Vùng Bắc", "", null, null, 10L);
        NhomKinhDoanh subHN1 = service.themNhomKinhDoanh("TREE_HN1", "Hà Nội 1", "", rootBac.getIdLong(), null, 11L);
        NhomKinhDoanh subHN2 = service.themNhomKinhDoanh("TREE_HN2", "Hà Nội 2", "", rootBac.getIdLong(), null, 12L);
        NhomKinhDoanh subCG = service.themNhomKinhDoanh("TREE_CG", "Tổ Cầu Giấy", "", subHN1.getIdLong(), null, null);
        NhomKinhDoanh rootNam = service.themNhomKinhDoanh("TREE_NAM", "Vùng Nam", "", null, null, 20L);

        // 1. Trưởng nhóm cấp 1 (User 10 - Vùng Bắc):
        // Phải nhìn thấy: Vùng Bắc, HN1, HN2, Cầu Giấy (4 nhóm)
        Set<Long> scopeBac = service.layDsIdNhomDuocXemBoiTruongNhom(10L);
        assertTrue(scopeBac.contains(rootBac.getIdLong()));
        assertTrue(scopeBac.contains(subHN1.getIdLong()));
        assertTrue(scopeBac.contains(subHN2.getIdLong()));
        assertTrue(scopeBac.contains(subCG.getIdLong()));
        assertFalse(scopeBac.contains(rootNam.getIdLong()));

        // 2. Trưởng nhóm cấp 2 (User 11 - HN1):
        // Phải nhìn thấy: HN1, Cầu Giấy (2 nhóm). KHÔNG nhìn thấy Root Bắc, HN2, Nam!
        Set<Long> scopeHN1 = service.layDsIdNhomDuocXemBoiTruongNhom(11L);
        assertTrue(scopeHN1.contains(subHN1.getIdLong()));
        assertTrue(scopeHN1.contains(subCG.getIdLong()));
        assertFalse(scopeHN1.contains(rootBac.getIdLong()));
        assertFalse(scopeHN1.contains(subHN2.getIdLong()));
        assertFalse(scopeHN1.contains(rootNam.getIdLong()));

        // 3. Trưởng nhóm cấp 2 (User 12 - HN2):
        // Chỉ nhìn thấy: HN2 (1 nhóm)
        Set<Long> scopeHN2 = service.layDsIdNhomDuocXemBoiTruongNhom(12L);
        assertTrue(scopeHN2.contains(subHN2.getIdLong()));
        assertFalse(scopeHN2.contains(subHN1.getIdLong()));
        assertFalse(scopeHN2.contains(subCG.getIdLong()));
        assertFalse(scopeHN2.contains(rootBac.getIdLong()));

        // 4. Trưởng nhóm Nam (User 20):
        // Chỉ nhìn thấy: Nam (1 nhóm)
        Set<Long> scopeNam = service.layDsIdNhomDuocXemBoiTruongNhom(20L);
        assertTrue(scopeNam.contains(rootNam.getIdLong()));
        assertFalse(scopeNam.contains(rootBac.getIdLong()));
        assertFalse(scopeNam.contains(subHN1.getIdLong()));
    }

    @Test
    @DisplayName("AC3: Tích hợp với PhanQuyenDuLieuService kiểm tra quyền xem bản ghi theo cây tổ chức")
    void testPhanQuyenDuLieuTheoCayToChuc() {
        NhomKinhDoanh rootBac = service.themNhomKinhDoanh("INT_BAC", "Int Vùng Bắc", "", null, null, 10L);
        NhomKinhDoanh subHN = service.themNhomKinhDoanh("INT_HN", "Int Hà Nội", "", rootBac.getIdLong(), null, 11L);
        NhomKinhDoanh subCG = service.themNhomKinhDoanh("INT_CG", "Int Cầu Giấy", "", subHN.getIdLong(), null, null);
        NhomKinhDoanh rootNam = service.themNhomKinhDoanh("INT_NAM", "Int Vùng Nam", "", null, null, 20L);

        PhanQuyenDuLieuService pqService = new PhanQuyenDuLieuService(null, service);

        // User 10: Trưởng nhóm Vùng Bắc (vai trò TEAM_LEAD, phạm vi NHOM)
        NguoiDungDTO leaderBac = new NguoiDungDTO(10L, "Trưởng nhóm Bắc", "lead_bac@crm.vn", vn.nhom10.crm.model.VaiTroEnum.TEAM_LEAD, rootBac.getIdLong(), "Int Vùng Bắc", PhamViDuLieu.NHOM);

        // Bản ghi 1: thuộc Tổ Cầu Giấy (nhóm cháu của Vùng Bắc) do sales_cg (ID: 103) phụ trách
        BanGhiNghiepVuDTO bgChau = new BanGhiNghiepVuDTO(
                901L, "KH-901", "Công ty Khách Hàng Cầu Giấy",
                BanGhiNghiepVuDTO.LoaiNghiepVu.KHACH_HANG,
                103L, "Sales CG", subCG.getIdLong(), "Int Cầu Giấy",
                "100,000,000 đ", "Tiềm năng", LocalDate.now(), ""
        );

        // Bản ghi 2: thuộc Vùng Nam do sales_hcm (ID: 201) phụ trách
        BanGhiNghiepVuDTO bgNam = new BanGhiNghiepVuDTO(
                902L, "KH-902", "Công ty Khách Hàng Sài Gòn",
                BanGhiNghiepVuDTO.LoaiNghiepVu.KHACH_HANG,
                201L, "Sales HCM", rootNam.getIdLong(), "Int Vùng Nam",
                "500,000,000 đ", "Tiềm năng", LocalDate.now(), ""
        );

        // Leader Bắc ĐƯỢC XEM bản ghi của nhóm cháu Cầu Giấy (AC3)
        PhanQuyenDuLieuService.KetQuaKiemTra kqChau = pqService.kiemTraQuyenTruyCap(leaderBac, bgChau);
        assertTrue(kqChau.isCoQuyen());
        assertTrue(kqChau.getThongBao().contains("cây tổ chức"));

        // Leader Bắc BỊ TỪ CHỐI xem bản ghi của Vùng Nam
        PhanQuyenDuLieuService.KetQuaKiemTra kqNam = pqService.kiemTraQuyenTruyCap(leaderBac, bgNam);
        assertFalse(kqNam.isCoQuyen());
    }
}
