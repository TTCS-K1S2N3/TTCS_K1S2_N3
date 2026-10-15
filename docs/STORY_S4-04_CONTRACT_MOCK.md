# MSys CRM — STORY S4-04 | CONTRACT FE–BE & MOCK CHUNG

> **QUYẾT ĐỊNH: APPROVED — ĐÃ PHÊ DUYỆT.** Hợp đồng FE–BE và Mock cho Story S4-04 đã được Leader Ngô Trung Kiên chính thức phê duyệt ngày 15/10/2026. Nhánh Git chính thức: `feature/S4-04-canh-bao-gop-lead-trung`. Nguồn nghiệp vụ: workbook `HỆ THỐNG QUẢN LÝ KHÁCH HÀNG (1)(3).xlsx`, `4. Product Backlog!A45:J45` và `2. User Roles` dòng 14–25. Kiến trúc: Jakarta Servlet/JSP + MySQL 8.4 LTS.

## 1. Contract dựa vào 3 AC nguyên văn

| AC | Nguyên văn [BACKLOG GỐC] | Mục đích contract [GIẢI THÍCH] |
|---|---|---|
| AC1 | Phát hiện trùng theo email, số điện thoại và tên công ty | Nhận input lead, tìm các ứng viên trùng qua 3 tín hiệu, trả lý do và chỉ dữ liệu nằm trong scope hợp lệ. |
| AC2 | Lead trùng với khách hàng đã có được gợi ý gắn thẳng vào khách hàng đó | Đề xuất bản ghi Customer hiện có rồi thực hiện **liên kết** lead→customer khi người có quyền xác nhận; không tự convert/merge customer. |
| AC3 | Gộp giữ nguyên lịch sử của cả hai bản ghi | Preview hai lead, POST merge hai lead trong một transaction, đảm bảo mỗi lịch sử gốc vẫn truy vết được và không mất quan hệ. |

**Nguyên tắc kỹ thuật:** Servlet/JSP monolith; ưu tiên `GET → Servlet → request.setAttribute → forward JSP`, `POST application/x-www-form-urlencoded → Servlet → Service → DAO → PRG redirect` theo pattern repo. Không ép REST/JSON hay đòi tạo AJAX endpoint mới. Truy vấn/commit thuộc server/service/DAO, **không có SQL trong JSP**. Auth + RBAC + Data Scope phải áp dụng tại **mọi truy vấn ứng viên và mọi POST**. `request.getContextPath()` tự tìm contextPath thật; các URL dưới đây chỉ là đề xuất.

**Đã biết từ Excel:** Marketing `Lead & phân bổ=F`, `Khách hàng & liên hệ=W`; Sales Rep `Lead=W*`, `Khách=W*`; Team Lead/Director `Lead=F`, `Khách=F`; Cust. Success `Lead=–`, Accountant `Lead=–`; Admin toàn quyền theo ghi chú. F/W/R/– **không thay thế kiểm tra record scope**. **CẦN CHỐT** quyền `MERGE`, `LINK_CUSTOMER`, chính sách ứng viên ngoài scope; không ngầm áp quy tắc merge **khách hàng** S3-04 lên merge **lead** S4-04.

## 2. Hợp đồng thao tác (DRAFT, không khẳng định route tồn tại)

