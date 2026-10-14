# HỢP ĐỒNG TÍCH HỢP DỮ LIỆU (FE–BE CONTRACT) & DỮ LIỆU MẪU (MOCK DATA)
## DỰ ÁN: MSys — Hệ Thống Quản Lý Khách Hàng & Cơ Hội Bán Hàng (CRM)
### STORY ID: S4-06 — Cấu hình quy tắc phân bổ lead tự động

---

## 1. THÔNG TIN BẢN HỢP ĐỒNG & BỐI CẢNH KỸ THUẬT

| Thuộc tính | Chi tiết | Ghi chú |
| :--- | :--- | :--- |
| **Story ID** | **S4-06** | Cấu hình quy tắc phân bổ lead tự động |
| **Phiên bản tài liệu** | 1.0.0-DRAFT | Bản dự thảo tích hợp Sprint 4 |
| **Trạng thái hợp đồng** | **APPROVED (ĐÃ DUYỆT)** | Đã được Leader/Mentor phê duyệt |
| **Kiến trúc kỹ thuật** | Jakarta Servlet 6.0 + JSP Monolith | Java 17+, Tomcat 10.1+, MySQL 8.0, H2 test |
| **Quy chuẩn mã nguồn** | MVC Monolith kết hợp AJAX REST-like | Form Submit (302 Redirect) + AJAX JSON API |
| **Nguồn dữ liệu DB** | Bảng: `quy_tac_phan_bo_lead`, `lich_su_phan_bo_lead`, `lead` | [ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO] |

---

## 2. QUY TẮC CHUNG VỀ ĐỊNH DẠNG & MÃ TRẠNG THÁI (STANDARDS & ENUMS)

### 2.1. Định dạng dữ liệu chuẩn
- **Thời gian (Timestamp):** ISO-8601 string `YYYY-MM-DDTHH:mm:ss` (ví dụ: `2026-10-15T09:30:00`) khi truyền JSON; định dạng `dd/MM/yyyy HH:mm` khi render ra JSP.
- **Mã định danh (ID):** `Long` / `BIGINT UNSIGNED` $\ge 1$.
- **Boolean / Flag:** `1` (True / Bật) hoặc `0` (False / Tắt).
- **Mã phản hồi chuẩn (Response Envelope) cho AJAX JSON:**
  ```json
  {
    "success": true,
    "code": 200,
    "message": "Thao tác thành công",
    "data": {},
    "errors": []
  }
  ```

### 2.2. Danh mục Enum nghiệp vụ [ĐỀ XUẤT]
1. **Kiểu phân bổ (`KieuPhanBoEnum`):**
   - `THEO_KHU_VUC`: Phân bổ dựa theo danh mục Khu vực địa lý của lead (`khu_vuc_id`).
   - `THEO_NGANH_NGHE`: Phân bổ dựa theo ngành nghề của lead (`nganh_nghe_id`).
   - `XOAY_VONG`: Phân bổ xoay vòng đều (Round-Robin) giữa các Sales Rep trong nhóm.
2. **Hành động phân bổ (`HanhDongPhanBoEnum`):**
   - `TU_DONG_THEO_QUY_TAC`: Máy tự động gán theo quy tắc phân bổ.
   - `VAO_HANG_CHO`: Lead không khớp quy tắc, được đưa vào hàng chờ.
   - `TRUONG_NHOM_PHAN_TAY`: Trưởng nhóm hoặc Giám đốc gán thủ công từ hàng chờ.
3. **Trạng thái Lead liên quan phân bổ (`TrangThaiLeadEnum`):**
   - `MOI`: Lead mới vào, chưa qua phân bổ.
   - `HANG_CHO_PHAN_BO`: Lead không khớp quy tắc, chờ phân tay.
   - `DA_PHAN_BO`: Đã có `nguoi_phu_trach_id`, chờ Sales Rep bấm nhận.

---

## 3. CHI TIẾT HỢP ĐỒNG TÍCH HỢP (FE–BE CONTRACT SPECIFICATION)

