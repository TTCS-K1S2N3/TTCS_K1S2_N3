package vn.nhom10.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import vn.nhom10.crm.dao.GiaiDoanPipelineDAO;
import vn.nhom10.crm.dto.DieuKienRoiGiaiDoanDTO;
import vn.nhom10.crm.dto.KetQuaGiaiDoanDTO;
import vn.nhom10.crm.model.GiaiDoanPipeline;
import vn.nhom10.crm.model.LoaiGiaiDoanEnum;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.TrangThaiGiaiDoanEnum;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;

import java.math.BigDecimal;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Kiểm thử toàn diện tầng Service cho Story S2-09 theo 4 Acceptance Criteria.
 */
public class GiaiDoanPipelineServiceTest {

    private GiaiDoanPipelineDAO daoMock;
    private GiaiDoanPipelineService service;

    private NguoiDung directorUser;
    private NguoiDung salesRepUser;

    @BeforeEach
    public void setUp() {
        daoMock = Mockito.mock(GiaiDoanPipelineDAO.class);
        service = new GiaiDoanPipelineService(daoMock);

        directorUser = new NguoiDung(1, "Giám Đốc Kinh Doanh", "director@crm.vn");
        directorUser.themVaiTro(new VaiTro(VaiTroEnum.DIRECTOR));

        salesRepUser = new NguoiDung(2, "Nhân Viên Sales", "sales@crm.vn");
        salesRepUser.themVaiTro(new VaiTro(VaiTroEnum.SALES_REP));
    }

    // ====================================================================
    // AC 1: Khai báo chuỗi giai đoạn (Tiếp cận → Nhu cầu → Giải pháp → Báo giá...)
    // ====================================================================

    @Test
    public void testTaoGiaiDoanThanhCong() throws SQLException {
        GiaiDoanPipeline gd = new GiaiDoanPipeline();
        gd.setMaGiaiDoan("TIEP_CAN");
        gd.setTenGiaiDoan("Tiếp cận khách hàng");
        gd.setThuTu(1);
        gd.setXacSuatThang(10);
        gd.setDieuKienBatBuoc("Có ít nhất 1 cuộc gọi kết nối");
        gd.setSoCuocGoiToiThieu(1);

        when(daoMock.kiemTraMaTonTai("TIEP_CAN", null)).thenReturn(false);
        when(daoMock.themGiaiDoan(any(GiaiDoanPipeline.class))).thenReturn(1);

        KetQuaGiaiDoanDTO ketQua = service.taoGiaiDoan(gd, directorUser);

        assertTrue(ketQua.isThanhCong());
        assertEquals(1, ketQua.getGiaiDoan().getId());
        verify(daoMock, times(1)).themGiaiDoan(gd);
    }

    @Test
    public void testTaoGiaiDoanThatBaiKhiTrungMa() {
        GiaiDoanPipeline gd = new GiaiDoanPipeline();
        gd.setMaGiaiDoan("TRUNG_MA");
        gd.setTenGiaiDoan("Giai đoạn trùng");
        gd.setThuTu(2);

        when(daoMock.kiemTraMaTonTai("TRUNG_MA", null)).thenReturn(true);

        KetQuaGiaiDoanDTO ketQua = service.taoGiaiDoan(gd, directorUser);

        assertFalse(ketQua.isThanhCong());
        assertTrue(ketQua.getDanhSachLoi().containsKey("maGiaiDoan"));
        assertTrue(ketQua.getDanhSachLoi().get("maGiaiDoan").contains("đã tồn tại"));
    }

    @Test
    public void testDoiThuTuHaiGiaiDoan() throws SQLException {
        GiaiDoanPipeline gd1 = new GiaiDoanPipeline(1, "B1", "Bước 1", 1, 10, "");
        GiaiDoanPipeline gd2 = new GiaiDoanPipeline(2, "B2", "Bước 2", 2, 20, "");

        when(daoMock.timTheoId(1)).thenReturn(gd1);
        when(daoMock.timTheoId(2)).thenReturn(gd2);
        when(daoMock.hoanDoiThuTu(1, 1, 2, 2)).thenReturn(true);

        KetQuaGiaiDoanDTO ketQua = service.doiThuTu(1, 2, directorUser);

        assertTrue(ketQua.isThanhCong());
        verify(daoMock, times(1)).hoanDoiThuTu(1, 1, 2, 2);
    }

