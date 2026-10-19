package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.TruongTuyChinh;
import vn.nhom10.crm.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO xử lý truy vấn dữ liệu cho Trường tuỳ chỉnh (Story S2-08).
 * Tương tác với các bảng:
 * - `truong_tuy_chinh`
 * - `gia_tri_truong_tuy_chinh_khach_hang`
 * - `gia_tri_truong_tuy_chinh_co_hoi`
 * Tuân thủ DATABASE_RULES.md (PreparedStatement, SQL chuẩn MySQL 8.4 LTS, không dùng SELECT *).
 */
public class TruongTuyChinhDAO {

    private static final Logger LOGGER = Logger.getLogger(TruongTuyChinhDAO.class.getName());

    /**
     * Lấy danh sách trường tuỳ chỉnh theo loại đối tượng (KHACH_HANG hoặc CO_HOI),
     * sắp xếp theo thứ tự hiển thị tăng dần, id tăng dần.
     */
    public List<TruongTuyChinh> layDanhSachTheoDoiTuong(String loaiDoiTuong, boolean chiLayDangHoatDong) {
        List<TruongTuyChinh> danhSach = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT id, loai_doi_tuong, ma_truong, ten_truong, kieu_du_lieu, bat_buoc, " +
                "lua_chon_json, gia_tri_mac_dinh_json, thu_tu_hien_thi, hoat_dong, created_by, created_at, updated_at " +
                "FROM truong_tuy_chinh WHERE loai_doi_tuong = ?"
        );

