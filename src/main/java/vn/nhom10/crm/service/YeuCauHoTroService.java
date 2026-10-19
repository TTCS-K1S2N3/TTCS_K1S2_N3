package vn.nhom10.crm.service;

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

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service xử lý nghiệp vụ Yêu cầu hỗ trợ và Cảnh báo rủi ro rời bỏ khách hàng (Story S3-08).
 * - AC1: Ghi nhận yêu cầu hỗ trợ với mức độ ưu tiên, người xử lý và trạng thái.
 * - AC2: Khách có nhiều yêu cầu chưa xử lý được gắn cờ rủi ro tự động.
 * - AC3: Cờ rủi ro hiển thị trên trang 360 và cảnh báo cho nhân viên kinh doanh phụ trách.
 */
public class YeuCauHoTroService {

    private static final Logger LOGGER = Logger.getLogger(YeuCauHoTroService.class.getName());

    private final YeuCauHoTroDAO yeuCauHoTroDAO;
    private final KhachHangDAO khachHangDAO;
    private final ThongBaoDAO thongBaoDAO;
    private final CauHinhHeThongDAO cauHinhHeThongDAO;

    public YeuCauHoTroService() {
        this.yeuCauHoTroDAO = new YeuCauHoTroDAO();
        this.khachHangDAO = new KhachHangDAO();
        this.thongBaoDAO = new ThongBaoDAO();
        this.cauHinhHeThongDAO = new CauHinhHeThongDAO();
    }

    public YeuCauHoTroService(YeuCauHoTroDAO yeuCauHoTroDAO, KhachHangDAO khachHangDAO,
                             ThongBaoDAO thongBaoDAO, CauHinhHeThongDAO cauHinhHeThongDAO) {
        this.yeuCauHoTroDAO = yeuCauHoTroDAO != null ? yeuCauHoTroDAO : new YeuCauHoTroDAO();
        this.khachHangDAO = khachHangDAO != null ? khachHangDAO : new KhachHangDAO();
        this.thongBaoDAO = thongBaoDAO != null ? thongBaoDAO : new ThongBaoDAO();
        this.cauHinhHeThongDAO = cauHinhHeThongDAO != null ? cauHinhHeThongDAO : new CauHinhHeThongDAO();
    }

    /**
     * Ghi nhận một yêu cầu hỗ trợ sau bán (AC1) và tự động cập nhật cờ rủi ro (AC2).
     *
     * @param ycht  Đối tượng yêu cầu hỗ trợ cần ghi nhận
     * @param actor Người thực hiện thao tác (nhân viên CSKH / NVKD)
     * @return YeuCauHoTro đã được lưu với ID hợp lệ
     * @throws IllegalArgumentException khi dữ liệu không hợp lệ
     */
    public YeuCauHoTro ghiNhanYeuCau(YeuCauHoTro ycht, NguoiDung actor) {
        if (ycht == null) {
            throw new IllegalArgumentException("Thông tin yêu cầu hỗ trợ không được để trống.");
        }

        if (ycht.getKhachHangId() == null || ycht.getKhachHangId() <= 0) {
            throw new IllegalArgumentException("Khách hàng bắt buộc phải được chọn.");
        }

        if (ycht.getTieuDe() == null || ycht.getTieuDe().trim().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề yêu cầu hỗ trợ không được để trống.");
        }

        if (ycht.getTieuDe().trim().length() > 255) {
            throw new IllegalArgumentException("Tiêu đề yêu cầu không được vượt quá 255 ký tự.");
        }

        KhachHang kh = khachHangDAO.timTheoId(ycht.getKhachHangId());
        if (kh == null) {
            throw new IllegalArgumentException("Khách hàng với ID [" + ycht.getKhachHangId() + "] không tồn tại trong hệ thống.");
        }

        // Chuẩn hóa mức độ ưu tiên và trạng thái
        MucUuTienYeuCauEnum mucUuTienEnum = MucUuTienYeuCauEnum.tuMa(ycht.getMucUuTien());
        ycht.setMucUuTien(mucUuTienEnum.getMa());

        TrangThaiYeuCauEnum trangThaiEnum = TrangThaiYeuCauEnum.tuMa(ycht.getTrangThai());
        ycht.setTrangThai(trangThaiEnum.getMa());

        // Thiết lập thời gian khởi tạo
        if (ycht.getTaoLuc() == null) {
            ycht.setTaoLuc(LocalDateTime.now());
        }

        if (trangThaiEnum == TrangThaiYeuCauEnum.DANG_XU_LY && ycht.getXuLyLuc() == null) {
            ycht.setXuLyLuc(LocalDateTime.now());
        } else if ((trangThaiEnum == TrangThaiYeuCauEnum.DA_XU_LY || trangThaiEnum == TrangThaiYeuCauEnum.DONG)
                && ycht.getHoanTatLuc() == null) {
            ycht.setHoanTatLuc(LocalDateTime.now());
        }

        Long genId = yeuCauHoTroDAO.themYeuCau(ycht);
        if (genId == null) {
            throw new RuntimeException("Không thể ghi nhận yêu cầu hỗ trợ vào cơ sở dữ liệu.");
        }
        ycht.setId(genId);

        // AC2: Tự động đánh giá số yêu cầu chưa xử lý và gắn cờ rủi ro nếu vượt ngưỡng
        kiemTraVaCapNhatCoRuiRo(ycht.getKhachHangId(), actor);

        return ycht;
    }

