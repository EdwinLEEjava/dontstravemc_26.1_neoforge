package com.theconstant.dontstravemc.item;

import com.theconstant.dontstravemc.TheConstant;
import com.theconstant.dontstravemc.block.ModBlocks;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;

public class ModItemGroups {
    public static final List<Item> TAB_ITEMS = new ArrayList<>();
    public static final List<Item> TAB_NATURE  = new ArrayList<>();
    public static final List<Item> TAB_FOODS    = new ArrayList<>();
    public static final List<Item> TAB_TOOLS   = new ArrayList<>();
    public static final List<Item> TAB_BLOCKS  = new ArrayList<>();
    public static final List<Item> TAB_MATERIALS  = new ArrayList<>();
    public static final List<Item> TAB_WEAPONS  = new ArrayList<>();
    public static final List<Item> TAB_CLOTH = new ArrayList<>();

    public static void buildTabs() {
        TAB_BLOCKS.add(ModBlocks.EVERGREEN_PLANKS.asItem());
        TAB_BLOCKS.add(ModBlocks.EVERGREEN_WOOD.asItem());
        TAB_BLOCKS.add(ModBlocks.EVERGREEN_LOG.asItem());
        TAB_BLOCKS.add(ModBlocks.STRIPPED_EVERGREEN_WOOD.asItem());
        TAB_BLOCKS.add(ModBlocks.STRIPPED_EVERGREEN_LOG.asItem());
        TAB_BLOCKS.add(ModBlocks.EVERGREEN_STAIRS.asItem());
        TAB_BLOCKS.add(ModBlocks.EVERGREEN_SLAB.asItem());
        TAB_BLOCKS.add(ModBlocks.EVERGREEN_DOOR.asItem());
        TAB_BLOCKS.add(ModBlocks.EVERGREEN_TRAPDOOR.asItem());
        TAB_BLOCKS.add(ModBlocks.EVERGREEN_FENCE.asItem());
        TAB_BLOCKS.add(ModBlocks.EVERGREEN_FENCE_GATE.asItem());
        TAB_BLOCKS.add(ModBlocks.EVERGREEN_BUTTON.asItem());
        TAB_BLOCKS.add(ModBlocks.EVERGREEN_PRESSURE_PLATE.asItem());
        TAB_BLOCKS.add(ModBlocks.CONSTANT_BIRCH_TREE_LOG.asItem());
        TAB_BLOCKS.add(ModBlocks.RADIO.asItem());
        TAB_BLOCKS.add(ModBlocks.ICE_BOX.asItem());
        TAB_BLOCKS.add(ModBlocks.LANTERN.asItem());
        TAB_BLOCKS.add(ModBlocks.TOMBSTONE_1.asItem());
        TAB_BLOCKS.add(ModBlocks.URN.asItem());
        TAB_BLOCKS.add(ModBlocks.CONSTANT_PORTAL.asItem());
        TAB_BLOCKS.add(ModBlocks.PIGMAN_BED.asItem());

        TAB_NATURE.add(ModBlocks.CONSTANT_GRASS_BLOCK.asItem());
        TAB_NATURE.add(ModBlocks.MARSH_TURF.asItem());
        TAB_NATURE.add(ModBlocks.TURF_DESERT.asItem());
        TAB_NATURE.add(ModBlocks.TURF_ROCKY.asItem());
        TAB_NATURE.add(ModBlocks.TURF_CAVE_ROCKY.asItem());
        TAB_NATURE.add(ModBlocks.TURF_MUD.asItem());
        TAB_NATURE.add(ModBlocks.TURF_SAND.asItem());
        TAB_NATURE.add(ModBlocks.UNDERROCK.asItem());
        TAB_NATURE.add(ModBlocks.SHADOW_BLOCK.asItem());
        TAB_NATURE.add(ModBlocks.EVERGREEN_LEAVES.asItem());
        TAB_NATURE.add(ModBlocks.CONSTANT_BIRCH_LEAVES.asItem());
        TAB_NATURE.add(ModBlocks.SAPLING.asItem());
        TAB_NATURE.add(ModBlocks.SAPLING_WAXED.asItem());
        TAB_NATURE.add(ModBlocks.GRASS.asItem());
        TAB_NATURE.add(ModBlocks.GRASS_WAXED.asItem());
        TAB_NATURE.add(ModBlocks.BERRY_BUSH.asItem());
        TAB_NATURE.add(ModBlocks.BERRY_BUSH_2.asItem());
        TAB_NATURE.add(ModBlocks.BERRY_BUSH_WAXED.asItem());
        TAB_NATURE.add(ModBlocks.BERRY_BUSH_2_WAXED.asItem());
        TAB_NATURE.add(ModBlocks.EVERGREEN_SAPLING.asItem());
        TAB_NATURE.add(ModBlocks.CONSTANT_BIRCH_SAPLING.asItem());
        TAB_NATURE.add(ModBlocks.BLUE_EYED_DAISY.asItem());
        TAB_NATURE.add(ModBlocks.CAMOMILE.asItem());
        TAB_NATURE.add(ModBlocks.CORYDALIS.asItem());
        TAB_NATURE.add(ModBlocks.DAYLILIES.asItem());
        TAB_NATURE.add(ModBlocks.GARDENIA.asItem());
        TAB_NATURE.add(ModBlocks.PINWHEEL_FLOWER.asItem());
        TAB_NATURE.add(ModBlocks.RED_HIBISCUS.asItem());
        TAB_NATURE.add(ModBlocks.TULIP_VIOLET.asItem());
        TAB_NATURE.add(ModBlocks.UMBELLED_ROCK_JASMINE.asItem());
        TAB_NATURE.add(ModBlocks.PERIWINKLE.asItem());
        TAB_NATURE.add(ModBlocks.ROSE.asItem());
        TAB_NATURE.add(ModBlocks.EVIL_FLOWER_0.asItem());
        TAB_NATURE.add(ModBlocks.EVIL_FLOWER_1.asItem());
        TAB_NATURE.add(ModBlocks.EVIL_FLOWER_2.asItem());
        TAB_NATURE.add(ModBlocks.EVIL_FLOWER_3.asItem());
        TAB_NATURE.add(ModBlocks.EVIL_FLOWER_4.asItem());
        TAB_NATURE.add(ModBlocks.EVIL_FLOWER_5.asItem());
        TAB_NATURE.add(ModBlocks.EVIL_FLOWER_6.asItem());
        TAB_NATURE.add(ModBlocks.EVIL_FLOWER_7.asItem());
        TAB_NATURE.add(ModBlocks.CONSTANT_GRASS.asItem());
        TAB_NATURE.add(ModItems.RED_MUSHROOM.asItem());
        TAB_NATURE.add(ModItems.GREEN_MUSHROOM.asItem());
        TAB_NATURE.add(ModItems.BLUE_MUSHROOM.asItem());

        TAB_ITEMS.add(ModItems.PINECONE.get());
        TAB_ITEMS.add(ModItems.TRAP.get());
        TAB_ITEMS.add(ModItems.BUTTERFLY_SPAWN_EGG.get());
        TAB_ITEMS.add(ModItems.MONSTER_SPIDER_SPAWN_EGG.get());
        TAB_ITEMS.add(ModItems.DS_RABBIT_SPAWN_EGG.get());
        TAB_ITEMS.add(ModItems.MOLE_WORM_SPAWN_EGG.get());
        TAB_ITEMS.add(ModItems.PIGMAN_SPAWN_EGG.get());
        TAB_ITEMS.add(ModItems.BIRCH_FRUIT_SPRITE_SPAWN_EGG.get());

        TAB_TOOLS.add(ModItems.FLINT_PICKAXE.get());
        TAB_TOOLS.add(ModItems.FLINT_AXE.get());
        TAB_TOOLS.add(ModItems.FLINT_SHOVEL.get());
        TAB_TOOLS.add(ModItems.RAW_GOLDEN_AXE.get());
        TAB_TOOLS.add(ModItems.RAW_GOLDEN_PICKAXE.get());
        TAB_TOOLS.add(ModItems.RAW_GOLDEN_SHOVEL.get());
        TAB_TOOLS.add(ModItems.HAMMER.get());
        TAB_TOOLS.add(ModItems.TORCH.get());

        TAB_CLOTH.add(ModItems.FLOWER_HAT.get());
        TAB_CLOTH.add(ModItems.STRAW_HAT.get());
        TAB_CLOTH.add(ModItems.BATTLE_HELMET.get());
        TAB_CLOTH.add(ModItems.GRASS_ARMOR.get());
        TAB_CLOTH.add(ModItems.WOOD_ARMOR.get());

        TAB_WEAPONS.add(ModItems.FLINT_SPEAR.get());
        TAB_WEAPONS.add(ModItems.HAM_BAT.get());
        TAB_WEAPONS.add(ModItems.MORNING_STAR.get());

        TAB_FOODS.add(ModItems.MEAT.get());
        TAB_FOODS.add(ModItems.MEAT_COOKED.get());
        TAB_FOODS.add(ModItems.MEAT_JERKY.get());
        TAB_FOODS.add(ModItems.MEAT_SMALL.get());
        TAB_FOODS.add(ModItems.COOKED_MEAT_SMALL.get());
        TAB_FOODS.add(ModItems.MEAT_SMALL_JERKY.get());
        TAB_FOODS.add(ModItems.MONSTER_MEAT.get());
        TAB_FOODS.add(ModItems.COOKED_MONSTER_MEAT.get());
        TAB_FOODS.add(ModItems.MONSTER_MEAT_JERKY.get());
        TAB_FOODS.add(ModItems.BIRD_LEG.get());
        TAB_FOODS.add(ModItems.BIRD_LEG_COOKED.get());
        TAB_FOODS.add(ModItems.BIRD_EGG.get());
        TAB_FOODS.add(ModItems.BIRD_EGG_COOKED.get());
        TAB_FOODS.add(ModItems.TALL_BIRD_EGG.get());
        TAB_FOODS.add(ModItems.TALL_BIRD_EGG_COOKED.get());
        TAB_FOODS.add(ModItems.BERRIES.get());
        TAB_FOODS.add(ModItems.COOKED_BERRIES.get());
        TAB_FOODS.add(ModItems.BUTTER.get());
        TAB_FOODS.add(ModItems.BUTTERFLY_WINGS.get());
        TAB_FOODS.add(ModItems.PETALS.get());
        TAB_FOODS.add(ModItems.EVIL_PETALS.get());
        TAB_FOODS.add(ModItems.MONSTER_LASAGNA.get());
        TAB_FOODS.add(ModItems.MEAT_BALL.get());
        TAB_FOODS.add(ModItems.TALL_BIRD_SCOTCH_EGGS.get());
        TAB_FOODS.add(ModItems.SPOILED_FOOD.get());

        TAB_MATERIALS.add(ModItems.CUT_GRASS.get());
        TAB_MATERIALS.add(ModItems.TWIGS.get());
        TAB_MATERIALS.add(ModItems.ROPE.get());
        TAB_MATERIALS.add(ModItems.NIGHTMAREFUEL.get());
        TAB_MATERIALS.add(ModItems.PIG_SKIN.get());

    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> DONTSTRAVE_TAB = TheConstant.CREATIVE_MODE_TABS.register(
            "main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.dontstravemc.main"))
                    .icon(() -> new ItemStack(ModItems.MONSTER_SPIDER_SPAWN_EGG.get()))
                    .displayItems((context, entries) -> TAB_ITEMS.forEach(entries::accept))
                    .build()
    );
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> DONTSTRAVE_FOODS_TAB = TheConstant.CREATIVE_MODE_TABS.register(
            "foods",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.dontstravemc.foods"))
                    .icon(() -> new ItemStack(ModItems.MONSTER_MEAT.get()))
                    .displayItems((context, entries) -> TAB_FOODS.forEach(entries::accept))
                    .build()
    );
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> DONTSTRAVE_NATURE_TAB = TheConstant.CREATIVE_MODE_TABS.register(
            "nature",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.dontstravemc.nature"))
                    .icon(() -> new ItemStack(ModBlocks.BERRY_BUSH.asItem()))
                    .displayItems((context, entries) -> TAB_NATURE.forEach(entries::accept))
                    .build()
    );
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> DONTSTRAVE_TOOLS_TAB = TheConstant.CREATIVE_MODE_TABS.register(
            "tools",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.dontstravemc.tools"))
                    .icon(() -> new ItemStack(ModItems.FLINT_PICKAXE.get()))
                    .displayItems((context, entries) -> TAB_TOOLS.forEach(entries::accept))
                    .build()
    );
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> DONTSTRAVE_CLOTH_TAB = TheConstant.CREATIVE_MODE_TABS.register(
            "cloth",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.dontstravemc.cloth"))
                    .icon(() -> new ItemStack(ModItems.FLOWER_HAT.get()))
                    .displayItems((context, entries) -> TAB_CLOTH.forEach(entries::accept))
                    .build()
    );
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> DONTSTRAVE_BLOCKS_TAB = TheConstant.CREATIVE_MODE_TABS.register(
            "blocks",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.dontstravemc.blocks"))
                    .icon(() -> new ItemStack(ModBlocks.RADIO.asItem()))
                    .displayItems((context, entries) -> TAB_BLOCKS.forEach(entries::accept))
                    .build()
    );
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> DONTSTRAVE_WEAPONS_TAB = TheConstant.CREATIVE_MODE_TABS.register(
            "weapons",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.dontstravemc.weapons"))
                    .icon(() -> new ItemStack(ModItems.FLINT_SPEAR.get()))
                    .displayItems((context, entries) -> TAB_WEAPONS.forEach(entries::accept))
                    .build()
    );
    static final DeferredHolder<CreativeModeTab, CreativeModeTab> DONTSTRAVE_MATERIALS_TAB = TheConstant.CREATIVE_MODE_TABS.register(
            "materials",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.dontstravemc.materials"))
                    .icon(() -> new ItemStack(ModItems.NIGHTMAREFUEL.get()))
                    .displayItems((context, entries) -> TAB_MATERIALS.forEach(entries::accept))
                    .build()
    );

    public static void init() {
    }
}
