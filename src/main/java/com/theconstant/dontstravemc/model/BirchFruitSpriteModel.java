package com.theconstant.dontstravemc.model;

import com.theconstant.dontstravemc.entity.monster.BirchFruitSpriteEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class BirchFruitSpriteModel extends GeoModel<BirchFruitSpriteEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("dontstravemc", "entity/birch_fruit_sprite");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("dontstravemc", "textures/entity/birch_fruit_sprite/birch_fruit_sprite.png");
    }

    @Override
    public Identifier getAnimationResource(BirchFruitSpriteEntity animatable) {
        return Identifier.fromNamespaceAndPath("dontstravemc", "entity/birch_fruit_sprite");
    }
}
