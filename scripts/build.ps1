# Build scpmod (Minecraft 26.2 / Fabric).
# Requires JDK 25 + Gradle 9.8.2 + a working proxy (Geph) for first-time dependency download.
$ErrorActionPreference = 'Stop'
$env:JAVA_HOME = '<JDK 25 路径>'
$env:GRADLE_USER_HOME = '<Gradle 缓存>'
$env:JAVA_TOOL_OPTIONS = '-DsocksProxyHost=127.0.0.1 -DsocksProxyPort=9909 -Djava.net.preferIPv4Stack=true'
$GRADLE_HOME = '<Gradle 路径>'
Set-Location (Split-Path -Parent $PSScriptRoot)
& "$GRADLE_HOME\bin\gradle.bat" --no-daemon --console=plain build
exit $LASTEXITCODE
