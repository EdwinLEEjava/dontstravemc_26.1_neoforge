package com.theconstant.dontstravemc.datagen;

import com.theconstant.dontstravemc.TheConstant;
import com.theconstant.dontstravemc.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.concurrent.CompletableFuture;


public class ModRecipeProvider extends RecipeProvider {
    protected ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    private static final List<ItemLike> TWIG_LIST = List.of(ModItems.TWIGS, Items.STICK);
    private static final List<ItemLike> MEAT_LIST = List.of(ModItems.MEAT);
    private static final List<ItemLike> SMALL_MEAT_LIST = List.of(ModItems.MEAT_SMALL);
    private static final List<ItemLike> MONSTER_MEAT_LIST = List.of(ModItems.MONSTER_MEAT);

    @Override
    protected void buildRecipes() {
        oreSmelting(MEAT_LIST, RecipeCategory.FOOD, CookingBookCategory.FOOD, ModItems.MEAT_COOKED, 0.7F, 200, "meat");
        oreSmelting(SMALL_MEAT_LIST, RecipeCategory.FOOD, CookingBookCategory.FOOD, ModItems.COOKED_MEAT_SMALL, 0.7F, 200, "small_meat");
        oreSmelting(MONSTER_MEAT_LIST, RecipeCategory.FOOD, CookingBookCategory.FOOD, ModItems.COOKED_MONSTER_MEAT, 0.7F, 200, "monster_meat");


        shaped(RecipeCategory.TOOLS, ModItems.TORCH,1)
                .pattern("#")
                .pattern("@")
                .define('#', ModItems.CUT_GRASS)
                .define('@', ModItems.TWIGS)
                .unlockedBy("has_cut_grass",has(ModItems.CUT_GRASS))
                .save(output, TheConstant.MODID + ":torch");

    }

    protected void oreSmelting(List<ItemLike> smeltables, RecipeCategory craftingCategory, CookingBookCategory cookingCategory, ItemLike result, float experience, int cookingTime, String group) {
        this.oreCooking(SmeltingRecipe::new, smeltables, craftingCategory, cookingCategory, result, experience, cookingTime, group, "_from_smelting");
    }

    protected void oreBlasting(List<ItemLike> smeltables, RecipeCategory craftingCategory, CookingBookCategory cookingCategory, ItemLike result, float experience, int cookingTime, String group) {
        this.oreCooking(BlastingRecipe::new, smeltables, craftingCategory, cookingCategory, result, experience, cookingTime, group, "_from_blasting");
    }

    protected <T extends AbstractCookingRecipe> void oreCooking(AbstractCookingRecipe.Factory<T> factory, List<ItemLike> smeltables, RecipeCategory craftingCategory, CookingBookCategory cookingCategory, ItemLike result, float experience, int cookingTime, String group, String fromDesc) {
        for (ItemLike item : smeltables) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(item), craftingCategory, cookingCategory, result, experience, cookingTime, factory).group(group).unlockedBy(getHasName(item),
                    this.has(item)).save(this.output, TheConstant.MODID + ":" + fromDesc + "_" + getItemName(item));
        }
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput recipeOutput) {
            return new ModRecipeProvider(provider, recipeOutput);
        }

        @Override
        public String getName() {
            return "receipe gen";
        }
    }
}
