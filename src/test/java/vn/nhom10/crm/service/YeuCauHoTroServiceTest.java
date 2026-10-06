package vn.nhom10.crm.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dao.CauHinhHeThongDAO;
import vn.nhom10.crm.dao.KhachHangDAO;
import vn.nhom10.crm.dao.ThongBaoDAO;
import vn.nhom10.crm.dao.YeuCauHoTroDAO;
import vn.nhom10.crm.dto.ThongTinRuiRoDTO;
import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.model.MucUuTienYeuCauEnum;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.ThongBao;
import vn.nhom10.crm.model.TrangThaiYeuCauEnum;
import vn.nhom10.crm.model.YeuCauHoTro;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử tầng Service cho Story S3-08:
 * - AC1: Ghi nhận yêu cầu hỗ trợ với mức độ ưu tiên, người xử lý và trạng thái.
 * - AC2: Khách có nhiều yêu cầu chưa xử lý được gắn cờ rủi ro tự động.
 * - AC3: Cờ rủi ro hiển thị trên trang 360 và cảnh báo cho nhân viên kinh doanh phụ trách.
 */
public class YeuCauHoTroServiceTest {

    private static Connection h2Connection;
    private YeuCauHoTroService service;
    private KhachHangDAO khachHangDAO;
    private YeuCauHoTroDAO yeuCauHoTroDAO;
    private ThongBaoDAO thongBaoDAO;
    private CauHinhHeThongDAO cauHinhHeThongDAO;

    private NguoiDung actor;
    private Long khachHangId;
    private final Long salesRepId = 88L;

