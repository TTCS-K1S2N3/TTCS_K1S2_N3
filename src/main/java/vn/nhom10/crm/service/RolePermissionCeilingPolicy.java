package vn.nhom10.crm.service;

import vn.nhom10.crm.model.MucQuyen;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.VaiTroEnum;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Chính sách trần quyền tối đa (Ceilings) bất biến của hệ thống theo Sheet 2. User Roles.
 * Định nghĩa 70 cặp (7 Vai trò x 10 Module Canonical).
 *
 * Nhiệm vụ:
 * 1. Khóa trần cấp quyền phía Server-side: Admin KHÔNG THỂ cấp vượt trần dù sửa request trực tiếp.
 * 2. Bảo vệ Runtime: Effective permission không bao giờ vượt trần kể cả khi DB có dữ liệu cũ sai.
 * 3. Hỗ trợ UI: Cung cấp thông tin để disable checkbox/select và cảnh báo cấu hình vượt trần.
 */
public class RolePermissionCeilingPolicy {

    public static class CeilingRule {
        private final MucQuyen maxLevel;
        private final PhamViDuLieu maxScope;
        private final Set<PhamViDuLieu> allowedScopes;

        public CeilingRule(MucQuyen maxLevel, PhamViDuLieu maxScope, Set<PhamViDuLieu> allowedScopes) {
            this.maxLevel = maxLevel != null ? maxLevel : MucQuyen.NONE;
            this.maxScope = maxScope;
            this.allowedScopes = allowedScopes != null ? Collections.unmodifiableSet(allowedScopes) : Collections.emptySet();
        }

        public MucQuyen getMaxLevel() {
            return maxLevel;
        }

        public PhamViDuLieu getMaxScope() {
            return maxScope;
        }

        public Set<PhamViDuLieu> getAllowedScopes() {
            return allowedScopes;
        }

        public boolean isLevelAllowed(MucQuyen level) {
            if (level == null || level == MucQuyen.NONE) {
                return true;
            }
            return level.getCapDo() <= maxLevel.getCapDo();
        }

        public boolean isScopeAllowed(PhamViDuLieu scope) {
            if (scope == null) {
                return true; // Không áp dụng phạm vi luôn được chấp nhận nếu level cho phép
            }
            if (maxLevel == MucQuyen.NONE) {
                return false;
            }
            return allowedScopes.contains(scope);
        }
    }

    private static final RolePermissionCeilingPolicy INSTANCE = new RolePermissionCeilingPolicy();

    private final Map<String, Map<String, CeilingRule>> matrix = new HashMap<>();

    public static RolePermissionCeilingPolicy getInstance() {
        return INSTANCE;
    }

    public RolePermissionCeilingPolicy() {
        khoiTaoMaTranTranQuyen();
    }

