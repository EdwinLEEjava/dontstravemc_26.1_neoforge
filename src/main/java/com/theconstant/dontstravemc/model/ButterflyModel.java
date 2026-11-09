package com.theconstant.dontstravemc.model;

import com.theconstant.dontstravemc.entity.animal.ButterflyEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class ButterflyModel extends GeoModel<ButterflyEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("dontstravemc", "entity/butterfly");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("dontstravemc", "textures/entity/butterfly.png");
    }

    @Override
    public Identifier getAnimationResource(ButterflyEntity animatable) {
        return Identifier.fromNamespaceAndPath("dontstravemc", "entity/butterfly");
    }
}
