package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dto.DuBaoDoanhSoDTO;
import vn.nhom10.crm.model.GiaiDoanPipeline;
import vn.nhom10.crm.model.LoaiGiaiDoanEnum;
import vn.nhom10.crm.model.TrangThaiGiaiDoanEnum;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

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
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

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
}
