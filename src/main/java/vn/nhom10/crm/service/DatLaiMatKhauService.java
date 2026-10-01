package vn.nhom10.crm.service;

import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dao.DatLaiMatKhauTokenDAO;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.model.DatLaiMatKhauToken;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.util.EmailUtil;
import vn.nhom10.crm.util.PasswordUtil;
import vn.nhom10.crm.util.TokenUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service xử lý nghiệp vụ đặt lại mật khẩu khi quên qua email (Story S1-03).
 * Đảm bảo các Acceptance Criteria:
 * - AC1: Nhập email nhận được liên kết đặt lại có hiệu lực 30 phút.
 * - AC2: Liên kết chỉ dùng được một lần.
 * - AC3: Email không tồn tại vẫn hiển thị cùng một thông báo.
 */
public class DatLaiMatKhauService {

    private static final Logger LOGGER = Logger.getLogger(DatLaiMatKhauService.class.getName());

    public static final String THONG_BAO_GUI_EMAIL_CHUNG =
            "Nếu địa chỉ email của bạn tồn tại trong hệ thống, chúng tôi đã gửi liên kết đặt lại mật khẩu. Vui lòng kiểm tra hộp thư (bao gồm cả thư mục Spam/Rác).";

    private final NguoiDungDAO nguoiDungDAO;
    private final DatLaiMatKhauTokenDAO tokenDAO;
    private final EmailService emailService;

    public DatLaiMatKhauService() {
        this(new NguoiDungDAO(), new DatLaiMatKhauTokenDAO(), EmailService.getInstance());
    }

    public DatLaiMatKhauService(NguoiDungDAO nguoiDungDAO, DatLaiMatKhauTokenDAO tokenDAO, EmailService emailService) {
        this.nguoiDungDAO = nguoiDungDAO;
        this.tokenDAO = tokenDAO;
        this.emailService = emailService;
    }

    /**
     * Trạng thái kiểm tra token.
     */
    public enum TrangThaiToken {
        HOP_LE,
        KHONG_TON_TAI,
        DA_SU_DUNG,
        DA_HET_HAN
    }

    /**
     * Kết quả xử lý nghiệp vụ.
     */
    public static class KetQuaXuLy {
        private final boolean thanhCong;
        private final String thongBao;
        private final TrangThaiToken trangThaiToken;

        public KetQuaXuLy(boolean thanhCong, String thongBao) {
            this(thanhCong, thongBao, null);
        }

        public KetQuaXuLy(boolean thanhCong, String thongBao, TrangThaiToken trangThaiToken) {
            this.thanhCong = thanhCong;
            this.thongBao = thongBao;
            this.trangThaiToken = trangThaiToken;
        }

        public boolean isThanhCong() {
            return thanhCong;
        }

        public String getThongBao() {
            return thongBao;
        }

        public TrangThaiToken getTrangThaiToken() {
            return trangThaiToken;
        }
    }

