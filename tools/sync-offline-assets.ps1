param([switch]$Check)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$web = Join-Path $projectRoot 'related-projects/TipspromenadQuizWebPage'
$data = Join-Path $projectRoot 'related-projects/Tipspromenad/database'
$android = Join-Path $projectRoot 'app/src/main/assets/quiz'
function Sync-File([string]$SourceFile, [string]$DestinationFile) {
    if ($Check) {
        if (!(Test-Path -LiteralPath $DestinationFile) -or (Get-FileHash -LiteralPath $SourceFile).Hash -ne (Get-FileHash -LiteralPath $DestinationFile).Hash) { throw "Outdated asset: $DestinationFile" }
    } else {
        New-Item -ItemType Directory -Force -Path (Split-Path $DestinationFile -Parent) | Out-Null
        Copy-Item -LiteralPath $SourceFile -Destination $DestinationFile
    }
}
foreach ($file in Get-ChildItem -LiteralPath $data -Filter 'revision-*.json') { Sync-File $file.FullName (Join-Path $web ('Data/' + $file.Name)) }
Sync-File (Join-Path $data 'latest.json') (Join-Path $web 'Data/latest.json')
$assets = @('index.html','script.js','styles.css','walk.css')
foreach ($directory in @('lib','locales','Data')) {
    $assets += Get-ChildItem -LiteralPath (Join-Path $web $directory) -File -Recurse | ForEach-Object { $_.FullName.Substring($web.Length + 1) }
}
foreach ($asset in $assets) { Sync-File (Join-Path $web $asset) (Join-Path $android $asset) }
Write-Output "Canonical database and $($assets.Count) Android assets verified/synced."
