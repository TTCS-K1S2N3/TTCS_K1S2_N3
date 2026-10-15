# MSys CRM — STORY S4-03 | PHÂN TÍCH STORY & ACCEPTANCE CRITERIA

> **Trạng thái tài liệu: APPROVED (phê duyệt tài liệu theo xác nhận của người dùng).** **Người duyệt: Ngô Trung Kiên — ngày 14/10/2026.** Việc phê duyệt không phải bằng chứng đã có code hoặc đã nghiệm thu. Repo, MySQL/schema và đường dẫn thực tế **CHƯA ĐƯỢC CUNG CẤP**. Mọi thiết kế giao diện, DB, service và route ở dưới đều là **[ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO]**.
>
> **Phạm vi phê duyệt:** hai tài liệu phân tích/contract đã được gửi và được leader duyệt **theo thông báo của người dùng**. **[CHƯA ĐƯỢC CUNG CẤP]** nội dung quyết định cụ thể Q1–Q5 và xác nhận riêng của FE/BE; các phần phụ thuộc quyết định đó vẫn **BLOCKED**, không tự coi phương án đề xuất là đã chốt. Nhánh do người dùng thông báo: `feature/S4-03-chien-dich-marketing` — **[CHƯA XÁC MINH]** sự tồn tại/trạng thái Git. Giai đoạn phân tích này **không thao tác Git, không code, không cập nhật DB**.

## 1. Story trích đúng từ workbook

**[BACKLOG GỐC]** Nguồn: tệp `HỆ THỐNG QUẢN LÝ KHÁCH HÀNG (1)(3).xlsx`, sheet **`4. Product Backlog`**, **dòng 44, A44:J44**. Đối chiếu quyền: sheet **`2. User Roles`**, dòng **3–10, 13–25**. Thông tin phạm vi: `1. Product Overview` (dòng 35–49, 78); tên Epic: `3. Epics` (dòng 7); bối cảnh Sprint: `5. Sprint Plan` (dòng 7).

| Trường | Giá trị nguyên văn workbook |
|---|---|
| Story ID | `S4-03` |
| Sprint | `4` |
| Epic | `EP-04` — **Lead & Phân bổ** (`3. Epics!A7:B7`) |
| Vai trò trong backlog | `Nhân viên Marketing` |
| User Story | **Là Nhân viên Marketing, tôi muốn theo dõi lead theo từng chiến dịch, để đo được chiến dịch nào thực sự ra doanh thu chứ không chỉ ra nhiều lead.** |
| Story Points | `5` |
| Ưu tiên | `Should` |
| Trạng thái backlog | `Đang làm` |
| Ghi chú / Người thực hiện | `Hứa Lan Hương(FE)`; `Thào A Khua(BE)` |
| Tên Story riêng | **CHƯA BIẾT**: workbook không có cột “Tên Story” riêng. Nhãn làm việc **[ĐỀ XUẤT]**: “Theo dõi lead theo chiến dịch marketing”; **không coi là nguyên văn workbook**. |

### Acceptance Criteria — nguyên văn và đúng thứ tự

- **AC1 [BACKLOG GỐC]:** Khai báo chiến dịch với ngân sách, thời gian chạy, kênh
- **AC2 [BACKLOG GỐC]:** Lead và cơ hội giữ liên kết tới chiến dịch đã sinh ra chúng
- **AC3 [BACKLOG GỐC]:** Xem được số lead, số cơ hội và giá trị đã chốt của từng chiến dịch

**[GIẢI THÍCH]** Story yêu cầu **khai báo chiến dịch**, **truy vết chiến dịch nguồn tới lead/cơ hội**, rồi **tính/hiển thị chỉ số theo chiến dịch**. AC không quy định tên trường, đường dẫn, loại tiền, thuật toán tính, định nghĩa doanh thu, ai được nhìn tổng hợp xuyên dữ liệu hay cách sửa chiến dịch. Những điều này được giữ ở trạng thái **CẦN CHỐT**, không tự biến thành nghiệp vụ bắt buộc.

## 2. Ma trận vai trò và phạm vi dữ liệu — đọc từ sheet `2. User Roles`

**[BACKLOG GỐC]** Có **7 vai trò**: `Sales Rep` (Nhân viên kinh doanh), `Team Lead` (Trưởng nhóm kinh doanh), `Director` (Giám đốc kinh doanh), `Marketing` (Nhân viên Marketing), `Cust. Success` (Chăm sóc khách hàng), `Accountant` (Kế toán), `Admin` (Quản trị hệ thống). Trên ma trận dòng 15–25, cột role lần lượt là Sales Rep, Marketing, Cust. Success, Accountant, Team Lead, Director; **Admin không có cột riêng** nhưng ghi chú dòng 14 nêu *Admin có toàn quyền trên mọi module*.

**[BACKLOG GỐC]** Giải nghĩa dòng 14: **F = toàn quyền**; **W = ghi trong phạm vi được giao**; **R = chỉ xem**; **– = không truy cập**; dấu `*` = **chỉ trên dữ liệu mình sở hữu**, hoặc **dữ liệu của nhóm mình quản lý đối với Team Lead**. Đây là lọc thực hiện tại tầng truy vấn **server**, không chỉ ẩn ở UI. Giá vốn và biên lợi nhuận chỉ Director xem được. **Quyền thao tác module không đồng nghĩa quyền xem mọi bản ghi**.

