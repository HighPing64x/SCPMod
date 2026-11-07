# 下一步（08-next）

## [2026-10-31 13:04:47] 等待用户确认，确认后进入 M1
- 状态：阻塞在用户确认（用户已离场，要求不用提问工具）
- 文件：memory/08-next.md
- 内容：恢复后立即要做的事、待用户决定项、M1 开工步骤
- 原因/命令：每轮更新 next
- 下一步：见下方动作清单

---

## 1. 用户回来后首先要做的两件事
1. 对 02-plan 第 3 节（M1~M3 交付物与验收）+ 04-decisions 的 A01~A04 推定项回一句「确认」或指出要改的编号。
2. 补定两个待定项：
   - **O1 非目标清单**（推荐：剧情/任务/NPC、自定义维度、合成经济、图形化配置、多版本兼容、图鉴 UI 全部排除）
   - **O8 时间盒数值**（每个里程碑的周数上限）

## 2. 确认后 M1 的开工顺序（尚未执行）
1. 定 **modid / 包名 / 显示名**（O6）—— 一旦确定写入 04-decisions 与 05-files。
2. 建 Fabric 工程：拉 fabric-example-mod 的 **26.2** 分支为基线（loader 0.20.1 / loom 1.19-SNAPSHOT / fabric-api 0.161.0+26.2 / Java 25 / splitEnvironmentSourceSets）。
3. 写 memory 的 M1 任务分解，然后按 结构层 → 方块层 → 注册层 → 数据层 顺序实现。
4. 每完成一个子任务立即追加 03-progress / 05-files / 06-commands。
5. 每 3 次工具调用或上下文变长写 checkpoint。

## 3. 仍待用户决定（O1~O8，详见 01-requirements 第 4 节）
O1 非目标清单 / O2 首批 SCP 清单（内容阶段）/ O3 美术排期细节 / O4 173 观察判定口径 / O5 文本语言 / O6 modid 命名 / O7 存档兼容期望 / O8 时间盒数值。

## 4. 恢复指令（给未来的自己）
> 先读 memory/00-index.md 与最近 checkpoint → 再读 04-decisions.md → 检查 08-next.md 第 2 节是否已执行 → 未执行则从 M1 第 1 步开始；已执行则按 03-progress 最后一条继续。


## [2026-10-31 21:24:17] M1 进行中：下一步动作
- 状态：等待首次 Gradle 编译结果
- 文件：见 05-files.md
- 内容：M1 任务分解与当前卡点
- 原因/命令：用户授权后推进
- 下一步：见下

### 当前步骤
1. [进行中] 首次 `compileJava`：下载 MC 26.2 / fabric-loader / fabric-api 并编译最小入口类。
2. [待做] 用实际 jar 核对 26.2 不混淆类名（禁止凭 1.21 记忆写 API）。
3. [待做] 注册层：Scp 接口/基类 + ScpRegistry + 事件钩子。
4. [待做] 方块层：ContainmentUnitBlock(+BE) / TerminalBlock(+BE)，拆解=泄压。
5. [待做] 数据层：JSON 配置 + /scp 命令（locate/status/event/spawn）。
6. [待做] 结构层：Jigsaw 站点（模板池 + worldgen JSON + 首个站点模板）。NBT 模板需用工具生成（见风险 R7）。
7. [待做] 资源：blockstates/models（先用原版材质占位）。
8. [待做] 编译通过后写 GameTest 骨架与设计文档。

### 新增风险
- **R7 NBT 结构模板生成**：.nbt 是二进制格式，需用 NBT 写入器或「游戏内导出」方案生成；DataVersion 必须匹配 26.2。M1 若卡住，改用「代码内生成站点（自定义 Structure + Piece）」过渡，再补 Jigsaw 数据。
- **R8 首次构建耗时**：需从 mojang/fabric maven 下载数 GB 依赖，可能 10~30 分钟。


## [2026-10-31 21:41:04] 当前进行中
- 状态：后台编译任务 pwsh-94（Gradle 9.8.2 + IPv4）
- 文件：见 05-files.md
- 内容：M1 代码已全部写入，等待编译反馈
- 原因/命令：M1 实现
- 下一步：
  1. 读 gradle-build.log，按编译错误修 26.2 API 名称（重点：ServerTickEvents / CommandRegistrationCallback / player.pick / ServerLevel.players）。
  2. 通过后补：创造模式物品栏（ItemGroupEvents）、/scp unit set 调试命令、结构层 Jigsaw 数据文件与首个模板。
  3. 更新 memory 与 checkpoint。


