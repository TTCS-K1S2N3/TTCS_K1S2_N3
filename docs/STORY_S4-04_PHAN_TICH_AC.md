# MSys CRM — STORY S4-04 | PHÂN TÍCH STORY & ACCEPTANCE CRITERIA

> **TRẠNG THÁI: APPROVED — ĐÃ PHÊ DUYỆT.** Tài liệu phân tích cho Story S4-04 đã được Leader Ngô Trung Kiên chính thức phê duyệt ngày 15/10/2026. Nhánh Git chính thức: `feature/S4-04-canh-bao-gop-lead-trung`.
>
> **Nguồn sự thật nghiệp vụ:** `HỆ THỐNG QUẢN LÝ KHÁCH HÀNG (1)(3).xlsx`, sheet **`4. Product Backlog` dòng 45** và **`2. User Roles` dòng 3–25**; đối chiếu `3. Epics` dòng 7, `5. Sprint Plan` dòng 7, 14–24. **Nguồn kỹ thuật:** Repo MSys CRM - Java Jakarta Servlet/JSP + MySQL 8.4 LTS.

## 1. Trích nguyên văn Story theo workbook

| Trường | Giá trị [BACKLOG GỐC] |
|---|---|
| Story ID | **S4-04** |
| Sprint | **4** |
| Epic | **EP-04** — **Lead & Phân bổ** (`3. Epics!A7:C7`) |
| Vai trò trong Story | **Nhân viên Marketing** |
| User Story (nguyên văn) | **Là Nhân viên Marketing, tôi muốn được cảnh báo và gộp lead trùng, để không để hai nhân viên cùng gọi một người trong một buổi sáng.** |
| Points | **5** |
| Ưu tiên | **Must** |
| Trạng thái backlog tại thời điểm file | **Đã bắt đầu triển khai Backend** |
| Ghi chú / người thực hiện | **Hứa Lan Hương(FE)**; **Thào A Khua(BE)** |
| Tên Story riêng | **Cảnh báo và gộp lead trùng** |
| Nhánh feature chính thức | **`feature/S4-04-canh-bao-gop-lead-trung`** (đã xác nhận trên remote origin, tách từ commit mới nhất của `dev`) |

### Acceptance Criteria — nguyên văn, không mở rộng phạm vi

- **AC1 [BACKLOG GỐC]:** Phát hiện trùng theo email, số điện thoại và tên công ty
- **AC2 [BACKLOG GỐC]:** Lead trùng với khách hàng đã có được gợi ý gắn thẳng vào khách hàng đó
- **AC3 [BACKLOG GỐC]:** Gộp giữ nguyên lịch sử của cả hai bản ghi

**[GIẢI THÍCH]** “Phát hiện”, “gợi ý gắn” và “gộp” là **ba hành động khác nhau**. Trùng là tín hiệu cần xử lý; không được tự xoá lead, tự hợp nhất khách hàng, hoặc suy đoán rằng cùng tên công ty là cùng một người. AC3 đòi bảo toàn lịch sử, không chỉ giữ vài cột hồ sơ. Workbook **chưa nêu** ngưỡng so khớp, quyền bấm gộp, chiến lược dữ liệu thắng khi xung đột, quy tắc đối với bản ghi khác phạm vi và định nghĩa đầy đủ “lịch sử”.

## 2. Vai trò, RBAC và phạm vi dữ liệu

**[BACKLOG GỐC]** `2. User Roles`: 7 vai trò `Sales Rep`, `Team Lead`, `Director`, `Marketing`, `Cust. Success`, `Accountant`, `Admin`. Dòng 14 định nghĩa **F = toàn quyền**, **W = ghi trong phạm vi được giao**, **R = chỉ xem**, **– = không truy cập**. Dấu `*` nghĩa là **chỉ dữ liệu mình sở hữu**, hoặc **dữ liệu nhóm mình quản lý đối với Team Lead**. Việc lọc này phải ở **tầng server/query**, không chỉ ẩn nút FE. `Admin` toàn quyền theo ghi chú (không có cột riêng). Chỉ Director xem giá vốn/biên lợi nhuận (không liên quan trực tiếp AC, vẫn không lộ dữ liệu ngoài scope).

