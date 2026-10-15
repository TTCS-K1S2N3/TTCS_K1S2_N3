package vn.nhom10.crm.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dao.CoHoiDAO;
import vn.nhom10.crm.dao.GiaiDoanPipelineDAO;
import vn.nhom10.crm.dao.KhachHang360DAO;
import vn.nhom10.crm.dao.KhachHangDAO;
import vn.nhom10.crm.dao.LeadDAO;
import vn.nhom10.crm.dao.NguoiLienHeDAO;
import vn.nhom10.crm.dto.ChuyenDoiLeadPreviewDTO;
import vn.nhom10.crm.dto.KetQuaChuyenDoiLeadDTO;
import vn.nhom10.crm.model.CoHoi;
import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.model.Lead;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NguoiLienHe;
import vn.nhom10.crm.model.TrangThaiLeadEnum;
import vn.nhom10.crm.model.VaiTroEnum;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử LeadChuyenDoiService - Story S4-08")
class LeadChuyenDoiServiceTest {

    private static final String H2_URL = "jdbc:h2:mem:crm_s4_08_service_test;MODE=MySQL;DB_CLOSE_DELAY=-1";
    private static Connection rootConnection;

    private LeadDAO leadDAO;
    private KhachHangDAO khachHangDAO;
    private NguoiLienHeDAO nguoiLienHeDAO;
    private CoHoiDAO coHoiDAO;
    private GiaiDoanPipelineDAO giaiDoanPipelineDAO;
    private KhachHang360DAO khachHang360DAO;
    private LeadChuyenDoiService service;

    private NguoiDung salesRepUser;
    private NguoiDung otherSalesRepUser;
    private NguoiDung teamLeadUser;
    private NguoiDung directorUser;
    private NguoiDung adminUser;
    private NguoiDung marketingUser;

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
            st.execute("CREATE TABLE IF NOT EXISTS nguoi_dung (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(150), " +
                    "email VARCHAR(255), " +
                    "nhom_kinh_doanh_id INT)");

            st.execute("CREATE TABLE IF NOT EXISTS nhom_kinh_doanh (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ten_nhom VARCHAR(150))");

            st.execute("CREATE TABLE IF NOT EXISTS nganh_nghe (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ten_nganh VARCHAR(150))");

            st.execute("CREATE TABLE IF NOT EXISTS quy_mo_doanh_nghiep (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ten_quy_mo VARCHAR(150))");

            st.execute("CREATE TABLE IF NOT EXISTS khu_vuc_dia_ly (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ten_khu_vuc VARCHAR(150))");

            st.execute("CREATE TABLE IF NOT EXISTS nguon_lead (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_nguon VARCHAR(50), " +
                    "ten_nguon VARCHAR(150), " +
                    "hoat_dong TINYINT DEFAULT 1)");

            st.execute("CREATE TABLE IF NOT EXISTS chien_dich (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_chien_dich VARCHAR(50), " +
                    "ten_chien_dich VARCHAR(200))");

            st.execute("CREATE TABLE IF NOT EXISTS bieu_mau_lead (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ten_bieu_mau VARCHAR(200))");

            st.execute("CREATE TABLE IF NOT EXISTS ly_do_thang_thua (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ten_ly_do VARCHAR(150))");

            st.execute("CREATE TABLE IF NOT EXISTS pipeline_ban_hang (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_pipeline VARCHAR(50) UNIQUE, " +
                    "ten_pipeline VARCHAR(150), " +
                    "mo_ta TEXT, " +
                    "mac_dinh TINYINT DEFAULT 1, " +
                    "hoat_dong TINYINT DEFAULT 1, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS giai_doan_pipeline (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "pipeline_id BIGINT, " +
                    "ma_giai_doan VARCHAR(50), " +
                    "ten_giai_doan VARCHAR(150), " +
                    "thu_tu INT DEFAULT 1, " +
                    "xac_suat_mac_dinh INT DEFAULT 10, " +
                    "so_ngay_dinh_tre INT DEFAULT 7, " +
                    "loai_ket_thuc VARCHAR(20) NULL, " +
                    "hoat_dong TINYINT DEFAULT 1, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS dieu_kien_giai_doan (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "giai_doan_id BIGINT, " +
                    "tieu_de VARCHAR(255))");

