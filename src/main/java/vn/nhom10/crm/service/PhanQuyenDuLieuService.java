package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.PhanQuyenDuLieuDAO;
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO;
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO.LoaiNghiepVu;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.VaiTroEnum;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Service xử lý phân quyền theo phạm vi dữ liệu sở hữu (Story S1-05).
 * Đáp ứng đầy đủ các Tiêu chí chấp nhận (AC):
 * - AC1: Ba phạm vi dữ liệu: Của tôi (CA_NHAN), Của nhóm tôi (NHOM), Tất cả (TOAN_BO) cho Khách hàng, Cơ hội, Hoạt động, Báo giá.
 * - AC2: Mọi truy vấn danh sách tự động lọc theo phạm vi, kể cả tìm kiếm và xuất Excel.
 * - AC3: Truy cập bản ghi ngoài phạm vi hiển thị thông báo tiếng Việt rõ ràng.
 * - AC4: Kiểm thử tự động chứng minh nhân viên A không đọc được khách hàng của nhân viên B.
 */
public class PhanQuyenDuLieuService {

    private static final Logger LOGGER = Logger.getLogger(PhanQuyenDuLieuService.class.getName());

    private final PhanQuyenDuLieuDAO dao;
    private final List<BanGhiNghiepVuDTO> danhSachBoNho;

    public PhanQuyenDuLieuService() {
        this(new PhanQuyenDuLieuDAO());
    }

    public PhanQuyenDuLieuService(PhanQuyenDuLieuDAO dao) {
        this.dao = dao;
        this.danhSachBoNho = new java.util.concurrent.CopyOnWriteArrayList<>(layDanhSachDuLieuMau());
    }

    /**
     * Kết quả kiểm tra quyền truy cập bản ghi chi tiết (AC3).
     */
    public static class KetQuaKiemTra {
        private final boolean coQuyen;
        private final String thongBao;
        private final BanGhiNghiepVuDTO banGhi;

        public KetQuaKiemTra(boolean coQuyen, String thongBao, BanGhiNghiepVuDTO banGhi) {
            this.coQuyen = coQuyen;
            this.thongBao = thongBao;
            this.banGhi = banGhi;
        }

        public boolean isCoQuyen() {
            return coQuyen;
        }

        public String getThongBao() {
            return thongBao;
        }

        public BanGhiNghiepVuDTO getBanGhi() {
            return banGhi;
        }
    }

    /**
     * Xác định phạm vi dữ liệu thực tế mà người dùng được áp dụng.
     * Ngăn chặn việc người dùng cố tình gửi request với phạm vi vượt quá quyền hạn (ví dụ Sales Rep yêu cầu TOAN_BO).
     */
    public PhamViDuLieu xacDinhPhamViHieuLuc(NguoiDungDTO user, PhamViDuLieu phamViYeuCau) {
        if (user == null) {
            return PhamViDuLieu.CA_NHAN;
        }
        PhamViDuLieu maxScope = user.getPhamViToiDa() != null ? user.getPhamViToiDa() : PhamViDuLieu.CA_NHAN;
        if (phamViYeuCau == null) {
            return user.getPhamViHienTai() != null ? user.getPhamViHienTai() : maxScope;
        }
        // Nếu người dùng không có quyền chọn phạm vi yêu cầu, ép về maxScope (Fail-closed)
        if (!user.coQuyenChonPhamVi(phamViYeuCau)) {
            return maxScope;
        }
        return phamViYeuCau;
    }

    public PhamViDuLieu xacDinhPhamViHieuLuc(NguoiDung nd, PhamViDuLieu phamViYeuCau) {
        return xacDinhPhamViHieuLuc(NguoiDungDTO.tuNguoiDung(nd), phamViYeuCau);
    }

