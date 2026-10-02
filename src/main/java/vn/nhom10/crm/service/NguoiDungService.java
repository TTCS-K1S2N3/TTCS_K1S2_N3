package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dao.NhomKinhDoanhDAO;
import vn.nhom10.crm.dao.VaiTroDAO;
import vn.nhom10.crm.dto.KetQuaNguoiDungDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhomKinhDoanh;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.util.PasswordUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service xử lý toàn bộ logic nghiệp vụ quản lý tài khoản người dùng, phân quyền và gửi email kích hoạt (S1-08).
 */
public class NguoiDungService {

    private static final Logger LOGGER = Logger.getLogger(NguoiDungService.class.getName());
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public static final int SO_BAN_GHI_MAC_DINH = 20;

    private final NguoiDungDAO nguoiDungDAO;
    private final VaiTroDAO vaiTroDAO;
    private final NhomKinhDoanhDAO nhomKinhDoanhDAO;
    private final EmailService emailService;

    public NguoiDungService() {
        this.nguoiDungDAO = new NguoiDungDAO();
        this.vaiTroDAO = new VaiTroDAO();
        this.nhomKinhDoanhDAO = new NhomKinhDoanhDAO();
        this.emailService = new EmailService();
    }

    public NguoiDungService(NguoiDungDAO nguoiDungDAO, VaiTroDAO vaiTroDAO, NhomKinhDoanhDAO nhomKinhDoanhDAO, EmailService emailService) {
        this.nguoiDungDAO = nguoiDungDAO;
        this.vaiTroDAO = vaiTroDAO;
        this.nhomKinhDoanhDAO = nhomKinhDoanhDAO;
        this.emailService = emailService;
    }

    /**
     * AC: Tìm theo tên, email, nhóm; lọc theo vai trò và trạng thái; Danh sách phân trang, mặc định 20 dòng.
     */
    public List<NguoiDung> layDanhSachNguoiDung(String tuKhoa, Integer nhomId, Integer vaiTroId, String trangThai, int trang, int soBanGhiMoiTrang) {
        int limit = soBanGhiMoiTrang > 0 ? soBanGhiMoiTrang : SO_BAN_GHI_MAC_DINH;
        int page = trang > 0 ? trang : 1;
        int offset = (page - 1) * limit;

        return nguoiDungDAO.timKiemVaPhanTrang(tuKhoa, nhomId, vaiTroId, trangThai, limit, offset);
    }

    /**
     * Đếm tổng số lượng người dùng theo tiêu chí tìm kiếm và bộ lọc để tính phân trang.
     */
    public int demTongSoNguoiDung(String tuKhoa, Integer nhomId, Integer vaiTroId, String trangThai) {
        return nguoiDungDAO.demSoLuong(tuKhoa, nhomId, vaiTroId, trangThai);
    }