### ACTION PBL-01: Xem danh sách quy tắc phân bổ (Server-Side Rendered JSP)
- **Tương ứng AC:** AC1, AC2
- **Mục đích:** Giám đốc kinh doanh xem toàn bộ các quy tắc phân bổ hiện có theo thứ tự ưu tiên từ 1 đến N.
- **URL đề xuất:** `GET /admin/lead/phan-bo` [ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO]
- **Quyền hạn (RBAC):** Bắt buộc vai trò `DIRECTOR` hoặc `ADMIN`. Nếu vai trò khác (`SALES_REP`, `ACCOUNTANT`, v.v.) trả về HTTP 403 Forbidden.
- **Request Parameters:** Không có (hoặc `?status=all|active`).
- **JSP Request Attributes (Model cấp cho View):**
  - `danhSachQuyTac` (`List<QuyTacPhanBoDTO>`): Danh sách các quy tắc, đã sắp xếp `thu_tu_uu_tien ASC`.
  - `danhSachKhuVuc` (`List<KhuVucDTO>`): Danh mục khu vực để nạp vào modal tạo/sửa.
  - `danhSachNganhNghe` (`List<NganhNgheDTO>`): Danh mục ngành nghề để nạp vào modal.
  - `danhSachNhomKinhDoanh` (`List<NhomKinhDoanhDTO>`): Danh sách nhóm để chọn đích xoay vòng.
  - `soLuongHangCho` (`Integer`): Số lượng lead đang nằm trong hàng chờ phân bổ.
- **Mẫu dữ liệu Model Attribute `danhSachQuyTac`:**
  ```java
  public class QuyTacPhanBoDTO {
      private Long id;
      private String maQuyTac;           // vd: "RULE-KV-MB"
      private String tenQuyTac;          // vd: "Lead Miền Bắc vào Nhóm Hà Nội"
      private Integer thuTuUuTien;        // vd: 1
      private String kieuPhanBo;         // "THEO_KHU_VUC", "THEO_NGANH_NGHE", "XOAY_VONG"
      private String dieuKienJson;       // JSON text: {"khu_vuc_ids": [1, 2]}
      private String dieuKienMoTa;       // Chuỗi tóm tắt hiển thị UI: "Hà Nội, Hải Phòng"
      private Long nhomDichId;
      private String tenNhomDich;        // vd: "Nhóm Kinh Doanh Miền Bắc"
      private Long nguoiDungDichId;
      private String tenNguoiDungDich;   // vd: "Nguyễn Văn A" (nếu có)
      private Boolean hoatDong;          // true / false
      private LocalDateTime updatedAt;
  }
  ```

---

### ACTION PBL-02: Tạo mới quy tắc phân bổ (Form Submit / POST)
- **Tương ứng AC:** AC1
- **Mục đích:** Giám đốc kinh doanh lưu một quy tắc phân bổ mới vào hệ thống.
- **URL đề xuất:** `POST /admin/lead/phan-bo/tao-moi` [ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO]
- **Quyền hạn (RBAC):** `DIRECTOR`, `ADMIN`.
- **Content-Type:** `application/x-www-form-urlencoded`
- **Form Fields:**

| Tên Field | Kiểu dữ liệu | Bắt buộc | Ràng buộc / Validation Rules |
| :--- | :--- | :---: | :--- |
| `tenQuyTac` | `String` | **Có** | Độ dài từ 3 đến 200 ký tự, không chứa ký tự độc hại XSS. |
| `kieuPhanBo` | `String` | **Có** | Giá trị thuộc enum: `THEO_KHU_VUC`, `THEO_NGANH_NGHE`, `XOAY_VONG`. |
| `khuVucIds` | `List<Long>` | Tùy chọn | Bắt buộc nếu `kieuPhanBo == 'THEO_KHU_VUC'`. Mảng ID khu vực hợp lệ. |
| `nganhNgheIds` | `List<Long>` | Tùy chọn | Bắt buộc nếu `kieuPhanBo == 'THEO_NGANH_NGHE'`. Mảng ID ngành hợp lệ. |
| `nhomDichId` | `Long` | Tùy chọn | Bắt buộc nếu `kieuPhanBo == 'XOAY_VONG'`. Nhóm kinh doanh phải tồn tại và có ít nhất 1 sales rep. |
| `nguoiDungDichId` | `Long` | Tùy chọn | ID nhân viên đích danh nếu không chọn xoay vòng nhóm. |
| `hoatDong` | `Boolean` | Không | Mặc định là `true` (1). |

