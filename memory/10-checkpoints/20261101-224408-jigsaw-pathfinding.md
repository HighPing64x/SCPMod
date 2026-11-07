# Checkpoint 20261101-224408 — Jigsaw 完成 & 穿墙/视场角修复

## Jigsaw 验证证据
- [selftest] start type=StructureType$$Lambda valid=true box=[-476,-444 , 51,80 , 1004,1036]
- [selftest] scanned=32670 stone_bricks=196 containment_units=1 terminals=1
- 无 jigsaw/template 加载错误

## 行为修复
- observationFovDegrees 默认 100（含运行目录配置）
- 攻击前 LOS 射线检查（禁止隔墙击杀）
- 移动：MoverType.SELF 碰撞 + 0.5 格子步，不再 teleport 穿墙
- 直行受阻时局部 BFS（半径 12）绕路，路径 10 tick 重算

## Git
- 按用户要求未提交；待用户自行 `git add -A && git commit`
