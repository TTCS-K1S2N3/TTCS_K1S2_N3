# TÀI LIỆU PHÂN TÍCH USER STORY & TIÊU CHÍ CHẤP NHẬN (AC)
## DỰ ÁN: MSys — Hệ Thống Quản Lý Khách Hàng & Cơ Hội Bán Hàng (CRM)
### STORY ID: S4-06 — Cấu hình quy tắc phân bổ lead tự động

---

## 1. THÔNG TIN CHUNG VỀ USER STORY & NGUỒN TRÍCH XUẤT

| Thuộc tính | Chi tiết trích xuất từ Workbook gốc | Ghi chú phân loại |
| :--- | :--- | :--- |
| **Tệp tài liệu nguồn** | `PLAN/HỆ THỐNG QUẢN LÝ KHÁCH HÀNG.xlsx` | [BACKLOG GỐC] |
| **Sheet nguồn** | `4. Product Backlog` (Dòng 47), `2. User Roles` (Dòng 6 & 18), `3. Epics` (Dòng 7), `5. Sprint Plan` (Dòng 7) | [BACKLOG GỐC] |
| **Story ID** | **S4-06** | [BACKLOG GỐC] |
| **Tên Story** | Cấu hình quy tắc phân bổ lead tự động | [BACKLOG GỐC] |
| **Thuộc Epic** | **EP-04**: Lead & Phân bổ (Tổng 48 points, 10 stories) | [BACKLOG GỐC] |
| **Sprint thực hiện** | **Sprint 4** (Chủ đề: *Lead & phân bổ*) | [BACKLOG GỐC] |
| **Story Points** | **8 points** (Story quy mô lớn, tương đương ~32 giờ công đội ngũ) | [BACKLOG GỐC] |
| **Mức độ ưu tiên** | **Must** (Bắt buộc hoàn thành trong Sprint 4) | [BACKLOG GỐC] |
| **Trạng thái Backlog** | **Chưa bắt đầu** (Tính đến thời điểm phân tích) | [BACKLOG GỐC] |
| **Vai trò người dùng** | **Giám đốc kinh doanh** (`Director`) | [BACKLOG GỐC] |
| **User Story nguyên văn** | *"Là Giám đốc kinh doanh, tôi muốn cấu hình quy tắc phân bổ lead tự động, để lead tới tay người phụ trách trong vài phút thay vì chờ họp giao ban."* | [BACKLOG GỐC] |

---

## 2. MA TRẬN PHÂN QUYỀN VÀ PHẠM VI DỮ LIỆU (RBAC & DATA SCOPE)

Trích xuất từ Sheet `2. User Roles` đối với Module **Lead & phân bổ**:

| Vai trò | Ký hiệu phân quyền | Ý nghĩa nghiệp vụ theo quy chế hệ thống | Tác vụ trên S4-06 |
| :--- | :---: | :--- | :--- |
| **Giám đốc kinh doanh (Director)** | **F** | Toàn quyền (Full access) trên toàn bộ hệ thống kinh doanh, xem toàn bộ dữ liệu, cấu hình quy tắc toàn công ty. | **Chủ thể chính:** Tạo, sửa, xóa, sắp xếp thứ tự ưu tiên, bật/tắt quy tắc phân bổ, kích hoạt chạy phân bổ ngay. |
| **Quản trị hệ thống (Admin)** | **F** | Toàn quyền kỹ thuật và vận hành ứng dụng. | Toàn quyền cấu hình, hỗ trợ bảo trì quy tắc và cấu hình tiến trình chạy nền. |
| **Trưởng nhóm kinh doanh (Team Lead)** | **F** | Toàn quyền trong phạm vi nhóm quản lý; phân bổ lại lead và cơ hội. | **Người thụ hưởng AC3:** Xem danh sách lead rơi vào hàng chờ (*Hàng chờ phân bổ*), phân bổ thủ công (phân tay) cho nhân viên thuộc nhóm mình. |
| **Nhân viên Marketing (Marketing)** | **F** | Người tạo ra lead từ form/chiến dịch; theo dõi luồng phân bổ lead. | Xem danh sách quy tắc để biết lead thu thập sẽ được chuyển đi đâu; không có quyền sửa quy tắc phân bổ. |
| **Nhân viên kinh doanh (Sales Rep)** | **W\*** | Ghi và xử lý trong phạm vi khách hàng/lead được giao (*Data Scope* cá nhân). | **Đối tượng nhận lead:** Không có quyền truy cập trang cấu hình phân bổ (HTTP 403 Forbidden). Nhận lead được gán tự động. |
| **Chăm sóc khách hàng (Cust. Success)** | **–** | Không có quyền truy cập module Lead & phân bổ. | Bị chặn hoàn toàn (HTTP 403 Forbidden). |
| **Kế toán (Accountant)** | **–** | Không có quyền truy cập module Lead & phân bổ. | Bị chặn hoàn toàn (HTTP 403 Forbidden). |

