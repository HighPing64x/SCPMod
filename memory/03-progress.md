# 进度（03-progress）

## [2026-10-31 13:04:47] 阶段一需求澄清完成 + memory 建立
- 状态：需求理解完成一轮，等待用户确认
- 文件：memory/03-progress.md
- 内容：R1~R10 澄清流水与结论
- 原因/命令：用户要求每轮更新 progress
- 下一步：见 08-next.md

---

## 阶段一：需求澄清（R1 ~ R10）

### R1 定位
- 结论：SCP 题材 **Minecraft Fabric 模组**；「高版本」；**无主要剧情线**；"先把东西做好"；具体版本未定。
- 同时确定：澄清深度 = **C 档**（范围 + 玩法设计 + 技术路线/里程碑，数值只到方向级）；**单人开发**、无现成美术/音效素材、接受占位。

### R2 版本与主轴
- 版本：不追最新，**要求社区支持丰富**（AI 负责查事实）。
- 玩法主轴：**图鉴式异常内容包 + 收容框架**。
- 分发：**自己 + 朋友小范围（LAN/小服），暂不公开发布**。

### R3 版本锁定与内容来源
- 版本：**MC 26.2**（AI 给出的生态数据支撑：Fabric mod 数 13,569，且本机 Loom 已解析过）。
- SCP 来源：**官方编号**，首批 3~5 个、机制各异。
- 收容承载：用户选「**模组自带可生成的收容设施结构**」（否决了「专用方块搭建」选项）。

### R4 结构形态、玩家目标、设施交互
- 结构：**Jigsaw 拼装式大型站点**（多房间、可随机布局）。
- 玩家目标：**沙盒**（自己决定做什么，设施只是场景）。
- 设施内：**固定少量专用方块（收容单元 + 操作终端）**。

### R5 驱动力 / 可搬移性 / 工程起点
- 用户反问："**可以像原版袭击那样条件触发收容失效吗**" → 方向确认为条件触发式事件。
- 收容单元：**可破坏、可重新放置**（拆掉 = 泄压）。
- 用户问是否需要他准备 Fabric 模板 → AI 答复：不需要，官方模板 26.2 分支已核实可直接拉。

### R6 事件链
- 触发：**混合**（玩家行为为主 + 低概率环境诱因）。
- 阶段：**多阶段 + 结算统计**（预警 → 突破 → 扩散 → 重新收容成功/失败）。
- 突破后：**世界级追猎 + 可在世界任意处用收容单元重新收容**。

### R7 内容清单 / 美术 / 联机
- SCP 清单：**暂不定，等 AI 给候选表**（候选表已提交，见下方附录）。
- 美术：**综合使用，要求做到较好效果**。
- 联机：**单机 + LAN + 专用服务器全兼容**。

### R8 清单延后 / 框架优先 / 配置
- SCP 清单：**之后再说，先做框架**。
- 美术：**先做模组框架，保证稳定性与可维护性**。
- 配置：**JSON/TOML 配置文件 + /scp 调试命令**。

### R9 框架边界与验证
- 框架七层**全部纳入 v1**：结构层、方块层、事件层、呈现层、SCP 注册层、数据层、联机层。
- 验证：**用 SCP-173 端到端验收框架**。
- 扩展：**代码级注册**。
- 质量：**GameTest + 设计/维护文档**。

### R10 节奏与收口
- 里程碑：**M1 地基 → M2 事件链 → M3 打磨**。
- 非目标：**待会再说**（未定，见 01-requirements O1）。
- 版本：**v1 锁死 26.2，不跟进新版本**。
- 时间盒：**每里程碑设上限，到点砍范围保可玩**。

### 用户离场提示（重要）
- 用户明确：即将离开，**不要使用 ask 等阻断性工具**，避免浪费时间。→ 后续若需用户决定，用普通文本列出问题，等其回来回答。

## 附录：R8 提交的 SCP 候选表（成本为 AI 工程判断）
| SCP | 机制 | 承载 | 成本 |
|---|---|---|---|
| 173 | 被注视时不可动，视线移开瞬移+扭颈 | 实体 | 中 |
| 096 | 被看到脸即暴怒追猎 | 实体 | 中 |
| 895 | 观看画面者受精神/致死侵害 | 方块设备 | 中 |
| 500 | 万能药 | 物品 | 低 |
| 268 | 遗忘帽（难以被注意） | 物品 | 低 |
| 939 | 模仿人声诱捕 | 实体 | 中高 |
| 049 | 触碰致死并转化 | 实体 | 中高 |
| 079 | 设施 AI 接管设备 | 设备系统 | 高 |
| 106 | 腐蚀固体、口袋维度 | 实体+维度 | 高 |
| 914 | 万能转换机 | 方块系统 | 高 |
| 131 | 无害围观小生物 | 实体 | 低 |
| 682 | 难杀适应 | 实体 | 中 |