    /**
     * Cập nhật trạng thái của yêu cầu hỗ trợ (AC1) và tự động cập nhật cờ rủi ro (AC2).
     */
    public boolean capNhatTrangThai(Long yeuCauId, String trangThaiMoi, NguoiDung actor) {
        if (yeuCauId == null || yeuCauId <= 0) {
            throw new IllegalArgumentException("ID yêu cầu không hợp lệ.");
        }

        YeuCauHoTro hienTai = yeuCauHoTroDAO.timTheoId(yeuCauId);
        if (hienTai == null) {
            throw new IllegalArgumentException("Không tìm thấy yêu cầu hỗ trợ có ID: " + yeuCauId);
        }

        TrangThaiYeuCauEnum statusEnum = TrangThaiYeuCauEnum.tuMa(trangThaiMoi);
        LocalDateTime xuLyLuc = hienTai.getXuLyLuc();
        LocalDateTime hoanTatLuc = hienTai.getHoanTatLuc();

        if (statusEnum == TrangThaiYeuCauEnum.DANG_XU_LY && xuLyLuc == null) {
            xuLyLuc = LocalDateTime.now();
        } else if (statusEnum == TrangThaiYeuCauEnum.DA_XU_LY || statusEnum == TrangThaiYeuCauEnum.DONG) {
            if (hoanTatLuc == null) {
                hoanTatLuc = LocalDateTime.now();
            }
        } else if (statusEnum.isChuaXuLy()) {
            // Nếu mở lại yêu cầu (từ đã đóng/xong chuyển về mới/đang xử lý), reset hoanTatLuc
            hoanTatLuc = null;
        }

        boolean success = yeuCauHoTroDAO.capNhatTrangThai(yeuCauId, statusEnum.getMa(), xuLyLuc, hoanTatLuc);
        if (success) {
            // AC2: Đánh giá lại cờ rủi ro sau khi trạng thái thay đổi
            kiemTraVaCapNhatCoRuiRo(hienTai.getKhachHangId(), actor);
        }
        return success;
    }

    /**
     * Cập nhật đầy đủ thông tin yêu cầu hỗ trợ (người xử lý, mức độ ưu tiên, nội dung).
     */
    public boolean capNhatYeuCau(YeuCauHoTro ycht, NguoiDung actor) {
        if (ycht == null || ycht.getId() == null) {
            throw new IllegalArgumentException("Yêu cầu hỗ trợ không hợp lệ.");
        }

        YeuCauHoTro hienTai = yeuCauHoTroDAO.timTheoId(ycht.getId());
        if (hienTai == null) {
            throw new IllegalArgumentException("Yêu cầu hỗ trợ không tồn tại.");
        }

        if (ycht.getTieuDe() == null || ycht.getTieuDe().trim().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề không được để trống.");
        }

        ycht.setMucUuTien(MucUuTienYeuCauEnum.tuMa(ycht.getMucUuTien()).getMa());
        ycht.setTrangThai(TrangThaiYeuCauEnum.tuMa(ycht.getTrangThai()).getMa());

        boolean success = yeuCauHoTroDAO.capNhatYeuCau(ycht);
        if (success) {
            kiemTraVaCapNhatCoRuiRo(hienTai.getKhachHangId(), actor);
        }
        return success;
    }

