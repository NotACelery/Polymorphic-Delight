@echo off
setlocal EnableExtensions
cd /d "%~dp0"

set "NO_PAUSE=0"
if /I "%~1"=="--no-pause" set "NO_PAUSE=1"

set "MOD_VERSION="
for /f "tokens=2 delims==" %%V in ('findstr /b /c:"mod_version=" "gradle.properties" 2^>nul') do (
    set "MOD_VERSION=%%V"
)
if not defined MOD_VERSION (
    echo ERROR: No pude leer mod_version desde gradle.properties.
    if "%NO_PAUSE%"=="0" pause
    exit /b 1
)

title Polymorphic Delight - Forge Build %MOD_VERSION%

echo ============================================================
echo Polymorphic Delight - Forge 1.20.1
echo Build %MOD_VERSION%
echo ============================================================
echo.

powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0build.ps1"
set "RC=%ERRORLEVEL%"

echo.
if "%RC%"=="0" (
    echo ============================================================
    echo BUILD %MOD_VERSION% COMPLETADO CORRECTAMENTE
    echo JAR final: "%~dp0dist\polymorphic_delight-%MOD_VERSION%-forge-1.20.1.jar"
    echo ============================================================
) else (
    echo ============================================================
    echo BUILD %MOD_VERSION% FALLIDO - codigo %RC%
    echo La ventana quedara abierta para que puedas leer el error.
    echo Log: "%~dp0build-logs"
    echo ============================================================
)

if "%NO_PAUSE%"=="0" (
    echo.
    pause
)
exit /b %RC%
