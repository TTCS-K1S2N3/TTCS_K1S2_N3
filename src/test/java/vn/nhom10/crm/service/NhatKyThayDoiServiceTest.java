package vn.nhom10.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dao.NhatKyThayDoiDAO;
import vn.nhom10.crm.dao.NhomKinhDoanhDAO;
import vn.nhom10.crm.dao.VaiTroDAO;
import vn.nhom10.crm.dto.BoLocNhatKyDTO;
import vn.nhom10.crm.dto.GanVaiTroNhomDTO;
import vn.nhom10.crm.dto.KetQuaPhanTrangDTO;
import vn.nhom10.crm.dto.NguoiDungOptionDTO;
import vn.nhom10.crm.dto.NhatKyThayDoiDTO;
import vn.nhom10.crm.dto.ThongKeNhatKyDTO;
import vn.nhom10.crm.model.HanhDongThayDoi;
import vn.nhom10.crm.model.LoaiDoiTuongNhayCam;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhatKyThayDoi;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Kiểm thử toàn diện NhatKyThayDoiService - Story S2-04")
class NhatKyThayDoiServiceTest {

    @Mock
    private NhatKyThayDoiDAO mockDAO;

    private NhatKyThayDoiService service;

    @BeforeEach
    void setUp() {
        service = new NhatKyThayDoiService(mockDAO);
    }

    @Test
    @DisplayName("DB trống: GET danh sách không tự nạp log mẫu, trả về danh sách rỗng (Empty State)")
    void testDbTrongKhongSinhLogMau() {
        when(mockDAO.demTongSoBanGhi(any())).thenReturn(0L);

        BoLocNhatKyDTO boLoc = new BoLocNhatKyDTO();
        KetQuaPhanTrangDTO<NhatKyThayDoiDTO> ketQua = service.timKiemNhatKy(boLoc);

        assertNotNull(ketQua);
        assertNotNull(ketQua.getDanhSach());
        assertTrue(ketQua.getDanhSach().isEmpty(), "CSDL trống thì danh sách phải rỗng hoàn toàn, không được có demo data");
        assertEquals(0, ketQua.getTongSoBanGhi());

        // Đảm bảo không có lệnh INSERT ghi log mẫu tự động nào được gọi
        verify(mockDAO, never()).ghiNhatKy(any(NhatKyThayDoi.class));
        verify(mockDAO, never()).layDanhSach(any());
    }

    @Test
    @DisplayName("DB lỗi: fail-safe trả về danh sách rỗng, không sập 500, không fallback dữ liệu giả")
    void testDbLoiKhongFallbackDuLieuGia() {
        when(mockDAO.demTongSoBanGhi(any())).thenThrow(new RuntimeException("Database connection timeout"));

        BoLocNhatKyDTO boLoc = new BoLocNhatKyDTO();
        KetQuaPhanTrangDTO<NhatKyThayDoiDTO> ketQua = service.timKiemNhatKy(boLoc);

        assertNotNull(ketQua);
        assertNotNull(ketQua.getDanhSach());
        assertTrue(ketQua.getDanhSach().isEmpty(), "Khi DB lỗi phải trả về danh sách rỗng, không được fallback log mẫu");
        assertEquals(0, ketQua.getTongSoBanGhi());
    }

    @Test
    @DisplayName("Filter người dùng: truyền đúng điều kiện lọc vào DAO")
    void testFilterNguoiDung() {
        BoLocNhatKyDTO boLoc = new BoLocNhatKyDTO();
        boLoc.setNguoiDungId(101L);

        NhatKyThayDoi model = new NhatKyThayDoi();
        model.setId(1L);
        model.setNguoiThucHienId(101L);
        model.setTenNguoiThucHien("Lê Minh Tuấn");
        model.setLoaiDoiTuong(LoaiDoiTuongNhayCam.CHI_TIEU);
        model.setTruongThayDoi("Chỉ tiêu");
        model.setGiaTriTruoc("500tr");
        model.setGiaTriSau("350tr");
        model.setCreatedAt(LocalDateTime.now());

        when(mockDAO.demTongSoBanGhi(boLoc)).thenReturn(1L);
        when(mockDAO.layDanhSach(boLoc)).thenReturn(Collections.singletonList(model));

        KetQuaPhanTrangDTO<NhatKyThayDoiDTO> ketQua = service.timKiemNhatKy(boLoc);
        assertEquals(1, ketQua.getDanhSach().size());
        assertEquals(101L, ketQua.getDanhSach().get(0).getNguoiThucHienId());
        verify(mockDAO).demTongSoBanGhi(boLoc);
        verify(mockDAO).layDanhSach(boLoc);
    }

