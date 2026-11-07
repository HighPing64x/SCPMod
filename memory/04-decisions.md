# 决策记录（04-decisions）

## [2026-10-31 13:04:47] 固化 D01~D22 决策
- 状态：全部为已确认决策（D21 除外，标注为推定）
- 文件：memory/04-decisions.md
- 内容：决策清单 + 理由 + 备选 + 用户选择依据
- 原因/命令：用户要求关键决策落盘
- 下一步：确认后执行 M1

---

## 已确认决策
| 编号 | 决策 | 用户选择 | 关键理由 |
|---|---|---|---|
| D01 | 题材与载体 | SCP 题材 **Minecraft Java 模组**（Fabric） | 工作区名 SCPMod + 用户唯一成熟工具链在 Fabric |
| D02 | 剧情 | **无主要剧情线** | 用户定位"先把东西做好" |
| D03 | 澄清深度 | **C 档**（范围+玩法+技术路线/里程碑，数值方向级） | 避免开工返工，又不做假精确 |
| D04 | 人力与素材 | **单人**、无现成美术/音效、接受占位 | 用户现状 |
| D05 | 目标版本 | **MC 26.2**（锁定，v1 不跟进 26.3/26.4） | 用户要求"社区支持丰富"；26.2 生态 13,569 且本机 Loom 已解析 |
| D06 | 玩法主轴 | **图鉴式异常内容包 + 收容框架** | 用户选择（后由 D09 修正目标层） |
| D07 | 分发 | **自己+朋友 LAN/小服，暂不公开** | 规避 SCP 授权（CC BY-SA 3.0）与素材合规风险 |
| D08 | 收容承载 | **模组自带可生成收容设施结构** | 用户选择；否决"专用方块搭建" |
| D09 | 玩家目标 | **沙盒**：玩家自己决定做什么，设施只是场景 | 用户选择；否决收容作业/探索搜刮/生存逃离 |
| D10 | 结构形态 | **Jigsaw 拼装式大型站点**（多房间、随机布局） | 用户选择；否决单房间小模板 |
| D11 | 设施内装置 | **固定少量专用方块：收容单元 + 操作终端** | 交互需要可感物理对象；≠建筑套件 |
| D12 | 收容单元可搬移性 | **可破坏、可重新放置**；拆掉 = 泄压，SCP 逃出 | 保住沙盒自由度；涌现玩法 |
| D13 | 事件触发 | **混合**：玩家行为为主 + 低概率环境诱因 | 兼顾可控（可复现调试）与不确定性 |
| D14 | 事件阶段 | **多阶段 + 结算统计**（预警→突破→扩散→重新收容→结算） | 用户选择；对齐原版袭击的准备期结构 |
| D15 | 突破后归宿 | **世界级追猎 + 可在任意处用收容单元重新收容** | 袭击式世界后果；失败有代价、成功有闭环 |
| D16 | SCP 内容来源 | **官方编号**，机制各异，首批 3~5 个（**清单延后**） | 玩家先验认知 + 设定现成 |
| D17 | 首批 SCP 清单 | **暂缓，先做框架** | 用户决定框架优先 |
| D18 | 美术路线 | **综合使用（原版组合 + Blockbench 自制），要到较好效果**；但**排期在框架之后** | 用户要求质量，同时要求先保稳定性 |
| D19 | 联机范围 | **单机 + LAN + 专用服务器全兼容** | 内容模组本就服务端权威，补测成本低 |
| D20 | 配置与调试 | **JSON/TOML 配置文件 + /scp 调试命令** | 零第三方依赖、调试效率高 |
| D21 | 框架七层 | **结构/方块/事件/呈现/注册/数据/联机 全部纳入 v1** | 用户多选全选 |
| D22 | 参考 SCP | **SCP-173** 端到端验证事件链 | 成本最低的可动实体，能跑完整链 |
| D23 | SCP 扩展机制 | **代码级注册**（基类/接口/钩子/注册表） | 类型安全、行为自由、易维护 |
| D24 | 质量投入 | **关键机制 GameTest + 设计/维护文档** | 用户要"稳定性与可维护性" |
| D25 | 里程碑 | **M1 地基 → M2 事件链 → M3 打磨** | 每刀都有可打开游戏验证的产物 |
| D26 | 时间盒 | **每里程碑设上限，到点砍范围保可玩** | 单人项目最大风险是无限打磨 |

