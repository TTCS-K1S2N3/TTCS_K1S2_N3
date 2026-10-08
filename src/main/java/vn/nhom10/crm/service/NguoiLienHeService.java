package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.KhachHangDAO;
import vn.nhom10.crm.dao.NguoiLienHeDAO;
import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.model.LichSuLienHeCongTy;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NguoiLienHe;
import vn.nhom10.crm.model.VaiTroQuyetDinhEnum;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service xử lý nghiệp vụ Quản lý người liên hệ và vai trò trong quyết định mua (Story S3-02).
 * Đảm bảo 4 Acceptance Criteria:
 * - AC1: Mỗi khách hàng có nhiều người liên hệ, mỗi người có chức danh, email, số điện thoại
 * - AC2: Đánh dấu vai trò trong quyết định mua: người quyết định, người ảnh hưởng, người dùng cuối, người cản trở
 * - AC3: Đánh dấu một người là đầu mối chính
 * - AC4: Một người liên hệ chuyển sang công ty khác thì gắn lại được sang khách hàng mới, giữ nguyên lịch sử
 */
public class NguoiLienHeService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{9,15}$");

    private final NguoiLienHeDAO nguoiLienHeDAO;
    private final KhachHangDAO khachHangDAO;
    private final KhachHangService khachHangService;

    public NguoiLienHeService() {
        this(new NguoiLienHeDAO(), new KhachHangDAO(), new KhachHangService());
    }

    public NguoiLienHeService(NguoiLienHeDAO nguoiLienHeDAO, KhachHangDAO khachHangDAO, KhachHangService khachHangService) {
        this.nguoiLienHeDAO = nguoiLienHeDAO != null ? nguoiLienHeDAO : new NguoiLienHeDAO();
        this.khachHangDAO = khachHangDAO != null ? khachHangDAO : new KhachHangDAO();
        this.khachHangService = khachHangService != null ? khachHangService : new KhachHangService();
    }

    /**
     * Lấy danh sách người liên hệ của một khách hàng (AC1).
     * Kiểm tra phân quyền Data Scope xem khách hàng phía server-side.
     */
    public List<NguoiLienHe> layDanhSachTheoKhachHang(NguoiDung currentUser, long khachHangId) {
        KhachHang kh = layKhachHangHoacBaoLoi(khachHangId);
        kiemTraQuyenXemKhachHang(currentUser, kh);
        return nguoiLienHeDAO.layDanhSachTheoKhachHang(khachHangId);
    }

    /**
     * Lấy chi tiết một người liên hệ theo ID.
     */
    public Optional<NguoiLienHe> timTheoId(NguoiDung currentUser, long nlhId) {
        Optional<NguoiLienHe> optNlh = nguoiLienHeDAO.timTheoId(nlhId);
        if (optNlh.isEmpty()) {
            return Optional.empty();
        }
        NguoiLienHe nlh = optNlh.get();
        KhachHang kh = layKhachHangHoacBaoLoi(nlh.getKhachHangId());
        kiemTraQuyenXemKhachHang(currentUser, kh);
        return optNlh;
    }

    /**
     * Thêm mới một người liên hệ cho khách hàng (AC1, AC2, AC3).
     * @param currentUser Người dùng đang thao tác (phải có quyền sửa trên khách hàng)
     * @param nlh Dữ liệu người liên hệ
     * @return NguoiLienHe đã lưu kèm ID
     */
    public NguoiLienHe themNguoiLienHe(NguoiDung currentUser, NguoiLienHe nlh) {
        if (currentUser == null) {
            throw new SecurityException("Vui lòng đăng nhập trước khi thực hiện thao tác.");
        }
        if (nlh == null) {
            throw new IllegalArgumentException("Thông tin người liên hệ không được để trống.");
        }
        if (nlh.getKhachHangId() == null || nlh.getKhachHangId() <= 0) {
            throw new IllegalArgumentException("Khách hàng liên kết không hợp lệ.");
        }

        // Kiểm tra khách hàng tồn tại và phân quyền sửa
        KhachHang kh = layKhachHangHoacBaoLoi(nlh.getKhachHangId());
        kiemTraQuyenSuaKhachHang(currentUser, kh);

        // Validate dữ liệu đầu vào
        validateThongTinNguoiLienHe(nlh);

        long newId = nguoiLienHeDAO.themNguoiLienHe(nlh);
        nlh.setId(newId);
        return nlh;
    }

    /**
     * Cập nhật thông tin người liên hệ (AC1, AC2).
     */
    public boolean capNhatNguoiLienHe(NguoiDung currentUser, NguoiLienHe nlh) {
        if (currentUser == null) {
            throw new SecurityException("Vui lòng đăng nhập trước khi thực hiện thao tác.");
        }
        if (nlh == null || nlh.getId() == null || nlh.getId() <= 0) {
            throw new IllegalArgumentException("ID người liên hệ không hợp lệ.");
        }

        NguoiLienHe existing = nguoiLienHeDAO.timTheoId(nlh.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người liên hệ id=" + nlh.getId()));

        KhachHang kh = layKhachHangHoacBaoLoi(existing.getKhachHangId());
        kiemTraQuyenSuaKhachHang(currentUser, kh);

        // Bảo toàn khachHangId từ bản ghi gốc (chuyển công ty phải gọi API riêng theo AC4)
        nlh.setKhachHangId(existing.getKhachHangId());
        validateThongTinNguoiLienHe(nlh);

        return nguoiLienHeDAO.capNhatThongTin(nlh);
    }

    /**
     * Đánh dấu một người là đầu mối chính của khách hàng (AC3).
     * Tự động gỡ bỏ đầu mối chính cũ trong cùng một transaction.
     */
    public boolean datLamDauMoiChinh(NguoiDung currentUser, long nlhId, long khachHangId) {
        if (currentUser == null) {
            throw new SecurityException("Vui lòng đăng nhập để thực hiện.");
        }
        NguoiLienHe nlh = nguoiLienHeDAO.timTheoId(nlhId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người liên hệ id=" + nlhId));

        if (!nlh.getKhachHangId().equals(khachHangId)) {
            throw new IllegalArgumentException("Người liên hệ không thuộc khách hàng được chỉ định.");
        }

        KhachHang kh = layKhachHangHoacBaoLoi(khachHangId);
        kiemTraQuyenSuaKhachHang(currentUser, kh);

        return nguoiLienHeDAO.datLamDauMoiChinh(nlhId, khachHangId);
    }

    /**
     * Bỏ đánh dấu đầu mối chính của một người liên hệ.
     */
    public boolean boDauMoiChinh(NguoiDung currentUser, long nlhId, long khachHangId) {
        if (currentUser == null) {
            throw new SecurityException("Vui lòng đăng nhập để thực hiện.");
        }
        NguoiLienHe nlh = nguoiLienHeDAO.timTheoId(nlhId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người liên hệ id=" + nlhId));

        if (!nlh.getKhachHangId().equals(khachHangId)) {
            throw new IllegalArgumentException("Người liên hệ không thuộc khách hàng được chỉ định.");
        }

        KhachHang kh = layKhachHangHoacBaoLoi(khachHangId);
        kiemTraQuyenSuaKhachHang(currentUser, kh);

        return nguoiLienHeDAO.boDauMoiChinh(nlhId, khachHangId);
    }

    /**
     * Chuyển người liên hệ sang công ty khác, giữ nguyên lịch sử làm việc (AC4).
     * Bắt buộc kiểm tra quyền trên cả khách hàng cũ và khách hàng mới.
     * @param currentUser Người thực hiện
     * @param nlhId ID người liên hệ
     * @param khachHangMoiId ID khách hàng công ty mới
     * @param chucDanhMoi Chức danh tại công ty mới
     * @param vaiTroMoi Vai trò quyết định mua tại công ty mới
     * @param ghiChu Ghi chú lý do chuyển
     */
    public boolean chuyenCongTy(NguoiDung currentUser, long nlhId, long khachHangMoiId,
                               String chucDanhMoi, VaiTroQuyetDinhEnum vaiTroMoi, String ghiChu) {
        if (currentUser == null) {
            throw new SecurityException("Vui lòng đăng nhập để thực hiện thao tác.");
        }

        // 1. Kiểm tra người liên hệ
        NguoiLienHe nlh = nguoiLienHeDAO.timTheoId(nlhId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người liên hệ id=" + nlhId));

        long khachHangCuId = nlh.getKhachHangId();
        if (khachHangCuId == khachHangMoiId) {
            throw new IllegalArgumentException("Khách hàng mới không được trùng với công ty hiện tại.");
        }

        // 2. Kiểm tra quyền sửa trên khách hàng CŨ
        KhachHang khCu = layKhachHangHoacBaoLoi(khachHangCuId);
        kiemTraQuyenSuaKhachHang(currentUser, khCu);

        // 3. Kiểm tra khách hàng MỚI tồn tại và quyền sửa trên khách hàng MỚI
        KhachHang khMoi = layKhachHangHoacBaoLoi(khachHangMoiId);
        kiemTraQuyenSuaKhachHang(currentUser, khMoi);

        // 4. Validate chức danh mới
        if (chucDanhMoi != null && chucDanhMoi.trim().length() > 150) {
            throw new IllegalArgumentException("Chức danh mới không được vượt quá 150 ký tự.");
        }

        return nguoiLienHeDAO.chuyenCongTy(nlhId, khachHangMoiId, chucDanhMoi, vaiTroMoi, ghiChu);
    }

    /**
     * Lấy toàn bộ lịch sử công tác/làm việc qua các công ty của người liên hệ (AC4).
     */
    public List<LichSuLienHeCongTy> layLichSuCongTy(NguoiDung currentUser, long nlhId) {
        NguoiLienHe nlh = nguoiLienHeDAO.timTheoId(nlhId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người liên hệ id=" + nlhId));

        KhachHang kh = layKhachHangHoacBaoLoi(nlh.getKhachHangId());
        kiemTraQuyenXemKhachHang(currentUser, kh);

        return nguoiLienHeDAO.layLichSuCongTy(nlhId);
    }

    /**
     * Xóa người liên hệ.
     */
    public boolean xoaNguoiLienHe(NguoiDung currentUser, long nlhId) {
        if (currentUser == null) {
            throw new SecurityException("Vui lòng đăng nhập.");
        }
        NguoiLienHe nlh = nguoiLienHeDAO.timTheoId(nlhId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người liên hệ id=" + nlhId));

        KhachHang kh = layKhachHangHoacBaoLoi(nlh.getKhachHangId());
        kiemTraQuyenSuaKhachHang(currentUser, kh);

        return nguoiLienHeDAO.xoaNguoiLienHe(nlhId);
    }

    /**
     * Validation dữ liệu người liên hệ.
     */
    private void validateThongTinNguoiLienHe(NguoiLienHe nlh) {
        // Họ tên bắt buộc
        if (nlh.getHoTen() == null || nlh.getHoTen().trim().isEmpty()) {
            throw new IllegalArgumentException("Họ và tên người liên hệ không được để trống.");
        }
        if (nlh.getHoTen().trim().length() > 150) {
            throw new IllegalArgumentException("Họ và tên người liên hệ không được vượt quá 150 ký tự.");
        }

        // Chức danh
        if (nlh.getChucDanh() != null && nlh.getChucDanh().trim().length() > 150) {
            throw new IllegalArgumentException("Chức danh không được vượt quá 150 ký tự.");
        }

        // Email (nếu có phải đúng định dạng)
        if (nlh.getEmail() != null && !nlh.getEmail().trim().isEmpty()) {
            String email = nlh.getEmail().trim();
            if (email.length() > 255) {
                throw new IllegalArgumentException("Email không được vượt quá 255 ký tự.");
            }
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                throw new IllegalArgumentException("Định dạng email không hợp lệ: " + email);
            }
        }

        // Số điện thoại (nếu có phải hợp lệ)
        if (nlh.getSoDienThoai() != null && !nlh.getSoDienThoai().trim().isEmpty()) {
            String sdt = nlh.getSoDienThoai().trim().replaceAll("[\\s.-]", "");
            if (sdt.length() < 9 || sdt.length() > 20 || !PHONE_PATTERN.matcher(sdt).matches()) {
                throw new IllegalArgumentException("Số điện thoại không hợp lệ: " + nlh.getSoDienThoai() +
                        ". Số điện thoại phải từ 9 đến 15 chữ số.");
            }
        }
    }

    private KhachHang layKhachHangHoacBaoLoi(long khachHangId) {
        try {
            KhachHang kh = khachHangDAO.timTheoId(khachHangId);
            if (kh == null) {
                throw new IllegalArgumentException("Không tìm thấy khách hàng id=" + khachHangId);
            }
            return kh;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi truy vấn dữ liệu khách hàng id=" + khachHangId, e);
        }
    }

    private void kiemTraQuyenXemKhachHang(NguoiDung user, KhachHang kh) {
        KhachHangService.KetQuaQuyenKhachHang kq = khachHangService.kiemTraQuyenXem(user, kh);
        if (!kq.isCoQuyen()) {
            throw new SecurityException(kq.getThongBao());
        }
    }

    private void kiemTraQuyenSuaKhachHang(NguoiDung user, KhachHang kh) {
        KhachHangService.KetQuaQuyenKhachHang kq = khachHangService.kiemTraQuyenSua(user, kh);
        if (!kq.isCoQuyen()) {
            throw new SecurityException(kq.getThongBao());
        }
    }
}