    /**
     * Xử lý yêu cầu gửi liên kết đặt lại mật khẩu qua email.
     * Tuân thủ AC3: Nếu email không tồn tại trong hệ thống vẫn hiển thị cùng một thông báo.
     * Tuân thủ AC1: Tạo token có thời hạn 30 phút.
     *
     * @param email   địa chỉ email người dùng nhập
     * @param baseUrl URL gốc của ứng dụng (ví dụ: http://localhost:8080/crm-ban-hang)
     * @return KetQuaXuLy
     */
    public KetQuaXuLy yeuCauDatLaiMatKhau(String email, String baseUrl) {
        if (email == null || email.trim().isEmpty()) {
            return new KetQuaXuLy(false, "Vui lòng nhập địa chỉ email công ty của bạn.");
        }

        String trimmedEmail = email.trim();
        if (!EmailUtil.isValidEmail(trimmedEmail)) {
            return new KetQuaXuLy(false, "Định dạng email không hợp lệ. Vui lòng kiểm tra lại.");
        }

        NguoiDung nguoiDung = nguoiDungDAO.timTheoEmail(trimmedEmail);

        // AC3: Email không tồn tại vẫn trả về cùng một thông báo thành công chung
        if (nguoiDung == null) {
            LOGGER.info("Yêu cầu đặt lại mật khẩu cho email không tồn tại: " + trimmedEmail);
            return new KetQuaXuLy(true, THONG_BAO_GUI_EMAIL_CHUNG);
        }

        // Nếu tài khoản bị vô hiệu hóa
        if ("VO_HIEU_HOA".equalsIgnoreCase(nguoiDung.getTrangThai())) {
            LOGGER.warning("Yêu cầu đặt lại mật khẩu cho tài khoản đã bị vô hiệu hóa: " + trimmedEmail);
            return new KetQuaXuLy(true, THONG_BAO_GUI_EMAIL_CHUNG);
        }

        // AC1: Sinh token bảo mật ngẫu nhiên và thiết lập thời hạn 30 phút
        String tokenString = TokenUtil.generateSecureToken();
        LocalDateTime thoiGianTao = LocalDateTime.now();
        LocalDateTime thoiGianHetHan = thoiGianTao.plusMinutes(30);

        DatLaiMatKhauToken token = new DatLaiMatKhauToken();
        token.setNguoiDungId(nguoiDung.getId());
        token.setToken(tokenString);
        token.setThoiGianTao(thoiGianTao);
        token.setThoiGianHetHan(thoiGianHetHan);
        token.setDaSuDung(false);

        Long savedId = tokenDAO.save(token);
        if (savedId == null) {
            LOGGER.severe("Không thể lưu token đặt lại mật khẩu vào cơ sở dữ liệu");
            return new KetQuaXuLy(false, "Hệ thống đang gặp sự cố. Vui lòng thử lại sau.");
        }

        // Xây dựng liên kết đặt lại mật khẩu
        String cleanBaseUrl = (baseUrl != null) ? baseUrl.replaceAll("/+$", "") : "";
        String resetLink = cleanBaseUrl + "/dat-lai-mat-khau?token=" + tokenString;

        // Gửi email thật qua SMTP
        boolean emailSent = emailService.guiEmailDatLaiMatKhau(nguoiDung.getEmail(), nguoiDung.getHoTen(), resetLink);
        if (!emailSent) {
            LOGGER.warning("Không thể gửi email đặt lại mật khẩu tới: " + nguoiDung.getEmail());
        }

        return new KetQuaXuLy(true, THONG_BAO_GUI_EMAIL_CHUNG);
    }

    /**
     * Kiểm tra tính hợp lệ của token khi người dùng truy cập liên kết đặt lại mật khẩu.
     * Đáp ứng AC1 (hiệu lực 30 phút) và AC2 (chỉ dùng một lần).
     *
     * @param tokenString chuỗi token nhận được từ URL
     * @return KetQuaXuLy kèm TrangThaiToken
     */
    public KetQuaXuLy kiemTraToken(String tokenString) {
        if (tokenString == null || tokenString.trim().isEmpty()) {
            return new KetQuaXuLy(false, "Mã liên kết không hợp lệ.", TrangThaiToken.KHONG_TON_TAI);
        }

        DatLaiMatKhauToken token = tokenDAO.findByToken(tokenString.trim());
        if (token == null) {
            return new KetQuaXuLy(false, "Liên kết đặt lại mật khẩu không tồn tại hoặc không hợp lệ.", TrangThaiToken.KHONG_TON_TAI);
        }

        // AC2: Liên kết chỉ dùng được một lần
        if (token.isDaSuDung()) {
            return new KetQuaXuLy(false, "Liên kết đặt lại mật khẩu này đã được sử dụng. Mỗi liên kết chỉ có thể dùng một lần duy nhất.", TrangThaiToken.DA_SU_DUNG);
        }

        // AC1: Liên kết có hiệu lực 30 phút
        if (token.isHetHan()) {
            return new KetQuaXuLy(false, "Liên kết đặt lại mật khẩu đã hết hạn (chỉ có hiệu lực trong 30 phút). Vui lòng yêu cầu liên kết mới.", TrangThaiToken.DA_HET_HAN);
        }

        return new KetQuaXuLy(true, "Liên kết hợp lệ.", TrangThaiToken.HOP_LE);
    }

