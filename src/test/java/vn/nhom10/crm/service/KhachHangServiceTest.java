package vn.nhom10.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.nhom10.crm.dao.KhachHangDAO;
import vn.nhom10.crm.dao.NhomKinhDoanhDAO;
import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.TrangThaiKhachHangEnum;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử KhachHangService - Nghiệp vụ hồ sơ khách hàng & Phân quyền Data Scope (Story S3-01)")
class KhachHangServiceTest {

    @Mock
    private KhachHangDAO khachHangDAO;

    @Mock
    private CoCauToChucService coCauToChucService;

    @Mock
    private NhomKinhDoanhDAO nhomKinhDoanhDAO;

    private KhachHangService service;

    private NguoiDung salesA;
    private NguoiDung salesB;
    private NguoiDung teamLeadBac;
    private NguoiDung director;

    @BeforeEach
    void setUp() {
        service = new KhachHangService(khachHangDAO, coCauToChucService, nhomKinhDoanhDAO);

        // Sales A: ID 101, Nhóm 1 (Miền Bắc)
        salesA = new NguoiDung();
        salesA.setId(101L);
        salesA.setHoTen("Nguyễn Văn A (Sales)");
        salesA.setEmail("sales.a@crm.vn");
        salesA.setNhomKinhDoanhId(1);
        salesA.setTenNhomKinhDoanh("Nhóm Miền Bắc");
        salesA.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.SALES_REP, PhamViDuLieu.CA_NHAN)));

        // Sales B: ID 102, Nhóm 1 (Miền Bắc)
        salesB = new NguoiDung();
        salesB.setId(102L);
        salesB.setHoTen("Trần Văn B (Sales)");
        salesB.setEmail("sales.b@crm.vn");
        salesB.setNhomKinhDoanhId(1);
        salesB.setTenNhomKinhDoanh("Nhóm Miền Bắc");
        salesB.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.SALES_REP, PhamViDuLieu.CA_NHAN)));

        // Team Lead Miền Bắc: ID 100, Nhóm 1
        teamLeadBac = new NguoiDung();
        teamLeadBac.setId(100L);
        teamLeadBac.setHoTen("Lê Trưởng Nhóm");
        teamLeadBac.setEmail("lead.bac@crm.vn");
        teamLeadBac.setNhomKinhDoanhId(1);
        teamLeadBac.setTenNhomKinhDoanh("Nhóm Miền Bắc");
        teamLeadBac.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.TEAM_LEAD, PhamViDuLieu.NHOM)));

        // Director: ID 2
        director = new NguoiDung();
        director.setId(2L);
        director.setHoTen("Trần Giám Đốc");
        director.setEmail("director@crm.vn");
        director.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.DIRECTOR, PhamViDuLieu.TOAN_BO)));
    }

    @Test
    @DisplayName("AC1 Validation: Tên công ty không được để trống khi tạo mới")
    void testTaoKhachHang_TenCongTyRong_NemLoi() {
        KhachHang kh = new KhachHang();
        kh.setTenCongTy("   "); // Trắng

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.taoKhachHang(salesA, kh);
        });

        assertTrue(ex.getMessage().contains("Tên công ty / khách hàng không được để trống"));
    }

    @Test
    @DisplayName("AC2 Validation: Mã số thuế nếu có thì phải là duy nhất - ném lỗi khi MST đã tồn tại")
    void testTaoKhachHang_MaSoThueTrung_NemLoi() throws SQLException {
        KhachHang kh = new KhachHang();
        kh.setTenCongTy("Công ty Tân Phát");
        kh.setMaSoThue("0101234567");

        when(khachHangDAO.kiemTraTrungMaSoThue("0101234567", null)).thenReturn(true);
        when(khachHangDAO.layTenCongTyTheoMaSoThue("0101234567", null)).thenReturn("Công ty Cổ phần ABC Đang Dùng");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.taoKhachHang(salesA, kh);
        });

        assertTrue(ex.getMessage().contains("Mã số thuế '0101234567' đã tồn tại trong hệ thống"));
        assertTrue(ex.getMessage().contains("Công ty Cổ phần ABC Đang Dùng"));
    }

    @Test
    @DisplayName("AC2: Cho phép lưu khách hàng không có mã số thuế (mã số thuế rỗng hoặc null)")
    void testTaoKhachHang_MaSoThueRong_LuuThanhCongVoiNull() throws SQLException {
        KhachHang kh = new KhachHang();
        kh.setTenCongTy("Công ty TNHH Startup Mới");
        kh.setMaSoThue("   "); // Rỗng

        when(khachHangDAO.themKhachHang(any(KhachHang.class))).thenReturn(10L);
        KhachHang khTraVe = new KhachHang(10L, "Công ty TNHH Startup Mới", 101L);
        when(khachHangDAO.timTheoId(10L)).thenReturn(khTraVe);

        KhachHang result = service.taoKhachHang(salesA, kh);
        assertNotNull(result);
        assertNull(kh.getMaSoThue(), "Mã số thuế rỗng phải được chuyển thành null để tránh vi phạm unique index");
    }

    @Test
    @DisplayName("AC3 Validation: Từ chối trạng thái không hợp lệ, chỉ chấp nhận 4 trạng thái chuẩn")
    void testTaoKhachHang_TrangThaiKhongHopLe_NemLoi() {
        KhachHang kh = new KhachHang();
        kh.setTenCongTy("Công ty Test");
        kh.setTrangThai("VIP_SPECIAL"); // Không thuộc 4 trạng thái

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.taoKhachHang(salesA, kh);
        });

        assertTrue(ex.getMessage().contains("Trạng thái khách hàng không hợp lệ"));
    }

    @Test
    @DisplayName("AC3 Validation: Chấp nhận 4 trạng thái chuẩn (Tiềm năng, Đang giao dịch, Khách hàng, Ngừng hợp tác)")
    void testTaoKhachHang_BonTrangThaiChuan_ThanhCong() throws SQLException {
        String[] bonTrangThai = {"Tiềm năng", "Đang giao dịch", "Khách hàng", "Ngừng hợp tác"};

        when(khachHangDAO.themKhachHang(any(KhachHang.class))).thenReturn(1L);
        when(khachHangDAO.timTheoId(1L)).thenReturn(new KhachHang(1L, "Công ty Test", 101L));

        for (String tt : bonTrangThai) {
            KhachHang kh = new KhachHang();
            kh.setTenCongTy("Công ty " + tt);
            kh.setTrangThai(tt);

            KhachHang result = service.taoKhachHang(salesA, kh);
            assertNotNull(result);
        }
    }

    @Test
    @DisplayName("AC4 Bảo mật: Chống giả mạo người sở hữu - Nhân viên luôn bị ép sở hữu chính mình")
    void testTaoKhachHang_SalesNganChanSpoofingNguoiSoHuu() throws SQLException {
        KhachHang kh = new KhachHang();
        kh.setTenCongTy("Công ty TNHH Bảo Mật");
        kh.setNguoiSoHuuId(999L); // Kẻ gian cố gán cho user khác

        when(khachHangDAO.themKhachHang(any(KhachHang.class))).thenReturn(5L);
        when(khachHangDAO.timTheoId(5L)).thenReturn(new KhachHang(5L, "Công ty TNHH Bảo Mật", 101L));

        service.taoKhachHang(salesA, kh);

        assertEquals(101L, kh.getNguoiSoHuuId(), "Server bắt buộc gán ID của Sales A (101), không nhận 999");
        assertEquals(1L, kh.getNhomKinhDoanhId(), "Nhóm kinh doanh phải tự động gán theo Sales A");
    }

    @Test
    @DisplayName("AC4 Phân quyền: Sales A truy cập khách hàng của Sales B bị từ chối")
    void testKiemTraQuyenXem_SalesAXemKhachCuaSalesB_BiTuChoi() {
        KhachHang khCuaB = new KhachHang();
        khCuaB.setId(20L);
        khCuaB.setTenCongTy("Khách của B");
        khCuaB.setNguoiSoHuuId(102L); // B sở hữu
        khCuaB.setTenNguoiSoHuu("Trần Văn B");

        KhachHangService.KetQuaQuyenKhachHang kq = service.kiemTraQuyenXem(salesA, khCuaB);

        assertFalse(kq.isCoQuyen());
        assertTrue(kq.getThongBao().contains("Từ chối truy cập"));
        assertTrue(kq.getThongBao().contains("Trần Văn B"));
    }

    @Test
    @DisplayName("AC4 Phân quyền: Sales A truy cập khách hàng do chính mình sở hữu -> Có quyền")
    void testKiemTraQuyenXem_SalesAXemKhachCuaMinh_CoQuyen() {
        KhachHang khCuaA = new KhachHang();
        khCuaA.setId(10L);
        khCuaA.setTenCongTy("Khách của A");
        khCuaA.setNguoiSoHuuId(101L); // A sở hữu

        KhachHangService.KetQuaQuyenKhachHang kq = service.kiemTraQuyenXem(salesA, khCuaA);

        assertTrue(kq.isCoQuyen());
    }

    @Test
    @DisplayName("AC4 Phân quyền: Trưởng nhóm Miền Bắc xem khách của Sales B (cùng nhóm) -> Có quyền")
    void testKiemTraQuyenXem_TruongNhomXemKhachCungNhom_CoQuyen() {
        KhachHang khCuaB = new KhachHang();
        khCuaB.setId(20L);
        khCuaB.setTenCongTy("Khách của B");
        khCuaB.setNguoiSoHuuId(102L);
        khCuaB.setNhomKinhDoanhId(1L); // Cùng nhóm 1

        KhachHangService.KetQuaQuyenKhachHang kq = service.kiemTraQuyenXem(teamLeadBac, khCuaB);

        assertTrue(kq.isCoQuyen());
    }

    @Test
    @DisplayName("AC4 Phân quyền: Trưởng nhóm Miền Bắc xem khách của nhóm Miền Nam (nhóm khác) -> Bị từ chối")
    void testKiemTraQuyenXem_TruongNhomXemKhachNhomKhac_BiTuChoi() {
        KhachHang khNhomNam = new KhachHang();
        khNhomNam.setId(30L);
        khNhomNam.setTenCongTy("Khách Miền Nam");
        khNhomNam.setNguoiSoHuuId(103L);
        khNhomNam.setNhomKinhDoanhId(2L); // Nhóm 2 khác
        khNhomNam.setTenNhomKinhDoanh("Nhóm Miền Nam");

        when(coCauToChucService.kiemTraThuocPhamViCayToChuc(100L, 2L)).thenReturn(false);

        KhachHangService.KetQuaQuyenKhachHang kq = service.kiemTraQuyenXem(teamLeadBac, khNhomNam);

        assertFalse(kq.isCoQuyen());
        assertTrue(kq.getThongBao().contains("không nằm trong phạm vi nhóm quản lý"));
    }

    @Test
    @DisplayName("AC4 Phân quyền: Giám đốc kinh doanh xem được mọi khách hàng")
    void testKiemTraQuyenXem_GiamDoc_XemDuocMoiKhachHang() {
        KhachHang khBatKy = new KhachHang();
        khBatKy.setId(99L);
        khBatKy.setTenCongTy("Khách Hàng Bất Kỳ");
        khBatKy.setNguoiSoHuuId(999L);
        khBatKy.setNhomKinhDoanhId(999L);

        KhachHangService.KetQuaQuyenKhachHang kq = service.kiemTraQuyenXem(director, khBatKy);

        assertTrue(kq.isCoQuyen());
    }

    @Test
    @DisplayName("AC4 Phân quyền sửa: Sales A cố sửa khách của B bị ném SecurityException")
    void testCapNhatKhachHang_SalesASuaKhachCuaB_NemSecurityException() throws SQLException {
        KhachHang khCu = new KhachHang();
        khCu.setId(20L);
        khCu.setTenCongTy("Khách của B");
        khCu.setNguoiSoHuuId(102L); // B sở hữu
        khCu.setNhomKinhDoanhId(1L);

        when(khachHangDAO.timTheoId(20L)).thenReturn(khCu);

        KhachHang khMoi = new KhachHang();
        khMoi.setId(20L);
        khMoi.setTenCongTy("Khách đã bị sửa lén");

        assertThrows(SecurityException.class, () -> {
            service.capNhatKhachHang(salesA, khMoi);
        });
    }

    @Test
    @DisplayName("AC2 Cập nhật: Sửa mã số thuế thành mã của khách khác bị ném lỗi")
    void testCapNhatKhachHang_SuaTrungMaSoThueCuaKhachKhac_NemLoi() throws SQLException {
        KhachHang khCu = new KhachHang();
        khCu.setId(10L);
        khCu.setTenCongTy("Khách của A");
        khCu.setNguoiSoHuuId(101L);
        khCu.setMaSoThue("0101111111");

        when(khachHangDAO.timTheoId(10L)).thenReturn(khCu);
        when(khachHangDAO.kiemTraTrungMaSoThue("0102222222", 10L)).thenReturn(true);
        when(khachHangDAO.layTenCongTyTheoMaSoThue("0102222222", 10L)).thenReturn("Công ty Khác Đã Đăng Ký");

        KhachHang khMoi = new KhachHang();
        khMoi.setId(10L);
        khMoi.setTenCongTy("Khách của A");
        khMoi.setMaSoThue("0102222222"); // Đổi sang mã trùng

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.capNhatKhachHang(salesA, khMoi);
        });

        assertTrue(ex.getMessage().contains("Mã số thuế '0102222222' đã tồn tại trong hệ thống"));
    }

    @Test
    @DisplayName("Xuất Excel: Danh sách khách hàng xuất ra file byte[] hợp lệ")
    void testXuatDanhSachExcel_ThanhCong() throws Exception {
        KhachHang kh = new KhachHang();
        kh.setMaKhachHang("KH000001");
        kh.setTenCongTy("Tập đoàn Viễn thông Quân đội");
        kh.setMaSoThue("0100109106");
        kh.setTenNganhNghe("Công nghệ thông tin");
        kh.setTenQuyMo("Trên 500 nhân sự");
        kh.setWebsite("https://viettel.vn");
        kh.setDiaChi("Lô D26 Khu đô thị mới Cầu Giấy, Hà Nội");
        kh.setTenNguoiSoHuu("Nguyễn Văn A");
        kh.setTenNhomKinhDoanh("Nhóm Miền Bắc");
        kh.setDoanhThuUocTinh(new BigDecimal("1000000000.00"));
        kh.setTrangThai("KHACH_HANG");

        byte[] excelBytes = service.xuatDanhSachExcel(List.of(kh));
        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0, "Dữ liệu Excel phải có dung lượng lớn hơn 0 byte");
    }
}
