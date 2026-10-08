package vn.nhom10.crm.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import vn.nhom10.crm.dao.KhachHangDAO;
import vn.nhom10.crm.dao.NhomKinhDoanhDAO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.TrangThaiKhachHangEnum;
import vn.nhom10.crm.model.VaiTroEnum;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service xử lý nghiệp vụ quản lý hồ sơ khách hàng doanh nghiệp (Story S3-01).
 * Đảm bảo các Acceptance Criteria:
 * - AC1: Khai báo tên công ty, mã số thuế, ngành nghề, quy mô, website, địa chỉ, người sở hữu
 * - AC2: Mã số thuế nếu có thì phải là duy nhất
 * - AC3: Khách hàng có 4 trạng thái: Tiềm năng, Đang giao dịch, Khách hàng, Ngừng hợp tác
 * - AC4: Nhân viên chỉ thấy khách hàng mình sở hữu; trưởng nhóm thấy toàn nhóm
 */
public class KhachHangService {

    private static final Logger LOGGER = Logger.getLogger(KhachHangService.class.getName());

    private final KhachHangDAO khachHangDAO;
    private final CoCauToChucService coCauToChucService;
    private final NhomKinhDoanhDAO nhomKinhDoanhDAO;

    public KhachHangService() {
        this(new KhachHangDAO(), new CoCauToChucService(), new NhomKinhDoanhDAO());
    }

    public KhachHangService(KhachHangDAO khachHangDAO) {
        this(khachHangDAO, new CoCauToChucService(), new NhomKinhDoanhDAO());
    }

    public KhachHangService(KhachHangDAO khachHangDAO, CoCauToChucService coCauToChucService, NhomKinhDoanhDAO nhomKinhDoanhDAO) {
        this.khachHangDAO = khachHangDAO != null ? khachHangDAO : new KhachHangDAO();
        this.coCauToChucService = coCauToChucService != null ? coCauToChucService : new CoCauToChucService();
        this.nhomKinhDoanhDAO = nhomKinhDoanhDAO != null ? nhomKinhDoanhDAO : new NhomKinhDoanhDAO();
    }

    /**
     * Kết quả kiểm tra quyền truy cập hoặc chỉnh sửa một bản ghi khách hàng (AC4).
     */
    public static class KetQuaQuyenKhachHang {
        private final boolean coQuyen;
        private final String thongBao;
        private final KhachHang khachHang;

        public KetQuaQuyenKhachHang(boolean coQuyen, String thongBao, KhachHang khachHang) {
            this.coQuyen = coQuyen;
            this.thongBao = thongBao;
            this.khachHang = khachHang;
        }

        public boolean isCoQuyen() {
            return coQuyen;
        }

        public String getThongBao() {
            return thongBao;
        }

        public KhachHang getKhachHang() {
            return khachHang;
        }
    }