## [2026-10-31 21:48:54] M1 剩余任务（按优先级）
- 状态：代码完成待联网构建验证
- 文件：见 05-files.md
- 内容：下一步清单
- 原因/命令：M1 收尾
- 下一步：

1. **网络恢复后**：Gradle 9.8.2 跑 `build` 出 jar，确认 Loom 重映射 + fabric.mod.json 打包无误。
2. 创造模式物品栏（Fabric ItemGroupEvents）——需真实 Fabric API 编译验证。
3. `/scp locate`：接入 26.2 的 LocateCommand/Structure 查找。
4. **结构层（Jigsaw）**：template pool JSON + 首个站点 .nbt（需要 NBT 生成方案，见风险 R7）。
5. GameTest：收容单元拆解泄压、事件状态机流转。
6. 设计/维护文档（可放 docs/ 或 memory 之外的项目文档）。

## [2026-11-01 10:44:42] M1 完成，下一步 M2
- 状态：M1 达成（构建 + 运行双验证）
- 文件：见 05-files.md
- 内容：M1 收尾小项 + M2 主体
- 原因/命令：阶段收口
- 下一步：
  1. 创造模式物品栏（Fabric ItemGroupEvents，依赖已就绪）
  2. GameTest：收容单元拆解泄压、事件状态机流转
  3. Jigsaw 结构层：template_pool JSON + structure JSON + 首个站点 .nbt（需 NBT 生成方案）
  4. /scp locate 接入结构查找
  5. M2 主体：SCP-173 视线判定/冻结/瞬移；突破后生成实体、世界级追猎、再收容、结算统计持久化；Boss 条/警报音/提示文本

## [2026-11-01 11:12:18] 基底已通，下一步
- 状态：结构基底通过世界生成验证
- 文件：见 05-files.md
- 内容：下一阶段任务
- 原因/命令：用户「先做基底」已完成
- 下一步：
  1. 去噪：ScpStationPiece 的按区块 debug 日志保留在 selftest 开关下即可（已隔离）
  2. 站点内容深化：更多房间类型/走廊/门（后续可迁移到 Jigsaw template_pool + .nbt）
  3. M2：SCP-173 实体（视线判定/冻结/瞬移/扭颈）接入收容单元与事件链
  4. 事件链实体化：突破后生成 SCP、世界级追猎、再收容、结算统计持久化
  5. GameTest：拆解泄压、事件状态机流转、站点生成

## [2026-11-01 12:35:25] 剩余工作（按优先级）
1. **SCP-173 实体**：EntityType/自定义 Mob（服务端视线判定：准星射线+可见性；被注视时冻结，视线移开瞬移靠近并伤害）；BREACHED 阶段生成于 origin；客户端渲染器（可先用原版方块/雕塑模型占位）。
2. **GameTest**：收容单元拆解→start()；终端 recontain；站点生成断言。
3. **Jigsaw 模板化**：把程序化房间迁到 template_pool + .nbt（需要 NBT 生成方案），实现 D10 的「多房间随机布局」。
4. 客户端运行验证（首次 runClient 需下载 assets，约 200MB，走 Geph 代理）。
5. 清理：structure piece 的 selftest debug 日志保留在开关下；run/ 与 build/ 已在 .gitignore。

## [2026-11-01 19:02:19] 剩余工作
1. **客户端实机验证**：首次 runClient 需下载 ~200MB assets（走 Geph）；验证 173 外观、注视冻结、未注视逼近、伤害、Boss 条 UI。
2. **GameTest**：收容单元拆解→事件启动；终端 recontain；站点生成断言（fabric-gametest-api）。
3. **Jigsaw 模板化**：template_pool + .nbt 房间（需 NBT 生成方案），替换现有程序化房间。
4. 完善项：注视遮挡（LOS）、SCP-173 声音/粒子、更多 SCP、创建物品栏。

## [2026-11-01 19:12:08] 173 平衡与验证待办
1. **客户端实机**：验证「注视即冻结/一眨眼贴脸/断颈」手感是否过凶；建议同时把 MAX_STEP、KILL_RANGE、伤害提到 scpmod.json 配置。
2. GameTest：拆解泄压、终端再收容、站点生成。
3. Jigsaw 模板化。

