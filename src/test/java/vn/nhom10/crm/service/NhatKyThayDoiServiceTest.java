package vn.nhom10.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dto.BoLocNhatKyDTO;
import vn.nhom10.crm.dto.KetQuaPhanTrangDTO;
import vn.nhom10.crm.dto.NguoiDungOptionDTO;
import vn.nhom10.crm.dto.NhatKyThayDoiDTO;
import vn.nhom10.crm.dto.ThongKeNhatKyDTO;
import vn.nhom10.crm.model.HanhDongThayDoi;
import vn.nhom10.crm.model.LoaiDoiTuongNhayCam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử NhatKyThayDoiService - Story S2-04")
class NhatKyThayDoiServiceTest {

    private NhatKyThayDoiService service;

    @BeforeEach
    void setUp() {
        service = new NhatKyThayDoiService();
    }

    @Test
    @DisplayName("AC1 & AC2: Dữ liệu khởi tạo có đủ 4 loại đối tượng nhạy cảm và đầy đủ trường thông tin")
    void testKhoiTaoDuLieuMauDayDuThongTin() {
        List<NhatKyThayDoiDTO> tatCa = service.layTatCa();
        assertNotNull(tatCa);
        assertFalse(tatCa.isEmpty(), "Danh sách không được rỗng");
        assertTrue(tatCa.size() >= 12, "Cần có ít nhất 12 bản ghi mô phỏng nghiệp vụ");

        // Kiểm tra từng bản ghi phải có đủ: người thực hiện, thời điểm, giá trị trước và sau
        for (NhatKyThayDoiDTO item : tatCa) {
            assertNotNull(item.getId(), "Bản ghi phải có ID");
            assertNotNull(item.getMaTruyVet(), "Bản ghi phải có mã truy vết");
            assertNotNull(item.getNguoiThucHienId(), "Phải có ID người thực hiện");
            assertNotNull(item.getTenNguoiThucHien(), "Phải có tên người thực hiện");
            assertNotNull(item.getThoiDiem(), "Phải có thời điểm thay đổi");
            assertNotNull(item.getLoaiDoiTuong(), "Phải có loại đối tượng nhạy cảm");
            assertNotNull(item.getGiaTriTruoc(), "Phải có giá trị trước (old value)");
            assertNotNull(item.getGiaTriSau(), "Phải có giá trị sau (new value)");
            assertNotNull(item.getTruongThayDoi(), "Phải có trường thay đổi");
        }

        // Kiểm tra đủ cả 4 loại dữ liệu nhạy cảm
        boolean coChietKhau = tatCa.stream().anyMatch(i -> i.getLoaiDoiTuong() == LoaiDoiTuongNhayCam.CHIET_KHAU);
        boolean coChiTieu = tatCa.stream().anyMatch(i -> i.getLoaiDoiTuong() == LoaiDoiTuongNhayCam.CHI_TIEU);
        boolean coQuyenSoHuu = tatCa.stream().anyMatch(i -> i.getLoaiDoiTuong() == LoaiDoiTuongNhayCam.QUYEN_SO_HUU);
        boolean coVaiTro = tatCa.stream().anyMatch(i -> i.getLoaiDoiTuong() == LoaiDoiTuongNhayCam.VAI_TRO_NGUOI_DUNG);

        assertTrue(coChietKhau, "Phải ghi lại thay đổi trên chiết khấu");
        assertTrue(coChiTieu, "Phải ghi lại thay đổi trên chỉ tiêu");
        assertTrue(coQuyenSoHuu, "Phải ghi lại thay đổi trên quyền sở hữu dữ liệu");
        assertTrue(coVaiTro, "Phải ghi lại thay đổi trên vai trò người dùng");
    }

    @Test
    @DisplayName("AC3: Lọc theo người dùng thực hiện")
    void testLocTheoNguoiDung() {
        BoLocNhatKyDTO boLoc = new BoLocNhatKyDTO();
        boLoc.setNguoiDungId(101L); // Lê Minh Tuấn

        KetQuaPhanTrangDTO<NhatKyThayDoiDTO> ketQua = service.layDanhSach(boLoc);
        assertNotNull(ketQua);
        assertFalse(ketQua.getDanhSach().isEmpty(), "Phải có bản ghi của người dùng 101");

        for (NhatKyThayDoiDTO item : ketQua.getDanhSach()) {
            assertEquals(101L, item.getNguoiThucHienId(), "Tất cả bản ghi phải do người dùng 101 thực hiện");
        }
    }

