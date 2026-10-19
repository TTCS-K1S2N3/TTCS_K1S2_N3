package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dto.DuBaoDoanhSoDTO;
import vn.nhom10.crm.dto.KetQuaGiaiDoanDTO;
import vn.nhom10.crm.model.GiaiDoanPipeline;
import vn.nhom10.crm.model.LoaiGiaiDoanEnum;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.TrangThaiGiaiDoanEnum;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.GiaiDoanPipelineService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử tầng DAO cho GiaiDoanPipelineDAO sử dụng H2 In-Memory (MySQL Mode).
 * Dựa trên đúng bảng pipeline_ban_hang, giai_doan_pipeline, dieu_kien_giai_doan, co_hoi.
 */
public class GiaiDoanPipelineDAOTest {

    private static Connection h2Connection;
    private GiaiDoanPipelineDAO dao;

    @BeforeAll
    public static void setUpDatabase() throws Exception {
        Class.forName("org.h2.Driver");
        h2Connection = DriverManager.getConnection("jdbc:h2:mem:crm_test_pipeline;MODE=MySQL;DB_CLOSE_DELAY=-1");

        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS pipeline_ban_hang (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_pipeline VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_pipeline VARCHAR(150) NOT NULL, " +
                    "mo_ta TEXT, " +
                    "mac_dinh INT NOT NULL DEFAULT 0, " +
                    "hoat_dong INT NOT NULL DEFAULT 1, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.execute("CREATE TABLE IF NOT EXISTS giai_doan_pipeline (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "pipeline_id BIGINT NOT NULL, " +
                    "ma_giai_doan VARCHAR(60) NOT NULL, " +
                    "ten_giai_doan VARCHAR(150) NOT NULL, " +
                    "thu_tu INT NOT NULL, " +
                    "xac_suat_mac_dinh DECIMAL(5,2) NOT NULL DEFAULT 0, " +
                    "so_ngay_dinh_tre INT NULL, " +
                    "loai_ket_thuc VARCHAR(20) NULL, " +
                    "hoat_dong INT NOT NULL DEFAULT 1, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "CONSTRAINT fk_gdp_pl FOREIGN KEY (pipeline_id) REFERENCES pipeline_ban_hang(id) ON DELETE RESTRICT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS dieu_kien_giai_doan (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "giai_doan_nguon_id BIGINT NOT NULL, " +
                    "giai_doan_dich_id BIGINT NULL, " +
                    "ma_dieu_kien VARCHAR(80) NOT NULL, " +
                    "ten_dieu_kien VARCHAR(200) NOT NULL, " +
                    "loai_dieu_kien VARCHAR(50) NOT NULL, " +
                    "cau_hinh_json VARCHAR(1000) NULL, " +
                    "thong_bao_thieu VARCHAR(500) NOT NULL, " +
                    "thu_tu INT NOT NULL DEFAULT 0, " +
                    "hoat_dong INT NOT NULL DEFAULT 1)");

            stmt.execute("CREATE TABLE IF NOT EXISTS co_hoi (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ten_co_hoi VARCHAR(200) NOT NULL, " +
                    "giai_doan_id BIGINT NULL, " +
                    "gia_tri_du_kien DECIMAL(18,2) NOT NULL DEFAULT 0.00, " +
                    "trang_thai VARCHAR(20) NOT NULL DEFAULT 'MO')");

            stmt.execute("CREATE TABLE IF NOT EXISTS lich_su_giai_doan_co_hoi (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "co_hoi_id BIGINT NOT NULL, " +
                    "giai_doan_id BIGINT NOT NULL, " +
                    "giai_doan_truoc_id BIGINT NULL, " +
                    "thoi_gian_chuyen TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "nguoi_thay_doi_id BIGINT NULL)");
        }

        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection("jdbc:h2:mem:crm_test_pipeline;MODE=MySQL;DB_CLOSE_DELAY=-1");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @AfterAll
    public static void tearDown() throws Exception {
        DatabaseConfig.resetConnectionSupplier();
        if (h2Connection != null && !h2Connection.isClosed()) {
            h2Connection.close();
        }
    }

    @BeforeEach
    public void clearData() throws Exception {
        dao = new GiaiDoanPipelineDAO();
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("DELETE FROM lich_su_giai_doan_co_hoi");
            stmt.execute("DELETE FROM co_hoi");
            stmt.execute("DELETE FROM dieu_kien_giai_doan");
            stmt.execute("DELETE FROM giai_doan_pipeline");
            stmt.execute("DELETE FROM pipeline_ban_hang");

            // Seed 1 pipeline mặc định
            stmt.execute("INSERT INTO pipeline_ban_hang (id, ma_pipeline, ten_pipeline, mac_dinh, hoat_dong) " +
                    "VALUES (1, 'PIPELINE_B2B_STANDARD', 'Pipeline Chuẩn B2B', 1, 1)");
        }
    }

    @Test
    public void testThemVaTimGiaiDoan() throws Exception {
        GiaiDoanPipeline gd = new GiaiDoanPipeline();
        gd.setPipelineId(1);
        gd.setMaGiaiDoan("TIEP_CAN");
        gd.setTenGiaiDoan("Tiếp cận");
        gd.setThuTu(1);
        gd.setXacSuatThang(10);
        gd.setDieuKienBatBuoc("Phải có ít nhất 1 cuộc gọi");
        gd.setSoCuocGoiToiThieu(1);
        gd.setLoaiGiaiDoan(LoaiGiaiDoanEnum.DANG_TIEN_HANH);

        int id = dao.themGiaiDoan(gd);
        assertTrue(id > 0);

        GiaiDoanPipeline timThay = dao.timTheoId(id);
        assertNotNull(timThay);
        assertEquals("TIEP_CAN", timThay.getMaGiaiDoan());
        assertEquals("Tiếp cận", timThay.getTenGiaiDoan());
        assertEquals(10, timThay.getXacSuatThang());
        assertEquals(1, timThay.getSoCuocGoiToiThieu());
    }

    @Test
    public void testKiemTraMaTonTai() throws Exception {
        GiaiDoanPipeline gd = new GiaiDoanPipeline();
        gd.setPipelineId(1);
        gd.setMaGiaiDoan("BAO_GIA");
        gd.setTenGiaiDoan("Báo giá");
        gd.setThuTu(4);
        gd.setXacSuatThang(70);

        int id = dao.themGiaiDoan(gd);

        assertTrue(dao.kiemTraMaTonTai("BAO_GIA", null));
        assertTrue(dao.kiemTraMaTonTai("bao_gia", null), "Không phân biệt hoa thường");
        assertFalse(dao.kiemTraMaTonTai("BAO_GIA", id), "Trùng chính ID đang cập nhật thì bỏ qua");
        assertFalse(dao.kiemTraMaTonTai("CHOT_DEAL", null));
    }

    @Test
    public void testDemSoCoHoiTrongGiaiDoanVaBaoToanDuLieu() throws Exception {
        GiaiDoanPipeline gd = new GiaiDoanPipeline();
        gd.setPipelineId(1);
        gd.setMaGiaiDoan("DAM_PHAN");
        gd.setTenGiaiDoan("Đàm phán");
        gd.setThuTu(5);
        gd.setXacSuatThang(85);

        int id = dao.themGiaiDoan(gd);

        // Ban đầu chưa có cơ hội
        assertEquals(0, dao.demSoCoHoiTrongGiaiDoan(id));

        // Thêm 2 cơ hội đang chạy liên kết vào giai đoạn này
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("INSERT INTO co_hoi (id, ten_co_hoi, giai_doan_id, gia_tri_du_kien, trang_thai) " +
                    "VALUES (1, 'Cơ hội 1', " + id + ", 100000000, 'MO'), (2, 'Cơ hội 2', " + id + ", 200000000, 'MO')");
        }

        // Kiểm tra số lượng cơ hội
        assertEquals(2, dao.demSoCoHoiTrongGiaiDoan(id));
        assertEquals(2, dao.demSoCoHoiHienTai(id));
        assertEquals(0, dao.demSoLichSuThamChieu(id));

        // Thêm 1 bản ghi lịch sử tham chiếu
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("INSERT INTO lich_su_giai_doan_co_hoi (co_hoi_id, giai_doan_id, giai_doan_truoc_id, nguoi_thay_doi_id) " +
                    "VALUES (1, " + id + ", NULL, 1)");
        }
        assertEquals(1, dao.demSoLichSuThamChieu(id));
        assertEquals(3, dao.demSoCoHoiTrongGiaiDoan(id));