## 推定项（AI 认为已隐含成立，但**用户未明确拍板**）
| 编号 | 推定内容 | 依据 | 待确认方式 |
|---|---|---|---|
| A01 | 从零新建 Fabric 工程（不复制 Apophis 骨架） | 用户问"要不要从 Fabric 网站搞 template"，AI 答"我自己拉"；Apophis 是 Meteor 客户端 addon 架构不匹配 | 用户回复确认 M1 方案即可视为通过 |
| A02 | 仅依赖 Fabric API，不引入第三方配置库 | 与 D20 一致 | 同上 |
| A03 | 里程碑验收细则（见 02-plan 第 3 节） | AI 起草，未逐条确认 | 同上 |
| A04 | 风险缓解措施（房间池上限、单链验证等） | AI 提议 | 同上 |

## 被用户否决的 AI 推荐（保留记录，避免重复提议）
| 项 | AI 推荐 | 用户选择 |
|---|---|---|
| 收容承载 | 专用方块搭建 | **可生成的设施结构** |
| 结构形态 | 单站点小模板 | **Jigsaw 大型站点** |
| 玩家目标 | 收容作业 / 探索搜刮 | **沙盒自由** |
| 版本 | 26.3 / 1.21.1 / 26.1.2 | **26.2** |
| 触发条件 | 纯玩家行为触发 | **混合触发** |
| 事件阶段 | 三阶段（无结算统计） | **多阶段 + 结算统计** |


## [2026-10-31 21:24:17] 用户确认「继续完成」后的补充决策
- 状态：生效（AI 默认值，用户可随时推翻）
- 文件：memory/04-decisions.md
- 内容：A01~A04 视为经用户确认；新增 A05~A09 默认值
- 原因/命令：用户回复「好的，请继续完成」，未逐项拍板
- 下一步：按默认值推进；若用户否决，改这里并回滚对应实现

| 编号 | 内容 | 依据 | 可推翻性 |
|---|---|---|---|
| A05 | modid = `scpmod`；group/包名 = `apep.highping.scpmod`（沿用 Apophis 的 highping 命名习惯） | O6 无人拍板，取最直观值 | 是（改包名代价中等，越早越好） |
| A06 | 文本语言：`zh_cn` + `en_us` 双语 key | O5 推荐值 | 是 |
| A07 | 非目标：采纳 01-requirements 第 3 节六项全部排除 | O1 推荐值 | 是 |
| A08 | 时间盒：每里程碑上限 **2 周**（M1/M2/M3 各 2 周，超出即砍范围） | O8 未给数值，取可执行默认 | 是（用户给数值即替换） |
| A09 | 存档策略：新世界完整体验；老世界靠探索新区块 + /scp locate | O7 推荐值 | 是 |
| A10 | M1 首个可运行验证目标 = 编译通过 + /scp 命令可用 + 收容单元可放置/破坏 | AI 依据 M1 验收细化 | 是 |

## [2026-10-31 21:24:17] 技术决策（实现级）
| 编号 | 决策 | 理由 |
|---|---|---|
| T01 | 工程基线 = fabric-example-mod 的 **26.2 分支**（MC 26.2 / loader 0.20.1 / loom 1.19-SNAPSHOT / fabric-api 0.161.0+26.2 / Java 25 / splitEnvironmentSourceSets） | 唯一与用户锁定版本匹配的官方模板；已实测可拉取 |
| T02 | Gradle 使用本机已解压的 **9.5.1**（<Gradle 缓存>\wrapper\dists\gradle-9.5.1-bin\bvvv.../gradle-9.5.1），GRADLE_USER_HOME=<Gradle 缓存>，JAVA_HOME=<JDK 25 路径> | 无需联网下载 gradle 发行版；JDK 25 与 MC 26.2 要求一致 |
| T03 | gradle wrapper 直接复用 Apophis 的 gradlew/gradle-wrapper.jar，properties 指向腾讯镜像 gradle-9.5.1 | 用户已有可用 wrapper，避免重新生成 |
| T04 | 26.x 为「不混淆」版本，类名与 1.21 时代不同（例：net.minecraft.resources.Identifier），因此**所有 API 名称必须以实际 jar 为准**，不得凭 1.21 记忆编写 | 模板 ExampleMod.java 已用 net.minecraft.resources.Identifier 证实 |


