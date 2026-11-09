package com.theconstant.dontstravemc.datagen;

import com.theconstant.dontstravemc.TheConstant;
import com.theconstant.dontstravemc.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModEnUsLangProvider extends LanguageProvider {
    public ModEnUsLangProvider(PackOutput output) {
        super(output, TheConstant.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add(ModItems.BERRIES.get(), "Berries");
        add("itemGroup.dontstravemc.main", "The Constant Item");
    }
}
