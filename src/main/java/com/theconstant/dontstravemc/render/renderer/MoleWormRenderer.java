package com.theconstant.dontstravemc.render.renderer;

import com.theconstant.dontstravemc.entity.animal.MoleWormEntity;
import com.theconstant.dontstravemc.model.MoleWormModel;
import com.theconstant.dontstravemc.render.state.MoleWormRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.geckolib.renderer.GeoEntityRenderer;

public class MoleWormRenderer extends GeoEntityRenderer<MoleWormEntity, MoleWormRenderState> {
    public MoleWormRenderer(EntityRendererProvider.Context context) {
        super(context, new MoleWormModel());
        this.shadowRadius = 0.5F;
    }
    @Override
    public MoleWormRenderState createRenderState(MoleWormEntity animatable, Void relatedObject) {
        return new MoleWormRenderState();
    }
    @Override
    public void extractRenderState(MoleWormEntity entity, MoleWormRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
    }
}
