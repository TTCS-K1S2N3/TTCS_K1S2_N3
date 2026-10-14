package vn.nhom10.crm.dao;

import vn.nhom10.crm.dto.ChienDichThongKeDTO;
import vn.nhom10.crm.model.ChienDich;
import vn.nhom10.crm.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO xử lý các thao tác dữ liệu với bảng 'chien_dich' (Epic EP-04, Story S4-03).
 * Tuân thủ CODING_RULES.md và DATABASE_RULES.md (PreparedStatement, không SELECT *, quản lý kết nối an toàn).
 */
public class ChienDichDAO {

    private static final Logger LOGGER = Logger.getLogger(ChienDichDAO.class.getName());

    private static final String COT_SELECT_CHIEN_DICH =
            "cd.id, cd.ma_chien_dich, cd.ten_chien_dich, cd.ngan_sach, cd.kenh, " +
            "cd.ngay_bat_dau, cd.ngay_ket_thuc, cd.trang_thai, cd.mo_ta, cd.created_by, " +
            "nd.ho_ten AS ten_nguoi_tao, cd.created_at, cd.updated_at ";

    private static final String FROM_JOIN_CHIEN_DICH =
            "FROM chien_dich cd " +
            "LEFT JOIN nguoi_dung nd ON cd.created_by = nd.id ";

    /**
     * Thêm mới một chiến dịch qua Connection được truyền vào (phục vụ Transaction).
     */
    public Long them(Connection conn, ChienDich cd) throws SQLException {
        String sql = "INSERT INTO chien_dich (" +
                "ma_chien_dich, ten_chien_dich, ngan_sach, kenh, ngay_bat_dau, ngay_ket_thuc, " +
                "trang_thai, mo_ta, created_by, created_at, updated_at" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cd.getMaChienDich());
            ps.setString(2, cd.getTenChienDich());
            ps.setBigDecimal(3, cd.getNganSach() != null ? cd.getNganSach() : BigDecimal.ZERO);
            ps.setString(4, cd.getKenh());

            if (cd.getNgayBatDau() != null) {
                ps.setDate(5, java.sql.Date.valueOf(cd.getNgayBatDau()));
            } else {
                ps.setNull(5, Types.DATE);
            }

            if (cd.getNgayKetThuc() != null) {
                ps.setDate(6, java.sql.Date.valueOf(cd.getNgayKetThuc()));
            } else {
                ps.setNull(6, Types.DATE);
            }

            ps.setString(7, cd.getTrangThai() != null ? cd.getTrangThai() : "DANG_CHAY");
            ps.setString(8, cd.getMoTa());

