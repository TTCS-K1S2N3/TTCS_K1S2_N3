package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.YeuCauHoTro;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object quản lý bảng `yeu_cau_ho_tro` (Story S3-08).
 * Quản lý ghi nhận yêu cầu hỗ trợ sau bán, mức độ ưu tiên, người xử lý và trạng thái.
 */
public class YeuCauHoTroDAO {

    private static final Logger LOGGER = Logger.getLogger(YeuCauHoTroDAO.class.getName());

    /**
     * Thêm mới yêu cầu hỗ trợ.
     *
     * @param ycht Đối tượng yêu cầu hỗ trợ
     * @return ID tự sinh, hoặc null nếu thất bại
     */
    public Long themYeuCau(YeuCauHoTro ycht) {
        if (ycht == null || ycht.getKhachHangId() == null || ycht.getTieuDe() == null) {
            return null;
        }

        if (ycht.getMaYeuCau() == null || ycht.getMaYeuCau().isBlank()) {
            ycht.setMaYeuCau(sinhMaYeuCau());
        }

        String sql = "INSERT INTO yeu_cau_ho_tro (ma_yeu_cau, khach_hang_id, nguoi_lien_he_id, nguoi_xu_ly_id, " +
                "tieu_de, noi_dung, muc_uu_tien, trang_thai, tao_luc, xu_ly_luc, hoan_tat_luc) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ycht.getMaYeuCau());
            ps.setLong(2, ycht.getKhachHangId());

            if (ycht.getNguoiLienHeId() != null && ycht.getNguoiLienHeId() > 0) {
                ps.setLong(3, ycht.getNguoiLienHeId());
            } else {
                ps.setNull(3, Types.BIGINT);
            }

            if (ycht.getNguoiXuLyId() != null && ycht.getNguoiXuLyId() > 0) {
                ps.setLong(4, ycht.getNguoiXuLyId());
            } else {
                ps.setNull(4, Types.BIGINT);
            }

            ps.setString(5, ycht.getTieuDe());
            ps.setString(6, ycht.getNoiDung());
            ps.setString(7, ycht.getMucUuTien() != null ? ycht.getMucUuTien() : "BINH_THUONG");
            ps.setString(8, ycht.getTrangThai() != null ? ycht.getTrangThai() : "MOI");

            LocalDateTime taoLuc = ycht.getTaoLuc() != null ? ycht.getTaoLuc() : LocalDateTime.now();
            ps.setTimestamp(9, Timestamp.valueOf(taoLuc));

            if (ycht.getXuLyLuc() != null) {
                ps.setTimestamp(10, Timestamp.valueOf(ycht.getXuLyLuc()));
            } else {
                ps.setNull(10, Types.TIMESTAMP);
            }

            if (ycht.getHoanTatLuc() != null) {
                ps.setTimestamp(11, Timestamp.valueOf(ycht.getHoanTatLuc()));
            } else {
                ps.setNull(11, Types.TIMESTAMP);
            }

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long genId = rs.getLong(1);
                        ycht.setId(genId);
                        return genId;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm yêu cầu hỗ trợ: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Cập nhật thông tin yêu cầu hỗ trợ.
     */
    public boolean capNhatYeuCau(YeuCauHoTro ycht) {
        if (ycht == null || ycht.getId() == null) {
            return false;
        }

        String sql = "UPDATE yeu_cau_ho_tro SET nguoi_lien_he_id = ?, nguoi_xu_ly_id = ?, " +
                "tieu_de = ?, noi_dung = ?, muc_uu_tien = ?, trang_thai = ?, " +
                "xu_ly_luc = ?, hoan_tat_luc = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (ycht.getNguoiLienHeId() != null && ycht.getNguoiLienHeId() > 0) {
                ps.setLong(1, ycht.getNguoiLienHeId());
            } else {
                ps.setNull(1, Types.BIGINT);
            }

            if (ycht.getNguoiXuLyId() != null && ycht.getNguoiXuLyId() > 0) {
                ps.setLong(2, ycht.getNguoiXuLyId());
            } else {
                ps.setNull(2, Types.BIGINT);
            }

            ps.setString(3, ycht.getTieuDe());
            ps.setString(4, ycht.getNoiDung());
            ps.setString(5, ycht.getMucUuTien());
            ps.setString(6, ycht.getTrangThai());

            if (ycht.getXuLyLuc() != null) {
                ps.setTimestamp(7, Timestamp.valueOf(ycht.getXuLyLuc()));
            } else {
                ps.setNull(7, Types.TIMESTAMP);
            }

            if (ycht.getHoanTatLuc() != null) {
                ps.setTimestamp(8, Timestamp.valueOf(ycht.getHoanTatLuc()));
            } else {
                ps.setNull(8, Types.TIMESTAMP);
            }

            ps.setLong(9, ycht.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật yêu cầu hỗ trợ [" + ycht.getId() + "]: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Cập nhật trạng thái và thời gian xử lý/hoàn tất của yêu cầu hỗ trợ.
     */
    public boolean capNhatTrangThai(Long id, String trangThai, LocalDateTime xuLyLuc, LocalDateTime hoanTatLuc) {
        if (id == null || id <= 0 || trangThai == null) {
            return false;
        }

        String sql = "UPDATE yeu_cau_ho_tro SET trang_thai = ?, xu_ly_luc = COALESCE(?, xu_ly_luc), " +
                "hoan_tat_luc = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);

            if (xuLyLuc != null) {
                ps.setTimestamp(2, Timestamp.valueOf(xuLyLuc));
            } else {
                ps.setNull(2, Types.TIMESTAMP);
            }

            if (hoanTatLuc != null) {
                ps.setTimestamp(3, Timestamp.valueOf(hoanTatLuc));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }

            ps.setLong(4, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật trạng thái yêu cầu hỗ trợ [" + id + "]: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Tìm yêu cầu hỗ trợ theo ID.
     */
    public YeuCauHoTro timTheoId(Long id) {
        if (id == null || id <= 0) {
            return null;
        }

        String sql = "SELECT y.id, y.ma_yeu_cau, y.khach_hang_id, y.nguoi_lien_he_id, y.nguoi_xu_ly_id, " +
                "y.tieu_de, y.noi_dung, y.muc_uu_tien, y.trang_thai, y.tao_luc, y.xu_ly_luc, y.hoan_tat_luc, " +
                "kh.ten_cong_ty AS ten_khach_hang, kh.ma_khach_hang, " +
                "nlh.ho_ten AS ten_nguoi_lien_he, nlh.so_dien_thoai AS sdt_nguoi_lien_he, " +
                "nd.ho_ten AS ten_nguoi_xu_ly, nd.email AS email_nguoi_xu_ly " +
                "FROM yeu_cau_ho_tro y " +
                "JOIN khach_hang kh ON y.khach_hang_id = kh.id " +
                "LEFT JOIN nguoi_lien_he nlh ON y.nguoi_lien_he_id = nlh.id " +
                "LEFT JOIN nguoi_dung nd ON y.nguoi_xu_ly_id = nd.id " +
                "WHERE y.id = ? LIMIT 1";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm yêu cầu hỗ trợ theo id [" + id + "]: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Tìm yêu cầu hỗ trợ theo mã yêu cầu.
     */
    public YeuCauHoTro timTheoMa(String maYeuCau) {
        if (maYeuCau == null || maYeuCau.isBlank()) {
            return null;
        }

        String sql = "SELECT y.id, y.ma_yeu_cau, y.khach_hang_id, y.nguoi_lien_he_id, y.nguoi_xu_ly_id, " +
                "y.tieu_de, y.noi_dung, y.muc_uu_tien, y.trang_thai, y.tao_luc, y.xu_ly_luc, y.hoan_tat_luc, " +
                "kh.ten_cong_ty AS ten_khach_hang, kh.ma_khach_hang, " +
                "nlh.ho_ten AS ten_nguoi_lien_he, nlh.so_dien_thoai AS sdt_nguoi_lien_he, " +
                "nd.ho_ten AS ten_nguoi_xu_ly, nd.email AS email_nguoi_xu_ly " +
                "FROM yeu_cau_ho_tro y " +
                "JOIN khach_hang kh ON y.khach_hang_id = kh.id " +
                "LEFT JOIN nguoi_lien_he nlh ON y.nguoi_lien_he_id = nlh.id " +
                "LEFT JOIN nguoi_dung nd ON y.nguoi_xu_ly_id = nd.id " +
                "WHERE y.ma_yeu_cau = ? LIMIT 1";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maYeuCau.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm yêu cầu hỗ trợ theo mã [" + maYeuCau + "]: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Lấy danh sách yêu cầu hỗ trợ của một khách hàng (hiển thị trên trang 360).
     */
    public List<YeuCauHoTro> layDanhSachTheoKhachHang(Long khachHangId) {
        List<YeuCauHoTro> list = new ArrayList<>();
        if (khachHangId == null || khachHangId <= 0) {
            return list;
        }

        String sql = "SELECT y.id, y.ma_yeu_cau, y.khach_hang_id, y.nguoi_lien_he_id, y.nguoi_xu_ly_id, " +
                "y.tieu_de, y.noi_dung, y.muc_uu_tien, y.trang_thai, y.tao_luc, y.xu_ly_luc, y.hoan_tat_luc, " +
                "kh.ten_cong_ty AS ten_khach_hang, kh.ma_khach_hang, " +
                "nlh.ho_ten AS ten_nguoi_lien_he, nlh.so_dien_thoai AS sdt_nguoi_lien_he, " +
                "nd.ho_ten AS ten_nguoi_xu_ly, nd.email AS email_nguoi_xu_ly " +
                "FROM yeu_cau_ho_tro y " +
                "JOIN khach_hang kh ON y.khach_hang_id = kh.id " +
                "LEFT JOIN nguoi_lien_he nlh ON y.nguoi_lien_he_id = nlh.id " +
                "LEFT JOIN nguoi_dung nd ON y.nguoi_xu_ly_id = nd.id " +
                "WHERE y.khach_hang_id = ? " +
                "ORDER BY y.tao_luc DESC, y.id DESC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, khachHangId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách yêu cầu hỗ trợ của khách hàng [" + khachHangId + "]: " + e.getMessage(), e);
        }

        return list;
    }

    /**
     * Đếm số yêu cầu hỗ trợ CHƯA XỬ LÝ của khách hàng (Story S3-08 AC2).
     * Các trạng thái chưa xử lý: MOI, DANG_XU_LY, CHO_KHACH_HANG (hoặc NOT IN DA_XU_LY, DONG, HUY).
     *
     * @param khachHangId ID khách hàng
     * @return Số lượng yêu cầu chưa xử lý
     */
    public int demYeuCauChuaXuLy(Long khachHangId) {
        if (khachHangId == null || khachHangId <= 0) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM yeu_cau_ho_tro " +
                "WHERE khach_hang_id = ? AND trang_thai IN ('MOI', 'DANG_XU_LY', 'CHO_KHACH_HANG')";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, khachHangId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đếm số yêu cầu chưa xử lý cho khách hàng [" + khachHangId + "]: " + e.getMessage(), e);
        }

        return 0;
    }

    /**
     * Lấy danh sách yêu cầu hỗ trợ có bộ lọc linh hoạt.
     */
    public List<YeuCauHoTro> layDanhSach(Long khachHangId, String trangThai, String mucUuTien,
                                         Long nguoiXuLyId, String tuKhoa, int limit, int offset) {
        List<YeuCauHoTro> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT y.id, y.ma_yeu_cau, y.khach_hang_id, y.nguoi_lien_he_id, y.nguoi_xu_ly_id, ")
                .append("y.tieu_de, y.noi_dung, y.muc_uu_tien, y.trang_thai, y.tao_luc, y.xu_ly_luc, y.hoan_tat_luc, ")
                .append("kh.ten_cong_ty AS ten_khach_hang, kh.ma_khach_hang, ")
                .append("nlh.ho_ten AS ten_nguoi_lien_he, nlh.so_dien_thoai AS sdt_nguoi_lien_he, ")
                .append("nd.ho_ten AS ten_nguoi_xu_ly, nd.email AS email_nguoi_xu_ly ")
                .append("FROM yeu_cau_ho_tro y ")
                .append("JOIN khach_hang kh ON y.khach_hang_id = kh.id ")
                .append("LEFT JOIN nguoi_lien_he nlh ON y.nguoi_lien_he_id = nlh.id ")
                .append("LEFT JOIN nguoi_dung nd ON y.nguoi_xu_ly_id = nd.id ")
                .append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (khachHangId != null && khachHangId > 0) {
            sql.append("AND y.khach_hang_id = ? ");
            params.add(khachHangId);
        }
        if (trangThai != null && !trangThai.isBlank() && !"TAT_CA".equalsIgnoreCase(trangThai)) {
            sql.append("AND y.trang_thai = ? ");
            params.add(trangThai.trim());
        }
        if (mucUuTien != null && !mucUuTien.isBlank() && !"TAT_CA".equalsIgnoreCase(mucUuTien)) {
            sql.append("AND y.muc_uu_tien = ? ");
            params.add(mucUuTien.trim());
        }
        if (nguoiXuLyId != null && nguoiXuLyId > 0) {
            sql.append("AND y.nguoi_xu_ly_id = ? ");
            params.add(nguoiXuLyId);
        }
        if (tuKhoa != null && !tuKhoa.isBlank()) {
            sql.append("AND (y.tieu_de LIKE ? OR y.ma_yeu_cau LIKE ? OR kh.ten_cong_ty LIKE ?) ");
            String kw = "%" + tuKhoa.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        sql.append("ORDER BY y.tao_luc DESC, y.id DESC LIMIT ? OFFSET ?");
        params.add(limit > 0 ? limit : 50);
        params.add(Math.max(offset, 0));

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Long) {
                    ps.setLong(i + 1, (Long) p);
                } else if (p instanceof Integer) {
                    ps.setInt(i + 1, (Integer) p);
                } else {
                    ps.setString(i + 1, p.toString());
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách yêu cầu hỗ trợ có bộ lọc: " + e.getMessage(), e);
        }

        return list;
    }

    /**
     * Tự sinh mã yêu cầu ngẫu nhiên duy nhất theo định dạng TK-YYYYMMDD-XXXX.
     */
    public String sinhMaYeuCau() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "TK-" + datePart + "-" + randomPart;
    }

    private YeuCauHoTro mapResultSet(ResultSet rs) throws SQLException {
        YeuCauHoTro y = new YeuCauHoTro();
        y.setId(rs.getLong("id"));
        y.setMaYeuCau(rs.getString("ma_yeu_cau"));
        y.setKhachHangId(rs.getLong("khach_hang_id"));

        long nlhId = rs.getLong("nguoi_lien_he_id");
        if (!rs.wasNull()) y.setNguoiLienHeId(nlhId);

        long nxlId = rs.getLong("nguoi_xu_ly_id");
        if (!rs.wasNull()) y.setNguoiXuLyId(nxlId);

        y.setTieuDe(rs.getString("tieu_de"));
        y.setNoiDung(rs.getString("noi_dung"));
        y.setMucUuTien(rs.getString("muc_uu_tien"));
        y.setTrangThai(rs.getString("trang_thai"));

        Timestamp taoLuc = rs.getTimestamp("tao_luc");
        if (taoLuc != null) y.setTaoLuc(taoLuc.toLocalDateTime());

        Timestamp xuLyLuc = rs.getTimestamp("xu_ly_luc");
        if (xuLyLuc != null) y.setXuLyLuc(xuLyLuc.toLocalDateTime());

        Timestamp hoanTatLuc = rs.getTimestamp("hoan_tat_luc");
        if (hoanTatLuc != null) y.setHoanTatLuc(hoanTatLuc.toLocalDateTime());

        try { y.setTenKhachHang(rs.getString("ten_khach_hang")); } catch (SQLException ignored) {}
        try { y.setMaKhachHang(rs.getString("ma_khach_hang")); } catch (SQLException ignored) {}
        try { y.setTenNguoiLienHe(rs.getString("ten_nguoi_lien_he")); } catch (SQLException ignored) {}
        try { y.setSdtNguoiLienHe(rs.getString("sdt_nguoi_lien_he")); } catch (SQLException ignored) {}
        try { y.setTenNguoiXuLy(rs.getString("ten_nguoi_xu_ly")); } catch (SQLException ignored) {}
        try { y.setEmailNguoiXuLy(rs.getString("email_nguoi_xu_ly")); } catch (SQLException ignored) {}

        return y;
    }
}