| Vai trò | `Lead & phân bổ` (dòng 18) | `Cơ hội & pipeline` (dòng 19) | `Báo cáo & dashboard` (dòng 23) | Tác động cho S4-03 |
|---|---|---|---|---|
| Sales Rep | `W*` | `W*` | `R*` | Chỉ dữ liệu được phép; **chưa rõ** có được khai báo/xem chiến dịch hay không. |
| Marketing | `F` | `R` | `R*` | Chủ thể Story; được thao tác module lead, **không được mặc nhiên sửa cơ hội**; báo cáo không mặc nhiên toàn công ty. |
| Cust. Success | `–` | `R*` | `R*` | Không tự cấp quyền vào phần khai báo chiến dịch thuộc module Lead. |
| Accountant | `–` | `R` | `R` | Không tự cấp quyền Lead; khả năng thấy thống kê chiến dịch chưa rõ. |
| Team Lead | `F` | `F` | `R*` | Quyền rộng ở module; phạm vi nhóm phải xét với dữ liệu dấu `*` và chính sách thực tế. |
| Director | `F` | `F` | `F` | Được cấp module toàn quyền theo matrix; chính sách báo cáo cụ thể vẫn đối chiếu source. |
| Admin | Toàn quyền (ghi chú) | Toàn quyền (ghi chú) | Toàn quyền (ghi chú) | Vẫn cần đăng nhập, kiểm tra server-side và giới hạn dữ liệu nhạy cảm. |

**[GIẢI THÍCH]** `Marketing: Lead & phân bổ = F` khác với `Marketing: Cơ hội & pipeline = R` và `Marketing: Báo cáo & dashboard = R*`. Hiển thị số tiền đã chốt **có thể** tiết lộ thông tin cơ hội không thuộc phạm vi người xem; **[CẦN CHỐT]** chính sách tổng hợp và kiểm tra phía server. Không tự suy luận “Marketing được xem mọi doanh thu”.

## 3. Ranh giới nghiệp vụ và phụ thuộc

- **[BACKLOG GỐC]** `1. Product Overview` nêu **ngoài phạm vi**: marketing automation (gửi email hàng loạt/nuôi dưỡng lead), hóa đơn/hạch toán doanh thu, mobile native. **[GIẢI THÍCH]** S4-03 **không** tự thêm các chức năng đó; giá trị “đã chốt” là một chỉ số CRM, không mặc nhiên là doanh thu kế toán.
- **[BACKLOG GỐC + GIẢI THÍCH]** Phần tạo/nhập lead từ `S4-01`, `S4-02`, chuyển lead thành cơ hội từ `S4-08`, quản lý cơ hội `S5-01`, đóng Thắng/Thua `S5-05` là **điểm tích hợp nghiệp vụ liên quan thật sự**. Ví dụ `S5-05` nêu khi đóng **Thắng** phải có **giá trị chốt thực tế** và **ngày ký**; điểm này hữu ích cho AC3. `S8-09` làm báo cáo chi tiết theo nguồn/chiến dịch và xuất Excel, là Story khác, **không gộp vào phạm vi S4-03**.
- **[CHƯA XÁC MINH]** Không biết entity/service/controller, mã DB, luồng chuyển đổi, trạng thái cơ hội, trường giá trị đã chốt, bảng chiến dịch hay quy tắc phân quyền nào đã tồn tại trong repo. Các trạng thái `Đang làm`/`Chưa bắt đầu` trong workbook **không thể chứng minh** chức năng đã code hoặc còn thiếu.

## 4. Phân tích chi tiết từng AC

### AC1 — Khai báo chiến dịch với ngân sách, thời gian chạy, kênh

**1) Ai / ở đâu / đầu ra.** **[BACKLOG GỐC]** Chủ thể Nhân viên Marketing khai báo chiến dịch. **[ĐỀ XUẤT]** Tại trang “Chiến dịch marketing”, bấm “Tạo chiến dịch”, điền tên nhận diện, ngân sách, ngày bắt đầu, ngày kết thúc, kênh rồi lưu. Quan sát được: chiến dịch xuất hiện trong danh sách; xem được các thuộc tính vừa khai báo. **Tên chiến dịch** là trường nhận diện **đề xuất** phục vụ chọn/lọc, không được nêu riêng ở AC.

**2) Điều kiện, dữ liệu, quy tắc.** Cần session hợp lệ, quyền tạo chiến dịch tương thích RBAC và nguồn danh mục kênh. **[ĐỀ XUẤT]** tên không rỗng, ngân sách không âm, ngày bắt đầu ≤ ngày kết thúc, kênh thuộc lựa chọn cho phép; ngày dạng `yyyy-MM-dd`, tiền xử lý `BigDecimal` phía server. **[CẦN CHỐT]** loại tiền, chính xác/giới hạn ngân sách, cho phép chiến dịch tương lai/quá khứ, cùng tên, trạng thái/đóng chiến dịch, khả năng sửa/xóa và người được tạo. Không tự quy định giới hạn độ dài hay trạng thái chuẩn.

**3) Luồng FE → BE → DB.** **[ĐỀ XUẤT]** JSP GET hiển thị form (kèm CSRF, danh sách kênh), POST `application/x-www-form-urlencoded` → Servlet đọc tham số → auth/RBAC/scope + CSRF + chuẩn hóa/validation → Service xác minh nghiệp vụ → DAO ghi **thực thể chiến dịch logic** trong DB bằng câu lệnh tham số hóa → commit nếu thành công → redirect (Post/Redirect/Get) tới trang chi tiết/danh sách có flash message; nếu input sai thì forward lại JSP với lỗi từng trường, giữ dữ liệu hợp lệ. **[CHƯA XÁC MINH]** tên controller, service, DAO, đường dẫn, table và transaction boundaries thật.

