# 命令与事实查询（06-commands）

## [2026-10-31 13:04:47] 记录本阶段全部命令与外部查询
- 状态：事实查询完成，结果已固化为需求基线
- 文件：memory/06-commands.md
- 内容：本地检查命令、外部 API 查询、关键返回
- 原因/命令：用户要求"AI 负责查事实"
- 下一步：实现阶段继续追加

---

## 本地环境事实
| 检查 | 结果 |
|---|---|
| 工作区 E:\SCPMod | **完全为空**（无 git/README/package.json） |
| ~/.dsh/sessions/--E-SCPMod-- | 仅一个 session 文件，内容只有 session 头，**无历史对话** |
| 设备/系统 | Windows（win32），用户目录 <用户目录> |
| Node | v24.18.1（zstd 解压可用） |
| MC 启动器 | <用户目录>\AppData\Roaming\.minecraft（官方启动器）与 E:\Minecraft2\.minecraft（PCL 多版本） |
| 已装 MC 版本 | 1.21 / 26.2 / 26.3 / 26.3-snapshot-10 / 26.4-snapshot-1 |
| 版本清单 | latest.release = **26.3**（2026-09-15）；latest.snapshot = 26.4-snapshot-1（2026-09-22） |
| 26.2 的 Java 要求 | javaVersion = java-runtime-epsilon，majorVersion **25** |
| 本机 JDK | <JDK 25 路径>（JDK 25）；Program Files\Java 有 17/21/23 |
| 既有模组工程 | <JDK 路径>\apophis-26.1.2：Fabric 客户端 mod（Meteor addon），MC 26.1.2、loader 0.19.3、loom 1.17、fabric_api 0.155.2+26.1.2、Java 25、Gradle 9.5.1 |
| Loom 缓存 | <Gradle 缓存>\caches\fabric-loom 下仅有 **26.1.2 与 26.2** 的解析产物 |
| Gradle 发行版 | <Gradle 缓存>\wrapper\dists 最高 9.6.1 |
| Steam 库 | E:\SteamLibrary 下只有 Graphwar II；**无 SCP:SL / GMod / Unity** |
| E: / D: 根目录 | 无任何 SCP 相关现存资产或工程 |

## 外部查询（web_fetch，均为真实返回）
| 查询 | 结果 |
|---|---|
| meta.fabricmc.net/v2/versions/game | Fabric 支持到 **26.3**；26.3/26.2/26.1.2/1.21.11/1.21.1 等均在支持列表 |
| Modrinth 搜索（Fabric mod 数） | 26.3=8948；26.2=13569；26.1.2=13536；1.21.11=18177；1.21.1=19862；1.21.6=11815 |
| Modrinth 搜索（NeoForge/Forge/Quilt） | NeoForge 26.3=5110、1.21.1=21759；Forge 26.3=2599；Quilt 26.3=2658 |
| Modrinth SCP 关键词 | 头部：SCP: Overtime（301,759 下载，版本 1.16.5/1.20.1）、SCP: Foundation Requisites（1.12.2/1.16.5/1.20.1/1.21.1）、SCP: Lockdown、Secure Craft Protect 等；**全部不覆盖 26.x** |
| fabric-api 版本（按游戏版本过滤） | 26.3=0.165.0+26.3；26.2=**0.161.0+26.2**；1.21.1=0.116.17+1.21.1 |
| fabric-example-mod 分支列表 | 含 **26.2** 与 26.3 分支（可自行拉取，无需用户下载） |
| fabric-example-mod 26.2 分支 gradle.properties | minecraft_version=26.2；loader_version=0.20.1；loom_version=1.19-SNAPSHOT；fabric_api_version=0.161.0+26.2 |
| fabric-example-mod 26.2 分支 fabric.mod.json | java >=25；minecraft ~26.2；fabricloader >=0.20.1；splitEnvironmentSourceSets |


## [2026-10-31 21:24:17] M1 环境探测与构建命令
- 状态：构建中（后台任务 pwsh-80）
- 文件：无（只读探测）
- 内容：本机网络、Gradle、JDK 探测结果
- 原因/命令：见下表
- 下一步：读取构建输出，修正编译错误

