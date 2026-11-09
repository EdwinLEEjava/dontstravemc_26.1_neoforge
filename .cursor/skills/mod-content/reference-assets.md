# 客户端资源清单

根目录：`src/main/resources/assets/dontstravemc/`。下表路径都相对这个根。复制参照后，把注册名和贴图路径改成新 id。贴图文件本身要放进对应目录；技能不生成图片。

物品定义的稳定写法：

```json
{
  "model": {
    "type": "minecraft:model",
    "model": "dontstravemc:item/<id>"
  }
}
```

染色十字植株在这个 `model` 对象里追加 `tints`（参照 `items/constant_grass.json`）。

| 种类 | 参照 | 要同时存在的文件 |
|---|---|---|
| 平面物品 | `bird_egg` | `textures/item/<id>.png`，`models/item/<id>.json`（父模型 `minecraft:item/generated`，`layer0` 为 `dontstravemc:item/<id>`），`items/<id>.json` |
| 手持工具 | 平面物品那一套 | 同上，父模型改为 `minecraft:item/handheld`。手写与 `ModModelsProvider` 只留一种 |
| 单贴图方块 | `turf_sand` | `textures/block/<id>.png`，`models/block/<id>.json`（`minecraft:block/cube_all`），`blockstates/<id>.json`，`models/item/<id>.json`（父模型指向方块模型），`items/<id>.json` |
| 原木 / 多变体柱 | `constant_birch_tree_log` | 侧面与顶面贴图，每个变体一个 `cube_column` 方块模型，带 `axis` 的 blockstate，物品模型指向其中一个方块模型，`items/<id>.json` |
| 十字植株 | `constant_birch_sapling` | 方块贴图，`models/block` 父模型 `minecraft:block/cross` 且 `render_type` 为 `minecraft:cutout`，blockstate，物品模型 `generated` 的 `layer0` 指向方块贴图，`items/<id>.json` |
| 染色十字 | `constant_grass` | 同十字植株，父模型用 `minecraft:block/tinted_cross`，`items/<id>.json` 写 `tints` |
| 花 + 花盆 | `camomile` 与 `potted_camomile` | 两个注册名各一套。花盆方块模型父级 `minecraft:block/flower_pot_cross` |
| 落叶 / 花坛 | `birch_leaf_litter`、`umbelled_rock_jasmine` | 一个 multipart blockstate，1 到 4 段模型（`template_leaf_litter_*` 或 `flowerbed_*`），一张贴图，物品模型与 `items/<id>.json` |
| 门 | `evergreen_door` | 整份复制：含 facing、half、hinge、open 的 blockstate，以及上下、左右、开合的全部方块模型、物品模型和贴图 |
| 楼梯、台阶、栅栏、活板门、按钮、压力板 | 同名前缀的 `evergreen_*` | 整份复制该方块的 blockstate 和全部 `models/block`，再改贴图 |
| 床 | 合并 JAR 里的 `white_bed` | `blockstates/<id>.json` 指向 `minecraft:block/bed`。`items/<id>.json` 按原版 composite：头、脚的 `base` 都是 `dontstravemc:item/<id>`，`texture` 都是 `dontstravemc:<精灵短名>`。`models/item/<id>.json` 父模型 `minecraft:item/template_bed`，只写 `particle`。贴图为 `textures/entity/bed/<精灵短名>.png`。不要对照 `pigman_bed` |
| Blockbench / 自定义元素 | `radio`、`constant_grass_block` | 方块元素模型（贴图可在 `textures/block/<名字>/`），`render_type`，blockstate，物品模型，`items/<id>.json` |
| 动画贴图 | `constant_grass_animated` | `.png` 与同名 `.mcmeta` 放在一起。模型里的贴图名包含 `_animated` |
| 无物品方块 | `sticky_webbing`、`twig` | 仅 blockstate、方块模型、方块贴图 |
| GeckoLib | `geckolib/models/entity/pigman.geo.json` | `geckolib/models/...geo.json`，`geckolib/animations/...animation.json`，渲染器使用的贴图路径 |

动画贴图的 `.mcmeta` 与 png 同目录，例如 `constant_grass_animated.png.mcmeta` 的 `animation.frametime`。
