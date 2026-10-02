package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.DanhMucBanHangDAO;
import vn.nhom10.crm.dto.MucDanhMucDTO;
import vn.nhom10.crm.model.LoaiDanhMuc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service xử lý nghiệp vụ cho Story S2-07:
 * "Khai báo các danh mục dùng chung của bán hàng (Ngành nghề, Quy mô, Nguồn lead, Loại hoạt động)"
 * 
 * Nghiệp vụ chính:
 * - AC1: Quản trị 4 loại danh mục dùng chung (Ngành nghề khách hàng, Quy mô doanh nghiệp, Nguồn lead, Loại hoạt động).
 * - AC2: Kiểm tra toàn vẹn dữ liệu: Giá trị đang được tham chiếu thì KHÔNG xóa được (ném lỗi có thông điệp cụ thể).
 * - AC3: Sắp xếp được thứ tự hiển thị (tăng/giảm thứ tự hiển thị).
 */
public class DanhMucBanHangService {

    private static final Logger LOGGER = Logger.getLogger(DanhMucBanHangService.class.getName());

    private final DanhMucBanHangDAO dao;

    public DanhMucBanHangService() {
        this(new DanhMucBanHangDAO());
    }

    public DanhMucBanHangService(DanhMucBanHangDAO dao) {
        this.dao = dao != null ? dao : new DanhMucBanHangDAO();
    }

    /**
     * Lấy danh sách toàn bộ các mục theo loại danh mục.
     * Tự động khởi tạo dữ liệu chuẩn ban đầu nếu danh mục trong cơ sở dữ liệu chưa có dữ liệu.
     */
    public List<MucDanhMucDTO> layDanhSachTheoLoai(LoaiDanhMuc loai) {
        if (loai == null) {
            return new ArrayList<>();
        }
        List<MucDanhMucDTO> list = dao.layDanhSach(loai, false);
        if (list.isEmpty()) {
            khoiTaoDuLieuMauNeuTrong(loai);
            list = dao.layDanhSach(loai, false);
        }
        return list;
    }

    /**
     * Lấy chi tiết một mục danh mục theo ID.
     */
    public MucDanhMucDTO layTheoId(LoaiDanhMuc loai, long id) {
        if (loai == null || id <= 0) {
            return null;
        }
        return dao.layTheoId(loai, id);
    }

    /**
     * Tìm kiếm mục danh mục theo từ khóa (tìm trên mã hoặc tên hoặc mô tả).
     */
    public List<MucDanhMucDTO> timKiem(LoaiDanhMuc loai, String tuKhoa) {
        List<MucDanhMucDTO> tatCa = layDanhSachTheoLoai(loai);
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            return tatCa;
        }