| Action | AC | HTTP & URL **ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO** | Input | Output JSP / redirect | Quyền / DB logic / lỗi |
|---|---|---|---|---|---|
| `DUP_LIST` | AC1 | `GET {contextPath}/lead/duplicates?page=1` | `page:int ≥ 1`, filter theo contract repo | forward `/WEB-INF/views/lead/duplicate-list.jsp`; attrs `candidateRows`, `pagination`, `permissions`, `flashMessage` | Read lead, candidate scope; no match → `[]`; 401/403/500 |
| `DUP_CHECK_LEAD` | AC1, AC2 | `GET {contextPath}/lead/duplicate?action=check&leadId=101` | `leadId:long > 0` | forward `/WEB-INF/views/lead/duplicate-check.jsp`; attrs `lead`, `leadCandidates`, `customerCandidates`, `matchRules`, `permissions` | Read lead + candidate/customer scope; 400/401/403/404/500 |
| `DUP_PREVIEW` | AC3 | `GET {contextPath}/lead/duplicate?action=preview&sourceLeadId=102&targetLeadId=101` | Hai ID khác nhau, positive long | forward `/WEB-INF/views/lead/duplicate-preview.jsp`; `sourceLead`, `targetLead`, `historyPreview`, `fieldConflicts`, `permissions` | Read 2 leads + merge eligibility; 400/403/404/409 |
| `DUP_LINK_CUSTOMER` | AC2 | `POST {contextPath}/lead/duplicate?action=linkCustomer` | `leadId`, `customerId`, CSRF, optional `requestToken` | PRG về lead detail (URL thật **CHƯA BIẾT**), flash success; invalid → form + `fieldErrors` | Auth, có quyền trên **cả** lead/customer; update association atomically; 400/403/404/409/500 |
| `DUP_MERGE` | AC3 | `POST {contextPath}/lead/duplicate?action=merge` | `sourceLeadId`, `targetLeadId`, CSRF, `requestToken`/`expectedVersion` nếu repo hỗ trợ | PRG tới survivor/preview với `mergeResult`, flash; lỗi 400/403/404/409/500 | Quyền merge **cả 2** lead, transaction atomic, preserving history + provenance |
| `LEAD_CREATE_DUP_CHECK` | AC1 | **Luồng tạo/nhập Lead đang có — URL CHƯA BIẾT** | Email/phone/company từ form tạo lead và import S4-01/S4-02 | Gợi ý trùng theo cơ chế sẵn có/chốt; không cản lưu tùy tiện | Đối chiếu mã nguồn, chốt khi nào chạy check; không tự sửa luồng khác |

**[CHƯA XÁC MINH]** Các đường dẫn JSP/Servlet, nơi flash, pattern `action=`, tên form fields và transaction DAO thực tế đều chưa được cung cấp. **Không phải** yêu cầu bắt buộc tạo tất cả endpoint riêng; có thể map vào servlet/service hiện có nếu vẫn đáp ứng AC.

### 2.1. Schema DTO/fields logic (không tự khai đã có cột DB)

