# MSys CRM — STORY S4-03 | CONTRACT FE–BE & MOCK DATA CHUNG

> **PHIÊN BẢN: `0.1` — TRẠNG THÁI TÀI LIỆU: `APPROVED` (theo xác nhận của người dùng). Người duyệt: Ngô Trung Kiên; ngày 14/10/2026.** Được thiết kế từ workbook, **không lấy từ repo**. Mọi URL, method mapping, DTO, JSP attribute, Java field, tên bảng/field, enum, HTTP status, giới hạn, cách lưu và câu truy vấn dưới đây là **[ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO]**. Không kết luận module/schema đã tồn tại hay đã triển khai. Không thực hiện migration, code hoặc kết nối database trong giai đoạn này.
>
> Nhánh được **người dùng cung cấp**, chưa kiểm tra Git: `feature/S4-03-chien-dich-marketing`. Tài liệu đã được leader duyệt **theo thông báo của người dùng** để FE + BE cùng sử dụng làm baseline phân tích. **Phê duyệt tài liệu không xác nhận các lựa chọn Q1–Q5 (chưa nhận nội dung chốt), cũng không xác nhận route/schema/field thực tế trong repo.** Các phần phụ thuộc quyết định này phải ghi `BLOCKED` và xin chốt trước khi triển khai.

## 1. Phạm vi contract và ranh giới đã biết

**[BACKLOG GỐC]** `4. Product Backlog!A44:J44`: `S4-03`, Sprint `4`, `EP-04 — Lead & Phân bổ`, vai trò `Nhân viên Marketing`, `5` points, `Should`, backlog `Đang làm`.

**User Story nguyên văn:** “Là Nhân viên Marketing, tôi muốn theo dõi lead theo từng chiến dịch, để đo được chiến dịch nào thực sự ra doanh thu chứ không chỉ ra nhiều lead.”

**Acceptance Criteria nguyên văn:**

1. **AC1:** Khai báo chiến dịch với ngân sách, thời gian chạy, kênh
2. **AC2:** Lead và cơ hội giữ liên kết tới chiến dịch đã sinh ra chúng
3. **AC3:** Xem được số lead, số cơ hội và giá trị đã chốt của từng chiến dịch

**[GIẢI THÍCH]** Không có cột “Tên Story” riêng trong workbook; nhãn làm việc **[ĐỀ XUẤT]** là “Theo dõi lead theo chiến dịch marketing”. Nguồn quyền: `2. User Roles!A14:H25`. Luồng tạo/convert/đóng cơ hội thuộc các Story liên quan (`S4-01`, `S4-02`, `S4-08`, `S5-01`, `S5-05`); **không được tự viết lại** những luồng này chỉ để hợp mock. `S8-09` mới nêu tỷ lệ chuyển đổi, lọc thời gian, xuất Excel — **ngoài S4-03**.

### 1.1. Chọn hình thức giao tiếp cho Servlet/JSP

**[ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO]** Ưu tiên **SSR (server-rendered JSP) + HTML form `GET/POST`**: request browser → Servlet → Service → DAO → MySQL → forward JSP với request attributes / redirect sau POST. **Không bắt buộc REST/JSON/fetch/AJAX**. Nếu source đã có JSON endpoint hoặc chuẩn URL khác, FE+BE cần chốt điều chỉnh, không xây song song hai API trùng nghiệp vụ. **Trong bản DRAFT này không đề xuất endpoint AJAX mới**; dữ liệu JSON phía dưới chỉ là **fixture giao kèo** để mô tả giá trị DTO, không phải response mạng đang tồn tại.

### 1.2. Phân quyền tương ứng module

**[BACKLOG GỐC]** Ghi chú ma trận: `F` toàn quyền module; `W` ghi trong phạm vi được giao; `R` chỉ đọc; `–` không truy cập; `*` chỉ dữ liệu sở hữu/nhóm quản lý; lọc ở server. `Admin` có toàn quyền các module theo ghi chú, dù không có cột riêng. Nguồn `2. User Roles`, dòng 14–25.

| Role | Lead & phân bổ | Cơ hội & pipeline | Báo cáo & dashboard | S4-03 cần lưu ý |
|---|---|---|---|---|
| Sales Rep | `W*` | `W*` | `R*` | Không suy luận được quyền tạo chiến dịch; scope record cá nhân |
| Team Lead | `F` | `F` | `R*` | Tôn trọng phạm vi nhóm ở truy vấn liên quan |
| Director | `F` | `F` | `F` | Module toàn quyền |
| Marketing | `F` | `R` | `R*` | Có thể làm chủ thể Story nhưng **không viết opportunity** nhờ role Marketing; KPI phải xét scope |
| Cust. Success | `–` | `R*` | `R*` | Không tự cấp module Lead |
| Accountant | `–` | `R` | `R` | Không tự cấp module Lead |
| Admin | Toàn quyền theo chú thích | Toàn quyền | Toàn quyền | Vẫn áp auth/session, kiểm soát server và quyền riêng theo source |

**[CẦN CHỐT Q1]** Module “Chiến dịch” nằm dưới `Lead & phân bổ` hay `Báo cáo & dashboard`, role nào có quyền **CREATE_CAMPAIGN**, **VIEW_CAMPAIGN**, **VIEW_CAMPAIGN_TOTALS**? Đặc biệt: `Marketing R*` ở báo cáo không mặc nhiên được thống kê tiền của mọi cơ hội ngoài phạm vi. **Quyền UI và BE phải cùng phản ánh quyết định này**.

## 2. Quy ước chung cho tên, kiểu, null, thời gian, tiền, trạng thái

**MỌI tên field/route trong phần này = [ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO].** Ký hiệu: **Required** = bắt buộc theo contract draft (không phải kết luận từ Excel); **Nullable** = cho phép null theo giả định draft cần duyệt.