| 命令 | 结果 |
|---|---|
| nslookup maven.fabricmc.net | 192.168.2.1 / 2606:4700:3031::6815:21f0（DNS 正常） |
| curl -I https://maven.fabricmc.net/ | **200**（宿主机可直连，PowerShell Invoke-WebRequest 失败是 TLS/超时问题，非断网） |
| curl https://services.gradle.org/ | **200** |
| Test-NetConnection 127.0.0.1 常见代理端口 | 全部关闭（无本地代理） |
| 本机 Gradle 发行版 | <Gradle 缓存>\wrapper\dists\gradle-9.5.1-bin\bvvv0hnjcinfezlom7fy9dbyi\gradle-9.5.1（含 bin/gradle.bat） |
| 本机 JDK 25 | <JDK 25 路径>（java.exe/javac.exe 齐全） |
| 编译命令 | `$env:JAVA_HOME=<JDK 25 路径>; $env:GRADLE_USER_HOME=<Gradle 缓存>; gradle.bat --no-daemon --console=plain --stacktrace compileJava` |


## [2026-10-31 21:41:04] 构建环境搭建命令与结果
| 命令/动作 | 结果 |
|---|---|
| Gradle 9.5.1（本机）编译 | **失败**：Loom 1.19.3 要求 Gradle ≥ 9.8.0 |
| wrapper 下载 gradle-9.8.2（services.gradle.org） | 失败：java.net.SocketException: Permission denied: getsockopt（但同一网络 curl 可通） |
| curl 测速 mirrors.cloud.tencent.com | **51 MB/s**（采用） |
| curl 测速 services.gradle.org | 0.1 KB/s |
| 下载并解压 gradle-9.8.2（腾讯镜像） | 成功：<Gradle 路径>\bin\gradle.bat |
| 编译（Gradle 9.8.2 + JDK25） | 失败：解析 net.fabricmc:tiny-remapper:0.15.0 超时（maven.fabricmc.net 太慢） |
| curl 测速 maven.fabricmc.net | 默认/IPv6 极慢或挂起；**加 -4 后 ~23KB/s（可用）** |
| curl 测速 BMCLAPI maven 镜像 | 目标 artifact 404（镜像不可用） |
| curl 测速 libraries.minecraft.net / piston-meta.mojang.com | 200，~300KB/s（可用） |
| 当前做法 | gradle.properties 加 IPv4 + 超时/重试；后台编译任务 pwsh-94 |


## [2026-10-31 21:48:54] 离线编译验证（成功）
- 状态：M1 源码全部通过
- 文件：.compilecheck/javac.args、javac.log（空 = 无错误）
- 内容：命令与结果
- 原因/命令：maven.fabricmc.net 不可用下的替代验证
- 下一步：联网后 Gradle 复验

```
# classpath = <Gradle 缓存>\caches\fabric-loom\26.2\minecraft-merged.jar
#            + <用户目录>\AppData\Roaming\.minecraft\libraries 下全部 jar（排除 net/minecraft|minecraftforge|neoforged 旧版干扰）
#            + E:\Minecraft2\.minecraft\libraries、E:\Minecraft\.minecraft\libraries
# 注意：classpath 必须把 26.2 merged jar 放最前，否则会被 1.18/1.19/1.20 旧 client jar 遮蔽
& '<JDK 25 路径>\bin\javac.exe' '@E:/SCPMod/.compilecheck/javac.args'
# 结果：exit 0，log 0 字节，生成 23 个 class
```

## Gradle 联网构建状态：阻塞
- fabric 依赖缓存停在 11 个文件 / 482KB（约 5KB/s），多次重试无进展 → 已终止任务。
- 结论：**本网络环境下无法完成首次 Gradle 依赖解析**；需换网络/代理后再跑：
  `$env:JAVA_HOME='<JDK 25 路径>'; $env:GRADLE_USER_HOME='<Gradle 缓存>'; & '<Gradle 路径>\bin\gradle.bat' --no-daemon --console=plain build`


