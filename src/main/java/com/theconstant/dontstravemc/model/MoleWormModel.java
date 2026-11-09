package com.theconstant.dontstravemc.model;

import com.theconstant.dontstravemc.entity.animal.MoleWormEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class MoleWormModel extends GeoModel<MoleWormEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("dontstravemc", "entity/mole_worm");
    }
    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("dontstravemc","textures/entity/mole/mole_worm.png");
    }
    @Override
    public Identifier getAnimationResource(MoleWormEntity animatable) {
        return Identifier.fromNamespaceAndPath("dontstravemc", "entity/mole_worm");
    }
}