| Vai trò | Lead & phân bổ (row 18) | Khách hàng & liên hệ (row 17) | Hoạt động & lịch làm việc (row 20) | Ý nghĩa khi làm S4-04 |
|---|---|---|---|---|
| Sales Rep | `W*` | `W*` | `W*` | Lead/khách/hoạt động **trong phạm vi**; không suy diễn có quyền gộp lead xuyên ownership. |
| Marketing | `F` | `W` | `W` | Chủ thể Story, quyền module Lead rộng; **chưa chắc** được nhìn hoặc gộp bản ghi ngoài phạm vi dữ liệu thực tế. |
| Cust. Success | `–` | `W*` | `W*` | Không cấp quyền thao tác Lead chỉ vì có quyền với khách hàng. |
| Accountant | `–` | `R` | `–` | Không cấp quyền Lead. |
| Team Lead | `F` | `F` | `F` | Có quyền module rộng; cần đối chiếu scope và chính sách duyệt gộp. |
| Director | `F` | `F` | `F` | Quyền module rộng, vẫn xác minh server. |
| Admin | toàn quyền (ghi chú) | toàn quyền | toàn quyền | Không bỏ auth, audit, validation và kiểm tra tính toàn vẹn. |

**[GIẢI THÍCH]** Quyền `F` module **không tự động** là quyền xem số điện thoại/email/chi tiết của mọi bản ghi trong hệ thống. Duplicate checking có nguy cơ **rò dữ liệu**: việc nhập một email vào form không được trả lại thông tin lead/khách hàng không được phép đọc. **[CẦN CHỐT]** ai được thấy ứng viên, xem thông tin được che/mask, thực hiện merge và link khách hàng; tách “được biết có trùng” khỏi “được xem chi tiết”.

## 3. Phạm vi và các dependency thật sự

**IN SCOPE [BACKLOG GỐC + GIẢI THÍCH]:** kiểm tra/gợi ý trùng trên ba tín hiệu; gợi ý liên kết tới **khách hàng đã tồn tại**; thao tác gộp hai lead với lịch sử còn nguyên; kiểm soát quyền trên tất cả luồng và test bảo toàn dữ liệu.

**OUT OF SCOPE [GIẢI THÍCH]:** tự động merge không hỏi người dùng; thuật toán ML/fuzzy AI tùy ý; gộp trực tiếp hai **khách hàng** (đã thuộc Story `S3-04`); xoá hàng loạt; chuyển lead thành cơ hội (Story `S4-08`); CRM import (Story `S4-02`); tạo chiến dịch (Story `S4-03`); xây lại RBAC; schema migration khi chưa được phê duyệt.

**Dependency / điểm tích hợp [BACKLOG GỐC]:**

- `S4-01` (thu thập lead từ web) và `S4-02` (tạo/nhập Excel lead): các điểm **phát sinh lead** nên có kiểm tra trùng theo cách đồng bộ đã chốt; **không tự sửa** các luồng này nếu thuộc Story khác/chưa thống nhất.
- `S3-04` (gộp **khách hàng** trùng) yêu cầu so sánh và giữ người liên hệ/cơ hội/hoạt động, riêng gộp khách hàng chỉ Team Lead trở lên. **Không áp dụng ngầm hạn chế đó cho gộp lead**; chỉ tham chiếu khi đội chốt và tái sử dụng kỹ thuật thích hợp.
- `S4-03` liên kết lead/cơ hội với chiến dịch: merge phải tránh làm mất liên kết/attribution; chính sách khi hai lead có chiến dịch khác nhau **CẦN CHỐT**.
- `S4-08` chuyển lead thành khách hàng + cơ hội, lưu hoạt động và đặt lead “Đã chuyển đổi”: cần ngăn merge đồng thời với conversion hoặc merge lead đã chuyển đổi theo chính sách được duyệt.
- `S1-05` RBAC + Data Scope: kiểm tra cả danh sách gợi ý **và** thao tác gộp/link trên server. Không suy diễn code sẵn có từ trạng thái backlog.

## 4. Phân tích sâu theo AC

### AC1 — Phát hiện trùng theo email, số điện thoại và tên công ty

