package vn.nhom10.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dao.GopKhachHangDAO;
import vn.nhom10.crm.dao.PhanQuyenDuLieuDAO;
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO;
import vn.nhom10.crm.dto.CapKhachHangTrungDTO;
import vn.nhom10.crm.dto.KetQuaGopKhachHangDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.dto.SoSanhKhachHangDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử nghiệp vụ Service Cảnh báo và Gộp khách hàng trùng lặp (Story S3-04)")
class GopKhachHangServiceTest {

    private GopKhachHangDAO gopDAO;
    private PhanQuyenDuLieuDAO phanQuyenDAO;
    private PhanQuyenDuLieuService phanQuyenService;
    private NhatKyThayDoiService nhatKyService;
    private GopKhachHangService service;

    private NguoiDungDTO userAdmin;
    private NguoiDungDTO userDirector;
    private NguoiDungDTO userTeamLeadBac;
    private NguoiDungDTO userSalesRepA;

    @BeforeEach
    void setUp() {
        gopDAO = mock(GopKhachHangDAO.class);
        phanQuyenDAO = mock(PhanQuyenDuLieuDAO.class);
        phanQuyenService = new PhanQuyenDuLieuService();
        nhatKyService = mock(NhatKyThayDoiService.class);

        service = new GopKhachHangService(gopDAO, phanQuyenDAO, phanQuyenService, nhatKyService);

        // Khởi tạo các user với vai trò khác nhau
        userAdmin = new NguoiDungDTO(1L, "Admin Quản trị", "admin@crm.vn", VaiTroEnum.ADMIN, null, "Quản trị", PhamViDuLieu.TOAN_BO);
        userDirector = new NguoiDungDTO(2L, "Giám đốc Kinh doanh", "director@crm.vn", VaiTroEnum.DIRECTOR, null, "Ban Giám đốc", PhamViDuLieu.TOAN_BO);
        userTeamLeadBac = new NguoiDungDTO(100L, "Trưởng nhóm Bắc", "lead@crm.vn", VaiTroEnum.TEAM_LEAD, 1L, "Nhóm Miền Bắc", PhamViDuLieu.NHOM);
        userSalesRepA = new NguoiDungDTO(101L, "Nguyễn Văn A", "a@crm.vn", VaiTroEnum.SALES_REP, 1L, "Nhóm Miền Bắc", PhamViDuLieu.CA_NHAN);
    }

    @Test
    @DisplayName("AC4 Phân quyền: Kiểm tra laTruongNhomTroLen chuẩn xác theo vai trò")
    void testPhanQuyen_LaTruongNhomTroLen() {
        assertTrue(service.laTruongNhomTroLen(userAdmin), "ADMIN phải được phép gộp");
        assertTrue(service.laTruongNhomTroLen(userDirector), "DIRECTOR phải được phép gộp");
        assertTrue(service.laTruongNhomTroLen(userTeamLeadBac), "TEAM_LEAD phải được phép gộp");
        assertFalse(service.laTruongNhomTroLen(userSalesRepA), "SALES_REP tuyệt đối không được phép gộp (AC4)");

        // Kiểm tra đối tượng NguoiDung chuẩn
        NguoiDung ndLead = new NguoiDung();
        ndLead.themVaiTro(VaiTroEnum.TEAM_LEAD);
        assertTrue(service.laTruongNhomTroLen(ndLead));

        NguoiDung ndSales = new NguoiDung();
        ndSales.themVaiTro(VaiTroEnum.SALES_REP);
        assertFalse(service.laTruongNhomTroLen(ndSales));
    }

    @Test
    @DisplayName("AC4 Phân quyền: Nhân viên Sales Rep cố tình thực hiện gộp bị từ chối")
    void testThucHienGop_SalesRep_BiTuChoi() {
        KetQuaGopKhachHangDTO kq = service.thucHienGopKhachHang(13L, 1L, "Lý do gộp", userSalesRepA, "127.0.0.1", "Browser");

        assertFalse(kq.isThanhCong(), "Sales Rep không được phép gộp");
        assertTrue(kq.getThongBao().contains("Chỉ Trưởng nhóm kinh doanh trở lên"));
        verifyNoInteractions(gopDAO);
    }

