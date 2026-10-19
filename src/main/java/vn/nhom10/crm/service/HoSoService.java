package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dto.KetQuaNguoiDungDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.util.DinhDangUtil;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service xử lý xem và cập nhật hồ sơ cá nhân của người dùng (Story S2-02 & S2-03).
 * Đảm bảo:
 * - AC1: Sửa được họ tên, số điện thoại, chữ ký email.
 * - AC2: Không tự đổi được email, nhóm và vai trò (phân quyền nghiêm ngặt phía server).
 * - AC3: Kiểm tra định dạng số điện thoại Việt Nam hợp lệ.
 */
public class HoSoService {

    private static final Logger LOGGER = Logger.getLogger(HoSoService.class.getName());

    private final NguoiDungDAO nguoiDungDAO;

    public HoSoService() {
        this.nguoiDungDAO = new NguoiDungDAO();
    }

    public HoSoService(NguoiDungDAO nguoiDungDAO) {
        this.nguoiDungDAO = nguoiDungDAO != null ? nguoiDungDAO : new NguoiDungDAO();
    }

    /**
     * Lấy thông tin hồ sơ cá nhân của người dùng theo ID (S2-02).
     *
     * @param nguoiDungId ID người dùng
     * @return NguoiDung đầy đủ thông tin hoặc null nếu không tồn tại
     */
    public NguoiDung layHoSo(long nguoiDungId) {
        if (nguoiDungId <= 0) {
            return null;
        }
        return nguoiDungDAO.timTheoId(nguoiDungId);
    }

    /**
     * Adapter lấy thông tin hồ sơ theo int ID (S2-03 tương thích).
     */
    public NguoiDung layThongTinHoSo(int nguoiDungId) {
        return layHoSo(nguoiDungId);
    }