        if (chiLayDangHoatDong) {
            sql.append(" AND hoat_dong = 1");
        }
        sql.append(" ORDER BY thu_tu_hien_thi ASC, id ASC");

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, loaiDoiTuong != null ? loaiDoiTuong.trim().toUpperCase() : "KHACH_HANG");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(mapResultSetToModel(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách trường tuỳ chỉnh cho đối tượng: " + loaiDoiTuong, e);
        }
        return danhSach;
    }

    /**
     * Lấy trường tuỳ chỉnh theo ID.
     */
    public TruongTuyChinh layTheoId(long id) {
        String sql = "SELECT id, loai_doi_tuong, ma_truong, ten_truong, kieu_du_lieu, bat_buoc, " +
                     "lua_chon_json, gia_tri_mac_dinh_json, thu_tu_hien_thi, hoat_dong, created_by, created_at, updated_at " +
                     "FROM truong_tuy_chinh WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToModel(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy trường tuỳ chỉnh theo ID: " + id, e);
        }
        return null;
    }

    /**
     * Tìm trường tuỳ chỉnh theo loại đối tượng và mã trường.
     */
    public TruongTuyChinh timTheoMa(String loaiDoiTuong, String maTruong) {
        if (loaiDoiTuong == null || maTruong == null) return null;
        String sql = "SELECT id, loai_doi_tuong, ma_truong, ten_truong, kieu_du_lieu, bat_buoc, " +
                     "lua_chon_json, gia_tri_mac_dinh_json, thu_tu_hien_thi, hoat_dong, created_by, created_at, updated_at " +
                     "FROM truong_tuy_chinh WHERE loai_doi_tuong = ? AND ma_truong = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, loaiDoiTuong.trim().toUpperCase());
            ps.setString(2, maTruong.trim().toLowerCase());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToModel(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm trường tuỳ chỉnh theo mã: " + maTruong, e);
        }
        return null;
    }

    /**
     * Kiểm tra xem mã trường đã tồn tại trong cùng loại đối tượng chưa (trừ ID đang sửa nếu có).
     */
    public boolean kiemTraTonTaiMa(String loaiDoiTuong, String maTruong, Long excludeId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM truong_tuy_chinh WHERE loai_doi_tuong = ? AND ma_truong = ?");
        if (excludeId != null) {
            sql.append(" AND id != ?");
        }

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, loaiDoiTuong.trim().toUpperCase());
            ps.setString(2, maTruong.trim().toLowerCase());
            if (excludeId != null) {
                ps.setLong(3, excludeId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kiểm tra trùng mã trường: " + maTruong, e);
        }
        return false;
    }

    /**
     * Thêm mới trường tuỳ chỉnh. Trả về ID mới được sinh hoặc -1 nếu lỗi.
     */
    public long them(TruongTuyChinh truong) {
        String sql = "INSERT INTO truong_tuy_chinh (loai_doi_tuong, ma_truong, ten_truong, kieu_du_lieu, " +
                     "bat_buoc, lua_chon_json, gia_tri_mac_dinh_json, thu_tu_hien_thi, hoat_dong, created_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, truong.getLoaiDoiTuong() != null ? truong.getLoaiDoiTuong().trim().toUpperCase() : "KHACH_HANG");
            ps.setString(2, truong.getMaTruong() != null ? truong.getMaTruong().trim().toLowerCase() : "");
            ps.setString(3, truong.getTenNhanGoc() != null ? truong.getTenNhanGoc().trim() : "");
            ps.setString(4, truong.getKieuDuLieu() != null ? truong.getKieuDuLieu().trim().toUpperCase() : "VAN_BAN");
            ps.setBoolean(5, truong.isBatBuoc());
            ps.setString(6, truong.getLuaChonJson());
            String macDinhJson = truong.getGiaTriMacDinhJson();
            if (macDinhJson == null) {
                macDinhJson = "{\"hienThiBoDac\":" + truong.isHienThiBoDac() + ",\"hienThiExcel\":" + truong.isHienThiExcel() + "}";
            }
            ps.setString(7, macDinhJson);
            ps.setInt(8, truong.getThuTuHienThi());
            ps.setBoolean(9, truong.isHoatDong());

            if (truong.getCreatedBy() != null) {
                ps.setLong(10, truong.getCreatedBy());
            } else {
                ps.setNull(10, Types.BIGINT);
            }

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long newId = rs.getLong(1);
                        truong.setId(newId);
                        return newId;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm trường tuỳ chỉnh: " + truong.getMaTruong(), e);
        }
        return -1;
    }

    /**
     * Cập nhật trường tuỳ chỉnh (chỉ sửa nhãn, bắt buộc, danh sách lựa chọn, thứ tự, trạng thái, metadata).
     * Tên kỹ thuật, kiểu dữ liệu và đối tượng không cho thay đổi để bảo toàn toàn vẹn dữ liệu.
     */
    public boolean capNhat(TruongTuyChinh truong) {
        String sql = "UPDATE truong_tuy_chinh SET ten_truong = ?, bat_buoc = ?, lua_chon_json = ?, " +
                     "gia_tri_mac_dinh_json = ?, thu_tu_hien_thi = ?, hoat_dong = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, truong.getTenNhanGoc() != null ? truong.getTenNhanGoc().trim() : "");
            ps.setBoolean(2, truong.isBatBuoc());
            ps.setString(3, truong.getLuaChonJson());
            ps.setString(4, truong.getGiaTriMacDinhJson());
            ps.setInt(5, truong.getThuTuHienThi());
            ps.setBoolean(6, truong.isHoatDong());
            ps.setLong(7, truong.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật trường tuỳ chỉnh ID=" + truong.getId(), e);
            return false;
        }
    }

    /**
     * Đổi trạng thái hoạt động (bật/tắt).
     */
    public boolean doiTrangThai(long id, boolean hoatDong) {
        String sql = "UPDATE truong_tuy_chinh SET hoat_dong = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, hoatDong);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đổi trạng thái trường tuỳ chỉnh ID=" + id, e);
            return false;
        }
    }

    // =========================================================================
    // QUẢN LÝ GIÁ TRỊ TRƯỜNG TUỲ CHỈNH (CHO KHÁCH HÀNG VÀ CƠ HỘI)
    // =========================================================================

    private String layTenBangGiaTri(String loaiDoiTuong) {
        if ("CO_HOI".equalsIgnoreCase(loaiDoiTuong)) {
            return "gia_tri_truong_tuy_chinh_co_hoi";
        }
        if ("KHACH_HANG".equalsIgnoreCase(loaiDoiTuong)) {
            return "gia_tri_truong_tuy_chinh_khach_hang";
        }
        throw new IllegalArgumentException("Loại đối tượng không hợp lệ: " + loaiDoiTuong);
    }

    private String layTenCotDoiTuong(String loaiDoiTuong) {
        if ("CO_HOI".equalsIgnoreCase(loaiDoiTuong)) {
            return "co_hoi_id";
        }
        if ("KHACH_HANG".equalsIgnoreCase(loaiDoiTuong)) {
            return "khach_hang_id";
        }
        throw new IllegalArgumentException("Loại đối tượng không hợp lệ: " + loaiDoiTuong);
    }

    /**
     * Lấy toàn bộ giá trị trường tuỳ chỉnh của một bản ghi (Khách hàng hoặc Cơ hội).
     * Trả về Map<maTruong, giaTriChuoi>.
     */
    public Map<String, String> layGiaTriTheoDoiTuong(String loaiDoiTuong, long doiTuongId) {
        Map<String, String> ketQua = new HashMap<>();
        String tableName = layTenBangGiaTri(loaiDoiTuong);
        String fkCol = layTenCotDoiTuong(loaiDoiTuong);

        String sql = "SELECT t.ma_truong, t.kieu_du_lieu, g.gia_tri_van_ban, g.gia_tri_so, g.gia_tri_ngay, g.gia_tri_json " +
                     "FROM " + tableName + " g " +
                     "JOIN truong_tuy_chinh t ON g.truong_tuy_chinh_id = t.id " +
                     "WHERE g." + fkCol + " = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, doiTuongId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maTruong = rs.getString("ma_truong");
                    String kieu = rs.getString("kieu_du_lieu");
                    String val = formatGiaTriTuResultSet(rs, kieu);
                    if (val != null) {
                        ketQua.put(maTruong, val);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy giá trị trường tuỳ chỉnh cho " + loaiDoiTuong + " ID=" + doiTuongId, e);
        }
        return ketQua;
    }

    /**
     * Lưu/Cập nhật một giá trị trường tuỳ chỉnh theo cặp (doiTuongId, truongId) bằng UPSERT.
     */
    public boolean luuGiaTri(String loaiDoiTuong, long doiTuongId, long truongId, String kieuDuLieu, String rawValue) {
        String tableName = layTenBangGiaTri(loaiDoiTuong);
        String fkCol = layTenCotDoiTuong(loaiDoiTuong);

        String sql = "INSERT INTO " + tableName + " (" + fkCol + ", truong_tuy_chinh_id, gia_tri_van_ban, gia_tri_so, gia_tri_ngay, gia_tri_json) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "gia_tri_van_ban = VALUES(gia_tri_van_ban), " +
                     "gia_tri_so = VALUES(gia_tri_so), " +
                     "gia_tri_ngay = VALUES(gia_tri_ngay), " +
                     "gia_tri_json = VALUES(gia_tri_json)";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, doiTuongId);
            ps.setLong(2, truongId);
            setThamSoGiaTri(ps, 3, kieuDuLieu, rawValue);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lưu giá trị trường tuỳ chỉnh ID=" + truongId + " cho " + loaiDoiTuong + " ID=" + doiTuongId, e);
            return false;
        }
    }

    /**
     * Lưu hàng loạt giá trị trường tuỳ chỉnh từ Map<maTruong, rawValue> cho một đối tượng.
     */
    public boolean luuNhieuGiaTri(String loaiDoiTuong, long doiTuongId, Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return true;
        }

        List<TruongTuyChinh> dsTruong = layDanhSachTheoDoiTuong(loaiDoiTuong, true);
        Map<String, TruongTuyChinh> mapTruong = new HashMap<>();
        for (TruongTuyChinh t : dsTruong) {
            mapTruong.put(t.getMaTruong(), t);
        }

        boolean thanhCong = true;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String ma = entry.getKey();
            if (mapTruong.containsKey(ma)) {
                TruongTuyChinh t = mapTruong.get(ma);
                boolean ok = luuGiaTri(loaiDoiTuong, doiTuongId, t.getId(), t.getKieuDuLieu(), entry.getValue());
                if (!ok) thanhCong = false;
            }
        }
        return thanhCong;
    }

    /**
     * Lấy giá trị trường tuỳ chỉnh cho danh sách nhiều ID đối tượng phục vụ xuất Excel hoặc bảng danh sách.
     * Trả về: Map<doiTuongId, Map<maTruong, giaTri>>.
     */
    public Map<Long, Map<String, String>> layTatCaGiaTriTheoDanhSach(String loaiDoiTuong, List<Long> dsDoiTuongId) {
        Map<Long, Map<String, String>> ketQua = new HashMap<>();
        if (dsDoiTuongId == null || dsDoiTuongId.isEmpty()) {
            return ketQua;
        }

        String tableName = layTenBangGiaTri(loaiDoiTuong);
        String fkCol = layTenCotDoiTuong(loaiDoiTuong);

        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < dsDoiTuongId.size(); i++) {
            inClause.append(i > 0 ? ",?" : "?");
        }

        String sql = "SELECT g." + fkCol + " AS obj_id, t.ma_truong, t.kieu_du_lieu, " +
                     "g.gia_tri_van_ban, g.gia_tri_so, g.gia_tri_ngay, g.gia_tri_json " +
                     "FROM " + tableName + " g " +
                     "JOIN truong_tuy_chinh t ON g.truong_tuy_chinh_id = t.id " +
                     "WHERE g." + fkCol + " IN (" + inClause + ")";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < dsDoiTuongId.size(); i++) {
                ps.setLong(i + 1, dsDoiTuongId.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long objId = rs.getLong("obj_id");
                    String maTruong = rs.getString("ma_truong");
                    String kieu = rs.getString("kieu_du_lieu");
                    String val = formatGiaTriTuResultSet(rs, kieu);

                    ketQua.computeIfAbsent(objId, k -> new HashMap<>()).put(maTruong, val != null ? val : "");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy giá trị trường tuỳ chỉnh hàng loạt cho " + loaiDoiTuong, e);
        }
        return ketQua;
    }

    /**
     * Lấy danh sách bản ghi thực tế từ cơ sở dữ liệu (khach_hang hoặc co_hoi) phục vụ xuất Excel.
     * Tuyệt đối không dùng dữ liệu mẫu (sample/mock data).
     */
    public List<Map<String, Object>> layDanhSachThucTeChoXuatExcel(String loaiDoiTuong) {
        List<Map<String, Object>> danhSach = new ArrayList<>();
        String upper = loaiDoiTuong != null ? loaiDoiTuong.trim().toUpperCase() : "KHACH_HANG";

        if ("KHACH_HANG".equals(upper)) {
            String sql = "SELECT id, ma_khach_hang, ten_cong_ty, ma_so_thue, dia_chi, trang_thai " +
                         "FROM khach_hang ORDER BY id ASC";
            try (Connection conn = DatabaseConnection.layKetNoi();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("id", rs.getLong("id"));
                    String ma = rs.getString("ma_khach_hang");
                    String ten = rs.getString("ten_cong_ty");
                    String mst = rs.getString("ma_so_thue");
                    String diaChi = rs.getString("dia_chi");
                    String trangThai = rs.getString("trang_thai");

                    r.put("ma_khach_hang", ma != null ? ma : "");
                    r.put("ten_cong_ty", ten != null ? ten : "");
                    r.put("ma_so_thue", mst != null ? mst : "");
                    r.put("dia_chi", diaChi != null ? diaChi : "");
                    r.put("trang_thai", trangThai != null ? trangThai : "");

                    // Alias rút gọn
                    r.put("ma", ma != null ? ma : "");
                    r.put("ten", ten != null ? ten : "");
                    r.put("mst", mst != null ? mst : "");
                    r.put("diaChi", diaChi != null ? diaChi : "");
                    r.put("trangThai", trangThai != null ? trangThai : "");

                    danhSach.add(r);
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách khách hàng thực tế cho xuất Excel", e);
            }
        } else if ("CO_HOI".equals(upper)) {
            String sql = "SELECT id, ma_co_hoi, ten_co_hoi, gia_tri_du_kien, ngay_chot_du_kien, trang_thai " +
                         "FROM co_hoi ORDER BY id ASC";
            try (Connection conn = DatabaseConnection.layKetNoi();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("id", rs.getLong("id"));
                    String ma = rs.getString("ma_co_hoi");
                    String ten = rs.getString("ten_co_hoi");
                    BigDecimal val = rs.getBigDecimal("gia_tri_du_kien");
                    String giaTri = (val != null) ? (val.compareTo(BigDecimal.ZERO) == 0 ? "0" : val.stripTrailingZeros().toPlainString()) : "";
                    java.sql.Date d = rs.getDate("ngay_chot_du_kien");
                    String ngayChot = d != null ? d.toString() : "";
                    String trangThai = rs.getString("trang_thai");

                    r.put("ma_co_hoi", ma != null ? ma : "");
                    r.put("ten_co_hoi", ten != null ? ten : "");
                    r.put("gia_tri_du_kien", giaTri);
                    r.put("ngay_chot_du_kien", ngayChot);
                    r.put("trang_thai", trangThai != null ? trangThai : "");

                    // Alias rút gọn
                    r.put("ma", ma != null ? ma : "");
                    r.put("ten", ten != null ? ten : "");
                    r.put("giaTri", giaTri);
                    r.put("ngayChot", ngayChot);
                    r.put("trangThai", trangThai != null ? trangThai : "");

                    danhSach.add(r);
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách cơ hội thực tế cho xuất Excel", e);
            }
        } else {
            throw new IllegalArgumentException("Loại đối tượng không hợp lệ: " + loaiDoiTuong);
        }

        return danhSach;
    }

    // --- Phương thức Helper ánh xạ dữ liệu ---

    private TruongTuyChinh mapResultSetToModel(ResultSet rs) throws SQLException {
        TruongTuyChinh t = new TruongTuyChinh();
        t.setId(rs.getLong("id"));
        t.setLoaiDoiTuong(rs.getString("loai_doi_tuong"));
        t.setMaTruong(rs.getString("ma_truong"));
        t.setTenTruong(rs.getString("ten_truong"));
        t.setKieuDuLieu(rs.getString("kieu_du_lieu"));
        t.setBatBuoc(rs.getBoolean("bat_buoc"));
        t.setLuaChonJson(rs.getString("lua_chon_json"));
        t.setGiaTriMacDinhJson(rs.getString("gia_tri_mac_dinh_json"));
        t.setThuTuHienThi(rs.getInt("thu_tu_hien_thi"));
        t.setHoatDong(rs.getBoolean("hoat_dong"));

        long createdBy = rs.getLong("created_by");
        if (!rs.wasNull()) {
            t.setCreatedBy(createdBy);
        }
        t.setCreatedAt(rs.getTimestamp("created_at"));
        t.setUpdatedAt(rs.getTimestamp("updated_at"));

        // Phân tích cờ hienThiBoDac và hienThiExcel từ gia_tri_mac_dinh_json nếu có
        String defJson = t.getGiaTriMacDinhJson();
        if (defJson != null && defJson.contains("\"hienThiBoDac\":false")) {
            t.setHienThiBoDac(false);
        } else {
            t.setHienThiBoDac(true);
        }
        if (defJson != null && defJson.contains("\"hienThiExcel\":false")) {
            t.setHienThiExcel(false);
        } else {
            t.setHienThiExcel(true);
        }

        return t;
    }

    private void setThamSoGiaTri(PreparedStatement ps, int startIndex, String kieuDuLieu, String rawValue) throws SQLException {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            ps.setNull(startIndex, Types.VARCHAR);     // gia_tri_van_ban
            ps.setNull(startIndex + 1, Types.DECIMAL); // gia_tri_so
            ps.setNull(startIndex + 2, Types.DATE);    // gia_tri_ngay
            ps.setNull(startIndex + 3, Types.VARCHAR); // gia_tri_json
            return;
        }

        String val = rawValue.trim();
        if ("SO".equalsIgnoreCase(kieuDuLieu)) {
            try {
                BigDecimal bd = new BigDecimal(val.replace(",", ""));
                ps.setNull(startIndex, Types.VARCHAR);     // gia_tri_van_ban
                ps.setBigDecimal(startIndex + 1, bd);      // gia_tri_so
                ps.setNull(startIndex + 2, Types.DATE);    // gia_tri_ngay
                ps.setNull(startIndex + 3, Types.VARCHAR); // gia_tri_json
            } catch (Exception e) {
                throw new IllegalArgumentException("Giá trị không phải là số hợp lệ: " + val);
            }
        } else if ("NGAY".equalsIgnoreCase(kieuDuLieu)) {
            try {
                LocalDate ld = LocalDate.parse(val);
                ps.setNull(startIndex, Types.VARCHAR);     // gia_tri_van_ban
                ps.setNull(startIndex + 1, Types.DECIMAL); // gia_tri_so
                ps.setDate(startIndex + 2, java.sql.Date.valueOf(ld)); // gia_tri_ngay
                ps.setNull(startIndex + 3, Types.VARCHAR); // gia_tri_json
            } catch (Exception e) {
                throw new IllegalArgumentException("Giá trị không phải là ngày hợp lệ (YYYY-MM-DD): " + val);
            }
        } else if ("DANH_SACH_CHON".equalsIgnoreCase(kieuDuLieu)) {
            ps.setString(startIndex, val);                 // gia_tri_van_ban
            ps.setNull(startIndex + 1, Types.DECIMAL);     // gia_tri_so
            ps.setNull(startIndex + 2, Types.DATE);        // gia_tri_ngay
            ps.setString(startIndex + 3, "\"" + val.replace("\"", "\\\"") + "\""); // gia_tri_json
        } else {
            // VAN_BAN
            ps.setString(startIndex, val);                 // gia_tri_van_ban
            ps.setNull(startIndex + 1, Types.DECIMAL);     // gia_tri_so
            ps.setNull(startIndex + 2, Types.DATE);        // gia_tri_ngay
            ps.setNull(startIndex + 3, Types.VARCHAR);     // gia_tri_json
        }
    }

    private String formatGiaTriTuResultSet(ResultSet rs, String kieuDuLieu) throws SQLException {
        if ("SO".equalsIgnoreCase(kieuDuLieu)) {
            BigDecimal bd = rs.getBigDecimal("gia_tri_so");
            if (bd != null) {
                return bd.compareTo(BigDecimal.ZERO) == 0 ? "0" : bd.stripTrailingZeros().toPlainString();
            }
            return rs.getString("gia_tri_van_ban");
        } else if ("NGAY".equalsIgnoreCase(kieuDuLieu)) {
            java.sql.Date d = rs.getDate("gia_tri_ngay");
            if (d != null) {
                return d.toString();
            }
            return rs.getString("gia_tri_van_ban");
        } else {
            String vb = rs.getString("gia_tri_van_ban");
            if (vb != null) return vb;
            String js = rs.getString("gia_tri_json");
            if (js != null && js.startsWith("\"") && js.endsWith("\"") && js.length() >= 2) {
                return js.substring(1, js.length() - 1);
            }
            return js;
        }
    }
}