> [!IMPORTANT]
> **Ràng buộc Server-Side Phân Quyền:** Việc phân quyền cấu hình quy tắc phân bổ phải được kiểm soát chặt chẽ ở tầng Servlet Filter và tầng Service. Tuyệt đối không chỉ ẩn nút trên giao diện JSP. Chỉ người dùng có vai trò `DIRECTOR` hoặc `ADMIN` mới được gọi các action thay đổi quy tắc (`POST /admin/lead/phan-bo/*`).

---

## 3. CÁC DEPENDENCY VÀ MỐI QUAN HỆ TRONG EP-04

1. **S4-01 (Thu thập lead từ website - Form nhúng):** Khi khách hàng gửi biểu mẫu trên website, lead được tạo ở trạng thái `MOI` (Mới) kèm nguồn và các trường thông tin (Khu vực, Ngành nghề, v.v.). Đây là đầu vào kích hoạt luồng phân bổ của S4-06.
2. **S4-02 (Nhập lead thủ công / Excel):** Các lead được import từ hội thảo, sự kiện sau khi lưu vào DB cũng cần được phân bổ tự động nếu chưa gán người phụ trách.
3. **S4-05 (Chấm điểm lead):** Lead sau khi vào hệ thống được tính điểm và phân loại (*Nóng/Ấm/Lạnh*). S4-06 có thể sử dụng thêm thông tin phân loại điểm để hỗ trợ điều kiện lọc phân bổ nếu cấu hình mở rộng.
4. **S4-07 (Nhận / từ chối lead và SLA phản hồi):** Sau khi S4-06 gán lead cho một Sales Rep, S4-07 kích hoạt bộ đếm SLA (Service Level Agreement) để nhân viên bấm "Nhận lead" hoặc "Từ chối".

---

## 4. PHÂN TÍCH SÂU TỪNG TIÊU CHÍ CHẤP NHẬN (ACCEPTANCE CRITERIA)

### AC1: Phân bổ theo khu vực, theo ngành nghề, hoặc xoay vòng đều trong nhóm

#### 1. Người thao tác & Mục tiêu
- **Người thao tác:** Giám đốc kinh doanh (`Director`) hoặc `Admin`.
- **Nơi thực hiện:** Màn hình Quản lý Quy tắc Phân bổ Lead (`/admin/lead/phan-bo` hoặc `/lead/phan-bo`).
- **Mục đích:** Thiết lập logic nghiệp vụ để tự động chỉ định Nhân viên kinh doanh (`nguoi_phu_trach_id`) hoặc Nhóm kinh doanh (`nhom_phan_bo_id`) nhận lead dựa trên đặc tính địa lý, lĩnh vực kinh doanh, hoặc chia đều khối lượng công việc.
- **Đầu ra quan sát được:** Bản ghi quy tắc phân bổ mới hiển thị trên bảng danh sách, hiển thị rõ kiểu phân bổ (`KHU_VUC`, `NGANH_NGHE`, `XOAY_VONG`), điều kiện áp dụng và đối tượng thụ hưởng.

#### 2. Điều kiện tiên quyết & Dữ liệu đầu vào
- **Điều kiện trước:** Hệ thống đã có danh mục Khu vực địa lý (`khu_vuc_dia_ly`), danh mục Ngành nghề (`nganh_nghe`), và danh sách Nhóm kinh doanh (`nhom_kinh_doanh`) cùng các thành viên Sales Rep đang hoạt động.
- **Dữ liệu bắt buộc:**
  - Tên quy tắc: chuỗi văn bản không rỗng (tối đa 200 ký tự).
  - Kiểu phân bổ: Chọn 1 trong 3 kiểu:
    1. `THEO_KHU_VUC`: Chọn 1 hoặc nhiều khu vực địa lý (ví dụ: Miền Bắc, Miền Nam, Hà Nội, TP.HCM).
    2. `THEO_NGANH_NGHE`: Chọn 1 hoặc nhiều ngành nghề kinh doanh (ví dụ: CNTT, Bất động sản, Sản xuất).
    3. `XOAY_VONG` (Round-Robin): Chọn Nhóm kinh doanh đích (`nhom_dich_id`). Hệ thống sẽ tự động xoay vòng giữa các thành viên có vai trò `SALES_REP` đang hoạt động (`active = 1`) trong nhóm đó.
  - Đích đến phân bổ:
    - Nếu phân bổ đích danh: Chỉ định `nguoi_dung_dich_id`.
    - Nếu phân bổ theo nhóm/xoay vòng: Chỉ định `nhom_dich_id`.
  - Trạng thái hoạt động: `hoat_dong` (Mặc định: Bật - `true`).

