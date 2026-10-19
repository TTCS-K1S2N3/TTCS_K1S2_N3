package vn.nhom10.crm.service;

import org.junit.jupiter.api.*;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dao.CauHinhHeThongDAO;
import vn.nhom10.crm.dao.LeadDAO;
import vn.nhom10.crm.dto.CauHinhChamDiemDTO;
import vn.nhom10.crm.dto.NguongPhanLoaiLeadDTO;
import vn.nhom10.crm.dto.TieuChiChamDiemDTO;
import vn.nhom10.crm.model.Lead;
import vn.nhom10.crm.model.TrangThaiLeadEnum;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử LeadScoringService - Quy tắc chấm điểm và phân loại Lead (Story S4-05)")
class LeadScoringServiceTest {

    private static Connection h2Connection;
    private LeadScoringService service;
    private LeadDAO leadDAO;
    private CauHinhHeThongDAO cauHinhDAO;

    @BeforeAll
    static void setUpDatabase() throws Exception {
        Class.forName("org.h2.Driver");
        h2Connection = DriverManager.getConnection("jdbc:h2:mem:crm_test_scoring;MODE=MySQL;DB_CLOSE_DELAY=-1");

        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS cau_hinh_he_thong (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_cau_hinh VARCHAR(100) NOT NULL UNIQUE, " +
                    "nhom_cau_hinh VARCHAR(60) NOT NULL, " +
                    "kieu_du_lieu VARCHAR(30) NOT NULL, " +
                    "gia_tri TEXT NULL, " +
                    "mo_ta VARCHAR(1000) NULL, " +
                    "bat_buoc_cau_hinh TINYINT(1) DEFAULT 0, " +
                    "bao_mat TINYINT(1) DEFAULT 0, " +
                    "updated_by BIGINT NULL, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.execute("CREATE TABLE IF NOT EXISTS nguon_lead (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_nguon VARCHAR(50) NOT NULL, " +
                    "ten_nguon VARCHAR(150) NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS chien_dich (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_chien_dich VARCHAR(50) NOT NULL, " +
                    "ten_chien_dich VARCHAR(150) NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS bieu_mau_lead (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_bieu_mau VARCHAR(50) NOT NULL, " +
                    "ten_bieu_mau VARCHAR(150) NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS nguoi_dung (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(150) NOT NULL, " +
                    "email VARCHAR(150) NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS `lead` (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_lead VARCHAR(50) NOT NULL UNIQUE, " +
                    "ho_ten VARCHAR(150) NOT NULL, " +
                    "email VARCHAR(255) NULL, " +
                    "email_chuan_hoa VARCHAR(255) NULL, " +
                    "so_dien_thoai VARCHAR(20) NULL, " +
                    "so_dien_thoai_chuan_hoa VARCHAR(20) NULL, " +
                    "ten_cong_ty VARCHAR(255) NULL, " +
                    "ten_cong_ty_chuan_hoa VARCHAR(255) NULL, " +
                    "nhu_cau_quan_tam TEXT NULL, " +
                    "muc_do_quan_tam VARCHAR(50) NULL, " +
                    "nganh_nghe_id BIGINT NULL, " +
                    "quy_mo_id BIGINT NULL, " +
                    "khu_vuc_id BIGINT NULL, " +
                    "nguon_lead_id BIGINT NOT NULL, " +
                    "chien_dich_id BIGINT NULL, " +
                    "bieu_mau_id BIGINT NULL, " +
                    "diem INT DEFAULT 0, " +
                    "phan_loai VARCHAR(30) NULL, " +
                    "trang_thai VARCHAR(40) DEFAULT 'MOI', " +
                    "nguoi_phu_trach_id BIGINT NULL, " +
                    "nhom_phan_bo_id BIGINT NULL, " +
                    "duoc_phan_bo_luc DATETIME NULL, " +
                    "han_phan_hoi_sla DATETIME NULL, " +
                    "duoc_nhan_luc DATETIME NULL, " +
                    "lien_he_dau_luc DATETIME NULL, " +
                    "ly_do_tu_choi VARCHAR(500) NULL, " +
                    "qua_sla TINYINT DEFAULT 0, " +
                    "khach_hang_chuyen_doi_id BIGINT NULL, " +
                    "nguoi_lien_he_chuyen_doi_id BIGINT NULL, " +
                    "co_hoi_chuyen_doi_id BIGINT NULL, " +
                    "chuyen_doi_luc DATETIME NULL, " +
                    "gop_vao_lead_id BIGINT NULL, " +
                    "created_by BIGINT NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at DATETIME NULL)");
        }

        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection("jdbc:h2:mem:crm_test_scoring;MODE=MySQL");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @AfterAll
    static void tearDownDatabase() throws Exception {
        DatabaseConfig.resetConnectionSupplier();
        if (h2Connection != null && !h2Connection.isClosed()) {
            h2Connection.close();
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        cauHinhDAO = new CauHinhHeThongDAO();
        leadDAO = new LeadDAO();
        service = new LeadScoringService(cauHinhDAO, leadDAO);

        // Reset dữ liệu bảng test
        try (Statement st = h2Connection.createStatement()) {
            st.execute("DELETE FROM cau_hinh_he_thong");
            st.execute("DELETE FROM `lead`");
            st.execute("DELETE FROM nguon_lead");
            st.execute("INSERT INTO nguon_lead (id, ma_nguon, ten_nguon) VALUES (1, 'WEB', 'Website')");
            st.execute("INSERT INTO nguon_lead (id, ma_nguon, ten_nguon) VALUES (2, 'FACEBOOK', 'Facebook')");
        }
    }

    // =========================================================================
    // AC1: KHAI BÁO TIÊU CHÍ VÀ SỐ ĐIỂM
    // =========================================================================

    @Test
    @DisplayName("AC1: Khởi tạo cấu hình mặc định gồm đủ 4 nhóm tiêu chí")
    void testLayCauHinhMacDinh() {
        CauHinhChamDiemDTO config = service.layCauHinhHienTai();
        assertNotNull(config);
        assertEquals(70, config.getNguongPhanLoai().getHotMin());
        assertEquals(40, config.getNguongPhanLoai().getWarmMin());
        assertEquals(4, config.getDanhSachTieuChi().size());

        assertTrue(config.getDanhSachTieuChi().stream().anyMatch(t -> "INDUSTRY_FIT".equals(t.getKey())));
        assertTrue(config.getDanhSachTieuChi().stream().anyMatch(t -> "COMPANY_SIZE".equals(t.getKey())));
        assertTrue(config.getDanhSachTieuChi().stream().anyMatch(t -> "SOURCE".equals(t.getKey())));
        assertTrue(config.getDanhSachTieuChi().stream().anyMatch(t -> "INTEREST_LEVEL".equals(t.getKey())));
    }

    @Test
    @DisplayName("AC1: Lưu cấu hình mới bền vững vào CSDL và tải lại thành công")
    void testLuuVaTaiCauHinhThanhCong() {
        CauHinhChamDiemDTO config = service.layCauHinhHienTai();
        config.getNguongPhanLoai().setHotMin(80);
        config.getNguongPhanLoai().setWarmMin(45);

        for (TieuChiChamDiemDTO tc : config.getDanhSachTieuChi()) {
            if ("INDUSTRY_FIT".equals(tc.getKey())) {
                tc.setSoDiem(30);
            }
        }

        boolean ok = service.luuCauHinh(config, 1L, "Giám đốc kinh doanh");
        assertTrue(ok);

        // Tạo service mới để kiểm tra đọc trực tiếp từ CSDL
        LeadScoringService newService = new LeadScoringService(cauHinhDAO, leadDAO);
        CauHinhChamDiemDTO reloaded = newService.layCauHinhHienTai();
        assertEquals(80, reloaded.getNguongPhanLoai().getHotMin());
        assertEquals(45, reloaded.getNguongPhanLoai().getWarmMin());

        TieuChiChamDiemDTO tcNganh = reloaded.getDanhSachTieuChi().stream()
                .filter(t -> "INDUSTRY_FIT".equals(t.getKey())).findFirst().orElse(null);
        assertNotNull(tcNganh);
        assertEquals(30, tcNganh.getSoDiem());
    }

    @Test
    @DisplayName("AC1 & BUG-06: Chặn lưu khi Ấm = 70, Nóng = 40 (hotMin <= warmMin)")
    void testChanLuuKhi_Warm70_Hot40() {
        CauHinhChamDiemDTO config = service.layCauHinhHienTai();
        int initialVersion = config.getPhienBan();
        config.getNguongPhanLoai().setWarmMin(70);
        config.getNguongPhanLoai().setHotMin(40);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.luuCauHinh(config, 1L, "Director"));
        assertTrue(ex.getMessage().contains("lớn hơn ngưỡng"));

        // Kiểm tra CSDL không bị thay đổi phiên bản hay ngưỡng điểm
        LeadScoringService checkService = new LeadScoringService(cauHinhDAO, leadDAO);
        CauHinhChamDiemDTO checkConfig = checkService.layCauHinhHienTai();
        assertEquals(initialVersion, checkConfig.getPhienBan());
        assertEquals(70, checkConfig.getNguongPhanLoai().getHotMin());
        assertEquals(40, checkConfig.getNguongPhanLoai().getWarmMin());
    }

    @Test
    @DisplayName("AC1 & BUG-06: Chặn lưu khi Ấm = 70, Nóng = 70 (hotMin == warmMin)")
    void testChanLuuKhi_Warm70_Hot70() {
        CauHinhChamDiemDTO config = service.layCauHinhHienTai();
        int initialVersion = config.getPhienBan();
        config.getNguongPhanLoai().setWarmMin(70);
        config.getNguongPhanLoai().setHotMin(70);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.luuCauHinh(config, 1L, "Director"));
        assertTrue(ex.getMessage().contains("lớn hơn ngưỡng"));

        // Kiểm tra CSDL không bị thay đổi phiên bản
        LeadScoringService checkService = new LeadScoringService(cauHinhDAO, leadDAO);
        CauHinhChamDiemDTO checkConfig = checkService.layCauHinhHienTai();
        assertEquals(initialVersion, checkConfig.getPhienBan());
    }

