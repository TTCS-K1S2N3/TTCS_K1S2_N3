package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.TruongTuyChinhDAO;
import vn.nhom10.crm.dto.TruongTuyChinhDTO;
import vn.nhom10.crm.model.KieuDuLieuCustomField;
import vn.nhom10.crm.model.LoaiDoiTuongCustomField;
import vn.nhom10.crm.model.TruongTuyChinh;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Service xử lý toàn bộ nghiệp vụ quản lý Trường tuỳ chỉnh (Story S2-08).
 * Đáp ứng đầy đủ các Acceptance Criteria:
 * 1. Thêm trường kiểu văn bản, số, ngày, danh sách chọn.
 * 2. Đặt được trường là bắt buộc hay không.
 * 3. Trường tuỳ chỉnh xuất hiện trong biểu mẫu, bộ lọc và bản xuất Excel.
 */
public class TruongTuyChinhService {

    private final TruongTuyChinhDAO dao;

    // Danh sách tên cột hệ thống mặc định để tránh xung đột mã trường kỹ thuật
    private static final Set<String> CAC_MA_CAM = Set.of(
            "id", "created_at", "updated_at", "created_by", "trang_thai",
            "nguoi_so_huu_id", "nhom_kinh_doanh_id", "nguoi_phu_trach_id",
            "ma_khach_hang", "ten_cong_ty", "ten_chuan_hoa", "ma_so_thue",
            "nganh_nghe_id", "quy_mo_id", "website", "website_chuan_hoa",
            "so_dien_thoai", "email", "dia_chi", "tinh_thanh_id", "quan_huyen_id",
            "phuong_xa_id", "mo_ta", "nguon_lead_id",
            "ma_co_hoi", "ten_co_hoi", "khach_hang_id", "nguoi_lien_he_id",
            "pipeline_id", "giai_doan_id", "gia_tri", "tien_te", "xac_suat",
            "ngay_ky_vong", "ly_do_that_bai", "mo_ta_that_bai"
    );

    public TruongTuyChinhService() {
        this.dao = new TruongTuyChinhDAO();
    }

    public TruongTuyChinhService(TruongTuyChinhDAO dao) {
        this.dao = dao;
    }

    // =========================================================================
    // QUẢN TRỊ ĐỊNH NGHĨA TRƯỜNG TUỲ CHỈNH (ADMIN / DIRECTOR)
    // =========================================================================

    /**
     * Lấy danh sách trường tuỳ chỉnh theo đối tượng (KHACH_HANG hoặc CO_HOI).
     */
    public List<TruongTuyChinh> layDanhSachTheoDoiTuong(String loaiDoiTuong, boolean chiLayDangHoatDong) {
        if (loaiDoiTuong == null || loaiDoiTuong.isBlank()) {
            return dao.layDanhSachTheoDoiTuong("KHACH_HANG", chiLayDangHoatDong);
        }
        String upper = loaiDoiTuong.trim().toUpperCase();
        if (!"KHACH_HANG".equals(upper) && !"CO_HOI".equals(upper)) {
            return Collections.emptyList();
        }
        return dao.layDanhSachTheoDoiTuong(upper, chiLayDangHoatDong);
    }

    /**
     * Lấy trường tuỳ chỉnh theo ID.
     */
    public TruongTuyChinh layTheoId(long id) {
        return dao.layTheoId(id);
    }