**4) Lỗi/biên.** Tên/kênh trống; ngân sách rỗng, chữ, số âm, số cực lớn hoặc dấu phẩy không đúng quy ước; ngày sai định dạng, ngày kết thúc trước bắt đầu; kênh hết hiệu lực; form gửi đôi; mất session, không quyền, CSRF sai, DB lỗi; hai người tạo cùng tên đồng thời (chỉ cấm trùng nếu nhóm chốt unique); user gửi ID/role giả từ form. **[ĐỀ XUẤT]** chống gửi đôi bằng disable nút/lưu trạng thái xử lý phía FE và token/idempotency hoặc quy tắc unique hợp lệ phía server; **không** dựa vào disabled button làm bảo vệ duy nhất. DB lỗi không tạo chiến dịch nửa vời, không lộ stack trace/password.

**5) UX.** Form rõ 5 nhãn, dấu trường bắt buộc chỉ sau khi chốt, input tiền số, chọn ngày, chọn kênh, nút chính “Lưu chiến dịch”, nút phụ “Hủy”; lỗi ngay cạnh trường và tóm tắt lỗi; loading khi submit, success flash sau redirect, empty state danh sách “Chưa có chiến dịch”; tên dài thu gọn kèm tooltip/chi tiết, cột bảng có thể cuộn ngang trên màn nhỏ, nhãn `label`/keyboard/focus rõ. Danh sách nhiều bản ghi dùng phân trang **[ĐỀ XUẤT]** và không tải vô hạn.

**6) BE và bảo mật.** Validation phải nằm tại BE; quyền FE chỉ để điều khiển hiển thị, BE kiểm tra lại. Kiểm tra quyền module và phạm vi chiến dịch trước thao tác, escape HTML đầu ra (chống XSS), CSRF với POST, truy vấn tham số hóa; audit tạo/sửa **nếu dự án quy định**, AC không đòi nhật ký riêng. Nếu thêm nhiều bảng liên quan khi tạo thì transaction toàn bộ, không ghi dở. Hạn chế tiết lộ thông tin người tạo và chi phí ngoài phạm vi.

**7) Kiểm thử cụ thể.** `T-AC1-01`: tài khoản Marketing có quyền hợp lệ mở form, nhập `"Sự kiện tháng 10"`, ngân sách `12000000`, `2026-10-01`–`2026-10-31`, kênh `EVENT` (**giá trị/kênh giả**) → lưu một chiến dịch, trang hiển thị đúng dữ liệu, DB test truy xuất một record. `T-AC1-02`: ngân sách `-1` → từ chối, lỗi ngân sách, DB không tăng record. `T-AC1-03`: `2026-10-31`–`2026-10-01` → lỗi thời gian. `T-AC1-04`: role bị cấm gửi POST trực tiếp → từ chối, 0 write. `T-AC1-05`: gửi POST hai lần nhanh → kết quả theo quy tắc idempotency/unique được **chốt trước test**, không ngầm kỳ vọng. `T-AC1-06`: DB tạo chiến dịch thất bại → thông báo an toàn, không lưu bản ghi không nhất quán. **ĐẠT AC1** khi có bằng chứng tạo/đọc thật + validation + quyền và các case biên đã thống nhất.

**8) IN / OUT.** **IN:** định danh chiến dịch để khai báo và lưu ngân sách/thời gian/kênh; hiển thị lại để kiểm tra. **OUT:** tự động gửi marketing, phân bổ lead, quản trị ngân sách chi tiết, ROI/CPL, workflow duyệt, xóa/nhân bản chiến dịch, upload ảnh trừ khi có quyết định mở rộng Story.

### AC2 — Lead và cơ hội giữ liên kết tới chiến dịch đã sinh ra chúng

**1) Ai / ở đâu / đầu ra.** **[BACKLOG GỐC]** Lead và cơ hội phải còn dấu vết chiến dịch tạo ra chúng. **[GIẢI THÍCH]** Người tạo lead thuộc luồng hiện hữu (Marketing); người tạo/chuyển cơ hội có thể là vai trò được phép tương ứng. **[ĐỀ XUẤT]** ở nơi tạo lead hiển thị trường chiến dịch (nếu có), khi chuyển từ lead sang cơ hội giữ đúng campaign ID, trong trang chi tiết thể hiện “Chiến dịch nguồn”. Không tự thêm quyền sửa chiến dịch nguồn.

**2) Điều kiện, dữ liệu, quy tắc.** Cần chiến dịch tồn tại và được phép gắn, dữ liệu lead/opportunity hợp lệ theo Story chủ quản. **[CẦN CHỐT]** campaign ID có được null cho lead nhập ngoài chiến dịch hay không; nguồn/chiến dịch có bắt buộc khớp; một lead/cơ hội có **một** hay **nhiều** chiến dịch; cơ hội tạo độc lập lấy nguồn từ đâu; khi convert lead thì kế thừa hay chọn lại; sau tạo có đổi chiến dịch nguồn được không; khi chiến dịch bị ngừng/xóa liên kết xử lý sao. **[ĐỀ XUẤT DRAFT]** một `campaignId` nguồn/record, nullable cho dữ liệu không có chiến dịch; khi chuyển lead → cơ hội thì kế thừa, **không cho FE giả dữ liệu liên kết**.