#### 3. Luồng xử lý chính (Happy Path)
1. Giám đốc kinh doanh mở màn hình `/admin/lead/phan-bo`, bấm nút **"+ Thêm quy tắc phân bổ"**.
2. Form modal hoặc trang tạo mới xuất hiện:
   - Nhập tên quy tắc (ví dụ: *"Lead Miền Bắc phân cho Nhóm Hà Nội"*).
   - Chọn kiểu: `THEO_KHU_VUC`.
   - Chọn danh sách khu vực: `[ID: 1 - Miền Bắc, ID: 2 - Hà Nội]`.
   - Chọn đích đến: Nhóm kinh doanh *"Kinh doanh Miền Bắc"* (Cơ chế xoay vòng nội bộ nhóm).
3. Bấm **"Lưu quy tắc"**.
4. Request `POST` gửi tới Servlet điều khiển.
5. Server kiểm tra quyền `Director`/`Admin`, validate dữ liệu.
6. DB lưu bản ghi vào bảng `quy_tac_phan_bo_lead`, tự động gán `thu_tu_uu_tien` lớn nhất tiếp theo.
7. Trả về thông báo thành công và chuyển hướng về danh sách quy tắc với dữ liệu mới.

#### 4. Các trường hợp lỗi & Ngoại lệ
- **Tên quy tắc trùng lặp hoặc để trống:** Báo lỗi vi phạm hợp lệ dữ liệu.
- **Không chọn tiêu chí điều kiện:** Khi chọn `THEO_KHU_VUC` mà không tick chọn khu vực nào, hoặc `THEO_NGANH_NGHE` mà danh sách ngành trống -> Báo lỗi `Vui lòng chọn ít nhất một điều kiện khu vực/ngành nghề`.
- **Nhóm kinh doanh đích không có nhân viên Sales Rep nào đang hoạt động:** Khi chọn cơ chế xoay vòng trong nhóm, nếu nhóm rỗng hoặc toàn bộ nhân viên bị khóa/chuyển nhóm -> Cảnh báo ngay trên form: `Nhóm được chọn hiện không có nhân viên bán hàng hoạt động`.
- **Lỗi thao tác đồng thời:** Hai Director cùng tạo quy tắc và tranh chấp thứ tự ưu tiên -> Tầng DAO xử lý trong transaction tự tăng thứ tự an toàn.

#### 5. Thiết kế Giao diện (FE UX)
- Bảng danh sách trực quan: Cột STT/Thứ tự ưu tiên, Tên quy tắc, Kiểu phân bổ (có Badge màu phân biệt: Xanh lá cho Khu vực, Cam cho Ngành nghề, Xanh dương cho Xoay vòng), Điều kiện tóm tắt, Đối tượng nhận, Nút Bật/Tắt (Toggle Switch), Nút Sửa/Xóa.
- Modal tạo/sửa: Form chia block rõ ràng; khi đổi "Kiểu phân bổ" từ dropdown, các trường điều kiện bên dưới tự động hiển thị tương ứng (Dynamic form inputs).
- Responsive: Tương thích hoàn toàn trên mobile khổ 360px (dạng danh sách thẻ card vuốt dọc).

#### 6. Xử lý Phía Máy Chủ (BE Architecture)
- Tầng Service: `QuyTacPhanBoService` kiểm tra nghiệp vụ, parse điều kiện JSON (`dieu_kien_json`), lưu vết người tạo `created_by`.
- Thuật toán Xoay vòng Round-Robin:
  - Bảng `quy_tac_phan_bo_lead` lưu trường `nguoi_dung_lan_cuoi_id`.
  - Khi phân bổ một lead mới khớp quy tắc xoay vòng của nhóm $X$:
    1. Lấy danh sách ID các nhân viên kinh doanh (`active = 1`, `role = 'SALES_REP'`) thuộc nhóm $X$, sắp xếp tăng dần theo ID: $[u_1, u_2, ..., u_n]$.
    2. Tìm vị trí của `nguoi_dung_lan_cuoi_id`. Nhân viên tiếp theo sẽ là người ở vị trí kế tiếp (vòng lại đầu mảng nếu đã tới cuối).
    3. Cập nhật `nguoi_dung_lan_cuoi_id` bằng ID người vừa được gán trong cùng một database transaction với thao tác gán lead để đảm bảo tính nhất quán (thread-safe, tránh race condition).

---

### AC2: Nhiều quy tắc xếp theo thứ tự ưu tiên, quy tắc đầu tiên khớp sẽ thắng