| Tên logic | Kiểu từ HTML → Java → persistence | Required / Nullable | Ý nghĩa / điều kiện DRAFT |
|---|---|---|---|
| `campaignId` | form/query `String` số nguyên → Java `Long` (hoặc PK theo repo) | Ở thao tác chi tiết: required; ở attribution: **nullable theo Q3** | ID ổn định, do BE cấp; không tin ID client chỉ vì hợp lệ cú pháp |
| `name` | `String` → `String` | **Required draft** | Tên hiển thị để phân biệt chiến dịch; trim, không nhận chuỗi chỉ toàn space; giới hạn dài **CHƯA BIẾT** |
| `budgetAmount` | form `String` thập phân → Java `BigDecimal` → numeric/decimal tương ứng | **Required draft**; không null | Mock dùng `"12000000"`; `>=0` **đề xuất**; precision/scale/currency **CẦN CHỐT**; tuyệt đối tránh `double` |
| `currencyCode` | `String` ISO 4217 | **CHƯA BIẾT** có lưu cột hay dùng currency hệ thống | Mock giả định đơn tiền `VND`; không tự thêm cột `currency_code` |
| `startDate` | form `String` ISO → Java `LocalDate` | **Required draft** | `yyyy-MM-dd`, không chứa múi giờ; quy tắc ngày quá khứ **CHƯA BIẾT** |
| `endDate` | form `String` ISO → Java `LocalDate` | **Required draft** | `yyyy-MM-dd`, `endDate >= startDate` là validation đề xuất |
| `channelCode` | `String` | **Required draft** | Thuộc danh mục kênh được BE cho phép; các mã mock `EVENT`, `SOCIAL`, `SEARCH` **không phải enum hệ thống** |
| `leadCount` | Java `Long` / JSP render integer | `0` nếu không có | Số **lead distinct** liên kết chiến dịch sau quyền/phạm vi (đề xuất) |
| `opportunityCount` | Java `Long` / JSP render integer | `0` nếu không có | Số **opportunity distinct** liên kết chiến dịch sau quyền/phạm vi (đề xuất) |
| `closedWonAmount` | Java `BigDecimal` / JSP format tiền | `BigDecimal.ZERO` nếu không có | Tổng **actual closed amount** của opportunity Thắng sau quyền/scope, **Q4 CẦN CHỐT** |
| `actualClosedAmount` | Java `BigDecimal` của nghiệp vụ cơ hội | Khi opportunity đóng **Thắng**, bắt buộc theo `S5-05`; khi khác có thể null theo source | Trường nguồn logic, **không khẳng định có field cùng tên trong DB** |
| `opportunityState` | enum logic do module cơ hội định nghĩa | Có khi thống kê | `WON`, `OPEN`, `LOST` chỉ là **mã minh họa**, phải map sang source (ví dụ “Đóng Thắng/Thua”) |
| `flashMessage` | JSP request/session flash theo pattern repo | Nullable | Thông báo sau redirect; escape output |
| `fieldErrors` | `Map<String,String>` | Có thể map rỗng | Lỗi validation keyed theo chính **tên field form**; không lộ stack trace |
| `csrfToken` | chuỗi token theo cơ chế auth thực tế | Required nếu repo sử dụng CSRF | Không log/bỏ vào fixture public; token thật do server sinh, **không có secret trong mock** |
| `requestToken` | token chống duplicate POST theo cơ chế hiện hữu | Optional **đề xuất** | Không khẳng định đã có; nếu dùng, server phải xử lý idempotency thực sự |

**Quy tắc format [ĐỀ XUẤT]:** form ngày `yyyy-MM-dd` (ví dụ `2026-10-01`); JSP hiển thị ngày theo locale `vi-VN` (ví dụ `01/10/2026`) **sau khi xác minh chuẩn dự án**. Giá trị tiền lưu/tính bằng `BigDecimal` không dùng phân cách nhóm trong HTTP form/fixture (`"45000000"`); JSP hiển thị `45.000.000 ₫` trong giả định VND, **không trộn giá trị dự kiến với giá trị chốt**. Nếu có nhiều tiền tệ phải có chính sách chuyển đổi/không cộng khác tiền **CẦN CHỐT**. `null` nghĩa là **chưa gắn chiến dịch** (giả định Q3), `0` nghĩa là **đã tính và không có bản ghi/giá trị**; không đánh đồng null/0/empty string. `campaignId`/count dùng integer, tiền dùng **decimal string trong JSON fixture**, Java dùng `BigDecimal`.

**Quy tắc state [ĐỀ XUẤT]:** không sáng tác trạng thái vòng đời chiến dịch vì AC1 không đòi; chỉ dùng trạng thái opportunity **đã có** sau khi source được xác minh. Nếu sử dụng enum mock `WON/OPEN/LOST`, đây **không phải tên enum chính thức**. Kiểu response HTML `text/html; charset=UTF-8`; UTF-8 toàn bộ form/JSP.

## 3. Danh mục hành động FE–BE — contract theo từng AC

> **Toàn bộ URL bên dưới là ví dụ DRAFT, chưa đọc repo.** Chọn query `action=` để phù hợp Servlet/JSP cổ điển, không áp đặt REST. `contextPath` được lấy từ `request.getContextPath()`, **CHƯA BIẾT** ở runtime; không hard-code `/msys`.

| Mã hành động | AC | Method + URL **ĐỀ XUẤT** | Input form/query | Output dự kiến | Kiểm tra quyền/đầu ra lỗi |
|---|---|---|---|---|---|
| `CAM_LIST` | AC1, AC3 | `GET {contextPath}/marketing/campaigns?page=1&pageSize=20` | `page` integer ≥1; `pageSize` integer giới hạn theo repo | `forward` JSP danh sách; attrs `campaignRows`, `pagination`, `permissions`, `flashMessage` | Auth; quyền xem chiến dịch/KPI + scope; 400 nếu query sai hoặc đưa mặc định **theo quy tắc duyệt**; 403 nếu thiếu quyền |
| `CAM_NEW_FORM` | AC1 | `GET {contextPath}/marketing/campaign?action=new` | không | `forward` JSP form; `campaignForm`, `channelOptions`, `fieldErrors`, `permissions` | Auth + quyền tạo; 403 nếu không quyền |
| `CAM_CREATE` | AC1 | `POST {contextPath}/marketing/campaign?action=create` | `name`, `budgetAmount`, `startDate`, `endDate`, `channelCode`, token CSRF và requestToken nếu repo dùng | Thành công `303 See Other` hoặc redirect PRG theo repo; trang đích hiển thị flash thành công; sai form → forward lại với `fieldErrors` và dữ liệu cũ | Auth, quyền CREATE, CSRF, validate; không ghi DB nếu lỗi; 400/422 (chọn theo repo); 403, 409 xung đột nếu được quy định, 500 |
| `CAM_DETAIL` | AC1, AC3 | `GET {contextPath}/marketing/campaign?action=detail&id=101` | `id` integer hợp lệ | `forward` JSP chi tiết; attrs `campaign`, `metrics`, `permissions` | Auth; kiểm tra **record + metric scope**; 400 input sai; 404 không có/quy tắc che dữ liệu; 403 thiếu quyền |
| `LEAD_ASSOCIATE_CAMPAIGN` | AC2 | **Luồng POST tạo lead hiện có — URL CHƯA BIẾT** | Field logic `campaignId` nullable đi cùng payload lead hiện có; **không thay** trường bắt buộc nguồn lead của S4-02 | Phản hồi/redirect theo luồng tạo lead hiện tại; record lead giữ campaign link | Quyền tạo lead + quyền gắn campaign mục tiêu + scope; 400/403/404/409 theo lỗi; không tự tạo endpoint riêng |
| `OPP_INHERIT_CAMPAIGN` | AC2 | **Service chuyển lead → opportunity hiện có — URL CHƯA BIẾT** | `leadId` theo contract `S4-08`; `campaignId` ưu tiên **đọc từ lead trong DB** (đề xuất), không tin tham số client | Cơ hội mới chứa liên kết campaign nguồn theo quyết định Q3; chuyển đổi nhiều bước atomic | Quyền chuyển đổi/đọc lead/ghi opportunity; transaction rollback; 403/404/409/500 theo lỗi |
| `OPP_DIRECT_CAMPAIGN` | AC2 | **Luồng tạo opportunity trực tiếp hiện có — URL CHƯA BIẾT** | Cách chọn/kế thừa `campaignId` khi cơ hội không từ lead: **CHƯA BIẾT** | Opportunity giữ attribution nếu có; không tự cấp sửa opportunity cho Marketing | Theo module opportunity và Data Scope; **BLOCKED cho tới Q3/Q5 được duyệt** |