    /**
     * Cập nhật thông tin hồ sơ cá nhân gồm họ tên, số điện thoại và chữ ký email (AC1 & AC3).
     *
     * @param nguoiDungId ID người dùng đang đăng nhập
     * @param hoTen       Họ và tên mới
     * @param soDienThoai Số điện thoại mới (phải đúng định dạng VN)
     * @param chuKyEmail  Nội dung chữ ký email
     * @return DTO kết quả cập nhật kèm thông báo hoặc danh sách lỗi validation
     */
    public KetQuaNguoiDungDTO capNhatHoSo(long nguoiDungId, String hoTen, String soDienThoai, String chuKyEmail) {
        KetQuaNguoiDungDTO ketQua = new KetQuaNguoiDungDTO();

        if (nguoiDungId <= 0) {
            return KetQuaNguoiDungDTO.thatBai("Tài khoản không hợp lệ hoặc phiên làm việc đã hết hạn.");
        }

        // 1. Validate Họ và tên (AC1)
        if (hoTen == null || hoTen.trim().isEmpty()) {
            ketQua.themLoi("hoTen", "Họ và tên không được để trống.");
        } else if (hoTen.trim().length() > 150) {
            ketQua.themLoi("hoTen", "Họ và tên không được vượt quá 150 ký tự.");
        }

        // 2. Validate định dạng Số điện thoại Việt Nam (AC3)
        if (soDienThoai == null || soDienThoai.trim().isEmpty()) {
            ketQua.themLoi("soDienThoai", "Số điện thoại không được để trống.");
        } else if (!DinhDangUtil.laSoDienThoaiVietNamHopLe(soDienThoai)) {
            ketQua.themLoi("soDienThoai", "Số điện thoại không đúng định dạng số điện thoại Việt Nam (ví dụ: 0901234567 hoặc +84901234567).");
        }

        // 3. Validate Chữ ký email (AC1)
        if (chuKyEmail != null && chuKyEmail.length() > 65535) {
            ketQua.themLoi("chuKyEmail", "Chữ ký email vượt quá độ dài tối đa cho phép.");
        }

        // Nếu có lỗi validation -> Dừng ngay, không tương tác database
        if (!ketQua.getDanhSachLoi().isEmpty()) {
            ketQua.setThanhCong(false);
            ketQua.setThongBao("Thông tin nhập vào không hợp lệ. Vui lòng kiểm tra lại các trường.");
            return ketQua;
        }

        // Chuẩn hóa số điện thoại trước khi lưu DB
        String sdtChuanHoa = DinhDangUtil.chuanHoaSoDienThoai(soDienThoai);
        String hoTenChuanHoa = hoTen.trim();
        String chuKyChuanHoa = (chuKyEmail != null) ? chuKyEmail.trim() : null;

        try {
            boolean thanhCong = nguoiDungDAO.capNhatHoSo(nguoiDungId, hoTenChuanHoa, sdtChuanHoa, chuKyChuanHoa);
            if (thanhCong) {
                NguoiDung ndCapNhat = nguoiDungDAO.timTheoId(nguoiDungId);
                return KetQuaNguoiDungDTO.thanhCong(ndCapNhat, "Cập nhật hồ sơ cá nhân thành công.", null);
            } else {
                return KetQuaNguoiDungDTO.thatBai("Không tìm thấy thông tin tài khoản để cập nhật.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi cập nhật hồ sơ người dùng ID=" + nguoiDungId + ": " + e.getMessage(), e);
            return KetQuaNguoiDungDTO.thatBai("Lỗi hệ thống khi cập nhật hồ sơ: " + e.getMessage());
        }
    }

    /**
     * Adapter cập nhật hồ sơ trả về boolean (S2-03 tương thích).
     */
    public boolean capNhatHoSo(int nguoiDungId, String hoTen, String soDienThoai, String chuKyEmail) {
        KetQuaNguoiDungDTO kq = capNhatHoSo((long) nguoiDungId, hoTen, soDienThoai, chuKyEmail);
        return kq.isThanhCong();
    }

    /**
     * Cập nhật hồ sơ có kiểm tra bảo vệ các trường nhạy cảm không được tự đổi (AC2).
     * Ngăn chặn người dùng cố tình can thiệp request để đổi email, nhóm hoặc vai trò.
     *
     * @param nguoiDungId    ID người dùng
     * @param hoTen          Họ và tên mới
     * @param soDienThoai    Số điện thoại mới
     * @param chuKyEmail     Chữ ký email mới
     * @param emailMoi       Email từ form submit (nếu client gửi lên)
     * @param nhomIdMoi      Nhóm kinh doanh từ form submit (nếu client gửi lên)
     * @param vaiTroIdsMoi   Danh sách vai trò từ form submit (nếu client gửi lên)
     * @return DTO kết quả
     */
    public KetQuaNguoiDungDTO capNhatHoSo(long nguoiDungId, String hoTen, String soDienThoai, String chuKyEmail,
                                         String emailMoi, Integer nhomIdMoi, List<Integer> vaiTroIdsMoi) {
        KetQuaNguoiDungDTO ketQua = new KetQuaNguoiDungDTO();

        NguoiDung hienTai = layHoSo(nguoiDungId);
        if (hienTai == null) {
            return KetQuaNguoiDungDTO.thatBai("Tài khoản không tồn tại trong hệ thống.");
        }

        // AC2: Kiểm tra người dùng không tự đổi email
        if (emailMoi != null && !emailMoi.trim().isEmpty() && !emailMoi.trim().equalsIgnoreCase(hienTai.getEmail())) {
            ketQua.themLoi("email", "Bạn không có quyền tự thay đổi địa chỉ email.");
        }

        // AC2: Kiểm tra người dùng không tự đổi nhóm kinh doanh
        if (nhomIdMoi != null && !Objects.equals(nhomIdMoi, hienTai.getNhomKinhDoanhId())) {
            ketQua.themLoi("nhomId", "Bạn không có quyền tự thay đổi nhóm kinh doanh.");
        }

        // AC2: Kiểm tra người dùng không tự đổi vai trò
        if (vaiTroIdsMoi != null && !vaiTroIdsMoi.isEmpty()) {
            Set<Integer> rolesHienTai = hienTai.getDsVaiTroIds();
            Set<Integer> rolesMoi = new HashSet<>(vaiTroIdsMoi);
            if (!rolesHienTai.equals(rolesMoi)) {
                ketQua.themLoi("vaiTro", "Bạn không có quyền tự thay đổi vai trò hệ thống.");
            }
        }

        if (!ketQua.getDanhSachLoi().isEmpty()) {
            ketQua.setThanhCong(false);
            ketQua.setThongBao("Hành động bị từ chối: Người dùng không được phép tự đổi email, nhóm hoặc vai trò.");
            return ketQua;
        }

        return capNhatHoSo(nguoiDungId, hoTen, soDienThoai, chuKyEmail);
    }
}
