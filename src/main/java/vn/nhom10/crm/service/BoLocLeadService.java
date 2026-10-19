package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.BoLocDaLuuDAO;
import vn.nhom10.crm.dao.DanhMucBanHangDAO;
import vn.nhom10.crm.dao.LeadDAO;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dto.BoLocLeadDTO;
import vn.nhom10.crm.dto.MucDanhMucDTO;
import vn.nhom10.crm.model.*;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service xử lý nghiệp vụ Lọc danh sách Lead và Quản lý bộ lọc đã lưu (Story S4-09):
 * - AC1: Lọc theo 5 tiêu chí: trạng thái, nguồn lead, phân loại nóng/ấm/lạnh, người phụ trách, khoảng thời gian
 * - AC2: Nổi bật Lead quá SLA và ưu tiên sắp xếp
 * - AC3: Lưu bộ lọc cá nhân, tải danh sách và áp dụng bộ lọc đã lưu
 */
public class BoLocLeadService {

    private static final Logger LOGGER = Logger.getLogger(BoLocLeadService.class.getName());
    public static final String LOAI_LEAD = "LEAD";

    private final LeadDAO leadDAO;
    private final BoLocDaLuuDAO boLocDaLuuDAO;
    private final DanhMucBanHangDAO danhMucBanHangDAO;
    private final NguoiDungDAO nguoiDungDAO;
    private final PermissionService permissionService;

    public BoLocLeadService() {
        this(new LeadDAO(), new BoLocDaLuuDAO(), new DanhMucBanHangDAO(), new NguoiDungDAO(), PermissionService.getInstance());
    }

    public BoLocLeadService(LeadDAO leadDAO, BoLocDaLuuDAO boLocDaLuuDAO, DanhMucBanHangDAO danhMucBanHangDAO,
                            NguoiDungDAO nguoiDungDAO, PermissionService permissionService) {
        this.leadDAO = leadDAO != null ? leadDAO : new LeadDAO();
        this.boLocDaLuuDAO = boLocDaLuuDAO != null ? boLocDaLuuDAO : new BoLocDaLuuDAO();
        this.danhMucBanHangDAO = danhMucBanHangDAO != null ? danhMucBanHangDAO : new DanhMucBanHangDAO();
        this.nguoiDungDAO = nguoiDungDAO != null ? nguoiDungDAO : new NguoiDungDAO();
        this.permissionService = permissionService != null ? permissionService : PermissionService.getInstance();
    }

    /**
     * Tìm kiếm và lọc danh sách Lead (AC1 & AC2) có kiểm soát Data Scope (Q2).
     */
    public List<Lead> timKiemLead(BoLocLeadDTO boLoc, NguoiDung user, int limit, int offset) {
        if (user == null || user.getId() <= 0) {
            return Collections.emptyList();
        }

        // Xác định Data Scope runtime của user đối với module LEAD
        PhamViDuLieu phamVi = permissionService.layPhamViHieuLuc(user, "LEAD", MucQuyen.READ);
        if (phamVi == null) {
            phamVi = PhamViDuLieu.CA_NHAN;
        }

        return leadDAO.layDanhSachLead(boLoc, user, phamVi, limit, offset);
    }

    /**
     * Đếm tổng số Lead thỏa mãn điều kiện lọc và Data Scope.
     */
    public int demTongSoLead(BoLocLeadDTO boLoc, NguoiDung user) {
        if (user == null || user.getId() <= 0) {
            return 0;
        }

        PhamViDuLieu phamVi = permissionService.layPhamViHieuLuc(user, "LEAD", MucQuyen.READ);
        if (phamVi == null) {
            phamVi = PhamViDuLieu.CA_NHAN;
        }

        return leadDAO.demTongSoLead(boLoc, user, phamVi);
    }

    /**
     * Lưu bộ lọc cá nhân của người dùng (AC3):
     * - Tên bộ lọc bắt buộc, tối đa 80 ký tự theo baseline Q4
     * - Lưu điều kiện dạng JSON vào bảng bo_loc_da_luu với loai_doi_tuong = 'LEAD'
     */
    public BoLocDaLuu luuBoLoc(NguoiDung user, String tenBoLoc, BoLocLeadDTO tieuChi, boolean macDinh) throws SQLException {
        if (user == null || user.getId() <= 0) {
            throw new IllegalArgumentException("Yêu cầu đăng nhập để lưu bộ lọc.");
        }
        if (tenBoLoc == null || tenBoLoc.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tên bộ lọc.");
        }
        String tenTrim = tenBoLoc.trim();
        if (tenTrim.length() > 80) {
            throw new IllegalArgumentException("Tên bộ lọc không được vượt quá 80 ký tự.");
        }

        if (tieuChi == null) {
            tieuChi = new BoLocLeadDTO();
        }

        String json = tieuChi.toJson();
        BoLocDaLuu model = new BoLocDaLuu();
        model.setNguoiDungId(user.getId());
        model.setLoaiDoiTuong(LOAI_LEAD);
        model.setTenBoLoc(tenTrim);
        model.setTieuChiJson(json);
        model.setMacDinh(macDinh);

        long id = boLocDaLuuDAO.luuBoLoc(model);
        model.setId(id);

        if (macDinh && id > 0) {
            boLocDaLuuDAO.datMacDinh(id, user.getId(), LOAI_LEAD);
        }

        return model;
    }

    /**
     * Lấy danh sách toàn bộ bộ lọc đã lưu của người dùng đối với module LEAD (AC3).
     */
    public List<BoLocDaLuu> layDanhSachBoLoc(NguoiDung user) {
        if (user == null || user.getId() <= 0) {
            return Collections.emptyList();
        }
        return boLocDaLuuDAO.layDanhSachTheoNguoiDung(user.getId(), LOAI_LEAD);
    }

    /**
     * Lấy chi tiết bộ lọc theo ID (chỉ cho phép nếu thuộc về người dùng hiện tại).
     */
    public BoLocDaLuu timBoLocTheoId(long id, NguoiDung user) {
        if (user == null || user.getId() <= 0 || id <= 0) {
            return null;
        }
        return boLocDaLuuDAO.timTheoId(id, user.getId());
    }

    /**
     * Lấy danh mục nguồn lead đang hoạt động phục vụ dropdown lọc.
     */
    public List<MucDanhMucDTO> layDanhSachNguonLead() {
        return danhMucBanHangDAO.layDanhSach(LoaiDanhMuc.NGUON_LEAD, true);
    }

    /**
     * Lấy danh sách người phụ trách có thể lọc tùy theo phạm vi quyền của người dùng.
     */
    public List<NguoiDung> layDanhSachNguoiPhuTrach(NguoiDung user) {
        if (user == null || user.getId() <= 0) {
            return Collections.emptyList();
        }

        PhamViDuLieu phamVi = permissionService.layPhamViHieuLuc(user, "LEAD", MucQuyen.READ);
        if (phamVi == PhamViDuLieu.CA_NHAN) {
            // Sales Rep chỉ xem của chính mình
            return Collections.singletonList(user);
        }

        // Team Lead hoặc Director/Admin: lấy danh sách người dùng khả dụng
        return nguoiDungDAO.layDanhSachNguoiDungKhaDungTiepNhan(0);
    }
}