    /**
     * Thực hiện đặt lại mật khẩu mới cho người dùng.
     * Sử dụng Transaction để đảm bảo tính toàn vẹn:
     * - Cập nhật mật khẩu băm BCrypt.
     * - Đánh dấu token đã dùng (AC2).
     * - Vô hiệu hóa các token khác của người dùng.
     *
     * @param tokenString     chuỗi token
     * @param matKhauMoi      mật khẩu mới
     * @param xacNhanMatKhau  xác nhận mật khẩu mới
     * @return KetQuaXuLy
     */
    public KetQuaXuLy datLaiMatKhau(String tokenString, String matKhauMoi, String xacNhanMatKhau) {
        // Validation đầu vào
        if (tokenString == null || tokenString.trim().isEmpty()) {
            return new KetQuaXuLy(false, "Mã liên kết không hợp lệ.", TrangThaiToken.KHONG_TON_TAI);
        }

        if (matKhauMoi == null || matKhauMoi.trim().isEmpty()) {
            return new KetQuaXuLy(false, "Vui lòng nhập mật khẩu mới.");
        }

        if (!PasswordUtil.isValidPassword(matKhauMoi)) {
            return new KetQuaXuLy(false, "Mật khẩu mới tối thiểu 8 ký tự, phải bao gồm cả chữ cái và số.");
        }

        if (!matKhauMoi.equals(xacNhanMatKhau)) {
            return new KetQuaXuLy(false, "Mật khẩu xác nhận không trùng khớp với mật khẩu mới.");
        }

        // Mở transaction cập nhật
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            DatLaiMatKhauToken token = tokenDAO.findByTokenForUpdate(tokenString.trim(), conn);
            if (token == null) {
                conn.rollback();
                return new KetQuaXuLy(false, "Liên kết đặt lại mật khẩu không tồn tại.", TrangThaiToken.KHONG_TON_TAI);
            }

            // AC2: Kiểm tra đã dùng chưa
            if (token.isDaSuDung()) {
                conn.rollback();
                return new KetQuaXuLy(false, "Liên kết này đã được sử dụng. Mỗi liên kết chỉ có thể dùng một lần.", TrangThaiToken.DA_SU_DUNG);
            }

            // AC1: Kiểm tra hết hạn
            if (token.isHetHan()) {
                conn.rollback();
                return new KetQuaXuLy(false, "Liên kết đặt lại mật khẩu đã hết hạn (chỉ có hiệu lực trong 30 phút).", TrangThaiToken.DA_HET_HAN);
            }

            NguoiDung nguoiDung = nguoiDungDAO.timTheoId(token.getNguoiDungId());
            if (nguoiDung == null) {
                conn.rollback();
                return new KetQuaXuLy(false, "Tài khoản người dùng không tồn tại.");
            }

            // Băm mật khẩu bằng BCrypt
            String matKhauHash = PasswordUtil.hashPassword(matKhauMoi);

            // 1. Cập nhật mật khẩu người dùng
            boolean updatePassSuccess = nguoiDungDAO.capNhatMatKhau(nguoiDung.getId(), matKhauHash, conn);
            if (!updatePassSuccess) {
                conn.rollback();
                return new KetQuaXuLy(false, "Không thể cập nhật mật khẩu mới. Vui lòng thử lại.");
            }

            // 2. Đánh dấu token đã được sử dụng (AC2)
            boolean markSuccess = tokenDAO.markAsUsed(token.getId(), conn);
            if (!markSuccess) {
                conn.rollback();
                return new KetQuaXuLy(false, "Không thể xác nhận token. Vui lòng thử lại.");
            }

            // 3. Vô hiệu hóa các token khác của user này
            tokenDAO.invalidateTokensByNguoiDungId(nguoiDung.getId(), conn);

            // Commit transaction
            conn.commit();
            LOGGER.info("Đặt lại mật khẩu thành công cho người dùng: " + nguoiDung.getEmail());

            return new KetQuaXuLy(true, "Đặt lại mật khẩu thành công! Bạn có thể đăng nhập bằng mật khẩu mới.");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cơ sở dữ liệu trong quá trình đặt lại mật khẩu", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Lỗi rollback transaction", ex);
                }
            }
            return new KetQuaXuLy(false, "Đã xảy ra lỗi hệ thống trong khi cập nhật mật khẩu. Vui lòng thử lại.");
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Lỗi đóng connection", e);
                }
            }
        }
    }
}
