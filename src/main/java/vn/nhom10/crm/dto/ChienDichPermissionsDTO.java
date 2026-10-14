package vn.nhom10.crm.dto;

import java.io.Serializable;

/**
 * DTO quyền hạn giao diện Chiến dịch (Story S4-03).
 * Khớp đúng cấu trúc CampaignPermissions trong Contract FE-BE (mục 3.1 & 3.2).
 */
public class ChienDichPermissionsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean canView = false;
    private boolean canCreate = false;
    private boolean canViewTotals = false;

    public ChienDichPermissionsDTO() {
    }

    public ChienDichPermissionsDTO(boolean canView, boolean canCreate, boolean canViewTotals) {
        this.canView = canView;
        this.canCreate = canCreate;
        this.canViewTotals = canViewTotals;
    }

    public boolean isCanView() {
        return canView;
    }

    public void setCanView(boolean canView) {
        this.canView = canView;
    }

    public boolean isCanCreate() {
        return canCreate;
    }

    public void setCanCreate(boolean canCreate) {
        this.canCreate = canCreate;
    }

    public boolean isCanViewTotals() {
        return canViewTotals;
    }

    public void setCanViewTotals(boolean canViewTotals) {
        this.canViewTotals = canViewTotals;
    }
}
