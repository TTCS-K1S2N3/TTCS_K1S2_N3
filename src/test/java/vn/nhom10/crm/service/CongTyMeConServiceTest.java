package vn.nhom10.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dao.HopDongDAO;
import vn.nhom10.crm.dao.KhachHangDAO;
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.dto.ThongKeNhomCongTyDTO;
import vn.nhom10.crm.model.HopDong;
import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.util.LoiKhongTimThayException;
import vn.nhom10.crm.util.LoiPhanQuyenException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho CongTyMeConService (Story S3-05).
 * Kiểm tra các Acceptance Criteria:
 * - AC1: Gắn một khách hàng làm công ty con của khách hàng khác
 * - AC2: Trang công ty mẹ hiển thị tổng giá trị hợp đồng của cả nhóm công ty
 * - Kiểm tra các validation: null, tự gán chính mình, vòng lặp chu kỳ, phân quyền Data Scope.
 */
public class CongTyMeConServiceTest {

    private KhachHangDAO khachHangDAO;
    private HopDongDAO hopDongDAO;
    private PhanQuyenDuLieuService phanQuyenService;
    private CongTyMeConService service;

    private NguoiDungDTO userAdmin;
    private NguoiDungDTO userSalesA;
    private KhachHang khachHangMe;
    private KhachHang khachHangCon1;
    private KhachHang khachHangCon2;

    @BeforeEach
    void setUp() {
        khachHangDAO = mock(KhachHangDAO.class);
        hopDongDAO = mock(HopDongDAO.class);
        phanQuyenService = mock(PhanQuyenDuLieuService.class);
        service = new CongTyMeConService(khachHangDAO, hopDongDAO, phanQuyenService);

        userAdmin = new NguoiDungDTO(1L, "Admin", "admin@crm.vn", vn.nhom10.crm.model.VaiTroEnum.ADMIN,
                null, null, PhamViDuLieu.TOAN_BO);

        userSalesA = new NguoiDungDTO(2L, "Sales A", "salesa@crm.vn", vn.nhom10.crm.model.VaiTroEnum.SALES_REP,
                1L, "Nhóm Kinh Doanh 1", PhamViDuLieu.CA_NHAN);

        khachHangMe = new KhachHang(10L, "Tập Đoàn Hoa Sen", 2L);
        khachHangMe.setMaKhachHang("HSG");

        khachHangCon1 = new KhachHang(20L, "Công ty TNHH Ống Thép Hoa Sen", 2L);
        khachHangCon1.setMaKhachHang("HS-STEEL");

        khachHangCon2 = new KhachHang(30L, "Công ty TNHH Nhựa Hoa Sen", 2L);
        khachHangCon2.setMaKhachHang("HS-PLASTIC");
    }

    @Test
    @DisplayName("AC1: Gắn thành công một khách hàng làm công ty con của khách hàng khác")
    void testGanCongTyCon_ThanhCong() throws Exception {
        when(khachHangDAO.timTheoId(20L)).thenReturn(khachHangCon1);
        when(khachHangDAO.timTheoId(10L)).thenReturn(khachHangMe);
        when(khachHangDAO.kiemTraVongLapCongTyMeCon(20L, 10L)).thenReturn(false);
        when(phanQuyenService.kiemTraQuyenSua(eq(userAdmin), any(BanGhiNghiepVuDTO.class)))
                .thenReturn(new PhanQuyenDuLieuService.KetQuaKiemTra(true, "Có quyền", null));
        when(khachHangDAO.ganCongTyMe(20L, 10L)).thenReturn(true);

        assertDoesNotThrow(() -> service.ganCongTyCon(20L, 10L, userAdmin));
        verify(khachHangDAO).ganCongTyMe(20L, 10L);
    }

