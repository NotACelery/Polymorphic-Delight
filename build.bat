@echo off
setlocal EnableExtensions DisableDelayedExpansion
cd /d "%~dp0"
if errorlevel 1 (
    echo ERROR: No se pudo abrir el directorio del proyecto.
    pause
    exit /b 1
)

set "MOD_VERSION="
for /f "tokens=2 delims==" %%V in ('findstr /b /c:"mod_version=" "gradle.properties" 2^>nul') do set "MOD_VERSION=%%V"
if not defined MOD_VERSION (
    echo ERROR: No se encontro mod_version en gradle.properties.
    pause
    exit /b 1
)

where powershell.exe >nul 2>&1
if errorlevel 1 (
    echo ERROR: Windows PowerShell no esta disponible.
    pause
    exit /b 1
)

echo.
echo =========================================
echo   Polymorphic Delight - NeoForge 1.21.1
echo =========================================
echo   Unica build compatible con Polymorph o Polymorph+
echo   Compilando...
echo.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0tools\run-build.ps1"
set "RESULT=%ERRORLEVEL%"
echo.
if "%RESULT%"=="0" (
    echo COMPILACION COMPLETADA.
    echo JAR FINAL: dist\polymorphic_delight-%MOD_VERSION%-neoforge-1.21.1.jar
) else (
    echo COMPILACION FALLIDA. Codigo: %RESULT%
    echo Revisa build-logs\gradle-build-*.log
)
echo.
pause
exit /b %RESULT%