        String keyword = tuKhoa.trim().toLowerCase();
        List<MucDanhMucDTO> ketQua = new ArrayList<>();
        for (MucDanhMucDTO item : tatCa) {
            boolean khopMa = item.getMaMuc() != null && item.getMaMuc().toLowerCase().contains(keyword);
            boolean khopTen = item.getTenMuc() != null && item.getTenMuc().toLowerCase().contains(keyword);
            boolean khopMoTa = item.getMoTa() != null && item.getMoTa().toLowerCase().contains(keyword);
            if (khopMa || khopTen || khopMoTa) {
                ketQua.add(item);
            }
        }
        return ketQua;
    }

    /**
     * Thêm mới một mục danh mục (AC1).
     * Kiểm tra tính hợp lệ và không cho phép trùng mã định danh.
     */
    public long themMuc(MucDanhMucDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Dữ liệu mục danh mục không được để trống.");
        }
        if (dto.getLoaiDanhMuc() == null) {
            throw new IllegalArgumentException("Loại danh mục không hợp lệ.");
        }
        if (dto.getMaMuc() == null || dto.getMaMuc().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã định danh không được để trống.");
        }
        if (dto.getTenMuc() == null || dto.getTenMuc().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên mục danh mục không được để trống.");
        }

        String maChuan = dto.getMaMuc().trim().toUpperCase();
        dto.setMaMuc(maChuan);
        dto.setTenMuc(dto.getTenMuc().trim());
        if (dto.getMoTa() != null) {
            dto.setMoTa(dto.getMoTa().trim());
        }

        LoaiDanhMuc loai = dto.getLoaiDanhMuc();
        if (dao.kiemTraTonTaiMa(loai, maChuan, null)) {
            throw new IllegalArgumentException("Mã định danh '" + maChuan + "' đã tồn tại trong danh mục " + loai.getTenHienThi() + ".");
        }

        if (dto.getThuTuHienThi() <= 0) {
            List<MucDanhMucDTO> hienTai = dao.layDanhSach(loai, false);
            dto.setThuTuHienThi(hienTai.size() + 1);
        }

        long newId = dao.them(loai, dto);
        if (newId <= 0) {
            throw new IllegalStateException("Không thể lưu mục danh mục vào cơ sở dữ liệu.");
        }
        dto.setId(newId);
        return newId;
    }

    /**
     * Cập nhật thông tin mục danh mục (tên hiển thị, mô tả, trạng thái kích hoạt).
     */
    public boolean capNhatMuc(MucDanhMucDTO dto) {
        if (dto == null || dto.getId() == null || dto.getId() <= 0) {
            throw new IllegalArgumentException("Mục danh mục cần cập nhật không hợp lệ.");
        }
        if (dto.getLoaiDanhMuc() == null) {
            throw new IllegalArgumentException("Loại danh mục không hợp lệ.");
        }
        if (dto.getTenMuc() == null || dto.getTenMuc().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên mục danh mục không được để trống.");
        }

        dto.setTenMuc(dto.getTenMuc().trim());
        if (dto.getMoTa() != null) {
            dto.setMoTa(dto.getMoTa().trim());
        }

        boolean ok = dao.capNhat(dto.getLoaiDanhMuc(), dto);
        if (!ok) {
            throw new IllegalStateException("Cập nhật mục danh mục không thành công.");
        }
        return true;
    }

    /**
     * AC2: Xóa mục danh mục.
     * Quy tắc bắt buộc: Giá trị đang được tham chiếu thì KHÔNG xóa được.
     */
    public boolean xoaMuc(LoaiDanhMuc loai, Long id) {
        if (loai == null || id == null || id <= 0) {
            throw new IllegalArgumentException("Thông tin mục danh mục cần xóa không hợp lệ.");
        }

        MucDanhMucDTO item = dao.layTheoId(loai, id);
        if (item == null) {
            throw new IllegalArgumentException("Mục danh mục không tồn tại.");
        }

        // Kiểm tra số lượng bản ghi đang tham chiếu
        int soThamChieu = dao.demSoLuongThamChieu(loai, id);
        if (soThamChieu > 0) {
            throw new IllegalStateException(
                "Không thể xóa mục '" + item.getTenMuc() + "' (" + item.getMaMuc() + ") vì hiện đang có " +
                soThamChieu + " bản ghi nghiệp vụ đang tham chiếu. Vui lòng chuyển trạng thái sang Tạm ngưng."
            );
        }

        boolean ok = dao.xoa(loai, id);
        if (!ok) {
            throw new IllegalStateException("Không thể xóa mục danh mục khỏi cơ sở dữ liệu.");
        }
        return true;
    }

    /**
     * AC3: Sắp xếp thứ tự hiển thị của mục danh mục.
     * Di chuyển lên trên (giảm thứ tự hiển thị) hoặc xuống dưới (tăng thứ tự hiển thị).
     */
    public boolean thayDoiThuTu(LoaiDanhMuc loai, Long id, boolean diChuyenLen) {
        if (loai == null || id == null || id <= 0) {
            return false;
        }

        List<MucDanhMucDTO> ds = dao.layDanhSach(loai, false);
        int index = -1;
        for (int i = 0; i < ds.size(); i++) {
            if (Objects.equals(ds.get(i).getId(), id)) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            return false;
        }

        int targetIndex = diChuyenLen ? (index - 1) : (index + 1);
        if (targetIndex < 0 || targetIndex >= ds.size()) {
            return false; // Đã ở đầu hoặc cuối danh sách
        }

        MucDanhMucDTO current = ds.get(index);
        MucDanhMucDTO neighbor = ds.get(targetIndex);

        int orderCurrent = current.getThuTuHienThi();
        int orderNeighbor = neighbor.getThuTuHienThi();

        // Hoán đổi nếu khác nhau, nếu bằng nhau thì gán lại theo thứ tự index
        if (orderCurrent == orderNeighbor) {
            orderCurrent = index + 1;
            orderNeighbor = targetIndex + 1;
        }

        dao.capNhatThuTu(loai, current.getId(), orderNeighbor);
        dao.capNhatThuTu(loai, neighbor.getId(), orderCurrent);
        return true;
    }

    /**
     * Bật hoặc tắt trạng thái áp dụng (kích hoạt) của mục danh mục.
     */
    public boolean chuyenTrangThaiKichHoat(LoaiDanhMuc loai, Long id) {
        if (loai == null || id == null || id <= 0) {
            return false;
        }
        MucDanhMucDTO item = dao.layTheoId(loai, id);
        if (item == null) {
            return false;
        }
        boolean trangThaiMoi = !item.isKichHoat();
        return dao.doiTrangThai(loai, id, trangThaiMoi);
    }

    /**
     * Thống kê số lượng mục: [tổng số mục, số mục kích hoạt, tổng số tham chiếu].
     */
    public long[] tinhThongKe(LoaiDanhMuc loai) {
        List<MucDanhMucDTO> list = layDanhSachTheoLoai(loai);
        long tong = list.size();
        long kichHoat = 0;
        long tongThamChieu = 0;

        for (MucDanhMucDTO item : list) {
            if (item.isKichHoat()) {
                kichHoat++;
            }
            tongThamChieu += item.getSoBanGhiDangSuDung();
        }
        return new long[]{tong, kichHoat, tongThamChieu};
    }

    /**
     * Khởi tạo bộ dữ liệu chuẩn của ngành bán hàng nếu bảng danh mục trong database chưa có dữ liệu.
     */
    private void khoiTaoDuLieuMauNeuTrong(LoaiDanhMuc loai) {
        try {
            switch (loai) {
                case NGANH_NGHE:
                    dao.them(loai, new MucDanhMucDTO(null, loai, "CNTT", "Công nghệ thông tin & Viễn thông", "Phần mềm, viễn thông, phần cứng và giải pháp số", 1, true, 0, LocalDate.now(), "Hệ thống"));
                    dao.them(loai, new MucDanhMucDTO(null, loai, "BAN_LE", "Bán lẻ & Thương mại điện tử", "Chuỗi cửa hàng bán lẻ, siêu thị, sàn thương mại điện tử", 2, true, 0, LocalDate.now(), "Hệ thống"));
                    dao.them(loai, new MucDanhMucDTO(null, loai, "SAN_XUAT", "Sản xuất & Chế biến", "Nhà máy, xưởng chế biến xuất nhập khẩu công nghiệp", 3, true, 0, LocalDate.now(), "Hệ thống"));
                    dao.them(loai, new MucDanhMucDTO(null, loai, "TAI_CHINH", "Tài chính - Ngân hàng - Bảo hiểm", "Ngân hàng thương mại, công ty chứng khoán, bảo hiểm nhân thọ", 4, true, 0, LocalDate.now(), "Hệ thống"));
                    dao.them(loai, new MucDanhMucDTO(null, loai, "BAT_DONG_SAN", "Bất động sản & Xây dựng", "Chủ đầu tư, sàn môi giới bất động sản và nhà thầu", 5, true, 0, LocalDate.now(), "Hệ thống"));
                    dao.them(loai, new MucDanhMucDTO(null, loai, "GIAO_DUC", "Giáo dục & Đào tạo", "Trường học, trung tâm ngoại ngữ, học viện kỹ năng", 6, true, 0, LocalDate.now(), "Hệ thống"));
                    dao.them(loai, new MucDanhMucDTO(null, loai, "Y_TE", "Y tế & Dược phẩm", "Bệnh viện, phòng khám đa khoa, doanh nghiệp phân phối dược", 7, true, 0, LocalDate.now(), "Hệ thống"));
                    dao.them(loai, new MucDanhMucDTO(null, loai, "DICH_VU", "Dịch vụ & Tư vấn", "Dịch vụ chuyên nghiệp, tư vấn pháp lý, kế toán thuế", 8, true, 0, LocalDate.now(), "Hệ thống"));
                    break;

                case QUY_MO:
                    dao.them(loai, new MucDanhMucDTO(null, loai, "DUOI_10", "Dưới 10 nhân sự (Siêu nhỏ)", "Doanh nghiệp khởi nghiệp, hộ kinh doanh cá thể", 1, true, 0, LocalDate.now(), "Hệ thống"));
                    dao.them(loai, new MucDanhMucDTO(null, loai, "TU_10_50", "Từ 10 - 50 nhân sự (Nhỏ)", "Doanh nghiệp quy mô nhỏ với cơ cấu phòng ban tinh gọn", 2, true, 0, LocalDate.now(), "Hệ thống"));
                    dao.them(loai, new MucDanhMucDTO(null, loai, "TU_51_200", "Từ 51 - 200 nhân sự (Vừa)", "Doanh nghiệp vừa đã có quy trình và quản lý trung cấp", 3, true, 0, LocalDate.now(), "Hệ thống"));
                    dao.them(loai, new MucDanhMucDTO(null, loai, "TU_201_500", "Từ 201 - 500 nhân sự (Lớn)", "Doanh nghiệp quy mô lớn đa chi nhánh", 4, true, 0, LocalDate.now(), "Hệ thống"));
                    dao.them(loai, new MucDanhMucDTO(null, loai, "TREN_500", "Trên 500 nhân sự (Tập đoàn)", "Tập đoàn kinh tế quy mô nhiều công ty thành viên", 5, true, 0, LocalDate.now(), "Hệ thống"));
                    break;

                case NGUON_LEAD:
                    dao.them(loai, new MucDanhMucDTO(null, loai, "WEBSITE", "Website & Đăng ký trực tuyến", "Khách hàng để lại thông tin form liên hệ trên website công ty", 1, true, 0, LocalDate.now(), "Hệ thống"));
                    dao.them(loai, new MucDanhMucDTO(null, loai, "FACEBOOK", "Facebook Ads & Fanpage", "Chiến dịch quảng cáo Facebook Lead Form và tin nhắn", 2, true, 0, LocalDate.now(), "Hệ thống"));
                    dao.them(loai, new MucDanhMucDTO(null, loai, "GOOGLE", "Google Search & Ads", "Tìm kiếm tự nhiên SEO và quảng cáo Google AdWords", 3, true, 0, LocalDate.now(), "Hệ thống"));
                    dao.them(loai, new MucDanhMucDTO(null, loai, "GIOI_THIEU", "Giới thiệu từ đối tác / khách hàng", "Kênh giới thiệu uy tín từ mạng lưới đối tác kinh doanh", 4, true, 0, LocalDate.now(), "Hệ thống"));
                    dao.them(loai, new MucDanhMucDTO(null, loai, "HOI_THAO", "Hội thảo & Sự kiện triển lãm", "Thu thập danh thiếp tại hội chợ, workshop kết nối kinh doanh", 5, true, 0, LocalDate.now(), "Hệ thống"));
                    dao.them(loai, new MucDanhMucDTO(null, loai, "TELESALES", "Cuộc gọi chủ động (Outbound)", "Nhân viên kinh doanh gọi điện tiếp cận danh sách tiềm năng", 6, true, 0, LocalDate.now(), "Hệ thống"));
                    dao.them(loai, new MucDanhMucDTO(null, loai, "EMAIL_MKT", "Email Marketing", "Chiến dịch gửi thư điện tử bản tin giới thiệu giải pháp", 7, true, 0, LocalDate.now(), "Hệ thống"));
                    break;

                case LOAI_HOAT_DONG:
                    dao.them(loai, new MucDanhMucDTO(null, loai, "CUOC_GOI", "Cuộc gọi điện thoại", "Liên hệ trao đổi thông tin trực tiếp qua điện thoại", 1, true, 0, LocalDate.now(), "Hệ thống"));
                    dao.them(loai, new MucDanhMucDTO(null, loai, "EMAIL", "Gửi email trao đổi", "Gửi tài liệu giới thiệu hoặc thư từ trao đổi nghiệp vụ", 2, true, 0, LocalDate.now(), "Hệ thống"));
                    dao.them(loai, new MucDanhMucDTO(null, loai, "GAP_MAT", "Gặp mặt trực tiếp", "Đến văn phòng khách hàng hoặc hẹn gặp trao đổi", 3, true, 0, LocalDate.now(), "Hệ thống"));
                    dao.them(loai, new MucDanhMucDTO(null, loai, "HOP_ONLINE", "Họp trực tuyến (Google Meet/Zoom)", "Trao đổi giải pháp từ xa qua hội nghị truyền hình", 4, true, 0, LocalDate.now(), "Hệ thống"));
                    dao.them(loai, new MucDanhMucDTO(null, loai, "GUI_BAO_GIA", "Gửi bảng báo giá", "Gửi bảng báo giá chi tiết sản phẩm / dịch vụ", 5, true, 0, LocalDate.now(), "Hệ thống"));
                    dao.them(loai, new MucDanhMucDTO(null, loai, "DEMO", "Demo thử nghiệm giải pháp", "Trực tiếp trình diễn tính năng phần mềm cho khách hàng", 6, true, 0, LocalDate.now(), "Hệ thống"));
                    break;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Không thể tự động nạp dữ liệu mẫu cho danh mục " + loai.getMa() + ": " + e.getMessage());
        }
    }
}
