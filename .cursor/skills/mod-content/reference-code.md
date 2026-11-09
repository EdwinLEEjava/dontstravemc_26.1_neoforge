# 代码落点

新内容只改下表里的现有类。`TheConstant.bootstrapModContent()` 已经调用各 `register` 方法。

| 种类 | 新类放在 | 必须修改的现有类 | 本仓库样本 | 原版对照 |
|---|---|---|---|---|
| 无行为方块 | 不建类，`Block::new` | `ModBlocks.registerBlock`，`ModItemGroups.buildTabs` | `TURF_SAND` | 同材质的原版方块属性 |
| 有行为方块 | `block/custom` | `ModBlocks` 三个 `registerBlock*` 之一，`ModItemGroups` | 最接近的 `block/custom` 类 | 合并 JAR 中被继承的方块类 |
| 方块与物品分开 | 物品在 `item/custom` 或 `dsfood` | `ModBlocks.registerBlockWithoutItem`，`ModItems` | 蘑菇：`RED_MUSHROOM` | 原版对应物品 |
| 普通物品 | 无额外行为则只注册；有行为放 `item/custom` | `ModItems`（`itemProps(id)`），`ModItemGroups` | 同栏已有物品 | 原版物品类 |
| 食物 | `Dsfoods.FoodConfig` | `ModItems` 用 `createItem` 或 `createPlaceableItem` | `MONSTER_MEAT_FOOD` | `FoodProperties` |
| 实体 | `entity/animal`、`monster`、`neutral`、`ambient`、`trap` | `ModEntities`（`ResourceKey` + `DeferredHolder`），`ModRegistryEvents.onEntityAttributes`，`TheConstantClient.onRegisterRenderers` | 同目录实体 | 同类别原版实体的属性与目标 |
| 方块实体 | `block/blockentity/custom` | `ModBlockEntities`。挂上原版方块实体类型时用 `BlockEntityTypeAddBlocksEvent` | `ModRegistryEvents.onBedBlockEntityBlocks` | 原版方块实体类 |
| 燃料、堆肥、命令、生成监听 | 不新建注册器 | 已有类的 `register` 方法 | `ModFuelItem`、`ModCompostables`、`ModCommands`、`SpawnListener` | 对应原版注册表 |
| 世界生成 | `world/gen` 与 `data/dontstravemc/worldgen` | 跟现有 configured / placed feature | 已有 biome JSON | 原版 feature JSON |
| 染色 | `render`，提供 `register` | `TheConstantClient.onRegisterBlockColors` | `ConstantGrassColors` | 原版 `BlockTintSource` |
| 横切交互 | 已有 `event`、`compat` 或功能包 | `@EventBusSubscriber(modid = TheConstant.MODID)` | `ModNeoForgeEventBridge` | NeoForge 事件类 |

创造栏列表在 `ModItemGroups`：`TAB_BLOCKS`、`TAB_NATURE`、`TAB_FOODS`、`TAB_TOOLS`、`TAB_MATERIALS`、`TAB_WEAPONS`、`TAB_CLOTH`。无物品方块不要加入。有物品的方块，语言同时写 `block.dontstravemc.<id>` 和 `item.dontstravemc.<id>`，显示名相同。

## 实体还要写的文件

- `data/dontstravemc/loot_table/entities/<id>.json`
- 需要刷怪蛋时在 `ModItems` 注册，并放进创造栏
- GeckoLib：`geckolib/models` 与 `geckolib/animations`，贴图路径与渲染器 `Identifier` 一致。渲染器注册仍在 `TheConstantClient.onRegisterRenderers`

## 数据与生成器

掉落、配方、标签复制 `src/main/resources/data/dontstravemc/` 下同目录的现有 JSON。

`ModModelsProvider`、`ModEnUsLangProvider`、`ModZhCnLangProvider`、`ModBlockLootTableProvider` 不覆盖现有手写资源。新 id 不要加进这些类。`ModBlockLootTableProvider.getKnownBlocks()` 声明了全部方块，但 `generate()` 只写了蓝蘑菇，跑数据生成不能用来补新方块。

## Mixin

先查 NeoForge 事件。确认事件无法保持同一次调用顺序后：

- 类放在 `com.theconstant.dontstravemc.mixin`（客户端在 `mixin.client`）
- 登记 `src/main/resources/dontstravemc.mixins.json` 的 `mixins` 或 `client`
- 在 `mixin/package-info.java` 写下保留原因

已有保留说明见该 `package-info.java`（护甲扣耐久、耐久条）。
