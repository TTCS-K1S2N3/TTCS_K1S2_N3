package vn.nhom10.crm.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.model.MucQuyen;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.service.RolePermissionCeilingPolicy.CeilingRule;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử trần phân quyền Sheet 2 (70 cặp Vai trò x Module) và cơ chế chống leo quyền")
class RolePermissionCeilingPolicyTest {

    private final RolePermissionCeilingPolicy policy = RolePermissionCeilingPolicy.getInstance();

    @Test
    @DisplayName("Sheet 2 Ma trận 70 cặp: Kiểm tra đúng toàn bộ trần quyền của 7 vai trò trên 10 module canonical")
    void test70CapTranQuyenChuanSheet2() {
        // --- 1. ADMIN: Toàn quyền FULL (TOAN_BO) trên 10 module ---
        String[] allModules = {"DANH_MUC", "KHACH_HANG", "LEAD", "CO_HOI", "HOAT_DONG", "BAO_GIA_HOP_DONG", "KPI", "BAO_CAO", "TU_DONG_HOA", "NGUOI_DUNG_NHAT_KY"};
        for (String mod : allModules) {
            CeilingRule rule = policy.layTran("ADMIN", mod);
            assertEquals(MucQuyen.FULL, rule.getMaxLevel(), "ADMIN trần phải là FULL trên " + mod);
            assertEquals(PhamViDuLieu.TOAN_BO, rule.getMaxScope(), "ADMIN scope phải là TOAN_BO trên " + mod);
        }

        // --- 2. DIRECTOR: FULL trên 9 module kinh doanh, riêng NGUOI_DUNG_NHAT_KY là READ (TOAN_BO) ---
        assertEquals(MucQuyen.FULL, policy.layTran("DIRECTOR", "DANH_MUC").getMaxLevel());
        assertEquals(MucQuyen.FULL, policy.layTran("DIRECTOR", "KHACH_HANG").getMaxLevel());
        assertEquals(MucQuyen.FULL, policy.layTran("DIRECTOR", "LEAD").getMaxLevel());
        assertEquals(MucQuyen.FULL, policy.layTran("DIRECTOR", "CO_HOI").getMaxLevel());
        assertEquals(MucQuyen.FULL, policy.layTran("DIRECTOR", "HOAT_DONG").getMaxLevel());
        assertEquals(MucQuyen.FULL, policy.layTran("DIRECTOR", "BAO_GIA_HOP_DONG").getMaxLevel());
        assertEquals(MucQuyen.FULL, policy.layTran("DIRECTOR", "KPI").getMaxLevel());
        assertEquals(MucQuyen.FULL, policy.layTran("DIRECTOR", "BAO_CAO").getMaxLevel());
        assertEquals(MucQuyen.FULL, policy.layTran("DIRECTOR", "TU_DONG_HOA").getMaxLevel());
        CeilingRule dirAudit = policy.layTran("DIRECTOR", "NGUOI_DUNG_NHAT_KY");
        assertEquals(MucQuyen.READ, dirAudit.getMaxLevel(), "DIRECTOR trần NGUOI_DUNG_NHAT_KY chỉ là READ");
        assertEquals(PhamViDuLieu.TOAN_BO, dirAudit.getMaxScope());

        // --- 3. TEAM_LEAD: KHACH_HANG/LEAD/CO_HOI/HOAT_DONG trần là FULL (NHOM) ---
        assertEquals(MucQuyen.READ, policy.layTran("TEAM_LEAD", "DANH_MUC").getMaxLevel());
        assertNull(policy.layTran("TEAM_LEAD", "DANH_MUC").getMaxScope());

        CeilingRule tlKh = policy.layTran("TEAM_LEAD", "KHACH_HANG");
        assertEquals(MucQuyen.FULL, tlKh.getMaxLevel());
        assertEquals(PhamViDuLieu.NHOM, tlKh.getMaxScope());

        CeilingRule tlLead = policy.layTran("TEAM_LEAD", "LEAD");
        assertEquals(MucQuyen.FULL, tlLead.getMaxLevel());
        assertEquals(PhamViDuLieu.NHOM, tlLead.getMaxScope());

        CeilingRule tlCoHoi = policy.layTran("TEAM_LEAD", "CO_HOI");
        assertEquals(MucQuyen.FULL, tlCoHoi.getMaxLevel());
        assertEquals(PhamViDuLieu.NHOM, tlCoHoi.getMaxScope());

        CeilingRule tlHoatDong = policy.layTran("TEAM_LEAD", "HOAT_DONG");
        assertEquals(MucQuyen.FULL, tlHoatDong.getMaxLevel());
        assertEquals(PhamViDuLieu.NHOM, tlHoatDong.getMaxScope());

        assertEquals(MucQuyen.WRITE, policy.layTran("TEAM_LEAD", "BAO_GIA_HOP_DONG").getMaxLevel());
        assertEquals(PhamViDuLieu.NHOM, policy.layTran("TEAM_LEAD", "BAO_GIA_HOP_DONG").getMaxScope());
        assertEquals(MucQuyen.WRITE, policy.layTran("TEAM_LEAD", "KPI").getMaxLevel());
        assertEquals(PhamViDuLieu.NHOM, policy.layTran("TEAM_LEAD", "KPI").getMaxScope());
        assertEquals(MucQuyen.READ, policy.layTran("TEAM_LEAD", "BAO_CAO").getMaxLevel());
        assertEquals(PhamViDuLieu.NHOM, policy.layTran("TEAM_LEAD", "BAO_CAO").getMaxScope());
        assertEquals(MucQuyen.READ, policy.layTran("TEAM_LEAD", "TU_DONG_HOA").getMaxLevel());
        assertEquals(MucQuyen.NONE, policy.layTran("TEAM_LEAD", "NGUOI_DUNG_NHAT_KY").getMaxLevel());

        // --- 4. SALES_REP: KHACH_HANG/LEAD/CO_HOI/HOAT_DONG/BAO_GIA_HOP_DONG trần là WRITE (CA_NHAN) ---
        assertEquals(MucQuyen.READ, policy.layTran("SALES_REP", "DANH_MUC").getMaxLevel());
        CeilingRule srKh = policy.layTran("SALES_REP", "KHACH_HANG");
        assertEquals(MucQuyen.WRITE, srKh.getMaxLevel(), "SALES_REP trần KHACH_HANG chỉ là WRITE");
        assertEquals(PhamViDuLieu.CA_NHAN, srKh.getMaxScope(), "SALES_REP phạm vi tối đa là CA_NHAN");

        assertEquals(MucQuyen.WRITE, policy.layTran("SALES_REP", "LEAD").getMaxLevel());
        assertEquals(PhamViDuLieu.CA_NHAN, policy.layTran("SALES_REP", "LEAD").getMaxScope());
        assertEquals(MucQuyen.WRITE, policy.layTran("SALES_REP", "CO_HOI").getMaxLevel());
        assertEquals(PhamViDuLieu.CA_NHAN, policy.layTran("SALES_REP", "CO_HOI").getMaxScope());
        assertEquals(MucQuyen.WRITE, policy.layTran("SALES_REP", "HOAT_DONG").getMaxLevel());
        assertEquals(PhamViDuLieu.CA_NHAN, policy.layTran("SALES_REP", "HOAT_DONG").getMaxScope());
        assertEquals(MucQuyen.WRITE, policy.layTran("SALES_REP", "BAO_GIA_HOP_DONG").getMaxLevel());
        assertEquals(PhamViDuLieu.CA_NHAN, policy.layTran("SALES_REP", "BAO_GIA_HOP_DONG").getMaxScope());

        assertEquals(MucQuyen.READ, policy.layTran("SALES_REP", "KPI").getMaxLevel());
        assertEquals(PhamViDuLieu.CA_NHAN, policy.layTran("SALES_REP", "KPI").getMaxScope());
        assertEquals(MucQuyen.READ, policy.layTran("SALES_REP", "BAO_CAO").getMaxLevel());
        assertEquals(PhamViDuLieu.CA_NHAN, policy.layTran("SALES_REP", "BAO_CAO").getMaxScope());
        assertEquals(MucQuyen.READ, policy.layTran("SALES_REP", "TU_DONG_HOA").getMaxLevel());
        assertEquals(MucQuyen.NONE, policy.layTran("SALES_REP", "NGUOI_DUNG_NHAT_KY").getMaxLevel());

        // --- 5. MARKETING: LEAD là FULL (TOAN_BO), BAO_CAO là READ (CA_NHAN), NGUOI_DUNG_NHAT_KY là NONE ---
        assertEquals(MucQuyen.READ, policy.layTran("MARKETING", "DANH_MUC").getMaxLevel());
        assertEquals(MucQuyen.WRITE, policy.layTran("MARKETING", "KHACH_HANG").getMaxLevel());
        assertEquals(PhamViDuLieu.TOAN_BO, policy.layTran("MARKETING", "KHACH_HANG").getMaxScope());
        assertEquals(MucQuyen.FULL, policy.layTran("MARKETING", "LEAD").getMaxLevel());
        assertEquals(PhamViDuLieu.TOAN_BO, policy.layTran("MARKETING", "LEAD").getMaxScope());
        assertEquals(MucQuyen.READ, policy.layTran("MARKETING", "CO_HOI").getMaxLevel());
        assertEquals(MucQuyen.WRITE, policy.layTran("MARKETING", "HOAT_DONG").getMaxLevel());
        assertEquals(MucQuyen.NONE, policy.layTran("MARKETING", "BAO_GIA_HOP_DONG").getMaxLevel());
        assertEquals(MucQuyen.NONE, policy.layTran("MARKETING", "KPI").getMaxLevel());
        CeilingRule mktBaoCao = policy.layTran("MARKETING", "BAO_CAO");
        assertEquals(MucQuyen.READ, mktBaoCao.getMaxLevel());
        assertEquals(PhamViDuLieu.CA_NHAN, mktBaoCao.getMaxScope(), "MARKETING trên BAO_CAO chỉ CA_NHAN");
        assertEquals(MucQuyen.WRITE, policy.layTran("MARKETING", "TU_DONG_HOA").getMaxLevel());
        assertEquals(MucQuyen.NONE, policy.layTran("MARKETING", "NGUOI_DUNG_NHAT_KY").getMaxLevel());

        // --- 6. CUST_SUCCESS: KHACH_HANG/HOAT_DONG trần là WRITE (CA_NHAN), LEAD/KPI/NGUOI_DUNG_NHAT_KY là NONE ---
        assertEquals(MucQuyen.READ, policy.layTran("CUST_SUCCESS", "DANH_MUC").getMaxLevel());
        assertEquals(MucQuyen.WRITE, policy.layTran("CUST_SUCCESS", "KHACH_HANG").getMaxLevel());
        assertEquals(PhamViDuLieu.CA_NHAN, policy.layTran("CUST_SUCCESS", "KHACH_HANG").getMaxScope());
        assertEquals(MucQuyen.NONE, policy.layTran("CUST_SUCCESS", "LEAD").getMaxLevel());
        assertEquals(MucQuyen.READ, policy.layTran("CUST_SUCCESS", "CO_HOI").getMaxLevel());
        assertEquals(PhamViDuLieu.CA_NHAN, policy.layTran("CUST_SUCCESS", "CO_HOI").getMaxScope());
        assertEquals(MucQuyen.WRITE, policy.layTran("CUST_SUCCESS", "HOAT_DONG").getMaxLevel());
        assertEquals(PhamViDuLieu.CA_NHAN, policy.layTran("CUST_SUCCESS", "HOAT_DONG").getMaxScope());
        assertEquals(MucQuyen.READ, policy.layTran("CUST_SUCCESS", "BAO_GIA_HOP_DONG").getMaxLevel());
        assertEquals(PhamViDuLieu.CA_NHAN, policy.layTran("CUST_SUCCESS", "BAO_GIA_HOP_DONG").getMaxScope());
        assertEquals(MucQuyen.NONE, policy.layTran("CUST_SUCCESS", "KPI").getMaxLevel());
        assertEquals(MucQuyen.READ, policy.layTran("CUST_SUCCESS", "BAO_CAO").getMaxLevel());
        assertEquals(PhamViDuLieu.CA_NHAN, policy.layTran("CUST_SUCCESS", "BAO_CAO").getMaxScope());
        assertEquals(MucQuyen.READ, policy.layTran("CUST_SUCCESS", "TU_DONG_HOA").getMaxLevel());
        assertEquals(MucQuyen.NONE, policy.layTran("CUST_SUCCESS", "NGUOI_DUNG_NHAT_KY").getMaxLevel());

        // --- 7. ACCOUNTANT: BAO_GIA_HOP_DONG là WRITE (TOAN_BO), LEAD/HOAT_DONG/TU_DONG_HOA/NGUOI_DUNG_NHAT_KY là NONE ---
        assertEquals(MucQuyen.READ, policy.layTran("ACCOUNTANT", "DANH_MUC").getMaxLevel());
        assertEquals(MucQuyen.READ, policy.layTran("ACCOUNTANT", "KHACH_HANG").getMaxLevel());
        assertEquals(MucQuyen.NONE, policy.layTran("ACCOUNTANT", "LEAD").getMaxLevel());
        assertEquals(MucQuyen.READ, policy.layTran("ACCOUNTANT", "CO_HOI").getMaxLevel());
        assertEquals(MucQuyen.NONE, policy.layTran("ACCOUNTANT", "HOAT_DONG").getMaxLevel());
        assertEquals(MucQuyen.WRITE, policy.layTran("ACCOUNTANT", "BAO_GIA_HOP_DONG").getMaxLevel());
        assertEquals(PhamViDuLieu.TOAN_BO, policy.layTran("ACCOUNTANT", "BAO_GIA_HOP_DONG").getMaxScope());
        assertEquals(MucQuyen.READ, policy.layTran("ACCOUNTANT", "KPI").getMaxLevel());
        assertEquals(MucQuyen.READ, policy.layTran("ACCOUNTANT", "BAO_CAO").getMaxLevel());
        assertEquals(MucQuyen.NONE, policy.layTran("ACCOUNTANT", "TU_DONG_HOA").getMaxLevel());
        assertEquals(MucQuyen.NONE, policy.layTran("ACCOUNTANT", "NGUOI_DUNG_NHAT_KY").getMaxLevel());
    }