**Ai / ở đâu / đầu ra.** [BACKLOG GỐC] Marketing cần biết lead nào có dấu hiệu trùng. [ĐỀ XUẤT] Khi nhập/sửa/xem lead hoặc từ danh sách “Nghi trùng”, FE hiển thị cảnh báo và **danh sách ứng viên** có căn cứ (`EMAIL`, `PHONE`, `COMPANY`), chỉ hiện thuộc tính người dùng có quyền xem. Không đồng nhất “trùng” với “phải gộp”.

**Điều kiện, input, quy tắc.** Đăng nhập hợp lệ; có dữ liệu lead để đối sánh. Logic định danh `leadId`/trường `email`, `phone`, `companyName` **đề xuất**. [CẦN CHỐT] email chuẩn hóa trim/lowercase (không bỏ phần trước dấu `+` nếu chưa quy định); số điện thoại xử lý khoảng trắng/ký tự định dạng/mã quốc gia Việt Nam; tên công ty chuẩn hóa khoảng trắng/hoa thường/dấu tiếng Việt/viết tắt đến mức nào. **[ĐỀ XUẤT]** tiêu chí riêng biệt: khớp email/phone chuẩn hóa → ứng viên mạnh; chỉ khớp tên công ty → **ứng viên cần xem xét**, không tự gộp. Giá trị `null`, `""`, chỉ có khoảng trắng **không tự khớp** với nhau. Trong một lượt phải loại lead hiện tại ra khỏi kết quả.

**Luồng chính:** nút/nhập thông tin trong JSP → GET/POST kiểm tra ứng viên theo contract repo → Servlet validate/auth → Service chuẩn hóa/so khớp/scope → DAO truy vấn index hoặc filter thích hợp theo schema thật, trả về danh sách ứng viên có phân trang → Servlet đặt request attributes → JSP hiển thị lý do trùng, nút “So sánh”, “Gợi ý gắn khách”, “Gộp” **chỉ khi có quyền**.

**Lỗi và cạnh tranh:** 0 ứng viên (empty), toàn null (không báo giả); email/phone sai cú pháp (validation, không match tùy tiện); tên công ty chung kiểu “Công ty TNHH” gây false positives; hàng nghìn ứng viên (phân trang/giới hạn; đo hiệu năng nếu có ngưỡng thống nhất); nhập lại cùng email trong 2 request gần nhau (cần transactional/double-check trước ghi); nguồn ngoài scope (không lộ email/số/ID qua API hoặc đếm suy đoán); token hết hạn 401, quyền 403, id không tồn tại 404, DB lỗi 500 an toàn. **Không cố định** chỉ số tốc độ khi workbook chưa có ngưỡng.

**FE UX:** cảnh báo màu dễ nhận nhưng không thay thế text, badge “Trùng email/số điện thoại/tên công ty”, kết quả phân trang, nút phụ “Bỏ qua cảnh báo” **chỉ nếu chính sách cho phép**, nút chính dẫn tới so sánh; loading/empty/error, bảng tràn ngang trên 360px cần card/overflow. Tránh hiển thị PII ngoài scope. **BE:** validation server, prepared statements, auth/module + Data Scope trước khi trả candidate, giới hạn request/tối ưu search, escape output, chống enumeration, kiểm soát dữ liệu nhạy cảm.

**Test có thể tái hiện:** `T1.1` lead A `demo01@example.test` và lead B `DEMO01@example.test` (không khoảng trắng sau trim) → thấy ứng viên trùng email, không tự merge; `T1.2` điện thoại định dạng có dấu cách/dấu gạch phù hợp chính sách chuẩn hóa → khớp phone; `T1.3` chỉ tên công ty giống → gợi ý với độ tin cậy **chưa chốt**, không tự merge; `T1.4` email/sđt/company null ở hai bản ghi → không sinh trùng giả; `T1.5` Sales Rep A không đọc được PII của lead B ngoài scope qua GET trực tiếp; `T1.6` nhập nhiều ứng viên → phân trang, không lỗi memory, chỉ kết luận hiệu năng khi đo; `T1.7` lỗi DB → thông báo tiếng Việt, không lộ SQL. **ĐẠT AC1** khi có bằng chứng đối sánh từng tín hiệu + false-positive/empty/scope test trên BE thật và người dùng nhìn được cảnh báo FE sau tích hợp.

### AC2 — Lead trùng với khách hàng đã có được gợi ý gắn thẳng vào khách hàng đó