## [2026-10-31 21:41:04] M1 实现级补充决策
| 编号 | 决策 | 理由 |
|---|---|---|
| T05 | Gradle 使用 **9.8.2**（手动从腾讯镜像下载解压到 <Gradle 路径>） | Loom 1.19.3 强制要求 Gradle ≥ 9.8.0；本机 9.5.1 不满足 |
| T06 | 构建 JVM 加 `-Djava.net.preferIPv4Stack=true` + 放宽 HTTP 超时/重试 | maven.fabricmc.net 在 IPv6 下极慢/超时；IPv4 可用（约 23KB/s） |
| T07 | M1 事件链先落「状态机骨架」：IDLE→WARNING→BREACHED→CONTAINED，SCP 实体生成留 M2 | 先让状态流转、提示、命令可验收；实体追猎属 M2 |
| T08 | 创造模式物品栏接入（Fabric ItemGroupEvents）推迟到编译通过后 | 先保证最小编译面，减少首轮错误 |
| T09 | 收容单元的「泄压」在 M1 用 `playerWillDestroy` + BreachEventManager 记录 + 全场提示实现 | 26.2 中 BlockBehaviour 无 onRemove/playerWillDestroy，钩子在 Block 上 |


## [2026-10-31 21:48:54] 实现级补充决策（T10~T12）
| 编号 | 决策 | 理由 |
|---|---|---|
| T10 | 建立**离线编译验证**通道：javac + MC 26.2 merged jar + 本地 .minecraft/libraries + 自写 Fabric API 桩 | maven.fabricmc.net 在此网络下基本不可用；离线验证能覆盖全部 MC API 误用风险（最大风险源） |
| T11 | Fabric API 桩的真实签名以 GitHub raw 源码核对（CommandRegistrationCallback / ServerTickEvents / FabricLoader），不凭记忆 | 桩如果与真实签名不符会造成假通过 |
| T12 | 创造模式物品栏、/scp locate、Jigsaw 结构层推迟到 Gradle 可构建之后 | 这些要么依赖 Fabric API 新模块（ItemGroupEvents），要么依赖二进制资源（.nbt），无法离线验证 |

## [2026-11-01 10:44:42] T13~T15 环境与 26.2 结论
| 编号 | 决策/结论 | 理由 |
|---|---|---|
| T13 | 构建统一走迷雾通 SOCKS5 <代理端口>（JAVA_TOOL_OPTIONS 注入），固化到 scripts/build.ps1 / run-server.ps1 | 直连 maven.fabricmc.net 不可用；代理实测 125~167KB/s |
| T14 | MC 26.x 中 BlockBehaviour.Properties / Item.Properties 必须在构造前 setId(...) | runServer 实测 NPE「Block id not set」，vanilla 在其 register helper 内做同样处理 |
| T15 | 自建 register() 辅助封装 setId + Registry.register | 避免逐方块样板，与 vanilla 模式一致 |

## [2026-11-01 11:12:18] T16~T18 结构层（基底）实现决策
| 编号 | 决策 | 理由 |
|---|---|---|
| T16 | 基底先用「代码注册 Structure + StructurePiece 程序化生成」实现站点；**Jigsaw/template-pool 留到内容阶段** | Jigsaw 需要二进制 .nbt 房间模板，会阻塞基底验证；程序化生成能立刻打通 worldgen JSON → StructureType → Piece → 方块/方块实体 全链路，且房间词汇表可复用到后续 Jigsaw 模板 |
| T17 | 站点蓝图做成与 world 无关的纯数据（StationBlueprint.Op/Unit/Terminal），由结构 Piece 与 /scp station build 命令共用 | 同一份布局既能世界生成也能调试命令放置，避免两套代码；调试自检可脱离世界生成验证 |
| T18 | 加 -Dscpmod.selftest=true 的服务端自检：直接放置蓝图 + 查找最近站点并扫描结构包围盒 | 无客户端/无交互条件下也能端到端验证结构生成 |

