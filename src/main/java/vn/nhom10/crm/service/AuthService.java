package vn.nhom10.crm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.nhom10.crm.dao.CauHinhHeThongDAO;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dao.PhienDangNhapDAO;
import vn.nhom10.crm.dao.TokenDatLaiMatKhauDAO;
import vn.nhom10.crm.dto.DangNhapResult;
import vn.nhom10.crm.dto.DatLaiMatKhauResult;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.TokenDatLaiMatKhau;
import vn.nhom10.crm.util.PasswordUtil;

import java.sql.Timestamp;

/**
 * Service xử lý xác thực và an toàn đăng nhập.
 * Đáp ứng toàn bộ Acceptance Criteria của Story S1-01:
 * - AC1: Đăng nhập đúng vào trang chủ tương ứng với vai trò.
 * - AC2: Sai thông tin hiển thị thông báo chung, không tiết lộ email có tồn tại hay không.
 * - AC3: Khoá tạm 15 phút sau 5 lần sai liên tiếp.
 */
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public static final String THONG_BAO_SAI_THONG_TIN = "Email hoặc mật khẩu không chính xác. Vui lòng kiểm tra lại.";
    public static final String THONG_BAO_KHOA_TAM_15_PHUT = "Tài khoản của bạn đã bị tạm khóa 15 phút do nhập sai mật khẩu 5 lần liên tiếp.";
    public static final String THONG_BAO_DANG_BI_KHOA_TAM = "Tài khoản của bạn đang bị tạm khóa 15 phút do nhập sai mật khẩu quá 5 lần liên tiếp. Vui lòng thử lại sau.";
    public static final String THONG_BAO_TAI_KHOAN_BI_KHOA = "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Quản trị viên.";

    // S1-03 Constants
    public static final String THONG_BAO_DAT_LAI_MAT_KHAU_CHUNG =
            "Nếu email tồn tại trong hệ thống, hướng dẫn đặt lại mật khẩu đã được gửi đến hộp thư của bạn. Vui lòng kiểm tra email và làm theo hướng dẫn.";
    public static final String THONG_BAO_TOKEN_KHONG_HOP_LE =
            "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.";
    public static final String THONG_BAO_TOKEN_DA_SU_DUNG =
            "Liên kết đặt lại mật khẩu này đã được sử dụng.";
    public static final String THONG_BAO_MAT_KHAU_KHONG_HOP_LE =
            "Mật khẩu mới phải có tối thiểu 8 ký tự, bao gồm cả chữ và số.";
    public static final String THONG_BAO_DAT_LAI_THANH_CONG =
            "Đặt lại mật khẩu thành công. Vui lòng đăng nhập bằng mật khẩu mới.";

    private final NguoiDungDAO nguoiDungDAO;
    private final CauHinhHeThongDAO cauHinhHeThongDAO;
    private final PhienDangNhapDAO phienDangNhapDAO;
    private TokenDatLaiMatKhauDAO tokenDatLaiMatKhauDAO;
    private EmailService emailService;

    public AuthService() {
        this.nguoiDungDAO = new NguoiDungDAO();
        this.cauHinhHeThongDAO = new CauHinhHeThongDAO();
        this.phienDangNhapDAO = new PhienDangNhapDAO();
        this.tokenDatLaiMatKhauDAO = new TokenDatLaiMatKhauDAO();
        this.emailService = new EmailService();
    }

    public AuthService(NguoiDungDAO nguoiDungDAO, CauHinhHeThongDAO cauHinhHeThongDAO, PhienDangNhapDAO phienDangNhapDAO) {
        this.nguoiDungDAO = nguoiDungDAO;
        this.cauHinhHeThongDAO = cauHinhHeThongDAO;
        this.phienDangNhapDAO = phienDangNhapDAO;
        this.tokenDatLaiMatKhauDAO = new TokenDatLaiMatKhauDAO();
        this.emailService = new EmailService();
    }

    public AuthService(NguoiDungDAO nguoiDungDAO, CauHinhHeThongDAO cauHinhHeThongDAO,
                       PhienDangNhapDAO phienDangNhapDAO, TokenDatLaiMatKhauDAO tokenDatLaiMatKhauDAO,
                       EmailService emailService) {
        this.nguoiDungDAO = nguoiDungDAO;
        this.cauHinhHeThongDAO = cauHinhHeThongDAO;
        this.phienDangNhapDAO = phienDangNhapDAO;
        this.tokenDatLaiMatKhauDAO = tokenDatLaiMatKhauDAO;
        this.emailService = emailService;
    }

    public void setTokenDatLaiMatKhauDAO(TokenDatLaiMatKhauDAO tokenDatLaiMatKhauDAO) {
        this.tokenDatLaiMatKhauDAO = tokenDatLaiMatKhauDAO;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Xử lý đăng nhập theo email và mật khẩu.
     *
     * @param email         Email đăng nhập của người dùng
     * @param matKhau       Mật khẩu plaintext
     * @param ip            Địa chỉ IP client
     * @param thietBi       Thông tin trình duyệt / thiết bị (User-Agent)
     * @param rawSessionId  Mã sessionId của HttpSession để băm lưu phien_dang_nhap
     * @return DangNhapResult kết quả xác thực
     */
    public DangNhapResult dangNhap(String email, String matKhau, String ip, String thietBi, String rawSessionId) {
        // Validate đầu vào cơ bản
        if (email == null || email.isBlank() || matKhau == null || matKhau.isBlank()) {
            return DangNhapResult.saiThongTin(THONG_BAO_SAI_THONG_TIN);
        }

        String normalizedEmail = email.trim().toLowerCase();
        NguoiDung nguoiDung = nguoiDungDAO.timTheoEmail(normalizedEmail);

        // AC2: Email không tồn tại -> hiển thị cùng thông báo chung, không tiết lộ email có tồn tại hay không
        if (nguoiDung == null) {
            logger.warn("Đăng nhập thất bại: email không tồn tại trong hệ thống");
            return DangNhapResult.saiThongTin(THONG_BAO_SAI_THONG_TIN);
        }

        // Kiểm tra trạng thái tài khoản bị khóa vĩnh viễn
        if ("BI_KHOA".equalsIgnoreCase(nguoiDung.getTrangThai())) {
            logger.warn("Đăng nhập thất bại: tài khoản id {} đang ở trạng thái BI_KHOA", nguoiDung.getId());
            return DangNhapResult.taiKhoanBiKhoa(THONG_BAO_TAI_KHOAN_BI_KHOA);
        }

        long now = System.currentTimeMillis();
        Timestamp khoaDen = nguoiDung.getKhoaDen();

        // AC3: Kiểm tra khóa tạm 15 phút
        if (khoaDen != null && khoaDen.getTime() > now) {
            logger.warn("Đăng nhập thất bại: tài khoản id {} đang bị khóa tạm đến {}", nguoiDung.getId(), khoaDen);
            return DangNhapResult.khoaTam15Phut(THONG_BAO_DANG_BI_KHOA_TAM);
        }

        // Nếu đã qua thời gian khóa tạm thì tự động mở khóa và reset số lần sai
        if (khoaDen != null && khoaDen.getTime() <= now) {
            nguoiDungDAO.moKhoaVaResetDangNhapSai(nguoiDung.getId());
            nguoiDung.setSoLanDangNhapSai(0);
            nguoiDung.setKhoaDen(null);
        }

        // Kiểm tra mật khẩu bằng BCrypt
        boolean passwordMatches = PasswordUtil.checkPassword(matKhau, nguoiDung.getMatKhauHash());

        if (passwordMatches) {
            // AC1: Đăng nhập đúng
            nguoiDungDAO.capNhatDangNhapThanhCong(nguoiDung.getId());
            nguoiDung.setSoLanDangNhapSai(0);
            nguoiDung.setKhoaDen(null);

            // Ghi nhận phiên đăng nhập phía server
            if (rawSessionId != null && !rawSessionId.isBlank()) {
                String maPhienHash = PasswordUtil.sha256Hex(rawSessionId);
                phienDangNhapDAO.taoPhien(
                        nguoiDung.getId(),
                        maPhienHash,
                        nguoiDung.getSessionVersion(),
                        ip,
                        thietBi,
                        30 // 30 phút mặc định theo web.xml
                );
            }

            logger.info("Người dùng {} đăng nhập thành công", nguoiDung.getEmail());
            return DangNhapResult.thanhCong(nguoiDung);
        } else {
            // Đăng nhập sai mật khẩu
            int soLanSaiHienTai = nguoiDung.getSoLanDangNhapSai() + 1;
            int maxAttempts = cauHinhHeThongDAO.layGiaTriInt("SO_LAN_DANG_NHAP_SAI_TOI_DA", 5);
            int lockMinutes = cauHinhHeThongDAO.layGiaTriInt("PHUT_KHOA_DANG_NHAP", 15);

            if (soLanSaiHienTai >= maxAttempts) {
                // AC3: Khóa tạm 15 phút sau 5 lần sai liên tiếp
                Timestamp thoiDiemKhoaDen = new Timestamp(now + (long) lockMinutes * 60 * 1000);
                nguoiDungDAO.capNhatDangNhapSai(nguoiDung.getId(), soLanSaiHienTai, thoiDiemKhoaDen);
                logger.warn("Tài khoản {} bị khóa tạm {} phút sau {} lần sai liên tiếp",
                        nguoiDung.getEmail(), lockMinutes, soLanSaiHienTai);
                return DangNhapResult.khoaTam15Phut(THONG_BAO_KHOA_TAM_15_PHUT);
            } else {
                // AC2: Cập nhật số lần sai và hiển thị thông báo chung
                nguoiDungDAO.capNhatDangNhapSai(nguoiDung.getId(), soLanSaiHienTai, null);
                logger.warn("Người dùng {} nhập sai mật khẩu lần {}", nguoiDung.getEmail(), soLanSaiHienTai);
                return DangNhapResult.saiThongTin(THONG_BAO_SAI_THONG_TIN);
            }
        }
    }

    /**
     * Ghi nhận phiên đăng nhập mới trong cơ sở dữ liệu.
     */
    public boolean ghiNhanPhienDangNhap(Long nguoiDungId, String maPhienHash, int sessionVersion,
                                        String ip, String thietBi, int phutHetHan) {
        return phienDangNhapDAO.taoPhien(nguoiDungId, maPhienHash, sessionVersion, ip, thietBi, phutHetHan);
    }

    /**
     * S1-03-AC1, AC3: Xử lý yêu cầu đặt lại mật khẩu khi quên qua email.
     * - S1-03-AC1: Tạo liên kết đặt lại có hiệu lực 30 phút và gửi qua email.
     * - S1-03-AC3: Email không tồn tại vẫn trả về cùng một thông báo chung (chống user enumeration).
     */
    public vn.nhom10.crm.dto.DatLaiMatKhauResult yeuCauDatLaiMatKhau(String email, String ip, String baseUrl) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return vn.nhom10.crm.dto.DatLaiMatKhauResult.thatBai("Vui lòng nhập địa chỉ email công ty hợp lệ.");
        }

        NguoiDung nguoiDung = nguoiDungDAO.timTheoEmail(email.trim().toLowerCase());

        // AC3: Email không tồn tại hoặc tài khoản không hoạt động vẫn hiển thị cùng một thông báo
        if (nguoiDung == null || !"HOAT_DONG".equalsIgnoreCase(nguoiDung.getTrangThai())) {
            logger.warn("Yêu cầu đặt lại mật khẩu cho email không tồn tại hoặc tài khoản không hoạt động: {}", email);
            return vn.nhom10.crm.dto.DatLaiMatKhauResult.thanhCong(THONG_BAO_DAT_LAI_MAT_KHAU_CHUNG);
        }

        int phutHetHan = cauHinhHeThongDAO.layGiaTriInt("PHUT_HET_HAN_RESET_MAT_KHAU", 30);
        String rawToken = java.util.UUID.randomUUID().toString().replace("-", "") + Long.toHexString(System.nanoTime());
        String tokenHash = PasswordUtil.sha256Hex(rawToken);

        boolean taoOk = tokenDatLaiMatKhauDAO.taoToken(nguoiDung.getId(), tokenHash, phutHetHan, ip);
        if (!taoOk) {
            logger.error("Không tạo được token đặt lại mật khẩu trong DB cho user {}", nguoiDung.getId());
            return vn.nhom10.crm.dto.DatLaiMatKhauResult.thatBai("Có lỗi xảy ra khi tạo liên kết đặt lại mật khẩu. Vui lòng thử lại sau.");
        }

        String resetLink = baseUrl + "/reset-password?token=" + rawToken;
        emailService.guiEmailDatLaiMatKhau(nguoiDung.getEmail(), nguoiDung.getHoTen(), resetLink, phutHetHan);

        logger.info("Đã tạo liên kết đặt lại mật khẩu cho user {}, hạn {} phút", nguoiDung.getEmail(), phutHetHan);
        return vn.nhom10.crm.dto.DatLaiMatKhauResult.thanhCong(THONG_BAO_DAT_LAI_MAT_KHAU_CHUNG);
    }

    /**
     * S1-03-AC1, AC2: Kiểm tra tính hợp lệ của token đặt lại mật khẩu.
     * - Chưa sử dụng (da_su_dung_luc IS NULL)
     * - Chưa hết hạn (het_han_luc > NOW())
     */
    public vn.nhom10.crm.dto.DatLaiMatKhauResult kiemTraTokenDatLaiMatKhau(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return vn.nhom10.crm.dto.DatLaiMatKhauResult.tokenKhongHopLe(THONG_BAO_TOKEN_KHONG_HOP_LE);
        }

        String tokenHash = PasswordUtil.sha256Hex(rawToken.trim());
        vn.nhom10.crm.model.TokenDatLaiMatKhau token = tokenDatLaiMatKhauDAO.timToken(tokenHash);

        if (token == null) {
            return vn.nhom10.crm.dto.DatLaiMatKhauResult.tokenKhongHopLe(THONG_BAO_TOKEN_KHONG_HOP_LE);
        }

        if (token.isDaSuDung()) {
            return vn.nhom10.crm.dto.DatLaiMatKhauResult.tokenKhongHopLe(THONG_BAO_TOKEN_DA_SU_DUNG);
        }

        Timestamp dbNow = tokenDatLaiMatKhauDAO.layThoiGianHienTaiDB();
        if (token.isHetHan(dbNow)) {
            return vn.nhom10.crm.dto.DatLaiMatKhauResult.tokenKhongHopLe(THONG_BAO_TOKEN_KHONG_HOP_LE);
        }

        if (!"HOAT_DONG".equalsIgnoreCase(token.getUserTrangThai())) {
            return vn.nhom10.crm.dto.DatLaiMatKhauResult.tokenKhongHopLe("Tài khoản liên kết hiện không ở trạng thái hoạt động.");
        }

        return vn.nhom10.crm.dto.DatLaiMatKhauResult.tokenHopLe();
    }

    /**
     * S1-03-AC2: Đặt lại mật khẩu mới bằng token.
     * - S1-03-AC2: Token chỉ dùng được duy nhất 1 lần (đánh dấu da_su_dung_luc = NOW()).
     */
    public vn.nhom10.crm.dto.DatLaiMatKhauResult datLaiMatKhau(String rawToken, String matKhauMoi, String xacNhanMatKhau) {
        if (rawToken == null || rawToken.isBlank()) {
            return vn.nhom10.crm.dto.DatLaiMatKhauResult.thatBai(THONG_BAO_TOKEN_KHONG_HOP_LE);
        }

        if (matKhauMoi == null || matKhauMoi.isBlank()) {
            return vn.nhom10.crm.dto.DatLaiMatKhauResult.thatBai("Vui lòng nhập mật khẩu mới.");
        }

        if (!matKhauMoi.equals(xacNhanMatKhau)) {
            return vn.nhom10.crm.dto.DatLaiMatKhauResult.thatBai("Mật khẩu xác nhận không khớp với mật khẩu mới.");
        }

        // Mật khẩu mới tối thiểu 8 ký tự, có chữ và số
        if (matKhauMoi.length() < 8 || !matKhauMoi.matches(".*[a-zA-Z].*") || !matKhauMoi.matches(".*\\d.*")) {
            return vn.nhom10.crm.dto.DatLaiMatKhauResult.thatBai(THONG_BAO_MAT_KHAU_KHONG_HOP_LE);
        }

        vn.nhom10.crm.dto.DatLaiMatKhauResult checkToken = kiemTraTokenDatLaiMatKhau(rawToken);
        if (!checkToken.isTokenHopLe()) {
            return checkToken;
        }

        String tokenHash = PasswordUtil.sha256Hex(rawToken.trim());
        vn.nhom10.crm.model.TokenDatLaiMatKhau token = tokenDatLaiMatKhauDAO.timToken(tokenHash);
        if (token == null) {
            return vn.nhom10.crm.dto.DatLaiMatKhauResult.thatBai(THONG_BAO_TOKEN_KHONG_HOP_LE);
        }

        // AC2: Đánh dấu đã sử dụng (chỉ 1 lần duy nhất)
        boolean daDanhDau = tokenDatLaiMatKhauDAO.danhDauDaSuDung(tokenHash);
        if (!daDanhDau) {
            return vn.nhom10.crm.dto.DatLaiMatKhauResult.thatBai(THONG_BAO_TOKEN_DA_SU_DUNG);
        }

        // Cập nhật mật khẩu mới và vô hiệu hóa các phiên đăng nhập cũ qua session_version
        String matKhauHash = PasswordUtil.hashPassword(matKhauMoi);
        boolean capNhatOk = nguoiDungDAO.datLaiMatKhau(token.getNguoiDungId(), matKhauHash);
        if (!capNhatOk) {
            logger.error("Không cập nhật được mật khẩu mới cho user id {}", token.getNguoiDungId());
            return vn.nhom10.crm.dto.DatLaiMatKhauResult.thatBai("Có lỗi xảy ra khi cập nhật mật khẩu. Vui lòng thử lại.");
        }

        logger.info("Đặt lại mật khẩu thành công cho user id {}", token.getNguoiDungId());
        return vn.nhom10.crm.dto.DatLaiMatKhauResult.thanhCong(THONG_BAO_DAT_LAI_THANH_CONG);
    }
}