    /**
     * Lõi nghiệp vụ AC2 & AC3:
     * - Kiểm tra số lượng yêu cầu hỗ trợ chưa xử lý của khách hàng.
     * - Đối chiếu với cấu hình ngưỡng rủi ro trong `cau_hinh_he_thong` (`NGUONG_YEU_CAU_HO_TRO_RUI_RO`).
     * - Tự động GẮN CỜ rủi ro khi số yêu cầu chưa xử lý >= ngưỡng, đồng thời gửi CẢNH BÁO cho NVKD phụ trách.
     * - Tự động GỠ CỜ rủi ro khi số yêu cầu chưa xử lý giảm xuống dưới ngưỡng.
     *
     * @param khachHangId ID khách hàng
     * @param actor       Người thực hiện tác vụ gây ra cập nhật
     * @return Trạng thái cờ rủi ro sau khi kiểm tra (true: có rủi ro, false: an toàn)
     */
    public boolean kiemTraVaCapNhatCoRuiRo(Long khachHangId, NguoiDung actor) {
        if (khachHangId == null || khachHangId <= 0) {
            return false;
        }

        KhachHang kh = khachHangDAO.timTheoId(khachHangId);
        if (kh == null) {
            return false;
        }

        // 1. Đọc ngưỡng cấu hình hệ thống (mặc định là 3 nếu chưa cấu hình hoặc là NULL)
        int nguong = cauHinhHeThongDAO.layGiaTriInt(
                CauHinhHeThongDAO.NGUONG_YEU_CAU_HO_TRO_RUI_RO,
                CauHinhHeThongDAO.NGUONG_RUI_RO_MAC_DINH
        );

        // 2. Đếm số yêu cầu hỗ trợ chưa xử lý hiện tại
        int soChuaXuLy = yeuCauHoTroDAO.demYeuCauChuaXuLy(khachHangId);

        boolean coRuiRoHienTai = kh.isCoRuiRo();
        boolean canGanCo = (soChuaXuLy >= nguong);

        if (canGanCo) {
            // Nếu chưa được gắn cờ -> Gắn cờ tự động & gửi thông báo cảnh báo cho NVKD phụ trách (AC2, AC3)
            if (!coRuiRoHienTai) {
                khachHangDAO.capNhatCoRuiRo(khachHangId, true);
                LOGGER.info("Đã TỰ ĐỘNG GẮN CỜ RỦI RO cho khách hàng [ID: " + khachHangId + ", Tên: " + kh.getTenCongTy() +
                        "] do có " + soChuaXuLy + "/" + nguong + " yêu cầu chưa xử lý.");

                // Gửi thông báo cảnh báo cho nhân viên kinh doanh phụ trách khách hàng (AC3)
                if (kh.getNguoiSoHuuId() != null && kh.getNguoiSoHuuId() > 0) {
                    guiThongBaoCanhBaoRuiRo(kh, soChuaXuLy, nguong);
                }
            }
            return true;
        } else {
            // Nếu trước đó đang có cờ rủi ro nhưng số yêu cầu chưa xử lý đã giảm xuống dưới ngưỡng -> Tự động gỡ cờ
            if (coRuiRoHienTai) {
                khachHangDAO.capNhatCoRuiRo(khachHangId, false);
                LOGGER.info("Đã TỰ ĐỘNG GỠ CỜ RỦI RO cho khách hàng [ID: " + khachHangId + ", Tên: " + kh.getTenCongTy() +
                        "] do số yêu cầu chưa xử lý đã giảm còn " + soChuaXuLy + " (dưới ngưỡng " + nguong + ").");
            }
            return false;
        }
    }

