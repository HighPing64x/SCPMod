# 错误与修复（07-errors）

## [2026-10-31 13:04:47] 记录本阶段遇到的 5 个问题及解决
- 状态：全部已解决，无遗留阻塞
- 文件：memory/07-errors.md
- 内容：现象 / 原因 / 解决
- 原因/命令：用户要求错误立即落盘
- 下一步：实现阶段继续追加

---

### E01 文件沙箱无法授权写入工作区（已解决）
- 现象：run_code 每次失败 SetNamedSecurityInfoW failed (Win32 5): grantWrite(E:\SCPMod)；连只读程序都无法启动。
- 原因：DSH 文件沙箱在 workspace-write 模式下需要给受限主体授予工作区写 ACL，该 ACL 授权失败（Win32 5 = 拒绝访问）。
- 解决：用户把会话文件策略改为 **danger-full-access**，之后一切读写正常。**不要再请求 sandbox 升级**（当前 approval policy = never，请求会被自动拒绝）。

### E02 ask_user_question 超时（已解决）
- 现象：ask 工具在默认 120s 执行预算内未返回，报 execution deadline reached。
- 原因：提问等待时间算在 run_code 的执行预算内。
- 解决：调用 run_code 时显式设置 timeoutMs=600000（10 分钟），ask 可正常等待用户作答。

### E03 web_search 不可用（已绕开）
- 现象：web_search 报 configured web provider "deepseek-official" is not registered。
- 解决：改用 **web_fetch** 直接取 API 与 raw 文件；事实性数据（Fabric 元数据、Modrinth、GitHub 原始文件）均通过 web_fetch 获得。

### E04 web_fetch 返回被截断导致 JSON 解析失败（已解决）
- 现象：拉取 fabric-api 全部版本列表时报 Unterminated string in JSON at position 100000（web_fetch 内容上限 100000 字符）。
- 解决：改用带过滤参数的接口（?game_versions=[...]）缩小返回体，再解析。

### E05 meta.fabricmc.net 子接口超时（已绕开）
- 现象：/v2/versions/loader 与 /v2/versions/loader/26.2 等请求 30s 超时或 fetch failed。
- 解决：改从 fabric-example-mod 的 26.2 分支 gradle.properties 直接读取 loader/loom/fabric-api 版本号，事实同等有效。


## [2026-10-31 21:41:04] 新增错误（E06~E09）
- 状态：E06/E07 已解决，E08 缓解中，E09 待编译确认
- 文件：memory/07-errors.md
- 内容：构建环境问题与处理
- 原因/命令：M1 首次构建
- 下一步：编译结果出来后补记

### E06 Gradle 版本过低（已解决）
- 现象：`Could not resolve net.fabricmc:fabric-loom:1.19.3` → `requires at least Gradle 9.8.0. This build uses Gradle 9.5.1`
- 解决：改用 Gradle 9.8.2（腾讯镜像手动下载解压）。

### E07 wrapper 无法联网下载发行版（已绕过）
- 现象：gradlew 下载 gradle zip 时 `java.net.SocketException: Permission denied: getsockopt`；但 curl.exe 相同 URL 可通。
- 解决：不再依赖 wrapper 自动下载，改为 curl 下载 + tar 解压 + 直接调用 gradle.bat。wrapper 仍保留供用户使用。

### E08 maven.fabricmc.net 极慢/超时（缓解中）
- 现象：解析 tiny-remapper 0.15.0 的 .module 超时（Read timed out）；curl 测速默认路由 1.5KB/s。
- 解决：构建 JVM 与 Gradle 均加 `-Djava.net.preferIPv4Stack=true`，HTTP 超时 180s、重试 5 次。仍有下载耗时风险（首次构建可能很久）。

### E09 Gradle 日志编码（已规避）
- 现象：`cmd /c ... > log` 重定向产生 UTF-16 混合字节，Node 直接按 utf8 读会错乱。
- 解决：读取时检测 BOM/零字节并按 utf16le 解码。


## [2026-10-31 21:48:54] E10 maven.fabricmc.net 带宽阻塞（未解决，已绕过验证）
- 现象：Gradle 解析 net.fabricmc:* 时长期停在 .module/.jar 下载；缓存 482KB 不再增长；curl 测速 0.1~5KB/s 且频繁超时；Cloudflare 两个 IP 均慢；IPv4 已是最好情况。
- 尝试：腾讯/华为/阿里/BMCLAPI 镜像均无 Fabric 构件（404）；corsproxy/allorigins 不稳定。
- 绕过：写 .compilecheck 离线 javac 验证通道（见 06-commands），M1 源码 0 错误。
- 遗留：无法产出 jar、无法启动 dev client 实测。待网络恢复后必须补跑 Gradle。

## [2026-10-31 21:48:54] E11 本地库目录含旧版 Minecraft client jar（已解决）
- 现象：离线 javac 报「BaseEntityBlock 无抽象方法 newBlockEntity」「找不到 simpleCodec/getBlockEntity」等一堆错。
- 原因：E:\Minecraft2 与 E:\Minecraft 的 libraries 里有 1.18.2/1.19.2/1.20.1/1.21.6 的 client-*-slim/srg/extra jar，classpath 顺序导致旧类遮蔽 26.2 类。
- 解决：classpath 中把 26.2 merged jar 放第一位，并排除路径含 net/minecraft、net/minecraftforge、net/neoforged 的 jar。

