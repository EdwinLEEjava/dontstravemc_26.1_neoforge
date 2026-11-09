package com.theconstant.dontstravemc.model;

import com.theconstant.dontstravemc.entity.neutral.PigmanEntity;
import com.theconstant.dontstravemc.render.state.PigmanRenderState;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class PigmanModel extends GeoModel<PigmanEntity> {
    private static final Identifier DEFAULT_TEXTURE = Identifier.fromNamespaceAndPath(
            "dontstravemc", "textures/entity/pigman/pigman.png");
    private static final Identifier SLEEP_TEXTURE = Identifier.fromNamespaceAndPath(
            "dontstravemc", "textures/entity/pigman/pigman_sleep.png");
    private static final Identifier SMILE_TEXTURE = Identifier.fromNamespaceAndPath(
            "dontstravemc", "textures/entity/pigman/pigman_smile.png");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("dontstravemc", "entity/pigman");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        if (renderState instanceof PigmanRenderState pigmanState) {
            if (pigmanState.isSleeping) {
                return SLEEP_TEXTURE;
            }
            if (pigmanState.useSmileTexture) {
                return SMILE_TEXTURE;
            }
        }
        return DEFAULT_TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(PigmanEntity animatable) {
        return Identifier.fromNamespaceAndPath("dontstravemc", "entity/pigman");
    }
}
