package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.DotNhapDuLieuDAO;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dao.NhomKinhDoanhDAO;
import vn.nhom10.crm.dao.VaiTroDAO;
import vn.nhom10.crm.dto.BaoCaoNhapExcelDTO;
import vn.nhom10.crm.dto.DongExcelNguoiDungDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhomKinhDoanh;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.util.ExcelNguoiDungUtil;
import vn.nhom10.crm.util.PasswordUtil;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Service xử lý nghiệp vụ nhập người dùng hàng loạt từ tệp Excel (Story S2-01).
 * Đáp ứng các tiêu chí chấp nhận:
 * • Tải được tệp mẫu
 * • Xem trước và báo lỗi theo từng dòng trước khi nhập
 * • Dòng lỗi bị bỏ qua, dòng hợp lệ vẫn được nhập, có báo cáo tổng kết
 */
public class NguoiDungImportService {

    private static final Logger LOGGER = Logger.getLogger(NguoiDungImportService.class.getName());

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(0[3|5|7|8|9])[0-9]{8}$|^0[0-9]{9,10}$");

    private final NguoiDungDAO nguoiDungDAO;
    private final VaiTroDAO vaiTroDAO;
    private final NhomKinhDoanhDAO nhomKinhDoanhDAO;
    private final DotNhapDuLieuDAO dotNhapDuLieuDAO;

    public NguoiDungImportService() {
        this.nguoiDungDAO = new NguoiDungDAO();
        this.vaiTroDAO = new VaiTroDAO();
        this.nhomKinhDoanhDAO = new NhomKinhDoanhDAO();
        this.dotNhapDuLieuDAO = new DotNhapDuLieuDAO();
    }

    public NguoiDungImportService(NguoiDungDAO nguoiDungDAO, VaiTroDAO vaiTroDAO, NhomKinhDoanhDAO nhomKinhDoanhDAO, DotNhapDuLieuDAO dotNhapDuLieuDAO) {
        this.nguoiDungDAO = nguoiDungDAO != null ? nguoiDungDAO : new NguoiDungDAO();
        this.vaiTroDAO = vaiTroDAO != null ? vaiTroDAO : new VaiTroDAO();
        this.nhomKinhDoanhDAO = nhomKinhDoanhDAO != null ? nhomKinhDoanhDAO : new NhomKinhDoanhDAO();
        this.dotNhapDuLieuDAO = dotNhapDuLieuDAO != null ? dotNhapDuLieuDAO : new DotNhapDuLieuDAO();
    }

    /**
     * AC 1: Tải được tệp mẫu.
     * Tạo tệp mẫu Excel .xlsx chuẩn kèm hướng dẫn và danh mục vai trò/nhóm.
     *
     * @return mảng byte tệp Excel
     * @throws IOException khi tạo tệp lỗi
     */
    public byte[] taoTepMauExcel() throws IOException {
        return ExcelNguoiDungUtil.taoTepMauExcel();
    }

    /**
     * AC 2: Xem trước và báo lỗi theo từng dòng trước khi nhập.
     *
     * @param is luồng dữ liệu tệp Excel tải lên
     * @return báo cáo xem trước chi tiết
     * @throws Exception khi tệp lỗi
     */
    public BaoCaoNhapExcelDTO xemTruoc(InputStream is) throws Exception {
        return xemTruoc(is, null, "import_nguoi_dung.xlsx");
    }

    /**
     * AC 2: Xem trước và báo lỗi theo từng dòng trước khi nhập (kèm tên file và người thực hiện).
     */
    public BaoCaoNhapExcelDTO xemTruoc(InputStream is, Long nguoiThucHienId, String tenTep) throws Exception {
        List<DongExcelNguoiDungDTO> dsDong = ExcelNguoiDungUtil.docDanhSachTuExcel(is);
        BaoCaoNhapExcelDTO baoCao = kiemTraVaPhanTich(dsDong, tenTep);
        baoCao.setThongDiep(String.format("Đã phân tích %d dòng dữ liệu: %d dòng hợp lệ, %d dòng có lỗi cần lưu ý.",
                baoCao.getTongSoDong(), baoCao.getSoDongHopLe(), baoCao.getSoDongLoi()));
        return baoCao;
    }

