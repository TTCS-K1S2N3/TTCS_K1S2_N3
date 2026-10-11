package vn.nhom10.crm.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dao.BieuMauLeadDAO;
import vn.nhom10.crm.dao.LeadDAO;
import vn.nhom10.crm.dto.GuiFormLeadDTO;
import vn.nhom10.crm.dto.KetQuaGuiFormDTO;
import vn.nhom10.crm.dto.MaNhungDTO;
import vn.nhom10.crm.model.BieuMauLead;
import vn.nhom10.crm.model.Lead;
import vn.nhom10.crm.model.NhatKyGuiFormLead;
import vn.nhom10.crm.model.TrangThaiLeadEnum;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử BieuMauLeadService - Toàn diện Story S4-01")
class BieuMauLeadServiceTest {

    private static final String H2_URL = "jdbc:h2:mem:crm_s4_01_svc_test;MODE=MySQL;DB_CLOSE_DELAY=-1";
    private static Connection rootConnection;

    private BieuMauLeadDAO bieuMauLeadDAO;
    private LeadDAO leadDAO;
    private BieuMauLeadService service;

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
            st.execute("CREATE TABLE IF NOT EXISTS nguon_lead (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_nguon VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_nguon VARCHAR(150) NOT NULL, " +
                    "thu_tu_hien_thi INT DEFAULT 0, " +
                    "hoat_dong TINYINT DEFAULT 1, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS chien_dich (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_chien_dich VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_chien_dich VARCHAR(200) NOT NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS nguoi_dung (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(150) NOT NULL, " +
                    "email VARCHAR(255) NOT NULL UNIQUE)");

            st.execute("CREATE TABLE IF NOT EXISTS bieu_mau_lead (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_bieu_mau VARCHAR(60) NOT NULL UNIQUE, " +
                    "ten_bieu_mau VARCHAR(200) NOT NULL, " +
                    "token_cong_khai VARCHAR(128) NOT NULL UNIQUE, " +
                    "nguon_lead_id BIGINT NOT NULL, " +
                    "chien_dich_id BIGINT NULL, " +
                    "cau_hinh_truong_json TEXT NULL, " +
                    "gioi_han_gui_moi_gio INT NULL, " +
                    "bat_honeypot TINYINT DEFAULT 1, " +
                    "hoat_dong TINYINT DEFAULT 1, " +
                    "created_by BIGINT NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at DATETIME NULL)");