    /**
     * Kiểm tra quyền xem chi tiết khách hàng phía server-side (AC4).
     */
    public KetQuaQuyenKhachHang kiemTraQuyenXem(NguoiDung user, KhachHang khachHang) {
        if (user == null) {
            return new KetQuaQuyenKhachHang(false, "Vui lòng đăng nhập để truy cập dữ liệu khách hàng.", null);
        }
        if (khachHang == null) {
            return new KetQuaQuyenKhachHang(false, "Không tìm thấy hồ sơ khách hàng yêu cầu.", null);
        }

        // 1. Quản trị hệ thống và Giám đốc kinh doanh có quyền toàn bộ
        if (user.coVaiTro(VaiTroEnum.ADMIN) || user.coVaiTro(VaiTroEnum.DIRECTOR)
                || user.layPhamViToiDa() == PhamViDuLieu.TOAN_BO) {
            return new KetQuaQuyenKhachHang(true, "Có quyền truy cập toàn bộ hệ thống.", khachHang);
        }

        // 2. Chính chủ sở hữu luôn có quyền xem
        if (khachHang.getNguoiSoHuuId() != null && khachHang.getNguoiSoHuuId().equals(user.getId())) {
            return new KetQuaQuyenKhachHang(true, "Có quyền truy cập khách hàng do bạn sở hữu.", khachHang);
        }

        // 3. Trưởng nhóm có quyền xem toàn bộ khách hàng thuộc nhóm của mình
        if (user.coVaiTro(VaiTroEnum.TEAM_LEAD) || user.layPhamViToiDa() == PhamViDuLieu.NHOM) {
            boolean thuocNhom = false;
            Long userNhomId = user.getNhomKinhDoanhId() != null ? Long.valueOf(user.getNhomKinhDoanhId()) : null;

            if (userNhomId != null) {
                if (userNhomId.equals(khachHang.getNhomKinhDoanhId())) {
                    thuocNhom = true;
                } else if (khachHang.getNhomKinhDoanhId() != null && coCauToChucService != null) {
                    try {
                        thuocNhom = coCauToChucService.kiemTraThuocPhamViCayToChuc(user.getId(), khachHang.getNhomKinhDoanhId());
                    } catch (Exception e) {
                        LOGGER.log(Level.FINE, "Lỗi kiểm tra cây tổ chức: " + e.getMessage());
                    }
                }
            }

            if (thuocNhom) {
                return new KetQuaQuyenKhachHang(true, "Có quyền truy cập khách hàng thuộc nhóm phụ trách.", khachHang);
            } else {
                String msg = String.format("Từ chối truy cập: Khách hàng '%s' (Mã: %s) thuộc về %s (%s), " +
                                "không nằm trong phạm vi nhóm quản lý của bạn (%s).",
                        khachHang.getTenCongTy(),
                        khachHang.getMaKhachHang() != null ? khachHang.getMaKhachHang() : "",
                        khachHang.getTenNguoiSoHuu(),
                        khachHang.getTenNhomKinhDoanh(),
                        user.getTenNhomKinhDoanh());
                return new KetQuaQuyenKhachHang(false, msg, khachHang);
            }
        }

        // 4. Mặc định (Sales Rep): Fail-Closed, từ chối xem khách của nhân viên khác
        String msg = String.format("Từ chối truy cập: Khách hàng '%s' hiện do %s phụ trách. " +
                        "Tài khoản của bạn chỉ có quyền xem khách hàng do chính mình sở hữu.",
                khachHang.getTenCongTy(), khachHang.getTenNguoiSoHuu());
        return new KetQuaQuyenKhachHang(false, msg, khachHang);
    }

    /**
     * Kiểm tra quyền cập nhật/sửa khách hàng phía server-side (AC4).
     */
    public KetQuaQuyenKhachHang kiemTraQuyenSua(NguoiDung user, KhachHang khachHang) {
        KetQuaQuyenKhachHang kqXem = kiemTraQuyenXem(user, khachHang);
        if (!kqXem.isCoQuyen()) {
            return new KetQuaQuyenKhachHang(false, "Từ chối thao tác sửa: " + kqXem.getThongBao(), khachHang);
        }
        return new KetQuaQuyenKhachHang(true, "Có quyền chỉnh sửa thông tin khách hàng.", khachHang);
    }

