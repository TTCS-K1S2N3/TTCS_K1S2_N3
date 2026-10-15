package vn.nhom10.crm.dao;

import vn.nhom10.crm.dto.BoLocLeadDTO;
import vn.nhom10.crm.model.Lead;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.TrangThaiLeadEnum;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO xử lý các thao tác dữ liệu với bảng '`lead`' (Epic EP-04, Story S4-01).
 * Lưu ý: 'lead' là từ khóa của MySQL 8.x, bắt buộc dùng backtick: `lead`.
 * Tuân thủ DATABASE_RULES.md (PreparedStatement, SQL chuẩn MySQL 8.4 LTS, không dùng SELECT *).
 */
public class LeadDAO {

    private static final Logger LOGGER = Logger.getLogger(LeadDAO.class.getName());

    private static final String COT_SELECT_LEAD =
            "l.id, l.ma_lead, l.ho_ten, l.email, l.email_chuan_hoa, " +
            "l.so_dien_thoai, l.so_dien_thoai_chuan_hoa, l.ten_cong_ty, l.ten_cong_ty_chuan_hoa, " +
            "l.nhu_cau_quan_tam, l.muc_do_quan_tam, l.nganh_nghe_id, l.quy_mo_id, l.khu_vuc_id, " +
            "l.nguon_lead_id, nl.ten_nguon AS ten_nguon_lead, " +
            "l.chien_dich_id, cd.ten_chien_dich AS ten_chien_dich, " +
            "l.bieu_mau_id, bm.ten_bieu_mau AS ten_bieu_mau, " +
            "l.diem, l.phan_loai, l.trang_thai, " +
            "l.nguoi_phu_trach_id, nd.ho_ten AS ten_nguoi_phu_trach, " +
            "l.nhom_phan_bo_id, l.duoc_phan_bo_luc, l.han_phan_hoi_sla, l.duoc_nhan_luc, l.lien_he_dau_luc, " +
            "l.ly_do_tu_choi, l.qua_sla, l.khach_hang_chuyen_doi_id, l.nguoi_lien_he_chuyen_doi_id, " +
            "l.co_hoi_chuyen_doi_id, l.chuyen_doi_luc, l.gop_vao_lead_id, l.created_by, l.created_at, l.updated_at ";

    private static final String FROM_JOIN_LEAD =
            "FROM `lead` l " +
            "LEFT JOIN nguon_lead nl ON l.nguon_lead_id = nl.id " +
            "LEFT JOIN chien_dich cd ON l.chien_dich_id = cd.id " +
            "LEFT JOIN bieu_mau_lead bm ON l.bieu_mau_id = bm.id " +
            "LEFT JOIN nguoi_dung nd ON l.nguoi_phu_trach_id = nd.id ";

    /**
     * Thêm mới một Lead vào bảng `lead` qua Connection được truyền vào (phục vụ Transaction).
     */
    public Long them(Connection conn, Lead lead) throws SQLException {
        String sql = "INSERT INTO `lead` (" +
                "ma_lead, ho_ten, email, email_chuan_hoa, so_dien_thoai, so_dien_thoai_chuan_hoa, " +
                "ten_cong_ty, ten_cong_ty_chuan_hoa, nhu_cau_quan_tam, muc_do_quan_tam, " +
                "nganh_nghe_id, quy_mo_id, khu_vuc_id, nguon_lead_id, chien_dich_id, bieu_mau_id, " +
                "diem, phan_loai, trang_thai, nguoi_phu_trach_id, nhom_phan_bo_id, " +
                "qua_sla, created_by, created_at" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            datThamSoLead(ps, lead);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    lead.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    /**
     * Thêm mới Lead độc lập không dùng transaction ngoài.
     */
    public Long them(Lead lead) {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return them(conn, lead);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm Lead: " + lead.getMaLead(), e);
            throw new RuntimeException("Lỗi lưu Lead: " + e.getMessage(), e);
        }
    }