    private void khoiTaoMaTranTranQuyen() {
        Set<PhamViDuLieu> scopeCaNhan = Collections.singleton(PhamViDuLieu.CA_NHAN);
        Set<PhamViDuLieu> scopeNhom = Collections.unmodifiableSet(EnumSet.of(PhamViDuLieu.CA_NHAN, PhamViDuLieu.NHOM));
        Set<PhamViDuLieu> scopeToanBo = Collections.unmodifiableSet(EnumSet.of(PhamViDuLieu.CA_NHAN, PhamViDuLieu.NHOM, PhamViDuLieu.TOAN_BO));
        Set<PhamViDuLieu> scopeNone = Collections.emptySet();

        // 1. ADMIN: Full toàn bộ 10 module, phạm vi TOÀN BỘ
        dangKy("ADMIN", "DANH_MUC", MucQuyen.FULL, PhamViDuLieu.TOAN_BO, scopeToanBo);
        dangKy("ADMIN", "KHACH_HANG", MucQuyen.FULL, PhamViDuLieu.TOAN_BO, scopeToanBo);
        dangKy("ADMIN", "LEAD", MucQuyen.FULL, PhamViDuLieu.TOAN_BO, scopeToanBo);
        dangKy("ADMIN", "CO_HOI", MucQuyen.FULL, PhamViDuLieu.TOAN_BO, scopeToanBo);
        dangKy("ADMIN", "HOAT_DONG", MucQuyen.FULL, PhamViDuLieu.TOAN_BO, scopeToanBo);
        dangKy("ADMIN", "BAO_GIA_HOP_DONG", MucQuyen.FULL, PhamViDuLieu.TOAN_BO, scopeToanBo);
        dangKy("ADMIN", "KPI", MucQuyen.FULL, PhamViDuLieu.TOAN_BO, scopeToanBo);
        dangKy("ADMIN", "BAO_CAO", MucQuyen.FULL, PhamViDuLieu.TOAN_BO, scopeToanBo);
        dangKy("ADMIN", "TU_DONG_HOA", MucQuyen.FULL, PhamViDuLieu.TOAN_BO, scopeToanBo);
        dangKy("ADMIN", "NGUOI_DUNG_NHAT_KY", MucQuyen.FULL, PhamViDuLieu.TOAN_BO, scopeToanBo);

        // 2. DIRECTOR: Full mọi module kinh doanh, riêng NGUOI_DUNG_NHAT_KY trần chỉ là READ
        dangKy("DIRECTOR", "DANH_MUC", MucQuyen.FULL, PhamViDuLieu.TOAN_BO, scopeToanBo);
        dangKy("DIRECTOR", "KHACH_HANG", MucQuyen.FULL, PhamViDuLieu.TOAN_BO, scopeToanBo);
        dangKy("DIRECTOR", "LEAD", MucQuyen.FULL, PhamViDuLieu.TOAN_BO, scopeToanBo);
        dangKy("DIRECTOR", "CO_HOI", MucQuyen.FULL, PhamViDuLieu.TOAN_BO, scopeToanBo);
        dangKy("DIRECTOR", "HOAT_DONG", MucQuyen.FULL, PhamViDuLieu.TOAN_BO, scopeToanBo);
        dangKy("DIRECTOR", "BAO_GIA_HOP_DONG", MucQuyen.FULL, PhamViDuLieu.TOAN_BO, scopeToanBo);
        dangKy("DIRECTOR", "KPI", MucQuyen.FULL, PhamViDuLieu.TOAN_BO, scopeToanBo);
        dangKy("DIRECTOR", "BAO_CAO", MucQuyen.FULL, PhamViDuLieu.TOAN_BO, scopeToanBo);
        dangKy("DIRECTOR", "TU_DONG_HOA", MucQuyen.FULL, PhamViDuLieu.TOAN_BO, scopeToanBo);
        dangKy("DIRECTOR", "NGUOI_DUNG_NHAT_KY", MucQuyen.READ, PhamViDuLieu.TOAN_BO, scopeToanBo);

        // 3. TEAM_LEAD: Trần tối đa NHOM cho các module có *; trần F tại KHACH_HANG/LEAD/CO_HOI/HOAT_DONG
        dangKy("TEAM_LEAD", "DANH_MUC", MucQuyen.READ, null, scopeNone);
        dangKy("TEAM_LEAD", "KHACH_HANG", MucQuyen.FULL, PhamViDuLieu.NHOM, scopeNhom);
        dangKy("TEAM_LEAD", "LEAD", MucQuyen.FULL, PhamViDuLieu.NHOM, scopeNhom);
        dangKy("TEAM_LEAD", "CO_HOI", MucQuyen.FULL, PhamViDuLieu.NHOM, scopeNhom);
        dangKy("TEAM_LEAD", "HOAT_DONG", MucQuyen.FULL, PhamViDuLieu.NHOM, scopeNhom);
        dangKy("TEAM_LEAD", "BAO_GIA_HOP_DONG", MucQuyen.WRITE, PhamViDuLieu.NHOM, scopeNhom);
        dangKy("TEAM_LEAD", "KPI", MucQuyen.WRITE, PhamViDuLieu.NHOM, scopeNhom);
        dangKy("TEAM_LEAD", "BAO_CAO", MucQuyen.READ, PhamViDuLieu.NHOM, scopeNhom);
        dangKy("TEAM_LEAD", "TU_DONG_HOA", MucQuyen.READ, PhamViDuLieu.NHOM, scopeNhom);
        dangKy("TEAM_LEAD", "NGUOI_DUNG_NHAT_KY", MucQuyen.NONE, null, scopeNone);

        // 4. SALES_REP: Trần tối đa CA_NHAN cho các module có *; WRITE cho KH/Lead/Cơ hội/HĐ/Báo giá
        dangKy("SALES_REP", "DANH_MUC", MucQuyen.READ, null, scopeNone);
        dangKy("SALES_REP", "KHACH_HANG", MucQuyen.WRITE, PhamViDuLieu.CA_NHAN, scopeCaNhan);
        dangKy("SALES_REP", "LEAD", MucQuyen.WRITE, PhamViDuLieu.CA_NHAN, scopeCaNhan);
        dangKy("SALES_REP", "CO_HOI", MucQuyen.WRITE, PhamViDuLieu.CA_NHAN, scopeCaNhan);
        dangKy("SALES_REP", "HOAT_DONG", MucQuyen.WRITE, PhamViDuLieu.CA_NHAN, scopeCaNhan);
        dangKy("SALES_REP", "BAO_GIA_HOP_DONG", MucQuyen.WRITE, PhamViDuLieu.CA_NHAN, scopeCaNhan);
        dangKy("SALES_REP", "KPI", MucQuyen.READ, PhamViDuLieu.CA_NHAN, scopeCaNhan);
        dangKy("SALES_REP", "BAO_CAO", MucQuyen.READ, PhamViDuLieu.CA_NHAN, scopeCaNhan);
        dangKy("SALES_REP", "TU_DONG_HOA", MucQuyen.READ, null, scopeNone);
        dangKy("SALES_REP", "NGUOI_DUNG_NHAT_KY", MucQuyen.NONE, null, scopeNone);

        // 5. MARKETING: Lead FULL; Khách hàng/Hoạt động/Tự động hóa WRITE; Báo cáo R* CA_NHAN; BAO_GIA/KPI/NGUOI_DUNG NONE
        dangKy("MARKETING", "DANH_MUC", MucQuyen.READ, null, scopeNone);
        dangKy("MARKETING", "KHACH_HANG", MucQuyen.WRITE, PhamViDuLieu.TOAN_BO, scopeToanBo);
        dangKy("MARKETING", "LEAD", MucQuyen.FULL, PhamViDuLieu.TOAN_BO, scopeToanBo);
        dangKy("MARKETING", "CO_HOI", MucQuyen.READ, PhamViDuLieu.TOAN_BO, scopeToanBo);
        dangKy("MARKETING", "HOAT_DONG", MucQuyen.WRITE, PhamViDuLieu.TOAN_BO, scopeToanBo);
        dangKy("MARKETING", "BAO_GIA_HOP_DONG", MucQuyen.NONE, null, scopeNone);
        dangKy("MARKETING", "KPI", MucQuyen.NONE, null, scopeNone);
        dangKy("MARKETING", "BAO_CAO", MucQuyen.READ, PhamViDuLieu.CA_NHAN, scopeCaNhan);
        dangKy("MARKETING", "TU_DONG_HOA", MucQuyen.WRITE, PhamViDuLieu.TOAN_BO, scopeToanBo);
        dangKy("MARKETING", "NGUOI_DUNG_NHAT_KY", MucQuyen.NONE, null, scopeNone);

        // 6. CUST_SUCCESS: Khách hàng/Hoạt động WRITE CA_NHAN; Cơ hội/Báo giá/Báo cáo/Tự động hóa READ CA_NHAN
        dangKy("CUST_SUCCESS", "DANH_MUC", MucQuyen.READ, null, scopeNone);
        dangKy("CUST_SUCCESS", "KHACH_HANG", MucQuyen.WRITE, PhamViDuLieu.CA_NHAN, scopeCaNhan);
        dangKy("CUST_SUCCESS", "LEAD", MucQuyen.NONE, null, scopeNone);
        dangKy("CUST_SUCCESS", "CO_HOI", MucQuyen.READ, PhamViDuLieu.CA_NHAN, scopeCaNhan);
        dangKy("CUST_SUCCESS", "HOAT_DONG", MucQuyen.WRITE, PhamViDuLieu.CA_NHAN, scopeCaNhan);
        dangKy("CUST_SUCCESS", "BAO_GIA_HOP_DONG", MucQuyen.READ, PhamViDuLieu.CA_NHAN, scopeCaNhan);
        dangKy("CUST_SUCCESS", "KPI", MucQuyen.NONE, null, scopeNone);
        dangKy("CUST_SUCCESS", "BAO_CAO", MucQuyen.READ, PhamViDuLieu.CA_NHAN, scopeCaNhan);
        dangKy("CUST_SUCCESS", "TU_DONG_HOA", MucQuyen.READ, null, scopeNone);
        dangKy("CUST_SUCCESS", "NGUOI_DUNG_NHAT_KY", MucQuyen.NONE, null, scopeNone);

        // 7. ACCOUNTANT: Hợp đồng WRITE; Khách hàng/Cơ hội/KPI/Báo cáo/Danh mục READ; LEAD/HOAT_DONG/TU_DONG_HOA/NGUOI_DUNG NONE
        dangKy("ACCOUNTANT", "DANH_MUC", MucQuyen.READ, null, scopeNone);
        dangKy("ACCOUNTANT", "KHACH_HANG", MucQuyen.READ, PhamViDuLieu.TOAN_BO, scopeToanBo);
        dangKy("ACCOUNTANT", "LEAD", MucQuyen.NONE, null, scopeNone);
        dangKy("ACCOUNTANT", "CO_HOI", MucQuyen.READ, PhamViDuLieu.TOAN_BO, scopeToanBo);
        dangKy("ACCOUNTANT", "HOAT_DONG", MucQuyen.NONE, null, scopeNone);
        dangKy("ACCOUNTANT", "BAO_GIA_HOP_DONG", MucQuyen.WRITE, PhamViDuLieu.TOAN_BO, scopeToanBo);
        dangKy("ACCOUNTANT", "KPI", MucQuyen.READ, PhamViDuLieu.TOAN_BO, scopeToanBo);
        dangKy("ACCOUNTANT", "BAO_CAO", MucQuyen.READ, PhamViDuLieu.TOAN_BO, scopeToanBo);
        dangKy("ACCOUNTANT", "TU_DONG_HOA", MucQuyen.NONE, null, scopeNone);
        dangKy("ACCOUNTANT", "NGUOI_DUNG_NHAT_KY", MucQuyen.NONE, null, scopeNone);
    }