**[GIẢI THÍCH]** Không có contract upload/export mới cho S4-03. Excel import thuộc `S4-02`, export báo cáo thuộc `S8-09`; nếu cần thêm cột chiến dịch khi import, phải cùng chủ Story chốt mapping, báo lỗi từng dòng và quyền trước khi tích hợp. Không có JSON endpoint được yêu cầu; bảng status HTTP nói rõ hành vi dự kiến của Servlet/JSP, không tuyên bố đây là route thực tế.

### 3.1. `CAM_CREATE`: field form, xử lý và response cụ thể

**[ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO]** Request HTML ví dụ (các giá trị đều giả):

```http
POST /<contextPath>/marketing/campaign?action=create HTTP/1.1
Content-Type: application/x-www-form-urlencoded; charset=UTF-8

name=Su+kien+thang+10&budgetAmount=12000000&startDate=2026-10-01&endDate=2026-10-31&channelCode=EVENT
```

Trong request thực **phải** kèm cơ chế session/CSRF của repo; ví dụ trên cố tình **không in token hoặc secret**. `name` trong HTTP form là ASCII minh họa cho khả năng encode; tên thật ở mock Unicode tiếng Việt.

1. Servlet đọc form/query, ép kiểu/validation (trường bắt buộc, `BigDecimal`, ISO date, kênh, chặn lỗi `NaN`/số thập phân bất thường), không nhận `ownerId` hoặc `role` từ client để quyết định quyền.
2. Service kiểm tra đăng nhập, quyền tạo module/chỉ tiêu campaign và Data Scope; chuẩn hóa dữ liệu; kiểm tra business invariant; gọi DAO persistence **trong DB thật của môi trường được cấp phép**, **không ghi MySQL chung chỉ để test**.
3. DAO dùng parameterized query, trả ID do DB sinh. Nếu thành công: PRG về `CAM_DETAIL` hoặc `CAM_LIST` (FE+BE chốt nơi điều hướng), flash “Đã tạo chiến dịch”. Nếu validation sai: forward lại form với `campaignForm`, `fieldErrors`, `channelOptions`; giữ giá trị cũ, không sinh record.
4. Nếu double-submit: client disable button **và** server kiểm tra token/unique logic theo chính sách duyệt; **không tuyên bố 409 bắt buộc** nếu trùng tên vẫn được phép.

**Request attributes cho JSP form — DTO schema logic DRAFT:**

| Attribute | Kiểu Java **đề xuất** | Rỗng / nullable | FE dùng thế nào |
|---|---|---|---|
| `campaignForm` | `CampaignFormView` (`name:String`, `budgetAmount:String`, `startDate:String`, `endDate:String`, `channelCode:String`) | Lúc GET new chứa chuỗi rỗng; lúc validation fail giữ giá trị đã nhập đã sanitize | Bind form, không lấy dữ liệu mock khi ở production |
| `channelOptions` | `List<ChannelOption>` với `code:String`, `label:String`, `enabled:boolean` | Có thể rỗng; phải hiển thị lỗi cấu hình, không tự bịa kênh mặc định | Render `<select>` với `option`, disable submit nếu không có option hợp lệ |
| `fieldErrors` | `Map<String,String>` | Map rỗng khi chưa lỗi | Error ngay dưới field, `aria-describedby`; escape |
| `permissions` | `CampaignPermissions` (`canView:boolean`, `canCreate:boolean`, `canViewTotals:boolean`) | Required | Ẩn nút không có quyền; **BE vẫn kiểm tra lại** |
| `flashMessage` | `String` / hệ flash repo | Nullable | Success/error UI, escape |

**Ví dụ lỗi validation:** HTTP `400` hoặc `422` theo chuẩn repo (**chưa chốt**), forward cùng JSP tạo, `fieldErrors={"budgetAmount":"Ngân sách phải là số không âm.","endDate":"Ngày kết thúc không được trước ngày bắt đầu."}`; `campaignForm` giữ dữ liệu người nhập, **0 thao tác ghi DB**.

### 3.2. `CAM_LIST` và `CAM_DETAIL`: JSP attributes + aggregate

**[ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO]** DTO logic:

```text
CampaignView {
  campaignId: Long                // BE-assigned; required
  name: String                   // required after validation
  budgetAmount: BigDecimal       // required after validation, monetary amount
  currencyCode: String?          // nullable/no stored field if system-wide currency
  startDate: LocalDate           // required after validation
  endDate: LocalDate             // required after validation
  channelCode: String            // required after validation
  channelLabel: String?          // optional display, source channel catalog
}
CampaignMetricsView {
  campaignId: Long               // required
  leadCount: Long                // always >= 0
  opportunityCount: Long         // always >= 0
  closedWonAmount: BigDecimal    // >= 0 only if signed amount data rules guarantee this; negatives/adjustments CHƯA BIẾT
  currencyCode: String?          // required if multiple currency units are supported
}
CampaignRowView { campaign: CampaignView; metrics: CampaignMetricsView }
CampaignPagination { page: int; pageSize: int; totalItems: long; totalPages: long }
```