## [2026-10-31 21:24:17] 用户确认并授权继续；进入 M1 实现
- 状态：实现阶段 M1 进行中（工程骨架已建，等待 Gradle 首次编译）
- 文件：见 05-files.md「M1 骨架」
- 内容：用户回复「好的，请继续完成」。据此视为：①确认 02-plan 的 M1~M3 与 A01~A04 推定项；②授权采用 AI 推荐默认值推进 O1/O3/O5/O6（详见 04-decisions 的 A05~A09，全部可被用户一句话推翻）。
- 原因/命令：用户指令「请继续完成」
- 下一步：跑通 Gradle 编译 → 按 M1 顺序实现 结构层/方块层/注册层/数据层


## [2026-10-31 21:41:04] M1 代码骨架完成，首次编译进行中
- 状态：M1 实现中（工程骨架 + 注册层 + 方块层 + 配置/命令/事件骨架已写；编译验证在跑）
- 文件：见 05-files.md「M1 代码」
- 内容：设计并落地 12 个 Java 类与资源文件；建立了 Gradle 构建环境（9.8.2 + JDK25 + IPv4）
- 原因/命令：用户授权「请继续完成」；按 02-plan M1 顺序推进
- 下一步：等编译结果 → 修 API 错误 → 补结构层（Jigsaw）与创造模式物品栏


## [2026-10-31 21:48:54] M1 代码完成 + 离线编译验证通过；联网构建被网络阻塞
- 状态：M1 代码层面完成（23 个 class 离线编译通过）；Gradle 依赖解析被 maven.fabricmc.net 带宽阻塞，未能产出 jar
- 文件：src/main/java 下 15 个类 + src/client 1 个 + 9 个资源 JSON
- 内容：离线用 JDK25 javac 以「MC 26.2 merged jar + 本地 MC 库 + Fabric API 桩」编译全部源码，**0 错误**
- 原因/命令：见 06-commands「离线编译验证」
- 下一步：网络恢复后跑 Gradle 出 jar → 补创造物品栏/结构层/GameTest

## [2026-11-01 10:41:51] 重启电脑后环境复核：构建仍通过
- 状态：环境可用（Geph 9909/9910 已恢复；JDK25 与 Gradle 9.8.2 路径不变）；增量构建 14s，jar 未变（up-to-date）
- 文件：build/libs/scpmod-0.1.0.jar（31KB）
- 内容：重启后复核构建链路 + 重新跑 dev 专用服务器验证
- 原因/命令：用户重启电脑后要求继续
- 下一步：M2（结构层 Jigsaw + SCP-173 + 事件实体化）

## [2026-11-01 10:44:42] M1 达成（构建 + 专用服务器运行验证）
- 状态：完成。构建成功，jar 产出，dev 专用服务器加载 mod 无异常。
- 文件：build/libs/scpmod-0.1.0.jar（31KB）
- 内容：M1 地基全部验收项通过
- 原因/命令：走迷雾通 Geph 代理完成首次依赖下载（12m29s），增量构建 14~27s
- 下一步：M2（Jigsaw 结构层 + SCP-173 + 事件链实体化）

## [2026-11-01 11:12:18] 结构层（基底）打通并验证
- 状态：**结构层可用**。站点可在世界生成，扫描确认 stone_bricks=615 / containment_units=2 / terminals=2
- 文件：structure/StationBlueprint.java、ScpStationPiece.java、ScpStationStructure.java、content/ModStructures.java、structure/StructureDiagnostics.java；data/scpmod/worldgen/{structure,structure_set}/…、data/scpmod/tags/worldgen/structure/scp_stations.json
- 内容：worldgen 数据驱动注册 + 代码生成站点 + /scp station build 调试命令 + 自检通道
- 原因/命令：用户指示「先做基底」
- 下一步：Jigsaw 模板化（内容阶段）、GameTest、SCP-173

## [2026-11-01 12:35:25] M2 核心：收容失效事件链闭环（无实体）验证通过
- 状态：事件状态机 + 呈现层 + 结算统计全部落地并由服务端自检验证
- 文件：event/BreachEventManager.java（重写）、event/ScpModStats.java（新增）、block/TerminalBlock.java、command/ScpCommand.java、structure/StructureDiagnostics.java
- 内容：IDLE→WARNING(boss bar/倒计时)→BREACHED(红条/广播)→CONTAINED(绿条/统计)→IDLE；终端=再收容手段；统计持久化到 config/scpmod_stats.json
- 原因/命令：用户「继续直到做完」
- 下一步：SCP-173 实体 + Jigsaw 模板化 + GameTest

