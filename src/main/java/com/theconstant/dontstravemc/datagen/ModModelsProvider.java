package com.theconstant.dontstravemc.datagen;

import com.theconstant.dontstravemc.TheConstant;
import com.theconstant.dontstravemc.block.ModBlocks;
import com.theconstant.dontstravemc.item.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.stream.Stream;

public class ModModelsProvider extends ModelProvider {
    public ModModelsProvider(PackOutput output) {
        super(output, TheConstant.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(ModItems.FLINT_PICKAXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);


        blockModels.createCrossBlock(ModBlocks.CONSTANT_BIRCH_SAPLING.get(), BlockModelGenerators.PlantType.NOT_TINTED);
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return super.getKnownBlocks();
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return super.getKnownItems();
    }
}
