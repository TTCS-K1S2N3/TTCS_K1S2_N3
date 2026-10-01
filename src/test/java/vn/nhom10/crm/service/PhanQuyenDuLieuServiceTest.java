package vn.nhom10.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO;
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO.LoaiNghiepVu;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.VaiTroNguoiDung;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử phân quyền phạm vi dữ liệu sở hữu - Story S1-05 (AC1, AC2, AC3, AC4)")
class PhanQuyenDuLieuServiceTest {

    private PhanQuyenDuLieuService service;
    private List<BanGhiNghiepVuDTO> danhSachMau;

    private NguoiDungDTO nhanVienA;
    private NguoiDungDTO nhanVienB;
    private NguoiDungDTO nhanVienC;
    private NguoiDungDTO truongNhomBac;
    private NguoiDungDTO giamDoc;

    private BanGhiNghiepVuDTO khachHangA;
    private BanGhiNghiepVuDTO khachHangB;
    private BanGhiNghiepVuDTO khachHangC;

    private BanGhiNghiepVuDTO coHoiB;
    private BanGhiNghiepVuDTO baoGiaB;
    private BanGhiNghiepVuDTO hoatDongB;

    @BeforeEach
    void setUp() {
        service = new PhanQuyenDuLieuService();
        danhSachMau = service.layDanhSachDuLieuMau();

        // 1. Khởi tạo người dùng kiểm thử
        nhanVienA = new NguoiDungDTO(101L, "Nguyễn Văn A", "sales.a@crm.vn",
                VaiTroNguoiDung.SALES_REP, 1L, "Nhóm Miền Bắc");

        nhanVienB = new NguoiDungDTO(102L, "Trần Thị B", "sales.b@crm.vn",
                VaiTroNguoiDung.SALES_REP, 1L, "Nhóm Miền Bắc");

        nhanVienC = new NguoiDungDTO(201L, "Lê Văn C", "sales.c@crm.vn",
                VaiTroNguoiDung.SALES_REP, 2L, "Nhóm Miền Nam");

        truongNhomBac = new NguoiDungDTO(100L, "Lê Thị Trưởng Nhóm", "lead.bac@crm.vn",
                VaiTroNguoiDung.TEAM_LEAD, 1L, "Nhóm Miền Bắc");

        giamDoc = new NguoiDungDTO(1L, "Bàn Thị Linh", "linh.ban@crm.vn",
                VaiTroNguoiDung.DIRECTOR, null, "Toàn công ty");

        // 2. Bản ghi mẫu của A
        khachHangA = new BanGhiNghiepVuDTO(
                1L, "KH-001", "Công ty FPT", LoaiNghiepVu.KHACH_HANG,
                101L, "Nguyễn Văn A", 1L, "Nhóm Miền Bắc",
                "VIP", "Hoạt động", LocalDate.now(), "Khách của A"
        );

        // 3. Bản ghi mẫu của B thuộc cả 4 nghiệp vụ: Khách hàng, Cơ hội, Báo giá, Hoạt động
        khachHangB = new BanGhiNghiepVuDTO(
                5L, "KH-002", "Tập đoàn Viettel", LoaiNghiepVu.KHACH_HANG,
                102L, "Trần Thị B", 1L, "Nhóm Miền Bắc",
                "VIP", "Hoạt động", LocalDate.now(), "Khách của B"
        );
        coHoiB = new BanGhiNghiepVuDTO(
                6L, "CH-102", "Dự án CRM Viettel IDC", LoaiNghiepVu.CO_HOI,
                102L, "Trần Thị B", 1L, "Nhóm Miền Bắc",
                "1,200,000,000 đ", "Khảo sát", LocalDate.now(), "Cơ hội của B"
        );
        baoGiaB = new BanGhiNghiepVuDTO(
                7L, "BG-202", "Báo giá Viettel IDC", LoaiNghiepVu.BAO_GIA,
                102L, "Trần Thị B", 1L, "Nhóm Miền Bắc",
                "1,200,000,000 đ", "Chờ duyệt", LocalDate.now(), "Báo giá của B"
        );
        hoatDongB = new BanGhiNghiepVuDTO(
                8L, "HD-302", "Gọi điện Viettel", LoaiNghiepVu.HOAT_DONG,
                102L, "Trần Thị B", 1L, "Nhóm Miền Bắc",
                "Cuộc gọi", "Hoàn thành", LocalDate.now(), "Hoạt động của B"
        );

        // 4. Bản ghi của C (nhóm Miền Nam)
        khachHangC = new BanGhiNghiepVuDTO(
                9L, "KH-003", "VNG Corporation", LoaiNghiepVu.KHACH_HANG,
                201L, "Lê Văn C", 2L, "Nhóm Miền Nam",
                "VIP", "Hoạt động", LocalDate.now(), "Khách của C"
        );
    }