| Field / attribute | Kiểu **đề xuất** | Required / nullable | Semantics, validation, security |
|---|---|---|---|
| `leadId`, `sourceLeadId`, `targetLeadId` | `Long` (HTML params `String` → server parse) | ID required khi có thao tác | Positive; source != target; ID không trao quyền, BE recheck record + scope. |
| `customerId` | `Long` | required khi link | Phải thực sự tồn tại, được phép link, không tin ID client. |
| `email` | `String` | nullable | Trim/lowercase cho lookup **[ĐỀ XUẤT]**, giữ bản gốc hiển thị; thiếu email **không có match**. Không lộ/email ở candidate ngoài scope. |
| `phone` | `String` | nullable | Chuẩn hóa khoảng trắng/dấu phân cách/mã nước theo **Q1 CẦN CHỐT**. Không chuyển thành số nguyên (mất số 0 đầu). |
| `companyName` | `String` | nullable | Chuẩn hóa cơ bản **đề xuất**, công ty trùng tên chỉ là dấu hiệu, không khẳng định chung pháp nhân. |
| `candidateKind` | `String` enum logic `LEAD` \| `CUSTOMER` | required đối với candidate | Mã mock **không khẳng định enum thật**. |
| `matchReasons` | `List<String>` of `EMAIL`, `PHONE`, `COMPANY` | list có ≥1 khi match | Danh sách tín hiệu; `confidenceScore` **không phải trường bắt buộc**. |
| `maskedLabel`, `maskedContact` | `String` | có thể null | Chỉ render mức thông tin có quyền theo quyết định Q2. Không trả raw PII cho người ngoài scope. |
| `leadCandidates`, `customerCandidates` | `List<DuplicateCandidateView>` | never null (empty `[]`) | Phân trang/truy vấn đúng scope; null chỉ khi backend lỗi và có cờ error riêng. |
| `sourceLead`, `targetLead` | `LeadSummaryView` | required khi preview hợp lệ | Chỉ thuộc tính được phép xem; dữ liệu có thể stale trước POST, BE kiểm tra lại. |
| `historyPreview` | `List<HistoryItemView>` / summary counts | empty `[]` hợp lệ | `eventId`, `sourceLeadId`, `type`, `occurredAt`, `summary` giả; **không khẳng định** có các cột tương ứng trong DB. |
| `fieldConflicts` | `List<FieldConflictView>` | empty `[]` khi không có | `fieldCode`, `sourceValue`, `targetValue`, `resolvedBy` chỉ sau Q3; không cho FE tự quyết field winner. |
| `mergeResult` | `MergeResultView` | có khi thành công | `survivorLeadId`, `mergedSourceLeadId`, `historyBeforeCount`, `historyAfterCount` (counts dùng cho đối soát, không đảm bảo persisted). |
| `permissions` | `DuplicatePermissions` | nonnull | `canViewCandidates`, `canLinkCustomer`, `canMerge` booleans do BE cấp sau auth/scope; FE chỉ để render nút. |
| `fieldErrors` | `Map<String,String>` | empty `{}` hợp lệ | Key trùng tên form field; lỗi tiếng Việt, không in SQL/stack trace. |
| `flashMessage` | `String` | nullable | Nội dung an toàn; escape ở JSP; hệ flash thực tế theo repo. |
| `requestToken` | `String` | **CHƯA BIẾT** (phụ thuộc repo) | Idempotency chống click 2 lần nếu được duyệt và hỗ trợ; không dùng token tĩnh trong production. |
| `expectedSourceVersion`, `expectedTargetVersion` | `Long` | **CHƯA BIẾT** | Optimistic concurrency **chỉ nếu entity/source hỗ trợ**; phương án thay thế locking DB. |
| `occurredAt` | `Instant/OffsetDateTime` server logic | required trên item có timestamp | Trong fixture dùng `2026-10-10T09:10:00+07:00`; khi render theo timezone app; **không tự bịa column**. |

**Giá trị trống/null:** `null` của email/phone/company là **không có tiêu chí đó**, không coi hai null là trùng. `[]` là kiểm tra xong và không có ứng viên; 0 lịch sử là kết quả hợp lệ, **không** tự suy diễn DB lỗi. IDs form invalid → 400; unknown record → 404 hoặc 403 masking theo chính sách Q2 (không tiết lộ sự tồn tại trái quyền). Mọi tên field và enum ở phần 2.1 là **đề xuất**.

**Date/format:** thời điểm là ISO 8601 có offset trong mock (`2026-10-10T09:10:00+07:00`), render ngày theo quy ước dự án sau khi đối chiếu. Số lượng record dùng số nguyên không âm. **Không có tiền tệ bắt buộc trong Story S4-04**, tuyệt đối không thêm ngân sách/khoản tiền vào request gộp. String/Unicode UTF-8; không đưa `ownerId`/`role`/`isAdmin` từ client làm căn cứ phân quyền.

## 3. Luồng chính và mẫu response server-render (ưu tiên JSP)

**[ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO]**

```text
Browser/JSP → GET kiểm tra lead trùng → Servlet → auth/scope → Service chuẩn hóa
→ DAO tìm lead và customer đủ quyền → request attributes → forward JSP
→ người dùng kiểm tra cảnh báo → POST link customer HOẶC GET preview merge
→ POST merge có xác nhận → Servlet (auth/CSRF/validate) → Service recheck scope + concurrency
→ DAO/transaction giữ lịch sử/provenance → COMMIT/ROLLBACK → Servlet PRG → JSP hiển thị kết quả
```