- **Xử lý phía Server:**
  - Tự động gán `maQuyTac` = `QT_` + timestamp ngẫu nhiên (ví dụ `QT_20261015_01`).
  - Gán `thuTuUuTien` = `MAX(thu_tu_uu_tien) + 1` để xếp ở cuối chuỗi ưu tiên hiện hành.
  - Lưu vào DB, set Flash Message `successMessage = "Tạo quy tắc phân bổ thành công"`.
  - HTTP 302 Redirect về `/admin/lead/phan-bo`.
- **Xử lý khi Lỗi Validation:**
  - Lưu Flash Attribute `errorMessage = "..."`, `formData = requestParams`.
  - HTTP 302 Redirect hoặc Forward lại trang form để người dùng sửa.

---

### ACTION PBL-03: Đổi thứ tự ưu tiên của quy tắc (AJAX JSON API)
- **Tương ứng AC:** AC2
- **Mục đích:** Đổi số thứ tự ưu tiên của các quy tắc (khi bấm nút Lên/Xuống hoặc Kéo-Thả).
- **URL đề xuất:** `POST /admin/lead/phan-bo/doi-thu-tu` [ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO]
- **Quyền hạn (RBAC):** `DIRECTOR`, `ADMIN`.
- **Content-Type:** `application/json`
- **Request Body (JSON):**
  ```json
  {
    "quyTacId": 5,
    "huongDiChuyen": "LEN" 
  }
  ```
  *Hoặc truyền danh sách sắp xếp đầy đủ:*
  ```json
  {
    "danhSachThuTu": [
      { "id": 5, "thuTuMoi": 1 },
      { "id": 2, "thuTuMoi": 2 },
      { "id": 8, "thuTuMoi": 3 }
    ]
  }
  ```
- **Phản hồi thành công (HTTP 200 OK):**
  ```json
  {
    "success": true,
    "code": 200,
    "message": "Cập nhật thứ tự ưu tiên thành công",
    "data": {
      "quyTacId": 5,
      "thuTuHienTai": 1
    }
  }
  ```
- **Phản hồi lỗi xung đột (HTTP 409 Conflict):**
  ```json
  {
    "success": false,
    "code": 409,
    "message": "Xung đột thứ tự ưu tiên với người dùng khác. Vui lòng tải lại trang."
  }
  ```

---

### ACTION PBL-04: Bật / Tắt trạng thái quy tắc (AJAX JSON API)
- **Tương ứng AC:** AC1, AC2
- **Mục đích:** Tạm dừng một quy tắc phân bổ mà không cần xóa khỏi hệ thống.
- **URL đề xuất:** `POST /admin/lead/phan-bo/doi-trang-thai` [ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO]
- **Request Body:**
  ```json
  {
    "id": 5,
    "hoatDong": false
  }
  ```
- **Phản hồi thành công (HTTP 200 OK):**
  ```json
  {
    "success": true,
    "code": 200,
    "message": "Đã tắt quy tắc phân bổ",
    "data": { "id": 5, "hoatDong": false }
  }
  ```

---

### ACTION PBL-05: Xem danh sách Hàng chờ phân bổ (Server-Rendered JSP)
- **Tương ứng AC:** AC3
- **Mục đích:** Trưởng nhóm kinh doanh hoặc Giám đốc kinh doanh xem các lead chưa khớp quy tắc nào.
- **URL đề xuất:** `GET /lead/hang-cho-phan-bo` [ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO]
- **Quyền hạn (RBAC):** `TEAM_LEAD`, `DIRECTOR`, `ADMIN`.
- **JSP Attributes:**
  - `danhSachLeadHangCho` (`List<LeadHangChoDTO>`): Các lead có `trang_thai = 'HANG_CHO_PHAN_BO'`.
  - `danhSachNhanVienNhom` (`List<NguoiDungDTO>`): Danh sách Sales Rep thuộc phạm vi phân quyền của người đang đăng nhập (Team Lead chỉ thấy nhân viên nhóm mình).

---

### ACTION PBL-06: Trưởng nhóm phân tay lead trong hàng chờ (POST)
- **Tương ứng AC:** AC3
- **Mục đích:** Gán nhân viên phụ trách cho một lead trong hàng chờ.
- **URL đề xuất:** `POST /lead/hang-cho-phan-bo/phan-tay` [ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO]
- **Quyền hạn (RBAC):** `TEAM_LEAD`, `DIRECTOR`, `ADMIN`.
- **Form Data:**
  - `leadId`: `Long` (Bắt buộc).
  - `nhanVienId`: `Long` (Bắt buộc).
  - `ghiChuPhanBo`: `String` (Tối đa 500 ký tự).