    @Test
    @DisplayName("AC4: Chứng minh nhân viên A KHÔNG ĐỌC ĐƯỢC khách hàng của nhân viên B")
    void testNhanVienAKhongDocDuocKhachHangCuaNhanVienB() {
        // 1. Quyền đọc chi tiết bản ghi khách hàng
        PhanQuyenDuLieuService.KetQuaKiemTra ketQuaDocA = service.kiemTraQuyenTruyCap(nhanVienA, khachHangA);
        assertTrue(ketQuaDocA.isCoQuyen(), "Nhân viên A phải đọc được khách hàng của chính mình");

        PhanQuyenDuLieuService.KetQuaKiemTra ketQuaDocB = service.kiemTraQuyenTruyCap(nhanVienA, khachHangB);
        assertFalse(ketQuaDocB.isCoQuyen(), "Nhân viên A KHÔNG ĐƯỢC đọc khách hàng của nhân viên B (kể cả cùng nhóm)");
        assertNotNull(ketQuaDocB.getThongBao(), "Phải trả về thông báo lỗi");
        assertTrue(ketQuaDocB.getThongBao().contains("Từ chối truy cập"), "Thông báo phải thể hiện rõ từ chối truy cập");
        assertTrue(ketQuaDocB.getThongBao().contains(khachHangB.getTenNguoiPhuTrach()), "Thông báo phải giải thích rõ người phụ trách là ai");

        // 2. Quyền trên danh sách: Khách hàng của B không bao giờ xuất hiện trong danh sách của A
        List<BanGhiNghiepVuDTO> danhSachCuaA = service.locTheoPhamVi(
                danhSachMau, nhanVienA, PhamViDuLieu.CA_NHAN, null, "KHACH_HANG"
        );

        assertFalse(danhSachCuaA.isEmpty());
        for (BanGhiNghiepVuDTO bg : danhSachCuaA) {
            assertEquals(nhanVienA.getId(), bg.getNguoiPhuTrachId(),
                    "Mọi khách hàng trong danh sách của A bắt buộc phải do chính A phụ trách");
            assertNotEquals(nhanVienB.getId(), bg.getNguoiPhuTrachId(),
                    "Danh sách của A tuyệt đối không được chứa khách hàng của B");
        }
    }

    @Test
    @DisplayName("AC1: Áp dụng phạm vi dữ liệu cho cả 4 nghiệp vụ: Khách hàng, Cơ hội, Báo giá, Hoạt động")
    void testApDungChoBonNghiepVuCotLoi() {
        // Nhân viên A không thể đọc bất kỳ bản ghi nào của B trong cả 4 nghiệp vụ
        assertFalse(service.kiemTraQuyenTruyCap(nhanVienA, khachHangB).isCoQuyen(), "A không được xem Khách hàng của B");
        assertFalse(service.kiemTraQuyenTruyCap(nhanVienA, coHoiB).isCoQuyen(), "A không được xem Cơ hội của B");
        assertFalse(service.kiemTraQuyenTruyCap(nhanVienA, baoGiaB).isCoQuyen(), "A không được xem Báo giá của B");
        assertFalse(service.kiemTraQuyenTruyCap(nhanVienA, hoatDongB).isCoQuyen(), "A không được xem Hoạt động của B");

        // Nhưng Trưởng nhóm cùng nhóm đọc được cả 4 nghiệp vụ của B
        assertTrue(service.kiemTraQuyenTruyCap(truongNhomBac, khachHangB).isCoQuyen());
        assertTrue(service.kiemTraQuyenTruyCap(truongNhomBac, coHoiB).isCoQuyen());
        assertTrue(service.kiemTraQuyenTruyCap(truongNhomBac, baoGiaB).isCoQuyen());
        assertTrue(service.kiemTraQuyenTruyCap(truongNhomBac, hoatDongB).isCoQuyen());

        // Lọc danh sách từng loại nghiệp vụ cho A
        for (LoaiNghiepVu loai : LoaiNghiepVu.values()) {
            List<BanGhiNghiepVuDTO> list = service.locTheoPhamVi(
                    danhSachMau, nhanVienA, PhamViDuLieu.CA_NHAN, null, loai.getMa()
            );
            for (BanGhiNghiepVuDTO bg : list) {
                assertEquals(loai, bg.getLoaiNghiepVu());
                assertEquals(nhanVienA.getId(), bg.getNguoiPhuTrachId());
            }
        }
    }