    @Test
    @DisplayName("Validation: Không thể gộp khi ID nguồn hoặc đích rỗng hoặc trùng nhau")
    void testThucHienGop_Validation_ThamSoKhongHopLe() {
        KetQuaGopKhachHangDTO kq1 = service.thucHienGopKhachHang(null, 1L, "Lý do", userTeamLeadBac, "127.0.0.1", "Browser");
        assertFalse(kq1.isThanhCong());
        assertTrue(kq1.getThongBao().contains("Vui lòng chọn đầy đủ"));

        KetQuaGopKhachHangDTO kq2 = service.thucHienGopKhachHang(1L, 1L, "Lý do", userTeamLeadBac, "127.0.0.1", "Browser");
        assertFalse(kq2.isThanhCong());
        assertTrue(kq2.getThongBao().contains("Không thể gộp một khách hàng vào chính nó"));

        KetQuaGopKhachHangDTO kq3 = service.thucHienGopKhachHang(13L, 1L, "   ", userTeamLeadBac, "127.0.0.1", "Browser");
        assertFalse(kq3.isThanhCong());
        assertTrue(kq3.getThongBao().contains("Vui lòng nhập lý do"));
    }

    private BanGhiNghiepVuDTO taoKhachHangMau(Long id, String ma, String ten, String mst, String website, Long nguoiSoHuuId, String tenNguoiSoHuu, Long nhomId) {
        BanGhiNghiepVuDTO bg = new BanGhiNghiepVuDTO();
        bg.setId(id);
        bg.setMaBanGhi(ma);
        bg.setTieuDe(ten);
        bg.setLoaiNghiepVu(BanGhiNghiepVuDTO.LoaiNghiepVu.KHACH_HANG);
        bg.setMaSoThue(mst);
        bg.setWebsite(website);
        bg.setNguoiPhuTrachId(nguoiSoHuuId);
        bg.setTenNguoiPhuTrach(tenNguoiSoHuu);
        bg.setNhomKinhDoanhId(nhomId);
        bg.setTenNhom("Nhóm Miền Bắc");
        bg.setTrangThai("Tiềm năng");
        return bg;
    }

    @Test
    @DisplayName("AC1 Phát hiện trùng lặp: Quét và trả về đúng danh sách các cặp trùng")
    void testPhatHienTrungLap_DanhSach() {
        BanGhiNghiepVuDTO kh1 = taoKhachHangMau(1L, "KH-001", "Công ty Cổ phần Công nghệ FPT", "0101248141", "fpt.com.vn", 101L, "Nguyễn Văn A (Sales)", 1L);
        BanGhiNghiepVuDTO kh13 = taoKhachHangMau(13L, "KH-004", "CTCP Cong Nghe FPT - Chi nhanh", "0101248141", "fpt.com.vn", 102L, "Trần Thị B (Sales)", 1L);
        BanGhiNghiepVuDTO khKhac = taoKhachHangMau(2L, "KH-002", "Tập đoàn Viettel", "0100109106", "viettel.vn", 101L, "Nguyễn Văn A (Sales)", 1L);

        List<BanGhiNghiepVuDTO> list = List.of(kh1, kh13, khKhac);
        List<CapKhachHangTrungDTO> dsTrung = service.phatHienTrungLap(list);

        assertNotNull(dsTrung);
        assertFalse(dsTrung.isEmpty(), "Danh sách phải chứa các cặp khách hàng trùng lặp để kiểm thử");

        // Tìm cặp FPT (KH-001 và KH-004)
        boolean coCapFpt = dsTrung.stream().anyMatch(c ->
                (c.getBanGhiA().getId().equals(1L) && c.getBanGhiB().getId().equals(13L)) ||
                (c.getBanGhiA().getId().equals(13L) && c.getBanGhiB().getId().equals(1L))
        );
        assertTrue(coCapFpt, "Phải phát hiện cặp trùng FPT giữa Sales A và Sales B");
    }

