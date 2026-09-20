param([switch]$Check)
$ErrorActionPreference = 'Stop'
# Compatibility entry point: the third repository is data-only now.
& (Join-Path $PSScriptRoot 'sync-offline-assets.ps1') -Check:$Check
