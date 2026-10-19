package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dao.VaiTroDAO;
import vn.nhom10.crm.dao.VaiTroModuleDAO;
import vn.nhom10.crm.model.HanhDongThayDoi;
import vn.nhom10.crm.model.LoaiDoiTuongNhayCam;
import vn.nhom10.crm.model.MucQuyen;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.model.VaiTroModule;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service quản lý phân quyền chức năng và phân quyền dữ liệu theo vai trò (DB-backed).
 * Tuân thủ nghiêm ngặt:
 * - Single Source of Truth là CSDL (vai_tro, module_he_thong, vai_tro_module).
 * - Phòng chống Privilege Escalation trong môi trường đa vai trò (Multi-role).
 * - Tách biệt độc lập giữa Chiều mức quyền (Permission Level) và Chiều phạm vi dữ liệu (Data Scope).
 */
public class PermissionService {

    private static final Logger LOGGER = Logger.getLogger(PermissionService.class.getName());

    private static final PermissionService INSTANCE = new PermissionService();

    private final VaiTroModuleDAO vaiTroModuleDAO;
    private final VaiTroDAO vaiTroDAO;
    private final NhatKyThayDoiService nhatKyThayDoiService;
    private final NguoiDungDAO nguoiDungDAO;

    // Bộ nhớ đệm phân quyền để tối ưu hiệu năng runtime, tự động xóa khi có cập nhật
    private final Map<String, Map<String, VaiTroModule>> cacheMatrix = new ConcurrentHashMap<>();

    public static PermissionService getInstance() {
        return INSTANCE;
    }

    public PermissionService() {
        this(new VaiTroModuleDAO(), new VaiTroDAO(), new NhatKyThayDoiService(), new NguoiDungDAO());
    }

    public PermissionService(VaiTroModuleDAO vaiTroModuleDAO, VaiTroDAO vaiTroDAO) {
        this(vaiTroModuleDAO, vaiTroDAO, new NhatKyThayDoiService(), new NguoiDungDAO());
    }

    public PermissionService(VaiTroModuleDAO vaiTroModuleDAO, VaiTroDAO vaiTroDAO, NhatKyThayDoiService nhatKyThayDoiService) {
        this(vaiTroModuleDAO, vaiTroDAO, nhatKyThayDoiService, new NguoiDungDAO());
    }

    public PermissionService(VaiTroModuleDAO vaiTroModuleDAO, VaiTroDAO vaiTroDAO, NhatKyThayDoiService nhatKyThayDoiService, NguoiDungDAO nguoiDungDAO) {
        this.vaiTroModuleDAO = vaiTroModuleDAO != null ? vaiTroModuleDAO : new VaiTroModuleDAO();
        this.vaiTroDAO = vaiTroDAO != null ? vaiTroDAO : new VaiTroDAO();
        this.nhatKyThayDoiService = nhatKyThayDoiService != null ? nhatKyThayDoiService : new NhatKyThayDoiService();
        this.nguoiDungDAO = nguoiDungDAO != null ? nguoiDungDAO : new NguoiDungDAO();
    }