#### 1. Người thao tác & Mục tiêu
- **Người thao tác:** Giám đốc kinh doanh (`Director`).
- **Nơi thực hiện:** Bảng danh sách quy tắc phân bổ (`/admin/lead/phan-bo`).
- **Mục đích:** Sắp xếp độ ưu tiên từ cao xuống thấp (Thứ tự $1, 2, 3...$). Khi có lead mới vào, hệ thống duyệt lần lượt từ trên xuống dưới, quy tắc đầu tiên thỏa mãn điều kiện sẽ được áp dụng ngay lập tức, các quy tắc bên dưới sẽ bị bỏ qua (First-Match-Wins).
- **Đầu ra quan sát được:** Giao diện cho phép đổi thứ tự dễ dàng (nút bấm Mũi tên Lên/Xuống hoặc Kéo - Thả Drag & Drop). Số thứ tự ưu tiên được cập nhật tức thì trên giao diện và DB.

#### 2. Điều kiện tiên quyết & Dữ liệu đầu vào
- Đã có từ 2 quy tắc phân bổ trở lên trong hệ thống.
- Chỉ các quy tắc đang có trạng thái `hoat_dong = 1` mới được tham gia vào chuỗi đánh giá (hoặc quy tắc tắt thì bị bỏ qua).

#### 3. Luồng xử lý chính (Happy Path)
1. Giám đốc kinh doanh muốn đưa quy tắc *"Lead VIP Ngành Tài chính"* lên vị trí ưu tiên số 1 (thay vì vị trí số 3).
2. Người dùng bấm nút "Lên" trên dòng quy tắc (hoặc kéo hàng lên vị trí đầu tiên).
3. FE gửi request `POST /admin/lead/phan-bo/doi-thu-tu` kèm `id` và vị trí mới (hoặc danh sách các cặp `[id, thu_tu]`).
4. BE nhận request trong một Transaction:
   - Tạm thời gán thứ tự để tránh vi phạm ràng buộc Unique Key `uk_qtpb_tt`.
   - Cập nhật lại toàn bộ chỉ số `thu_tu_uu_tien` của các bản ghi liên quan (1, 2, 3...).
   - Commit transaction.
5. Khi một Lead mới được tạo (ví dụ: Khách hàng ở Hà Nội, ngành Tài chính):
   - Engine phân bổ truy vấn: `SELECT * FROM quy_tac_phan_bo_lead WHERE hoat_dong = 1 ORDER BY thu_tu_uu_tien ASC`.
   - Duyệt Rule 1: Khớp ngành Tài chính -> Phân bổ ngay cho Chuyên viên phụ trách ngành Tài chính.
   - Dừng duyệt chuỗi (ngắt vòng lặp), Rule 2 (Khu vực Hà Nội) không được xét đến nữa.

#### 4. Các trường hợp lỗi & Ngoại lệ
- **Xung đột chỉ số thứ tự:** Nếu cập nhật trực tiếp gây trùng giá trị `thu_tu_uu_tien`, DB sẽ ném lỗi `Duplicate entry`. Giải pháp: BE dùng kỹ thuật offset tạm thời hoặc thực thi cập nhật mảng trong batch transaction.
- **Quy tắc bị Tắt (`hoat_dong = 0`):** Engine phân bổ phải bỏ qua, không so khớp lead với các quy tắc đang tắt, tự động chuyển xuống quy tắc kế tiếp.

#### 5. Thiết kế Giao diện (FE UX)
- Cột "Ưu tiên" hiển thị số thứ tự to rõ: Badge `Top 1`, `Top 2`, `Top 3`...
- Cung cấp hai nút điều hướng tiện lợi: `▲ Lên` và `▼ Xuống` (tự động disable nút Lên ở dòng đầu và nút Xuống ở dòng cuối).

---

### AC3: Lead không khớp quy tắc nào rơi vào hàng chờ để trưởng nhóm phân tay

#### 1. Người thao tác & Mục tiêu
- **Người thao tác:**
  - *Hệ thống tự động:* Gán trạng thái Hàng chờ khi không khớp quy tắc.
  - *Trưởng nhóm kinh doanh (`Team Lead`) / Giám đốc kinh doanh (`Director`):* Thao tác trên màn hình Hàng chờ để chỉ định nhân viên phụ trách.
- **Nơi thực hiện:** Màn hình *"Hàng chờ phân bổ lead"* (`/lead/hang-cho-phan-bo`).
- **Mục đích:** Đảm bảo không có bất kỳ lead nào bị bỏ quên hoặc thất lạc khi thông tin khu vực/ngành nghề của khách hàng chưa rõ ràng hoặc không khớp với danh mục quy tắc hiện hành.
- **Đầu ra quan sát được:**
  - Lead có `trang_thai = 'HANG_CHO_PHAN_BO'` (hoặc `MOI` nhưng `nguoi_phu_trach_id IS NULL`).
  - Giao diện có tab hoặc bộ lọc riêng *"Lead chờ phân bổ"* kèm số lượng badge thông báo (ví dụ: `Hàng chờ (5)` màu đỏ).
  - Trưởng nhóm bấm nút "Phân bổ", chọn nhân viên trong popup modal -> Lead được gán và chuyển sang trạng thái đã phân bổ.