## [2026-10-31 21:48:54] E12 javac argfile 反斜杠被当转义（已解决）
- 现象：`找不到文件: E:SCPModsrcmainjava...`
- 解决：argfile 内路径统一用正斜杠、不加引号（路径本身无空格）。


## [2026-10-31 23:01:03] E10 解决：走迷雾通代理
- 现象回顾：直连 maven.fabricmc.net 0.1~1.5KB/s，依赖解析永久卡住。
- 解决：本机迷雾通 Geph 正在运行，<代理端口> (HTTP) / 9909 (SOCKS5)；JAVA_TOOL_OPTIONS 套 HTTP 代理后下载 153KB/s，可正常构建。
- 待办：构建成功后在 02-plan 记录「开发环境必须开 Geph」。

## [2026-11-01 10:44:42] E13 运行期 NPE「Block id not set」（已解决）
- 现象：runServer 启动时 entrypoint 抛 ExceptionInInitializerError → NPE: Block id not set at BlockBehaviour$Properties.effectiveDrops
- 原因：MC 26.x 要求在构造 Block 前调用 Properties.setId(ResourceKey<Block>)；Properties.of() 不再自带 id
- 解决：ModBlocks.register(name, factory, props) 内 props.setId(ResourceKey.create(Registries.BLOCK, id)) 后再构造并 Registry.register；Item 同理

## [2026-11-01 10:44:42] E14 静态初始化顺序导致 COUNT 被重置（已解决）
- 现象：启动日志 blocks=0
- 原因：private static int COUNT = 0; 声明在方块字段之后，静态初始化按文本顺序执行把已累加值覆盖回 0
- 解决：COUNT 声明移到方块字段之前（仅影响日志）

## [2026-11-01 10:44:42] E15 PowerShell 传 -D 给 java 被吞（已解决）
- 现象：java -Dhttps.proxyHost=... Foo.java 报 ClassNotFound .proxyHost=...
- 解决：改用 $env:JAVA_TOOL_OPTIONS，JVM 自动读取，Gradle 子进程也继承

## [2026-11-01 11:12:18] E16 StructurePiece 未设 orientation → 坐标不变换（已解决）
- 现象：结构 start 有效、包围盒正确，但扫描盒内 stone_bricks=0；日志报 `Detected unsafe terrain read ... reading from chunk [0,0] while generating [-17,-32]` + `Requested chunk unavailable during world generation`
- 根因：MC 26.2 的 `StructurePiece.getWorldPos/getWorldX/Y/Z` 只有在 `orientation != null` 时才把局部坐标映射为世界坐标；否则原样返回局部坐标 → placeBlock 被 chunkBox 拒绝，且读 (0,0,0) 落到 0 号区块。
- 解决：piece 两个构造器里都调用 `this.setOrientation(Direction.NORTH)`。

## [2026-11-01 11:12:18] E17 自检在主线程 getChunk 触发 watchdog（已规避）
- 现象：世界生成异常后，自检在 SERVER_STARTED 主线程 `level.getChunk()` 阻塞，60s 后 watchdog 强退。
- 处理：生成 bug 修复后不再阻塞；同时把查找半径从 128 降到 64 区块，减少主线程卡顿风险。

## [2026-11-01 21:51:51] E18 异常对尸体每 tick 重复攻击并刷屏日志（已修复）
- 现象：L3 实测 55 秒写出 260 条 `[scpmod] anomaly snapped the neck of Player`；玩家死亡后仍在持续结算
- 原因：快速行为重写时删掉了冷却字段；`body.distanceTo(nearest) <= KILL_RANGE` 每 tick 成立且未检查目标存活
- 修复：恢复 `lastAttackTick`，冷却 20 tick；`!nearest.isAlive()` 直接 return；只有 `hurtServer` 返回 true 才播放音效并打日志
- 验证：BUILD SUCCESSFUL + L1 自检 7/7 PASS

## [2026-11-06 23:38:20] E19 物品显示紫黑块（missing model）
- 现象：物品栏/手持物品显示紫黑格
- 原因：**MC 1.21.4+/26.2 起物品模型由 `assets/<ns>/items/<id>.json` 定义**；只有 `models/item/<id>.json` 不会被自动绑定
- 修复：为每个物品补 `assets/<ns>/items/<id>.json`（`{"model":{"type":"minecraft:model","model":"<ns>:item|block/<name>"}}`）；方块物品指向 block model

## [2026-11-06 23:45:14] E20 重进世界后旧 173 失效
- 现象：世界重载后实体仍在（模型/重力正常），但不再追踪/攻击玩家
- 原因：AnomalyController 用静态 `body` 引用；新会话静态为空，旧实体未被接管
- 修复：`ensureAdopted()` 定时扫描并 `adopt()`（最好用实体加载事件，但 26.2 fabric-entity-events-v1 中 `ServerEntityEvents` 已不存在，改用轮询扫描）
