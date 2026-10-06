package vn.nhom10.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dao.KhachHangDAO;
import vn.nhom10.crm.dto.BoLocKhachHangDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.model.*;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử KhachHangService - Nghiệp vụ tìm kiếm, lọc và phân quyền Data Scope (Story S3-07)")
class KhachHangServiceTest {

    private KhachHangDAO khachHangDAO;
    private KhachHangService service;

    private NguoiDung userA;
    private NguoiDungDTO userDTOA;

    @BeforeEach
    void setUp() {
        khachHangDAO = mock(KhachHangDAO.class);
        service = new KhachHangService(khachHangDAO);

        userA = new NguoiDung(101L, "Nguyễn Văn A", "sales.a@crm.vn");
        userA.setNhomKinhDoanhId(1);
        userA.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.SALES_REP, PhamViDuLieu.CA_NHAN)));
        userDTOA = NguoiDungDTO.tuNguoiDung(userA);
    }

    @Test
    @DisplayName("AC1 & AC2: timKiemVaLoc gọi đúng DAO với bộ lọc và người dùng")
    void testTimKiemVaLoc_GoiDAO() {
        BoLocKhachHangDTO boLoc = new BoLocKhachHangDTO();
        boLoc.setTrangThai("TIEM_NANG");
        boLoc.setTenCongTy("FPT");

        KhachHang kh = new KhachHang(1L, "Tập đoàn FPT", 101L);
        when(khachHangDAO.timKiemVaLoc(eq(userDTOA), any(BoLocKhachHangDTO.class)))
                .thenReturn(Collections.singletonList(kh));

        List<KhachHang> result = service.timKiemVaLoc(userDTOA, boLoc);
        assertEquals(1, result.size());
        assertEquals("Tập đoàn FPT", result.get(0).getTenCongTy());

        verify(khachHangDAO).timKiemVaLoc(eq(userDTOA), any(BoLocKhachHangDTO.class));
    }

    @Test
    @DisplayName("Data Scope: Người dùng chỉ có quyền CA_NHAN khi yêu cầu TOAN_BO bị ép về phạm vi an toàn")
    void testTimKiemVaLoc_EpDataScopeAnToan() {
        BoLocKhachHangDTO boLoc = new BoLocKhachHangDTO();
        boLoc.setPhamVi(PhamViDuLieu.TOAN_BO); // Cố tình chọn Toàn bộ

        service.timKiemVaLoc(userDTOA, boLoc);

        assertEquals(PhamViDuLieu.CA_NHAN, boLoc.getPhamVi(),
                "Sales Rep chỉ có quyền CA_NHAN, phạm vi yêu cầu phải bị ép về CA_NHAN");
    }

    @Test
    @DisplayName("AC3: timTheoId trả về khách hàng khi người dùng có quyền sở hữu")
    void testTimTheoId_DungQuyen_TraVeKhachHang() {
        KhachHang kh = new KhachHang(1L, "Khách hàng của A", 101L);
        kh.setNhomKinhDoanhId(1L);
        when(khachHangDAO.timTheoId(1L)).thenReturn(kh);

        KhachHang result = service.timTheoId(1L, userDTOA);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("AC3 Bảo mật: timTheoId từ chối trả về khách hàng của nhân viên khác (ngoài phạm vi CA_NHAN)")
    void testTimTheoId_SaiQuyen_TraVeNull() {
        KhachHang khB = new KhachHang(2L, "Khách hàng của B", 102L); // Thuộc sở hữu của B (102)
        khB.setNhomKinhDoanhId(1L);
        when(khachHangDAO.timTheoId(2L)).thenReturn(khB);

        KhachHang result = service.timTheoId(2L, userDTOA);
        assertNull(result, "Sales Rep A không được xem chi tiết khách hàng của Sales Rep B");
    }

    @Test
    @DisplayName("Validation: Tạo khách hàng bắt buộc phải có tên công ty")
    void testTaoKhachHang_TenCongTyRong_NemLoi() {
        KhachHang kh = new KhachHang();
        kh.setTenCongTy("");

        assertThrows(IllegalArgumentException.class, () ->
                service.taoKhachHang(userA, kh));
    }

    @Test
    @DisplayName("Validation: Tạo khách hàng trùng mã số thuế bị từ chối")
    void testTaoKhachHang_TrungMaSoThue_NemLoi() {
        KhachHang kh = new KhachHang();
        kh.setTenCongTy("Công ty Trùng MST");
        kh.setMaSoThue("0101234567");

        when(khachHangDAO.kiemTraTonTaiMaSoThue("0101234567", null)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.taoKhachHang(userA, kh));
        assertTrue(ex.getMessage().contains("đã tồn tại"));
    }

    @Test
    @DisplayName("Bảo mật: Sales Rep tạo khách hàng luôn tự động gán người sở hữu là chính mình")
    void testTaoKhachHang_Sales_TuDongGanNguoiSoHuu() throws SQLException {
        KhachHang kh = new KhachHang();
        kh.setTenCongTy("Công ty Mới");
        kh.setNguoiSoHuuId(999L); // Kẻ xấu cố tình truyền ID người khác

        when(khachHangDAO.themKhachHang(any(KhachHang.class))).thenReturn(20L);

        KhachHang taoMoi = service.taoKhachHang(userA, kh);
        assertEquals(101L, taoMoi.getNguoiSoHuuId(), "Chủ sở hữu phải tự động gán từ session user A");
    }
}