#### 2. Điều kiện tiên quyết & Dữ liệu đầu vào
- Lead đã qua quy trình chấm điểm và chạy qua toàn bộ danh sách quy tắc phân bổ đang hoạt động nhưng không có quy tắc nào thỏa mãn điều kiện.
- Trưởng nhóm khi phân tay bắt buộc chọn:
  - Nhân viên phụ trách (`nguoi_phu_trach_id`): Phải là thành viên thuộc nhóm do Trưởng nhóm đó quản lý (theo Data Scope).
  - Ghi chú/Lý do phân bổ (tùy chọn, tối đa 500 ký tự).

#### 3. Luồng xử lý chính (Happy Path)
1. **Luồng máy tự động:**
   - Lead #999 từ form web có `khu_vuc_id = NULL` (khách không nhập) và `nganh_nghe_id = 99` (Khác).
   - Engine chạy qua toàn bộ Rule 1..N: Không có rule nào khớp.
   - Engine cập nhật Lead: `trang_thai = 'HANG_CHO_PHAN_BO'`, `nguoi_phu_trach_id = NULL`.
   - Ghi nhật ký vào bảng `lich_su_phan_bo_lead`: `hanh_dong = 'VAO_HANG_CHO'`, `ly_do = 'Không khớp bất kỳ quy tắc phân bổ tự động nào'`.
2. **Luồng Trưởng nhóm phân tay:**
   - Trưởng nhóm đăng nhập vào hệ thống, thấy badge trên menu: `Lead & Phân bổ` -> `Hàng chờ (1)`.
   - Bấm vào hàng chờ, xem thông tin chi tiết của Lead #999 (Tên, SĐT, Email, Nhu cầu).
   - Bấm nút **"Phân bổ cho nhân viên"**.
   - Modal hiển thị dropdown danh sách Sales Rep thuộc nhóm của Trưởng nhóm.
   - Chọn nhân viên *Nguyễn Văn A*, bấm **"Xác nhận phân bổ"**.
   - Server kiểm tra: Trưởng nhóm có quyền trên nhóm của Nguyễn Văn A không? -> Hợp lệ.
   - Cập nhật Lead: `nguoi_phu_trach_id = [ID Nguyễn Văn A]`, `trang_thai = 'DA_PHAN_BO'`, `duoc_phan_bo_luc = NOW()`, thiết lập hạn SLA phản hồi (`han_phan_hoi_sla`).
   - Ghi nhật ký `lich_su_phan_bo_lead`: `hanh_dong = 'TRUONG_NHOM_PHAN_TAY'`, `thuc_hien_boi_id = [ID Trưởng nhóm]`.
   - Lead biến mất khỏi hàng chờ và chuyển vào danh sách quản lý của Nguyễn Văn A.

#### 4. Các trường hợp lỗi & Ngoại lệ
- **Trưởng nhóm cố tình gán nhân viên thuộc nhóm khác:** BE chặn lại với lỗi `403 Forbidden - Bạn chỉ được phân bổ lead cho nhân viên thuộc nhóm bạn quản lý`.
- **Nhân viên được chọn đã bị khóa tài khoản:** BE kiểm tra `trang_thai = 'HOAT_DONG'`, nếu đã bị khóa báo lỗi `Tài khoản nhân viên đã bị vô hiệu hóa, vui lòng chọn người khác`.
- **Lead trong hàng chờ đã được người khác phân bổ trước đó (Race condition):** Nếu 2 Trưởng nhóm hoặc Director cùng mở modal và bấm phân bổ -> BE kiểm tra trạng thái hiện tại của Lead bằng Optimistic Locking hoặc `SELECT FOR UPDATE`, nếu lead không còn ở hàng chờ thì báo `Lead này đã được phân bổ bởi người dùng khác`.

---

### AC4: Phân bổ chạy nền, hoàn tất trong vòng 5 phút kể từ khi lead vào

#### 1. Người thao tác & Mục tiêu
- **Người thao tác:** Hệ thống tự động (Background Job / Daemon Scheduler / Async Worker).
- **Nơi thực hiện:** Tiến trình ngầm phía máy chủ (`LeadAssignmentBackgroundWorker`).
- **Mục đích:** Không làm tắc nghẽn luồng gửi form của khách hàng trên website (Form submit phản hồi trong < 1s), đồng thời đảm bảo lead được tiếp nhận và phân bổ tới tay nhân viên kinh doanh trong thời gian tối đa 5 phút theo đúng cam kết Sprint Goal.
- **Đầu ra quan sát được:**
  - Lead vừa submit xong có thể thấy ngay trên DB hoặc danh sách quản trị.
  - Sau tối đa 5 phút (thực tế cấu hình chu kỳ job chạy mỗi 15-30 giây), lead đã có `nguoi_phu_trach_id` hoặc chuyển sang `HANG_CHO_PHAN_BO`.
  - Cột `duoc_phan_bo_luc` ghi nhận timestamp cách `created_at` không quá 5 phút ($T_{\text{assign}} - T_{\text{create}} \le 300\text{s}$).