    /**
     * Tìm Lead theo ID.
     */
    public Lead timTheoId(Long id) {
        if (id == null) return null;
        String sql = "SELECT " + COT_SELECT_LEAD + FROM_JOIN_LEAD + "WHERE l.id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetSangLead(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm Lead theo ID: " + id, e);
        }
        return null;
    }

    /**
     * Tìm Lead theo mã định danh.
     */
    public Lead timTheoMa(String maLead) {
        if (maLead == null || maLead.trim().isEmpty()) return null;
        String sql = "SELECT " + COT_SELECT_LEAD + FROM_JOIN_LEAD + "WHERE l.ma_lead = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maLead.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetSangLead(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm Lead theo mã: " + maLead, e);
        }
        return null;
    }

    /**
     * Lấy danh sách Lead theo biểu mẫu.
     */
    public List<Lead> layDanhSachTheoBieuMau(Long bieuMauId, int limit) {
        List<Lead> danhSach = new ArrayList<>();
        if (bieuMauId == null) return danhSach;

        String sql = "SELECT " + COT_SELECT_LEAD + FROM_JOIN_LEAD +
                "WHERE l.bieu_mau_id = ? ORDER BY l.created_at DESC LIMIT ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, bieuMauId);
            ps.setInt(2, limit > 0 ? limit : 50);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(mapResultSetSangLead(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách Lead theo biểu mẫu: " + bieuMauId, e);
        }
        return danhSach;
    }

    /**
     * Lấy danh sách Lead tổng quan.
     */
    public List<Lead> layDanhSach(int limit, int offset) {
        List<Lead> danhSach = new ArrayList<>();
        String sql = "SELECT " + COT_SELECT_LEAD + FROM_JOIN_LEAD +
                "ORDER BY l.created_at DESC LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit > 0 ? limit : 50);
            ps.setInt(2, Math.max(0, offset));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(mapResultSetSangLead(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách Lead", e);
        }
        return danhSach;
    }

    /**
     * Kiểm tra trùng mã Lead.
     */
    public boolean kiemTraTonTaiMa(String maLead) {
        if (maLead == null || maLead.trim().isEmpty()) return false;
        String sql = "SELECT COUNT(id) FROM `lead` WHERE ma_lead = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maLead.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kiểm tra trùng mã Lead: " + maLead, e);
        }
        return false;
    }

    private void datThamSoLead(PreparedStatement ps, Lead lead) throws SQLException {
        ps.setString(1, lead.getMaLead());
        ps.setString(2, lead.getHoTen());
        ps.setString(3, lead.getEmail());
        ps.setString(4, lead.getEmailChuanHoa());
        ps.setString(5, lead.getSoDienThoai());
        ps.setString(6, lead.getSoDienThoaiChuanHoa());
        ps.setString(7, lead.getTenCongTy());
        ps.setString(8, lead.getTenCongTyChuanHoa());
        ps.setString(9, lead.getNhuCauQuanTam());
        ps.setString(10, lead.getMucDoQuanTam());

        if (lead.getNganhNgheId() != null) {
            ps.setLong(11, lead.getNganhNgheId());
        } else {
            ps.setNull(11, Types.BIGINT);
        }

        if (lead.getQuyMoId() != null) {
            ps.setLong(12, lead.getQuyMoId());
        } else {
            ps.setNull(12, Types.BIGINT);
        }

        if (lead.getKhuVucId() != null) {
            ps.setLong(13, lead.getKhuVucId());
        } else {
            ps.setNull(13, Types.BIGINT);
        }

        ps.setLong(14, lead.getNguonLeadId());

        if (lead.getChienDichId() != null) {
            ps.setLong(15, lead.getChienDichId());
        } else {
            ps.setNull(15, Types.BIGINT);
        }

        if (lead.getBieuMauId() != null) {
            ps.setLong(16, lead.getBieuMauId());
        } else {
            ps.setNull(16, Types.BIGINT);
        }

        ps.setInt(17, lead.getDiem());
        ps.setString(18, lead.getPhanLoai());
        ps.setString(19, lead.getTrangThai() != null ? lead.getTrangThai().getMa() : TrangThaiLeadEnum.MOI.getMa());

        if (lead.getNguoiPhuTrachId() != null) {
            ps.setLong(20, lead.getNguoiPhuTrachId());
        } else {
            ps.setNull(20, Types.BIGINT);
        }

        if (lead.getNhomPhanBoId() != null) {
            ps.setLong(21, lead.getNhomPhanBoId());
        } else {
            ps.setNull(21, Types.BIGINT);
        }

        ps.setBoolean(22, lead.isQuaSla());

        if (lead.getCreatedBy() != null) {
            ps.setLong(23, lead.getCreatedBy());
        } else {
            ps.setNull(23, Types.BIGINT);
        }

        LocalDateTime createdAt = lead.getCreatedAt() != null ? lead.getCreatedAt() : LocalDateTime.now();
        ps.setTimestamp(24, Timestamp.valueOf(createdAt));
    }

    private Lead mapResultSetSangLead(ResultSet rs) throws SQLException {
        Lead lead = new Lead();
        lead.setId(rs.getLong("id"));
        lead.setMaLead(rs.getString("ma_lead"));
        lead.setHoTen(rs.getString("ho_ten"));
        lead.setEmail(rs.getString("email"));
        lead.setEmailChuanHoa(rs.getString("email_chuan_hoa"));
        lead.setSoDienThoai(rs.getString("so_dien_thoai"));
        lead.setSoDienThoaiChuanHoa(rs.getString("so_dien_thoai_chuan_hoa"));
        lead.setTenCongTy(rs.getString("ten_cong_ty"));
        lead.setTenCongTyChuanHoa(rs.getString("ten_cong_ty_chuan_hoa"));
        lead.setNhuCauQuanTam(rs.getString("nhu_cau_quan_tam"));
        lead.setMucDoQuanTam(rs.getString("muc_do_quan_tam"));

        long nnId = rs.getLong("nganh_nghe_id");
        if (!rs.wasNull()) lead.setNganhNgheId(nnId);

        long qmId = rs.getLong("quy_mo_id");
        if (!rs.wasNull()) lead.setQuyMoId(qmId);

        long kvId = rs.getLong("khu_vuc_id");
        if (!rs.wasNull()) lead.setKhuVucId(kvId);

        lead.setNguonLeadId(rs.getLong("nguon_lead_id"));
        lead.setTenNguonLead(rs.getString("ten_nguon_lead"));

        long cdId = rs.getLong("chien_dich_id");
        if (!rs.wasNull()) {
            lead.setChienDichId(cdId);
            lead.setTenChienDich(rs.getString("ten_chien_dich"));
        }

        long bmId = rs.getLong("bieu_mau_id");
        if (!rs.wasNull()) {
            lead.setBieuMauId(bmId);
            lead.setTenBieuMau(rs.getString("ten_bieu_mau"));
        }

        lead.setDiem(rs.getInt("diem"));
        lead.setPhanLoai(rs.getString("phan_loai"));

        String tt = rs.getString("trang_thai");
        lead.setTrangThai(TrangThaiLeadEnum.tuChuoi(tt));

        long nptId = rs.getLong("nguoi_phu_trach_id");
        if (!rs.wasNull()) {
            lead.setNguoiPhuTrachId(nptId);
            lead.setTenNguoiPhuTrach(rs.getString("ten_nguoi_phu_trach"));
        }

        long npbId = rs.getLong("nhom_phan_bo_id");
        if (!rs.wasNull()) lead.setNhomPhanBoId(npbId);

        Timestamp dpb = rs.getTimestamp("duoc_phan_bo_luc");
        if (dpb != null) lead.setDuocPhanBoLuc(dpb.toLocalDateTime());

        Timestamp sla = rs.getTimestamp("han_phan_hoi_sla");
        if (sla != null) lead.setHanPhanHoiSla(sla.toLocalDateTime());

        Timestamp dn = rs.getTimestamp("duoc_nhan_luc");
        if (dn != null) lead.setDuocNhanLuc(dn.toLocalDateTime());

        Timestamp lh = rs.getTimestamp("lien_he_dau_luc");
        if (lh != null) lead.setLienHeDauLuc(lh.toLocalDateTime());

        lead.setLyDoTuChoi(rs.getString("ly_do_tu_choi"));
        lead.setQuaSla(rs.getBoolean("qua_sla"));

        long cdKhId = rs.getLong("khach_hang_chuyen_doi_id");
        if (!rs.wasNull()) lead.setKhachHangChuyenDoiId(cdKhId);

        long cdNlhId = rs.getLong("nguoi_lien_he_chuyen_doi_id");
        if (!rs.wasNull()) lead.setNguoiLienHeChuyenDoiId(cdNlhId);

        long cdChId = rs.getLong("co_hoi_chuyen_doi_id");
        if (!rs.wasNull()) lead.setCoHoiChuyenDoiId(cdChId);

        Timestamp cdl = rs.getTimestamp("chuyen_doi_luc");
        if (cdl != null) lead.setChuyenDoiLuc(cdl.toLocalDateTime());

        long gopId = rs.getLong("gop_vao_lead_id");
        if (!rs.wasNull()) lead.setGopVaoLeadId(gopId);

        long cb = rs.getLong("created_by");
        if (!rs.wasNull()) lead.setCreatedBy(cb);

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) lead.setCreatedAt(created.toLocalDateTime());

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) lead.setUpdatedAt(updated.toLocalDateTime());

        return lead;
    }

    /**
     * Tìm kiếm và lọc danh sách Lead theo 5 nhóm tiêu chí và Data Scope (Story S4-09, AC1, AC2).
     * Mặc định sắp xếp: Lead quá SLA lên trước, sau đó ngày tạo mới nhất (qua_sla DESC, created_at DESC, id DESC).
     */
    public List<Lead> layDanhSachLead(BoLocLeadDTO boLoc, NguoiDung user, PhamViDuLieu phamVi, int limit, int offset) {
        List<Lead> danhSach = new ArrayList<>();
        List<Object> thamSo = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT " + COT_SELECT_LEAD + FROM_JOIN_LEAD + "WHERE 1=1 ");

        apDungDieuKienLocLead(sql, thamSo, boLoc, user, phamVi);

        // Baseline Q5: Ưu tiên Lead quá SLA lên trước, sau đó ngày tạo mới nhất, cuối cùng ID giảm dần
        sql.append(" ORDER BY l.qua_sla DESC, l.created_at DESC, l.id DESC");

        if (limit > 0) {
            sql.append(" LIMIT ? OFFSET ?");
            thamSo.add(limit);
            thamSo.add(Math.max(0, offset));
        }

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < thamSo.size(); i++) {
                ps.setObject(i + 1, thamSo.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(mapResultSetSangLead(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn danh sách Lead theo bộ lọc: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Đếm tổng số Lead thỏa mãn điều kiện lọc và Data Scope (Story S4-09).
     */
    public int demTongSoLead(BoLocLeadDTO boLoc, NguoiDung user, PhamViDuLieu phamVi) {
        List<Object> thamSo = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT COUNT(l.id) FROM `lead` l WHERE 1=1 ");

        apDungDieuKienLocLead(sql, thamSo, boLoc, user, phamVi);

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < thamSo.size(); i++) {
                ps.setObject(i + 1, thamSo.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đếm số lượng Lead theo bộ lọc: " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Áp dụng các điều kiện lọc và Data Scope cho truy vấn Lead (AC1 & AC2).
     */
    private void apDungDieuKienLocLead(StringBuilder sql, List<Object> thamSo, BoLocLeadDTO boLoc, NguoiDung user, PhamViDuLieu phamVi) {
        // 1. Phân quyền Data Scope (Server-side Enforcement - Q2)
        if (user != null) {
            if (phamVi == null) {
                phamVi = PhamViDuLieu.CA_NHAN;
            }
            if (phamVi == PhamViDuLieu.CA_NHAN) {
                // Sales Rep chỉ xem Lead do chính mình phụ trách
                sql.append(" AND l.nguoi_phu_trach_id = ?");
                thamSo.add(user.getId());
            } else if (phamVi == PhamViDuLieu.NHOM) {
                // Team Lead xem Lead thuộc nhóm mình quản lý
                Long nhomId = user.getNhomKinhDoanhId() != null ? Long.valueOf(user.getNhomKinhDoanhId()) : -1L;
                sql.append(" AND (l.nhom_phan_bo_id = ? OR l.nguoi_phu_trach_id IN (SELECT id FROM nguoi_dung WHERE nhom_kinh_doanh_id = ?))");
                thamSo.add(nhomId);
                thamSo.add(nhomId);
            }
            // TOAN_BO (Director / Admin): Không giới hạn theo phạm vi sở hữu
        }

        if (boLoc == null) {
            return;
        }

        // 2. Tiêu chí 1: Trạng thái Lead
        if (boLoc.getTrangThai() != null && !boLoc.getTrangThai().isBlank()) {
            sql.append(" AND l.trang_thai = ?");
            thamSo.add(boLoc.getTrangThai().trim());
        }

        // 3. Tiêu chí 2: Nguồn Lead
        if (boLoc.getNguonLeadId() != null && boLoc.getNguonLeadId() > 0) {
            sql.append(" AND l.nguon_lead_id = ?");
            thamSo.add(boLoc.getNguonLeadId());
        }

        // 4. Tiêu chí 3: Phân loại Nóng / Ấm / Lạnh
        if (boLoc.getPhanLoai() != null && !boLoc.getPhanLoai().isBlank()) {
            String pl = boLoc.getPhanLoai().trim().toUpperCase();
            if ("NONG".equals(pl) || "NÓNG".equals(pl)) {
                sql.append(" AND (UPPER(l.phan_loai) = 'NONG' OR l.phan_loai = 'Nóng')");
            } else if ("AM".equals(pl) || "ẤM".equals(pl)) {
                sql.append(" AND (UPPER(l.phan_loai) = 'AM' OR l.phan_loai = 'Ấm')");
            } else if ("LANH".equals(pl) || "LẠNH".equals(pl)) {
                sql.append(" AND (UPPER(l.phan_loai) = 'LANH' OR l.phan_loai = 'Lạnh')");
            } else {
                sql.append(" AND l.phan_loai = ?");
                thamSo.add(boLoc.getPhanLoai().trim());
            }
        }

        // 5. Tiêu chí 4: Người phụ trách cụ thể (nếu được phép xem toàn bộ/nhóm và chọn lọc)
        if (boLoc.getNguoiPhuTrachId() != null && boLoc.getNguoiPhuTrachId() > 0) {
            if (phamVi != PhamViDuLieu.CA_NHAN) {
                sql.append(" AND l.nguoi_phu_trach_id = ?");
                thamSo.add(boLoc.getNguoiPhuTrachId());
            }
        }

        // 6. Tiêu chí 5: Khoảng thời gian tạo (tuNgay - denNgay, Q1)
        if (boLoc.getTuNgay() != null && !boLoc.getTuNgay().isBlank()) {
            try {
                String tuNgayStr = boLoc.getTuNgay().trim();
                if (!tuNgayStr.contains(":")) {
                    tuNgayStr += " 00:00:00";
                }
                sql.append(" AND l.created_at >= ?");
                thamSo.add(Timestamp.valueOf(tuNgayStr));
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Không thể parse tuNgay: " + boLoc.getTuNgay(), e);
            }
        }

        if (boLoc.getDenNgay() != null && !boLoc.getDenNgay().isBlank()) {
            try {
                String denNgayStr = boLoc.getDenNgay().trim();
                if (!denNgayStr.contains(":")) {
                    denNgayStr += " 23:59:59";
                }
                sql.append(" AND l.created_at <= ?");
                thamSo.add(Timestamp.valueOf(denNgayStr));
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Không thể parse denNgay: " + boLoc.getDenNgay(), e);
            }
        }
    }
}