    /**
     * AC 3: Dòng lỗi bị bỏ qua, dòng hợp lệ vẫn được nhập, có báo cáo tổng kết.
     *
     * @param is luồng dữ liệu tệp Excel tải lên
     * @return báo cáo tổng kết kết quả nhập
     * @throws Exception khi tệp lỗi
     */
    public BaoCaoNhapExcelDTO thucHienNhap(InputStream is) throws Exception {
        return thucHienNhap(is, null, "import_nguoi_dung.xlsx");
    }

    /**
     * AC 3: Dòng lỗi bị bỏ qua, dòng hợp lệ vẫn được nhập, có báo cáo tổng kết (kèm thông tin lưu vết).
     */
    public BaoCaoNhapExcelDTO thucHienNhap(InputStream is, Long nguoiThucHienId, String tenTep) throws Exception {
        // Bước 1: Đọc và thẩm định lại toàn bộ dữ liệu trước khi chèn
        List<DongExcelNguoiDungDTO> dsDong = ExcelNguoiDungUtil.docDanhSachTuExcel(is);
        BaoCaoNhapExcelDTO baoCao = kiemTraVaPhanTich(dsDong, tenTep);

        if (baoCao.getTongSoDong() == 0) {
            baoCao.setThongDiep("Tệp Excel không chứa dòng dữ liệu nào hợp lệ để nhập.");
            return baoCao;
        }

        // Bước 2: Duyệt từng dòng. Bỏ qua dòng lỗi, chỉ nhập dòng hợp lệ
        for (DongExcelNguoiDungDTO dong : baoCao.getDanhSachTatCaDong()) {
            if (!dong.isHopLe()) {
                // Dòng lỗi bị bỏ qua
                baoCao.capNhatKetQuaNhap(dong, false);
                continue;
            }

            try {
                // Xác định mật khẩu khởi tạo: dùng mật khẩu trong file hoặc sinh mật khẩu tạm mạnh
                String matKhauGoc;
                if (dong.getMatKhau() != null && !dong.getMatKhau().isBlank()) {
                    matKhauGoc = dong.getMatKhau().trim();
                } else {
                    matKhauGoc = PasswordUtil.taoMatKhauTam();
                }

                String matKhauHash = PasswordUtil.hashPassword(matKhauGoc);

                NguoiDung nguoiDung = new NguoiDung();
                nguoiDung.setHoTen(dong.getHoTen().trim());
                nguoiDung.setEmail(dong.getEmail().trim().toLowerCase());
                nguoiDung.setMatKhau(matKhauHash);
                nguoiDung.setSoDienThoai(dong.getSoDienThoai() != null && !dong.getSoDienThoai().isBlank()
                        ? dong.getSoDienThoai().trim() : null);
                nguoiDung.setTrangThai(NguoiDung.TRANG_THAI_CHO_KICH_HOAT);
                nguoiDung.setNhomKinhDoanhId(dong.getNhomKinhDoanhId());

                long idMoi = nguoiDungDAO.themNguoiDung(nguoiDung, dong.getDsVaiTroIds());

                dong.setDaNhap(true);
                dong.setIdNguoiDung(idMoi);
                dong.setMatKhauTam(matKhauGoc);

                baoCao.capNhatKetQuaNhap(dong, true);

            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Lỗi khi nhập dòng " + dong.getSoDong() + " (" + dong.getEmail() + "): " + e.getMessage(), e);
                dong.themLoi("Lỗi cơ sở dữ liệu khi lưu: " + e.getMessage());
                baoCao.capNhatKetQuaNhap(dong, false);
            }
        }

        // Bước 3: Lưu vết đợt nhập vào dot_nhap_du_lieu và loi_nhap_du_lieu
        dotNhapDuLieuDAO.luuDotNhap(baoCao, nguoiThucHienId);

        baoCao.setThongDiep(String.format("Kết quả nhập danh sách người dùng: %d/%d tài khoản nhập thành công, %d dòng lỗi bị bỏ qua.",
                baoCao.getSoDongThanhCong(), baoCao.getTongSoDong(), baoCao.getSoDongThatBai()));

        return baoCao;
    }