**JSP attrs `CAM_LIST`**: `campaignRows:List<CampaignRowView>` (rỗng `[]` khi không có), `pagination:CampaignPagination`, `permissions:CampaignPermissions`, `flashMessage:String?`, `errorMessage:String?` (lỗi truy vấn có thể dẫn sang error page tùy repo). **JSP attrs `CAM_DETAIL`**: `campaign:CampaignView`, `metrics:CampaignMetricsView`, `permissions:CampaignPermissions`; nếu campaign không tồn tại thì HTTP 404/forward trang lỗi, **không render null như campaign hợp lệ**.

**[ĐỀ XUẤT]** Khi phân trang, server đếm/tính các chỉ số từ toàn bộ dữ liệu được cấp phép của **mỗi** chiến dịch, không chỉ từ 20 lead đầu tiên. `page=1`, `pageSize=20` là **giá trị mock**, giới hạn thật theo source. Không load toàn bộ lead rồi tổng hợp trên trình duyệt. UI có loading cho request (nếu điều hướng SSR có thể dùng trạng thái nút), empty table, error page, tiền format `vi-VN` theo currency được duyệt.

### 3.3. Liên kết `campaignId` trong nghiệp vụ lead và opportunity

**[ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO]** Đây là **field logic bổ sung vào contract chung**, chưa khẳng định DB có cột `campaign_id`.

| Chỗ tích hợp | Input | Output/vết kiểm tra | Bảo mật & null |
|---|---|---|---|
| Tạo lead qua form hiện hữu | `campaignId: Long?` (có thể nhập `"101"` từ HTML select) | Lead đọc lại được `campaignId=101` | Nếu `null`, chỉ cho phép theo Q3; nếu khác null → campaign phải có và user có quyền gắn |
| Nhập lead từ Excel / webform | Mapping tới campaign nếu nguồn thật có; **CHƯA BIẾT cột/import/form có hỗ trợ** | Liên kết sau import phải chính xác nếu đường nhập có attribution | Lỗi theo dòng thuộc phạm vi Story chủ quản; không tự thêm cột file template |
| Convert lead `201` → opportunity | Đọc campaign từ bản ghi lead, không lấy tùy tiện từ client | Opportunity có campaign nguồn `101`, đối soát DB | Transaction cùng customer/contact/opp của luồng convert, chống convert trùng |
| Tạo opportunity không qua lead | Cách gán chiến dịch nguồn **CẦN CHỐT Q3** | Không được tự tạo liên kết vô căn cứ | Marketing `R` trên cơ hội, không cho tự sửa record ngoài quyền |
| Thống kê chiến dịch | Dùng record linkage đã lưu và trạng thái opportunity | `COUNT(DISTINCT leadId)`, `COUNT(DISTINCT opportunityId)`, tổng actual won | Query phải lọc **cả record cho phép tính KPI** theo Q1; tránh fan-out join |

**Đề xuất transaction/concurrency:** nếu luồng convert gồm nhiều insert/update, hoặc bước gán nguồn thất bại, rollback toàn bộ; nếu cùng lead được convert đồng thời thì giao ước `409 Conflict` hoặc cách báo lỗi hiện hành đã duyệt; nếu record có phiên bản cập nhật, dùng cơ chế optimistic locking hiện hữu. Không tự thêm unique index/SQL vào schema mà chưa được cho phép.

## 4. HTTP lỗi và định dạng thông báo chung (SSR)

| Tình huống | HTTP/control flow **ĐỀ XUẤT** | FE hiển thị | Quy tắc BE |
|---|---|---|---|
| Chưa đăng nhập / session hết | GET/POST SSR redirect sang login kèm thông báo; `401` nếu repo có API JSON | “Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.” | Không ghi DB; không hé dữ liệu quyền hạn |
| Thiếu quyền module | `403 Forbidden` + JSP lỗi | “Bạn không có quyền thực hiện thao tác này.” | Check permission server-side, không chỉ ẩn nút |
| Bản ghi ngoài Data Scope | `403` hoặc `404` theo policy che tồn tại được duyệt | Tiếng Việt, không cho thấy thông tin campaign/số tiền | Scope áp ngay query, không query tất cả rồi lọc ở JSP |
| Không có ID hoặc ID sai định dạng | `400 Bad Request` | “Yêu cầu không hợp lệ.” | Validate và không query bằng chuỗi trực tiếp |
| Campaign không tồn tại | `404 Not Found` | “Không tìm thấy chiến dịch.” | Không gửi DTO giả |
| Sai form | `400`/`422` + forward form | `fieldErrors` keyed theo tên field, giữ dữ liệu nhập | Không ghi DB; không echo HTML độc hại |
| Gửi trùng / cập nhật đồng thời | `409 Conflict` **nếu chính sách idempotency/unique có hiệu lực** | “Dữ liệu đã thay đổi hoặc yêu cầu được gửi trùng. Vui lòng tải lại.” | Không tạo duplicate khi contract cấm; đừng áp 409 khi trùng tên vẫn được phép |
| DB/network/server lỗi | `500` (hoặc trang lỗi an toàn) | “Có lỗi xảy ra. Vui lòng thử lại.” | Rollback, log server không chứa secret/PII, không in stacktrace ra JSP |
| Không có dữ liệu | `200 OK` | `0/0/0`, “Chưa có dữ liệu”, không báo 404 | Trả list rỗng và zero rõ nghĩa |

**Thông báo/JSON errors:** tài liệu này **không định nghĩa public JSON API**. Nếu repo thực sự dùng fetch/AJAX, FE + BE phải thống nhất riêng cấu trúc như `{ "errorCode":"VALIDATION_ERROR", "message":"...", "fieldErrors": {"budgetAmount":"..."}}` và các status 400/401/403/404/409/500 **trước khi code**. Chuỗi ở đây chỉ là **gợi ý mô tả**, không khẳng định BE đã trả JSON.

## 5. Bộ MOCK CHUNG — tất cả là dữ liệu GIẢ, KHÔNG chứa thông tin người thật