    private void dangKy(String maVaiTro, String maCanonicalModule, MucQuyen maxLevel, PhamViDuLieu maxScope, Set<PhamViDuLieu> allowedScopes) {
        matrix.computeIfAbsent(maVaiTro.toUpperCase(), k -> new HashMap<>())
                .put(maCanonicalModule.toUpperCase(), new CeilingRule(maxLevel, maxScope, allowedScopes));
    }

    /**
     * Lấy quy tắc trần quyền cho cặp vai trò - module canonical.
     */
    public CeilingRule layTran(String maVaiTro, String maCanonicalModule) {
        if (maVaiTro == null || maCanonicalModule == null) {
            return new CeilingRule(MucQuyen.NONE, null, Collections.emptySet());
        }
        Map<String, CeilingRule> modMap = matrix.get(maVaiTro.trim().toUpperCase());
        if (modMap != null) {
            CeilingRule rule = modMap.get(maCanonicalModule.trim().toUpperCase());
            if (rule != null) {
                return rule;
            }
        }
        return new CeilingRule(MucQuyen.NONE, null, Collections.emptySet());
    }

    public MucQuyen layMucQuyenTran(String maVaiTro, String maCanonicalModule) {
        return layTran(maVaiTro, maCanonicalModule).getMaxLevel();
    }

