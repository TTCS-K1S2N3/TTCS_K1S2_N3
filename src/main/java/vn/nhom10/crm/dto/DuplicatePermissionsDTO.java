package vn.nhom10.crm.dto;

import java.io.Serializable;

/**
 * DTO quyền thao tác phát hiện trùng, gắn khách hàng và gộp Lead (Story S4-04).
 */
public class DuplicatePermissionsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean canViewCandidates = false;
    private boolean canLinkCustomer = false;
    private boolean canMerge = false;

    public DuplicatePermissionsDTO() {
    }

    public DuplicatePermissionsDTO(boolean canViewCandidates, boolean canLinkCustomer, boolean canMerge) {
        this.canViewCandidates = canViewCandidates;
        this.canLinkCustomer = canLinkCustomer;
        this.canMerge = canMerge;
    }

    public boolean isCanViewCandidates() {
        return canViewCandidates;
    }

    public void setCanViewCandidates(boolean canViewCandidates) {
        this.canViewCandidates = canViewCandidates;
    }

    public boolean isCanLinkCustomer() {
        return canLinkCustomer;
    }

    public void setCanLinkCustomer(boolean canLinkCustomer) {
        this.canLinkCustomer = canLinkCustomer;
    }

    public boolean isCanMerge() {
        return canMerge;
    }

    public void setCanMerge(boolean canMerge) {
        this.canMerge = canMerge;
    }
}