    @Test
    @DisplayName("Negative Tampering: Chặn mọi nỗ lực POST cấp quyền vượt trần Sheet 2")
    void testNegativeTamperingValidation() {
        // 1. Sales Rep không thể cấp FULL trên KHACH_HANG
        assertFalse(policy.kiemTraHopLe("SALES_REP", "KHACH_HANG", MucQuyen.FULL, PhamViDuLieu.CA_NHAN));

        // 2. Sales Rep không thể cấp phạm vi NHOM hoặc TOAN_BO trên KHACH_HANG
        assertFalse(policy.kiemTraHopLe("SALES_REP", "KHACH_HANG", MucQuyen.WRITE, PhamViDuLieu.NHOM));
        assertFalse(policy.kiemTraHopLe("SALES_REP", "KHACH_HANG", MucQuyen.WRITE, PhamViDuLieu.TOAN_BO));

        // 3. Accountant không thể bật READ/WRITE trên module LEAD (trần NONE)
        assertFalse(policy.kiemTraHopLe("ACCOUNTANT", "LEAD", MucQuyen.READ, null));
        assertFalse(policy.kiemTraHopLe("ACCOUNTANT", "LEAD", MucQuyen.WRITE, PhamViDuLieu.CA_NHAN));

        // 4. Director không thể bật WRITE hoặc FULL trên NGUOI_DUNG_NHAT_KY (trần READ)
        assertFalse(policy.kiemTraHopLe("DIRECTOR", "NGUOI_DUNG_NHAT_KY", MucQuyen.WRITE, PhamViDuLieu.TOAN_BO));
        assertFalse(policy.kiemTraHopLe("DIRECTOR", "NGUOI_DUNG_NHAT_KY", MucQuyen.FULL, PhamViDuLieu.TOAN_BO));

        // 5. Team Lead không thể cấp TOAN_BO trên KHACH_HANG (trần NHOM)
        assertFalse(policy.kiemTraHopLe("TEAM_LEAD", "KHACH_HANG", MucQuyen.FULL, PhamViDuLieu.TOAN_BO));

        // 6. Marketing không thể cấp TOAN_BO trên BAO_CAO (trần CA_NHAN)
        assertFalse(policy.kiemTraHopLe("MARKETING", "BAO_CAO", MucQuyen.READ, PhamViDuLieu.TOAN_BO));

        // 7. Admin row bất biến: Không thể hạ xuống WRITE/READ/NONE
        assertFalse(policy.kiemTraHopLe("ADMIN", "KHACH_HANG", MucQuyen.WRITE, PhamViDuLieu.TOAN_BO));
        assertFalse(policy.kiemTraHopLe("ADMIN", "KHACH_HANG", MucQuyen.NONE, null));

        // 8. Quyền NONE không được đi kèm phạm vi dữ liệu
        assertFalse(policy.kiemTraHopLe("SALES_REP", "LEAD", MucQuyen.NONE, PhamViDuLieu.CA_NHAN));
    }

