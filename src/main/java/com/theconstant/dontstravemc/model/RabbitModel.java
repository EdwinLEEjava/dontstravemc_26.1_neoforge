package com.theconstant.dontstravemc.model;

import com.theconstant.dontstravemc.entity.animal.RabbitEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class RabbitModel extends GeoModel<RabbitEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("dontstravemc", "entity/ds_rabbit");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("dontstravemc", "textures/entity/rabbit/ds_rabbit.png");
    }

    @Override
    public Identifier getAnimationResource(RabbitEntity animatable) {
        return Identifier.fromNamespaceAndPath("dontstravemc", "entity/ds_rabbit");
    }
}