> **[MOCK]** Các ID, tên chiến dịch, ngày, mã kênh, số tiền, quyền và trạng thái bên dưới hoàn toàn do tài liệu sinh, **không phải dữ liệu MySQL hiện có**. Fixture JSON được dùng để FE/BE thống nhất **giá trị DTO/request attributes**; **không phải JSON endpoint**. Khi đi qua Java/JSP, các tiền chuỗi decimal chuyển thành `BigDecimal`, các ngày thành `LocalDate`.

### MOCK 01 — Thành công: tạo và xem thống kê có lead/opportunity (AC1+AC2+AC3)

**Dữ liệu logic nguồn cho DAO mock/in-memory (không chèn DB production):**

```json
{
  "campaigns": [
    {"campaignId": 101, "name": "Sự kiện sản phẩm tháng 10", "budgetAmount": "12000000", "currencyCode": "VND", "startDate": "2026-10-01", "endDate": "2026-10-31", "channelCode": "EVENT"},
    {"campaignId": 102, "name": "Giới thiệu trực tuyến", "budgetAmount": "3500000", "currencyCode": "VND", "startDate": "2026-10-05", "endDate": "2026-10-25", "channelCode": "SOCIAL"}
  ],
  "leads": [
    {"leadId": 201, "campaignId": 101, "testLabel": "Lead giả 01"},
    {"leadId": 202, "campaignId": 101, "testLabel": "Lead giả 02"},
    {"leadId": 203, "campaignId": 101, "testLabel": "Lead giả 03"}
  ],
  "opportunities": [
    {"opportunityId": 301, "originLeadId": 201, "campaignId": 101, "opportunityState": "WON", "expectedAmount": "60000000", "actualClosedAmount": "45000000", "signedOn": "2026-10-12"},
    {"opportunityId": 302, "originLeadId": 203, "campaignId": 101, "opportunityState": "OPEN", "expectedAmount": "30000000", "actualClosedAmount": null, "signedOn": null}
  ]
}
```

**Request attributes `CAM_LIST` tương ứng để FE render (mô tả JSON fixture, không phải HTTP response):**

```json
{
  "campaignRows": [
    {"campaign": {"campaignId": 101, "name": "Sự kiện sản phẩm tháng 10", "budgetAmount": "12000000", "currencyCode": "VND", "startDate": "2026-10-01", "endDate": "2026-10-31", "channelCode": "EVENT", "channelLabel": "Sự kiện"}, "metrics": {"campaignId": 101, "leadCount": 3, "opportunityCount": 2, "closedWonAmount": "45000000", "currencyCode": "VND"}},
    {"campaign": {"campaignId": 102, "name": "Giới thiệu trực tuyến", "budgetAmount": "3500000", "currencyCode": "VND", "startDate": "2026-10-05", "endDate": "2026-10-25", "channelCode": "SOCIAL", "channelLabel": "Mạng xã hội"}, "metrics": {"campaignId": 102, "leadCount": 0, "opportunityCount": 0, "closedWonAmount": "0", "currencyCode": "VND"}}
  ],
  "pagination": {"page": 1, "pageSize": 20, "totalItems": 2, "totalPages": 1},
  "permissions": {"canView": true, "canCreate": true, "canViewTotals": true},
  "flashMessage": "Đã tạo chiến dịch.",
  "errorMessage": null
}
```

**Kiểm tra tính đúng:** campaign `101`: **3 lead** (`201`,`202`,`203`); **2 cơ hội** (`301`,`302`); đóng Thắng đúng **45.000.000 VND** từ `301`; cơ hội `302` đang Mở có `expectedAmount 30.000.000` **không** được cộng vào giá trị đã chốt. Campaign `102`: `0 / 0 / 0`. Đây là kết quả **theo quy tắc DRAFT Q3/Q4**; có thể đổi khi nhóm duyệt.

**JSP có thể hiển thị dự kiến:**

| Chiến dịch | Kênh | Ngân sách | Thời gian | Số lead | Số cơ hội | Giá trị đã chốt |
|---|---|---:|---|---:|---:|---:|
| Sự kiện sản phẩm tháng 10 | Sự kiện | 12.000.000 ₫ | 01/10/2026–31/10/2026 | 3 | 2 | **45.000.000 ₫** |
| Giới thiệu trực tuyến | Mạng xã hội | 3.500.000 ₫ | 05/10/2026–25/10/2026 | 0 | 0 | **0 ₫** |

### MOCK 02 — Rỗng: chưa có chiến dịch / chiến dịch chưa có lead (AC1+AC3)

```json
{
  "campaignRows": [],
  "pagination": {"page": 1, "pageSize": 20, "totalItems": 0, "totalPages": 0},
  "permissions": {"canView": true, "canCreate": true, "canViewTotals": true},
  "flashMessage": null,
  "errorMessage": null
}
```

**Expected FE:** trang hiển thị “Chưa có chiến dịch”, nút “Tạo chiến dịch” chỉ khi `canCreate=true`; **không** tự bịa bản ghi demo, không báo lỗi. Trường hợp campaign **đã có nhưng chưa có lead** dùng campaign `102` ở MOCK 01 để chứng minh `0 / 0 / 0`.

### MOCK 03 — Form sai validation: ngân sách âm, khoảng ngày đảo chiều (AC1)

**Input form mock** `name="Sự kiện thử"`, `budgetAmount="-100"`, `startDate="2026-10-31"`, `endDate="2026-10-01"`, `channelCode="EVENT"`.

**DTO attrs forward JSP — không phải JSON response HTTP:**

```json
{
  "campaignForm": {"name": "Sự kiện thử", "budgetAmount": "-100", "startDate": "2026-10-31", "endDate": "2026-10-01", "channelCode": "EVENT"},
  "channelOptions": [{"code": "EVENT", "label": "Sự kiện", "enabled": true}, {"code": "SOCIAL", "label": "Mạng xã hội", "enabled": true}, {"code": "SEARCH", "label": "Tìm kiếm", "enabled": true}],
  "fieldErrors": {"budgetAmount": "Ngân sách phải là số không âm.", "endDate": "Ngày kết thúc không được trước ngày bắt đầu."},
  "permissions": {"canView": true, "canCreate": true, "canViewTotals": true},
  "flashMessage": null
}
```

**Expected:** không insert; lưu lại các giá trị đã nhập dưới form để sửa, lỗi hiển thị dưới trường. Thử thêm trường `name="  "`, `channelCode="INVALID"`, `budgetAmount="abc"` hoặc ngày sai format; server phải từ chối theo quy tắc cuối cùng được phê duyệt. Không khẳng định “cấm trùng tên” vì AC không nêu.