    @Test
    @DisplayName("AC1 & AC2: Nhân viên Sales Rep không thể bypass để xem phạm vi NHOM hoặc TOAN_BO")
    void testNhanVienSalesKhongTheBypassPhamVi() {
        // Cố tình yêu cầu phạm vi TOAN_BO khi vai trò là SALES_REP
        List<BanGhiNghiepVuDTO> danhSach = service.locTheoPhamVi(
                danhSachMau, nhanVienA, PhamViDuLieu.TOAN_BO, null, "ALL"
        );

        // Server-side tự động ép về phạm vi CA_NHAN của A
        assertFalse(danhSach.isEmpty());
        for (BanGhiNghiepVuDTO bg : danhSach) {
            assertEquals(nhanVienA.getId(), bg.getNguoiPhuTrachId(),
                    "Hệ thống phải tự ép về phạm vi CA_NHAN khi người dùng không đủ quyền");
        }

        // Cố tình yêu cầu phạm vi NHOM khi vai trò là SALES_REP
        PhamViDuLieu hieuLuc = service.xacDinhPhamViHieuLuc(nhanVienA, PhamViDuLieu.NHOM);
        assertEquals(PhamViDuLieu.CA_NHAN, hieuLuc, "Sales Rep không được phép có phạm vi NHOM");
    }

    @Test
    @DisplayName("AC1: Trưởng nhóm đọc được khách hàng của cả A và B trong nhóm, nhưng KHÔNG đọc được nhóm khác")
    void testTruongNhomDocDuocDuLieuNhomMinhNhungKhongDocDuocNhomKhac() {
        // Trưởng nhóm Miền Bắc đọc khách của A (cùng nhóm)
        assertTrue(service.kiemTraQuyenTruyCap(truongNhomBac, khachHangA).isCoQuyen());

        // Trưởng nhóm Miền Bắc đọc khách của B (cùng nhóm)
        assertTrue(service.kiemTraQuyenTruyCap(truongNhomBac, khachHangB).isCoQuyen());

        // Trưởng nhóm Miền Bắc đọc khách của C (nhóm Miền Nam khác) -> BỊ TỪ CHỐI
        PhanQuyenDuLieuService.KetQuaKiemTra ketQua = service.kiemTraQuyenTruyCap(truongNhomBac, khachHangC);
        assertFalse(ketQua.isCoQuyen(), "Trưởng nhóm Miền Bắc không được đọc khách hàng thuộc nhóm Miền Nam");
        assertTrue(ketQua.getThongBao().contains("không nằm trong phạm vi nhóm quản lý của bạn"));

        // Lọc danh sách theo phạm vi NHOM
        List<BanGhiNghiepVuDTO> danhSachNhom = service.locTheoPhamVi(
                danhSachMau, truongNhomBac, PhamViDuLieu.NHOM, null, "ALL"
        );
        for (BanGhiNghiepVuDTO bg : danhSachNhom) {
            assertEquals(truongNhomBac.getNhomKinhDoanhId(), bg.getNhomKinhDoanhId(),
                    "Tất cả bản ghi phải thuộc nhóm Miền Bắc (ID = 1)");
            assertNotEquals(2L, bg.getNhomKinhDoanhId(), "Không được chứa bản ghi của nhóm khác");
        }
    }

    @Test
    @DisplayName("AC1: Giám đốc (Director) có quyền xem toàn bộ khách hàng và cơ hội trên toàn hệ thống")
    void testGiamDocXemDuocToanBo() {
        assertTrue(service.kiemTraQuyenTruyCap(giamDoc, khachHangA).isCoQuyen());
        assertTrue(service.kiemTraQuyenTruyCap(giamDoc, khachHangB).isCoQuyen());
        assertTrue(service.kiemTraQuyenTruyCap(giamDoc, khachHangC).isCoQuyen());

        List<BanGhiNghiepVuDTO> danhSachToanBo = service.locTheoPhamVi(
                danhSachMau, giamDoc, PhamViDuLieu.TOAN_BO, null, "ALL"
        );
        assertEquals(danhSachMau.size(), danhSachToanBo.size(),
                "Giám đốc phải xem được đầy đủ 100% bản ghi mẫu");
    }

