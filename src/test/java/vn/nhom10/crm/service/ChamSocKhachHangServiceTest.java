package vn.nhom10.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dto.KhachHangChamSocDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.dto.ThongKeChamSocDTO;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.util.LoiPhanQuyenException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử ChamSocKhachHangService - Nghiệp vụ S3-09 và Phân quyền")
class ChamSocKhachHangServiceTest {

    private ChamSocKhachHangService service;
    private NguoiDungDTO userCSKH;
    private NguoiDungDTO userSalesA;
    private NguoiDungDTO userTeamLead;
    private NguoiDungDTO userDirector;
    private NguoiDungDTO userAccountant;

    @BeforeEach
    void setUp() {
        // Khởi tạo service không truyền DAO phụ thuộc DB ngoài để kích hoạt logic nghiệp vụ và fallback an toàn
        service = new ChamSocKhachHangService(null, null);

        // Chăm sóc khách hàng: vai trò CUST_SUCCESS, scope TOAN_BO
        userCSKH = new NguoiDungDTO(10L, "Trần Chăm Sóc", "cskh@crm.vn", VaiTroEnum.CUST_SUCCESS, 1L, "Phòng CSKH", PhamViDuLieu.TOAN_BO);

        // Nhân viên kinh doanh A: SALES_REP, scope CA_NHAN, ID 101, nhóm 1
        userSalesA = new NguoiDungDTO(101L, "Nguyễn Văn A (Sales)", "sales.a@crm.vn", VaiTroEnum.SALES_REP, 1L, "Nhóm Miền Bắc", PhamViDuLieu.CA_NHAN);

        // Trưởng nhóm: TEAM_LEAD, scope NHOM, ID 100, nhóm 1
        userTeamLead = new NguoiDungDTO(100L, "Lê Trưởng Nhóm", "lead@crm.vn", VaiTroEnum.TEAM_LEAD, 1L, "Nhóm Miền Bắc", PhamViDuLieu.NHOM);

        // Giám đốc: DIRECTOR, scope TOAN_BO, ID 1
        userDirector = new NguoiDungDTO(1L, "Phạm Giám Đốc", "director@crm.vn", VaiTroEnum.DIRECTOR, 1L, "Ban Giám Đốc", PhamViDuLieu.TOAN_BO);

        // Kế toán: ACCOUNTANT, chỉ xem
        userAccountant = new NguoiDungDTO(301L, "Vũ Kế Toán", "accountant@crm.vn", VaiTroEnum.ACCOUNTANT, 1L, "Phòng Kế Toán", PhamViDuLieu.TOAN_BO);
    }

    @Test
    @DisplayName("AC1: Cấu hình chu kỳ N ngày nhận tham số người dùng nhập hoặc fallback")
    void testLaySoNgayCauHinh() {
        assertEquals(45, service.laySoNgayCauHinh("45"));
        assertEquals(60, service.laySoNgayCauHinh("60"));
        assertEquals(30, service.laySoNgayCauHinh(""));
        assertEquals(30, service.laySoNgayCauHinh(null));
        assertEquals(30, service.laySoNgayCauHinh("-5"));
        assertEquals(30, service.laySoNgayCauHinh("abc"));
    }

    @Test
    @DisplayName("AC1: Lọc danh sách khách hàng chưa tương tác >= N ngày")
    void testLayDanhSachCanChamSoc_LocTheoSoNgay() {
        // N = 30 ngày: VinFast (15 ngày) sẽ bị loại bỏ
        List<KhachHangChamSocDTO> ds30 = service.layDanhSachCanChamSoc(userCSKH, 30, null, true);
        assertNotNull(ds30);
        for (KhachHangChamSocDTO kh : ds30) {
            assertTrue(kh.getSoNgayChuaTuongTac() >= 30, "Mọi khách hàng phải có số ngày chưa tương tác >= 30");
        }

        // N = 60 ngày: chỉ khách hàng > 60 ngày (Viettel - 75 ngày) lọt vào danh sách
        List<KhachHangChamSocDTO> ds60 = service.layDanhSachCanChamSoc(userCSKH, 60, null, true);
        assertNotNull(ds60);
        for (KhachHangChamSocDTO kh : ds60) {
            assertTrue(kh.getSoNgayChuaTuongTac() >= 60);
        }
    }