    /**
     * Chuyển đổi mã module giao diện (12 module) sang mã module chuẩn trong CSDL (10 module).
     *
     * Mapping tập trung:
     * - BAO_GIA          -> BAO_GIA_HOP_DONG
     * - HOP_DONG         -> BAO_GIA_HOP_DONG
     * - CHI_TIEU         -> KPI
     * - NGUOI_DUNG       -> NGUOI_DUNG_NHAT_KY
     * - DANH_MUC         -> DANH_MUC
     * - KHACH_HANG       -> KHACH_HANG
     * - LEAD             -> LEAD
     * - CO_HOI           -> CO_HOI
     * - HOAT_DONG        -> HOAT_DONG
     * - BAO_CAO          -> BAO_CAO
     * - TU_DONG_HOA      -> TU_DONG_HOA
     * - TONG_QUAN        -> Ngoại lệ: CSDL chưa có hàng canonical riêng, cho phép người dùng hoạt động xem.
     */
    public String chuyenMaCanonical(String maModule) {
        if (maModule == null || maModule.isBlank()) {
            return "";
        }
        String upper = maModule.trim().toUpperCase();
        switch (upper) {
            case "BAO_GIA":
            case "HOP_DONG":
            case "BAO_GIA_HOP_DONG":
                return "BAO_GIA_HOP_DONG";
            case "CHI_TIEU":
            case "KPI":
                return "KPI";
            case "NGUOI_DUNG":
            case "NGUOI_DUNG_NHAT_KY":
                return "NGUOI_DUNG_NHAT_KY";
            case "DANH_MUC":
                return "DANH_MUC";
            case "KHACH_HANG":
                return "KHACH_HANG";
            case "LEAD":
                return "LEAD";
            case "CO_HOI":
                return "CO_HOI";
            case "HOAT_DONG":
                return "HOAT_DONG";
            case "BAO_CAO":
                return "BAO_CAO";
            case "TU_DONG_HOA":
                return "TU_DONG_HOA";
            case "TONG_QUAN":
                return "TONG_QUAN";
            default:
                return upper;
        }
    }