    // ====================================================================
    // AC 2: Mỗi giai đoạn có xác suất thắng mặc định dùng để tính dự báo
    // ====================================================================

    @Test
    public void testTinhDuBaoDoanhSoChuanXac() {
        // Giá trị 100,000,000 với xác suất 25% -> 25,000,000
        BigDecimal duBao1 = service.tinhDuBaoDoanhSo(new BigDecimal("100000000.00"), 25);
        assertEquals(0, new BigDecimal("25000000.00").compareTo(duBao1));

        // Giá trị 300,000,000 với xác suất 70% -> 210,000,000
        BigDecimal duBao2 = service.tinhDuBaoDoanhSo(new BigDecimal("300000000.00"), 70);
        assertEquals(0, new BigDecimal("210000000.00").compareTo(duBao2));

        // Giá trị âm hoặc 0 -> 0
        BigDecimal duBao3 = service.tinhDuBaoDoanhSo(BigDecimal.ZERO, 50);
        assertEquals(0, BigDecimal.ZERO.compareTo(duBao3));
    }

    @Test
    public void testValidateXacSuatThangTu0Den100() throws Exception {
        GiaiDoanPipeline gdAm = new GiaiDoanPipeline();
        gdAm.setMaGiaiDoan("TEST_AM");
        gdAm.setTenGiaiDoan("Âm");
        gdAm.setThuTu(1);
        gdAm.setXacSuatThang(-5); // Nhỏ hơn 0

        KetQuaGiaiDoanDTO ketQuaAm = service.taoGiaiDoan(gdAm, directorUser);
        assertFalse(ketQuaAm.isThanhCong(), "Xác suất âm phải bị từ chối");
        assertTrue(ketQuaAm.getDanhSachLoi().containsKey("xacSuatThang"));

        GiaiDoanPipeline gdQua100 = new GiaiDoanPipeline();
        gdQua100.setMaGiaiDoan("TEST_150");
        gdQua100.setTenGiaiDoan("Quá 100");
        gdQua100.setThuTu(2);
        gdQua100.setXacSuatThang(150); // Lớn hơn 100

        KetQuaGiaiDoanDTO ketQuaQua100 = service.taoGiaiDoan(gdQua100, directorUser);
        assertFalse(ketQuaQua100.isThanhCong(), "Xác suất > 100 phải bị từ chối");
        assertTrue(ketQuaQua100.getDanhSachLoi().containsKey("xacSuatThang"));

        // Xác suất 0 và 100 hợp lệ
        GiaiDoanPipeline gd0 = new GiaiDoanPipeline();
        gd0.setMaGiaiDoan("TEST_0");
        gd0.setTenGiaiDoan("Không phần trăm");
        gd0.setThuTu(3);
        gd0.setXacSuatThang(0);
        when(daoMock.kiemTraMaTonTai("TEST_0", null)).thenReturn(false);
        when(daoMock.themGiaiDoan(gd0)).thenReturn(3);
        KetQuaGiaiDoanDTO ketQua0 = service.taoGiaiDoan(gd0, directorUser);
        assertTrue(ketQua0.isThanhCong(), "Xác suất 0% phải hợp lệ");

        GiaiDoanPipeline gd100 = new GiaiDoanPipeline();
        gd100.setMaGiaiDoan("TEST_100");
        gd100.setTenGiaiDoan("Một trăm phần trăm");
        gd100.setThuTu(4);
        gd100.setXacSuatThang(100);
        when(daoMock.kiemTraMaTonTai("TEST_100", null)).thenReturn(false);
        when(daoMock.themGiaiDoan(gd100)).thenReturn(4);
        KetQuaGiaiDoanDTO ketQua100 = service.taoGiaiDoan(gd100, directorUser);
        assertTrue(ketQua100.isThanhCong(), "Xác suất 100% phải hợp lệ");
    }

