@echo off
setlocal EnableExtensions DisableDelayedExpansion
cd /d "%~dp0"
if errorlevel 1 (
    echo ERROR: No se pudo abrir la carpeta del proyecto.
    pause
    exit /b 1
)

if not exist "gradle.properties" (
    echo ERROR: Falta gradle.properties.
    pause
    exit /b 1
)
if not exist "tools\run-build.ps1" (
    echo ERROR: Falta tools\run-build.ps1.
    pause
    exit /b 1
)
where powershell.exe >nul 2>&1
if errorlevel 1 (
    echo ERROR: Windows PowerShell no esta disponible.
    pause
    exit /b 1
)

set "MOD_VERSION="
for /f "tokens=2 delims== " %%V in ('findstr /r /b /c:"mod_version[ ]*=" "gradle.properties" 2^>nul') do set "MOD_VERSION=%%V"

echo.
echo =========================================
echo   Polymorphic Delight - Fabric 26.1.x
echo =========================================
echo   Minecraft 26.1 / 26.1.1 / 26.1.2
echo   JDK 25 - deteccion automatica
echo.
powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0tools\run-build.ps1"
set "RESULT=%ERRORLEVEL%"
echo.
if "%RESULT%"=="0" (
    echo COMPILACION COMPLETADA.
    echo JAR FINAL: dist\polymorphic_delight-%MOD_VERSION%.jar
) else (
    echo COMPILACION FALLIDA. Codigo: %RESULT%
    echo Revisa los archivos de build-logs\
)
echo.
if /i not "%~1"=="--no-pause" pause
exit /b %RESULT%