    /**
     * Tạo mới một khách hàng doanh nghiệp (AC1, AC2, AC3, AC4).
     * @param nguoiThucHien Người dùng đang thao tác
     * @param khachHang Thông tin khách hàng cần tạo
     * @return Khách hàng đã lưu với đầy đủ ID và mã
     */
    public KhachHang taoKhachHang(NguoiDung nguoiThucHien, KhachHang khachHang) throws SQLException {
        if (nguoiThucHien == null) {
            throw new SecurityException("Vui lòng đăng nhập trước khi tạo khách hàng.");
        }
        if (khachHang == null) {
            throw new IllegalArgumentException("Dữ liệu khách hàng không được để trống.");
        }

        // 1. Validation AC1: Tên công ty bắt buộc
        if (khachHang.getTenCongTy() == null || khachHang.getTenCongTy().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên công ty / khách hàng không được để trống.");
        }
        khachHang.setTenCongTy(khachHang.getTenCongTy().trim());

        // 2. Validation AC2: Mã số thuế nếu có thì phải là duy nhất
        String mst = khachHang.getMaSoThue();
        if (mst != null && !mst.trim().isEmpty()) {
            mst = mst.trim();
            if (khachHangDAO.kiemTraTrungMaSoThue(mst, null)) {
                String tenTonTai = khachHangDAO.layTenCongTyTheoMaSoThue(mst, null);
                throw new IllegalArgumentException("Mã số thuế '" + mst + "' đã tồn tại trong hệ thống" +
                        (tenTonTai != null ? " (thuộc khách hàng '" + tenTonTai + "')" : "") + ".");
            }
            khachHang.setMaSoThue(mst);
        } else {
            khachHang.setMaSoThue(null);
        }

        // 3. Validation AC3: Trạng thái khách hàng
        String tt = khachHang.getTrangThai();
        if (tt == null || tt.trim().isEmpty()) {
            khachHang.setTrangThai(TrangThaiKhachHangEnum.TIEM_NANG.getMa());
        } else {
            TrangThaiKhachHangEnum en = TrangThaiKhachHangEnum.tuChuoi(tt);
            if (en == null) {
                throw new IllegalArgumentException("Trạng thái khách hàng không hợp lệ. Chỉ chấp nhận: " +
                        "Tiềm năng, Đang giao dịch, Khách hàng, Ngừng hợp tác.");
            }
            khachHang.setTrangThai(en.getMa());
        }

        // 4. Quyền sở hữu & Data Scope (AC4)
        // Chống giả mạo người sở hữu: Nhân viên chỉ được gán chính mình
        boolean laAdminHoacDirector = nguoiThucHien.coVaiTro(VaiTroEnum.ADMIN) || nguoiThucHien.coVaiTro(VaiTroEnum.DIRECTOR);
        boolean laTeamLead = nguoiThucHien.coVaiTro(VaiTroEnum.TEAM_LEAD);

        if (!laAdminHoacDirector && !laTeamLead) {
            // Sales Rep bắt buộc là chính mình
            khachHang.setNguoiSoHuuId(nguoiThucHien.getId());
            khachHang.setNhomKinhDoanhId(nguoiThucHien.getNhomKinhDoanhId() != null ? Long.valueOf(nguoiThucHien.getNhomKinhDoanhId()) : null);
        } else if (laTeamLead) {
            // Team Lead nếu không chọn ai thì mặc định là mình, nếu chọn thì phải cùng nhóm
            if (khachHang.getNguoiSoHuuId() == null) {
                khachHang.setNguoiSoHuuId(nguoiThucHien.getId());
                khachHang.setNhomKinhDoanhId(nguoiThucHien.getNhomKinhDoanhId() != null ? Long.valueOf(nguoiThucHien.getNhomKinhDoanhId()) : null);
            } else if (khachHang.getNhomKinhDoanhId() == null) {
                khachHang.setNhomKinhDoanhId(nguoiThucHien.getNhomKinhDoanhId() != null ? Long.valueOf(nguoiThucHien.getNhomKinhDoanhId()) : null);
            }
        } else {
            // Director / Admin
            if (khachHang.getNguoiSoHuuId() == null) {
                khachHang.setNguoiSoHuuId(nguoiThucHien.getId());
            }
        }

        // 5. Kiểm tra mã khách hàng nếu người dùng tự nhập
        if (khachHang.getMaKhachHang() != null && !khachHang.getMaKhachHang().trim().isEmpty()) {
            String ma = khachHang.getMaKhachHang().trim();
            if (khachHangDAO.kiemTraTrungMaKhachHang(ma, null)) {
                throw new IllegalArgumentException("Mã khách hàng '" + ma + "' đã tồn tại trong hệ thống.");
            }
            khachHang.setMaKhachHang(ma);
        }

        Long idMoi = khachHangDAO.themKhachHang(khachHang);
        if (idMoi == null) {
            throw new SQLException("Không thể lưu thông tin khách hàng vào cơ sở dữ liệu.");
        }

        return khachHangDAO.timTheoId(idMoi);
    }

