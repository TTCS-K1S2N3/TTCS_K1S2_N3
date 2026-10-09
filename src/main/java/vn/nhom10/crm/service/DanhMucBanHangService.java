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
        return dao.layDanhSach(loai, false);
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
        if (maChuan.length() > 50) {
            throw new IllegalArgumentException("Mã định danh không được vượt quá 50 ký tự.");
        }
        String tenChuan = dto.getTenMuc().trim();
        if (tenChuan.length() > 150) {
            throw new IllegalArgumentException("Tên mục danh mục không được vượt quá 150 ký tự.");
        }

        dto.setMaMuc(maChuan);
        dto.setTenMuc(tenChuan);
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

        String tenChuan = dto.getTenMuc().trim();
        if (tenChuan.length() > 150) {
            throw new IllegalArgumentException("Tên mục danh mục không được vượt quá 150 ký tự.");
        }

        dto.setTenMuc(tenChuan);
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
     * Áp dụng cơ chế fail-closed: nếu kiểm tra tham chiếu lỗi, từ chối xóa.
     */
    public boolean xoaMuc(LoaiDanhMuc loai, Long id) {
        if (loai == null || id == null || id <= 0) {
            throw new IllegalArgumentException("Thông tin mục danh mục cần xóa không hợp lệ.");
        }

        MucDanhMucDTO item = dao.layTheoId(loai, id);
        if (item == null) {
            throw new IllegalArgumentException("Mục danh mục không tồn tại.");
        }

        // Kiểm tra số lượng bản ghi đang tham chiếu (Fail-closed)
        int soThamChieu = dao.demSoLuongThamChieu(loai, id);
        if (soThamChieu < 0) {
            throw new IllegalStateException("Không thể kiểm tra dữ liệu tham chiếu do lỗi hệ thống. Để bảo vệ dữ liệu, không thực hiện xóa.");
        }
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

        // Hoán đổi nếu khác nhau, nếu bằng nhau hoặc <= 0 thì gán lại an toàn >= 1
        if (orderCurrent == orderNeighbor || orderCurrent <= 0 || orderNeighbor <= 0) {
            orderCurrent = index + 1;
            orderNeighbor = targetIndex + 1;
        }

        int newCurrentOrder = Math.max(1, orderNeighbor);
        int newNeighborOrder = Math.max(1, orderCurrent);
        if (newCurrentOrder == newNeighborOrder) {
            newCurrentOrder = targetIndex + 1;
            newNeighborOrder = index + 1;
        }

        dao.capNhatThuTu(loai, current.getId(), newCurrentOrder);
        dao.capNhatThuTu(loai, neighbor.getId(), newNeighborOrder);
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
}
