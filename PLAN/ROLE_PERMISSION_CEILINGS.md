# MA TRẬN TRẦN QUYỀN HỆ THỐNG CRM BÁN HÀNG (SHEET 2. USER ROLES)

Tài liệu chuẩn hóa trần quyền tối đa (Ceilings) cho 7 vai trò trên 10 module cơ sở dữ liệu.
Đây là **TRẦN CỐ ĐỊNH BẤT BIẾN** của vai trò theo đề bài và thiết kế bảo mật, độc lập với cấu hình phân quyền hiện hành trong bảng `vai_tro_module`.

---

## 1. Bảng Ma Trận Trần Quyền 70 Cặp (7 Vai trò x 10 Module)

Ký hiệu:
- `–` = NONE (Không có quyền)
- `R` = READ (Chỉ xem)
- `W` = WRITE (Xem, Tạo, Sửa)
- `F` = FULL (Xem, Tạo, Sửa, Xóa)
- `*` = Giới hạn phạm vi dữ liệu theo quyền sở hữu (Cá nhân / Nhóm)

| Module Canonical | SALES_REP | MARKETING | CUST_SUCCESS | ACCOUNTANT | TEAM_LEAD | DIRECTOR | ADMIN | Ghi chú phạm vi dữ liệu & Nghiệp vụ |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| **DANH_MUC** | R (N/A) | R (N/A) | R (N/A) | R (N/A) | R (N/A) | F (TOAN_BO) | F (TOAN_BO) | Dùng chung toàn cty; chỉ Director/Admin quản lý |
| **KHACH_HANG** | W* (CA_NHAN) | W (TOAN_BO) | W* (CA_NHAN) | R (TOAN_BO) | F* (NHOM) | F (TOAN_BO) | F (TOAN_BO) | Phân cấp sở hữu: Cá nhân / Nhóm / Toàn bộ |
| **LEAD** | W* (CA_NHAN) | F (TOAN_BO) | – | – | F* (NHOM) | F (TOAN_BO) | F (TOAN_BO) | Marketing toàn quyền Lead; CSKH/Accountant NONE |
| **CO_HOI** | W* (CA_NHAN) | R (TOAN_BO) | R* (CA_NHAN) | R (TOAN_BO) | F* (NHOM) | F (TOAN_BO) | F (TOAN_BO) | Cơ hội kinh doanh & Pipeline |
| **HOAT_DONG** | W* (CA_NHAN) | W (TOAN_BO) | W* (CA_NHAN) | – | F* (NHOM) | F (TOAN_BO) | F (TOAN_BO) | Lịch làm việc & Hoạt động tương tác |
| **BAO_GIA_HOP_DONG** | W* (CA_NHAN) | – | R* (CA_NHAN) | W (TOAN_BO) | W* (NHOM) | F (TOAN_BO) | F (TOAN_BO) | Báo giá & Hợp đồng (Kế toán WRITE; TL tối đa W) |
| **KPI** | R* (CA_NHAN) | – | – | R (TOAN_BO) | W* (NHOM) | F (TOAN_BO) | F (TOAN_BO) | Chỉ tiêu & KPI bán hàng |
| **BAO_CAO** | R* (CA_NHAN) | R* (CA_NHAN) | R* (CA_NHAN) | R (TOAN_BO) | R* (NHOM) | F (TOAN_BO) | F (TOAN_BO) | Sheet 2: Báo cáo của Marketing chỉ xem R* CA_NHAN |
| **TU_DONG_HOA** | R (N/A) | W (TOAN_BO) | R (N/A) | – | R (NHOM/NA) | F (TOAN_BO) | F (TOAN_BO) | Tự động hóa & quy tắc gửi thông báo |
| **NGUOI_DUNG_NHAT_KY** | – | – | – | – | – | R (TOAN_BO) | F (TOAN_BO) | Director chỉ xem nhật ký (READ); Admin FULL |

---

## 2. Quy Tắc Phạm Vi Dữ Liệu (Data Scope)

1. **SALES_REP**:
   - Module có `*`: tối đa `CA_NHAN`.
   - DANH_MUC, TU_DONG_HOA: Không áp dụng quyền sở hữu cá nhân (`NULL` / `N/A`).
   - NGUOI_DUNG_NHAT_KY: `NONE`.
2. **TEAM_LEAD**:
   - Module có `*`: tối đa `NHOM`.
   - Trần `FULL` tại KHACH_HANG, LEAD, CO_HOI, HOAT_DONG **không đồng nghĩa với TOAN_BO**, chỉ thao tác trong phạm vi nhóm.
   - NGUOI_DUNG_NHAT_KY: `NONE`.
3. **DIRECTOR**:
   - Mọi module kinh doanh: `FULL`, phạm vi `TOAN_BO`.
   - **NGUOI_DUNG_NHAT_KY**: Trần chỉ là `READ`, phạm vi `TOAN_BO`. Không được cấp WRITE/FULL.
   - Ngoại lệ nghiệp vụ: Giá vốn và biên lợi nhuận của sản phẩm chỉ Giám đốc kinh doanh được xem.
4. **ADMIN**:
   - Toàn quyền `FULL`, phạm vi `TOAN_BO` trên toàn bộ 10 module.
   - Hàng Admin trên giao diện và backend là bất biến, không thể sửa đổi hoặc hạ quyền.
5. **CUST_SUCCESS**:
   - KHACH_HANG, CO_HOI, HOAT_DONG, BAO_GIA_HOP_DONG, BAO_CAO có `*` tối đa `CA_NHAN`.
   - LEAD, KPI, NGUOI_DUNG_NHAT_KY: `NONE`.
6. **MARKETING**:
   - LEAD: `FULL`, `TOAN_BO`.
   - KHACH_HANG, HOAT_DONG, TU_DONG_HOA: `WRITE`, `TOAN_BO`.
   - CO_HOI: `READ`, `TOAN_BO`.
   - **BAO_CAO**: Theo Sheet 2 là `R*`, chỉ xem phạm vi `CA_NHAN` (không được cấp `TOAN_BO`).
   - BAO_GIA_HOP_DONG, KPI, NGUOI_DUNG_NHAT_KY: `NONE`.
7. **ACCOUNTANT**:
   - KHACH_HANG, CO_HOI, KPI, BAO_CAO: `READ`, `TOAN_BO`.
   - BAO_GIA_HOP_DONG: `WRITE`, `TOAN_BO`.
   - DANH_MUC: `READ`, `NULL`.
   - LEAD, HOAT_DONG, TU_DONG_HOA, NGUOI_DUNG_NHAT_KY: `NONE`.

---

## 3. Chính Sách Thực Thi An Toàn (Security Enforcement)

1. **Tách biệt Trần quyền và Cấu hình DB**:
   - CSDL `vai_tro_module` phản ánh cấu hình hiện hành được Admin cấp.
   - Nếu Admin hạ quyền (ví dụ Sales Rep từ WRITE xuống READ), hệ thống chỉ cho phép READ.
   - Nếu dữ liệu DB cũ hoặc request bị giả mạo vượt trần (ví dụ Sales Rep thành FULL), hệ thống runtime tự động ép về mức trần tối đa.
2. **Server-Side Validation**:
   - POST `/nguoi-dung/phan-quyen-vai-tro` kiểm tra nghiêm ngặt từng cell. Bất kỳ tham số nào vượt trần mức quyền hoặc phạm vi đều bị từ chối ngay lập tức.
   - Chỉ người dùng có vai trò `ADMIN` mới được POST. `DIRECTOR` chỉ xem read-only (GET). Các vai trò khác bị chặn HTTP 403.