    @Test
    @DisplayName("AC2: Sắp xếp theo giá trị hợp đồng giảm dần")
    void testLayDanhSachCanChamSoc_SapXepGiaTriHopDongGiamDan() {
        List<KhachHangChamSocDTO> ds = service.layDanhSachCanChamSoc(userCSKH, 30, null, true);
        assertNotNull(ds);
        assertTrue(ds.size() >= 2);

        for (int i = 0; i < ds.size() - 1; i++) {
            BigDecimal truoc = ds.get(i).getTongGiaTriHopDong();
            BigDecimal sau = ds.get(i + 1).getTongGiaTriHopDong();
            assertTrue(truoc.compareTo(sau) >= 0, "Giá trị hợp đồng dòng trước phải lớn hơn hoặc bằng dòng sau");
        }
    }

    @Test
    @DisplayName("AC3: Đánh dấu đã liên hệ cập nhật trạng thái khách hàng thành công")
    void testDanhDauDaLienHe_ThanhCong() {
        boolean ok = service.danhDauDaLienHe(userCSKH, 1L, "Công ty FPT", "CUOC_GOI", "Đã liên hệ trao đổi gia hạn.");
        assertTrue(ok);

        List<KhachHangChamSocDTO> ds = service.layDanhSachCanChamSoc(userCSKH, 30, null, true);
        KhachHangChamSocDTO khFPT = ds.stream().filter(k -> k.getId().equals(1L)).findFirst().orElse(null);

        assertNotNull(khFPT);
        assertTrue(khFPT.isDaLienHeHomNay(), "Khách hàng phải có trạng thái đã liên hệ hôm nay");
        assertEquals(0, khFPT.getSoNgayChuaTuongTac(), "Số ngày chưa tương tác phải được reset về 0");
    }

    @Test
    @DisplayName("Phân quyền: Chặn tài khoản Kế toán (ACCOUNTANT) thực hiện đánh dấu đã liên hệ")
    void testPhanQuyen_ChanAccountant() {
        assertFalse(service.kiemTraQuyenChamSoc(userAccountant), "Kế toán không được phép có quyền chăm sóc khách hàng");

        LoiPhanQuyenException ex = assertThrows(LoiPhanQuyenException.class, () -> {
            service.danhDauDaLienHe(userAccountant, 1L, "Công ty FPT", "CUOC_GOI", "Kế toán cố ý ghi nhận");
        });

        assertTrue(ex.getMessage().contains("không có quyền"));
    }

    @Test
    @DisplayName("Data Scope: Sales A (CA_NHAN) chỉ thấy khách hàng của chính mình")
    void testDataScope_SalesA_CaNhan() {
        List<KhachHangChamSocDTO> ds = service.layDanhSachCanChamSoc(userSalesA, 30, null, true);
        assertNotNull(ds);
        for (KhachHangChamSocDTO kh : ds) {
            assertEquals(101L, kh.getNguoiPhuTrachId(), "Chỉ thấy khách do User 101 phụ trách");
        }
    }

    @Test
    @DisplayName("Data Scope: Team Lead (NHOM) thấy tất cả khách hàng trong nhóm Miền Bắc")
    void testDataScope_TeamLead_Nhom() {
        List<KhachHangChamSocDTO> ds = service.layDanhSachCanChamSoc(userTeamLead, 30, null, true);
        assertNotNull(ds);
        for (KhachHangChamSocDTO kh : ds) {
            assertEquals(1L, kh.getNhomKinhDoanhId(), "Chỉ thấy khách thuộc Nhóm Miền Bắc (ID = 1)");
            assertNotEquals(2L, kh.getNhomKinhDoanhId(), "Không được thấy khách của Nhóm Miền Nam (ID = 2)");
        }
    }

    @Test
    @DisplayName("Thống kê: Tính toán chính xác 4 chỉ số tóm tắt")
    void testTinhThongKe() {
        List<KhachHangChamSocDTO> ds = service.layDanhSachCanChamSoc(userCSKH, 30, null, true);
        ThongKeChamSocDTO stats = service.tinhThongKe(ds);

        assertNotNull(stats);
        assertTrue(stats.getTongSoCanChamSoc() > 0);
        assertTrue(stats.getSoQuaHanNghiemTrong() > 0);
        assertTrue(stats.getTongGiaTriHopDong().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(stats.getTongGiaTriHopDongDinhDang());
    }
}
