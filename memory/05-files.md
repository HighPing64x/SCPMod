# 文件变更（05-files）

## [2026-10-31 13:04:47] 仅创建 memory 目录，未创建任何实现文件
- 状态：实现文件 0；memory 文件 10
- 文件：见下表
- 内容：记录当前文件清单与未来工程结构规划（规划非代码）
- 原因/命令：用户下达 memory 规则；实现尚未被授权
- 下一步：用户确认后建工程（M1）

---

## 已创建文件（全部属于 memory 系统）
| 文件 | 用途 |
|---|---|
| memory/00-index.md | 索引/状态/恢复入口 |
| memory/01-requirements.md | 需求与非目标、待定项 |
| memory/02-plan.md | 里程碑与计划 |
| memory/03-progress.md | 进度流水 |
| memory/04-decisions.md | 决策记录 |
| memory/05-files.md | 本文件 |
| memory/06-commands.md | 命令与事实来源 |
| memory/07-errors.md | 错误与修复 |
| memory/08-next.md | 下一步 |
| memory/10-checkpoints/20261031-130447.md | 完整快照 |

## 实现文件
**无。** 工作区 E:\SCPMod 在 memory 建立前完全为空；按用户要求「本阶段不写代码」，未创建 gradle 工程、未改任何工程文件。

## 未来工程结构（规划，便于 M1 对齐；不是已创建文件）
- 根：build.gradle / settings.gradle / gradle.properties / gradlew / src/main / src/client
- 代码包名与 modid：**待定（O6）**，一旦确定即写入 memory
- 资源：assets/<modid>/（textures/models/lang）、data/<modid>/（structures/worldgen/tags）
- 站点模板：data/<modid>/structure/ 与 worldgen/template_pool/


## [2026-10-31 21:24:17] M1 工程骨架（新建）
- 状态：已创建，待编译验证
- 文件：见下表
- 内容：Fabric 26.2 工程骨架 + 最小入口类 + 语言文件
- 原因/命令：用户授权进入实现（「请继续完成」）
- 下一步：编译通过后补 结构层/方块层/注册层/数据层

| 文件 | 说明 |
|---|---|
| build.gradle | loom 1.19-SNAPSHOT；splitEnvironmentSourceSets；依赖 fabric-loader 0.20.1 + fabric-api 0.161.0+26.2；Java release 25 |
| settings.gradle | rootProject.name=scpmod；Fabric maven + mavenCentral + gradlePluginPortal |
| gradle.properties | minecraft_version=26.2 / loader_version=0.20.1 / loom_version=1.19-SNAPSHOT / fabric_api_version=0.161.0+26.2 / version=0.1.0 / group=apep.highping.scpmod |
| gradle/wrapper/gradle-wrapper.properties | 指向腾讯镜像 gradle-9.5.1-bin.zip |
| gradlew / gradlew.bat / gradle-wrapper.jar | 复用自 <JDK 路径>\apophis-26.1.2（未修改） |
| src/main/resources/fabric.mod.json | id=scpmod；entrypoint main=apep.highping.scpmod.ScpMod、client=...client.ScpModClient；depends minecraft ~26.2 / java >=25 / fabric-api |
| src/main/java/apep/highping/scpmod/ScpMod.java | ModInitializer 入口；MOD_ID、LOGGER、id() 工具方法 |
| src/client/java/apep/highping/scpmod/client/ScpModClient.java | ClientModInitializer 空实现 |
| src/main/resources/assets/scpmod/lang/en_us.json, zh_cn.json | 语言文件（占位） |


## [2026-10-31 21:41:04] M1 代码文件（新建）
- 状态：已写入，待编译验证
- 文件：下表
- 内容：注册层 / 方块层 / 配置 / 命令 / 事件骨架 / 资源
- 原因/命令：M1 实现
- 下一步：编译通过后补结构层与创造物品栏