    @Test
    @DisplayName("Filter loại đối tượng: truyền đúng loại nhạy cảm vào DAO")
    void testFilterLoaiDoiTuong() {
        BoLocNhatKyDTO boLoc = new BoLocNhatKyDTO();
        boLoc.setLoaiDoiTuong("CHIET_KHAU");

        NhatKyThayDoi model = new NhatKyThayDoi();
        model.setId(2L);
        model.setLoaiDoiTuong(LoaiDoiTuongNhayCam.CHIET_KHAU);
        model.setTruongThayDoi("Tỷ lệ chiết khấu");
        model.setGiaTriTruoc("10%");
        model.setGiaTriSau("25%");
        model.setCreatedAt(LocalDateTime.now());

        when(mockDAO.demTongSoBanGhi(boLoc)).thenReturn(1L);
        when(mockDAO.layDanhSach(boLoc)).thenReturn(Collections.singletonList(model));

        KetQuaPhanTrangDTO<NhatKyThayDoiDTO> ketQua = service.timKiemNhatKy(boLoc);
        assertEquals(1, ketQua.getDanhSach().size());
        assertEquals(LoaiDoiTuongNhayCam.CHIET_KHAU, ketQua.getDanhSach().get(0).getLoaiDoiTuong());
        verify(mockDAO).layDanhSach(boLoc);
    }

    @Test
    @DisplayName("Filter khoảng ngày: truyền đúng từ ngày - đến ngày vào DAO")
    void testFilterKhoangNgay() {
        BoLocNhatKyDTO boLoc = new BoLocNhatKyDTO();
        boLoc.setTuNgay(LocalDate.of(2026, 9, 1));
        boLoc.setDenNgay(LocalDate.of(2026, 9, 30));

        when(mockDAO.demTongSoBanGhi(boLoc)).thenReturn(0L);

        KetQuaPhanTrangDTO<NhatKyThayDoiDTO> ketQua = service.timKiemNhatKy(boLoc);
        assertNotNull(ketQua);
        verify(mockDAO).demTongSoBanGhi(boLoc);
    }

    @Test
    @DisplayName("Tính bất biến (Immutability): Service và DAO không có API sửa hoặc xóa nhật ký")
    void testAuditImmutability() {
        Method[] serviceMethods = NhatKyThayDoiService.class.getDeclaredMethods();
        for (Method m : serviceMethods) {
            String name = m.getName().toLowerCase();
            assertFalse(name.contains("update"), "Service không được có phương thức update audit log: " + m.getName());
            assertFalse(name.contains("delete"), "Service không được có phương thức delete audit log: " + m.getName());
            assertFalse(name.contains("sua"), "Service không được có phương thức sửa audit log: " + m.getName());
            assertFalse(name.contains("xoa"), "Service không được có phương thức xóa audit log: " + m.getName());
        }

        Method[] daoMethods = NhatKyThayDoiDAO.class.getDeclaredMethods();
        for (Method m : daoMethods) {
            String name = m.getName().toLowerCase();
            assertFalse(name.contains("update"), "DAO không được có phương thức update audit log: " + m.getName());
            assertFalse(name.contains("delete"), "DAO không được có phương thức delete audit log: " + m.getName());
            assertFalse(name.contains("xoa"), "DAO không được có phương thức xóa audit log: " + m.getName());
        }
    }

    @Test
    @DisplayName("Bảo mật dữ liệu nhạy cảm: Tự động redact mật khẩu, token, secret")
    void testRedactionSensitiveData() {
        NhatKyThayDoi nk = new NhatKyThayDoi();
        nk.setTruongThayDoi("mat_khau");
        nk.setGiaTriTruoc("SuperSecret123!");
        nk.setGiaTriSau("NewSecret456@");

        assertEquals("******", nk.getGiaTriTruoc(), "Mật khẩu cũ phải được redact");
        assertEquals("******", nk.getGiaTriSau(), "Mật khẩu mới phải được redact");

        // Kiểm tra với chuỗi JSON chứa secret/token
        String rawJson = "{\"token\":\"xyz123secret\",\"status\":\"ACTIVE\"}";
        String safeJson = NhatKyThayDoi.cheGiaTriNhayCam(null, rawJson);
        assertFalse(safeJson.contains("xyz123secret"), "Token bí mật không được lộ trong JSON");
        assertTrue(safeJson.contains("\"token\":\"******\""));
    }