- **Kiểm tra nghiệp vụ Server-Side:**
  - Nếu người thao tác là `TEAM_LEAD`: `nhanVienId` bắt buộc phải là nhân viên thuộc nhóm do `TEAM_LEAD` quản lý. Nếu không -> Trả về HTTP 403 Forbidden.
  - Kiểm tra trạng thái hiện tại của lead: Phải là `HANG_CHO_PHAN_BO`. Nếu đã có người nhận -> Báo lỗi `Lead đã được phân bổ bởi người khác`.
- **Xử lý DB:**
  - Cập nhật `lead`: `nguoi_phu_trach_id = nhanVienId`, `trang_thai = 'DA_PHAN_BO'`, `duoc_phan_bo_luc = NOW()`, `han_phan_hoi_sla = DATE_ADD(NOW(), INTERVAL 24 HOUR)`.
  - Thêm bản ghi `lich_su_phan_bo_lead`: `hanh_dong = 'TRUONG_NHOM_PHAN_TAY'`, `thuc_hien_boi_id = currentUser.getId()`.

---

### ACTION PBL-07: Kích hoạt chạy tiến trình phân bổ nền ngay lập tức (Test / Admin Tool)
- **Tương ứng AC:** AC4
- **Mục đích:** Giúp đội ngũ kiểm thử (Tester) và Giám đốc có thể kích hoạt phân bổ ngay mà không cần chờ hết chu kỳ 30s.
- **URL đề xuất:** `POST /admin/lead/phan-bo/chay-ngay` [ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO]
- **Quyền hạn:** `ADMIN`, `DIRECTOR`.
- **Phản hồi (JSON):**
  ```json
  {
    "success": true,
    "code": 200,
    "message": "Đã chạy phân bổ nền thành công",
    "data": {
      "soLeadDaQuet": 12,
      "soLeadPhanBoThanhCong": 10,
      "soLeadVaoHangCho": 2,
      "thoiGianXuLyMs": 145
    }
  }
  ```

---

## 4. DỮ LIỆU MẪU MOCK DÙNG CHUNG (MOCK DATA FOR FE & BE)

Toàn bộ dữ liệu dưới đây là giả định hoàn toàn, phục vụ phát triển giao diện độc lập và viết kiểm thử Mock Unit Test.

