package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.MucUuTienYeuCauEnum;
import vn.nhom10.crm.model.TrangThaiYeuCauEnum;
import vn.nhom10.crm.model.YeuCauHoTro;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class YeuCauHoTroDAOTest {

    private static Connection h2Connection;
    private final YeuCauHoTroDAO dao = new YeuCauHoTroDAO();

    @BeforeAll
    public static void setUp() throws Exception {
        Class.forName("org.h2.Driver");
        h2Connection = DriverManager.getConnection("jdbc:h2:mem:crm_test_ycht;MODE=MySQL;DB_CLOSE_DELAY=-1");

        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS nguoi_dung (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(255) NOT NULL, " +
                    "email VARCHAR(255) NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS nguoi_lien_he (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(255) NOT NULL, " +
                    "so_dien_thoai VARCHAR(50) NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS khach_hang (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_khach_hang VARCHAR(50) NULL, " +
                    "ten_cong_ty VARCHAR(255) NOT NULL)");

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

            stmt.execute("INSERT INTO nguoi_dung (id, ho_ten, email) VALUES (1, 'Kỹ thuật viên 1', 'kt1@crm.vn')");
            stmt.execute("INSERT INTO nguoi_lien_he (id, ho_ten, so_dien_thoai) VALUES (1, 'Chị Mai Kế toán', '0912345678')");
            stmt.execute("INSERT INTO khach_hang (id, ma_khach_hang, ten_cong_ty) VALUES (10, 'KH-010', 'Công ty Thử Nghiệm')");
        }

        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection("jdbc:h2:mem:crm_test_ycht;MODE=MySQL");
            } catch (Exception e) {
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

    @Test
    public void testThemYeuCauVaDemYeuCauChuaXuLy() {
        YeuCauHoTro y1 = new YeuCauHoTro();
        y1.setKhachHangId(10L);
        y1.setNguoiXuLyId(1L);
        y1.setNguoiLienHeId(1L);
        y1.setTieuDe("Lỗi không xuất được hóa đơn điện tử");
        y1.setNoiDung("Khách hàng báo lỗi cổng kết nối");
        y1.setMucUuTien(MucUuTienYeuCauEnum.KHAN_CAP.getMa());
        y1.setTrangThai(TrangThaiYeuCauEnum.MOI.getMa());

        Long id1 = dao.themYeuCau(y1);
        assertNotNull(id1);
        assertNotNull(y1.getMaYeuCau());

        YeuCauHoTro y2 = new YeuCauHoTro();
        y2.setKhachHangId(10L);
        y2.setTieuDe("Hỗ trợ đào tạo phân quyền người dùng mới");
        y2.setMucUuTien(MucUuTienYeuCauEnum.BINH_THUONG.getMa());
        y2.setTrangThai(TrangThaiYeuCauEnum.DANG_XU_LY.getMa());
        y2.setXuLyLuc(LocalDateTime.now());
        Long id2 = dao.themYeuCau(y2);
        assertNotNull(id2);

        // Lúc này có 2 yêu cầu chưa xử lý (MOI, DANG_XU_LY)
        assertEquals(2, dao.demYeuCauChuaXuLy(10L));

        // Thêm yêu cầu thứ 3 ở trạng thái DA_XU_LY (đã xong)
        YeuCauHoTro y3 = new YeuCauHoTro();
        y3.setKhachHangId(10L);
        y3.setTieuDe("Yêu cầu gửi tài liệu hướng dẫn");
        y3.setTrangThai(TrangThaiYeuCauEnum.DA_XU_LY.getMa());
        dao.themYeuCau(y3);

        // Vẫn là 2 yêu cầu chưa xử lý
        assertEquals(2, dao.demYeuCauChuaXuLy(10L));

        // Đổi y1 sang DA_XU_LY
        dao.capNhatTrangThai(id1, TrangThaiYeuCauEnum.DA_XU_LY.getMa(), null, LocalDateTime.now());
        // Số yêu cầu chưa xử lý giảm xuống 1
        assertEquals(1, dao.demYeuCauChuaXuLy(10L));

        List<YeuCauHoTro> ds = dao.layDanhSachTheoKhachHang(10L);
        assertEquals(3, ds.size());
    }
}