### MOCK 04 — Thiếu quyền / dữ liệu ngoài phạm vi (AC1+AC2+AC3)

**Giả thiết test**: session hợp lệ thuộc role không có quyền truy cập trang chiến dịch (ví dụ `Accountant` không có module Lead & phân bổ theo matrix); request `GET /marketing/campaign?action=detail&id=101` bị chặn.

```json
{
  "httpStatus": 403,
  "view": "error/forbidden.jsp (tên trang ĐỀ XUẤT)",
  "errorCode": "FORBIDDEN",
  "message": "Bạn không có quyền xem chiến dịch này.",
  "campaign": null,
  "metrics": null
}
```

**Expected:** không trả KPI/amount, không tạo lead/opportunity, **không** bypass được bằng cách gọi URL trực tiếp. Kết quả JSON trên chỉ mô tả **nội dung dự kiến của error view**, không có nghĩa Servlet trả JSON. Nếu user được xem một campaign nhưng không được xem doanh số cơ hội ngoài scope, `canViewTotals=false` hoặc server aggregate đã lọc theo quyền; **policy phải chốt Q1 trước**, không gán mặc định cho role Marketing mọi số tiền.

### MOCK 05 — Biên đặc trưng: 1 lead sinh nhiều cơ hội / join làm nhân đôi (AC2+AC3)

**Bổ sung fixture độc lập** để test aggregate:

```json
{
  "campaignId": 103,
  "leads": [
    {"leadId": 204, "campaignId": 103}
  ],
  "opportunities": [
    {"opportunityId": 303, "originLeadId": 204, "campaignId": 103, "opportunityState": "WON", "actualClosedAmount": "20000000"},
    {"opportunityId": 304, "originLeadId": 204, "campaignId": 103, "opportunityState": "LOST", "actualClosedAmount": null}
  ],
  "activitiesOnLead": [
    {"activityId": 9001, "leadId": 204},
    {"activityId": 9002, "leadId": 204}
  ],
  "expectedMetrics": {"campaignId": 103, "leadCount": 1, "opportunityCount": 2, "closedWonAmount": "20000000"}
}
```

**Expected:** `leadCount=1`, `opportunityCount=2`, `closedWonAmount=20000000` **dù join 2 activity**; opportunity Thua không cộng giá trị chốt. **[CẦN CHỐT]** chính sách “một lead sinh nhiều cơ hội” nếu module chuyển đổi giới hạn 1 cơ hội/lead: dùng fixture này để **thử tính tổng theo quan hệ giả định**, không khẳng định workflow cho phép tạo như vậy. Nếu không có quan hệ hợp lệ, thay case này bằng hai lead độc lập và cùng chiến dịch; vẫn bắt buộc kiểm tra chống double-count khi join.

### MOCK 06 — Liên kết không hợp lệ / xung đột khi convert (AC2)

```json
{
  "testInput": {"leadId": 201, "campaignId": 999999},
  "expectedError": {"httpStatus": 404, "errorCode": "CAMPAIGN_NOT_FOUND", "message": "Không tìm thấy chiến dịch phù hợp.", "fieldErrors": {"campaignId": "Chiến dịch không tồn tại hoặc không được phép sử dụng."}},
  "expectedPersistence": {"leadCampaignLinkCreated": false, "opportunityCreated": false}
}
```

**Expected:** validate tham chiếu chiến dịch trước khi lưu; account ngoài scope không suy ra sự tồn tại của campaign; nếu convert thất bại giữa transaction thì rollback toàn bộ thay đổi nghiệp vụ. `404` vs `403` cho record ngoài scope và `409` cho click convert đồng thời là **ĐỀ XUẤT, CẦN CHỐT theo repo**.

## 6. Phương án thay mock FE bằng dữ liệu BE thật (không code trong giai đoạn này)

1. **Cùng duyệt contract:** FE+BE/leader thống nhất các câu **Q1–Q5** ở tài liệu A; xác minh route/form field/DTO/JSP attr/schema/state hiện tại; ghi thay đổi và version vào Decision Log. Nếu source không phù hợp, phân loại `MATCH` / `ADAPT` / `ALREADY IMPLEMENTED` / `BLOCKED`; không tự sửa một phía.
2. **FE mock chỉ để dựng UI:** FE dùng cùng shape `CampaignRowView`/fixture thành công, rỗng, lỗi validation, cấm quyền, biên. Không lưu fixture thành dữ liệu production, không show số giả ở demo nghiệm thu.
3. **BE kết nối dữ liệu thật:** Servlet/Service/DAO hiện hữu hoặc được duyệt lấy campaign + link lead/opp + actual closed value; áp module RBAC/scope **trước** aggregate, query có tham số, không tự điều chỉnh schema SQL. MySQL runtime, H2 cách ly cho test khi được cấu hình đúng.
4. **FE chuyển nguồn dữ liệu:** JSP đọc đúng `request.setAttribute` từ Servlet hoặc API đã chốt; bỏ/disable injection fixture; button/filter/form truyền đúng **field names**; xử lý `400/401/403/404/409/500`, PRG, empty/0/null.
5. **Tích hợp và đối soát:** trình duyệt Developer Tools → Network xem **request method/path/form params** và status/redirect; phía server log có che secret; kiểm tra request attributes/render; dùng DB **test cách ly** truy vấn record thật và đối soát `3/2/45000000`; test quyền 2 tài khoản khác scope. Nếu `CAM_DETAIL` hiện tại khác proposal, sửa **cả** FE/BE contract cùng lúc trước triển khai.

**Ranh giới an toàn:** Không chạy seed/migration trên MySQL chung, không tự `ALTER/CREATE/DROP`, không sửa `src/main/resources/db/*.sql`, `pom.xml`, config mật khẩu, shared security khi chưa duyệt; không suy diễn H2 setup an toàn nếu chưa đọc source. Không dùng mock để báo AC PASS.

## 7. Bộ contract test FE–BE và điều kiện ĐẠT (chưa chạy)