**POST link customer ví dụ (chỉ minh họa request, không có secret):**

```http
POST /<contextPath>/lead/duplicate?action=linkCustomer HTTP/1.1
Content-Type: application/x-www-form-urlencoded; charset=UTF-8

leadId=101&customerId=801
```

**POST merge ví dụ (nhấn xác nhận, không thực thi thật):**

```http
POST /<contextPath>/lead/duplicate?action=merge HTTP/1.1
Content-Type: application/x-www-form-urlencoded; charset=UTF-8

sourceLeadId=102&targetLeadId=101
```

Các ví dụ không bao gồm CSRF/session để **không lộ secret**; request thực phải dùng cơ chế auth/CSRF của repo. Ở `DUP_MERGE`, server **phải** kiểm tra lại quyền cả hai lead, bản ghi chưa được merge/chuyển đổi xung đột, sau đó transaction atomically update/copy/repoint history **theo repo**, không được thực hiện `DELETE` trực tiếp khi chưa có phương án bảo toàn mọi quan hệ.

### 3.1. HTTP status và mẫu lỗi

| Tình huống | HTTP dự kiến | JSP/form / message | DB phải thế nào |
|---|---|---|---|
| GET thành công | 200 | forward JSP, attrs được khai báo rõ | Read only |
| POST thành công | 303 See Other / redirect PRG theo repo | Flash success, GET survivor | Transaction commit một lần |
| Input rỗng/invalid/source=target | 400 (hoặc 422 theo chuẩn repo) | `fieldErrors` tiếng Việt, giữ form | 0 writes |
| Chưa đăng nhập | 401 hoặc redirect login theo filter repo | Không lộ ứng viên | 0 writes |
| Không có quyền / khác scope | 403 (hoặc 404 che existence theo chính sách) | “Bạn không có quyền thực hiện thao tác này.” | 0 writes |
| Lead/customer không tồn tại | 404 | “Không tìm thấy bản ghi hợp lệ.” | 0 writes |
| Đã merge, link sai, stale version, merge xung đột | 409 | “Dữ liệu đã thay đổi. Tải lại trước khi tiếp tục.” | 0 partial writes |
| Lỗi DB/timeout/transaction | 500 | “Không thể hoàn tất, vui lòng thử lại.”, có trace server an toàn | Rollback đầy đủ |

**Nếu thực sự cần AJAX:** **chưa có chứng cứ** bắt buộc phải dùng; sau khi xem repo có thể thống nhất `GET {contextPath}/lead/duplicate/candidates?leadId=101` với JSON `{"leadCandidates":[],"customerCandidates":[]}` và lỗi `{"error":{"code":"FORBIDDEN","message":"Không có quyền truy cập."}}`. **Chỉ triển khai nếu FE/BE cùng quyết định**, không song song tạo hai API khác logic. JSON example này hợp lệ, `[]` là mảng chứ không phải null. API cũng phải áp cùng auth/scope và giới hạn trả dữ liệu.

## 4. MOCK DATA CHUNG — HOÀN TOÀN GIẢ / KHÔNG PHẢI PERSISTENCE THẬT

Mọi `leadId`, `customerId`, email `.test`, tên “Demo”, thời gian, lịch sử dưới đây đều là **dữ liệu dựng để FE render và BE test fixture**, không chứa dữ liệu khách thực hay token. Mock không chứng minh AC đã xong.

### Mock A — thành công: phát hiện lead trùng + gợi ý customer (AC1, AC2)