    @Test
    @DisplayName("AC1 & BUG-06: Lưu thành công khi Ấm = 40, Nóng = 70 (hotMin > warmMin)")
    void testLuuHopLe_Warm40_Hot70() {
        CauHinhChamDiemDTO config = service.layCauHinhHienTai();
        config.getNguongPhanLoai().setWarmMin(40);
        config.getNguongPhanLoai().setHotMin(70);

        boolean ok = service.luuCauHinh(config, 1L, "Director");
        assertTrue(ok);

        LeadScoringService checkService = new LeadScoringService(cauHinhDAO, leadDAO);
        CauHinhChamDiemDTO checkConfig = checkService.layCauHinhHienTai();
        assertEquals(40, checkConfig.getNguongPhanLoai().getWarmMin());
        assertEquals(70, checkConfig.getNguongPhanLoai().getHotMin());
    }

    @Test
    @DisplayName("BUG-05: Mỗi lần lưu thành công chỉ tăng đúng 1 phiên bản (v1 -> v2, v2 -> v3)")
    void testPhienBanTangDungMotLanKhiLuuThanhCong() {
        CauHinhChamDiemDTO config1 = service.layCauHinhHienTai();
        int v0 = config1.getPhienBan(); // Thường là 1

        // Lần lưu 1:
        config1.getNguongPhanLoai().setHotMin(75);
        config1.getNguongPhanLoai().setWarmMin(45);
        boolean ok1 = service.luuCauHinh(config1, 1L, "Director");
        assertTrue(ok1);
        assertEquals(v0 + 1, config1.getPhienBan(), "Phiên bản trong DTO sau lần lưu 1 phải tăng đúng 1");

        // Kiểm tra trong DB và service mới:
        LeadScoringService s2 = new LeadScoringService(cauHinhDAO, leadDAO);
        CauHinhChamDiemDTO config2 = s2.layCauHinhHienTai();
        assertEquals(v0 + 1, config2.getPhienBan(), "Phiên bản trong DB sau lần lưu 1 phải bằng v0 + 1");

        // Lần lưu 2:
        config2.getNguongPhanLoai().setHotMin(80);
        config2.getNguongPhanLoai().setWarmMin(50);
        boolean ok2 = s2.luuCauHinh(config2, 1L, "Director");
        assertTrue(ok2);
        assertEquals(v0 + 2, config2.getPhienBan(), "Phiên bản trong DTO sau lần lưu 2 phải tăng tiếp đúng 1");

        // Kiểm tra lại trong DB:
        LeadScoringService s3 = new LeadScoringService(cauHinhDAO, leadDAO);
        CauHinhChamDiemDTO config3 = s3.layCauHinhHienTai();
        assertEquals(v0 + 2, config3.getPhienBan(), "Phiên bản trong DB sau lần lưu 2 phải bằng v0 + 2");
    }