**Ai / ở đâu / đầu ra.** [BACKLOG GỐC] Marketing nhận **gợi ý khách hàng hiện có**. [ĐỀ XUẤT] Danh sách “Có thể là khách hàng X” với lý do match và nút “Gắn với khách hàng” khi được cấp quyền; sau xử lý, lead liên kết đến khách tương ứng và có thể xem lại liên kết. **Không tự tạo khách hàng mới** trong AC2.

**Điều kiện, input, quy tắc.** Có lead và record khách hàng tồn tại, có đường liên kết logic trong DB hoặc service được duyệt; quyền đọc/ghi phù hợp cả Lead và Khách hàng. [CẦN CHỐT] mapping giữa email/phone/name company của lead với trường gì của khách hàng/contacts (do khách hàng doanh nghiệp có thể có nhiều contact); gắn một hay nhiều customer; gợi ý có cần xác nhận từ Team Lead; sau khi link có thay đổi trạng thái/owner của lead không. Không mặc định auto-link hoặc biến link thành convert.

**Luồng chính:** FE mở gợi ý → BE đọc lead + customer candidates được phép xem, chuẩn hóa cùng chính sách AC1 → hiển thị summary đã mask → người có quyền chọn một customer và xác nhận → POST validate quyền trên **cả leadId/customerId**, tồn tại và eligibility hiện tại → transaction tạo/cập nhật **liên kết logic** qua DAO (chỉ nếu schema/quan hệ đã tồn tại hoặc có kế hoạch migration được duyệt) → redirect thành công và hiển thị customer association. **Không nhận owner/role từ client để quyết định quyền.**

**Lỗi/biên:** Customer vừa bị xoá/merge hoặc ra ngoài scope; lead đã link với customer khác; email match khách hàng nhưng người liên hệ thuộc công ty khác; nhiều customer có cùng tên; gửi trùng POST; người dùng chỉ có quyền Lead mà không có quyền link Customer; CSRF, DB lỗi; dữ liệu không phù hợp → 400/403/404/409/500 có thông báo an toàn. **BE:** transaction nếu nhiều bước, ghi audit theo quy định repo nếu cần, tránh lộ dữ liệu khách hàng không thuộc scope. **UX:** hiện nguyên nhân gợi ý, trạng thái chưa gắn/đã gắn, dialog xác nhận ngắn, cho xem chi tiết trong phạm vi, disable khi loading.

**Test:** `T2.1` lead A và customer 801 giả cùng phone/email hợp lệ → customer xuất hiện trong gợi ý và liên kết thành công theo rule đã duyệt; `T2.2` customer khác scope → không lộ PII hoặc gắn trái phép; `T2.3` ID không tồn tại → 404/không ghi; `T2.4` lead đã liên kết customer khác → theo conflict policy đã chốt, không ghi đè im lặng; `T2.5` hai người gắn cùng lúc → một quyết định cuối nhất quán, không tạo hai quan hệ nếu chỉ một được phép; `T2.6` DB lỗi giữa transaction → rollback; `T2.7` verify record lead vẫn tồn tại, không tự kích hoạt luồng convert. **ĐẠT AC2** khi chứng minh gợi ý hợp lệ + link persistence thật theo schema và kiểm tra auth/scope trên môi trường test riêng.

### AC3 — Gộp giữ nguyên lịch sử của cả hai bản ghi

**Ai / ở đâu / đầu ra.** [BACKLOG GỐC] Khi gộp 2 lead trùng, **lịch sử của cả hai không được mất**. [ĐỀ XUẤT] Người có quyền vào trang so sánh nguồn/đích và xác nhận merge; hiển thị bản lead được giữ (survivor), có cách xem lịch sử đến từ lead đã gộp. Merge **khác** liên kết lead với customer; không âm thầm “xóa bản trùng” chỉ vì matching.

**Điều kiện, input, trạng thái.** Hai ID lead khác nhau, tồn tại, cùng loại, chưa merge vào nơi khác, eligibility/owner/scope hợp lệ, quyền `merge` **CẦN CHỐT**; xác định lead nào **giữ lại** (`target`), lead nào **nguồn** (`source`); các giá trị xung đột (email/phone/owner/status/company/campaign/customer relationship) có chính sách rõ trước khi commit. Không tự cho phép merge lead đã chuyển đổi / đang được nhân viên xử lý / thuộc nhóm khác. “Lịch sử” logic gồm timeline activity, ghi chú, tương tác, audit/provenance, contact/customer/campaign links và mối quan hệ (tuỳ source). Không bịa các bảng đang tồn tại.

