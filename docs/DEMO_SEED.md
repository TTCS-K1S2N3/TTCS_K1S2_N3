# CRM - Du lieu demo local/dev

Bo du lieu mau nam trong `src/main/resources/db/seed_demo.sql`. Script nap la `scripts/seed-demo.ps1`.

## Pham vi

Tao them 12 san pham/dich vu, 40 khach hang, 80 nguoi lien he, 40 lead,
28 co hoi (mo/thang/thua), 28 dong san pham co hoi, 60 hoat dong,
12 bao gia + 12 phien ban + 12 chi tiet bao gia, 5 hop dong + chi tiet.
Ngoai ra bo sung pipeline + giai doan, ly do thang/thua, nganh va quy mo phuc vu lien ket.

Moi ban ghi demo nghiep vu su dung ma `DEMO-*` hoac email `@example.com`.
Data scope duoc gan qua user seed Sales Rep/Team Lead, khong tao mat khau moi.
Du lieu hoan toan gia lap.

## Cach chay (Windows PowerShell)

1. Sao luu du lieu quan trong neu da co.
2. Database `crm_ban_hang` phai duoc khoi tao truoc tu schema goc.
   `scripts/setup-db.ps1` co DROP DATABASE; **KHONG chay lai** tren DB can giu du lieu.
3. Gan `DB_PASSWORD` cho phien terminal, va `DB_USER` neu khac `root`:

```powershell
$env:DB_PASSWORD = Read-Host "MySQL password"
.\scripts\seed-demo.ps1
```

SQL khong DROP/DELETE, khong UPDATE ban ghi da ton tai, su dung
`INSERT ... SELECT ... WHERE NOT EXISTS` va tao theo thu tu FK.
Chay nhieu lan se bo qua cac ban ghi DEMO da ton tai.
Script khong tu dong khoi tao schema, khong chay tu `setup-local.ps1`.

## Kiem tra

```sql
USE crm_ban_hang;
SELECT COUNT(*) FROM khach_hang WHERE ma_khach_hang LIKE 'DEMO-KH-%';
SELECT COUNT(*) FROM `lead` WHERE ma_lead LIKE 'DEMO-LEAD-%';
SELECT COUNT(*) FROM co_hoi WHERE ma_co_hoi LIKE 'DEMO-CH-%';
SELECT COUNT(*) FROM bao_gia WHERE ma_bao_gia LIKE 'DEMO-BG-%';
SELECT COUNT(*) FROM hop_dong WHERE so_hop_dong LIKE 'DEMO-HOP-DONG-%';
```

## Gioi han

- Seed truc tiep DB de hien thi/demo, **khong thay the** kiem thu AC, luong duyet,
  gui email that, transaction hay phan quyen.
- Cac module chua co giao dien tren nhanh dev (vi du bao gia/hop dong) co data,
  nhung chua the dam bao xem duoc trong UI. Neu can demo end-to-end, phai
  tich hop code cac story tuong ung va test lai.
- Neu cac thanh vien da doi schema/enum o nhanh khac, doi chieu va test lai tren
  DB test moi truoc khi merge. Chua chay MySQL that tren may nguoi dung.
