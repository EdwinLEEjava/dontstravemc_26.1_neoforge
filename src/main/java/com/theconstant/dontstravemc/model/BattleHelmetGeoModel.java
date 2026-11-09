package com.theconstant.dontstravemc.model;

import com.theconstant.dontstravemc.TheConstant;
import com.theconstant.dontstravemc.item.custom.BattleHelmetItem;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class BattleHelmetGeoModel extends GeoModel<BattleHelmetItem> {

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(TheConstant.MODID, "armor/battle_helmet");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(TheConstant.MODID, "textures/item/battle_helmet.png");
    }

    @Override
    public Identifier getAnimationResource(BattleHelmetItem animatable) {
        return Identifier.fromNamespaceAndPath(TheConstant.MODID, "armor/battle_helmet");
    }
}