#### 2. Điều kiện tiên quyết & Cơ chế kỹ thuật
- **Kiến trúc Jakarta Servlet Monolith:**
  - Khởi tạo tiến trình nền bằng `ScheduledExecutorService` bên trong lớp `ServletContextListener` (ví dụ: `BackgroundJobListener`).
  - Khởi động khi ứng dụng Web khởi động (`contextInitialized`) và hủy giải phóng an toàn khi shutdown (`contextDestroyed`).
  - Chu kỳ quét: Cấu hình mặc định mỗi **30 giây** quét một lần (`initialDelay = 5s, period = 30s`). Như vậy, thời gian chờ tối đa trung bình chỉ từ 30s đến 60s, hoàn toàn thỏa mãn ngưỡng cam kết 5 phút (300s).

#### 3. Thuật toán xử lý của Background Worker
1. Tiến trình thức giấc mỗi 30s.
2. Quét DB lấy các lead cần xử lý:
   ```sql
   SELECT id, ho_ten, email, so_dien_thoai, khu_vuc_id, nganh_nghe_id, nguon_lead_id, created_at 
   FROM lead 
   WHERE trang_thai = 'MOI' AND nguoi_phu_trach_id IS NULL
   ORDER BY created_at ASC 
   LIMIT 50;
   ```
3. Nạp danh sách các quy tắc phân bổ đang hoạt động đã được sắp xếp ưu tiên:
   ```sql
   SELECT * FROM quy_tac_phan_bo_lead WHERE hoat_dong = 1 ORDER BY thu_tu_uu_tien ASC;
   ```
4. Với từng lead trong danh sách 50 lead:
   - Duyệt qua từng quy tắc.
   - Nếu khớp: Xác định người/nhóm đích -> Thực hiện gán lead -> Ghi log lịch sử -> Đánh dấu thành công -> Sang lead tiếp theo.
   - Nếu duyệt hết tất cả quy tắc mà không khớp: Đưa lead vào hàng chờ (`trang_thai = 'HANG_CHO_PHAN_BO'`) -> Ghi log lịch sử -> Sang lead tiếp theo.
5. Cập nhật xong theo từng Transaction độc lập cho mỗi lead (để lỗi của 1 lead dị biệt không làm rollback 49 lead còn lại).

#### 4. Kế hoạch đo lường và Kiểm thử hiệu năng (Performance Test)
- **Kịch bản đo lường thực tế:**
  1. Tạo mock dataset gồm 100 lead được nạp đồng thời (mô phỏng sự kiện chiến dịch marketing gửi hàng loạt).
  2. Bắt đầu bấm giờ $T_0$.
  3. Kích hoạt Worker chạy nền.
  4. Sau khi kết thúc, chạy câu truy vấn SQL kiểm chứng:
     ```sql
     SELECT 
         COUNT(*) as total_leads,
         MAX(TIMESTAMPDIFF(SECOND, created_at, duoc_phan_bo_luc)) as max_latency_seconds,
         AVG(TIMESTAMPDIFF(SECOND, created_at, duoc_phan_bo_luc)) as avg_latency_seconds
     FROM lead
     WHERE id IN (...100 ids...);
     ```
  5. **Tiêu chí ĐẠT (PASS):** `max_latency_seconds` $\le 300$ giây (5 phút), `total_leads` có `nguoi_phu_trach_id IS NOT NULL` hoặc `trang_thai = 'HANG_CHO_PHAN_BO'` đạt 100%.

---

## 5. PHÂN ĐỊNH PHẠM VI (IN-SCOPE vs OUT-OF-SCOPE)

| Tiêu chí | TRONG PHẠM VI (In-Scope S4-06) | NGOÀI PHẠM VI (Out-of-Scope S4-06) |
| :--- | :--- | :--- |
| **Loại quy tắc** | 1. Phân theo Khu vực địa lý.<br>2. Phân theo Ngành nghề.<br>3. Phân xoay vòng đều (Round-Robin) trong nhóm. | Phân bổ bằng Machine Learning / AI dự đoán xác suất chốt; phân bổ theo tải công việc thực tế (Dynamic workload balancing) phức tạp. |
| **Chuỗi ưu tiên** | Sắp xếp thứ tự ưu tiên 1, 2, 3... Quy tắc đầu tiên khớp sẽ thắng. | Cây logic phân nhánh đa tầng lồng nhau phức tạp dạng flow-builder đồ họa. |
| **Không khớp quy tắc** | Tự động chuyển vào Hàng chờ (`HANG_CHO_PHAN_BO`) để Trưởng nhóm phân tay. | Tự động hủy lead hoặc từ chối nhận lead từ khách hàng. |
| **Tiến trình thực thi** | Background worker định kỳ chạy ngầm trong ứng dụng, xử lý trong $\le 5$ phút. | Sử dụng các message broker phân tán bên ngoài như Apache Kafka, RabbitMQ (không cần thiết và quá phức tạp với monolith Servlet). |
| **Phản hồi của Sales Rep** | Gán người phụ trách và ghi nhận mốc thời gian phân bổ. | Xử lý nút "Nhận lead / Từ chối lead" và đếm ngược cảnh báo SLA (đây là phạm vi của **S4-07**). |