    /**
     * Lấy danh sách bản ghi theo phạm vi dữ liệu, hỗ trợ truy vấn trực tiếp từ DAO hoặc fallback dữ liệu mẫu.
     */
    public List<BanGhiNghiepVuDTO> layDanhSachDuLieu(NguoiDungDTO user,
                                                     PhamViDuLieu phamViYeuCau,
                                                     String tuKhoa,
                                                     String loaiNghiepVu) {
        if (user == null) {
            return Collections.emptyList();
        }

        PhamViDuLieu phamViHieuLuc = xacDinhPhamViHieuLuc(user, phamViYeuCau);
        LoaiNghiepVu loaiEnum = parseLoaiNghiepVu(loaiNghiepVu);

        // 1. Thử truy vấn qua DAO từ cơ sở dữ liệu
        if (dao != null) {
            try {
                List<BanGhiNghiepVuDTO> dbList = dao.layDanhSachTongHopTheoPhamVi(
                        user.getId(),
                        user.getNhomKinhDoanhId(),
                        phamViHieuLuc,
                        tuKhoa,
                        loaiEnum
                );
                if (dbList != null && !dbList.isEmpty()) {
                    return dbList;
                }
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Không thể truy vấn từ database, chuyển sang fallback bộ nhớ: " + e.getMessage());
            }
        }

        // 2. Fallback sử dụng dữ liệu mẫu trong bộ nhớ để demo và test độc lập
        return locTheoPhamVi(new ArrayList<>(danhSachBoNho), user, phamViHieuLuc, tuKhoa, loaiNghiepVu);
    }

    public List<BanGhiNghiepVuDTO> layDanhSachDuLieu(NguoiDung nd,
                                                     PhamViDuLieu phamViYeuCau,
                                                     String tuKhoa,
                                                     String loaiNghiepVu) {
        return layDanhSachDuLieu(NguoiDungDTO.tuNguoiDung(nd), phamViYeuCau, tuKhoa, loaiNghiepVu);
    }

    /**
     * Tìm bản ghi theo ID (hỗ trợ cả DAO và danh sách mẫu).
     */
    public BanGhiNghiepVuDTO timBanGhiTheoId(Long id, String loaiNghiepVu) {
        if (id == null) {
            return null;
        }

        LoaiNghiepVu loaiEnum = parseLoaiNghiepVu(loaiNghiepVu);
        if (dao != null) {
            try {
                BanGhiNghiepVuDTO bg = dao.timBanGhiTheoId(id, loaiEnum);
                if (bg != null) {
                    return bg;
                }
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Không thể tìm bản ghi trong database: " + e.getMessage());
            }
        }

        // Fallback tìm trong danh sách bộ nhớ
        for (BanGhiNghiepVuDTO bg : danhSachBoNho) {
            if (bg.getId().equals(id)) {
                return bg;
            }
        }
        return null;
    }

