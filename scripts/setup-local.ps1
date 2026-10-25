<#
.SYNOPSIS
    Thiet lap moi truong local, kiem thu va build CRM tren Windows.
.DESCRIPTION
    Script KHONG reset database, khong sua file tracked va khong merge Git.
    1. Kiem tra Java 25+, Maven (dung JDK 25+) va file project.
    2. Tao database-local.properties tu file mau neu chua ton tai.
    3. Chay Maven test va package (WAR).
    4. Neu co -Deploy, copy WAR vao Tomcat 11 (khong tu start/stop Tomcat).

    Database can duoc tao san. Khi can reset database test, su dung
    scripts/setup-db.ps1 RIENG va doc ky canh bao xoa du lieu.
.EXAMPLE
    .\scripts\setup-local.ps1
.EXAMPLE
    .\scripts\setup-local.ps1 -Deploy
.EXAMPLE
    .\scripts\setup-local.ps1 -SkipTests
.PARAMETER Deploy
    Copy WAR sau khi build vao CATALINA_HOME/webapps (ghi de WAR cu neu co).
.PARAMETER SkipTests
    Bo qua unit test chi cho viec build thu local. Khong dung de xac nhan Story Done.
#>

[CmdletBinding()]
param(
    [switch]$Deploy,
    [switch]$SkipTests
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $PSScriptRoot
$originalLocation = Get-Location

function Invoke-ExternalCommand {
    param(
        [Parameter(Mandatory = $true)][string]$Name,
        [string[]]$Arguments = @(),
        [switch]$CaptureOutput
    )

    # PowerShell 5.1 can treat native STDERR warnings as terminating
    # NativeCommandError when ErrorActionPreference is Stop.
    # Inspect the process exit code instead of classifying STDERR as failure.
    $savedPreference = $ErrorActionPreference
    $exitCode = -1
    try {
        $ErrorActionPreference = 'Continue'
        if ($CaptureOutput) {
            $output = @(& $Name @Arguments 2>&1)
        } else {
            & $Name @Arguments 2>&1 | ForEach-Object { Write-Host $_.ToString() }
        }
        $exitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $savedPreference
    }
    if ($exitCode -ne 0) {
        throw "$Name $($Arguments -join ' ') that bai (exit code: $exitCode)."
    }
    if ($CaptureOutput) {
        return $output
    }
}

try {
    Set-Location -LiteralPath $projectRoot
    Write-Host "[SETUP] Project: $projectRoot" -ForegroundColor Cyan

    foreach ($relativePath in @('pom.xml', 'database-local.properties.example')) {
        if (-not (Test-Path -LiteralPath (Join-Path $projectRoot $relativePath) -PathType Leaf)) {
            throw "Thieu file bat buoc: $relativePath"
        }
    }

    foreach ($commandName in @('java', 'mvn')) {
        if (-not (Get-Command -Name $commandName -ErrorAction SilentlyContinue)) {
            throw "Khong tim thay '$commandName' trong PATH. Hay cai dat/cau hinh truoc khi chay."
        }
    }

    $javaInfo = Invoke-ExternalCommand -Name 'java' -Arguments @('--version') -CaptureOutput
    $javaText = $javaInfo -join "`n"
    $javaMatch = [regex]::Match($javaText, '(?im)^(?:openjdk|java)\s+(\d+)')
    if (-not $javaMatch.Success) {
        throw "Khong doc duoc Java version. Ket qua: $javaText"
    }
    $javaMajor = [int]$javaMatch.Groups[1].Value
    if ($javaMajor -lt 25) {
        throw "Project pom.xml can Java 25, nhung may dang dung Java $javaMajor. Kiem tra JAVA_HOME va PATH."
    }
    Write-Host "[OK] Java $javaMajor" -ForegroundColor Green

    $mavenInfo = Invoke-ExternalCommand -Name 'mvn' -Arguments @('-version') -CaptureOutput
    $mavenText = $mavenInfo -join "`n"
    $mavenJavaMatch = [regex]::Match($mavenText, '(?i)Java version:\s*(\d+)')
    if (-not $mavenJavaMatch.Success -or [int]$mavenJavaMatch.Groups[1].Value -lt 25) {
        throw "Maven khong dung JDK 25+. Kiem tra JAVA_HOME. Thong tin Maven: $mavenText"
    }
    Write-Host "[OK] Maven dung JDK $($mavenJavaMatch.Groups[1].Value)" -ForegroundColor Green

    $exampleConfig = Join-Path $projectRoot 'database-local.properties.example'
    $localConfig = Join-Path $projectRoot 'database-local.properties'
    if (-not (Test-Path -LiteralPath $localConfig -PathType Leaf)) {
        Copy-Item -LiteralPath $exampleConfig -Destination $localConfig
        Write-Host '[OK] Da tao database-local.properties tu file mau.' -ForegroundColor Green
    } else {
        Write-Host '[OK] Giu nguyen database-local.properties hien co.' -ForegroundColor Green
    }

    if ((Get-Content -LiteralPath $localConfig -Raw).Contains('YOUR_LOCAL_MYSQL_PASSWORD') -and
        [string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) {
        Write-Warning 'Chua cau hinh mat khau MySQL local. Sua database-local.properties hoac set DB_PASSWORD.'
    }

    if (-not (Get-Command -Name 'mysql' -ErrorAction SilentlyContinue)) {
        Write-Warning 'Khong tim thay mysql client trong PATH. Maven test van co the chay, nhung can MySQL Server de su dung CRM.'
    }

    if ($SkipTests) {
        Write-Warning 'Dang build bo qua test; khong duoc xem la test PASS.'
        Invoke-ExternalCommand -Name 'mvn' -Arguments @('clean', 'package', '-DskipTests')
    } else {
        Write-Host '[BUILD] Running mvn clean test...' -ForegroundColor Cyan
        Invoke-ExternalCommand -Name 'mvn' -Arguments @('clean', 'test')
        Write-Host '[BUILD] Running mvn clean package...' -ForegroundColor Cyan
        Invoke-ExternalCommand -Name 'mvn' -Arguments @('clean', 'package')
    }

    $warPath = Join-Path $projectRoot 'target\crm-ban-hang.war'
    if (-not (Test-Path -LiteralPath $warPath -PathType Leaf)) {
        throw 'Maven khong tao target/crm-ban-hang.war. Kiem tra pom.xml.'
    }
    Write-Host "[OK] WAR: $warPath" -ForegroundColor Green

    if ($Deploy) {
        $tomcatHome = $env:CATALINA_HOME
        if ([string]::IsNullOrWhiteSpace($tomcatHome)) {
            throw 'Can thiet lap CATALINA_HOME tro toi thu muc Apache Tomcat 11 de deploy.'
        }
        $versionBat = Join-Path $tomcatHome 'bin\version.bat'
        $webappsDir = Join-Path $tomcatHome 'webapps'
        if (-not (Test-Path -LiteralPath $versionBat -PathType Leaf) -or
            -not (Test-Path -LiteralPath $webappsDir -PathType Container)) {
            throw 'CATALINA_HOME khong tro toi mot ban cai Tomcat hop le.'
        }
        $tomcatInfo = Invoke-ExternalCommand -Name $versionBat -CaptureOutput | Out-String
        if ($tomcatInfo -notmatch 'Apache Tomcat/11\.') {
            throw 'Project dung Jakarta Servlet 6.1: can Apache Tomcat 11, khong dung Tomcat 10.1.'
        }

        $destination = Join-Path $webappsDir 'crm-ban-hang.war'
        Write-Warning 'Nen dung Tomcat truoc khi thay WAR. -Deploy se ghi de WAR cu, khong xoa database.'
        Copy-Item -LiteralPath $warPath -Destination $destination -Force
        Write-Host "[OK] Da copy WAR: $destination" -ForegroundColor Green
        Write-Host 'Khoi dong/lai Tomcat sau khi cau hinh DB_* trong moi truong cua Tomcat.' -ForegroundColor Yellow
    } else {
        Write-Host 'De copy WAR vao Tomcat 11: .\scripts\setup-local.ps1 -Deploy' -ForegroundColor Yellow
    }

    Write-Host 'Luu y: Script khong tao/reset MySQL database. database-local.properties o goc project co the khong duoc Tomcat doc neu thu muc lam viec khac; nen dung DB_URL, DB_USER, DB_PASSWORD trong moi truong Tomcat.' -ForegroundColor Yellow
    Write-Host 'URL sau khi khoi dong Tomcat: http://localhost:8080/crm-ban-hang/' -ForegroundColor Cyan
} finally {
    Set-Location -LiteralPath $originalLocation
}