    @Test
    @DisplayName("AC3: Lọc theo loại đối tượng nhạy cảm (CHIET_KHAU)")
    void testLocTheoLoaiDoiTuong() {
        BoLocNhatKyDTO boLoc = new BoLocNhatKyDTO();
        boLoc.setLoaiDoiTuong("CHIET_KHAU");

        KetQuaPhanTrangDTO<NhatKyThayDoiDTO> ketQua = service.layDanhSach(boLoc);
        assertNotNull(ketQua);
        assertFalse(ketQua.getDanhSach().isEmpty());

        for (NhatKyThayDoiDTO item : ketQua.getDanhSach()) {
            assertEquals(LoaiDoiTuongNhayCam.CHIET_KHAU, item.getLoaiDoiTuong());
        }
    }

    @Test
    @DisplayName("AC3: Lọc theo khoảng thời gian (Từ ngày - Đến ngày)")
    void testLocTheoKhoangThoiGian() {
        BoLocNhatKyDTO boLoc = new BoLocNhatKyDTO();
        boLoc.setTuNgay(LocalDate.of(2026, 9, 25));
        boLoc.setDenNgay(LocalDate.of(2026, 9, 30));

        KetQuaPhanTrangDTO<NhatKyThayDoiDTO> ketQua = service.layDanhSach(boLoc);
        assertNotNull(ketQua);
        assertFalse(ketQua.getDanhSach().isEmpty());

        for (NhatKyThayDoiDTO item : ketQua.getDanhSach()) {
            LocalDate ngay = item.getThoiDiem().toLocalDate();
            assertFalse(ngay.isBefore(LocalDate.of(2026, 9, 25)), "Ngày không được trước 25/09/2026");
            assertFalse(ngay.isAfter(LocalDate.of(2026, 9, 30)), "Ngày không được sau 30/09/2026");
        }
    }

    @Test
    @DisplayName("AC3: Lọc kết hợp đa tiêu chí (Người dùng + Loại đối tượng + Thời gian)")
    void testLocKetHopDaTieuChi() {
        BoLocNhatKyDTO boLoc = new BoLocNhatKyDTO();
        boLoc.setNguoiDungId(101L);
        boLoc.setLoaiDoiTuong("CHI_TIEU");
        boLoc.setTuNgay(LocalDate.of(2026, 9, 1));
        boLoc.setDenNgay(LocalDate.of(2026, 9, 30));

        KetQuaPhanTrangDTO<NhatKyThayDoiDTO> ketQua = service.layDanhSach(boLoc);
        assertNotNull(ketQua);
        assertFalse(ketQua.getDanhSach().isEmpty());

        for (NhatKyThayDoiDTO item : ketQua.getDanhSach()) {
            assertEquals(101L, item.getNguoiThucHienId());
            assertEquals(LoaiDoiTuongNhayCam.CHI_TIEU, item.getLoaiDoiTuong());
            LocalDate ngay = item.getThoiDiem().toLocalDate();
            assertFalse(ngay.isBefore(LocalDate.of(2026, 9, 1)));
            assertFalse(ngay.isAfter(LocalDate.of(2026, 9, 30)));
        }
    }

    @Test
    @DisplayName("Lọc theo từ khóa tìm kiếm nhanh")
    void testLocTheoTuKhoa() {
        BoLocNhatKyDTO boLoc = new BoLocNhatKyDTO();
        boLoc.setTuKhoa("Sao Mai");

        KetQuaPhanTrangDTO<NhatKyThayDoiDTO> ketQua = service.layDanhSach(boLoc);
        assertNotNull(ketQua);
        assertFalse(ketQua.getDanhSach().isEmpty());

        for (NhatKyThayDoiDTO item : ketQua.getDanhSach()) {
            boolean coTuKhoa = (item.getTenDoiTuong() != null && item.getTenDoiTuong().toLowerCase().contains("sao mai"))
                    || (item.getGiaTriSau() != null && item.getGiaTriSau().toLowerCase().contains("sao mai"));
            assertTrue(coTuKhoa, "Bản ghi phải chứa từ khóa tìm kiếm");
        }
    }

    @Test
    @DisplayName("Tính toán số liệu thống kê đầy đủ cho KPI cards")
    void testTinhThongKe() {
        ThongKeNhatKyDTO thongKe = service.tinhThongKe();
        assertNotNull(thongKe);
        assertTrue(thongKe.getTongSoBanGhi() >= 12);
        assertTrue(thongKe.getSoThayDoiChietKhau() > 0);
        assertTrue(thongKe.getSoThayDoiChiTieu() > 0);
        assertTrue(thongKe.getSoThayDoiQuyenSoHuu() > 0);
        assertTrue(thongKe.getSoThayDoiVaiTro() > 0);
        assertTrue(thongKe.getSoNguoiThucHien() > 0);
    }

