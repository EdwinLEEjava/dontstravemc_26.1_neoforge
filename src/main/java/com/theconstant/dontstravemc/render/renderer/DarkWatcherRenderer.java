package com.theconstant.dontstravemc.render.renderer;

import com.theconstant.dontstravemc.entity.ambient.DarkWatcherEntity;
import com.theconstant.dontstravemc.model.DarkWatcherModel;
import com.theconstant.dontstravemc.render.state.DarkWatcherRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import com.geckolib.constant.DataTickets;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.layer.builtin.TextureLayerGeoLayer;

public class DarkWatcherRenderer extends GeoEntityRenderer<DarkWatcherEntity, DarkWatcherRenderState> {

    private static final Identifier EYE_GLOW_TEXTURE =
            Identifier.fromNamespaceAndPath("dontstravemc", "textures/entity/darkwatcher/darkwatcher_eye.png");
    private static final int BODY_ALPHA = 204; // 80%

    public DarkWatcherRenderer(EntityRendererProvider.Context context) {
        super(context, new DarkWatcherModel());
        this.shadowRadius = 0.6F;

        this.withRenderLayer(new TextureLayerGeoLayer<>(this, EYE_GLOW_TEXTURE, RenderTypes::eyes));
    }

    @Override
    public DarkWatcherRenderState createRenderState(DarkWatcherEntity animatable, Void relatedObject) {
        return new DarkWatcherRenderState();
    }

    @Override
    public void extractRenderState(DarkWatcherEntity entity, DarkWatcherRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
    }

    @Nullable
    @Override
    public RenderType getRenderType(DarkWatcherRenderState renderState, Identifier texture) {
        if (renderState.isInvisible && !renderState.getGeckolibData(DataTickets.INVISIBLE_TO_PLAYER)) {
            return RenderTypes.entityTranslucentCullItemTarget(texture);
        }

        if (!renderState.isInvisible) {
            return RenderTypes.entityTranslucentCullItemTarget(texture);
        }

        return renderState.appearsGlowing() ? RenderTypes.outline(texture) : null;
    }

    @Override
    public int getRenderColor(DarkWatcherEntity animatable, Void relatedObject, float partialTick) {
        int color = super.getRenderColor(animatable, relatedObject, partialTick);
        return (BODY_ALPHA << 24) | (color & 0x00FFFFFF);
    }

    @Override
    public int getPackedOverlay(DarkWatcherEntity animatable, Void relatedObject, float u, float partialTick) {
        return OverlayTexture.NO_OVERLAY;
    }
}