    @Test
    @DisplayName("AC1: Từ chối cấu hình khi ngưỡng Nóng <= ngưỡng Ấm")
    void testValidation_NguongLoi() {
        CauHinhChamDiemDTO config = service.layCauHinhHienTai();
        config.getNguongPhanLoai().setHotMin(40);
        config.getNguongPhanLoai().setWarmMin(50);

        assertThrows(IllegalArgumentException.class, () -> service.luuCauHinh(config, 1L, "Director"));
    }

    @Test
    @DisplayName("AC1: Từ chối cấu hình khi không có tiêu chí nào được kích hoạt")
    void testValidation_KhongCoTieuChi() {
        CauHinhChamDiemDTO config = service.layCauHinhHienTai();
        for (TieuChiChamDiemDTO tc : config.getDanhSachTieuChi()) {
            tc.setKichHoat(false);
        }

        assertThrows(IllegalArgumentException.class, () -> service.luuCauHinh(config, 1L, "Director"));
    }

    // =========================================================================
    // AC2 & AC3: TỰ ĐỘNG TÍNH LẠI ĐIỂM VÀ PHÂN LOẠI KHI THÔNG TIN LEAD THAY ĐỔI
    // =========================================================================

    @Test
    @DisplayName("AC2 & AC3: Lead đầy đủ thông tin đạt 100 điểm và phân loại NÓNG")
    void testTinhDiemLeadDayDuThongTin() {
        Lead lead = new Lead();
        lead.setHoTen("Nguyễn Văn A");
        lead.setEmail("a@congty.vn");
        lead.setNganhNgheId(10L);       // +25
        lead.setQuyMoId(20L);          // +20
        lead.setNguonLeadId(1L);        // +15
        lead.setMucDoQuanTam("CAN_GAP"); // +40

        service.tinhDiemVaPhanLoai(lead);

        assertEquals(100, lead.getDiem());
        assertEquals("NONG", lead.getPhanLoai());
    }

