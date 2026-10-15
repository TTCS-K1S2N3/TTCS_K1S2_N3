package vn.nhom10.crm.service;

import org.junit.jupiter.api.*;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dao.*;
import vn.nhom10.crm.dto.KetQuaPhanBoDTO;
import vn.nhom10.crm.dto.PhanBoLeadRequestDTO;
import vn.nhom10.crm.dto.QuyTacPhanBoDTO;
import vn.nhom10.crm.model.*;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử QuyTacPhanBoService - Story S4-06 (AC1, AC2, AC3, AC4)")
class QuyTacPhanBoServiceTest {

    private static final String H2_URL = "jdbc:h2:mem:crm_s4_06_service_test;MODE=MySQL;DB_CLOSE_DELAY=-1";
    private static Connection rootConnection;

    private QuyTacPhanBoService service;
    private QuyTacPhanBoLeadDAO quyTacDAO;
    private LichSuPhanBoLeadDAO lichSuDAO;
    private LeadDAO leadDAO;
    private NhomKinhDoanhDAO nhomDAO;
    private KhuVucDiaLyDAO khuVucDAO;
    private DanhMucBanHangDAO danhMucDAO;

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
            st.execute("CREATE TABLE IF NOT EXISTS nguon_lead (id BIGINT AUTO_INCREMENT PRIMARY KEY, ten_nguon VARCHAR(150))");
            st.execute("CREATE TABLE IF NOT EXISTS chien_dich (id BIGINT AUTO_INCREMENT PRIMARY KEY, ten_chien_dich VARCHAR(150))");
            st.execute("CREATE TABLE IF NOT EXISTS bieu_mau_lead (id BIGINT AUTO_INCREMENT PRIMARY KEY, ten_bieu_mau VARCHAR(150))");
            st.execute("CREATE TABLE IF NOT EXISTS khu_vuc_dia_ly (id BIGINT AUTO_INCREMENT PRIMARY KEY, ma_khu_vuc VARCHAR(50), ten_khu_vuc VARCHAR(150), loai_khu_vuc VARCHAR(50), khu_vuc_cha_id BIGINT, thu_tu_hien_thi INT, hoat_dong TINYINT, created_at DATETIME, updated_at DATETIME)");
            st.execute("CREATE TABLE IF NOT EXISTS nganh_nghe (id BIGINT AUTO_INCREMENT PRIMARY KEY, ma_nganh VARCHAR(50), ten_nganh VARCHAR(150), hoat_dong TINYINT, thu_tu INT, mo_ta VARCHAR(255), icon VARCHAR(50))");
            st.execute("CREATE TABLE IF NOT EXISTS quy_mo_doanh_nghiep (id BIGINT AUTO_INCREMENT PRIMARY KEY, ten_quy_mo VARCHAR(150))");

            st.execute("CREATE TABLE IF NOT EXISTS nhom_kinh_doanh (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_nhom VARCHAR(50) NULL, " +
                    "ten_nhom VARCHAR(150) NOT NULL, " +
                    "mo_ta VARCHAR(255) NULL, " +
                    "nhom_cha_id BIGINT NULL, " +
                    "khu_vuc_id BIGINT NULL, " +
                    "truong_nhom_id BIGINT NULL, " +
                    "hoat_dong TINYINT DEFAULT 1, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS nguoi_dung (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "email VARCHAR(150) NOT NULL, " +
                    "mat_khau VARCHAR(150) NOT NULL DEFAULT '', " +
                    "ho_ten VARCHAR(150) NOT NULL, " +
                    "so_dien_thoai VARCHAR(20) NULL, " +
                    "trang_thai VARCHAR(30) DEFAULT 'HOAT_DONG', " +
                    "hoat_dong TINYINT DEFAULT 1, " +
                    "nhom_kinh_doanh_id BIGINT NULL, " +
                    "so_lan_sai INT DEFAULT 0, " +
                    "thoi_gian_khoa DATETIME NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS quy_tac_phan_bo_lead (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_quy_tac VARCHAR(60) NOT NULL UNIQUE, " +
                    "ten_quy_tac VARCHAR(200) NOT NULL, " +
                    "thu_tu_uu_tien INT NOT NULL, " +
                    "kieu_phan_bo VARCHAR(30) NOT NULL, " +
                    "dieu_kien_json TEXT NULL, " +
                    "nhom_dich_id BIGINT NULL, " +
                    "nguoi_dung_dich_id BIGINT NULL, " +
                    "nguoi_dung_lan_cuoi_id BIGINT NULL, " +
                    "hoat_dong TINYINT DEFAULT 1, " +
                    "created_by BIGINT NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                    "CONSTRAINT uk_qtpb_tt UNIQUE (thu_tu_uu_tien))");

