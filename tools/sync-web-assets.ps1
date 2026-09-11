param([switch]$Check)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$canonical = Join-Path $projectRoot 'related-projects/TipspromenadQuizWebPage'
$consumer = Join-Path $projectRoot 'related-projects/Tipspromenad/public'
$assets = @('lib/quiz-core.js', 'lib/i18n.js', 'lib/quiz-ui.js', 'locales/ui.json', 'Data/multilingual.json', 'styles.css')
foreach ($asset in $assets) {
    $sourceFile = Join-Path $canonical $asset
    $destinationFile = Join-Path $consumer $asset
    if (-not (Test-Path -LiteralPath $sourceFile -PathType Leaf)) { throw "Missing canonical asset: $sourceFile" }
    if ($Check) {
        if (-not (Test-Path -LiteralPath $destinationFile -PathType Leaf)) { throw "Missing distribution: $destinationFile" }
        if ((Get-FileHash -LiteralPath $sourceFile).Hash -ne (Get-FileHash -LiteralPath $destinationFile).Hash) { throw "Outdated distribution: $asset" }
    } else {
        New-Item -ItemType Directory -Force -Path (Split-Path $destinationFile -Parent) | Out-Null
        Copy-Item -LiteralPath $sourceFile -Destination $destinationFile
    }
    Write-Output "$asset OK"
}
