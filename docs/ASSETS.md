# 如何给 SCPMod 添加材质与模型

所有资源都在 `src/main/resources/assets/scpmod/` 下，**目录名、文件名必须和注册名（id）一致**，代码里不需要为贴图改任何东西。

## 目录速查

```
src/main/resources/assets/scpmod/
├─ blockstates/<方块id>.json      # 方块状态 -> 模型（必填，方块才能渲染）
├─ models/block/<模型名>.json      # 方块几何模型（cube_all / 自定义 JSON）
├─ models/item/<物品id>.json       # 物品模型（生成物品用 item/generated）
├─ textures/block/<贴图>.png      # 方块贴图（16×16，可带 alpha）
├─ textures/item/<贴图>.png       # 物品贴图（16×16）
├─ textures/entity/<贴图>.png     # 实体贴图（自定义渲染器时才需要）
└─ lang/en_us.json, zh_cn.json    # 名称翻译
```

## 1. 给现有方块换材质（收容单元 / 操作终端）

目前它们用的是原版贴图占位：

- `models/block/containment_unit.json` → `"all": "minecraft:block/iron_block"`
- `models/block/terminal.json` → `"all": "minecraft:block/observer_front"`

换成自己的贴图：

1. 把 `containment_unit.png` 放进 `textures/block/`（16×16 PNG，透明背景也可以）
2. 改 `models/block/containment_unit.json`：

```json
{
  "parent": "minecraft:block/cube_all",
  "textures": { "all": "scpmod:block/containment_unit" }
}
```

3. 游戏里按 **F3 + T** 重载资源（或重启客户端）——不需要重新构建

## 2. 添加一个**新方块**（完整流程）

以「收容闸门 `blast_door`」为例：

1. **代码**：在 `content/ModBlocks.java` 里注册（构造前必须 `setId`，这是 26.2 的坑）
2. **blockstate**：`assets/scpmod/blockstates/blast_door.json`

```json
{ "variants": { "": { "model": "scpmod:block/blast_door" } } }
```

3. **方块模型**：`assets/scpmod/models/block/blast_door.json`

```json
{
  "parent": "minecraft:block/cube_all",
  "textures": { "all": "scpmod:block/blast_door" }
}
```

4. **贴图**：`assets/scpmod/textures/block/blast_door.png`（16×16）
5. **物品模型**：`assets/scpmod/models/item/blast_door.json`

```json
{ "parent": "scpmod:block/blast_door" }
```

6. **物品栏**：在 `content/ModCreativeTabs.java` 的 `BLOCKS` 页签里 `output.accept(...)`
7. **语言**：`lang/zh_cn.json` 加 `"block.scpmod.blast_door": "收容闸门"`

## 3. 添加**新物品**（非方块）

1. 代码里注册 Item（参考 `Scp173SpawnerItem`）
2. 物品模型 `models/item/<id>.json`：

```json
{ "parent": "minecraft:item/generated", "textures": { "layer0": "scpmod:item/<id>" } }
```

3. 贴图 `textures/item/<id>.png`（16×16）
4. 语言 `item.scpmod.<id>`

## 4. 用 Blockbench 做模型

1. 建模完成后 **File → Export → Java Block/Item Model**，得到 JSON
2. 把 JSON 放到 `models/block/<id>.json`，**贴图 PNG** 放到 `textures/block/<id>.png`
3. JSON 里 `textures` 的路径改成 `scpmod:block/<id>`（Blockbench 默认会写 `block/<id>` 或 `minecraft:...`，注意命名空间）
4. 方块不是满方块时要在 `blockstates` 里给变体（或直接用 `variants { "": {...} }`），并在代码的 `Properties` 上按需加 `.noOcclusion()`

## 5. 实体（SCP）模型

目前 SCP-173 用 ArmorStand + 石方块做占位，**不需要模型文件**。
等做真正的自定义实体时：

- 模型 JSON（Blockbench 导出 Entity 模型）放 `models/entity/...`
- 贴图放 `textures/entity/...`
- 需要写客户端渲染器（`EntityRenderer` + `EntityModel`）并在 client 入口注册

## 6. 常见坑

| 现象 | 原因 |
|---|---|
| 紫黑格 / 贴图丢失 | 路径或大小写不对；必须是 `assets/scpmod/textures/block/xxx.png` 且 id 全小写 |
| 物品是 2D 紫黑方块 | 缺少 `models/item/<id>.json` |
| 方块不可见 / 透明异常 | 非满方块模型需要在 Properties 加 `.noOcclusion()` |
| 改了贴图没变化 | 客户端缓存：F3+T 重载；仍不行再重启 runClient |
| 纹理模糊 | 用 16×16（或 16 的倍数）PNG，不要放大图片 |
