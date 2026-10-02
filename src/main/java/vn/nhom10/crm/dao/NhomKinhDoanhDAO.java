package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhomKinhDoanh;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO xử lý bảng nhom_kinh_doanh (Story S2-06).
 * Hỗ trợ cấu trúc cây phân cấp nhóm kinh doanh, gán trưởng nhóm, gán khu vực địa lý,
 * và tính toán đệ quy tập hợp nhóm con/cháu phục vụ phân quyền dữ liệu.
 */
public class NhomKinhDoanhDAO {

    private static final Logger LOGGER = Logger.getLogger(NhomKinhDoanhDAO.class.getName());

    public NhomKinhDoanh timTheoId(long id) {
        String sql = "SELECT nkd.id, nkd.ma_nhom, nkd.ten_nhom, nkd.mo_ta, nkd.nhom_cha_id, "
                   + "nkd.khu_vuc_id, nkd.truong_nhom_id, nkd.hoat_dong, nkd.created_at, nkd.updated_at, "
                   + "cha.ten_nhom AS ten_nhom_cha, "
                   + "kv.ten_khu_vuc, kv.ma_khu_vuc, "
                   + "tn.ho_ten AS ten_truong_nhom, tn.email AS email_truong_nhom, "
                   + "(SELECT COUNT(*) FROM nguoi_dung nd WHERE nd.nhom_kinh_doanh_id = nkd.id) AS so_luong_thanh_vien "
                   + "FROM nhom_kinh_doanh nkd "
                   + "LEFT JOIN nhom_kinh_doanh cha ON nkd.nhom_cha_id = cha.id "
                   + "LEFT JOIN khu_vuc_dia_ly kv ON nkd.khu_vuc_id = kv.id "
                   + "LEFT JOIN nguoi_dung tn ON nkd.truong_nhom_id = tn.id "
                   + "WHERE nkd.id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToModel(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm nhóm kinh doanh theo ID=" + id + ": " + e.getMessage(), e);
        }
        return null;
    }

    public NhomKinhDoanh timTheoId(int id) {
        return timTheoId((long) id);
    }

    public NhomKinhDoanh timTheoMa(String maNhom) {
        if (maNhom == null || maNhom.isBlank()) {
            return null;
        }

        String sql = "SELECT nkd.id, nkd.ma_nhom, nkd.ten_nhom, nkd.mo_ta, nkd.nhom_cha_id, "
                   + "nkd.khu_vuc_id, nkd.truong_nhom_id, nkd.hoat_dong, nkd.created_at, nkd.updated_at, "
                   + "cha.ten_nhom AS ten_nhom_cha, "
                   + "kv.ten_khu_vuc, kv.ma_khu_vuc, "
                   + "tn.ho_ten AS ten_truong_nhom, tn.email AS email_truong_nhom, "
                   + "(SELECT COUNT(*) FROM nguoi_dung nd WHERE nd.nhom_kinh_doanh_id = nkd.id) AS so_luong_thanh_vien "
                   + "FROM nhom_kinh_doanh nkd "
                   + "LEFT JOIN nhom_kinh_doanh cha ON nkd.nhom_cha_id = cha.id "
                   + "LEFT JOIN khu_vuc_dia_ly kv ON nkd.khu_vuc_id = kv.id "
                   + "LEFT JOIN nguoi_dung tn ON nkd.truong_nhom_id = tn.id "
                   + "WHERE LOWER(nkd.ma_nhom) = LOWER(?) LIMIT 1";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNhom.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToModel(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm nhóm theo mã " + maNhom + ": " + e.getMessage(), e);
        }
        return null;
    }

    public boolean kiemTraTonTaiMa(String maNhom, Long excludeId) {
        if (maNhom == null || maNhom.isBlank()) {
            return false;
        }

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM nhom_kinh_doanh WHERE LOWER(ma_nhom) = LOWER(?)");
        if (excludeId != null && excludeId > 0) {
            sql.append(" AND id <> ?");
        }

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            ps.setString(1, maNhom.trim());
            if (excludeId != null && excludeId > 0) {
                ps.setLong(2, excludeId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kiểm tra trùng mã nhóm: " + e.getMessage(), e);
        }
        return false;
    }

    public List<NhomKinhDoanh> layTatCa() {
        List<NhomKinhDoanh> danhSach = new ArrayList<>();
        String sql = "SELECT nkd.id, nkd.ma_nhom, nkd.ten_nhom, nkd.mo_ta, nkd.nhom_cha_id, "
                   + "nkd.khu_vuc_id, nkd.truong_nhom_id, nkd.hoat_dong, nkd.created_at, nkd.updated_at, "
                   + "cha.ten_nhom AS ten_nhom_cha, "
                   + "kv.ten_khu_vuc, kv.ma_khu_vuc, "
                   + "tn.ho_ten AS ten_truong_nhom, tn.email AS email_truong_nhom, "
                   + "(SELECT COUNT(*) FROM nguoi_dung nd WHERE nd.nhom_kinh_doanh_id = nkd.id) AS so_luong_thanh_vien "
                   + "FROM nhom_kinh_doanh nkd "
                   + "LEFT JOIN nhom_kinh_doanh cha ON nkd.nhom_cha_id = cha.id "
                   + "LEFT JOIN khu_vuc_dia_ly kv ON nkd.khu_vuc_id = kv.id "
                   + "LEFT JOIN nguoi_dung tn ON nkd.truong_nhom_id = tn.id "
                   + "ORDER BY nkd.id ASC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                danhSach.add(mapResultSetToModel(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn danh sách nhóm kinh doanh: " + e.getMessage(), e);
        }
        return danhSach;
    }

    public List<NhomKinhDoanh> layTatCaDangHoatDong() {
        List<NhomKinhDoanh> danhSach = new ArrayList<>();
        String sql = "SELECT nkd.id, nkd.ma_nhom, nkd.ten_nhom, nkd.mo_ta, nkd.nhom_cha_id, "
                   + "nkd.khu_vuc_id, nkd.truong_nhom_id, nkd.hoat_dong, nkd.created_at, nkd.updated_at, "
                   + "cha.ten_nhom AS ten_nhom_cha, "
                   + "kv.ten_khu_vuc, kv.ma_khu_vuc, "
                   + "tn.ho_ten AS ten_truong_nhom, tn.email AS email_truong_nhom, "
                   + "(SELECT COUNT(*) FROM nguoi_dung nd WHERE nd.nhom_kinh_doanh_id = nkd.id) AS so_luong_thanh_vien "
                   + "FROM nhom_kinh_doanh nkd "
                   + "LEFT JOIN nhom_kinh_doanh cha ON nkd.nhom_cha_id = cha.id "
                   + "LEFT JOIN khu_vuc_dia_ly kv ON nkd.khu_vuc_id = kv.id "
                   + "LEFT JOIN nguoi_dung tn ON nkd.truong_nhom_id = tn.id "
                   + "WHERE nkd.hoat_dong = 1 "
                   + "ORDER BY nkd.id ASC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                danhSach.add(mapResultSetToModel(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn nhóm kinh doanh đang hoạt động: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Lấy cấu trúc cây phân cấp nhóm kinh doanh (Story S2-06, AC1).
     * Trả về danh sách các nhóm gốc (gốc cây), bên trong mỗi nhóm chứa dsNhomCon đệ quy.
     */
    public List<NhomKinhDoanh> layCayNhomKinhDoanh() {
        List<NhomKinhDoanh> tatCa = layTatCa();
        Map<Long, NhomKinhDoanh> map = new HashMap<>();
        for (NhomKinhDoanh nkd : tatCa) {
            map.put(nkd.getIdLong(), nkd);
        }

        List<NhomKinhDoanh> cayNhom = new ArrayList<>();
        for (NhomKinhDoanh nkd : tatCa) {
            if (nkd.getNhomChaIdLong() == null || !map.containsKey(nkd.getNhomChaIdLong())) {
                nkd.setCapDo(1);
                cayNhom.add(nkd);
            } else {
                NhomKinhDoanh cha = map.get(nkd.getNhomChaIdLong());
                nkd.setCapDo(cha.getCapDo() + 1);
                cha.themNhomCon(nkd);
            }
        }
        return cayNhom;
    }

    /**
     * Thu thập ID của chính nhóm này và TẤT CẢ các nhóm con, cháu, chắt... cấp dưới (Story S2-06, AC3).
     * Phục vụ phân quyền dữ liệu để Trưởng nhóm nhìn thấy toàn bộ dữ liệu của cây nhánh mình quản lý.
     */
    public Set<Long> layDsIdNhomConVaChau(long nhomId) {
        Set<Long> ketQua = new HashSet<>();
        ketQua.add(nhomId);

        List<NhomKinhDoanh> tatCa = layTatCa();
        Map<Long, List<Long>> adj = new HashMap<>();
        for (NhomKinhDoanh nkd : tatCa) {
            if (nkd.getNhomChaIdLong() != null) {
                adj.computeIfAbsent(nkd.getNhomChaIdLong(), k -> new ArrayList<>()).add(nkd.getIdLong());
            }
        }

        Queue<Long> queue = new ArrayDeque<>();
        queue.add(nhomId);

        while (!queue.isEmpty()) {
            Long curr = queue.poll();
            List<Long> children = adj.get(curr);
            if (children != null) {
                for (Long childId : children) {
                    if (ketQua.add(childId)) {
                        queue.add(childId);
                    }
                }
            }
        }

        return ketQua;
    }

    /**
     * Thêm mới nhóm kinh doanh.
     */
    public long themNhom(NhomKinhDoanh nhom) throws SQLException {
        String sql = "INSERT INTO nhom_kinh_doanh (ma_nhom, ten_nhom, mo_ta, nhom_cha_id, khu_vuc_id, truong_nhom_id, hoat_dong) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nhom.getMaNhom().trim());
            ps.setString(2, nhom.getTenNhom().trim());
            ps.setString(3, nhom.getMoTa());

            if (nhom.getNhomChaIdLong() != null && nhom.getNhomChaIdLong() > 0) {
                ps.setLong(4, nhom.getNhomChaIdLong());
            } else {
                ps.setNull(4, Types.BIGINT);
            }

            if (nhom.getKhuVucId() != null && nhom.getKhuVucId() > 0) {
                ps.setLong(5, nhom.getKhuVucId());
            } else {
                ps.setNull(5, Types.BIGINT);
            }

            if (nhom.getTruongNhomId() != null && nhom.getTruongNhomId() > 0) {
                ps.setLong(6, nhom.getTruongNhomId());
            } else {
                ps.setNull(6, Types.BIGINT);
            }

            ps.setBoolean(7, nhom.isHoatDong());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    nhom.setId(id);
                    return id;
                }
            }
        }
        return 0;
    }

    /**
     * Cập nhật thông tin nhóm kinh doanh.
     */
    public boolean capNhatNhom(NhomKinhDoanh nhom) throws SQLException {
        String sql = "UPDATE nhom_kinh_doanh SET ten_nhom = ?, mo_ta = ?, nhom_cha_id = ?, "
                   + "khu_vuc_id = ?, truong_nhom_id = ?, hoat_dong = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nhom.getTenNhom().trim());
            ps.setString(2, nhom.getMoTa());

            if (nhom.getNhomChaIdLong() != null && nhom.getNhomChaIdLong() > 0) {
                ps.setLong(3, nhom.getNhomChaIdLong());
            } else {
                ps.setNull(3, Types.BIGINT);
            }

            if (nhom.getKhuVucId() != null && nhom.getKhuVucId() > 0) {
                ps.setLong(4, nhom.getKhuVucId());
            } else {
                ps.setNull(4, Types.BIGINT);
            }

            if (nhom.getTruongNhomId() != null && nhom.getTruongNhomId() > 0) {
                ps.setLong(5, nhom.getTruongNhomId());
            } else {
                ps.setNull(5, Types.BIGINT);
            }

            ps.setBoolean(6, nhom.isHoatDong());
            ps.setLong(7, nhom.getIdLong());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Gán / đổi trưởng nhóm cho nhóm kinh doanh (AC1).
     */
    public boolean ganTruongNhom(long nhomId, Long truongNhomId) throws SQLException {
        String sql = "UPDATE nhom_kinh_doanh SET truong_nhom_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (truongNhomId != null && truongNhomId > 0) {
                ps.setLong(1, truongNhomId);
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setLong(2, nhomId);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Gán khu vực địa lý cho nhóm kinh doanh (AC4).
     */
    public boolean ganKhuVuc(long nhomId, Long khuVucId) throws SQLException {
        String sql = "UPDATE nhom_kinh_doanh SET khu_vuc_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (khuVucId != null && khuVucId > 0) {
                ps.setLong(1, khuVucId);
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setLong(2, nhomId);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Lấy danh sách nhân viên thuộc nhóm kinh doanh (AC2).
     */
    public List<NguoiDung> layDsThanhVien(long nhomId) {
        List<NguoiDung> danhSach = new ArrayList<>();
        String sql = "SELECT id, ho_ten, email, so_dien_thoai, trang_thai, nhom_kinh_doanh_id "
                   + "FROM nguoi_dung WHERE nhom_kinh_doanh_id = ? ORDER BY ho_ten ASC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, nhomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    NguoiDung nd = new NguoiDung();
                    nd.setId(rs.getLong("id"));
                    nd.setHoTen(rs.getString("ho_ten"));
                    nd.setEmail(rs.getString("email"));
                    nd.setSoDienThoai(rs.getString("so_dien_thoai"));
                    nd.setTrangThai(rs.getString("trang_thai"));
                    nd.setNhomKinhDoanhId((int) nhomId);
                    danhSach.add(nd);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách thành viên nhóm " + nhomId + ": " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Gán nhân viên vào nhóm kinh doanh (AC2).
     * Mỗi nhân viên thuộc đúng một nhóm tại một thời điểm.
     */
    public boolean ganNhanVienVaoNhom(long nguoiDungId, Long nhomId) throws SQLException {
        String sql = "UPDATE nguoi_dung SET nhom_kinh_doanh_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (nhomId != null && nhomId > 0) {
                ps.setLong(1, nhomId);
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setLong(2, nguoiDungId);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Lấy danh sách người dùng chưa thuộc nhóm kinh doanh nào.
     */
    public List<NguoiDung> layDsNhanVienChuaCoNhom() {
        List<NguoiDung> danhSach = new ArrayList<>();
        String sql = "SELECT id, ho_ten, email, so_dien_thoai, trang_thai "
                   + "FROM nguoi_dung WHERE (nhom_kinh_doanh_id IS NULL OR nhom_kinh_doanh_id = 0) "
                   + "AND trang_thai = 'HOAT_DONG' ORDER BY ho_ten ASC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                NguoiDung nd = new NguoiDung();
                nd.setId(rs.getLong("id"));
                nd.setHoTen(rs.getString("ho_ten"));
                nd.setEmail(rs.getString("email"));
                nd.setSoDienThoai(rs.getString("so_dien_thoai"));
                nd.setTrangThai(rs.getString("trang_thai"));
                danhSach.add(nd);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách nhân viên chưa có nhóm: " + e.getMessage(), e);
        }
        return danhSach;
    }

    private NhomKinhDoanh mapResultSetToModel(ResultSet rs) throws SQLException {
        NhomKinhDoanh nkd = new NhomKinhDoanh();
        nkd.setId(rs.getLong("id"));
        nkd.setMaNhom(rs.getString("ma_nhom"));
        nkd.setTenNhom(rs.getString("ten_nhom"));
        nkd.setMoTa(rs.getString("mo_ta"));

        long chaId = rs.getLong("nhom_cha_id");
        if (!rs.wasNull()) {
            nkd.setNhomChaId(chaId);
        }

        long kvId = rs.getLong("khu_vuc_id");
        if (!rs.wasNull()) {
            nkd.setKhuVucId(kvId);
        }

        long tnId = rs.getLong("truong_nhom_id");
        if (!rs.wasNull()) {
            nkd.setTruongNhomId(tnId);
        }

        try {
            nkd.setHoatDong(rs.getBoolean("hoat_dong"));
        } catch (SQLException ignored) {}

        try {
            nkd.setCreatedAt(rs.getTimestamp("created_at"));
            nkd.setUpdatedAt(rs.getTimestamp("updated_at"));
        } catch (SQLException ignored) {}

        try {
            nkd.setTenNhomCha(rs.getString("ten_nhom_cha"));
        } catch (SQLException ignored) {}

        try {
            nkd.setTenKhuVuc(rs.getString("ten_khu_vuc"));
            nkd.setMaKhuVuc(rs.getString("ma_khu_vuc"));
        } catch (SQLException ignored) {}

        try {
            nkd.setTenTruongNhom(rs.getString("ten_truong_nhom"));
            nkd.setEmailTruongNhom(rs.getString("email_truong_nhom"));
        } catch (SQLException ignored) {}

        try {
            nkd.setSoLuongThanhVien(rs.getInt("so_luong_thanh_vien"));
        } catch (SQLException ignored) {}

        return nkd;
    }
}
