package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.MucQuyen;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.VaiTroModule;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object phụ trách bảng vai_tro_module và liên kết với vai_tro, module_he_thong.
 */
public class VaiTroModuleDAO {

    private static final Logger LOGGER = Logger.getLogger(VaiTroModuleDAO.class.getName());

    public VaiTroModuleDAO() {
    }

    /**
     * Lấy toàn bộ ma trận phân quyền của hệ thống (7 vai trò x 10 module).
     */
    public List<VaiTroModule> layTatCa() {
        List<VaiTroModule> ds = new ArrayList<>();
        String sql = "SELECT vtm.id, vtm.vai_tro_id, vt.ma_vai_tro, vt.ten_vai_tro, vt.pham_vi_toi_da, " +
                     "       vtm.module_id, mh.ma_module, mh.ten_module, mh.mo_ta, mh.thu_tu_hien_thi, " +
                     "       vtm.muc_quyen, vtm.pham_vi_du_lieu, vtm.created_at " +
                     "FROM vai_tro_module vtm " +
                     "JOIN vai_tro vt ON vtm.vai_tro_id = vt.id " +
                     "JOIN module_he_thong mh ON vtm.module_id = mh.id " +
                     "ORDER BY vt.id ASC, mh.thu_tu_hien_thi ASC";

        try (Connection conn = DatabaseConnection.layKetNoi()) {
            damBaoSchemaTonTai(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn toàn bộ ma trận quyền: " + e.getMessage(), e);
        }
        return ds;
    }

    /**
     * Lấy ma trận phân quyền của một vai trò theo ID vai trò.
     */
    public List<VaiTroModule> layTheoVaiTroId(int vaiTroId) {
        List<VaiTroModule> ds = new ArrayList<>();
        String sql = "SELECT vtm.id, vtm.vai_tro_id, vt.ma_vai_tro, vt.ten_vai_tro, vt.pham_vi_toi_da, " +
                     "       vtm.module_id, mh.ma_module, mh.ten_module, mh.mo_ta, mh.thu_tu_hien_thi, " +
                     "       vtm.muc_quyen, vtm.pham_vi_du_lieu, vtm.created_at " +
                     "FROM vai_tro_module vtm " +
                     "JOIN vai_tro vt ON vtm.vai_tro_id = vt.id " +
                     "JOIN module_he_thong mh ON vtm.module_id = mh.id " +
                     "WHERE vtm.vai_tro_id = ? " +
                     "ORDER BY mh.thu_tu_hien_thi ASC";

        try (Connection conn = DatabaseConnection.layKetNoi()) {
            damBaoSchemaTonTai(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, vaiTroId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ds.add(mapResultSet(rs));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn quyền theo vaiTroId=" + vaiTroId + ": " + e.getMessage(), e);
        }
        return ds;
    }

    /**
     * Lấy ma trận phân quyền của một vai trò theo mã vai trò (vd: SALES_REP).
     */
    public List<VaiTroModule> layTheoMaVaiTro(String maVaiTro) {
        List<VaiTroModule> ds = new ArrayList<>();
        if (maVaiTro == null || maVaiTro.isBlank()) {
            return ds;
        }

        String sql = "SELECT vtm.id, vtm.vai_tro_id, vt.ma_vai_tro, vt.ten_vai_tro, vt.pham_vi_toi_da, " +
                     "       vtm.module_id, mh.ma_module, mh.ten_module, mh.mo_ta, mh.thu_tu_hien_thi, " +
                     "       vtm.muc_quyen, vtm.pham_vi_du_lieu, vtm.created_at " +
                     "FROM vai_tro_module vtm " +
                     "JOIN vai_tro vt ON vtm.vai_tro_id = vt.id " +
                     "JOIN module_he_thong mh ON vtm.module_id = mh.id " +
                     "WHERE UPPER(vt.ma_vai_tro) = UPPER(?) " +
                     "ORDER BY mh.thu_tu_hien_thi ASC";

        try (Connection conn = DatabaseConnection.layKetNoi()) {
            damBaoSchemaTonTai(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maVaiTro.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ds.add(mapResultSet(rs));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn quyền theo maVaiTro=" + maVaiTro + ": " + e.getMessage(), e);
        }
        return ds;
    }

    /**
     * Lấy quyền cụ thể của một vai trò trên một module theo mã.
     */
    public VaiTroModule layQuyen(String maVaiTro, String maCanonicalModule) {
        if (maVaiTro == null || maCanonicalModule == null) {
            return null;
        }

        String sql = "SELECT vtm.id, vtm.vai_tro_id, vt.ma_vai_tro, vt.ten_vai_tro, vt.pham_vi_toi_da, " +
                     "       vtm.module_id, mh.ma_module, mh.ten_module, mh.mo_ta, mh.thu_tu_hien_thi, " +
                     "       vtm.muc_quyen, vtm.pham_vi_du_lieu, vtm.created_at " +
                     "FROM vai_tro_module vtm " +
                     "JOIN vai_tro vt ON vtm.vai_tro_id = vt.id " +
                     "JOIN module_he_thong mh ON vtm.module_id = mh.id " +
                     "WHERE UPPER(vt.ma_vai_tro) = UPPER(?) AND UPPER(mh.ma_module) = UPPER(?)";

        try (Connection conn = DatabaseConnection.layKetNoi()) {
            damBaoSchemaTonTai(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maVaiTro.trim());
                ps.setString(2, maCanonicalModule.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSet(rs);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn quyền " + maVaiTro + " - " + maCanonicalModule + ": " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Cập nhật một dòng phân quyền trong Connection (hỗ trợ transaction).
     */
    public boolean capNhatQuyen(int vaiTroId, int moduleId, MucQuyen mucQuyen, PhamViDuLieu phamViDuLieu, Connection conn) throws SQLException {
        String sql = "UPDATE vai_tro_module SET muc_quyen = ?, pham_vi_du_lieu = ? WHERE vai_tro_id = ? AND module_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mucQuyen != null ? mucQuyen.getMa() : MucQuyen.NONE.getMa());
            if (phamViDuLieu != null) {
                ps.setString(2, phamViDuLieu.getMa());
            } else {
                ps.setNull(2, java.sql.Types.VARCHAR);
            }
            ps.setInt(3, vaiTroId);
            ps.setInt(4, moduleId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cập nhật toàn bộ các module của một vai trò trong một transaction duy nhất.
     */
    public boolean capNhatToanBoChoVaiTro(int vaiTroId, List<VaiTroModule> dsQuyenMoi) {
        if (dsQuyenMoi == null || dsQuyenMoi.isEmpty()) {
            return false;
        }

        try (Connection conn = DatabaseConnection.layKetNoi()) {
            damBaoSchemaTonTai(conn);
            conn.setAutoCommit(false);
            try {
                for (VaiTroModule vtm : dsQuyenMoi) {
                    capNhatQuyen(vaiTroId, vtm.getModuleId(), vtm.getMucQuyen(), vtm.getPhamViDuLieu(), conn);
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Lỗi rollback transaction: " + ex.getMessage(), ex);
                }
                LOGGER.log(Level.SEVERE, "Lỗi cập nhật ma trận quyền cho vaiTroId=" + vaiTroId + ": " + e.getMessage(), e);
                return false;
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi mở kết nối CSDL: " + e.getMessage(), e);
            return false;
        }
    }

    private VaiTroModule mapResultSet(ResultSet rs) throws SQLException {
        VaiTroModule vtm = new VaiTroModule();
        vtm.setId(rs.getLong("id"));
        vtm.setVaiTroId(rs.getInt("vai_tro_id"));
        vtm.setMaVaiTro(rs.getString("ma_vai_tro"));
        vtm.setTenVaiTro(rs.getString("ten_vai_tro"));
        vtm.setPhamViToiDaVaiTro(PhamViDuLieu.tuMa(rs.getString("pham_vi_toi_da")));

        vtm.setModuleId(rs.getInt("module_id"));
        vtm.setMaModule(rs.getString("ma_module"));
        vtm.setTenModule(rs.getString("ten_module"));
        vtm.setMoTaModule(rs.getString("mo_ta"));
        vtm.setThuTuHienThi(rs.getInt("thu_tu_hien_thi"));

        vtm.setMucQuyen(MucQuyen.tuMa(rs.getString("muc_quyen")));
        String pv = rs.getString("pham_vi_du_lieu");
        vtm.setPhamViDuLieu(pv != null && !pv.isBlank() ? PhamViDuLieu.tuMa(pv) : null);
        try {
            vtm.setCreatedAt(rs.getTimestamp("created_at"));
        } catch (SQLException ignored) {
        }
        return vtm;
    }

    /**
     * Đảm bảo các bảng vai_tro, module_he_thong, vai_tro_module tồn tại và có dữ liệu seed.
     * Khi chạy trong môi trường test H2 in-memory sạch, phương thức này tự động nạp bảng và seed chuẩn.
     */
    private synchronized void damBaoSchemaTonTai(Connection conn) {
        try (Statement st = conn.createStatement()) {
            // Kiểm tra xem bảng vai_tro_module đã tồn tại chưa
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM vai_tro_module")) {
                if (rs.next()) {
                    long count = rs.getLong(1);
                    if (count > 0) {
                        return; // Bảng đã có dữ liệu seed đầy đủ
                    }
                }
            } catch (SQLException ignored) {
                // Bảng chưa tồn tại -> tiến hành tạo schema và nạp seed
            }

            st.execute("CREATE TABLE IF NOT EXISTS vai_tro (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_vai_tro VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_vai_tro VARCHAR(100) NOT NULL, " +
                    "mo_ta VARCHAR(500) NULL, " +
                    "pham_vi_toi_da VARCHAR(50) NOT NULL DEFAULT 'CA_NHAN', " +
                    "hoat_dong TINYINT NOT NULL DEFAULT 1, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS module_he_thong (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_module VARCHAR(60) NOT NULL UNIQUE, " +
                    "ten_module VARCHAR(150) NOT NULL, " +
                    "mo_ta VARCHAR(500) NULL, " +
                    "thu_tu_hien_thi INT NOT NULL DEFAULT 0, " +
                    "hien_thi_menu TINYINT NOT NULL DEFAULT 1, " +
                    "hoat_dong TINYINT NOT NULL DEFAULT 1" +
                    ")");

            st.execute("CREATE TABLE IF NOT EXISTS vai_tro_module (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "vai_tro_id BIGINT NOT NULL, " +
                    "module_id BIGINT NOT NULL, " +
                    "muc_quyen VARCHAR(20) NOT NULL, " +
                    "pham_vi_du_lieu VARCHAR(20) NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // Nạp 7 vai trò chuẩn
            st.execute("MERGE INTO vai_tro (id, ma_vai_tro, ten_vai_tro, mo_ta, pham_vi_toi_da) KEY(id) VALUES " +
                    "(1, 'ADMIN', 'Quản trị hệ thống', 'Quản trị tài khoản, quyền, cấu hình và nhật ký.', 'TOAN_BO'), " +
                    "(2, 'DIRECTOR', 'Giám đốc kinh doanh', 'Xem và quản lý toàn bộ dữ liệu kinh doanh.', 'TOAN_BO'), " +
                    "(3, 'TEAM_LEAD', 'Trưởng nhóm kinh doanh', 'Theo dõi và quản lý dữ liệu của nhóm.', 'NHOM'), " +
                    "(4, 'SALES_REP', 'Nhân viên kinh doanh', 'Quản lý khách và cơ hội mình phụ trách.', 'CA_NHAN'), " +
                    "(5, 'MARKETING', 'Nhân viên Marketing', 'Quản lý chiến dịch và lead.', 'CA_NHAN'), " +
                    "(6, 'CUST_SUCCESS', 'Chăm sóc khách hàng', 'Theo dõi lịch sử sau bán và rủi ro khách hàng.', 'CA_NHAN'), " +
                    "(7, 'ACCOUNTANT', 'Kế toán', 'Theo dõi báo giá, hợp đồng và công nợ.', 'TOAN_BO')");

            // Nạp 10 module canonical chuẩn từ DB
            st.execute("MERGE INTO module_he_thong (id, ma_module, ten_module, thu_tu_hien_thi, hien_thi_menu) KEY(id) VALUES " +
                    "(1, 'DANH_MUC', 'Danh mục & cấu hình bán hàng', 10, 1), " +
                    "(2, 'KHACH_HANG', 'Khách hàng & liên hệ', 20, 1), " +
                    "(3, 'LEAD', 'Lead & phân bổ', 30, 1), " +
                    "(4, 'CO_HOI', 'Cơ hội & pipeline', 40, 1), " +
                    "(5, 'HOAT_DONG', 'Hoạt động & lịch làm việc', 50, 1), " +
                    "(6, 'BAO_GIA_HOP_DONG', 'Báo giá & hợp đồng', 60, 1), " +
                    "(7, 'KPI', 'Chỉ tiêu & KPI', 70, 1), " +
                    "(8, 'BAO_CAO', 'Báo cáo & dashboard', 80, 1), " +
                    "(9, 'TU_DONG_HOA', 'Tự động hoá & thông báo', 90, 1), " +
                    "(10, 'NGUOI_DUNG_NHAT_KY', 'Người dùng & nhật ký', 100, 1)");

            // Nạp seed vai_tro_module chuẩn theo 001_crm_ban_hang_full_schema_8_sprints.sql
            st.execute("INSERT INTO vai_tro_module (vai_tro_id, module_id, muc_quyen, pham_vi_du_lieu) VALUES " +
                    // Admin (id=1)
                    "(1,1,'FULL','TOAN_BO'),(1,2,'FULL','TOAN_BO'),(1,3,'FULL','TOAN_BO'),(1,4,'FULL','TOAN_BO')," +
                    "(1,5,'FULL','TOAN_BO'),(1,6,'FULL','TOAN_BO'),(1,7,'FULL','TOAN_BO'),(1,8,'FULL','TOAN_BO')," +
                    "(1,9,'FULL','TOAN_BO'),(1,10,'FULL','TOAN_BO')," +
                    // Director (id=2)
                    "(2,1,'FULL','TOAN_BO'),(2,2,'FULL','TOAN_BO'),(2,3,'FULL','TOAN_BO'),(2,4,'FULL','TOAN_BO')," +
                    "(2,5,'FULL','TOAN_BO'),(2,6,'FULL','TOAN_BO'),(2,7,'FULL','TOAN_BO'),(2,8,'FULL','TOAN_BO')," +
                    "(2,9,'FULL','TOAN_BO'),(2,10,'READ','TOAN_BO')," +
                    // Team Lead (id=3)
                    "(3,1,'READ',NULL),(3,2,'FULL','NHOM'),(3,3,'FULL','NHOM'),(3,4,'FULL','NHOM')," +
                    "(3,5,'FULL','NHOM'),(3,6,'WRITE','NHOM'),(3,7,'WRITE','NHOM'),(3,8,'READ','NHOM')," +
                    "(3,9,'READ','NHOM'),(3,10,'NONE',NULL)," +
                    // Sales Rep (id=4)
                    "(4,1,'READ',NULL),(4,2,'WRITE','CA_NHAN'),(4,3,'WRITE','CA_NHAN'),(4,4,'WRITE','CA_NHAN')," +
                    "(4,5,'WRITE','CA_NHAN'),(4,6,'WRITE','CA_NHAN'),(4,7,'READ','CA_NHAN'),(4,8,'READ','CA_NHAN')," +
                    "(4,9,'READ',NULL),(4,10,'NONE',NULL)," +
                    // Marketing (id=5)
                    "(5,1,'READ',NULL),(5,2,'WRITE','TOAN_BO'),(5,3,'FULL','TOAN_BO'),(5,4,'READ','TOAN_BO')," +
                    "(5,5,'WRITE','TOAN_BO'),(5,6,'NONE',NULL),(5,7,'NONE',NULL),(5,8,'READ','CA_NHAN')," +
                    "(5,9,'WRITE','TOAN_BO'),(5,10,'NONE',NULL)," +
                    // Customer Success (id=6)
                    "(6,1,'READ',NULL),(6,2,'WRITE','CA_NHAN'),(6,3,'NONE',NULL),(6,4,'READ','CA_NHAN')," +
                    "(6,5,'WRITE','CA_NHAN'),(6,6,'READ','CA_NHAN'),(6,7,'NONE',NULL),(6,8,'READ','CA_NHAN')," +
                    "(6,9,'READ',NULL),(6,10,'NONE',NULL)," +
                    // Accountant (id=7)
                    "(7,1,'READ',NULL),(7,2,'READ','TOAN_BO'),(7,3,'NONE',NULL),(7,4,'READ','TOAN_BO')," +
                    "(7,5,'NONE',NULL),(7,6,'WRITE','TOAN_BO'),(7,7,'READ','TOAN_BO'),(7,8,'READ','TOAN_BO')," +
                    "(7,9,'NONE',NULL),(7,10,'NONE',NULL)");
        } catch (SQLException e) {
            LOGGER.log(Level.FINE, "Ghi nhận trạng thái schema hoặc bảng đã tồn tại: " + e.getMessage());
        }
    }
}