    /**
     * Kiểm tra tính hợp lệ của từng dòng dữ liệu và gắn danh sách lỗi chi tiết nếu có.
     */
    public BaoCaoNhapExcelDTO kiemTraVaPhanTich(List<DongExcelNguoiDungDTO> dsDong, String tenTep) {
        BaoCaoNhapExcelDTO baoCao = new BaoCaoNhapExcelDTO(tenTep);

        if (dsDong == null || dsDong.isEmpty()) {
            baoCao.napDanhSachDong(new ArrayList<>());
            return baoCao;
        }

        Map<String, VaiTro> mapVaiTro = napMapVaiTro();
        Map<String, NhomKinhDoanh> mapNhom = napMapNhom();
        Set<String> emailTrongFile = new HashSet<>();

        for (DongExcelNguoiDungDTO dong : dsDong) {
            kiemTraHopLeDong(dong, emailTrongFile, mapVaiTro, mapNhom);
        }

        baoCao.napDanhSachDong(dsDong);
        return baoCao;
    }

    private void kiemTraHopLeDong(DongExcelNguoiDungDTO dong, Set<String> emailTrongFile,
                                  Map<String, VaiTro> mapVaiTro, Map<String, NhomKinhDoanh> mapNhom) {

        // 1. Kiểm tra Họ và tên
        String hoTen = dong.getHoTen();
        if (hoTen == null || hoTen.isBlank()) {
            dong.themLoi("Họ và tên không được để trống");
        } else if (hoTen.trim().length() > 150) {
            dong.themLoi("Họ và tên không được vượt quá 150 ký tự");
        }

        // 2. Kiểm tra Email
        String email = dong.getEmail();
        if (email == null || email.isBlank()) {
            dong.themLoi("Email không được để trống");
        } else {
            String emailTrim = email.trim().toLowerCase();
            if (!EMAIL_PATTERN.matcher(emailTrim).matches()) {
                dong.themLoi("Email '" + email.trim() + "' không đúng định dạng chuẩn (ví dụ: ten@crm.vn)");
            } else if (emailTrongFile.contains(emailTrim)) {
                dong.themLoi("Email '" + emailTrim + "' bị trùng lặp với dòng khác trong cùng tệp Excel");
            } else if (nguoiDungDAO.kiemTraEmailTonTai(emailTrim, null) || nguoiDungDAO.timTheoEmail(emailTrim) != null) {
                dong.themLoi("Email '" + emailTrim + "' đã tồn tại trong hệ thống");
            } else {
                emailTrongFile.add(emailTrim);
            }
        }

        // 3. Kiểm tra Số điện thoại (tùy chọn)
        String sdt = dong.getSoDienThoai();
        if (sdt != null && !sdt.isBlank()) {
            String sdtTrim = sdt.trim();
            if (!PHONE_PATTERN.matcher(sdtTrim).matches()) {
                dong.themLoi("Số điện thoại '" + sdtTrim + "' không đúng định dạng số điện thoại Việt Nam");
            }
        }

        // 4. Kiểm tra Vai trò (bắt buộc)
        String vaiTroNhap = dong.getVaiTroNhap();
        boolean coVaiTroTruongNhom = false;

        if (vaiTroNhap == null || vaiTroNhap.isBlank()) {
            dong.themLoi("Mã vai trò không được để trống");
        } else {
            // Hỗ trợ nhiều vai trò phân tách bằng dấu phẩy hoặc chấm phẩy
            String[] dsMa = vaiTroNhap.split("[,;]");
            List<Integer> vaiTroIds = new ArrayList<>();
            List<String> tenVaiTroList = new ArrayList<>();

            for (String vtStr : dsMa) {
                String maTrim = vtStr.trim();
                if (maTrim.isEmpty()) continue;

                VaiTro vt = timVaiTroTrongMap(maTrim, mapVaiTro);
                if (vt == null) {
                    dong.themLoi("Vai trò '" + maTrim + "' không tồn tại trong danh mục hệ thống");
                } else {
                    if (!vaiTroIds.contains(vt.getId())) {
                        vaiTroIds.add(vt.getId());
                        tenVaiTroList.add(vt.getTenVaiTro() != null ? vt.getTenVaiTro() : vt.getMaVaiTro());
                    }
                    if (VaiTroEnum.TEAM_LEAD.getMaVaiTro().equalsIgnoreCase(vt.getMaVaiTro())) {
                        coVaiTroTruongNhom = true;
                    }
                }
            }

            if (!vaiTroIds.isEmpty()) {
                dong.setDsVaiTroIds(vaiTroIds);
                dong.setTenVaiTroGiaiQuyet(tenVaiTroList);
            }
        }

        // 5. Kiểm tra Nhóm kinh doanh
        String nhomNhap = dong.getNhomNhap();
        if (nhomNhap != null && !nhomNhap.isBlank()) {
            String nhomTrim = nhomNhap.trim();
            NhomKinhDoanh nhom = timNhomTrongMap(nhomTrim, mapNhom);
            if (nhom == null) {
                dong.themLoi("Nhóm kinh doanh '" + nhomTrim + "' không tồn tại trong hệ thống");
            } else {
                dong.setNhomKinhDoanhId(nhom.getId());
                dong.setTenNhomGiaiQuyet(nhom.getTenNhom());
            }
        }

        // Ràng buộc nghiệp vụ S1-09: Người giữ vai trò Trưởng nhóm kinh doanh bắt buộc phải có nhóm
        if (coVaiTroTruongNhom && dong.getNhomKinhDoanhId() == null) {
            dong.themLoi("Người giữ vai trò Trưởng nhóm kinh doanh bắt buộc phải được gán vào một nhóm kinh doanh cụ thể");
        }

        // 6. Kiểm tra mật khẩu tự chọn (nếu có)
        String matKhau = dong.getMatKhau();
        if (matKhau != null && !matKhau.isBlank()) {
            if (matKhau.trim().length() < 6) {
                dong.themLoi("Mật khẩu ban đầu phải có độ dài từ 6 ký tự trở lên");
            }
        }
    }

