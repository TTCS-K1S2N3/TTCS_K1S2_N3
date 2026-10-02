package vn.nhom10.crm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.nhom10.crm.dao.CauHinhHeThongDAO;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dao.PhienDangNhapDAO;
import vn.nhom10.crm.dto.DangNhapResult;
import vn.nhom10.crm.model.NguoiDung;
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

    private final NguoiDungDAO nguoiDungDAO;
    private final CauHinhHeThongDAO cauHinhHeThongDAO;
    private final PhienDangNhapDAO phienDangNhapDAO;

    public AuthService() {
        this.nguoiDungDAO = new NguoiDungDAO();
        this.cauHinhHeThongDAO = new CauHinhHeThongDAO();
        this.phienDangNhapDAO = new PhienDangNhapDAO();
    }

    public AuthService(NguoiDungDAO nguoiDungDAO, CauHinhHeThongDAO cauHinhHeThongDAO, PhienDangNhapDAO phienDangNhapDAO) {
        this.nguoiDungDAO = nguoiDungDAO;
        this.cauHinhHeThongDAO = cauHinhHeThongDAO;
        this.phienDangNhapDAO = phienDangNhapDAO;
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
}