    @Test
    @DisplayName("AC1 Validation: Tự gắn chính mình làm công ty con -> ném IllegalArgumentException")
    void testGanCongTyCon_ChinhMinh_Loi() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.ganCongTyCon(10L, 10L, userAdmin));
        assertTrue(ex.getMessage().contains("không thể tự làm"));
        verifyNoInteractions(khachHangDAO);
    }

    @Test
    @DisplayName("AC1 Validation: Tham số null -> ném IllegalArgumentException")
    void testGanCongTyCon_NullParams_Loi() {
        assertThrows(IllegalArgumentException.class, () -> service.ganCongTyCon(null, 10L, userAdmin));
        assertThrows(IllegalArgumentException.class, () -> service.ganCongTyCon(20L, null, userAdmin));
    }

    @Test
    @DisplayName("AC1 Validation: Gắn gây vòng lặp chu kỳ mẹ - con -> ném IllegalArgumentException")
    void testGanCongTyCon_VongLapChuKy_Loi() throws Exception {
        when(khachHangDAO.timTheoId(20L)).thenReturn(khachHangCon1);
        when(khachHangDAO.timTheoId(10L)).thenReturn(khachHangMe);
        when(khachHangDAO.kiemTraVongLapCongTyMeCon(20L, 10L)).thenReturn(true); // Báo phát hiện vòng lặp!

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.ganCongTyCon(20L, 10L, userAdmin));
        assertTrue(ex.getMessage().contains("vòng lặp"));
        verify(khachHangDAO, never()).ganCongTyMe(anyLong(), anyLong());
    }

    @Test
    @DisplayName("AC1 Validation: Không tìm thấy khách hàng con hoặc mẹ -> ném LoiKhongTimThayException")
    void testGanCongTyCon_KhongTimThay_Loi() throws Exception {
        when(khachHangDAO.timTheoId(999L)).thenReturn(null);

        assertThrows(LoiKhongTimThayException.class, () -> service.ganCongTyCon(999L, 10L, userAdmin));
    }

    @Test
    @DisplayName("AC1 Permission: Người dùng không có quyền sửa khách hàng -> ném LoiPhanQuyenException")
    void testGanCongTyCon_KhongCoQuyen_Loi() throws Exception {
        when(khachHangDAO.timTheoId(20L)).thenReturn(khachHangCon1);
        when(khachHangDAO.timTheoId(10L)).thenReturn(khachHangMe);
        when(khachHangDAO.kiemTraVongLapCongTyMeCon(20L, 10L)).thenReturn(false);
        // Từ chối quyền sửa
        when(phanQuyenService.kiemTraQuyenSua(eq(userSalesA), any(BanGhiNghiepVuDTO.class)))
                .thenReturn(new PhanQuyenDuLieuService.KetQuaKiemTra(false, "Không có quyền sở hữu", null));

        assertThrows(LoiPhanQuyenException.class, () -> service.ganCongTyCon(20L, 10L, userSalesA));
        verify(khachHangDAO, never()).ganCongTyMe(anyLong(), anyLong());
    }

    @Test
    @DisplayName("AC1: Gỡ bỏ thành công quan hệ công ty con")
    void testGoCongTyCon_ThanhCong() throws Exception {
        when(khachHangDAO.timTheoId(20L)).thenReturn(khachHangCon1);
        when(khachHangDAO.ganCongTyMe(20L, null)).thenReturn(true);

        assertDoesNotThrow(() -> service.goCongTyCon(20L, userAdmin));
        verify(khachHangDAO).ganCongTyMe(20L, null);
    }

    @Test
    @DisplayName("AC2: Hiển thị tổng giá trị hợp đồng của cả nhóm công ty (Tập đoàn = Mẹ + Các con)")
    void testLayThongKeNhomCongTy_TinhDungTongGiaTri() throws Exception {
        when(khachHangDAO.timTheoId(10L)).thenReturn(khachHangMe);

        // Danh sách công ty con
        List<KhachHang> dsCon = Arrays.asList(khachHangCon1, khachHangCon2);
        when(khachHangDAO.layDanhSachCongTyCon(10L)).thenReturn(dsCon);

        // Hợp đồng của mẹ: 500 triệu
        when(hopDongDAO.tinhTongGiaTriHopDongTheoKhachHang(10L))
                .thenReturn(new BigDecimal("500000000.00"));

        // Hợp đồng của con 1: 300 triệu
        when(hopDongDAO.tinhTongGiaTriHopDongTheoKhachHang(20L))
                .thenReturn(new BigDecimal("300000000.00"));

        // Hợp đồng của con 2: 200 triệu
        when(hopDongDAO.tinhTongGiaTriHopDongTheoKhachHang(30L))
                .thenReturn(new BigDecimal("200000000.00"));

        // Hợp đồng chi tiết toàn nhóm
        HopDong hd1 = new HopDong(1L, "HD-ME", 10L, new BigDecimal("500000000.00"), "DA_KY");
        HopDong hd2 = new HopDong(2L, "HD-CON1", 20L, new BigDecimal("300000000.00"), "DA_KY");
        HopDong hd3 = new HopDong(3L, "HD-CON2", 30L, new BigDecimal("200000000.00"), "DA_KY");
        when(hopDongDAO.layDanhSachHopDongNhomCongTy(eq(10L), anyList()))
                .thenReturn(Arrays.asList(hd1, hd2, hd3));

        // Gọi service
        ThongKeNhomCongTyDTO dto = service.layThongKeNhomCongTy(10L);

        assertNotNull(dto);
        assertEquals(khachHangMe, dto.getCongTyMe());
        assertEquals(2, dto.getSoLuongCongTyCon());
        assertEquals(3, dto.getTongSoHopDong());

        // Kiểm tra số tiền:
        // Mẹ: 500 triệu
        assertEquals(new BigDecimal("500000000.00"), dto.getTongGiaTriHopDongCongTyMe());
        // Các con: 300 + 200 = 500 triệu
        assertEquals(new BigDecimal("500000000.00"), dto.getTongGiaTriHopDongCacCongTyCon());
        // Toàn bộ nhóm công ty: 500 + 500 = 1,000,000,000 (1 tỷ)
        assertEquals(new BigDecimal("1000000000.00"), dto.getTongGiaTriHopDongNhomCongTy());
        assertTrue(dto.getTongGiaTriHopDongNhomCongTyDinhDang().contains("1.000.000.000"));
    }

    @Test
    @DisplayName("AC2: Công ty mẹ chưa có công ty con nào thì tổng giá trị nhóm bằng giá trị hợp đồng của chính nó")
    void testLayThongKeNhomCongTy_KhongCoCon() throws Exception {
        when(khachHangDAO.timTheoId(10L)).thenReturn(khachHangMe);
        when(khachHangDAO.layDanhSachCongTyCon(10L)).thenReturn(Collections.emptyList());
        when(hopDongDAO.tinhTongGiaTriHopDongTheoKhachHang(10L))
                .thenReturn(new BigDecimal("250000000.00"));
        when(hopDongDAO.layDanhSachHopDongNhomCongTy(eq(10L), anyList()))
                .thenReturn(Collections.singletonList(new HopDong(1L, "HD-01", 10L, new BigDecimal("250000000.00"), "DA_KY")));

        ThongKeNhomCongTyDTO dto = service.layThongKeNhomCongTy(10L);

        assertNotNull(dto);
        assertEquals(0, dto.getSoLuongCongTyCon());
        assertEquals(new BigDecimal("250000000.00"), dto.getTongGiaTriHopDongNhomCongTy());
        assertEquals(BigDecimal.ZERO, dto.getTongGiaTriHopDongCacCongTyCon());
    }
}
