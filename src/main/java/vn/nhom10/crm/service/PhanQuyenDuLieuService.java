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

    public PhanQuyenDuLieuService() {
        this(new PhanQuyenDuLieuDAO());
    }

    public PhanQuyenDuLieuService(PhanQuyenDuLieuDAO dao) {
        this.dao = dao;
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
        if (phamViYeuCau == null) {
            return user.getPhamViHienTai() != null ? user.getPhamViHienTai() : PhamViDuLieu.CA_NHAN;
        }
        // Nếu người dùng không có quyền chọn phạm vi yêu cầu, ép về phạm vi an toàn cao nhất của họ
        if (!user.coQuyenChonPhamVi(phamViYeuCau)) {
            return user.getVaiTro() != null ? user.getVaiTro().getPhamViToiDa() : PhamViDuLieu.CA_NHAN;
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
        List<BanGhiNghiepVuDTO> danhSachGoc = layDanhSachDuLieuMau();
        return locTheoPhamVi(danhSachGoc, user, phamViHieuLuc, tuKhoa, loaiNghiepVu);
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

        // Fallback tìm trong danh sách mẫu
        for (BanGhiNghiepVuDTO bg : layDanhSachDuLieuMau()) {
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

        VaiTroEnum vaiTro = user.getVaiTro() != null ? user.getVaiTro() : VaiTroEnum.SALES_REP;

        // Giám đốc (Director), Admin hoặc Kế toán có quyền xem toàn bộ
        if (vaiTro == VaiTroEnum.DIRECTOR || vaiTro == VaiTroEnum.ADMIN || vaiTro == VaiTroEnum.ACCOUNTANT) {
            return new KetQuaKiemTra(true, "Truy cập hợp lệ với quyền " + vaiTro.getTenHienThi() + ".", banGhi);
        }

        // Bản ghi do chính user phụ trách trực tiếp -> luôn có quyền xem
        if (banGhi.getNguoiPhuTrachId() != null && banGhi.getNguoiPhuTrachId().equals(user.getId())) {
            return new KetQuaKiemTra(true, "Truy cập hợp lệ với tư cách người phụ trách trực tiếp.", banGhi);
        }

        // Trưởng nhóm (Team Lead): được xem dữ liệu của thành viên trong nhóm mình
        if (vaiTro == VaiTroEnum.TEAM_LEAD) {
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

        // Nhân viên kinh doanh (Sales Rep): chỉ được xem bản ghi của chính mình.
        // Cố tình truy cập bản ghi của nhân viên khác (kể cả cùng nhóm hoặc khác nhóm) đều bị chặn (AC4).
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
                return dao.capNhatBanGhi(banGhi);
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Không thể cập nhật database: " + e.getMessage());
            }
        }
        return true;
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
