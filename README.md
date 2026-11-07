# SCPMod

SCP 题材的 Minecraft **Fabric** 模组（目标 **MC 26.2**，Java 25）。

无主线剧情；沙盒向。玩家探索随机生成的大型收容站点，用收容单元与操作终端处置异常；
收容失效会触发事件链（预警 → 突破 → 异常脱逃 → 重新收容 → 结算统计）。

## 当前进度

- [x] 工程基底（Fabric 26.2 / Gradle 9.8.2 / JDK 25，包名 `com.scpmod`）
- [x] 收容单元 + 操作终端方块（含方块实体、拆解泄压、终端再收容）
- [x] 结构层：代码生成的收容站点（worldgen JSON + StructurePiece）
- [x] 收容失效事件链（Boss 条预警、突破、再收容、统计持久化）
- [x] SCP-173 原型：服务器端 AI（注视冻结 / 未观察瞬移贴脸 / 断颈秒杀 / 自定义死亡消息 / 石脚步声），身体用 ArmorStand 占位
- [ ] 客户端实机验证（Boss 条 UI、死亡消息、音效、手感）
- [ ] GameTest 自动化测试
- [x] Jigsaw 模板化（template_pool + .nbt 房间模板，多房间随机拼接）

## 开发环境（本机）

- JDK 25：`<JDK 25 路径>`
- Gradle 9.8.2：`<Gradle 路径>`（Loom 1.19.3 要求 Gradle ≥ 9.8）
- `GRADLE_USER_HOME=<Gradle 缓存>`
- 首次依赖下载需要代理：迷雾通 SOCKS5 `<代理端口>` / HTTP `<代理端口>`

## 构建 / 运行

`powershell
# 一键构建（已内置 JDK/Gradle/代理设置）
powershell -ExecutionPolicy Bypass -File scripts\build.ps1

# 启动开发用专用服务器
powershell -ExecutionPolicy Bypass -File scripts\run-server.ps1

# 服务端自检（结构生成 + 事件链 + 异常生命周期）
$env:JAVA_TOOL_OPTIONS='-DsocksProxyHost=127.0.0.1 -DsocksProxyPort=9909 -Djava.net.preferIPv4Stack=true -Dscpmod.selftest=true'
& '<Gradle 路径>\bin\gradle.bat' --no-daemon --console=plain runServer
`

产物：`build/libs/scpmod-0.1.0.jar`

## 游戏内命令

| 命令 | 说明 |
|---|---|
| `/scp status` | 事件阶段 / SCP 数量 / 配置开关 |
| `/scp stats` | 结算统计（触发/再收容/强制终止/脱逃秒数） |
| `/scp event start` / `stop` | 手动触发/终止收容失效事件 |
| `/scp station build` | 在玩家脚下直接生成一个收容站点（调试） |
| `/scp locate` | 预留（结构查找） |
| `/scp unit set <scp>` / `empty` | 设置视线所指收容单元的内容（调试） |
| `/scp look` / `scps` / `reload` | 调试与配置重载 |

## 配置（三个文件）

- `config/scpmod.json`：框架开关 —— `breachEventsEnabled`、`environmentalTriggerChance`、`debugLogging`
- `config/scpmod-events.json`：事件时序 —— `breachWarningSeconds`（**默认 30**）、`containedDisplaySeconds`、`eventsEnabled`
- `config/scpmod-anomalies.json`：异常行为 —— `observationFovDegrees`（**默认 100°，判定"在屏幕上"的视场角**）、`viewDistance`、`closeRangeObserved`、`maxStep`、`killRange`、`neckSnapDamage`、`ignoreCreativePlayers`、`respawnProtectionSeconds`（**默认 5 秒防刷人头**）、`attackCooldownTicks`、`ambientWarningRange`（**默认 32 格：进入/离开范围时的氛围小标题**）、`ambientWarningHysteresis`、`ambientMessagesEnabled`
- `config/scpmod_stats.json`：结算统计（跨世界累计）
- 改完文件后在游戏里 `/scp reload` 即可热重载（stats 无需重载）

## 文档 / 记忆

项目决策、进度、错误与恢复入口都在 `memory/`（从 `memory/00-index.md` 开始读）。