package com.theconstant.dontstravemc;

import com.theconstant.dontstravemc.datagen.*;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = TheConstant.MODID)
public class TheConstantDataGenerator {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event){
        event.createProvider(((output, lookupProvider) ->
                new LootTableProvider(output, Set.of(), List.of(new LootTableProvider.SubProviderEntry(
                        ModBlockLootTableProvider::new, LootContextParamSets.BLOCK
                )), lookupProvider)));

        event.createProvider(ModRecipeProvider.Runner::new);
        event.createProvider(ModBlockTagsProvider::new);
        event.createProvider(ModItemTagsProvider::new);
        event.createProvider(ModEnUsLangProvider::new);
        event.createProvider(ModZhCnLangProvider::new);
        event.createProvider(ModModelsProvider::new);
    }
}
