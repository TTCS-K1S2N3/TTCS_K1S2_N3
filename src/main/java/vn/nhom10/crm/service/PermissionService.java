package vn.nhom10.crm.service;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    // Bộ nhớ đệm phân quyền để tối ưu hiệu năng runtime, tự động xóa khi có cập nhật
    private final Map<String, Map<String, VaiTroModule>> cacheMatrix = new ConcurrentHashMap<>();

    public static PermissionService getInstance() {
        return INSTANCE;
    }

    public PermissionService() {
        this(new VaiTroModuleDAO(), new VaiTroDAO(), new NhatKyThayDoiService());
    }

    public PermissionService(VaiTroModuleDAO vaiTroModuleDAO, VaiTroDAO vaiTroDAO) {
        this(vaiTroModuleDAO, vaiTroDAO, new NhatKyThayDoiService());
    }

    public PermissionService(VaiTroModuleDAO vaiTroModuleDAO, VaiTroDAO vaiTroDAO, NhatKyThayDoiService nhatKyThayDoiService) {
        this.vaiTroModuleDAO = vaiTroModuleDAO != null ? vaiTroModuleDAO : new VaiTroModuleDAO();
        this.vaiTroDAO = vaiTroDAO != null ? vaiTroDAO : new VaiTroDAO();
        this.nhatKyThayDoiService = nhatKyThayDoiService != null ? nhatKyThayDoiService : new NhatKyThayDoiService();
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
     * Cập nhật phân quyền cho một vai trò từ quản trị viên.
     * Có kiểm tra xác thực phía server nghiêm ngặt:
     * - Role tồn tại trong hệ thống.
     * - Tuyệt đối không cho phép chỉnh sửa hoặc hạ quyền vai trò ADMIN.
     * - Mức quyền chỉ được trong 4 giá trị NONE/READ/WRITE/FULL.
     * - Phạm vi dữ liệu không được vượt quá pham_vi_toi_da của vai trò.
     * - Không được vượt trần tối đa theo quy định của Sheet 2 User Roles.
     * - Thực thi trong transaction duy nhất.
     */
    public boolean capNhatMatrixChoVaiTro(String maVaiTro, List<VaiTroModule> danhSachMoi, int nguoiThucHienId, String diaChiIp, String thietBi) {
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

        boolean thanhCong = vaiTroModuleDAO.capNhatToanBoChoVaiTro(vaiTro.getId(), danhSachMoi);
        if (thanhCong) {
            // Xóa cache để các request tiếp theo truy vấn dữ liệu mới nhất từ DB
            xoaCache();
            ghiAuditThayDoi(vaiTro, danhSachMoi, nguoiThucHienId, diaChiIp, thietBi);
        }
        return thanhCong;
    }

    private void ghiAuditThayDoi(VaiTro vaiTro, List<VaiTroModule> danhSachMoi, int nguoiThucHienId, String diaChiIp, String thietBi) {
        try {
            NguoiDung actor = vaiTroDAO.timTheoId(nguoiThucHienId) != null ? null : null; // service logging
            nhatKyThayDoiService.ghiNhatKyThayDoi(
                    (long) nguoiThucHienId,
                    "Quản trị hệ thống",
                    "admin@crm.vn",
                    LoaiDoiTuongNhayCam.VAI_TRO_NGUOI_DUNG,
                    vaiTro.getMaVaiTro(),
                    "Ma trận phân quyền: " + vaiTro.getTenVaiTro(),
                    "vai_tro_module",
                    "Phân quyền cũ",
                    "Phân quyền mới (" + danhSachMoi.size() + " module)",
                    HanhDongThayDoi.CAP_NHAT,
                    "Cập nhật ma trận phân quyền vai trò " + vaiTro.getTenVaiTro(),
                    diaChiIp != null ? diaChiIp : "127.0.0.1",
                    thietBi != null ? thietBi : "Trình duyệt CRM"
            );
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Ghi nhận audit log phân quyền: " + e.getMessage());
        }
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