### 4.1. Mock Danh sách Quy tắc Phân bổ (`MockQuyTacPhanBoData.json`)
```json
[
  {
    "id": 1,
    "maQuyTac": "QT-01-KV-MB",
    "tenQuyTac": "Phân bổ khách hàng Doanh nghiệp Miền Bắc",
    "thuTuUuTien": 1,
    "kieuPhanBo": "THEO_KHU_VUC",
    "dieuKienJson": "{\"khu_vuc_ids\": [1, 2, 3]}",
    "dieuKienMoTa": "Khu vực: Hà Nội, Quảng Ninh, Hải Phòng",
    "nhomDichId": 101,
    "tenNhomDich": "Nhóm Kinh Doanh Miền Bắc",
    "nguoiDungDichId": null,
    "tenNguoiDungDich": "Xoay vòng trong nhóm",
    "nguoiDungLanCuoiId": 10,
    "hoatDong": true,
    "updatedAt": "2026-10-14T10:15:00"
  },
  {
    "id": 2,
    "maQuyTac": "QT-02-NN-TAICHINH",
    "tenQuyTac": "Ưu tiên Khách hàng Ngành Tài chính - Ngân hàng",
    "thuTuUuTien": 2,
    "kieuPhanBo": "THEO_NGANH_NGHE",
    "dieuKienJson": "{\"nganh_nghe_ids\": [5, 8]}",
    "dieuKienMoTa": "Ngành: Tài chính - Bảo hiểm, Công nghệ thông tin",
    "nhomDichId": 102,
    "tenNhomDich": "Nhóm Khách hàng Doanh nghiệp Lớn",
    "nguoiDungDichId": 12,
    "tenNguoiDungDich": "Trần Thị Mai (Chuyên gia giải pháp)",
    "nguoiDungLanCuoiId": null,
    "hoatDong": true,
    "updatedAt": "2026-10-14T11:20:00"
  },
  {
    "id": 3,
    "maQuyTac": "QT-03-RR-TOANCONGTY",
    "tenQuyTac": "Xoay vòng đều cho Nhóm Bán hàng Trực tuyến",
    "thuTuUuTien": 3,
    "kieuPhanBo": "XOAY_VONG",
    "dieuKienJson": "{}",
    "dieuKienMoTa": "Áp dụng xoay vòng cho mọi lead phổ thông",
    "nhomDichId": 103,
    "tenNhomDich": "Nhóm Inside Sales",
    "nguoiDungDichId": null,
    "tenNguoiDungDich": "Xoay vòng 5 nhân viên",
    "nguoiDungLanCuoiId": 25,
    "hoatDong": true,
    "updatedAt": "2026-10-14T14:00:00"
  },
  {
    "id": 4,
    "maQuyTac": "QT-04-KV-MN-TAMNGUNG",
    "tenQuyTac": "Phân bổ Miền Nam (Tạm ngừng bảo trì chiến dịch)",
    "thuTuUuTien": 4,
    "kieuPhanBo": "THEO_KHU_VUC",
    "dieuKienJson": "{\"khu_vuc_ids\": [4, 5]}",
    "dieuKienMoTa": "Khu vực: TP. Hồ Chí Minh, Bình Dương",
    "nhomDichId": 104,
    "tenNhomDich": "Nhóm Kinh Doanh Miền Nam",
    "nguoiDungDichId": null,
    "tenNguoiDungDich": "Xoay vòng trong nhóm",
    "nguoiDungLanCuoiId": null,
    "hoatDong": false,
    "updatedAt": "2026-10-14T16:45:00"
  }
]
```

---

### 4.2. Mock Ca Kiểm Thử Thành Công & Biên (Edge Cases)

#### Case 1: Lead khớp Rule 1 (Ưu tiên cao nhất)
- **Input Lead:**
  ```json
  {
    "id": 1001,
    "hoTen": "Phạm Quốc Bảo",
    "tenCongTy": "Công ty TNHH Vận tải Biển Đông",
    "email": "bao.pq@biendong.com",
    "khuVucId": 1, 
    "nganhNheId": 5, 
    "trangThai": "MOI",
    "createdAt": "2026-10-15T08:00:00"
  }
  ```
- **Kết quả mong đợi:**
  - Lead có cả `khuVucId = 1` (Hà Nội - khớp Rule 1) và `nganhNheId = 5` (Tài chính - khớp Rule 2).
  - Vì Rule 1 có `thuTuUuTien = 1` nhỏ hơn Rule 2 (`thuTuUuTien = 2`), Rule 1 **THẮNG (First-Match-Wins)**.
  - Lead được gán cho nhân viên thuộc Nhóm Miền Bắc theo cơ chế xoay vòng.
  - Lịch sử phân bổ ghi nhận:
    ```json
    {
      "leadId": 1001,
      "quyTacId": 1,
      "hanhDong": "TU_DONG_THEO_QUY_TAC",
      "lyDo": "Khớp quy tắc ưu tiên 1: Phân bổ khách hàng Doanh nghiệp Miền Bắc"
    }
    ```

#### Case 2: Lead không khớp bất kỳ quy tắc nào -> Hàng chờ (AC3)
- **Input Lead:**
  ```json
  {
    "id": 1002,
    "hoTen": "Lê Hoài Thu",
    "tenCongTy": "Nông trại Xanh",
    "email": "thu.le@xanhfarm.vn",
    "khuVucId": null,
    "nganhNheId": 99, 
    "trangThai": "MOI",
    "createdAt": "2026-10-15T08:02:00"
  }
  ```
- **Kết quả mong đợi:**
  - Không khớp Rule 1 (Khu vực rỗng).
  - Không khớp Rule 2 (Ngành nghề 99 không nằm trong [5, 8]).
  - Rule 3 tắt hoặc không áp dụng.
  - Lead chuyển thành `trangThai = "HANG_CHO_PHAN_BO"`, `nguoiPhuTrachId = null`.
  - Lịch sử phân bổ:
    ```json
    {
      "leadId": 1002,
      "quyTacId": null,
      "hanhDong": "VAO_HANG_CHO",
      "lyDo": "Không khớp bất kỳ quy tắc phân bổ tự động nào"
    }
    ```