**3) Luồng FE → BE → DB.** **[ĐỀ XUẤT]** tại các form lead hiện hành, người dùng chọn chiến dịch được phép → POST luồng lead hiện hữu có `campaignId` → Servlet/Service validate ID + quyền truy cập chiến dịch → DAO lưu liên kết logic. Khi chuyển đổi, Service đọc `campaignId` của lead từ DB (không tin campaignId do client gửi), kiểm tra transaction → tạo customer/contact/opportunity theo Story chuyển đổi và **sao chép/tạo quan hệ nguồn** được chốt → commit tất cả hoặc rollback tất cả. Khi tạo cơ hội độc lập, quy tắc gán campaign phải được nhóm chốt và tích hợp trong Service tạo cơ hội. FE đọc từ BE và render liên kết, không giữ riêng ở JS làm nguồn sự thật.

**4) Lỗi/biên.** Campaign ID trống/hết hiệu lực/không tồn tại/sai định dạng/ngoài phạm vi; lead đã convert; nhiều người cùng chuyển lead; request của người A gắn campaign của người B không được phép; tạo opportunity lỗi sau khi tạo customer/contact; thay đổi attribution đồng thời; delete/merge lead/đóng campaign; nhập Excel có mã chiến dịch sai. **[ĐỀ XUẤT]** 403 nếu người dùng không được phép, 404 khi tham chiếu không tìm thấy mà không rò thông tin, 409 cho xung đột/trùng convert; rollback tất cả các ghi trong một nghiệp vụ nhiều bước. Không tự giả định import có cột chiến dịch.

**5) UX.** Dropdown searchable (nếu nhiều chiến dịch) với nhãn dễ hiểu, trạng thái không có chiến dịch, hiển thị chiến dịch nguồn trong lead và opportunity; trường read-only ở form sau convert **nếu chính sách attribution bất biến được duyệt**. Nếu campaign không còn truy cập được, không hiển thị thông tin nhạy cảm hoặc nút truy cập trái phép. Trên di động tránh bảng dàn ngang quá rộng; nếu form lỗi giữ lựa chọn hợp lệ, thông báo ngay dưới trường.

**6) BE và bảo mật.** Chặn IDOR: validate quyền với *bản ghi cụ thể*, không chỉ kiểm tra có role; scope của lead khác scope của cơ hội; cross-module read Marketing `R` không cho write opportunity. DB phải có liên kết bền vững, bảo toàn toàn vẹn tham chiếu, nhất quán khi chuyển đổi; transaction cần có nếu chuyển đổi nhiều entity. Không tự khẳng định `campaign_id` hiện diện trong các bảng hiện tại hoặc được phép ALTER schema.

**7) Kiểm thử cụ thể.** `T-AC2-01`: có campaign giả `101`, tạo lead `201` chọn campaign `101` → đọc lại từ DB test đúng liên kết. `T-AC2-02`: convert lead `201` qua luồng hợp lệ → cơ hội mới truy vấn lại vẫn trỏ campaign `101`, transaction hoàn chỉnh. `T-AC2-03`: campaign `999999` không tồn tại → lỗi an toàn, không ghi link ma. `T-AC2-04`: role bị giới hạn cố đổi ID trong POST → 403/404 theo contract đã duyệt, không tạo link. `T-AC2-05`: lỗi DB ở bước tạo opportunity → rollback các ghi nghiệp vụ liên quan (nếu cùng transaction). `T-AC2-06`: tạo opportunity trực tiếp và lead không gắn campaign → expected **CHƯA BIẾT** cho tới khi chốt chính sách. **ĐẠT AC2** khi truy vết liên kết bằng truy vấn DB test **sau** toàn luồng (không chỉ hiển thị mock), có test quyền và rollback.

**8) IN / OUT.** **IN:** lưu/truy vấn liên kết nguồn chiến dịch của lead và opportunity; giữ liên kết khi chuyển đổi phù hợp contract. **OUT:** tự động gộp lead trùng (`S4-04`), thiết kế toàn luồng import Excel (`S4-02`), chia doanh thu đa chiến dịch, multi-touch attribution, sửa/đổi toàn bộ pipeline của Story khác.

### AC3 — Xem được số lead, số cơ hội và giá trị đã chốt của từng chiến dịch

**1) Ai / ở đâu / đầu ra.** **[BACKLOG GỐC]** Có ba chỉ số trên từng chiến dịch. **[ĐỀ XUẤT]** danh sách/chi tiết chiến dịch có các nhãn “Số lead”, “Số cơ hội”, “Giá trị đã chốt”, cho phép Marketing/role được cấp quyền xem. Giá trị 0 hiển thị `0`, không bỏ trống; danh sách trống có empty state.

**2) Điều kiện, dữ liệu, quy tắc.** Phải xác định source-of-truth của link campaign và trạng thái/value của opportunity; số lượng là số bản ghi phân biệt sau lọc quyền. **[CẦN CHỐT]** số lead có tính lead đã chuyển đổi/gộp/xóa mềm? Số cơ hội có gồm đang mở/thua? Giá trị chốt chỉ cơ hội `Thắng` với **giá trị chốt thực tế** theo `S5-05` hay hợp đồng đã ký / doanh thu thực thu? Xử lý cơ hội mở lại, đa loại tiền, thời gian ghi nhận, phạm vi báo cáo người xem? **[ĐỀ XUẤT DRAFT]** `leadCount = COUNT(DISTINCT leadId)` gắn campaign, `opportunityCount = COUNT(DISTINCT opportunityId)` gắn campaign, `closedWonAmount = SUM(actualClosedAmount)` của **cơ hội thắng** gắn campaign; chưa tính thua/đang mở, không cộng `expectedAmount`, không tính hóa đơn. Cần thống nhất số liệu có bị giới hạn Data Scope hay không.

