package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.SanPhamDAO;
import vn.nhom10.crm.dto.KetQuaSanPhamDTO;
import vn.nhom10.crm.dto.PhanTrangDTO;
import vn.nhom10.crm.model.LoaiSanPhamEnum;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.SanPham;
import vn.nhom10.crm.model.TrangThaiSanPhamEnum;
import vn.nhom10.crm.model.VaiTroEnum;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Service xử lý business logic cho Quản lý danh mục sản phẩm dịch vụ và bảng giá niêm yết (S2-05).
 */
public class SanPhamService {

    private static final Logger LOGGER = Logger.getLogger(SanPhamService.class.getName());
    private static final Pattern MA_SAN_PHAM_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{2,50}$");

    private final SanPhamDAO sanPhamDAO;

    public SanPhamService() {
        this.sanPhamDAO = new SanPhamDAO();
    }

    public SanPhamService(SanPhamDAO sanPhamDAO) {
        this.sanPhamDAO = sanPhamDAO;
    }

    /**
     * AC: Giá vốn CHỈ Giám đốc kinh doanh (DIRECTOR) xem và sửa được.
     * Admin và các vai trò khác không được xem/sửa giá vốn theo quy định bảo mật.
     */
    public boolean coQuyenGiaVon(NguoiDung nguoiDung) {
        if (nguoiDung == null) {
            return false;
        }
        return nguoiDung.coVaiTro(VaiTroEnum.DIRECTOR);
    }

    /**
     * Kiểm tra quyền quản lý (tạo, sửa, xóa, ngừng kinh doanh sản phẩm).
     * Chỉ Giám đốc kinh doanh hoặc Quản trị hệ thống được phép thực hiện.
     */
    public boolean coQuyenQuanLy(NguoiDung nguoiDung) {
        if (nguoiDung == null) {
            return false;
        }
        return nguoiDung.coVaiTro(VaiTroEnum.DIRECTOR) || nguoiDung.coVaiTro(VaiTroEnum.ADMIN);
    }

    /**
     * Lấy danh sách sản phẩm có tìm kiếm, lọc và phân trang.
     * Bảo mật phía server: Nếu người dùng không có quyền giá vốn, giá vốn trả về sẽ là null.
     */
    public PhanTrangDTO<SanPham> layDanhSachSanPham(String tuKhoa, LoaiSanPhamEnum loai,
                                                    TrangThaiSanPhamEnum trangThai,
                                                    int trangHienTai, int soBanGhiMoiTrang,
                                                    NguoiDung nguoiDung) {
        int page = Math.max(trangHienTai, 1);
        int pageSize = soBanGhiMoiTrang > 0 ? soBanGhiMoiTrang : 20;
        int offset = (page - 1) * pageSize;

        boolean quyenGiaVon = coQuyenGiaVon(nguoiDung);
        int tongSoBanGhi = sanPhamDAO.demSoLuong(tuKhoa, loai, trangThai);
        List<SanPham> danhSach = sanPhamDAO.timKiemVaPhanTrang(tuKhoa, loai, trangThai, pageSize, offset, quyenGiaVon);

        return new PhanTrangDTO<>(danhSach, page, pageSize, tongSoBanGhi);
    }

    /**
     * Xem thông tin chi tiết một sản phẩm theo ID.
     */
    public SanPham layChiTietSanPham(int id, NguoiDung nguoiDung) {
        boolean quyenGiaVon = coQuyenGiaVon(nguoiDung);
        return sanPhamDAO.timTheoId(id, quyenGiaVon);
    }