## [2026-11-01 18:56:38] 包名独立化完成并验证
- 状态：命名空间已与 Apophis 解耦（com.scpmod），构建与运行验证通过
- 文件：src/main/java/com/scpmod/**、src/client/java/com/scpmod/client/**、fabric.mod.json、gradle.properties
- 内容：批量重写包名/import/entrypoint；重命名后 selftest：结构 615 石砖/2 单元/2 终端 + 事件链 WARNING→BREACHED→CONTAINED 全通过
- 原因/命令：用户要求「包名改一下，独立出来」
- 下一步：SCP-173 实体 → GameTest → Jigsaw 模板化

## [2026-11-01 19:02:19] 异常实体接入事件链（SCP-173 原型，服务器端行为完成）
- 状态：BREACHED 时生成异常实体、再收容/终止时移除；行为（注视冻结/未注视逼近/近身伤害）已实现
- 文件：event/AnomalyController.java（新增）、ServerTickHandler、BreachEventManager、StructureDiagnostics、lang
- 内容：异常身体用 ArmorStand 占位（无需自定义渲染器即可在所有客户端可见）；视线判定=32 格内 + 视线向量点积 > 0.90
- 原因/命令：用户「继续」
- 下一步：GameTest、Jigsaw 模板化、客户端实机验证（行为需真实玩家才能观察）

## [2026-11-01 19:12:08] 173 行为按原文修正并通过服务端自检
- 状态：视线判定改为「视野锥+遮挡射线」，未观察时 12 格/tick 秒贴脸，接触断颈 1000 伤害，移动有刮石声
- 文件：src/main/java/com/scpmod/event/AnomalyController.java（重写）
- 内容：生命周期自检仍全通过（BREACHED spawn / CONTAINED despawn）；行为细节需客户端真实玩家验证
- 原因/命令：用户指出 2.5 格/秒 不符合 https://scp-wiki-cn.wikidot.com/scp-173 原文
- 下一步：客户端实机验证 173 手感；GameTest；Jigsaw 模板化

## [2026-11-01 19:32:21] 173 死亡消息 + 石脚步声完成
- 状态：自定义伤害类型已加载（selftest 确认），死亡消息与石脚步声已接入；构建/服务端自检全通过
- 文件：ScpMod.java（NECK_SNAP key）、event/AnomalyController.java、data/scpmod/damage_type/neck_snap.json、lang/*.json
- 内容：击杀 → 自定义 death.attack.scpmod.neck_snap；移动 → STONE_STEP
- 原因/命令：用户要求加死亡消息并确认刮石声用石头走路声表现
- 下一步：客户端实机验证死亡消息/音效/手感；GameTest；Jigsaw 模板化

## [2026-11-01 19:33:02] 本地 Git 建立
- 状态：仓库已初始化，初始提交 + 基线标签完成，工作区干净
- 文件：.git/、.gitattributes、.gitignore、README.md
- 内容：65→66 文件纳入版本控制（含 memory 知识库），构建/运行产物忽略
- 原因/命令：用户要求加 git 便于回退
- 下一步：之后每个增量完成即 commit；基线标签 baseline-20261101 可作为回退点

## [2026-11-01 20:23:38] 测试体系建立并通过实跑验证
- 状态：`scripts/selftest.ps1` 实跑 **7/7 PASS**（EXIT=0）；`TESTING.md` 写入四层测试指南
- 文件：scripts/selftest.ps1、TESTING.md（均已提交 git）
- 内容：无头自检覆盖 结构生成 / BREACHED / 异常生成 / 再收容 / 异常移除 / neck_snap 伤害类型 / 服务器启动完成
- 原因/命令：用户问如何测试
- 下一步：需要时跑 L3 客户端清单；L4 GameTest 待补

## [2026-11-01 21:51:51] L3 客户端实测（第一轮）完成并修复 2 个问题
- 状态：客户端成功启动，mod 正常加载；实测发现攻击日志刷屏 bug，已修复并通过 L1 自检复验
- 文件：event/AnomalyController.java、src/client/resources/.gitkeep（新增）
- 实测证据：
  - 客户端 `framework initialised: blocks=2, scps=1`
  - `/scp scps` → `registered: scpmod:scp_173`
  - `/scp event start` → 中文提示「警告：检测到收容失效，预计 60 秒后异常将被释放。」
  - 异常生成并攻击玩家（日志出现 snapped the neck）
- 发现问题：
  1. **攻击日志刷屏**：55 秒内 260 条 `anomaly snapped the neck`（缺冷却 + 对尸体每 tick 结算）→ 已修（20 tick 冷却、跳过死亡目标、只在成功命中时打日志）
  2. `Class path entries reference missing files: build/resources/client` 警告 → 已加 src/client/resources/.gitkeep
  3. authlib 401 / Realms / sessionserver 超时 = 开发环境离线账号的正常噪音，非模组问题
- 原因/命令：用户要求执行 L3 并后台监控日志
- 下一步：第二轮客户端验证死亡消息文本/Boss 条/石脚步声/终端再收容/统计；无刷屏

## [2026-11-01 22:01:43] L3 反馈全部实现并通过 L1 自检
- 状态：观察判定改屏幕视场角、近距离修复、创造/重生保护、30s 预警、配置拆三文件；构建 + 自检 7/7 PASS
- 文件：config/{EventConfig,AnomalyConfig}.java、event/AnomalyController.java（重写）、BreachEventManager.java、ScpMod.java、ScpCommand.java、README.md、TESTING.md
- 原因/命令：用户 L3 实测反馈（贴脸误判/创造模式/重生保护/30 秒/多设置文件）
- 下一步：第二轮 L3 验证（屏幕内冻结、贴脸不误杀、创造不追、重生 5s 保护、30s 预警、死亡消息）

## [2026-11-01 22:15:24] 创造物品栏 + 173 生成器完成
- 状态：3 个创造物品栏、SCP-173 生成器物品、/scp anomaly clear 命令；构建通过并已提交
- 文件：content/ModCreativeTabs.java、item/Scp173SpawnerItem.java、content/ModItems.java、command/ScpCommand.java、lang、item 模型
- 原因/命令：用户提出「加物品栏类 / 实体、方块&杂项、装备三个物品栏」
- 下一步：用户客户端确认三个物品栏显示正常；后续 GameTest/Jigsaw

## [2026-11-01 22:22:34] L3 第二轮实测（创造栏 + 生成器 + 死亡消息）——日志确认
**确认通过**：
- 创造栏/生成器：`anomaly body spawned at BlockPos{x=3, y=-60, z=0}`（右键生成器成功）
- **死亡消息正确**：`Player 被 SCP-173 折断了颈部`（自动带上实体名，非 raw key）
- 无模型/贴图缺失警告，无模组异常
- 攻击刷屏已修：本轮约 60 秒内 17 条（上一轮 55 秒 260 条）
**新观察**：
- 玩家在重生点附近被反复击杀（每 5~8 秒一次），与「重生保护 5 秒」一致，但体验上像在守尸；待用户确认是否符合预期，可选：击杀后异常回到释放点 / 延长重生保护 / 只在事件期间击杀。
- 无法从日志判断 FOV 观察、贴脸豁免、创造模式免疫 —— 需要用户主观反馈。

## [2026-11-01 22:30:49] 氛围小标题完成
- 状态：进入/离开异常范围的小标题已实现；构建 + L1 自检 7/7 PASS；客户端已重启待验证
- 文件：event/AnomalyController.java（updateAmbientMessages/sendSubtitle）、config/AnomalyConfig.java、lang/*.json、README
- 原因/命令：用户要求「到达173检测范围内提示玩家，进入和离开分别提示」
- 下一步：客户端确认两种小标题显示时机与文案；再决定守尸平衡方案

## [2026-11-01 22:44:08] Jigsaw 模板化完成 + 穿墙/视场角修复（待用户提交）
- **Jigsaw 已跑通**：自检日志显示 jigsaw 结构有效，站点包围盒 33×30×33，扫描到 stone_bricks=196 / containment_units=1 / terminals=1（随机房间拼接生效，模板带方块实体数据正常加载）
- 模板：`data/scpmod/structure/station/{start_room,room_a,room_b,corridor}.nbt`（自写 NBT 生成，DataVersion=4903）
- 数据：`template_pool/station/{start,rooms}.json`、`worldgen/structure/scp_jigsaw_station.json`（type=minecraft:jigsaw，size=4）、structure_set 指向新结构、tag 更新
- 视场角 70→100；攻击加视线门禁；移动改碰撞 + 局部 BFS 寻路（半径 12，10 tick 重算）
- 文档：新增 docs/ASSETS.md（材质/模型添加指南）；README/TESTING 更新
- 注意：按用户要求**本轮不提交 git**，等用户自己提交

## [2026-11-01 22:49:22] L3 第四轮（寻路/视场角版）日志检查
- 客户端启动后无任何游戏内操作（无 /scp 命令、无异常生成），日志 122 行，无 ERROR/Exception、无 jigsaw 警告
- 结论：本次会话未产生有效测试数据；寻路/防穿墙/视场角仍需用户实机操作验证

## [2026-11-01 23:08:54] SCP-173 生成器物品贴图替换 + 素材来源调研 + Blockbench 指南
- 用户提供 512×512 的 SCP-173 收容室标牌图（<用户目录>\Downloads\无标题.png），要求作为「生成 173 的物品」的材质（一开始我误做成独立标签物品，已纠正）
- 已用 Pillow 缩放到 64×64 生成 `assets/scpmod/textures/item/scp_173_spawner.png`，物品模型 layer0 改为 `scpmod:item/scp_173_spawner`；构建成功，jar 内已含该贴图
- 调研成套标签来源：SCP: Containment Breach 开源仓库（Regalis11/scpcb，GFX 下有 map/screens/items 等贴图，注意 CC BY-SA 授权）；SCP wiki 图像使用守则（合规）；SCP:SL 素材非开源不建议分发；推荐本地程序化生成
- 新增 tools/gen_scp_label.py（Pillow 批量生成统一风格标牌）+ tools/labels.example.json + docs/labels/ 三张样例（SCP-173/096/895）
- 新增 docs/BLOCKBENCH_ENTITY.md：Blockbench 做 Java 实体模型的完整流程（Java Entity 工程→导出模型类/贴图→ModelLayerLocation→EntityRenderer+RenderState→注册），以及常见坑
- 注意：本轮仍未提交 git（用户自行提交）

## [2026-11-01 23:19:48] Blockbench 新建参数已写入指南
- 用户截图询问「模组版实体」新建对话框怎么填；已在 docs/BLOCKBENCH_ENTITY.md 增加参数表：文件名 Scp173 / 模型标识符 scpmod:scp173 / 实体层级 main / 导出版本选最新 Mojmaps（无则 Forge 1.17+ Mojmaps）/ Y轴翻转先默认 / 纹理 64×64。

## [2026-11-06 22:48:03] 收到 Blockbench 导出的 scp173.java：诊断并落地模型类
- 用户提供 <用户目录>\Downloads\scp173.java（Blockbench 4.12.4 / Mojmaps 导出，39 行）
- 导出文件的问题：① 类名 `scpmod:scp173` 含冒号非法 ② 泛型 `<T extends main>` 非法 ③ `ResourceLocation` 在 26.2 已改为 `Identifier` ④ `LayerDefinition/MeshDefinition/PartDefinition/CubeListBuilder` 的包已变成 `net.minecraft.client.model.geom.builders` ⑤ 26.2 的 `Model.renderToBuffer(...)` 是 final 且签名变为 5 参数（无颜色/alpha），不能照旧重写 ⑥ 26.2 渲染走新的 submit/SubmitNodeCollector + RenderState 体系
- 已落地：`client/model/Scp173Model.java`（修正版，可编译）、`client/render/Scp173RenderState.java`、`textures/entity/scp173.png`（程序化水泥占位贴图，用户可替换）
- 构建通过（BUILD SUCCESSFUL）
- 下一步：自定义实体 Scp173Entity（Monster 子类）+ EntityType/属性注册 + ModelLayerRegistry 注册 + Scp173Renderer（submit 体系）+ AnomalyController 从 ArmorStand 切到自定义实体
- 仍未提交 git（用户自行提交）

## [2026-11-06 22:53:12] SCP-173 真实体接入完成（模型 + 渲染器 + 实体 + 行为切换）
- 新增 `entity/Scp173Entity extends Monster`（无 AI，`registerGoals` 空；属性 200 血/0 速/0 攻击/满击退抗性；不可推动）
- 新增 `content/ModEntities`：EntityType `scpmod:scp_173`（MONSTER，0.8×2.0，eyeHeight 1.7）+ FabricDefaultAttributeRegistry 属性注册；已接入 ScpMod.onInitialize
- 新增 `client/render/Scp173Renderer`：26.2 submit 体系（`createRenderState`/`extractRenderState`/`submit` + `collector.submitModel(model,state,poseStack,TEXTURE,light,NO_OVERLAY,-1,null)`），朝向用 `180 - bodyRotation`，`scale(-1,-1,1)`，`translate(0,-1.5,0)`
- `AnomalyController` 的 body 从 ArmorStand 换成 Scp173Entity（ArmorStand 残留 0 处）；生成逻辑改为 `new Scp173Entity(ModEntities.SCP_173, level)` + setPos/setNoGravity/setInvulnerable/setCustomName
- 客户端 `ScpModClient` 注册 `ModelLayerRegistry.registerModelLayer(Scp173Model.LAYER_LOCATION, Scp173Model::createBodyLayer)` 与 `EntityRendererRegistry.register`
- **构建一次通过**；已启动 runClient，启动日志无渲染/模型错误（只有离线账号 Realms 噪音），等待用户实机确认模型外观
- 仍未提交 git（用户自行提交）

## [2026-11-06 23:06:28] 用户实机反馈 4 项修复（重力/朝向/抖动/材质）
- 用户反馈：① 材质变成水泥占位（因为我暂时用程序化占位贴图，用户的 Blockbench 贴图尚未提供）② 隔墙通过发光能看到雕像往复抖动 ③ data get 显示 noGravity=true ④ 需要转向目标玩家
- 已修复：
  - 移除构造器与 spawn 里的 `setNoGravity(true)`，现在受重力落地
  - 新增 `faceTarget(target)`：每 tick 用 atan2 计算 yaw，设置 setYRot/setYHeadRot/yBodyRot/yBodyRotO，被观察时也面向玩家
  - 撞墙抖动：移动后若位移 <0.02 且无视线，设置 `stallUntilTick = now + 20`，原地等待，避免贴墙抽搐
- 材质：等待用户提供 Blockbench 的 64×64 贴图 PNG（当前 textures/entity/scp173.png 为程序化水泥占位）
- 发光：mod 未设置 glowing/发光；隔墙看到轮廓大概率是用户客户端 ESP/发光类模组（如 Meteor/Apophis）；若无此类模组再排查
- 构建通过，客户端已重启待复测

## [2026-11-06 23:14:31] 用户提供 173 实体贴图 + 水泥质感处理 + 转向规则修正
- 用户提供 <用户目录>\Downloads\texture.png（64×64，混凝土人形 + 红色喷漆痕迹）
- 用 Pillow 叠加程序化水泥质感：按原图亮度反比加权（暗部权重≈0.55、亮部≈0.04），细颗粒噪声 + 8×8 低频斑块，保留原色与 alpha；输出到 textures/entity/scp173.png；对比预览 docs/scp173_texture_preview.png
- 转向规则修正（用户要求）：**被注视时完全不转头**；只在「未被注视移动时」与「击杀瞬间」转向目标玩家（原实现是每 tick 都转）
- 构建通过；客户端已重启
- 仍未提交 git

## [2026-11-06 23:20:37] 刷怪蛋图标 + 跨 1 格台阶 + 修复发光轮廓
- 生成仿原版刷怪蛋风格的 `scp_173_spawner` 物品贴图（16×16：蛋形轮廓 + 173 水泥土黄色 + 红色喷漆点 + 左上高光），预览 docs/spawner_icon_preview.png
- 修复被一格高方块拦住：`moveTowards` 增加 `tryStepUp`（前方一格高障碍时垂直抬升 1 格，不改变水平位置，避免穿墙）
- **修复隔墙发光/白色描边根因**：26.2 `OrderedSubmitNodeCollector.submitModel(Model,S,PoseStack,Identifier,int,int,int,CrumblingOverlay)` 的第 7 个 int 实际传给接口方法的 **outlineColor**（默认方法内部 iconst_m1），我们此前传 -1 → 实体白色发光轮廓，隔墙可见；改为传 `state.outlineColor`（默认 0，不发光）
- 构建通过，客户端已重启
- 仍未提交 git

## [2026-11-06 23:26:30] 日志复查发现并修复「重生保护失效」
- 用户本轮日志：173 生成后连续 6 次被击杀（23:22:31/33/34/37/41/42，间隔 2~4 秒），说明 5 秒重生保护没有生效；随后切创造模式、设晴天
- 根因：`refreshPlayerState` 的 `spawnAtTick` 在玩家死亡时被设为死亡时刻；重生后（isAlive 变 true 且间隔 <40 tick）不满足 fresh 条件 → 保护期从死亡时算起，重生时已耗尽 → 复活即被秒
- 修复：`PlayerState` 增加 `alive` 字段；`fresh = previous == null || !alive || !previous.alive() || 离开 >40 tick`，即「重生瞬间」重新开始计时
- 构建通过；客户端已重启
- 仍未提交 git

## [2026-11-06 23:33:12] 重生保护改为事件驱动 + 173 贴图水泥质感加强
- 用户复测仍被秒杀（6 次击杀间隔 1~2 秒），说明状态推断式修复不可靠
- 改用 Fabric `ServerPlayerEvents.AFTER_RESPAWN` 事件：重生瞬间调用 `AnomalyController.noteRespawn(newPlayer)`，直接写入 spawnAtTick=当前 tick（并清空寻路状态）；`noteRespawn` 用 `player.level() instanceof ServerLevel` 取 gameTime（26.2 ServerPlayer 没有 serverLevel() 方法）
- 贴图加强：水泥叠加权重提高到 `(1-lum)^1.1*0.78+0.07`，并按饱和度降权（`*(1-0.78*sat)`）保留红色喷漆；新增低频暗斑气孔；对比图 docs/scp173_texture_preview.png
- 构建通过（先前一次失败：ServerPlayer.serverLevel() 不存在，已改 level() instanceof ServerLevel）
- 客户端已重启
- 仍未提交 git

## [2026-11-06 23:38:20] 物品紫黑块根因修复 + 实体贴图回滚
- 用户反馈：生成器物品仍是紫黑块；且没让我改实体贴图（要原来那版）
- **根因（重要）**：MC 26.2 物品渲染需要 `assets/<ns>/items/<id>.json` 物品模型定义（`{"model":{"type":"minecraft:model","model":"..."}}`）；项目只做了 `models/item/*.json`，所以物品一直是 missing model（紫黑块）
- 已补：`assets/scpmod/items/{scp_173_spawner,containment_unit,terminal}.json`，分别指向 `scpmod:item/scp_173_spawner`、`scpmod:block/containment_unit`、`scpmod:block/terminal`
- 实体贴图已回滚为 v1（weight `(1-lum)^1.3*0.55+0.04`，轻水泥质感，无暗斑/饱和度处理）
- 构建通过；客户端已重启
- 仍未提交 git

## [2026-11-06 23:45:14] 世界重载后接管旧 173 + 击杀音效调整
- 用户反馈：重建/重进世界后，上次放在地图里的 173 失效（模型在、有重力，但无法交互/无行为）。原因：`AnomalyController.body` 是静态引用，重进世界后丢失，旧实体没有被接管
- 修复：新增 `ensureAdopted(ServerLevel)`（每 20 tick 兜底扫描已加载实体，`level.getEntities(EntityTypeTest.forClass(Scp173Entity.class), 大 AABB, ...)`）与 `adopt(Scp173Entity)`（设为活动 body、清空状态、恢复 invulnerable/重力）；`ServerTickHandler` 每 tick 遍历 `server.getAllLevels()` 调用
- 击杀音效：`PLAYER_ATTACK_CRIT` → `STONE_HIT`（音量 0.35、音调 0.8），非常轻的石头敲击声
- 编译错误修正：`EntityTypeTest` 在 `net.minecraft.world.level.entity` 包（不是 world.entity）
- 构建通过；客户端已重启
- 仍未提交 git

## [2026-11-06 23:58:06] .gitignore 完善 + 收容间刮石环境音
- `.gitignore` 补充 IDE（.idea/*.iml/.vscode/.class/.project 等）、OS 垃圾（.DS_Store/Thumbs.db/desktop.ini）、crash-reports/logs/hs_err_pid 等
- 新增 `event/ContainmentAmbience`：收容单元非空且 8 格内无玩家时，每 80~200 tick 随机播放 `STONE_STEP`（音量 1.0、音调 0.35，AMBIENT）——对应 SCP-173「收容间无人时传出刮石声」
- 玩家进入 8 格 → 刮石声停止，并向该玩家发送 `message.scpmod.report.scrape_stopped`（“已向值班中的代理 HMCL 监督员报告”）；空单元不发声
- `ContainmentUnitBlockEntity` 增加静态弱引用集合 LOADED（setLevel/setRemoved 维护）+ nextScrapeTick/hadNearbyPlayer 状态
- ServerTickHandler 每 20 tick 调用一次环境音系统
- 编译错误：`Level.random` 是 protected，改用自有 `java.util.Random`
- 构建通过；客户端已重启
- 仍未提交 git

## [2026-11-07 12:11:37] 去掉 HMCL 监督员；刮石声改为 173 未观察时触发 + 字幕
- 用户要求：删除 HMCL 监督员信息；收容单元环境音那套不做，去掉/禁用；改为 173 被生成且**无任何视线接触**时触发「小幅度移动 + 音效」，音效用右下角字幕显示为「SCP173：刮石」
- 已删除：`ContainmentAmbience.java`、ServerTickHandler 中的调用、收容单元 BE 的 LOADED/环境音状态（回滚）、`message.scpmod.report.scrape_stopped` 两个语言键
- 新增 `content/ModSounds`：`scpmod:scrape`（`SoundEvent.createVariableRangeEvent`），注册到 SOUND_EVENT，ScpMod.onInitialize 调用
- 新增 `assets/scpmod/sounds.json`：scrape → subtitle `subtitles.scpmod.scrape`，音源复用原版 `minecraft:block/stone/step/stone1~4`（音量 1.0、音调 0.35）
- 字幕：`subtitles.scpmod.scrape` = 中文「SCP173：刮石」/ 英文「SCP173: Scraping」
- AnomalyController：未观察时每 10 tick 小幅漂移（0.06~0.16 格）+ 每 40~80 tick 播放 SCRAPE；被观察时立即停止移动与声音
- 构建通过；客户端已重启待测试
- 仍未提交 git

## [2026-11-07 12:16:49] 刮石声改用自制 ogg + 观察判定多采样点 + 村民踱步
- 用户反馈：① 没听到刮石声（上一版引用原版 `block/stone/step/stone1.ogg`，26.2 资源包中不存在 → WARN "does not exist"）② 部分遮挡时 173 仍在移动，会被人看到移动；瞬移感强，希望像村民踱步
- 刮石声：用 ffmpeg 自制 3 段 ogg（`assets/scpmod/sounds/scrape1~3.ogg`，Python 合成噪声摩擦声 + 衰减包络），sounds.json 指向同命名空间；字幕 `subtitles.scpmod.scrape` = SCP173：刮石
- 观察判定：原本只检查实体中心一点 → 部分遮挡被判未观察。改为包围盒 8 角 + 中心共 9 个采样点，任一可见即算被观察；**旁观者不算观察者**（旁观能看到它移动）
- 移动：`nudge`（每 10 tick 瞬移 0.06~0.16 格）改为 `wander` 村民踱步式（速度 0.015 格/tick ≈ 0.3 格/秒，在 1~2.8 格半径内随机选可站立目标点，到达/超时/撞墙后重选）
- 构建通过；客户端已重启待测试
- 仍未提交 git

## [2026-11-07 12:36:15] 刮石声改回原版 STONE_STEP + 取消主动踱步
- 用户反馈：自制刮石声太诡异；希望用 `block.stone.step` 这类原版声音；频率不要太高；**不用让它主动移动**
- 已删除：自制 ogg（sounds/scrape1~3.ogg）、sounds.json、ModSounds.java、subtitles.scpmod.scrape 语言键、ScpMod 中的 ModSounds 注册
- 刮石声改为原版 `SoundEvents.STONE_STEP`（音量 1.0、音调 0.7），间隔改为 **100~200 tick（5~10 秒）**
- 删除 wander/pickWanderTarget/findGround 及调用：未观察时不再主动踱步；只有追击玩家时才会移动
- 构建通过；客户端已重启
- 仍未提交 git

## [2026-11-07 12:45:01] 字幕修复（自定义声音事件 type=event）+ 全项目隐私脱敏
- 用户反馈：有声音但字幕没显示
- 修复：重新加入 `ModSounds.SCRAPE` 自定义声音事件；sounds.json 用 `{"type": "event", "name": "minecraft:block.stone.step"}` 复用原版石头脚步声，字幕 `subtitles.scpmod.scrape` = 「SCP173：刮石」；客户端日志无声音加载警告
- 提醒用户：MC 的字幕默认关闭，需要在 选项→音乐和声音→显示字幕 打开
- **隐私脱敏**（用户要求）：扫描 85 个文本文件，17 个命中并替换：`<用户目录>`→`<用户目录>`、`<JDK 路径> 路径>`、`<Gradle 缓存> 缓存>`、`<代理端口>`→`<代理端口>`、`Player`→`Player`；清理 docs/PROMPT 残留 `.dsh` 路径；复查无残留
- ⚠️ **git 历史风险**：3 个旧提交仍包含 `<用户目录>` 路径（c89e407、010c0c9、79447d3）——工作区已清理但历史仍在；若要公开仓库需重写历史（待用户决定）
- 构建通过；客户端已重启
- 仍未提交 git

## [2026-11-07 12:50] 音效换成磨石声 + git 历史重写完成 + 历史压缩为单提交
- sounds.json 的 scrape 事件改为引用 `minecraft:block.grindstone.use`（磨石使用声），字幕仍为「SCP173：刮石」
- git 历史重写：全部旧提交被重写以移除本机路径/用户名/玩家名；清理 refs/original、reflog、gc 后压缩为单个初始提交
- 远程仓库已用 force push 覆盖；旧提交哈希失效
- 如果还有旧提交缓存，是 GitHub 服务端缓存，可等待回收或删除仓库重建

## [2026-11-07 13:03:53] git 历史最终压缩为单提交 + 删除 Opus55 提示词文档
- 用户指出旧提交仍能看到本机路径（GitHub/IDE 旧对象缓存）并要求删除 docs/PROMPT_给Opus55_标牌模板.md
- 处理：修复被编码破坏的 memory 文件（从备份恢复 + UTF-8 重写）→ 删除 PROMPT 文档 → 全项目 Node 脱敏 → `git checkout --orphan` 重建历史为**单个提交** `2acbf0a` → 删除旧 main/tag/远程跟踪引用 → reflog expire + gc
- 验证：本地提交数=1，无本机路径/玩家名命中；远程（GitHub）main 与 tag 均 force push 为 `2acbf0a`
- 已删除含旧历史的备份目录（位于本地工具缓存区）
- 提醒：GitHub 服务端可能缓存旧对象一段时间；正常仓库 History 已干净；若旧 commit 直链仍可访问，可等待回收或删库重建
- 磨石音效（`block.grindstone.use`）已构建；客户端已重启测试