| Test ID | AC | Setup/request | Expected quan sát + DB test | Hiện trạng |
|---|---|---|---|---|
| `CT-01` | AC1 | Marketing authorized POST đủ trường hợp lệ | Redirect PRG/flash đúng; record campaign có ngân sách/ngày/kênh đọc lại khớp | **CHƯA CHẠY** |
| `CT-02` | AC1 | POST thiếu name/kênh hoặc budget âm, ngày đảo | Forward lỗi từng field, giữ input, 0 insert | **CHƯA CHẠY** |
| `CT-03` | AC1 | POST không login/role không quyền, fake CSRF | 401/login/403 theo chuẩn đã duyệt, 0 insert, không lộ giá trị | **CHƯA CHẠY** |
| `CT-04` | AC1 | Hai POST nhanh cùng form | Không duplicate ngoài chính sách idempotency/unique chốt; message nhất quán | **BLOCKED: Q2** |
| `CT-05` | AC2 | Tạo lead gắn campaign 101 → query DB test | Đọc lại `lead.campaignId=101` qua layer thật | **CHƯA CHẠY** |
| `CT-06` | AC2 | Convert lead 201 có campaign 101 | Opportunity giữ `campaignId=101`; rollback toàn bộ nếu 1 bước lỗi | **BLOCKED: Q3/Q5, source** |
| `CT-07` | AC2 | ID campaign 999999, ID ngoài Data Scope, convert cùng lúc | Validation/403/404/409 phù hợp policy, 0 link trái quyền/không trạng thái dở | **BLOCKED: Q3/Q5** |
| `CT-08` | AC3 | Fixture campaign 101: 3 lead, 2 opp (1 WON actual 45M + 1 OPEN 30M expected) | `3 / 2 / 45.000.000`, không cộng expected | **BLOCKED: Q4** |
| `CT-09` | AC3 | Campaign 102 không lead; danh sách rỗng | Zero `0/0/0` và empty state, không null/NPE | **CHƯA CHẠY** |
| `CT-10` | AC3 | Fan-out: 1 lead, 2 opp, 2 activities | `1/2/20.000.000`; không nhân tổng theo số hoạt động | **CHƯA CHẠY** |
| `CT-11` | AC3 | Marketing chỉ có scope hạn chế; test Sales Rep/Team Lead/Director cùng records | Không leak counts/money; kết quả theo policy đúng role và record | **BLOCKED: Q1** |
| `CT-12` | AC3 | ≥10.000 lead/campaign; pageSize 20; nhiều concurrent GET | Đo thời gian thực, ghi p50/p95, kiểm tra đúng tổng và page; backlog **không có ngưỡng bắt buộc riêng** | **CHƯA ĐO** |
| `CT-13` | AC1–3 | Mobile 360px, keyboard, error flash, tên dài/xss test | Form/3 KPI dễ đọc; escaped content; role ẩn nút nhưng BE cũng chặn | **CHƯA CHẠY** |

**Không có kết quả PASS/FAIL thực nghiệm**, vì chưa được cấp repo, DB test hoặc quyền chạy E2E. Mock/unit-style fixtures **chỉ xác định expected**, không được gọi là acceptance evidence.

## 8. Danh sách URL/field/table/enum/service đề xuất, hoàn toàn CHƯA XÁC MINH

| Nhóm | Tên ví dụ cần tìm/đối chiếu | Tình trạng |
|---|---|---|
| URL | `/marketing/campaigns`, `/marketing/campaign?action=new`, `?action=create`, `?action=detail&id=...` | **ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO** |
| Field/form/DTO | `campaignId`, `name`, `budgetAmount`, `currencyCode`, `startDate`, `endDate`, `channelCode`, `fieldErrors`, `campaignRows`, `metrics` | **ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO** |
| Model/DTO | `CampaignView`, `CampaignFormView`, `CampaignMetricsView`, `CampaignRowView`, `CampaignPermissions`, `CampaignPagination` | **ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO** |
| DB logic | Thực thể chiến dịch; liên kết chiến dịch trên lead và opportunity; actualClosedAmount/state/signDate của opportunity | Chỉ **mô hình logic**, **không khẳng định** có bảng/cột |
| Tên bảng SQL ví dụ để thảo luận | `campaigns`, `leads`, `opportunities`; cột ví dụ `campaign_id`, `actual_closed_amount` | **ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO**; **không được tạo/ALTER từ tài liệu này** |
| Enum ví dụ | `EVENT`, `SOCIAL`, `SEARCH`; `WON`, `OPEN`, `LOST` | **MOCK/ĐỀ XUẤT**, chưa biết mã thực |
| Service/DAO | `CampaignService`, `CampaignDao`, attribution trong `LeadService`/`OpportunityService`, `CampaignMetricsQuery` | **ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO**, không tạo class trùng với source |
| Schema/migration | Tên PK, FK, indexed columns, unique constraints, precision money, scope columns | **CHƯA BIẾT**, cần đọc SQL/schema; thay đổi bắt buộc có duyệt riêng |
| Test/environment | CI/test Maven, H2 isolated, MySQL test riêng, app JSP locale/CSRF | **CHƯA XÁC MINH**; không khởi chạy test ghi vào DB chung |

## 9. Câu hỏi nghiệp vụ CẦN CHỐT và quyết định phiên bản

| ID | CẦN CHỐT | Lựa chọn đề xuất | Hệ quả đối với contract |
|---|---|---|---|
| `Q1` | Quyền tạo/xem chiến dịch và KPI tổng; Marketing `R*` báo cáo có được xem tiền ngoài scope? | Lọc đúng scope; hoặc đặc quyền aggregate riêng được leader phê duyệt | `permissions`, `CAM_LIST/DETAIL`, aggregate query, test 403 |
| `Q2` | Name/budget/currency/date/channel: bắt buộc, validate, cho phép trùng/sửa/xóa? | Tên required, ngân sách ≥0, ngày hợp lệ, kênh danh mục; VND chỉ là giả định | Form, DTO, errors, constraints, double-submit |
| `Q3` | Attribution: 1 hay nhiều chiến dịch, null được không, cơ hội convert kế thừa hay được đổi? | 1 source ID nullable, convert kế thừa từ lead | `campaignId` & model/relationship, convert transaction, KPI |
| `Q4` | Closed value: giá trị thắng thực tế hay hợp đồng, ngày ghi nhận/tiền tệ/mở lại? | Giá trị chốt thực tế của opportunity đóng Thắng (`S5-05`) | `closedWonAmount`, trạng thái/BigDecimal, KPI tests |
| `Q5` | Những luồng tạo lead/import/web/conversion/opp hiện tại có gì; ai sửa shared DTO/schema? | FE–BE/leader phân owner tích hợp sau khi kiểm tra repo | Phạm vi thay đổi, blocker/contract, tính an toàn schema |