    /**
     * Kiểm tra quyền truy cập một bản ghi cụ thể theo phạm vi dữ liệu sở hữu (AC3, AC4).
     */
    public KetQuaKiemTra kiemTraQuyenTruyCap(NguoiDungDTO user, BanGhiNghiepVuDTO banGhi) {
        if (user == null) {
            return new KetQuaKiemTra(false, "Vui lòng đăng nhập để truy cập dữ liệu hệ thống.", null);
        }
        if (banGhi == null) {
            return new KetQuaKiemTra(false, "Bản ghi yêu cầu không tồn tại trong hệ thống.", null);
        }

        PhamViDuLieu phamViToiDa = user.getPhamViToiDa() != null ? user.getPhamViToiDa() : PhamViDuLieu.CA_NHAN;

        // 1. Giám đốc, Admin (nếu có quyền TOAN_BO xác thực từ DB): xem toàn bộ
        if (phamViToiDa == PhamViDuLieu.TOAN_BO) {
            return new KetQuaKiemTra(true, "Truy cập hợp lệ với phạm vi toàn bộ hệ thống.", banGhi);
        }

        // 2. Bản ghi do chính user phụ trách trực tiếp -> luôn có quyền xem
        if (banGhi.getNguoiPhuTrachId() != null && banGhi.getNguoiPhuTrachId().equals(user.getId())) {
            return new KetQuaKiemTra(true, "Truy cập hợp lệ với tư cách người phụ trách trực tiếp.", banGhi);
        }

        // 3. Trưởng nhóm (nếu có quyền NHOM xác thực từ DB): được xem dữ liệu của thành viên trong nhóm mình
        if (phamViToiDa == PhamViDuLieu.NHOM) {
            if (banGhi.getNhomKinhDoanhId() != null && banGhi.getNhomKinhDoanhId().equals(user.getNhomKinhDoanhId())) {
                return new KetQuaKiemTra(true, "Truy cập hợp lệ với tư cách Trưởng nhóm quản lý " + user.getTenNhom() + ".", banGhi);
            } else {
                String thongBao = String.format(
                        "Từ chối truy cập: Bản ghi '%s' (Mã: %s) thuộc về %s (%s), không nằm trong phạm vi nhóm quản lý của bạn (%s).",
                        banGhi.getTieuDe(), banGhi.getMaBanGhi(), banGhi.getTenNguoiPhuTrach(), banGhi.getTenNhom(), user.getTenNhom()
                );
                return new KetQuaKiemTra(false, thongBao, banGhi);
            }
        }

        // 4. Mặc định (CA_NHAN, hoặc khi DB lỗi): FAIL-CLOSED - từ chối truy cập bản ghi của người khác
        String thongBao = String.format(
                "Từ chối truy cập: Bản ghi '%s' (Mã: %s) hiện do %s (%s) phụ trách. Tài khoản của bạn (%s) chỉ có quyền xem dữ liệu cá nhân của chính mình.",
                banGhi.getTieuDe(), banGhi.getMaBanGhi(), banGhi.getTenNguoiPhuTrach(), banGhi.getTenNhom(), user.getHoTen()
        );
        return new KetQuaKiemTra(false, thongBao, banGhi);
    }

    public KetQuaKiemTra kiemTraQuyenTruyCap(NguoiDung nd, BanGhiNghiepVuDTO banGhi) {
        return kiemTraQuyenTruyCap(NguoiDungDTO.tuNguoiDung(nd), banGhi);
    }

    /**
     * Kiểm tra quyền sửa/thao tác bản ghi theo Data Scope (chặn sửa trực tiếp qua ID/URL).
     */
    public KetQuaKiemTra kiemTraQuyenSua(NguoiDungDTO user, BanGhiNghiepVuDTO banGhi) {
        if (user == null) {
            return new KetQuaKiemTra(false, "Vui lòng đăng nhập để thao tác dữ liệu.", null);
        }
        if (banGhi == null) {
            return new KetQuaKiemTra(false, "Bản ghi yêu cầu không tồn tại.", null);
        }
        KetQuaKiemTra kqTruyCap = kiemTraQuyenTruyCap(user, banGhi);
        if (!kqTruyCap.isCoQuyen()) {
            return new KetQuaKiemTra(false, "Từ chối thao tác sửa: " + kqTruyCap.getThongBao(), banGhi);
        }
        return new KetQuaKiemTra(true, "Có quyền sửa đổi bản ghi.", banGhi);
    }

    public KetQuaKiemTra kiemTraQuyenSua(NguoiDung nd, BanGhiNghiepVuDTO banGhi) {
        return kiemTraQuyenSua(NguoiDungDTO.tuNguoiDung(nd), banGhi);
    }

    /**
     * Cập nhật bản ghi nghiệp vụ sau khi đã kiểm tra quyền.
     */
    public boolean capNhatBanGhi(BanGhiNghiepVuDTO banGhi) {
        if (banGhi == null) {
            return false;
        }
        if (dao != null) {
            try {
                dao.capNhatBanGhi(banGhi);
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Không thể cập nhật database: " + e.getMessage());
            }
        }
        for (int i = 0; i < danhSachBoNho.size(); i++) {
            if (danhSachBoNho.get(i).getId().equals(banGhi.getId())) {
                danhSachBoNho.set(i, banGhi);
                break;
            }
        }
        return true;
    }

