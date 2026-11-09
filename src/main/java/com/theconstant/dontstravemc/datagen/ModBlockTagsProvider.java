package com.theconstant.dontstravemc.datagen;

import com.theconstant.dontstravemc.TheConstant;
import com.theconstant.dontstravemc.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, TheConstant.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(ModBlocks.CONSTANT_BIRCH_TREE_LOG.get())
                .add(ModBlocks.EVERGREEN_FENCE.get())
                .add(ModBlocks.EVERGREEN_PLANKS.get())
                .add(ModBlocks.EVERGREEN_BUTTON.get())
                .add(ModBlocks.EVERGREEN_DOOR.get())
                .add(ModBlocks.EVERGREEN_LOG.get())
                .add(ModBlocks.EVERGREEN_FENCE_GATE.get())
                .add(ModBlocks.EVERGREEN_PRESSURE_PLATE.get())
                .add(ModBlocks.EVERGREEN_SLAB.get())
                .add(ModBlocks.EVERGREEN_STAIRS.get())
                .add(ModBlocks.EVERGREEN_TRAPDOOR.get())
                .add(ModBlocks.EVERGREEN_WOOD.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.RADIO.get())
                .add(ModBlocks.TURF_CAVE_ROCKY.get())
                .add(ModBlocks.UNDERROCK.get());
    }
}