    // ====================================================================
    // AC 3: Khai báo điều kiện bắt buộc để rời một giai đoạn (VD: có ít nhất 1 cuộc gặp)
    // ====================================================================

    @Test
    public void testKiemTraDieuKienRoiGiaiDoanThieuCuocGap() {
        GiaiDoanPipeline gd = new GiaiDoanPipeline();
        gd.setId(2);
        gd.setTenGiaiDoan("Xác định nhu cầu");
        gd.setSoCuocGapToiThieu(1); // Yêu cầu >= 1 cuộc gặp
        gd.setSoCuocGoiToiThieu(1); // Yêu cầu >= 1 cuộc gọi

        when(daoMock.timTheoId(2)).thenReturn(gd);

        // Trường hợp: Đã có 1 cuộc gọi nhưng chưa có cuộc gặp nào
        DieuKienRoiGiaiDoanDTO ketQua = service.kiemTraDieuKienRoiGiaiDoan(2, 0, 1, false, false);

        assertFalse(ketQua.isThoaDieuKien(), "Chưa có cuộc gặp trực tiếp thì không được rời giai đoạn");
        assertEquals(1, ketQua.getDanhSachYeuCauThieu().size());
        assertTrue(ketQua.getDanhSachYeuCauThieu().get(0).contains("Phải có ít nhất 1 cuộc gặp trực tiếp"));
    }

    @Test
    public void testKiemTraDieuKienRoiGiaiDoanThoaManTatCa() {
        GiaiDoanPipeline gd = new GiaiDoanPipeline();
        gd.setId(4);
        gd.setTenGiaiDoan("Báo giá");
        gd.setYeuCauBaoGia(true); // Bắt buộc có báo giá
        gd.setSoCuocGapToiThieu(1);

        when(daoMock.timTheoId(4)).thenReturn(gd);

        // Trường hợp: Có 1 cuộc gặp và đã có báo giá
        DieuKienRoiGiaiDoanDTO ketQua = service.kiemTraDieuKienRoiGiaiDoan(4, 1, 2, true, false);

        assertTrue(ketQua.isThoaDieuKien(), "Khi đáp ứng đủ tất cả tiêu chí thì phải được phép chuyển giai đoạn");
        assertTrue(ketQua.getDanhSachYeuCauThieu().isEmpty());
    }

    // ====================================================================
    // AC 3: Kiểm tra điều kiện giai đoạn KHAO_SAT (id=2, meeting >= 1, survey = true)
    // ====================================================================

    @Test
    public void testKiemTraDieuKienKhaoSatCase1FailThieuGapVaKhaoSat() {
        GiaiDoanPipeline gd = new GiaiDoanPipeline();
        gd.setId(2);
        gd.setMaGiaiDoan("KHAO_SAT");
        gd.setTenGiaiDoan("Khảo sát nhu cầu thực tế");
        gd.setSoCuocGapToiThieu(1);
        gd.setYeuCauKhaoSatNhuCau(true);

        when(daoMock.timTheoId(2)).thenReturn(gd);

        // Case 1: meeting = 0, survey = false -> FAIL (thiếu cả 2)
        DieuKienRoiGiaiDoanDTO ketQua = service.kiemTraDieuKienRoiGiaiDoan(2, 0, 0, false, false);

        assertFalse(ketQua.isThoaDieuKien(), "Case 1: meeting=0, survey=false phải FAIL");
        assertEquals(2, ketQua.getDanhSachYeuCauThieu().size(), "Phải báo thiếu 2 điều kiện");
        assertTrue(ketQua.getDanhSachYeuCauThieu().stream().anyMatch(msg -> msg.toLowerCase().contains("cuộc gặp")));
        assertTrue(ketQua.getDanhSachYeuCauThieu().stream().anyMatch(msg -> msg.toLowerCase().contains("khảo sát")));
    }

