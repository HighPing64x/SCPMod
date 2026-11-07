# Start the dev dedicated server (validates that the mod loads on a server).
$ErrorActionPreference = 'Stop'
$env:JAVA_HOME = '<JDK 25 路径>'
$env:GRADLE_USER_HOME = '<Gradle 缓存>'
$env:JAVA_TOOL_OPTIONS = '-DsocksProxyHost=127.0.0.1 -DsocksProxyPort=9909 -Djava.net.preferIPv4Stack=true'
$GRADLE_HOME = '<Gradle 路径>'
Set-Location (Split-Path -Parent $PSScriptRoot)
& "$GRADLE_HOME\bin\gradle.bat" --no-daemon --console=plain runServer
exit $LASTEXITCODE
