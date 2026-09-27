@echo off
setlocal EnableExtensions
cd /d "%~dp0"

set "MOD_VERSION="
for /f "tokens=2 delims==" %%V in ('findstr /b /c:"mod_version=" "gradle.properties" 2^>nul') do (
    set "MOD_VERSION=%%V"
)
if not defined MOD_VERSION set "MOD_VERSION=unknown"

title Polymorphic Delight - Build %MOD_VERSION%

set "GRADLE_VERSION=9.2.1"
set "GRADLE_DIST_DIR=%CD%\.gradle-dist"
set "GRADLE_HOME=%GRADLE_DIST_DIR%\gradle-%GRADLE_VERSION%"
set "GRADLE_ZIP=%GRADLE_DIST_DIR%\gradle-%GRADLE_VERSION%-bin.zip"
set "GRADLE_URL=https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip"
set "BUILD_MODE=original"

if /I "%~1"=="plus" set "BUILD_MODE=plus"
if /I "%~1"=="original" set "BUILD_MODE=original"
if /I "%~1"=="all" set "BUILD_MODE=all"
if not "%~1"=="" (
    if /I not "%~1"=="plus" (
        if /I not "%~1"=="original" (
            if /I not "%~1"=="all" goto :usage
        )
    )
)

echo ============================================================
echo  POLYMORPHIC DELIGHT %MOD_VERSION%
echo  Minecraft 1.21.1 / NeoForge
echo ============================================================
echo Directorio: %CD%
echo.
if /I "%BUILD_MODE%"=="all" (
    echo Matriz de compilacion: Polymorph + Polymorph+
) else (
    echo Polymorph de compilacion: %BUILD_MODE%
)
echo.

set "JAVA_EXE="
set "JAVA_VERSION="
set "JAVA_BIN="

echo [INFO] Buscando automaticamente un JDK 21...

if defined JAVA_HOME call :tryJava "%JAVA_HOME%\bin\java.exe"

if not defined JAVA_EXE if defined ProgramFiles (
    call :scanJdkPattern "%ProgramFiles%\Eclipse Adoptium\jdk-21*"
    call :scanJdkPattern "%ProgramFiles%\Microsoft\jdk-21*"
    call :scanJdkPattern "%ProgramFiles%\Java\jdk-21*"
    call :scanJdkPattern "%ProgramFiles%\Amazon Corretto\jdk21*"
    call :scanJdkPattern "%ProgramFiles%\Zulu\zulu-21*"
    call :scanJdkPattern "%ProgramFiles%\BellSoft\LibericaJDK-21*"
)

if not defined JAVA_EXE if defined LOCALAPPDATA (
    call :scanJdkPattern "%LOCALAPPDATA%\Programs\Eclipse Adoptium\jdk-21*"
    call :scanJdkPattern "%LOCALAPPDATA%\Programs\Microsoft\jdk-21*"
)

if not defined JAVA_EXE if defined APPDATA (
    call :scanJavaRoot "%APPDATA%\PrismLauncher\java"
)

if not defined JAVA_EXE if defined LOCALAPPDATA (
    call :scanJavaRoot "%LOCALAPPDATA%\PrismLauncher\java"
    call :scanJavaRoot "%LOCALAPPDATA%\Programs\PrismLauncher\java"
)

if not defined JAVA_EXE if defined ProgramFiles (
    call :scanJavaRoot "%ProgramFiles%\PrismLauncher\java"
)

if not defined JAVA_EXE if defined USERPROFILE (
    call :scanJdkPattern "%USERPROFILE%\.jdks\*21*"
)

if not defined JAVA_EXE (
    for /f "delims=" %%J in ('where java.exe 2^>nul') do (
        if not defined JAVA_EXE call :tryJava "%%~fJ"
    )
)