    @Test
    @DisplayName("AC2: Điểm được tính lại tự động khi thông tin lead thay đổi")
    void testTinhLaiDiemKhiLeadThayDoi() {
        Lead lead = new Lead();
        lead.setHoTen("Trần Thị B");
        lead.setNguonLeadId(1L);
        lead.setMucDoQuanTam("CAN_GAP");

        // Ban đầu chỉ có nguồn (15) + quan tâm (40) = 55 điểm -> Ấm
        service.tinhDiemVaPhanLoai(lead);
        assertEquals(55, lead.getDiem());
        assertEquals("AM", lead.getPhanLoai());

        // Khách hàng cập nhật thêm ngành nghề (+25) và quy mô (+20) -> 100 điểm -> Nóng
        lead.setNganhNgheId(5L);
        lead.setQuyMoId(3L);
        service.tinhDiemVaPhanLoai(lead);
        assertEquals(100, lead.getDiem());
        assertEquals("NONG", lead.getPhanLoai());

        // Bỏ bớt thông tin: không còn nhu cầu quan tâm (null) -> 100 - 40 = 60 điểm -> Ấm
        lead.setMucDoQuanTam(null);
        service.tinhDiemVaPhanLoai(lead);
        assertEquals(60, lead.getDiem());
        assertEquals("AM", lead.getPhanLoai());
    }

