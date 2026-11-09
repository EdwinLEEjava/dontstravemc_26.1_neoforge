package com.theconstant.dontstravemc.render.renderer;

import com.theconstant.dontstravemc.entity.monster.BirchFruitSpriteEntity;
import com.theconstant.dontstravemc.model.BirchFruitSpriteModel;
import com.theconstant.dontstravemc.render.state.BirchFruitSpriteRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;

public class BirchFruitSpriteRenderer extends GeoEntityRenderer<BirchFruitSpriteEntity, BirchFruitSpriteRenderState> {
    public BirchFruitSpriteRenderer(EntityRendererProvider.Context context) {
        super(context, new BirchFruitSpriteModel());
        this.shadowRadius = 0.35F;
    }

    @Override
    public BirchFruitSpriteRenderState createRenderState(BirchFruitSpriteEntity animatable, Void relatedObject) {
        return new BirchFruitSpriteRenderState();
    }

    /** 死亡动画自己负责向后倒下，关掉原版的侧翻。 */
    @Override
    protected float getDeathMaxRotation(GeoRenderState renderState) {
        return 0f;
    }

    /** 死亡时不叠加红色受伤滤镜，避免倒地全程发红。 */
    @Override
    public int getPackedOverlay(BirchFruitSpriteEntity animatable, Void relatedObject, float u, float partialTick) {
        if (animatable.isDeadOrDying()) {
            return OverlayTexture.NO_OVERLAY;
        }
        return super.getPackedOverlay(animatable, relatedObject, u, partialTick);
    }
}
