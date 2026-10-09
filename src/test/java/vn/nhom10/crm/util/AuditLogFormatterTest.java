package vn.nhom10.crm.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử định dạng dữ liệu Audit Log cho UI (Story S2-04)")
class AuditLogFormatterTest {

    @BeforeEach
    void setUp() {
        AuditLogFormatter.setCacheTenNhom(Map.of(
                "1", "Khối Kinh Doanh",
                "2", "Kinh doanh Miền Bắc",
                "3", "Kinh doanh Miền Nam"
        ));
    }

    @AfterEach
    void tearDown() {
        AuditLogFormatter.xoaCacheTenNhom();
    }

    @Test
    @DisplayName("S2-04: Định dạng vai trò đơn lẻ trong mảng JSON [\"ACCOUNTANT\"] => Kế toán")
    void testFormatRoleAccountant() {
        String input = "[\"ACCOUNTANT\"]";
        String formatted = AuditLogFormatter.dinhDangGiaTri("vai_tro", input);
        assertEquals("Kế toán", formatted, "Phải hiển thị 'Kế toán' thay vì mã kỹ thuật [\"ACCOUNTANT\"]");
    }

    @Test
    @DisplayName("S2-04: Định dạng nhiều vai trò [\"ADMIN\",\"ACCOUNTANT\"] => Quản trị hệ thống, Kế toán")
    void testFormatRoleAdminAndAccountant() {
        String input = "[\"ADMIN\",\"ACCOUNTANT\"]";
        String formatted = AuditLogFormatter.dinhDangGiaTri("vai_tro", input);
        assertEquals("Quản trị hệ thống, Kế toán", formatted);

        // Kiểm tra biến thể có khoảng trắng trong JSON
        String inputWithSpace = "[\"ADMIN\", \"ACCOUNTANT\"]";
        assertEquals("Quản trị hệ thống, Kế toán", AuditLogFormatter.dinhDangGiaTri("vai_tro", inputWithSpace));
    }

    @Test
    @DisplayName("S2-04: Định dạng mã vai trò đơn lẻ TEAM_LEAD => Trưởng nhóm kinh doanh")
    void testFormatRoleTeamLead() {
        String input = "TEAM_LEAD";
        String formatted = AuditLogFormatter.dinhDangGiaTri("vai_tro", input);
        assertEquals("Trưởng nhóm kinh doanh", formatted);
    }

    @Test
    @DisplayName("S2-04: Mảng rỗng [] => giá trị thân thiện 'Không có'")
    void testFormatEmptyArray() {
        String formatted = AuditLogFormatter.dinhDangGiaTri("vai_tro", "[]");
        assertNotNull(formatted);
        assertNotEquals("[]", formatted);
        assertFalse(formatted.contains("null"));
        assertTrue(formatted.equals("Không có") || formatted.equals("Chưa thiết lập"));
    }

    @Test
    @DisplayName("S2-04: Giá trị null hoặc chuỗi rỗng => giá trị thân thiện 'Không có', tuyệt đối không hiển thị chữ 'null'")
    void testFormatNullAndBlank() {
        String fromNull = AuditLogFormatter.dinhDangGiaTri("vai_tro", null);
        assertNotNull(fromNull);
        assertFalse(fromNull.equalsIgnoreCase("null"));
        assertTrue(fromNull.equals("Không có") || fromNull.equals("Chưa thiết lập"));

        String fromNullString = AuditLogFormatter.dinhDangGiaTri("vai_tro", "null");
        assertNotNull(fromNullString);
        assertFalse(fromNullString.equalsIgnoreCase("null"));
        assertTrue(fromNullString.equals("Không có") || fromNullString.equals("Chưa thiết lập"));

        String fromBlank = AuditLogFormatter.dinhDangGiaTri("vai_tro", "   ");
        assertNotNull(fromBlank);
        assertTrue(fromBlank.equals("Không có") || fromBlank.equals("Chưa thiết lập"));
    }

