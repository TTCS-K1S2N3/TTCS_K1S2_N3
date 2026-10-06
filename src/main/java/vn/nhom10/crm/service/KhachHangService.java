package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.KhachHangDAO;
import vn.nhom10.crm.dto.BoLocKhachHangDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.TrangThaiKhachHangEnum;
import vn.nhom10.crm.model.VaiTroEnum;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service xử lý nghiệp vụ tìm kiếm, lọc và quản lý khách hàng (Story S3-07).
 * Tích hợp chặt chẽ với cơ chế kiểm soát phân quyền phạm vi dữ liệu (Data Scope).
 */
public class KhachHangService {

    private static final Logger LOGGER = Logger.getLogger(KhachHangService.class.getName());

    private final KhachHangDAO khachHangDAO;

    public KhachHangService() {
        this(new KhachHangDAO());
    }

    public KhachHangService(KhachHangDAO khachHangDAO) {
        this.khachHangDAO = khachHangDAO != null ? khachHangDAO : new KhachHangDAO();
    }

    /**
     * Tìm kiếm và lọc danh sách khách hàng kết hợp quyền hạn Data Scope của người dùng.
     * Đáp ứng AC1 & AC2:
     * - AC1: Lọc theo trạng thái, ngành nghề, quy mô, khu vực, người sở hữu
     * - AC2: Tìm theo tên, mã số thuế, số điện thoại người liên hệ
     */
    public List<KhachHang> timKiemVaLoc(NguoiDungDTO user, BoLocKhachHangDTO boLoc) {
        if (user == null) {
            return Collections.emptyList();
        }

        if (boLoc == null) {
            boLoc = new BoLocKhachHangDTO();
        }

        // Đảm bảo Data Scope không bị vượt quá quyền tối đa
        PhamViDuLieu phamViHieuLuc = xacDinhPhamViHieuLuc(user, boLoc.getPhamVi());
        boLoc.setPhamVi(phamViHieuLuc);

        return khachHangDAO.timKiemVaLoc(user, boLoc);
    }

    /**
     * Đếm tổng số lượng khách hàng theo bộ lọc.
     */
    public int demSoLuong(NguoiDungDTO user, BoLocKhachHangDTO boLoc) {
        if (user == null) {
            return 0;
        }
        if (boLoc == null) {
            boLoc = new BoLocKhachHangDTO();
        }
        PhamViDuLieu phamViHieuLuc = xacDinhPhamViHieuLuc(user, boLoc.getPhamVi());
        boLoc.setPhamVi(phamViHieuLuc);

        return khachHangDAO.demSoLuong(user, boLoc);
    }

    /**
     * Tìm khách hàng theo ID và kiểm tra quyền truy cập theo Data Scope.
     */
    public KhachHang timTheoId(long id, NguoiDungDTO user) {
        if (id <= 0 || user == null) {
            return null;
        }

        KhachHang kh = khachHangDAO.timTheoId(id);
        if (kh == null) {
            return null;
        }

        // Kiểm tra quyền truy cập Data Scope
        if (!kiemTraQuyenTruyCap(user, kh)) {
            return null;
        }

        return kh;
    }

