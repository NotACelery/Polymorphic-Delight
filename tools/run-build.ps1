$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
Set-Location -LiteralPath $project

$logDirectory = Join-Path $project 'build-logs'
New-Item -ItemType Directory -Force -Path $logDirectory | Out-Null
$stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$log = Join-Path $logDirectory "fabric-build-$stamp.log"
$gradleLog = Join-Path $logDirectory "gradle-build-$stamp.log"
$startedTranscript = $false
$result = 1

function Test-Jdk25Home {
    param([string]$HomePath)
    if ([string]::IsNullOrWhiteSpace($HomePath)) { return $false }
    $candidate = [Environment]::ExpandEnvironmentVariables($HomePath.Trim().Trim('"'))
    if (-not [System.IO.Path]::IsPathRooted($candidate)) { return $false }
    if (-not (Test-Path -LiteralPath $candidate -PathType Container -ErrorAction SilentlyContinue)) { return $false }
    $javaExe = [System.IO.Path]::Combine($candidate, 'bin', 'java.exe')
    $javacExe = [System.IO.Path]::Combine($candidate, 'bin', 'javac.exe')
    if (-not (Test-Path -LiteralPath $javaExe -PathType Leaf -ErrorAction SilentlyContinue)) { return $false }
    if (-not (Test-Path -LiteralPath $javacExe -PathType Leaf -ErrorAction SilentlyContinue)) { return $false }

    try {
        $startInfo = New-Object System.Diagnostics.ProcessStartInfo
        $startInfo.FileName = $javaExe
        $startInfo.Arguments = '-version'
        $startInfo.UseShellExecute = $false
        $startInfo.CreateNoWindow = $true
        $startInfo.RedirectStandardError = $true
        $startInfo.RedirectStandardOutput = $true
        $process = [System.Diagnostics.Process]::Start($startInfo)
        if ($null -eq $process) { return $false }
        $versionText = $process.StandardError.ReadToEnd() + $process.StandardOutput.ReadToEnd()
        $process.WaitForExit()
        $match = [regex]::Match($versionText, 'version\s+"(\d+)')
        return ($process.ExitCode -eq 0 -and $match.Success -and [int]$match.Groups[1].Value -eq 25)
    } catch {
        return $false
    }
}

function Find-Jdk25 {
    $candidates = New-Object 'System.Collections.Generic.List[string]'
    foreach ($name in @('POLYMORPH_JAVA_HOME', 'JAVA25_HOME', 'JDK25_HOME', 'JAVA_HOME')) {
        $value = [Environment]::GetEnvironmentVariable($name)
        if (-not [string]::IsNullOrWhiteSpace($value)) { $candidates.Add($value) }
    }

    foreach ($pathEntry in ($env:Path -split ';')) {
        if ([string]::IsNullOrWhiteSpace($pathEntry)) { continue }
        $dir = [Environment]::ExpandEnvironmentVariables($pathEntry.Trim().Trim('"'))
        if (-not [System.IO.Path]::IsPathRooted($dir)) { continue }
        $javaExe = [System.IO.Path]::Combine($dir, 'java.exe')
        if (Test-Path -LiteralPath $javaExe -PathType Leaf -ErrorAction SilentlyContinue) {
            $candidates.Add((Split-Path -Parent $dir))
        }
    }

    $roots = @(
        (Join-Path $project '.jdk-bin'),
        (Join-Path $env:ProgramFiles 'Eclipse Adoptium'),
        (Join-Path $env:ProgramFiles 'Java'),
        (Join-Path $env:ProgramFiles 'Microsoft'),
        (Join-Path $env:ProgramFiles 'Amazon Corretto'),
        (Join-Path $env:ProgramFiles 'Zulu'),
        (Join-Path $env:ProgramFiles 'BellSoft'),
        (Join-Path $env:LOCALAPPDATA 'Programs\Eclipse Adoptium'),
        (Join-Path $env:USERPROFILE '.jdks'),
        (Join-Path $env:USERPROFILE '.gradle\jdks')
    )
    foreach ($root in $roots) {
        if ([string]::IsNullOrWhiteSpace($root)) { continue }
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
        if (Test-Jdk25Home -HomePath $path) {
            return (Resolve-Path -LiteralPath ([Environment]::ExpandEnvironmentVariables($path.Trim().Trim('"')))).Path
        }
    }
    return $null
}