    @Test
    @DisplayName("S2-04: Mã vai trò không xác định => không crash, hiển thị an toàn")
    void testFormatUnknownRoleDoesNotCrash() {
        assertDoesNotThrow(() -> {
            String formatted = AuditLogFormatter.dinhDangGiaTri("vai_tro", "CUSTOM_UNKNOWN_ROLE");
            assertNotNull(formatted);
            assertEquals("CUSTOM_UNKNOWN_ROLE", formatted);
        });

        assertDoesNotThrow(() -> {
            String formatted = AuditLogFormatter.dinhDangGiaTri("vai_tro", "[\"UNKNOWN_1\",\"ACCOUNTANT\"]");
            assertNotNull(formatted);
            assertEquals("UNKNOWN_1, Kế toán", formatted);
        });
    }

    @Test
    @DisplayName("S2-04: Trường kỹ thuật vai_tro => 'Vai trò'")
    void testFormatTechnicalFieldVaiTro() {
        assertEquals("Vai trò", AuditLogFormatter.dinhDangTruong("vai_tro"));
        assertEquals("Vai trò", AuditLogFormatter.dinhDangTruong("nguoi_dung_vai_tro"));
    }

    @Test
    @DisplayName("S2-04: Các trường kỹ thuật khác: ho_ten, email, so_dien_thoai, nhom_kinh_doanh_id, trang_thai")
    void testFormatOtherTechnicalFields() {
        assertEquals("Họ và tên", AuditLogFormatter.dinhDangTruong("ho_ten"));
        assertEquals("Email", AuditLogFormatter.dinhDangTruong("email"));
        assertEquals("Số điện thoại", AuditLogFormatter.dinhDangTruong("so_dien_thoai"));
        assertEquals("Nhóm kinh doanh", AuditLogFormatter.dinhDangTruong("nhom_kinh_doanh_id"));
        assertEquals("Trạng thái", AuditLogFormatter.dinhDangTruong("trang_thai"));
    }

    @Test
    @DisplayName("S2-04: Nhóm kinh doanh ID 2 -> 3 hiển thị tên nhóm thân thiện")
    void testFormatTeamBusinessNames() {
        assertEquals("Kinh doanh Miền Bắc", AuditLogFormatter.dinhDangGiaTri("nhom_kinh_doanh_id", "2"));
        assertEquals("Kinh doanh Miền Nam", AuditLogFormatter.dinhDangGiaTri("nhom_kinh_doanh_id", "3"));
        assertEquals("Kinh doanh Miền Bắc -> Kinh doanh Miền Nam",
                AuditLogFormatter.dinhDangGiaTri("nhom_kinh_doanh_id", "2 -> 3"));
    }

    @Test
    @DisplayName("S2-04: Tất cả vai trò trong VaiTroEnum đều được format đúng sang tiếng Việt")
    void testAllEnumRolesMapped() {
        assertEquals("Quản trị hệ thống", AuditLogFormatter.dinhDangGiaTri("vai_tro", "ADMIN"));
        assertEquals("Giám đốc kinh doanh", AuditLogFormatter.dinhDangGiaTri("vai_tro", "DIRECTOR"));
        assertEquals("Trưởng nhóm kinh doanh", AuditLogFormatter.dinhDangGiaTri("vai_tro", "TEAM_LEAD"));
        assertEquals("Nhân viên kinh doanh", AuditLogFormatter.dinhDangGiaTri("vai_tro", "SALES_REP"));
        assertEquals("Nhân viên Marketing", AuditLogFormatter.dinhDangGiaTri("vai_tro", "MARKETING"));
        assertEquals("Chăm sóc khách hàng", AuditLogFormatter.dinhDangGiaTri("vai_tro", "CUST_SUCCESS"));
        assertEquals("Kế toán", AuditLogFormatter.dinhDangGiaTri("vai_tro", "ACCOUNTANT"));
    }
}
