package vn.nhom10.crm.service;

import vn.nhom10.crm.dto.ThongTinLoi;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service xử lý logic định dạng thông tin báo lỗi và xác định hành động gợi ý.
 * Đảm bảo tiêu chuẩn:
 * - AC1: Cung cấp thông tin chuẩn hóa cho trang lỗi dùng chung giao diện.
 * - AC2: Mỗi mã lỗi đều có ít nhất một hành động gợi ý cụ thể để quay lại luồng làm việc.
 */
public class BaoLoiService {

    private static final Logger LOGGER = Logger.getLogger(BaoLoiService.class.getName());

    /**
     * Tạo đối tượng ThongTinLoi từ mã lỗi HTTP, URI và ngoại lệ (nếu có).
     *
     * @param maLoi           mã trạng thái HTTP (400, 401, 403, 404, 500...)
     * @param requestUri      đường dẫn URI người dùng truy cập
     * @param throwable       ngoại lệ bắt được từ hệ thống
     * @param contextPath     đường dẫn gốc của ứng dụng (context path)
     * @param thongBaoTuyChon thông điệp tùy chỉnh (nếu có)
     * @return ThongTinLoi chứa đầy đủ thông tin hiển thị và hành động gợi ý
     */
    public ThongTinLoi taoThongTinLoi(int maLoi, String requestUri, Throwable throwable, 
                                     String contextPath, String thongBaoTuyChon) {
        String cleanContextPath = (contextPath != null) ? contextPath.replaceAll("/+$", "") : "";
        String urlTrangChu = cleanContextPath.isEmpty() ? "/" : cleanContextPath;
        String urlDangNhap = cleanContextPath + "/dang-nhap";

        ThongTinLoi thongTin = new ThongTinLoi();
        thongTin.setMaLoi(maLoi);

        switch (maLoi) {
            case 403:
                thongTin.setTieuDe("Không Đủ Quyền Truy Cập (403)");
                thongTin.setMoTa(thongBaoTuyChon != null && !thongBaoTuyChon.trim().isEmpty() 
                        ? thongBaoTuyChon 
                        : "Tài khoản của bạn chưa được cấp quyền truy cập mục này, hoặc dữ liệu này nằm ngoài phạm vi được phân công của bạn.");
                thongTin.setChiTiet("Nếu bạn cho rằng đây là một sự nhầm lẫn, vui lòng liên hệ Trưởng nhóm kinh doanh hoặc Quản trị viên hệ thống để được cấp quyền.");
                thongTin.setLoaiGiaoDien("warning");
                thongTin.setBieuTuong("🛡️");

                // AC2: Hành động gợi ý quay lại luồng làm việc
                thongTin.setUrlHanhDongChinh(urlTrangChu);
                thongTin.setTenHanhDongChinh("Về Bàn Làm Việc Của Tôi");
                thongTin.setUrlHanhDongPhu(urlDangNhap);
                thongTin.setTenHanhDongPhu("Đăng Nhập Tài Khoản Khác");
                break;

            case 404:
                thongTin.setTieuDe("Không Tìm Thấy Trang Yêu Cầu (404)");
                thongTin.setMoTa(thongBaoTuyChon != null && !thongBaoTuyChon.trim().isEmpty() 
                        ? thongBaoTuyChon 
                        : "Trang hoặc tài nguyên bạn đang tìm kiếm không tồn tại, đã bị đổi tên hoặc bạn đã nhập sai địa chỉ đường dẫn.");
                thongTin.setChiTiet(requestUri != null ? "Đường dẫn không tồn tại: " + requestUri : null);
                thongTin.setLoaiGiaoDien("info");
                thongTin.setBieuTuong("🔍");

                // AC2: Hành động gợi ý quay lại luồng làm việc
                thongTin.setUrlHanhDongChinh(urlTrangChu);
                thongTin.setTenHanhDongChinh("Quay Về Trang Chủ");
                thongTin.setUrlHanhDongPhu("javascript:history.back()");
                thongTin.setTenHanhDongPhu("Quay Lại Trang Trước");
                break;

            case 401:
                thongTin.setTieuDe("Phiên Đăng Nhập Hết Hạn (401)");
                thongTin.setMoTa("Phiên làm việc của bạn đã hết hạn hoặc bạn chưa đăng nhập vào hệ thống.");
                thongTin.setChiTiet("Vui lòng đăng nhập lại để tiếp tục công việc đang thực hiện.");
                thongTin.setLoaiGiaoDien("info");
                thongTin.setBieuTuong("🔑");

                // AC2: Hành động gợi ý quay lại luồng làm việc
                thongTin.setUrlHanhDongChinh(urlDangNhap);
                thongTin.setTenHanhDongChinh("Đăng Nhập Ngay");
                thongTin.setUrlHanhDongPhu(urlTrangChu);
                thongTin.setTenHanhDongPhu("Quay Về Trang Chủ");
                break;

            case 400:
                thongTin.setTieuDe("Yêu Cầu Không Hợp Lệ (400)");
                thongTin.setMoTa(thongBaoTuyChon != null && !thongBaoTuyChon.trim().isEmpty() 
                        ? thongBaoTuyChon 
                        : "Dữ liệu hoặc tham số bạn gửi lên máy chủ không đúng định dạng quy định.");
                thongTin.setChiTiet("Vui lòng kiểm tra lại thông tin trên biểu mẫu và gửi lại.");
                thongTin.setLoaiGiaoDien("warning");
                thongTin.setBieuTuong("⚠️");

                // AC2: Hành động gợi ý
                thongTin.setUrlHanhDongChinh("javascript:history.back()");
                thongTin.setTenHanhDongChinh("Quay Lại Kiểm Tra Biểu Mẫu");
                thongTin.setUrlHanhDongPhu(urlTrangChu);
                thongTin.setTenHanhDongPhu("Về Trang Chủ");
                break;

            case 500:
            default:
                thongTin.setMaLoi(500);
                thongTin.setTieuDe("Sự Cố Hệ Thống Tạm Thời (500)");
                thongTin.setMoTa("Hệ thống đã ghi nhận một lỗi phát sinh bất ngờ trong quá trình xử lý yêu cầu của bạn.");
                
                // Sinh mã tham chiếu lỗi an toàn để người dùng gửi IT, không lộ stack trace
                String maThamChieu = "ERR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                thongTin.setMaThamChieu(maThamChieu);
                thongTin.setChiTiet("Mã tra cứu sự cố: " + maThamChieu + " (hãy cung cấp mã này cho đội ngũ kỹ thuật nếu sự cố vẫn tiếp diễn).");
                thongTin.setLoaiGiaoDien("danger");
                thongTin.setBieuTuong("⚙️");

                // AC2: Hành động gợi ý quay lại luồng làm việc
                thongTin.setUrlHanhDongChinh("javascript:window.location.reload()");
                thongTin.setTenHanhDongChinh("Thử Tải Lại Trang");
                thongTin.setUrlHanhDongPhu(urlTrangChu);
                thongTin.setTenHanhDongPhu("Quay Về Bàn Làm Việc");

                // Ghi log chi tiết phía server kèm mã tham chiếu
                if (throwable != null) {
                    LOGGER.log(Level.SEVERE, "[" + maThamChieu + "] Lỗi hệ thống tại URI: " + requestUri, throwable);
                } else {
                    LOGGER.log(Level.SEVERE, "[" + maThamChieu + "] Lỗi hệ thống 500 tại URI: " + requestUri);
                }
                break;
        }

        return thongTin;
    }
}