    /**
     * Cập nhật thông tin khách hàng doanh nghiệp (AC1, AC2, AC3, AC4).
     */
    public KhachHang capNhatKhachHang(NguoiDung nguoiThucHien, KhachHang khachHangMoi) throws SQLException {
        if (nguoiThucHien == null) {
            throw new SecurityException("Vui lòng đăng nhập trước khi cập nhật.");
        }
        if (khachHangMoi == null || khachHangMoi.getId() == null) {
            throw new IllegalArgumentException("Không xác định được khách hàng cần cập nhật.");
        }

        KhachHang khachHangCu = khachHangDAO.timTheoId(khachHangMoi.getId());
        if (khachHangCu == null) {
            throw new IllegalArgumentException("Khách hàng không tồn tại trong hệ thống.");
        }

        // Kiểm tra quyền sửa (AC4)
        KetQuaQuyenKhachHang kq = kiemTraQuyenSua(nguoiThucHien, khachHangCu);
        if (!kq.isCoQuyen()) {
            throw new SecurityException(kq.getThongBao());
        }

        // Validation AC1: Tên công ty
        if (khachHangMoi.getTenCongTy() == null || khachHangMoi.getTenCongTy().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên công ty / khách hàng không được để trống.");
        }
        khachHangMoi.setTenCongTy(khachHangMoi.getTenCongTy().trim());

        // Validation AC2: Mã số thuế nếu có thì phải duy nhất
        String mst = khachHangMoi.getMaSoThue();
        if (mst != null && !mst.trim().isEmpty()) {
            mst = mst.trim();
            if (khachHangDAO.kiemTraTrungMaSoThue(mst, khachHangMoi.getId())) {
                String tenTonTai = khachHangDAO.layTenCongTyTheoMaSoThue(mst, khachHangMoi.getId());
                throw new IllegalArgumentException("Mã số thuế '" + mst + "' đã tồn tại trong hệ thống" +
                        (tenTonTai != null ? " (thuộc khách hàng '" + tenTonTai + "')" : "") + ".");
            }
            khachHangMoi.setMaSoThue(mst);
        } else {
            khachHangMoi.setMaSoThue(null);
        }

        // Validation AC3: Trạng thái khách hàng
        String tt = khachHangMoi.getTrangThai();
        if (tt != null && !tt.trim().isEmpty()) {
            TrangThaiKhachHangEnum en = TrangThaiKhachHangEnum.tuChuoi(tt);
            if (en == null) {
                throw new IllegalArgumentException("Trạng thái khách hàng không hợp lệ. Chỉ chấp nhận: " +
                        "Tiềm năng, Đang giao dịch, Khách hàng, Ngừng hợp tác.");
            }
            khachHangMoi.setTrangThai(en.getMa());
        } else {
            khachHangMoi.setTrangThai(khachHangCu.getTrangThai());
        }

        // Bảo vệ quyền sở hữu: nếu là Sales Rep, không được chuyển quyền sở hữu của mình cho người khác
        boolean laAdminHoacDirector = nguoiThucHien.coVaiTro(VaiTroEnum.ADMIN) || nguoiThucHien.coVaiTro(VaiTroEnum.DIRECTOR);
        boolean laTeamLead = nguoiThucHien.coVaiTro(VaiTroEnum.TEAM_LEAD);
        if (!laAdminHoacDirector && !laTeamLead) {
            khachHangMoi.setNguoiSoHuuId(khachHangCu.getNguoiSoHuuId());
            khachHangMoi.setNhomKinhDoanhId(khachHangCu.getNhomKinhDoanhId());
        } else {
            if (khachHangMoi.getNguoiSoHuuId() == null) {
                khachHangMoi.setNguoiSoHuuId(khachHangCu.getNguoiSoHuuId());
            }
            if (khachHangMoi.getNhomKinhDoanhId() == null) {
                khachHangMoi.setNhomKinhDoanhId(khachHangCu.getNhomKinhDoanhId());
            }
        }

        boolean ok = khachHangDAO.capNhatKhachHang(khachHangMoi);
        if (!ok) {
            throw new SQLException("Không thể cập nhật hồ sơ khách hàng.");
        }

        return khachHangDAO.timTheoId(khachHangMoi.getId());
    }

