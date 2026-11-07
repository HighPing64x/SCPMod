# 用 Blockbench 给 SCPMod 做实体模型（中文指南）

> 当前 SCP-173 是 **ArmorStand + 石方块** 占位。要换成真正的模型，需要在客户端加「实体模型 + 渲染器」，下面是完整流程。

## 一、Blockbench 准备

1. 下载 [Blockbench](https://www.blockbench.net/)（免费开源）
2. 新建项目时，在 **Minecraft** 分类里选 **「模组版实体」（Modded Entity）**
   - 说明：Blockbench 的新建菜单里没有叫 “Java Entity” 的项；对应 Java 模组实体建模的是 **模组版实体**
   - 不要选「Bedrock 版实体」（那是基岩版格式，Java 用不了），也不要选「Java 版方块/物品」（那是方块/物品，不是实体）
   - 会提示填写 `Entity Name`（写 `Scp173`）、`Texture Width/Height`（建议 64×64 或 128×128）
3. 建模要点：
   - **模型原点**在脚底中心（y=0），面朝 **+Z**（Minecraft 实体都朝 +Z，渲染时由代码旋转）
   - 只用一个 texture、UV 自动或手动排布都行
   - 姿态默认 T-Pose/A-Pose 均可，动画不是必需的（173 本来就不动）
4. 画贴图：Blockbench 左侧「Paint」→ 右上「Create Texture」，画完直接存在项目里


## 二·补充：新建「模组版实体」对话框各项怎么填

| 选项 | 建议填法 | 说明 |
|---|---|---|
| 格式 | 模组版实体 | 已经是正确格式 |
| 文件名 | `Scp173` | 导出的模型类名/文件名，建议与实体类一致 |
| 模型标识符 | `scpmod:scp173` | 用作 ModelLayerLocation 的 id（格式 `命名空间:路径`） |
| 实体层级 | `main`（若不可改就保持 `Entity`） | 图层名，代码里 `new ModelLayerLocation(id, layer)` 要一致 |
| 导出版本 | **选 `Forge 1.17+ (Mojmaps)`**，**不要选 `Fabric 1.17+ (Yarn)`** | 26.x 是不混淆版本，类名是官方名（Mojmaps 风格：`LayerDefinition` / `PartDefinition` / `MeshDefinition`）；Yarn 是旧 Fabric 映射体系，26.x 已不用，选它导出会全是 `TexturedModelData` / `ModelPartBuilder` 之类对不上的名字 |
| Y轴翻转 | 先保持默认（勾选） | 导出后若模型上下颠倒，再取消勾选重新导出 |
| 纹理分辨率 | `64 × 64`（模型细节多可 `128 × 128`） | 与贴图 PNG 尺寸保持一致 |

> 26.2 的模型 API 与 1.17+ 导出的类可能有差异（如 `createBodyLayer()` 返回 `LayerDefinition`、`PartDefinition` 替代旧 `ModelPartBuilder`），接入时按真实 jar 核对修正即可。
## 二、导出到项目里

1. **File → Export → Java Entity**（模组版实体项目导出的就是 Java 实体模型类）：会得到**一个 .java 模型类**（包含 `createBodyLayer()`），以及一张贴图 PNG
2. 把模型类放到：

```
src/client/java/com/scpmod/client/model/Scp173Model.java
```

3. 贴图放到：

```
src/main/resources/assets/scpmod/textures/entity/scp173.png
```

4. 把模型类里的 `LayerDefinition.create(...)` 改成在 `createBodyLayer()` 里返回，并按需调整：
   - `MeshDefinition` / `PartDefinition` 的坐标单位是 **1/16 格**
   - 模型整体缩放可以在渲染器里用 `poseStack.scale(...)` 调，不必改模型

## 三、代码接入（Minecraft 26.2 / Fabric）

26.2 的渲染是 **EntityRenderState** 体系，分三步：

1. **注册模型层**（client 初始化里）：

```java
ModelLayerLocation LAYER = new ModelLayerLocation(
    Identifier.fromNamespaceAndPath("scpmod", "scp173"), "main");
EntityModelLayerRegistry.registerModelLayer(LAYER, Scp173Model::createBodyLayer);
```

2. **实体渲染器**（26.2 需要自定义 RenderState）：

```java
public class Scp173Renderer extends EntityRenderer<Scp173Entity, Scp173RenderState> {
    private final Scp173Model model;
    // 构造函数里用 EntityRendererProvider.Context.bakeLayer(LAYER) 拿模型
    // 重写 createRenderState / extractRenderState / submit
    // submit 里用 model.renderToBuffer(...) 或 Model.render(...) 画到 PoseStack
}
```

3. **注册渲染器**（客户端入口）：

```java
EntityRenderers.register(ModEntities.SCP_173, Scp173Renderer::new);
```

> 这些渲染 API 名字在 26.2 有变化（RenderState 是新增的），我会在接代码时用 `javap` 对照真实 jar 核对一遍，不凭记忆写。

## 四、服务端实体本身

目前 173 的行为全是服务端驱动（`AnomalyController`），身体是 ArmorStand。换成真实体时：

1. 新建 `Scp173Entity extends Monster`（或 `PathfinderMob`，可复用原版寻路 AI），注册 `EntityType` + 属性
2. `AnomalyController` 从 ArmorStand 改成 `spawn(level, pos)` 生成 `Scp173Entity`
3. 观察冻结/瞬移贴脸/断颈伤害的逻辑保持不动（它们只依赖「实体 + 玩家」坐标）
4. 寻路可以交给原版 `PathNavigation`，比现在的 BFS 更平滑

## 五、常见坑

| 现象 | 原因 |
|---|---|
| 模型全白 / 紫黑 | 贴图路径或命名空间不对；贴图必须在 `textures/entity/` 下 |
| 模型位置偏移 | 原点没放在脚底中心；单位是 1/16 格 |
| 模型倒立 / 朝向反了 | Minecraft 实体朝 +Z，Blockbench 建模方向要和它一致 |
| 实体不可见但能被打 | 忘了注册渲染器，或渲染器里没调模型渲染 |
| 崩溃 NoSuchMethodError | Blockbench 导出的是旧版 API 的模型类，需要按当前版本改 `createBodyLayer` 签名 |
| 新建菜单找不到 Java Entity | 选 **模组版实体（Modded Entity）** 就对了 |