**[ĐÃ ĐƯỢC NGƯỜI DÙNG XÁC NHẬN]** Leader Ngô Trung Kiên đã duyệt cả hai tài liệu ngày 14/10/2026. **[CHƯA ĐƯỢC CUNG CẤP]** Câu trả lời và lựa chọn cụ thể Q1–Q5; không xem phê duyệt tài liệu là đã chốt ngầm các đề xuất kỹ thuật/nghiệp vụ. Nhánh do người dùng cung cấp, chưa xác minh Git.

### Ô QUYẾT ĐỊNH / APPROVAL GATE

| Trường | Giá trị hiện tại |
|---|---|
| **Trạng thái tài liệu contract** | **APPROVED — theo xác nhận người dùng về việc leader đã duyệt; các quyết định Q1–Q5 chưa được cung cấp** |
| Story / version | `S4-03` / `0.1` |
| Nguồn nghiệp vụ | `4. Product Backlog!A44:J44`, `2. User Roles!A14:H25` |
| Người duyệt FE | **CHƯA BIẾT / CHƯA DUYỆT** |
| Người duyệt BE | **CHƯA BIẾT / CHƯA DUYỆT** |
| Mentor/leader xác nhận (nếu yêu cầu) | **Ngô Trung Kiên — người dùng xác nhận đã duyệt** |
| Ngày duyệt | **14/10/2026** |
| Q1–Q5 / Decision Log cuối cùng | **CHƯA CÓ NỘI DUNG QUYẾT ĐỊNH ĐƯỢC CUNG CẤP**; giữ `BLOCKED` ở những phần contract phụ thuộc; khi nhận quyết định ghi lựa chọn/lý do/người/ngày chốt |
| Các khác biệt repo so với contract | **CHƯA BIẾT** — phải lập danh sách `MATCH`/`ADAPT`/`ALREADY IMPLEMENTED`/`BLOCKED` |
| Phạm vi phê duyệt | **Leader duyệt hai tài liệu theo xác nhận của người dùng; không xác nhận đã thẩm định riêng FE/BE, nội dung Q1–Q5 hoặc source** |

---

**Đã cập nhật `APPROVED` cho tài liệu theo thông báo phê duyệt từ người dùng.** Có thể chuyển **cả hai file** sang Antigravity để **đọc/đối chiếu repo**; không tự code các phần ảnh hưởng contract, quyền hoặc schema còn `BLOCKED` bởi Q1–Q5. Prompt 02 yêu cầu rà soát các quyết định quan trọng và dừng ở bất kỳ sai khác chưa được leader/FE–BE chốt.


---

## 10. Kết quả tái đối chiếu Excel để xem xét hợp đồng (14/10/2026)

**[ĐÃ ĐỐI CHIẾU BACKLOG; TÀI LIỆU ĐÃ ĐƯỢC LEADER DUYỆT THEO XÁC NHẬN CỦA NGƯỜI DÙNG]** Workbook xác nhận S4-03 (dòng 44) có đúng 3 AC: (1) khai báo ngân sách/thời gian/kênh; (2) lead và cơ hội giữ liên kết chiến dịch nguồn; (3) xem số lead, số cơ hội, giá trị đã chốt theo chiến dịch. `2. User Roles` xác nhận Marketing `F` cho Lead, `R` cho Cơ hội và `R*` cho Báo cáo; kiểm tra `*` phải đặt phía server. `S5-05` xác nhận thuộc tính nghiệp vụ giá trị chốt **thực tế** của cơ hội Thắng.

| Phần contract | Mức độ xác nhận | Hệ quả đối với bản DRAFT |
|---|---|---|
| `CAM_CREATE` / ngân sách – thời gian – kênh | **[BACKLOG GỐC]** Có thao tác và 3 nhóm dữ liệu AC1. | `name`, URL, form field, required rules, `BigDecimal`, VND, code kênh là **[ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO]**. |
| Liên kết `campaignId` cho lead/opportunity | **[BACKLOG GỐC]** Bắt buộc giữ được mối liên kết nguồn (AC2); không yêu cầu cụ thể tên/cột DB. | `Long?`, quan hệ một nguồn, null cho lead ngoài chiến dịch, convert kế thừa đều là **[ĐỀ XUẤT]**; chưa chốt cardinality hay owner tích hợp. |
| `leadCount`, `opportunityCount` | **[BACKLOG GỐC]** Cần hiển thị 2 số theo từng chiến dịch (AC3). | `COUNT(DISTINCT ...)` để tránh đếm trùng là **[ĐỀ XUẤT]**; lọc quyền, trạng thái record/soft-delete cần đối chiếu repo. |
| `closedWonAmount` | **[BACKLOG GỐC]** AC3 có “giá trị đã chốt”; `S5-05` có “giá trị chốt thực tế” cho Thắng. | `SUM(actualClosedAmount)` của cơ hội Thắng là **[ĐỀ XUẤT MẠNH]**, chưa quyết định chuẩn trạng thái/tiền tệ/kỳ; không tính hóa đơn kế toán. |
| `canViewTotals` & Data Scope | **[BACKLOG GỐC]** Marketing R* báo cáo, chỉ được xem theo phạm vi dữ liệu. | **[CẦN CHỐT Q1]** chính sách tổng hợp KPI khi chiến dịch Marketing tạo nhưng cơ hội thuộc Sales. Tạm không xuất số của record ngoài scope; không âm thầm trao quyền tổng hợp xuyên scope. |
| Servlet/JSP, mẫu lỗi, mock và các route | Không có dữ kiện về source từ workbook. | **[ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO]**; mock chỉ là fixture, không chứng minh AC PASS. |

**Kết luận contract:** Các *mục tiêu dữ liệu* khớp Excel; **trạng thái tài liệu = APPROVED (Ngô Trung Kiên, 14/10/2026, theo xác nhận của người dùng)**. Tuy vậy, **lựa chọn cụ thể Q1–Q5 và xác nhận FE/BE riêng chưa được cung cấp**, vì thế các giả định kỹ thuật vẫn là **ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO**; những thay đổi có thể ảnh hưởng interface, quyền hoặc DB phải được chốt rõ trước khi code. Phê duyệt tài liệu **không** có nghĩa mock là dữ liệu thật, đã test PASS hoặc mọi quyết định kỹ thuật đã được xác nhận.