    @Test
    @DisplayName("AC1 & AC2: Ghi nhận thành công thay đổi nhạy cảm mới")
    void testGhiNhatKyThayDoiMoi() {
        int soLuongTruoc = service.layTatCa().size();

        boolean ketQua = service.ghiNhatKyThayDoi(
                105L, "Hoàng Văn Nam", "nam.hv@crm.vn",
                LoaiDoiTuongNhayCam.CHIET_KHAU,
                "BG-2026-999", "Báo giá Thiết bị Mạng Cisco",
                "Tỷ lệ chiết khấu (%)",
                "5.0%", "18.0%",
                HanhDongThayDoi.CAP_NHAT,
                "Giám đốc phê duyệt đặc biệt",
                "192.168.1.200", "Chrome 128 / Windows 11"
        );

        assertTrue(ketQua, "Ghi nhận thay đổi phải thành công");
        assertEquals(soLuongTruoc + 1, service.layTatCa().size());

        // Kiểm tra bản ghi mới nhất được đưa lên đầu danh sách
        NhatKyThayDoiDTO moiNhat = service.layTatCa().get(0);
        assertEquals("BG-2026-999", moiNhat.getMaDoiTuong());
        assertEquals("5.0%", moiNhat.getGiaTriTruoc());
        assertEquals("18.0%", moiNhat.getGiaTriSau());
        assertEquals(LoaiDoiTuongNhayCam.CHIET_KHAU, moiNhat.getLoaiDoiTuong());
    }

    @Test
    @DisplayName("Lấy chi tiết bản ghi theo ID")
    void testLayChiTiet() {
        NhatKyThayDoiDTO chiTiet = service.layChiTiet(1L);
        assertNotNull(chiTiet);
        assertEquals(1L, chiTiet.getId());
        assertEquals("KPI-2026-Q3-T01", chiTiet.getMaDoiTuong());
        assertEquals("500,000,000 đ", chiTiet.getGiaTriTruoc());
        assertEquals("350,000,000 đ", chiTiet.getGiaTriSau());

        NhatKyThayDoiDTO khongTonTai = service.layChiTiet(99999L);
        assertNull(khongTonTai);
    }

    @Test
    @DisplayName("Lấy danh sách người dùng cho dropdown bộ lọc")
    void testLayDanhSachNguoiDung() {
        List<NguoiDungOptionDTO> danhSach = service.layDanhSachNguoiDung();
        assertNotNull(danhSach);
        assertFalse(danhSach.isEmpty());
        assertTrue(danhSach.stream().anyMatch(u -> "Lê Minh Tuấn".equals(u.getHoTen())));
    }

    @Test
    @DisplayName("Kiểm tra phân trang hợp lệ")
    void testPhanTrangHopLe() {
        BoLocNhatKyDTO boLoc = new BoLocNhatKyDTO();
        boLoc.setTrang(1);
        boLoc.setSoBanGhiTrenTrang(5);

        KetQuaPhanTrangDTO<NhatKyThayDoiDTO> trang1 = service.layDanhSach(boLoc);
        assertNotNull(trang1);
        assertEquals(5, trang1.getDanhSach().size());
        assertEquals(1, trang1.getTrangHienTai());
        assertTrue(trang1.isCoTrangSau());
        assertFalse(trang1.isCoTrangTruoc());
        assertTrue(trang1.getTongSoTrang() >= 3);

        // Lấy trang 2
        boLoc.setTrang(2);
        KetQuaPhanTrangDTO<NhatKyThayDoiDTO> trang2 = service.layDanhSach(boLoc);
        assertEquals(5, trang2.getDanhSach().size());
        assertEquals(2, trang2.getTrangHienTai());
        assertTrue(trang2.isCoTrangTruoc());
    }

    @Test
    @DisplayName("Kiểm tra tính hợp lệ của khoảng thời gian trong BoLocNhatKyDTO")
    void testValidateKhoangThoiGian() {
        BoLocNhatKyDTO boLocHopLe = new BoLocNhatKyDTO();
        boLocHopLe.setTuNgay(LocalDate.of(2026, 9, 1));
        boLocHopLe.setDenNgay(LocalDate.of(2026, 9, 30));
        assertTrue(boLocHopLe.isKhoangThoiGianHopLe());

        BoLocNhatKyDTO boLocKhongHopLe = new BoLocNhatKyDTO();
        boLocKhongHopLe.setTuNgay(LocalDate.of(2026, 10, 1));
        boLocKhongHopLe.setDenNgay(LocalDate.of(2026, 9, 1));
        assertFalse(boLocKhongHopLe.isKhoangThoiGianHopLe(), "Từ ngày sau đến ngày phải báo không hợp lệ");
    }
}