    /**
     * AC: Khai báo mã, tên, loại (sản phẩm một lần hoặc dịch vụ thuê bao), đơn vị tính, giá niêm yết, giá sàn.
     * AC: Giá vốn chỉ Giám đốc kinh doanh xem và sửa được.
     */
    public KetQuaSanPhamDTO taoSanPham(SanPham sp, NguoiDung nguoiDung) {
        KetQuaSanPhamDTO ketQua = new KetQuaSanPhamDTO();

        if (!coQuyenQuanLy(nguoiDung)) {
            return KetQuaSanPhamDTO.thatBai("Bạn không có quyền thực hiện thao tác này. Chỉ Giám đốc kinh doanh mới có quyền quản lý bảng giá.");
        }

        // Validate dữ liệu đầu vào
        validateDuLieuSanPham(sp, null, ketQua);
        if (!ketQua.getDanhSachLoi().isEmpty()) {
            ketQua.setThanhCong(false);
            ketQua.setThongBao("Dữ liệu nhập vào chưa hợp lệ. Vui lòng kiểm tra lại các trường báo lỗi.");
            return ketQua;
        }

        // Kiểm tra quyền giá vốn
        boolean quyenGiaVon = coQuyenGiaVon(nguoiDung);
        if (!quyenGiaVon) {
            sp.setGiaVon(null);
        }

        try {
            int newId = sanPhamDAO.themSanPham(sp, quyenGiaVon);
            if (newId > 0) {
                sp.setId(newId);
                return KetQuaSanPhamDTO.thanhCong(sp, "Thêm mới sản phẩm/dịch vụ '" + sp.getTenSanPham() + "' thành công.");
            } else {
                return KetQuaSanPhamDTO.thatBai("Không thể lưu sản phẩm vào cơ sở dữ liệu.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm sản phẩm: " + e.getMessage(), e);
            return KetQuaSanPhamDTO.thatBai("Lỗi hệ thống khi thêm sản phẩm: " + e.getMessage());
        }
    }

    /**
     * Cập nhật thông tin sản phẩm và bảng giá niêm yết.
     * AC: Giá vốn chỉ Giám đốc kinh doanh xem và sửa được.
     */
    public KetQuaSanPhamDTO capNhatSanPham(SanPham sp, NguoiDung nguoiDung) {
        KetQuaSanPhamDTO ketQua = new KetQuaSanPhamDTO();

        if (!coQuyenQuanLy(nguoiDung)) {
            return KetQuaSanPhamDTO.thatBai("Bạn không có quyền cập nhật bảng giá niêm yết.");
        }

        SanPham spHienTai = sanPhamDAO.timTheoId(sp.getId(), true);
        if (spHienTai == null) {
            return KetQuaSanPhamDTO.thatBai("Không tìm thấy sản phẩm cần cập nhật (ID=" + sp.getId() + ").");
        }

        validateDuLieuSanPham(sp, sp.getId(), ketQua);
        if (!ketQua.getDanhSachLoi().isEmpty()) {
            ketQua.setThanhCong(false);
            ketQua.setThongBao("Dữ liệu cập nhật chưa hợp lệ. Vui lòng kiểm tra lại.");
            return ketQua;
        }

        boolean quyenGiaVon = coQuyenGiaVon(nguoiDung);
        if (!quyenGiaVon) {
            // Không có quyền sửa giá vốn -> giữ nguyên giá vốn ban đầu trong cơ sở dữ liệu
            sp.setGiaVon(spHienTai.getGiaVon());
        }

        try {
            boolean capNhatOk = sanPhamDAO.capNhatSanPham(sp, quyenGiaVon);
            if (capNhatOk) {
                return KetQuaSanPhamDTO.thanhCong(sp, "Cập nhật sản phẩm '" + sp.getTenSanPham() + "' thành công.");
            } else {
                return KetQuaSanPhamDTO.thatBai("Cập nhật thông tin sản phẩm thất bại.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật sản phẩm ID=" + sp.getId() + ": " + e.getMessage(), e);
            return KetQuaSanPhamDTO.thatBai("Lỗi hệ thống khi cập nhật sản phẩm: " + e.getMessage());
        }
    }

    /**
     * AC: Sản phẩm đã xuất hiện trong báo giá thì không xoá được, chỉ ngừng kinh doanh.
     */
    public KetQuaSanPhamDTO xoaSanPham(int id, NguoiDung nguoiDung) {
        if (!coQuyenQuanLy(nguoiDung)) {
            return KetQuaSanPhamDTO.thatBai("Bạn không có quyền xoá sản phẩm.");
        }

        SanPham sp = sanPhamDAO.timTheoId(id, false);
        if (sp == null) {
            return KetQuaSanPhamDTO.thatBai("Không tìm thấy sản phẩm có ID=" + id);
        }

        // Kiểm tra ràng buộc: Sản phẩm đã có trong bất kỳ báo giá nào hay chưa (Fail-closed)
        try {
            boolean daCoTrongBaoGia = sanPhamDAO.kiemTraXuatHienTrongBaoGia(id);
            if (daCoTrongBaoGia) {
                return KetQuaSanPhamDTO.loiDaCoTrongBaoGia(sp.getTenSanPham());
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Không thể kiểm tra dữ liệu tham chiếu báo giá cho sản phẩm ID=" + id + ": " + e.getMessage(), e);
            return KetQuaSanPhamDTO.thatBai("Không thể kiểm tra dữ liệu tham chiếu do lỗi hệ thống. Để bảo vệ dữ liệu, không thực hiện xóa.");
        }

        try {
            boolean xoaOk = sanPhamDAO.xoaSanPham(id);
            if (xoaOk) {
                return KetQuaSanPhamDTO.thanhCong(sp, "Đã xoá hoàn toàn sản phẩm '" + sp.getTenSanPham() + "' khỏi hệ thống.");
            } else {
                return KetQuaSanPhamDTO.thatBai("Xoá sản phẩm không thành công.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi xoá sản phẩm ID=" + id + ": " + e.getMessage(), e);
            return KetQuaSanPhamDTO.thatBai("Lỗi cơ sở dữ liệu khi xoá sản phẩm: " + e.getMessage());
        }
    }

    /**
     * Chuyển trạng thái sản phẩm (vd: Ngừng kinh doanh hoặc Mở bán lại).
     */
    public KetQuaSanPhamDTO chuyenTrangThai(int id, TrangThaiSanPhamEnum trangThaiMoi, NguoiDung nguoiDung) {
        if (!coQuyenQuanLy(nguoiDung)) {
            return KetQuaSanPhamDTO.thatBai("Bạn không có quyền thay đổi trạng thái sản phẩm.");
        }

        SanPham sp = sanPhamDAO.timTheoId(id, false);
        if (sp == null) {
            return KetQuaSanPhamDTO.thatBai("Không tìm thấy sản phẩm ID=" + id);
        }

        try {
            boolean ok = sanPhamDAO.capNhatTrangThai(id, trangThaiMoi);
            if (ok) {
                sp.setTrangThai(trangThaiMoi);
                String msg = (trangThaiMoi == TrangThaiSanPhamEnum.NGUNG_KINH_DOANH)
                        ? "Đã chuyển sản phẩm '" + sp.getTenSanPham() + "' sang trạng thái Ngừng kinh doanh."
                        : "Đã kích hoạt kinh doanh lại cho sản phẩm '" + sp.getTenSanPham() + "'.";
                return KetQuaSanPhamDTO.thanhCong(sp, msg);
            } else {
                return KetQuaSanPhamDTO.thatBai("Cập nhật trạng thái sản phẩm thất bại.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi chuyển trạng thái sản phẩm ID=" + id + ": " + e.getMessage(), e);
            return KetQuaSanPhamDTO.thatBai("Lỗi hệ thống: " + e.getMessage());
        }
    }

    /**
     * AC: Giá sàn là ngưỡng để xác định báo giá có cần duyệt chiết khấu hay không.
     *
     * @param sanPhamId ID sản phẩm
     * @param donGiaBaoGia Đơn giá đề xuất trong báo giá
     * @return true nếu đơn vị đề xuất < giá sàn (cần Giám đốc phê duyệt chiết khấu)
     */
    public boolean kiemTraCanDuyetChietKhau(int sanPhamId, BigDecimal donGiaBaoGia) {
        if (donGiaBaoGia == null) {
            return false;
        }
        SanPham sp = sanPhamDAO.timTheoId(sanPhamId, false);
        if (sp == null || sp.getGiaSan() == null) {
            return false;
        }
        return sp.kiemTraDuoiGiaSan(donGiaBaoGia);
    }

    /**
     * Hàm kiểm tra tính hợp lệ của dữ liệu sản phẩm trước khi lưu.
     */
    private void validateDuLieuSanPham(SanPham sp, Integer excludeId, KetQuaSanPhamDTO ketQua) {
        if (sp == null) {
            ketQua.themLoi("_global", "Dữ liệu sản phẩm không được để trống.");
            return;
        }

        // 1. Mã sản phẩm
        if (sp.getMaSanPham() == null || sp.getMaSanPham().trim().isEmpty()) {
            ketQua.themLoi("maSanPham", "Mã sản phẩm không được để trống.");
        } else {
            String ma = sp.getMaSanPham().trim();
            if (!MA_SAN_PHAM_PATTERN.matcher(ma).matches()) {
                ketQua.themLoi("maSanPham", "Mã sản phẩm từ 2-50 ký tự, chỉ gồm chữ cái, số, gạch ngang (-) hoặc gạch dưới (_).");
            } else if (sanPhamDAO.kiemTraMaTonTai(ma, excludeId)) {
                ketQua.themLoi("maSanPham", "Mã sản phẩm '" + ma + "' đã tồn tại trên hệ thống.");
            }
        }

        // 2. Tên sản phẩm
        if (sp.getTenSanPham() == null || sp.getTenSanPham().trim().isEmpty()) {
            ketQua.themLoi("tenSanPham", "Tên sản phẩm/dịch vụ không được để trống.");
        } else if (sp.getTenSanPham().trim().length() > 255) {
            ketQua.themLoi("tenSanPham", "Tên sản phẩm tối đa 255 ký tự.");
        }

        // 3. Loại sản phẩm
        if (sp.getLoai() == null) {
            ketQua.themLoi("loai", "Vui lòng chọn loại sản phẩm (Sản phẩm một lần hoặc Dịch vụ thuê bao).");
        }

        // 4. Đơn vị tính
        if (sp.getDonViTinh() == null || sp.getDonViTinh().trim().isEmpty()) {
            ketQua.themLoi("donViTinh", "Đơn vị tính không được để trống.");
        } else if (sp.getDonViTinh().trim().length() > 50) {
            ketQua.themLoi("donViTinh", "Đơn vị tính tối đa 50 ký tự.");
        }

        // 5. Giá niêm yết
        if (sp.getGiaNiemYet() == null) {
            ketQua.themLoi("giaNiemYet", "Giá niêm yết không được để trống.");
        } else if (sp.getGiaNiemYet().compareTo(BigDecimal.ZERO) < 0) {
            ketQua.themLoi("giaNiemYet", "Giá niêm yết phải lớn hơn hoặc bằng 0.");
        }

        // 6. Giá sàn
        if (sp.getGiaSan() == null) {
            ketQua.themLoi("giaSan", "Giá sàn không được để trống.");
        } else if (sp.getGiaSan().compareTo(BigDecimal.ZERO) < 0) {
            ketQua.themLoi("giaSan", "Giá sàn phải lớn hơn hoặc bằng 0.");
        } else if (sp.getGiaNiemYet() != null && sp.getGiaSan().compareTo(sp.getGiaNiemYet()) > 0) {
            ketQua.themLoi("giaSan", "Giá sàn (" + sp.getGiaSan() + ") không được vượt quá giá niêm yết (" + sp.getGiaNiemYet() + ").");
        }

        // 7. Giá vốn (nếu có nhập)
        if (sp.getGiaVon() != null && sp.getGiaVon().compareTo(BigDecimal.ZERO) < 0) {
            ketQua.themLoi("giaVon", "Giá vốn nếu có phải lớn hơn hoặc bằng 0.");
        }
    }
}