    /**
     * Tạo khách hàng mới với validation nghiệp vụ và gán người sở hữu an toàn (S3-01 / S1-05).
     */
    public KhachHang taoKhachHang(NguoiDung user, KhachHang kh) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("Yêu cầu đăng nhập.");
        }
        if (kh == null) {
            throw new IllegalArgumentException("Dữ liệu khách hàng không hợp lệ.");
        }

        if (kh.getTenCongTy() == null || kh.getTenCongTy().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên công ty / khách hàng không được để trống.");
        }

        // Kiểm tra trùng mã số thuế nếu có nhập
        if (kh.getMaSoThue() != null && !kh.getMaSoThue().trim().isEmpty()) {
            if (khachHangDAO.kiemTraTonTaiMaSoThue(kh.getMaSoThue().trim(), null)) {
                throw new IllegalArgumentException("Mã số thuế '" + kh.getMaSoThue().trim() + "' đã tồn tại trong hệ thống.");
            }
        }

        // Kiểm tra trùng mã khách hàng nếu có nhập
        if (kh.getMaKhachHang() != null && !kh.getMaKhachHang().trim().isEmpty()) {
            if (khachHangDAO.kiemTraTonTaiMaKhachHang(kh.getMaKhachHang().trim(), null)) {
                throw new IllegalArgumentException("Mã khách hàng '" + kh.getMaKhachHang().trim() + "' đã tồn tại trong hệ thống.");
            }
        }

        // Chuẩn hóa trạng thái
        if (kh.getTrangThai() == null || kh.getTrangThai().trim().isEmpty()) {
            kh.setTrangThai(TrangThaiKhachHangEnum.TIEM_NANG.getMa());
        } else {
            kh.setTrangThai(TrangThaiKhachHangEnum.chuanHoaMa(kh.getTrangThai()));
        }

        // Server-side enforcement: Gán người sở hữu nếu chưa có hoặc Sales Rep
        boolean coQuyenChonOwner = user.coVaiTro(VaiTroEnum.ADMIN)
                || user.coVaiTro(VaiTroEnum.DIRECTOR)
                || user.coVaiTro(VaiTroEnum.TEAM_LEAD);

        if (!coQuyenChonOwner || kh.getNguoiSoHuuId() == null || kh.getNguoiSoHuuId() <= 0) {
            kh.setNguoiSoHuuId(user.getId());
            kh.setNhomKinhDoanhId(user.getNhomKinhDoanhId() != null ? Long.valueOf(user.getNhomKinhDoanhId()) : null);
        }

        long id = khachHangDAO.themKhachHang(kh);
        kh.setId(id);
        return kh;
    }

    /**
     * Cập nhật thông tin khách hàng.
     */
    public boolean capNhatKhachHang(NguoiDung user, KhachHang kh) throws SQLException {
        if (user == null || kh == null || kh.getId() == null) {
            return false;
        }

        NguoiDungDTO userDTO = NguoiDungDTO.tuNguoiDung(user);
        KhachHang goc = khachHangDAO.timTheoId(kh.getId());
        if (goc == null || !kiemTraQuyenTruyCap(userDTO, goc)) {
            return false;
        }

        if (kh.getTenCongTy() == null || kh.getTenCongTy().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên công ty / khách hàng không được để trống.");
        }

        if (kh.getMaSoThue() != null && !kh.getMaSoThue().trim().isEmpty()) {
            if (khachHangDAO.kiemTraTonTaiMaSoThue(kh.getMaSoThue().trim(), kh.getId())) {
                throw new IllegalArgumentException("Mã số thuế '" + kh.getMaSoThue().trim() + "' đã tồn tại.");
            }
        }

        return khachHangDAO.capNhatKhachHang(kh);
    }

    private PhamViDuLieu xacDinhPhamViHieuLuc(NguoiDungDTO user, PhamViDuLieu phamViYeuCau) {
        if (user == null) {
            return PhamViDuLieu.CA_NHAN;
        }
        PhamViDuLieu maxScope = user.getPhamViToiDa() != null ? user.getPhamViToiDa() : PhamViDuLieu.CA_NHAN;
        if (phamViYeuCau == null) {
            return user.getPhamViHienTai() != null ? user.getPhamViHienTai() : maxScope;
        }
        if (!user.coQuyenChonPhamVi(phamViYeuCau)) {
            return maxScope;
        }
        return phamViYeuCau;
    }

    private boolean kiemTraQuyenTruyCap(NguoiDungDTO user, KhachHang kh) {
        if (user == null || kh == null) {
            return false;
        }
        PhamViDuLieu maxScope = user.getPhamViToiDa() != null ? user.getPhamViToiDa() : PhamViDuLieu.CA_NHAN;
        if (maxScope == PhamViDuLieu.TOAN_BO) {
            return true;
        }
        if (maxScope == PhamViDuLieu.NHOM) {
            Long userNhomId = user.getNhomKinhDoanhId() != null ? Long.valueOf(user.getNhomKinhDoanhId()) : null;
            return userNhomId != null && userNhomId.equals(kh.getNhomKinhDoanhId());
        }
        // CA_NHAN
        return user.getId() != null && user.getId().equals(kh.getNguoiSoHuuId());
    }
}
