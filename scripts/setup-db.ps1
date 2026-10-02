<#
.SYNOPSIS
    Script khoi tao / reset database crm_ban_hang cho moi truong local/dev.
.DESCRIPTION
    Script thuc hien:
    1. Canh bao viec RESET database crm_ban_hang va yeu cau xac nhan.
    2. Kiem tra cac dieu kien tien quyet: mysql.exe, DB_PASSWORD, file SQL bootstrap.
    3. Nap toan bo schema va seed data tu 001_crm_ban_hang_full_schema_8_sprints.sql.
    4. Kiem tra nhanh tinh toan ven (so luong bang, tai khoan seed dev).
.PARAMETER Force
    Bo qua buoc xac nhan tuong tac, chay truc tiep (thich hop cho CI/CD hoac tu dong hoa).
.PARAMETER HostName
    Dia chi may chu MySQL (mac dinh doc tu $env:DB_HOST hoac 'localhost').
.PARAMETER Port
    Cong ket noi MySQL (mac dinh doc tu $env:DB_PORT hoac '3306').
.PARAMETER UserName
    Ten nguoi dung MySQL (mac dinh doc tu $env:DB_USER hoac 'root').
#>

[CmdletBinding()]
param (
    [switch]$Force,
    [string]$HostName = $env:DB_HOST,
    [string]$Port = $env:DB_PORT,
    [string]$UserName = $env:DB_USER
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# Thiet lap gia tri mac dinh cho ket noi neu chua co
if ([string]::IsNullOrWhiteSpace($HostName)) {
    $HostName = "localhost"
}
if ([string]::IsNullOrWhiteSpace($Port)) {
    $Port = "3306"
}
if ([string]::IsNullOrWhiteSpace($UserName)) {
    $UserName = "root"
}

# ----------------------------------------------------------------------
# 1. CANH BAO RESET DATABASE
# ----------------------------------------------------------------------
Write-Host ""
Write-Host "======================================================================" -ForegroundColor Yellow
Write-Host " [CANH BAO] KHOI TAO / RESET DATABASE CRM BAN HANG (8 SPRINTS)" -ForegroundColor Yellow
Write-Host "======================================================================" -ForegroundColor Yellow
Write-Host " Script nay se RESET (XOA VA KHOI TAO LAI) toan bo co so du lieu:" -ForegroundColor Red
Write-Host "   -> Database: crm_ban_hang" -ForegroundColor Red
Write-Host "   -> Toan bo du lieu hien co trong database nay SE BI XOA HOAN TOAN!" -ForegroundColor Red
Write-Host " Chi su dung cho may dev moi hoac khi can lam sach du lieu kiem thu." -ForegroundColor Yellow
Write-Host "======================================================================" -ForegroundColor Yellow
Write-Host ""

if (-not $Force) {
    $canPrompt = $false
    try {
        $canPrompt = [Environment]::UserInteractive -and -not [Console]::IsInputRedirected
    } catch {
        $canPrompt = $false
    }

    if ($canPrompt) {
        $confirm = Read-Host "Ban co chac chan muon RESET database 'crm_ban_hang'? (Nhap 'YES' hoac 'y' de tiep tuc)"
        if ($confirm -notmatch '^(y|yes)$') {
            Write-Host "Da huy thao tac khoi tao database. Khong co thay doi nao duoc thuc hien." -ForegroundColor Yellow
            exit 0
        }
    } else {
        Write-Error "LOI: Moi truong khong ho tro nhap tuong tac. Vui long truyen them tham so -Force de xac nhan reset database 'crm_ban_hang'."
        exit 1
    }
}

# ----------------------------------------------------------------------
# 2. KIEM TRA BIEN MOI TRUONG DB_PASSWORD
# ----------------------------------------------------------------------
$dbPassword = $env:DB_PASSWORD
if ([string]::IsNullOrWhiteSpace($dbPassword)) {
    Write-Host ""
    Write-Host "----------------------------------------------------------------------" -ForegroundColor Red
    Write-Host " LOI: Bien moi truong DB_PASSWORD chua duoc thiet lap!" -ForegroundColor Red
    Write-Host "----------------------------------------------------------------------" -ForegroundColor Red
    Write-Host " De bao mat, mat khau co so du lieu khong duoc hard-code trong script." -ForegroundColor Yellow
    Write-Host " Vui long thiet lap bien moi truong DB_PASSWORD truoc khi chay, vi du:" -ForegroundColor Cyan
    Write-Host "   `$env:DB_PASSWORD = 'mat_khau_mysql_cua_ban'" -ForegroundColor White
    Write-Host "   .\scripts\setup-db.ps1" -ForegroundColor White
    Write-Host "----------------------------------------------------------------------" -ForegroundColor Red
    exit 1
}

# ----------------------------------------------------------------------
# 3. TIM VA XAC DINH MYSQL CLIENT (mysql.exe)
# ----------------------------------------------------------------------
$mysqlCmd = Get-Command "mysql.exe" -ErrorAction SilentlyContinue
$mysqlExe = $null

if ($mysqlCmd) {
    $mysqlExe = $mysqlCmd.Source
} else {
    # Tim kiem tai cac vi tri cai dat pho bien tren Windows
    $candidatePaths = @(
        "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe",
        "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
        "C:\Program Files (x86)\MySQL\MySQL Server 8.4\bin\mysql.exe",
        "C:\Program Files (x86)\MySQL\MySQL Server 8.0\bin\mysql.exe",
        "D:\Apache\mysql\bin\mysql.exe",
        "C:\tools\mysql\bin\mysql.exe",
        "C:\xampp\mysql\bin\mysql.exe"
    )

    foreach ($path in $candidatePaths) {
        if (Test-Path -LiteralPath $path) {
            $mysqlExe = $path
            break
        }
    }
}

if (-not $mysqlExe) {
    Write-Host ""
    Write-Host "----------------------------------------------------------------------" -ForegroundColor Red
    Write-Host " LOI: Khong tim thay client 'mysql.exe'!" -ForegroundColor Red
    Write-Host "----------------------------------------------------------------------" -ForegroundColor Red
    Write-Host " Vui long dam bao da cai dat MySQL Server/Client va them thu muc bin" -ForegroundColor Yellow
    Write-Host " chua mysql.exe vao bien moi truong PATH." -ForegroundColor Yellow
    Write-Host "----------------------------------------------------------------------" -ForegroundColor Red
    exit 1
}

Write-Host "[OK] Da tim thay MySQL Client: $mysqlExe" -ForegroundColor Green

# ----------------------------------------------------------------------
# 4. TIM FILE SCHEMA 001_crm_ban_hang_full_schema_8_sprints.sql
# ----------------------------------------------------------------------
$scriptDir = $PSScriptRoot
if (-not $scriptDir) {
    $scriptDir = (Get-Item .).FullName
}
$projectRoot = Split-Path -Parent $scriptDir

$sqlRelPath = "src/main/resources/db/001_crm_ban_hang_full_schema_8_sprints.sql"
$sqlFullPath = Join-Path $projectRoot $sqlRelPath

if (-not (Test-Path -LiteralPath $sqlFullPath)) {
    # Thu tim theo thu muc lam viec hien tai
    $sqlFullPath = Join-Path (Get-Location) $sqlRelPath
}

if (-not (Test-Path -LiteralPath $sqlFullPath)) {
    Write-Host ""
    Write-Host "----------------------------------------------------------------------" -ForegroundColor Red
    Write-Host " LOI: Khong tim thay file schema SQL!" -ForegroundColor Red
    Write-Host " File ky vong: $sqlFullPath" -ForegroundColor Yellow
    Write-Host "----------------------------------------------------------------------" -ForegroundColor Red
    exit 1
}

$resolvedSqlPath = (Resolve-Path -LiteralPath $sqlFullPath).Path
Write-Host "[OK] Da tim thay file schema SQL: $resolvedSqlPath" -ForegroundColor Green

# ----------------------------------------------------------------------
# 5. THUC HIEN IMPORT DATABASE TU FILE 001
# ----------------------------------------------------------------------
Write-Host ""
Write-Host "[*] Dang ket noi toi MySQL ($($HostName):$($Port), User: $($UserName))..." -ForegroundColor Cyan
Write-Host "[*] Dang thuc thi bootstrap database crm_ban_hang tu file 001..." -ForegroundColor Cyan

# Bao mat: Su dung bien moi truong MYSQL_PWD de truyen mat khau cho mysql client
# Giup tranh canh bao insecure password tren CLI va khong lo mat khau trong danh sach tien trinh
$savedMysqlPwd = $env:MYSQL_PWD
$env:MYSQL_PWD = $dbPassword

try {
    # Thuc hien import file SQL vao MySQL bang cach redirect standard input tu file
    $importProcess = Start-Process -FilePath $mysqlExe `
        -ArgumentList "--host=$HostName", "--port=$Port", "--user=$UserName", "--default-character-set=utf8mb4" `
        -RedirectStandardInput $resolvedSqlPath `
        -NoNewWindow -Wait -PassThru

    if ($importProcess.ExitCode -ne 0) {
        Write-Host ""
        Write-Host "----------------------------------------------------------------------" -ForegroundColor Red
        Write-Host " LOI: Qua trinh import SQL that bai (Exit code: $($importProcess.ExitCode))!" -ForegroundColor Red
        Write-Host " Vui long kiem tra quyen nguoi dung, mat khau hoac trang thai MySQL." -ForegroundColor Yellow
        Write-Host "----------------------------------------------------------------------" -ForegroundColor Red
        exit $importProcess.ExitCode
    }
} finally {
    # Don dep mat khau khoi moi truong ngay sau khi thuc thi
    $env:MYSQL_PWD = $savedMysqlPwd
}

# ----------------------------------------------------------------------
# 6. KIEM TRA HAU KHOI TAO (POST-IMPORT VERIFICATION)
# ----------------------------------------------------------------------
Write-Host ""
Write-Host "[*] Dang kiem tra tinh toan ven cua database sau khi import..." -ForegroundColor Cyan

$tableCountVal = "N/A"
$userCountVal = "N/A"
$devUserVal = "N/A"

$env:MYSQL_PWD = $dbPassword
try {
    $verifySql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'crm_ban_hang';"
    $tableCountOutput = & $mysqlExe --host=$HostName --port=$Port --user=$UserName --batch --skip-column-names -e $verifySql 2>$null
    if ($LASTEXITCODE -eq 0 -and $tableCountOutput) {
        $tableCountVal = $tableCountOutput.ToString().Trim()
    }

    $userCountSql = "SELECT COUNT(*) FROM crm_ban_hang.nguoi_dung;"
    $userCountOutput = & $mysqlExe --host=$HostName --port=$Port --user=$UserName --batch --skip-column-names -e $userCountSql 2>$null
    if ($LASTEXITCODE -eq 0 -and $userCountOutput) {
        $userCountVal = $userCountOutput.ToString().Trim()
    }

    $devUserSql = "SELECT CONCAT(ho_ten, ' <', email, '> - ', trang_thai) FROM crm_ban_hang.nguoi_dung WHERE email = 'dtc245200134@ictu.edu.vn';"
    $devUserOutput = & $mysqlExe --host=$HostName --port=$Port --user=$UserName --batch --skip-column-names -e $devUserSql 2>$null
    if ($LASTEXITCODE -eq 0 -and $devUserOutput) {
        $devUserVal = $devUserOutput.ToString().Trim()
    }
} finally {
    $env:MYSQL_PWD = $savedMysqlPwd
}

# ----------------------------------------------------------------------
# 7. THONG BAO THANH CONG
# ----------------------------------------------------------------------
Write-Host ""
Write-Host "======================================================================" -ForegroundColor Green
Write-Host " KHOI TAO DATABASE 'crm_ban_hang' THANH CONG!" -ForegroundColor Green
Write-Host "======================================================================" -ForegroundColor Green
Write-Host " Thong tin ket noi:" -ForegroundColor Cyan
Write-Host "   - May chu: $($HostName):$($Port)"
Write-Host "   - Database: crm_ban_hang"
Write-Host "   - Tai khoan ket noi DB: $($UserName)"
Write-Host ""
Write-Host " Ket qua kiem tra:" -ForegroundColor Cyan
Write-Host "   - So luong bang da tao: $tableCountVal"
Write-Host "   - So tai khoan nguoi dung: $userCountVal"
Write-Host "   - Tai khoan dev kiem thu: $devUserVal (Vai tro: SALES_REP)"
Write-Host "   - Mat khau mac dinh dev: 123456@Aa"
Write-Host "======================================================================" -ForegroundColor Green
Write-Host " Ban da co the khoi chay project CRM." -ForegroundColor Green
Write-Host ""
