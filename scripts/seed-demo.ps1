<#
.SYNOPSIS
    Nap du lieu demo CRM an toan (khong DROP/RESET database).
.DESCRIPTION
    Chay src/main/resources/db/seed_demo.sql tren database crm_ban_hang
    DA duoc khoi tao. SQL co prefix DEMO- va bo qua ban ghi da ton tai.
    Chi dung tren local/dev; script khong tu dong reset schema.
.EXAMPLE
    $env:DB_PASSWORD = Read-Host 'MySQL password'
    .\scripts\seed-demo.ps1
#>
[CmdletBinding()]
param(
    [string]$HostName = $env:DB_HOST,
    [string]$Port = $env:DB_PORT,
    [string]$UserName = $env:DB_USER
)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
if ([string]::IsNullOrWhiteSpace($HostName)) { $HostName = 'localhost' }
if ([string]::IsNullOrWhiteSpace($Port)) { $Port = '3306' }
if ([string]::IsNullOrWhiteSpace($UserName)) { $UserName = 'root' }
if ([string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) {
    throw 'Set DB_PASSWORD trong PowerShell truoc khi chay (khong commit mat khau).'
}
$projectRoot = Split-Path -Parent $PSScriptRoot
$sqlFile = Join-Path $projectRoot 'src\main\resources\db\seed_demo.sql'
if (-not (Test-Path -LiteralPath $sqlFile -PathType Leaf)) { throw "Khong tim thay: $sqlFile" }

$mysql = Get-Command mysql.exe -ErrorAction SilentlyContinue
if (-not $mysql) {
    foreach ($path in @(
        'C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe',
        'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'
    )) {
        if (Test-Path -LiteralPath $path -PathType Leaf) {
            $mysql = @{ Source = $path }
            break
        }
    }
}
if (-not $mysql) { throw 'Khong tim thay mysql.exe. Them MySQL bin vao PATH.' }

Write-Host '[SEED] Nap demo tren database crm_ban_hang. KHONG reset du lieu.' -ForegroundColor Cyan
Write-Host '[SEED] Chi chay tren local/dev. Dam bao da backup neu co du lieu can giu.' -ForegroundColor Yellow

$savedPwd = $env:MYSQL_PWD
$env:MYSQL_PWD = $env:DB_PASSWORD
try {
    # --database dam bao chi ket noi DB da ton tai; file seed khong co CREATE/DROP DATABASE.
    $proc = Start-Process -FilePath $mysql.Source -ArgumentList @(
        "--host=$HostName", "--port=$Port", "--user=$UserName",
        '--default-character-set=utf8mb4', '--database=crm_ban_hang', '--show-warnings', '--batch'
    ) -RedirectStandardInput $sqlFile -Wait -PassThru -NoNewWindow
    if ($proc.ExitCode -ne 0) { throw "Nap du lieu demo that bai (exit code $($proc.ExitCode))." }
    Write-Host '[OK] Import SQL thanh cong. Xem so luong DEMO-* duoc in o tren.' -ForegroundColor Green
} finally {
    $env:MYSQL_PWD = $savedPwd
}
