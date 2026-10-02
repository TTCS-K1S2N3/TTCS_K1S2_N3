package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.model.NguoiDung;

/**
 * Service quản lý hồ sơ cá nhân của người dùng.
 */
public class HoSoService {

    private final NguoiDungDAO nguoiDungDAO;

    public HoSoService() {
        this(new NguoiDungDAO());
    }

    public HoSoService(NguoiDungDAO nguoiDungDAO) {
        this.nguoiDungDAO = nguoiDungDAO;
    }

    public NguoiDung layThongTinHoSo(int nguoiDungId) {
        return nguoiDungDAO.timTheoId(nguoiDungId);
    }

    public boolean capNhatHoSo(int nguoiDungId, String hoTen, String soDienThoai, String chuKyEmail) {
        if (hoTen == null || hoTen.isBlank()) {
            return false;
        }
        return nguoiDungDAO.capNhatHoSo(nguoiDungId, hoTen.trim(), soDienThoai, chuKyEmail);
    }
}
