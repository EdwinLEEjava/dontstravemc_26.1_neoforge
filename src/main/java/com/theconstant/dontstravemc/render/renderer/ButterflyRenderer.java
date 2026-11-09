package com.theconstant.dontstravemc.render.renderer;

import com.theconstant.dontstravemc.entity.animal.ButterflyEntity;
import com.theconstant.dontstravemc.model.ButterflyModel;
import com.theconstant.dontstravemc.render.state.ButterflyRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;

public class ButterflyRenderer extends GeoEntityRenderer<ButterflyEntity, ButterflyRenderState> {
    public ButterflyRenderer(EntityRendererProvider.Context context) {
        super(context, new ButterflyModel());
        this.shadowRadius = 0.2F;
    }

    @Override
    public ButterflyRenderState createRenderState(ButterflyEntity animatable, Void relatedObject) {
        return new ButterflyRenderState();
    }

    /** 死亡由 GeckoLib death 动画表现，不用原版侧翻。 */
    @Override
    protected float getDeathMaxRotation(GeoRenderState renderState) {
        return 0f;
    }
}
