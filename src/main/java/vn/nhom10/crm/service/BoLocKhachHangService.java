package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.BoLocDaLuuDAO;
import vn.nhom10.crm.dto.BoLocKhachHangDTO;
import vn.nhom10.crm.model.BoLocDaLuu;
import vn.nhom10.crm.model.NguoiDung;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service xử lý nghiệp vụ Lưu lại bộ lọc hay dùng (Story S3-07, AC3).
 * Đáp ứng:
 * - Lưu bộ lọc hiện tại với tên do người dùng đặt
 * - Tự động định dạng JSON lưu trữ
 * - Quản lý bộ lọc mặc định khi vào trang
 * - Xóa bộ lọc của người dùng
 */
public class BoLocKhachHangService {

    private static final Logger LOGGER = Logger.getLogger(BoLocKhachHangService.class.getName());

    private final BoLocDaLuuDAO boLocDAO;

    public BoLocKhachHangService() {
        this(new BoLocDaLuuDAO());
    }

    public BoLocKhachHangService(BoLocDaLuuDAO boLocDAO) {
        this.boLocDAO = boLocDAO != null ? boLocDAO : new BoLocDaLuuDAO();
    }

    /**
     * Lưu bộ lọc của người dùng:
     * - Kiểm tra tính hợp lệ của người dùng và tên bộ lọc
     * - Chuyển đổi tiêu chí DTO sang JSON chuẩn
     * - Nếu người dùng chọn đặt mặc định, kích hoạt cờ mặc định
     */
    public BoLocDaLuu luuBoLoc(NguoiDung user, String tenBoLoc, BoLocKhachHangDTO tieuChi, boolean macDinh) throws SQLException {
        if (user == null || user.getId() <= 0) {
            throw new IllegalArgumentException("Yêu cầu đăng nhập để lưu bộ lọc.");
        }
        if (tenBoLoc == null || tenBoLoc.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên bộ lọc không được để trống.");
        }
        if (tenBoLoc.trim().length() > 150) {
            throw new IllegalArgumentException("Tên bộ lọc tối đa 150 ký tự.");
        }
        if (tieuChi == null) {
            tieuChi = new BoLocKhachHangDTO();
        }

        String json = tieuChi.toJson();
        BoLocDaLuu model = new BoLocDaLuu();
        model.setNguoiDungId(user.getId());
        model.setLoaiDoiTuong(BoLocDaLuu.LOAI_KHACH_HANG);
        model.setTenBoLoc(tenBoLoc.trim());
        model.setTieuChiJson(json);
        model.setMacDinh(macDinh);

        long id = boLocDAO.luuBoLoc(model);
        model.setId(id);

        if (macDinh && id > 0) {
            boLocDAO.datMacDinh(id, user.getId(), BoLocDaLuu.LOAI_KHACH_HANG);
        }

        return model;
    }

    /**
     * Lấy danh sách toàn bộ bộ lọc đã lưu của người dùng đối với đối tượng khách hàng.
     */
    public List<BoLocDaLuu> layDanhSachBoLoc(NguoiDung user) {
        if (user == null || user.getId() <= 0) {
            return Collections.emptyList();
        }
        return boLocDAO.layDanhSachTheoNguoiDung(user.getId(), BoLocDaLuu.LOAI_KHACH_HANG);
    }

    /**
     * Lấy chi tiết bộ lọc theo ID (chỉ cho phép nếu thuộc về người dùng hiện tại).
     */
    public BoLocDaLuu timBoLocTheoId(long id, NguoiDung user) {
        if (user == null || user.getId() <= 0 || id <= 0) {
            return null;
        }
        return boLocDAO.timTheoId(id, user.getId());
    }

    /**
     * Tìm bộ lọc mặc định của người dùng (nếu có).
     */
    public BoLocDaLuu timBoLocMacDinh(NguoiDung user) {
        if (user == null || user.getId() <= 0) {
            return null;
        }
        return boLocDAO.timBoLocMacDinh(user.getId(), BoLocDaLuu.LOAI_KHACH_HANG);
    }

    /**
     * Xóa một bộ lọc đã lưu của người dùng.
     */
    public boolean xoaBoLoc(long id, NguoiDung user) throws SQLException {
        if (user == null || user.getId() <= 0 || id <= 0) {
            return false;
        }
        return boLocDAO.xoaBoLoc(id, user.getId());
    }

    /**
     * Đặt một bộ lọc làm mặc định (hoặc hủy mặc định nếu id <= 0).
     */
    public boolean datMacDinh(long id, NguoiDung user) throws SQLException {
        if (user == null || user.getId() <= 0) {
            return false;
        }
        return boLocDAO.datMacDinh(id, user.getId(), BoLocDaLuu.LOAI_KHACH_HANG);
    }
}
