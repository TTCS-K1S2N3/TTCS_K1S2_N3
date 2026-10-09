package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.ThongBao;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object quản lý bảng `thong_bao` (Story S3-08).
 * Lưu và truy vấn thông báo cảnh báo khách hàng có rủi ro rời bỏ cho nhân viên kinh doanh phụ trách.
 */
public class ThongBaoDAO {

    private static final Logger LOGGER = Logger.getLogger(ThongBaoDAO.class.getName());
    public static final long LOAI_THONG_BAO_KHACH_HANG_RUI_RO = 10L;

    /**
     * Tạo mới một thông báo trong hệ thống.
     *
     * @param tb Đối tượng thông báo
     * @return ID tự sinh, hoặc null nếu lỗi
     */
    public Long taoThongBao(ThongBao tb) {
        if (tb == null || tb.getNguoiDungId() == null || tb.getTieuDe() == null) {
            return null;
        }

        String sql = "INSERT INTO thong_bao (nguoi_dung_id, loai_thong_bao_id, tieuDe, noiDung, " +
                "loai_doi_tuong, doi_tuong_id, duong_dan_mo, da_doc, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Lưu ý: Tên cột trong schema là tieu_de, noi_dung
        String sqlChuan = "INSERT INTO thong_bao (nguoi_dung_id, loai_thong_bao_id, tieu_de, noi_dung, " +
                "loai_doi_tuong, doi_tuong_id, duong_dan_mo, da_doc, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sqlChuan, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, tb.getNguoiDungId());
            ps.setLong(2, tb.getLoaiThongBaoId() != null ? tb.getLoaiThongBaoId() : LOAI_THONG_BAO_KHACH_HANG_RUI_RO);
            ps.setString(3, tb.getTieuDe());
            ps.setString(4, tb.getNoiDung());
            ps.setString(5, tb.getLoaiDoiTuong() != null ? tb.getLoaiDoiTuong() : "KHACH_HANG");

            if (tb.getDoiTuongId() != null) {
                ps.setLong(6, tb.getDoiTuongId());
            } else {
                ps.setNull(6, Types.BIGINT);
            }

            ps.setString(7, tb.getDuongDanMo());
            ps.setBoolean(8, tb.isDaDoc());

            Timestamp ts = tb.getCreatedAt() != null ? Timestamp.valueOf(tb.getCreatedAt()) : new Timestamp(System.currentTimeMillis());
            ps.setTimestamp(9, ts);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long generatedId = rs.getLong(1);
                        tb.setId(generatedId);
                        return generatedId;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tạo thông báo: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Lấy danh sách thông báo theo người dùng.
     */
    public List<ThongBao> layDanhSachTheoNguoiDung(Long nguoiDungId, Boolean chiChuaDoc, int limit) {
        List<ThongBao> list = new ArrayList<>();
        if (nguoiDungId == null || nguoiDungId <= 0) {
            return list;
        }

        StringBuilder sql = new StringBuilder("SELECT id, nguoi_dung_id, loai_thong_bao_id, tieu_de, noi_dung, ")
                .append("loai_doi_tuong, doi_tuong_id, duong_dan_mo, da_doc, doc_luc, created_at ")
                .append("FROM thong_bao WHERE nguoi_dung_id = ? ");

        if (chiChuaDoc != null && chiChuaDoc) {
            sql.append("AND da_doc = 0 ");
        }

        sql.append("ORDER BY created_at DESC LIMIT ?");

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setLong(1, nguoiDungId);
            ps.setInt(2, limit > 0 ? limit : 20);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ThongBao tb = mapResultSet(rs);
                    list.add(tb);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách thông báo: " + e.getMessage(), e);
        }

        return list;
    }

    /**
     * Đếm số thông báo chưa đọc của người dùng.
     */
    public int demThongBaoChuaDoc(Long nguoiDungId) {
        if (nguoiDungId == null || nguoiDungId <= 0) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM thong_bao WHERE nguoi_dung_id = ? AND da_doc = 0";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, nguoiDungId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đếm thông báo chưa đọc: " + e.getMessage(), e);
        }

        return 0;
    }

    /**
     * Đánh dấu thông báo đã đọc.
     */
    public boolean danhDauDaDoc(Long thongBaoId, Long nguoiDungId) {
        if (thongBaoId == null || thongBaoId <= 0) {
            return false;
        }

        String sql = "UPDATE thong_bao SET da_doc = 1, doc_luc = CURRENT_TIMESTAMP WHERE id = ? AND nguoi_dung_id = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, thongBaoId);
            ps.setLong(2, nguoiDungId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đánh dấu đã đọc: " + e.getMessage(), e);
            return false;
        }
    }

    private ThongBao mapResultSet(ResultSet rs) throws SQLException {
        ThongBao tb = new ThongBao();
        tb.setId(rs.getLong("id"));
        tb.setNguoiDungId(rs.getLong("nguoi_dung_id"));
        tb.setLoaiThongBaoId(rs.getLong("loai_thong_bao_id"));
        tb.setTieuDe(rs.getString("tieu_de"));
        tb.setNoiDung(rs.getString("noi_dung"));
        tb.setLoaiDoiTuong(rs.getString("loai_doi_tuong"));

        long dtId = rs.getLong("doi_tuong_id");
        if (!rs.wasNull()) {
            tb.setDoiTuongId(dtId);
        }

        tb.setDuongDanMo(rs.getString("duong_dan_mo"));
        tb.setDaDoc(rs.getBoolean("da_doc"));

        Timestamp docLuc = rs.getTimestamp("doc_luc");
        if (docLuc != null) {
            tb.setDocLuc(docLuc.toLocalDateTime());
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            tb.setCreatedAt(createdAt.toLocalDateTime());
        }

        return tb;
    }
}