    public PhamViDuLieu layPhamViTran(String maVaiTro, String maCanonicalModule) {
        return layTran(maVaiTro, maCanonicalModule).getMaxScope();
    }

    /**
     * Kiểm tra tính hợp lệ của một cấu hình đề xuất (mức quyền + phạm vi).
     * Trả về true nếu cấu hình không vượt quá trần cho phép của vai trò theo Sheet 2.
     */
    public boolean kiemTraHopLe(String maVaiTro, String maCanonicalModule, MucQuyen mucQuyen, PhamViDuLieu phamVi) {
        if (maVaiTro == null || maCanonicalModule == null) {
            return false;
        }
        // Admin row là bất biến, không thể sửa đổi
        if (VaiTroEnum.ADMIN.getMaVaiTro().equalsIgnoreCase(maVaiTro.trim())) {
            return mucQuyen == MucQuyen.FULL && phamVi == PhamViDuLieu.TOAN_BO;
        }

        CeilingRule rule = layTran(maVaiTro, maCanonicalModule);
        if (mucQuyen == null) {
            mucQuyen = MucQuyen.NONE;
        }

        // Mức quyền không được vượt trần
        if (!rule.isLevelAllowed(mucQuyen)) {
            return false;
        }

        // Nếu quyền là NONE thì phạm vi phải là null
        if (mucQuyen == MucQuyen.NONE && phamVi != null) {
            return false;
        }

        // Nếu có phạm vi thì phạm vi phải nằm trong danh sách cho phép
        if (phamVi != null && !rule.isScopeAllowed(phamVi)) {
            return false;
        }

        return true;
    }

