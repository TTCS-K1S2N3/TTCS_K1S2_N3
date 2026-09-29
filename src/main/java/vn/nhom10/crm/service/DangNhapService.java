package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.util.PasswordUtil;

import java.sql.Timestamp;

/**
 * Service xử lý xác thực đăng nhập người dùng.
 */
public class DangNhapService {

    public static final int SO_LAN_SAI_TOI_DA = 5;
    public static final int THOI_GIAN_KHOA_PHUT = 15;

    private final NguoiDungDAO nguoiDungDAO;

    public DangNhapService() {
        this.nguoiDungDAO = new NguoiDungDAO();
    }

    public DangNhapService(NguoiDungDAO nguoiDungDAO) {
        this.nguoiDungDAO = nguoiDungDAO;
    }

    public static class KetQuaDangNhap {
        private final boolean thanhCong;
        private final String thongBaoLoi;
        private final NguoiDung nguoiDung;

        public KetQuaDangNhap(boolean thanhCong, String thongBaoLoi, NguoiDung nguoiDung) {
            this.thanhCong = thanhCong;
            this.thongBaoLoi = thongBaoLoi;
            this.nguoiDung = nguoiDung;
        }

        public boolean isThanhCong() {
            return thanhCong;
        }

        public String getThongBaoLoi() {
            return thongBaoLoi;
        }

        public NguoiDung getNguoiDung() {
            return nguoiDung;
        }
    }

    /**
     * Xác thực thông tin đăng nhập bằng email và mật khẩu
     */
    public KetQuaDangNhap dangNhap(String email, String matKhau) {
        if (email == null || email.trim().isEmpty() || matKhau == null || matKhau.trim().isEmpty()) {
            return new KetQuaDangNhap(false, "Vui lòng nhập đầy đủ email và mật khẩu.", null);
        }

        String emailChuanHoa = email.trim().toLowerCase();
        NguoiDung nguoiDung = nguoiDungDAO.timTheoEmail(emailChuanHoa);

        // Thông báo chung bảo mật khi sai thông tin
        String thongBaoChung = "Email hoặc mật khẩu không chính xác.";

        if (nguoiDung == null) {
            return new KetQuaDangNhap(false, thongBaoChung, null);
        }

        if (!nguoiDung.dangHoatDong()) {
            return new KetQuaDangNhap(false, "Tài khoản của bạn đã bị vô hiệu hóa hoặc chưa kích hoạt.", null);
        }

        // Kiểm tra khóa tạm thời
        long nowMillis = System.currentTimeMillis();
        if (nguoiDung.getThoiGianKhoa() != null && nguoiDung.getThoiGianKhoa().getTime() > nowMillis) {
            return new KetQuaDangNhap(false, "Tài khoản tạm thời bị khóa do nhập sai nhiều lần. Vui lòng thử lại sau.", null);
        }

        // Kiểm tra mật khẩu
        boolean matKhauDung = PasswordUtil.kiemTraMatKhau(matKhau, nguoiDung.getMatKhau());
        if (!matKhauDung) {
            int soLanSaiMoi = nguoiDung.getSoLanSai() + 1;
            Timestamp thoiGianKhoa = null;
            if (soLanSaiMoi >= SO_LAN_SAI_TOI_DA) {
                thoiGianKhoa = new Timestamp(nowMillis + THOI_GIAN_KHOA_PHUT * 60 * 1000L);
            }
            nguoiDungDAO.capNhatDangNhapThatBai(nguoiDung.getId(), soLanSaiMoi, thoiGianKhoa);

            if (soLanSaiMoi >= SO_LAN_SAI_TOI_DA) {
                return new KetQuaDangNhap(false, "Bạn đã nhập sai 5 lần liên tiếp. Tài khoản bị tạm khóa 15 phút.", null);
            }
            return new KetQuaDangNhap(false, thongBaoChung, null);
        }

        // Đăng nhập thành công -> reset số lần sai
        Timestamp now = new Timestamp(nowMillis);
        nguoiDungDAO.capNhatDangNhapThanhCong(nguoiDung.getId(), now);
        nguoiDung.setSoLanSai(0);
        nguoiDung.setThoiGianKhoa(null);

        return new KetQuaDangNhap(true, null, nguoiDung);
    }
}
