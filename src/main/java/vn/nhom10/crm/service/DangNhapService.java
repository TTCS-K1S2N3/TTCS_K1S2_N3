package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dto.KetQuaDangNhapDTO;
import vn.nhom10.crm.model.ModuleHeThong;
import vn.nhom10.crm.model.MucQuyen;
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
    private final PermissionService permissionService;

    public DangNhapService() {
        this(new NguoiDungDAO(), PermissionService.getInstance());
    }

    public DangNhapService(NguoiDungDAO nguoiDungDAO) {
        this(nguoiDungDAO, PermissionService.getInstance());
    }

    public DangNhapService(NguoiDungDAO nguoiDungDAO, PermissionService permissionService) {
        this.nguoiDungDAO = nguoiDungDAO;
        this.permissionService = permissionService != null ? permissionService : PermissionService.getInstance();
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
     * Áp dụng nguyên tắc điều hướng động theo DB permission và trạng thái triển khai thật:
     * 1. Ưu tiên trang làm việc phù hợp vai trò (nếu có quyền READ+ VÀ route GET thực sự khả dụng/đã triển khai).
     * 2. Tuyệt đối không chọn module NONE, chưa có Servlet/JSP hoặc demo /dieu-huong.
     * 3. Fallback an toàn về /ho-so nếu không còn module nào khả dụng.
     *
     * @param user Người dùng đã xác thực
     * @return Đường dẫn trang chủ tương ứng (/nguoi-dung, /khach-hang, /danh-muc, /ho-so)
     */
    public String xacDinhTrangChu(NguoiDung user) {
        if (user == null || !user.dangHoatDong()) {
            return "/dang-nhap";
        }

        // Nếu người dùng không có bất kỳ vai trò nào được phân công: Fallback an toàn về /ho-so
        boolean coVaiTro = (user.getDanhSachVaiTro() != null && !user.getDanhSachVaiTro().isEmpty())
                || (user.getDanhSachVaiTroEnum() != null && !user.getDanhSachVaiTroEnum().isEmpty());
        if (!coVaiTro) {
            return "/ho-so";
        }

        // 1. Quản trị hệ thống (Admin): Ưu tiên Người dùng & Nhật ký nếu có quyền READ và đã triển khai
        if (user.coVaiTro(VaiTroEnum.ADMIN) || user.coVaiTro("ADMIN")) {
            if (permissionService.coQuyen(user, "NGUOI_DUNG_NHAT_KY", MucQuyen.READ) && ModuleHeThong.NGUOI_DUNG.isDaTrienKhai()) {
                return "/nguoi-dung";
            }
        }

        // 2. Ưu tiên các module nghiệp vụ kinh doanh đã triển khai thật mà user có quyền READ+
        // Ưu tiên 1: Khách hàng & Liên hệ (/khach-hang)
        if (permissionService.coQuyen(user, "KHACH_HANG", MucQuyen.READ) && ModuleHeThong.KHACH_HANG.isDaTrienKhai()) {
            return "/khach-hang";
        }

        // Ưu tiên 2: Danh mục & Cấu hình bán hàng (/danh-muc)
        if (permissionService.coQuyen(user, "DANH_MUC", MucQuyen.READ) && ModuleHeThong.DANH_MUC.isDaTrienKhai()) {
            return "/danh-muc";
        }

        // Ưu tiên 3: Người dùng & Nhật ký (/nguoi-dung) (ví dụ Director có quyền READ)
        if (permissionService.coQuyen(user, "NGUOI_DUNG_NHAT_KY", MucQuyen.READ) && ModuleHeThong.NGUOI_DUNG.isDaTrienKhai()) {
            return "/nguoi-dung";
        }

        // 3. Fallback an toàn: Nếu không còn module nghiệp vụ nào khả dụng hoặc bị NONE:
        // Đưa về trang hồ sơ cá nhân /ho-so, tuyệt đối không vào /dieu-huong, không 403/404, không lặp redirect
        return "/ho-so";
    }
}