| 文件 | 说明 |
|---|---|
| src/main/java/apep/highping/scpmod/ScpMod.java | 入口；初始化顺序 config→blocks→BEs→items→registry→command→tick→event |
| .../content/ModBlocks.java | containment_unit / terminal 方块注册（BuiltInRegistries.BLOCK） |
| .../content/ModBlockEntities.java | 26.2 无 Builder，直接 new BlockEntityType<>(supplier, Set.of(block)) |
| .../content/ModItems.java | BlockItem 注册 |
| .../block/ContainmentUnitBlock.java | BaseEntityBlock；useWithoutItem 查看状态；playerWillDestroy 触发泄压 |
| .../block/TerminalBlock.java | 交互切换 armed 状态 |
| .../block/entity/ContainmentUnitBlockEntity.java | 存 containedScp/sealed，ValueInput/ValueOutput 读写 |
| .../block/entity/TerminalBlockEntity.java | armed 状态持久化 |
| .../scp/ScpDefinition.java | record(id, nameKey, threat) |
| .../scp/ScpRegistry.java | 代码级注册表 + nameKey/displayName |
| .../config/ScpModConfig.java | Gson JSON 配置（config/scpmod.json），字段 breachEventsEnabled/breachWarningSeconds/environmentalTriggerChance/debugLogging |
| .../command/ScpCommand.java | /scp status|reload|look|event start|event stop|scps，权限 LEVEL_GAMEMASTERS |
| .../event/BreachEventManager.java | 状态机骨架 IDLE/WARNING/BREACHED/CONTAINED + 广播 |
| .../event/ServerTickHandler.java | ServerTickEvents.END_SERVER_TICK → tick |
| .../client/ScpModClient.java | 空客户端入口（已有） |
| src/main/resources/assets/scpmod/** | blockstates / models / item models / en_us / zh_cn（先用原版材质占位：iron_block、observer_front） |
| src/main/resources/fabric.mod.json | id=scpmod，entrypoints main/client，depends ~26.2 / java>=25 / fabric-api |
| gradle.properties / build.gradle / settings.gradle / gradle wrapper | 构建配置（见 06-commands T05/T06） |


## [2026-10-31 21:48:54] 新增：离线编译验证目录与命令扩展
- 状态：.compilecheck 为本地验证工具目录（不进入构建产物）
- 文件：见下
- 内容：离线编译脚本 + Fabric 桩；ScpCommand 增加 unit set/empty
- 原因/命令：T10
- 下一步：联网后用 Gradle 复验

| 路径 | 说明 |
|---|---|
| .compilecheck/javac.args | javac 参数文件（classpath=MC 26.2 merged jar + 855 个本地库 jar；源码=main+client+stubs） |
| .compilecheck/stubs/net/fabricmc/** | Fabric API 桩（ModInitializer/ClientModInitializer/FabricLoader/Event/CommandRegistrationCallback/ServerTickEvents） |
| .compilecheck/classes | 编译产物（23 个 class） |
| src/main/java/.../command/ScpCommand.java | 新增 /scp unit set <id>、/scp unit empty（对视线所指收容单元操作） |

## [2026-11-01 10:41:51] 便捷脚本与忽略规则
- 状态：已创建
- 文件：scripts/build.ps1、scripts/run-server.ps1、.gitignore
- 内容：一键构建 / 一键启动 dev 服务器（固定 JDK25、Gradle9.8.2、Geph SOCKS 代理）；忽略 build/run/.gradle/*.log/.compilecheck
- 原因/命令：减少每次手工设置环境变量的出错面
- 下一步：M2 开发即用这两个脚本

## [2026-11-01 10:44:42] M1 产出与脚本
| 路径 | 说明 |
|---|---|
| build/libs/scpmod-0.1.0.jar | 可分发的模组 jar（31KB，含 main/client 类、assets、lang） |
| scripts/build.ps1 | 一键构建（固定 JDK25 / Gradle9.8.2 / Geph SOCKS） |
| scripts/run-server.ps1 | 一键启动 dev 专用服务器 |
| .gitignore | 忽略 build/、run/、.gradle/、*.log、.compilecheck/ |
| run/ | dev 运行目录（eula.txt、config/scpmod.json、world、logs），已忽略 |

## [2026-11-01 11:12:18] 结构层新增文件
| 路径 | 说明 |
|---|---|
| src/main/java/.../structure/StationBlueprint.java | 纯布局数据：WIDTH=17/HEIGHT=8/DEPTH=17 站点（地板/墙/隔断/天花板/日光灯/2 收容单元/2 终端/铁栏牢房） |
| src/main/java/.../structure/ScpStationPiece.java | 结构片段：按蓝图 placeBlock + 填充方块实体；已 setOrientation(NORTH) |
| src/main/java/.../structure/ScpStationStructure.java | Structure 子类：simpleCodec + findGenerationPoint |
| src/main/java/.../structure/StructureDiagnostics.java | -Dscpmod.selftest=true 自检（蓝图直建 + 最近站点扫描） |
| src/main/java/.../content/ModStructures.java | 注册 STRUCTURE_TYPE / STRUCTURE_PIECE |
| src/main/resources/data/scpmod/worldgen/structure/scp_station.json | biomes(8 个平原/森林系) / step surface_structures / terrain_adaptation beard_thin |
| src/main/resources/data/scpmod/worldgen/structure_set/scp_stations.json | random_spread spacing=40 separation=12 salt=20261031 |
| src/main/resources/data/scpmod/tags/worldgen/structure/scp_stations.json | 结构 tag，供 locate/自检使用 |
| ScpCommand.java | 新增 /scp station build（在玩家脚下生成站点） |

## [2026-11-01 12:35:25] 事件链文件（修改/新增）
| 路径 | 说明 |
|---|---|
| src/main/java/.../event/BreachEventManager.java | 重写：Phase 状态机 + ServerBossEvent + 预警倒计时 + 广播 + recontain/stop 统计 |
| src/main/java/.../event/ScpModStats.java | 新增：eventsTriggered/Recontained/EndedByForce/totalBreachedSeconds，Gson 持久化 |
| src/main/java/.../block/TerminalBlock.java | BREACHED 且已激活时交互 → tryRecontain |
| src/main/java/.../command/ScpCommand.java | /scp event start 接新 API；新增 /scp stats |
| src/main/java/.../structure/StructureDiagnostics.java | 增加事件自检（3s 预警→突破→再收容→统计） |
| assets/scpmod/lang/{en_us,zh_cn}.json | 新增 bossbar/recontain/stillbreached 等文本 |

## [2026-11-01 18:56:38] 包名迁移
- 旧：src/main/java/apep/highping/scpmod/**、src/client/java/apep/highping/scpmod/client/**（已删除）
- 新：src/main/java/com/scpmod/**（entrypoint ScpMod、content/block/entity/event/structure/scp/config/command/scp 子包）、src/client/java/com/scpmod/client/ScpModClient.java
- 配置：gradle.properties group=com.scpmod；fabric.mod.json entrypoints 指向 com.scpmod.*，authors=[scpmod]

## [2026-11-01 19:02:19] 异常实体文件
| 路径 | 说明 |
|---|---|
| src/main/java/com/scpmod/event/AnomalyController.java | 生成/移除异常身体；每 tick 找最近玩家；被注视冻结，未注视每 20 tick 逼近 2.5 格，<3 格且冷却结束则 hurtServer 8 点 |
| src/main/java/com/scpmod/event/ServerTickHandler.java | tick 里追加 AnomalyController.tick() |
| src/main/java/com/scpmod/event/BreachEventManager.java | BREACHED 时 spawn；recontain/stop/reset 时 despawn |
| src/main/java/com/scpmod/structure/StructureDiagnostics.java | 自检日志带 anomalyBodyActive |
| lang | 新增 entity.scpmod.scp_173 |

## [2026-11-01 19:32:21] 死亡消息相关文件
| 路径 | 说明 |
|---|---|
| src/main/resources/data/scpmod/damage_type/neck_snap.json | 自定义伤害类型：message_id=scpmod.neck_snap、exhaustion=0、scaling=when_caused_by_living_non_player |
| src/main/java/com/scpmod/ScpMod.java | 新增 ResourceKey<DamageType> NECK_SNAP |
| src/main/java/com/scpmod/event/AnomalyController.java | neckSnapSource(level) 用异常身体作为攻击者；移动音效改为 STONE_STEP（每 3 tick） |
| assets/scpmod/lang/{en_us,zh_cn}.json | 新增 death.attack.scpmod.neck_snap(.player) |

## [2026-11-01 19:33:02] 版本控制与文档
| 路径 | 说明 |
|---|---|
| .git/（main 分支） | 本地仓库；初始提交 79447d3，标签 baseline-20261101 |
| .gitattributes | 行尾策略（text=auto；jar/png/nbt=binary；gradlew LF） |
| .gitignore | 忽略 build/、.gradle/、run/、*.log、.compilecheck/、.nettest/、*.tmp |
| README.md | 项目说明、进度清单、环境要求、构建/运行/自检命令、游戏内命令、配置说明 |

## [2026-11-01 20:23:38] 测试文件
| 路径 | 说明 |
|---|---|
| TESTING.md | 分层测试指南（L0 构建 / L1 无头自检 / L2 服务器控制台 / L3 客户端 12 条清单 / L4 GameTest 计划 / 常见问题） |
| scripts/selftest.ps1 | 无头自检脚本（Start-Job 启动 runServer，轮询 run/logs/latest.log，7 项断言，PASS/FAIL） |

## [2026-11-01 22:01:43] 配置与行为文件
| 路径 | 说明 |
|---|---|
| src/main/java/com/scpmod/config/EventConfig.java | config/scpmod-events.json：breachWarningSeconds=30、containedDisplaySeconds、eventsEnabled |
| src/main/java/com/scpmod/config/AnomalyConfig.java | config/scpmod-anomalies.json：observationFovDegrees=70、closeRangeObserved=3、maxStep=12、killRange=2.5、neckSnapDamage=1000、ignoreCreativePlayers、respawnProtectionSeconds=5、attackCooldownTicks=20 |
| src/main/java/com/scpmod/event/AnomalyController.java | 重写：FOV 屏幕判定 + 近距离豁免 + 创造/旁观/死亡/重生保护过滤 + 状态表 PlayerState |
| README.md / TESTING.md | 配置说明与新增参数表 |

## [2026-11-01 22:15:24] 创造栏相关文件
| 路径 | 说明 |
|---|---|
| src/main/java/com/scpmod/content/ModCreativeTabs.java | 注册 3 个创造栏（entities/blocks/equipment） |
| src/main/java/com/scpmod/item/Scp173SpawnerItem.java | 右键生成 173 原型的调试物品 |
| src/main/resources/assets/scpmod/models/item/scp_173_spawner.json | 生成器物品模型（铁锭贴图占位） |
| ScpMod.java | onInitialize 里调用 ModCreativeTabs.register() |
| ScpCommand.java | 新增 /scp anomaly clear |

## [2026-11-01 22:30:49] 氛围提示相关文件
| 路径 | 说明 |
|---|---|
| src/main/java/com/scpmod/event/AnomalyController.java | 新增 updateAmbientMessages / sendSubtitle（title+subtitle 包），NEAR_STATE 记录跨越状态 |
| src/main/java/com/scpmod/config/AnomalyConfig.java | 新增 ambientWarningRange=32 / ambientWarningHysteresis=4 / ambientMessagesEnabled=true |
| assets/scpmod/lang/{zh_cn,en_us}.json | message.scpmod.ambient.near / .far |

## [2026-11-01 22:44:08] Jigsaw 与素材文档
| 路径 | 说明 |
|---|---|
| src/main/resources/data/scpmod/structure/station/*.nbt | 4 个房间模板（start_room/room_a/room_b/corridor），含 jigsaw 连接块与方块实体 NBT |
| src/main/resources/data/scpmod/worldgen/template_pool/station/{start,rooms}.json | Jigsaw 模板池 |
| src/main/resources/data/scpmod/worldgen/structure/scp_jigsaw_station.json | minecraft:jigsaw 结构（size 4，surface_structures，beard_thin） |
| data/scpmod/worldgen/structure_set/scp_stations.json | 改为指向 jigsaw 结构 |
| docs/ASSETS.md | 材质/模型添加完整指南（方块/物品/Blockbench/实体/常见坑） |
| src/main/java/com/scpmod/event/AnomalyController.java | 碰撞移动 + 局部 BFS 寻路 + 攻击视线门禁 |

## [2026-11-01 23:08:54] 贴图与素材工具
| 路径 | 说明 |
|---|---|
| src/main/resources/assets/scpmod/textures/item/scp_173_spawner.png | SCP-173 生成器物品贴图（用户提供的标牌图，64×64） |
| src/main/resources/assets/scpmod/models/item/scp_173_spawner.json | layer0 指向 scpmod:item/scp_173_spawner |
| tools/gen_scp_label.py | 批量生成 SCP 收容室标牌（Pillow，512×512，风格统一） |
| tools/labels.example.json | 标牌批量生成配置样例（编号/等级/说明/照片） |
| docs/labels/*.png | 生成的样例标牌（SCP-173/096/895） |
| docs/BLOCKBENCH_ENTITY.md | Blockbench 实体模型制作与接入指南 |

## [2026-11-01 23:14:31] 文档新增
| 路径 | 说明 |
|---|---|
| docs/PROMPT_给Opus55_标牌模板.md | 给 Opus55 的完整提示词（4 种风格、批量生成、验收标准），可直接复制
| docs/BLOCKBENCH_ENTITY.md | 已更正：实体建模选「模组版实体（Modded Entity）」

## [2026-11-06 22:48:03] 实体模型相关文件
| 路径 | 说明 |
|---|---|
| src/client/java/com/scpmod/client/model/Scp173Model.java | 修正版 173 模型类（LayerLocation=scpmod:scp173#main，createBodyLayer 64×64，setupAnim 空） |
| src/client/java/com/scpmod/client/render/Scp173RenderState.java | 渲染状态（LivingEntityRenderState 子类 + bodyRotation） |
| src/main/resources/assets/scpmod/textures/entity/scp173.png | 64×64 水泥占位贴图（程序化生成，待用户替换） |

## [2026-11-06 22:53:12] 173 实体/渲染相关文件
| 路径 | 说明 |
|---|---|
| src/main/java/com/scpmod/entity/Scp173Entity.java | 自定义实体（Monster 子类，无 AI） |
| src/main/java/com/scpmod/content/ModEntities.java | EntityType + 属性注册 |
| src/client/java/com/scpmod/client/render/Scp173Renderer.java | 26.2 submit 体系渲染器 |
| src/client/java/com/scpmod/client/model/Scp173Model.java | 修正版模型类 |
| src/client/java/com/scpmod/client/ScpModClient.java | 注册模型层与渲染器 |
| src/main/resources/assets/scpmod/textures/entity/scp173.png | 64×64 水泥占位贴图 |

## [2026-11-06 23:38:20] 26.2 物品模型定义（必须）
| 路径 | 说明 |
|---|---|
| src/main/resources/assets/scpmod/items/scp_173_spawner.json | 生成器物品模型定义 → scpmod:item/scp_173_spawner |
| src/main/resources/assets/scpmod/items/containment_unit.json | 收容单元物品模型定义 → scpmod:block/containment_unit |
| src/main/resources/assets/scpmod/items/terminal.json | 终端物品模型定义 → scpmod:block/terminal |

## [2026-11-06 23:58:06] 环境音相关文件
| 路径 | 说明 |
|---|---|
| src/main/java/com/scpmod/event/ContainmentAmbience.java | 收容间刮石声 + 靠近停止时报告监督员 |
| src/main/java/com/scpmod/block/entity/ContainmentUnitBlockEntity.java | 增加已加载集合与环境音状态字段 |
| .gitignore | 补充 IDE/OS/崩溃日志忽略 |

## [2026-11-07 12:11:37] 刮石声相关文件
| 路径 | 说明 |
|---|---|
| src/main/java/com/scpmod/content/ModSounds.java | 注册 `scpmod:scrape` 声音事件 |
| src/main/resources/assets/scpmod/sounds.json | scrape 定义（字幕 + 复用原版石头脚步音源） |
| src/main/java/com/scpmod/event/AnomalyController.java | 未观察时 nudge 漂移 + SCRAPE 播放 |

## [2026-11-07 12:16:49] 刮石音频与踱步
| 路径 | 说明 |
|---|---|
| assets/scpmod/sounds/scrape1.ogg ~ scrape3.ogg | 自制刮石音效（ffmpeg 转 ogg） |
| assets/scpmod/sounds.json | scrape 事件：字幕 + 3 段音源 |
| event/AnomalyController.java | isObservedByAnyone 9 采样点 + 旁观者排除；wander 村民踱步 |

## [2026-11-07 20:49:15] SCP-500 文件
| 路径 | 说明 |
|---|---|
| src/main/java/com/scpmod/item/Scp500Item.java | 药丸物品与治愈逻辑（含抗药性减弱） |
| src/main/java/com/scpmod/effect/Scp500ResistanceEffect.java | 抗药性状态效果 |
| src/main/java/com/scpmod/content/ModEffects.java | 效果注册 |
| src/main/resources/assets/scpmod/items/scp_500.json | 26.2 物品模型定义 |
| src/main/resources/assets/scpmod/models/item/scp_500.json | 物品模型 |
| src/main/resources/assets/scpmod/textures/item/scp_500.png | 16×16 红色药片图标 |
| docs/scp500_icon_preview.png | 图标预览（8×放大） |
