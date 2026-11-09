package com.theconstant.dontstravemc.block;

import com.theconstant.dontstravemc.TheConstant;

import com.theconstant.dontstravemc.block.custom.*;
import com.theconstant.dontstravemc.block.custom.SaplingBlock;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.awt.*;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ModBlocks {


    public static final DeferredBlock<Block> RADIO = registerBlock("radio",
            RadioBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(1.0f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .noOcclusion()
    );

    public static final DeferredBlock<Block> ICE_BOX = registerBlock("ice_box",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SNOW)
                    .strength(2.0f, 6.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
    );

    public static final DeferredBlock<Block> STICKY_WEBBING = registerBlockWithoutItem("sticky_webbing",
            StickyWebbingBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOL)
                    .replaceable()
                    .noCollision()
                    .strength(0.5f)
                    .sound(SoundType.SLIME_BLOCK)
                    .noOcclusion()
                    .ignitedByLava()
    );

    public static final DeferredBlock<Block> SAPLING = registerBlock("sapling",
            SaplingBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .replaceable()
                    .noCollision()
                    .strength(0.0f)
                    .sound(SoundType.GRASS)
                    .noOcclusion()
    );
    public static final DeferredBlock<Block> SAPLING_WAXED = registerBlock("sapling_waxed",
            WaxedSaplingBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .replaceable()
                    .noCollision()
                    .strength(0.0f)
                    .sound(SoundType.GRASS)
                    .noOcclusion()
    );

    public static final DeferredBlock<Block> MARSH_TURF = registerBlock("marsh_turf",
            ForestTurfBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(0.6f)
                    .sound(SoundType.MUD)
                    .randomTicks()
    );
    public static final DeferredBlock<Block> TURF_DESERT = registerBlock("turf_desert",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SAND)
                    .strength(0.6f)
                    .sound(SoundType.SAND)
    );

    public static final DeferredBlock<Block> TURF_ROCKY = registerBlock("turf_rocky",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(0.6f)
                    .sound(SoundType.STONE)
    );

    public static final DeferredBlock<Block> TURF_CAVE_ROCKY = registerBlock("turf_cave_rocky",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(0.6f)
                    .sound(SoundType.STONE)
    );

    public static final DeferredBlock<Block> TURF_MUD = registerBlock("turf_mud",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(0.6f)
                    .sound(SoundType.MUD)
    );

    public static final DeferredBlock<Block> TURF_SAND = registerBlock("turf_sand",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SAND)
                    .strength(0.6f)
                    .sound(SoundType.SAND)
    );

    public static final DeferredBlock<Block> UNDERROCK = registerBlock("underrock",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(1.5f, 6.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
    );

    /** 阴影裂隙里替换地形用的全黑方块。 */
    public static final DeferredBlock<Block> SHADOW_BLOCK = registerBlock("shadow_block",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(1.5f, 6.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<Block> TWIG = registerBlockWithoutItem("twig",
            TwigBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .replaceable()
                    .noCollision()
                    .strength(0.0f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    );
    public static final DeferredBlock<Block> GRASS = registerBlock("grass",
            DsGrassBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .replaceable()
                    .noCollision()
                    .strength(0.0f)
                    .sound(SoundType.GRASS)
                    .noOcclusion()
    );
    public static final DeferredBlock<Block> GRASS_WAXED = registerBlock("grass_waxed",
            WaxedGrassBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .replaceable()
                    .noCollision()
                    .strength(0.0f)
                    .sound(SoundType.GRASS)
                    .noOcclusion()
    );
    public static final DeferredBlock<Block> EVERGREEN_LOG = registerBlock("evergreen_log",
            RotatedPillarBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PODZOL)
                    .strength(2.0f)
                    .sound(SoundType.WOOD)
    );

    public static final DeferredBlock<Block> EVERGREEN_WOOD = registerBlock("evergreen_wood",
            RotatedPillarBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PODZOL)
                    .strength(2.0f)
                    .sound(SoundType.WOOD)
    );

    public static final DeferredBlock<Block> STRIPPED_EVERGREEN_LOG = registerBlock("stripped_evergreen_log",
            RotatedPillarBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PODZOL)
                    .strength(2.0f)
                    .sound(SoundType.WOOD)
    );

    public static final DeferredBlock<Block> STRIPPED_EVERGREEN_WOOD = registerBlock("stripped_evergreen_wood",
            RotatedPillarBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PODZOL)
                    .strength(2.0f)
                    .sound(SoundType.WOOD)
    );

    public static final DeferredBlock<Block> EVERGREEN_PLANKS = registerBlock("evergreen_planks",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PODZOL)
                    .strength(2.0f, 3.0f)
                    .sound(SoundType.WOOD)
    );

    public static final DeferredBlock<Block> EVERGREEN_STAIRS = registerBlock("evergreen_stairs",
            p -> new StairBlock(ModBlocks.EVERGREEN_PLANKS.get().defaultBlockState(), p),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PODZOL)
                    .strength(2.0f, 3.0f)
                    .sound(SoundType.WOOD)
    );

    public static final DeferredBlock<Block> EVERGREEN_SLAB = registerBlock("evergreen_slab",
            SlabBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PODZOL)
                    .strength(2.0f, 3.0f)
                    .sound(SoundType.WOOD)
    );

    public static final DeferredBlock<Block> EVERGREEN_FENCE = registerBlock("evergreen_fence",
            FenceBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PODZOL)
                    .strength(2.0f, 3.0f)
                    .sound(SoundType.WOOD)
    );

    public static final DeferredBlock<Block> EVERGREEN_FENCE_GATE = registerBlock("evergreen_fence_gate",
            p -> new FenceGateBlock(WoodType.SPRUCE, p),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PODZOL)
                    .strength(2.0f, 3.0f)
                    .sound(SoundType.WOOD)
    );

    public static final DeferredBlock<Block> EVERGREEN_PRESSURE_PLATE = registerBlock("evergreen_pressure_plate",
            p -> new PressurePlateBlock(BlockSetType.SPRUCE, p),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PODZOL)
                    .forceSolidOn()
                    .noCollision()
                    .strength(0.5f)
                    .sound(SoundType.WOOD)
    );

    public static final DeferredBlock<Block> EVERGREEN_BUTTON = registerBlock("evergreen_button",
            p -> new ButtonBlock(BlockSetType.SPRUCE, 30, p),
            BlockBehaviour.Properties.of()
                    .noCollision()
                    .strength(0.5f)
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
    );

    public static final DeferredBlock<Block> EVERGREEN_DOOR = registerBlock("evergreen_door",
            p -> new DoorBlock(BlockSetType.SPRUCE, p),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PODZOL)
                    .strength(3.0f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
    );

    public static final DeferredBlock<Block> EVERGREEN_TRAPDOOR = registerBlock("evergreen_trapdoor",
            p -> new TrapDoorBlock(BlockSetType.SPRUCE, p),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PODZOL)
                    .strength(3.0f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .isValidSpawn((state, level, pos, entityType) -> false)
    );

    public static final DeferredBlock<Block> PIGMAN_BED = registerBlock("pigman_bed",
            PigmanBedBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(0.2f)
                    .sound(SoundType.WOOD)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .ignitedByLava()
    );

    public static final DeferredBlock<Block> LANTERN = registerBlock("lantern",
            ProximityLanternBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(3.5f)
                    .sound(SoundType.LANTERN)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)
                    .lightLevel(state -> state.getValue(ProximityLanternBlock.LIT) ? 10 : 0)
    );

    public static final DeferredBlock<Block> EVERGREEN_LEAVES = registerBlock("evergreen_leaves",
            p -> new TintedParticleLeavesBlock(0.01f, p),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .strength(0.2f)
                    .randomTicks()
                    .sound(SoundType.GRASS)
                    .noOcclusion()
                    .isValidSpawn((state, level, pos, entityType) -> false)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)
    );

    /** 永恒维度桦木：叶色由 {@link com.theconstant.dontstravemc.block.custom.ConstantBirchLeavesBlock.BirchAutumnHue} 决定，世界生成一树单色。 */
    public static final DeferredBlock<Block> CONSTANT_BIRCH_LEAVES = registerBlock("constant_birch_leaves",
            p -> new ConstantBirchLeavesBlock(0.01F, p),
            BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_LEAVES)
    );


    public static final DeferredBlock<Block> CONSTANT_BIRCH_TREE_LOG = registerBlock("constant_birch_tree_log",
            RotatedPillarBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_LOG)
    );

    public static final DeferredBlock<Block> BERRY_BUSH = registerBlock("berry_bush",
            BerryBushBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .strength(0.0f)
                    .sound(SoundType.GRASS)
                    .noOcclusion()
    );

    public static final DeferredBlock<Block> BERRY_BUSH_2 = registerBlock("berry_bush_2",
            BerryBush2Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .strength(0.0f)
                    .sound(SoundType.GRASS)
                    .noOcclusion()
    );
    public static final DeferredBlock<Block> BERRY_BUSH_WAXED = registerBlock("berry_bush_waxed",
            WaxedBerryBushBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .strength(0.0f)
                    .sound(SoundType.GRASS)
                    .noOcclusion()
    );
    public static final DeferredBlock<Block> BERRY_BUSH_2_WAXED = registerBlock("berry_bush_2_waxed",
            WaxedBerryBush2Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .strength(0.0f)
                    .sound(SoundType.GRASS)
                    .noOcclusion()
    );
    public static final DeferredBlock<Block> EVERGREEN_SAPLING = registerBlock("evergreen_sapling",
            EvergreenSaplingBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .randomTicks()
                    .strength(0.0f)
                    .sound(SoundType.GRASS)
                    .noOcclusion()
    );

    public static final DeferredBlock<Block> FLINT = registerBlockWithoutItem("flint",
            FlintBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .noCollision()
                    .strength(0.0f)
                    .sound(SoundType.STONE)
    );

    public static final DeferredBlock<Block> BUTTER_BLOCK = registerBlockWithoutItem("butter_block",
            ButterBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(0.2f)
                    .sound(SoundType.HONEY_BLOCK)
                    .noOcclusion()
    );

    public static final DeferredBlock<Block> CAMOMILE = registerBlock("camomile",
            properties -> new DsFlowerBlock(MobEffects.FIRE_RESISTANCE, 0.35F, properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY)
    );
    public static final DeferredBlock<Block> BLUE_EYED_DAISY = registerBlock("blue_eyed_daisy",
            properties -> new DsFlowerBlock(MobEffects.POISON, 0.5F, properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY)
    );
    public static final DeferredBlock<Block> CORYDALIS = registerBlock("corydalis",
            properties -> new DsFlowerBlock(MobEffects.REGENERATION, 0.4F, properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY)
    );
    public static final DeferredBlock<Block> DAYLILIES = registerBlock("daylilies",
            properties -> new DsFlowerBlock(MobEffects.FIRE_RESISTANCE, 0.35F, properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY)
    );
    public static final DeferredBlock<Block> GARDENIA = registerBlock("gardenia",
            properties -> new DsFlowerBlock(MobEffects.HEALTH_BOOST, 0.2F, properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY)
    );
    public static final DeferredBlock<Block> PINWHEEL_FLOWER = registerBlock("pinwheel_flower",
            properties -> new DsFlowerBlock(MobEffects.INSTANT_HEALTH, 0.25F, properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY)
    );
    public static final DeferredBlock<Block> RED_HIBISCUS = registerBlock("red_hibiscus",
            properties -> new DsFlowerBlock(MobEffects.FIRE_RESISTANCE, 0.25F, properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY)
    );
    public static final DeferredBlock<Block> TULIP_VIOLET = registerBlock("tulip_violet",
            properties -> new DsFlowerBlock(MobEffects.NAUSEA, 0.7F, properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY)
    );
    public static final DeferredBlock<Block> UMBELLED_ROCK_JASMINE = registerBlock("umbelled_rock_jasmine",
            DsFlowerBedBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.PINK_PETALS)
                    .pushReaction(PushReaction.DESTROY)
    );
    public static final DeferredBlock<Block> PERIWINKLE = registerBlock("periwinkle",
            properties -> new DsFlowerBlock(MobEffects.INSTANT_HEALTH, 0.25F, properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY)
    );
    public static final DeferredBlock<Block> ROSE = registerBlock("rose",
            properties -> new RoseFlowerBlock(MobEffects.LUCK, 0.5F, properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY)
    );
    public static final DeferredBlock<Block> EVIL_FLOWER_0 = registerBlock("evil_flower_0",
            properties -> new EvilFlowerBlock(MobEffects.DARKNESS, 0.6F, properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY)
    );
    public static final DeferredBlock<Block> EVIL_FLOWER_1 = registerBlock("evil_flower_1",
            properties -> new EvilFlowerBlock(MobEffects.WEAKNESS, 0.8F, properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY)
    );
    public static final DeferredBlock<Block> EVIL_FLOWER_2 = registerBlock("evil_flower_2",
            properties -> new EvilFlowerBlock(MobEffects.MINING_FATIGUE, 0.7F, properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY)
    );
    public static final DeferredBlock<Block> EVIL_FLOWER_3 = registerBlock("evil_flower_3",
            properties -> new EvilFlowerBlock(MobEffects.SLOWNESS, 0.75F, properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY)
    );
    public static final DeferredBlock<Block> EVIL_FLOWER_4 = registerBlock("evil_flower_4",
            properties -> new EvilFlowerBlock(MobEffects.BLINDNESS, 0.7F, properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY)
    );
    public static final DeferredBlock<Block> EVIL_FLOWER_5 = registerBlock("evil_flower_5",
            properties -> new EvilFlowerBlock(MobEffects.BAD_OMEN, 0.6F, properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY)
    );
    public static final DeferredBlock<Block> EVIL_FLOWER_6 = registerBlock("evil_flower_6",
            properties -> new EvilFlowerBlock(MobEffects.POISON, 0.75F, properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY)
    );
    public static final DeferredBlock<Block> EVIL_FLOWER_7 = registerBlock("evil_flower_7",
            properties -> new EvilFlowerBlock(MobEffects.WITHER, 0.7F, properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY)
    );

    public static final List<DeferredBlock<Block>> POTTED_FLOWERS = List.of(
            registerPotted("camomile", CAMOMILE),
            registerPotted("blue_eyed_daisy", BLUE_EYED_DAISY),
            registerPotted("corydalis", CORYDALIS),
            registerPotted("daylilies", DAYLILIES),
            registerPotted("gardenia", GARDENIA),
            registerPotted("pinwheel_flower", PINWHEEL_FLOWER),
            registerPotted("red_hibiscus", RED_HIBISCUS),
            registerPotted("tulip_violet", TULIP_VIOLET),
            registerPotted("periwinkle", PERIWINKLE),
            registerPotted("rose", ROSE),
            registerPotted("evil_flower_0", EVIL_FLOWER_0),
            registerPotted("evil_flower_1", EVIL_FLOWER_1),
            registerPotted("evil_flower_2", EVIL_FLOWER_2),
            registerPotted("evil_flower_3", EVIL_FLOWER_3),
            registerPotted("evil_flower_4", EVIL_FLOWER_4),
            registerPotted("evil_flower_5", EVIL_FLOWER_5),
            registerPotted("evil_flower_6", EVIL_FLOWER_6),
            registerPotted("evil_flower_7", EVIL_FLOWER_7)
    );

    public static final DeferredBlock<Block> TOMBSTONE_1 = registerBlock("tombstone_1",
            TombstoneBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(1.5f, 6.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
    );

    public static final DeferredBlock<Block> CONSTANT_PORTAL = registerBlock("constant_portal",
            ConstantPortalBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .noCollision()
                    .strength(0.4f)
                    .sound(SoundType.WOOL)
                    .noOcclusion()
                    .pushReaction(PushReaction.BLOCK));

    public static final DeferredBlock<Block> URN = registerBlock("urn",
            UrnBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(0.5f, 6.0f)
                    .sound(SoundType.DECORATED_POT)
                    .noOcclusion()
    );

    public static final DeferredBlock<Block> CONSTANT_GRASS = registerBlock("constant_grass",
            ConstantGrassBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .replaceable()
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XYZ)
                    .ignitedByLava()
                    .pushReaction(PushReaction.DESTROY)
    );

    public static final DeferredBlock<Block> CONSTANT_GRASS_BLOCK = registerBlock("constant_grass_block",
        GrassBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)
    );

    public static final DeferredBlock<Block> RED_MUSHROOM = registerBlockWithoutItem("red_mushroom",
            (p) -> new DsMushroomBlock(TreeFeatures.HUGE_RED_MUSHROOM, p),BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .noCollision()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY));
    public static final DeferredBlock<Block> GREEN_MUSHROOM = registerBlockWithoutItem("green_mushroom",
            (p) -> new DsMushroomBlock(TreeFeatures.HUGE_RED_MUSHROOM, p),BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .noCollision()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY));
    public static final DeferredBlock<Block> BLUE_MUSHROOM = registerBlockWithoutItem("blue_mushroom",
            (p) -> new DsMushroomBlock(TreeFeatures.HUGE_RED_MUSHROOM, p),BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .noCollision()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<Block> BIRCH_LEAF_LITTER = registerBlock("birch_leaf_litter",
            LeafLitterBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.LEAF_LITTER));

    public static final DeferredBlock<Block> CONSTANT_BIRCH_SAPLING = registerBlock("constant_birch_sapling",
             Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_SAPLING));


    private static DeferredBlock<Block> registerPotted(String idSuffix, DeferredBlock<Block> content) {
        String name = "potted_" + idSuffix;
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(TheConstant.MODID, name));
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT).setId(blockKey);
        return TheConstant.BLOCKS.register(name, () -> new FlowerPotBlock(content.get(), properties));
    }

    private static DeferredBlock<Block> registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(TheConstant.MODID, name));
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(TheConstant.MODID, name));

        properties.setId(blockKey);
        DeferredBlock<Block> deferred = TheConstant.BLOCKS.register(name, () -> factory.apply(properties));
        TheConstant.ITEMS.register(name, () -> new BlockItem(deferred.get(), new Item.Properties().setId(itemKey)));
        return deferred;
    }

    private static DeferredBlock<Block> registerBlockWithCustomItem(String name, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties properties, BiFunction<Block, Item.Properties, Item> itemFactory) {
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(TheConstant.MODID, name));
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(TheConstant.MODID, name));

        properties.setId(blockKey);
        DeferredBlock<Block> deferred = TheConstant.BLOCKS.register(name, () -> blockFactory.apply(properties));
        TheConstant.ITEMS.register(name, () -> itemFactory.apply(deferred.get(), new Item.Properties().setId(itemKey)));
        return deferred;
    }

    private static DeferredBlock<Block> registerBlockWithoutItem(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(TheConstant.MODID, name));

        properties.setId(blockKey);
        return TheConstant.BLOCKS.register(name, () -> factory.apply(properties));
    }

    public static void registerModBlocks() {
        TheConstant.LOGGER.info("Registering Blocks for " + TheConstant.MODID);
    }
}