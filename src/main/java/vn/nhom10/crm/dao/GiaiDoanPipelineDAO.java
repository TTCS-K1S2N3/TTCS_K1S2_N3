package vn.nhom10.crm.dao;

import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dto.DuBaoDoanhSoDTO;
import vn.nhom10.crm.model.DieuKienGiaiDoan;
import vn.nhom10.crm.model.GiaiDoanPipeline;
import vn.nhom10.crm.model.LoaiGiaiDoanEnum;
import vn.nhom10.crm.model.PipelineBanHang;
import vn.nhom10.crm.model.TrangThaiGiaiDoanEnum;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO xử lý truy vấn cho Pipeline bán hàng và các giai đoạn (S2-09).
 * Tương thích 100% với schema canonical:
 * - pipeline_ban_hang
 * - giai_doan_pipeline
 * - dieu_kien_giai_doan
 * - co_hoi
 */
public class GiaiDoanPipelineDAO {

    private static final Logger LOGGER = Logger.getLogger(GiaiDoanPipelineDAO.class.getName());

    /**
     * Lấy pipeline mặc định nếu có trong DB.
     * GET/Read operation KHÔNG được mutate DB.
     */
    public PipelineBanHang layPipelineMacDinh() {
        String sql = "SELECT id, ma_pipeline, ten_pipeline, mo_ta, mac_dinh, hoat_dong, created_at, updated_at " +
                "FROM pipeline_ban_hang WHERE mac_dinh = 1 OR hoat_dong = 1 ORDER BY mac_dinh DESC, id ASC LIMIT 1";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                PipelineBanHang p = new PipelineBanHang();
                p.setId(rs.getLong("id"));
                p.setMaPipeline(rs.getString("ma_pipeline"));
                p.setTenPipeline(rs.getString("ten_pipeline"));
                p.setMoTa(rs.getString("mo_ta"));
                p.setMacDinh(rs.getBoolean("mac_dinh"));
                p.setHoatDong(rs.getBoolean("hoat_dong"));
                p.setCreatedAt(rs.getTimestamp("created_at"));
                p.setUpdatedAt(rs.getTimestamp("updated_at"));
                return p;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Không tìm thấy pipeline: " + e.getMessage());
        }

