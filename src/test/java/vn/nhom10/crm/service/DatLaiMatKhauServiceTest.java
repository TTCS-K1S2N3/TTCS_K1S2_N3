package vn.nhom10.crm.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dao.DatLaiMatKhauTokenDAO;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.model.DatLaiMatKhauToken;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.util.PasswordUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử nghiệp vụ đặt lại mật khẩu DatLaiMatKhauService (Story S1-03)")
class DatLaiMatKhauServiceTest {

    private static final String H2_URL = "jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1";
    private Connection initConn;
    private DatLaiMatKhauService service;
    private NguoiDungDAO nguoiDungDAO;
    private DatLaiMatKhauTokenDAO tokenDAO;
    private EmailService emailService;

    @BeforeEach
    void setUp() throws Exception {
        // Thiết lập supplier cấp connection tới H2 in-memory DB
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection(H2_URL);
            } catch (SQLException e) {
                throw new RuntimeException("Không thể kết nối H2 test DB", e);
            }
        });

        initConn = DriverManager.getConnection(H2_URL);
        try (Statement stmt = initConn.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");

            // Tạo bảng vai_tro & nguoi_dung_vai_tro
            stmt.execute("CREATE TABLE vai_tro ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "ma_vai_tro VARCHAR(50) NOT NULL UNIQUE, "
                    + "ten_vai_tro VARCHAR(100) NOT NULL, "
                    + "mo_ta VARCHAR(255) NULL, "
                    + "pham_vi_toi_da VARCHAR(50) NOT NULL DEFAULT 'CA_NHAN'"
                    + ")");

            stmt.execute("CREATE TABLE nguoi_dung_vai_tro ("
                    + "nguoi_dung_id BIGINT NOT NULL, "
                    + "vai_tro_id INT NOT NULL, "
                    + "PRIMARY KEY (nguoi_dung_id, vai_tro_id)"
                    + ")");

            // Tạo bảng nguoi_dung
            stmt.execute("CREATE TABLE nguoi_dung ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "ho_ten VARCHAR(100) NOT NULL, "
                    + "email VARCHAR(150) NOT NULL UNIQUE, "
                    + "mat_khau VARCHAR(255) NOT NULL, "
                    + "so_dien_thoai VARCHAR(20) NULL, "
                    + "trang_thai VARCHAR(50) NOT NULL DEFAULT 'HOAT_DONG', "
                    + "so_lan_sai INT NOT NULL DEFAULT 0, "
                    + "thoi_gian_khoa TIMESTAMP NULL, "
                    + "nhom_kinh_doanh_id INT NULL, "
                    + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + ")");

            // Tạo bảng dat_lai_mat_khau_token
            stmt.execute("CREATE TABLE dat_lai_mat_khau_token ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "nguoi_dung_id BIGINT NOT NULL, "
                    + "token VARCHAR(255) NOT NULL UNIQUE, "
                    + "thoi_gian_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "thoi_gian_het_han TIMESTAMP NOT NULL, "
                    + "da_su_dung BOOLEAN NOT NULL DEFAULT FALSE, "
                    + "thoi_gian_su_dung TIMESTAMP NULL, "
                    + "FOREIGN KEY (nguoi_dung_id) REFERENCES nguoi_dung(id) ON DELETE CASCADE"
                    + ")");
        }

        nguoiDungDAO = new NguoiDungDAO();
        tokenDAO = new DatLaiMatKhauTokenDAO();
        emailService = EmailService.getInstance();
        emailService.clearLastSentLinks();

        service = new DatLaiMatKhauService(nguoiDungDAO, tokenDAO, emailService);
    }

    @AfterEach
    void tearDown() throws Exception {
        DatabaseConfig.resetConnectionSupplier();
        if (initConn != null && !initConn.isClosed()) {
            initConn.close();
        }
    }

    private NguoiDung createSampleUser(String email) {
        String sql = "INSERT INTO nguoi_dung (ho_ten, email, mat_khau, so_dien_thoai, trang_thai, so_lan_sai, created_at, updated_at) "
                   + "VALUES (?, ?, ?, ?, 'HOAT_DONG', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        long id = 0;
        try (Connection conn = DriverManager.getConnection(H2_URL);
             var ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "Nguyễn Văn Bán Hàng");
            ps.setString(2, email);
            ps.setString(3, PasswordUtil.hashPassword("OldPassword123"));
            ps.setString(4, "0987654321");
            ps.executeUpdate();
            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    id = rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tạo user mẫu trong test H2", e);
        }

        NguoiDung user = new NguoiDung();
        user.setId(id);
        user.setHoTen("Nguyễn Văn Bán Hàng");
        user.setEmail(email);
        user.setMatKhau(PasswordUtil.hashPassword("OldPassword123"));
        user.setSoDienThoai("0987654321");
        user.setTrangThai("HOAT_DONG");
        user.setSoLanSai(0);
        return user;
    }

    @Test
    @DisplayName("AC3: Email không tồn tại vẫn hiển thị cùng một thông báo")
    void testAC3_EmailKhongTonTaiVanHienThiCungThongBao() {
        String emailKhongTonTai = "khongtontai@crm.vn";
        String baseUrl = "http://localhost:8080/crm-ban-hang";

        // Thực hiện yêu cầu với email không có trong DB
        DatLaiMatKhauService.KetQuaXuLy ketQua1 = service.yeuCauDatLaiMatKhau(emailKhongTonTai, baseUrl);
        assertTrue(ketQua1.isThanhCong());
        assertEquals(DatLaiMatKhauService.THONG_BAO_GUI_EMAIL_CHUNG, ketQua1.getThongBao());

        // Tạo một user thật và thực hiện với email tồn tại
        String emailTonTai = "sales@crm.vn";
        createSampleUser(emailTonTai);

        DatLaiMatKhauService.KetQuaXuLy ketQua2 = service.yeuCauDatLaiMatKhau(emailTonTai, baseUrl);
        assertTrue(ketQua2.isThanhCong());
        // AC3: Cùng một thông báo giống hệt nhau
        assertEquals(ketQua1.getThongBao(), ketQua2.getThongBao());
    }

    @Test
    @DisplayName("AC1: Nhập email nhận được liên kết đặt lại có hiệu lực 30 phút")
    void testAC1_LienKetDatLaiCoHieuLuc30Phut() {
        String email = "nhanvien1@crm.vn";
        NguoiDung user = createSampleUser(email);
        String baseUrl = "http://localhost:8080/crm-ban-hang";

        DatLaiMatKhauService.KetQuaXuLy ketQua = service.yeuCauDatLaiMatKhau(email, baseUrl);
        assertTrue(ketQua.isThanhCong());

        // Kiểm tra liên kết đã được tạo và gửi
        String sentLink = emailService.getLastSentLink(email);
        assertNotNull(sentLink);
        assertTrue(sentLink.contains("token="));

        String tokenString = sentLink.substring(sentLink.indexOf("token=") + 6);
        DatLaiMatKhauToken token = tokenDAO.findByToken(tokenString);
        assertNotNull(token);
        assertEquals(user.getId(), token.getNguoiDungId());
        assertFalse(token.isDaSuDung());

        // Kiểm tra thời gian hết hạn chính xác 30 phút sau thời gian tạo
        long diffSeconds = java.time.Duration.between(token.getThoiGianTao(), token.getThoiGianHetHan()).getSeconds();
        assertEquals(1800, diffSeconds, 2); // 30 phút = 1800 giây

        // Kiểm tra token khi còn hiệu lực
        DatLaiMatKhauService.KetQuaXuLy ketQuaKiemTra = service.kiemTraToken(tokenString);
        assertTrue(ketQuaKiemTra.isThanhCong());
        assertEquals(DatLaiMatKhauService.TrangThaiToken.HOP_LE, ketQuaKiemTra.getTrangThaiToken());

        // Kiểm tra trường hợp token đã quá 30 phút (hết hạn)
        token.setThoiGianHetHan(LocalDateTime.now().minusMinutes(1)); // Đã hết hạn trước 1 phút
        // Update lại token hết hạn vào DB
        try (Connection c = DatabaseConfig.getConnection();
             Statement s = c.createStatement()) {
            s.execute("UPDATE dat_lai_mat_khau_token SET thoi_gian_het_han = DATEADD('MINUTE', -1, CURRENT_TIMESTAMP) WHERE token = '" + tokenString + "'");
        } catch (Exception e) {
            fail("Lỗi cập nhật test token hết hạn: " + e.getMessage());
        }

        DatLaiMatKhauService.KetQuaXuLy ketQuaHetHan = service.kiemTraToken(tokenString);
        assertFalse(ketQuaHetHan.isThanhCong());
        assertEquals(DatLaiMatKhauService.TrangThaiToken.DA_HET_HAN, ketQuaHetHan.getTrangThaiToken());
        assertTrue(ketQuaHetHan.getThongBao().contains("hết hạn"));
    }

    @Test
    @DisplayName("AC2: Liên kết chỉ dùng được một lần")
    void testAC2_LienKetChiDungDuocMotLan() {
        String email = "nhanvien2@crm.vn";
        createSampleUser(email);
        String baseUrl = "http://localhost:8080/crm-ban-hang";

        service.yeuCauDatLaiMatKhau(email, baseUrl);
        String sentLink = emailService.getLastSentLink(email);
        String tokenString = sentLink.substring(sentLink.indexOf("token=") + 6);

        // Đổi mật khẩu lần đầu: thành công
        String matKhauMoi = "MatKhauMoi2026";
        DatLaiMatKhauService.KetQuaXuLy ketQua1 = service.datLaiMatKhau(tokenString, matKhauMoi, matKhauMoi);
        assertTrue(ketQua1.isThanhCong(), "Lần 1 phải thành công");
        assertTrue(ketQua1.getThongBao().contains("thành công"));

        // Kiểm tra mật khẩu trong DB đã thay đổi và hash BCrypt khớp
        NguoiDung userUpdated = nguoiDungDAO.timTheoEmail(email);
        assertTrue(PasswordUtil.checkPassword(matKhauMoi, userUpdated.getMatKhau()));

        // Kiểm tra token đã được đánh dấu sử dụng
        DatLaiMatKhauToken tokenDb = tokenDAO.findByToken(tokenString);
        assertTrue(tokenDb.isDaSuDung());
        assertNotNull(tokenDb.getThoiGianSuDung());

        // AC2: Cố gắng dùng lại cùng liên kết đó lần 2: PHẢI BỊ TỪ CHỐI
        DatLaiMatKhauService.KetQuaXuLy ketQuaKiemTra = service.kiemTraToken(tokenString);
        assertFalse(ketQuaKiemTra.isThanhCong());
        assertEquals(DatLaiMatKhauService.TrangThaiToken.DA_SU_DUNG, ketQuaKiemTra.getTrangThaiToken());
        assertTrue(ketQuaKiemTra.getThongBao().contains("đã được sử dụng"));

        DatLaiMatKhauService.KetQuaXuLy ketQua2 = service.datLaiMatKhau(tokenString, "MatKhauMoiThuHai999", "MatKhauMoiThuHai999");
        assertFalse(ketQua2.isThanhCong(), "Lần 2 phải thất bại vì token chỉ dùng 1 lần");
        assertEquals(DatLaiMatKhauService.TrangThaiToken.DA_SU_DUNG, ketQua2.getTrangThaiToken());
    }

    @Test
    @DisplayName("Validation: Kiểm tra mật khẩu không đủ 8 ký tự hoặc không khớp")
    void testValidationMatKhau() {
        String email = "nhanvien3@crm.vn";
        createSampleUser(email);
        service.yeuCauDatLaiMatKhau(email, "http://localhost:8080/crm-ban-hang");
        String sentLink = emailService.getLastSentLink(email);
        String tokenString = sentLink.substring(sentLink.indexOf("token=") + 6);

        // Mật khẩu dưới 8 ký tự
        DatLaiMatKhauService.KetQuaXuLy ketQuaNgan = service.datLaiMatKhau(tokenString, "Abc1", "Abc1");
        assertFalse(ketQuaNgan.isThanhCong());
        assertTrue(ketQuaNgan.getThongBao().contains("tối thiểu 8 ký tự"));

        // Mật khẩu không có số
        DatLaiMatKhauService.KetQuaXuLy ketQuaKhongSo = service.datLaiMatKhau(tokenString, "Abcdefghijk", "Abcdefghijk");
        assertFalse(ketQuaKhongSo.isThanhCong());
        assertTrue(ketQuaKhongSo.getThongBao().contains("cả chữ cái và số"));

        // Mật khẩu xác nhận không khớp
        DatLaiMatKhauService.KetQuaXuLy ketQuaKhongKhop = service.datLaiMatKhau(tokenString, "MatKhauChuan123", "MatKhauKhac456");
        assertFalse(ketQuaKhongKhop.isThanhCong());
        assertTrue(ketQuaKhongKhop.getThongBao().contains("không trùng khớp"));
    }

    @Test
    @DisplayName("S1-03: Yêu cầu reset lần hai sau cooldown tạo token mới, gửi mail mới và vô hiệu hóa token cũ")
    void testYeuCauResetLanHaiSauCooldown_TaoTokenMoiVaVoHieuHoaTokenCu() throws Exception {
        String email = "resend_user@crm.vn";
        createSampleUser(email);
        String baseUrl = "http://localhost:8080/crm-ban-hang";

        // Lần 1: Yêu cầu reset mật khẩu
        DatLaiMatKhauService.KetQuaXuLy ketQua1 = service.yeuCauDatLaiMatKhau(email, baseUrl);
        assertTrue(ketQua1.isThanhCong());

        String link1 = emailService.getLastSentLink(email);
        String tokenStr1 = link1.substring(link1.indexOf("token=") + 6);
        DatLaiMatKhauToken tokenDb1 = tokenDAO.findByToken(tokenStr1);
        assertNotNull(tokenDb1);
        assertFalse(tokenDb1.isDaSuDung(), "Token 1 ban đầu chưa được sử dụng");

        // Giả lập đã qua thời gian cooldown 60 giây (lùi thời gian tạo về 65 giây trước)
        try (Connection c = DatabaseConfig.getConnection();
             Statement s = c.createStatement()) {
            s.execute("UPDATE dat_lai_mat_khau_token SET thoi_gian_tao = DATEADD('SECOND', -65, CURRENT_TIMESTAMP) WHERE token = '" + tokenStr1 + "'");
        }

        // Lần 2: Yêu cầu reset mật khẩu sau cooldown
        DatLaiMatKhauService.KetQuaXuLy ketQua2 = service.yeuCauDatLaiMatKhau(email, baseUrl);
        assertTrue(ketQua2.isThanhCong());

        String link2 = emailService.getLastSentLink(email);
        String tokenStr2 = link2.substring(link2.indexOf("token=") + 6);
        assertNotEquals(tokenStr1, tokenStr2, "Token lần 2 phải là token mới khác biệt");

        // Kiểm tra token 1 đã bị vô hiệu hóa theo thiết kế an toàn
        DatLaiMatKhauToken tokenDb1SauResend = tokenDAO.findByToken(tokenStr1);
        assertTrue(tokenDb1SauResend.isDaSuDung(), "Token cũ phải bị vô hiệu hóa khi người dùng yêu cầu liên kết mới");

        // Kiểm tra token 2 mới tạo có hiệu lực
        DatLaiMatKhauToken tokenDb2 = tokenDAO.findByToken(tokenStr2);
        assertNotNull(tokenDb2);
        assertFalse(tokenDb2.isDaSuDung(), "Token mới phải hợp lệ và chưa sử dụng");

        // Kiểm tra token 1 bị từ chối khi truy cập
        DatLaiMatKhauService.KetQuaXuLy kiemTraToken1 = service.kiemTraToken(tokenStr1);
        assertFalse(kiemTraToken1.isThanhCong(), "Token 1 cũ phải bị từ chối");
        assertEquals(DatLaiMatKhauService.TrangThaiToken.DA_SU_DUNG, kiemTraToken1.getTrangThaiToken());

        // Kiểm tra token 2 được chấp nhận khi truy cập
        DatLaiMatKhauService.KetQuaXuLy kiemTraToken2 = service.kiemTraToken(tokenStr2);
        assertTrue(kiemTraToken2.isThanhCong(), "Token 2 mới phải hợp lệ");
        assertEquals(DatLaiMatKhauService.TrangThaiToken.HOP_LE, kiemTraToken2.getTrangThaiToken());
    }

    @Test
    @DisplayName("S1-03: Yêu cầu gửi lại trong vòng 60 giây cooldown trả thông báo chung chống user enumeration và không sinh token mới")
    void testYeuCauResetTrongThoiGianCooldown_ChongUserEnumerationVaKhongTaoTokenMoi() {
        String email = "spam_user@crm.vn";
        createSampleUser(email);
        String baseUrl = "http://localhost:8080/crm-ban-hang";

        // Lần 1: Thành công
        DatLaiMatKhauService.KetQuaXuLy ketQua1 = service.yeuCauDatLaiMatKhau(email, baseUrl);
        assertTrue(ketQua1.isThanhCong());
        assertEquals(DatLaiMatKhauService.THONG_BAO_GUI_EMAIL_CHUNG, ketQua1.getThongBao());

        String link1 = emailService.getLastSentLink(email);

        // Lần 2 ngay lập tức (< 60s):
        // Vẫn phải trả về thông báo chung giống hệt để kẻ tấn công không thể dò biết email tồn tại
        DatLaiMatKhauService.KetQuaXuLy ketQua2 = service.yeuCauDatLaiMatKhau(email, baseUrl);
        assertTrue(ketQua2.isThanhCong(), "Phải trả về thành công an toàn để chống user enumeration");
        assertEquals(DatLaiMatKhauService.THONG_BAO_GUI_EMAIL_CHUNG, ketQua2.getThongBao());

        // Kiểm tra link không bị ghi đè / không sinh token mới khi bị chặn bởi cooldown
        String link2 = emailService.getLastSentLink(email);
        assertEquals(link1, link2, "Không được gửi link mới khi chưa qua cooldown");
    }

    @Test
    @DisplayName("S1-03: Lỗi SMTP không làm lộ tài khoản ra UI và không làm mất token cũ còn hiệu lực")
    void testLoiSMTPKhongLamLoThongTinTaiKhoanVaKhongMatTokenCu() throws Exception {
        String email = "smtp_err_user@crm.vn";
        createSampleUser(email);
        String baseUrl = "http://localhost:8080/crm-ban-hang";

        // Lần 1: Gửi thành công với EmailService thật, sinh token 1
        DatLaiMatKhauService.KetQuaXuLy ketQua1 = service.yeuCauDatLaiMatKhau(email, baseUrl);
        assertTrue(ketQua1.isThanhCong());
        String link1 = emailService.getLastSentLink(email);
        String tokenStr1 = link1.substring(link1.indexOf("token=") + 6);
        DatLaiMatKhauToken tokenDb1 = tokenDAO.findByToken(tokenStr1);
        assertNotNull(tokenDb1);
        assertFalse(tokenDb1.isDaSuDung(), "Token 1 ban đầu hợp lệ");

        // Giả lập qua cooldown 60 giây
        try (Connection c = DatabaseConfig.getConnection();
             Statement s = c.createStatement()) {
            s.execute("UPDATE dat_lai_mat_khau_token SET thoi_gian_tao = DATEADD('SECOND', -65, CURRENT_TIMESTAMP) WHERE token = '" + tokenStr1 + "'");
        }

        // Lần 2: Giả lập SMTP gặp lỗi
        EmailService mockEmail = org.mockito.Mockito.mock(EmailService.class);
        org.mockito.Mockito.when(mockEmail.guiEmailDatLaiMatKhau(org.mockito.ArgumentMatchers.anyString(), 
                                                                 org.mockito.ArgumentMatchers.anyString(), 
                                                                 org.mockito.ArgumentMatchers.anyString()))
                           .thenReturn(false);

        DatLaiMatKhauService serviceLoi = new DatLaiMatKhauService(nguoiDungDAO, tokenDAO, mockEmail);
        DatLaiMatKhauService.KetQuaXuLy ketQua2 = serviceLoi.yeuCauDatLaiMatKhau(email, baseUrl);

        // 1. Phản hồi cho UI phải là thông báo chung để không làm lộ trạng thái tài khoản
        assertTrue(ketQua2.isThanhCong());
        assertEquals(DatLaiMatKhauService.THONG_BAO_GUI_EMAIL_CHUNG, ketQua2.getThongBao());

        // 2. Token cũ 1 KHÔNG bị vô hiệu hóa oan, người dùng vẫn có thể dùng link cũ trong hộp thư
        DatLaiMatKhauToken tokenDb1SauLoiSmtp = tokenDAO.findByToken(tokenStr1);
        assertNotNull(tokenDb1SauLoiSmtp);
        assertFalse(tokenDb1SauLoiSmtp.isDaSuDung(), "Token cũ phải được giữ nguyên hợp lệ khi SMTP lỗi");

        DatLaiMatKhauService.KetQuaXuLy kiemTraToken1 = service.kiemTraToken(tokenStr1);
        assertTrue(kiemTraToken1.isThanhCong(), "Token cũ vẫn dùng được bình thường");
    }

    @Test
    @DisplayName("S1-03: Bảo mật tuyệt đối - Phản hồi UI đồng nhất 100% giữa mọi kịch bản chống User Enumeration")
    void testChongUserEnumeration_PhanHoiDongNhatTuyetDoi() {
        String baseUrl = "http://localhost:8080/crm-ban-hang";

        // Kịch bản A: Email không tồn tại
        DatLaiMatKhauService.KetQuaXuLy resKhongTonTai = service.yeuCauDatLaiMatKhau("khong_ton_tai_123@crm.vn", baseUrl);

        // Kịch bản B: Email tồn tại (lần 1)
        String emailTonTai = "user_ton_tai_456@crm.vn";
        createSampleUser(emailTonTai);
        DatLaiMatKhauService.KetQuaXuLy resTonTaiLan1 = service.yeuCauDatLaiMatKhau(emailTonTai, baseUrl);

        // Kịch bản C: Email tồn tại (đang trong cooldown < 60s)
        DatLaiMatKhauService.KetQuaXuLy resTrongCooldown = service.yeuCauDatLaiMatKhau(emailTonTai, baseUrl);

        // Kịch bản D: Email tồn tại nhưng SMTP lỗi
        EmailService mockEmail = org.mockito.Mockito.mock(EmailService.class);
        org.mockito.Mockito.when(mockEmail.guiEmailDatLaiMatKhau(org.mockito.ArgumentMatchers.anyString(), 
                                                                 org.mockito.ArgumentMatchers.anyString(), 
                                                                 org.mockito.ArgumentMatchers.anyString()))
                           .thenReturn(false);
        String emailSmtpLoi = "user_smtp_loi_789@crm.vn";
        createSampleUser(emailSmtpLoi);
        DatLaiMatKhauService serviceMock = new DatLaiMatKhauService(nguoiDungDAO, tokenDAO, mockEmail);
        DatLaiMatKhauService.KetQuaXuLy resSmtpLoi = serviceMock.yeuCauDatLaiMatKhau(emailSmtpLoi, baseUrl);

        // Xác nhận CẢ 4 KỊCH BẢN đều trả về thông báo chung giống hệt nhau
        assertTrue(resKhongTonTai.isThanhCong());
        assertTrue(resTonTaiLan1.isThanhCong());
        assertTrue(resTrongCooldown.isThanhCong());
        assertTrue(resSmtpLoi.isThanhCong());

        assertEquals(DatLaiMatKhauService.THONG_BAO_GUI_EMAIL_CHUNG, resKhongTonTai.getThongBao());
        assertEquals(DatLaiMatKhauService.THONG_BAO_GUI_EMAIL_CHUNG, resTonTaiLan1.getThongBao());
        assertEquals(DatLaiMatKhauService.THONG_BAO_GUI_EMAIL_CHUNG, resTrongCooldown.getThongBao());
        assertEquals(DatLaiMatKhauService.THONG_BAO_GUI_EMAIL_CHUNG, resSmtpLoi.getThongBao());
    }
}