**3) Luồng FE → BE → DB.** **[ĐỀ XUẤT]** GET danh sách/chi tiết chiến dịch → Servlet xác thực + module permission + scope → Service truy vấn DAO thống kê từ tập lead/opportunity **mà người xem được quyền đếm/đọc** → aggregate độc lập hoặc `COUNT DISTINCT`, `COALESCE(SUM(...),0)` phù hợp DB → trả DTO số nguyên + `BigDecimal` → JSP định dạng VND/đơn vị tiền đã chốt, kèm phân trang. FE không tự tổng hợp từ 1 trang lead hạn chế, không lấy tổng toàn DB rồi che hàng ở FE.

**4) Lỗi/biên.** Không có lead/opportunity (0/0/0); link campaign thiếu; cơ hội đã đóng Thua, đang mở, đóng Thắng thiếu giá trị (**phải xử lý dữ liệu lỗi**, không đoán là 0); 1 lead sinh nhiều cơ hội; join làm nhân đôi tiền; cơ hội mở lại/đổi trạng thái; 2 người đóng cơ hội cùng lúc; tổng `BigDecimal` rất lớn; cùng tên chiến dịch; 10.000+ lead; DB timeout; role ngoài scope. Nếu dữ liệu bị lỗi, log bằng tham chiếu an toàn và có chính sách hiển thị, không im lặng lấy expected value.

**5) UX.** Bảng/cards có 3 KPI/chiến dịch, nhãn “giá trị đã chốt” (không thay thành “doanh thu thực thu”), format đơn vị rõ; loading/empty/error; tên quá dài thu gọn; phân trang ở server, thông báo thiếu quyền bằng tiếng Việt, tooltip giải nghĩa “đã chốt” sau khi chốt định nghĩa. Nếu có filter thì chỉ được bổ sung nếu thuộc phạm vi chốt; **không** âm thầm triển khai dashboard biểu đồ/Excel export thuộc Story khác.

**6) BE và bảo mật.** Kiểm tra quyền **trước khi** chạy aggregate; tất cả truy vấn gồm dữ liệu được phép trong scope, kể cả số lượng/tổng tiền. Không dùng `float/double` cho tiền; aggregate không double count; xử lý nullable đúng; index/performance chỉ **đề xuất sau đo**, không khẳng định schema đang có index. Kết quả giữa các phép đếm là snapshot mong muốn nhưng mức nhất quán DB **CHƯA BIẾT**; đối soát lúc có cập nhật đồng thời bằng test tích hợp.

**7) Kiểm thử cụ thể.** `T-AC3-01`: campaign `101` có **3 lead** (`201`, `202`, `203`), **2 cơ hội** (`301` Thắng giá trị chốt `45000000`, `302` Đang mở giá trị dự kiến `30000000`) → `leadCount=3`, `opportunityCount=2`, `closedWonAmount=45000000` (không cộng `30000000`). `T-AC3-02`: campaign `102` không có liên kết → `0/0/0`. `T-AC3-03`: thêm cơ hội Thua giá trị dự kiến `99999999` → số cơ hội tăng 1, giá trị chốt không đổi. `T-AC3-04`: fan-out join 1 lead nhiều hoạt động → giá trị chốt vẫn chỉ cộng 1 lần. `T-AC3-05`: Marketing dùng quyền hạn chế → chỉ thống kê tập được phép theo chính sách nhóm duyệt, truy cập campaign ngoài phạm vi bị từ chối. `T-AC3-06`: tạo ≥10.000 lead test + phân trang → đo p50/p95 thực tế ở môi trường test; **backlog S4-03 không có ngưỡng hiệu năng, không được tự kết luận PASS/FAIL bằng giả định**. `T-AC3-07`: cơ hội Thắng bị mở lại → chỉ số theo quy tắc sau khi nhóm chốt, không tự suy đoán. **ĐẠT AC3** khi kết quả từ DB test khớp fixture và các chính sách về status/currency/scope đã chốt.

**8) IN / OUT.** **IN:** thống kê, hiển thị 3 chỉ số theo từng chiến dịch, an toàn role/scope. **OUT:** CPL/ROI, lợi nhuận, chi phí thực chi, dự báo, tỷ lệ chuyển đổi, xuất Excel, biểu đồ chuyên sâu, đo tốc độ dưới 2 giây của `S8-04` (không phải AC S4-03).

## 5. Bảng mapping AC → quy tắc → FE → BE/DB → dữ liệu → test

| AC | Diễn giải chính xác | Luồng FE | Luồng BE/DB dự kiến | Quyền/phạm vi | Test | Câu hỏi chưa chốt |
|---|---|---|---|---|---|---|
| AC1 | Khai báo ngân sách, thời gian, kênh (tên là trường **đề xuất**) | Danh sách → form → Lưu → success/field errors | Validate; lưu campaign logic; PRG; transaction khi cần | Marketing theo module Lead `F`; quyền cụ thể tạo chiến dịch và các role khác phải đối chiếu | T-AC1-01…06; đọc lại DB test, denied write | Q1, Q2 |
| AC2 | Lead và opportunity giữ đúng nguồn chiến dịch | Chọn/xem nguồn chiến dịch tại form liên quan; không tự sửa ngoài chính sách | Xác minh `campaignId`; persistence; khi convert copy link trong transaction hoặc cơ chế tương đương | Lead permission **khác** opportunity permission; chặn IDOR | T-AC2-01…06; DB readback, rollback | Q3, Q5 |
| AC3 | Mỗi chiến dịch có 3 KPI: số lead, số cơ hội, giá trị đã chốt | Danh sách/chi tiết hiển thị 3 KPI + zero/error/long-list | `COUNT DISTINCT`/`SUM BigDecimal` trên tập bản ghi hợp lệ sau RBAC + Data Scope | Marketing opportunity `R`, báo cáo `R*`; tuyệt đối không lọt aggregate ngoài scope | T-AC3-01…07; đối soát SQL/DAO và test quyền | Q4, Q5 |