```json
{
  "lead": {
    "leadId": 101,
    "displayName": "Lead Demo A",
    "email": "contact01@example.test",
    "phone": "0900000000",
    "companyName": "Công ty ABC Demo"
  },
  "leadCandidates": [
    {
      "candidateKind": "LEAD",
      "recordId": 102,
      "maskedLabel": "Lead Demo B",
      "maskedContact": "co***01@example.test",
      "matchReasons": ["EMAIL", "PHONE"],
      "canViewDetail": true
    }
  ],
  "customerCandidates": [
    {
      "candidateKind": "CUSTOMER",
      "recordId": 801,
      "maskedLabel": "Công ty ABC Demo",
      "maskedContact": null,
      "matchReasons": ["COMPANY"],
      "canViewDetail": true
    }
  ],
  "permissions": {
    "canViewCandidates": true,
    "canLinkCustomer": true,
    "canMerge": false
  },
  "fieldErrors": {}
}
```

**JSP mock tương ứng:** `request.setAttribute("lead", leadView101); request.setAttribute("leadCandidates", List.of(leadCandidate102)); request.setAttribute("customerCandidates", List.of(customerCandidate801)); request.setAttribute("permissions", permissions); request.setAttribute("fieldErrors", Map.of());` **Chỉ ở fixture/test hoặc dùng để dựng giao diện**, không đặt trong JSP production. `canMerge:false` ở ví dụ này minh họa **quyền riêng theo Q2 chưa chốt**; không kết luận Marketing mặc định không được merge.

### Mock B — rỗng: không có ứng viên trùng (AC1)

```json
{
  "lead": {"leadId": 103, "displayName": "Lead Demo Mới", "email": null, "phone": null, "companyName": "Công ty Demo Khác"},
  "leadCandidates": [],
  "customerCandidates": [],
  "permissions": {"canViewCandidates": true, "canLinkCustomer": false, "canMerge": false},
  "fieldErrors": {}
}
```

**FE:** hiện “Chưa phát hiện bản ghi trùng”; không hiển thị gợi ý giả. **BE:** null/empty không tạo match; nếu không có quyền, không đánh tráo thành empty thành công.

### Mock C — sai validation: source và target trùng ID (AC3)

```json
{
  "httpStatus": 400,
  "fieldErrors": {
    "sourceLeadId": "Lead nguồn phải khác lead được giữ lại.",
    "targetLeadId": "Lead được giữ lại phải khác lead nguồn."
  },
  "mergeResult": null
}
```

**BE expected:** 0 DB writes, không ghi thiếu một phần.

### Mock D — thiếu quyền / Data Scope (AC1–AC3)

```json
{
  "httpStatus": 403,
  "error": {
    "code": "FORBIDDEN",
    "message": "Bạn không có quyền truy cập hoặc thao tác trên bản ghi này."
  },
  "leadCandidates": null,
  "customerCandidates": null,
  "mergeResult": null
}
```

**Đây là mẫu dữ liệu cho test harness hoặc AJAX nếu được duyệt**, không tuyên bố ứng dụng Servlet bắt buộc trả JSON. JSP lỗi render từ server; **không trả** raw email/customer/ID ngoài scope trong error message. **Khác với empty `[]` thành công**.

### Mock E — biên: merge giữ lịch sử (AC3)

```json
{
  "sourceLead": {"leadId": 102, "displayName": "Lead Demo B", "status": "NEW_MOCK"},
  "targetLead": {"leadId": 101, "displayName": "Lead Demo A", "status": "NEW_MOCK"},
  "historyPreview": [
    {"eventId": 1001, "sourceLeadId": 101, "type": "NOTE", "occurredAt": "2026-10-10T09:10:00+07:00", "summary": "Ghi chú Demo A1"},
    {"eventId": 1002, "sourceLeadId": 101, "type": "CALL", "occurredAt": "2026-10-11T10:00:00+07:00", "summary": "Cuộc gọi Demo A2"},
    {"eventId": 1003, "sourceLeadId": 102, "type": "NOTE", "occurredAt": "2026-10-12T08:05:00+07:00", "summary": "Ghi chú Demo B1"},
    {"eventId": 1004, "sourceLeadId": 102, "type": "CALL", "occurredAt": "2026-10-12T14:00:00+07:00", "summary": "Cuộc gọi Demo B2"},
    {"eventId": 1005, "sourceLeadId": 102, "type": "NOTE", "occurredAt": "2026-10-13T16:30:00+07:00", "summary": "Ghi chú Demo B3"}
  ],
  "mergeResult": {
    "survivorLeadId": 101,
    "mergedSourceLeadId": 102,
    "historyBeforeCount": 5,
    "historyAfterCount": 5
  }
}
```

