package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.BieuMauLead;
import vn.nhom10.crm.model.NhatKyGuiFormLead;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO xử lý thao tác với bảng 'bieu_mau_lead' và 'nhat_ky_gui_form_lead' (Story S4-01).
 * Tuân thủ DATABASE_RULES.md (PreparedStatement, SQL chuẩn MySQL 8.4 LTS, không dùng SELECT *).
 */
public class BieuMauLeadDAO {

    private static final Logger LOGGER = Logger.getLogger(BieuMauLeadDAO.class.getName());

    private static final String COT_SELECT_BIEU_MAU =
            "bm.id, bm.ma_bieu_mau, bm.ten_bieu_mau, bm.token_cong_khai, " +
            "bm.nguon_lead_id, nl.ten_nguon AS ten_nguon_lead, " +
            "bm.chien_dich_id, cd.ten_chien_dich AS ten_chien_dich, " +
            "bm.cau_hinh_truong_json, bm.gioi_han_gui_moi_gio, bm.bat_honeypot, bm.hoat_dong, " +
            "bm.created_by, nd.ho_ten AS ten_nguoi_tao, bm.created_at, bm.updated_at ";

    private static final String FROM_JOIN_BIEU_MAU =
            "FROM bieu_mau_lead bm " +
            "LEFT JOIN nguon_lead nl ON bm.nguon_lead_id = nl.id " +
            "LEFT JOIN chien_dich cd ON bm.chien_dich_id = cd.id " +
            "LEFT JOIN nguoi_dung nd ON bm.created_by = nd.id ";