    /**
     * AC 1 & 2:
     * - Tạo tài khoản gửi email kích hoạt kèm mật khẩu tạm
     * - Email trùng bị từ chối kèm thông báo cụ thể
     *
     * @param nguoiDung  thông tin người dùng mới
     * @param dsVaiTroIds danh sách ID vai trò
     * @param baseUrl     đường dẫn gốc webapp (vd: /crm)
     * @return DTO kết quả xử lý
     */
    public KetQuaNguoiDungDTO taoTaiKhoan(NguoiDung nguoiDung, List<Integer> dsVaiTroIds, String baseUrl) {
        KetQuaNguoiDungDTO ketQua = new KetQuaNguoiDungDTO();

        // 1. Validation dữ liệu đầu vào
        if (nguoiDung.getHoTen() == null || nguoiDung.getHoTen().trim().isEmpty()) {
            ketQua.themLoi("hoTen", "Họ và tên không được để trống.");
        } else if (nguoiDung.getHoTen().trim().length() > 150) {
            ketQua.themLoi("hoTen", "Họ và tên không được vượt quá 150 ký tự.");
        }

        String email = nguoiDung.getEmail() != null ? nguoiDung.getEmail().trim() : "";
        if (email.isEmpty()) {
            ketQua.themLoi("email", "Email công ty không được để trống.");
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            ketQua.themLoi("email", "Địa chỉ email không đúng định dạng hợp lệ.");
        } else if (email.length() > 200) {
            ketQua.themLoi("email", "Email không được vượt quá 200 ký tự.");
        }

        if (dsVaiTroIds == null || dsVaiTroIds.isEmpty()) {
            ketQua.themLoi("vaiTro", "Vui lòng chọn ít nhất một vai trò cho người dùng.");
        }

        // Kiểm tra ràng buộc TEAM_LEAD bắt buộc có nhóm (Story S1-09)
        boolean coVaiTroTruongNhom = false;
        if (dsVaiTroIds != null) {
            for (Integer vtId : dsVaiTroIds) {
                if (vtId == null) continue;
                VaiTro vt = vaiTroDAO.timTheoId(vtId);
                if (vt != null && VaiTroEnum.TEAM_LEAD.getMaVaiTro().equalsIgnoreCase(vt.getMaVaiTro())) {
                    coVaiTroTruongNhom = true;
                    break;
                }
            }
        }

        if (coVaiTroTruongNhom) {
            Integer nhomId = nguoiDung.getNhomKinhDoanhId();
            if (nhomId == null || nhomId <= 0) {
                ketQua.themLoi("nhomId", "Người giữ vai trò Trưởng nhóm kinh doanh bắt buộc phải được gán vào một nhóm kinh doanh cụ thể.");
            } else {
                NhomKinhDoanh nhom = nhomKinhDoanhDAO.timTheoId(nhomId);
                if (nhom == null) {
                    ketQua.themLoi("nhomId", "Nhóm kinh doanh được chọn không tồn tại trong hệ thống.");
                }
            }
        }

        if (!ketQua.getDanhSachLoi().isEmpty()) {
            ketQua.setThanhCong(false);
            ketQua.setThongBao("Dữ liệu không hợp lệ. Vui lòng kiểm tra lại thông tin.");
            return ketQua;
        }

        // 2. AC: Kiểm tra email trùng
        boolean emailDaTonTai = nguoiDungDAO.kiemTraEmailTonTai(email, null);
        if (emailDaTonTai) {
            return KetQuaNguoiDungDTO.loiEmailTrung(email);
        }

        // 3. Sinh mật khẩu tạm ngẫu nhiên và băm BCrypt
        String matKhauTam = PasswordUtil.taoMatKhauTam();
        String matKhauHash = PasswordUtil.hashPassword(matKhauTam);

        nguoiDung.setEmail(email);
        nguoiDung.setHoTen(nguoiDung.getHoTen().trim());
        nguoiDung.setMatKhau(matKhauHash);
        nguoiDung.setTrangThai(NguoiDung.TRANG_THAI_CHO_KICH_HOAT);

        // 4. Lưu người dùng và phân quyền vào cơ sở dữ liệu qua Transaction
        try {
            int newId = nguoiDungDAO.themNguoiDung(nguoiDung, dsVaiTroIds);
            nguoiDung.setId(newId);

            // 5. Gửi email kích hoạt tài khoản kèm mật khẩu tạm
            String loginUrl = (baseUrl != null ? baseUrl : "") + "/dang-nhap";
            boolean daGuiEmail = emailService.guiEmailKichHoatTaiKhoan(
                    nguoiDung.getEmail(),
                    nguoiDung.getHoTen(),
                    matKhauTam,
                    loginUrl
            );

            String thongBao = "Tạo tài khoản thành công cho " + nguoiDung.getHoTen() + ". ";
            if (daGuiEmail) {
                thongBao += "Email kích hoạt kèm mật khẩu tạm đã được gửi tới " + nguoiDung.getEmail() + ".";
            } else {
                thongBao += "Tuy nhiên gửi email kích hoạt thất bại. Vui lòng kiểm tra lại cấu hình SMTP.";
            }

            return KetQuaNguoiDungDTO.thanhCong(nguoiDung, thongBao, matKhauTam);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lưu tài khoản người dùng vào DB: " + e.getMessage(), e);
            return KetQuaNguoiDungDTO.thatBai("Lỗi hệ thống khi lưu tài khoản: " + e.getMessage());
        }
    }

    /**
     * Cập nhật thông tin tài khoản người dùng và vai trò.
     * Kiểm tra không cho phép sửa email trùng với tài khoản khác.
     */
    public KetQuaNguoiDungDTO capNhatTaiKhoan(NguoiDung nguoiDung, List<Integer> dsVaiTroIds) {
        return capNhatTaiKhoan(nguoiDung, dsVaiTroIds, null);
    }