            st.execute("CREATE TABLE IF NOT EXISTS lich_su_phan_bo_lead (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "lead_id BIGINT NOT NULL, " +
                    "quy_tac_id BIGINT NULL, " +
                    "nguoi_dung_cu_id BIGINT NULL, " +
                    "nguoi_dung_moi_id BIGINT NULL, " +
                    "nhom_id BIGINT NULL, " +
                    "hanh_dong VARCHAR(30) NOT NULL, " +
                    "ly_do VARCHAR(500) NULL, " +
                    "thuc_hien_boi_id BIGINT NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS `lead` (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_lead VARCHAR(60) NOT NULL UNIQUE, " +
                    "ho_ten VARCHAR(150) NOT NULL, " +
                    "email VARCHAR(150) NULL, " +
                    "email_chuan_hoa VARCHAR(150) NULL, " +
                    "so_dien_thoai VARCHAR(20) NULL, " +
                    "so_dien_thoai_chuan_hoa VARCHAR(20) NULL, " +
                    "ten_cong_ty VARCHAR(200) NULL, " +
                    "ten_cong_ty_chuan_hoa VARCHAR(200) NULL, " +
                    "nhu_cau_quan_tam TEXT NULL, " +
                    "muc_do_quan_tam VARCHAR(50) NULL, " +
                    "nganh_nghe_id BIGINT NULL, " +
                    "quy_mo_id BIGINT NULL, " +
                    "khu_vuc_id BIGINT NULL, " +
                    "nguon_lead_id BIGINT NOT NULL, " +
                    "chien_dich_id BIGINT NULL, " +
                    "bieu_mau_id BIGINT NULL, " +
                    "diem INT DEFAULT 0, " +
                    "phan_loai VARCHAR(50) NULL, " +
                    "trang_thai VARCHAR(50) DEFAULT 'MOI', " +
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
                    "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)");
        }
    }

    @AfterAll
    static void tearDownDatabase() throws Exception {
        if (rootConnection != null && !rootConnection.isClosed()) {
            rootConnection.close();
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        quyTacDAO = new QuyTacPhanBoLeadDAO();
        lichSuDAO = new LichSuPhanBoLeadDAO();
        leadDAO = new LeadDAO();
        nhomDAO = new NhomKinhDoanhDAO();
        khuVucDAO = new KhuVucDiaLyDAO();
        danhMucDAO = new DanhMucBanHangDAO();

        service = new QuyTacPhanBoService(quyTacDAO, lichSuDAO, leadDAO, nhomDAO, khuVucDAO, danhMucDAO);

        try (Statement st = rootConnection.createStatement()) {
            st.execute("DELETE FROM lich_su_phan_bo_lead");
            st.execute("DELETE FROM `lead`");
            st.execute("DELETE FROM quy_tac_phan_bo_lead");
            st.execute("DELETE FROM nguoi_dung");
            st.execute("DELETE FROM nhom_kinh_doanh");
            st.execute("DELETE FROM nguon_lead");
            st.execute("INSERT INTO nguon_lead (id, ten_nguon) VALUES (1, 'Website')");
        }
    }

    // ==========================================
    // AC1: PHÂN BỔ THEO KHU VỰC, NGÀNH NGHỀ & XOAY VÒNG NHÓM
    // ==========================================

    @Test
    @DisplayName("AC1: Tạo quy tắc phân bổ theo khu vực, ngành nghề, xoay vòng và kiểm tra validation")
    void testAC1_TaoQuyTacVaValidation() {
        // 1. Validation: Tên rỗng -> Báo lỗi
        PhanBoLeadRequestDTO reqRong = new PhanBoLeadRequestDTO();
        reqRong.setTenQuyTac("   ");
        reqRong.setKieuPhanBo("THEO_KHU_VUC");
        assertThrows(IllegalArgumentException.class, () -> service.taoQuyTac(reqRong, 1L));

        // 2. Validation: Theo khu vực nhưng danh sách khu vực rỗng -> Báo lỗi
        PhanBoLeadRequestDTO reqKVThieu = new PhanBoLeadRequestDTO();
        reqKVThieu.setTenQuyTac("Lead Miền Bắc");
        reqKVThieu.setKieuPhanBo("THEO_KHU_VUC");
        reqKVThieu.setKhuVucIds(Collections.emptyList());
        assertThrows(IllegalArgumentException.class, () -> service.taoQuyTac(reqKVThieu, 1L));

        // 3. Validation: Xoay vòng nhưng thiếu nhóm đích -> Báo lỗi
        PhanBoLeadRequestDTO reqRRThieuNhom = new PhanBoLeadRequestDTO();
        reqRRThieuNhom.setTenQuyTac("Xoay vòng");
        reqRRThieuNhom.setKieuPhanBo("XOAY_VONG");
        assertThrows(IllegalArgumentException.class, () -> service.taoQuyTac(reqRRThieuNhom, 1L));

        // 4. Tạo thành công 3 kiểu quy tắc
        PhanBoLeadRequestDTO req1 = new PhanBoLeadRequestDTO();
        req1.setTenQuyTac("Lead Miền Bắc");
        req1.setKieuPhanBo("THEO_KHU_VUC");
        req1.setKhuVucIds(Arrays.asList(1L, 2L));
        req1.setNhomDichId(10L);
        QuyTacPhanBoLead rule1 = service.taoQuyTac(req1, 1L);
        assertNotNull(rule1.getId());
        assertEquals(1, rule1.getThuTuUuTien());

        PhanBoLeadRequestDTO req2 = new PhanBoLeadRequestDTO();
        req2.setTenQuyTac("Lead Ngành Tài Chính");
        req2.setKieuPhanBo("THEO_NGANH_NGHE");
        req2.setNganhNgheIds(Collections.singletonList(5L));
        req2.setNguoiDungDichId(99L);
        QuyTacPhanBoLead rule2 = service.taoQuyTac(req2, 1L);
        assertNotNull(rule2.getId());
        assertEquals(2, rule2.getThuTuUuTien());

        List<QuyTacPhanBoDTO> ds = service.layDanhSachQuyTac(false);
        assertEquals(2, ds.size());
    }

    @Test
    @DisplayName("AC1: Phân bổ xoay vòng Round-Robin chia đều cho các thành viên trong nhóm")
    void testAC1_XoayVongRoundRobin() throws Exception {
        // Tạo nhóm kinh doanh 10
        try (Statement st = rootConnection.createStatement()) {
            st.execute("INSERT INTO nhom_kinh_doanh (id, ten_nhom, hoat_dong) VALUES (10, 'Nhóm Hà Nội', 1)");
            // Tạo 3 nhân viên hoạt động trong nhóm 10: ID 101, 102, 103
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email, hoat_dong, trang_thai, nhom_kinh_doanh_id) VALUES " +
                    "(101, 'Sales A', 'a@crm.vn', 1, 'HOAT_DONG', 10), " +
                    "(102, 'Sales B', 'b@crm.vn', 1, 'HOAT_DONG', 10), " +
                    "(103, 'Sales C', 'c@crm.vn', 1, 'HOAT_DONG', 10)");
        }

        // Tạo quy tắc xoay vòng trong nhóm 10
        PhanBoLeadRequestDTO req = new PhanBoLeadRequestDTO();
        req.setTenQuyTac("Xoay vòng Nhóm Hà Nội");
        req.setKieuPhanBo("XOAY_VONG");
        req.setNhomDichId(10L);
        service.taoQuyTac(req, 1L);

        // Tạo 4 lead để phân bổ
        Lead lead1 = taoLeadMock("L01", null, null);
        Lead lead2 = taoLeadMock("L02", null, null);
        Lead lead3 = taoLeadMock("L03", null, null);
        Lead lead4 = taoLeadMock("L04", null, null);

        try (Connection conn = DatabaseConnection.layKetNoi()) {
            conn.setAutoCommit(false);
            service.phanBoChoMotLead(conn, lead1);
            service.phanBoChoMotLead(conn, lead2);
            service.phanBoChoMotLead(conn, lead3);
            service.phanBoChoMotLead(conn, lead4);
            conn.commit();
        }

        // Kiểm tra thứ tự phân bổ xoay vòng: 101 -> 102 -> 103 -> quay lại 101
        Lead l1 = leadDAO.timTheoId(lead1.getId());
        Lead l2 = leadDAO.timTheoId(lead2.getId());
        Lead l3 = leadDAO.timTheoId(lead3.getId());
        Lead l4 = leadDAO.timTheoId(lead4.getId());

        assertEquals(101L, l1.getNguoiPhuTrachId());
        assertEquals(102L, l2.getNguoiPhuTrachId());
        assertEquals(103L, l3.getNguoiPhuTrachId());
        assertEquals(101L, l4.getNguoiPhuTrachId()); // Xoay vòng lại đầu mảng
    }

    // ==========================================
    // AC2: THỨ TỰ ƯU TIÊN - FIRST MATCH WINS
    // ==========================================

    @Test
    @DisplayName("AC2: Quy tắc xếp theo thứ tự ưu tiên, quy tắc đầu tiên khớp sẽ thắng")
    void testAC2_FirstMatchWins() throws Exception {
        try (Statement st = rootConnection.createStatement()) {
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email, hoat_dong, trang_thai) VALUES " +
                    "(50, 'Chuyên gia Tài chính', 'tc@crm.vn', 1, 'HOAT_DONG'), " +
                    "(60, 'Sales Miền Bắc', 'mb@crm.vn', 1, 'HOAT_DONG')");
        }

        // Rule 1: Ưu tiên ngành Tài chính (ID: 8) -> Gán cho User 50
        PhanBoLeadRequestDTO req1 = new PhanBoLeadRequestDTO();
        req1.setTenQuyTac("Ưu tiên Ngành Tài Chính");
        req1.setKieuPhanBo("THEO_NGANH_NGHE");
        req1.setNganhNgheIds(Collections.singletonList(8L));
        req1.setNguoiDungDichId(50L);
        service.taoQuyTac(req1, 1L);

        // Rule 2: Ưu tiên Khu vực Hà Nội (ID: 1) -> Gán cho User 60
        PhanBoLeadRequestDTO req2 = new PhanBoLeadRequestDTO();
        req2.setTenQuyTac("Ưu tiên Khu vực Hà Nội");
        req2.setKieuPhanBo("THEO_KHU_VUC");
        req2.setKhuVucIds(Collections.singletonList(1L));
        req2.setNguoiDungDichId(60L);
        service.taoQuyTac(req2, 1L);

        // Lead X có CẢ ngành Tài chính (8) và khu vực Hà Nội (1)
        Lead leadX = taoLeadMock("LX", 1L, 8L);

        try (Connection conn = DatabaseConnection.layKetNoi()) {
            conn.setAutoCommit(false);
            boolean ok = service.phanBoChoMotLead(conn, leadX);
            conn.commit();
            assertTrue(ok);
        }

        // Vì Rule 1 có thứ tự ưu tiên cao hơn (Top 1), Rule 1 thắng -> User 50 nhận lead
        Lead ketQua = leadDAO.timTheoId(leadX.getId());
        assertEquals(50L, ketQua.getNguoiPhuTrachId());
        assertEquals(TrangThaiLeadEnum.DA_PHAN_BO, ketQua.getTrangThai());
    }

    // ==========================================
    // AC3: HÀNG CHỜ PHÂN TAY & DATA SCOPE
    // ==========================================

    @Test
    @DisplayName("AC3: Lead không khớp quy tắc nào rơi vào hàng chờ và Trưởng nhóm phân tay")
    void testAC3_HangChoVaPhanTay() throws Exception {
        try (Statement st = rootConnection.createStatement()) {
            st.execute("INSERT INTO nhom_kinh_doanh (id, ten_nhom, hoat_dong) VALUES " +
                    "(1, 'Nhóm 1', 1), (2, 'Nhóm 2', 1)");

            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email, hoat_dong, trang_thai, nhom_kinh_doanh_id) VALUES " +
                    "(10, 'Trưởng nhóm 1', 'lead1@crm.vn', 1, 'HOAT_DONG', 1), " +
                    "(11, 'Nhân viên 1A', 'nv1a@crm.vn', 1, 'HOAT_DONG', 1), " +
                    "(21, 'Nhân viên 2A', 'nv2a@crm.vn', 1, 'HOAT_DONG', 2)");
        }

        // Chỉ có 1 quy tắc cho khu vực ID 99
        PhanBoLeadRequestDTO req = new PhanBoLeadRequestDTO();
        req.setTenQuyTac("Rule duy nhất khu vực 99");
        req.setKieuPhanBo("THEO_KHU_VUC");
        req.setKhuVucIds(Collections.singletonList(99L));
        req.setNguoiDungDichId(11L);
        service.taoQuyTac(req, 1L);

        // Lead có khu vực ID 1 (không khớp quy tắc 99)
        Lead leadKoKhop = taoLeadMock("L_KHONG_KHOP", 1L, null);

        try (Connection conn = DatabaseConnection.layKetNoi()) {
            conn.setAutoCommit(false);
            boolean assigned = service.phanBoChoMotLead(conn, leadKoKhop);
            conn.commit();
            assertFalse(assigned); // Không gán được quy tắc nào
        }

        // Kiểm chứng Lead đã rơi vào hàng chờ phân bổ
        Lead leadSau = leadDAO.timTheoId(leadKoKhop.getId());
        assertEquals(TrangThaiLeadEnum.HANG_CHO_PHAN_BO, leadSau.getTrangThai());
        assertNull(leadSau.getNguoiPhuTrachId());

        // Kiểm tra danh sách hàng chờ có 1 lead
        assertEquals(1, service.demLeadHangCho());

        // Tạo mock User Trưởng nhóm 1
        NguoiDung truongNhom1 = new NguoiDung();
        truongNhom1.setId(10L);
        truongNhom1.setNhomKinhDoanhId(1);
        truongNhom1.themVaiTro(VaiTroEnum.TEAM_LEAD);

        // TH1: Trưởng nhóm 1 cố tình phân cho Nhân viên 2A (thuộc Nhóm 2) -> Bị SecurityException (Data Scope)
        assertThrows(SecurityException.class, () ->
                service.phanTayTuHangCho(leadKoKhop.getId(), 21L, truongNhom1, "Phân nhầm"));

        // TH2: Trưởng nhóm 1 phân cho Nhân viên 1A (thuộc Nhóm 1) -> Thành công
        boolean phanTayOk = service.phanTayTuHangCho(leadKoKhop.getId(), 11L, truongNhom1, "Phân đúng nhóm");
        assertTrue(phanTayOk);

        Lead leadPhanXong = leadDAO.timTheoId(leadKoKhop.getId());
        assertEquals(11L, leadPhanXong.getNguoiPhuTrachId());
        assertEquals(TrangThaiLeadEnum.DA_PHAN_BO, leadPhanXong.getTrangThai());
        assertNotNull(leadPhanXong.getDuocPhanBoLuc());
        assertNotNull(leadPhanXong.getHanPhanHoiSla());
    }

    // ==========================================
    // AC4: PHÂN BỔ CHẠY NỀN BATCH
    // ==========================================

    @Test
    @DisplayName("AC4: Tiến trình quét ngầm chạy batch phân bổ hàng loạt trong thời gian < 5 phút")
    void testAC4_ChayBatchPhanBoNen() throws Exception {
        try (Statement st = rootConnection.createStatement()) {
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email, hoat_dong, trang_thai) VALUES " +
                    "(88, 'Sales Batch', 'batch@crm.vn', 1, 'HOAT_DONG')");
        }

        // Tạo 1 quy tắc cho khu vực 1
        PhanBoLeadRequestDTO req = new PhanBoLeadRequestDTO();
        req.setTenQuyTac("Batch Rule KV 1");
        req.setKieuPhanBo("THEO_KHU_VUC");
        req.setKhuVucIds(Collections.singletonList(1L));
        req.setNguoiDungDichId(88L);
        service.taoQuyTac(req, 1L);

        // Bơm 5 lead: 3 lead khu vực 1, 2 lead không có khu vực
        taoLeadMock("B01", 1L, null);
        taoLeadMock("B02", 1L, null);
        taoLeadMock("B03", 1L, null);
        taoLeadMock("B04", null, null);
        taoLeadMock("B05", null, null);

        // Chạy batch
        KetQuaPhanBoDTO kq = service.chayTienTrinhPhanBoBatch(10);
        assertEquals(5, kq.getSoLeadDaQuet());
        assertEquals(3, kq.getSoLeadPhanBoThanhCong());
        assertEquals(2, kq.getSoLeadVaoHangCho());
        assertTrue(kq.getThoiGianXuLyMs() < 5000); // Hoàn thành dưới 5 giây, vượt xa tiêu chuẩn < 5 phút
    }

    private Lead taoLeadMock(String maLead, Long khuVucId, Long nganhNgheId) {
        Lead lead = new Lead();
        lead.setMaLead(maLead);
        lead.setHoTen("Khách hàng " + maLead);
        lead.setEmail(maLead.toLowerCase() + "@test.vn");
        lead.setSoDienThoai("0988776655");
        lead.setNguonLeadId(1L);
        lead.setKhuVucId(khuVucId);
        lead.setNganhNgheId(nganhNgheId);
        lead.setTrangThai(TrangThaiLeadEnum.MOI);
        Long id = leadDAO.them(lead);
        lead.setId(id);
        return lead;
    }
}
