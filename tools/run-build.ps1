$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
Set-Location -LiteralPath $project
$logDirectory = Join-Path $project 'build-logs'
New-Item -ItemType Directory -Force -Path $logDirectory | Out-Null
$stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$log = Join-Path $logDirectory "neoforge-build-$stamp.log"
$startedTranscript = $false
$result = 0

try {
    Start-Transcript -Path $log -Force | Out-Null
    $startedTranscript = $true
    Write-Host '============================================'
    Write-Host ' Polymorphic Delight - NeoForge'
    Write-Host '============================================'
    Write-Host "Proyecto: $project"
    Write-Host "Registro: $log"
    Write-Host ''

function Test-Jdk21Home {
    param([string]$HomePath)
    if ([string]::IsNullOrWhiteSpace($HomePath)) { return $false }
    $javaExe = Join-Path $HomePath 'bin\java.exe'
    $javacExe = Join-Path $HomePath 'bin\javac.exe'
    if (-not (Test-Path -LiteralPath $javaExe -PathType Leaf) -or
        -not (Test-Path -LiteralPath $javacExe -PathType Leaf)) { return $false }

    try {
        $startInfo = New-Object System.Diagnostics.ProcessStartInfo
        $startInfo.FileName = $javaExe
        $startInfo.Arguments = '-version'
        $startInfo.UseShellExecute = $false
        $startInfo.CreateNoWindow = $true
        $startInfo.RedirectStandardError = $true
        $startInfo.RedirectStandardOutput = $true
        $process = [System.Diagnostics.Process]::Start($startInfo)
        $versionText = $process.StandardError.ReadToEnd() + $process.StandardOutput.ReadToEnd()
        $process.WaitForExit()
        $match = [regex]::Match($versionText, 'version\s+"(\d+)')
        return ($process.ExitCode -eq 0 -and $match.Success -and [int]$match.Groups[1].Value -eq 21)
    } catch { return $false }
}

function Find-Jdk21 {
    $candidates = New-Object 'System.Collections.Generic.List[string]'
    foreach ($name in @('POLYMORPH_JAVA_HOME', 'JAVA21_HOME', 'JDK21_HOME', 'JAVA_HOME')) {
        $value = [Environment]::GetEnvironmentVariable($name)
        if (-not [string]::IsNullOrWhiteSpace($value)) { $candidates.Add($value.Trim('"')) }
    }
    $pathJava = Get-Command java.exe -ErrorAction SilentlyContinue
    if ($pathJava) { $candidates.Add((Split-Path -Parent (Split-Path -Parent $pathJava.Source))) }

    $jdkRoot = Join-Path $project '.jdk-bin'
    $roots = @(
        $jdkRoot,
        "$env:ProgramFiles\Eclipse Adoptium",
        "$env:ProgramFiles\Java",
        "$env:ProgramFiles\Microsoft",
        "$env:ProgramFiles\Amazon Corretto",
        "$env:ProgramFiles\Zulu",
        "$env:ProgramFiles\BellSoft",
        "$env:LOCALAPPDATA\Programs\Eclipse Adoptium",
        "$env:USERPROFILE\.jdks",
        "$env:USERPROFILE\.gradle\jdks"
    )
    foreach ($root in $roots) {
        if (-not (Test-Path -LiteralPath $root -PathType Container)) { continue }
        $candidates.Add($root)
        foreach ($child in @(Get-ChildItem -LiteralPath $root -Directory -ErrorAction SilentlyContinue)) {
            $candidates.Add($child.FullName)
            if ($root -like '*\.gradle\jdks') {
                foreach ($nested in @(Get-ChildItem -LiteralPath $child.FullName -Directory -ErrorAction SilentlyContinue)) {
                    $candidates.Add($nested.FullName)
                }
            }
        }
    }
    foreach ($path in @($candidates | Select-Object -Unique)) {
        if (Test-Jdk21Home $path) { return (Resolve-Path -LiteralPath $path).Path }
    }
    return $null
}

Write-Host 'Buscando JDK 21 para NeoForge / Gradle 9.2.1...'
Write-Host "JAVA_HOME anterior: $env:JAVA_HOME"
$jdkHome = Find-Jdk21
if (-not $jdkHome) {
    Write-Host 'JDK 21 no encontrado. Descargando Eclipse Temurin 21 (Windows x64)...' -ForegroundColor Yellow
    $jdkDirectory = Join-Path $project '.jdk-bin'
    New-Item -ItemType Directory -Force -Path $jdkDirectory | Out-Null
    $jdkUrl = 'https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jdk/hotspot/normal/eclipse'
    $jdkPartial = Join-Path $jdkDirectory 'jdk21.zip.partial'
    $jdkZip = Join-Path $jdkDirectory 'jdk21.zip'
    try {
        [Net.ServicePointManager]::SecurityProtocol = [Net.ServicePointManager]::SecurityProtocol -bor [Net.SecurityProtocolType]::Tls12
        if (Test-Path -LiteralPath $jdkPartial) { Remove-Item -LiteralPath $jdkPartial -Force }
        Invoke-WebRequest -Uri $jdkUrl -OutFile $jdkPartial -UseBasicParsing -TimeoutSec 1200 -ErrorAction Stop
        if ((Get-Item -LiteralPath $jdkPartial).Length -lt 30000000) {
            throw 'El archivo del JDK esta incompleto.'
        }
        Move-Item -LiteralPath $jdkPartial -Destination $jdkZip -Force
        Write-Host 'Extrayendo Eclipse Temurin 21...'
        Expand-Archive -LiteralPath $jdkZip -DestinationPath $jdkDirectory -Force -ErrorAction Stop
        Remove-Item -LiteralPath $jdkZip -Force
    } catch {
        throw "No se pudo obtener JDK 21: $($_.Exception.Message). Puedes instalar JDK 21 desde https://adoptium.net/temurin/releases/?version=21 o establecer POLYMORPH_JAVA_HOME con su directorio."
    } finally {
        if (Test-Path -LiteralPath $jdkPartial) {
            Remove-Item -LiteralPath $jdkPartial -Force -ErrorAction SilentlyContinue
        }
    }
    $jdkHome = Find-Jdk21
}
if (-not $jdkHome) {
    throw 'No se encontro un JDK 21 valido (java.exe y javac.exe) despues de la instalacion.'
}
$env:JAVA_HOME = $jdkHome
$env:Path = (Join-Path $jdkHome 'bin') + ';' + $env:Path
Write-Host "JDK 21 seleccionado: $env:JAVA_HOME" -ForegroundColor Green
Write-Host "Ejecutable: $(Join-Path $jdkHome 'bin\java.exe')"
Write-Host ''

    $gradleVersion = '9.2.1'
    $gradleDirectory = Join-Path $project '.gradle-bin'
    $localGradle = Join-Path $gradleDirectory "gradle-$gradleVersion\bin\gradle.bat"
    $wrapper = Join-Path $project 'gradlew.bat'

    if (Test-Path -LiteralPath $wrapper) {
        $gradleExecutable = $wrapper
        Write-Host 'Usando Gradle Wrapper del proyecto.'
    } elseif (Test-Path -LiteralPath $localGradle) {
        $gradleExecutable = $localGradle
        Write-Host "Usando Gradle local $gradleVersion."
    } else {
        Write-Host "Gradle local $gradleVersion no encontrado. Preparando descarga..."
        New-Item -ItemType Directory -Force -Path $gradleDirectory | Out-Null
        $distributionUrl = "https://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip"
        $downloadPath = Join-Path $gradleDirectory "gradle-$gradleVersion-bin.zip"
        $partialPath = "$downloadPath.partial"

        try {
            [Net.ServicePointManager]::SecurityProtocol = [Net.ServicePointManager]::SecurityProtocol -bor [Net.SecurityProtocolType]::Tls12
            if (Test-Path -LiteralPath $partialPath) {
                Remove-Item -LiteralPath $partialPath -Force
            }
            if (-not (Test-Path -LiteralPath $downloadPath)) {
                Write-Host "Descargando: $distributionUrl"
                Invoke-WebRequest -Uri $distributionUrl -OutFile $partialPath -UseBasicParsing -TimeoutSec 600 -ErrorAction Stop
                Move-Item -LiteralPath $partialPath -Destination $downloadPath -Force
            } else {
                Write-Host 'Ya existe el ZIP de Gradle. Comprobando contenido...'
            }
            $fileInfo = Get-Item -LiteralPath $downloadPath
            if ($fileInfo.Length -lt 10000000) {
                throw 'El ZIP descargado es demasiado pequeno (posible descarga incompleta).'
            }
            Write-Host 'Extrayendo Gradle. Esta operacion puede demorar...'
            Expand-Archive -LiteralPath $downloadPath -DestinationPath $gradleDirectory -Force -ErrorAction Stop
            if (-not (Test-Path -LiteralPath $localGradle)) {
                throw 'No se encontro gradle.bat despues de extraer el ZIP.'
            }
            Remove-Item -LiteralPath $downloadPath -Force
        } catch {
            Write-Host ''
            Write-Host 'No fue posible descargar o extraer Gradle.' -ForegroundColor Red
            Write-Host "Detalle: $($_.Exception.Message)" -ForegroundColor Red
            Write-Host "Descarga manual: $distributionUrl"
            Write-Host "Extrae la carpeta gradle-$gradleVersion dentro de: $gradleDirectory"
            Write-Host 'Luego vuelve a ejecutar build.bat.'
            if (Test-Path -LiteralPath $downloadPath) {
                Remove-Item -LiteralPath $downloadPath -Force -ErrorAction SilentlyContinue
            }
            throw
        } finally {
            if (Test-Path -LiteralPath $partialPath) {
                Remove-Item -LiteralPath $partialPath -Force -ErrorAction SilentlyContinue
            }
        }
        $gradleExecutable = $localGradle
    }

    Write-Host 'Comprobando version de Java de Gradle:'
    & $gradleExecutable '--version'
    if ($LASTEXITCODE -ne 0) { throw 'Gradle no pudo iniciar con JDK 21.' }

    Write-Host ''
    Write-Host '--- Compilando JAR unico NeoForge ---' -ForegroundColor Cyan
    $arguments = @('--no-daemon', '--console=plain', '--stacktrace', "-Dorg.gradle.java.home=$jdkHome", 'clean', 'build')
    $gradleLog = Join-Path $logDirectory "gradle-build-$stamp.log"
    Write-Host "Registro completo de Gradle: $gradleLog"
    $previousErrorAction = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        & $gradleExecutable @arguments 2>&1 | Tee-Object -FilePath $gradleLog
        $buildExitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousErrorAction
    }
    if ($buildExitCode -ne 0) {
        throw "Gradle termino con codigo $buildExitCode. Revisa $gradleLog"
    }

    $propertiesText = Get-Content -LiteralPath (Join-Path $project 'gradle.properties') -Raw
    $versionMatch = [regex]::Match($propertiesText, '(?m)^\s*mod_version\s*=\s*([^\s#]+)\s*$')
    if (-not $versionMatch.Success) { throw 'No se encontro mod_version en gradle.properties.' }
    $releaseFile = "polymorphic_delight-$($versionMatch.Groups[1].Value)-neoforge-1.21.1.jar"
    $compiledDirectory = Join-Path $project 'build\libs'
    $compiledJar = Join-Path $compiledDirectory $releaseFile
    if (-not (Test-Path -LiteralPath $compiledJar -PathType Leaf)) {
        throw "No se encontro el JAR esperado: $compiledJar"
    }
    Get-ChildItem -LiteralPath $compiledDirectory -Filter '*.jar' -File |
        Where-Object { $_.Name -ne $releaseFile } |
        Remove-Item -Force

    $releaseDirectory = Join-Path $project 'dist'
    New-Item -ItemType Directory -Force -Path $releaseDirectory | Out-Null
    Get-ChildItem -LiteralPath $releaseDirectory -Filter '*.jar' -File | Remove-Item -Force
    Copy-Item -LiteralPath $compiledJar -Destination (Join-Path $releaseDirectory $releaseFile) -Force
    Write-Host "JAR final: $(Join-Path $releaseDirectory $releaseFile)" -ForegroundColor Green
    Write-Host ''
    Write-Host 'Compilacion completada correctamente.' -ForegroundColor Green
} catch {
    $result = 1
    Write-Host ''
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "El registro completo esta en: $log" -ForegroundColor Yellow
} finally {
    if ($startedTranscript) {
        Stop-Transcript | Out-Null
    }
}

exit $result
