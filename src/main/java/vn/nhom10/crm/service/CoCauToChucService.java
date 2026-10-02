package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.KhuVucDiaLyDAO;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dao.NhomKinhDoanhDAO;
import vn.nhom10.crm.model.KhuVucDiaLy;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhomKinhDoanh;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service xử lý nghiệp vụ Khai báo cơ cấu tổ chức kinh doanh (Story S2-06).
 * Đáp ứng các Tiêu chí chấp nhận (AC):
 * - AC1: Nhóm kinh doanh có cấu trúc cây, mỗi nhóm có một trưởng nhóm.
 * - AC2: Mỗi nhân viên thuộc đúng một nhóm tại một thời điểm.
 * - AC3: Cây tổ chức này quyết định phạm vi dữ liệu mà Trưởng nhóm nhìn thấy (nhóm mình + các nhóm con cháu).
 * - AC4: Khai báo khu vực địa lý và gán khu vực cho nhóm.
 */
public class CoCauToChucService {

    private static final Logger LOGGER = Logger.getLogger(CoCauToChucService.class.getName());

    private final NhomKinhDoanhDAO nhomDAO;
    private final KhuVucDiaLyDAO khuVucDAO;
    private final NguoiDungDAO nguoiDungDAO;

    public CoCauToChucService() {
        this(new NhomKinhDoanhDAO(), new KhuVucDiaLyDAO(), new NguoiDungDAO());
    }

    public CoCauToChucService(NhomKinhDoanhDAO nhomDAO, KhuVucDiaLyDAO khuVucDAO, NguoiDungDAO nguoiDungDAO) {
        this.nhomDAO = nhomDAO != null ? nhomDAO : new NhomKinhDoanhDAO();
        this.khuVucDAO = khuVucDAO != null ? khuVucDAO : new KhuVucDiaLyDAO();
        this.nguoiDungDAO = nguoiDungDAO != null ? nguoiDungDAO : new NguoiDungDAO();
    }

    // =========================================================================
    // 1. QUẢN LÝ NHÓM KINH DOANH VÀ CÂY PHÂN CẤP (AC1)
    // =========================================================================

    public List<NhomKinhDoanh> layTatCaNhom() {
        return nhomDAO.layTatCa();
    }

    public List<NhomKinhDoanh> layTatCaNhomHoatDong() {
        return nhomDAO.layTatCaDangHoatDong();
    }

    public List<NhomKinhDoanh> layCayNhomKinhDoanh() {
        return nhomDAO.layCayNhomKinhDoanh();
    }

    public NhomKinhDoanh timNhomTheoId(long id) {
        return nhomDAO.timTheoId(id);
    }