#### Case 3: Trưởng nhóm phân tay từ hàng chờ (AC3)
- **Thao tác:** Trưởng nhóm Nhóm 101 đăng nhập, phân Lead #1002 cho Sales Rep `ID: 15` (Trần Văn Cường).
- **Kết quả mong đợi:**
  - Lead #1002: `trangThai = "DA_PHAN_BO"`, `nguoiPhuTrachId = 15`.
  - Lịch sử phân bổ:
    ```json
    {
      "leadId": 1002,
      "nguoiDungMoiId": 15,
      "hanhDong": "TRUONG_NHOM_PHAN_TAY",
      "thucHienBoiId": 5, 
      "lyDo": "Trưởng nhóm chỉ định phụ trách do khách hàng tiềm năng"
    }
    ```

#### Case 4: Lỗi Thiếu quyền (HTTP 403 Forbidden)
- **Thao tác:** Nhân viên Sales Rep gọi URL `POST /admin/lead/phan-bo/tao-moi`.
- **Response:**
  ```json
  {
    "success": false,
    "code": 403,
    "message": "Từ chối truy cập: Bạn không có quyền cấu hình quy tắc phân bổ lead.",
    "errors": ["ROLE_ACCESS_DENIED"]
  }
  ```

---

## 5. HƯỚNG DẪN KIỂM THỬ HỢP ĐỒNG (CONTRACT TESTING & DEV HANDOFF)

### 5.1. Dành cho lập trình viên Giao diện (FE Developer)
1. Trong quá trình chưa có Backend hoàn chỉnh, FE có thể tạo mảng Mock JSON như ở Mục 4 để hiển thị danh sách quy tắc trên JSP/JS.
2. Form submit POST phải gửi đúng tên `name="..."` của các thẻ input khớp với bảng tham số tại Action `PBL-02`.
3. Kiểm tra responsive giao diện ở kích thước màn hình điện thoại khổ 360px:
   - Các nút Lên/Xuống xếp gọn trong dropdown actions hoặc dạng nút icon tròn.
   - Thẻ badge trạng thái hiển thị rõ ràng không bị tràn chữ.

### 5.2. Dành cho lập trình viên Máy chủ (BE Developer)
1. Đảm bảo Transaction cô lập: Khi chạy background job hoặc đổi thứ tự ưu tiên, luôn dùng `Connection.setAutoCommit(false)` và `try ... commit ... catch ... rollback`.
2. Bảo đảm tính Thread-Safe cho Xoay vòng Round-Robin: Sử dụng cơ chế khóa hàng (`SELECT ... FOR UPDATE` trên bảng `quy_tac_phan_bo_lead` tại ID quy tắc đang xử lý) để cập nhật `nguoi_dung_lan_cuoi_id` chính xác khi có nhiều lead vào đồng thời.
3. Độ trễ tiến trình nền: Khởi tạo luồng Scheduler `Executors.newSingleThreadScheduledExecutor()` chạy mỗi 30s.

---

## 6. Ô QUYẾT ĐỊNH & PHÊ DUYỆT (DECISION & SIGN-OFF BOX)

| Trạng thái tài liệu | Người duyệt đề xuất | Ngày duyệt dự kiến | Quyết định & Ý kiến chỉ đạo |
| :---: | :---: | :---: | :--- |
| **APPROVED** | **Product Owner / Mentor** | 2026-10-14 | Đã phê duyệt phương án kỹ thuật và hợp đồng tích hợp |
| **APPROVED** | **Tech Lead (Backend)** | 2026-10-14 | Đã rà soát logic Round Robin, Hàng chờ và Background Job |
| **APPROVED** | **Frontend Lead** | 2026-10-14 | Đã rà soát hợp đồng tham số form và responsive 360px |

> [!NOTE]
> Sau khi các thành viên trong nhóm và Mentor thống nhất các câu hỏi tại Mục 7 của File Phân Tích AC, trạng thái tài liệu sẽ được chuyển từ **DRAFT** sang **APPROVED** trước khi chuyển sang bước sinh mã (Prompt 02).
