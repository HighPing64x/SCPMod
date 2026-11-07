# Headless self-test for scpmod.
# Runs the dev dedicated server with -Dscpmod.selftest=true, waits for the expected log
# markers and reports PASS/FAIL. Needs: JDK 25, Gradle 9.8.2, Geph proxy running (first run).
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
$env:JAVA_HOME = '<JDK 25 路径>'
$env:GRADLE_USER_HOME = '<Gradle 缓存>'
$env:JAVA_TOOL_OPTIONS = '-DsocksProxyHost=127.0.0.1 -DsocksProxyPort=9909 -Djava.net.preferIPv4Stack=true -Dscpmod.selftest=true'
$gradle = '<Gradle 路径>\bin\gradle.bat'
$consoleLog = Join-Path $root 'selftest-console.log'
$serverLog = Join-Path $root 'run\logs\latest.log'

New-Item -ItemType Directory -Force -Path (Join-Path $root 'run') | Out-Null
if (-not (Test-Path (Join-Path $root 'run\eula.txt'))) { 'eula=true' | Set-Content (Join-Path $root 'run\eula.txt') }
Remove-Item -Recurse -Force (Join-Path $root 'run\logs') -ErrorAction SilentlyContinue
Remove-Item -Force $consoleLog -ErrorAction SilentlyContinue
Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.Path -like '*jdk-25*' } | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2

Write-Host 'Starting dev server (this can take 1-6 minutes)...'
$job = Start-Job -ArgumentList $root, $gradle, $consoleLog -ScriptBlock {
  param($workDir, $gradlePath, $outFile)
  Set-Location $workDir
  & $gradlePath --no-daemon --console=plain runServer *> $outFile
}

$checks = [ordered]@{
  'structure generated in world' = 'scanned=\d+ stone_bricks=[1-9]\d*'
  'event reached BREACHED'       = 'event phase=BREACHED'
  'anomaly body spawned'         = 'anomaly body spawned'
  'event re-contained'           = 'event phase=CONTAINED'
  'anomaly body removed'         = 'anomaly body removed'
  'neck_snap damage type loaded' = 'neckSnapDamageTypeLoaded=true'
  'server finished starting'     = 'Done \('
}
$results = @{}
$deadline = (Get-Date).AddMinutes(12)
while ((Get-Date) -lt $deadline) {
  if (Test-Path $serverLog) {
    $text = Get-Content -Raw $serverLog -ErrorAction SilentlyContinue
    foreach ($key in $checks.Keys) {
      if (-not $results.ContainsKey($key) -and $text -match $checks[$key]) { $results[$key] = $true }
    }
    if ($results.Count -eq $checks.Count) { break }
  }
  if ($job.State -eq 'Failed') { break }
  Start-Sleep -Seconds 2
}

Stop-Job $job -ErrorAction SilentlyContinue
Get-Job | Remove-Job -Force -ErrorAction SilentlyContinue
Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.Path -like '*jdk-25*' } | Stop-Process -Force -ErrorAction SilentlyContinue

Write-Host ''
Write-Host '=== scpmod headless self-test ==='
$failed = 0
foreach ($key in $checks.Keys) {
  if ($results.ContainsKey($key)) { Write-Host ('[PASS] ' + $key) } else { Write-Host ('[FAIL] ' + $key); $failed++ }
}
Write-Host ''
if ($failed -gt 0) {
  if (Test-Path $consoleLog) {
    Write-Host '--- last console lines ---'
    Get-Content $consoleLog -Tail 20 | ForEach-Object { Write-Host $_ }
  } else {
    Write-Host "console log was not created: $consoleLog"
  }
  Write-Host "$failed check(s) failed"
  exit 1
}
Write-Host 'ALL CHECKS PASSED'
exit 0