    @Test
    @DisplayName("AC2 So sánh cạnh nhau: Chuẩn bị đầy đủ thông tin hai bản ghi trước khi gộp")
    void testLayDuLieuSoSanh_CungCapThongTinDayDu() throws Exception {
        BanGhiNghiepVuDTO kh1 = taoKhachHangMau(1L, "KH-001", "Công ty Cổ phần Công nghệ FPT", "0101248141", "fpt.com.vn", 101L, "Nguyễn Văn A (Sales)", 1L);
        BanGhiNghiepVuDTO kh13 = taoKhachHangMau(13L, "KH-004", "CTCP Cong Nghe FPT - Chi nhanh", "0101248141", "fpt.com.vn", 102L, "Trần Thị B (Sales)", 1L);

        when(gopDAO.layChiTietKhachHang(1L)).thenReturn(kh1);
        when(gopDAO.layChiTietKhachHang(13L)).thenReturn(kh13);

        GopKhachHangDAO.ThongKeLienQuan tk = new GopKhachHangDAO.ThongKeLienQuan();
        tk.soNguoiLienHe = 3;
        tk.soCoHoi = 2;
        tk.soHoatDong = 5;
        tk.soTepDinhKem = 1;
        when(gopDAO.layThongKeLienQuan(anyLong())).thenReturn(tk);

        SoSanhKhachHangDTO soSanh = service.layDuLieuSoSanh(13L, 1L, userTeamLeadBac);

        assertNotNull(soSanh);
        assertEquals(13L, soSanh.getKhachHangNguon().getId());
        assertEquals(1L, soSanh.getKhachHangDich().getId());
        assertEquals(3, soSanh.getSoLuongNguoiLienHeNguon());
        assertEquals(2, soSanh.getSoLuongCoHoiNguon());
        assertEquals(5, soSanh.getSoLuongHoatDongNguon());
        assertTrue(soSanh.isTrungMst(), "Cặp này phải trùng MST");
        assertTrue(soSanh.isXungDotNhanVien(), "Phải cảnh báo 2 nhân viên khác nhau cùng chào công ty");
    }

    @Test
    @DisplayName("AC3 Gộp thành công: Trưởng nhóm thực hiện gộp chuyển giao bảo toàn dữ liệu và ghi kiểm toán")
    void testThucHienGop_TruongNhom_ThanhCong() throws Exception {
        BanGhiNghiepVuDTO kh1 = taoKhachHangMau(1L, "KH-001", "Công ty Cổ phần Công nghệ FPT", "0101248141", "fpt.com.vn", 101L, "Nguyễn Văn A (Sales)", 1L);
        BanGhiNghiepVuDTO kh13 = taoKhachHangMau(13L, "KH-004", "CTCP Cong Nghe FPT - Chi nhanh", "0101248141", "fpt.com.vn", 102L, "Trần Thị B (Sales)", 1L);

        when(gopDAO.layChiTietKhachHang(1L)).thenReturn(kh1);
        when(gopDAO.layChiTietKhachHang(13L)).thenReturn(kh13);

        KetQuaGopKhachHangDTO mockKq = KetQuaGopKhachHangDTO.thanhCong("Gộp thành công", 13L, 1L);
        mockKq.setSoNguoiLienHeDaChuyen(2);
        mockKq.setSoCoHoiDaChuyen(1);
        mockKq.setSoHoatDongDaChuyen(3);
        mockKq.setSoTepDinhKemDaChuyen(1);

        when(gopDAO.thucHienGop(eq(13L), eq(1L), anyLong(), anyString(), anyString())).thenReturn(mockKq);

        KetQuaGopKhachHangDTO result = service.thucHienGopKhachHang(
                13L, 1L, "Thống nhất 1 đầu mối chăm sóc", userTeamLeadBac, "127.0.0.1", "Mozilla"
        );

        assertTrue(result.isThanhCong(), "Gộp phải thành công");
        assertTrue(result.getThongBao().contains("thành công"));
        assertEquals(2, result.getSoNguoiLienHeDaChuyen());
        assertEquals(1, result.getSoCoHoiDaChuyen());
        assertEquals(3, result.getSoHoatDongDaChuyen());

        // Kiểm tra gọi ghi nhật ký kiểm toán
        verify(nhatKyService).ghiNhatKyThayDoi(
                eq(100L), anyString(), anyString(), any(), any(), any(), any(), any(), any(), any(), anyString(), anyString(), anyString()
        );
    }
}