        return null;
    }

    /**
     * Khởi tạo container pipeline_ban_hang mặc định khi có thao tác ghi hợp lệ (nếu chưa có).
     * Chỉ tạo container bảng cha, KHÔNG tự ý chèn các giai đoạn mẫu.
     */
    public long khoiTaoPipelineContainerMacDinh(Connection conn) throws SQLException {
        String insertPipelineSql = "INSERT INTO pipeline_ban_hang (ma_pipeline, ten_pipeline, mo_ta, mac_dinh, hoat_dong) " +
                "VALUES ('PIPELINE_B2B_STANDARD', 'Chuỗi Pipeline Bán Hàng Chuẩn B2B', " +
                "'Quy trình tiếp cận và chuyển đổi cơ hội doanh nghiệp', 1, 1)";
        try (PreparedStatement ps = conn.prepareStatement(insertPipelineSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 1L;
    }

    /**
     * Khởi tạo pipeline chuẩn B2B và 6 giai đoạn chuẩn nếu database chưa có.
     */
    public synchronized PipelineBanHang khoiTaoPipelineChuan() {
        String insertPipelineSql = "INSERT INTO pipeline_ban_hang (ma_pipeline, ten_pipeline, mo_ta, mac_dinh, hoat_dong) " +
                "VALUES ('PIPELINE_B2B_STANDARD', 'Chuỗi Pipeline Bán Hàng Chuẩn B2B', " +
                "'Quy trình tiếp cận và chuyển đổi cơ hội doanh nghiệp chuẩn 6 bước: Tiếp cận → Xác định nhu cầu → Đề xuất giải pháp → Báo giá → Đàm phán → Chốt', 1, 1)";

        try (Connection conn = DatabaseConfig.getConnection()) {
            long pipelineId = -1;
            try (PreparedStatement ps = conn.prepareStatement(insertPipelineSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        pipelineId = rs.getLong(1);
                    }
                }
            }

            if (pipelineId > 0) {
                // Thêm chuỗi 6 giai đoạn mẫu theo đề bài:
                // Tiếp cận → Xác định nhu cầu → Đề xuất giải pháp → Báo giá → Đàm phán → Chốt
                khoiTaoGiaiDoanMau(conn, pipelineId);

                PipelineBanHang p = new PipelineBanHang(pipelineId, "PIPELINE_B2B_STANDARD",
                        "Chuỗi Pipeline Bán Hàng Chuẩn B2B", "Quy trình bán hàng chuẩn 6 bước");
                p.setMacDinh(true);
                p.setHoatDong(true);
                return p;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Khởi tạo pipeline chuẩn gặp thông báo: " + e.getMessage());
            // Nếu đã tồn tại bản ghi, truy vấn lại để lấy thông tin thực tế
            String findSql = "SELECT id, ma_pipeline, ten_pipeline, mo_ta, mac_dinh, hoat_dong, created_at, updated_at " +
                    "FROM pipeline_ban_hang WHERE ma_pipeline = 'PIPELINE_B2B_STANDARD' LIMIT 1";
            try (Connection conn2 = DatabaseConfig.getConnection();
                 PreparedStatement ps2 = conn2.prepareStatement(findSql);
                 ResultSet rs2 = ps2.executeQuery()) {
                if (rs2.next()) {
                    PipelineBanHang p = new PipelineBanHang();
                    p.setId(rs2.getLong("id"));
                    p.setMaPipeline(rs2.getString("ma_pipeline"));
                    p.setTenPipeline(rs2.getString("ten_pipeline"));
                    p.setMoTa(rs2.getString("mo_ta"));
                    p.setMacDinh(rs2.getBoolean("mac_dinh"));
                    p.setHoatDong(rs2.getBoolean("hoat_dong"));
                    return p;
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Lỗi truy vấn pipeline mặc định fallback: " + ex.getMessage(), ex);
            }
        }

        PipelineBanHang pFallback = new PipelineBanHang(1, "PIPELINE_B2B_STANDARD",
                "Chuỗi Pipeline Bán Hàng Chuẩn B2B", "Mặc định");
        return pFallback;
    }

    private void khoiTaoGiaiDoanMau(Connection conn, long pipelineId) {
        GiaiDoanPipeline[] danhSach = new GiaiDoanPipeline[]{
                taoMauGiaiDoan(pipelineId, "TIEP_CAN", "Tiếp cận", 1, 10, 7, LoaiGiaiDoanEnum.DANG_TIEN_HANH,
                        "Phải có ít nhất 1 cuộc gọi kết nối với đầu mối khách hàng", 0, 1, false, false),
                taoMauGiaiDoan(pipelineId, "XAC_DINH_NHU_CAU", "Xác định nhu cầu", 2, 25, 10, LoaiGiaiDoanEnum.DANG_TIEN_HANH,
                        "Phải có ít nhất một cuộc gặp trực tiếp/online và hoàn thành khảo sát nhu cầu", 1, 1, false, true),
                taoMauGiaiDoan(pipelineId, "DE_XUAT_GIAI_PHAP", "Đề xuất giải pháp", 3, 45, 14, LoaiGiaiDoanEnum.DANG_TIEN_HANH,
                        "Phải tổ chức ít nhất một cuộc gặp demo hoặc trình bày giải pháp kỹ thuật", 1, 0, false, false),
                taoMauGiaiDoan(pipelineId, "BAO_GIA", "Báo giá", 4, 65, 10, LoaiGiaiDoanEnum.DANG_TIEN_HANH,
                        "Bắt buộc phải tạo và gửi Báo giá niêm yết chính thức cho khách hàng", 0, 0, true, false),
                taoMauGiaiDoan(pipelineId, "DAM_PHAN", "Đàm phán", 5, 85, 7, LoaiGiaiDoanEnum.DANG_TIEN_HANH,
                        "Phải có ít nhất 1 cuộc gặp đàm phán hợp đồng thương mại với người quyết định", 1, 0, false, false),
                taoMauGiaiDoan(pipelineId, "CHOT_THANH_CONG", "Chốt thành công", 6, 100, 0, LoaiGiaiDoanEnum.THANH_CONG,
                        "Hợp đồng đã được ký kết và bàn giao triển khai", 0, 0, false, false)
        };

        for (GiaiDoanPipeline gd : danhSach) {
            try {
                themGiaiDoanNoiBo(conn, gd);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Không thể chèn giai đoạn mẫu " + gd.getMaGiaiDoan() + ": " + e.getMessage());
            }
        }
    }

    private GiaiDoanPipeline taoMauGiaiDoan(long pipelineId, String ma, String ten, int thuTu,
                                            int xacSuat, int dinhTre, LoaiGiaiDoanEnum loai,
                                            String dieuKien, int soGap, int soGoi, boolean baoGia, boolean khaoSat) {
        GiaiDoanPipeline gd = new GiaiDoanPipeline();
        gd.setPipelineId(pipelineId);
        gd.setMaGiaiDoan(ma);
        gd.setTenGiaiDoan(ten);
        gd.setThuTu(thuTu);
        gd.setXacSuatThang(xacSuat);
        gd.setSoNgayCanhBaoDinhTre(dinhTre);
        gd.setLoaiGiaiDoan(loai);
        gd.setTrangThai(TrangThaiGiaiDoanEnum.DANG_AP_DUNG);
        gd.setDieuKienBatBuoc(dieuKien);
        gd.setSoCuocGapToiThieu(soGap);
        gd.setSoCuocGoiToiThieu(soGoi);
        gd.setYeuCauBaoGia(baoGia);
        gd.setYeuCauKhaoSatNhuCau(khaoSat);
        return gd;
    }

    /**
     * Lấy toàn bộ danh sách các giai đoạn theo pipelineId, kèm số lượng cơ hội đang chạy.
     * Nếu chưa có pipeline nào trong DB: trả về danh sách rỗng (KHÔNG tự insert).
     */
    public List<GiaiDoanPipeline> layTatCaGiaiDoan() {
        PipelineBanHang p = layPipelineMacDinh();
        if (p == null) {
            return new ArrayList<>();
        }
        return layTatCaGiaiDoanTheoPipeline(p.getId());
    }

    public List<GiaiDoanPipeline> layTatCaGiaiDoanTheoPipeline(long pipelineId) {
        List<GiaiDoanPipeline> danhSach = new ArrayList<>();
        String sql = "SELECT g.id, g.pipeline_id, g.ma_giai_doan, g.ten_giai_doan, g.thu_tu, " +
                "g.xac_suat_mac_dinh, g.so_ngay_dinh_tre, g.loai_ket_thuc, g.hoat_dong, " +
                "g.created_at, g.updated_at, " +
                "COUNT(c.id) AS so_co_hoi " +
                "FROM giai_doan_pipeline g " +
                "LEFT JOIN co_hoi c ON c.giai_doan_id = g.id AND c.trang_thai = 'MO' " +
                "WHERE g.pipeline_id = ? " +
                "GROUP BY g.id, g.pipeline_id, g.ma_giai_doan, g.ten_giai_doan, g.thu_tu, " +
                "g.xac_suat_mac_dinh, g.so_ngay_dinh_tre, g.loai_ket_thuc, g.hoat_dong, " +
                "g.created_at, g.updated_at " +
                "ORDER BY g.thu_tu ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, pipelineId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GiaiDoanPipeline gd = anhXaGiaiDoan(rs);
                    // Nạp điều kiện tương ứng từ dieu_kien_giai_doan
                    napDieuKienGiaiDoan(conn, gd);
                    danhSach.add(gd);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn danh sách giai đoạn pipeline: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Tìm giai đoạn theo ID, kèm theo điều kiện bắt buộc và số cơ hội đang chạy.
     */
    public GiaiDoanPipeline timTheoId(int id) {
        String sql = "SELECT g.id, g.pipeline_id, g.ma_giai_doan, g.ten_giai_doan, g.thu_tu, " +
                "g.xac_suat_mac_dinh, g.so_ngay_dinh_tre, g.loai_ket_thuc, g.hoat_dong, " +
                "g.created_at, g.updated_at, " +
                "COUNT(c.id) AS so_co_hoi " +
                "FROM giai_doan_pipeline g " +
                "LEFT JOIN co_hoi c ON c.giai_doan_id = g.id AND c.trang_thai = 'MO' " +
                "WHERE g.id = ? " +
                "GROUP BY g.id, g.pipeline_id, g.ma_giai_doan, g.ten_giai_doan, g.thu_tu, " +
                "g.xac_suat_mac_dinh, g.so_ngay_dinh_tre, g.loai_ket_thuc, g.hoat_dong, " +
                "g.created_at, g.updated_at";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    GiaiDoanPipeline gd = anhXaGiaiDoan(rs);
                    napDieuKienGiaiDoan(conn, gd);
                    return gd;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm giai đoạn theo ID=" + id + ": " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Kiểm tra mã giai đoạn đã tồn tại chưa trong pipeline.
     */
    public boolean kiemTraMaTonTai(String maGiaiDoan, Integer excludeId) {
        if (maGiaiDoan == null || maGiaiDoan.trim().isEmpty()) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM giai_doan_pipeline WHERE UPPER(ma_giai_doan) = UPPER(?)";
        if (excludeId != null && excludeId > 0) {
            sql += " AND id != ?";
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maGiaiDoan.trim());
            if (excludeId != null && excludeId > 0) {
                ps.setInt(2, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kiểm tra mã giai đoạn: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Đếm số cơ hội và lịch sử đang liên kết với giai đoạn (AC 4: bảo vệ cơ hội đang chạy).
     * Fail-closed: Bắt buộc ném SQLException nếu có lỗi truy vấn DB.
     */
    public int demSoCoHoiTrongGiaiDoan(int giaiDoanId) throws SQLException {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM co_hoi WHERE giai_doan_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, giaiDoanId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    count += rs.getInt(1);
                }
            }
        }

        // Kiểm tra thêm lịch sử giai đoạn cơ hội nếu bảng đã được khởi tạo
        String sqlHistory = "SELECT COUNT(*) FROM lich_su_giai_doan_co_hoi WHERE giai_doan_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlHistory)) {
            ps.setInt(1, giaiDoanId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    count += rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            String state = e.getSQLState();
            int code = e.getErrorCode();
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if ("42S02".equalsIgnoreCase(state) || code == 1146 || code == 42102 || msg.contains("not found")) {
                LOGGER.log(Level.FINE, "Bảng lich_su_giai_doan_co_hoi chưa tồn tại trong môi trường hiện tại: " + e.getMessage());
            } else {
                LOGGER.log(Level.SEVERE, "Lỗi kiểm tra tham chiếu lịch sử: " + e.getMessage(), e);
                throw e;
            }
        }

        return count;
    }

    /**
     * Thêm mới một giai đoạn vào pipeline.
     */
    public int themGiaiDoan(GiaiDoanPipeline gd) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            boolean autoCommitOld = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                int id = themGiaiDoanNoiBo(conn, gd);
                conn.commit();
                return id;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommitOld);
            }
        }
    }

    private int themGiaiDoanNoiBo(Connection conn, GiaiDoanPipeline gd) throws SQLException {
        if (gd.getPipelineId() <= 0) {
            PipelineBanHang p = layPipelineMacDinh();
            if (p != null) {
                gd.setPipelineId(p.getId());
            } else {
                long newPid = khoiTaoPipelineContainerMacDinh(conn);
                gd.setPipelineId(newPid);
            }
        }

        // Tự động tính toán thứ tự tiếp theo nếu thứ tự truyền vào <= 0
        if (gd.getThuTu() <= 0) {
            String maxSql = "SELECT COALESCE(MAX(thu_tu), 0) + 1 FROM giai_doan_pipeline WHERE pipeline_id = ?";
            try (PreparedStatement psMax = conn.prepareStatement(maxSql)) {
                psMax.setLong(1, gd.getPipelineId());
                try (ResultSet rsMax = psMax.executeQuery()) {
                    if (rsMax.next()) {
                        gd.setThuTu(rsMax.getInt(1));
                    }
                }
            }
        } else {
            // Nếu thứ tự đã bị chiếm, dời các giai đoạn từ thuTu đó trở đi lên +1 để nhường chỗ
            giaiPhongThuTu(conn, gd.getPipelineId(), gd.getThuTu());
        }

        String sql = "INSERT INTO giai_doan_pipeline (pipeline_id, ma_giai_doan, ten_giai_doan, thu_tu, " +
                "xac_suat_mac_dinh, so_ngay_dinh_tre, loai_ket_thuc, hoat_dong) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, gd.getPipelineId());
            ps.setString(2, gd.getMaGiaiDoan().trim());
            ps.setString(3, gd.getTenGiaiDoan().trim());
            ps.setInt(4, gd.getThuTu());
            ps.setBigDecimal(5, new BigDecimal(gd.getXacSuatThang()));
            if (gd.getSoNgayCanhBaoDinhTre() > 0) {
                ps.setInt(6, gd.getSoNgayCanhBaoDinhTre());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            if (gd.getLoaiGiaiDoan() != null && gd.getLoaiGiaiDoan().getMaDb() != null) {
                ps.setString(7, gd.getLoaiGiaiDoan().getMaDb());
            } else {
                ps.setNull(7, Types.VARCHAR);
            }
            ps.setInt(8, gd.getTrangThai() != null ? gd.getTrangThai().getGiaTriDb() : 1);

            ps.executeUpdate();
            try (ResultSet rsKey = ps.getGeneratedKeys()) {
                if (rsKey.next()) {
                    int genId = rsKey.getInt(1);
                    gd.setId(genId);

                    // Lưu các điều kiện bắt buộc rời giai đoạn (AC 3)
                    luuDieuKienGiaiDoan(conn, gd);
                    return genId;
                }
            }
        }
        return -1;
    }

    /**
     * Dời các giai đoạn có thứ tự >= targetThuTu lên +1 theo thứ tự giảm dần để giải phóng vị trí.
     */
    private void giaiPhongThuTu(Connection conn, long pipelineId, int targetThuTu) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM giai_doan_pipeline WHERE pipeline_id = ? AND thu_tu = ?";
        try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
            psCheck.setLong(1, pipelineId);
            psCheck.setInt(2, targetThuTu);
            try (ResultSet rsCheck = psCheck.executeQuery()) {
                if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                    String selectSql = "SELECT id, thu_tu FROM giai_doan_pipeline WHERE pipeline_id = ? AND thu_tu >= ? ORDER BY thu_tu DESC";
                    List<int[]> itemsToShift = new ArrayList<>();
                    try (PreparedStatement psSel = conn.prepareStatement(selectSql)) {
                        psSel.setLong(1, pipelineId);
                        psSel.setInt(2, targetThuTu);
                        try (ResultSet rsSel = psSel.executeQuery()) {
                            while (rsSel.next()) {
                                itemsToShift.add(new int[]{rsSel.getInt("id"), rsSel.getInt("thu_tu")});
                            }
                        }
                    }

                    String updateSql = "UPDATE giai_doan_pipeline SET thu_tu = ? WHERE id = ?";
                    try (PreparedStatement psUpd = conn.prepareStatement(updateSql)) {
                        for (int[] item : itemsToShift) {
                            psUpd.setInt(1, item[1] + 1);
                            psUpd.setInt(2, item[0]);
                            psUpd.executeUpdate();
                        }
                    }
                }
            }
        }
    }

    /**
     * Cập nhật thông tin giai đoạn pipeline.
     * AC 4: Thay đổi cấu hình không làm hỏng cơ hội đang chạy (cơ hội vẫn liên kết theo ID).
     */
    public boolean capNhatGiaiDoan(GiaiDoanPipeline gd) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            boolean autoCommitOld = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                // Kiểm tra thứ tự cũ và đồng bộ nếu người dùng đổi vị trí
                int oldThuTu = -1;
                long pipelineId = gd.getPipelineId();
                String selectOldSql = "SELECT pipeline_id, thu_tu FROM giai_doan_pipeline WHERE id = ?";
                try (PreparedStatement psOld = conn.prepareStatement(selectOldSql)) {
                    psOld.setInt(1, gd.getId());
                    try (ResultSet rsOld = psOld.executeQuery()) {
                        if (rsOld.next()) {
                            pipelineId = rsOld.getLong("pipeline_id");
                            oldThuTu = rsOld.getInt("thu_tu");
                            if (gd.getPipelineId() <= 0) {
                                gd.setPipelineId(pipelineId);
                            }
                        }
                    }
                }

                if (oldThuTu > 0 && oldThuTu != gd.getThuTu()) {
                    dieuChinhThuTuCapNhat(conn, pipelineId, gd.getId(), oldThuTu, gd.getThuTu());
                }

                String sql = "UPDATE giai_doan_pipeline SET ma_giai_doan = ?, ten_giai_doan = ?, thu_tu = ?, " +
                        "xac_suat_mac_dinh = ?, so_ngay_dinh_tre = ?, loai_ket_thuc = ?, hoat_dong = ? WHERE id = ?";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, gd.getMaGiaiDoan().trim());
                    ps.setString(2, gd.getTenGiaiDoan().trim());
                    ps.setInt(3, gd.getThuTu());
                    ps.setBigDecimal(4, new BigDecimal(gd.getXacSuatThang()));
                    if (gd.getSoNgayCanhBaoDinhTre() > 0) {
                        ps.setInt(5, gd.getSoNgayCanhBaoDinhTre());
                    } else {
                        ps.setNull(5, Types.INTEGER);
                    }
                    if (gd.getLoaiGiaiDoan() != null && gd.getLoaiGiaiDoan().getMaDb() != null) {
                        ps.setString(6, gd.getLoaiGiaiDoan().getMaDb());
                    } else {
                        ps.setNull(6, Types.VARCHAR);
                    }
                    ps.setInt(7, gd.getTrangThai() != null ? gd.getTrangThai().getGiaTriDb() : 1);
                    ps.setInt(8, gd.getId());

                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        // Cập nhật lại các điều kiện tương ứng trong bảng dieu_kien_giai_doan
                        xoaDieuKienGiaiDoan(conn, gd.getId());
                        luuDieuKienGiaiDoan(conn, gd);
                        conn.commit();
                        return true;
                    }
                }
                conn.commit();
                return false;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommitOld);
            }
        }
    }

    private void dieuChinhThuTuCapNhat(Connection conn, long pipelineId, int stageId, int oldThuTu, int newThuTu) throws SQLException {
        // Gán tạm thời vị trí âm để giải phóng slot cũ
        String tempSql = "UPDATE giai_doan_pipeline SET thu_tu = ? WHERE id = ?";
        try (PreparedStatement psTemp = conn.prepareStatement(tempSql)) {
            psTemp.setInt(1, -100000 - stageId);
            psTemp.setInt(2, stageId);
            psTemp.executeUpdate();
        }

        if (newThuTu < oldThuTu) {
            // Di chuyển lên: các phần tử trong khoảng [newThuTu, oldThuTu - 1] dịch chuyển +1 (giảm dần)
            String selSql = "SELECT id, thu_tu FROM giai_doan_pipeline WHERE pipeline_id = ? AND thu_tu >= ? AND thu_tu < ? ORDER BY thu_tu DESC";
            List<int[]> items = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(selSql)) {
                ps.setLong(1, pipelineId);
                ps.setInt(2, newThuTu);
                ps.setInt(3, oldThuTu);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        items.add(new int[]{rs.getInt("id"), rs.getInt("thu_tu")});
                    }
                }
            }
            String updSql = "UPDATE giai_doan_pipeline SET thu_tu = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updSql)) {
                for (int[] item : items) {
                    ps.setInt(1, item[1] + 1);
                    ps.setInt(2, item[0]);
                    ps.executeUpdate();
                }
            }
        } else {
            // Di chuyển xuống: các phần tử trong khoảng [oldThuTu + 1, newThuTu] dịch chuyển -1 (tăng dần)
            String selSql = "SELECT id, thu_tu FROM giai_doan_pipeline WHERE pipeline_id = ? AND thu_tu > ? AND thu_tu <= ? ORDER BY thu_tu ASC";
            List<int[]> items = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(selSql)) {
                ps.setLong(1, pipelineId);
                ps.setInt(2, oldThuTu);
                ps.setInt(3, newThuTu);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        items.add(new int[]{rs.getInt("id"), rs.getInt("thu_tu")});
                    }
                }
            }
            String updSql = "UPDATE giai_doan_pipeline SET thu_tu = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updSql)) {
                for (int[] item : items) {
                    ps.setInt(1, item[1] - 1);
                    ps.setInt(2, item[0]);
                    ps.executeUpdate();
                }
            }
        }
    }

    /**
     * Đổi thứ tự 2 giai đoạn liền kề (Move Up / Move Down) trong transaction an toàn.
     */
    public boolean hoanDoiThuTu(int id1, int thuTu1, int id2, int thuTu2) throws SQLException {
        String sql = "UPDATE giai_doan_pipeline SET thu_tu = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            boolean autoCommitOld = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                // Tạm thời gán id1 sang thứ tự âm duy nhất để tránh trùng UK uk_gdp_tt
                ps.setInt(1, -100000 - Math.abs(id1));
                ps.setInt(2, id1);
                ps.executeUpdate();

                // Cập nhật id2 sang thuTu1
                ps.setInt(1, thuTu1);
                ps.setInt(2, id2);
                ps.executeUpdate();

                // Cập nhật id1 sang thuTu2
                ps.setInt(1, thuTu2);
                ps.setInt(2, id1);
                ps.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommitOld);
            }
        }
    }

    /**
     * Chuyển trạng thái giai đoạn (Đang áp dụng <-> Ngừng áp dụng).
     */
    public boolean capNhatTrangThai(int id, TrangThaiGiaiDoanEnum trangThaiMoi) throws SQLException {
        String sql = "UPDATE giai_doan_pipeline SET hoat_dong = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, trangThaiMoi.getGiaTriDb());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa giai đoạn (AC 4: chỉ được gọi khi không có bất kỳ cơ hội hoặc lịch sử nào đang liên kết).
     */
    public boolean xoaGiaiDoan(int id) throws SQLException {
        // Kiểm tra an toàn trước khi xóa (Fail-closed)
        int soCoHoi = demSoCoHoiTrongGiaiDoan(id);
        if (soCoHoi > 0) {
            throw new IllegalStateException("Không thể xóa giai đoạn đang có " + soCoHoi + " cơ hội hoặc lịch sử tham chiếu!");
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            boolean autoCommitOld = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                // Xóa điều kiện trước
                xoaDieuKienGiaiDoan(conn, id);

                String sql = "DELETE FROM giai_doan_pipeline WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    int r = ps.executeUpdate();
                    conn.commit();
                    return r > 0;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommitOld);
            }
        }
    }

    /**
     * AC 2 & AC 4: Tính toán tổng hợp số liệu dự báo doanh số theo từng giai đoạn và toàn pipeline.
     * Bảo toàn thương vụ đang chạy: Các giai đoạn NGUNG_AP_DUNG nhưng vẫn có cơ hội mở sẽ được giữ lại trong dự báo.
     */
    public List<DuBaoDoanhSoDTO> layThongKeDuBaoPipeline() {
        List<DuBaoDoanhSoDTO> danhSach = new ArrayList<>();
        PipelineBanHang p = layPipelineMacDinh();
        if (p == null) {
            return danhSach;
        }

        String sql = "SELECT g.id, g.ten_giai_doan, g.xac_suat_mac_dinh, g.hoat_dong, " +
                "COALESCE(SUM(c.gia_tri_du_kien), 0) AS tong_gia_tri, " +
                "COUNT(c.id) AS so_co_hoi " +
                "FROM giai_doan_pipeline g " +
                "LEFT JOIN co_hoi c ON c.giai_doan_id = g.id AND c.trang_thai = 'MO' " +
                "WHERE g.pipeline_id = ? " +
                "GROUP BY g.id, g.ten_giai_doan, g.xac_suat_mac_dinh, g.hoat_dong, g.thu_tu " +
                "HAVING g.hoat_dong = 1 OR COUNT(c.id) > 0 " +
                "ORDER BY g.thu_tu ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, p.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int gid = rs.getInt("id");
                    String ten = rs.getString("ten_giai_doan");
                    BigDecimal xsBd = rs.getBigDecimal("xac_suat_mac_dinh");
                    int xacSuat = xsBd != null ? xsBd.intValue() : 0;
                    BigDecimal tongGiaTri = rs.getBigDecimal("tong_gia_tri");
                    int soCoHoi = rs.getInt("so_co_hoi");

                    danhSach.add(new DuBaoDoanhSoDTO(gid, ten, xacSuat, tongGiaTri, soCoHoi));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn thống kê dự báo doanh số pipeline: " + e.getMessage(), e);
        }
        return danhSach;
    }

    // --- Quản lý điều kiện rời giai đoạn (bảng dieu_kien_giai_doan) ---

    private void napDieuKienGiaiDoan(Connection conn, GiaiDoanPipeline gd) {
        String sql = "SELECT id, giai_doan_nguon_id, giai_doan_dich_id, ma_dieu_kien, ten_dieu_kien, " +
                "loai_dieu_kien, cau_hinh_json, thong_bao_thieu, thu_tu, hoat_dong " +
                "FROM dieu_kien_giai_doan WHERE giai_doan_nguon_id = ? AND hoat_dong = 1 ORDER BY thu_tu ASC";

        List<DieuKienGiaiDoan> list = new ArrayList<>();
        List<String> textConditions = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gd.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DieuKienGiaiDoan dk = new DieuKienGiaiDoan();
                    dk.setId(rs.getLong("id"));
                    dk.setGiaiDoanNguonId(rs.getLong("giai_doan_nguon_id"));
                    dk.setMaDieuKien(rs.getString("ma_dieu_kien"));
                    dk.setTenDieuKien(rs.getString("ten_dieu_kien"));
                    dk.setLoaiDieuKien(rs.getString("loai_dieu_kien"));
                    dk.setCauHinhJson(rs.getString("cau_hinh_json"));
                    dk.setThongBaoThieu(rs.getString("thong_bao_thieu"));
                    dk.setThuTu(rs.getInt("thu_tu"));
                    dk.setHoatDong(rs.getBoolean("hoat_dong"));

                    list.add(dk);
                    textConditions.add(dk.getTenDieuKien());

                    // Trích xuất cấu hình nhanh vào model
                    String loai = dk.getLoaiDieuKien();
                    if ("CUOC_GAP".equalsIgnoreCase(loai)) {
                        int gap = trichXuatSoNguyen(dk.getCauHinhJson(), 1);
                        gd.setSoCuocGapToiThieu(gap);
                    } else if ("CUOC_GOI".equalsIgnoreCase(loai)) {
                        int goi = trichXuatSoNguyen(dk.getCauHinhJson(), 1);
                        gd.setSoCuocGoiToiThieu(goi);
                    } else if ("BAO_GIA".equalsIgnoreCase(loai)) {
                        gd.setYeuCauBaoGia(true);
                    } else if ("KHAO_SAT".equalsIgnoreCase(loai)) {
                        gd.setYeuCauKhaoSatNhuCau(true);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Không thể đọc điều kiện cho giai đoạn ID=" + gd.getId() + ": " + e.getMessage());
        }

        gd.setDanhSachDieuKien(list);
        if (!textConditions.isEmpty()) {
            gd.setDieuKienBatBuoc(String.join("; ", textConditions));
        }
    }

    private void luuDieuKienGiaiDoan(Connection conn, GiaiDoanPipeline gd) throws SQLException {
        String insertSql = "INSERT INTO dieu_kien_giai_doan (giai_doan_nguon_id, ma_dieu_kien, ten_dieu_kien, " +
                "loai_dieu_kien, cau_hinh_json, thong_bao_thieu, thu_tu, hoat_dong) VALUES (?, ?, ?, ?, ?, ?, ?, 1)";

        int thuTu = 1;
        // 1. Điều kiện cuộc gặp
        if (gd.getSoCuocGapToiThieu() > 0) {
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, gd.getId());
                ps.setString(2, "DK_GAP_" + gd.getId());
                ps.setString(3, "Tối thiểu " + gd.getSoCuocGapToiThieu() + " cuộc gặp trực tiếp với khách");
                ps.setString(4, "CUOC_GAP");
                ps.setString(5, "{\"so_cuoc_gap_toi_thieu\": " + gd.getSoCuocGapToiThieu() + "}");
                ps.setString(6, "Cần có ít nhất " + gd.getSoCuocGapToiThieu() + " cuộc gặp trước khi chuyển giai đoạn");
                ps.setInt(7, thuTu++);
                ps.executeUpdate();
            }
        }

        // 2. Điều kiện cuộc gọi
        if (gd.getSoCuocGoiToiThieu() > 0) {
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, gd.getId());
                ps.setString(2, "DK_GOI_" + gd.getId());
                ps.setString(3, "Tối thiểu " + gd.getSoCuocGoiToiThieu() + " cuộc gọi trao đổi");
                ps.setString(4, "CUOC_GOI");
                ps.setString(5, "{\"so_cuoc_goi_toi_thieu\": " + gd.getSoCuocGoiToiThieu() + "}");
                ps.setString(6, "Cần có ít nhất " + gd.getSoCuocGoiToiThieu() + " cuộc gọi trước khi chuyển giai đoạn");
                ps.setInt(7, thuTu++);
                ps.executeUpdate();
            }
        }

        // 3. Yêu cầu báo giá
        if (gd.isYeuCauBaoGia()) {
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, gd.getId());
                ps.setString(2, "DK_BAO_GIA_" + gd.getId());
                ps.setString(3, "Bắt buộc tạo và gửi Báo giá niêm yết");
                ps.setString(4, "BAO_GIA");
                ps.setString(5, "{\"yeu_cau_bao_gia\": true}");
                ps.setString(6, "Phải phát hành báo giá chính thức cho khách hàng");
                ps.setInt(7, thuTu++);
                ps.executeUpdate();
            }
        }

        // 4. Yêu cầu khảo sát
        if (gd.isYeuCauKhaoSatNhuCau()) {
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, gd.getId());
                ps.setString(2, "DK_KHAO_SAT_" + gd.getId());
                ps.setString(3, "Hoàn tất xác nhận bảng khảo sát nhu cầu");
                ps.setString(4, "KHAO_SAT");
                ps.setString(5, "{\"yeu_cau_khao_sat\": true}");
                ps.setString(6, "Phải khảo sát và xác nhận đầy đủ nhu cầu của khách hàng");
                ps.setInt(7, thuTu++);
                ps.executeUpdate();
            }
        }

        // 5. Điều kiện văn bản tuỳ chỉnh nếu có
        if (gd.getDieuKienBatBuoc() != null && !gd.getDieuKienBatBuoc().trim().isEmpty() &&
                gd.getSoCuocGapToiThieu() == 0 && gd.getSoCuocGoiToiThieu() == 0 &&
                !gd.isYeuCauBaoGia() && !gd.isYeuCauKhaoSatNhuCau()) {
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, gd.getId());
                ps.setString(2, "DK_QUY_DINH_" + gd.getId());
                ps.setString(3, gd.getDieuKienBatBuoc().trim());
                ps.setString(4, "TUY_CHINH");
                ps.setString(5, "{}");
                ps.setString(6, "Chưa đáp ứng: " + gd.getDieuKienBatBuoc().trim());
                ps.setInt(7, thuTu);
                ps.executeUpdate();
            }
        }
    }

    private void xoaDieuKienGiaiDoan(Connection conn, int giaiDoanId) throws SQLException {
        String sql = "DELETE FROM dieu_kien_giai_doan WHERE giai_doan_nguon_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, giaiDoanId);
            ps.executeUpdate();
        }
    }

    private int trichXuatSoNguyen(String json, int giaTriMacDinh) {
        if (json == null || json.isBlank()) return giaTriMacDinh;
        try {
            String digits = json.replaceAll("[^0-9]", "");
            if (!digits.isEmpty()) {
                return Integer.parseInt(digits);
            }
        } catch (Exception ignored) {
        }
        return giaTriMacDinh;
    }

    private GiaiDoanPipeline anhXaGiaiDoan(ResultSet rs) throws SQLException {
        GiaiDoanPipeline gd = new GiaiDoanPipeline();
        gd.setId(rs.getInt("id"));
        gd.setPipelineId(rs.getLong("pipeline_id"));
        gd.setMaGiaiDoan(rs.getString("ma_giai_doan"));
        gd.setTenGiaiDoan(rs.getString("ten_giai_doan"));
        gd.setThuTu(rs.getInt("thu_tu"));

        BigDecimal xsBd = rs.getBigDecimal("xac_suat_mac_dinh");
        gd.setXacSuatThang(xsBd != null ? xsBd.intValue() : 0);

        int dinhTre = rs.getInt("so_ngay_dinh_tre");
        gd.setSoNgayCanhBaoDinhTre(rs.wasNull() ? 0 : dinhTre);

        String loaiKetThuc = rs.getString("loai_ket_thuc");
        gd.setLoaiGiaiDoan(LoaiGiaiDoanEnum.tuMa(loaiKetThuc));

        int hoatDong = rs.getInt("hoat_dong");
        gd.setTrangThai(TrangThaiGiaiDoanEnum.tuGiaTriDb(hoatDong));

        gd.setCreatedAt(rs.getTimestamp("created_at"));
        gd.setUpdatedAt(rs.getTimestamp("updated_at"));

        try {
            gd.setSoCoHoiHienTai(rs.getInt("so_co_hoi"));
        } catch (SQLException ignored) {
        }

        return gd;
    }
}