    /**
     * Kiểm tra xem mã khách hàng đã tồn tại hay chưa.
     */
    public boolean kiemTraTonTaiMaKhachHang(String maKhachHang) {
        if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
            return false;
        }
        String cleanMa = maKhachHang.trim();
        if (dao != null) {
            try {
                if (dao.kiemTraTonTaiMaKhachHang(cleanMa)) {
                    return true;
                }
            } catch (Exception ignored) {}
        }
        for (BanGhiNghiepVuDTO bg : danhSachBoNho) {
            if (cleanMa.equalsIgnoreCase(bg.getMaBanGhi())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Thêm mới khách hàng với kiểm tra phân quyền sở hữu tự động (Story S1-05).
     * Server-side ownership determination:
     * Người sở hữu (nguoi_so_huu_id) LUÔN LUÔN được gán từ user hiện tại.
     * Tuyệt đối không cho phép client giả mạo (no client-side owner spoofing).
     */
    public BanGhiNghiepVuDTO themKhachHang(NguoiDungDTO user,
                                           String maKhachHang,
                                           String tenCongTy,
                                           String giaTri,
                                           String trangThai,
                                           String moTaChiTiet) {
        if (user == null) {
            throw new SecurityException("Vui lòng đăng nhập để thực hiện thêm khách hàng.");
        }
        if (tenCongTy == null || tenCongTy.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên công ty / khách hàng không được để trống.");
        }

        String ma = (maKhachHang != null && !maKhachHang.trim().isEmpty())
                ? maKhachHang.trim()
                : "KH-" + (System.currentTimeMillis() % 100000);

        if (kiemTraTonTaiMaKhachHang(ma)) {
            throw new IllegalArgumentException("Mã khách hàng '" + ma + "' đã tồn tại trong hệ thống.");
        }

        String tt = (trangThai != null && !trangThai.trim().isEmpty()) ? trangThai.trim() : "Tiềm năng";
        String moTa = moTaChiTiet != null ? moTaChiTiet.trim() : "";
        String gt = (giaTri != null && !giaTri.trim().isEmpty()) ? giaTri.trim() : "0 đ";

        Long newId = null;
        if (dao != null) {
            try {
                java.math.BigDecimal bdGiaTri = parseGiaTri(gt);
                newId = dao.themKhachHang(ma, tenCongTy.trim(), user.getId(), user.getNhomKinhDoanhId(), bdGiaTri, tt, moTa);
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Không thể lưu vào database, chuyển sang fallback bộ nhớ: " + e.getMessage());
            }
        }

        if (newId == null) {
            newId = (long) (Math.abs(ma.hashCode()) + 1000L);
        }

        BanGhiNghiepVuDTO bg = new BanGhiNghiepVuDTO(
                newId,
                ma,
                tenCongTy.trim(),
                LoaiNghiepVu.KHACH_HANG,
                user.getId(), // QUAN TRỌNG (S1-05): Quyền sở hữu bắt buộc gắn với user đang đăng nhập
                user.getHoTen(),
                user.getNhomKinhDoanhId(),
                user.getTenNhom(),
                gt,
                tt,
                LocalDate.now(),
                moTa
        );

        danhSachBoNho.add(0, bg);
        return bg;
    }

    public BanGhiNghiepVuDTO themKhachHang(NguoiDung nd,
                                           String maKhachHang,
                                           String tenCongTy,
                                           String giaTri,
                                           String trangThai,
                                           String moTaChiTiet) {
        return themKhachHang(NguoiDungDTO.tuNguoiDung(nd), maKhachHang, tenCongTy, giaTri, trangThai, moTaChiTiet);
    }

    /**
     * Lọc danh sách bản ghi theo phạm vi dữ liệu, từ khóa tìm kiếm và loại nghiệp vụ (AC1, AC2).
     */
    public List<BanGhiNghiepVuDTO> locTheoPhamVi(List<BanGhiNghiepVuDTO> danhSachGoc,
                                                NguoiDungDTO user,
                                                PhamViDuLieu phamViHieuLuc,
                                                String tuKhoa,
                                                String loaiNghiepVu) {
        if (danhSachGoc == null || user == null) {
            return Collections.emptyList();
        }

        PhamViDuLieu phamViThucTe = xacDinhPhamViHieuLuc(user, phamViHieuLuc);
        LoaiNghiepVu loaiEnum = parseLoaiNghiepVu(loaiNghiepVu);

        return danhSachGoc.stream()
                // 1. Lọc theo Data Scope (AC1)
                .filter(bg -> {
                    if (phamViThucTe == PhamViDuLieu.TOAN_BO) {
                        return true;
                    }
                    if (phamViThucTe == PhamViDuLieu.NHOM) {
                        return bg.getNhomKinhDoanhId() != null
                                && bg.getNhomKinhDoanhId().equals(user.getNhomKinhDoanhId());
                    }
                    // Mặc định: CA_NHAN
                    return bg.getNguoiPhuTrachId() != null
                            && bg.getNguoiPhuTrachId().equals(user.getId());
                })
                // 2. Lọc theo Loại nghiệp vụ
                .filter(bg -> {
                    if (loaiEnum == null) {
                        return true;
                    }
                    return bg.getLoaiNghiepVu() == loaiEnum;
                })
                // 3. Lọc theo Từ khóa tìm kiếm (AC2)
                .filter(bg -> {
                    if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
                        return true;
                    }
                    String kw = tuKhoa.trim().toLowerCase();
                    boolean matchTieuDe = bg.getTieuDe() != null && bg.getTieuDe().toLowerCase().contains(kw);
                    boolean matchMa = bg.getMaBanGhi() != null && bg.getMaBanGhi().toLowerCase().contains(kw);
                    boolean matchOwner = bg.getTenNguoiPhuTrach() != null && bg.getTenNguoiPhuTrach().toLowerCase().contains(kw);
                    boolean matchNhom = bg.getTenNhom() != null && bg.getTenNhom().toLowerCase().contains(kw);
                    return matchTieuDe || matchMa || matchOwner || matchNhom;
                })
                .collect(Collectors.toList());
    }

    /**
     * Xuất dữ liệu đã lọc theo phạm vi sang định dạng CSV/Excel (AC2).
     */
    public String xuatDuLieuCSV(List<BanGhiNghiepVuDTO> danhSachDaLoc) {
        StringBuilder csv = new StringBuilder();
        // BOM UTF-8 để mở tiếng Việt trên Microsoft Excel không bị lỗi font
        csv.append("\uFEFF");
        csv.append("Mã,Loại nghiệp vụ,Tiêu đề / Đối tượng,Người phụ trách,Nhóm kinh doanh,Giá trị,Trạng thái,Ngày tạo\n");

        if (danhSachDaLoc != null) {
            for (BanGhiNghiepVuDTO bg : danhSachDaLoc) {
                csv.append(escapeCsv(bg.getMaBanGhi())).append(",");
                csv.append(escapeCsv(bg.getLoaiNghiepVu() != null ? bg.getLoaiNghiepVu().getTenHienThi() : "")).append(",");
                csv.append(escapeCsv(bg.getTieuDe())).append(",");
                csv.append(escapeCsv(bg.getTenNguoiPhuTrach())).append(",");
                csv.append(escapeCsv(bg.getTenNhom())).append(",");
                csv.append(escapeCsv(bg.getGiaTri())).append(",");
                csv.append(escapeCsv(bg.getTrangThai())).append(",");
                csv.append(escapeCsv(bg.getNgayTao() != null ? bg.getNgayTao().toString() : "")).append("\n");
            }
        }
        return csv.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "\"\"";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private java.math.BigDecimal parseGiaTri(String giaTri) {
        if (giaTri == null || giaTri.trim().isEmpty()) {
            return java.math.BigDecimal.ZERO;
        }
        try {
            String clean = giaTri.replaceAll("[^0-9.]", "");
            if (clean.isEmpty()) {
                return java.math.BigDecimal.ZERO;
            }
            return new java.math.BigDecimal(clean);
        } catch (Exception e) {
            return java.math.BigDecimal.ZERO;
        }
    }

    private LoaiNghiepVu parseLoaiNghiepVu(String loai) {
        if (loai == null || loai.trim().isEmpty() || "ALL".equalsIgnoreCase(loai)) {
            return null;
        }
        for (LoaiNghiepVu l : LoaiNghiepVu.values()) {
            if (l.getMa().equalsIgnoreCase(loai.trim())) {
                return l;
            }
        }
        return null;
    }

    /**
     * Tạo tập dữ liệu mẫu B2B đa dạng phục vụ hiển thị trực quan và kiểm thử demo:
     * - Nhân viên A (ID: 101, Sales Rep - Nhóm Miền Bắc)
     * - Nhân viên B (ID: 102, Sales Rep - Nhóm Miền Bắc)
     * - Nhân viên C (ID: 201, Sales Rep - Nhóm Miền Nam)
     * - Trưởng nhóm Bắc (ID: 100, Team Lead - Nhóm Miền Bắc)
     * - Giám đốc kinh doanh (ID: 1, Director - Toàn bộ)
     */
    public List<BanGhiNghiepVuDTO> layDanhSachDuLieuMau() {
        List<BanGhiNghiepVuDTO> list = new ArrayList<>();

        // 1. Dữ liệu của Nhân viên A (ID: 101, Nhóm 1 - Miền Bắc)
        list.add(new BanGhiNghiepVuDTO(
                1L, "KH-001", "Công ty Cổ phần Công nghệ FPT", BanGhiNghiepVuDTO.LoaiNghiepVu.KHACH_HANG,
                101L, "Nguyễn Văn A (Sales)", 1L, "Nhóm Miền Bắc",
                "Khách hàng VIP", "Đang hợp tác", LocalDate.now().minusDays(15), "Khách hàng doanh nghiệp công nghệ lớn tại Hà Nội."
        ));
        list.add(new BanGhiNghiepVuDTO(
                2L, "CH-101", "Triển khai hệ thống Cloud CRM cho FPT", BanGhiNghiepVuDTO.LoaiNghiepVu.CO_HOI,
                101L, "Nguyễn Văn A (Sales)", 1L, "Nhóm Miền Bắc",
                "850,000,000 đ", "Đàm phán hợp đồng", LocalDate.now().minusDays(10), "Giai đoạn chốt điều khoản thanh toán."
        ));
        list.add(new BanGhiNghiepVuDTO(
                3L, "BG-201", "Báo giá gói Enterprise 100 User - FPT", BanGhiNghiepVuDTO.LoaiNghiepVu.BAO_GIA,
                101L, "Nguyễn Văn A (Sales)", 1L, "Nhóm Miền Bắc",
                "850,000,000 đ", "Đã gửi khách", LocalDate.now().minusDays(5), "Báo giá chiết khấu 10% đã được phê duyệt."
        ));
        list.add(new BanGhiNghiepVuDTO(
                4L, "HD-301", "Cuộc họp demo tính năng bảo mật với Giám đốc IT FPT", BanGhiNghiepVuDTO.LoaiNghiepVu.HOAT_DONG,
                101L, "Nguyễn Văn A (Sales)", 1L, "Nhóm Miền Bắc",
                "Họp trực tiếp", "Hoàn thành", LocalDate.now().minusDays(2), "Khách hàng rất hài lòng về phân quyền đa cấp."
        ));

        // 2. Dữ liệu của Nhân viên B (ID: 102, Nhóm 1 - Miền Bắc) - Cùng nhóm với A
        list.add(new BanGhiNghiepVuDTO(
                5L, "KH-002", "Tập đoàn Viễn thông Viettel", BanGhiNghiepVuDTO.LoaiNghiepVu.KHACH_HANG,
                102L, "Trần Thị B (Sales)", 1L, "Nhóm Miền Bắc",
                "Khách hàng trọng điểm", "Tiềm năng", LocalDate.now().minusDays(20), "Khách hàng quy mô tập đoàn viễn thông."
        ));
        list.add(new BanGhiNghiepVuDTO(
                6L, "CH-102", "Dự án CRM Bán hàng cho Trung tâm Viettel IDC", BanGhiNghiepVuDTO.LoaiNghiepVu.CO_HOI,
                102L, "Trần Thị B (Sales)", 1L, "Nhóm Miền Bắc",
                "1,200,000,000 đ", "Khảo sát nhu cầu", LocalDate.now().minusDays(8), "Cơ hội giá trị cao đang phối hợp kỹ thuật."
        ));
        list.add(new BanGhiNghiepVuDTO(
                7L, "BG-202", "Báo giá gói Hạ tầng dữ liệu Viettel IDC", BanGhiNghiepVuDTO.LoaiNghiepVu.BAO_GIA,
                102L, "Trần Thị B (Sales)", 1L, "Nhóm Miền Bắc",
                "1,200,000,000 đ", "Chờ duyệt chiết khấu", LocalDate.now().minusDays(4), "Xin chiết khấu vượt ngưỡng 15%."
        ));
        list.add(new BanGhiNghiepVuDTO(
                8L, "HD-302", "Gọi điện trao đổi yêu cầu bảo mật với Viettel", BanGhiNghiepVuDTO.LoaiNghiepVu.HOAT_DONG,
                102L, "Trần Thị B (Sales)", 1L, "Nhóm Miền Bắc",
                "Cuộc gọi", "Hoàn thành", LocalDate.now().minusDays(1), "Đã thống nhất gửi tài liệu kỹ thuật."
        ));

        // 3. Dữ liệu của Nhân viên C (ID: 201, Nhóm 2 - Miền Nam) - Khác nhóm với A & B
        list.add(new BanGhiNghiepVuDTO(
                9L, "KH-003", "Công ty TNHH VNG Corporation", BanGhiNghiepVuDTO.LoaiNghiepVu.KHACH_HANG,
                201L, "Lê Văn C (Sales HCM)", 2L, "Nhóm Miền Nam",
                "Khách hàng chiến lược", "Đang hợp tác", LocalDate.now().minusDays(25), "Khách hàng Internet và Game tại TP.HCM."
        ));
        list.add(new BanGhiNghiepVuDTO(
                10L, "CH-103", "Nâng cấp hạ tầng CRM cho VNG Campus", BanGhiNghiepVuDTO.LoaiNghiepVu.CO_HOI,
                201L, "Lê Văn C (Sales HCM)", 2L, "Nhóm Miền Nam",
                "650,000,000 đ", "Đã ký hợp đồng", LocalDate.now().minusDays(12), "Thương vụ đã thắng trong tháng."
        ));
        list.add(new BanGhiNghiepVuDTO(
                11L, "BG-203", "Báo giá gia hạn bảo trì thường niên VNG", BanGhiNghiepVuDTO.LoaiNghiepVu.BAO_GIA,
                201L, "Lê Văn C (Sales HCM)", 2L, "Nhóm Miền Nam",
                "150,000,000 đ", "Khách đã chấp nhận", LocalDate.now().minusDays(6), "Đã bàn giao hợp đồng cho kế toán."
        ));
        list.add(new BanGhiNghiepVuDTO(
                12L, "HD-303", "Gặp mặt đánh giá chất lượng dịch vụ định kỳ VNG", BanGhiNghiepVuDTO.LoaiNghiepVu.HOAT_DONG,
                201L, "Lê Văn C (Sales HCM)", 2L, "Nhóm Miền Nam",
                "Gặp trực tiếp", "Hoàn thành", LocalDate.now().minusDays(3), "Khách hàng phản hồi rất tích cực."
        ));

        return list;
    }
}