## [2026-10-31 23:01:03] 找到可用代理：迷雾通 Geph（关键环境事实）
- 状态：已套用到构建
- 文件：memory/06-commands.md
- 内容：本机代理探测结果与用法
- 原因/命令：用户提示「看看能不能走迷雾通代理」
- 下一步：构建成功后固化到 02-plan/08-next

### 本机代理（探测结果）
| 代理 | 进程 | 端口 | 可用性 |
|---|---|---|---|
| **迷雾通 Geph** | geph5-client (PID 29592) | **<代理端口> (SOCKS5)**、**<代理端口> (HTTP)**、12223 | **可用** |
| Clash Verge | clash-verge (14776) / clash-verge-service | 7890 关闭、7897 极慢、33331 是控制器端口 | 不适用 |
| gephgui-wry | 迷雾通 GUI | 5678 | 仅 GUI 通信 |

### 实测速度（下载 fabric-loader-0.20.1.jar，1.99MB）
| 路径 | 速度 |
|---|---|
| 直连（无代理） | 0.1~1.5 KB/s，频繁超时 |
| Geph HTTP 9910 | **153 KB/s**（1.99MB / 12.7s） |
| Geph SOCKS5 9909 | **167 KB/s**（1.99MB / 11.6s） |

### 用法（PowerShell 里给 JVM 套代理）
```powershell
$env:JAVA_TOOL_OPTIONS='-Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=9910 -Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=9910 -Djava.net.preferIPv4Stack=true'
```
注意：不要用 `java -Dxxx` 直接传参（PowerShell 会把 -D 参数吞掉导致 ClassNotFound），用 JAVA_TOOL_OPTIONS 环境变量。

## [2026-11-01 10:41:51] 重启后复核命令
    # 构建（增量）
    $env:JAVA_HOME='<JDK 25 路径>'
    $env:GRADLE_USER_HOME='<Gradle 缓存>'
    $env:JAVA_TOOL_OPTIONS='-DsocksProxyHost=127.0.0.1 -DsocksProxyPort=9909 -Djava.net.preferIPv4Stack=true'
    & '<Gradle 路径>\bin\gradle.bat' --no-daemon --console=plain build
    # 结果：BUILD SUCCESSFUL in 14s（5 tasks up-to-date）

    # 运行验证
    & '<Gradle 路径>\bin\gradle.bat' --no-daemon --console=plain runServer
    # 见 run/logs/latest.log：framework initialised: blocks=2, scps=1 / Done

## [2026-11-01 11:12:18] 结构层自检命令与结果
    # 自检运行（服务端）
    $env:JAVA_TOOL_OPTIONS='-DsocksProxyHost=127.0.0.1 -DsocksProxyPort=9909 -Djava.net.preferIPv4Stack=true -Dscpmod.selftest=true'
    gradle.bat --no-daemon --console=plain runServer
    # 关键日志：
    # [selftest] blueprint build at (...) -> ops=934 units=2 terminals=2
    # [selftest] start type=... valid=true box=[...]
    # [selftest] scanned=53792 stone_bricks=615 containment_units=2 terminals=2

## [2026-11-01 12:35:25] 事件链自检结果（服务端 headless）
    # -Dscpmod.selftest=true 后 runServer 的关键日志：
    # [selftest] blueprint build at (40,74,-24) -> ops=934 units=2 terminals=2
    # [selftest] event test started=true scp=scpmod:scp_173
    # [selftest] event phase=WARNING remainingTicks=50
    # [selftest] event phase=BREACHED after 60 ticks
    # [selftest] recontain attempted ok=true -> phase=CONTAINED
    # [selftest] event phase=CONTAINED, stats: triggered=1, recontained=1, forced=0, breachedSeconds=2
    # run/config/scpmod_stats.json 已落盘：{eventsTriggered:1, eventsRecontained:1, eventsEndedByForce:0, totalBreachedSeconds:2}