    @Test
    @DisplayName("AC3: Kiểm tra các giá trị biên ngưỡng phân loại Nóng/Ấm/Lạnh")
    void testNguongBienPhanLoai() {
        NguongPhanLoaiLeadDTO thresholds = new NguongPhanLoaiLeadDTO(70, 40);

        assertEquals("LANH", thresholds.phanLoai(0));
        assertEquals("LANH", thresholds.phanLoai(39));
        assertEquals("AM", thresholds.phanLoai(40));   // Đúng ngưỡng warmMin
        assertEquals("AM", thresholds.phanLoai(55));
        assertEquals("AM", thresholds.phanLoai(69));
        assertEquals("NONG", thresholds.phanLoai(70)); // Đúng ngưỡng hotMin
        assertEquals("NONG", thresholds.phanLoai(85));
        assertEquals("NONG", thresholds.phanLoai(100));
        assertEquals("CHUA_CHAM", thresholds.phanLoai(null));
    }

    // =========================================================================
    // AC4: ĐIỂM CHỈ ĐỂ ƯU TIÊN, KHÔNG TỰ ĐỘNG LOẠI LEAD
    // =========================================================================

    @Test
    @DisplayName("AC4: Lead điểm 0 vẫn tồn tại ở trạng thái MỚI, không bị tự động loại")
    void testLeadDiemThapKhongBiLoai() {
        Lead lead = new Lead();
        lead.setMaLead("LEAD-TEST-001");
        lead.setHoTen("Khách Không Rõ Nhu Cầu");
        lead.setNguonLeadId(1L);
        lead.setTrangThai(TrangThaiLeadEnum.MOI);
        // Tiêu chí nguồn lead bị null hoặc điểm thấp
        lead.setNguonLeadId(0L);
        lead.setNganhNgheId(null);
        lead.setQuyMoId(null);
        lead.setMucDoQuanTam(null);

        service.tinhDiemVaPhanLoai(lead);

        assertEquals(0, lead.getDiem());
        assertEquals("LANH", lead.getPhanLoai());
        // Trạng thái tuyệt đối không bị chuyển sang ĐÃ_HỦY hay BỊ_LOẠI
        assertEquals(TrangThaiLeadEnum.MOI, lead.getTrangThai());
    }

    @Test
    @DisplayName("AC4: Sắp xếp danh sách lead theo điểm ưu tiên giảm dần nhưng vẫn giữ đầy đủ lead lạnh")
    void testSapXepUuTienTheoDiem() {
        // Tạo 3 lead với điểm khác nhau
        Lead l1 = new Lead("LEAD-001", "Lead Nóng", "nong@crm.vn", "0901", "Cty Nóng", "Nhu cầu cao", 1L);
        l1.setNganhNgheId(1L);
        l1.setQuyMoId(1L);
        l1.setMucDoQuanTam("CAN_GAP");
        service.tinhDiemVaPhanLoai(l1);
        leadDAO.them(l1);

        Lead l2 = new Lead("LEAD-002", "Lead Ấm", "am@crm.vn", "0902", "Cty Ấm", "Nhu cầu vừa", 1L);
        l2.setMucDoQuanTam("TIM_HIEU");
        service.tinhDiemVaPhanLoai(l2);
        leadDAO.them(l2);

        Lead l3 = new Lead("LEAD-003", "Lead Lạnh", "lanh@crm.vn", "0903", "Cty Lạnh", "Chưa rõ", 1L);
        l3.setNguonLeadId(1L);
        l3.setNganhNgheId(null);
        l3.setQuyMoId(null);
        l3.setMucDoQuanTam(null);
        service.tinhDiemVaPhanLoai(l3);
        leadDAO.them(l3);

        List<Lead> danhSach = service.layDanhSachLeadUuTien(10, 0);
        assertEquals(3, danhSach.size());

        // Thứ tự sắp xếp phải giảm dần theo điểm: L1 (100) -> L2 (31) -> L3 (15)
        assertTrue(danhSach.get(0).getDiem() >= danhSach.get(1).getDiem());
        assertTrue(danhSach.get(1).getDiem() >= danhSach.get(2).getDiem());

        // Lead lạnh vẫn tồn tại trong kết quả
        assertTrue(danhSach.stream().anyMatch(l -> "LEAD-003".equals(l.getMaLead())));
    }
}
