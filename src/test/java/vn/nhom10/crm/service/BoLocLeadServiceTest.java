package vn.nhom10.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử BoLocLeadService - Lọc Lead và Lưu bộ lọc cá nhân (Story S4-09)")
class BoLocLeadServiceTest {

    private LeadDAO leadDAO;
    private BoLocDaLuuDAO boLocDaLuuDAO;
    private DanhMucBanHangDAO danhMucBanHangDAO;
    private NguoiDungDAO nguoiDungDAO;
    private PermissionService permissionService;

    private BoLocLeadService service;
    private NguoiDung user;

    @BeforeEach
    void setUp() {
        leadDAO = mock(LeadDAO.class);
        boLocDaLuuDAO = mock(BoLocDaLuuDAO.class);
        danhMucBanHangDAO = mock(DanhMucBanHangDAO.class);
        nguoiDungDAO = mock(NguoiDungDAO.class);
        permissionService = mock(PermissionService.class);

        service = new BoLocLeadService(leadDAO, boLocDaLuuDAO, danhMucBanHangDAO, nguoiDungDAO, permissionService);

        user = new NguoiDung();
        user.setId(101L);
        user.setHoTen("Nguyễn Văn An");
        user.setEmail("sales.an@crm.vn");
    }

    @Test
    @DisplayName("AC1 & AC2: timKiemLead lấy danh sách lead theo Data Scope an toàn")
    void testTimKiemLead_DataScope() {
        BoLocLeadDTO filter = new BoLocLeadDTO();
        filter.setTrangThai("MOI");

        when(permissionService.layPhamViHieuLuc(user, "LEAD", MucQuyen.READ)).thenReturn(PhamViDuLieu.CA_NHAN);

        Lead leadMock = new Lead();
        leadMock.setId(1L);
        leadMock.setMaLead("LEAD-001");
        leadMock.setHoTen("Khách A");
        leadMock.setQuaSla(true);

        when(leadDAO.layDanhSachLead(eq(filter), eq(user), eq(PhamViDuLieu.CA_NHAN), eq(100), eq(0)))
                .thenReturn(Collections.singletonList(leadMock));

        List<Lead> results = service.timKiemLead(filter, user, 100, 0);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("LEAD-001", results.get(0).getMaLead());
        assertTrue(results.get(0).isQuaSla());

        verify(leadDAO).layDanhSachLead(eq(filter), eq(user), eq(PhamViDuLieu.CA_NHAN), eq(100), eq(0));
    }

    @Test
    @DisplayName("AC1: demTongSoLead đếm số lead chính xác")
    void testDemTongSoLead() {
        BoLocLeadDTO filter = new BoLocLeadDTO();
        when(permissionService.layPhamViHieuLuc(user, "LEAD", MucQuyen.READ)).thenReturn(PhamViDuLieu.CA_NHAN);
        when(leadDAO.demTongSoLead(eq(filter), eq(user), eq(PhamViDuLieu.CA_NHAN))).thenReturn(15);

        int count = service.demTongSoLead(filter, user);
        assertEquals(15, count);
    }

    @Test
    @DisplayName("AC3: luuBoLoc lưu thành công khi tên và tiêu chí hợp lệ")
    void testLuuBoLoc_ThanhCong() throws SQLException {
        BoLocLeadDTO filter = new BoLocLeadDTO();
        filter.setPhanLoai("NONG");
        filter.setTrangThai("MOI");

        when(boLocDaLuuDAO.luuBoLoc(any(BoLocDaLuu.class))).thenReturn(88L);

        BoLocDaLuu saved = service.luuBoLoc(user, "Lead Nóng mới sáng nay", filter, false);

        assertNotNull(saved);
        assertEquals(88L, saved.getId());
        assertEquals("Lead Nóng mới sáng nay", saved.getTenBoLoc());

        ArgumentCaptor<BoLocDaLuu> captor = ArgumentCaptor.forClass(BoLocDaLuu.class);
        verify(boLocDaLuuDAO).luuBoLoc(captor.capture());
        BoLocDaLuu captured = captor.getValue();
        assertEquals(101L, captured.getNguoiDungId());
        assertEquals("LEAD", captured.getLoaiDoiTuong());
        assertTrue(captured.getTieuChiJson().contains("\"phanLoai\":\"NONG\""));
        assertTrue(captured.getTieuChiJson().contains("\"trangThai\":\"MOI\""));
    }

    @Test
    @DisplayName("AC3: luuBoLoc từ chối khi tên rỗng hoặc vượt quá 80 ký tự theo baseline Q4")
    void testLuuBoLoc_TenKhongHopLe() {
        BoLocLeadDTO filter = new BoLocLeadDTO();

        // Tên rỗng
        assertThrows(IllegalArgumentException.class, () -> service.luuBoLoc(user, "   ", filter, false));

        // Tên quá 80 ký tự
        String tenQuaDai = "A".repeat(81);
        assertThrows(IllegalArgumentException.class, () -> service.luuBoLoc(user, tenQuaDai, filter, false));
    }

    @Test
    @DisplayName("AC3: layDanhSachBoLoc lấy đúng danh sách bộ lọc loại LEAD của người dùng")
    void testLayDanhSachBoLoc() {
        BoLocDaLuu bl = new BoLocDaLuu();
        bl.setId(10L);
        bl.setTenBoLoc("Bộ lọc test");
        when(boLocDaLuuDAO.layDanhSachTheoNguoiDung(101L, "LEAD")).thenReturn(Collections.singletonList(bl));

        List<BoLocDaLuu> list = service.layDanhSachBoLoc(user);
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Bộ lọc test", list.get(0).getTenBoLoc());
    }

    @Test
    @DisplayName("layDanhSachNguonLead trả về danh mục nguồn từ DanhMucBanHangDAO")
    void testLayDanhSachNguonLead() {
        MucDanhMucDTO dto = new MucDanhMucDTO();
        dto.setId(1L);
        dto.setTenMuc("Website");
        when(danhMucBanHangDAO.layDanhSach(LoaiDanhMuc.NGUON_LEAD, true)).thenReturn(Collections.singletonList(dto));

        List<MucDanhMucDTO> list = service.layDanhSachNguonLead();
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Website", list.get(0).getTenMuc());
    }
}
