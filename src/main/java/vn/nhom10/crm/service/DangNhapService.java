package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dto.KetQuaDangNhapDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.util.PasswordUtil;

/**
 * Service xử lý nghiệp vụ đăng nhập, bảo mật và phân quyền điều hướng người dùng.
 * Đáp ứng đầy đủ Acceptance Criteria của Story S1-01:
 * - Đăng nhập đúng thì vào được trang chủ tương ứng với vai trò
 * - Sai thông tin hiển thị thông báo chung, không tiết lộ email có tồn tại hay không
 * - Khóa tạm 15 phút sau 5 lần sai liên tiếp
 */
public class DangNhapService {

    public static final String THONG_BAO_SAI_THONG_TIN = "Thông tin đăng nhập không chính xác. Vui lòng kiểm tra lại email hoặc mật khẩu.";
    public static final int SO_LAN_SAI_TOI_DA = 5;
    public static final int SO_PHUT_KHOA_TAM = 15;

    private final NguoiDungDAO nguoiDungDAO;

    public DangNhapService() {
        this.nguoiDungDAO = new NguoiDungDAO();
    }

    public DangNhapService(NguoiDungDAO nguoiDungDAO) {
        this.nguoiDungDAO = nguoiDungDAO;
    }

    /**
     * Xác thực thông tin đăng nhập bằng email công ty và mật khẩu.
     *
     * @param email   Email công ty
     * @param matKhau Mật khẩu người dùng nhập
     * @return KetQuaDangNhapDTO chứa kết quả xác thực
     */
    public KetQuaDangNhapDTO dangNhap(String email, String matKhau) {
        if (email == null || email.isBlank() || matKhau == null || matKhau.isBlank()) {
            return KetQuaDangNhapDTO.thatBai("Vui lòng nhập đầy đủ email và mật khẩu.");
        }

        String emailChuan = email.trim().toLowerCase();
        NguoiDung user = nguoiDungDAO.timTheoEmail(emailChuan);

        // AC: Sai thông tin hiển thị thông báo chung, không tiết lộ email có tồn tại hay không
        if (user == null) {
            return KetQuaDangNhapDTO.thatBai(THONG_BAO_SAI_THONG_TIN);
        }

        // Kiểm tra tài khoản bị khóa vĩnh viễn hoặc ngừng hoạt động
        if (NguoiDung.TRANG_THAI_KHOA.equalsIgnoreCase(user.getTrangThai())
                || NguoiDung.TRANG_THAI_NGUNG_HOAT_DONG.equalsIgnoreCase(user.getTrangThai())) {
            return KetQuaDangNhapDTO.taiKhoanBiKhoa();
        }

        // AC: Khóa tạm 15 phút sau 5 lần sai liên tiếp
        if (user.coBiKhoaTam()) {
            long soPhutConLai = user.getSoPhutKhoaConLai();
            return KetQuaDangNhapDTO.khoaTam(soPhutConLai);
        }

        // Nếu đã từng bị khóa tạm nhưng thời gian 15 phút đã trôi qua, tự động mở khóa
        if (user.getThoiGianKhoa() != null) {
            nguoiDungDAO.resetSoLanSai(user.getId());
            user.setSoLanSai(0);
            user.setThoiGianKhoa(null);
        }

        // Kiểm tra mật khẩu băm BCrypt
        boolean matKhauDung = PasswordUtil.checkPassword(matKhau, user.getMatKhau());
        if (!matKhauDung) {
            int soLanMoi = user.getSoLanSai() + 1;
            if (soLanMoi >= SO_LAN_SAI_TOI_DA) {
                nguoiDungDAO.khoaTam(user.getId(), SO_PHUT_KHOA_TAM);
                return KetQuaDangNhapDTO.khoaTam(SO_PHUT_KHOA_TAM);
            } else {
                nguoiDungDAO.tangSoLanSai(user.getId());
                return KetQuaDangNhapDTO.thatBai(THONG_BAO_SAI_THONG_TIN);
            }
        }

        // Đăng nhập thành công: reset bộ đếm lần sai
        if (user.getSoLanSai() > 0 || user.getThoiGianKhoa() != null) {
            nguoiDungDAO.resetSoLanSai(user.getId());
            user.setSoLanSai(0);
            user.setThoiGianKhoa(null);
        }

        // Kích hoạt tài khoản lần đầu nếu đang ở trạng thái CHO_KICH_HOAT (Story S1-08, S2-01)
        if (NguoiDung.TRANG_THAI_CHO_KICH_HOAT.equalsIgnoreCase(user.getTrangThai())) {
            nguoiDungDAO.kichHoatTaiKhoan(user.getId());
            user.setTrangThai(NguoiDung.TRANG_THAI_HOAT_DONG);
        }

        // AC: Đăng nhập đúng thì vào được trang chủ tương ứng với vai trò
        String trangChuUrl = xacDinhTrangChu(user);
        return KetQuaDangNhapDTO.thanhCong(user, trangChuUrl);
    }

    /**
     * Xác định trang chủ tương ứng với vai trò của người dùng theo phân tích nghiệp vụ.
     *
     * @param user Người dùng đã xác thực
     * @return Đường dẫn trang chủ tương ứng (ví dụ /khach-hang, /nguoi-dung, /lead, /hop-dong)
     */
    public String xacDinhTrangChu(NguoiDung user) {
        if (user == null) {
            return "/dang-nhap";
        }

        // Nếu là Admin quản trị hệ thống
        if (user.coVaiTro(VaiTroEnum.ADMIN)) {
            return "/nguoi-dung";
        }

        // Nếu là Marketing -> Trang chủ quản lý Lead
        if (user.coVaiTro(VaiTroEnum.MARKETING)) {
            return "/lead";
        }

        // Nếu là Kế toán -> Quản lý Hợp đồng
        if (user.coVaiTro(VaiTroEnum.ACCOUNTANT)) {
            return "/hop-dong";
        }

        // Các vai trò kinh doanh (Sales Rep, Team Lead, Director, Cust. Success):
        // Theo Story S1-01: "truy cập được danh mục khách hàng của mình một cách an toàn"
        return "/khach-hang";
    }
}