    /**
     * Ép mức quyền và phạm vi về trần an toàn (Fail-closed) nếu bản ghi DB hiện hành vượt trần.
     */
    public MucQuyen epMucQuyenAnToan(String maVaiTro, String maCanonicalModule, MucQuyen currentLevel) {
        if (currentLevel == null) {
            return MucQuyen.NONE;
        }
        CeilingRule rule = layTran(maVaiTro, maCanonicalModule);
        if (currentLevel.getCapDo() > rule.getMaxLevel().getCapDo()) {
            return rule.getMaxLevel();
        }
        return currentLevel;
    }

    /**
     * Ép phạm vi về trần an toàn nếu bản ghi DB hiện hành vượt trần.
     */
    public PhamViDuLieu epPhamViAnToan(String maVaiTro, String maCanonicalModule, MucQuyen effectiveLevel, PhamViDuLieu currentScope) {
        if (effectiveLevel == null || effectiveLevel == MucQuyen.NONE) {
            return null;
        }
        CeilingRule rule = layTran(maVaiTro, maCanonicalModule);
        if (currentScope == null) {
            return null;
        }
        if (!rule.isScopeAllowed(currentScope)) {
            return rule.getMaxScope();
        }
        return currentScope;
    }

    /**
     * Kiểm tra xem cấu hình DB hiện tại có đang vượt trần hay không (để cảnh báo trên UI).
     */
    public boolean coGrantVuotTran(String maVaiTro, String maCanonicalModule, MucQuyen currentLevel, PhamViDuLieu currentScope) {
        if (maVaiTro == null || maCanonicalModule == null) {
            return false;
        }
        if (VaiTroEnum.ADMIN.getMaVaiTro().equalsIgnoreCase(maVaiTro.trim())) {
            return false;
        }
        CeilingRule rule = layTran(maVaiTro, maCanonicalModule);
        if (currentLevel != null && currentLevel.getCapDo() > rule.getMaxLevel().getCapDo()) {
            return true;
        }
        if (currentScope != null && !rule.isScopeAllowed(currentScope)) {
            return true;
        }
        return false;
    }
}