### Luồng xuyên AC — dữ liệu thật, không dùng mock để nghiệm thu

`Người dùng đủ quyền → JSP/form chiến dịch → Servlet → Service validation/authorization → DAO lưu campaign → DB test` → `Tạo lead gắn campaign qua luồng sở hữu lead → chuyển/tạo opportunity gắn campaign qua luồng sở hữu opportunity` → `Đóng Thắng với actualClosedAmount theo Story chủ quản` → `DAO tính 3 KPI theo scope → Servlet set JSP attributes → JSP hiển thị`. **[ĐỀ XUẤT]** Đây là luồng logic kỳ vọng, **không là mô tả source đã có**.

## 6. Tối đa 5 câu hỏi CẦN CHỐT trước khi khóa contract / đụng DB

| ID | CÂU HỎI CẦN CHỐT | Phương án thảo luận (đều là ĐỀ XUẤT) | Hệ quả nếu khác nhau | Người quyết định |
|---|---|---|---|---|
| **Q1** | Ai được **tạo/xem** chiến dịch và xem 3 KPI? Có cho Marketing xem tổng doanh số của cơ hội ngoài scope không? | A: thống kê đúng từng record nhìn được (an toàn theo scope); B: chỉ role được cấp đặc quyền xem tổng xuyên scope | Quyết định auth, câu SQL lọc, JSP buttons, test 403 và khả năng lộ tiền | Mentor/leader + FE/BE |
| **Q2** | Bộ trường và quy tắc khi khai báo chiến dịch là gì: **tên/kênh**, đơn vị tiền, ngân sách 0, ngày chạy, sửa/xóa/trùng tên? | A: tên + ngân sách ≥0 + start≤end + kênh trong danh mục, đơn tiền VND; B: theo quy ước hiện có của repo | Đổi form, validation, DTO, DB constraints | Mentor/leader + FE/BE |
| **Q3** | Chiến dịch nguồn của lead/opportunity có bắt buộc không, một hay nhiều, **kế thừa** khi convert và có cho sửa lại không? | A: tối đa một ID, nullable khi không phát sinh từ chiến dịch, opportunity từ lead kế thừa; B: đa chiến dịch hoặc luôn bắt buộc | Thay quan hệ DB, endpoint tạo lead/opportunity, tính KPI, chống trùng | Mentor/leader + FE/BE, nhóm phụ trách lead/opp |
| **Q4** | “Giá trị đã chốt” là gì, tính vào kỳ nào, loại tiền nào, xử lý **Thắng/Thua/mở lại** thế nào? | A: tổng *giá trị chốt thực tế* của cơ hội Thắng (liên hệ S5-05), không là doanh thu kế toán; B: giá trị hợp đồng đã ký với quy tắc riêng | Thay DAO aggregate, status enum, schema/mapping, kỳ báo cáo và đối soát test | Mentor/leader + FE/BE, nhóm cơ hội |
| **Q5** | Chỗ tích hợp hiện tại của `campaignId` ở luồng **tạo lead, nhập Excel/web form, tạo/chuyển cơ hội** nằm ở đâu và sẽ được nhóm nào chỉnh? | A: bổ sung tại các luồng hiện có sau khi đối chiếu repo; B: phạm vi S4-03 chỉ phát triển contract + adapter tạm, blocker nếu schema thiếu | Ảnh hưởng shared DTO/schema, transaction chuyển đổi, phối hợp Story khác | Leader + FE/BE + owner liên quan |

**[ĐÃ ĐƯỢC NGƯỜI DÙNG XÁC NHẬN]** Leader Ngô Trung Kiên đã duyệt hai tài liệu ngày 14/10/2026. **[CHƯA ĐƯỢC CUNG CẤP]** nội dung trả lời/lựa chọn cho Q1–Q5: các đề xuất A vẫn chỉ là **ĐỀ XUẤT**, **không** phải quyết định ngầm của người duyệt. Không được triển khai phần phụ thuộc Q1–Q5, thay đổi schema hoặc tự sửa module/shared contract khi chưa có quyết định/ủy quyền phù hợp.

## 7. Decision log / các giả định chưa có lựa chọn cụ thể

| Decision | Tạm đề xuất | Trạng thái | Người/ngày chốt |
|---|---|---|---|
| DEC-01: Dữ liệu khởi tạo chiến dịch | Form server-render + POST/PRG; trường tên, ngân sách, ngày bắt đầu/kết thúc, kênh | **DRAFT** | CHƯA BIẾT |
| DEC-02: Attribution | Một `campaignId` logic, nullable; cơ hội convert kế thừa từ lead | **DRAFT** | CHƯA BIẾT |
| DEC-03: Số liệu | Đếm bản ghi phân biệt; Thắng cộng actual closed value; không tính expected | **DRAFT** | CHƯA BIẾT |
| DEC-04: Quyền | Kiểm tra module + từng record + tổng hợp đúng scope | **DRAFT** | CHƯA BIẾT |
| DEC-05: Đơn tiền / enum kênh | Ví dụ mock dùng VND và `EVENT`; giá trị chính thức do nhóm chốt | **DRAFT** | CHƯA BIẾT |