if not defined JAVA_EXE (
    echo.
    echo [ERROR] No se encontro un JDK 21 valido automaticamente.
    echo         El Java de Minecraft/Prism puede ser solo una runtime y no incluir javac.
    echo.
    echo Lugares revisados:
    echo   - JAVA_HOME
    echo   - Eclipse Adoptium / Microsoft OpenJDK / Oracle
    echo   - Amazon Corretto / Zulu / Liberica
    echo   - Prism Launcher
    echo   - %%USERPROFILE%%\.jdks
    echo   - PATH
    echo.
    echo Instala un JDK 21 ^(por ejemplo Temurin 21 o Microsoft OpenJDK 21^)
    echo y vuelve a ejecutar este mismo build.bat.
    goto :fail
)

for %%I in ("%JAVA_EXE%") do set "JAVA_BIN=%%~dpI"
for %%I in ("%JAVA_BIN%..") do set "JAVA_HOME=%%~fI"
set "PATH=%JAVA_BIN%;%PATH%"

echo [OK] JDK 21 encontrado automaticamente:
echo      %JAVA_EXE%
echo [OK] JAVA_HOME=%JAVA_HOME%
"%JAVA_EXE%" -version
echo.

set "GRADLE_CMD="
if exist "%CD%\gradlew.bat" (
    set "GRADLE_CMD=%CD%\gradlew.bat"
    echo [OK] Usando Gradle Wrapper del proyecto.
) else (
    if not exist "%GRADLE_HOME%\bin\gradle.bat" (
        echo [INFO] Gradle %GRADLE_VERSION% no esta instalado en el proyecto.
        echo [INFO] Descargando Gradle %GRADLE_VERSION%...
        if not exist "%GRADLE_DIST_DIR%" mkdir "%GRADLE_DIST_DIR%"
        if errorlevel 1 goto :gradle_dir_fail

        if exist "%GRADLE_ZIP%" del /q "%GRADLE_ZIP%" >nul 2>nul

        where curl.exe >nul 2>nul
        if not errorlevel 1 (
            curl.exe -L --fail --retry 3 --output "%GRADLE_ZIP%" "%GRADLE_URL%"
        ) else (
            powershell -NoProfile -ExecutionPolicy Bypass -Command ^
                "Invoke-WebRequest '%GRADLE_URL%' -OutFile '%GRADLE_ZIP%'"
        )
        if errorlevel 1 goto :gradle_download_fail
        if not exist "%GRADLE_ZIP%" goto :gradle_download_fail

        echo [INFO] Descomprimiendo Gradle...
        powershell -NoProfile -ExecutionPolicy Bypass -Command ^
            "Expand-Archive '%GRADLE_ZIP%' '%GRADLE_DIST_DIR%' -Force"
        if errorlevel 1 goto :gradle_extract_fail

        del /q "%GRADLE_ZIP%" >nul 2>nul
    )

    if not exist "%GRADLE_HOME%\bin\gradle.bat" goto :gradle_missing
    set "GRADLE_CMD=%GRADLE_HOME%\bin\gradle.bat"
    echo [OK] Gradle %GRADLE_VERSION% disponible.
)

if /I "%BUILD_MODE%"=="all" (
    if exist "verified-builds" rmdir /s /q "verified-builds"
    mkdir "verified-builds"
    call :runBuild original
    if errorlevel 1 goto :fail
    call :runBuild plus
    if errorlevel 1 goto :fail
) else (
    call :runBuild %BUILD_MODE%
    if errorlevel 1 goto :fail
)

echo.
echo ============================================================
echo  BUILD SUCCESS
echo ============================================================
echo.
echo JAR generado por la ultima compilacion:
for %%F in ("build\libs\*.jar") do echo   %%~fF
echo.
if /I "%BUILD_MODE%"=="all" (
    echo [OK] Compilo correctamente contra Polymorph y Polymorph+.
    echo [OK] Copias verificadas guardadas en:
    echo      verified-builds\original\
    echo      verified-builds\plus\
) else (
    echo Para comprobar contra ambas implementaciones:
    echo   build.bat all
)
echo.
pause
endlocal & exit /b 0

