# SCPMod

> 一个 SCP 题材的 Minecraft **Fabric** 模组（目标 **Minecraft 26.2** / **Java 25**）

**非官方同人作品**。没有主线剧情，沙盒向：玩家探索随机生成的收容站点，用收容单元和操作终端处置异常；
收容失效会触发事件链（预警 → 突破 → 异常脱逃 → 重新收容 → 结算统计）。

---

## 目录

- [特性](#特性)
- [已实现内容](#已实现内容)
- [玩法](#玩法)
- [安装](#安装)
- [开发与构建](#开发与构建)
- [命令](#命令)
- [配置](#配置)
- [内容 ID 速查](#内容-id-速查)
- [模组兼容](#模组兼容)
- [项目结构](#项目结构)
- [文档与工具](#文档与工具)
- [已知限制 / 路线图](#已知限制--路线图)
- [许可与致谢](#许可与致谢)

---

## 特性

- **异常实体**：SCP-173 使用真实体 + Blockbench 模型 + 自定义渲染器，行为完全由服务端驱动。
- **收容框架**：收容单元 / 操作终端方块、事件链、结算统计、配置热重载。
- **Jigsaw 站点**：模板池 + `.nbt` 房间模板，多房间随机拼接的收容站点。
- **状态效果系统**：SCP-500 抗药性（I~V 级）与可扩展的模组兼容接口。
- **氛围表现**：Boss 条预警、氛围小标题、石头脚步声（字幕）、自定义死亡消息。
- **26.2 渲染管线**：自定义 `EntityRenderer` + `EntityRenderState`（submit / SubmitNodeCollector）。

## 已实现内容

### 异常

| 编号 | 等级 | 行为 |
|---|---|---|
| **SCP-173** | Euclid | 被任何玩家直接注视时**完全静止**（视场角判定 + 遮挡检测 + 包围盒多点采样）；无人注视时高速逼近（碰撞移动 + 局部寻路，可跨 1 格台阶、不会穿墙）；接触后折断玩家颈部（自定义伤害类型与死亡消息）。未观察时会以稀疏间隔传来石头脚步声（字幕「SCP173：刮石」）。支持重生保护、创造/旁观免疫、退出重进世界后自动接管旧实体。 |
| **SCP-500** | Safe | 塑料小罐里的红色药片（单粒物品，堆叠 59）。服用后清除全部负面效果、回满生命值、灭火/解冻；对已死亡无效。连续服用会产生 **I~V 级抗药性**：I 回血减半；II 随机清除 2 个效果；III 随机清除 1 个；IV 只能获得缓慢生命恢复；V 完全无效。状态栏显示抗药性等级与药片图标。 |

### 方块

| 方块 | 说明 |
|---|---|
| `scpmod:containment_unit` 收容单元 | 存放异常；被破坏/泄压时触发收容失效 |
| `scpmod:terminal` 操作终端 | 再收容、状态显示 |

### 物品

| 物品 | 说明 |
|---|---|
| `scpmod:scp_173_spawner` SCP-173 生成器 | 刷怪蛋风格图标，右键生成 173（调试/创造用） |
| `scpmod:scp_500` SCP-500 万能药 | 红色药片图标，右键服用 |

### 结构与事件

- 收容站点：`scpmod:scp_jigsaw_station`，`template_pool` + `.nbt`（起始厅 / 收容室 / 牢房 / 走廊）随机拼接。
- 收容失效：预警（默认 30 秒，Boss 条）→ 突破 → 异常脱逃 → 再收容 → 统计写入 `config/scpmod_stats.json`。
- `/scp station build`：在脚下现场生成一个调试站点。

### 反馈与氛围

- 进入/离开异常范围时的小标题（默认 32 格，可配置）。
- 自定义伤害类型 `scpmod:neck_snap` 与死亡消息「XX 被 SCP-173 折断了颈部」。
- 石头脚步声（SCP-173 未观察时）、轻度石头敲击声（击杀时）。

## 玩法

1. 在**创造模式物品栏**的三个 SCPMod 页签里取用内容（实体 / 方块&杂项 / 装备）。
2. 用 `/scp station build` 或自然生成找到收容站点，里面的收容单元存放着异常。
3. 破坏收容单元（或 `/scp event start`）触发收容失效：先预警，随后异常脱逃。
4. 利用**注视**让 SCP-173 静止，或用终端/收容单元把它重新收容。
5. 被 173 击杀后会有 5 秒重生保护；创造/旁观不会被追击。
6. 需要治疗时服用 **SCP-500**（注意连续服用会累积抗药性）。

## 安装

> 目前处于开发阶段，尚未发布正式版本。

1. 安装 **Fabric Loader 0.20.1+**（Minecraft **26.2**）。
2. 将 [Fabric API](https://modrinth.com/mod/fabric-api)（`0.161.0+26.2` 或更新）放入 `mods/`。
3. 将构建产物 `build/libs/scpmod-0.1.0.jar` 放入 `mods/`。

## 开发与构建

构建脚本通过环境变量定位工具链（不再写死本机路径）：

```powershell
$env:JAVA_HOME        = '<JDK 25 路径>'
$env:GRADLE_USER_HOME = '<Gradle 缓存路径>'
$env:SCPMOD_GRADLE    = '<Gradle 9.8.2 安装目录>'
```

首次下载依赖如果需要代理，自行设置 `JAVA_TOOL_OPTIONS`（例如 SOCKS5 / HTTP 代理）。

```powershell
# 构建
powershell -ExecutionPolicy Bypass -File scripts\build.ps1

# 启动开发服务器
powershell -ExecutionPolicy Bypass -File scripts\run-server.ps1

# 无头自检（结构生成 + 事件链 + 异常生命周期），期望 7/7 PASS
powershell -ExecutionPolicy Bypass -File scripts\selftest.ps1
```

技术栈：Fabric Loader 0.20.1 / Fabric API 0.161.0+26.2 / Loom 1.19.3 / Gradle 9.8.2 / JDK 25。

## 命令

| 命令 | 说明 |
|---|---|
| `/scp status` | 事件阶段 / SCP 数量 / 配置开关 |
| `/scp stats` | 结算统计（触发 / 再收容 / 强制终止 / 脱逃秒数） |
| `/scp event start` / `stop` | 手动触发 / 终止收容失效事件 |
| `/scp station build` | 在玩家脚下生成一个收容站点（调试） |
| `/scp unit set <scp>` / `empty` | 设置视线所指收容单元的内容（调试） |
| `/scp give scp_500 [数量]` | 给予 SCP-500 药丸（1~59） |
| `/scp anomaly clear` | 清除当前异常实体 |
| `/scp look` | 查看视线所指方块坐标 |
| `/scp scps` | 列出已注册的 SCP |
| `/scp reload` | 热重载配置文件 |

> 需要管理员权限（权限等级 2）。

## 配置

配置文件位于 `config/`，修改后 `/scp reload` 即可生效（统计文件无需重载）。

### `scpmod.json`（框架开关）

| 字段 | 默认 | 说明 |
|---|---|---|
| `breachEventsEnabled` | `true` | 是否允许收容失效事件 |
| `environmentalTriggerChance` | `0.02` | 环境触发概率 |
| `debugLogging` | `false` | 调试日志 |

### `scpmod-events.json`（事件时序）

| 字段 | 默认 | 说明 |
|---|---|---|
| `breachWarningSeconds` | `30` | 预警秒数 |
| `containedDisplaySeconds` | `5` | 再收容提示停留时长 |
| `eventsEnabled` | `true` | 事件总开关 |

### `scpmod-anomalies.json`（异常行为）

| 字段 | 默认 | 说明 |
|---|---|---|
| `observationFovDegrees` | `100` | 判定“在屏幕上”的视场角 |
| `viewDistance` | `40` | 观察/追踪距离 |
| `closeRangeObserved` | `3` | 该距离内视为被观察 |
| `maxStep` | `12` | 每 tick 最大移动距离 |
| `killRange` | `2.5` | 攻击距离 |
| `neckSnapDamage` | `1000` | 断颈伤害 |
| `ignoreCreativePlayers` | `true` | 是否忽略创造模式玩家 |
| `respawnProtectionSeconds` | `5` | 重生保护秒数 |
| `attackCooldownTicks` | `20` | 攻击冷却 |
| `ambientWarningRange` | `32` | 氛围提示范围（格） |
| `ambientWarningHysteresis` | `4` | 氛围提示迟滞 |
| `ambientMessagesEnabled` | `true` | 是否显示氛围小标题 |

### `scpmod_stats.json`

结算统计（跨世界累计）：触发 / 再收容 / 强制终止 / 脱逃秒数。

## 内容 ID 速查

| 类型 | ID |
|---|---|
| 方块 | `scpmod:containment_unit`、`scpmod:terminal` |
| 物品 | `scpmod:scp_173_spawner`、`scpmod:scp_500` |
| 实体 | `scpmod:scp_173` |
| 状态效果 | `scpmod:scp500_resistance` |
| 结构 | `scpmod:scp_jigsaw_station` |
| 伤害类型 | `scpmod:neck_snap` |

## 模组兼容

SCP-500 的清除逻辑提供三层兼容手段：

1. **原版分类**：自动清除 `HARMFUL` 分类的效果，覆盖绝大多数模组的负面效果。
2. **数据包标签** `scpmod:curable_effects`：

   ```json
   {
     "replace": false,
     "values": ["othermod:radiation", "othermod:infection"]
   }
   ```

3. **公开事件** `com.scpmod.api.Scp500Events.CURE`：

   ```java
   Scp500Events.CURE.register((player, resistant) -> MyInfectionData.clear(player));
   ```

适用于用自定义数据（而不是 MobEffect）表示“疾病/感染”的模组。

## 项目结构

```
src/main/java/com/scpmod/
├─ ScpMod.java              入口：配置、注册、事件
├─ api/                     公开 API（Scp500Events）
├─ block/                   收容单元、终端
├─ command/                 /scp 命令
├─ config/                  三个配置文件
├─ content/                 注册表（方块/物品/实体/声音/效果/创造栏）
├─ effect/                  自定义状态效果
├─ entity/                  Scp173Entity
├─ event/                   事件链、异常控制器、统计
├─ item/                    生成器、SCP-500
├─ scp/                     SCP 注册表
└─ structure/               Jigsaw 站点与调试蓝图
src/client/java/com/scpmod/client/
├─ model/Scp173Model.java   26.2 实体模型
└─ render/Scp173Renderer    26.2 渲染器（submit / RenderState）
src/main/resources/
├─ assets/scpmod/           贴图、模型、语言、声音
└─ data/scpmod/             结构、模板池、伤害类型、标签
```

## 文档与工具

| 路径 | 说明 |
|---|---|
| `docs/ASSETS.md` | 材质/模型添加指南（方块、物品、Blockbench） |
| `docs/BLOCKBENCH_ENTITY.md` | 用 Blockbench 制作实体模型并接入 26.2 渲染管线 |
| `docs/labels/` | 批量生成的 SCP 收容室标牌样例 |
| `tools/gen_scp_label.py` | 按 JSON 配置批量生成统一风格的 SCP 标牌 |
| `tools/labels.example.json` | 标牌生成配置示例 |
| `memory/` | 决策、进度、错误记录（从 `memory/00-index.md` 开始读） |

## 已知限制 / 路线图

**已知限制**

- 音效字幕需要在 `选项 → 音乐和声音 → 显示字幕` 中开启。
- SCP-500 目前只能通过创造栏 / `/scp give` 获取（站点生成掉落未接入）。
- 26.2 的实体渲染 API 较新，升级 Minecraft 版本时需要重新核对渲染管线。
- 暂无 GameTest 自动化测试；当前使用无头自检 + 实机验证。

**路线图**

- [ ] SCP-500 的站点生成/战利品掉落
- [ ] 更多 SCP（如 096 / 895 等）
- [ ] GameTest 自动化测试
- [ ] 收容室挂牌方块（使用 `tools/gen_scp_label.py` 的贴图）
- [ ] 更完善的自定义实体 AI 与寻路

## 许可与致谢

- 本项目为**非官方粉丝作品**，与 SCP 基金会无隶属关系。
- SCP 系列设定、名称与相关文本来自 [SCP 基金会](https://scp-wiki-cn.wikidot.com/)，遵循 **CC BY-SA 3.0** 协议。
- 本项目代码与自制美术资源版权归项目作者；使用或分发 SCP 相关设定时请遵守 CC BY-SA 3.0。
- 感谢 Fabric / Fabric API、Blockbench、以及 SCP 社区。
