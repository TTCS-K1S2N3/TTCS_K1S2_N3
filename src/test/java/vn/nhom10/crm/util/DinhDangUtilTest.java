package vn.nhom10.crm.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử DinhDangUtil - Định dạng số điện thoại Việt Nam (Story S2-02 AC3)")
class DinhDangUtilTest {

    @ParameterizedTest(name = "Số điện thoại di động hợp lệ: {0}")
    @ValueSource(strings = {
            "0901234567",
            "0912345678",
            "0923456789",
            "0934567890",
            "0945678901",
            "0966789012",
            "0977890123",
            "0988901234",
            "0999012345",
            "0812345678",
            "0823456789",
            "0834567890",
            "0845678901",
            "0856789012",
            "0867890123",
            "0878901234",
            "0889012345",
            "0890123456",
            "0701234567",
            "0762345678",
            "0773456789",
            "0784567890",
            "0795678901",
            "0321234567",
            "0332345678",
            "0343456789",
            "0354567890",
            "0365678901",
            "0376789012",
            "0387890123",
            "0398901234",
            "0521234567",
            "0552345678",
            "0563456789",
            "0584567890",
            "0595678901"
    })
    @DisplayName("Chấp nhận các đầu số di động 10 số chính thống của các nhà mạng Việt Nam")
    void testSoDienThoaiDiDongHopLe(String soDienThoai) {
        assertTrue(DinhDangUtil.laSoDienThoaiVietNamHopLe(soDienThoai),
                "Số điện thoại phải hợp lệ: " + soDienThoai);
    }

    @ParameterizedTest(name = "Định dạng quốc tế và định dạng có khoảng trắng/dấu chấm: {0}")
    @ValueSource(strings = {
            "+84901234567",
            "+84 901 234 567",
            "+84.901.234.567",
            "84901234567",
            "090 123 4567",
            "090.123.4567",
            "090-123-4567"
    })
    @DisplayName("Chấp nhận định dạng quốc tế (+84/84) và định dạng có phân tách ký tự")
    void testDinhDangQuocTeVaPhanTachHopLe(String soDienThoai) {
        assertTrue(DinhDangUtil.laSoDienThoaiVietNamHopLe(soDienThoai),
                "Số điện thoại định dạng mở rộng phải hợp lệ: " + soDienThoai);
    }

    @Test
    @DisplayName("Chấp nhận số điện thoại cố định 11 số (đầu số 02x)")
    void testSoDienThoaiCoDinhHopLe() {
        assertTrue(DinhDangUtil.laSoDienThoaiVietNamHopLe("02412345678")); // Hà Nội
        assertTrue(DinhDangUtil.laSoDienThoaiVietNamHopLe("02812345678")); // TP.HCM
    }

    @ParameterizedTest(name = "Số điện thoại không hợp lệ: {0}")
    @ValueSource(strings = {
            "",
            "   ",
            "abcdefghij",
            "09012abcde",
            "1234567890",      // Không bắt đầu bằng 0, +84, 84
            "0123456789",      // Đầu số 01 đã chuyển đổi, không còn hợp lệ
            "0412345678",      // Đầu số 04 cũ của Hà Nội không còn tồn tại
            "090123456",       // 9 số (thiếu số)
            "09012345678",     // 11 số nhưng đầu 09 (thừa số)
            "090@123#456",     // Chứa ký tự đặc biệt lạ
            "0000000000",      // Đầu số 00 không tồn tại
            "+14155552671"     // Số điện thoại Mỹ
    })
    @DisplayName("Từ chối các số điện thoại sai định dạng, thiếu số, thừa số hoặc chứa chữ cái")
    void testSoDienThoaiKhongHopLe(String soDienThoai) {
        assertFalse(DinhDangUtil.laSoDienThoaiVietNamHopLe(soDienThoai),
                "Số điện thoại không được coi là hợp lệ: " + soDienThoai);
    }

    @Test
    @DisplayName("Từ chối khi số điện thoại là null")
    void testSoDienThoaiNull() {
        assertFalse(DinhDangUtil.laSoDienThoaiVietNamHopLe(null));
    }

    @Test
    @DisplayName("Chuẩn hóa số điện thoại về dạng 10 số nội địa bắt đầu bằng 0")
    void testChuanHoaSoDienThoai() {
        assertEquals("0901234567", DinhDangUtil.chuanHoaSoDienThoai("+84901234567"));
        assertEquals("0901234567", DinhDangUtil.chuanHoaSoDienThoai("84901234567"));
        assertEquals("0901234567", DinhDangUtil.chuanHoaSoDienThoai("090.123.4567"));
        assertEquals("0901234567", DinhDangUtil.chuanHoaSoDienThoai("090-123-4567"));
        assertEquals("0901234567", DinhDangUtil.chuanHoaSoDienThoai("090 123 4567"));
        assertEquals("0901234567", DinhDangUtil.chuanHoaSoDienThoai("+84 901 234 567"));
        assertEquals("", DinhDangUtil.chuanHoaSoDienThoai(null));
    }
}