    private Map<String, VaiTro> napMapVaiTro() {
        Map<String, VaiTro> map = new HashMap<>();
        List<VaiTro> ds = vaiTroDAO.layTatCa();
        if (ds != null) {
            for (VaiTro vt : ds) {
                if (vt.getMaVaiTro() != null) {
                    map.put(vt.getMaVaiTro().toUpperCase().trim(), vt);
                }
                if (vt.getTenVaiTro() != null) {
                    map.put(vt.getTenVaiTro().toUpperCase().trim(), vt);
                }
            }
        }
        return map;
    }

    private Map<String, NhomKinhDoanh> napMapNhom() {
        Map<String, NhomKinhDoanh> map = new HashMap<>();
        List<NhomKinhDoanh> ds = nhomKinhDoanhDAO.layTatCa();
        if (ds != null) {
            for (NhomKinhDoanh nhom : ds) {
                if (nhom.getMaNhom() != null) {
                    map.put(nhom.getMaNhom().toUpperCase().trim(), nhom);
                }
                if (nhom.getTenNhom() != null) {
                    map.put(nhom.getTenNhom().toUpperCase().trim(), nhom);
                }
            }
        }
        return map;
    }

    private VaiTro timVaiTroTrongMap(String input, Map<String, VaiTro> map) {
        String key = input.toUpperCase().trim();
        if (map.containsKey(key)) {
            return map.get(key);
        }
        // Thử tìm qua VaiTroEnum
        VaiTroEnum vte = VaiTroEnum.tuMa(input);
        if (vte != null && map.containsKey(vte.getMaVaiTro().toUpperCase())) {
            return map.get(vte.getMaVaiTro().toUpperCase());
        }
        return null;
    }

    private NhomKinhDoanh timNhomTrongMap(String input, Map<String, NhomKinhDoanh> map) {
        String key = input.toUpperCase().trim();
        if (map.containsKey(key)) {
            return map.get(key);
        }
        return null;
    }
}