    /**
     * Cập nhật thông tin tài khoản người dùng và vai trò có xác thực người thực hiện.
     * Ràng buộc nghiệp vụ S1-09:
     * 1. Validate toàn bộ trước khi ghi DB: nếu vai trò có TEAM_LEAD thì nhóm bắt buộc khác null.
     * 2. Admin không được tự thu hồi vai trò ADMIN của chính mình.
     * 3. Nếu validation fail: không update nhóm, không sửa role, dữ liệu giữ nguyên 100%.
     */
    public KetQuaNguoiDungDTO capNhatTaiKhoan(NguoiDung nguoiDung, List<Integer> dsVaiTroIds, Integer nguoiThucHienId) {
        KetQuaNguoiDungDTO ketQua = new KetQuaNguoiDungDTO();

        if (nguoiDung == null || nguoiDung.getId() <= 0) {
            return KetQuaNguoiDungDTO.thatBai("Tài khoản không tồn tại hoặc ID không hợp lệ.");
        }

        if (nguoiDung.getHoTen() == null || nguoiDung.getHoTen().trim().isEmpty()) {
            ketQua.themLoi("hoTen", "Họ và tên không được để trống.");
        } else if (nguoiDung.getHoTen().trim().length() > 150) {
            ketQua.themLoi("hoTen", "Họ và tên không được vượt quá 150 ký tự.");
        }

        String email = nguoiDung.getEmail() != null ? nguoiDung.getEmail().trim() : "";
        if (email.isEmpty()) {
            ketQua.themLoi("email", "Email công ty không được để trống.");
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            ketQua.themLoi("email", "Địa chỉ email không đúng định dạng hợp lệ.");
        }

        if (dsVaiTroIds == null || dsVaiTroIds.isEmpty()) {
            ketQua.themLoi("vaiTro", "Vui lòng chọn ít nhất một vai trò cho người dùng.");
        }

        // Kiểm tra ràng buộc TEAM_LEAD và ADMIN (Story S1-09)
        boolean coVaiTroTruongNhom = false;
        boolean coVaiTroAdminMoi = false;
        if (dsVaiTroIds != null) {
            for (Integer vtId : dsVaiTroIds) {
                if (vtId == null) continue;
                VaiTro vt = vaiTroDAO.timTheoId(vtId);
                if (vt != null) {
                    if (VaiTroEnum.TEAM_LEAD.getMaVaiTro().equalsIgnoreCase(vt.getMaVaiTro())) {
                        coVaiTroTruongNhom = true;
                    }
                    if (VaiTroEnum.ADMIN.getMaVaiTro().equalsIgnoreCase(vt.getMaVaiTro())) {
                        coVaiTroAdminMoi = true;
                    }
                }
            }
        }

        // AC2 S1-09: TEAM_LEAD bắt buộc phải có nhóm kinh doanh cụ thể
        if (coVaiTroTruongNhom) {
            Integer nhomId = nguoiDung.getNhomKinhDoanhId();
            if (nhomId == null || nhomId <= 0) {
                ketQua.themLoi("nhomId", "Người giữ vai trò Trưởng nhóm kinh doanh bắt buộc phải được gán vào một nhóm kinh doanh cụ thể.");
            } else {
                NhomKinhDoanh nhom = nhomKinhDoanhDAO.timTheoId(nhomId);
                if (nhom == null) {
                    ketQua.themLoi("nhomId", "Nhóm kinh doanh được chọn không tồn tại trong hệ thống.");
                }
            }
        }

        // AC3 S1-09: Admin không được tự thu hồi ADMIN của chính mình
        if (nguoiThucHienId != null && nguoiThucHienId.intValue() == (int) nguoiDung.getId()) {
            NguoiDung hienTai = nguoiDungDAO.timTheoId(nguoiDung.getId());
            if (hienTai != null && hienTai.coVaiTro(VaiTroEnum.ADMIN) && !coVaiTroAdminMoi) {
                ketQua.themLoi("vaiTro", "Không thể tự thu hồi vai trò quản trị (Admin) của chính mình.");
            }
        }

        // Nếu có bất kỳ lỗi validation nào -> dừng ngay, TUYỆT ĐỐI KHÔNG GHI DATABASE
        if (!ketQua.getDanhSachLoi().isEmpty()) {
            ketQua.setThanhCong(false);
            ketQua.setThongBao("Dữ liệu không hợp lệ. Vui lòng kiểm tra lại thông tin.");
            return ketQua;
        }

        // Kiểm tra email trùng với người dùng khác
        boolean emailDaTonTai = nguoiDungDAO.kiemTraEmailTonTai(email, (int) nguoiDung.getId());
        if (emailDaTonTai) {
            return KetQuaNguoiDungDTO.loiEmailTrung(email);
        }

        nguoiDung.setEmail(email);
        nguoiDung.setHoTen(nguoiDung.getHoTen().trim());

        try {
            boolean capNhatThanhCong = nguoiDungDAO.capNhatNguoiDung(nguoiDung, dsVaiTroIds);
            if (capNhatThanhCong) {
                return KetQuaNguoiDungDTO.thanhCong(nguoiDung, "Cập nhật tài khoản " + nguoiDung.getEmail() + " thành công.", null);
            } else {
                return KetQuaNguoiDungDTO.thatBai("Không tìm thấy tài khoản để cập nhật.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi cập nhật tài khoản người dùng: " + e.getMessage(), e);
            return KetQuaNguoiDungDTO.thatBai("Lỗi hệ thống khi cập nhật: " + e.getMessage());
        }
    }

    public NguoiDung timTheoId(long id) {
        return nguoiDungDAO.timTheoId(id);
    }

    public NguoiDung timTheoId(int id) {
        return nguoiDungDAO.timTheoId((long) id);
    }

    public List<VaiTro> layDanhSachVaiTro() {
        return vaiTroDAO.layTatCa();
    }

    public List<NhomKinhDoanh> layDanhSachNhom() {
        return nhomKinhDoanhDAO.layTatCa();
    }
}
