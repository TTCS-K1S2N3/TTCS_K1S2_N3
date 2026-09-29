package vn.nhom10.crm.service;

import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dao.PhienDangNhapDAO;
import vn.nhom10.crm.dto.KetQuaDoiMatKhauDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.util.PasswordUtil;
import vn.nhom10.crm.util.SessionRegistry;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service xử lý nghiệp vụ Đổi mật khẩu đang đăng nhập và thu hồi các phiên khác (Story S1-04).
 * Toàn bộ transaction được quản lý chặt chẽ, tự động ROLLBACK khi có lỗi phát sinh.
 */
public class DoiMatKhauService {

    private static final Logger LOGGER = Logger.getLogger(DoiMatKhauService.class.getName());

    private final NguoiDungDAO nguoiDungDAO;
    private final PhienDangNhapDAO phienDangNhapDAO;
    private final SessionRegistry sessionRegistry;

    public DoiMatKhauService() {
        this.nguoiDungDAO = new NguoiDungDAO();
        this.phienDangNhapDAO = new PhienDangNhapDAO();
        this.sessionRegistry = SessionRegistry.getInstance();
    }

    public DoiMatKhauService(NguoiDungDAO nguoiDungDAO, PhienDangNhapDAO phienDangNhapDAO, SessionRegistry sessionRegistry) {
        this.nguoiDungDAO = nguoiDungDAO;
        this.phienDangNhapDAO = phienDangNhapDAO;
        this.sessionRegistry = sessionRegistry;
    }

    /**
     * Thực hiện đổi mật khẩu và thu hồi các phiên đăng nhập khác.
     *
     * @param nguoiDungId     ID người dùng đang đăng nhập
     * @param matKhauHienTai  mật khẩu hiện tại do người dùng nhập
     * @param matKhauMoi      mật khẩu mới mong muốn
     * @param xacNhanMatKhau  xác nhận lại mật khẩu mới
     * @param thuHoiPhienKhac cờ thu hồi các phiên khác (AC 3)
     * @param maPhienHienTai  mã phiên hiện tại (không bị thu hồi)
     * @return KetQuaDoiMatKhauDTO chi tiết kết quả xử lý
     */
    public KetQuaDoiMatKhauDTO doiMatKhau(Long nguoiDungId,
                                         String matKhauHienTai,
                                         String matKhauMoi,
                                         String xacNhanMatKhau,
                                         boolean thuHoiPhienKhac,
                                         String maPhienHienTai) {

        // 1. Kiểm tra xác thực người dùng
        if (nguoiDungId == null) {
            return KetQuaDoiMatKhauDTO.thatBai("Phiên làm việc không hợp lệ. Vui lòng đăng nhập lại.", "UNAUTHORIZED");
        }

        NguoiDung nguoiDung = nguoiDungDAO.findById(nguoiDungId);
        if (nguoiDung == null) {
            return KetQuaDoiMatKhauDTO.thatBai("Tài khoản người dùng không tồn tại trong hệ thống.", "USER_NOT_FOUND");
        }

        if (!nguoiDung.isDangHoatDong()) {
            return KetQuaDoiMatKhauDTO.thatBai("Tài khoản đang bị khóa hoặc ngưng hoạt động. Không thể đổi mật khẩu.", "ACCOUNT_INACTIVE");
        }

        // 2. AC 1: Bắt buộc nhập mật khẩu hiện tại
        if (matKhauHienTai == null || matKhauHienTai.trim().isEmpty()) {
            return KetQuaDoiMatKhauDTO.thatBai("Bắt buộc phải nhập mật khẩu hiện tại để xác thực bạn là chủ tài khoản.", "CURRENT_PASSWORD_EMPTY");
        }

        boolean dungMatKhauHienTai = PasswordUtil.checkPassword(matKhauHienTai, nguoiDung.getMatKhau());
        if (!dungMatKhauHienTai) {
            return KetQuaDoiMatKhauDTO.thatBai("Mật khẩu hiện tại không chính xác. Vui lòng kiểm tra lại.", "CURRENT_PASSWORD_INCORRECT");
        }

        // 3. AC 2: Mật khẩu mới tối thiểu 8 ký tự, có chữ và số
        if (matKhauMoi == null || matKhauMoi.trim().isEmpty()) {
            return KetQuaDoiMatKhauDTO.thatBai("Mật khẩu mới không được để trống.", "NEW_PASSWORD_EMPTY");
        }

        if (!PasswordUtil.isValidPassword(matKhauMoi)) {
            return KetQuaDoiMatKhauDTO.thatBai("Mật khẩu mới phải có tối thiểu 8 ký tự, bao gồm cả chữ cái và chữ số.", "PASSWORD_WEAK");
        }

        if (matKhauMoi.equals(matKhauHienTai)) {
            return KetQuaDoiMatKhauDTO.thatBai("Mật khẩu mới không được trùng với mật khẩu hiện tại.", "PASSWORD_SAME_AS_CURRENT");
        }

        if (xacNhanMatKhau == null || !xacNhanMatKhau.equals(matKhauMoi)) {
            return KetQuaDoiMatKhauDTO.thatBai("Xác nhận mật khẩu mới không trùng khớp.", "CONFIRM_PASSWORD_MISMATCH");
        }

        // 4. Băm mật khẩu mới bằng BCrypt
        String matKhauMoiHash = PasswordUtil.hashPassword(matKhauMoi);

        // 5. AC 3: Thực hiện cập nhật mật khẩu và thu hồi các phiên khác trong Transaction
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            // Cập nhật mật khẩu trong database
            boolean updated = nguoiDungDAO.updateMatKhau(nguoiDungId, matKhauMoiHash, conn);
            if (!updated) {
                conn.rollback();
                return KetQuaDoiMatKhauDTO.thatBai("Không thể cập nhật mật khẩu. Vui lòng thử lại sau.", "UPDATE_FAILED");
            }

            int soPhienDbThuHoi = 0;
            if (thuHoiPhienKhac) {
                // Thu hồi các bản ghi phiên khác trong DB
                soPhienDbThuHoi = phienDangNhapDAO.thuHoiCacPhienKhac(nguoiDungId, maPhienHienTai, conn);
            }

            // Commit transaction
            conn.commit();

            int soPhienMemThuHoi = 0;
            if (thuHoiPhienKhac && sessionRegistry != null) {
                // Thu hồi (invalidate) các HttpSession khác trong bộ nhớ container
                soPhienMemThuHoi = sessionRegistry.thuHoiCacPhienKhac(nguoiDungId, maPhienHienTai);
            }

            int tongThuHoi = Math.max(soPhienDbThuHoi, soPhienMemThuHoi);

            LOGGER.info(() -> String.format(
                    "Người dùng [ID: %d] đã đổi mật khẩu thành công. Thu hồi %d phiên khác.",
                    nguoiDungId, tongThuHoi
            ));

            String thongBao = thuHoiPhienKhac
                    ? "Đổi mật khẩu thành công! Đã thu hồi toàn bộ các phiên đăng nhập khác để bảo vệ an toàn danh mục khách hàng của bạn."
                    : "Đổi mật khẩu thành công!";

            return KetQuaDoiMatKhauDTO.thanhCong(thongBao, tongThuHoi);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi SQL khi thực hiện đổi mật khẩu cho user ID: " + nguoiDungId, e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Lỗi rollback transaction", ex);
                }
            }
            return KetQuaDoiMatKhauDTO.thatBai("Lỗi hệ thống khi cập nhật cơ sở dữ liệu. Vui lòng thử lại.", "DATABASE_ERROR");
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }
}