## [2026-11-01 19:02:19] 异常实体自检结果
    # -Dscpmod.selftest=true runServer：
    # [scpmod] anomaly body spawned at BlockPos{x=16, y=72, z=-48}
    # [selftest] event phase=BREACHED after 60 ticks, anomalyBodyActive=true
    # [scpmod] anomaly body removed
    # [selftest] event phase=CONTAINED, anomalyBodyActive=false, stats: triggered=3, recontained=3
    # 结论：事件链 <-> 实体生命周期绑定正确；注视/逼近/伤害行为需真实玩家（客户端）验证

## [2026-11-01 19:12:08] 原文抓取与依据
- web_fetch https://scp-wiki-cn.wikidot.com/scp-173 （HTTP 200，38KB）
- 关键原文：「SCP-173会移动且具有高度敌意。该物体在直接视线内无法移动。与SCP-173之间的视线时刻都不得中断。」「攻击方式为折断头骨底处的颈部，或是勒颈。」
- 自检复验：anomaly body spawned / removed、event BREACHED→CONTAINED 均正常（无玩家时行为分支不触发）

## [2026-11-01 19:32:21] 生成本次验证日志
    # selftest 关键行：
    # [selftest] event test started=true scp=scpmod:scp_173 neckSnapDamageTypeLoaded=true
    # [selftest] event phase=BREACHED ... anomalyBodyActive=true
    # [selftest] event phase=CONTAINED ... stats: triggered=5, recontained=5
    # 注意：本次构建/启动明显变慢（gradle 报 1.7GB 内存请求、可用仅 1GB），原因是 VS Code Java 扩展等进程占用内存，属环境问题不是代码问题

## [2026-11-01 19:33:02] Git 命令速查
    git init -b main
    git config user.name scpmod; git config user.email scpmod@localhost
    git add -A; git commit -m "..."
    git tag -a baseline-20261101 -m "Baseline"
    git log --oneline --decorate --graph
    git status --porcelain
    git ls-files | wc -l
    # 回退
    git restore <file>              # 撤销未暂存改动
    git restore --staged <file>     # 取消暂存
    git revert <hash>               # 反向提交（推荐，保留历史）
    git switch -c fix-xxx baseline-20261101   # 从基线开新分支试错
    git reset --hard <hash>         # 强制回退（会丢工作区改动，慎用）

## [2026-11-01 20:23:38] 测试命令与实跑结果
    # L0
    powershell -ExecutionPolicy Bypass -File scripts\build.ps1
    # L1（实跑结果：7/7 PASS，EXIT=0）
    powershell -ExecutionPolicy Bypass -File scripts\selftest.ps1
    # L2
    powershell -ExecutionPolicy Bypass -File scripts\run-server.ps1
    # L3（首次会下载 ~200MB assets）
    $env:JAVA_TOOL_OPTIONS='-DsocksProxyHost=127.0.0.1 -DsocksProxyPort=9909 -Djava.net.preferIPv4Stack=true'
    & '<Gradle 路径>\bin\gradle.bat' --no-daemon --console=plain runClient
    # 实跑输出：
    # [PASS] structure generated in world
    # [PASS] event reached BREACHED
    # [PASS] anomaly body spawned
    # [PASS] event re-contained
    # [PASS] anomaly body removed
    # [PASS] neck_snap damage type loaded
    # [PASS] server finished starting
    # ALL CHECKS PASSED

## [2026-11-01 21:51:51] L3 客户端实测记录
    # 启动（首次下载 assets 460MB+，走 Geph）
    & '<Gradle 路径>\bin\gradle.bat' --no-daemon --console=plain runClient
    # 实测时用户操作：/scp scps → /scp event start（3,-60,9）→ 被异常击杀
    # 日志问题统计：anomaly snapped 日志 260 条/55 秒 → 已修
    # 客户端日志：run/logs/latest.log

## [2026-11-01 22:22:34] L3 第二轮日志要点
    # anomaly body spawned at BlockPos{x=3, y=-60, z=0}      ← 创造栏生成器可用
    # Player 被 SCP-173 折断了颈部                        ← 死亡消息正确渲染
    # anomaly snapped ... 17 条 / ~60s                       ← 冷却生效（不再 260 条）
    # 无 model/texture/creative tab 相关警告或异常