**[ĐÃ ĐƯỢC NGƯỜI DÙNG XÁC NHẬN]** Phê duyệt tài liệu: Ngô Trung Kiên, 14/10/2026; nhánh dự kiến `feature/S4-03-chien-dich-marketing` do người dùng cung cấp, **chưa xác nhận tồn tại trên remote**. **[CHƯA ĐƯỢC CUNG CẤP]** lựa chọn cụ thể Q1–Q5 và người/ngày chốt từng DEC-01…05; không đánh đồng phê duyệt tài liệu với chấp thuận mặc định toàn bộ các giả định.

## 8. Checklist nghiệm thu thực tế (tất cả hiện CHƯA THỬ)

- [x] Tìm đúng bản ghi `S4-03` với **3 AC**; người dùng xác nhận leader đã duyệt tài liệu ngày 14/10/2026. **[ ]** Nhận nội dung lựa chọn Q1–Q5 từ leader/FE+BE cho các phần cần chốt.
- [ ] Đối chiếu route, DTO/fields, DB/schema, permission/filter và các luồng lead → cơ hội hiện có trước khi code; mọi thay đổi giao diện chia sẻ được thống nhất.
- [ ] **AC1**: người có quyền tạo/lưu/xem lại chiến dịch trên DB test; validation, date/budget/channel/CSRF/403 và gửi đôi đúng quy tắc đã duyệt.
- [ ] **AC2**: từ UI/form hoặc luồng nhập thật tạo lead đúng campaign, chuyển/tạo opportunity rồi đọc lại link từ DB test; kiểm tra lỗi, IDOR và transaction rollback.
- [ ] **AC3**: 3 KPI từ DB test, 0/0/0, có thắng/thua/đang mở, không double count, scope theo tài khoản test, BigDecimal/format đúng.
- [ ] FE và BE dùng chung contract/field names, không hard-code mock trong demo/production; lỗi HTTP/field errors nhất quán.
- [ ] Manual test với ≥2 người dùng khác quyền/nhóm và trường hợp không có dữ liệu; responsive 360px và desktop, XSS escaping, keyboard navigation.
- [ ] Nếu có yêu cầu hiệu năng được nhóm bổ sung: setup dataset/đo thực trên test environment, lưu mẫu, query plan, p50/p95, **không tự báo PASS**.
- [ ] Đối soát test tự động, tích hợp FE ↔ BE ↔ **DB test cách ly**, báo rõ PASS/FAIL/SKIPPED; không kết luận DONE chỉ từ mock hoặc riêng BE/FE.
- [ ] Không đụng MySQL chung/schema production; schema SQL chỉ đọc cho tới khi có phê duyệt riêng.

**Trạng thái AC1/AC2/AC3 hiện tại: CHƯA KIỂM THỬ — KHÔNG PHẢI PASS.**

## 9. Điểm bắt buộc kiểm chứng trong repo trước khi triển khai (cho Antigravity giai đoạn sau)

1. Cấu trúc **Servlet/JSP + Maven + Jakarta**, mapping/context path, `web.xml`/annotation, cách forward/redirect, danh mục JSP/layout/shared navigation; có route chiến dịch sẵn chưa?
2. Auth/session/filters, CSRF, RBAC module, scope owner/team/all, cơ chế cấp quyền `Marketing = F` trên lead và `Marketing = R` trên cơ hội; aggregate đã có cơ chế lọc chưa?
3. Schema SQL thực tế (chỉ đọc): có campaign, lead, opportunity, quan hệ attribution, FK/unique/index, bảng/field actual closed value, won/closed states; tên **CHƯA BIẾT**.
4. DTO/Service/DAO đang phục vụ tạo lead tay/import/web, chuyển lead, tạo cơ hội, đóng Thắng/Thua; ranh giới transaction và các Story cùng chỉnh file/shared DTO.
5. Danh mục kênh bán hàng/marketing, loại tiền, format ngày/decimal, timezone, cơ chế lỗi/flash message, helper escape HTML, log/audit hiện hữu.
6. Test hiện có và cấu hình H2 cách ly/MySQL runtime: kiểm tra cách ly **trước** test; không được chạy migration/seed hay test có ghi trên DB dùng chung.
7. Kiểm tra nhánh `feature/S4-03-chien-dich-marketing` ở remote **sau** khi 2 tài liệu được duyệt và bắt đầu quy trình coding; hiện chưa kiểm tra Git.

---

**Ghi nhận phê duyệt:** `APPROVED` cho tài liệu theo thông báo của người dùng (Ngô Trung Kiên, 14/10/2026). Những điều chưa rõ Q1–Q5 **chưa tự động trở thành quyết định đã chốt**; khi Antigravity đối chiếu repo cần khoanh vùng `MATCH/ADAPT/ALREADY IMPLEMENTED/BLOCKED`, dừng ở các interface/schema/quyền còn tranh chấp và xin leader quyết định cụ thể trước khi code phần liên quan.


---

## 10. Kết quả tự phân tích và xác nhận trực tiếp với Excel (14/10/2026)