    @Test
    public void testKiemTraDieuKienKhaoSatCase2FailChiThieuKhaoSat() {
        GiaiDoanPipeline gd = new GiaiDoanPipeline();
        gd.setId(2);
        gd.setMaGiaiDoan("KHAO_SAT");
        gd.setTenGiaiDoan("Khảo sát nhu cầu thực tế");
        gd.setSoCuocGapToiThieu(1);
        gd.setYeuCauKhaoSatNhuCau(true);

        when(daoMock.timTheoId(2)).thenReturn(gd);

        // Case 2: meeting = 1, survey = false -> FAIL (chỉ thiếu khảo sát)
        DieuKienRoiGiaiDoanDTO ketQua = service.kiemTraDieuKienRoiGiaiDoan(2, 1, 0, false, false);

        assertFalse(ketQua.isThoaDieuKien(), "Case 2: meeting=1, survey=false phải FAIL");
        assertEquals(1, ketQua.getDanhSachYeuCauThieu().size(), "Chỉ thiếu 1 điều kiện khảo sát");
        assertTrue(ketQua.getDanhSachYeuCauThieu().get(0).toLowerCase().contains("khảo sát"));
    }

    @Test
    public void testKiemTraDieuKienKhaoSatCase3PassThoaManTatCa() {
        GiaiDoanPipeline gd = new GiaiDoanPipeline();
        gd.setId(2);
        gd.setMaGiaiDoan("KHAO_SAT");
        gd.setTenGiaiDoan("Khảo sát nhu cầu thực tế");
        gd.setSoCuocGapToiThieu(1);
        gd.setYeuCauKhaoSatNhuCau(true);

        when(daoMock.timTheoId(2)).thenReturn(gd);

        // Case 3: meeting = 1, survey = true -> PASS (đủ điều kiện)
        DieuKienRoiGiaiDoanDTO ketQua = service.kiemTraDieuKienRoiGiaiDoan(2, 1, 0, false, true);

        assertTrue(ketQua.isThoaDieuKien(), "Case 3: meeting=1, survey=true phải PASS");
        assertTrue(ketQua.getDanhSachYeuCauThieu().isEmpty(), "Không còn điều kiện nào bị thiếu");
    }

    @Test
    public void testKiemTraDieuKienRoiGiaiDoanKhongTonTai() {
        when(daoMock.timTheoId(999999)).thenReturn(null);

        DieuKienRoiGiaiDoanDTO ketQua = service.kiemTraDieuKienRoiGiaiDoan(999999, 1, 1, true, true);

        assertFalse(ketQua.isThoaDieuKien());
        assertTrue(ketQua.getThongBaoChiTiet().contains("ID giai đoạn không hợp lệ."));
    }

    @Test
    public void testKiemTraDieuKienRoiGiaiDoanIdAm() {
        DieuKienRoiGiaiDoanDTO ketQua = service.kiemTraDieuKienRoiGiaiDoan(-1, 0, 0, false, false);

        assertFalse(ketQua.isThoaDieuKien());
        assertTrue(ketQua.getThongBaoChiTiet().contains("ID giai đoạn không hợp lệ."));
    }

    // ====================================================================
    // AC 4: Thay đổi cấu hình không làm hỏng cơ hội đang chạy
    // ====================================================================

    @Test
    public void testKhongChoXoaGiaiDoanDangCoCoHoiChay() throws SQLException {
        GiaiDoanPipeline gd = new GiaiDoanPipeline();
        gd.setId(5);
        gd.setTenGiaiDoan("Đàm phán");

        when(daoMock.timTheoId(5)).thenReturn(gd);
        when(daoMock.demSoCoHoiTrongGiaiDoan(5)).thenReturn(3);
        when(daoMock.demSoCoHoiHienTai(5)).thenReturn(3);
        when(daoMock.demSoLichSuThamChieu(5)).thenReturn(0);

        KetQuaGiaiDoanDTO ketQua = service.xoaGiaiDoan(5, directorUser);

        assertFalse(ketQua.isThanhCong(), "Giai đoạn đang có cơ hội không được phép xoá cứng");
        assertTrue(ketQua.getThongBao().contains("đang có 3 cơ hội hiện tại tham chiếu"));
        assertTrue(ketQua.getThongBao().contains("Ngừng áp dụng"));
        verify(daoMock, never()).xoaGiaiDoan(5);
    }

