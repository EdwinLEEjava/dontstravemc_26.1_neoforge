package com.theconstant.dontstravemc.model;

import com.theconstant.dontstravemc.entity.ambient.CreepyEyesEntity;
import com.theconstant.dontstravemc.render.state.CreepyEyesRenderState;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class CreepyEyesModel extends GeoModel<CreepyEyesEntity> {

    private static final Identifier MODEL =
            Identifier.fromNamespaceAndPath("dontstravemc", "entity/creepyeyes");

    private static final Identifier ANIMATION =
            Identifier.fromNamespaceAndPath("dontstravemc", "entity/creepeyes");

    // Three texture variants
    private static final Identifier TEX_1 =
            Identifier.fromNamespaceAndPath("dontstravemc",
                    "textures/entity/creepyeyes/creepyeyes_1.png");
    private static final Identifier TEX_2 =
            Identifier.fromNamespaceAndPath("dontstravemc",
                    "textures/entity/creepyeyes/creepyeyes_2.png");
    private static final Identifier TEX_3 =
            Identifier.fromNamespaceAndPath("dontstravemc",
                    "textures/entity/creepyeyes/creepyeye_3.png");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        if (renderState instanceof CreepyEyesRenderState state) {
            return switch (state.textureIndex) {
                case 2  -> TEX_2;
                case 3  -> TEX_3;
                default -> TEX_1;
            };
        }
        return TEX_1;
    }

    @Override
    public Identifier getAnimationResource(CreepyEyesEntity animatable) {
        return ANIMATION;
    }
}