        // Lấy danh sách kiểm tra xem trường so_co_hoi có ánh xạ đúng không
        List<GiaiDoanPipeline> list = dao.layTatCaGiaiDoan();
        assertEquals(1, list.size());
        assertEquals(2, list.get(0).getSoCoHoiHienTai());

        // Thử xóa giai đoạn đang có cơ hội -> phải ném exception hoặc chặn
        assertThrows(IllegalStateException.class, () -> dao.xoaGiaiDoan(id));
    }

    @Test
    public void testHoanDoiThuTuGiaiDoan() throws Exception {
        GiaiDoanPipeline gd1 = new GiaiDoanPipeline();
        gd1.setPipelineId(1);
        gd1.setMaGiaiDoan("BUOC_1");
        gd1.setTenGiaiDoan("Bước 1");
        gd1.setThuTu(1);
        int id1 = dao.themGiaiDoan(gd1);

        GiaiDoanPipeline gd2 = new GiaiDoanPipeline();
        gd2.setPipelineId(1);
        gd2.setMaGiaiDoan("BUOC_2");
        gd2.setTenGiaiDoan("Bước 2");
        gd2.setThuTu(2);
        int id2 = dao.themGiaiDoan(gd2);

        boolean swapOk = dao.hoanDoiThuTu(id1, 1, id2, 2);
        assertTrue(swapOk);

        assertEquals(2, dao.timTheoId(id1).getThuTu());
        assertEquals(1, dao.timTheoId(id2).getThuTu());
    }

    @Test
    public void testLayThongKeDuBaoPipeline() throws Exception {
        GiaiDoanPipeline gd = new GiaiDoanPipeline();
        gd.setPipelineId(1);
        gd.setMaGiaiDoan("XAC_DINH_NHU_CAU");
        gd.setTenGiaiDoan("Xác định nhu cầu");
        gd.setThuTu(2);
        gd.setXacSuatThang(25);
        int id = dao.themGiaiDoan(gd);

        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("INSERT INTO co_hoi (id, ten_co_hoi, giai_doan_id, gia_tri_du_kien, trang_thai) " +
                    "VALUES (10, 'Hợp đồng 100M', " + id + ", 100000000.00, 'MO')");
        }

        List<DuBaoDoanhSoDTO> thongKe = dao.layThongKeDuBaoPipeline();
        assertFalse(thongKe.isEmpty());
        DuBaoDoanhSoDTO item = thongKe.get(0);
        assertEquals("Xác định nhu cầu", item.getTenGiaiDoan());
        assertEquals(25, item.getXacSuatThang());
        assertEquals(0, new BigDecimal("100000000.00").compareTo(item.getTongGiaTriCoHoi()));
        // Dự báo = 100,000,000 * 25% = 25,000,000
        assertEquals(0, new BigDecimal("25000000.00").compareTo(item.getDoanhSoDuBao()));
    }

    @Test
    public void testChenThuTuBiTrungVaTuDongDoi() throws Exception {
        GiaiDoanPipeline gd1 = new GiaiDoanPipeline();
        gd1.setPipelineId(1);
        gd1.setMaGiaiDoan("BUOC_A");
        gd1.setTenGiaiDoan("Bước A");
        gd1.setThuTu(1);
        int id1 = dao.themGiaiDoan(gd1);

        GiaiDoanPipeline gd2 = new GiaiDoanPipeline();
        gd2.setPipelineId(1);
        gd2.setMaGiaiDoan("BUOC_B");
        gd2.setTenGiaiDoan("Bước B");
        gd2.setThuTu(2);
        int id2 = dao.themGiaiDoan(gd2);

        // Chèn bước mới nhưng chỉ định thứ tự = 1 (trùng với BUOC_A)
        GiaiDoanPipeline gdChen = new GiaiDoanPipeline();
        gdChen.setPipelineId(1);
        gdChen.setMaGiaiDoan("BUOC_CHEN");
        gdChen.setTenGiaiDoan("Bước Chèn Đầu");
        gdChen.setThuTu(1);
        int idChen = dao.themGiaiDoan(gdChen);

        assertTrue(idChen > 0);
        // BUOC_CHEN có thứ tự 1
        assertEquals(1, dao.timTheoId(idChen).getThuTu());
        // BUOC_A bị dời lên thứ tự 2
        assertEquals(2, dao.timTheoId(id1).getThuTu());
        // BUOC_B bị dời lên thứ tự 3
        assertEquals(3, dao.timTheoId(id2).getThuTu());
    }

    @Test
    public void testCapNhatThuTuGiaiDoan() throws Exception {
        GiaiDoanPipeline gd1 = new GiaiDoanPipeline(0, "S1", "Stage 1", 1, 10, "");
        gd1.setPipelineId(1);
        int id1 = dao.themGiaiDoan(gd1);

        GiaiDoanPipeline gd2 = new GiaiDoanPipeline(0, "S2", "Stage 2", 2, 20, "");
        gd2.setPipelineId(1);
        int id2 = dao.themGiaiDoan(gd2);

        GiaiDoanPipeline gd3 = new GiaiDoanPipeline(0, "S3", "Stage 3", 3, 30, "");
        gd3.setPipelineId(1);
        int id3 = dao.themGiaiDoan(gd3);

        // Đổi gd3 từ vị trí 3 lên vị trí 1
        GiaiDoanPipeline gd3Update = dao.timTheoId(id3);
        gd3Update.setThuTu(1);
        boolean updateOk = dao.capNhatGiaiDoan(gd3Update);
        assertTrue(updateOk);

        assertEquals(1, dao.timTheoId(id3).getThuTu());
        assertEquals(2, dao.timTheoId(id1).getThuTu());
        assertEquals(3, dao.timTheoId(id2).getThuTu());
    }

    @Test
    public void testThemGiaiDoanThuTuKhongHopLeTuDongTang() throws Exception {
        GiaiDoanPipeline gd = new GiaiDoanPipeline();
        gd.setPipelineId(1);
        gd.setMaGiaiDoan("AUTO_SEQ");
        gd.setTenGiaiDoan("Tự động gán thứ tự");
        gd.setThuTu(0); // Không chỉ định hoặc <= 0
        int id = dao.themGiaiDoan(gd);

        assertTrue(id > 0);
        assertTrue(dao.timTheoId(id).getThuTu() >= 1);
    }

    @Test
    public void testDocDatabaseRongKhongTuInsert() throws Exception {
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("DELETE FROM pipeline_ban_hang");
            stmt.execute("DELETE FROM giai_doan_pipeline");
        }

        // Đọc danh sách khi DB rỗng
        List<GiaiDoanPipeline> list = dao.layTatCaGiaiDoan();
        assertNotNull(list);
        assertTrue(list.isEmpty(), "DB rỗng thì phải trả về danh sách rỗng");

        // Xác nhận không tự ý mutate/insert pipeline hay giai đoạn nào vào DB
        try (Statement stmt = h2Connection.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM pipeline_ban_hang")) {
            assertTrue(rs.next());
            assertEquals(0, rs.getInt(1), "GET/read tuyệt đối không được tự động INSERT pipeline");
        }
    }

    @Test
    public void testDuBaoBaoToanCoHoiTrongGiaiDoanNgungApDung() throws Exception {
        GiaiDoanPipeline gd = new GiaiDoanPipeline();
        gd.setPipelineId(1);
        gd.setMaGiaiDoan("DIS_STAGE");
        gd.setTenGiaiDoan("Stage Ngừng Áp Dụng");
        gd.setThuTu(1);
        gd.setXacSuatThang(50);
        gd.setTrangThai(TrangThaiGiaiDoanEnum.NGUNG_AP_DUNG);
        int id = dao.themGiaiDoan(gd);

        // Chuyển sang ngừng áp dụng
        dao.capNhatTrangThai(id, TrangThaiGiaiDoanEnum.NGUNG_AP_DUNG);

        // Thêm cơ hội đang mở vào stage này
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("INSERT INTO co_hoi (id, ten_co_hoi, giai_doan_id, gia_tri_du_kien, trang_thai) " +
                    "VALUES (99, 'Hợp đồng trong stage ngừng áp dụng', " + id + ", 50000000.00, 'MO')");
        }

        List<DuBaoDoanhSoDTO> duBao = dao.layThongKeDuBaoPipeline();
        boolean timThay = false;
        for (DuBaoDoanhSoDTO dto : duBao) {
            if ("Stage Ngừng Áp Dụng".equals(dto.getTenGiaiDoan())) {
                timThay = true;
                assertEquals(1, dto.getSoLuongCoHoi());
                assertEquals(0, new BigDecimal("50000000.00").compareTo(dto.getTongGiaTriCoHoi()));
                // 50,000,000 * 50% = 25,000,000
                assertEquals(0, new BigDecimal("25000000.00").compareTo(dto.getDoanhSoDuBao()));
                break;
            }
        }
        assertTrue(timThay, "Cơ hội đang mở trong giai đoạn ngừng áp dụng phải được bảo toàn trong thống kê dự báo (AC 4)");
    }

    @Test
    public void testA_EmptyDbAndGet_ReadOnlyNoMutate() throws Exception {
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("DELETE FROM co_hoi");
            stmt.execute("DELETE FROM dieu_kien_giai_doan");
            stmt.execute("DELETE FROM giai_doan_pipeline");
            stmt.execute("DELETE FROM pipeline_ban_hang");
        }

        // Kiểm tra hiện trạng DB rỗng
        assertEquals(0, demSoBanGhi("pipeline_ban_hang"));
        assertEquals(0, demSoBanGhi("giai_doan_pipeline"));

        // Gọi các thao tác GET / READ
        List<GiaiDoanPipeline> list = dao.layTatCaGiaiDoan();
        assertNotNull(list);
        assertTrue(list.isEmpty());

        List<DuBaoDoanhSoDTO> duBao = dao.layThongKeDuBaoPipeline();
        assertNotNull(duBao);
        assertTrue(duBao.isEmpty());

        // Kiểm tra sau GET: DB vẫn hoàn toàn = 0 rows
        assertEquals(0, demSoBanGhi("pipeline_ban_hang"), "pipeline_ban_hang vẫn = 0");
        assertEquals(0, demSoBanGhi("giai_doan_pipeline"), "giai_doan_pipeline vẫn = 0");
    }

    @Test
    public void testB_EmptyDbAndCreateFirstStage_InitializesParentWithRealId() throws Exception {
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("DELETE FROM co_hoi");
            stmt.execute("DELETE FROM dieu_kien_giai_doan");
            stmt.execute("DELETE FROM giai_doan_pipeline");
            stmt.execute("DELETE FROM pipeline_ban_hang");
        }

        // Tạo stage đầu tiên từ input người dùng
        GiaiDoanPipeline gd = new GiaiDoanPipeline();
        gd.setMaGiaiDoan("KHAO_SAT");
        gd.setTenGiaiDoan("Khảo sát nhu cầu thực tế");
        gd.setThuTu(1);
        gd.setXacSuatThang(20);
        gd.setSoCuocGapToiThieu(1);
        gd.setYeuCauKhaoSatNhuCau(true);
        gd.setLoaiGiaiDoan(LoaiGiaiDoanEnum.DANG_TIEN_HANH);
        gd.setTrangThai(TrangThaiGiaiDoanEnum.DANG_AP_DUNG);

        int stageId = dao.themGiaiDoan(gd);
        assertTrue(stageId > 0, "Insert stage đầu tiên phải thành công");

        // Kỳ vọng sau POST:
        // pipeline_ban_hang = 1 row
        // giai_doan_pipeline = 1 row
        long realPipelineId = -1;
        try (Statement stmt = h2Connection.createStatement();
             ResultSet rsPl = stmt.executeQuery("SELECT id, COUNT(*) OVER() as total FROM pipeline_ban_hang")) {
            assertTrue(rsPl.next());
            assertEquals(1, rsPl.getInt("total"), "pipeline_ban_hang = 1 row");
            realPipelineId = rsPl.getLong("id");
            assertTrue(realPipelineId > 0, "ID thật của pipeline phải > 0");
        }

        try (Statement stmt = h2Connection.createStatement();
             ResultSet rsStage = stmt.executeQuery("SELECT id, pipeline_id, ma_giai_doan, COUNT(*) OVER() as total FROM giai_doan_pipeline")) {
            assertTrue(rsStage.next());
            assertEquals(1, rsStage.getInt("total"), "giai_doan_pipeline = 1 row");
            assertEquals("KHAO_SAT", rsStage.getString("ma_giai_doan"));
            assertEquals(realPipelineId, rsStage.getLong("pipeline_id"), "stage.pipeline_id phải bằng id thật của pipeline_ban_hang");
            assertEquals(realPipelineId, gd.getPipelineId(), "gd.pipelineId phải bằng id thật");
        }

        // dieu_kien_giai_doan có đúng các condition cần thiết
        try (Statement stmt = h2Connection.createStatement();
             ResultSet rsDk = stmt.executeQuery("SELECT COUNT(*) FROM dieu_kien_giai_doan WHERE giai_doan_nguon_id = " + stageId)) {
            assertTrue(rsDk.next());
            assertEquals(2, rsDk.getInt(1), "Phải có đúng 2 điều kiện (cuộc gặp và khảo sát nhu cầu)");
        }
    }

    @Test
    public void testC_CreateSecondStage_UsesSamePipeline() throws Exception {
        // Tiếp nối sau khi đã có 1 pipeline
        testB_EmptyDbAndCreateFirstStage_InitializesParentWithRealId();

        long firstPipelineId = -1;
        try (Statement stmt = h2Connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id FROM pipeline_ban_hang LIMIT 1")) {
            assertTrue(rs.next());
            firstPipelineId = rs.getLong("id");
        }

        // Tạo stage thứ 2
        GiaiDoanPipeline gd2 = new GiaiDoanPipeline();
        gd2.setMaGiaiDoan("DE_XUAT");
        gd2.setTenGiaiDoan("Đề xuất giải pháp kỹ thuật");
        gd2.setThuTu(2);
        gd2.setXacSuatThang(40);
        gd2.setLoaiGiaiDoan(LoaiGiaiDoanEnum.DANG_TIEN_HANH);
        gd2.setTrangThai(TrangThaiGiaiDoanEnum.DANG_AP_DUNG);

        int stageId2 = dao.themGiaiDoan(gd2);
        assertTrue(stageId2 > 0);

        // Kỳ vọng: pipeline_ban_hang vẫn chỉ = 1 row
        try (Statement stmt = h2Connection.createStatement();
             ResultSet rsPl = stmt.executeQuery("SELECT COUNT(*) FROM pipeline_ban_hang")) {
            assertTrue(rsPl.next());
            assertEquals(1, rsPl.getInt(1), "pipeline_ban_hang vẫn chỉ = 1 row");
        }

        // stage thứ 2 dùng cùng pipeline_id
        assertEquals(firstPipelineId, gd2.getPipelineId(), "stage thứ hai dùng cùng pipeline_id");
    }

    @Test
    public void testD_InvalidFirstStage_ValidationBlocksParentCreation() throws Exception {
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("DELETE FROM co_hoi");
            stmt.execute("DELETE FROM dieu_kien_giai_doan");
            stmt.execute("DELETE FROM giai_doan_pipeline");
            stmt.execute("DELETE FROM pipeline_ban_hang");
        }

        GiaiDoanPipelineService service = new GiaiDoanPipelineService(dao);
        NguoiDung director = new NguoiDung(1, "Giám đốc", "director@crm.vn");
        director.themVaiTro(new VaiTro(VaiTroEnum.DIRECTOR));

        // POST với xacSuat = 150
        GiaiDoanPipeline gd = new GiaiDoanPipeline();
        gd.setMaGiaiDoan("INVALID_XS");
        gd.setTenGiaiDoan("Stage sai xác suất");
        gd.setThuTu(1);
        gd.setXacSuatThang(150);

        KetQuaGiaiDoanDTO ketQua = service.themGiaiDoan(gd, director);
        assertFalse(ketQua.isThanhCong(), "Validation phải thất bại");
        assertTrue(ketQua.getDanhSachLoi().containsKey("xacSuatThang"));

        // Kỳ vọng DB:
        // pipeline_ban_hang = 0
        // giai_doan_pipeline = 0
        assertEquals(0, demSoBanGhi("pipeline_ban_hang"), "pipeline_ban_hang = 0");
        assertEquals(0, demSoBanGhi("giai_doan_pipeline"), "giai_doan_pipeline = 0");
    }

    @Test
    public void testE_TransactionRollback_NoOrphanPipelineOnFailure() throws Exception {
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("DELETE FROM co_hoi");
            stmt.execute("DELETE FROM dieu_kien_giai_doan");
            stmt.execute("DELETE FROM giai_doan_pipeline");
            stmt.execute("DELETE FROM pipeline_ban_hang");
        }

        // Ép lỗi khi insert giai_doan_pipeline (ví dụ ma_giai_doan dài 100 ký tự vượt quá VARCHAR(60))
        GiaiDoanPipeline gd = new GiaiDoanPipeline();
        gd.setMaGiaiDoan("A".repeat(100)); // Vi phạm VARCHAR(60) constraint của DB
        gd.setTenGiaiDoan("Stage Lỗi Rollback");
        gd.setThuTu(1);
        gd.setXacSuatThang(30);

        assertThrows(SQLException.class, () -> dao.themGiaiDoan(gd));

        // Kỳ vọng sau rollback: không còn pipeline parent rác
        assertEquals(0, demSoBanGhi("pipeline_ban_hang"), "Rollback hoàn toàn, không còn pipeline parent rác");
        assertEquals(0, demSoBanGhi("giai_doan_pipeline"), "Rollback giai_doan_pipeline = 0");
    }

    @Test
    public void testF_NoSampleStages_OnlyUserCreatedStageExists() throws Exception {
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("DELETE FROM co_hoi");
            stmt.execute("DELETE FROM dieu_kien_giai_doan");
            stmt.execute("DELETE FROM giai_doan_pipeline");
            stmt.execute("DELETE FROM pipeline_ban_hang");
        }

        GiaiDoanPipeline gd = new GiaiDoanPipeline();
        gd.setMaGiaiDoan("KHAO_SAT");
        gd.setTenGiaiDoan("Khảo sát nhu cầu thực tế");
        gd.setThuTu(1);
        gd.setXacSuatThang(20);

        dao.themGiaiDoan(gd);

        List<GiaiDoanPipeline> list = dao.layTatCaGiaiDoan();
        assertEquals(1, list.size(), "Chỉ có đúng 1 giai đoạn do người dùng tạo");
        assertEquals("KHAO_SAT", list.get(0).getMaGiaiDoan());

        // KHÔNG tự sinh 6 stage mẫu
        List<String> mauKhongDuocCo = List.of(
                "TIEP_CAN", "XAC_DINH_NHU_CAU", "DE_XUAT_GIAI_PHAP",
                "BAO_GIA", "DAM_PHAN", "CHOT_THANH_CONG"
        );
        for (String maMau : mauKhongDuocCo) {
            assertFalse(dao.kiemTraMaTonTai(maMau, null), "Tuyệt đối không được tự sinh stage mẫu " + maMau);
        }
    }

    @Test
    public void testConcurrentFirstStageCreation_OnlyOnePipelineCreated() throws Exception {
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("DELETE FROM co_hoi");
            stmt.execute("DELETE FROM dieu_kien_giai_doan");
            stmt.execute("DELETE FROM giai_doan_pipeline");
            stmt.execute("DELETE FROM pipeline_ban_hang");
        }

        int threads = 4;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Callable<Long>> tasks = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            tasks.add(() -> {
                try (Connection conn = DatabaseConfig.getConnection()) {
                    return dao.layHoacTaoPipelineContainerMacDinh(conn);
                }
            });
        }

        List<Future<Long>> results = executor.invokeAll(tasks);
        executor.shutdown();

        Long firstId = null;
        for (Future<Long> f : results) {
            Long pid = f.get();
            assertNotNull(pid);
            assertTrue(pid > 0);
            if (firstId == null) {
                firstId = pid;
            } else {
                assertEquals(firstId, pid, "Tất cả các luồng đồng thời phải dùng chung 1 pipeline_id");
            }
        }

        assertEquals(1, demSoBanGhi("pipeline_ban_hang"), "Chỉ tạo duy nhất 1 pipeline dù có nhiều luồng cùng gọi");
    }

    private int demSoBanGhi(String table) throws SQLException {
        try (Statement stmt = h2Connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }
}