**Expected:** sau transaction, xem lịch sử survivor vẫn đủ 5 ID `1001..1005`, có thể truy vết lead gốc theo chính sách Q3/Q5; không nhân đôi, không bỏ mất timeline/quan hệ. `NEW_MOCK`, `NOTE`, `CALL` chỉ là **nhãn ví dụ**, không khẳng định enum DB. Kiểm tra trong DB test, mock preview **không phải** chứng cứ AC3 PASS.

### Mock F — biên: hai request merge đồng thời hoặc đối nghịch (AC3)

```json
{
  "httpStatus": 409,
  "error": {
    "code": "MERGE_CONFLICT",
    "message": "Một lead đã thay đổi trong khi bạn thao tác. Vui lòng tải lại và kiểm tra kết quả."
  },
  "mergeResult": null
}
```

**Expected:** yêu cầu xung đột không tạo side effects, không có cycle A→B→A, không làm mất lịch sử. **Giải pháp locking/idempotency bắt buộc phải đối chiếu repo/DB**, không suy đoán có cột `version`.

### Mock G — gợi ý gắn customer thành công (AC2)

```json
{
  "leadId": 101,
  "suggestedCustomerId": 801,
  "matchReasons": ["COMPANY"],
  "linkResult": {
    "leadId": 101,
    "customerId": 801,
    "linked": true
  },
  "flashMessage": "Đã gắn lead với khách hàng hiện có."
}
```

**Expected:** DB test có quan hệ lead↔customer như schema thực/thiết kế đã được duyệt; **không tự tạo customer hoặc cơ hội, không tự đổi lead sang trạng thái Đã chuyển đổi**. Nếu công ty chỉ cùng tên nhưng không cùng pháp nhân, người dùng cần xác nhận đúng customer.

## 5. Kế hoạch thay mock bằng dữ liệu thật và contract test FE–BE

1. FE dựng trang JSP bằng `List<DuplicateCandidateView>`, `LeadSummaryView`, `fieldErrors`, `permissions` giả **chỉ trong fixture/test**; không hard-code danh sách demo vào JSP production.
2. BE xem repo (Model, DAO, Service, Servlet, Filter, JSP hiện có) và schema **chỉ đọc**, lập bảng mapping field **đề xuất** ↔ field **thực tế**: `MATCH`, `ADAPT`, `ALREADY IMPLEMENTED`, `BLOCKED`. Với `ADAPT` ảnh hưởng interface, cùng FE/BE/Leader chốt rồi sửa tài liệu trước khi code.
3. BE trả request attributes thật từ Service/DAO khi thực hiện GET; JSP bind vào các attr được duyệt; phân biệt `null` / `[]` / lỗi 403; không render mock khi tích hợp.
4. Xác nhận method/URL/action/form params/kểu `Long`, encoding UTF-8, auth/CSRF, lỗi 400/401/403/404/409/500, PRG/forward; FE có thể kiểm tra request/response qua Network tab browser và log server **không chứa PII/token**.
5. Contract tests (trên H2 cách ly, không MySQL chung): (i) khớp email, phone, company và false positive; (ii) null/empty; (iii) forbidden cross-scope; (iv) customer suggestions/link; (v) merge giữ toàn bộ 5 events, các FK/links, audit provenance; (vi) double submit; (vii) rollback do DAO lỗi giữa nhiều bước; (viii) conflict 409 khi concurrent merge/convert; (ix) JSP có attr đúng/PRG; (x) serialization JSON chỉ nếu dùng AJAX thật.
6. Integration/E2E với DB thử nghiệm độc lập đã được nhóm cho phép; kiểm tra FK và locking trên cùng engine/schema phù hợp runtime nếu H2 không thể đại diện cho MySQL; Leader/PO nghiệm thu AC sau kiểm chứng, không đánh PASS từ mock/unit tests một phía.

