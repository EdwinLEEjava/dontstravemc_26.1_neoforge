package com.theconstant.dontstravemc.model;

import com.theconstant.dontstravemc.TheConstant;

import com.theconstant.dontstravemc.item.custom.StrawHatItem;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class StrawHatGeoModel extends GeoModel<StrawHatItem> {

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(TheConstant.MODID, "armor/strawhat");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(TheConstant.MODID, "textures/entity/equipment/humanoid/strawhat.png");
    }

    @Override
    public Identifier getAnimationResource(StrawHatItem animatable) {
        return Identifier.fromNamespaceAndPath(TheConstant.MODID, "armor/strawhat");
    }
}
