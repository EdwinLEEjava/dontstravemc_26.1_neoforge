package com.theconstant.dontstravemc.model;

import com.theconstant.dontstravemc.entity.trap.TrapEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class TrapModel extends GeoModel<TrapEntity> {
    
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("dontstravemc", "entity/trap");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("dontstravemc", "textures/entity/trap.png");
    }

    @Override
    public Identifier getAnimationResource(TrapEntity animatable) {
        return Identifier.fromNamespaceAndPath("dontstravemc", "entity/trap");
    }
}