**Trạng thái rà soát workbook: ĐÃ KIỂM CHỨNG NỘI DUNG BACKLOG. Trạng thái tài liệu: APPROVED theo xác nhận của người dùng về việc leader duyệt ngày 14/10/2026; các lựa chọn nghiệp vụ chi tiết chưa được cung cấp.** Đã mở workbook đính kèm để kiểm tra lại `4. Product Backlog!A44:J44`, `2. User Roles!A14:H25`, `3. Epics!A7:G7`, `1. Product Overview!A35:D49, A78:D78` và các dòng Story phụ thuộc. Nội dung dưới đây chỉ xác nhận điều thực sự có trong Excel và phân biệt rõ diễn giải với quyết định còn thiếu.

| Nội dung | Xác nhận từ Excel | Phần chỉ là giải thích/đề xuất (chưa duyệt) |
|---|---|---|
| Story S4-03 | **[BACKLOG GỐC — XÁC NHẬN]** Sprint 4, EP-04, Marketing, 5 points, Should, Đang làm; một User Story và **đúng 3 AC** được chép ở §1. Workbook **không có cột Tên Story**. | Tên ngắn “Theo dõi lead theo chiến dịch” chỉ là nhãn làm việc. `Đang làm` không chứng minh code đã triển khai. |
| **Q1 — Phân quyền** | **[BACKLOG GỐC — XÁC NHẬN]** `Lead & phân bổ`: Marketing `F`; `Cơ hội & pipeline`: Marketing `R`; `Báo cáo & dashboard`: Marketing `R*`; `*` nghĩa là giới hạn dữ liệu theo chủ sở hữu/nhóm tại **server**. Director `F` ba module; Admin toàn quyền theo ghi chú. | **[CẦN CHỐT]** Có cho Marketing xem tổng giá trị cơ hội ngoài scope dưới dạng **aggregate** hay không; không thể suy từ chữ `F` ở module Lead. **Phương án thận trọng:** không công bố số ngoài scope khi chưa được phê duyệt. Điều này có thể làm KPI chiến dịch thiếu doanh số và cần leader quyết định chính sách tổng hợp phù hợp AC3. |
| **Q2 — Khai báo chiến dịch** | **[BACKLOG GỐC — XÁC NHẬN]** AC1 yêu cầu ngân sách, thời gian chạy và kênh. | **[ĐỀ XUẤT]** cần tên để nhận diện; kiểm tra ngân sách ≥0, bắt đầu ≤ kết thúc, kênh hợp lệ. **[CẦN CHỐT]** tính bắt buộc/giới hạn, đơn tiền, trùng tên và quyền sửa/xóa; Excel không định nghĩa. |
| **Q3 — Nguồn chiến dịch** | **[BACKLOG GỐC — XÁC NHẬN]** AC2 yêu cầu **cả lead lẫn cơ hội** giữ liên kết đến chiến dịch sinh ra chúng. | **[ĐỀ XUẤT]** `campaignId` logic tùy chọn với bản ghi không phát sinh từ chiến dịch; cơ hội convert kế thừa từ lead; tối đa một nguồn. **[CẦN CHỐT]** 1–nhiều và thay đổi nguồn sau tạo; các chi tiết đó không có trong Excel. |
| **Q4 — Giá trị đã chốt** | **[BACKLOG GỐC — XÁC NHẬN]** AC3 hiển thị số lead, số cơ hội và giá trị đã chốt **theo chiến dịch**; Story `S5-05` quy định đóng **Thắng** phải nhập **giá trị chốt thực tế và ngày ký**. `1. Product Overview` loại trừ xuất hóa đơn/hạch toán doanh thu. | **[GIẢI THÍCH / ĐỀ XUẤT MẠNH]** tổng giá trị chốt thực tế của cơ hội đóng Thắng là phù hợp dữ kiện nhất, không dùng giá trị dự kiến và không đồng nhất với doanh thu kế toán. **[CẦN CHỐT]** kỳ tính, đa tiền tệ, cơ hội mở lại, xóa mềm; Excel chưa xác định. |
| **Q5 — Điểm tích hợp** | **[BACKLOG GỐC — XÁC NHẬN]** `S4-01` web lead, `S4-02` thủ công/Excel, `S4-08` chuyển lead, `S5-01` tạo cơ hội, `S5-05` đóng Thắng/Thua có nghiệp vụ liên quan trực tiếp. | **[CHƯA XÁC MINH]** Không có bằng chứng route, service, bảng, field hay tình trạng đã code. **[CẦN CHỐT]** owner sửa liên kết giữa các luồng và phương án thay đổi shared model/schema (nếu cần). |

**Kết luận rà soát:** **[BACKLOG GỐC — XÁC NHẬN]** Hai tài liệu mô tả đúng Story S4-03, ba AC gốc và ma trận quyền liên quan. **[ĐÃ ĐƯỢC NGƯỜI DÙNG XÁC NHẬN]** Leader Ngô Trung Kiên duyệt cả hai tài liệu ngày 14/10/2026 → trạng thái quản lý tài liệu **APPROVED**. **[CHƯA ĐƯỢC CUNG CẤP]** văn bản lựa chọn Q1–Q5, xác nhận riêng FE/BE và phiên bản contract kỹ thuật đã đối chiếu source; các hướng thiết kế ở §4–§7 vẫn là **[ĐỀ XUẤT]** chờ rà soát repo và quyết định chi tiết. **[CHƯA XÁC MINH]** Dữ liệu repository, DB, nhánh remote và bằng chứng test; không được tuyên bố AC PASS hoặc tự động áp dụng phương án A.