    /**
     * Kiểm tra người dùng có đạt mức quyền yêu cầu (requiredLevel) trên module hay không.
     * Hỗ trợ đa vai trò: người dùng có quyền nếu ÍT NHẤT 1 vai trò của họ đạt mức quyền yêu cầu.
     * Admin luôn có toàn quyền (FULL).
     */
    public boolean coQuyen(NguoiDung user, String maModule, MucQuyen requiredLevel) {
        if (user == null) {
            return false;
        }
        if (user.getTrangThai() != null && !user.dangHoatDong()) {
            return false;
        }
        if (requiredLevel == null || requiredLevel == MucQuyen.NONE) {
            return true;
        }

        // Quản trị hệ thống (Admin) luôn có toàn quyền
        if (user.coVaiTro(VaiTroEnum.ADMIN) || user.coVaiTro("ADMIN")) {
            return true;
        }

        String canonical = chuyenMaCanonical(maModule);
        if ("TONG_QUAN".equalsIgnoreCase(canonical)) {
            // Module Tổng quan: CSDL không lưu hàng riêng, cho phép mọi tài khoản hợp lệ xem
            return requiredLevel == MucQuyen.READ;
        }

        Set<String> dsMaVaiTro = layDanhSachMaVaiTro(user);
        if (dsMaVaiTro.isEmpty()) {
            return false;
        }

        for (String maVt : dsMaVaiTro) {
            VaiTroModule grant = layQuyenCuaVaiTro(maVt, canonical);
            if (grant != null && grant.getMucQuyen().baoGom(requiredLevel)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Xác định phạm vi dữ liệu hiệu lực của người dùng cho một module ở một mức thao tác cụ thể.
     *
     * QUY TẮC CHỐNG PRIVILEGE ESCALATION BẮT BUỘC:
     * Tuyệt đối KHÔNG tính độc lập max(level) + max(scope).
     * Chỉ những vai trò đạt mức requiredLevel mới được tham gia tính phạm vi dữ liệu:
     * - VIEW (cần READ): chỉ xét các grant có MucQuyen >= READ, lấy max scope của chúng.
     * - CREATE/UPDATE (cần WRITE): chỉ xét các grant có MucQuyen >= WRITE, lấy max scope của chúng.
     * - DELETE (cần FULL): chỉ xét các grant có MucQuyen == FULL, lấy max scope của chúng.
     */
    public PhamViDuLieu layPhamViHieuLuc(NguoiDung user, String maModule, MucQuyen requiredLevel) {
        if (user == null) {
            return null;
        }
        if (user.getTrangThai() != null && !user.dangHoatDong()) {
            return null;
        }

        // Admin luôn có quyền toàn bộ
        if (user.coVaiTro(VaiTroEnum.ADMIN) || user.coVaiTro("ADMIN")) {
            return PhamViDuLieu.TOAN_BO;
        }

        String canonical = chuyenMaCanonical(maModule);
        if ("TONG_QUAN".equalsIgnoreCase(canonical)) {
            return PhamViDuLieu.TOAN_BO;
        }

        Set<String> dsMaVaiTro = layDanhSachMaVaiTro(user);
        if (dsMaVaiTro.isEmpty()) {
            return null;
        }

        PhamViDuLieu maxScope = null;
        for (String maVt : dsMaVaiTro) {
            VaiTroModule grant = layQuyenCuaVaiTro(maVt, canonical);
            if (grant != null && grant.getMucQuyen().baoGom(requiredLevel)) {
                PhamViDuLieu pv = grant.getPhamViDuLieu();
                if (pv != null) {
                    if (maxScope == null) {
                        maxScope = pv;
                    } else if (soSanhPhamVi(pv, maxScope) > 0) {
                        maxScope = pv;
                    }
                }
            }
        }
        return maxScope;
    }

    /**
     * Kiểm tra quyền cho một vai trò đơn lẻ (thuận tiện cho unit test và kiểm tra đơn lẻ).
     */
    public boolean coQuyen(VaiTroEnum vaiTro, String maModule, MucQuyen requiredLevel) {
        if (vaiTro == null) {
            return false;
        }
        if (vaiTro == VaiTroEnum.ADMIN) {
            return true;
        }
        if (requiredLevel == null || requiredLevel == MucQuyen.NONE) {
            return true;
        }
        String canonical = chuyenMaCanonical(maModule);
        if ("TONG_QUAN".equalsIgnoreCase(canonical)) {
            return requiredLevel == MucQuyen.READ;
        }
        VaiTroModule grant = layQuyenCuaVaiTro(vaiTro.getMaVaiTro(), canonical);
        return grant != null && grant.getMucQuyen().baoGom(requiredLevel);
    }

    /**
     * Lấy bản ghi phân quyền từ bộ nhớ đệm (hoặc nạp từ DB nếu chưa có).
     */
    public VaiTroModule layQuyenCuaVaiTro(String maVaiTro, String maCanonicalModule) {
        if (maVaiTro == null || maCanonicalModule == null) {
            return null;
        }
        String vtKey = maVaiTro.trim().toUpperCase();
        String mdKey = maCanonicalModule.trim().toUpperCase();

        Map<String, VaiTroModule> moduleMap = cacheMatrix.get(vtKey);
        if (moduleMap == null) {
            napCacheMatrixChoVaiTro(vtKey);
            moduleMap = cacheMatrix.get(vtKey);
        }
        VaiTroModule raw = moduleMap != null ? moduleMap.get(mdKey) : null;
        if (raw == null) {
            return null;
        }

        // BẢO VỆ RUNTIME: Ép trần quyền an toàn theo Sheet 2 (ngay cả khi DB có grant vượt trần cũ)
        RolePermissionCeilingPolicy ceilingPolicy = RolePermissionCeilingPolicy.getInstance();
        MucQuyen safeLevel = ceilingPolicy.epMucQuyenAnToan(vtKey, mdKey, raw.getMucQuyen());
        PhamViDuLieu safeScope = ceilingPolicy.epPhamViAnToan(vtKey, mdKey, safeLevel, raw.getPhamViDuLieu());

        VaiTroModule safe = new VaiTroModule();
        safe.setId(raw.getId());
        safe.setVaiTroId(raw.getVaiTroId());
        safe.setMaVaiTro(raw.getMaVaiTro());
        safe.setTenVaiTro(raw.getTenVaiTro());
        safe.setPhamViToiDaVaiTro(raw.getPhamViToiDaVaiTro());
        safe.setModuleId(raw.getModuleId());
        safe.setMaModule(raw.getMaModule());
        safe.setTenModule(raw.getTenModule());
        safe.setMoTaModule(raw.getMoTaModule());
        safe.setThuTuHienThi(raw.getThuTuHienThi());
        safe.setMucQuyen(safeLevel);
        safe.setPhamViDuLieu(safeScope);
        safe.setCreatedAt(raw.getCreatedAt());
        return safe;
    }

    /**
     * Lấy toàn bộ ma trận 10 module cho một vai trò cụ thể.
     */
    public List<VaiTroModule> layMatrixChoVaiTro(String maVaiTro) {
        if (maVaiTro == null || maVaiTro.isBlank()) {
            return Collections.emptyList();
        }
        String vtKey = maVaiTro.trim().toUpperCase();
        Map<String, VaiTroModule> moduleMap = cacheMatrix.get(vtKey);
        if (moduleMap == null) {
            napCacheMatrixChoVaiTro(vtKey);
            moduleMap = cacheMatrix.get(vtKey);
        }
        if (moduleMap != null) {
            List<VaiTroModule> list = new ArrayList<>(moduleMap.values());
            list.sort((a, b) -> Integer.compare(a.getThuTuHienThi(), b.getThuTuHienThi()));
            return list;
        }
        return vaiTroModuleDAO.layTheoMaVaiTro(maVaiTro);
    }

    /**
     * DTO nội bộ mô tả chi tiết thay đổi trên từng module của một vai trò.
     */
    public static class ThayDoiModuleDTO {
        private final int moduleId;
        private final String maModule;
        private final String tenModule;
        private final MucQuyen mucQuyenCu;
        private final MucQuyen mucQuyenMoi;
        private final PhamViDuLieu phamViCu;
        private final PhamViDuLieu phamViMoi;

        public ThayDoiModuleDTO(int moduleId, String maModule, String tenModule,
                                MucQuyen mucQuyenCu, MucQuyen mucQuyenMoi,
                                PhamViDuLieu phamViCu, PhamViDuLieu phamViMoi) {
            this.moduleId = moduleId;
            this.maModule = maModule;
            this.tenModule = tenModule;
            this.mucQuyenCu = mucQuyenCu;
            this.mucQuyenMoi = mucQuyenMoi;
            this.phamViCu = phamViCu;
            this.phamViMoi = phamViMoi;
        }

        public int getModuleId() { return moduleId; }
        public String getMaModule() { return maModule; }
        public String getTenModule() { return tenModule; }
        public MucQuyen getMucQuyenCu() { return mucQuyenCu; }
        public MucQuyen getMucQuyenMoi() { return mucQuyenMoi; }
        public PhamViDuLieu getPhamViCu() { return phamViCu; }
        public PhamViDuLieu getPhamViMoi() { return phamViMoi; }
    }

    /**
     * Cập nhật phân quyền cho một vai trò từ quản trị viên với tài khoản xác thực (actor).
     *
     * Thực thi trong transaction duy nhất:
     * - Kiểm tra xác thực phía server và khóa trần Sheet 2.
     * - Đọc trạng thái quyền thực tế trước cập nhật.
     * - Cập nhật dữ liệu quyền mới vào CSDL.
     * - Xác định chính xác các module thay đổi muc_quyen hoặc pham_vi_du_lieu.
     * - Nếu có thay đổi: ghi đầy đủ giá trị trước/sau vào các trường JSON của bảng nhat_ky_he_thong.
     * - Nếu ghi audit thất bại (ném SQLException): Rollback cả thay đổi quyền.
     * - Nếu dữ liệu trước và sau giống nhau: Bỏ qua không ghi audit.
     * - Lấy người thực hiện từ tài khoản xác thực (actor), không hardcode.
     */
    public boolean capNhatMatrixChoVaiTro(String maVaiTro, List<VaiTroModule> danhSachMoi, NguoiDung actor, String diaChiIp, String thietBi) {
        if (maVaiTro == null || maVaiTro.isBlank() || danhSachMoi == null || danhSachMoi.isEmpty()) {
            return false;
        }

        // BẢO VỆ ADMIN: Tuyệt đối không cho phép sửa đổi vai trò ADMIN
        if (VaiTroEnum.ADMIN.getMaVaiTro().equalsIgnoreCase(maVaiTro.trim())) {
            LOGGER.log(Level.WARNING, "Từ chối cập nhật vai trò ADMIN: Quản trị viên luôn có toàn quyền và không thể sửa đổi.");
            return false;
        }

        VaiTro vaiTro = vaiTroDAO.timTheoMa(maVaiTro);
        if (vaiTro == null) {
            LOGGER.log(Level.WARNING, "Không tìm thấy vai trò: " + maVaiTro);
            return false;
        }

        PhamViDuLieu phamViToiDa = vaiTro.getPhamViToiDa();
        RolePermissionCeilingPolicy ceilingPolicy = RolePermissionCeilingPolicy.getInstance();

        // Server-side validation cho từng dòng phân quyền theo trần quyền Sheet 2
        for (VaiTroModule vtm : danhSachMoi) {
            if (vtm.getMucQuyen() == null) {
                vtm.setMucQuyen(MucQuyen.NONE);
            }
            PhamViDuLieu pv = vtm.getPhamViDuLieu();
            if (pv != null) {
                // Kiểm tra xem phạm vi được cấp có vượt quá phạm vi tối đa của vai trò không
                if (soSanhPhamVi(pv, phamViToiDa) > 0) {
                    LOGGER.log(Level.WARNING, "Phạm vi " + pv + " vượt quá phạm vi tối đa " + phamViToiDa + " của vai trò " + maVaiTro);
                    return false;
                }
            }
            // Khóa trần cứng theo ma trận Sheet 2
            if (!ceilingPolicy.kiemTraHopLe(maVaiTro, vtm.getMaModule(), vtm.getMucQuyen(), pv)) {
                LOGGER.log(Level.WARNING, "Cấu hình " + vtm.getMaModule() + " [mức " + vtm.getMucQuyen() +
                        ", phạm vi " + pv + "] vượt trần Sheet 2 của vai trò " + maVaiTro);
                return false;
            }
        }

        try (Connection conn = DatabaseConnection.layKetNoi()) {
            conn.setAutoCommit(false);
            try {
                // 1. Lấy trạng thái quyền thực tế trước cập nhật trong cùng connection
                List<VaiTroModule> danhSachHienTai = vaiTroModuleDAO.layTheoVaiTroId(conn, vaiTro.getId());
                Map<Integer, VaiTroModule> mapHienTai = new HashMap<>();
                for (VaiTroModule vtm : danhSachHienTai) {
                    mapHienTai.put(vtm.getModuleId(), vtm);
                }

                // 2. Cập nhật các dòng phân quyền mới
                for (VaiTroModule vtm : danhSachMoi) {
                    vaiTroModuleDAO.capNhatQuyen(vaiTro.getId(), vtm.getModuleId(), vtm.getMucQuyen(), vtm.getPhamViDuLieu(), conn);
                }

                // 3. Xác định chính xác các module có thay đổi
                List<ThayDoiModuleDTO> dsThayDoi = xacDinhThayDoi(mapHienTai, danhSachMoi);

                // 4. Ghi audit log nếu có thay đổi thực tế
                if (!dsThayDoi.isEmpty()) {
                    String giaTriTruocJson = taoJsonTrangThai(vaiTro, dsThayDoi, true);
                    String giaTriSauJson = taoJsonTrangThai(vaiTro, dsThayDoi, false);

                    Long actorId = (actor != null && actor.getId() > 0) ? actor.getId() : null;
                    String actorName = (actor != null && actor.getHoTen() != null && !actor.getHoTen().isBlank())
                            ? actor.getHoTen() : "Quản trị hệ thống";
                    String actorEmail = (actor != null && actor.getEmail() != null && !actor.getEmail().isBlank())
                            ? actor.getEmail() : "admin@crm.vn";

                    nhatKyThayDoiService.ghiNhatKyThayDoi(
                            conn,
                            actorId,
                            actorName,
                            actorEmail,
                            LoaiDoiTuongNhayCam.VAI_TRO_NGUOI_DUNG,
                            (long) vaiTro.getId(),
                            vaiTro.getMaVaiTro(),
                            "Ma trận phân quyền: " + vaiTro.getTenVaiTro(),
                            "Ma trận phân quyền vai trò",
                            giaTriTruocJson,
                            giaTriSauJson,
                            HanhDongThayDoi.CAP_NHAT,
                            "Cập nhật ma trận phân quyền vai trò " + vaiTro.getTenVaiTro() + " (" + dsThayDoi.size() + " module thay đổi)",
                            diaChiIp != null ? diaChiIp : "127.0.0.1",
                            thietBi != null ? thietBi : "Trình duyệt CRM"
                    );
                }

                // 5. Commit transaction đồng bộ
                conn.commit();
                xoaCache();
                return true;

            } catch (Exception e) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Lỗi rollback transaction ma trận phân quyền: " + ex.getMessage(), ex);
                }
                LOGGER.log(Level.SEVERE, "Lỗi cập nhật ma trận quyền cho vaiTro=" + maVaiTro + ": " + e.getMessage(), e);
                return false;
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kết nối cơ sở dữ liệu khi cập nhật phân quyền: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Tương thích ngược: Cập nhật ma trận phân quyền cho một vai trò theo nguoiThucHienId.
     */
    public boolean capNhatMatrixChoVaiTro(String maVaiTro, List<VaiTroModule> danhSachMoi, int nguoiThucHienId, String diaChiIp, String thietBi) {
        NguoiDung actor = null;
        if (nguoiThucHienId > 0) {
            try {
                actor = nguoiDungDAO.timTheoId((long) nguoiThucHienId);
            } catch (Exception ignored) {
            }
            if (actor == null) {
                actor = new NguoiDung();
                actor.setId((long) nguoiThucHienId);
            }
        }
        return capNhatMatrixChoVaiTro(maVaiTro, danhSachMoi, actor, diaChiIp, thietBi);
    }

    private List<ThayDoiModuleDTO> xacDinhThayDoi(Map<Integer, VaiTroModule> mapHienTai, List<VaiTroModule> danhSachMoi) {
        List<ThayDoiModuleDTO> dsThayDoi = new ArrayList<>();
        for (VaiTroModule moi : danhSachMoi) {
            VaiTroModule cu = mapHienTai.get(moi.getModuleId());
            MucQuyen levelCu = cu != null && cu.getMucQuyen() != null ? cu.getMucQuyen() : MucQuyen.NONE;
            MucQuyen levelMoi = moi.getMucQuyen() != null ? moi.getMucQuyen() : MucQuyen.NONE;
            PhamViDuLieu scopeCu = cu != null ? cu.getPhamViDuLieu() : null;
            PhamViDuLieu scopeMoi = moi.getPhamViDuLieu();

            boolean doiLevel = (levelCu != levelMoi);
            boolean doiScope = !Objects.equals(scopeCu, scopeMoi);

            if (doiLevel || doiScope) {
                String maModule = cu != null && cu.getMaModule() != null ? cu.getMaModule() : moi.getMaModule();
                String tenModule = cu != null && cu.getTenModule() != null ? cu.getTenModule() : moi.getTenModule();
                dsThayDoi.add(new ThayDoiModuleDTO(moi.getModuleId(), maModule, tenModule, levelCu, levelMoi, scopeCu, scopeMoi));
            }
        }
        return dsThayDoi;
    }

    private String taoJsonTrangThai(VaiTro vaiTro, List<ThayDoiModuleDTO> dsThayDoi, boolean laTruoc) {
        StringBuilder sb = new StringBuilder("{");
        appendJsonField(sb, "maDoiTuong", vaiTro.getMaVaiTro());
        sb.append(",");
        appendJsonField(sb, "tenDoiTuong", "Ma trận phân quyền: " + vaiTro.getTenVaiTro());
        sb.append(",");
        appendJsonField(sb, "truongThayDoi", "Ma trận phân quyền vai trò");
        sb.append(",");

        StringBuilder giaTriSb = new StringBuilder();
        for (int i = 0; i < dsThayDoi.size(); i++) {
            ThayDoiModuleDTO td = dsThayDoi.get(i);
            if (i > 0) {
                giaTriSb.append(", ");
            }
            String tenHienThi = td.getMaModule() != null ? td.getMaModule() : ("Module #" + td.getModuleId());
            MucQuyen mq = laTruoc ? td.getMucQuyenCu() : td.getMucQuyenMoi();
            PhamViDuLieu pv = laTruoc ? td.getPhamViCu() : td.getPhamViMoi();
            giaTriSb.append(tenHienThi).append(" [").append(mq != null ? mq.getMa() : "NONE").append(", ")
                    .append(pv != null ? pv.getMa() : "-").append("]");
        }
        appendJsonField(sb, "giaTri", giaTriSb.toString());
        sb.append(",");
        sb.append("\"soModuleThayDoi\":").append(dsThayDoi.size()).append(",");
        sb.append("\"cacThayDoi\":[");
        for (int i = 0; i < dsThayDoi.size(); i++) {
            ThayDoiModuleDTO td = dsThayDoi.get(i);
            if (i > 0) {
                sb.append(",");
            }
            sb.append("{");
            sb.append("\"moduleId\":").append(td.getModuleId()).append(",");
            appendJsonField(sb, "maModule", td.getMaModule());
            sb.append(",");
            appendJsonField(sb, "tenModule", td.getTenModule());
            sb.append(",");
            MucQuyen mq = laTruoc ? td.getMucQuyenCu() : td.getMucQuyenMoi();
            PhamViDuLieu pv = laTruoc ? td.getPhamViCu() : td.getPhamViMoi();
            appendJsonField(sb, "mucQuyen", mq != null ? mq.getMa() : "NONE");
            sb.append(",");
            appendJsonField(sb, "phamViDuLieu", pv != null ? pv.getMa() : null);
            sb.append("}");
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    private static void appendJsonField(StringBuilder sb, String key, String value) {
        sb.append("\"").append(key).append("\":");
        if (value == null) {
            sb.append("null");
        } else {
            sb.append("\"").append(escapeJson(value)).append("\"");
        }
    }

    private static String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public void xoaCache() {
        cacheMatrix.clear();
    }

    private synchronized void napCacheMatrixChoVaiTro(String maVaiTro) {
        List<VaiTroModule> list = vaiTroModuleDAO.layTheoMaVaiTro(maVaiTro);
        Map<String, VaiTroModule> map = new HashMap<>();
        for (VaiTroModule vtm : list) {
            if (vtm.getMaModule() != null) {
                map.put(vtm.getMaModule().trim().toUpperCase(), vtm);
            }
        }
        cacheMatrix.put(maVaiTro.trim().toUpperCase(), map);
    }

    private Set<String> layDanhSachMaVaiTro(NguoiDung user) {
        Set<String> set = new HashSet<>();
        if (user.getDanhSachVaiTro() != null) {
            for (VaiTro vt : user.getDanhSachVaiTro()) {
                if (vt != null && vt.getMaVaiTro() != null) {
                    set.add(vt.getMaVaiTro().trim().toUpperCase());
                }
            }
        }
        if (user.getDanhSachVaiTroEnum() != null) {
            for (VaiTroEnum vte : user.getDanhSachVaiTroEnum()) {
                if (vte != null) {
                    set.add(vte.getMaVaiTro().trim().toUpperCase());
                }
            }
        }
        if (set.isEmpty()) {
            vn.nhom10.crm.dto.NguoiDungDTO dto = vn.nhom10.crm.dto.NguoiDungDTO.tuNguoiDung(user);
            if (dto != null && dto.getVaiTro() != null) {
                set.add(dto.getVaiTro().getMaVaiTro().trim().toUpperCase());
            }
        }
        return set;
    }

    /**
     * So sánh 2 phạm vi dữ liệu: CA_NHAN (1) < NHOM (2) < TOAN_BO (3).
     */
    private int soSanhPhamVi(PhamViDuLieu a, PhamViDuLieu b) {
        int valA = getScopeWeight(a);
        int valB = getScopeWeight(b);
        return Integer.compare(valA, valB);
    }

    private int getScopeWeight(PhamViDuLieu p) {
        if (p == null) return 0;
        switch (p) {
            case CA_NHAN: return 1;
            case NHOM: return 2;
            case TOAN_BO: return 3;
            default: return 0;
        }
    }
}
