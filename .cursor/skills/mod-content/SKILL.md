---
name: mod-content
description: >-
  Adds DontStarveMC blocks, items, entities, and client assets, and locates
  Minecraft, NeoForge, and GeckoLib sources on disk. Use when writing or
  registering blocks, items, foods, entities, block entities, renderers, events,
  mixins, models, textures, blockstates, items/*.json, loot, or lang, and when
  looking up Minecraft, NeoForge, GeckoLib, registries, events, or rendering.
  Read this skill before those edits or API answers.
---

# 模组内容与本地源

固定入口：`.cursor/skills/mod-content/SKILL.md`（相对仓库根）。`.cursor/rules/mod-content.mdc` 始终指向这里。本地源查找只使用本技能，不使用写死版本号的 JAR 文件名，也不跟随仓库外技能路径。

## 本地源

先读 `gradle.properties` 的 `minecraft_version`、`neo_version`、`geckolib_version`。再用这些值定位文件，步骤和坐标见 [reference-sources.md](reference-sources.md)。

查类、注册、事件、渲染时，先读本地 JAR。合并包缺失时先跑会生成 moddev artifacts 的 Gradle 任务。不要把网上教程当作这些 API 的第一来源。

床的资源格式也从合并 JAR 里的原版 `white_bed` 读取，见下文。

## 写代码

沿现有注册脊柱加内容。`TheConstant` 里的 `DeferredRegister` 已经挂上模组总线，并用 `Class.forName` 装载 `ModBlocks`、`ModItems`、`ModEntities`、`ModBlockEntities`、`ModItemGroups`、`ModFeatures`。新内容写在这些类的静态字段或已有 `register` 方法里。

参照顺序，停在第一份够用的代码上：

1. 本仓库里最接近的类（注册方式、包名、创造栏）。
2. 合并 JAR 里的原版类（能继承就继承，方法签名与方块属性跟 26.1 对齐）。床读 `BedBlock` 与 `white_bed` 资源，不读 `pigman_bed`。
3. GeckoLib 源码只在写 `GeoEntityRenderer` 时打开。注册仍跟 `TheConstantClient.onRegisterRenderers`。

写之前先定种类，再只改 [reference-code.md](reference-code.md) 里该种类的现有类。客户端渲染、模型层、方块染色只进 `TheConstantClient` 的现有监听。带 `Dist.CLIENT` 的类型不能出现在服务端会加载的类里。

有物品形态的内容加入 `ModItemGroups.buildTabs()` 的对应列表。掉落、配方、标签沿 `src/main/resources/data/dontstravemc/` 里已有的手写 JSON。语言写入 `lang/en_us.json` 和 `lang/zh_cn.json`。

改原版行为时先在 NeoForge 源码里找事件，监听器使用 `@EventBusSubscriber(modid = TheConstant.MODID)`，放在已有的 `event`、`compat` 或功能包。Mixin 仅在事件无法卡进同一次调用顺序时使用：类放在 `com.theconstant.dontstravemc.mixin`，登记进 `dontstravemc.mixins.json`（客户端放 `client`），并在 `mixin/package-info.java` 写明原因。

不要新建 `DeferredRegister`，不要往 `TheConstant` 构造器加注册，不要把新 id 加进 `ModModelsProvider`、语言 Provider 或 `ModBlockLootTableProvider`，也不要靠跑数据生成补模型或语言。这些 Provider 与手写资源不是同一条路径。

## 添加方块或物品

命名空间是 `dontstravemc`。注册名与文件名相同。资源手写在 `src/main/resources`。同一个 id 不要再交给 `ModModelsProvider`。

模组贴图路径必须带 `dontstravemc:`。没有命名空间的 `block/`、`item/` 会解析到 `minecraft`。

已注册的物品（含 `registerBlock` 生成的方块物品，以及 `ModItems` 里另注册的可放置物品）必须有：

`assets/dontstravemc/items/<id>.json`

它指向实际模型。只写 `models/item` 时，背包和手持仍是缺失模型。

`registerBlockWithoutItem` 且没有同名物品时，只写方块状态、方块模型和方块贴图。

显示名写入 `assets/dontstravemc/lang/en_us.json` 和 `lang/zh_cn.json`。

`registerBlock` 会同时注册同名 `BlockItem`。这种方块要写两对，并且用同一个显示名：

- `block.dontstravemc.<id>`
- `item.dontstravemc.<id>`

背包、手持和创造栏读的是 `item.`。只写 `block.` 时，物品名会显示成未翻译的键。`registerBlockWithoutItem` 且没有同名物品时，只写 `block.`。

按种类整套复制本仓库参照，再改 id 和贴图。文件清单在 [reference-assets.md](reference-assets.md)。楼梯、门、落叶、花坛、花盆是多文件：参照物里有的相对路径，新 id 下也要有。

## 床

床只对照合并 JAR 中的原版文件：

- `assets/minecraft/blockstates/white_bed.json`
- `assets/minecraft/items/white_bed.json`
- `assets/minecraft/models/item/white_bed.json`
- `assets/minecraft/textures/entity/bed/white.png`

不要读取或复制 `pigman_bed` 的 JSON、贴图或 mixin。`texture` 写精灵短名（原版是 `minecraft:white`），由 `Sheets.BED_MAPPER`（前缀 `entity/bed`）对应 `textures/entity/bed/<短名>.png`。物品模型只继承 `minecraft:item/template_bed` 并写 `particle`。

## GeckoLib

实体和盔甲使用 `assets/dontstravemc/geckolib/models/` 与 `geckolib/animations/`。贴图路径与渲染器里的 `Identifier` 一致。不要再配一套方块 cube 模型，除非这个方块本身仍需要 blockstate。