    /**
     * Xác thực thông tin khi tạo hoặc cập nhật trường tuỳ chỉnh.
     * Trả về Map<tênTrườngLỗi, thôngBáoLỗi>. Rỗng nếu hoàn toàn hợp lệ.
     */
    public Map<String, String> validateDinhNghiaTruong(TruongTuyChinhDTO dto, boolean laTaoMoi) {
        Map<String, String> errors = new HashMap<>();

        // 1. Kiểm tra đối tượng
        String doiTuong = dto.getDoiTuong();
        if (doiTuong == null || doiTuong.isBlank() || LoaiDoiTuongCustomField.tuMa(doiTuong) == null) {
            errors.put("doiTuong", "Vui lòng chọn loại đối tượng hợp lệ (Khách hàng hoặc Cơ hội).");
        }

        // 2. Kiểm tra nhãn hiển thị
        String nhanHien = dto.getNhanHien();
        if (nhanHien == null || nhanHien.trim().isEmpty()) {
            errors.put("nhanHien", "Nhãn hiển thị không được để trống.");
        } else if (nhanHien.trim().length() > 150) {
            errors.put("nhanHien", "Nhãn hiển thị không được vượt quá 150 ký tự.");
        }

        // 3. Kiểm tra tên kỹ thuật (chỉ kiểm tra khi tạo mới vì khi sửa không cho đổi)
        if (laTaoMoi) {
            String tenTruong = dto.getTenTruong();
            if (tenTruong == null || tenTruong.trim().isEmpty()) {
                errors.put("tenTruong", "Tên kỹ thuật không được để trống.");
            } else {
                String slug = tenTruong.trim().toLowerCase();
                if (!slug.matches("^[a-z][a-z0-9_]*$")) {
                    errors.put("tenTruong", "Tên kỹ thuật chỉ gồm chữ cái thường, số và dấu gạch dưới (_). Phải bắt đầu bằng chữ cái.");
                } else if (slug.length() > 80) {
                    errors.put("tenTruong", "Tên kỹ thuật không được vượt quá 80 ký tự.");
                } else if (CAC_MA_CAM.contains(slug)) {
                    errors.put("tenTruong", "Tên kỹ thuật trùng với trường chuẩn của hệ thống. Vui lòng chọn tên khác.");
                } else if (doiTuong != null && dao.kiemTraTonTaiMa(doiTuong, slug, null)) {
                    errors.put("tenTruong", "Tên kỹ thuật này đã tồn tại trong đối tượng " + doiTuong + ".");
                }
            }
        }

        // 4. Kiểm tra kiểu dữ liệu
        String kieu = dto.getKieuDuLieu();
        KieuDuLieuCustomField kieuEnum = KieuDuLieuCustomField.tuMa(kieu);
        if (kieu == null || kieuEnum == null) {
            errors.put("kieuDuLieu", "Kiểu dữ liệu không hợp lệ. Chỉ chấp nhận: Văn bản, Số, Ngày hoặc Danh sách chọn.");
        } else if (kieuEnum == KieuDuLieuCustomField.DANH_SACH_CHON) {
            // Khi kiểu là Danh sách chọn, bắt buộc phải có ít nhất 1 lựa chọn hợp lệ
            List<String> options = dto.getDanhSachLuaChon();
            boolean coLuaChon = false;
            if (options != null) {
                for (String opt : options) {
                    if (opt != null && !opt.trim().isEmpty()) {
                        coLuaChon = true;
                        break;
                    }
                }
            }
            if (!coLuaChon) {
                errors.put("danhSachLuaChon", "Kiểu danh sách chọn bắt buộc phải có ít nhất một lựa chọn.");
            }
        } else {
            // Nếu không phải DANH_SACH_CHON, xoá toàn bộ lựa chọn rác nếu có
            dto.setDanhSachLuaChon(new ArrayList<>());
        }

        return errors;
    }

    /**
     * Tạo mới một trường tuỳ chỉnh (Story S2-08).
     */
    public long taoTruongTuyChinh(TruongTuyChinhDTO dto, Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Không xác định được người dùng tạo trường.");
        }