            st.execute("CREATE TABLE IF NOT EXISTS khach_hang (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_khach_hang VARCHAR(50) UNIQUE, " +
                    "ten_cong_ty VARCHAR(255) NOT NULL, " +
                    "ten_chuan_hoa VARCHAR(255), " +
                    "ma_so_thue VARCHAR(50) UNIQUE, " +
                    "nganh_nghe_id BIGINT NULL, " +
                    "quy_mo_id BIGINT NULL, " +
                    "website VARCHAR(255), " +
                    "website_chuan_hoa VARCHAR(255), " +
                    "dia_chi VARCHAR(500), " +
                    "khu_vuc_id BIGINT NULL, " +
                    "nguoi_so_huu_id BIGINT NOT NULL, " +
                    "nhom_kinh_doanh_id BIGINT NULL, " +
                    "doanh_thu_uoc_tinh DECIMAL(18,2) DEFAULT 0.00, " +
                    "cong_ty_me_id BIGINT NULL, " +
                    "trang_thai VARCHAR(50) DEFAULT 'TIEM_NANG', " +
                    "co_rui_ro TINYINT DEFAULT 0, " +
                    "rui_ro_cap_nhat_luc DATETIME NULL, " +
                    "lan_tuong_tac_cuoi DATETIME NULL, " +
                    "gop_vao_khach_hang_id BIGINT NULL, " +
                    "mo_ta_chi_tiet TEXT, " +
                    "ngay_tao DATE DEFAULT CURRENT_DATE, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS nguoi_lien_he (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "khach_hang_id BIGINT NOT NULL, " +
                    "ho_ten VARCHAR(150) NOT NULL, " +
                    "chuc_danh VARCHAR(150), " +
                    "email VARCHAR(255), " +
                    "so_dien_thoai VARCHAR(20), " +
                    "vai_tro_quyet_dinh VARCHAR(50), " +
                    "la_dau_moi_chinh TINYINT DEFAULT 0, " +
                    "trang_thai VARCHAR(30) DEFAULT 'DANG_HOAT_DONG', " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS lich_su_lien_he_cong_ty (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "nguoi_lien_he_id BIGINT NOT NULL, " +
                    "khach_hang_id BIGINT NOT NULL, " +
                    "chuc_danh VARCHAR(150), " +
                    "vai_tro_quyet_dinh VARCHAR(50), " +
                    "tu_ngay DATE, " +
                    "den_ngay DATE, " +
                    "ghi_chu VARCHAR(500), " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS `lead` (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_lead VARCHAR(50) UNIQUE, " +
                    "ho_ten VARCHAR(150) NOT NULL, " +
                    "email VARCHAR(255), " +
                    "email_chuan_hoa VARCHAR(255), " +
                    "so_dien_thoai VARCHAR(20), " +
                    "so_dien_thoai_chuan_hoa VARCHAR(20), " +
                    "ten_cong_ty VARCHAR(255), " +
                    "ten_cong_ty_chuan_hoa VARCHAR(255), " +
                    "nhu_cau_quan_tam TEXT, " +
                    "muc_do_quan_tam VARCHAR(50), " +
                    "nganh_nghe_id BIGINT NULL, " +
                    "quy_mo_id BIGINT NULL, " +
                    "khu_vuc_id BIGINT NULL, " +
                    "nguon_lead_id BIGINT NOT NULL, " +
                    "chien_dich_id BIGINT NULL, " +
                    "bieu_mau_id BIGINT NULL, " +
                    "diem INT DEFAULT 0, " +
                    "phan_loai VARCHAR(30), " +
                    "trang_thai VARCHAR(40) DEFAULT 'MOI', " +
                    "nguoi_phu_trach_id BIGINT NULL, " +
                    "nhom_phan_bo_id BIGINT NULL, " +
                    "duoc_phan_bo_luc DATETIME, " +
                    "han_phan_hoi_sla DATETIME, " +
                    "duoc_nhan_luc DATETIME, " +
                    "lien_he_dau_luc DATETIME, " +
                    "ly_do_tu_choi VARCHAR(500), " +
                    "qua_sla TINYINT DEFAULT 0, " +
                    "khach_hang_chuyen_doi_id BIGINT NULL, " +
                    "nguoi_lien_he_chuyen_doi_id BIGINT NULL, " +
                    "co_hoi_chuyen_doi_id BIGINT NULL, " +
                    "chuyen_doi_luc DATETIME, " +
                    "gop_vao_lead_id BIGINT NULL, " +
                    "created_by BIGINT NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS co_hoi (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_co_hoi VARCHAR(50) UNIQUE, " +
                    "ten_co_hoi VARCHAR(255) NOT NULL, " +
                    "khach_hang_id BIGINT NULL, " +
                    "nguoi_lien_he_chinh_id BIGINT NULL, " +
                    "lead_id BIGINT NULL, " +
                    "chien_dich_id BIGINT NULL, " +
                    "nguon_lead_id BIGINT NULL, " +
                    "giai_doan_id BIGINT NULL, " +
                    "nguoi_phu_trach_id BIGINT NOT NULL, " +
                    "nhom_kinh_doanh_id BIGINT NULL, " +
                    "gia_tri_du_kien DECIMAL(18,2) DEFAULT 0.00, " +
                    "xac_suat INT DEFAULT 0, " +
                    "ly_do_sua_xac_suat VARCHAR(500), " +
                    "ngay_chot_du_kien DATE, " +
                    "trang_thai VARCHAR(50) DEFAULT 'MO', " +
                    "gia_tri_chot_thuc_te DECIMAL(18,2), " +
                    "ngay_ky DATE, " +
                    "ly_do_thang_thua_id BIGINT NULL, " +
                    "doi_thu_id BIGINT NULL, " +
                    "ngay_hoat_dong_cuoi DATETIME, " +
                    "bi_dinh_tre TINYINT DEFAULT 0, " +
                    "mo_ta_chi_tiet TEXT, " +
                    "ngay_tao DATE DEFAULT CURRENT_DATE, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS hoat_dong (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_hoat_dong VARCHAR(50), " +
                    "tieu_de VARCHAR(255), " +
                    "loai_hoat_dong VARCHAR(50), " +
                    "lead_id BIGINT NULL, " +
                    "khach_hang_id BIGINT NULL, " +
                    "nguoi_lien_he_id BIGINT NULL, " +
                    "co_hoi_id BIGINT NULL, " +
                    "nguoi_phu_trach_id BIGINT NULL, " +
                    "nhom_kinh_doanh_id BIGINT NULL, " +
                    "chi_phi DECIMAL(18,2) DEFAULT 0.00, " +
                    "thoi_gian_bat_dau DATETIME, " +
                    "thoi_gian_ket_thuc DATETIME, " +
                    "trang_thai VARCHAR(50), " +
                    "mo_ta_chi_tiet TEXT, " +
                    "noi_dung TEXT, " +
                    "ket_qua TEXT, " +
                    "thoi_luong_phut INT DEFAULT 0, " +
                    "la_ghi_nhan_qua_khu TINYINT DEFAULT 0, " +
                    "la_ghi_nhan_nhanh TINYINT DEFAULT 0, " +
                    "ngay_tao DATE DEFAULT CURRENT_DATE, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP)");
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
    void setUp() throws SQLException {
        leadDAO = new LeadDAO();
        khachHangDAO = new KhachHangDAO();
        nguoiLienHeDAO = new NguoiLienHeDAO();
        coHoiDAO = new CoHoiDAO();
        giaiDoanPipelineDAO = new GiaiDoanPipelineDAO();
        khachHang360DAO = new KhachHang360DAO();

        service = new LeadChuyenDoiService(leadDAO, khachHangDAO, nguoiLienHeDAO, coHoiDAO, giaiDoanPipelineDAO);

        // Clear tables
        try (Statement st = rootConnection.createStatement()) {
            st.execute("DELETE FROM hoat_dong");
            st.execute("DELETE FROM co_hoi");
            st.execute("DELETE FROM lich_su_lien_he_cong_ty");
            st.execute("DELETE FROM nguoi_lien_he");
            st.execute("DELETE FROM khach_hang");
            st.execute("DELETE FROM `lead`");
            st.execute("DELETE FROM giai_doan_pipeline");
            st.execute("DELETE FROM pipeline_ban_hang");
            st.execute("DELETE FROM nguoi_dung");

            // Setup default pipeline and active stages
            st.execute("INSERT INTO pipeline_ban_hang (id, ma_pipeline, ten_pipeline, mac_dinh, hoat_dong) " +
                    "VALUES (1, 'PIPELINE_DEFAULT', 'Quy trình chuẩn B2B', 1, 1)");

            st.execute("INSERT INTO giai_doan_pipeline (id, pipeline_id, ma_giai_doan, ten_giai_doan, thu_tu, xac_suat_mac_dinh, hoat_dong) " +
                    "VALUES (1, 1, 'TIEP_CAN', 'Tiếp cận', 1, 10, 1)");
            st.execute("INSERT INTO giai_doan_pipeline (id, pipeline_id, ma_giai_doan, ten_giai_doan, thu_tu, xac_suat_mac_dinh, hoat_dong) " +
                    "VALUES (2, 1, 'KHAO_SAT', 'Khảo sát nhu cầu', 2, 25, 1)");
        }

        // Setup users
        salesRepUser = new NguoiDung(701L, "Nguyễn Văn Sales", "sales701@crm.vn");
        salesRepUser.themVaiTro(VaiTroEnum.SALES_REP);
        salesRepUser.setNhomKinhDoanhId(10);

        otherSalesRepUser = new NguoiDung(702L, "Trần Văn Sales", "sales702@crm.vn");
        otherSalesRepUser.themVaiTro(VaiTroEnum.SALES_REP);
        otherSalesRepUser.setNhomKinhDoanhId(10);

        teamLeadUser = new NguoiDung(801L, "Lê Trưởng Nhóm", "lead801@crm.vn");
        teamLeadUser.themVaiTro(VaiTroEnum.TEAM_LEAD);
        teamLeadUser.setNhomKinhDoanhId(10);

        directorUser = new NguoiDung(901L, "Phạm Giám Đốc", "director901@crm.vn");
        directorUser.themVaiTro(VaiTroEnum.DIRECTOR);

        adminUser = new NguoiDung(1L, "Admin Hệ Thống", "admin@crm.vn");
        adminUser.themVaiTro(VaiTroEnum.ADMIN);

        marketingUser = new NguoiDung(501L, "Vũ Marketing", "marketing501@crm.vn");
        marketingUser.themVaiTro(VaiTroEnum.MARKETING);
    }

    private Lead taoLeadMau(String maLead, String tenCongTy, String hoTen, String email, String sdt, Long ownerId, Long nhomId, TrangThaiLeadEnum trangThai) {
        Lead lead = new Lead();
        lead.setMaLead(maLead);
        lead.setTenCongTy(tenCongTy);
        lead.setHoTen(hoTen);
        lead.setEmail(email);
        lead.setSoDienThoai(sdt);
        lead.setNguonLeadId(1L);
        lead.setNhuCauQuanTam("Tìm giải pháp CRM quản trị");
        lead.setNguoiPhuTrachId(ownerId);
        lead.setNhomPhanBoId(nhomId);
        lead.setTrangThai(trangThai);
        lead.setDuocNhanLuc(LocalDateTime.now().minusDays(1));
        Long id = leadDAO.them(lead);
        lead.setId(id);
        return lead;
    }

    @Test
    @DisplayName("T01: Chuyển đổi Lead thành công sinh đồng thời 3 thực thể (AC1, AC2, AC3, AC4)")
    void testChuyenDoiLeadThanhCong() throws SQLException {
        Lead lead = taoLeadMau("LEAD-001", "Công ty TNHH Sao Bắc Mẫu", "Nguyễn Văn Đại Diện",
                "lienhe@saobac.example", "0901234567", 701L, 10L, TrangThaiLeadEnum.DANG_CHAM_SOC);

        // Thêm 3 hoạt động ghi trên lead
        try (Statement st = rootConnection.createStatement()) {
            st.execute("INSERT INTO hoat_dong (ma_hoat_dong, tieu_de, loai_hoat_dong, lead_id) " +
                    "VALUES ('HD-01', 'Gọi điện khảo sát', 'CUOC_GOI', " + lead.getId() + ")");
            st.execute("INSERT INTO hoat_dong (ma_hoat_dong, tieu_de, loai_hoat_dong, lead_id) " +
                    "VALUES ('HD-02', 'Gặp gỡ demo', 'CUOC_GAP', " + lead.getId() + ")");
            st.execute("INSERT INTO hoat_dong (ma_hoat_dong, tieu_de, loai_hoat_dong, lead_id) " +
                    "VALUES ('HD-03', 'Gửi email báo giá', 'EMAIL', " + lead.getId() + ")");
        }

        // Preview GET check
        ChuyenDoiLeadPreviewDTO preview = service.layPreviewChuyenDoi(lead.getId(), salesRepUser);
        assertNotNull(preview);
        assertTrue(preview.isQuyenChuyenDoi());
        assertTrue(preview.getLeadChuyenDoi().isCanConvert());
        assertEquals(3, preview.getLeadChuyenDoi().getActivityCount());
        assertEquals("Cơ hội - Công ty TNHH Sao Bắc Mẫu", preview.getThongTinTaoCoHoi().getTenCoHoi());
        assertEquals(0, BigDecimal.ZERO.compareTo(preview.getThongTinTaoCoHoi().getGiaTriDuKien()));
        assertEquals(LocalDate.now().plusDays(30), preview.getThongTinTaoCoHoi().getNgayChotDuKien());

        // POST Convert
        KetQuaChuyenDoiLeadDTO kq = service.chuyenDoiLead(lead.getId(), salesRepUser);
        assertNotNull(kq);
        assertTrue(kq.isThanhCong(), "Chuyển đổi phải thành công: " + kq.getThongBao());
        assertNotNull(kq.getKhachHangId());
        assertNotNull(kq.getNguoiLienHeId());
        assertNotNull(kq.getCoHoiId());
        assertEquals("DA_CHUYEN_DOI", kq.getTrangThaiLead());
        assertEquals(3, kq.getSoHoatDongBaoToan());

        // Kiểm tra Khách hàng được tạo đúng
        KhachHang kh = khachHangDAO.timTheoId(kq.getKhachHangId());
        assertNotNull(kh);
        assertEquals("Công ty TNHH Sao Bắc Mẫu", kh.getTenCongTy());
        assertEquals(701L, kh.getNguoiSoHuuId(), "Owner của Khách hàng phải kế thừa từ Lead (Q5)");
        assertEquals(10L, kh.getNhomKinhDoanhId());
        assertEquals("TIEM_NANG", kh.getTrangThai());

        // Kiểm tra Người liên hệ được tạo đúng
        java.util.Optional<NguoiLienHe> optNlh = nguoiLienHeDAO.timTheoId(kq.getNguoiLienHeId());
        assertTrue(optNlh.isPresent());
        NguoiLienHe nlh = optNlh.get();
        assertEquals("Nguyễn Văn Đại Diện", nlh.getHoTen());
        assertEquals("lienhe@saobac.example", nlh.getEmail());
        assertEquals("0901234567", nlh.getSoDienThoai());
        assertTrue(nlh.isLaDauMoiChinh());

        // Kiểm tra Cơ hội được tạo đúng mặc định server (Q3)
        CoHoi ch = coHoiDAO.timTheoId(kq.getCoHoiId());
        assertNotNull(ch);
        assertEquals("Cơ hội - Công ty TNHH Sao Bắc Mẫu", ch.getTenCoHoi());
        assertEquals(kh.getId(), ch.getKhachHangId());
        assertEquals(nlh.getId(), ch.getNguoiLienHeChinhId());
        assertEquals(lead.getId(), ch.getLeadId());
        assertEquals(701L, ch.getNguoiPhuTrachId(), "Owner của Cơ hội phải kế thừa từ Lead (Q5)");
        assertEquals(0, BigDecimal.ZERO.compareTo(ch.getGiaTriDuKien()), "Giá trị dự kiến mặc định là 0 VND");
        assertEquals(LocalDate.now().plusDays(30), ch.getNgayChotDuKien(), "Ngày chốt dự kiến là now + 30 ngày");

        // AC3: Kiểm tra trạng thái Lead và FK liên kết
        Lead updatedLead = leadDAO.timTheoId(lead.getId());
        assertEquals(TrangThaiLeadEnum.DA_CHUYEN_DOI, updatedLead.getTrangThai());
        assertEquals(kh.getId(), updatedLead.getKhachHangChuyenDoiId());
        assertEquals(nlh.getId(), updatedLead.getNguoiLienHeChuyenDoiId());
        assertEquals(ch.getId(), updatedLead.getCoHoiChuyenDoiId());
        assertNotNull(updatedLead.getChuyenDoiLuc());

        // AC4: Kiểm tra toàn bộ 3 hoạt động được bảo toàn và truy vấn được qua Khách hàng 360
        int count360 = khachHang360DAO.demTongSoHoatDong(kh.getId());
        assertEquals(3, count360, "Khách hàng 360 phải truy vấn được cả 3 hoạt động của Lead gốc qua liên kết");
    }

    @Test
    @DisplayName("T02: Chặn sửa Lead khi đã chuyển đổi (AC3 Guard)")
    void testChanSuaLeadKhiDaChuyenDoi() throws SQLException {
        Lead lead = taoLeadMau("LEAD-002", "Công ty Cổ Phần Alpha", "Lê Văn Alpha",
                "alpha@example.com", "0911223344", 701L, 10L, TrangThaiLeadEnum.DANG_CHAM_SOC);

        // Chuyển đổi lead
        KetQuaChuyenDoiLeadDTO kq = service.chuyenDoiLead(lead.getId(), salesRepUser);
        assertTrue(kq.isThanhCong());

        // Cố gắng cập nhật lead đã chuyển đổi qua leadDAO.capNhat
        lead.setTenCongTy("Công ty Alpha Đã Đổi Tên");
        assertThrows(IllegalStateException.class, () -> leadDAO.capNhat(lead),
                "Phải ném IllegalStateException khi cố gắng sửa lead DA_CHUYEN_DOI");

        // Cố gắng chuyển đổi lần 2 -> 409 LEAD_ALREADY_CONVERTED
        KetQuaChuyenDoiLeadDTO kq2 = service.chuyenDoiLead(lead.getId(), salesRepUser);
        assertFalse(kq2.isThanhCong());
        assertEquals("LEAD_ALREADY_CONVERTED", kq2.getMaLoi());
    }

    @Test
    @DisplayName("T03: Chặn chuyển đổi khi doanh nghiệp đã tồn tại (Q2 / 409 Conflict)")
    void testChanChuyenDoiKhiDoanhNghiepDaTonTai() throws SQLException {
        // Tạo sẵn khách hàng có tên trùng
        KhachHang khSan = new KhachHang();
        khSan.setTenCongTy("Công ty TNHH Toàn Cầu");
        khSan.setTenChuanHoa("toan cau");
        khSan.setNguoiSoHuuId(701L);
        khachHangDAO.themKhachHang(khSan);

        // Tạo lead có cùng tên công ty
        Lead lead = taoLeadMau("LEAD-003", "Công ty TNHH Toàn Cầu", "Trần Khách",
                "toancau@lead.vn", "0988776655", 701L, 10L, TrangThaiLeadEnum.DANG_CHAM_SOC);

        // Preview phải có cảnh báo trùng
        ChuyenDoiLeadPreviewDTO preview = service.layPreviewChuyenDoi(lead.getId(), salesRepUser);
        assertFalse(preview.getCanhBaoTrung().isEmpty());
        assertFalse(preview.getLeadChuyenDoi().isCanConvert());

        // POST Convert phải bị chặn với 409 CUSTOMER_ALREADY_EXISTS
        KetQuaChuyenDoiLeadDTO kq = service.chuyenDoiLead(lead.getId(), salesRepUser);
        assertFalse(kq.isThanhCong());
        assertEquals("CUSTOMER_ALREADY_EXISTS", kq.getMaLoi());

        // Đảm bảo Lead chưa bị đổi trạng thái
        Lead lDb = leadDAO.timTheoId(lead.getId());
        assertEquals(TrangThaiLeadEnum.DANG_CHAM_SOC, lDb.getTrangThai());
    }

    @Test
    @DisplayName("T04: Chặn chuyển đổi khi dữ liệu nguồn Lead không đủ (Q1 / 422)")
    void testChanChuyenDoiKhiThieuDuLieuNguon() {
        // Lead thiếu tên công ty và số điện thoại + email sai
        Lead lead = taoLeadMau("LEAD-004", "", "Nguyễn Văn Test",
                "invalid-email", null, 701L, 10L, TrangThaiLeadEnum.DANG_CHAM_SOC);

        KetQuaChuyenDoiLeadDTO kq = service.chuyenDoiLead(lead.getId(), salesRepUser);
        assertFalse(kq.isThanhCong());
        assertEquals("LEAD_SOURCE_INVALID", kq.getMaLoi());
        assertFalse(kq.getLoiTruong().isEmpty());
        assertTrue(kq.getLoiTruong().containsKey("tenCongTy"));
    }

    @Test
    @DisplayName("T05: Chặn chuyển đổi khi Lead chưa ở trạng thái Đang chăm sóc (Q1 / 409)")
    void testChanChuyenDoiKhiLeadChuaDangChamSoc() {
        // Lead ở trạng thái MOI (chưa nhận)
        Lead lead = taoLeadMau("LEAD-005", "Công ty TNHH Mới", "Nguyễn Khách Mới",
                "moi@demo.vn", "0912345678", 701L, 10L, TrangThaiLeadEnum.MOI);

        KetQuaChuyenDoiLeadDTO kq = service.chuyenDoiLead(lead.getId(), salesRepUser);
        assertFalse(kq.isThanhCong());
        assertEquals("LEAD_NOT_QUALIFIED", kq.getMaLoi());
    }

    @Test
    @DisplayName("T06: Kiểm tra quyền phân cấp: Sales Rep khác owner bị từ chối 403 (Q5)")
    void testPhanQuyenSalesRepKhacOwner() {
        Lead lead = taoLeadMau("LEAD-006", "Công ty Của Sales 701", "Khách 701",
                "khach701@crm.vn", "0909999999", 701L, 10L, TrangThaiLeadEnum.DANG_CHAM_SOC);

        // otherSalesRepUser (702) cố chuyển lead của 701
        KetQuaChuyenDoiLeadDTO kq = service.chuyenDoiLead(lead.getId(), otherSalesRepUser);
        assertFalse(kq.isThanhCong());
        assertEquals("LEAD_ACCESS_DENIED", kq.getMaLoi());
    }

    @Test
    @DisplayName("T07: Team Lead chuyển đổi được lead trong nhóm của mình (Q5)")
    void testTeamLeadChuyenDoiLeadTrongNhom() {
        Lead lead = taoLeadMau("LEAD-007", "Công ty Nhóm 10", "Khách Nhóm 10",
                "nhom10@crm.vn", "0908888888", 701L, 10L, TrangThaiLeadEnum.DANG_CHAM_SOC);

        // teamLeadUser quản lý nhóm 10
        KetQuaChuyenDoiLeadDTO kq = service.chuyenDoiLead(lead.getId(), teamLeadUser);
        assertTrue(kq.isThanhCong());

        // Kiểm tra owner vẫn là Sales Rep 701, không bị gán sang Team Lead
        KhachHang kh = khachHangDAO.timTheoId(kq.getKhachHangId());
        assertEquals(701L, kh.getNguoiSoHuuId());
    }

    @Test
    @DisplayName("T08: Director và Admin chuyển đổi được mọi lead (Q5)")
    void testDirectorVaAdminChuyenDoiLead() {
        Lead leadDir = taoLeadMau("LEAD-008A", "Công ty Director Test", "Khách A",
                "dir@test.vn", "0907777777", 701L, 10L, TrangThaiLeadEnum.DANG_CHAM_SOC);
        KetQuaChuyenDoiLeadDTO kqDir = service.chuyenDoiLead(leadDir.getId(), directorUser);
        assertTrue(kqDir.isThanhCong());

        Lead leadAdm = taoLeadMau("LEAD-008B", "Công ty Admin Test", "Khách B",
                "adm@test.vn", "0906666666", 701L, 10L, TrangThaiLeadEnum.DANG_CHAM_SOC);
        KetQuaChuyenDoiLeadDTO kqAdm = service.chuyenDoiLead(leadAdm.getId(), adminUser);
        assertTrue(kqAdm.isThanhCong());
    }

    @Test
    @DisplayName("T09: Marketing và Accountant không có quyền chuyển đổi Lead (Q5)")
    void testMarketingKhongCoQuyenChuyenDoi() {
        Lead lead = taoLeadMau("LEAD-009", "Công ty Marketing Test", "Khách Mkt",
                "mkt@test.vn", "0905555555", 701L, 10L, TrangThaiLeadEnum.DANG_CHAM_SOC);

        KetQuaChuyenDoiLeadDTO kq = service.chuyenDoiLead(lead.getId(), marketingUser);
        assertFalse(kq.isThanhCong());
        assertEquals("LEAD_ACCESS_DENIED", kq.getMaLoi());
    }

    @Test
    @DisplayName("T10: Lead không có hoạt động vẫn chuyển đổi thành công (Mock M4)")
    void testLeadKhongCoHoatDongVanChuyenDoiThanhCong() throws SQLException {
        Lead lead = taoLeadMau("LEAD-010", "Công ty Zero Activity", "Khách Không Hoạt Động",
                "zero@test.vn", "0904444444", 701L, 10L, TrangThaiLeadEnum.DANG_CHAM_SOC);

        KetQuaChuyenDoiLeadDTO kq = service.chuyenDoiLead(lead.getId(), salesRepUser);
        assertTrue(kq.isThanhCong());
        assertEquals(0, kq.getSoHoatDongBaoToan());

        // Trang 360 trả về 0 hoạt động (không lỗi)
        int count = khachHang360DAO.demTongSoHoatDong(kq.getKhachHangId());
        assertEquals(0, count);
    }

    @Test
    @DisplayName("T11: Chuyển đổi thất bại -> Transaction rollback toàn bộ (không tạo rác dữ liệu)")
    void testChuyenDoiThatBai_RollbackToanBo() throws SQLException {
        // Tạo khách hàng trùng trước
        KhachHang khSan = new KhachHang();
        khSan.setTenCongTy("Công ty Đã Có Sẵn Trùng");
        khSan.setTenChuanHoa("da co san trung");
        khSan.setNguoiSoHuuId(701L);
        khachHangDAO.themKhachHang(khSan);

        // Tạo lead trùng tên công ty
        Lead lead = taoLeadMau("LEAD-011", "Công ty Đã Có Sẵn Trùng", "Khách Rollback",
                "rollback@test.vn", "0903333333", 701L, 10L, TrangThaiLeadEnum.DANG_CHAM_SOC);

        int soKhachHangTruoc = 0;
        try (var rs = rootConnection.createStatement().executeQuery("SELECT COUNT(*) FROM khach_hang")) {
            if (rs.next()) soKhachHangTruoc = rs.getInt(1);
        }

        KetQuaChuyenDoiLeadDTO kq = service.chuyenDoiLead(lead.getId(), salesRepUser);
        assertFalse(kq.isThanhCong());
        assertEquals("CUSTOMER_ALREADY_EXISTS", kq.getMaLoi());

        // Kiểm tra số lượng khách hàng không tăng
        int soKhachHangSau = 0;
        try (var rs = rootConnection.createStatement().executeQuery("SELECT COUNT(*) FROM khach_hang")) {
            if (rs.next()) soKhachHangSau = rs.getInt(1);
        }
        assertEquals(soKhachHangTruoc, soKhachHangSau, "Không được tạo thêm khách hàng khi rollback");

        // Lead vẫn ở trạng thái DANG_CHAM_SOC và chưa có FK
        Lead leadSau = leadDAO.timTheoId(lead.getId());
        assertEquals(TrangThaiLeadEnum.DANG_CHAM_SOC, leadSau.getTrangThai());
        assertNull(leadSau.getKhachHangChuyenDoiId());
        assertNull(leadSau.getNguoiLienHeChuyenDoiId());
        assertNull(leadSau.getCoHoiChuyenDoiId());
    }
}
