package vn.nhom10.crm.dao;

import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.LichSuLienHeCongTy;
import vn.nhom10.crm.model.NguoiLienHe;
import vn.nhom10.crm.model.VaiTroQuyetDinhEnum;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object quản lý người liên hệ (nguoi_lien_he)
 * và lịch sử công ty (lich_su_lien_he_cong_ty) theo Story S3-02.
 */
public class NguoiLienHeDAO {

    /**
     * Lấy danh sách người liên hệ của một khách hàng.
     * Sắp xếp: Đầu mối chính lên đầu (la_dau_moi_chinh DESC), sau đó theo họ tên ASC.
     */
    public List<NguoiLienHe> layDanhSachTheoKhachHang(long khachHangId) {
        String sql = "SELECT nlh.id, nlh.khach_hang_id, nlh.ho_ten, nlh.chuc_danh, nlh.email, nlh.so_dien_thoai, " +
                "nlh.vai_tro_quyet_dinh, nlh.la_dau_moi_chinh, nlh.trang_thai, nlh.created_at, nlh.updated_at, " +
                "kh.ten_cong_ty, kh.ma_khach_hang, kh.ma_so_thue " +
                "FROM nguoi_lien_he nlh " +
                "JOIN khach_hang kh ON nlh.khach_hang_id = kh.id " +
                "WHERE nlh.khach_hang_id = ? AND nlh.trang_thai != 'DA_XOA' " +
                "ORDER BY nlh.la_dau_moi_chinh DESC, nlh.ho_ten ASC";

        List<NguoiLienHe> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, khachHangId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToNguoiLienHe(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy danh sách người liên hệ của khách hàng id=" + khachHangId, e);
        }
        return list;
    }

    /**
     * Tìm người liên hệ theo ID.
     */
    public Optional<NguoiLienHe> timTheoId(long id) {
        String sql = "SELECT nlh.id, nlh.khach_hang_id, nlh.ho_ten, nlh.chuc_danh, nlh.email, nlh.so_dien_thoai, " +
                "nlh.vai_tro_quyet_dinh, nlh.la_dau_moi_chinh, nlh.trang_thai, nlh.created_at, nlh.updated_at, " +
                "kh.ten_cong_ty, kh.ma_khach_hang, kh.ma_so_thue " +
                "FROM nguoi_lien_he nlh " +
                "JOIN khach_hang kh ON nlh.khach_hang_id = kh.id " +
                "WHERE nlh.id = ? AND nlh.trang_thai != 'DA_XOA'";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToNguoiLienHe(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tìm người liên hệ id=" + id, e);
        }
        return Optional.empty();
    }

    /**
     * Thêm mới người liên hệ (có quản lý Transaction).
     * Nếu laDauMoiChinh == true: Tự động gỡ cờ đầu mối chính cũ của khách hàng.
     * Tự động khởi tạo 1 bản ghi trong bảng lich_su_lien_he_cong_ty.
     */
    public long themNguoiLienHe(NguoiLienHe nlh) {
        String sqlResetMain = "UPDATE nguoi_lien_he SET la_dau_moi_chinh = 0 WHERE khach_hang_id = ?";
        String sqlInsert = "INSERT INTO nguoi_lien_he (khach_hang_id, ho_ten, chuc_danh, email, so_dien_thoai, " +
                "vai_tro_quyet_dinh, la_dau_moi_chinh, trang_thai, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        String sqlInsertHistory = "INSERT INTO lich_su_lien_he_cong_ty (nguoi_lien_he_id, khach_hang_id, " +
                "chuc_danh, vai_tro_quyet_dinh, tu_ngay, den_ngay, ghi_chu, created_at) " +
                "VALUES (?, ?, ?, ?, CURRENT_DATE, NULL, ?, NOW())";

        Connection conn = null;
        boolean originalAutoCommit = true;
        try {
            conn = DatabaseConfig.getConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // 1. Nếu là đầu mối chính, reset các đầu mối cũ của khách hàng này
            if (nlh.isLaDauMoiChinh()) {
                try (PreparedStatement psReset = conn.prepareStatement(sqlResetMain)) {
                    psReset.setLong(1, nlh.getKhachHangId());
                    psReset.executeUpdate();
                }
            }

            // 2. Insert người liên hệ mới
            long newId;
            try (PreparedStatement ps = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, nlh.getKhachHangId());
                ps.setString(2, nlh.getHoTen().trim());
                ps.setString(3, nlh.getChucDanh() != null ? nlh.getChucDanh().trim() : null);
                ps.setString(4, nlh.getEmail() != null ? nlh.getEmail().trim() : null);
                ps.setString(5, nlh.getSoDienThoai() != null ? nlh.getSoDienThoai().trim() : null);
                if (nlh.getVaiTroQuyetDinh() != null) {
                    ps.setString(6, nlh.getVaiTroQuyetDinh().getMa());
                } else {
                    ps.setNull(6, Types.VARCHAR);
                }
                ps.setBoolean(7, nlh.isLaDauMoiChinh());
                ps.setString(8, nlh.getTrangThai() != null ? nlh.getTrangThai() : "DANG_HOAT_DONG");

                ps.executeUpdate();
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        newId = gk.getLong(1);
                        nlh.setId(newId);
                    } else {
                        throw new SQLException("Không lấy được id phát sinh của người liên hệ mới.");
                    }
                }
            }