    @Test
    @DisplayName("AC2: Tìm kiếm và Xuất Excel tự động tuân thủ phạm vi dữ liệu sở hữu")
    void testTimKiemVaXuatExcelTuanThuPhamVi() {
        // Tìm từ khóa "FPT" trong phạm vi của A -> Có kết quả
        List<BanGhiNghiepVuDTO> kqTimA = service.locTheoPhamVi(
                danhSachMau, nhanVienA, PhamViDuLieu.CA_NHAN, "FPT", "ALL"
        );
        assertFalse(kqTimA.isEmpty());

        // Tìm từ khóa "Viettel" (khách của B) trong phạm vi của A -> Phải trả về rỗng vì không thuộc quyền của A
        List<BanGhiNghiepVuDTO> kqTimViettel = service.locTheoPhamVi(
                danhSachMau, nhanVienA, PhamViDuLieu.CA_NHAN, "Viettel", "ALL"
        );
        assertTrue(kqTimViettel.isEmpty(), "Tìm kiếm không được tiết lộ bản ghi ngoài phạm vi sở hữu của A");

        // Xuất file CSV cho A -> Tuyệt đối không chứa "Viettel" hay "VNG"
        String csv = service.xuatDuLieuCSV(kqTimA);
        assertTrue(csv.contains("FPT"), "File xuất của A phải chứa khách hàng của A");
        assertFalse(csv.contains("Viettel"), "File xuất của A tuyệt đối không được chứa dữ liệu của B");
        assertFalse(csv.contains("VNG"), "File xuất của A tuyệt đối không được chứa dữ liệu của C");
    }

    @Test
    @DisplayName("AC3: Thông báo tiếng Việt rõ ràng khi truy cập bản ghi ngoài phạm vi")
    void testThongBaoTiengVietRoRangKhiNgoaiPhamVi() {
        PhanQuyenDuLieuService.KetQuaKiemTra ketQua = service.kiemTraQuyenTruyCap(nhanVienA, khachHangB);
        assertFalse(ketQua.isCoQuyen());
        String msg = ketQua.getThongBao();
        assertNotNull(msg);
        assertTrue(msg.contains("Từ chối truy cập"));
        assertTrue(msg.contains("Trần Thị B"));
        assertTrue(msg.contains("cá nhân"));
    }

    @Test
    @DisplayName("Security: Kiểm tra quyền sửa bản ghi theo Data Scope")
    void testKiemTraQuyenSuaTheoPhamViDuLieu() {
        // Sales Rep A không được sửa bản ghi của B
        PhanQuyenDuLieuService.KetQuaKiemTra kqSuaB = service.kiemTraQuyenSua(nhanVienA, khachHangB);
        assertFalse(kqSuaB.isCoQuyen(), "Sales Rep A tuyệt đối không được sửa bản ghi của B");
        assertTrue(kqSuaB.getThongBao().contains("Từ chối thao tác sửa"));

        // Sales Rep A được sửa bản ghi của chính mình
        PhanQuyenDuLieuService.KetQuaKiemTra kqSuaA = service.kiemTraQuyenSua(nhanVienA, khachHangA);
        assertTrue(kqSuaA.isCoQuyen(), "Sales Rep A được phép sửa bản ghi của chính mình");

        // Team Lead sửa được bản ghi của B (cùng nhóm)
        PhanQuyenDuLieuService.KetQuaKiemTra kqLeadSuaB = service.kiemTraQuyenSua(truongNhomBac, khachHangB);
        assertTrue(kqLeadSuaB.isCoQuyen(), "Trưởng nhóm được phép sửa bản ghi của thành viên trong nhóm");

        // Team Lead không được sửa bản ghi của C (nhóm khác)
        PhanQuyenDuLieuService.KetQuaKiemTra kqLeadSuaC = service.kiemTraQuyenSua(truongNhomBac, khachHangC);
        assertFalse(kqLeadSuaC.isCoQuyen(), "Trưởng nhóm không được sửa bản ghi của nhóm khác");

        // Giám đốc sửa được toàn bộ
        assertTrue(service.kiemTraQuyenSua(giamDoc, khachHangC).isCoQuyen(), "Giám đốc có quyền sửa tất cả");
    }
}