    @BeforeAll
    public static void setUpAll() throws Exception {
        Class.forName("org.h2.Driver");
        h2Connection = DriverManager.getConnection("jdbc:h2:mem:crm_test_service_s308;MODE=MySQL;DB_CLOSE_DELAY=-1");

        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS nguoi_dung (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(255) NOT NULL, " +
                    "email VARCHAR(255) NOT NULL UNIQUE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS nhom_kinh_doanh (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ten_nhom VARCHAR(255) NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS khach_hang (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_khach_hang VARCHAR(50) NULL UNIQUE, " +
                    "ten_cong_ty VARCHAR(255) NOT NULL, " +
                    "ten_chuan_hoa VARCHAR(255) NULL, " +
                    "ma_so_thue VARCHAR(50) NULL, " +
                    "nganh_nghe_id BIGINT NULL, " +
                    "quy_mo_id BIGINT NULL, " +
                    "website VARCHAR(255) NULL, " +
                    "website_chuan_hoa VARCHAR(255) NULL, " +
                    "dia_chi VARCHAR(500) NULL, " +
                    "khu_vuc_id BIGINT NULL, " +
                    "nguoi_so_huu_id BIGINT NOT NULL, " +
                    "nhom_kinh_doanh_id BIGINT NULL, " +
                    "doanh_thu_uoc_tinh DECIMAL(18,2) DEFAULT 0.00, " +
                    "cong_ty_me_id BIGINT NULL, " +
                    "trang_thai VARCHAR(50) DEFAULT 'TIEM_NANG', " +
                    "co_rui_ro TINYINT(1) DEFAULT 0, " +
                    "rui_ro_cap_nhat_luc TIMESTAMP NULL, " +
                    "lan_tuong_tac_cuoi TIMESTAMP NULL, " +
                    "gop_vao_khach_hang_id BIGINT NULL, " +
                    "mo_ta_chi_tiet TEXT NULL, " +
                    "ngay_tao DATE DEFAULT CURRENT_DATE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.execute("CREATE TABLE IF NOT EXISTS nguoi_lien_he (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(255) NOT NULL, " +
                    "so_dien_thoai VARCHAR(50) NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS yeu_cau_ho_tro (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_yeu_cau VARCHAR(50) NOT NULL UNIQUE, " +
                    "khach_hang_id BIGINT NOT NULL, " +
                    "nguoi_lien_he_id BIGINT NULL, " +
                    "nguoi_xu_ly_id BIGINT NULL, " +
                    "tieu_de VARCHAR(255) NOT NULL, " +
                    "noi_dung TEXT NULL, " +
                    "muc_uu_tien VARCHAR(20) DEFAULT 'BINH_THUONG', " +
                    "trang_thai VARCHAR(30) DEFAULT 'MOI', " +
                    "tao_luc TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "xu_ly_luc TIMESTAMP NULL, " +
                    "hoan_tat_luc TIMESTAMP NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS thong_bao (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "nguoi_dung_id BIGINT NOT NULL, " +
                    "loai_thong_bao_id BIGINT NOT NULL, " +
                    "tieu_de VARCHAR(255) NOT NULL, " +
                    "noi_dung TEXT NULL, " +
                    "loai_doi_tuong VARCHAR(50) NULL, " +
                    "doi_tuong_id BIGINT NULL, " +
                    "duong_dan_mo VARCHAR(500) NULL, " +
                    "da_doc TINYINT(1) DEFAULT 0, " +
                    "doc_luc TIMESTAMP NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.execute("CREATE TABLE IF NOT EXISTS cau_hinh_he_thong (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_cau_hinh VARCHAR(100) NOT NULL UNIQUE, " +
                    "nhom_cau_hinh VARCHAR(60) NOT NULL, " +
                    "kieu_du_lieu VARCHAR(30) NOT NULL, " +
                    "gia_tri TEXT NULL, " +
                    "mo_ta VARCHAR(1000) NULL, " +
                    "bat_buoc_cau_hinh TINYINT(1) DEFAULT 0, " +
                    "bao_mat TINYINT(1) DEFAULT 0, " +
                    "updated_by BIGINT NULL, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.execute("INSERT INTO nguoi_dung (id, ho_ten, email) VALUES (88, 'Vũ Đức Sales', 'sales88@crm.vn')");
            stmt.execute("INSERT INTO nguoi_dung (id, ho_ten, email) VALUES (99, 'Lê Thị CSKH', 'cskh99@crm.vn')");
        }

        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection("jdbc:h2:mem:crm_test_service_s308;MODE=MySQL");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @AfterAll
    public static void tearDownAll() throws Exception {
        DatabaseConfig.resetConnectionSupplier();
        if (h2Connection != null && !h2Connection.isClosed()) {
            h2Connection.close();
        }
    }

    @BeforeEach
    public void setUp() {
        yeuCauHoTroDAO = new YeuCauHoTroDAO();
        khachHangDAO = new KhachHangDAO();
        thongBaoDAO = new ThongBaoDAO();
        cauHinhHeThongDAO = new CauHinhHeThongDAO();
        service = new YeuCauHoTroService(yeuCauHoTroDAO, khachHangDAO, thongBaoDAO, cauHinhHeThongDAO);

        actor = new NguoiDung();
        actor.setId(99L);
        actor.setHoTen("Lê Thị CSKH");

        // Tạo khách hàng test mới cho mỗi kịch bản
        KhachHang kh = new KhachHang("Công ty Cổ phần Alpha " + System.currentTimeMillis(), salesRepId);
        kh.setMaKhachHang("KH-TEST-" + System.currentTimeMillis());
        khachHangId = khachHangDAO.themKhachHang(kh);

        // Thiết lập ngưỡng rủi ro mặc định là 3
        cauHinhHeThongDAO.datGiaTri(CauHinhHeThongDAO.NGUONG_YEU_CAU_HO_TRO_RUI_RO, "3", 1L);
    }

    @Test
    public void testAC1_GhiNhanYeuCauHopLe() {
        YeuCauHoTro ycht = new YeuCauHoTro();
        ycht.setKhachHangId(khachHangId);
        ycht.setTieuDe("Sự cố tích hợp cổng thanh toán");
        ycht.setNoiDung("Khách hàng không nhận được callback kết quả");
        ycht.setMucUuTien(MucUuTienYeuCauEnum.CAO.getMa());
        ycht.setNguoiXuLyId(actor.getId());
        ycht.setTrangThai(TrangThaiYeuCauEnum.MOI.getMa());

        YeuCauHoTro daLuu = service.ghiNhanYeuCau(ycht, actor);
        assertNotNull(daLuu.getId());
        assertNotNull(daLuu.getMaYeuCau());
        assertTrue(daLuu.getMaYeuCau().startsWith("TK-"));
        assertEquals("CAO", daLuu.getMucUuTien());
        assertEquals("MOI", daLuu.getTrangThai());

        // Khách hàng mới có 1 yêu cầu chưa xử lý, chưa đạt ngưỡng 3 -> chưa có cờ rủi ro
        assertFalse(khachHangDAO.kiemTraCoRuiRo(khachHangId));
    }

    @Test
    public void testAC1_ValidateDuLieuDauVao() {
        // Thiếu khách hàng
        YeuCauHoTro ycht1 = new YeuCauHoTro();
        ycht1.setTieuDe("Lỗi hệ thống");
        assertThrows(IllegalArgumentException.class, () -> service.ghiNhanYeuCau(ycht1, actor));

        // Tiêu đề rỗng
        YeuCauHoTro ycht2 = new YeuCauHoTro();
        ycht2.setKhachHangId(khachHangId);
        ycht2.setTieuDe("   ");
        assertThrows(IllegalArgumentException.class, () -> service.ghiNhanYeuCau(ycht2, actor));

        // Khách hàng không tồn tại
        YeuCauHoTro ycht3 = new YeuCauHoTro();
        ycht3.setKhachHangId(9999999L);
        ycht3.setTieuDe("Hỗ trợ tài khoản");
        assertThrows(IllegalArgumentException.class, () -> service.ghiNhanYeuCau(ycht3, actor));
    }

    @Test
    public void testAC2_GanCoRuiRoTuDongKhiDatNguong() {
        // Thêm yêu cầu thứ 1: Chưa xử lý
        YeuCauHoTro y1 = new YeuCauHoTro(khachHangId, "Sự cố phần mềm đợt 1", "Mô tả 1", "BINH_THUONG", actor.getId());
        service.ghiNhanYeuCau(y1, actor);
        assertFalse(khachHangDAO.kiemTraCoRuiRo(khachHangId), "1 yêu cầu chưa bị gắn cờ");

        // Thêm yêu cầu thứ 2: Chưa xử lý
        YeuCauHoTro y2 = new YeuCauHoTro(khachHangId, "Sự cố phần mềm đợt 2", "Mô tả 2", "CAO", actor.getId());
        service.ghiNhanYeuCau(y2, actor);
        assertFalse(khachHangDAO.kiemTraCoRuiRo(khachHangId), "2 yêu cầu chưa bị gắn cờ");

        // Thêm yêu cầu thứ 3: Chưa xử lý (đạt ngưỡng 3)
        YeuCauHoTro y3 = new YeuCauHoTro(khachHangId, "Sự cố phần mềm đợt 3", "Mô tả 3", "KHAN_CAP", actor.getId());
        service.ghiNhanYeuCau(y3, actor);

        // AC2: Khách hàng TỰ ĐỘNG ĐƯỢC GẮN CỜ RỦI RO!
        assertTrue(khachHangDAO.kiemTraCoRuiRo(khachHangId), "Khách hàng phải tự động được gắn cờ rủi ro khi đạt ngưỡng 3");

        // AC3: Kiểm tra đã gửi thông báo cảnh báo cho nhân viên kinh doanh phụ trách (salesRepId = 88)
        List<ThongBao> dsThongBao = thongBaoDAO.layDanhSachTheoNguoiDung(salesRepId, false, 10);
        assertFalse(dsThongBao.isEmpty(), "NVKD phụ trách phải nhận được thông báo cảnh báo");
        ThongBao tb = dsThongBao.get(0);
        assertTrue(tb.getTieuDe().contains("Cảnh báo rủi ro rời bỏ"));
        assertEquals(ThongBaoDAO.LOAI_THONG_BAO_KHACH_HANG_RUI_RO, tb.getLoaiThongBaoId());
        assertEquals("KHACH_HANG", tb.getLoaiDoiTuong());
        assertEquals(khachHangId, tb.getDoiTuongId());
    }

    @Test
    public void testAC2_GoCoRuiRoTuDongKhiSoYeuCauChuaXuLyGiamDuoiNguong() {
        // Tạo 3 yêu cầu chưa xử lý để khách hàng bị gắn cờ
        YeuCauHoTro y1 = service.ghiNhanYeuCau(new YeuCauHoTro(khachHangId, "Yêu cầu 1", "Nội dung", "BINH_THUONG", actor.getId()), actor);
        YeuCauHoTro y2 = service.ghiNhanYeuCau(new YeuCauHoTro(khachHangId, "Yêu cầu 2", "Nội dung", "BINH_THUONG", actor.getId()), actor);
        YeuCauHoTro y3 = service.ghiNhanYeuCau(new YeuCauHoTro(khachHangId, "Yêu cầu 3", "Nội dung", "BINH_THUONG", actor.getId()), actor);

        assertTrue(khachHangDAO.kiemTraCoRuiRo(khachHangId), "Phải gắn cờ khi có 3 yêu cầu chưa xử lý");

        // Đội ngũ giải quyết xong 1 yêu cầu (chuyển sang DA_XU_LY)
        service.capNhatTrangThai(y1.getId(), TrangThaiYeuCauEnum.DA_XU_LY.getMa(), actor);

        // Số yêu cầu chưa xử lý giảm xuống 2 (dưới ngưỡng 3) -> AC2: TỰ ĐỘNG GỠ CỜ RỦI RO!
        assertFalse(khachHangDAO.kiemTraCoRuiRo(khachHangId), "Phải tự động gỡ cờ khi số yêu cầu chưa xử lý giảm dưới ngưỡng");
    }

    @Test
    public void testAC3_LayThongTinRuiRoTrang360() {
        // Ban đầu an toàn
        ThongTinRuiRoDTO ruiRoBanDau = service.layThongTinRuiRo(khachHangId);
        assertNotNull(ruiRoBanDau);
        assertFalse(ruiRoBanDau.isCoRuiRo());
        assertEquals(0, ruiRoBanDau.getSoYeuCauChuaXuLy());
        assertEquals(3, ruiRoBanDau.getNguongRuiRo());

        // Thêm 3 yêu cầu
        service.ghiNhanYeuCau(new YeuCauHoTro(khachHangId, "Yêu cầu 1", "Nội dung", "THAP", actor.getId()), actor);
        service.ghiNhanYeuCau(new YeuCauHoTro(khachHangId, "Yêu cầu 2", "Nội dung", "CAO", actor.getId()), actor);
        service.ghiNhanYeuCau(new YeuCauHoTro(khachHangId, "Yêu cầu 3", "Nội dung", "KHAN_CAP", actor.getId()), actor);

        // Sau khi đạt ngưỡng
        ThongTinRuiRoDTO ruiRoSauKhiGanCo = service.layThongTinRuiRo(khachHangId);
        assertNotNull(ruiRoSauKhiGanCo);
        assertTrue(ruiRoSauKhiGanCo.isCoRuiRo());
        assertEquals(3, ruiRoSauKhiGanCo.getSoYeuCauChuaXuLy());
        assertTrue(ruiRoSauKhiGanCo.getThongDiepCanhBao().contains("Nguy cơ rời bỏ cao"));
    }

    @Test
    public void testAC1_CapNhatYeuCauVaTrangThai() {
        YeuCauHoTro y = new YeuCauHoTro(khachHangId, "Cần sửa cấu hình", "Mô tả ban đầu", "THAP", actor.getId());
        YeuCauHoTro daTao = service.ghiNhanYeuCau(y, actor);

        daTao.setTieuDe("Cần sửa cấu hình nâng cao");
        daTao.setMucUuTien("CAO");
        daTao.setNoiDung("Đã bổ sung yêu cầu chi tiết");
        boolean ok = service.capNhatYeuCau(daTao, actor);
        assertTrue(ok);

        YeuCauHoTro sauCapNhat = service.timTheoId(daTao.getId());
        assertEquals("Cần sửa cấu hình nâng cao", sauCapNhat.getTieuDe());
        assertEquals("CAO", sauCapNhat.getMucUuTien());
        assertEquals("Đã bổ sung yêu cầu chi tiết", sauCapNhat.getNoiDung());
    }

    @Test
    public void testAC2_TuyChinhNguongCauHinh() {
        // Đổi ngưỡng trong cấu hình hệ thống từ 3 lên 5
        cauHinhHeThongDAO.datGiaTri(CauHinhHeThongDAO.NGUONG_YEU_CAU_HO_TRO_RUI_RO, "5", 1L);

        // Tạo 3 yêu cầu chưa xử lý
        service.ghiNhanYeuCau(new YeuCauHoTro(khachHangId, "Yêu cầu 1", "Nội dung", "THAP", actor.getId()), actor);
        service.ghiNhanYeuCau(new YeuCauHoTro(khachHangId, "Yêu cầu 2", "Nội dung", "THAP", actor.getId()), actor);
        service.ghiNhanYeuCau(new YeuCauHoTro(khachHangId, "Yêu cầu 3", "Nội dung", "THAP", actor.getId()), actor);

        // Với ngưỡng 5, có 3 yêu cầu thì vẫn CHƯA bị gắn cờ
        assertFalse(khachHangDAO.kiemTraCoRuiRo(khachHangId));

        // Thêm yêu cầu thứ 4 và 5 -> đạt ngưỡng 5
        service.ghiNhanYeuCau(new YeuCauHoTro(khachHangId, "Yêu cầu 4", "Nội dung", "THAP", actor.getId()), actor);
        service.ghiNhanYeuCau(new YeuCauHoTro(khachHangId, "Yêu cầu 5", "Nội dung", "THAP", actor.getId()), actor);

        // Đạt ngưỡng 5 -> gắn cờ!
        assertTrue(khachHangDAO.kiemTraCoRuiRo(khachHangId));
    }
}
