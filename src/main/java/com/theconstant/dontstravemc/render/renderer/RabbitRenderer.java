package com.theconstant.dontstravemc.render.renderer;

import com.theconstant.dontstravemc.entity.animal.RabbitEntity;
import com.theconstant.dontstravemc.model.RabbitModel;
import com.theconstant.dontstravemc.render.state.RabbitRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.geckolib.renderer.GeoEntityRenderer;

public class RabbitRenderer extends GeoEntityRenderer<RabbitEntity, RabbitRenderState> {
    public RabbitRenderer(EntityRendererProvider.Context context) {
        super(context, new RabbitModel());
        this.shadowRadius = 0.4F;
    }

    @Override
    public RabbitRenderState createRenderState(RabbitEntity animatable, Void relatedObject) {
        return new RabbitRenderState();
    }

    @Override
    public void extractRenderState(RabbitEntity entity, RabbitRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
    }
}
