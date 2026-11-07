# 需要先设置环境变量：JAVA_HOME（JDK 25）、GRADLE_USER_HOME、SCPMOD_GRADLE（Gradle 9.8.2 目录）
$ErrorActionPreference = 'Stop'
if (-not $env:JAVA_HOME) { throw '请先设置 JAVA_HOME 指向 JDK 25' }
if (-not $env:GRADLE_USER_HOME) { throw '请先设置 GRADLE_USER_HOME' }
if (-not $env:SCPMOD_GRADLE) { throw '请先设置 SCPMOD_GRADLE 指向 Gradle 9.8.2 安装目录' }
if (-not $env:JAVA_TOOL_OPTIONS) { $env:JAVA_TOOL_OPTIONS = '-Djava.net.preferIPv4Stack=true' }
$gradle = Join-Path $env:SCPMOD_GRADLE 'bin\gradle.bat'
Set-Location (Split-Path -Parent $PSScriptRoot)
# Start the dev dedicated server (validates that the mod loads on a server).
& $gradle --no-daemon --console=plain runServer
exit $LASTEXITCODE