**Luồng chính:** FE chọn ứng viên → GET preview trả hai bản lead kèm lịch sử tổng hợp **trong scope** → người thao tác xác nhận survivor/field winner theo chính sách → POST `merge` (token chống CSRF, idempotency, version nếu source có) → Servlet auth/validate → Service **recheck scope + version + sự hợp lệ** → DAO transaction: khóa 2 lead theo thứ tự nhất quán, lưu provenance/merge mapping, chuyển hoặc tham chiếu nguyên vẹn mọi bản ghi lịch sử/phụ thuộc được phép, cập nhật survivor theo quy tắc đã duyệt, đánh dấu source là merged/redirect alias **nếu repo hỗ trợ** → kiểm tra số quan hệ/trạng thái → commit hoặc rollback tất cả → PRG tới survivor và hiển thị “Đã gộp”. **Không DELETE vật lý source trừ khi có thiết kế và bằng chứng giữ nguyên toàn bộ lịch sử.**

**Lỗi/biên:** source=target; thiếu ID; một lead đã chuyển đổi; một lead đã được merge; đồng thời hai người merge A→B và B→A; lead tạo activity mới khi đang merge; foreign key cản merge; trùng idempotency token; thiếu quyền một trong hai ID; DB fail giữa chừng; liên kết campaign S4-03 xung đột; customer associations mâu thuẫn; activity timeline có nội dung nhạy cảm. **BE:** một transaction atomic, isolation/locking hoặc optimistic concurrency; không được tạo cycle; chuẩn hóa `merge-to` theo canonical target và read redirect policy; kiểm tra count + data integrity, lưu audit nếu repo có; mọi delete/archive bắt buộc nằm trong quyết định duyệt.

**UX:** trang so sánh 2 cột trên desktop, xếp dọc mobile, tô rõ “Giữ bản nào”; cho biết lịch sử nguồn không mất; cảnh báo không thể hoàn tác hoặc có thể hoàn tác **chỉ khi thực sự có khả năng**; nút chính “Xác nhận gộp”, phụ “Huỷ”; dialog xác nhận, spinner, lỗi cạnh trường/409 nếu stale, trang sau merge có merged timeline với chỉ báo nguồn.

**Test:** `T3.1` lead 101 có lịch sử `[A1,A2]`, lead 102 có `[B1,B2,B3]`, merge 102→101 → truy xuất survivor xem được đủ **5 mục phân biệt**, không mất nội dung/người tạo/thời gian; nguồn truy vết được theo policy; `T3.2` mất kết nối sau re-parent thứ nhất → rollback, cả hai lead + 5 hoạt động giữ nguyên; `T3.3` người không có quyền source/target → 403, không update; `T3.4` POST hai lần → không tạo bản ghi lịch sử lặp; `T3.5` merge đối nghịch đồng thời → một thành công, một 409 hoặc kết quả nhất quán theo rule; `T3.6` merge lead đã converted/merged → từ chối theo chính sách; `T3.7` campaign links khác nhau và customer link khác nhau → tuân thủ quy tắc chốt, không mất dấu nguồn; `T3.8` kiểm tra direct URL lead source sau gộp không hiển thị sai/để lộ PII. **ĐẠT AC3** khi đối chiếu trực tiếp DB test: total events và quan hệ trước/sau, rollback và concurrency, timeline FE sau tích hợp; không chỉ unit test giả bằng mock.

## 5. Bảng mapping AC → hành vi → FE → BE/DB → quyền → test → câu hỏi