    /**
     * Thêm mới nhóm kinh doanh với kiểm tra hợp lệ và tính toàn vẹn (AC1, AC4).
     */
    public NhomKinhDoanh themNhomKinhDoanh(String maNhom,
                                          String tenNhom,
                                          String moTa,
                                          Long nhomChaId,
                                          Long khuVucId,
                                          Long truongNhomId) {
        if (maNhom == null || maNhom.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhóm kinh doanh không được để trống.");
        }
        if (tenNhom == null || tenNhom.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên nhóm kinh doanh không được để trống.");
        }

        String ma = maNhom.trim().toUpperCase();
        if (nhomDAO.kiemTraTonTaiMa(ma, null)) {
            throw new IllegalArgumentException("Mã nhóm kinh doanh '" + ma + "' đã tồn tại trong hệ thống.");
        }

        // Kiểm tra nhóm cha nếu có
        if (nhomChaId != null && nhomChaId > 0) {
            NhomKinhDoanh nhomCha = nhomDAO.timTheoId(nhomChaId);
            if (nhomCha == null) {
                throw new IllegalArgumentException("Nhóm cha đã chọn không tồn tại.");
            }
        } else {
            nhomChaId = null;
        }

        // Kiểm tra khu vực địa lý nếu có (AC4)
        if (khuVucId != null && khuVucId > 0) {
            KhuVucDiaLy kv = khuVucDAO.timTheoId(khuVucId);
            if (kv == null) {
                throw new IllegalArgumentException("Khu vực địa lý đã chọn không tồn tại.");
            }
        } else {
            khuVucId = null;
        }

        // Kiểm tra trưởng nhóm nếu có (AC1)
        if (truongNhomId != null && truongNhomId > 0) {
            NguoiDung tn = nguoiDungDAO.timTheoId(truongNhomId);
            if (tn == null) {
                throw new IllegalArgumentException("Trưởng nhóm đã chọn không tồn tại trong hệ thống.");
            }
        } else {
            truongNhomId = null;
        }

        NhomKinhDoanh nhom = new NhomKinhDoanh();
        nhom.setMaNhom(ma);
        nhom.setTenNhom(tenNhom.trim());
        nhom.setMoTa(moTa != null ? moTa.trim() : "");
        nhom.setNhomChaId(nhomChaId);
        nhom.setKhuVucId(khuVucId);
        nhom.setTruongNhomId(truongNhomId);
        nhom.setHoatDong(true);

        try {
            long newId = nhomDAO.themNhom(nhom);
            nhom.setId(newId);

            // Nếu có gán trưởng nhóm, tự động cập nhật nhóm cho người này (AC2)
            if (truongNhomId != null && newId > 0) {
                nhomDAO.ganNhanVienVaoNhom(truongNhomId, newId);
            }

            return nhom;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm nhóm kinh doanh: " + e.getMessage(), e);
            throw new RuntimeException("Lỗi hệ thống khi thêm nhóm kinh doanh: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật thông tin nhóm kinh doanh, kiểm tra chống vòng lặp cây tổ chức (AC1).
     */
    public boolean capNhatNhomKinhDoanh(long nhomId,
                                        String tenNhom,
                                        String moTa,
                                        Long nhomChaId,
                                        Long khuVucId,
                                        Long truongNhomId,
                                        boolean hoatDong) {
        NhomKinhDoanh nhomCu = nhomDAO.timTheoId(nhomId);
        if (nhomCu == null) {
            throw new IllegalArgumentException("Nhóm kinh doanh không tồn tại (ID=" + nhomId + ").");
        }

        if (tenNhom == null || tenNhom.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên nhóm kinh doanh không được để trống.");
        }

        // Chống vòng lặp cây (Cycle Detection):
        if (nhomChaId != null && nhomChaId > 0) {
            if (nhomChaId == nhomId) {
                throw new IllegalArgumentException("Một nhóm không thể chọn chính nó làm nhóm cha.");
            }
            Set<Long> dsConChau = nhomDAO.layDsIdNhomConVaChau(nhomId);
            if (dsConChau.contains(nhomChaId)) {
                throw new IllegalArgumentException("Không thể chọn nhóm con/cháu làm nhóm cha (gây lặp vô hạn cây tổ chức).");
            }
            NhomKinhDoanh nhomCha = nhomDAO.timTheoId(nhomChaId);
            if (nhomCha == null) {
                throw new IllegalArgumentException("Nhóm cha đã chọn không tồn tại.");
            }
        } else {
            nhomChaId = null;
        }

        if (khuVucId != null && khuVucId > 0) {
            KhuVucDiaLy kv = khuVucDAO.timTheoId(khuVucId);
            if (kv == null) {
                throw new IllegalArgumentException("Khu vực địa lý đã chọn không tồn tại.");
            }
        } else {
            khuVucId = null;
        }

        if (truongNhomId != null && truongNhomId > 0) {
            NguoiDung tn = nguoiDungDAO.timTheoId(truongNhomId);
            if (tn == null) {
                throw new IllegalArgumentException("Trưởng nhóm đã chọn không tồn tại.");
            }
        } else {
            truongNhomId = null;
        }

        nhomCu.setTenNhom(tenNhom.trim());
        nhomCu.setMoTa(moTa != null ? moTa.trim() : "");
        nhomCu.setNhomChaId(nhomChaId);
        nhomCu.setKhuVucId(khuVucId);
        nhomCu.setTruongNhomId(truongNhomId);
        nhomCu.setHoatDong(hoatDong);

        try {
            boolean kq = nhomDAO.capNhatNhom(nhomCu);
            if (kq && truongNhomId != null) {
                // Tự động gán trưởng nhóm vào nhóm này nếu đang ở nhóm khác hoặc chưa có nhóm (AC2)
                nhomDAO.ganNhanVienVaoNhom(truongNhomId, nhomId);
            }
            return kq;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật nhóm kinh doanh: " + e.getMessage(), e);
            throw new RuntimeException("Lỗi hệ thống khi cập nhật nhóm kinh doanh: " + e.getMessage(), e);
        }
    }

    /**
     * Gán hoặc thay đổi Trưởng nhóm cho nhóm kinh doanh (AC1).
     * Đảm bảo mỗi nhóm chỉ có đúng 1 trưởng nhóm tại một thời điểm.
     */
    public boolean ganTruongNhom(long nhomId, Long truongNhomId) {
        NhomKinhDoanh nhom = nhomDAO.timTheoId(nhomId);
        if (nhom == null) {
            throw new IllegalArgumentException("Nhóm kinh doanh không tồn tại.");
        }

        if (truongNhomId != null && truongNhomId > 0) {
            NguoiDung nd = nguoiDungDAO.timTheoId(truongNhomId);
            if (nd == null) {
                throw new IllegalArgumentException("Người dùng được chỉ định làm trưởng nhóm không tồn tại.");
            }
            if (!nd.dangHoatDong()) {
                throw new IllegalArgumentException("Không thể gán tài khoản đã bị khóa/ngừng hoạt động làm trưởng nhóm.");
            }
        } else {
            truongNhomId = null;
        }

        try {
            boolean kq = nhomDAO.ganTruongNhom(nhomId, truongNhomId);
            if (kq && truongNhomId != null) {
                // Gán nhân viên vào nhóm này (AC2)
                nhomDAO.ganNhanVienVaoNhom(truongNhomId, nhomId);
            }
            return kq;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi gán trưởng nhóm: " + e.getMessage(), e);
            throw new RuntimeException("Lỗi hệ thống khi gán trưởng nhóm: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // 2. GÁN VÀ CHUYỂN NHÂN VIÊN GIỮA CÁC NHÓM (AC2)
    // =========================================================================

    /**
     * Chuyển nhân viên sang nhóm kinh doanh mới.
     * Quy tắc bắt buộc: Mỗi nhân viên thuộc đúng một nhóm tại một thời điểm (AC2).
     */
    public boolean chuyenNhomNhanVien(long nguoiDungId, Long nhomMoiId) {
        NguoiDung nd = nguoiDungDAO.timTheoId(nguoiDungId);
        if (nd == null) {
            throw new IllegalArgumentException("Nhân viên không tồn tại trong hệ thống.");
        }

        if (nhomMoiId != null && nhomMoiId > 0) {
            NhomKinhDoanh nhom = nhomDAO.timTheoId(nhomMoiId);
            if (nhom == null) {
                throw new IllegalArgumentException("Nhóm kinh doanh đích không tồn tại.");
            }
            if (!nhom.isHoatDong()) {
                throw new IllegalArgumentException("Không thể chuyển nhân viên vào nhóm đang ngừng hoạt động.");
            }
        } else {
            nhomMoiId = null;
        }

        try {
            return nhomDAO.ganNhanVienVaoNhom(nguoiDungId, nhomMoiId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi chuyển nhóm cho nhân viên: " + e.getMessage(), e);
            throw new RuntimeException("Lỗi hệ thống khi chuyển nhóm nhân viên: " + e.getMessage(), e);
        }
    }

    public List<NguoiDung> layDsNhanVienThuocNhom(long nhomId) {
        return nhomDAO.layDsThanhVien(nhomId);
    }

    public List<NguoiDung> layDsNhanVienChuaCoNhom() {
        return nhomDAO.layDsNhanVienChuaCoNhom();
    }

    // =========================================================================
    // 3. TÍNH TOÁN PHẠM VI DỮ LIỆU CỦA TRƯỞNG NHÓM THEO CÂY TỔ CHỨC (AC3)
    // =========================================================================

    /**
     * Xác định tập hợp tất cả ID nhóm kinh doanh mà một Trưởng nhóm có quyền nhìn thấy (AC3).
     * Trưởng nhóm được xem dữ liệu của nhóm mình và TẤT CẢ các nhóm con/cháu cấp dưới trong cây tổ chức.
     *
     * @param nguoiDungId ID của người dùng (Trưởng nhóm)
     * @return Set<Long> danh sách ID các nhóm trong phạm vi
     */
    public Set<Long> layDsIdNhomDuocXemBoiTruongNhom(long nguoiDungId) {
        Set<Long> tapHopNhom = new HashSet<>();

        // 1. Tìm các nhóm mà user này được gán làm trưởng nhóm (truong_nhom_id = nguoiDungId)
        List<NhomKinhDoanh> tatCaNhom = nhomDAO.layTatCa();
        boolean laTruongNhomCuaItNhatMotNhom = false;

        for (NhomKinhDoanh nkd : tatCaNhom) {
            if (nkd.getTruongNhomId() != null && nkd.getTruongNhomId() == nguoiDungId) {
                laTruongNhomCuaItNhatMotNhom = true;
                // Thu thập nhóm này và toàn bộ cây con cháu của nó
                tapHopNhom.addAll(nhomDAO.layDsIdNhomConVaChau(nkd.getIdLong()));
            }
        }

        // 2. Nếu user có thuộc một nhóm (nhom_kinh_doanh_id) nhưng chưa được gán chính thức trong truong_nhom_id
        if (!laTruongNhomCuaItNhatMotNhom) {
            NguoiDung nd = nguoiDungDAO.timTheoId(nguoiDungId);
            if (nd != null && nd.getNhomKinhDoanhId() != null && nd.getNhomKinhDoanhId() > 0) {
                tapHopNhom.addAll(nhomDAO.layDsIdNhomConVaChau(nd.getNhomKinhDoanhId().longValue()));
            }
        }

        return tapHopNhom;
    }

    /**
     * Kiểm tra một nhóm có nằm trong cây tổ chức quản lý của Trưởng nhóm hay không (AC3).
     */
    public boolean kiemTraThuocPhamViCayToChuc(long truongNhomId, Long banGhiNhomId) {
        if (banGhiNhomId == null) {
            return false;
        }
        Set<Long> nhomDuocXem = layDsIdNhomDuocXemBoiTruongNhom(truongNhomId);
        return nhomDuocXem.contains(banGhiNhomId);
    }

    // =========================================================================
    // 4. QUẢN LÝ KHU VỰC ĐỊA LÝ VÀ GÁN KHU VỰC CHO NHÓM (AC4)
    // =========================================================================

    public List<KhuVucDiaLy> layTatCaKhuVuc() {
        return khuVucDAO.layTatCa();
    }

    public List<KhuVucDiaLy> layTatCaKhuVucHoatDong() {
        return khuVucDAO.layTatCaDangHoatDong();
    }

    public List<KhuVucDiaLy> layCayKhuVuc() {
        return khuVucDAO.layCayKhuVuc();
    }

    public KhuVucDiaLy timKhuVucTheoId(long id) {
        return khuVucDAO.timTheoId(id);
    }

    /**
     * Khai báo khu vực địa lý mới (AC4).
     */
    public KhuVucDiaLy themKhuVucDiaLy(String maKhuVuc,
                                      String tenKhuVuc,
                                      String loaiKhuVuc,
                                      Long khuVucChaId,
                                      int thuTuHienThi) {
        if (maKhuVuc == null || maKhuVuc.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khu vực không được để trống.");
        }
        if (tenKhuVuc == null || tenKhuVuc.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khu vực không được để trống.");
        }

        String ma = maKhuVuc.trim().toUpperCase();
        if (khuVucDAO.kiemTraTonTaiMa(ma, null)) {
            throw new IllegalArgumentException("Mã khu vực '" + ma + "' đã tồn tại trong hệ thống.");
        }

        if (khuVucChaId != null && khuVucChaId > 0) {
            KhuVucDiaLy cha = khuVucDAO.timTheoId(khuVucChaId);
            if (cha == null) {
                throw new IllegalArgumentException("Khu vực cha đã chọn không tồn tại.");
            }
        } else {
            khuVucChaId = null;
        }

        KhuVucDiaLy kv = new KhuVucDiaLy();
        kv.setMaKhuVuc(ma);
        kv.setTenKhuVuc(tenKhuVuc.trim());
        kv.setLoaiKhuVuc(loaiKhuVuc != null ? loaiKhuVuc.trim().toUpperCase() : "TINH_THANH");
        kv.setKhuVucChaId(khuVucChaId);
        kv.setThuTuHienThi(thuTuHienThi);
        kv.setHoatDong(true);

        try {
            long newId = khuVucDAO.themKhuVuc(kv);
            kv.setId(newId);
            return kv;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm khu vực địa lý: " + e.getMessage(), e);
            throw new RuntimeException("Lỗi hệ thống khi thêm khu vực địa lý: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật khu vực địa lý (AC4).
     */
    public boolean capNhatKhuVucDiaLy(long id,
                                      String tenKhuVuc,
                                      String loaiKhuVuc,
                                      Long khuVucChaId,
                                      int thuTuHienThi,
                                      boolean hoatDong) {
        KhuVucDiaLy kv = khuVucDAO.timTheoId(id);
        if (kv == null) {
            throw new IllegalArgumentException("Khu vực địa lý không tồn tại (ID=" + id + ").");
        }

        if (tenKhuVuc == null || tenKhuVuc.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khu vực không được để trống.");
        }

        if (khuVucChaId != null && khuVucChaId > 0) {
            if (khuVucChaId == id) {
                throw new IllegalArgumentException("Khu vực không thể chọn chính nó làm khu vực cha.");
            }
            KhuVucDiaLy cha = khuVucDAO.timTheoId(khuVucChaId);
            if (cha == null) {
                throw new IllegalArgumentException("Khu vực cha đã chọn không tồn tại.");
            }
        } else {
            khuVucChaId = null;
        }

        kv.setTenKhuVuc(tenKhuVuc.trim());
        kv.setLoaiKhuVuc(loaiKhuVuc != null ? loaiKhuVuc.trim().toUpperCase() : "TINH_THANH");
        kv.setKhuVucChaId(khuVucChaId);
        kv.setThuTuHienThi(thuTuHienThi);
        kv.setHoatDong(hoatDong);

        try {
            return khuVucDAO.capNhatKhuVuc(kv);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật khu vực: " + e.getMessage(), e);
            throw new RuntimeException("Lỗi hệ thống khi cập nhật khu vực: " + e.getMessage(), e);
        }
    }

    /**
     * Gán khu vực địa lý cho nhóm kinh doanh (AC4).
     */
    public boolean ganKhuVucChoNhom(long nhomId, Long khuVucId) {
        NhomKinhDoanh nhom = nhomDAO.timTheoId(nhomId);
        if (nhom == null) {
            throw new IllegalArgumentException("Nhóm kinh doanh không tồn tại.");
        }

        if (khuVucId != null && khuVucId > 0) {
            KhuVucDiaLy kv = khuVucDAO.timTheoId(khuVucId);
            if (kv == null) {
                throw new IllegalArgumentException("Khu vực địa lý không tồn tại.");
            }
        } else {
            khuVucId = null;
        }

        try {
            return nhomDAO.ganKhuVuc(nhomId, khuVucId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi gán khu vực cho nhóm: " + e.getMessage(), e);
            throw new RuntimeException("Lỗi hệ thống khi gán khu vực: " + e.getMessage(), e);
        }
    }
}