    @Test
    public void testKhongChoXoaGiaiDoanCurrentOnly() throws SQLException {
        GiaiDoanPipeline gd = new GiaiDoanPipeline();
        gd.setId(2);
        gd.setTenGiaiDoan("Khảo sát nhu cầu thực tế");

        when(daoMock.timTheoId(2)).thenReturn(gd);
        when(daoMock.demSoCoHoiTrongGiaiDoan(2)).thenReturn(2);
        when(daoMock.demSoCoHoiHienTai(2)).thenReturn(2);
        when(daoMock.demSoLichSuThamChieu(2)).thenReturn(0);

        KetQuaGiaiDoanDTO ketQua = service.xoaGiaiDoan(2, directorUser);

        assertFalse(ketQua.isThanhCong());
        assertTrue(ketQua.getThongBao().contains("Giai đoạn 'Khảo sát nhu cầu thực tế' đang có 2 cơ hội hiện tại tham chiếu."));
        assertTrue(ketQua.getThongBao().contains("Ngừng áp dụng"));
        verify(daoMock, never()).xoaGiaiDoan(2);
    }

    @Test
    public void testKhongChoXoaGiaiDoanHistoryOnly() throws SQLException {
        GiaiDoanPipeline gd = new GiaiDoanPipeline();
        gd.setId(2);
        gd.setTenGiaiDoan("Khảo sát nhu cầu thực tế");

        when(daoMock.timTheoId(2)).thenReturn(gd);
        when(daoMock.demSoCoHoiTrongGiaiDoan(2)).thenReturn(1);
        when(daoMock.demSoCoHoiHienTai(2)).thenReturn(0);
        when(daoMock.demSoLichSuThamChieu(2)).thenReturn(1);

        KetQuaGiaiDoanDTO ketQua = service.xoaGiaiDoan(2, directorUser);

        assertFalse(ketQua.isThanhCong());
        assertTrue(ketQua.getThongBao().contains("Không thể xóa vì giai đoạn 'Khảo sát nhu cầu thực tế' đang được 1 bản ghi lịch sử cơ hội tham chiếu."));
        assertTrue(ketQua.getThongBao().contains("Ngừng áp dụng"));
        verify(daoMock, never()).xoaGiaiDoan(2);
    }

    @Test
    public void testKhongChoXoaGiaiDoanCurrentAndHistory() throws SQLException {
        GiaiDoanPipeline gd = new GiaiDoanPipeline();
        gd.setId(2);
        gd.setTenGiaiDoan("Khảo sát nhu cầu thực tế");

        when(daoMock.timTheoId(2)).thenReturn(gd);
        when(daoMock.demSoCoHoiTrongGiaiDoan(2)).thenReturn(5);
        when(daoMock.demSoCoHoiHienTai(2)).thenReturn(2);
        when(daoMock.demSoLichSuThamChieu(2)).thenReturn(3);

        KetQuaGiaiDoanDTO ketQua = service.xoaGiaiDoan(2, directorUser);

        assertFalse(ketQua.isThanhCong());
        assertTrue(ketQua.getThongBao().contains("Giai đoạn 'Khảo sát nhu cầu thực tế' đang có 2 cơ hội hiện tại và 3 bản ghi lịch sử tham chiếu."));
        assertTrue(ketQua.getThongBao().contains("Ngừng áp dụng"));
        verify(daoMock, never()).xoaGiaiDoan(2);
    }

    @Test
    public void testCapNhatGiaiDoanKhongLamHongCoHoiDangChay() throws SQLException {
        GiaiDoanPipeline gdCu = new GiaiDoanPipeline(5, "DAM_PHAN", "Đàm phán", 5, 80, "Điều kiện cũ");
        GiaiDoanPipeline gdMoi = new GiaiDoanPipeline(5, "DAM_PHAN", "Đàm phán & Thương thảo", 5, 85, "Điều kiện mới");

        when(daoMock.timTheoId(5)).thenReturn(gdCu);
        when(daoMock.capNhatGiaiDoan(gdMoi)).thenReturn(true);

        KetQuaGiaiDoanDTO ketQua = service.capNhatGiaiDoan(gdMoi, directorUser);

        assertTrue(ketQua.isThanhCong());
        assertTrue(ketQua.getThongBao().contains("bảo toàn an toàn"));
        verify(daoMock, times(1)).capNhatGiaiDoan(gdMoi);
    }