## [2026-11-01 19:32:21] 客户端验证清单（下次）
1. `runClient`（首次需下 assets，走 Geph）
2. 进入世界：`/scp station build` 或 `/scp locate`，破坏收容单元触发事件
3. 验证：Boss 条预警 → 173（石头头 ArmorStand）出现 → 盯着它则冻结 → 视线移开瞬间贴脸 → 死亡消息「xx 被 SCP-173 折断了颈部」→ 移动时能听到石脚步声
4. 若手感过狠：调 AnomalyController.MAX_STEP / KILL_RANGE，或提到 scpmod.json

## [2026-11-01 19:33:02] Git 工作流约定
1. 每个可运行增量完成后 commit（构建通过 + selftest 通过再提交）。
2. 大改（如 Jigsaw 模板化）从 `baseline-20261101` 开分支试，成功再合并回 main。
3. 提交信息格式：`<area>: <what>`（例：`scp173: source-accurate speed + neck snap message`）。
4. memory/ 与代码一起提交，保证上下文可回溯。

## [2026-11-01 20:23:38] 测试工作流约定
1. 每次改动后顺序：`build.ps1` → `selftest.ps1` → 通过才 `git commit`。
2. 涉及玩家可见行为（UI/音效/手感/死亡消息）的改动，额外记录到 TESTING.md 的 L3 清单，等客户端实机验收。
3. 下一项自动化：GameTest（拆解泄压→事件启动、终端再收容、站点生成断言）。

## [2026-11-01 21:51:51] 第二轮 L3 验证清单
1. 死亡消息文本是否为「<玩家> 被 SCP-173 折断了颈部」（不是 death.attack.... 原文）
2. Boss 条是否显示预警 → 突破颜色变化
3. 未观察时是否听到石头脚步声（STONE_STEP）
4. 日志中 snapped 是否最多 1 条/秒、且死后不再刷
5. 用已激活终端再收容 → Boss 条变绿 + `/scp stats` 数字 +1
6. 进入创造模式检查方块是否可放置、右键提示是否正常

## [2026-11-01 22:01:43] 第二轮 L3 验证清单（客户端已重启）
1. 屏幕内的任意位置（不一定要准星指着）看它 → 冻结
2. 贴近它但保持它在屏幕内 → 不触发击杀
3. 创造模式 → 不被追、不被杀（可用来安全观察）
4. 死亡重生后 5 秒内 → 不被攻击；5 秒后恢复
5. `/scp event start` 预警应为 **30 秒**
6. 死亡消息「xx 被 SCP-173 折断了颈部」；日志不再刷屏（每秒最多 1 条）
7. `/scp reload` 后改 config/scpmod-anomalies.json 的值应生效（如 FOV=100）

## [2026-11-01 22:15:24] 待确认 / 后续
1. 客户端确认：创造模式物品栏出现「SCPMod：实体 / 方块&杂项 / 装备」三栏，实体栏里有 173 生成器。
2. 装备栏目前为空（预留）；后续做 SCP 装备（防护服/手电/武器等）往里填。
3. GameTest、Jigsaw 模板化仍在队列。

## [2026-11-01 22:22:34] 待用户确认的平衡点
1. 击杀后异常是否应返回释放点（避免守重生点）？
2. 重生保护 5 秒是否够（可调 scpmod-anomalies.json → respawnProtectionSeconds）？
3. FOV/贴脸豁免/创造免疫的主观手感是否正常（日志无法判断）。

## [2026-11-01 22:30:49] 氛围提示验证与后续
1. 客户端验证：拿生成器放 173，靠近到 32 格内应出现「背后凉飕飕……」；走远到 36 格外应出现「不再那么压抑了……」
2. 仍在等用户决定守尸平衡方案（杀完回释放点 / 延长重生保护 / 保持现状）。

## [2026-11-01 22:44:08] 交给用户的提交清单（AI 不提交）
- 变更内容：jigsaw 模板化 + FOV 100 + 寻路/防穿墙/LOS 击杀 + docs/ASSETS.md + memory
- 建议提交命令：
  git add -A
  git commit -m "feat(structure): jigsaw template pools + NBT rooms; feat(scp173): wider FOV, no wall-phasing, LOS-gated kills, local pathfinding"
- 之后待办：客户端实测寻路/不穿墙/更宽容视场角；GameTest。

