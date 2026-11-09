package com.theconstant.dontstravemc.model;

import com.theconstant.dontstravemc.entity.ambient.DarkWatcherEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class DarkWatcherModel extends GeoModel<DarkWatcherEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("dontstravemc", "entity/dark_watcher");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("dontstravemc", "textures/entity/darkwatcher/darkwatcher_body.png");
    }

    @Override
    public Identifier getAnimationResource(DarkWatcherEntity animatable) {
        return Identifier.fromNamespaceAndPath("dontstravemc", "entity/dark_watcher");
    }
}