    /**
     * Gửi bản ghi thông báo cảnh báo cho nhân viên kinh doanh phụ trách khách hàng (AC3).
     */
    private void guiThongBaoCanhBaoRuiRo(KhachHang kh, int soChuaXuLy, int nguong) {
        try {
            ThongBao tb = new ThongBao();
            tb.setNguoiDungId(kh.getNguoiSoHuuId());
            tb.setLoaiThongBaoId(ThongBaoDAO.LOAI_THONG_BAO_KHACH_HANG_RUI_RO);
            tb.setTieuDe("[Cảnh báo rủi ro rời bỏ] Khách hàng " + kh.getTenCongTy());
            tb.setNoiDung("Khách hàng '" + kh.getTenCongTy() + "' hiện đang có " + soChuaXuLy +
                    " yêu cầu hỗ trợ chưa được xử lý (vượt ngưỡng cảnh báo " + nguong + " yêu cầu). " +
                    "Vui lòng phối hợp với bộ phận CSKH và chủ động liên hệ khách hàng ngay để ngăn ngừa rủi ro rời bỏ.");
            tb.setLoaiDoiTuong("KHACH_HANG");
            tb.setDoiTuongId(kh.getId());
            tb.setDuongDanMo("/chi-tiet-ban-ghi?id=" + kh.getId());
            tb.setDaDoc(false);
            tb.setCreatedAt(LocalDateTime.now());

            thongBaoDAO.taoThongBao(tb);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Không thể gửi thông báo cảnh báo rủi ro cho NVKD [" + kh.getNguoiSoHuuId() + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy thông tin trạng thái rủi ro rời bỏ phục vụ hiển thị trên trang 360 (AC3).
     */
    public ThongTinRuiRoDTO layThongTinRuiRo(Long khachHangId) {
        if (khachHangId == null || khachHangId <= 0) {
            return null;
        }

        KhachHang kh = khachHangDAO.timTheoId(khachHangId);
        if (kh == null) {
            return null;
        }

        int nguong = cauHinhHeThongDAO.layGiaTriInt(
                CauHinhHeThongDAO.NGUONG_YEU_CAU_HO_TRO_RUI_RO,
                CauHinhHeThongDAO.NGUONG_RUI_RO_MAC_DINH
        );
        int soChuaXuLy = yeuCauHoTroDAO.demYeuCauChuaXuLy(khachHangId);

        String thongDiep;
        if (kh.isCoRuiRo()) {
            thongDiep = "Khách hàng đang có " + soChuaXuLy + " yêu cầu hỗ trợ chưa xử lý (vượt ngưỡng " +
                    nguong + "). Nguy cơ rời bỏ cao! Nhân viên kinh doanh phụ trách cần phối hợp CSKH xử lý ngay.";
        } else {
            thongDiep = "Khách hàng đang ở trạng thái an toàn (" + soChuaXuLy + "/" + nguong + " yêu cầu chưa xử lý).";
        }

        return new ThongTinRuiRoDTO(
                kh.getId(),
                kh.getTenCongTy(),
                kh.getNguoiSoHuuId(),
                kh.getTenNguoiSoHuu(),
                kh.isCoRuiRo(),
                kh.getRuiRoCapNhatLuc(),
                soChuaXuLy,
                nguong,
                thongDiep
        );
    }

    /**
     * Lấy danh sách yêu cầu hỗ trợ của một khách hàng (hiển thị tab hỗ trợ trên trang 360).
     */
    public List<YeuCauHoTro> layDanhSachTheoKhachHang(Long khachHangId) {
        return yeuCauHoTroDAO.layDanhSachTheoKhachHang(khachHangId);
    }

    /**
     * Lấy danh sách yêu cầu hỗ trợ có bộ lọc phục vụ trang quản lý CSKH.
     */
    public List<YeuCauHoTro> layDanhSachYeuCau(Long khachHangId, String trangThai, String mucUuTien,
                                              Long nguoiXuLyId, String tuKhoa, int limit, int offset) {
        return yeuCauHoTroDAO.layDanhSach(khachHangId, trangThai, mucUuTien, nguoiXuLyId, tuKhoa, limit, offset);
    }

    /**
     * Tìm yêu cầu theo ID.
     */
    public YeuCauHoTro timTheoId(Long id) {
        return yeuCauHoTroDAO.timTheoId(id);
    }
}
