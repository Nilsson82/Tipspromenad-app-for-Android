$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
$projectRoot = Split-Path $PSScriptRoot -Parent
$outputDirectory = Join-Path $projectRoot 'deliverables'
New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null
foreach ($name in @('TipspromenadQuizWebPage', 'Tipspromenad')) {
    $repository = Join-Path $projectRoot "related-projects/$name"
    $archivePath = Join-Path $outputDirectory "$name-phase1.zip"
    # Only tracked/new project files, excluding Git internals, installed packages and caches.
    $files = @(& git -C $repository -c core.quotepath=false ls-files --cached --others --exclude-standard) | Sort-Object -Unique
    if ($LASTEXITCODE -ne 0) { throw "Cannot list $name" }
    $stream = [System.IO.File]::Open($archivePath, [System.IO.FileMode]::Create)
    $archive = [System.IO.Compression.ZipArchive]::new($stream, [System.IO.Compression.ZipArchiveMode]::Create)
    try {
        foreach ($relative in $files) {
            $source = Join-Path $repository $relative
            if (Test-Path -LiteralPath $source -PathType Leaf) {
                [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($archive, $source, $relative.Replace('\', '/')) | Out-Null
            }
        }
    } finally { $archive.Dispose(); $stream.Dispose() }
    Get-Item -LiteralPath $archivePath | Select-Object FullName, Length
}