        Map<String, String> errors = validateDinhNghiaTruong(dto, true);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(errors.values().iterator().next());
        }

        TruongTuyChinh model = dto.chuyenSangModel();
        model.setCreatedBy(userId);

        if (!KieuDuLieuCustomField.DANH_SACH_CHON.getMa().equalsIgnoreCase(model.getKieuDuLieu())) {
            model.setDanhSachLuaChon(new ArrayList<>());
            model.setLuaChonJson(null);
        }

        // Lưu metadata cấu hình hiển thị (bộ lọc, excel) vào giaTriMacDinhJson
        model.setGiaTriMacDinhJson(buildMetadataJson(dto.isHienThiBoDac(), dto.isHienThiExcel()));

        long id = dao.them(model);
        if (id <= 0) {
            throw new RuntimeException("Không thể tạo trường tuỳ chỉnh vào cơ sở dữ liệu.");
        }
        return id;
    }

    /**
     * Cập nhật trường tuỳ chỉnh đã có.
     */
    public boolean capNhatTruongTuyChinh(TruongTuyChinhDTO dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("ID trường tuỳ chỉnh không được để trống.");
        }

        TruongTuyChinh existing = dao.layTheoId(dto.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Không tìm thấy trường tuỳ chỉnh với ID=" + dto.getId());
        }

        Map<String, String> errors = validateDinhNghiaTruong(dto, false);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(errors.values().iterator().next());
        }

        existing.setTenNhan(dto.getNhanHien());
        existing.setBatBuoc(dto.isBatBuoc());
        existing.setThuTuHienThi(dto.getThuTu());
        existing.setHoatDong(dto.isDangHoatDong());
        existing.setHienThiBoDac(dto.isHienThiBoDac());
        existing.setHienThiExcel(dto.isHienThiExcel());

        if (KieuDuLieuCustomField.DANH_SACH_CHON.getMa().equalsIgnoreCase(existing.getKieuDuLieu())) {
            existing.setDanhSachLuaChon(dto.getDanhSachLuaChon());
        } else {
            existing.setDanhSachLuaChon(new ArrayList<>());
            existing.setLuaChonJson(null);
        }

        existing.setGiaTriMacDinhJson(buildMetadataJson(dto.isHienThiBoDac(), dto.isHienThiExcel()));

        return dao.capNhat(existing);
    }

    /**
     * Bật hoặc tắt trạng thái hoạt động của trường tuỳ chỉnh.
     */
    public boolean doiTrangThai(long id, boolean hoatDong) {
        return dao.doiTrangThai(id, hoatDong);
    }

    // =========================================================================
    // AC3: HIỂN THỊ VÀ LƯU DỮ LIỆU BIỂU MẪU (FORM)
    // =========================================================================

    /**
     * Lấy danh sách trường tuỳ chỉnh đang hoạt động phục vụ hiển thị trong biểu mẫu Form.
     */
    public List<TruongTuyChinh> layDanhSachChoBieuMau(String loaiDoiTuong) {
        return layDanhSachTheoDoiTuong(loaiDoiTuong, true);
    }

    /**
     * Lấy giá trị các trường tuỳ chỉnh của một bản ghi để nạp vào Form sửa.
     */
    public Map<String, String> layGiaTriTheoDoiTuong(String loaiDoiTuong, long doiTuongId) {
        return dao.layGiaTriTheoDoiTuong(chuanHoaLoaiDoiTuong(loaiDoiTuong), doiTuongId);
    }

    /**
     * Kiểm tra hợp lệ dữ liệu người dùng nhập cho các trường tuỳ chỉnh trong biểu mẫu.
     * Kiểm tra:
     * - Bắt buộc (batBuoc = true) -> không để trống
     * - Kiểu Số (SO) -> phải là số hợp lệ
     * - Kiểu Ngày (NGAY) -> phải là định dạng ngày hợp lệ (YYYY-MM-DD)
     * - Kiểu Danh sách chọn (DANH_SACH_CHON) -> phải nằm trong danh mục lựa chọn
     */
    public Map<String, String> validateGiaTriBieuMau(String loaiDoiTuong, Map<String, String> rawInput) {
        Map<String, String> errors = new HashMap<>();
        List<TruongTuyChinh> dsTruong = layDanhSachChoBieuMau(loaiDoiTuong);

        for (TruongTuyChinh t : dsTruong) {
            String ma = t.getMaTruong();
            String rawVal = rawInput != null ? rawInput.get(ma) : null;
            if (rawVal == null && rawInput != null) {
                // Hỗ trợ trường hợp form gửi param với prefix ttc_ (vd ttc_nguon_khach_hang)
                rawVal = rawInput.get("ttc_" + ma);
            }

            String val = (rawVal != null) ? rawVal.trim() : "";

            // 1. Kiểm tra bắt buộc nhập
            if (t.isBatBuoc() && val.isEmpty()) {
                errors.put(ma, "Trường '" + t.getNhanHien() + "' là bắt buộc nhập.");
                continue;
            }

            // Nếu người dùng không nhập và không bắt buộc thì bỏ qua các bước sau
            if (val.isEmpty()) {
                continue;
            }

            // 2. Kiểm tra theo kiểu dữ liệu
            KieuDuLieuCustomField kieu = KieuDuLieuCustomField.tuMa(t.getKieuDuLieu());
            if (kieu == KieuDuLieuCustomField.SO) {
                try {
                    new BigDecimal(val.replace(",", ""));
                } catch (Exception e) {
                    errors.put(ma, "Trường '" + t.getNhanHien() + "' phải là số hợp lệ.");
                }
            } else if (kieu == KieuDuLieuCustomField.NGAY) {
                try {
                    LocalDate.parse(val);
                } catch (DateTimeParseException e) {
                    errors.put(ma, "Trường '" + t.getNhanHien() + "' phải là ngày hợp lệ (định dạng YYYY-MM-DD).");
                }
            } else if (kieu == KieuDuLieuCustomField.DANH_SACH_CHON) {
                List<String> options = t.getDanhSachLuaChon();
                if (options != null && !options.isEmpty() && !options.contains(val)) {
                    errors.put(ma, "Giá trị '" + val + "' không nằm trong danh sách lựa chọn của '" + t.getNhanHien() + "'.");
                }
            }
        }
        return errors;
    }

    /**
     * Xác thực và lưu giá trị các trường tuỳ chỉnh khi người dùng submit form tạo hoặc sửa.
     */
    public boolean luuGiaTriBieuMau(String loaiDoiTuong, long doiTuongId, Map<String, String> rawInput) {
        Map<String, String> errors = validateGiaTriBieuMau(loaiDoiTuong, rawInput);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(errors.values().iterator().next());
        }

        // Lọc lấy map giá trị sạch (bỏ tiền tố ttc_ nếu có)
        List<TruongTuyChinh> dsTruong = layDanhSachChoBieuMau(loaiDoiTuong);
        Map<String, String> cleanMap = new HashMap<>();
        for (TruongTuyChinh t : dsTruong) {
            String ma = t.getMaTruong();
            String val = rawInput.get(ma);
            if (val == null) {
                val = rawInput.get("ttc_" + ma);
            }
            if (val != null) {
                cleanMap.put(ma, val.trim());
            }
        }

        return dao.luuNhieuGiaTri(chuanHoaLoaiDoiTuong(loaiDoiTuong), doiTuongId, cleanMap);
    }

    // =========================================================================
    // AC3: HỖ TRỢ BỘ LỌC (FILTER)
    // =========================================================================

    /**
     * Lấy danh sách trường tuỳ chỉnh được phép hiển thị trên thanh bộ lọc danh sách.
     */
    public List<TruongTuyChinh> layDanhSachChoBoLoc(String loaiDoiTuong) {
        List<TruongTuyChinh> tatCa = layDanhSachTheoDoiTuong(loaiDoiTuong, true);
        List<TruongTuyChinh> ketQua = new ArrayList<>();
        for (TruongTuyChinh t : tatCa) {
            if (t.isHienThiBoDac()) {
                ketQua.add(t);
            }
        }
        return ketQua;
    }

    /**
     * Kiểm tra xem một bản ghi (có tập giá trị custom fields) có thoả mãn các tiêu chí lọc hay không.
     */
    public boolean kiemTraKhopBoLoc(String loaiDoiTuong, Map<String, String> giaTriBanGhi, Map<String, String> filterParams) {
        if (filterParams == null || filterParams.isEmpty()) {
            return true;
        }

        List<TruongTuyChinh> dsTruongBoLoc = layDanhSachChoBoLoc(loaiDoiTuong);
        for (TruongTuyChinh t : dsTruongBoLoc) {
            String ma = t.getMaTruong();
            String valRecord = (giaTriBanGhi != null && giaTriBanGhi.containsKey(ma)) ? giaTriBanGhi.get(ma) : "";

            KieuDuLieuCustomField kieu = KieuDuLieuCustomField.tuMa(t.getKieuDuLieu());
            if (kieu == KieuDuLieuCustomField.VAN_BAN) {
                String filterVal = filterParams.get("ttcf_" + ma);
                if (filterVal == null) filterVal = filterParams.get(ma);
                if (filterVal != null && !filterVal.trim().isEmpty()) {
                    if (!valRecord.toLowerCase().contains(filterVal.trim().toLowerCase())) {
                        return false;
                    }
                }
            } else if (kieu == KieuDuLieuCustomField.SO) {
                String filterVal = filterParams.get("ttcf_" + ma);
                if (filterVal == null) filterVal = filterParams.get(ma);
                if (filterVal != null && !filterVal.trim().isEmpty()) {
                    try {
                        BigDecimal fNum = new BigDecimal(filterVal.trim());
                        BigDecimal rNum = new BigDecimal(valRecord.trim());
                        if (fNum.compareTo(rNum) != 0) {
                            return false;
                        }
                    } catch (Exception e) {
                        return false;
                    }
                }
            } else if (kieu == KieuDuLieuCustomField.NGAY) {
                String tuNgay = filterParams.get("ttcf_" + ma + "_tu");
                String denNgay = filterParams.get("ttcf_" + ma + "_den");
                if (tuNgay != null && !tuNgay.trim().isEmpty()) {
                    try {
                        LocalDate rDate = LocalDate.parse(valRecord.trim());
                        LocalDate fDate = LocalDate.parse(tuNgay.trim());
                        if (rDate.isBefore(fDate)) return false;
                    } catch (Exception e) {
                        return false;
                    }
                }
                if (denNgay != null && !denNgay.trim().isEmpty()) {
                    try {
                        LocalDate rDate = LocalDate.parse(valRecord.trim());
                        LocalDate fDate = LocalDate.parse(denNgay.trim());
                        if (rDate.isAfter(fDate)) return false;
                    } catch (Exception e) {
                        return false;
                    }
                }
            } else if (kieu == KieuDuLieuCustomField.DANH_SACH_CHON) {
                String filterVal = filterParams.get("ttcf_" + ma);
                if (filterVal == null) filterVal = filterParams.get(ma);
                if (filterVal != null && !filterVal.trim().isEmpty()) {
                    if (!valRecord.equalsIgnoreCase(filterVal.trim())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // =========================================================================
    // AC3: HỖ TRỢ XUẤT EXCEL / CSV (EXCEL EXPORT)
    // =========================================================================

    /**
     * Lấy danh sách trường tuỳ chỉnh được cấu hình để xuất ra file Excel.
     */
    public List<TruongTuyChinh> layDanhSachChoExcel(String loaiDoiTuong) {
        List<TruongTuyChinh> tatCa = layDanhSachTheoDoiTuong(loaiDoiTuong, true);
        List<TruongTuyChinh> ketQua = new ArrayList<>();
        for (TruongTuyChinh t : tatCa) {
            if (t.isHienThiExcel()) {
                ketQua.add(t);
            }
        }
        return ketQua;
    }

    /**
     * Lấy toàn bộ giá trị trường tuỳ chỉnh cho danh sách ID để phục vụ xuất Excel theo lô.
     */
    public Map<Long, Map<String, String>> layTatCaGiaTriTheoDanhSach(String loaiDoiTuong, List<Long> dsDoiTuongId) {
        return dao.layTatCaGiaTriTheoDanhSach(chuanHoaLoaiDoiTuong(loaiDoiTuong), dsDoiTuongId);
    }

    /**
     * Lấy danh sách bản ghi thực tế từ cơ sở dữ liệu (khach_hang hoặc co_hoi) phục vụ xuất Excel.
     */
    public List<Map<String, Object>> layDanhSachThucTeChoXuatExcel(String loaiDoiTuong) {
        return dao.layDanhSachThucTeChoXuatExcel(chuanHoaLoaiDoiTuong(loaiDoiTuong));
    }

    /**
     * Xác định tiêu đề hiển thị cho các cột trường tuỳ chỉnh khi xuất Excel/CSV.
     * Nếu có từ 2 trường trở lên trùng tên hiển thị (ten_truong / nhanHien), tự động gán thêm [ma_truong]
     * để tránh tạo ra các cột mơ hồ giống hệt nhau (ví dụ: "Phân khúc khách hàng [phan_khuc_kh]").
     * Nếu không trùng thì giữ nguyên tên hiển thị chuẩn.
     */
    public Map<String, String> xacDinhTieuDeCotCustomFields(List<TruongTuyChinh> dsTruong, List<String> cotChuanHeader) {
        Map<String, String> headerMap = new LinkedHashMap<>();
        if (dsTruong == null || dsTruong.isEmpty()) {
            return headerMap;
        }

        // Đếm tần suất xuất hiện của mỗi tên hiển thị
        Map<String, Integer> demNhan = new HashMap<>();
        for (TruongTuyChinh t : dsTruong) {
            String nhan = (t.getNhanHien() != null && !t.getNhanHien().isBlank())
                    ? t.getNhanHien().trim() : t.getMaTruong();
            demNhan.put(nhan, demNhan.getOrDefault(nhan, 0) + 1);
        }

        Set<String> cotChuanSet = new HashSet<>();
        if (cotChuanHeader != null) {
            for (String h : cotChuanHeader) {
                if (h != null) cotChuanSet.add(h.trim());
            }
        }

        for (TruongTuyChinh t : dsTruong) {
            String nhan = (t.getNhanHien() != null && !t.getNhanHien().isBlank())
                    ? t.getNhanHien().trim() : t.getMaTruong();
            // Nếu trùng tên với trường tuỳ chỉnh khác hoặc trùng với cột chuẩn hệ thống, thêm [ma_truong]
            if (demNhan.getOrDefault(nhan, 0) > 1 || cotChuanSet.contains(nhan)) {
                headerMap.put(t.getMaTruong(), nhan + " [" + t.getMaTruong() + "]");
            } else {
                headerMap.put(t.getMaTruong(), nhan);
            }
        }

        return headerMap;
    }

    /**
     * Xuất dữ liệu bảng có gắn kèm các cột trường tuỳ chỉnh sang file CSV UTF-8 mở được ngay trên Excel.
     */
    public String xuatDuLieuExcelCSV(String loaiDoiTuong,
                                     List<String> cotChuanHeader,
                                     List<String> cotChuanKeys,
                                     List<Map<String, Object>> danhSachBanGhi,
                                     Map<Long, Map<String, String>> giaTriCustomFields) {
        StringBuilder csv = new StringBuilder();
        // BOM UTF-8 để Microsoft Excel hiển thị đúng tiếng Việt không bị lỗi font
        csv.append("\uFEFF");

        List<TruongTuyChinh> dsTruongExcel = layDanhSachChoExcel(loaiDoiTuong);
        Map<String, String> customHeaders = xacDinhTieuDeCotCustomFields(dsTruongExcel, cotChuanHeader);

        // 1. Dòng tiêu đề
        boolean first = true;
        for (String h : cotChuanHeader) {
            if (!first) csv.append(",");
            csv.append(escapeCsv(h));
            first = false;
        }
        for (TruongTuyChinh t : dsTruongExcel) {
            csv.append(",").append(escapeCsv(customHeaders.get(t.getMaTruong())));
        }
        csv.append("\n");

        // 2. Dòng dữ liệu
        if (danhSachBanGhi != null) {
            for (Map<String, Object> record : danhSachBanGhi) {
                Long id = null;
                Object idObj = record.get("id");
                if (idObj instanceof Number) {
                    id = ((Number) idObj).longValue();
                } else if (idObj != null) {
                    try {
                        id = Long.parseLong(idObj.toString());
                    } catch (Exception ignored) {}
                }

                first = true;
                for (String key : cotChuanKeys) {
                    if (!first) csv.append(",");
                    Object val = record.get(key);
                    csv.append(escapeCsv(val != null ? val.toString() : ""));
                    first = false;
                }

                // Nạp giá trị trường tuỳ chỉnh
                Map<String, String> customVals = (id != null && giaTriCustomFields != null)
                        ? giaTriCustomFields.get(id) : null;

                for (TruongTuyChinh t : dsTruongExcel) {
                    String v = (customVals != null) ? customVals.get(t.getMaTruong()) : "";
                    csv.append(",").append(escapeCsv(v != null ? v : ""));
                }
                csv.append("\n");
            }
        }

        return csv.toString();
    }

    /**
     * Xuất dữ liệu bảng có gắn kèm các cột trường tuỳ chỉnh sang file Excel (.xlsx) chuẩn Apache POI.
     */
    public byte[] xuatDuLieuExcelXLSX(String loaiDoiTuong,
                                      List<String> cotChuanHeader,
                                      List<String> cotChuanKeys,
                                      List<Map<String, Object>> danhSachBanGhi,
                                      Map<Long, Map<String, String>> giaTriCustomFields) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            String sheetName = "CO_HOI".equalsIgnoreCase(loaiDoiTuong) ? "Cơ hội" : "Khách hàng";
            Sheet sheet = workbook.createSheet(sheetName);

            // Style cho hàng tiêu đề
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            List<TruongTuyChinh> dsTruongExcel = layDanhSachChoExcel(loaiDoiTuong);
            Map<String, String> customHeaders = xacDinhTieuDeCotCustomFields(dsTruongExcel, cotChuanHeader);

            // 1. Dòng tiêu đề
            Row headerRow = sheet.createRow(0);
            int colIdx = 0;
            for (String h : cotChuanHeader) {
                Cell cell = headerRow.createCell(colIdx++);
                cell.setCellValue(h);
                cell.setCellStyle(headerStyle);
            }
            for (TruongTuyChinh t : dsTruongExcel) {
                Cell cell = headerRow.createCell(colIdx++);
                cell.setCellValue(customHeaders.get(t.getMaTruong()));
                cell.setCellStyle(headerStyle);
            }

            // 2. Dòng dữ liệu
            int rowIdx = 1;
            if (danhSachBanGhi != null) {
                for (Map<String, Object> record : danhSachBanGhi) {
                    Row row = sheet.createRow(rowIdx++);
                    colIdx = 0;

                    Long id = null;
                    Object idObj = record.get("id");
                    if (idObj instanceof Number) {
                        id = ((Number) idObj).longValue();
                    } else if (idObj != null) {
                        try {
                            id = Long.parseLong(idObj.toString());
                        } catch (Exception ignored) {}
                    }

                    for (String key : cotChuanKeys) {
                        Cell cell = row.createCell(colIdx++);
                        Object val = record.get(key);
                        cell.setCellValue(val != null ? val.toString() : "");
                    }

                    Map<String, String> customVals = (id != null && giaTriCustomFields != null)
                            ? giaTriCustomFields.get(id) : null;

                    for (TruongTuyChinh t : dsTruongExcel) {
                        Cell cell = row.createCell(colIdx++);
                        String v = (customVals != null) ? customVals.get(t.getMaTruong()) : "";
                        cell.setCellValue(v != null ? v : "");
                    }
                }
            }

            // Auto-size các cột để file Excel dễ nhìn
            for (int i = 0; i < colIdx; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi tạo file Excel .xlsx: " + e.getMessage(), e);
        }
    }

    // --- Helper methods ---

    public String chuanHoaLoaiDoiTuong(String loaiDoiTuong) {
        if (loaiDoiTuong == null || loaiDoiTuong.isBlank()) {
            return "KHACH_HANG";
        }
        String upper = loaiDoiTuong.trim().toUpperCase();
        if ("CO_HOI".equals(upper)) {
            return "CO_HOI";
        }
        if ("KHACH_HANG".equals(upper)) {
            return "KHACH_HANG";
        }
        throw new IllegalArgumentException("Loại đối tượng không hợp lệ: " + loaiDoiTuong + ". Chỉ chấp nhận KHACH_HANG hoặc CO_HOI.");
    }

    private String buildMetadataJson(boolean hienThiBoDac, boolean hienThiExcel) {
        return "{\"hienThiBoDac\":" + hienThiBoDac + ",\"hienThiExcel\":" + hienThiExcel + "}";
    }

    private String escapeCsv(String value) {
        if (value == null) return "\"\"";
        String s = value;
        // Chống Formula Injection khi mở CSV bằng Excel: escape các ký tự khởi đầu công thức =, +, -, @
        if (s.startsWith("=") || s.startsWith("+") || s.startsWith("-") || s.startsWith("@")) {
            s = "'" + s;
        }
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