    @Test
    public void testChuyenSangNgungApDungBaoToanCoHoi() throws SQLException {
        GiaiDoanPipeline gd = new GiaiDoanPipeline(6, "CHOT_MOI", "Giai đoạn cũ", 6, 90, "");

        when(daoMock.timTheoId(6)).thenReturn(gd);
        when(daoMock.capNhatTrangThai(6, TrangThaiGiaiDoanEnum.NGUNG_AP_DUNG)).thenReturn(true);

        KetQuaGiaiDoanDTO ketQua = service.chuyenTrangThai(6, TrangThaiGiaiDoanEnum.NGUNG_AP_DUNG, directorUser);

        assertTrue(ketQua.isThanhCong());
        assertEquals(TrangThaiGiaiDoanEnum.NGUNG_AP_DUNG, gd.getTrangThai());
        assertTrue(ketQua.getThongBao().contains("Các cơ hội cũ vẫn được lưu giữ an toàn"));
    }

    // ====================================================================
    // Phân quyền: Giám đốc kinh doanh vs Nhân viên kinh doanh
    // ====================================================================

    @Test
    public void testSalesRepKhongCoQuyenCauHinhPipeline() {
        GiaiDoanPipeline gd = new GiaiDoanPipeline();
        gd.setMaGiaiDoan("TRY");
        gd.setTenGiaiDoan("Sales thử tạo");

        KetQuaGiaiDoanDTO ketQua = service.taoGiaiDoan(gd, salesRepUser);

        assertFalse(ketQua.isThanhCong());
        assertTrue(ketQua.getThongBao().contains("không có quyền"));
        verifyNoInteractions(daoMock);
    }

    @Test
    public void testCoQuyenCauHinhNullVaKhoaTaiKhoan() {
        assertFalse(service.coQuyenCauHinh(null), "Null user không được cấp quyền");

        NguoiDung directorLocked = new NguoiDung(3, "Giám đốc bị khoá", "director.locked@crm.vn");
        directorLocked.themVaiTro(new VaiTro(VaiTroEnum.DIRECTOR));
        directorLocked.setTrangThai(NguoiDung.TRANG_THAI_KHOA);

        assertFalse(service.coQuyenCauHinh(directorLocked), "Tài khoản bị khoá không được cấp quyền cấu hình");
    }

    @Test
    public void testDieuKienBatBuocQua500KyTuBaoLoi() {
        GiaiDoanPipeline gd = new GiaiDoanPipeline();
        gd.setMaGiaiDoan("LONG_DK");
        gd.setTenGiaiDoan("Mô tả quá dài");
        gd.setThuTu(1);
        gd.setXacSuatThang(30);
        gd.setDieuKienBatBuoc("A".repeat(501));

        KetQuaGiaiDoanDTO ketQua = service.taoGiaiDoan(gd, directorUser);
        assertFalse(ketQua.isThanhCong());
        assertTrue(ketQua.getDanhSachLoi().containsKey("dieuKienBatBuoc"));
    }

    @Test
    public void testFailClosedWhenReferenceCheckThrowsSQLException() throws SQLException {
        GiaiDoanPipeline gd = new GiaiDoanPipeline(7, "STAGE_ERR", "Stage lỗi", 7, 50, "");
        when(daoMock.timTheoId(7)).thenReturn(gd);
        when(daoMock.demSoCoHoiTrongGiaiDoan(7)).thenThrow(new SQLException("Lỗi kết nối CSDL"));

        KetQuaGiaiDoanDTO ketQua = service.xoaGiaiDoan(7, directorUser);

        assertFalse(ketQua.isThanhCong(), "Khi DB bị lỗi kiểm tra tham chiếu, phải fail-closed chặn xóa");
        assertTrue(ketQua.getThongBao().contains("Để bảo vệ dữ liệu, không thực hiện xóa"));
        verify(daoMock, never()).xoaGiaiDoan(7);
    }
}