            // 3. Khởi tạo bản ghi lịch sử công ty ban đầu (AC4)
            try (PreparedStatement psHist = conn.prepareStatement(sqlInsertHistory)) {
                psHist.setLong(1, newId);
                psHist.setLong(2, nlh.getKhachHangId());
                psHist.setString(3, nlh.getChucDanh() != null ? nlh.getChucDanh().trim() : null);
                if (nlh.getVaiTroQuyetDinh() != null) {
                    psHist.setString(4, nlh.getVaiTroQuyetDinh().getMa());
                } else {
                    psHist.setNull(4, Types.VARCHAR);
                }
                psHist.setString(5, "Khởi tạo thông tin liên hệ ban đầu");
                psHist.executeUpdate();
            }

            conn.commit();
            return newId;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    e.addSuppressed(ex);
                }
            }
            throw new RuntimeException("Lỗi khi thêm người liên hệ: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Cập nhật thông tin cơ bản của người liên hệ (họ tên, chức danh, email, sđt, vai trò quyết định).
     */
    public boolean capNhatThongTin(NguoiLienHe nlh) {
        String sqlUpdate = "UPDATE nguoi_lien_he SET ho_ten = ?, chuc_danh = ?, email = ?, so_dien_thoai = ?, " +
                "vai_tro_quyet_dinh = ?, updated_at = NOW() WHERE id = ?";
        String sqlUpdateHistory = "UPDATE lich_su_lien_he_cong_ty SET chuc_danh = ?, vai_tro_quyet_dinh = ? " +
                "WHERE nguoi_lien_he_id = ? AND khach_hang_id = ? AND den_ngay IS NULL";

        Connection conn = null;
        boolean originalAutoCommit = true;
        try {
            conn = DatabaseConfig.getConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            int updated;
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setString(1, nlh.getHoTen().trim());
                ps.setString(2, nlh.getChucDanh() != null ? nlh.getChucDanh().trim() : null);
                ps.setString(3, nlh.getEmail() != null ? nlh.getEmail().trim() : null);
                ps.setString(4, nlh.getSoDienThoai() != null ? nlh.getSoDienThoai().trim() : null);
                if (nlh.getVaiTroQuyetDinh() != null) {
                    ps.setString(5, nlh.getVaiTroQuyetDinh().getMa());
                } else {
                    ps.setNull(5, Types.VARCHAR);
                }
                ps.setLong(6, nlh.getId());
                updated = ps.executeUpdate();
            }

            if (updated > 0) {
                // Đồng bộ cập nhật vào bản ghi lịch sử hiện tại nếu có
                try (PreparedStatement psHist = conn.prepareStatement(sqlUpdateHistory)) {
                    psHist.setString(1, nlh.getChucDanh() != null ? nlh.getChucDanh().trim() : null);
                    if (nlh.getVaiTroQuyetDinh() != null) {
                        psHist.setString(2, nlh.getVaiTroQuyetDinh().getMa());
                    } else {
                        psHist.setNull(2, Types.VARCHAR);
                    }
                    psHist.setLong(3, nlh.getId());
                    psHist.setLong(4, nlh.getKhachHangId());
                    psHist.executeUpdate();
                }
            }

            conn.commit();
            return updated > 0;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    e.addSuppressed(ex);
                }
            }
            throw new RuntimeException("Lỗi khi cập nhật người liên hệ id=" + nlh.getId(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Đánh dấu một người là đầu mối chính của khách hàng (AC3).
     * Bắt buộc dùng Transaction để đảm bảo tính toàn vẹn (mỗi khách hàng chỉ có 1 đầu mối chính).
     */
    public boolean datLamDauMoiChinh(long nlhId, long khachHangId) {
        String sqlResetAll = "UPDATE nguoi_lien_he SET la_dau_moi_chinh = 0 WHERE khach_hang_id = ?";
        String sqlSetMain = "UPDATE nguoi_lien_he SET la_dau_moi_chinh = 1, updated_at = NOW() WHERE id = ? AND khach_hang_id = ?";

        Connection conn = null;
        boolean originalAutoCommit = true;
        try {
            conn = DatabaseConfig.getConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // 1. Reset tất cả về 0
            try (PreparedStatement psReset = conn.prepareStatement(sqlResetAll)) {
                psReset.setLong(1, khachHangId);
                psReset.executeUpdate();
            }

            // 2. Set đầu mối chính cho người được chọn
            int rows;
            try (PreparedStatement psSet = conn.prepareStatement(sqlSetMain)) {
                psSet.setLong(1, nlhId);
                psSet.setLong(2, khachHangId);
                rows = psSet.executeUpdate();
            }

            if (rows > 0) {
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    e.addSuppressed(ex);
                }
            }
            throw new RuntimeException("Lỗi khi đánh dấu đầu mối chính: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Bỏ đánh dấu đầu mối chính của một người.
     */
    public boolean boDauMoiChinh(long nlhId, long khachHangId) {
        String sql = "UPDATE nguoi_lien_he SET la_dau_moi_chinh = 0, updated_at = NOW() WHERE id = ? AND khach_hang_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, nlhId);
            ps.setLong(2, khachHangId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi bỏ đánh dấu đầu mối chính: " + e.getMessage(), e);
        }
    }

    /**
     * Chuyển người liên hệ sang công ty khác (AC4).
     * Bắt buộc dùng Transaction:
     * 1. Cập nhật ngày kết thúc (den_ngay = CURRENT_DATE) cho công ty cũ trong lich_su_lien_he_cong_ty.
     *    Nếu chưa từng có bản ghi của công ty cũ thì tạo bản ghi kết thúc tại công ty cũ.
     * 2. Tạo bản ghi mới trong lich_su_lien_he_cong_ty cho công ty mới (tu_ngay = CURRENT_DATE, den_ngay = NULL).
     * 3. Cập nhật nguoi_lien_he sang khach_hang_id mới, chức danh mới, vai trò mới, la_dau_moi_chinh = 0.
     */
    public boolean chuyenCongTy(long nlhId, long khachHangMoiId, String chucDanhMoi,
                               VaiTroQuyetDinhEnum vaiTroMoi, String ghiChu) {
        String sqlGetContact = "SELECT khach_hang_id, chuc_danh, vai_tro_quyet_dinh, created_at " +
                "FROM nguoi_lien_he WHERE id = ?";
        String sqlCloseOldHistory = "UPDATE lich_su_lien_he_cong_ty SET den_ngay = CURRENT_DATE " +
                "WHERE nguoi_lien_he_id = ? AND khach_hang_id = ? AND den_ngay IS NULL";
        String sqlInsertOldHistoryIfMissing = "INSERT INTO lich_su_lien_he_cong_ty (nguoi_lien_he_id, khach_hang_id, " +
                "chuc_danh, vai_tro_quyet_dinh, tu_ngay, den_ngay, ghi_chu, created_at) " +
                "VALUES (?, ?, ?, ?, ?, CURRENT_DATE, ?, NOW())";
        String sqlInsertNewHistory = "INSERT INTO lich_su_lien_he_cong_ty (nguoi_lien_he_id, khach_hang_id, " +
                "chuc_danh, vai_tro_quyet_dinh, tu_ngay, den_ngay, ghi_chu, created_at) " +
                "VALUES (?, ?, ?, ?, CURRENT_DATE, NULL, ?, NOW())";
        String sqlUpdateContact = "UPDATE nguoi_lien_he SET khach_hang_id = ?, chuc_danh = ?, vai_tro_quyet_dinh = ?, " +
                "la_dau_moi_chinh = 0, updated_at = NOW() WHERE id = ?";

        Connection conn = null;
        boolean originalAutoCommit = true;
        try {
            conn = DatabaseConfig.getConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // 1. Lấy thông tin công ty hiện tại của contact
            long oldKhachHangId;
            String oldChucDanh;
            String oldVaiTro;
            Timestamp oldCreatedAt;
            try (PreparedStatement psGet = conn.prepareStatement(sqlGetContact)) {
                psGet.setLong(1, nlhId);
                try (ResultSet rs = psGet.executeQuery()) {
                    if (rs.next()) {
                        oldKhachHangId = rs.getLong("khach_hang_id");
                        oldChucDanh = rs.getString("chuc_danh");
                        oldVaiTro = rs.getString("vai_tro_quyet_dinh");
                        oldCreatedAt = rs.getTimestamp("created_at");
                    } else {
                        throw new IllegalArgumentException("Không tìm thấy người liên hệ id=" + nlhId);
                    }
                }
            }

            if (oldKhachHangId == khachHangMoiId) {
                throw new IllegalArgumentException("Khách hàng mới không được trùng với khách hàng hiện tại.");
            }

            // 2. Đóng lịch sử công ty cũ
            int closedRows;
            try (PreparedStatement psClose = conn.prepareStatement(sqlCloseOldHistory)) {
                psClose.setLong(1, nlhId);
                psClose.setLong(2, oldKhachHangId);
                closedRows = psClose.executeUpdate();
            }

            // Nếu chưa có bản ghi mở nào tại công ty cũ, tạo 1 bản ghi công ty cũ đã kết thúc
            if (closedRows == 0) {
                try (PreparedStatement psInsertOld = conn.prepareStatement(sqlInsertOldHistoryIfMissing)) {
                    psInsertOld.setLong(1, nlhId);
                    psInsertOld.setLong(2, oldKhachHangId);
                    psInsertOld.setString(3, oldChucDanh);
                    if (oldVaiTro != null) {
                        psInsertOld.setString(4, oldVaiTro);
                    } else {
                        psInsertOld.setNull(4, Types.VARCHAR);
                    }
                    Date startDate = oldCreatedAt != null ? new Date(oldCreatedAt.getTime()) : Date.valueOf(LocalDate.now());
                    psInsertOld.setDate(5, startDate);
                    psInsertOld.setString(6, "Giai đoạn làm việc tại công ty trước khi chuyển");
                    psInsertOld.executeUpdate();
                }
            }

            // 3. Thêm bản ghi lịch sử mới tại công ty đích
            try (PreparedStatement psInsertNew = conn.prepareStatement(sqlInsertNewHistory)) {
                psInsertNew.setLong(1, nlhId);
                psInsertNew.setLong(2, khachHangMoiId);
                psInsertNew.setString(3, chucDanhMoi != null ? chucDanhMoi.trim() : null);
                if (vaiTroMoi != null) {
                    psInsertNew.setString(4, vaiTroMoi.getMa());
                } else {
                    psInsertNew.setNull(4, Types.VARCHAR);
                }
                String note = (ghiChu != null && !ghiChu.trim().isEmpty())
                        ? ghiChu.trim()
                        : "Chuyển công tác từ khách hàng ID=" + oldKhachHangId;
                psInsertNew.setString(5, note);
                psInsertNew.executeUpdate();
            }

            // 4. Cập nhật bảng nguoi_lien_he
            int contactUpdated;
            try (PreparedStatement psUpContact = conn.prepareStatement(sqlUpdateContact)) {
                psUpContact.setLong(1, khachHangMoiId);
                psUpContact.setString(2, chucDanhMoi != null ? chucDanhMoi.trim() : null);
                if (vaiTroMoi != null) {
                    psUpContact.setString(3, vaiTroMoi.getMa());
                } else {
                    psUpContact.setNull(3, Types.VARCHAR);
                }
                psUpContact.setLong(4, nlhId);
                contactUpdated = psUpContact.executeUpdate();
            }

            if (contactUpdated > 0) {
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    e.addSuppressed(ex);
                }
            }
            throw new RuntimeException("Lỗi khi chuyển công ty cho người liên hệ id=" + nlhId + ": " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Lấy toàn bộ lịch sử công ty của một người liên hệ (AC4).
     * Sắp xếp từ ngày mới nhất giảm dần.
     */
    public List<LichSuLienHeCongTy> layLichSuCongTy(long nlhId) {
        String sql = "SELECT ls.id, ls.nguoi_lien_he_id, ls.khach_hang_id, ls.chuc_danh, ls.vai_tro_quyet_dinh, " +
                "ls.tu_ngay, ls.den_ngay, ls.ghi_chu, ls.created_at, " +
                "kh.ten_cong_ty, kh.ma_khach_hang, kh.ma_so_thue " +
                "FROM lich_su_lien_he_cong_ty ls " +
                "JOIN khach_hang kh ON ls.khach_hang_id = kh.id " +
                "WHERE ls.nguoi_lien_he_id = ? " +
                "ORDER BY ls.tu_ngay DESC, ls.id DESC";

        List<LichSuLienHeCongTy> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, nlhId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LichSuLienHeCongTy ls = new LichSuLienHeCongTy();
                    ls.setId(rs.getLong("id"));
                    ls.setNguoiLienHeId(rs.getLong("nguoi_lien_he_id"));
                    ls.setKhachHangId(rs.getLong("khach_hang_id"));
                    ls.setChucDanh(rs.getString("chuc_danh"));
                    String vaiTro = rs.getString("vai_tro_quyet_dinh");
                    if (vaiTro != null) {
                        try {
                            ls.setVaiTroQuyetDinh(VaiTroQuyetDinhEnum.fromMa(vaiTro));
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                    Date tuNgay = rs.getDate("tu_ngay");
                    if (tuNgay != null) ls.setTuNgay(tuNgay.toLocalDate());
                    Date denNgay = rs.getDate("den_ngay");
                    if (denNgay != null) ls.setDenNgay(denNgay.toLocalDate());
                    ls.setGhiChu(rs.getString("ghi_chu"));
                    Timestamp cat = rs.getTimestamp("created_at");
                    if (cat != null) ls.setCreatedAt(cat.toLocalDateTime());

                    ls.setTenCongTy(rs.getString("ten_cong_ty"));
                    ls.setMaKhachHang(rs.getString("ma_khach_hang"));
                    ls.setMaSoThue(rs.getString("ma_so_thue"));

                    list.add(ls);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy lịch sử công ty của người liên hệ id=" + nlhId, e);
        }
        return list;
    }

    /**
     * Xóa mềm người liên hệ (đổi trạng thái sang DA_XOA).
     */
    public boolean xoaNguoiLienHe(long id) {
        String sql = "UPDATE nguoi_lien_he SET trang_thai = 'DA_XOA', updated_at = NOW() WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi xóa người liên hệ id=" + id, e);
        }
    }

    /**
     * Đếm số lượng người liên hệ đang hoạt động của khách hàng.
     */
    public int demSoNguoiLienHe(long khachHangId) {
        String sql = "SELECT COUNT(*) FROM nguoi_lien_he WHERE khach_hang_id = ? AND trang_thai != 'DA_XOA'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, khachHangId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi đếm số người liên hệ của khách hàng id=" + khachHangId, e);
        }
        return 0;
    }

    private NguoiLienHe mapResultSetToNguoiLienHe(ResultSet rs) throws SQLException {
        NguoiLienHe nlh = new NguoiLienHe();
        nlh.setId(rs.getLong("id"));
        nlh.setKhachHangId(rs.getLong("khach_hang_id"));
        nlh.setHoTen(rs.getString("ho_ten"));
        nlh.setChucDanh(rs.getString("chuc_danh"));
        nlh.setEmail(rs.getString("email"));
        nlh.setSoDienThoai(rs.getString("so_dien_thoai"));

        String vaiTroStr = rs.getString("vai_tro_quyet_dinh");
        if (vaiTroStr != null) {
            try {
                nlh.setVaiTroQuyetDinh(VaiTroQuyetDinhEnum.fromMa(vaiTroStr));
            } catch (IllegalArgumentException ignored) {
            }
        }

        nlh.setLaDauMoiChinh(rs.getBoolean("la_dau_moi_chinh"));
        nlh.setTrangThai(rs.getString("trang_thai"));

        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) nlh.setCreatedAt(cat.toLocalDateTime());
        Timestamp uat = rs.getTimestamp("updated_at");
        if (uat != null) nlh.setUpdatedAt(uat.toLocalDateTime());

        nlh.setTenKhachHang(rs.getString("ten_cong_ty"));
        nlh.setMaKhachHang(rs.getString("ma_khach_hang"));
        nlh.setMaSoThueKhachHang(rs.getString("ma_so_thue"));

        return nlh;
    }
}
