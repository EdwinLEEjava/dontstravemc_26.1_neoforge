package com.theconstant.dontstravemc.item;

import com.theconstant.dontstravemc.TheConstant;

import com.theconstant.dontstravemc.block.ModBlocks;
import com.theconstant.dontstravemc.dsfood.Dsfoods;
import com.theconstant.dontstravemc.dsfood.custom.DsFoodItem;
import com.theconstant.dontstravemc.entity.ModEntities;
import com.theconstant.dontstravemc.item.custom.PineconeItem;
import com.theconstant.dontstravemc.item.custom.SpoiledFoodItem;
import com.theconstant.dontstravemc.item.custom.PlaceableTwigsItem;
import com.theconstant.dontstravemc.item.custom.TrapItem;
import com.theconstant.dontstravemc.item.custom.BattleHelmetItem;
import com.theconstant.dontstravemc.item.custom.FlowerHatItem;
import com.theconstant.dontstravemc.item.custom.StrawHatItem;
import com.theconstant.dontstravemc.item.custom.HamBatItem;
import com.theconstant.dontstravemc.item.custom.HammerItem;
import com.theconstant.dontstravemc.tag.ModBlockTags;
import com.theconstant.dontstravemc.item.custom.TorchItem;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TypedEntityData;
import net.neoforged.neoforge.registries.DeferredItem;

import static com.theconstant.dontstravemc.item.custom.ModsToolMaterial.*;

public class ModItems {