---

## 6. BẢNG MAPPING TỔNG HỢP: AC → QUY TẮC → FE → BE → DB → TEST

| AC | Diễn giải quy tắc nghiệp vụ | Luồng FE | Luồng BE / DB dự kiến | Quyền / Data Scope | Phương pháp & Kịch bản Test | Câu hỏi / Điểm cần lưu ý |
| :---: | :--- | :--- | :--- | :--- | :--- | :--- |
| **AC1** | Hỗ trợ 3 kiểu cấu hình: Khu vực, Ngành nghề, Xoay vòng nội bộ nhóm. | Form modal thêm/sửa, dynamic dropdown theo kiểu phân bổ, badge hiển thị kiểu. | Service parse JSON điều kiện, lưu `quy_tac_phan_bo_lead`, quản lý con trỏ `nguoi_dung_lan_cuoi_id`. | `Director`, `Admin` toàn quyền `F`. | Tạo 3 rule cho 3 kiểu; kiểm tra gán đúng đối tượng; test xoay vòng A -> B -> C -> A. | Trạng thái nghỉ phép/vắng mặt của nhân viên có tính vào xoay vòng không? |
| **AC2** | Nhiều quy tắc, xếp theo thứ tự ưu tiên (1, 2, 3..), quy tắc đầu tiên khớp sẽ dừng duyệt. | Cột thứ tự ưu tiên, nút điều hướng Lên/Xuống hoặc kéo thả, phản hồi vị trí mới tức thì. | API cập nhật thứ tự trong transaction an toàn; Engine truy vấn `ORDER BY thu_tu_uu_tien ASC`. | `Director`, `Admin` toàn quyền `F`. | Tạo Rule 1 (Ngành nghề) và Rule 2 (Khu vực); lead thỏa mãn cả 2; kiểm chứng Lead được gán theo Rule 1. | Khi đổi thứ tự thì các lead đang trong hàng chờ có được quét lại không? |
| **AC3** | Lead không khớp bất kỳ quy tắc nào thì rơi vào Hàng chờ để Trưởng nhóm phân tay. | Tab/Màn hình "Hàng chờ phân bổ", badge số lượng màu đỏ, modal phân bổ cho nhân viên. | Engine cập nhật trạng thái `HANG_CHO_PHAN_BO`; API phân tay kiểm tra quyền Trưởng nhóm trên nhân viên được gán. | `Team Lead` phân tay trong nhóm mình; `Director` phân tay toàn công ty. | Tạo lead không có khu vực/ngành nghề; kiểm tra lead rơi vào hàng chờ; Trưởng nhóm đăng nhập phân bổ thành công. | Lead nằm trong hàng chờ bao lâu thì bị cảnh báo cho Giám đốc? |
| **AC4** | Tiến trình phân bổ chạy nền, hoàn tất gán lead trong vòng 5 phút kể từ khi lead vào. | Màn hình danh sách lead tự động refresh hoặc cập nhật trạng thái khi mở lại; có nút "Phân bổ ngay" cho Admin test. | `ScheduledExecutorService` kích hoạt mỗi 30s, quét batch 50 lead, cập nhật transaction từng bản ghi. | Chạy dưới quyền hệ thống (`SYSTEM_DAEMON`), log ghi rõ `hanh_dong = 'TU_DONG_THEO_QUY_TAC'`. | Bơm 100 lead giả lập; đo độ trễ $T_{\text{assign}} - T_{\text{create}}$; nghiệm thu độ trễ tối đa < 300 giây. | Có cần ghi nhận log lỗi riêng nếu worker gặp sự cố DB không? |

---

## 7. CÂU HỎI QUAN TRỌNG CẦN MENTOR / LEADER CHỐT [CẦN CHỐT]

Sau đây là 4 câu hỏi nghiệp vụ và kỹ thuật cốt lõi ảnh hưởng trực tiếp tới Contract dữ liệu và Database:

1. **[CẦN CHỐT 01 - Nghiệp vụ xoay vòng Round-Robin]:** Khi phân bổ xoay vòng trong một nhóm kinh doanh, nếu một nhân viên trong nhóm đang bị khóa tài khoản hoặc có quá nhiều lead chưa xử lý, thuật toán có tự động bỏ qua người này để gán cho người kế tiếp không?
   - *Phương án A (Khuyến nghị):* Chỉ lọc các nhân viên có `trang_thai = 'HOAT_DONG'` và `vai_tro = 'SALES_REP'` trong nhóm. Tạm thời chưa tính đến số lượng lead tồn đọng để giữ đúng phạm vi 8 point của S4-06.
   - *Phương án B:* Bổ sung kiểm tra hạn mức lead tối đa (Cap quota). Hệ quả: Tăng độ phức tạp thuật toán và vượt phạm vi AC1.

2. **[CẦN CHỐT 02 - Tái phân bổ khi thay đổi thứ tự quy tắc]:** Khi Giám đốc thay đổi thứ tự ưu tiên của các quy tắc hoặc tạo một quy tắc mới, hệ thống có tự động quét lại các lead đang nằm trong "Hàng chờ phân bổ" để phân bổ lại không?
   - *Phương án A (Khuyến nghị):* Không tự động quét lại các lead cũ trong hàng chờ; quy tắc mới chỉ áp dụng cho các lead mới sinh ra. Cung cấp một nút thủ công *"Quét lại hàng chờ theo quy tắc"* cho Director nếu muốn kích hoạt.
   - *Phương án B:* Tự động quét lại toàn bộ hàng chờ mỗi khi có thay đổi quy tắc. Hệ quả: Có thể xung đột nếu Trưởng nhóm đang phân tay dở dang.

3. **[CẦN CHỐT 03 - Trạng thái của Lead sau khi phân bổ]:** Khi hệ thống gán thành công `nguoi_phu_trach_id`, trạng thái của lead nên chuyển thành gì?
   - *Phương án A (Khuyến nghị):* Chuyển thành `DA_PHAN_BO` (Đã phân bổ). Nhân viên kinh doanh sau đó sẽ bấm "Tiếp nhận" để chuyển sang `DANG_CHAM_SOC` (theo đúng luồng S4-07).
   - *Phương án B:* Giữ nguyên trạng thái `MOI` nhưng điền `nguoi_phu_trach_id`. Hệ quả: Khó phân biệt giữa lead mới tinh chưa ai đụng và lead đã có người nhận.

4. **[CẦN CHỐT 04 - Cơ chế Trigger Background Worker]:** Chu kỳ quét nền nên chạy theo phương thức nào?
   - *Phương án A (Khuyến nghị):* Dùng `ScheduledExecutorService` chạy nền chu kỳ 30 giây một lần. Đơn giản, tin cậy, không phụ thuộc thư viện ngoài, hoàn thành phân bổ trong < 1 phút (thỏa mãn < 5 phút).
   - *Phương án B:* Dùng Event Listener (bất đồng bộ ngay khi tạo lead gọi ThreadPool). Hệ quả: Nhanh tức thì nhưng cần xử lý kỹ rollback nếu giao dịch tạo lead chính bị fail.

---

## 8. CHECKLIST NGHIỆM THU THỰC TẾ (DEFINITION OF DONE FOR S4-06)

- [ ] Giám đốc kinh doanh (`Director`) truy cập được màn hình Cấu hình phân bổ lead, nhân viên kinh doanh (`Sales Rep`) bị chặn HTTP 403.
- [ ] Thêm mới thành công quy tắc phân bổ theo Khu vực (chọn được nhiều khu vực).
- [ ] Thêm mới thành công quy tắc phân bổ theo Ngành nghề (chọn được nhiều ngành nghề).
- [ ] Thêm mới thành công quy tắc phân bổ Xoay vòng trong Nhóm kinh doanh.
- [ ] Đổi được thứ tự ưu tiên của các quy tắc (Lên/Xuống) và DB lưu đúng thứ tự.
- [ ] Khi có lead mới khớp quy tắc trên cùng: Lead được gán đúng người/nhóm đích, các quy tắc dưới bị bỏ qua.
- [ ] Khi có lead mới không khớp quy tắc nào: Lead chuyển vào trạng thái Hàng chờ, hiển thị trên màn hình Hàng chờ của Trưởng nhóm.
- [ ] Trưởng nhóm phân tay được lead trong hàng chờ cho nhân viên thuộc nhóm mình.
- [ ] Tiến trình chạy nền tự động xử lý toàn bộ lead mới trong thời gian $\le 5$ phút.
- [ ] Bộ kiểm thử Unit Test cho `QuyTacPhanBoService` đạt độ phủ nhánh (branch coverage) $\ge 60\%$.
- [ ] Giao diện hiển thị chuẩn xác, không vỡ layout trên màn hình di động 360px.