## 6. Danh sách đề xuất CHỜ ĐỐI CHIẾU REPO

**Routes/JSP/attributes:** `/lead/duplicates`, `/lead/duplicate`, `action=check|preview|linkCustomer|merge`, `/WEB-INF/views/lead/duplicate-list.jsp`, `/WEB-INF/views/lead/duplicate-check.jsp`, `/WEB-INF/views/lead/duplicate-preview.jsp`, `lead`, `leadCandidates`, `customerCandidates`, `historyPreview`, `permissions`, `fieldErrors`, `flashMessage`.

**DTO/classes/services (tên logic):** `DuplicateCandidateView`, `LeadSummaryView`, `HistoryItemView`, `MergeResultView`, `DuplicatePermissions`, `LeadDuplicateService`, `LeadMergeService`, `LeadDAO`, `CustomerDAO`, `HistoryDAO`; không tạo lớp trùng nếu repo đã có. **Bảng/cột DB** liên quan lead, customer, contact, activities, audit/provenance, customer link, merge mapping, campaign association chỉ là **nhóm dữ liệu logic**, **KHÔNG biết tên thực tế**. Đặc biệt **chưa xác nhận** `merged_into_id`, `version`, `customer_id`, hay bảng `lead_history` có tồn tại; không tự ALTER/CREATE/DROP.

**Security/behavior:** auth session, quyền module + Data Scope query-level, masking PII, CSRF, idempotency/concurrency, transaction rollback, parameterized queries, không hard-delete source khi chưa duyệt. Cần có tài liệu mapping quan hệ `lead→activities/notes/conversion/campaign/customer` trước AC3.

## 7. Ô QUYẾT ĐỊNH / XÁC NHẬN

| Mục | Giá trị |
|---|---|
| Trạng thái contract | **APPROVED** |
| Người duyệt | **Ngô Trung Kiên** |
| Ngày duyệt | **15/10/2026** |
| FE được giao theo workbook | Hứa Lan Hương |
| BE được giao theo workbook | Thào A Khua |
| Nhánh feature chính xác / remote confirmed | **`feature/S4-04-canh-bao-gop-lead-trung`** |
| Q1 Phát hiện trùng / normalization | **APPROVED** (khớp >=1 tiêu chí email/phone/tên công ty, không auto-merge) |
| Q2 Quyền merge/link & Data Scope | **APPROVED** (Marketing & Team Lead trở lên theo data scope) |
| Q3 Survivor / conflict / provenance | **APPROVED** (chọn survivor UI, source DA_GOP, lưu lich_su_gop_lead) |
| Q4 Customer link & match mapping | **APPROVED** (gắn vào khach_hang_chuyen_doi_id, giữ nguyên trạng thái lead) |
| Q5 History coverage & concurrent ops | **APPROVED** (chuyển hoat_dong, cong_viec, co_hoi trong 1 transaction atomic) |
| Kết quả source đối chiếu | **MATCH** (Schema MySQL đã có sẵn đầy đủ cột và bảng liên quan) |
| Code / Tests / FE–BE integration | **Đang tiến hành triển khai Backend** |

**Hợp đồng FE–BE đã được phê duyệt chính thức bởi Leader Ngô Trung Kiên ngày 15/10/2026. Backend tiến hành triển khai theo đúng contract và các quyết định Q1–Q5.**
