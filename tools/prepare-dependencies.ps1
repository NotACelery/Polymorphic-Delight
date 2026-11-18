param([Parameter(Mandatory=$true)][ValidateSet('26.2','26.3')][string]$MinecraftVersion)
$ErrorActionPreference = 'Stop'

$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$libs = Join-Path $project 'libs'
New-Item -ItemType Directory -Force -Path $libs | Out-Null
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
$clientName = 'PolymorphicDelight/1.0 (https://github.com/NotACelery/Polymorphic-Delight)'

function Test-ValidJar([string]$Path) {
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) { return $false }
    try {
        $archive = [System.IO.Compression.ZipFile]::OpenRead($Path)
        try { return ($archive.Entries.Count -gt 0) }
        finally { $archive.Dispose() }
    } catch { return $false }
}

function Get-PublishedVersion([string]$ProjectId, [string]$ExpectedVersion) {
    $loaders = [uri]::EscapeDataString('["fabric"]')
    $game = [uri]::EscapeDataString('["' + $MinecraftVersion + '"]')
    $uri = "https://api.modrinth.com/v2/project/$ProjectId/version?loaders=$loaders&game_versions=$game"
    Write-Host "Consultando versiones Fabric $MinecraftVersion en Modrinth: $ProjectId"
    $versions = @(Invoke-RestMethod -Uri $uri -Headers @{ 'User-Agent' = $clientName; 'Accept' = 'application/json' } -TimeoutSec 90)
    $match = @($versions | Where-Object { $_.version_number -eq $ExpectedVersion })
    if ($match.Count -eq 0 -and $ProjectId -eq '3uT4J18I') {
        $match = @($versions | Where-Object { $_.version_number -eq "$ExpectedVersion-fabric" })
    }
    if ($match.Count -eq 0) { throw "No existe la version exacta $ExpectedVersion del proyecto $ProjectId para Fabric $MinecraftVersion en Modrinth. Verifica la publicacion, no uses una version distinta automaticamente." }
    return $match[0]
}

function Download-Jar([object]$Version, [string]$Destination) {
    if (Test-ValidJar $Destination) {
        Write-Host "Dependencia existente: $(Split-Path -Leaf $Destination)"
        return
    }
    if (Test-Path -LiteralPath $Destination) { Remove-Item -LiteralPath $Destination -Force }
    $jarFiles = @($Version.files | Where-Object { $_.filename -like '*.jar' -and $_.url -like 'https://*' })
    if ($jarFiles.Count -eq 0) { throw "No existe JAR descargable para $($Version.version_number)." }
    $asset = @($jarFiles | Where-Object { $_.primary -eq $true } | Select-Object -First 1)
    if ($asset.Count -eq 0) { $asset = @($jarFiles | Select-Object -First 1) }
    $file = $asset[0]
    $partial = "$Destination.partial"
    try {
        if (Test-Path -LiteralPath $partial) { Remove-Item -LiteralPath $partial -Force }
        Write-Host "Descargando $($file.filename)"
        Invoke-WebRequest -Uri $file.url -OutFile $partial -Headers @{ 'User-Agent' = $clientName } -UseBasicParsing -TimeoutSec 900
        if (-not (Test-ValidJar $partial)) { throw "El archivo de $($Version.version_number) no es un JAR valido." }
        if ($file.hashes.sha512) {
            $actual = (Get-FileHash -LiteralPath $partial -Algorithm SHA512).Hash
            if ($actual -ine $file.hashes.sha512) { throw "SHA-512 incorrecto para $($file.filename)." }
        } elseif ($file.hashes.sha1) {
            $actual = (Get-FileHash -LiteralPath $partial -Algorithm SHA1).Hash
            if ($actual -ine $file.hashes.sha1) { throw "SHA-1 incorrecto para $($file.filename)." }
        }
        Move-Item -LiteralPath $partial -Destination $Destination -Force
    } finally {
        if (Test-Path -LiteralPath $partial) { Remove-Item -LiteralPath $partial -Force }
    }
}

function Extract-EmbeddedCca([string]$PolymorphJar) {
    $zip = [System.IO.Compression.ZipFile]::OpenRead($PolymorphJar)
    try {
        foreach ($module in @('base','block')) {
            $entry = @($zip.Entries | Where-Object {
                $_.FullName -like "META-INF/jars/cardinal-components-$module-*.jar"
            } | Select-Object -First 1)
            if ($entry.Count -eq 0) { throw "Polymorph+ no incluye Cardinal Components $module; revisa la version publicada." }
            $destination = Join-Path $libs $entry[0].Name
            if (-not (Test-ValidJar $destination)) {
                Write-Host "Extrayendo dependencia de compilacion: $($entry[0].Name)"
                [System.IO.Compression.ZipFileExtensions]::ExtractToFile($entry[0], $destination, $true)
            }
        }
    } finally { $zip.Dispose() }
}

$fdrVersion = "$MinecraftVersion-3.6.30"
$polyVersion = "1.5.0+$MinecraftVersion.0"
$fdrJar = Join-Path $libs "FarmersDelight-$fdrVersion+refabricated.jar"
$polyJar = Join-Path $libs "polymorph_plus-fabric-$polyVersion.jar"
if (-not (Test-ValidJar $fdrJar)) {
    Download-Jar (Get-PublishedVersion '7vxePowz' $fdrVersion) $fdrJar
}
if (-not (Test-ValidJar $polyJar)) {
    Download-Jar (Get-PublishedVersion '3uT4J18I' $polyVersion) $polyJar
}
Extract-EmbeddedCca $polyJar
Write-Host 'Dependencias Fabric verificadas y preparadas.' -ForegroundColor Green