    /**
     * Thêm mới một biểu mẫu lead.
     */
    public Long them(BieuMauLead bm) {
        String sql = "INSERT INTO bieu_mau_lead (" +
                "ma_bieu_mau, ten_bieu_mau, token_cong_khai, nguon_lead_id, chien_dich_id, " +
                "cau_hinh_truong_json, gioi_han_gui_moi_gio, bat_honeypot, hoat_dong, created_by, created_at" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            datThamSoBieuMau(ps, bm);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    bm.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm biểu mẫu lead: " + bm.getMaBieuMau(), e);
            throw new RuntimeException("Lỗi lưu biểu mẫu lead vào cơ sở dữ liệu: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Cập nhật thông tin biểu mẫu lead.
     */
    public boolean capNhat(BieuMauLead bm) {
        String sql = "UPDATE bieu_mau_lead SET " +
                "ten_bieu_mau = ?, nguon_lead_id = ?, chien_dich_id = ?, " +
                "cau_hinh_truong_json = ?, gioi_han_gui_moi_gio = ?, bat_honeypot = ?, hoat_dong = ?, " +
                "updated_at = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, bm.getTenBieuMau());
            ps.setLong(2, bm.getNguonLeadId());

            if (bm.getChienDichId() != null) {
                ps.setLong(3, bm.getChienDichId());
            } else {
                ps.setNull(3, Types.BIGINT);
            }

            ps.setString(4, bm.getCauHinhTruongJson());

            if (bm.getGioiHanGuiMoiGio() != null) {
                ps.setInt(5, bm.getGioiHanGuiMoiGio());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            ps.setBoolean(6, bm.isBatHoneypot());
            ps.setBoolean(7, bm.isHoatDong());
            ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(9, bm.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật biểu mẫu lead ID: " + bm.getId(), e);
            throw new RuntimeException("Lỗi cập nhật biểu mẫu lead: " + e.getMessage(), e);
        }
    }

    /**
     * Tìm biểu mẫu theo ID.
     */
    public BieuMauLead timTheoId(Long id) {
        if (id == null) return null;
        String sql = "SELECT " + COT_SELECT_BIEU_MAU + FROM_JOIN_BIEU_MAU + "WHERE bm.id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetSangBieuMau(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm biểu mẫu theo ID: " + id, e);
        }
        return null;
    }

    /**
     * Tìm biểu mẫu theo token công khai (dùng khi khách truy cập nhúng/gửi form).
     */
    public BieuMauLead timTheoToken(String tokenCongKhai) {
        if (tokenCongKhai == null || tokenCongKhai.trim().isEmpty()) return null;
        String sql = "SELECT " + COT_SELECT_BIEU_MAU + FROM_JOIN_BIEU_MAU + "WHERE bm.token_cong_khai = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tokenCongKhai.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetSangBieuMau(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm biểu mẫu theo token: " + tokenCongKhai, e);
        }
        return null;
    }

    /**
     * Tìm biểu mẫu theo mã biểu mẫu.
     */
    public BieuMauLead timTheoMa(String maBieuMau) {
        if (maBieuMau == null || maBieuMau.trim().isEmpty()) return null;
        String sql = "SELECT " + COT_SELECT_BIEU_MAU + FROM_JOIN_BIEU_MAU + "WHERE bm.ma_bieu_mau = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maBieuMau.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetSangBieuMau(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm biểu mẫu theo mã: " + maBieuMau, e);
        }
        return null;
    }

    /**
     * Lấy danh sách tất cả biểu mẫu (có thể lọc chỉ lấy biểu mẫu đang hoạt động).
     */
    public List<BieuMauLead> layDanhSach(Boolean chiLayHoatDong) {
        List<BieuMauLead> danhSach = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT " + COT_SELECT_BIEU_MAU + FROM_JOIN_BIEU_MAU);

        if (chiLayHoatDong != null && chiLayHoatDong) {
            sql.append("WHERE bm.hoat_dong = 1 ");
        }
        sql.append("ORDER BY bm.created_at DESC");

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString());
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                danhSach.add(mapResultSetSangBieuMau(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách biểu mẫu lead", e);
        }
        return danhSach;
    }

    /**
     * Kiểm tra trùng mã biểu mẫu.
     */
    public boolean kiemTraTonTaiMa(String maBieuMau, Long excludeId) {
        if (maBieuMau == null || maBieuMau.trim().isEmpty()) return false;
        StringBuilder sql = new StringBuilder("SELECT COUNT(id) FROM bieu_mau_lead WHERE ma_bieu_mau = ?");
        if (excludeId != null) {
            sql.append(" AND id != ?");
        }

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            ps.setString(1, maBieuMau.trim());
            if (excludeId != null) {
                ps.setLong(2, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kiểm tra trùng mã biểu mẫu: " + maBieuMau, e);
        }
        return false;
    }

    /**
     * Đếm số lượt gửi biểu mẫu từ một địa chỉ IP trong vòng 1 giờ qua (AC3: giới hạn tần suất).
     */
    public int demSoLanGuiTrongMotGio(Long bieuMauId, String diaChiIp) {
        if (bieuMauId == null || diaChiIp == null) return 0;

        String sql = "SELECT COUNT(id) FROM nhat_ky_gui_form_lead " +
                "WHERE bieu_mau_id = ? AND dia_chi_ip = ? AND created_at >= ?";

        LocalDateTime motGioTruoc = LocalDateTime.now().minusHours(1);

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, bieuMauId);
            ps.setString(2, diaChiIp.trim());
            ps.setTimestamp(3, Timestamp.valueOf(motGioTruoc));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đếm số lượt gửi từ IP: " + diaChiIp, e);
        }
        return 0;
    }

    /**
     * Ghi nhật ký lượt gửi biểu mẫu (chạy trong transaction của Service).
     */
    public Long ghiNhatKy(Connection conn, NhatKyGuiFormLead nhatKy) throws SQLException {
        String sql = "INSERT INTO nhat_ky_gui_form_lead (bieu_mau_id, dia_chi_ip, thanh_cong, ly_do_tu_choi, created_at) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, nhatKy.getBieuMauId());
            ps.setString(2, nhatKy.getDiaChiIp());
            ps.setBoolean(3, nhatKy.isThanhCong());
            ps.setString(4, nhatKy.getLyDoTuChoi());
            ps.setTimestamp(5, Timestamp.valueOf(nhatKy.getCreatedAt() != null ? nhatKy.getCreatedAt() : LocalDateTime.now()));

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    nhatKy.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    /**
     * Ghi nhật ký độc lập khi bị từ chối trước transaction hoặc từ chối chống spam/rate limit.
     */
    public Long ghiNhatKy(NhatKyGuiFormLead nhatKy) {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return ghiNhatKy(conn, nhatKy);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi ghi nhật ký gửi biểu mẫu lead", e);
            return null;
        }
    }

    /**
     * Lấy lịch sử gửi của một biểu mẫu.
     */
    public List<NhatKyGuiFormLead> layLichSuGui(Long bieuMauId, int limit) {
        List<NhatKyGuiFormLead> danhSach = new ArrayList<>();
        if (bieuMauId == null) return danhSach;

        String sql = "SELECT id, bieu_mau_id, dia_chi_ip, thanh_cong, ly_do_tu_choi, created_at " +
                "FROM nhat_ky_gui_form_lead WHERE bieu_mau_id = ? ORDER BY created_at DESC LIMIT ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, bieuMauId);
            ps.setInt(2, limit > 0 ? limit : 50);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    NhatKyGuiFormLead nk = new NhatKyGuiFormLead();
                    nk.setId(rs.getLong("id"));
                    nk.setBieuMauId(rs.getLong("bieu_mau_id"));
                    nk.setDiaChiIp(rs.getString("dia_chi_ip"));
                    nk.setThanhCong(rs.getBoolean("thanh_cong"));
                    nk.setLyDoTuChoi(rs.getString("ly_do_tu_choi"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        nk.setCreatedAt(ts.toLocalDateTime());
                    }
                    danhSach.add(nk);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy lịch sử gửi form ID: " + bieuMauId, e);
        }
        return danhSach;
    }

    private void datThamSoBieuMau(PreparedStatement ps, BieuMauLead bm) throws SQLException {
        ps.setString(1, bm.getMaBieuMau());
        ps.setString(2, bm.getTenBieuMau());
        ps.setString(3, bm.getTokenCongKhai());
        ps.setLong(4, bm.getNguonLeadId());

        if (bm.getChienDichId() != null) {
            ps.setLong(5, bm.getChienDichId());
        } else {
            ps.setNull(5, Types.BIGINT);
        }

        ps.setString(6, bm.getCauHinhTruongJson());

        if (bm.getGioiHanGuiMoiGio() != null) {
            ps.setInt(7, bm.getGioiHanGuiMoiGio());
        } else {
            ps.setNull(7, Types.INTEGER);
        }

        ps.setBoolean(8, bm.isBatHoneypot());
        ps.setBoolean(9, bm.isHoatDong());

        if (bm.getCreatedBy() != null) {
            ps.setLong(10, bm.getCreatedBy());
        } else {
            ps.setNull(10, Types.BIGINT);
        }

        LocalDateTime createdAt = bm.getCreatedAt() != null ? bm.getCreatedAt() : LocalDateTime.now();
        ps.setTimestamp(11, Timestamp.valueOf(createdAt));
    }

    private BieuMauLead mapResultSetSangBieuMau(ResultSet rs) throws SQLException {
        BieuMauLead bm = new BieuMauLead();
        bm.setId(rs.getLong("id"));
        bm.setMaBieuMau(rs.getString("ma_bieu_mau"));
        bm.setTenBieuMau(rs.getString("ten_bieu_mau"));
        bm.setTokenCongKhai(rs.getString("token_cong_khai"));
        bm.setNguonLeadId(rs.getLong("nguon_lead_id"));
        bm.setTenNguonLead(rs.getString("ten_nguon_lead"));

        long cdId = rs.getLong("chien_dich_id");
        if (!rs.wasNull()) {
            bm.setChienDichId(cdId);
            bm.setTenChienDich(rs.getString("ten_chien_dich"));
        }

        bm.setCauHinhTruongJson(rs.getString("cau_hinh_truong_json"));

        int gh = rs.getInt("gioi_han_gui_moi_gio");
        if (!rs.wasNull()) {
            bm.setGioiHanGuiMoiGio(gh);
        }

        bm.setBatHoneypot(rs.getBoolean("bat_honeypot"));
        bm.setHoatDong(rs.getBoolean("hoat_dong"));

        long cb = rs.getLong("created_by");
        if (!rs.wasNull()) {
            bm.setCreatedBy(cb);
            bm.setTenNguoiTao(rs.getString("ten_nguoi_tao"));
        }

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            bm.setCreatedAt(created.toLocalDateTime());
        }
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            bm.setUpdatedAt(updated.toLocalDateTime());
        }

        return bm;
    }
}
