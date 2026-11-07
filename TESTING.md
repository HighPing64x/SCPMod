# 如何测试 SCPMod

分四层：从最快的自动检查到最完整的人工验收。

## 0. 构建检查（秒级~分钟级）

```powershell
powershell -ExecutionPolicy Bypass -File scripts\build.ps1
```

期望：`BUILD SUCCESSFUL`，产物 `build/libs/scpmod-0.1.0.jar`。
首次运行需要迷雾通代理（`<代理端口>`）下载 Minecraft/Fabric 依赖。

## 1. 无头自检（推荐每次改完都跑，约 2~5 分钟）

```powershell
powershell -ExecutionPolicy Bypass -File scripts\selftest.ps1
```

它启动开发用专用服务器（带 `-Dscpmod.selftest=true`），等日志出现下列标记后自动判 PASS/FAIL：

| 检查项 | 日志标记 | 含义 |
|---|---|---|
| structure generated in world | `scanned=... stone_bricks=N>0` | 世界生成路径真的产出了方块/方块实体 |
| event reached BREACHED | `event phase=BREACHED` | 事件状态机 WARNING→BREACHED |
| anomaly body spawned | `anomaly body spawned` | 突破时异常实体生成 |
| event re-contained | `event phase=CONTAINED` | 终端再收容闭环 |
| anomaly body removed | `anomaly body removed` | 再收容时实体移除 |
| neck_snap damage type loaded | `neckSnapDamageTypeLoaded=true` | 自定义伤害类型（死亡消息）已注册 |

日志位置：`run/logs/latest.log`、`selftest-console.log`。

> 注意：无头自检**无法验证**需要真实玩家的部分（注视冻结、瞬移贴脸、死亡消息文本、音效、Boss 条 UI）——那些在第 3 层。

## 2. 专用服务器 + 控制台（半自动）

```powershell
powershell -ExecutionPolicy Bypass -File scripts\run-server.ps1
```

在服务器控制台可执行（不需要玩家）：

| 命令 | 期望 |
|---|---|
| `/scp status` | 输出 `phase=... ticks=... scps=1 events=on` |
| `/scp stats` | 输出统计（全新世界为全 0） |
| `/scp scps` | `registered: scpmod:scp_173` |
| `/scp reload` | 重载 `config/scpmod.json` |

需要玩家的命令（`/scp event start`、`/scp station build`、`/scp unit set`）在控制台会报 `player only`，请在客户端里用。

## 3. 客户端人工验收清单（最完整）

```powershell
# 首次会下载约 200MB 客户端 assets，走迷雾通代理
$env:JAVA_TOOL_OPTIONS='-DsocksProxyHost=127.0.0.1 -DsocksProxyPort=9909 -Djava.net.preferIPv4Stack=true'
& '<Gradle 路径>\bin\gradle.bat' --no-daemon --console=plain runClient
```

进入世界（建议开创造 + 允许作弊），逐条验收：

| # | 操作 | 期望 |
|---|---|---|
| 1 | `/give @s scpmod:containment_unit`、`/give @s scpmod:terminal` | 拿到两个方块，可放置 |
| 2 | 右键收容单元 | 聊天栏提示「收容单元是空的」 |
| 3 | 右键终端 | 「终端已激活」/「已关闭」切换 |
| 4 | `/scp station build` | 脚下生成 17×8×17 站点（地板/墙/隔断/灯/2 收容单元/2 终端/铁栏牢房） |
| 5 | 右键站点里的收容单元 | 显示收容中：SCP-173 |
| 6 | 打掉一个**有内容**的收容单元 | Boss 条出现预警倒计时；到期后提示「SCP-173 已脱逃」 |
| 7 | 正对 SCP-173 保持盯视 | 它**完全不动** |
| 8 | 视线移开（转头/后退到墙后） | 它瞬间贴脸（12 格/tick），移动时有**石头脚步声** |
| 9 | 被贴近 | 死亡界面显示「<你的名字> 被 SCP-173 折断了颈部」 |
| 10 | `/scp event start` + 用已激活终端交互 | 事件转为 CONTAINED，Boss 条变绿 |
| 11 | `/scp stats` | `triggered/recontained` 数字增长，且重启后不丢（`config/scpmod_stats.json`） |
| 12 | 关闭服务端/客户端再进 | 站点结构仍在；已生成的区块不会新增结构（正常） |

## 3b. 新增行为参数（本轮 L3 反馈）

| 参数（config/scpmod-anomalies.json） | 默认 | 作用 |
|---|---|---|
| observationFovDegrees | **100** | 判定"在屏幕上"的视场角（不再是以准星命中为准） |
| closeRangeObserved | 3.0 | 该距离内只要在身前就算被观察（修复贴脸误判） |
| ignoreCreativePlayers | true | 创造模式玩家不作为目标、不追 |
| respawnProtectionSeconds | 5 | 重生后 5 秒内不作为目标（防刷人头） |
| maxStep / killRange / neckSnapDamage / attackCooldownTicks | 12 / 2.5 / 1000 / 20 | 位移速度、击杀距离、伤害、攻击冷却 |

| 参数（config/scpmod-events.json） | 默认 | 作用 |
|---|---|---|
| breachWarningSeconds | **30**（已从 60 调低） | 预警秒数 |
| containedDisplaySeconds | 5 | 再收容提示停留时长 |

## 4. 常见问题

| 现象 | 原因 / 处理 |
|---|---|
| `Could not resolve net.fabricmc...` / 下载卡住 | 迷雾通没开或端口变了：确认 9909/9910 在监听再去跑构建 |
| `requires at least Gradle 9.8.0` | 别用系统 Gradle 9.5.1，用脚本里指定的 `<Gradle 路径>` |
| 构建/启动特别慢、`Can't keep up!` | 内存压力：关掉 VS Code Java 扩展等；至少留 2~3GB 空闲内存 |
| `Address already in use: 25565` | 有残留服务端：`taskkill /F /IM java.exe` 后重跑 |
| 服务端提示 eula | 脚本已自动写 `run/eula.txt`；手动跑时检查该文件 |
| 死亡消息显示成 `death.attack.scpmod.neck_snap` | 语言文件未加载（确认构建包含 assets、客户端用对应语言） |

## 5. 计划中的自动化（尚未实现）

- `GameTest`：拆解收容单元→事件启动、终端再收容、站点生成断言；目标命令 `gradle runGametest`。
- 结构生成种子快照测试（固定 seed + 期望方块计数）。