    @Test
    @DisplayName("Fail-closed Runtime Protection: Ép trần quyền và phạm vi an toàn khi DB grant cũ vượt trần")
    void testFailClosedRuntimeEpTran() {
        // Giả lập DB có bản ghi cũ cấp SALES_REP = FULL trên KHACH_HANG -> Ép về WRITE
        MucQuyen safeLevel = policy.epMucQuyenAnToan("SALES_REP", "KHACH_HANG", MucQuyen.FULL);
        assertEquals(MucQuyen.WRITE, safeLevel);

        // Giả lập DB có bản ghi cũ cấp SALES_REP = TOAN_BO trên KHACH_HANG -> Ép về CA_NHAN
        PhamViDuLieu safeScope = policy.epPhamViAnToan("SALES_REP", "KHACH_HANG", MucQuyen.WRITE, PhamViDuLieu.TOAN_BO);
        assertEquals(PhamViDuLieu.CA_NHAN, safeScope);

        // Giả lập DB có bản ghi cũ cấp DIRECTOR = WRITE trên NGUOI_DUNG_NHAT_KY -> Ép về READ
        assertEquals(MucQuyen.READ, policy.epMucQuyenAnToan("DIRECTOR", "NGUOI_DUNG_NHAT_KY", MucQuyen.WRITE));

        // Giả lập DB có bản ghi cũ cấp ACCOUNTANT = READ trên LEAD -> Ép về NONE
        assertEquals(MucQuyen.NONE, policy.epMucQuyenAnToan("ACCOUNTANT", "LEAD", MucQuyen.READ));
        assertNull(policy.epPhamViAnToan("ACCOUNTANT", "LEAD", MucQuyen.NONE, PhamViDuLieu.TOAN_BO));
    }

    @Test
    @DisplayName("Cảnh báo vượt trần: Phát hiện chính xác bản ghi DB không hợp lệ để cảnh báo UI")
    void testPhatHienGrantVuotTran() {
        // SALES_REP grant = FULL -> Vượt trần
        assertTrue(policy.coGrantVuotTran("SALES_REP", "KHACH_HANG", MucQuyen.FULL, PhamViDuLieu.CA_NHAN));

        // SALES_REP grant = WRITE, CA_NHAN -> Đúng trần, không vượt
        assertFalse(policy.coGrantVuotTran("SALES_REP", "KHACH_HANG", MucQuyen.WRITE, PhamViDuLieu.CA_NHAN));

        // SALES_REP grant = WRITE, TOAN_BO -> Vượt phạm vi
        assertTrue(policy.coGrantVuotTran("SALES_REP", "KHACH_HANG", MucQuyen.WRITE, PhamViDuLieu.TOAN_BO));

        // ACCOUNTANT grant = READ trên LEAD -> Vượt trần
        assertTrue(policy.coGrantVuotTran("ACCOUNTANT", "LEAD", MucQuyen.READ, null));
    }
}