## [2026-11-01 23:08:54] 下一步待办
1. 用户自行 git 提交（AI 不再提交）：`git add -A && git commit -m "feat(assets): SCP-173 spawner texture, label generator, Blockbench guide"`
2. 若要做“收容室挂牌”：新增 label 方块（可贴在墙上，贴图为该标牌图），或做 label 物品右键放置。
3. 若要做真实体 173：Blockbench 做模型 → 自定义 Entity + 客户端渲染器（需 26.2 RenderState 适配，我来写）。
4. 批量生成其他 SCP 标牌：改 tools/labels.example.json 后重跑脚本。

## [2026-11-01 23:14:31] 待办
1. 用户把提示词发给 Opus55，拿回模板代码/设计后交给我跑通并批量产出 SCP 标牌。
2. 用户自行 git 提交（AI 不提交）：docs、tools、贴图、memory 等改动仍在工作区。
3. 之后决定标牌用途：仅物品图标 / 收容室墙上的挂牌方块。

## [2026-11-06 22:48:03] 接实体渲染的待办
1. 新建 Scp173Entity extends Monster（禁用 AI、注册属性、尺寸 0.8×2.0）
2. ModEntities 注册 EntityType + FabricDefaultAttributeRegistry 属性
3. 客户端：ModelLayerRegistry.registerModelLayer(Scp173Model.LAYER_LOCATION, Scp173Model::createBodyLayer)；EntityRendererRegistry.register(ModEntities.SCP_173, Scp173Renderer::new)
4. Scp173Renderer：createRenderState/extractRenderState/submit（用 SubmitNodeCollector.submitModel(..., Identifier, light, overlay, color, null)）
5. AnomalyController 的 body 从 ArmorStand 换成 Scp173Entity；测试客户端模型显示/朝向/比例

## [2026-11-06 22:53:12] 等待用户查看模型效果；可能需要微调的点
1. 模型朝向不对 → 调 `Scp173Renderer.submit` 里的 `Axis.YP.rotationDegrees(180 - bodyRotation)`
2. 模型悬空/陷地 → 调 `poseStack.translate(0, -1.5, 0)` 的 Y 值（当前 173 模型本体在 Blockbench 里是 24 偏移）
3. 模型大小不对 → 加 `poseStack.scale(s,s,s)`
4. 贴图错位 → 用正式 64×64 贴图替换 `textures/entity/scp173.png`（UV 与原 Blockbench 一致）
5. 若客户端崩溃，看 run/logs/latest.log 中 `Scp173Renderer`/`Scp173Model` 相关堆栈，我据此修

## [2026-11-06 23:06:28] 待用户提供/确认
1. 用户提供 173 实体贴图（64×64 PNG，Blockbench 导出），替换 assets/scpmod/textures/entity/scp173.png
2. 复测：重力落地、面向玩家、撞墙不抖、材质（若贴图提供后）
3. 模型朝向/位置若仍有偏差：调 Scp173Renderer 的 rotationDegrees/scale/translate

## [2026-11-06 23:17:11] 待用户确认（贴图/转向版）
1. 173 模型外观：贴图颜色/水泥质感强度（暗部权重 `(1-lum)^1.3 * 0.55 + 0.04`，可调）；生成对比图 docs/scp173_texture_preview.png
2. 转向行为：被注视时不转头、未注视移动/击杀时转向 —— 用户复测确认
3. 重力落地、撞墙不抖动是否正常
4. 若模型朝向/位置仍有偏差：调 Scp173Renderer 的 rotationDegrees/scale/translate（08-next 前面已有清单）
5. 用户自行 git 提交全部改动

## [2026-11-06 23:23:15] 待用户复测（刷怪蛋/跨台阶/发光修复）
1. 创造栏「实体」里的生成器图标应为刷怪蛋风格（水泥色+红漆点）
2. 173 不再有白色发光轮廓、隔墙不可见（outlineColor 修复）
3. 一格高的方块/台阶能跨上去
4. 用户自行 git 提交全部改动

## [2026-11-07 12:45:01] 待用户确认
1. 字幕：打开「显示字幕」后测试右下角是否显示「SCP173：刮石」
2. 是否要重写 git 历史以彻底删除旧提交里的 `<用户目录>` 路径（会改变所有提交哈希，需用户确认）
3. 用户自行提交本次改动（脱敏 + 字幕）

## [2026-11-07 12:50] 待用户测试
1. 磨石音效 + 字幕「SCP173：刮石」（需要开启显示字幕）
2. 观察冻结/重生保护/旧实体接管等功能是否正常
