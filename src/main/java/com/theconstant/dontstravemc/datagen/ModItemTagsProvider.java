package com.theconstant.dontstravemc.datagen;

import com.theconstant.dontstravemc.TheConstant;
import com.theconstant.dontstravemc.block.ModBlocks;
import com.theconstant.dontstravemc.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, TheConstant.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ItemTags.BEDS)
                .add(ModBlocks.PIGMAN_BED.asItem());
    }
}