    private static Item.Properties itemProps(String name) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(TheConstant.MODID, name)));
    }

    public static final DeferredItem<Item> MONSTER_MEAT = TheConstant.ITEMS.register("monster_meat",
            () -> Dsfoods.MONSTER_MEAT_FOOD.createItem(itemProps("monster_meat")));
    public static final DeferredItem<Item> COOKED_MONSTER_MEAT = TheConstant.ITEMS.register("cooked_monster_meat",
            () -> Dsfoods.COOKED_MONSTER_MEAT_FOOD.createItem(itemProps("cooked_monster_meat")));
    public static final DeferredItem<Item> MONSTER_MEAT_JERKY = TheConstant.ITEMS.register("monster_meat_jerky",
            () -> Dsfoods.MONSTER_MEAT_JERKY_FOOD.createItem(itemProps("monster_meat_jerky")));
    public static final DeferredItem<Item> BUTTER = TheConstant.ITEMS.register("butter",
            () -> Dsfoods.BUTTER_FOOD.createPlaceableItem(ModBlocks.BUTTER_BLOCK.get(),itemProps("butter")));
    public static final DeferredItem<Item> BUTTERFLY_WINGS = TheConstant.ITEMS.register("butterfly_wings",
            () -> Dsfoods.BUTTERFLY_WINGS_FOOD.createItem(itemProps("butterfly_wings")));
    public static final DeferredItem<Item> SPOILED_FOOD = TheConstant.ITEMS.register("spoiled_food",
            () -> new SpoiledFoodItem(itemProps("spoiled_food").food(Dsfoods.SPOILED_FOOD.foodProperties())));
    public static final DeferredItem<Item> NIGHTMAREFUEL = TheConstant.ITEMS.register("nightmarefuel", () -> new Item(itemProps("nightmarefuel")));
    public static final DeferredItem<Item> TWIGS = TheConstant.ITEMS.register("twigs", () -> new PlaceableTwigsItem(itemProps("twigs")));
    public static final DeferredItem<Item> CUT_GRASS = TheConstant.ITEMS.register("cut_grass", () -> new Item(itemProps("cut_grass")));
    public static final DeferredItem<Item> ROPE = TheConstant.ITEMS.register("rope", () -> new Item(itemProps("rope")));
    public static final DeferredItem<Item> BERRIES = TheConstant.ITEMS.register("berries",
            () -> Dsfoods.BERRIES_FOOD.createItem(itemProps("berries")));
    public static final DeferredItem<Item> COOKED_BERRIES = TheConstant.ITEMS.register("cooked_berries",
            () -> Dsfoods.COOKED_BERRIES_FOOD.createItem(itemProps("cooked_berries")));
    public static final DeferredItem<Item> MEAT_SMALL = TheConstant.ITEMS.register("meat_small",
            () -> Dsfoods.SMALL_MEAT_FOOD.createItem(itemProps("meat_small")));
    public static final DeferredItem<Item> COOKED_MEAT_SMALL = TheConstant.ITEMS.register("meat_small_cooked",
            () -> Dsfoods.COOKED_SMALL_MEAT_FOOD.createItem(itemProps("meat_small_cooked")));
    public static final DeferredItem<Item> MEAT_SMALL_JERKY = TheConstant.ITEMS.register("meat_small_jerky",
            () -> Dsfoods.SMALL_JERKY_FOOD.createItem(itemProps("meat_small_jerky")));
    public static final DeferredItem<Item> PETALS = TheConstant.ITEMS.register("petals",
            () -> Dsfoods.PETALS_FOOD.createItem(itemProps("petals")));
    public static final DeferredItem<Item> EVIL_PETALS = TheConstant.ITEMS.register("evil_petals",
            () -> Dsfoods.EVIL_PETALS_FOOD.createItem(itemProps("evil_petals")));
    public static final DeferredItem<Item> MEAT = TheConstant.ITEMS.register("meat",
            () -> Dsfoods.MEAT_FOOD.createItem(itemProps("meat")));
    public static final DeferredItem<Item> MEAT_COOKED = TheConstant.ITEMS.register("meat_cooked",
            () -> Dsfoods.MEAT_COOKED_FOOD.createItem(itemProps("meat_cooked")));
    public static final DeferredItem<Item> MEAT_JERKY = TheConstant.ITEMS.register("meat_jerky",
            () -> Dsfoods.MEAT_JERKY_FOOD.createItem(itemProps("meat_jerky")));
    public static final DeferredItem<Item> RED_MUSHROOM = TheConstant.ITEMS.register("red_mushroom",
            () -> Dsfoods.RED_MUSHROOM_FOOD.createPlaceableItem(ModBlocks.RED_MUSHROOM.get(),itemProps("red_mushroom")));
    public static final DeferredItem<Item> GREEN_MUSHROOM = TheConstant.ITEMS.register("green_mushroom",
            () -> Dsfoods.GREEN_MUSHROOM_FOOD.createPlaceableItem(ModBlocks.GREEN_MUSHROOM.get(),itemProps("green_mushroom")));
    public static final DeferredItem<Item> BLUE_MUSHROOM = TheConstant.ITEMS.register("blue_mushroom",
            () -> Dsfoods.BLUE_MUSHROOM_FOOD.createPlaceableItem(ModBlocks.BLUE_MUSHROOM.get(),itemProps("blue_mushroom")));
    public static final DeferredItem<Item> PINECONE = TheConstant.ITEMS.register("pinecone", () -> new PineconeItem(itemProps("pinecone")));
    public static final DeferredItem<Item> PIG_SKIN = TheConstant.ITEMS.register("pig_skin", () -> new Item(itemProps("pig_skin")));
    public static final DeferredItem<Item> BIRD_EGG = TheConstant.ITEMS.register("bird_egg",
            () -> Dsfoods.BIRD_EGG_FOOD.createItem(itemProps("bird_egg")));
    public static final DeferredItem<Item> BIRD_EGG_COOKED = TheConstant.ITEMS.register("bird_egg_cooked",
            () -> Dsfoods.BIRD_EGG_COOKED_FOOD.createItem(itemProps("bird_egg_cooked")));
    public static final DeferredItem<Item> BIRD_LEG = TheConstant.ITEMS.register("bird_leg",
            () -> Dsfoods.BIRD_LEG_FOOD.createItem(itemProps("bird_leg")));
    public static final DeferredItem<Item> BIRD_LEG_COOKED = TheConstant.ITEMS.register("bird_leg_cooked",
            () -> Dsfoods.BIRD_LEG_COOKED_FOOD.createItem(itemProps("bird_leg_cooked")));
    public static final DeferredItem<Item> MEAT_BALL = TheConstant.ITEMS.register("meat_ball",
            () -> Dsfoods.MEAT_BALL_FOOD.createItem(itemProps("meat_ball")));
    public static final DeferredItem<Item> MONSTER_LASAGNA = TheConstant.ITEMS.register("monster_lasagna",
            () -> Dsfoods.MONSTER_LASAGNA_FOOD.createItem(itemProps("monster_lasagna")));
    public static final DeferredItem<Item> TALL_BIRD_EGG = TheConstant.ITEMS.register("tall_bird_egg",
            () -> Dsfoods.TALL_BIRD_EGG_FOOD.createItem(itemProps("tall_bird_egg")));
    public static final DeferredItem<Item> TALL_BIRD_EGG_COOKED = TheConstant.ITEMS.register("tall_bird_egg_cooked",
            () -> Dsfoods.TALL_BIRD_EGG_COOKED_FOOD.createItem(itemProps("tall_bird_egg_cooked")));
    public static final DeferredItem<Item> TALL_BIRD_SCOTCH_EGGS = TheConstant.ITEMS.register("tall_bird_scotch_eggs",
            () -> Dsfoods.TALL_BIRD_SCOTCH_EGGS_FOOD.createItem(itemProps("tall_bird_scotch_eggs")));
    public static final DeferredItem<Item> BIRCH_NUT = TheConstant.ITEMS.register("birch_nut",
            () -> new Item(itemProps("birch_nut")));
    /// 工具类
    public static final DeferredItem<Item> FLINT_AXE = TheConstant.ITEMS.register("flint_axe",
            () -> new AxeItem(FLINT_MATERIAL, 4.0F, -3.2F, itemProps("flint_axe")));
    public static final DeferredItem<Item> FLINT_PICKAXE = TheConstant.ITEMS.register("flint_pickaxe",
            () -> new Item(itemProps("flint_pickaxe").pickaxe(FLINT_MATERIAL, 2.0F, -2.4F)));
    public static final DeferredItem<Item> FLINT_SHOVEL = TheConstant.ITEMS.register("flint_shovel",
            () -> new ShovelItem(FLINT_MATERIAL, 2.0F, -3.2F, itemProps("flint_shovel")));
    public static final DeferredItem<Item> RAW_GOLDEN_SHOVEL = TheConstant.ITEMS.register("raw_golden_shovel",
            () -> new ShovelItem(RAW_GOLDEN_MATERIAL, 4.0F, -1.6F, itemProps("raw_golden_shovel")));
    public static final DeferredItem<Item> RAW_GOLDEN_PICKAXE = TheConstant.ITEMS.register("raw_golden_pickaxe",
            () -> new Item(itemProps("raw_golden_pickaxe").pickaxe(RAW_GOLDEN_MATERIAL, 4.0F, -1.2F)));
    public static final DeferredItem<Item> RAW_GOLDEN_AXE = TheConstant.ITEMS.register("raw_golden_axe",
            () -> new AxeItem(RAW_GOLDEN_MATERIAL, 8.0F, -1.6F, itemProps("raw_golden_axe")));
    public static final DeferredItem<Item> TORCH = TheConstant.ITEMS.register("torch",
            () -> new TorchItem(itemProps("torch").stacksTo(1).durability(3000)));
    public static final DeferredItem<Item> HAMMER = TheConstant.ITEMS.register("hammer",
            () -> new HammerItem(itemProps("hammer").tool(FLINT_MATERIAL, ModBlockTags.MINEABLE_HAMMER, 4.0F, -3.2F, 0.0F)));

    /// 武器类
    public static final DeferredItem<Item> FLINT_SPEAR = TheConstant.ITEMS.register("flint_spear",
            () -> new Item(itemProps("flint_spear").sword(FLINT_MATERIAL, 7.0F, -2.4F)));
    public static final DeferredItem<Item> HAM_BAT = TheConstant.ITEMS.register("hambat",
            () -> new HamBatItem(HamBatItem.createProperties(itemProps("hambat"))));
    public static final DeferredItem<Item> MORNING_STAR = TheConstant.ITEMS.register("morning_star",
            () -> new Item(itemProps("morning_star").stacksTo(1)));

    public static final DeferredItem<Item> GRASS_ARMOR = TheConstant.ITEMS.register("grass_armor",
            () -> new Item(FoliageChestplateItems.grassProperties(itemProps("grass_armor"))));
    public static final DeferredItem<Item> WOOD_ARMOR = TheConstant.ITEMS.register("wood_armor",
            () -> new Item(FoliageChestplateItems.woodProperties(itemProps("wood_armor"))));
    public static final DeferredItem<Item> FLOWER_HAT = TheConstant.ITEMS.register("flower_hat",
            () -> new FlowerHatItem(FlowerHatItem.createProperties(itemProps("flower_hat"))));
    public static final DeferredItem<Item> STRAW_HAT = TheConstant.ITEMS.register("straw_hat",
            () -> new StrawHatItem(StrawHatItem.createProperties(itemProps("straw_hat"))));
    public static final DeferredItem<Item> BATTLE_HELMET = TheConstant.ITEMS.register("battle_helmet",
            () -> new BattleHelmetItem(BattleHelmetItem.createProperties(itemProps("battle_helmet"))));

    /// 刷怪蛋
    public static final DeferredItem<Item> BUTTERFLY_SPAWN_EGG = TheConstant.ITEMS.register("butterfly_spawn_egg",
            () -> new SpawnEggItem(itemProps("butterfly_spawn_egg").component(DataComponents.ENTITY_DATA,
                    TypedEntityData.of(ModEntities.BUTTERFLY.get(), createButterflyNbt()))));

    public static final DeferredItem<Item> MONSTER_SPIDER_SPAWN_EGG = TheConstant.ITEMS.register("monster_spider_spawn_egg",
            () -> new SpawnEggItem(itemProps("monster_spider_spawn_egg").component(DataComponents.ENTITY_DATA,
                    TypedEntityData.of(ModEntities.MONSTER_SPIDER.get(), createMonsterSpiderNbt()))));

    public static final DeferredItem<Item> DS_RABBIT_SPAWN_EGG = TheConstant.ITEMS.register("ds_rabbit_spawn_egg",
            () -> new SpawnEggItem(itemProps("ds_rabbit_spawn_egg").component(DataComponents.ENTITY_DATA,
                    TypedEntityData.of(ModEntities.RABBIT.get(), createDsRabbitNbt()))));

    public static final DeferredItem<Item> MOLE_WORM_SPAWN_EGG = TheConstant.ITEMS.register("mole_worm_spawn_egg",
            () -> new SpawnEggItem(itemProps("mole_worm_spawn_egg").component(DataComponents.ENTITY_DATA,
                    TypedEntityData.of(ModEntities.MOLE_WORM.get(), createMoleWormNbt()))));

    public static final DeferredItem<Item> PIGMAN_SPAWN_EGG = TheConstant.ITEMS.register("pigman_spawn_egg",
            () -> new SpawnEggItem(itemProps("pigman_spawn_egg").component(DataComponents.ENTITY_DATA,
                    TypedEntityData.of(ModEntities.PIGMAN.get(), createPigmanNbt()))));

    public static final DeferredItem<Item> BIRCH_FRUIT_SPRITE_SPAWN_EGG = TheConstant.ITEMS.register("birch_fruit_sprite_spawn_egg",
            () -> new SpawnEggItem(itemProps("birch_fruit_sprite_spawn_egg").component(DataComponents.ENTITY_DATA,
                    TypedEntityData.of(ModEntities.BIRCH_FRUIT_SPRITE.get(), createBirchFruitSpriteNbt()))));

    public static final DeferredItem<Item> TRAP = TheConstant.ITEMS.register("trap",
            () -> new TrapItem(itemProps("trap").stacksTo(1).durability(8)));

    private static CompoundTag createButterflyNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("id", "dontstravemc:butterfly");
        return nbt;
    }

    private static CompoundTag createMonsterSpiderNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("id", "dontstravemc:monster_spider");
        return nbt;
    }

    private static CompoundTag createDsRabbitNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("id", "dontstravemc:rabbit");
        return nbt;
    }

    private static CompoundTag createMoleWormNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("id", "dontstravemc:mole_worm");
        return nbt;
    }

    private static CompoundTag createBirchFruitSpriteNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("id", "dontstravemc:birch_fruit_sprite");
        return nbt;
    }

    private static CompoundTag createPigmanNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("id", "dontstravemc:pigman");
        return nbt;
    }

    public static void registerModItems() {
        TheConstant.LOGGER.info("Registering Mod Items for " + TheConstant.MODID);
    }

    /** 非草/木胸甲时返回负值。 */
    public static final float NOT_FOLIAGE_CHEST = -1.0F;

    public static boolean isFoliageChestplate(ItemStack chest) {
        return foliagePlayerDamageFraction(chest) >= 0.0F;
    }

    public static float foliagePlayerDamageFraction(ItemStack chest) {
        if (chest.isEmpty()) return NOT_FOLIAGE_CHEST;
        if (chest.is(GRASS_ARMOR.get())) return 0.4F;
        if (chest.is(WOOD_ARMOR.get())) return 0.2F;
        return NOT_FOLIAGE_CHEST;
    }

    public static float foliageArmorDamageFraction(ItemStack chest) {
        if (chest.isEmpty()) return NOT_FOLIAGE_CHEST;
        if (chest.is(GRASS_ARMOR.get())) return 0.6F;
        if (chest.is(WOOD_ARMOR.get())) return 0.8F;
        return NOT_FOLIAGE_CHEST;
    }
}