    /**
     * Tìm khách hàng theo ID kèm kiểm tra quyền xem (AC4).
     */
    public KhachHang timTheoIdVaKiemTraQuyen(Long id, NguoiDung user) throws SQLException {
        if (id == null) {
            return null;
        }
        KhachHang kh = khachHangDAO.timTheoId(id);
        if (kh == null) {
            return null;
        }
        KetQuaQuyenKhachHang kq = kiemTraQuyenXem(user, kh);
        if (!kq.isCoQuyen()) {
            throw new SecurityException(kq.getThongBao());
        }
        return kh;
    }

    /**
     * Lấy danh sách khách hàng phân trang lọc theo Data Scope của người dùng (AC4).
     */
    public List<KhachHang> layDanhSachTheoQuyen(NguoiDung user,
                                                PhamViDuLieu phamViYeuCau,
                                                String tuKhoa,
                                                String trangThai,
                                                Long nganhNgheId,
                                                Long quyMoId,
                                                int trang,
                                                int kichThuocTrang) throws SQLException {
        if (user == null) {
            return Collections.emptyList();
        }

        PhamViDuLieu phamViHieuLuc = xacDinhPhamViHieuLuc(user, phamViYeuCau);
        Set<Long> dsNhomIds = layTapHopNhomTheoUser(user);

        int offset = Math.max(0, (trang - 1) * kichThuocTrang);
        return khachHangDAO.layDanhSach(user.getId(), dsNhomIds, phamViHieuLuc, tuKhoa, trangThai, nganhNgheId, quyMoId, offset, kichThuocTrang);
    }

    /**
     * Đếm tổng số khách hàng theo quyền và bộ lọc.
     */
    public int demTongSoTheoQuyen(NguoiDung user,
                                  PhamViDuLieu phamViYeuCau,
                                  String tuKhoa,
                                  String trangThai,
                                  Long nganhNgheId,
                                  Long quyMoId) throws SQLException {
        if (user == null) {
            return 0;
        }

        PhamViDuLieu phamViHieuLuc = xacDinhPhamViHieuLuc(user, phamViYeuCau);
        Set<Long> dsNhomIds = layTapHopNhomTheoUser(user);

        return khachHangDAO.demTongSo(user.getId(), dsNhomIds, phamViHieuLuc, tuKhoa, trangThai, nganhNgheId, quyMoId);
    }

    /**
     * Xác định phạm vi dữ liệu tối đa và an toàn cho người dùng (Fail-Closed).
     */
    public PhamViDuLieu xacDinhPhamViHieuLuc(NguoiDung user, PhamViDuLieu phamViYeuCau) {
        if (user == null) {
            return PhamViDuLieu.CA_NHAN;
        }

        PhamViDuLieu phamViToiDa = user.layPhamViToiDa();
        if (user.coVaiTro(VaiTroEnum.ADMIN) || user.coVaiTro(VaiTroEnum.DIRECTOR)) {
            phamViToiDa = PhamViDuLieu.TOAN_BO;
        } else if (user.coVaiTro(VaiTroEnum.TEAM_LEAD)) {
            phamViToiDa = PhamViDuLieu.NHOM;
        }

        if (phamViYeuCau == null) {
            return phamViToiDa;
        }

        // Không cho phép yêu cầu phạm vi cao hơn quyền hạn tối đa
        if (phamViYeuCau == PhamViDuLieu.TOAN_BO && phamViToiDa != PhamViDuLieu.TOAN_BO) {
            return phamViToiDa;
        }
        if (phamViYeuCau == PhamViDuLieu.NHOM && phamViToiDa == PhamViDuLieu.CA_NHAN) {
            return PhamViDuLieu.CA_NHAN;
        }

        return phamViYeuCau;
    }

    /**
     * Lấy tập hợp ID các nhóm thuộc quyền quản lý của người dùng.
     */
    public Set<Long> layTapHopNhomTheoUser(NguoiDung user) {
        Set<Long> ketQua = new HashSet<>();
        if (user == null || user.getNhomKinhDoanhId() == null) {
            return ketQua;
        }

        Long nhomGocId = Long.valueOf(user.getNhomKinhDoanhId());
        ketQua.add(nhomGocId);

        if (coCauToChucService != null) {
            try {
                Set<Long> nhomCon = coCauToChucService.layDsIdNhomDuocXemBoiTruongNhom(user.getId());
                if (nhomCon != null) {
                    ketQua.addAll(nhomCon);
                }
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Không thể lấy cây nhóm con cháu: " + e.getMessage());
            }
        }
        return ketQua;
    }