| AC | Diễn giải chính xác | Luồng FE | Luồng BE/DB dự kiến | Quyền và phạm vi | Test xác nhận | CẦN CHỐT |
|---|---|---|---|---|---|---|
| AC1 | Có cảnh báo lead có khả năng trùng theo 3 tín hiệu | Form/chi tiết lead → danh sách candidate có lý do/empty/loading | Chuẩn hoá → search theo scope → danh sách ứng viên (không tự ghi) | Auth + Lead Read; lọc cả candidate ở server | T1.1–T1.7 | Q1, Q2 |
| AC2 | Gợi ý gắn lead với customer hiện hữu | Gợi ý customer → xác nhận liên kết → xem trạng thái | Search customer dưới scope → validate cả 2 ID → transaction link theo schema | Lead quyền thích hợp **và** Customer quyền phù hợp | T2.1–T2.7 | Q2, Q4 |
| AC3 | Merge hai lead mà lịch sử cả hai được giữ lại | Preview 2 lead + history → xác nhận → survivor timeline | Recheck/lock → transaction chuyển/tham chiếu lịch sử, provenance → commit/rollback | Quyền merge, scope **cả source/target**; không rò PII | T3.1–T3.8 | Q2, Q3, Q5 |

## 6. Tối đa 5 câu hỏi BẮT BUỘC CHỐT trước khi code liên quan

1. **Q1 — Điều kiện xác định trùng?** Đề xuất chuẩn hóa email/phone, tên công ty là tín hiệu gợi ý; **không auto-merge**. Nếu yêu cầu khớp đủ cả 3/1 trong 3 hoặc fuzzy threshold khác, truy vấn/index/test thay đổi lớn. **CẦN CHỐT**.
2. **Q2 — Ai được thấy gợi ý và ai được thực hiện merge/link, đặc biệt xuyên ownership/team?** Đề xuất quyền tách `VIEW_DUPLICATE`, `LINK_EXISTING_CUSTOMER`, `MERGE_LEADS`, kiểm tra cả record và scope trên BE. Nếu merge chỉ Team Lead+, khác quyền `F` Marketing trong workbook và phải được Leader xác nhận rõ. **CẦN CHỐT**.
3. **Q3 — Giữ bản lead nào, xử lý field/owner/status xung đột và cơ chế lưu dấu source sau merge ra sao?** Đề xuất chọn survivor rõ ràng, mapping source→target, không hard-delete khi chưa đảm bảo mọi lịch sử/foreign key; ảnh hưởng transaction và schema. **CẦN CHỐT**.
4. **Q4 — Match lead với customer/contacts bằng trường nào, liên kết khách hàng dạng nào, ai được xác nhận?** Đề xuất gợi ý → xác nhận thủ công, không tự convert; nếu chưa có quan hệ trong schema thì thiết kế thay đổi DB cần phê duyệt riêng. **CẦN CHỐT**.
5. **Q5 — “Lịch sử” gồm những liên kết nào (activity, audit, campaign, customer, attribution...), và xử lý merge đồng thời với chuyển đổi/chỉnh sửa ra sao?** Đề xuất transaction atomic, test đếm sự kiện trước/sau, conflict 409 cho stale merge, giữ provenance. **CẦN CHỐT**.

**Decision Log — ĐÃ ĐƯỢC LEADER PHÊ DUYỆT (15/10/2026)**

| Quyết định | Trạng thái | Người quyết | Ngày | Nội dung được chốt |
|---|---|---|---|---|
| Q1 (Tiêu chí trùng) | APPROVED | Ngô Trung Kiên | 15/10/2026 | Chuẩn hóa email trim/lower, sđt bỏ ký tự định dạng & +84->0, tên công ty bỏ từ khóa pháp lý & so khớp tương đồng >=70% hoặc trùng khớp. Khớp >=1 tín hiệu hợp lệ là cảnh báo. Không auto-merge. Null/rỗng không khớp. |
| Q2 (Phân quyền & Scope) | APPROVED | Ngô Trung Kiên | 15/10/2026 | Kiểm tra quyền và Data Scope ở tầng server. Marketing (quyền F module Lead) và Team Lead trở lên được quyền gộp Lead và gắn khách hàng trong phạm vi được giao. Ứng viên ngoài scope che thông tin PII. |
| Q3 (Survivor & Bảo toàn) | APPROVED | Ngô Trung Kiên | 15/10/2026 | Chọn bản ghi đích (survivor) trên UI. Bản ghi nguồn cập nhật trang_thai='DA_GOP', gop_vao_lead_id=targetId. Tuyệt đối KHÔNG xóa vật lý. Ghi vết kiểm toán vào lich_su_gop_lead. |
| Q4 (Gắn Khách hàng) | APPROVED | Ngô Trung Kiên | 15/10/2026 | Gợi ý khách hàng theo tên công ty và người liên hệ (email/sđt). Xác nhận gắn cập nhật lead.khach_hang_chuyen_doi_id. Giữ nguyên trạng thái Lead, KHÔNG tự chuyển sang DA_CHUYEN_DOI. |
| Q5 (Bảo toàn lịch sử) | APPROVED | Ngô Trung Kiên | 15/10/2026 | Chuyển toàn bộ hoat_dong, cong_viec, co_hoi sang lead đích trong 1 Transaction atomic có rollback. Giữ nguyên người tạo/thực hiện gốc. Chặn gộp đối nghịch hoặc lead đã gộp (409 Conflict). |