## [2026-11-01 12:35:25] T19~T21 事件链实现决策
| 编号 | 决策 | 理由 |
|---|---|---|
| T19 | M2 先把事件链做成「异常抽象」的完整闭环（状态机/呈现/统计/再收容），SCP-173 实体下一个增量 | 事件链是最大的单块工作量，先验证它不依赖实体；实体接入时只挂 BREACHED 阶段 |
| T20 | 再收容手段 = 已激活的操作终端（BREACHED 时交互即 recontain） | 让 D11 的终端方块有实际用途，并形成「准备→失效→反制」闭环；后续可加距离/耗材约束 |
| T21 | 统计写 config/scpmod_stats.json（Gson），与 scpmod.json 同目录 | 零依赖、可跨世界累计；后续可迁到 SavedData 做每档统计 |

## [2026-11-01 18:56:38] T22 命名空间独立化（用户纠正）
- 用户指出：本项目与 Apophis 完全无关，要求独立命名。
- 决策：Java 包 / Gradle group 从 `apep.highping.scpmod` 改为 **`com.scpmod`**；modid 保持 `scpmod`；fabric.mod.json 的 authors 从 highping 改为中性的 `scpmod`（用户可随时改成自己的名字）。
- 影响：21 个 Java 文件包名与 import 全部重写；源码目录 src/{main,client}/java/com/scpmod/**；fabric.mod.json entrypoints 改为 com.scpmod.ScpMod / com.scpmod.client.ScpModClient；gradle.properties group=com.scpmod。
- 验证：重命名后 BUILD SUCCESSFUL（jar 54KB），服务端 selftest 全部通过（结构层 + 事件链）。

## [2026-11-01 19:02:19] T23~T24 异常实体实现决策
| 编号 | 决策 | 理由 |
|---|---|---|
| T23 | 异常身体先用 vanilla **ArmorStand** 占位（头/手持石方块），行为由 AnomalyController 服务器端驱动 | 26.2 自定义实体渲染器（EntityRenderState 体系）成本高且无法在本轮无客户端验证；ArmorStand 即插即见，行为验证不受影响；后续换自定义模型只需替换身体，不动控制器 |
| T24 | 注视判定 = 距离 ≤32 + `player.getViewVector` 与「眼→身体」单位向量点积 > 0.90（暂不做遮挡 LOS） | 26.2 未见公开 hasLineOfSight，先用视野锥近似；遮挡判定列为后续完善项 |

## [2026-11-01 19:12:08] T25 按 SCP-173 原文修正行为（用户纠正：2.5 格太慢）
- 依据（已抓原文 https://scp-wiki-cn.wikidot.com/scp-173）：
  - 「SCP-173会移动且具有高度敌意。该物体在直接视线内无法移动。与SCP-173之间的视线时刻都不得中断。派往收容间的人员要在眨眼前相互提醒。」
  - 「该物体的攻击方式为折断头骨底处的颈部，或是勒颈。」
  - 「当收容间内无人存在时，其中会传出刮石的声音。」
- 旧实现：2.5 格/秒 + 仅视野锥判定 → 与原文不符（太慢、可隔墙冻结）。
- 新实现（AnomalyController）：
  - 冻结条件：任一玩家在 40 格内、身体在视野锥内（点积>0.90）且 **射线无遮挡**（level.clip COLLIDER）——即「直接视线内无法移动」；
  - 未观察：每 tick 最多移动 **12 格**（≈240 格/秒，眨眼即贴脸）；落地前做简单可站立校验；
  - 接触（≤2.5 格）：`hurtServer(magic, 1000)` = 断颈秒杀，并播放暴击音效；
  - 移动时播放 STONE_BREAK（刮石声）。
- 备选/可调：MAX_STEP 与 KILL_RANGE 目前是常量，后续可提到配置；若觉得过于致命可加「被注视时玩家有 1 tick 惩罚窗口」等平衡项。

## [2026-11-01 19:32:21] T26~T27 死亡消息与刮石声（用户要求）
- T26：SCP-173 击杀改用**自定义伤害类型** `scpmod:neck_snap`（data/scpmod/damage_type/neck_snap.json，message_id=scpmod.neck_snap），死亡消息：
  - zh：`%1$s 被 %2$s 折断了颈部`（%2$s = 异常身体名 SCP-173）→「玩家名 被 SCP-173 折断了颈部」
  - en：`%1$s had their neck snapped by %2$s`
  - DamageSource 把攻击者指向异常身体（ArmorStand，自定义名 SCP-173），因此消息里出现 SCP-173 而不是无名实体。
- T27：刮石声按用户提议改为「石头上的脚步声」= `SoundEvents.STONE_STEP`（移动时每 3 tick 播放一次，volume 0.9 / pitch 0.5，听起来像缓慢刮擦的石步）。替代原先的 STONE_BREAK。
- 验证：selftest 日志 `neckSnapDamageTypeLoaded=true`（动态注册表已加载该伤害类型）；死亡消息文本需客户端实际死亡才能看到。

## [2026-11-01 19:33:02] T28 项目纳入本地 Git（用户要求，便于回退）
| 项 | 值 |
|---|---|
| 仓库 | E:\SCPMod（`git init -b main`） |
| 初始提交 | 79447d3 "Initial commit: SCP containment framework (MC 26.2 Fabric)" |
| 基线标签 | `baseline-20261101`（附注标签） |
| 提交者身份 | 仓库级 user.name=scpmod / user.email=scpmod@localhost（可按需改） |
| 纳入版本控制 | src/、gradle 配置与 wrapper、scripts/、.gitignore、.gitattributes、README.md、memory/（含 checkpoints） |
| 忽略 | build/、.gradle/、run/（世界/日志/统计）、*.log、.compilecheck/、.nettest/、*.tmp |
| 行尾策略 | .gitattributes: `* text=auto`，jar/png/nbt 标记 binary，gradlew=LF、gradlew.bat=CRLF |

### 回退速查（已写入 06-commands）
- 看历史：`git log --oneline --decorate --graph`
- 暂存区回退：`git restore <file>`；取消暂存 `git restore --staged <file>`
- 回到某次提交（不改历史）：`git checkout <hash>` / `git switch -c <branch> <hash>`
- 丢弃本地未提交改动：`git restore .` / `git clean -fd`（慎用）
- 整体回到基线：`git switch -c fix-xxx baseline-20261101` 或 `git reset --hard baseline-20261101`（会丢工作区改动）
- 反向提交：`git revert <hash>`（保留历史，适合已提交的坏改动）

## [2026-11-01 20:23:38] T29 测试分层策略（用户问「如何进行测试」）
| 层 | 方式 | 覆盖 | 耗时 |
|---|---|---|---|
| L0 构建 | `scripts/build.ps1` | 编译 + 打包 | 秒~分钟 |
| L1 无头自检 | `scripts/selftest.ps1` | 结构生成、事件链、异常生命周期、伤害类型注册 | 2~6 分钟 |
| L2 专用服务器控制台 | `scripts/run-server.ps1` | `/scp status/stats/scps/reload` | 手动 |
| L3 客户端人工清单 | `runClient` + TESTING.md 12 条清单 | 方块交互、Boss 条 UI、173 手感、死亡消息、音效、统计持久化 | 手动 |
| L4（计划） | GameTest | 自动化断言 | 待实现 |
- T29 决定：凡是能在服务端无头验证的，都写进 L1 自检（作为每次改动的验收闸门）；需要玩家的留 L3 清单。

## [2026-11-01 22:01:43] T30 按 L3 实测反馈重构 173 观察/目标规则 + 配置拆分
**观察判定**：从「准星点积 > 0.90」改为「**是否在屏幕内**」——用可配置视场角（`observationFovDegrees`，默认 70°）计算半角余弦阈值，再叠加遮挡射线；新增 `closeRangeObserved`（默认 3 格）近距离在身前一律算被观察，修复「贴脸时被误判为没看着」。
**目标规则**：
- 创造模式玩家默认不作为目标、不追（`ignoreCreativePlayers=true`）；旁观者也不作为目标。
- 重生保护：重生后 `respawnProtectionSeconds`（默认 5s）内不作为目标，防刷人头；实现为每玩家记录 spawnAtTick（首次出现/死亡后重新出现/超 40 tick 未见过都视为新生成）。
- 仍然跳过死亡目标，攻击冷却 `attackCooldownTicks`（默认 20）。
**配置拆分为多个文件**（用户要求）：
- `config/scpmod.json`：框架开关（`breachEventsEnabled`、`environmentalTriggerChance`、`debugLogging`）
- `config/scpmod-events.json`：`breachWarningSeconds`（**默认改为 30**）、`containedDisplaySeconds`、`eventsEnabled`
- `config/scpmod-anomalies.json`：FOV、距离、速度、伤害、创造模式/重生保护、攻击冷却
- `/scp reload` 会一起重载三个文件。

## [2026-11-01 22:15:24] T31 创造模式物品栏与 173 生成方式（用户要求）
- 新增 3 个独立创造物品栏（Fabric Creative Tab API，26.2 包名已改为 `net.fabricmc.fabric.api.creativetab.v1`）：
  - `scpmod:entities` → 「SCPMod：实体」：放置 SCP-173 生成器
  - `scpmod:blocks` → 「SCPMod：方块&杂项」：收容单元、操作终端
  - `scpmod:equipment` → 「SCPMod：装备」：预留空栏（图标铁胸甲），等装备内容
- 新增物品 `scpmod:scp_173_spawner`（Scp173SpawnerItem）：右键方块 → 在该位置生成 173 原型实体（调试/创造用）
- 新增命令 `/scp anomaly clear`：清除当前异常实体（手动生成后用于收尾；事件 stop 也会清理）
- 173 的三种生成途径：① 收容失效事件（破坏有内容收容单元 / `/scp event start`）② 创造栏生成器右键 ③ 调试命令

## [2026-11-01 22:30:49] T32 异常接近/离开的氛围小标题（用户要求）
- 进入检测范围（默认 32 格，可配）→ 副标题：「你感觉到背后凉飕飕的，似乎有一个水泥雕塑就在周围……」
- 离开范围（range + hysteresis，默认 32+4=36 格）→ 副标题：「你感觉到周围不再那么压抑了……」
- 实现：ServerPlayer.connection 发送 ClientboundSetTitlesAnimationPacket(10,60,20) + ClientboundSetSubtitleTextPacket + ClientboundSetTitleTextPacket(Component.empty())（空主标题 = 只有小标题）
- 状态存 NEAR_STATE(HashMap<UUID,Boolean>)，仅在跨越边界时触发一次；异常实体生成/移除时清空，避免误报
- 配置（scpmod-anomalies.json）：ambientWarningRange=32、ambientWarningHysteresis=4、ambientMessagesEnabled=true
- 氛围提示对所有玩家（含创造模式）生效——它只是氛围，不是伤害逻辑

## [2026-11-01 22:44:08] T33 用户新规则（重要）
- **从此刻起由用户自己提交 git**：AI 只做「构建 + L1 自检」，改完把 `git status`/建议的 commit message 交给用户，不执行 `git commit`/`git add`。
- 视场角默认从 70° 扩大到 **100°**（用户反馈 70° 像误判）；运行目录 config 已同步为 100。
- **禁止隔墙击杀**：攻击前检查异常眼睛到玩家眼睛的射线（`level.clip`），被方块挡住则不造成伤害。
- **禁止穿墙移动**：移除原来「直接 teleport」的做法，改为 `body.move(MoverType.SELF, ...)` 碰撞移动 + 0.5 格子步进（保留高速感但不会穿墙）。
- **靠近玩家时进行寻路**：新增局部 BFS（半径 12，可行走格 = 脚下实心 + 2 格空气），直行被挡时沿 waypoint 绕行；路径每 10 tick 重算。

## [2026-11-01 23:08:54] T34 贴图用途与素材策略
- 用户提供的 SCP-173 标牌图用作 **scpmod:scp_173_spawner 物品的材质**（不是新增标签物品）；如需做成“墙上的挂牌”，后续再单独做 label 方块/物品。
- 成套标牌策略：优先**本地程序化生成**（tools/gen_scp_label.py），避免第三方素材授权问题；参考素材可用 SCP:CB 开源仓库（CC BY-SA 3.0，需署名并同协议），SCP:SL 素材非开源不建议使用。
- 实体模型：走 Blockbench「Java Entity」导出模型类 + 贴图，再接 ModelLayer/EntityRenderer（26.2 RenderState 体系）；具体代码实现下一步做。

## [2026-11-01 23:14:31] T35 标牌方案与 Blockbench 选项更正
- Blockbench 新建菜单里**没有 Java Entity**；Java 模组实体对应的是 **「模组版实体」（Modded Entity）**，已更正 docs/BLOCKBENCH_ENTITY.md（不要选 Bedrock 版实体 / Java 版方块物品）。
- 用户认为本地程序化生成的标牌「有点丑」，且成套模板其实有很多种风格；决定外包给 Opus55 做更好的模板设计+代码。
- 已生成提示词文档 docs/PROMPT_给Opus55_标牌模板.md：要求 4 种风格（工业警示/基金会公文/终端监控/金属铭牌）、Pillow 批量生成、中文字体、质感与排版红线、验收清单。
- 后续拿到 Opus55 产出后，由我负责本地跑通、批量生成、接入物品/墙牌贴图。

## [2026-11-01 23:21:38] T36 Blockbench 导出版本：Forge 1.17+ (Mojmaps)
- 用户截图确认下拉选项含 Forge 各版本与 Fabric 1.17+ (Yarn)；**选 `Forge 1.17+ (Mojmaps)`**。
- 原因：MC 26.x 为不混淆版本，类名是官方名（Mojmaps 风格），与 Yarn 映射不兼容；Yarn 导出会产生 TexturedModelData/ModelPartBuilder 等对不上的名字。
- 已同步更新 docs/BLOCKBENCH_ENTITY.md。

## [2026-11-06 23:14:31] T37 材质与转向规则
- 实体贴图以用户手绘 64×64 为基础，程序化叠加水泥质感：暗色权重高、灰色权重低（`weight = (1-lum)^1.3 * 0.55 + 0.04`），保留原色和 alpha。
- 转向：被观察时保持绝对静止（含不转头）；未观察移动时与击杀瞬间面向玩家。

## [2026-11-06 23:20:37] T38 渲染参数与跨越
- `submitModel` 8 参重载的第 7 个 int = outlineColor：必须传 `state.outlineColor`（0=无轮廓），传 -1 会导致实体发光（穿墙可见白色轮廓）。
- 173 可跨 1 格高方块：`tryStepUp` 仅垂直抬升，不横向移动，保证不穿墙。

## [2026-11-06 23:26:30] T39 重生保护计时规则
- 重生保护必须从「重生时刻」开始算，而不是死亡时刻；用 `PlayerState.alive` 状态翻转检测重新计时（死亡 tick 与重生 tick 都重置 spawnAtTick）。

## [2026-11-06 23:33:12] T40 重生保护实现方式
- 必须用事件（Fabric `ServerPlayerEvents.AFTER_RESPAWN`）在重生瞬间重置保护计时，不能只靠「上一 tick 是否存活」的状态推断（重生时机不稳定，推断会漏）。
- 贴图水泥质感叠加时按饱和度降权，避免红色喷漆被灰化。

## [2026-11-07 12:11:37] T41 刮石声触发与字幕
- 收容单元环境音方案放弃（用户决定不做收容单元部分）；改为「173 未被任何玩家注视时」触发：小幅度移动 + 自定义声音事件 `scpmod:scrape`。
- 字幕用 sounds.json 的 subtitle 字段实现（右下角字幕显示「SCP173：刮石」），音源复用原版石头脚步声文件，不需要自制 ogg。
- 不再向玩家发送「已向代理 HMCL 监督员报告」类消息。

## [2026-11-07 12:36:15] T42 刮石声与移动策略
- 刮石声使用原版 `block.stone.step`（复刻设定里“在石头上走动/刮擦”的听感），频率 5~10 秒一次；不再使用自制合成音效与自定义声音事件。
- 173 不做无人时的主动踱步（避免被玩家看到移动）；只有追击目标玩家时才移动，被观察时完全静止。