    /**
     * Xuất danh sách khách hàng doanh nghiệp ra tệp Excel (.xlsx).
     */
    public byte[] xuatDanhSachExcel(List<KhachHang> danhSach) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Danh sách khách hàng");

            // Tạo header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Tạo data style
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            CellStyle tienStyle = workbook.createCellStyle();
            tienStyle.cloneStyleFrom(dataStyle);
            DataFormat df = workbook.createDataFormat();
            tienStyle.setDataFormat(df.getFormat("#,##0"));
            tienStyle.setAlignment(HorizontalAlignment.RIGHT);

            String[] tieuDeCot = {
                    "STT", "Mã KH", "Tên công ty / Khách hàng", "Mã số thuế",
                    "Ngành nghề", "Quy mô", "Website", "Địa chỉ",
                    "Người sở hữu", "Nhóm kinh doanh", "Doanh thu ước tính (VND)",
                    "Trạng thái", "Ngày tạo"
            };

            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(24);
            for (int i = 0; i < tieuDeCot.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(tieuDeCot[i]);
                cell.setCellStyle(headerStyle);
            }

            if (danhSach != null) {
                int rowIdx = 1;
                for (KhachHang kh : danhSach) {
                    Row row = sheet.createRow(rowIdx);
                    row.setHeightInPoints(20);

                    Cell c0 = row.createCell(0);
                    c0.setCellValue(rowIdx);
                    c0.setCellStyle(dataStyle);

                    Cell c1 = row.createCell(1);
                    c1.setCellValue(kh.getMaKhachHang() != null ? kh.getMaKhachHang() : "");
                    c1.setCellStyle(dataStyle);

                    Cell c2 = row.createCell(2);
                    c2.setCellValue(kh.getTenCongTy() != null ? kh.getTenCongTy() : "");
                    c2.setCellStyle(dataStyle);

                    Cell c3 = row.createCell(3);
                    c3.setCellValue(kh.getMaSoThue() != null ? kh.getMaSoThue() : "");
                    c3.setCellStyle(dataStyle);

                    Cell c4 = row.createCell(4);
                    c4.setCellValue(kh.getTenNganhNghe() != null ? kh.getTenNganhNghe() : "");
                    c4.setCellStyle(dataStyle);

                    Cell c5 = row.createCell(5);
                    c5.setCellValue(kh.getTenQuyMo() != null ? kh.getTenQuyMo() : "");
                    c5.setCellStyle(dataStyle);

                    Cell c6 = row.createCell(6);
                    c6.setCellValue(kh.getWebsite() != null ? kh.getWebsite() : "");
                    c6.setCellStyle(dataStyle);

                    Cell c7 = row.createCell(7);
                    c7.setCellValue(kh.getDiaChi() != null ? kh.getDiaChi() : "");
                    c7.setCellStyle(dataStyle);

                    Cell c8 = row.createCell(8);
                    c8.setCellValue(kh.getTenNguoiSoHuu() != null ? kh.getTenNguoiSoHuu() : "");
                    c8.setCellStyle(dataStyle);

                    Cell c9 = row.createCell(9);
                    c9.setCellValue(kh.getTenNhomKinhDoanh() != null ? kh.getTenNhomKinhDoanh() : "");
                    c9.setCellStyle(dataStyle);

                    Cell c10 = row.createCell(10);
                    c10.setCellValue(kh.getDoanhThuUocTinh() != null ? kh.getDoanhThuUocTinh().doubleValue() : 0.0);
                    c10.setCellStyle(tienStyle);

                    Cell c11 = row.createCell(11);
                    c11.setCellValue(kh.getTrangThaiHienThi());
                    c11.setCellStyle(dataStyle);

                    Cell c12 = row.createCell(12);
                    c12.setCellValue(kh.getNgayTaoDinhDang());
                    c12.setCellStyle(dataStyle);

                    rowIdx++;
                }
            }

            for (int i = 0; i < tieuDeCot.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