## 7. Kiểm tra SOURCE BẮT BUỘC trước khi code — chưa thực hiện

- Xác minh **nhánh mới S4-04** có thật trên `origin` và repo sạch/an toàn; kiểm tra code S4-03 đã push trên **nhánh khác** không đồng nghĩa S4-04 đã chứa code đó (phải đối chiếu nguồn gốc dev/nhánh mới). Không tự merge S4-03 vào S4-04.
- Đọc README, CODING_RULES, DATABASE_RULES, PLAN, pom; kiểm tra framework Servlet/JSP hiện hữu và view flow, auth/filter/CSRF/RBAC + scope.
- Xem model Lead/Customer/Contact, ownership/team, bảng/fields chuẩn hoá email/phone/company, khoá và index; không tuyên bố có schema nào từ workbook.
- Xem DAO/Service nhập lead thủ công/import/web, tìm lead, convert lead, customer merge, activity/timeline, audit; dùng lại service hợp lệ, tránh trùng tên/chức năng.
- Lập inventory mọi FK/tham chiếu liên quan source/target để bảo toàn lịch sử; xác minh cơ chế `version`, soft-delete/merged alias, transaction isolation, optimistic locking có/không; không sửa SQL schema mặc định.
- Đối chiếu tất cả route/field trong file Contract Mock với code, nhãn `MATCH/ADAPT/ALREADY IMPLEMENTED/BLOCKED`; khác contract chung phải hỏi FE+BE/Leader trước khi một bên sửa.
- Trước bất kỳ Maven test nào phải chứng minh H2 cách ly hoàn toàn MySQL chung; **không INSERT/UPDATE/DELETE** MySQL runtime và không chạy setup/reset schema/seed thật.

## 8. Checklist nghiệm thu THỰC TẾ — CHƯA ĐÁNH DẤU PASS

- [ ] 3 AC trích đúng theo workbook, Q1–Q5 được chốt và ghi người/ngày duyệt; contract FE–BE được cùng nhóm xác nhận.
- [ ] Người dùng nhìn thấy cảnh báo đúng 3 tín hiệu, rỗng không báo giả, không lộ PII/ID ngoài scope.
- [ ] Gợi ý customer hiện hữu và chỉ người có quyền mới link được; không tạo customer mới ngoài AC.
- [ ] Merge đúng 2 bản lead, giữ đủ lịch sử của cả hai, theo mapping/field priority được duyệt.
- [ ] Thử rollback, concurrent merge/conversion, double-submit, lead đã merge/converted; không mất dữ liệu hoặc tạo bản ghi mồ côi.
- [ ] 401/403/404/409/500 không lộ thông tin nhạy cảm; bypass FE không qua được BE; truy vấn tham số hóa.
- [ ] H2 tests an toàn được chạy và ghi pass/fail/skipped; nếu cần test real FK/MySQL, chỉ dùng môi trường test riêng đã cấp quyền.
- [ ] Kiểm tra thực tế FE–BE–DB với JSP, màn 360px, empty/loading/error/confirmed state; không tuyên bố AC PASS từ mock.
- [ ] Điều kiện DoD theo `5. Sprint Plan` (review, unit test tầng service/độ phủ nhánh mới ≥60%, CI, staging, quyền server, responsive, PO nghiệm thu) được chứng minh bằng kết quả thực, không tự tick.

**Kết luận [CHƯA XÁC MINH]:** Đây là **phân tích DRAFT**, không có thông tin route, bảng, schema hoặc test của repo thực tế; nhánh Git cho S4-04 chưa được xác nhận; **chưa triển khai BE, chưa commit/push, chưa nghiệm thu**.
