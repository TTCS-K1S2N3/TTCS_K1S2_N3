package vn.nhom10.crm.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.model.PhamViDuLieu;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử BoLocLeadDTO - Serialization & Validation (Story S4-09)")
class BoLocLeadDTOTest {

    @Test
    @DisplayName("toJson & tuJson: Chuyển đổi hai chiều đầy đủ 5 nhóm tiêu chí lọc Lead")
    void testToJsonVaTuJson_DayDu5TieuChi() {
        BoLocLeadDTO goc = new BoLocLeadDTO();
        goc.setTrangThai("MOI");
        goc.setNguonLeadId(2L);
        goc.setPhanLoai("NONG");
        goc.setNguoiPhuTrachId(101L);
        goc.setTuNgay("2026-10-01");
        goc.setDenNgay("2026-10-15");
        goc.setPhamVi(PhamViDuLieu.CA_NHAN);

        String json = goc.toJson();
        assertNotNull(json);
        assertTrue(json.contains("\"trangThai\":\"MOI\""));
        assertTrue(json.contains("\"nguonLeadId\":2"));
        assertTrue(json.contains("\"phanLoai\":\"NONG\""));
        assertTrue(json.contains("\"nguoiPhuTrachId\":101"));
        assertTrue(json.contains("\"tuNgay\":\"2026-10-01\""));
        assertTrue(json.contains("\"denNgay\":\"2026-10-15\""));
        assertTrue(json.contains("\"phamVi\":\"CA_NHAN\""));

        BoLocLeadDTO phucHoi = BoLocLeadDTO.tuJson(json);
        assertEquals("MOI", phucHoi.getTrangThai());
        assertEquals(2L, phucHoi.getNguonLeadId());
        assertEquals("NONG", phucHoi.getPhanLoai());
        assertEquals(101L, phucHoi.getNguoiPhuTrachId());
        assertEquals("2026-10-01", phucHoi.getTuNgay());
        assertEquals("2026-10-15", phucHoi.getDenNgay());
        assertEquals(PhamViDuLieu.CA_NHAN, phucHoi.getPhamVi());
        assertTrue(phucHoi.coDieuKienLoc());
        assertEquals(5, phucHoi.soDieuKienLoc());
    }

    @Test
    @DisplayName("toJson & tuJson: Đối tượng rỗng chuyển đổi an toàn")
    void testToJsonVaTuJson_DoiTuongRong() {
        BoLocLeadDTO goc = new BoLocLeadDTO();
        String json = goc.toJson();
        assertEquals("{}", json);

        BoLocLeadDTO phucHoi = BoLocLeadDTO.tuJson(json);
        assertNotNull(phucHoi);
        assertFalse(phucHoi.coDieuKienLoc());
        assertEquals(0, phucHoi.soDieuKienLoc());
        assertNull(phucHoi.getTrangThai());
        assertNull(phucHoi.getNguonLeadId());
    }

    @Test
    @DisplayName("coDieuKienLoc: Nhận diện chính xác khi có ít nhất một tiêu chí lọc được chọn")
    void testCoDieuKienLoc() {
        BoLocLeadDTO dto = new BoLocLeadDTO();
        assertFalse(dto.coDieuKienLoc());

        dto.setTrangThai("MOI");
        assertTrue(dto.coDieuKienLoc());

        dto = new BoLocLeadDTO();
        dto.setPhanLoai("NONG");
        assertTrue(dto.coDieuKienLoc());

        dto = new BoLocLeadDTO();
        dto.setTuNgay("2026-10-01");
        assertTrue(dto.coDieuKienLoc());

        dto = new BoLocLeadDTO();
        dto.setSavedFilterId(5L);
        assertTrue(dto.coDieuKienLoc());
    }
}