    @Test
    @DisplayName("Chi tiết bản ghi theo ID: ID không hợp lệ hoặc không tồn tại trả về null")
    void testLayChiTietId() {
        assertNull(service.layChiTiet(-1L));
        assertNull(service.layChiTiet(0L));

        when(mockDAO.timTheoId(999L)).thenReturn(null);
        assertNull(service.layChiTiet(999L));

        NhatKyThayDoi model = new NhatKyThayDoi();
        model.setId(10L);
        model.setMaDoiTuong("BG-088");
        model.setTruongThayDoi("Chiết khấu");
        model.setGiaTriTruoc("10%");
        model.setGiaTriSau("20%");
        model.setCreatedAt(LocalDateTime.now());

        when(mockDAO.timTheoId(10L)).thenReturn(model);
        NhatKyThayDoiDTO dto = service.layChiTiet(10L);
        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("BG-088", dto.getMaDoiTuong());
    }

    @Test
    @DisplayName("Audit Write Hook: Phân quyền thay đổi vai trò người dùng -> tự động tạo bản ghi nhật ký")
    void testWriteHookPhanQuyenNguoiDung() throws SQLException {
        NguoiDungDAO mockUserDAO = mock(NguoiDungDAO.class);
        VaiTroDAO mockRoleDAO = mock(VaiTroDAO.class);
        NhomKinhDoanhDAO mockTeamDAO = mock(NhomKinhDoanhDAO.class);

        NguoiDung targetUser = new NguoiDung(5, "Trần Văn Nam", "nam.tv@crm.vn");
        targetUser.themVaiTro(new VaiTro(4, "SALES_REP", "Nhân viên kinh doanh", "Sales Rep"));

        NguoiDung adminActor = new NguoiDung(1, "Admin Tổng", "admin@crm.vn");
        adminActor.themVaiTro(new VaiTro(1, "ADMIN", "Quản trị hệ thống", "Admin"));

        when(mockUserDAO.timTheoId(5)).thenReturn(targetUser);
        when(mockUserDAO.timTheoId(1)).thenReturn(adminActor);

        VaiTro roleLeader = new VaiTro(3, "TEAM_LEAD", "Trưởng nhóm kinh doanh", "Team Lead");
        when(mockRoleDAO.timTheoId(3)).thenReturn(roleLeader);
        when(mockTeamDAO.timTheoId(2)).thenReturn(new vn.nhom10.crm.model.NhomKinhDoanh(2, "KD1", "Nhóm 1", "Mô tả", 1));
        when(mockUserDAO.capNhatVaiTroVaNhomTransaction(eq(5), anyList(), eq(2))).thenReturn(true);

        when(mockDAO.ghiNhatKy(any(NhatKyThayDoi.class))).thenReturn(101L);

        PhanQuyenService phanQuyenService = new PhanQuyenService(mockUserDAO, mockRoleDAO, mockTeamDAO, service);

        GanVaiTroNhomDTO result = phanQuyenService.ganVaiTroVaNhomKinhDoanh(
                5, Collections.singletonList(3), 2, 1, "192.168.1.50", "Firefox/Linux");

        assertTrue(result.isThanhCong());

        // Kiểm tra audit hook đã được gọi và ghi đúng thông tin actor, target, before, after
        ArgumentCaptor<NhatKyThayDoi> captor = ArgumentCaptor.forClass(NhatKyThayDoi.class);
        verify(mockDAO).ghiNhatKy(captor.capture());

        NhatKyThayDoi captured = captor.getValue();
        assertEquals(1L, captured.getNguoiThucHienId(), "Actor phải là ID 1");
        assertEquals("Admin Tổng", captured.getTenNguoiThucHien());
        assertEquals(LoaiDoiTuongNhayCam.VAI_TRO_NGUOI_DUNG, captured.getLoaiDoiTuong());
        assertEquals("ND-5", captured.getMaDoiTuong());
        assertTrue(captured.getGiaTriTruoc().contains("Nhân viên kinh doanh"), "Giá trị trước phải có vai trò cũ");
        assertTrue(captured.getGiaTriSau().contains("Trưởng nhóm kinh doanh"), "Giá trị sau phải có vai trò mới");
        assertEquals("192.168.1.50", captured.getDiaChiIp());
        assertEquals("Firefox/Linux", captured.getThietBi());
    }
}