function Assert-ZipDownload {
    param([string]$FilePath, [long]$MinimumSize)
    if (-not (Test-Path -LiteralPath $FilePath -PathType Leaf)) {
        throw "Archivo no encontrado: $FilePath"
    }
    if ((Get-Item -LiteralPath $FilePath).Length -lt $MinimumSize) {
        throw "Descarga incompleta o demasiado pequena: $FilePath"
    }
    $stream = [System.IO.File]::OpenRead($FilePath)
    try {
        $bytes = New-Object byte[] 4
        if ($stream.Read($bytes, 0, 4) -ne 4 -or $bytes[0] -ne 0x50 -or $bytes[1] -ne 0x4B -or
            $bytes[2] -ne 0x03 -or $bytes[3] -ne 0x04) {
            throw "El archivo descargado no es un ZIP valido: $FilePath"
        }
    } finally {
        $stream.Dispose()
    }
}

function Download-OfficialZip {
    param([string]$Url, [string]$Destination, [long]$MinimumSize)
    $partial = "$Destination.partial"
    if (Test-Path -LiteralPath $partial) { Remove-Item -LiteralPath $partial -Force }
    try {
        Write-Host "Descargando: $Url"
        Invoke-WebRequest -Uri $Url -OutFile $partial -UseBasicParsing -TimeoutSec 1200 -ErrorAction Stop
        Assert-ZipDownload -FilePath $partial -MinimumSize $MinimumSize
        Move-Item -LiteralPath $partial -Destination $Destination -Force
    } finally {
        if (Test-Path -LiteralPath $partial) {
            Remove-Item -LiteralPath $partial -Force -ErrorAction SilentlyContinue
        }
    }
}

function Get-GradleExecutable {
    $wrapper = Join-Path $project 'gradlew.bat'
    $wrapperJar = Join-Path $project 'gradle\wrapper\gradle-wrapper.jar'
    if ((Test-Path -LiteralPath $wrapper -PathType Leaf) -and
        (Test-Path -LiteralPath $wrapperJar -PathType Leaf)) {
        Write-Host 'Usando el Gradle Wrapper del proyecto.'
        return $wrapper
    }

    $gradleVersion = '9.4.1'
    $gradleDirectory = Join-Path $project '.gradle-bin'
    $localGradle = Join-Path $gradleDirectory "gradle-$gradleVersion\bin\gradle.bat"
    if (Test-Path -LiteralPath $localGradle -PathType Leaf) {
        Write-Host "Usando Gradle $gradleVersion local."
        return $localGradle
    }

    New-Item -ItemType Directory -Path $gradleDirectory -Force | Out-Null
    $url = "https://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip"
    $zip = Join-Path $gradleDirectory "gradle-$gradleVersion-bin.zip"
    Write-Host "Gradle $gradleVersion no encontrado. Descarga automatica..." -ForegroundColor Yellow
    try {
        if (Test-Path -LiteralPath $zip) {
            try { Assert-ZipDownload -FilePath $zip -MinimumSize 10000000 }
            catch { Remove-Item -LiteralPath $zip -Force }
        }
        if (-not (Test-Path -LiteralPath $zip)) {
            Download-OfficialZip -Url $url -Destination $zip -MinimumSize 10000000
        }
        Expand-Archive -LiteralPath $zip -DestinationPath $gradleDirectory -Force
        if (-not (Test-Path -LiteralPath $localGradle -PathType Leaf)) {
            throw 'No aparece gradle.bat despues de extraer Gradle.'
        }
        Remove-Item -LiteralPath $zip -Force
        return $localGradle
    } catch {
        throw "No se pudo preparar Gradle $gradleVersion. $($_.Exception.Message). Descarga manual: $url"
    }
}