            st.execute("CREATE TABLE IF NOT EXISTS nhat_ky_gui_form_lead (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "bieu_mau_id BIGINT NOT NULL, " +
                    "dia_chi_ip VARCHAR(45) NOT NULL, " +
                    "thanh_cong TINYINT DEFAULT 0, " +
                    "ly_do_tu_choi VARCHAR(255) NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS `lead` (" +
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

            st.execute("INSERT INTO nguon_lead (id, ma_nguon, ten_nguon) VALUES (1, 'WEB_LANDING', 'Landing Page Website')");
            st.execute("INSERT INTO nguon_lead (id, ma_nguon, ten_nguon) VALUES (2, 'FACEBOOK_ADS', 'Quảng cáo Facebook')");
        }
    }

    @AfterAll
    static void tearDownDatabase() throws Exception {
        if (rootConnection != null && !rootConnection.isClosed()) {
            rootConnection.close();
        }
        DatabaseConfig.resetConnectionSupplier();
    }

    @BeforeEach
    void setUp() throws Exception {
        bieuMauLeadDAO = new BieuMauLeadDAO();
        leadDAO = new LeadDAO();
        service = new BieuMauLeadService(bieuMauLeadDAO, leadDAO);

        try (Statement st = rootConnection.createStatement()) {
            st.execute("DELETE FROM `lead`");
            st.execute("DELETE FROM nhat_ky_gui_form_lead");
            st.execute("DELETE FROM bieu_mau_lead");
        }
    }

    @Test
    @DisplayName("AC1: Sinh mã nhúng cho một biểu mẫu, dán được vào website bất kỳ")
    void testSinhMaNhungBieuMau() {
        BieuMauLead bm = new BieuMauLead();
        bm.setMaBieuMau("FORM_TU_VAN");
        bm.setTenBieuMau("Form tư vấn dịch vụ phần mềm");
        bm.setNguonLeadId(1L);

        BieuMauLead daTao = service.taoBieuMau(bm, 1L);
        assertNotNull(daTao.getId());
        assertNotNull(daTao.getTokenCongKhai());
        assertTrue(daTao.getTokenCongKhai().length() >= 20);

        MaNhungDTO maNhung = service.sinhMaNhung(daTao.getId(), "https://crm.congty.vn");
        assertNotNull(maNhung);
        assertEquals("FORM_TU_VAN", maNhung.getMaBieuMau());
        assertEquals("Form tư vấn dịch vụ phần mềm", maNhung.getTenBieuMau());
        assertEquals(daTao.getTokenCongKhai(), maNhung.getTokenCongKhai());

        // Kiểm tra mã iframe
        assertTrue(maNhung.getMaIframe().contains("<iframe src=\"https://crm.congty.vn/bieu-mau/nhung?token=" + daTao.getTokenCongKhai() + "\""));
        assertTrue(maNhung.getMaIframe().contains("loading=\"lazy\""));

        // Kiểm tra mã script
        assertTrue(maNhung.getMaScript().contains("https://crm.congty.vn/bieu-mau/embed.js?token=" + daTao.getTokenCongKhai()));

        // Kiểm tra đường dẫn trực tiếp
        assertEquals("https://crm.congty.vn/bieu-mau/nhung?token=" + daTao.getTokenCongKhai(), maNhung.getDuongDanTrucTiep());
    }

    @Test
    @DisplayName("AC2: Xác thực các trường bắt buộc của biểu mẫu (Họ tên, Email/SĐT)")
    void testXacThucTruongBieuMau() {
        BieuMauLead bm = new BieuMauLead("FORM_AC2", "Form AC2", "token_ac2", 1L);
        service.taoBieuMau(bm, 1L);

        // Trường hợp 1: Thiếu họ tên -> Bị từ chối
        GuiFormLeadDTO dtoThieuHoTen = new GuiFormLeadDTO("token_ac2", "", "test@gmail.com", "0901234567", "Công ty X", "Nhu cầu ABC");
        dtoThieuHoTen.setDiaChiIp("1.1.1.1");
        KetQuaGuiFormDTO kq1 = service.xuLyGuiForm(dtoThieuHoTen);
        assertFalse(kq1.isThanhCong());
        assertEquals(400, kq1.getMaHttp());
        assertEquals("THIEU_HO_TEN", kq1.getLyDoTuChoi());

        // Trường hợp 2: Thiếu cả email và SĐT -> Bị từ chối
        GuiFormLeadDTO dtoThieuLienHe = new GuiFormLeadDTO("token_ac2", "Nguyễn Văn Test", "", "", "Công ty X", "Nhu cầu ABC");
        dtoThieuLienHe.setDiaChiIp("1.1.1.1");
        KetQuaGuiFormDTO kq2 = service.xuLyGuiForm(dtoThieuLienHe);
        assertFalse(kq2.isThanhCong());
        assertEquals("THIEU_THONG_TIN_LIEN_HE", kq2.getLyDoTuChoi());

        // Trường hợp 3: Email sai định dạng -> Bị từ chối
        GuiFormLeadDTO dtoEmailSai = new GuiFormLeadDTO("token_ac2", "Nguyễn Văn Test", "email-khong-hop-le", "0901234567", "Công ty X", "");
        dtoEmailSai.setDiaChiIp("1.1.1.1");
        KetQuaGuiFormDTO kq3 = service.xuLyGuiForm(dtoEmailSai);
        assertFalse(kq3.isThanhCong());
        assertEquals("EMAIL_KHONG_HOP_LE", kq3.getLyDoTuChoi());

        // Trường hợp 4: SĐT sai định dạng -> Bị từ chối
        GuiFormLeadDTO dtoSdtSai = new GuiFormLeadDTO("token_ac2", "Nguyễn Văn Test", "test@crm.vn", "abcxyz", "Công ty X", "");
        dtoSdtSai.setDiaChiIp("1.1.1.1");
        KetQuaGuiFormDTO kq4 = service.xuLyGuiForm(dtoSdtSai);
        assertFalse(kq4.isThanhCong());
        assertEquals("SDT_KHONG_HOP_LE", kq4.getLyDoTuChoi());
    }

    @Test
    @DisplayName("AC3: Chống spam bằng Honeypot bẫy bot")
    void testChongSpamHoneypot() {
        BieuMauLead bm = new BieuMauLead("FORM_SPAM", "Form chống spam", "token_spam", 1L);
        bm.setBatHoneypot(true);
        service.taoBieuMau(bm, 1L);

        // Giả lập bot điền vào trường ẩn hpWebsite
        GuiFormLeadDTO dtoSpam = new GuiFormLeadDTO("token_spam", "Spam Bot", "bot@spammer.org", "0900000000", "Spam Inc", "Buy cheap drugs");
        dtoSpam.setHpWebsite("http://spamsite.com/ref");
        dtoSpam.setDiaChiIp("123.45.67.89");

        KetQuaGuiFormDTO kq = service.xuLyGuiForm(dtoSpam);
        assertFalse(kq.isThanhCong());
        assertEquals(400, kq.getMaHttp());
        assertEquals("SPAM_HONEYPOT", kq.getLyDoTuChoi());

        // Đảm bảo KHÔNG có lead nào được tạo trong DB
        Lead lead = leadDAO.timTheoMa(kq.getMaLead());
        assertNull(lead);

        // Đảm bảo có bản ghi từ chối trong nhật ký
        List<NhatKyGuiFormLead> nhatKy = bieuMauLeadDAO.layLichSuGui(bm.getId(), 5);
        assertEquals(1, nhatKy.size());
        assertFalse(nhatKy.get(0).isThanhCong());
        assertEquals("SPAM_HONEYPOT", nhatKy.get(0).getLyDoTuChoi());
    }

    @Test
    @DisplayName("AC3: Giới hạn tần suất gửi theo địa chỉ IP (Rate Limit)")
    void testGioiHanTanSuatTheoIp() {
        BieuMauLead bm = new BieuMauLead("FORM_RATE", "Form Rate Limit", "token_rate", 1L);
        bm.setGioiHanGuiMoiGio(3); // Giới hạn tối đa 3 lượt gửi / IP / giờ
        service.taoBieuMau(bm, 1L);

        String ipspam = "118.69.100.50";

        // Lượt 1: Thành công
        GuiFormLeadDTO d1 = new GuiFormLeadDTO("token_rate", "Khách 1", "k1@gmail.com", "0911111111", "Cty 1", "Nhu cầu 1");
        d1.setDiaChiIp(ipspam);
        assertTrue(service.xuLyGuiForm(d1).isThanhCong());

        // Lượt 2: Thành công
        GuiFormLeadDTO d2 = new GuiFormLeadDTO("token_rate", "Khách 2", "k2@gmail.com", "0922222222", "Cty 2", "Nhu cầu 2");
        d2.setDiaChiIp(ipspam);
        assertTrue(service.xuLyGuiForm(d2).isThanhCong());

        // Lượt 3: Thành công
        GuiFormLeadDTO d3 = new GuiFormLeadDTO("token_rate", "Khách 3", "k3@gmail.com", "0933333333", "Cty 3", "Nhu cầu 3");
        d3.setDiaChiIp(ipspam);
        assertTrue(service.xuLyGuiForm(d3).isThanhCong());

        // Lượt 4: Vượt giới hạn 3 lượt -> BỊ CHẶN (HTTP 429)
        GuiFormLeadDTO d4 = new GuiFormLeadDTO("token_rate", "Khách 4", "k4@gmail.com", "0944444444", "Cty 4", "Nhu cầu 4");
        d4.setDiaChiIp(ipspam);
        KetQuaGuiFormDTO kq4 = service.xuLyGuiForm(d4);
        assertFalse(kq4.isThanhCong());
        assertEquals(429, kq4.getMaHttp());
        assertEquals("GIOI_HAN_TAN_SUAT_IP", kq4.getLyDoTuChoi());

        // Địa chỉ IP khác vẫn gửi được bình thường (cách ly theo IP)
        GuiFormLeadDTO dKhac = new GuiFormLeadDTO("token_rate", "Khách Khác", "khac@gmail.com", "0955555555", "Cty Khác", "Nhu cầu");
        dKhac.setDiaChiIp("200.200.200.200");
        KetQuaGuiFormDTO kqKhac = service.xuLyGuiForm(dKhac);
        assertTrue(kqKhac.isThanhCong());
    }

    @Test
    @DisplayName("AC4: Gửi thành công tạo lead ở trạng thái MỚI và gắn đúng nguồn của biểu mẫu")
    void testGuiThanhCongTaoLeadTrangThaiMoiVaGanDungNguon() {
        // Biểu mẫu gắn với Nguồn Lead ID = 2 (FACEBOOK_ADS)
        BieuMauLead bm = new BieuMauLead("FORM_FB", "Form Facebook Ads", "token_fb", 2L);
        service.taoBieuMau(bm, 1L);

        GuiFormLeadDTO dto = new GuiFormLeadDTO(
                "token_fb",
                "Hoàng Kim Ngân",
                "kimngan@techcorp.vn",
                "0978 123 456",
                "Công ty TNHH TechCorp Toàn Cầu",
                "Quan tâm phần mềm CRM gói 50 người dùng"
        );
        dto.setDiaChiIp("14.161.20.15");

        KetQuaGuiFormDTO kq = service.xuLyGuiForm(dto);
        assertTrue(kq.isThanhCong());
        assertNotNull(kq.getMaLead());
        assertTrue(kq.getMaLead().startsWith("LEAD-"));

        // Lấy Lead vừa tạo từ DB để kiểm tra tính toàn vẹn
        Lead lead = leadDAO.timTheoMa(kq.getMaLead());
        assertNotNull(lead);
        assertEquals("Hoàng Kim Ngân", lead.getHoTen());
        assertEquals("kimngan@techcorp.vn", lead.getEmail());
        assertEquals("kimngan@techcorp.vn", lead.getEmailChuanHoa());
        assertEquals("0978 123 456", lead.getSoDienThoai());
        assertEquals("0978123456", lead.getSoDienThoaiChuanHoa());
        assertEquals("Công ty TNHH TechCorp Toàn Cầu", lead.getTenCongTy());
        assertEquals("CÔNG TY TNHH TECHCORP TOÀN CẦU", lead.getTenCongTyChuanHoa());
        assertEquals("Quan tâm phần mềm CRM gói 50 người dùng", lead.getNhuCauQuanTam());

        // AC4: TRẠNG THÁI MỚI
        assertEquals(TrangThaiLeadEnum.MOI, lead.getTrangThai(), "Lead tạo từ biểu mẫu phải có trạng thái là MOI");

        // AC4: GẮN ĐÚNG NGUỒN CỦA BIỂU MẪU (Nguồn ID = 2)
        assertEquals(2L, lead.getNguonLeadId(), "Lead tạo ra phải gắn đúng nguồn của biểu mẫu (ID=2)");
        assertEquals(bm.getId(), lead.getBieuMauId(), "Lead phải giữ liên kết tới biểu mẫu ID sinh ra nó");

        // Kiểm tra nhật ký gửi form thành công
        List<NhatKyGuiFormLead> nhatKy = bieuMauLeadDAO.layLichSuGui(bm.getId(), 5);
        assertEquals(1, nhatKy.size());
        assertTrue(nhatKy.get(0).isThanhCong());
        assertNull(nhatKy.get(0).getLyDoTuChoi());
        assertEquals("14.161.20.15", nhatKy.get(0).getDiaChiIp());
    }
}
