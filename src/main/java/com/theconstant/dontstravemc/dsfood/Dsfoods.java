package com.theconstant.dontstravemc.dsfood;

import com.theconstant.dontstravemc.dsfood.custom.DsFoodItem;
import com.theconstant.dontstravemc.dsfood.custom.DsPlaceableFoodItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class Dsfoods extends Foods {

        public record FoodConfig(FoodProperties foodProperties, float healthMod, float sanityMod) {

        public DsFoodItem createItem(Item.Properties properties) {
                return new DsFoodItem(properties.food(foodProperties), healthMod, sanityMod);
            }

        public DsPlaceableFoodItem createPlaceableItem(Block blockToPlace, Item.Properties properties) {
            return new DsPlaceableFoodItem(
                    blockToPlace,
                    properties.food(foodProperties),
                    healthMod,
                    sanityMod
            );
        }

        }



    public static final FoodConfig MONSTER_MEAT_FOOD = new FoodConfig(
            new FoodProperties.Builder()
                    .nutrition(3)
                    .saturationModifier(0.1f)
                    .alwaysEdible()
                    .build(),
            -2.5f, -15.0f
    );

    public static final FoodConfig COOKED_MONSTER_MEAT_FOOD = new FoodConfig(
            new FoodProperties.Builder()
                    .nutrition(4)
                    .saturationModifier(0.3f)
                    .alwaysEdible()
                    .build(),
            0f, -10.0f
    );

    public static final FoodConfig MONSTER_MEAT_JERKY_FOOD = new FoodConfig(
            new FoodProperties.Builder()
                    .nutrition(6)
                    .saturationModifier(0.6f)
                    .alwaysEdible()
                    .build(),
            0f, -5.0f
    );

    public static final FoodConfig BUTTERFLY_WINGS_FOOD = new FoodConfig(
            new FoodProperties.Builder()
                    .nutrition(1)
                    .saturationModifier(0f)
                    .alwaysEdible()
                    .build(),
            0f, 0f
    );

    public static final FoodConfig BUTTER_FOOD = new FoodConfig(
            new FoodProperties.Builder()
                    .nutrition(8)
                    .saturationModifier(1f)
                    .alwaysEdible()
                    .build(),
            5.0f, 0f
    );

    public static final FoodConfig SPOILED_FOOD = new FoodConfig(
            new FoodProperties.Builder()
                    .nutrition(0)
                    .saturationModifier(0f)
                    .alwaysEdible()
                    .build(),
            0f, 0f
    );

    public static final FoodConfig BERRIES_FOOD = new FoodConfig(
            new FoodProperties.Builder()
                    .nutrition(1)
                    .saturationModifier(0.1f)
                    .build(),
            0f, 0f
    );

    public static final FoodConfig COOKED_BERRIES_FOOD = new FoodConfig(
            new FoodProperties.Builder()
                    .nutrition(2)
                    .saturationModifier(0.3f)
                    .build(),
            0f, 0f
    );

    public static final FoodConfig SMALL_MEAT_FOOD = new FoodConfig(
            new FoodProperties.Builder()
                    .nutrition(2)
                    .saturationModifier(0.1f)
                    .build(),
            0f, -10.0f
    );

    public static final FoodConfig COOKED_SMALL_MEAT_FOOD = new FoodConfig(
            new FoodProperties.Builder()
                    .nutrition(2)
                    .saturationModifier(0.6f)
                    .build(),
            0f, 0f
    );

    public static final FoodConfig SMALL_JERKY_FOOD = new FoodConfig(
            new FoodProperties.Builder()
                    .nutrition(2)
                    .saturationModifier(0.6f)
                    .build(),
            1.0f, 10.0f
    );
    public static final FoodConfig PETALS_FOOD = new FoodConfig(
            new FoodProperties.Builder()
                    .nutrition(0)
                    .saturationModifier(0.0f)
                    .build(),
            1.0f, 0.0f
    );
    public static final FoodConfig EVIL_PETALS_FOOD = new FoodConfig(
            new FoodProperties.Builder()
                    .nutrition(0)
                    .saturationModifier(0.0f)
                    .build(),
            0.0f, -5.0f
    );
    public static final FoodConfig MEAT_FOOD = new FoodConfig(
            new FoodProperties.Builder()
                    .nutrition(3)
                    .saturationModifier(0.2f)
                    .build(),
            1.0f, -10.0f
    );
    public static final FoodConfig MEAT_COOKED_FOOD = new FoodConfig(
            new FoodProperties.Builder()
                    .nutrition(3)
                    .saturationModifier(0.6f)
                    .build(),
            1.0f, 10.0f
    );
    public static final FoodConfig MEAT_JERKY_FOOD = new FoodConfig(
            new FoodProperties.Builder()
                    .nutrition(3)
                    .saturationModifier(0.6f)
                    .build(),
            3.0f, 15.0f
    );
    public static FoodConfig RED_MUSHROOM_FOOD = new FoodConfig(
            new FoodProperties.Builder()
                    .nutrition(3)
                    .saturationModifier(0f)
                    .build(),
            -2f, -10f
    );
    public static FoodConfig GREEN_MUSHROOM_FOOD = new FoodConfig(
            new FoodProperties.Builder()
                    .nutrition(3)
                    .saturationModifier(0f)
                    .build(),
            4f, -20f
    );
    public static FoodConfig BLUE_MUSHROOM_FOOD = new FoodConfig(
            new FoodProperties.Builder()
                    .nutrition(3)
                    .saturationModifier(0f)
                    .build(),
            -2f, 15f
    );

    public static FoodConfig BIRD_EGG_FOOD = new FoodConfig(
            new FoodProperties.Builder()
                    .nutrition(3)
                    .saturationModifier(1f)
                    .build(),
            0f,0f
    );
    public static FoodConfig BIRD_EGG_COOKED_FOOD = new FoodConfig(
            new FoodProperties.Builder()
                    .nutrition(3)
                    .saturationModifier(1f)
                    .build(),
            1f,0f
    );
    public static FoodConfig BIRD_LEG_FOOD = new FoodConfig(
            new FoodProperties.Builder()
                    .nutrition(3)
                    .saturationModifier(1f)
                    .build(),
            0f,0f
    );
    public static FoodConfig BIRD_LEG_COOKED_FOOD = new FoodConfig(
            new FoodProperties.Builder()
                    .nutrition(3)
                    .saturationModifier(1f)
                    .build(),
            1f,0f
    );
    public static FoodConfig MEAT_BALL_FOOD = new FoodConfig(
            new FoodProperties.Builder()
                    .nutrition(6)
                    .saturationModifier(3f)
                    .build(),
            1f,5f
    );
    public static FoodConfig MONSTER_LASAGNA_FOOD = new FoodConfig(
            new FoodProperties.Builder()
                    .nutrition(8)
                    .saturationModifier(4f)
                    .build(),
            -5f,-30f
    );
    public static FoodConfig TALL_BIRD_EGG_FOOD = new FoodConfig(
            new FoodProperties.Builder()
                    .nutrition(3)
                    .saturationModifier(1f)
                    .build(),
            0f,0f
    );
    public static FoodConfig TALL_BIRD_SCOTCH_EGGS_FOOD = new FoodConfig(
            new FoodProperties.Builder()
                    .nutrition(20)
                    .saturationModifier(4f)
                    .build(),
            10f,30f
    );
    public static FoodConfig TALL_BIRD_EGG_COOKED_FOOD = new FoodConfig(
            new FoodProperties.Builder()
                    .nutrition(3)
                    .saturationModifier(1f)
                    .build(),
            1f,0f
    );

}
