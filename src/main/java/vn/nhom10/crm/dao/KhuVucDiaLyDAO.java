package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.KhuVucDiaLy;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object phụ trách bảng khu_vuc_dia_ly (Story S2-06).
 */
public class KhuVucDiaLyDAO {

    private static final Logger LOGGER = Logger.getLogger(KhuVucDiaLyDAO.class.getName());

    public KhuVucDiaLy timTheoId(long id) {
        String sql = "SELECT kv.id, kv.ma_khu_vuc, kv.ten_khu_vuc, kv.loai_khu_vuc, kv.khu_vuc_cha_id, "
                   + "kv.thu_tu_hien_thi, kv.hoat_dong, kv.created_at, kv.updated_at, "
                   + "cha.ten_khu_vuc AS ten_khu_vuc_cha "
                   + "FROM khu_vuc_dia_ly kv "
                   + "LEFT JOIN khu_vuc_dia_ly cha ON kv.khu_vuc_cha_id = cha.id "
                   + "WHERE kv.id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToModel(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm khu vực theo ID=" + id + ": " + e.getMessage(), e);
        }
        return null;
    }

    public KhuVucDiaLy timTheoMa(String maKhuVuc) {
        if (maKhuVuc == null || maKhuVuc.isBlank()) {
            return null;
        }

        String sql = "SELECT kv.id, kv.ma_khu_vuc, kv.ten_khu_vuc, kv.loai_khu_vuc, kv.khu_vuc_cha_id, "
                   + "kv.thu_tu_hien_thi, kv.hoat_dong, kv.created_at, kv.updated_at, "
                   + "cha.ten_khu_vuc AS ten_khu_vuc_cha "
                   + "FROM khu_vuc_dia_ly kv "
                   + "LEFT JOIN khu_vuc_dia_ly cha ON kv.khu_vuc_cha_id = cha.id "
                   + "WHERE LOWER(kv.ma_khu_vuc) = LOWER(?) LIMIT 1";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maKhuVuc.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToModel(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm khu vực theo mã=" + maKhuVuc + ": " + e.getMessage(), e);
        }
        return null;
    }

    public boolean kiemTraTonTaiMa(String maKhuVuc, Long excludeId) {
        if (maKhuVuc == null || maKhuVuc.isBlank()) {
            return false;
        }

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM khu_vuc_dia_ly WHERE LOWER(ma_khu_vuc) = LOWER(?)");
        if (excludeId != null && excludeId > 0) {
            sql.append(" AND id <> ?");
        }

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            ps.setString(1, maKhuVuc.trim());
            if (excludeId != null && excludeId > 0) {
                ps.setLong(2, excludeId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kiểm tra trùng mã khu vực: " + e.getMessage(), e);
        }
        return false;
    }

    public List<KhuVucDiaLy> layTatCa() {
        List<KhuVucDiaLy> danhSach = new ArrayList<>();
        String sql = "SELECT kv.id, kv.ma_khu_vuc, kv.ten_khu_vuc, kv.loai_khu_vuc, kv.khu_vuc_cha_id, "
                   + "kv.thu_tu_hien_thi, kv.hoat_dong, kv.created_at, kv.updated_at, "
                   + "cha.ten_khu_vuc AS ten_khu_vuc_cha, "
                   + "(SELECT COUNT(*) FROM nhom_kinh_doanh nkd WHERE nkd.khu_vuc_id = kv.id) AS so_luong_nhom "
                   + "FROM khu_vuc_dia_ly kv "
                   + "LEFT JOIN khu_vuc_dia_ly cha ON kv.khu_vuc_cha_id = cha.id "
                   + "ORDER BY kv.thu_tu_hien_thi ASC, kv.id ASC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                KhuVucDiaLy kv = mapResultSetToModel(rs);
                kv.setSoLuongNhom(rs.getInt("so_luong_nhom"));
                danhSach.add(kv);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách khu vực: " + e.getMessage(), e);
        }
        return danhSach;
    }

    public List<KhuVucDiaLy> layTatCaDangHoatDong() {
        List<KhuVucDiaLy> danhSach = new ArrayList<>();
        String sql = "SELECT kv.id, kv.ma_khu_vuc, kv.ten_khu_vuc, kv.loai_khu_vuc, kv.khu_vuc_cha_id, "
                   + "kv.thu_tu_hien_thi, kv.hoat_dong, kv.created_at, kv.updated_at, "
                   + "cha.ten_khu_vuc AS ten_khu_vuc_cha, "
                   + "(SELECT COUNT(*) FROM nhom_kinh_doanh nkd WHERE nkd.khu_vuc_id = kv.id) AS so_luong_nhom "
                   + "FROM khu_vuc_dia_ly kv "
                   + "LEFT JOIN khu_vuc_dia_ly cha ON kv.khu_vuc_cha_id = cha.id "
                   + "WHERE kv.hoat_dong = 1 "
                   + "ORDER BY kv.thu_tu_hien_thi ASC, kv.id ASC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                KhuVucDiaLy kv = mapResultSetToModel(rs);
                kv.setSoLuongNhom(rs.getInt("so_luong_nhom"));
                danhSach.add(kv);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách khu vực hoạt động: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Lấy cấu trúc cây khu vực địa lý.
     */
    public List<KhuVucDiaLy> layCayKhuVuc() {
        List<KhuVucDiaLy> tatCa = layTatCa();
        Map<Long, KhuVucDiaLy> map = new HashMap<>();
        for (KhuVucDiaLy kv : tatCa) {
            map.put(kv.getId(), kv);
        }

        List<KhuVucDiaLy> danhSachGoc = new ArrayList<>();
        for (KhuVucDiaLy kv : tatCa) {
            if (kv.getKhuVucChaId() == null || !map.containsKey(kv.getKhuVucChaId())) {
                kv.setCapDo(1);
                danhSachGoc.add(kv);
            } else {
                KhuVucDiaLy cha = map.get(kv.getKhuVucChaId());
                kv.setCapDo(cha.getCapDo() + 1);
                cha.themKhuVucCon(kv);
            }
        }
        return danhSachGoc;
    }

    public long themKhuVuc(KhuVucDiaLy kv) throws SQLException {
        String sql = "INSERT INTO khu_vuc_dia_ly (ma_khu_vuc, ten_khu_vuc, loai_khu_vuc, khu_vuc_cha_id, thu_tu_hien_thi, hoat_dong) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, kv.getMaKhuVuc().trim());
            ps.setString(2, kv.getTenKhuVuc().trim());
            ps.setString(3, kv.getLoaiKhuVuc() != null ? kv.getLoaiKhuVuc().trim() : null);

            if (kv.getKhuVucChaId() != null && kv.getKhuVucChaId() > 0) {
                ps.setLong(4, kv.getKhuVucChaId());
            } else {
                ps.setNull(4, Types.BIGINT);
            }

            ps.setInt(5, kv.getThuTuHienThi());
            ps.setBoolean(6, kv.isHoatDong());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    kv.setId(id);
                    return id;
                }
            }
        }
        return 0;
    }

    public boolean capNhatKhuVuc(KhuVucDiaLy kv) throws SQLException {
        String sql = "UPDATE khu_vuc_dia_ly SET ten_khu_vuc = ?, loai_khu_vuc = ?, khu_vuc_cha_id = ?, "
                   + "thu_tu_hien_thi = ?, hoat_dong = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, kv.getTenKhuVuc().trim());
            ps.setString(2, kv.getLoaiKhuVuc() != null ? kv.getLoaiKhuVuc().trim() : null);

            if (kv.getKhuVucChaId() != null && kv.getKhuVucChaId() > 0) {
                ps.setLong(3, kv.getKhuVucChaId());
            } else {
                ps.setNull(3, Types.BIGINT);
            }

            ps.setInt(4, kv.getThuTuHienThi());
            ps.setBoolean(5, kv.isHoatDong());
            ps.setLong(6, kv.getId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean xoaKhuVuc(long id) throws SQLException {
        String sql = "DELETE FROM khu_vuc_dia_ly WHERE id = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public int demSoNhomTheoKhuVuc(long khuVucId) {
        String sql = "SELECT COUNT(*) FROM nhom_kinh_doanh WHERE khu_vuc_id = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, khuVucId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đếm số nhóm theo khu vực: " + e.getMessage(), e);
        }
        return 0;
    }

    public int demSoKhuVucCon(long khuVucChaId) {
        String sql = "SELECT COUNT(*) FROM khu_vuc_dia_ly WHERE khu_vuc_cha_id = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, khuVucChaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đếm số khu vực con: " + e.getMessage(), e);
        }
        return 0;
    }

    private KhuVucDiaLy mapResultSetToModel(ResultSet rs) throws SQLException {
        KhuVucDiaLy kv = new KhuVucDiaLy();
        kv.setId(rs.getLong("id"));
        kv.setMaKhuVuc(rs.getString("ma_khu_vuc"));
        kv.setTenKhuVuc(rs.getString("ten_khu_vuc"));
        kv.setLoaiKhuVuc(rs.getString("loai_khu_vuc"));

        long chaId = rs.getLong("khu_vuc_cha_id");
        if (!rs.wasNull()) {
            kv.setKhuVucChaId(chaId);
        }

        kv.setThuTuHienThi(rs.getInt("thu_tu_hien_thi"));
        kv.setHoatDong(rs.getBoolean("hoat_dong"));

        try {
            kv.setCreatedAt(rs.getTimestamp("created_at"));
            kv.setUpdatedAt(rs.getTimestamp("updated_at"));
        } catch (SQLException ignored) {}

        try {
            kv.setTenKhuVucCha(rs.getString("ten_khu_vuc_cha"));
        } catch (SQLException ignored) {}

        return kv;
    }
}