            if (cd.getCreatedBy() != null) {
                ps.setLong(9, cd.getCreatedBy());
            } else {
                ps.setNull(9, Types.BIGINT);
            }

            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            ps.setTimestamp(10, now);
            ps.setTimestamp(11, now);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    cd.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    /**
     * Thêm mới chiến dịch độc lập.
     */
    public Long them(ChienDich cd) {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return them(conn, cd);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm Chiến dịch: " + cd.getTenChienDich(), e);
            throw new RuntimeException("Lỗi lưu Chiến dịch: " + e.getMessage(), e);
        }
    }

    /**
     * Tìm chiến dịch theo ID.
     */
    public ChienDich timTheoId(Long id) {
        if (id == null) return null;
        String sql = "SELECT " + COT_SELECT_CHIEN_DICH + FROM_JOIN_CHIEN_DICH + "WHERE cd.id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetSangChienDich(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm Chiến dịch theo ID: " + id, e);
        }
        return null;
    }

    /**
     * Tìm chiến dịch theo mã chiến dịch.
     */
    public ChienDich timTheoMa(String maChienDich) {
        if (maChienDich == null || maChienDich.isBlank()) return null;
        String sql = "SELECT " + COT_SELECT_CHIEN_DICH + FROM_JOIN_CHIEN_DICH + "WHERE cd.ma_chien_dich = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maChienDich.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetSangChienDich(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm Chiến dịch theo mã: " + maChienDich, e);
        }
        return null;
    }

    /**
     * Kiểm tra mã chiến dịch đã tồn tại hay chưa.
     */
    public boolean kiemTraTonTaiMa(String maChienDich) {
        if (maChienDich == null || maChienDich.isBlank()) return false;
        String sql = "SELECT 1 FROM chien_dich WHERE ma_chien_dich = ? LIMIT 1";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maChienDich.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kiểm tra tồn tại mã chiến dịch: " + maChienDich, e);
        }
        return false;
    }

    /**
     * Lấy danh sách chiến dịch kèm phân trang.
     */
    public List<ChienDich> layDanhSachPhanTrang(int offset, int limit) {
        List<ChienDich> danhSach = new ArrayList<>();
        String sql = "SELECT " + COT_SELECT_CHIEN_DICH + FROM_JOIN_CHIEN_DICH +
                "ORDER BY cd.id DESC LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit > 0 ? limit : 20);
            ps.setInt(2, Math.max(0, offset));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(mapResultSetSangChienDich(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách chiến dịch phân trang", e);
        }
        return danhSach;
    }

    /**
     * Đếm tổng số chiến dịch trong hệ thống.
     */
    public long demTongSo() {
        String sql = "SELECT COUNT(id) FROM chien_dich";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đếm tổng số chiến dịch", e);
        }
        return 0;
    }

    /**
     * Lấy toàn bộ danh sách chiến dịch đang hoạt động để gán cho Lead (AC2).
     */
    public List<ChienDich> layDanhSachHoatDong() {
        List<ChienDich> danhSach = new ArrayList<>();
        String sql = "SELECT " + COT_SELECT_CHIEN_DICH + FROM_JOIN_CHIEN_DICH +
                "WHERE cd.trang_thai = 'DANG_CHAY' OR cd.trang_thai = 'NHAP' " +
                "ORDER BY cd.id DESC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                danhSach.add(mapResultSetSangChienDich(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách chiến dịch hoạt động", e);
        }
        return danhSach;
    }

    /**
     * AC3: Lấy 3 chỉ số thống kê KPI cho MỘT chiến dịch cụ thể:
     * 1. Số lead distinct
     * 2. Số cơ hội distinct
     * 3. Giá trị đã chốt (tổng gia_tri_chot_thuc_te của cơ hội đóng THẮNG)
     * Tránh fan-out join bằng 3 truy vấn độc lập và aggregate an toàn.
     */
    public ChienDichThongKeDTO layThongKeChiTieu(Long chienDichId) {
        if (chienDichId == null) {
            return new ChienDichThongKeDTO(null, 0, 0, BigDecimal.ZERO);
        }

        long leadCount = 0;
        long oppCount = 0;
        BigDecimal closedWonAmount = BigDecimal.ZERO;

        // 1. Đếm số lead
        String sqlLead = "SELECT COUNT(DISTINCT id) FROM `lead` WHERE chien_dich_id = ?";
        // 2. Đếm số cơ hội
        String sqlOpp = "SELECT COUNT(DISTINCT id) FROM co_hoi WHERE chien_dich_id = ?";
        // 3. Tính tổng giá trị chốt thực tế của cơ hội đóng Thắng
        String sqlWon = "SELECT COALESCE(SUM(gia_tri_chot_thuc_te), 0) FROM co_hoi " +
                "WHERE chien_dich_id = ? AND (UPPER(trang_thai) = 'DONG_THANG' OR UPPER(trang_thai) = 'THANG')";

        try (Connection conn = DatabaseConnection.layKetNoi()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlLead)) {
                ps.setLong(1, chienDichId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) leadCount = rs.getLong(1);
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlOpp)) {
                ps.setLong(1, chienDichId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) oppCount = rs.getLong(1);
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlWon)) {
                ps.setLong(1, chienDichId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        BigDecimal val = rs.getBigDecimal(1);
                        if (val != null) closedWonAmount = val;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy thống kê KPI cho chiến dịch ID: " + chienDichId, e);
        }

        return new ChienDichThongKeDTO(chienDichId, leadCount, oppCount, closedWonAmount);
    }

    /**
     * AC3: Lấy bản đồ thống kê KPI cho nhiều chiến dịch cùng lúc (tối ưu tránh N+1 query).
     */
    public Map<Long, ChienDichThongKeDTO> layThongKeChoDanhSach(List<Long> campaignIds) {
        Map<Long, ChienDichThongKeDTO> ketQua = new HashMap<>();
        if (campaignIds == null || campaignIds.isEmpty()) {
            return ketQua;
        }

        for (Long id : campaignIds) {
            ketQua.put(id, new ChienDichThongKeDTO(id, 0, 0, BigDecimal.ZERO));
        }

        String sqlLeads = "SELECT chien_dich_id, COUNT(DISTINCT id) AS cnt FROM `lead` " +
                "WHERE chien_dich_id IS NOT NULL GROUP BY chien_dich_id";

        String sqlOpps = "SELECT chien_dich_id, COUNT(DISTINCT id) AS cnt FROM co_hoi " +
                "WHERE chien_dich_id IS NOT NULL GROUP BY chien_dich_id";

        String sqlWon = "SELECT chien_dich_id, COALESCE(SUM(gia_tri_chot_thuc_te), 0) AS total_val FROM co_hoi " +
                "WHERE chien_dich_id IS NOT NULL AND (UPPER(trang_thai) = 'DONG_THANG' OR UPPER(trang_thai) = 'THANG') " +
                "GROUP BY chien_dich_id";

        try (Connection conn = DatabaseConnection.layKetNoi()) {
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sqlLeads)) {
                while (rs.next()) {
                    long cid = rs.getLong("chien_dich_id");
                    if (ketQua.containsKey(cid)) {
                        ketQua.get(cid).setLeadCount(rs.getLong("cnt"));
                    }
                }
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sqlOpps)) {
                while (rs.next()) {
                    long cid = rs.getLong("chien_dich_id");
                    if (ketQua.containsKey(cid)) {
                        ketQua.get(cid).setOpportunityCount(rs.getLong("cnt"));
                    }
                }
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sqlWon)) {
                while (rs.next()) {
                    long cid = rs.getLong("chien_dich_id");
                    if (ketQua.containsKey(cid)) {
                        BigDecimal val = rs.getBigDecimal("total_val");
                        ketQua.get(cid).setClosedWonAmount(val != null ? val : BigDecimal.ZERO);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy thống kê KPI danh sách chiến dịch", e);
        }

        return ketQua;
    }

    private ChienDich mapResultSetSangChienDich(ResultSet rs) throws SQLException {
        ChienDich cd = new ChienDich();
        cd.setId(rs.getLong("id"));
        cd.setMaChienDich(rs.getString("ma_chien_dich"));
        cd.setTenChienDich(rs.getString("ten_chien_dich"));
        cd.setNganSach(rs.getBigDecimal("ngan_sach"));
        cd.setKenh(rs.getString("kenh"));

        java.sql.Date start = rs.getDate("ngay_bat_dau");
        if (start != null) cd.setNgayBatDau(start.toLocalDate());

        java.sql.Date end = rs.getDate("ngay_ket_thuc");
        if (end != null) cd.setNgayKetThuc(end.toLocalDate());

        cd.setTrangThai(rs.getString("trang_thai"));
        cd.setMoTa(rs.getString("mo_ta"));

        long cb = rs.getLong("created_by");
        if (!rs.wasNull()) cd.setCreatedBy(cb);

        cd.setTenNguoiTao(rs.getString("ten_nguoi_tao"));

        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) cd.setCreatedAt(cat.toLocalDateTime());

        Timestamp uat = rs.getTimestamp("updated_at");
        if (uat != null) cd.setUpdatedAt(uat.toLocalDateTime());

        return cd;
    }
}