try {
    Start-Transcript -Path $log -Force | Out-Null
    $startedTranscript = $true
    Write-Host '============================================'
    Write-Host ' Polymorphic Delight - Fabric 26.3'
    Write-Host '============================================'
    Write-Host "Proyecto: $project"
    Write-Host "Registro: $log"
    Write-Host 'Version objetivo: Minecraft 26.3 (compatibilidad declarada ~26.3)'
    Write-Host ''

    Write-Host 'Buscando JDK 25 instalado, incluyendo Eclipse Temurin...'
    Write-Host "JAVA_HOME anterior: $env:JAVA_HOME"
    $jdkHome = Find-Jdk25
    if (-not $jdkHome) {
        Write-Host 'No hay un JDK 25 instalado o detectable. Descargando Eclipse Temurin 25...' -ForegroundColor Yellow
        $jdkDirectory = Join-Path $project '.jdk-bin'
        New-Item -ItemType Directory -Force -Path $jdkDirectory | Out-Null
        $jdkZip = Join-Path $jdkDirectory 'jdk25.zip'
        $url = 'https://api.adoptium.net/v3/binary/latest/25/ga/windows/x64/jdk/hotspot/normal/eclipse'
        try {
            [Net.ServicePointManager]::SecurityProtocol = [Net.ServicePointManager]::SecurityProtocol -bor [Net.SecurityProtocolType]::Tls12
            if (Test-Path -LiteralPath $jdkZip) {
                try { Assert-ZipDownload -FilePath $jdkZip -MinimumSize 30000000 }
                catch { Remove-Item -LiteralPath $jdkZip -Force }
            }
            if (-not (Test-Path -LiteralPath $jdkZip)) {
                Download-OfficialZip -Url $url -Destination $jdkZip -MinimumSize 30000000
            }
            Write-Host 'Extrayendo JDK 25 en .jdk-bin...'
            Expand-Archive -LiteralPath $jdkZip -DestinationPath $jdkDirectory -Force
            Remove-Item -LiteralPath $jdkZip -Force
        } catch {
            throw "No se pudo obtener Temurin 25: $($_.Exception.Message). Puedes instalarlo o indicar POLYMORPH_JAVA_HOME."
        }
        $jdkHome = Find-Jdk25
    }
    if (-not $jdkHome) {
        throw 'No se encontro un JDK 25 valido con java.exe y javac.exe.'
    }
    $env:JAVA_HOME = $jdkHome
    $env:Path = (Join-Path $jdkHome 'bin') + ';' + $env:Path
    Write-Host "JDK 25 seleccionado: $jdkHome" -ForegroundColor Green
    Write-Host ''

    & (Join-Path $PSScriptRoot "prepare-dependencies.ps1") -MinecraftVersion "26.3"

    $gradle = Get-GradleExecutable
    Write-Host 'Comprobando Java usado por Gradle...'
    $previousErrorAction = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        & $gradle '--version' 2>&1 | ForEach-Object { Write-Host $_ }
        $gradleVersionExitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousErrorAction
    }
    if ($gradleVersionExitCode -ne 0) { throw 'Gradle no pudo iniciar con Java 25.' }

    $propertiesText = Get-Content -LiteralPath (Join-Path $project 'gradle.properties') -Raw
    $versionMatch = [regex]::Match($propertiesText, '(?m)^\s*mod_version\s*=\s*([^\s#]+)')
    if (-not $versionMatch.Success) { throw 'No existe mod_version en gradle.properties.' }
    $releaseFile = "polymorphic_delight-$($versionMatch.Groups[1].Value).jar"

    Write-Host ''
    Write-Host '--- Compilando Fabric 26.3 ---' -ForegroundColor Cyan
    Write-Host "Registro completo de Gradle: $gradleLog"
    $arguments = @('--no-daemon', '--console=plain', '--stacktrace', "-Dorg.gradle.java.home=$jdkHome", 'clean', 'build')
    $previousErrorAction = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        & $gradle @arguments 2>&1 | Tee-Object -FilePath $gradleLog
        $buildExitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousErrorAction
    }
    if ($buildExitCode -ne 0) {
        throw "Gradle termino con codigo $buildExitCode. Revisa $gradleLog"
    }

    $compiledDirectory = Join-Path $project 'build\libs'
    $compiledJar = Join-Path $compiledDirectory $releaseFile
    if (-not (Test-Path -LiteralPath $compiledJar -PathType Leaf)) {
        throw "No se genero el JAR esperado: $compiledJar"
    }

    Get-ChildItem -LiteralPath $compiledDirectory -Filter '*.jar' -File |
        Where-Object { $_.Name -ne $releaseFile } | Remove-Item -Force
    $releaseDirectory = Join-Path $project 'dist'
    New-Item -ItemType Directory -Path $releaseDirectory -Force | Out-Null
    Get-ChildItem -LiteralPath $releaseDirectory -Filter '*.jar' -File |
        Remove-Item -Force
    Copy-Item -LiteralPath $compiledJar -Destination (Join-Path $releaseDirectory $releaseFile) -Force
    Write-Host ''
    Write-Host "JAR final: $(Join-Path $releaseDirectory $releaseFile)" -ForegroundColor Green
    Write-Host 'Compilacion terminada correctamente.' -ForegroundColor Green
    $result = 0
} catch {
    Write-Host ''
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Registro de ejecucion: $log" -ForegroundColor Yellow
    Write-Host "Registro de Gradle:   $gradleLog" -ForegroundColor Yellow
    $result = 1
} finally {
    if ($startedTranscript) { Stop-Transcript | Out-Null }
}
exit $result