:runBuild
set "POLYMORPH_FLAVOR=%~1"
echo.
echo ------------------------------------------------------------
echo  BUILD: %POLYMORPH_FLAVOR%
echo ------------------------------------------------------------
echo [BUILD] clean build --stacktrace -Ppolymorph_flavor=%POLYMORPH_FLAVOR%
echo.
call "%GRADLE_CMD%" --no-daemon clean build --stacktrace -Ppolymorph_flavor=%POLYMORPH_FLAVOR%
if errorlevel 1 exit /b 1

if /I "%BUILD_MODE%"=="all" (
    if not exist "verified-builds\%POLYMORPH_FLAVOR%" mkdir "verified-builds\%POLYMORPH_FLAVOR%"
    copy /Y "build\libs\*.jar" "verified-builds\%POLYMORPH_FLAVOR%\" >nul
    if errorlevel 1 exit /b 1
)

echo [OK] Build %POLYMORPH_FLAVOR% completado.
exit /b 0

:scanJdkPattern
if defined JAVA_EXE exit /b 0
for /d %%D in ("%~1") do (
    if not defined JAVA_EXE call :tryJava "%%~fD\bin\java.exe"
)
exit /b 0

:scanJavaRoot
if defined JAVA_EXE exit /b 0
if not exist "%~1" exit /b 0
for /r "%~1" %%J in (java.exe) do (
    if not defined JAVA_EXE call :tryJava "%%~fJ"
)
exit /b 0

:tryJava
if defined JAVA_EXE exit /b 0
if "%~1"=="" exit /b 0
if not exist "%~1" exit /b 0

set "CANDIDATE_BIN="
set "CANDIDATE_VERSION="
set "CANDIDATE_MAJOR="
set "JAVA_PROBE="

for %%I in ("%~1") do set "CANDIDATE_BIN=%%~dpI"
if not exist "%CANDIDATE_BIN%javac.exe" exit /b 0

set "JAVA_PROBE=%TEMP%\polymorphic-delight-java-%RANDOM%-%RANDOM%.txt"
"%~1" -XshowSettings:properties -version >"%JAVA_PROBE%" 2>&1
if errorlevel 1 (
    del /q "%JAVA_PROBE%" >nul 2>nul
    exit /b 0
)

for /f "tokens=3" %%V in ('findstr /C:"java.version =" "%JAVA_PROBE%"') do (
    set "CANDIDATE_VERSION=%%V"
)
del /q "%JAVA_PROBE%" >nul 2>nul
if not defined CANDIDATE_VERSION exit /b 0

for /f "tokens=1 delims=." %%M in ("%CANDIDATE_VERSION%") do set "CANDIDATE_MAJOR=%%M"
if not "%CANDIDATE_MAJOR%"=="21" exit /b 0

set "JAVA_EXE=%~1"
set "JAVA_VERSION=%CANDIDATE_VERSION%"
exit /b 0

:usage
echo Uso:
echo   build.bat          ^(Polymorph original^)
echo   build.bat original
echo   build.bat plus
echo   build.bat all      ^(compila contra ambos^)
echo.
goto :fail

:gradle_dir_fail
echo [ERROR] No se pudo crear "%GRADLE_DIST_DIR%".
goto :fail

:gradle_download_fail
echo.
echo [ERROR] No se pudo descargar Gradle %GRADLE_VERSION%.
echo URL: %GRADLE_URL%
goto :fail

:gradle_extract_fail
echo.
echo [ERROR] Gradle se descargo, pero no pudo descomprimirse.
echo         Borra .gradle-dist y vuelve a ejecutar build.bat.
goto :fail

:gradle_missing
echo.
echo [ERROR] No existe "%GRADLE_HOME%\bin\gradle.bat" tras la descarga.
goto :fail

:fail
echo.
echo ============================================================
echo  BUILD FAILED
echo ============================================================
echo.
echo La ventana queda abierta para poder copiar el error.
pause
endlocal & exit /b 1
