package com.theconstant.dontstravemc.render.renderer;

import com.theconstant.dontstravemc.entity.ambient.CreepyEyesEntity;
import com.theconstant.dontstravemc.model.CreepyEyesModel;
import com.theconstant.dontstravemc.render.state.CreepyEyesRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import com.geckolib.renderer.GeoEntityRenderer;

public class CreepyEyesRenderer extends GeoEntityRenderer<CreepyEyesEntity, CreepyEyesRenderState> {

    public CreepyEyesRenderer(EntityRendererProvider.Context context) {
        super(context, new CreepyEyesModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public CreepyEyesRenderState createRenderState(CreepyEyesEntity animatable, Void relatedObject) {
        return new CreepyEyesRenderState();
    }

    @Override
    public void extractRenderState(CreepyEyesEntity entity, CreepyEyesRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.textureIndex = entity.getTextureIndex();
    }

    @Override
    public RenderType getRenderType(CreepyEyesRenderState renderState, Identifier texture) {
        // Use eyes render type for full-bright glow (same as DarkWatcher eye layer)
        return RenderTypes.eyes(texture);
    }

    @Override
    public int getPackedOverlay(CreepyEyesEntity animatable, Void relatedObject, float u, float partialTick) {
        return OverlayTexture.NO_OVERLAY;
    }
}
