package vn.nhom10.crm.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.model.PhamViDuLieu;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử BoLocKhachHangDTO - Serialization & Validation (Story S3-07)")
class BoLocKhachHangDTOTest {

    @Test
    @DisplayName("toJson & tuJson: Chuyển đổi hai chiều đầy đủ tất cả các trường tiêu chí lọc và tìm kiếm")
    void testToJsonVaTuJson_DayDuTruong() {
        BoLocKhachHangDTO goc = new BoLocKhachHangDTO();
        goc.setTrangThai("TIEM_NANG");
        goc.setNganhNgheId(1L);
        goc.setQuyMoId(2L);
        goc.setKhuVucId(3L);
        goc.setNguoiSoHuuId(101L);
        goc.setTuKhoa("công nghệ");
        goc.setTenCongTy("FPT Software");
        goc.setMaSoThue("0101234567");
        goc.setSoDienThoai("0987654321");
        goc.setPhamVi(PhamViDuLieu.CA_NHAN);

        String json = goc.toJson();
        assertNotNull(json);
        assertTrue(json.contains("\"trangThai\":\"TIEM_NANG\""));
        assertTrue(json.contains("\"nganhNgheId\":1"));
        assertTrue(json.contains("\"quyMoId\":2"));
        assertTrue(json.contains("\"khuVucId\":3"));
        assertTrue(json.contains("\"nguoiSoHuuId\":101"));
        assertTrue(json.contains("\"tuKhoa\":\"công nghệ\""));
        assertTrue(json.contains("\"tenCongTy\":\"FPT Software\""));
        assertTrue(json.contains("\"maSoThue\":\"0101234567\""));
        assertTrue(json.contains("\"soDienThoai\":\"0987654321\""));
        assertTrue(json.contains("\"phamVi\":\"CA_NHAN\""));

        BoLocKhachHangDTO phucHoi = BoLocKhachHangDTO.tuJson(json);
        assertEquals("TIEM_NANG", phucHoi.getTrangThai());
        assertEquals(1L, phucHoi.getNganhNgheId());
        assertEquals(2L, phucHoi.getQuyMoId());
        assertEquals(3L, phucHoi.getKhuVucId());
        assertEquals(101L, phucHoi.getNguoiSoHuuId());
        assertEquals("công nghệ", phucHoi.getTuKhoa());
        assertEquals("FPT Software", phucHoi.getTenCongTy());
        assertEquals("0101234567", phucHoi.getMaSoThue());
        assertEquals("0987654321", phucHoi.getSoDienThoai());
        assertEquals(PhamViDuLieu.CA_NHAN, phucHoi.getPhamVi());
    }

    @Test
    @DisplayName("toJson & tuJson: Ký tự đặc biệt và dấu ngoặc kép được escape an toàn")
    void testToJson_EscapeKyTuDacBiet() {
        BoLocKhachHangDTO goc = new BoLocKhachHangDTO();
        goc.setTenCongTy("Công ty \"ABC\" & đối tác");
        goc.setTuKhoa("tìm \"test\"\nxuống dòng");

        String json = goc.toJson();
        BoLocKhachHangDTO phucHoi = BoLocKhachHangDTO.tuJson(json);

        assertNotNull(phucHoi.getTenCongTy());
        assertTrue(phucHoi.getTenCongTy().contains("ABC"));
    }

    @Test
    @DisplayName("tuJson: Chuỗi JSON rỗng hoặc null trả về đối tượng mặc định không lỗi")
    void testTuJson_NullHoacRong() {
        BoLocKhachHangDTO dto1 = BoLocKhachHangDTO.tuJson(null);
        assertNotNull(dto1);
        assertFalse(dto1.coDieuKienLoc());

        BoLocKhachHangDTO dto2 = BoLocKhachHangDTO.tuJson("");
        assertNotNull(dto2);
        assertFalse(dto2.coDieuKienLoc());

        BoLocKhachHangDTO dto3 = BoLocKhachHangDTO.tuJson("{}");
        assertNotNull(dto3);
        assertFalse(dto3.coDieuKienLoc());
    }

    @Test
    @DisplayName("coDieuKienLoc: Phát hiện chính xác khi có ít nhất một tiêu chí lọc")
    void testCoDieuKienLoc() {
        BoLocKhachHangDTO dto = new BoLocKhachHangDTO();
        assertFalse(dto.coDieuKienLoc());

        dto.setTrangThai("TIEM_NANG");
        assertTrue(dto.coDieuKienLoc());

        dto.setTrangThai(null);
        dto.setSoDienThoai("0912345678");
        assertTrue(dto.coDieuKienLoc());

        dto.setSoDienThoai("");
        dto.setNganhNgheId(5L);
        assertTrue(dto.coDieuKienLoc());
    }
}
